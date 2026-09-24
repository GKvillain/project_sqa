package org.apache.commons.math3.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

public class SimplexTableauTest {

    private static final double EPSILON = 1.0e-6;

    // Tests tableau creation with LEQ constraints and non-negative restriction
    @Test
    public void testCreateTableau_leqConstraints_nonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(2, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(2, tableau.getNumSlackVariables());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(3, tableau.getHeight());
        Assert.assertEquals(6, tableau.getWidth());
    }

    // Tests tableau creation with GEQ and EQ constraints requiring Phase 1 objective and artificial variables
    @Test
    public void testCreateTableau_geqAndEqConstraints_phase1Required() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 10);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.EQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);

        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(1, tableau.getNumSlackVariables());
        Assert.assertEquals(2, tableau.getNumArtificialVariables());
        Assert.assertEquals(4, tableau.getHeight());
        Assert.assertEquals(8, tableau.getWidth());
    }

    // Tests tableau creation when variables are unrestricted in sign (restrictToNonNegative = false)
    @Test
    public void testCreateTableau_unrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 4 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);

        Assert.assertEquals(3, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(1, tableau.getNumSlackVariables());
    }

    // Tests constraint normalization with negative RHS
    @Test
    public void testNormalizeConstraints_negativeRhs_invertsCoefficientsAndRelationship() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -2 }, Relationship.LEQ, -5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.normalizeConstraints(constraints);

        Assert.assertEquals(1, normalized.size());
        LinearConstraint c = normalized.get(0);
        Assert.assertEquals(5.0, c.getValue(), EPSILON);
        Assert.assertEquals(Relationship.GEQ, c.getRelationship());
        Assert.assertEquals(-1.0, c.getCoefficients().getEntry(0), EPSILON);
        Assert.assertEquals(2.0, c.getCoefficients().getEntry(1), EPSILON);
    }

    // Tests dropPhase1Objective for single objective problem (no-op)
    @Test
    public void testDropPhase1Objective_singleObjective_noChange() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int initialHeight = tableau.getHeight();
        int initialWidth = tableau.getWidth();

        tableau.dropPhase1Objective();

        Assert.assertEquals(initialHeight, tableau.getHeight());
        Assert.assertEquals(initialWidth, tableau.getWidth());
    }

    // Tests dropPhase1Objective removes artificial variables and phase 1 objective row
    @Test
    public void testDropPhase1Objective_twoObjectives_dropsRowAndColumns() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(1, tableau.getNumArtificialVariables());

        tableau.dropPhase1Objective();

        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(2, tableau.getHeight());
    }

    // Tests dropPhase1Objective with positive cost columns that should be dropped (detecting precision issues)
    @Test
    public void testDropPhase1Objective_positiveCostColumns_droppedCorrectly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 1));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6, 10);
        int origWidth = tableau.getWidth();
        tableau.dropPhase1Objective();

        Assert.assertTrue(tableau.getWidth() < origWidth);
    }

    // Tests basic row identification for basic and non-basic columns
    @Test
    public void testGetBasicRow_basicAndNonBasicColumns() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Slack variable column should be basic in constraint row (row 1)
        int slackCol = tableau.getSlackVariableOffset();
        Integer basicRow = tableau.getBasicRow(slackCol);
        Assert.assertNotNull(basicRow);
        Assert.assertEquals(Integer.valueOf(1), basicRow);

        // Objective column Z should be basic in row 0
        Integer zRow = tableau.getBasicRow(0);
        Assert.assertNotNull(zRow);
        Assert.assertEquals(Integer.valueOf(0), zRow);

        // Decision variable column is non-basic initially
        Integer xRow = tableau.getBasicRow(1);
        Assert.assertNull(xRow);
    }

    // Tests isOptimal logic for tableau
    @Test
    public void testIsOptimal_optimalAndNonOptimalStates() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        // Initially not optimal because maximize puts negative coefficients in row 0
        Assert.assertFalse(tableau.isOptimal());

        // Set all objective row entries to non-negative
        for (int j = 0; j < tableau.getWidth(); j++) {
            tableau.setEntry(0, j, 1.0);
        }
        Assert.assertTrue(tableau.isOptimal());
    }

    // Tests getSolution when variables are non-negative
    @Test
    public void testGetSolution_nonNegativeVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 5);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Transform tableau so x0 and x1 are basic
        tableau.setEntry(0, 1, 0); // x0 in obj row
        tableau.setEntry(0, 2, 0); // x1 in obj row
        tableau.setEntry(1, 3, 0); // remove s0 basic
        tableau.setEntry(2, 4, 0); // remove s1 basic

        PointValuePair solution = tableau.getSolution();
        Assert.assertEquals(4.0, solution.getPoint()[0], EPSILON);
        Assert.assertEquals(6.0, solution.getPoint()[1], EPSILON);
        Assert.assertEquals(2 * 4.0 + 3 * 6.0 + 5.0, solution.getValue(), EPSILON);
    }

    // Tests getSolution with unrestricted variables
    @Test
    public void testGetSolution_unrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, -1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);
        PointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
        Assert.assertEquals(2, solution.getPoint().length);
    }

    // Tests row operations: divideRow and subtractRow
    @Test
    public void testRowOperations_divideAndSubtract() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 4 }, Relationship.LEQ, 8));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int row = 1;

        tableau.divideRow(row, 2.0);
        Assert.assertEquals(1.0, tableau.getEntry(row, 1), EPSILON);
        Assert.assertEquals(2.0, tableau.getEntry(row, 2), EPSILON);
        Assert.assertEquals(4.0, tableau.getEntry(row, tableau.getRhsOffset()), EPSILON);

        tableau.subtractRow(0, row, 1.0);
        Assert.assertEquals(-1.0 - 1.0, tableau.getEntry(0, 1), EPSILON);
    }

    // Tests static helper getInvertedCoefficientSum
    @Test
    public void testGetInvertedCoefficientSum() {
        ArrayRealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 3.0 });
        double sum = SimplexTableau.getInvertedCoefficientSum(vector);
        Assert.assertEquals(-2.0, sum, EPSILON);
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f3 = new LinearObjectiveFunction(new double[] { 2, 2 }, 0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 3));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 3));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t3 = new SimplexTableau(f3, c1, GoalType.MAXIMIZE, true, EPSILON);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertEquals(t1.hashCode(), t2.hashCode());

        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals("String"));
        Assert.assertFalse(t1.equals(t3));
    }

    // Tests serialization and deserialization of SimplexTableau
    @Test
    public void testSerialization() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 3));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        Assert.assertEquals(original.getWidth(), deserialized.getWidth());
        Assert.assertEquals(original.getHeight(), deserialized.getHeight());
        Assert.assertEquals(original, deserialized);
    }

    // Tests getData accessor
    @Test
    public void testGetData() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        double[][] data = tableau.getData();

        Assert.assertEquals(tableau.getHeight(), data.length);
        Assert.assertEquals(tableau.getWidth(), data[0].length);
    }
}
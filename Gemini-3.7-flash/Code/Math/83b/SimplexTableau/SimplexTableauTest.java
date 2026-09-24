package org.apache.commons.math.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimplexTableauTest {

    private static final double EPSILON = 1.0e-6;

    // Tests tableau construction for a simple maximization problem with LEQ constraints
    @Test
    public void testConstructTableau_maximizeLEQ_correctDimensionsAndValues() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(3, tableau.getHeight());
        assertEquals(6, tableau.getWidth()); // 1 (z) + 2 (decision) + 2 (slack) + 1 (rhs)
        assertEquals(5, tableau.getRhsOffset());
        assertEquals(3, tableau.getSlackVariableOffset());
    }

    // Tests tableau construction for a problem with unrestricted variables (restrictToNonNegative = false)
    @Test
    public void testConstructTableau_unrestrictedVariables_addsNegativeDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, -2 }, 5);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(3, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(3, tableau.getNegativeDecisionVariableOffset());
        assertEquals(4, tableau.getSlackVariableOffset());
    }

    // Tests tableau construction with EQ and GEQ constraints creating artificial variables and 2-phase objective
    @Test
    public void testConstructTableau_withArtificialVariables_twoPhaseObjective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());
        assertEquals(4, tableau.getHeight()); // 2 objective + 2 constraints
        assertEquals(8, tableau.getWidth());  // 2 objective + 2 decision + 1 slack + 2 artificial + 1 rhs
        assertEquals(5, tableau.getArtificialVariableOffset());
    }

    // Tests constraint normalization when right-hand side is negative
    @Test
    public void testNormalizeConstraints_negativeRhs_invertsCoefficientsAndRelationship() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -2 }, Relationship.LEQ, -10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(1, normalized.size());
        LinearConstraint normalizedConstraint = normalized.get(0);
        assertEquals(10.0, normalizedConstraint.getValue(), EPSILON);
        assertEquals(Relationship.GEQ, normalizedConstraint.getRelationship());
        assertEquals(-1.0, normalizedConstraint.getCoefficients().getEntry(0), EPSILON);
        assertEquals(2.0, normalizedConstraint.getCoefficients().getEntry(1), EPSILON);
    }

    // Tests inverted coefficient sum calculation
    @Test
    public void testGetInvertedCoeffiecientSum_validVector_returnsNegativeSum() {
        ArrayRealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 4.0 });
        double sum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        assertEquals(-3.0, sum, EPSILON);
    }

    // Tests discarding artificial variables after Phase 1
    @Test
    public void testDiscardArtificialVariables_removesPhaseOneRowAndColumns() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 3));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumArtificialVariables());

        int initialWidth = tableau.getWidth();
        int initialHeight = tableau.getHeight();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(initialHeight - 1, tableau.getHeight());
        assertEquals(initialWidth - 2, tableau.getWidth()); // removed 1 artificial col + 1 phase1 obj col
    }

    // Tests discarding artificial variables when there are none
    @Test
    public void testDiscardArtificialVariables_noArtificialVariables_noChange() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int widthBefore = tableau.getWidth();
        int heightBefore = tableau.getHeight();

        tableau.discardArtificialVariables();

        assertEquals(widthBefore, tableau.getWidth());
        assertEquals(heightBefore, tableau.getHeight());
    }

    // Tests row division and subtraction elementary row operations
    @Test
    public void testRowOperations_divideAndSubtract_modifiesTableauCorrectly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 4 }, Relationship.LEQ, 8));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int constraintRow = 1;

        tableau.divideRow(constraintRow, 2.0);
        assertEquals(1.0, tableau.getEntry(constraintRow, 1), EPSILON);
        assertEquals(2.0, tableau.getEntry(constraintRow, 2), EPSILON);
        assertEquals(4.0, tableau.getEntry(constraintRow, tableau.getRhsOffset()), EPSILON);

        tableau.setEntry(0, 1, 3.0);
        tableau.subtractRow(0, constraintRow, 3.0);
        assertEquals(0.0, tableau.getEntry(0, 1), EPSILON);
    }

    // Tests getSolution method for restricted non-negative variables
    @Test
    public void testGetSolution_basicSolution_returnsCorrectValues() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        RealPointValuePair solution = tableau.getSolution();

        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(2, point.length);
        assertEquals(4.0, point[0], EPSILON);
        assertEquals(6.0, point[1], EPSILON);
        assertEquals(42.0, solution.getValue(), EPSILON);
    }

    // Tests getSolution method for unrestricted variables with negative offset
    @Test
    public void testGetSolution_unrestrictedVariables_adjustsByMostNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);
        RealPointValuePair solution = tableau.getSolution();

        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(2, point.length);
        assertEquals(3.0, point[0], EPSILON);
        assertEquals(2.0, point[1], EPSILON);
    }

    // Tests getData returning full matrix
    @Test
    public void testGetData_returnsValidMatrix() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        double[][] data = tableau.getData();

        assertNotNull(data);
        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f3 = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t3 = new SimplexTableau(f3, c1, GoalType.MAXIMIZE, true, EPSILON);

        assertTrue(t1.equals(t1));
        assertTrue(t1.equals(t2));
        assertEquals(t1.hashCode(), t2.hashCode());

        assertFalse(t1.equals(null));
        assertFalse(t1.equals("non-tableau object"));
        assertFalse(t1.equals(t3));
    }

    // Tests serialization and deserialization of SimplexTableau
    @Test
    public void testSerialization_roundTrip_reconstitutesEqualObject() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 1);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 5));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getEntry(0, 0), deserialized.getEntry(0, 0), EPSILON);
    }

    // Tests isOptimal method when optimal and when not optimal
    @Test
    public void testIsOptimal_optimalAndNonOptimalCases() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        // Initially, objective function coefficients in row 0 are negative for MAXIMIZE
        assertFalse(tableau.isOptimal());

        // Make all entries in row 0 non-negative
        for (int j = 0; j < tableau.getWidth() - 1; j++) {
            tableau.setEntry(0, j, 0.0);
        }
        assertTrue(tableau.isOptimal());
    }

    // Tests getBasicRow with basic and non-basic columns
    @Test
    public void testGetBasicRow_basicAndNonBasicColumns() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        // Slack variable column (index 3) is a unit column in row 1
        Integer basicRow = tableau.getBasicRow(tableau.getSlackVariableOffset());
        assertNotNull(basicRow);
        assertEquals(Integer.valueOf(1), basicRow);

        // Column with multiple non-zero values is not basic
        tableau.setEntry(0, tableau.getSlackVariableOffset(), 1.0);
        assertNull(tableau.getBasicRow(tableau.getSlackVariableOffset()));

        // Column with no 1.0 entry is not basic
        tableau.setEntry(0, tableau.getSlackVariableOffset(), 0.0);
        tableau.setEntry(1, tableau.getSlackVariableOffset(), 2.0);
        assertNull(tableau.getBasicRow(tableau.getSlackVariableOffset()));
    }

    // Tests getSolution when non-basic variable is zero and negative variable is active in unrestricted problem
    @Test
    public void testGetSolution_nonBasicZeroAndNegativeVariableBasic() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, -1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);
        // Set the negative decision variable column (index 3) to basic in row 1 with RHS value 2.0
        tableau.setEntry(1, 1, 0.0);
        tableau.setEntry(1, 2, 0.0);
        tableau.setEntry(1, tableau.getNegativeDecisionVariableOffset(), 1.0);
        tableau.setEntry(1, tableau.getRhsOffset(), 2.0);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        double[] point = solution.getPoint();
        // Since x1 and x2 are non-basic (null basic row -> 0), x1 = 0 - 2 = -2, x2 = 0 - 2 = -2
        assertEquals(-2.0, point[0], EPSILON);
        assertEquals(-2.0, point[1], EPSILON);
    }

    // Tests equals comparison branches for all fields
    @Test
    public void testEquals_allFieldBranches() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 2, 2 }, Relationship.LEQ, 8));

        SimplexTableau base = new SimplexTableau(f, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau diffRestrict = new SimplexTableau(f, c1, GoalType.MAXIMIZE, false, EPSILON);
        SimplexTableau diffGoal = new SimplexTableau(f, c1, GoalType.MINIMIZE, true, EPSILON);
        SimplexTableau diffEps = new SimplexTableau(f, c1, GoalType.MAXIMIZE, true, 1.0e-4);
        SimplexTableau diffConstraints = new SimplexTableau(f, c2, GoalType.MAXIMIZE, true, EPSILON);

        SimplexTableau diffTableauData = new SimplexTableau(f, c1, GoalType.MAXIMIZE, true, EPSILON);
        diffTableauData.setEntry(0, 0, 999.0);

        assertFalse(base.equals(diffRestrict));
        assertFalse(base.equals(diffGoal));
        assertFalse(base.equals(diffEps));
        assertFalse(base.equals(diffConstraints));
        assertFalse(base.equals(diffTableauData));
    }

    // Tests constraint normalization with negative RHS on EQ and GEQ relationships
    @Test
    public void testNormalizeConstraints_negativeRhsOnEqAndGeq() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, -3 }, Relationship.EQ, -6));
        constraints.add(new LinearConstraint(new double[] { -1, 4 }, Relationship.GEQ, -8));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(2, normalized.size());

        LinearConstraint eqConstraint = normalized.get(0);
        assertEquals(6.0, eqConstraint.getValue(), EPSILON);
        assertEquals(Relationship.EQ, eqConstraint.getRelationship());
        assertEquals(-2.0, eqConstraint.getCoefficients().getEntry(0), EPSILON);
        assertEquals(3.0, eqConstraint.getCoefficients().getEntry(1), EPSILON);

        LinearConstraint geqConstraint = normalized.get(1);
        assertEquals(8.0, geqConstraint.getValue(), EPSILON);
        assertEquals(Relationship.LEQ, geqConstraint.getRelationship());
        assertEquals(1.0, geqConstraint.getCoefficients().getEntry(0), EPSILON);
        assertEquals(-4.0, geqConstraint.getCoefficients().getEntry(1), EPSILON);
    }
}
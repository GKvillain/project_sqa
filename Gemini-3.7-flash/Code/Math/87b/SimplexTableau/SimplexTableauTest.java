package org.apache.commons.math.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimplexTableauTest {

    private static final double EPSILON = 1.0e-6;

    // Tests tableau construction for a standard maximization problem with LEQ constraints
    @Test
    public void testConstructor_standardMaximizeNonNegative_createsCorrectTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 10.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 2.0 }, Relationship.LEQ, 12.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(3, tableau.getHeight());
        assertEquals(6, tableau.getWidth());
        assertEquals(5, tableau.getRhsOffset());
        assertEquals(3, tableau.getSlackVariableOffset());
    }

    // Tests tableau construction with artificial variables (EQ and GEQ constraints)
    @Test
    public void testConstructor_withArtificialVariables_createsTwoPhaseTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 5.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());
        assertEquals(4, tableau.getHeight());
        assertEquals(2 + 1 + 2 + 2 + 1, tableau.getWidth());
        assertEquals(5, tableau.getArtificialVariableOffset());
    }

    // Tests tableau construction when variables are unrestricted in sign (restrictToNonNegative = false)
    @Test
    public void testConstructor_unrestrictedVariables_addsExtraDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);

        assertEquals(3, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
    }

    // Tests normalization of constraints with negative right hand side values
    @Test
    public void testGetNormalizedConstraints_negativeRhs_invertsCoefficientsAndRelationship() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, -2.0 }, Relationship.LEQ, -5.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 4.0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 2.0, -1.0 }, Relationship.EQ, -3.0));
        constraints.add(new LinearConstraint(new double[] { -1.0, 2.0 }, Relationship.GEQ, -4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(4, normalized.size());
        LinearConstraint c1 = normalized.get(0);
        assertEquals(5.0, c1.getValue(), EPSILON);
        assertEquals(Relationship.GEQ, c1.getRelationship());
        assertEquals(-1.0, c1.getCoefficients().getEntry(0), EPSILON);
        assertEquals(2.0, c1.getCoefficients().getEntry(1), EPSILON);

        LinearConstraint c2 = normalized.get(1);
        assertEquals(10.0, c2.getValue(), EPSILON);
        assertEquals(Relationship.GEQ, c2.getRelationship());

        LinearConstraint c3 = normalized.get(2);
        assertEquals(3.0, c3.getValue(), EPSILON);
        assertEquals(Relationship.EQ, c3.getRelationship());
        assertEquals(-2.0, c3.getCoefficients().getEntry(0), EPSILON);
        assertEquals(1.0, c3.getCoefficients().getEntry(1), EPSILON);

        LinearConstraint c4 = normalized.get(3);
        assertEquals(4.0, c4.getValue(), EPSILON);
        assertEquals(Relationship.LEQ, c4.getRelationship());
        assertEquals(1.0, c4.getCoefficients().getEntry(0), EPSILON);
        assertEquals(-2.0, c4.getCoefficients().getEntry(1), EPSILON);
    }

    // Tests discarding artificial variables after Phase 1
    @Test
    public void testDiscardArtificialVariables_phase1Complete_removesPhase1RowAndColumns() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);
        int initialHeight = tableau.getHeight();
        int initialWidth = tableau.getWidth();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(initialHeight - 1, tableau.getHeight());
        assertEquals(initialWidth - 1 - 1, tableau.getWidth());
    }

    // Tests discarding artificial variables when there are none
    @Test
    public void testDiscardArtificialVariables_noArtificialVariables_doesNothing() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int initialHeight = tableau.getHeight();
        int initialWidth = tableau.getWidth();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(initialHeight, tableau.getHeight());
        assertEquals(initialWidth, tableau.getWidth());
    }

    // Tests row operations: divideRow and subtractRow
    @Test
    public void testDivideAndSubtractRow_validInputs_modifiesTableauCorrectly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 3.0 }, Relationship.LEQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 1), EPSILON);
        assertEquals(2.0, tableau.getEntry(1, 2), EPSILON);
        assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), EPSILON);

        tableau.subtractRow(2, 1, 1.0);
        assertEquals(0.0, tableau.getEntry(2, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(2, 2), EPSILON);
        assertEquals(2.0, tableau.getEntry(2, tableau.getRhsOffset()), EPSILON);
    }

    // Tests getSolution when variables are basic and non-basic
    @Test
    public void testGetSolution_standardBasicVariables_returnsCorrectSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(2, point.length);
        assertEquals(4.0, point[0], EPSILON);
        assertEquals(6.0, point[1], EPSILON);
        assertEquals(42.0, solution.getValue(), EPSILON);
    }

    // Tests getSolution with unrestricted variables
    @Test
    public void testGetSolution_unrestrictedVariables_computesOffsetCorrectly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(2, point.length);
        assertEquals(2.0, point[0], EPSILON);
        assertEquals(3.0, point[1], EPSILON);
    }

    // Tests getBasicRow method for basic and non-basic columns
    @Test
    public void testGetBasicRow_basicAndNonBasicColumns_returnsExpectedRows() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        Integer slack1Row = tableau.getBasicRow(tableau.getSlackVariableOffset());
        assertEquals(Integer.valueOf(1), slack1Row);

        Integer slack2Row = tableau.getBasicRow(tableau.getSlackVariableOffset() + 1);
        assertEquals(Integer.valueOf(2), slack2Row);

        // Column with non-unit value or multiple non-zeros returns null
        tableau.setEntry(1, 1, 2.0);
        tableau.setEntry(2, 1, 1.0);
        assertNull(tableau.getBasicRow(1));

        tableau.setEntry(1, 1, 0.0);
        tableau.setEntry(2, 1, 0.5);
        assertNull(tableau.getBasicRow(1));
    }

    // Tests getSolution when two decision variables point to the same basic row (conflict resolution branch)
    @Test
    public void testGetSolution_duplicateBasicRow_setsZeroForDuplicateVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        tableau.setEntry(1, 1, 1.0);
        tableau.setEntry(1, 2, 1.0);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(5.0, point[0], EPSILON);
        assertEquals(0.0, point[1], EPSILON);
    }

    // Tests getInvertedCoeffiecientSum static utility method
    @Test
    public void testGetInvertedCoeffiecientSum_variousCoefficients_returnsNegativeSum() {
        RealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 4.0 });
        double sum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        assertEquals(-3.0, sum, EPSILON);

        RealVector zeroVector = new ArrayRealVector(new double[] { 0.0, 0.0 });
        assertEquals(0.0, SimplexTableau.getInvertedCoeffiecientSum(zeroVector), EPSILON);
    }

    // Tests setEntry and getEntry and getData
    @Test
    public void testSetEntryAndGetData_validValues_updatesAndReturnsTableauMatrix() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        tableau.setEntry(0, 0, 99.0);

        assertEquals(99.0, tableau.getEntry(0, 0), EPSILON);
        double[][] data = tableau.getData();
        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);
        assertEquals(99.0, data[0][0], EPSILON);
    }

    // Tests equals and hashCode consistency and branch coverage
    @Test
    public void testEqualsAndHashCode_sameAndDifferentInstances_behaveCorrectly() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        LinearObjectiveFunction f3 = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);

        List<LinearConstraint> constraints1 = new ArrayList<LinearConstraint>();
        constraints1.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        List<LinearConstraint> constraints2 = new ArrayList<LinearConstraint>();
        constraints2.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        List<LinearConstraint> constraints3 = new ArrayList<LinearConstraint>();
        constraints3.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau1 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tableau2 = new SimplexTableau(f2, constraints2, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tableau3 = new SimplexTableau(f3, constraints1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tableau4 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, EPSILON);
        SimplexTableau tableau5 = new SimplexTableau(f1, constraints1, GoalType.MINIMIZE, true, EPSILON);
        SimplexTableau tableau6 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, true, 1.0e-4);
        SimplexTableau tableau7 = new SimplexTableau(f1, constraints3, GoalType.MAXIMIZE, true, EPSILON);

        assertTrue(tableau1.equals(tableau1));
        assertTrue(tableau1.equals(tableau2));
        assertEquals(tableau1.hashCode(), tableau2.hashCode());

        assertFalse(tableau1.equals(null));
        assertFalse(tableau1.equals("NotATableau"));
        assertFalse(tableau1.equals(tableau3));
        assertFalse(tableau1.equals(tableau4));
        assertFalse(tableau1.equals(tableau5));
        assertFalse(tableau1.equals(tableau6));
        assertFalse(tableau1.equals(tableau7));

        SimplexTableau tableauModified = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, true, EPSILON);
        tableauModified.setEntry(0, 0, 123.45);
        assertFalse(tableau1.equals(tableauModified));
    }

    // Tests serialization and deserialization of SimplexTableau
    @Test
    public void testSerialization_roundTrip_preservesState() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 2.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, 2.0));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getNumDecisionVariables(), deserialized.getNumDecisionVariables());
        assertEquals(original.getNumSlackVariables(), deserialized.getNumSlackVariables());
        assertEquals(original.getNumArtificialVariables(), deserialized.getNumArtificialVariables());

        for (int i = 0; i < original.getHeight(); i++) {
            for (int j = 0; j < original.getWidth(); j++) {
                assertEquals(original.getEntry(i, j), deserialized.getEntry(i, j), EPSILON);
            }
        }
    }
}
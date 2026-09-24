package org.apache.commons.math.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimplexTableauTest {

    private static final double DEFAULT_EPSILON = 1.0e-6;

    // Tests tableau creation with MAXIMIZE and LEQ constraints (standard form)
    @Test
    public void testCreateTableau_maximizeLEQ_correctDimensionsAndValues() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(3, tableau.getHeight());
        assertEquals(6, tableau.getWidth()); // 1 obj + 2 vars + 2 slack + 1 RHS
        assertEquals(5, tableau.getRhsOffset());
    }

    // Tests tableau creation with MINIMIZE and negative constraint normalization
    @Test
    public void testCreateTableau_minimizeNegativeConstraint_normalizedProperly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 10.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // -x1 - x2 <= -4  ->  x1 + x2 >= 4
        constraints.add(new LinearConstraint(new double[] { -1.0, -1.0 }, Relationship.LEQ, -4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, DEFAULT_EPSILON);

        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();
        assertEquals(1, normalized.size());
        assertEquals(4.0, normalized.get(0).getValue(), DEFAULT_EPSILON);
        assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        assertEquals(1.0, normalized.get(0).getCoefficients().getEntry(0), DEFAULT_EPSILON);
        assertEquals(1.0, normalized.get(0).getCoefficients().getEntry(1), DEFAULT_EPSILON);
    }

    // Tests two-phase simplex tableau with GEQ and EQ constraints
    @Test
    public void testCreateTableau_withArtificialVariables_twoPhaseSetup() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.EQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(4, tableau.getSlackVariableOffset());
        assertEquals(5, tableau.getArtificialVariableOffset());
    }

    // Tests non-restricted variables (allows negative values with extra decision variable)
    @Test
    public void testCreateTableau_unrestrictedVariables_addsExtraDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, -2.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 7.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        assertEquals(3, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(4, tableau.getSlackVariableOffset());
    }

    // Tests helper getInvertedCoeffiecientSum
    @Test
    public void testGetInvertedCoeffiecientSum_validVector_returnsNegativeSum() {
        ArrayRealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 3.0 });
        double sum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        assertEquals(-2.0, sum, DEFAULT_EPSILON);
    }

    // Tests discardArtificialVariables removes phase 1 objective row and artificial columns
    @Test
    public void testDiscardArtificialVariables_removesArtificials() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.EQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        int originalHeight = tableau.getHeight();
        int originalWidth = tableau.getWidth();
        assertTrue(tableau.getNumArtificialVariables() > 0);

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(originalHeight - 1, tableau.getHeight());
        assertEquals(originalWidth - 1 - 1, tableau.getWidth()); // -1 artificial, -1 phase 1 obj col
    }

    // Tests discardArtificialVariables when there are no artificial variables
    @Test
    public void testDiscardArtificialVariables_noArtificials_doesNothing() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        int height = tableau.getHeight();
        int width = tableau.getWidth();
        tableau.discardArtificialVariables();

        assertEquals(height, tableau.getHeight());
        assertEquals(width, tableau.getWidth());
    }

    // Tests divideRow and subtractRow elementary row operations
    @Test
    public void testRowOperations_divideAndSubtract_modifiesTableauEntries() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 4.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Divide row 1 by 2.0
        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 1), DEFAULT_EPSILON);
        assertEquals(2.0, tableau.getEntry(1, 2), DEFAULT_EPSILON);
        assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), DEFAULT_EPSILON);

        // Set entry and subtract row
        tableau.setEntry(0, 1, 3.0);
        tableau.subtractRow(0, 1, 3.0);
        assertEquals(0.0, tableau.getEntry(0, 1), DEFAULT_EPSILON);
    }

    // Tests getSolution for multi-variable problem where basic variables should be resolved correctly
    @Test
    public void testGetSolution_multipleDecisionVariables_returnsCorrectSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0, 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0, 0.0, 0.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 1.0, 0.0 }, Relationship.LEQ, 3.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 1.0 }, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Set decision variable columns to basic form
        tableau.setEntry(1, 1, 1.0);
        tableau.setEntry(2, 2, 1.0);
        tableau.setEntry(3, 3, 1.0);
        tableau.setEntry(4, 4, 1.0);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        double[] point = solution.getPoint();
        assertEquals(4, point.length);
        assertEquals(1.0, point[0], DEFAULT_EPSILON);
        assertEquals(2.0, point[1], DEFAULT_EPSILON);
        assertEquals(3.0, point[2], DEFAULT_EPSILON);
        assertEquals(4.0, point[3], DEFAULT_EPSILON);
        assertEquals(10.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests getSolution with non-restricted variables (unbounded below)
    @Test
    public void testGetSolution_unrestrictedVariables_resolvesWithMostNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(1, solution.getPoint().length);
    }

    // Tests getData returning full 2D matrix
    @Test
    public void testGetData_returnsCorrectMatrixDimensions() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        double[][] data = tableau.getData();

        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);
    }

    // Tests equals and hashCode consistency and branch coverage
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_behaveCorrectly() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 3.0 }, 0.0);
        Collection<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));
        Collection<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau t1Clone = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau t2 = new SimplexTableau(f2, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau t3 = new SimplexTableau(f1, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau t4 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        // Reflexive
        assertTrue(t1.equals(t1));
        // Symmetric
        assertTrue(t1.equals(t1Clone));
        assertTrue(t1Clone.equals(t1));
        assertEquals(t1.hashCode(), t1Clone.hashCode());

        // Null and different type
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("Some String"));

        // Differences
        assertFalse(t1.equals(t2));
        assertFalse(t1.equals(t3));
        assertFalse(t1.equals(t4));
    }

    // Tests serialization and deserialization of SimplexTableau
    @Test
    public void testSerialization_roundTrip_restoresEqualTableau() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 3.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 2.0));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getEntry(0, 0), deserialized.getEntry(0, 0), DEFAULT_EPSILON);
    }
}
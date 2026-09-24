package org.apache.commons.math.optimization.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimplexSolverTest {

    // Tests default constructor and basic maximization problem
    @Test
    public void testOptimize_maximizeStandardProblem_returnsOptimalSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(2.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(6.0, solution.getPoint()[1], 1.0e-6);
        assertEquals(36.0, solution.getValue(), 1.0e-6);
    }

    // Tests custom epsilon constructor and minimization problem
    @Test
    public void testOptimize_minimizeStandardProblem_returnsOptimalSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 3));

        SimplexSolver solver = new SimplexSolver(1.0e-8);
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertEquals(3.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(0.0, solution.getPoint()[1], 1.0e-6);
        assertEquals(-6.0, solution.getValue(), 1.0e-6);
    }

    // Tests defect where zero entry in pivot column should not be selected in minimum ratio test
    @Test
    public void testOptimize_pivotColumnHasZeroEntry_selectsCorrectPivotRow() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 5 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 2, 2 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(1.0 / 3.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(1.0 / 6.0, solution.getPoint()[1], 1.0e-6);
        assertEquals(17.0 / 6.0, solution.getValue(), 1.0e-6);
    }

    // Tests Phase 1 with artificial variables (equality and greater-than-or-equal constraints)
    @Test
    public void testOptimize_phase1WithEqualityAndGeqConstraints_solvesCorrectly() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.GEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 3));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertEquals(3.0, solution.getValue(), 1.0e-6);
    }

    // Tests unbounded problem throwing UnboundedSolutionException
    @Test(expected = UnboundedSolutionException.class)
    public void testOptimize_unboundedProblem_throwsUnboundedSolutionException() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // Tests infeasible problem throwing NoFeasibleSolutionException
    @Test(expected = NoFeasibleSolutionException.class)
    public void testOptimize_infeasibleProblem_throwsNoFeasibleSolutionException() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // Tests unrestricted variables (negative values allowed)
    @Test
    public void testOptimize_unrestrictedToNonNegative_allowsNegativeValues() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, -3));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 5));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        assertEquals(-3.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(5.0, solution.getPoint()[1], 1.0e-6);
        assertEquals(2.0, solution.getValue(), 1.0e-6);
    }

    // Tests problem with constant term in objective function
    @Test
    public void testOptimize_withConstantOffsetInObjective_returnsCorrectValue() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 10);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(0.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(4.0, solution.getPoint()[1], 1.0e-6);
        assertEquals(22.0, solution.getValue(), 1.0e-6);
    }

    // Tests single variable linear program
    @Test
    public void testOptimize_singleVariable_returnsCorrectResult() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 5 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(10.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(50.0, solution.getValue(), 1.0e-6);
    }

    // Tests multiple iterations in Phase 2
    @Test
    public void testOptimize_multipleIterationsRequired_solvesSuccessfully() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10, -57, -9, -24 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0.5, -5.5, -2.5, 9 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0.5, -1.5, -0.5, 1 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(1.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(1.0, solution.getValue(), 1.0e-6);
    }
}
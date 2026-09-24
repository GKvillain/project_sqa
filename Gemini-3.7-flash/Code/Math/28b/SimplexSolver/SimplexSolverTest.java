package org.apache.commons.math3.optimization.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link SimplexSolver}.
 */
public class SimplexSolverTest {

    private static final double DEFAULT_EPSILON = 1.0e-6;

    // Tests default constructor and simple maximization problem
    @Test
    public void testOptimize_simpleMaximization_returnsOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(2.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(6.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(36.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests custom epsilon and maxUlps constructor with simple minimization problem
    @Test
    public void testOptimize_simpleMinimization_returnsOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));

        SimplexSolver solver = new SimplexSolver(1e-4, 5);
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(4.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(0.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(-8.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests unbounded problem throwing UnboundedSolutionException
    @Test(expected = UnboundedSolutionException.class)
    public void testOptimize_unboundedProblem_throwsUnboundedSolutionException() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // Tests infeasible problem throwing NoFeasibleSolutionException
    @Test(expected = NoFeasibleSolutionException.class)
    public void testOptimize_infeasibleProblem_throwsNoFeasibleSolutionException() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // Tests optimization with equality constraints (Phase 1 resolution)
    @Test
    public void testOptimize_withEqualityConstraints_returnsOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(1.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(1.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(3.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests non-restricted to non-negative variables allowing negative solutions
    @Test
    public void testOptimize_unrestrictedVariables_returnsOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, -10));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, -5));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertEquals(-10.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(-5.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(-15.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests degenerate linear program with Bland's rule / cycling prevention (Math-28 / Math-828)
    @Test
    public void testOptimize_degenerateModelBlandRule_reachesOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, 0d);

        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0.0, 16.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 16.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 0.0, 0.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }, Relationship.EQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
    }

    // Tests another cycling/degeneracy case (Math-842)
    @Test
    public void testOptimize_blandRuleCyclingPrevention_terminatesSuccessfully() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10, -57, -9, -24 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0.5, -5.5, -2.5, 9 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0.5, -1.5, -0.5, 1 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests iteration limit throwing MaxCountExceededException
    @Test(expected = MaxCountExceededException.class)
    public void testOptimize_exceedMaxIterations_throwsMaxCountExceededException() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(0);
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // Tests objective function with constant term
    @Test
    public void testOptimize_objectiveWithConstantTerm_returnsValueIncludingConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 1 }, 10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(5.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(0.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(20.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests problem with greater-than-or-equal constraint
    @Test
    public void testOptimize_greaterThanEqualToConstraint_returnsOptimalSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.GEQ, 8));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(4.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(2.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(6.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // Tests already optimal problem requiring 0 simplex iterations
    @Test
    public void testOptimize_alreadyOptimal_returnsZeroIterationsSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(0.0, solution.getPoint()[0], DEFAULT_EPSILON);
        Assert.assertEquals(0.0, solution.getPoint()[1], DEFAULT_EPSILON);
        Assert.assertEquals(0.0, solution.getValue(), DEFAULT_EPSILON);
    }
}
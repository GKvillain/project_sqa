package org.apache.commons.math3.ode;

import java.util.Collection;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractIntegratorTest {

    private TestIntegrator integrator;

    @Before
    public void setUp() {
        integrator = new TestIntegrator("testIntegrator");
    }

    // Tests getName when initialized with a specific name
    @Test
    public void testGetName_customName_returnsName() {
        assertEquals("testIntegrator", integrator.getName());
    }

    // Tests getName when initialized with default constructor
    @Test
    public void testGetName_defaultConstructor_returnsNull() {
        TestIntegrator defaultIntegrator = new TestIntegrator();
        assertNull(defaultIntegrator.getName());
    }

    // Tests setMaxEvaluations with positive limit and getEvaluations tracking
    @Test
    public void testSetMaxEvaluations_positiveValue_setsLimit() {
        integrator.setMaxEvaluations(100);
        assertEquals(100, integrator.getMaxEvaluations());
        assertEquals(0, integrator.getEvaluations());
    }

    // Tests setMaxEvaluations with negative limit, which should default to Integer.MAX_VALUE
    @Test
    public void testSetMaxEvaluations_negativeValue_setsToIntegerMax() {
        integrator.setMaxEvaluations(-5);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());
    }

    // Tests adding, retrieving, and clearing step handlers
    @Test
    public void testStepHandlers_addGetClear_manipulatesCollection() {
        StepHandler handler1 = new DummyStepHandler();
        StepHandler handler2 = new DummyStepHandler();

        integrator.addStepHandler(handler1);
        integrator.addStepHandler(handler2);

        Collection<StepHandler> handlers = integrator.getStepHandlers();
        assertEquals(2, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertTrue(handlers.contains(handler2));

        integrator.clearStepHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
    }

    // Tests adding event handlers and clearing them
    @Test
    public void testEventHandlers_addGetClear_manipulatesCollection() {
        EventHandler eventHandler = new DummyEventHandler();
        integrator.addEventHandler(eventHandler, 1.0, 1e-6, 100);

        Collection<EventHandler> handlers = integrator.getEventHandlers();
        assertEquals(1, handlers.size());
        assertTrue(handlers.contains(eventHandler));

        integrator.clearEventHandlers();
        assertEquals(0, integrator.getEventHandlers().size());
    }

    // Tests adding event handler with custom solver
    @Test
    public void testAddEventHandler_withCustomSolver_addsSuccessfully() {
        EventHandler eventHandler = new DummyEventHandler();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 5);
        integrator.addEventHandler(eventHandler, 0.5, 1e-6, 50, solver);

        Collection<EventHandler> handlers = integrator.getEventHandlers();
        assertEquals(1, handlers.size());
        assertTrue(handlers.contains(eventHandler));
    }

    // Tests getCurrentStepStart and getCurrentSignedStepsize initial states
    @Test
    public void testGetCurrentStepValues_initialState_returnsNaN() {
        assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
        assertTrue(Double.isNaN(integrator.getCurrentSignedStepsize()));
    }

    // Tests integrate throws DimensionMismatchException when y0 length does not match equations dimension
    @Test(expected = DimensionMismatchException.class)
    public void testIntegrate_y0DimensionMismatch_throwsException() {
        FirstOrderDifferentialEquations ode = new DummyODE(2);
        integrator.integrate(ode, 0.0, new double[1], 1.0, new double[2]);
    }

    // Tests integrate throws DimensionMismatchException when y length does not match equations dimension
    @Test(expected = DimensionMismatchException.class)
    public void testIntegrate_yDimensionMismatch_throwsException() {
        FirstOrderDifferentialEquations ode = new DummyODE(2);
        integrator.integrate(ode, 0.0, new double[2], 1.0, new double[1]);
    }

    // Tests sanityChecks throws NumberIsTooSmallException when interval dt <= threshold
    @Test(expected = NumberIsTooSmallException.class)
    public void testSanityChecks_intervalTooSmall_throwsException() {
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE(1));
        ode.setTime(1.0);
        integrator.testSanityChecks(ode, 1.0);
    }

    // Tests normal execution of sanityChecks with valid time interval
    @Test
    public void testSanityChecks_validInterval_passesWithoutException() {
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE(1));
        ode.setTime(0.0);
        integrator.testSanityChecks(ode, 1.0);
    }

    // Tests computeDerivatives increments evaluation count and computes derivative
    @Test
    public void testComputeDerivatives_normalCase_incrementsEvaluations() {
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE(1));
        ode.setTime(0.0);
        ode.setPrimaryState(new double[]{2.0});
        integrator.testSetEquations(ode);

        double[] y = new double[]{2.0};
        double[] yDot = new double[1];

        integrator.computeDerivatives(0.0, y, yDot);
        assertEquals(1, integrator.getEvaluations());
        assertEquals(1.0, yDot[0], 1e-10);
    }

    // Tests computeDerivatives throws MaxCountExceededException when evaluation limit is exceeded
    @Test(expected = MaxCountExceededException.class)
    public void testComputeDerivatives_maxEvaluationsExceeded_throwsException() {
        integrator.setMaxEvaluations(1);
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE(1));
        ode.setTime(0.0);
        ode.setPrimaryState(new double[]{1.0});
        integrator.testSetEquations(ode);

        double[] y = new double[]{1.0};
        double[] yDot = new double[1];
        integrator.computeDerivatives(0.0, y, yDot);
        integrator.computeDerivatives(0.0, y, yDot);
    }

    // Tests initIntegration initializes step handlers and event handlers and resets evaluation counter
    @Test
    public void testInitIntegration_resetsEvaluationsAndInitializesHandlers() {
        DummyStepHandler stepHandler = new DummyStepHandler();
        DummyEventHandler eventHandler = new DummyEventHandler();

        integrator.addStepHandler(stepHandler);
        integrator.addEventHandler(eventHandler, 1.0, 1e-6, 100);

        integrator.testInitIntegration(0.0, new double[]{1.0}, 10.0);

        assertTrue(stepHandler.initCalled);
        assertTrue(eventHandler.initCalled);
        assertEquals(0, integrator.getEvaluations());
    }

    // Tests integration workflow end-to-end with DummyIntegrator implementation
    @Test
    public void testIntegrate_validODE_completesIntegration() {
        FirstOrderDifferentialEquations ode = new DummyODE(1);
        double[] y0 = new double[]{0.0};
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1e-10);
        assertEquals(1.0, y[0], 1e-10);
    }

    // Concrete dummy implementation of AbstractIntegrator for testing
    private static class TestIntegrator extends AbstractIntegrator {

        public TestIntegrator() {
            super();
        }

        public TestIntegrator(String name) {
            super(name);
        }

        public void testSetEquations(ExpandableStatefulODE equations) {
            setEquations(equations);
        }

        public void testInitIntegration(double t0, double[] y0, double t) {
            initIntegration(t0, y0, t);
        }

        public void testSanityChecks(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
            setEquations(equations);
            initIntegration(equations.getTime(), equations.getPrimaryState(), t);

            double t0 = equations.getTime();
            double[] y = equations.getPrimaryState();
            double[] yDot = new double[y.length];

            computeDerivatives(t0, y, yDot);
            y[0] += yDot[0] * (t - t0);
            equations.setTime(t);
            equations.setPrimaryState(y);
        }
    }

    // Dummy FirstOrderDifferentialEquations for test cases
    private static class DummyODE implements FirstOrderDifferentialEquations {
        private final int dimension;

        public DummyODE(int dimension) {
            this.dimension = dimension;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            for (int i = 0; i < dimension; i++) {
                yDot[i] = 1.0;
            }
        }
    }

    // Dummy StepHandler for testing initialization and calls
    private static class DummyStepHandler implements StepHandler {
        boolean initCalled = false;

        public void init(double t0, double[] y0, double t) {
            initCalled = true;
        }

        public void handleStep(StepInterpolator interpolator, boolean isLast) {
        }
    }

    // Dummy EventHandler for testing initialization and event handling
    private static class DummyEventHandler implements EventHandler {
        boolean initCalled = false;

        public void init(double t0, double[] y0, double t) {
            initCalled = true;
        }

        public double g(double t, double[] y) {
            return 0.0;
        }

        public Action eventOccurred(double t, double[] y, boolean increasing) {
            return Action.CONTINUE;
        }

        public void resetState(double t, double[] y) {
        }
    }
}
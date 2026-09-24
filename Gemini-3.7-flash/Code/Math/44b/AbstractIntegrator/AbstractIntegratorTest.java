package org.apache.commons.math.ode;

import java.util.Collection;

import org.apache.commons.math.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractIntegratorTest {

    private DummyIntegrator integrator;

    @Before
    public void setUp() {
        integrator = new DummyIntegrator("testIntegrator");
    }

    // Tests constructor with name and default constructor
    @Test
    public void testConstructor_withNameAndDefault_setsCorrectName() {
        assertEquals("testIntegrator", integrator.getName());
        DummyIntegrator defaultIntegrator = new DummyIntegrator();
        assertNull(defaultIntegrator.getName());
    }

    // Tests step handlers addition, retrieval, immutability and clearing
    @Test
    public void testStepHandlers_addGetClear_managesHandlersCorrectly() {
        StepHandler handler1 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        StepHandler handler2 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };

        integrator.addStepHandler(handler1);
        integrator.addStepHandler(handler2);
        Collection<StepHandler> handlers = integrator.getStepHandlers();
        assertEquals(2, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertTrue(handlers.contains(handler2));

        try {
            handlers.clear();
            fail("Collection should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        integrator.clearStepHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
    }

    // Tests event handlers addition, retrieval, custom solver, and clearing
    @Test
    public void testEventHandlers_addGetClear_managesHandlersCorrectly() {
        EventHandler handler1 = new DummyEventHandler();
        EventHandler handler2 = new DummyEventHandler();

        integrator.addEventHandler(handler1, 1.0, 1e-6, 100);
        integrator.addEventHandler(handler2, 0.5, 1e-4, 50, new BracketingNthOrderBrentSolver(1e-4, 5));

        Collection<EventHandler> handlers = integrator.getEventHandlers();
        assertEquals(2, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertTrue(handlers.contains(handler2));

        integrator.clearEventHandlers();
        assertEquals(0, integrator.getEventHandlers().size());
    }

    // Tests default step start and signed step size
    @Test
    public void testStepProperties_initialValues_areNaN() {
        assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
        assertTrue(Double.isNaN(integrator.getCurrentSignedStepsize()));
    }

    // Tests max evaluations configuration and resetting
    @Test
    public void testMaxEvaluations_setAndReset_updatesCountCorrectly() {
        integrator.setMaxEvaluations(100);
        assertEquals(100, integrator.getMaxEvaluations());
        assertEquals(0, integrator.getEvaluations());

        integrator.setMaxEvaluations(-1);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());
    }

    // Tests computeDerivatives increments evaluation count
    @Test
    public void testComputeDerivatives_validCall_incrementsEvaluations() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0 * y[0];
            }
        };

        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 1.0 });
        integrator.setEquations(expandable);

        double[] y = new double[] { 3.0 };
        double[] yDot = new double[1];

        integrator.computeDerivatives(0.0, y, yDot);
        assertEquals(1, integrator.getEvaluations());
        assertEquals(6.0, yDot[0], 1e-10);

        integrator.resetEvaluations();
        assertEquals(0, integrator.getEvaluations());
    }

    // Tests computeDerivatives exceeding max evaluations throws MaxCountExceededException
    @Test(expected = MaxCountExceededException.class)
    public void testComputeDerivatives_exceedingMaxEvaluations_throwsException() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.setEquations(expandable);

        integrator.setMaxEvaluations(1);
        double[] y = new double[] { 0.0 };
        double[] yDot = new double[1];

        integrator.computeDerivatives(0.0, y, yDot);
        integrator.computeDerivatives(1.0, y, yDot);
    }

    // Tests integrate with mismatched y0 dimension
    @Test(expected = DimensionMismatchException.class)
    public void testIntegrate_y0DimensionMismatch_throwsException() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[] { 1.0 }, 1.0, new double[] { 1.0, 2.0 });
    }

    // Tests integrate with mismatched y dimension
    @Test(expected = DimensionMismatchException.class)
    public void testIntegrate_yDimensionMismatch_throwsException() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[] { 1.0, 2.0 }, 1.0, new double[] { 1.0 });
    }

    // Tests sanityChecks when integration interval is too small
    @Test(expected = NumberIsTooSmallException.class)
    public void testSanityChecks_intervalTooSmall_throwsException() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(1.0);
        integrator.callSanityChecks(expandable, 1.0);
    }

    // Tests sanityChecks when integration interval is valid
    @Test
    public void testSanityChecks_validInterval_doesNotThrow() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        integrator.callSanityChecks(expandable, 1.0);
    }

    // Tests acceptStep without events
    @Test
    public void testAcceptStep_noEvents_advancesToCurrentTime() {
        DummyInterpolator interpolator = new DummyInterpolator(0.0, 1.0, new double[] { 0.0 }, new double[] { 1.0 });
        double[] y = new double[1];
        double[] yDot = new double[1];

        final boolean[] handlerCalled = new boolean[1];
        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                handlerCalled[0] = true;
            }
        });

        integrator.setStateInitialized(false);
        double endT = integrator.callAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(1.0, endT, 1e-10);
        assertTrue(handlerCalled[0]);
    }

    // Tests acceptStep with event triggering a stop
    @Test
    public void testAcceptStep_eventStopsIntegration_stopsAtEventTime() {
        DummyInterpolator interpolator = new DummyInterpolator(0.0, 1.0, new double[] { 0.0 }, new double[] { 1.0 });
        double[] y = new double[1];
        double[] yDot = new double[1];

        EventHandler eventHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.5;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(eventHandler, 0.1, 1e-6, 100);
        integrator.setStateInitialized(false);
        double endT = integrator.callAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(0.5, endT, 1e-5);
        assertEquals(0.5, y[0], 1e-5);
    }

    // Tests acceptStep with event triggering state reset
    @Test
    public void testAcceptStep_eventResetsState_updatesStateAndDerivatives() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0;
            }
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.setEquations(expandable);

        DummyInterpolator interpolator = new DummyInterpolator(0.0, 1.0, new double[] { 0.0 }, new double[] { 1.0 });
        double[] y = new double[1];
        double[] yDot = new double[1];

        EventHandler eventHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.5;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            public void resetState(double t, double[] y) {
                y[0] = 10.0;
            }
        };

        integrator.addEventHandler(eventHandler, 0.1, 1e-6, 100);
        integrator.setStateInitialized(false);
        double endT = integrator.callAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(0.5, endT, 1e-5);
        assertEquals(10.0, y[0], 1e-5);
        assertEquals(2.0, yDot[0], 1e-5);
    }

    // Tests integrate method successfully performing integration workflow
    @Test
    public void testIntegrate_validODE_completesAndCopiesState() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(2.0, stopTime, 1e-10);
        assertEquals(2.0, y[0], 1e-10);
    }

    // Concrete test integrator subclass
    private static class DummyIntegrator extends AbstractIntegrator {
        public DummyIntegrator(String name) {
            super(name);
        }

        public DummyIntegrator() {
            super();
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            setEquations(equations);
            sanityChecks(equations, t);
            equations.setTime(t);
            double[] state = equations.getPrimaryState();
            state[0] += (t - 0.0);
            equations.setPrimaryState(state);
        }

        public void callSanityChecks(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
        }

        public double callAcceptStep(AbstractStepInterpolator interpolator, double[] y, double[] yDot, double tEnd) {
            return acceptStep(interpolator, y, yDot, tEnd);
        }

        @Override
        public void setEquations(ExpandableStatefulODE equations) {
            super.setEquations(equations);
        }

        @Override
        public void setStateInitialized(boolean stateInitialized) {
            super.setStateInitialized(stateInitialized);
        }
    }

    // Concrete step interpolator for testing
    private static class DummyInterpolator extends AbstractStepInterpolator {
        private final double[] y0;
        private final double[] y1;

        public DummyInterpolator(double t0, double t1, double[] y0, double[] y1) {
            super();
            this.previousTime = t0;
            this.currentTime = t1;
            this.h = t1 - t0;
            this.interpolatedTime = t0;
            this.interpolatedState = new double[y0.length];
            this.interpolatedDerivatives = new double[y0.length];
            this.y0 = y0.clone();
            this.y1 = y1.clone();
            System.arraycopy(y0, 0, this.interpolatedState, 0, y0.length);
            setSoftPreviousTime(t0);
            setSoftCurrentTime(t1);
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            for (int i = 0; i < interpolatedState.length; i++) {
                interpolatedState[i] = (1.0 - theta) * y0[i] + theta * y1[i];
                interpolatedDerivatives[i] = (y1[i] - y0[i]) / (currentTime - previousTime);
            }
        }
    }

    // Dummy event handler for basic checks
    private static class DummyEventHandler implements EventHandler {
        public void init(double t0, double[] y0, double t) {}
        public double g(double t, double[] y) {
            return 0.0;
        }
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            return Action.CONTINUE;
        }
        public void resetState(double t, double[] y) {}
    }
}
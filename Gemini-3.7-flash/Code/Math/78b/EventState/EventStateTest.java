package org.apache.commons.math.ode.events;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventStateTest {

    private static class DummyInterpolator implements StepInterpolator {
        private double previousTime;
        private double currentTime;
        private double interpolatedTime;
        private double[] state;
        private boolean forward;

        public DummyInterpolator(double previousTime, double currentTime, double[] state, boolean forward) {
            this.previousTime = previousTime;
            this.currentTime = currentTime;
            this.interpolatedTime = previousTime;
            this.state = state;
            this.forward = forward;
        }

        public double getPreviousTime() {
            return previousTime;
        }

        public double getCurrentTime() {
            return currentTime;
        }

        public double getInterpolatedTime() {
            return interpolatedTime;
        }

        public void setInterpolatedTime(double time) {
            this.interpolatedTime = time;
        }

        public double[] getInterpolatedState() {
            return state;
        }

        public double[] getInterpolatedDerivatives() {
            return new double[state.length];
        }

        public boolean isForward() {
            return forward;
        }

        public StepInterpolator copy() {
            return this;
        }

        public void writeExternal(ObjectOutput out) {}

        public void readExternal(ObjectInput in) {}
    }

    private static class SimpleHandler implements EventHandler {
        private double eventTime;
        private int action;
        private boolean resetCalled = false;

        public SimpleHandler(double eventTime, int action) {
            this.eventTime = eventTime;
            this.action = action;
        }

        public double g(double t, double[] y) {
            return t - eventTime;
        }

        public int eventOccurred(double t, double[] y, boolean increasing) {
            return action;
        }

        public void resetState(double t, double[] y) {
            resetCalled = true;
            y[0] = 0.0;
        }
    }

    // Tests constructor initialization and getters
    @Test
    public void testGetters_validParameters_returnExpectedValues() {
        EventHandler handler = new SimpleHandler(0.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        assertSame(handler, es.getEventHandler());
        assertEquals(10.0, es.getMaxCheckInterval(), 1e-15);
        assertEquals(1e-6, es.getConvergence(), 1e-15);
        assertEquals(100, es.getMaxIterationCount());
        assertTrue(Double.isNaN(es.getEventTime()));
        assertFalse(es.stop());
    }

    // Tests reinitializeBegin initializes state correctly
    @Test
    public void testReinitializeBegin_standardInput_initializesCorrectly() throws EventException {
        EventHandler handler = new SimpleHandler(2.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 1.0 });
        assertTrue(Double.isNaN(es.getEventTime()));
        assertFalse(es.stop());
    }

    // Tests evaluateStep when no event occurs during the step
    @Test
    public void testEvaluateStep_noEventInStep_returnsFalse() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(5.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 2.0, new double[] { 0.0 }, true);

        boolean eventFound = es.evaluateStep(interpolator);
        assertFalse(eventFound);
        assertTrue(Double.isNaN(es.getEventTime()));
    }

    // Tests evaluateStep when an event occurs in forward integration
    @Test
    public void testEvaluateStep_forwardEventOccurs_returnsTrueAndSetsEventTime() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(2.5, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 5.0, new double[] { 0.0 }, true);

        boolean eventFound = es.evaluateStep(interpolator);
        assertTrue(eventFound);
        assertEquals(2.5, es.getEventTime(), 1e-5);
    }

    // Tests evaluateStep when an event occurs in backward integration
    @Test
    public void testEvaluateStep_backwardEventOccurs_returnsTrueAndSetsEventTime() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(2.5, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(5.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(5.0, 0.0, new double[] { 0.0 }, false);

        boolean eventFound = es.evaluateStep(interpolator);
        assertTrue(eventFound);
        assertEquals(2.5, es.getEventTime(), 1e-5);
    }

    // Tests evaluateStep with multiple substeps based on maxCheckInterval
    @Test
    public void testEvaluateStep_multipleSubsteps_findsEventCorrectly() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(3.2, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 1.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 5.0, new double[] { 0.0 }, true);

        boolean eventFound = es.evaluateStep(interpolator);
        assertTrue(eventFound);
        assertEquals(3.2, es.getEventTime(), 1e-5);
    }

    // Tests evaluateStep when step ends exactly at the pending event time
    @Test
    public void testEvaluateStep_stepEndsAtPendingEvent_returnsFalse() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(2.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator1 = new DummyInterpolator(0.0, 4.0, new double[] { 0.0 }, true);
        boolean found1 = es.evaluateStep(interpolator1);
        assertTrue(found1);
        assertEquals(2.0, es.getEventTime(), 1e-5);

        StepInterpolator interpolator2 = new DummyInterpolator(0.0, 2.0, new double[] { 0.0 }, true);
        boolean found2 = es.evaluateStep(interpolator2);
        assertFalse(found2);
    }

    // Tests stepAccepted with EventHandler.STOP action
    @Test
    public void testStepAccepted_stopAction_stopReturnsTrue() throws DerivativeException, EventException, ConvergenceException {
        SimpleHandler handler = new SimpleHandler(2.0, EventHandler.STOP);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 4.0, new double[] { 0.0 }, true);
        es.evaluateStep(interpolator);

        double[] y = new double[] { 0.0 };
        es.stepAccepted(2.0, y);
        assertTrue(es.stop());
    }

    // Tests stepAccepted without pending event sets CONTINUE action
    @Test
    public void testStepAccepted_noPendingEvent_continuesIntegration() throws EventException {
        SimpleHandler handler = new SimpleHandler(5.0, EventHandler.STOP);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        double[] y = new double[] { 0.0 };
        es.stepAccepted(1.0, y);
        assertFalse(es.stop());
    }

    // Tests reset method when RESET_STATE is returned by eventOccurred
    @Test
    public void testReset_resetStateAction_invokesHandlerResetStateAndReturnsTrue() throws DerivativeException, EventException, ConvergenceException {
        SimpleHandler handler = new SimpleHandler(2.0, EventHandler.RESET_STATE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 4.0, new double[] { 0.0 }, true);
        es.evaluateStep(interpolator);

        double[] y = new double[] { 5.0 };
        es.stepAccepted(2.0, y);

        boolean resetDerivatives = es.reset(2.0, y);
        assertTrue(resetDerivatives);
        assertTrue(handler.resetCalled);
        assertEquals(0.0, y[0], 1e-15);
        assertTrue(Double.isNaN(es.getEventTime()));
    }

    // Tests reset method when RESET_DERIVATIVES is returned by eventOccurred
    @Test
    public void testReset_resetDerivativesAction_returnsTrueWithoutResetState() throws DerivativeException, EventException, ConvergenceException {
        SimpleHandler handler = new SimpleHandler(2.0, EventHandler.RESET_DERIVATIVES);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 4.0, new double[] { 0.0 }, true);
        es.evaluateStep(interpolator);

        double[] y = new double[] { 5.0 };
        es.stepAccepted(2.0, y);

        boolean resetDerivatives = es.reset(2.0, y);
        assertTrue(resetDerivatives);
        assertFalse(handler.resetCalled);
    }

    // Tests reset method when there is no pending event
    @Test
    public void testReset_noPendingEvent_returnsFalse() throws EventException {
        EventHandler handler = new SimpleHandler(5.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        double[] y = new double[] { 1.0 };
        boolean result = es.reset(1.0, y);
        assertFalse(result);
    }

    // Tests evaluateStep exception unwrapping when handler throws EventException
    @Test(expected = EventException.class)
    public void testEvaluateStep_handlerThrowsEventException_throwsEventException() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) throws EventException {
                if (t > 1.0) {
                    throw new EventException("Handler error", new Object[0]);
                }
                return t;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator = new DummyInterpolator(0.0, 2.0, new double[] { 0.0 }, true);
        es.evaluateStep(interpolator);
    }

    // Tests evaluateStep exception unwrapping when handler throws DerivativeException
    @Test(expected = DerivativeException.class)
    public void testEvaluateStep_handlerThrowsDerivativeException_throwsDerivativeException() throws DerivativeException, EventException, ConvergenceException {
        StepInterpolator interpolator = new StepInterpolator() {
            public double getPreviousTime() { return 0.0; }
            public double getCurrentTime() { return 2.0; }
            public double getInterpolatedTime() { return 0.0; }
            public void setInterpolatedTime(double time) {}
            public double[] getInterpolatedState() throws DerivativeException {
                throw new DerivativeException("Derivative error", new Object[0]);
            }
            public double[] getInterpolatedDerivatives() { return new double[0]; }
            public boolean isForward() { return true; }
            public StepInterpolator copy() { return this; }
            public void writeExternal(ObjectOutput out) {}
            public void readExternal(ObjectInput in) {}
        };

        EventHandler handler = new SimpleHandler(1.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        es.reinitializeBegin(0.0, new double[] { 0.0 });
        es.evaluateStep(interpolator);
    }

    // Tests close consecutive events handling in evaluateStep
    @Test
    public void testEvaluateStep_pastEventIgnored_returnsExpectedResult() throws DerivativeException, EventException, ConvergenceException {
        EventHandler handler = new SimpleHandler(1.0, EventHandler.CONTINUE);
        EventState es = new EventState(handler, 10.0, 1e-6, 100);

        es.reinitializeBegin(0.0, new double[] { 0.0 });
        StepInterpolator interpolator1 = new DummyInterpolator(0.0, 1.0, new double[] { 0.0 }, true);
        es.evaluateStep(interpolator1);
        es.stepAccepted(1.0, new double[] { 0.0 });

        StepInterpolator interpolator2 = new DummyInterpolator(1.0, 3.0, new double[] { 0.0 }, true);
        boolean eventFound = es.evaluateStep(interpolator2);
        assertFalse(eventFound);
    }
}
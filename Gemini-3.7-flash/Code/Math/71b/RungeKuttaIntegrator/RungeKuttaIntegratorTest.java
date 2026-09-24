package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RungeKuttaIntegratorTest {

    // Tests standard forward integration with EulerIntegrator
    @Test
    public void testIntegrate_forwardIntegration_reachesFinalTime() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
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
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-10);
        assertEquals(1.0, y[0], 1e-10);
    }

    // Tests backward integration (t < t0)
    @Test
    public void testIntegrate_backwardIntegration_reachesFinalTime() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -1.0;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 1.0, y0, 0.0, y);
        assertEquals(0.0, stopTime, 1e-10);
        assertEquals(1.0, y[0], 1e-10);
    }

    // Tests integration when initial state array and target state array are the same instance
    @Test
    public void testIntegrate_sameInputOutputArray_success() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.05);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[1];
                yDot[1] = -y[0];
            }
        };
        double[] y = new double[] { 0.0, 1.0 };
        double stopTime = integrator.integrate(ode, 0.0, y, Math.PI, y);
        assertEquals(Math.PI, stopTime, 1e-10);
        assertEquals(0.0, y[0], 1e-3);
        assertEquals(-1.0, y[1], 1e-3);
    }

    // Tests exception when equations dimension does not match initial state dimension
    @Test(expected = IntegratorException.class)
    public void testIntegrate_dimensionMismatch_throwsIntegratorException() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 0;
                yDot[1] = 0;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    // Tests exception when t0 equals t (almost zero time step check in sanityChecks)
    @Test(expected = IntegratorException.class)
    public void testIntegrate_zeroIntegrationInterval_throwsIntegratorException() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
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
        integrator.integrate(ode, 1.0, y0, 1.0, y);
    }

    // Tests integration with StepHandler requiring dense output
    @Test
    public void testIntegrate_withDenseStepHandler_handlesIntermediateSteps() throws DerivativeException, IntegratorException {
        MidpointIntegrator integrator = new MidpointIntegrator(0.1);
        final int[] stepCount = new int[] { 0 };
        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return true;
            }
            public void reset() {
                stepCount[0] = 0;
            }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
                double t = interpolator.getCurrentTime();
                assertTrue(t >= 0.0 && t <= 1.0);
            }
        });
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-10);
        assertTrue(stepCount[0] >= 10);
    }

    // Tests integration with discrete event handler that stops integration early
    @Test
    public void testIntegrate_eventHandlerStopsIntegration_stopsAtEventTime() throws DerivativeException, IntegratorException {
        GillIntegrator integrator = new GillIntegrator(0.1);
        final double eventTime = 0.45;
        integrator.addEventHandler(new EventHandler() {
            public void resetState(double t, double[] y) {}
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
        }, 0.1, 1e-6, 100);

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
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(eventTime, stopTime, 1e-5);
        assertEquals(eventTime, y[0], 1e-5);
    }

    // Tests integration with event handler that resets state during integration
    @Test
    public void testIntegrate_eventHandlerResetsState_stateUpdatedProperly() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        final double eventTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public void resetState(double t, double[] y) {
                y[0] += 10.0;
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_STATE;
            }
        }, 0.1, 1e-6, 100);

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
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-5);
        // y without reset would be 1.0, with +10.0 jump at 0.5 it becomes 11.0
        assertEquals(11.0, y[0], 1e-4);
    }

    // Tests integration with event handler that resets derivatives
    @Test
    public void testIntegrate_eventHandlerResetsDerivatives_continuesWithNewDerivatives() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        final double eventTime = 0.5;
        final boolean[] rateChanged = new boolean[] { false };

        integrator.addEventHandler(new EventHandler() {
            public void resetState(double t, double[] y) {}
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                rateChanged[0] = true;
                return RESET_DERIVATIVES;
            }
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = rateChanged[0] ? 2.0 : 1.0;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-5);
        assertEquals(1.5, y[0], 1e-4);
    }

    // Tests event occurring exactly at or near step start (dt <= Math.ulp(stepStart) branch)
    @Test
    public void testIntegrate_eventAtStartBoundary_handledProperly() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        integrator.addEventHandler(new EventHandler() {
            public void resetState(double t, double[] y) {}
            public double g(double t, double[] y) {
                return t - 0.0;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }
        }, 0.1, 1e-6, 100);

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
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-10);
        assertEquals(1.0, y[0], 1e-5);
    }

    // Tests multiple stages execution with non-trivial Butcher array (ClassicalRungeKuttaIntegrator)
    @Test
    public void testIntegrate_classicalRungeKuttaMultipleStages_evaluatesAccurately() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.2);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                // y' = 3 * t^2 -> exact solution y(t) = t^3
                yDot[0] = 3.0 * t * t;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals(2.0, stopTime, 1e-10);
        assertEquals(8.0, y[0], 1e-6);
    }

    // Tests step size sign reset when switching between steps
    @Test
    public void testIntegrate_nonMultipleStepInterval_truncatesFinalStepProperly() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.3);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        // 0.0 to 1.0 with step size 0.3 leaves last step as 0.1
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-10);
        assertEquals(2.0, y[0], 1e-10);
    }

    // Tests ThreeEighthesIntegrator integration
    @Test
    public void testIntegrate_threeEighthesIntegrator_reachesFinalTime() throws DerivativeException, IntegratorException {
        ThreeEighthesIntegrator integrator = new ThreeEighthesIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 3.0 * t * t;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals(2.0, stopTime, 1e-10);
        assertEquals(8.0, y[0], 1e-6);
    }

    // Tests integration when step size is larger than total interval (single truncated step)
    @Test
    public void testIntegrate_stepSizeLargerThanInterval_singleStepIntegration() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(10.0);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0;
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stopTime, 1e-10);
        assertEquals(2.0, y[0], 1e-10);
    }

    // Tests backward integration with event handler stopping integration
    @Test
    public void testIntegrate_backwardWithEventHandlerStop_stopsAtEventTime() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        final double eventTime = 0.45;
        integrator.addEventHandler(new EventHandler() {
            public void resetState(double t, double[] y) {}
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
        }, 0.1, 1e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 1.0, y0, 0.0, y);
        assertEquals(eventTime, stopTime, 1e-5);
        assertEquals(0.45, y[0], 1e-5);
    }

    // Tests dense output step interpolator at intermediate time query
    @Test
    public void testIntegrate_denseStepInterpolatorInterpolateState_matchesExact() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.5);
        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return true;
            }
            public void reset() {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
                double tMid = (interpolator.getPreviousTime() + interpolator.getCurrentTime()) / 2.0;
                interpolator.setInterpolatedTime(tMid);
                double[] interpolatedState = interpolator.getInterpolatedState();
                assertEquals(tMid, interpolatedState[0], 1e-4);
            }
        });
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
        integrator.integrate(ode, 0.0, y0, 2.0, y);
    }

    // Tests exception propagated when derivative computation fails
    @Test(expected = DerivativeException.class)
    public void testIntegrate_derivativeException_throwsDerivativeException() throws DerivativeException, IntegratorException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                throw new DerivativeException("Derivative computation failed", new Object[0]);
            }
        };
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }
}
package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;
import static org.apache.commons.math.ode.events.EventHandler.CONTINUE;
import static org.apache.commons.math.ode.events.EventHandler.RESET_DERIVATIVES;
import static org.apache.commons.math.ode.events.EventHandler.RESET_STATE;
import static org.apache.commons.math.ode.events.EventHandler.STOP;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmbeddedRungeKuttaIntegratorTest {

    // Tests default safety factor and setter/getter
    @Test
    public void testGetSetSafety_validValue_updatesCorrectly() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(0.9, integrator.getSafety(), 1.0e-15);
        integrator.setSafety(0.85);
        assertEquals(0.85, integrator.getSafety(), 1.0e-15);
    }

    // Tests default min reduction factor and setter/getter
    @Test
    public void testGetSetMinReduction_validValue_updatesCorrectly() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(0.2, integrator.getMinReduction(), 1.0e-15);
        integrator.setMinReduction(0.1);
        assertEquals(0.1, integrator.getMinReduction(), 1.0e-15);
    }

    // Tests default max growth factor and setter/getter
    @Test
    public void testGetSetMaxGrowth_validValue_updatesCorrectly() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-15);
        integrator.setMaxGrowth(15.0);
        assertEquals(15.0, integrator.getMaxGrowth(), 1.0e-15);
    }

    // Tests standard forward integration with scalar tolerances
    @Test
    public void testIntegrate_scalarTolerances_integratesAccurately() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(2.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(2.0), y[0], 1.0e-6);
    }

    // Tests forward integration with vector tolerances
    @Test
    public void testIntegrate_vectorTolerances_integratesAccurately() throws DerivativeException, IntegratorException {
        double[] vecAbsoluteTolerance = new double[] { 1.0e-10 };
        double[] vecRelativeTolerance = new double[] { 1.0e-10 };
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0,
                vecAbsoluteTolerance, vecRelativeTolerance);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests backward integration (t0 > t)
    @Test
    public void testIntegrate_backwardDirection_integratesAccurately() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new HighamHall54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { Math.exp(2.0) };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 2.0, y0, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-10);
        assertEquals(1.0, y[0], 1.0e-6);
    }

    // Tests integration where output array y is the same instance as y0
    @Test
    public void testIntegrate_sameInputOutputArray_integratesInPlace() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y = new double[] { 1.0 };

        double stopTime = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests integration with a StepHandler
    @Test
    public void testIntegrate_withStepHandler_callsHandler() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-8, 1.0e-8);
        final int[] stepCount = new int[] { 0 };
        integrator.addStepHandler(new StepHandler() {
            public boolean isResetting() {
                return false;
            }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
                assertNotNull(interpolator);
            }
            public void reset() {
            }
        });

        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertTrue(stepCount[0] > 0);
    }

    // Tests discrete event triggering integration stop
    @Test
    public void testIntegrate_withEventHandlerStop_stopsAtEventTime() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        final double eventTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-8, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(eventTime, stopTime, 1.0e-7);
        assertEquals(Math.exp(eventTime), y[0], 1.0e-5);
    }

    // Tests event handler that resets state and forces derivative recomputation
    @Test
    public void testIntegrate_withEventHandlerResetState_continuesIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        final double eventTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_STATE;
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public void resetState(double t, double[] y) {
                y[0] += 1.0;
            }
        }, 1.0, 1.0e-8, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertTrue(y[0] > Math.exp(1.0));
    }

    // Tests event occurring extremely close to the start time
    @Test
    public void testIntegrate_eventAtStartTime_handlesSmallDeltaTime() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }
            public double g(double t, double[] y) {
                return t - 0.0;
            }
            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-12, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests dimension mismatch throwing IntegratorException
    @Test(expected = IntegratorException.class)
    public void testIntegrate_dimensionMismatch_throwsIntegratorException() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0, 2.0 };
        double[] y = new double[2];

        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    // Tests non-FSAL integrator behavior
    @Test
    public void testIntegrate_highamHall54NonFsal_integratesCorrectly() throws DerivativeException, IntegratorException {
        HighamHall54Integrator integrator = new HighamHall54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(5, integrator.getOrder());

        FirstOrderDifferentialEquations ode = new LinearODE(-0.5);
        double[] y0 = new double[] { 2.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(2.0, stopTime, 1.0e-10);
        assertEquals(2.0 * Math.exp(-1.0), y[0], 1.0e-6);
    }

    // Tests event handler returning RESET_DERIVATIVES
    @Test
    public void testIntegrate_withEventHandlerResetDerivatives_continuesIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        final double eventTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_DERIVATIVES;
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-8, 100);

        FirstOrderDifferentialEquations ode = new LinearODE(1.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests step rejection and step reduction when minimal step size is violated
    @Test(expected = IntegratorException.class)
    public void testIntegrate_minStepTooLarge_throwsIntegratorException() throws DerivativeException, IntegratorException {
        // Minimum step is 0.5 but error tolerance is very strict on stiff ODE, forcing step rejection below minStep
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(0.5, 1.0, 1.0e-15, 1.0e-15);
        FirstOrderDifferentialEquations ode = new LinearODE(100.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    // Tests step rejection and recovery within allowed step range
    @Test
    public void testIntegrate_stepRejectionAndAdaptation_integratesAccurately() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-8, 1.0e-8);
        integrator.setSafety(0.5);
        integrator.setMinReduction(0.05);
        integrator.setMaxGrowth(5.0);

        FirstOrderDifferentialEquations ode = new LinearODE(5.0);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];

        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(5.0), y[0], 1.0e-3);
    }

    // Helper ODE: y' = a * y
    private static class LinearODE implements FirstOrderDifferentialEquations {
        private final double a;

        public LinearODE(double a) {
            this.a = a;
        }

        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = a * y[0];
        }
    }
}
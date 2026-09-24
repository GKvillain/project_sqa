package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.events.EventHandler.Action;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmbeddedRungeKuttaIntegratorTest {

    private static class LinearODE implements FirstOrderDifferentialEquations {
        private final double rate;

        public LinearODE(double rate) {
            this.rate = rate;
        }

        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = rate * y[0];
        }
    }

    private static class System2DODE implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 2;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = -y[1];
            yDot[1] = y[0];
        }
    }

    // Tests default safety factor and setter/getter
    @Test
    public void testSafety_getAndSet_returnsUpdatedValue() {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(0.9, integrator.getSafety(), 1.0e-12);

        integrator.setSafety(0.85);
        assertEquals(0.85, integrator.getSafety(), 1.0e-12);
    }

    // Tests default minReduction factor and setter/getter
    @Test
    public void testMinReduction_getAndSet_returnsUpdatedValue() {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);

        integrator.setMinReduction(0.15);
        assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);
    }

    // Tests default maxGrowth factor and setter/getter
    @Test
    public void testMaxGrowth_getAndSet_returnsUpdatedValue() {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-10, 10.0, 1.0e-10, 1.0e-10);
        assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);

        integrator.setMaxGrowth(12.0);
        assertEquals(12.0, integrator.getMaxGrowth(), 1.0e-12);
    }

    // Tests forward integration with scalar tolerances
    @Test
    public void testIntegrate_forwardScalarTolerance_succeeds()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests backward integration with scalar tolerances
    @Test
    public void testIntegrate_backwardScalarTolerance_succeeds()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        double[] y0 = new double[] { Math.exp(1.0) };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 1.0, y0, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-10);
        assertEquals(1.0, y[0], 1.0e-6);
    }

    // Tests integration with vector tolerances constructor
    @Test
    public void testIntegrate_vectorTolerance_succeeds()
            throws DerivativeException, IntegratorException {
        double[] vecAbsTol = new double[] { 1.0e-8, 1.0e-8 };
        double[] vecRelTol = new double[] { 1.0e-8, 1.0e-8 };

        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince853Integrator(1.0e-5, 1.0, vecAbsTol, vecRelTol);
        FirstOrderDifferentialEquations ode = new System2DODE();

        double[] y0 = new double[] { 1.0, 0.0 };
        double[] y = new double[2];
        double stopTime = integrator.integrate(ode, 0.0, y0, Math.PI / 2.0, y);

        assertEquals(Math.PI / 2.0, stopTime, 1.0e-10);
        assertEquals(0.0, y[0], 1.0e-6);
        assertEquals(1.0, y[1], 1.0e-6);
    }

    // Tests integration with HighamHall54 (non-FSAL) integrator
    @Test
    public void testIntegrate_nonFsalIntegrator_succeeds()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new HighamHall54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(-0.5);

        double[] y0 = new double[] { 2.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(2.0, stopTime, 1.0e-10);
        assertEquals(2.0 * Math.exp(-1.0), y[0], 1.0e-6);
    }

    // Tests integration with StepHandler attached
    @Test
    public void testIntegrate_withStepHandler_callsHandler()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        final int[] stepCount = new int[1];
        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return false;
            }

            public void reset() {
                stepCount[0] = 0;
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
            }
        });

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertTrue(stepCount[0] > 0);
    }

    // Tests integration with dense output StepHandler
    @Test
    public void testIntegrate_withDenseStepHandler_interpolatesCorrectly()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince853Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        final double[] midValue = new double[1];
        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return true;
            }

            public void reset() {}

            public void handleStep(StepInterpolator interpolator, boolean isLast)
                    throws DerivativeException {
                if (interpolator.getPreviousTime() <= 0.5 && interpolator.getCurrentTime() >= 0.5) {
                    interpolator.setInterpolatedTime(0.5);
                    midValue[0] = interpolator.getInterpolatedState()[0];
                }
            }
        });

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(Math.exp(0.5), midValue[0], 1.0e-4);
    }

    // Tests integration with EventHandler stopping the integration
    @Test
    public void testIntegrate_withStoppingEventHandler_stopsAtEvent()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        integrator.addEventHandler(new EventHandler() {
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {}
        }, 1.0, 1.0e-6, 100);

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(0.5, stopTime, 1.0e-5);
        assertEquals(Math.exp(0.5), y[0], 1.0e-4);
    }

    // Tests integration with EventHandler resetting the state
    @Test
    public void testIntegrate_withResetStateEventHandler_resetsState()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        integrator.addEventHandler(new EventHandler() {
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {
                y[0] = 1.0;
            }
        }, 1.0, 1.0e-6, 100);

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(0.5), y[0], 1.0e-4);
    }

    // Tests integration with EventHandler resetting derivatives
    @Test
    public void testIntegrate_withResetDerivativesEventHandler_continuesIntegration()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        integrator.addEventHandler(new EventHandler() {
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_DERIVATIVES;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {}
        }, 1.0, 1.0e-6, 100);

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-4);
    }

    // Tests integration with EventHandler returning CONTINUE
    @Test
    public void testIntegrate_withContinueEventHandler_continuesIntegration()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        integrator.addEventHandler(new EventHandler() {
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }

            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public void resetState(double t, double[] y) {}
        }, 1.0, 1.0e-6, 100);

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-4);
    }

    // Tests integration when input and output arrays are the same
    @Test
    public void testIntegrate_sameInputOutputArray_integratesInPlace()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        double[] y = new double[] { 1.0 };
        double stopTime = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-6);
    }

    // Tests exception when dimension of initial state does not match ODE dimension
    @Test(expected = IntegratorException.class)
    public void testIntegrate_dimensionMismatch_throwsException()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new LinearODE(1.0);

        double[] y0 = new double[] { 1.0, 2.0 };
        double[] y = new double[2];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    // Tests exception when minimal stepsize is reached during step rejection
    @Test(expected = IntegratorException.class)
    public void testIntegrate_minStepSizeExceeded_throwsException()
            throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(0.1, 1.0, 1.0e-15, 1.0e-15);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1000.0 * y[0];
            }
        };

        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }
}
package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.ode.ExpandableStatefulODE;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    // Tests default getter and setter values for safety, minReduction, maxGrowth
    @Test
    public void testGettersAndSetters_defaultAndCustomValues_returnExpectedValues() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);

        Assert.assertEquals(0.9, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);

        integrator.setSafety(0.8);
        integrator.setMinReduction(0.1);
        integrator.setMaxGrowth(5.0);

        Assert.assertEquals(0.8, integrator.getSafety(), 1.0e-12);
        Assert.assertEquals(0.1, integrator.getMinReduction(), 1.0e-12);
        Assert.assertEquals(5.0, integrator.getMaxGrowth(), 1.0e-12);
    }

    // Tests forward integration with scalar tolerance (normal case)
    @Test
    public void testIntegrate_forwardScalarTolerance_succeeds() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 1.0 });

        integrator.integrate(stateful, 1.0);

        Assert.assertEquals(1.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(FastMath.exp(1.0), stateful.getPrimaryState()[0], 1.0e-8);
    }

    // Tests backward integration with scalar tolerance
    @Test
    public void testIntegrate_backwardIntegration_succeeds() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(1.0);
        stateful.setPrimaryState(new double[] { FastMath.exp(-1.0) });

        integrator.integrate(stateful, 0.0);

        Assert.assertEquals(0.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(1.0, stateful.getPrimaryState()[0], 1.0e-8);
    }

    // Tests integration with vector tolerance
    @Test
    public void testIntegrate_vectorTolerance_succeeds() {
        double[] vecAbsTol = new double[] { 1.0e-10, 1.0e-10 };
        double[] vecRelTol = new double[] { 1.0e-10, 1.0e-10 };
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0, vecAbsTol, vecRelTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[1];
                yDot[1] = -y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 0.0, 1.0 });

        integrator.integrate(stateful, FastMath.PI / 2.0);

        Assert.assertEquals(FastMath.PI / 2.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(1.0, stateful.getPrimaryState()[0], 1.0e-7);
        Assert.assertEquals(0.0, stateful.getPrimaryState()[1], 1.0e-7);
    }

    // Tests non-FSAL integrator branch using HighamHall54Integrator
    @Test
    public void testIntegrate_nonFsalIntegrator_succeeds() {
        HighamHall54Integrator integrator = new HighamHall54Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -2.0 * y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 1.0 });

        integrator.integrate(stateful, 1.0);

        Assert.assertEquals(1.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(FastMath.exp(-2.0), stateful.getPrimaryState()[0], 1.0e-7);
    }

    // Tests non-FSAL integrator with vector tolerances
    @Test
    public void testIntegrate_nonFsalVectorTolerance_succeeds() {
        double[] vecAbsTol = new double[] { 1.0e-8 };
        double[] vecRelTol = new double[] { 1.0e-8 };
        HighamHall54Integrator integrator = new HighamHall54Integrator(1.0e-8, 10.0, vecAbsTol, vecRelTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 3.0;
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 0.0 });

        integrator.integrate(stateful, 2.0);

        Assert.assertEquals(2.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(6.0, stateful.getPrimaryState()[0], 1.0e-8);
    }

    // Tests regression for initial step size bounding (Defects4J Math-39 defect)
    @Test
    public void testIntegrate_shortTimeIntervalWithLargeInitialStep_doesNotExceedInterval() {
        DormandPrince853Integrator integrator =
                new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
        final double start = 0.0;
        final double end = 0.001;
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                Assert.assertTrue("Derivative evaluation time t is before start: " + t,
                        t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
                Assert.assertTrue("Derivative evaluation time t is beyond end: " + t,
                        t <= FastMath.nextAfter(end, Double.POSITIVE_INFINITY));
                yDot[0] = -100.0 * y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(start);
        stateful.setPrimaryState(new double[] { 1.0 });

        integrator.integrate(stateful, end);

        Assert.assertEquals(end, stateful.getTime(), 1.0e-12);
    }

    // Tests regression for backward short time interval initial step size bounding
    @Test
    public void testIntegrate_shortBackwardInterval_doesNotExceedInterval() {
        DormandPrince853Integrator integrator =
                new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
        final double start = 1.0;
        final double end = 0.999;
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                Assert.assertTrue("Derivative evaluation time t is after start: " + t,
                        t <= FastMath.nextAfter(start, Double.POSITIVE_INFINITY));
                Assert.assertTrue("Derivative evaluation time t is before end: " + t,
                        t >= FastMath.nextAfter(end, Double.NEGATIVE_INFINITY));
                yDot[0] = -100.0 * y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(start);
        stateful.setPrimaryState(new double[] { 1.0 });

        integrator.integrate(stateful, end);

        Assert.assertEquals(end, stateful.getTime(), 1.0e-12);
    }

    // Tests sanity check exception when integration interval is zero
    @Test(expected = MathIllegalArgumentException.class)
    public void testIntegrate_zeroIntegrationInterval_throwsException() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(1.0);
        stateful.setPrimaryState(new double[] { 0.0 });

        integrator.integrate(stateful, 1.0);
    }

    // Tests step rejection and stepsize reduction when tolerance is very tight
    @Test
    public void testIntegrate_stiffStepRejection_adaptsStepSizeAndCompletes() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-15, 10.0, 1.0e-12, 1.0e-12);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = (t < 0.5) ? 1.0 : 1000.0;
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 0.0 });

        integrator.integrate(stateful, 1.0);

        Assert.assertEquals(1.0, stateful.getTime(), 1.0e-12);
        Assert.assertTrue(stateful.getPrimaryState()[0] > 0.0);
    }

    // Tests multiple steps integration to cover loop continuation and isLastStep branch
    @Test
    public void testIntegrate_multipleSteps_completesSuccessfully() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-5, 0.1, 1.0e-8, 1.0e-8);
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = FastMath.sin(t);
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { -1.0 });

        integrator.integrate(stateful, 5.0);

        Assert.assertEquals(5.0, stateful.getTime(), 1.0e-12);
        Assert.assertEquals(-FastMath.cos(5.0), stateful.getPrimaryState()[0], 1.0e-6);
    }

    // Tests dimension mismatch with vector tolerances
    @Test(expected = DimensionMismatchException.class)
    public void testIntegrate_mismatchedVectorToleranceDimension_throwsException() {
        double[] vecAbsTol = new double[] { 1.0e-8, 1.0e-8 };
        double[] vecRelTol = new double[] { 1.0e-8, 1.0e-8 };
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-8, 10.0, vecAbsTol, vecRelTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };

        ExpandableStatefulODE stateful = new ExpandableStatefulODE(ode);
        stateful.setTime(0.0);
        stateful.setPrimaryState(new double[] { 1.0 });

        integrator.integrate(stateful, 1.0);
    }
}
package org.apache.commons.math3.random;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link BitsStreamGenerator}.
 */
public class BitsStreamGeneratorTest {

    private DummyGenerator generator;

    /**
     * Concrete dummy implementation of BitsStreamGenerator for testing.
     */
    private static class DummyGenerator extends BitsStreamGenerator {
        private int[] sequence;
        private int index = 0;
        private int fixedValue = 0;
        private boolean useSequence = false;

        public void setSequence(int... values) {
            this.sequence = values;
            this.index = 0;
            this.useSequence = true;
        }

        public void setFixedValue(int value) {
            this.fixedValue = value;
            this.useSequence = false;
        }

        @Override
        public void setSeed(int seed) {
            this.fixedValue = seed;
        }

        @Override
        public void setSeed(int[] seed) {
            if (seed != null && seed.length > 0) {
                this.fixedValue = seed[0];
            }
        }

        @Override
        public void setSeed(long seed) {
            this.fixedValue = (int) seed;
        }

        @Override
        protected int next(int bits) {
            int val;
            if (useSequence && sequence != null && index < sequence.length) {
                val = sequence[index++];
            } else {
                val = fixedValue;
            }
            return val >>> (32 - bits);
        }
    }

    @Before
    public void setUp() {
        generator = new DummyGenerator();
    }

    // Tests nextBoolean returning true when bit is 1
    @Test
    public void testNextBoolean_bitOne_returnsTrue() {
        generator.setFixedValue(0x80000000);
        assertTrue(generator.nextBoolean());
    }

    // Tests nextBoolean returning false when bit is 0
    @Test
    public void testNextBoolean_bitZero_returnsFalse() {
        generator.setFixedValue(0x00000000);
        assertFalse(generator.nextBoolean());
    }

    // Tests nextBytes with zero length array
    @Test
    public void testNextBytes_emptyArray_doesNotThrow() {
        byte[] bytes = new byte[0];
        generator.nextBytes(bytes);
        assertEquals(0, bytes.length);
    }

    // Tests nextBytes with length less than 4
    @Test
    public void testNextBytes_lengthLessThanFour_fillsCorrectly() {
        generator.setFixedValue(0x04030201);
        byte[] bytes = new byte[3];
        generator.nextBytes(bytes);
        assertEquals(0x01, bytes[0]);
        assertEquals(0x02, bytes[1]);
        assertEquals(0x03, bytes[2]);
    }

    // Tests nextBytes with length multiple of 4
    @Test
    public void testNextBytes_lengthMultipleOfFour_fillsCorrectly() {
        generator.setFixedValue(0x04030201);
        byte[] bytes = new byte[4];
        generator.nextBytes(bytes);
        assertEquals(0x01, bytes[0]);
        assertEquals(0x02, bytes[1]);
        assertEquals(0x03, bytes[2]);
        assertEquals(0x04, bytes[3]);
    }

    // Tests nextBytes with length greater than 4 and non-multiple of 4
    @Test
    public void testNextBytes_lengthGreaterThanFourNotMultiple_fillsCorrectly() {
        generator.setSequence(0x04030201, 0x08070605);
        byte[] bytes = new byte[6];
        generator.nextBytes(bytes);
        assertEquals(0x01, bytes[0]);
        assertEquals(0x02, bytes[1]);
        assertEquals(0x03, bytes[2]);
        assertEquals(0x04, bytes[3]);
        assertEquals(0x05, bytes[4]);
        assertEquals(0x06, bytes[5]);
    }

    // Tests nextDouble range and calculation
    @Test
    public void testNextDouble_normalValues_returnsInRange() {
        generator.setFixedValue(0x7FFFFFFF);
        double val = generator.nextDouble();
        assertTrue(val >= 0.0);
        assertTrue(val < 1.0);
    }

    // Tests nextDouble minimum boundary value
    @Test
    public void testNextDouble_zeroBits_returnsZero() {
        generator.setFixedValue(0x00000000);
        double val = generator.nextDouble();
        assertEquals(0.0, val, 1e-15);
    }

    // Tests nextFloat range and calculation
    @Test
    public void testNextFloat_normalValues_returnsInRange() {
        generator.setFixedValue(0x7FFFFFFF);
        float val = generator.nextFloat();
        assertTrue(val >= 0.0f);
        assertTrue(val < 1.0f);
    }

    // Tests nextFloat minimum boundary value
    @Test
    public void testNextFloat_zeroBits_returnsZero() {
        generator.setFixedValue(0x00000000);
        float val = generator.nextFloat();
        assertEquals(0.0f, val, 1e-7f);
    }

    // Tests nextGaussian pair generation and cached value retrieval
    @Test
    public void testNextGaussian_consecutiveCalls_generatesAndUsesCache() {
        generator.setFixedValue(0x40000000);
        double first = generator.nextGaussian();
        double second = generator.nextGaussian();
        assertFalse(Double.isNaN(first));
        assertFalse(Double.isNaN(second));
    }

    // Tests clear method resetting gaussian cache
    @Test
    public void testClear_afterGaussianCall_clearsCache() {
        generator.setFixedValue(0x40000000);
        generator.nextGaussian();
        generator.clear();
        double third = generator.nextGaussian();
        assertFalse(Double.isNaN(third));
    }

    // Tests nextInt without parameter
    @Test
    public void testNextInt_noArgs_returnsInt() {
        generator.setFixedValue(0x12345678);
        assertEquals(0x12345678, generator.nextInt());
    }

    // Tests nextInt with power of 2 bound
    @Test
    public void testNextInt_powerOfTwoBound_returnsBoundedValue() {
        generator.setFixedValue(0x40000000);
        int result = generator.nextInt(16);
        assertTrue(result >= 0);
        assertTrue(result < 16);
    }

    // Tests nextInt with non-power of 2 bound
    @Test
    public void testNextInt_nonPowerOfTwoBound_returnsBoundedValue() {
        generator.setFixedValue(0x40000000);
        int result = generator.nextInt(10);
        assertTrue(result >= 0);
        assertTrue(result < 10);
    }

    // Tests nextInt with non-power of 2 rejection branch execution
    @Test
    public void testNextInt_nonPowerOfTwoBound_handlesRejectionLoop() {
        generator.setSequence(0x7FFFFFFF, 0x00000002);
        int result = generator.nextInt(Integer.MAX_VALUE - 1);
        assertTrue(result >= 0);
        assertTrue(result < Integer.MAX_VALUE - 1);
    }

    // Tests nextInt with zero bound throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextInt_zeroBound_throwsException() {
        generator.nextInt(0);
    }

    // Tests nextInt with negative bound throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextInt_negativeBound_throwsException() {
        generator.nextInt(-5);
    }

    // Tests nextLong combining two 32-bit values
    @Test
    public void testNextLong_twoCalls_combinesBitsCorrectly() {
        generator.setSequence(0x12345678, 0x0abcdef0);
        long result = generator.nextLong();
        long expected = ((long) 0x12345678 << 32) | (0x0abcdef0L & 0xffffffffL);
        assertEquals(expected, result);
    }

    // Tests seed methods for coverage
    @Test
    public void testSetSeed_variousTypes_setsCorrectly() {
        generator.setSeed(42);
        generator.setSeed(new int[]{42, 43});
        generator.setSeed(42L);
        assertEquals(42, generator.nextInt());
    }
}
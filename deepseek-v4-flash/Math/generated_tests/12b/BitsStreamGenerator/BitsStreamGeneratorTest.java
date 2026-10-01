package org.apache.commons.math3.random;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;

public class BitsStreamGeneratorTest {

    // Stub that allows predefined return values for next(int bits)
    private static class ControlledGenerator extends BitsStreamGenerator {
        private final Queue<Integer> nextValues = new LinkedList<Integer>();

        public void setNextValues(int... values) {
            for (int v : values) {
                nextValues.add(v);
            }
        }

        @Override
        protected int next(int bits) {
            if (nextValues.isEmpty()) {
                throw new RuntimeException("No more values");
            }
            int val = nextValues.remove();
            int mask = (bits == 32) ? -1 : (1 << bits) - 1;
            return val & mask;
        }

        @Override
        public void setSeed(int seed) { }

        @Override
        public void setSeed(int[] seed) { }

        @Override
        public void setSeed(long seed) { }
    }

    private ControlledGenerator generator;

    @Before
    public void setUp() {
        generator = new ControlledGenerator();
    }

    // Tests nextBoolean: true path
    @Test
    public void testNextBoolean_true_returnsTrue() {
        generator.setNextValues(1);
        assertTrue(generator.nextBoolean());
    }

    // Tests nextBoolean: false path
    @Test
    public void testNextBoolean_false_returnsFalse() {
        generator.setNextValues(0);
        assertFalse(generator.nextBoolean());
    }

    // Tests nextBytes with length multiple of 4
    @Test
    public void testNextBytes_lengthMultipleOf4_writesCorrectly() {
        generator.setNextValues(0x12345678);
        byte[] bytes = new byte[4];
        generator.nextBytes(bytes);
        assertEquals((byte) 0x78, bytes[0]);
        assertEquals((byte) 0x56, bytes[1]);
        assertEquals((byte) 0x34, bytes[2]);
        assertEquals((byte) 0x12, bytes[3]);
    }

    // Tests nextBytes with length not multiple of 4
    @Test
    public void testNextBytes_lengthNotMultipleOf4_writesCorrectly() {
        generator.setNextValues(0xAABBCCDD, 0x11223344);
        byte[] bytes = new byte[5];
        generator.nextBytes(bytes);
        assertEquals((byte) 0xDD, bytes[0]);
        assertEquals((byte) 0xCC, bytes[1]);
        assertEquals((byte) 0xBB, bytes[2]);
        assertEquals((byte) 0xAA, bytes[3]);
        assertEquals((byte) 0x44, bytes[4]);
    }

    // Tests nextBytes with empty array (no call to next)
    @Test
    public void testNextBytes_emptyArray_noNextCall() {
        generator.setNextValues();
        byte[] bytes = new byte[0];
        generator.nextBytes(bytes);
        // No exception expected
    }

    // Tests nextDouble calculation
    @Test
    public void testNextDouble_computesCorrectValue() {
        generator.setNextValues(0x2AAAAAA, 0x1555555);
        long high = ((long) 0x2AAAAAA) << 26;
        int low = 0x1555555;
        double expected = (high | low) * 0x1.0p-52d;
        double actual = generator.nextDouble();
        assertEquals(expected, actual, 0.0);
    }

    // Tests nextDouble boundary (max 26-bit values)
    @Test
    public void testNextDouble_boundaryMaxValues() {
        generator.setNextValues(0x3FFFFFF, 0x3FFFFFF);
        long high = ((long) 0x3FFFFFF) << 26;
        int low = 0x3FFFFFF;
        double expected = (high | low) * 0x1.0p-52d;
        double actual = generator.nextDouble();
        assertEquals(expected, actual, 0.0);
    }

    // Tests nextFloat calculation
    @Test
    public void testNextFloat_computesCorrectValue() {
        generator.setNextValues(0x7FFFFF); // max 23-bit
        float expected = 0x7FFFFF * 0x1.0p-23f;
        float actual = generator.nextFloat();
        assertEquals(expected, actual, 0.0f);
    }

    // Tests nextGaussian: first call generates pair and returns first component
    @Test
    public void testNextGaussian_firstCall_returnsFirstComponent() {
        // Construct nextDouble values to produce x=0.25 and y=0.5
        // x=0.25: high=0x1000000, low=0; y=0.5: high=0x2000000, low=0
        generator.setNextValues(0x1000000, 0, 0x2000000, 0);
        double first = generator.nextGaussian();
        assertEquals(0.0, first, 1e-12);
        double second = generator.nextGaussian();
        assertEquals(1.1774100221154745, second, 1e-12);
    }

    // Tests nextGaussian: second call returns cached value
    @Test
    public void testNextGaussian_secondCall_returnsCachedValue() {
        generator.setNextValues(0x1000000, 0, 0x2000000, 0);
        generator.nextGaussian(); // first call
        double second = generator.nextGaussian(); // cached
        assertEquals(1.1774100221154745, second, 1e-12);
    }

    // Tests nextInt returns next(32)
    @Test
    public void testNextInt_returnsNext32Bits() {
        generator.setNextValues(0xDEADBEEF);
        assertEquals(0xDEADBEEF, generator.nextInt());
    }

    // Tests nextInt(int n): n negative throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextInt_nNegative_throwsException() {
        generator.nextInt(-1);
    }

    // Tests nextInt(int n): n zero throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextInt_nZero_throwsException() {
        generator.nextInt(0);
    }

    // Tests nextInt(int n): power of two (n=2)
    @Test
    public void testNextInt_nPowerOfTwo_n2_returnsCorrectResult() {
        generator.setNextValues(0x12345678 & 0x7FFFFFFF);
        int expected = (int) ((2L * (0x12345678 & 0x7FFFFFFF)) >> 31);
        assertEquals(expected, generator.nextInt(2));
    }

    // Tests nextInt(int n): power of two (n=1)
    @Test
    public void testNextInt_nIsOne_returnsZero() {
        generator.setNextValues(12345);
        assertEquals(0, generator.nextInt(1));
    }

    // Tests nextInt(int n): non-power of two (n=3) with accepted value
    @Test
    public void testNextInt_nNonPowerOfTwo_n3_accepted() {
        generator.setNextValues(5);
        assertEquals(5 % 3, generator.nextInt(3));
    }

    // Tests nextInt(int n): rejection loop (n=3)
    @Test
    public void testNextInt_nNonPowerOfTwo_n3_rejectedThenAccepted() {
        // 2147483646 is in rejection range for n=3
        generator.setNextValues(2147483646, 5);
        assertEquals(5 % 3, generator.nextInt(3));
    }

    // Tests nextInt(int n): boundary value n=Integer.MAX_VALUE
    @Test
    public void testNextInt_nMaxValue_returnsZero() {
        generator.setNextValues(0);
        assertEquals(0, generator.nextInt(Integer.MAX_VALUE));
    }

    // Tests nextLong composition
    @Test
    public void testNextLong_returnsCorrectComposition() {
        int high = 0x12345678;
        int low = 0x9ABCDEF0;
        generator.setNextValues(high, low);
        long expected = ((long) high << 32) | (low & 0xFFFFFFFFL);
        assertEquals(expected, generator.nextLong());
    }

    // Tests clear resets the Gaussian cache
    @Test
    public void testClear_resetsGaussianCache() {
        // First pair: x=0.25, y=0.5
        generator.setNextValues(0x1000000, 0, 0x2000000, 0);
        generator.nextGaussian(); // first call, cache populated
        generator.clear();
        // New pair: x=0.5, y=0.25
        generator.setNextValues(0x2000000, 0, 0x1000000, 0);
        double afterClear = generator.nextGaussian();
        assertEquals(-1.665109913995753, afterClear, 1e-12);
    }

    // Tests nextBytes with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testNextBytes_nullInput_throwsNullPointer() {
        generator.nextBytes(null);
    }
}
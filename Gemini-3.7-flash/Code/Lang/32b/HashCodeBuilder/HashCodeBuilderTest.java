package org.apache.commons.lang3.builder;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashCodeBuilderTest {

    static class TestObject {
        private int a;

        public TestObject(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }
    }

    static class TestSubObject extends TestObject {
        private int b;
        @SuppressWarnings("unused")
        private transient int t;

        public TestSubObject(int a, int b, int t) {
            super(a);
            this.b = b;
            this.t = t;
        }
    }

    static class ReflectionTestCycleA {
        ReflectionTestCycleB b;
    }

    static class ReflectionTestCycleB {
        ReflectionTestCycleA a;
    }

    // Tests default constructor and manual appending of primitives
    @Test
    public void testAppend_primitiveTypes_computesExpectedHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(true);
        builder.append((byte) 1);
        builder.append('a');
        builder.append(1.0d);
        builder.append(1.0f);
        builder.append(1);
        builder.append(1L);
        builder.append((short) 1);
        builder.append("test");
        builder.appendSuper(100);

        int expected = 17;
        expected = expected * 37 + 0;
        expected = expected * 37 + 1;
        expected = expected * 37 + 'a';
        expected = expected * 37 + ((int) (Double.doubleToLongBits(1.0d) ^ (Double.doubleToLongBits(1.0d) >> 32)));
        expected = expected * 37 + Float.floatToIntBits(1.0f);
        expected = expected * 37 + 1;
        expected = expected * 37 + ((int) (1L ^ (1L >> 32)));
        expected = expected * 37 + 1;
        expected = expected * 37 + "test".hashCode();
        expected = expected * 37 + 100;

        assertEquals(expected, builder.toHashCode());
        assertEquals(expected, builder.hashCode());
    }

    // Tests null values and arrays of primitives and objects
    @Test
    public void testAppend_arraysAndNulls_computesCorrectly() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append((Object) null);
        builder.append((boolean[]) null);
        builder.append((byte[]) null);
        builder.append((char[]) null);
        builder.append((double[]) null);
        builder.append((float[]) null);
        builder.append((int[]) null);
        builder.append((long[]) null);
        builder.append((short[]) null);
        builder.append((Object[]) null);

        builder.append(new boolean[]{true, false});
        builder.append(new byte[]{1, 2});
        builder.append(new char[]{'a', 'b'});
        builder.append(new double[]{1.0, 2.0});
        builder.append(new float[]{1.0f, 2.0f});
        builder.append(new int[]{1, 2});
        builder.append(new long[]{1L, 2L});
        builder.append(new short[]{1, 2});
        builder.append(new Object[]{"a", "b"});
        builder.append(new Object[]{new int[]{1, 2}});

        assertTrue(builder.toHashCode() != 0);
    }

    // Tests constructor exception on even initial value
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenInitialValue_throwsException() {
        new HashCodeBuilder(2, 37);
    }

    // Tests constructor exception on zero initial value
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroInitialValue_throwsException() {
        new HashCodeBuilder(0, 37);
    }

    // Tests constructor exception on even multiplier
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenMultiplier_throwsException() {
        new HashCodeBuilder(17, 2);
    }

    // Tests constructor exception on zero multiplier
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroMultiplier_throwsException() {
        new HashCodeBuilder(17, 0);
    }

    // Tests basic reflectionHashCode
    @Test
    public void testReflectionHashCode_validObject_calculatesHashCode() {
        TestObject obj = new TestObject(42);
        int expected = (17 * 37 + 42);
        assertEquals(expected, HashCodeBuilder.reflectionHashCode(obj));
    }

    // Tests reflectionHashCode with transient fields flag true and false
    @Test
    public void testReflectionHashCode_transientFields_handledCorrectly() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        int hashWithoutTransients = HashCodeBuilder.reflectionHashCode(obj, false);
        int hashWithTransients = HashCodeBuilder.reflectionHashCode(obj, true);

        int expectedWithout = ((17 * 37 + 2) * 37 + 1);
        int expectedWith = (((17 * 37 + 2) * 37 + 3) * 37 + 1);

        assertEquals(expectedWithout, hashWithoutTransients);
        assertEquals(expectedWith, hashWithTransients);
    }

    // Tests reflectionHashCode with excludeFields array
    @Test
    public void testReflectionHashCode_excludeFields_excludesCorrectFields() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        int hash = HashCodeBuilder.reflectionHashCode(obj, new String[]{"b"});
        int expected = 17 * 37 + 1;
        assertEquals(expected, hash);
    }

    // Tests reflectionHashCode with excludeFields collection
    @Test
    public void testReflectionHashCode_excludeFieldsCollection_excludesCorrectFields() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        Collection<String> excludes = new ArrayList<String>();
        excludes.add("b");
        int hash = HashCodeBuilder.reflectionHashCode(obj, excludes);
        int expected = 17 * 37 + 1;
        assertEquals(expected, hash);
    }

    // Tests reflectionHashCode up to specified superclass
    @Test
    public void testReflectionHashCode_reflectUpToClass_stopsAtSpecifiedClass() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, TestSubObject.class);
        int expected = 17 * 37 + 2;
        assertEquals(expected, hash);
    }

    // Tests reflectionHashCode with null target object
    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCode_nullObject_throwsException() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    // Tests cyclical references handling in reflectionHashCode (Lang-32 defect prevention)
    @Test
    public void testReflectionHashCode_cyclicalReference_avoidsInfiniteLoop() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;

        int hashA = HashCodeBuilder.reflectionHashCode(a);
        int hashB = HashCodeBuilder.reflectionHashCode(b);

        assertTrue(hashA != 0);
        assertTrue(hashB != 0);
        assertFalse(HashCodeBuilder.isRegistered(a));
        assertFalse(HashCodeBuilder.isRegistered(b));
    }

    // Tests registry methods directly for registration lifecycle
    @Test
    public void testRegistry_registerAndUnregister_registryStateMaintained() {
        Object obj = new Object();
        assertFalse(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.register(obj);
        assertTrue(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.unregister(obj);
        assertFalse(HashCodeBuilder.isRegistered(obj));
    }
}
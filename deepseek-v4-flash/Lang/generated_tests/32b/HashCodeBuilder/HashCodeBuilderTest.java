package org.apache.commons.lang3.builder;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class HashCodeBuilderTest {

    private static class TestObject {
        public int intField = 1;
        public String stringField = "test";
        public transient int transField = 2;
        public static int staticField = 3;
    }

    private static class SubTestObject extends TestObject {
        public double doubleField = 2.0;
    }

    private static class SelfRef {
        public SelfRef ref;
    }

    @Before
    public void setUp() {
        HashCodeBuilder.getRegistry().clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroInitial_throwsException() {
        new HashCodeBuilder(0, 37);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenInitial_throwsException() {
        new HashCodeBuilder(2, 37);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroMultiplier_throwsException() {
        new HashCodeBuilder(17, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenMultiplier_throwsException() {
        new HashCodeBuilder(17, 2);
    }

    @Test
    public void testConstructor_validValues_initializesTotalAndConstant() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        assertEquals(17, builder.toHashCode());
        builder.append(0);
        assertEquals(629, builder.toHashCode());
    }

    @Test
    public void testAppend_boolean_verifiesValues() {
        assertEquals(629, new HashCodeBuilder(17, 37).append(true).toHashCode());
        assertEquals(630, new HashCodeBuilder(17, 37).append(false).toHashCode());
    }

    @Test
    public void testAppend_booleanArray_null() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37).append((boolean[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppend_booleanArray_equalsSequentialAppend() {
        HashCodeBuilder seq = new HashCodeBuilder(17, 37).append(true).append(false);
        HashCodeBuilder arr = new HashCodeBuilder(17, 37).append(new boolean[]{true, false});
        assertEquals(seq.toHashCode(), arr.toHashCode());
    }

    @Test
    public void testAppend_intArray_equalsSequentialAppend() {
        int[] values = {5, 10};
        HashCodeBuilder seq = new HashCodeBuilder(17, 37).append(5).append(10);
        HashCodeBuilder arr = new HashCodeBuilder(17, 37).append(values);
        assertEquals(seq.toHashCode(), arr.toHashCode());
    }

    @Test
    public void testAppend_object_null() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37).append((Object) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppend_object_primitiveArray_equalsSequentialAppend() {
        int[] arr = {1, 2};
        HashCodeBuilder seq = new HashCodeBuilder(17, 37).append(1).append(2);
        HashCodeBuilder obj = new HashCodeBuilder(17, 37).append((Object) arr);
        assertEquals(seq.toHashCode(), obj.toHashCode());
    }

    @Test
    public void testAppend_object_objectArray_equalsSequentialAppend() {
        String[] arr = {"a", "b"};
        HashCodeBuilder seq = new HashCodeBuilder(17, 37).append("a").append("b");
        HashCodeBuilder obj = new HashCodeBuilder(17, 37).append((Object) arr);
        assertEquals(seq.toHashCode(), obj.toHashCode());
    }

    @Test
    public void testAppend_long_usesShift() {
        long value = 0x123456789ABCDEFL;
        int expectedPart = (int) (value ^ (value >> 32));
        int expected = 17 * 37 + expectedPart;
        assertEquals(expected, new HashCodeBuilder(17, 37).append(value).toHashCode());
    }

    @Test
    public void testAppendSuper() {
        int superHash = 999;
        HashCodeBuilder builder = new HashCodeBuilder(17, 37).appendSuper(superHash);
        assertEquals(17 * 37 + superHash, builder.toHashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCode_nullObject_throwsException() {
        HashCodeBuilder.reflectionHashCode(17, 37, null);
    }

    @Test
    public void testReflectionHashCode_sameObject_consistent() {
        TestObject obj = new TestObject();
        assertEquals(HashCodeBuilder.reflectionHashCode(17, 37, obj),
                     HashCodeBuilder.reflectionHashCode(17, 37, obj));
    }

    @Test
    public void testReflectionHashCode_differentObjects_different() {
        TestObject obj1 = new TestObject();
        TestObject obj2 = new TestObject();
        obj2.intField = 999;
        assertNotEquals(HashCodeBuilder.reflectionHashCode(17, 37, obj1),
                        HashCodeBuilder.reflectionHashCode(17, 37, obj2));
    }

    @Test
    public void testReflectionHashCode_withTransients() {
        TestObject obj = new TestObject();
        int hWithout = HashCodeBuilder.reflectionHashCode(17, 37, obj, false);
        int hWith = HashCodeBuilder.reflectionHashCode(17, 37, obj, true);
        assertNotEquals(hWithout, hWith);
    }

    @Test
    public void testReflectionHashCode_excludeFields() {
        TestObject obj = new TestObject();
        int hAll = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, null);
        int hExclude = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, new String[]{"stringField"});
        assertNotEquals(hAll, hExclude);
    }

    @Test
    public void testReflectionHashCode_reflectUpToClass() {
        SubTestObject obj = new SubTestObject();
        int hSub = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, SubTestObject.class);
        int hTest = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, TestObject.class);
        assertNotEquals(hSub, hTest);
    }

    @Test(timeout = 2000)
    public void testReflectionHashCode_circularReference_noInfiniteLoop() {
        SelfRef obj = new SelfRef();
        obj.ref = obj;
        int h = HashCodeBuilder.reflectionHashCode(17, 37, obj);
        assertNotNull(Integer.valueOf(h));
    }

    @Test
    public void testRegister_isRegistered_unregister() {
        Object obj = new Object();
        assertFalse(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.register(obj);
        assertTrue(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.unregister(obj);
        assertFalse(HashCodeBuilder.isRegistered(obj));
    }
}
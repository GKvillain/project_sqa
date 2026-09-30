package org.apache.commons.lang3.builder;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HashCodeBuilderTest {

    static class SimpleObject {
        private final int value;

        public SimpleObject(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    static class ParentObject {
        private final int parentValue = 10;
    }

    static class ChildObject extends ParentObject {
        private final int childValue = 20;
        private transient int transientValue = 30;
        private static int staticValue = 40;
    }

    static class CyclicA {
        CyclicB b;
        int value = 1;
    }

    static class CyclicB {
        CyclicA a;
        int value = 2;
    }

    // Tests default constructor and toHashCode / hashCode contract
    @Test
    public void testConstructor_default_computesInitialTotal() {
        HashCodeBuilder builder = new HashCodeBuilder();
        assertEquals(17, builder.toHashCode());
        assertEquals(17, builder.hashCode());
    }

    // Tests custom valid initial and multiplier values
    @Test
    public void testConstructor_customOddParameters_computesInitialTotal() {
        HashCodeBuilder builder = new HashCodeBuilder(19, 41);
        assertEquals(19, builder.toHashCode());
    }

    // Tests exception path for zero initial number
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroInitialNumber_throwsException() {
        new HashCodeBuilder(0, 37);
    }

    // Tests exception path for even initial number
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenInitialNumber_throwsException() {
        new HashCodeBuilder(18, 37);
    }

    // Tests exception path for zero multiplier number
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroMultiplier_throwsException() {
        new HashCodeBuilder(17, 0);
    }

    // Tests exception path for even multiplier number
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_evenMultiplier_throwsException() {
        new HashCodeBuilder(17, 38);
    }

    // Tests primitive boolean and boolean array appending
    @Test
    public void testAppend_booleanAndBooleanArray_returnsCorrectHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(true).append(false);
        int expected = (17 * 37 + 0) * 37 + 1;
        assertEquals(expected, builder.toHashCode());

        HashCodeBuilder arrayBuilder = new HashCodeBuilder(17, 37);
        arrayBuilder.append((boolean[]) null);
        assertEquals(17 * 37, arrayBuilder.toHashCode());

        HashCodeBuilder arrayBuilder2 = new HashCodeBuilder(17, 37);
        arrayBuilder2.append(new boolean[]{true, false});
        assertEquals(expected, arrayBuilder2.toHashCode());
    }

    // Tests primitive byte, char, short, int, long, float, double and their arrays
    @Test
    public void testAppend_primitivesAndArrays_returnsCorrectHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((byte) 1)
               .append('a')
               .append((short) 2)
               .append(3)
               .append(4L)
               .append(5.0f)
               .append(6.0d);
        assertTrue(builder.toHashCode() != 17);

        HashCodeBuilder nullArrayBuilder = new HashCodeBuilder(17, 37);
        nullArrayBuilder.append((byte[]) null)
                        .append((char[]) null)
                        .append((short[]) null)
                        .append((int[]) null)
                        .append((long[]) null)
                        .append((float[]) null)
                        .append((double[]) null);
        assertTrue(nullArrayBuilder.toHashCode() != 17);

        HashCodeBuilder arrayBuilder = new HashCodeBuilder(17, 37);
        arrayBuilder.append(new byte[]{1})
                    .append(new char[]{'a'})
                    .append(new short[]{2})
                    .append(new int[]{3})
                    .append(new long[]{4L})
                    .append(new float[]{5.0f})
                    .append(new double[]{6.0d});
        assertTrue(arrayBuilder.toHashCode() != 17);
    }

    // Tests Object, nested primitive arrays, and multi-dimensional Object arrays
    @Test
    public void testAppend_objectAndNestedArrays_returnsCorrectHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append("test")
               .append((Object) null)
               .append((Object) new int[]{1, 2})
               .append((Object) new Object[]{"sub", null});
        assertTrue(builder.toHashCode() != 17);

        HashCodeBuilder objArrayBuilder = new HashCodeBuilder(17, 37);
        objArrayBuilder.append((Object[]) null);
        assertEquals(17 * 37, objArrayBuilder.toHashCode());
    }

    // Tests appendSuper method
    @Test
    public void testAppendSuper_validSuperHashCode_updatesRunningTotal() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.appendSuper(100);
        assertEquals(17 * 37 + 100, builder.toHashCode());
    }

    // Tests basic reflectionHashCode invocation on a simple object
    @Test
    public void testReflectionHashCode_simpleObject_computesHashCode() {
        SimpleObject obj = new SimpleObject(42);
        int expected = (17 * 37 + 42);
        assertEquals(expected, HashCodeBuilder.reflectionHashCode(obj));
    }

    // Tests reflectionHashCode with excludeFields array
    @Test
    public void testReflectionHashCode_excludeFields_ignoresSpecifiedFields() {
        SimpleObject obj = new SimpleObject(42);
        int codeExcluded = HashCodeBuilder.reflectionHashCode(obj, new String[]{"value"});
        assertEquals(17, codeExcluded);
    }

    // Tests reflectionHashCode with excludeFields collection
    @Test
    public void testReflectionHashCode_excludeFieldsCollection_ignoresSpecifiedFields() {
        SimpleObject obj = new SimpleObject(42);
        List<String> excludes = new ArrayList<String>();
        excludes.add("value");
        int codeExcluded = HashCodeBuilder.reflectionHashCode(obj, excludes);
        assertEquals(17, codeExcluded);
    }

    // Tests reflectionHashCode with transient fields and inheritance
    @Test
    public void testReflectionHashCode_hierarchyAndTransients_handlesHierarchyCorrectly() {
        ChildObject obj = new ChildObject();
        int withoutTransients = HashCodeBuilder.reflectionHashCode(obj, false);
        int withTransients = HashCodeBuilder.reflectionHashCode(obj, true);
        assertTrue(withoutTransients != withTransients);

        int upToChild = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, ChildObject.class);
        int upToParent = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, ParentObject.class);
        assertTrue(upToChild != upToParent);
    }

    // Tests reflectionHashCode with cyclic reference to detect infinite loop / recursion defects
    @Test
    public void testReflectionHashCode_cyclicalReference_avoidsInfiniteLoop() {
        CyclicA a = new CyclicA();
        CyclicB b = new CyclicB();
        a.b = b;
        b.a = a;

        int hashA = HashCodeBuilder.reflectionHashCode(a);
        int hashB = HashCodeBuilder.reflectionHashCode(b);
        assertTrue(hashA != 0);
        assertTrue(hashB != 0);
        assertFalse(HashCodeBuilder.isRegistered(a));
        assertFalse(HashCodeBuilder.isRegistered(b));
    }

    // Tests registry operations directly
    @Test
    public void testRegistry_registerAndUnregister_managesThreadLocalState() {
        Object obj = new Object();
        assertFalse(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.register(obj);
        assertTrue(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.unregister(obj);
        assertFalse(HashCodeBuilder.isRegistered(obj));
        assertNotNull(HashCodeBuilder.getRegistry());
    }

    // Tests reflectionHashCode null argument exception path
    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCode_nullObject_throwsException() {
        HashCodeBuilder.reflectionHashCode(null);
    }
}
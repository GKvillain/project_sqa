package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

public class ClassUtilsTest {

    // getShortClassName tests
    @Test
    public void testGetShortClassName_nullInput_returnsEmpty() {
        assertEquals("", ClassUtils.getShortClassName((String) null));
    }

    @Test
    public void testGetShortClassName_simpleClass_returnsShortName() {
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
    }

    @Test
    public void testGetShortClassName_arrayPrimitiveEncoded_returnsShortNameWithBrackets() {
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
    }

    @Test
    public void testGetShortClassName_arrayObjectEncoded_returnsShortNameWithBrackets() {
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
    }

    @Test
    public void testGetShortClassName_innerClass_returnsShortNameWithDot() {
        assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
    }

    // getPackageName tests
    @Test
    public void testGetPackageName_nullInput_returnsEmpty() {
        assertEquals("", ClassUtils.getPackageName((String) null));
    }

    @Test
    public void testGetPackageName_arrayEncoded_returnsCorrectPackage() {
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
    }

    // isInnerClass test
    @Test
    public void testIsInnerClass_innerClass_returnsTrue() {
        assertTrue(ClassUtils.isInnerClass(java.util.Map.Entry.class));
    }

    // primitiveToWrapper tests
    @Test
    public void testPrimitiveToWrapper_null_returnsNull() {
        assertNull(ClassUtils.primitiveToWrapper(null));
    }

    @Test
    public void testPrimitiveToWrapper_primitiveInt_returnsInteger() {
        assertSame(Integer.class, ClassUtils.primitiveToWrapper(int.class));
    }

    // wrapperToPrimitive tests
    @Test
    public void testWrapperToPrimitive_wrapperInteger_returnsIntType() {
        assertSame(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
    }

    @Test
    public void testWrapperToPrimitive_nonWrapper_returnsNull() {
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
    }

    // isAssignable tests
    @Test
    public void testIsAssignable_intToLong_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(int.class, long.class));
    }

    @Test
    public void testIsAssignable_intToBoolean_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(int.class, boolean.class));
    }

    @Test
    public void testIsAssignable_nullToClass_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(null, String.class));
    }

    @Test
    public void testIsAssignable_nullToPrimitive_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(null, int.class));
    }

    @Test
    public void testIsAssignable_toClassNull_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(String.class, null));
    }

    @Test
    public void testIsAssignable_autoboxingPrimitiveToWrapper_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(int.class, Integer.class));
    }

    @Test
    public void testIsAssignable_autoboxingWrapperToPrimitive_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Integer.class, int.class));
    }

    @Test
    public void testIsAssignable_autoboxingFalsePrimitiveToWrapper_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(int.class, Integer.class, false));
    }

    // isAssignable with class arrays
    @Test
    public void testIsAssignableClassArrays_bothNull_returnsTrue() {
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
    }

    @Test
    public void testIsAssignableClassArrays_differentLength_returnsFalse() {
        Class<?>[] from = {int.class};
        Class<?>[] to = {int.class, long.class};
        assertFalse(ClassUtils.isAssignable(from, to));
    }

    // getClass tests
    @Test
    public void testGetClass_primitiveName_returnsPrimitiveClass() throws ClassNotFoundException {
        assertEquals(int.class, ClassUtils.getClass("int"));
    }

    @Test
    public void testGetClass_objectArrayName_returnsArrayClass() throws ClassNotFoundException {
        Class<?> clazz = ClassUtils.getClass("[Ljava.lang.String;");
        assertTrue(clazz.isArray());
        assertEquals(String.class, clazz.getComponentType());
    }

    // getShortCanonicalName (indirectly tests getCanonicalName)
    @Test
    public void testGetShortCanonicalName_arrayEncoded_returnsCanonicalName() {
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
    }

    // convertClassNamesToClasses
    @Test
    public void testConvertClassNamesToClasses_nullInput_returnsNull() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
    }

    @Test
    public void testConvertClassNamesToClasses_mixedNames_returnsCorrectList() {
        List<String> names = new ArrayList<String>();
        names.add("java.lang.String");
        names.add("invalid.ClassName");
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(2, classes.size());
        assertSame(String.class, classes.get(0));
        assertNull(classes.get(1));
    }
}
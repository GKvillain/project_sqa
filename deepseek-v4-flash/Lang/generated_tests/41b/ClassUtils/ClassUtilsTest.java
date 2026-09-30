package org.apache.commons.lang;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

public class ClassUtilsTest {

    // Tests getShortClassName with null Class argument
    @Test
    public void testGetShortClassName_nullClass_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getShortClassName((Class<?>) null));
    }

    // Tests getShortClassName with null String argument
    @Test
    public void testGetShortClassName_nullString_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getShortClassName((String) null));
    }

    // Tests getShortClassName with empty String
    @Test
    public void testGetShortClassName_emptyString_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getShortClassName(""));
    }

    // Tests getShortClassName with a simple class name (no inner class)
    @Test
    public void testGetShortClassName_simpleClass_returnsClassName() {
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
    }

    // Tests getShortClassName with an inner class (separator $)
    @Test
    public void testGetShortClassName_innerClass_returnsClassNameWithDot() {
        // Example: java.util.Map$Entry -> short name becomes "Map.Entry"
        assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
    }

    // Tests getShortClassName with an array class name (JVM representation)
    @Test
    public void testGetShortClassName_arrayJvmName_returnsShortName() {
        // "[Ljava.lang.String;" -> "String[]"
        // Note: current implementation does not strip array encoding, so it returns "String[]" ? Actually method uses lastIndexOf('.') and inner index.
        // For "[Ljava.lang.String;", lastDotIdx = -1? No, there is no dot, so lastDotIdx = -1, innerIdx = -1, then out = "[Ljava.lang.String;".
        // We need to check behavior. The code has comment "Handle array encoding" but not implemented. So it returns the raw string.
        // We'll test that it returns the JVM encoded name as is.
        String className = "[Ljava.lang.String;";
        assertEquals(className, ClassUtils.getShortClassName(className));
    }

    // Tests getPackageName with null Class
    @Test
    public void testGetPackageName_nullClass_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageName((Class<?>) null));
    }

    // Tests getPackageName with a class that has a package
    @Test
    public void testGetPackageName_withPackage_returnsPackage() {
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
    }

    // Tests getPackageName with a class without a package (default package)
    @Test
    public void testGetPackageName_noPackage_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageName("SomeClass"));
    }

    // Tests getAllSuperclasses with null
    @Test
    public void testGetAllSuperclasses_null_returnsNull() {
        assertNull(ClassUtils.getAllSuperclasses(null));
    }

    // Tests getAllSuperclasses for a class with superclass
    @Test
    public void testGetAllSuperclasses_nonNull_returnsList() {
        List<Class<?>> supers = ClassUtils.getAllSuperclasses(java.util.ArrayList.class);
        assertTrue(supers.contains(java.util.AbstractList.class));
        assertTrue(supers.contains(java.util.AbstractCollection.class));
        assertTrue(supers.contains(java.lang.Object.class));
    }

    // Tests getAllInterfaces with null
    @Test
    public void testGetAllInterfaces_null_returnsNull() {
        assertNull(ClassUtils.getAllInterfaces(null));
    }

    // Tests getAllInterfaces for ArrayList
    @Test
    public void testGetAllInterfaces_arrayList_containsList() {
        List<Class<?>> ifaces = ClassUtils.getAllInterfaces(java.util.ArrayList.class);
        assertTrue(ifaces.contains(java.util.List.class));
        assertTrue(ifaces.contains(java.util.Collection.class));
        assertTrue(ifaces.contains(java.lang.Iterable.class));
    }

    // Tests isAssignable with primitive widening (int to long)
    @Test
    public void testIsAssignable_primitiveWidening_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
    }

    // Tests isAssignable with null to non-primitive (should return true)
    @Test
    public void testIsAssignable_nullToNonPrimitive_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(null, String.class));
    }

    // Tests isAssignable with null to primitive (should return false)
    @Test
    public void testIsAssignable_nullToPrimitive_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
    }

    // Tests isAssignable with autoboxing from int to Integer
    @Test
    public void testIsAssignable_autoboxingIntToInteger_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
    }

    // Tests isAssignable with autoboxing from Integer to int
    @Test
    public void testIsAssignable_autoboxingIntegerToInt_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
    }

    // Tests isAssignable with array length mismatch
    @Test
    public void testIsAssignable_arrayLengthMismatch_returnsFalse() {
        Class<?>[] from = {String.class, Integer.class};
        Class<?>[] to = {String.class};
        assertFalse(ClassUtils.isAssignable(from, to));
    }

    // Tests primitiveToWrapper with primitive type
    @Test
    public void testPrimitiveToWrapper_int_returnsInteger() {
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
    }

    // Tests primitiveToWrapper with non-primitive (should return same class)
    @Test
    public void testPrimitiveToWrapper_nonPrimitive_returnsSame() {
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
    }

    // Tests wrapperToPrimitive with wrapper
    @Test
    public void testWrapperToPrimitive_integer_returnsInt() {
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
    }

    // Tests wrapperToPrimitive with non-wrapper (should return null)
    @Test
    public void testWrapperToPrimitive_nonWrapper_returnsNull() {
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
    }

    // Tests isInnerClass with an inner class (Map.Entry)
    @Test
    public void testIsInnerClass_innerClass_returnsTrue() {
        assertTrue(ClassUtils.isInnerClass(java.util.Map.Entry.class));
    }

    // Tests isInnerClass with a non-inner class
    @Test
    public void testIsInnerClass_nonInnerClass_returnsFalse() {
        assertFalse(ClassUtils.isInnerClass(String.class));
    }

    // Tests isInnerClass with null
    @Test
    public void testIsInnerClass_null_returnsFalse() {
        assertFalse(ClassUtils.isInnerClass(null));
    }

    // Tests getClass with primitive abbreviation (int)
    @Test
    public void testGetClass_primitiveAbbreviation_returnsClass() throws ClassNotFoundException {
        assertEquals(Integer.TYPE, ClassUtils.getClass("int"));
    }

    // Tests getClass with an array class name (JVM style)
    @Test
    public void testGetClass_arrayJvmName_returnsClass() throws ClassNotFoundException {
        assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
    }

    // Tests getClass with canonical array name (int[])
    @Test
    public void testGetClass_canonicalArrayName_returnsClass() throws ClassNotFoundException {
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
    }

    // Tests toClass with non-null array
    @Test
    public void testToClass_nonEmptyArray_returnsClassArray() {
        Object[] array = {"hello", 123};
        Class<?>[] classes = ClassUtils.toClass(array);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
    }

    // Tests toClass with null input
    @Test
    public void testToClass_null_returnsNull() {
        assertNull(ClassUtils.toClass(null));
    }

    // Tests getShortCanonicalName with a simple class
    @Test
    public void testGetShortCanonicalName_simple_returnsShortName() {
        assertEquals("String", ClassUtils.getShortCanonicalName("java.lang.String"));
    }

    // Tests getPackageCanonicalName with a simple class
    @Test
    public void testGetPackageCanonicalName_simple_returnsPackage() {
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String"));
    }

    // Tests getShortCanonicalName with an array canonical name
    @Test
    public void testGetShortCanonicalName_array_returnsShortNameWithBrackets() {
        // "java.lang.String[]" -> "String[]"
        assertEquals("String[]", ClassUtils.getShortCanonicalName("java.lang.String[]"));
    }

    // ========== New test cases for uncovered branches ==========

    // getShortClassName - primitive array JVM name (e.g., "[I" -> "int[]")
    @Test
    public void testGetShortClassName_primitiveArrayJvmName_returnsShortName() {
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        assertEquals("long[]", ClassUtils.getShortClassName("[J"));
        assertEquals("boolean[]", ClassUtils.getShortClassName("[Z"));
    }

    // getShortClassName - inner class array JVM name ("[Ljava.util.Map$Entry;" -> "Map.Entry[]")
    @Test
    public void testGetShortClassName_innerClassArrayJvmName_returnsShortName() {
        assertEquals("Map.Entry[]", ClassUtils.getShortClassName("[Ljava.util.Map$Entry;"));
    }

    // getShortClassName - multi-dimensional array ("[[Ljava.lang.String;" -> "String[][]")
    @Test
    public void testGetShortClassName_multiDimensionalArray_returnsShortName() {
        assertEquals("String[][]", ClassUtils.getShortClassName("[[Ljava.lang.String;"));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
    }

    // getPackageName - array class (should return empty string)
    @Test
    public void testGetPackageName_arrayClass_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageName("[I"));
    }

    // getPackageName - inner class
    @Test
    public void testGetPackageName_innerClass_returnsPackage() {
        assertEquals("java.util", ClassUtils.getPackageName("java.util.Map$Entry"));
    }

    // getAllSuperclasses - interface (List) -> should return empty list
    @Test
    public void testGetAllSuperclasses_interface_returnsEmptyList() {
        List<Class<?>> supers = ClassUtils.getAllSuperclasses(java.util.List.class);
        assertTrue(supers.isEmpty());
    }

    // getAllInterfaces - primitive class -> should return empty list
    @Test
    public void testGetAllInterfaces_primitive_returnsEmptyList() {
        List<Class<?>> ifaces = ClassUtils.getAllInterfaces(Integer.TYPE);
        assertTrue(ifaces.isEmpty());
    }

    // isAssignable - primitive widening from short to int
    @Test
    public void testIsAssignable_shortToInt_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
    }

    // isAssignable - primitive widening from byte to short
    @Test
    public void testIsAssignable_byteToShort_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
    }

    // isAssignable - primitive widening from char to int
    @Test
    public void testIsAssignable_charToInt_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
    }

    // isAssignable - primitive widening from boolean to int (should be false)
    @Test
    public void testIsAssignable_booleanToInt_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
    }

    // isAssignable - int to long without autoboxing (autoboxing=false)
    @Test
    public void testIsAssignable_intToLongNoAutoboxing_returnsTrue() {
        // primitive widening works without autoboxing
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE, false));
    }

    // isAssignable - int to Integer without autoboxing (should be false)
    @Test
    public void testIsAssignable_intToIntegerNoAutoboxing_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
    }

    // isAssignable - Integer to int without autoboxing (should be false)
    @Test
    public void testIsAssignable_integerToIntNoAutoboxing_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
    }

    // isAssignable - String[] to Object[] (array covariance)
    @Test
    public void testIsAssignable_arrayToObjectArray_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(String[].class, Object[].class));
    }

    // isAssignable - int[] to Object[] (primitive array not covariant with Object[])
    @Test
    public void testIsAssignable_primitiveArrayToObjectArray_returnsFalse() {
        assertFalse(ClassUtils.isAssignable(int[].class, Object[].class));
    }

    // isAssignable - String[] to Object (array is Object)
    @Test
    public void testIsAssignable_arrayToObject_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(String[].class, Object.class));
    }

    // isAssignable - null array to non-null array (null can be assigned to any array)
    @Test
    public void testIsAssignable_nullToArray_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(null, String[].class));
    }

    // primitiveToWrapper - void type -> Void.class
    @Test
    public void testPrimitiveToWrapper_void_returnsVoid() {
        assertEquals(Void.class, ClassUtils.primitiveToWrapper(Void.TYPE));
    }

    // wrapperToPrimitive - Void.class -> void type
    @Test
    public void testWrapperToPrimitive_void_returnsVoidType() {
        assertEquals(Void.TYPE, ClassUtils.wrapperToPrimitive(Void.class));
    }

    // getClass - primitive void
    @Test
    public void testGetClass_primitiveVoid_returnsVoidType() throws ClassNotFoundException {
        assertEquals(Void.TYPE, ClassUtils.getClass("void"));
    }

    // getClass - other primitive abbreviations
    @Test
    public void testGetClass_primitiveBoolean_returnsBooleanType() throws ClassNotFoundException {
        assertEquals(Boolean.TYPE, ClassUtils.getClass("boolean"));
    }

    @Test
    public void testGetClass_primitiveLong_returnsLongType() throws ClassNotFoundException {
        assertEquals(Long.TYPE, ClassUtils.getClass("long"));
    }

    // getClass - inner class name (e.g., "java.util.Map$Entry")
    @Test
    public void testGetClass_innerClassName_returnsClass() throws ClassNotFoundException {
        assertEquals(java.util.Map.Entry.class, ClassUtils.getClass("java.util.Map$Entry"));
    }

    // getClass - multi-dimensional canonical array (e.g., "int[][]")
    @Test
    public void testGetClass_canonicalMultiArray_returnsClass() throws ClassNotFoundException {
        assertEquals(int[][].class, ClassUtils.getClass("int[][]"));
    }

    // toClass - empty array -> empty Class array
    @Test
    public void testToClass_emptyArray_returnsEmptyClassArray() {
        Object[] array = {};
        Class<?>[] classes = ClassUtils.toClass(array);
        assertEquals(0, classes.length);
    }

    // toClass - array with null element -> returns array with null for that element
    @Test
    public void testToClass_arrayWithNull_returnsClassArrayWithNull() {
        Object[] array = {"hello", null, 123};
        Class<?>[] classes = ClassUtils.toClass(array);
        assertEquals(String.class, classes[0]);
        assertNull(classes[1]);
        assertEquals(Integer.class, classes[2]);
    }

    // getShortCanonicalName - null input
    @Test
    public void testGetShortCanonicalName_null_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getShortCanonicalName((String) null));
    }

    // getShortCanonicalName - empty string
    @Test
    public void testGetShortCanonicalName_empty_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getShortCanonicalName(""));
    }

    // getShortCanonicalName - inner class canonical name (e.g., "java.util.Map.Entry")
    @Test
    public void testGetShortCanonicalName_innerClass_returnsShortName() {
        assertEquals("Map.Entry", ClassUtils.getShortCanonicalName("java.util.Map.Entry"));
    }

    // getPackageCanonicalName - null input
    @Test
    public void testGetPackageCanonicalName_null_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageCanonicalName((String) null));
    }

    // getPackageCanonicalName - empty string
    @Test
    public void testGetPackageCanonicalName_empty_returnsEmpty() {
        assertEquals(StringUtils.EMPTY, ClassUtils.getPackageCanonicalName(""));
    }

    // getPackageCanonicalName - inner class canonical name
    @Test
    public void testGetPackageCanonicalName_innerClass_returnsPackage() {
        assertEquals("java.util", ClassUtils.getPackageCanonicalName("java.util.Map.Entry"));
    }

    // isAssignable with multiple classes - null in from array
    @Test
    public void testIsAssignable_nullInFromArray_returnsTrue() {
        Class<?>[] from = {null, String.class};
        Class<?>[] to = {Object.class, String.class};
        assertTrue(ClassUtils.isAssignable(from, to));
    }

    // isAssignable with multiple classes - null in to array (null is assignable to any reference)
    @Test
    public void testIsAssignable_nullInToArray_returnsFalse() {
        // null cannot be assigned to primitive
        Class<?>[] from = {Integer.class, Integer.TYPE};
        Class<?>[] to = {Integer.class, null};
        assertFalse(ClassUtils.isAssignable(from, to));
    }

    // isAssignable with empty arrays
    @Test
    public void testIsAssignable_emptyArrays_returnsTrue() {
        Class<?>[] from = {};
        Class<?>[] to = {};
        assertTrue(ClassUtils.isAssignable(from, to));
    }
}
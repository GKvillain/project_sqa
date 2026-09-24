package org.apache.commons.lang3;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ClassUtils}.
 */
public class ClassUtilsTest {

    // Tests instantiation of public constructor
    @Test
    public void testConstructor_instantiation_createsInstance() {
        assertNotNull(new ClassUtils());
    }

    // Tests toClass with array containing null element (Defects4J Lang-33 regression)
    @Test
    public void testToClass_arrayWithNullElement_returnsArrayWithNull() {
        Object[] array = new Object[] { "Hello", null, Integer.valueOf(1) };
        Class<?>[] result = ClassUtils.toClass(array);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(String.class, result[0]);
        assertNull(result[1]);
        assertEquals(Integer.class, result[2]);
    }

    // Tests toClass with null input and empty input
    @Test
    public void testToClass_nullAndEmptyInput_returnsNullAndEmpty() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(new Object[0]));
    }

    // Tests getShortClassName for Class, Object and String including arrays and inner classes
    @Test
    public void testGetShortClassName_variousInputs_returnsShortNames() {
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("null", ClassUtils.getShortClassName((Object) null, "null"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "null"));
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName(InnerClass.class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
    }

    // Tests getPackageName for Class, Object and String including array forms
    @Test
    public void testGetPackageName_variousInputs_returnsPackageNames() {
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
        assertEquals("java.lang", ClassUtils.getPackageName("hello", "default"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(ClassUtils.class));
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageName("UnpackagedClass"));
    }

    // Tests getShortCanonicalName and getPackageCanonicalName
    @Test
    public void testGetCanonicalNames_variousInputs_returnsCanonicalForms() {
        assertNull(ClassUtils.getShortCanonicalName((Object) null, null));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertNull(ClassUtils.getPackageCanonicalName((Object) null, null));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
    }

    // Tests getAllSuperclasses with inheritance hierarchy and null
    @Test
    public void testGetAllSuperclasses_classHierarchy_returnsSuperclassList() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertTrue(superclasses.contains(java.util.AbstractList.class));
        assertTrue(superclasses.contains(java.util.AbstractCollection.class));
        assertTrue(superclasses.contains(Object.class));
        assertFalse(superclasses.contains(ArrayList.class));
    }

    // Tests getAllInterfaces with inheritance hierarchy and null
    @Test
    public void testGetAllInterfaces_classHierarchy_returnsInterfacesList() {
        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(Cloneable.class));
        assertTrue(interfaces.contains(java.io.Serializable.class));
    }

    // Tests convertClassNamesToClasses and convertClassesToClassNames
    @Test
    public void testConvertClassNamesAndClasses_validAndInvalidEntries_returnsConvertedList() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
        assertNull(ClassUtils.convertClassesToClassNames(null));

        List<String> names = Arrays.asList("java.lang.String", "non.existent.ClassName", null);
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertNull(classes.get(2));

        List<Class<?>> classList = Arrays.<Class<?>>asList(String.class, null, Integer.class);
        List<String> convertedNames = ClassUtils.convertClassesToClassNames(classList);
        assertEquals(3, convertedNames.size());
        assertEquals("java.lang.String", convertedNames.get(0));
        assertNull(convertedNames.get(1));
        assertEquals("java.lang.Integer", convertedNames.get(2));
    }

    // Tests primitiveToWrapper and primitivesToWrappers
    @Test
    public void testPrimitiveToWrapper_primitivesAndWrappers_returnsWrapperTypes() {
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));

        assertNull(ClassUtils.primitivesToWrappers(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));

        Class<?>[] primitives = new Class<?>[] { Integer.TYPE, Boolean.TYPE, String.class, null };
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        assertEquals(Integer.class, wrappers[0]);
        assertEquals(Boolean.class, wrappers[1]);
        assertEquals(String.class, wrappers[2]);
        assertNull(wrappers[3]);
    }

    // Tests wrapperToPrimitive and wrappersToPrimitives
    @Test
    public void testWrapperToPrimitive_wrappersAndPrimitives_returnsPrimitiveTypes() {
        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.TYPE));

        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));

        Class<?>[] wrappers = new Class<?>[] { Integer.class, Double.class, String.class, null };
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        assertEquals(Integer.TYPE, primitives[0]);
        assertEquals(Double.TYPE, primitives[1]);
        assertNull(primitives[2]);
        assertNull(primitives[3]);
    }

    // Tests isAssignable with widening conversions for primitives
    @Test
    public void testIsAssignable_primitiveWidening_returnsTrue() {
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));

        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));

        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));

        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));

        assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));

        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));

        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
    }

    // Tests isAssignable with autoboxing true and false
    @Test
    public void testIsAssignable_autoboxing_returnsCorrectResult() {
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
    }

    // Tests isAssignable with null checks and class arrays
    @Test
    public void testIsAssignable_nullInputsAndClassArrays_returnsExpected() {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertTrue(ClassUtils.isAssignable((Class<?>) null, Object.class));
        assertFalse(ClassUtils.isAssignable((Class<?>) null, Integer.TYPE));

        Class<?>[] from = new Class<?>[] { String.class, Integer.class };
        Class<?>[] to = new Class<?>[] { Object.class, Number.class };
        assertTrue(ClassUtils.isAssignable(from, to, false));

        Class<?>[] mismatchLen = new Class<?>[] { String.class };
        assertFalse(ClassUtils.isAssignable(from, mismatchLen, false));

        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
    }

    // Tests isInnerClass for inner, top-level classes and null
    @Test
    public void testIsInnerClass_variousClasses_returnsExpected() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(InnerClass.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
    }

    // Tests getClass with different ClassLoader overloads and primitive/array names
    @Test
    public void testGetClass_primitiveAndArrayNames_loadsExpectedClass() throws ClassNotFoundException {
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
        assertEquals(String.class, ClassUtils.getClass(ClassLoader.getSystemClassLoader(), "java.lang.String"));
    }

    // Tests getClass throwing ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testGetClass_nonExistentClassName_throwsClassNotFoundException() throws ClassNotFoundException {
        ClassUtils.getClass("non.existent.ClassName");
    }

    // Tests getPublicMethod on public method and interface/superclass implementation
    @Test
    public void testGetPublicMethod_publicMethodAndInterface_returnsMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(ArrayList.class, "size", new Class<?>[0]);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getDeclaringClass().getModifiers()));

        Method unmodifiableMethod = ClassUtils.getPublicMethod(
                Collections.unmodifiableList(new ArrayList<String>()).getClass(), "size", new Class<?>[0]);
        assertNotNull(unmodifiableMethod);
        assertTrue(Modifier.isPublic(unmodifiableMethod.getDeclaringClass().getModifiers()));
    }

    // Tests getPublicMethod throwing NoSuchMethodException
    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethod_nonExistentMethod_throwsNoSuchMethodException() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethodName", new Class<?>[0]);
    }

    // Static nested class used for inner class testing
    private static class InnerClass {
    }
}
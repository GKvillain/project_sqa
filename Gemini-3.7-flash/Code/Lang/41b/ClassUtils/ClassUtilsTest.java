package org.apache.commons.lang;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    // Tests constructor
    @Test
    public void testConstructor() {
        assertNotNull(new ClassUtils());
    }

    // Tests getShortClassName with null and valid Object/Class/String
    @Test
    public void testGetShortClassName_variousInputs() {
        assertEquals("default", ClassUtils.getShortClassName((Object) null, "default"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "default"));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName(InnerClass.class));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName("org.apache.commons.lang.ClassUtilsTest$InnerClass"));
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
    }

    // Tests getPackageName with null and valid Object/Class/String
    @Test
    public void testGetPackageName_variousInputs() {
        assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
        assertEquals("java.lang", ClassUtils.getPackageName("hello", "default"));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(InnerClass.class));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageName("UnpackagedClass"));
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
    }

    // Tests getAllSuperclasses and getAllInterfaces
    @Test
    public void testGetAllSuperclassesAndInterfaces() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertTrue(superclasses.contains(java.util.AbstractList.class));
        assertTrue(superclasses.contains(Object.class));

        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(java.util.List.class));
        assertTrue(interfaces.contains(java.util.Collection.class));
        assertTrue(interfaces.contains(java.lang.Iterable.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
    }

    // Tests convertClassNamesToClasses and convertClassesToClassNames
    @Test
    public void testConvertClassNamesAndClasses() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
        assertNull(ClassUtils.convertClassesToClassNames(null));

        List<String> classNames = new ArrayList<String>();
        classNames.add("java.lang.String");
        classNames.add("non.existing.ClassName");
        classNames.add(null);

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(classNames);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertNull(classes.get(2));

        List<Class<?>> classList = new ArrayList<Class<?>>();
        classList.add(String.class);
        classList.add(null);

        List<String> names = ClassUtils.convertClassesToClassNames(classList);
        assertEquals(2, names.size());
        assertEquals("java.lang.String", names.get(0));
        assertNull(names.get(1));
    }

    // Tests isAssignable between single classes without autoboxing
    @Test
    public void testIsAssignable_singleClass_noAutoboxing() {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));

        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Byte.TYPE));

        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));

        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
    }

    // Tests isAssignable between single classes with autoboxing enabled
    @Test
    public void testIsAssignable_singleClass_withAutoboxing() {
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Number.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Double.TYPE, true));
        assertFalse(ClassUtils.isAssignable(Double.class, Integer.TYPE, true));
    }

    // Tests isAssignable with array of classes
    @Test
    public void testIsAssignable_classArrays() {
        assertFalse(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{String.class, Integer.class}));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{Object.class}));
        assertFalse(ClassUtils.isAssignable(new Class<?>[]{Object.class}, new Class<?>[]{String.class}));

        assertTrue(ClassUtils.isAssignable(new Class<?>[]{Integer.TYPE}, new Class<?>[]{Integer.class}, true));
        assertFalse(ClassUtils.isAssignable(new Class<?>[]{Integer.TYPE}, new Class<?>[]{Integer.class}, false));
    }

    // Tests primitiveToWrapper and wrapperToPrimitive mappings
    @Test
    public void testPrimitiveAndWrapperConversions() {
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));

        assertNull(ClassUtils.primitivesToWrappers(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));
        assertArrayEquals(new Class<?>[]{Integer.class, Boolean.class, String.class},
                ClassUtils.primitivesToWrappers(new Class<?>[]{Integer.TYPE, Boolean.TYPE, String.class}));

        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));

        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        assertArrayEquals(new Class<?>[]{Integer.TYPE, Boolean.TYPE, null},
                ClassUtils.wrappersToPrimitives(new Class<?>[]{Integer.class, Boolean.class, String.class}));
    }

    // Tests isInnerClass detection
    @Test
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(InnerClass.class));
    }

    // Tests getClass loading method variants
    @Test
    public void testGetClass() throws ClassNotFoundException {
        assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
        assertEquals(int[].class, ClassUtils.getClass("[I"));
        assertEquals(String.class, ClassUtils.getClass(getClass().getClassLoader(), "java.lang.String"));
    }

    // Tests getClass with non-existing class throwing ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testGetClass_notFound_throwsException() throws ClassNotFoundException {
        ClassUtils.getClass("non.existing.ClassName");
    }

    // Tests toClass method
    @Test
    public void testToClass() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.toClass(new Object[0]));
        assertArrayEquals(new Class<?>[]{String.class, Integer.class},
                ClassUtils.toClass(new Object[]{"text", 123}));
    }

    // Tests getPublicMethod lookup
    @Test
    public void testGetPublicMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(ArrayList.class, "size", new Class<?>[0]);
        assertNotNull(method);
        assertEquals("size", method.getName());

        Method unmodifiableListSize = ClassUtils.getPublicMethod(
                Collections.unmodifiableList(new ArrayList<Object>()).getClass(), "size", new Class<?>[0]);
        assertNotNull(unmodifiableListSize);
    }

    // Tests getPublicMethod NoSuchMethodException for nonexistent method
    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethod_notFound_throwsException() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethod", new Class<?>[0]);
    }

    // Tests getShortCanonicalName and getPackageCanonicalName
    @Test
    public void testGetCanonicalNames() {
        assertEquals("default", ClassUtils.getShortCanonicalName((Object) null, "default"));
        assertEquals("String", ClassUtils.getShortCanonicalName("hello", "default"));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("String", ClassUtils.getShortCanonicalName(String.class));
        assertEquals("String[]", ClassUtils.getShortCanonicalName(String[].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));

        assertEquals("default", ClassUtils.getPackageCanonicalName((Object) null, "default"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("hello", "default"));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String.class));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
    }

    private static class InnerClass {
    }
}
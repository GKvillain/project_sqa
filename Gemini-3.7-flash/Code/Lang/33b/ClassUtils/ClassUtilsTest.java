package org.apache.commons.lang3;

import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ClassUtilsTest {

    // Tests constructor public access for JavaBean tools
    @Test
    public void testConstructor_default_canBeInstantiated() {
        assertNotNull(new ClassUtils());
        Constructor<?>[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
    }

    // Tests toClass with array containing null elements (Lang-33 defect)
    @Test
    public void testToClass_arrayWithNullElements_returnsArrayWithNull() {
        Object[] array = new Object[] { "Test", null, 123 };
        Class<?>[] classes = ClassUtils.toClass(array);
        assertNotNull(classes);
        assertEquals(3, classes.length);
        assertEquals(String.class, classes[0]);
        assertNull(classes[1]);
        assertEquals(Integer.class, classes[2]);
    }

    // Tests toClass with null and empty array inputs
    @Test
    public void testToClass_nullAndEmptyInput_returnsNullAndEmpty() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(new Object[0]));
    }

    // Tests getShortClassName with various representations
    @Test
    public void testGetShortClassName_variousInputs_returnsExpectedShortName() {
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("default", ClassUtils.getShortClassName((Object) null, "default"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "default"));
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName("org.apache.commons.lang3.ClassUtilsTest$InnerClass"));
    }

    // Tests getPackageName with various representations
    @Test
    public void testGetPackageName_variousInputs_returnsExpectedPackage() {
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
        assertEquals("java.lang", ClassUtils.getPackageName("hello", "default"));
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        assertEquals("", ClassUtils.getPackageName("UnpackagedClass"));
    }

    // Tests getAllSuperclasses and getAllInterfaces hierarchies
    @Test
    public void testGetAllSuperclassesAndInterfaces_validClass_returnsHierarchy() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        assertNull(ClassUtils.getAllInterfaces(null));

        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertTrue(superclasses.contains(java.util.AbstractList.class));
        assertTrue(superclasses.contains(Object.class));

        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(java.util.Collection.class));
        assertTrue(interfaces.contains(Iterable.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(Cloneable.class));
        assertTrue(interfaces.contains(java.io.Serializable.class));
    }

    // Tests convertClassNamesToClasses and convertClassesToClassNames
    @Test
    public void testConvertClassNamesAndClasses_lists_convertsBothWays() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
        assertNull(ClassUtils.convertClassesToClassNames(null));

        List<String> names = new ArrayList<String>();
        names.add("java.lang.String");
        names.add("invalid.ClassName");
        names.add(null);

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertNull(classes.get(2));

        List<String> convertedNames = ClassUtils.convertClassesToClassNames(classes);
        assertEquals(3, convertedNames.size());
        assertEquals("java.lang.String", convertedNames.get(0));
        assertNull(convertedNames.get(1));
        assertNull(convertedNames.get(2));
    }

    // Tests primitiveToWrapper and wrapperToPrimitive conversions
    @Test
    public void testPrimitiveAndWrapperConversions_allTypes_convertsCorrectly() {
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(int.class));
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(boolean.class));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(byte.class));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(char.class));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(short.class));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(long.class));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(float.class));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(double.class));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
        assertNull(ClassUtils.primitiveToWrapper(null));

        assertEquals(int.class, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(boolean.class, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(byte.class, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(char.class, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(short.class, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(long.class, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(float.class, ClassUtils.wrapperToPrimitive(Float.class));
        assertEquals(double.class, ClassUtils.wrapperToPrimitive(Double.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertNull(ClassUtils.wrapperToPrimitive(null));

        assertNull(ClassUtils.primitivesToWrappers(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));
        Class<?>[] prims = new Class<?>[] { int.class, String.class };
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(prims);
        assertEquals(Integer.class, wrappers[0]);
        assertEquals(String.class, wrappers[1]);

        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        Class<?>[] toPrims = ClassUtils.wrappersToPrimitives(wrappers);
        assertEquals(int.class, toPrims[0]);
        assertNull(toPrims[1]);
    }

    // Tests isAssignable single class widening and autoboxing branches
    @Test
    public void testIsAssignable_primitivesAndWidening_returnsExpected() {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertTrue(ClassUtils.isAssignable(null, Object.class));
        assertFalse(ClassUtils.isAssignable(null, int.class));

        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));

        // Widening primitives
        assertTrue(ClassUtils.isAssignable(byte.class, short.class, false));
        assertTrue(ClassUtils.isAssignable(byte.class, int.class, false));
        assertTrue(ClassUtils.isAssignable(byte.class, long.class, false));
        assertTrue(ClassUtils.isAssignable(byte.class, float.class, false));
        assertTrue(ClassUtils.isAssignable(byte.class, double.class, false));
        assertFalse(ClassUtils.isAssignable(byte.class, char.class, false));

        assertTrue(ClassUtils.isAssignable(short.class, int.class, false));
        assertTrue(ClassUtils.isAssignable(char.class, int.class, false));
        assertTrue(ClassUtils.isAssignable(int.class, long.class, false));
        assertTrue(ClassUtils.isAssignable(int.class, float.class, false));
        assertTrue(ClassUtils.isAssignable(int.class, double.class, false));
        assertTrue(ClassUtils.isAssignable(long.class, float.class, false));
        assertTrue(ClassUtils.isAssignable(long.class, double.class, false));
        assertTrue(ClassUtils.isAssignable(float.class, double.class, false));
        assertFalse(ClassUtils.isAssignable(boolean.class, int.class, false));
        assertFalse(ClassUtils.isAssignable(double.class, float.class, false));

        // Autoboxing
        assertTrue(ClassUtils.isAssignable(int.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, int.class, true));
        assertTrue(ClassUtils.isAssignable(int.class, Long.class, true));
        assertFalse(ClassUtils.isAssignable(int.class, Integer.class, false));
    }

    // Tests isAssignable with class arrays
    @Test
    public void testIsAssignable_classArrays_returnsExpected() {
        assertFalse(ClassUtils.isAssignable(new Class<?>[] { String.class }, new Class<?>[] { String.class, Integer.class }));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[] { Integer.class, String.class }, new Class<?>[] { Number.class, Object.class }));
        assertFalse(ClassUtils.isAssignable(new Class<?>[] { Object.class }, new Class<?>[] { String.class }));
    }

    // Tests isInnerClass detection
    @Test
    public void testIsInnerClass_topLevelAndNested_returnsCorrectResult() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
    }

    // Tests getClass loading mechanism with various formats
    @Test
    public void testGetClass_validClassNames_returnsLoadedClasses() throws Exception {
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
        assertEquals(String.class, ClassUtils.getClass(ClassLoader.getSystemClassLoader(), "java.lang.String"));
        assertEquals(String.class, ClassUtils.getClass("java.lang.String", false));
    }

    // Tests getClass throws ClassNotFoundException for missing classes
    @Test(expected = ClassNotFoundException.class)
    public void testGetClass_notFound_throwsClassNotFoundException() throws Exception {
        ClassUtils.getClass("non.existing.ClassName");
    }

    // Tests getPublicMethod lookup for public methods and interfaces
    @Test
    public void testGetPublicMethod_interfaceAndPublicMethods_returnsInvokableMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(ArrayList.class, "size", new Class<?>[0]);
        assertNotNull(method);
        assertEquals("size", method.getName());

        Method unmodifiableSetMethod = ClassUtils.getPublicMethod(
                Collections.unmodifiableSet(Collections.emptySet()).getClass(), "isEmpty", new Class<?>[0]);
        assertNotNull(unmodifiableSetMethod);
        assertTrue(Modifier.isPublic(unmodifiableSetMethod.getDeclaringClass().getModifiers()));
    }

    // Tests getPublicMethod NoSuchMethodException when method does not exist
    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethod_nonExistingMethod_throwsNoSuchMethodException() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistingMethod", new Class<?>[0]);
    }

    // Tests getShortCanonicalName and getPackageCanonicalName methods
    @Test
    public void testGetCanonicalNameVariants_variousInputs_returnsExpectedNames() {
        assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("default", ClassUtils.getShortCanonicalName((Object) null, "default"));
        assertEquals("String", ClassUtils.getShortCanonicalName("hello", "default"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("String[][]", ClassUtils.getShortCanonicalName("[[Ljava.lang.String;"));

        assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertEquals("default", ClassUtils.getPackageCanonicalName((Object) null, "default"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("hello", "default"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String[].class));
    }
}
package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

public class UnsafeAllocatorTest {

    // ========== Existing tests ==========

    // Tests create() returns an instance that can allocate simple class
    @Test
    public void testCreate_returnsUnsafeAllocatorInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        assertNotNull(allocator);
    }

    // Tests newInstance with a concrete class using the default (first) strategy
    @Test
    public void testNewInstance_concreteClass_returnsNewInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Object obj = allocator.newInstance(Object.class);
        assertNotNull(obj);
        assertTrue(obj instanceof Object);
    }

    // Tests newInstance with a simple String class
    @Test
    public void testNewInstance_stringClass_returnsNewInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        String str = allocator.newInstance(String.class);
        assertNotNull(str);
        assertEquals("", str); // String constructor not called, so it's empty mutable
    }

    // Tests newInstance with a class that has no default constructor
    public static class NoDefaultConstructor {
        private int value;
        public NoDefaultConstructor(int value) {
            this.value = value;
        }
        public int getValue() { return value; }
    }

    @Test
    public void testNewInstance_classWithoutDefaultConstructor_returnsInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        NoDefaultConstructor instance = allocator.newInstance(NoDefaultConstructor.class);
        assertNotNull(instance);
        assertTrue(instance instanceof NoDefaultConstructor);
    }

    // Tests newInstance with a final class
    @Test
    public void testNewInstance_finalClass_returnsInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Integer instance = allocator.newInstance(Integer.class);
        assertNotNull(instance);
    }

    // Tests newInstance with an array class (boundary case: should throw Exception)
    @Test(expected = Exception.class)
    public void testNewInstance_arrayClass_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(int[].class);
    }

    // Tests newInstance with a primitive class (edge case: should throw Exception)
    @Test(expected = Exception.class)
    public void testNewInstance_primitiveClass_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(int.class);
    }

    // Tests that multiple calls to create return working instances
    @Test
    public void testCreate_multipleCalls_returnsWorkingAllocators() throws Exception {
        UnsafeAllocator allocator1 = UnsafeAllocator.create();
        UnsafeAllocator allocator2 = UnsafeAllocator.create();
        Object obj1 = allocator1.newInstance(Object.class);
        Object obj2 = allocator2.newInstance(Object.class);
        assertNotNull(obj1);
        assertNotNull(obj2);
        assertNotSame(obj1, obj2);
    }

    // Tests newInstance with a class that has only private constructors
    private static class PrivateConstructorOnly {
        private PrivateConstructorOnly() {}
        public static PrivateConstructorOnly create() { return new PrivateConstructorOnly(); }
    }

    @Test
    public void testNewInstance_privateConstructorClass_returnsInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        PrivateConstructorOnly instance = allocator.newInstance(PrivateConstructorOnly.class);
        assertNotNull(instance);
    }

    // Tests newInstance with a class that has field initialization (regression test for defect)
    public static class InitField {
        public int x = 42;
    }

    @Test
    public void testNewInstance_fieldInitNotCalled_returnsDefault() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        InitField obj = allocator.newInstance(InitField.class);
        assertEquals(0, obj.x); // Constructor not called, so field initializer not executed
    }

    // Tests create() returns different allocator instance each call
    @Test
    public void testCreate_differentInstances() {
        UnsafeAllocator allocator1 = UnsafeAllocator.create();
        UnsafeAllocator allocator2 = UnsafeAllocator.create();
        assertNotSame(allocator1, allocator2);
    }

    // Tests newInstance with null input (should throw NullPointerException or Exception)
    @Test(expected = NullPointerException.class)
    public void testNewInstance_nullClass_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(null);
    }

    // Tests create() does not return null
    @Test
    public void testCreate_notNull() {
        assertNotNull(UnsafeAllocator.create());
    }

    // ========== New tests for uncovered parts ==========

    // Abstract class – should throw an exception
    public static abstract class AbstractTestClass {
        public abstract void doSomething();
    }

    @Test(expected = Exception.class)
    public void testNewInstance_abstractClass_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(AbstractTestClass.class);
    }

    // Interface – should throw an exception
    public interface TestInterface {
        void foo();
    }

    @Test(expected = Exception.class)
    public void testNewInstance_interface_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(TestInterface.class);
    }

    // Enum – should throw an exception
    public enum TestEnum {
        VALUE;
    }

    @Test(expected = Exception.class)
    public void testNewInstance_enum_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(TestEnum.class);
    }

    // Annotation (annotation interface) – should throw an exception
    public @interface TestAnnotation {
        String value() default "";
    }

    @Test(expected = Exception.class)
    public void testNewInstance_annotation_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(TestAnnotation.class);
    }

    // Primitive void class (void.class / Void.TYPE) – should throw an exception
    @Test(expected = Exception.class)
    public void testNewInstance_primitiveVoid_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(void.class);
    }

    // Array of objects (e.g., String[]) – should throw an exception
    @Test(expected = Exception.class)
    public void testNewInstance_arrayOfObjects_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(String[].class);
    }

    // Multi-dimensional array (e.g., int[][]) – should throw an exception
    @Test(expected = Exception.class)
    public void testNewInstance_multiDimensionalArray_throwsException() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        allocator.newInstance(int[][].class);
    }

    // Void wrapper class (Void.class) – should succeed (like other final classes)
    @Test
    public void testNewInstance_voidWrapperClass_returnsInstance() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Void instance = allocator.newInstance(Void.class);
        assertNotNull(instance);
    }
}
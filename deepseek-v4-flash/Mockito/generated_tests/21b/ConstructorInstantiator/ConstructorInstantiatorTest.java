package org.mockito.internal.creation.instance;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for ConstructorInstantiator.
 */
public class ConstructorInstantiatorTest {

    // Helper inner classes for testing

    public static class MyClass {
        public MyClass() {}
    }

    public static class NoDefaultConstructor {
        public NoDefaultConstructor(int x) {}
    }

    public static class MyClassWithOuter {
        public MyClassWithOuter(Object outer) {}
    }

    public static class ThrowingDefaultConstructor {
        public ThrowingDefaultConstructor() {
            throw new RuntimeException("fail");
        }
    }

    public static class ThrowingOuterConstructor {
        public ThrowingOuterConstructor(Object o) {
            throw new RuntimeException("fail");
        }
    }

    public static class PrivateOuterConstructor {
        private PrivateOuterConstructor(Object o) {}
    }

    public static abstract class AbstractClass {}

    public static class NonStaticInnerClass {
        // non-static inner class can only be instantiated with an outer instance
    }

    // Tests for null outer class instance (noArgConstructor path)

    @Test
    // Tests that noArgConstructor creates instance successfully when class has default constructor
    public void testNewInstance_nullOuterInstance_returnsInstanceViaNoArgConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        MyClass instance = instantiator.newInstance(MyClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof MyClass);
    }

    @Test
    // Tests that noArgConstructor throws InstantationException when class lacks default constructor
    public void testNewInstance_nullOuterInstance_noDefaultConstructor_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(NoDefaultConstructor.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("parameter-less constructor"));
        }
    }

    @Test
    // Tests that noArgConstructor throws InstantationException when default constructor throws exception
    public void testNewInstance_nullOuterInstance_defaultConstructorThrows_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ThrowingDefaultConstructor.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("parameter-less constructor"));
        }
    }

    @Test
    // Tests that noArgConstructor throws InstantationException when class is abstract
    public void testNewInstance_nullOuterInstance_abstractClass_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(AbstractClass.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("parameter-less constructor"));
        }
    }

    // Tests for non-null outer class instance (withOuterClass path)

    @Test
    // Tests that withOuterClass creates instance successfully when matching constructor exists
    public void testNewInstance_withOuterInstance_returnsInstanceViaOuterConstructor() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        MyClassWithOuter instance = instantiator.newInstance(MyClassWithOuter.class);
        assertNotNull(instance);
        assertTrue(instance instanceof MyClassWithOuter);
    }

    @Test
    // Tests that withOuterClass throws InstantationException when no constructor matches outer type
    public void testNewInstance_withOuterInstance_noMatchingConstructor_throwsInstantationException() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(NoDefaultConstructor.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type") ||
                       e.getMessage().contains("parameter-less constructor"));
        }
    }

    @Test
    // Tests that withOuterClass throws InstantationException when matching constructor throws exception
    public void testNewInstance_withOuterInstance_constructorThrows_throwsInstantationException() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(ThrowingOuterConstructor.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type"));
        }
    }

    @Test
    // Tests that withOuterClass throws InstantationException when matching constructor is private
    public void testNewInstance_withOuterInstance_privateConstructor_throwsInstantationException() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(PrivateOuterConstructor.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type"));
        }
    }

    @Test
    // Tests that withOuterClass throws InstantationException when class is abstract
    public void testNewInstance_withOuterInstance_abstractClass_throwsInstantationException() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(AbstractClass.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type") ||
                       e.getMessage().contains("parameter-less constructor"));
        }
    }

    @Test
    // Tests that withOuterClass correctly uses exact type match (not subtype)
    public void testNewInstance_withOuterInstance_subtypeOfParameter_throwsInstantationException() {
        String outer = "hello"; // String is subtype of CharSequence
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(MyClassWithOuter.class); // MyClassWithOuter expects Object, not String
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type"));
        }
    }

    @Test
    // Tests the branch when outerClassInstance is not null but class has no suitable constructor (cover false branch)
    public void testNewInstance_withOuterInstance_noOuterConstructor_throwsInstantationException() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(MyClass.class); // MyClass has no constructor(Object)
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("outer instance has correct type") ||
                       e.getMessage().contains("parameter-less constructor"));
        }
    }

    @Test
    // Tests that when outerClassInstance is null, the if branch is taken and noArgConstructor is used
    public void testNewInstance_nullOuterInstance_usesIfBranch() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        MyClass instance = instantiator.newInstance(MyClass.class);
        assertNotNull(instance);
    }

    @Test
    // Tests full success path with non-null outer class instance
    public void testNewInstance_withOuterInstance_successPath() {
        Object outer = new Object();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        MyClassWithOuter instance = instantiator.newInstance(MyClassWithOuter.class);
        assertNotNull(instance);
    }
}
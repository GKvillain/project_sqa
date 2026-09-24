package org.mockito.internal.creation.instance;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConstructorInstantiatorTest {

    static class SimpleClass {
        public SimpleClass() {}
    }

    static abstract class AbstractClass {
        public AbstractClass() {}
    }

    static class ThrowingClass {
        public ThrowingClass() {
            throw new RuntimeException("Constructor failure");
        }
    }

    static class OuterClass {
        class InnerClass {
            public InnerClass() {}
        }
    }

    static class SubOuterClass extends OuterClass {}

    static class NoDefaultConstructor {
        public NoDefaultConstructor(String param) {}
    }

    static class PrivateConstructorClass {
        private PrivateConstructorClass() {}
    }

    static class MultipleConstructorsClass {
        public MultipleConstructorsClass() {}
        public MultipleConstructorsClass(String param) {}
    }

    // Tests creating instance with null outer class using no-arg constructor
    @Test
    public void testNewInstance_nullOuterClass_createsInstance() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        SimpleClass instance = instantiator.newInstance(SimpleClass.class);
        assertNotNull(instance);
    }

    // Tests exception thrown when instantiating abstract class with null outer class
    @Test(expected = InstantationException.class)
    public void testNewInstance_nullOuterClassAbstractClass_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(AbstractClass.class);
    }

    // Tests exception thrown when constructor throws exception with null outer class
    @Test(expected = InstantationException.class)
    public void testNewInstance_nullOuterClassThrowingConstructor_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(ThrowingClass.class);
    }

    // Tests creating inner class instance with matching outer class instance
    @Test
    public void testNewInstance_withMatchingOuterClass_createsInstance() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        OuterClass.InnerClass inner = instantiator.newInstance(OuterClass.InnerClass.class);
        assertNotNull(inner);
    }

    // Tests creating inner class instance with outer class subclass
    @Test
    public void testNewInstance_withSubOuterClass_createsInstance() {
        SubOuterClass subOuter = new SubOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(subOuter);
        try {
            OuterClass.InnerClass inner = instantiator.newInstance(OuterClass.InnerClass.class);
            assertNotNull(inner);
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("Unable to create mock instance"));
        }
    }

    // Tests exception thrown when outer class instance is of wrong type
    @Test(expected = InstantationException.class)
    public void testNewInstance_withWrongOuterClassType_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator("invalid outer instance");
        instantiator.newInstance(OuterClass.InnerClass.class);
    }

    // Tests exception thrown when passing outer class instance for class with no-arg constructor
    @Test(expected = InstantationException.class)
    public void testNewInstance_withOuterClassForTopLevelClass_throwsInstantationException() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        instantiator.newInstance(SimpleClass.class);
    }

    // Tests exception message contains class name on failure with null outer class
    @Test
    public void testNewInstance_nullOuterClassFailure_exceptionMessageContainsClassName() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(AbstractClass.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("AbstractClass"));
        }
    }

    // Tests exception message contains class name on failure with outer class
    @Test
    public void testNewInstance_withOuterClassFailure_exceptionMessageContainsClassName() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(new Object());
        try {
            instantiator.newInstance(SimpleClass.class);
            fail("Expected InstantationException");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("SimpleClass"));
        }
    }

    // Tests exception thrown when class has no 0-arg constructor
    @Test(expected = InstantationException.class)
    public void testNewInstance_noDefaultConstructor_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(NoDefaultConstructor.class);
    }

    // Tests creating instance for class with private 0-arg constructor
    @Test
    public void testNewInstance_privateConstructor_createsInstance() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        PrivateConstructorClass instance = instantiator.newInstance(PrivateConstructorClass.class);
        assertNotNull(instance);
    }

    // Tests creating instance for class with multiple constructors
    @Test
    public void testNewInstance_multipleConstructors_createsInstance() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        MultipleConstructorsClass instance = instantiator.newInstance(MultipleConstructorsClass.class);
        assertNotNull(instance);
    }

    // Tests exception thrown when instantiating inner class without outer class instance
    @Test(expected = InstantationException.class)
    public void testNewInstance_innerClassWithoutOuterInstance_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(OuterClass.InnerClass.class);
    }

    // Tests exception thrown when constructor throws exception with outer class instance
    @Test(expected = InstantationException.class)
    public void testNewInstance_outerClassThrowingConstructor_throwsInstantationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(new Object());
        instantiator.newInstance(ThrowingClass.class);
    }
}
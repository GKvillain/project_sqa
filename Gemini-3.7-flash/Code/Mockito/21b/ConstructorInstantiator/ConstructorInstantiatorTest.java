package org.mockito.internal.creation.instance;

import org.junit.Test;
import org.mockito.creation.instance.InstantiationException;
import static org.junit.Assert.*;

public class ConstructorInstantiatorTest {

    static class SimpleClass {
        public SimpleClass() {}
    }

    static class ClassWithoutNoArgConstructor {
        public ClassWithoutNoArgConstructor(String arg) {}
    }

    static abstract class AbstractClass {
        public AbstractClass() {}
    }

    static class ExceptionThrowingClass {
        public ExceptionThrowingClass() {
            throw new RuntimeException("Constructor failed");
        }
    }

    static class OuterClass {
        class InnerClass {
            public InnerClass() {}
        }
    }

    static class ParentOuter {}

    static class ChildOuter extends ParentOuter {}

    static class InnerWithParentParam {
        public InnerWithParentParam(ParentOuter parent) {}
    }

    static class ExceptionThrowingInnerClass {
        public ExceptionThrowingInnerClass(OuterClass outer) {
            throw new RuntimeException("Inner constructor failed");
        }
    }

    static abstract class AbstractInnerClass {
        public AbstractInnerClass(OuterClass outer) {}
    }

    static class MultipleMatchingConstructors {
        public MultipleMatchingConstructors(ParentOuter parent) {}
        public MultipleMatchingConstructors(ChildOuter child) {}
    }

    // Tests creating instance of normal class with no outer class instance (outerClassInstance == null)
    @Test
    public void testNewInstance_nullOuterClass_createsInstanceSuccessfully() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        SimpleClass result = instantiator.newInstance(SimpleClass.class);
        assertNotNull(result);
        assertTrue(result instanceof SimpleClass);
    }

    // Tests exception thrown when target class lacks parameter-less constructor with null outer instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_nullOuterClassWithoutNoArgConstructor_throwsInstantiationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(ClassWithoutNoArgConstructor.class);
    }

    // Tests exception thrown when instantiating abstract class with null outer instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_nullOuterClassAbstractClass_throwsInstantiationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(AbstractClass.class);
    }

    // Tests exception thrown when target class constructor throws an exception with null outer instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_nullOuterClassConstructorThrowsException_throwsInstantiationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        instantiator.newInstance(ExceptionThrowingClass.class);
    }

    // Tests creating inner class instance when valid outer class instance is provided
    @Test
    public void testNewInstance_validOuterClassInstance_createsInnerClassInstanceSuccessfully() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        OuterClass.InnerClass result = instantiator.newInstance(OuterClass.InnerClass.class);
        assertNotNull(result);
        assertTrue(result instanceof OuterClass.InnerClass);
    }

    // Tests exception thrown when outer class instance type does not match inner class requirement
    @Test(expected = InstantiationException.class)
    public void testNewInstance_mismatchedOuterClassInstance_throwsInstantiationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator("invalid outer instance");
        instantiator.newInstance(OuterClass.InnerClass.class);
    }

    // Tests exception thrown when target class does not support outer class constructor
    @Test(expected = InstantiationException.class)
    public void testNewInstance_outerClassProvidedForSimpleClass_throwsInstantiationException() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        instantiator.newInstance(SimpleClass.class);
    }

    // Tests instantiating class when outer instance is a subclass of expected constructor parameter type
    @Test
    public void testNewInstance_outerClassIsSubtypeOfConstructorParameter_createsInstanceSuccessfully() {
        ChildOuter childOuter = new ChildOuter();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(childOuter);
        InnerWithParentParam result = instantiator.newInstance(InnerWithParentParam.class);
        assertNotNull(result);
        assertTrue(result instanceof InnerWithParentParam);
    }

    // Tests constructor with explicit boolean flag (hasOuterClassInstance = false)
    @Test
    public void testNewInstance_booleanConstructor_hasOuterFalse_createsInstanceSuccessfully() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(false, null);
        SimpleClass result = instantiator.newInstance(SimpleClass.class);
        assertNotNull(result);
        assertTrue(result instanceof SimpleClass);
    }

    // Tests constructor with explicit boolean flag (hasOuterClassInstance = true) and null outer instance throws exception
    @Test(expected = InstantiationException.class)
    public void testNewInstance_booleanConstructor_hasOuterTrueWithNullOuter_throwsInstantiationException() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(true, null);
        instantiator.newInstance(OuterClass.InnerClass.class);
    }

    // Tests exception thrown when inner class constructor throws an exception with valid outer instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_innerClassConstructorThrowsException_throwsInstantiationException() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        instantiator.newInstance(ExceptionThrowingInnerClass.class);
    }

    // Tests exception thrown when instantiating abstract inner class with valid outer instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_abstractInnerClass_throwsInstantiationException() {
        OuterClass outer = new OuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        instantiator.newInstance(AbstractInnerClass.class);
    }

    // Tests exception thrown when multiple 1-arg constructors match the outer class instance
    @Test(expected = InstantiationException.class)
    public void testNewInstance_multipleMatchingConstructors_throwsInstantiationException() {
        ChildOuter child = new ChildOuter();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(child);
        instantiator.newInstance(MultipleMatchingConstructors.class);
    }
}
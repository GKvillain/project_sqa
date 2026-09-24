package com.google.gson.internal;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnsafeAllocatorTest {

  private UnsafeAllocator unsafeAllocator;

  static class ConcreteClass {
    int value = 42;
  }

  static class ClassWithThrowingConstructor {
    public ClassWithThrowingConstructor() {
      throw new UnsupportedOperationException("Constructor should not be invoked");
    }
  }

  static class ClassWithPrivateConstructor {
    private final String name;

    private ClassWithPrivateConstructor() {
      this.name = "default";
    }

    public String getName() {
      return name;
    }
  }

  static abstract class AbstractClass {
    int value;
  }

  interface TestInterface {
    void execute();
  }

  @Before
  public void setUp() {
    unsafeAllocator = UnsafeAllocator.create();
  }

  // Tests factory method returns non-null allocator instance
  @Test
  public void testCreate_defaultEnvironment_returnsNonNullAllocator() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    assertNotNull(allocator);
  }

  // Tests instantiating a regular concrete class
  @Test
  public void testNewInstance_concreteClass_allocatesInstanceSuccessfully() throws Exception {
    ConcreteClass instance = unsafeAllocator.newInstance(ConcreteClass.class);
    assertNotNull(instance);
    assertTrue(instance instanceof ConcreteClass);
  }

  // Tests constructor is bypassed when allocating instance
  @Test
  public void testNewInstance_throwingConstructor_allocatesWithoutCallingConstructor() throws Exception {
    ClassWithThrowingConstructor instance = unsafeAllocator.newInstance(ClassWithThrowingConstructor.class);
    assertNotNull(instance);
    assertTrue(instance instanceof ClassWithThrowingConstructor);
  }

  // Tests private constructor class can be instantiated with default field values
  @Test
  public void testNewInstance_privateConstructor_allocatesWithDefaultFieldValues() throws Exception {
    ClassWithPrivateConstructor instance = unsafeAllocator.newInstance(ClassWithPrivateConstructor.class);
    assertNotNull(instance);
    // Field 'name' should be null because constructor was not executed
    assertEquals(null, instance.getName());
  }

  // Tests multiple allocations create distinct object instances
  @Test
  public void testNewInstance_multipleInvocations_returnsDistinctInstances() throws Exception {
    ConcreteClass instance1 = unsafeAllocator.newInstance(ConcreteClass.class);
    ConcreteClass instance2 = unsafeAllocator.newInstance(ConcreteClass.class);
    assertNotNull(instance1);
    assertNotNull(instance2);
    assertNotSame(instance1, instance2);
  }

  // Tests attempting to instantiate an abstract class throws Exception
  @Test(expected = Exception.class)
  public void testNewInstance_abstractClass_throwsException() throws Exception {
    unsafeAllocator.newInstance(AbstractClass.class);
  }

  // Tests attempting to instantiate an interface throws Exception
  @Test(expected = Exception.class)
  public void testNewInstance_interfaceType_throwsException() throws Exception {
    unsafeAllocator.newInstance(TestInterface.class);
  }

  // Tests assertInstantiable succeeds for valid instantiable concrete class
  @Test
  public void testAssertInstantiable_concreteClass_succeeds() {
    UnsafeAllocator.assertInstantiable(ConcreteClass.class);
  }

  // Tests assertInstantiable throws UnsupportedOperationException with informative message for interface
  @Test
  public void testAssertInstantiable_interface_throwsUnsupportedOperationExceptionWithMessage() {
    try {
      UnsafeAllocator.assertInstantiable(TestInterface.class);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      assertTrue(e.getMessage().contains("Interface can't be instantiated!"));
      assertTrue(e.getMessage().contains(TestInterface.class.getName()));
    }
  }

  // Tests assertInstantiable throws UnsupportedOperationException with informative message for abstract class
  @Test
  public void testAssertInstantiable_abstractClass_throwsUnsupportedOperationExceptionWithMessage() {
    try {
      UnsafeAllocator.assertInstantiable(AbstractClass.class);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      assertTrue(e.getMessage().contains("Abstract class can't be instantiated!"));
      assertTrue(e.getMessage().contains(AbstractClass.class.getName()));
    }
  }

  // Tests newInstance throws UnsupportedOperationException with descriptive message when attempting to instantiate an abstract class
  @Test
  public void testNewInstance_abstractClass_throwsUnsupportedOperationExceptionWithMessage() {
    try {
      unsafeAllocator.newInstance(AbstractClass.class);
      fail("Expected UnsupportedOperationException");
    } catch (Exception e) {
      assertTrue(e instanceof UnsupportedOperationException);
      assertTrue(e.getMessage().contains("Abstract class can't be instantiated!"));
    }
  }

  // Tests newInstance throws UnsupportedOperationException with descriptive message when attempting to instantiate an interface
  @Test
  public void testNewInstance_interface_throwsUnsupportedOperationExceptionWithMessage() {
    try {
      unsafeAllocator.newInstance(TestInterface.class);
      fail("Expected UnsupportedOperationException");
    } catch (Exception e) {
      assertTrue(e instanceof UnsupportedOperationException);
      assertTrue(e.getMessage().contains("Interface can't be instantiated!"));
    }
  }
}
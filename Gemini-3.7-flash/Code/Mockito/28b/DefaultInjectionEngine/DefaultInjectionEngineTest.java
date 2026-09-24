package org.mockito.internal.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultInjectionEngineTest {

    private DefaultInjectionEngine injectionEngine;

    @Before
    public void setUp() {
        injectionEngine = new DefaultInjectionEngine();
    }

    // Helper classes for testing injection
    static class ServiceA {}
    static class ServiceB {}

    static class ParentService {}
    static class ChildService extends ParentService {}

    static class TargetWithSingleField {
        ServiceA serviceA;
    }

    static class TargetWithMultipleFields {
        ServiceA serviceA;
        ServiceB serviceB;
    }

    static class TargetWithSameTypeFields {
        ServiceA serviceA1;
        ServiceA serviceA2;
    }

    static class TargetWithHierarchyFields {
        ParentService parentService;
        ChildService childService;
    }

    static class BaseTarget {
        ServiceA baseServiceA;
    }

    static class SubTarget extends BaseTarget {
        ServiceB subServiceB;
    }

    abstract static class AbstractTarget {
        ServiceA serviceA;
    }

    static class TestClassWithTarget {
        TargetWithSingleField target = new TargetWithSingleField();
    }

    static class TestClassWithUninitializedTarget {
        TargetWithSingleField target;
    }

    static class TestClassWithMultipleFieldsTarget {
        TargetWithMultipleFields target = new TargetWithMultipleFields();
    }

    static class TestClassWithSameTypeFieldsTarget {
        TargetWithSameTypeFields target = new TargetWithSameTypeFields();
    }

    static class TestClassWithHierarchyFieldsTarget {
        TargetWithHierarchyFields target = new TargetWithHierarchyFields();
    }

    static class TestClassWithSubTarget {
        SubTarget target = new SubTarget();
    }

    static class TestClassWithAbstractTarget {
        AbstractTarget target;
    }

    // Tests injection of a single mock into a matching field
    @Test
    public void testInjectMocksOnFields_singleMatchingMock_injectsMock() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Field targetField = TestClassWithTarget.class.getDeclaredField("target");

        ServiceA mockServiceA = new ServiceA();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockServiceA);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertSame(mockServiceA, testInstance.target.serviceA);
    }

    // Tests injection of multiple mocks matching different field types
    @Test
    public void testInjectMocksOnFields_multipleMatchingMocks_injectsAllMocks() throws Exception {
        TestClassWithMultipleFieldsTarget testInstance = new TestClassWithMultipleFieldsTarget();
        Field targetField = TestClassWithMultipleFieldsTarget.class.getDeclaredField("target");

        ServiceA mockServiceA = new ServiceA();
        ServiceB mockServiceB = new ServiceB();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockServiceA);
        mocks.add(mockServiceB);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertSame(mockServiceA, testInstance.target.serviceA);
        assertSame(mockServiceB, testInstance.target.serviceB);
    }

    // Tests initialization and injection when target field is initially null
    @Test
    public void testInjectMocksOnFields_uninitializedTarget_initializesAndInjectsMock() throws Exception {
        TestClassWithUninitializedTarget testInstance = new TestClassWithUninitializedTarget();
        Field targetField = TestClassWithUninitializedTarget.class.getDeclaredField("target");

        ServiceA mockServiceA = new ServiceA();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockServiceA);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNotNull(testInstance.target);
        assertSame(mockServiceA, testInstance.target.serviceA);
    }

    // Tests injection across superclass and subclass hierarchy
    @Test
    public void testInjectMocksOnFields_classHierarchyTarget_injectsSuperAndSubFields() throws Exception {
        TestClassWithSubTarget testInstance = new TestClassWithSubTarget();
        Field targetField = TestClassWithSubTarget.class.getDeclaredField("target");

        ServiceA mockServiceA = new ServiceA();
        ServiceB mockServiceB = new ServiceB();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockServiceA);
        mocks.add(mockServiceB);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertSame(mockServiceA, testInstance.target.baseServiceA);
        assertSame(mockServiceB, testInstance.target.subServiceB);
    }

    // Tests ordered field injection when target has both supertype and subtype fields
    @Test
    public void testInjectMocksOnFields_parentAndChildFields_injectsSubtypeFirst() throws Exception {
        TestClassWithHierarchyFieldsTarget testInstance = new TestClassWithHierarchyFieldsTarget();
        Field targetField = TestClassWithHierarchyFieldsTarget.class.getDeclaredField("target");

        ChildService mockChild = new ChildService();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockChild);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertSame(mockChild, testInstance.target.childService);
        assertNull(testInstance.target.parentService);
    }

    // Tests injection when target has multiple fields of the same type
    @Test
    public void testInjectMocksOnFields_sameTypeFields_sortsAndInjects() throws Exception {
        TestClassWithSameTypeFieldsTarget testInstance = new TestClassWithSameTypeFieldsTarget();
        Field targetField = TestClassWithSameTypeFieldsTarget.class.getDeclaredField("target");

        ServiceA mockServiceA = new ServiceA();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockServiceA);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        boolean injectedInA1 = testInstance.target.serviceA1 != null;
        boolean injectedInA2 = testInstance.target.serviceA2 != null;
        assertEquals(true, injectedInA1 ^ injectedInA2);
    }

    // Tests empty injectMocksFields set does nothing
    @Test
    public void testInjectMocksOnFields_emptyInjectMocksFields_doesNothing() {
        Set<Field> emptyInjectMocksFields = Collections.emptySet();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(new ServiceA());

        injectionEngine.injectMocksOnFields(emptyInjectMocksFields, mocks, new Object());
    }

    // Tests empty mocks set does not inject anything
    @Test
    public void testInjectMocksOnFields_emptyMocksSet_targetFieldRemainsNull() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Field targetField = TestClassWithTarget.class.getDeclaredField("target");

        Set<Object> emptyMocks = Collections.emptySet();
        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, emptyMocks, testInstance);

        assertNull(testInstance.target.serviceA);
    }

    // Tests exception handling when target instance cannot be instantiated
    @Test(expected = MockitoException.class)
    public void testInjectMocksOnFields_uninstantiableTarget_throwsMockitoException() throws Exception {
        TestClassWithAbstractTarget testInstance = new TestClassWithAbstractTarget();
        Field targetField = TestClassWithAbstractTarget.class.getDeclaredField("target");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add(new ServiceA());

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(targetField);

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);
    }
}
package org.mockito.internal.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class DefaultInjectionEngineTest {

    private DefaultInjectionEngine injectionEngine;

    @Before
    public void setUp() {
        injectionEngine = new DefaultInjectionEngine();
    }

    // Tests normal case: injecting a matching mock into an initialized field
    @Test
    public void testInjectMocksOnFields_matchingMock_injectsSuccessfully() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("injectedString"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNotNull(testInstance.target);
        assertEquals("injectedString", testInstance.target.message);
    }

    // Tests normal case: multiple mocks injected into fields of matching types
    @Test
    public void testInjectMocksOnFields_multipleMocks_injectsMatchingTypes() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>();
        mocks.add("hello");
        mocks.add(Integer.valueOf(100));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertEquals("hello", testInstance.target.message);
        assertEquals(Integer.valueOf(100), testInstance.target.number);
    }

    // Tests edge case: empty injectMocksFields set does nothing
    @Test
    public void testInjectMocksOnFields_emptyInjectMocksFields_doesNothing() {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Set<Field> emptyFields = Collections.emptySet();
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("value"));

        injectionEngine.injectMocksOnFields(emptyFields, mocks, testInstance);

        assertNull(testInstance.target.message);
    }

    // Tests edge case: empty mocks set leaves fields as null
    @Test
    public void testInjectMocksOnFields_emptyMocks_leavesFieldsNull() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithTarget.class, "target"));
        Set<Object> emptyMocks = Collections.emptySet();

        injectionEngine.injectMocksOnFields(injectMocksFields, emptyMocks, testInstance);

        assertNull(testInstance.target.message);
        assertNull(testInstance.target.number);
    }

    // Tests normal case: uninitialized target field is instantiated and injected
    @Test
    public void testInjectMocksOnFields_uninitializedField_initializesAndInjects() throws Exception {
        TestClassWithUninitializedTarget testInstance = new TestClassWithUninitializedTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithUninitializedTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("initializedAndInjected"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNotNull(testInstance.target);
        assertEquals("initializedAndInjected", testInstance.target.message);
    }

    // Tests branch coverage: class hierarchy with fields in both superclass and subclass
    @Test
    public void testInjectMocksOnFields_classHierarchy_injectsSuperAndSubclassFields() throws Exception {
        TestClassWithSubTarget testInstance = new TestClassWithSubTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithSubTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>();
        mocks.add("childValue");
        mocks.add(Integer.valueOf(200));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertEquals("childValue", testInstance.target.childField);
        assertEquals(Integer.valueOf(200), testInstance.target.parentField);
    }

    // Tests regression/branch case: fields with type hierarchy (subtype before supertype ordering)
    @Test
    public void testInjectMocksOnFields_typeHierarchyFields_ordersSubtypesFirst() throws Exception {
        TestClassWithHierarchyTypes testInstance = new TestClassWithHierarchyTypes();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithHierarchyTypes.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("stringValue"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertEquals("stringValue", testInstance.target.specificString);
        assertNull(testInstance.target.generalObject);
    }

    // Tests branch case: multiple fields of the same type in the target class
    @Test
    public void testInjectMocksOnFields_sameTypeFields_injectsWithoutComparatorError() throws Exception {
        TestClassWithSameTypeFields testInstance = new TestClassWithSameTypeFields();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithSameTypeFields.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("onlyOneMock"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertTrue(testInstance.target.first != null || testInstance.target.second != null);
    }

    // Tests edge case: mock type does not match any field type
    @Test
    public void testInjectMocksOnFields_noMatchingTypeMock_leavesFieldNull() throws Exception {
        TestClassWithTarget testInstance = new TestClassWithTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList(Double.valueOf(3.14)));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNull(testInstance.target.message);
        assertNull(testInstance.target.number);
    }

    // Tests exception path: target field is an interface/abstract class that cannot be instantiated
    @Test(expected = MockitoException.class)
    public void testInjectMocksOnFields_abstractFieldCannotInitialize_throwsMockitoException() throws Exception {
        TestClassWithAbstractTarget testInstance = new TestClassWithAbstractTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithAbstractTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("someValue"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);
    }

    // Tests normal case: multiple injectMocks target fields injected in a single execution
    @Test
    public void testInjectMocksOnFields_multipleTargetFields_injectsBoth() throws Exception {
        TestClassWithMultipleTargets testInstance = new TestClassWithMultipleTargets();
        Set<Field> injectMocksFields = new HashSet<Field>(Arrays.asList(
                getField(TestClassWithMultipleTargets.class, "target1"),
                getField(TestClassWithMultipleTargets.class, "target2")
        ));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("sharedMock"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNotNull(testInstance.target1);
        assertNotNull(testInstance.target2);
        assertEquals("sharedMock", testInstance.target1.message);
        assertEquals("sharedMock", testInstance.target2.message);
    }

    // Tests branch coverage: target class with static and final fields ignores non-injectable fields
    @Test
    public void testInjectMocksOnFields_targetWithStaticAndFinalFields_onlyInjectsInstanceField() throws Exception {
        TestClassWithStaticAndFinalTarget testInstance = new TestClassWithStaticAndFinalTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithStaticAndFinalTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("injectedValue"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertEquals("injectedValue", testInstance.target.normalField);
        assertEquals("constantFinal", testInstance.target.finalField);
        assertEquals("constantStatic", StaticAndFinalTarget.staticField);
    }

    // Tests constructor injection: uninitialized target with parameter constructor is instantiated with mock
    @Test
    public void testInjectMocksOnFields_uninitializedTargetWithConstructor_injectsViaConstructor() throws Exception {
        TestClassWithConstructorTarget testInstance = new TestClassWithConstructorTarget();
        Set<Field> injectMocksFields = Collections.singleton(getField(TestClassWithConstructorTarget.class, "target"));
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("constructorValue"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        assertNotNull(testInstance.target);
        assertEquals("constructorValue", testInstance.target.message);
    }

    private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static class SimpleTarget {
        public String message;
        public Integer number;
    }

    public static class SuperTarget {
        public Integer parentField;
    }

    public static class SubTarget extends SuperTarget {
        public String childField;
    }

    public static class HierarchyTypeTarget {
        public Object generalObject;
        public String specificString;
    }

    public static class SameTypeFieldsTarget {
        public String first;
        public String second;
    }

    public static abstract class AbstractTarget {
        public String field;
    }

    public static class StaticAndFinalTarget {
        public static String staticField = "constantStatic";
        public final String finalField = "constantFinal";
        public String normalField;
    }

    public static class ConstructorTarget {
        public String message;

        public ConstructorTarget(String message) {
            this.message = message;
        }
    }

    public static class TestClassWithTarget {
        public SimpleTarget target = new SimpleTarget();
    }

    public static class TestClassWithUninitializedTarget {
        public SimpleTarget target;
    }

    public static class TestClassWithSubTarget {
        public SubTarget target = new SubTarget();
    }

    public static class TestClassWithHierarchyTypes {
        public HierarchyTypeTarget target = new HierarchyTypeTarget();
    }

    public static class TestClassWithSameTypeFields {
        public SameTypeFieldsTarget target = new SameTypeFieldsTarget();
    }

    public static class TestClassWithAbstractTarget {
        public AbstractTarget target;
    }

    public static class TestClassWithMultipleTargets {
        public SimpleTarget target1 = new SimpleTarget();
        public SimpleTarget target2 = new SimpleTarget();
    }

    public static class TestClassWithStaticAndFinalTarget {
        public StaticAndFinalTarget target = new StaticAndFinalTarget();
    }

    public static class TestClassWithConstructorTarget {
        public ConstructorTarget target;
    }
}
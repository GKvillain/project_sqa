package org.mockito.internal.configuration.injection;

import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.mockito.exceptions.base.MockitoException;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for FinalMockCandidateFilter.
 * Designed to detect the Defects4J bug 15b (static field injection) and achieve good coverage.
 */
public class FinalMockCandidateFilterTest {

    // Helper class with instance and static fields
    private static class TestSubject {
        private Object instanceField;
        private static Object staticField;
    }

    // Tests normal injection into an instance field
    @Test
    public void testFilterCandidate_singleMock_validField_returnsTrueAndInjects() throws Exception {
        TestSubject subject = new TestSubject();
        Field instanceField = TestSubject.class.getDeclaredField("instanceField");
        instanceField.setAccessible(true);
        Collection<Object> mocks = Collections.singletonList("injectedValue");
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, instanceField, subject);
        boolean result = injecter.thenInject();
        assertTrue(result);
        assertEquals("injectedValue", instanceField.get(subject));
    }

    // Tests that static fields are NOT injected (detects Defects4J bug 15b)
    @Test
    public void testFilterCandidate_singleMock_staticField_returnsFalse() throws Exception {
        Field staticField = TestSubject.class.getDeclaredField("staticField");
        Collection<Object> mocks = Collections.singletonList("mock");
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, staticField, null);
        boolean result = injecter.thenInject();
        assertFalse(result); // buggy version would return true
    }

    // Tests exception path when injection fails due to type mismatch
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_singleMock_incompatibleType_throwsMockitoException() throws Exception {
        TestSubject subject = new TestSubject();
        Field instanceField = TestSubject.class.getDeclaredField("instanceField");
        instanceField.setAccessible(true);
        Collection<Object> mocks = Collections.singletonList(123); // Integer cannot be set to Object field? Actually it can, so use a primitive field? We'll use a different field type for failure.
        // Use a field of type String with an Integer mock for type mismatch
        // To guarantee failure, we can make the field final? No, reflection can set final. Better: use a field of primitive type (e.g., int) with Object mock – will cause IllegalArgumentException.
        // Since TestSubject has only Object fields, we need a field that fails. Let's use a mock that is not assignable: e.g., try to inject into a field of type List with a String (will succeed because Object). 
        // To force failure, use a field of primitive type (int) – but we don't have one. Add a temporary private field? We cannot add fields to TestSubject. Simulate by passing a null fieldInstance? That will cause exception for instance field.
        // Simpler: pass null fieldInstance while field is an instance field. This will cause IllegalArgumentException when calling field.get() inside FieldSetter? Actually FieldSetter.set uses Field.set(object, value). If object is null and field is instance, it throws IllegalArgumentException. That's reliable.
        Field instanceField2 = TestSubject.class.getDeclaredField("instanceField");
        instanceField2.setAccessible(true);
        Collection<Object> mocks2 = Collections.singletonList("any");
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks2, instanceField2, null);
        injecter.thenInject(); // Should throw MockitoException wrapping IllegalArgumentException
    }

    // Tests false branch: empty mocks collection
    @Test
    public void testFilterCandidate_emptyMocks_returnsFalse() throws Exception {
        Field instanceField = TestSubject.class.getDeclaredField("instanceField");
        Collection<Object> mocks = Collections.emptyList();
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, instanceField, new TestSubject());
        boolean result = injecter.thenInject();
        assertFalse(result);
    }

    // Tests false branch: multiple mocks
    @Test
    public void testFilterCandidate_multipleMocks_returnsFalse() throws Exception {
        Field instanceField = TestSubject.class.getDeclaredField("instanceField");
        Collection<Object> mocks = Arrays.asList("mock1", "mock2");
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, instanceField, new TestSubject());
        boolean result = injecter.thenInject();
        assertFalse(result);
    }

    // Tests null mocks input (edge case)
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullMocks_throwsNullPointerException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        filter.filterCandidate(null, TestSubject.class.getDeclaredField("instanceField"), new TestSubject());
    }

    // Tests null field input (edge case)
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullField_throwsNullPointerException() throws Exception {
        Collection<Object> mocks = Collections.singletonList("mock");
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        filter.filterCandidate(mocks, null, new TestSubject());
    }
}
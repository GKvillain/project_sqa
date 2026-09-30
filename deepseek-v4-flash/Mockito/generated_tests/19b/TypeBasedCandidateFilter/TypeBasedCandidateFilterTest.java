package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TypeBasedCandidateFilterTest {

    static class TestClass {
        public String stringField;
        public Number numberField;
        public Object objectField;
        public Integer integerField;
        public int intField;
        public List<String> listField;
    }

    static class MockFilterStub implements MockCandidateFilter {
        Collection<Object> capturedMocks;
        Field capturedField;
        Object capturedInstance;
        int callCount;
        OngoingInjecter returnValue;

        @Override
        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            capturedMocks = mocks;
            capturedField = field;
            capturedInstance = fieldInstance;
            callCount++;
            return returnValue;
        }
    }

    private Field stringField;
    private Field numberField;
    private Field objectField;
    private Field integerField;
    private Field intField;
    private Field listField;
    private TestClass testInstance;

    @Before
    public void setUp() throws Exception {
        stringField = TestClass.class.getDeclaredField("stringField");
        numberField = TestClass.class.getDeclaredField("numberField");
        objectField = TestClass.class.getDeclaredField("objectField");
        integerField = TestClass.class.getDeclaredField("integerField");
        intField = TestClass.class.getDeclaredField("intField");
        listField = TestClass.class.getDeclaredField("listField");
        testInstance = new TestClass();
    }

    @Test
    public void testFilterCandidate_allMocksMatchObjectField_passesAllMocks() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList("a", 1, new Object());
        OngoingInjecter result = filter.filterCandidate(mocks, objectField, testInstance);
        assertNull(result);
        assertEquals(1, next.callCount);
        assertSame(objectField, next.capturedField);
        assertSame(testInstance, next.capturedInstance);
        assertEquals(mocks.size(), next.capturedMocks.size());
        assertTrue(next.capturedMocks.containsAll(mocks));
    }

    @Test
    public void testFilterCandidate_noMocksMatchStringField_passesEmptyList() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList(1, new Object());
        filter.filterCandidate(mocks, stringField, testInstance);
        assertTrue(next.capturedMocks.isEmpty());
        assertEquals(1, next.callCount);
    }

    @Test
    public void testFilterCandidate_someMocksMatchNumberField_passesOnlyMatching() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList("text", 1, 2.5, new Object());
        filter.filterCandidate(mocks, numberField, testInstance);
        assertEquals(2, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(1));
        assertTrue(next.capturedMocks.contains(2.5));
        assertFalse(next.capturedMocks.contains("text"));
    }

    @Test
    public void testFilterCandidate_emptyMocksList_passesEmptyList() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = new ArrayList<>();
        filter.filterCandidate(mocks, objectField, testInstance);
        assertTrue(next.capturedMocks.isEmpty());
        assertEquals(1, next.callCount);
    }

    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_mocksContainingNull_throwsNullPointerException() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList("ok", null, "also ok");
        filter.filterCandidate(mocks, objectField, testInstance);
    }

    @Test
    public void testFilterCandidate_primitiveIntField_noMocksMatch() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList(1, 2.5, new Object());
        filter.filterCandidate(mocks, intField, testInstance);
        assertTrue(next.capturedMocks.isEmpty());
    }

    @Test
    public void testFilterCandidate_supertypeRelationMockNumberForIntegerField_noMatch() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList(2.5, "text");
        filter.filterCandidate(mocks, integerField, testInstance);
        assertTrue(next.capturedMocks.isEmpty());
    }

    @Test
    public void testFilterCandidate_subtypeRelationMockIntegerForNumberField_match() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList(1, "text");
        filter.filterCandidate(mocks, numberField, testInstance);
        assertEquals(1, next.capturedMocks.size());
        assertSame(1, next.capturedMocks.iterator().next());
    }

    @Test
    public void testFilterCandidate_interfaceFieldTypeList_passesMatchingMock() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        List<String> listMock = new ArrayList<String>();
        Collection<Object> mocks = Arrays.asList(listMock, "not a list");
        filter.filterCandidate(mocks, listField, testInstance);
        assertEquals(1, next.capturedMocks.size());
        assertSame(listMock, next.capturedMocks.iterator().next());
    }

    @Test
    public void testFilterCandidate_callsNextWithCorrectFieldAndInstance() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = Arrays.asList("a");
        filter.filterCandidate(mocks, stringField, testInstance);
        assertSame(stringField, next.capturedField);
        assertSame(testInstance, next.capturedInstance);
    }

    @Test
    public void testFilterCandidate_returnsNullWhenNextReturnsNull() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        OngoingInjecter result = filter.filterCandidate(new ArrayList<>(), objectField, testInstance);
        assertNull(result);
    }

    @Test
    public void testFilterCandidate_multipleCalls_incrementsCallCount() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        Collection<Object> mocks = new ArrayList<>();
        filter.filterCandidate(mocks, objectField, testInstance);
        filter.filterCandidate(mocks, objectField, testInstance);
        assertEquals(2, next.callCount);
    }

    // New test cases to increase coverage

    @Test
    public void testFilterCandidate_mocksAllMatchPrimitiveWrapperField_passesAll() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        // Integer field can accept Integer mocks (auto-boxing compatible)
        Collection<Object> mocks = Arrays.asList(10, 20, 30);
        filter.filterCandidate(mocks, integerField, testInstance);
        assertEquals(3, next.capturedMocks.size());
        assertTrue(next.capturedMocks.containsAll(mocks));
    }

    @Test
    public void testFilterCandidate_mocksNullCollection_throwsNullPointerException() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        try {
            filter.filterCandidate(null, objectField, testInstance);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFilterCandidate_mocksWithNullField_throwsNullPointerException() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        try {
            filter.filterCandidate(Arrays.asList("a"), null, testInstance);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFilterCandidate_subtypeChainMatch_abstractClassField() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        // java.lang.Number is abstract; Integer is subtype
        Collection<Object> mocks = Arrays.asList("text", 42);
        filter.filterCandidate(mocks, numberField, testInstance);
        assertEquals(1, next.capturedMocks.size());
        assertSame(42, next.capturedMocks.iterator().next());
    }

    @Test
    public void testFilterCandidate_returnValueFromNext_notNull() throws Exception {
        MockFilterStub next = new MockFilterStub();
        OngoingInjecter mockInjector = new OngoingInjecter() {
            // empty implementation for testing return value
        };
        next.returnValue = mockInjector;
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        OngoingInjecter result = filter.filterCandidate(Arrays.asList("a"), stringField, testInstance);
        assertSame(mockInjector, result);
    }

    @Test
    public void testFilterCandidate_interfaceFieldWithMultipleImplementations() throws Exception {
        MockFilterStub next = new MockFilterStub();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        // List is interface, ArrayList and LinkedList are implementations
        List<String> arrayList = new ArrayList<>();
        List<String> linkedList = new java.util.LinkedList<>();
        Collection<Object> mocks = Arrays.asList(arrayList, linkedList, "not a list");
        filter.filterCandidate(mocks, listField, testInstance);
        assertEquals(2, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(arrayList));
        assertTrue(next.capturedMocks.contains(linkedList));
    }
}
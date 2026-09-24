package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TypeBasedCandidateFilterTest {

    private DummyCandidateFilter nextFilter;
    private TypeBasedCandidateFilter filter;
    private TestSampleTarget sampleTarget;
    private List<Field> allFields;

    private static class TestSampleTarget {
        public String stringField;
        public CharSequence charSequenceField;
        public Number numberField;
        public Integer integerField;
        public List<?> listField;
        public Object objectField;
    }

    private static class DummyCandidateFilter implements MockCandidateFilter {
        private Collection<Object> receivedMocks;
        private Field receivedField;
        private List<Field> receivedAllFields;
        private Object receivedFieldInstance;
        private final OngoingInjecter stubbedOngoingInjecter = new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field candidateFieldToBeInjected, List<Field> allFields, Object injectee) {
            this.receivedMocks = mocks;
            this.receivedField = candidateFieldToBeInjected;
            this.receivedAllFields = allFields;
            this.receivedFieldInstance = injectee;
            return stubbedOngoingInjecter;
        }
    }

    @Before
    public void setUp() {
        nextFilter = new DummyCandidateFilter();
        filter = new TypeBasedCandidateFilter(nextFilter);
        sampleTarget = new TestSampleTarget();
        allFields = Arrays.asList(TestSampleTarget.class.getFields());
    }

    // Tests exact type match gets forwarded to next filter
    @Test
    public void testFilterCandidate_exactTypeMatch_passesMockToNextFilter() throws Exception {
        Field field = TestSampleTarget.class.getField("stringField");
        String mockInstance = "testString";
        List<Object> mocks = Collections.<Object>singletonList(mockInstance);

        OngoingInjecter result = filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertNotNull(result);
        assertEquals(1, nextFilter.receivedMocks.size());
        assertTrue(nextFilter.receivedMocks.contains(mockInstance));
    }

    // Tests non-matching type is filtered out
    @Test
    public void testFilterCandidate_nonMatchingType_filtersOutMock() throws Exception {
        Field field = TestSampleTarget.class.getField("stringField");
        Integer mockInstance = 123;
        List<Object> mocks = Collections.<Object>singletonList(mockInstance);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(0, nextFilter.receivedMocks.size());
    }

    // Tests superclass field type matches subclass mock instance
    @Test
    public void testFilterCandidate_subclassMock_matchesSuperclassField() throws Exception {
        Field field = TestSampleTarget.class.getField("numberField");
        Integer integerMock = 42;
        List<Object> mocks = Collections.<Object>singletonList(integerMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(1, nextFilter.receivedMocks.size());
        assertTrue(nextFilter.receivedMocks.contains(integerMock));
    }

    // Tests interface field type matches implementing class mock instance
    @Test
    public void testFilterCandidate_interfaceImplementation_matchesInterfaceField() throws Exception {
        Field field = TestSampleTarget.class.getField("charSequenceField");
        String stringMock = "hello";
        List<Object> mocks = Collections.<Object>singletonList(stringMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(1, nextFilter.receivedMocks.size());
        assertTrue(nextFilter.receivedMocks.contains(stringMock));
    }

    // Tests Object field type matches any mock instance
    @Test
    public void testFilterCandidate_objectFieldType_matchesAnyMock() throws Exception {
        Field field = TestSampleTarget.class.getField("objectField");
        String stringMock = "anyString";
        Integer intMock = 100;
        List<Object> mocks = Arrays.<Object>asList(stringMock, intMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(2, nextFilter.receivedMocks.size());
        assertTrue(nextFilter.receivedMocks.contains(stringMock));
        assertTrue(nextFilter.receivedMocks.contains(intMock));
    }

    // Tests multiple mocks filtering with only matching types retained
    @Test
    public void testFilterCandidate_multipleMocks_retainsOnlyMatchingTypes() throws Exception {
        Field field = TestSampleTarget.class.getField("numberField");
        Integer intMock = 1;
        Double doubleMock = 2.0;
        String stringMock = "nonMatching";
        List<Object> mocks = Arrays.asList(intMock, stringMock, doubleMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(2, nextFilter.receivedMocks.size());
        assertTrue(nextFilter.receivedMocks.contains(intMock));
        assertTrue(nextFilter.receivedMocks.contains(doubleMock));
    }

    // Tests empty mock collection input results in empty candidate list
    @Test
    public void testFilterCandidate_emptyMocks_passesEmptyListToNextFilter() throws Exception {
        Field field = TestSampleTarget.class.getField("stringField");
        List<Object> mocks = Collections.emptyList();

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(0, nextFilter.receivedMocks.size());
    }

    // Tests no matching candidates among multiple mocks
    @Test
    public void testFilterCandidate_noMatchingMocks_passesEmptyListToNextFilter() throws Exception {
        Field field = TestSampleTarget.class.getField("integerField");
        String stringMock = "test";
        List<?> listMock = new ArrayList<Object>();
        List<Object> mocks = Arrays.<Object>asList(stringMock, listMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertEquals(0, nextFilter.receivedMocks.size());
    }

    // Tests field, allFields, and fieldInstance arguments are correctly forwarded to next filter
    @Test
    public void testFilterCandidate_validInput_forwardsFieldAndInstanceCorrectly() throws Exception {
        Field field = TestSampleTarget.class.getField("listField");
        List<?> listMock = new ArrayList<Object>();
        List<Object> mocks = Collections.<Object>singletonList(listMock);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertSame(field, nextFilter.receivedField);
        assertSame(allFields, nextFilter.receivedAllFields);
        assertSame(sampleTarget, nextFilter.receivedFieldInstance);
    }

    // Tests that return value from next filter is returned as-is
    @Test
    public void testFilterCandidate_nextFilterReturnsOngoingInjecter_returnsSameInstance() throws Exception {
        Field field = TestSampleTarget.class.getField("stringField");
        List<Object> mocks = Collections.<Object>singletonList("dummy");

        OngoingInjecter result = filter.filterCandidate(mocks, field, allFields, sampleTarget);

        assertSame(nextFilter.stubbedOngoingInjecter, result);
    }

    // Tests null element inside mocks collection throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullMockElement_throwsNullPointerException() throws Exception {
        Field field = TestSampleTarget.class.getField("stringField");
        List<Object> mocks = Collections.singletonList(null);

        filter.filterCandidate(mocks, field, allFields, sampleTarget);
    }
}
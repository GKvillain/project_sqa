package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NameBasedCandidateFilterTest {

    private CapturingCandidateFilter nextFilter;
    private NameBasedCandidateFilter filter;
    private SampleTarget targetInstance;
    private Field someField;
    private Field otherField;

    private static class SampleTarget {
        public Object someField;
        public Object otherField;
    }

    private static class CapturingCandidateFilter implements MockCandidateFilter {
        Collection<Object> capturedMocks;
        Field capturedField;
        Object capturedFieldInstance;
        OngoingInjecter injecterToReturn = new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
            this.capturedMocks = mocks;
            this.capturedField = fieldToBeInjected;
            this.capturedFieldInstance = fieldInstance;
            return injecterToReturn;
        }
    }

    @Before
    public void setUp() throws Exception {
        nextFilter = new CapturingCandidateFilter();
        filter = new NameBasedCandidateFilter(nextFilter);
        targetInstance = new SampleTarget();
        someField = SampleTarget.class.getField("someField");
        otherField = SampleTarget.class.getField("otherField");
    }

    // Tests empty mocks collection branch (size <= 1)
    @Test
    public void testFilterCandidate_emptyMocks_passesOriginalMocksToNext() {
        Collection<Object> mocks = Collections.emptyList();

        OngoingInjecter result = filter.filterCandidate(mocks, someField, targetInstance);

        assertSame(mocks, nextFilter.capturedMocks);
        assertSame(nextFilter.injecterToReturn, result);
    }

    // Tests single mock collection branch (size <= 1)
    @Test
    public void testFilterCandidate_singleMock_passesOriginalMocksToNext() {
        Object mock = Mockito.mock(List.class, "someField");
        Collection<Object> mocks = Collections.singletonList(mock);

        OngoingInjecter result = filter.filterCandidate(mocks, someField, targetInstance);

        assertSame(mocks, nextFilter.capturedMocks);
        assertSame(nextFilter.injecterToReturn, result);
    }

    // Tests multiple mocks where exactly one mock matches the field name
    @Test
    public void testFilterCandidate_multipleMocksOneMatching_passesMatchingMockToNext() {
        Object mock1 = Mockito.mock(List.class, "someField");
        Object mock2 = Mockito.mock(List.class, "unrelatedMock");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        filter.filterCandidate(mocks, someField, targetInstance);

        assertNotNull(nextFilter.capturedMocks);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertFalse(nextFilter.capturedMocks.contains(mock2));
    }

    // Tests multiple mocks where no mock matches the field name
    @Test
    public void testFilterCandidate_multipleMocksNoneMatching_passesEmptyCollectionToNext() {
        Object mock1 = Mockito.mock(List.class, "mockOne");
        Object mock2 = Mockito.mock(List.class, "mockTwo");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        filter.filterCandidate(mocks, someField, targetInstance);

        assertNotNull(nextFilter.capturedMocks);
        assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    // Tests multiple mocks where more than one mock has the matching name
    @Test
    public void testFilterCandidate_multipleMocksMultipleMatching_passesAllMatchingMocksToNext() {
        Object mock1 = Mockito.mock(List.class, "someField");
        Object mock2 = Mockito.mock(List.class, "someField");
        Object mock3 = Mockito.mock(List.class, "otherName");
        List<Object> mocks = Arrays.asList(mock1, mock2, mock3);

        filter.filterCandidate(mocks, someField, targetInstance);

        assertNotNull(nextFilter.capturedMocks);
        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertTrue(nextFilter.capturedMocks.contains(mock2));
        assertFalse(nextFilter.capturedMocks.contains(mock3));
    }

    // Tests that field and fieldInstance arguments are correctly forwarded to next filter
    @Test
    public void testFilterCandidate_validInput_forwardsFieldAndInstanceAccurately() {
        Object mock1 = Mockito.mock(List.class, "otherField");
        Object mock2 = Mockito.mock(List.class, "someField");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        filter.filterCandidate(mocks, otherField, targetInstance);

        assertSame(otherField, nextFilter.capturedField);
        assertSame(targetInstance, nextFilter.capturedFieldInstance);
    }

    // Tests that return value from next filter is returned directly
    @Test
    public void testFilterCandidate_anyInput_returnsNextFilterResult() {
        Object mock1 = Mockito.mock(List.class, "someField");
        Object mock2 = Mockito.mock(List.class, "otherField");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, someField, targetInstance);

        assertSame(nextFilter.injecterToReturn, result);
    }

    // Tests with default unnamed mocks
    @Test
    public void testFilterCandidate_multipleDefaultUnnamedMocks_filtersCorrectly() {
        Object mock1 = Mockito.mock(List.class);
        Object mock2 = Mockito.mock(List.class);
        List<Object> mocks = Arrays.asList(mock1, mock2);

        filter.filterCandidate(mocks, someField, targetInstance);

        assertNotNull(nextFilter.capturedMocks);
        assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    // Tests with Set collection instead of List
    @Test
    public void testFilterCandidate_setCollection_filtersMatchingMocks() {
        Object mock1 = Mockito.mock(List.class, "someField");
        Object mock2 = Mockito.mock(List.class, "otherField");
        Set<Object> mocks = new HashSet<Object>(Arrays.asList(mock1, mock2));

        filter.filterCandidate(mocks, someField, targetInstance);

        assertNotNull(nextFilter.capturedMocks);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
    }
}
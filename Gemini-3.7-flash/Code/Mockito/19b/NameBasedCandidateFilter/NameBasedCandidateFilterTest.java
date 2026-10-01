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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NameBasedCandidateFilterTest {

    private CapturingMockCandidateFilter nextFilter;
    private NameBasedCandidateFilter filter;
    private SampleTarget targetInstance;
    private Field matchingField;
    private Field otherField;
    private Field thirdField;

    private static class SampleTarget {
        private Object matchingMock;
        private Object anotherField;
        private Object thirdField;
    }

    private static class CapturingMockCandidateFilter implements MockCandidateFilter {
        private Collection<Object> capturedMocks;
        private Field capturedField;
        private Object capturedFieldInstance;
        private final OngoingInjecter stubbedInjecter = new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = mocks;
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return stubbedInjecter;
        }
    }

    @Before
    public void setUp() throws Exception {
        nextFilter = new CapturingMockCandidateFilter();
        filter = new NameBasedCandidateFilter(nextFilter);
        targetInstance = new SampleTarget();
        matchingField = SampleTarget.class.getDeclaredField("matchingMock");
        otherField = SampleTarget.class.getDeclaredField("anotherField");
        thirdField = SampleTarget.class.getDeclaredField("thirdField");
    }

    // Tests empty collection where size is not greater than 1
    @Test
    public void testFilterCandidate_emptyMocksCollection_delegatesOriginalCollectionToNext() {
        Collection<Object> mocks = Collections.emptyList();

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertSame(mocks, nextFilter.capturedMocks);
        assertSame(matchingField, nextFilter.capturedField);
        assertSame(targetInstance, nextFilter.capturedFieldInstance);
    }

    // Tests single mock where size is 1 and does not enter branch
    @Test
    public void testFilterCandidate_singleMock_delegatesOriginalCollectionToNext() {
        Object mock = Mockito.mock(List.class, "anyName");
        Collection<Object> mocks = Collections.singletonList(mock);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertSame(mocks, nextFilter.capturedMocks);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock));
    }

    // Tests multiple mocks where one matches field name
    @Test
    public void testFilterCandidate_multipleMocksOneMatching_delegatesMatchingMockToNext() {
        Object matchingMock = Mockito.mock(List.class, "matchingMock");
        Object nonMatchingMock = Mockito.mock(List.class, "otherName");
        List<Object> mocks = Arrays.asList(matchingMock, nonMatchingMock);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(matchingMock));
    }

    // Tests multiple mocks where none matches field name
    @Test
    public void testFilterCandidate_multipleMocksNoneMatching_delegatesEmptyMatchesToNext() {
        Object mock1 = Mockito.mock(List.class, "firstMock");
        Object mock2 = Mockito.mock(List.class, "secondMock");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    // Tests multiple mocks where multiple mocks share the matching name
    @Test
    public void testFilterCandidate_multipleMocksMultipleMatching_delegatesAllMatchingMocksToNext() {
        Object mock1 = Mockito.mock(List.class, "matchingMock");
        Object mock2 = Mockito.mock(List.class, "matchingMock");
        Object mock3 = Mockito.mock(List.class, "unrelatedMock");
        List<Object> mocks = Arrays.asList(mock1, mock2, mock3);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertTrue(nextFilter.capturedMocks.contains(mock2));
    }

    // Tests multiple mocks against a different target field
    @Test
    public void testFilterCandidate_multipleMocksMatchingDifferentField_delegatesCorrectMatchToNext() {
        Object mockForAnother = Mockito.mock(List.class, "anotherField");
        Object mockForMatching = Mockito.mock(List.class, "matchingMock");
        List<Object> mocks = Arrays.asList(mockForAnother, mockForMatching);

        OngoingInjecter result = filter.filterCandidate(mocks, otherField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mockForAnother));
    }

    // Tests preserving field and fieldInstance parameters with multiple mocks
    @Test
    public void testFilterCandidate_multipleMocks_forwardsFieldAndInstanceUnchanged() {
        Object mock1 = Mockito.mock(List.class, "matchingMock");
        Object mock2 = Mockito.mock(List.class, "other");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(matchingField, nextFilter.capturedField);
        assertSame(targetInstance, nextFilter.capturedFieldInstance);
    }

    // Tests multiple mocks using Set collection input
    @Test
    public void testFilterCandidate_setCollectionWithMultipleMocks_filtersCorrectly() {
        Object mock1 = Mockito.mock(List.class, "thirdField");
        Object mock2 = Mockito.mock(List.class, "unmatchedField");
        Set<Object> mocks = new HashSet<Object>(Arrays.asList(mock1, mock2));

        OngoingInjecter result = filter.filterCandidate(mocks, thirdField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertSame(thirdField, nextFilter.capturedField);
        assertSame(targetInstance, nextFilter.capturedFieldInstance);
    }

    // Tests when all mocks match the target field name
    @Test
    public void testFilterCandidate_allMocksMatch_delegatesAllMocksToNext() {
        Object mock1 = Mockito.mock(List.class, "matchingMock");
        Object mock2 = Mockito.mock(List.class, "matchingMock");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertTrue(nextFilter.capturedMocks.contains(mock2));
    }

    // Tests filter with null field instance
    @Test
    public void testFilterCandidate_nullFieldInstance_delegatesNullInstanceToNext() {
        Object mock1 = Mockito.mock(List.class, "matchingMock");
        Object mock2 = Mockito.mock(List.class, "other");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, null);

        assertSame(nextFilter.stubbedInjecter, result);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock1));
        assertSame(matchingField, nextFilter.capturedField);
        assertSame(null, nextFilter.capturedFieldInstance);
    }

    // Tests single mock in a Set collection where size is 1
    @Test
    public void testFilterCandidate_singleMockInSet_delegatesDirectlyToNext() {
        Object mock = Mockito.mock(List.class, "anyName");
        Set<Object> mocks = Collections.singleton(mock);

        OngoingInjecter result = filter.filterCandidate(mocks, matchingField, targetInstance);

        assertSame(nextFilter.stubbedInjecter, result);
        assertSame(mocks, nextFilter.capturedMocks);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mock));
    }
}
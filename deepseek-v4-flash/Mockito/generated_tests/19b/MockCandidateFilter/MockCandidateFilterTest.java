package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test class for the MockCandidateFilter interface,
 * using the concrete implementation TypeBasedCandidateFilter.
 * Tests focus on null safety and type matching logic,
 * which are relevant to the Defects4J bug 19b scenario.
 */
public class MockCandidateFilterTest {

    // ------------------------------------------------------------------
    // Helper: create a simple mock of OngoingInjecter for chaining
    // ------------------------------------------------------------------
    private static OngoingInjecter createMockOngoingInjecter(final boolean injectable) {
        return new OngoingInjecter() {
            public Object inject() {
                return null; // not needed for testing filterCandidate
            }
            public boolean isInjectable() {
                return injectable;
            }
            public void thenInject() {
                // no-op
            }
        };
    }

    // ------------------------------------------------------------------
    // 1. Normal case: mocks collection contains a single matching type
    // ------------------------------------------------------------------
    @Test
    public void testFilterCandidate_matchingType_returnsOngoingInjecter() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        Object mock = new Object();
        mocks.add(mock);
        Field field = Object.class.getDeclaredField("hash"); // any field of Object type
        Object fieldInstance = new Object();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        OngoingInjecter result = filter.filterCandidate(mocks, field, fieldInstance);

        // then
        assertNotNull("OngoingInjecter should not be null", result);
        assertTrue("Should be injectable", result.isInjectable());
    }

    // ------------------------------------------------------------------
    // 2. No matching type in mocks
    // ------------------------------------------------------------------
    @Test
    public void testFilterCandidate_noMatchingType_returnsFallback() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(Integer.valueOf(1));
        Field field = String.class.getDeclaredField("value"); // field of char[] type
        Object fieldInstance = new String();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(false);
            }
        });
        OngoingInjecter result = filter.filterCandidate(mocks, field, fieldInstance);

        // then
        assertNotNull(result);
        assertFalse("Should not be injectable since no type match", result.isInjectable());
    }

    // ------------------------------------------------------------------
    // 3. Null mocks collection - expect exception from chain
    // ------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullMocks_throwsNpe() throws Exception {
        // given
        Field field = Object.class.getDeclaredField("hash");
        Object fieldInstance = new Object();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        filter.filterCandidate(null, field, fieldInstance);
    }

    // ------------------------------------------------------------------
    // 4. Null field - expect exception from reflection code in filter
    // ------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testFilterCandidate_nullField_throwsException() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new Object());
        Object fieldInstance = new Object();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        filter.filterCandidate(mocks, null, fieldInstance);
    }

    // ------------------------------------------------------------------
    // 5. Null fieldInstance - some implementations may throw
    // ------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullFieldInstance_throwsException() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new Object());
        Field field = Object.class.getDeclaredField("hash");

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        filter.filterCandidate(mocks, field, null);
    }

    // ------------------------------------------------------------------
    // 6. Empty mocks collection - should delegate to next filter
    // ------------------------------------------------------------------
    @Test
    public void testFilterCandidate_emptyMocks_delegatesToNext() throws Exception {
        // given
        List<Object> mocks = Collections.emptyList();
        Field field = Object.class.getDeclaredField("hash");
        Object fieldInstance = new Object();

        final boolean[] called = {false};
        MockCandidateFilter next = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                called[0] = true;
                return createMockOngoingInjecter(true);
            }
        };

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(next);
        OngoingInjecter result = filter.filterCandidate(mocks, field, fieldInstance);

        // then
        assertTrue("Next filter should be called when mocks empty", called[0]);
        assertNotNull(result);
    }

    // ------------------------------------------------------------------
    // 7. Multiple mocks with matching type - should be injectable
    // ------------------------------------------------------------------
    @Test
    public void testFilterCandidate_multipleMatchingMocks_returnsInjector() throws Exception {
        // given
        List<Object> mocks = Arrays.asList(new Object(), "string", Integer.valueOf(1));
        Field stringField = String.class.getDeclaredField("hash"); // int, no match
        // Actually use Object type field: Object.class.getField("hash") is int; use Object.class field "someField" not exist.
        // Let's use a custom dummy class
        class Dummy {
            public Object obj;
        }
        Field objField = Dummy.class.getField("obj");
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        OngoingInjecter result = filter.filterCandidate(mocks, objField, new Dummy());
        assertNotNull(result);
        assertTrue("Should be injectable because Object matches one mock", result.isInjectable());
    }

    // ------------------------------------------------------------------
    // 8. Field type is primitive - should not match any mock
    // ------------------------------------------------------------------
    @Test
    public void testFilterCandidate_primitiveField_noMatch() throws Exception {
        // given
        List<Object> mocks = Collections.singletonList(new Object());
        Field field = Integer.class.getDeclaredField("value"); // int primitive
        Object fieldInstance = new Integer(0);

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(false);
            }
        });
        OngoingInjecter result = filter.filterCandidate(mocks, field, fieldInstance);

        // then
        assertFalse("Primitive field should not be matchable", result.isInjectable());
    }

    // ------------------------------------------------------------------
    // 9. Mock inside collection is null - some implementations may handle
    // ------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullElementInMocks_throws() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(null);
        Field field = Object.class.getDeclaredField("hash");
        Object fieldInstance = new Object();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object instance) {
                return createMockOngoingInjecter(true);
            }
        });
        filter.filterCandidate(mocks, field, fieldInstance);
    }

    // ------------------------------------------------------------------
    // 10. TypeBasedCandidateFilter with no next filter - should throw NPE
    // ------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_noNextFilter_throws() throws Exception {
        // given
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new Object());
        Field field = Object.class.getDeclaredField("hash");
        Object fieldInstance = new Object();

        // when
        MockCandidateFilter filter = new TypeBasedCandidateFilter(null);
        filter.filterCandidate(mocks, field, fieldInstance);
    }
}
package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;
    private SampleTarget target;

    public static class SampleTarget {
        private String message;
        private Integer count;
        private Object customObject;
    }

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
        target = new SampleTarget();
    }

    // Tests successful injection when exactly one matching mock is provided
    @Test
    public void testFilterCandidate_singleMatchingMock_injectsFieldAndReturnsTrue() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = Collections.<Object>singletonList("hello world");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertTrue(injected);
        assertEquals("hello world", target.message);
    }

    // Tests false return and no injection when mocks collection is empty (boundary: size 0)
    @Test
    public void testFilterCandidate_emptyMocksCollection_returnsFalseAndDoesNotInject() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertFalse(injected);
        assertNull(target.message);
    }

    // Tests false return and no injection when multiple mocks are provided (boundary: size > 1)
    @Test
    public void testFilterCandidate_multipleMocks_returnsFalseAndDoesNotInject() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = Arrays.<Object>asList("first", "second");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertFalse(injected);
        assertNull(target.message);
    }

    // Tests successful injection with null mock candidate when size is 1
    @Test
    public void testFilterCandidate_singleNullMock_injectsNullAndReturnsTrue() throws Exception {
        target.message = "initial";
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = Collections.singletonList(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertTrue(injected);
        assertNull(target.message);
    }

    // Tests successful injection for non-String reference types
    @Test
    public void testFilterCandidate_singleIntegerMock_injectsSuccessfully() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("count");
        Integer expectedValue = 42;
        List<Object> mocks = Collections.<Object>singletonList(expectedValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertTrue(injected);
        assertEquals(expectedValue, target.count);
    }

    // Tests successful injection for generic Object field
    @Test
    public void testFilterCandidate_singleObjectMock_injectsSuccessfully() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("customObject");
        Object expectedValue = new Object();
        List<Object> mocks = Collections.singletonList(expectedValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertTrue(injected);
        assertEquals(expectedValue, target.customObject);
    }

    // Tests exception path when injecting incompatible type into field
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_incompatibleMockType_throwsMockitoException() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("count");
        List<Object> mocks = Collections.<Object>singletonList("incompatible string");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        injecter.thenInject();
    }

    // Tests exception path when target instance is null for instance field
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_nullTargetInstance_throwsMockitoException() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = Collections.<Object>singletonList("testValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, null);
        assertNotNull(injecter);

        injecter.thenInject();
    }

    // Tests boundary condition with ArrayList containing exactly 3 items
    @Test
    public void testFilterCandidate_threeMocks_returnsFalse() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("message");
        List<Object> mocks = new ArrayList<Object>(Arrays.asList("a", "b", "c"));

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean injected = injecter.thenInject();

        assertFalse(injected);
        assertNull(target.message);
    }
}
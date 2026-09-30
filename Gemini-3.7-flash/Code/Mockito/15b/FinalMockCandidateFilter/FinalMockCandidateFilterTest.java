package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;

    public static class SampleTarget {
        private String stringField;
        public final int finalIntField = 42;
        private Object objectField;
    }

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
    }

    // Tests injection when exactly one mock is provided
    @Test
    public void testFilterCandidate_singleMatchingMock_injectsSuccessfully() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.singletonList((Object) "injectedValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean result = injecter.thenInject();
        assertTrue(result);
        assertEquals("injectedValue", target.stringField);
    }

    // Tests false branch when mocks collection is empty
    @Test
    public void testFilterCandidate_emptyMocks_returnsFalse() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean result = injecter.thenInject();
        assertFalse(result);
        assertNull(target.stringField);
    }

    // Tests false branch when more than one mock is provided
    @Test
    public void testFilterCandidate_multipleMocks_returnsFalse() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Arrays.asList("mock1", "mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean result = injecter.thenInject();
        assertFalse(result);
        assertNull(target.stringField);
    }

    // Tests exception handling when injection fails due to type mismatch
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_incompatibleMockType_throwsMockitoException() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.singletonList((Object) Integer.valueOf(123));

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);
        injecter.thenInject();
    }

    // Tests injection with null element in single-element mock collection
    @Test
    public void testFilterCandidate_singleNullMock_injectsNullValue() throws Exception {
        SampleTarget target = new SampleTarget();
        target.stringField = "initial";
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.singletonList((Object) null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean result = injecter.thenInject();
        assertTrue(result);
        assertNull(target.stringField);
    }

    // Tests injection on generic object field
    @Test
    public void testFilterCandidate_singleObjectMock_injectsReference() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("objectField");
        Object mockInstance = new Object();
        List<Object> mocks = Collections.singletonList(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        boolean result = injecter.thenInject();
        assertTrue(result);
        assertSame(mockInstance, target.objectField);
    }
}
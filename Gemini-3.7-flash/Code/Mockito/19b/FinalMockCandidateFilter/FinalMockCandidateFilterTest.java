package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
    }

    // Tests that OngoingInjecter is not null when mock collection is empty
    @Test
    public void testFilterCandidate_emptyMocks_returnsNonNullInjecter() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
    }

    // Tests that thenInject returns null when mock collection is empty
    @Test
    public void testFilterCandidate_emptyMocks_thenInjectReturnsNull() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertNull(result);
        assertNull(target.getValue());
    }

    // Tests that thenInject returns null when mock collection contains multiple elements
    @Test
    public void testFilterCandidate_multipleMocks_thenInjectReturnsNull() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        List<Object> mocks = Arrays.<Object>asList("mock1", "mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertNull(result);
        assertNull(target.getValue());
    }

    // Tests successful injection directly via field access when no setter exists
    @Test
    public void testFilterCandidate_singleMockFieldAccess_injectsDirectlyToField() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        String mockInstance = "injectedMockValue";
        List<Object> mocks = Collections.<Object>singletonList(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertEquals(mockInstance, result);
        assertEquals(mockInstance, target.getValue());
    }

    // Tests successful injection via property setter when setter exists
    @Test
    public void testFilterCandidate_singleMockPropertySetter_injectsViaSetter() throws Exception {
        SampleTargetWithSetter target = new SampleTargetWithSetter();
        Field field = SampleTargetWithSetter.class.getDeclaredField("value");
        String mockInstance = "setterMockValue";
        List<Object> mocks = Collections.<Object>singletonList(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertEquals(mockInstance, result);
        assertEquals(mockInstance, target.getValue());
        assertTrue(target.isSetterCalled());
    }

    // Tests returning the matching mock instance when injected
    @Test
    public void testFilterCandidate_singleMock_returnsMatchingMockInstance() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        Object mockInstance = new String("testInstance");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertSame(mockInstance, result);
    }

    // Tests exception path when setter throws a RuntimeException
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_setterThrowsRuntimeException_throwsMockitoException() throws Exception {
        SampleTargetWithThrowingSetter target = new SampleTargetWithThrowingSetter();
        Field field = SampleTargetWithThrowingSetter.class.getDeclaredField("value");
        String mockInstance = "mockValue";
        List<Object> mocks = Collections.<Object>singletonList(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        injecter.thenInject();
    }

    // Tests exception path when field injection fails due to type mismatch
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_typeMismatch_throwsMockitoException() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        Integer incompatibleMock = Integer.valueOf(123);
        List<Object> mocks = Collections.<Object>singletonList(incompatibleMock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        injecter.thenInject();
    }

    // Tests that OngoingInjecter is not null when mock collection contains multiple elements
    @Test
    public void testFilterCandidate_multipleMocks_returnsNonNullInjecter() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        List<Object> mocks = Arrays.<Object>asList("mock1", "mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
    }

    // Tests injection with a Set collection containing a single mock
    @Test
    public void testFilterCandidate_singleMockInSet_injectsCorrectly() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        String mockInstance = "setMockValue";
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertEquals(mockInstance, result);
        assertEquals(mockInstance, target.getValue());
    }

    // Tests injection when mock instance is null inside a single-element collection
    @Test
    public void testFilterCandidate_singleNullMock_injectsNullValue() throws Exception {
        SampleTargetFieldOnly target = new SampleTargetFieldOnly();
        Field field = SampleTargetFieldOnly.class.getDeclaredField("value");
        List<Object> mocks = Collections.singletonList(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        Object result = injecter.thenInject();

        assertNull(result);
        assertNull(target.getValue());
    }

    // Helper classes for testing reflection injection

    static class SampleTargetFieldOnly {
        private String value;

        public String getValue() {
            return value;
        }
    }

    static class SampleTargetWithSetter {
        private String value;
        private boolean setterCalled = false;

        public void setValue(String value) {
            this.value = value;
            this.setterCalled = true;
        }

        public String getValue() {
            return value;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    static class SampleTargetWithThrowingSetter {
        private String value;

        public void setValue(String value) {
            throw new RuntimeException("Setter failure");
        }

        public String getValue() {
            return value;
        }
    }
}
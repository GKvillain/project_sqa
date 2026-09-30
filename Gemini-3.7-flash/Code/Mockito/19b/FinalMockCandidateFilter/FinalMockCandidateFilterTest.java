package org.mockito.internal.configuration.injection.filter;

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
    private SampleTarget target;
    private List<Field> allFields;

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
        target = new SampleTarget();
        allFields = Arrays.asList(SampleTarget.class.getDeclaredFields());
    }

    // Tests empty mock collection returns injecter that returns null
    @Test
    public void testFilterCandidate_emptyMockCollection_returnsNullInjecter() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueNoSetter");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        assertNull(injecter.thenInject());
        assertNull(target.valueNoSetter);
    }

    // Tests multiple mocks in collection injects first mock candidate
    @Test
    public void testFilterCandidate_multipleMocks_injectsFirstMock() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueNoSetter");
        String firstMock = "firstMock";
        String secondMock = "secondMock";
        List<Object> mocks = Arrays.asList(firstMock, secondMock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertEquals(firstMock, result);
        assertEquals(firstMock, target.valueNoSetter);
    }

    // Tests single mock injected directly into field when no setter is available
    @Test
    public void testFilterCandidate_singleMockFieldAccess_injectsAndReturnsMock() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueNoSetter");
        String mockValue = "injectedViaField";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertEquals(mockValue, result);
        assertEquals(mockValue, target.valueNoSetter);
    }

    // Tests single mock injected via setter method when available
    @Test
    public void testFilterCandidate_singleMockPropertySetter_injectsViaSetterAndReturnsMock() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueWithSetter");
        String mockValue = "injectedViaSetter";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertEquals(mockValue, result);
        assertEquals(mockValue, target.getValueWithSetter());
        assertTrue(target.setterCalled);
    }

    // Tests single mock injected into primitive field
    @Test
    public void testFilterCandidate_singleMockPrimitiveField_injectsCorrectly() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("primitiveInt");
        Integer mockValue = 42;
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertEquals(mockValue, result);
        assertEquals(42, target.primitiveInt);
    }

    // Tests single null element in mock collection
    @Test
    public void testFilterCandidate_singleNullMock_injectsNullAndReturnsNull() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueNoSetter");
        target.valueNoSetter = "initialValue";
        List<Object> mocks = Collections.singletonList(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertNull(result);
        assertNull(target.valueNoSetter);
    }

    // Tests single mock injected into final field
    @Test
    public void testFilterCandidate_singleMockFinalField_injectsSuccessfully() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("finalField");
        String mockValue = "overriddenFinal";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);

        assertNotNull(injecter);
        Object result = injecter.thenInject();
        assertEquals(mockValue, result);
        assertEquals(mockValue, target.finalField);
    }

    // Tests type mismatch during injection throws MockitoException
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_typeMismatch_throwsMockitoException() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueNoSetter");
        Integer incompatibleMock = 12345;
        List<Object> mocks = Collections.singletonList((Object) incompatibleMock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);
        assertNotNull(injecter);
        injecter.thenInject();
    }

    // Tests setter throwing runtime exception triggers Reporter and throws MockitoException
    @Test(expected = MockitoException.class)
    public void testFilterCandidate_setterThrowsException_throwsMockitoException() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("valueThrowingSetter");
        String mockValue = "testValue";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, allFields, target);
        assertNotNull(injecter);
        injecter.thenInject();
    }

    static class SampleTarget {
        private String valueNoSetter;
        private String valueWithSetter;
        private boolean setterCalled;
        private int primitiveInt;
        private final String finalField = "initial";
        private String valueThrowingSetter;

        public void setValueWithSetter(String valueWithSetter) {
            this.valueWithSetter = valueWithSetter;
            this.setterCalled = true;
        }

        public String getValueWithSetter() {
            return valueWithSetter;
        }

        public void setValueThrowingSetter(String val) {
            throw new RuntimeException("Setter invocation failed");
        }

        public String getValueThrowingSetter() {
            return valueThrowingSetter;
        }
    }
}
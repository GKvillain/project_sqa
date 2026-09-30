package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for FinalMockCandidateFilter.
 * Target: Defects4J bug 19b in Mockito.
 */
public class FinalMockCandidateFilterTest {

    // Helper class with various field types for injection testing
    private static class Bean {
        private String name;
        private int value;
        private String strField;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        // no setter for value

        public String getStrField() { return strField; }
        // no setter for strField
    }

    @Test
    // Tests single mock with successful property setter injection
    public void testFilterCandidate_singleMockPropertySetterSuccess_shouldInjectAndReturnMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field nameField = Bean.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();
        String mock = "injectedValue";
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, nameField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        assertEquals("injectedValue", bean.getName());
    }

    @Test
    // Tests single mock with property setter failing, then field setter succeeds
    public void testFilterCandidate_singleMockPropertySetterFailFieldSetterSuccess_shouldInjectViaReflectionAndReturnMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field valueField = Bean.class.getDeclaredField("value");
        Collection<Object> mocks = new ArrayList<Object>();
        Integer mock = 42;
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, valueField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        assertEquals(42, bean.getValue());
    }

    @Test
    // Tests single mock where field setter throws RuntimeException (type mismatch)
    // The exception is caught, so the field remains unchanged but the mock is still returned.
    public void testFilterCandidate_singleMockFieldSetterThrows_shouldCatchExceptionAndReturnMockWithoutInjection() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field strField = Bean.class.getDeclaredField("strField");
        Collection<Object> mocks = new ArrayList<Object>();
        Integer mock = 123; // incompatible type for a String field
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, strField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        assertNull(bean.getStrField()); // field not set due to exception
    }

    @Test
    // Tests empty mock collection -> injecter returns null
    public void testFilterCandidate_emptyMocks_returnsNullInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field nameField = Bean.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();

        OngoingInjecter injecter = filter.filterCandidate(mocks, nameField, bean);
        assertNull(injecter.thenInject());
    }

    @Test
    // Tests multiple mocks -> injecter returns null
    public void testFilterCandidate_multipleMocks_returnsNullInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field nameField = Bean.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();
        mocks.add("mock1");
        mocks.add("mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, nameField, bean);
        assertNull(injecter.thenInject());
    }

    @Test(expected = NullPointerException.class)
    // Tests null mock collection -> NullPointerException from filterCandidate
    public void testFilterCandidate_nullMocks_throwsNullPointerException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field nameField = Bean.class.getDeclaredField("name");

        filter.filterCandidate(null, nameField, bean);
    }

    @Test(expected = NullPointerException.class)
    // Tests null field -> NullPointerException when creating BeanPropertySetter
    public void testFilterCandidate_nullField_throwsNullPointerException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Collection<Object> mocks = new ArrayList<Object>();
        mocks.add("mock");

        filter.filterCandidate(mocks, null, bean);
    }

    @Test(expected = NullPointerException.class)
    // Tests null fieldInstance -> NullPointerException when creating BeanPropertySetter
    public void testFilterCandidate_nullFieldInstance_throwsNullPointerException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field nameField = Bean.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();
        mocks.add("mock");

        filter.filterCandidate(mocks, nameField, null);
    }

    // ============================== New test cases for uncovered parts ==============================

    // Helper class with a setter that throws an exception
    private static class BeanWithThrowingSetter {
        private String name;
        public String getName() { return name; }
        public void setName(String name) {
            throw new RuntimeException("setter forced failure");
        }
    }

    @Test
    // Tests single mock with property setter that throws, then field setter succeeds
    public void testFilterCandidate_setterThrowsFieldSetterWorks_shouldInjectViaReflection() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        BeanWithThrowingSetter bean = new BeanWithThrowingSetter();
        Field nameField = BeanWithThrowingSetter.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();
        String mock = "injectedViaReflection";
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, nameField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        assertEquals("injectedViaReflection", bean.getName());
    }

    @Test
    // Tests single mock that is null (null element in collection) -> exception caught, mock (null) returned
    public void testFilterCandidate_singleNullMock_returnsNull() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Bean bean = new Bean();
        Field nameField = Bean.class.getDeclaredField("name");
        Collection<Object> mocks = new ArrayList<Object>();
        mocks.add(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, nameField, bean);
        Object result = injecter.thenInject();

        assertNull(result);
        assertNull(bean.getName()); // field should remain unchanged
    }

    @Test
    // Tests field that does not belong to the field instance -> IllegalArgException caught, mock returned
    public void testFilterCandidate_wrongFieldForInstance_returnsMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        // Use a field from a different class to cause IllegalArgumentException during field set
        class OtherBean {
            public String otherField;
        }
        Bean bean = new Bean(); // instance of Bean, not OtherBean
        Field otherField = OtherBean.class.getField("otherField");
        Collection<Object> mocks = new ArrayList<Object>();
        String mock = "test";
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, otherField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        // Nothing else to assert, bean's fields should be unchanged
    }

    // Helper class with a final field
    private static class BeanWithFinalField {
        private final String finalField = "initial";
        public String getFinalField() { return finalField; }
    }

    @Test
    // Tests injection into a final field; the mock is returned regardless of injection success
    public void testFilterCandidate_finalField_returnsMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        BeanWithFinalField bean = new BeanWithFinalField();
        Field finalField = BeanWithFinalField.class.getDeclaredField("finalField");
        Collection<Object> mocks = new ArrayList<Object>();
        String mock = "newValue";
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, finalField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        // The final field may or may not be set; no assertion on bean.getFinalField()
    }

    // Helper class with a static field
    private static class BeanWithStaticField {
        private static String staticField;
        public static String getStaticField() { return staticField; }
    }

    @Test
    // Tests injection into a static field
    public void testFilterCandidate_staticField_returnsMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        BeanWithStaticField bean = new BeanWithStaticField();
        Field staticField = BeanWithStaticField.class.getDeclaredField("staticField");
        Collection<Object> mocks = new ArrayList<Object>();
        String mock = "staticValue";
        mocks.add(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, staticField, bean);
        Object result = injecter.thenInject();

        assertSame(mock, result);
        // Verify static field was set (if injection via reflection works)
        assertEquals("staticValue", BeanWithStaticField.staticField);
    }
}
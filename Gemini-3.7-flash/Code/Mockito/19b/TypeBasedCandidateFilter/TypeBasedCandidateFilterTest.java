package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TypeBasedCandidateFilterTest {

    private String stringField;
    private CharSequence charSequenceField;
    private Integer integerField;
    private Number numberField;
    private Object objectField;
    private List<?> listField;
    private String[] stringArrayField;
    private Object[] objectArrayField;
    private Integer[] integerArrayField;
    private CustomBase customBaseField;
    private CustomInterface customInterfaceField;

    private RecordingMockCandidateFilter nextFilter;
    private TypeBasedCandidateFilter filter;

    static interface CustomInterface {}
    static class CustomBase implements CustomInterface {}
    static class CustomSub extends CustomBase {}
    static class UnrelatedClass {}

    @Before
    public void setUp() {
        nextFilter = new RecordingMockCandidateFilter();
        filter = new TypeBasedCandidateFilter(nextFilter);
    }

    // Tests empty mock collection passing through
    @Test
    public void testFilterCandidate_emptyMocks_passesEmptyListToNext() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        OngoingInjecter result = filter.filterCandidate(Collections.emptyList(), field, this);

        assertNotNull(result);
        assertTrue(nextFilter.capturedMocks.isEmpty());
        assertSame(field, nextFilter.capturedField);
        assertSame(this, nextFilter.capturedFieldInstance);
    }

    // Tests matching exact type
    @Test
    public void testFilterCandidate_exactTypeMatch_includesMock() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        String mockString = "test";
        Collection<Object> mocks = Collections.<Object>singletonList(mockString);

        OngoingInjecter result = filter.filterCandidate(mocks, field, this);

        assertNotNull(result);
        assertEquals(1, nextFilter.capturedMocks.size());
        assertSame(mockString, nextFilter.capturedMocks.get(0));
    }

    // Tests non-matching type
    @Test
    public void testFilterCandidate_typeMismatch_excludesMock() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        Integer mockInt = 123;
        Collection<Object> mocks = Collections.<Object>singletonList(mockInt);

        OngoingInjecter result = filter.filterCandidate(mocks, field, this);

        assertNotNull(result);
        assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    // Tests assignable superclass type match
    @Test
    public void testFilterCandidate_subclassToSuperclass_includesMock() throws Exception {
        Field field = getClass().getDeclaredField("numberField");
        Integer mockInt = 456;
        Double mockDouble = 3.14;
        String mockString = "notANumber";
        Collection<Object> mocks = Arrays.asList(mockInt, mockDouble, mockString);

        filter.filterCandidate(mocks, field, this);

        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mockInt));
        assertTrue(nextFilter.capturedMocks.contains(mockDouble));
    }

    // Tests assignable interface type match
    @Test
    public void testFilterCandidate_classToInterface_includesMock() throws Exception {
        Field field = getClass().getDeclaredField("charSequenceField");
        String mockString = "chars";
        StringBuilder mockBuilder = new StringBuilder();
        Integer mockInt = 789;
        Collection<Object> mocks = Arrays.asList(mockString, mockBuilder, mockInt);

        filter.filterCandidate(mocks, field, this);

        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(mockString));
        assertTrue(nextFilter.capturedMocks.contains(mockBuilder));
    }

    // Tests matching against Object field
    @Test
    public void testFilterCandidate_objectField_acceptsAllMocks() throws Exception {
        Field field = getClass().getDeclaredField("objectField");
        String mockString = "str";
        Integer mockInt = 1;
        List<Object> mockList = new ArrayList<Object>();
        Collection<Object> mocks = Arrays.asList(mockString, mockInt, mockList);

        filter.filterCandidate(mocks, field, this);

        assertEquals(3, nextFilter.capturedMocks.size());
    }

    // Tests multiple mocks of same matching type
    @Test
    public void testFilterCandidate_multipleMatchingMocks_preservesAllMatches() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        String mock1 = "first";
        String mock2 = "second";
        Collection<Object> mocks = Arrays.<Object>asList(mock1, mock2);

        filter.filterCandidate(mocks, field, this);

        assertEquals(2, nextFilter.capturedMocks.size());
        assertSame(mock1, nextFilter.capturedMocks.get(0));
        assertSame(mock2, nextFilter.capturedMocks.get(1));
    }

    // Tests return value delegation from next filter
    @Test
    public void testFilterCandidate_delegatesReturnValueFromNextFilter() throws Exception {
        Field field = getClass().getDeclaredField("integerField");
        final OngoingInjecter expectedInjecter = new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };
        filter = new TypeBasedCandidateFilter(new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field f, Object fi) {
                return expectedInjecter;
            }
        });

        OngoingInjecter actual = filter.filterCandidate(Collections.emptyList(), field, this);

        assertSame(expectedInjecter, actual);
    }

    // Tests null field instance passed through
    @Test
    public void testFilterCandidate_nullFieldInstance_passesNullToNext() throws Exception {
        Field field = getClass().getDeclaredField("stringField");

        filter.filterCandidate(Collections.emptyList(), field, null);

        assertNull(nextFilter.capturedFieldInstance);
    }

    // Tests null mocks collection causes NullPointerException
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullMocks_throwsException() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        filter.filterCandidate(null, field, this);
    }

    // Tests null field causes NullPointerException
    @Test(expected = NullPointerException.class)
    public void testFilterCandidate_nullField_throwsException() {
        filter.filterCandidate(Collections.singletonList(new Object()), null, this);
    }

    // Tests array type exact match
    @Test
    public void testFilterCandidate_arrayTypeMatch_includesMock() throws Exception {
        Field field = getClass().getDeclaredField("stringArrayField");
        String[] mockArray = new String[]{"hello", "world"};
        Collection<Object> mocks = Collections.<Object>singletonList(mockArray);

        filter.filterCandidate(mocks, field, this);

        assertEquals(1, nextFilter.capturedMocks.size());
        assertSame(mockArray, nextFilter.capturedMocks.get(0));
    }

    // Tests array type mismatch
    @Test
    public void testFilterCandidate_arrayTypeMismatch_excludesMock() throws Exception {
        Field field = getClass().getDeclaredField("stringArrayField");
        Integer[] mockArray = new Integer[]{1, 2, 3};
        Collection<Object> mocks = Collections.<Object>singletonList(mockArray);

        filter.filterCandidate(mocks, field, this);

        assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    // Tests array assignable to Object array
    @Test
    public void testFilterCandidate_arrayToObjectArray_includesMock() throws Exception {
        Field field = getClass().getDeclaredField("objectArrayField");
        String[] mockArray = new String[]{"a", "b"};
        Collection<Object> mocks = Collections.<Object>singletonList(mockArray);

        filter.filterCandidate(mocks, field, this);

        assertEquals(1, nextFilter.capturedMocks.size());
        assertSame(mockArray, nextFilter.capturedMocks.get(0));
    }

    // Tests custom class hierarchy candidate matching
    @Test
    public void testFilterCandidate_customClassHierarchy_matchesCorrectTypes() throws Exception {
        Field baseField = getClass().getDeclaredField("customBaseField");
        CustomBase baseObj = new CustomBase();
        CustomSub subObj = new CustomSub();
        UnrelatedClass unrelated = new UnrelatedClass();
        Collection<Object> mocks = Arrays.asList(baseObj, subObj, unrelated);

        filter.filterCandidate(mocks, baseField, this);

        assertEquals(2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(baseObj));
        assertTrue(nextFilter.capturedMocks.contains(subObj));
        assertFalse(nextFilter.capturedMocks.contains(unrelated));
    }

    // Tests custom interface candidate matching
    @Test
    public void testFilterCandidate_customInterface_matchesImplementors() throws Exception {
        Field interfaceField = getClass().getDeclaredField("customInterfaceField");
        CustomSub subObj = new CustomSub();
        UnrelatedClass unrelated = new UnrelatedClass();
        Collection<Object> mocks = Arrays.asList(subObj, unrelated);

        filter.filterCandidate(mocks, interfaceField, this);

        assertEquals(1, nextFilter.capturedMocks.size());
        assertSame(subObj, nextFilter.capturedMocks.get(0));
    }

    // Tests preserving order of matching candidates when interspersed with non-matching ones
    @Test
    public void testFilterCandidate_interspersedMatches_preservesRelativeOrder() throws Exception {
        Field field = getClass().getDeclaredField("numberField");
        Integer intVal = 10;
        String strVal1 = "skip1";
        Double doubleVal = 20.5;
        String strVal2 = "skip2";
        Long longVal = 30L;
        Collection<Object> mocks = Arrays.asList(intVal, strVal1, doubleVal, strVal2, longVal);

        filter.filterCandidate(mocks, field, this);

        assertEquals(3, nextFilter.capturedMocks.size());
        assertSame(intVal, nextFilter.capturedMocks.get(0));
        assertSame(doubleVal, nextFilter.capturedMocks.get(1));
        assertSame(longVal, nextFilter.capturedMocks.get(2));
    }

    // Tests mocks passed as a Set collection
    @Test
    public void testFilterCandidate_setCollectionOfMocks_filtersSuccessfully() throws Exception {
        Field field = getClass().getDeclaredField("stringField");
        String mockStr = "fromSet";
        Integer mockInt = 42;
        Set<Object> mocks = new HashSet<Object>(Arrays.asList(mockStr, mockInt));

        filter.filterCandidate(mocks, field, this);

        assertEquals(1, nextFilter.capturedMocks.size());
        assertSame(mockStr, nextFilter.capturedMocks.get(0));
    }

    private static class RecordingMockCandidateFilter implements MockCandidateFilter {
        List<Object> capturedMocks;
        Field capturedField;
        Object capturedFieldInstance;

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = new ArrayList<Object>(mocks);
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return new OngoingInjecter() {
                public Object thenInject() {
                    return null;
                }
            };
        }
    }
}
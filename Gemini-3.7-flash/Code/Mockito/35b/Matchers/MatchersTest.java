package org.mockito;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchersTest {

    private MockingProgress mockingProgress;

    @Before
    public void setUp() {
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.getArgumentMatcherStorage().reset();
    }

    @After
    public void tearDown() {
        mockingProgress.getArgumentMatcherStorage().reset();
    }

    // Tests primitive any matchers return their default primitive values
    @Test
    public void testPrimitiveAnyMatchers_returnDefaultPrimitiveValues() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals((char) 0, Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0001f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0001d);
        assertEquals((short) 0, Matchers.anyShort());
    }

    // Tests object and collection any matchers return empty or null values
    @Test
    public void testObjectAndCollectionAnyMatchers_returnExpectedValues() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.anyVararg());
        assertEquals("", Matchers.anyString());

        List<?> list = Matchers.anyList();
        assertNotNull(list);
        assertTrue(list.isEmpty());

        List<String> stringList = Matchers.anyListOf(String.class);
        assertNotNull(stringList);
        assertTrue(stringList.isEmpty());

        Set<?> set = Matchers.anySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());

        Set<Integer> intSet = Matchers.anySetOf(Integer.class);
        assertNotNull(intSet);
        assertTrue(intSet.isEmpty());

        Map<?, ?> map = Matchers.anyMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());

        Collection<?> collection = Matchers.anyCollection();
        assertNotNull(collection);
        assertTrue(collection.isEmpty());

        Collection<Double> doubleCollection = Matchers.anyCollectionOf(Double.class);
        assertNotNull(doubleCollection);
        assertTrue(doubleCollection.isEmpty());
    }

    // Tests primitive eq matchers return the expected dummy primitive values
    @Test
    public void testPrimitiveEqMatchers_returnDefaultPrimitiveValues() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 10));
        assertEquals((char) 0, Matchers.eq('x'));
        assertEquals(0, Matchers.eq(100));
        assertEquals(0L, Matchers.eq(1000L));
        assertEquals(0.0f, Matchers.eq(5.5f), 0.0001f);
        assertEquals(0.0d, Matchers.eq(9.99d), 0.0001d);
        assertEquals((short) 0, Matchers.eq((short) 3));
    }

    // Tests object equality and identity matchers
    @Test
    public void testObjectEqAndSameMatchers_returnNull() {
        String testValue = "test";
        assertNull(Matchers.eq(testValue));
        assertNull(Matchers.same(testValue));
        assertNull(Matchers.refEq(testValue, "someField"));
    }

    // Tests null and not-null matchers
    @Test
    public void testNullAndNotNullMatchers_returnNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.notNull());
        assertNull(Matchers.isNotNull());
    }

    // Tests String condition matchers return empty string
    @Test
    public void testStringConditionMatchers_returnEmptyString() {
        assertEquals("", Matchers.contains("substring"));
        assertEquals("", Matchers.matches(".*regex.*"));
        assertEquals("", Matchers.endsWith("suffix"));
        assertEquals("", Matchers.startsWith("prefix"));
    }

    // Tests isA matcher returns null
    @Test
    public void testIsA_withValidClass_returnsNull() {
        Integer result = Matchers.isA(Integer.class);
        assertNull(result);
    }

    // Tests custom hamcrest argument matchers
    @Test
    public void testCustomHamcrestMatchers_returnDefaultValues() {
        assertNull(Matchers.argThat(IsNull.nullValue()));
        assertEquals((char) 0, Matchers.charThat(IsEqual.equalTo('a')));
        assertFalse(Matchers.booleanThat(IsEqual.equalTo(true)));
        assertEquals((byte) 0, Matchers.byteThat(IsEqual.equalTo((byte) 1)));
        assertEquals((short) 0, Matchers.shortThat(IsEqual.equalTo((short) 2)));
        assertEquals(0, Matchers.intThat(IsEqual.equalTo(3)));
        assertEquals(0L, Matchers.longThat(IsEqual.equalTo(4L)));
        assertEquals(0.0f, Matchers.floatThat(IsEqual.equalTo(5.0f)), 0.0001f);
        assertEquals(0.0d, Matchers.doubleThat(IsEqual.equalTo(6.0d)), 0.0001d);
    }

    // Tests Matchers in Mockito stubbing and verification scenario
    @Test
    public void testMatchersIntegration_withMockitoMock() {
        List<String> mock = Mockito.mock(List.class);

        Mockito.when(mock.get(Matchers.anyInt())).thenReturn("matched");
        assertEquals("matched", mock.get(5));
        assertEquals("matched", mock.get(999));

        Mockito.verify(mock, Mockito.times(2)).get(Matchers.anyInt());
    }
}
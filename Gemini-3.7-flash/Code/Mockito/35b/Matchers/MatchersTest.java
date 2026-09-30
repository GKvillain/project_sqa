package org.mockito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchersTest {

    @Before
    public void setUp() {
        new ThreadSafeMockingProgress().reset();
    }

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().reset();
    }

    // Tests primitive any matchers return default primitive values
    @Test
    public void testAnyPrimitiveMatchers_returnsDefaultPrimitiveValues() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals('\u0000', Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0d);
        assertEquals((short) 0, Matchers.anyShort());
    }

    // Tests general object any matchers return null
    @Test
    public void testAnyObjectMatchers_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.anyVararg());
    }

    // Tests string matcher returns empty string
    @Test
    public void testAnyString_returnsEmptyString() {
        String result = Matchers.anyString();
        assertNotNull(result);
        assertEquals("", result);
    }

    // Tests collection any matchers return empty collection instances
    @Test
    public void testAnyCollectionMatchers_returnsEmptyCollections() {
        List list = Matchers.anyList();
        assertNotNull(list);
        assertTrue(list.isEmpty());

        List<String> stringList = Matchers.anyListOf(String.class);
        assertNotNull(stringList);
        assertTrue(stringList.isEmpty());

        Set set = Matchers.anySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());

        Set<String> stringSet = Matchers.anySetOf(String.class);
        assertNotNull(stringSet);
        assertTrue(stringSet.isEmpty());

        Map map = Matchers.anyMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());

        Collection collection = Matchers.anyCollection();
        assertNotNull(collection);
        assertTrue(collection.isEmpty());

        Collection<String> stringCollection = Matchers.anyCollectionOf(String.class);
        assertNotNull(stringCollection);
        assertTrue(stringCollection.isEmpty());
    }

    // Tests isA matcher returns null
    @Test
    public void testIsA_validClass_returnsNull() {
        String result = Matchers.isA(String.class);
        assertNull(result);

        Integer intResult = Matchers.isA(Integer.class);
        assertNull(intResult);
    }

    // Tests primitive eq matchers return default primitive values
    @Test
    public void testEqPrimitive_returnsDefaultValues() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 5));
        assertEquals('\u0000', Matchers.eq('c'));
        assertEquals(0, Matchers.eq(42));
        assertEquals(0L, Matchers.eq(100L));
        assertEquals(0.0f, Matchers.eq(1.5f), 0.0f);
        assertEquals(0.0d, Matchers.eq(2.5d), 0.0d);
        assertEquals((short) 0, Matchers.eq((short) 10));
    }

    // Tests object eq matcher returns null
    @Test
    public void testEqObject_returnsNull() {
        String value = "test";
        String result = Matchers.eq(value);
        assertNull(result);

        Object nullResult = Matchers.eq((Object) null);
        assertNull(nullResult);
    }

    // Tests refEq matcher with and without excluded fields returns null
    @Test
    public void testRefEq_returnsNull() {
        String value = "test";
        String result = Matchers.refEq(value);
        assertNull(result);

        String resultWithExclude = Matchers.refEq(value, "fieldToExclude");
        assertNull(resultWithExclude);
    }

    // Tests same matcher returns null
    @Test
    public void testSame_returnsNull() {
        String value = "test";
        String result = Matchers.same(value);
        assertNull(result);

        Object nullResult = Matchers.same(null);
        assertNull(nullResult);
    }

    // Tests null and notNull matchers return null
    @Test
    public void testNullMatchers_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.notNull());
        assertNull(Matchers.isNotNull());
    }

    // Tests string specific matchers return empty string
    @Test
    public void testStringSpecificMatchers_returnsEmptyString() {
        assertEquals("", Matchers.contains("sub"));
        assertEquals("", Matchers.matches(".*"));
        assertEquals("", Matchers.endsWith("end"));
        assertEquals("", Matchers.startsWith("start"));
    }

    // Tests custom argThat matcher returns null
    @Test
    public void testArgThat_customMatcher_returnsNull() {
        Matcher<String> customMatcher = new BaseMatcher<String>() {
            public boolean matches(Object item) {
                return true;
            }
            public void describeTo(Description description) {
                description.appendText("custom");
            }
        };
        String result = Matchers.argThat(customMatcher);
        assertNull(result);
    }

    // Tests primitive *That matchers return default primitive values
    @Test
    public void testPrimitiveThatMatchers_returnsDefaultValues() {
        Matcher<Boolean> boolMatcher = new BaseMatcher<Boolean>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertFalse(Matchers.booleanThat(boolMatcher));

        Matcher<Byte> byteMatcher = new BaseMatcher<Byte>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals((byte) 0, Matchers.byteThat(byteMatcher));

        Matcher<Character> charMatcher = new BaseMatcher<Character>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals('\u0000', Matchers.charThat(charMatcher));

        Matcher<Short> shortMatcher = new BaseMatcher<Short>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals((short) 0, Matchers.shortThat(shortMatcher));

        Matcher<Integer> intMatcher = new BaseMatcher<Integer>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals(0, Matchers.intThat(intMatcher));

        Matcher<Long> longMatcher = new BaseMatcher<Long>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals(0L, Matchers.longThat(longMatcher));

        Matcher<Float> floatMatcher = new BaseMatcher<Float>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals(0.0f, Matchers.floatThat(floatMatcher), 0.0f);

        Matcher<Double> doubleMatcher = new BaseMatcher<Double>() {
            public boolean matches(Object item) { return true; }
            public void describeTo(Description description) {}
        };
        assertEquals(0.0d, Matchers.doubleThat(doubleMatcher), 0.0d);
    }
}
package org.mockito;

import org.junit.Test;
import static org.junit.Assert.*;
import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.BaseMatcher;

public class MatchersTest {

    // Test all primitive any* methods return default values
    @Test
    public void testAnyPrimitives_returnsDefaultValues() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals((char) 0, Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
        assertEquals((short) 0, Matchers.anyShort());
    }

    // Test anyObject, anyVararg, any(Class), any() all return null
    @Test
    public void testAnyObjectVariants_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.anyVararg());
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.any());
    }

    // Test anyString returns empty string
    @Test
    public void testAnyString_returnsEmptyString() {
        assertEquals("", Matchers.anyString());
    }

    // Test anyList, anySet, anyMap, anyCollection and their generic variants return empty collections
    @Test
    public void testAnyCollectionVariants_returnsEmpty() {
        assertNotNull(Matchers.anyList());
        assertTrue(Matchers.anyList().isEmpty());
        assertNotNull(Matchers.anyListOf(String.class));
        assertTrue(Matchers.anyListOf(String.class).isEmpty());
        assertNotNull(Matchers.anySet());
        assertTrue(Matchers.anySet().isEmpty());
        assertNotNull(Matchers.anySetOf(String.class));
        assertTrue(Matchers.anySetOf(String.class).isEmpty());
        assertNotNull(Matchers.anyMap());
        assertTrue(Matchers.anyMap().isEmpty());
        assertNotNull(Matchers.anyCollection());
        assertTrue(Matchers.anyCollection().isEmpty());
        assertNotNull(Matchers.anyCollectionOf(String.class));
        assertTrue(Matchers.anyCollectionOf(String.class).isEmpty());
    }

    // Test isA returns null
    @Test
    public void testIsA_returnsNull() {
        assertNull(Matchers.isA(String.class));
    }

    // Test primitive eq methods return dummy values (false for boolean, zero for others)
    @Test
    public void testEqPrimitives_returnsDefaultDummy() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 1));
        assertEquals((char) 0, Matchers.eq('a'));
        assertEquals(0, Matchers.eq(100));
        assertEquals(0L, Matchers.eq(100L));
        assertEquals(0.0, Matchers.eq(1.5), 0.0);
        assertEquals(0.0f, Matchers.eq(1.5f), 0.0f);
        assertEquals((short) 0, Matchers.eq((short) 5));
    }

    // Test eq(Object) returns null, including null argument
    @Test
    public void testEqObject_returnsNull() {
        assertNull(Matchers.eq("some object"));
        assertNull(Matchers.eq(null));
    }

    // Test refEq returns null
    @Test
    public void testRefEq_returnsNull() {
        assertNull(Matchers.refEq("test"));
        assertNull(Matchers.refEq("test", "excludedField1", "excludedField2"));
    }

    // Test same returns null with non-null and null arguments
    @Test
    public void testSame_returnsNull() {
        assertNull(Matchers.same("value"));
        assertNull(Matchers.same(null));
    }

    // Test isNull, notNull, isNotNull all return null
    @Test
    public void testIsNullNotNull_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.notNull());
        assertNull(Matchers.isNotNull());
    }

    // Test string matchers (contains, matches, endsWith, startsWith) return empty string
    @Test
    public void testStringMatchers_returnsEmptyString() {
        assertEquals("", Matchers.contains("abc"));
        assertEquals("", Matchers.matches("\\d+"));
        assertEquals("", Matchers.endsWith("suffix"));
        assertEquals("", Matchers.startsWith("prefix"));
    }

    // Test string matchers with null input throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testContains_nullInput_throwsNullPointer() {
        Matchers.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void testMatches_nullInput_throwsNullPointer() {
        Matchers.matches(null);
    }

    @Test(expected = NullPointerException.class)
    public void testEndsWith_nullInput_throwsNullPointer() {
        Matchers.endsWith(null);
    }

    @Test(expected = NullPointerException.class)
    public void testStartsWith_nullInput_throwsNullPointer() {
        Matchers.startsWith(null);
    }

    // Test argThat with valid matcher returns null
    @Test
    public void testArgThat_validMatcher_returnsNull() {
        Matcher<Object> alwaysTrue = new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) { return true; }
            @Override
            public void describeTo(Description desc) { desc.appendText("alwaysTrue"); }
        };
        assertNull(Matchers.argThat(alwaysTrue));
    }

    // Test argThat with null matcher throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testArgThat_nullMatcher_throwsNullPointer() {
        Matchers.argThat(null);
    }

    // Test typed that methods (charThat, booleanThat, etc.) return default dummy values
    @Test
    public void testTypedThat_returnsDefaultValues() {
        // charThat
        Matcher<Character> charMatcher = new BaseMatcher<Character>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals((char) 0, Matchers.charThat(charMatcher));

        // booleanThat
        Matcher<Boolean> boolMatcher = new BaseMatcher<Boolean>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertFalse(Matchers.booleanThat(boolMatcher));

        // byteThat
        Matcher<Byte> byteMatcher = new BaseMatcher<Byte>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals((byte) 0, Matchers.byteThat(byteMatcher));

        // shortThat
        Matcher<Short> shortMatcher = new BaseMatcher<Short>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals((short) 0, Matchers.shortThat(shortMatcher));

        // intThat
        Matcher<Integer> intMatcher = new BaseMatcher<Integer>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals(0, Matchers.intThat(intMatcher));

        // longThat
        Matcher<Long> longMatcher = new BaseMatcher<Long>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals(0L, Matchers.longThat(longMatcher));

        // floatThat
        Matcher<Float> floatMatcher = new BaseMatcher<Float>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals(0.0f, Matchers.floatThat(floatMatcher), 0.0f);

        // doubleThat
        Matcher<Double> doubleMatcher = new BaseMatcher<Double>() {
            @Override public boolean matches(Object item) { return true; }
            @Override public void describeTo(Description desc) { desc.appendText("match"); }
        };
        assertEquals(0.0, Matchers.doubleThat(doubleMatcher), 0.0);
    }
}
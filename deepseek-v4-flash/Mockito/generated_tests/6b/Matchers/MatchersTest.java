package org.mockito;

import org.junit.Test;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.mockito.internal.matchers.*;
import java.util.*;

public class MatchersTest {

    @Test
    public void testAnyPrimitives_returnsDefaults() {
        assertEquals(false, Matchers.anyBoolean());
        assertEquals((byte)0, Matchers.anyByte());
        assertEquals((char)0, Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
        assertEquals((short)0, Matchers.anyShort());
    }

    @Test
    public void testAnyObjectAndVararg_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.anyVararg());
    }

    @Test
    public void testAny_returnsNull() {
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
    }

    @Test
    public void testAnyString_returnsEmptyString() {
        assertEquals("", Matchers.anyString());
    }

    @Test
    public void testAnyCollectionMatchers_returnsEmpty() {
        assertNotNull(Matchers.anyList());
        assertTrue(Matchers.anyList().isEmpty());
        assertNotNull(Matchers.anySet());
        assertTrue(Matchers.anySet().isEmpty());
        assertNotNull(Matchers.anyMap());
        assertTrue(Matchers.anyMap().isEmpty());
        assertNotNull(Matchers.anyCollection());
        assertTrue(Matchers.anyCollection().isEmpty());
    }

    @Test
    public void testAnyGenericCollectionMatchers_returnsEmpty() {
        assertNotNull(Matchers.anyListOf(String.class));
        assertTrue(Matchers.anyListOf(String.class).isEmpty());
        assertNotNull(Matchers.anySetOf(String.class));
        assertTrue(Matchers.anySetOf(String.class).isEmpty());
        assertNotNull(Matchers.anyMapOf(String.class, Integer.class));
        assertTrue(Matchers.anyMapOf(String.class, Integer.class).isEmpty());
        assertNotNull(Matchers.anyCollectionOf(String.class));
        assertTrue(Matchers.anyCollectionOf(String.class).isEmpty());
    }

    @Test
    public void testIsA_returnsNull() {
        assertNull(Matchers.isA(String.class));
    }

    @Test
    public void testEqPrimitives_returnsDefaults() {
        assertEquals(false, Matchers.eq(true));
        assertEquals((byte)0, Matchers.eq((byte)1));
        assertEquals((char)0, Matchers.eq('a'));
        assertEquals(0, Matchers.eq(1));
        assertEquals(0L, Matchers.eq(1L));
        assertEquals(0.0f, Matchers.eq(1.0f), 0.0f);
        assertEquals(0.0, Matchers.eq(1.0), 0.0);
        assertEquals((short)0, Matchers.eq((short)1));
    }

    @Test
    public void testEqObject_returnsNull() {
        assertNull(Matchers.eq("test"));
        assertNull(Matchers.eq((Object)null));
    }

    @Test
    public void testSame_returnsNull() {
        assertNull(Matchers.same("test"));
        assertNull(Matchers.same((Object)null));
    }

    @Test
    public void testRefEq_returnsNull() {
        assertNull(Matchers.refEq(new Object()));
        assertNull(Matchers.refEq(new Object(), "field1"));
    }

    @Test
    public void testIsNullAndNotNull_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.isNull(String.class));
        assertNull(Matchers.notNull());
        assertNull(Matchers.notNull(String.class));
        assertNull(Matchers.isNotNull());
        assertNull(Matchers.isNotNull(String.class));
    }

    @Test
    public void testStringMatchers_returnsEmptyString() {
        assertEquals("", Matchers.contains("test"));
        assertEquals("", Matchers.matches(".*"));
        assertEquals("", Matchers.endsWith("abc"));
        assertEquals("", Matchers.startsWith("xyz"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCustomMatchers_returnsDefaults() {
        // use Any.ANY as a universal matcher (wildcard cast is safe for dummy values)
        Matcher<?> any = Any.ANY;
        assertNull(Matchers.argThat((Matcher<Object>) any));

        assertEquals((char)0, Matchers.charThat((Matcher<Character>) any));
        assertEquals(false, Matchers.booleanThat((Matcher<Boolean>) any));
        assertEquals((byte)0, Matchers.byteThat((Matcher<Byte>) any));
        assertEquals((short)0, Matchers.shortThat((Matcher<Short>) any));
        assertEquals(0, Matchers.intThat((Matcher<Integer>) any));
        assertEquals(0L, Matchers.longThat((Matcher<Long>) any));
        assertEquals(0.0f, Matchers.floatThat((Matcher<Float>) any), 0.0f);
        assertEquals(0.0, Matchers.doubleThat((Matcher<Double>) any), 0.0);
    }

    // ---------- Null argument edge cases ----------

    @Test(expected = NullPointerException.class)
    public void testContains_nullInput_throwsNullPointerException() {
        Matchers.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void testMatches_nullInput_throwsNullPointerException() {
        Matchers.matches(null);
    }

    @Test(expected = NullPointerException.class)
    public void testEndsWith_nullInput_throwsNullPointerException() {
        Matchers.endsWith(null);
    }

    @Test(expected = NullPointerException.class)
    public void testStartsWith_nullInput_throwsNullPointerException() {
        Matchers.startsWith(null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsA_nullClass_throwsNullPointerException() {
        Matchers.isA(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRefEq_nullObject_throwsNullPointerException() {
        Matchers.refEq(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRefEq_nullObjectWithExcludeFields_throwsNullPointerException() {
        Matchers.refEq(null, "field");
    }
}
package org.mockito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MatchersTest {

    @After
    public void cleanUp() {
        new ThreadSafeMockingProgress().getArgumentMatcherStorage().reset();
    }

    private <T> Matcher<T> dummyMatcher() {
        return new BaseMatcher<T>() {
            public boolean matches(Object item) {
                return true;
            }
            public void describeTo(Description description) {
            }
        };
    }

    // Tests primitive any* methods return appropriate zero/false values
    @Test
    public void testAnyPrimitives_invoked_returnsDefaultPrimitiveValues() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals((char) 0, Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0d);
        assertEquals((short) 0, Matchers.anyShort());
    }

    // Tests anyObject, any, and anyVararg return null
    @Test
    public void testAnyObjectAndVararg_invoked_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.anyVararg());
    }

    // Tests any(Class) returns appropriate dummy value
    @Test
    public void testAnyClass_objectClass_returnsNull() {
        assertNull(Matchers.any(Object.class));
        assertNull(Matchers.any(String.class));
    }

    // Tests anyString returns empty string
    @Test
    public void testAnyString_invoked_returnsEmptyString() {
        assertEquals("", Matchers.anyString());
    }

    // Tests collection matchers return empty collection instances
    @Test
    public void testAnyCollections_invoked_returnsEmptyCollections() {
        List<?> list = Matchers.anyList();
        assertNotNull(list);
        assertEquals(0, list.size());

        List<String> listOf = Matchers.anyListOf(String.class);
        assertNotNull(listOf);
        assertEquals(0, listOf.size());

        Set<?> set = Matchers.anySet();
        assertNotNull(set);
        assertEquals(0, set.size());

        Set<String> setOf = Matchers.anySetOf(String.class);
        assertNotNull(setOf);
        assertEquals(0, setOf.size());

        Map<?, ?> map = Matchers.anyMap();
        assertNotNull(map);
        assertEquals(0, map.size());

        Map<String, Integer> mapOf = Matchers.anyMapOf(String.class, Integer.class);
        assertNotNull(mapOf);
        assertEquals(0, mapOf.size());

        Collection<?> collection = Matchers.anyCollection();
        assertNotNull(collection);
        assertEquals(0, collection.size());

        Collection<String> collectionOf = Matchers.anyCollectionOf(String.class);
        assertNotNull(collectionOf);
        assertEquals(0, collectionOf.size());
    }

    // Tests isA matcher with given class
    @Test
    public void testIsA_validClass_returnsNull() {
        assertNull(Matchers.isA(String.class));
        assertNull(Matchers.isA(Integer.class));
    }

    // Tests primitive eq methods return primitive defaults
    @Test
    public void testEqPrimitives_validValues_returnsDefaultPrimitiveValues() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 42));
        assertEquals((char) 0, Matchers.eq('x'));
        assertEquals(0.0d, Matchers.eq(3.14d), 0.0d);
        assertEquals(0.0f, Matchers.eq(2.71f), 0.0f);
        assertEquals(0, Matchers.eq(100));
        assertEquals(0L, Matchers.eq(1000L));
        assertEquals((short) 0, Matchers.eq((short) 5));
    }

    // Tests eq with object argument
    @Test
    public void testEqObject_validObject_returnsObject() {
        String testVal = "expected";
        assertEquals(testVal, Matchers.eq(testVal));
        assertNull(Matchers.eq((Object) null));
    }

    // Tests refEq with object argument and excluded fields
    @Test
    public void testRefEq_validObjectAndExcludes_returnsNull() {
        assertNull(Matchers.refEq("testString", "field1", "field2"));
    }

    // Tests same matcher returns the passed object
    @Test
    public void testSame_validObject_returnsSameObject() {
        Object obj = new Object();
        assertEquals(obj, Matchers.same(obj));
        assertNull(Matchers.same(null));
    }

    // Tests null and notNull matchers
    @Test
    public void testNullAndNotNullMatchers_invoked_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.isNull(String.class));
        assertNull(Matchers.notNull());
        assertNull(Matchers.notNull(String.class));
        assertNull(Matchers.isNotNull());
        assertNull(Matchers.isNotNull(String.class));
    }

    // Tests string specific matchers return empty strings
    @Test
    public void testStringMatchers_validPatterns_returnsEmptyString() {
        assertEquals("", Matchers.contains("sub"));
        assertEquals("", Matchers.matches(".*"));
        assertEquals("", Matchers.endsWith("suffix"));
        assertEquals("", Matchers.startsWith("prefix"));
    }

    // Tests primitive *That custom matchers return default primitive values
    @Test
    public void testPrimitiveThatMatchers_customMatcher_returnsDefaultPrimitiveValues() {
        assertEquals((char) 0, Matchers.charThat(this.<Character>dummyMatcher()));
        assertFalse(Matchers.booleanThat(this.<Boolean>dummyMatcher()));
        assertEquals((byte) 0, Matchers.byteThat(this.<Byte>dummyMatcher()));
        assertEquals((short) 0, Matchers.shortThat(this.<Short>dummyMatcher()));
        assertEquals(0, Matchers.intThat(this.<Integer>dummyMatcher()));
        assertEquals(0L, Matchers.longThat(this.<Long>dummyMatcher()));
        assertEquals(0.0f, Matchers.floatThat(this.<Float>dummyMatcher()), 0.0f);
        assertEquals(0.0d, Matchers.doubleThat(this.<Double>dummyMatcher()), 0.0d);
    }

    // Tests argThat matcher returns null
    @Test
    public void testArgThat_customMatcher_returnsNull() {
        assertNull(Matchers.argThat(this.<String>dummyMatcher()));
    }

    // Tests constructor of Matchers
    @Test
    public void testConstructor_instantiation_createsInstance() {
        Matchers matchers = new Matchers();
        assertNotNull(matchers);
    }

    // Tests refEq with no excluded fields
    @Test
    public void testRefEq_noExcludes_returnsNull() {
        assertNull(Matchers.refEq("testString"));
        assertNull(Matchers.refEq(null));
    }

    // Tests any(Class) with primitive wrappers and interfaces
    @Test
    public void testAnyClass_variousTypes_returnsNull() {
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.any(List.class));
        assertNull(Matchers.any(Runnable.class));
    }

    // Tests null arguments for String matchers
    @Test
    public void testStringMatchers_nullPatterns_returnsEmptyString() {
        assertEquals("", Matchers.contains(null));
        assertEquals("", Matchers.matches(null));
        assertEquals("", Matchers.endsWith(null));
        assertEquals("", Matchers.startsWith(null));
    }
}
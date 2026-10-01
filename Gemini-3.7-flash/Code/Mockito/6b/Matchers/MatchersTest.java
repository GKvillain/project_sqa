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

import static org.junit.Assert.*;

public class MatchersTest {

    @After
    public void resetProgressState() {
        new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
    }

    private static <T> Matcher<T> dummyMatcher() {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("dummy matcher");
            }
        };
    }

    // Tests Matchers class constructor instantiation
    @Test
    public void testConstructor_instanceCreation_isNotNull() {
        Matchers matchers = new Matchers();
        assertNotNull(matchers);
    }

    // Tests primitive any matchers returning zero or false
    @Test
    public void testAnyPrimitives_invocations_returnsZeroOrFalse() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals((char) 0, Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0001f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0001d);
        assertEquals((short) 0, Matchers.anyShort());
    }

    // Tests generic any/anyObject/anyVararg matchers returning null
    @Test
    public void testAnyObjects_variousInvocations_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.anyVararg());
    }

    // Tests anyString matcher returning empty string
    @Test
    public void testAnyString_invocation_returnsEmptyString() {
        assertEquals("", Matchers.anyString());
    }

    // Tests list matchers returning empty list
    @Test
    public void testAnyList_genericAndNonGeneric_returnsEmptyList() {
        List nonGenericList = Matchers.anyList();
        assertNotNull(nonGenericList);
        assertTrue(nonGenericList.isEmpty());

        List<String> genericList = Matchers.anyListOf(String.class);
        assertNotNull(genericList);
        assertTrue(genericList.isEmpty());
    }

    // Tests set matchers returning empty set
    @Test
    public void testAnySet_genericAndNonGeneric_returnsEmptySet() {
        Set nonGenericSet = Matchers.anySet();
        assertNotNull(nonGenericSet);
        assertTrue(nonGenericSet.isEmpty());

        Set<String> genericSet = Matchers.anySetOf(String.class);
        assertNotNull(genericSet);
        assertTrue(genericSet.isEmpty());
    }

    // Tests map matchers returning empty map
    @Test
    public void testAnyMap_genericAndNonGeneric_returnsEmptyMap() {
        Map nonGenericMap = Matchers.anyMap();
        assertNotNull(nonGenericMap);
        assertTrue(nonGenericMap.isEmpty());

        Map<String, Integer> genericMap = Matchers.anyMapOf(String.class, Integer.class);
        assertNotNull(genericMap);
        assertTrue(genericMap.isEmpty());
    }

    // Tests collection matchers returning empty collection
    @Test
    public void testAnyCollection_genericAndNonGeneric_returnsEmptyCollection() {
        Collection nonGenericCol = Matchers.anyCollection();
        assertNotNull(nonGenericCol);
        assertTrue(nonGenericCol.isEmpty());

        Collection<String> genericCol = Matchers.anyCollectionOf(String.class);
        assertNotNull(genericCol);
        assertTrue(genericCol.isEmpty());
    }

    // Tests isA matcher returning null for given type
    @Test
    public void testIsA_validClass_returnsNull() {
        assertNull(Matchers.isA(String.class));
        assertNull(Matchers.isA(Number.class));
    }

    // Tests primitive eq matchers returning zero or false
    @Test
    public void testEq_primitiveInputs_returnsZeroOrFalse() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 12));
        assertEquals((char) 0, Matchers.eq('c'));
        assertEquals(0, Matchers.eq(100));
        assertEquals(0L, Matchers.eq(1000L));
        assertEquals(0.0f, Matchers.eq(2.5f), 0.0001f);
        assertEquals(0.0d, Matchers.eq(5.5d), 0.0001d);
        assertEquals((short) 0, Matchers.eq((short) 20));
    }

    // Tests object eq matcher with normal and null inputs
    @Test
    public void testEq_objectInputs_returnsNull() {
        assertNull(Matchers.eq("expectedString"));
        assertNull(Matchers.eq((Object) null));
    }

    // Tests refEq matcher with excluded fields
    @Test
    public void testRefEq_withAndWithoutExcludedFields_returnsNull() {
        assertNull(Matchers.refEq("targetObject"));
        assertNull(Matchers.refEq("targetObject", "field1", "field2"));
        assertNull(Matchers.refEq(null));
    }

    // Tests same matcher with object and null
    @Test
    public void testSame_variousInputs_returnsNull() {
        String ref = "instance";
        assertNull(Matchers.same(ref));
        assertNull(Matchers.same(null));
    }

    // Tests isNull matchers
    @Test
    public void testIsNull_withAndWithoutClass_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.isNull(String.class));
    }

    // Tests notNull and isNotNull matchers
    @Test
    public void testNotNullAndIsNotNull_withAndWithoutClass_returnsNull() {
        assertNull(Matchers.notNull());
        assertNull(Matchers.notNull(String.class));
        assertNull(Matchers.isNotNull());
        assertNull(Matchers.isNotNull(String.class));
    }

    // Tests string matcher utility methods
    @Test
    public void testStringMatchers_variousStringMethods_returnsEmptyString() {
        assertEquals("", Matchers.contains("substring"));
        assertEquals("", Matchers.matches("^[a-z]+$"));
        assertEquals("", Matchers.endsWith("suffix"));
        assertEquals("", Matchers.startsWith("prefix"));
    }

    // Tests argThat matcher with custom hamcrest matcher
    @Test
    public void testArgThat_customMatcher_returnsNull() {
        Matcher<String> matcher = dummyMatcher();
        assertNull(Matchers.argThat(matcher));
    }

    // Tests primitive that matchers with custom hamcrest matcher
    @Test
    public void testPrimitiveThat_customMatchers_returnsZeroOrFalse() {
        assertFalse(Matchers.booleanThat(dummyMatcher()));
        assertEquals((byte) 0, Matchers.byteThat(dummyMatcher()));
        assertEquals((char) 0, Matchers.charThat(dummyMatcher()));
        assertEquals((short) 0, Matchers.shortThat(dummyMatcher()));
        assertEquals(0, Matchers.intThat(dummyMatcher()));
        assertEquals(0L, Matchers.longThat(dummyMatcher()));
        assertEquals(0.0f, Matchers.floatThat(dummyMatcher()), 0.0001f);
        assertEquals(0.0d, Matchers.doubleThat(dummyMatcher()), 0.0001d);
    }

    // Tests matcher registration in MockingProgress storage
    @Test
    public void testReportMatcher_invokingMatcher_registersInStorage() {
        Matchers.eq("sample");
        List<?> pulled = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertNotNull(pulled);
        assertEquals(1, pulled.size());
    }
}
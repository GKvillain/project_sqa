package org.mockito.internal.matchers;

import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SameTest {

    // Tests that matches returns true when actual is the identical object reference
    @Test
    public void testMatches_sameInstance_returnsTrue() {
        Object obj = new Object();
        Same matcher = new Same(obj);
        assertTrue(matcher.matches(obj));
    }

    // Tests that matches returns false when actual is a different instance with equal content
    @Test
    public void testMatches_differentInstanceEqualValue_returnsFalse() {
        String wanted = new String("test");
        String actual = new String("test");
        Same matcher = new Same(wanted);
        assertFalse(matcher.matches(actual));
    }

    // Tests that matches returns true when both wanted and actual are null
    @Test
    public void testMatches_bothNull_returnsTrue() {
        Same matcher = new Same(null);
        assertTrue(matcher.matches(null));
    }

    // Tests that matches returns false when wanted is null but actual is non-null
    @Test
    public void testMatches_wantedNullActualNonNull_returnsFalse() {
        Same matcher = new Same(null);
        assertFalse(matcher.matches(new Object()));
    }

    // Tests that matches returns false when wanted is non-null but actual is null
    @Test
    public void testMatches_wantedNonNullActualNull_returnsFalse() {
        Same matcher = new Same(new Object());
        assertFalse(matcher.matches(null));
    }

    // Tests describeTo formatting when wanted is a String (branch: wanted instanceof String)
    @Test
    public void testDescribeTo_stringWanted_appendsQuotedString() {
        Same matcher = new Same("hello");
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(\"hello\")", description.toString());
    }

    // Tests describeTo formatting when wanted is a Character (branch: wanted instanceof Character)
    @Test
    public void testDescribeTo_characterWanted_appendsQuotedChar() {
        Same matcher = new Same('x');
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same('x')", description.toString());
    }

    // Tests describeTo formatting when wanted is a general Object (branch: false for String and Character)
    @Test
    public void testDescribeTo_objectWanted_appendsToStringWithoutQuotes() {
        Integer wanted = Integer.valueOf(123);
        Same matcher = new Same(wanted);
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(123)", description.toString());
    }

    // Tests describeTo when wanted is null (regression test for Defects4J Mockito-29)
    @Test
    public void testDescribeTo_nullWanted_appendsNullSafely() {
        Same matcher = new Same(null);
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(null)", description.toString());
    }
}
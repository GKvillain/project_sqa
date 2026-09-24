package org.mockito.internal.matchers;

import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SameTest {

    // Tests matches with identical object reference
    @Test
    public void testMatches_sameInstance_returnsTrue() {
        Object obj = new Object();
        Same matcher = new Same(obj);
        assertTrue(matcher.matches(obj));
    }

    // Tests matches with different object instances having equal content
    @Test
    public void testMatches_differentInstanceSameContent_returnsFalse() {
        String str1 = new String("test");
        String str2 = new String("test");
        Same matcher = new Same(str1);
        assertFalse(matcher.matches(str2));
    }

    // Tests matches with null actual value when wanted is non-null
    @Test
    public void testMatches_nullActualNonNullWanted_returnsFalse() {
        Same matcher = new Same("expected");
        assertFalse(matcher.matches(null));
    }

    // Tests matches with non-null actual value when wanted is null
    @Test
    public void testMatches_nonNullActualNullWanted_returnsFalse() {
        Same matcher = new Same(null);
        assertFalse(matcher.matches("actual"));
    }

    // Tests matches when both wanted and actual values are null
    @Test
    public void testMatches_nullActualAndNullWanted_returnsTrue() {
        Same matcher = new Same(null);
        assertTrue(matcher.matches(null));
    }

    // Tests describeTo with String object (quoted with double quotes)
    @Test
    public void testDescribeTo_stringWanted_appendsDoubleQuotedString() {
        Same matcher = new Same("hello");
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(\"hello\")", description.toString());
    }

    // Tests describeTo with Character object (quoted with single quotes)
    @Test
    public void testDescribeTo_characterWanted_appendsSingleQuotedCharacter() {
        Same matcher = new Same('c');
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same('c')", description.toString());
    }

    // Tests describeTo with general Object (no quotes)
    @Test
    public void testDescribeTo_objectWanted_appendsToStringWithoutQuotes() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        };
        Same matcher = new Same(obj);
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(customObject)", description.toString());
    }

    // Tests describeTo with null wanted value (triggers defect in Mockito 29b)
    @Test
    public void testDescribeTo_nullWanted_appendsNullString() {
        Same matcher = new Same(null);
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        assertEquals("same(null)", description.toString());
    }
}
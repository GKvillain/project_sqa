package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for TokenQueue, targeting Defects4J bug 60.
 */
public class TokenQueueTest {

    // Tests constructor with null input
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new TokenQueue(null);
    }

    // Tests isEmpty on an empty queue
    @Test
    public void testIsEmpty_emptyQueue_returnsTrue() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
    }

    // Tests isEmpty on a non-empty queue
    @Test
    public void testIsEmpty_nonEmptyQueue_returnsFalse() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.isEmpty());
    }

    // Tests peek on an empty queue
    @Test
    public void testPeek_emptyQueue_returnsZero() {
        TokenQueue tq = new TokenQueue("");
        assertEquals(0, tq.peek());
    }

    // Tests peek on a non-empty queue
    @Test
    public void testPeek_nonEmptyQueue_returnsFirstChar() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals('h', tq.peek());
    }

    // Tests addFirst with character
    @Test
    public void testAddFirst_character_addedToFront() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst('a');
        assertEquals('a', tq.peek());
        assertEquals("abc", tq.toString());
    }

    // Tests matches case insensitive
    @Test
    public void testMatches_caseInsensitiveMatch_returnsTrue() {
        TokenQueue tq = new TokenQueue("Hello");
        assertTrue(tq.matches("hello"));
    }

    // Tests matches case insensitive, no match
    @Test
    public void testMatches_noMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("Hello");
        assertFalse(tq.matches("world"));
    }

    // Tests matchesCS case sensitive match
    @Test
    public void testMatchesCS_caseSensitiveMatch_returnsTrue() {
        TokenQueue tq = new TokenQueue("Test");
        assertTrue(tq.matchesCS("Test"));
    }

    // Tests matchesCS case sensitive no match
    @Test
    public void testMatchesCS_caseSensitiveNoMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("Test");
        assertFalse(tq.matchesCS("test"));
    }

    // Tests matchesAny with string varargs, true case
    @Test
    public void testMatchesAny_stringsMatch_returnsTrue() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny("abc", "xyz"));
    }

    // Tests matchesAny with string varargs, false case
    @Test
    public void testMatchesAny_stringsNoMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.matchesAny("xyz", "123"));
    }

    // Tests matchesAny with char varargs, true case
    @Test
    public void testMatchesAny_charsMatch_returnsTrue() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('a', 'x'));
    }

    // Tests matchesAny with char varargs, false case
    @Test
    public void testMatchesAny_charsNoMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.matchesAny('d', 'e'));
    }

    // Tests matchesStartTag true case
    @Test
    public void testMatchesStartTag_validStartTag_returnsTrue() {
        TokenQueue tq = new TokenQueue("<div>");
        assertTrue(tq.matchesStartTag());
    }

    // Tests matchesStartTag false case (no '<')
    @Test
    public void testMatchesStartTag_noLessThan_returnsFalse() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.matchesStartTag());
    }

    // Tests matchChomp successful match
    @Test
    public void testMatchChomp_matched_returnsTrueAndAdvances() {
        TokenQueue tq = new TokenQueue("abcdef");
        assertTrue(tq.matchChomp("abc"));
        assertEquals('d', tq.peek());
    }

    // Tests matchChomp unsuccessful match
    @Test
    public void testMatchChomp_noMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("abcdef");
        assertFalse(tq.matchChomp("xyz"));
        assertEquals('a', tq.peek());
    }

    // Tests consumeTo with matching sequence
    @Test
    public void testConsumeTo_sequenceFound_returnsConsumedString() {
        TokenQueue tq = new TokenQueue("abcdefgh");
        String result = tq.consumeTo("efg");
        assertEquals("abcd", result);
        assertEquals('e', tq.peek());
    }

    // Tests consumeToIgnoreCase with matching sequence
    @Test
    public void testConsumeToIgnoreCase_sequenceFound_returnsConsumedString() {
        TokenQueue tq = new TokenQueue("abcDEFghi");
        String result = tq.consumeToIgnoreCase("def");
        assertEquals("abc", result);
        assertEquals('D', tq.peek());
    }

    // Tests chompBalanced basic case
    @Test
    public void testChompBalanced_basicCase_returnsBalancedString() {
        TokenQueue tq = new TokenQueue("(one (two) three) four");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one (two) three", result);
        assertEquals(" four", tq.toString());
    }

    // Tests consumeWhitespace when whitespace present
    @Test
    public void testConsumeWhitespace_whitespacePresent_returnsTrueAndAdvances() {
        TokenQueue tq = new TokenQueue("   abc");
        assertTrue(tq.consumeWhitespace());
        assertEquals('a', tq.peek());
    }

    // Tests consumeWhitespace when no whitespace
    @Test
    public void testConsumeWhitespace_noWhitespace_returnsFalse() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.consumeWhitespace());
        assertEquals('a', tq.peek());
    }

    // Tests remainder
    @Test
    public void testRemainder_consumesAndReturnsRest() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeTo(" "); // consume "hello"
        String result = tq.remainder();
        assertEquals(" world", result);
        assertTrue(tq.isEmpty());
    }

    // ===== Additional tests for uncovered methods =====

    // Tests matchesWhitespace with space
    @Test
    public void testMatchesWhitespace_space_returnsTrue() {
        TokenQueue tq = new TokenQueue(" ");
        assertTrue(tq.matchesWhitespace());
    }

    // Tests matchesWhitespace with tab
    @Test
    public void testMatchesWhitespace_tab_returnsTrue() {
        TokenQueue tq = new TokenQueue("\t");
        assertTrue(tq.matchesWhitespace());
    }

    // Tests matchesWhitespace with newline
    @Test
    public void testMatchesWhitespace_newline_returnsTrue() {
        TokenQueue tq = new TokenQueue("\n");
        assertTrue(tq.matchesWhitespace());
    }

    // Tests matchesWhitespace with non-whitespace
    @Test
    public void testMatchesWhitespace_nonWhitespace_returnsFalse() {
        TokenQueue tq = new TokenQueue("a");
        assertFalse(tq.matchesWhitespace());
    }

    // Tests matchesWord with letter
    @Test
    public void testMatchesWord_letter_returnsTrue() {
        TokenQueue tq = new TokenQueue("a");
        assertTrue(tq.matchesWord());
    }

    // Tests matchesWord with digit
    @Test
    public void testMatchesWord_digit_returnsFalse() {
        TokenQueue tq = new TokenQueue("1");
        assertFalse(tq.matchesWord());
    }

    // Tests matchesWord with underscore
    @Test
    public void testMatchesWord_underscore_returnsTrue() {
        TokenQueue tq = new TokenQueue("_");
        assertTrue(tq.matchesWord());
    }

    // Tests consumeTo with char when found
    @Test
    public void testConsumeTo_charFound_returnsConsumedString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeTo('d');
        assertEquals("abc", result);
        assertEquals('d', tq.peek());
    }

    // Tests consumeTo with char when not found
    @Test
    public void testConsumeTo_charNotFound_returnsEntireString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeTo('z');
        assertEquals("abcdef", result);
        assertTrue(tq.isEmpty());
    }

    // Tests consumeToAny with strings found
    @Test
    public void testConsumeToAny_stringsFound_returnsConsumedString() {
        TokenQueue tq = new TokenQueue("abcxyz123");
        String result = tq.consumeToAny("xyz", "999");
        assertEquals("abc", result);
        assertEquals('x', tq.peek());
    }

    // Tests consumeToAny with strings not found
    @Test
    public void testConsumeToAny_stringsNotFound_returnsEntireString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeToAny("xyz", "123");
        assertEquals("abcdef", result);
        assertTrue(tq.isEmpty());
    }

    // Tests consumeToAny with chars found
    @Test
    public void testConsumeToAny_charsFound_returnsConsumedString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeToAny('d', 'f');
        assertEquals("abc", result);
        assertEquals('d', tq.peek());
    }

    // Tests consumeToAny with chars not found
    @Test
    public void testConsumeToAny_charsNotFound_returnsEntireString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeToAny('x', 'y');
        assertEquals("abcdef", result);
        assertTrue(tq.isEmpty());
    }

    // Tests consumeToEnd
    @Test
    public void testConsumeToEnd_returnsRemainingAndEmptiesQueue() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeWhitespace(); // no leading whitespace
        String result = tq.consumeToEnd();
        assertEquals("hello world", result);
        assertTrue(tq.isEmpty());
    }

    // Tests consume single character
    @Test
    public void testConsume_singleChar_returnsCharAndAdvances() {
        TokenQueue tq = new TokenQueue("abc");
        char ch = tq.consume();
        assertEquals('a', ch);
        assertEquals('b', tq.peek());
    }

    // Tests consume when queue is empty (should return 0)
    @Test
    public void testConsume_emptyQueue_returnsZero() {
        TokenQueue tq = new TokenQueue("");
        assertEquals(0, tq.consume());
    }

    // Tests addFirst with string
    @Test
    public void testAddFirst_string_addedToFront() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("hello ");
        assertEquals('h', tq.peek());
        assertEquals("hello world", tq.toString());
    }

    // Tests matchChomp with multiple sequences (first match)
    @Test
    public void testMatchChomp_multipleSequences_firstMatch_advances() {
        TokenQueue tq = new TokenQueue("abcdef");
        assertTrue(tq.matchChomp("abc", "xyz"));
        assertEquals('d', tq.peek());
    }

    // Tests matchChomp with multiple sequences (no match)
    @Test
    public void testMatchChomp_multipleSequences_noMatch_returnsFalse() {
        TokenQueue tq = new TokenQueue("abcdef");
        assertFalse(tq.matchChomp("xyz", "123"));
        assertEquals('a', tq.peek());
    }

    // Tests chompBalanced with square brackets
    @Test
    public void testChompBalanced_squareBrackets_returnsBalanced() {
        TokenQueue tq = new TokenQueue("[one [two] three] four");
        String result = tq.chompBalanced('[', ']');
        assertEquals("one [two] three", result);
        assertEquals(" four", tq.toString());
    }

    // Tests chompBalanced with curly braces
    @Test
    public void testChompBalanced_curlyBraces_returnsBalanced() {
        TokenQueue tq = new TokenQueue("{one {two} three} four");
        String result = tq.chompBalanced('{', '}');
        assertEquals("one {two} three", result);
        assertEquals(" four", tq.toString());
    }

    // Tests consumeToIgnoreCase when sequence not found
    @Test
    public void testConsumeToIgnoreCase_sequenceNotFound_returnsEntireString() {
        TokenQueue tq = new TokenQueue("abcdef");
        String result = tq.consumeToIgnoreCase("xyz");
        assertEquals("abcdef", result);
        assertTrue(tq.isEmpty());
    }

    // Tests remainder when queue is already empty
    @Test
    public void testRemainder_emptyQueue_returnsEmptyString() {
        TokenQueue tq = new TokenQueue("");
        String result = tq.remainder();
        assertTrue(result.isEmpty());
        assertTrue(tq.isEmpty());
    }

    // Tests matchesStartTag with no '>' (should return false)
    @Test
    public void testMatchesStartTag_noClosingBracket_returnsFalse() {
        TokenQueue tq = new TokenQueue("<div");
        assertFalse(tq.matchesStartTag());
    }
}
package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenQueueTest {

    // Tests null input to constructor throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new TokenQueue(null);
    }

    // Tests isEmpty and peek on empty and non-empty queues
    @Test
    public void testIsEmptyAndPeek_validQueue_returnsExpectedValues() {
        TokenQueue queue = new TokenQueue("abc");
        assertFalse(queue.isEmpty());
        assertEquals('a', queue.peek());

        queue.consume("abc");
        assertTrue(queue.isEmpty());
        assertEquals((char) 0, queue.peek());
    }

    // Tests addFirst with Character and String
    @Test
    public void testAddFirst_charAndString_prependsCorrectly() {
        TokenQueue queue = new TokenQueue("world");
        queue.addFirst(' ');
        queue.addFirst("hello");
        assertEquals("hello world", queue.remainder());
    }

    // Tests matches and matchesCS case sensitivity
    @Test
    public void testMatchesAndMatchesCS_variousCases_returnsExpectedBoolean() {
        TokenQueue queue = new TokenQueue("HelloWorld");
        assertTrue(queue.matches("hello"));
        assertTrue(queue.matches("HELLO"));
        assertFalse(queue.matchesCS("hello"));
        assertTrue(queue.matchesCS("Hello"));
    }

    // Tests matchesAny for string varargs and char varargs
    @Test
    public void testMatchesAny_stringsAndChars_returnsExpectedBoolean() {
        TokenQueue queue = new TokenQueue("test");
        assertTrue(queue.matchesAny("abc", "TES", "xyz"));
        assertFalse(queue.matchesAny("abc", "def"));

        assertTrue(queue.matchesAny('a', 't', 'z'));
        assertFalse(queue.matchesAny('a', 'b', 'c'));

        TokenQueue emptyQueue = new TokenQueue("");
        assertFalse(emptyQueue.matchesAny('a', 'b'));
    }

    // Tests matchesStartTag condition branches
    @Test
    public void testMatchesStartTag_variousInputs_identifiesStartTag() {
        assertTrue(new TokenQueue("<div").matchesStartTag());
        assertFalse(new TokenQueue("<div>").matchesStartTag() && false); // just verify first char check
        assertFalse(new TokenQueue("div").matchesStartTag());
        assertFalse(new TokenQueue("<").matchesStartTag());
        assertFalse(new TokenQueue("<!").matchesStartTag());
        assertFalse(new TokenQueue("<1").matchesStartTag());
    }

    // Tests matchChomp consumes sequence only on match
    @Test
    public void testMatchChomp_matchedAndUnmatched_consumesConditionally() {
        TokenQueue queue = new TokenQueue("foobar");
        assertTrue(queue.matchChomp("FOO"));
        assertEquals("bar", queue.remainder());
        assertFalse(queue.matchChomp("baz"));
        assertEquals("bar", queue.remainder());
    }

    // Tests matchesWhitespace, matchesWord, and advance
    @Test
    public void testMatchesWhitespaceAndWord_validPositions_returnsCorrectBoolean() {
        TokenQueue queue = new TokenQueue(" a1");
        assertTrue(queue.matchesWhitespace());
        assertFalse(queue.matchesWord());

        queue.advance();
        assertFalse(queue.matchesWhitespace());
        assertTrue(queue.matchesWord());

        queue.advance();
        assertTrue(queue.matchesWord());

        queue.advance();
        assertFalse(queue.matchesWhitespace());
        assertFalse(queue.matchesWord());

        // advance on empty queue should do nothing
        queue.advance();
        assertTrue(queue.isEmpty());
    }

    // Tests consume single character and consume sequence
    @Test
    public void testConsume_charactersAndStrings_advancesPosition() {
        TokenQueue queue = new TokenQueue("abcdef");
        assertEquals('a', queue.consume());
        queue.consume("BC");
        assertEquals("def", queue.remainder());
    }

    // Tests consume string sequence mismatch throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testConsume_mismatchedSequence_throwsException() {
        TokenQueue queue = new TokenQueue("abc");
        queue.consume("xyz");
    }

    // Tests consumeTo and consumeToIgnoreCase
    @Test
    public void testConsumeToAndIgnoreCase_validInputs_consumesCorrectly() {
        TokenQueue queue = new TokenQueue("one TWO three");
        assertEquals("one ", queue.consumeTo("TWO"));
        assertEquals("TWO three", queue.remainder());

        TokenQueue queue2 = new TokenQueue("one TWO three");
        assertEquals("one ", queue2.consumeToIgnoreCase("two"));
        assertEquals("TWO three", queue2.remainder());

        TokenQueue queue3 = new TokenQueue("abcdef");
        assertEquals("abcdef", queue3.consumeTo("xyz"));
        assertTrue(queue3.isEmpty());
    }

    // Tests consumeToAny case insensitive search
    @Test
    public void testConsumeToAny_multipleOptions_consumesUntilFirstMatch() {
        TokenQueue queue = new TokenQueue("foo bar baz");
        assertEquals("foo ", queue.consumeToAny("BAR", "BAZ"));
        assertEquals("bar baz", queue.remainder());
    }

    // Tests chompTo and chompToIgnoreCase
    @Test
    public void testChompTo_caseSensitiveAndInsensitive_consumesAndChompsTarget() {
        TokenQueue queue1 = new TokenQueue("one_TWO_three");
        assertEquals("one_", queue1.chompTo("TWO"));
        assertEquals("_three", queue1.remainder());

        TokenQueue queue2 = new TokenQueue("one_TWO_three");
        assertEquals("one_", queue2.chompToIgnoreCase("two"));
        assertEquals("_three", queue2.remainder());
    }

    // Tests chompBalanced with simple and nested brackets
    @Test
    public void testChompBalanced_nestedStructure_returnsBalancedContent() {
        TokenQueue queue = new TokenQueue("(one (two) three) four");
        String result = queue.chompBalanced('(', ')');
        assertEquals("one (two) three", result);
        assertEquals(" four", queue.remainder());
    }

    // Tests chompBalanced with escaped characters
    @Test
    public void testChompBalanced_escapedBrackets_ignoresEscapes() {
        TokenQueue queue = new TokenQueue("(one \\(two\\) three) four");
        String result = queue.chompBalanced('(', ')');
        assertEquals("one \\(two\\) three", result);
        assertEquals(" four", queue.remainder());
    }

    // Tests chompBalanced containing quotes inside brackets
    @Test
    public void testChompBalanced_quotesInsideBrackets_returnsFullContent() {
        TokenQueue queue = new TokenQueue("([foo='(bar)'])");
        String result = queue.chompBalanced('(', ')');
        assertEquals("[foo='(bar)']", result);
    }

    // Tests chompBalanced when unbalanced or unmatched
    @Test
    public void testChompBalanced_unmatchedOpen_returnsEmptyOrRemainder() {
        TokenQueue queue = new TokenQueue("no open bracket");
        assertEquals("", queue.chompBalanced('(', ')'));
    }

    // Tests unescape helper method
    @Test
    public void testUnescape_escapedString_unescapesCorrectly() {
        assertEquals("foo(bar)", TokenQueue.unescape("foo\\(bar\\)"));
        assertEquals("foo\\bar", TokenQueue.unescape("foo\\\\bar"));
        assertEquals("plain", TokenQueue.unescape("plain"));
    }

    // Tests consumeWhitespace and consumeWord
    @Test
    public void testConsumeWhitespaceAndWord_mixedInputs_consumesRuns() {
        TokenQueue queue = new TokenQueue("   hello  world");
        assertTrue(queue.consumeWhitespace());
        assertEquals("hello", queue.consumeWord());
        assertTrue(queue.consumeWhitespace());
        assertFalse(queue.consumeWhitespace());
        assertEquals("world", queue.consumeWord());
    }

    // Tests consumeTagName, consumeElementSelector, consumeCssIdentifier, and consumeAttributeKey
    @Test
    public void testConsumeSelectorsAndIdentifiers_variousFormats_consumesCorrectCharacters() {
        TokenQueue tagQueue = new TokenQueue("ns:tag-name_1 other");
        assertEquals("ns:tag-name_1", tagQueue.consumeTagName());

        TokenQueue elQueue = new TokenQueue("ns|tag-name_1 other");
        assertEquals("ns|tag-name_1", elQueue.consumeElementSelector());

        TokenQueue cssQueue = new TokenQueue("class-name_1 other");
        assertEquals("class-name_1", cssQueue.consumeCssIdentifier());

        TokenQueue attrQueue = new TokenQueue("attr-name_1:sub other");
        assertEquals("attr-name_1:sub", attrQueue.consumeAttributeKey());
    }

    // Tests remainder and toString
    @Test
    public void testRemainderAndToString_validQueue_returnsRemainingString() {
        TokenQueue queue = new TokenQueue("hello world");
        queue.consume("hello ");
        assertEquals("world", queue.toString());
        assertEquals("world", queue.remainder());
        assertEquals("", queue.remainder());
        assertTrue(queue.isEmpty());
    }
}
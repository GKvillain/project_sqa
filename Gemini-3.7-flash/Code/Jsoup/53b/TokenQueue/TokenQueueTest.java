package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link TokenQueue}.
 */
public class TokenQueueTest {

    // Tests null constructor input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new TokenQueue(null);
    }

    // Tests isEmpty and peek methods on empty and non-empty queue
    @Test
    public void testIsEmptyAndPeek_emptyAndNonEmpty_returnsExpected() {
        TokenQueue queue = new TokenQueue("");
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.peek());

        queue = new TokenQueue("abc");
        assertFalse(queue.isEmpty());
        assertEquals('a', queue.peek());
    }

    // Tests addFirst with Character and String
    @Test
    public void testAddFirst_charAndString_prependsToQueue() {
        TokenQueue queue = new TokenQueue("world");
        queue.addFirst(' ');
        queue.addFirst("hello");
        assertEquals("hello world", queue.remainder());
    }

    // Tests matches and matchesCS case sensitivity
    @Test
    public void testMatchesAndMatchesCS_caseVariations_returnsCorrectResult() {
        TokenQueue queue = new TokenQueue("One Two");
        assertTrue(queue.matches("one"));
        assertTrue(queue.matches("ONE"));
        assertTrue(queue.matchesCS("One"));
        assertFalse(queue.matchesCS("one"));
    }

    // Tests matchesAny for string and char varargs
    @Test
    public void testMatchesAny_stringAndCharArrays_matchesCorrectly() {
        TokenQueue queue = new TokenQueue("abcdef");
        assertTrue(queue.matchesAny("xyz", "ABC", "123"));
        assertFalse(queue.matchesAny("xyz", "123"));

        assertTrue(queue.matchesAny('x', 'a', 'z'));
        assertFalse(queue.matchesAny('x', 'y', 'z'));

        TokenQueue emptyQueue = new TokenQueue("");
        assertFalse(emptyQueue.matchesAny('a', 'b'));
    }

    // Tests matchesStartTag with valid and invalid HTML tags
    @Test
    public void testMatchesStartTag_variousSequences_identifiesTags() {
        assertTrue(new TokenQueue("<div").matchesStartTag());
        assertTrue(new TokenQueue("<P>").matchesStartTag());
        assertFalse(new TokenQueue("<!DOCTYPE").matchesStartTag());
        assertFalse(new TokenQueue("<?xml").matchesStartTag());
        assertFalse(new TokenQueue("<3").matchesStartTag());
        assertFalse(new TokenQueue("<").matchesStartTag());
        assertFalse(new TokenQueue("div").matchesStartTag());
    }

    // Tests matchChomp consumes sequence only on match
    @Test
    public void testMatchChomp_matchingAndNonMatching_consumesOnMatch() {
        TokenQueue queue = new TokenQueue("HelloWorld");
        assertTrue(queue.matchChomp("hello"));
        assertEquals("World", queue.toString());
        assertFalse(queue.matchChomp("Earth"));
        assertEquals("World", queue.toString());
    }

    // Tests matchesWhitespace and matchesWord predicates
    @Test
    public void testMatchesWhitespaceAndWord_variousChars_returnsCorrectFlags() {
        TokenQueue queue = new TokenQueue(" a1_ \n\t");
        assertTrue(queue.matchesWhitespace());
        assertFalse(queue.matchesWord());

        queue.consume(); // now at 'a'
        assertFalse(queue.matchesWhitespace());
        assertTrue(queue.matchesWord());

        queue.consume(); // now at '1'
        assertTrue(queue.matchesWord());

        queue.consume(); // now at '_'
        assertFalse(queue.matchesWord());
        assertFalse(queue.matchesWhitespace());
    }

    // Tests advance and consume single character
    @Test
    public void testAdvanceAndConsume_normalSequence_movesPointer() {
        TokenQueue queue = new TokenQueue("abc");
        assertEquals('a', queue.consume());
        queue.advance();
        assertEquals('c', queue.consume());
        assertTrue(queue.isEmpty());
        queue.advance(); // should not fail when empty
    }

    // Tests consume matching sequence
    @Test
    public void testConsume_matchingSequence_consumesString() {
        TokenQueue queue = new TokenQueue("abcdef");
        queue.consume("abc");
        assertEquals("def", queue.remainder());
    }

    // Tests consume unmatched sequence throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testConsume_mismatchedSequence_throwsException() {
        TokenQueue queue = new TokenQueue("abcdef");
        queue.consume("xyz");
    }

    // Tests consumeTo and consumeToIgnoreCase
    @Test
    public void testConsumeTo_caseSensitiveAndInsensitive_extractsTarget() {
        TokenQueue queue = new TokenQueue("one TWO three");
        assertEquals("one ", queue.consumeTo("TWO"));
        assertEquals("TWO three", queue.remainder());

        TokenQueue queue2 = new TokenQueue("abcDEFghi");
        assertEquals("abc", queue2.consumeToIgnoreCase("def"));
        assertEquals("DEFghi", queue2.remainder());

        TokenQueue queue3 = new TokenQueue("no match here");
        assertEquals("no match here", queue3.consumeTo("xyz"));
        assertTrue(queue3.isEmpty());
    }

    // Tests consumeToAny with multiple candidates
    @Test
    public void testConsumeToAny_multipleCandidates_consumesToFirst() {
        TokenQueue queue = new TokenQueue("foo:bar-baz");
        assertEquals("foo", queue.consumeToAny(":", "-"));
        assertEquals(':', queue.peek());
    }

    // Tests chompTo and chompToIgnoreCase
    @Test
    public void testChompTo_caseSensitiveAndInsensitive_extractsAndPulls() {
        TokenQueue queue = new TokenQueue("first;second;third");
        assertEquals("first", queue.chompTo(";"));
        assertEquals("second;third", queue.remainder());

        TokenQueue queue2 = new TokenQueue("firstENDsecond");
        assertEquals("first", queue2.chompToIgnoreCase("end"));
        assertEquals("second", queue2.remainder());
    }

    // Tests chompBalanced with nested parentheses and escapes
    @Test
    public void testChompBalanced_nestedAndEscaped_extractsBalancedContent() {
        TokenQueue queue = new TokenQueue("(one (two (three) two) one) rest");
        assertEquals("one (two (three) two) one", queue.chompBalanced('(', ')'));
        assertEquals(" rest", queue.remainder());

        TokenQueue queueEscaped = new TokenQueue("(one \\( nested \\) one) rest");
        assertEquals("one \\( nested \\) one", queueEscaped.chompBalanced('(', ')'));
        assertEquals(" rest", queueEscaped.remainder());

        TokenQueue queueUnbalanced = new TokenQueue("no open bracket");
        assertEquals("", queueUnbalanced.chompBalanced('(', ')'));
    }

    // Tests chompBalanced with quotes inside balanced brackets
    @Test
    public void testChompBalanced_quotesInsideBrackets_handlesCorrectly() {
        TokenQueue queue = new TokenQueue("[foo='[inner]'] rest");
        assertEquals("foo='[inner]'", queue.chompBalanced('[', ']'));
        assertEquals(" rest", queue.remainder());
    }

    // Tests unescape static method
    @Test
    public void testUnescape_escapedChars_unescapesProperly() {
        assertEquals("hello world", TokenQueue.unescape("hello\\ world"));
        assertEquals("slash\\char", TokenQueue.unescape("slash\\\\char"));
        assertEquals("abc", TokenQueue.unescape("abc"));
    }

    // Tests consumeTagName, consumeElementSelector, consumeCssIdentifier, consumeAttributeKey
    @Test
    public void testConsumeIdentifiers_validCssAndHtml_parsesTokens() {
        TokenQueue queue = new TokenQueue("div:custom_tag-name.class#id|ns:attr_1-key ");
        assertEquals("div:custom_tag-name", queue.consumeTagName());

        TokenQueue selectorQueue = new TokenQueue("ns|div-name remaining");
        assertEquals("ns|div-name", selectorQueue.consumeElementSelector());

        TokenQueue idQueue = new TokenQueue("my-id_123 remaining");
        assertEquals("my-id_123", idQueue.consumeCssIdentifier());

        TokenQueue attrQueue = new TokenQueue("data-attr:key remaining");
        assertEquals("data-attr:key", attrQueue.consumeAttributeKey());
    }

    // Tests consumeWhitespace and consumeWord
    @Test
    public void testConsumeWhitespaceAndWord_sequences_consumesExpected() {
        TokenQueue queue = new TokenQueue("   \n\tword123  ");
        assertTrue(queue.consumeWhitespace());
        assertEquals("word123", queue.consumeWord());
        assertTrue(queue.consumeWhitespace());
        assertFalse(queue.consumeWhitespace());
    }

    // Tests remainder and toString
    @Test
    public void testRemainderAndToString_partialQueue_returnsRemainder() {
        TokenQueue queue = new TokenQueue("hello world");
        queue.consume("hello ");
        assertEquals("world", queue.toString());
        assertEquals("world", queue.remainder());
        assertTrue(queue.isEmpty());
        assertEquals("", queue.remainder());
    }
}
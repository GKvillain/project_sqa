package com.fasterxml.jackson.core.util;

import java.io.IOException;
import com.fasterxml.jackson.core.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonParserSequenceTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // Tests that createFlattened produces a sequence with correct count
    @Test
    public void testCreateFlattened_twoSimpleParsers_returnsSequenceWithTwoParsers() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq.containedParsersCount());
        // consume all tokens
        while (seq.nextToken() != null) {}
    }

    // Tests that nextToken returns tokens in correct order across two parsers
    @Test
    public void testNextToken_sequenceOfTwoParsers_returnsTokensInOrder() throws IOException {
        JsonParser p1 = factory.createParser("[1]");
        JsonParser p2 = factory.createParser("{\"a\":2}");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(JsonToken.START_ARRAY, seq.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.END_ARRAY, seq.nextToken());
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("a", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());
        assertNull(seq.nextToken());
    }

    // Tests that when a later parser already has a current token, that token is not skipped
    // This test should FAIL under the bug in Defects4J 16b (missing flag)
    @Test
    public void testNextToken_secondParserHasCurrentToken_doesNotSkipToken() throws IOException {
        JsonParser first = factory.createParser("true");
        JsonParser second = factory.createParser("123");
        // Advance second parser to its first token
        assertEquals(JsonToken.VALUE_NUMBER_INT, second.nextToken());
        int expectedValue = second.getIntValue();  // 123
        // Build sequence – first parser is delegate initially
        JsonParserSequence seq = JsonParserSequence.createFlattened(first, second);
        // Consume first parser's single token
        assertEquals(JsonToken.VALUE_TRUE, seq.nextToken());
        // Now first parser is exhausted, next call should return second parser's current token (not null)
        JsonToken token = seq.nextToken();
        assertNotNull(token);
        assertTrue(token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(expectedValue, seq.getIntValue());
    }

    // Tests that after all parsers are exhausted, nextToken returns null
    @Test
    public void testNextToken_allParsersExhausted_returnsNull() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.nextToken(); // 1
        seq.nextToken(); // 2
        assertNull(seq.nextToken());
    }

    // Tests close closes all parsers without exception
    @Test
    public void testClose_closesAllParsers() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.close();
        // After close, trying to read should throw
        try {
            p1.nextToken();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
        try {
            p2.nextToken();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // Tests containedParsersCount with flattened nested sequences
    @Test
    public void testContainedParsersCount_flattenedNestedSequences_returnsFlatCount() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParser p3 = factory.createParser("3");
        // Create nested sequence: (p1, p2) as one sequence, then flatten with p3
        JsonParser inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence outer = JsonParserSequence.createFlattened(inner, p3);
        // Should have three parsers
        assertEquals(3, outer.containedParsersCount());
        while (outer.nextToken() != null) {}
    }

    // Tests createFlattened with null first parser throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testCreateFlattened_firstParserNull_throwsNPE() {
        JsonParser p2 = factory.createParser("{}");
        JsonParserSequence.createFlattened(null, p2);
    }

    // Tests createFlattened with null second parser throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testCreateFlattened_secondParserNull_throwsNPE() {
        JsonParser p1 = factory.createParser("{}");
        JsonParserSequence.createFlattened(p1, null);
    }

    // Tests that nextToken works when first parser has no tokens (empty input)
    @Test
    public void testNextToken_firstParserEmpty_returnsTokensFromSecond() throws IOException {
        JsonParser empty = factory.createParser("");   // produces no tokens
        JsonParser p2 = factory.createParser("42");
        JsonParserSequence seq = JsonParserSequence.createFlattened(empty, p2);
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(42, seq.getIntValue());
        assertNull(seq.nextToken());
    }

    // Tests that switchToNext works correctly when there is exactly one parser after the first
    // (this test indirectly exercises switchToNext)
    @Test
    public void testNextToken_oneParserAfterExhaustion_switchesAndReturnsToken() throws IOException {
        JsonParser p1 = factory.createParser("null");
        JsonParser p2 = factory.createParser("true");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(JsonToken.VALUE_NULL, seq.nextToken());
        // now p1 exhausted, should switch to p2
        assertEquals(JsonToken.VALUE_TRUE, seq.nextToken());
        assertNull(seq.nextToken());
    }

    // Tests edge where a parser sequence contains many parsers
    @Test
    public void testCreateFlattened_manyParsers_allTokensReturned() throws IOException {
        // With five simple numeric parsers
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParser p3 = factory.createParser("3");
        JsonParser p4 = factory.createParser("4");
        int expected = 0;
        JsonParser seq = p1;
        seq = JsonParserSequence.createFlattened(seq, p2);
        seq = JsonParserSequence.createFlattened(seq, p3);
        seq = JsonParserSequence.createFlattened(seq, p4);
        expected = 4;
        assertEquals(expected, ((JsonParserSequence) seq).containedParsersCount());
        // Read all numbers
        int sum = 0;
        int count = 0;
        while (seq.nextToken() != null) {
            sum += seq.getIntValue();
            count++;
        }
        assertEquals(4, count);
        assertEquals(10, sum);
    }

    // Tests that createFlattened with a sequence as first argument flattens correctly
    @Test
    public void testCreateFlattened_sequenceFirstArg_flattensToTwo() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParser inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParser p3 = factory.createParser("3");
        JsonParserSequence outer = JsonParserSequence.createFlattened(inner, p3);
        // Should contain 3 parsers: p1, p2, p3
        assertEquals(3, outer.containedParsersCount());
        while (outer.nextToken() != null) {}
    }

    // ========== New tests added to cover missing parts ==========

    // Test that hasCurrentToken works correctly across sequence
    @Test
    public void testHasCurrentToken_sequence_returnsCorrect() throws IOException {
        JsonParser p1 = factory.createParser("1");
        JsonParser p2 = factory.createParser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertFalse(seq.hasCurrentToken()); // before any nextToken
        seq.nextToken(); // 1
        assertTrue(seq.hasCurrentToken());
        seq.nextToken(); // 2
        assertTrue(seq.hasCurrentToken());
        seq.nextToken(); // null
        assertFalse(seq.hasCurrentToken());
    }

    // Test that getCurrentToken returns correct after switch
    @Test
    public void testGetCurrentToken_afterSwitch_returnsTokenFromSecondParser() throws IOException {
        JsonParser p1 = factory.createParser("null");
        JsonParser p2 = factory.createParser("true");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.nextToken(); // null
        assertEquals(JsonToken.VALUE_NULL, seq.getCurrentToken());
        seq.nextToken(); // true
        assertEquals(JsonToken.VALUE_TRUE, seq.getCurrentToken());
    }

    // Test getText returns correct string for tokens in sequence
    @Test
    public void testGetText_sequence_returnsCorrectText() throws IOException {
        JsonParser p1 = factory.createParser("\"hello\"");
        JsonParser p2 = factory.createParser("42");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.nextToken(); // string
        assertEquals("hello", seq.getText());
        seq.nextToken(); // number
        assertEquals("42", seq.getText());
    }
}
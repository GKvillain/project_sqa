package com.fasterxml.jackson.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import static org.junit.Assert.*;

public class JsonParserSequenceTest {

    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
    }

    // Tests createFlattened with two simple parsers
    @Test
    public void testCreateFlattened_twoSimpleParsers_createsSequenceOfTwo() throws IOException {
        JsonParser p1 = jsonFactory.createParser("[ 1 ]");
        JsonParser p2 = jsonFactory.createParser("[ 2 ]");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertNotNull(seq);
        assertEquals(2, seq.containedParsersCount());
        seq.close();
    }

    // Tests createFlattened when first parser is already a sequence
    @Test
    public void testCreateFlattened_firstIsSequence_flattensCorrectly() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq1, p3);

        assertEquals(3, seq2.containedParsersCount());
        seq2.close();
    }

    // Tests createFlattened when second parser is already a sequence
    @Test
    public void testCreateFlattened_secondIsSequence_flattensCorrectly() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p2, p3);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p1, seq1);

        assertEquals(3, seq2.containedParsersCount());
        seq2.close();
    }

    // Tests createFlattened when both parsers are sequences
    @Test
    public void testCreateFlattened_bothAreSequences_flattensAll() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");
        JsonParser p4 = jsonFactory.createParser("4");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p3, p4);
        JsonParserSequence combined = JsonParserSequence.createFlattened(seq1, seq2);

        assertEquals(4, combined.containedParsersCount());
        combined.close();
    }

    // Tests createFlattened with nested sequences within a sequence
    @Test
    public void testCreateFlattened_nestedSequences_flattensRecursively() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence middle = new JsonParserSequence(new JsonParser[] { inner, p3 });
        JsonParser p4 = jsonFactory.createParser("4");

        JsonParserSequence result = JsonParserSequence.createFlattened(middle, p4);

        assertEquals(4, result.containedParsersCount());
        result.close();
    }

    // Tests nextToken token sequence across two simple parsers
    @Test
    public void testNextToken_twoParsers_iteratesAllTokensInOrder() throws IOException {
        JsonParser p1 = jsonFactory.createParser("[ 1 ]");
        JsonParser p2 = jsonFactory.createParser("[ 2 ]");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertEquals(JsonToken.START_ARRAY, seq.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.END_ARRAY, seq.nextToken());

        assertEquals(JsonToken.START_ARRAY, seq.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.END_ARRAY, seq.nextToken());

        assertNull(seq.nextToken());
        seq.close();
    }

    // Tests nextToken with empty parsers
    @Test
    public void testNextToken_emptyParsers_returnsNull() throws IOException {
        JsonParser p1 = jsonFactory.createParser("");
        JsonParser p2 = jsonFactory.createParser("");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertNull(seq.nextToken());
        seq.close();
    }

    // Tests nextToken when first parser is empty and second has content
    @Test
    public void testNextToken_firstParserEmpty_switchesToSecond() throws IOException {
        JsonParser p1 = jsonFactory.createParser("");
        JsonParser p2 = jsonFactory.createParser("42");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(42, seq.getIntValue());
        assertNull(seq.nextToken());
        seq.close();
    }

    // Tests nextToken when second parser is empty
    @Test
    public void testNextToken_secondParserEmpty_completesAfterFirst() throws IOException {
        JsonParser p1 = jsonFactory.createParser("42");
        JsonParser p2 = jsonFactory.createParser("");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(42, seq.getIntValue());
        assertNull(seq.nextToken());
        seq.close();
    }

    // Tests close closes all underlying parsers in sequence
    @Test
    public void testClose_unconsumedSequence_closesAllParsers() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.close();

        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(seq.isClosed());
    }

    // Tests close when partially consumed
    @Test
    public void testClose_partiallyConsumed_closesRemainingParsers() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        seq.close();

        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(seq.isClosed());
    }

    // Tests containedParsersCount with direct constructor
    @Test
    public void testContainedParsersCount_directConstructor_returnsLength() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        assertEquals(3, seq.containedParsersCount());
        seq.close();
    }

    // Tests switchToNext returns false when no more parsers left
    @Test
    public void testSwitchToNext_exhaustedParsers_returnsFalse() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertTrue(seq.switchToNext());
        assertFalse(seq.switchToNext());
        seq.close();
    }

    // Tests addFlattenedActiveParsers on partially consumed sequence
    @Test
    public void testAddFlattenedActiveParsers_partiallyConsumed_addsOnlyActiveParsers() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });
        // Consume first parser and switch to second
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertNull(p1.nextToken());
        assertTrue(seq.switchToNext());

        List<JsonParser> result = new ArrayList<JsonParser>();
        seq.addFlattenedActiveParsers(result);

        assertEquals(2, result.size());
        assertSame(p2, result.get(0));
        assertSame(p3, result.get(1));
        seq.close();
    }

    // Tests flattening partially consumed sequence with another parser
    @Test
    public void testCreateFlattened_partiallyConsumedSequence_flattensActiveOnly() throws IOException {
        JsonParser p1 = jsonFactory.createParser("1");
        JsonParser p2 = jsonFactory.createParser("2");
        JsonParser p3 = jsonFactory.createParser("3");

        JsonParserSequence seq1 = new JsonParserSequence(new JsonParser[] { p1, p2 });
        // Switch to second parser
        assertTrue(seq1.switchToNext());

        JsonParserSequence combined = JsonParserSequence.createFlattened(seq1, p3);

        assertEquals(2, combined.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(2, combined.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(3, combined.getIntValue());
        assertNull(combined.nextToken());
        combined.close();
    }
}
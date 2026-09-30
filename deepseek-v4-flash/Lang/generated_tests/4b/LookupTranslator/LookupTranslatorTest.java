package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

public class LookupTranslatorTest {

    // Basic lookup with one mapping
    @Test
    public void testTranslate_singleEntry_returnsMappedValue() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(1, translator.translate("a", 0, writer));
        assertEquals("b", writer.toString());
    }

    // Greedy algorithm should choose the longest matching key
    @Test
    public void testTranslate_greedyLongestMatch_returnsReplacement() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "X"}, {"ab", "Y"} });

        StringWriter writer = new StringWriter();
        assertEquals(2, translator.translate("ab", 0, writer));
        assertEquals("Y", writer.toString());

        writer = new StringWriter();
        assertEquals(1, translator.translate("ac", 0, writer));
        assertEquals("X", writer.toString());
    }

    // Constructor branch: a later shorter key should not overwrite the longest length
    @Test
    public void testTranslate_shorterEntryAfterLongerEntry_returnsReplacement() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"ab", "Y"}, {"a", "X"} });
        StringWriter writer = new StringWriter();

        assertEquals(1, translator.translate("ac", 0, writer));
        assertEquals("X", writer.toString());
    }

    // No match should return 0 and write nothing
    @Test
    public void testTranslate_noMatch_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("x", 0, writer));
        assertEquals("", writer.toString());
    }

    // Input shorter than the longest key
    @Test
    public void testTranslate_inputShorterThanKey_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"abc", "X"} });
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("ab", 0, writer));
        assertEquals("", writer.toString());
    }

    // Empty input string
    @Test
    public void testTranslate_emptyInput_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("", 0, writer));
        assertEquals("", writer.toString());
    }

    // Empty lookup table
    @Test
    public void testTranslate_emptyLookupTable_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator();
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("abc", 0, writer));
        assertEquals("", writer.toString());
    }

    // Null lookup table
    @Test
    public void testTranslate_nullLookupTable_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("abc", 0, writer));
        assertEquals("", writer.toString());
    }

    // Regression for LANG-882: lookup keys must be compared by value, not identity
    @Test
    public void testTranslate_nonStringKey_usesStringValueForLookup() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {new StringBuilder("a"), "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(1, translator.translate("a", 0, writer));
        assertEquals("b", writer.toString());
    }

    // Regression for LANG-882: subsequence CharSequence must be converted to String for lookup
    @Test
    public void testTranslate_customSubSequence_usesStringValueForLookup() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });

        CharSequence input = new CharSequence() {
            @Override
            public int length() {
                return toString().length();
            }

            @Override
            public char charAt(int index) {
                return toString().charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(toString().substring(start, end));
            }

            @Override
            public String toString() {
                return "a";
            }
        };

        StringWriter writer = new StringWriter();
        assertEquals(1, translator.translate(input, 0, writer));
        assertEquals("b", writer.toString());
    }

    // Direct translate at the end of input
    @Test
    public void testTranslate_indexAtEnd_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("a", 1, writer));
        assertEquals("", writer.toString());
    }

    // Direct translate at an index where a key starts
    @Test
    public void testTranslate_indexInMiddle_matchesKey() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"b", "B"} });
        StringWriter writer = new StringWriter();

        assertEquals(1, translator.translate("ab", 1, writer));
        assertEquals("B", writer.toString());
    }

    // Direct translate at an index where no key starts
    @Test
    public void testTranslate_indexInMiddle_noMatch_returnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(new CharSequence[][] { {"a", "b"} });
        StringWriter writer = new StringWriter();

        assertEquals(0, translator.translate("ab", 1, writer));
        assertEquals("", writer.toString());
    }
}
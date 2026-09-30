package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link LookupTranslator}.
 */
public class LookupTranslatorTest {

    // Tests defect LANG-882 where lookup key is a StringBuffer (CharSequence)
    @Test
    public void testTranslate_stringBufferKey_translatesSuccessfully() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuffer("one"), new StringBuffer("two") }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate(new StringBuffer("one"), 0, writer);

        assertEquals(3, consumed);
        assertEquals("two", writer.toString());
    }

    // Tests CharSequence key using StringBuilder
    @Test
    public void testTranslate_stringBuilderKey_translatesSuccessfully() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuilder("foo"), new StringBuilder("bar") }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("foo", 0, writer);

        assertEquals(3, consumed);
        assertEquals("bar", writer.toString());
    }

    // Tests standard String mapping translation
    @Test
    public void testTranslate_exactMatch_returnsConsumedLengthAndWritesTranslation() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "two" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("one", 0, writer);

        assertEquals(3, consumed);
        assertEquals("two", writer.toString());
    }

    // Tests input that does not match any lookup entry
    @Test
    public void testTranslate_noMatch_returnsZeroAndWritesNothing() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "two" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("three", 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests greedy algorithm selecting the longest matching prefix
    @Test
    public void testTranslate_greedyPrefixMatch_selectsLongestMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "ab", "1" },
            { "abcd", "2" },
            { "abc", "3" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("abcdef", 0, writer);

        assertEquals(4, consumed);
        assertEquals("2", writer.toString());
    }

    // Tests selecting shorter match when longer prefix does not match
    @Test
    public void testTranslate_shorterMatch_selectsValidShorterPrefix() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "ab", "1" },
            { "abcd", "2" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("abc", 0, writer);

        assertEquals(2, consumed);
        assertEquals("1", writer.toString());
    }

    // Tests translation starting from a non-zero index
    @Test
    public void testTranslate_nonZeroIndex_translatesSubstringCorrectly() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "target", "replacement" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("pre-target-post", 4, writer);

        assertEquals(6, consumed);
        assertEquals("replacement", writer.toString());
    }

    // Tests boundary condition where remaining characters are fewer than the longest key
    @Test
    public void testTranslate_inputShorterThanLongestKey_translatesCorrectly() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "longerKey", "1" },
            { "sh", "2" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("sh", 0, writer);

        assertEquals(2, consumed);
        assertEquals("2", writer.toString());
    }

    // Tests null lookup array handling
    @Test
    public void testTranslate_nullLookupArray_returnsZero() throws IOException {
        final LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("test", 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests empty lookup array handling
    @Test
    public void testTranslate_emptyLookupArray_returnsZero() throws IOException {
        final LookupTranslator translator = new LookupTranslator(new CharSequence[0][0]);
        final StringWriter writer = new StringWriter();
        final int consumed = translator.translate("test", 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests multiple translations across separate calls
    @Test
    public void testTranslate_multipleLookups_translatesEachCorrectly() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "hello", "hi" },
            { "world", "earth" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final StringWriter writer1 = new StringWriter();
        final int consumed1 = translator.translate("hello world", 0, writer1);
        assertEquals(5, consumed1);
        assertEquals("hi", writer1.toString());

        final StringWriter writer2 = new StringWriter();
        final int consumed2 = translator.translate("hello world", 6, writer2);
        assertEquals(5, consumed2);
        assertEquals("earth", writer2.toString());
    }
}
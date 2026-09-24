package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link LookupTranslator}.
 */
public class LookupTranslatorTest {

    // Tests normal translation with basic String mapping
    @Test
    public void testTranslate_basicStringMapping_translatesCorrectly() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "two" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("one", 0, out);

        assertEquals(3, consumed);
        assertEquals("two", out.toString());
    }

    // Tests Lang-882 defect where key is a non-String CharSequence (e.g., StringBuffer)
    @Test
    public void testTranslate_charSequenceKeyNonString_translatesCorrectly() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuffer("one"), new StringBuffer("two") }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("one", 0, out);

        assertEquals(3, consumed);
        assertEquals("two", out.toString());
    }

    // Tests greedy matching behavior when multiple prefixes match
    @Test
    public void testTranslate_greedyMatching_selectsLongestMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "ab", "[ab]" },
            { "abc", "[abc]" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("abcd", 0, out);

        assertEquals(3, consumed);
        assertEquals("[abc]", out.toString());
    }

    // Tests translation at a non-zero start index
    @Test
    public void testTranslate_nonZeroIndex_translatesFromIndex() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "target", "replacement" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("pre-target-post", 4, out);

        assertEquals(6, consumed);
        assertEquals("replacement", out.toString());
    }

    // Tests unmatched input returning 0 and writing nothing
    @Test
    public void testTranslate_unmatchedInput_returnsZero() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "foo", "bar" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("baz", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests null lookup array in constructor
    @Test
    public void testTranslate_nullLookup_returnsZero() throws IOException {
        final LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("test", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests empty lookup array in constructor
    @Test
    public void testTranslate_emptyLookup_returnsZero() throws IOException {
        final LookupTranslator translator = new LookupTranslator(new CharSequence[0][0]);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("test", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests boundary condition where remaining characters are fewer than longest match
    @Test
    public void testTranslate_indexNearEndOfInput_handlesTruncatedWindow() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "longerPrefix", "replacement" },
            { "short", "hit" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("short", 0, out);

        assertEquals(5, consumed);
        assertEquals("hit", out.toString());
    }

    // Tests boundary condition where input length is shorter than shortest lookup key
    @Test
    public void testTranslate_inputShorterThanShortest_returnsZero() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "three", "3" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();
        final int consumed = translator.translate("to", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests full translation via CharSequenceTranslator base translate method
    @Test
    public void testTranslate_fullStringTranslation_replacesAllOccurrences() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "<", "&lt;" },
            { ">", "&gt;" },
            { "&", "&amp;" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final String result = translator.translate("<tag & value>");

        assertEquals("&lt;tag &amp; value&gt;", result);
    }
}
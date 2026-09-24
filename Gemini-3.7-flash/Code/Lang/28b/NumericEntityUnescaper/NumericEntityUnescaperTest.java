package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class NumericEntityUnescaperTest {

    // Tests unescaping a valid decimal numeric entity
    @Test
    public void testTranslate_decimalEntity_returnsConsumedLengthAndWritesChar() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#65;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping a valid hex numeric entity with lower-case 'x'
    @Test
    public void testTranslate_hexEntityLowerCase_returnsConsumedLengthAndWritesChar() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#x41;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping a valid hex numeric entity with upper-case 'X'
    @Test
    public void testTranslate_hexEntityUpperCase_returnsConsumedLengthAndWritesChar() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#X41;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping supplementary characters with decimal representation (> 0xFFFF)
    @Test
    public void testTranslate_supplementaryCharacterDecimal_writesCorrectSurrogatePair() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#68642;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(8, consumed);
        int codePoint = 68642;
        String expected = new String(Character.toChars(codePoint));
        assertEquals(expected, out.toString());
    }

    // Tests unescaping supplementary characters with hex representation (> 0xFFFF)
    @Test
    public void testTranslate_supplementaryCharacterHex_writesCorrectSurrogatePair() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#x10000;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(9, consumed);
        int codePoint = 0x10000;
        String expected = new String(Character.toChars(codePoint));
        assertEquals(expected, out.toString());
    }

    // Tests input that does not start with entity prefix '&'
    @Test
    public void testTranslate_notAnEntity_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "Hello";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests input starting with '&' but not followed by '#'
    @Test
    public void testTranslate_ampersandWithoutHash_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&amp;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests input with non-numeric content causing NumberFormatException
    @Test
    public void testTranslate_invalidNumberFormat_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&#invalid;";
        int consumed = unescaper.translate(input, 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests translating an entity starting at an offset index
    @Test
    public void testTranslate_entityAtIndexOffset_returnsConsumedLengthAndWritesChar() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "Prefix &#66; Suffix";
        int consumed = unescaper.translate(input, 7, out);

        assertEquals(5, consumed);
        assertEquals("B", out.toString());
    }

    // Tests full string unescaping via CharSequenceTranslator translate method
    @Test
    public void testTranslate_fullStringWithMultipleEntities_returnsUnescapedString() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Test &#65;&#x42;&#X43;!";
        String result = unescaper.translate(input);

        assertEquals("Test ABC!", result);
    }

    // Tests full string unescaping with supplementary character
    @Test
    public void testTranslate_fullStringWithSupplementaryEntity_returnsUnescapedString() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "&#x10000;";
        String result = unescaper.translate(input);
        String expected = new String(Character.toChars(0x10000));

        assertEquals(expected, result);
    }
}
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link NumericEntityUnescaper}.
 */
public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    // Tests unescaping standard decimal entity
    @Test
    public void testTranslate_decimalEntity_unescapesCorrectly() throws IOException {
        String input = "&#65;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping lowercase hex entity
    @Test
    public void testTranslate_hexEntityLowerCase_unescapesCorrectly() throws IOException {
        String input = "&#x41;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping uppercase hex entity
    @Test
    public void testTranslate_hexEntityUpperCase_unescapesCorrectly() throws IOException {
        String input = "&#X41;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    // Tests unescaping supplementary characters in decimal format (Defects4J Lang-28)
    @Test
    public void testTranslate_supplementaryDecimalEntity_unescapesToSurrogatePair() throws IOException {
        int codePoint = 0x10C48; // 68664 in decimal
        String input = "&#68664;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(8, consumed);
        assertEquals(new String(Character.toChars(codePoint)), out.toString());
    }

    // Tests unescaping supplementary characters in hex format (Defects4J Lang-28)
    @Test
    public void testTranslate_supplementaryHexEntity_unescapesToSurrogatePair() throws IOException {
        int codePoint = 0x10000;
        String input = "&#x10000;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(9, consumed);
        assertEquals(new String(Character.toChars(codePoint)), out.toString());
    }

    // Tests non-entity input starting with a character other than '&'
    @Test
    public void testTranslate_notStartingWithAmpersand_returnsZero() throws IOException {
        String input = "Hello";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests input starting with '&' but not followed by '#'
    @Test
    public void testTranslate_ampersandWithoutHash_returnsZero() throws IOException {
        String input = "&amp;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests invalid decimal number format inside entity
    @Test
    public void testTranslate_invalidDecimalFormat_returnsZero() throws IOException {
        String input = "&#abc;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests invalid hex number format inside entity
    @Test
    public void testTranslate_invalidHexFormat_returnsZero() throws IOException {
        String input = "&#xgh;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests full string unescaping via CharSequenceTranslator translate method
    @Test
    public void testTranslate_mixedString_unescapesAllEntities() {
        String input = "Test &#65; and &#x42; and &#68664; end";
        String expected = "Test A and B and " + new String(Character.toChars(0x10C48)) + " end";
        String result = unescaper.translate(input);
        assertEquals(expected, result);
    }

    // Tests string without any entities
    @Test
    public void testTranslate_stringWithoutEntities_returnsUnchanged() {
        String input = "Plain text without entities.";
        String result = unescaper.translate(input);
        assertEquals(input, result);
    }
}
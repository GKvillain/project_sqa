package org.apache.commons.lang3.text.translate;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import static org.junit.Assert.*;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    // Tests null input -> NullPointerException
    @Test(expected = NullPointerException.class)
    public void testTranslate_nullInput_throwsNullPointerException() throws IOException {
        unescaper.translate(null, 0, new StringWriter());
    }

    // Tests empty string -> returns 0
    @Test
    public void testTranslate_emptyString_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests string with no entity -> returns 0
    @Test
    public void testTranslate_noEntity_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("abc", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests single ampersand (index < seqEnd-1 false) -> returns 0
    @Test
    public void testTranslate_onlyAmpersand_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests entity without number and semicolon (&#) -> IndexOutOfBoundsException
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testTranslate_entityWithoutNumberAndSemicolon_throwsIndexOutOfBoundsException() throws IOException {
        unescaper.translate("&#", 0, new StringWriter());
    }

    // Tests decimal entity without semicolon -> IndexOutOfBoundsException
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testTranslate_decimalEntityWithoutSemicolon_throwsIndexOutOfBoundsException() throws IOException {
        unescaper.translate("&#65", 0, new StringWriter());
    }

    // Tests hex entity without semicolon -> IndexOutOfBoundsException
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testTranslate_hexEntityWithoutSemicolon_throwsIndexOutOfBoundsException() throws IOException {
        unescaper.translate("&#x41", 0, new StringWriter());
    }

    // Tests simple decimal entity &#65; -> 'A'
    @Test
    public void testTranslate_simpleDecimalEntity_returnsA() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#65;", 0, writer);
        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests simple hex entity &#x41; -> 'A'
    @Test
    public void testTranslate_simpleHexEntity_returnsA() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x41;", 0, writer);
        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests decimal entity &#0; -> null character
    @Test
    public void testTranslate_decimalEntityZero_returnsNullChar() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#0;", 0, writer);
        assertEquals(4, consumed);
        assertEquals("\0", writer.toString());
    }

    // Tests decimal entity &#65535; -> \uFFFF
    @Test
    public void testTranslate_decimalEntityMaxFFFF_returnsCorrectChar() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#65535;", 0, writer);
        assertEquals(8, consumed);
        assertEquals("\uFFFF", writer.toString());
    }

    // Tests hex entity &#xFFFF; -> \uFFFF
    @Test
    public void testTranslate_hexEntityMaxFFFF_returnsCorrectChar() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#xFFFF;", 0, writer);
        assertEquals(8, consumed);
        assertEquals("\uFFFF", writer.toString());
    }

    // Tests decimal entity &#128512; -> surrogate pair (😀)
    @Test
    public void testTranslate_decimalEntitySurrogate_returnsTwoChars() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#128512;", 0, writer);
        assertEquals(9, consumed);
        assertEquals("\uD83D\uDE00", writer.toString());
    }

    // Tests hex entity &#x1F600; -> surrogate pair (😀)
    @Test
    public void testTranslate_hexEntitySurrogate_returnsTwoChars() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#x1F600;", 0, writer);
        assertEquals(9, consumed);
        assertEquals("\uD83D\uDE00", writer.toString());
    }

    // Tests entity with only prefix &#; -> NumberFormatException, returns 0
    @Test
    public void testTranslate_decimalEntityOnlyPrefix_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests invalid decimal entity &#abc; -> NumberFormatException, returns 0
    @Test
    public void testTranslate_invalidDecimalEntity_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#abc;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests invalid hex entity &#xGHI; -> NumberFormatException, returns 0
    @Test
    public void testTranslate_invalidHexEntity_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#xGHI;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests non-numeric entity &amp; -> returns 0
    @Test
    public void testTranslate_notNumericEntity_returnsZero() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&amp;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests entity with extra characters after -> consumed length and correct char
    @Test
    public void testTranslate_entityWithExtraChars_returnsConsumedLength() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("&#65;extra", 0, writer);
        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests entity at non-zero index -> correctly processes entity
    @Test
    public void testTranslate_entityAtNonZeroIndex_returnsCorrect() throws IOException {
        StringWriter writer = new StringWriter();
        int consumed = unescaper.translate("abc&#65;", 3, writer);
        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }
}
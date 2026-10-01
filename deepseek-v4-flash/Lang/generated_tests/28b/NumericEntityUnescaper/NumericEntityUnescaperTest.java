package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper translator;

    @Before
    public void setUp() {
        translator = new NumericEntityUnescaper();
    }

    @Test
    public void testTranslate_basicDecimal_consumesCorrect() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#65;", 0, writer);
        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslate_hexLowercase_consumesCorrect() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#x41;", 0, writer);
        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslate_hexUppercase_consumesCorrect() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#X41;", 0, writer);
        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    @Test
    public void testTranslate_noAmpersand_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abcd", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_noHashAfterAmpersand_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&abc;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_invalidNumber_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_invalidHexNumber_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#x;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_nonNumericEntity_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#abc;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_nonHexEntity_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#xG1;", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_entityWithoutSemicolon_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#65", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_hexEntityWithoutSemicolon_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#x41", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_emptyEntity_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("&#", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_emptyInput_returnsZero() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslate_offsetEntity_consumesCorrect() throws Exception {
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abc&#65;def", 3, writer);
        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }
}
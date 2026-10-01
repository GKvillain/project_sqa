package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class EntitiesTest {

    // Tests isNamedEntity with a known entity
    @Test
    public void testIsNamedEntity_knownEntity_returnsTrue() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
    }

    // Tests isNamedEntity with an unknown entity
    @Test
    public void testIsNamedEntity_unknownEntity_returnsFalse() {
        assertFalse(Entities.isNamedEntity("nosuchentity"));
    }

    // Tests isNamedEntity with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testIsNamedEntity_nullInput_throwsNullPointerException() {
        Entities.isNamedEntity(null);
    }

    // Tests isBaseNamedEntity with a known base entity
    @Test
    public void testIsBaseNamedEntity_knownBaseEntity_returnsTrue() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
    }

    // Tests isBaseNamedEntity with an unknown base entity
    @Test
    public void testIsBaseNamedEntity_unknownBaseEntity_returnsFalse() {
        assertFalse(Entities.isBaseNamedEntity("thetasym"));
    }

    // Tests getCharacterByName with a known entity
    @Test
    public void testGetCharacterByName_knownEntity_returnsCorrectChar() {
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
    }

    // Tests getCharacterByName with an unknown entity
    @Test
    public void testGetCharacterByName_unknownEntity_returnsNull() {
        assertNull(Entities.getCharacterByName("nosuchentity"));
    }

    // Tests escape with no special characters
    @Test
    public void testEscape_noSpecialChars_returnsSameString() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("hello", Entities.escape("hello", out));
    }

    // Tests escape of ampersand
    @Test
    public void testEscape_ampersand_escapesToAmpersand() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("a&amp;b", Entities.escape("a&b", out));
    }

    // Tests escape of '<' when not in attribute
    @Test
    public void testEscape_lessThanNotInAttribute_escapesToLt() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("&lt;", Entities.escape("<", out));
    }

    // Tests escape of '>' when not in attribute
    @Test
    public void testEscape_greaterThanNotInAttribute_escapesToGt() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("&gt;", Entities.escape(">", out));
    }

    // Tests escape of '"' when not in attribute (should not escape)
    @Test
    public void testEscape_quoteNotInAttribute_unchanged() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("\"", Entities.escape("\"", out));
    }

    // Tests escape of non-breaking space in xhtml mode
    @Test
    public void testEscape_nbspXhtml_escapesToNumeric() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        out.charset("UTF-8");
        assertEquals("&#xa0;", Entities.escape("\u00A0", out));
    }

    // Tests escape of non-breaking space in base mode
    @Test
    public void testEscape_nbspBase_escapesToNbsp() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        assertEquals("&nbsp;", Entities.escape("\u00A0", out));
    }

    // Tests escape of character that has an entity name and is not encodable
    @Test
    public void testEscape_characterWithEntity_usesEntityName() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("US-ASCII");
        assertEquals("&copy;", Entities.escape("\u00A9", out));
    }

    // Tests escape of character without entity name, using numeric hex
    @Test
    public void testEscape_characterWithoutEntity_usesNumericHex() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("US-ASCII");
        assertEquals("&#x1000;", Entities.escape("\u1000", out));
    }

    // Tests escape of supplementary character with ASCII encoder
    @Test
    public void testEscape_surrogatePairAsciiEncoder_usesNumericHex() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("US-ASCII");
        String emoji = new String(Character.toChars(0x1F600));
        assertEquals("&#x1f600;", Entities.escape(emoji, out));
    }

    // Tests unescape with basic named entity
    @Test
    public void testUnescape_basicEntity_returnsDecoded() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
    }

    // Tests unescape with numeric entity
    @Test
    public void testUnescape_numericEntity_returnsDecoded() {
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("<", Entities.unescape("&#x3C;"));
    }

    // Tests unescape with strict mode and missing semicolon
    @Test
    public void testUnescape_strictModeMissingSemicolon_unchanged() {
        assertEquals("&amp", Entities.unescape("&amp", true));
    }

    // Tests unescape with non-strict mode and missing semicolon
    @Test
    public void testUnescape_nonStrictModeMissingSemicolon_converted() {
        assertEquals("&", Entities.unescape("&amp", false));
    }
}
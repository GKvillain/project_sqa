package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests isNamedEntity with known and unknown entities
    @Test
    public void testIsNamedEntity_validAndInvalidNames_returnsExpected() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertFalse(Entities.isNamedEntity("nonExistentEntityName123"));
    }

    // Tests isBaseNamedEntity with base, extended, and unknown entities
    @Test
    public void testIsBaseNamedEntity_baseAndExtendedNames_returnsExpected() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntityName123"));
    }

    // Tests getCharacterByName with valid and invalid entity names
    @Test
    public void testGetCharacterByName_validAndInvalidNames_returnsCharacterOrNull() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonExistentEntityName123"));
    }

    // Tests escape with basic characters in normal text data
    @Test
    public void testEscape_textDataWithSpecialCharacters_escapesCorrectly() {
        Document.OutputSettings settings = new Document.OutputSettings();
        String input = "Hello & < > \" World";
        String escaped = Entities.escape(input, settings);
        assertEquals("Hello &amp; &lt; &gt; \" World", escaped);
    }

    // Tests escape with attribute mode enabled
    @Test
    public void testEscape_attributeMode_escapesQuotesAndAmpersand() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();
        String input = "Hello & < > \" World";
        Entities.escape(accum, input, settings, true, false, false);
        assertEquals("Hello &amp; < > &quot; World", accum.toString());
    }

    // Tests escape with whitespace normalization and leading whitespace stripping
    @Test
    public void testEscape_whitespaceNormalization_collapsesAndStripsSpaces() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();
        String input = "   Hello   \t\n  World   ";
        Entities.escape(accum, input, settings, false, true, true);
        assertEquals("Hello World ", accum.toString());
    }

    // Tests escape with non-breaking space in base/extended mode vs xhtml mode
    @Test
    public void testEscape_nonBreakingSpace_handledAccordingToEscapeMode() {
        Document.OutputSettings settingsBase = new Document.OutputSettings();
        settingsBase.escapeMode(Entities.EscapeMode.base);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settingsBase));

        Document.OutputSettings settingsXhtml = new Document.OutputSettings();
        settingsXhtml.escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("&#xa0;", Entities.escape("\u00A0", settingsXhtml));
    }

    // Tests escape with US-ASCII charset where characters outside ASCII are escaped
    @Test
    public void testEscape_asciiCharset_escapesNonAsciiCharacters() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Ç ü é";
        String escaped = Entities.escape(input, settings);
        assertTrue(escaped.contains("&Ccedil;"));
        assertTrue(escaped.contains("&eacute;"));
    }

    // Tests escape with UTF-8 charset where characters within charset are not escaped
    @Test
    public void testEscape_utfCharset_preservesUnicodeCharacters() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Ç ü é";
        String escaped = Entities.escape(input, settings);
        assertEquals("Ç ü é", escaped);
    }

    // Tests escape with supplementary code points (surrogate pairs)
    @Test
    public void testEscape_supplementaryCharacters_handledCorrectly() {
        Document.OutputSettings utfSettings = new Document.OutputSettings();
        utfSettings.charset("UTF-8");
        String emoji = "\uD83D\uDE00"; // 😀 U+1F600
        assertEquals(emoji, Entities.escape(emoji, utfSettings));

        Document.OutputSettings asciiSettings = new Document.OutputSettings();
        asciiSettings.charset("US-ASCII");
        assertEquals("&#x1f600;", Entities.escape(emoji, asciiSettings));
    }

    // Tests escape with character not present in named entity map when using fallback encoding
    @Test
    public void testEscape_unmappedCharacterInAscii_outputsHexEntity() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.xhtml);
        String input = "\u03A9"; // Greek Omega (not in xhtml entity map)
        String escaped = Entities.escape(input, settings);
        assertEquals("&#x3a9;", escaped);
    }

    // Tests unescape without strict mode
    @Test
    public void testUnescape_namedAndNumericEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("Ç", Entities.unescape("&Ccedil;"));
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("A", Entities.unescape("&#x41;"));
    }

    // Tests unescape with strict parameter
    @Test
    public void testUnescape_withStrictParameter_unescapesAccordingly() {
        assertEquals("&", Entities.unescape("&amp;", true));
        assertEquals("&", Entities.unescape("&amp", false));
    }

    // Tests EscapeMode enum maps
    @Test
    public void testEscapeMode_getMap_returnsNonEmptyMap() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('<'));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('>'));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('&'));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('"'));
    }

    // Tests escape with empty string
    @Test
    public void testEscape_emptyString_returnsEmptyString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals("", Entities.escape("", settings));
    }
}
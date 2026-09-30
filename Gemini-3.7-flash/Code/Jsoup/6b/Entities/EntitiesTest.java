package org.jsoup.nodes;

import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests unescaping text without any ampersand characters
    @Test
    public void testUnescape_noEntities_returnsOriginalString() {
        String input = "Plain text with no entities.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping standard base named entities with semicolons
    @Test
    public void testUnescape_namedBaseEntities_unescapesCorrectly() {
        String input = "&lt;&gt;&amp;&quot;&apos;";
        String expected = "<>&\"'";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping base named entities without trailing semicolons
    @Test
    public void testUnescape_namedBaseEntitiesWithoutSemicolon_unescapesCorrectly() {
        String input = "&lt text &gt &amp";
        String expected = "< text > &";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping decimal numeric character references
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        String input = "&#60;&#62;&#38;&#34;&#39;";
        String expected = "<>&\"'";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping decimal entities without trailing semicolons
    @Test
    public void testUnescape_decimalNumericEntitiesWithoutSemicolon_unescapesCorrectly() {
        String input = "&#60 text &#62 &#38";
        String expected = "< text > &";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping hexadecimal numeric character references (lower and uppercase X)
    @Test
    public void testUnescape_hexNumericEntities_unescapesCorrectly() {
        String input = "&#x3c;&#X3E;&#x26;&#x22;&#x27;";
        String expected = "<>&\"'";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping extended/full named character references
    @Test
    public void testUnescape_extendedNamedEntities_unescapesCorrectly() {
        String input = "&copy; &reg; &euro; &AElig; &thorn;";
        String expected = "\u00a9 \u00ae \u20ac \u00c6 \u00fe";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping unknown entity names that should remain unchanged
    @Test
    public void testUnescape_unknownNamedEntity_preservesOriginalText() {
        String input = "&notanentity; &unknown;";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping invalid numeric formats that should remain unchanged
    @Test
    public void testUnescape_invalidNumericEntity_preservesOriginalText() {
        String input = "&#999999999999999999999999999999999; &#xZZZ;";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping supplementary characters above 0xFFFF
    @Test
    public void testUnescape_supplementaryCharacters_unescapesCorrectly() {
        String input = "&#x1D504; &#120120;";
        String expected = new String(Character.toChars(0x1D504)) + " " + new String(Character.toChars(120120));
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping mixed text containing plain characters, valid entities, and unknown entities
    @Test
    public void testUnescape_mixedString_unescapesOnlyValidEntities() {
        String input = "Hello &amp; welcome to &lt;Jsoup&gt;! &notreal; &#65;";
        String expected = "Hello & welcome to <Jsoup>! &notreal; A";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests escaping with EscapeMode.base and ASCII charset encoder
    @Test
    public void testEscape_baseModeAsciiEncoder_escapesBaseAndNonAscii() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<foo & bar> \" ' \u00a9 \u20ac";
        String escaped = Entities.escape(input, encoder, EscapeMode.base);
        assertEquals("&lt;foo &amp; bar&gt; &quot; ' &copy; &#8364;", escaped);
    }

    // Tests escaping with EscapeMode.extended and ASCII charset encoder
    @Test
    public void testEscape_extendedModeAsciiEncoder_escapesToNamedEntities() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<foo & bar> \" ' \u00a9 \u20ac";
        String escaped = Entities.escape(input, encoder, EscapeMode.extended);
        assertEquals("&lt;foo &amp; bar&gt; &quot; ' &copy; &euro;", escaped);
    }

    // Tests escaping with EscapeMode.base and UTF-8 charset encoder
    @Test
    public void testEscape_baseModeUtf8Encoder_escapesOnlyBaseEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "<foo & bar> \u00a9 \u20ac";
        String escaped = Entities.escape(input, encoder, EscapeMode.base);
        assertEquals("&lt;foo &amp; bar&gt; &copy; \u20ac", escaped);
    }

    // Tests escaping with Document.OutputSettings
    @Test
    public void testEscape_outputSettings_escapesCorrectly() {
        OutputSettings settings = new OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(EscapeMode.base);
        String input = "Hello <world> & \u00a0";
        String escaped = Entities.escape(input, settings);
        assertEquals("Hello &lt;world&gt; &amp; &nbsp;", escaped);
    }

    // Tests escaping empty string
    @Test
    public void testEscape_emptyString_returnsEmptyString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, EscapeMode.base));
    }

    // Tests unescaping empty string
    @Test
    public void testUnescape_emptyString_returnsEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    // Tests checking if a name is a known named entity
    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("euro"));
        assertFalse(Entities.isNamedEntity("nonexistentEntityName"));
    }

    // Tests checking if a name is a base named entity
    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("apos"));
        assertFalse(Entities.isBaseNamedEntity("euro"));
        assertFalse(Entities.isBaseNamedEntity("copy"));
        assertFalse(Entities.isBaseNamedEntity("nonexistentEntityName"));
    }

    // Tests retrieving character by entity name
    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf('\''), Entities.getCharacterByName("apos"));
        assertEquals(Character.valueOf('\u20ac'), Entities.getCharacterByName("euro"));
        assertNull(Entities.getCharacterByName("unknownEntity"));
    }

    // Tests strict unescaping mode where missing semicolon should not unescape
    @Test
    public void testUnescape_strictMode() {
        assertEquals("&lt text", Entities.unescape("&lt text", true));
        assertEquals("< text", Entities.unescape("&lt; text", true));
        assertEquals("&#60 text", Entities.unescape("&#60 text", true));
        assertEquals("< text", Entities.unescape("&#60; text", true));
    }

    // Tests unescaping incomplete ampersand patterns at the end of string
    @Test
    public void testUnescape_danglingAmpersands() {
        assertEquals("&", Entities.unescape("&"));
        assertEquals("foo &", Entities.unescape("foo &"));
        assertEquals("&#", Entities.unescape("&#"));
        assertEquals("&#x", Entities.unescape("&#x"));
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
    }

    // Tests EscapeMode enum values and valueOf
    @Test
    public void testEscapeMode_enumValues() {
        assertEquals(EscapeMode.base, EscapeMode.valueOf("base"));
        assertEquals(EscapeMode.extended, EscapeMode.valueOf("extended"));
        assertEquals(2, EscapeMode.values().length);
    }

    // Tests escaping with supplementary characters (surrogate pairs) in ASCII
    @Test
    public void testEscape_supplementaryCharactersAscii() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = new String(Character.toChars(0x1D504));
        String escaped = Entities.escape(input, encoder, EscapeMode.base);
        assertEquals("&#120068;", escaped);
    }
}
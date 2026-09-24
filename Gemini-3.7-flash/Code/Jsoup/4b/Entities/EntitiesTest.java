package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests string without ampersand returns immediately without modification
    @Test
    public void testUnescape_noAmpersand_returnsOriginal() {
        String input = "Hello World! No entities here.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests empty string handling
    @Test
    public void testUnescape_emptyString_returnsEmpty() {
        assertEquals("", Entities.unescape(""));
    }

    // Tests unescaping common named entities with semicolon
    @Test
    public void testUnescape_namedEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
        assertEquals("©", Entities.unescape("&copy;"));
    }

    // Tests unescaping decimal numeric character references
    @Test
    public void testUnescape_decimalEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("&", Entities.unescape("&#38;"));
        assertEquals("©", Entities.unescape("&#169;"));
    }

    // Tests unescaping lowercase and uppercase hexadecimal character references
    @Test
    public void testUnescape_hexEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("A", Entities.unescape("&#X41;"));
        assertEquals("—", Entities.unescape("&#x2014;"));
    }

    // Tests unescaping entities without trailing semicolon
    @Test
    public void testUnescape_withoutTrailingSemicolon_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("A", Entities.unescape("&#x41"));
    }

    // Tests unknown named entity remains unchanged
    @Test
    public void testUnescape_unknownNamedEntity_leavesUnchanged() {
        assertEquals("&foobar;", Entities.unescape("&foobar;"));
        assertEquals("&notanentity;", Entities.unescape("&notanentity;"));
    }

    // Tests invalid numeric format branch (NumberFormatException path)
    @Test
    public void testUnescape_invalidNumericEntity_leavesUnchanged() {
        assertEquals("&#99999999999999999999;", Entities.unescape("&#99999999999999999999;"));
    }

    // Tests mixed text containing multiple entities and plain text
    @Test
    public void testUnescape_mixedContent_unescapesAllEntities() {
        String input = "One &amp; Two &lt; Three &gt; &#65; &#x42;";
        String expected = "One & Two < Three > A B";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests escaping with base escape mode and ASCII encoder
    @Test
    public void testEscape_baseModeAsciiEncoder_escapesMappedCharacters() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<foo & \"bar\">";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&lt;foo &amp; &quot;bar&quot;&gt;", escaped);
    }

    // Tests escaping with extended mode
    @Test
    public void testEscape_extendedMode_escapesExtendedEntities() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "© & ®";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);
        assertEquals("&copy; &amp; &reg;", escaped);
    }

    // Tests escaping characters that encoder can encode without mapping
    @Test
    public void testEscape_utf8Encoder_leavesUnmappedEncodableCharacters() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        String input = "Hello 世界!";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);
        assertEquals("Hello 世界!", escaped);
    }

    // Tests escaping characters that cannot be encoded by encoder and not in base map (numeric fallback)
    @Test
    public void testEscape_unencodableUnmappedCharacter_escapesAsNumericEntity() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Hello \u4e2d\u6587";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("Hello &#20013;&#25991;", escaped);
    }

    // Tests escape overload taking Document.OutputSettings
    @Test
    public void testEscape_withOutputSettings_escapesCorrectly() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().charset("US-ASCII");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);

        String input = "1 < 2 & 3 > 0";
        String escaped = Entities.escape(input, doc.outputSettings());
        assertEquals("1 &lt; 2 &amp; 3 &gt; 0", escaped);
    }

    // Tests EscapeMode enum values and valueOf
    @Test
    public void testEscapeMode_enumValues_returnsValidConstants() {
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
        assertEquals(2, Entities.EscapeMode.values().length);
    }

    // Tests isNamedEntity helper method
    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("nonExistentEntityName"));
    }

    // Tests isBaseNamedEntity helper method
    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertFalse(Entities.isBaseNamedEntity("copy"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntityName"));
    }

    // Tests getCharacterByName helper method
    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('©'), Entities.getCharacterByName("copy"));
        assertNull(Entities.getCharacterByName("nonExistentEntityName"));
    }

    // Tests strict unescaping mode (requires trailing semicolons)
    @Test
    public void testUnescape_strictMode() {
        assertEquals("&", Entities.unescape("&amp;", true));
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("A", Entities.unescape("&#65;", true));
        assertEquals("&#65", Entities.unescape("&#65", true));
        assertEquals("A", Entities.unescape("&#x41;", true));
        assertEquals("&#x41", Entities.unescape("&#x41", true));
        assertEquals("No entities", Entities.unescape("No entities", true));
    }

    // Tests numeric entity beyond maximum character value range (> 0xFFFF)
    @Test
    public void testUnescape_numericCodePointAboveMaxChar_leavesUnchanged() {
        assertEquals("&#65536;", Entities.unescape("&#65536;"));
        assertEquals("&#x10000;", Entities.unescape("&#x10000;"));
    }

    // Tests Entities instantiation coverage
    @Test
    public void testConstructor() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }
}
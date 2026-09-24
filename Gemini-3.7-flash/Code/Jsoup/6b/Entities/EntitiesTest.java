package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests unescaping a string containing no ampersand
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "Hello World! No entities here.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping named entities with semicolon
    @Test
    public void testUnescape_namedEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("©", Entities.unescape("&copy;"));
        assertEquals("Æ", Entities.unescape("&AElig;"));
    }

    // Tests unescaping named entities without semicolon
    @Test
    public void testUnescape_namedEntitiesWithoutSemicolon_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
        assertEquals(">", Entities.unescape("&gt"));
        assertEquals("\"", Entities.unescape("&quot"));
    }

    // Tests unescaping decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("z", Entities.unescape("&#122;"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("$", Entities.unescape("&#36;"));
    }

    // Tests unescaping hex numeric entities (both lower and upper case x)
    @Test
    public void testUnescape_hexNumericEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("A", Entities.unescape("&#X41;"));
        assertEquals("z", Entities.unescape("&#x7a;"));
        assertEquals("“", Entities.unescape("&#x201c;"));
    }

    // Tests unescaping invalid / unknown named entities
    @Test
    public void testUnescape_unknownNamedEntity_leavesAsIs() {
        assertEquals("&notanentity;", Entities.unescape("&notanentity;"));
        assertEquals("&foobar;", Entities.unescape("&foobar;"));
    }

    // Tests unescaping numeric entities with number format overflow
    @Test
    public void testUnescape_numericOverflow_leavesAsIs() {
        assertEquals("&#9999999999999999999999999999999999;", Entities.unescape("&#9999999999999999999999999999999999;"));
    }

    // Tests unescaping mixed text containing valid entities, text, and unknown entities
    @Test
    public void testUnescape_mixedText_unescapesValidEntitiesOnly() {
        String input = "one &amp; two &lt; three &gt; four &notanentity; &#65; &#x42;";
        String expected = "one & two < three > four &notanentity; A B";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests escaping with base escape mode and ASCII encoder
    @Test
    public void testEscape_baseModeAsciiEncoder_escapesBaseEntitiesAndUnencodable() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Hello & < > \" ' © \u00E9";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("Hello &amp; &lt; &gt; &quot; ' &copy; &eacute;", escaped);
    }

    // Tests escaping with base mode when non-base entity is unencodable in ASCII
    @Test
    public void testEscape_baseModeUnencodableNonBaseChar_escapesToNumericEntity() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        // '\u0102' is Abreve, which is in fullArray but not baseArray
        String input = "Abreve: \u0102";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("Abreve: &#258;", escaped);
    }

    // Tests escaping with extended mode and ASCII encoder
    @Test
    public void testEscape_extendedModeAsciiEncoder_escapesAllNamedEntities() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u0102 \u2265";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.extended);
        assertEquals("&Abreve; &ge;", escaped);
    }

    // Tests escaping with UTF-8 encoder where characters can be encoded directly
    @Test
    public void testEscape_utf8EncoderBaseMode_onlyEscapesMappedBaseEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "<foo> & © \u4E16";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("&lt;foo&gt; &amp; &copy; \u4E16", escaped);
    }

    // Tests escaping using Document.OutputSettings
    @Test
    public void testEscape_withOutputSettings_escapesCorrectly() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "<a> & \"foo\" ©";
        String escaped = Entities.escape(input, settings);
        assertEquals("&lt;a&gt; &amp; &quot;foo&quot; &copy;", escaped);
    }

    // Tests EscapeMode enum values and valueOf
    @Test
    public void testEscapeMode_enumValues_matchExpected() {
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
        assertEquals(2, Entities.EscapeMode.values().length);
    }
}
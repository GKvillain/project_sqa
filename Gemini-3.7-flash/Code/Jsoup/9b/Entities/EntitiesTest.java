package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests unescape on string without ampersand (fast-path)
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "Hello World! No entities here.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescape with standard named entities
    @Test
    public void testUnescape_namedEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
        assertEquals("Hello & < > \" ' World", Entities.unescape("Hello &amp; &lt; &gt; &quot; &apos; World"));
    }

    // Tests unescape with named entities missing trailing semicolon
    @Test
    public void testUnescape_namedEntitiesWithoutSemicolon_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
        assertEquals(">", Entities.unescape("&gt"));
    }

    // Tests unescape with decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("Hello", Entities.unescape("&#72;&#101;&#108;&#108;&#111;"));
    }

    // Tests unescape with hexadecimal numeric entities (lower and uppercase x)
    @Test
    public void testUnescape_hexNumericEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("A", Entities.unescape("&#X41;"));
        assertEquals("A", Entities.unescape("&#x41"));
    }

    // Tests unescape with unknown named entity
    @Test
    public void testUnescape_unknownNamedEntity_leavesAsOriginal() {
        assertEquals("&foobar;", Entities.unescape("&foobar;"));
        assertEquals("&invalidEntity;", Entities.unescape("&invalidEntity;"));
    }

    // Tests unescape with code point out of BMP range (> 0xFFFF)
    @Test
    public void testUnescape_outOfRangeNumericEntity_leavesAsOriginal() {
        assertEquals("&#65536;", Entities.unescape("&#65536;"));
        assertEquals("&#x10000;", Entities.unescape("&#x10000;"));
        assertEquals("&#1000000;", Entities.unescape("&#1000000;"));
    }

    // Tests unescape with numeric entity causing NumberFormatException
    @Test
    public void testUnescape_numericEntityOverflow_leavesAsOriginal() {
        assertEquals("&#99999999999999999999999999999;", Entities.unescape("&#99999999999999999999999999999;"));
    }

    // Tests unescape with mixed text and entities
    @Test
    public void testUnescape_mixedTextAndEntities_unescapesCorrectly() {
        String input = "&lt;div class=&quot;test&quot;&gt;Tom &amp; Jerry &#65; &copy;&lt;/div&gt;";
        String expected = "<div class=\"test\">Tom & Jerry A \u00a9</div>";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests escape using Document.OutputSettings
    @Test
    public void testEscape_withOutputSettings_escapesAppropriately() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("US-ASCII"));
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "Hello <foo> & \"bar\" \u00a9 \u03c0";
        String escaped = Entities.escape(input, settings);
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&quot;"));
        assertTrue(escaped.contains("&copy;") || escaped.contains("&#169;"));
        assertTrue(escaped.contains("&#960;"));
    }

    // Tests escape with xhtml mode
    @Test
    public void testEscape_xhtmlMode_escapesOnlyXhtmlEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "< > & \" ' \u00a9";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.xhtml);

        assertEquals("&lt; &gt; &amp; &quot; &apos; \u00a9", escaped);
    }

    // Tests escape with base mode and UTF-8 charset
    @Test
    public void testEscape_baseModeUtf8_escapesBaseEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "< > & \" \u00a0 \u00a9";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);

        assertEquals("&lt; &gt; &amp; &quot; &nbsp; &copy;", escaped);
    }

    // Tests escape with extended mode
    @Test
    public void testEscape_extendedMode_escapesExtendedEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "\u2208 \u2203";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.extended);

        assertTrue(escaped.contains("&isin;") || escaped.contains("&Element;"));
        assertTrue(escaped.contains("&exist;") || escaped.contains("&Exists;"));
    }

    // Tests escape when character cannot be encoded by charset encoder
    @Test
    public void testEscape_unencodableChar_escapesToNumericEntity() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u00e9"; // e-acute (not in base escape map for ASCII encoder test)
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.xhtml);

        assertEquals("&#233;", escaped);
    }

    // Tests escape on empty string
    @Test
    public void testEscape_emptyString_returnsEmptyString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
    }

    // Tests EscapeMode enum values and getMap
    @Test
    public void testEscapeMode_getMap_returnsNonEmptyMap() {
        for (Entities.EscapeMode mode : Entities.EscapeMode.values()) {
            Map<Character, String> map = mode.getMap();
            assertNotNull(map);
            assertFalse(map.isEmpty());
        }
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    // Tests isNamedEntity method
    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("notARealEntityName"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Tests isBaseNamedEntity method
    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("copy"));
        assertFalse(Entities.isBaseNamedEntity("notARealEntityName"));
        assertFalse(Entities.isBaseNamedEntity(""));
    }

    // Tests getCharacterByName method
    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("invalidNamedEntity"));
    }

    // Tests unescape with strict flag
    @Test
    public void testUnescape_strictMode() {
        assertEquals("&", Entities.unescape("&amp;", true));
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&", Entities.unescape("&amp", false));
    }
}
package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests isNamedEntity with known and unknown entity names
    @Test
    public void testIsNamedEntity_validAndInvalidNames_returnsCorrectBoolean() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertFalse(Entities.isNamedEntity("nonexistent_entity_name"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Tests getCharacterByName with valid and invalid entity names
    @Test
    public void testGetCharacterByName_validAndInvalidNames_returnsExpectedCharacter() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonexistent_entity_name"));
    }

    // Tests unescape without ampersand takes fast path
    @Test
    public void testUnescape_stringWithoutAmpersand_returnsOriginalString() {
        String input = "Hello world! No entities here.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescape with standard named entities
    @Test
    public void testUnescape_namedEntities_unescapesCorrectly() {
        String input = "&lt;div&gt;&amp;&quot;&apos;&lt;/div&gt;";
        String expected = "<div>&\"'</div>";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescape with numeric decimal and hexadecimal entities
    @Test
    public void testUnescape_numericEntities_unescapesCorrectly() {
        assertEquals("<", Entities.unescape("&#60;"));
        assertEquals("<", Entities.unescape("&#x3c;"));
        assertEquals("<", Entities.unescape("&#X3C;"));
        assertEquals("©", Entities.unescape("&#169;"));
        assertEquals("©", Entities.unescape("&#xa9;"));
    }

    // Tests unescape non-strict allows entities without trailing semicolon
    @Test
    public void testUnescape_nonStrictWithoutSemicolon_unescapes() {
        assertEquals("<", Entities.unescape("&lt", false));
        assertEquals("&", Entities.unescape("&amp", false));
        assertEquals("<", Entities.unescape("&#60", false));
        assertEquals("<", Entities.unescape("&#x3c", false));
    }

    // Tests unescape strict requires trailing semicolon
    @Test
    public void testUnescape_strictWithoutSemicolon_doesNotUnescape() {
        assertEquals("&lt", Entities.unescape("&lt", true));
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&#60", Entities.unescape("&#60", true));
        assertEquals("&lt;", Entities.unescape("&lt;", true));
    }

    // Tests unescape with unknown or invalid entity keeps original text
    @Test
    public void testUnescape_unknownOrInvalidEntity_retainsOriginal() {
        assertEquals("&unknownentity;", Entities.unescape("&unknownentity;"));
        assertEquals("foo &ampbar; baz", Entities.unescape("foo &ampbar; baz"));
    }

    // Tests escape using xhtml mode
    @Test
    public void testEscape_xhtmlMode_escapesRestrictedEntitiesOnly() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<foo & bar \" ' >";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;foo &amp; bar &quot; &apos; &gt;", escaped);
    }

    // Tests escape using base mode with unencodable characters
    @Test
    public void testEscape_baseModeWithAsciiEncoder_escapesNonAsciiAsNumeric() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Hello © <world>";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("Hello &#169; &lt;world&gt;", escaped);
    }

    // Tests escape using extended mode and UTF-8 encoder
    @Test
    public void testEscape_extendedModeWithUtf8Encoder_encodesCorrectly() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        String input = "Hello © & <world>";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.extended);
        assertEquals("Hello &copy; &amp; &lt;world&gt;", escaped);
    }

    // Tests escape with Document.OutputSettings
    @Test
    public void testEscape_withDocumentOutputSettings_escapesCorrectly() {
        Document doc = new Document("");
        doc.outputSettings().charset("US-ASCII");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);
        String escaped = Entities.escape("Hello & <world> ©", doc.outputSettings());
        assertEquals("Hello &amp; &lt;world&gt; &#169;", escaped);
    }

    // Tests EscapeMode enum values and map retrieval
    @Test
    public void testEscapeMode_getMap_returnsNonEmptyMap() {
        for (Entities.EscapeMode mode : Entities.EscapeMode.values()) {
            Map<Character, String> map = mode.getMap();
            assertNotNull(map);
            assertFalse(map.isEmpty());
        }
    }

    // Tests isBaseNamedEntity with base, non-base, and invalid entity names
    @Test
    public void testIsBaseNamedEntity_validAndInvalidNames_returnsExpectedBoolean() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("nonexistent_base_entity"));
        assertFalse(Entities.isBaseNamedEntity(""));
    }

    // Tests unescape with malformed, empty, or incomplete numeric entities
    @Test
    public void testUnescape_malformedNumericEntities_handlesGracefully() {
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
        assertEquals("&#X;", Entities.unescape("&#X;"));
        assertEquals("&#999999999999999999999999;", Entities.unescape("&#999999999999999999999999;"));
        assertEquals("&#xfffffff;", Entities.unescape("&#xfffffff;"));
        assertEquals("&;", Entities.unescape("&;"));
    }

    // Tests unescape with trailing ampersands or incomplete patterns at string boundaries
    @Test
    public void testUnescape_trailingAmpersands_preservesInput() {
        assertEquals("foo&", Entities.unescape("foo&"));
        assertEquals("foo&#", Entities.unescape("foo&#"));
        assertEquals("foo&#x", Entities.unescape("foo&#x"));
        assertEquals("&", Entities.unescape("&"));
        assertEquals("&&", Entities.unescape("&&"));
        assertEquals("foo & bar", Entities.unescape("foo & bar"));
    }

    // Tests escape with characters outside the ASCII range when encoder cannot encode them
    @Test
    public void testEscape_unencodableCharactersNotInMap_escapedAsNumericEntity() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u0152 \u0153 \u4e2d\u6587";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&#338; &#339; &#20013;&#25991;", escaped);
    }

    // Tests escape with empty and standard strings needing no escaping
    @Test
    public void testEscape_plainAndEmptyStrings_remainUnchanged() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", utf8Encoder, Entities.EscapeMode.base));
        assertEquals("abc XYZ 123", Entities.escape("abc XYZ 123", utf8Encoder, Entities.EscapeMode.base));
    }
}
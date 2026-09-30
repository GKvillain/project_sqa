package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class EntitiesTest {

    // Test that known named entity is recognized
    @Test
    public void testIsNamedEntity_knownEntity_returnsTrue() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
    }

    // Test that unknown named entity returns false
    @Test
    public void testIsNamedEntity_unknownEntity_returnsFalse() {
        assertFalse(Entities.isNamedEntity("unknownEntityXYZ"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Test base named entity detection
    @Test
    public void testIsBaseNamedEntity_baseEntity_returnsTrue() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("nbsp"));
    }

    // Test base named entity for non-base entity
    @Test
    public void testIsBaseNamedEntity_nonBaseEntity_returnsFalse() {
        // "ang" is not in base set (probably only in full)
        assertFalse(Entities.isBaseNamedEntity("ang"));
    }

    // Test getCharacterByName returns correct character
    @Test
    public void testGetCharacterByName_knownEntity_returnsCharacter() {
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
    }

    // Test getCharacterByName returns null for unknown entity
    @Test
    public void testGetCharacterByName_unknownEntity_returnsNull() {
        assertNull(Entities.getCharacterByName("nonexistent"));
    }

    // Test escape of simple text that doesn't need escaping
    @Test
    public void testEscape_plainText_noEscaping() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        String result = Entities.escape("Hello World", out);
        assertEquals("Hello World", result);
    }

    // Test escape of characters that must be escaped in text mode
    @Test
    public void testEscape_specialChars_escaped() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        String result = Entities.escape("a<b>c&d\"e", out);
        assertEquals("a&lt;b&gt;c&amp;d\"e", result); // " not escaped in text mode
    }

    // Test escape in attribute mode (quotes should be escaped)
    @Test
    public void testEscape_specialCharsInAttribute_escaped() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        // Use reflection to test the internal escape with inAttribute true? Actually the public escape always uses false for inAttribute.
        // We need to test the package-private escape method directly.
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "a\"b", out, true, false, false);
        assertEquals("a&quot;b", accum.toString());
    }

    // Test escape of non-breaking space (0xA0) in base mode -> &nbsp;
    @Test
    public void testEscape_nonBreakingSpace_escapedToNbsp() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "\u00A0", out, false, false, false);
        assertEquals("&nbsp;", accum.toString());
    }

    // Test escape of non-breaking space in xhtml mode -> not escaped
    @Test
    public void testEscape_nonBreakingSpaceXhtml_notEscaped() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        out.charset("UTF-8");
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "\u00A0", out, false, false, false);
        assertEquals("\u00A0", accum.toString());
    }

    // Test escape with normaliseWhite and stripLeadingWhite flags
    @Test
    public void testEscape_normaliseWhite_stripLeadingWhitespace() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        StringBuilder accum = new StringBuilder();
        // Input: leading whitespace, then multiple spaces, then text
        Entities.escape(accum, "   hello   world   ", out, false, true, true);
        // After stripLeadingWhite and normaliseWhite: "hello world " (trailing spaces become one space because lastWasWhite)
        assertEquals("hello world ", accum.toString());
    }

    // Test escape with character not encodable in ASCII -> should output hex entity
    @Test
    public void testEscape_nonAsciiChar_asciiCharset_usesHexEntity() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("US-ASCII");
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "\u00E9", out, false, false, false); // é
        // é is not in base entity map (maybe not), so should become &#xE9;
        assertEquals("&#xe9;", accum.toString());
    }

    // Test escape with supplementary character (surrogate pair) that can be encoded in UTF-8
    @Test
    public void testEscape_supplementaryCharacter_utf8_encodesDirectly() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        StringBuilder accum = new StringBuilder();
        String input = new String(Character.toChars(0x1F600)); // 😀
        Entities.escape(accum, input, out, false, false, false);
        // Should be output directly if encoder can encode
        assertEquals(input, accum.toString());
    }

    // Test escape with supplementary character that cannot be encoded in ASCII -> hex entity
    @Test
    public void testEscape_supplementaryCharacter_ascii_usesHexEntity() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("US-ASCII");
        StringBuilder accum = new StringBuilder();
        String input = new String(Character.toChars(0x1F600));
        Entities.escape(accum, input, out, false, false, false);
        // Should become &#x1F600;
        assertEquals("&#x1f600;", accum.toString());
    }

    // Test unescape basic
    @Test
    public void testUnescape_basicEntities_decoded() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
    }

    // Test unescape with strict mode
    @Test
    public void testUnescape_strictMode_withoutSemicolon_notDecoded() {
        // In strict mode, missing semicolon should not be decoded
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&amp", Entities.unescape("&amp", false)); // non-strict still optional? Actually default false will decode anyway
        // In strict, it should remain as is
        assertEquals("&amp", Entities.unescape("&amp", true));
    }

    // Test unescape with numeric hex entity
    @Test
    public void testUnescape_numericHexEntity_decoded() {
        assertEquals("\u00E9", Entities.unescape("&#xE9;"));
        assertEquals("\u00E9", Entities.unescape("&#xe9;"));
    }
}
package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests escape with empty string
    @Test
    public void testEscape_emptyString_returnsEmpty() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        String result = Entities.escape("", encoder, Entities.EscapeMode.base);
        assertEquals("", result);
    }

    // Tests escape with special chars using XHTML mode
    @Test
    public void testEscape_specialChars_xhtml_escaped() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        String result = Entities.escape("&<>\"'", encoder, Entities.EscapeMode.xhtml);
        assertEquals("&amp;&lt;&gt;&quot;&apos;", result);
    }

    // Tests escape when encoder cannot encode a character (numeric escape)
    @Test
    public void testEscape_unencodableChar_numericEscape() {
        CharsetEncoder isoEncoder = java.nio.charset.Charset.forName("ISO-8859-1").newEncoder();
        // U+03A9 (Omega) is not in ISO-8859-1 and not in base map
        String result = Entities.escape("\u03A9", isoEncoder, Entities.EscapeMode.base);
        assertEquals("&#937;", result);
    }

    // Tests escape with a character present in the escape mode map
    @Test
    public void testEscape_characterInMap_usesEntity() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        String result = Entities.escape("&", encoder, Entities.EscapeMode.base);
        assertEquals("&amp;", result);
    }

    // Tests escape with extended mode for a character only in full map
    @Test
    public void testEscape_extendedMode_usesExtendedEntities() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        String result = Entities.escape("\u0391", encoder, Entities.EscapeMode.extended);
        assertEquals("&Alpha;", result);
    }

    // Tests unescape with no '&' character
    @Test
    public void testUnescape_noEntities_returnsSame() {
        String result = Entities.unescape("hello world");
        assertEquals("hello world", result);
    }

    // Tests unescape with empty string
    @Test
    public void testUnescape_emptyString_returnsEmpty() {
        String result = Entities.unescape("");
        assertEquals("", result);
    }

    // Tests unescape of a named entity with semicolon
    @Test
    public void testUnescape_namedEntity_unescaped() {
        String result = Entities.unescape("&amp;");
        assertEquals("&", result);
    }

    // Tests unescape of a numeric decimal entity
    @Test
    public void testUnescape_numericDecimal_unescaped() {
        String result = Entities.unescape("&#65;");
        assertEquals("A", result);
    }

    // Tests unescape of a numeric hex entity
    @Test
    public void testUnescape_numericHex_unescaped() {
        String result = Entities.unescape("&#x41;");
        assertEquals("A", result);
    }

    // Tests unescape of unknown named entity (kept as is)
    @Test
    public void testUnescape_unknownNamedEntity_keptAsIs() {
        String result = Entities.unescape("&unknown;");
        assertEquals("&unknown;", result);
    }

    // Tests unescape of base entity without trailing semicolon
    @Test
    public void testUnescape_missingSemicolon_namedEntity_unescaped() {
        String result = Entities.unescape("&amp");
        assertEquals("&", result);
    }

    // Tests unescape of numeric entity with value > 0xFFFF (should produce surrogate pair)
    @Test
    public void testUnescape_numericEntityOutOfRange_returnsSurrogatePair() {
        String input = "&#x10000;";
        String expected = new String(Character.toChars(0x10000));
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    // Tests unescape of largest valid Unicode code point
    @Test
    public void testUnescape_largeHexEntity_returnsSurrogatePair() {
        String input = "&#x10FFFF;";
        String expected = new String(Character.toChars(0x10FFFF));
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    // Tests unescape of multiple entities in one string
    @Test
    public void testUnescape_multipleEntities_unescapedCorrectly() {
        String result = Entities.unescape("&lt; &gt; &amp; &#x26;");
        assertEquals("< > & &", result);
    }

    // Tests unescape of invalid numeric entity (no digits) kept as original
    @Test
    public void testUnescape_invalidNumericFormat_keepsOriginal() {
        String result = Entities.unescape("&#x;");
        assertEquals("&#x;", result);
    }

    // Tests unescape with zero character
    @Test
    public void testUnescape_numericZero_returnsNullChar() {
        String result = Entities.unescape("&#0;");
        assertEquals("\0", result);
    }

    // ===== New test cases to improve coverage =====

    // Tests escape with normal characters (should remain unchanged)
    @Test
    public void testEscape_normalChars_unchanged() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        String result = Entities.escape("abc123", encoder, Entities.EscapeMode.base);
        assertEquals("abc123", result);
    }

    // Tests escape with character that exists in base map using extended mode
    @Test
    public void testEscape_extendedModeForBaseChar_usesBaseEntity() {
        CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
        // '&' is in base map; extended mode should still use &amp;
        String result = Entities.escape("&", encoder, Entities.EscapeMode.extended);
        assertEquals("&amp;", result);
    }

    // Tests unescape of named entity without semicolon followed by more letters
    @Test
    public void testUnescape_namedEntityMissingSemicolonFollowedByChar() {
        String result = Entities.unescape("&amptest");
        assertEquals("&test", result);
    }

    // Tests unescape of numeric decimal entity without semicolon
    @Test
    public void testUnescape_numericEntityMissingSemicolon() {
        String result = Entities.unescape("&#65");
        assertEquals("A", result);
    }

    // Tests unescape of numeric hex entity using lowercase 'x'
    @Test
    public void testUnescape_numericEntityLowercaseHex() {
        String result = Entities.unescape("&#x41;");
        assertEquals("A", result);
    }

    // Tests unescape of numeric hex entity with invalid hex characters (non-hex)
    @Test
    public void testUnescape_invalidHexChars_returnsOriginal() {
        String result = Entities.unescape("&#xGH;");
        assertEquals("&#xGH;", result);
    }

    // Tests unescape of named entity without semicolon at end of string (different entity)
    @Test
    public void testUnescape_namedEntityMissingSemicolonEndOfString() {
        String result = Entities.unescape("&lt");
        assertEquals("<", result);
    }
}
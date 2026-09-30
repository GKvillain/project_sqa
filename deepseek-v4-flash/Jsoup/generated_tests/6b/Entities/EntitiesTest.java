package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests escape with base mode on a regular character
    @Test
    public void testEscape_baseMode_regularCharacter_returnsEncoded() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        String result = Entities.escape("a", out);
        assertEquals("a", result);
    }

    // Tests escape with extended mode on a regular character
    @Test
    public void testEscape_extendedMode_regularCharacter_returnsEncoded() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        String result = Entities.escape("a", out);
        assertEquals("a", result);
    }

    // Tests escape with base mode on an entity character (ampersand)
    @Test
    public void testEscape_baseMode_ampersand_returnsEntity() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        String result = Entities.escape("&", out);
        assertEquals("&amp;", result);
    }

    // Tests escape with extended mode on a non-base but extended entity character
    @Test
    public void testEscape_extendedMode_nonBaseEntity_returnsExtendedEntity() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        String result = Entities.escape("\u00E1", out);  // aacute
        assertEquals("&aacute;", result);
    }

    // Tests escape with base mode on a non-base, non-extended but encodable character
    @Test
    public void testEscape_baseMode_nonEntityEncodable_returnsSameCharacter() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        String result = Entities.escape("b", out);
        assertEquals("b", result);
    }

    // Tests escape with character that cannot be encoded by encoder (fallback to numeric)
    @Test
    public void testEscape_encoderCannotEncode_fallbackToNumeric() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        // Use a character outside ASCII range that is not in baseByVal and encoder can't encode
        // This simulates a character not in map and not encodable; we use a high Unicode.
        java.nio.charset.CharsetEncoder encoder = java.nio.charset.Charset.forName("ASCII").newEncoder();
        String result = Entities.escape("\u20AC", encoder, Entities.EscapeMode.base);
        assertEquals("&#8364;", result);
    }

    // Tests unescape with a valid base entity (with semicolon)
    @Test
    public void testUnescape_validBaseEntityWithSemicolon_returnsDecoded() {
        String result = Entities.unescape("&amp;");
        assertEquals("&", result);
    }

    // Tests unescape with a valid full entity (with semicolon)
    @Test
    public void testUnescape_validFullEntityWithSemicolon_returnsDecoded() {
        String result = Entities.unescape("&aacute;");
        assertEquals("\u00E1", result);
    }

    // Tests unescape with a hexadecimal numeric entity
    @Test
    public void testUnescape_hexNumericEntity_returnsDecoded() {
        String result = Entities.unescape("&#x26;");
        assertEquals("&", result);
    }

    // Tests unescape with a decimal numeric entity
    @Test
    public void testUnescape_decimalNumericEntity_returnsDecoded() {
        String result = Entities.unescape("&#38;");
        assertEquals("&", result);
    }

    // Tests unescape with an out-of-range character value (> 0xFFFF) which should still be decoded
    @Test
    public void testUnescape_outOfRangeCharValue_returnsDecoded() {
        // This represents a character > 0xFFFF (e.g., 0x10000)
        String result = Entities.unescape("&#65536;");
        assertEquals("\uD800\uDC00", result);
    }

    // Tests unescape with an unknown entity name (should remain as-is)
    @Test
    public void testUnescape_unknownEntityName_returnsOriginal() {
        String result = Entities.unescape("&unknown;");
        assertEquals("&unknown;", result);
    }

    // Tests unescape with a non-entity string (no ampersand)
    @Test
    public void testUnescape_noAmpersand_returnsSameString() {
        String result = Entities.unescape("hello");
        assertEquals("hello", result);
    }

    // Tests unescape with empty string
    @Test
    public void testUnescape_emptyString_returnsEmpty() {
        String result = Entities.unescape("");
        assertEquals("", result);
    }

    // Tests unescape with a malformed numeric entity (invalid number format) should skip
    @Test
    public void testUnescape_malformedNumericEntity_returnsOriginal() {
        String result = Entities.unescape("&#abc;");
        assertEquals("&#abc;", result);
    }

    // Tests unescape with a base entity without trailing semicolon (e.g., &amp)
    @Test
    public void testUnescape_baseEntityWithoutSemicolon_returnsDecoded() {
        String result = Entities.unescape("&amp");
        assertEquals("&", result);
    }

    // Tests escape with empty string
    @Test
    public void testEscape_emptyString_returnsEmpty() {
        Document.OutputSettings out = new Document("").new OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        String result = Entities.escape("", out);
        assertEquals("", result);
    }

    // Tests unescape with entity at start and end of string
    @Test
    public void testUnescape_entitiesAtStartAndEnd_returnsDecoded() {
        String result = Entities.unescape("&amp;start&amp;");
        assertEquals("&start&", result);
    }
}
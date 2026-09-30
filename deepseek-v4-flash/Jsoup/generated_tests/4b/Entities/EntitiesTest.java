package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    // Tests that a character in base escape map is escaped correctly
    public void testEscape_characterInBaseMap_returnsEntity() {
        assertEquals("&amp;", Entities.escape("&", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.base));
    }

    @Test
    // Tests that a character only in extended escape map is escaped correctly in extended mode
    public void testEscape_characterInExtendedMap_returnsEntity() {
        // Character Ă (U+0102) is in fullByVal but not in baseByVal
        assertEquals("&abreve;", Entities.escape("\u0102", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    // Tests that a character not in escape map but encodable is kept as-is
    public void testEscape_characterNotInMapEncodable_returnsSame() {
        assertEquals("a", Entities.escape("a", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.base));
    }

    @Test
    // Tests that a character not encodable by the given encoder is escaped as numeric
    public void testEscape_characterNotEncodable_returnsNumeric() {
        CharsetEncoder isoEncoder = java.nio.charset.Charset.forName("ISO-8859-1").newEncoder();
        String chinese = "\u4E2D";
        assertFalse(isoEncoder.canEncode(chinese));
        assertEquals("&#20013;", Entities.escape(chinese, isoEncoder, Entities.EscapeMode.base));
    }

    @Test
    // Tests that empty string remains empty
    public void testEscape_emptyString_returnsEmpty() {
        assertEquals("", Entities.escape("", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.base));
    }

    @Test
    // Tests unescape of a string without any ampersand
    public void testUnescape_noAmpersand_returnsSame() {
        assertEquals("hello", Entities.unescape("hello"));
    }

    @Test
    // Tests unescape of a named entity with semicolon
    public void testUnescape_namedEntity_returnsChar() {
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    // Tests unescape of a numeric decimal entity
    public void testUnescape_numericDecEntity_returnsChar() {
        assertEquals("&", Entities.unescape("&#38;"));
    }

    @Test
    // Tests unescape of a numeric hex entity (lowercase x)
    public void testUnescape_numericHexEntity_returnsChar() {
        assertEquals("&", Entities.unescape("&#x26;"));
    }

    @Test
    // Tests unescape of a named entity without trailing semicolon
    public void testUnescape_entityWithoutSemicolon_returnsChar() {
        assertEquals("&", Entities.unescape("&amp"));
    }

    @Test
    // Tests unescape of an unknown entity returns the original text
    public void testUnescape_unknownEntity_returnsOriginal() {
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
    }

    @Test
    // Tests unescape of a numeric entity with invalid base (hex digit treated as decimal) -> NumberFormatException -> original
    public void testUnescape_numericEntityInvalidBase_returnsOriginal() {
        assertEquals("&#a;", Entities.unescape("&#a;"));
    }

    @Test
    // Tests unescape of an entity whose character value > 0xFFFF (defect detection)
    public void testUnescape_entityOverFFFF_returnsOriginal() {
        // "Afr" maps to 0x1D504 which is > 0xFFFF; current buggy code converts it wrongly
        assertEquals("&Afr;", Entities.unescape("&Afr;"));
    }

    @Test
    // Tests unescape of a mixed string with multiple entities
    public void testUnescape_mixedContent_unescapesProperly() {
        assertEquals("&<test>", Entities.unescape("&amp;&lt;test&gt;"));
    }

    @Test
    // Tests unescape of a numeric hex entity with uppercase X
    public void testUnescape_NumericHexEntityUpperCaseX_returnsChar() {
        assertEquals("&", Entities.unescape("&#X26;"));
    }

    @Test
    // Tests that in base mode, a character only present in extended map is not escaped
    public void testEscape_baseMode_characterOnlyInExtended_returnsSame() {
        // Character ă (U+0103) is only in extended map; base mode should leave it as is (encodable)
        assertEquals("\u0103", Entities.escape("\u0103", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.base));
    }

    // ===================== New test cases for uncovered coverage =====================

    @Test
    // Tests escape with null encoder (edge case) - should not throw NullPointerException
    public void testEscape_nullEncoder_returnsSame() {
        // If encoder is null, the method should still work (likely using default encoding)
        assertEquals("test", Entities.escape("test", null, Entities.EscapeMode.base));
    }

    @Test
    // Tests unescape of a string that starts with ampersand but is malformed
    public void testUnescape_startWithAmpersandOnly_returnsSame() {
        assertEquals("&", Entities.unescape("&"));
    }

    @Test
    // Tests unescape of a numeric entity with value 0 (edge case)
    public void testUnescape_numericEntityZero_returnsNullChar() {
        assertEquals("\0", Entities.unescape("&#0;"));
    }

    @Test
    // Tests that escape with extended mode escapes characters from base map as well
    public void testEscape_extendedMode_characterInBaseMap_returnsEntity() {
        assertEquals("&amp;", Entities.escape("&", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    // Tests escape with empty string in extended mode
    public void testEscape_emptyStringExtended_returnsEmpty() {
        assertEquals("", Entities.escape("", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    // Tests unescape of a numeric decimal entity with leading zeros
    public void testUnescape_numericDecimalWithLeadingZeros_returnsChar() {
        assertEquals("A", Entities.unescape("&#065;"));
    }

    @Test
    // Tests unescape of a numeric hex entity with leading zeros
    public void testUnescape_numericHexWithLeadingZeros_returnsChar() {
        assertEquals("A", Entities.unescape("&#x41;"));
    }

    @Test
    // Tests unescape of multiple consecutive entities
    public void testUnescape_multipleConsecutiveEntities_unescapesAll() {
        assertEquals("&&", Entities.unescape("&amp;&amp;"));
    }

    @Test
    // Tests that escape with base mode handles non-ASCII encodable characters correctly
    public void testEscape_baseMode_nonAsciiEncodable_returnsSame() {
        assertEquals("\u00E9", Entities.escape("\u00E9", StandardCharsets.UTF_8.newEncoder(), Entities.EscapeMode.base));
    }

    @Test
    // Tests unescape of an entity with a semicolon in the middle (malformed)
    public void testUnescape_entityWithExtraSemicolon_returnsOriginal() {
        assertEquals("&amp;;", Entities.unescape("&amp;;"));
    }
}
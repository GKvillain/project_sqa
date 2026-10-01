package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntitiesTest {

    private static java.nio.charset.CharsetEncoder asciiEncoder() {
        return java.nio.charset.Charset.forName("US-ASCII").newEncoder();
    }

    @Test
    public void testIsNamedEntity_knownName_returnsTrue() {
        assertTrue(Entities.isNamedEntity("amp"));
    }

    @Test
    public void testIsNamedEntity_unknownName_returnsFalse() {
        assertFalse(Entities.isNamedEntity("definitelynotarealentity"));
    }

    @Test
    public void testGetCharacterByName_knownName_returnsCharacter() {
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
    }

    @Test
    public void testGetCharacterByName_unknownName_returnsNull() {
        assertNull(Entities.getCharacterByName("definitelynotarealentity"));
    }

    @Test
    public void testEscape_characterInEscapeMap_returnsNamedReference() {
        assertEquals("&amp;", Entities.escape("&", asciiEncoder(), Entities.EscapeMode.xhtml));
    }

    @Test
    public void testEscape_encodableCharacter_keepsCharacter() {
        assertEquals("abc", Entities.escape("abc", asciiEncoder(), Entities.EscapeMode.xhtml));
    }

    @Test
    public void testEscape_nonEncodableCharacter_returnsNumericReference() {
        assertEquals("&#233;", Entities.escape("\u00e9", asciiEncoder(), Entities.EscapeMode.xhtml));
    }

    @Test
    public void testUnescape_stringWithoutAmpersand_returnsSameString() {
        assertEquals("no entities here", Entities.unescape("no entities here"));
    }

    @Test
    public void testUnescape_ampersandWithoutEntity_returnsSameString() {
        assertEquals("foo & bar", Entities.unescape("foo & bar"));
    }

    @Test
    public void testUnescape_namedEntity_returnsCharacter() {
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescape_namedEntityStrict_returnsCharacter() {
        assertEquals("&", Entities.unescape("&amp;", true));
    }

    @Test
    public void testUnescape_namedEntityWithoutSemicolon_nonStrict_returnsCharacter() {
        assertEquals("&", Entities.unescape("&amp"));
    }

    @Test
    public void testUnescape_namedEntityWithoutSemicolon_strict_returnsOriginal() {
        assertEquals("&amp", Entities.unescape("&amp", true));
    }

    @Test
    public void testUnescape_unknownNamedEntity_returnsOriginal() {
        assertEquals("&definitelynotarealentity;", Entities.unescape("&definitelynotarealentity;"));
    }

    @Test
    public void testUnescape_numericDecimalEntity_returnsCharacter() {
        assertEquals("A", Entities.unescape("&#65;"));
    }

    @Test
    public void testUnescape_numericDecimalEntityWithoutSemicolon_nonStrict_returnsCharacter() {
        assertEquals("A", Entities.unescape("&#65"));
    }

    @Test
    public void testUnescape_numericHexEntity_returnsCharacter() {
        assertEquals("J", Entities.unescape("&#x4A;"));
        assertEquals("J", Entities.unescape("&#X4A;"));
    }

    @Test
    public void testUnescape_invalidNumericEntity_returnsOriginal() {
        assertEquals("&#xFFFFFFFF;", Entities.unescape("&#xFFFFFFFF;"));
    }

    @Test
    public void testUnescape_supplementaryCodePoint_returnsSurrogatePair() {
        assertEquals(String.valueOf(Character.toChars(0x10000)), Entities.unescape("&#x10000;"));
        assertEquals(String.valueOf(Character.toChars(0x10FFFF)), Entities.unescape("&#x10FFFF;"));
    }

    @Test
    public void testEscape_nonEncodableCharacter_returnsNumericReferenceWithSemicolon() {
        assertEquals("&#00e9;", Entities.escape("\u00e9", asciiEncoder(), Entities.EscapeMode.base));
    }

    @Test
    public void testEscape_controlCharacter_returnsNumericReference() {
        assertEquals("&#x7F;", Entities.escape("\u007f", asciiEncoder(), Entities.EscapeMode.base));
    }

    @Test
    public void testEscape_apostrophe_returnsNamedReference() {
        assertEquals("&apos;", Entities.escape("'", asciiEncoder(), Entities.EscapeMode.base));
    }

    @Test
    public void testEscape_containsMultipleSpecialChars_returnsMixedReferences() {
        assertEquals("&amp;&lt;&gt;&quot;", Entities.escape("&<>\"", asciiEncoder(), Entities.EscapeMode.base));
    }

    @Test
    public void testUnescape_namedEntityCaseInsensitive_returnsCharacter() {
        assertEquals("&", Entities.unescape("&AMP;"));
    }

    @Test
    public void testUnescape_multipleEntities_returnsDecodedString() {
        assertEquals("&<>\"", Entities.unescape("&amp;&lt;&gt;&quot;"));
    }

    @Test
    public void testUnescape_numericEntityWithHighValue_returnsUnicodeCharacter() {
        assertEquals("\u20ac", Entities.unescape("&#8364;"));
    }

    @Test
    public void testUnescape_specialNamedEntity_returnsCharacter() {
        assertEquals("©", Entities.unescape("&copy;"));
    }

    @Test
    public void testUnescape_invalidNamedEntityWithoutSemicolon_returnsOriginal() {
        assertEquals("&invalidentity", Entities.unescape("&invalidentity"));
    }

    @Test
    public void testUnescape_plainTextWithNumericEntityAtStart_returnsDecoded() {
        assertEquals("A text", Entities.unescape("&#65; text"));
    }

    @Test
    public void testUnescape_malformedEntity_returnsOriginal() {
        assertEquals("&#;", Entities.unescape("&#;"));
    }

    @Test
    public void testEscape_modeBase_usesOnlyNumericReferences() {
        assertEquals("&#233;", Entities.escape("\u00e9", asciiEncoder(), Entities.EscapeMode.base));
    }

    @Test
    public void testEscape_modeExtended_usesFullNamedReferences() {
        assertEquals("&euro;", Entities.escape("\u20ac", asciiEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    public void testUnescape_entityWithNumberAtEnd_returnsOriginal() {
        assertEquals("&amp123", Entities.unescape("&amp123"));
    }

    @Test
    public void testUnescape_invalidHexEntity_returnsOriginal() {
        assertEquals("&#xZZ;", Entities.unescape("&#xZZ;"));
    }

    @Test
    public void testUnescape_entityWithTrailingText_returnsDecoded() {
        assertEquals("hello & world", Entities.unescape("hello &amp; world"));
    }
}
package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EntitiesTest {

    // Tests escaping with standard base mode in UTF-8
    @Test
    public void testEscape_baseModeUtf8_escapesBasicEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String escaped = Entities.escape("Hello & < > \" '", encoder, Entities.EscapeMode.base);
        assertEquals("Hello &amp; &lt; &gt; &quot; '", escaped);
    }

    // Tests escaping with extended mode
    @Test
    public void testEscape_extendedMode_escapesExtendedEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String escaped = Entities.escape("\u00A0 \u00A9 \u00C6", encoder, Entities.EscapeMode.extended);
        assertEquals("&nbsp; &copy; &aelig;", escaped);
    }

    // Tests escaping characters that cannot be encoded by the charset
    @Test
    public void testEscape_unencodableCharacters_escapesToNumericEntity() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String escaped = Entities.escape("Hello \u0102 \u00C0", encoder, Entities.EscapeMode.base);
        assertEquals("Hello &#258; &agrave;", escaped);
    }

    // Tests escaping via Document.OutputSettings helper
    @Test
    public void testEscape_withOutputSettings_escapesCorrectly() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);
        String escaped = Entities.escape("& < > \u00A0", settings);
        assertEquals("&amp; &lt; &gt; &nbsp;", escaped);
    }

    // Tests string with no ampersand returns early in unescape
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "Hello World, no entities here!";
        String unescaped = Entities.unescape(input);
        assertEquals(input, unescaped);
    }

    // Tests unescaping empty string
    @Test
    public void testUnescape_emptyString_returnsEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    // Tests unescaping standard named entities in lower case
    @Test
    public void testUnescape_namedEntitiesLowerCase_unescapesCorrectly() {
        String input = "&amp; &lt; &gt; &quot; &nbsp; &copy;";
        String expected = "& < > \" \u00A0 \u00A9";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping uppercase and mixed case named entities
    @Test
    public void testUnescape_namedEntitiesUppercase_unescapesCorrectly() {
        String input = "&AMP; &LT; &GT; &QUOT; &COPY; &AElig;";
        String expected = "& < > \" \u00A9 \u00C6";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        String input = "&#38; &#60; &#62; &#34; &#160;";
        String expected = "& < > \" \u00A0";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping hex numeric entities with lower 'x' and upper 'X'
    @Test
    public void testUnescape_hexNumericEntities_unescapesCorrectly() {
        String input = "&#x26; &#X3C; &#x3e; &#x22; &#xa0;";
        String expected = "& < > \" \u00A0";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping entities without trailing semicolons
    @Test
    public void testUnescape_entitiesWithoutSemicolon_unescapesCorrectly() {
        String input = "&amp &lt &gt &quot &#38 &#x26";
        String expected = "& < > \" & &";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests unescaping invalid / unknown named entity leaves it as-is
    @Test
    public void testUnescape_unknownEntityName_replacesWithOriginal() {
        String input = "&nonexistententity; &foobar;";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping invalid numeric format within entity syntax
    @Test
    public void testUnescape_invalidNumericFormat_replacesWithOriginal() {
        String input = "&#xZZ; &#999999999999999999999999999999;";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping mixed text containing valid, invalid, and numeric entities
    @Test
    public void testUnescape_mixedTextAndEntities_unescapesCorrectly() {
        String input = "Text with &amp; some &#60;tags&#62; and &unknown; plus &#x201C;quotes&#x201D;.";
        String expected = "Text with & some <tags> and &unknown; plus \u201Cquotes\u201D.";
        assertEquals(expected, Entities.unescape(input));
    }

    // Tests EscapeMode enum values and valueOf
    @Test
    public void testEscapeMode_enumValues_areAccessible() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertTrue(modes.length >= 2);
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    @Test
    public void testIsNamedEntity_validAndInvalidNames() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("nonExistentEntityName"));
    }

    @Test
    public void testIsBaseNamedEntity_validAndInvalidNames() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("copy"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntityName"));
    }

    @Test
    public void testGetCharacterByName_returnsExpectedCharacterOrNull() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonExistentEntityName"));
    }

    @Test
    public void testUnescape_strictModeTrueAndFalse() {
        String input = "&amp; &lt &gt; &#60; &unknown;";
        String expectedStrict = "& < &unknown;";
        String unescapedStrict = Entities.unescape("&amp; &#60; &unknown;", true);
        assertEquals(expectedStrict, unescapedStrict);

        String unescapedLoose = Entities.unescape(input, false);
        assertEquals("& < > < &unknown;", unescapedLoose);
    }

    @Test
    public void testUnescape_malformedAmpersandSequences() {
        String input = "& &; &#; &#x; &amp";
        String expected = "& &; &#; &#x; &";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testUnescape_supplementaryCharacters() {
        String input = "&#x1F600; &#128512;";
        String expected = "\uD83D\uDE00 \uD83D\uDE00";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test
    public void testEscape_noSpecialCharacters_returnsUnchanged() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "Simple text without special chars 12345.";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals(input, escaped);
    }

    @Test
    public void testEscape_escapeModeMapsAccessible() {
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        assertTrue(Entities.EscapeMode.base.getMap().size() > 0);
        assertTrue(Entities.EscapeMode.extended.getMap().size() > Entities.EscapeMode.base.getMap().size());
    }

    @Test
    public void testEntitiesConstructorInstantiable() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }
}
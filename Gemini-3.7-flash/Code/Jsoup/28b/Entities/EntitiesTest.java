package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests known named entity check
    @Test
    public void testIsNamedEntity_knownEntity_returnsTrue() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
    }

    // Tests unknown named entity check
    @Test
    public void testIsNamedEntity_unknownEntity_returnsFalse() {
        assertFalse(Entities.isNamedEntity("nonExistentEntity123"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Tests getting character value for a valid entity name
    @Test
    public void testGetCharacterByName_validName_returnsCharacter() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
    }

    // Tests getting character value for an invalid entity name
    @Test
    public void testGetCharacterByName_invalidName_returnsNull() {
        assertNull(Entities.getCharacterByName("unknownEntity"));
    }

    // Tests escape with basic characters using xhtml mode
    @Test
    public void testEscape_xhtmlMode_escapesRestrictedEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String input = "Hello <foo & 'bar'> \"world\"";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.xhtml);
        assertEquals("Hello &lt;foo &amp; &apos;bar&gt; &quot;world&quot;", escaped);
    }

    // Tests escape with Document.OutputSettings
    @Test
    public void testEscape_outputSettings_escapesCorrectly() {
        Document.OutputSettings settings = new Document.OutputSettings();
        String input = "<a> & \"b\"";
        String escaped = Entities.escape(input, settings);
        assertEquals("&lt;a&gt; &amp; \"b\"", escaped);
    }

    // Tests escape with characters outside the target charset encoding
    @Test
    public void testEscape_unencodableCharacter_escapesToNumericEntity() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "ü and ☺";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&#252; and &#9786;", escaped);
    }

    // Tests unescape on string without ampersand branch
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "Simple plain text with no entities.";
        String unescaped = Entities.unescape(input);
        assertSame(input, unescaped);
    }

    // Tests unescaping standard named entities
    @Test
    public void testUnescape_namedEntities_returnsUnescapedCharacters() {
        String input = "&lt;div class=&quot;test&quot;&gt;&amp;&apos;&lt;/div&gt;";
        String unescaped = Entities.unescape(input);
        assertEquals("<div class=\"test\">&'</div>", unescaped);
    }

    // Tests unescaping decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_returnsUnescapedCharacters() {
        String input = "&#65;&#66;&#67;";
        String unescaped = Entities.unescape(input);
        assertEquals("ABC", unescaped);
    }

    // Tests unescaping hexadecimal numeric entities (lower and upper case 'x')
    @Test
    public void testUnescape_hexNumericEntities_returnsUnescapedCharacters() {
        String input = "&#x41;&#X42;&#x43;";
        String unescaped = Entities.unescape(input);
        assertEquals("ABC", unescaped);
    }

    // Tests non-strict unescape without trailing semicolon
    @Test
    public void testUnescape_nonStrictWithoutSemicolon_unescapesEntity() {
        String input = "&lt &amp &gt";
        String unescaped = Entities.unescape(input, false);
        assertEquals("< & >", unescaped);
    }

    // Tests strict unescape requiring trailing semicolon
    @Test
    public void testUnescape_strictWithoutSemicolon_doesNotUnescape() {
        String input = "&lt test &amp";
        String unescaped = Entities.unescape(input, true);
        assertEquals("&lt test &amp", unescaped);
    }

    // Tests unescape with unknown named entity
    @Test
    public void testUnescape_unknownEntity_preservesOriginalString() {
        String input = "&unknownEntity; and &notAnEntity";
        String unescaped = Entities.unescape(input);
        assertEquals("&unknownEntity; and &notAnEntity", unescaped);
    }

    // Tests unescape with invalid number format in numeric entity
    @Test
    public void testUnescape_invalidNumberFormat_preservesOriginalString() {
        String input = "&#xZZZ; and &#9999999999999999999999999;";
        String unescaped = Entities.unescape(input);
        assertEquals("&#xZZZ; and &#9999999999999999999999999;", unescaped);
    }

    // Tests EscapeMode map retrieval
    @Test
    public void testEscapeMode_getMap_returnsPopulatedMap() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('<'));
    }

    // Tests isBaseNamedEntity check
    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("nbsp"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentBaseEntity"));
    }

    // Tests unescape with supplementary characters (code points > 0xFFFF)
    @Test
    public void testUnescape_supplementaryCodePoint_returnsSurrogatePair() {
        String input = "&#x1F600; and &#128512;";
        String unescaped = Entities.unescape(input);
        String expectedEmoji = new String(Character.toChars(0x1F600));
        assertEquals(expectedEmoji + " and " + expectedEmoji, unescaped);
    }

    // Tests unescape with empty numeric entity syntax
    @Test
    public void testUnescape_emptyNumericEntities_preservesOriginalString() {
        String input = "&#; and &#x; and &#X;";
        String unescaped = Entities.unescape(input);
        assertEquals("&#; and &#x; and &#X;", unescaped);
    }

    // Tests escape with base and extended escape modes
    @Test
    public void testEscape_baseAndExtendedModes() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "© and \u00A0 and \u03C0";

        String escapedBase = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&copy; and &nbsp; and &#960;", escapedBase);

        String escapedExtended = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);
        assertEquals("&copy; and &nbsp; and &pi;", escapedExtended);
    }

    // Tests escape with custom OutputSettings charset and escape mode
    @Test
    public void testEscape_customOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.extended);

        String input = "foo © <bar> \u03C0";
        String escaped = Entities.escape(input, settings);
        assertEquals("foo &copy; &lt;bar&gt; &pi;", escaped);
    }
}
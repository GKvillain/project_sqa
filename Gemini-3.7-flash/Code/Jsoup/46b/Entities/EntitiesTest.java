package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests checking if known named entity exists
    @Test
    public void testIsNamedEntity_validAndInvalidNames_returnsCorrectBoolean() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertFalse(Entities.isNamedEntity("nonExistentEntityXYZ123"));
    }

    // Tests checking if known base named entity exists
    @Test
    public void testIsBaseNamedEntity_baseAndNonBaseEntities_returnsCorrectBoolean() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntityXYZ123"));
    }

    // Tests getting character value by entity name
    @Test
    public void testGetCharacterByName_validAndInvalidNames_returnsExpectedCharacterOrNull() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertNull(Entities.getCharacterByName("nonExistentEntityXYZ123"));
    }

    // Tests basic unescaping of string containing entities
    @Test
    public void testUnescape_escapedString_returnsUnescapedCharacters() {
        String escaped = "&lt;div class=&quot;test&quot;&gt;Hello &amp; welcome&lt;/div&gt;";
        String unescaped = Entities.unescape(escaped);
        assertEquals("<div class=\"test\">Hello & welcome</div>", unescaped);
    }

    // Tests unescape with strict flag
    @Test
    public void testUnescape_strictMode_handlesOptionalSemicolon() {
        assertEquals("&", Entities.unescape("&amp", false));
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&", Entities.unescape("&amp;", true));
    }

    // Tests escaping with default UTF-8 output settings
    @Test
    public void testEscape_utfCharset_escapesSpecialHtmlCharsOnly() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("UTF-8"));
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "<foo & bar > \" \u00a0 \u00e9";
        String escaped = Entities.escape(input, settings);

        assertEquals("&lt;foo &amp; bar &gt; \" &nbsp; \u00e9", escaped);
    }

    // Tests escaping with US-ASCII charset where non-ascii chars should be escaped as entities or hex
    @Test
    public void testEscape_asciiCharset_escapesNonAsciiCharacters() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("US-ASCII"));
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "<foo & bar > \" \u00a0 \u00e9 \u4e2d";
        String escaped = Entities.escape(input, settings);

        assertEquals("&lt;foo &amp; bar &gt; \" &nbsp; &eacute; &#x4e2d;", escaped);
    }

    // Tests escaping with fallback charset like ISO-8859-1
    @Test
    public void testEscape_iso8859Charset_handlesSupportedAndUnsupportedChars() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("ISO-8859-1"));
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "\u00e9 \u4e2d";
        String escaped = Entities.escape(input, settings);

        assertEquals("\u00e9 &#x4e2d;", escaped);
    }

    // Tests escaping under XHTML escape mode where &nbsp; is not defined
    @Test
    public void testEscape_xhtmlEscapeMode_preservesNbspOrEncodesAccordingToXhtml() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("UTF-8"));
        settings.escapeMode(Entities.EscapeMode.xhtml);

        String input = "& < > \" \u00a0";
        String escaped = Entities.escape(input, settings);

        assertEquals("&amp; &lt; &gt; \" \u00a0", escaped);
    }

    // Tests escaping in attribute mode where quotes are escaped but lt/gt are not
    @Test
    public void testEscape_inAttributeMode_escapesQuotesAndAmpersand() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("UTF-8"));
        settings.escapeMode(Entities.EscapeMode.base);

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "<tag attr=\"value & 'test'\">", settings, true, false, false);

        assertEquals("<tag attr=&quot;value &amp; 'test'&quot;>", accum.toString());
    }

    // Tests whitespace normalization and leading whitespace stripping
    @Test
    public void testEscape_normaliseAndStripLeadingWhitespace_collapsesWhitespace() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("UTF-8"));

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "   Hello   \n\t  World!   ", settings, false, true, true);

        assertEquals("Hello World! ", accum.toString());
    }

    // Tests escaping supplementary unicode characters (surrogate pairs)
    @Test
    public void testEscape_supplementaryCharacters_encodedCorrectly() {
        Document.OutputSettings settingsUtf = new Document.OutputSettings();
        settingsUtf.charset(Charset.forName("UTF-8"));

        Document.OutputSettings settingsAscii = new Document.OutputSettings();
        settingsAscii.charset(Charset.forName("US-ASCII"));

        String emoji = "\uD83D\uDE00"; // 😀 (U+1F600)

        String escapedUtf = Entities.escape(emoji, settingsUtf);
        assertEquals(emoji, escapedUtf);

        String escapedAscii = Entities.escape(emoji, settingsAscii);
        assertEquals("&#x1f600;", escapedAscii);
    }

    // Tests EscapeMode enum maps
    @Test
    public void testEscapeMode_getMap_returnsPopulatedMaps() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());

        assertEquals("quot", Entities.EscapeMode.xhtml.getMap().get('"'));
        assertEquals("amp", Entities.EscapeMode.base.getMap().get('&'));
    }

    // Tests empty string handling
    @Test
    public void testEscape_emptyString_returnsEmptyString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals("", Entities.escape("", settings));
        assertEquals("", Entities.unescape(""));
    }
}
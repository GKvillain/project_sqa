package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class EntitiesTest {
    private Document.OutputSettings defaultSettings;

    @Before
    public void setUp() {
        defaultSettings = new Document.OutputSettings();
    }

    // Tests lookup for standard named entities in full set
    @Test
    public void testIsNamedEntity_validAndInvalidNames_returnsCorrectBoolean() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("nbsp"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("nonExistentEntity12345"));
    }

    // Tests lookup for named entities in base set
    @Test
    public void testIsBaseNamedEntity_baseAndExtendedNames_distinguishesSets() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("nbsp"));
        assertFalse(Entities.isBaseNamedEntity("nonExistentEntity12345"));
    }

    // Tests retrieving Character mapping by entity name
    @Test
    public void testGetCharacterByName_knownAndUnknownEntities_returnsExpectedCharacter() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf((char) 0xA0), Entities.getCharacterByName("nbsp"));
        assertNull(Entities.getCharacterByName("nonExistentEntity12345"));
    }

    // Tests standard unescaping of HTML entity strings
    @Test
    public void testUnescape_escapedEntities_returnsDecodedString() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<foo & bar>", Entities.unescape("&lt;foo &amp; bar&gt;"));
        assertEquals("\"quotes\"", Entities.unescape("&quot;quotes&quot;"));
        assertEquals("Hello world", Entities.unescape("Hello world"));
    }

    // Tests strict vs non-strict unescaping
    @Test
    public void testUnescape_strictMode_handlesMissingSemicolon() {
        assertEquals("&", Entities.unescape("&amp", false));
        assertEquals("&amp", Entities.unescape("&amp", true));
    }

    // Tests basic HTML escaping with default settings
    @Test
    public void testEscape_standardHtmlChars_escapesHtmlSpecialCharacters() {
        String input = "<a href=\"test.html?a=1&b=2\">link > here</a>";
        String escaped = Entities.escape(input, defaultSettings);
        assertEquals("&lt;a href=\"test.html?a=1&amp;b=2\"&gt;link &gt; here&lt;/a&gt;", escaped);
    }

    // Tests escaping with characters inside attribute value
    @Test
    public void testEscape_inAttributeMode_escapesQuotesAndPreservesAngleBrackets() {
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "<tag \"attr\" & 'val'>", defaultSettings, true, false, false);
        assertEquals("<tag &quot;attr&quot; &amp; 'val'>", accum.toString());
    }

    // Tests non-breaking space handling in different escape modes
    @Test
    public void testEscape_nonBreakingSpace_handlesXhtmlAndHtmlEscapeModes() {
        String nbspStr = "\u00a0";

        Document.OutputSettings xhtmlSettings = new Document.OutputSettings().escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("\u00a0", Entities.escape(nbspStr, xhtmlSettings));

        Document.OutputSettings baseSettings = new Document.OutputSettings().escapeMode(Entities.EscapeMode.base);
        assertEquals("&nbsp;", Entities.escape(nbspStr, baseSettings));

        Document.OutputSettings extendedSettings = new Document.OutputSettings().escapeMode(Entities.EscapeMode.extended);
        assertEquals("&nbsp;", Entities.escape(nbspStr, extendedSettings));
    }

    // Tests whitespace normalization with multiple spaces and newlines
    @Test
    public void testEscape_whitespaceNormalization_collapsesAndStripsWhitespace() {
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "   multiple   spaces  \n\t text   ", defaultSettings, false, true, true);
        assertEquals("multiple spaces text ", accum.toString());

        accum.setLength(0);
        Entities.escape(accum, "   leading space", defaultSettings, false, true, false);
        assertEquals(" leading space", accum.toString());
    }

    // Tests escaping using US-ASCII charset encoding
    @Test
    public void testEscape_asciiCharset_escapesNonAsciiCharacters() {
        Document.OutputSettings asciiSettings = new Document.OutputSettings()
                .charset(Charset.forName("US-ASCII"))
                .escapeMode(Entities.EscapeMode.base);

        String input = "Hello \u00a9 \u00c5 \u03c0";
        String escaped = Entities.escape(input, asciiSettings);
        assertTrue(escaped.contains("&copy;"));
        assertTrue(escaped.contains("&#x3c0;") || escaped.contains("&pi;"));
    }

    // Tests escaping using fallback/other charsets such as ISO-8859-1 and Shift_JIS
    @Test
    public void testEscape_fallbackCharset_handlesEncodingCapabilities() {
        Document.OutputSettings isoSettings = new Document.OutputSettings()
                .charset(Charset.forName("ISO-8859-1"))
                .escapeMode(Entities.EscapeMode.base);

        String input = "Caf\u00e9 \u03c0";
        String escaped = Entities.escape(input, isoSettings);
        assertTrue(escaped.contains("\u00e9"));
        assertTrue(escaped.contains("&#x3c0;"));

        Document.OutputSettings shiftJisSettings = new Document.OutputSettings()
                .charset(Charset.forName("Shift_JIS"))
                .escapeMode(Entities.EscapeMode.base);
        String sjisInput = "\u65e5\u672c\u8a9e \u00a9";
        String sjisEscaped = Entities.escape(sjisInput, shiftJisSettings);
        assertNotNull(sjisEscaped);
    }

    // Tests escaping supplementary characters (surrogate pairs / code points >= 0x10000)
    @Test
    public void testEscape_supplementaryCharacters_encodesOrHexEscapes() {
        String supplementary = new String(Character.toChars(0x1F600)); // Emoji grinning face

        Document.OutputSettings utfSettings = new Document.OutputSettings().charset(Charset.forName("UTF-8"));
        String utfResult = Entities.escape(supplementary, utfSettings);
        assertEquals(supplementary, utfResult);

        Document.OutputSettings asciiSettings = new Document.OutputSettings().charset(Charset.forName("US-ASCII"));
        String asciiResult = Entities.escape(supplementary, asciiSettings);
        assertEquals("&#x1f600;", asciiResult);
    }

    // Tests EscapeMode enum maps
    @Test
    public void testEscapeMode_enumValues_returnNonEmptyMaps() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        assertTrue(Entities.EscapeMode.xhtml.getMap().size() > 0);
        assertTrue(Entities.EscapeMode.base.getMap().size() > 0);
        assertTrue(Entities.EscapeMode.extended.getMap().size() > 0);
    }

    // Tests empty and plain string escaping
    @Test
    public void testEscape_emptyAndPlainString_returnsIdenticalString() {
        assertEquals("", Entities.escape("", defaultSettings));
        assertEquals("hello world", Entities.escape("hello world", defaultSettings));
    }
}
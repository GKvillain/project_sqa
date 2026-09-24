package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests checking if a valid entity name exists in the full entity set
    @Test
    public void testIsNamedEntity_validEntity_returnsTrue() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("nbsp"));
    }

    // Tests checking if an invalid entity name exists
    @Test
    public void testIsNamedEntity_invalidEntity_returnsFalse() {
        assertFalse(Entities.isNamedEntity("notAnEntity123"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Tests checking if an entity is in the base entity set
    @Test
    public void testIsBaseNamedEntity_baseAndNonBaseEntities_returnsCorrectBoolean() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertFalse(Entities.isBaseNamedEntity("notinbaseEntityXYZ"));
    }

    // Tests getting character value for known entity names
    @Test
    public void testGetCharacterByName_validName_returnsCharacter() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
    }

    // Tests getting character value for unknown entity name returns null
    @Test
    public void testGetCharacterByName_invalidName_returnsNull() {
        assertNull(Entities.getCharacterByName("unknownEntity"));
    }

    // Tests basic escaping of HTML special characters outside attribute
    @Test
    public void testEscape_textNodeSpecialCharacters_escapesProperly() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(Charset.forName("UTF-8"));
        out.escapeMode(Entities.EscapeMode.base);

        String result = Entities.escape("Hello & < > \" ' World", out);
        assertEquals("Hello &amp; &lt; &gt; \" ' World", result);
    }

    // Tests escaping of quotes when inside attribute vs outside attribute
    @Test
    public void testEscape_inAttributeQuotes_escapesQuotesOnlyInAttribute() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(Charset.forName("UTF-8"));
        out.escapeMode(Entities.EscapeMode.base);

        StringBuilder accumInAttr = new StringBuilder();
        Entities.escape(accumInAttr, "value with \"quotes\" & < >", out, true, false, false);
        assertEquals("value with &quot;quotes&quot; &amp; < >", accumInAttr.toString());

        StringBuilder accumNotInAttr = new StringBuilder();
        Entities.escape(accumNotInAttr, "value with \"quotes\" & < >", out, false, false, false);
        assertEquals("value with \"quotes\" &amp; &lt; &gt;", accumNotInAttr.toString());
    }

    // Tests escaping non-breaking space with different escape modes
    @Test
    public void testEscape_nonBreakingSpace_escapesAccordingToMode() {
        Document.OutputSettings outBase = new Document.OutputSettings();
        outBase.charset(Charset.forName("UTF-8"));
        outBase.escapeMode(Entities.EscapeMode.base);

        String baseResult = Entities.escape("hello\u00A0world", outBase);
        assertEquals("hello&nbsp;world", baseResult);

        Document.OutputSettings outXhtml = new Document.OutputSettings();
        outXhtml.charset(Charset.forName("UTF-8"));
        outXhtml.escapeMode(Entities.EscapeMode.xhtml);

        String xhtmlResult = Entities.escape("hello\u00A0world", outXhtml);
        assertEquals("hello&#xa0;world", xhtmlResult);
    }

    // Tests whitespace normalisation and leading whitespace stripping
    @Test
    public void testEscape_normaliseAndStripLeadingWhitespace_collapsesAndStripsSpaces() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(Charset.forName("UTF-8"));

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "   multiple   spaces\n\t text   ", out, false, true, true);
        assertEquals("multiple spaces text ", accum.toString());

        StringBuilder accumNoStrip = new StringBuilder();
        Entities.escape(accumNoStrip, "   leading space", out, false, true, false);
        assertEquals(" leading space", accumNoStrip.toString());
    }

    // Tests escaping characters under US-ASCII charset encoding
    @Test
    public void testEscape_asciiCharset_escapesNonAsciiCharacters() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(Charset.forName("US-ASCII"));
        out.escapeMode(Entities.EscapeMode.base);

        String result = Entities.escape("Ascii & \u00E9 \u03C0", out);
        assertEquals("Ascii &amp; &eacute; &#x3c0;", result);
    }

    // Tests escaping supplementary characters (surrogate pairs) with UTF-8 vs US-ASCII
    @Test
    public void testEscape_supplementaryCharacters_handlesCodePoints() {
        String supplementary = new String(Character.toChars(0x1F4A9)); // Pile of Poo emoji

        Document.OutputSettings outUtf = new Document.OutputSettings();
        outUtf.charset(Charset.forName("UTF-8"));
        String utfResult = Entities.escape(supplementary, outUtf);
        assertEquals(supplementary, utfResult);

        Document.OutputSettings outAscii = new Document.OutputSettings();
        outAscii.charset(Charset.forName("US-ASCII"));
        String asciiResult = Entities.escape(supplementary, outAscii);
        assertEquals("&#x1f4a9;", asciiResult);
    }

    // Tests unescaping named and numeric entities in normal mode
    @Test
    public void testUnescape_namedAndNumericEntities_unescapesCorrectly() {
        String input = "&lt;div class=&quot;main&quot;&gt;Hello &amp; &#39;World&#39; &#x20;&nbsp;&lt;/div&gt;";
        String unescaped = Entities.unescape(input);
        assertEquals("<div class=\"main\">Hello & 'World'  \u00A0</div>", unescaped);
    }

    // Tests strict unescaping mode requiring trailing semicolon
    @Test
    public void testUnescape_strictMode_handlesMissingSemicolon() {
        String input = "&amp &amp; &lt";
        assertEquals("& & <", Entities.unescape(input, false));
        assertEquals("&amp & <", Entities.unescape(input, true));
    }

    // Tests EscapeMode enum maps are populated
    @Test
    public void testEscapeMode_getMap_returnsNonNullPopulatedMap() {
        Map<Character, String> xhtmlMap = Entities.EscapeMode.xhtml.getMap();
        assertNotNull(xhtmlMap);
        assertTrue(xhtmlMap.containsKey('<'));
        assertTrue(xhtmlMap.containsKey('>'));
        assertTrue(xhtmlMap.containsKey('&'));
        assertTrue(xhtmlMap.containsKey('"'));

        Map<Character, String> baseMap = Entities.EscapeMode.base.getMap();
        assertNotNull(baseMap);
        assertTrue(baseMap.size() > xhtmlMap.size());

        Map<Character, String> extendedMap = Entities.EscapeMode.extended.getMap();
        assertNotNull(extendedMap);
        assertTrue(extendedMap.size() >= baseMap.size());
    }

    // Tests fallback charset encoding branch
    @Test
    public void testEscape_fallbackCharset_encodesOrEscapesCorrectly() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(Charset.forName("ISO-8859-1"));
        out.escapeMode(Entities.EscapeMode.base);

        String result = Entities.escape("Fran\u00E7ais \u03C0", out);
        assertEquals("Fran\u00E7ais &#x3c0;", result);
    }
}
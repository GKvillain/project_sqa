package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesTest {

    // Tests unescaping named entities containing digits (defect regression)
    @Test
    public void testUnescape_namedEntitiesWithDigits_unescapesCorrectly() {
        assertEquals("½", Entities.unescape("&frac12;"));
        assertEquals("¾", Entities.unescape("&frac34;"));
        assertEquals("¼", Entities.unescape("&frac14;"));
        assertEquals("¹", Entities.unescape("&sup1;"));
        assertEquals("²", Entities.unescape("&sup2;"));
        assertEquals("³", Entities.unescape("&sup3;"));
    }

    // Tests unescaping standard named entities
    @Test
    public void testUnescape_standardNamedEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("©", Entities.unescape("&copy;"));
        assertEquals("—", Entities.unescape("&mdash;"));
    }

    // Tests unescaping decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals(" ", Entities.unescape("&#160;"));
        assertEquals("Hello © World", Entities.unescape("Hello &#169; World"));
    }

    // Tests unescaping hexadecimal numeric entities
    @Test
    public void testUnescape_hexNumericEntities_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&#x26;"));
        assertEquals("&", Entities.unescape("&#X26;"));
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("©", Entities.unescape("&#xa9;"));
    }

    // Tests unescaping entities without trailing semicolon
    @Test
    public void testUnescape_entityWithoutSemicolon_unescapesCorrectly() {
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("A", Entities.unescape("&#x41"));
    }

    // Tests unescaping string without ampersand returns identical string
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "Hello World! No entities here.";
        assertEquals(input, Entities.unescape(input));
    }

    // Tests unescaping unknown named entity remains unchanged
    @Test
    public void testUnescape_unknownNamedEntity_leavesUnchanged() {
        assertEquals("&nonexistententity;", Entities.unescape("&nonexistententity;"));
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
    }

    // Tests unescaping invalid numeric entity format
    @Test
    public void testUnescape_invalidNumericEntity_leavesUnchanged() {
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
    }

    // Tests escaping with xhtml escape mode
    @Test
    public void testEscape_xhtmlMode_escapesRestrictedEntitiesOnly() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String text = "<p class=\"main\">Fish & 'Chips' > 5</p>";
        String escaped = Entities.escape(text, encoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;p class=&quot;main&quot;&gt;Fish &amp; &apos;Chips&apos; &gt; 5&lt;/p&gt;", escaped);
    }

    // Tests escaping with base escape mode
    @Test
    public void testEscape_baseMode_escapesBaseEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String text = "<p>& \" ' ©</p>";
        String escaped = Entities.escape(text, encoder, Entities.EscapeMode.base);
        assertEquals("&lt;p&gt;&amp; &quot; ' &copy;&lt;/p&gt;", escaped);
    }

    // Tests escaping characters that cannot be encoded by the charset encoder
    @Test
    public void testEscape_unencodableCharacters_escapesToNumericEntities() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String text = "Hello © ü World";
        String escaped = Entities.escape(text, encoder, Entities.EscapeMode.xhtml);
        assertEquals("Hello &#169; &#252; World", escaped);
    }

    // Tests escaping with Document.OutputSettings helper
    @Test
    public void testEscape_withOutputSettings_escapesCorrectly() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().charset("UTF-8");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);

        String text = "<Hello & World>";
        String escaped = Entities.escape(text, doc.outputSettings());
        assertEquals("&lt;Hello &amp; World&gt;", escaped);
    }

    // Tests EscapeMode enum getMap and values
    @Test
    public void testEscapeMode_getMap_returnsValidMap() {
        for (Entities.EscapeMode mode : Entities.EscapeMode.values()) {
            Map<Character, String> map = mode.getMap();
            assertNotNull(map);
            assertFalse(map.isEmpty());
        }
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    // Tests escaping with extended escape mode
    @Test
    public void testEscape_extendedMode_escapesExtendedEntities() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String text = "α & β → γ ♥";
        String escaped = Entities.escape(text, encoder, Entities.EscapeMode.extended);
        assertEquals("&alpha; &amp; &beta; &rarr; &gamma; &hearts;", escaped);
    }

    // Tests isNamedEntity helper method
    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertTrue(Entities.isNamedEntity("frac12"));
        assertFalse(Entities.isNamedEntity("notanentity"));
        assertFalse(Entities.isNamedEntity(""));
    }

    // Tests isBaseNamedEntity helper method
    @Test
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("copy"));
        assertFalse(Entities.isBaseNamedEntity("alpha"));
        assertFalse(Entities.isBaseNamedEntity("notanentity"));
    }

    // Tests getCharacterByName helper method
    @Test
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf('©'), Entities.getCharacterByName("copy"));
        assertNull(Entities.getCharacterByName("notanentity"));
    }

    // Tests unescape with strict flag
    @Test
    public void testUnescape_strictMode() {
        assertEquals("&", Entities.unescape("&amp;", true));
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&", Entities.unescape("&amp", false));
        assertEquals("&#65", Entities.unescape("&#65", true));
        assertEquals("A", Entities.unescape("&#65", false));
        assertEquals("A", Entities.unescape("&#65;", true));
        assertEquals("A", Entities.unescape("&#x41;", true));
        assertEquals("&#x41", Entities.unescape("&#x41", true));
        assertEquals("A", Entities.unescape("&#x41", false));
        assertEquals("Hello World", Entities.unescape("Hello World", true));
    }

    // Tests unescape handling of numeric entities out of valid char range or format
    @Test
    public void testUnescape_numericEntityExceptionsAndBoundaries() {
        assertEquals("&#99999999999999999999999999999;", Entities.unescape("&#99999999999999999999999999999;"));
        assertEquals("&#xZZZZ;", Entities.unescape("&#xZZZZ;"));
        assertEquals("&#-1;", Entities.unescape("&#-1;"));
        assertEquals("&#0;", Entities.unescape("&#0;"));
    }

    // Tests escape with plain characters requiring no escaping
    @Test
    public void testEscape_noEscapingNeeded() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String text = "Hello World 123 !?/$%^()_+-=";
        String escaped = Entities.escape(text, encoder, Entities.EscapeMode.base);
        assertEquals(text, escaped);
    }
}
package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntitiesTest {

    private static final String[][] BASIC_ARRAY = {
        {"quot", "34"},
        {"amp", "38"},
        {"lt", "60"},
        {"gt", "62"}
    };

    private static final String[][] APOS_ARRAY = {
        {"apos", "39"}
    };

    // Helper: creates Entities with basic, apos, and ISO-8859-1 arrays
    private Entities createTestEntities() {
        Entities e = new Entities();
        e.addEntities(BASIC_ARRAY);
        e.addEntities(APOS_ARRAY);
        e.addEntities(Entities.ISO8859_1_ARRAY);
        return e;
    }

    // Tests that entityName returns correct name for known value
    @Test
    public void testEntityName_existingValue_returnsName() {
        Entities e = createTestEntities();
        assertEquals("quot", e.entityName(34));
        assertEquals("amp", e.entityName(38));
        assertEquals("nbsp", e.entityName(160));
    }

    // Tests that entityName returns null for unknown value
    @Test
    public void testEntityName_nonExistingValue_returnsNull() {
        Entities e = createTestEntities();
        assertNull(e.entityName(0));
        assertNull(e.entityName(10000));
    }

    // Tests that entityValue returns correct value for known name
    @Test
    public void testEntityValue_existingName_returnsValue() {
        Entities e = createTestEntities();
        assertEquals(34, e.entityValue("quot"));
        assertEquals(38, e.entityValue("amp"));
        assertEquals(160, e.entityValue("nbsp"));
    }

    // Tests that entityValue returns -1 for unknown name
    @Test
    public void testEntityValue_nonExistingName_returnsMinusOne() {
        Entities e = createTestEntities();
        assertEquals(-1, e.entityValue("nonexistent"));
        assertEquals(-1, e.entityValue(""));
    }

    // Tests escape with simple ASCII text unchanged
    @Test
    public void testEscape_simpleText_noChange() {
        Entities e = createTestEntities();
        assertEquals("hello", e.escape("hello"));
        assertEquals("123", e.escape("123"));
        assertEquals("", e.escape(""));
    }

    // Tests escape for characters > 0x7F without entity name (numeric escape)
    @Test
    public void testEscape_charAbove127_withoutEntity_writesNumeric() {
        Entities e = createTestEntities();
        // 0x80 (128) is not an entity in ISO-8859-1
        assertEquals("&#128;", e.escape("\u0080"));
        // 0x100 (256) not entity
        assertEquals("&#256;", e.escape("\u0100"));
    }

    // Tests escape for characters that have a named entity
    @Test
    public void testEscape_entityPresent_writesEntity() {
        Entities e = createTestEntities();
        assertEquals("&amp;", e.escape("&"));
        assertEquals("&lt;", e.escape("<"));
        assertEquals("&gt;", e.escape(">"));
        assertEquals("&apos;", e.escape("'"));
        assertEquals("&nbsp;", e.escape("\u00A0"));
    }

    // Tests escape with mixed content
    @Test
    public void testEscape_mixedContent() {
        Entities e = createTestEntities();
        String input = "a<>&'\u00A0b\u0080c";
        String expected = "a&lt;&gt;&amp;&apos;&nbsp;b&#128;c";
        assertEquals(expected, e.escape(input));
    }

    // Tests unescape with no ampersand returns same string
    @Test
    public void testUnescape_noAmpersand_returnsSame() {
        Entities e = createTestEntities();
        assertEquals("hello", e.unescape("hello"));
        assertEquals("", e.unescape(""));
    }

    // Tests unescape with named entity
    @Test
    public void testUnescape_entityName_unescaped() {
        Entities e = createTestEntities();
        assertEquals("\u00A0", e.unescape("&nbsp;"));
        assertEquals("&", e.unescape("&amp;"));
        assertEquals("<", e.unescape("&lt;"));
    }

    // Tests unescape with numeric decimal entity
    @Test
    public void testUnescape_numericDecimal_unescaped() {
        Entities e = createTestEntities();
        assertEquals("\u00A0", e.unescape("&#160;"));
        assertEquals("A", e.unescape("&#65;"));
        assertEquals("\u0080", e.unescape("&#128;"));
    }

    // Tests unescape with numeric hex entity (both uppercase and lowercase X)
    @Test
    public void testUnescape_numericHex_unescaped() {
        Entities e = createTestEntities();
        assertEquals("\u00A0", e.unescape("&#xA0;"));
        assertEquals("\u00A0", e.unescape("&#xa0;"));
        assertEquals("A", e.unescape("&#x41;"));
        assertEquals("A", e.unescape("&#X41;"));
    }

    // Tests unescape with invalid entity name returns literal
    @Test
    public void testUnescape_invalidEntity_returnsLiteral() {
        Entities e = createTestEntities();
        assertEquals("&foo;", e.unescape("&foo;"));
        assertEquals("&#xyz;", e.unescape("&#xyz;"));
        assertEquals("&#xGH;", e.unescape("&#xGH;"));
    }

    // Tests unescape without semicolon returns literal
    @Test
    public void testUnescape_noSemicolon_returnsLiteral() {
        Entities e = createTestEntities();
        assertEquals("&amp", e.unescape("&amp"));
        assertEquals("&", e.unescape("&"));
    }

    // Tests unescape with nested ampersand (another '&' before ';')
    @Test
    public void testUnescape_nestedAmpersand_returnsLiteralPart() {
        Entities e = createTestEntities();
        // "&a&b;c" : first '&' is literal because there is another '&' before the ';'
        assertEquals("&a&b;c", e.unescape("&a&b;c"));
    }

    // Tests unescape when numeric value > 0xFFFF (should treat as invalid)
    @Test
    public void testUnescape_valueAboveFFFF_returnsLiteral() {
        Entities e = createTestEntities();
        assertEquals("&#100000;", e.unescape("&#100000;"));
        assertEquals("&#x110000;", e.unescape("&#x110000;"));
    }

    // Tests unescape with empty entity content (e.g. "&;")
    @Test
    public void testUnescape_emptyEntityContent_returnsLiteral() {
        Entities e = createTestEntities();
        assertEquals("&;", e.unescape("&;"));
    }

    // Tests static XML entity set (basic + apos)
    @Test
    public void testStaticXML_escapeAndUnescape() {
        String escaped = Entities.XML.escape("&<>'\"");
        assertEquals("&amp;&lt;&gt;&apos;&quot;", escaped);
        assertEquals("&<>'\"", Entities.XML.unescape(escaped));
    }

    // Tests static HTML40 entity name lookup
    @Test
    public void testStaticHTML40_entityName_returnsName() {
        assertEquals("nbsp", Entities.HTML40.entityName(160));
        assertEquals("copy", Entities.HTML40.entityName(169));
        assertEquals("euro", Entities.HTML40.entityName(8364));
    }

    // Tests unescape via Writer (no IOException expected)
    @Test
    public void testUnescape_withWriter_success() throws IOException {
        StringWriter sw = new StringWriter();
        Entities e = createTestEntities();
        e.unescape(sw, "&amp;");
        assertEquals("&", sw.toString());
    }

    // Tests escape via Writer (no IOException expected)
    @Test
    public void testEscape_withWriter_success() throws IOException {
        StringWriter sw = new StringWriter();
        Entities e = createTestEntities();
        e.escape(sw, "&");
        assertEquals("&amp;", sw.toString());
    }
}
package org.apache.commons.lang;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;

public class EntitiesTest {

    // Helper to create an Entities instance with the basic entities (quot, amp, lt, gt)
    private Entities createBasicEntities() {
        Entities entities = new Entities();
        entities.addEntity("quot", 34);
        entities.addEntity("amp", 38);
        entities.addEntity("lt", 60);
        entities.addEntity("gt", 62);
        return entities;
    }

    // Tests basic HTML entity escape
    @Test
    public void testEscape_basicEntities_returnsEscapedString() {
        Entities entities = createBasicEntities();
        assertEquals("&amp;&lt;&gt;&quot;", entities.escape("&<>\""));
    }

    // Tests escape with non-ASCII character (above 0x7F) -> numeric entity
    @Test
    public void testEscape_nonAsciiCharacter_returnsNumericEntity() {
        Entities entities = new Entities();
        assertEquals("&#161;", entities.escape("\u00A1"));
    }

    // Tests escape with ASCII character not in entity map -> unchanged
    @Test
    public void testEscape_asciiNoEntity_returnsSameChar() {
        Entities entities = new Entities();
        assertEquals("a", entities.escape("a"));
    }

    // Tests escape(Writer, String) with basic entities
    @Test
    public void testEscapeWriter_basicEntities_writesEscapedString() throws IOException {
        Entities entities = createBasicEntities();
        StringWriter writer = new StringWriter();
        entities.escape(writer, "&<>\"");
        assertEquals("&amp;&lt;&gt;&quot;", writer.toString());
    }

    // Tests unescape: basic XML entities
    @Test
    public void testUnescape_basicEntities_returnsUnescapedString() {
        Entities entities = createBasicEntities();
        assertEquals("&<>\"", entities.unescape("&amp;&lt;&gt;&quot;"));
    }

    // Tests unescape: numeric decimal entity
    @Test
    public void testUnescape_numericDecimal_returnsChar() {
        Entities entities = new Entities();
        assertEquals("\u00A1", entities.unescape("&#161;"));
    }

    // Tests unescape: numeric hex entity (lowercase x)
    @Test
    public void testUnescape_numericHexLower_returnsChar() {
        Entities entities = new Entities();
        assertEquals("\u00A1", entities.unescape("&#x00A1;"));
    }

    // Tests unescape: numeric hex entity (uppercase X)
    @Test
    public void testUnescape_numericHexUpper_returnsChar() {
        Entities entities = new Entities();
        assertEquals("\u00A1", entities.unescape("&#X00A1;"));
    }

    // Tests unescape: string without any ampersand -> unchanged
    @Test
    public void testUnescape_noAmpersand_returnsSameString() {
        Entities entities = new Entities();
        assertEquals("hello", entities.unescape("hello"));
    }

    // Tests unescape: ampersand with no semicolon -> unchanged
    @Test
    public void testUnescape_ampersandNoSemicolon_returnsSameString() {
        Entities entities = new Entities();
        assertEquals("&amp", entities.unescape("&amp"));
    }

    // Tests unescape: malformed entity (empty entity name)
    @Test
    public void testUnescape_emptyEntityName_returnsOriginal() {
        Entities entities = new Entities();
        assertEquals("&;", entities.unescape("&;"));
    }

    // Tests unescape: invalid numeric entity (number format exception)
    @Test
    public void testUnescape_invalidNumericEntity_returnsOriginal() {
        Entities entities = new Entities();
        assertEquals("&#abc;", entities.unescape("&#abc;"));
    }

    // Tests unescape: nested ampersand before semicolon (e.g., &amp&amp;)
    @Test
    public void testUnescape_nestedAmpersandBeforeSemicolon_keepsFirstAmpersand() {
        Entities entities = new Entities();
        assertEquals("&amp", entities.unescape("&amp&amp;"));
    }

    // Tests unescape(Writer, String) basic
    @Test
    public void testUnescapeWriter_basicEntities_writesUnescapedString() throws IOException {
        Entities entities = createBasicEntities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&amp;&lt;&gt;&quot;");
        assertEquals("&<>\"", writer.toString());
    }

    // Tests unescape(Writer, String) with no ampersand
    @Test
    public void testUnescapeWriter_noAmpersand_writesSameString() throws IOException {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "hello");
        assertEquals("hello", writer.toString());
    }

    // Tests unescape(Writer, String) with malformed input (no semicolon)
    @Test
    public void testUnescapeWriter_noSemicolon_writesAmpersand() throws IOException {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&amp");
        assertEquals("&amp", writer.toString());
    }

    // Tests unescape(Writer, String) with numeric decimal
    @Test
    public void testUnescapeWriter_numericDecimal_writesChar() throws IOException {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&#161;");
        assertEquals("\u00A1", writer.toString());
    }

    // Tests unescape(Writer, String) with nested ampersand (same as unescape logic)
    @Test
    public void testUnescapeWriter_nestedAmpersand_keepsFirstAmpersand() throws IOException {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&amp&amp;");
        assertEquals("&amp", writer.toString());
    }

    // Tests entityName with known entity (XML basic)
    @Test
    public void testEntityName_knownValue_returnsEntityName() {
        Entities entities = createBasicEntities();
        assertEquals("amp", entities.entityName(38));
    }

    // Tests entityName with unknown value
    @Test
    public void testEntityName_unknownValue_returnsNull() {
        Entities entities = new Entities();
        assertNull(entities.entityName(999));
    }

    // Tests entityValue with known name
    @Test
    public void testEntityValue_knownName_returnsValue() {
        Entities entities = createBasicEntities();
        assertEquals(38, entities.entityValue("amp"));
    }

    // Tests entityValue with unknown name
    @Test
    public void testEntityValue_unknownName_returnsMinusOne() {
        Entities entities = new Entities();
        assertEquals(-1, entities.entityValue("unknown"));
    }

    // Tests addEntity and then escape/unescape round-trip
    @Test
    public void testAddEntity_roundTrip_worksCorrectly() {
        Entities entities = new Entities();
        entities.addEntity("foo", 0xA1);
        String escaped = entities.escape("\u00A1");
        assertEquals("&foo;", escaped);
        assertEquals("\u00A1", entities.unescape(escaped));
    }

    // Tests static XML entity set
    @Test
    public void testXmlEntitySet_hasBasicAndApos() {
        assertEquals("quot", Entities.XML.entityName(34));
        assertEquals("apos", Entities.XML.entityName(39));
        assertEquals(60, Entities.XML.entityValue("lt"));
    }

    // Tests static HTML40 entity set
    @Test
    public void testHtml40EntitySet_hasBasicAndIsoAndHtml40() {
        assertEquals("quot", Entities.HTML40.entityName(34));
        assertEquals("nbsp", Entities.HTML40.entityName(160));
        assertEquals("fnof", Entities.HTML40.entityName(402));
    }

    // Tests unescape with empty string
    @Test
    public void testUnescape_emptyString_returnsEmpty() {
        Entities entities = new Entities();
        assertEquals("", entities.unescape(""));
    }

    // Tests escape with string containing only ASCII < 0x7F and no entity
    @Test
    public void testEscape_asciiOnlyNoEntity_returnsSameString() {
        Entities entities = new Entities();
        assertEquals("abc123", entities.escape("abc123"));
    }
}
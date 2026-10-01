package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EntitiesTest {

    private Entities entities;

    @Before
    public void setUp() {
        entities = new Entities();
        entities.addEntity("foo", 161);
        entities.addEntity("bar", 162);
    }

    // Tests escaping of normal, non-entity characters and ASCII characters
    @Test
    public void testEscape_normalAsciiString_returnsUnchanged() {
        assertEquals("Hello World", entities.escape("Hello World"));
    }

    // Tests escaping of defined entity and unicode character > 0x7F
    @Test
    public void testEscape_definedEntityAndUnicode_returnsEscapedString() {
        assertEquals("&foo;&#163;&bar;", entities.escape("\u00A1\u00A3\u00A2"));
    }

    // Tests escaping with Writer
    @Test
    public void testEscapeWriter_definedEntityAndUnicode_writesEscapedString() throws IOException {
        StringWriter writer = new StringWriter();
        entities.escape(writer, "\u00A1\u00A3\u00A2");
        assertEquals("&foo;&#163;&bar;", writer.toString());
    }

    // Tests unescaping a string containing no ampersand
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        assertEquals("plain text", entities.unescape("plain text"));
    }

    // Tests unescaping a string containing no ampersand using Writer
    @Test
    public void testUnescapeWriter_noAmpersand_writesOriginalString() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "plain text");
        assertEquals("plain text", writer.toString());
    }

    // Tests unescaping named entities
    @Test
    public void testUnescape_namedEntities_returnsUnescapedString() {
        assertEquals("\u00A1 and \u00A2", entities.unescape("&foo; and &bar;"));
    }

    // Tests unescaping decimal numeric entities
    @Test
    public void testUnescape_decimalNumericEntities_returnsUnescapedString() {
        assertEquals("A \u00A1", entities.unescape("&#65; &#161;"));
    }

    // Tests unescaping hexadecimal numeric entities in unescape(String)
    @Test
    public void testUnescape_hexNumericEntities_returnsUnescapedString() {
        assertEquals("A B", entities.unescape("&#x41; &#X42;"));
    }

    // Tests unescaping hexadecimal numeric entities in unescape(Writer, String) - Defects4J Lang-62 bug
    @Test
    public void testUnescapeWriter_hexNumericEntities_writesUnescapedString() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&#x41; &#X42;");
        assertEquals("A B", writer.toString());
    }

    // Tests unescaping invalid and malformed entities
    @Test
    public void testUnescape_malformedEntities_preservesMalformedText() {
        assertEquals("&", entities.unescape("&"));
        assertEquals("&;", entities.unescape("&;"));
        assertEquals("&#;", entities.unescape("&#;"));
        assertEquals("&#x;", entities.unescape("&#x;"));
        assertEquals("&unknown;", entities.unescape("&unknown;"));
        assertEquals("&foo&bar;", entities.unescape("&foo&bar;"));
    }

    // Tests unescaping invalid and malformed entities using Writer
    @Test
    public void testUnescapeWriter_malformedEntities_preservesMalformedText() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&&;&foo&bar;&unknown;&#;&#x;");
        assertEquals("&&;&foo&bar;&unknown;&#;&#x;", writer.toString());
    }

    // Tests XML preset entity configuration
    @Test
    public void testXmlEntities_presetValues_escapesAndUnescapesCorrectly() {
        assertEquals("&quot;&amp;&lt;&gt;&apos;", Entities.XML.escape("\"&<>'"));
        assertEquals("\"&<>'", Entities.XML.unescape("&quot;&amp;&lt;&gt;&apos;"));
    }

    // Tests HTML32 preset entity configuration
    @Test
    public void testHtml32Entities_presetValues_escapesAndUnescapesCorrectly() {
        assertEquals("&copy;", Entities.HTML32.escape("\u00A9"));
        assertEquals("\u00A9", Entities.HTML32.unescape("&copy;"));
    }

    // Tests HTML40 preset entity configuration
    @Test
    public void testHtml40Entities_presetValues_escapesAndUnescapesCorrectly() {
        assertEquals("&euro;", Entities.HTML40.escape("\u20AC"));
        assertEquals("\u20AC", Entities.HTML40.unescape("&euro;"));
    }

    // Tests PrimitiveEntityMap lookup and value resolution
    @Test
    public void testPrimitiveEntityMap_addAndLookup_returnsExpectedValues() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("alpha", 1);
        assertEquals("alpha", map.name(1));
        assertEquals(1, map.value("alpha"));
        assertNull(map.name(2));
        assertEquals(-1, map.value("beta"));
    }

    // Tests HashEntityMap and TreeEntityMap implementations
    @Test
    public void testHashAndTreeEntityMap_addAndLookup_returnsExpectedValues() {
        Entities.HashEntityMap hashMap = new Entities.HashEntityMap();
        hashMap.add("test", 10);
        assertEquals("test", hashMap.name(10));
        assertEquals(10, hashMap.value("test"));
        assertNull(hashMap.name(20));
        assertEquals(-1, hashMap.value("unknown"));

        Entities.TreeEntityMap treeMap = new Entities.TreeEntityMap();
        treeMap.add("test", 10);
        assertEquals("test", treeMap.name(10));
        assertEquals(10, treeMap.value("test"));
        assertNull(treeMap.name(20));
        assertEquals(-1, treeMap.value("unknown"));
    }

    // Tests ArrayEntityMap and BinaryEntityMap capacity and search
    @Test
    public void testArrayAndBinaryEntityMap_addAndLookup_returnsExpectedValues() {
        Entities.ArrayEntityMap arrayMap = new Entities.ArrayEntityMap(1);
        arrayMap.add("a", 1);
        arrayMap.add("b", 2);
        assertEquals("a", arrayMap.name(1));
        assertEquals("b", arrayMap.name(2));
        assertEquals(1, arrayMap.value("a"));
        assertEquals(2, arrayMap.value("b"));
        assertNull(arrayMap.name(3));
        assertEquals(-1, arrayMap.value("c"));

        Entities.BinaryEntityMap binaryMap = new Entities.BinaryEntityMap(1);
        binaryMap.add("b", 2);
        binaryMap.add("a", 1);
        binaryMap.add("c", 3);
        binaryMap.add("b", 2); // Duplicate add
        assertEquals("a", binaryMap.name(1));
        assertEquals("b", binaryMap.name(2));
        assertEquals("c", binaryMap.name(3));
        assertNull(binaryMap.name(4));
    }
}
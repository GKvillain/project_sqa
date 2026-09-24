package org.apache.commons.lang;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class EntitiesTest {

    private Entities entities;

    @Before
    public void setUp() {
        entities = new Entities();
    }

    // Tests escaping of basic predefined XML entities
    @Test
    public void testEscape_xmlPredefinedEntities_escapesCorrectly() {
        assertEquals("&quot;&amp;&lt;&gt;", Entities.XML.escape("\"&<>"));
    }

    // Tests escaping of apostrophe in XML
    @Test
    public void testEscape_xmlApos_escapesCorrectly() {
        assertEquals("&apos;", Entities.XML.escape("'"));
    }

    // Tests escaping characters above 0x7F without mapped name
    @Test
    public void testEscape_unicodeCharacter_escapesToNumericEntity() {
        assertEquals("&#256;", Entities.XML.escape("\u0100"));
    }

    // Tests escaping to a Writer
    @Test
    public void testEscape_writer_writesEscapedString() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.escape(writer, "<b>\"&'test\u0100\"</b>");
        assertEquals("&lt;b&gt;&quot;&amp;'test&#256;&quot;&lt;/b&gt;", writer.toString());
    }

    // Tests unescaping normal text without ampersands
    @Test
    public void testUnescape_noEntities_returnsOriginalString() {
        assertEquals("plain text", entities.unescape("plain text"));
    }

    // Tests unescaping to a Writer with no entities
    @Test
    public void testUnescape_writerNoEntities_writesOriginalString() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "plain text");
        assertEquals("plain text", writer.toString());
    }

    // Tests unescaping named entities via XML and HTML40
    @Test
    public void testUnescape_namedEntities_unescapesCorrectly() {
        assertEquals("\"&<>", Entities.XML.unescape("&quot;&amp;&lt;&gt;"));
        assertEquals("À", Entities.HTML40.unescape("&Agrave;"));
    }

    // Tests unescaping decimal numeric entities in String
    @Test
    public void testUnescape_decimalNumericEntities_unescapesCorrectly() {
        assertEquals("A", entities.unescape("&#65;"));
    }

    // Tests unescaping decimal numeric entities to Writer
    @Test
    public void testUnescape_writerDecimalNumericEntities_unescapesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&#65;");
        assertEquals("A", writer.toString());
    }

    // Tests unescaping lowercase hexadecimal numeric entities to Writer (Defects4J Lang-62b bug)
    @Test
    public void testUnescape_writerHexNumericEntityLower_unescapesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&#x41;");
        assertEquals("A", writer.toString());
    }

    // Tests unescaping uppercase hexadecimal numeric entities to Writer (Defects4J Lang-62b bug)
    @Test
    public void testUnescape_writerHexNumericEntityUpper_unescapesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&#X42;");
        assertEquals("B", writer.toString());
    }

    // Tests unescaping hex numeric entities in String
    @Test
    public void testUnescape_hexNumericEntitiesInString_unescapesCorrectly() {
        assertEquals("AB", entities.unescape("&#x41;&#X42;"));
    }

    // Tests unescaping malformed or incomplete entity references
    @Test
    public void testUnescape_malformedEntities_keepsUnresolvedParts() {
        assertEquals("&", entities.unescape("&"));
        assertEquals("&;", entities.unescape("&;"));
        assertEquals("&#;", entities.unescape("&#;"));
        assertEquals("&#xyz;", entities.unescape("&#xyz;"));
        assertEquals("&unknown;", entities.unescape("&unknown;"));
        assertEquals("&a&amp;", Entities.XML.unescape("&a&amp;"));
    }

    // Tests unescaping malformed entities to Writer
    @Test
    public void testUnescape_writerMalformedEntities_keepsUnresolvedParts() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&&;&unknown;&#;&#xyz;&a&amp;");
        assertEquals("&&;&unknown;&#;&#xyz;&a&amp;", writer.toString());
    }

    // Tests custom entity registration and lookup
    @Test
    public void testAddEntity_customEntity_resolvesCorrectly() {
        entities.addEntity("custom", 1234);
        assertEquals(1234, entities.entityValue("custom"));
        assertEquals("custom", entities.entityName(1234));
        assertEquals("&custom;", entities.escape("\u04D2"));
        assertEquals("\u04D2", entities.unescape("&custom;"));
    }

    // Tests ArrayEntityMap implementation and capacity growth
    @Test
    public void testArrayEntityMap_addAndLookup() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
        map.add("a", 1);
        map.add("b", 2);
        map.add("c", 3);
        assertEquals("a", map.name(1));
        assertEquals("b", map.name(2));
        assertEquals("c", map.name(3));
        assertNull(map.name(4));
        assertEquals(1, map.value("a"));
        assertEquals(2, map.value("b"));
        assertEquals(3, map.value("c"));
        assertEquals(-1, map.value("d"));
    }

    // Tests BinaryEntityMap implementation with binary search
    @Test
    public void testBinaryEntityMap_addAndLookup() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap(2);
        map.add("c", 3);
        map.add("a", 1);
        map.add("b", 2);
        map.add("dup", 2); // Duplicate value should not be inserted
        assertEquals("a", map.name(1));
        assertEquals("b", map.name(2));
        assertEquals("c", map.name(3));
        assertNull(map.name(99));
    }

    // Tests HashEntityMap and TreeEntityMap implementations
    @Test
    public void testHashAndTreeEntityMap_operations() {
        Entities.HashEntityMap hashMap = new Entities.HashEntityMap();
        hashMap.add("foo", 100);
        assertEquals("foo", hashMap.name(100));
        assertEquals(100, hashMap.value("foo"));
        assertEquals(-1, hashMap.value("bar"));
        assertNull(hashMap.name(999));

        Entities.TreeEntityMap treeMap = new Entities.TreeEntityMap();
        treeMap.add("foo", 100);
        assertEquals("foo", treeMap.name(100));
        assertEquals(100, treeMap.value("foo"));
        assertEquals(-1, treeMap.value("bar"));
        assertNull(treeMap.name(999));
    }

    // Tests PrimitiveEntityMap and LookupEntityMap boundaries
    @Test
    public void testLookupEntityMap_belowAndAboveTableSize() {
        Entities.LookupEntityMap lookupMap = new Entities.LookupEntityMap();
        lookupMap.add("low", 65);
        lookupMap.add("high", 300);

        assertEquals("low", lookupMap.name(65));
        assertEquals("high", lookupMap.name(300));
        assertNull(lookupMap.name(66));
        assertNull(lookupMap.name(301));
        assertEquals(65, lookupMap.value("low"));
        assertEquals(300, lookupMap.value("high"));
        assertEquals(-1, lookupMap.value("unknown"));
    }

    // Tests HTML32 predefined entities configuration
    @Test
    public void testHTML32Entities() {
        assertEquals("&copy;", Entities.HTML32.escape("\u00A9"));
        assertEquals("\u00A9", Entities.HTML32.unescape("&copy;"));
    }
}
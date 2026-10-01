package org.apache.commons.lang;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EntitiesTest {

    private Entities entities;

    @Before
    public void setUp() {
        entities = new Entities();
    }

    // Tests adding single entity and lookup by name and value
    @Test
    public void testAddEntity_validNameAndValue_correctLookup() {
        entities.addEntity("foo", 100);
        assertEquals("foo", entities.entityName(100));
        assertEquals(100, entities.entityValue("foo"));
        assertNull(entities.entityName(999));
        assertEquals(-1, entities.entityValue("bar"));
    }

    // Tests adding entity array
    @Test
    public void testAddEntities_validArray_correctLookup() {
        String[][] array = {{"foo", "101"}, {"bar", "102"}};
        entities.addEntities(array);
        assertEquals("foo", entities.entityName(101));
        assertEquals("bar", entities.entityName(102));
        assertEquals(101, entities.entityValue("foo"));
        assertEquals(102, entities.entityValue("bar"));
    }

    // Tests XML preset entities escaping and unescaping
    @Test
    public void testEscapeAndUnescape_xmlPreset_success() {
        String original = "<foo & bar \"quote\" 'apos' >";
        String expectedEscaped = "&lt;foo &amp; bar &quot;quote&quot; &apos;apos&apos; &gt;";
        assertEquals(expectedEscaped, Entities.XML.escape(original));
        assertEquals(original, Entities.XML.unescape(expectedEscaped));
    }

    // Tests HTML32 preset entities
    @Test
    public void testEscapeAndUnescape_html32Preset_success() {
        String original = "\u00A0\u00A9\u00C0";
        String expectedEscaped = "&nbsp;&copy;&Agrave;";
        assertEquals(expectedEscaped, Entities.HTML32.escape(original));
        assertEquals(original, Entities.HTML32.unescape(expectedEscaped));
    }

    // Tests HTML40 preset entities with Greek characters and symbols
    @Test
    public void testEscapeAndUnescape_html40Preset_success() {
        String original = "\u0391\u20AC\u2200";
        String expectedEscaped = "&Alpha;&euro;&forall;";
        assertEquals(expectedEscaped, Entities.HTML40.escape(original));
        assertEquals(original, Entities.HTML40.unescape(expectedEscaped));
    }

    // Tests escaping non-entity ASCII and characters greater than 0x7F
    @Test
    public void testEscape_charactersAbove0x7FWithoutNamedEntity_escapedAsNumeric() {
        entities.addEntity("lt", 60);
        String input = "abc < \u0100 \u03A3";
        String expected = "abc &lt; &#256; &#931;";
        assertEquals(expected, entities.escape(input));
    }

    // Tests escape using custom Writer
    @Test
    public void testEscape_withWriter_writesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.XML.escape(writer, "a < b");
        assertEquals("a &lt; b", writer.toString());
    }

    // Tests unescape fast-path when no ampersand is present
    @Test
    public void testUnescape_noAmpersand_returnsOriginalString() {
        String input = "plain text without entities";
        assertEquals(input, Entities.HTML40.unescape(input));
    }

    // Tests unescape using custom Writer with and without ampersand
    @Test
    public void testUnescape_withWriter_writesCorrectly() throws IOException {
        StringWriter writer1 = new StringWriter();
        Entities.XML.unescape(writer1, "plain text");
        assertEquals("plain text", writer1.toString());

        StringWriter writer2 = new StringWriter();
        Entities.XML.unescape(writer2, "&lt;foo&gt;");
        assertEquals("<foo>", writer2.toString());
    }

    // Tests unescaping decimal and hexadecimal numeric entities
    @Test
    public void testUnescape_numericEntities_returnsDecodedCharacters() {
        assertEquals("\u00A0", Entities.HTML40.unescape("&#160;"));
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xa0;"));
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xA0;"));
        assertEquals("\u0391", Entities.HTML40.unescape("&#913;"));
        assertEquals("\u0391", Entities.HTML40.unescape("&#x391;"));
    }

    // Tests unescaping malformed or incomplete entity patterns
    @Test
    public void testUnescape_malformedEntities_preservesText() {
        assertEquals("&", Entities.HTML40.unescape("&"));
        assertEquals("&abc", Entities.HTML40.unescape("&abc"));
        assertEquals("&;", Entities.HTML40.unescape("&;"));
        assertEquals("&#;", Entities.HTML40.unescape("&#;"));
        assertEquals("&#x;", Entities.HTML40.unescape("&#x;"));
        assertEquals("&#xZZ;", Entities.HTML40.unescape("&#xZZ;"));
        assertEquals("&#notNumber;", Entities.HTML40.unescape("&#notNumber;"));
        assertEquals("&unknownEntity;", Entities.HTML40.unescape("&unknownEntity;"));
        assertEquals("&amp&lt;", Entities.HTML40.unescape("&amp&lt;"));
        assertEquals("&#70000;", Entities.HTML40.unescape("&#70000;"));
    }

    // Tests PrimitiveEntityMap implementation
    @Test
    public void testPrimitiveEntityMap_addAndLookup_returnsExpected() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("alpha", 1);
        map.add("beta", 2);

        assertEquals("alpha", map.name(1));
        assertEquals("beta", map.name(2));
        assertNull(map.name(3));

        assertEquals(1, map.value("alpha"));
        assertEquals(2, map.value("beta"));
        assertEquals(-1, map.value("gamma"));
    }

    // Tests HashEntityMap implementation
    @Test
    public void testHashEntityMap_addAndLookup_returnsExpected() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("test", 123);
        assertEquals("test", map.name(123));
        assertNull(map.name(456));
        assertEquals(123, map.value("test"));
        assertEquals(-1, map.value("unknown"));
    }

    // Tests TreeEntityMap implementation
    @Test
    public void testTreeEntityMap_addAndLookup_returnsExpected() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("test", 123);
        assertEquals("test", map.name(123));
        assertNull(map.name(456));
        assertEquals(123, map.value("test"));
        assertEquals(-1, map.value("unknown"));
    }

    // Tests LookupEntityMap boundary below and above 256
    @Test
    public void testLookupEntityMap_belowAndAbove256_returnsExpected() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("low", 100);
        map.add("high", 300);

        assertEquals("low", map.name(100));
        assertNull(map.name(101));
        assertEquals("high", map.name(300));
        assertNull(map.name(301));
    }

    // Tests ArrayEntityMap grow capacity and lookup
    @Test
    public void testArrayEntityMap_growAndLookup_returnsExpected() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
        map.add("one", 1);
        map.add("two", 2);
        map.add("three", 3); // triggers ensureCapacity

        assertEquals("one", map.name(1));
        assertEquals("two", map.name(2));
        assertEquals("three", map.name(3));
        assertNull(map.name(4));

        assertEquals(1, map.value("one"));
        assertEquals(2, map.value("two"));
        assertEquals(3, map.value("three"));
        assertEquals(-1, map.value("four"));
    }

    // Tests BinaryEntityMap binary search and duplicate insertion
    @Test
    public void testBinaryEntityMap_binarySearchAndDuplicates_returnsExpected() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap(2);
        map.add("c", 30);
        map.add("a", 10);
        map.add("b", 20);
        map.add("duplicate_a", 10); // duplicate insertion branch

        assertEquals("a", map.name(10));
        assertEquals("b", map.name(20));
        assertEquals("c", map.name(30));
        assertNull(map.name(40));
        assertNull(map.name(5));

        assertEquals(10, map.value("a"));
        assertEquals(20, map.value("b"));
        assertEquals(30, map.value("c"));
        assertEquals(-1, map.value("d"));
    }
}
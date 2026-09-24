package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectReaderTest {

    private ObjectMapper _mapper;
    private ObjectReader _reader;

    public static class Point {
        public int x;
        public int y;

        public Point() { }
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _reader = _mapper.reader();
    }

    // Tests reading a single object from a JSON string
    @Test
    public void testReadValue_fromString_returnsObject() throws Exception {
        ObjectReader reader = _reader.forType(Point.class);
        Point p = reader.readValue("{\"x\":1,\"y\":2}");
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
    }

    // Tests reading an object from byte array input
    @Test
    public void testReadValue_fromByteArray_returnsObject() throws Exception {
        byte[] bytes = "{\"x\":10,\"y\":20}".getBytes("UTF-8");
        Point p = _reader.forType(Point.class).readValue(bytes);
        assertNotNull(p);
        assertEquals(10, p.x);
        assertEquals(20, p.y);
    }

    // Tests reading an object from byte array with offset and length
    @Test
    public void testReadValue_fromByteArrayWithOffset_returnsObject() throws Exception {
        byte[] prefix = "padding".getBytes("UTF-8");
        byte[] json = "{\"x\":5,\"y\":6}".getBytes("UTF-8");
        byte[] combined = new byte[prefix.length + json.length];
        System.arraycopy(prefix, 0, combined, 0, prefix.length);
        System.arraycopy(json, 0, combined, prefix.length, json.length);

        Point p = _reader.forType(Point.class).readValue(combined, prefix.length, json.length);
        assertNotNull(p);
        assertEquals(5, p.x);
        assertEquals(6, p.y);
    }

    // Tests reading an object from an InputStream
    @Test
    public void testReadValue_fromInputStream_returnsObject() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"x\":3,\"y\":4}".getBytes("UTF-8"));
        Point p = _reader.forType(Point.class).readValue(in);
        assertNotNull(p);
        assertEquals(3, p.x);
        assertEquals(4, p.y);
    }

    // Tests reading an object from a Reader
    @Test
    public void testReadValue_fromReader_returnsObject() throws Exception {
        StringReader sr = new StringReader("{\"x\":7,\"y\":8}");
        Point p = _reader.forType(Point.class).readValue(sr);
        assertNotNull(p);
        assertEquals(7, p.x);
        assertEquals(8, p.y);
    }

    // Tests updating an existing object instance
    @Test
    public void testReadValue_withValueToUpdate_updatesExistingInstance() throws Exception {
        Point existing = new Point(100, 200);
        ObjectReader updatingReader = _reader.withValueToUpdate(existing);
        Point result = updatingReader.readValue("{\"x\":99}");
        assertSame(existing, result);
        assertEquals(99, existing.x);
        assertEquals(200, existing.y);
    }

    // Tests exception path when trying to update null
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_nullValue_throwsException() {
        _reader.withValueToUpdate(null);
    }

    // Tests exception path when trying to update an array type
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_arrayValue_throwsException() {
        int[] arr = new int[]{1, 2, 3};
        _reader.forType(int[].class).withValueToUpdate(arr);
    }

    // Tests reading tree structure from String and JsonParser
    @Test
    public void testReadTree_fromStringAndParser_returnsJsonNode() throws Exception {
        JsonNode node = _reader.readTree("{\"a\":123,\"b\":\"test\"}");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(123, node.get("a").asInt());
        assertEquals("test", node.get("b").asText());

        JsonParser parser = _reader.getFactory().createParser("{\"k\":true}");
        JsonNode node2 = _reader.readTree(parser);
        assertNotNull(node2);
        assertTrue(node2.get("k").asBoolean());
    }

    // Tests reading sequence of values from byte array (related to Defects4J 57)
    @Test
    public void testReadValues_fromByteArray_returnsMappingIterator() throws Exception {
        byte[] bytes = "{\"x\":1,\"y\":2} {\"x\":3,\"y\":4}".getBytes("UTF-8");
        MappingIterator<Point> it = _reader.forType(Point.class).readValues(bytes);
        assertNotNull(it);
        assertTrue(it.hasNext());
        Point p1 = it.next();
        assertEquals(1, p1.x);
        assertEquals(2, p1.y);
        assertTrue(it.hasNext());
        Point p2 = it.next();
        assertEquals(3, p2.x);
        assertEquals(4, p2.y);
        assertFalse(it.hasNext());
        it.close();
    }

    // Tests reading sequence of values from String
    @Test
    public void testReadValues_fromString_returnsMappingIterator() throws Exception {
        MappingIterator<Integer> it = _reader.forType(Integer.class).readValues("1 2 3");
        assertNotNull(it);
        List<Integer> list = it.readAll();
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    // Tests reading empty input throws JsonMappingException due to end-of-input
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsJsonMappingException() throws Exception {
        _reader.forType(Point.class).readValue("");
    }

    // Tests type configuration with TypeReference
    @Test
    public void testForType_typeReference_deserializesGenericType() throws Exception {
        ObjectReader mapReader = _reader.forType(new TypeReference<Map<String, Integer>>() {});
        Map<String, Integer> map = mapReader.readValue("{\"key1\":10,\"key2\":20}");
        assertNotNull(map);
        assertEquals(Integer.valueOf(10), map.get("key1"));
        assertEquals(Integer.valueOf(20), map.get("key2"));
    }

    // Tests navigation with JSON Pointer via at()
    @Test
    public void testAt_jsonPointer_extractsSubTree() throws Exception {
        ObjectReader subReader = _reader.at("/user/name").forType(String.class);
        String name = subReader.readValue("{\"user\":{\"name\":\"Alice\",\"age\":30}}");
        assertEquals("Alice", name);
    }

    // Tests configuration fluent methods modifying DeserializationFeature and JsonParser.Feature
    @Test
    public void testWithAndWithoutFeatures_configuresCorrectly() {
        ObjectReader r = _reader
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .with(JsonParser.Feature.ALLOW_COMMENTS)
                .without(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(r.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertTrue(r.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(r.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertNotNull(r.getConfig());
        assertNotNull(r.getFactory());
        assertNotNull(r.getTypeFactory());
        assertNotNull(r.version());
    }

    // Tests TreeCodec node creation helper methods
    @Test
    public void testCreateNodesAndTreeToValue() throws Exception {
        JsonNode arrNode = _reader.createArrayNode();
        assertNotNull(arrNode);
        assertTrue(arrNode.isArray());

        JsonNode objNode = _reader.createObjectNode();
        assertNotNull(objNode);
        assertTrue(objNode.isObject());
        ((ObjectNode) objNode).put("x", 42);
        ((ObjectNode) objNode).put("y", 84);

        Point p = _reader.treeToValue(objNode, Point.class);
        assertNotNull(p);
        assertEquals(42, p.x);
        assertEquals(84, p.y);
    }

    // Tests unsupported write operations throw UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteValue_throwsUnsupportedOperationException() throws Exception {
        _reader.writeValue(null, new Point(1, 2));
    }

    // Tests unsupported writeTree throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteTree_throwsUnsupportedOperationException() {
        _reader.writeTree(null, _reader.createObjectNode());
    }

    // Tests ContextAttributes handling
    @Test
    public void testWithAttribute_storesAndRetrievesAttributes() throws Exception {
        ObjectReader r = _reader.withAttribute("testKey", "testVal");
        assertNotNull(r.getAttributes());
        assertEquals("testVal", r.getAttributes().getAttribute("testKey"));

        ObjectReader r2 = r.withoutAttribute("testKey");
        assertNull(r2.getAttributes().getAttribute("testKey"));
    }
}
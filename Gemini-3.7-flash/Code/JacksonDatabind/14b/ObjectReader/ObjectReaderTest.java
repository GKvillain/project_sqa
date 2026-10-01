package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectReaderTest {

    private ObjectMapper _mapper;
    private ObjectReader _reader;

    public static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() { }

        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _reader = _mapper.reader();
    }

    // Tests normal reading from String into target type
    @Test
    public void testReadValue_fromString_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        SimpleBean bean = reader.readValue("{\"x\":10,\"y\":\"abc\"}");
        assertNotNull(bean);
        assertEquals(10, bean.x);
        assertEquals("abc", bean.y);
    }

    // Tests reading from byte array with offset and length boundaries
    @Test
    public void testReadValue_fromByteArrayWithOffsetAndLength_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        byte[] paddedBytes = "PADDING{\"x\":42,\"y\":\"hello\"}TRAILING".getBytes("UTF-8");
        int offset = 7;
        int length = "{\"x\":42,\"y\":\"hello\"}".getBytes("UTF-8").length;

        SimpleBean bean = reader.readValue(paddedBytes, offset, length);
        assertNotNull(bean);
        assertEquals(42, bean.x);
        assertEquals("hello", bean.y);
    }

    // Tests reading from whole byte array
    @Test
    public void testReadValue_fromByteArray_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        byte[] bytes = "{\"x\":5,\"y\":\"bytes\"}".getBytes("UTF-8");
        SimpleBean bean = reader.readValue(bytes);
        assertNotNull(bean);
        assertEquals(5, bean.x);
        assertEquals("bytes", bean.y);
    }

    // Tests reading from InputStream
    @Test
    public void testReadValue_fromInputStream_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        ByteArrayInputStream bais = new ByteArrayInputStream("{\"x\":12,\"y\":\"stream\"}".getBytes("UTF-8"));
        SimpleBean bean = reader.readValue(bais);
        assertNotNull(bean);
        assertEquals(12, bean.x);
        assertEquals("stream", bean.y);
    }

    // Tests reading from Reader
    @Test
    public void testReadValue_fromReader_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        StringReader sr = new StringReader("{\"x\":99,\"y\":\"reader\"}");
        SimpleBean bean = reader.readValue(sr);
        assertNotNull(bean);
        assertEquals(99, bean.x);
        assertEquals("reader", bean.y);
    }

    // Tests reading from JsonNode
    @Test
    public void testReadValue_fromJsonNode_returnsBoundObject() throws Exception {
        ObjectReader reader = _reader.forType(SimpleBean.class);
        JsonNode node = _mapper.readTree("{\"x\":77,\"y\":\"node\"}");
        SimpleBean bean = reader.readValue(node);
        assertNotNull(bean);
        assertEquals(77, bean.x);
        assertEquals("node", bean.y);
    }

    // Tests updating an existing object instance
    @Test
    public void testReadValue_withValueToUpdate_updatesExistingInstance() throws Exception {
        SimpleBean existing = new SimpleBean(1, "orig");
        ObjectReader updatingReader = _mapper.readerForUpdating(existing);
        SimpleBean result = updatingReader.readValue("{\"y\":\"updated\"}");
        assertSame(existing, result);
        assertEquals(1, existing.x);
        assertEquals("updated", existing.y);
    }

    // Tests exception when passing null to withValueToUpdate
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_nullValue_throwsException() {
        _reader.withValueToUpdate(null);
    }

    // Tests exception when passing array to withValueToUpdate
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_arrayType_throwsException() {
        int[] array = new int[]{1, 2, 3};
        _reader.withType(int[].class).withValueToUpdate(array);
    }

    // Tests readTree from String, InputStream, and Reader
    @Test
    public void testReadTree_validJsonSources_returnsJsonNode() throws Exception {
        JsonNode nodeFromString = _reader.readTree("{\"key\":\"val1\"}");
        assertTrue(nodeFromString.isObject());
        assertEquals("val1", nodeFromString.get("key").asText());

        JsonNode nodeFromStream = _reader.readTree(new ByteArrayInputStream("{\"key\":\"val2\"}".getBytes("UTF-8")));
        assertTrue(nodeFromStream.isObject());
        assertEquals("val2", nodeFromStream.get("key").asText());

        JsonNode nodeFromReader = _reader.readTree(new StringReader("{\"key\":\"val3\"}"));
        assertTrue(nodeFromReader.isObject());
        assertEquals("val3", nodeFromReader.get("key").asText());
    }

    // Tests readValues to iterate multiple values from a stream
    @Test
    public void testReadValues_multipleValues_iteratesCorrectly() throws Exception {
        ObjectReader reader = _reader.forType(Integer.class);
        MappingIterator<Integer> it = reader.readValues("1 2 3 4");
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
        it.close();
    }

    // Tests empty content mapping exception
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyContent_throwsJsonMappingException() throws Exception {
        _reader.forType(SimpleBean.class).readValue("");
    }

    // Tests fluent configuration with TypeReference, JavaType, and Class
    @Test
    public void testForType_variousTypeRepresentations_bindsCorrectly() throws Exception {
        JavaType listType = _mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        ObjectReader listReader = _reader.forType(listType);
        List<String> list = listReader.readValue("[\"a\",\"b\"]");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));

        TypeReference<Map<String, Integer>> typeRef = new TypeReference<Map<String, Integer>>() {};
        ObjectReader mapReader = _reader.forType(typeRef);
        Map<String, Integer> map = mapReader.readValue("{\"k\":100}");
        assertEquals(Integer.valueOf(100), map.get("k"));
    }

    // Tests feature toggle methods
    @Test
    public void testWithAndWithoutFeatures_togglesFeatureCorrectly() {
        ObjectReader r = _reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(r.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        r = r.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(r.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        r = r.withFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        r = r.withoutFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
    }

    // Tests ContextAttributes manipulation
    @Test
    public void testContextAttributes_manipulation_retainsAttributes() {
        ObjectReader r = _reader.withAttribute("key1", "val1");
        assertNotNull(r.getAttributes());
        assertEquals("val1", r.getAttributes().getAttribute("key1"));

        r = r.withoutAttribute("key1");
        assertNull(r.getAttributes().getAttribute("key1"));

        Map<Object, Object> attrs = new HashMap<Object, Object>();
        attrs.put("key2", "val2");
        r = r.withAttributes(attrs);
        assertEquals("val2", r.getAttributes().getAttribute("key2"));
    }

    // Tests TreeCodec methods
    @Test
    public void testTreeCodec_nodeCreationAndConversion_createsAndConvertsCorrectly() throws Exception {
        ArrayNode arrayNode = _reader.createArrayNode();
        assertNotNull(arrayNode);
        assertTrue(arrayNode.isArray());

        ObjectNode objectNode = _reader.createObjectNode();
        assertNotNull(objectNode);
        assertTrue(objectNode.isObject());
        objectNode.put("x", 50);
        objectNode.put("y", "tree");

        SimpleBean bean = _reader.treeToValue(objectNode, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(50, bean.x);
        assertEquals("tree", bean.y);
    }

    // Tests unsupported write operations throw exception
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteValue_throwsUnsupportedOperationException() throws Exception {
        _reader.writeValue(null, new SimpleBean());
    }

    // Tests unsupported writeTree operation throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteTree_throwsUnsupportedOperationException() throws Exception {
        _reader.writeTree(null, _reader.createObjectNode());
    }

    // Tests with(Locale), with(TimeZone), and with(JsonNodeFactory)
    @Test
    public void testWithLocaleTimeZoneAndNodeFactory_returnsConfiguredInstances() {
        Locale locale = Locale.GERMANY;
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        JsonNodeFactory nodeFactory = new JsonNodeFactory(true);

        ObjectReader r = _reader.with(locale).with(tz).with(nodeFactory);
        assertNotNull(r);
        assertEquals(locale, r.getConfig().getLocale());
        assertEquals(tz, r.getConfig().getTimeZone());
        assertSame(nodeFactory, r.getConfig().getNodeFactory());
    }

    @Test
    public void testFactoryAndVersion() {
        assertNotNull(_reader.getFactory());
        Version version = _reader.version();
        assertNotNull(version);
        assertFalse(version.isUnknownVersion());
    }

    @Test
    public void testWithRootNameAndWithoutRootName() throws Exception {
        ObjectReader r = _reader.withRootName("root");
        assertNotNull(r);
        r = r.withoutRootName();
        assertNotNull(r);

        r = _reader.withRootName(PropertyName.construct("rootProp"));
        assertNotNull(r);
    }

    @Test
    public void testWithHandlerAndView() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        ObjectReader r = _reader.withHandler(handler);
        assertNotNull(r);

        r = _reader.withView(String.class);
        assertNotNull(r);
    }

    @Test
    public void testWithBase64Variant() {
        ObjectReader r = _reader.with(Base64Variants.MIME);
        assertNotNull(r);
        assertSame(Base64Variants.MIME, r.getConfig().getBase64Variant());
    }

    @Test
    public void testAtJsonPointer() throws Exception {
        ObjectReader r = _reader.at("/a/b");
        assertNotNull(r);

        JsonPointer ptr = JsonPointer.compile("/x");
        r = _reader.at(ptr);
        JsonNode node = r.readTree("{\"x\": {\"val\": 123}}");
        assertNotNull(node);
        assertEquals(123, node.get("val").asInt());
    }

    @Test
    public void testTreeAsTokens() throws Exception {
        JsonNode node = _reader.readTree("{\"x\": 1}");
        JsonParser p = _reader.treeAsTokens(node);
        assertNotNull(p);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testCreateParserMethods() throws Exception {
        JsonParser p1 = _reader.createParser("{\"a\":1}");
        assertNotNull(p1);
        p1.close();

        JsonParser p2 = _reader.createParser("{\"a\":1}".getBytes("UTF-8"));
        assertNotNull(p2);
        p2.close();

        JsonParser p3 = _reader.createParser(new ByteArrayInputStream("{\"a\":1}".getBytes("UTF-8")));
        assertNotNull(p3);
        p3.close();

        JsonParser p4 = _reader.createParser(new StringReader("{\"a\":1}"));
        assertNotNull(p4);
        p4.close();
    }

    @Test
    public void testReadValuesFromDifferentSources() throws Exception {
        ObjectReader reader = _reader.forType(Integer.class);

        byte[] bytes = "10 20".getBytes("UTF-8");
        MappingIterator<Integer> it1 = reader.readValues(bytes);
        assertEquals(Integer.valueOf(10), it1.next());
        assertEquals(Integer.valueOf(20), it1.next());
        it1.close();

        MappingIterator<Integer> it2 = reader.readValues(bytes, 0, bytes.length);
        assertEquals(Integer.valueOf(10), it2.next());
        assertEquals(Integer.valueOf(20), it2.next());
        it2.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        MappingIterator<Integer> it3 = reader.readValues(bais);
        assertEquals(Integer.valueOf(10), it3.next());
        assertEquals(Integer.valueOf(20), it3.next());
        it3.close();

        StringReader sr = new StringReader("30 40");
        MappingIterator<Integer> it4 = reader.readValues(sr);
        assertEquals(Integer.valueOf(30), it4.next());
        assertEquals(Integer.valueOf(40), it4.next());
        it4.close();
    }
}
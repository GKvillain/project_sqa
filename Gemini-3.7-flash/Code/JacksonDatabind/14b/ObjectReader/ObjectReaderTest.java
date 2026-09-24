package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    static class TargetBean {
        public int id;
        public String name;

        public TargetBean() {}

        public TargetBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    // Tests reading simple POJO value from String with forType(Class)
    @Test
    public void testReadValue_fromString_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        TargetBean result = typedReader.readValue("{\"id\":123,\"name\":\"test\"}");

        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("test", result.name);
    }

    // Tests reading value from byte array with offset and length
    @Test
    public void testReadValue_fromByteArrayWithOffset_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        byte[] bytes = "prefix{\"id\":456,\"name\":\"offset\"}suffix".getBytes("UTF-8");
        TargetBean result = typedReader.readValue(bytes, 6, 29);

        assertNotNull(result);
        assertEquals(456, result.id);
        assertEquals("offset", result.name);
    }

    // Tests reading value from InputStream
    @Test
    public void testReadValue_fromInputStream_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        ByteArrayInputStream bais = new ByteArrayInputStream("{\"id\":789,\"name\":\"stream\"}".getBytes("UTF-8"));
        TargetBean result = typedReader.readValue(bais);

        assertNotNull(result);
        assertEquals(789, result.id);
        assertEquals("stream", result.name);
    }

    // Tests reading value from Reader
    @Test
    public void testReadValue_fromReader_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        StringReader sr = new StringReader("{\"id\":999,\"name\":\"reader\"}");
        TargetBean result = typedReader.readValue(sr);

        assertNotNull(result);
        assertEquals(999, result.id);
        assertEquals("reader", result.name);
    }

    // Tests reading value from JsonParser directly
    @Test
    public void testReadValue_fromJsonParser_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        JsonParser jp = mapper.getFactory().createParser("{\"id\":100,\"name\":\"parser\"}");
        TargetBean result = typedReader.readValue(jp);

        assertNotNull(result);
        assertEquals(100, result.id);
        assertEquals("parser", result.name);
    }

    // Tests reading value from JsonNode
    @Test
    public void testReadValue_fromJsonNode_returnsObject() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        JsonNode node = mapper.readTree("{\"id\":200,\"name\":\"node\"}");
        TargetBean result = typedReader.readValue(node);

        assertNotNull(result);
        assertEquals(200, result.id);
        assertEquals("node", result.name);
    }

    // Tests reading value using TypeReference for generic collections
    @Test
    public void testReadValue_withTypeReference_returnsList() throws Exception {
        ObjectReader listReader = reader.forType(new TypeReference<List<String>>() {});
        List<String> list = listReader.readValue("[\"item1\",\"item2\"]");

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("item1", list.get(0));
        assertEquals("item2", list.get(1));
    }

    // Tests reading tree from String and InputStream
    @Test
    public void testReadTree_validJson_returnsJsonNode() throws Exception {
        JsonNode nodeFromString = reader.readTree("{\"key\":\"value\"}");
        assertNotNull(nodeFromString);
        assertEquals("value", nodeFromString.get("key").asText());

        ByteArrayInputStream bais = new ByteArrayInputStream("{\"num\":42}".getBytes("UTF-8"));
        JsonNode nodeFromStream = reader.readTree(bais);
        assertNotNull(nodeFromStream);
        assertEquals(42, nodeFromStream.get("num").asInt());
    }

    // Tests reading empty input throws JsonMappingException due to end-of-input
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsJsonMappingException() throws Exception {
        reader.forType(TargetBean.class).readValue("");
    }

    // Tests reading multiple sequence values via readValues
    @Test
    public void testReadValues_multipleObjects_returnsIterator() throws Exception {
        ObjectReader typedReader = reader.forType(TargetBean.class);
        Iterator<TargetBean> it = typedReader.readValues("{\"id\":1,\"name\":\"a\"} {\"id\":2,\"name\":\"b\"}");

        assertTrue(it.hasNext());
        TargetBean first = it.next();
        assertEquals(1, first.id);
        assertEquals("a", first.name);

        assertTrue(it.hasNext());
        TargetBean second = it.next();
        assertEquals(2, second.id);
        assertEquals("b", second.name);

        assertFalse(it.hasNext());
    }

    // Tests updating an existing object instance
    @Test
    public void testWithValueToUpdate_validObject_updatesInstance() throws Exception {
        TargetBean existing = new TargetBean(1, "original");
        ObjectReader updatingReader = reader.forType(TargetBean.class).withValueToUpdate(existing);
        TargetBean updated = updatingReader.readValue("{\"name\":\"updated\"}");

        assertSame(existing, updated);
        assertEquals(1, existing.id);
        assertEquals("updated", existing.name);
    }

    // Tests setting null value to update throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_nullValue_throwsException() {
        reader.withValueToUpdate(null);
    }

    // Tests setting array type for valueToUpdate throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_arrayType_throwsException() {
        int[] array = new int[]{1, 2, 3};
        reader.forType(int[].class).withValueToUpdate(array);
    }

    // Tests node creation methods (createObjectNode, createArrayNode)
    @Test
    public void testCreateNodes_returnsValidJsonNodes() {
        JsonNode objNode = reader.createObjectNode();
        assertNotNull(objNode);
        assertTrue(objNode.isObject());

        JsonNode arrNode = reader.createArrayNode();
        assertNotNull(arrNode);
        assertTrue(arrNode.isArray());
    }

    // Tests treeToValue conversion
    @Test
    public void testTreeToValue_validTreeNode_convertsSuccessfully() throws Exception {
        JsonNode node = mapper.readTree("{\"id\":50,\"name\":\"converted\"}");
        TargetBean bean = reader.treeToValue(node, TargetBean.class);

        assertNotNull(bean);
        assertEquals(50, bean.id);
        assertEquals("converted", bean.name);
    }

    // Tests writeValue throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteValue_throwsUnsupportedOperationException() throws Exception {
        reader.writeValue(null, new Object());
    }

    // Tests fluent configuration methods and feature checks
    @Test
    public void testFluentConfiguration_andFeatureInspection() {
        ObjectReader configured = reader
                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .with(JsonParser.Feature.ALLOW_COMMENTS)
                .without(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

        assertTrue(configured.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(configured.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertNotNull(configured.getConfig());
        assertNotNull(configured.getFactory());
        assertNotNull(configured.getTypeFactory());
    }

    // Tests version method returns package version
    @Test
    public void testVersion_returnsNonNullVersion() {
        Version v = reader.version();
        assertNotNull(v);
        assertNotEquals(Version.unknownVersion(), v);
    }

    // Tests with(JsonFactory) switching factory
    @Test
    public void testWithJsonFactory_switchesFactoryInstance() {
        JsonFactory customFactory = new JsonFactory();
        ObjectReader readerWithCustomFactory = reader.with(customFactory);

        assertNotNull(readerWithCustomFactory);
        assertSame(customFactory, readerWithCustomFactory.getFactory());
    }
}
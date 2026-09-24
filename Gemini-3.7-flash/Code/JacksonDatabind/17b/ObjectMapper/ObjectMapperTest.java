package com.fasterxml.jackson.databind;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    static class SampleBean {
        public int id;
        public String name;

        public SampleBean() {}
        public SampleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class ContainerWithObject {
        public Object value;

        public ContainerWithObject() {}
        public ContainerWithObject(Object value) {
            this.value = value;
        }
    }

    static class ContainerWithNode {
        public JsonNode node;

        public ContainerWithNode() {}
        public ContainerWithNode(JsonNode node) {
            this.node = node;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests default type resolver builder for TreeNode and sub-types with OBJECT_AND_NON_CONCRETE
    @Test
    public void testUseForType_objectAndNonConcrete_treeNodeExcluded() {
        ObjectMapper.DefaultTypeResolverBuilder builder =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        JavaType objectNodeType = mapper.constructType(ObjectNode.class);
        JavaType jsonNodeType = mapper.constructType(JsonNode.class);
        JavaType treeNodeType = mapper.constructType(TreeNode.class);
        JavaType objectType = mapper.constructType(Object.class);
        JavaType stringType = mapper.constructType(String.class);

        assertTrue(builder.useForType(objectType));
        assertFalse(builder.useForType(stringType));
        assertFalse(builder.useForType(objectNodeType));
        assertFalse(builder.useForType(jsonNodeType));
        assertFalse(builder.useForType(treeNodeType));
    }

    // Tests default type resolver builder with NON_FINAL and TreeNode types
    @Test
    public void testUseForType_nonFinal_treeNodeExcluded() {
        ObjectMapper.DefaultTypeResolverBuilder builder =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        JavaType jsonNodeType = mapper.constructType(JsonNode.class);
        JavaType objectNodeType = mapper.constructType(ObjectNode.class);
        JavaType beanType = mapper.constructType(SampleBean.class);

        assertTrue(builder.useForType(beanType));
        assertFalse(builder.useForType(jsonNodeType));
        assertFalse(builder.useForType(objectNodeType));
    }

    // Tests default type resolver builder with JAVA_LANG_OBJECT
    @Test
    public void testUseForType_javaLangObject_onlyObjectMatches() {
        ObjectMapper.DefaultTypeResolverBuilder builder =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        JavaType objectType = mapper.constructType(Object.class);
        JavaType listType = mapper.constructType(List.class);

        assertTrue(builder.useForType(objectType));
        assertFalse(builder.useForType(listType));
    }

    // Tests default type resolver builder with NON_CONCRETE_AND_ARRAYS
    @Test
    public void testUseForType_nonConcreteAndArrays_resolvesArraysCorrectly() {
        ObjectMapper.DefaultTypeResolverBuilder builder =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        JavaType listArrayType = mapper.constructType(List[].class);
        JavaType stringArrayType = mapper.constructType(String[].class);

        assertTrue(builder.useForType(listArrayType));
        assertFalse(builder.useForType(stringArrayType));
    }

    // Tests serialization and deserialization with default typing for Object property containing JsonNode
    @Test
    public void testDefaultTyping_objectAndNonConcrete_withJsonNode() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        ObjectNode node = mapper.createObjectNode();
        node.put("field", "value");

        ContainerWithNode container = new ContainerWithNode(node);
        String json = mapper.writeValueAsString(container);

        ContainerWithNode result = mapper.readValue(json, ContainerWithNode.class);
        assertNotNull(result);
        assertNotNull(result.node);
        assertEquals("value", result.node.get("field").asText());
    }

    // Tests simple bean serialization and deserialization
    @Test
    public void testReadWriteValue_sampleBean_success() throws Exception {
        SampleBean original = new SampleBean(101, "Alice");
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"id\":101"));
        assertTrue(json.contains("\"name\":\"Alice\""));

        SampleBean result = mapper.readValue(json, SampleBean.class);
        assertEquals(101, result.id);
        assertEquals("Alice", result.name);
    }

    // Tests readValue from byte array and streams
    @Test
    public void testReadValue_fromByteArrayAndStream_success() throws Exception {
        byte[] bytes = "{\"id\":42,\"name\":\"Bob\"}".getBytes("UTF-8");
        SampleBean fromBytes = mapper.readValue(bytes, SampleBean.class);
        assertEquals(42, fromBytes.id);
        assertEquals("Bob", fromBytes.name);

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SampleBean fromStream = mapper.readValue(in, SampleBean.class);
        assertEquals(42, fromStream.id);
        assertEquals("Bob", fromStream.name);
    }

    // Tests readTree with JSON string and Token tree manipulation
    @Test
    public void testReadTree_stringInput_returnsJsonNode() throws Exception {
        String json = "{\"key\":\"val\",\"num\":123}";
        JsonNode root = mapper.readTree(json);
        assertNotNull(root);
        assertTrue(root.isObject());
        assertEquals("val", root.get("key").asText());
        assertEquals(123, root.get("num").asInt());
    }

    // Tests createObjectNode and createArrayNode
    @Test
    public void testCreateNodes_objectAndArray_correctStructure() {
        ObjectNode obj = mapper.createObjectNode();
        ArrayNode arr = mapper.createArrayNode();
        obj.set("items", arr);
        arr.add(1).add(2);

        assertEquals(1, obj.size());
        assertEquals(2, arr.size());
        assertEquals(2, obj.get("items").size());
    }

    // Tests treeToValue and valueToTree conversion
    @Test
    public void testTreeAndValueConversions_successful() throws Exception {
        SampleBean bean = new SampleBean(7, "Seven");
        JsonNode tree = mapper.valueToTree(bean);
        assertTrue(tree.isObject());
        assertEquals(7, tree.get("id").asInt());

        SampleBean roundTrip = mapper.treeToValue(tree, SampleBean.class);
        assertEquals(7, roundTrip.id);
        assertEquals("Seven", roundTrip.name);
    }

    // Tests convertValue with TypeReference and compatible types
    @Test
    public void testConvertValue_mapToBean_success() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 99);
        map.put("name", "Converted");

        SampleBean bean = mapper.convertValue(map, SampleBean.class);
        assertEquals(99, bean.id);
        assertEquals("Converted", bean.name);

        Map<String, Object> backToMap = mapper.convertValue(bean, new TypeReference<Map<String, Object>>() {});
        assertEquals(99, backToMap.get("id"));
        assertEquals("Converted", backToMap.get("name"));
    }

    // Tests convertValue with null input
    @Test
    public void testConvertValue_nullInput_returnsNull() {
        SampleBean bean = mapper.convertValue(null, SampleBean.class);
        assertNull(bean);
    }

    // Tests copy constructor functionality
    @Test
    public void testCopy_createsIndependentInstance() {
        ObjectMapper copy = mapper.copy();
        assertNotNull(copy);
        assertNotSame(mapper, copy);
        assertNotSame(mapper.getSerializationConfig(), copy.getSerializationConfig());
    }

    // Tests feature configuration and isEnabled checks
    @Test
    public void testConfigure_features_togglesCorrectly() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
    }

    // Tests canSerialize and canDeserialize queries
    @Test
    public void testCanSerializeAndDeserialize_validTypes_returnsTrue() {
        assertTrue(mapper.canSerialize(SampleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SampleBean.class, cause));
        assertNull(cause.get());

        JavaType type = mapper.constructType(SampleBean.class);
        assertTrue(mapper.canDeserialize(type));
        assertTrue(mapper.canDeserialize(type, cause));
    }

    // Tests mix-in annotation registration
    @Test
    public void testMixInAnnotations_addAndFind() {
        abstract class MixInSource {}
        assertEquals(0, mapper.mixInCount());

        mapper.addMixIn(SampleBean.class, MixInSource.class);
        assertEquals(1, mapper.mixInCount());
        assertEquals(MixInSource.class, mapper.findMixInClassFor(SampleBean.class));
    }

    // Tests exception path for empty input reading
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsException() throws Exception {
        mapper.readValue("", SampleBean.class);
    }

    // Tests serialization with custom configuration like inclusion and timezone
    @Test
    public void testSerializationSettings_localeAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        mapper.setTimeZone(tz);
        mapper.setLocale(Locale.US);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        assertEquals(tz, mapper.getSerializationConfig().getTimeZone());
        assertEquals(Locale.US, mapper.getSerializationConfig().getLocale());
    }

    // Tests ObjectWriter and ObjectReader builders
    @Test
    public void testWriterAndReader_creation() throws Exception {
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        assertNotNull(writer);
        SampleBean bean = new SampleBean(1, "Test");
        String json = writer.writeValueAsString(bean);
        assertTrue(json.contains("\n") || json.contains(" "));

        ObjectReader reader = mapper.reader(SampleBean.class);
        assertNotNull(reader);
        SampleBean readBean = reader.readValue(json);
        assertEquals(1, readBean.id);
    }
}
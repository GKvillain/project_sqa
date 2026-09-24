package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() { }

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class MixInTarget {
        public String hidden;
        public String visible;
    }

    abstract static class MixInSource {
        @JsonProperty("renamedHidden")
        public String hidden;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests simple serialization and deserialization of a POJO
    @Test
    public void testReadAndWriteValue_simpleBean_success() throws Exception {
        SimpleBean bean = new SimpleBean(1, "test");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));

        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("test", result.name);
    }

    // Tests writeValue and readValue with byte array
    @Test
    public void testReadAndWriteValueAsBytes_simpleBean_success() throws Exception {
        SimpleBean bean = new SimpleBean(42, "bytes");
        byte[] bytes = mapper.writeValueAsBytes(bean);
        assertNotNull(bytes);

        SimpleBean result = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(42, result.id);
        assertEquals("bytes", result.name);

        SimpleBean resultOffset = mapper.readValue(bytes, 0, bytes.length, SimpleBean.class);
        assertEquals(42, resultOffset.id);
    }

    // Tests readValue using TypeReference for generic collections
    @Test
    public void testReadValue_typeReference_success() throws Exception {
        String json = "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]";
        List<SimpleBean> list = mapper.readValue(json, new TypeReference<List<SimpleBean>>() {});
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
        assertEquals("B", list.get(1).name);
    }

    // Tests tree model operations: readTree, writeTree, createObjectNode, createArrayNode
    @Test
    public void testTreeModel_manipulation_success() throws Exception {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("key", "value");
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add(123);
        objectNode.set("arr", arrayNode);

        String json = mapper.writeValueAsString(objectNode);
        JsonNode root = mapper.readTree(json);

        assertTrue(root.isObject());
        assertEquals("value", root.get("key").asText());
        assertTrue(root.get("arr").isArray());
        assertEquals(123, root.get("arr").get(0).asInt());
    }

    // Tests valueToTree and treeToValue conversion
    @Test
    public void testTreeConversion_valueToTreeAndTreeToValue_success() throws Exception {
        SimpleBean bean = new SimpleBean(10, "tree");
        JsonNode node = mapper.valueToTree(bean);
        assertNotNull(node);
        assertEquals(10, node.get("id").asInt());

        SimpleBean converted = mapper.treeToValue(node, SimpleBean.class);
        assertNotNull(converted);
        assertEquals(10, converted.id);
        assertEquals("tree", converted.name);
    }

    // Tests convertValue between compatible structures
    @Test
    public void testConvertValue_mapToBean_success() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 99);
        map.put("name", "mapBean");

        SimpleBean bean = mapper.convertValue(map, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(99, bean.id);
        assertEquals("mapBean", bean.name);

        Map<?, ?> result = mapper.convertValue(bean, Map.class);
        assertEquals(99, result.get("id"));
        assertEquals("mapBean", result.get("name"));
    }

    // Tests readTree with null / empty input
    @Test
    public void testReadTree_emptyOrNullString_returnsNull() throws Exception {
        JsonNode node = mapper.readTree("");
        assertNull(node);

        JsonNode nullNode = mapper.readTree("null");
        assertNotNull(nullNode);
        assertTrue(nullNode.isNull());
    }

    // Tests exception on invalid JSON during readValue
    @Test(expected = JsonMappingException.class)
    public void testReadValue_invalidJsonStructure_throwsException() throws Exception {
        mapper.readValue("{\"id\": \"not-an-int\"}", SimpleBean.class);
    }

    // Tests mapper copy creates an independent copy
    @Test
    public void testCopy_configuredMapper_createsIndependentInstance() {
        ObjectMapper copy = mapper.copy();
        assertNotNull(copy);
        assertNotSame(mapper, copy);
        assertNotNull(copy.getDeserializationConfig());
        assertNotNull(copy.getSerializationConfig());
    }

    // Tests DefaultTypeResolverBuilder with various DefaultTyping rules
    @Test
    public void testDefaultTypeResolverBuilder_useForType_checksApplicability() {
        TypeFactory tf = mapper.getTypeFactory();
        JavaType objectType = tf.constructType(Object.class);
        JavaType stringType = tf.constructType(String.class);
        JavaType listType = tf.constructType(List.class);
        JavaType arrayListType = tf.constructType(ArrayList.class);
        JavaType intArrayType = tf.constructType(int[].class);
        JavaType jsonNodeType = tf.constructType(JsonNode.class);

        // JAVA_LANG_OBJECT
        ObjectMapper.DefaultTypeResolverBuilder b1 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        assertTrue(b1.useForType(objectType));
        assertFalse(b1.useForType(stringType));
        assertFalse(b1.useForType(listType));

        // OBJECT_AND_NON_CONCRETE
        ObjectMapper.DefaultTypeResolverBuilder b2 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        assertTrue(b2.useForType(objectType));
        assertTrue(b2.useForType(listType));
        assertFalse(b2.useForType(arrayListType));
        assertFalse(b2.useForType(stringType));
        assertFalse(b2.useForType(jsonNodeType));

        // NON_CONCRETE_AND_ARRAYS
        ObjectMapper.DefaultTypeResolverBuilder b3 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        assertTrue(b3.useForType(objectType));
        assertTrue(b3.useForType(listType));
        assertTrue(b3.useForType(tf.constructType(List[].class)));
        assertFalse(b3.useForType(jsonNodeType));

        // NON_FINAL
        ObjectMapper.DefaultTypeResolverBuilder b4 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        assertTrue(b4.useForType(objectType));
        assertTrue(b4.useForType(listType));
        assertTrue(b4.useForType(arrayListType));
        assertFalse(b4.useForType(stringType));
        assertFalse(b4.useForType(jsonNodeType));
    }

    // Tests enableDefaultTyping serialization and deserialization
    @Test
    public void testEnableDefaultTyping_polymorphicRoundtrip_success() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        List<Object> list = new ArrayList<Object>();
        list.add(new SimpleBean(5, "polymorphic"));

        String json = mapper.writeValueAsString(list);
        assertTrue(json.contains(SimpleBean.class.getName()));

        List<?> result = mapper.readValue(json, List.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SimpleBean);
        assertEquals(5, ((SimpleBean) result.get(0)).id);
    }

    // Tests mix-in annotations
    @Test
    public void testMixIns_addAndFindMixIn_success() throws Exception {
        mapper.addMixIn(MixInTarget.class, MixInSource.class);
        assertEquals(1, mapper.mixInCount());
        assertEquals(MixInSource.class, mapper.findMixInClassFor(MixInTarget.class));

        MixInTarget target = new MixInTarget();
        target.hidden = "val1";
        target.visible = "val2";

        String json = mapper.writeValueAsString(target);
        assertTrue(json.contains("renamedHidden"));
        assertFalse(json.contains("\"hidden\""));

        mapper.setMixIns(Collections.<Class<?>, Class<?>>emptyMap());
        assertEquals(0, mapper.mixInCount());
    }

    // Tests features configuration for mapper, serializer, and deserializer
    @Test
    public void testConfigure_features_affectsConfiguration() {
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    // Tests canSerialize and canDeserialize query methods
    @Test
    public void testCanSerializeAndDeserialize_validTypes_returnsTrue() {
        assertTrue(mapper.canSerialize(SimpleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        assertNull(cause.get());

        JavaType type = mapper.constructType(SimpleBean.class);
        assertTrue(mapper.canDeserialize(type));
        assertTrue(mapper.canDeserialize(type, cause));
        assertNull(cause.get());
    }

    // Tests stream, reader, and string overloads for readValue and writeValue
    @Test
    public void testReadAndWrite_streamAndReaderVariants_success() throws Exception {
        SimpleBean bean = new SimpleBean(7, "io");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, bean);
        byte[] bytes = out.toByteArray();

        SimpleBean fromStream = mapper.readValue(new ByteArrayInputStream(bytes), SimpleBean.class);
        assertEquals(7, fromStream.id);

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, bean);
        String json = writer.toString();

        SimpleBean fromReader = mapper.readValue(new StringReader(json), SimpleBean.class);
        assertEquals(7, fromReader.id);
    }

    // Tests ObjectReader and ObjectWriter construction and basic execution
    @Test
    public void testReaderAndWriter_scopedExecution_success() throws Exception {
        ObjectWriter writer = mapper.writerFor(SimpleBean.class);
        SimpleBean bean = new SimpleBean(100, "scoped");
        String json = writer.writeValueAsString(bean);

        ObjectReader reader = mapper.readerFor(SimpleBean.class);
        SimpleBean result = reader.readValue(json);
        assertEquals(100, result.id);
        assertEquals("scoped", result.name);
    }

    // Tests problem handler registration and clearing
    @Test
    public void testHandler_addAndClear_success() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();
    }

    // Tests subtype registration
    @Test
    public void testRegisterSubtypes_classesAndNamedTypes_success() {
        mapper.registerSubtypes(SimpleBean.class);
        mapper.registerSubtypes(new NamedType(SimpleBean.class, "simple"));
        assertNotNull(mapper.getSubtypeResolver());
    }
}
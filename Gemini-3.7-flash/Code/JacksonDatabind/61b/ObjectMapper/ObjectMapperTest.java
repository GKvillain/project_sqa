package com.fasterxml.jackson.databind;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    public static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() {}

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class ContainerBean {
        public Object item;

        public ContainerBean() {}

        public ContainerBean(Object item) {
            this.item = item;
        }
    }

    public static class PrimitiveArrayBean {
        public int[] numbers;

        public PrimitiveArrayBean() {}

        public PrimitiveArrayBean(int[] numbers) {
            this.numbers = numbers;
        }
    }

    public static class DateBean {
        public Date date;

        public DateBean() {}

        public DateBean(Date date) {
            this.date = date;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests default constructor and non-null initial configuration
    @Test
    public void testConstructor_default_initializesSuccessfully() {
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getNodeFactory());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getFactory());
    }

    // Tests custom JsonFactory constructor
    @Test
    public void testConstructor_withCustomJsonFactory() {
        JsonFactory jf = new JsonFactory();
        ObjectMapper customMapper = new ObjectMapper(jf);
        assertSame(jf, customMapper.getFactory());
        assertNotNull(customMapper.getSerializationConfig());
    }

    // Tests serialization to JSON string and deserialization from string
    @Test
    public void testReadWriteValue_string_success() throws Exception {
        SimpleBean bean = new SimpleBean(1, "test");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));

        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertEquals(1, result.id);
        assertEquals("test", result.name);
    }

    // Tests serialization and deserialization using byte arrays
    @Test
    public void testReadWriteValue_byteArray_success() throws Exception {
        SimpleBean bean = new SimpleBean(42, "bytes");
        byte[] bytes = mapper.writeValueAsBytes(bean);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        SimpleBean result = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(42, result.id);
        assertEquals("bytes", result.name);
    }

    // Tests deserialization from InputStream and Reader
    @Test
    public void testReadValue_streamAndReader_success() throws Exception {
        String json = "{\"id\":10,\"name\":\"stream\"}";
        InputStream in = new ByteArrayInputStream(json.getBytes("UTF-8"));
        SimpleBean fromStream = mapper.readValue(in, SimpleBean.class);
        assertEquals(10, fromStream.id);

        Reader reader = new StringReader(json);
        SimpleBean fromReader = mapper.readValue(reader, SimpleBean.class);
        assertEquals("stream", fromReader.name);
    }

    // Tests reading with TypeReference for generic collections
    @Test
    public void testReadValue_typeReference_deserializesGenericMap() throws Exception {
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        assertNotNull(result);
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    // Tests tree model operations: createObjectNode, createArrayNode, readTree, writeTree
    @Test
    public void testTreeModel_manipulationAndReadTree_success() throws Exception {
        ObjectNode objNode = mapper.createObjectNode();
        objNode.put("count", 5);
        ArrayNode arrNode = mapper.createArrayNode();
        arrNode.add("element1");
        objNode.set("list", arrNode);

        String json = mapper.writeValueAsString(objNode);
        JsonNode rootNode = mapper.readTree(json);

        assertTrue(rootNode.isObject());
        assertEquals(5, rootNode.get("count").asInt());
        assertTrue(rootNode.get("list").isArray());
        assertEquals("element1", rootNode.get("list").get(0).asText());
    }

    // Tests valueToTree and treeToValue conversion
    @Test
    public void testTreeConversion_valueToTreeAndTreeToValue_success() throws Exception {
        SimpleBean bean = new SimpleBean(99, "treeValue");
        JsonNode node = mapper.valueToTree(bean);
        assertNotNull(node);
        assertEquals(99, node.get("id").asInt());

        SimpleBean fromNode = mapper.treeToValue(node, SimpleBean.class);
        assertEquals(99, fromNode.id);
        assertEquals("treeValue", fromNode.name);
    }

    // Tests valueToTree with null input returns null
    @Test
    public void testValueToTree_nullInput_returnsNull() {
        assertNull(mapper.valueToTree(null));
    }

    // Tests convertValue functionality between compatible types
    @Test
    public void testConvertValue_mapToBean_success() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 7);
        map.put("name", "converted");

        SimpleBean bean = mapper.convertValue(map, SimpleBean.class);
        assertEquals(7, bean.id);
        assertEquals("converted", bean.name);
    }

    // Tests convertValue with null input returns null
    @Test
    public void testConvertValue_nullInput_returnsNull() {
        SimpleBean result = mapper.convertValue(null, SimpleBean.class);
        assertNull(result);
    }

    // Tests copy() creates an independent mapper instance
    @Test
    public void testCopy_createsDistinctInstance() {
        ObjectMapper copyMapper = mapper.copy();
        assertNotNull(copyMapper);
        assertNotSame(mapper, copyMapper);
    }

    // Tests feature configuration on/off
    @Test
    public void testConfigure_featureState_reflectedCorrectly() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    // Tests canSerialize and canDeserialize checks
    @Test
    public void testCanSerializeAndDeserialize_validType_returnsTrue() {
        assertTrue(mapper.canSerialize(SimpleBean.class));
        assertTrue(mapper.canDeserialize(mapper.constructType(SimpleBean.class)));

        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        assertNull(cause.get());
    }

    // Tests DefaultTypeResolverBuilder useForType logic with JavaType for primitives
    @Test
    public void testDefaultTyping_primitiveTypeCheck_doesNotApplyTypingToPrimitives() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.NON_FINAL);

        JavaType intType = mapper.constructType(int.class);
        JavaType longType = mapper.constructType(long.class);
        JavaType booleanType = mapper.constructType(boolean.class);

        assertFalse("Default typing should not apply to primitive int", builder.useForType(intType));
        assertFalse("Default typing should not apply to primitive long", builder.useForType(longType));
        assertFalse("Default typing should not apply to primitive boolean", builder.useForType(booleanType));
    }

    // Tests DefaultTypeResolverBuilder useForType logic with JavaType for primitive arrays
    @Test
    public void testDefaultTyping_primitiveArrayTypeCheck_underNonConcreteAndArrays() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

        JavaType intArrayType = mapper.constructType(int[].class);
        JavaType stringArrayType = mapper.constructType(String[].class);
        JavaType objectArrayType = mapper.constructType(Object[].class);

        assertFalse("Default typing should not apply to primitive int array", builder.useForType(intArrayType));
        assertFalse("Default typing should not apply to concrete String array", builder.useForType(stringArrayType));
        assertTrue("Default typing should apply to Object array", builder.useForType(objectArrayType));
    }

    // Tests enableDefaultTyping serialization and deserialization of polymorphic Object fields
    @Test
    public void testDefaultTyping_roundTripWithObjectField_preservesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        ContainerBean container = new ContainerBean(new SimpleBean(123, "poly"));
        String json = mapper.writeValueAsString(container);

        assertTrue("JSON should contain type info", json.contains(SimpleBean.class.getName()));

        ContainerBean result = mapper.readValue(json, ContainerBean.class);
        assertNotNull(result.item);
        assertTrue(result.item instanceof SimpleBean);
        SimpleBean deserializedItem = (SimpleBean) result.item;
        assertEquals(123, deserializedItem.id);
        assertEquals("poly", deserializedItem.name);
    }

    // Tests enableDefaultTyping with primitive array fields
    @Test
    public void testDefaultTyping_primitiveArrayField_serializesAndDeserializesCorrectly() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        PrimitiveArrayBean bean = new PrimitiveArrayBean(new int[] { 1, 2, 3 });
        String json = mapper.writeValueAsString(bean);

        PrimitiveArrayBean result = mapper.readValue(json, PrimitiveArrayBean.class);
        assertNotNull(result.numbers);
        assertEquals(3, result.numbers.length);
        assertEquals(1, result.numbers[0]);
        assertEquals(2, result.numbers[1]);
        assertEquals(3, result.numbers[2]);
    }

    // Tests disableDefaultTyping disables polymorphic type wrapper inclusion
    @Test
    public void testDisableDefaultTyping_removesTypeInfo() throws Exception {
        mapper.enableDefaultTyping();
        mapper.disableDefaultTyping();

        ContainerBean container = new ContainerBean(new SimpleBean(1, "plain"));
        String json = mapper.writeValueAsString(container);
        assertFalse("JSON should not contain class type info when default typing is disabled",
                json.contains(SimpleBean.class.getName()));
    }

    // Tests enableDefaultTyping with EXTERNAL_PROPERTY throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEnableDefaultTyping_externalPropertyInclusion_throwsException() {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    // Tests subtype registration
    @Test
    public void testRegisterSubtypes_registersNamedSubtypes() {
        mapper.registerSubtypes(new NamedType(SimpleBean.class, "simple"));
        assertNotNull(mapper.getSubtypeResolver());
    }

    // Tests ObjectReader and ObjectWriter factory methods
    @Test
    public void testReaderAndWriter_constructValidInstances() throws Exception {
        ObjectWriter writer = mapper.writer();
        assertNotNull(writer);

        ObjectReader reader = mapper.reader(SimpleBean.class);
        assertNotNull(reader);

        SimpleBean bean = new SimpleBean(8, "readerWriter");
        String json = writer.writeValueAsString(bean);
        SimpleBean parsed = reader.readValue(json);
        assertEquals(8, parsed.id);
        assertEquals("readerWriter", parsed.name);
    }

    // Tests empty input string throws JsonMappingException due to end of input
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsMappingException() throws Exception {
        mapper.readValue("", SimpleBean.class);
    }

    // Tests version retrieval
    @Test
    public void testVersion_returnsNonNullVersion() {
        Version v = mapper.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // Tests date format configuration
    @Test
    public void testDateFormat_customFormat_appliedCorrectly() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        assertEquals(sdf, mapper.getDateFormat());

        Date d = sdf.parse("2023-01-15");
        DateBean bean = new DateBean(d);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("2023-01-15"));
    }

    // Tests locale and timezone configuration
    @Test
    public void testLocaleAndTimeZone_configuration() {
        mapper.setLocale(Locale.FRANCE);
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        assertNotNull(mapper.getSerializationConfig().getLocale());
        assertNotNull(mapper.getSerializationConfig().getTimeZone());
    }

    // Tests updating values into existing object instance
    @Test
    public void testUpdateValue_updatesExistingBean() throws Exception {
        SimpleBean target = new SimpleBean(1, "original");
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", "updated");

        SimpleBean result = mapper.updateValue(target, updates);
        assertSame(target, result);
        assertEquals(1, result.id);
        assertEquals("updated", result.name);
    }

    // Tests node factory setter and getter
    @Test
    public void testNodeFactory_customSetter() {
        JsonNodeFactory customFactory = new JsonNodeFactory(true);
        mapper.setNodeFactory(customFactory);
        assertSame(customFactory, mapper.getNodeFactory());
    }

    // Tests setting visibility checker
    @Test
    public void testVisibility_configuration() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        assertNotNull(mapper.getVisibilityChecker());
    }

    // Tests setting annotation introspector
    @Test
    public void testAnnotationIntrospector_configuration() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(ai);
        assertSame(ai, mapper.getSerializationConfig().getAnnotationIntrospector());
    }

    // Tests readValues returns MappingIterator
    @Test
    public void testReadValues_multipleValues_iteratesSuccessfully() throws Exception {
        String json = "{\"id\":1,\"name\":\"a\"}{\"id\":2,\"name\":\"b\"}";
        MappingIterator<SimpleBean> it = mapper.readValues(mapper.getFactory().createParser(json), SimpleBean.class);
        assertTrue(it.hasNext());
        assertEquals(1, it.next().id);
        assertTrue(it.hasNext());
        assertEquals(2, it.next().id);
        assertFalse(it.hasNext());
    }
}
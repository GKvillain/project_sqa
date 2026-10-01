package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    // Helper POJO for basic serialization and deserialization
    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() { }

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Abstract class and implementation for polymorphic/typing tests
    static abstract class AbstractBase {
        public int x;
    }

    static class ConcreteImpl extends AbstractBase {
        public ConcreteImpl() { }
        public ConcreteImpl(int x) { this.x = x; }
    }

    // Mix-in test helpers
    interface MixInSource {
        @JsonProperty("custom_id")
        int getId();
    }

    static class TargetBean {
        private int id;

        public TargetBean() { }
        public TargetBean(int id) { this.id = id; }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests default constructor and non-null initial configuration
    @Test
    public void testConstructor_default_initializesCorrectly() {
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getDeserializationContext());
        assertNotNull(mapper.getSerializerProvider());
        assertNotNull(mapper.getSerializerFactory());
        assertNotNull(mapper.getNodeFactory());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getFactory());
    }

    // Tests copy constructor creates independent yet equivalent instance
    @Test
    public void testCopy_standardMapper_createsIndependentCopy() {
        ObjectMapper copy = mapper.copy();
        assertNotNull(copy);
        assertNotSame(mapper, copy);
        assertNotSame(mapper.getFactory(), copy.getFactory());
    }

    // Tests readValue and writeValueAsString on a standard POJO
    @Test
    public void testReadAndWrite_simplePojo_success() throws Exception {
        SimpleBean bean = new SimpleBean(1, "test");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));

        SimpleBean deserialized = mapper.readValue(json, SimpleBean.class);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.id);
        assertEquals("test", deserialized.name);
    }

    // Tests writeValue and readValue with byte arrays and Streams
    @Test
    public void testReadAndWrite_byteAndStream_success() throws Exception {
        SimpleBean bean = new SimpleBean(42, "stream");
        byte[] bytes = mapper.writeValueAsBytes(bean);
        assertNotNull(bytes);

        SimpleBean fromBytes = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(42, fromBytes.id);
        assertEquals("stream", fromBytes.name);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, bean);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        SimpleBean fromStream = mapper.readValue(in, SimpleBean.class);
        assertEquals(42, fromStream.id);
        assertEquals("stream", fromStream.name);
    }

    // Tests writeValue and readValue using Reader and Writer
    @Test
    public void testReadAndWrite_readerWriter_success() throws Exception {
        SimpleBean bean = new SimpleBean(100, "rw");
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean);

        StringReader sr = new StringReader(sw.toString());
        SimpleBean read = mapper.readValue(sr, SimpleBean.class);
        assertEquals(100, read.id);
        assertEquals("rw", read.name);
    }

    // Tests reading generic Collection using TypeReference
    @Test
    public void testReadValue_typeReference_deserializesList() throws Exception {
        String json = "[{\"id\":10,\"name\":\"item1\"},{\"id\":20,\"name\":\"item2\"}]";
        List<SimpleBean> list = mapper.readValue(json, new TypeReference<List<SimpleBean>>() {});
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(10, list.get(0).id);
        assertEquals(20, list.get(1).id);
    }

    // Tests JSON Tree model creation and reading
    @Test
    public void testTreeModel_readAndWrite_success() throws Exception {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("key", "value");
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add(1);
        arrayNode.add(2);
        objectNode.set("array", arrayNode);

        String json = mapper.writeValueAsString(objectNode);
        JsonNode readNode = mapper.readTree(json);
        assertTrue(readNode.isObject());
        assertEquals("value", readNode.get("key").asText());
        assertEquals(2, readNode.get("array").size());
        assertEquals(1, readNode.get("array").get(0).asInt());
    }

    // Tests treeToValue and valueToTree conversions
    @Test
    public void testValueToTreeAndTreeToValue_conversion() throws Exception {
        SimpleBean bean = new SimpleBean(7, "convert");
        JsonNode node = mapper.valueToTree(bean);
        assertTrue(node.isObject());
        assertEquals(7, node.get("id").asInt());

        SimpleBean converted = mapper.treeToValue(node, SimpleBean.class);
        assertNotNull(converted);
        assertEquals(7, converted.id);
        assertEquals("convert", converted.name);
    }

    // Tests convertValue utility method
    @Test
    public void testConvertValue_mapToPojo_success() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 99);
        map.put("name", "mapped");

        SimpleBean bean = mapper.convertValue(map, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(99, bean.id);
        assertEquals("mapped", bean.name);

        assertNull(mapper.convertValue(null, SimpleBean.class));
    }

    // Tests canSerialize and canDeserialize reflection query methods
    @Test
    public void testCanSerializeAndCanDeserialize_supportedTypes_returnsTrue() {
        assertTrue(mapper.canSerialize(SimpleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        assertNull(cause.get());

        JavaType type = mapper.constructType(SimpleBean.class);
        assertTrue(mapper.canDeserialize(type));
        assertTrue(mapper.canDeserialize(type, cause));
        assertNull(cause.get());
    }

    // Tests mix-in annotation registration
    @Test
    public void testMixInAnnotations_appliesCorrectly() throws Exception {
        mapper.addMixIn(TargetBean.class, MixInSource.class);
        assertEquals(1, mapper.mixInCount());
        assertEquals(MixInSource.class, mapper.findMixInClassFor(TargetBean.class));

        TargetBean bean = new TargetBean(123);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"custom_id\":123"));

        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mapper.setMixIns(mixins);
        assertEquals(0, mapper.mixInCount());
    }

    // Tests DefaultTyping configuration and serialization for polymorphism
    @Test
    public void testDefaultTyping_objectAndNonConcrete_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        AbstractBase[] array = new AbstractBase[] { new ConcreteImpl(5) };
        String json = mapper.writeValueAsString(array);
        assertTrue(json.contains(ConcreteImpl.class.getName()));

        AbstractBase[] deserialized = mapper.readValue(json, AbstractBase[].class);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.length);
        assertTrue(deserialized[0] instanceof ConcreteImpl);
        assertEquals(5, deserialized[0].x);

        mapper.disableDefaultTyping();
    }

    // Tests DefaultTyping with NON_FINAL and array types
    @Test
    public void testDefaultTyping_nonFinalArrays_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        Object[] data = new Object[] { new ConcreteImpl(10) };
        String json = mapper.writeValueAsString(data);
        assertTrue(json.contains(ConcreteImpl.class.getName()));

        Object[] result = mapper.readValue(json, Object[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof ConcreteImpl);

        mapper.disableDefaultTyping();
    }

    // Tests configuration of MapperFeature, SerializationFeature, and DeserializationFeature
    @Test
    public void testConfigurationFeatures_enableDisable() {
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        assertTrue(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        mapper.disable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        assertFalse(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    // Tests ObjectReader and ObjectWriter factory methods
    @Test
    public void testReaderAndWriterFactories_constructValidInstances() throws Exception {
        ObjectWriter writer = mapper.writer();
        assertNotNull(writer);
        SimpleBean bean = new SimpleBean(55, "writer");
        String json = writer.writeValueAsString(bean);

        ObjectReader reader = mapper.readerFor(SimpleBean.class);
        assertNotNull(reader);
        SimpleBean read = reader.readValue(json);
        assertEquals(55, read.id);
        assertEquals("writer", read.name);
    }

    // Tests subtype registration
    @Test
    public void testRegisterSubtypes_classesAndNamedTypes() {
        mapper.registerSubtypes(ConcreteImpl.class);
        mapper.registerSubtypes(new NamedType(ConcreteImpl.class, "concrete"));
        assertNotNull(mapper.getSubtypeResolver());
    }

    // Tests invalid JSON parsing throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReadValue_malformedJson_throwsJsonParseException() throws Exception {
        mapper.readValue("{malformed:json}", SimpleBean.class);
    }

    // Tests registering module with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterModule_nullModuleName_throwsException() {
        mapper.registerModule(new Module() {
            @Override
            public String getModuleName() { return null; }

            @Override
            public com.fasterxml.jackson.core.Version version() {
                return com.fasterxml.jackson.core.Version.unknownVersion();
            }

            @Override
            public void setupModule(SetupContext context) { }
        });
    }

    // Tests setting visibility checker and property serialization inclusion
    @Test
    public void testSetVisibilityAndInclusion_modifiesConfig() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        assertNotNull(mapper.getVisibilityChecker());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertEquals(JsonInclude.Include.NON_NULL, mapper.getSerializationConfig().getSerializationInclusion());
    }

    // Tests File-based read and write operations
    @Test
    public void testReadAndWrite_file_success() throws Exception {
        File tempFile = File.createTempFile("jackson_test", ".json");
        tempFile.deleteOnExit();

        SimpleBean bean = new SimpleBean(77, "fileTest");
        mapper.writeValue(tempFile, bean);

        SimpleBean read = mapper.readValue(tempFile, SimpleBean.class);
        assertNotNull(read);
        assertEquals(77, read.id);
        assertEquals("fileTest", read.name);

        JsonNode treeFromFile = mapper.readTree(tempFile);
        assertNotNull(treeFromFile);
        assertEquals(77, treeFromFile.get("id").asInt());
    }

    // Tests readValue using JsonParser
    @Test
    public void testReadValue_jsonParser_success() throws Exception {
        String json = "{\"id\":33,\"name\":\"parser\"}";
        JsonParser parser = mapper.getFactory().createParser(json);
        SimpleBean bean = mapper.readValue(parser, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(33, bean.id);
        assertEquals("parser", bean.name);
        parser.close();
    }

    // Tests readValues with MappingIterator
    @Test
    public void testReadValues_multipleObjects_success() throws Exception {
        String json = "{\"id\":1,\"name\":\"a\"}\n{\"id\":2,\"name\":\"b\"}";
        JsonParser parser = mapper.getFactory().createParser(json);
        MappingIterator<SimpleBean> iterator = mapper.readValues(parser, SimpleBean.class);

        assertTrue(iterator.hasNext());
        SimpleBean first = iterator.next();
        assertEquals(1, first.id);
        assertEquals("a", first.name);

        assertTrue(iterator.hasNext());
        SimpleBean second = iterator.next();
        assertEquals(2, second.id);
        assertEquals("b", second.name);

        assertFalse(iterator.hasNext());
        iterator.close();
    }

    // Tests readTree variants for byte array, InputStream, Reader, and JsonParser
    @Test
    public void testReadTree_variousSources_success() throws Exception {
        String json = "{\"count\":5}";

        JsonNode fromBytes = mapper.readTree(json.getBytes("UTF-8"));
        assertEquals(5, fromBytes.get("count").asInt());

        JsonNode fromStream = mapper.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        assertEquals(5, fromStream.get("count").asInt());

        JsonNode fromReader = mapper.readTree(new StringReader(json));
        assertEquals(5, fromReader.get("count").asInt());

        JsonParser parser = mapper.getFactory().createParser(json);
        JsonNode fromParser = mapper.readTree(parser);
        assertEquals(5, fromParser.get("count").asInt());
        parser.close();
    }

    // Tests updateValue and readerForUpdating
    @Test
    public void testUpdateValue_updatesExistingObject() throws Exception {
        SimpleBean target = new SimpleBean(1, "original");
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", "updated");

        SimpleBean result = mapper.updateValue(target, updates);
        assertSame(target, result);
        assertEquals(1, result.id);
        assertEquals("updated", result.name);

        mapper.readerForUpdating(target).readValue("{\"id\":2}");
        assertEquals(2, target.id);
        assertEquals("updated", target.name);
    }

    // Tests date, timezone, and locale configuration
    @Test
    public void testDateAndLocaleConfiguration() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        mapper.setDateFormat(sdf);
        assertEquals(sdf, mapper.getDateFormat());

        TimeZone tz = TimeZone.getTimeZone("GMT+0");
        mapper.setTimeZone(tz);
        mapper.setLocale(Locale.GERMANY);

        Date date = new Date(0);
        String formatted = mapper.writeValueAsString(date);
        assertTrue(formatted.contains("1970/01/01"));
    }

    // Tests filter provider registration and configuration
    @Test
    public void testFilterProvider_configuration() {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filterProvider);
        assertSame(filterProvider, mapper.getSerializationConfig().getFilterProvider());
    }

    // Tests problem handlers management
    @Test
    public void testDeserializationProblemHandlers() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();
    }

    // Tests JsonParser and JsonGenerator feature configuration
    @Test
    public void testParserAndGeneratorFeatureConfiguration() {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, false);
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        assertTrue(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    // Tests custom node factory and type factory setup
    @Test
    public void testNodeAndTypeFactoryConfiguration() {
        JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
        mapper.setNodeFactory(nodeFactory);
        assertSame(nodeFactory, mapper.getNodeFactory());

        TypeFactory tf = TypeFactory.defaultInstance();
        mapper.setTypeFactory(tf);
        assertSame(tf, mapper.getTypeFactory());
    }

    // Tests annotation introspector and base64 configuration
    @Test
    public void testIntrospectorAndBase64Configuration() {
        JacksonAnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(introspector);
        mapper.setBase64Variant(Base64Variants.MIME);
        assertNotNull(mapper.getSerializationConfig().getAnnotationIntrospector());
    }

    // Tests registerModule and version getter
    @Test
    public void testModuleRegistrationAndVersion() {
        SimpleModule module = new SimpleModule("CustomTestModule");
        mapper.registerModule(module);
        assertNotNull(mapper.version());
        assertFalse(mapper.version().isUknownVersion());
    }

    // Tests JSON format visitor acceptance
    @Test
    public void testAcceptJsonFormatVisitor_executesWithoutError() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
        mapper.acceptJsonFormatVisitor(mapper.constructType(SimpleBean.class), visitor);
    }
}
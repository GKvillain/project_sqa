package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    public static class Target {
        public int a;
        public String b;

        public Target() {}

        public Target(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewTarget {
        @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
        public String pub;

        @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class)
        public String priv;
    }

    public static class InjectedTarget {
        @com.fasterxml.jackson.annotation.JacksonInject("val")
        public String injected;
        public int normal;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    // Tests normal deserialization from a valid JSON string into a POJO
    @Test
    public void testReadValue_validJsonString_returnsObject() throws IOException {
        ObjectReader typedReader = reader.forType(Target.class);
        Target target = typedReader.readValue("{\"a\":10,\"b\":\"test\"}");
        assertNotNull(target);
        assertEquals(10, target.a);
        assertEquals("test", target.b);
    }

    // Tests deserialization from a byte array input
    @Test
    public void testReadValue_validByteArray_returnsObject() throws IOException {
        byte[] bytes = "{\"a\":20,\"b\":\"bytes\"}".getBytes(StandardCharsets.UTF_8);
        Target target = reader.forType(Target.class).readValue(bytes);
        assertNotNull(target);
        assertEquals(20, target.a);
        assertEquals("bytes", target.b);
    }

    // Tests deserialization from a byte array slice with offset and length
    @Test
    public void testReadValue_byteArraySlice_returnsObject() throws IOException {
        byte[] bytes = "XX{\"a\":30,\"b\":\"slice\"}YY".getBytes(StandardCharsets.UTF_8);
        Target target = reader.forType(Target.class).readValue(bytes, 2, bytes.length - 4);
        assertNotNull(target);
        assertEquals(30, target.a);
        assertEquals("slice", target.b);
    }

    // Tests deserialization from an InputStream
    @Test
    public void testReadValue_validInputStream_returnsObject() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"a\":40,\"b\":\"stream\"}".getBytes(StandardCharsets.UTF_8));
        Target target = reader.forType(Target.class).readValue(in);
        assertNotNull(target);
        assertEquals(40, target.a);
        assertEquals("stream", target.b);
    }

    // Tests deserialization from a Reader source
    @Test
    public void testReadValue_validReader_returnsObject() throws IOException {
        StringReader sr = new StringReader("{\"a\":50,\"b\":\"reader\"}");
        Target target = reader.forType(Target.class).readValue(sr);
        assertNotNull(target);
        assertEquals(50, target.a);
        assertEquals("reader", target.b);
    }

    // Tests deserialization using a custom JsonParser directly
    @Test
    public void testReadValue_jsonParser_returnsObject() throws IOException {
        JsonParser p = mapper.getFactory().createParser("{\"a\":60,\"b\":\"parser\"}");
        Target target = reader.forType(Target.class).readValue(p);
        assertNotNull(target);
        assertEquals(60, target.a);
        assertEquals("parser", target.b);
        p.close();
    }

    // Tests deserialization from a JsonNode tree
    @Test
    public void testReadValue_jsonNode_returnsObject() throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("a", 70);
        node.put("b", "node");
        Target target = reader.forType(Target.class).readValue(node);
        assertNotNull(target);
        assertEquals(70, target.a);
        assertEquals("node", target.b);
    }

    // Tests parsing JSON string into a JsonNode tree
    @Test
    public void testReadTree_validString_returnsJsonNode() throws IOException {
        JsonNode node = reader.readTree("{\"a\":80,\"b\":\"tree\"}");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(80, node.get("a").asInt());
        assertEquals("tree", node.get("b").asText());
    }

    // Tests parsing InputStream into a JsonNode tree
    @Test
    public void testReadTree_validInputStream_returnsJsonNode() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"a\":90}".getBytes(StandardCharsets.UTF_8));
        JsonNode node = reader.readTree(in);
        assertNotNull(node);
        assertEquals(90, node.get("a").asInt());
    }

    // Tests reading a sequence of values from a JSON string using MappingIterator
    @Test
    public void testReadValues_jsonString_returnsMappingIterator() throws IOException {
        String json = "{\"a\":1,\"b\":\"one\"} {\"a\":2,\"b\":\"two\"}";
        MappingIterator<Target> it = reader.forType(Target.class).readValues(json);
        assertTrue(it.hasNext());
        Target first = it.next();
        assertEquals(1, first.a);
        assertEquals("one", first.b);
        assertTrue(it.hasNext());
        Target second = it.next();
        assertEquals(2, second.a);
        assertEquals("two", second.b);
        assertFalse(it.hasNext());
        it.close();
    }

    // Tests reading a sequence of values from a byte array
    @Test
    public void testReadValues_byteArray_returnsMappingIterator() throws IOException {
        byte[] bytes = "[{\"a\":100},{\"a\":200}]".getBytes(StandardCharsets.UTF_8);
        MappingIterator<Target> it = reader.forType(Target.class).readValues(bytes);
        assertTrue(it.hasNext());
        assertEquals(100, it.next().a);
        assertTrue(it.hasNext());
        assertEquals(200, it.next().a);
        assertFalse(it.hasNext());
        it.close();
    }

    // Tests updating an existing object instance instead of creating a new one
    @Test
    public void testWithValueToUpdate_validObject_updatesExistingInstance() throws IOException {
        Target existing = new Target(1, "initial");
        Target updated = reader.forType(Target.class).withValueToUpdate(existing).readValue("{\"b\":\"modified\"}");
        assertSame(existing, updated);
        assertEquals(1, updated.a);
        assertEquals("modified", updated.b);
    }

    // Tests exception when passing null to withValueToUpdate
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_nullValue_throwsIllegalArgumentException() {
        reader.withValueToUpdate(null);
    }

    // Tests exception when attempting to update an array type
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_arrayType_throwsIllegalArgumentException() {
        reader.forType(int[].class).withValueToUpdate(new int[]{1, 2});
    }

    // Tests type configuration with TypeReference
    @Test
    public void testForType_typeReference_deserializesGenericCollection() throws IOException {
        String json = "[{\"a\":1,\"b\":\"first\"},{\"a\":2,\"b\":\"second\"}]";
        List<Target> list = reader.forType(new TypeReference<List<Target>>() {}).readValue(json);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).a);
        assertEquals("second", list.get(1).b);
    }

    // Tests feature reconfiguration with DeserializationFeature
    @Test(expected = JsonMappingException.class)
    public void testWithFeatures_failOnUnknownProperties_throwsExceptionOnUnknown() throws IOException {
        ObjectReader strictReader = reader.forType(Target.class).with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(strictReader.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        strictReader.readValue("{\"a\":1,\"unknown\":true}");
    }

    // Tests feature disabling with without()
    @Test
    public void testWithout_failOnUnknownProperties_ignoresUnknownField() throws IOException {
        ObjectReader lenientReader = reader.forType(Target.class).without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(lenientReader.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Target target = lenientReader.readValue("{\"a\":5,\"unknown\":true}");
        assertNotNull(target);
        assertEquals(5, target.a);
    }

    // Tests JSON Pointer based filtering using at()
    @Test
    public void testAt_jsonPointer_extractsSubField() throws IOException {
        String json = "{\"outer\":{\"inner\":{\"a\":42,\"b\":\"nested\"}}}";
        Target target = reader.forType(Target.class).at("/outer/inner").readValue(json);
        assertNotNull(target);
        assertEquals(42, target.a);
        assertEquals("nested", target.b);
    }

    // Tests root unwrapping feature with withRootName()
    @Test
    public void testWithRootName_rootWrappingEnabled_unwrapsRootCorrectly() throws IOException {
        ObjectReader rootReader = reader.forType(Target.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("payload");
        Target target = rootReader.readValue("{\"payload\":{\"a\":77,\"b\":\"wrapped\"}}");
        assertNotNull(target);
        assertEquals(77, target.a);
        assertEquals("wrapped", target.b);
    }

    // Tests exception on empty input string
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsJsonMappingException() throws IOException {
        reader.forType(Target.class).readValue("");
    }

    // Tests format detection rejection when char-based source is passed
    @Test(expected = JsonParseException.class)
    public void testWithFormatDetection_charSource_throwsJsonParseException() throws IOException {
        ObjectReader r1 = reader.forType(Target.class);
        ObjectReader r2 = reader.forType(Map.class);
        ObjectReader detectorReader = r1.withFormatDetection(r1, r2);
        detectorReader.readValue(new StringReader("{\"a\":1}"));
    }

    // Tests conversion of TreeNode into value via treeToValue
    @Test
    public void testTreeToValue_validTreeNode_convertsToValue() throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("a", 99);
        node.put("b", "fromTree");
        Target target = reader.treeToValue(node, Target.class);
        assertNotNull(target);
        assertEquals(99, target.a);
        assertEquals("fromTree", target.b);
    }

    // --- New Tests for Full Coverage ---

    @Test
    public void testVersionAndBasicGetters() {
        Version v = reader.version();
        assertNotNull(v);
        assertFalse(v.isUknownVersion());

        assertNotNull(reader.getFactory());
        assertNotNull(reader.getConfig());
        assertNotNull(reader.getTypeFactory());
        assertNotNull(reader.getContextAttributes());
        assertNotNull(reader.getAttributes());
    }

    @Test
    public void testCreateNodeMethods() {
        ObjectNode objNode = reader.createObjectNode();
        assertNotNull(objNode);
        assertTrue(objNode.isObject());

        ArrayNode arrNode = reader.createArrayNode();
        assertNotNull(arrNode);
        assertTrue(arrNode.isArray());
    }

    @Test
    public void testWithTypeVariants() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Target.class);
        ObjectReader rJavaType = reader.withType(javaType);
        Target t1 = rJavaType.readValue("{\"a\":1,\"b\":\"javaType\"}");
        assertEquals(1, t1.a);

        ObjectReader rClass = reader.withType(Target.class);
        Target t2 = rClass.readValue("{\"a\":2,\"b\":\"class\"}");
        assertEquals(2, t2.a);

        ObjectReader rType = reader.withType((java.lang.reflect.Type) Target.class);
        Target t3 = rType.readValue("{\"a\":3,\"b\":\"type\"}");
        assertEquals(3, t3.a);

        ObjectReader rTypeRef = reader.withType(new TypeReference<Target>() {});
        Target t4 = rTypeRef.readValue("{\"a\":4,\"b\":\"typeRef\"}");
        assertEquals(4, t4.a);

        ObjectReader forTypeClass = reader.forType((Class<?>) Target.class);
        Target t5 = forTypeClass.readValue("{\"a\":5,\"b\":\"forTypeClass\"}");
        assertEquals(5, t5.a);

        ObjectReader forTypeJavaType = reader.forType(javaType);
        Target t6 = forTypeJavaType.readValue("{\"a\":6,\"b\":\"forTypeJavaType\"}");
        assertEquals(6, t6.a);

        ObjectReader forTypeReflect = reader.forType((java.lang.reflect.Type) Target.class);
        Target t7 = forTypeReflect.readValue("{\"a\":7,\"b\":\"forTypeReflect\"}");
        assertEquals(7, t7.a);
    }

    @Test
    public void testWithConfigAndFactory() throws IOException {
        DeserializationConfig config = reader.getConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ObjectReader rConfig = reader.with(config);
        assertSame(config, rConfig.getConfig());

        JsonFactory factory = new JsonFactory();
        ObjectReader rFactory = reader.with(factory);
        assertSame(factory, rFactory.getFactory());
    }

    @Test
    public void testWithLocaleAndTimeZoneAndBase64() {
        Locale locale = Locale.GERMANY;
        ObjectReader rLocale = reader.with(locale);
        assertEquals(locale, rLocale.getConfig().getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        ObjectReader rTz = reader.with(tz);
        assertEquals(tz, rTz.getConfig().getTimeZone());

        ObjectReader rB64 = reader.with(Base64Variants.MODIFIED_FOR_URL);
        assertEquals(Base64Variants.MODIFIED_FOR_URL, rB64.getConfig().getBase64Variant());
    }

    @Test
    public void testWithAttributes() throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k1", "v1");
        ObjectReader rAttrs = reader.with(attrs);
        assertEquals("v1", rAttrs.getAttributes().getAttribute("k1"));

        ObjectReader rSingleAttr = reader.withAttribute("k2", "v2");
        assertEquals("v2", rSingleAttr.getAttributes().getAttribute("k2"));

        ObjectReader rWithoutAttr = rSingleAttr.withoutAttribute("k2");
        assertNull(rWithoutAttr.getAttributes().getAttribute("k2"));
    }

    @Test
    public void testWithInjectableValues() throws IOException {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("val", "injected_value");

        ObjectReader rInject = reader.forType(InjectedTarget.class).with(injectables);
        InjectedTarget target = rInject.readValue("{\"normal\":123}");
        assertEquals(123, target.normal);
        assertEquals("injected_value", target.injected);
    }

    @Test
    public void testWithView() throws IOException {
        ViewTarget target = new ViewTarget();
        target.pub = "pubVal";
        target.priv = "privVal";
        String json = mapper.writeValueAsString(target);

        ObjectReader rPub = reader.forType(ViewTarget.class).withView(Views.Public.class);
        ViewTarget resPub = rPub.readValue(json);
        assertEquals("pubVal", resPub.pub);
        assertNull(resPub.priv);

        ObjectReader rPriv = reader.forType(ViewTarget.class).withView(Views.Internal.class);
        ViewTarget resPriv = rPriv.readValue(json);
        assertEquals("pubVal", resPriv.pub);
        assertEquals("privVal", resPriv.priv);
    }

    @Test
    public void testWithHandler() throws IOException {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
                                                 JsonDeserializer<?> deserializer, Object beanOrClass,
                                                 String propertyName) throws IOException {
                p.skipChildren();
                return true;
            }
        };

        ObjectReader rStrict = reader.forType(Target.class).with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ObjectReader rHandler = rStrict.withHandler(handler);
        Target t = rHandler.readValue("{\"a\":55,\"extra\":999}");
        assertEquals(55, t.a);
    }

    @Test
    public void testWithRootNamePropertyName() throws IOException {
        PropertyName pName = new PropertyName("rootObj");
        ObjectReader rRoot = reader.forType(Target.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName(pName);
        Target t = rRoot.readValue("{\"rootObj\":{\"a\":11,\"b\":\"hello\"}}");
        assertEquals(11, t.a);
        assertEquals("hello", t.b);

        ObjectReader rNoRoot = rRoot.withoutRootName();
        assertFalse(rNoRoot.getConfig().isAnnotationProcessingEnabled() && rNoRoot.getConfig().getRootName() != null);
    }

    @Test
    public void testMultipleDeserializationFeatures() {
        ObjectReader r = reader.withFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        ObjectReader r2 = r.withoutFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(r2.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(r2.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        ObjectReader r3 = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(r3.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(r3.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testJsonParserFeatures() {
        ObjectReader r = reader.with(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(r.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        ObjectReader r2 = r.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(r2.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        ObjectReader r3 = reader.withFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertTrue(r3.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(r3.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));

        ObjectReader r4 = r3.withoutFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertFalse(r4.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(r4.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
    }

    @Test
    public void testReadTreeVariants() throws IOException {
        byte[] bytes = "{\"k\":\"v1\"}".getBytes(StandardCharsets.UTF_8);
        JsonNode n1 = reader.readTree(bytes);
        assertEquals("v1", n1.get("k").asText());

        byte[] sliceBytes = "abc{\"k\":\"v2\"}xyz".getBytes(StandardCharsets.UTF_8);
        JsonNode n2 = reader.readTree(sliceBytes, 3, 10);
        assertEquals("v2", n2.get("k").asText());

        JsonNode n3 = reader.readTree(new StringReader("{\"k\":\"v3\"}"));
        assertEquals("v3", n3.get("k").asText());

        JsonParser p = mapper.getFactory().createParser("{\"k\":\"v4\"}");
        JsonNode n4 = reader.readTree(p);
        assertEquals("v4", n4.get("k").asText());
        p.close();
    }

    @Test
    public void testReadValuesVariants() throws IOException {
        String json = "{\"a\":1} {\"a\":2}";

        // readValues(InputStream)
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        MappingIterator<Target> it1 = reader.forType(Target.class).readValues(in);
        assertEquals(1, it1.next().a);
        assertEquals(2, it1.next().a);
        it1.close();

        // readValues(Reader)
        StringReader sr = new StringReader(json);
        MappingIterator<Target> it2 = reader.forType(Target.class).readValues(sr);
        assertEquals(1, it2.next().a);
        assertEquals(2, it2.next().a);
        it2.close();

        // readValues(JsonParser)
        JsonParser p = mapper.getFactory().createParser(json);
        MappingIterator<Target> it3 = reader.forType(Target.class).readValues(p);
        assertEquals(1, it3.next().a);
        assertEquals(2, it3.next().a);
        it3.close();

        // readValues(byte[], int, int)
        byte[] slice = ("XYZ" + json + "XYZ").getBytes(StandardCharsets.UTF_8);
        MappingIterator<Target> it4 = reader.forType(Target.class).readValues(slice, 3, json.length());
        assertEquals(1, it4.next().a);
        assertEquals(2, it4.next().a);
        it4.close();
    }

    @Test
    public void testFileAndURLReadValueAndReadValues() throws IOException {
        File tempFile = File.createTempFile("objectreader_test", ".json");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("{\"a\":123,\"b\":\"file\"}".getBytes(StandardCharsets.UTF_8));
        }

        // readValue(File)
        Target tFile = reader.forType(Target.class).readValue(tempFile);
        assertEquals(123, tFile.a);
        assertEquals("file", tFile.b);

        // readValue(URL)
        Target tUrl = reader.forType(Target.class).readValue(tempFile.toURI().toURL());
        assertEquals(123, tUrl.a);
        assertEquals("file", tUrl.b);

        // readValues(File)
        MappingIterator<Target> itFile = reader.forType(Target.class).readValues(tempFile);
        assertTrue(itFile.hasNext());
        assertEquals(123, itFile.next().a);
        itFile.close();

        // readValues(URL)
        MappingIterator<Target> itUrl = reader.forType(Target.class).readValues(tempFile.toURI().toURL());
        assertTrue(itUrl.hasNext());
        assertEquals(123, itUrl.next().a);
        itUrl.close();
    }

    @Test
    public void testTreeAsTokensAndWriteTree() throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("a", 88);

        JsonParser parser = reader.treeAsTokens(node);
        assertNotNull(parser);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Target t = reader.forType(Target.class).readValue(parser);
        assertEquals(88, t.a);
        parser.close();

        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        reader.writeTree(g, node);
        g.close();
        assertEquals("{\"a\":88}", sw.toString());
    }

    @Test
    public void testAtWithStringPointer() throws IOException {
        JsonPointer ptr = JsonPointer.compile("/deep/item");
        ObjectReader r = reader.forType(Target.class).at(ptr);
        Target t = r.readValue("{\"deep\":{\"item\":{\"a\":500,\"b\":\"pointer\"}}}");
        assertEquals(500, t.a);
        assertEquals("pointer", t.b);
    }

    @Test
    public void testWithSchema() {
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "TEST_SCHEMA";
            }
        };
        ObjectReader rSchema = reader.with(schema);
        assertSame(schema, rSchema.getConfig().getSchema());
    }

    @Test
    public void testFormatDetectionWithValidBytes() throws IOException {
        ObjectReader rTarget = reader.forType(Target.class);
        ObjectReader rMap = reader.forType(Map.class);
        ObjectReader detector = rTarget.withFormatDetection(rTarget, rMap);

        byte[] input = "{\"a\":12,\"b\":\"detector\"}".getBytes(StandardCharsets.UTF_8);
        Target t = (Target) detector.readValue(input);
        assertEquals(12, t.a);
        assertEquals("detector", t.b);

        DataFormatReaders dfReaders = new DataFormatReaders(rTarget, rMap);
        ObjectReader detector2 = rTarget.withFormatDetection(dfReaders);
        Target t2 = (Target) detector2.readValue(new ByteArrayInputStream(input));
        assertEquals(12, t2.a);
        assertEquals("detector", t2.b);
    }

    @Test
    public void testFormatDetectionReadValues() throws IOException {
        ObjectReader rTarget = reader.forType(Target.class);
        ObjectReader detector = rTarget.withFormatDetection(rTarget);

        byte[] input = "{\"a\":1}{\"a\":2}".getBytes(StandardCharsets.UTF_8);
        MappingIterator<Target> it = detector.readValues(new ByteArrayInputStream(input));
        assertTrue(it.hasNext());
        assertEquals(1, it.next().a);
        assertEquals(2, it.next().a);
        it.close();
    }
}
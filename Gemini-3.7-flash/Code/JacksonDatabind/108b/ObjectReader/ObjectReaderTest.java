package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectReaderTest {

    private ObjectMapper _mapper;
    private ObjectReader _reader;

    public interface PublicView {}
    public interface ExtendedView extends PublicView {}

    public static class Pojo {
        public int id;
        public String name;

        public Pojo() {}

        public Pojo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class ViewPojo {
        @JsonView(PublicView.class)
        public int publicVal;

        @JsonView(ExtendedView.class)
        public int extendedVal;
    }

    public static class InjectedPojo {
        public int id;
        public String value;
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _reader = _mapper.reader();
    }

    // Tests normal deserialization from String input into POJO
    @Test
    public void testReadValue_stringInput_returnsPojo() throws IOException {
        String json = "{\"id\":123,\"name\":\"test\"}";
        Pojo result = _reader.forType(Pojo.class).readValue(json);
        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("test", result.name);
    }

    // Tests deserialization from byte array input
    @Test
    public void testReadValue_byteArrayInput_returnsPojo() throws IOException {
        byte[] bytes = "{\"id\":456,\"name\":\"bytes\"}".getBytes("UTF-8");
        Pojo result = _reader.forType(Pojo.class).readValue(bytes);
        assertNotNull(result);
        assertEquals(456, result.id);
        assertEquals("bytes", result.name);
    }

    // Tests deserialization from InputStream input
    @Test
    public void testReadValue_inputStreamInput_returnsPojo() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"id\":789,\"name\":\"stream\"}".getBytes("UTF-8"));
        Pojo result = _reader.forType(Pojo.class).readValue(in);
        assertNotNull(result);
        assertEquals(789, result.id);
        assertEquals("stream", result.name);
    }

    // Tests deserialization from Reader input
    @Test
    public void testReadValue_readerInput_returnsPojo() throws IOException {
        StringReader sr = new StringReader("{\"id\":999,\"name\":\"reader\"}");
        Pojo result = _reader.forType(Pojo.class).readValue(sr);
        assertNotNull(result);
        assertEquals(999, result.id);
        assertEquals("reader", result.name);
    }

    // Tests reading value from JsonNode directly
    @Test
    public void testReadValue_jsonNodeInput_returnsPojo() throws IOException {
        JsonNode node = _mapper.readTree("{\"id\":111,\"name\":\"node\"}");
        Pojo result = _reader.forType(Pojo.class).readValue(node);
        assertNotNull(result);
        assertEquals(111, result.id);
        assertEquals("node", result.name);
    }

    // Tests updating an existing object instance
    @Test
    public void testReadValue_withValueToUpdate_updatesExistingObject() throws IOException {
        Pojo target = new Pojo(1, "initial");
        Pojo updated = _reader.withValueToUpdate(target).readValue("{\"name\":\"updated\"}");
        assertSame(target, updated);
        assertEquals(1, target.id);
        assertEquals("updated", target.name);
    }

    // Tests exception on empty input string when reading value
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsException() throws IOException {
        _reader.forType(Pojo.class).readValue("");
    }

    // Tests reading JSON null literal
    @Test
    public void testReadValue_nullLiteral_returnsNull() throws IOException {
        Pojo result = _reader.forType(Pojo.class).readValue("null");
        assertNull(result);
    }

    // Tests readTree from String input
    @Test
    public void testReadTree_stringInput_returnsJsonNode() throws IOException {
        JsonNode node = _reader.readTree("{\"key\":\"value\"}");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("value", node.get("key").asText());
    }

    // Tests readTree from empty input returning missing node
    @Test
    public void testReadTree_emptyString_returnsMissingNode() throws IOException {
        JsonNode node = _reader.readTree("");
        assertNotNull(node);
        assertTrue(node.isMissingNode());
    }

    // Tests reading sequence of values from parser
    @Test
    public void testReadValues_jsonArray_returnsIterator() throws IOException {
        String json = "[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]";
        Iterator<Pojo> it = _reader.forType(Pojo.class).readValues(json);
        assertNotNull(it);
        assertTrue(it.hasNext());
        Pojo p1 = it.next();
        assertEquals(1, p1.id);
        assertTrue(it.hasNext());
        Pojo p2 = it.next();
        assertEquals(2, p2.id);
        assertFalse(it.hasNext());
    }

    // Tests reading value using JsonParser with explicit JavaType and TypeReference
    @Test
    public void testReadValue_withTypeReference_returnsList() throws IOException {
        String json = "[{\"id\":10,\"name\":\"item1\"}]";
        JsonParser p = _reader.getFactory().createParser(json);
        List<Pojo> list = _reader.readValue(p, new TypeReference<List<Pojo>>() {});
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(10, list.get(0).id);
        p.close();
    }

    // Tests filtering via JsonPointer (at method)
    @Test
    public void testAt_jsonPointer_readsSubTree() throws IOException {
        String json = "{\"root\":{\"nested\":{\"id\":55,\"name\":\"at-filter\"}}}";
        ObjectReader pointerReader = _reader.forType(Pojo.class).at(JsonPointer.compile("/root/nested"));
        Pojo result = pointerReader.readValue(json);
        assertNotNull(result);
        assertEquals(55, result.id);
        assertEquals("at-filter", result.name);
    }

    // Tests treeToValue conversion
    @Test
    public void testTreeToValue_validTreeNode_returnsPojo() throws Exception {
        JsonNode node = _mapper.readTree("{\"id\":77,\"name\":\"treeToValue\"}");
        Pojo result = _reader.treeToValue(node, Pojo.class);
        assertNotNull(result);
        assertEquals(77, result.id);
        assertEquals("treeToValue", result.name);
    }

    // Tests treeAsTokens creating a JsonParser
    @Test
    public void testTreeAsTokens_treeNode_createsParser() throws IOException {
        JsonNode node = _mapper.readTree("{\"id\":88}");
        JsonParser parser = _reader.treeAsTokens(node);
        assertNotNull(parser);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(88, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests createObjectNode and createArrayNode
    @Test
    public void testCreateNodes_createsObjectAndArray() {
        ObjectNode objNode = _reader.createObjectNode();
        assertNotNull(objNode);
        assertTrue(objNode.isObject());

        ArrayNode arrNode = _reader.createArrayNode();
        assertNotNull(arrNode);
        assertTrue(arrNode.isArray());
    }

    // Tests configuration modification via fluent with/without methods
    @Test
    public void testWithAndWithout_features_modifiesConfig() {
        ObjectReader r = _reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        r = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        ObjectReader rWithLocale = _reader.with(Locale.GERMANY);
        assertEquals(Locale.GERMANY, rWithLocale.getConfig().getLocale());

        ObjectReader rWithTZ = _reader.with(TimeZone.getTimeZone("GMT"));
        assertEquals(TimeZone.getTimeZone("GMT"), rWithTZ.getConfig().getTimeZone());

        ObjectReader rWithBase64 = _reader.with(Base64Variants.MODIFIED_FOR_URL);
        assertEquals(Base64Variants.MODIFIED_FOR_URL, rWithBase64.getConfig().getBase64Variant());
    }

    // Tests ContextAttributes handling
    @Test
    public void testWithAttribute_storesAndRetrievesAttribute() {
        ObjectReader r = _reader.withAttribute("customKey", "customVal");
        ContextAttributes attrs = r.getAttributes();
        assertNotNull(attrs);
        assertEquals("customVal", attrs.getAttribute("customKey"));

        ObjectReader withoutAttr = r.withoutAttribute("customKey");
        assertNull(withoutAttr.getAttributes().getAttribute("customKey"));
    }

    // Tests FAIL_ON_TRAILING_TOKENS throwing exception
    @Test(expected = JsonMappingException.class)
    public void testVerifyNoTrailingTokens_trailingTokensPresent_throwsException() throws IOException {
        String json = "{\"id\":1} 123";
        _reader.forType(Pojo.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .readValue(json);
    }

    // Tests version accessor
    @Test
    public void testVersion_returnsNonNullVersion() {
        Version v = _reader.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // Tests root name unwrapping failure on mismatch
    @Test(expected = JsonMappingException.class)
    public void testUnwrapRoot_mismatchedRootName_throwsException() throws IOException {
        String json = "{\"WrongRoot\":{\"id\":1,\"name\":\"test\"}}";
        _reader.forType(Pojo.class)
                .withRootName("ExpectedRoot")
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(json);
    }

    // Tests root name unwrapping success
    @Test
    public void testUnwrapRoot_matchingRootName_success() throws IOException {
        String json = "{\"ExpectedRoot\":{\"id\":12,\"name\":\"wrapped\"}}";
        Pojo result = _reader.forType(Pojo.class)
                .withRootName("ExpectedRoot")
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(json);
        assertNotNull(result);
        assertEquals(12, result.id);
        assertEquals("wrapped", result.name);
    }

    // --- Added test cases for missing coverage ---

    @Test
    public void testReadValue_byteArrayOffsetLength() throws IOException {
        byte[] bytes = "prefix{\"id\":321,\"name\":\"offset\"}suffix".getBytes("UTF-8");
        int offset = 6;
        int len = "{\"id\":321,\"name\":\"offset\"}".getBytes("UTF-8").length;
        Pojo result = _reader.forType(Pojo.class).readValue(bytes, offset, len);
        assertNotNull(result);
        assertEquals(321, result.id);
        assertEquals("offset", result.name);
    }

    @Test
    public void testReadValue_fileAndUrlInput() throws IOException {
        File tempFile = File.createTempFile("jackson-test-", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("{\"id\":654,\"name\":\"file\"}".getBytes("UTF-8"));
        }

        Pojo fromFile = _reader.forType(Pojo.class).readValue(tempFile);
        assertNotNull(fromFile);
        assertEquals(654, fromFile.id);
        assertEquals("file", fromFile.name);

        URL url = tempFile.toURI().toURL();
        Pojo fromUrl = _reader.forType(Pojo.class).readValue(url);
        assertNotNull(fromUrl);
        assertEquals(654, fromUrl.id);
        assertEquals("file", fromUrl.name);
    }

    @Test
    public void testReadValue_dataInput() throws IOException {
        byte[] bytes = "{\"id\":987,\"name\":\"dataInput\"}".getBytes("UTF-8");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        Pojo result = _reader.forType(Pojo.class).readValue(dis);
        assertNotNull(result);
        assertEquals(987, result.id);
        assertEquals("dataInput", result.name);
    }

    @Test
    public void testReadValue_withJavaTypeAndClassOverloads() throws IOException {
        JavaType type = _reader.getTypeFactory().constructType(Pojo.class);
        JsonParser p = _reader.getFactory().createParser("{\"id\":5,\"name\":\"jt\"}");
        Pojo resultJT = _reader.readValue(p, type);
        assertEquals(5, resultJT.id);
        p.close();

        JsonParser p2 = _reader.getFactory().createParser("{\"id\":6,\"name\":\"cls\"}");
        Pojo resultCls = _reader.readValue(p2, Pojo.class);
        assertEquals(6, resultCls.id);
        p2.close();
    }

    @Test
    public void testReadTree_variousInputSources() throws IOException {
        byte[] bytes = "{\"k\":\"v1\"}".getBytes("UTF-8");
        JsonNode nodeFromBytes = _reader.readTree(bytes);
        assertEquals("v1", nodeFromBytes.get("k").asText());

        JsonNode nodeFromOffset = _reader.readTree(bytes, 0, bytes.length);
        assertEquals("v1", nodeFromOffset.get("k").asText());

        JsonNode nodeFromStream = _reader.readTree(new ByteArrayInputStream(bytes));
        assertEquals("v1", nodeFromStream.get("k").asText());

        JsonNode nodeFromReader = _reader.readTree(new StringReader("{\"k\":\"v2\"}"));
        assertEquals("v2", nodeFromReader.get("k").asText());

        JsonParser p = _reader.getFactory().createParser("{\"k\":\"v3\"}");
        JsonNode nodeFromParser = _reader.readTree(p);
        assertEquals("v3", nodeFromParser.get("k").asText());
        p.close();
    }

    @Test
    public void testReadValues_variousSources() throws IOException {
        String json = "{\"id\":1,\"name\":\"a\"}{\"id\":2,\"name\":\"b\"}";
        byte[] bytes = json.getBytes("UTF-8");

        MappingIterator<Pojo> itStream = _reader.forType(Pojo.class).readValues(new ByteArrayInputStream(bytes));
        List<Pojo> listStream = itStream.readAll();
        assertEquals(2, listStream.size());

        MappingIterator<Pojo> itReader = _reader.forType(Pojo.class).readValues(new StringReader(json));
        List<Pojo> listReader = itReader.readAll();
        assertEquals(2, listReader.size());

        MappingIterator<Pojo> itBytes = _reader.forType(Pojo.class).readValues(bytes);
        List<Pojo> listBytes = itBytes.readAll();
        assertEquals(2, listBytes.size());

        MappingIterator<Pojo> itBytesOffset = _reader.forType(Pojo.class).readValues(bytes, 0, bytes.length);
        List<Pojo> listBytesOffset = itBytesOffset.readAll();
        assertEquals(2, listBytesOffset.size());

        File tempFile = File.createTempFile("jackson-values-", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }

        MappingIterator<Pojo> itFile = _reader.forType(Pojo.class).readValues(tempFile);
        assertEquals(2, itFile.readAll().size());

        MappingIterator<Pojo> itUrl = _reader.forType(Pojo.class).readValues(tempFile.toURI().toURL());
        assertEquals(2, itUrl.readAll().size());

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        MappingIterator<Pojo> itDataInput = _reader.forType(Pojo.class).readValues(dis);
        assertEquals(2, itDataInput.readAll().size());
    }

    @Test
    public void testAt_stringExpression() throws IOException {
        String json = "{\"outer\":{\"inner\":{\"id\":99,\"name\":\"string-at\"}}}";
        Pojo result = _reader.forType(Pojo.class).at("/outer/inner").readValue(json);
        assertNotNull(result);
        assertEquals(99, result.id);
        assertEquals("string-at", result.name);
    }

    @Test
    public void testAt_missingSubPathReturnsMissingOrNull() throws IOException {
        String json = "{\"outer\":{}}";
        Pojo result = _reader.forType(Pojo.class).at("/outer/missing").readValue(json);
        assertNull(result);

        JsonNode missingNode = _reader.at("/outer/missing").readTree(json);
        assertNotNull(missingNode);
        assertTrue(missingNode.isMissingNode());
    }

    @Test
    public void testWithView_filtersFieldsProperly() throws IOException {
        String json = "{\"publicVal\":10,\"extendedVal\":20}";

        ObjectReader publicReader = _reader.forType(ViewPojo.class).withView(PublicView.class);
        ViewPojo publicResult = publicReader.readValue(json);
        assertEquals(10, publicResult.publicVal);
        assertEquals(0, publicResult.extendedVal);

        ObjectReader extendedReader = _reader.forType(ViewPojo.class).withView(ExtendedView.class);
        ViewPojo extendedResult = extendedReader.readValue(json);
        assertEquals(10, extendedResult.publicVal);
        assertEquals(20, extendedResult.extendedVal);
    }

    @Test
    public void testWithType_variousOverloads() {
        JavaType jt = _reader.getTypeFactory().constructType(Pojo.class);
        ObjectReader r1 = _reader.withType(Pojo.class);
        ObjectReader r2 = _reader.withType(jt);
        ObjectReader r3 = _reader.withType(new TypeReference<Pojo>() {});
        ObjectReader r4 = _reader.withType(Pojo.class.getGenericSuperclass());

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotNull(r4);
    }

    @Test
    public void testTreeToValue_withJavaType() throws Exception {
        JsonNode node = _mapper.readTree("{\"id\":33,\"name\":\"jtTree\"}");
        JavaType jt = _reader.getTypeFactory().constructType(Pojo.class);
        Pojo result = _reader.treeToValue(node, jt);
        assertNotNull(result);
        assertEquals(33, result.id);
        assertEquals("jtTree", result.name);

        assertNull(_reader.treeToValue(null, Pojo.class));
        assertNull(_reader.treeToValue(NullNode.getInstance(), Pojo.class));
    }

    @Test
    public void testWithAndWithout_parserAndDeserializationFeatures() {
        ObjectReader r = _reader.with(JsonParser.Feature.ALLOW_COMMENTS)
                .withFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        assertTrue(r.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertTrue(r.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS));
        assertFalse(r.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        r = r.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(r.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        r = r.without(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertFalse(r.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS));
    }

    @Test
    public void testWithConfig_andWithHandlerInstantiator() {
        DeserializationConfig cfg = _reader.getConfig();
        ObjectReader r = _reader.with(cfg);
        assertSame(cfg, r.getConfig());

        ObjectReader rHandler = _reader.with((com.fasterxml.jackson.databind.cfg.HandlerInstantiator) null);
        assertNotNull(rHandler);
    }

    @Test
    public void testWithNodeFactory_andTypeFactory_andSchema() {
        JsonNodeFactory nf = new JsonNodeFactory(true);
        TypeFactory tf = TypeFactory.defaultInstance();
        ObjectReader r = _reader.with(nf).with(tf).with((FormatSchema) null);
        assertNotNull(r);
        assertSame(tf, r.getTypeFactory());
    }

    @Test
    public void testWithDateFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        ObjectReader r = _reader.with(df);
        assertNotNull(r);
        assertEquals(df, r.getConfig().getDateFormat());
    }

    @Test
    public void testWithRootName_propertyName() throws IOException {
        PropertyName pn = new PropertyName("RootObj");
        ObjectReader r = _reader.forType(Pojo.class)
                .withRootName(pn)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE);
        Pojo result = r.readValue("{\"RootObj\":{\"id\":888,\"name\":\"rootProp\"}}");
        assertNotNull(result);
        assertEquals(888, result.id);
        assertEquals("rootProp", result.name);

        ObjectReader emptyRoot = _reader.withRootName((PropertyName) null);
        assertNotNull(emptyRoot);
    }

    @Test
    public void testWithAttributes_mapOverload() {
        Map<String, Object> map = Collections.singletonMap("k1", "v1");
        ObjectReader r = _reader.withAttributes(map);
        assertEquals("v1", r.getAttributes().getAttribute("k1"));

        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k2", "v2");
        r = r.with(attrs);
        assertEquals("v2", r.getAttributes().getAttribute("k2"));
    }

    @Test
    public void testInjectableValues() throws IOException {
        InjectableValues.Std iv = new InjectableValues.Std();
        iv.addValue("injectedKey", "injectedVal");
        ObjectReader r = _reader.with(iv);
        assertSame(iv, r.getInjectableValues());
    }

    @Test
    public void testCreateDeserializationContext() throws IOException {
        JsonParser p = _reader.getFactory().createParser("{}");
        DefaultDeserializationContext ctxt = _reader.createDeserializationContext(p);
        assertNotNull(ctxt);
        assertSame(p, ctxt.getParser());
        p.close();
    }
}
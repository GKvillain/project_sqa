package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.RootNameLookup;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectReaderTest {

    static class SimpleBean {
        public String name;
        public int age;
        public SimpleBean() {}
        public SimpleBean(String name, int age) { this.name = name; this.age = age; }
    }

    // Helper to create a basic ObjectReader
    private ObjectReader createReader() {
        return new ObjectMapper().reader();
    }

    private ObjectReader createReaderFor(Class<?> type) {
        return new ObjectMapper().readerFor(type);
    }

    // 1. Test constructor with array valueToUpdate throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_withArrayValueToUpdate_throwsIllegalArgumentException() {
        new ObjectMapper().readerFor(int[].class).withValueToUpdate(new int[]{1,2});
    }

    // 2. Test readValue with null JSON returns null (or deserializer null value)
    @Test
    public void testReadValue_nullJson_returnsNull() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        SimpleBean result = reader.readValue("null");
        assertNull(result);
    }

    // 3. Test readValue with empty array when valueToUpdate is set returns the update object
    @Test
    public void testReadValue_emptyArrayWithUpdate_returnsUpdateObject() throws Exception {
        SimpleBean update = new SimpleBean("update", 100);
        ObjectReader reader = createReaderFor(SimpleBean.class).withValueToUpdate(update);
        SimpleBean result = reader.readValue("[]");
        assertSame(update, result);
    }

    // 4. Test readValue with normal JSON
    @Test
    public void testReadValue_normalJson_deserializesCorrectly() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        SimpleBean result = reader.readValue("{\"name\":\"John\",\"age\":30}");
        assertEquals("John", result.name);
        assertEquals(30, result.age);
    }

    // 5. Test readValue with root wrapping enabled and correct root name
    @Test
    public void testReadValue_withRootWrapping_correctRootName_deserializes() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class)
                .withRootName("simpleBean");
        SimpleBean result = reader.readValue("{\"simpleBean\":{\"name\":\"Alice\",\"age\":25}}");
        assertEquals("Alice", result.name);
        assertEquals(25, result.age);
    }

    // 6. Test readValue with root wrapping enabled and wrong root name throws exception
    @Test(expected = JsonMappingException.class)
    public void testReadValue_withRootWrapping_wrongRootName_throwsException() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class)
                .withRootName("wrongName");
        reader.readValue("{\"simpleBean\":{\"name\":\"Alice\",\"age\":25}}");
    }

    // 7. Test _initForReading when parser has no current token and next token is null (end-of-input)
    @Test(expected = JsonMappingException.class)
    public void testInitForReading_noContent_throwsMappingException() throws Exception {
        ObjectReader reader = createReader();
        // Create a parser that reads empty string - will get null token on nextToken
        JsonParser p = reader.getFactory().createParser("");
        // p.getCurrentToken() is null, nextToken returns null => should throw
        reader._initForReading(p);
    }

    // 8. Test _findRootDeserializer with null valueType throws exception
    @Test(expected = JsonMappingException.class)
    public void testFindRootDeserializer_nullValueType_throwsMappingException() throws Exception {
        ObjectReader reader = createReader();
        // We need a DeserializationContext; create via readTree to get a context
        // Instead, use reflection? Simpler: call readValue on a parser with null valueType is not easy.
        // Actually we can create a context via createDeserializationContext.
        // But the method directly: _findRootDeserializer(ctxt, null). We need a non-null context.
        // Use a helper: create a JsonParser from a string, then createDeserializationContext
        JsonParser p = reader.getFactory().createParser("{}");
        DeserializationContext ctxt = reader.createDeserializationContext(p, reader.getConfig());
        // Now call protected method - note: it's accessible because same package
        reader._findRootDeserializer(ctxt, null);
    }

    // 9. Test withValueToUpdate with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithValueToUpdate_nullValue_throwsIllegalArgument() {
        createReader().withValueToUpdate(null);
    }

    // 10. Test forType with non-null type returns new reader with that type
    @Test
    public void testForType_givenType_returnsReaderWithThatType() throws Exception {
        ObjectReader base = createReader();
        ObjectReader typed = base.forType(SimpleBean.class);
        // Verify by reading
        SimpleBean bean = typed.readValue("{\"name\":\"Test\",\"age\":1}");
        assertNotNull(bean);
        assertEquals("Test", bean.name);
    }

    // 11. Test readValues with JSON array returns iterator
    @Test
    public void testReadValues_jsonArray_returnsIterator() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        MappingIterator<SimpleBean> iter = reader.readValues("[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]");
        assertTrue(iter.hasNext());
        SimpleBean first = iter.next();
        assertEquals("A", first.name);
        assertEquals(1, first.age);
        assertTrue(iter.hasNext());
        SimpleBean second = iter.next();
        assertEquals("B", second.name);
        assertEquals(2, second.age);
        assertFalse(iter.hasNext());
    }

    // 12. Test readTree from string returns JsonNode
    @Test
    public void testReadTree_stringJson_returnsJsonNode() throws Exception {
        ObjectReader reader = createReader();
        JsonNode node = reader.readTree("{\"key\":\"value\"}");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("value", node.get("key").asText());
    }

    // 13. Test readTree with empty JSON object returns NullNode? Actually empty object returns ObjectNode
    // We'll test a null token input: "null"
    @Test
    public void testReadTree_nullString_returnsNullNode() throws Exception {
        ObjectReader reader = createReader();
        JsonNode node = reader.readTree("null");
        assertTrue(node instanceof com.fasterxml.jackson.databind.node.NullNode);
    }

    // 14. Test with(JsonFactory) works
    @Test
    public void testWith_JsonFactory_returnsNewReader() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        JsonFactory newFactory = new JsonFactory();
        ObjectReader newReader = reader.with(newFactory);
        assertNotSame(reader, newReader);
        // Verify it can still read
        SimpleBean bean = newReader.readValue("{\"name\":\"X\",\"age\":0}");
        assertNotNull(bean);
    }

    // 15. Test with(FormatSchema) with incompatible schema throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWith_FormatSchema_incompatible_throwsIllegalArgument() {
        ObjectReader reader = createReader();
        // Create a dummy schema that is not supported by JsonFactory (which is JSON)
        FormatSchema schema = new FormatSchema() {
            @Override public String getSchemaType() { return "dummy"; }
        };
        reader.with(schema);
    }

    // 16. Test isEnabled methods
    @Test
    public void testIsEnabled_checksFeature() {
        ObjectReader reader = createReader();
        // Default is false for FAIL_ON_UNKNOWN_PROPERTIES? Actually default is true? We'll test something known.
        boolean original = reader.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        // Toggle via with
        ObjectReader modified = reader.without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        assertFalse(modified.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
    }

    // 17. Test readValue with InputStream source
    @Test
    public void testReadValue_inputStream_deserializes() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        ByteArrayInputStream bis = new ByteArrayInputStream("{\"name\":\"Stream\",\"age\":99}".getBytes("UTF-8"));
        SimpleBean bean = reader.readValue(bis);
        assertEquals("Stream", bean.name);
        assertEquals(99, bean.age);
    }

    // 18. Test readValue with byte array source
    @Test
    public void testReadValue_byteArray_deserializes() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        byte[] src = "{\"name\":\"Bytes\",\"age\":50}".getBytes("UTF-8");
        SimpleBean bean = reader.readValue(src);
        assertEquals("Bytes", bean.name);
        assertEquals(50, bean.age);
    }

    // 19. Test treeToValue conversion
    @Test
    public void testTreeToValue_validTree_returnsBean() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        JsonNode tree = reader.readTree("{\"name\":\"Tree\",\"age\":10}");
        SimpleBean bean = reader.treeToValue(tree, SimpleBean.class);
        assertEquals("Tree", bean.name);
        assertEquals(10, bean.age);
    }

    // 20. Test with(InjectableValues) returns new reader
    @Test
    public void testWith_InjectableValues_returnsNewReader() {
        ObjectReader reader = createReader();
        InjectableValues values = new InjectableValues.Std();
        ObjectReader newReader = reader.with(values);
        assertNotSame(reader, newReader);
    }

    // ========== New test cases added for uncovered parts ==========

    // 21. Test readValue with File source
    @Test
    public void testReadValue_fileSource_deserializes() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        File tempFile = File.createTempFile("jackson-test-", ".json");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("{\"name\":\"File\",\"age\":77}");
        }
        SimpleBean bean = reader.readValue(tempFile);
        assertEquals("File", bean.name);
        assertEquals(77, bean.age);
    }

    // 22. Test readValue with Reader source
    @Test
    public void testReadValue_readerSource_deserializes() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        StringReader sr = new StringReader("{\"name\":\"Reader\",\"age\":33}");
        SimpleBean bean = reader.readValue(sr);
        assertEquals("Reader", bean.name);
        assertEquals(33, bean.age);
    }

    // 23. Test readValues with InputStream source
    @Test
    public void testReadValues_inputStream_returnsIterator() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        String json = "[{\"name\":\"X\",\"age\":1},{\"name\":\"Y\",\"age\":2}]";
        ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes("UTF-8"));
        MappingIterator<SimpleBean> iter = reader.readValues(bis);
        assertTrue(iter.hasNext());
        SimpleBean first = iter.next();
        assertEquals("X", first.name);
        assertEquals(1, first.age);
        assertTrue(iter.hasNext());
        SimpleBean second = iter.next();
        assertEquals("Y", second.name);
        assertEquals(2, second.age);
        assertFalse(iter.hasNext());
    }

    // 24. Test readTree with InputStream source
    @Test
    public void testReadTree_inputStream_returnsJsonNode() throws Exception {
        ObjectReader reader = createReader();
        ByteArrayInputStream bis = new ByteArrayInputStream("{\"a\":123}".getBytes("UTF-8"));
        JsonNode node = reader.readTree(bis);
        assertTrue(node.isObject());
        assertEquals(123, node.get("a").asInt());
    }

    // 25. Test with(Attribute) returns new reader with attribute set
    @Test
    public void testWith_Attribute_returnsNewReader() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        ObjectReader newReader = reader.with(new Attribute("key", "value"));
        assertNotSame(reader, newReader);
        // Just ensure it can still read normally
        SimpleBean bean = newReader.readValue("{\"name\":\"Attr\",\"age\":1}");
        assertEquals("Attr", bean.name);
    }

    // 26. Test with(DateFormat) returns new reader with custom date format
    @Test
    public void testWith_DateFormat_returnsNewReader() throws Exception {
        // Use a bean with Date field for actual verification, but SimpleBean has no Date.
        // We'll just check that a new reader is returned and can read simple bean.
        ObjectReader reader = createReaderFor(SimpleBean.class);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        ObjectReader newReader = reader.with(df);
        assertNotSame(reader, newReader);
        SimpleBean bean = newReader.readValue("{\"name\":\"Date\",\"age\":5}");
        assertEquals("Date", bean.name);
        assertEquals(5, bean.age);
    }

    // 27. Test treeToValue with ArrayNode (converts to List)
    @Test
    public void testTreeToValue_arrayNode_returnsList() throws Exception {
        ObjectReader reader = createReader();
        JsonNode tree = reader.readTree("[1,2,3]");
        List<Integer> list = reader.treeToValue(tree, List.class);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    // 28. Test readValue with invalid JSON throws JsonParseException (or JsonMappingException)
    @Test(expected = JsonParseException.class)
    public void testReadValue_invalidJson_throwsException() throws Exception {
        ObjectReader reader = createReaderFor(SimpleBean.class);
        reader.readValue("{invalid}");
    }
}
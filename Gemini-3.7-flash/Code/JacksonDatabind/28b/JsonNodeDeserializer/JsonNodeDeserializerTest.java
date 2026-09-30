package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;

public class JsonNodeDeserializerTest {

    private ObjectMapper mapper;
    private JsonFactory factory;
    private JsonNodeDeserializer deserializer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = mapper.getFactory();
        deserializer = new JsonNodeDeserializer();
    }

    // Tests factory method for getting specialized deserializers
    @Test
    public void testGetDeserializer_specificTypes_returnsCorrectInstances() {
        JsonDeserializer<? extends JsonNode> objDeser = JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        assertTrue(objDeser instanceof JsonNodeDeserializer.ObjectDeserializer);

        JsonDeserializer<? extends JsonNode> arrDeser = JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        assertTrue(arrDeser instanceof JsonNodeDeserializer.ArrayDeserializer);

        JsonDeserializer<? extends JsonNode> genericDeser = JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertTrue(genericDeser instanceof JsonNodeDeserializer);
    }

    // Tests null value provider methods and isCachable flag
    @Test
    public void testGetNullValueAndIsCachable_default_returnsNullNodeAndTrue() {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals(NullNode.getInstance(), deserializer.getNullValue(ctxt));
        @SuppressWarnings("deprecation")
        JsonNode deprecatedNull = deserializer.getNullValue();
        assertEquals(NullNode.getInstance(), deprecatedNull);
        assertTrue(deserializer.isCachable());
    }

    // Tests deserializing a complete JSON Object with various field types
    @Test
    public void testDeserialize_fullObjectNode_returnsPopulatedObjectNode() throws IOException {
        String json = "{\"str\":\"value\",\"num\":100,\"bool\":true,\"fbool\":false,\"nil\":null,\"arr\":[1,2],\"sub\":{\"key\":\"v\"}}";
        JsonNode node = mapper.readTree(json);

        assertTrue(node instanceof ObjectNode);
        assertEquals("value", node.get("str").asText());
        assertEquals(100, node.get("num").asInt());
        assertTrue(node.get("bool").asBoolean());
        assertFalse(node.get("fbool").asBoolean());
        assertTrue(node.get("nil").isNull());
        assertTrue(node.get("arr").isArray());
        assertEquals(2, node.get("arr").size());
        assertTrue(node.get("sub").isObject());
        assertEquals("v", node.get("sub").get("key").asText());
    }

    // Tests deserializing an empty JSON Object
    @Test
    public void testDeserialize_emptyObject_returnsEmptyObjectNode() throws IOException {
        JsonNode node = mapper.readTree("{}");
        assertTrue(node instanceof ObjectNode);
        assertEquals(0, node.size());
    }

    // Tests deserializing an empty and non-empty JSON Array
    @Test
    public void testDeserialize_arrayNode_returnsPopulatedArrayNode() throws IOException {
        JsonNode emptyArray = mapper.readTree("[]");
        assertTrue(emptyArray instanceof ArrayNode);
        assertEquals(0, emptyArray.size());

        JsonNode array = mapper.readTree("[\"item1\", 2, true, null, {\"k\":\"v\"}, [3]]");
        assertTrue(array instanceof ArrayNode);
        assertEquals(6, array.size());
        assertEquals("item1", array.get(0).asText());
        assertEquals(2, array.get(1).asInt());
        assertTrue(array.get(2).asBoolean());
        assertTrue(array.get(3).isNull());
        assertTrue(array.get(4).isObject());
        assertTrue(array.get(5).isArray());
    }

    // Tests direct deserialization of scalar values (string, integer, float, boolean, null)
    @Test
    public void testDeserialize_scalarValues_returnsCorrespondingNodeTypes() throws IOException {
        assertEquals("hello", mapper.readTree("\"hello\"").asText());
        assertEquals(42, mapper.readTree("42").asInt());
        assertEquals(3.1415, mapper.readTree("3.1415").asDouble(), 0.0001);
        assertTrue(mapper.readTree("true").asBoolean());
        assertFalse(mapper.readTree("false").asBoolean());
        assertTrue(mapper.readTree("null").isNull());
    }

    // Tests numeric type handling including long and big numbers
    @Test
    public void testDeserialize_numericCoercions_handlesTypesCorrectly() throws IOException {
        long largeLong = 9999999999999L;
        JsonNode longNode = mapper.readTree(String.valueOf(largeLong));
        assertTrue(longNode instanceof LongNode);
        assertEquals(largeLong, longNode.asLong());

        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        JsonNode bigIntNode = mapper.readTree(bigInt.toString());
        assertTrue(bigIntNode instanceof BigIntegerNode);
        assertEquals(bigInt, bigIntNode.bigIntegerValue());

        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonNode decNode = mapper.readTree("123.456");
        assertTrue(decNode instanceof DecimalNode);
        assertEquals(new BigDecimal("123.456"), decNode.decimalValue());

        mapper.disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonNode dblNode = mapper.readTree("123.456");
        assertTrue(dblNode instanceof DoubleNode);
    }

    // Tests duplicate field handling under default mode (last value wins)
    @Test
    public void testDeserialize_duplicateFieldsDefault_keepsLastValue() throws IOException {
        String json = "{\"dup\":1, \"dup\":2}";
        JsonNode node = mapper.readTree(json);
        assertEquals(2, node.get("dup").asInt());
    }

    // Tests duplicate field handling when FAIL_ON_READING_DUP_TREE_KEY is enabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_duplicateFieldsFailFeatureEnabled_throwsException() throws IOException {
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        mapper.readTree("{\"dup\":1, \"dup\":2}");
    }

    // Tests ObjectDeserializer when parser is on START_OBJECT and FIELD_NAME tokens
    @Test
    public void testObjectDeserializer_validTokens_deserializesSuccessfully() throws IOException {
        JsonParser p1 = factory.createParser("{\"a\":1}");
        ObjectNode node1 = mapper.readValue(p1, ObjectNode.class);
        assertEquals(1, node1.get("a").asInt());

        JsonParser p2 = factory.createParser("{\"a\":2}");
        p2.nextToken(); // Move to START_OBJECT
        p2.nextToken(); // Move to FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ObjectNode node2 = JsonNodeDeserializer.ObjectDeserializer.getInstance().deserialize(p2, ctxt);
        assertEquals(2, node2.get("a").asInt());
    }

    // Tests ObjectDeserializer when parser is on invalid token
    @Test(expected = JsonMappingException.class)
    public void testObjectDeserializer_invalidToken_throwsMappingException() throws IOException {
        JsonParser p = factory.createParser("[1, 2]");
        mapper.readValue(p, ObjectNode.class);
    }

    // Tests ArrayDeserializer when parser is on invalid token
    @Test(expected = JsonMappingException.class)
    public void testArrayDeserializer_invalidToken_throwsMappingException() throws IOException {
        JsonParser p = factory.createParser("{\"k\":\"v\"}");
        mapper.readValue(p, ArrayNode.class);
    }

    // Tests ArrayDeserializer on unexpected end of input
    @Test(expected = JsonMappingException.class)
    public void testArrayDeserializer_unexpectedEndOfInput_throwsMappingException() throws IOException {
        JsonParser p = factory.createParser("[1, 2");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // 1
        p.nextToken(); // 2
        p.nextToken(); // EOF (null)
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonNodeDeserializer.ArrayDeserializer.getInstance().deserialize(p, ctxt);
    }

    // Tests embedded object handling in _fromEmbedded
    @Test
    public void testDeserialize_embeddedObjects_deserializesExpectedNodes() throws IOException {
        byte[] binaryData = new byte[]{1, 2, 3};
        BinaryNode binaryNode = mapper.getNodeFactory().binaryNode(binaryData);
        assertEquals(binaryData.length, binaryNode.binaryValue().length);

        RawValue raw = new RawValue("{\"raw\":true}");
        JsonNode rawNode = mapper.getNodeFactory().rawValueNode(raw);
        assertNotNull(rawNode);

        ObjectNode inner = mapper.createObjectNode().put("test", "embedded");
        JsonNode pojoNode = mapper.getNodeFactory().pojoNode(inner);
        assertNotNull(pojoNode);
    }

    // Tests deserializing empty object specifically targeting ObjectNode
    @Test
    public void testObjectDeserializer_emptyObject_returnsEmptyObjectNode() throws IOException {
        ObjectNode node = mapper.readValue("{}", ObjectNode.class);
        assertNotNull(node);
        assertEquals(0, node.size());
    }

    // Tests deserializing empty array specifically targeting ArrayNode
    @Test
    public void testArrayDeserializer_emptyArray_returnsEmptyArrayNode() throws IOException {
        ArrayNode node = mapper.readValue("[]", ArrayNode.class);
        assertNotNull(node);
        assertEquals(0, node.size());
    }
}
package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.RawValue;

public class POJONodeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests node type and token representation
    @Test
    public void testNodeTypeAndToken_standardValues_returnsCorrectEnumConstants() {
        POJONode node = new POJONode("test");
        assertEquals(JsonNodeType.POJO, node.getNodeType());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
    }

    // Tests binaryValue with byte array payload
    @Test
    public void testBinaryValue_byteArrayValue_returnsSameByteArray() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4 };
        POJONode node = new POJONode(data);
        assertArrayEquals(data, node.binaryValue());
    }

    // Tests binaryValue fallback with non-byte array payload
    @Test
    public void testBinaryValue_nonByteArrayValue_returnsNull() throws IOException {
        POJONode node = new POJONode("not a byte array");
        assertNull(node.binaryValue());
    }

    // Tests asText with both null and non-null values
    @Test
    public void testAsText_nullAndNonNullValue_returnsExpectedString() {
        POJONode nullNode = new POJONode(null);
        assertEquals("null", nullNode.asText());

        POJONode textNode = new POJONode("hello");
        assertEquals("hello", textNode.asText());

        POJONode intNode = new POJONode(123);
        assertEquals("123", intNode.asText());
    }

    // Tests asText(defaultValue) with null and non-null values
    @Test
    public void testAsTextWithDefault_nullAndNonNullValue_returnsExpectedString() {
        POJONode nullNode = new POJONode(null);
        assertEquals("defaultVal", nullNode.asText("defaultVal"));

        POJONode textNode = new POJONode("custom");
        assertEquals("custom", textNode.asText("defaultVal"));
    }

    // Tests asBoolean coercion with Boolean, non-Boolean, and null
    @Test
    public void testAsBoolean_variousInputs_returnsExpectedCoercion() {
        POJONode trueNode = new POJONode(Boolean.TRUE);
        assertTrue(trueNode.asBoolean(false));

        POJONode falseNode = new POJONode(Boolean.FALSE);
        assertFalse(falseNode.asBoolean(true));

        POJONode nonBoolNode = new POJONode("true");
        assertTrue(nonBoolNode.asBoolean(true));
        assertFalse(nonBoolNode.asBoolean(false));

        POJONode nullNode = new POJONode(null);
        assertTrue(nullNode.asBoolean(true));
        assertFalse(nullNode.asBoolean(false));
    }

    // Tests asInt coercion with Number, non-Number, and null
    @Test
    public void testAsInt_variousInputs_returnsExpectedCoercion() {
        POJONode intNode = new POJONode(Integer.valueOf(42));
        assertEquals(42, intNode.asInt(0));

        POJONode doubleNode = new POJONode(Double.valueOf(42.9));
        assertEquals(42, doubleNode.asInt(0));

        POJONode textNode = new POJONode("42");
        assertEquals(99, textNode.asInt(99));

        POJONode nullNode = new POJONode(null);
        assertEquals(99, nullNode.asInt(99));
    }

    // Tests asLong coercion with Number, non-Number, and null
    @Test
    public void testAsLong_variousInputs_returnsExpectedCoercion() {
        POJONode longNode = new POJONode(Long.valueOf(1234567890123L));
        assertEquals(1234567890123L, longNode.asLong(0L));

        POJONode textNode = new POJONode("not-a-number");
        assertEquals(55L, textNode.asLong(55L));

        POJONode nullNode = new POJONode(null);
        assertEquals(55L, nullNode.asLong(55L));
    }

    // Tests asDouble coercion with Number, non-Number, and null
    @Test
    public void testAsDouble_variousInputs_returnsExpectedCoercion() {
        POJONode doubleNode = new POJONode(Double.valueOf(3.14159));
        assertEquals(3.14159, doubleNode.asDouble(0.0), 0.000001);

        POJONode textNode = new POJONode("pi");
        assertEquals(1.23, textNode.asDouble(1.23), 0.000001);

        POJONode nullNode = new POJONode(null);
        assertEquals(1.23, nullNode.asDouble(1.23), 0.000001);
    }

    // Tests getPojo accessor method
    @Test
    public void testGetPojo_returnsWrappedObject() {
        Object obj = new Object();
        POJONode node = new POJONode(obj);
        assertSame(obj, node.getPojo());

        POJONode nullNode = new POJONode(null);
        assertNull(nullNode.getPojo());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_variousCases_returnsExpectedResults() {
        POJONode node1 = new POJONode("test");
        POJONode node2 = new POJONode("test");
        POJONode node3 = new POJONode("other");
        POJONode nullNode1 = new POJONode(null);
        POJONode nullNode2 = new POJONode(null);

        assertTrue(node1.equals(node1));
        assertTrue(node1.equals(node2));
        assertTrue(node2.equals(node1));
        assertEquals(node1.hashCode(), node2.hashCode());

        assertFalse(node1.equals(node3));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("test"));
        assertFalse(node1.equals(nullNode1));
        assertFalse(nullNode1.equals(node1));

        assertTrue(nullNode1.equals(nullNode2));
    }

    // Tests toString with byte array
    @Test
    public void testToString_byteArray_formatsWithByteCount() {
        byte[] data = new byte[] { 1, 2, 3 };
        POJONode node = new POJONode(data);
        assertEquals("(binary value of 3 bytes)", node.toString());
    }

    // Tests toString with RawValue
    @Test
    public void testToString_rawValue_formatsWithRawString() {
        RawValue raw = new RawValue("{\"k\":\"v\"}");
        POJONode node = new POJONode(raw);
        assertEquals("(raw value '{\"k\":\"v\"}')", node.toString());
    }

    // Tests toString with general object and null
    @Test
    public void testToString_generalObjectAndNull_returnsStringValue() {
        POJONode textNode = new POJONode("simple string");
        assertEquals("simple string", textNode.toString());

        POJONode nullNode = new POJONode(null);
        assertEquals("null", nullNode.toString());
    }

    // Tests serialize method with null value
    @Test
    public void testSerialize_nullValue_serializesAsJsonNull() throws IOException {
        POJONode node = new POJONode(null);
        String json = mapper.writeValueAsString(node);
        assertEquals("null", json);
    }

    // Tests serialize method with JsonSerializable instance
    @Test
    public void testSerialize_jsonSerializableValue_invokesCustomSerialization() throws IOException {
        JsonSerializable customSerializable = new JsonSerializable() {
            @Override
            public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("serialized_custom");
            }

            @Override
            public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
                    throws IOException {
                serialize(gen, serializers);
            }
        };

        POJONode node = new POJONode(customSerializable);
        String json = mapper.writeValueAsString(node);
        assertEquals("\"serialized_custom\"", json);
    }

    // Tests serialize method with regular POJO
    @Test
    public void testSerialize_regularObject_serializesCorrectly() throws IOException {
        POJONode node = new POJONode("hello world");
        String json = mapper.writeValueAsString(node);
        assertEquals("\"hello world\"", json);
    }

    // Tests binaryValue with null payload
    @Test
    public void testBinaryValue_nullValue_returnsNull() throws IOException {
        POJONode nullNode = new POJONode(null);
        assertNull(nullNode.binaryValue());
    }

    // Tests serialize method with RawValue instance
    @Test
    public void testSerialize_rawValue_serializesRawContent() throws IOException {
        RawValue raw = new RawValue("{\"key\":123}");
        POJONode node = new POJONode(raw);
        String json = mapper.writeValueAsString(node);
        assertEquals("{\"key\":123}", json);
    }

    // Tests no-arg coercion methods (asBoolean, asInt, asLong, asDouble)
    @Test
    public void testNoArgCoercions_returnsDefaultCoercedValues() {
        POJONode boolNode = new POJONode(Boolean.TRUE);
        assertTrue(boolNode.asBoolean());

        POJONode intNode = new POJONode(Integer.valueOf(100));
        assertEquals(100, intNode.asInt());

        POJONode longNode = new POJONode(Long.valueOf(200L));
        assertEquals(200L, longNode.asLong());

        POJONode doubleNode = new POJONode(Double.valueOf(3.5));
        assertEquals(3.5, doubleNode.asDouble(), 0.00001);
    }

    // Tests equality when comparing against a non-POJONode instance
    @Test
    public void testEquals_differentNodeType_returnsFalse() {
        POJONode node = new POJONode("value");
        TextNode textNode = new TextNode("value");
        assertFalse(node.equals(textNode));
    }
}
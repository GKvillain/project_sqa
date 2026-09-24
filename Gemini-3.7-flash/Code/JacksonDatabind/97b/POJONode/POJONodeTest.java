package com.fasterxml.jackson.databind.node;

import java.io.IOException;

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

    // Tests getNodeType returns JsonNodeType.POJO
    @Test
    public void testGetNodeType_always_returnsPojo() {
        POJONode node = new POJONode("test");
        assertEquals(JsonNodeType.POJO, node.getNodeType());
    }

    // Tests asToken returns JsonToken.VALUE_EMBEDDED_OBJECT
    @Test
    public void testAsToken_always_returnsValueEmbeddedObject() {
        POJONode node = new POJONode("test");
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
    }

    // Tests binaryValue when wrapped value is byte array
    @Test
    public void testBinaryValue_withByteArray_returnsByteArray() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        POJONode node = new POJONode(data);
        assertArrayEquals(data, node.binaryValue());
    }

    // Tests binaryValue when wrapped value is not byte array
    @Test
    public void testBinaryValue_withNonByteArray_returnsNull() throws IOException {
        POJONode node = new POJONode("not-bytes");
        assertNull(node.binaryValue());
    }

    // Tests binaryValue when wrapped value is RawValue containing byte array
    @Test
    public void testBinaryValue_withRawValueByteArray_returnsByteArray() throws IOException {
        byte[] data = new byte[]{4, 5, 6};
        POJONode node = new POJONode(new RawValue(data));
        assertArrayEquals(data, node.binaryValue());
    }

    // Tests binaryValue when wrapped value is RawValue containing non-byte array
    @Test
    public void testBinaryValue_withRawValueNonByteArray_returnsNull() throws IOException {
        POJONode node = new POJONode(new RawValue("non-bytes"));
        assertNull(node.binaryValue());
    }

    // Tests asText when wrapped value is null
    @Test
    public void testAsText_nullValue_returnsNullString() {
        POJONode node = new POJONode(null);
        assertEquals("null", node.asText());
    }

    // Tests asText when wrapped value is non-null
    @Test
    public void testAsText_nonNullValue_returnsToString() {
        POJONode node = new POJONode(12345);
        assertEquals("12345", node.asText());
    }

    // Tests asText(defaultValue) when wrapped value is null
    @Test
    public void testAsTextWithDefault_nullValue_returnsDefault() {
        POJONode node = new POJONode(null);
        assertEquals("fallback", node.asText("fallback"));
    }

    // Tests asText(defaultValue) when wrapped value is non-null
    @Test
    public void testAsTextWithDefault_nonNullValue_returnsToString() {
        POJONode node = new POJONode("hello");
        assertEquals("hello", node.asText("fallback"));
    }

    // Tests asBoolean when wrapped value is Boolean
    @Test
    public void testAsBoolean_booleanValue_returnsBoolean() {
        POJONode trueNode = new POJONode(Boolean.TRUE);
        assertTrue(trueNode.asBoolean(false));

        POJONode falseNode = new POJONode(Boolean.FALSE);
        assertFalse(falseNode.asBoolean(true));
    }

    // Tests asBoolean when wrapped value is not Boolean or is null
    @Test
    public void testAsBoolean_nonBooleanValue_returnsDefault() {
        POJONode stringNode = new POJONode("true");
        assertFalse(stringNode.asBoolean(false));
        assertTrue(stringNode.asBoolean(true));

        POJONode nullNode = new POJONode(null);
        assertTrue(nullNode.asBoolean(true));
    }

    // Tests asInt when wrapped value is Number
    @Test
    public void testAsInt_numberValue_returnsIntValue() {
        POJONode node = new POJONode(Integer.valueOf(42));
        assertEquals(42, node.asInt(0));

        POJONode doubleNode = new POJONode(Double.valueOf(42.9));
        assertEquals(42, doubleNode.asInt(0));
    }

    // Tests asInt when wrapped value is not Number
    @Test
    public void testAsInt_nonNumberValue_returnsDefault() {
        POJONode node = new POJONode("42");
        assertEquals(99, node.asInt(99));

        POJONode nullNode = new POJONode(null);
        assertEquals(99, nullNode.asInt(99));
    }

    // Tests asLong when wrapped value is Number
    @Test
    public void testAsLong_numberValue_returnsLongValue() {
        POJONode node = new POJONode(Long.valueOf(1234567890123L));
        assertEquals(1234567890123L, node.asLong(0L));
    }

    // Tests asLong when wrapped value is not Number
    @Test
    public void testAsLong_nonNumberValue_returnsDefault() {
        POJONode node = new POJONode("123");
        assertEquals(999L, node.asLong(999L));
    }

    // Tests asDouble when wrapped value is Number
    @Test
    public void testAsDouble_numberValue_returnsDoubleValue() {
        POJONode node = new POJONode(Double.valueOf(3.14159));
        assertEquals(3.14159, node.asDouble(0.0), 0.00001);
    }

    // Tests asDouble when wrapped value is not Number
    @Test
    public void testAsDouble_nonNumberValue_returnsDefault() {
        POJONode node = new POJONode("3.14");
        assertEquals(1.23, node.asDouble(1.23), 0.00001);
    }

    // Tests getPojo returns wrapped instance
    @Test
    public void testGetPojo_returnsWrappedValue() {
        Object obj = new Object();
        POJONode node = new POJONode(obj);
        assertSame(obj, node.getPojo());
    }

    // Tests equals and hashCode across identical, equal, different, and null values
    @Test
    public void testEqualsAndHashCode_variousScenarios_returnsExpected() {
        POJONode node1 = new POJONode("value");
        POJONode node2 = new POJONode("value");
        POJONode node3 = new POJONode("other");
        POJONode nullNode1 = new POJONode(null);
        POJONode nullNode2 = new POJONode(null);

        assertTrue(node1.equals(node1));
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());

        assertFalse(node1.equals(node3));
        assertFalse(node1.equals(nullNode1));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("value"));

        assertTrue(nullNode1.equals(nullNode2));
        assertFalse(nullNode1.equals(node1));
    }

    // Tests toString with byte array
    @Test
    public void testToString_byteArray_formatsCorrectly() {
        POJONode node = new POJONode(new byte[5]);
        assertEquals("(binary value of 5 bytes)", node.toString());
    }

    // Tests toString with RawValue
    @Test
    public void testToString_rawValue_formatsCorrectly() {
        POJONode node = new POJONode(new RawValue("{\"k\":\"v\"}"));
        assertEquals("(raw value '{\"k\":\"v\"}')", node.toString());
    }

    // Tests toString with standard object and null
    @Test
    public void testToString_standardObject_returnsStringValueOf() {
        POJONode node = new POJONode("hello");
        assertEquals("hello", node.toString());

        POJONode nullNode = new POJONode(null);
        assertEquals("null", nullNode.toString());
    }

    // Tests serialize with null value
    @Test
    public void testSerialize_nullValue_serializesNull() throws IOException {
        POJONode node = new POJONode(null);
        String json = mapper.writeValueAsString(node);
        assertEquals("null", json);
    }

    // Tests serialize with JsonSerializable instance
    @Test
    public void testSerialize_jsonSerializable_callsSerialize() throws IOException {
        JsonSerializable customSerializable = new JsonSerializable() {
            @Override
            public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("custom-serialized");
            }

            @Override
            public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
                serialize(gen, serializers);
            }
        };

        POJONode node = new POJONode(customSerializable);
        String json = mapper.writeValueAsString(node);
        assertEquals("\"custom-serialized\"", json);
    }

    // Tests serialize with RawValue
    @Test
    public void testSerialize_rawValue_serializesRawContent() throws IOException {
        POJONode node = new POJONode(new RawValue("{\"key\":123}"));
        String json = mapper.writeValueAsString(node);
        assertEquals("{\"key\":123}", json);
    }

    // Tests serialize with standard object
    @Test
    public void testSerialize_standardObject_serializesCorrectly() throws IOException {
        POJONode node = new POJONode("test-string");
        String json = mapper.writeValueAsString(node);
        assertEquals("\"test-string\"", json);
    }
}
package com.fasterxml.jackson.databind.node;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;

public class POJONodeTest {

    // Test getNodeType returns POJO type
    @Test
    public void testGetNodeType_always_returnsPOJO() {
        POJONode node = new POJONode("test");
        assertEquals(JsonNodeType.POJO, node.getNodeType());
    }

    // Test asToken returns VALUE_EMBEDDED_OBJECT
    @Test
    public void testAsToken_always_returnsEmbeddedObject() {
        POJONode node = new POJONode("test");
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
    }

    // Test binaryValue when _value is byte[]
    @Test
    public void testBinaryValue_byteArrayValue_returnsByteArray() throws IOException {
        byte[] bytes = new byte[]{1, 2, 3};
        POJONode node = new POJONode(bytes);
        assertArrayEquals(bytes, node.binaryValue());
    }

    // Test binaryValue when _value is not byte[]
    @Test
    public void testBinaryValue_nonByteArrayValue_returnsNull() throws IOException {
        POJONode node = new POJONode("test");
        assertNull(node.binaryValue());
    }

    // Test asText with null _value returns "null"
    @Test
    public void testAsText_nullValue_returnsNullString() {
        POJONode node = new POJONode(null);
        assertEquals("null", node.asText());
    }

    // Test asText with non-null _value returns toString()
    @Test
    public void testAsText_nonNullValue_returnsToString() {
        POJONode node = new POJONode("hello");
        assertEquals("hello", node.asText());
    }

    // Test asText(String) with null _value returns defaultValue
    @Test
    public void testAsTextWithDefault_nullValue_returnsDefault() {
        POJONode node = new POJONode(null);
        assertEquals("default", node.asText("default"));
    }

    // Test asText(String) with non-null _value returns toString()
    @Test
    public void testAsTextWithDefault_nonNullValue_returnsToString() {
        POJONode node = new POJONode("hello");
        assertEquals("hello", node.asText("default"));
    }

    // Test asBoolean with null _value returns defaultValue
    @Test
    public void testAsBoolean_nullValue_returnsDefault() {
        POJONode node = new POJONode(null);
        assertFalse(node.asBoolean(true));
    }

    // Test asBoolean with Boolean _value returns booleanValue
    @Test
    public void testAsBoolean_booleanValue_returnsBoolean() {
        POJONode node = new POJONode(true);
        assertTrue(node.asBoolean(false));
    }

    // Test asBoolean with non-Boolean _value returns defaultValue
    @Test
    public void testAsBoolean_nonBooleanValue_returnsDefault() {
        POJONode node = new POJONode("test");
        assertFalse(node.asBoolean(false));
    }

    // Test asInt with Number _value returns intValue
    @Test
    public void testAsInt_numberValue_returnsInt() {
        POJONode node = new POJONode(42);
        assertEquals(42, node.asInt(0));
    }

    // Test asInt with non-Number _value returns defaultValue
    @Test
    public void testAsInt_nonNumberValue_returnsDefault() {
        POJONode node = new POJONode("test");
        assertEquals(10, node.asInt(10));
    }

    // Test asLong with Number _value returns longValue
    @Test
    public void testAsLong_numberValue_returnsLong() {
        POJONode node = new POJONode(100L);
        assertEquals(100L, node.asLong(0L));
    }

    // Test asDouble with Number _value returns doubleValue
    @Test
    public void testAsDouble_numberValue_returnsDouble() {
        POJONode node = new POJONode(3.14);
        assertEquals(3.14, node.asDouble(0.0), 0.001);
    }

    // Test getPojo returns _value
    @Test
    public void testGetPojo_anyValue_returnsValue() {
        Object obj = new Object();
        POJONode node = new POJONode(obj);
        assertSame(obj, node.getPojo());
    }

    // Test equals with same reference returns true
    @Test
    public void testEquals_sameReference_returnsTrue() {
        POJONode node = new POJONode("test");
        assertTrue(node.equals(node));
    }

    // Test equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        POJONode node = new POJONode("test");
        assertFalse(node.equals(null));
    }

    // Test equals with different type returns false
    @Test
    public void testEquals_differentType_returnsFalse() {
        POJONode node = new POJONode("test");
        assertFalse(node.equals("string"));
    }

    // Test equals with both null values returns true
    @Test
    public void testEquals_bothNullValues_returnsTrue() {
        POJONode node1 = new POJONode(null);
        POJONode node2 = new POJONode(null);
        assertTrue(node1.equals(node2));
    }

    // Test equals with same non-null values returns true
    @Test
    public void testEquals_sameNonNullValues_returnsTrue() {
        POJONode node1 = new POJONode("test");
        POJONode node2 = new POJONode("test");
        assertTrue(node1.equals(node2));
    }

    // Test equals with different values returns false
    @Test
    public void testEquals_differentValues_returnsFalse() {
        POJONode node1 = new POJONode("test1");
        POJONode node2 = new POJONode("test2");
        assertFalse(node1.equals(node2));
    }

    // Test toString with byte[] value
    @Test
    public void testToString_byteArrayValue_returnsFormattedString() {
        byte[] bytes = new byte[]{1, 2, 3};
        POJONode node = new POJONode(bytes);
        assertEquals("(binary value of 3 bytes)", node.toString());
    }

    // Test toString with null value returns empty string
    @Test
    public void testToString_nullValue_returnsEmpty() {
        POJONode node = new POJONode(null);
        assertEquals("null", node.toString());
    }
}
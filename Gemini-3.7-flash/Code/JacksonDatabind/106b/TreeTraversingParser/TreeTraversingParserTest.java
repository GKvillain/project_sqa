package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import static org.junit.Assert.*;

public class TreeTraversingParserTest {

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    // Tests traversing an object node and verifying token stream
    @Test
    public void testNextToken_objectStructure_traversesCorrectly() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.put("field1", "val1");
        root.put("field2", 100);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertNull(parser.currentToken());
        assertFalse(parser.isClosed());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field1", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val1", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field2", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
        parser.close();
    }

    // Tests traversing an empty and non-empty array
    @Test
    public void testNextToken_arrayStructure_traversesCorrectly() throws IOException {
        ArrayNode root = nodeFactory.arrayNode();
        root.add(true);
        root.addNull();

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests skipping children of an object node
    @Test
    public void testSkipChildren_insideObject_skipsToEndObject() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        ObjectNode child = root.putObject("nested");
        child.put("inner", "value");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.currentToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests skipping children of an array node
    @Test
    public void testSkipChildren_insideArray_skipsToEndArray() throws IOException {
        ArrayNode root = nodeFactory.arrayNode();
        ArrayNode inner = root.addArray();
        inner.add(1);
        inner.add(2);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        parser.skipChildren();
        assertEquals(JsonToken.END_ARRAY, parser.currentToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests integer and long numeric accessors
    @Test
    public void testGetNumericValues_intAndLongNodes_returnsCorrectNumbers() throws IOException {
        JsonNode intNode = nodeFactory.numberNode(42);
        TreeTraversingParser parser = new TreeTraversingParser(intNode);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0, parser.getDoubleValue(), 0.0001);
        assertEquals(42.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(Integer.valueOf(42), parser.getNumberValue());
        parser.close();
    }

    // Tests BigInteger and BigDecimal numeric accessors
    @Test
    public void testGetNumericValues_bigIntegerAndDecimal_returnsCorrectNumbers() throws IOException {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        BigDecimal bigDec = new BigDecimal("12345.67890");

        TreeTraversingParser parser1 = new TreeTraversingParser(nodeFactory.numberNode(bigInt));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser1.nextToken());
        assertEquals(bigInt, parser1.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser1.getNumberType());
        parser1.close();

        TreeTraversingParser parser2 = new TreeTraversingParser(nodeFactory.numberNode(bigDec));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser2.nextToken());
        assertEquals(bigDec, parser2.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser2.getNumberType());
        parser2.close();
    }

    // Tests Double numeric accessors and isNaN check
    @Test
    public void testGetNumericValues_doubleNode_returnsCorrectValues() throws IOException {
        JsonNode doubleNode = nodeFactory.numberNode(Double.NaN);
        TreeTraversingParser parser = new TreeTraversingParser(doubleNode);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(parser.isNaN());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        parser.close();
    }

    // Tests exception path when non-numeric node accesses int value
    @Test(expected = JsonParseException.class)
    public void testGetIntValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getIntValue();
    }

    // Tests exception path when non-numeric node accesses big integer value
    @Test(expected = JsonParseException.class)
    public void testGetBigIntegerValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getBigIntegerValue();
    }

    // Tests exception path when non-numeric node accesses long value
    @Test(expected = JsonParseException.class)
    public void testGetLongValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getLongValue();
    }

    // Tests exception path when non-numeric node accesses float value
    @Test(expected = JsonParseException.class)
    public void testGetFloatValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getFloatValue();
    }

    // Tests exception path when non-numeric node accesses double value
    @Test(expected = JsonParseException.class)
    public void testGetDoubleValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getDoubleValue();
    }

    // Tests exception path when non-numeric node accesses decimal value
    @Test(expected = JsonParseException.class)
    public void testGetDecimalValue_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = nodeFactory.textNode("hello");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getDecimalValue();
    }

    // Tests textual representation and char buffer accessors
    @Test
    public void testGetText_textAndNumberTokens_returnsCorrectText() throws IOException {
        TextNode textNode = nodeFactory.textNode("test string");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test string", parser.getText());
        assertEquals(11, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertArrayEquals("test string".toCharArray(), parser.getTextCharacters());
        assertFalse(parser.hasTextCharacters());
        parser.close();
        assertNull(parser.getText());
    }

    // Tests binary value extraction from BinaryNode and TextNode (base64)
    @Test
    public void testGetBinaryValue_binaryAndTextNodes_returnsBytes() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        BinaryNode binaryNode = nodeFactory.binaryNode(data);
        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, parser.getBinaryValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(5, written);
        assertArrayEquals(data, out.toByteArray());
        parser.close();

        // TextNode containing Base64
        TextNode textNode = nodeFactory.textNode(Base64Variants.MIME.encode(data));
        TreeTraversingParser parser2 = new TreeTraversingParser(textNode);
        assertEquals(JsonToken.VALUE_STRING, parser2.nextToken());
        assertArrayEquals(data, parser2.getBinaryValue(Base64Variants.MIME));
        parser2.close();
    }

    // Tests embedded object retrieval for POJONode and BinaryNode
    @Test
    public void testGetEmbeddedObject_pojoAndBinaryNodes_returnsEmbeddedObjects() throws IOException {
        Object pojo = new Object();
        POJONode pojoNode = nodeFactory.pojoNode(pojo);
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(pojo, parser.getEmbeddedObject());
        parser.close();

        assertNull(parser.getEmbeddedObject());
    }

    // Tests overrideCurrentName functionality on ObjectCursor
    @Test
    public void testOverrideCurrentName_inObject_overridesNameSuccessfully() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.put("originalName", "val");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("originalName", parser.getCurrentName());

        parser.overrideCurrentName("overriddenName");
        assertEquals("overriddenName", parser.getCurrentName());
        parser.close();
    }

    // Tests codec, version, and parsing context metadata accessors
    @Test
    public void testContextAndCodec_metadataAccessors_returnsExpected() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NullNode nullNode = nodeFactory.nullNode();
        TreeTraversingParser parser = new TreeTraversingParser(nullNode, mapper);

        assertSame(mapper, parser.getCodec());
        parser.setCodec(null);
        assertNull(parser.getCodec());
        assertNotNull(parser.version());
        assertNotNull(parser.getParsingContext());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        parser.close();
    }

    // Tests closing the parser directly and checking state
    @Test
    public void testClose_explicitCall_closesParserAndClearsCursor() throws IOException {
        TextNode textNode = nodeFactory.textNode("test");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextToken());
        assertNull(parser.getCurrentName());
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.isNaN());
    }

    // Tests numeric types float, short, and long node accessors
    @Test
    public void testGetNumericValues_shortAndFloatNodes_returnsCorrectNumbers() throws IOException {
        JsonNode shortNode = nodeFactory.numberNode((short) 123);
        TreeTraversingParser parser1 = new TreeTraversingParser(shortNode);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser1.nextToken());
        assertEquals(JsonParser.NumberType.INT, parser1.getNumberType());
        assertEquals((short) 123, parser1.getShortValue());
        assertEquals((byte) 123, parser1.getByteValue());
        parser1.close();

        JsonNode floatNode = nodeFactory.numberNode(12.34f);
        TreeTraversingParser parser2 = new TreeTraversingParser(floatNode);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser2.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, parser2.getNumberType());
        assertEquals(12.34f, parser2.getFloatValue(), 0.001f);
        parser2.close();

        JsonNode longNode = nodeFactory.numberNode(9876543210L);
        TreeTraversingParser parser3 = new TreeTraversingParser(longNode);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser3.nextToken());
        assertEquals(JsonParser.NumberType.LONG, parser3.getNumberType());
        assertEquals(9876543210L, parser3.getLongValue());
        parser3.close();
    }

    // Tests boolean tokens and values
    @Test
    public void testBooleanNodes_returnsExpectedTokensAndValues() throws IOException {
        BooleanNode falseNode = BooleanNode.FALSE;
        TreeTraversingParser parser = new TreeTraversingParser(falseNode);
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(Boolean.FALSE.toString(), parser.getText());
        parser.close();
    }

    // Tests null root node construction
    @Test
    public void testNullRootNode_traversesNullNode() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(null);
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
        parser.close();
    }

    // Tests MissingNode handling
    @Test
    public void testMissingNode_traversesMissingNode() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MissingNode.getInstance());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests POJONode binary conversion fallback
    @Test
    public void testGetBinaryValue_pojoNodeContainingBytes_returnsBytes() throws IOException {
        byte[] raw = new byte[]{10, 20, 30};
        POJONode pojoNode = nodeFactory.pojoNode(raw);
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(raw, parser.getBinaryValue());
        parser.close();
    }

    // Tests empty ObjectNode and ArrayNode traversal
    @Test
    public void testEmptyContainers_traversesStartAndEnd() throws IOException {
        ObjectNode emptyObj = nodeFactory.objectNode();
        TreeTraversingParser parser1 = new TreeTraversingParser(emptyObj);
        assertEquals(JsonToken.START_OBJECT, parser1.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser1.nextToken());
        assertNull(parser1.nextToken());
        parser1.close();

        ArrayNode emptyArr = nodeFactory.arrayNode();
        TreeTraversingParser parser2 = new TreeTraversingParser(emptyArr);
        assertEquals(JsonToken.START_ARRAY, parser2.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser2.nextToken());
        assertNull(parser2.nextToken());
        parser2.close();
    }
}
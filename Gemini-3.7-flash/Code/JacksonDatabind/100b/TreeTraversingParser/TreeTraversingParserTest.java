package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import static org.junit.Assert.*;

public class TreeTraversingParserTest
{
    private final JsonNodeFactory _nodes = JsonNodeFactory.instance;

    // Tests reading binary value from TextNode using Base64Variant (Defects4J bug 100)
    @Test
    public void testGetBinaryValue_fromTextNode_returnsDecodedBytes() throws IOException {
        byte[] inputData = new byte[] { 1, 2, 3, 4, 5, 127, -128 };
        String base64Str = Base64Variants.MIME.encode(inputData);
        TextNode textNode = _nodes.textNode(base64Str);

        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertNull(parser.currentToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] result = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(result);
        assertArrayEquals(inputData, result);
        parser.close();
    }

    // Tests reading binary value from BinaryNode
    @Test
    public void testGetBinaryValue_fromBinaryNode_returnsBytes() throws IOException {
        byte[] inputData = new byte[] { 10, 20, 30 };
        BinaryNode binaryNode = _nodes.binaryNode(inputData);

        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        byte[] result = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertArrayEquals(inputData, result);
        parser.close();
    }

    // Tests reading binary value from POJONode holding byte array
    @Test
    public void testGetBinaryValue_fromPOJONode_returnsBytes() throws IOException {
        byte[] inputData = new byte[] { 4, 5, 6 };
        POJONode pojoNode = new POJONode(inputData);

        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        byte[] result = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertArrayEquals(inputData, result);
        parser.close();
    }

    // Tests readBinaryValue writing to OutputStream
    @Test
    public void testReadBinaryValue_validBinaryNode_writesToOutputStream() throws IOException {
        byte[] inputData = new byte[] { 7, 8, 9 };
        BinaryNode binaryNode = _nodes.binaryNode(inputData);

        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(3, count);
        assertArrayEquals(inputData, out.toByteArray());
        parser.close();
    }

    // Tests traversing an ObjectNode with multiple fields
    @Test
    public void testNextToken_objectNode_traversesStructureCorrectly() throws IOException {
        ObjectNode root = _nodes.objectNode();
        root.put("name", "John");
        root.put("age", 30);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("John", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());
        assertEquals(30L, parser.getLongValue());
        assertEquals(30.0, parser.getDoubleValue(), 0.0001);
        assertEquals(30.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(BigInteger.valueOf(30), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(30), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    // Tests traversing an ArrayNode with values
    @Test
    public void testNextToken_arrayNode_traversesElementsCorrectly() throws IOException {
        ArrayNode root = _nodes.arrayNode();
        root.add("first");
        root.add(123.45);
        root.add(true);
        root.addNull();

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("first", parser.getText());
        assertEquals("first".length(), parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertArrayEquals("first".toCharArray(), parser.getTextCharacters());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        assertEquals(123.45, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests skipChildren on an ArrayNode container
    @Test
    public void testSkipChildren_arrayContainer_skipsToEndArray() throws IOException {
        ArrayNode root = _nodes.arrayNode();
        root.add("element1");
        root.add("element2");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_ARRAY, parser.currentToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests skipChildren on an ObjectNode container
    @Test
    public void testSkipChildren_objectContainer_skipsToEndObject() throws IOException {
        ObjectNode root = _nodes.objectNode();
        root.put("k", "v");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.currentToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests currentNumericNode exception when current token is not numeric
    @Test(expected = JsonParseException.class)
    public void testCurrentNumericNode_nonNumericToken_throwsJsonParseException() throws IOException {
        TextNode textNode = _nodes.textNode("not_a_number");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getIntValue();
    }

    // Tests embedded object retrieval for POJO and Binary nodes
    @Test
    public void testGetEmbeddedObject_pojoAndBinaryNodes_returnsUnderlyingObject() throws IOException {
        Object pojo = new Object();
        POJONode pojoNode = new POJONode(pojo);
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        parser.nextToken();
        assertSame(pojo, parser.getEmbeddedObject());
        parser.close();

        byte[] raw = new byte[] { 1, 2 };
        BinaryNode binaryNode = _nodes.binaryNode(raw);
        TreeTraversingParser parser2 = new TreeTraversingParser(binaryNode);
        parser2.nextToken();
        assertArrayEquals(raw, (byte[]) parser2.getEmbeddedObject());
        parser2.close();
    }

    // Tests isNaN for DoubleNode containing Double.NaN
    @Test
    public void testIsNaN_nanDoubleNode_returnsTrue() throws IOException {
        DoubleNode nanNode = DoubleNode.valueOf(Double.NaN);
        TreeTraversingParser parser = new TreeTraversingParser(nanNode);
        parser.nextToken();
        assertTrue(parser.isNaN());
        parser.close();

        DoubleNode normalNode = DoubleNode.valueOf(1.5);
        TreeTraversingParser parser2 = new TreeTraversingParser(normalNode);
        parser2.nextToken();
        assertFalse(parser2.isNaN());
        parser2.close();
    }

    // Tests overrideCurrentName and parsing context
    @Test
    public void testOverrideCurrentName_objectNode_updatesCurrentName() throws IOException {
        ObjectNode root = _nodes.objectNode();
        root.put("original", 1);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("original", parser.getCurrentName());
        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
        assertNotNull(parser.getParsingContext());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        parser.close();
    }

    // Tests codec getter/setter and version
    @Test
    public void testCodecAndVersion_validCodec_returnsExpectedValues() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TreeTraversingParser parser = new TreeTraversingParser(_nodes.nullNode(), mapper);
        assertSame(mapper, parser.getCodec());

        parser.setCodec(null);
        assertNull(parser.getCodec());
        assertNotNull(parser.version());
        parser.close();
    }

    // Tests parser operations after close
    @Test
    public void testClose_alreadyClosed_returnsNullTextAndClosedState() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(_nodes.textNode("test"));
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.getText());
        assertNull(parser.getCurrentName());
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.isNaN());
    }

    // Tests empty containers handling
    @Test
    public void testNextToken_emptyContainers_returnsStartAndEndTokens() throws IOException {
        ObjectNode emptyObj = _nodes.objectNode();
        TreeTraversingParser parserObj = new TreeTraversingParser(emptyObj);
        assertEquals(JsonToken.START_OBJECT, parserObj.nextToken());
        assertEquals(JsonToken.END_OBJECT, parserObj.nextToken());
        assertNull(parserObj.nextToken());
        parserObj.close();

        ArrayNode emptyArr = _nodes.arrayNode();
        TreeTraversingParser parserArr = new TreeTraversingParser(emptyArr);
        assertEquals(JsonToken.START_ARRAY, parserArr.nextToken());
        assertEquals(JsonToken.END_ARRAY, parserArr.nextToken());
        assertNull(parserArr.nextToken());
        parserArr.close();
    }

    // Tests getBinaryValue throwing JsonParseException for non-binary node
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_fromIntNode_throwsJsonParseException() throws IOException {
        IntNode intNode = IntNode.valueOf(123);
        TreeTraversingParser parser = new TreeTraversingParser(intNode);
        parser.nextToken();
        try {
            parser.getBinaryValue();
        } finally {
            parser.close();
        }
    }

    // Tests getBinaryValue throwing JsonParseException for POJONode holding non-byte-array
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_fromNonBytePOJONode_throwsJsonParseException() throws IOException {
        POJONode pojoNode = new POJONode("Not a byte array");
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        parser.nextToken();
        try {
            parser.getBinaryValue(Base64Variants.MIME);
        } finally {
            parser.close();
        }
    }

    // Tests readBinaryValue from TextNode to OutputStream
    @Test
    public void testReadBinaryValue_fromTextNode_writesToOutputStream() throws IOException {
        byte[] inputData = new byte[] { 11, 22, 33, 44 };
        String base64Str = Base64Variants.MIME.encode(inputData);
        TextNode textNode = _nodes.textNode(base64Str);

        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(4, count);
        assertArrayEquals(inputData, out.toByteArray());
        parser.close();
    }

    // Tests getValueAs* conversion methods across different types of nodes
    @Test
    public void testGetValueAsMethods_variousNodes_returnsConvertedValues() throws IOException {
        ObjectNode root = _nodes.objectNode();
        root.put("intStr", "42");
        root.put("boolStr", "true");
        root.put("doubleVal", 3.14);
        root.put("boolVal", false);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        parser.nextToken(); // START_OBJECT

        parser.nextToken(); // FIELD_NAME: intStr
        parser.nextToken(); // VALUE_STRING: "42"
        assertEquals("42", parser.getValueAsString());
        assertEquals("42", parser.getValueAsString("default"));
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(0));
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(0L));
        assertEquals(42.0, parser.getValueAsDouble(), 0.001);
        assertEquals(42.0, parser.getValueAsDouble(0.0), 0.001);

        parser.nextToken(); // FIELD_NAME: boolStr
        parser.nextToken(); // VALUE_STRING: "true"
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(false));

        parser.nextToken(); // FIELD_NAME: doubleVal
        parser.nextToken(); // VALUE_NUMBER_FLOAT: 3.14
        assertEquals(3, parser.getValueAsInt());
        assertEquals(3L, parser.getValueAsLong());
        assertEquals(3.14, parser.getValueAsDouble(), 0.001);
        assertEquals("3.14", parser.getValueAsString());

        parser.nextToken(); // FIELD_NAME: boolVal
        parser.nextToken(); // VALUE_FALSE
        assertFalse(parser.getValueAsBoolean());
        assertEquals("false", parser.getValueAsString());

        parser.nextToken(); // END_OBJECT
        parser.close();
    }

    // Tests BigInteger and Decimal node number types
    @Test
    public void testNumberTypes_bigIntegerAndBigDecimal_returnsCorrectTypes() throws IOException {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        BigIntegerNode bigIntNode = BigIntegerNode.valueOf(bigInt);
        TreeTraversingParser parser1 = new TreeTraversingParser(bigIntNode);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser1.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser1.getNumberType());
        assertEquals(bigInt, parser1.getBigIntegerValue());
        assertEquals(bigInt, parser1.getNumberValue());
        parser1.close();

        BigDecimal bigDec = new BigDecimal("1234567890.123456789");
        DecimalNode decimalNode = DecimalNode.valueOf(bigDec);
        TreeTraversingParser parser2 = new TreeTraversingParser(decimalNode);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser2.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser2.getNumberType());
        assertEquals(bigDec, parser2.getDecimalValue());
        assertEquals(bigDec, parser2.getNumberValue());
        parser2.close();
    }

    // Tests BooleanNode false token
    @Test
    public void testBooleanNode_false_returnsValueFalseToken() throws IOException {
        BooleanNode falseNode = BooleanNode.getFalse();
        TreeTraversingParser parser = new TreeTraversingParser(falseNode);
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getText());
        assertFalse(parser.getBooleanValue());
        parser.close();
    }

    // Tests MissingNode handling
    @Test
    public void testMissingNode_returnsNullOrMissingToken() throws IOException {
        MissingNode missingNode = MissingNode.getInstance();
        TreeTraversingParser parser = new TreeTraversingParser(missingNode);
        JsonToken token = parser.nextToken();
        assertTrue(token == null || token == JsonToken.VALUE_EMBEDDED_OBJECT || token == JsonToken.NOT_AVAILABLE);
        parser.close();
    }
}
package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;

public class TreeTraversingParserTest
{
    private JsonNodeFactory _nodeFactory;

    @Before
    public void setUp()
    {
        _nodeFactory = JsonNodeFactory.instance;
    }

    // Tests traversal over an ObjectNode containing various child nodes
    @Test
    public void testNextToken_objectNode_traversesCorrectly() throws Exception
    {
        ObjectNode root = _nodeFactory.objectNode();
        root.put("name", "test");
        root.put("age", 30);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertNull(parser.getCurrentToken());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertEquals(4, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());
        assertArrayEquals("test".toCharArray(), parser.getTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());
        assertEquals(30L, parser.getLongValue());
        assertEquals(30.0, parser.getDoubleValue(), 0.001);
        assertEquals(30.0f, parser.getFloatValue(), 0.001f);
        assertEquals(BigInteger.valueOf(30), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(30), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(Integer.valueOf(30), parser.getNumberValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
        parser.close();
    }

    // Tests traversal over an ArrayNode
    @Test
    public void testNextToken_arrayNode_traversesCorrectly() throws Exception
    {
        ArrayNode root = _nodeFactory.arrayNode();
        root.add(true);
        root.add(12.5);

        TreeTraversingParser parser = new TreeTraversingParser(root);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        assertEquals(12.5, parser.getDoubleValue(), 0.001);
        assertEquals("12.5", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests empty container traversal optimization
    @Test
    public void testNextToken_emptyContainers_skipsChildren() throws Exception
    {
        ObjectNode root = _nodeFactory.objectNode();
        root.putObject("emptyObj");
        root.putArray("emptyArr");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyObj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyArr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests skipChildren on START_OBJECT token
    @Test
    public void testSkipChildren_startObject_skipsToEndObject() throws Exception
    {
        ObjectNode root = _nodeFactory.objectNode();
        ObjectNode child = root.putObject("child");
        child.put("k", "v");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests skipChildren on START_ARRAY token
    @Test
    public void testSkipChildren_startArray_skipsToEndArray() throws Exception
    {
        ArrayNode root = _nodeFactory.arrayNode();
        ArrayNode child = root.addArray();
        child.add(1);
        child.add(2);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        parser.skipChildren();
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests getBinaryValue with custom Base64Variant on TextNode
    @Test
    public void testGetBinaryValue_textNodeWithBase64_decodesUsingVariant() throws Exception
    {
        byte[] original = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        Base64Variant variant = Base64Variants.MODIFIED_FOR_URL;
        String encoded = variant.encode(original);

        TextNode textNode = _nodeFactory.textNode(encoded);
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();

        byte[] actual = parser.getBinaryValue(variant);
        assertNotNull(actual);
        assertArrayEquals(original, actual);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = parser.readBinaryValue(variant, out);
        assertEquals(original.length, written);
        assertArrayEquals(original, out.toByteArray());

        parser.close();
    }

    // Tests getBinaryValue on BinaryNode
    @Test
    public void testGetBinaryValue_binaryNode_returnsBytes() throws Exception
    {
        byte[] expected = new byte[] { 10, 20, 30 };
        BinaryNode binaryNode = _nodeFactory.binaryNode(expected);
        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        parser.nextToken();

        byte[] actual = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(actual);
        assertArrayEquals(expected, actual);
        parser.close();
    }

    // Tests getBinaryValue on POJONode holding byte array
    @Test
    public void testGetBinaryValue_pojoNodeByteArray_returnsBytes() throws Exception
    {
        byte[] expected = new byte[] { 7, 8, 9 };
        ValueNode pojoNode = _nodeFactory.pojoNode(expected);
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        parser.nextToken();

        byte[] actual = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(actual);
        assertArrayEquals(expected, actual);
        parser.close();
    }

    // Tests getEmbeddedObject on POJONode and BinaryNode
    @Test
    public void testGetEmbeddedObject_pojoAndBinaryNodes_returnsEmbeddedData() throws Exception
    {
        Object pojo = new Object();
        ValueNode pojoNode = _nodeFactory.pojoNode(pojo);
        TreeTraversingParser pojoParser = new TreeTraversingParser(pojoNode);
        pojoParser.nextToken();
        assertSame(pojo, pojoParser.getEmbeddedObject());
        pojoParser.close();

        byte[] data = new byte[] { 1, 2 };
        BinaryNode binaryNode = _nodeFactory.binaryNode(data);
        TreeTraversingParser binParser = new TreeTraversingParser(binaryNode);
        binParser.nextToken();
        assertArrayEquals(data, (byte[]) binParser.getEmbeddedObject());
        assertEquals(binaryNode.asText(), binParser.getText());
        binParser.close();
    }

    // Tests numeric accessor exception path when node is not numeric
    @Test(expected = JsonParseException.class)
    public void testCurrentNumericNode_nonNumericNode_throwsJsonParseException() throws Exception
    {
        TextNode textNode = _nodeFactory.textNode("not_a_number");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        parser.getIntValue();
    }

    // Tests isNaN for DoubleNode containing NaN and normal values
    @Test
    public void testIsNaN_doubleNode_returnsCorrectFlag() throws Exception
    {
        NumericNode nanNode = _nodeFactory.numberNode(Double.NaN);
        TreeTraversingParser parser = new TreeTraversingParser(nanNode);
        parser.nextToken();
        assertTrue(parser.isNaN());
        parser.close();
        assertFalse(parser.isNaN());

        NumericNode normalNode = _nodeFactory.numberNode(3.14);
        TreeTraversingParser parserNormal = new TreeTraversingParser(normalNode);
        parserNormal.nextToken();
        assertFalse(parserNormal.isNaN());
        parserNormal.close();
    }

    // Tests overrideCurrentName and parsing context
    @Test
    public void testOverrideCurrentName_objectField_updatesCurrentName() throws Exception
    {
        ObjectNode root = _nodeFactory.objectNode();
        root.put("oldName", "value");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME

        assertEquals("oldName", parser.getCurrentName());
        parser.overrideCurrentName("newName");
        assertEquals("newName", parser.getCurrentName());
        assertNotNull(parser.getParsingContext());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        parser.close();
        assertNull(parser.getCurrentName());
        assertNull(parser.getText());
    }

    // Tests codec getter/setter and version
    @Test
    public void testCodecAndVersion_accessors_behaveCorrectly() throws Exception
    {
        TreeTraversingParser parser = new TreeTraversingParser(_nodeFactory.nullNode(), null);
        assertNull(parser.getCodec());

        ObjectCodec mockCodec = null;
        parser.setCodec(mockCodec);
        assertNull(parser.getCodec());

        Version version = parser.version();
        assertNotNull(version);

        parser.nextToken();
        assertEquals(JsonToken.VALUE_NULL, parser.getCurrentToken());
        assertEquals("null", parser.getText());
        parser.close();
    }
}
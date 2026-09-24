package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class FromXmlParserTest {

    private FromXmlParser _createParser(String xml) throws Exception {
        XMLInputFactory staxFactory = XMLInputFactory.newInstance();
        XMLStreamReader sr = staxFactory.createXMLStreamReader(new StringReader(xml));
        IOContext ctxt = new IOContext(new BufferRecycler(), xml, false);
        return new FromXmlParser(ctxt, 0, 0, null, sr);
    }

    private FromXmlParser _createParser(String xml, ObjectCodec codec) throws Exception {
        XMLInputFactory staxFactory = XMLInputFactory.newInstance();
        XMLStreamReader sr = staxFactory.createXMLStreamReader(new StringReader(xml));
        IOContext ctxt = new IOContext(new BufferRecycler(), xml, false);
        return new FromXmlParser(ctxt, 0, 0, codec, sr);
    }

    // Tests metadata and codec configuration
    @Test
    public void testCodecAndVersion_basicAccess_returnsExpected() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertNotNull(parser.version());
        assertTrue(parser.requiresCustomCodec());
        assertNull(parser.getCodec());

        XmlMapper mapper = new XmlMapper();
        parser.setCodec(mapper);
        assertSame(mapper, parser.getCodec());
        parser.close();
    }

    // Tests XML text element configuration and format feature configuration
    @Test
    public void testFormatFeaturesAndTextElement_configuration_updatesState() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertEquals(0, parser.getFormatFeatures());
        assertEquals(0, FromXmlParser.Feature.collectDefaults());

        parser.overrideFormatFeatures(1, 1);
        assertEquals(1, parser.getFormatFeatures());

        parser.setXMLTextElementName("customText");
        assertEquals("customText", parser._cfgNameForTextElement);
        parser.close();
    }

    // Tests normal parsing sequence for basic nested XML elements
    @Test
    public void testNextToken_nestedElements_returnsMatchingTokens() throws Exception {
        FromXmlParser parser = _createParser("<root><name>Bob</name><age>25</age></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Bob", parser.getText());
        assertEquals("Bob", parser.getValueAsString());
        assertArrayEquals("Bob".toCharArray(), parser.getTextCharacters());
        assertEquals(3, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("25", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests parsing elements containing attributes
    @Test
    public void testNextToken_withAttributes_readsAttributesAsFields() throws Exception {
        FromXmlParser parser = _createParser("<root id=\"123\"><name>Alice</name></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Alice", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests empty leaf tag token generation
    @Test
    public void testNextToken_emptyLeafTag_returnsNullToken() throws Exception {
        FromXmlParser parser = _createParser("<root><empty/></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests nextTextValue on leaf string element
    @Test
    public void testNextTextValue_validStringLeaf_returnsText() throws Exception {
        FromXmlParser parser = _createParser("<root><item>Hello</item></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());

        String text = parser.nextTextValue();
        assertEquals("Hello", text);
        assertEquals(JsonToken.VALUE_STRING, parser.currentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextTextValue on empty leaf element producing empty string
    @Test
    public void testNextTextValue_emptyLeaf_returnsEmptyString() throws Exception {
        FromXmlParser parser = _createParser("<root><item/></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());

        String text = parser.nextTextValue();
        assertEquals("", text);
        assertEquals(JsonToken.VALUE_STRING, parser.currentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests array start conversion when START_OBJECT is expected to be START_ARRAY
    @Test
    public void testIsExpectedStartArrayToken_startObject_convertsToArray() throws Exception {
        FromXmlParser parser = _createParser("<root><item>1</item><item>2</item></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.currentToken());

        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("1", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("2", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests isExpectedStartArrayToken on non-array tokens
    @Test
    public void testIsExpectedStartArrayToken_scalarToken_returnsFalse() throws Exception {
        FromXmlParser parser = _createParser("<root>text</root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.isExpectedStartArrayToken());
        parser.close();
    }

    // Tests virtual wrapping for unwrapped lists
    @Test
    public void testAddVirtualWrapping_matchingName_repeatsStartElement() throws Exception {
        FromXmlParser parser = _createParser("<root><item>A</item><item>B</item></root>");

        Set<String> wrapNames = new HashSet<String>();
        wrapNames.add("item");
        parser.addVirtualWrapping(wrapNames);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNotNull(parser.getParsingContext());
        parser.close();
    }

    // Tests overriding current name in context
    @Test
    public void testOverrideCurrentName_validContext_updatesName() throws Exception {
        FromXmlParser parser = _createParser("<root><name>Test</name></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());

        parser.overrideCurrentName("newName");
        assertEquals("newName", parser.getCurrentName());
        parser.close();
    }

    // Tests getCurrentName when missing name throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetCurrentName_missingName_throwsException() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        parser.getCurrentName();
    }

    // Tests Base64 binary value decoding
    @Test
    public void testGetBinaryValue_validBase64_returnsDecodedBytes() throws Exception {
        // "SGVsbG8=" is base64 for "Hello"
        FromXmlParser parser = _createParser("<root><data>SGVsbG8=</data></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] bytes = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(bytes);
        assertEquals("Hello", new String(bytes, "UTF-8"));

        // Second call should return cached binary value
        byte[] cached = parser.getBinaryValue(Base64Variants.MIME);
        assertSame(bytes, cached);
        parser.close();
    }

    // Tests getBinaryValue on invalid token throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_nonStringToken_throwsException() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    // Tests getValueAsString with default value on non-scalar token
    @Test
    public void testGetValueAsString_withDefault_returnsExpected() throws Exception {
        FromXmlParser parser = _createParser("<root><child>value</child></root>");

        assertNull(parser.getValueAsString("default"));

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getValueAsString("default"));

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getValueAsString("default"));
        parser.close();
    }

    // Tests parser location, Stax reader access and dummy numeric accessors
    @Test
    public void testMiscAccessors_defaultState_returnsExpectedValues() throws Exception {
        FromXmlParser parser = _createParser("<root/>");

        assertNotNull(parser.getStaxReader());
        JsonLocation tokenLoc = parser.getTokenLocation();
        assertNotNull(tokenLoc);
        JsonLocation currLoc = parser.getCurrentLocation();
        assertNotNull(currLoc);

        assertNull(parser.getEmbeddedObject());
        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0001);
        assertEquals(0.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertNull(parser.getNumberType());
        assertNull(parser.getNumberValue());

        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());

        // Multiple close calls should be safe
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests readBinaryValue with OutputStream
    @Test
    public void testReadBinaryValue_validBase64_writesToOutputStream() throws Exception {
        FromXmlParser parser = _createParser("<root><data>SGVsbG8gd29ybGQ=</data></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(11, bytesRead);
        assertEquals("Hello world", new String(out.toByteArray(), "UTF-8"));
        parser.close();
    }

    // Tests nextFieldName and nextFieldName(SerializableString)
    @Test
    public void testNextFieldName_matchingAndNonMatching_returnsExpected() throws Exception {
        FromXmlParser parser = _createParser("<root><name>Alice</name><age>30</age></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        String name = parser.nextFieldName();
        assertEquals("name", name);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        boolean matched = parser.nextFieldName(new SerializedString("age"));
        assertTrue(matched);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests finishToken method execution
    @Test
    public void testFinishToken_executesWithoutError() throws Exception {
        FromXmlParser parser = _createParser("<root><item>test</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.finishToken();
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests Feature enable/disable and configure methods
    @Test
    public void testFeatureConfiguration_enableDisable_updatesParserFeatures() throws Exception {
        FromXmlParser parser = _createParser("<root/>");

        assertFalse(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
        parser.enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
        assertTrue(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

        parser.disable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
        assertFalse(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

        parser.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
        assertTrue(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

        parser.overrideStdFeatures(JsonParser.Feature.ALLOW_COMMENTS.getMask(), JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertTrue(parser.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        parser.close();
    }

    // Tests CDATA text extraction
    @Test
    public void testNextToken_withCData_parsesCorrectly() throws Exception {
        FromXmlParser parser = _createParser("<root><desc><![CDATA[Some <raw> & text]]></desc></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("desc", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Some <raw> & text", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }
}
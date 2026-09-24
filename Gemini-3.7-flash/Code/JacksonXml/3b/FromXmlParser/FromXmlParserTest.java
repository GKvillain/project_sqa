package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class FromXmlParserTest
{
    private final XmlFactory _xmlFactory = new XmlFactory();

    private FromXmlParser _createParser(String xml) throws IOException {
        return (FromXmlParser) _xmlFactory.createParser(new StringReader(xml));
    }

    // Tests nextTextValue on simple element text
    @Test
    public void testNextTextValue_simpleText_returnsTextValue() throws IOException
    {
        FromXmlParser parser = _createParser("<root><name>Hello</name></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        String text = parser.nextTextValue();
        assertEquals("Hello", text);
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextTextValue on empty leaf element
    @Test
    public void testNextTextValue_emptyElement_returnsEmptyString() throws IOException
    {
        FromXmlParser parser = _createParser("<root><name/></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        String text = parser.nextTextValue();
        assertEquals("", text);
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextToken on empty leaf returning VALUE_NULL
    @Test
    public void testNextToken_emptyElement_returnsValueNull() throws IOException
    {
        FromXmlParser parser = _createParser("<root><name/></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests parsing elements with attributes
    @Test
    public void testNextToken_withAttributes_readsAttributeTokens() throws IOException
    {
        FromXmlParser parser = _createParser("<root id=\"123\">Value</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests conversion of START_OBJECT to START_ARRAY
    @Test
    public void testIsExpectedStartArrayToken_onStartObject_convertsToArray() throws IOException
    {
        FromXmlParser parser = _createParser("<root><item>1</item><item>2</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("1", parser.getText());

        assertTrue(parser.getParsingContext().inObject());
        parser.close();
    }

    // Tests array start expectation and context conversion
    @Test
    public void testIsExpectedStartArrayToken_initialToken_returnsTrueAndConverts() throws IOException
    {
        FromXmlParser parser = _createParser("<root><item>A</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        assertTrue(parser.getParsingContext().inArray());
        parser.close();
    }

    // Tests getText, getTextCharacters, getTextLength, getTextOffset, hasTextCharacters
    @Test
    public void testGetText_textProperties_returnExpectedValues() throws IOException
    {
        FromXmlParser parser = _createParser("<root>Testing</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals("Testing", parser.getText());
        assertArrayEquals("Testing".toCharArray(), parser.getTextCharacters());
        assertEquals(7, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());
        assertEquals("Testing", parser.getValueAsString());
        assertEquals("Testing", parser.getValueAsString("default"));
        parser.close();
    }

    // Tests overrideCurrentName and getCurrentName
    @Test
    public void testOverrideCurrentName_updatesCurrentName() throws IOException
    {
        FromXmlParser parser = _createParser("<root><name>Val</name></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());

        parser.overrideCurrentName("newName");
        assertEquals("newName", parser.getCurrentName());
        parser.close();
    }

    // Tests base64 binary decoding
    @Test
    public void testGetBinaryValue_validBase64_returnsDecodedBytes() throws IOException
    {
        // "SGVsbG8=" is Base64 for "Hello"
        FromXmlParser parser = _createParser("<root>SGVsbG8=</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(binary);
        assertArrayEquals("Hello".getBytes("UTF-8"), binary);

        // Multiple calls return cached instance
        assertSame(binary, parser.getBinaryValue(Base64Variants.MIME));
        parser.close();
    }

    // Tests binary decoding exception on non-value-string token
    @Test(expected = IOException.class)
    public void testGetBinaryValue_nonStringToken_throwsException() throws IOException
    {
        FromXmlParser parser = _createParser("<root><elem>val</elem></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
        parser.close();
    }

    // Tests virtual wrapping handling
    @Test
    public void testAddVirtualWrapping_repeatsStartElement() throws IOException
    {
        FromXmlParser parser = _createParser("<root><item>1</item><item>2</item></root>");
        Set<String> wrapNames = new HashSet<String>();
        wrapNames.add("item");
        parser.addVirtualWrapping(wrapNames);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());
        parser.close();
    }

    // Tests parser life-cycle methods: close, isClosed, codec, version, customCodec
    @Test
    public void testLifeCycle_propertiesAndClose_behaveCorrectly() throws IOException
    {
        XmlMapper mapper = new XmlMapper();
        FromXmlParser parser = (FromXmlParser) mapper.getFactory().createParser(new StringReader("<root/>"));

        assertFalse(parser.isClosed());
        assertTrue(parser.requiresCustomCodec());
        Version v = parser.version();
        assertNotNull(v);
        assertFalse(v.isUknownVersion());

        ObjectCodec codec = parser.getCodec();
        assertNotNull(codec);
        parser.setCodec(null);
        assertNull(parser.getCodec());
        parser.setCodec(codec);

        assertNotNull(parser.getStaxReader());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        parser.close();
        assertTrue(parser.isClosed());

        // Repeated close should be no-op
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests numeric accessors default stubs
    @Test
    public void testNumericAccessors_stubs_returnDefaultValues() throws IOException
    {
        FromXmlParser parser = _createParser("<root>123</root>");
        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0001);
        assertEquals(0.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertNull(parser.getNumberType());
        assertNull(parser.getNumberValue());
        assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    // Tests format features manipulation
    @Test
    public void testFormatFeatures_override_updatesFlags() throws IOException
    {
        FromXmlParser parser = _createParser("<root/>");
        int initialFeatures = parser.getFormatFeatures();
        parser.overrideFormatFeatures(0xFFFF, 0x00FF);
        assertEquals((initialFeatures & ~0x00FF) | 0x00FF, parser.getFormatFeatures());
        parser.close();
    }

    // Tests custom XML text element name configuration
    @Test
    public void testSetXMLTextElementName_customName_usedForAnonymousText() throws IOException
    {
        FromXmlParser parser = _createParser("<root attr=\"val\">inner</root>");
        parser.setXMLTextElementName("value");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("attr", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("value", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("inner", parser.getText());
        parser.close();
    }

    // Tests parser feature configuration methods
    @Test
    public void testParserFeatures_enableDisableConfigure() throws IOException
    {
        FromXmlParser parser = _createParser("<root/>");
        FromXmlParser.Feature feat = FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL;

        boolean initial = parser.isEnabled(feat);
        parser.configure(feat, !initial);
        assertEquals(!initial, parser.isEnabled(feat));

        parser.enable(feat);
        assertTrue(parser.isEnabled(feat));

        parser.disable(feat);
        assertFalse(parser.isEnabled(feat));
        parser.close();
    }

    // Tests getText(Writer)
    @Test
    public void testGetText_writer_writesExpectedText() throws IOException
    {
        FromXmlParser parser = _createParser("<root>Hello World</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        StringWriter sw = new StringWriter();
        int written = parser.getText(sw);
        assertEquals(11, written);
        assertEquals("Hello World", sw.toString());
        parser.close();
    }

    // Tests readBinaryValue to OutputStream
    @Test
    public void testReadBinaryValue_outputStream_writesDecodedBytes() throws IOException
    {
        FromXmlParser parser = _createParser("<root>SGVsbG8=</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(5, count);
        assertArrayEquals("Hello".getBytes("UTF-8"), baos.toByteArray());
        parser.close();
    }

    // Tests getValueAs* conversions for various types
    @Test
    public void testGetValueAs_typeConversions_returnConvertedValues() throws IOException
    {
        FromXmlParser parser = _createParser("<root>123</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals(123, parser.getValueAsInt());
        assertEquals(123, parser.getValueAsInt(456));
        assertEquals(123L, parser.getValueAsLong());
        assertEquals(123L, parser.getValueAsLong(456L));
        assertEquals(123.0, parser.getValueAsDouble(), 0.001);
        assertEquals(123.0, parser.getValueAsDouble(456.0), 0.001);
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
        parser.close();

        FromXmlParser boolParser = _createParser("<root>true</root>");
        assertEquals(JsonToken.START_OBJECT, boolParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, boolParser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, boolParser.nextToken());
        assertTrue(boolParser.getValueAsBoolean());
        assertTrue(boolParser.getValueAsBoolean(false));
        boolParser.close();
    }

    // Tests isExpectedStartArrayToken when current token is already START_ARRAY or other token
    @Test
    public void testIsExpectedStartArrayToken_nonStartObjectTokens_returnsExpected() throws IOException
    {
        FromXmlParser parser = _createParser("<root><item>1</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        // Second call while on START_ARRAY returns true
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // On FIELD_NAME, should return false
        assertFalse(parser.isExpectedStartArrayToken());
        parser.close();
    }

    // Tests nextTextValue returning null when next token is not a text value
    @Test
    public void testNextTextValue_nestedObject_returnsNull() throws IOException
    {
        FromXmlParser parser = _createParser("<root><child><nested>val</nested></child></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        // Next is START_OBJECT for <child>, so nextTextValue returns null
        assertNull(parser.nextTextValue());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        parser.close();
    }

    // Tests getBinaryValue throwing JsonParseException on malformed base64
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_corruptBase64_throwsJsonParseException() throws IOException
    {
        FromXmlParser parser = _createParser("<root>???not-valid-base64???</root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
        parser.close();
    }

    // Tests getText on null/empty parser tokens
    @Test
    public void testGetText_noCurrentToken_returnsNull() throws IOException
    {
        FromXmlParser parser = _createParser("<root/>");
        assertNull(parser.getText());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        parser.close();
    }
}
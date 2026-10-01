package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class FromXmlParserTest {

    private final XmlMapper _mapper = new XmlMapper();

    private FromXmlParser _createParser(String xml) throws IOException {
        return (FromXmlParser) _mapper.getFactory().createParser(xml);
    }

    // Tests standard XML token stream parsing
    @Test
    public void testNextToken_simpleXml_returnsExpectedTokens() throws IOException {
        FromXmlParser parser = _createParser("<root><name>Jackson</name><count>123</count></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Jackson", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("count", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests nextTextValue for leaf string elements
    @Test
    public void testNextTextValue_validText_returnsTextValueDirectly() throws IOException {
        FromXmlParser parser = _createParser("<root><title>XML Testing</title></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("title", parser.getCurrentName());

        String text = parser.nextTextValue();
        assertEquals("XML Testing", text);
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests nextTextValue on empty leaf element
    @Test
    public void testNextTextValue_emptyLeafElement_returnsEmptyString() throws IOException {
        FromXmlParser parser = _createParser("<root><empty/></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());

        String text = parser.nextTextValue();
        assertEquals("", text);
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests conversion of START_OBJECT to START_ARRAY token
    @Test
    public void testIsExpectedStartArrayToken_startObject_convertsToArray() throws IOException {
        FromXmlParser parser = _createParser("<root><item><sub/></item></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());

        // Repeated call when already START_ARRAY should still return true
        assertTrue(parser.isExpectedStartArrayToken());
        parser.close();
    }

    // Tests isExpectedStartArrayToken when token is not START_OBJECT or START_ARRAY
    @Test
    public void testIsExpectedStartArrayToken_scalarValue_returnsFalse() throws IOException {
        FromXmlParser parser = _createParser("<root>value</root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertFalse(parser.isExpectedStartArrayToken());
        parser.close();
    }

    // Tests overrideCurrentName functionality
    @Test
    public void testOverrideCurrentName_updatesNameInContext() throws IOException {
        FromXmlParser parser = _createParser("<root><item>val</item></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());

        parser.overrideCurrentName("renamedItem");
        assertEquals("renamedItem", parser.getCurrentName());
        parser.close();
    }

    // Tests getCurrentName throws exception when no name is available
    @Test(expected = IllegalStateException.class)
    public void testGetCurrentName_missingName_throwsIllegalStateException() throws IOException {
        FromXmlParser parser = _createParser("<root/>");
        parser.getCurrentName();
    }

    // Tests text character buffer methods
    @Test
    public void testGetTextCharacters_andOffsets_returnsCorrectValues() throws IOException {
        FromXmlParser parser = _createParser("<root>sample</root>");

        assertNull(parser.getText());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals("sample", parser.getText());
        assertArrayEquals("sample".toCharArray(), parser.getTextCharacters());
        assertEquals(6, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());
        parser.close();
    }

    // Tests getValueAsString with scalar and fallback default value
    @Test
    public void testGetValueAsString_withDefault_returnsCorrectString() throws IOException {
        FromXmlParser parser = _createParser("<root><elem>text</elem></root>");

        assertNull(parser.getValueAsString());
        assertNull(parser.getValueAsString("default"));

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("elem", parser.getValueAsString());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getValueAsString());
        parser.close();
    }

    // Tests base64 decoding from string token
    @Test
    public void testGetBinaryValue_validBase64_returnsDecodedBytes() throws IOException {
        FromXmlParser parser = _createParser("<root><data>SGVsbG8gV29ybGQ=</data></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(binary);
        assertEquals("Hello World", new String(binary, "UTF-8"));

        // Second call should return cached binary value
        byte[] cached = parser.getBinaryValue(Base64Variants.MIME);
        assertSame(binary, cached);
        parser.close();
    }

    // Tests base64 decoding on invalid token throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_invalidToken_throwsException() throws IOException {
        FromXmlParser parser = _createParser("<root/>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    // Tests base64 decoding with corrupted content throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_corruptedBase64_throwsException() throws IOException {
        FromXmlParser parser = _createParser("<root><data>!!!InvalidBase64!!!</data></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    // Tests XML attributes reading as fields and values
    @Test
    public void testAttributes_readAsFieldAndValueTokens() throws IOException {
        FromXmlParser parser = _createParser("<root id=\"123\" name=\"test\"><child>content</child></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("content", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests virtual wrapping for unwrapped lists
    @Test
    public void testAddVirtualWrapping_repeatsStartElement() throws IOException {
        FromXmlParser parser = _createParser("<root><item>one</item><item>two</item></root>");

        Set<String> wrapping = new HashSet<String>();
        wrapping.add("item");
        parser.addVirtualWrapping(wrapping);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNotNull(parser.getParsingContext());
        parser.close();
    }

    // Tests format features overrides
    @Test
    public void testFormatFeatures_overrideAndGet() throws IOException {
        FromXmlParser parser = _createParser("<root/>");

        int initialFeatures = parser.getFormatFeatures();
        parser.overrideFormatFeatures(0xFFFF, 0x00FF);
        assertEquals((initialFeatures & ~0x00FF) | (0xFFFF & 0x00FF), parser.getFormatFeatures());
        parser.close();
    }

    // Tests lifecycle, codec, locations, and close operations
    @Test
    public void testLifecycle_andMiscMethods() throws IOException {
        FromXmlParser parser = _createParser("<root/>");

        assertFalse(parser.isClosed());
        assertTrue(parser.requiresCustomCodec());
        assertNotNull(parser.version());
        assertNotNull(parser.getStaxReader());
        assertNotNull(parser.getParsingContext());

        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        assertNotNull(parser.getCurrentLocation());

        ObjectCodec codec = parser.getCodec();
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());

        // Numeric accessors default behavior check
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
        assertTrue(parser.isClosed());

        // Repeated close should be a safe no-op
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests Feature configure and isEnabled
    @Test
    public void testConfigure_andIsEnabled() throws IOException {
        FromXmlParser parser = _createParser("<root/>");

        assertFalse(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
        parser.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
        assertTrue(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

        parser.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, false);
        assertFalse(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));
        parser.close();
    }

    // Tests EMPTY_ELEMENT_AS_NULL feature behavior on empty element
    @Test
    public void testFeature_emptyElementAsNull() throws IOException {
        FromXmlParser parser = _createParser("<root><empty/></root>");
        parser.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextValue navigation skipping explicit field name tokens
    @Test
    public void testNextValue_skipsToValueTokens() throws IOException {
        FromXmlParser parser = _createParser("<root><item>val</item></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextValue());
        assertEquals("val", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextValue());
        assertNull(parser.nextValue());
        parser.close();
    }

    // Tests readBinaryValue writing to OutputStream
    @Test
    public void testReadBinaryValue_streamsBytesCorrectly() throws IOException {
        FromXmlParser parser = _createParser("<root><data>SGVsbG8=</data></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);

        assertEquals(5, count);
        assertArrayEquals("Hello".getBytes("UTF-8"), out.toByteArray());
        parser.close();
    }

    // Tests getText writing to Writer
    @Test
    public void testGetText_withWriter_writesCompleteString() throws IOException {
        FromXmlParser parser = _createParser("<root><message>WritingToWriter</message></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        StringWriter writer = new StringWriter();
        int written = parser.getText(writer);

        assertEquals(15, written);
        assertEquals("WritingToWriter", writer.toString());
        parser.close();
    }

    // Tests skipChildren skips inner nested objects/arrays
    @Test
    public void testSkipChildren_skipsNestedSubTree() throws IOException {
        FromXmlParser parser = _createParser("<root><nested><a/><b/></nested><after>ok</after></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nested", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("after", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ok", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }
}
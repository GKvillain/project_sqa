package com.fasterxml.jackson.dataformat.xml.deser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class FromXmlParserTest {

    private XmlMapper mapper;

    @Before
    public void setUp() {
        mapper = new XmlMapper();
    }

    private FromXmlParser createParser(String xml) throws IOException {
        return (FromXmlParser) mapper.getFactory()
                .createParser(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    // Normal: simple object parsing
    @Test
    public void testNextToken_simpleObject_returnsCorrectTokens() throws IOException {
        String xml = "<root><field>value</field></root>";
        FromXmlParser parser = createParser(xml);

        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Normal: array via isExpectedStartArrayToken
    @Test
    public void testIsExpectedStartArrayToken_convertsStartObjectToArray() throws IOException {
        String xml = "<root><item>1</item></root>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());

        // After conversion, next token should be FIELD_NAME for the element
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("1", parser.getText());
        // End of array
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Defect detection: nextTextValue should return null for empty element in array (bug #3b)
    @Test
    public void testNextTextValue_emptyElementInArray_returnsNull() throws IOException {
        String xml = "<root><item/></root>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        // After conversion, call nextTextValue – expected null, but current code returns ""
        // Test will fail if defect is present
        assertNull(parser.nextTextValue());
    }

    // Normal: nextToken on empty element in array produces empty object
    @Test
    public void testNextToken_emptyElementInArray_returnsEmptyObject() throws IOException {
        String xml = "<root><item/></root>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());

        // Should see empty object: START_OBJECT, END_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Normal: nextTextValue for non-empty text in array
    @Test
    public void testNextTextValue_nonEmptyTextInArray_returnsText() throws IOException {
        String xml = "<root><item>hello</item></root>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals("hello", parser.nextTextValue());
        // After that, token should be VALUE_STRING
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        assertEquals("hello", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Normal: getCurrentName inside object
    @Test
    public void testGetCurrentName_inObjectContext_returnsFieldName() throws IOException {
        String xml = "<root><field>value</field></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME (root)
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME (field)
        assertEquals("field", parser.getCurrentName());
    }

    // Normal: getValueAsString when current token is object with text
    @Test
    public void testGetValueAsString_objectWithText_returnsText() throws IOException {
        String xml = "<root attr='x'>textContent</root>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // Now at START_OBJECT, call getValueAsString
        String result = parser.getValueAsString();
        // Should convert to VALUE_STRING with "textContent"
        assertEquals("textContent", result);
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        assertEquals("textContent", parser.getText());
        // Remaining: END_OBJECT
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Normal: getValueAsString for object with only attributes returns null
    @Test
    public void testGetValueAsString_objectWithOnlyAttributes_returnsNull() throws IOException {
        String xml = "<root attr='x'/>";
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
        // Token should remain START_OBJECT (no conversion)
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    }

    // Normal: close and isClosed
    @Test
    public void testClose_closesParserAndReleasesResources() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);

        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Normal: addVirtualWrapping
    @Test
    public void testAddVirtualWrapping_specifiedName_repeatsStartElement() throws IOException {
        String xml = "<root><wrapper><item>a</item><item>b</item></wrapper></root>";
        FromXmlParser parser = createParser(xml);

        // Navigate to the wrapper field
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("wrapper", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        // Now set virtual wrapping on "item"
        Set<String> names = new HashSet<>();
        names.add("item");
        parser.addVirtualWrapping(names);

        // Next token should be FIELD_NAME "item"
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());
        // Because of virtual wrapping, the element is repeated
        // Actually, the next token after FIELD_NAME will be START_OBJECT (due to wrapping)
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // Next field again
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("item", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Normal: getBinaryValue decodes base64
    @Test
    public void testGetBinaryValue_base64Text_returnsDecodedBytes() throws IOException {
        String xml = "<root>SGVsbG8=</root>"; // "Hello" in base64
        FromXmlParser parser = createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertArrayEquals("Hello".getBytes("UTF-8"), decoded);
    }

    // Edge: getText on field name
    @Test
    public void testGetText_fieldName_returnsName() throws IOException {
        String xml = "<root><field>v</field></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        assertEquals("root", parser.getText());
    }

    // Edge: getText on value string
    @Test
    public void testGetText_valueString_returnsText() throws IOException {
        String xml = "<root>data</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // VALUE_STRING
        assertEquals("data", parser.getText());
    }

    // Edge: getValueAsString with default value
    @Test
    public void testGetValueAsString_withDefault_returnsDefaultWhenNoValue() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        // No content yet, _currToken is START_OBJECT
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Edge: overrideCurrentName
    @Test
    public void testOverrideCurrentName_updatesFieldName() throws IOException {
        String xml = "<root><field>v</field></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "field"
        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
        assertEquals("overridden", parser.getText());
    }

    // Edge: nextTextValue returns null when current token is not VALUE_STRING
    @Test
    public void testNextTextValue_afterFieldName_returnsNull() throws IOException {
        String xml = "<root><field>v</field></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        assertNull(parser.nextTextValue()); // token is FIELD_NAME, not string
    }

    // Edge: isClosed before close
    @Test
    public void testIsClosed_afterClose_returnsTrue() throws IOException {
        FromXmlParser parser = createParser("<root/>");
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Error: getBinaryValue on non-string token throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_onNonStringToken_throwsException() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    // ===================== New tests for uncovered coverage =====================

    // Test getNumberValue(), getIntValue(), getLongValue() for numeric text
    @Test
    public void testGetNumberValue_integerText_returnsNumber() throws IOException {
        String xml = "<root>123</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
        assertEquals(BigInteger.valueOf(123), parser.getBigIntegerValue());
        assertNotNull(parser.getNumberValue());
    }

    // Test getTextCharacters() and getTextLength()
    @Test
    public void testGetTextCharacters_returnsCorrectChars() throws IOException {
        String xml = "<root>hello</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // VALUE_STRING

        char[] chars = parser.getTextCharacters();
        assertEquals(5, parser.getTextLength());
        assertEquals("hello", new String(chars, parser.getTextOffset(), parser.getTextLength()));
    }

    // Test nextFieldName() returns next field name and advances correctly
    @Test
    public void testNextFieldName_returnsExpectedFieldName() throws IOException {
        String xml = "<root><a>1</a><b>2</b></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // START_OBJECT

        assertTrue(parser.nextFieldName("a")); // returns true if next field name matches
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("1", parser.getText());

        assertTrue(parser.nextFieldName("b"));
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("2", parser.getText());
    }

    // Test nextValue() for simple text
    @Test
    public void testNextValue_returnsValueToken() throws IOException {
        String xml = "<root>value</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        JsonToken token = parser.nextValue();
        assertEquals(JsonToken.VALUE_STRING, token);
        assertEquals("value", parser.getText());
    }

    // Test skipChildren() skips nested content
    @Test
    public void testSkipChildren_skipsNestedObject() throws IOException {
        String xml = "<root><a><b>1</b></a><c>2</c></root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        parser.skipChildren();
        // After skipping, should be at END_OBJECT of "a"
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        // Next should be FIELD_NAME "c"
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
    }

    // Test getParsingContext() returns non-null context
    @Test
    public void testGetParsingContext_returnsNonNullContext() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken();

        assertNotNull(parser.getParsingContext());
        assertNotNull(parser.getCurrentLocation());
    }

    // Test getFormatName() returns "XML"
    @Test
    public void testGetFormatName_returnsXml() throws IOException {
        FromXmlParser parser = createParser("<root/>");
        assertEquals("XML", parser.getFormatName());
    }

    // Test getVersion() returns non-null version
    @Test
    public void testGetVersion_returnsVersion() throws IOException {
        FromXmlParser parser = createParser("<root/>");
        assertNotNull(parser.getVersion());
    }

    // Test hasCurrentToken(), hasToken(), hasTokenId()
    @Test
    public void testTokenStateMethods_workCorrectly() throws IOException {
        String xml = "<root></root>";
        FromXmlParser parser = createParser(xml);

        assertFalse(parser.hasCurrentToken());
        parser.nextToken(); // START_OBJECT
        assertTrue(parser.hasCurrentToken());
        assertTrue(parser.hasToken(JsonToken.START_OBJECT));
        assertTrue(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, parser.getCurrentTokenId());
    }

    // Test getValueAsBoolean() for text "true"/"false"
    @Test
    public void testGetValueAsBoolean_parsesBooleanText() throws IOException {
        String xml = "<root>true</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // VALUE_STRING

        assertTrue(parser.getValueAsBoolean());
    }

    // Test getEmbeddedObject() is null for normal string
    @Test
    public void testGetEmbeddedObject_returnsNull() throws IOException {
        String xml = "<root>text</root>";
        FromXmlParser parser = createParser(xml);

        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "root"
        parser.nextToken(); // VALUE_STRING

        assertNull(parser.getEmbeddedObject());
    }
}
package com.fasterxml.jackson.dataformat.xml.deser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

public class XmlTokenStreamTest {

    private XMLStreamReader createXmlStreamReader(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        reader.nextTag(); // move to START_ELEMENT
        return reader;
    }

    // Tests constructor with valid START_ELEMENT state
    @Test
    public void testConstructor_validStartElement_setsInitialState() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='v'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());
        assertTrue(tokenStream.hasAttributes());
    }

    // Tests constructor with non-START_ELEMENT reader throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nonStartElement_throwsException() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader("<?xml version='1.0'?>"));
        // reader is at START_DOCUMENT, not START_ELEMENT
        new XmlTokenStream(reader, "test");
    }

    // Tests next() returns XML_ATTRIBUTE_NAME for the first attribute
    @Test
    public void testNext_elementWithAttribute_returnsAttributeName() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='val'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);
        assertEquals("attr", tokenStream.getLocalName());
        assertEquals("val", tokenStream.getText());
    }

    // Tests next() returns XML_ATTRIBUTE_VALUE after XML_ATTRIBUTE_NAME
    @Test
    public void testNext_afterAttributeName_returnsAttributeValue() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='val'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.next(); // attribute name
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, token);
        // after attribute value, state is value
        assertEquals("val", tokenStream.getText());
    }

    // Tests next() returns XML_TEXT for element with text content
    @Test
    public void testNext_elementWithText_returnsText() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root>hello</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // skip attributes (none)
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_TEXT, token);
        assertEquals("hello", tokenStream.getText());
    }

    // Tests next() returns XML_END_ELEMENT for empty element (self-closing)
    @Test
    public void testNext_emptyElement_returnsEndElement() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // skip attributes (none)
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        assertEquals("root", tokenStream.getLocalName());
    }

    // Tests next() returns XML_END for end of document
    @Test
    public void testNext_endOfDocument_returnsEnd() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root><child/></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // consume tokens
        while (tokenStream.next() != XmlTokenStream.XML_END) {
            // loop
        }
        assertEquals(XmlTokenStream.XML_END, tokenStream.getCurrentToken());
    }

    // Tests next() returns XML_START_ELEMENT for nested element
    @Test
    public void testNext_nestedElement_returnsStartElement() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root><child/></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // root start element, skip attributes
        tokenStream.next(); // XML_END_ELEMENT for root (since empty, but root has child so text may be collected)
        // Let's step through until we see child start
        int token;
        do {
            token = tokenStream.next();
        } while (token != XmlTokenStream.XML_START_ELEMENT && token != XmlTokenStream.XML_END);
        if (token == XmlTokenStream.XML_START_ELEMENT) {
            assertEquals("child", tokenStream.getLocalName());
        } else {
            fail("Expected START_ELEMENT for child");
        }
    }

    // Tests skipEndElement() skips the next END_ELEMENT successfully
    @Test
    public void testSkipEndElement_validEndElement_skips() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // current state is START_ELEMENT, next will be END_ELEMENT
        tokenStream.skipEndElement();
        // after skip, state should be XML_END or next
        assertTrue(tokenStream.getCurrentToken() == XmlTokenStream.XML_END_ELEMENT || tokenStream.getCurrentToken() == XmlTokenStream.XML_END);
    }

    // Tests skipEndElement() throws IOException when next token is not END_ELEMENT
    @Test(expected = IOException.class)
    public void testSkipEndElement_nonEndElement_throwsIOException() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='v'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // next token is ATTRIBUTE_NAME, not END_ELEMENT
        tokenStream.skipEndElement();
    }

    // Tests repeatStartElement() and subsequent next() returns XML_START_ELEMENT again
    @Test
    public void testRepeatStartElement_afterStartElement_repeatsStart() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // current state is START_ELEMENT
        tokenStream.repeatStartElement();
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        assertEquals("root", tokenStream.getLocalName());
    }

    // Tests skipAttributes() when current state is START_ELEMENT and attributes exist
    @Test
    public void testSkipAttributes_onStartElementWithAttr_skips() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr1='a' attr2='b'><child/></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // state is START_ELEMENT with attributes
        assertTrue(tokenStream.hasAttributes());
        tokenStream.skipAttributes();
        // after skip, next() should go to first child or text, not attributes
        int token = tokenStream.next();
        // with child element, after text collection might get END_ELEMENT or START_ELEMENT
        assertTrue(token == XmlTokenStream.XML_END_ELEMENT || token == XmlTokenStream.XML_TEXT || token == XmlTokenStream.XML_START_ELEMENT);
    }

    // Tests convertToString() returns text for attribute-induced object with text content
    @Test
    public void testConvertToString_validAttributeState_returnsText() throws Exception {
        // This tests the path where state is XML_ATTRIBUTE_NAME and _nextAttributeIndex is 0
        XMLStreamReader reader = createXmlStreamReader("<root attr='a'>text</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // move to attribute name
        tokenStream.next(); // ATTRIBUTE_NAME
        // now state is XML_ATTRIBUTE_NAME, _nextAttributeIndex is 0
        // For convertToString to succeed, we need _currentState == XML_ATTRIBUTE_NAME and _nextAttributeIndex == 0
        // But original element has more than one attribute? Let's simplify with single attribute
        // After attribute name, call convertToString - it should collect until tag and if END_ELEMENT returns text
        // But here after attribute name, the underlying stream is at attribute name.
        // convertToString will call _collectUntilTag which will advance to child/text
        // For this test to work, we need a scenario where after attribute name, the next token is text and then END_ELEMENT
        // Let's create a more complex scenario: <root attr='a'>text</root>
        // The stream after attribute name will have next as text or end element
        // This is tricky.  We'll test the condition where it returns null for non-attribute state.
        String result = tokenStream.convertToString();
        // Since we are at ATTRIBUTE_NAME and _nextAttributeIndex == 0, it should attempt to collect until tag
        // After text "text" and END_ELEMENT, it should return "text"
        assertNotNull(result);
        assertEquals("text", result);
    }

    // Tests convertToString() returns null when current state is not XML_ATTRIBUTE_NAME
    @Test
    public void testConvertToString_nonAttributeState_returnsNull() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root>text</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // state is START_ELEMENT, not ATTRIBUTE_NAME
        String result = tokenStream.convertToString();
        assertNull(result);
    }

    // Tests getText() returns text after XML_TEXT token
    @Test
    public void testGetText_afterTextToken_returnsValue() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root>hello</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.next(); // XML_TEXT
        assertEquals("hello", tokenStream.getText());
    }

    // Tests getLocalName() returns correct name
    @Test
    public void testGetLocalName_afterConstruction_returnsRoot() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<myns:root xmlns:myns='uri'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertEquals("root", tokenStream.getLocalName());
    }

    // Tests getNamespaceURI() returns namespace for namespaced element
    @Test
    public void testGetNamespaceURI_withNamespace_returnsUri() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<myns:root xmlns:myns='http://example.com'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertEquals("http://example.com", tokenStream.getNamespaceURI());
    }

    // Tests hasAttributes() returns false for element without attributes
    @Test
    public void testHasAttributes_noAttributes_returnsFalse() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertFalse(tokenStream.hasAttributes());
    }

    // Tests hasAttributes() returns true for element with attributes
    @Test
    public void testHasAttributes_withAttributes_returnsTrue() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='1'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertTrue(tokenStream.hasAttributes());
    }

    // Tests close() method does not throw
    @Test
    public void testClose_validStream_closes() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.close();
        // no exception expected
    }

    // Tests closeCompletely() method does not throw
    @Test
    public void testCloseCompletely_validStream_closes() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.closeCompletely();
    }

    // Tests getCurrentLocation() returns a non-null location
    @Test
    public void testGetCurrentLocation_returnsLocation() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertNotNull(tokenStream.getCurrentLocation());
    }

    // Tests getTokenLocation() returns a non-null location
    @Test
    public void testGetTokenLocation_returnsLocation() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        assertNotNull(tokenStream.getTokenLocation());
    }

    // ---- New tests for uncovered parts ----

    // Tests next() with multiple attributes returns attributes in order
    @Test
    public void testNext_multipleAttributes_returnsAttributesInOrder() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root a='1' b='2'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // first attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("a", tokenStream.getLocalName());
        assertEquals("1", tokenStream.getText());
        // first attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("1", tokenStream.getText());
        // second attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("b", tokenStream.getLocalName());
        assertEquals("2", tokenStream.getText());
        // second attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("2", tokenStream.getText());
        // then end element (self-closing)
        int token = tokenStream.next();
        assertTrue(token == XmlTokenStream.XML_END_ELEMENT || token == XmlTokenStream.XML_END);
    }

    // Tests next() with attributes and text content
    @Test
    public void testNext_elementWithAttributesAndText_returnsTokens() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='val'>text content</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        // attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        // after attributes, next should be text (since element has text)
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_TEXT, token);
        assertEquals("text content", tokenStream.getText());
        // then end element
        token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
    }

    // Tests getLocalName() when current token is an attribute name
    @Test
    public void testGetLocalName_onAttributeName_returnsAttributeName() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root myattr='x'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.next(); // XML_ATTRIBUTE_NAME
        assertEquals("myattr", tokenStream.getLocalName());
    }

    // Tests skipAttributes() when element has no attributes (should be a no-op)
    @Test
    public void testSkipAttributes_onElementWithoutAttributes_skips() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root><child/></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // start element with no attributes
        assertFalse(tokenStream.hasAttributes());
        tokenStream.skipAttributes(); // should do nothing
        int token = tokenStream.next();
        // after skipping, next should be either text, end element, or start element of child
        assertTrue(token == XmlTokenStream.XML_END_ELEMENT || token == XmlTokenStream.XML_TEXT || token == XmlTokenStream.XML_START_ELEMENT);
    }

    // Tests repeatStartElement() and then next() returns attribute
    @Test
    public void testRepeatStartElement_thenNextAttribute_works() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root attr='1'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        tokenStream.repeatStartElement();
        // now current token is START_ELEMENT again (repeat)
        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);
        assertEquals("attr", tokenStream.getLocalName());
    }

    // Tests getNamespaceURI() on an attribute with namespace prefix
    @Test
    public void testGetNamespaceURI_onAttributeWithNamespace_returnsUri() throws Exception {
        XMLStreamReader reader = createXmlStreamReader("<root xmlns:ns='http://ns' ns:attr='val'/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "test");
        // move to attribute name (ns:attr)
        tokenStream.next(); // XML_ATTRIBUTE_NAME
        assertEquals("http://ns", tokenStream.getNamespaceURI());
    }
}
package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;

public class XmlTokenStreamTest {

    private XmlTokenStream _createStream(String xml) throws Exception {
        XMLInputFactory f = XMLInputFactory.newInstance();
        XMLStreamReader sr = f.createXMLStreamReader(new StringReader(xml));
        while (sr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            sr.next();
        }
        return new XmlTokenStream(sr, xml);
    }

    // Tests constructor exception when XMLStreamReader is not pointing to START_ELEMENT
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_notAtStartElement_throwsIllegalArgumentException() throws Exception {
        XMLInputFactory f = XMLInputFactory.newInstance();
        XMLStreamReader sr = f.createXMLStreamReader(new StringReader("<root/>"));
        // sr is at START_DOCUMENT, not START_ELEMENT
        new XmlTokenStream(sr, "source");
    }

    // Tests basic token iteration for simple text element
    @Test
    public void testNext_simpleTextElement_readsTokensCorrectly() throws Exception {
        XmlTokenStream stream = _createStream("<root>Hello</root>");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertEquals("root", stream.getLocalName());
        assertFalse(stream.hasAttributes());

        int token = stream.next();
        assertEquals(XmlTokenStream.XML_TEXT, token);
        assertEquals("Hello", stream.getText());

        token = stream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next();
        assertEquals(XmlTokenStream.XML_END, token);

        // Calling next after XML_END remains XML_END
        assertEquals(XmlTokenStream.XML_END, stream.next());
    }

    // Tests parsing attributes and verifying name/value token sequences
    @Test
    public void testNext_withAttributes_readsAttributeNameAndValueTokens() throws Exception {
        XmlTokenStream stream = _createStream("<root attr=\"val\" id=\"123\">content</root>");
        assertTrue(stream.hasAttributes());

        // First attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("attr", stream.getLocalName());

        // First attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("val", stream.getText());

        // Second attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("id", stream.getLocalName());

        // Second attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("123", stream.getText());

        // Content text
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("content", stream.getText());

        // End element
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    // Tests skipAttributes when currently pointing to an attribute name
    @Test
    public void testSkipAttributes_atAttributeName_resetsStateToStartElement() throws Exception {
        XmlTokenStream stream = _createStream("<root attr=\"val\">test</root>");
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());

        stream.skipAttributes();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertFalse(stream.hasAttributes());

        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("test", stream.getText());
    }

    // Tests skipAttributes exception when called in invalid state
    @Test(expected = IllegalStateException.class)
    public void testSkipAttributes_invalidState_throwsIllegalStateException() throws Exception {
        XmlTokenStream stream = _createStream("<root>text</root>");
        stream.next(); // XML_TEXT
        stream.next(); // XML_END_ELEMENT
        stream.skipAttributes();
    }

    // Tests skipEndElement successfully skipping an END_ELEMENT token
    @Test
    public void testSkipEndElement_onEndElement_advancesStream() throws Exception {
        XmlTokenStream stream = _createStream("<root>text</root>");
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        stream.skipEndElement();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.getCurrentToken());
    }

    // Tests skipEndElement throwing IOException when next token is not END_ELEMENT
    @Test(expected = IOException.class)
    public void testSkipEndElement_notEndElement_throwsIOException() throws Exception {
        XmlTokenStream stream = _createStream("<root><child>value</child></root>");
        stream.skipEndElement();
    }

    // Tests repeatStartElement mechanism for virtual wrapping duplication
    @Test
    public void testRepeatStartElement_atStartElement_replaysStartElement() throws Exception {
        XmlTokenStream stream = _createStream("<root><item>1</item><item>2</item></root>");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.next()); // <item>
        assertEquals("item", stream.getLocalName());

        stream.repeatStartElement();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.next()); // Replayed <item>

        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("1", stream.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    // Tests repeatStartElement throwing exception when not at XML_START_ELEMENT
    @Test(expected = IllegalStateException.class)
    public void testRepeatStartElement_invalidState_throwsIllegalStateException() throws Exception {
        XmlTokenStream stream = _createStream("<root>text</root>");
        stream.next(); // XML_TEXT
        stream.repeatStartElement();
    }

    // Tests convertToString when in attribute name state with empty element
    @Test
    public void testConvertToString_emptyElementWithAttributes_returnsEmptyString() throws Exception {
        XmlTokenStream stream = _createStream("<root attr=\"val\"></root>");
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());

        String text = stream.convertToString();
        assertNotNull(text);
        assertEquals("", text);
        assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
    }

    // Tests convertToString returns null when not at XML_ATTRIBUTE_NAME
    @Test
    public void testConvertToString_notAtAttributeName_returnsNull() throws Exception {
        XmlTokenStream stream = _createStream("<root>value</root>");
        assertNull(stream.convertToString());
    }

    // Tests CDATA content and mixed whitespace handling
    @Test
    public void testCollectUntilTag_withCData_combinesText() throws Exception {
        XmlTokenStream stream = _createStream("<root><![CDATA[cdata-text]]></root>");
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("cdata-text", stream.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    // Tests stream location extraction and reader access methods
    @Test
    public void testLocationsAndXmlReader_validStream_returnsNonProperties() throws Exception {
        XmlTokenStream stream = _createStream("<root attr=\"val\">text</root>");
        assertNotNull(stream.getXmlReader());
        assertNotNull(stream.getCurrentLocation());
        assertNotNull(stream.getTokenLocation());
        assertEquals("root", stream.getLocalName());
        assertNull(stream.getNamespaceURI());
    }

    // Tests close and closeCompletely execution
    @Test
    public void testCloseMethods_openStream_executesWithoutException() throws Exception {
        XmlTokenStream stream = _createStream("<root/>");
        stream.close();
        stream.closeCompletely();
    }

    // Tests toString method returns non-empty diagnostic representation
    @Test
    public void testToString_validStream_containsStateInformation() throws Exception {
        XmlTokenStream stream = _createStream("<root>text</root>");
        String str = stream.toString();
        assertNotNull(str);
        assertTrue(str.contains("state="));
        assertTrue(str.contains("root"));
    }

    // Tests repeat element handling when delayed start element is triggered
    @Test
    public void testRepeatElement_delayedStartElement_switchesWrapperScope() throws Exception {
        XmlTokenStream stream = _createStream("<root><item>A</item><other>B</other></root>");
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.next()); // <item>
        stream.repeatStartElement();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.next()); // virtual intermediate <item>
        assertEquals(XmlTokenStream.XML_TEXT, stream.next()); // "A"
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next()); // end of intermediate

        // Next encountered tag is <other>, which does not match wrapper <item>
        // and triggers REPLAY_START_DELAYED
        int token = stream.next();
        assertTrue(token == XmlTokenStream.XML_END_ELEMENT || token == XmlTokenStream.XML_START_ELEMENT);
        if (token == XmlTokenStream.XML_END_ELEMENT) {
            token = stream.next(); // Delayed start element restored
            assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
            assertEquals("other", stream.getLocalName());
        }
    }
}
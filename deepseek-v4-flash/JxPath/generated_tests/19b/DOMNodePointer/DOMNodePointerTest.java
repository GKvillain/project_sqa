package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import java.util.Locale;

public class DOMNodePointerTest {

    private Document document;
    private Element element;
    private Text text;
    private Comment comment;
    private ProcessingInstruction pi;
    private DOMNodePointer elementPointer;
    private DOMNodePointer textPointer;
    private DOMNodePointer piPointer;
    private DOMNodePointer documentPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        element = document.createElementNS(null, "root");
        document.appendChild(element);
        text = document.createTextNode("text content");
        element.appendChild(text);
        comment = document.createComment("comment data");
        element.appendChild(comment);
        pi = document.createProcessingInstruction("target", "data");
        element.appendChild(pi);

        elementPointer = new DOMNodePointer(element, Locale.ENGLISH);
        textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        documentPointer = new DOMNodePointer(document, Locale.ENGLISH);
    }

    // Test testNode with null test -> true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        assertTrue(DOMNodePointer.testNode(element, null));
    }

    // Test NodeNameTest wildcard without prefix -> true
    @Test
    public void testTestNode_NodeNameTest_wildcardNoPrefix_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null);
        assertTrue(DOMNodePointer.testNode(element, test));
    }

    // Test NodeNameTest wildcard with prefix and matching namespace (nodeNS==null, namespaceURI==null) -> true
    @Test
    public void testTestNode_NodeNameTest_wildcardWithPrefix_matchingNullNS_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName("p", "*"), null);
        assertTrue(DOMNodePointer.testNode(element, test));
    }

    // Test NodeNameTest wildcard with prefix and non-matching namespace -> false
    @Test
    public void testTestNode_NodeNameTest_wildcardWithPrefix_wrongNamespace_returnsFalse() throws Exception {
        // Create element with namespace
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        Element elem = doc.createElementNS("http://ns", "p:root");
        doc.appendChild(elem);
        NodeNameTest test = new NodeNameTest(new QName("p", "*"), "http://other");
        assertFalse(DOMNodePointer.testNode(elem, test));
    }

    // Test NodeNameTest exact name match (no namespace) -> true
    @Test
    public void testTestNode_NodeNameTest_exactName_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "root"), null);
        assertTrue(DOMNodePointer.testNode(element, test));
    }

    // Test NodeNameTest exact name mismatch -> false
    @Test
    public void testTestNode_NodeNameTest_exactNameMismatch_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName(null, "wrong"), null);
        assertFalse(DOMNodePointer.testNode(element, test));
    }

    // Test NodeNameTest on non-element node -> false
    @Test
    public void testTestNode_NodeNameTest_nonElement_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName(null, "root"), null);
        assertFalse(DOMNodePointer.testNode(text, test));
    }

    // Test NodeTypeTest NODE -> true for all types
    @Test
    public void testTestNode_NodeTypeTest_node_returnsTrue() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(element, test));
        assertTrue(DOMNodePointer.testNode(text, test));
        assertTrue(DOMNodePointer.testNode(comment, test));
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    // Test NodeTypeTest TEXT -> true only for text and CDATA
    @Test
    public void testTestNode_NodeTypeTest_text_returnsTrueForText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(text, test));
        assertFalse(DOMNodePointer.testNode(element, test));
        assertFalse(DOMNodePointer.testNode(comment, test));
    }

    // Test NodeTypeTest COMMENT -> true for comment
    @Test
    public void testTestNode_NodeTypeTest_comment_returnsTrue() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, test));
        assertFalse(DOMNodePointer.testNode(element, test));
    }

    // Test NodeTypeTest PI -> true for processing instruction
    @Test
    public void testTestNode_NodeTypeTest_pi_returnsTrue() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, test));
        assertFalse(DOMNodePointer.testNode(element, test));
    }

    // Test ProcessingInstructionTest with matching target -> true
    @Test
    public void testTestNode_ProcessingInstructionTest_targetMatch_returnsTrue() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    // Test ProcessingInstructionTest with non-matching target -> false
    @Test
    public void testTestNode_ProcessingInstructionTest_targetMismatch_returnsFalse() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(pi, test));
    }

    // Test getNamespaceURI with null prefix -> default namespace (null here)
    @Test
    public void testGetNamespaceURI_nullPrefix_returnsDefaultNamespace() {
        assertNull(elementPointer.getNamespaceURI((String) null));
    }

    // Test getNamespaceURI with "xml" prefix -> XML namespace
    @Test
    public void testGetNamespaceURI_xmlPrefix_returnsXMLNamespace() {
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, elementPointer.getNamespaceURI("xml"));
    }

    // Test getNamespaceURI with "xmlns" prefix -> XMLNS namespace
    @Test
    public void testGetNamespaceURI_xmlnsPrefix_returnsXMLNSNamespace() {
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, elementPointer.getNamespaceURI("xmlns"));
    }

    // Test getNamespaceURI with known prefix from attribute
    @Test
    public void testGetNamespaceURI_knownPrefix_returnsNamespace() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        Element elem = doc.createElementNS(null, "test");
        elem.setAttribute("xmlns:my", "http://example.com");
        doc.appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("http://example.com", ptr.getNamespaceURI("my"));
    }

    // Test getNamespaceURI with unknown prefix -> null
    @Test
    public void testGetNamespaceURI_unknownPrefix_returnsNull() {
        assertNull(elementPointer.getNamespaceURI("unknown"));
    }

    // Test getDefaultNamespaceURI when xmlns attribute present
    @Test
    public void testGetDefaultNamespaceURI_withXmlns_returnsNamespace() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        Element elem = doc.createElementNS(null, "test");
        elem.setAttribute("xmlns", "http://default.example.com");
        doc.appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("http://default.example.com", ptr.getDefaultNamespaceURI());
    }

    // Test getDefaultNamespaceURI when no xmlns -> null
    @Test
    public void testGetDefaultNamespaceURI_noXmlns_returnsNull() {
        assertNull(elementPointer.getDefaultNamespaceURI());
    }

    // Test setValue on text node with non-empty string -> updates value
    @Test
    public void testSetValue_textNode_setString_updatesNodeValue() {
        DOMNodePointer ptr = new DOMNodePointer(text, Locale.ENGLISH);
        ptr.setValue("new text");
        assertEquals("new text", text.getNodeValue());
    }

    // Test setValue on text node with empty string -> removes node
    @Test
    public void testSetValue_textNode_setEmptyString_removesNode() {
        Node parent = text.getParentNode();
        DOMNodePointer ptr = new DOMNodePointer(text, Locale.ENGLISH);
        ptr.setValue("");
        assertNull(text.getParentNode());
        assertEquals(0, parent.getChildNodes().getLength());
    }

    // Test asPath for element without namespace
    @Test
    public void testAsPath_elementNoNamespace_returnsPath() {
        assertEquals("/root[1]", elementPointer.asPath());
    }

    // Test asPath for text node
    @Test
    public void testAsPath_textNode_returnsPath() {
        assertEquals("/text()[1]", textPointer.asPath());
    }

    // Test asPath for processing instruction
    @Test
    public void testAsPath_pi_returnsPath() {
        assertEquals("/processing-instruction('target')[1]", piPointer.asPath());
    }

    // Test asPath for document node -> empty
    @Test
    public void testAsPath_documentNode_returnsEmpty() {
        assertEquals("", documentPointer.asPath());
    }

    // Test asPath when id is set
    @Test
    public void testAsPath_withId_returnsIdPath() {
        DOMNodePointer ptr = new DOMNodePointer(element, Locale.ENGLISH, "myId");
        assertEquals("id('myId')", ptr.asPath());
    }

    // Test getValue on comment node
    @Test
    public void testGetValue_commentNode_returnsTrimmedData() {
        assertEquals("comment data", new DOMNodePointer(comment, Locale.ENGLISH).getValue());
    }

    // Test getValue on text node
    @Test
    public void testGetValue_textNode_returnsTrimmedText() {
        assertEquals("text content", textPointer.getValue());
    }

    // Test getPointerByID with non-existent id -> NullPointer
    @Test
    public void testGetPointerByID_nonExistentId_returnsNullPointer() {
        Pointer result = documentPointer.getPointerByID(null, "dummy");
        assertTrue(result instanceof NullPointer);
        assertEquals(new NullPointer(Locale.ENGLISH, "dummy"), result);
    }
}
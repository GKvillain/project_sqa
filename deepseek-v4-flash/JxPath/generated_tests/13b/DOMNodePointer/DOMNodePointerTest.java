package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;

public class DOMNodePointerTest {

    private Document document;
    private Element root;
    private Element child;
    private Text textNode;
    private Comment comment;
    private ProcessingInstruction pi;
    private DOMNodePointer rootPointer;
    private DOMNodePointer childPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        root = document.createElement("root");
        document.appendChild(root);
        child = document.createElement("child");
        root.appendChild(child);
        textNode = document.createTextNode("text content");
        child.appendChild(textNode);
        comment = document.createComment("comment data");
        root.appendChild(comment);
        pi = document.createProcessingInstruction("target", "data");
        root.appendChild(pi);

        rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        childPointer = new DOMNodePointer(child, Locale.ENGLISH);
    }

    // ---- testNode ----
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    @Test
    public void testTestNode_nodeNameTest_match_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "root"));
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNode_nodeNameTest_noMatch_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName(null, "nonexistent"));
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNode_nodeNameTest_wildcardNoPrefix_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null, true);
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNode_nodeTypeTest_node_returnsTrueForElement() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNode_nodeTypeTest_text_returnsTrueForText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, test));
    }

    @Test
    public void testTestNode_nodeTypeTest_comment_returnsTrueForComment() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, test));
    }

    @Test
    public void testTestNode_nodeTypeTest_pi_returnsTrueForPI() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_processingInstructionTest_match_returnsTrue() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_processingInstructionTest_noMatch_returnsFalse() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(pi, test));
    }

    // ---- getNamespaceURI ----
    @Test
    public void testGetNamespaceURI_instance_withExplicitNamespace_returnsURI() throws Exception {
        Element nsElem = document.createElementNS("http://example.com/ns", "pre:local");
        root.appendChild(nsElem);
        DOMNodePointer ptr = new DOMNodePointer(nsElem, Locale.ENGLISH);
        assertEquals("http://example.com/ns", ptr.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURI_instance_noNamespace_returnsNull() {
        DOMNodePointer ptr = new DOMNodePointer(child, Locale.ENGLISH);
        assertNull(ptr.getNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURI_withDefaultXmlns_returnsURI() {
        Element elem = document.createElement("elem");
        elem.setAttribute("xmlns", "http://default.com");
        root.appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("http://default.com", ptr.getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURI_noDefault_returnsNull() {
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        assertNull(ptr.getDefaultNamespaceURI());
    }

    // ---- asPath ----
    @Test
    public void testAsPath_element_returnsPathWithIndex() {
        assertEquals("/root[1]", rootPointer.asPath());
    }

    @Test
    public void testAsPath_childElement_returnsPathWithIndex() {
        assertEquals("/child[1]", childPointer.asPath());
    }

    @Test
    public void testAsPath_textNode_returnsPathWithIndex() {
        DOMNodePointer textPointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        assertEquals("/text()[1]", textPointer.asPath());
    }

    @Test
    public void testAsPath_pi_returnsPathWithIndex() {
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("/processing-instruction('target')[1]", piPointer.asPath());
    }

    @Test
    public void testAsPath_withId_returnsIdExpression() {
        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.ENGLISH, "myId");
        assertEquals("id('myId')", idPointer.asPath());
    }

    // ---- setValue ----
    @Test
    public void testSetValue_stringOnTextNode_replacesContent() {
        DOMNodePointer textPointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        textPointer.setValue("new text");
        assertEquals("new text", textNode.getNodeValue());
    }

    @Test
    public void testSetValue_nodeOnElement_replacesChildren() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document newDoc = db.newDocument();
        Element newChild = newDoc.createElement("newChild");
        newChild.appendChild(newDoc.createTextNode("inner"));

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        rootPtr.setValue(newChild);

        assertEquals(1, root.getChildNodes().getLength());
        Node firstChild = root.getFirstChild();
        assertEquals(Node.TEXT_NODE, firstChild.getNodeType());
        assertEquals("inner", firstChild.getNodeValue());
    }

    // ---- getValue ----
    @Test
    public void testGetValue_element_returnsConcatenatedText() {
        // root contains child element (which has text node), comment, pi
        // expected concatenated text is "text content"
        assertEquals("text content", rootPointer.getValue());
    }

    @Test
    public void testGetValue_comment_returnsData() {
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment data", commentPointer.getValue());
    }

    // ---- isLanguage ----
    @Test
    public void testIsLanguage_withXmlLang_returnsCorrectly() {
        root.setAttribute("xml:lang", "en");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        assertTrue(ptr.isLanguage("en"));
        assertTrue(ptr.isLanguage("EN"));
        assertTrue(ptr.isLanguage("en-US"));
        assertFalse(ptr.isLanguage("fr"));
    }

    // ---- static helpers ----
    @Test
    public void testGetPrefix_static_returnsPrefixOrNull() throws Exception {
        Element nsElem = document.createElementNS("http://ns", "pre:local");
        assertEquals("pre", DOMNodePointer.getPrefix(nsElem));
        assertNull(DOMNodePointer.getPrefix(root));
    }

    @Test
    public void testGetLocalName_static_returnsLocalPartOrNodeName() throws Exception {
        Element nsElem = document.createElementNS("http://ns", "pre:local");
        assertEquals("local", DOMNodePointer.getLocalName(nsElem));
        assertEquals("root", DOMNodePointer.getLocalName(root));
    }

    @Test
    public void testGetNamespaceURI_static_withExplicitNS_returnsURI() throws Exception {
        Element nsElem = document.createElementNS("http://example.com/ns", "pre:local");
        root.appendChild(nsElem);
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(nsElem));
    }

    @Test
    public void testGetNamespaceURI_static_withXmlnsAttr_returnsURI() {
        Element elem = document.createElement("elem");
        elem.setAttribute("xmlns", "http://example.com/ns2");
        root.appendChild(elem);
        assertEquals("http://example.com/ns2", DOMNodePointer.getNamespaceURI(elem));
    }

    // ---- equals ----
    @Test
    public void testEquals_sameNode_returnsTrueAndDifferentNode_returnsFalse() {
        DOMNodePointer ptr1 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer ptr3 = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(ptr1.equals(ptr2));
        assertFalse(ptr1.equals(ptr3));
    }
}
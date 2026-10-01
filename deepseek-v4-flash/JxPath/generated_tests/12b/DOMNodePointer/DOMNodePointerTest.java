package org.apache.commons.jxpath.ri.model.dom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;

import org.w3c.dom.*;

import java.util.Locale;

public class DOMNodePointerTest {
    private Document doc;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        doc = factory.newDocumentBuilder().newDocument();
    }

    @After
    public void tearDown() {
        doc = null;
    }

    // helper: create pointer without parent
    private DOMNodePointer createPointer(Node node) {
        return new DOMNodePointer(node, Locale.ENGLISH);
    }

    // helper: create pointer with parent
    private DOMNodePointer createPointer(DOMNodePointer parent, Node node) {
        return new DOMNodePointer(parent, node);
    }

    // ===== testNode tests =====

    @Test
    public void testTestNode_nullTest_returnsTrue() {
        assertTrue(DOMNodePointer.testNode(doc, null));
    }

    @Test
    public void testTestNode_NodeNameTestExactMatch_returnsTrue() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        NodeNameTest test = new NodeNameTest(new QName(null, "elem"), null);
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_NodeNameTestNameMismatch_returnsFalse() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        NodeNameTest test = new NodeNameTest(new QName(null, "other"), null);
        assertFalse(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_NodeNameTestNamespaceMismatch_returnsFalse() {
        Element elem = doc.createElementNS("http://example.com", "ex:elem");
        doc.appendChild(elem);
        NodeNameTest test = new NodeNameTest(new QName(null, "elem"), null);
        assertFalse(DOMNodePointer.testNode(elem, test));
    }

    // This test detects the defect: node() should match text nodes
    @Test
    public void testTestNode_NodeTypeTestNodeOnTextNode_returnsTrue() {
        Node text = doc.createTextNode("text");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(text, test));
    }

    @Test
    public void testTestNode_NodeTypeTextOnTextNode_returnsTrue() {
        Node text = doc.createTextNode("text");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(text, test));
    }

    @Test
    public void testTestNode_ProcessingInstructionTestMatch_returnsTrue() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        doc.appendChild(pi);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_ProcessingInstructionTestMismatch_returnsFalse() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        doc.appendChild(pi);
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(pi, test));
    }

    // ===== getNamespaceURI (static) tests =====

    @Test
    public void testGetNamespaceURI_ElementWithNamespace_returnsURI() {
        Element elem = doc.createElementNS("http://example.com", "ex:elem");
        doc.appendChild(elem);
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(elem));
    }

    // This test detects the defect: getNamespaceURI should not cast to Element unconditionally
    @Test
    public void testGetNamespaceURI_TextNode_doesNotThrow() {
        Node text = doc.createTextNode("text");
        assertNull(DOMNodePointer.getNamespaceURI(text));
    }

    // This test detects the defect: same for Attr nodes
    @Test
    public void testGetNamespaceURI_AttrNode_doesNotThrow() {
        Element elem = doc.createElement("elem");
        elem.setAttribute("attr", "value");
        Attr attr = elem.getAttributeNode("attr");
        assertNull(DOMNodePointer.getNamespaceURI(attr));
    }

    @Test
    public void testGetNamespaceURI_DocumentNode_returnsURI() {
        Element elem = doc.createElementNS("http://example.com", "ex:elem");
        doc.appendChild(elem);
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(doc));
    }

    // ===== instance methods =====

    @Test
    public void testGetNamespaceURI_Instance_returnsURI() {
        Element elem = doc.createElementNS("http://example.com", "ex:elem");
        doc.appendChild(elem);
        DOMNodePointer ptr = createPointer(elem);
        assertEquals("http://example.com", ptr.getNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURI_ElementWithXmlns_returnsURI() {
        Element root = doc.createElement("root");
        root.setAttribute("xmlns", "http://defaultns");
        doc.appendChild(root);
        DOMNodePointer ptr = createPointer(root);
        assertEquals("http://defaultns", ptr.getDefaultNamespaceURI());
    }

    // ===== asPath tests =====

    @Test
    public void testAsPath_WithId_returnsIdPath() {
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH, "myid");
        assertEquals("id('myid')", ptr.asPath());
    }

    @Test
    public void testAsPath_ElementWithParent_returnsPath() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer rootPtr = createPointer(root);
        DOMNodePointer childPtr = createPointer(rootPtr, child);
        assertEquals("/child[1]", childPtr.asPath());
    }

    @Test
    public void testAsPath_TextNode_returnsPath() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Node text = doc.createTextNode("text");
        root.appendChild(text);
        DOMNodePointer rootPtr = createPointer(root);
        DOMNodePointer textPtr = createPointer(rootPtr, text);
        assertEquals("/text()[1]", textPtr.asPath());
    }

    @Test
    public void testAsPath_ProcessingInstruction_returnsPath() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        root.appendChild(pi);
        DOMNodePointer rootPtr = createPointer(root);
        DOMNodePointer piPtr = createPointer(rootPtr, pi);
        assertEquals("/processing-instruction('target')[1]", piPtr.asPath());
    }

    // ===== isLeaf tests =====

    @Test
    public void testIsLeaf_hasChildren_returnsFalse() {
        Element root = doc.createElement("root");
        root.appendChild(doc.createElement("child"));
        DOMNodePointer ptr = createPointer(root);
        assertFalse(ptr.isLeaf());
    }

    @Test
    public void testIsLeaf_noChildren_returnsTrue() {
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = createPointer(elem);
        assertTrue(ptr.isLeaf());
    }

    // ===== getValue tests =====

    @Test
    public void testGetValue_CommentNode_returnsData() {
        Comment comment = doc.createComment("comment data");
        doc.appendChild(comment);
        DOMNodePointer ptr = createPointer(comment);
        assertEquals("comment data", ptr.getValue());
    }

    @Test
    public void testGetValue_ElementWithText_returnsTrimmedText() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        elem.appendChild(doc.createTextNode("  text  "));
        DOMNodePointer ptr = createPointer(elem);
        assertEquals("text", ptr.getValue());
    }

    // ===== setValue test =====

    @Test
    public void testSetValue_Element_SetsTextContent() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        DOMNodePointer ptr = createPointer(elem);
        ptr.setValue("new content");
        Node child = elem.getFirstChild();
        assertNotNull(child);
        assertEquals(Node.TEXT_NODE, child.getNodeType());
        assertEquals("new content", child.getNodeValue());
    }

    // ===== remove tests =====

    @Test(expected = JXPathException.class)
    public void testRemove_RootNode_throwsException() {
        DOMNodePointer ptr = createPointer(doc);
        ptr.remove();
    }

    @Test
    public void testRemove_ChildNode_removesNode() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer rootPtr = createPointer(root);
        DOMNodePointer childPtr = createPointer(rootPtr, child);
        childPtr.remove();
        assertNull(root.getFirstChild());
    }

    // ===== getName tests =====

    @Test
    public void testGetName_Element_returnsQName() {
        Element elem = doc.createElementNS("http://example.com", "ex:elem");
        doc.appendChild(elem);
        DOMNodePointer ptr = createPointer(elem);
        QName name = ptr.getName();
        assertEquals("ex", name.getPrefix());
        assertEquals("elem", name.getName());
    }

    @Test
    public void testGetName_PI_returnsQName() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        doc.appendChild(pi);
        DOMNodePointer ptr = createPointer(pi);
        QName name = ptr.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    // ========== NEW TEST CASES สำหรับส่วนที่ยังไม่ถูกครอบคลุม ==========

    // ----- testNode เพิ่มเติม -----

    @Test
    public void testTestNode_NodeTypeTestCommentOnCommentNode_returnsTrue() {
        Comment comment = doc.createComment("test");
        doc.appendChild(comment);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, test));
    }

    @Test
    public void testTestNode_NodeTypeTestPiOnPiNode_returnsTrue() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        doc.appendChild(pi);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_NodeTypeTestDocumentOnDocumentNode_returnsTrue() {
        // Append an element so document has at least one child (though testNode checks node type directly)
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_DOCUMENT);
        assertTrue(DOMNodePointer.testNode(doc, test));
    }

    @Test
    public void testTestNode_NodeNameTestWithPrefixMatch_returnsTrue() {
        Element elem = doc.createElement("p:elem");
        // Use a prefix in the test, but note that without namespace, prefix is just part of the local name
        NodeNameTest test = new NodeNameTest(new QName("p", "elem"), null);
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    // ----- getNamespaceURI เพิ่มเติม -----

    @Test
    public void testGetNamespaceURI_ElementWithoutNamespace_returnsNull() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        assertNull(DOMNodePointer.getNamespaceURI(elem));
    }

    // ----- getDefaultNamespaceURI เพิ่มเติม -----

    @Test
    public void testGetDefaultNamespaceURI_ElementWithoutXmlns_returnsNull() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer ptr = createPointer(root);
        // If no xmlns attribute, getDefaultNamespaceURI may return null or empty string; we assume null.
        assertNull(ptr.getDefaultNamespaceURI());
    }

    // ----- asPath เพิ่มเติม -----

    @Test
    public void testAsPath_CommentNode_returnsPath() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Comment comment = doc.createComment("test comment");
        root.appendChild(comment);
        DOMNodePointer rootPtr = createPointer(root);
        DOMNodePointer commentPtr = createPointer(rootPtr, comment);
        // Expected path: /comment()[1]
        assertEquals("/comment()[1]", commentPtr.asPath());
    }

    @Test
    public void testAsPath_DocumentNode_returnsPath() {
        // Ensure document has a child so it's not empty? asPath on document root should be "/"
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        DOMNodePointer docPtr = createPointer(doc);
        assertEquals("/", docPtr.asPath());
    }

    // ----- isLeaf เพิ่มเติม -----

    @Test
    public void testIsLeaf_TextNode_returnsTrue() {
        Node text = doc.createTextNode("text");
        DOMNodePointer ptr = createPointer(text);
        assertTrue(ptr.isLeaf());
    }

    @Test
    public void testIsLeaf_AttrNode_returnsTrue() {
        Element elem = doc.createElement("elem");
        elem.setAttribute("attr", "value");
        Attr attr = elem.getAttributeNode("attr");
        DOMNodePointer ptr = createPointer(attr);
        // Attr nodes are considered leaves
        assertTrue(ptr.isLeaf());
    }

    // ----- getValue เพิ่มเติม -----

    @Test
    public void testGetValue_TextNode_returnsNodeValue() {
        Node text = doc.createTextNode("text content");
        DOMNodePointer ptr = createPointer(text);
        assertEquals("text content", ptr.getValue());
    }

    @Test
    public void testGetValue_PINode_returnsData() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "pi data");
        doc.appendChild(pi);
        DOMNodePointer ptr = createPointer(pi);
        assertEquals("pi data", ptr.getValue());
    }

    @Test
    public void testGetValue_AttrNode_returnsValue() {
        Element elem = doc.createElement("elem");
        elem.setAttribute("attr", "attrValue");
        Attr attr = elem.getAttributeNode("attr");
        DOMNodePointer ptr = createPointer(attr);
        assertEquals("attrValue", ptr.getValue());
    }

    // ----- setValue เพิ่มเติม -----

    @Test
    public void testSetValue_TextNode_setsNodeValue() {
        Node text = doc.createTextNode("old");
        DOMNodePointer ptr = createPointer(text);
        ptr.setValue("new");
        assertEquals("new", text.getNodeValue());
    }

    @Test
    public void testSetValue_PINode_setsData() {
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "old");
        doc.appendChild(pi);
        DOMNodePointer ptr = createPointer(pi);
        ptr.setValue("new data");
        assertEquals("new data", pi.getData());
    }

    // ----- remove เพิ่มเติม -----

    @Test
    public void testRemove_AttrNode_removesAttribute() {
        Element elem = doc.createElement("elem");
        elem.setAttribute("attr", "value");
        Attr attr = elem.getAttributeNode("attr");
        DOMNodePointer parentPtr = createPointer(elem);
        DOMNodePointer attrPtr = createPointer(parentPtr, attr);
        attrPtr.remove();
        assertNull(elem.getAttributeNode("attr"));
    }

    // ----- getName เพิ่มเติม -----

    @Test
    public void testGetName_AttrNode_returnsQName() {
        Element elem = doc.createElement("elem");
        elem.setAttribute("attr", "value");
        Attr attr = elem.getAttributeNode("attr");
        DOMNodePointer ptr = createPointer(attr);
        QName name = ptr.getName();
        assertNull(name.getPrefix());
        assertEquals("attr", name.getName());
    }

    @Test
    public void testGetName_CommentNode_returnsQName() {
        Comment comment = doc.createComment("test");
        doc.appendChild(comment);
        DOMNodePointer ptr = createPointer(comment);
        QName name = ptr.getName();
        // For comment nodes, getName() typically returns null or a special QName; we assume null.
        assertNull(name);
    }

    // ----- hashCode เพิ่มเติม -----

    @Test
    public void testHashCode_consistent() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        DOMNodePointer ptr1 = createPointer(elem);
        DOMNodePointer ptr2 = createPointer(elem);
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }
}
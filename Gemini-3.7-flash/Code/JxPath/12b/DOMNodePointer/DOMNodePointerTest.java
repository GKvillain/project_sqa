package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private Document doc;
    private DocumentBuilder db;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        db = factory.newDocumentBuilder();
        doc = db.newDocument();
    }

    // Tests testNode with null test returning true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element element = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest matching element name and namespace
    @Test
    public void testTestNode_matchingNodeNameAndNamespace_returnsTrue() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("ns", "test"), "http://example.com/ns");
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with NodeNameTest wildcard and null prefix
    @Test
    public void testTestNode_wildcardWithoutPrefix_returnsTrue() {
        Element element = doc.createElement("item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with NodeNameTest wildcard with namespace match
    @Test
    public void testTestNode_wildcardWithNamespace_returnsTrue() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("ns", "*"), "http://example.com/ns");
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with NodeNameTest on non-element node returning false
    @Test
    public void testTestNode_nodeNameTestOnTextNode_returnsFalse() {
        Text text = doc.createTextNode("sample");
        DOMNodePointer pointer = new DOMNodePointer(text, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("sample"));
        assertFalse(pointer.testNode(test));
    }

    // Tests testNode with NodeTypeTest for element, text, comment, and processing instruction
    @Test
    public void testTestNode_nodeTypeTest_returnsExpectedResult() {
        Element element = doc.createElement("elem");
        Text text = doc.createTextNode("text");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
    }

    // Tests testNode with ProcessingInstructionTest matching and mismatching target
    @Test
    public void testTestNode_processingInstructionTest_matchesCorrectly() {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "href=\"style.css\"");
        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("xml-stylesheet");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("other");

        assertTrue(DOMNodePointer.testNode(pi, matchingTest));
        assertFalse(DOMNodePointer.testNode(pi, mismatchTest));
    }

    // Tests getName for Element and ProcessingInstruction nodes
    @Test
    public void testGetName_elementAndPI_returnsCorrectQName() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:element");
        DOMNodePointer elementPointer = new DOMNodePointer(element, Locale.getDefault());
        assertEquals(new QName("ns", "element"), elementPointer.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());
        assertEquals(new QName(null, "myTarget"), piPointer.getName());
    }

    // Tests getNamespaceURI with prefix resolution
    @Test
    public void testGetNamespaceURI_withPrefix_resolvesNamespace() {
        Element root = doc.createElement("root");
        root.setAttribute("xmlns:foo", "http://example.com/foo");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.getDefault());
        assertEquals("http://example.com/foo", pointer.getNamespaceURI("foo"));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI resolution
    @Test
    public void testGetDefaultNamespaceURI_withDefaultNs_returnsUri() {
        Element root = doc.createElement("root");
        root.setAttribute("xmlns", "http://example.com/default");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        assertEquals("http://example.com/default", pointer.getDefaultNamespaceURI());
    }

    // Tests getValue on comment, text, element, and CDATA
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectString() {
        Comment comment = doc.createComment(" my comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.getDefault());
        assertEquals("my comment", commentPointer.getValue());

        Element root = doc.createElement("root");
        Text text = doc.createTextNode(" Hello ");
        CDATASection cdata = doc.createCDATASection(" World ");
        root.appendChild(text);
        root.appendChild(cdata);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        assertEquals("HelloWorld", rootPointer.getValue());
    }

    // Tests setValue replacing children of an element with text
    @Test
    public void testSetValue_elementNode_setsTextChild() {
        Element elem = doc.createElement("elem");
        elem.appendChild(doc.createElement("child"));
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.getDefault());

        pointer.setValue("new value");
        assertEquals("new value", pointer.getValue());
    }

    // Tests setValue on Text node
    @Test
    public void testSetValue_textNode_updatesNodeValue() {
        Element elem = doc.createElement("elem");
        Text text = doc.createTextNode("initial");
        elem.appendChild(text);
        DOMNodePointer pointer = new DOMNodePointer(text, Locale.getDefault());

        pointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());
    }

    // Tests setValue on Text node with empty string removes the text node
    @Test
    public void testSetValue_textNodeEmpty_removesNode() {
        Element elem = doc.createElement("elem");
        Text text = doc.createTextNode("initial");
        elem.appendChild(text);
        DOMNodePointer pointer = new DOMNodePointer(text, Locale.getDefault());

        pointer.setValue("");
        assertNull(text.getParentNode());
    }

    // Tests createAttribute on Element node
    @Test
    public void testCreateAttribute_element_createsAttribute() {
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        DOMNodePointer rootPointer = new DOMNodePointer(doc, Locale.getDefault());
        DOMNodePointer pointer = new DOMNodePointer(rootPointer, elem);
        JXPathContext context = JXPathContext.newContext(doc);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("attr"));
        assertNotNull(attrPointer);
        assertTrue(elem.hasAttribute("attr"));
    }

    // Tests remove method for non-root and root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.getDefault());

        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove method throwing exception on root node
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() {
        Element root = doc.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        rootPointer.remove();
    }

    // Tests asPath with id, element, text, and processing instruction
    @Test
    public void testAsPath_variousNodes_returnsCorrectXPath() {
        Element root = doc.createElement("root");
        Element child1 = doc.createElement("child");
        Element child2 = doc.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);
        Text text = doc.createTextNode("text");
        child2.appendChild(text);
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        child2.appendChild(pi);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer child2Pointer = new DOMNodePointer(rootPointer, child2);
        DOMNodePointer textPointer = new DOMNodePointer(child2Pointer, text);
        DOMNodePointer piPointer = new DOMNodePointer(child2Pointer, pi);

        assertEquals("/child[2]", child2Pointer.asPath());
        assertEquals("/child[2]/text()[1]", textPointer.asPath());
        assertEquals("/child[2]/processing-instruction('target')[1]", piPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.getDefault(), "item'1");
        assertEquals("id('item&apos;1')", idPointer.asPath());
    }

    // Tests isLanguage and findEnclosingAttribute
    @Test
    public void testIsLanguage_xmlLangAttribute_checksLanguageCorrectly() {
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.getDefault());
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));
    }

    // Tests compareChildNodePointers between attributes and child elements
    @Test
    public void testCompareChildNodePointers_orderVerification() {
        Element root = doc.createElement("root");
        root.setAttribute("a", "1");
        root.setAttribute("b", "2");
        Element child1 = doc.createElement("child1");
        Element child2 = doc.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer pChild1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(-1, rootPointer.compareChildNodePointers(pChild1, pChild2));
        assertEquals(1, rootPointer.compareChildNodePointers(pChild2, pChild1));
        assertEquals(0, rootPointer.compareChildNodePointers(pChild1, pChild1));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_validatesEquality() {
        Element elem1 = doc.createElement("elem");
        Element elem2 = doc.createElement("elem");

        DOMNodePointer ptr1a = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr1b = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.getDefault());

        assertEquals(ptr1a, ptr1b);
        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("string"));
    }

    // Tests boolean properties: isLeaf, isActual, isCollection, getLength
    @Test
    public void testBasicProperties_returnsExpectedValues() {
        Element elem = doc.createElement("elem");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.getDefault());

        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        elem.appendChild(doc.createElement("child"));
        assertFalse(pointer.isLeaf());
    }
}
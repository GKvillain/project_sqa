package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
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

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    // Tests testNode with null NodeTest returns true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element element = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
        assertTrue(DOMNodePointer.testNode(element, null));
    }

    // Tests testNode with NodeNameTest matching element and wildcard
    @Test
    public void testTestNode_nodeNameTest_elementMatchingAndWildcard() {
        Element element = document.createElementNS("http://example.com/ns", "ns:testElem");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        NodeNameTest testExact = new NodeNameTest(new QName("ns", "testElem"), "http://example.com/ns");
        assertTrue(pointer.testNode(testExact));

        NodeNameTest testMismatchName = new NodeNameTest(new QName("ns", "otherElem"), "http://example.com/ns");
        assertFalse(pointer.testNode(testMismatchName));

        NodeNameTest testWildcardNoPrefix = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(testWildcardNoPrefix));

        Text text = document.createTextNode("text");
        assertFalse(DOMNodePointer.testNode(text, testExact));
    }

    // Tests testNode with NodeTypeTest for node, text, comment, and processing instruction
    @Test
    public void testTestNode_nodeTypeTest_variousNodeTypes() {
        Element element = document.createElement("elem");
        Text text = document.createTextNode("sample");
        Comment comment = document.createComment("a comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(999)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchingAndNonMatching() {
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("target");
        ProcessingInstructionTest nonMatchingTest = new ProcessingInstructionTest("otherTarget");

        assertTrue(DOMNodePointer.testNode(pi, matchingTest));
        assertFalse(DOMNodePointer.testNode(pi, nonMatchingTest));

        Element element = document.createElement("elem");
        assertFalse(DOMNodePointer.testNode(element, matchingTest));
    }

    // Tests getName for Element, ProcessingInstruction, and other node types
    @Test
    public void testGetName_variousNodes_returnsExpectedQName() {
        Element element = document.createElementNS("http://example.com/ns", "p:myElem");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("p", name.getPrefix());
        assertEquals("myElem", name.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("targetPI", piName.getName());

        Document doc = document;
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.ENGLISH);
        QName docName = docPointer.getName();
        assertNull(docName.getPrefix());
        assertNull(docName.getName());
    }

    // Tests static helpers getPrefix, getLocalName, and getNamespaceURI
    @Test
    public void testStaticHelpers_getPrefixLocalNameAndNamespaceURI() {
        Element element = document.createElementNS("http://example.com/ns", "p:localPart");
        assertEquals("p", DOMNodePointer.getPrefix(element));
        assertEquals("localPart", DOMNodePointer.getLocalName(element));
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(element));

        Element plainElement = document.createElement("simple");
        assertNull(DOMNodePointer.getPrefix(plainElement));
        assertEquals("simple", DOMNodePointer.getLocalName(plainElement));
    }

    // Tests getNamespaceURI with prefixes xml, xmlns, declared prefixes, and unknown prefix
    @Test
    public void testGetNamespaceURI_withPrefix_resolvesCorrectly() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:foo", "http://foo.com");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://foo.com", pointer.getNamespaceURI("foo"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI on element and document
    @Test
    public void testGetDefaultNamespaceURI_defaultDeclared_returnsURI() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://default.com");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://default.com", pointer.getDefaultNamespaceURI());

        Element noDefault = document.createElement("noDefault");
        DOMNodePointer noDefaultPointer = new DOMNodePointer(noDefault, Locale.ENGLISH);
        assertNull(noDefaultPointer.getDefaultNamespaceURI());
    }

    // Tests isLeaf, isCollection, getLength, isActual, getBaseValue, and getImmediateNode
    @Test
    public void testBasicProperties_variousMethods_returnExpectedValues() {
        Element element = document.createElement("elem");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        assertTrue(pointer.isLeaf());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isActual());
        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());

        element.appendChild(document.createElement("subElem"));
        assertFalse(pointer.isLeaf());
    }

    // Tests isLanguage when xml:lang is present or inherited
    @Test
    public void testIsLanguage_withLanguageAttribute_matchesPrefix() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("EN-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests getValue on comment, text, CDATA, PI, and element with children
    @Test
    public void testGetValue_variousNodes_returnsTextValue() {
        Comment comment = document.createComment(" sample comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("sample comment", commentPointer.getValue());

        Element element = document.createElement("parent");
        Text text1 = document.createTextNode("Hello ");
        CDATASection cdata = document.createCDATASection("World");
        element.appendChild(text1);
        element.appendChild(cdata);

        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.ENGLISH);
        assertEquals("HelloWorld", elemPointer.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", " some data ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("some data", piPointer.getValue());
    }

    // Tests setValue replacing content on text node and element node
    @Test
    public void testSetValue_textAndElementNodes_updatesValue() {
        Element element = document.createElement("parent");
        Text text = document.createTextNode("old");
        element.appendChild(text);

        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        textPointer.setValue("newText");
        assertEquals("newText", text.getNodeValue());

        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.ENGLISH);
        elemPointer.setValue("replacedContent");
        assertEquals("replacedContent", elemPointer.getValue());
    }

    // Tests asPath with id, element, text, processing instruction, and document
    @Test
    public void testAsPath_variousNodes_returnsExpectedXPath() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);

        Element child1 = document.createElement("child");
        Element child2 = document.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer child2Pointer = new DOMNodePointer(rootPointer, child2);
        assertEquals("/child[2]", child2Pointer.asPath());

        Text text = document.createTextNode("content");
        child2.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(child2Pointer, text);
        assertEquals("/child[2]/text()[1]", textPointer.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "data");
        child2.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(child2Pointer, pi);
        assertEquals("/child[2]/processing-instruction('myTarget')[1]", piPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.ENGLISH, "elemId");
        assertEquals("id('elemId')", idPointer.asPath());

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertEquals("", docPointer.asPath());
    }

    // Tests compareChildNodePointers between children and attributes
    @Test
    public void testCompareChildNodePointers_orderComparisons_returnsCorrectOrdering() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);

        Element child1 = document.createElement("c1");
        Element child2 = document.createElement("c2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer p1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(0, rootPointer.compareChildNodePointers(p1, p1));
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointers(p2, p1));

        root.setAttribute("attr1", "v1");
        Attr attr = root.getAttributeNode("attr1");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);

        assertEquals(-1, rootPointer.compareChildNodePointers(attrPointer, p1));
        assertEquals(1, rootPointer.compareChildNodePointers(p1, attrPointer));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_equalAndUnequalNodes_correctBehavior() {
        Element elem1 = document.createElement("elem");
        Element elem2 = document.createElement("elem");

        DOMNodePointer ptr1a = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr1b = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.ENGLISH);

        assertEquals(ptr1a, ptr1a);
        assertEquals(ptr1a, ptr1b);
        assertNotEquals(ptr1a, ptr2);
        assertNotEquals(ptr1a, null);
        assertNotEquals(ptr1a, "string");

        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests remove method removes node from parent
    @Test
    public void testRemove_childNode_removesSuccessfully() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove method throws JXPathException on root node with no parent
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        rootPointer.remove();
    }

    // Tests getPointerByID returns NullPointer when id is not found
    @Test
    public void testGetPointerByID_notFound_returnsNullPointer() {
        Element root = document.createElement("root");
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);
        Pointer pointer = rootPointer.getPointerByID(context, "missingId");

        assertNotNull(pointer);
        assertTrue(pointer instanceof NullPointer);
    }

    // Tests createAttribute for element node creates attribute correctly
    @Test
    public void testCreateAttribute_unprefixedAttribute_createsAndReturnsPointer() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer attrPointer = rootPointer.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPointer);
        assertTrue(root.hasAttribute("myAttr"));
    }
}
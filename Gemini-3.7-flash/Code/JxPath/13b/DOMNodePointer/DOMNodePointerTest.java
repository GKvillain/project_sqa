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

    // Tests testNode with null test returns true
    @Test
    public void testTestNode_nullNodeTest_returnsTrue() {
        Element element = document.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest matching element local name and wildcard
    @Test
    public void testTestNode_nodeNameTest_matchesCorrectly() {
        Element element = document.createElement("testElement");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        NodeNameTest matchTest = new NodeNameTest(new QName("testElement"));
        assertTrue(pointer.testNode(matchTest));

        NodeNameTest mismatchTest = new NodeNameTest(new QName("otherElement"));
        assertFalse(pointer.testNode(mismatchTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName("*"));
        assertTrue(pointer.testNode(wildcardTest));
    }

    // Tests testNode with NodeTypeTest for element, text, comment, and processing instruction
    @Test
    public void testTestNode_nodeTypeTest_matchesExpectedTypes() {
        Element element = document.createElement("el");
        Text text = document.createTextNode("txt");
        Comment comment = document.createComment("comm");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
    }

    // Tests testNode with ProcessingInstructionTest target matching
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("target1", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.getDefault());

        assertTrue(pointer.testNode(new ProcessingInstructionTest("target1")));
        assertFalse(pointer.testNode(new ProcessingInstructionTest("target2")));
    }

    // Tests getName for element and processing instruction nodes
    @Test
    public void testGetName_elementAndPINodes_returnsExpectedQName() {
        Element element = document.createElementNS("http://example.com/ns", "ns:item");
        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.getDefault());
        QName elemQName = elemPointer.getName();
        assertEquals("ns", elemQName.getPrefix());
        assertEquals("item", elemQName.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "myContent");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());
        QName piQName = piPointer.getName();
        assertNull(piQName.getPrefix());
        assertEquals("myTarget", piQName.getName());
    }

    // Tests getNamespaceURI with standard xml and xmlns prefixes, custom prefixes, and default namespace
    @Test
    public void testGetNamespaceURI_variousPrefixes_returnsCorrectURI() {
        Element root = document.createElementNS("http://example.com/default", "root");
        root.setAttribute("xmlns", "http://example.com/default");
        root.setAttribute("xmlns:custom", "http://example.com/custom");
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());

        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://example.com/custom", pointer.getNamespaceURI("custom"));
        assertEquals("http://example.com/default", pointer.getNamespaceURI(""));
        assertEquals("http://example.com/default", pointer.getNamespaceURI((String) null));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests basic DOMNodePointer properties like isLeaf, isActual, isCollection, getLength
    @Test
    public void testProperties_elementNode_returnsExpectedValues() {
        Element element = document.createElement("parent");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        element.appendChild(document.createElement("child"));
        assertFalse(pointer.isLeaf());
        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());
    }

    // Tests isLanguage when xml:lang is present on current element or parent
    @Test
    public void testIsLanguage_enclosingXmlLang_evaluatesCorrectly() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.getDefault());
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));
    }

    // Tests getValue on comment, text, and element with nested text
    @Test
    public void testGetValue_variousNodes_returnsTextRepresentation() {
        Comment comment = document.createComment(" a comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.getDefault());
        assertEquals("a comment", commentPointer.getValue());

        Element parent = document.createElement("root");
        Text t1 = document.createTextNode("Hello ");
        Text t2 = document.createTextNode("World");
        parent.appendChild(t1);
        parent.appendChild(t2);

        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.getDefault());
        assertEquals("Hello World", parentPointer.getValue());
    }

    // Tests setValue on text node and element node
    @Test
    public void testSetValue_textAndElementNodes_updatesContent() {
        Element element = document.createElement("node");
        document.appendChild(element);
        Text text = document.createTextNode("old");
        element.appendChild(text);

        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.getDefault());
        textPointer.setValue("newText");
        assertEquals("newText", text.getNodeValue());

        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.getDefault());
        elemPointer.setValue("updatedElementText");
        assertEquals("updatedElementText", elemPointer.getValue());
    }

    // Tests asPath with id, element hierarchy, and processing instruction
    @Test
    public void testAsPath_withIdAndElements_returnsXPathString() {
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        assertEquals("/child[1]", childPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.getDefault(), "elemId");
        assertEquals("id('elemId')", idPointer.asPath());
    }

    // Tests remove method on root node and child node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.getDefault());
        childPointer.remove();
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests remove method on root node throws exception
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        rootPointer.remove();
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes() {
        Element element1 = document.createElement("elem");
        Element element2 = document.createElement("elem");

        DOMNodePointer ptr1a = new DOMNodePointer(element1, Locale.getDefault());
        DOMNodePointer ptr1b = new DOMNodePointer(element1, Locale.getDefault());
        DOMNodePointer ptr2 = new DOMNodePointer(element2, Locale.getDefault());

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("notAPointer"));
        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests static helpers getPrefix, getLocalName, and getNamespaceURI
    @Test
    public void testStaticNodeHelpers_prefixedAndLocalName() {
        Element elem = document.createElementNS("http://example.com/ns", "pref:tag");
        assertEquals("pref", DOMNodePointer.getPrefix(elem));
        assertEquals("tag", DOMNodePointer.getLocalName(elem));
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(elem));

        Element simpleElem = document.createElement("plain");
        assertNull(DOMNodePointer.getPrefix(simpleElem));
        assertEquals("plain", DOMNodePointer.getLocalName(simpleElem));
    }

    // Tests compareChildNodePointers between attribute nodes and child element nodes
    @Test
    public void testCompareChildNodePointers_differentChildPositions() {
        Element root = document.createElement("root");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer ptr1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(-1, rootPointer.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, rootPointer.compareChildNodePointers(ptr2, ptr1));
        assertEquals(0, rootPointer.compareChildNodePointers(ptr1, ptr1));
    }

    // Tests createAttribute creates new attribute on element
    @Test
    public void testCreateAttribute_simpleAttribute_createsAttributeSuccessfully() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer attrPtr = rootPointer.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("myAttr"));
    }
}
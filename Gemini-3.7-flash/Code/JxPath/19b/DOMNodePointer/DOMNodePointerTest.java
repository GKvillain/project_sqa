package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        Element elem = document.createElement("item");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest on element matching name and wildcard
    @Test
    public void testTestNode_nodeNameTest_matchesCorrectly() {
        Element elem = document.createElement("testElement");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);

        NodeNameTest matchTest = new NodeNameTest(new QName("testElement"));
        assertTrue(pointer.testNode(matchTest));

        NodeNameTest mismatchTest = new NodeNameTest(new QName("otherElement"));
        assertFalse(pointer.testNode(mismatchTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName("*"));
        assertTrue(pointer.testNode(wildcardTest));
    }

    // Tests testNode with NodeNameTest on non-element node returns false
    @Test
    public void testTestNode_nodeNameTestOnNonElement_returnsFalse() {
        Text textNode = document.createTextNode("sample text");
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.ENGLISH);

        NodeNameTest test = new NodeNameTest(new QName("sample"));
        assertFalse(pointer.testNode(test));
    }

    // Tests testNode with NodeTypeTest covering text, comment, and processing instruction
    @Test
    public void testTestNode_nodeTypeTest_matchesExpectedTypes() {
        Text textNode = document.createTextNode("text");
        Comment commentNode = document.createComment("comment");
        ProcessingInstruction piNode = document.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(textNode, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(commentNode, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(commentNode, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(piNode, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(piNode, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertTrue(DOMNodePointer.testNode(textNode, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
    }

    // Tests testNode with ProcessingInstructionTest matching target
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);

        assertTrue(pointer.testNode(new ProcessingInstructionTest("targetPI")));
        assertFalse(pointer.testNode(new ProcessingInstructionTest("differentTarget")));
    }

    // Tests getName for Element, ProcessingInstruction, and Text nodes
    @Test
    public void testGetName_differentNodeTypes_returnsCorrectQName() {
        Element elem = document.createElementNS("http://example.com/ns", "pfx:myElement");
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("pfx", elemPointer.getName().getPrefix());
        assertEquals("myElement", elemPointer.getName().getName());

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "some data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertNull(piPointer.getName().getPrefix());
        assertEquals("myTarget", piPointer.getName().getName());
    }

    // Tests getPrefix and getLocalName static methods
    @Test
    public void testGetPrefixAndLocalName_elementWithPrefix_returnsComponents() {
        Element elem = document.createElement("ns:customTag");
        assertEquals("ns", DOMNodePointer.getPrefix(elem));
        assertEquals("customTag", DOMNodePointer.getLocalName(elem));

        Element noPrefixElem = document.createElement("plainTag");
        assertNull(DOMNodePointer.getPrefix(noPrefixElem));
        assertEquals("plainTag", DOMNodePointer.getLocalName(noPrefixElem));
    }

    // Tests getNamespaceURI with standard prefixes xml and xmlns
    @Test
    public void testGetNamespaceURI_standardPrefixes_returnsFixedURIs() {
        Element elem = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);

        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
    }

    // Tests getNamespaceURI and getDefaultNamespaceURI resolved from xmlns attributes
    @Test
    public void testGetNamespaceURI_declaredNamespaces_resolvesCorrectly() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://default.ns");
        root.setAttribute("xmlns:custom", "http://custom.ns");

        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://default.ns", childPointer.getDefaultNamespaceURI());
        assertEquals("http://custom.ns", childPointer.getNamespaceURI("custom"));
        assertNull(childPointer.getNamespaceURI("nonExistentPrefix"));
    }

    // Tests basic properties isActual, isCollection, getLength, and isLeaf
    @Test
    public void testProperties_standardElement_returnsExpectedValues() {
        Element elem = document.createElement("parent");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);

        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        elem.appendChild(document.createElement("child"));
        assertFalse(pointer.isLeaf());
        assertEquals(elem, pointer.getBaseValue());
        assertEquals(elem, pointer.getImmediateNode());
    }

    // Tests isLanguage with xml:lang attribute on element and parent hierarchy
    @Test
    public void testIsLanguage_xmlLangAttribute_returnsTrueForMatchingLang() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));
    }

    // Tests getValue and setValue for text nodes
    @Test
    public void testValue_textNode_getsAndSetsText() {
        Element parent = document.createElement("parent");
        Text textNode = document.createTextNode("initial text");
        parent.appendChild(textNode);

        DOMNodePointer textPointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        assertEquals("initial text", textPointer.getValue());

        textPointer.setValue("updated text");
        assertEquals("updated text", textPointer.getValue());
    }

    // Tests setValue on element node replacing children with new string value
    @Test
    public void testSetValue_elementNode_replacesChildrenWithText() {
        Element elem = document.createElement("container");
        elem.appendChild(document.createElement("child1"));
        elem.appendChild(document.createElement("child2"));

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        pointer.setValue("new simple value");

        assertEquals("new simple value", pointer.getValue());
        assertEquals(1, elem.getChildNodes().getLength());
    }

    // Tests remove method removing child element from parent
    @Test
    public void testRemove_childNode_successfullyRemovesFromParent() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        childPointer.remove();
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests remove method on root node without parent throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        rootPointer.remove();
    }

    // Tests asPath generation for root, nested element, text, and PI nodes
    @Test
    public void testAsPath_nestedNodes_buildsValidXPathExpression() {
        Element root = document.createElement("root");
        document.appendChild(root);

        Element child1 = document.createElement("item");
        Element child2 = document.createElement("item");
        root.appendChild(child1);
        root.appendChild(child2);

        Text text = document.createTextNode("content");
        child2.appendChild(text);

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "data");
        child2.appendChild(pi);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer child2Pointer = new DOMNodePointer(rootPointer, child2);
        DOMNodePointer textPointer = new DOMNodePointer(child2Pointer, text);
        DOMNodePointer piPointer = new DOMNodePointer(child2Pointer, pi);

        assertEquals("/item[2]", child2Pointer.asPath());
        assertEquals("/item[2]/text()[1]", textPointer.asPath());
        assertEquals("/item[2]/processing-instruction('piTarget')[1]", piPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.ENGLISH, "elementId");
        assertEquals("id('elementId')", idPointer.asPath());
    }

    // Tests compareChildNodePointers ordering for attribute and child element nodes
    @Test
    public void testCompareChildNodePointers_orderOfChildren_returnsCorrectComparison() {
        Element parent = document.createElement("parent");
        Element firstChild = document.createElement("childA");
        Element secondChild = document.createElement("childB");
        parent.appendChild(firstChild);
        parent.appendChild(secondChild);

        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer ptr1 = new DOMNodePointer(parentPointer, firstChild);
        DOMNodePointer ptr2 = new DOMNodePointer(parentPointer, secondChild);

        assertEquals(-1, parentPointer.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptr2, ptr1));
        assertEquals(0, parentPointer.compareChildNodePointers(ptr1, ptr1));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_returnsExpected() {
        Element elem1 = document.createElement("elem");
        Element elem2 = document.createElement("elem");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr1Duplicate = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr1Duplicate));
        assertFalse(ptr1.equals(ptr2));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("string"));
        assertEquals(ptr1.hashCode(), ptr1Duplicate.hashCode());
    }

    // Tests iterators and namespace resolver instantiation
    @Test
    public void testIteratorsAndResolver_instantiation_returnsNonNullObjects() {
        Element elem = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);

        assertNotNull(pointer.getNamespaceResolver());
        assertNotNull(pointer.childIterator(null, false, null));
        assertNotNull(pointer.attributeIterator(new QName("attr")));
        assertNotNull(pointer.namespaceIterator());
        assertNotNull(pointer.namespacePointer("prefix"));
    }
}
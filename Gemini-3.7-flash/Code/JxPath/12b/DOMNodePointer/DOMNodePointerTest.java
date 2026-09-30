package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
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

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        document = factory.newDocumentBuilder().newDocument();
    }

    // Tests testNode with null test
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element element = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest on Element node matching local name and namespace
    @Test
    public void testTestNode_nodeNameTest_returnsTrueWhenMatches() {
        Element element = document.createElementNS("http://example.com/ns", "p:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("p", "item"), "http://example.com/ns");
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with NodeNameTest on Element node when namespace does not match
    @Test
    public void testTestNode_nodeNameTest_returnsFalseWhenNamespaceMismatch() {
        Element element = document.createElementNS("http://example.com/ns1", "p:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("p", "item"), "http://example.com/ns2");
        assertFalse(pointer.testNode(test));
    }

    // Tests testNode with wildcard NodeNameTest and null prefix
    @Test
    public void testTestNode_wildcardWithoutPrefix_returnsTrue() {
        Element element = document.createElement("testNode");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with wildcard NodeNameTest and namespace URI (Defects4J bug 12 target)
    @Test
    public void testTestNode_wildcardWithNamespace_returnsTrueWhenNamespaceMatches() {
        Element element = document.createElementNS("http://example.com/ns", "p:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("p", "*"), "http://example.com/ns");
        assertTrue(pointer.testNode(test));
    }

    // Tests testNode with NodeTypeTest for Element and Document
    @Test
    public void testTestNode_nodeTypeTest_elementAndDocument() {
        Element element = document.createElement("item");
        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.getDefault());
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.getDefault());

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(elemPointer.testNode(nodeTest));
        assertTrue(docPointer.testNode(nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertFalse(elemPointer.testNode(textTest));
    }

    // Tests testNode with NodeTypeTest for Text and CDATA
    @Test
    public void testTestNode_nodeTypeTest_textAndCData() {
        Text text = document.createTextNode("content");
        CDATASection cdata = document.createCDATASection("content");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.getDefault());
        DOMNodePointer cdataPointer = new DOMNodePointer(cdata, Locale.getDefault());

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(textPointer.testNode(textTest));
        assertTrue(cdataPointer.testNode(textTest));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "dataPI");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.getDefault());

        ProcessingInstructionTest matchTest = new ProcessingInstructionTest("targetPI");
        ProcessingInstructionTest nonMatchTest = new ProcessingInstructionTest("otherPI");

        assertTrue(pointer.testNode(matchTest));
        assertFalse(pointer.testNode(nonMatchTest));
    }

    // Tests getName for element and processing instruction
    @Test
    public void testGetName_elementAndProcessingInstruction() {
        Element element = document.createElementNS("http://example.com", "ns:tag");
        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.getDefault());
        QName elemName = elemPointer.getName();
        assertEquals("ns", elemName.getPrefix());
        assertEquals("tag", elemName.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet", "href='style.css'");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());
        QName piName = piPointer.getName();
        assertEquals("xml-stylesheet", piName.getName());
    }

    // Tests getNamespaceURI with standard prefixes and custom namespaces
    @Test
    public void testGetNamespaceURI_standardAndDeclaredPrefixes() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:custom", "http://custom.uri");
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://custom.uri", pointer.getNamespaceURI("custom"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getValue and setValue for text nodes
    @Test
    public void testGetValueAndSetValue_textNode() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertEquals("initial", textPointer.getValue());

        textPointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        textPointer.setValue("");
        assertNull(text.getParentNode());
    }

    // Tests getValue on comment node
    @Test
    public void testGetValue_commentNode() {
        Comment comment = document.createComment(" sample comment ");
        DOMNodePointer pointer = new DOMNodePointer(comment, Locale.getDefault());
        assertEquals("sample comment", pointer.getValue());
    }

    // Tests isLanguage when xml:lang is present
    @Test
    public void testIsLanguage_withXmlLangAttribute() {
        Element element = document.createElement("elem");
        element.setAttribute("xml:lang", "en-US");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("en-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests asPath for root element and nested elements
    @Test
    public void testAsPath_nestedElements() {
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        assertEquals("/child[1]", childPointer.asPath());
    }

    // Tests asPath with id attribute
    @Test
    public void testAsPath_withId() {
        Element element = document.createElement("item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault(), "elemId");
        assertEquals("id('elemId')", pointer.asPath());
    }

    // Tests compareChildNodePointers for attribute and child element ordering
    @Test
    public void testCompareChildNodePointers_orderVerification() {
        Element parent = document.createElement("parent");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.getDefault());
        DOMNodePointer childPointer1 = new DOMNodePointer(parentPointer, child1);
        DOMNodePointer childPointer2 = new DOMNodePointer(parentPointer, child2);

        assertEquals(-1, parentPointer.compareChildNodePointers(childPointer1, childPointer2));
        assertEquals(1, parentPointer.compareChildNodePointers(childPointer2, childPointer1));
        assertEquals(0, parentPointer.compareChildNodePointers(childPointer1, childPointer1));
    }

    // Tests createAttribute on an Element node
    @Test
    public void testCreateAttribute_success() {
        Element element = document.createElement("element");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("attr"));
        assertNotNull(attrPointer);
        assertTrue(element.hasAttribute("attr"));
    }

    // Tests remove method on child and exception on root
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        pointer.remove();
    }

    // Tests getPointerByID returns NullPointer when id is not found
    @Test
    public void testGetPointerByID_notFound_returnsNullPointer() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(document, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(document);

        Pointer result = pointer.getPointerByID(context, "nonExistentId");
        assertNotNull(result);
        assertFalse(result.isActual());
    }

    // Tests remove on child node successfully removes it from parent
    @Test
    public void testRemove_childNode_success() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests basic properties: isLeaf, isCollection, getLength, getBaseValue, getImmediateNode, isActual
    @Test
    public void testBasicProperties() {
        Element element = document.createElement("item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isActual());
        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());

        // Leaf checks
        assertTrue(pointer.isLeaf()); // empty element has no children
        element.appendChild(document.createElement("sub"));
        assertFalse(pointer.isLeaf());
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        Element elem1 = document.createElement("a");
        Element elem2 = document.createElement("a");

        DOMNodePointer ptr1a = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr1b = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.getDefault());

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("string"));
        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests childIterator, attributeIterator, namespaceIterator and namespacePointer
    @Test
    public void testIteratorsAndNamespacePointer() {
        Element root = document.createElementNS("http://example.com", "ns:root");
        root.setAttribute("attr1", "val1");
        Element child = document.createElement("child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());

        NodeIterator childIt = rootPointer.childIterator(new NodeNameTest(new QName("child")), false, null);
        assertNotNull(childIt);
        assertTrue(childIt.setPosition(1));
        assertNotNull(childIt.getNodePointer());

        NodeIterator attrIt = rootPointer.attributeIterator(new QName("attr1"));
        assertNotNull(attrIt);
        assertTrue(attrIt.setPosition(1));
        assertNotNull(attrIt.getNodePointer());

        NodeIterator nsIt = rootPointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPointer.namespacePointer("ns");
        assertNotNull(nsPtr);
        assertEquals("http://example.com", nsPtr.getNamespaceURI());
    }

    // Tests testNode with NodeTypeTest for Comment and PI
    @Test
    public void testTestNode_commentAndProcessingInstruction() {
        Comment comment = document.createComment("comment text");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.getDefault());
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);

        assertTrue(commentPointer.testNode(commentTest));
        assertFalse(commentPointer.testNode(piTest));

        assertTrue(piPointer.testNode(piTest));
        assertFalse(piPointer.testNode(commentTest));
    }

    // Tests createChild by index and value on Element
    @Test
    public void testCreateChild_element() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer createdChild = pointer.createChild(context, new QName("child"), 0);
        assertNotNull(createdChild);
        assertEquals(1, root.getElementsByTagName("child").getLength());

        NodePointer createdWithValue = pointer.createChild(context, new QName("childWithValue"), 0, "hello");
        assertNotNull(createdWithValue);
        assertEquals("hello", createdWithValue.getValue());
    }

    // Tests getDefaultNamespaceURI and findEnclosingAttribute
    @Test
    public void testDefaultNamespaceURI() {
        Element root = document.createElementNS("http://default.ns", "root");
        root.setAttribute("xmlns", "http://default.ns");
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        assertEquals("http://default.ns", pointer.getDefaultNamespaceURI());
    }
}
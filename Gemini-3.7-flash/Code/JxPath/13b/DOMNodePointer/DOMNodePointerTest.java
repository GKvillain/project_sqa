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
    private Element rootElement;
    private DOMNodePointer rootPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        rootElement = document.createElementNS("http://example.com/ns", "test:root");
        rootElement.setAttribute("xmlns:test", "http://example.com/ns");
        rootElement.setAttribute("xml:lang", "en-US");
        document.appendChild(rootElement);
        rootPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
    }

    // Tests getName for element node with prefix and local name
    @Test
    public void testGetName_elementNode_returnsCorrectQName() {
        QName name = rootPointer.getName();
        assertEquals("test", name.getPrefix());
        assertEquals("root", name.getName());
    }

    // Tests getName for processing instruction node
    @Test
    public void testGetName_processingInstruction_returnsTargetName() {
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName name = piPointer.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    // Tests getNamespaceURI with standard xml and xmlns prefixes
    @Test
    public void testGetNamespaceURI_standardPrefixes_returnsFixedURIs() {
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
    }

    // Tests getNamespaceURI with custom mapped prefix
    @Test
    public void testGetNamespaceURI_declaredPrefix_returnsCorrectURI() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("test"));
    }

    // Tests getNamespaceURI for unknown prefix returns null
    @Test
    public void testGetNamespaceURI_unknownPrefix_returnsNull() {
        assertNull(rootPointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests testNode with NodeNameTest matching element
    @Test
    public void testTestNode_nodeNameTestMatch_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName("test", "root"), "http://example.com/ns");
        assertTrue(rootPointer.testNode(test));
    }

    // Tests testNode with NodeNameTest mismatch
    @Test
    public void testTestNode_nodeNameTestMismatch_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName("other"));
        assertFalse(rootPointer.testNode(test));
    }

    // Tests testNode with NodeTypeTest for node and text types
    @Test
    public void testTestNode_nodeTypeTest_evaluatesCorrectly() {
        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(rootPointer.testNode(nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertFalse(rootPointer.testNode(textTest));

        Text text = document.createTextNode("content");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPointer.testNode(textTest));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "someData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);

        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("myTarget");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("otherTarget");

        assertTrue(piPointer.testNode(matchingTest));
        assertFalse(piPointer.testNode(mismatchTest));
    }

    // Tests isLanguage when xml:lang attribute matches
    @Test
    public void testIsLanguage_matchingLanguage_returnsTrue() {
        assertTrue(rootPointer.isLanguage("en"));
        assertTrue(rootPointer.isLanguage("en-US"));
        assertFalse(rootPointer.isLanguage("fr"));
    }

    // Tests setValue with string on element node replaces children
    @Test
    public void testSetValue_stringOnElement_updatesContent() {
        rootPointer.setValue("Updated Text");
        assertEquals("Updated Text", rootPointer.getValue());
    }

    // Tests getValue on comment node
    @Test
    public void testGetValue_commentNode_returnsCommentText() {
        Comment comment = document.createComment(" sample comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("sample comment", commentPointer.getValue());
    }

    // Tests createAttribute without prefix creates attribute
    @Test
    public void testCreateAttribute_simpleAttribute_createsSuccessfully() {
        JXPathContext context = JXPathContext.newContext(document);
        QName attrName = new QName("attr");
        NodePointer ptr = rootPointer.createAttribute(context, attrName);
        assertNotNull(ptr);
        assertTrue(rootElement.hasAttribute("attr"));
    }

    // Tests createAttribute with unknown prefix throws JXPathException
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        JXPathContext context = JXPathContext.newContext(document);
        QName attrName = new QName("unboundPrefix", "attr");
        rootPointer.createAttribute(context, attrName);
    }

    // Tests createAttribute with bound prefix adds namespaced attribute
    @Test
    public void testCreateAttribute_boundPrefix_createsNamespacedAttribute() {
        JXPathContext context = JXPathContext.newContext(document);
        QName attrName = new QName("test", "myAttr");
        NodePointer ptr = rootPointer.createAttribute(context, attrName);
        assertNotNull(ptr);
        assertEquals("", rootElement.getAttributeNS("http://example.com/ns", "myAttr"));
    }

    // Tests asPath on root pointer with ID
    @Test
    public void testAsPath_withId_returnsIdExpression() {
        DOMNodePointer idPointer = new DOMNodePointer(rootElement, Locale.ENGLISH, "root-id");
        assertEquals("id('root-id')", idPointer.asPath());
    }

    // Tests asPath for child element
    @Test
    public void testAsPath_childElement_returnsXPath() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        assertEquals("/child[1]", childPointer.asPath());
    }

    // Tests compareChildNodePointers on element children ordering
    @Test
    public void testCompareChildNodePointers_siblingElements_maintainsDocumentOrder() {
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);

        DOMNodePointer ptr1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(-1, rootPointer.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, rootPointer.compareChildNodePointers(ptr2, ptr1));
        assertEquals(0, rootPointer.compareChildNodePointers(ptr1, ptr1));
    }

    // Tests remove method on child element removes it from parent
    @Test
    public void testRemove_childNode_removesNodeFromParent() {
        Element child = document.createElement("toRemove");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove method on root node without parent throws exception
    @Test(expected = JXPathException.class)
    public void testRemove_nodeWithoutParent_throwsException() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        docPointer.remove();
    }

    // Tests isLeaf on document, element, text, and empty element
    @Test
    public void testIsLeaf() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertFalse(docPointer.isLeaf());

        Text text = document.createTextNode("hello");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPointer.isLeaf());

        Element emptyElem = document.createElement("empty");
        DOMNodePointer emptyElemPointer = new DOMNodePointer(emptyElem, Locale.ENGLISH);
        assertTrue(emptyElemPointer.isLeaf());

        emptyElem.appendChild(document.createTextNode("content"));
        assertFalse(emptyElemPointer.isLeaf());
    }

    // Tests isCollection and getLength
    @Test
    public void testIsCollectionAndGetLength() {
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertTrue(rootPointer.isActual());
    }

    // Tests getBaseValue and getImmediateNode
    @Test
    public void testGetBaseValueAndImmediateNode() {
        assertSame(rootElement, rootPointer.getBaseValue());
        assertSame(rootElement, rootPointer.getImmediateNode());
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        DOMNodePointer sameNodePointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals(rootPointer, sameNodePointer);
        assertEquals(rootPointer.hashCode(), sameNodePointer.hashCode());

        Element anotherElement = document.createElement("other");
        DOMNodePointer otherPointer = new DOMNodePointer(anotherElement, Locale.ENGLISH);
        assertNotEquals(rootPointer, otherPointer);
        assertNotEquals(rootPointer, null);
        assertNotEquals(rootPointer, "someString");
    }

    // Tests testNode with comment and PI node types
    @Test
    public void testTestNode_commentAndPiNodeTypeTests() {
        Comment comment = document.createComment("a comment");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertTrue(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertTrue(piPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(piPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(rootPointer.testNode(null));
    }

    // Tests asPath on various node types: Text, CDATA, PI, Comment, and Document
    @Test
    public void testAsPath_variousNodeTypes() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertEquals("", docPointer.asPath());

        Text text = document.createTextNode("text1");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertEquals("/text()[1]", textPointer.asPath());

        CDATASection cdata = document.createCDATASection("cdata content");
        rootElement.appendChild(cdata);
        DOMNodePointer cdataPointer = new DOMNodePointer(rootPointer, cdata);
        assertEquals("/text()[2]", cdataPointer.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "piData");
        rootElement.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(rootPointer, pi);
        assertEquals("/processing-instruction('piTarget')[1]", piPointer.asPath());

        Comment comment = document.createComment("comment text");
        rootElement.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(rootPointer, comment);
        assertEquals("/comment()[1]", commentPointer.asPath());
    }

    // Tests childIterator, attributeIterator, namespaceIterator, and namespacePointer
    @Test
    public void testIteratorsAndNamespacePointer() {
        NodeIterator childIter = rootPointer.childIterator(null, false, null);
        assertNotNull(childIter);

        NodeIterator attrIter = rootPointer.attributeIterator(new QName("test"));
        assertNotNull(attrIter);

        NodeIterator nsIter = rootPointer.namespaceIterator();
        assertNotNull(nsIter);

        NodePointer nsPointer = rootPointer.namespacePointer("test");
        assertNotNull(nsPointer);
        assertEquals("http://example.com/ns", nsPointer.getNamespaceURI());
    }

    // Tests createChild by name
    @Test
    public void testCreateChild_elementNode() {
        JXPathContext context = JXPathContext.newContext(document);
        QName childName = new QName("childElem");
        NodePointer createdPointer = rootPointer.createChild(context, childName, 0);
        assertNotNull(createdPointer);
        assertEquals("childElem", ((Element) createdPointer.getNode()).getTagName());

        NodePointer createdWithValue = rootPointer.createChild(context, new QName("childWithVal"), 1, "testValue");
        assertNotNull(createdWithValue);
        assertEquals("testValue", createdWithValue.getValue());
    }

    // Tests setValue with null, empty string, and Text node
    @Test
    public void testSetValue_variousValues() {
        rootPointer.setValue("initial");
        assertEquals("initial", rootPointer.getValue());

        rootPointer.setValue(null);
        assertEquals("", rootPointer.getValue());

        Text text = document.createTextNode("hello");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        textPointer.setValue("world");
        assertEquals("world", textPointer.getValue());

        textPointer.setValue(null);
        assertNull(text.getParentNode());
    }

    // Tests getPrefix for registered and unregistered namespace URI
    @Test
    public void testGetPrefix() {
        assertEquals("test", rootPointer.getPrefix("http://example.com/ns"));
        assertEquals("xml", rootPointer.getPrefix(DOMNodePointer.XML_NAMESPACE_URI));
        assertNull(rootPointer.getPrefix("http://unregistered.com/ns"));
    }

    // Tests getDefaultNamespaceURI
    @Test
    public void testGetDefaultNamespaceURI() {
        Element defaultNsElem = document.createElementNS("http://default.com/ns", "def");
        defaultNsElem.setAttribute("xmlns", "http://default.com/ns");
        DOMNodePointer defPointer = new DOMNodePointer(defaultNsElem, Locale.ENGLISH);
        assertEquals("http://default.com/ns", defPointer.getDefaultNamespaceURI());
    }

    // Tests compareChildNodePointers with attribute pointers
    @Test
    public void testCompareChildNodePointers_withAttributes() {
        rootElement.setAttribute("attr1", "val1");
        rootElement.setAttribute("attr2", "val2");
        Element child = document.createElement("child");
        rootElement.appendChild(child);

        Attr attr1 = rootElement.getAttributeNode("attr1");
        DOMAttributePointer attrPtr1 = new DOMAttributePointer(rootPointer, attr1);
        DOMNodePointer childPtr = new DOMNodePointer(rootPointer, child);

        assertEquals(-1, rootPointer.compareChildNodePointers(attrPtr1, childPtr));
        assertEquals(1, rootPointer.compareChildNodePointers(childPtr, attrPtr1));
    }
}
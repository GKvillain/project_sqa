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
    private Element rootElement;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        rootElement = document.createElementNS("http://example.com/ns", "test:root");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:test", "http://example.com/ns");
        rootElement.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en-US");
        document.appendChild(rootElement);
    }

    // Tests getName on an element node with namespace prefix
    @Test
    public void testGetName_elementNode_returnsCorrectQName() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("test", name.getPrefix());
        assertEquals("root", name.getName());
    }

    // Tests getName on a processing instruction node
    @Test
    public void testGetName_processingInstructionNode_returnsTargetName() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("targetPI", name.getName());
    }

    // Tests static getNamespaceURI on Document and Element nodes
    @Test
    public void testGetNamespaceURI_documentAndElement_returnsCorrectURI() {
        String docUri = DOMNodePointer.getNamespaceURI(document);
        assertEquals("http://example.com/ns", docUri);

        String elemUri = DOMNodePointer.getNamespaceURI(rootElement);
        assertEquals("http://example.com/ns", elemUri);
    }

    // Tests getNamespaceURI with standard prefixes "xml", "xmlns", and null prefix
    @Test
    public void testGetNamespaceURI_standardPrefixes_returnsExpectedConstants() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertNull(pointer.getNamespaceURI((String) null));
        assertEquals("http://example.com/ns", pointer.getNamespaceURI("test"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI when xmlns default attribute is present
    @Test
    public void testGetDefaultNamespaceURI_withDefaultNamespace_returnsURI() {
        Element child = document.createElement("child");
        child.setAttribute("xmlns", "http://example.com/default");
        rootElement.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://example.com/default", childPointer.getDefaultNamespaceURI());
    }

    // Tests testNode with NodeNameTest matching wildcard and specific names
    @Test
    public void testTestNode_nodeNameTest_returnsExpectedMatches() {
        Element child = document.createElementNS("http://example.com/ns", "test:child");
        rootElement.appendChild(child);
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));

        NodeNameTest matchingTest = new NodeNameTest(new QName("test", "child"), "http://example.com/ns");
        assertTrue(pointer.testNode(matchingTest));

        NodeNameTest nonMatchingTest = new NodeNameTest(new QName("test", "other"), "http://example.com/ns");
        assertFalse(pointer.testNode(nonMatchingTest));

        // Non-element node should return false for NodeNameTest
        Text textNode = document.createTextNode("content");
        assertFalse(DOMNodePointer.testNode(textNode, matchingTest));
    }

    // Tests testNode with NodeTypeTest for node, text, comment, and PI types
    @Test
    public void testTestNode_nodeTypeTest_returnsMatchingTypeResults() {
        Text text = document.createTextNode("sample text");
        Comment comment = document.createComment("a comment");
        ProcessingInstruction pi = document.createProcessingInstruction("pi", "data");

        assertTrue(DOMNodePointer.testNode(rootElement, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(rootElement, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("target1", "data");
        ProcessingInstructionTest matchTest = new ProcessingInstructionTest("target1");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("target2");

        assertTrue(DOMNodePointer.testNode(pi, matchTest));
        assertFalse(DOMNodePointer.testNode(pi, mismatchTest));
        assertFalse(DOMNodePointer.testNode(rootElement, matchTest));
    }

    // Tests testNode with null NodeTest returns true
    @Test
    public void testTestNode_nullNodeTest_returnsTrue() {
        assertTrue(DOMNodePointer.testNode(rootElement, null));
    }

    // Tests setValue on Text node and Element node
    @Test
    public void testSetValue_textAndElementNodes_updatesContent() {
        Text text = document.createTextNode("old");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);

        textPointer.setValue("newText");
        assertEquals("newText", text.getNodeValue());

        DOMNodePointer elemPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        elemPointer.setValue("replacement");
        assertEquals("replacement", elemPointer.getValue());
    }

    // Tests setValue with empty string on Text node removes it from parent
    @Test
    public void testSetValue_emptyStringOnTextNode_removesNode() {
        Text text = document.createTextNode("initial");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);

        textPointer.setValue("");
        assertNull(text.getParentNode());
    }

    // Tests isLanguage with matching and non-matching language codes
    @Test
    public void testIsLanguage_variousLocales_returnsExpectedResults() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("en-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests isLeaf and getLength methods
    @Test
    public void testIsLeafAndLength_emptyAndNonEmptyElements_returnsExpected() {
        DOMNodePointer rootPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(rootPointer.isLeaf());
        assertEquals(1, rootPointer.getLength());
        assertTrue(rootPointer.isActual());
        assertFalse(rootPointer.isCollection());

        Element child = document.createElement("child");
        rootElement.appendChild(child);
        assertFalse(rootPointer.isLeaf());
    }

    // Tests getValue on Comment, Text, CDATA, and Element nodes
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectString() {
        Comment comment = document.createComment(" test comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("test comment", commentPointer.getValue());

        Element elem = document.createElement("sample");
        elem.appendChild(document.createTextNode("hello "));
        CDATASection cdata = document.createCDATASection("world");
        elem.appendChild(cdata);
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("helloworld", elemPointer.getValue());
    }

    // Tests asPath for root element, child element, text node, and id pointer
    @Test
    public void testAsPath_variousNodes_returnsCorrectXPathExpressions() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        DOMNodePointer rootPointer = new DOMNodePointer(docPointer, rootElement);

        Element child = document.createElementNS(null, "sub");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        assertEquals("/sub[1]", childPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(rootElement, Locale.ENGLISH, "elemId");
        assertEquals("id('elemId')", idPointer.asPath());

        Text text = document.createTextNode("abc");
        child.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(childPointer, text);
        assertEquals("/sub[1]/text()[1]", textPointer.asPath());
    }

    // Tests remove method on root element throws exception
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsJXPathException() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        docPointer.remove();
    }

    // Tests remove method on child element removes it from DOM
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element child = document.createElement("toBeRemoved");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);

        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests compareChildNodePointers between attributes and child elements
    @Test
    public void testCompareChildNodePointers_differentTypes_returnsConsistentOrdering() {
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);

        DOMNodePointer parentPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        DOMNodePointer ptr1 = new DOMNodePointer(parentPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(parentPointer, child2);

        assertEquals(0, parentPointer.compareChildNodePointers(ptr1, ptr1));
        assertEquals(-1, parentPointer.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptr2, ptr1));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_returnsConsistent() {
        DOMNodePointer ptr1 = new DOMNodePointer(rootElement, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(rootElement, Locale.ENGLISH);

        Element other = document.createElement("other");
        DOMNodePointer ptr3 = new DOMNodePointer(other, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr2));
        assertEquals(ptr1.hashCode(), ptr2.hashCode());

        assertFalse(ptr1.equals(ptr3));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("string"));
    }

    // Tests getPointerByID with existing and non-existing element ids
    @Test
    public void testGetPointerByID_existingAndNonExisting_returnsExpectedPointers() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer nullPtr = docPointer.getPointerByID(context, "nonExistent");
        assertTrue(nullPtr instanceof NullPointer);
    }

    // Tests static getPrefix and getLocalName utility methods
    @Test
    public void testGetPrefixAndLocalName_prefixedAndUnprefixedNodes_returnsExpectedNames() {
        Element prefixed = document.createElementNS("http://example.com/ns", "foo:bar");
        assertEquals("foo", DOMNodePointer.getPrefix(prefixed));
        assertEquals("bar", DOMNodePointer.getLocalName(prefixed));

        Element unprefixed = document.createElement("simple");
        assertNull(DOMNodePointer.getPrefix(unprefixed));
        assertEquals("simple", DOMNodePointer.getLocalName(unprefixed));
    }
}
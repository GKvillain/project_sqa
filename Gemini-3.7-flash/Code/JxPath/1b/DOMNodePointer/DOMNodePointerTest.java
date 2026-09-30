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

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    // Tests testNode with null test returning true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        assertTrue(pointer.testNode(null));
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    // Tests testNode with NodeNameTest against element and non-element nodes
    @Test
    public void testTestNode_nodeNameTest_matchesElement() {
        Element elem = document.createElementNS("http://example.com/ns", "p:test");
        Text text = document.createTextNode("content");

        NodeNameTest testMatch = new NodeNameTest(new QName("p", "test"), "http://example.com/ns");
        NodeNameTest testMismatch = new NodeNameTest(new QName("p", "other"), "http://example.com/ns");
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));

        assertTrue(DOMNodePointer.testNode(elem, testMatch));
        assertFalse(DOMNodePointer.testNode(elem, testMismatch));
        assertTrue(DOMNodePointer.testNode(elem, wildcardTest));
        assertFalse(DOMNodePointer.testNode(text, testMatch));
    }

    // Tests testNode with NodeTypeTest for different DOM node types
    @Test
    public void testTestNode_nodeTypeTest_matchesCorrectType() {
        Element elem = document.createElement("elem");
        Text text = document.createTextNode("text");
        CDATASection cdata = document.createCDATASection("cdata");
        Comment comment = document.createComment("comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);

        assertTrue(DOMNodePointer.testNode(elem, nodeTest));
        assertFalse(DOMNodePointer.testNode(text, nodeTest));

        assertTrue(DOMNodePointer.testNode(text, textTest));
        assertTrue(DOMNodePointer.testNode(cdata, textTest));
        assertFalse(DOMNodePointer.testNode(elem, textTest));

        assertTrue(DOMNodePointer.testNode(comment, commentTest));
        assertFalse(DOMNodePointer.testNode(elem, commentTest));

        assertTrue(DOMNodePointer.testNode(pi, piTest));
        assertFalse(DOMNodePointer.testNode(elem, piTest));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "some data");
        Element elem = document.createElement("elem");

        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("targetPI");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("otherPI");

        assertTrue(DOMNodePointer.testNode(pi, matchingTest));
        assertFalse(DOMNodePointer.testNode(pi, mismatchTest));
        assertFalse(DOMNodePointer.testNode(elem, matchingTest));
    }

    // Tests getName for Element and ProcessingInstruction nodes
    @Test
    public void testGetName_elementAndPI_returnsExpectedQNames() {
        Element elem = document.createElementNS("http://example.com", "ns:item");
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.getDefault());
        QName elemQName = elemPointer.getName();
        assertEquals("ns", elemQName.getPrefix());
        assertEquals("item", elemQName.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());
        QName piQName = piPointer.getName();
        assertNull(piQName.getPrefix());
        assertEquals("myTarget", piQName.getName());
    }

    // Tests getNamespaceURI with prefix resolution
    @Test
    public void testGetNamespaceURI_predefinedAndDeclaredPrefixes() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:custom", "http://custom.com/ns");
        root.setAttribute("xmlns", "http://default.com/ns");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());

        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://custom.com/ns", pointer.getNamespaceURI("custom"));
        assertEquals("http://default.com/ns", pointer.getNamespaceURI(""));
        assertEquals("http://default.com/ns", pointer.getDefaultNamespaceURI());
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests isLanguage and getLanguage
    @Test
    public void testIsLanguage_xmlLangAttribute_returnsMatching() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        assertTrue(rootPointer.isLanguage("en"));
        assertTrue(rootPointer.isLanguage("en-US"));
        assertFalse(rootPointer.isLanguage("fr"));

        assertTrue(childPointer.isLanguage("en"));
        assertEquals("en-US", childPointer.getLanguage());
    }

    // Tests setValue for text node and element node
    @Test
    public void testSetValue_textAndElementNodes() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        textPointer.setValue("updatedText");
        assertEquals("updatedText", text.getNodeValue());

        // Empty value removes text node
        textPointer.setValue("");
        assertEquals(0, root.getChildNodes().getLength());

        // Setting string on element replaces children
        DOMNodePointer elemPointer = new DOMNodePointer(root, Locale.getDefault());
        elemPointer.setValue("newChildText");
        assertEquals("newChildText", root.getTextContent());

        // Setting Element node as value
        Element newElem = document.createElement("sub");
        newElem.appendChild(document.createTextNode("subText"));
        elemPointer.setValue(newElem);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("sub", root.getFirstChild().getNodeName());
    }

    // Tests asPath for root, children, text, and PI nodes
    @Test
    public void testAsPath_variousNodeTypes_constructsValidXPath() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPointer = new DOMNodePointer(null, root);

        Element child1 = document.createElement("child");
        root.appendChild(child1);
        DOMNodePointer child1Pointer = new DOMNodePointer(rootPointer, child1);

        Element child2 = document.createElement("child");
        root.appendChild(child2);
        DOMNodePointer child2Pointer = new DOMNodePointer(rootPointer, child2);

        Text text = document.createTextNode("hello");
        child1.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(child1Pointer, text);

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        child1.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(child1Pointer, pi);

        assertEquals("/child[1]", child1Pointer.asPath());
        assertEquals("/child[2]", child2Pointer.asPath());
        assertEquals("/child[1]/text()[1]", textPointer.asPath());
        assertEquals("/child[1]/processing-instruction('target')[1]", piPointer.asPath());
    }

    // Tests asPath with id attribute
    @Test
    public void testAsPath_withIdentifier_returnsIdExpression() {
        Element elem = document.createElement("item");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.getDefault(), "elem'1\"");
        assertEquals("id('elem&apos;1&quot;')", pointer.asPath());
    }

    // Tests remove on child node and exception on root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.getDefault());
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove on root node throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        pointer.remove();
    }

    // Tests createAttribute without prefix
    @Test
    public void testCreateAttribute_noPrefix_createsAttributeNode() {
        Element elem = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("attr"));
        assertNotNull(attrPointer);
        assertTrue(elem.hasAttribute("attr"));
    }

    // Tests createAttribute with unknown namespace prefix throws JXPathException
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element elem = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(elem);

        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests getValue for Comment, Text, CDATA, PI, and Element nodes
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectString() {
        Comment comment = document.createComment(" a comment ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.getDefault());
        assertEquals("a comment", commentPointer.getValue());

        Text text = document.createTextNode(" text value ");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.getDefault());
        assertEquals("text value", textPointer.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("pi", " instruction data ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.getDefault());
        assertEquals("instruction data", piPointer.getValue());

        Element parent = document.createElement("parent");
        Text childText = document.createTextNode("Hello ");
        Element childElem = document.createElement("child");
        childElem.appendChild(document.createTextNode("World"));
        parent.appendChild(childText);
        parent.appendChild(childElem);

        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.getDefault());
        assertEquals("Hello World", parentPointer.getValue());
    }

    // Tests compareChildNodePointers between attributes and child elements
    @Test
    public void testCompareChildNodePointers_orderVerification() {
        Element elem = document.createElement("parent");
        elem.setAttribute("a", "1");
        elem.setAttribute("b", "2");

        Element child1 = document.createElement("c1");
        Element child2 = document.createElement("c2");
        elem.appendChild(child1);
        elem.appendChild(child2);

        DOMNodePointer parentPointer = new DOMNodePointer(elem, Locale.getDefault());

        NodeIterator attrIt = parentPointer.attributeIterator(new QName("a"));
        attrIt.setPosition(1);
        NodePointer attrPtr1 = attrIt.getNodePointer();

        DOMNodePointer childPtr1 = new DOMNodePointer(parentPointer, child1);
        DOMNodePointer childPtr2 = new DOMNodePointer(parentPointer, child2);

        // Attribute vs Element
        assertEquals(-1, parentPointer.compareChildNodePointers(attrPtr1, childPtr1));
        assertEquals(1, parentPointer.compareChildNodePointers(childPtr1, attrPtr1));

        // Same pointers
        assertEquals(0, parentPointer.compareChildNodePointers(childPtr1, childPtr1));

        // Element order
        assertEquals(-1, parentPointer.compareChildNodePointers(childPtr1, childPtr2));
        assertEquals(1, parentPointer.compareChildNodePointers(childPtr2, childPtr1));
    }

    // Tests equals, hashCode, and basic pointer properties
    @Test
    public void testEqualsAndHashCodeAndProperties() {
        Element elem1 = document.createElement("item");
        Element elem2 = document.createElement("item");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr1Same = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.getDefault());

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr1Same));
        assertFalse(ptr1.equals(ptr2));
        assertFalse(ptr1.equals("string"));
        assertEquals(ptr1.hashCode(), ptr1Same.hashCode());

        assertSame(elem1, ptr1.getBaseValue());
        assertSame(elem1, ptr1.getImmediateNode());
        assertTrue(ptr1.isActual());
        assertFalse(ptr1.isCollection());
        assertEquals(1, ptr1.getLength());
        assertTrue(ptr1.isLeaf());

        elem1.appendChild(document.createElement("sub"));
        assertFalse(ptr1.isLeaf());
    }

    // Tests getPointerByID returns DOMNodePointer when found, NullPointer when not found
    @Test
    public void testGetPointerByID_findsOrReturnsNullPointer() {
        Element elem = document.createElement("elem");
        elem.setAttribute("id", "targetId");
        elem.setIdAttribute("id", true);
        document.appendChild(elem);

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = docPointer.getPointerByID(context, "targetId");
        assertNotNull(found);
        assertTrue(found instanceof DOMNodePointer);
        assertSame(elem, found.getNode());

        Pointer notFound = docPointer.getPointerByID(context, "missingId");
        assertNotNull(notFound);
        assertNull(notFound.getNode());
    }

    // Tests static helpers getPrefix and getLocalName
    @Test
    public void testStaticGetPrefixAndGetLocalName() {
        Element elemNoPrefix = document.createElement("simple");
        assertNull(DOMNodePointer.getPrefix(elemNoPrefix));
        assertEquals("simple", DOMNodePointer.getLocalName(elemNoPrefix));

        Element elemWithPrefix = document.createElement("ns:complex");
        assertEquals("ns", DOMNodePointer.getPrefix(elemWithPrefix));
        assertEquals("complex", DOMNodePointer.getLocalName(elemWithPrefix));
    }

    // Tests createChild with and without value
    @Test
    public void testCreateChild_appendsElement() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer childPtr = pointer.createChild(context, new QName("child"), 0);
        assertNotNull(childPtr);
        assertEquals(1, root.getElementsByTagName("child").getLength());

        NodePointer childWithValuePtr = pointer.createChild(context, new QName("childWithVal"), 0, "val");
        assertNotNull(childWithValuePtr);
        assertEquals("val", childWithValuePtr.getValue());
    }

    // Tests childIterator and namespaceIterator
    @Test
    public void testIterators_childAndNamespace() {
        Element root = document.createElementNS("http://custom.com/ns", "p:root");
        root.setAttribute("xmlns:p", "http://custom.com/ns");
        Element c1 = document.createElement("c1");
        Element c2 = document.createElement("c2");
        root.appendChild(c1);
        root.appendChild(c2);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());

        NodeIterator childIt = pointer.childIterator(new NodeNameTest(new QName("c1")), false, null);
        assertNotNull(childIt);
        assertTrue(childIt.setPosition(1));
        assertEquals("c1", childIt.getNodePointer().getName().getName());

        NodeIterator nsIt = pointer.namespaceIterator();
        assertNotNull(nsIt);
        assertTrue(nsIt.setPosition(1));
        assertNotNull(nsIt.getNodePointer());

        NodePointer nsPtr = pointer.namespacePointer("p");
        assertNotNull(nsPtr);
        assertEquals("http://custom.com/ns", nsPtr.getValue());
    }

    // Tests asPath on document node
    @Test
    public void testAsPath_documentNode() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.getDefault());
        assertEquals("", docPointer.asPath());
    }
}
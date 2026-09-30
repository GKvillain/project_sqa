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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DOMNodePointerTest {

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    // Tests constructors and basic property accessors
    @Test
    public void testBasicProperties_elementNode_returnsExpectedValues() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US, "rootId");

        assertEquals(root, pointer.getBaseValue());
        assertEquals(root, pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        root.appendChild(document.createElement("child"));
        assertFalse(pointer.isLeaf());
    }

    // Tests testNode with NodeNameTest (matching name, wildcard, and non-element)
    @Test
    public void testTestNode_nodeNameTest_matchesCorrectly() {
        Element element = document.createElementNS("http://example.com/ns", "ns:test");
        document.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US);

        NodeNameTest exactTest = new NodeNameTest(new QName("ns", "test"), "http://example.com/ns");
        assertTrue(pointer.testNode(exactTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));

        NodeNameTest wrongNameTest = new NodeNameTest(new QName("other"));
        assertFalse(pointer.testNode(wrongNameTest));

        Text text = document.createTextNode("sample");
        assertFalse(DOMNodePointer.testNode(text, exactTest));
        assertTrue(DOMNodePointer.testNode(element, null));
    }

    // Tests testNode with NodeTypeTest across various node types
    @Test
    public void testTestNode_nodeTypeTest_handlesAllTypes() {
        Element element = document.createElement("elem");
        Text text = document.createTextNode("text");
        CDATASection cdata = document.createCDATASection("cdata");
        Comment comment = document.createComment("comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        NodeTypeTest unknownTest = new NodeTypeTest(999);

        assertTrue(DOMNodePointer.testNode(element, nodeTest));
        assertTrue(DOMNodePointer.testNode(text, textTest));
        assertTrue(DOMNodePointer.testNode(cdata, textTest));
        assertTrue(DOMNodePointer.testNode(comment, commentTest));
        assertTrue(DOMNodePointer.testNode(pi, piTest));
        assertFalse(DOMNodePointer.testNode(element, textTest));
        assertFalse(DOMNodePointer.testNode(element, unknownTest));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("target1", "data");
        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("target1");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("target2");

        assertTrue(DOMNodePointer.testNode(pi, matchingTest));
        assertFalse(DOMNodePointer.testNode(pi, mismatchTest));

        Element element = document.createElement("elem");
        assertFalse(DOMNodePointer.testNode(element, matchingTest));
    }

    // Tests getName for Element and ProcessingInstruction nodes
    @Test
    public void testGetName_elementAndPI_returnsCorrectQName() {
        Element element = document.createElementNS("http://example.com/ns", "pfx:item");
        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.US);
        assertEquals(new QName("pfx", "item"), elemPointer.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        assertEquals(new QName(null, "myTarget"), piPointer.getName());
    }

    // Tests static getPrefix and getLocalName helper methods
    @Test
    public void testGetPrefixAndLocalName_variousNodes_returnsExpectedNames() {
        Element elementWithPrefix = document.createElement("pfx:child");
        assertEquals("pfx", DOMNodePointer.getPrefix(elementWithPrefix));
        assertEquals("child", DOMNodePointer.getLocalName(elementWithPrefix));

        Element elementNoPrefix = document.createElement("plain");
        assertNull(DOMNodePointer.getPrefix(elementNoPrefix));
        assertEquals("plain", DOMNodePointer.getLocalName(elementNoPrefix));
    }

    // Tests getNamespaceURI static and instance methods with declarations
    @Test
    public void testGetNamespaceURI_withXmlnsAttributes_resolvesCorrectly() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://example.com/default");
        root.setAttribute("xmlns:foo", "http://example.com/foo");
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, rootPointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, rootPointer.getNamespaceURI("xmlns"));
        assertEquals("http://example.com/default", rootPointer.getNamespaceURI(""));
        assertEquals("http://example.com/default", rootPointer.getNamespaceURI((String) null));
        assertEquals("http://example.com/foo", rootPointer.getNamespaceURI("foo"));
        assertNull(rootPointer.getNamespaceURI("unknownPrefix"));

        assertEquals("http://example.com/default", DOMNodePointer.getNamespaceURI(document));
        assertEquals("http://example.com/default", rootPointer.getDefaultNamespaceURI());
    }

    // Tests getNamespaceResolver, childIterator, and namespaceIterator
    @Test
    public void testIteratorsAndResolvers_validNode_returnsNonNullIterators() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);

        assertNotNull(pointer.getNamespaceResolver());
        assertNotNull(pointer.childIterator(null, false, null));
        assertNotNull(pointer.attributeIterator(new QName("attr")));
        assertNotNull(pointer.namespaceIterator());
        assertNotNull(pointer.namespacePointer("foo"));
    }

    // Tests language detection through xml:lang attribute hierarchy
    @Test
    public void testIsLanguage_withXmlLang_evaluatesCorrectly() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);
        document.appendChild(parent);

        DOMNodePointer childPointer = new DOMNodePointer(new DOMNodePointer(parent, Locale.US), child);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));
    }

    // Tests getValue and stringValue for Element, Text, Comment, and PI nodes
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectStringRepresentation() {
        Element root = document.createElement("root");
        Text text1 = document.createTextNode(" Hello ");
        Text text2 = document.createTextNode("World ");
        root.appendChild(text1);
        root.appendChild(text2);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals("HelloWorld", pointer.getValue());

        Comment comment = document.createComment(" comment text ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        assertEquals("comment text", commentPointer.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", " pi text ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        assertEquals("pi text", piPointer.getValue());
    }

    // Tests setValue for Text node and Element node
    @Test
    public void testSetValue_textAndElementNodes_updatesContent() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);
        document.appendChild(root);

        DOMNodePointer textPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childTextPointer = new DOMNodePointer(textPointer, text);

        childTextPointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        childTextPointer.setValue("");
        assertNull(text.getParentNode());

        textPointer.setValue("new root text");
        assertEquals("new root text", root.getTextContent());

        Element newChild = document.createElement("sub");
        newChild.appendChild(document.createTextNode("nested"));
        textPointer.setValue(newChild);
        assertEquals("nested", root.getTextContent());
    }

    // Tests createAttribute for Element node
    @Test
    public void testCreateAttribute_elementNode_addsAttribute() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPointer);
        assertTrue(root.hasAttribute("myAttr"));
    }

    // Tests remove method on child element and exception on root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);
        document.appendChild(root);

        DOMNodePointer childPointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), child);
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove exception path when parent is null
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        DOMNodePointer pointer = new DOMNodePointer(document, Locale.US);
        pointer.remove();
    }

    // Tests asPath method for id, element, text, and processing instruction
    @Test
    public void testAsPath_variousNodeTypes_generatesCorrectPath() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        Text text = document.createTextNode("text");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        root.appendChild(child);
        root.appendChild(text);
        root.appendChild(pi);
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        DOMNodePointer piPointer = new DOMNodePointer(rootPointer, pi);

        assertEquals("/child[1]", childPointer.asPath());
        assertEquals("/text()[1]", textPointer.asPath());
        assertEquals("/processing-instruction('target')[1]", piPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", idPointer.asPath());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_comparesCorrectly() {
        Element elem1 = document.createElement("elem");
        Element elem2 = document.createElement("elem");

        DOMNodePointer p1 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer p3 = new DOMNodePointer(elem2, Locale.US);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("non-pointer"));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // Tests getPointerByID method
    @Test
    public void testGetPointerByID_existingAndNonExistingId_returnsCorrectPointer() {
        Element root = document.createElement("root");
        root.setAttribute("id", "elem1");
        root.setIdAttribute("id", true);
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(document, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = pointer.getPointerByID(context, "elem1");
        assertNotNull(found);
        assertEquals(root, found.getBaseValue());

        Pointer notFound = pointer.getPointerByID(context, "nonExistent");
        assertTrue(notFound instanceof NullPointer);
    }

    // Tests compareChildNodePointers method
    @Test
    public void testCompareChildNodePointers_siblingOrder_returnsExpectedOrder() {
        Element root = document.createElement("root");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(0, rootPointer.compareChildNodePointers(p1, p1));
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointers(p2, p1));
    }

    // Tests createChild with and without value
    @Test
    public void testCreateChild_elementNode_createsAndAppends() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer childPtr = rootPointer.createChild(context, new QName("child"), 0);
        assertNotNull(childPtr);
        assertEquals("child", ((Element) childPtr.getNode()).getTagName());

        NodePointer childWithValuePtr = rootPointer.createChild(context, new QName("item"), 0, "hello");
        assertNotNull(childWithValuePtr);
        assertEquals("item", ((Element) childWithValuePtr.getNode()).getTagName());
        assertEquals("hello", childWithValuePtr.getValue());
    }

    // Tests createChild at index requiring filler elements
    @Test
    public void testCreateChild_withHigherIndex_createsIntermediateElements() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer childPtr = rootPointer.createChild(context, new QName("elem"), 2);
        assertNotNull(childPtr);
        assertEquals(3, root.getElementsByTagName("elem").getLength());
    }

    // Tests createAttribute with prefix and namespace
    @Test
    public void testCreateAttribute_withPrefixAndNamespace() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);
        context.registerNamespace("custom", "http://example.com/custom");

        NodePointer attrPointer = pointer.createAttribute(context, new QName("custom", "myAttr"));
        assertNotNull(attrPointer);
        assertTrue(root.hasAttributeNS("http://example.com/custom", "myAttr"));
    }

    // Tests setValue with null, empty, comment, and processing instruction
    @Test
    public void testSetValue_commentAndPINodes() {
        Comment comment = document.createComment("oldComment");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        commentPointer.setValue("newComment");
        assertEquals("newComment", comment.getData());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "oldData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        piPointer.setValue("newData");
        assertEquals("newData", pi.getData());
    }

    // Tests setValue on Element with null or empty removes children
    @Test
    public void testSetValue_elementWithNullOrEmpty_removesChildren() {
        Element root = document.createElement("root");
        root.appendChild(document.createElement("c1"));
        root.appendChild(document.createElement("c2"));
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        rootPointer.setValue(null);
        assertEquals(0, root.getChildNodes().getLength());

        root.appendChild(document.createElement("c3"));
        rootPointer.setValue("");
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests asPath with Document, Comment, and repeated sibling indices
    @Test
    public void testAsPath_documentAndCommentAndSiblings() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.US);
        assertEquals("", docPointer.asPath());

        Element root = document.createElement("root");
        document.appendChild(root);
        Element c1 = document.createElement("item");
        Element c2 = document.createElement("item");
        Comment comment = document.createComment("a comment");
        root.appendChild(c1);
        root.appendChild(comment);
        root.appendChild(c2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer c2Pointer = new DOMNodePointer(rootPointer, c2);
        DOMNodePointer commentPointer = new DOMNodePointer(rootPointer, comment);

        assertEquals("/item[2]", c2Pointer.asPath());
        assertEquals("/comment()[1]", commentPointer.asPath());
    }

    // Tests isLeaf for text, comment, pi, and document nodes
    @Test
    public void testIsLeaf_variousNodes() {
        Text text = document.createTextNode("txt");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.US);
        assertTrue(textPointer.isLeaf());

        Comment comment = document.createComment("cmt");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        assertTrue(commentPointer.isLeaf());

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.US);
        assertTrue(docPointer.isLeaf());
    }

    // Tests getPointerByKey method
    @Test
    public void testGetPointerByKey_returnsNullPointerWhenNotFound() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(document, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer result = pointer.getPointerByKey(context, "keyName", "keyValue");
        assertTrue(result instanceof NullPointer);
    }

    // Tests getLanguage when no xml:lang attribute is present
    @Test
    public void testGetLanguage_withoutXmlLang_returnsNull() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertNull(pointer.getLanguage());
    }

    // Tests getNamespaceURI on non-element and null nodes
    @Test
    public void testGetNamespaceURI_nullAndNonElementNodes() {
        assertNull(DOMNodePointer.getNamespaceURI((org.w3c.dom.Node) null));

        Comment comment = document.createComment("text");
        assertNull(DOMNodePointer.getNamespaceURI(comment));
    }
}
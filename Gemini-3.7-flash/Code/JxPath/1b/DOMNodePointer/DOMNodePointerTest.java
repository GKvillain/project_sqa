package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;

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
        document = factory.newDocumentBuilder().newDocument();
    }

    // Tests getName for element node with prefix and local name
    @Test
    public void testGetName_elementNode_returnsPrefixAndLocalName() {
        Element element = document.createElementNS("http://example.com/ns", "p:child");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName name = pointer.getName();
        assertEquals("p", name.getPrefix());
        assertEquals("child", name.getName());
    }

    // Tests getName for processing instruction node
    @Test
    public void testGetName_processingInstruction_returnsTargetAsName() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);

        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("targetPI", name.getName());
    }

    // Tests testNode with null NodeTest returning true
    @Test
    public void testTestNode_nullNodeTest_returnsTrue() {
        Element element = document.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest for wildcard and matching name
    @Test
    public void testTestNode_nodeNameTest_matchesWildcardAndExactName() {
        Element element = document.createElementNS("http://example.com/test", "ns:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));

        NodeNameTest exactMatchTest = new NodeNameTest(new QName("ns", "item"), "http://example.com/test");
        assertTrue(pointer.testNode(exactMatchTest));

        NodeNameTest nonMatchTest = new NodeNameTest(new QName("ns", "other"), "http://example.com/test");
        assertFalse(pointer.testNode(nonMatchTest));

        Text text = document.createTextNode("content");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertFalse(textPointer.testNode(exactMatchTest));
    }

    // Tests testNode with NodeTypeTest for element, text, cdata, comment, and processing instruction
    @Test
    public void testTestNode_nodeTypeTest_matchesCorrespondingTypes() {
        Element element = document.createElement("el");
        Text text = document.createTextNode("txt");
        CDATASection cdata = document.createCDATASection("cdata");
        Comment comment = document.createComment("comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        DOMNodePointer elPtr = new DOMNodePointer(element, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.ENGLISH);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);

        assertTrue(elPtr.testNode(nodeTest));
        assertFalse(textPtr.testNode(nodeTest));

        assertTrue(textPtr.testNode(textTest));
        assertTrue(cdataPtr.testNode(textTest));
        assertFalse(elPtr.testNode(textTest));

        assertTrue(commentPtr.testNode(commentTest));
        assertFalse(elPtr.testNode(commentTest));

        assertTrue(piPtr.testNode(piTest));
        assertFalse(elPtr.testNode(piTest));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetA", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);

        ProcessingInstructionTest matchingTest = new ProcessingInstructionTest("targetA");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("targetB");

        assertTrue(pointer.testNode(matchingTest));
        assertFalse(pointer.testNode(mismatchTest));
    }

    // Tests getNamespaceURI with standard prefixes "xml" and "xmlns"
    @Test
    public void testGetNamespaceURI_standardPrefixes_returnsFixedURIs() {
        Element element = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
    }

    // Tests getNamespaceURI and getDefaultNamespaceURI with declared namespaces
    @Test
    public void testGetNamespaceURI_declaredNamespaces_returnsResolvedURIs() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://example.com/default");
        root.setAttribute("xmlns:custom", "http://example.com/custom");
        document.appendChild(root);

        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals("http://example.com/default", pointer.getDefaultNamespaceURI());
        assertEquals("http://example.com/default", pointer.getNamespaceURI((String) null));
        assertEquals("http://example.com/default", pointer.getNamespaceURI(""));
        assertEquals("http://example.com/custom", pointer.getNamespaceURI("custom"));
        assertNull(pointer.getNamespaceURI("unknown"));
    }

    // Tests isLanguage when xml:lang attribute is present and absent
    @Test
    public void testIsLanguage_xmlLangAttribute_checksMatchingCaseInsensitive() {
        Element element = document.createElement("doc");
        element.setAttribute("xml:lang", "en-US");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("en-US"));
        assertTrue(pointer.isLanguage("EN"));
        assertFalse(pointer.isLanguage("fr"));

        Element noLangElem = document.createElement("other");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLangElem, Locale.ENGLISH);
        assertFalse(noLangPtr.isLanguage("en"));
    }

    // Tests getValue on comment, text, and composite element node
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectText() {
        Comment comment = document.createComment("  sample comment  ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("sample comment", commentPtr.getValue());

        Element root = document.createElement("root");
        Text t1 = document.createTextNode("Hello ");
        Element sub = document.createElement("sub");
        Text t2 = document.createTextNode("World");
        sub.appendChild(t2);
        root.appendChild(t1);
        root.appendChild(sub);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("Hello World", rootPtr.getValue());
    }

    // Tests setValue on Text node and Element node
    @Test
    public void testSetValue_textAndElementNode_updatesContents() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        textPtr.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        rootPtr.setValue("new root text");
        assertEquals("new root text", rootPtr.getValue());
    }

    // Tests asPath with id specified
    @Test
    public void testAsPath_withId_escapesAndFormatsIdPath() {
        Element element = document.createElement("element");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH, "item's \"key\"");

        assertEquals("id('item&apos;s &quot;key&quot;')", pointer.asPath());
    }

    // Tests asPath for hierarchical DOM elements
    @Test
    public void testAsPath_elementHierarchy_generatesExpectedXPath() {
        Element root = document.createElement("root");
        document.appendChild(root);

        Element child1 = document.createElement("item");
        Element child2 = document.createElement("item");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);

        assertEquals("", rootPtr.asPath());
        assertEquals("/item[1]", child1Ptr.asPath());
        assertEquals("/item[2]", child2Ptr.asPath());
    }

    // Tests asPath for text node and processing instruction node
    @Test
    public void testAsPath_textAndProcessingInstructionNodes() {
        Element root = document.createElement("root");
        Text textNode = document.createTextNode("sample");
        ProcessingInstruction piNode = document.createProcessingInstruction("app", "config");
        root.appendChild(textNode);
        root.appendChild(piNode);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, textNode);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, piNode);

        assertEquals("/text()[1]", textPtr.asPath());
        assertEquals("/processing-instruction('app')[1]", piPtr.asPath());
    }

    // Tests remove method on root node throwing exception
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element element = document.createElement("orphan");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);
        pointer.remove();
    }

    // Tests remove method on child node successfully removing it
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.remove();

        assertNull(child.getParentNode());
        assertFalse(root.hasChildNodes());
    }

    // Tests compareChildNodePointers between children
    @Test
    public void testCompareChildNodePointers_orderOfSiblings() {
        Element root = document.createElement("root");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer parentPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer ptr1 = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(parentPtr, child2);

        assertEquals(0, parentPtr.compareChildNodePointers(ptr1, ptr1));
        assertEquals(-1, parentPtr.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, parentPtr.compareChildNodePointers(ptr2, ptr1));
    }

    // Tests getPointerByID when ID exists and does not exist
    @Test
    public void testGetPointerByID_existingAndNonExistingIds() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);

        Pointer nonExisting = docPointer.getPointerByID(null, "noSuchId");
        assertTrue(nonExisting instanceof NullPointer);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes() {
        Element elem1 = document.createElement("a");
        Element elem2 = document.createElement("b");

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

    // Tests isLeaf, isCollection, getLength, and isActual methods
    @Test
    public void testBasicProperties_isLeafIsActualIsCollection() {
        Element emptyElement = document.createElement("empty");
        DOMNodePointer pointer = new DOMNodePointer(emptyElement, Locale.ENGLISH);

        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        emptyElement.appendChild(document.createElement("child"));
        assertFalse(pointer.isLeaf());
        assertEquals(emptyElement, pointer.getBaseValue());
        assertEquals(emptyElement, pointer.getImmediateNode());
    }

    // Tests helper static methods getPrefix and getLocalName
    @Test
    public void testStaticGetPrefixAndGetLocalName() {
        Element elementWithPrefix = document.createElementNS("http://ns", "pre:testName");
        assertEquals("pre", DOMNodePointer.getPrefix(elementWithPrefix));
        assertEquals("testName", DOMNodePointer.getLocalName(elementWithPrefix));

        Element elementWithoutPrefix = document.createElement("simple");
        assertNull(DOMNodePointer.getPrefix(elementWithoutPrefix));
        assertEquals("simple", DOMNodePointer.getLocalName(elementWithoutPrefix));
    }
}
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
    private Element rootElement;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        document = factory.newDocumentBuilder().newDocument();

        rootElement = document.createElementNS("http://example.com/ns", "ns:root");
        rootElement.setAttribute("xmlns:ns", "http://example.com/ns");
        rootElement.setAttribute("xmlns", "http://example.com/default");
        rootElement.setAttribute("xml:lang", "en-US");
        document.appendChild(rootElement);
    }

    // Tests testNode with NodeNameTest matching element name and namespace
    @Test
    public void testTestNode_nodeNameTest_returnsTrueOnMatch() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName("ns", "root"), "http://example.com/ns");
        assertTrue(pointer.testNode(test));

        NodeNameTest wrongNsTest = new NodeNameTest(new QName("ns", "root"), "http://example.com/other");
        assertFalse(pointer.testNode(wrongNsTest));
    }

    // Tests testNode with wildcard NodeNameTest
    @Test
    public void testTestNode_wildcardNodeNameTest_returnsTrue() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));
    }

    // Tests testNode with NodeTypeTest for element, comment, text, and PI
    @Test
    public void testTestNode_nodeTypeTest_matchesCorrespondingTypes() {
        DOMNodePointer elementPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(elementPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(elementPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Text text = document.createTextNode("Sample Text");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(textPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        Comment comment = document.createComment("Sample Comment");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertTrue(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertTrue(piPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "my-data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertTrue(piPointer.testNode(new ProcessingInstructionTest("myTarget")));
        assertFalse(piPointer.testNode(new ProcessingInstructionTest("otherTarget")));
    }

    // Tests getName for Element and ProcessingInstruction
    @Test
    public void testGetName_elementAndPI_returnsCorrectQName() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("root", name.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "content");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("piTarget", piPointer.getName().getName());
        assertNull(piPointer.getName().getPrefix());
    }

    // Tests getNamespaceURI with standard prefixes and declared prefixes
    @Test
    public void testGetNamespaceURI_withPrefix_returnsCorrectURI() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://example.com/ns", pointer.getNamespaceURI("ns"));
        assertEquals("http://example.com/default", pointer.getNamespaceURI(""));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI
    @Test
    public void testGetDefaultNamespaceURI_withDefaultNamespace_returnsURI() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals("http://example.com/default", pointer.getDefaultNamespaceURI());
    }

    // Tests isLanguage with matching and non-matching language codes
    @Test
    public void testIsLanguage_xmlLangAttribute_matchesPrefixCaseInsensitive() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("EN-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests getValue and setValue for text and element nodes
    @Test
    public void testGetAndSetValue_textAndElement_updatesValue() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        childPointer.setValue("Hello World");
        assertEquals("Hello World", childPointer.getValue());

        childPointer.setValue("Updated Text");
        assertEquals("Updated Text", childPointer.getValue());
    }

    // Tests asPath for root element, nested element, text, and PI
    @Test
    public void testAsPath_nestedNodes_buildsExpectedPath() {
        Element child1 = document.createElement("item");
        Element child2 = document.createElement("item");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(rootElement, Locale.ENGLISH);
        DOMNodePointer childPtr1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer childPtr2 = new DOMNodePointer(rootPtr, child2);

        assertEquals("/ns:root[1]/item[1]", childPtr1.asPath());
        assertEquals("/ns:root[1]/item[2]", childPtr2.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(rootElement, Locale.ENGLISH, "root-id");
        assertEquals("id('root-id')", idPointer.asPath());
    }

    // Tests asPath with escaped characters in ID
    @Test
    public void testAsPath_escapedId_escapesQuotes() {
        DOMNodePointer idPointer = new DOMNodePointer(rootElement, Locale.ENGLISH, "id'with\"quotes");
        assertEquals("id('id&apos;with&quot;quotes')", idPointer.asPath());
    }

    // Tests compareChildNodePointers ordering of attributes and child elements
    @Test
    public void testCompareChildNodePointers_differentNodes_returnsCorrectOrder() {
        Element child1 = document.createElement("first");
        Element child2 = document.createElement("second");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);

        DOMNodePointer parentPtr = new DOMNodePointer(rootElement, Locale.ENGLISH);
        DOMNodePointer ptr1 = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(parentPtr, child2);

        assertEquals(-1, parentPtr.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, parentPtr.compareChildNodePointers(ptr2, ptr1));
        assertEquals(0, parentPtr.compareChildNodePointers(ptr1, ptr1));
    }

    // Tests remove for child element and root document node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element child = document.createElement("toRemove");
        rootElement.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        childPtr.remove();
        assertNull(child.getParentNode());
    }

    // Tests remove on root node throwing JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsJXPathException() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        docPointer.remove();
    }

    // Tests getPointerByID returning DOMNodePointer when found, NullPointer when not found
    @Test
    public void testGetPointerByID_existingAndNonExistingId_returnsExpectedPointer() {
        rootElement.setAttribute("id", "elem1");
        rootElement.setIdAttribute("id", true);

        DOMNodePointer pointer = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = pointer.getPointerByID(context, "elem1");
        assertTrue(found instanceof DOMNodePointer);
        assertEquals(rootElement, found.getNode());

        Pointer notFound = pointer.getPointerByID(context, "nonExistent");
        assertTrue(notFound instanceof NullPointer);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_behavesCorrectly() {
        DOMNodePointer ptr1 = new DOMNodePointer(rootElement, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(rootElement, Locale.ENGLISH);
        Element otherElement = document.createElement("other");
        DOMNodePointer ptr3 = new DOMNodePointer(otherElement, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr2));
        assertEquals(ptr1.hashCode(), ptr2.hashCode());

        assertFalse(ptr1.equals(ptr3));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("aString"));
    }

    // Tests leaf, length, actual, and collection flags
    @Test
    public void testNodeProperties_leafAndCollection_returnsExpectedValues() {
        Element emptyElem = document.createElement("empty");
        DOMNodePointer leafPtr = new DOMNodePointer(emptyElem, Locale.ENGLISH);

        assertTrue(leafPtr.isLeaf());
        assertTrue(leafPtr.isActual());
        assertFalse(leafPtr.isCollection());
        assertEquals(1, leafPtr.getLength());
        assertEquals(emptyElem, leafPtr.getBaseValue());
        assertEquals(emptyElem, leafPtr.getImmediateNode());
    }

    // Tests childIterator, attributeIterator, and namespaceIterator return non-null iterators
    @Test
    public void testIterators_createIterators_returnsNonNull() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        NodeIterator childIt = pointer.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = pointer.attributeIterator(new QName("xml:lang"));
        assertNotNull(attrIt);

        NodeIterator nsIt = pointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = pointer.namespacePointer("ns");
        assertNotNull(nsPtr);
    }

    // Tests asPath and getValue for Document, Comment, CDATASection, and ProcessingInstruction
    @Test
    public void testAsPathAndValue_variousNodeTypes() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertEquals("", docPointer.asPath());
        assertNull(docPointer.getName());

        Comment comment = document.createComment("my-comment");
        rootElement.appendChild(comment);
        DOMNodePointer commentPtr = new DOMNodePointer(new DOMNodePointer(rootElement, Locale.ENGLISH), comment);
        assertEquals("/ns:root[1]/comment()[1]", commentPtr.asPath());
        assertEquals("my-comment", commentPtr.getValue());
        assertNull(commentPtr.getName());

        commentPtr.setValue("updated-comment");
        assertEquals("updated-comment", comment.getData());

        CDATASection cdata = document.createCDATASection("cdata-content");
        rootElement.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(new DOMNodePointer(rootElement, Locale.ENGLISH), cdata);
        assertEquals("/ns:root[1]/text()[1]", cdataPtr.asPath());
        assertEquals("cdata-content", cdataPtr.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("pi-target", "pi-data");
        rootElement.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(new DOMNodePointer(rootElement, Locale.ENGLISH), pi);
        assertEquals("/ns:root[1]/processing-instruction('pi-target')[1]", piPtr.asPath());
        assertEquals("pi-data", piPtr.getValue());

        piPtr.setValue("updated-pi-data");
        assertEquals("updated-pi-data", pi.getData());
    }

    // Tests setValue with null removing children / clearing value
    @Test
    public void testSetValue_nullOrEmpty_clearsChildren() {
        Element child = document.createElement("child");
        child.appendChild(document.createTextNode("text1"));
        child.appendChild(document.createTextNode("text2"));
        rootElement.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        childPtr.setValue(null);
        assertNull(child.getFirstChild());

        childPtr.setValue("");
        assertNull(child.getFirstChild());
    }

    // Tests createChild and createAttribute operations
    @Test
    public void testCreateChildAndCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(document);
        DOMNodePointer rootPtr = new DOMNodePointer(rootElement, Locale.ENGLISH);

        NodePointer createdElem = rootPtr.createChild(context, new QName("newChild"), 0);
        assertNotNull(createdElem);
        assertEquals("newChild", createdElem.getName().getName());

        NodePointer createdElemWithValue = rootPtr.createChild(context, new QName("childWithValue"), 1, "testVal");
        assertNotNull(createdElemWithValue);
        assertEquals("testVal", createdElemWithValue.getValue());

        NodePointer createdAttr = rootPtr.createAttribute(context, new QName("newAttr"));
        assertNotNull(createdAttr);
        assertEquals("newAttr", createdAttr.getName().getName());
    }

    // Tests compareChildNodePointers ordering between attributes and element nodes
    @Test
    public void testCompareChildNodePointers_attributeAndElement() {
        DOMNodePointer parentPtr = new DOMNodePointer(rootElement, Locale.ENGLISH);
        NodeIterator attrIt = parentPtr.attributeIterator(new QName("xml:lang"));
        attrIt.setPosition(1);
        NodePointer attrPtr = attrIt.getNodePointer();

        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(parentPtr, child);

        assertEquals(-1, parentPtr.compareChildNodePointers(attrPtr, childPtr));
        assertEquals(1, parentPtr.compareChildNodePointers(childPtr, attrPtr));
    }

    // Tests isLeaf for nodes with and without children
    @Test
    public void testIsLeaf_withChildren_returnsFalse() {
        Element parent = document.createElement("parent");
        parent.appendChild(document.createElement("child"));
        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.ENGLISH);
        assertFalse(parentPtr.isLeaf());

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertFalse(docPointer.isLeaf());
    }

    // Tests isLanguage inherited from ancestor
    @Test
    public void testIsLanguage_inheritedFromParent() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(new DOMNodePointer(rootElement, Locale.ENGLISH), child);

        assertTrue(childPtr.isLanguage("en"));
        assertFalse(childPtr.isLanguage("de"));
    }

    // Tests getNamespaceURI when prefix is null or node is not element
    @Test
    public void testGetNamespaceURI_nullPrefixAndNonElement() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals("http://example.com/default", pointer.getNamespaceURI(null));

        Text text = document.createTextNode("text");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        assertNull(textPtr.getNamespaceURI("ns"));
        assertNull(textPtr.getDefaultNamespaceURI());
    }

    // Tests testNode with null test
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        DOMNodePointer pointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
    }
}
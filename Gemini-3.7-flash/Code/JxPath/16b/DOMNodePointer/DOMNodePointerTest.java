package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DOMNodePointerTest {

    private Document doc;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
    }

    // Tests testNode with null test returning true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element element = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
    }

    // Tests testNode with NodeNameTest matching element name and namespace
    @Test
    public void testTestNode_nodeNameTest_matchesElement() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:item");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        NodeNameTest nameTest = new NodeNameTest(new QName("ns", "item"), "http://example.com/ns");
        assertTrue(pointer.testNode(nameTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));

        NodeNameTest mismatchTest = new NodeNameTest(new QName("ns", "other"), "http://example.com/ns");
        assertFalse(pointer.testNode(mismatchTest));

        Text text = doc.createTextNode("sample");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertFalse(textPointer.testNode(nameTest));
    }

    // Tests testNode with NodeTypeTest for various node types
    @Test
    public void testTestNode_nodeTypeTest_validatesCorrectly() {
        Element element = doc.createElement("item");
        Text text = doc.createTextNode("content");
        Comment comment = doc.createComment("a comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        DOMNodePointer elemPtr = new DOMNodePointer(element, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);

        assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\"");
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);

        assertTrue(pointer.testNode(new ProcessingInstructionTest("xml-stylesheet")));
        assertFalse(pointer.testNode(new ProcessingInstructionTest("other")));
    }

    // Tests getName for Element and ProcessingInstruction
    @Test
    public void testGetName_elementAndPI_returnsExpectedQName() {
        Element element = doc.createElementNS("http://example.com", "prefix:elem");
        DOMNodePointer elemPtr = new DOMNodePointer(element, Locale.ENGLISH);
        QName qName = elemPtr.getName();
        assertEquals("prefix", qName.getPrefix());
        assertEquals("elem", qName.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("myTarget", piPtr.getName().getName());
        assertNull(piPtr.getName().getPrefix());
    }

    // Tests getNamespaceURI with special prefixes xml, xmlns, custom and unknown
    @Test
    public void testGetNamespaceURI_prefixes_resolvesCorrectly() {
        Element root = doc.createElementNS("http://default.com", "root");
        root.setAttribute("xmlns", "http://default.com");
        root.setAttribute("xmlns:custom", "http://custom.com");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://default.com", pointer.getNamespaceURI(null));
        assertEquals("http://default.com", pointer.getNamespaceURI(""));
        assertEquals("http://custom.com", pointer.getNamespaceURI("custom"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI on element without default namespace
    @Test
    public void testGetDefaultNamespaceURI_noDefault_returnsNull() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    // Tests basic pointer properties: length, leaf, actual, collection
    @Test
    public void testBasicProperties_variousStates_returnsCorrectValues() {
        Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertTrue(pointer.isLeaf());

        root.appendChild(doc.createTextNode("child"));
        assertFalse(pointer.isLeaf());
        assertEquals(root, pointer.getBaseValue());
        assertEquals(root, pointer.getImmediateNode());
    }

    // Tests isLanguage and language lookup from xml:lang attribute
    @Test
    public void testIsLanguage_withXmlLang_evaluatesCorrectly() {
        Element root = doc.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("EN-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests getValue for Text, Element, CDATA, and Comment nodes
    @Test
    public void testGetValue_differentNodeTypes_returnsExpectedString() {
        Comment comment = doc.createComment(" sample comment ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("sample comment", commentPtr.getValue());

        Element root = doc.createElement("root");
        Text text1 = doc.createTextNode("Hello ");
        CDATASection cdata = doc.createCDATASection("World");
        root.appendChild(text1);
        root.appendChild(cdata);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("Hello World", rootPtr.getValue());
    }

    // Tests setValue replacing children of an element with String value
    @Test
    public void testSetValue_elementNodeWithString_replacesChildren() {
        Element root = doc.createElement("root");
        root.appendChild(doc.createElement("child"));
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        rootPtr.setValue("New Text");
        assertEquals("New Text", rootPtr.getValue());
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals(Node.TEXT_NODE, root.getFirstChild().getNodeType());
    }

    // Tests setValue on Text node and clearing text node removes it from parent
    @Test
    public void testSetValue_textNode_updatesOrRemoves() {
        Element root = doc.createElement("root");
        Text textNode = doc.createTextNode("initial");
        root.appendChild(textNode);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, textNode);
        textPtr.setValue("updated");
        assertEquals("updated", textNode.getNodeValue());

        textPtr.setValue("");
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests remove method on child element and exception when removing root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);
        childPtr.remove();
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests remove method throws JXPathException when node has no parent
    @Test(expected = JXPathException.class)
    public void testRemove_nodeWithoutParent_throwsException() {
        Element orphan = doc.createElement("orphan");
        DOMNodePointer orphanPtr = new DOMNodePointer(orphan, Locale.ENGLISH);
        orphanPtr.remove();
    }

    // Tests asPath with id, element, text, cdata, and processing instruction
    @Test
    public void testAsPath_variousHierarchy_buildsCorrectXPath() {
        DOMNodePointer idPtr = new DOMNodePointer(doc, Locale.ENGLISH, "item'1\"");
        assertEquals("id('item&apos;1&quot;')", idPtr.asPath());

        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Element child1 = doc.createElement("child");
        Element child2 = doc.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);

        assertEquals("/child[1]", child1Ptr.asPath());
        assertEquals("/child[2]", child2Ptr.asPath());

        Text text = doc.createTextNode("txt");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, text);
        assertEquals("/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = doc.createProcessingInstruction("test-pi", "data");
        root.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, pi);
        assertEquals("/processing-instruction('test-pi')[1]", piPtr.asPath());
    }

    // Tests getPointerByID finding existing element and returning NullPointer when not found
    @Test
    public void testGetPointerByID_elementLookup_returnsPointerOrNullPointer() {
        Element root = doc.createElement("root");
        root.setAttribute("id", "elem1");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        Pointer foundPtr = docPtr.getPointerByID(context, "elem1");
        assertNotNull(foundPtr);
        assertTrue(foundPtr instanceof DOMNodePointer);
        assertEquals(root, foundPtr.getNode());

        Pointer notFoundPtr = docPtr.getPointerByID(context, "nonExistent");
        assertTrue(notFoundPtr instanceof NullPointer);
    }

    // Tests compareChildNodePointers with attributes and child elements
    @Test
    public void testCompareChildNodePointers_elementsAndAttributes_ordersCorrectly() {
        Element parent = doc.createElement("parent");
        parent.setAttribute("attr1", "val1");
        parent.setAttribute("attr2", "val2");

        Element child1 = doc.createElement("child1");
        Element child2 = doc.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer child1Ptr = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(parentPtr, child2);

        assertEquals(0, parentPtr.compareChildNodePointers(child1Ptr, child1Ptr));
        assertEquals(-1, parentPtr.compareChildNodePointers(child1Ptr, child2Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child2Ptr, child1Ptr));

        Attr attr1 = parent.getAttributeNode("attr1");
        DOMNodePointer attr1Ptr = new DOMNodePointer(parentPtr, attr1);
        assertEquals(-1, parentPtr.compareChildNodePointers(attr1Ptr, child1Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child1Ptr, attr1Ptr));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_validatesEquality() {
        Element elem1 = doc.createElement("elem1");
        Element elem2 = doc.createElement("elem2");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr1Same = new DOMNodePointer(elem1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr1Same));
        assertFalse(ptr1.equals(ptr2));
        assertFalse(ptr1.equals("non-pointer"));
        assertEquals(ptr1.hashCode(), ptr1Same.hashCode());
    }

    // Tests createAttribute with and without namespace prefix
    @Test
    public void testCreateAttribute_simpleAndPrefixed_setsAttribute() {
        Element elem = doc.createElementNS("http://example.com/ns", "ns:root");
        elem.setAttribute("xmlns:ns", "http://example.com/ns");
        doc.appendChild(elem);

        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        NodePointer attrPtr = elemPtr.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr);
        assertTrue(elem.hasAttribute("simpleAttr"));

        NodePointer nsAttrPtr = elemPtr.createAttribute(context, new QName("ns", "prefixedAttr"));
        assertNotNull(nsAttrPtr);
        assertTrue(elem.hasAttributeNS("http://example.com/ns", "prefixedAttr"));
    }

    // Tests createAttribute throwing exception when namespace prefix is unknown
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element elem = doc.createElement("root");
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        elemPtr.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests iterators and namespace resolver creation
    @Test
    public void testIteratorsAndResolver_instantiation_returnsNonNullObjects() {
        Element elem = doc.createElement("root");
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);

        NodeIterator childIt = elemPtr.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = elemPtr.attributeIterator(new QName("test"));
        assertNotNull(attrIt);

        NodeIterator nsIt = elemPtr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = elemPtr.namespacePointer("xml");
        assertNotNull(nsPtr);

        NamespaceResolver resolver = elemPtr.getNamespaceResolver();
        assertNotNull(resolver);
    }

    // Tests createChild by index and by value
    @Test
    public void testCreateChild_elementAndValue_createsCorrectChild() {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        NodePointer childPtr = rootPtr.createChild(context, new QName("child"), 0);
        assertNotNull(childPtr);
        assertEquals("child", childPtr.getName().getName());
        assertEquals(1, root.getChildNodes().getLength());

        NodePointer childWithValPtr = rootPtr.createChild(context, new QName("childWithVal"), 1, "childValue");
        assertNotNull(childWithValPtr);
        assertEquals("childValue", childWithValPtr.getValue());
    }

    // Tests setValue with DocumentFragment, Node, and null values
    @Test
    public void testSetValue_variousObjectTypes_replacesOrRemovesChildren() {
        Element root = doc.createElement("root");
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        DocumentFragment df = doc.createDocumentFragment();
        df.appendChild(doc.createElement("child1"));
        df.appendChild(doc.createElement("child2"));
        rootPtr.setValue(df);
        assertEquals(2, root.getChildNodes().getLength());

        Element newChild = doc.createElement("newChild");
        rootPtr.setValue(newChild);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("newChild", root.getFirstChild().getNodeName());

        rootPtr.setValue(null);
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests asPath with document root and comments
    @Test
    public void testAsPath_documentAndComment_generatesCorrectPath() {
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());

        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);
        assertEquals("/root[1]", rootPtr.asPath());

        Comment comment = doc.createComment("my comment");
        root.appendChild(comment);
        DOMNodePointer commentPtr = new DOMNodePointer(rootPtr, comment);
        assertEquals("/root[1]/comment()[1]", commentPtr.asPath());
    }

    // Tests isLanguage inherited from ancestor elements
    @Test
    public void testIsLanguage_inheritedFromParent_evaluatesCorrectly() {
        Element root = doc.createElement("root");
        root.setAttribute("xml:lang", "en");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        assertTrue(childPtr.isLanguage("en"));
        assertFalse(childPtr.isLanguage("fr"));
    }
}
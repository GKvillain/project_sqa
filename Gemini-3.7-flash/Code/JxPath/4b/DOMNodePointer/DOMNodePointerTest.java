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

    // Tests testNode with NodeNameTest on Element and Text nodes
    @Test
    public void testTestNode_nodeNameTest() {
        Element element = document.createElementNS("http://example.com/ns", "ns:item");
        Text text = document.createTextNode("text");

        NodeNameTest exactTest = new NodeNameTest(new QName("ns", "item"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(element, exactTest));
        assertFalse(DOMNodePointer.testNode(text, exactTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(element, wildcardTest));

        NodeNameTest nonMatchingNsTest = new NodeNameTest(new QName("ns", "item"), "http://other.com");
        assertFalse(DOMNodePointer.testNode(element, nonMatchingNsTest));

        NodeNameTest nullPrefixWildcard = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(element, nullPrefixWildcard));
    }

    // Tests testNode with NodeTypeTest covering various DOM node types
    @Test
    public void testTestNode_nodeTypeTest() {
        Element element = document.createElement("root");
        Text text = document.createTextNode("content");
        CDATASection cdata = document.createCDATASection("cdata content");
        Comment comment = document.createComment("a comment");
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(document, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        assertTrue(DOMNodePointer.testNode(element, null));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest() {
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "data");
        Element element = document.createElement("root");

        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("targetPI")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("otherPI")));
        assertFalse(DOMNodePointer.testNode(element, new ProcessingInstructionTest("targetPI")));
    }

    // Tests getName for Element and ProcessingInstruction
    @Test
    public void testGetName_elementAndProcessingInstruction() {
        Element element = document.createElementNS("http://example.com", "prefix:elementName");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("prefix", name.getPrefix());
        assertEquals("elementName", name.getName());

        ProcessingInstruction pi = document.createProcessingInstruction("piTarget", "piData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("piTarget", piName.getName());
    }

    // Tests getNamespaceURI with standard prefix, xmlns prefix and declared attributes
    @Test
    public void testGetNamespaceURI_predefinedAndCustomPrefix() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:custom", "http://custom.com");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
        assertEquals("http://custom.com", pointer.getNamespaceURI("custom"));
        assertNull(pointer.getNamespaceURI("unknownPrefix"));
    }

    // Tests getDefaultNamespaceURI for element with and without xmlns
    @Test
    public void testGetDefaultNamespaceURI_withAndWithoutDefaultNS() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns", "http://default.com");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://default.com", pointer.getDefaultNamespaceURI());
        assertEquals("http://default.com", pointer.getNamespaceURI(""));
        assertEquals("http://default.com", pointer.getNamespaceURI((String) null));

        Element simpleRoot = document.createElement("simple");
        DOMNodePointer simplePointer = new DOMNodePointer(simpleRoot, Locale.ENGLISH);
        assertNull(simplePointer.getDefaultNamespaceURI());
    }

    // Tests isLanguage check from xml:lang attribute
    @Test
    public void testIsLanguage_withXmlLangAttribute() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("en-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests setValue on a Text node
    @Test
    public void testSetValue_textNode() {
        Element root = document.createElement("root");
        Text text = document.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        textPointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        textPointer.setValue("");
        assertNull(text.getParentNode());
    }

    // Tests setValue on an Element node with String and Element node values
    @Test
    public void testSetValue_elementNode() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        pointer.setValue("text content");
        assertEquals("text content", root.getTextContent());

        Element newChild = document.createElement("sub");
        newChild.setTextContent("sub text");
        pointer.setValue(newChild);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("sub", root.getFirstChild().getNodeName());
    }

    // Tests getValue on comment, text, PI and element hierarchy
    @Test
    public void testGetValue_differentNodeTypes() {
        Comment comment = document.createComment(" comment text ");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment text", commentPointer.getValue());

        Text text = document.createTextNode(" text value ");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        assertEquals("text value", textPointer.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", " pi text ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi text", piPointer.getValue());

        Element root = document.createElement("root");
        Element child1 = document.createElement("child");
        child1.appendChild(document.createTextNode("hello"));
        Element child2 = document.createElement("child");
        child2.appendChild(document.createTextNode("world"));
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("helloworld", rootPointer.getValue());
    }

    // Tests asPath with id, element, text, and PI
    @Test
    public void testAsPath_elementsAndNodes() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);

        Element child = document.createElement("item");
        root.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        assertEquals("/item[1]", childPointer.asPath());

        Text text = document.createTextNode("hello");
        child.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(childPointer, text);
        assertEquals("/item[1]/text()[1]", textPointer.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("test-pi", "data");
        child.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(childPointer, pi);
        assertEquals("/item[1]/processing-instruction('test-pi')[1]", piPointer.asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.ENGLISH, "root-id");
        assertEquals("id('root-id')", idPointer.asPath());
    }

    // Tests createAttribute with and without namespace prefix
    @Test
    public void testCreateAttribute_withAndWithoutPrefix() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:ns", "http://example.com/ns");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("attr"));
        assertNotNull(attrPointer);
        assertTrue(root.hasAttribute("attr"));

        NodePointer nsAttrPointer = pointer.createAttribute(context, new QName("ns", "attr2"));
        assertNotNull(nsAttrPointer);
        assertEquals("http://example.com/ns", root.getAttributeNodeNS("http://example.com/ns", "attr2").getNamespaceURI());
    }

    // Tests createAttribute throws JXPathException on unknown prefix
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests remove method on child node and root node
    @Test
    public void testRemove_childNode() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        childPointer.remove();
        assertFalse(root.hasChildNodes());
    }

    // Tests remove on root node throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        rootPointer.remove();
    }

    // Tests compareChildNodePointers between child elements and attributes
    @Test
    public void testCompareChildNodePointers() {
        Element root = document.createElement("root");
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer ptr1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(-1, rootPointer.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, rootPointer.compareChildNodePointers(ptr2, ptr1));
        assertEquals(0, rootPointer.compareChildNodePointers(ptr1, ptr1));

        DOMNodePointer attrPtr = new DOMNodePointer(rootPointer, root.getAttributeNode("attr1"));
        assertEquals(-1, rootPointer.compareChildNodePointers(attrPtr, ptr1));
        assertEquals(1, rootPointer.compareChildNodePointers(ptr1, attrPtr));
    }

    // Tests getPointerByID for existing and non-existing IDs
    @Test
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        root.setAttribute("id", "elem1");
        root.setIdAttribute("id", true);
        document.appendChild(root);

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = docPointer.getPointerByID(context, "elem1");
        assertTrue(found instanceof DOMNodePointer);
        assertEquals(root, found.getNode());

        Pointer notFound = docPointer.getPointerByID(context, "nonexistent");
        assertTrue(notFound instanceof NullPointer);
    }

    // Tests equals, hashCode, isLeaf, isCollection, getLength, and getBaseValue
    @Test
    public void testGeneralPropertiesAndEquals() {
        Element root = document.createElement("root");
        DOMNodePointer pointer1 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(root, Locale.ENGLISH);
        Element other = document.createElement("other");
        DOMNodePointer pointer3 = new DOMNodePointer(other, Locale.ENGLISH);

        assertTrue(pointer1.equals(pointer1));
        assertTrue(pointer1.equals(pointer2));
        assertFalse(pointer1.equals(pointer3));
        assertFalse(pointer1.equals(null));
        assertEquals(pointer1.hashCode(), pointer2.hashCode());

        assertTrue(pointer1.isActual());
        assertFalse(pointer1.isCollection());
        assertEquals(1, pointer1.getLength());
        assertTrue(pointer1.isLeaf());

        root.appendChild(document.createElement("child"));
        assertFalse(pointer1.isLeaf());

        assertEquals(root, pointer1.getBaseValue());
        assertEquals(root, pointer1.getImmediateNode());
    }

    // Tests static helper methods getPrefix, getLocalName, getNamespaceURI
    @Test
    public void testStaticHelperMethods() {
        Element element = document.createElementNS("http://example.com/ns", "ns:myTag");
        assertEquals("ns", DOMNodePointer.getPrefix(element));
        assertEquals("myTag", DOMNodePointer.getLocalName(element));
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(element));

        Element simpleElem = document.createElement("simpleTag");
        assertNull(DOMNodePointer.getPrefix(simpleElem));
        assertEquals("simpleTag", DOMNodePointer.getLocalName(simpleElem));
    }

    // Tests createChild with and without prefix, index and value
    @Test
    public void testCreateChild_elementAndValues() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:ns", "http://example.com/ns");
        document.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        NodePointer childPtr1 = pointer.createChild(context, new QName("item"), 0);
        assertNotNull(childPtr1);
        assertEquals(1, root.getElementsByTagName("item").getLength());

        NodePointer childPtr2 = pointer.createChild(context, new QName("ns", "item2"), 1, "customValue");
        assertNotNull(childPtr2);
        assertEquals("customValue", childPtr2.getValue());

        NodePointer appendChild = pointer.createChild(context, new QName("item"), NodePointer.WHOLE_COLLECTION);
        assertNotNull(appendChild);
    }

    // Tests createChild throws JXPathException for unknown prefix
    @Test(expected = JXPathException.class)
    public void testCreateChild_unknownPrefix_throwsException() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        pointer.createChild(context, new QName("unknown", "child"), 0);
    }

    // Tests childIterator, attributeIterator, namespaceIterator, and namespacePointer
    @Test
    public void testIteratorsAndNamespacePointer() {
        Element root = document.createElement("root");
        root.setAttribute("xmlns:ns", "http://example.com/ns");
        root.setAttribute("attr", "val");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = rootPointer.childIterator(new NodeNameTest(new QName("child")), false, null);
        assertNotNull(childIt);
        assertTrue(childIt.setPosition(1));
        assertNotNull(childIt.getNodePointer());

        NodeIterator attrIt = rootPointer.attributeIterator(new QName("attr"));
        assertNotNull(attrIt);
        assertTrue(attrIt.setPosition(1));
        assertNotNull(attrIt.getNodePointer());

        NodeIterator nsIt = rootPointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPointer.namespacePointer("ns");
        assertNotNull(nsPtr);
        assertEquals("http://example.com/ns", nsPtr.getNamespaceURI());
    }

    // Tests getNamespaceResolver and language inheritance
    @Test
    public void testNamespaceResolverAndLanguage() {
        Element root = document.createElement("root");
        root.setAttribute("xml:lang", "en-GB");
        Element child = document.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        NamespaceResolver resolver = childPointer.getNamespaceResolver();
        assertNotNull(resolver);

        assertEquals("en-gb", childPointer.getLanguage().toLowerCase(Locale.ENGLISH));
    }

    // Tests asPath with Document and Attribute nodes
    @Test
    public void testAsPath_documentAndAttribute() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        assertEquals("", docPointer.asPath());

        Element root = document.createElement("root");
        root.setAttribute("testAttr", "val");
        document.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(docPointer, root);
        Attr attr = root.getAttributeNode("testAttr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);

        assertEquals("/root[1]/@testAttr", attrPointer.asPath());
    }

    // Tests setValue with DocumentFragment and null
    @Test
    public void testSetValue_documentFragmentAndNull() {
        Element root = document.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        DocumentFragment fragment = document.createDocumentFragment();
        Element f1 = document.createElement("fragmentChild");
        fragment.appendChild(f1);
        pointer.setValue(fragment);
        assertEquals(1, root.getChildNodes().getLength());

        pointer.setValue(null);
        assertEquals(0, root.getChildNodes().getLength());
    }

    // Tests getPointerByKey
    @Test
    public void testGetPointerByKey() {
        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer ptr = docPointer.getPointerByKey(context, "keyName", "keyValue");
        assertNotNull(ptr);
    }

    // Tests isLeaf for CDATASection and Comment nodes
    @Test
    public void testIsLeaf_variousNodes() {
        CDATASection cdata = document.createCDATASection("text");
        DOMNodePointer cdataPointer = new DOMNodePointer(cdata, Locale.ENGLISH);
        assertTrue(cdataPointer.isLeaf());

        Comment comment = document.createComment("comment");
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        assertTrue(commentPointer.isLeaf());
    }
}
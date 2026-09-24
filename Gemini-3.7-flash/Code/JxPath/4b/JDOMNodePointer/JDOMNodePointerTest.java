package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMNodePointerTest {

    // Tests getName on Element and ProcessingInstruction
    @Test
    public void testGetName_elementAndPI_returnsCorrectQName() {
        Element element = new Element("testElem", "ns", "http://example.com");
        JDOMNodePointer elemPointer = new JDOMNodePointer(element, Locale.ENGLISH);
        QName elemName = elemPointer.getName();
        assertEquals("ns", elemName.getPrefix());
        assertEquals("testElem", elemName.getName());

        ProcessingInstruction pi = new ProcessingInstruction("targetPI", "data");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("targetPI", piName.getName());
    }

    // Tests getNamespaceURI for element and document with prefix
    @Test
    public void testGetNamespaceURI_withPrefix_returnsCorrectURI() {
        Element root = new Element("root");
        Namespace ns = Namespace.getNamespace("custom", "http://custom.org");
        root.addNamespaceDeclaration(ns);
        Document doc = new Document(root);

        JDOMNodePointer docPointer = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://custom.org", docPointer.getNamespaceURI("custom"));
        assertNull(docPointer.getNamespaceURI("unknown"));

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        assertEquals("http://custom.org", rootPointer.getNamespaceURI("custom"));
        assertNull(rootPointer.getNamespaceURI("unknown"));
    }

    // Tests getNamespaceURI without arguments for element with and without namespace
    @Test
    public void testGetNamespaceURI_noPrefix_returnsNamespaceURI() {
        Element elementWithNs = new Element("elem", "http://example.com");
        JDOMNodePointer pointerWithNs = new JDOMNodePointer(elementWithNs, Locale.ENGLISH);
        assertEquals("http://example.com", pointerWithNs.getNamespaceURI());

        Element elementWithoutNs = new Element("elem");
        JDOMNodePointer pointerWithoutNs = new JDOMNodePointer(elementWithoutNs, Locale.ENGLISH);
        assertNull(pointerWithoutNs.getNamespaceURI());

        Text text = new Text("sample text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertNull(textPointer.getNamespaceURI());
    }

    // Tests isLeaf for empty/non-empty Element, Document, and other node types
    @Test
    public void testIsLeaf_variousNodes_returnsExpected() {
        Element emptyElem = new Element("elem");
        JDOMNodePointer emptyPointer = new JDOMNodePointer(emptyElem, Locale.ENGLISH);
        assertTrue(emptyPointer.isLeaf());

        Element parentElem = new Element("parent");
        parentElem.addContent(new Element("child"));
        JDOMNodePointer parentPointer = new JDOMNodePointer(parentElem, Locale.ENGLISH);
        assertFalse(parentPointer.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer docPointer = new JDOMNodePointer(emptyDoc, Locale.ENGLISH);
        assertTrue(docPointer.isLeaf());

        Text textNode = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(textNode, Locale.ENGLISH);
        assertTrue(textPointer.isLeaf());
    }

    // Tests getValue and setValue on Element and Text nodes
    @Test
    public void testGetAndSetValue_elementAndText_updatesContent() {
        Element element = new Element("elem");
        element.setText("initial");
        JDOMNodePointer elemPointer = new JDOMNodePointer(element, Locale.ENGLISH);
        assertEquals("initial", elemPointer.getValue());

        elemPointer.setValue("updated");
        assertEquals("updated", elemPointer.getValue());

        Text textNode = new Text("initial text");
        element.addContent(textNode);
        JDOMNodePointer textPointer = new JDOMNodePointer(elemPointer, textNode);
        assertEquals("initial text", textPointer.getValue());

        textPointer.setValue("updated text");
        assertEquals("updated text", textPointer.getValue());
    }

    // Tests getValue on Comment, CDATA, and ProcessingInstruction
    @Test
    public void testGetValue_commentCdataAndPI_returnsTrimmedValue() {
        Comment comment = new Comment("  a comment  ");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("a comment", commentPointer.getValue());

        CDATA cdata = new CDATA("  cdata content  ");
        JDOMNodePointer cdataPointer = new JDOMNodePointer(cdata, Locale.ENGLISH);
        assertEquals("cdata content", cdataPointer.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi data", piPointer.getValue());
    }

    // Tests compareChildNodePointers between Attributes and Elements
    @Test
    public void testCompareChildNodePointers_mixedChildren_ordersAttributesFirstThenElements() {
        Element root = new Element("root");
        Attribute attr1 = new Attribute("attr1", "val1");
        Attribute attr2 = new Attribute("attr2", "val2");
        root.setAttribute(attr1);
        root.setAttribute(attr2);

        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        NodePointer ptrAttr1 = new JDOMNodePointer(rootPointer, attr1);
        NodePointer ptrAttr2 = new JDOMNodePointer(rootPointer, attr2);
        NodePointer ptrChild1 = new JDOMNodePointer(rootPointer, child1);
        NodePointer ptrChild2 = new JDOMNodePointer(rootPointer, child2);

        assertEquals(0, rootPointer.compareChildNodePointers(ptrAttr1, ptrAttr1));
        assertEquals(-1, rootPointer.compareChildNodePointers(ptrAttr1, ptrChild1));
        assertEquals(1, rootPointer.compareChildNodePointers(ptrChild1, ptrAttr1));
        assertEquals(-1, rootPointer.compareChildNodePointers(ptrAttr1, ptrAttr2));
        assertEquals(1, rootPointer.compareChildNodePointers(ptrAttr2, ptrAttr1));
        assertEquals(-1, rootPointer.compareChildNodePointers(ptrChild1, ptrChild2));
        assertEquals(1, rootPointer.compareChildNodePointers(ptrChild2, ptrChild1));
    }

    // Tests testNode with NodeNameTest wildcards and namespace matching
    @Test
    public void testTestNode_nodeNameTest_matchesCorrectly() {
        Element elem = new Element("child", "ns", "http://example.com");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, Locale.ENGLISH);

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(wildcardTest));

        NodeNameTest exactMatchTest = new NodeNameTest(new QName("ns", "child"), "http://example.com");
        assertTrue(pointer.testNode(exactMatchTest));

        NodeNameTest wrongNameTest = new NodeNameTest(new QName("ns", "other"), "http://example.com");
        assertFalse(pointer.testNode(wrongNameTest));

        NodeNameTest wrongNsTest = new NodeNameTest(new QName("ns", "child"), "http://other.com");
        assertFalse(pointer.testNode(wrongNsTest));

        Text text = new Text("hello");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertFalse(textPointer.testNode(exactMatchTest));
    }

    // Tests testNode with NodeTypeTest
    @Test
    public void testTestNode_nodeTypeTest_matchesTypes() {
        Element elem = new Element("elem");
        Text text = new Text("text");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        assertTrue(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(JDOMNodePointer.testNode(null, comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(JDOMNodePointer.testNode(null, pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("targetA", "data");
        ProcessingInstructionTest testMatch = new ProcessingInstructionTest("targetA");
        ProcessingInstructionTest testMismatch = new ProcessingInstructionTest("targetB");

        assertTrue(JDOMNodePointer.testNode(null, pi, testMatch));
        assertFalse(JDOMNodePointer.testNode(null, pi, testMismatch));
        assertFalse(JDOMNodePointer.testNode(null, new Element("elem"), testMatch));
    }

    // Tests getPrefix and getLocalName helper methods
    @Test
    public void testGetPrefixAndLocalName_elementAndAttribute_returnsCorrectValues() {
        Element elem = new Element("elemName", "pfx", "http://example.com");
        Attribute attr = new Attribute("attrName", "val", Namespace.getNamespace("pfx", "http://example.com"));

        assertEquals("pfx", JDOMNodePointer.getPrefix(elem));
        assertEquals("elemName", JDOMNodePointer.getLocalName(elem));
        assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        assertEquals("attrName", JDOMNodePointer.getLocalName(attr));

        Element noPrefixElem = new Element("elemName");
        assertNull(JDOMNodePointer.getPrefix(noPrefixElem));

        Text text = new Text("text");
        assertNull(JDOMNodePointer.getPrefix(text));
        assertNull(JDOMNodePointer.getLocalName(text));
    }

    // Tests isLanguage when xml:lang is set on element
    @Test
    public void testIsLanguage_withXmlLangAttribute_returnsMatchingResult() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);

        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("EN-US"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests createAttribute for element with and without namespace prefix
    @Test
    public void testCreateAttribute_validElement_createsAttributeSuccessfully() {
        Element root = new Element("root");
        Namespace ns = Namespace.getNamespace("custom", "http://custom.com");
        root.addNamespaceDeclaration(ns);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer attrPointer1 = rootPointer.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPointer1);
        assertEquals("", root.getAttributeValue("simpleAttr"));

        NodePointer attrPointer2 = rootPointer.createAttribute(context, new QName("custom", "nsAttr"));
        assertNotNull(attrPointer2);
        assertEquals("", root.getAttributeValue("nsAttr", ns));
    }

    // Tests createAttribute throwing exception for unknown namespace prefix
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);

        rootPointer.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests remove for child element and exception for root node without parent
    @Test
    public void testRemove_childElement_removesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer childPointer = new JDOMNodePointer(parentPointer, child);

        childPointer.remove();
        assertEquals(0, parent.getContent().size());
    }

    // Tests remove on root node throws exception
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        rootPointer.remove();
    }

    // Tests asPath for root, id, and child elements
    @Test
    public void testAsPath_variousConfigurations_returnsXPathString() {
        Element root = new Element("root");
        JDOMNodePointer idPointer = new JDOMNodePointer(root, Locale.ENGLISH, "item'1");
        assertEquals("id('item&apos;1')", idPointer.asPath());

        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        assertEquals("/child[1]", childPointer.asPath());

        Text text = new Text("content");
        child.addContent(text);
        JDOMNodePointer textPointer = new JDOMNodePointer(childPointer, text);
        assertEquals("/child[1]/text()[1]", textPointer.asPath());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        child.addContent(pi);
        JDOMNodePointer piPointer = new JDOMNodePointer(childPointer, pi);
        assertEquals("/child[1]/processing-instruction('target')[1]", piPointer.asPath());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_behaviorConsistent() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1a = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1b = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("string"));

        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
        assertEquals(1, ptr1a.getLength());
        assertFalse(ptr1a.isCollection());
        assertSame(elem1, ptr1a.getBaseValue());
        assertSame(elem1, ptr1a.getImmediateNode());
    }
}
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;

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

    // Tests getName for Element without namespace and with namespace
    @Test
    public void testGetName_element_returnsCorrectQName() {
        Element elem1 = new Element("test");
        JDOMNodePointer ptr1 = new JDOMNodePointer(elem1, Locale.ENGLISH);
        assertEquals(new QName(null, "test"), ptr1.getName());

        Namespace ns = Namespace.getNamespace("ns", "http://example.com/ns");
        Element elem2 = new Element("item", ns);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);
        assertEquals(new QName("ns", "item"), ptr2.getName());
    }

    // Tests getName for ProcessingInstruction
    @Test
    public void testGetName_processingInstruction_returnsTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals(new QName(null, "target"), ptr.getName());
    }

    // Tests getNamespaceURI for Element and default/custom prefix
    @Test
    public void testGetNamespaceURI_elementWithNamespace_returnsURI() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com/ns");
        Element elem = new Element("root", ns);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertEquals("http://example.com/ns", ptr.getNamespaceURI());
        assertEquals("http://example.com/ns", ptr.getNamespaceURI("pfx"));
        assertEquals(Namespace.XML_NAMESPACE.getURI(), ptr.getNamespaceURI("xml"));
        assertNull(ptr.getNamespaceURI("unknown"));
    }

    // Tests getNamespaceURI when node is a Document
    @Test
    public void testGetNamespaceURI_documentRoot_returnsURI() {
        Namespace ns = Namespace.getNamespace("pfx", "http://example.com/doc");
        Element root = new Element("root", ns);
        Document doc = new Document(root);
        JDOMNodePointer ptr = new JDOMNodePointer(doc, Locale.ENGLISH);

        assertEquals("http://example.com/doc", ptr.getNamespaceURI("pfx"));
        assertNull(ptr.getNamespaceURI("nonexistent"));
    }

    // Tests isLeaf for elements, documents, and other nodes
    @Test
    public void testIsLeaf_differentNodes_returnsExpected() {
        Element emptyElem = new Element("empty");
        JDOMNodePointer emptyPtr = new JDOMNodePointer(emptyElem, Locale.ENGLISH);
        assertTrue(emptyPtr.isLeaf());

        Element parentElem = new Element("parent");
        parentElem.addContent(new Element("child"));
        JDOMNodePointer parentPtr = new JDOMNodePointer(parentElem, Locale.ENGLISH);
        assertFalse(parentPtr.isLeaf());

        Text text = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPtr.isLeaf());
    }

    // Tests getValue for element, comment, text, and PI
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectValues() {
        Element root = new Element("root");
        root.addContent(new Text("Hello "));
        Element child = new Element("child");
        child.addContent(new Text("World"));
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        assertEquals("Hello World", rootPtr.getValue());

        Comment comment = new Comment("  a comment  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("a comment", commentPtr.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi data", piPtr.getValue());
    }

    // Tests setValue for element replacing content with string
    @Test
    public void testSetValue_element_updatesContent() {
        Element elem = new Element("elem");
        elem.addContent(new Text("old"));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        ptr.setValue("new value");
        assertEquals("new value", ptr.getValue());

        Element newChild = new Element("sub");
        newChild.addContent(new Text("content"));
        ptr.setValue(newChild);
        assertEquals("content", ptr.getValue());
    }

    // Tests setValue for Text node
    @Test
    public void testSetValue_textNode_updatesTextOrRemovesWhenEmpty() {
        Element parent = new Element("parent");
        Text text = new Text("original");
        parent.addContent(text);

        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.ENGLISH);
        ptr.setValue("modified");
        assertEquals("modified", text.getText());

        ptr.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    // Tests testNode with NodeNameTest (wildcards, prefix matching)
    @Test
    public void testTestNode_nodeNameTest_returnsTrueWhenMatching() {
        Element elem = new Element("target");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "target"))));
        assertFalse(ptr.testNode(new NodeNameTest(new QName(null, "other"))));
        assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "*"))));
        assertNull(JDOMNodePointer.getPrefix(new Object()));
    }

    // Tests testNode with NodeTypeTest for node, text, comment, pi
    @Test
    public void testTestNode_nodeTypeTest_returnsCorrectEvaluation() {
        Element elem = new Element("elem");
        Text text = new Text("text");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);

        assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertTrue(piPtr.testNode(new ProcessingInstructionTest("target")));
        assertFalse(piPtr.testNode(new ProcessingInstructionTest("wrong")));
    }

    // Tests isLanguage with xml:lang attribute on element and parent
    @Test
    public void testIsLanguage_withXmlLangAttribute_returnsTrue() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPtr = new JDOMNodePointer(child, Locale.ENGLISH);
        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("en-US"));
        assertFalse(childPtr.isLanguage("fr"));
    }

    // Tests compareChildNodePointers between attributes and content nodes
    @Test
    public void testCompareChildNodePointers_differentTypes_returnsOrdering() {
        Element root = new Element("root");
        Attribute attr1 = new Attribute("a", "1");
        Attribute attr2 = new Attribute("b", "2");
        root.setAttribute(attr1);
        root.setAttribute(attr2);

        Element child1 = new Element("c1");
        Element child2 = new Element("c2");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer pAttr1 = new JDOMNodePointer(rootPtr, attr1);
        JDOMNodePointer pAttr2 = new JDOMNodePointer(rootPtr, attr2);
        JDOMNodePointer pChild1 = new JDOMNodePointer(rootPtr, child1);
        JDOMNodePointer pChild2 = new JDOMNodePointer(rootPtr, child2);

        assertEquals(0, rootPtr.compareChildNodePointers(pAttr1, pAttr1));
        assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pChild1));
        assertEquals(1, rootPtr.compareChildNodePointers(pChild1, pAttr1));
        assertEquals(-1, rootPtr.compareChildNodePointers(pAttr1, pAttr2));
        assertEquals(1, rootPtr.compareChildNodePointers(pAttr2, pAttr1));
        assertEquals(-1, rootPtr.compareChildNodePointers(pChild1, pChild2));
        assertEquals(1, rootPtr.compareChildNodePointers(pChild2, pChild1));
    }

    // Tests asPath with id, element, text, and PI
    @Test
    public void testAsPath_variousNodeTypes_generatesCorrectPath() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH, "root-id");
        assertEquals("id('root-id')", rootPtr.asPath());

        JDOMNodePointer basePtr = new JDOMNodePointer(root, Locale.ENGLISH);
        Element child = new Element("item");
        root.addContent(child);
        JDOMNodePointer childPtr = new JDOMNodePointer(basePtr, child);
        assertEquals("/item[1]", childPtr.asPath());

        Text text = new Text("hello");
        root.addContent(text);
        JDOMNodePointer textPtr = new JDOMNodePointer(basePtr, text);
        assertEquals("/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = new ProcessingInstruction("test-pi", "data");
        root.addContent(pi);
        JDOMNodePointer piPtr = new JDOMNodePointer(basePtr, pi);
        assertEquals("/processing-instruction('test-pi')[1]", piPtr.asPath());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1 = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1Same = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr1Same));
        assertFalse(ptr1.equals(ptr2));
        assertFalse(ptr1.equals("non-pointer"));
        assertEquals(elem1.hashCode(), ptr1.hashCode());
    }

    // Tests remove for child node and exception when removing root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer childPtr = new JDOMNodePointer(child, Locale.ENGLISH);
        childPtr.remove();
        assertEquals(0, parent.getContent().size());
    }

    // Tests remove exception path when node has no parent
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        rootPtr.remove();
    }

    // Tests basic accessors: isCollection, getLength, getBaseValue, getImmediateNode
    @Test
    public void testBasicAccessors() {
        Element elem = new Element("node");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertFalse(ptr.isCollection());
        assertEquals(1, ptr.getLength());
        assertSame(elem, ptr.getBaseValue());
        assertSame(elem, ptr.getImmediateNode());
        assertNotNull(ptr.getNamespaceResolver());
    }
}
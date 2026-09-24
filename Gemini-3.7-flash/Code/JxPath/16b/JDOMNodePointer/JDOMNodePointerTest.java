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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMNodePointerTest {

    private Element rootElement;
    private Document document;
    private JDOMNodePointer rootPointer;

    @Before
    public void setUp() {
        rootElement = new Element("root");
        document = new Document(rootElement);
        rootPointer = new JDOMNodePointer(rootElement, Locale.ENGLISH);
    }

    // Tests getName and getLocalName for Element and ProcessingInstruction
    @Test
    public void testGetName_elementAndPI_returnsCorrectQName() {
        Element child = new Element("child", "ns", "http://test.org");
        JDOMNodePointer ptr = new JDOMNodePointer(child, Locale.ENGLISH);
        QName qName = ptr.getName();
        assertEquals("ns", qName.getPrefix());
        assertEquals("child", qName.getName());

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("target", piPtr.getName().getName());
        assertNull(piPtr.getName().getPrefix());
    }

    // Tests getNamespaceURI with prefix and default namespace
    @Test
    public void testGetNamespaceURI_withAndWithoutPrefix_returnsUri() {
        Element child = new Element("child", "ns", "http://test.org");
        rootElement.addContent(child);
        JDOMNodePointer docPointer = new JDOMNodePointer(document, Locale.ENGLISH);

        assertEquals(Namespace.XML_NAMESPACE.getURI(), rootPointer.getNamespaceURI("xml"));
        assertEquals("http://test.org", new JDOMNodePointer(child, Locale.ENGLISH).getNamespaceURI());
        assertNull(rootPointer.getNamespaceURI());
        assertNull(docPointer.getNamespaceURI("unknown"));
    }

    // Tests compareChildNodePointers between children and attributes (JxPath-16 bug area)
    @Test
    public void testCompareChildNodePointers_elementsAndAttributes_returnsCorrectOrder() {
        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        rootElement.addContent(child1);
        rootElement.addContent(child2);

        Attribute attr1 = new Attribute("attr1", "val1");
        Attribute attr2 = new Attribute("attr2", "val2");
        rootElement.setAttribute(attr1);
        rootElement.setAttribute(attr2);

        JDOMNodePointer p1 = new JDOMNodePointer(rootPointer, child1);
        JDOMNodePointer p2 = new JDOMNodePointer(rootPointer, child2);
        JDOMNodePointer a1 = new JDOMNodePointer(rootPointer, attr1);
        JDOMNodePointer a2 = new JDOMNodePointer(rootPointer, attr2);

        assertEquals(0, rootPointer.compareChildNodePointers(p1, p1));
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointers(p2, p1));
        assertEquals(-1, rootPointer.compareChildNodePointers(a1, p1));
        assertEquals(1, rootPointer.compareChildNodePointers(p1, a1));
        assertEquals(-1, rootPointer.compareChildNodePointers(a1, a2));
        assertEquals(1, rootPointer.compareChildNodePointers(a2, a1));
    }

    // Tests compareChildNodePointers with non-element parent throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testCompareChildNodePointers_nonElementNode_throwsException() {
        Text text = new Text("sample");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        JDOMNodePointer childPointer = new JDOMNodePointer(textPointer, new Text("sub"));
        textPointer.compareChildNodePointers(childPointer, childPointer);
    }

    // Tests isLeaf for Element, Document, and other node types
    @Test
    public void testIsLeaf_differentNodes_returnsCorrectStatus() {
        assertTrue(rootPointer.isLeaf());

        Element child = new Element("child");
        rootElement.addContent(child);
        assertFalse(rootPointer.isLeaf());

        JDOMNodePointer docPointer = new JDOMNodePointer(document, Locale.ENGLISH);
        assertFalse(docPointer.isLeaf());

        JDOMNodePointer textPointer = new JDOMNodePointer(new Text("txt"), Locale.ENGLISH);
        assertTrue(textPointer.isLeaf());
    }

    // Tests isCollection and getLength
    @Test
    public void testIsCollectionAndLength_returnsDefaults() {
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertEquals(rootElement, rootPointer.getBaseValue());
        assertEquals(rootElement, rootPointer.getImmediateNode());
    }

    // Tests getValue on Element, Comment, Text, and ProcessingInstruction
    @Test
    public void testGetValue_differentNodeTypes_returnsExpectedValue() {
        Element child = new Element("child");
        child.addContent(new Text("Hello"));
        child.addContent(new Element("sub").addContent(new Text(" World")));
        rootElement.addContent(child);

        JDOMNodePointer childPtr = new JDOMNodePointer(rootPointer, child);
        assertEquals("Hello World", childPtr.getValue());

        Comment comment = new Comment("  a comment  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("a comment", commentPtr.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", " piData ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("piData", piPtr.getValue());

        Text text = new Text("  some text  ");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("some text", textPtr.getValue());
    }

    // Tests setValue for Text and Element with various input types
    @Test
    public void testSetValue_variousTypes_updatesContent() {
        Element child = new Element("child");
        rootElement.addContent(child);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPointer, child);

        childPtr.setValue("Simple text");
        assertEquals("Simple text", childPtr.getValue());

        Element newChild = new Element("newChild").addContent(new Text("fromElem"));
        childPtr.setValue(newChild);
        assertEquals("fromElem", childPtr.getValue());

        Comment comment = new Comment("com");
        childPtr.setValue(comment);
        assertEquals(1, child.getContent().size());
        assertTrue(child.getContent().get(0) instanceof Comment);

        ProcessingInstruction pi = new ProcessingInstruction("pi", "data");
        childPtr.setValue(pi);
        assertEquals(1, child.getContent().size());
        assertTrue(child.getContent().get(0) instanceof ProcessingInstruction);

        Text textNode = new Text("initial");
        rootElement.addContent(textNode);
        JDOMNodePointer textPtr = new JDOMNodePointer(rootPointer, textNode);
        textPtr.setValue("updated");
        assertEquals("updated", textNode.getText());

        textPtr.setValue("");
        assertFalse(rootElement.getContent().contains(textNode));
    }

    // Tests testNode with NodeNameTest, NodeTypeTest, and ProcessingInstructionTest
    @Test
    public void testTestNode_variousNodeTests_evaluatesCorrectly() {
        assertTrue(rootPointer.testNode(null));

        NodeNameTest nameTestMatch = new NodeNameTest(new QName("root"));
        NodeNameTest nameTestMismatch = new NodeNameTest(new QName("other"));
        NodeNameTest wildcardTest = new NodeNameTest(new QName("*"));

        assertTrue(rootPointer.testNode(nameTestMatch));
        assertFalse(rootPointer.testNode(nameTestMismatch));
        assertTrue(rootPointer.testNode(wildcardTest));

        Text text = new Text("sample");
        assertFalse(JDOMNodePointer.testNode(null, text, nameTestMatch));

        assertTrue(JDOMNodePointer.testNode(null, rootElement, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(JDOMNodePointer.testNode(null, new CDATA("cdata"), new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(JDOMNodePointer.testNode(null, new Comment("c"), new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(JDOMNodePointer.testNode(null, new ProcessingInstruction("p", "d"), new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        assertTrue(JDOMNodePointer.testNode(null, pi, new ProcessingInstructionTest("target")));
        assertFalse(JDOMNodePointer.testNode(null, pi, new ProcessingInstructionTest("other")));
    }

    // Tests isLanguage and findEnclosingAttribute
    @Test
    public void testIsLanguage_xmlLangAttribute_returnsCorrectBoolean() {
        rootElement.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        rootElement.addContent(child);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPointer, child);

        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("EN-US"));
        assertFalse(childPtr.isLanguage("fr"));
    }

    // Tests createAttribute with and without namespace prefix
    @Test
    public void testCreateAttribute_withAndWithoutPrefix_createsAttributeSuccessfully() {
        JXPathContext context = JXPathContext.newContext(rootElement);

        NodePointer attrPtr = rootPointer.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPtr);
        assertEquals("", rootElement.getAttributeValue("myAttr"));

        rootElement.addNamespaceDeclaration(Namespace.getNamespace("custom", "http://custom.org"));
        NodePointer nsAttrPtr = rootPointer.createAttribute(context, new QName("custom", "nsAttr"));
        assertNotNull(nsAttrPtr);
        assertEquals("http://custom.org", rootElement.getAttribute("nsAttr", Namespace.getNamespace("custom", "http://custom.org")).getNamespaceURI());
    }

    // Tests createAttribute with unknown namespace prefix throws JXPathException
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        JXPathContext context = JXPathContext.newContext(rootElement);
        rootPointer.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests createChild throws exception when factory is not set
    @Test(expected = JXPathException.class)
    public void testCreateChild_noFactory_throwsException() {
        JXPathContext context = JXPathContext.newContext(rootElement);
        rootPointer.createChild(context, new QName("child"), 0);
    }

    // Tests remove for child node and root node
    @Test
    public void testRemove_childNode_removesSuccessfully() {
        Element child = new Element("child");
        rootElement.addContent(child);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPointer, child);

        childPtr.remove();
        assertEquals(0, rootElement.getContent().size());
    }

    // Tests remove root node throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() {
        rootPointer.remove();
    }

    // Tests asPath for id pointer, element, text, and processing instruction
    @Test
    public void testAsPath_variousNodes_returnsXPathString() {
        JDOMNodePointer idPtr = new JDOMNodePointer(rootElement, Locale.ENGLISH, "elem'id\"");
        assertEquals("id('elem&apos;id&quot;')", idPtr.asPath());

        Element child1 = new Element("child");
        Element child2 = new Element("child");
        rootElement.addContent(child1);
        rootElement.addContent(child2);

        JDOMNodePointer c1Ptr = new JDOMNodePointer(rootPointer, child1);
        JDOMNodePointer c2Ptr = new JDOMNodePointer(rootPointer, child2);

        assertEquals("/child[1]", c1Ptr.asPath());
        assertEquals("/child[2]", c2Ptr.asPath());

        Text text = new Text("txt");
        child1.addContent(text);
        JDOMNodePointer textPtr = new JDOMNodePointer(c1Ptr, text);
        assertEquals("/child[1]/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = new ProcessingInstruction("piTarget", "data");
        child1.addContent(pi);
        JDOMNodePointer piPtr = new JDOMNodePointer(c1Ptr, pi);
        assertEquals("/child[1]/processing-instruction('piTarget')[1]", piPtr.asPath());
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects() {
        JDOMNodePointer ptr1 = new JDOMNodePointer(rootElement, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(rootElement, Locale.ENGLISH);
        Element otherElem = new Element("other");
        JDOMNodePointer otherPtr = new JDOMNodePointer(otherElem, Locale.ENGLISH);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr2));
        assertFalse(ptr1.equals(otherPtr));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("aString"));

        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    // Tests childIterator, attributeIterator, namespaceIterator, and namespacePointer
    @Test
    public void testIteratorsAndNamespacePointer_returnsNonNullIterators() {
        NodeIterator childIt = rootPointer.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = rootPointer.attributeIterator(new QName("test"));
        assertNotNull(attrIt);

        NodeIterator nsIt = rootPointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPointer.namespacePointer("xml");
        assertNotNull(nsPtr);

        assertNotNull(rootPointer.getNamespaceResolver());
    }
}
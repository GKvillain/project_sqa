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

    // Tests getName and getLocalName on Element node
    @Test
    public void testGetName_element_returnsCorrectQName() {
        Element element = new Element("testElem", "prefix", "http://example.com");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);

        QName name = pointer.getName();
        assertEquals("prefix", name.getPrefix());
        assertEquals("testElem", name.getName());
        assertEquals("testElem", JDOMNodePointer.getLocalName(element));
        assertEquals("prefix", JDOMNodePointer.getPrefix(element));
    }

    // Tests getName on ProcessingInstruction node
    @Test
    public void testGetName_processingInstruction_returnsTargetAsName() {
        ProcessingInstruction pi = new ProcessingInstruction("targetPI", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.ENGLISH);

        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("targetPI", name.getName());
    }

    // Tests getBaseValue, getImmediateNode, isCollection, getLength
    @Test
    public void testBasicProperties_elementNode_returnsExpectedValues() {
        Element element = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);

        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
    }

    // Tests isLeaf for empty vs non-empty Element and Document
    @Test
    public void testIsLeaf_emptyAndNonEmpty_returnsExpectedBoolean() {
        Element emptyElement = new Element("empty");
        JDOMNodePointer emptyElemPointer = new JDOMNodePointer(emptyElement, Locale.ENGLISH);
        assertTrue(emptyElemPointer.isLeaf());

        Element parentElement = new Element("parent");
        parentElement.addContent(new Element("child"));
        JDOMNodePointer parentElemPointer = new JDOMNodePointer(parentElement, Locale.ENGLISH);
        assertFalse(parentElemPointer.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer emptyDocPointer = new JDOMNodePointer(emptyDoc, Locale.ENGLISH);
        assertTrue(emptyDocPointer.isLeaf());

        Document nonEmpyDoc = new Document(new Element("root"));
        JDOMNodePointer docPointer = new JDOMNodePointer(nonEmpyDoc, Locale.ENGLISH);
        assertFalse(docPointer.isLeaf());

        Text text = new Text("sample");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPointer.isLeaf());
    }

    // Tests getValue on Element, Comment, Text, CDATA, and ProcessingInstruction
    @Test
    public void testGetValue_differentNodeTypes_returnsTrimmedValues() {
        Element element = new Element("elem");
        element.setText("  elem text  ");
        JDOMNodePointer elemPointer = new JDOMNodePointer(element, Locale.ENGLISH);
        assertEquals("elem text", elemPointer.getValue());

        Comment comment = new Comment("  comment data  ");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment data", commentPointer.getValue());

        Text text = new Text("  text content  ");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("text content", textPointer.getValue());

        CDATA cdata = new CDATA("  cdata content  ");
        JDOMNodePointer cdataPointer = new JDOMNodePointer(cdata, Locale.ENGLISH);
        assertEquals("cdata content", cdataPointer.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi data", piPointer.getValue());
    }

    // Tests setValue with String and another Element on Element node
    @Test
    public void testSetValue_elementValue_updatesContent() {
        Element element = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);

        pointer.setValue("new value");
        assertEquals("new value", pointer.getValue());

        Element source = new Element("source");
        source.addContent(new Element("child1"));
        source.addContent(new Text("text1"));
        source.addContent(new Comment("comment1"));
        source.addContent(new ProcessingInstruction("pi", "data"));
        pointer.setValue(source);
        assertEquals(4, element.getContent().size());
    }

    // Tests setValue with Text node removal when value is empty
    @Test
    public void testSetValue_textNodeEmpty_removesTextContent() {
        Element parent = new Element("parent");
        Text textNode = new Text("initial");
        parent.addContent(textNode);

        JDOMNodePointer pointer = new JDOMNodePointer(textNode, Locale.ENGLISH);
        pointer.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    // Tests getNamespaceURI with element, prefix, and document
    @Test
    public void testGetNamespaceURI_withAndWithoutPrefix_returnsCorrectURI() {
        Namespace ns = Namespace.getNamespace("ex", "http://example.com/schema");
        Element root = new Element("root", ns);
        Document doc = new Document(root);

        JDOMNodePointer docPointer = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://example.com/schema", docPointer.getNamespaceURI("ex"));
        assertNull(docPointer.getNamespaceURI("unknown"));

        JDOMNodePointer elemPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        assertEquals("http://example.com/schema", elemPointer.getNamespaceURI());
        assertEquals("http://example.com/schema", elemPointer.getNamespaceURI("ex"));
        assertNull(elemPointer.getNamespaceURI("unknown"));

        Element noNsElem = new Element("plain");
        JDOMNodePointer plainPointer = new JDOMNodePointer(noNsElem, Locale.ENGLISH);
        assertNull(plainPointer.getNamespaceURI());
    }

    // Tests testNode with NodeNameTest, wildcard, and NodeTypeTest
    @Test
    public void testTestNode_variousNodeTests_evaluatesCorrectly() {
        Element element = new Element("child", "ns", "http://example.com");

        assertTrue(JDOMNodePointer.testNode(null, element, null));

        NodeNameTest matchTest = new NodeNameTest(new QName("child"), "http://example.com");
        assertTrue(JDOMNodePointer.testNode(null, element, matchTest));

        NodeNameTest mismatchNameTest = new NodeNameTest(new QName("other"), "http://example.com");
        assertFalse(JDOMNodePointer.testNode(null, element, mismatchNameTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(JDOMNodePointer.testNode(null, element, wildcardTest));

        NodeTypeTest nodeTypeNode = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(JDOMNodePointer.testNode(null, element, nodeTypeNode));

        Text text = new Text("content");
        NodeTypeTest nodeTypeText = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(JDOMNodePointer.testNode(null, text, nodeTypeText));
        assertFalse(JDOMNodePointer.testNode(null, element, nodeTypeText));

        Comment comment = new Comment("comm");
        NodeTypeTest nodeTypeComment = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(JDOMNodePointer.testNode(null, comment, nodeTypeComment));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        NodeTypeTest nodeTypePI = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(JDOMNodePointer.testNode(null, pi, nodeTypePI));

        ProcessingInstructionTest piTestMatch = new ProcessingInstructionTest("target");
        assertTrue(JDOMNodePointer.testNode(null, pi, piTestMatch));

        ProcessingInstructionTest piTestMismatch = new ProcessingInstructionTest("other");
        assertFalse(JDOMNodePointer.testNode(null, pi, piTestMismatch));
    }

    // Tests isLanguage with xml:lang attribute
    @Test
    public void testIsLanguage_xmlLangAttributePresent_evaluatesLanguageCorrectly() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPointer = new JDOMNodePointer(child, Locale.ENGLISH);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));
    }

    // Tests compareChildNodePointers between attributes and elements
    @Test
    public void testCompareChildNodePointers_differentChildren_ordersProperly() {
        Element parent = new Element("parent");
        Attribute attr1 = new Attribute("id", "1");
        Attribute attr2 = new Attribute("name", "test");
        parent.setAttribute(attr1);
        parent.setAttribute(attr2);

        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        parent.addContent(child1);
        parent.addContent(child2);

        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer ptrAttr1 = new JDOMNodePointer(parentPointer, attr1);
        JDOMNodePointer ptrAttr2 = new JDOMNodePointer(parentPointer, attr2);
        JDOMNodePointer ptrChild1 = new JDOMNodePointer(parentPointer, child1);
        JDOMNodePointer ptrChild2 = new JDOMNodePointer(parentPointer, child2);

        assertEquals(0, parentPointer.compareChildNodePointers(ptrAttr1, ptrAttr1));
        assertEquals(-1, parentPointer.compareChildNodePointers(ptrAttr1, ptrAttr2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptrAttr2, ptrAttr1));

        assertEquals(-1, parentPointer.compareChildNodePointers(ptrAttr1, ptrChild1));
        assertEquals(1, parentPointer.compareChildNodePointers(ptrChild1, ptrAttr1));

        assertEquals(-1, parentPointer.compareChildNodePointers(ptrChild1, ptrChild2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptrChild2, ptrChild1));
    }

    // Tests compareChildNodePointers exception when parent is not Element
    @Test(expected = RuntimeException.class)
    public void testCompareChildNodePointers_nonElementNode_throwsException() {
        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        JDOMNodePointer ptr1 = new JDOMNodePointer(textPointer, new Text("c1"));
        JDOMNodePointer ptr2 = new JDOMNodePointer(textPointer, new Text("c2"));

        textPointer.compareChildNodePointers(ptr1, ptr2);
    }

    // Tests asPath with id, element, text, and PI
    @Test
    public void testAsPath_variousNodeTypes_producesCorrectPath() {
        Element root = new Element("root");
        JDOMNodePointer idPointer = new JDOMNodePointer(root, Locale.ENGLISH, "id'123\"test");
        assertEquals("id('id&apos;123&quot;test')", idPointer.asPath());

        Element parent = new Element("root");
        Element child1 = new Element("item");
        Element child2 = new Element("item");
        Text text = new Text("sample");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        parent.addContent(child1);
        parent.addContent(child2);
        parent.addContent(text);
        parent.addContent(pi);

        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer child2Pointer = new JDOMNodePointer(parentPointer, child2);
        assertEquals("/item[2]", child2Pointer.asPath());

        JDOMNodePointer textPointer = new JDOMNodePointer(parentPointer, text);
        assertEquals("/text()[1]", textPointer.asPath());

        JDOMNodePointer piPointer = new JDOMNodePointer(parentPointer, pi);
        assertEquals("/processing-instruction('target')[1]", piPointer.asPath());
    }

    // Tests createAttribute for Element
    @Test
    public void testCreateAttribute_validName_createsAndReturnsPointer() {
        Element element = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);

        NodePointer attrPointer = pointer.createAttribute(context, new QName("attr"));
        assertNotNull(attrPointer);
        assertEquals("", attrPointer.getValue());
        assertEquals("attr", ((Attribute) attrPointer.getBaseValue()).getName());
    }

    // Tests createAttribute with unknown prefix throws exception
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element element = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);

        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests remove method on child node and exception on root
    @Test
    public void testRemove_childElement_removesFromParent() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPointer = new JDOMNodePointer(child, Locale.ENGLISH);
        childPointer.remove();
        assertEquals(0, root.getContent().size());
    }

    // Tests remove method throwing exception when root node has no parent
    @Test(expected = JXPathException.class)
    public void testRemove_rootElementWithoutParent_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.ENGLISH);
        pointer.remove();
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1a = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1b = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(new Object()));
        assertFalse(ptr1a.equals(null));

        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests iterators creation
    @Test
    public void testIterators_createNotNullIterators() {
        Element elem = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, Locale.ENGLISH);

        NodeIterator childIt = pointer.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = pointer.attributeIterator(new QName("test"));
        assertNotNull(attrIt);

        NodeIterator nsIt = pointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = pointer.namespacePointer("xml");
        assertNotNull(nsPtr);
    }
}
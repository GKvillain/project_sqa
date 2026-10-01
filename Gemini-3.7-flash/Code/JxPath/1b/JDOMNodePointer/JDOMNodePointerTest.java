package org.apache.commons.jxpath.ri.model.jdom;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class JDOMNodePointerTest {

    private Element rootElement;
    private Document document;
    private JDOMNodePointer rootPointer;

    @Before
    public void setUp() {
        rootElement = new Element("root", "http://example.com/ns");
        document = new Document(rootElement);
        rootPointer = new JDOMNodePointer(rootElement, Locale.ENGLISH);
    }

    // Tests getName for Element with and without namespace
    @Test
    public void testGetName_elementNode_returnsCorrectQName() {
        Element elem = new Element("child", "ns", "http://example.com/ns");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        QName name = ptr.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("child", name.getName());

        Element simpleElem = new Element("simple");
        JDOMNodePointer simplePtr = new JDOMNodePointer(simpleElem, Locale.ENGLISH);
        QName simpleName = simplePtr.getName();
        assertNull(simpleName.getPrefix());
        assertEquals("simple", simpleName.getName());
    }

    // Tests getName for ProcessingInstruction
    @Test
    public void testGetName_processingInstruction_returnsTargetName() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.ENGLISH);
        QName name = ptr.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    // Tests getValue for Element, Text, CDATA, Comment, and ProcessingInstruction
    @Test
    public void testGetValue_differentNodeTypes_returnsCorrectTrimmedText() {
        Element elem = new Element("test");
        elem.setText("  sample text  ");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("sample text", elemPtr.getValue());

        Text text = new Text("  some text  ");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("some text", textPtr.getValue());

        CDATA cdata = new CDATA("  cdata text  ");
        JDOMNodePointer cdataPtr = new JDOMNodePointer(cdata, Locale.ENGLISH);
        assertEquals("cdata text", cdataPtr.getValue());

        Comment comment = new Comment("  comment content  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment content", commentPtr.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi data", piPtr.getValue());
    }

    // Tests setValue with String on Element and Text
    @Test
    public void testSetValue_stringOnElementAndText_updatesContent() {
        Element elem = new Element("test");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);
        elemPtr.setValue("new value");
        assertEquals("new value", elem.getTextTrim());

        Text textNode = new Text("initial");
        elem.getContent().clear();
        elem.addContent(textNode);
        JDOMNodePointer textPtr = new JDOMNodePointer(elemPtr, textNode);
        textPtr.setValue("updated");
        assertEquals("updated", textNode.getText());
    }

    // Tests isLeaf for Document and Element nodes
    @Test
    public void testIsLeaf_emptyAndNonEmptyNodes_returnsExpectedBoolean() {
        Element emptyElem = new Element("empty");
        JDOMNodePointer emptyPtr = new JDOMNodePointer(emptyElem, Locale.ENGLISH);
        assertTrue(emptyPtr.isLeaf());

        Element parentElem = new Element("parent");
        parentElem.addContent(new Element("child"));
        JDOMNodePointer parentPtr = new JDOMNodePointer(parentElem, Locale.ENGLISH);
        assertFalse(parentPtr.isLeaf());

        Text text = new Text("text");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPtr.isLeaf());
    }

    // Tests getNamespaceURI with prefix and default
    @Test
    public void testGetNamespaceURI_withAndWithoutPrefix_returnsCorrectURI() {
        Element elem = new Element("child", "pfx", "http://example.com/pfx");
        elem.addNamespaceDeclaration(Namespace.getNamespace("other", "http://example.com/other"));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertEquals("http://example.com/pfx", ptr.getNamespaceURI());
        assertEquals("http://example.com/other", ptr.getNamespaceURI("other"));
        assertNull(ptr.getNamespaceURI("unknown"));

        Document doc = new Document(elem);
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://example.com/pfx", docPtr.getNamespaceURI("pfx"));
    }

    // Tests testNode with NodeNameTest (matching, wildcard, and mismatch)
    @Test
    public void testTestNode_nodeNameTest_returnsCorrectMatchResult() {
        Element elem = new Element("item", "http://example.com/ns");

        NodeNameTest exactTest = new NodeNameTest(new QName("item"), "http://example.com/ns");
        assertTrue(JDOMNodePointer.testNode(null, elem, exactTest));

        NodeNameTest wrongNameTest = new NodeNameTest(new QName("other"), "http://example.com/ns");
        assertFalse(JDOMNodePointer.testNode(null, elem, wrongNameTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(JDOMNodePointer.testNode(null, elem, wildcardTest));

        Text text = new Text("content");
        assertFalse(JDOMNodePointer.testNode(null, text, exactTest));
    }

    // Tests testNode with NodeTypeTest
    @Test
    public void testTestNode_nodeTypeTest_matchesExpectedTypes() {
        Element elem = new Element("elem");
        Text text = new Text("text");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        assertTrue(JDOMNodePointer.testNode(null, elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        assertTrue(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(JDOMNodePointer.testNode(null, comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(JDOMNodePointer.testNode(null, pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        ProcessingInstructionTest matchTest = new ProcessingInstructionTest("target");
        ProcessingInstructionTest diffTest = new ProcessingInstructionTest("other");

        assertTrue(JDOMNodePointer.testNode(null, pi, matchTest));
        assertFalse(JDOMNodePointer.testNode(null, pi, diffTest));
        assertFalse(JDOMNodePointer.testNode(null, new Element("elem"), matchTest));
    }

    // Tests compareChildNodePointers for attributes vs elements and sibling order
    @Test
    public void testCompareChildNodePointers_orderAndAttributes_returnsCorrectComparison() {
        Element parent = new Element("parent");
        Attribute attr1 = new Attribute("a1", "v1");
        Attribute attr2 = new Attribute("a2", "v2");
        Element child1 = new Element("c1");
        Element child2 = new Element("c2");

        parent.setAttribute(attr1);
        parent.setAttribute(attr2);
        parent.addContent(child1);
        parent.addContent(child2);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer attr1Ptr = new JDOMNodePointer(parentPtr, attr1);
        JDOMNodePointer attr2Ptr = new JDOMNodePointer(parentPtr, attr2);
        JDOMNodePointer child1Ptr = new JDOMNodePointer(parentPtr, child1);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(parentPtr, child2);

        assertEquals(0, parentPtr.compareChildNodePointers(child1Ptr, child1Ptr));
        assertEquals(-1, parentPtr.compareChildNodePointers(attr1Ptr, child1Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child1Ptr, attr1Ptr));
        assertEquals(-1, parentPtr.compareChildNodePointers(attr1Ptr, attr2Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(attr2Ptr, attr1Ptr));
        assertEquals(-1, parentPtr.compareChildNodePointers(child1Ptr, child2Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child2Ptr, child1Ptr));
    }

    // Tests compareChildNodePointers exception when parent is not Element
    @Test(expected = RuntimeException.class)
    public void testCompareChildNodePointers_nonElementParent_throwsRuntimeException() {
        Text text = new Text("text");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        JDOMNodePointer childPtr1 = new JDOMNodePointer(textPtr, new Element("c1"));
        JDOMNodePointer childPtr2 = new JDOMNodePointer(textPtr, new Element("c2"));
        textPtr.compareChildNodePointers(childPtr1, childPtr2);
    }

    // Tests isLanguage with xml:lang attribute inheritance
    @Test
    public void testIsLanguage_xmlLangAttribute_returnsCorrectBoolean() {
        Element parent = new Element("parent");
        parent.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);

        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("en-US"));
        assertFalse(childPtr.isLanguage("fr"));
    }

    // Tests remove method on child element
    @Test
    public void testRemove_childElement_removesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);

        assertEquals(1, parent.getContent().size());
        childPtr.remove();
        assertEquals(0, parent.getContent().size());
    }

    // Tests remove method throwing exception on root node without parent
    @Test(expected = JXPathException.class)
    public void testRemove_rootNodeWithoutParent_throwsJXPathException() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        rootPtr.remove();
    }

    // Tests asPath with id, elements, text, and processing instruction
    @Test
    public void testAsPath_variousNodes_returnsFormattedXPath() {
        Element root = new Element("root");
        Element child1 = new Element("item");
        Element child2 = new Element("item");
        Text text = new Text("hello");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        root.addContent(child1);
        root.addContent(child2);
        child2.addContent(text);
        child2.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer child1Ptr = new JDOMNodePointer(rootPtr, child1);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        JDOMNodePointer textPtr = new JDOMNodePointer(child2Ptr, text);
        JDOMNodePointer piPtr = new JDOMNodePointer(child2Ptr, pi);

        assertEquals("/item[1]", child1Ptr.asPath());
        assertEquals("/item[2]", child2Ptr.asPath());
        assertEquals("/item[2]/text()[1]", textPtr.asPath());
        assertEquals("/item[2]/processing-instruction('target')[1]", piPtr.asPath());

        JDOMNodePointer idPtr = new JDOMNodePointer(root, Locale.ENGLISH, "item'1'");
        assertEquals("id('item&apos;1&apos;')", idPtr.asPath());
    }

    // Tests createAttribute with prefix and without prefix
    @Test
    public void testCreateAttribute_withAndWithoutPrefix_createsAttributeSuccessfully() {
        Element elem = new Element("test");
        elem.addNamespaceDeclaration(Namespace.getNamespace("pfx", "http://example.com/pfx"));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attrPtr = ptr.createAttribute(context, new QName("attr"));
        assertNotNull(attrPtr);
        assertEquals("", elem.getAttributeValue("attr"));

        NodePointer nsAttrPtr = ptr.createAttribute(context, new QName("pfx", "nsAttr"));
        assertNotNull(nsAttrPtr);
        assertEquals("", elem.getAttributeValue("nsAttr", Namespace.getNamespace("pfx", "http://example.com/pfx")));
    }

    // Tests createAttribute with unknown prefix throwing exception
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        ptr.createAttribute(context, new QName("unknown", "attr"));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_contractFulfilled() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1a = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1b = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("not-a-pointer"));

        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests getPrefix and getLocalName helper methods
    @Test
    public void testGetPrefixAndGetLocalName_variousNodes_returnsExpectedValues() {
        Element elem = new Element("elem", "ns", "http://example.com");
        Attribute attr = new Attribute("attr", "val", Namespace.getNamespace("pfx", "http://pfx.com"));

        assertEquals("ns", JDOMNodePointer.getPrefix(elem));
        assertEquals("elem", JDOMNodePointer.getLocalName(elem));
        assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));

        assertNull(JDOMNodePointer.getPrefix(new Text("text")));
        assertNull(JDOMNodePointer.getLocalName(new Text("text")));
    }

    // Tests iterator methods (childIterator, attributeIterator, namespaceIterator, namespacePointer)
    @Test
    public void testIterators_basicCreation_notNull() {
        Element elem = new Element("test");
        elem.setAttribute("a", "1");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        NodeIterator childIt = ptr.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = ptr.attributeIterator(new QName("a"));
        assertNotNull(attrIt);

        NodeIterator nsIt = ptr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        assertNotNull(nsPtr);
    }

    @Test
    public void testCreateChild_withAndWithoutValue_appendsChildren() {
        Element elem = new Element("parent");
        elem.addNamespaceDeclaration(Namespace.getNamespace("ns", "http://example.com"));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer child1 = ptr.createChild(context, new QName("child"), 0);
        assertNotNull(child1);
        assertEquals(1, elem.getChildren("child").size());

        NodePointer child2 = ptr.createChild(context, new QName("ns", "childWithNs"), 1, "value");
        assertNotNull(child2);
        assertEquals("value", elem.getChildTextTrim("childWithNs", Namespace.getNamespace("ns", "http://example.com")));
    }

    @Test(expected = JXPathException.class)
    public void testCreateChild_unknownPrefix_throwsException() {
        Element elem = new Element("parent");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        ptr.createChild(context, new QName("unknown", "child"), 0);
    }

    @Test
    public void testSetValue_elementWithVariousTypes_setsProperContent() {
        Element elem = new Element("parent");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        ptr.setValue(new CDATA("cdata-val"));
        assertEquals("cdata-val", elem.getTextTrim());

        Element newChild = new Element("sub");
        ptr.setValue(newChild);
        assertEquals(1, elem.getChildren("sub").size());

        List<Element> list = new ArrayList<Element>();
        list.add(new Element("item1"));
        list.add(new Element("item2"));
        ptr.setValue(list);
        assertEquals(2, elem.getChildren().size());

        ptr.setValue(null);
        assertEquals(0, elem.getContent().size());
    }

    @Test(expected = JXPathException.class)
    public void testSetValue_documentNode_throwsException() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        docPtr.setValue("value");
    }

    @Test
    public void testRemove_attribute_removesAttributeFromParent() {
        Element elem = new Element("parent");
        Attribute attr = new Attribute("name", "value");
        elem.setAttribute(attr);

        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JDOMNodePointer attrPtr = new JDOMNodePointer(elemPtr, attr);

        assertNotNull(elem.getAttribute("name"));
        attrPtr.remove();
        assertNull(elem.getAttribute("name"));
    }

    @Test
    public void testAsPath_documentAndAttribute_returnsCorrectXPath() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());

        Element elem = new Element("item");
        Attribute attr = new Attribute("id", "123");
        elem.setAttribute(attr);
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JDOMNodePointer attrPtr = new JDOMNodePointer(elemPtr, attr);

        assertEquals("/@id", attrPtr.asPath());
    }

    @Test
    public void testGetBaseValueAndImmediateNode_returnsBackingNode() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertSame(elem, ptr.getBaseValue());
        assertSame(elem, ptr.getImmediateNode());
        assertFalse(ptr.isCollection());
        assertEquals(1, ptr.getLength());
    }

    @Test
    public void testGetNamespaceResolver_returnsValidResolver() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        NamespaceResolver resolver = ptr.getNamespaceResolver();
        assertNotNull(resolver);
    }
}
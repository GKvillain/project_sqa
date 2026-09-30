package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
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

    // Tests getName for Element with and without namespace prefix
    @Test
    public void testGetName_element_returnsCorrectQName() {
        Element elementWithoutNs = new Element("item");
        JDOMNodePointer ptr1 = new JDOMNodePointer(elementWithoutNs, Locale.ENGLISH);
        assertEquals(new QName(null, "item"), ptr1.getName());

        Namespace ns = Namespace.getNamespace("ns", "http://example.com");
        Element elementWithNs = new Element("item", ns);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elementWithNs, Locale.ENGLISH);
        assertEquals(new QName("ns", "item"), ptr2.getName());
    }

    // Tests getName for ProcessingInstruction
    @Test
    public void testGetName_processingInstruction_returnsTargetAsName() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals(new QName(null, "target"), ptr.getName());
    }

    // Tests getBaseValue, getImmediateNode, isCollection, and getLength
    @Test
    public void testBasicProperties_element_returnsExpectedValues() {
        Element element = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);
        assertSame(element, ptr.getBaseValue());
        assertSame(element, ptr.getImmediateNode());
        assertFalse(ptr.isCollection());
        assertEquals(1, ptr.getLength());
    }

    // Tests isLeaf for element and document with and without content
    @Test
    public void testIsLeaf_emptyAndNonEmptyNodes_returnsExpectedResults() {
        Element emptyElement = new Element("root");
        JDOMNodePointer emptyElemPtr = new JDOMNodePointer(emptyElement, Locale.ENGLISH);
        assertTrue(emptyElemPtr.isLeaf());

        emptyElement.addContent(new Element("child"));
        assertFalse(emptyElemPtr.isLeaf());

        Document doc = new Document(new Element("root"));
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertFalse(docPtr.isLeaf());

        Text textNode = new Text("sample");
        JDOMNodePointer textPtr = new JDOMNodePointer(textNode, Locale.ENGLISH);
        assertTrue(textPtr.isLeaf());
    }

    // Tests getValue on various node types
    @Test
    public void testGetValue_differentNodeTypes_returnsTrimmedValue() {
        Element element = new Element("elem");
        element.setText("  hello world  ");
        assertEquals("hello world", new JDOMNodePointer(element, Locale.ENGLISH).getValue());

        Comment comment = new Comment("  comment text  ");
        assertEquals("comment text", new JDOMNodePointer(comment, Locale.ENGLISH).getValue());

        Text text = new Text("  text value  ");
        assertEquals("text value", new JDOMNodePointer(text, Locale.ENGLISH).getValue());

        CDATA cdata = new CDATA("  cdata value  ");
        assertEquals("cdata value", new JDOMNodePointer(cdata, Locale.ENGLISH).getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        assertEquals("pi data", new JDOMNodePointer(pi, Locale.ENGLISH).getValue());

        assertEquals(null, new JDOMNodePointer(new Object(), Locale.ENGLISH).getValue());
    }

    // Tests setValue on an Element with String and other types
    @Test
    public void testSetValue_elementNode_updatesContent() {
        Element element = new Element("elem");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);

        ptr.setValue("new text");
        assertEquals("new text", ptr.getValue());

        Element newChild = new Element("child");
        newChild.setText("child content");
        ptr.setValue(newChild);
        assertEquals("child content", ptr.getValue());
    }

    // Tests setValue on a Text node
    @Test
    public void testSetValue_textNode_updatesTextContent() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);

        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.ENGLISH);
        ptr.setValue("updated");
        assertEquals("updated", text.getText());
    }

    // Tests setValue on Text with empty string to trigger removal from parent
    @Test
    public void testSetValue_textNodeEmpty_removesFromParent() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);

        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.ENGLISH);
        ptr.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    // Tests getNamespaceURI with prefix on Document and Element
    @Test
    public void testGetNamespaceURI_withPrefix_resolvesNamespace() {
        Namespace ns = Namespace.getNamespace("ns", "http://example.com/ns");
        Element root = new Element("root");
        root.addNamespaceDeclaration(ns);
        Document doc = new Document(root);

        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://example.com/ns", docPtr.getNamespaceURI("ns"));
        assertNull(docPtr.getNamespaceURI("unknown"));

        JDOMNodePointer elemPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        assertEquals("http://example.com/ns", elemPtr.getNamespaceURI("ns"));
        assertNull(elemPtr.getNamespaceURI("unknown"));
    }

    // Tests getNamespaceURI without prefix
    @Test
    public void testGetNamespaceURI_elementWithAndWithoutNamespace_returnsExpectedUri() {
        Element noNsElem = new Element("root");
        assertNull(new JDOMNodePointer(noNsElem, Locale.ENGLISH).getNamespaceURI());

        Namespace ns = Namespace.getNamespace("http://example.com/default");
        Element nsElem = new Element("root", ns);
        assertEquals("http://example.com/default", new JDOMNodePointer(nsElem, Locale.ENGLISH).getNamespaceURI());
    }

    // Tests compareChildNodePointers with attributes and elements
    @Test
    public void testCompareChildNodePointers_differentChildTypes_comparesCorrectly() {
        Element root = new Element("root");
        Attribute attr1 = new Attribute("a1", "v1");
        Attribute attr2 = new Attribute("a2", "v2");
        root.setAttribute(attr1);
        root.setAttribute(attr2);

        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        NodePointer ptrAttr1 = new JDOMNodePointer(rootPtr, attr1);
        NodePointer ptrAttr2 = new JDOMNodePointer(rootPtr, attr2);
        NodePointer ptrChild1 = new JDOMNodePointer(rootPtr, child1);
        NodePointer ptrChild2 = new JDOMNodePointer(rootPtr, child2);

        assertEquals(0, rootPtr.compareChildNodePointers(ptrAttr1, ptrAttr1));
        assertEquals(-1, rootPtr.compareChildNodePointers(ptrAttr1, ptrAttr2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrAttr2, ptrAttr1));

        assertEquals(-1, rootPtr.compareChildNodePointers(ptrAttr1, ptrChild1));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrChild1, ptrAttr1));

        assertEquals(-1, rootPtr.compareChildNodePointers(ptrChild1, ptrChild2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrChild2, ptrChild1));
    }

    // Tests testNode with NodeNameTest
    @Test
    public void testTestNode_nodeNameTest_matchesCorrectly() {
        Element element = new Element("item");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);

        assertTrue(ptr.testNode(null));
        assertTrue(ptr.testNode(new NodeNameTest(new QName("item"))));
        assertFalse(ptr.testNode(new NodeNameTest(new QName("other"))));
        assertTrue(ptr.testNode(new NodeNameTest(new QName("*"))));

        Text text = new Text("content");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertFalse(textPtr.testNode(new NodeNameTest(new QName("item"))));
    }

    // Tests testNode with NodeTypeTest
    @Test
    public void testTestNode_nodeTypeTest_matchesNodeType() {
        Element element = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(element, Locale.ENGLISH);
        assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Text text = new Text("text");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        Comment comment = new Comment("comm");
        JDOMNodePointer commPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertTrue(commPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_processingInstructionTest_matchesTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.ENGLISH);

        assertTrue(ptr.testNode(new ProcessingInstructionTest("target")));
        assertFalse(ptr.testNode(new ProcessingInstructionTest("other")));
    }

    // Tests static helpers getPrefix and getLocalName
    @Test
    public void testGetPrefixAndGetLocalName_variousObjects_returnsExpected() {
        Namespace ns = Namespace.getNamespace("p", "http://example.com");
        Element element = new Element("elem", ns);
        assertEquals("p", JDOMNodePointer.getPrefix(element));
        assertEquals("elem", JDOMNodePointer.getLocalName(element));

        Attribute attr = new Attribute("attr", "val", ns);
        assertEquals("p", JDOMNodePointer.getPrefix(attr));
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));

        assertNull(JDOMNodePointer.getPrefix(new Object()));
        assertNull(JDOMNodePointer.getLocalName(new Object()));
    }

    // Tests isLanguage when xml:lang attribute is present or absent
    @Test
    public void testIsLanguage_xmlLangAttribute_matchesLanguagePrefix() {
        Element element = new Element("elem");
        element.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);

        assertTrue(ptr.isLanguage("en"));
        assertTrue(ptr.isLanguage("EN"));
        assertFalse(ptr.isLanguage("fr"));
    }

    // Tests asPath with id attribute
    @Test
    public void testAsPath_withId_returnsIdFunction() {
        Element element = new Element("elem");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH, "item'1\"a");
        assertEquals("id('item&apos;1&quot;a')", ptr.asPath());
    }

    // Tests asPath for child elements and text nodes
    @Test
    public void testAsPath_childElementAndText_returnsCorrectXPath() {
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Text text = new Text("hello");
        child2.addContent(text);
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        JDOMNodePointer textPtr = new JDOMNodePointer(child2Ptr, text);

        assertEquals("/child[2]", child2Ptr.asPath());
        assertEquals("/child[2]/text()[1]", textPtr.asPath());
    }

    // Tests createAttribute on Element
    @Test
    public void testCreateAttribute_noNamespace_createsAttributeOnElement() {
        Element element = new Element("elem");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);

        NodePointer attrPtr = ptr.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPtr);
        assertEquals("", element.getAttributeValue("myAttr"));
    }

    // Tests remove on child node and exception on root node
    @Test
    public void testRemove_childNode_removesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer ptr = new JDOMNodePointer(child, Locale.ENGLISH);
        ptr.remove();
        assertEquals(0, parent.getContent().size());
    }

    // Tests remove on root element throwing exception
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);
        ptr.remove();
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode_sameAndDifferentNodes_behavesCorrectly() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1a = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1b = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals("not a pointer"));
        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    // Tests iterator methods return non-null iterators
    @Test
    public void testIterators_element_returnsValidIterators() {
        Element element = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);

        NodeIterator childIt = ptr.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = ptr.attributeIterator(new QName("test"));
        assertNotNull(attrIt);

        NodeIterator nsIt = ptr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        assertNotNull(nsPtr);
    }

    @Test
    public void testSetValue_nullAndVariousNodeTypes() {
        Element element = new Element("elem");
        element.setText("old content");
        JDOMNodePointer elemPtr = new JDOMNodePointer(element, Locale.ENGLISH);
        elemPtr.setValue(null);
        assertEquals(0, element.getContent().size());

        Comment comment = new Comment("initial comment");
        JDOMNodePointer commPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        commPtr.setValue("updated comment");
        assertEquals("updated comment", comment.getText());

        ProcessingInstruction pi = new ProcessingInstruction("target", "initial data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        piPtr.setValue("updated data");
        assertEquals("updated data", pi.getData());

        CDATA cdata = new CDATA("initial cdata");
        JDOMNodePointer cdataPtr = new CDATA("initial cdata") != null ? new JDOMNodePointer(cdata, Locale.ENGLISH) : null;
        cdataPtr.setValue("updated cdata");
        assertEquals("updated cdata", cdata.getText());
    }

    @Test
    public void testCreateAttribute_withNamespacePrefix() {
        Element element = new Element("elem");
        Namespace ns = Namespace.getNamespace("custom", "http://example.com/custom");
        element.addNamespaceDeclaration(ns);

        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);

        NodePointer attrPtr = ptr.createAttribute(context, new QName("custom", "attrName"));
        assertNotNull(attrPtr);
        assertEquals("", element.getAttributeValue("attrName", ns));

        // Creating existing attribute returns pointer to existing one
        NodePointer existingAttrPtr = ptr.createAttribute(context, new QName("custom", "attrName"));
        assertNotNull(existingAttrPtr);
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unknownPrefix_throwsException() {
        Element element = new Element("elem");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);

        ptr.createAttribute(context, new QName("unknownPrefix", "attrName"));
    }

    @Test
    public void testCreateChild_withFactory() {
        Element element = new Element("parent");
        JDOMNodePointer ptr = new JDOMNodePointer(element, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(element);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                if (parent instanceof Element) {
                    Element child = new Element(name);
                    ((Element) parent).addContent(child);
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = ptr.createChild(context, new QName("child"), 0);
        assertNotNull(childPtr);
        assertEquals("child", ((Element) childPtr.getNode()).getName());

        NodePointer childWithValuePtr = ptr.createChild(context, new QName("child2"), 1, "childValue");
        assertNotNull(childWithValuePtr);
        assertEquals("child2", ((Element) childWithValuePtr.getNode()).getName());
        assertEquals("childValue", childWithValuePtr.getValue());
    }

    @Test
    public void testIsLanguage_inheritedFromParent() {
        Element parent = new Element("parent");
        parent.setAttribute("lang", "fr", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);

        assertTrue(childPtr.isLanguage("fr"));
        assertFalse(childPtr.isLanguage("en"));
    }

    @Test
    public void testAsPath_variousChildNodes() {
        Element root = new Element("root");
        Comment comment = new Comment("comm");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        CDATA cdata = new CDATA("cdata_text");
        root.addContent(comment);
        root.addContent(pi);
        root.addContent(cdata);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer commPtr = new JDOMNodePointer(rootPtr, comment);
        JDOMNodePointer piPtr = new JDOMNodePointer(rootPtr, pi);
        JDOMNodePointer cdataPtr = new JDOMNodePointer(rootPtr, cdata);

        assertEquals("", rootPtr.asPath());
        assertEquals("/comment()[1]", commPtr.asPath());
        assertEquals("/processing-instruction('target')[1]", piPtr.asPath());
        assertEquals("/text()[1]", cdataPtr.asPath());

        Document doc = new Document(new Element("docRoot"));
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());
    }

    @Test
    public void testNamespaceResolverAndNamespacePointer() {
        Element root = new Element("root");
        Namespace ns = Namespace.getNamespace("custom", "http://example.com/ns");
        root.addNamespaceDeclaration(ns);
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);

        assertNotNull(rootPtr.getNamespaceResolver());
        NodePointer nsPtr = rootPtr.namespacePointer("custom");
        assertNotNull(nsPtr);
        assertEquals("http://example.com/ns", nsPtr.getNamespaceURI());
    }

    @Test
    public void testTestNode_withNamespacePrefix() {
        Namespace ns = Namespace.getNamespace("p", "http://example.com/p");
        Element elem = new Element("elem", ns);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertTrue(ptr.testNode(new NodeNameTest(new QName("p", "elem"))));
        assertFalse(ptr.testNode(new NodeNameTest(new QName("p", "other"))));
        assertTrue(ptr.testNode(new NodeNameTest(new QName("p", "*"))));
    }
}
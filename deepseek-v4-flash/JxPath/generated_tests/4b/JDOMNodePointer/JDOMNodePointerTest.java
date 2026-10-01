package org.apache.commons.jxpath.ri.model.jdom;

import org.jdom.*;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;

public class JDOMNodePointerTest {

    private JDOMNodePointer createPointer(Object node) {
        return new JDOMNodePointer(node, Locale.US);
    }

    // Tests setValue with CDATA value (defect: addContent uses wrong variable)
    @Test
    public void testSetValue_CDATAValue_addsCDATAContent() {
        Element element = new Element("root");
        JDOMNodePointer ptr = createPointer(element);
        CDATA cdata = new CDATA("test");
        ptr.setValue(cdata);
        assertEquals(1, element.getContent().size());
        assertTrue(element.getContent(0) instanceof CDATA);
        assertEquals("test", ((CDATA)element.getContent(0)).getText());
    }

    // Tests setValue with ProcessingInstruction value (defect: addContent uses wrong variable)
    @Test
    public void testSetValue_ProcessingInstructionValue_addsPIContent() {
        Element element = new Element("root");
        JDOMNodePointer ptr = createPointer(element);
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        ptr.setValue(pi);
        assertEquals(1, element.getContent().size());
        assertTrue(element.getContent(0) instanceof ProcessingInstruction);
        assertEquals("target", ((ProcessingInstruction)element.getContent(0)).getTarget());
    }

    // Tests setValue with Comment value (defect: addContent uses wrong variable)
    @Test
    public void testSetValue_CommentValue_addsCommentContent() {
        Element element = new Element("root");
        JDOMNodePointer ptr = createPointer(element);
        Comment comment = new Comment("comment");
        ptr.setValue(comment);
        assertEquals(1, element.getContent().size());
        assertTrue(element.getContent(0) instanceof Comment);
        assertEquals("comment", ((Comment)element.getContent(0)).getText());
    }

    // Tests setValue with Element value
    @Test
    public void testSetValue_ElementValue_addsClonedElement() {
        Element element = new Element("root");
        JDOMNodePointer ptr = createPointer(element);
        Element child = new Element("child");
        child.setText("text");
        ptr.setValue(child);
        assertEquals(1, element.getContent().size());
        assertTrue(element.getContent(0) instanceof Element);
        Element added = (Element) element.getContent(0);
        assertEquals("child", added.getName());
        assertNotSame(child, added);
    }

    // Tests setValue on Text node with string text
    @Test
    public void testSetValue_TextValueOnTextNode_setsText() {
        Text text = new Text("old");
        JDOMNodePointer ptr = createPointer(text);
        ptr.setValue("new");
        assertEquals("new", text.getText());
    }

    // Tests setValue with empty string on Text node removes text
    @Test
    public void testSetValue_EmptyStringOnTextNode_removesText() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer ptr = createPointer(text);
        ptr.setValue("");
        assertTrue(parent.getContent().isEmpty());
    }

    // Tests getValue for various node types returns trimmed text
    @Test
    public void testGetValue_variousNodeTypes_returnsTrimmedText() {
        Element element = new Element("root");
        element.setText("  hello  ");
        assertEquals("hello", createPointer(element).getValue());

        Comment comment = new Comment("  comment  ");
        assertEquals("comment", createPointer(comment).getValue());

        Text text = new Text("  text  ");
        assertEquals("text", createPointer(text).getValue());

        CDATA cdata = new CDATA("  cdata  ");
        assertEquals("cdata", createPointer(cdata).getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  data  ");
        assertEquals("data", createPointer(pi).getValue());
    }

    // Tests isLeaf for Element with content, without content, and Text
    @Test
    public void testIsLeaf_variousNodes_returnsCorrect() {
        Element elementWithContent = new Element("root");
        elementWithContent.addContent(new Text("text"));
        assertFalse(createPointer(elementWithContent).isLeaf());

        Element elementEmpty = new Element("root");
        assertTrue(createPointer(elementEmpty).isLeaf());

        Text text = new Text("text");
        assertTrue(createPointer(text).isLeaf());
    }

    // Tests remove on root node throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_RootNode_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer ptr = createPointer(root);
        ptr.remove();
    }

    // Tests testNode with NodeTypeTest for all node types
    @Test
    public void testTestNode_NodeTypeTests_allTypes() {
        Element element = new Element("root");
        assertTrue(JDOMNodePointer.testNode(null, element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(JDOMNodePointer.testNode(null, element, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Document doc = new Document(new Element("root"));
        assertTrue(JDOMNodePointer.testNode(null, doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        Text text = new Text("hello");
        assertTrue(JDOMNodePointer.testNode(null, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        CDATA cdata = new CDATA("hello");
        assertTrue(JDOMNodePointer.testNode(null, cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = new Comment("comment");
        assertTrue(JDOMNodePointer.testNode(null, comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        assertTrue(JDOMNodePointer.testNode(null, pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
    }

    // Tests testNode with NodeNameTest matching element
    @Test
    public void testTestNode_NodeNameTestMatching_returnsTrue() {
        Element element = new Element("local", Namespace.getNamespace("pref", "http://example.com"));
        QName qname = new QName("pref", "local");
        NodeNameTest nameTest = new NodeNameTest(qname, "http://example.com");
        assertTrue(JDOMNodePointer.testNode(null, element, nameTest));
    }

    // Tests testNode with ProcessingInstructionTest matching
    @Test
    public void testTestNode_ProcessingInstructionTestMatching_returnsTrue() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        assertTrue(JDOMNodePointer.testNode(null, pi, piTest));
    }

    // Tests compareChildNodePointers ordering between Attribute and Element
    @Test
    public void testCompareChildNodePointers_AttributeBeforeElement_returnsCorrectOrder() {
        Element parent = new Element("parent");
        Attribute attr = new Attribute("attr", "value");
        parent.setAttribute(attr);
        Element child1 = new Element("child1");
        parent.addContent(child1);
        Element child2 = new Element("child2");
        parent.addContent(child2);

        JDOMNodePointer parentPtr = createPointer(parent);
        JDOMNodePointer attrPtr = new JDOMNodePointer(parentPtr, attr);
        JDOMNodePointer child1Ptr = new JDOMNodePointer(parentPtr, child1);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(parentPtr, child2);

        assertEquals(-1, parentPtr.compareChildNodePointers(attrPtr, child1Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child1Ptr, attrPtr));
        assertEquals(0, parentPtr.compareChildNodePointers(attrPtr, attrPtr));
        assertEquals(-1, parentPtr.compareChildNodePointers(child1Ptr, child2Ptr));
        assertEquals(1, parentPtr.compareChildNodePointers(child2Ptr, child1Ptr));
    }

    // Tests static methods: getPrefix, getLocalName, getNamespaceURI
    @Test
    public void testStaticUtils_variousNodes_returnsCorrect() {
        Element elementWithPrefix = new Element("local", "pref", "uri");
        assertEquals("pref", JDOMNodePointer.getPrefix(elementWithPrefix));
        Element elementNoPrefix = new Element("local");
        assertNull(JDOMNodePointer.getPrefix(elementNoPrefix));

        assertEquals("local", JDOMNodePointer.getLocalName(elementWithPrefix));
        Element elementName = new Element("local");
        assertEquals("local", JDOMNodePointer.getLocalName(elementName));

        // fix: use instance method to get namespace URI for an Element
        assertEquals("uri", new JDOMNodePointer(elementWithPrefix, Locale.US).getNamespaceURI());
        assertNull(new JDOMNodePointer(elementNoPrefix, Locale.US).getNamespaceURI());

        Attribute attr = new Attribute("aname", "avalue", Namespace.getNamespace("ans", "auri"));
        assertEquals("ans", JDOMNodePointer.getPrefix(attr));
        assertEquals("aname", JDOMNodePointer.getLocalName(attr));
    }

    // Tests instance method getNamespaceURI for Element with and without namespace
    @Test
    public void testGetNamespaceURI_instance() {
        Element elementWithNS = new Element("local", "pref", "http://example.com");
        assertEquals("http://example.com", createPointer(elementWithNS).getNamespaceURI());
        Element elementWithoutNS = new Element("local");
        assertNull(createPointer(elementWithoutNS).getNamespaceURI());
    }

    // Tests equals and hashCode for same and different nodes
    @Test
    public void testEqualsAndHashCode() {
        Element element = new Element("root");
        JDOMNodePointer p1 = createPointer(element);
        JDOMNodePointer p2 = createPointer(element);
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());

        Element differentElement = new Element("root");
        JDOMNodePointer p3 = createPointer(differentElement);
        assertFalse(p1.equals(p3));
        assertTrue(p1.hashCode() != p3.hashCode());
    }
}
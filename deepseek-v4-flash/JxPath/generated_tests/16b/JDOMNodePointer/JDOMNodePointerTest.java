package org.apache.commons.jxpath.ri.model.jdom;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jdom.*;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.*;

public class JDOMNodePointerTest {

    // Test constructor with node and locale
    @Test
    public void testConstructor_NodeLocale_returnsNode() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());
        assertNull(pointer.getParent());
    }

    // Test constructor with id – asPath should return id expression
    @Test
    public void testConstructor_NodeLocaleId_asPathReturnsId() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH, "myid");
        assertEquals("id('myid')", pointer.asPath());
    }

    // Test getName for element without namespace
    @Test
    public void testGetName_ElementNoNamespace_returnsQName() {
        Element element = new Element("foo");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("foo", name.getName());
    }

    // Test getName for element with namespace
    @Test
    public void testGetName_ElementWithNamespace_returnsQNameWithPrefix() {
        Element element = new Element("bar", "ns", "http://ns");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("bar", name.getName());
    }

    // Test getName for ProcessingInstruction
    @Test
    public void testGetName_ProcessingInstruction_returnsTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet", "href=\"style.css\"");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("xml-stylesheet", name.getName());
    }

    // Test getValue for Text node (default trim)
    @Test
    public void testGetValue_TextNode_returnsTrimmedText() {
        Text text = new Text("  hello  ");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("hello", pointer.getValue());
    }

    // Test getValue for Comment (trimmed)
    @Test
    public void testGetValue_Comment_returnsTrimmedText() {
        Comment comment = new Comment("  comment  ");
        JDOMNodePointer pointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment", pointer.getValue());
    }

    // Test getValue for ProcessingInstruction
    @Test
    public void testGetValue_ProcessingInstruction_returnsData() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("data", pointer.getValue());
    }

    // Test getValue for Element with text children
    @Test
    public void testGetValue_ElementWithTextChildren_returnsConcatenatedText() {
        Element parent = new Element("parent");
        parent.addContent(new Text("Hello"));
        parent.addContent(new Text("World"));
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        assertEquals("HelloWorld", parentPointer.getValue());
    }

    // testNode with null test
    @Test
    public void testTestNode_NullTest_returnsTrue() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
    }

    // testNode with wildcard NodeNameTest and no prefix
    @Test
    public void testTestNode_NodeNameTestWildcardNoPrefix_returnsTrue() {
        Element element = new Element("any");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName("*"), true);
        assertTrue(pointer.testNode(test));
    }

    // testNode with matching name and namespace
    @Test
    public void testTestNode_NodeNameTestMatchNameAndNamespace_returnsTrue() {
        Element element = new Element("foo", "ns", "http://ns");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName("ns", "foo"), "http://ns");
        assertTrue(pointer.testNode(test));
    }

    // testNode with matching name but mismatched namespace
    @Test
    public void testTestNode_NodeNameTestMismatchNamespace_returnsFalse() {
        Element element = new Element("foo", "ns", "http://ns");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName("foo"), "http://other");
        assertFalse(pointer.testNode(test));
    }

    // testNode with matching name and no namespace on node
    @Test
    public void testTestNode_NodeNameTestMatchNameNoNamespace_returnsTrue() {
        Element element = new Element("foo");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName("foo"), null);
        assertTrue(pointer.testNode(test));
    }

    // testNode with NodeTypeTest NODE on Element
    @Test
    public void testTestNode_NodeTypeTestNode_Element_returnsTrue() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(pointer.testNode(test));
    }

    // testNode with NodeTypeTest TEXT on Text
    @Test
    public void testTestNode_NodeTypeTestText_Text_returnsTrue() {
        Text text = new Text("test");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pointer.testNode(test));
    }

    // testNode with NodeTypeTest TEXT on Element (should be false)
    @Test
    public void testTestNode_NodeTypeTestText_Element_returnsFalse() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertFalse(pointer.testNode(test));
    }

    // testNode with ProcessingInstructionTest matching target
    @Test
    public void testTestNode_ProcessingInstructionTestMatch_returnsTrue() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(pointer.testNode(test));
    }

    // testNode with ProcessingInstructionTest mismatching target
    @Test
    public void testTestNode_ProcessingInstructionTestMismatch_returnsFalse() {
        ProcessingInstruction pi = new ProcessingInstruction("target1", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target2");
        assertFalse(pointer.testNode(test));
    }

    // isLanguage returns true when xml:lang attribute is set on ancestor
    @Test
    public void testIsLanguage_EnclosingLangAttribute_returnsTrue() {
        Element parent = new Element("parent");
        parent.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer childPointer = new JDOMNodePointer(child, Locale.ENGLISH);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("EN"));
    }

    // isLanguage returns false when no xml:lang attribute exists
    @Test
    public void testIsLanguage_NoLangAttribute_returnsFalse() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        assertFalse(pointer.isLanguage("en"));
    }

    // asPath for child element without namespace
    @Test
    public void testAsPath_ElementNoNamespaceChild_returnsPath() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        assertEquals("/child[1]", childPointer.asPath());
    }

    // asPath for child element with default namespace (no prefix mapping)
    @Test
    public void testAsPath_ElementWithDefaultNamespaceChild_returnsNodePath() {
        Element root = new Element("root");
        Element child = new Element("child", "", "http://default");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        assertEquals("/node()[1]", childPointer.asPath());
    }

    // asPath for text node
    @Test
    public void testAsPath_TextNode_returnsTextPath() {
        Element root = new Element("root");
        Text text = new Text("hello");
        root.addContent(text);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text);
        assertEquals("/text()[1]", textPointer.asPath());
    }

    // asPath for processing instruction
    @Test
    public void testAsPath_ProcessingInstruction_returnsPIPath() {
        Element root = new Element("root");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        root.addContent(pi);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer piPointer = new JDOMNodePointer(rootPointer, pi);
        assertEquals("/processing-instruction('target')[1]", piPointer.asPath());
    }

    // compareChildNodePointers: attribute before element
    @Test
    public void testCompareChildNodePointers_AttributeBeforeElement_returnsNegative() {
        Element parent = new Element("parent");
        Attribute attr = new Attribute("id", "1");
        parent.setAttribute(attr);
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        JDOMNodePointer attrPointer = new JDOMNodePointer(parentPointer, attr);
        JDOMNodePointer childPointer = new JDOMNodePointer(parentPointer, child);
        int result = parentPointer.compareChildNodePointers(attrPointer, childPointer);
        assertTrue("Attribute should be ordered before element", result < 0);
    }

    // setValue on Text node updates text
    @Test
    public void testSetValue_TextNode_updatesText() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        textPointer.setValue("new");
        assertEquals("new", text.getText());
    }

    // setValue on Element with String clears content and adds Text
    @Test
    public void testSetValue_ElementWithString_clearsContentAndAddsText() {
        Element parent = new Element("parent");
        parent.addContent(new Text("old"));
        JDOMNodePointer pointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        pointer.setValue("new value");
        List content = parent.getContent();
        assertEquals(1, content.size());
        assertTrue(content.get(0) instanceof Text);
        assertEquals("new value", ((Text) content.get(0)).getText());
    }

    // remove child element from parent
    @Test
    public void testRemove_ChildElement_removesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer childPointer = new JDOMNodePointer(child, Locale.ENGLISH);
        childPointer.remove();
        assertFalse(parent.getContent().contains(child));
    }

    // remove root element throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_RootElement_throwsException() {
        Element root = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.ENGLISH);
        pointer.remove();
    }
}
package org.apache.commons.jxpath.ri.model.jdom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jdom.*;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.*;
import java.util.Locale;

public class JDOMNodePointerTest {

    private JDOMNodePointer createPointer(Object node) {
        return new JDOMNodePointer(node, Locale.US);
    }

    // ==================== testNode ====================

    @Test
    public void testTestNode_nullTest_returnsTrue() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertTrue(pointer.testNode(null));
    }

    @Test
    public void testTestNode_ElementNodeNameTestExactMatch_returnsTrue() {
        Element element = new Element("foo", "uri");
        JDOMNodePointer pointer = createPointer(element);
        QName qname = new QName("foo");
        NodeNameTest test = new NodeNameTest(qname, "uri");
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ElementNodeNameTestNamespaceMismatch_returnsFalse() {
        Element element = new Element("foo", "uri1");
        JDOMNodePointer pointer = createPointer(element);
        QName qname = new QName("foo");
        NodeNameTest test = new NodeNameTest(qname, "uri2");
        assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ElementNodeTypeNode_returnsTrue() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_TextNodeTypeText_returnsTrue() {
        Text text = new Text("hello");
        JDOMNodePointer pointer = createPointer(text);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_CDATANodeTypeText_returnsTrue() {
        CDATA cdata = new CDATA("content");
        JDOMNodePointer pointer = createPointer(cdata);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ElementNodeTypeText_returnsFalse() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertFalse(pointer.testNode(test));
    }

    @Test
    public void testTestNode_CommentNodeTypeComment_returnsTrue() {
        Comment comment = new Comment("comment");
        JDOMNodePointer pointer = createPointer(comment);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ProcessingInstructionNodeTypePI_returnsTrue() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = createPointer(pi);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ProcessingInstructionProcessingInstructionTestMatch_returnsTrue() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = createPointer(pi);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(pointer.testNode(test));
    }

    @Test
    public void testTestNode_ProcessingInstructionProcessingInstructionTestNoMatch_returnsFalse() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = createPointer(pi);
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(pointer.testNode(test));
    }

    // ==================== getValue ====================

    @Test
    public void testGetValue_Element_returnsTrimmedText() {
        Element element = new Element("root");
        element.addContent(new Text("  hello  "));
        JDOMNodePointer pointer = createPointer(element);
        assertEquals("hello", pointer.getValue());
    }

    @Test
    public void testGetValue_Text_returnsTrimmedText() {
        Text text = new Text("  world  ");
        JDOMNodePointer pointer = createPointer(text);
        assertEquals("world", pointer.getValue());
    }

    @Test
    public void testGetValue_CDATA_returnsTrimmedText() {
        CDATA cdata = new CDATA("  data  ");
        JDOMNodePointer pointer = createPointer(cdata);
        assertEquals("data", pointer.getValue());
    }

    @Test
    public void testGetValue_Comment_returnsTrimmedText() {
        Comment comment = new Comment("  note  ");
        JDOMNodePointer pointer = createPointer(comment);
        assertEquals("note", pointer.getValue());
    }

    @Test
    public void testGetValue_ProcessingInstruction_returnsTrimmedData() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "  info  ");
        JDOMNodePointer pointer = createPointer(pi);
        assertEquals("info", pointer.getValue());
    }

    @Test
    public void testGetValue_Document_returnsNull() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer pointer = createPointer(doc);
        assertNull(pointer.getValue());
    }

    // ==================== isLeaf ====================

    @Test
    public void testIsLeaf_ElementWithNoContent_returnsTrue() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeaf_ElementWithChild_returnsFalse() {
        Element parent = new Element("parent");
        parent.addContent(new Element("child"));
        JDOMNodePointer pointer = createPointer(parent);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testIsLeaf_DocumentWithNoContent_returnsTrue() {
        Document doc = new Document();
        JDOMNodePointer pointer = createPointer(doc);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeaf_Text_returnsTrue() {
        Text text = new Text("text");
        JDOMNodePointer pointer = createPointer(text);
        assertTrue(pointer.isLeaf());
    }

    // ==================== isLanguage ====================

    @Test
    public void testIsLanguage_ElementWithXmlLang_returnsCorrectResult() {
        Element element = new Element("root");
        element.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        JDOMNodePointer pointer = createPointer(element);
        assertTrue(pointer.isLanguage("en"));
        assertTrue(pointer.isLanguage("EN"));
        assertFalse(pointer.isLanguage("fr"));
    }

    // ==================== getName ====================

    @Test
    public void testGetName_ElementWithPrefix_returnsQName() {
        Element element = new Element("ns:local", "ns", "uri");
        JDOMNodePointer pointer = createPointer(element);
        QName name = pointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("local", name.getName());
    }

    @Test
    public void testGetName_ElementNoPrefix_returnsNullPrefix() {
        Element element = new Element("local");
        JDOMNodePointer pointer = createPointer(element);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("local", name.getName());
    }

    @Test
    public void testGetName_ProcessingInstruction_returnsTarget() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = createPointer(pi);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    // ==================== compareChildNodePointers ====================

    @Test
    public void testCompareChildNodePointers_AttributeVsElement_returnsMinusOne() {
        Element parent = new Element("parent");
        Attribute attr = new Attribute("id", "1");
        parent.setAttribute(attr);
        Element childElem = new Element("child");
        parent.addContent(childElem);
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer attrPtr = new JDOMNodePointer(parentPtr, attr);
        JDOMNodePointer elemPtr = new JDOMNodePointer(parentPtr, childElem);
        int result = parentPtr.compareChildNodePointers(attrPtr, elemPtr);
        assertTrue(result < 0);
    }

    @Test
    public void testCompareChildNodePointers_ElementVsAttribute_returnsOne() {
        Element parent = new Element("parent");
        Attribute attr = new Attribute("id", "1");
        parent.setAttribute(attr);
        Element childElem = new Element("child");
        parent.addContent(childElem);
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer attrPtr = new JDOMNodePointer(parentPtr, attr);
        JDOMNodePointer elemPtr = new JDOMNodePointer(parentPtr, childElem);
        int result = parentPtr.compareChildNodePointers(elemPtr, attrPtr);
        assertTrue(result > 0);
    }

    @Test
    public void testCompareChildNodePointers_TwoAttributesFirstOrder() {
        Element parent = new Element("parent");
        Attribute attr1 = new Attribute("a", "1");
        Attribute attr2 = new Attribute("b", "2");
        parent.setAttribute(attr1);
        parent.setAttribute(attr2);
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer attrPtr1 = new JDOMNodePointer(parentPtr, attr1);
        JDOMNodePointer attrPtr2 = new JDOMNodePointer(parentPtr, attr2);
        int result = parentPtr.compareChildNodePointers(attrPtr1, attrPtr2);
        assertTrue(result < 0);
    }

    @Test
    public void testCompareChildNodePointers_TwoChildrenOrder() {
        Element parent = new Element("parent");
        Element child1 = new Element("first");
        Element child2 = new Element("second");
        parent.addContent(child1);
        parent.addContent(child2);
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer childPtr1 = new JDOMNodePointer(parentPtr, child1);
        JDOMNodePointer childPtr2 = new JDOMNodePointer(parentPtr, child2);
        int result = parentPtr.compareChildNodePointers(childPtr1, childPtr2);
        assertTrue(result < 0);
    }

    // ==================== setValue ====================

    @Test
    public void testSetValue_TextNodeWithNonEmptyString_UpdatesText() {
        Text text = new Text("old");
        Element parent = new Element("parent");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(new JDOMNodePointer(parent, Locale.US), text);
        pointer.setValue("new");
        assertEquals("new", text.getText());
    }

    @Test
    public void testSetValue_TextNodeWithEmptyString_RemovesContent() {
        Text text = new Text("old");
        Element parent = new Element("parent");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(new JDOMNodePointer(parent, Locale.US), text);
        pointer.setValue("");
        assertNull(text.getParent()); // text removed from parent
    }

    // ==================== remove ====================

    @Test
    public void testRemove_ElementChild_RemovesFromParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);
        childPtr.remove();
        assertFalse(parent.getContent().contains(child));
    }

    // ==================== getNamespaceURI ====================

    @Test
    public void testGetNamespaceURI_ElementWithNamespace_ReturnsURI() {
        Element element = new Element("root", "http://example.com");
        JDOMNodePointer pointer = createPointer(element);
        assertEquals("http://example.com", pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURI_ElementNoNamespace_ReturnsNull() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertNull(pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIWithPrefix_ElementWithNamespace_ReturnsURI() {
        Element element = new Element("root", "ns", "http://example.com");
        JDOMNodePointer pointer = createPointer(element);
        assertEquals("http://example.com", pointer.getNamespaceURI("ns"));
    }

    @Test
    public void testGetNamespaceURIWithPrefix_ElementUnknownPrefix_ReturnsNull() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertNull(pointer.getNamespaceURI("unknown"));
    }

    // ==================== equals/hashCode ====================

    @Test
    public void testEquals_SameNode_ReturnsTrue() {
        Element element = new Element("root");
        JDOMNodePointer p1 = createPointer(element);
        JDOMNodePointer p2 = createPointer(element);
        assertEquals(p1, p2);
    }

    @Test
    public void testEquals_DifferentNode_ReturnsFalse() {
        Element e1 = new Element("a");
        Element e2 = new Element("b");
        JDOMNodePointer p1 = createPointer(e1);
        JDOMNodePointer p2 = createPointer(e2);
        assertNotEquals(p1, p2);
    }

    // ==================== isCollection / getLength ====================

    @Test
    public void testIsCollection_ReturnsFalse() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testGetLength_ReturnsOne() {
        Element element = new Element("root");
        JDOMNodePointer pointer = createPointer(element);
        assertEquals(1, pointer.getLength());
    }
}
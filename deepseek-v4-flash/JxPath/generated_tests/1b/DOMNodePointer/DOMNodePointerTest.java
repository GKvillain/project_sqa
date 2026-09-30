package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;

import org.junit.Test;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;

import java.util.Locale;

public class DOMNodePointerTest {

    // Helper: create an empty Document
    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    // ========== testNode ==========

    @Test
    public void testTestNode_nullTest_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("any");
        assertTrue(DOMNodePointer.testNode(elem, null));
    }

    @Test
    public void testTestNode_nonElementNodeNameTest_returnsFalse() throws Exception {
        Document doc = createDocument();
        Text text = doc.createTextNode("text");
        NodeNameTest test = new NodeNameTest(new QName("test"));
        assertFalse(DOMNodePointer.testNode(text, test));
    }

    @Test
    public void testTestNode_wildcardNoPrefix_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("any");
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_wildcardWithPrefixMatchingNS_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns", "p:foo");
        NodeNameTest test = new NodeNameTest(new QName("p", "*"), "http://ns");
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_exactNameMatchSameNS_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns", "p:foo");
        NodeNameTest test = new NodeNameTest(new QName("p", "foo"), "http://ns");
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_exactNameMatchDifferentNS_returnsFalse() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://other", "p:foo");
        NodeNameTest test = new NodeNameTest(new QName("p", "foo"), "http://ns");
        assertFalse(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_nodeTypeNodeOnElement_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("e");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(elem, test));
    }

    @Test
    public void testTestNode_nodeTypeNodeOnText_returnsFalse() throws Exception {
        Document doc = createDocument();
        Text text = doc.createTextNode("txt");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertFalse(DOMNodePointer.testNode(text, test));
    }

    @Test
    public void testTestNode_nodeTypeTextOnText_returnsTrue() throws Exception {
        Document doc = createDocument();
        Text text = doc.createTextNode("txt");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(text, test));
    }

    @Test
    public void testTestNode_nodeTypeTextOnCDATA_returnsTrue() throws Exception {
        Document doc = createDocument();
        CDATASection cdata = doc.createCDATASection("cdata");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(cdata, test));
    }

    @Test
    public void testTestNode_nodeTypeComment_returnsTrue() throws Exception {
        Document doc = createDocument();
        Comment comment = doc.createComment("c");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, test));
    }

    @Test
    public void testTestNode_nodeTypePI_returnsTrue() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_piTestMatch_returnsTrue() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(pi, test));
    }

    @Test
    public void testTestNode_piTestNoMatch_returnsFalse() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(pi, test));
    }

    // ========== static getNamespaceURI (known defect) ==========

    @Test(expected = ClassCastException.class)
    public void testStaticGetNamespaceURI_textNode_throwsClassCastException() throws Exception {
        Document doc = createDocument();
        Text text = doc.createTextNode("x");
        DOMNodePointer.getNamespaceURI((Node) text);
    }

    @Test(expected = ClassCastException.class)
    public void testStaticGetNamespaceURI_attrNode_throwsClassCastException() throws Exception {
        Document doc = createDocument();
        Attr attr = doc.createAttribute("a");
        DOMNodePointer.getNamespaceURI((Node) attr);
    }

    @Test
    public void testStaticGetNamespaceURI_document_returnsNull() throws Exception {
        Document doc = createDocument();
        assertNull(DOMNodePointer.getNamespaceURI((Node) doc));
    }

    @Test
    public void testStaticGetNamespaceURI_elementWithNS_returnsURI() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns", "p:foo");
        assertEquals("http://ns", DOMNodePointer.getNamespaceURI(elem));
    }

    @Test
    public void testStaticGetNamespaceURI_elementWithXmlnsDefault_returnsURI() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("foo");
        elem.setAttribute("xmlns", "http://default");
        assertEquals("http://default", DOMNodePointer.getNamespaceURI(elem));
    }

    @Test
    public void testStaticGetNamespaceURI_elementWithXmlnsPrefix_returnsURI() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("p:foo");
        elem.setAttribute("xmlns:p", "http://prefix");
        assertEquals("http://prefix", DOMNodePointer.getNamespaceURI(elem));
    }

    // ========== getLocalName / getPrefix ==========

    @Test
    public void testGetLocalName_withLocalName_returnsLocal() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns", "p:foo");
        assertEquals("foo", DOMNodePointer.getLocalName(elem));
    }

    @Test
    public void testGetLocalName_withoutLocalName_returnsNodeName() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("foo");
        assertEquals("foo", DOMNodePointer.getLocalName(elem));
    }

    @Test
    public void testGetPrefix_withPrefix_returnsPrefix() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns", "p:foo");
        assertEquals("p", DOMNodePointer.getPrefix(elem));
    }

    @Test
    public void testGetPrefix_withoutPrefix_returnsNull() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("foo");
        assertNull(DOMNodePointer.getPrefix(elem));
    }

    // ========== asPath (basic) ==========

    @Test
    public void testAsPath_rootElement_returnsEmptyPath() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("", ptr.asPath());
    }

    @Test
    public void testAsPath_textNode_returnsTextPosition() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Text text = doc.createTextNode("text");
        root.appendChild(text);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, text);
        assertEquals("/text()[1]", textPtr.asPath());
    }

    // ========== setValue / getValue ==========

    @Test
    public void testSetValue_textNode_setString_updatesValue() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Text text = doc.createTextNode("old");
        root.appendChild(text);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, text);
        textPtr.setValue("new");
        assertEquals("new", text.getNodeValue());
    }

    @Test
    public void testGetValue_elementWithText_returnsTrimmedText() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Text text = doc.createTextNode("  content  ");
        root.appendChild(text);
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("content", ptr.getValue());
    }

    // ========== isLanguage / getLanguage ==========

    @Test
    public void testIsLanguage_withXmlLang_returnsTrue() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        elem.setAttribute("xml:lang", "en");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertTrue(ptr.isLanguage("en"));
    }

    // ========== instance getNamespaceURI / getDefaultNamespaceURI ==========

    @Test
    public void testInstanceGetNamespaceURI_prefixXml_returnsXMLNamespace() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("http://www.w3.org/XML/1998/namespace", ptr.getNamespaceURI("xml"));
    }

    @Test
    public void testGetDefaultNamespaceURI_withXmlns_returnsURI() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xmlns", "http://default");
        doc.appendChild(root);
        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer ptr = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://default", ptr.getDefaultNamespaceURI());
    }

    // ========== compareChildNodePointers ==========

    @Test
    public void testCompareChildNodePointers_sameNode_returnsZero() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(0, ptr.compareChildNodePointers(ptr, ptr));
    }
}
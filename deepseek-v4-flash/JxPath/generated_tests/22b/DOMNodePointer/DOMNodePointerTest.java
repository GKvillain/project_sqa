package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Test class for DOMNodePointer.
 */
public class DOMNodePointerTest {

    private Document createDocument(String xml) throws Exception {
        // Use a simple DOM implementation
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        return doc;
    }

    // Tests the constructor with Node and Locale
    @Test
    public void testConstructor_nodeAndLocale_setsNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNotNull(pointer);
        assertEquals(root, pointer.getBaseValue());
        assertEquals(root, pointer.getImmediateNode());
    }

    // Tests the constructor with Node, Locale, and id
    @Test
    public void testConstructor_nodeLocaleId_setsFields() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH, "test-id");
        assertNotNull(pointer);
        assertEquals("test-id", pointer.asPath().substring(4, 12));
    }

    // Tests the constructor with parent pointer and node
    @Test
    public void testConstructor_parentNode_setsParentAndNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Node child = root.getFirstChild();
        DOMNodePointer parentPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, child);
        assertNotNull(childPointer);
        assertEquals(child, childPointer.getBaseValue());
        assertEquals(parentPointer, childPointer.getParent());
    }

    // Tests testNode with null test returns true
    @Test
    public void testTestNode_nullTest_returnsTrue() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    // Tests testNode with NodeNameTest for element node
    @Test
    public void testTestNode_elementNodeNameTest_returnsTrue() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        NodeNameTest test = new NodeNameTest(new QName(null, "root"));
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    // Tests testNode with NodeNameTest for non-element node returns false
    @Test
    public void testTestNode_textNodeNameTest_returnsFalse() throws Exception {
        Document doc = createDocument("<root>text</root>");
        Node textNode = doc.getDocumentElement().getFirstChild();
        NodeNameTest test = new NodeNameTest(new QName(null, "text"));
        assertFalse(DOMNodePointer.testNode(textNode, test));
    }

    // Tests testNode with NodeNameTest wildcard and no prefix returns true
    @Test
    public void testTestNode_wildcardNoPrefix_returnsTrue() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(child, test));
    }

    // Tests testNode with NodeTypeTest for NODE type
    @Test
    public void testTestNode_typeTestNode_returnsTrue() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    // Tests testNode with NodeTypeTest for TEXT type on text node
    @Test
    public void testTestNode_typeTestText_returnsTrue() throws Exception {
        Document doc = createDocument("<root>text</root>");
        Node textNode = doc.getDocumentElement().getFirstChild();
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, test));
    }

    // Tests testNode with NodeTypeTest for COMMENT type
    @Test
    public void testTestNode_typeTestComment_returnsTrue() throws Exception {
        Document doc = createDocument("<root><!-- comment --></root>");
        Node commentNode = doc.getDocumentElement().getFirstChild();
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(commentNode, test));
    }

    // Tests testNode with NodeTypeTest for PI type
    @Test
    public void testTestNode_typeTestPI_returnsTrue() throws Exception {
        Document doc = createDocument("<?xml version='1.0'?><root><?target data?></root>");
        Node piNode = doc.getDocumentElement().getFirstChild();
        NodeTypeTest test = new NodeTypeTest(org.apache.commons.jxpath.ri.Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    // Tests testNode with NodeTypeTest for invalid type returns false
    @Test
    public void testTestNode_typeTestInvalidType_returnsFalse() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        NodeTypeTest test = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    // Tests testNode with ProcessingInstructionTest
    @Test
    public void testTestNode_piTest_returnsTrue() throws Exception {
        Document doc = createDocument("<root><?target data?></root>");
        Node piNode = doc.getDocumentElement().getFirstChild();
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    // Tests testNode with ProcessingInstructionTest for non-PI node returns false
    @Test
    public void testTestNode_piTestOnElement_returnsFalse() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    // Tests testNode with ProcessingInstructionTest for wrong target returns false
    @Test
    public void testTestNode_piTestWrongTarget_returnsFalse() throws Exception {
        Document doc = createDocument("<root><?target data?></root>");
        Node piNode = doc.getDocumentElement().getFirstChild();
        ProcessingInstructionTest test = new ProcessingInstructionTest("wrong");
        assertFalse(DOMNodePointer.testNode(piNode, test));
    }

    // Tests getName for element node
    @Test
    public void testGetName_elementNode_returnsCorrectName() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("child", name.getName());
    }

    // Tests getNamespaceURI for element with namespace
    @Test
    public void testGetNamespaceURI_elementWithNamespace_returnsURI() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'><ns:child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://example.com", pointer.getNamespaceURI());
    }

    // Tests getNamespaceURI for element without namespace
    @Test
    public void testGetNamespaceURI_elementWithoutNamespace_returnsNull() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertNull(pointer.getNamespaceURI());
    }

    // Tests isLeaf for leaf node
    @Test
    public void testIsLeaf_leafNode_returnsTrue() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(pointer.isLeaf());
    }

    // Tests isLeaf for non-leaf node
    @Test
    public void testIsLeaf_nonLeafNode_returnsFalse() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertFalse(pointer.isLeaf());
    }

    // Tests isCollection returns false
    @Test
    public void testIsCollection_always_returnsFalse() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertFalse(pointer.isCollection());
    }

    // Tests getLength returns 1
    @Test
    public void testGetLength_always_returnsOne() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(1, pointer.getLength());
    }

    // Tests isActual returns true
    @Test
    public void testIsActual_always_returnsTrue() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertTrue(pointer.isActual());
    }

    // Tests getValue for text node
    @Test
    public void testGetValue_textNode_returnsText() throws Exception {
        Document doc = createDocument("<root>hello world</root>");
        Node textNode = doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        assertEquals("hello world", pointer.getValue());
    }

    // Tests getValue for comment node
    @Test
    public void testGetValue_commentNode_returnsTrimmedText() throws Exception {
        Document doc = createDocument("<root><!-- comment text --></root>");
        Node commentNode = doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(commentNode, Locale.ENGLISH);
        assertEquals("comment text", pointer.getValue());
    }

    // Tests getValue for element node returns concatenated text
    @Test
    public void testGetValue_elementNode_returnsConcatenatedText() throws Exception {
        Document doc = createDocument("<root><child>text1</child><child>text2</child></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("text1text2", pointer.getValue());
    }

    // Tests remove on non-root node
    @Test
    public void testRemove_nonRootNode_removesNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Node child = root.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.remove();
        assertNull(root.getFirstChild());
    }

    // Tests remove on root node throws JXPathException
    @Test(expected = JXPathException.class)
    public void testRemove_rootNode_throwsException() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        pointer.remove();
    }

    // Tests isLanguage with matching language
    @Test
    public void testIsLanguage_matchingLanguage_returnsTrue() throws Exception {
        Document doc = createDocument("<root xml:lang='en'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
    }

    // Tests isLanguage with non-matching language
    @Test
    public void testIsLanguage_nonMatchingLanguage_returnsFalse() throws Exception {
        Document doc = createDocument("<root xml:lang='en'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertFalse(pointer.isLanguage("fr"));
    }

    // Tests equals with same node returns true
    @Test
    public void testEquals_sameNode_returnsTrue() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer1 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(root, Locale.FRENCH);
        assertTrue(pointer1.equals(pointer2));
    }

    // Tests equals with different node returns false
    @Test
    public void testEquals_differentNode_returnsFalse() throws Exception {
        Document doc = createDocument("<root><child1/><child2/></root>");
        Element root = doc.getDocumentElement();
        Node child1 = root.getFirstChild();
        Node child2 = root.getLastChild();
        DOMNodePointer pointer1 = new DOMNodePointer(child1, Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(child2, Locale.ENGLISH);
        assertFalse(pointer1.equals(pointer2));
    }

    // Tests equals with same object returns true
    @Test
    public void testEquals_sameObject_returnsTrue() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertTrue(pointer.equals(pointer));
    }

    // Tests equals with non-DOMNodePointer returns false
    @Test
    public void testEquals_nonDOMNodePointer_returnsFalse() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertFalse(pointer.equals(new Object()));
    }

    // Tests hashCode returns node hash
    @Test
    public void testHashCode_returnsNodeHashCode() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(root.hashCode(), pointer.hashCode());
    }

    // Tests getPrefix for node with prefix
    @Test
    public void testGetPrefix_nodeWithPrefix_returnsPrefix() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'><ns:child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertEquals("ns", DOMNodePointer.getPrefix(child));
    }

    // Tests getPrefix for node without prefix
    @Test
    public void testGetPrefix_nodeWithoutPrefix_returnsNull() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertNull(DOMNodePointer.getPrefix(child));
    }

    // Tests getLocalName for node with local name
    @Test
    public void testGetLocalName_nodeWithLocalName_returnsLocalName() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertEquals("child", DOMNodePointer.getLocalName(child));
    }

    // Tests getLocalName for node with prefix
    @Test
    public void testGetLocalName_nodeWithPrefix_returnsLocalName() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'><ns:child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertEquals("child", DOMNodePointer.getLocalName(child));
    }

    // Tests getNamespaceURI static method for node with namespace
    @Test
    public void testGetNamespaceURIStatic_nodeWithNamespace_returnsURI() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'><ns:child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(child));
    }

    // Tests getNamespaceURI static method for node without namespace
    @Test
    public void testGetNamespaceURIStatic_nodeWithoutNamespace_returnsNull() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        assertNull(DOMNodePointer.getNamespaceURI(child));
    }

    // Tests setValue on text node with non-empty string
    @Test
    public void testSetValue_textNodeNonEmpty_setsValue() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Node child = root.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.setValue("new text");
        assertEquals("new text", child.getNodeValue());
    }

    // Tests setValue on text node with empty string
    @Test
    public void testSetValue_textNodeEmpty_removesNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Node child = root.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.setValue("");
        assertNull(root.getFirstChild());
    }

    // Tests setValue on element with string value
    @Test
    public void testSetValue_elementWithString_addsTextNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.setValue("value");
        assertEquals("value", child.getTextContent());
    }

    // Tests setValue with Node value on element
    @Test
    public void testSetValue_elementWithNode_clonesChildren() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();
        // Create a node with children
        Document doc2 = createDocument("<source><child1/><child2/></source>");
        Element sourceRoot = doc2.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        pointer.setValue(sourceRoot);
        assertEquals(2, child.getChildNodes().getLength());
    }

    // Tests getPointerByID returns NullPointer when element not found
    @Test
    public void testGetPointerByID_notFound_returnsNullPointer() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        org.apache.commons.jxpath.Pointer result = pointer.getPointerByID(null, "nonexistent");
        assertTrue(result instanceof org.apache.commons.jxpath.ri.model.beans.NullPointer);
    }

    // Tests createAttribute creates attribute on element
    @Test
    public void testCreateAttribute_element_createsAttribute() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        QName name = new QName(null, "attr");
        org.apache.commons.jxpath.ri.model.NodePointer result = pointer.createAttribute(null, name);
        assertNotNull(result);
        assertTrue(root.hasAttribute("attr"));
    }

    // Tests createAttribute creates attribute with prefix
    @Test
    public void testCreateAttribute_elementWithPrefix_createsAttribute() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        QName name = new QName("ns", "attr");
        org.apache.commons.jxpath.ri.model.NodePointer result = pointer.createAttribute(null, name);
        assertNotNull(result);
        assertEquals("", root.getAttributeNS("http://example.com", "attr"));
    }

    // Tests asPath for element with parent
    @Test
    public void testAsPath_elementWithParent_returnsPath() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();
        DOMNodePointer parentPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, child);
        String path = childPointer.asPath();
        assertTrue(path.contains("child"));
    }

    // Tests asPath for text node
    @Test
    public void testAsPath_textNode_returnsTextPath() throws Exception {
        Document doc = createDocument("<root>text</root>");
        Element root = doc.getDocumentElement();
        Node textNode = root.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        String path = pointer.asPath();
        assertTrue(path.contains("text()"));
    }

    // Tests asPath for document node returns empty string
    @Test
    public void testAsPath_documentNode_returnsEmptyString() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", pointer.asPath());
    }

    // Tests asPath for processing instruction
    @Test
    public void testAsPath_piNode_returnsPIPath() throws Exception {
        Document doc = createDocument("<root><?target data?></root>");
        Node piNode = doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(piNode, Locale.ENGLISH);
        String path = pointer.asPath();
        assertTrue(path.contains("processing-instruction"));
    }

    // Tests getNamespaceURI with prefix
    @Test
    public void testGetNamespaceURIWithPrefix_prefixXml_returnsXMLURI() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
    }

    // Tests getNamespaceURI with prefix xmlns
    @Test
    public void testGetNamespaceURIWithPrefix_prefixXmlns_returnsXMLNSURI() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
    }

    // Tests getNamespaceURI with null prefix returns default namespace
    @Test
    public void testGetNamespaceURIWithPrefix_nullPrefix_returnsDefault() throws Exception {
        Document doc = createDocument("<root xmlns='http://default.com'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("http://default.com", pointer.getNamespaceURI(null));
    }

    // Tests getNamespaceURI with unknown prefix returns null
    @Test
    public void testGetNamespaceURIWithPrefix_unknownPrefix_returnsNull() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNull(pointer.getNamespaceURI("unknown"));
    }

    // Tests getDefaultNamespaceURI returns null when no default namespace
    @Test
    public void testGetDefaultNamespaceURI_noDefault_returnsNull() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    // Tests getDefaultNamespaceURI returns default when present
    @Test
    public void testGetDefaultNamespaceURI_withDefault_returnsDefault() throws Exception {
        Document doc = createDocument("<root xmlns='http://default.com'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("http://default.com", pointer.getDefaultNamespaceURI());
    }

    // Tests getDefaultNamespaceURI returns null for empty default
    @Test
    public void testGetDefaultNamespaceURI_emptyDefault_returnsNull() throws Exception {
        Document doc = createDocument("<root xmlns=''/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    // Tests getNamespaceResolver returns non-null resolver
    @Test
    public void testGetNamespaceResolver_returnsResolver() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNotNull(pointer.getNamespaceResolver());
    }

    // Tests getNamespaceResolver called twice returns same resolver
    @Test
    public void testGetNamespaceResolver_calledTwice_returnsSameInstance() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        org.apache.commons.jxpath.ri.NamespaceResolver resolver1 = pointer.getNamespaceResolver();
        org.apache.commons.jxpath.ri.NamespaceResolver resolver2 = pointer.getNamespaceResolver();
        assertEquals(resolver1, resolver2);
    }

    // Tests namespacePointer returns NamespacePointer
    @Test
    public void testNamespacePointer_returnsNamespacePointer() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        NodePointer result = pointer.namespacePointer("prefix");
        assertTrue(result instanceof NamespacePointer);
    }

    // Tests namespaceIterator returns DOMNamespaceIterator
    @Test
    public void testNamespaceIterator_returnsIterator() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertNotNull(pointer.namespaceIterator());
    }

    // Tests getNode returns the underlying node
    @Test
    public void testGetNode_returnsNode() throws Exception {
        Document doc = createDocument("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals(root, pointer.getImmediateNode());
    }
}
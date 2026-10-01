package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.Locale;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;

public class DOMNodePointerTest {

    private Document document;
    private Element rootElement;
    private Element childElement;
    private Element childWithNamespace;
    private Text textNode;
    private ProcessingInstruction piNode;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        rootElement = document.createElement("root");
        document.appendChild(rootElement);

        childElement = document.createElement("child");
        rootElement.appendChild(childElement);

        textNode = document.createTextNode("text content");
        childElement.appendChild(textNode);

        piNode = document.createProcessingInstruction("target", "data");
        childElement.appendChild(piNode);

        childWithNamespace = document.createElementNS("http://example.com/ns", "ns:prefixed");
        rootElement.appendChild(childWithNamespace);
    }

    private DOMNodePointer createPointer(Node node) {
        return new DOMNodePointer(node, Locale.ENGLISH);
    }

    // Tests wildcard without prefix – should match any element
    @Test
    public void testTestNode_wildcardNoPrefix_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    // Tests exact element name match
    @Test
    public void testTestNode_exactNameMatch_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName(null, "child"), null);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    // Tests namespace URI match
    @Test
    public void testTestNode_namespaceMatch_returnsTrue() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "prefixed"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(childWithNamespace, test));
    }

    // Tests namespace URI mismatch
    @Test
    public void testTestNode_namespaceMismatch_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName("ns", "prefixed"), "http://other.com/ns");
        assertFalse(DOMNodePointer.testNode(childWithNamespace, test));
    }

    // Tests NodeNameTest on non-element node – should return false
    @Test
    public void testTestNode_nonElementNode_returnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName(null, "text"), null);
        assertFalse(DOMNodePointer.testNode(textNode, test));
    }

    // Tests node type test for NODE_TYPE_NODE on element
    @Test
    public void testTestNode_nodeTypeTestNode_returnsTrueForElement() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(childElement, test));
    }

    // Tests node type test for NODE_TYPE_TEXT on text node
    @Test
    public void testTestNode_nodeTypeTestText_returnsTrueForText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, test));
    }

    // Tests ProcessingInstructionTest with matching target
    @Test
    public void testTestNode_processingInstructionTestMatch_returnsTrue() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    // Tests getName for element without prefix
    @Test
    public void testGetName_elementNoPrefix_returnsCorrectQName() {
        DOMNodePointer ptr = createPointer(childElement);
        QName name = ptr.getName();
        assertEquals("child", name.getName());
        assertNull(name.getPrefix());
    }

    // Tests getName for element with prefix
    @Test
    public void testGetName_elementWithPrefix_returnsQNameWithPrefix() {
        DOMNodePointer ptr = createPointer(childWithNamespace);
        QName name = ptr.getName();
        assertEquals("prefixed", name.getName());
        assertEquals("ns", name.getPrefix());
    }

    // Tests getNamespaceURI() instance method
    @Test
    public void testGetNamespaceURI_withNamespace_returnsURI() {
        DOMNodePointer ptr = createPointer(childWithNamespace);
        assertEquals("http://example.com/ns", ptr.getNamespaceURI());
    }

    // Tests getNamespaceURI(String) for "xml" prefix
    @Test
    public void testGetNamespaceURI_xmlPrefix_returnsXmlNamespace() {
        DOMNodePointer ptr = createPointer(childElement);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, ptr.getNamespaceURI("xml"));
    }

    // Tests getNamespaceURI(String) for a known prefix defined on an ancestor
    @Test
    public void testGetNamespaceURI_knownPrefix_returnsNamespace() {
        rootElement.setAttribute("xmlns:test", "http://test.com/ns");
        DOMNodePointer ptr = createPointer(childElement);
        assertEquals("http://test.com/ns", ptr.getNamespaceURI("test"));
    }

    // Tests setValue on a text node
    @Test
    public void testSetValue_textNode_setsNodeValue() {
        DOMNodePointer ptr = createPointer(textNode);
        ptr.setValue("new value");
        assertEquals("new value", textNode.getNodeValue());
    }

    // Tests setValue on an element node (replaces children with text)
    @Test
    public void testSetValue_elementNode_removesChildrenAndAddsTextNode() {
        DOMNodePointer ptr = createPointer(childElement);
        ptr.setValue("new text");
        NodeList children = childElement.getChildNodes();
        assertEquals(1, children.getLength());
        assertEquals(Node.TEXT_NODE, children.item(0).getNodeType());
        assertEquals("new text", children.item(0).getNodeValue());
    }

    // Tests asPath for a child element
    @Test
    public void testAsPath_childElement_returnsProperPath() {
        DOMNodePointer rootPtr = createPointer(rootElement);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, childElement);
        assertEquals("/root[1]/child[1]", childPtr.asPath());
    }

    // Tests asPath for a text node
    @Test
    public void testAsPath_textNode_returnsTextPosition() {
        DOMNodePointer rootPtr = createPointer(rootElement);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, childElement);
        DOMNodePointer textPtr = new DOMNodePointer(childPtr, textNode);
        assertEquals("/root[1]/child[1]/text()[1]", textPtr.asPath());
    }

    // Tests isLanguage with matching xml:lang attribute
    @Test
    public void testIsLanguage_matchingLanguage_returnsTrue() {
        childElement.setAttribute("xml:lang", "en-US");
        DOMNodePointer ptr = createPointer(childElement);
        assertTrue(ptr.isLanguage("en"));
    }

    // Tests equals for two pointers pointing to the same node
    @Test
    public void testEquals_differentObjectSameNode_returnsTrue() {
        DOMNodePointer ptr1 = createPointer(childElement);
        DOMNodePointer ptr2 = createPointer(childElement);
        assertTrue(ptr1.equals(ptr2));
    }

    // Tests compareChildNodePointers with attribute before element
    @Test
    public void testCompareChildNodePointers_attributeBeforeElement_returnsMinusOne() {
        Attr attr = document.createAttribute("myattr");
        attr.setValue("val");
        rootElement.setAttributeNode(attr);
        DOMNodePointer rootPtr = createPointer(rootElement);
        DOMNodePointer attrPtr = new DOMNodePointer(rootPtr, attr);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, childElement);
        int result = rootPtr.compareChildNodePointers(attrPtr, childPtr);
        assertTrue(result < 0);
    }
}
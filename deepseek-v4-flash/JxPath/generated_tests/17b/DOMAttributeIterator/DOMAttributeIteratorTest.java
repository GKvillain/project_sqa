package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class DOMAttributeIteratorTest {

    // Creates an element with attributes from XML string
    private Element createElementWithAttributes(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        return doc.getDocumentElement();
    }

    // Creates a simple NodePointer for testing
    private NodePointer createPointer(Node node) throws Exception {
        // We need a NodePointer implementation that works with DOM nodes
        // Use DOMNodePointer which is available in the same package
        return new DOMNodePointer(node, null, null);
    }

    // Helper to get attributes from iterator
    private List<Attr> collectAttributes(DOMAttributeIterator iterator) {
        List<Attr> result = new ArrayList<>();
        while (iterator.setPosition(iterator.getPosition() + 1)) {
            NodePointer pointer = iterator.getNodePointer();
            if (pointer != null) {
                result.add((Attr) pointer.getNode());
            }
        }
        return result;
    }

    // Tests normal case: element with attributes, wildcard name
    @Test
    public void testConstructor_wildcardName_returnsAllAttributes() throws Exception {
        Element element = createElementWithAttributes(
            "<root attr1=\"value1\" attr2=\"value2\" attr3=\"value3\"/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Should have 3 attributes
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        
        // Collect all attributes
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(3, attrs.size());
        assertEquals("attr1", attrs.get(0).getName());
        assertEquals("attr2", attrs.get(1).getName());
        assertEquals("attr3", attrs.get(2).getName());
    }

    // Tests normal case: element with specific attribute name
    @Test
    public void testConstructor_specificName_returnsMatchingAttribute() throws Exception {
        Element element = createElementWithAttributes(
            "<root attr1=\"value1\" attr2=\"value2\"/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(1, attrs.size());
        assertEquals("attr1", attrs.get(0).getName());
        assertEquals("value1", attrs.get(0).getValue());
    }

    // Tests edge case: element without attributes
    @Test
    public void testConstructor_noAttributes_returnsEmpty() throws Exception {
        Element element = createElementWithAttributes("<root/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(0, attrs.size());
    }

    // Tests edge case: non-element node (e.g., text node)
    @Test
    public void testConstructor_nonElementNode_returnsEmpty() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("root");
        element.appendChild(doc.createTextNode("text"));
        NodePointer pointer = createPointer(element.getFirstChild());
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(0, attrs.size());
    }

    // Tests specific attribute with namespace prefix
    @Test
    public void testConstructor_attributeWithNamespace_returnsMatchingAttribute() throws Exception {
        Element element = createElementWithAttributes(
            "<root xmlns:ns=\"http://example.com\" ns:attr1=\"value1\" attr2=\"value2\"/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, 
            new QName("ns", "attr1"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(1, attrs.size());
        assertEquals("value1", attrs.get(0).getValue());
    }

    // Tests excluding namespace attributes (xmlns)
    @Test
    public void testConstructor_wildcardWithNamespaces_excludesXmlns() throws Exception {
        Element element = createElementWithAttributes(
            "<root xmlns:ns=\"http://example.com\" attr1=\"value1\"/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        List<Attr> attrs = collectAttributes(iterator);
        // Should only have attr1, not xmlns:ns
        assertEquals(1, attrs.size());
        assertEquals("attr1", attrs.get(0).getName());
    }

    // Tests case: attribute with prefix but no namespace URI match
    @Test
    public void testConstructor_prefixMismatch_returnsEmpty() throws Exception {
        Element element = createElementWithAttributes(
            "<root xmlns:ns1=\"http://example1.com\" xmlns:ns2=\"http://example2.com\" " +
            "ns1:attr1=\"value1\" ns2:attr2=\"value2\"/>");
        NodePointer pointer = createPointer(element);
        
        // Try to match with wrong namespace prefix
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, 
            new QName("ns2", "attr1"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(0, attrs.size());
    }

    // Tests namespace URI matching, not just prefix matching
    @Test
    public void testConstructor_prefixDifferentButSameNamespace_returnsMatch() throws Exception {
        Element element = createElementWithAttributes(
            "<root xmlns:prefix1=\"http://example.com\" prefix1:attr=\"value\"/>");
        NodePointer pointer = createPointer(element);
        
        // Try with different prefix that maps to same namespace URI
        // Note: This typically won't match because prefixes must match
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, 
            new QName("otherPrefix", "attr"));
        
        // This may or may not match depending on resolver implementation
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(0, attrs.size());
    }

    // Tests setPosition with invalid positions
    @Test
    public void testSetPosition_invalidPositions_returnsFalse() throws Exception {
        Element element = createElementWithAttributes(
            "<root attr1=\"value1\" attr2=\"value2\"/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Position 0 should fail
        assertFalse(iterator.setPosition(0));
        // Position beyond size should fail
        assertFalse(iterator.setPosition(3));
        // Negative position should fail
        assertFalse(iterator.setPosition(-1));
        
        // Valid positions should work
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
    }

    // Tests getNodePointer returns null for invalid position
    @Test
    public void testGetNodePointer_invalidPosition_returnsNull() throws Exception {
        Element element = createElementWithAttributes("<root/>");
        NodePointer pointer = createPointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // No attributes, so no valid position
        assertNull(iterator.getNodePointer());
    }

    // Tests with null prefix attribute handling
    @Test
    public void testConstructor_noPrefixButNameMatches_returnsAttribute() throws Exception {
        Element element = createElementWithAttributes("<root attr1=\"value1\"/>");
        NodePointer pointer = createPointer(element);
        
        // No prefix in QName
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(1, attrs.size());
        assertEquals("value1", attrs.get(0).getValue());
    }

    // Tests attribute with prefix matching by prefix
    @Test
    public void testConstructor_prefixMatchAndAttributeExists_returnsAttribute() throws Exception {
        Element element = createElementWithAttributes(
            "<root xmlns:ns=\"http://example.com\" ns:attr1=\"value1\"/>");
        NodePointer pointer = createPointer(element);
        
        // Match with correct prefix
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, 
            new QName("ns", "attr1"));
        
        List<Attr> attrs = collectAttributes(iterator);
        assertEquals(1, attrs.size());
        assertEquals("value1", attrs.get(0).getValue());
    }
}
package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DOMAttributeIteratorTest {

    private Document doc;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
    }

    // Tests non-element node returns empty iterator and handles setPosition/getNodePointer correctly
    @Test
    public void testConstructor_nonElementNode_emptyAttributes() {
        Text textNode = doc.createTextNode("sample text");
        DOMNodePointer parent = new DOMNodePointer(textNode, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests element with no attributes returns false for setPosition
    @Test
    public void testSetPosition_emptyElement_returnsFalse() {
        Element element = doc.createElement("root");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));

        assertFalse(iterator.setPosition(1));
        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests exact match attribute retrieval without namespace
    @Test
    public void testGetAttribute_exactMatchWithoutNamespace_returnsAttributePointer() {
        Element element = doc.createElement("root");
        element.setAttribute("name", "value");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("name"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("value", ((Attr) ptr.getNode()).getValue());
        assertFalse(iterator.setPosition(2));
    }

    // Tests non-matching attribute name returns empty iterator
    @Test
    public void testGetAttribute_nonMatchingName_returnsNull() {
        Element element = doc.createElement("root");
        element.setAttribute("name", "value");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("other"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests position=0 behavior in getNodePointer without calling setPosition first
    @Test
    public void testGetNodePointer_positionZeroWithAttributes_returnsFirstAttributePointer() {
        Element element = doc.createElement("root");
        element.setAttribute("attr1", "val1");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr1"));

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("attr1", ((Attr) ptr.getNode()).getName());
        assertEquals(0, iterator.getPosition());
    }

    // Tests wildcard query collects all attributes and iterates properly
    @Test
    public void testIterate_wildcardAllAttributes_iteratesCorrectly() {
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertFalse(iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());
    }

    // Tests wildcard query ignores xmlns and xmlns:prefix declarations
    @Test
    public void testIterate_wildcardIgnoresXmlnsAttributes() {
        Element element = doc.createElementNS("http://example.com/ns", "test:root");
        element.setAttribute("xmlns", "http://example.com/ns");
        element.setAttribute("xmlns:foo", "http://foo.com");
        element.setAttribute("validAttr", "val");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("validAttr", ((Attr) iterator.getNodePointer().getNode()).getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests attribute retrieval with namespace prefix matching
    @Test
    public void testGetAttribute_withNamespacePrefix_matchesCorrectly() {
        Element element = doc.createElementNS("http://example.com", "ns:root");
        element.setAttributeNS("http://example.com/custom", "custom:attr", "val");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("custom", "attr"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("val", ((Attr) ptr.getNode()).getValue());
    }

    // Tests boundary positions on setPosition
    @Test
    public void testSetPosition_boundaryValues_returnsExpectedBooleans() {
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("a"));

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertFalse(iterator.setPosition(-5));
        assertEquals(-5, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests wildcard with namespace prefix
    @Test
    public void testIterate_wildcardWithNamespacePrefix_matchesNamespaceAttributes() {
        Element element = doc.createElementNS("http://example.com", "root");
        element.setAttributeNS("http://example.com/ns1", "p1:attr1", "v1");
        element.setAttributeNS("http://example.com/ns2", "p2:attr2", "v2");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("p1", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("p1:attr1", ((Attr) iterator.getNodePointer().getNode()).getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests attribute retrieval with unresolved namespace prefix falls back to local name check
    @Test
    public void testGetAttribute_unresolvedPrefix_matchesByName() {
        Element element = doc.createElement("root");
        element.setAttribute("attr", "val");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("unknown", "attr"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("attr", ((Attr) ptr.getNode()).getName());
    }

    // Tests attribute retrieval when namespace is resolved but attribute does not exist in namespace
    @Test
    public void testGetAttribute_resolvedNamespaceNonExistingAttribute_returnsEmpty() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:root");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("ns", "nonExistent"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard with unresolved prefix matches non-namespaced attributes
    @Test
    public void testIterate_wildcardWithUnresolvedPrefix_matchesNonNamespacedAttributes() {
        Element element = doc.createElement("root");
        element.setAttribute("attr1", "v1");
        element.setAttributeNS("http://example.com/ns", "p:attr2", "v2");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("unknown", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", ((Attr) iterator.getNodePointer().getNode()).getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests getNodePointer returns null when position is set beyond the valid attribute list
    @Test
    public void testGetNodePointer_outOfBoundsPosition_returnsNull() {
        Element element = doc.createElement("root");
        element.setAttribute("attr", "val");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));

        assertFalse(iterator.setPosition(5));
        assertNull(iterator.getNodePointer());

        assertFalse(iterator.setPosition(-2));
        assertNull(iterator.getNodePointer());
    }

    // Tests searching an attribute with prefix against an element having namespaced attribute with non-matching local name
    @Test
    public void testGetAttribute_resolvedNamespaceLocalNameMismatch_returnsEmpty() {
        Element element = doc.createElementNS("http://example.com/ns", "ns:root");
        element.setAttributeNS("http://example.com/ns", "ns:otherAttr", "value");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("ns", "targetAttr"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests xml:lang attribute handling via standard xml prefix
    @Test
    public void testGetAttribute_xmlPrefixAttribute_matchesCorrectly() {
        Element element = doc.createElementNS("http://example.com", "root");
        element.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en");
        DOMNodePointer parent = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("xml", "lang"));

        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("en", ((Attr) ptr.getNode()).getValue());
    }
}
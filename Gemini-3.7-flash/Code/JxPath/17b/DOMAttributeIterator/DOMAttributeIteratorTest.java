package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.*;

public class DOMAttributeIteratorTest {

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    // Tests iterating all attributes using wildcard '*' on element with multiple attributes
    @Test
    public void testConstructor_wildcard_iteratesAllAttributes() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertFalse(iterator.setPosition(3));
    }

    // Tests specific attribute by simple name without prefix
    @Test
    public void testConstructor_specificAttributeName_findsAttribute() {
        Element element = document.createElement("testElement");
        element.setAttribute("id", "123");
        element.setAttribute("name", "test");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("id"));

        assertTrue(iterator.setPosition(1));
        assertEquals("id", ((DOMAttributePointer) iterator.getNodePointer()).getName().getName());
        assertEquals("123", iterator.getNodePointer().getValue());
        assertFalse(iterator.setPosition(2));
    }

    // Tests specific attribute that does not exist on element
    @Test
    public void testConstructor_nonExistentAttribute_returnsEmpty() {
        Element element = document.createElement("testElement");
        element.setAttribute("id", "123");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("nonexistent"));

        assertFalse(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
    }

    // Tests parent node that is not an Element (e.g. Document node)
    @Test
    public void testConstructor_nonElementNode_returnsEmpty() {
        DOMNodePointer pointer = new DOMNodePointer(document, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests filtering out xmlns declarations when wildcard is used
    @Test
    public void testConstructor_wildcard_ignoresXmlnsDeclarations() {
        Element element = document.createElementNS("http://example.com/ns", "ns:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://example.com/ns");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://example.com/foo");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", ((DOMAttributePointer) iterator.getNodePointer()).getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests wildcard with namespace prefix matching
    @Test
    public void testConstructor_wildcardWithNamespacePrefix_matchesNamespacedAttributes() {
        Element element = document.createElement("testElement");
        element.setAttributeNS("http://example.com/ns", "foo:attr1", "val1");
        element.setAttribute("attr2", "val2");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        pointer.getNamespaceResolver().registerNamespace("myfoo", "http://example.com/ns");

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("myfoo", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", ((DOMAttributePointer) iterator.getNodePointer()).getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests specific attribute with namespace prefix
    @Test
    public void testConstructor_specificNamespacedAttribute_findsMatchingAttribute() {
        Element element = document.createElement("testElement");
        element.setAttributeNS("http://example.com/ns", "foo:attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        pointer.getNamespaceResolver().registerNamespace("foo", "http://example.com/ns");

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "attr1"));

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals("val1", iterator.getNodePointer().getValue());
    }

    // Tests specific attribute with unknown prefix
    @Test
    public void testConstructor_unknownNamespacePrefix_returnsEmpty() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknown", "attr1"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer behavior when position is reset to 0
    @Test
    public void testGetNodePointer_positionZero_automaticallyAdvancesToFirst() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        assertEquals(0, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals(0, iterator.getPosition());
    }

    // Tests setPosition with negative, zero, and boundary values
    @Test
    public void testSetPosition_boundaryValues_returnsExpectedBoolean() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests getNodePointer when attributes list is empty
    @Test
    public void testGetNodePointer_emptyAttributes_returnsNull() {
        Element element = document.createElement("testElement");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        assertNull(iterator.getNodePointer());
    }

    // Tests namespace prefix with predefined 'xml' namespace (e.g. xml:lang)
    @Test
    public void testConstructor_xmlPrefix_matchesXmlAttribute() {
        Element element = document.createElement("testElement");
        element.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("xml", "lang"));

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals("en", iterator.getNodePointer().getValue());
    }

    // Tests specific namespaced attribute with mismatched local name or mismatched namespace
    @Test
    public void testConstructor_namespacedAttribute_mismatchNamespaceOrLocalName() {
        Element element = document.createElement("testElement");
        element.setAttributeNS("http://example.com/ns1", "ns1:attr1", "val1");
        element.setAttributeNS("http://example.com/ns2", "ns2:attr2", "val2");
        element.setAttribute("attr1", "valUnnamespaced");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        pointer.getNamespaceResolver().registerNamespace("ns1", "http://example.com/ns1");

        // Namespace matches, but local name does not match
        DOMAttributeIterator iterator1 = new DOMAttributeIterator(pointer, new QName("ns1", "otherAttr"));
        assertFalse(iterator1.setPosition(1));

        // Local name matches, but namespace does not match
        DOMAttributeIterator iterator2 = new DOMAttributeIterator(pointer, new QName("ns1", "attr2"));
        assertFalse(iterator2.setPosition(1));
    }

    // Tests wildcard ignores plain attribute whose name starts with "xmlns"
    @Test
    public void testConstructor_wildcard_ignoresPlainXmlnsAttribute() {
        Element element = document.createElement("testElement");
        element.setAttribute("xmlns", "http://example.com/test");
        element.setAttribute("xmlns:custom", "http://example.com/custom");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer when position is set to an out-of-bounds index
    @Test
    public void testGetNodePointer_outOfBoundsPosition_returnsNull() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        iterator.setPosition(5);
        assertNull(iterator.getNodePointer());

        iterator.setPosition(-2);
        assertNull(iterator.getNodePointer());
    }

    // Tests element without any attributes attached
    @Test
    public void testConstructor_elementWithoutAttributes_attributesEmpty() {
        Element element = document.createElement("testElement");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }
}
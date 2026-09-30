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

    // Tests initial position state
    @Test
    public void testGetPosition_initiallyZero_returnsZero() {
        Element element = document.createElement("testElement");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));

        assertEquals(0, iterator.getPosition());
    }

    // Tests iterating when parent node is not an element node
    @Test
    public void testConstructor_nonElementNode_iteratorIsEmpty() {
        NodePointer pointer = new DOMNodePointer(document, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests finding a specific attribute by local name
    @Test
    public void testConstructor_existingAttribute_findsAttribute() {
        Element element = document.createElement("testElement");
        element.setAttribute("name", "value");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("name"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("value", attrPointer.getValue());
    }

    // Tests querying an attribute that does not exist
    @Test
    public void testConstructor_nonExistentAttribute_emptyIterator() {
        Element element = document.createElement("testElement");
        element.setAttribute("other", "value");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("missing"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard "*" iteration across multiple attributes
    @Test
    public void testConstructor_wildcard_iteratesAllAttributes() {
        Element element = document.createElement("testElement");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertTrue(iterator.setPosition(2));
        assertNotNull(iterator.getNodePointer());
        assertFalse(iterator.setPosition(3));
    }

    // Tests wildcard iteration ignores xmlns attribute declarations
    @Test
    public void testConstructor_wildcardWithXmlns_ignoresXmlnsAttributes() {
        Element element = document.createElementNS("http://test", "testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://test");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://foo");
        element.setAttribute("actualAttr", "val");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("actualAttr", iterator.getNodePointer().getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests setPosition boundaries (negative, zero, beyond size)
    @Test
    public void testSetPosition_boundaryValues_returnsExpectedBoolean() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        assertFalse(iterator.setPosition(-1));
        assertFalse(iterator.setPosition(0));
        assertTrue(iterator.setPosition(1));
        assertFalse(iterator.setPosition(2));
    }

    // Tests getNodePointer behavior when position is 0
    @Test
    public void testGetNodePointer_positionZero_automaticallyAdvancesAndReturnsPointer() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr", "val");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));

        assertEquals(0, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer returns null when position is 0 and iterator is empty
    @Test
    public void testGetNodePointer_positionZeroAndEmpty_returnsNull() {
        Element element = document.createElement("testElement");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("missing"));

        assertNull(iterator.getNodePointer());
    }

    // Tests namespaced attribute lookup by prefix and local name
    @Test
    public void testConstructor_namespacedAttributeWithPrefix_findsAttribute() {
        Element element = document.createElementNS("http://example.com/ns", "testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        element.setAttributeNS("http://example.com/ns", "ns:attr", "nsValue");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns", "attr"));

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("nsValue", attrPointer.getValue());
    }

    // Tests namespaced wildcard lookup matching specific prefix
    @Test
    public void testConstructor_namespacedWildcard_matchesMatchingNamespace() {
        Element element = document.createElementNS("http://example.com/ns", "testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:other", "http://example.com/other");
        element.setAttributeNS("http://example.com/ns", "ns:attr1", "val1");
        element.setAttributeNS("http://example.com/other", "other:attr2", "val2");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", iterator.getNodePointer().getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests non-matching namespace attribute lookup returns empty
    @Test
    public void testConstructor_namespacedAttributeUnmatchedPrefix_emptyIterator() {
        Element element = document.createElementNS("http://example.com/ns", "testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        element.setAttributeNS("http://example.com/ns", "ns:attr", "val");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknown", "attr"));

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests repositioning back and forth across multiple attributes
    @Test
    public void testSetPosition_navigateForwardAndBackward() {
        Element element = document.createElement("testElement");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertEquals("2", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertEquals("1", iterator.getNodePointer().getValue());
    }

    // Tests looking up an attribute with xml namespace prefix
    @Test
    public void testConstructor_xmlPrefixAttribute_findsAttribute() {
        Element element = document.createElement("testElement");
        element.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("xml", "lang"));

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("en", attrPointer.getValue());
    }

    // Tests looking up xmlns attribute specifically by xmlns prefix
    @Test
    public void testConstructor_xmlnsPrefixQuery_findsXmlnsAttribute() {
        Element element = document.createElementNS("http://example.com/ns", "testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://foo");

        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("xmlns", "foo"));

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("http://foo", attrPointer.getValue());
    }

    // Tests wildcard query on an element with no attributes
    @Test
    public void testConstructor_elementWithNoAttributes_returnsEmpty() {
        Element element = document.createElement("emptyElement");
        NodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }
}
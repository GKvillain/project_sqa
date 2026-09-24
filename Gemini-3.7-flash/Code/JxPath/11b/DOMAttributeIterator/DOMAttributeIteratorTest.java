package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DOMAttributeIteratorTest {

    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        document = factory.newDocumentBuilder().newDocument();
    }

    // Tests iteration on a non-element DOM node (document node)
    @Test
    public void testConstructor_nonElementNode_emptyAttributes() {
        DOMNodePointer pointer = new DOMNodePointer(document, Locale.ENGLISH);
        QName qname = new QName("attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests matching a specific attribute without namespace
    @Test
    public void testConstructor_specificAttribute_matchesExistingAttribute() {
        Element element = document.createElement("test");
        element.setAttribute("name", "value");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("name");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("value", attrPointer.getValue());
    }

    // Tests matching a specific attribute that does not exist on the element
    @Test
    public void testConstructor_specificAttributeNotFound_returnsNull() {
        Element element = document.createElement("test");
        element.setAttribute("other", "value");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("name");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard matching for all standard attributes
    @Test
    public void testConstructor_wildcardAttribute_matchesAllAttributes() {
        Element element = document.createElement("test");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertTrue(iterator.setPosition(2));
        assertNotNull(iterator.getNodePointer());
        assertFalse(iterator.setPosition(3));
    }

    // Tests that xmlns and xmlns:prefix declarations are filtered out during wildcard iteration
    @Test
    public void testConstructor_wildcardAttribute_ignoresXmlnsAttributes() {
        Element element = document.createElementNS("http://example.com/ns", "test");
        element.setAttribute("xmlns", "http://example.com/ns");
        element.setAttribute("xmlns:custom", "http://example.com/custom");
        element.setAttribute("regular", "value");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("regular", attrPointer.getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests specific namespaced attribute lookup with matching prefix and namespace
    @Test
    public void testConstructor_namespacedAttribute_matchesCorrectNamespace() {
        Element element = document.createElementNS("http://example.com/ns", "test");
        element.setAttributeNS("http://example.com/ns", "custom:attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("custom", "attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("val", attrPointer.getValue());
    }

    // Tests wildcard attribute matching filtered by specific namespace prefix
    @Test
    public void testConstructor_namespacedWildcard_matchesOnlyMatchingPrefix() {
        Element element = document.createElementNS("http://example.com/default", "test");
        element.setAttributeNS("http://example.com/ns", "p:attr1", "val1");
        element.setAttributeNS("http://example.com/other", "other:attr2", "val2");
        element.setAttribute("unprefixed", "val3");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("p", "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("attr1", attrPointer.getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    // Tests setPosition with boundaries and off-by-one indices
    @Test
    public void testSetPosition_boundaryValues_returnsExpectedBoolean() {
        Element element = document.createElement("test");
        element.setAttribute("attr1", "val1");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests getNodePointer automatically advancing when position is initial 0
    @Test
    public void testGetNodePointer_positionZero_advancesToFirstElement() {
        Element element = document.createElement("test");
        element.setAttribute("attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertEquals(0, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals(0, iterator.getPosition());
    }

    // Tests namespaced attribute when attribute is not present
    @Test
    public void testConstructor_namespacedAttributeNotFound_returnsNull() {
        Element element = document.createElementNS("http://example.com/ns", "test");
        element.setAttributeNS("http://example.com/ns", "custom:attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("custom", "nonExisting");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer returning null when initial position is 0 and element has no attributes
    @Test
    public void testGetNodePointer_positionZero_emptyAttributes_returnsNull() {
        Element element = document.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertEquals(0, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests unprefixed QName does not match namespaced/prefixed attribute with the same local name
    @Test
    public void testConstructor_unprefixedQName_doesNotMatchPrefixedAttribute() {
        Element element = document.createElementNS("http://example.com/ns", "test");
        element.setAttributeNS("http://example.com/ns", "p:attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests standard XML namespace attributes such as xml:lang
    @Test
    public void testConstructor_xmlNamespaceAttribute_matchesCorrectly() {
        Element element = document.createElement("test");
        element.setAttributeNS(XMLConstants.XML_NS_URI, "xml:lang", "en");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("xml", "lang");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("en", attrPointer.getValue());
    }

    // Tests lookup with an unknown/unresolvable namespace prefix
    @Test
    public void testConstructor_unresolvablePrefix_returnsNull() {
        Element element = document.createElement("test");
        element.setAttribute("attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("unknownPrefix", "attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard with an unresolvable namespace prefix
    @Test
    public void testConstructor_wildcardWithUnresolvablePrefix_returnsEmpty() {
        Element element = document.createElementNS("http://example.com/ns", "test");
        element.setAttributeNS("http://example.com/ns", "p:attr", "val");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        QName qname = new QName("unknownPrefix", "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }
}
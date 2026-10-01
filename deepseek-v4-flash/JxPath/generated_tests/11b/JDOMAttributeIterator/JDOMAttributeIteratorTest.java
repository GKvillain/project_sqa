package org.apache.commons.jxpath.ri.model.jdom;

import static org.junit.Assert.*;
import org.junit.Test;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;

public class JDOMAttributeIteratorTest {

    // Tests constructor for element with no namespace and a specific existing attribute
    @Test
    public void testConstructor_elementWithNoNamespaceAndSpecificAttrExists_returnsOneAttribute() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("id", "1"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("id"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());
        assertFalse(iterator.setPosition(2));
    }

    // Tests constructor for element with no namespace and a specific non-existing attribute
    @Test
    public void testConstructor_elementWithNoNamespaceAndSpecificAttrNotExists_returnsEmpty() {
        Element element = new Element("root");
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("missing"));
        assertFalse(iterator.setPosition(1));
    }

    // Tests constructor with wildcard and no namespace, returns all no-namespace attributes
    @Test
    public void testConstructor_elementWithNoNamespaceAndWildcard_returnsAllNoNamespaceAttrs() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1"));
        element.setAttribute(new Attribute("b", "2"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertFalse(iterator.setPosition(3));
    }

    // Tests constructor with XML namespace and specific attribute
    @Test
    public void testConstructor_elementWithXMLNamespaceAndSpecificAttr_returnsAttribute() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("lang", "en", Namespace.XML_NAMESPACE));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("xml", "lang"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertFalse(iterator.setPosition(2));
    }

    // Tests constructor with custom namespace and specific attribute
    @Test
    public void testConstructor_elementWithCustomNamespaceAndSpecificAttr_returnsAttribute() {
        Namespace ns = Namespace.getNamespace("pre", "http://example.com");
        Element element = new Element("root", ns);
        element.setAttribute(new Attribute("attr", "val", ns));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("pre", "attr"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertFalse(iterator.setPosition(2));
    }

    // Tests constructor with undefined prefix, should return empty list immediately
    @Test
    public void testConstructor_elementWithUndefinedPrefix_returnsEmpty() {
        Element element = new Element("root");
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("undefined", "attr"));
        assertFalse(iterator.setPosition(1));
    }

    // Tests constructor with wildcard and specific namespace filters attributes correctly
    @Test
    public void testConstructor_elementWithWildcardAndSpecificNamespace_returnsMatchingAttrs() {
        Namespace ns = Namespace.getNamespace("pre", "http://example.com");
        Element element = new Element("root");
        element.setAttribute(new Attribute("a1", "v1", ns));
        element.setAttribute(new Attribute("a2", "v2"));
        element.setAttribute(new Attribute("a3", "v3", ns));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("pre", "*"));
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertFalse(iterator.setPosition(3));
    }

    // Tests setPosition with invalid positions (less than 1)
    @Test
    public void testSetPosition_invalidPositionLessThanOne_returnsFalse() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("a"));
        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(-1));
        assertEquals(0, iterator.getPosition());
    }

    // Tests setPosition with valid positions returns true and updates position
    @Test
    public void testSetPosition_validPosition_returnsTrueAndUpdatesPosition() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1"));
        element.setAttribute(new Attribute("b", "2"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests getNodePointer when position is 0, internally calls setPosition(1)
    @Test
    public void testGetNodePointer_whenPositionZero_callsSetPositionOneAndReturnsPointer() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("a"));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer when setPosition fails (no attributes) returns null
    @Test
    public void testGetNodePointer_whenSetPositionFails_returnsNull() {
        Element element = new Element("root");
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("nonexistent"));
        NodePointer ptr = iterator.getNodePointer();
        assertNull(ptr);
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer after a valid setPosition returns pointer at that position
    @Test
    public void testGetNodePointer_afterSetPosition_returnsPointerAtThatPosition() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1"));
        element.setAttribute(new Attribute("b", "2"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        iterator.setPosition(2);
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals(2, iterator.getPosition());
    }

    // Tests constructor with wildcard and XML namespace returns only XML attributes
    @Test
    public void testConstructor_elementWithXMLNamespaceWildcard_returnsXmlAttrs() {
        Element element = new Element("root");
        element.setAttribute(new Attribute("space", "preserve", Namespace.XML_NAMESPACE));
        element.setAttribute(new Attribute("id", "1"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("xml", "*"));
        assertTrue(iterator.setPosition(1));
        assertFalse(iterator.setPosition(2));
    }

    // Tests constructor with wildcard and no namespace returns only no-namespace attributes
    @Test
    public void testConstructor_elementWithWildcardAndNoNamespace_returnsOnlyNoNamespaceAttrs() {
        Namespace ns = Namespace.getNamespace("pre", "http://example.com");
        Element element = new Element("root");
        element.setAttribute(new Attribute("a", "1", ns));
        element.setAttribute(new Attribute("b", "2"));
        NodePointer parent = new JDOMNodePointer(element);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        assertTrue(iterator.setPosition(1));
        assertFalse(iterator.setPosition(2));
    }
}
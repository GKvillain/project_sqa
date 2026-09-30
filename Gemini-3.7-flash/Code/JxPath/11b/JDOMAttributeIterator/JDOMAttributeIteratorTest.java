package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JDOMAttributeIteratorTest {

    private Element rootElement;
    private NodePointer rootPointer;

    @Before
    public void setUp() {
        rootElement = new Element("root");
        rootPointer = new JDOMNodePointer(rootElement, Locale.ENGLISH);
    }

    // Tests iterating when parent node is not an Element
    @Test
    public void testConstructor_nonElementNode_attributesRemainNull() {
        NodePointer textPointer = new JDOMNodePointer("plain text", Locale.ENGLISH);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(textPointer, new QName("attr"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests specific attribute without prefix when attribute exists
    @Test
    public void testGetNodePointer_existingAttributeNoPrefix_returnsAttributePointer() {
        rootElement.setAttribute(new Attribute("name", "value"));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("name"));

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("value", ptr.getValue());
    }

    // Tests specific attribute without prefix when attribute does not exist
    @Test
    public void testGetNodePointer_nonExistingAttributeNoPrefix_returnsNull() {
        rootElement.setAttribute(new Attribute("other", "value"));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("name"));

        assertFalse(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard '*' matching all attributes in default (no) namespace
    @Test
    public void testSetPosition_wildcardNoPrefix_matchesAllNoNamespaceAttributes() {
        Namespace customNs = Namespace.getNamespace("custom", "http://example.com/custom");
        rootElement.setAttribute(new Attribute("a1", "v1"));
        rootElement.setAttribute(new Attribute("a2", "v2"));
        rootElement.setAttribute(new Attribute("a3", "v3", customNs));

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("v1", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals("v2", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(3));
    }

    // Tests attribute with 'xml' prefix
    @Test
    public void testGetNodePointer_xmlPrefixAttribute_matchesXmlNamespace() {
        rootElement.setAttribute(new Attribute("lang", "en", Namespace.XML_NAMESPACE));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("xml", "lang"));

        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("en", ptr.getValue());
    }

    // Tests wildcard '*' with 'xml' prefix
    @Test
    public void testSetPosition_xmlPrefixWildcard_matchesXmlAttributesOnly() {
        rootElement.setAttribute(new Attribute("lang", "en", Namespace.XML_NAMESPACE));
        rootElement.setAttribute(new Attribute("id", "123"));

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("xml", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("en", iterator.getNodePointer().getValue());
        assertFalse(iterator.setPosition(2));
    }

    // Tests attribute with known custom namespace prefix
    @Test
    public void testGetNodePointer_customNamespacePrefix_matchesCustomNamespaceAttribute() {
        Namespace customNs = Namespace.getNamespace("custom", "http://example.com/custom");
        rootElement.addNamespaceDeclaration(customNs);
        rootElement.setAttribute(new Attribute("item", "widget", customNs));

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("custom", "item"));

        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("widget", ptr.getValue());
    }

    // Tests attribute with unknown namespace prefix
    @Test
    public void testSetPosition_unknownNamespacePrefix_emptyListAndReturnsFalse() {
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("unknown", "item"));

        assertFalse(iterator.setPosition(1));
        assertEquals(0, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer() when position is 0 and attributes are present
    @Test
    public void testGetNodePointer_positionZero_automaticallyAdvancesAndResetsPosition() {
        rootElement.setAttribute(new Attribute("title", "book"));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("title"));

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("book", ptr.getValue());
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer() when position is 0 and attributes list is empty
    @Test
    public void testGetNodePointer_positionZeroAndNoAttributes_returnsNull() {
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("missing"));

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNull(ptr);
        assertEquals(1, iterator.getPosition());
    }

    // Tests boundary positions: negative, zero, and out-of-range positive
    @Test
    public void testSetPosition_boundaryValues_returnsCorrectBoolean() {
        rootElement.setAttribute(new Attribute("k1", "v1"));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("k1"));

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests wildcard '*' with custom namespace prefix
    @Test
    public void testSetPosition_customNamespaceWildcard_matchesCustomNamespaceAttributesOnly() {
        Namespace customNs = Namespace.getNamespace("custom", "http://example.com/custom");
        rootElement.addNamespaceDeclaration(customNs);
        rootElement.setAttribute(new Attribute("attr1", "val1", customNs));
        rootElement.setAttribute(new Attribute("attr2", "val2", customNs));
        rootElement.setAttribute(new Attribute("other", "val3"));

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("custom", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("val1", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals("val2", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(3));
    }

    // Tests getNodePointer() when position is negative
    @Test
    public void testGetNodePointer_negativePosition_returnsFirstAttributePointer() {
        rootElement.setAttribute(new Attribute("name", "value"));
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("name"));

        iterator.setPosition(-1);
        assertEquals(-1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("value", ptr.getValue());
    }

    // Tests getNodePointer() when attributes list is null due to unknown namespace prefix
    @Test
    public void testGetNodePointer_nullAttributes_returnsNull() {
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(rootPointer, new QName("unknown", "attr"));
        assertNull(iterator.getNodePointer());
    }
}
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JDOMAttributeIteratorTest {

    // Tests non-Element parent node pointer
    @Test
    public void testConstructor_nonElementNode_attributesIsNull() {
        NodePointer parent = NodePointer.newNodePointer(new QName("test"), "stringNode", Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr"));

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests specific attribute found without namespace prefix
    @Test
    public void testGetNodePointer_specificAttributeFound_returnsPointer() {
        Element element = new Element("test");
        element.setAttribute("name", "value");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("name"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertTrue(ptr instanceof JDOMAttributePointer);
        assertEquals("value", ptr.getValue());
    }

    // Tests specific attribute not found without namespace prefix
    @Test
    public void testGetNodePointer_specificAttributeNotFound_returnsNull() {
        Element element = new Element("test");
        element.setAttribute("other", "value");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("nonexistent"));
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer auto-advancing when position is zero
    @Test
    public void testGetNodePointer_positionZeroWithAttribute_returnsPointerAndPreservesPosition() {
        Element element = new Element("test");
        element.setAttribute("name", "value");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("name"));
        assertEquals(0, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer when position is zero and no attributes exist
    @Test
    public void testGetNodePointer_positionZeroWithoutAttribute_returnsNull() {
        Element element = new Element("test");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("name"));
        assertNull(iterator.getNodePointer());
    }

    // Tests xml prefix attribute resolution
    @Test
    public void testConstructor_xmlPrefix_matchesXmlNamespaceAttribute() {
        Element element = new Element("test");
        element.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("xml", "lang"));
        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("en", ptr.getValue());
    }

    // Tests unknown custom prefix where element namespace is null
    @Test
    public void testConstructor_unknownPrefix_emptyListCreated() {
        Element element = new Element("test");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("unknownPrefix", "attr"));
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests known custom prefix matching attribute
    @Test
    public void testConstructor_customNamespacePrefix_matchesAttribute() {
        Namespace ns = Namespace.getNamespace("custom", "http://example.com/custom");
        Element element = new Element("test");
        element.addNamespaceDeclaration(ns);
        element.setAttribute("myAttr", "customVal", ns);
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("custom", "myAttr"));
        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("customVal", ptr.getValue());
    }

    // Tests wildcard attribute matching with default namespace
    @Test
    public void testConstructor_wildcardName_matchesAllAttributesInDefaultNamespace() {
        Element element = new Element("test");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        Namespace customNs = Namespace.getNamespace("custom", "http://example.com/custom");
        element.setAttribute("attr3", "val3", customNs);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("val1", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals("val2", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(3));
    }

    // Tests wildcard attribute matching with custom namespace prefix
    @Test
    public void testConstructor_wildcardWithNamespacePrefix_matchesOnlyNamespaceAttributes() {
        Namespace ns = Namespace.getNamespace("custom", "http://example.com/custom");
        Element element = new Element("test");
        element.addNamespaceDeclaration(ns);
        element.setAttribute("noNsAttr", "val0");
        element.setAttribute("attr1", "val1", ns);
        element.setAttribute("attr2", "val2", ns);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("custom", "*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("val1", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals("val2", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(3));
    }

    // Tests wildcard attribute matching when element has no attributes
    @Test
    public void testConstructor_wildcardWithNoAttributes_returnsEmpty() {
        Element element = new Element("test");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests setPosition boundaries (negative, zero, valid, out of bounds)
    @Test
    public void testSetPosition_boundaryValues_returnsExpectedResults() {
        Element element = new Element("test");
        element.setAttribute("attr", "val");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr"));

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests getNodePointer after setting an explicit positive index
    @Test
    public void testGetNodePointer_afterExplicitSetPosition_returnsCorrectAttribute() {
        Element element = new Element("test");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));
        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("2", ptr.getValue());
    }
}
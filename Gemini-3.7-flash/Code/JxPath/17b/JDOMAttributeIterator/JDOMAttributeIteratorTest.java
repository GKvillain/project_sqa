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

    private Element element;
    private JDOMNodePointer parentPointer;
    private Namespace customNs;

    @Before
    public void setUp() {
        customNs = Namespace.getNamespace("custom", "http://example.com/custom");
        element = new Element("testRoot");
        element.setAttribute(new Attribute("id", "123"));
        element.setAttribute(new Attribute("name", "sample"));
        element.setAttribute(new Attribute("lang", "en", Namespace.XML_NAMESPACE));
        element.setAttribute(new Attribute("customAttr", "val", customNs));

        parentPointer = new JDOMNodePointer(element, Locale.getDefault());
        parentPointer.getNamespaceResolver().registerNamespace("custom", "http://example.com/custom");
    }

    // Tests iterating with wildcard (*) matching attributes without prefix (NO_NAMESPACE)
    @Test
    public void testWildcardWithoutPrefix_returnsMatchingAttributes() {
        QName name = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr1 = iterator.getNodePointer();
        assertNotNull(ptr1);
        assertEquals("id", ptr1.getName().getName());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        NodePointer ptr2 = iterator.getNodePointer();
        assertNotNull(ptr2);
        assertEquals("name", ptr2.getName().getName());

        assertFalse(iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());
    }

    // Tests iterating with wildcard (*) and xml prefix matching XML namespace attributes
    @Test
    public void testWildcardWithXmlPrefix_returnsXmlNamespaceAttributes() {
        QName name = new QName("xml", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("lang", ptr.getName().getName());

        assertFalse(iterator.setPosition(2));
    }

    // Tests iterating with wildcard (*) and custom prefix matching custom namespace attributes
    @Test
    public void testWildcardWithCustomPrefix_returnsCustomNamespaceAttributes() {
        QName name = new QName("custom", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("customAttr", ptr.getName().getName());

        assertFalse(iterator.setPosition(2));
    }

    // Tests matching specific attribute by name with no prefix
    @Test
    public void testSpecificAttributeNoPrefix_found_returnsAttributePointer() {
        QName name = new QName("name");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("name", ptr.getName().getName());
        assertEquals("sample", ptr.getValue());

        assertFalse(iterator.setPosition(2));
    }

    // Tests searching for non-existing attribute by name with no prefix
    @Test
    public void testSpecificAttributeNoPrefix_notFound_returnsEmpty() {
        QName name = new QName("nonExisting");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertFalse(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests matching attribute with xml prefix (xml:lang)
    @Test
    public void testSpecificAttributeXmlPrefix_found_returnsAttributePointer() {
        QName name = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("lang", ptr.getName().getName());
        assertEquals("en", ptr.getValue());
    }

    // Tests matching attribute with custom prefix resolved by namespace resolver
    @Test
    public void testSpecificAttributeCustomPrefix_found_returnsAttributePointer() {
        QName name = new QName("custom", "customAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("customAttr", ptr.getName().getName());
        assertEquals("val", ptr.getValue());
    }

    // Tests prefix that cannot be resolved by namespace resolver
    @Test
    public void testUnresolvablePrefix_returnsEmptyList() {
        QName name = new QName("unknownPrefix", "testAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests getNodePointer when position is initially 0
    @Test
    public void testGetNodePointer_positionZero_advancesAndResetsPosition() {
        QName name = new QName("id");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("id", ptr.getName().getName());
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer when position is 0 but attributes list is empty
    @Test
    public void testGetNodePointer_positionZeroEmptyList_returnsNull() {
        QName name = new QName("nonExistent");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNull(ptr);
    }

    // Tests setPosition boundary conditions (negative, zero, and out of range)
    @Test
    public void testSetPosition_boundaryValues_returnsCorrectBoolean() {
        QName name = new QName("id");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parentPointer, name);

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    // Tests parent pointer whose node is not an Element instance (e.g. String)
    @Test
    public void testParentNodeNotElement_attributesNull_operationsReturnSafeDefaults() {
        JDOMNodePointer nonElementParent = new JDOMNodePointer("just a string", Locale.getDefault());
        QName name = new QName("any");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(nonElementParent, name);

        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }
}
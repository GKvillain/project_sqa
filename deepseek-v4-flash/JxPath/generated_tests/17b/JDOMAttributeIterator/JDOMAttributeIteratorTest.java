package org.apache.commons.jxpath.ri.model.jdom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;

public class JDOMAttributeIteratorTest {

    // Test constructor with null namespace prefix and non-wildcard name for existing attribute
    @Test
    public void testConstructor_nullPrefixNonWildcardName_existingAttribute_returnsIteratorWithOneElement() {
        Element element = new Element("testElement");
        Attribute attr = new Attribute("testAttr", "value");
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "testAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull(pointer);
        assertEquals("value", pointer.getValue());
    }

    // Test constructor with null namespace prefix and non-wildcard name for non-existing attribute
    @Test
    public void testConstructor_nullPrefixNonWildcardName_nonExistingAttribute_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Attribute attr = new Attribute("testAttr", "value");
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "nonExistingAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Test constructor with null namespace prefix and wildcard name matching all attributes
    @Test
    public void testConstructor_nullPrefixWildcardName_matchingAttributes_returnsIteratorWithAll() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        element.setAttribute(new Attribute("attr2", "val2"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertFalse(iterator.setPosition(3));
    }

    // Test constructor with xml prefix
    @Test
    public void testConstructor_xmlPrefix_returnsIteratorWithXmlAttribute() {
        Element element = new Element("testElement");
        Attribute attr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
    }

    // Test constructor with non-null prefix (not xml) that resolves to a namespace
    @Test
    public void testConstructor_nonNullPrefix_resolvedNamespace_returnsIteratorWithMatchingAttribute() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("myPrefix", "http://my.uri");
        Attribute attr = new Attribute("myAttr", "myValue", ns);
        element.setAttribute(attr);
        // need a NodePointer that can resolve namespace; JDOMNodePointer does this via getNamespaceResolver
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("myPrefix", "myAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
    }

    // Test constructor with non-null prefix that does not resolve
    @Test
    public void testConstructor_nonNullPrefix_unresolvedNamespace_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("myPrefix", "http://my.uri");
        Attribute attr = new Attribute("myAttr", "myValue", ns);
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("nonExistingPrefix", "myAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
    }

    // Test getNodePointer when position is 0
    @Test
    public void testGetNodePointer_positionZero_setsPositionAndReturnsFirstPointer() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        element.setAttribute(new Attribute("attr2", "val2"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        // call getNodePointer when position is still 0
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull(pointer);
        assertEquals("val1", pointer.getValue());
        // After getNodePointer, position should be reset to 0? Actually it sets position=1 then resets to 0
        // Then getNodePointer again should still return first
        assertEquals("val1", pointer.getValue());
    }

    // Test getNodePointer when position is already set
    @Test
    public void testGetNodePointer_positionSet_returnsCorrectPointer() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        element.setAttribute(new Attribute("attr2", "val2"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        iterator.setPosition(2);
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull(pointer);
        assertEquals("val2", pointer.getValue());
    }

    // Test getNodePointer when position is negative (should treat as 0 index)
    @Test
    public void testGetNodePointer_negativePosition_returnsFirstPointer() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        // simulate negative position by setting position to 0 then calling getNodePointer
        // getNodePointer with position == 0 will setPosition(1) and then set position = 0
        // Then index = 0-1 = -1, then index = 0, should return first element
        iterator.setPosition(0);
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull(pointer);
        assertEquals("val1", pointer.getValue());
    }

    // Test setPosition with valid position returns true
    @Test
    public void testSetPosition_validPosition_returnsTrue() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        element.setAttribute(new Attribute("attr2", "val2"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
    }

    // Test setPosition with invalid position (0) returns false
    @Test
    public void testSetPosition_positionZero_returnsFalse() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertFalse(iterator.setPosition(0));
    }

    // Test setPosition with position greater than size returns false
    @Test
    public void testSetPosition_positionGreaterThanSize_returnsFalse() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertFalse(iterator.setPosition(2));
    }

    // Test setPosition when attributes list is null (e.g., constructor early return due to null ns unresolved prefix)
    @Test
    public void testSetPosition_attributesNull_returnsFalse() {
        Element element = new Element("testElement");
        // This path happens when prefix is non-null, not xml, and ns resolves to null
        // To trigger attributes = EMPTY_LIST, we need a prefix that resolves to something that gives null uri? Actually code sets EMPTY_LIST and returns.
        // We'll use a prefix that does not resolve; constructor will set attributes = EMPTY_LIST, not null
        // The only way attributes is null is if parent.getNode() is not an Element
        NodePointer parent = new JDOMNodePointer("someString", null, null);
        QName name = new QName(null, "testAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertFalse(iterator.setPosition(1));
    }

    // Test getPosition returns current position
    @Test
    public void testGetPosition_returnsCurrentPosition() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("attr1", "val1"));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertEquals(0, iterator.getPosition());
        iterator.setPosition(1);
        assertEquals(1, iterator.getPosition());
    }

    // Test constructor with non-element node (e.g., String)
    @Test
    public void testConstructor_nonElementNode_attributesIsNullAndSetPositionReturnsFalse() {
        NodePointer parent = new JDOMNodePointer("someString", null, null);
        QName name = new QName(null, "testAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Test getNodePointer when attributes is empty (e.g., no matching attributes)
    @Test
    public void testGetNodePointer_emptyAttributes_returnsNull() {
        Element element = new Element("testElement");
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "nonExistingAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNull(iterator.getNodePointer());
    }

    // Test constructor with wildcard and namespace prefix that matches no attributes
    @Test
    public void testConstructor_wildcardWithNamespace_noMatchingAttributes_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("a", "http://a.uri");
        Attribute attr = new Attribute("attr1", "val1", ns);
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("b", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
    }

    // ========== New test methods to improve coverage ==========

    // Test constructor with xml prefix and wildcard (should return empty because no attribute named "*" in xml namespace)
    @Test
    public void testConstructor_xmlPrefix_wildcard_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Attribute attr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("xml", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
    }

    // Test constructor with xml prefix and non-existing xml attribute
    @Test
    public void testConstructor_xmlPrefix_nonExistentXmlAttribute_returnsEmptyIterator() {
        Element element = new Element("testElement");
        // no xml:lang attribute
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Test constructor with null prefix and non-wildcard name when attribute has namespace (should not match)
    @Test
    public void testConstructor_nullPrefixNonWildcardName_attributeWithNamespace_notMatched_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("p", "http://example.com");
        Attribute attr = new Attribute("myAttr", "value", ns);
        element.setAttribute(attr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "myAttr");  // no prefix, expects attribute with no namespace
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Test constructor with null prefix and wildcard includes attributes with namespace
    @Test
    public void testConstructor_nullPrefixWildcardName_includesNamespaceAttributes_returnsAllAttributes() {
        Element element = new Element("testElement");
        element.setAttribute(new Attribute("noNsAttr", "noNsValue"));
        Namespace ns = Namespace.getNamespace("p", "http://example.com");
        Attribute nsAttr = new Attribute("nsAttr", "nsValue", ns);
        element.setAttribute(nsAttr);
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName(null, "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertFalse(iterator.setPosition(3));
    }

    // Test constructor with resolved namespace and wildcard matching multiple attributes in that namespace
    @Test
    public void testConstructor_nonNullPrefix_resolvedNamespace_wildcard_matchingAttributes_returnsAllInNamespace() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("myPrefix", "http://my.uri");
        element.addNamespaceDeclaration(ns);  // ensure resolver knows the prefix
        element.setAttribute(new Attribute("attr1", "val1", ns));
        element.setAttribute(new Attribute("attr2", "val2", ns));
        // add an attribute from another namespace to verify it is not included
        Namespace otherNs = Namespace.getNamespace("other", "http://other.uri");
        element.setAttribute(new Attribute("otherAttr", "otherVal", otherNs));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("myPrefix", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertFalse(iterator.setPosition(3));
        // verify that the returned pointers are from the correct namespace
        iterator.setPosition(1);
        assertEquals("val1", iterator.getNodePointer().getValue());
        iterator.setPosition(2);
        assertEquals("val2", iterator.getNodePointer().getValue());
    }

    // Test constructor with resolved namespace and wildcard where no attribute has that namespace
    @Test
    public void testConstructor_nonNullPrefix_resolvedNamespace_wildcard_noMatchingAttributes_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("myPrefix", "http://my.uri");
        element.addNamespaceDeclaration(ns);
        // add attribute in a different namespace
        Namespace otherNs = Namespace.getNamespace("other", "http://other.uri");
        element.setAttribute(new Attribute("otherAttr", "otherVal", otherNs));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("myPrefix", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
    }

    // Test constructor with resolved namespace and non-wildcard name that does not match any attribute in that namespace
    @Test
    public void testConstructor_nonNullPrefix_resolvedNamespace_nonWildcard_nonMatchingAttribute_returnsEmptyIterator() {
        Element element = new Element("testElement");
        Namespace ns = Namespace.getNamespace("myPrefix", "http://my.uri");
        element.addNamespaceDeclaration(ns);
        element.setAttribute(new Attribute("existingAttr", "value", ns));
        NodePointer parent = new JDOMNodePointer(element, null, null);
        QName name = new QName("myPrefix", "nonExistingAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        assertNotNull(iterator);
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }
}
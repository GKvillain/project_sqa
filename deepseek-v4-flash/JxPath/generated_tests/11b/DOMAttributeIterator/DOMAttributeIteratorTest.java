package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import org.apache.commons.jxpath.ri.QName;

/**
 * JUnit 4 test class for DOMAttributeIterator.
 * Targets defect D4J-11b: testAttr returns true on prefix match without namespace URI check.
 */
public class DOMAttributeIteratorTest {
    private DocumentBuilder docBuilder;

    @Before
    public void setUp() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            fail("Cannot create DocumentBuilder: " + e.getMessage());
        }
    }

    // Tests constructor with non-element node (DOCUMENT_NODE) -> attributes empty
    @Test
    public void testConstructor_nonElementNode_returnsEmpty() {
        Document document = docBuilder.newDocument();
        DOMNodePointer parent = new DOMNodePointer(document, null);
        QName name = new QName("any");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertFalse("Should have no attributes", iter.setPosition(1));
        assertNull("getNodePointer should be null", iter.getNodePointer());
    }

    // Tests constructor with element and specific localName that does not exist -> empty
    @Test
    public void testConstructor_specificNameNotFound_returnsEmpty() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("nonexistent");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertFalse("Should have no attributes", iter.setPosition(1));
    }

    // Tests constructor with element and specific localName that exists -> returns attribute
    @Test
    public void testConstructor_specificNameFound_returnsAttribute() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("id", "123");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("id");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertTrue("Should have one attribute", iter.setPosition(1));
        assertNotNull("getNodePointer should not be null", iter.getNodePointer());
    }

    // Tests constructor with wildcard and multiple attributes (including xmlns) -> excludes xmlns attributes
    @Test
    public void testConstructor_wildcardExcludesXmlnsAttributes() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        element.setAttribute("xmlns:foo", "http://foo"); // attribute with prefix "xmlns"
        element.setAttribute("xmlns", "http://default"); // attribute localName="xmlns"
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        // Should only see "a" and "b"
        assertTrue("Should have at least one attribute", iter.setPosition(1));
        int count = 0;
        while (iter.setPosition(count + 1)) {
            count++;
        }
        assertEquals("Should have exactly 2 non-xmlns attributes", 2, count);
    }

    // Tests specific name with prefix and matching namespace URI -> found
    @Test
    public void testConstructor_prefixNamespaceMatch_returnsAttribute() throws Exception {
        Document document = docBuilder.newDocument();
        Element element = document.createElementNS(null, "root");
        // set namespace declaration
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a", "http://ns1");
        // create attribute with namespace "http://ns1", prefix "a"
        Attr attr = document.createAttributeNS("http://ns1", "a:attr");
        attr.setValue("value");
        element.setAttributeNodeNS(attr);
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("a", "attr");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertTrue("Should find the attribute", iter.setPosition(1));
        assertNotNull("getNodePointer should not be null", iter.getNodePointer());
    }

    // Tests bug: specific name with prefix but namespace URI mismatch (parent has different mapping)
    // Expected: no attribute found (bug returns true due to prefix match short-circuit)
    @Test
    public void testConstructor_prefixNamespaceMismatch_returnsEmpty() throws Exception {
        Document document = docBuilder.newDocument();
        Element element = document.createElementNS(null, "root");
        // set namespace declaration for prefix "a" to "http://ns2"
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a", "http://ns2");
        // create attribute with namespace "http://ns1" (different) and same prefix "a"
        Attr attr = document.createAttributeNS("http://ns1", "a:attr");
        attr.setValue("value");
        element.setAttributeNodeNS(attr);
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("a", "attr");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        // name.getName() = "attr", testAttr will compare prefix "a" -> matches, but namespace check should fail.
        // Without bug fix, it returns true; with fix, false.
        // So we expect empty (no attribute)
        assertFalse("Should not find attribute due to namespace mismatch", iter.setPosition(1));
    }

    // Tests setPosition with position 0 -> returns false
    @Test
    public void testSetPosition_zeroPosition_returnsFalse() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("x", "1");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertFalse("setPosition(0) should return false", iter.setPosition(0));
    }

    // Tests setPosition with position > size -> returns false
    @Test
    public void testSetPosition_outOfRange_returnsFalse() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("x", "1");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertFalse("setPosition(2) should return false", iter.setPosition(2));
    }

    // Tests setPosition with valid position -> returns true
    @Test
    public void testSetPosition_validPosition_returnsTrue() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("x", "1");
        element.setAttribute("y", "2");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertTrue("setPosition(1) should return true", iter.setPosition(1));
        assertTrue("setPosition(2) should return true", iter.setPosition(2));
    }

    // Tests getNodePointer before any setPosition call -> returns first attribute
    @Test
    public void testGetNodePointer_beforeIteration_returnsFirst() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("first", "1");
        element.setAttribute("second", "2");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        NodePointer pointer = iter.getNodePointer();
        assertNotNull("Should return first attribute pointer", pointer);
        // ensure it's a DOMAttributePointer that points to "first"
        assertTrue("Should be DOMAttributePointer", pointer instanceof DOMAttributePointer);
    }

    // Tests getNodePointer after setPosition to valid index
    @Test
    public void testGetNodePointer_afterSetPosition_returnsCorrect() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("a1", "v1");
        element.setAttribute("a2", "v2");
        element.setAttribute("a3", "v3");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertTrue("Should set position 2", iter.setPosition(2));
        NodePointer pointer = iter.getNodePointer();
        assertNotNull("Pointer should not be null", pointer);
        assertEquals("Position should be 2", 2, iter.getPosition());
    }

    // Tests equalStrings static method indirectly via testAttr branches (covered in other tests)
    // Direct unit test for equalStrings
    @Test
    public void testEqualStrings_nullAndNull_returnsTrue() {
        assertTrue("null and null should be equal", DOMAttributeIterator.equalStrings(null, null));
    }

    @Test
    public void testEqualStrings_nullAndNonNull_returnsFalse() {
        assertFalse("null and non-null should not be equal", DOMAttributeIterator.equalStrings(null, "x"));
        assertFalse("non-null and null should not be equal", DOMAttributeIterator.equalStrings("x", null));
    }

    @Test
    public void testEqualStrings_sameString_returnsTrue() {
        assertTrue("same strings should be equal", DOMAttributeIterator.equalStrings("hello", "hello"));
    }

    // Tests constructor with specific name for attribute without namespace, query with prefix null -> match
    @Test
    public void testConstructor_specificNameNoPrefixMatches() {
        Document document = docBuilder.newDocument();
        Element element = document.createElement("root");
        element.setAttribute("attr", "val");
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName(null, "attr"); // prefix null
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertTrue("Should find attribute with no prefix", iter.setPosition(1));
    }

    // Tests constructor with specific name but attribute has prefix (namespace) and query prefix null -> no match
    @Test
    public void testConstructor_specificNamePrefixMismatchNoPrefixQuery() throws Exception {
        Document document = docBuilder.newDocument();
        Element element = document.createElementNS(null, "root");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a", "http://ns1");
        Attr attr = document.createAttributeNS("http://ns1", "a:attr");
        attr.setValue("val");
        element.setAttributeNodeNS(attr);
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName(null, "attr"); // query with null prefix
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        assertFalse("Should not find attribute with prefix when query prefix is null", iter.setPosition(1));
    }

    // Tests constructor with wildcard where some attributes have namespace -> only non-namespace attributes are matched (current behavior)
    @Test
    public void testConstructor_wildcardOnlyMatchesNonNamespaceAttributes() throws Exception {
        Document document = docBuilder.newDocument();
        Element element = document.createElementNS(null, "root");
        element.setAttribute("nonns", "1");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:b", "http://ns2");
        Attr nsAttr = document.createAttributeNS("http://ns2", "b:attr");
        nsAttr.setValue("2");
        element.setAttributeNodeNS(nsAttr);
        DOMNodePointer parent = new DOMNodePointer(element, null);
        QName name = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, name);
        // Should only contain "nonns" because namespace attributes are excluded by testAttr for wildcard
        int count = 0;
        while (iter.setPosition(count + 1)) {
            count++;
        }
        assertEquals("Wildcard should only include non-namespace attributes", 1, count);
    }
}
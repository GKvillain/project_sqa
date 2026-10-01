package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMAttributeIteratorTest {

    private Element element;
    private NodePointer elementPointer;

    @Before
    public void setUp() {
        element = new Element("root");
        elementPointer = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
    }

    // Tests iterating over an existing single attribute with no namespace
    @Test
    public void testConstructor_singleAttribute_success() {
        element.setAttribute("attr1", "val1");
        QName qname = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());
        assertEquals("val1", iterator.getNodePointer().getValue());
        assertFalse(iterator.setPosition(2));
    }

    // Tests query for an attribute that does not exist
    @Test
    public void testConstructor_nonExistingAttribute_emptyList() {
        element.setAttribute("attr1", "val1");
        QName qname = new QName("attr2");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertFalse(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests wildcard attribute matching with no namespace
    @Test
    public void testConstructor_wildcardMatching_findsAllMatchingAttributes() {
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        QName qname = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertFalse(iterator.setPosition(3));
    }

    // Tests attribute with XML prefix (xml:lang / xml namespace)
    @Test
    public void testConstructor_xmlPrefix_findsXmlAttribute() {
        Attribute xmlAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(xmlAttr);

        QName qname = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals("en", iterator.getNodePointer().getValue());
    }

    // Tests attribute wildcard with XML prefix
    @Test
    public void testConstructor_xmlPrefixWildcard_findsXmlAttributes() {
        Attribute xmlAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(xmlAttr);

        QName qname = new QName("xml", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals("en", iterator.getNodePointer().getValue());
        assertFalse(iterator.setPosition(2));
    }

    // Tests attribute with custom defined namespace
    @Test
    public void testConstructor_customNamespace_findsAttribute() {
        Namespace ns = Namespace.getNamespace("custom", "http://commons.apache.org/test");
        element.addNamespaceDeclaration(ns);
        element.setAttribute(new Attribute("name", "testVal", ns));

        elementPointer = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());

        QName qname = new QName("custom", "name");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals("testVal", iterator.getNodePointer().getValue());
    }

    // Tests undefined namespace prefix
    @Test
    public void testConstructor_unknownNamespacePrefix_emptyIterator() {
        QName qname = new QName("unknown", "attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // Tests non-Element parent pointer (e.g. Document node)
    @Test
    public void testConstructor_nonElementParent_emptyAttributes() {
        Document doc = new Document(new Element("root"));
        NodePointer docPointer = NodePointer.newNodePointer(new QName("doc"), doc, Locale.getDefault());

        QName qname = new QName("attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(docPointer, qname);

        assertFalse(iterator.setPosition(1));
        assertEquals(0, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    // Tests setPosition with out of bound lower and upper limits
    @Test
    public void testSetPosition_boundaryValues_correctReturn() {
        element.setAttribute("attr1", "val1");
        QName qname = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(-1));
        assertTrue(iterator.setPosition(1));
        assertFalse(iterator.setPosition(2));
    }

    // Tests getNodePointer when position is initially 0
    @Test
    public void testGetNodePointer_positionZero_advancesAndResetsPosition() {
        element.setAttribute("attr1", "val1");
        QName qname = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertEquals(0, iterator.getPosition());
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer when position is set to valid index
    @Test
    public void testGetNodePointer_afterSetPosition_returnsPointer() {
        element.setAttribute("attr1", "val1");
        QName qname = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        iterator.setPosition(1);
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("val1", np.getValue());
    }

    // Tests wildcard iteration with mixed namespaces
    @Test
    public void testConstructor_wildcardWithNamespace_onlyMatchesSameNamespace() {
        Namespace ns = Namespace.getNamespace("custom", "http://commons.apache.org/test");
        element.addNamespaceDeclaration(ns);
        element.setAttribute("attrNoNs", "valNoNs");
        element.setAttribute(new Attribute("attrNs", "valNs", ns));

        elementPointer = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());

        QName qnameNoNs = new QName("*");
        JDOMAttributeIterator iterNoNs = new JDOMAttributeIterator(elementPointer, qnameNoNs);
        assertTrue(iterNoNs.setPosition(1));
        assertEquals("valNoNs", iterNoNs.getNodePointer().getValue());
        assertFalse(iterNoNs.setPosition(2));

        QName qnameNs = new QName("custom", "*");
        JDOMAttributeIterator iterNs = new JDOMAttributeIterator(elementPointer, qnameNs);
        assertTrue(iterNs.setPosition(1));
        assertEquals("valNs", iterNs.getNodePointer().getValue());
        assertFalse(iterNs.setPosition(2));
    }

    // Tests wildcard query with unknown namespace prefix matching all attributes
    @Test
    public void testConstructor_unknownNamespaceWildcard_matchesAllAttributes() {
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");

        QName qname = new QName("unknown", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertTrue(iterator.setPosition(1));
        assertEquals("val1", iterator.getNodePointer().getValue());
        assertTrue(iterator.setPosition(2));
        assertEquals("val2", iterator.getNodePointer().getValue());
        assertFalse(iterator.setPosition(3));
    }

    // Tests getNodePointer directly at position 0 when attributes list is empty
    @Test
    public void testGetNodePointer_emptyAttributesAtPositionZero_returnsNull() {
        QName qname = new QName("nonExistent");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(elementPointer, qname);

        assertEquals(0, iterator.getPosition());
        assertNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    // Tests getNodePointer for non-element parent when position is manually set
    @Test
    public void testGetNodePointer_nonElementParentWithNonZeroPosition_returnsNull() {
        Document doc = new Document(new Element("root"));
        NodePointer docPointer = NodePointer.newNodePointer(new QName("doc"), doc, Locale.getDefault());

        QName qname = new QName("attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(docPointer, qname);

        iterator.setPosition(5);
        assertNull(iterator.getNodePointer());
    }
}
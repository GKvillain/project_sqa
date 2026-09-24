package org.apache.commons.jxpath.ri.axes;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
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

public class AttributeContextTest {

    private JXPathContextReferenceImpl context;
    private InitialContext initialContext;
    private Element rootElement;

    @Before
    public void setUp() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        rootElement = doc.createElement("root");
        rootElement.setAttribute("attr1", "value1");
        rootElement.setAttribute("attr2", "value2");
        doc.appendChild(rootElement);

        context = (JXPathContextReferenceImpl) JXPathContext.newContext(doc);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), rootElement, Locale.getDefault());
        RootContext rootContext = new RootContext(context, rootPointer);
        initialContext = new InitialContext(rootContext);
    }

    // Tests initial state of getCurrentNodePointer
    @Test
    public void testGetCurrentNodePointer_initialState_returnsNull() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        assertNull(attributeContext.getCurrentNodePointer());
        assertEquals(0, attributeContext.getPosition());
    }

    // Tests nextNode with matching attribute name
    @Test
    public void testNextNode_matchingAttribute_returnsTrueAndSetsPointer() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        boolean result = attributeContext.nextNode();

        assertTrue(result);
        assertNotNull(attributeContext.getCurrentNodePointer());
        assertEquals("value1", attributeContext.getCurrentNodePointer().getValue());
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests nextNode advancing past available attributes
    @Test
    public void testNextNode_afterLastAttribute_returnsFalse() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        assertTrue(attributeContext.nextNode());
        assertFalse(attributeContext.nextNode());
        assertEquals(2, attributeContext.getPosition());
    }

    // Tests nextNode with non-existent attribute name
    @Test
    public void testNextNode_nonExistentAttribute_returnsFalse() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("nonExistent"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        boolean result = attributeContext.nextNode();

        assertFalse(result);
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests nextNode with null nodeTest
    @Test
    public void testNextNode_nullNodeTest_returnsFalse() {
        AttributeContext attributeContext = new AttributeContext(initialContext, null);

        boolean result = attributeContext.nextNode();

        assertFalse(result);
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests nextNode when nodeTest is NodeTypeTest instead of NodeNameTest
    @Test
    public void testNextNode_nodeTypeTest_returnsFalse() {
        NodeTypeTest nodeTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTypeTest);

        boolean result = attributeContext.nextNode();

        assertFalse(result);
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests nextNode with wildcard node name
    @Test
    public void testNextNode_wildcardAttribute_iteratesMultipleAttributes() {
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext attributeContext = new AttributeContext(initialContext, wildcardTest);

        assertTrue(attributeContext.nextNode());
        assertEquals(1, attributeContext.getPosition());

        assertTrue(attributeContext.nextNode());
        assertEquals(2, attributeContext.getPosition());

        assertFalse(attributeContext.nextNode());
    }

    // Tests reset method resetting position and state
    @Test
    public void testReset_afterIteration_resetsPositionAndState() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        assertTrue(attributeContext.nextNode());
        assertEquals(1, attributeContext.getPosition());

        attributeContext.reset();
        assertEquals(0, attributeContext.getPosition());

        assertTrue(attributeContext.nextNode());
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests setPosition forward navigation
    @Test
    public void testSetPosition_validPosition_returnsTrue() {
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext attributeContext = new AttributeContext(initialContext, wildcardTest);

        boolean result = attributeContext.setPosition(2);

        assertTrue(result);
        assertEquals(2, attributeContext.getPosition());
        assertNotNull(attributeContext.getCurrentNodePointer());
    }

    // Tests setPosition beyond available count
    @Test
    public void testSetPosition_outOfBounds_returnsFalse() {
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext attributeContext = new AttributeContext(initialContext, wildcardTest);

        boolean result = attributeContext.setPosition(5);

        assertFalse(result);
        assertEquals(3, attributeContext.getPosition());
    }

    // Tests setPosition backward navigation triggering reset
    @Test
    public void testSetPosition_backtrackPosition_resetsAndNavigates() {
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext attributeContext = new AttributeContext(initialContext, wildcardTest);

        assertTrue(attributeContext.setPosition(2));
        assertEquals(2, attributeContext.getPosition());

        boolean result = attributeContext.setPosition(1);

        assertTrue(result);
        assertEquals(1, attributeContext.getPosition());
    }

    // Tests setPosition to zero
    @Test
    public void testSetPosition_zeroPosition_returnsTrueAndResets() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        assertTrue(attributeContext.nextNode());
        assertEquals(1, attributeContext.getPosition());

        boolean result = attributeContext.setPosition(0);

        assertTrue(result);
        assertEquals(0, attributeContext.getPosition());
    }

    // Tests setPosition with negative value
    @Test
    public void testSetPosition_negativePosition_returnsTrueAndResetsToZero() {
        NodeNameTest nodeTest = new NodeNameTest(new QName("attr1"));
        AttributeContext attributeContext = new AttributeContext(initialContext, nodeTest);

        assertTrue(attributeContext.nextNode());
        assertEquals(1, attributeContext.getPosition());

        boolean result = attributeContext.setPosition(-1);

        assertTrue(result);
        assertEquals(0, attributeContext.getPosition());
    }
}
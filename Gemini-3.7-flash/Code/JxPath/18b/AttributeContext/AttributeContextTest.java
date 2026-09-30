package org.apache.commons.jxpath.ri.axes;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
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

    private InitialContext parentContext;
    private Element element;

    @Before
    public void setUp() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        element = doc.createElement("root");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        doc.appendChild(element);

        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        RootContext rootContext = new RootContext(null, pointer);
        parentContext = new InitialContext(rootContext);
    }

    // Tests initial state before iteration
    @Test
    public void testGetCurrentNodePointer_initiallyNull() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName("attr1"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertNull(context.getCurrentNodePointer());
        assertEquals(0, context.getPosition());
    }

    // Tests matching a specific attribute by name
    @Test
    public void testNextNode_specificAttributeName_returnsTrue() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName("attr1"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.nextNode());
        assertNotNull(context.getCurrentNodePointer());
        assertEquals(1, context.getPosition());

        assertFalse(context.nextNode());
    }

    // Tests non-existent attribute name
    @Test
    public void testNextNode_nonExistentAttribute_returnsFalse() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName("nonExistent"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests wildcard attribute name test matching all attributes
    @Test
    public void testNextNode_wildcardAttribute_matchesMultiple() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.nextNode());
        assertEquals(1, context.getPosition());
        assertNotNull(context.getCurrentNodePointer());

        assertTrue(context.nextNode());
        assertEquals(2, context.getPosition());
        assertNotNull(context.getCurrentNodePointer());

        assertFalse(context.nextNode());
    }

    // Tests node type test for node() type on attribute axis
    @Test
    public void testNextNode_nodeTypeTestNode_matchesAttributes() {
        NodeTypeTest nodeTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        AttributeContext context = new AttributeContext(parentContext, nodeTypeTest);

        assertTrue(context.nextNode());
        assertNotNull(context.getCurrentNodePointer());
    }

    // Tests unsupported node type test returns false
    @Test
    public void testNextNode_unsupportedNodeTypeTest_returnsFalse() {
        NodeTypeTest nodeTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        AttributeContext context = new AttributeContext(parentContext, nodeTypeTest);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests null NodeTest handling
    @Test
    public void testNextNode_nullNodeTest_returnsFalse() {
        AttributeContext context = new AttributeContext(parentContext, null);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests parent context returning null pointer
    @Test
    public void testNextNode_nullParentPointer_returnsFalse() {
        NullPointer nullPointer = new NullPointer(Locale.getDefault(), "id");
        RootContext rootContext = new RootContext(null, nullPointer);
        InitialContext parent = new InitialContext(rootContext);

        NodeNameTest nodeNameTest = new NodeNameTest(new QName("attr1"));
        AttributeContext context = new AttributeContext(parent, nodeNameTest);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests setPosition forward navigation
    @Test
    public void testSetPosition_forwardValid_returnsTrue() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.setPosition(2));
        assertEquals(2, context.getPosition());
        assertNotNull(context.getCurrentNodePointer());
    }

    // Tests setPosition out of bounds
    @Test
    public void testSetPosition_outOfBounds_returnsFalse() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertFalse(context.setPosition(5));
        assertEquals(3, context.getPosition());
    }

    // Tests setPosition backwards navigation causing reset
    @Test
    public void testSetPosition_backwards_resetsAndPositionsCorrectly() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.setPosition(2));
        assertEquals(2, context.getPosition());

        assertTrue(context.setPosition(1));
        assertEquals(1, context.getPosition());
    }

    // Tests setPosition to zero or negative position
    @Test
    public void testSetPosition_zeroOrNegative_resets() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        context.setPosition(1);
        assertTrue(context.setPosition(0));
        assertEquals(0, context.getPosition());
    }

    // Tests reset method resets state and position
    @Test
    public void testReset_clearsState() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.nextNode());
        assertEquals(1, context.getPosition());

        context.reset();
        assertEquals(0, context.getPosition());

        assertTrue(context.nextNode());
        assertEquals(1, context.getPosition());
    }

    // Tests processing instruction test on attribute context returns false
    @Test
    public void testNextNode_processingInstructionTest_returnsFalse() {
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        AttributeContext context = new AttributeContext(parentContext, piTest);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests element with no attributes using wildcard search
    @Test
    public void testNextNode_emptyAttributes_returnsFalse() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element emptyElement = doc.createElement("empty");
        doc.appendChild(emptyElement);

        NodePointer pointer = NodePointer.newNodePointer(new QName("empty"), emptyElement, Locale.getDefault());
        RootContext rootContext = new RootContext(null, pointer);
        InitialContext emptyParentContext = new InitialContext(rootContext);

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(emptyParentContext, wildcardTest);

        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests setPosition when target position is equal to current position
    @Test
    public void testSetPosition_samePosition_returnsTrue() {
        NodeNameTest nodeNameTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parentContext, nodeNameTest);

        assertTrue(context.setPosition(1));
        assertEquals(1, context.getPosition());

        assertTrue(context.setPosition(1));
        assertEquals(1, context.getPosition());
    }
}
package org.apache.commons.jxpath.ri.axes;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.EvalContext;

public class AttributeContextTest {

    private EvalContext parentContext;
    private NodePointer parentPointer;

    // A minimal concrete implementation of NodePointer for testing
    // This is a common practice in JxPath tests; it's not a mock but a stub
    // that provides minimal necessary functionality.
    private static class TestNodePointer extends NodePointer {
        private static final long serialVersionUID = 1L;
        private NodeIterator iterator;

        TestNodePointer(NodeIterator iterator) {
            super(null);
            this.iterator = iterator;
        }

        @Override
        public NodeIterator attributeIterator(QName name) {
            return iterator;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public Object getBaseValue() {
            return null;
        }

        @Override
        public Object getImmediateNode() {
            return null;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        @Override
        public String asPath() {
            return "/test";
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public boolean testNode(NodeTest test) {
            return false;
        }

        @Override
        public QName getName() {
            return null;
        }
    }

    // A simple NodeIterator that returns one node pointer
    private static class SingleNodeIterator extends NodePointer implements NodeIterator {
        private static final long serialVersionUID = 1L;
        private boolean called = false;
        private int position = 0;
        private NodePointer pointer;

        SingleNodeIterator(NodePointer pointer) {
            super(null);
            this.pointer = pointer;
        }

        @Override
        public NodePointer getNodePointer() {
            return pointer;
        }

        @Override
        public boolean setPosition(int position) {
            if (position == 1 && !called) {
                called = true;
                this.position = position;
                return true;
            }
            return false;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public Object getBaseValue() {
            return null;
        }

        @Override
        public Object getImmediateNode() {
            return null;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        @Override
        public String asPath() {
            return "/test";
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public boolean testNode(NodeTest test) {
            return false;
        }

        @Override
        public QName getName() {
            return null;
        }
    }

    @Before
    public void setUp() {
        // Create a minimal EvalContext as parent that returns a test NodePointer
        parentContext = new EvalContext(null) {
            @Override
            public NodePointer getCurrentNodePointer() {
                return parentPointer;
            }

            @Override
            public boolean setPosition(int position) {
                return true;
            }

            @Override
            public boolean nextNode() {
                return false;
            }
        };
    }

    // Tests nextNode() with NodeNameTest and valid iterator => returns true
    @Test
    public void testNextNode_NodeNameTestWithValidIterator_ReturnsTrue() {
        NodePointer attrPointer = new TestNodePointer(null);
        NodeIterator iterator = new SingleNodeIterator(attrPointer);
        parentPointer = new TestNodePointer(iterator);
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertTrue(context.nextNode());
        assertNotNull(context.getCurrentNodePointer());
    }

    // Tests nextNode() with NodeNameTest and null iterator => returns false
    @Test
    public void testNextNode_NodeNameTestWithNullIterator_ReturnsFalse() {
        parentPointer = new TestNodePointer(null); // returns null iterator
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests nextNode() with non-NodeNameTest => returns false
    @Test
    public void testNextNode_NonNodeNameTest_ReturnsFalse() {
        NodeTest test = new NodeTest() {
            // anonymous subclass is necessary for this test; it's a minimal valid NodeTest
            // NodeTest is an abstract class that doesn't require any methods to be implemented
        };
        AttributeContext context = new AttributeContext(parentContext, test);
        assertFalse(context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // Tests nextNode() called twice, second call should fail (iterator exhausted)
    @Test
    public void testNextNode_SecondCall_ReturnsFalse() {
        NodePointer attrPointer = new TestNodePointer(null);
        NodeIterator iterator = new SingleNodeIterator(attrPointer);
        parentPointer = new TestNodePointer(iterator);
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertTrue(context.nextNode());
        assertFalse(context.nextNode());
    }

    // Tests setPosition with position less than current position triggers reset
    @Test
    public void testSetPosition_PositionLessThanCurrent_ResetsAndMovesToNewPosition() {
        NodePointer attrPointer = new TestNodePointer(null);
        NodeIterator iterator = new SingleNodeIterator(attrPointer);
        parentPointer = new TestNodePointer(iterator);
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        context.nextNode();
        // now at position 1
        assertTrue(context.setPosition(2));
        assertFalse(context.setPosition(1));
    }

    // Tests setPosition with position equal to current => should succeed
    @Test
    public void testSetPosition_WhenPositionAlreadyReached_ReturnsTrue() {
        NodePointer attrPointer = new TestNodePointer(null);
        NodeIterator iterator = new SingleNodeIterator(attrPointer);
        parentPointer = new TestNodePointer(iterator);
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        context.nextNode();
        assertTrue(context.setPosition(1));
    }

    // Tests reset after nextNode sets started to true
    @Test
    public void testReset_AfterNextNode_ResetsState() {
        NodePointer attrPointer = new TestNodePointer(null);
        NodeIterator iterator = new SingleNodeIterator(attrPointer);
        parentPointer = new TestNodePointer(iterator);
        NodeTest test = new NodeNameTest(new QName("attrName"));
        AttributeContext context = new AttributeContext(parentContext, test);
        context.nextNode();
        context.reset();
        assertNull(context.getCurrentNodePointer());
        // After reset, nextNode should work again
        assertTrue(context.nextNode());
    }

    // Tests getCurrentNodePointer initially returns null
    @Test
    public void testGetCurrentNodePointer_Initially_ReturnsNull() {
        NodeTest test = new NodeNameTest(new QName("test"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertNull(context.getCurrentNodePointer());
    }

    // Tests constructor
    @Test
    public void testConstructor_WithValidInputs_ShouldNotThrow() {
        NodeTest test = new NodeNameTest(new QName("test"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertNotNull(context);
    }

    // Tests setPosition with position 0 (boundary) => returns false
    @Test
    public void testSetPosition_PositionZero_ReturnsFalse() {
        NodeTest test = new NodeNameTest(new QName("test"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertFalse(context.setPosition(0));
    }

    // Tests setPosition with negative position (invalid) => returns false
    @Test
    public void testSetPosition_NegativePosition_ReturnsFalse() {
        NodeTest test = new NodeNameTest(new QName("test"));
        AttributeContext context = new AttributeContext(parentContext, test);
        assertFalse(context.setPosition(-1));
    }
}
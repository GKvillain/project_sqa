package org.apache.commons.jxpath.ri.axes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UnionContextTest {

    private JXPathContextReferenceImpl contextRef;
    private RootContext rootContext;

    @Before
    public void setUp() {
        contextRef = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(null, "root", Locale.getDefault());
        rootContext = new RootContext(contextRef, rootPointer);
    }

    // Tests initialization and getNodeSet not null
    @Test
    public void testConstructor_validArguments_initializesProperly() {
        EvalContext[] contexts = new EvalContext[0];
        UnionContext unionContext = new UnionContext(rootContext, contexts);
        assertNotNull(unionContext.getNodeSet());
        assertEquals(0, unionContext.getPosition());
    }

    // Tests getDocumentOrder when multiple contexts are supplied
    @Test
    public void testGetDocumentOrder_multipleContexts_returnsOne() {
        EvalContext ctx1 = new InitialContext(rootContext);
        EvalContext ctx2 = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx1, ctx2 });
        assertEquals(1, unionContext.getDocumentOrder());
    }

    // Tests getDocumentOrder when a single context is supplied
    @Test
    public void testGetDocumentOrder_singleContext_returnsSuperDocumentOrder() {
        EvalContext ctx1 = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx1 });
        assertEquals(0, unionContext.getDocumentOrder());
    }

    // Tests getDocumentOrder when empty contexts array is supplied
    @Test
    public void testGetDocumentOrder_emptyContexts_returnsSuperDocumentOrder() {
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[0]);
        assertEquals(0, unionContext.getDocumentOrder());
    }

    // Tests setPosition on empty contexts array
    @Test
    public void testSetPosition_emptyContexts_returnsFalse() {
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[0]);
        boolean result = unionContext.setPosition(1);
        assertFalse(result);
    }

    // Tests setPosition with single context containing one element
    @Test
    public void testSetPosition_singleContext_returnsTrueForValidPosition() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });
        boolean result = unionContext.setPosition(1);
        assertTrue(result);
        assertEquals(1, unionContext.getPosition());
        assertNotNull(unionContext.getCurrentNodePointer());
    }

    // Tests setPosition out of bounds
    @Test
    public void testSetPosition_outOfBounds_returnsFalse() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });
        boolean result = unionContext.setPosition(2);
        assertFalse(result);
    }

    // Tests setPosition with zero position
    @Test
    public void testSetPosition_zeroPosition_returnsTrue() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });
        boolean result = unionContext.setPosition(0);
        assertTrue(result);
        assertEquals(0, unionContext.getPosition());
    }

    // Tests setPosition with negative position
    @Test
    public void testSetPosition_negativePosition_returnsFalse() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });
        boolean result = unionContext.setPosition(-1);
        assertFalse(result);
    }

    // Tests deduplication when multiple contexts return identical node pointers
    @Test
    public void testSetPosition_duplicateNodes_deduplicatesCorrectly() {
        EvalContext ctx1 = new InitialContext(rootContext);
        EvalContext ctx2 = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx1, ctx2 });

        assertTrue(unionContext.setPosition(1));
        assertFalse(unionContext.setPosition(2));
        assertEquals(1, unionContext.getNodeSet().getPointers().size());
    }

    // Tests repeated calls to setPosition to ensure prepared flag prevents re-evaluation
    @Test
    public void testSetPosition_multipleInvocations_preparesOnlyOnce() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });

        assertTrue(unionContext.setPosition(1));
        BasicNodeSet nodeSetFirst = (BasicNodeSet) unionContext.getNodeSet();
        int sizeFirst = nodeSetFirst.getPointers().size();

        assertTrue(unionContext.setPosition(1));
        BasicNodeSet nodeSetSecond = (BasicNodeSet) unionContext.getNodeSet();
        assertEquals(sizeFirst, nodeSetSecond.getPointers().size());
    }

    // Tests nextNode iteration through UnionContext
    @Test
    public void testNextNode_singleNodeContext_iteratesCorrectly() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });

        assertTrue(unionContext.nextNode());
        assertEquals(1, unionContext.getPosition());
        assertNotNull(unionContext.getCurrentNodePointer());
        assertFalse(unionContext.nextNode());
    }

    // Tests nextSet behavior on UnionContext
    @Test
    public void testNextSet_multipleInvocations_returnsTrueThenFalse() {
        EvalContext ctx = new InitialContext(rootContext);
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[] { ctx });

        assertTrue(unionContext.nextSet());
        assertFalse(unionContext.nextSet());
    }

    // Tests union operation with ChildContext instances containing multiple children
    @Test
    public void testSetPosition_multipleChildrenContexts_accumulatesAllNodes() {
        String[] data = new String[] { "a", "b", "c" };
        JXPathContextReferenceImpl contextWithList = (JXPathContextReferenceImpl) JXPathContext.newContext(data);
        NodePointer listPointer = NodePointer.newNodePointer(null, data, Locale.getDefault());
        RootContext listRootContext = new RootContext(contextWithList, listPointer);

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        ChildContext childCtx1 = new ChildContext(listRootContext, nodeTest, false, false);
        ChildContext childCtx2 = new ChildContext(listRootContext, nodeTest, false, false);

        UnionContext unionContext = new UnionContext(listRootContext, new EvalContext[] { childCtx1, childCtx2 });

        assertTrue(unionContext.setPosition(1));
        assertEquals(3, unionContext.getNodeSet().getPointers().size());
        assertTrue(unionContext.setPosition(3));
        assertFalse(unionContext.setPosition(4));
    }

    // Tests integration via XPath union expression query
    @Test
    public void testXPathUnionExpression_evaluatesCorrectUnion() {
        TestBean bean = new TestBean();
        JXPathContext context = JXPathContext.newContext(bean);

        Iterator iterator = context.iteratePointers("items[1] | items[2]");
        int count = 0;
        while (iterator.hasNext()) {
            Pointer ptr = (Pointer) iterator.next();
            assertNotNull(ptr);
            count++;
        }
        assertEquals(2, count);
    }

    public static class TestBean {
        private String[] items = new String[] { "item1", "item2", "item3" };

        public String[] getItems() {
            return items;
        }

        public void setItems(String[] items) {
            this.items = items;
        }
    }
}
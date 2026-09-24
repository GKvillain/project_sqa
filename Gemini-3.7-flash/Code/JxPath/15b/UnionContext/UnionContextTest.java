package org.apache.commons.jxpath.ri.axes;

import java.util.Locale;
import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnionContextTest {

    // Tests getDocumentOrder when multiple contexts are supplied
    @Test
    public void testGetDocumentOrder_multipleContexts_returnsOne() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx1 = new InitialContext(rootContext);
        InitialContext initCtx2 = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx1, initCtx2 };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertEquals(1, unionContext.getDocumentOrder());
    }

    // Tests getDocumentOrder when a single context is supplied
    @Test
    public void testGetDocumentOrder_singleContext_returnsSuperDocumentOrder() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertEquals(0, unionContext.getDocumentOrder());
    }

    // Tests getDocumentOrder with empty contexts array
    @Test
    public void testGetDocumentOrder_emptyContexts_returnsSuperDocumentOrder() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));

        EvalContext[] contexts = new EvalContext[0];
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertEquals(0, unionContext.getDocumentOrder());
    }

    // Tests setPosition with empty contexts array
    @Test
    public void testSetPosition_emptyContexts_returnsFalse() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));

        EvalContext[] contexts = new EvalContext[0];
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        boolean result = unionContext.setPosition(1);
        assertFalse(result);
    }

    // Tests setPosition on valid node contexts with position 1
    @Test
    public void testSetPosition_singleContextValidPosition_returnsTrue() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        boolean result = unionContext.setPosition(1);
        assertTrue(result);
        assertEquals(1, unionContext.getPosition());
        assertNotNull(unionContext.getCurrentNodePointer());
    }

    // Tests setPosition out of bounds (position larger than number of nodes)
    @Test
    public void testSetPosition_positionOutOfBounds_returnsFalse() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        boolean result = unionContext.setPosition(2);
        assertFalse(result);
    }

    // Tests setPosition with duplicate pointers across contexts to verify uniqueness
    @Test
    public void testSetPosition_duplicatePointersAcrossContexts_deduplicatesNodes() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx1 = new InitialContext(rootContext);
        InitialContext initCtx2 = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx1, initCtx2 };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertTrue(unionContext.setPosition(1));
        assertFalse(unionContext.setPosition(2));
        assertEquals(1, unionContext.getNodeSet().getPointers().size());
    }

    // Tests setPosition called multiple times to verify prepared branch logic
    @Test
    public void testSetPosition_calledMultipleTimes_preparedFlagPreventsReprocessing() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertTrue(unionContext.setPosition(1));
        assertTrue(unionContext.setPosition(1));
        assertEquals(1, unionContext.getNodeSet().getPointers().size());
    }

    // Tests setPosition with zero position
    @Test
    public void testSetPosition_zeroPosition_returnsFalse() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        boolean result = unionContext.setPosition(0);
        assertFalse(result);
        assertEquals(0, unionContext.getPosition());
    }

    // Tests setPosition with negative position
    @Test
    public void testSetPosition_negativePosition_returnsFalse() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));
        InitialContext initCtx = new InitialContext(rootContext);

        EvalContext[] contexts = new EvalContext[] { initCtx };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        boolean result = unionContext.setPosition(-1);
        assertFalse(result);
    }

    // Tests setPosition when context contains multiple child nodes
    @Test
    public void testSetPosition_multipleDistinctNodes_iteratesAllNodes() {
        String[] data = new String[] { "node1", "node2" };
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, data, null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), data, Locale.getDefault()));
        
        NodeTypeTest nodeTypeTest = new NodeTypeTest(1);
        ChildContext childContext = new ChildContext(rootContext, nodeTypeTest, true, false);

        EvalContext[] contexts = new EvalContext[] { childContext };
        UnionContext unionContext = new UnionContext(rootContext, contexts);

        assertTrue(unionContext.setPosition(1));
        assertNotNull(unionContext.getCurrentNodePointer());
        assertTrue(unionContext.nextSet());
    }

    // Tests NullPointerException when contexts array is null during setPosition
    @Test(expected = NullPointerException.class)
    public void testSetPosition_nullContextsArray_throwsNullPointerException() {
        JXPathContextReferenceImpl context = new JXPathContextReferenceImpl(null, "root", null);
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(new QName("test"), "root", Locale.getDefault()));

        UnionContext unionContext = new UnionContext(rootContext, null);
        unionContext.setPosition(1);
    }
}
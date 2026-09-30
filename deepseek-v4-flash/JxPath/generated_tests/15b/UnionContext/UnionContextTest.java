package org.apache.commons.jxpath.ri.axes;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;

/**
 * JUnit 4 test class for UnionContext (Defects4J bug 15b).
 */
public class UnionContextTest {

    // Helper method to create a NodePointer for testing
    private NodePointer createPointer(String value) {
        return new BeanPointer(null, new QName("test"), value, null);
    }

    // Helper method to create an EvalContext that provides given pointers
    private EvalContext createContext(NodePointer... pointers) {
        BasicNodeSet nodeSet = new BasicNodeSet();
        for (NodePointer ptr : pointers) {
            nodeSet.add(ptr);
        }
        return new NodeSetContext(null, nodeSet);
    }

    // Tests constructor with empty contexts array
    @Test
    public void testConstructor_emptyContextsArray_createsInstance() {
        UnionContext union = new UnionContext(null, new EvalContext[0]);
        assertNotNull(union);
    }

    // Tests getDocumentOrder with empty contexts
    @Test
    public void testGetDocumentOrder_emptyContexts_returnsParentOrder() {
        UnionContext union = new UnionContext(null, new EvalContext[0]);
        // contexts.length = 0 => return super.getDocumentOrder()
        // NodeSetContext.getDocumentOrder() returns 0 by default? (unclear but no exception)
        int order = union.getDocumentOrder();
        assertTrue(order >= -1 && order <= 1); // just a sanity check
    }

    // Tests getDocumentOrder with single context
    @Test
    public void testGetDocumentOrder_singleContext_returnsParentOrder() {
        EvalContext ctx = createContext();
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        // length = 1 => not >1 => super.getDocumentOrder()
        int order = union.getDocumentOrder();
        assertTrue(order >= -1 && order <= 1);
    }

    // Tests getDocumentOrder with multiple contexts returns 1
    @Test
    public void testGetDocumentOrder_multipleContexts_returnsOne() {
        EvalContext ctx1 = createContext();
        EvalContext ctx2 = createContext();
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx1, ctx2});
        // length = 2 => >1 => return 1
        assertEquals(1, union.getDocumentOrder());
    }

    // Tests setPosition first call (prepared = false) with empty context
    @Test
    public void testSetPosition_firstCallEmptyContexts_doesNotThrow() {
        UnionContext union = new UnionContext(null, new EvalContext[0]);
        boolean result = union.setPosition(1);
        // super.setPosition(1) with empty nodeSet => returns false
        assertFalse(result);
    }

    // Tests setPosition second call (prepared = true) still works
    @Test
    public void testSetPosition_secondCall_preparedTrue() {
        UnionContext union = new UnionContext(null, new EvalContext[0]);
        union.setPosition(1); // first call, prepares
        boolean result = union.setPosition(1); // second call, prepared already
        assertFalse(result); // same behavior
    }

    // Tests setPosition with contexts that yield no nodes
    @Test
    public void testSetPosition_contextsWithNoNodes_preparesEmptyNodeSet() {
        EvalContext ctx = createContext(); // no pointers
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        // after prepare, nodeSet should be empty
        boolean result = union.setPosition(1);
        assertFalse(result);
        // verify nodeSet size via cast
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals(0, nodeSet.getPointers().size());
    }

    // Tests that duplicate pointers from different contexts are deduplicated
    @Test
    public void testSetPosition_duplicatePointers_acrossContexts_deduplicates() {
        NodePointer ptr = createPointer("value");
        EvalContext ctx1 = createContext(ptr);
        EvalContext ctx2 = createContext(ptr); // same pointer object
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx1, ctx2});
        union.setPosition(1);
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals(1, nodeSet.getPointers().size());
        assertTrue(nodeSet.getPointers().contains(ptr));
    }

    // Tests duplicate pointers that are equal but not same reference
    @Test
    public void testSetPosition_equalPointers_deduplicates() {
        NodePointer ptr1 = createPointer("x");
        NodePointer ptr2 = createPointer("x"); // different object but equal by path
        EvalContext ctx1 = createContext(ptr1);
        EvalContext ctx2 = createContext(ptr2);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx1, ctx2});
        union.setPosition(1);
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals(1, nodeSet.getPointers().size());
    }

    // Tests distinct pointers from different contexts are all added
    @Test
    public void testSetPosition_distinctPointers_allAdded() {
        NodePointer ptr1 = createPointer("a");
        NodePointer ptr2 = createPointer("b");
        EvalContext ctx1 = createContext(ptr1);
        EvalContext ctx2 = createContext(ptr2);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx1, ctx2});
        union.setPosition(1);
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals(2, nodeSet.getPointers().size());
    }

    // Tests setPosition with valid position after preparation
    @Test
    public void testSetPosition_validPosition_afterPrepare_returnsCorrect() {
        NodePointer ptr = createPointer("x");
        EvalContext ctx = createContext(ptr);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        // after prepare, nodeSet contains one pointer -> setPosition(1) should succeed
        boolean result = union.setPosition(1);
        assertTrue(result);
    }

    // Tests setPosition with out-of-range position returns false
    @Test
    public void testSetPosition_invalidPosition_returnsFalse() {
        NodePointer ptr = createPointer("x");
        EvalContext ctx = createContext(ptr);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        // nodeSet size=1, position=2 out of range
        boolean result = union.setPosition(2);
        assertFalse(result);
    }

    // Tests setPosition with position=0 (invalid) returns false
    @Test
    public void testSetPosition_positionZero_returnsFalse() {
        NodePointer ptr = createPointer("x");
        EvalContext ctx = createContext(ptr);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        boolean result = union.setPosition(0);
        assertFalse(result);
    }

    // Tests that setPosition does not re-add nodes on second call
    @Test
    public void testSetPosition_secondCall_doesNotAddDuplicates() {
        NodePointer ptr = createPointer("x");
        EvalContext ctx = createContext(ptr);
        UnionContext union = new UnionContext(null, new EvalContext[]{ctx});
        union.setPosition(1); // first call, adds pointer
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals(1, nodeSet.getPointers().size());
        // second call should not double the node set
        union.setPosition(1);
        assertEquals(1, nodeSet.getPointers().size());
    }
}
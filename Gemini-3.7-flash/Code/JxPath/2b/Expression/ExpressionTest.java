package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class ExpressionTest {

    private static class MockExpression extends Expression {
        private final boolean contextDependentValue;
        private int computeContextDependentCalls = 0;
        private Object computeResult;

        MockExpression(boolean contextDependentValue, Object computeResult) {
            this.contextDependentValue = contextDependentValue;
            this.computeResult = computeResult;
        }

        public boolean computeContextDependent() {
            computeContextDependentCalls++;
            return contextDependentValue;
        }

        public Object computeValue(EvalContext context) {
            return computeResult;
        }

        public Object compute(EvalContext context) {
            return computeResult;
        }
    }

    private static class DummyPointer implements Pointer {
        private final Object value;

        DummyPointer(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public Object getNode() {
            return value;
        }

        public void setValue(Object value) {
        }

        public Object getRootNode() {
            return value;
        }

        public int compareTo(Object o) {
            return 0;
        }

        public Object clone() {
            return this;
        }

        public String asPath() {
            return "/dummy";
        }
    }

    // Tests static constants defined on Expression
    @Test
    public void testConstants_validValues() {
        assertEquals(0.0, Expression.ZERO.doubleValue(), 0.0);
        assertEquals(1.0, Expression.ONE.doubleValue(), 0.0);
        assertTrue(Double.isNaN(Expression.NOT_A_NUMBER.doubleValue()));
    }

    // Tests isContextDependent returns true and caches result
    @Test
    public void testIsContextDependent_trueResult_cachedCorrectly() {
        MockExpression expr = new MockExpression(true, null);
        assertTrue(expr.isContextDependent());
        assertTrue(expr.isContextDependent());
        assertEquals(1, expr.computeContextDependentCalls);
    }

    // Tests isContextDependent returns false and caches result
    @Test
    public void testIsContextDependent_falseResult_cachedCorrectly() {
        MockExpression expr = new MockExpression(false, null);
        assertFalse(expr.isContextDependent());
        assertFalse(expr.isContextDependent());
        assertEquals(1, expr.computeContextDependentCalls);
    }

    // Tests iterate when compute returns a standard collection
    @Test
    public void testIterate_collectionResult_returnsValueIterator() {
        List list = Arrays.asList("a", "b", "c");
        MockExpression expr = new MockExpression(false, list);

        Iterator it = expr.iterate(null);
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests iterate when compute returns null
    @Test
    public void testIterate_nullResult_returnsEmptyIterator() {
        MockExpression expr = new MockExpression(false, null);
        Iterator it = expr.iterate(null);
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // Tests iteratePointers when compute returns null
    @Test
    public void testIteratePointers_nullResult_returnsEmptyIterator() {
        MockExpression expr = new MockExpression(false, null);
        Iterator it = expr.iteratePointers(null);
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // Tests PointerIterator hasNext and next with non-Pointer items
    @Test
    public void testPointerIterator_nonPointerItems_wrapsInNodePointer() {
        List items = Arrays.asList("first", "second");
        QName qname = new QName(null, "value");
        Expression.PointerIterator it = new Expression.PointerIterator(items.iterator(), qname, Locale.US);

        assertTrue(it.hasNext());
        Object first = it.next();
        assertTrue(first instanceof NodePointer);
        assertEquals("first", ((NodePointer) first).getValue());

        assertTrue(it.hasNext());
        Object second = it.next();
        assertTrue(second instanceof NodePointer);
        assertEquals("second", ((NodePointer) second).getValue());

        assertFalse(it.hasNext());
    }

    // Tests PointerIterator hasNext and next with existing Pointer items
    @Test
    public void testPointerIterator_existingPointerItems_returnsAsIs() {
        Pointer p1 = new DummyPointer("val1");
        Pointer p2 = new DummyPointer("val2");
        List items = Arrays.asList(p1, p2);
        QName qname = new QName(null, "value");
        Expression.PointerIterator it = new Expression.PointerIterator(items.iterator(), qname, Locale.US);

        assertTrue(it.hasNext());
        assertSame(p1, it.next());
        assertTrue(it.hasNext());
        assertSame(p2, it.next());
        assertFalse(it.hasNext());
    }

    // Tests PointerIterator remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPointerIterator_remove_throwsException() {
        List items = Collections.singletonList("item");
        Expression.PointerIterator it = new Expression.PointerIterator(items.iterator(), new QName(null, "val"), Locale.US);
        it.remove();
    }

    // Tests ValueIterator hasNext and next with Pointer items
    @Test
    public void testValueIterator_pointerItems_unwrapsValues() {
        Pointer p1 = new DummyPointer("unwrapped1");
        Pointer p2 = new DummyPointer("unwrapped2");
        List items = Arrays.asList(p1, p2);
        Expression.ValueIterator it = new Expression.ValueIterator(items.iterator());

        assertTrue(it.hasNext());
        assertEquals("unwrapped1", it.next());
        assertTrue(it.hasNext());
        assertEquals("unwrapped2", it.next());
        assertFalse(it.hasNext());
    }

    // Tests ValueIterator hasNext and next with non-Pointer items
    @Test
    public void testValueIterator_nonPointerItems_returnsDirectValues() {
        List items = Arrays.asList("direct1", "direct2");
        Expression.ValueIterator it = new Expression.ValueIterator(items.iterator());

        assertTrue(it.hasNext());
        assertEquals("direct1", it.next());
        assertTrue(it.hasNext());
        assertEquals("direct2", it.next());
        assertFalse(it.hasNext());
    }

    // Tests ValueIterator remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testValueIterator_remove_throwsException() {
        List items = Collections.singletonList("item");
        Expression.ValueIterator it = new Expression.ValueIterator(items.iterator());
        it.remove();
    }

    // Tests ValueIterator on empty iterator
    @Test
    public void testValueIterator_emptyIterator_hasNextReturnsFalse() {
        Expression.ValueIterator it = new Expression.ValueIterator(Collections.EMPTY_LIST.iterator());
        assertFalse(it.hasNext());
    }

    // Tests PointerIterator on empty iterator
    @Test
    public void testPointerIterator_emptyIterator_hasNextReturnsFalse() {
        Expression.PointerIterator it = new Expression.PointerIterator(Collections.EMPTY_LIST.iterator(), new QName("test"), Locale.ENGLISH);
        assertFalse(it.hasNext());
    }
}
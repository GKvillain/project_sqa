package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ExpressionTest {

    private static class TestExpression extends Expression {
        private final boolean contextDependentValue;
        private final Object computeResult;
        private int computeContextDependentCallCount = 0;

        public TestExpression(boolean contextDependentValue, Object computeResult) {
            this.contextDependentValue = contextDependentValue;
            this.computeResult = computeResult;
        }

        public boolean computeContextDependent() {
            computeContextDependentCallCount++;
            return contextDependentValue;
        }

        public Object computeValue(EvalContext context) {
            return computeResult;
        }

        public Object compute(EvalContext context) {
            return computeResult;
        }

        public int getComputeContextDependentCallCount() {
            return computeContextDependentCallCount;
        }
    }

    private static class MockPointer implements Pointer {
        private final Object value;

        public MockPointer(Object value) {
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
            return "/";
        }

        public JXPathContext getRelativeContext(JXPathContext context) {
            return null;
        }
    }

    // Tests context dependency caching when context dependent is false
    @Test
    public void testIsContextDependent_whenFalse_returnsFalseAndCachesResult() {
        TestExpression expr = new TestExpression(false, null);
        assertFalse(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());

        // Second call should return cached value without recomputing
        assertFalse(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
    }

    // Tests context dependency caching when context dependent is true
    @Test
    public void testIsContextDependent_whenTrue_returnsTrueAndCachesResult() {
        TestExpression expr = new TestExpression(true, null);
        assertTrue(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());

        // Second call should return cached value without recomputing
        assertTrue(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
    }

    // Tests iterate method when compute returns a simple object
    @Test
    public void testIterate_simpleObject_returnsIterator() {
        TestExpression expr = new TestExpression(false, "testValue");
        Iterator iterator = expr.iterate(null);
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals("testValue", iterator.next());
        assertFalse(iterator.hasNext());
    }

    // Tests iterate method when compute returns null
    @Test
    public void testIterate_nullResult_returnsEmptyIterator() {
        TestExpression expr = new TestExpression(false, null);
        Iterator iterator = expr.iterate(null);
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    // Tests iteratePointers method when compute returns null
    @Test
    public void testIteratePointers_nullResult_returnsEmptyIterator() {
        TestExpression expr = new TestExpression(false, null);
        Iterator iterator = expr.iteratePointers(null);
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    // Tests ValueIterator with Pointer elements
    @Test
    public void testValueIterator_withPointer_unwrapsPointerValue() {
        Pointer pointer1 = new MockPointer("value1");
        Pointer pointer2 = new MockPointer("value2");
        Iterator sourceIterator = Arrays.asList(pointer1, pointer2).iterator();

        Expression.ValueIterator valueIterator = new Expression.ValueIterator(sourceIterator);

        assertTrue(valueIterator.hasNext());
        assertEquals("value1", valueIterator.next());
        assertTrue(valueIterator.hasNext());
        assertEquals("value2", valueIterator.next());
        assertFalse(valueIterator.hasNext());
    }

    // Tests ValueIterator with non-Pointer elements
    @Test
    public void testValueIterator_withNonPointer_returnsObjectAsIs() {
        Iterator sourceIterator = Arrays.asList("rawString", Integer.valueOf(42)).iterator();

        Expression.ValueIterator valueIterator = new Expression.ValueIterator(sourceIterator);

        assertTrue(valueIterator.hasNext());
        assertEquals("rawString", valueIterator.next());
        assertTrue(valueIterator.hasNext());
        assertEquals(Integer.valueOf(42), valueIterator.next());
        assertFalse(valueIterator.hasNext());
    }

    // Tests ValueIterator remove operation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testValueIterator_remove_throwsUnsupportedOperationException() {
        Iterator sourceIterator = Collections.singletonList("item").iterator();
        Expression.ValueIterator valueIterator = new Expression.ValueIterator(sourceIterator);
        valueIterator.remove();
    }

    // Tests PointerIterator with existing Pointer element
    @Test
    public void testPointerIterator_withExistingPointer_returnsSamePointer() {
        Pointer pointer = new MockPointer("pointerData");
        Iterator sourceIterator = Collections.singletonList(pointer).iterator();
        QName qname = new QName(null, "value");

        Expression.PointerIterator pointerIterator = new Expression.PointerIterator(sourceIterator, qname, Locale.US);

        assertTrue(pointerIterator.hasNext());
        Object result = pointerIterator.next();
        assertSame(pointer, result);
        assertFalse(pointerIterator.hasNext());
    }

    // Tests PointerIterator with non-Pointer element wrapping into NodePointer
    @Test
    public void testPointerIterator_withNonPointer_wrapsInNodePointer() {
        Iterator sourceIterator = Collections.singletonList("nonPointerValue").iterator();
        QName qname = new QName(null, "value");

        Expression.PointerIterator pointerIterator = new Expression.PointerIterator(sourceIterator, qname, Locale.US);

        assertTrue(pointerIterator.hasNext());
        Object result = pointerIterator.next();
        assertTrue(result instanceof NodePointer);
        assertEquals("nonPointerValue", ((NodePointer) result).getValue());
        assertFalse(pointerIterator.hasNext());
    }

    // Tests PointerIterator remove operation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPointerIterator_remove_throwsUnsupportedOperationException() {
        Iterator sourceIterator = Collections.singletonList("item").iterator();
        QName qname = new QName(null, "value");
        Expression.PointerIterator pointerIterator = new Expression.PointerIterator(sourceIterator, qname, Locale.US);
        pointerIterator.remove();
    }
}
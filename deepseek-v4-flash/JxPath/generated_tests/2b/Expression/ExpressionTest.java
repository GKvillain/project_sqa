package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Collections;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * JUnit 4 test class for {@link Expression}.
 * Tests static fields, inner classes (ValueIterator, PointerIterator),
 * and accessible behavior of the abstract class via the concrete subclass Constant.
 */
public class ExpressionTest {

    // ----- Static fields -----
    @Test
    public void testStaticFields_valuesCorrect() {
        assertEquals(new Double(0), Expression.ZERO);
        assertEquals(new Double(1), Expression.ONE);
        assertEquals(new Double(Double.NaN), Expression.NOT_A_NUMBER);
    }

    // ----- ValueIterator -----
    @Test
    public void testValueIterator_hasNextNonEmpty_returnsTrue() {
        Iterator<String> source = Arrays.asList("a", "b").iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(source);
        assertTrue(vi.hasNext());
        vi.next();
        assertTrue(vi.hasNext());
        vi.next();
        assertFalse(vi.hasNext());
    }

    @Test
    public void testValueIterator_hasNextEmpty_returnsFalse() {
        Iterator<Object> source = Collections.emptyList().iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(source);
        assertFalse(vi.hasNext());
    }

    @Test
    public void testValueIterator_nextWithNonPointerObject_returnsObject() {
        Iterator<String> source = Arrays.asList("hello").iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(source);
        assertEquals("hello", vi.next());
    }

    @Test
    public void testValueIterator_nextWithPointerObject_returnsPointerValue() {
        Pointer pointer = NodePointer.newNodePointer(
                new QName(null, "test"), "content", Locale.US);
        Iterator<Pointer> source = Arrays.asList(pointer).iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(source);
        Object next = vi.next();
        assertNotNull(next);
        assertEquals("content", next);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValueIterator_remove_throwsUnsupportedOperationException() {
        Iterator<String> source = Arrays.asList("a").iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(source);
        vi.next();
        vi.remove();
    }

    // ----- PointerIterator -----
    @Test
    public void testPointerIterator_hasNextNonEmpty_returnsTrue() {
        Iterator<String> source = Arrays.asList("a").iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(
                source, new QName(null, "val"), Locale.US);
        assertTrue(pi.hasNext());
        pi.next();
        assertFalse(pi.hasNext());
    }

    @Test
    public void testPointerIterator_hasNextEmpty_returnsFalse() {
        Iterator<Object> source = Collections.emptyList().iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(
                source, new QName(null, "val"), Locale.US);
        assertFalse(pi.hasNext());
    }

    @Test
    public void testPointerIterator_nextWithNonPointerObject_returnsNewNodePointer() {
        Iterator<String> source = Arrays.asList("value").iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(
                source, new QName(null, "node"), Locale.US);
        Object next = pi.next();
        assertNotNull(next);
        assertTrue(next instanceof NodePointer);
        NodePointer np = (NodePointer) next;
        assertEquals("value", np.getValue());
    }

    @Test
    public void testPointerIterator_nextWithPointerObject_returnsPointer() {
        Pointer pointer = NodePointer.newNodePointer(
                new QName(null, "p"), "data", Locale.US);
        Iterator<Pointer> source = Arrays.asList(pointer).iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(
                source, new QName(null, "ignore"), Locale.US);
        Object next = pi.next();
        assertNotNull(next);
        assertTrue(next instanceof Pointer);
        // Should be the same pointer instance
        assertSame(pointer, next);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPointerIterator_remove_throwsUnsupportedOperationException() {
        Iterator<String> source = Arrays.asList("x").iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(
                source, new QName(null, "x"), Locale.US);
        pi.next();
        pi.remove();
    }

    // ----- isContextDependent() using concrete subclass Constant -----
    @Test
    public void testIsContextDependent_constant_returnsFalseAndCaches() {
        // Constant.computeContextDependent() returns false
        Expression expr = new Constant(new Double(1));
        assertFalse(expr.isContextDependent());
        // Second call uses cached value
        assertFalse(expr.isContextDependent());
    }

    // ----- iterate() using Constant (non-null, non-EvalContext) -----
    @Test
    public void testIterate_constantValue_returnsIteratorWithValue() {
        Expression expr = new Constant(new Integer(42));
        Iterator iter = expr.iterate(null);  // context not used by Constant.compute()
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(42, iter.next());
        assertFalse(iter.hasNext());
    }

    // iterate() with null value (Constant(null) returns null from compute)
    @Test
    public void testIterate_constantNull_returnsEmptyIterator() {
        // Use explicit cast to Number to resolve ambiguous constructor (Number vs String)
        Expression expr = new Constant((Number) null);
        Iterator iter = expr.iterate(null);
        assertNotNull(iter);
        assertFalse(iter.hasNext());
    }

    // ----- iteratePointers() using Constant(null) -----
    @Test
    public void testIteratePointers_nullValue_returnsEmptyIterator() {
        // Use explicit cast to Number to resolve ambiguous constructor (Number vs String)
        Expression expr = new Constant((Number) null);
        Iterator iter = expr.iteratePointers(null);
        assertNotNull(iter);
        assertFalse(iter.hasNext());
    }
}
package org.apache.commons.jxpath.ri.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationCompareTest {

    private static class DummyCompare extends CoreOperationCompare {
        public DummyCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        public String getSymbol() {
            return "==";
        }
    }

    private static class DummyExpression extends Expression {
        private final Object value;

        public DummyExpression(Object value) {
            this.value = value;
        }

        public Object compute(EvalContext context) {
            return value;
        }

        public Object computeValue(EvalContext context) {
            return value;
        }

        public boolean computeContextDependent() {
            return false;
        }
    }

    private DummyCompare compareOp;

    @Before
    public void setUp() {
        compareOp = new DummyCompare(new Constant("left"), new Constant("right"));
    }

    // Tests equal with identical object references
    @Test
    public void testEqual_sameObjectReference_returnsTrue() {
        String str = "test";
        assertTrue(compareOp.equal(str, str));
    }

    // Tests equal with both null values
    @Test
    public void testEqual_bothNull_returnsTrue() {
        assertTrue(compareOp.equal((Object) null, (Object) null));
    }

    // Tests equal with one null and one non-null value
    @Test
    public void testEqual_oneNullOneNonNull_returnsFalse() {
        assertFalse(compareOp.equal("test", null));
        assertFalse(compareOp.equal(null, "test"));
    }

    // Tests equal with boolean comparisons
    @Test
    public void testEqual_booleanComparisons_returnsCorrectResult() {
        assertTrue(compareOp.equal(Boolean.TRUE, Boolean.TRUE));
        assertFalse(compareOp.equal(Boolean.TRUE, Boolean.FALSE));
        assertTrue(compareOp.equal(Boolean.TRUE, "true"));
        assertFalse(compareOp.equal(Boolean.TRUE, "false"));
    }

    // Tests equal with number comparisons
    @Test
    public void testEqual_numberComparisons_returnsCorrectResult() {
        assertTrue(compareOp.equal(new Integer(10), new Double(10.0)));
        assertFalse(compareOp.equal(new Integer(10), new Double(20.0)));
        assertTrue(compareOp.equal(new Integer(10), "10"));
        assertFalse(compareOp.equal(new Integer(10), "20"));
    }

    // Tests equal with string comparisons
    @Test
    public void testEqual_stringComparisons_returnsCorrectResult() {
        assertTrue(compareOp.equal("hello", "hello"));
        assertFalse(compareOp.equal("hello", "world"));
    }

    // Tests equal with Pointer objects wrapping equal values
    @Test
    public void testEqual_pointerComparisons_returnsCorrectResult() {
        NodePointer np1 = NodePointer.newNodePointer(null, "value", Locale.getDefault());
        NodePointer np2 = NodePointer.newNodePointer(null, "value", Locale.getDefault());
        assertTrue(compareOp.equal(np1, np2));
        assertTrue(compareOp.equal(np1, "value"));
        assertTrue(compareOp.equal("value", np2));
    }

    // Tests contains method when element is found in iterator
    @Test
    public void testContains_elementInIterator_returnsTrue() {
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertTrue(compareOp.contains(it, "b"));
    }

    // Tests contains method when element is not found in iterator
    @Test
    public void testContains_elementNotInIterator_returnsFalse() {
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertFalse(compareOp.contains(it, "d"));
    }

    // Tests contains method with empty iterator
    @Test
    public void testContains_emptyIterator_returnsFalse() {
        Iterator it = Collections.emptyList().iterator();
        assertFalse(compareOp.contains(it, "a"));
    }

    // Tests findMatch method when a match exists between two iterators
    @Test
    public void testFindMatch_matchingElementsExist_returnsTrue() {
        Iterator lit = Arrays.asList(new Integer(1), new Integer(2)).iterator();
        Iterator rit = Arrays.asList(new Double(2.0), new Double(3.0)).iterator();
        assertTrue(compareOp.findMatch(lit, rit));
    }

    // Tests findMatch method when no match exists between two iterators
    @Test
    public void testFindMatch_noMatchingElements_returnsFalse() {
        Iterator lit = Arrays.asList("a", "b").iterator();
        Iterator rit = Arrays.asList("c", "d").iterator();
        assertFalse(compareOp.findMatch(lit, rit));
    }

    // Tests equal with expressions evaluating to collections
    @Test
    public void testEqual_expressionsWithCollections_returnsCorrectResult() {
        final List list1 = Arrays.asList("a", "b");
        final List list2 = Arrays.asList("b", "c");
        final List list3 = Arrays.asList("x", "y");

        Expression expr1 = new DummyExpression(list1);
        Expression expr2 = new DummyExpression(list2);
        Expression expr3 = new DummyExpression(list3);

        assertTrue(compareOp.equal(null, expr1, expr2));
        assertFalse(compareOp.equal(null, expr1, expr3));
    }

    // Tests equal with one collection expression and one scalar expression
    @Test
    public void testEqual_collectionAndScalarExpressions_returnsCorrectResult() {
        final List list = Arrays.asList("a", "b");
        Expression exprList = new DummyExpression(list);
        Expression exprMatch = new Constant("b");
        Expression exprNoMatch = new Constant("z");

        assertTrue(compareOp.equal(null, exprList, exprMatch));
        assertTrue(compareOp.equal(null, exprMatch, exprList));
        assertFalse(compareOp.equal(null, exprList, exprNoMatch));
        assertFalse(compareOp.equal(null, exprNoMatch, exprList));
    }

    // Tests equal with InitialContext expressions
    @Test
    public void testEqual_initialContextExpressions_returnsCorrectResult() {
        JXPathContextReferenceImpl context = (JXPathContextReferenceImpl) JXPathContext.newContext("testValue");
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, "testValue", Locale.getDefault()));
        final InitialContext initContext1 = new InitialContext(rootContext);
        final InitialContext initContext2 = new InitialContext(rootContext);

        Expression expr1 = new DummyExpression(initContext1);
        Expression expr2 = new DummyExpression(initContext2);

        assertTrue(compareOp.equal(rootContext, expr1, expr2));
    }
}
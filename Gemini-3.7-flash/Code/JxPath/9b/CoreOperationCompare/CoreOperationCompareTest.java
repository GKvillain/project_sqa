package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoreOperationCompareTest {

    private static class ConcreteCompare extends CoreOperationCompare {
        public ConcreteCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public ConcreteCompare(Expression arg1, Expression arg2, boolean invert) {
            super(arg1, arg2, invert);
        }

        public Object computeValue(EvalContext context) {
            return Boolean.valueOf(equal(context, args[0], args[1]));
        }

        public String getSymbol() {
            return "==";
        }
    }

    private static class ValueExpr extends Expression {
        private final Object val;

        public ValueExpr(Object val) {
            this.val = val;
        }

        public Object compute(EvalContext context) {
            return val;
        }

        public Object computeValue(EvalContext context) {
            return val;
        }

        public boolean isContextDependent() {
            return false;
        }
    }

    private ConcreteCompare compare;

    @Before
    public void setUp() {
        compare = new ConcreteCompare(new Constant("a"), new Constant("b"));
    }

    // Tests operator precedence returns 2
    @Test
    public void testGetPrecedence_returnsTwo() {
        assertEquals(2, compare.getPrecedence());
    }

    // Tests symmetry property returns true
    @Test
    public void testIsSymmetric_returnsTrue() {
        assertTrue(compare.isSymmetric());
    }

    // Tests equality between two null values
    @Test
    public void testEqual_bothNull_returnsTrue() {
        assertTrue(compare.equal((Object) null, (Object) null));
    }

    // Tests equality when left side is null and right side is non-null
    @Test
    public void testEqual_leftNullRightNonNull_returnsFalse() {
        assertFalse(compare.equal((Object) null, "test"));
    }

    // Tests equality when left side is non-null and right side is null
    @Test
    public void testEqual_leftNonNullRightNull_returnsFalse() {
        assertFalse(compare.equal("test", (Object) null));
    }

    // Tests equality between boolean values
    @Test
    public void testEqual_sameBooleanValues_returnsTrue() {
        assertTrue(compare.equal(Boolean.TRUE, Boolean.TRUE));
        assertTrue(compare.equal(Boolean.FALSE, Boolean.FALSE));
    }

    // Tests inequality between different boolean values
    @Test
    public void testEqual_differentBooleanValues_returnsFalse() {
        assertFalse(compare.equal(Boolean.TRUE, Boolean.FALSE));
    }

    // Tests boolean conversion comparison with string
    @Test
    public void testEqual_booleanAndString_convertsAndCompares() {
        assertTrue(compare.equal(Boolean.TRUE, "true"));
        assertFalse(compare.equal(Boolean.FALSE, "true"));
    }

    // Tests equality between numbers with identical values
    @Test
    public void testEqual_sameNumericValues_returnsTrue() {
        assertTrue(compare.equal(Double.valueOf(10.5), Double.valueOf(10.5)));
        assertTrue(compare.equal(Integer.valueOf(10), Double.valueOf(10.0)));
    }

    // Tests inequality between numbers with different values
    @Test
    public void testEqual_differentNumericValues_returnsFalse() {
        assertFalse(compare.equal(Double.valueOf(10.5), Double.valueOf(20.5)));
    }

    // Tests numeric comparison involving NaN
    @Test
    public void testEqual_nanValues_returnsFalse() {
        assertFalse(compare.equal(Double.valueOf(Double.NaN), Double.valueOf(Double.NaN)));
        assertFalse(compare.equal(Double.valueOf(Double.NaN), Double.valueOf(1.0)));
    }

    // Tests equality between identical strings
    @Test
    public void testEqual_sameStrings_returnsTrue() {
        assertTrue(compare.equal("hello", "hello"));
    }

    // Tests inequality between different strings
    @Test
    public void testEqual_differentStrings_returnsFalse() {
        assertFalse(compare.equal("hello", "world"));
    }

    // Tests string vs number comparison
    @Test
    public void testEqual_stringAndNumber_comparesNumerically() {
        assertTrue(compare.equal("100", Integer.valueOf(100)));
        assertFalse(compare.equal("100", Integer.valueOf(200)));
    }

    // Tests equality between identical Pointer objects
    @Test
    public void testEqual_identicalPointers_returnsTrue() {
        Pointer p1 = new NullPointer(java.util.Locale.getDefault(), "id1");
        assertTrue(compare.equal(p1, p1));
    }

    // Tests contains method when element exists in iterator
    @Test
    public void testContains_elementInIterator_returnsTrue() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertTrue(compare.contains(list.iterator(), "b"));
    }

    // Tests contains method when element does not exist in iterator
    @Test
    public void testContains_elementNotInIterator_returnsFalse() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertFalse(compare.contains(list.iterator(), "d"));
    }

    // Tests findMatch when there is an intersection between iterators
    @Test
    public void testFindMatch_commonElements_returnsTrue() {
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("c", "d", "e");
        assertTrue(compare.findMatch(list1.iterator(), list2.iterator()));
    }

    // Tests findMatch when there is no intersection between iterators
    @Test
    public void testFindMatch_noCommonElements_returnsFalse() {
        List<String> list1 = Arrays.asList("a", "b");
        List<String> list2 = Arrays.asList("c", "d");
        assertFalse(compare.findMatch(list1.iterator(), list2.iterator()));
    }

    // Tests findMatch with empty iterators
    @Test
    public void testFindMatch_emptyIterators_returnsFalse() {
        List<String> list1 = Collections.emptyList();
        List<String> list2 = Collections.emptyList();
        assertFalse(compare.findMatch(list1.iterator(), list2.iterator()));
    }

    // Tests equal with eval context and constant expressions
    @Test
    public void testEqual_evalContextWithConstants_evaluatesCorrectly() {
        Expression expr1 = new Constant("test");
        Expression expr2 = new Constant("test");
        assertTrue(compare.equal(null, expr1, expr2));

        Expression expr3 = new Constant("other");
        assertFalse(compare.equal(null, expr1, expr3));
    }

    // Tests three-argument constructor with invert parameter
    @Test
    public void testConstructor_withInvert_createsInstance() {
        ConcreteCompare compareInvert = new ConcreteCompare(new Constant("a"), new Constant("b"), true);
        assertNotNull(compareInvert);
        assertEquals(2, compareInvert.getPrecedence());
    }

    // Tests equal with context when left expression evaluates to Collection and right to single value
    @Test
    public void testEqual_evalContextLeftCollectionRightValue() {
        Expression leftExpr = new ValueExpr(Arrays.asList("x", "y", "z"));
        Expression rightExprMatch = new ValueExpr("y");
        Expression rightExprNoMatch = new ValueExpr("w");

        assertTrue(compare.equal(null, leftExpr, rightExprMatch));
        assertFalse(compare.equal(null, leftExpr, rightExprNoMatch));
    }

    // Tests equal with context when left expression evaluates to single value and right to Collection
    @Test
    public void testEqual_evalContextLeftValueRightCollection() {
        Expression leftExprMatch = new ValueExpr("y");
        Expression leftExprNoMatch = new ValueExpr("w");
        Expression rightExpr = new ValueExpr(Arrays.asList("x", "y", "z"));

        assertTrue(compare.equal(null, leftExprMatch, rightExpr));
        assertFalse(compare.equal(null, leftExprNoMatch, rightExpr));
    }

    // Tests equal with context when both expressions evaluate to Collections
    @Test
    public void testEqual_evalContextBothCollections() {
        Expression leftExpr = new ValueExpr(Arrays.asList("a", "b"));
        Expression rightExprMatch = new ValueExpr(Arrays.asList("b", "c"));
        Expression rightExprNoMatch = new ValueExpr(Arrays.asList("c", "d"));

        assertTrue(compare.equal(null, leftExpr, rightExprMatch));
        assertFalse(compare.equal(null, leftExpr, rightExprNoMatch));
    }

    // Tests equal with distinct Pointer instances having equal wrapped values
    @Test
    public void testEqual_distinctPointersWithEqualValues() {
        NodePointer p1 = NodePointer.newNodePointer(new QName("p1"), "value", java.util.Locale.getDefault());
        NodePointer p2 = NodePointer.newNodePointer(new QName("p2"), "value", java.util.Locale.getDefault());
        assertTrue(compare.equal(p1, p2));

        NodePointer p3 = NodePointer.newNodePointer(new QName("p3"), "different", java.util.Locale.getDefault());
        assertFalse(compare.equal(p1, p3));
    }

    // Tests equal unwrapping Pointer on one side and raw value on the other
    @Test
    public void testEqual_pointerAndRawValue() {
        NodePointer p = NodePointer.newNodePointer(new QName("p"), "value", java.util.Locale.getDefault());
        assertTrue(compare.equal(p, "value"));
        assertTrue(compare.equal("value", p));
        assertFalse(compare.equal(p, "other"));
        assertFalse(compare.equal("other", p));
    }

    // Tests equal when Pointer wraps null
    @Test
    public void testEqual_pointerWithNullValue() {
        Pointer nullPointer1 = new NullPointer(java.util.Locale.getDefault(), "id1");
        Pointer nullPointer2 = new NullPointer(java.util.Locale.getDefault(), "id2");
        assertTrue(compare.equal(nullPointer1, nullPointer2));
        assertTrue(compare.equal(nullPointer1, (Object) null));
        assertTrue(compare.equal((Object) null, nullPointer1));
    }

    // Tests equal fallback to Object.equals for non-primitive/non-standard types
    @Test
    public void testEqual_arbitraryObjects_usesEquals() {
        Date d1 = new Date(1000L);
        Date d2 = new Date(1000L);
        Date d3 = new Date(2000L);

        assertTrue(compare.equal(d1, d2));
        assertFalse(compare.equal(d1, d3));
    }

    // Tests equal with InitialContext and SelfContext expression evaluation
    @Test
    public void testEqual_evalContextWithInitialAndSelfContext() {
        JXPathContextReferenceImpl ctx = new JXPathContextReferenceImpl(null, "rootVal", null);
        RootContext rootCtx = ctx.getRootContext();
        InitialContext initCtx1 = new InitialContext(rootCtx);
        InitialContext initCtx2 = new InitialContext(rootCtx);
        SelfContext selfCtx1 = new SelfContext(initCtx1, new NodeTypeTest(1));
        SelfContext selfCtx2 = new SelfContext(initCtx2, new NodeTypeTest(1));

        assertTrue(compare.equal(null, new ValueExpr(initCtx1), new ValueExpr("rootVal")));
        assertTrue(compare.equal(null, new ValueExpr("rootVal"), new ValueExpr(initCtx2)));
        assertTrue(compare.equal(null, new ValueExpr(selfCtx1), new ValueExpr("rootVal")));
        assertTrue(compare.equal(null, new ValueExpr("rootVal"), new ValueExpr(selfCtx2)));
    }
}
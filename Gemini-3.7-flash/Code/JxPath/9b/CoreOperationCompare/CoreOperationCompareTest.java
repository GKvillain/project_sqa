package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationCompareTest {

    private CoreOperationCompare compareOp;

    private static class ConcreteCoreOperationCompare extends CoreOperationCompare {
        public ConcreteCoreOperationCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public ConcreteCoreOperationCompare(Expression arg1, Expression arg2, boolean invert) {
            super(arg1, arg2, invert);
        }

        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        public String getSymbol() {
            return "=";
        }
    }

    @Before
    public void setUp() {
        compareOp = new ConcreteCoreOperationCompare(new Constant("left"), new Constant("right"));
    }

    // Tests getPrecedence returns correct precedence value
    @Test
    public void testGetPrecedence_default_returnsTwo() {
        assertEquals(2, compareOp.getPrecedence());
    }

    // Tests isSymmetric returns true
    @Test
    public void testIsSymmetric_default_returnsTrue() {
        assertTrue(compareOp.isSymmetric());
    }

    // Tests equal with identical references and both null
    @Test
    public void testEqual_sameAndNullObjects_returnsTrue() {
        assertTrue(compareOp.equal(null, null));
        String text = "same";
        assertTrue(compareOp.equal(text, text));
    }

    // Tests equal with null vs non-null object
    @Test
    public void testEqual_nullVsNonNull_returnsFalse() {
        assertFalse(compareOp.equal(null, "nonNull"));
        assertFalse(compareOp.equal("nonNull", null));
    }

    // Tests equal comparing Boolean objects
    @Test
    public void testEqual_booleans_returnsCorrectComparison() {
        assertTrue(compareOp.equal(Boolean.TRUE, Boolean.TRUE));
        assertTrue(compareOp.equal(Boolean.FALSE, Boolean.FALSE));
        assertFalse(compareOp.equal(Boolean.TRUE, Boolean.FALSE));
    }

    // Tests equal comparing Boolean and String/Number representations
    @Test
    public void testEqual_booleanWithOtherTypes_returnsCorrectComparison() {
        assertTrue(compareOp.equal(Boolean.TRUE, "true"));
        assertTrue(compareOp.equal(Boolean.FALSE, "false"));
        assertTrue(compareOp.equal(Boolean.TRUE, new Double(1.0)));
        assertTrue(compareOp.equal(Boolean.FALSE, new Double(0.0)));
    }

    // Tests equal comparing Numbers
    @Test
    public void testEqual_numbers_returnsCorrectComparison() {
        assertTrue(compareOp.equal(new Integer(10), new Double(10.0)));
        assertFalse(compareOp.equal(new Integer(10), new Double(20.0)));
    }

    // Tests equal comparing NaN values
    @Test
    public void testEqual_nanValues_returnsFalse() {
        assertFalse(compareOp.equal(new Double(Double.NaN), new Double(Double.NaN)));
        assertFalse(compareOp.equal(new Double(Double.NaN), new Double(1.0)));
    }

    // Tests equal comparing Strings
    @Test
    public void testEqual_strings_returnsCorrectComparison() {
        assertTrue(compareOp.equal("hello", "hello"));
        assertFalse(compareOp.equal("hello", "world"));
    }

    // Tests equal comparing String and Number
    @Test
    public void testEqual_stringAndNumber_returnsCorrectComparison() {
        assertTrue(compareOp.equal("42", new Integer(42)));
        assertTrue(compareOp.equal(new Double(42.0), "42"));
        assertFalse(compareOp.equal("42", new Integer(43)));
    }

    // Tests contains when element is present in iterator
    @Test
    public void testContains_elementInIterator_returnsTrue() {
        List list = Arrays.asList("apple", "banana", "cherry");
        assertTrue(compareOp.contains(list.iterator(), "banana"));
    }

    // Tests contains when element is absent from iterator
    @Test
    public void testContains_elementNotInIterator_returnsFalse() {
        List list = Arrays.asList("apple", "banana", "cherry");
        assertFalse(compareOp.contains(list.iterator(), "orange"));
    }

    // Tests contains with empty iterator
    @Test
    public void testContains_emptyIterator_returnsFalse() {
        List list = Collections.emptyList();
        assertFalse(compareOp.contains(list.iterator(), "anything"));
    }

    // Tests findMatch with overlapping iterators
    @Test
    public void testFindMatch_overlappingIterators_returnsTrue() {
        List left = Arrays.asList("a", "b", "c");
        List right = Arrays.asList("x", "b", "z");
        assertTrue(compareOp.findMatch(left.iterator(), right.iterator()));
    }

    // Tests findMatch with disjoint iterators
    @Test
    public void testFindMatch_disjointIterators_returnsFalse() {
        List left = Arrays.asList("a", "b", "c");
        List right = Arrays.asList("x", "y", "z");
        assertFalse(compareOp.findMatch(left.iterator(), right.iterator()));
    }

    // Tests equal with Expression arguments evaluated in context
    @Test
    public void testEqual_expressionContextEvaluation_returnsExpectedResult() {
        Constant left = new Constant("value");
        Constant right = new Constant("value");
        CoreOperationCompare op = new ConcreteCoreOperationCompare(left, right);
        assertTrue(op.equal(null, left, right));

        Constant diff = new Constant("different");
        assertFalse(op.equal(null, left, diff));
    }

    // Tests equal with Collection left and right arguments
    @Test
    public void testEqual_collectionExpressions_matchesContainedElement() {
        final List leftList = new ArrayList();
        leftList.add("item1");
        leftList.add("item2");

        Expression leftExpr = new Expression() {
            public Object compute(EvalContext context) {
                return leftList;
            }
            public Object computeValue(EvalContext context) {
                return leftList;
            }
            public boolean computeContextDependent() {
                return false;
            }
        };

        Expression rightExpr = new Constant("item2");
        CoreOperationCompare op = new ConcreteCoreOperationCompare(leftExpr, rightExpr);
        assertTrue(op.equal(null, leftExpr, rightExpr));

        Expression noMatchRight = new Constant("item3");
        assertFalse(op.equal(null, leftExpr, noMatchRight));
    }

    // Tests equal with Collection on right side
    @Test
    public void testEqual_collectionOnRight_matchesContainedElement() {
        final List rightList = Arrays.asList("alpha", "beta");
        Expression leftExpr = new Constant("beta");
        Expression rightExpr = new Expression() {
            public Object compute(EvalContext context) {
                return rightList;
            }
            public Object computeValue(EvalContext context) {
                return rightList;
            }
            public boolean computeContextDependent() {
                return false;
            }
        };

        CoreOperationCompare op = new ConcreteCoreOperationCompare(leftExpr, rightExpr);
        assertTrue(op.equal(null, leftExpr, rightExpr));

        Expression noMatchLeft = new Constant("gamma");
        assertFalse(op.equal(null, noMatchLeft, rightExpr));
    }

    // Tests equal with Collection on both left and right sides
    @Test
    public void testEqual_bothCollectionExpressions_matchesOverlap() {
        final List leftList = Arrays.asList("1", "2");
        final List rightListMatching = Arrays.asList("2", "3");
        final List rightListNonMatching = Arrays.asList("3", "4");

        Expression leftExpr = new Expression() {
            public Object compute(EvalContext context) {
                return leftList;
            }
            public Object computeValue(EvalContext context) {
                return leftList;
            }
            public boolean computeContextDependent() {
                return false;
            }
        };

        Expression rightMatchExpr = new Expression() {
            public Object compute(EvalContext context) {
                return rightListMatching;
            }
            public Object computeValue(EvalContext context) {
                return rightListMatching;
            }
            public boolean computeContextDependent() {
                return false;
            }
        };

        Expression rightNoMatchExpr = new Expression() {
            public Object compute(EvalContext context) {
                return rightListNonMatching;
            }
            public Object computeValue(EvalContext context) {
                return rightListNonMatching;
            }
            public boolean computeContextDependent() {
                return false;
            }
        };

        CoreOperationCompare op = new ConcreteCoreOperationCompare(leftExpr, rightMatchExpr);
        assertTrue(op.equal(null, leftExpr, rightMatchExpr));
        assertFalse(op.equal(null, leftExpr, rightNoMatchExpr));
    }

    // Tests invert constructor and evaluation
    @Test
    public void testInvertConstructor_invertsResult() {
        Constant left = new Constant("same");
        Constant right = new Constant("same");
        CoreOperationCompare invertedOp = new ConcreteCoreOperationCompare(left, right, true);
        assertFalse(invertedOp.equal(null, left, right));

        Constant diff = new Constant("different");
        assertTrue(invertedOp.equal(null, left, diff));
    }

    // Tests equal with arbitrary non-primitive objects
    @Test
    public void testEqual_arbitraryObjects_usesEquals() {
        Object obj1 = new HashSet(Arrays.asList("a"));
        Object obj2 = new HashSet(Arrays.asList("a"));
        Object obj3 = new HashSet(Arrays.asList("b"));

        assertTrue(compareOp.equal(obj1, obj2));
        assertFalse(compareOp.equal(obj1, obj3));
    }
}
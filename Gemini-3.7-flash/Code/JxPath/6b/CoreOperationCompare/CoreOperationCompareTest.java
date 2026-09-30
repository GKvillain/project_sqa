package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationCompareTest {

    private TestCoreOperationCompare operation;
    private EvalContext rootEvalContext;

    private static class TestCoreOperationCompare extends CoreOperationCompare {
        public TestCoreOperationCompare(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public TestCoreOperationCompare(Expression arg1, Expression arg2, boolean invert) {
            super(arg1, arg2, invert);
        }

        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        public String getSymbol() {
            return "==";
        }

        public boolean testEqual(EvalContext context, Expression left, Expression right) {
            return equal(context, left, right);
        }

        public boolean testEqual(Object l, Object r) {
            return equal(l, r);
        }

        public boolean testContains(java.util.Iterator it, Object value) {
            return contains(it, value);
        }

        public boolean testFindMatch(java.util.Iterator lit, java.util.Iterator rit) {
            return findMatch(lit, rit);
        }
    }

    @Before
    public void setUp() {
        Constant left = new Constant("dummy");
        Constant right = new Constant("dummy");
        operation = new TestCoreOperationCompare(left, right);

        JXPathContextReferenceImpl context = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(null, new Object(), Locale.getDefault());
        RootContext rootContext = new RootContext(context, rootPointer);
        rootEvalContext = new InitialContext(rootContext);
    }

    // Tests equal comparison when both objects are identical references
    @Test
    public void testEqual_sameObjectReference_returnsTrue() {
        Object obj = new Object();
        assertTrue(operation.testEqual(obj, obj));
    }

    // Tests equal comparison when both objects are null
    @Test
    public void testEqual_bothNull_returnsTrue() {
        assertTrue(operation.testEqual(null, null));
    }

    // Tests equal comparison when one object is null and the other is not
    @Test
    public void testEqual_oneNullOneNonNull_returnsFalse() {
        assertFalse(operation.testEqual(null, "test"));
        assertFalse(operation.testEqual("test", null));
    }

    // Tests equal comparison with boolean types
    @Test
    public void testEqual_booleansEqual_returnsTrue() {
        assertTrue(operation.testEqual(Boolean.TRUE, Boolean.TRUE));
        assertTrue(operation.testEqual(Boolean.TRUE, "true"));
    }

    // Tests equal comparison with boolean types mismatch
    @Test
    public void testEqual_booleansNotEqual_returnsFalse() {
        assertFalse(operation.testEqual(Boolean.TRUE, Boolean.FALSE));
        assertFalse(operation.testEqual(Boolean.FALSE, "true"));
    }

    // Tests equal comparison with numbers
    @Test
    public void testEqual_numbersEqual_returnsTrue() {
        assertTrue(operation.testEqual(Double.valueOf(10.5), Double.valueOf(10.5)));
        assertTrue(operation.testEqual(Double.valueOf(10.0), Integer.valueOf(10)));
        assertTrue(operation.testEqual(Integer.valueOf(10), "10.0"));
    }

    // Tests equal comparison with numbers mismatch
    @Test
    public void testEqual_numbersNotEqual_returnsFalse() {
        assertFalse(operation.testEqual(Double.valueOf(10.5), Double.valueOf(20.5)));
        assertFalse(operation.testEqual(Integer.valueOf(10), "20.0"));
    }

    // Tests equal comparison with strings
    @Test
    public void testEqual_stringsEqual_returnsTrue() {
        assertTrue(operation.testEqual("hello", "hello"));
        assertTrue(operation.testEqual("123", new StringBuffer("123")));
    }

    // Tests equal comparison with strings mismatch
    @Test
    public void testEqual_stringsNotEqual_returnsFalse() {
        assertFalse(operation.testEqual("hello", "world"));
    }

    // Tests equal comparison with Pointer objects wrapping equal values
    @Test
    public void testEqual_pointersWithEqualValues_returnsTrue() {
        NodePointer ptr1 = NodePointer.newNodePointer(null, "value", Locale.getDefault());
        NodePointer ptr2 = NodePointer.newNodePointer(null, "value", Locale.getDefault());
        assertTrue(operation.testEqual(ptr1, ptr2));
    }

    // Tests contains method when element is present in iterator
    @Test
    public void testContains_elementInIterator_returnsTrue() {
        List list = Arrays.asList("a", "b", "c");
        assertTrue(operation.testContains(list.iterator(), "b"));
    }

    // Tests contains method when element is not present in iterator
    @Test
    public void testContains_elementNotInIterator_returnsFalse() {
        List list = Arrays.asList("a", "b", "c");
        assertFalse(operation.testContains(list.iterator(), "d"));
    }

    // Tests contains method with empty iterator
    @Test
    public void testContains_emptyIterator_returnsFalse() {
        List emptyList = Collections.emptyList();
        assertFalse(operation.testContains(emptyList.iterator(), "a"));
    }

    // Tests findMatch method when collections have common elements
    @Test
    public void testFindMatch_overlappingIterators_returnsTrue() {
        List list1 = Arrays.asList("a", "b", "c");
        List list2 = Arrays.asList("c", "d", "e");
        assertTrue(operation.testFindMatch(list1.iterator(), list2.iterator()));
    }

    // Tests findMatch method when collections are disjoint
    @Test
    public void testFindMatch_disjointIterators_returnsFalse() {
        List list1 = Arrays.asList("a", "b");
        List list2 = Arrays.asList("c", "d");
        assertFalse(operation.testFindMatch(list1.iterator(), list2.iterator()));
    }

    // Tests equal with Expressions returning constant values
    @Test
    public void testEqual_constantExpressionsEqual_returnsTrue() {
        Expression left = new Constant("test");
        Expression right = new Constant("test");
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    // Tests equal with Expressions returning different constant values
    @Test
    public void testEqual_constantExpressionsNotEqual_returnsFalse() {
        Expression left = new Constant("test1");
        Expression right = new Constant("test2");
        assertFalse(operation.testEqual(rootEvalContext, left, right));
    }

    // Tests equal with Expressions evaluating to collections
    @Test
    public void testEqual_collectionAndScalarMatch_returnsTrue() {
        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("x", "y", "z");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Constant("y");
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    // Tests equal with Expressions evaluating to EvalContext instances
    @Test
    public void testEqual_evalContextEvaluation_returnsCorrectResult() {
        JXPathContextReferenceImpl ctx = (JXPathContextReferenceImpl) JXPathContext.newContext("nodeValue");
        NodePointer ptr = NodePointer.newNodePointer(null, "nodeValue", Locale.getDefault());
        RootContext root = new RootContext(ctx, ptr);
        final InitialContext initCtx = new InitialContext(root);

        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return initCtx;
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Constant("nodeValue");
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testIsSymmetric() {
        assertTrue(operation.isSymmetric());
    }

    @Test
    public void testGetPrecedence() {
        assertEquals(Compiler.COMPARE_PRECEDENCE, operation.getPrecedence());
    }

    @Test
    public void testConstructorWithInvert() {
        Constant left = new Constant("a");
        Constant right = new Constant("b");
        TestCoreOperationCompare comp = new TestCoreOperationCompare(left, right, true);
        assertEquals("==", comp.getSymbol());
    }

    @Test
    public void testEqual_doubleNaN_returnsFalse() {
        assertFalse(operation.testEqual(Double.valueOf(Double.NaN), Double.valueOf(Double.NaN)));
        assertFalse(operation.testEqual(Double.valueOf(Double.NaN), Double.valueOf(1.0)));
        assertFalse(operation.testEqual(Double.valueOf(1.0), Double.valueOf(Double.NaN)));
    }

    @Test
    public void testEqual_pointerAndScalar() {
        NodePointer ptr = NodePointer.newNodePointer(null, "apple", Locale.getDefault());
        assertTrue(operation.testEqual(ptr, "apple"));
        assertTrue(operation.testEqual("apple", ptr));
        assertFalse(operation.testEqual(ptr, "banana"));
    }

    @Test
    public void testEqual_pointersUnequalValues_returnsFalse() {
        NodePointer ptr1 = NodePointer.newNodePointer(null, "val1", Locale.getDefault());
        NodePointer ptr2 = NodePointer.newNodePointer(null, "val2", Locale.getDefault());
        assertFalse(operation.testEqual(ptr1, ptr2));
    }

    @Test
    public void testEqual_scalarAndCollectionOnRight_returnsTrue() {
        Expression left = new Constant("b");
        Expression right = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("a", "b", "c");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testEqual_bothExpressionsAreCollections_returnsMatch() {
        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("1", "2", "3");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("3", "4", "5");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testEqual_bothExpressionsAreCollections_noMatch() {
        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("1", "2");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Expression() {
            public Object compute(EvalContext context) {
                return Arrays.asList("3", "4");
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        assertFalse(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testEqual_evalContextOnRightSide() {
        JXPathContextReferenceImpl ctx = (JXPathContextReferenceImpl) JXPathContext.newContext("nodeValue");
        NodePointer ptr = NodePointer.newNodePointer(null, "nodeValue", Locale.getDefault());
        RootContext root = new RootContext(ctx, ptr);
        final InitialContext initCtx = new InitialContext(root);

        Expression left = new Constant("nodeValue");
        Expression right = new Expression() {
            public Object compute(EvalContext context) {
                return initCtx;
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testEqual_bothExpressionsAreEvalContexts() {
        JXPathContextReferenceImpl ctx1 = (JXPathContextReferenceImpl) JXPathContext.newContext("commonValue");
        NodePointer ptr1 = NodePointer.newNodePointer(null, "commonValue", Locale.getDefault());
        final InitialContext initCtx1 = new InitialContext(new RootContext(ctx1, ptr1));

        JXPathContextReferenceImpl ctx2 = (JXPathContextReferenceImpl) JXPathContext.newContext("commonValue");
        NodePointer ptr2 = NodePointer.newNodePointer(null, "commonValue", Locale.getDefault());
        final InitialContext initCtx2 = new InitialContext(new RootContext(ctx2, ptr2));

        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return initCtx1;
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Expression() {
            public Object compute(EvalContext context) {
                return initCtx2;
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testEqual_selfContextHandling() {
        JXPathContextReferenceImpl ctx = (JXPathContextReferenceImpl) JXPathContext.newContext("selfValue");
        NodePointer ptr = NodePointer.newNodePointer(null, "selfValue", Locale.getDefault());
        InitialContext parentCtx = new InitialContext(new RootContext(ctx, ptr));
        final SelfContext selfCtx = new SelfContext(parentCtx, new NodeTypeTest(Compiler.NODE_TYPE_NODE));

        Expression left = new Expression() {
            public Object compute(EvalContext context) {
                return selfCtx;
            }
            public Object computeValue(EvalContext context) {
                return compute(context);
            }
        };
        Expression right = new Constant("selfValue");
        assertTrue(operation.testEqual(rootEvalContext, left, right));
    }

    @Test
    public void testFindMatch_emptyIterators_returnsFalse() {
        List empty = Collections.emptyList();
        List nonEmpty = Arrays.asList("a", "b");
        assertFalse(operation.testFindMatch(empty.iterator(), nonEmpty.iterator()));
        assertFalse(operation.testFindMatch(nonEmpty.iterator(), empty.iterator()));
    }
}
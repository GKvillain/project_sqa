package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoreOperationRelationalExpressionTest {

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        public TestRelationalExpression(Expression[] args) {
            super(args);
        }

        public Object computeValue(EvalContext context) {
            return null;
        }

        public String getSymbol() {
            return "<=>";
        }

        public int getPrecedence() {
            return super.getPrecedence();
        }

        public boolean isSymmetric() {
            return super.isSymmetric();
        }

        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }
    }

    private static class ConcreteRelationalExpression extends CoreOperationRelationalExpression {
        public ConcreteRelationalExpression(Expression[] args) {
            super(args);
        }

        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }

        public String getSymbol() {
            return "<=";
        }
    }

    // Tests that getPrecedence returns expected operator precedence value 3
    @Test
    public void testGetPrecedence_validInstance_returnsThree() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[] { c1, c2 });
        assertEquals(3, expr.getPrecedence());
    }

    // Tests that isSymmetric returns false for relational operations
    @Test
    public void testIsSymmetric_validInstance_returnsFalse() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[] { c1, c2 });
        assertFalse(expr.isSymmetric());
    }

    // Tests constructor initialization with non-empty expression arguments
    @Test
    public void testConstructor_withArguments_argumentsStoredCorrectly() {
        Constant c1 = new Constant("a");
        Constant c2 = new Constant("b");
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[] { c1, c2 });
        assertNotNull(expr.getArguments());
        assertEquals(2, expr.getArguments().length);
        assertEquals(c1, expr.getArguments()[0]);
        assertEquals(c2, expr.getArguments()[1]);
    }

    // Tests constructor with null arguments array
    @Test
    public void testConstructor_nullArguments_handlesNull() {
        TestRelationalExpression expr = new TestRelationalExpression(null);
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests less-than comparison with numeric values yielding true
    @Test
    public void testCompute_lessThanTrue_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("1 < 2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests less-than comparison with numeric values yielding false
    @Test
    public void testCompute_lessThanFalse_returnsFalse() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("2 < 1");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests greater-than comparison with numeric values yielding true
    @Test
    public void testCompute_greaterThanTrue_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("3 > 2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests greater-than comparison with numeric values yielding false
    @Test
    public void testCompute_greaterThanFalse_returnsFalse() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("2 > 3");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests less-than-or-equal comparison with equal values yielding true
    @Test
    public void testCompute_lessThanOrEqualBoundary_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("2 <= 2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests greater-than-or-equal comparison with equal values yielding true
    @Test
    public void testCompute_greaterThanOrEqualBoundary_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("5 >= 5");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison between integer and double
    @Test
    public void testCompute_mixedNumericTypes_returnsCorrectResult() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("1.5 < 2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison between string representation of number and number
    @Test
    public void testCompute_stringAndNumberComparison_returnsCorrectResult() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("'10' > 2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison with negative numbers
    @Test
    public void testCompute_negativeNumbers_returnsCorrectResult() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("-5 < -2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison with list elements where at least one element satisfies condition
    @Test
    public void testCompute_collectionGreaterThanScalar_returnsTrueWhenMatched() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Arrays.asList(new Integer[] { Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(10) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list > 4");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison with list elements where no element satisfies condition
    @Test
    public void testCompute_collectionLessThanScalar_returnsFalseWhenNoneMatch() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Arrays.asList(new Integer[] { Integer.valueOf(5), Integer.valueOf(10) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list < 1");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison with empty collection
    @Test
    public void testCompute_emptyCollectionComparison_returnsFalse() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Collections.emptyList());
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list > 0");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison with two collections
    @Test
    public void testCompute_collectionToCollectionComparison_returnsCorrectResult() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list1", Arrays.asList(new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }));
        map.put("list2", Arrays.asList(new Integer[] { Integer.valueOf(2), Integer.valueOf(3) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list1 < $list2");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison with scalar on left and collection on right yielding true
    @Test
    public void testCompute_scalarLessThanCollection_returnsTrueWhenMatched() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Arrays.asList(new Integer[] { Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(10) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("4 < $list");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests relational comparison with scalar on left and collection on right yielding false
    @Test
    public void testCompute_scalarGreaterThanCollection_returnsFalseWhenNoneMatch() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Arrays.asList(new Integer[] { Integer.valueOf(1), Integer.valueOf(5) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("10 < $list");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison with scalar on left and empty collection on right
    @Test
    public void testCompute_scalarComparedWithEmptyCollection_returnsFalse() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", Collections.emptyList());
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("0 < $list");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison between two empty collections
    @Test
    public void testCompute_emptyCollectionToEmptyCollection_returnsFalse() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list1", Collections.emptyList());
        map.put("list2", Collections.emptyList());
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list1 < $list2");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison between two collections where no pair satisfies condition
    @Test
    public void testCompute_collectionToCollectionNoMatch_returnsFalse() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list1", Arrays.asList(new Integer[] { Integer.valueOf(10), Integer.valueOf(20) }));
        map.put("list2", Arrays.asList(new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("$list1 < $list2");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests relational comparison with NaN strings returning false
    @Test
    public void testCompute_nanStringComparison_returnsFalse() {
        JXPathContext context = JXPathContext.newContext(new Object());
        assertEquals(Boolean.FALSE, context.getValue("'abc' < 5"));
        assertEquals(Boolean.FALSE, context.getValue("5 < 'abc'"));
        assertEquals(Boolean.FALSE, context.getValue("'foo' > 'bar'"));
        assertEquals(Boolean.FALSE, context.getValue("'foo' <= 'bar'"));
        assertEquals(Boolean.FALSE, context.getValue("'foo' >= 'bar'"));
    }

    // Tests relational comparison with boolean values
    @Test
    public void testCompute_booleanValues_returnsCorrectResult() {
        JXPathContext context = JXPathContext.newContext(new Object());
        assertEquals(Boolean.TRUE, context.getValue("true() > false()"));
        assertEquals(Boolean.FALSE, context.getValue("false() > true()"));
        assertEquals(Boolean.TRUE, context.getValue("true() >= true()"));
    }

    // Tests computeValue directly on ConcreteRelationalExpression using EvalContext
    @Test
    public void testComputeValue_concreteSubclass_evaluatesCorrectly() {
        Constant c1 = new Constant(Integer.valueOf(5));
        Constant c2 = new Constant(Integer.valueOf(10));
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(new Expression[] { c1, c2 });

        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(parentContext, (NodePointer) null);

        Object result = expr.computeValue(rootContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue on ConcreteRelationalExpression when condition is false
    @Test
    public void testComputeValue_concreteSubclassConditionFalse_returnsFalse() {
        Constant c1 = new Constant(Integer.valueOf(15));
        Constant c2 = new Constant(Integer.valueOf(10));
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(new Expression[] { c1, c2 });

        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(parentContext, (NodePointer) null);

        Object result = expr.computeValue(rootContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests path expression node-set comparisons
    @Test
    public void testCompute_nodeSetPathComparison_returnsCorrectResult() {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> child1 = new HashMap<String, Object>();
        child1.put("val", Integer.valueOf(10));
        Map<String, Object> child2 = new HashMap<String, Object>();
        child2.put("val", Integer.valueOf(20));
        map.put("items", Arrays.asList(new Object[] { child1, child2 }));

        JXPathContext context = JXPathContext.newContext(map);
        assertEquals(Boolean.TRUE, context.getValue("items/val > 15"));
        assertEquals(Boolean.FALSE, context.getValue("items/val > 25"));
        assertEquals(Boolean.TRUE, context.getValue("5 < items/val"));
        assertEquals(Boolean.FALSE, context.getValue("25 < items/val"));
    }
}
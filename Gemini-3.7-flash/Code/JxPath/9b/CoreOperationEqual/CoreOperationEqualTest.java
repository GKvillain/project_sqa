package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationEqualTest {

    // Tests getSymbol method returns "="
    @Test
    public void testGetSymbol_returnsEqualSymbol() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        assertEquals("=", op.getSymbol());
    }

    // Tests equality of identical string constants
    @Test
    public void testComputeValue_identicalStrings_returnsTrue() {
        Constant arg1 = new Constant("hello");
        Constant arg2 = new Constant("hello");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests inequality of different string constants
    @Test
    public void testComputeValue_differentStrings_returnsFalse() {
        Constant arg1 = new Constant("hello");
        Constant arg2 = new Constant("world");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality of identical numbers
    @Test
    public void testComputeValue_identicalNumbers_returnsTrue() {
        Constant arg1 = new Constant(new Double(42.0));
        Constant arg2 = new Constant(new Double(42.0));
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests inequality of different numbers
    @Test
    public void testComputeValue_differentNumbers_returnsFalse() {
        Constant arg1 = new Constant(new Double(42.0));
        Constant arg2 = new Constant(new Double(43.0));
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality of identical boolean constants
    @Test
    public void testComputeValue_identicalBooleans_returnsTrue() {
        Constant arg1 = new Constant(Boolean.TRUE);
        Constant arg2 = new Constant(Boolean.TRUE);
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests inequality of different boolean constants
    @Test
    public void testComputeValue_differentBooleans_returnsFalse() {
        Constant arg1 = new Constant(Boolean.TRUE);
        Constant arg2 = new Constant(Boolean.FALSE);
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests comparison between number and string representation of the same number
    @Test
    public void testComputeValue_numberAndEquivalentString_returnsTrue() {
        Constant arg1 = new Constant(new Double(100.0));
        Constant arg2 = new Constant("100");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison between number and string representation of different numbers
    @Test
    public void testComputeValue_numberAndDifferentString_returnsFalse() {
        Constant arg1 = new Constant(new Double(100.0));
        Constant arg2 = new Constant("200");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests comparison between boolean and boolean-convertible string
    @Test
    public void testComputeValue_booleanAndString_returnsCorrectResult() {
        Constant arg1 = new Constant(Boolean.TRUE);
        Constant arg2 = new Constant("true");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison with empty string
    @Test
    public void testComputeValue_emptyStrings_returnsTrue() {
        Constant arg1 = new Constant("");
        Constant arg2 = new Constant("");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality expression evaluation via JXPathContext
    @Test
    public void testEvaluate_xpathEqualExpression_evaluatesTrue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("1 = 1");
        assertTrue(result instanceof Boolean && ((Boolean) result).booleanValue());
    }

    // Tests inequality expression evaluation via JXPathContext
    @Test
    public void testEvaluate_xpathNotEqualExpression_evaluatesFalse() {
        JXPathContext context = JXPathContext.newContext(new Object());
        Object result = context.getValue("1 = 2");
        assertFalse(result instanceof Boolean && ((Boolean) result).booleanValue());
    }

    // Tests comparison involving NaN values
    @Test
    public void testComputeValue_nanComparison_returnsFalse() {
        Constant arg1 = new Constant(new Double(Double.NaN));
        Constant arg2 = new Constant(new Double(Double.NaN));
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests node-set equals scalar when matching element is present
    @Test
    public void testEvaluate_nodeSetEqualsScalar_matchFound() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("items", Arrays.asList("apple", "banana", "cherry"));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("items = 'banana'");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests node-set equals scalar when no matching element is present
    @Test
    public void testEvaluate_nodeSetEqualsScalar_noMatchFound() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("items", Arrays.asList("apple", "banana", "cherry"));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("items = 'orange'");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests two node-sets comparison when overlap exists
    @Test
    public void testEvaluate_twoNodeSetsComparison_withOverlap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("listA", Arrays.asList("x", "y", "z"));
        map.put("listB", Arrays.asList("a", "b", "y"));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("listA = listB'");
        // listB' is invalid syntax, use listA = listB
        result = context.getValue("listA = listB");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests two node-sets comparison when no overlap exists
    @Test
    public void testEvaluate_twoNodeSetsComparison_withoutOverlap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("listA", Arrays.asList("x", "y"));
        map.put("listB", Arrays.asList("a", "b"));
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("listA = listB");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests empty node-set comparison
    @Test
    public void testEvaluate_emptyNodeSet_returnsFalse() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("emptyList", Collections.emptyList());
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("emptyList = 'anything'");
        assertEquals(Boolean.FALSE, result);
    }

    // Tests comparison with null context variables
    @Test
    public void testEvaluate_nullVariableComparison() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("nullVal", null);
        JXPathContext context = JXPathContext.newContext(map);
        Object result = context.getValue("nullVal = nullVal");
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison between boolean and node-set (XPath boolean conversion rule)
    @Test
    public void testEvaluate_booleanAndNodeSetComparison() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("items", Arrays.asList("a", "b"));
        JXPathContext context = JXPathContext.newContext(map);
        // Non-empty node-set converts to true as boolean
        Object resultTrue = context.getValue("true() = items");
        assertEquals(Boolean.TRUE, resultTrue);

        map.put("emptyItems", Collections.emptyList());
        Object resultFalse = context.getValue("true() = emptyItems");
        assertEquals(Boolean.FALSE, resultFalse);
    }
}
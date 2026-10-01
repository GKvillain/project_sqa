package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for {@link CoreOperationEqual}.
 */
public class CoreOperationEqualTest {

    private JXPathContext context;
    private EvalContext evalContext;

    @Before
    public void setUp() {
        context = JXPathContext.createContext(new Object(), null);
        evalContext = context.getEvalContext();
    }

    // Tests getSymbol returns "="
    @Test
    public void testGetSymbol_always_returnsEquals() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("x"), new Constant("y"));
        assertEquals("=", op.getSymbol());
    }

    // Tests computeValue with two equal numbers
    @Test
    public void testComputeValue_equalNumbers_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(1), new Constant(1));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with two unequal numbers
    @Test
    public void testComputeValue_unequalNumbers_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(1), new Constant(2));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with two equal strings
    @Test
    public void testComputeValue_equalStrings_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("abc"), new Constant("abc"));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with two unequal strings
    @Test
    public void testComputeValue_unequalStrings_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("abc"), new Constant("xyz"));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with two equal booleans
    @Test
    public void testComputeValue_equalBooleans_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Boolean.TRUE), new Constant(Boolean.TRUE));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with two unequal booleans
    @Test
    public void testComputeValue_unequalBooleans_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Boolean.TRUE), new Constant(Boolean.FALSE));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with null and null (both null)
    @Test
    public void testComputeValue_nullEqualsNull_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(null), new Constant(null));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with null and non-null string
    @Test
    public void testComputeValue_nullEqualsString_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(null), new Constant("a"));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with non-null string and null
    @Test
    public void testComputeValue_stringEqualsNull_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("a"), new Constant(null));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with empty string equals empty string
    @Test
    public void testComputeValue_emptyStringEqualsEmptyString_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(""), new Constant(""));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with empty string and null
    @Test
    public void testComputeValue_emptyStringEqualsNull_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(""), new Constant(null));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with zero equals zero
    @Test
    public void testComputeValue_zeroEqualsZero_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(0), new Constant(0));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with negative equals negative
    @Test
    public void testComputeValue_negativeEqualsNegative_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(-1), new Constant(-1));
        assertTrue((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with number and string (type mismatch)
    @Test
    public void testComputeValue_numberEqualsString_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(1), new Constant("1"));
        assertFalse((Boolean) op.computeValue(evalContext));
    }

    // Tests computeValue with integer and double of same numeric value
    @Test
    public void testComputeValue_integerEqualsDouble_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(2), new Constant(2.0));
        assertTrue((Boolean) op.computeValue(evalContext));
    }
}
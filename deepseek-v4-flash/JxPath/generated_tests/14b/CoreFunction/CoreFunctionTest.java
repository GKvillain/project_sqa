package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;

public class CoreFunctionTest {

    private JXPathContext context;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream("<root/>".getBytes()));
        context = JXPathContext.newContext(doc);
    }

    // Tests for basic CoreFunction methods

    @Test
    public void testGetFunctionCode() {
        CoreFunction cf = new CoreFunction(Compiler.FUNCTION_LAST, null);
        assertEquals(Compiler.FUNCTION_LAST, cf.getFunctionCode());
    }

    @Test
    public void testGetFunctionName_knownFunctions() {
        assertEquals("last", new CoreFunction(Compiler.FUNCTION_LAST, null).getFunctionName());
        assertEquals("position", new CoreFunction(Compiler.FUNCTION_POSITION, null).getFunctionName());
        assertEquals("count", new CoreFunction(Compiler.FUNCTION_COUNT, null).getFunctionName());
        assertEquals("string", new CoreFunction(Compiler.FUNCTION_STRING, null).getFunctionName());
        assertEquals("format-number", new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, null).getFunctionName());
    }

    @Test
    public void testGetFunctionName_unknownCode_returnsUnknown() {
        String name = new CoreFunction(999, null).getFunctionName();
        assertTrue(name.startsWith("unknownFunction"));
    }

    @Test
    public void testGetArgumentCount_nullArgs_returnsZero() {
        assertEquals(0, new CoreFunction(Compiler.FUNCTION_LAST, null).getArgumentCount());
    }

    @Test
    public void testGetArgumentCount_withArgs_returnsCorrectCount() {
        Expression[] args = new Expression[]{new NumberConstant(1), new StringConstant("x")};
        CoreFunction cf = new CoreFunction(Compiler.FUNCTION_CONCAT, args);
        assertEquals(2, cf.getArgumentCount());
    }

    @Test
    public void testComputeContextDependent_lastAndPosition_returnsTrue() {
        assertTrue(new CoreFunction(Compiler.FUNCTION_LAST, null).computeContextDependent());
        assertTrue(new CoreFunction(Compiler.FUNCTION_POSITION, null).computeContextDependent());
    }

    @Test
    public void testComputeContextDependent_boolean_noArgs_returnsTrue() {
        assertTrue(new CoreFunction(Compiler.FUNCTION_BOOLEAN, null).computeContextDependent());
    }

    @Test
    public void testComputeContextDependent_boolean_withArgs_returnsFalse() {
        Expression[] args = new Expression[]{new NumberConstant(1)};
        assertFalse(new CoreFunction(Compiler.FUNCTION_BOOLEAN, args).computeContextDependent());
    }

    @Test
    public void testComputeContextDependent_formatNumber_2args_returnsTrue() {
        Expression[] args = new Expression[]{new NumberConstant(2), new StringConstant("0")};
        assertTrue(new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, args).computeContextDependent());
    }

    @Test
    public void testComputeContextDependent_formatNumber_3args_returnsFalse() {
        Expression[] args = new Expression[]{new NumberConstant(2), new StringConstant("0"), new StringConstant("s")};
        assertFalse(new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, args).computeContextDependent());
    }

    @Test
    public void testToString_withArgs_containsFunctionName() {
        Expression[] args = new Expression[]{new NumberConstant(1.5), new StringConstant("test")};
        String s = new CoreFunction(Compiler.FUNCTION_CONCAT, args).toString();
        assertTrue(s.startsWith("concat("));
        assertTrue(s.contains("1.5"));
        assertTrue(s.contains("'test'"));
        assertTrue(s.endsWith(")"));
    }

    // Integration tests using JXPathContext evaluation

    @Test
    public void testStringLength_function_returnsCorrectLength() {
        Object result = context.evaluate("string-length('hello')");
        assertEquals(5.0, ((Number)result).doubleValue(), 0.0);
    }

    @Test
    public void testConcat_function_returnsConcatenatedString() {
        Object result = context.evaluate("concat('Hello', ' ', 'World')");
        assertEquals("Hello World", result);
    }

    @Test
    public void testStartsWith_function_matching_returnsTrue() {
        Object result = context.evaluate("starts-with('Hello World', 'Hello')");
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testSubstringBefore_function_returnsSubstring() {
        Object result = context.evaluate("substring-before('1999/04/01', '/')");
        assertEquals("1999", result);
    }

    @Test
    public void testNormalizeSpace_function_removesExtraSpaces() {
        Object result = context.evaluate("normalize-space('  Hello   World  ')");
        assertEquals("Hello World", result);
    }

    @Test
    public void testTranslate_function_returnsTranslatedString() {
        Object result = context.evaluate("translate('Hello', 'el', 'ip')");
        assertEquals("Hippo", result);
    }

    @Test
    public void testBoolean_function_number_returnsTrue() {
        Object result = context.evaluate("boolean(1)");
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testNumber_function_string_returnsNumeric() {
        Object result = context.evaluate("number('123.45')");
        assertEquals(123.45, ((Number)result).doubleValue(), 0.001);
    }

    @Test
    public void testFloor_function_returnsFloor() {
        Object result = context.evaluate("floor(1.9)");
        assertEquals(1.0, ((Number)result).doubleValue(), 0.0);
    }

    @Test
    public void testRound_function_returnsRounded() {
        Object result = context.evaluate("round(1.5)");
        assertEquals(2.0, ((Number)result).doubleValue(), 0.0);
    }

    // Primary defect-detecting test for format-number (Defects4J bug 14b)
    @Test
    public void testFormatNumber_function_returnsFormattedString() {
        Object result = context.evaluate("format-number(5, '0')");
        assertNotNull(result);
        assertEquals("5", result.toString());
    }

    @Test(expected = JXPathException.class)
    public void testStringLength_invalidArgCount_throwsException() {
        context.evaluate("string-length('a', 'b')");
    }
}
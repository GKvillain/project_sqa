package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.Compiler;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreFunctionTest {

    // Tests functionFloor with positive and negative decimal numbers
    @Test
    public void testFunctionFloor_decimalValues_returnsFlooredDouble() {
        CoreFunction floor1 = new CoreFunction(Compiler.FUNCTION_FLOOR,
                new Expression[]{new Constant(new Double(1.7))});
        assertEquals(new Double(1.0), floor1.computeValue(null));

        CoreFunction floor2 = new CoreFunction(Compiler.FUNCTION_FLOOR,
                new Expression[]{new Constant(new Double(-1.2))});
        assertEquals(new Double(-2.0), floor2.computeValue(null));
    }

    // Tests functionCeiling with positive and negative decimal numbers
    @Test
    public void testFunctionCeiling_decimalValues_returnsCeiledDouble() {
        CoreFunction ceil1 = new CoreFunction(Compiler.FUNCTION_CEILING,
                new Expression[]{new Constant(new Double(1.2))});
        assertEquals(new Double(2.0), ceil1.computeValue(null));

        CoreFunction ceil2 = new CoreFunction(Compiler.FUNCTION_CEILING,
                new Expression[]{new Constant(new Double(-1.7))});
        assertEquals(new Double(-1.0), ceil2.computeValue(null));
    }

    // Tests functionRound with standard positive and negative values
    @Test
    public void testFunctionRound_standardValues_returnsRoundedDouble() {
        CoreFunction round1 = new CoreFunction(Compiler.FUNCTION_ROUND,
                new Expression[]{new Constant(new Double(1.5))});
        assertEquals(new Double(2.0), round1.computeValue(null));

        CoreFunction round2 = new CoreFunction(Compiler.FUNCTION_ROUND,
                new Expression[]{new Constant(new Double(1.4))});
        assertEquals(new Double(1.0), round2.computeValue(null));
    }

    // Tests functionRound with NaN boundary value
    @Test
    public void testFunctionRound_nanValue_returnsNanDouble() {
        CoreFunction roundNan = new CoreFunction(Compiler.FUNCTION_ROUND,
                new Expression[]{new Constant(new Double(Double.NaN))});
        Object result = roundNan.computeValue(null);
        assertTrue(result instanceof Double);
        assertTrue(Double.isNaN(((Double) result).doubleValue()));
    }

    // Tests functionSubstring with 2 arguments
    @Test
    public void testFunctionSubstring_twoArguments_returnsSuffixString() {
        CoreFunction substring = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("12345"), new Constant(new Double(2))});
        assertEquals("2345", substring.computeValue(null));
    }

    // Tests functionSubstring with 3 arguments including edge boundaries
    @Test
    public void testFunctionSubstring_threeArguments_returnsExpectedSubstring() {
        CoreFunction substring = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("12345"), new Constant(new Double(2)), new Constant(new Double(3))});
        assertEquals("234", substring.computeValue(null));

        CoreFunction substringNegativeFrom = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("12345"), new Constant(new Double(-1)), new Constant(new Double(3))});
        assertEquals("1", substringNegativeFrom.computeValue(null));

        CoreFunction substringNanFrom = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("12345"), new Constant(new Double(Double.NaN)), new Constant(new Double(3))});
        assertEquals("", substringNanFrom.computeValue(null));

        CoreFunction substringNegativeLength = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("12345"), new Constant(new Double(1)), new Constant(new Double(-2))});
        assertEquals("", substringNegativeLength.computeValue(null));
    }

    // Tests functionSubstringBefore and functionSubstringAfter
    @Test
    public void testFunctionSubstringBeforeAndAfter_matchingAndNonMatchingPatterns() {
        CoreFunction beforeMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE,
                new Expression[]{new Constant("1999/04/01"), new Constant("/")});
        assertEquals("1999", beforeMatch.computeValue(null));

        CoreFunction beforeNoMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE,
                new Expression[]{new Constant("1999/04/01"), new Constant("?")});
        assertEquals("", beforeNoMatch.computeValue(null));

        CoreFunction afterMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER,
                new Expression[]{new Constant("1999/04/01"), new Constant("/")});
        assertEquals("04/01", afterMatch.computeValue(null));

        CoreFunction afterNoMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER,
                new Expression[]{new Constant("1999/04/01"), new Constant("?")});
        assertEquals("", afterNoMatch.computeValue(null));
    }

    // Tests startsWith and contains functions
    @Test
    public void testFunctionStartsWithAndContains_trueAndFalseCases() {
        CoreFunction startsWithTrue = new CoreFunction(Compiler.FUNCTION_STARTS_WITH,
                new Expression[]{new Constant("hello world"), new Constant("hello")});
        assertEquals(Boolean.TRUE, startsWithTrue.computeValue(null));

        CoreFunction startsWithFalse = new CoreFunction(Compiler.FUNCTION_STARTS_WITH,
                new Expression[]{new Constant("hello world"), new Constant("world")});
        assertEquals(Boolean.FALSE, startsWithFalse.computeValue(null));

        CoreFunction containsTrue = new CoreFunction(Compiler.FUNCTION_CONTAINS,
                new Expression[]{new Constant("hello world"), new Constant("lo wo")});
        assertEquals(Boolean.TRUE, containsTrue.computeValue(null));

        CoreFunction containsFalse = new CoreFunction(Compiler.FUNCTION_CONTAINS,
                new Expression[]{new Constant("hello world"), new Constant("xyz")});
        assertEquals(Boolean.FALSE, containsFalse.computeValue(null));
    }

    // Tests functionConcat with multiple arguments
    @Test
    public void testFunctionConcat_multipleStrings_returnsConcatenatedString() {
        CoreFunction concat = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("a"), new Constant("b"), new Constant("c")});
        assertEquals("abc", concat.computeValue(null));
    }

    // Tests functionStringLength with constant string
    @Test
    public void testFunctionStringLength_validString_returnsLength() {
        CoreFunction stringLength = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH,
                new Expression[]{new Constant("hello")});
        assertEquals(new Double(5.0), stringLength.computeValue(null));
    }

    // Tests functionNormalizeSpace with multiple whitespaces, tabs, and newlines
    @Test
    public void testFunctionNormalizeSpace_spacedString_returnsNormalizedString() {
        CoreFunction norm = new CoreFunction(Compiler.FUNCTION_NORMALIZE_SPACE,
                new Expression[]{new Constant(" \t  hello \n world   ")});
        assertEquals("hello world", norm.computeValue(null));
    }

    // Tests functionTranslate with character replacement and deletion
    @Test
    public void testFunctionTranslate_validMapping_returnsTranslatedString() {
        CoreFunction translate = new CoreFunction(Compiler.FUNCTION_TRANSLATE,
                new Expression[]{new Constant("--aaa--"), new Constant("abc-"), new Constant("ABC")});
        assertEquals("AAA", translate.computeValue(null));
    }

    // Tests boolean logic functions: boolean, not, true, false, and null
    @Test
    public void testBooleanLogicFunctions_variousInputs_returnsExpectedBooleans() {
        CoreFunction boolTrue = new CoreFunction(Compiler.FUNCTION_BOOLEAN,
                new Expression[]{new Constant(new Double(1))});
        assertEquals(Boolean.TRUE, boolTrue.computeValue(null));

        CoreFunction notTrue = new CoreFunction(Compiler.FUNCTION_NOT,
                new Expression[]{new Constant(new Double(1))});
        assertEquals(Boolean.FALSE, notTrue.computeValue(null));

        CoreFunction fnTrue = new CoreFunction(Compiler.FUNCTION_TRUE, null);
        assertEquals(Boolean.TRUE, fnTrue.computeValue(null));

        CoreFunction fnFalse = new CoreFunction(Compiler.FUNCTION_FALSE, null);
        assertEquals(Boolean.FALSE, fnFalse.computeValue(null));

        CoreFunction fnNull = new CoreFunction(Compiler.FUNCTION_NULL, null);
        assertNull(fnNull.computeValue(null));
    }

    // Tests functionNumber conversion from string to double
    @Test
    public void testFunctionNumber_stringInput_returnsParsedNumber() {
        CoreFunction fnNumber = new CoreFunction(Compiler.FUNCTION_NUMBER,
                new Expression[]{new Constant("123.45")});
        assertEquals(new Double(123.45), fnNumber.computeValue(null));
    }

    // Tests toString, getFunctionName, and argument accessors
    @Test
    public void testToStringAndAccessors_validFunction_returnsCorrectMetadata() {
        CoreFunction fn = new CoreFunction(Compiler.FUNCTION_STARTS_WITH,
                new Expression[]{new Constant("abc"), new Constant("a")});
        assertEquals(Compiler.FUNCTION_STARTS_WITH, fn.getFunctionCode());
        assertEquals("starts-with", fn.getFunctionName());
        assertEquals(2, fn.getArgumentCount());
        assertNotNull(fn.getArg1());
        assertNotNull(fn.getArg2());
        assertEquals("starts-with('abc', 'a')", fn.toString());

        CoreFunction unknownFn = new CoreFunction(9999, null);
        assertEquals("unknownFunction9999()", unknownFn.getFunctionName());
        assertEquals(0, unknownFn.getArgumentCount());
    }

    // Tests computeContextDependent for various built-in function codes
    @Test
    public void testComputeContextDependent_variousFunctionTypes_returnsCorrectDependency() {
        CoreFunction lastFn = new CoreFunction(Compiler.FUNCTION_LAST, null);
        assertTrue(lastFn.computeContextDependent());

        CoreFunction positionFn = new CoreFunction(Compiler.FUNCTION_POSITION, null);
        assertTrue(positionFn.computeContextDependent());

        CoreFunction stringNoArgs = new CoreFunction(Compiler.FUNCTION_STRING, null);
        assertTrue(stringNoArgs.computeContextDependent());

        CoreFunction stringWithArgs = new CoreFunction(Compiler.FUNCTION_STRING,
                new Expression[]{new Constant("test")});
        assertFalse(stringWithArgs.computeContextDependent());

        CoreFunction formatNumber2Args = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(new Double(1)), new Constant("#")});
        assertTrue(formatNumber2Args.computeContextDependent());

        CoreFunction concatFn = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("a"), new Constant("b")});
        assertFalse(concatFn.computeContextDependent());
    }

    // Tests exception path when argument count is invalid
    @Test(expected = JXPathInvalidSyntaxException.class)
    public void testAssertArgCount_insufficientArguments_throwsException() {
        CoreFunction invalidConcat = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("onlyOneArg")});
        invalidConcat.computeValue(null);
    }
}
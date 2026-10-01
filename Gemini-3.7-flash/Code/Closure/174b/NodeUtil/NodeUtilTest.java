package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

    // Tests pure boolean value evaluation for primitive literals and edge cases
    @Test
    public void testGetPureBooleanValue_literals_returnsCorrectTernaryValue() {
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.string("non-empty")));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.string("")));
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.number(1.0)));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.number(0.0)));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.nullNode()));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.falseNode()));
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.trueNode()));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("undefined")));
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("NaN")));
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.name("Infinity")));
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(IR.name("customVar")));
    }

    // Tests impure boolean value evaluation including logical operators and hook expressions
    @Test
    public void testGetImpureBooleanValue_logicalAndHookNodes_returnsExpectedValue() {
        Node andNode = IR.and(IR.trueNode(), IR.falseNode());
        assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

        Node orNode = IR.or(IR.trueNode(), IR.falseNode());
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

        Node notNode = IR.not(IR.trueNode());
        assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notNode));

        Node hookNodeSame = IR.hook(IR.name("cond"), IR.trueNode(), IR.trueNode());
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookNodeSame));

        Node hookNodeDiff = IR.hook(IR.name("cond"), IR.trueNode(), IR.falseNode());
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookNodeDiff));

        Node assignNode = IR.assign(IR.name("x"), IR.trueNode());
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assignNode));
    }

    // Tests string conversion for various AST nodes and arrays
    @Test
    public void testGetStringValue_variousNodes_returnsExpectedStrings() {
        assertEquals("hello", NodeUtil.getStringValue(IR.string("hello")));
        assertEquals("123", NodeUtil.getStringValue(IR.number(123.0)));
        assertEquals("123.45", NodeUtil.getStringValue(IR.number(123.45)));
        assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
        assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
        assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
        assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
        assertEquals("[object Object]", NodeUtil.getStringValue(IR.objectlit()));

        Node arrayNode = IR.arraylit(IR.string("a"), IR.nullNode(), IR.number(1.0));
        assertEquals("a,,1", NodeUtil.getStringValue(arrayNode));
        assertNull(NodeUtil.getStringValue(IR.add(IR.number(1), IR.number(2))));
    }

    // Tests string to number conversions with hex, whitespaces, and invalid formats
    @Test
    public void testGetStringNumberValue_variousFormats_returnsParsedDoubles() {
        assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
        assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
        assertEquals(Double.valueOf(42.0), NodeUtil.getStringNumberValue("  42  "));
        assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
        assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0X10"));
        assertNull(NodeUtil.getStringNumberValue("+0x10"));
        assertNull(NodeUtil.getStringNumberValue("-0x10"));
        assertNull(NodeUtil.getStringNumberValue("infinity"));
        assertNull(NodeUtil.getStringNumberValue("a\u000bb"));
        assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_number")));
    }

    // Tests number value extraction from AST nodes
    @Test
    public void testGetNumberValue_variousNodes_returnsExpectedNumber() {
        assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
        assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
        assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
        assertEquals(Double.valueOf(3.14), NodeUtil.getNumberValue(IR.number(3.14)));
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));
        assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
        assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));

        Node negInf = IR.neg(IR.name("Infinity"));
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

        Node notNode = IR.not(IR.trueNode());
        assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notNode));
    }

    // Tests immutable and literal values determination
    @Test
    public void testIsImmutableAndLiteralValue_variousNodes_returnsCorrectBooleans() {
        assertTrue(NodeUtil.isImmutableValue(IR.string("test")));
        assertTrue(NodeUtil.isImmutableValue(IR.number(100)));
        assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
        assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
        assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
        assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
        assertFalse(NodeUtil.isImmutableValue(IR.name("myVar")));

        assertTrue(NodeUtil.isLiteralValue(IR.arraylit(IR.number(1), IR.string("x")), false));
        assertFalse(NodeUtil.isLiteralValue(IR.arraylit(IR.name("variable")), false));
        assertTrue(NodeUtil.isLiteralValue(IR.regexp(IR.string("abc")), false));
    }

    // Tests valid define value validation for compiler defines
    @Test
    public void testIsValidDefineValue_expressionsAndNames_validatesCorrectly() {
        Set<String> defines = new HashSet<String>();
        defines.add("DEF_A");
        defines.add("DEF_B");

        assertTrue(NodeUtil.isValidDefineValue(IR.string("str"), defines));
        assertTrue(NodeUtil.isValidDefineValue(IR.number(10), defines));
        assertTrue(NodeUtil.isValidDefineValue(IR.trueNode(), defines));
        assertTrue(NodeUtil.isValidDefineValue(IR.name("DEF_A"), defines));
        assertFalse(NodeUtil.isValidDefineValue(IR.name("UNDEFINED_DEF"), defines));

        Node addNode = IR.add(IR.name("DEF_A"), IR.number(5));
        assertTrue(NodeUtil.isValidDefineValue(addNode, defines));

        Node notNode = IR.not(IR.name("DEF_B"));
        assertTrue(NodeUtil.isValidDefineValue(notNode, defines));

        Node invalidExpr = IR.add(IR.name("UNKNOWN_DEF"), IR.number(5));
        assertFalse(NodeUtil.isValidDefineValue(invalidExpr, defines));
    }

    // Tests operator classification: simple, symmetric, relational, commutative, associative
    @Test
    public void testOperatorProperties_variousTokens_classifiesCorrectly() {
        assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
        assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
        assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));

        assertTrue(NodeUtil.isSymmetricOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.EQ)));
        assertTrue(NodeUtil.isSymmetricOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.MUL)));
        assertFalse(NodeUtil.isSymmetricOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.ADD)));

        assertTrue(NodeUtil.isRelationalOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.GT)));
        assertTrue(NodeUtil.isRelationalOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.LE)));
        assertFalse(NodeUtil.isRelationalOperation(IR.add(IR.number(1), IR.number(2)).setType(Token.EQ)));

        assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
        assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
        assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
        assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
        assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.ADD));

        assertTrue(NodeUtil.isAssociative(Token.MUL));
        assertTrue(NodeUtil.isAssociative(Token.AND));
        assertFalse(NodeUtil.isAssociative(Token.ADD));

        assertTrue(NodeUtil.isCommutative(Token.MUL));
        assertFalse(NodeUtil.isCommutative(Token.ADD));
    }

    // Tests result type deduction: numeric, boolean, string
    @Test
    public void testResultTypePredicates_variousNodes_identifiesTypesAccurately() {
        assertTrue(NodeUtil.isNumericResult(IR.number(5)));
        assertTrue(NodeUtil.isNumericResult(IR.sub(IR.number(10), IR.number(5))));
        assertTrue(NodeUtil.isNumericResult(IR.name("NaN")));
        assertTrue(NodeUtil.isNumericResult(IR.name("Infinity")));
        assertFalse(NodeUtil.isNumericResult(IR.string("test")));

        assertTrue(NodeUtil.isBooleanResult(IR.trueNode()));
        assertTrue(NodeUtil.isBooleanResult(IR.falseNode()));
        assertTrue(NodeUtil.isBooleanResult(IR.eq(IR.name("a"), IR.name("b"))));
        assertTrue(NodeUtil.isBooleanResult(IR.not(IR.name("a"))));
        assertFalse(NodeUtil.isBooleanResult(IR.number(1)));

        assertTrue(NodeUtil.mayBeString(IR.string("text")));
        assertTrue(NodeUtil.mayBeString(IR.name("unknownVar")));
        assertFalse(NodeUtil.mayBeString(IR.number(123)));
        assertFalse(NodeUtil.mayBeString(IR.trueNode()));
    }

    // Tests side-effect detection on literals, operations, and function calls
    @Test
    public void testMayHaveSideEffects_variousNodes_returnsCorrectDetection() {
        assertFalse(NodeUtil.mayHaveSideEffects(IR.number(1)));
        assertFalse(NodeUtil.mayHaveSideEffects(IR.string("abc")));
        assertFalse(NodeUtil.mayHaveSideEffects(IR.add(IR.number(1), IR.number(2))));
        assertFalse(NodeUtil.mayHaveSideEffects(IR.arraylit(IR.number(1), IR.number(2))));

        assertTrue(NodeUtil.mayHaveSideEffects(IR.throwNode(IR.string("err"))));
        assertTrue(NodeUtil.mayHaveSideEffects(IR.assign(IR.name("x"), IR.number(1))));

        Node callMathFloor = IR.call(IR.getprop(IR.name("Math"), IR.string("floor")));
        assertFalse(NodeUtil.mayHaveSideEffects(callMathFloor));

        Node callCustom = IR.call(IR.name("customFunction"));
        assertTrue(NodeUtil.mayHaveSideEffects(callCustom));

        Node newArray = IR.newNode(IR.name("Array"));
        assertFalse(NodeUtil.mayHaveSideEffects(newArray));

        Node newCustom = IR.newNode(IR.name("CustomType"));
        assertTrue(NodeUtil.mayHaveSideEffects(newCustom));
    }

    // Tests assignment operator checks and extraction of underlying operator
    @Test
    public void testAssignmentOps_assignmentNodes_detectsAndExtractsCorrectly() {
        assertTrue(NodeUtil.isAssignmentOp(IR.assign(IR.name("a"), IR.number(1))));
        assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD, IR.name("a"), IR.number(1))));
        assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR, IR.name("a"), IR.number(1))));
        assertFalse(NodeUtil.isAssignmentOp(IR.add(IR.number(1), IR.number(2))));

        assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD, IR.name("a"), IR.number(1))));
        assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB, IR.name("a"), IR.number(1))));
        assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL, IR.name("a"), IR.number(1))));
    }

    // Tests exception path for invalid assignment op
    @Test(expected = IllegalArgumentException.class)
    public void testGetOpFromAssignmentOp_nonAssignOp_throwsException() {
        NodeUtil.getOpFromAssignmentOp(IR.add(IR.number(1), IR.number(2)));
    }

    // Tests control structures and loop block identification
    @Test
    public void testControlStructures_variousNodes_identifiesStructureAndBlocks() {
        Node forNode = IR.forNode(IR.var(IR.name("i"), IR.number(0)), IR.lt(IR.name("i"), IR.number(10)), IR.inc(IR.name("i"), false), IR.block());
        assertTrue(NodeUtil.isControlStructure(forNode));
        assertTrue(NodeUtil.isLoopStructure(forNode));
        assertNotNull(NodeUtil.getLoopCodeBlock(forNode));

        Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
        assertTrue(NodeUtil.isLoopStructure(whileNode));
        assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));

        Node doNode = IR.doNode(IR.block(), IR.trueNode());
        assertTrue(NodeUtil.isLoopStructure(doNode));
        assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));

        Node ifNode = IR.ifNode(IR.trueNode(), IR.block());
        assertTrue(NodeUtil.isControlStructure(ifNode));
        assertFalse(NodeUtil.isLoopStructure(ifNode));
        assertNull(NodeUtil.getLoopCodeBlock(ifNode));
    }

    // Tests identifier and qualified name validations
    @Test
    public void testNameValidation_validAndInvalidNames_validatesCorrectly() {
        assertTrue(NodeUtil.isValidSimpleName("foo"));
        assertTrue(NodeUtil.isValidSimpleName("$var_1"));
        assertFalse(NodeUtil.isValidSimpleName("class"));
        assertFalse(NodeUtil.isValidSimpleName("123abc"));
        assertFalse(NodeUtil.isValidSimpleName("foo\u0100"));

        assertTrue(NodeUtil.isValidQualifiedName("foo.bar.baz"));
        assertTrue(NodeUtil.isValidQualifiedName("a.b"));
        assertFalse(NodeUtil.isValidQualifiedName(".foo"));
        assertFalse(NodeUtil.isValidQualifiedName("foo."));
        assertFalse(NodeUtil.isValidQualifiedName("foo..bar"));
        assertFalse(NodeUtil.isValidQualifiedName("foo.class.bar"));

        assertTrue(NodeUtil.isLatin("asciiOnly"));
        assertFalse(NodeUtil.isLatin("unicode\u0080"));
    }

    // Tests precedence retrieval and error handling for unknown type
    @Test
    public void testPrecedence_variousTokens_returnsCorrectPrecedence() {
        assertEquals(0, NodeUtil.precedence(Token.COMMA));
        assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
        assertEquals(2, NodeUtil.precedence(Token.HOOK));
        assertEquals(3, NodeUtil.precedence(Token.OR));
        assertEquals(4, NodeUtil.precedence(Token.AND));
        assertEquals(8, NodeUtil.precedence(Token.EQ));
        assertEquals(11, NodeUtil.precedence(Token.ADD));
        assertEquals(12, NodeUtil.precedence(Token.MUL));
        assertEquals(15, NodeUtil.precedence(Token.NAME));
    }

    // Tests exception on precedence for invalid token
    @Test(expected = Error.class)
    public void testPrecedence_unknownToken_throwsError() {
        NodeUtil.precedence(Token.SCRIPT);
    }

    // Tests opToStr conversion and fail handling
    @Test
    public void testOpToStr_tokens_returnsCorrectOperatorString() {
        assertEquals("+", NodeUtil.opToStr(Token.ADD));
        assertEquals("-", NodeUtil.opToStr(Token.SUB));
        assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
        assertEquals("==", NodeUtil.opToStr(Token.EQ));
        assertEquals("void", NodeUtil.opToStr(Token.VOID));
        assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
        assertNull(NodeUtil.opToStr(Token.NAME));
        assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
    }

    // Tests opToStrNoFail exception for invalid operator
    @Test(expected = Error.class)
    public void testOpToStrNoFail_nonOperator_throwsError() {
        NodeUtil.opToStrNoFail(Token.NAME);
    }

    // Tests LValue identification and BestLValue navigation
    @Test
    public void testLValueAndBestLValue_astNodes_resolvesCorrectly() {
        Node varName = IR.name("x");
        Node varNode = IR.var(varName, IR.number(1));
        assertTrue(NodeUtil.isLValue(varName));
        assertTrue(NodeUtil.isVarDeclaration(varName));
        assertEquals(IR.number(1).getDouble(), NodeUtil.getAssignedValue(varName).getDouble(), 0.0);

        Node assignTarget = IR.name("y");
        Node assignVal = IR.number(2);
        Node assignNode = IR.assign(assignTarget, assignVal);
        assertTrue(NodeUtil.isLValue(assignTarget));
        assertEquals(assignTarget, NodeUtil.getBestLValue(assignVal));
        assertEquals("y", NodeUtil.getBestLValueName(assignTarget));

        Node objKey = IR.stringKey("prop", IR.number(3));
        assertEquals("prop", NodeUtil.getObjectLitKeyName(objKey));
        assertTrue(NodeUtil.isObjectLitKey(objKey));
    }

    // Tests removing AST children safely under various structures
    @Test
    public void testRemoveChild_blockAndVarStatements_removesCorrectly() {
        Node block = IR.block();
        Node stmt1 = IR.exprResult(IR.number(1));
        Node stmt2 = IR.exprResult(IR.number(2));
        block.addChildToBack(stmt1);
        block.addChildToBack(stmt2);

        NodeUtil.removeChild(block, stmt1);
        assertEquals(1, block.getChildCount());
        assertEquals(stmt2, block.getFirstChild());

        Node script = IR.script();
        Node var1 = IR.name("a");
        Node var2 = IR.name("b");
        Node varNode = IR.var(var1, var2);
        script.addChildToBack(varNode);

        NodeUtil.removeChild(varNode, var1);
        assertEquals(1, varNode.getChildCount());
        assertEquals(var2, varNode.getFirstChild());
    }

    // Tests merging child block into parent block
    @Test
    public void testTryMergeBlock_nestedBlock_mergesSuccessfully() {
        Node parentBlock = IR.block();
        Node childBlock = IR.block();
        Node stmtA = IR.exprResult(IR.string("A"));
        Node stmtB = IR.exprResult(IR.string("B"));
        childBlock.addChildToBack(stmtA);
        childBlock.addChildToBack(stmtB);
        parentBlock.addChildToBack(childBlock);

        boolean merged = NodeUtil.tryMergeBlock(childBlock);
        assertTrue(merged);
        assertEquals(2, parentBlock.getChildCount());
        assertEquals(stmtA, parentBlock.getFirstChild());
        assertEquals(stmtB, parentBlock.getLastChild());
    }
}
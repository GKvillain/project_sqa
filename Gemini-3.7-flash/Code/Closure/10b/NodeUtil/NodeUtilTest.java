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

  // Tests mayBeString for conditional/hook and arithmetic expressions (Defects4J Closure-10 defect)
  @Test
  public void testMayBeString_withHookAndAddition_detectsStringPossibility() {
    // Condition: (x ? "a" : 1) + 2
    Node cond = IR.name("x");
    Node hook = IR.hook(cond, IR.string("a"), IR.number(1));
    Node add = IR.add(hook, IR.number(2));
    assertTrue(NodeUtil.mayBeString(add));
  }

  // Tests mayBeString for pure number addition
  @Test
  public void testMayBeString_numericAddition_returnsFalse() {
    Node add = IR.add(IR.number(1), IR.number(2));
    assertFalse(NodeUtil.mayBeString(add));
  }

  // Tests mayBeString for string literal and string addition
  @Test
  public void testMayBeString_stringLiteral_returnsTrue() {
    assertTrue(NodeUtil.mayBeString(IR.string("hello")));
    Node add = IR.add(IR.string("hello"), IR.number(1));
    assertTrue(NodeUtil.mayBeString(add));
  }

  // Tests mayBeString with boolean result and undefined/null
  @Test
  public void testMayBeString_booleanAndNull_returnsFalse() {
    assertFalse(NodeUtil.mayBeString(IR.trueNode()));
    assertFalse(NodeUtil.mayBeString(IR.falseNode()));
    assertFalse(NodeUtil.mayBeString(IR.nullNode()));
    assertFalse(NodeUtil.mayBeString(NodeUtil.newUndefinedNode(null)));
  }

  // Tests getImpureBooleanValue across various node types
  @Test
  public void testGetImpureBooleanValue_variousNodes_returnsExpectedTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.string("non-empty")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.string("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.number(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.number(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.nullNode()));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.arraylit()));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.objectlit()));

    Node hookBothTrue = IR.hook(IR.name("cond"), IR.string("a"), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookBothTrue));

    Node hookMismatch = IR.hook(IR.name("cond"), IR.string("a"), IR.number(0));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookMismatch));
  }

  // Tests getStringValue for literals and expressions
  @Test
  public void testGetStringValue_variousNodes_returnsCorrectString() {
    assertEquals("test", NodeUtil.getStringValue(IR.string("test")));
    assertEquals("42", NodeUtil.getStringValue(IR.number(42.0)));
    assertEquals("42.5", NodeUtil.getStringValue(IR.number(42.5)));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("[object Object]", NodeUtil.getStringValue(IR.objectlit()));

    Node array = IR.arraylit(IR.string("a"), IR.number(1), IR.nullNode());
    assertEquals("a,1,", NodeUtil.getStringValue(array));
  }

  // Tests getNumberValue for various node types
  @Test
  public void testGetNumberValue_variousNodes_returnsCorrectDouble() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
    assertEquals(Double.valueOf(123.45), NodeUtil.getNumberValue(IR.number(123.45)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));

    Node negInfinity = IR.neg(IR.name("Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInfinity));

    assertEquals(Double.valueOf(10.0), NodeUtil.getNumberValue(IR.string("10")));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.string("")));
    assertEquals(Double.valueOf(16.0), NodeUtil.getNumberValue(IR.string("0x10")));
    assertNull(NodeUtil.getNumberValue(IR.string("invalid_num")));
  }

  // Tests isImmutableValue on immutable and mutable node types
  @Test
  public void testIsImmutableValue_returnsExpectedValue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("str")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(10)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    assertTrue(NodeUtil.isImmutableValue(IR.not(IR.number(1))));
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));
    assertFalse(NodeUtil.isImmutableValue(IR.arraylit()));
  }

  // Tests isLiteralValue with arrays, objects, and nested nodes
  @Test
  public void testIsLiteralValue_nestedStructures_returnsExpectedValue() {
    assertTrue(NodeUtil.isLiteralValue(IR.arraylit(IR.number(1), IR.string("a")), false));
    assertFalse(NodeUtil.isLiteralValue(IR.arraylit(IR.name("x")), false));

    Node objLit = IR.objectlit(IR.propdef(IR.stringKey("k"), IR.number(1)));
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node objWithVar = IR.objectlit(IR.propdef(IR.stringKey("k"), IR.name("x")));
    assertFalse(NodeUtil.isLiteralValue(objWithVar, false));
  }

  // Tests isSymmetricOperation and isRelationalOperation
  @Test
  public void testSymmetricAndRelationalOperators() {
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.EQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.NE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHEQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHNE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSymmetricOperation(new Node(Token.ADD)));

    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GE)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LE)));
    assertFalse(NodeUtil.isRelationalOperation(new Node(Token.EQ)));

    assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
    assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
    assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
    assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
    assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.EQ));
  }

  // Tests isValidDefineValue with defined constants and expressions
  @Test
  public void testIsValidDefineValue_returnsExpectedValue() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");

    assertTrue(NodeUtil.isValidDefineValue(IR.number(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.string("text"), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.trueNode(), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.name("DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(IR.name("UNKNOWN_DEF"), defines));

    Node add = IR.add(IR.number(1), IR.name("DEF_A"));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = IR.not(IR.trueNode());
    assertTrue(NodeUtil.isValidDefineValue(not, defines));
  }

  // Tests mayHaveSideEffects and mayEffectMutableState
  @Test
  public void testMayHaveSideEffects_and_mayEffectMutableState() {
    assertFalse(NodeUtil.mayHaveSideEffects(IR.number(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.string("str")));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.arraylit()));

    assertTrue(NodeUtil.mayEffectMutableState(IR.arraylit()));
    assertTrue(NodeUtil.mayEffectMutableState(IR.objectlit()));

    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.mayHaveSideEffects(assign));
    assertTrue(NodeUtil.mayEffectMutableState(assign));

    Node throwNode = new Node(Token.THROW, IR.name("err"));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));
  }

  // Tests functionCallHasSideEffects for built-in functions and constructor calls
  @Test
  public void testCallHasSideEffects_builtinsAndConstructors() {
    Node mathFloor = IR.call(IR.getprop(IR.name("Math"), IR.string("floor")), IR.number(1.5));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathFloor));

    Node stringCast = IR.call(IR.name("String"), IR.number(10));
    assertFalse(NodeUtil.functionCallHasSideEffects(stringCast));

    Node customCall = IR.call(IR.name("customFunc"), IR.number(1));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));

    Node arrayNew = IR.newNode(IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(arrayNew));

    Node customNew = IR.newNode(IR.name("CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));
  }

  // Tests precedence table values
  @Test
  public void testPrecedence_standardOperators_returnsCorrectRank() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  // Tests opToStr and opToStrNoFail
  @Test
  public void testOpToStr_validAndInvalidOperators() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.SCRIPT));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests opToStrNoFail with invalid operator throwing Error
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  // Tests isValidSimpleName, isValidQualifiedName, and isLatin
  @Test
  public void testIdentifierValidation() {
    assertTrue(NodeUtil.isValidSimpleName("validName"));
    assertTrue(NodeUtil.isValidSimpleName("$var_1"));
    assertFalse(NodeUtil.isValidSimpleName("class")); // keyword
    assertFalse(NodeUtil.isValidSimpleName("123abc"));

    assertTrue(NodeUtil.isValidQualifiedName("a.b.c"));
    assertFalse(NodeUtil.isValidQualifiedName(".a.b"));
    assertFalse(NodeUtil.isValidQualifiedName("a.b."));
    assertFalse(NodeUtil.isValidQualifiedName("a..b"));

    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("nonAscii\u0080"));
  }

  // Tests evaluatesToLocalValue for various node constructs
  @Test
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.number(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.string("a")));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.arraylit()));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.objectlit()));

    Node hook = IR.hook(IR.name("cond"), IR.number(1), IR.number(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hook));

    Node hookNonLocal = IR.hook(IR.name("cond"), IR.name("externalA"), IR.number(2));
    assertFalse(NodeUtil.evaluatesToLocalValue(hookNonLocal));
  }

  // Tests prototype helper methods
  @Test
  public void testPrototypeHelpers() {
    Node qName = IR.getprop(
        IR.getprop(IR.name("Foo"), IR.string("prototype")),
        IR.string("bar"));

    assertTrue(NodeUtil.isPrototypeProperty(qName));
    assertEquals("bar", NodeUtil.getPrototypePropertyName(qName));
    Node protoClass = NodeUtil.getPrototypeClassName(qName);
    assertNotNull(protoClass);
    assertEquals("Foo", protoClass.getQualifiedName());

    Node nonProto = IR.getprop(IR.name("Foo"), IR.string("bar"));
    assertFalse(NodeUtil.isPrototypeProperty(nonProto));
  }

  // Tests booleanNode and numberNode creation
  @Test
  public void testBooleanAndNumberNodeCreation() {
    Node t = NodeUtil.booleanNode(true);
    assertEquals(Token.TRUE, t.getType());

    Node f = NodeUtil.booleanNode(false);
    assertEquals(Token.FALSE, f.getType());

    Node nanNode = NodeUtil.numberNode(Double.NaN, null);
    assertTrue(nanNode.isName());
    assertEquals("NaN", nanNode.getString());

    Node posInfNode = NodeUtil.numberNode(Double.POSITIVE_INFINITY, null);
    assertTrue(posInfNode.isName());
    assertEquals("Infinity", posInfNode.getString());

    Node numNode = NodeUtil.numberNode(42.0, null);
    assertTrue(numNode.isNumber());
    assertEquals(42.0, numNode.getDouble(), 0.0);
  }

  // Tests removeChild and tryMergeBlock
  @Test
  public void testASTModificationHelpers() {
    Node block = IR.block(IR.exprResult(IR.number(1)), IR.exprResult(IR.number(2)));
    Node parentBlock = IR.block(block);

    assertTrue(NodeUtil.tryMergeBlock(block));
    assertEquals(2, parentBlock.getChildCount());

    Node firstChild = parentBlock.getFirstChild();
    NodeUtil.removeChild(parentBlock, firstChild);
    assertEquals(1, parentBlock.getChildCount());
  }

  // Tests result type checks: isNumericResult, isBooleanResult, isStringResult, mayBeObject
  @Test
  public void testResultTypeChecks() {
    Node numSub = IR.sub(IR.number(5), IR.number(2));
    assertTrue(NodeUtil.isNumericResult(numSub));
    assertFalse(NodeUtil.isBooleanResult(numSub));
    assertFalse(NodeUtil.isStringResult(numSub));

    Node boolEq = IR.eq(IR.number(1), IR.number(2));
    assertTrue(NodeUtil.isBooleanResult(boolEq));
    assertFalse(NodeUtil.isNumericResult(boolEq));
    assertFalse(NodeUtil.isStringResult(boolEq));

    Node strLit = IR.string("abc");
    assertTrue(NodeUtil.isStringResult(strLit));
    assertFalse(NodeUtil.isNumericResult(strLit));
    assertFalse(NodeUtil.isBooleanResult(strLit));

    assertTrue(NodeUtil.mayBeObject(IR.objectlit()));
    assertTrue(NodeUtil.mayBeObject(IR.arraylit()));
    assertFalse(NodeUtil.mayBeObject(IR.number(123)));
    assertFalse(NodeUtil.mayBeObject(IR.string("test")));
  }

  // Tests algebraic property checks: isAssociative, isCommutative, isAssignmentOp
  @Test
  public void testAlgebraicPropertiesAndAssignmentOps() {
    assertTrue(NodeUtil.isAssociative(Token.ADD));
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.SUB));
    assertFalse(NodeUtil.isAssociative(Token.DIV));

    assertTrue(NodeUtil.isCommutative(Token.ADD));
    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertFalse(NodeUtil.isCommutative(Token.SUB));
    assertFalse(NodeUtil.isCommutative(Token.DIV));

    Node assign = IR.assign(IR.name("x"), IR.number(1));
    Node assignAdd = IR.assignAdd(IR.name("x"), IR.number(1));
    Node add = IR.add(IR.name("x"), IR.number(1));

    assertTrue(NodeUtil.isAssignmentOp(assign));
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertFalse(NodeUtil.isAssignmentOp(add));
  }

  // Tests function classification: isFunctionDeclaration, isFunctionExpression, isAnonymousFunction, getFunctionName
  @Test
  public void testFunctionClassification() {
    Node namedFn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    Node anonFn = IR.function(IR.name(""), IR.paramList(), IR.block());

    Node block = IR.block(namedFn);
    assertTrue(NodeUtil.isFunctionDeclaration(namedFn));
    assertFalse(NodeUtil.isFunctionExpression(namedFn));
    assertFalse(NodeUtil.isAnonymousFunction(namedFn));
    assertEquals("foo", NodeUtil.getFunctionName(namedFn));

    Node exprResult = IR.exprResult(anonFn);
    assertFalse(NodeUtil.isFunctionDeclaration(anonFn));
    assertTrue(NodeUtil.isFunctionExpression(anonFn));
    assertTrue(NodeUtil.isAnonymousFunction(anonFn));
    assertNull(NodeUtil.getFunctionName(anonFn));
  }

  // Tests loop and control structure checks: isLoopStructure, isControlStructure, getConditionExpression
  @Test
  public void testControlStructuresAndConditions() {
    Node cond = IR.name("cond");
    Node ifNode = IR.ifNode(cond, IR.block());
    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertFalse(NodeUtil.isLoopStructure(ifNode));
    assertSame(cond, NodeUtil.getConditionExpression(ifNode));

    Node whileCond = IR.name("wCond");
    Node whileNode = IR.whileNode(whileCond, IR.block());
    assertTrue(NodeUtil.isControlStructure(whileNode));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertSame(whileCond, NodeUtil.getConditionExpression(whileNode));

    Node forIn = IR.forIn(IR.name("k"), IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isLoopStructure(forIn));
    assertTrue(NodeUtil.isForIn(forIn));

    Node doCond = IR.name("dCond");
    Node doNode = IR.doNode(IR.block(), doCond);
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertSame(doCond, NodeUtil.getConditionExpression(doNode));
  }

  // Tests getAssignedValue and getRootOfQualifiedName
  @Test
  public void testAssignedValueAndQualifiedNameRoots() {
    Node value = IR.number(42);
    Node varNode = IR.var(IR.name("x"), value);
    Node varName = varNode.getFirstChild();
    assertSame(value, NodeUtil.getAssignedValue(varName));

    Node assignVal = IR.string("hello");
    Node assignNode = IR.assign(IR.name("y"), assignVal);
    assertSame(assignVal, NodeUtil.getAssignedValue(assignNode));

    Node rootName = IR.name("root");
    Node qname = IR.getprop(IR.getprop(rootName, IR.string("sub")), IR.string("prop"));
    assertSame(rootName, NodeUtil.getRootOfQualifiedName(qname));
    assertSame(rootName, NodeUtil.getRootOfQualifiedName(rootName));
  }

  // Tests containsType, referencesThis, and isLhs
  @Test
  public void testASTInspectionMethods() {
    Node thisNode = IR.thisNode();
    Node blockWithThis = IR.block(IR.exprResult(thisNode));
    assertTrue(NodeUtil.referencesThis(blockWithThis));

    Node blockWithoutThis = IR.block(IR.exprResult(IR.number(1)));
    assertFalse(NodeUtil.referencesThis(blockWithoutThis));

    assertTrue(NodeUtil.containsType(blockWithThis, Token.THIS));
    assertFalse(NodeUtil.containsType(blockWithoutThis, Token.THIS));

    Node lhs = IR.name("target");
    Node rhs = IR.number(100);
    Node assign = IR.assign(lhs, rhs);
    assertTrue(NodeUtil.isLhs(lhs, assign));
    assertFalse(NodeUtil.isLhs(rhs, assign));
  }
}
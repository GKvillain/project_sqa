package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  // Tests boolean conversion of literal nodes and special names
  @Test
  public void testGetBooleanValue_literals_returnsExpectedBoolean() {
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "hello")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "")));

    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0.0)));

    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  // Tests exception path for non-literal node in getBooleanValue
  @Test(expected = IllegalArgumentException.class)
  public void testGetBooleanValue_nonLiteral_throwsException() {
    NodeUtil.getBooleanValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)));
  }

  // Tests string conversion of various literal nodes
  @Test
  public void testGetStringValue_variousTokens_returnsExpectedString() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString(Token.NAME, "test")));
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.STRING, "foo")));
    assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    assertEquals("123.45", NodeUtil.getStringValue(Node.newNumber(123.45)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  // Tests function name extraction from different parent AST structures
  @Test
  public void testGetFunctionName_differentParents_returnsCorrectName() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fnName"), new Node(Token.LP), new Node(Token.BLOCK));
    
    Node varParent = Node.newString(Token.NAME, "varName");
    assertEquals("varName", NodeUtil.getFunctionName(fn, varParent));

    Node assignTarget = Node.newString(Token.NAME, "propName");
    Node assignParent = new Node(Token.ASSIGN, assignTarget, fn);
    assertEquals("propName", NodeUtil.getFunctionName(fn, assignParent));

    Node exprParent = new Node(Token.EXPR_RESULT, fn);
    assertEquals("fnName", NodeUtil.getFunctionName(fn, exprParent));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertNull(NodeUtil.getFunctionName(anonFn, exprParent));
  }

  // Tests immutability check for primitive literals and special names
  @Test
  public void testIsImmutableValue_variousNodes_returnsExpected() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "abc")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(42)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));

    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  // Tests literal value validation including compound literals
  @Test
  public void testIsLiteralValue_nestedLiterals_returnsExpected() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString(Token.STRING, "a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit));

    Node nonConstArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(nonConstArray));
  }

  // Tests validation of values assignable to a compile-time define
  @Test
  public void testIsValidDefineValue_validAndInvalid_returnsExpected() {
    Set<String> defines = Sets.newHashSet("DEF_A", "a.b.DEF_B");

    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "str"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.NOT, new Node(Token.TRUE)), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN"), defines));

    Node getProp = new Node(Token.GETPROP, 
        new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b")), 
        Node.newString(Token.STRING, "DEF_B"));
    assertTrue(NodeUtil.isValidDefineValue(getProp, defines));
  }

  // Tests side effect detection across statements and expressions
  @Test
  public void testMayHaveSideEffects_variousNodes_identifiesSideEffects() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(123)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.EMPTY)));
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString(Token.STRING, "err"))));

    Node varNodeNoChild = new Node(Token.VAR);
    assertFalse(NodeUtil.mayHaveSideEffects(varNodeNoChild));

    Node varNodeWithInit = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    varNodeWithInit.getFirstChild().addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.mayHaveSideEffects(varNodeWithInit));

    Node callNoSideEffects = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    callNoSideEffects.setIsNoSideEffectsCall();
    assertFalse(NodeUtil.mayHaveSideEffects(callNoSideEffects));

    Node regularCall = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    assertTrue(NodeUtil.mayHaveSideEffects(regularCall));
  }

  // Tests operator precedence and associativity definitions
  @Test
  public void testPrecedence_and_isAssociative_returnsCorrectValues() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(15, NodeUtil.precedence(Token.NAME));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));
  }

  // Tests token to string conversion helpers
  @Test
  public void testOpToStr_variousOperators_returnsStringOrNull() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("&&", NodeUtil.opToStr(Token.AND));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.SCRIPT));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests exception in opToStrNoFail when given non-operator token
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  // Tests Latin character verification and property name validity
  @Test
  public void testIsLatin_and_isValidPropertyName_validatesIdentifiers() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("nonLatin_\u00E9"));

    assertTrue(NodeUtil.isValidPropertyName("foo"));
    assertTrue(NodeUtil.isValidPropertyName("$bar_123"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("123abc")); // invalid JS identifier
  }

  // Tests qualified name node generation
  @Test
  public void testNewQualifiedNameNode_dottedNames_createsGetPropTree() {
    Node simpleName = NodeUtil.newQualifiedNameNode("simple", 1, 2);
    assertTrue(simpleName.isName());
    assertEquals("simple", simpleName.getString());

    Node dotted = NodeUtil.newQualifiedNameNode("a.b.c", 1, 0);
    assertTrue(dotted.getType() == Token.GETPROP);
    assertEquals("c", dotted.getLastChild().getString());
    assertTrue(dotted.getFirstChild().getType() == Token.GETPROP);
    assertEquals("b", dotted.getFirstChild().getLastChild().getString());
    assertEquals("a", dotted.getFirstChild().getFirstChild().getString());
  }

  // Tests prototype property detection and class/property name extraction
  @Test
  public void testIsPrototypeProperty_and_GetPrototypeClassName_parsesCorrectly() {
    Node qName = NodeUtil.newQualifiedNameNode("MyClass.prototype.myMethod", 0, 0);
    assertTrue(NodeUtil.isPrototypeProperty(qName));

    Node classNameNode = NodeUtil.getPrototypeClassName(qName);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getString());

    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qName));

    Node nonProto = NodeUtil.newQualifiedNameNode("MyClass.myMethod", 0, 0);
    assertFalse(NodeUtil.isPrototypeProperty(nonProto));
  }

  // Tests AST manipulation: removeChild on statement block and var
  @Test
  public void testRemoveChild_varAndBlock_removesCorrectly() {
    Node block = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);

    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertEquals(stmt2, block.getFirstChild());

    Node varNode = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "a");
    Node name2 = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(name1);
    varNode.addChildToBack(name2);
    block.addChildToBack(varNode);

    NodeUtil.removeChild(varNode, name1);
    assertEquals(1, varNode.getChildCount());
    assertEquals(name2, varNode.getFirstChild());
  }

  // Tests merging a block into its parent statement block
  @Test
  public void testTryMergeBlock_statementBlockParent_mergesSuccessfully() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    script.addChildToBack(block);

    boolean merged = NodeUtil.tryMergeBlock(block);
    assertTrue(merged);
    assertEquals(2, script.getChildCount());
    assertEquals(stmt1, script.getFirstChild());
    assertEquals(stmt2, script.getLastChild());
  }

  // Tests canBeSideEffected with known constants
  @Test
  public void testCanBeSideEffected_callsAndConstants_returnsExpected() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertTrue(NodeUtil.canBeSideEffected(callNode));

    Node constName = Node.newString(Token.NAME, "CONST_VAL");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertFalse(NodeUtil.canBeSideEffected(constName));

    Node nonConstName = Node.newString(Token.NAME, "mutableVar");
    assertTrue(NodeUtil.canBeSideEffected(nonConstName));

    Set<String> knownConsts = Collections.singleton("mutableVar");
    assertFalse(NodeUtil.canBeSideEffected(nonConstName, knownConsts));
  }

  // Tests function call side-effect recognition for built-in Math and String functions
  @Test
  public void testFunctionCallHasSideEffects_builtins_returnsFalse() {
    Node stringCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(stringCall));

    Node mathSinCall = new Node(Token.CALL, 
        new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin")));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathSinCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));
  }

  // Tests statement and statement block classification
  @Test
  public void testIsStatement_and_isStatementBlock() {
    Node exprStmt = NodeUtil.newExpr(Node.newNumber(1));
    Node varStmt = new Node(Token.VAR);
    Node ifStmt = new Node(Token.IF);
    Node blockNode = new Node(Token.BLOCK);
    Node scriptNode = new Node(Token.SCRIPT);
    Node numberNode = Node.newNumber(1);

    assertTrue(NodeUtil.isStatement(exprStmt));
    assertTrue(NodeUtil.isStatement(varStmt));
    assertTrue(NodeUtil.isStatement(ifStmt));
    assertFalse(NodeUtil.isStatement(numberNode));

    assertTrue(NodeUtil.isStatementBlock(blockNode));
    assertTrue(NodeUtil.isStatementBlock(scriptNode));
    assertFalse(NodeUtil.isStatementBlock(numberNode));
  }

  // Tests control structures and loop structures detection
  @Test
  public void testIsControlStructure_and_isLoopStructure() {
    Node ifNode = new Node(Token.IF);
    Node whileNode = new Node(Token.WHILE);
    Node forNode = new Node(Token.FOR);
    Node doNode = new Node(Token.DO);
    Node exprNode = NodeUtil.newExpr(Node.newNumber(1));

    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertTrue(NodeUtil.isControlStructure(whileNode));
    assertTrue(NodeUtil.isControlStructure(forNode));
    assertFalse(NodeUtil.isControlStructure(exprNode));

    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertFalse(NodeUtil.isLoopStructure(ifNode));
  }

  // Tests result type predicates (isBooleanResult, isNumericResult, isStringResult)
  @Test
  public void testResultTypePredicates() {
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Node eqNode = new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(2));
    Node numNode = Node.newNumber(42);
    Node strNode = Node.newString(Token.STRING, "hello");
    Node addNums = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node typeOfNode = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));

    assertTrue(NodeUtil.isBooleanResult(notNode));
    assertTrue(NodeUtil.isBooleanResult(eqNode));
    assertFalse(NodeUtil.isBooleanResult(numNode));

    assertTrue(NodeUtil.isNumericResult(numNode));
    assertTrue(NodeUtil.isNumericResult(addNums));
    assertFalse(NodeUtil.isNumericResult(strNode));

    assertTrue(NodeUtil.isStringResult(strNode));
    assertTrue(NodeUtil.isStringResult(typeOfNode));
    assertFalse(NodeUtil.isStringResult(numNode));
  }

  // Tests symmetric, relational, and commutative operator identification
  @Test
  public void testOperatorProperties() {
    Node eq = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
    Node lt = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));

    assertTrue(NodeUtil.isSymmetricOperation(eq));
    assertFalse(NodeUtil.isSymmetricOperation(lt));

    assertTrue(NodeUtil.isRelationalOp(lt));
    assertFalse(NodeUtil.isRelationalOp(eq));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  // Tests root of qualified name extraction
  @Test
  public void testGetRootOfQualifiedName() {
    Node singleName = Node.newString(Token.NAME, "rootVar");
    assertEquals(singleName, NodeUtil.getRootOfQualifiedName(singleName));

    Node qualified = NodeUtil.newQualifiedNameNode("a.b.c", 0, 0);
    Node root = NodeUtil.getRootOfQualifiedName(qualified);
    assertNotNull(root);
    assertTrue(root.isName());
    assertEquals("a", root.getString());
  }

  // Tests condition expression retrieval from control structures
  @Test
  public void testGetConditionExpression() {
    Node cond = new Node(Token.TRUE);
    Node body = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, cond, body);

    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));

    Node whileCond = Node.newString(Token.NAME, "run");
    Node whileNode = new Node(Token.WHILE, whileCond, new Node(Token.BLOCK));
    assertEquals(whileCond, NodeUtil.getConditionExpression(whileNode));
  }

  // Tests empty block detection and AST node searching via containsType
  @Test
  public void testIsEmptyBlock_and_containsType() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node nonEmptyBlock = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(nonEmptyBlock));

    assertTrue(NodeUtil.containsType(nonEmptyBlock, Token.NUMBER));
    assertFalse(NodeUtil.containsType(nonEmptyBlock, Token.STRING));
  }

  // Tests local value evaluation predicate
  @Test
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(10)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString(Token.STRING, "val")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));

    Node nameNode = Node.newString(Token.NAME, "globalVar");
    assertFalse(NodeUtil.evaluatesToLocalValue(nameNode));
  }

  // Tests node construction helper functions
  @Test
  public void testNodeCreationHelpers() {
    Node undefinedNode = NodeUtil.newUndefinedNode(Node.newNumber(0));
    assertEquals(Token.VOID, undefinedNode.getType());
    assertEquals(Token.NUMBER, undefinedNode.getFirstChild().getType());

    Node varNode = NodeUtil.newVarNode("myVar", Node.newNumber(123));
    assertTrue(varNode.isVar());
    assertEquals("myVar", varNode.getFirstChild().getString());
    assertEquals(123.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0);

    Node callNode = NodeUtil.newCallNode(
        Node.newString(Token.NAME, "func"),
        Node.newNumber(1),
        Node.newString(Token.STRING, "arg2"));
    assertTrue(callNode.isCall());
    assertEquals(3, callNode.getChildCount());
  }

  // Tests var-args function detection
  @Test
  public void testIsVarArgsFunction() {
    Node fnNoArgs = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isVarArgsFunction(fnNoArgs));

    Node bodyWithArguments = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Node fnWithArgs = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), bodyWithArguments);
    assertTrue(NodeUtil.isVarArgsFunction(fnWithArgs));
  }
}
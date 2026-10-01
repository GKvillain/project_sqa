package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  // Tests boolean evaluation for literal nodes (true, false, string, number, names)
  @Test
  public void testGetBooleanValue_literals_returnsExpectedBoolean() {
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));

    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "hello")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "")));

    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(-1.0)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0.0)));

    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  // Tests exception path when getBooleanValue receives a non-literal node
  @Test(expected = IllegalArgumentException.class)
  public void testGetBooleanValue_nonLiteral_throwsException() {
    Node varNode = new Node(Token.VAR);
    NodeUtil.getBooleanValue(varNode);
  }

  // Tests string conversion for various literal and primitive node types
  @Test
  public void testGetStringValue_variousTypes_returnsFormattedString() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString(Token.STRING, "test")));
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));
    assertEquals("1", NodeUtil.getStringValue(Node.newNumber(1.0)));
    assertEquals("1.5", NodeUtil.getStringValue(Node.newNumber(1.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.BLOCK)));
  }

  // Tests function name extraction from function statements and assignments
  @Test
  public void testGetFunctionName_variousFunctionForms_returnsExpectedName() {
    Node fnName = Node.newString(Token.NAME, "myFunc");
    Node fn = new Node(Token.FUNCTION, fnName, new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);

    assertEquals("myFunc", NodeUtil.getFunctionName(fn, script));

    Node varName = Node.newString(Token.NAME, "varFunc");
    Node varNode = new Node(Token.VAR, varName);
    varName.addChildToBack(fn);
    assertEquals("varFunc", NodeUtil.getFunctionName(fn, varName));

    Node propName = Node.newString(Token.NAME, "obj");
    Node getProp = new Node(Token.GETPROP, propName, Node.newString(Token.STRING, "method"));
    Node assignNode = new Node(Token.ASSIGN, getProp, fn);
    assertEquals("obj.method", NodeUtil.getFunctionName(fn, assignNode));
  }

  // Tests literal and immutable value recognition
  @Test
  public void testIsLiteralValue_primitiveAndComplexLiterals_returnsCorrectBoolean() {
    assertTrue(NodeUtil.isLiteralValue(Node.newNumber(42)));
    assertTrue(NodeUtil.isLiteralValue(Node.newString(Token.STRING, "abc")));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.NULL)));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isLiteralValue(arrayLit));

    Node invalidArrayLit = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "unresolvedVar"));
    assertFalse(NodeUtil.isLiteralValue(invalidArrayLit));

    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isLiteralValue(fn));
  }

  // Tests valid define value validation logic
  @Test
  public void testIsValidDefineValue_validAndInvalidValues_returnsCorrectBoolean() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");
    defines.add("CONFIG.FLAG");

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));

    Node notOp = new Node(Token.NOT, new Node(Token.FALSE));
    assertTrue(NodeUtil.isValidDefineValue(notOp, defines));

    Node nameDef = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(nameDef, defines));

    Node undefinedName = Node.newString(Token.NAME, "UNKNOWN");
    assertFalse(NodeUtil.isValidDefineValue(undefinedName, defines));
  }

  // Tests side-effect detection for various statements and operations
  @Test
  public void testMayHaveSideEffects_variousNodes_returnsExpected() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newString(Token.STRING, "str")));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.EMPTY)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.BLOCK)));

    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString(Token.STRING, "error"))));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.mayHaveSideEffects(assign));

    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertTrue(NodeUtil.mayHaveSideEffects(call));
  }

  // Tests whether expressions can be affected by side-effects or have side-effects
  @Test
  public void testCanBeSideEffected_nameAndCalls_returnsExpected() {
    Node normalName = Node.newString(Token.NAME, "x");
    assertTrue(NodeUtil.canBeSideEffected(normalName));

    Set<String> constants = Collections.singleton("x");
    assertFalse(NodeUtil.canBeSideEffected(normalName, constants));

    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertTrue(NodeUtil.canBeSideEffected(call));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "prop"));
    assertTrue(NodeUtil.canBeSideEffected(getProp, constants));
  }

  // Tests side effect detection on Math namespace and String built-in function calls
  @Test
  public void testFunctionCallHasSideEffects_mathAndString_returnsFalse() {
    Node stringCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(123));
    assertFalse(NodeUtil.functionCallHasSideEffects(stringCall));

    Node mathSin = new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin"));
    Node mathCall = new Node(Token.CALL, mathSin, Node.newNumber(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "myFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));
  }

  // Tests side effect detection on known side-effect free constructor calls
  @Test
  public void testConstructorCallHasSideEffects_builtins_returnsFalse() {
    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newDate = new Node(Token.NEW, Node.newString(Token.NAME, "Date"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newDate));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "MyClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));
  }

  // Tests operator precedence and associativity lookup
  @Test
  public void testPrecedence_andIsAssociative_returnsExpectedValues() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));
  }

  // Tests assignment operators recognition and underlying op retrieval
  @Test
  public void testIsAssignmentOp_andGetOpFromAssignmentOp_returnsCorrectOp() {
    Node assignAdd = new Node(Token.ASSIGN_ADD);
    Node assignSub = new Node(Token.ASSIGN_SUB);
    Node assignMul = new Node(Token.ASSIGN_MUL);
    Node plainAssign = new Node(Token.ASSIGN);
    Node addNode = new Node(Token.ADD);

    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertTrue(NodeUtil.isAssignmentOp(plainAssign));
    assertFalse(NodeUtil.isAssignmentOp(addNode));

    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(assignMul));
  }

  // Tests creation of qualified name node hierarchy
  @Test
  public void testNewQualifiedNameNode_singleAndMultipleParts_createsExpectedTree() {
    Node single = NodeUtil.newQualifiedNameNode("foo", 1, 0);
    assertTrue(single.isName());
    assertEquals("foo", single.getString());

    Node qualified = NodeUtil.newQualifiedNameNode("foo.bar.baz", 1, 0);
    assertTrue(NodeUtil.isGetProp(qualified));
    assertEquals("foo.bar.baz", qualified.getQualifiedName());
    assertEquals("baz", qualified.getLastChild().getString());
  }

  // Tests prototype property detection and class name extraction
  @Test
  public void testIsPrototypeProperty_andGetters_returnsCorrectValues() {
    Node qname = NodeUtil.newQualifiedNameNode("MyClass.prototype.doSomething", 1, 0);
    assertTrue(NodeUtil.isPrototypeProperty(qname));

    Node classNameNode = NodeUtil.getPrototypeClassName(qname);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getQualifiedName());
    assertEquals("doSomething", NodeUtil.getPrototypePropertyName(qname));

    Node normalProp = NodeUtil.newQualifiedNameNode("MyClass.doSomething", 1, 0);
    assertFalse(NodeUtil.isPrototypeProperty(normalProp));
  }

  // Tests property name validation according to JavaScript identifier rules
  @Test
  public void testIsValidPropertyName_validAndInvalidNames_returnsExpected() {
    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("_privateProp"));
    assertTrue(NodeUtil.isValidPropertyName("$special"));

    assertFalse(NodeUtil.isValidPropertyName("class"));
    assertFalse(NodeUtil.isValidPropertyName("123invalid"));
    assertFalse(NodeUtil.isValidPropertyName("has-dash"));
  }

  // Tests node removal in statement blocks and variable declarations
  @Test
  public void testRemoveChild_varAndBlock_removesCorrectly() {
    Node block = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node stmt2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);

    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertSame(stmt2, block.getFirstChild());

    Node parentBlock = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
    parentBlock.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    assertEquals(0, parentBlock.getChildCount());
  }

  // Tests merging a block into its parent block
  @Test
  public void testTryMergeBlock_statementBlock_mergesCorrectly() {
    Node parentBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node stmt = new Node(Token.EXPR_RESULT, Node.newNumber(42));
    innerBlock.addChildToBack(stmt);
    parentBlock.addChildToBack(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(1, parentBlock.getChildCount());
    assertSame(stmt, parentBlock.getFirstChild());
  }

  // Tests type matching, counting, and node searching utilities
  @Test
  public void testContainsType_andCountReferences_countsCorrectly() {
    Node root = new Node(Token.BLOCK);
    Node call1 = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node call2 = new Node(Token.CALL, Node.newString(Token.NAME, "bar"));
    root.addChildToBack(new Node(Token.EXPR_RESULT, call1));
    root.addChildToBack(new Node(Token.EXPR_RESULT, call2));

    assertTrue(NodeUtil.containsType(root, Token.CALL));
    assertFalse(NodeUtil.containsType(root, Token.WHILE));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.CALL));
    assertTrue(NodeUtil.isNameReferenced(root, "foo"));
    assertFalse(NodeUtil.isNameReferenced(root, "baz"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "foo"));
  }

  // Tests operator token conversion to corresponding string representations
  @Test
  public void testOpToStr_andOpToStrNoFail_returnsOperatorStrings() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.NUMBER));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests exception thrown when opToStrNoFail receives an invalid operator
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.NUMBER);
  }
}
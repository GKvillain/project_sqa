package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  // Tests static diagnostic type definition
  @Test
  public void testDiagnosticType_warningCreation_returnsNonNull() {
    DiagnosticType warning = TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS;
    assertNotNull(warning);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS", warning.key);
  }

  // Tests getBooleanOutcomes when condition is true and left is TRUE
  @Test
  public void testGetBooleanOutcomes_trueCondition_leftTrue_returnsRightOutcomes() {
    BooleanLiteralSet left = BooleanLiteralSet.TRUE;
    BooleanLiteralSet right = BooleanLiteralSet.FALSE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.FALSE, result);
  }

  // Tests getBooleanOutcomes when condition is true and left is FALSE
  @Test
  public void testGetBooleanOutcomes_trueCondition_leftFalse_returnsUnionWithFalse() {
    BooleanLiteralSet left = BooleanLiteralSet.FALSE;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes when condition is false and left is TRUE
  @Test
  public void testGetBooleanOutcomes_falseCondition_leftTrue_returnsUnionWithTrue() {
    BooleanLiteralSet left = BooleanLiteralSet.TRUE;
    BooleanLiteralSet right = BooleanLiteralSet.FALSE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, false);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes when condition is false and left is FALSE
  @Test
  public void testGetBooleanOutcomes_falseCondition_leftFalse_returnsRightOutcomes() {
    BooleanLiteralSet left = BooleanLiteralSet.FALSE;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, false);
    assertEquals(BooleanLiteralSet.TRUE, result);
  }

  // Tests getBooleanOutcomes with BOTH on both sides
  @Test
  public void testGetBooleanOutcomes_bothSidesBoth_returnsBoth() {
    BooleanLiteralSet left = BooleanLiteralSet.BOTH;
    BooleanLiteralSet right = BooleanLiteralSet.BOTH;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes with EMPTY literal set
  @Test
  public void testGetBooleanOutcomes_emptyLeft_returnsRightOutcomes() {
    BooleanLiteralSet left = BooleanLiteralSet.EMPTY;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.TRUE, result);
  }

  // Tests type inference on simple number assignment
  @Test
  public void testTypeInference_numberAssignment_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var x = 1; }");
    Node varNode = findFirstVarName(root, "x");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varNode.getJSType());
  }

  // Tests type inference on string concatenation
  @Test
  public void testTypeInference_stringAddition_infersStringType() {
    Node root = parseAndTypeCheck("function f() { var s = 'hello' + ' world'; }");
    Node varNode = findFirstVarName(root, "s");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), varNode.getJSType());
  }

  // Tests type inference on array literal
  @Test
  public void testTypeInference_arrayLiteral_infersArrayType() {
    Node root = parseAndTypeCheck("function f() { var arr = [1, 2, 3]; }");
    Node varNode = findFirstVarName(root, "arr");
    assertNotNull(varNode);
    assertTrue(varNode.getJSType().isSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE)));
  }

  // Tests type inference on comparison expression
  @Test
  public void testTypeInference_comparisonOp_infersBooleanType() {
    Node root = parseAndTypeCheck("function f() { var b = 1 < 2; }");
    Node varNode = findFirstVarName(root, "b");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varNode.getJSType());
  }

  // Tests type inference on typeof expression
  @Test
  public void testTypeInference_typeofOp_infersStringType() {
    Node root = parseAndTypeCheck("function f() { var t = typeof 42; }");
    Node varNode = findFirstVarName(root, "t");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), varNode.getJSType());
  }

  // Tests type inference on subtraction operation
  @Test
  public void testTypeInference_subtractionOp_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var n = 10 - 5; }");
    Node varNode = findFirstVarName(root, "n");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varNode.getJSType());
  }

  // Tests type inference on conditional hook (ternary)
  @Test
  public void testTypeInference_hookOp_infersUnionOrCommonType() {
    Node root = parseAndTypeCheck("function f() { var x = true ? 1 : 2; }");
    Node varNode = findFirstVarName(root, "x");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varNode.getJSType());
  }

  // Tests type inference on logical AND operation
  @Test
  public void testTypeInference_logicalAnd_infersCorrectType() {
    Node root = parseAndTypeCheck("function f() { var a = true && false; }");
    Node varNode = findFirstVarName(root, "a");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varNode.getJSType());
  }

  // Tests type inference on object literal assignment
  @Test
  public void testTypeInference_objectLiteral_infersObjectType() {
    Node root = parseAndTypeCheck("function f() { var obj = { foo: 'bar', num: 123 }; }");
    Node varNode = findFirstVarName(root, "obj");
    assertNotNull(varNode);
    assertTrue(varNode.getJSType().isObject());
  }

  // Tests type inference on null literal
  @Test
  public void testTypeInference_nullLiteral_infersNullType() {
    Node root = parseAndTypeCheck("function f() { var n = null; }");
    Node varNode = findFirstVarName(root, "n");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), varNode.getJSType());
  }

  // Tests type inference on void operator
  @Test
  public void testTypeInference_voidOp_infersVoidType() {
    Node root = parseAndTypeCheck("function f() { var u = void 0; }");
    Node varNode = findFirstVarName(root, "u");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), varNode.getJSType());
  }

  // Tests type inference on NOT operator
  @Test
  public void testTypeInference_notOp_infersBooleanType() {
    Node root = parseAndTypeCheck("function f() { var b = !0; }");
    Node varNode = findFirstVarName(root, "b");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varNode.getJSType());
  }

  // Tests type inference on bitwise NOT operator
  @Test
  public void testTypeInference_bitNotOp_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var bn = ~5; }");
    Node varNode = findFirstVarName(root, "bn");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varNode.getJSType());
  }

  // Tests type inference on bitwise AND/OR/XOR operators
  @Test
  public void testTypeInference_bitwiseOps_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var a = 1 & 2; var o = 1 | 2; var x = 1 ^ 2; }");
    Node varA = findFirstVarName(root, "a");
    Node varO = findFirstVarName(root, "o");
    Node varX = findFirstVarName(root, "x");
    assertNotNull(varA);
    assertNotNull(varO);
    assertNotNull(varX);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varA.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varO.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varX.getJSType());
  }

  // Tests type inference on bit shift operators
  @Test
  public void testTypeInference_shiftOps_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var s1 = 1 << 2; var s2 = 4 >> 1; var s3 = -1 >>> 1; }");
    Node varS1 = findFirstVarName(root, "s1");
    Node varS2 = findFirstVarName(root, "s2");
    Node varS3 = findFirstVarName(root, "s3");
    assertNotNull(varS1);
    assertNotNull(varS2);
    assertNotNull(varS3);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varS1.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varS2.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varS3.getJSType());
  }

  // Tests type inference on multiply, divide, and modulo operators
  @Test
  public void testTypeInference_arithmeticOps_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var m = 3 * 4; var d = 10 / 2; var rem = 7 % 3; }");
    Node varM = findFirstVarName(root, "m");
    Node varD = findFirstVarName(root, "d");
    Node varRem = findFirstVarName(root, "rem");
    assertNotNull(varM);
    assertNotNull(varD);
    assertNotNull(varRem);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varM.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varD.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varRem.getJSType());
  }

  // Tests type inference on instanceof and in operators
  @Test
  public void testTypeInference_instanceofAndIn_infersBooleanType() {
    Node root = parseAndTypeCheck("function f(o) { var inst = o instanceof Object; var hasProp = 'p' in o; }");
    Node varInst = findFirstVarName(root, "inst");
    Node varHasProp = findFirstVarName(root, "hasProp");
    assertNotNull(varInst);
    assertNotNull(varHasProp);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varInst.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varHasProp.getJSType());
  }

  // Tests type inference on logical OR operation
  @Test
  public void testTypeInference_logicalOr_infersCorrectType() {
    Node root = parseAndTypeCheck("function f() { var b = false || true; }");
    Node varNode = findFirstVarName(root, "b");
    assertNotNull(varNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), varNode.getJSType());
  }

  // Tests type inference on increment and decrement operators
  @Test
  public void testTypeInference_incDecOps_infersNumberType() {
    Node root = parseAndTypeCheck("function f() { var x = 0; var y = ++x; var z = x--; }");
    Node varY = findFirstVarName(root, "y");
    Node varZ = findFirstVarName(root, "z");
    assertNotNull(varY);
    assertNotNull(varZ);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varY.getJSType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varZ.getJSType());
  }

  private Node parseAndTypeCheck(String js) {
    Node scriptRoot = compiler.parseTestCode(js);
    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(scriptRoot, null);

    Node functionNode = findFirstFunction(scriptRoot);
    if (functionNode != null) {
      Scope functionScope = scopeCreator.createScope(functionNode, globalScope);
      ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
      cfa.process(null, functionNode.getLastChild());
      ControlFlowGraph<Node> cfg = cfa.getCfg();
      TypeInference inference = new TypeInference(
          compiler, cfg, rai, functionScope, Collections.<String, CodingConvention.AssertionFunctionSpec>emptyMap());
      inference.analyze();
    }
    return scriptRoot;
  }

  private Node findFirstFunction(Node n) {
    if (n.isFunction()) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node fn = findFirstFunction(child);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  private Node findFirstVarName(Node n, String name) {
    if (n.isName() && name.equals(n.getString())) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstVarName(child, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
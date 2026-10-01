package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.REGEXP_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope topScope;
  private Scope functionScope;
  private Node rootBlockNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private void inFunction(String js) {
    Node scriptNode = compiler.parseTestCode("function f() {" + js + "}");
    assertEquals(0, compiler.getErrorCount());

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    topScope = scopeCreator.createScope(scriptNode, null);

    Node functionNode = findFunctionNode(scriptNode);
    assertNotNull(functionNode);

    functionScope = scopeCreator.createScope(functionNode, topScope);
    rootBlockNode = functionNode.getLastChild();

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, rootBlockNode);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    Map<String, CodingConvention.AssertionFunctionSpec> assertionMap = Maps.newHashMap();
    TypeInference typeInference = new TypeInference(
        compiler, cfg, compiler.getReverseAbstractInterpreter(), functionScope, assertionMap);
    typeInference.analyze();
  }

  private Node findFunctionNode(Node n) {
    if (n.isFunction()) {
      return n;
    }
    for (Node child : n.children()) {
      Node result = findFunctionNode(child);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private Node findLastNameNode(Node n, String name) {
    Node last = null;
    if (n.isName() && name.equals(n.getString())) {
      last = n;
    }
    for (Node child : n.children()) {
      Node result = findLastNameNode(child, name);
      if (result != null) {
        last = result;
      }
    }
    return last;
  }

  private JSType getType(String name) {
    Node nameNode = findLastNameNode(rootBlockNode, name);
    assertNotNull("Node not found: " + name, nameNode);
    return nameNode.getJSType();
  }

  private JSType getNativeType(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }

  // Tests Issue 783: Assignment of null to declared Object type (Defects4J 176 regression)
  @Test
  public void testIssue783_assignNullToDeclaredObject_infersNullType() {
    inFunction(
        "/** @type {Object} */ var x = {};" +
        "var y = (x = null);");
    assertEquals(getNativeType(NULL_TYPE), getType("y"));
  }

  // Tests Issue 783b: Assignment of string to declared Object type (Defects4J 176 regression)
  @Test
  public void testIssue783b_assignStringToDeclaredObject_infersStringType() {
    inFunction(
        "/** @type {Object} */ var x = {};" +
        "var y = (x = 'foo');");
    assertEquals(getNativeType(STRING_TYPE), getType("y"));
  }

  // Tests Issue 783c: Assignment of boolean to declared Object type (Defects4J 176 regression)
  @Test
  public void testIssue783c_assignBooleanToDeclaredObject_infersBooleanType() {
    inFunction(
        "/** @type {Object} */ var x = {};" +
        "var y = (x = true);");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("y"));
  }

  // Tests addition of numbers
  @Test
  public void testTraverseAdd_numbers_infersNumberType() {
    inFunction("var x = 1 + 2;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }

  // Tests addition involving strings
  @Test
  public void testTraverseAdd_stringConcatenation_infersStringType() {
    inFunction("var x = 'hello' + 5;");
    assertEquals(getNativeType(STRING_TYPE), getType("x"));
  }

  // Tests array literal traversal
  @Test
  public void testTraverseArrayLiteral_infersArrayType() {
    inFunction("var arr = [1, 2, 3];");
    assertEquals(getNativeType(ARRAY_TYPE), getType("arr"));
  }

  // Tests object literal traversal
  @Test
  public void testTraverseObjectLiteral_infersObjectType() {
    inFunction("var obj = {a: 1, b: 'str'};");
    assertNotNull(getType("obj"));
    assertTrue(getType("obj").isObjectType());
  }

  // Tests logical AND operator
  @Test
  public void testTraverseAnd_shortCircuiting_infersCorrectType() {
    inFunction("var x = true && 'hello';");
    assertEquals(getNativeType(STRING_TYPE), getType("x"));
  }

  // Tests logical OR operator
  @Test
  public void testTraverseOr_shortCircuiting_infersCorrectType() {
    inFunction("var x = false || 42;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }

  // Tests ternary hook operator
  @Test
  public void testTraverseHook_conditionalTernary_infersUnionType() {
    inFunction("var cond = true; var x = cond ? 1 : 'str';");
    JSType unionType = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    assertEquals(unionType, getType("x"));
  }

  // Tests typeof operator
  @Test
  public void testTraverseTypeOf_operator_infersStringType() {
    inFunction("var x = typeof 123;");
    assertEquals(getNativeType(STRING_TYPE), getType("x"));
  }

  // Tests unary plus and minus operators
  @Test
  public void testTraverseUnaryOps_negationAndPlus_infersNumberType() {
    inFunction("var a = -'5'; var b = +'5';");
    assertEquals(getNativeType(NUMBER_TYPE), getType("a"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("b"));
  }

  // Tests comparison operators
  @Test
  public void testTraverseComparison_operators_infersBooleanType() {
    inFunction("var a = 1 < 2; var b = 3 === 4;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("a"));
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("b"));
  }

  // Tests bitwise and arithmetic operations
  @Test
  public void testTraverseBitwiseAndArithmetic_infersNumberType() {
    inFunction("var a = 1 & 2; var b = 3 * 4; var c = 5 % 2;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("a"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("b"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("c"));
  }

  // Tests catch block variable typing
  @Test
  public void testTraverseCatch_untypedCatchParam_infersUnknownType() {
    inFunction("try {} catch (e) { var x = e; }");
    assertEquals(getNativeType(UNKNOWN_TYPE), getType("x"));
  }

  // Tests property access on declared object
  @Test
  public void testTraverseGetProp_propertyAccess_infersDeclaredPropertyType() {
    inFunction("/** @type {{foo: number}} */ var obj = {foo: 1}; var x = obj.foo;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }

  // Tests comma operator
  @Test
  public void testTraverseComma_operator_infersLastChildType() {
    inFunction("var x = (1, 'second');");
    assertEquals(getNativeType(STRING_TYPE), getType("x"));
  }

  // Tests logical NOT operator
  @Test
  public void testTraverseNot_infersBooleanType() {
    inFunction("var x = !0; var y = !'hello';");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("x"));
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("y"));
  }

  // Tests bitwise NOT operator (~)
  @Test
  public void testTraverseBitwiseNot_infersNumberType() {
    inFunction("var x = ~42;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }

  // Tests increment and decrement operations (INC and DEC)
  @Test
  public void testTraverseIncDec_infersNumberType() {
    inFunction("var a = 1; var b = a++; var c = ++a; var d = a--; var e = --a;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("b"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("c"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("d"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("e"));
  }

  // Tests delete operator (DELPROP)
  @Test
  public void testTraverseDelProp_infersBooleanType() {
    inFunction("var obj = {a: 1}; var x = delete obj.a;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("x"));
  }

  // Tests instanceof operator
  @Test
  public void testTraverseInstanceOf_infersBooleanType() {
    inFunction("var x = ({}) instanceof Object;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("x"));
  }

  // Tests in operator
  @Test
  public void testTraverseIn_infersBooleanType() {
    inFunction("var x = 'prop' in {};");
    assertEquals(getNativeType(BOOLEAN_TYPE), getType("x"));
  }

  // Tests void operator
  @Test
  public void testTraverseVoid_infersVoidType() {
    inFunction("var x = void 0;");
    assertEquals(getNativeType(VOID_TYPE), getType("x"));
  }

  // Tests RegExp literal traversal
  @Test
  public void testTraverseRegExp_infersRegExpType() {
    inFunction("var re = /abc/g;");
    assertEquals(getNativeType(REGEXP_TYPE), getType("re"));
  }

  // Tests compound assignment operators (ASSIGN_ADD, ASSIGN_SUB, etc.)
  @Test
  public void testTraverseAssignOps_infersEvaluatedType() {
    inFunction("var x = 1; x += 2; var y = x;" +
               "var s = 'a'; s += 'b'; var t = s;" +
               "var num = 10; num *= 2; var res = num;");
    assertEquals(getNativeType(NUMBER_TYPE), getType("y"));
    assertEquals(getNativeType(STRING_TYPE), getType("t"));
    assertEquals(getNativeType(NUMBER_TYPE), getType("res"));
  }

  // Tests element access (GETELEM) on typed Array
  @Test
  public void testTraverseGetElem_arrayAccess_infersElementType() {
    inFunction("/** @type {Array.<number>} */ var arr = [1, 2]; var x = arr[0];");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }

  // Tests type refinement through conditional check (null narrowing)
  @Test
  public void testTypeRefinement_nullCheck_narrowsType() {
    inFunction("/** @type {?string} */ var x = null; if (x !== null) { var y = x; }");
    assertEquals(getNativeType(STRING_TYPE), getType("y"));
  }

  // Tests type refinement through typeof operator
  @Test
  public void testTypeRefinement_typeofCheck_narrowsType() {
    inFunction("/** @type {number|string} */ var x = 1; if (typeof x === 'string') { var y = x; }");
    assertEquals(getNativeType(STRING_TYPE), getType("y"));
  }

  // Tests type refinement through instanceof operator
  @Test
  public void testTypeRefinement_instanceofCheck_narrowsType() {
    inFunction("/** @type {Object} */ var x = {}; if (x instanceof Array) { var y = x; }");
    assertEquals(getNativeType(ARRAY_TYPE), getType("y"));
  }

  // Tests function expression inference
  @Test
  public void testTraverseFunctionExpression_infersFunctionType() {
    inFunction("var fn = function(a, b) { return a + b; };");
    assertNotNull(getType("fn"));
    assertTrue(getType("fn").isFunctionType());
  }

  // Tests explicit type casting annotation
  @Test
  public void testTraverseCast_infersExplicitType() {
    inFunction("var x = /** @type {number} */ ('123');");
    assertEquals(getNativeType(NUMBER_TYPE), getType("x"));
  }
}
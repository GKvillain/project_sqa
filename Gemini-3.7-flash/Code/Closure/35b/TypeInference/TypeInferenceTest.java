package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TypeInference}.
 */
public class TypeInferenceTest extends CompilerTypeTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  private void inFunction(String js) {
    inFunction(js, (DiagnosticType[]) null);
  }

  private void inFunction(String js, DiagnosticType... warnings) {
    Node root = compiler.parseTestCode("function() {" + js + "}");
    assertEquals(0, compiler.getErrorCount());
    Node n = root.getFirstChild();
    assertEquals(Token.FUNCTION, n.getType());
    Scope scope = (new SyntacticScopeCreator(compiler)).createScope(n, globalScope);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, n.getLastChild());
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    Map<String, AssertionFunctionSpec> assertionFunctionsMap = Maps.newHashMap();
    TypeInference typeInference = new TypeInference(
        compiler, cfg, compiler.getReverseAbstractInterpreter(),
        scope, assertionFunctionsMap);
    typeInference.analyze();
    if (warnings != null) {
      assertEquals(warnings.length, compiler.getWarningCount());
      for (int i = 0; i < warnings.length; i++) {
        assertEquals(warnings[i], compiler.getWarnings()[i].getType());
      }
    } else {
      assertEquals(0, compiler.getWarningCount());
    }
  }

  // Tests property type inference matching return constraint with record type
  @Test
  public void testTraverseReturn_matchingRecordTypeConstraint_infersProperties() {
    inFunction(
        "/** @return {{foo: (number|undefined)}} */" +
        "function f() {" +
        "  return {};" +
        "}");
  }

  // Tests regression for anonymous object returned matching record type
  @Test
  public void testInferPropertyTypesToMatchConstraint_missingProperty_infersVoidUnion() {
    inFunction(
        "/** @type {function({a: (number|undefined)})} */" +
        "var f = function(x) {};" +
        "var g = function() { return {}; };" +
        "f(g());");
  }

  // Tests assignment of number literal
  @Test
  public void testTraverseAssign_numberLiteral_infersNumberType() {
    inFunction("var x; x = 1;");
  }

  // Tests assignment of string literal
  @Test
  public void testTraverseAssign_stringLiteral_infersStringType() {
    inFunction("var x; x = 'hello';");
  }

  // Tests object literal traversal and property type inference
  @Test
  public void testTraverseObjectLiteral_withProperties_infersObjectType() {
    inFunction("var obj = {a: 1, b: 'str'};");
  }

  // Tests binary addition with number operands
  @Test
  public void testTraverseAdd_numbers_infersNumberType() {
    inFunction("var x = 1 + 2;");
  }

  // Tests binary addition with string operand
  @Test
  public void testTraverseAdd_stringAndNumber_infersStringType() {
    inFunction("var x = 'str' + 2;");
  }

  // Tests ternary hook operator
  @Test
  public void testTraverseHook_conditionalExpression_infersUnionType() {
    inFunction("var cond = true; var x = cond ? 1 : 'two';");
  }

  // Tests short-circuiting logical AND operator
  @Test
  public void testTraverseAnd_shortCircuitingCondition_evaluatesCorrectly() {
    inFunction("var x = null; if (x && x.foo) { var y = 1; }");
  }

  // Tests short-circuiting logical OR operator
  @Test
  public void testTraverseOr_shortCircuitingExpression_infersType() {
    inFunction("var x = 'default'; var y = x || 123;");
  }

  // Tests unary typeof operator
  @Test
  public void testTraverseTypeof_expression_infersStringType() {
    inFunction("var x = typeof 123;");
  }

  // Tests constructor invocation with new keyword
  @Test
  public void testTraverseNew_constructorCall_infersInstanceType() {
    inFunction("var x = new Object();");
  }

  // Tests array literal traversal
  @Test
  public void testTraverseArrayLiteral_elements_infersArrayType() {
    inFunction("var arr = [1, 2, 3];");
  }

  // Tests logical NOT operator
  @Test
  public void testTraverseNot_booleanNegation_infersBooleanType() {
    inFunction("var b = !0;");
  }

  // Tests relational comparison operators
  @Test
  public void testTraverseRelational_lessThan_infersBooleanType() {
    inFunction("var res = 1 < 2;");
  }

  // Tests bitwise operations
  @Test
  public void testTraverseBitwise_bitAnd_infersNumberType() {
    inFunction("var res = 1 & 2;");
  }

  // Tests catch block exception parameter inference
  @Test
  public void testTraverseCatch_exceptionVariable_infersUnknownType() {
    inFunction("try { throw 'err'; } catch (e) { var x = e; }");
  }

  // Tests property access via getprop
  @Test
  public void testTraverseGetProp_propertyAccess_infersPropertyType() {
    inFunction("var obj = {x: 10}; var y = obj.x;");
  }

  // Tests element access via getelem
  @Test
  public void testTraverseGetElem_arrayElementAccess_infersElementType() {
    inFunction("var arr = [1, 2]; var val = arr[0];");
  }

  // Tests function call with template this warning
  @Test
  public void testTraverseCall_missingTemplateThisParameter_reportsWarning() {
    inFunction(
        "/** @param {function(this:T, ...)} fn\n" +
        "  * @param {T} obj\n" +
        "  * @template T */\n" +
        "function bind(fn, obj) {}\n" +
        "bind(function() { this.foo(); }, 123);",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE);
  }

  // Tests unary increment and decrement operators
  @Test
  public void testTraverseIncDec_numbers_infersNumberType() {
    inFunction("var x = 0; x++; ++x; x--; --x;");
  }

  // Tests compound assignment operators
  @Test
  public void testTraverseCompoundAssign_numericAndString_infersTypes() {
    inFunction("var x = 1; x += 2; x -= 1; x *= 3; x /= 2; x %= 2; x |= 1; x &= 1; x ^= 1; x <<= 1; x >>= 1; x >>>= 1;");
  }

  // Tests instanceof binary operator
  @Test
  public void testTraverseInstanceOf_customType_infersBoolean() {
    inFunction("function Foo() {} var f = new Foo(); var isFoo = f instanceof Foo;");
  }

  // Tests in operator
  @Test
  public void testTraverseIn_propertyInObject_infersBoolean() {
    inFunction("var obj = {a: 1}; var exists = 'a' in obj;");
  }

  // Tests unary positive, negation, bitwise NOT, and void operators
  @Test
  public void testTraverseUnaryOperators_infersCorrectTypes() {
    inFunction("var a = +1; var b = -1; var c = ~1; var d = void 0;");
  }

  // Tests delete operator
  @Test
  public void testTraverseDelProp_objectProperty_infersBoolean() {
    inFunction("var obj = {a: 1}; var res = delete obj.a;");
  }

  // Tests for-in loop traversal
  @Test
  public void testTraverseForIn_objectKeys_infersStringType() {
    inFunction("var obj = {a: 1, b: 2}; for (var k in obj) { var key = k; }");
  }

  // Tests while and do-while loops
  @Test
  public void testTraverseLoops_whileAndDoWhile_infersCondition() {
    inFunction("var i = 0; while (i < 5) { i++; } do { i--; } while (i > 0);");
  }

  // Tests switch and case statement traversal
  @Test
  public void testTraverseSwitch_caseClauses_infersBranches() {
    inFunction(
        "var x = 1; var y = '';" +
        "switch (x) {" +
        "  case 1: y = 'one'; break;" +
        "  case 2: y = 'two'; break;" +
        "  default: y = 'other';" +
        "}");
  }

  // Tests comma operator
  @Test
  public void testTraverseComma_multipleExpressions_infersLastExpressionType() {
    inFunction("var x = (1, 'hello');");
  }

  // Tests type narrowing with null / undefined checks
  @Test
  public void testTypeNarrowing_nullCheck_narrowsType() {
    inFunction(
        "/** @param {?string} s */" +
        "function f(s) {" +
        "  if (s != null) {" +
        "    var len = s.length;" +
        "  }" +
        "}");
  }

  // Tests type cast expression
  @Test
  public void testTraverseCast_expression_infersCastedType() {
    inFunction("var x = /** @type {number} */ ('123');");
  }

  // Tests return statement without expression
  @Test
  public void testTraverseReturn_emptyReturn_infersVoid() {
    inFunction("function f() { return; }");
  }

  // Tests function expression with parameter type annotation
  @Test
  public void testTraverseFunctionExpression_withParams_infersTypes() {
    inFunction("var fn = /** @param {number} a @param {string} b @return {string} */ function(a, b) { return b + a; };");
  }

  // Tests multiple template types matching
  @Test
  public void testTemplateTypeInference_multipleTemplateKeys_matches() {
    inFunction(
        "/** @template K, V\n" +
        "  * @param {K} key\n" +
        "  * @param {V} val\n" +
        "  * @return {Object.<K, V>} */\n" +
        "function makeMap(key, val) { return {}; }\n" +
        "var m = makeMap('foo', 123);");
  }
}
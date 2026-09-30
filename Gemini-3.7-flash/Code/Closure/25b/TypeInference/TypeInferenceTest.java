package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.DataFlowAnalysis.BranchedFlowState;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class TypeInferenceTest extends CompilerTypeTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  private FlowScope inFunction(String js) {
    return inFunction(js, null);
  }

  private FlowScope inFunction(String js, DiagnosticType warning) {
    String script = "function() {" + js + "};";
    Node root = compiler.parseTestCode(script);
    assertEquals(0, compiler.getErrorCount());
    Node functionNode = root.getFirstChild().getFirstChild();
    ControlFlowGraph<Node> cfg =
        ControlFlowAnalysis.computeCfg(functionNode.getLastChild(), true);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);
    Scope functionScope = scopeCreator.createScope(functionNode, globalScope);
    Map<String, AssertionFunctionSpec> assertions = Maps.newHashMap();
    ReverseAbstractInterpreter rai =
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry);
    TypeInference inference = new TypeInference(
        compiler, cfg, rai,
        functionScope, assertions);
    inference.analyze();
    if (warning != null) {
      assertEquals(1, compiler.getWarningCount());
      assertEquals(warning, compiler.getWarnings()[0].getType());
    } else {
      assertEquals(0, compiler.getWarningCount());
    }
    BranchedFlowState<FlowScope> state = cfg.getImplicitReturn().getAnnotation();
    return state.getIn();
  }

  private JSType getSlotType(FlowScope flowScope, String name) {
    StaticSlot<JSType> slot = flowScope.getSlot(name);
    return slot == null ? null : slot.getType();
  }

  private JSType getNativeType(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }

  // Tests constructor backwards inference for object argument properties
  @Test
  public void testTraverseNew_backwardsInferenceOnArguments_infersProperty() {
    FlowScope scope = inFunction(
        "/** @constructor */ function F(/** {foo: number} */ x) {};" +
        "var y = {};" +
        "new F(y);");
    ObjectType yType = ObjectType.cast(getSlotType(scope, "y"));
    assertNotNull(yType);
    assertTrue(yType.hasProperty("foo"));
    assertTrue(yType.getPropertyType("foo").isSubtype(getNativeType(NUMBER_TYPE)));
  }

  // Tests constructor invocation with unknown constructor type
  @Test
  public void testTraverseNew_unknownConstructor_createsUnknownInstance() {
    FlowScope scope = inFunction("var x = new unknownConstructor();");
    JSType xType = getSlotType(scope, "x");
    assertNotNull(xType);
    assertTrue(xType.isUnknownType());
  }

  // Tests assignment of number literal to local variable
  @Test
  public void testTraverseAssign_numberLiteral_infersNumberType() {
    FlowScope scope = inFunction("var x = 1;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
  }

  // Tests assignment of string literal to local variable
  @Test
  public void testTraverseAssign_stringLiteral_infersStringType() {
    FlowScope scope = inFunction("var x = 'hello';");
    assertEquals(getNativeType(STRING_TYPE), getSlotType(scope, "x"));
  }

  // Tests addition of two numbers infers number type
  @Test
  public void testTraverseAdd_numbers_infersNumberType() {
    FlowScope scope = inFunction("var x = 1 + 2;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
  }

  // Tests addition involving a string infers string type
  @Test
  public void testTraverseAdd_stringAndNumber_infersStringType() {
    FlowScope scope = inFunction("var x = 'str' + 1;");
    assertEquals(getNativeType(STRING_TYPE), getSlotType(scope, "x"));
  }

  // Tests array literal traversal infers Array type
  @Test
  public void testTraverseArrayLiteral_elements_infersArrayType() {
    FlowScope scope = inFunction("var x = [1, 2, 3];");
    assertEquals(getNativeType(ARRAY_TYPE), getSlotType(scope, "x"));
  }

  // Tests object literal traversal infers properties on anonymous object
  @Test
  public void testTraverseObjectLiteral_withProperties_infersObjectProperties() {
    FlowScope scope = inFunction("var x = {a: 10, b: 'str'};");
    ObjectType xType = ObjectType.cast(getSlotType(scope, "x"));
    assertNotNull(xType);
    assertTrue(xType.hasProperty("a"));
    assertTrue(xType.hasProperty("b"));
    assertEquals(getNativeType(NUMBER_TYPE), xType.getPropertyType("a"));
    assertEquals(getNativeType(STRING_TYPE), xType.getPropertyType("b"));
  }

  // Tests hook (ternary) operator joins branch types
  @Test
  public void testTraverseHook_differentBranchTypes_joinsTypes() {
    FlowScope scope = inFunction("var condition = true; var x = condition ? 1 : 'str';");
    JSType xType = getSlotType(scope, "x");
    assertNotNull(xType);
    assertTrue(getNativeType(NUMBER_TYPE).isSubtype(xType));
    assertTrue(getNativeType(STRING_TYPE).isSubtype(xType));
  }

  // Tests short-circuiting AND operator
  @Test
  public void testTraverseAnd_booleanOperands_infersResult() {
    FlowScope scope = inFunction("var x = true && false;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests short-circuiting OR operator
  @Test
  public void testTraverseOr_booleanOperands_infersResult() {
    FlowScope scope = inFunction("var x = false || true;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests unary typeof operator returns string type
  @Test
  public void testTraverseTypeOf_anyOperand_infersStringType() {
    FlowScope scope = inFunction("var x = typeof 123;");
    assertEquals(getNativeType(STRING_TYPE), getSlotType(scope, "x"));
  }

  // Tests comparison operators return boolean type
  @Test
  public void testTraverseComparison_lessThan_infersBooleanType() {
    FlowScope scope = inFunction("var x = 1 < 2;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests bitwise/arithmetic unary operators return number type
  @Test
  public void testTraverseUnaryNumeric_negation_infersNumberType() {
    FlowScope scope = inFunction("var x = -5;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
  }

  // Tests backwards inference from function call on function literal argument
  @Test
  public void testTraverseCall_functionArgument_infersParameterFunctionType() {
    FlowScope scope = inFunction(
        "/** @param {function(number): string} fn */ function f(fn) {};" +
        "var g = function(a) { return ''; };" +
        "f(g);");
    JSType gType = getSlotType(scope, "g");
    assertNotNull(gType);
    assertTrue(gType.isFunctionType());
  }

  // Tests uninitialized local variable starts with void type
  @Test
  public void testTraverseVar_uninitialized_infersVoidType() {
    FlowScope scope = inFunction("var x;");
    assertEquals(getNativeType(VOID_TYPE), getSlotType(scope, "x"));
  }

  // Tests catch block param infers unknown type
  @Test
  public void testTraverseCatch_exceptionParam_infersUnknownType() {
    FlowScope scope = inFunction("try { } catch (e) { var x = e; }");
    JSType eType = getSlotType(scope, "e");
    assertNotNull(eType);
    assertTrue(eType.isUnknownType());
  }

  // Tests null literal assignment infers null type
  @Test
  public void testTraverseAssign_nullLiteral_infersNullType() {
    FlowScope scope = inFunction("var x = null;");
    assertEquals(getNativeType(NULL_TYPE), getSlotType(scope, "x"));
  }

  // Tests logical NOT operator returns boolean type
  @Test
  public void testTraverseNot_numberOperand_infersBooleanType() {
    FlowScope scope = inFunction("var x = !0;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests bitwise binary operators return number type
  @Test
  public void testTraverseBitwiseOp_numbers_infersNumberType() {
    FlowScope scope = inFunction("var x = 1 | 2; var y = 3 & 4; var z = 5 ^ 6;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "y"));
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "z"));
  }

  // Tests multiplication and division operators return number type
  @Test
  public void testTraverseMultiplyAndDivide_numbers_infersNumberType() {
    FlowScope scope = inFunction("var x = 4 * 2; var y = 4 / 2; var z = 4 % 2;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "y"));
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "z"));
  }

  // Tests increment and decrement operators return number type
  @Test
  public void testTraverseIncDec_variable_infersNumberType() {
    FlowScope scope = inFunction("var x = 1; x++; var y = --x;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "y"));
  }

  // Tests instanceof operator returns boolean type
  @Test
  public void testTraverseInstanceOf_infersBooleanType() {
    FlowScope scope = inFunction("var x = {} instanceof Object;");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests in operator returns boolean type
  @Test
  public void testTraverseInOperator_infersBooleanType() {
    FlowScope scope = inFunction("var x = 'foo' in {};");
    assertEquals(getNativeType(BOOLEAN_TYPE), getSlotType(scope, "x"));
  }

  // Tests property access on known object infers member type
  @Test
  public void testTraverseGetProp_definedProperty_infersPropType() {
    FlowScope scope = inFunction("var obj = {prop: 42}; var x = obj.prop;");
    assertEquals(getNativeType(NUMBER_TYPE), getSlotType(scope, "x"));
  }

  // Tests if-else branches merging types on flow scope
  @Test
  public void testTraverseIfElse_mergeScopes_joinsTypes() {
    FlowScope scope = inFunction(
        "var x; var cond = true;" +
        "if (cond) { x = 10; } else { x = 'str'; }");
    JSType xType = getSlotType(scope, "x");
    assertNotNull(xType);
    assertTrue(getNativeType(NUMBER_TYPE).isSubtype(xType));
    assertTrue(getNativeType(STRING_TYPE).isSubtype(xType));
  }
}
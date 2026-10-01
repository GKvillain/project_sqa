package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter reverseInterpreter;
  private Map<String, AssertionFunctionSpec> assertionFunctionsMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    reverseInterpreter = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    assertionFunctionsMap = Maps.newHashMap();
  }

  private Node parseAndInfer(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(root, null);
    ControlFlowGraph<Node> cfg = ControlFlowAnalysis.computeFallThroughCfg(root);
    TypeInference typeInference = new TypeInference(
        compiler, cfg, reverseInterpreter, topScope, assertionFunctionsMap);
    typeInference.analyze();
    return root;
  }

  private Node findFirstNode(Node root, int tokenType) {
    if (root.getType() == tokenType) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNode(child, tokenType);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // Tests getBooleanOutcomes with true condition and non-empty sets
  @Test
  public void testGetBooleanOutcomes_conditionTrue_returnsUnionWithIntersection() {
    BooleanLiteralSet left = BooleanLiteralSet.BOTH;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes with false condition
  @Test
  public void testGetBooleanOutcomes_conditionFalse_returnsUnionWithIntersection() {
    BooleanLiteralSet left = BooleanLiteralSet.FALSE;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, false);
    assertEquals(BooleanLiteralSet.TRUE, result);
  }

  // Tests FUNCTION_LITERAL_UNDEFINED_THIS constant definition
  @Test
  public void testDiagnosticType_functionLiteralUndefinedThis_isDefined() {
    assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  // Tests initial lattice and entry lattice creation on TypeInference instance
  @Test
  public void testCreateEntryAndInitialEstimateLattice_notNull() {
    Node root = compiler.parseTestCode("var x = 10;");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(root, null);
    ControlFlowGraph<Node> cfg = ControlFlowAnalysis.computeFallThroughCfg(root);

    TypeInference typeInference = new TypeInference(
        compiler, cfg, reverseInterpreter, scope, assertionFunctionsMap);

    FlowScope entry = typeInference.createEntryLattice();
    FlowScope initial = typeInference.createInitialEstimateLattice();

    assertNotNull(entry);
    assertNotNull(initial);
    assertNotSame(entry, initial);
  }

  // Tests flowThrough with bottom scope returns bottom scope unmodified
  @Test
  public void testFlowThrough_bottomScopeInput_returnsBottomScope() {
    Node root = compiler.parseTestCode("var a = 1;");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(root, null);
    ControlFlowGraph<Node> cfg = ControlFlowAnalysis.computeFallThroughCfg(root);

    TypeInference typeInference = new TypeInference(
        compiler, cfg, reverseInterpreter, scope, assertionFunctionsMap);

    FlowScope bottom = typeInference.createInitialEstimateLattice();
    FlowScope result = typeInference.flowThrough(root, bottom);
    assertSame(bottom, result);
  }

  // Tests arithmetic addition type inference with numeric operands
  @Test
  public void testTraverseAdd_numbers_infersNumberType() {
    Node root = parseAndInfer("var x = 1 + 2;");
    Node addNode = findFirstNode(root, Token.ADD);
    assertNotNull(addNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), addNode.getJSType());
  }

  // Tests addition type inference with string operand
  @Test
  public void testTraverseAdd_stringAndNumber_infersStringType() {
    Node root = parseAndInfer("var x = 'hello' + 2;");
    Node addNode = findFirstNode(root, Token.ADD);
    assertNotNull(addNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), addNode.getJSType());
  }

  // Tests array literal type inference
  @Test
  public void testTraverseArrayLiteral_infersArrayType() {
    Node root = parseAndInfer("var arr = [1, 2, 3];");
    Node arrayNode = findFirstNode(root, Token.ARRAYLIT);
    assertNotNull(arrayNode);
    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), arrayNode.getJSType());
  }

  // Tests unary operators: negation, pos, bitwise not, not
  @Test
  public void testTraverseUnaryOperators_infersNumberAndBoolean() {
    Node root = parseAndInfer("var a = -5; var b = !0; var c = ~2; var d = typeof a;");
    Node negNode = findFirstNode(root, Token.NEG);
    assertNotNull(negNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), negNode.getJSType());

    Node notNode = findFirstNode(root, Token.NOT);
    assertNotNull(notNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), notNode.getJSType());

    Node bitNotNode = findFirstNode(root, Token.BITNOT);
    assertNotNull(bitNotNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), bitNotNode.getJSType());

    Node typeofNode = findFirstNode(root, Token.TYPEOF);
    assertNotNull(typeofNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), typeofNode.getJSType());
  }

  // Tests comparison expressions yielding boolean type
  @Test
  public void testTraverseComparison_infersBooleanType() {
    Node root = parseAndInfer("var cmp = (1 < 2) && (3 == 3);");
    Node ltNode = findFirstNode(root, Token.LT);
    assertNotNull(ltNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), ltNode.getJSType());

    Node eqNode = findFirstNode(root, Token.EQ);
    assertNotNull(eqNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), eqNode.getJSType());
  }

  // Tests ternary hook operator type inference
  @Test
  public void testTraverseHook_infersUnionOfBranches() {
    Node root = parseAndInfer("var h = true ? 10 : 'str';");
    Node hookNode = findFirstNode(root, Token.HOOK);
    assertNotNull(hookNode);
    JSType hookType = hookNode.getJSType();
    assertNotNull(hookType);
    assertTrue(hookType.isUnionType());
  }

  // Tests logical AND expression type inference
  @Test
  public void testTraverseAnd_infersOutcomeType() {
    Node root = parseAndInfer("var a = 1 && 2;");
    Node andNode = findFirstNode(root, Token.AND);
    assertNotNull(andNode);
    assertNotNull(andNode.getJSType());
  }

  // Tests logical OR expression type inference
  @Test
  public void testTraverseOr_infersOutcomeType() {
    Node root = parseAndInfer("var o = 0 || 'default';");
    Node orNode = findFirstNode(root, Token.OR);
    assertNotNull(orNode);
    assertNotNull(orNode.getJSType());
  }

  // Tests catch block variable type inference (unknown type)
  @Test
  public void testTraverseCatch_infersUnknownTypeForExceptionVar() {
    Node root = parseAndInfer("try { throw 'err'; } catch (e) { var x = e; }");
    Node catchNode = findFirstNode(root, Token.CATCH);
    assertNotNull(catchNode);
    Node param = catchNode.getFirstChild();
    assertNotNull(param);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), param.getJSType());
  }

  // Tests object literal type inference and property traversal
  @Test
  public void testTraverseObjectLiteral_infersObjectType() {
    Node root = parseAndInfer("var obj = { x: 10, y: 'str' };");
    Node objLit = findFirstNode(root, Token.OBJECTLIT);
    assertNotNull(objLit);
    JSType objType = objLit.getJSType();
    assertNotNull(objType);
    assertTrue(objType.isObjectType());
  }

  // Tests template type inference in generic function call
  @Test
  public void testInferTemplatedTypesForCall_resolvesTemplateKeys() {
    Node root = parseAndInfer(
        "/**\n" +
        " * @param {T} a\n" +
        " * @return {T}\n" +
        " * @template T\n" +
        " */\n" +
        "function identity(a) { return a; }\n" +
        "var result = identity('hello');");
    Node callNode = findFirstNode(root, Token.CALL);
    assertNotNull(callNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), callNode.getJSType());
  }

  // Tests template type inference when call site does not provide matching arguments
  @Test
  public void testInferTemplatedTypesForCall_emptyArgs_noCrash() {
    Node root = parseAndInfer(
        "/**\n" +
        " * @param {Array.<T>=} opt_arr\n" +
        " * @return {T}\n" +
        " * @template T\n" +
        " */\n" +
        "function getFirst(opt_arr) {}\n" +
        "var result = getFirst();");
    Node callNode = findFirstNode(root, Token.CALL);
    assertNotNull(callNode);
    assertNotNull(callNode.getJSType());
  }

  // Tests return statement type constraint inference
  @Test
  public void testTraverseReturn_matchesReturnTypeConstraint() {
    Node root = parseAndInfer(
        "/** @return {number} */\n" +
        "function getNum() { return 42; }");
    Node returnNode = findFirstNode(root, Token.RETURN);
    assertNotNull(returnNode);
    Node retValue = returnNode.getFirstChild();
    assertNotNull(retValue);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), retValue.getJSType());
  }

  // Tests property access GETPROP type narrowing
  @Test
  public void testTraverseGetProp_narrowsPropertyType() {
    Node root = parseAndInfer("var a = { prop: 123 }; var b = a.prop;");
    Node getPropNode = findFirstNode(root, Token.GETPROP);
    assertNotNull(getPropNode);
    assertNotNull(getPropNode.getJSType());
  }

  // Tests GETELEM element access
  @Test
  public void testTraverseGetElem_infersElementType() {
    Node root = parseAndInfer("var arr = [1, 2]; var el = arr[0];");
    Node getElemNode = findFirstNode(root, Token.GETELEM);
    assertNotNull(getElemNode);
    assertNotNull(getElemNode.getJSType());
  }
}
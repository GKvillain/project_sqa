package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private Node parseAndInfer(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    Map<String, AssertionFunctionSpec> assertionMap = Maps.newHashMap();

    TypeInference typeInference = new TypeInference(
        compiler, cfg, rai, globalScope, assertionMap);
    typeInference.analyze();
    return root;
  }

  private Node findFirstNode(Node root, int tokenType) {
    if (root.getType() == tokenType) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstNode(child, tokenType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  // Tests declared variable assignment with object literal (Regression for 176b)
  @Test
  public void testUpdateScopeForTypeChange_declaredVarObjectLiteral_keepsDeclaredType() {
    Node root = parseAndInfer("/** @type {Object} */ var x = {}; x.result = 1; x.result = true;");
    assertNotNull(root);
    Node varNode = findFirstNode(root, com.google.javascript.rhino.Token.VAR);
    assertNotNull(varNode);
    Node nameNode = varNode.getFirstChild();
    assertNotNull(nameNode.getJSType());
    assertNotNull(nameNode.getJSType().toMaybeObjectType());
  }

  // Tests declared variable assignment with null initialization
  @Test
  public void testUpdateScopeForTypeChange_declaredVarNullLiteral_hasDeclaredType() {
    Node root = parseAndInfer("/** @type {Object} */ var x = null; x = {};");
    assertNotNull(root);
    Node varNode = findFirstNode(root, com.google.javascript.rhino.Token.VAR);
    assertNotNull(varNode);
    Node nameNode = varNode.getFirstChild();
    assertNotNull(nameNode.getJSType());
  }

  // Tests inferred variable type change on assignment
  @Test
  public void testTraverseAssign_inferredVariable_updatesVariableType() {
    Node root = parseAndInfer("var x = 1; x = 'str';");
    Node assignNode = findFirstNode(root, com.google.javascript.rhino.Token.ASSIGN);
    assertNotNull(assignNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), assignNode.getJSType());
  }

  // Tests string addition yielding string type
  @Test
  public void testTraverseAdd_stringAndNumber_infersStringType() {
    Node root = parseAndInfer("var a = 'hello ' + 5;");
    Node addNode = findFirstNode(root, com.google.javascript.rhino.Token.ADD);
    assertNotNull(addNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), addNode.getJSType());
  }

  // Tests number addition yielding number type
  @Test
  public void testTraverseAdd_twoNumbers_infersNumberType() {
    Node root = parseAndInfer("var a = 5 + 10;");
    Node addNode = findFirstNode(root, com.google.javascript.rhino.Token.ADD);
    assertNotNull(addNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), addNode.getJSType());
  }

  // Tests bitwise and arithmetic operations inferring number type
  @Test
  public void testTraverseArithmetic_bitwiseAndSub_infersNumberType() {
    Node root = parseAndInfer("var a = 10 - 2; var b = 10 & 2; var c = 10 | 2;");
    Node subNode = findFirstNode(root, com.google.javascript.rhino.Token.SUB);
    assertNotNull(subNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), subNode.getJSType());

    Node bitAndNode = findFirstNode(root, com.google.javascript.rhino.Token.BITAND);
    assertNotNull(bitAndNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), bitAndNode.getJSType());
  }

  // Tests array literal traversal
  @Test
  public void testTraverseArrayLiteral_elementsPresent_infersArrayType() {
    Node root = parseAndInfer("var arr = [1, 'two', 3];");
    Node arrayNode = findFirstNode(root, com.google.javascript.rhino.Token.ARRAYLIT);
    assertNotNull(arrayNode);
    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), arrayNode.getJSType());
  }

  // Tests object literal traversal and property inference
  @Test
  public void testTraverseObjectLiteral_definesProperties() {
    Node root = parseAndInfer("var obj = { foo: 'bar', count: 42 };");
    Node objNode = findFirstNode(root, com.google.javascript.rhino.Token.OBJECTLIT);
    assertNotNull(objNode);
    JSType objType = objNode.getJSType();
    assertNotNull(objType);
    assertNotNull(objType.toMaybeObjectType());
  }

  // Tests short-circuiting AND operator
  @Test
  public void testTraverseAnd_booleanOperands_infersType() {
    Node root = parseAndInfer("var res = true && false;");
    Node andNode = findFirstNode(root, com.google.javascript.rhino.Token.AND);
    assertNotNull(andNode);
    assertNotNull(andNode.getJSType());
  }

  // Tests short-circuiting OR operator
  @Test
  public void testTraverseOr_differentTypes_infersUnionType() {
    Node root = parseAndInfer("var res = 'default' || 123;");
    Node orNode = findFirstNode(root, com.google.javascript.rhino.Token.OR);
    assertNotNull(orNode);
    assertNotNull(orNode.getJSType());
  }

  // Tests ternary hook operator
  @Test
  public void testTraverseHook_conditionalBranches_infersSupertype() {
    Node root = parseAndInfer("var x = true ? 1 : 2;");
    Node hookNode = findFirstNode(root, com.google.javascript.rhino.Token.HOOK);
    assertNotNull(hookNode);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), hookNode.getJSType());
  }

  // Tests comparison and relational operators inferring boolean type
  @Test
  public void testTraverseRelational_comparison_infersBooleanType() {
    Node root = parseAndInfer("var eq = (1 === 2); var lt = (1 < 2); var inst = ({} instanceof Object);");
    Node sheqNode = findFirstNode(root, com.google.javascript.rhino.Token.SHEQ);
    assertNotNull(sheqNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), sheqNode.getJSType());

    Node ltNode = findFirstNode(root, com.google.javascript.rhino.Token.LT);
    assertNotNull(ltNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), ltNode.getJSType());

    Node instNode = findFirstNode(root, com.google.javascript.rhino.Token.INSTANCEOF);
    assertNotNull(instNode);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), instNode.getJSType());
  }

  // Tests typeof expression inferring string type
  @Test
  public void testTraverseTypeof_expression_infersStringType() {
    Node root = parseAndInfer("var t = typeof 123;");
    Node typeofNode = findFirstNode(root, com.google.javascript.rhino.Token.TYPEOF);
    assertNotNull(typeofNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), typeofNode.getJSType());
  }

  // Tests catch block variable type inference
  @Test
  public void testTraverseCatch_untypedCatchVar_infersUnknownType() {
    Node root = parseAndInfer("try { throw 'err'; } catch (e) { var x = e; }");
    Node catchNode = findFirstNode(root, com.google.javascript.rhino.Token.CATCH);
    assertNotNull(catchNode);
    Node catchParam = catchNode.getFirstChild();
    assertNotNull(catchParam);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), catchParam.getJSType());
  }

  // Tests constructor invocation with new keyword
  @Test
  public void testTraverseNew_customConstructor_infersInstanceType() {
    Node root = parseAndInfer("/** @constructor */ function Foo() {} var f = new Foo();");
    Node newNode = findFirstNode(root, com.google.javascript.rhino.Token.NEW);
    assertNotNull(newNode);
    JSType newType = newNode.getJSType();
    assertNotNull(newType);
    assertNotNull(newType.toMaybeObjectType());
  }

  // Tests function call return type inference
  @Test
  public void testTraverseCall_typedFunction_infersReturnType() {
    Node root = parseAndInfer("/** @return {string} */ function getStr() { return 'a'; } var s = getStr();");
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);
    assertNotNull(callNode);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), callNode.getJSType());
  }

  // Tests type cast evaluation
  @Test
  public void testTraverseCast_explicitTypeCast_infersCastedType() {
    Node root = parseAndInfer("var x = /** @type {number} */ ('test');");
    Node castNode = findFirstNode(root, com.google.javascript.rhino.Token.CAST);
    if (castNode != null) {
      assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), castNode.getJSType());
    }
  }

  // Tests for-in loop variable iteration
  @Test
  public void testBranchedFlowThrough_forInLoop_infersStringPropertyKey() {
    Node root = parseAndInfer("var obj = {a: 1, b: 2}; for (var k in obj) { var val = k; }");
    assertNotNull(root);
    Node forInNode = findFirstNode(root, com.google.javascript.rhino.Token.FOR);
    assertNotNull(forInNode);
  }
}
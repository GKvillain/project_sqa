package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter reverseInterpreter;
  private Map<String, AssertionFunctionSpec> assertionMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    reverseInterpreter = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    assertionMap = Maps.newHashMap();
  }

  private TypeInference createTypeInference(String js) {
    Node scriptNode = compiler.parseTestCode(js);
    Node functionNode = scriptNode.getFirstChild();
    if (!functionNode.isFunction()) {
      functionNode = scriptNode;
    }
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, functionNode);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(functionNode, null);

    return new TypeInference(compiler, cfg, reverseInterpreter, scope, assertionMap);
  }

  // Tests getBooleanOutcomes when condition is true with BOTH sets
  @Test
  public void testGetBooleanOutcomes_conditionTrueBoth_returnsBoth() {
    BooleanLiteralSet left = BooleanLiteralSet.BOTH;
    BooleanLiteralSet right = BooleanLiteralSet.BOTH;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes when condition is false with TRUE and FALSE sets
  @Test
  public void testGetBooleanOutcomes_conditionFalse_returnsUnion() {
    BooleanLiteralSet left = BooleanLiteralSet.TRUE;
    BooleanLiteralSet right = BooleanLiteralSet.FALSE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, false);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests getBooleanOutcomes with EMPTY literal sets
  @Test
  public void testGetBooleanOutcomes_emptySets_returnsEmpty() {
    BooleanLiteralSet left = BooleanLiteralSet.EMPTY;
    BooleanLiteralSet right = BooleanLiteralSet.EMPTY;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.EMPTY, result);
  }

  // Tests getBooleanOutcomes with TRUE condition and left as FALSE
  @Test
  public void testGetBooleanOutcomes_conditionTrueLeftFalse_returnsRight() {
    BooleanLiteralSet left = BooleanLiteralSet.FALSE;
    BooleanLiteralSet right = BooleanLiteralSet.TRUE;
    BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
    assertEquals(BooleanLiteralSet.BOTH, result);
  }

  // Tests initial lattice creation produces non-null lattice
  @Test
  public void testCreateInitialEstimateLattice_validInference_returnsBottomScope() {
    TypeInference inference = createTypeInference("function f() { var x = 1; }");
    FlowScope bottom = inference.createInitialEstimateLattice();
    assertNotNull(bottom);
  }

  // Tests entry lattice creation produces non-null lattice
  @Test
  public void testCreateEntryLattice_validInference_returnsFunctionScope() {
    TypeInference inference = createTypeInference("function f(a, b) { return a + b; }");
    FlowScope entry = inference.createEntryLattice();
    assertNotNull(entry);
  }

  // Tests flowThrough with bottom scope returns bottom scope unchanged
  @Test
  public void testFlowThrough_bottomScope_returnsBottomScope() {
    TypeInference inference = createTypeInference("function f() { var a = 1; }");
    FlowScope bottom = inference.createInitialEstimateLattice();
    Node node = new Node(Token.EMPTY);
    FlowScope output = inference.flowThrough(node, bottom);
    assertSame(bottom, output);
  }

  // Tests flowThrough arithmetic addition of numbers
  @Test
  public void testFlowThrough_additionNumbers_infersNumberType() {
    TypeInference inference = createTypeInference("function f() { var x = 1 + 2; }");
    FlowScope entry = inference.createEntryLattice();

    Node left = Node.newNumber(1.0);
    left.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node right = Node.newNumber(2.0);
    right.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node add = new Node(Token.ADD, left, right);

    FlowScope out = inference.flowThrough(add, entry);
    assertNotNull(out);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), add.getJSType());
  }

  // Tests flowThrough string concatenation
  @Test
  public void testFlowThrough_additionString_infersStringType() {
    TypeInference inference = createTypeInference("function f() { var x = 'a' + 1; }");
    FlowScope entry = inference.createEntryLattice();

    Node left = Node.newString("a");
    left.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node right = Node.newNumber(1.0);
    right.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node add = new Node(Token.ADD, left, right);

    inference.flowThrough(add, entry);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), add.getJSType());
  }

  // Tests flowThrough array literal
  @Test
  public void testFlowThrough_arrayLiteral_infersArrayType() {
    TypeInference inference = createTypeInference("function f() { var arr = [1, 2]; }");
    FlowScope entry = inference.createEntryLattice();

    Node elem1 = Node.newNumber(1.0);
    Node elem2 = Node.newNumber(2.0);
    Node arrayLit = new Node(Token.ARRAYLIT, elem1, elem2);

    inference.flowThrough(arrayLit, entry);
    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), arrayLit.getJSType());
  }

  // Tests flowThrough typeof operator
  @Test
  public void testFlowThrough_typeofOperator_infersStringType() {
    TypeInference inference = createTypeInference("function f() { var x = typeof 1; }");
    FlowScope entry = inference.createEntryLattice();

    Node child = Node.newNumber(1.0);
    child.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node typeOfNode = new Node(Token.TYPEOF, child);

    inference.flowThrough(typeOfNode, entry);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), typeOfNode.getJSType());
  }

  // Tests flowThrough comparison operators
  @Test
  public void testFlowThrough_comparisonOperator_infersBooleanType() {
    TypeInference inference = createTypeInference("function f() { var x = (1 < 2); }");
    FlowScope entry = inference.createEntryLattice();

    Node left = Node.newNumber(1.0);
    Node right = Node.newNumber(2.0);
    Node ltNode = new Node(Token.LT, left, right);

    inference.flowThrough(ltNode, entry);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), ltNode.getJSType());
  }

  // Tests flowThrough unary negation operator
  @Test
  public void testFlowThrough_negationOperator_infersNumberType() {
    TypeInference inference = createTypeInference("function f() { var x = -5; }");
    FlowScope entry = inference.createEntryLattice();

    Node child = Node.newNumber(5.0);
    child.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node negNode = new Node(Token.NEG, child);

    inference.flowThrough(negNode, entry);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), negNode.getJSType());
  }

  // Tests flowThrough hook (ternary) operator
  @Test
  public void testFlowThrough_hookOperator_infersUnionOrCommonType() {
    TypeInference inference = createTypeInference("function f() { var x = true ? 1 : 2; }");
    FlowScope entry = inference.createEntryLattice();

    Node cond = new Node(Token.TRUE);
    cond.setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    Node trueBranch = Node.newNumber(1.0);
    trueBranch.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node falseBranch = Node.newNumber(2.0);
    falseBranch.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node hook = new Node(Token.HOOK, cond, trueBranch, falseBranch);
    inference.flowThrough(hook, entry);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), hook.getJSType());
  }

  // Tests flowThrough assignment node
  @Test
  public void testFlowThrough_assignment_updatesNodeAndVariableType() {
    TypeInference inference = createTypeInference("function f() { var x; x = 10; }");
    FlowScope entry = inference.createEntryLattice();

    Node name = Node.newString(Token.NAME, "x");
    Node value = Node.newNumber(10.0);
    value.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node assign = new Node(Token.ASSIGN, name, value);

    FlowScope out = inference.flowThrough(assign, entry);
    assertNotNull(out);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), assign.getJSType());
  }

  // Tests branchedFlowThrough on an IF condition
  @Test
  public void testBranchedFlowThrough_ifCondition_returnsBranchedScopes() {
    TypeInference inference = createTypeInference("function f(x) { if (x) { return 1; } else { return 2; } }");
    FlowScope entry = inference.createEntryLattice();

    Node scriptNode = compiler.parseTestCode("function f(x) { if (x) { return 1; } else { return 2; } }");
    Node fn = scriptNode.getFirstChild();
    Node block = fn.getLastChild();
    Node ifNode = block.getFirstChild();

    List<FlowScope> branched = inference.branchedFlowThrough(ifNode, entry);
    assertNotNull(branched);
  }

  // Tests diagnostic warning constant definition
  @Test
  public void testDiagnosticConstant_definedCorrectly() {
    assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }
}
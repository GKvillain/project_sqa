package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.FunctionInjector.CanInlineResult;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.jscomp.FunctionInjector.Reference;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> safeNameIdSupplier;
  private FunctionInjector injector;

  @Before
  public void setUp() {
    compiler = new Compiler();
    safeNameIdSupplier = compiler.getUniqueNameIdSupplier();
    injector = new FunctionInjector(compiler, safeNameIdSupplier, true, true, true);
  }

  private Node parse(String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return n;
  }

  private Node findFirstNode(Node root, final Predicate<Node> predicate) {
    if (predicate.apply(root)) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstNode(child, predicate);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private Node findFunction(Node root, final String name) {
    return findFirstNode(root, new Predicate<Node>() {
      @Override
      public boolean apply(Node n) {
        return n.isFunction() && (name == null || name.equals(NodeUtil.getFunctionName(n)));
      }
    });
  }

  private Node findCall(root) {
    return findFirstNode(root, new Predicate<Node>() {
      @Override
      public boolean apply(Node n) {
        return n.isCall();
      }
    });
  }

  private Node findCall(Node root) {
    return findFirstNode(root, new Predicate<Node>() {
      @Override
      public boolean apply(Node n) {
        return n.isCall();
      }
    });
  }

  // Tests constructor with null compiler throwing NPE
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullCompiler_throwsException() {
    new FunctionInjector(null, safeNameIdSupplier, true, true, true);
  }

  // Tests constructor with null supplier throwing NPE
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullSupplier_throwsException() {
    new FunctionInjector(compiler, null, true, true, true);
  }

  // Tests doesFunctionMeetMinimumRequirements with valid simple function
  @Test
  public void testDoesFunctionMeetMinimumRequirements_validFunction_returnsTrue() {
    Node root = parse("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements with arguments reference
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments_returnsFalse() {
    Node root = parse("function foo() { return arguments[0]; }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements with eval reference
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesEval_returnsFalse() {
    Node root = parse("function foo(x) { return eval(x); }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements with recursive function reference
  @Test
  public void testDoesFunctionMeetMinimumRequirements_recursiveReference_returnsFalse() {
    Node root = parse("function foo(n) { return n <= 1 ? 1 : n * foo(n - 1); }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests isDirectCallNodeReplacementPossible on empty function
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyFunction_returnsTrue() {
    Node root = parse("function foo() {}");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on single return expression
  @Test
  public void testIsDirectCallNodeReplacementPossible_singleReturn_returnsTrue() {
    Node root = parse("function foo(x) { return x * 2; }");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on empty return statement
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyReturn_returnsFalse() {
    Node root = parse("function foo() { return; }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on multi-statement function
  @Test
  public void testIsDirectCallNodeReplacementPossible_multipleStatements_returnsFalse() {
    Node root = parse("function foo(x) { var y = x + 1; return y; }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests canInlineReferenceToFunction for direct mode with clean parameters
  @Test
  public void testCanInlineReferenceToFunction_directModeValid_returnsYes() {
    Node root = parse("function foo(a, b) { return a + b; } foo(1, 2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction for direct mode when argument has side effects and parameter used multiple times
  @Test
  public void testCanInlineReferenceToFunction_directModeSideEffectMutableArg_returnsNo() {
    Node root = parse("var i = 0; function foo(a) { return a + a; } foo(i++);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction with block mode for simple statement call
  @Test
  public void testCanInlineReferenceToFunction_blockModeSimpleCall_returnsYes() {
    Node root = parse("function foo() { var x = 1; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction when referencesThis is true but call is not .call()
  @Test
  public void testCanInlineReferenceToFunction_referencesThisWithoutCallObject_returnsNo() {
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests direct inlining replacing call node in AST
  @Test
  public void testInline_directMode_replacesCallNode() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a, b) { return a + b; } var x = foo(1, 2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue(inlined.isAdd());
  }

  // Tests block inlining replacing var initialization in AST
  @Test
  public void testInline_blockModeVarDeclaration_replacesParent() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo() { return 42; } var x = foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(inlined);
    assertTrue(inlined.isBlock());
  }

  // Tests inliningLowersCost with zero references returns true
  @Test
  public void testInliningLowersCost_noReferences_returnsTrue() {
    Node root = parse("function foo() { return 1; }");
    Node fn = findFunction(root, "foo");
    boolean lowers = injector.inliningLowersCost(
        null, fn, Collections.<Reference>emptyList(), Collections.<String>emptySet(), true, false);
    assertTrue(lowers);
  }

  // Tests inliningLowersCost with single direct removable reference returns true
  @Test
  public void testInliningLowersCost_singleDirectRemovableReference_returnsTrue() {
    Node root = parse("function foo(x) { return x; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    Reference ref = new Reference(call, null, InliningMode.DIRECT);

    boolean lowers = injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref), Collections.<String>emptySet(), true, false);
    assertTrue(lowers);
  }

  // Tests setKnownConstants accepts first set and rejects second call
  @Test(expected = IllegalStateException.class)
  public void testSetKnownConstants_calledTwice_throwsException() {
    Set<String> constants = Sets.newHashSet("CONST_A");
    injector.setKnownConstants(constants);
    injector.setKnownConstants(constants);
  }

  // Tests doesFunctionMeetMinimumRequirements with nested function declaration
  @Test
  public void testDoesFunctionMeetMinimumRequirements_innerFunction_returnsFalse() {
    Node root = parse("function foo() { function bar() {} }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests canInlineReferenceToFunction with call method invoking function referencing this
  @Test
  public void testCanInlineReferenceToFunction_callMethodWithThis_returnsYes() {
    Node root = parse("function foo(x) { return this.v + x; } foo.call(obj, 1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction when decomposition is required for block inlining in expression
  @Test
  public void testCanInlineReferenceToFunction_blockModeExpressionRequiringDecompose_returnsAfterPrecondition() {
    Node root = parse("function foo() { var z = 1; return z; } var a = 1 + foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.AFTER_PRECONDITION_CHECK, result);
  }

  // Tests canInlineReferenceToFunction when allowDecomposition is false
  @Test
  public void testCanInlineReferenceToFunction_noDecompositionAllowed_returnsNo() {
    FunctionInjector noDecompInjector = new FunctionInjector(compiler, safeNameIdSupplier, false, true, true);
    Node root = parse("function foo() { var z = 1; return z; } var a = 1 + foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = noDecompInjector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction for direct mode when parameter name collides with namesToAlias
  @Test
  public void testCanInlineReferenceToFunction_parameterInNamesToAlias_returnsNo() {
    Node root = parse("function foo(x) { return x + 1; } foo(2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.newHashSet("x"), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests inline in direct mode with .call() invocation
  @Test
  public void testInline_directModeCallTarget_replacesCallNode() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(x) { return this.v + x; } var res = foo.call(obj, 1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue(inlined.isAdd());
  }

  // Tests inline in direct mode with fewer arguments than parameters
  @Test
  public void testInline_directModeFewerArguments_inlinesVoidZero() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a, b) { return a + b; } var x = foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue(inlined.isAdd());
  }

  // Tests inline in block mode for simple expression statement
  @Test
  public void testInline_blockModeExprResult_replacesExpr() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo() { var a = 1; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(inlined);
    assertTrue(inlined.isBlock());
  }

  // Tests inline in block mode for assignment expression
  @Test
  public void testInline_blockModeAssignment_replacesAssignment() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("var x; function foo() { var a = 1; return a; } x = foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(inlined);
    assertTrue(inlined.isBlock());
  }

  // Tests inliningLowersCost when reference is not removable and inlining is not cost-effective
  @Test
  public void testInliningLowersCost_nonRemovableComplexFunction_returnsFalse() {
    Node root = parse("function foo(a, b, c) { var x = a + b; var y = x + c; return y; } foo(1, 2, 3); foo(4, 5, 6);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    Reference ref1 = new Reference(call, null, InliningMode.BLOCK);
    Reference ref2 = new Reference(call, null, InliningMode.BLOCK);

    boolean lowers = injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref1, ref2), Collections.<String>emptySet(), false, false);
    assertFalse(lowers);
  }
}
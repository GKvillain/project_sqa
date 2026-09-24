package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> safeNameIdSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    safeNameIdSupplier = new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return "temp_id_" + (id++);
      }
    };
  }

  private Node parse(String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return n;
  }

  private Node findFunction(Node root, final String name) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isFunction()) {
          if (name == null || name.equals(NodeUtil.getFunctionName(n))) {
            result[0] = n;
          }
        }
      }
    });
    return result[0];
  }

  private Node findCall(Node root, final String name) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          Node target = n.getFirstChild();
          if (name == null || (target.isName() && name.equals(target.getString()))) {
            result[0] = n;
          }
        }
      }
    });
    return result[0];
  }

  // Tests doesFunctionMeetMinimumRequirements with a standard inlinable function
  @Test
  public void testDoesFunctionMeetMinimumRequirements_validFunction_returnsTrue() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function references arguments
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments_returnsFalse() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(a) { return arguments[0]; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function references eval
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesEval_returnsFalse() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(a) { return eval(a); }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function is recursive
  @Test
  public void testDoesFunctionMeetMinimumRequirements_recursiveFunction_returnsFalse() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(a) { return foo(a - 1); }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests isDirectCallNodeReplacementPossible on empty function
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyFunction_returnsTrue() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo() {}");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on single return statement function
  @Test
  public void testIsDirectCallNodeReplacementPossible_singleReturn_returnsTrue() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x * 2; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on return with no expression
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyReturn_returnsFalse() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo() { return; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible on multi-statement function
  @Test
  public void testIsDirectCallNodeReplacementPossible_multiStatements_returnsFalse() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { var y = x; return y; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests canInlineReferenceToFunction with direct mode and immutable argument
  @Test
  public void testCanInlineReferenceToFunction_directModeImmutableArg_returnsYes() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x + x; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, false, false);

    assertEquals(FunctionInjector.CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction with direct mode and mutable argument referenced multiple times
  @Test
  public void testCanInlineReferenceToFunction_directModeMutableArgMultipleRefs_returnsNo() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x + x; } var obj = {}; foo(obj.val);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, false, false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction with side effect in call arguments
  @Test
  public void testCanInlineReferenceToFunction_directModeSideEffectArg_returnsNo() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x; } var i = 0; foo(i++);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, false, false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction when function references 'this' but called directly
  @Test
  public void testCanInlineReferenceToFunction_referencesThisWithoutCall_returnsNo() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, true, false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction in block mode for simple call statement
  @Test
  public void testCanInlineReferenceToFunction_blockModeSimpleCall_returnsYes() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { var y = x + 1; return y; } foo(5);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.BLOCK, false, false);

    assertEquals(FunctionInjector.CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction when inner function is in local scope without assumeMinimumCapture
  @Test
  public void testCanInlineReferenceToFunction_innerFunctionNonGlobalScope_returnsNo() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, false);
    Node root = parse("function outer() { function foo() { return function() {}; } foo(); }");
    Node outerFn = findFunction(root, "outer");
    Node fooFn = findFunction(outerFn, "foo");
    Node call = findCall(outerFn, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(outerFn);

    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fooFn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.BLOCK, false, true);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // Tests inliningLowersCost with 0 references
  @Test
  public void testInliningLowersCost_zeroReferences_returnsTrue() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.inliningLowersCost(
        null, fn, Collections.<FunctionInjector.Reference>emptyList(),
        Collections.<String>emptySet(), true, false));
  }

  // Tests inliningLowersCost with single direct removable reference
  @Test
  public void testInliningLowersCost_singleDirectRemovableReference_returnsTrue() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Node root = parse("function foo(x) { return x; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        call, null, FunctionInjector.InliningMode.DIRECT);

    assertTrue(injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref),
        Collections.<String>emptySet(), true, false));
  }

  // Tests setKnownConstants and setting it twice throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetKnownConstants_twice_throwsIllegalStateException() {
    FunctionInjector injector = new FunctionInjector(
        compiler, safeNameIdSupplier, true, true, true);
    Set<String> constants = Sets.newHashSet("CONST_A");
    injector.setKnownConstants(constants);
    injector.setKnownConstants(constants);
  }
}
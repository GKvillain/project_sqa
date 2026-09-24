package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.FunctionInjector.CanInlineResult;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.jscomp.FunctionInjector.Reference;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
        return "JSCompiler_temp_" + id++;
      }
    };
  }

  private FunctionInjector createInjector(boolean allowDecomposition,
                                          boolean assumeStrictThis,
                                          boolean assumeMinimumCapture) {
    return new FunctionInjector(
        compiler, safeNameIdSupplier, allowDecomposition, assumeStrictThis, assumeMinimumCapture);
  }

  private Node parseCode(String js) {
    Node root = compiler.parseSyntheticCode("test.js", js);
    assertEquals(0, compiler.getErrorCount());
    return root;
  }

  private Node findFunction(Node n, String name) {
    if (n.isFunction()) {
      Node nameNode = n.getFirstChild();
      if (nameNode != null && name.equals(nameNode.getString())) {
        return n;
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node result = findFunction(c, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private Node findCall(Node n, String targetName) {
    if (n.isCall()) {
      Node first = n.getFirstChild();
      if (first != null && first.isName() && targetName.equals(first.getString())) {
        return n;
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node result = findCall(c, targetName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  // Tests that normal function meets minimum requirements for inlining
  @Test
  public void testDoesFunctionMeetMinimumRequirements_normalFunction_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests that function referencing "arguments" fails minimum requirements
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo() { return arguments[0]; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests that function referencing "eval" fails minimum requirements
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesEval_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(str) { return eval(str); }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests that recursive function referencing its own name fails minimum requirements
  @Test
  public void testDoesFunctionMeetMinimumRequirements_recursiveFunction_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(n) { if (n <= 1) return 1; return n * foo(n - 1); }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests that empty function body is considered replaceable directly
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyBody_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo() {}");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests that single return statement with value is directly replaceable
  @Test
  public void testIsDirectCallNodeReplacementPossible_singleReturn_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { return x * 2; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests that multi-statement function cannot be directly replaced as expression
  @Test
  public void testIsDirectCallNodeReplacementPossible_multipleStatements_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { var y = x; return y * 2; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests direct inlining check for a simple function call
  @Test
  public void testCanInlineReferenceToFunction_directCall_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { return x + 1; } var y = foo(5);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests direct inlining with call argument having side effects and param referenced multiple times
  @Test
  public void testCanInlineReferenceToFunction_argumentWithSideEffects_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { return x + x; } var i = 0; var y = foo(i++);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests unsupported function.apply call rejection
  @Test
  public void testCanInlineReferenceToFunction_functionApplyCall_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { return x; } foo.apply(null, [1]);");
    Node fn = findFunction(root, "foo");
    Node call = root.getFirstChild().getNext().getFirstChild();

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests inlining cost calculation when reference list is empty
  @Test
  public void testInliningLowersCost_emptyReferences_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");

    List<Reference> refs = Collections.emptyList();
    assertTrue(injector.inliningLowersCost(null, fn, refs, Collections.<String>emptySet(), true, false));
  }

  // Tests inlining cost calculation for single removable direct inline reference
  @Test
  public void testInliningLowersCost_singleDirectRemovableReference_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parseCode("function foo(x) { return x; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    List<Reference> refs = new ArrayList<Reference>();
    refs.add(new Reference(call, null, InliningMode.DIRECT));

    assertTrue(injector.inliningLowersCost(null, fn, refs, Collections.<String>emptySet(), true, false));
  }

  // Tests setting known constants once
  @Test
  public void testSetKnownConstants_validSet_success() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> constants = ImmutableSet.of("CONST_A", "CONST_B");
    injector.setKnownConstants(constants);
  }

  // Tests exception when setKnownConstants is called more than once
  @Test(expected = IllegalStateException.class)
  public void testSetKnownConstants_calledTwice_throwsException() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> constants = ImmutableSet.of("CONST_A");
    injector.setKnownConstants(constants);
    injector.setKnownConstants(constants);
  }

  // Tests constructor null checks
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullCompiler_throwsException() {
    new FunctionInjector(null, safeNameIdSupplier, true, true, true);
  }

  // Tests constructor null supplier check
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullSupplier_throwsException() {
    new FunctionInjector(compiler, null, true, true, true);
  }
}
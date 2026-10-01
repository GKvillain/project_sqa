package com.google.javascript.jscomp;

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

import static org.junit.Assert.*;

public class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> safeNameIdSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    safeNameIdSupplier = new Supplier<String>() {
      private int nextId = 0;
      @Override
      public String get() {
        return "JSCompiler_temp_const" + (nextId++);
      }
    };
  }

  private FunctionInjector createInjector(
      boolean allowDecomposition, boolean assumeStrictThis, boolean assumeMinimumCapture) {
    return new FunctionInjector(
        compiler, safeNameIdSupplier, allowDecomposition, assumeStrictThis, assumeMinimumCapture);
  }

  private Node parse(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return root;
  }

  private Node findFunction(Node root, final String name) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isFunction()) {
          if (name == null) {
            result[0] = n;
          } else {
            String fnName = NodeUtil.getNearestFunctionName(n);
            if (name.equals(fnName)) {
              result[0] = n;
            }
          }
        }
      }
    });
    return result[0];
  }

  private Node findCall(Node root, final String targetName) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          Node callee = n.getFirstChild();
          if (callee.isName() && callee.getString().equals(targetName)) {
            result[0] = n;
          }
        }
      }
    });
    return result[0];
  }

  // Tests doesFunctionMeetMinimumRequirements with a standard valid function
  @Test
  public void testDoesFunctionMeetMinimumRequirements_validFunction_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function references 'arguments'
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { return arguments[0]; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function references 'eval'
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesEval_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { return eval(a); }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests doesFunctionMeetMinimumRequirements when function is recursive to its name
  @Test
  public void testDoesFunctionMeetMinimumRequirements_recursiveFunction_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(n) { return n > 0 ? foo(n - 1) : 0; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests isDirectCallNodeReplacementPossible for empty function
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyFunction_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() {}");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible for single return expression
  @Test
  public void testIsDirectCallNodeReplacementPossible_singleReturn_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x + 1; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible for multi-statement function
  @Test
  public void testIsDirectCallNodeReplacementPossible_multiStatement_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { var y = 1; return x + y; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests isDirectCallNodeReplacementPossible for return without expression
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyReturn_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return; }");
    Node fn = findFunction(root, "foo");

    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests canInlineReferenceToFunction for direct inlining with pure arguments
  @Test
  public void testCanInlineReferenceToFunction_directInliningPureArgs_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a, b) { return a + b; } var x = foo(1, 2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction for direct inlining when call argument has side effects
  @Test
  public void testCanInlineReferenceToFunction_directInliningSideEffectArg_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { return a + a; } var i = 0; var x = foo(i++);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction for block inlining of simple call
  @Test
  public void testCanInlineReferenceToFunction_blockInliningSimpleCall_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { var b = a + 1; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction when referencesThis is true but not a function object call
  @Test
  public void testCanInlineReferenceToFunction_referencesThisNotMethodCall_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests inliningLowersCost when there are no references
  @Test
  public void testInliningLowersCost_noReferences_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return 1; }");
    Node fn = findFunction(root, "foo");

    assertTrue(injector.inliningLowersCost(
        null, fn, Collections.<Reference>emptyList(), Collections.<String>emptySet(), true, false));
  }

  // Tests inliningLowersCost for a single direct removable reference
  @Test
  public void testInliningLowersCost_singleDirectRemovableReference_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x + 1; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    Reference ref = new Reference(call, null, InliningMode.DIRECT);
    assertTrue(injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref), Collections.<String>emptySet(), true, false));
  }

  // Tests setKnownConstants and exception when setting twice
  @Test(expected = IllegalStateException.class)
  public void testSetKnownConstants_settingTwice_throwsException() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> consts1 = Sets.newHashSet("CONST_A");
    Set<String> consts2 = Sets.newHashSet("CONST_B");

    injector.setKnownConstants(consts1);
    injector.setKnownConstants(consts2);
  }

  // Tests inline DIRECT mode for a simple return function
  @Test
  public void testInline_directMode_replacesCallNode() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a, b) { return a + b; } var result = foo(1, 2);");
    compiler.newLifeCycle();
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue(inlined.isAdd());
  }

  // Tests inline DIRECT mode for an empty function body resulting in undefined
  @Test
  public void testInline_directModeEmptyFunction_replacesWithUndefined() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() {} var result = foo();");
    compiler.newLifeCycle();
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue(inlined.isVoid());
  }

  // Tests inline BLOCK mode for simple assignment
  @Test
  public void testInline_blockModeSimpleAssignment_replacesWithBlock() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { return a + 1; } var x; x = foo(2);");
    compiler.newLifeCycle();
    Node fn = findFunction(root, "foo");
    Node call = findCall(root, "foo");

    Node inlinedBlock = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(inlinedBlock);
    assertTrue(inlinedBlock.isBlock());
  }
}
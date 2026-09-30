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
    safeNameIdSupplier = compiler.getUniqueNameIdSupplier();
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
          Node nameNode = n.getFirstChild();
          if (nameNode != null && name.equals(nameNode.getString())) {
            result[0] = n;
          } else if (parent != null && parent.isName() && name.equals(parent.getString())) {
            result[0] = n;
          } else if (parent != null && parent.isAssign() && parent.getFirstChild().isName()
              && name.equals(parent.getFirstChild().getString())) {
            result[0] = n;
          }
        }
      }
    });
    return result[0];
  }

  private Node findCall(Node root) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall() && result[0] == null) {
          result[0] = n;
        }
      }
    });
    return result[0];
  }

  private Node findNew(Node root) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isNew() && result[0] == null) {
          result[0] = n;
        }
      }
    });
    return result[0];
  }

  // Tests constructor validation when compiler is null
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullCompiler_throwsException() {
    new FunctionInjector(null, safeNameIdSupplier, true, true, true);
  }

  // Tests constructor validation when safeNameIdSupplier is null
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullSupplier_throwsException() {
    new FunctionInjector(compiler, null, true, true, true);
  }

  // Tests minimum requirements for a simple valid function
  @Test
  public void testDoesFunctionMeetMinimumRequirements_validFunction_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a, b) { return a + b; }");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests minimum requirements when function references arguments
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return arguments[0]; }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests minimum requirements when function references eval
  @Test
  public void testDoesFunctionMeetMinimumRequirements_referencesEval_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { eval('1'); }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests minimum requirements when function references itself recursively
  @Test
  public void testDoesFunctionMeetMinimumRequirements_recursiveFunction_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return foo(); }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn));
  }

  // Tests direct replacement possibility for empty function
  @Test
  public void testIsDirectCallNodeReplacementPossible_emptyFunction_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() {}");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests direct replacement possibility for single return statement
  @Test
  public void testIsDirectCallNodeReplacementPossible_singleReturn_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x + 1; }");
    Node fn = findFunction(root, "foo");
    assertTrue(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests direct replacement possibility for multi-statement function
  @Test
  public void testIsDirectCallNodeReplacementPossible_multiStatement_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { var y = x; return y; }");
    Node fn = findFunction(root, "foo");
    assertFalse(injector.isDirectCallNodeReplacementPossible(fn));
  }

  // Tests canInlineReferenceToFunction with direct mode and clean arguments
  @Test
  public void testCanInlineReferenceToFunction_directModeCleanArgs_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  // Tests canInlineReferenceToFunction when argument has side effects and parameter used multiple times
  @Test
  public void testCanInlineReferenceToFunction_directModeSideEffectsArgMultipleUses_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x + x; } foo(i++);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction with unsupported call type like .apply
  @Test
  public void testCanInlineReferenceToFunction_applyCallType_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return 1; } foo.apply(null);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests canInlineReferenceToFunction with this reference when not supported
  @Test
  public void testCanInlineReferenceToFunction_referencesThisWithoutCall_returnsNo() {
    FunctionInjector injector = createInjector(true, false, false);
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, true, false);
    assertEquals(CanInlineResult.NO, result);
  }

  // Tests inliningLowersCost when there are no references
  @Test
  public void testInliningLowersCost_noReferences_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return 1; }");
    Node fn = findFunction(root, "foo");

    boolean lowersCost = injector.inliningLowersCost(
        null, fn, Collections.<Reference>emptyList(), Sets.<String>newHashSet(), true, false);
    assertTrue(lowersCost);
  }

  // Tests inliningLowersCost with a single direct inlinable reference
  @Test
  public void testInliningLowersCost_singleDirectRef_returnsTrue() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return 1; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Reference ref = new Reference(call, null, InliningMode.DIRECT);
    boolean lowersCost = injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref), Sets.<String>newHashSet(), true, false);
    assertTrue(lowersCost);
  }

  // Tests setKnownConstants throws exception if called more than once
  @Test(expected = IllegalStateException.class)
  public void testSetKnownConstants_calledTwice_throwsException() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> constants = Sets.newHashSet("CONST_A");
    injector.setKnownConstants(constants);
    injector.setKnownConstants(constants);
  }

  // Tests inline with DIRECT mode on normalized code
  @Test
  public void testInline_directMode_replacesCallNode() {
    FunctionInjector injector = createInjector(true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a) { return a + 1; } var x = foo(2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node result = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(result);
    assertFalse(result.isCall());
  }

  // Tests inline with BLOCK mode on normalized code
  @Test
  public void testInline_blockMode_inlinesAsBlock() {
    FunctionInjector injector = createInjector(true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a) { var y = a; return y; } var x = foo(2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node result = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(result);
    assertTrue(result.isBlock());
  }

  // Tests direct call with mutable argument passed to unmodified parameter
  @Test
  public void testCanInlineReferenceToFunction_directCallMutableArgument_evaluatesCorrectly() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a, b) { return a; } foo(modify(x), y);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  @Test
  public void testSetKnownConstants_calledOnce_succeeds() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> constants = Sets.newHashSet("CONST_A", "CONST_B");
    injector.setKnownConstants(constants);
  }

  @Test
  public void testCanInlineReferenceToFunction_blockModeStatement_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { var y = a + 1; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  @Test
  public void testCanInlineReferenceToFunction_blockModeExpressionNeedDecomposition_returnsAfterPrep() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(a) { var y = a + 1; return y; } var x = foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.AFTER_PREPARATION, result);
  }

  @Test
  public void testCanInlineReferenceToFunction_blockModeNoDecomposition_returnsNo() {
    FunctionInjector injector = createInjector(false, true, true);
    Node root = parse("function foo(a) { var y = a + 1; return y; } var x = foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.BLOCK, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  @Test
  public void testCanInlineReferenceToFunction_newCall_returnsNo() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { this.x = 1; } new foo();");
    Node fn = findFunction(root, "foo");
    Node newCall = findNew(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, newCall, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.NO, result);
  }

  @Test
  public void testCanInlineReferenceToFunction_dotCallMethod_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { return x + 1; } foo.call(null, 1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, false, false);
    assertEquals(CanInlineResult.YES, result);
  }

  @Test
  public void testCanInlineReferenceToFunction_strictThisWithThisUsage_returnsYes() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);
    NodeTraversal t = new NodeTraversal(compiler, null);

    CanInlineResult result = injector.canInlineReferenceToFunction(
        t, call, fn, Sets.<String>newHashSet(), InliningMode.DIRECT, true, false);
    assertEquals(CanInlineResult.YES, result);
  }

  @Test
  public void testInliningLowersCost_multipleBlockReferences_returnsFalse() {
    FunctionInjector injector = createInjector(true, true, true);
    Node root = parse("function foo(x) { var a = x; var b = a + 1; var c = b + 2; return c; } foo(1); foo(2); foo(3);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Reference ref1 = new Reference(call, null, InliningMode.BLOCK);
    Reference ref2 = new Reference(call, null, InliningMode.BLOCK);
    Reference ref3 = new Reference(call, null, InliningMode.BLOCK);

    boolean lowersCost = injector.inliningLowersCost(
        null, fn, ImmutableList.of(ref1, ref2, ref3), Sets.<String>newHashSet(), false, false);
    assertFalse(lowersCost);
  }

  @Test
  public void testInline_directModeEmptyFunction_replacesWithVoid() {
    FunctionInjector injector = createInjector(true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo() {} var x = foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node result = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    assertNotNull(result);
    assertTrue(result.isVoid());
  }

  @Test
  public void testInline_blockModeReturnWithoutValue_inlinesCorrectly() {
    FunctionInjector injector = createInjector(true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a) { if (a) { return; } var b = 1; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node result = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    assertNotNull(result);
    assertTrue(result.isBlock());
  }

  @Test
  public void testMaybePrepareCall_decomposesCallExpression() {
    FunctionInjector injector = createInjector(true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = parse("function foo(a) { var y = a + 1; return y; } var x = foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    injector.maybePrepareCall(call);
    assertNotNull(call.getParent());
  }
}
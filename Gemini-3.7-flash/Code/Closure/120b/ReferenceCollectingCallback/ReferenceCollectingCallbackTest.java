package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceMap;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReferenceCollectingCallbackTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ReferenceCollectingCallback parseAndRun(String js, Behavior behavior, Predicate<Var> filter) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior, filter);
    callback.process(externs, root);
    return callback;
  }

  private ReferenceCollectingCallback parseAndRun(String js) {
    return parseAndRun(js, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, Predicates.<Var>alwaysTrue());
  }

  private Var getVar(ReferenceCollectingCallback callback, String name) {
    for (Var v : callback.getAllSymbols()) {
      if (v.getName().equals(name)) {
        return v;
      }
    }
    return null;
  }

  // Tests basic variable declaration with assignment
  @Test
  public void testProcess_basicVarDeclaration_collectsReferences() {
    String js = "var x = 1; x++;";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertNotNull(refs);
    assertEquals(2, refs.references.size());

    Reference decl = refs.references.get(0);
    assertTrue(decl.isDeclaration());
    assertTrue(decl.isVarDeclaration());
    assertTrue(decl.isInitializingDeclaration());
    assertTrue(refs.firstReferenceIsAssigningDeclaration());
    assertFalse(refs.isNeverAssigned());
  }

  // Tests uninitialized var declaration followed by assignment
  @Test
  public void testProcess_uninitializedVarThenAssign_wellDefined() {
    String js = "var x; x = 2; var y = x;";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertNotNull(refs);
    assertEquals(3, refs.references.size());

    assertFalse(refs.references.get(0).isInitializingDeclaration());
    assertTrue(refs.references.get(1).isSimpleAssignmentToName());
    assertTrue(refs.isWellDefined());
    assertNotNull(refs.getInitializingReference());
    assertEquals(refs.references.get(1), refs.getInitializingReference());
  }

  // Tests uninitialized variable without assignment
  @Test
  public void testIsWellDefined_uninitializedVar_returnsFalse() {
    String js = "var x; alert(x);";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertFalse(refs.isWellDefined());
    assertNull(refs.getInitializingReference());
  }

  // Tests variable assigned multiple times
  @Test
  public void testIsAssignedOnceInLifetime_multipleAssignments_returnsFalse() {
    String js = "var x = 1; x = 2;";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertFalse(refs.isAssignedOnceInLifetime());
    assertFalse(refs.isNeverAssigned());
  }

  // Tests variable assigned inside a loop
  @Test
  public void testIsAssignedOnceInLifetime_insideLoop_returnsFalse() {
    String js = "var x; while (true) { x = 1; }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests variable assigned in function inside a loop (Defects4J 120 regression check)
  @Test
  public void testIsAssignedOnceInLifetime_inFunctionInsideLoop_returnsFalse() {
    String js = "var x; while (true) { function f() { x = 1; } f(); }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests variable assigned once outside loop
  @Test
  public void testIsAssignedOnceInLifetime_singleAssignment_returnsTrue() {
    String js = "var x = 1; alert(x);";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertTrue(refs.isAssignedOnceInLifetime());
    assertTrue(refs.isWellDefined());
  }

  // Tests variable escaping into inner function scope
  @Test
  public void testIsEscaped_innerScope_returnsTrue() {
    String js = "var x = 1; function f() { return x; }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertTrue(refs.isEscaped());
  }

  // Tests variable not escaping scope
  @Test
  public void testIsEscaped_sameScope_returnsFalse() {
    String js = "function f() { var x = 1; return x + 1; }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var xVar = getVar(callback, "x");

    assertNotNull(xVar);
    ReferenceCollection refs = callback.getReferences(xVar);
    assertFalse(refs.isEscaped());
  }

  // Tests arguments variable reference
  @Test
  public void testVisit_argumentsVar_collected() {
    String js = "function f() { return arguments[0]; }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var argsVar = getVar(callback, "arguments");

    assertNotNull(argsVar);
    ReferenceCollection refs = callback.getReferences(argsVar);
    assertNotNull(refs);
    assertEquals(1, refs.references.size());
  }

  // Tests variable filter predicate
  @Test
  public void testVarFilter_excludesFilteredVariables() {
    String js = "var a = 1; var b = 2;";
    Predicate<Var> filterOnlyA = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return "a".equals(input.getName());
      }
    };
    ReferenceCollectingCallback callback = parseAndRun(js, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filterOnlyA);

    assertNotNull(getVar(callback, "a"));
    assertNull(getVar(callback, "b"));
  }

  // Tests behavior callback invocation
  @Test
  public void testBehavior_afterExitScope_invoked() {
    final int[] exitScopeCount = new int[]{0};
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceMap referenceMap) {
        exitScopeCount[0]++;
      }
    };
    String js = "function f() { var x = 1; }";
    parseAndRun(js, behavior, Predicates.<Var>alwaysTrue());

    assertTrue(exitScopeCount[0] >= 2);
  }

  // Tests hoisted function declaration
  @Test
  public void testHoistedFunction_isHoistedFunctionTrue() {
    String js = "function foo() {} foo();";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var fooVar = getVar(callback, "foo");

    assertNotNull(fooVar);
    ReferenceCollection refs = callback.getReferences(fooVar);
    assertTrue(refs.references.get(0).isHoistedFunction());
  }

  // Tests BasicBlock provablyExecutesBefore
  @Test
  public void testBasicBlock_provablyExecutesBefore() {
    Node n1 = new Node(Token.BLOCK);
    Node n2 = new Node(Token.BLOCK);
    BasicBlock rootBlock = new BasicBlock(null, n1);
    BasicBlock childBlock = new BasicBlock(rootBlock, n2);

    assertTrue(rootBlock.provablyExecutesBefore(childBlock));
    assertTrue(rootBlock.provablyExecutesBefore(rootBlock));
    assertFalse(childBlock.provablyExecutesBefore(rootBlock));
  }

  // Tests ReferenceCollection iterator
  @Test
  public void testReferenceCollection_iterator() {
    ReferenceCollection collection = new ReferenceCollection();
    Iterator<Reference> it = collection.iterator();
    assertFalse(it.hasNext());
    assertFalse(collection.isWellDefined());
    assertNull(collection.getInitializingReference());
    assertNull(collection.getInitializingReferenceForConstants());
  }

  // Tests for-in loop variable and lvalue
  @Test
  public void testForIn_lvalueDetection() {
    String js = "var obj = {a: 1}; for (var k in obj) { alert(k); }";
    ReferenceCollectingCallback callback = parseAndRun(js);
    Var kVar = getVar(callback, "k");

    assertNotNull(kVar);
    ReferenceCollection refs = callback.getReferences(kVar);
    assertNotNull(refs);
    assertTrue(refs.references.get(0).isLvalue());
  }

  // Tests hotSwapScript entry point
  @Test
  public void testHotSwapScript_processesNode() {
    String js = "var z = 10;";
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(root, null);

    Var zVar = getVar(callback, "z");
    assertNotNull(zVar);
    assertEquals(1, callback.getReferences(zVar).references.size());
  }
}
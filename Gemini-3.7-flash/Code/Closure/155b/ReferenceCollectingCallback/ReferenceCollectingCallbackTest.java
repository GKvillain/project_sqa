package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ReferenceCollectingCallbackTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ReferenceCollectingCallback parseAndTraverse(String js) {
    return parseAndTraverse(js, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, Predicates.<Var>alwaysTrue());
  }

  private ReferenceCollectingCallback parseAndTraverse(
      String js, Behavior behavior, Predicate<Var> filter) {
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, behavior, filter);
    callback.process(new Node(Token.BLOCK), root);
    return callback;
  }

  private ReferenceCollection getCollectionFor(ReferenceCollectingCallback callback, String varName) {
    for (Var var : callback.getReferencedVariables()) {
      if (var.getName().equals(varName)) {
        return callback.getReferenceCollection(var);
      }
    }
    return null;
  }

  // Tests variable declaration with immediate initialization
  @Test
  public void testProcess_initializedVar_isWellDefinedAndAssignedOnce() {
    String js = "var x = 10; var y = x + 1;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertEquals(2, xRefs.references.size());
    assertTrue(xRefs.isWellDefined());
    assertTrue(xRefs.isAssignedOnceInLifetime());
    assertFalse(xRefs.isNeverAssigned());
    assertTrue(xRefs.firstReferenceIsAssigningDeclaration());
    assertNotNull(xRefs.getInitializingReference());
    assertFalse(xRefs.isEscaped());
  }

  // Tests uninitialized variable declaration followed by assignment
  @Test
  public void testProcess_uninitializedVarThenAssigned_isWellDefined() {
    String js = "var x; x = 10; var y = x;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertEquals(3, xRefs.references.size());
    assertTrue(xRefs.isWellDefined());
    assertTrue(xRefs.isAssignedOnceInLifetime());
    assertFalse(xRefs.isNeverAssigned());
    assertFalse(xRefs.firstReferenceIsAssigningDeclaration());
    assertNotNull(xRefs.getInitializingReference());
    assertEquals(xRefs.references.get(1), xRefs.getInitializingReference());
  }

  // Tests variable declared but never assigned a value
  @Test
  public void testProcess_unassignedVar_isNeverAssigned() {
    String js = "var x; var y = x;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertEquals(2, xRefs.references.size());
    assertFalse(xRefs.isWellDefined());
    assertFalse(xRefs.isAssignedOnceInLifetime());
    assertTrue(xRefs.isNeverAssigned());
    assertNull(xRefs.getInitializingReference());
  }

  // Tests variable assigned multiple times in the same scope
  @Test
  public void testProcess_multipleAssignments_notAssignedOnce() {
    String js = "var x = 1; x = 2; x = 3;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertEquals(3, xRefs.references.size());
    assertFalse(xRefs.isAssignedOnceInLifetime());
    assertFalse(xRefs.isNeverAssigned());
  }

  // Tests assignment within a loop block
  @Test
  public void testProcess_assignmentInsideLoop_notAssignedOnceInLifetime() {
    String js = "var x; while (true) { x = 1; }";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertFalse(xRefs.isAssignedOnceInLifetime());
  }

  // Tests variable referenced inside an inner function scope (escaped)
  @Test
  public void testProcess_variableUsedInInnerScope_isEscaped() {
    String js = "var x = 1; function f() { return x; }";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertTrue(xRefs.isEscaped());
  }

  // Tests variable confined strictly to a single scope (not escaped)
  @Test
  public void testProcess_variableInSingleScope_isNotEscaped() {
    String js = "function f() { var x = 1; return x; }";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);
    assertFalse(xRefs.isEscaped());
  }

  // Tests behavior afterExitScope callback execution
  @Test
  public void testProcess_customBehavior_invokesAfterExitScope() {
    final int[] exitScopeCount = new int[1];
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        exitScopeCount[0]++;
      }
    };

    String js = "function f() { var a = 1; }";
    parseAndTraverse(js, behavior, Predicates.<Var>alwaysTrue());

    assertTrue(exitScopeCount[0] > 0);
  }

  // Tests varFilter predicate to collect only matching variables
  @Test
  public void testProcess_varFilter_onlyCollectsMatchingVars() {
    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return input != null && "target".equals(input.getName());
      }
    };

    String js = "var ignored = 1; var target = 2;";
    ReferenceCollectingCallback callback =
        parseAndTraverse(js, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);

    Set<Var> vars = callback.getReferencedVariables();
    for (Var var : vars) {
      assertEquals("target", var.getName());
    }
    assertNull(getCollectionFor(callback, "ignored"));
    assertNotNull(getCollectionFor(callback, "target"));
  }

  // Tests function declaration reference and hoisting
  @Test
  public void testProcess_functionDeclaration_isDeclarationAndInitializing() {
    String js = "function foo() {} foo();";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection fooRefs = getCollectionFor(callback, "foo");
    assertNotNull(fooRefs);
    assertTrue(fooRefs.references.get(0).isDeclaration());
    assertTrue(fooRefs.references.get(0).isInitializingDeclaration());
    assertTrue(fooRefs.references.get(0).isHoistedFunction());
    assertNotNull(fooRefs.references.get(0).getAssignedValue());
  }

  // Tests catch block variable declaration reference
  @Test
  public void testProcess_catchClause_createsInitializingDeclaration() {
    String js = "try { var x = 1; } catch (e) { var y = e; }";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection eRefs = getCollectionFor(callback, "e");
    assertNotNull(eRefs);
    assertTrue(eRefs.references.get(0).isDeclaration());
    assertTrue(eRefs.references.get(0).isInitializingDeclaration());
    assertFalse(eRefs.references.get(0).isVarDeclaration());
  }

  // Tests basic block provablyExecutesBefore ordering
  @Test
  public void testBasicBlock_provablyExecutesBefore_parentExecutesBeforeChild() {
    Node rootNode = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF);
    rootNode.addChildToBack(ifNode);

    BasicBlock rootBlock = new BasicBlock(null, rootNode);
    BasicBlock ifBlock = new BasicBlock(rootBlock, ifNode);

    assertTrue(rootBlock.provablyExecutesBefore(ifBlock));
    assertFalse(ifBlock.provablyExecutesBefore(rootBlock));
    assertEquals(rootBlock, ifBlock.getParent());
  }

  // Tests constant initializing reference lookup
  @Test
  public void testReferenceCollection_getInitializingReferenceForConstants() {
    String js = "var y = CONST; var CONST = 10;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection constRefs = getCollectionFor(callback, "CONST");
    assertNotNull(constRefs);
    Reference initRef = constRefs.getInitializingReferenceForConstants();
    assertNotNull(initRef);
    assertTrue(initRef.isInitializingDeclaration());
  }

  // Tests empty ReferenceCollection edge cases
  @Test
  public void testReferenceCollection_emptyCollection_returnsFalseAndNull() {
    ReferenceCollection emptyCol = new ReferenceCollection();
    assertFalse(emptyCol.isWellDefined());
    assertFalse(emptyCol.isEscaped());
    assertTrue(emptyCol.isNeverAssigned());
    assertFalse(emptyCol.isAssignedOnceInLifetime());
    assertFalse(emptyCol.firstReferenceIsAssigningDeclaration());
    assertNull(emptyCol.getInitializingReference());
    assertNull(emptyCol.getInitializingReferenceForConstants());
  }

  // Tests reference properties for compound assignment and unary operators
  @Test
  public void testReference_lValueOperations_identifiedAsLvalues() {
    String js = "var a = 0; a++; a += 2;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection aRefs = getCollectionFor(callback, "a");
    assertNotNull(aRefs);
    assertEquals(3, aRefs.references.size());
    assertTrue(aRefs.references.get(0).isLvalue());
    assertTrue(aRefs.references.get(1).isLvalue());
    assertTrue(aRefs.references.get(2).isLvalue());
    assertNotNull(aRefs.references.get(0).getParent());
    assertNotNull(aRefs.references.get(0).getNameNode());
    assertNotNull(aRefs.references.get(0).getScope());
    assertNotNull(aRefs.references.get(0).getBasicBlock());
  }

  // Tests for-in loop header reference
  @Test
  public void testProcess_forInLoop_isSetForLoop() {
    String js = "var obj = {}; for (var key in obj) { alert(key); }";
    ReferenceCollectingCallback callback = parseAndTraverse(js);

    ReferenceCollection keyRefs = getCollectionFor(callback, "key");
    assertNotNull(keyRefs);
    assertTrue(keyRefs.references.get(0).isSetForLoop());
    assertTrue(keyRefs.references.get(0).isVarDeclaration());
    assertFalse(keyRefs.isAssignedOnceInLifetime());
  }

  // Tests do-while and for loop basic block properties
  @Test
  public void testBasicBlock_loopAndFunctionTypes() {
    Node loopNode = new Node(Token.DO);
    Node fnNode = new Node(Token.FUNCTION);
    BasicBlock rootBlock = new BasicBlock(null, new Node(Token.BLOCK));
    BasicBlock loopBlock = new BasicBlock(rootBlock, loopNode);
    BasicBlock fnBlock = new BasicBlock(rootBlock, fnNode);

    assertTrue(loopBlock.isLoop());
    assertFalse(loopBlock.isFunction());
    assertTrue(fnBlock.isFunction());
    assertFalse(fnBlock.isLoop());
  }

  // Tests isOnlyAssignmentSameScopeAsDeclaration
  @Test
  public void testReferenceCollection_isOnlyAssignmentSameScopeAsDeclaration() {
    String jsSameScope = "var x; function f() {} x = 1; var y = x;";
    ReferenceCollectingCallback callbackSame = parseAndTraverse(jsSameScope);
    ReferenceCollection xSame = getCollectionFor(callbackSame, "x");
    assertNotNull(xSame);
    assertTrue(xSame.isOnlyAssignmentSameScopeAsDeclaration());

    String jsDiffScope = "var x; function f() { x = 1; }";
    ReferenceCollectingCallback callbackDiff = parseAndTraverse(jsDiffScope);
    ReferenceCollection xDiff = getCollectionFor(callbackDiff, "x");
    assertNotNull(xDiff);
    assertFalse(xDiff.isOnlyAssignmentSameScopeAsDeclaration());
  }

  // Tests ReferenceCollection iteration
  @Test
  public void testReferenceCollection_iterator() {
    String js = "var x = 1; x = 2;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);
    ReferenceCollection xRefs = getCollectionFor(callback, "x");
    assertNotNull(xRefs);

    Iterator<Reference> iterator = xRefs.iterator();
    int count = 0;
    while (iterator.hasNext()) {
      assertNotNull(iterator.next());
      count++;
    }
    assertEquals(2, count);
  }

  // Tests hotSwapScript entry point
  @Test
  public void testHotSwapScript() {
    Node scriptRoot = compiler.parseTestCode("var a = 1;");
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(scriptRoot, null);
    ReferenceCollection aRefs = getCollectionFor(callback, "a");
    assertNotNull(aRefs);
    assertEquals(1, aRefs.references.size());
  }

  // Tests simple assignment to name check
  @Test
  public void testReference_isSimpleAssignmentToName() {
    String js = "var a; a = 10; a += 5;";
    ReferenceCollectingCallback callback = parseAndTraverse(js);
    ReferenceCollection aRefs = getCollectionFor(callback, "a");
    assertNotNull(aRefs);

    // a (var decl)
    assertFalse(aRefs.references.get(0).isSimpleAssignmentToName());
    // a = 10
    assertTrue(aRefs.references.get(1).isSimpleAssignmentToName());
    // a += 5
    assertFalse(aRefs.references.get(2).isSimpleAssignmentToName());
  }
}
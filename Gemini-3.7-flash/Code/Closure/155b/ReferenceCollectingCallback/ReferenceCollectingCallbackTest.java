package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ReferenceCollectingCallbackTest {

  private ReferenceCollectingCallback parseAndRun(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(null, root);
    return callback;
  }

  private Var getVar(ReferenceCollectingCallback callback, String name) {
    for (Var v : callback.getReferencedVariables()) {
      if (v.getName().equals(name)) {
        return v;
      }
    }
    return null;
  }

  // Tests basic reference collection on variable declaration and usage
  @Test
  public void testProcess_simpleVar_collectsReferences() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a;");
    Var a = getVar(callback, "a");
    assertNotNull(a);
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertNotNull(col);
    assertEquals(2, col.references.size());
  }

  // Tests well-defined variable initialized at declaration
  @Test
  public void testIsWellDefined_initializedAtDecl_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a();");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isWellDefined());
    assertTrue(col.firstReferenceIsAssigningDeclaration());
    assertNotNull(col.getInitializingReference());
  }

  // Tests well-defined variable declared and immediately assigned
  @Test
  public void testIsWellDefined_uninitializedDeclThenAssigned_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("var a; a = 1; a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isWellDefined());
    assertNotNull(col.getInitializingReference());
  }

  // Tests variable used before assigned is not well-defined
  @Test
  public void testIsWellDefined_usedBeforeAssigned_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("a; var a = 1;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isWellDefined());
  }

  // Tests uninitialized variable is not well-defined
  @Test
  public void testIsWellDefined_uninitializedVar_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isWellDefined());
    assertNull(col.getInitializingReference());
  }

  // Tests variable initialized in conditional block is not well-defined
  @Test
  public void testIsWellDefined_conditionalAssignment_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a; if (true) { a = 1; } a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isWellDefined());
  }

  // Tests single assignment in lifetime returns true
  @Test
  public void testIsAssignedOnceInLifetime_singleAssignment_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isAssignedOnceInLifetime());
    assertFalse(col.isNeverAssigned());
  }

  // Tests multiple assignments in lifetime returns false
  @Test
  public void testIsAssignedOnceInLifetime_multipleAssignments_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a = 2;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isAssignedOnceInLifetime());
    assertFalse(col.isNeverAssigned());
  }

  // Tests assignment inside loop returns false for assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_assignmentInWhileLoop_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a; while (true) { a = 1; }");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isAssignedOnceInLifetime());
  }

  // Tests assignment inside for loop returns false for assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_assignmentInForLoop_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a; for (var i = 0; i < 10; i++) { a = 1; }");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isAssignedOnceInLifetime());
  }

  // Tests variable declared inside a function within a loop is assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_insideFunctionInLoop_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("while (true) { function f() { var a = 1; } }");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isAssignedOnceInLifetime());
  }

  // Tests variable never assigned value
  @Test
  public void testIsNeverAssigned_unassignedVar_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("var a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isNeverAssigned());
    assertFalse(col.isAssignedOnceInLifetime());
  }

  // Tests variable escaped into inner scope
  @Test
  public void testIsEscaped_innerScopeAccess_returnsTrue() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; function f() { a; }");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertTrue(col.isEscaped());
  }

  // Tests variable not escaped when only accessed in same scope
  @Test
  public void testIsEscaped_sameScopeOnly_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a++;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isEscaped());
  }

  // Tests getInitializingReferenceForConstants finds later initialization
  @Test
  public void testGetInitializingReferenceForConstants_lateInit_findsInit() {
    ReferenceCollectingCallback callback = parseAndRun("a; var a = 1;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertNotNull(col.getInitializingReferenceForConstants());
  }

  // Tests variable filtering constructor
  @Test
  public void testVarFilter_onlyCollectsFilteredVars() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return "a".equals(input.getName());
      }
    };
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);
    callback.process(null, root);

    Set<Var> vars = callback.getReferencedVariables();
    assertEquals(1, vars.size());
    assertNotNull(getVar(callback, "a"));
    assertNull(getVar(callback, "b"));
  }

  // Tests custom behavior invocation upon exiting scope
  @Test
  public void testBehavior_afterExitScopeInvoked() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    final boolean[] exited = new boolean[]{false};
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        exited[0] = true;
      }
    };
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(null, root);
    assertTrue(exited[0]);
  }

  // Tests Reference inspection methods (isDeclaration, isVarDeclaration, isLvalue, isHoistedFunction)
  @Test
  public void testReference_propertiesAndMethods() {
    ReferenceCollectingCallback callback = parseAndRun(
        "function foo() {} var x = 1; x++; for (var k in {}) {}");
    Var foo = getVar(callback, "foo");
    Reference refFoo = callback.getReferenceCollection(foo).references.get(0);
    assertTrue(refFoo.isDeclaration());
    assertTrue(refFoo.isHoistedFunction());
    assertNotNull(refFoo.getAssignedValue());
    assertNotNull(refFoo.getNameNode());
    assertNotNull(refFoo.getParent());
    assertNotNull(refFoo.getGrandparent());
    assertNotNull(refFoo.getScope());
    assertNotNull(refFoo.getBasicBlock());

    Var x = getVar(callback, "x");
    ReferenceCollection colX = callback.getReferenceCollection(x);
    Reference declX = colX.references.get(0);
    assertTrue(declX.isVarDeclaration());
    assertTrue(declX.isInitializingDeclaration());
    assertTrue(declX.isLvalue());

    Reference incX = colX.references.get(1);
    assertTrue(incX.isLvalue());
    assertFalse(incX.isDeclaration());

    Var k = getVar(callback, "k");
    Reference refK = callback.getReferenceCollection(k).references.get(0);
    assertTrue(refK.isLvalue());
  }

  // Tests BasicBlock provablyExecutesBefore hierarchy
  @Test
  public void testBasicBlock_provablyExecutesBefore() {
    Node root = new Node(Token.BLOCK);
    BasicBlock rootBlock = new BasicBlock(null, root);
    assertNull(rootBlock.getParent());

    Node childNode = new Node(Token.BLOCK);
    BasicBlock childBlock = new BasicBlock(rootBlock, childNode);
    assertEquals(rootBlock, childBlock.getParent());

    assertTrue(rootBlock.provablyExecutesBefore(childBlock));
    assertFalse(childBlock.provablyExecutesBefore(rootBlock));
    assertTrue(rootBlock.provablyExecutesBefore(rootBlock));
  }

  // Tests control structures block boundaries traversal
  @Test
  public void testControlStructures_traversal() {
    ReferenceCollectingCallback callback = parseAndRun(
        "var x = 1;" +
        "do { x; } while (true);" +
        "try { x; } catch (e) { x; } finally { x; }" +
        "with ({}) { x; }" +
        "if (true && x || x ? x : x) { x; }" +
        "switch (x) { case 1: x; }");
    Var x = getVar(callback, "x");
    ReferenceCollection col = callback.getReferenceCollection(x);
    assertNotNull(col);
    assertTrue(col.references.size() > 5);
  }

  // Tests hotSwapScript invocation
  @Test
  public void testHotSwapScript_processesScriptRoot() {
    Compiler compiler = new Compiler();
    Node scriptRoot = compiler.parseTestCode("var a = 1; a;");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(scriptRoot, null);
    Var a = getVar(callback, "a");
    assertNotNull(a);
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertNotNull(col);
    assertEquals(2, col.references.size());
  }

  // Tests ReferenceCollection iterable implementation
  @Test
  public void testReferenceCollection_iterator() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; a; a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    int count = 0;
    for (Reference ref : col) {
      assertNotNull(ref);
      assertNotNull(ref.getNode());
      count++;
    }
    assertEquals(3, count);
  }

  // Tests Reference isSimpleAssignmentToName
  @Test
  public void testReference_isSimpleAssignmentToName() {
    ReferenceCollectingCallback callback = parseAndRun("var a; a = 1; a += 2; a++;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.references.get(0).isSimpleAssignmentToName());
    assertTrue(col.references.get(1).isSimpleAssignmentToName());
    assertFalse(col.references.get(2).isSimpleAssignmentToName());
    assertFalse(col.references.get(3).isSimpleAssignmentToName());
  }

  // Tests BasicBlock isFunction and isLoop
  @Test
  public void testBasicBlock_isFunctionAndIsLoop() {
    Node fnNode = new Node(Token.FUNCTION);
    BasicBlock fnBlock = new BasicBlock(null, fnNode);
    assertTrue(fnBlock.isFunction());
    assertFalse(fnBlock.isLoop());

    Node whileNode = new Node(Token.WHILE);
    BasicBlock whileBlock = new BasicBlock(null, whileNode);
    assertFalse(whileBlock.isFunction());
    assertTrue(whileBlock.isLoop());

    Node doNode = new Node(Token.DO);
    BasicBlock doBlock = new BasicBlock(null, doNode);
    assertFalse(doBlock.isFunction());
    assertTrue(doBlock.isLoop());

    Node forNode = new Node(Token.FOR);
    BasicBlock forBlock = new BasicBlock(null, forNode);
    assertFalse(forBlock.isFunction());
    assertTrue(forBlock.isLoop());
  }

  // Tests BasicBlock provablyExecutesBefore with disjoint branches
  @Test
  public void testBasicBlock_disjointBranches() {
    Node rootNode = new Node(Token.IF);
    BasicBlock rootBlock = new BasicBlock(null, rootNode);
    Node branch1Node = new Node(Token.BLOCK);
    BasicBlock branch1Block = new BasicBlock(rootBlock, branch1Node);
    Node branch2Node = new Node(Token.BLOCK);
    BasicBlock branch2Block = new BasicBlock(rootBlock, branch2Node);

    assertTrue(rootBlock.provablyExecutesBefore(branch1Block));
    assertTrue(rootBlock.provablyExecutesBefore(branch2Block));
    assertFalse(branch1Block.provablyExecutesBefore(branch2Block));
    assertFalse(branch2Block.provablyExecutesBefore(branch1Block));
  }

  // Tests isAssignedOnceInLifetime when assigned in do-while loop
  @Test
  public void testIsAssignedOnceInLifetime_doWhileLoop_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a; do { a = 1; } while (false);");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isAssignedOnceInLifetime());
  }

  // Tests isAssignedOnceInLifetime when assigned in for-in loop header
  @Test
  public void testIsAssignedOnceInLifetime_forInLoop_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a; for (a in {x: 1, y: 2}) {}");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isAssignedOnceInLifetime());
  }

  // Tests function parameters reference collection
  @Test
  public void testFunctionParameters_referenceCollection() {
    ReferenceCollectingCallback callback = parseAndRun("function foo(param) { return param; }");
    Var param = getVar(callback, "param");
    assertNotNull(param);
    ReferenceCollection col = callback.getReferenceCollection(param);
    assertNotNull(col);
    assertEquals(2, col.references.size());
    assertFalse(col.firstReferenceIsAssigningDeclaration());
  }

  // Tests redeclared variable is not well-defined
  @Test
  public void testIsWellDefined_redeclaredVar_returnsFalse() {
    ReferenceCollectingCallback callback = parseAndRun("var a = 1; var a = 2; a;");
    Var a = getVar(callback, "a");
    ReferenceCollection col = callback.getReferenceCollection(a);
    assertFalse(col.isWellDefined());
  }
}
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ReferenceCollectingCallbackTest {

  private Compiler compiler;
  private Map<String, ReferenceCollection> referenceMap;
  private Map<String, Var> varMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    referenceMap = new HashMap<String, ReferenceCollection>();
    varMap = new HashMap<String, Var>();
  }

  private void processCode(String js) {
    Node root = compiler.parseTestCode(js);
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceMap rm) {
        for (Var var : t.getScope().getAllSymbols()) {
          varMap.put(var.getName(), var);
          ReferenceCollection collection = rm.getReferences(var);
          if (collection != null) {
            referenceMap.put(var.getName(), collection);
          }
        }
      }
    };
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(new Node(com.google.javascript.rhino.Token.BLOCK), root);
  }

  // Tests single assignment in a loop is detected as not assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_inWhileLoop_returnsFalse() {
    processCode("var x; while (true) { x = 1; }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests single assignment in a for loop is not assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_inForLoop_returnsFalse() {
    processCode("var x; for (var i = 0; i < 10; i++) { x = 1; }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests single assignment in a do-while loop is not assigned once in lifetime
  @Test
  public void testIsAssignedOnceInLifetime_inDoWhileLoop_returnsFalse() {
    processCode("var x; do { x = 1; } while (false);");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests assignment inside a function inside a loop
  @Test
  public void testIsAssignedOnceInLifetime_functionInsideLoop_returnsFalse() {
    processCode("var x; while (true) { function f() { x = 1; } f(); }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests variable initialized at declaration outside loops
  @Test
  public void testIsAssignedOnceInLifetime_singleDeclarationInit_returnsTrue() {
    processCode("var x = 1;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.isAssignedOnceInLifetime());
  }

  // Tests multiple assignments to the same variable
  @Test
  public void testIsAssignedOnceInLifetime_multipleAssignments_returnsFalse() {
    processCode("var x = 1; x = 2;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests variable declared without assignment
  @Test
  public void testIsNeverAssigned_unassignedVar_returnsTrue() {
    processCode("var x;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.isNeverAssigned());
    assertFalse(refs.isWellDefined());
  }

  // Tests variable initialized at declaration is well defined
  @Test
  public void testIsWellDefined_initializedVar_returnsTrue() {
    processCode("var x = 1; var y = x;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.isWellDefined());
    assertFalse(refs.isNeverAssigned());
  }

  // Tests variable initialized immediately after declaration
  @Test
  public void testIsWellDefined_initializedAssignmentAfterDeclaration_returnsTrue() {
    processCode("var x; x = 1; var y = x;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.isWellDefined());
  }

  // Tests variable used before assignment
  @Test
  public void testIsWellDefined_usedBeforeAssignment_returnsFalse() {
    processCode("function f() { var y = x; var x = 1; }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isWellDefined());
  }

  // Tests variable escaping to inner function scope
  @Test
  public void testIsEscaped_innerScopeAccess_returnsTrue() {
    processCode("var x = 1; function f() { return x; }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.isEscaped());
  }

  // Tests variable not escaping inner scope
  @Test
  public void testIsEscaped_noInnerScopeAccess_returnsFalse() {
    processCode("function f() { var x = 1; return x; }");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isEscaped());
  }

  // Tests first reference is assigning declaration
  @Test
  public void testFirstReferenceIsAssigningDeclaration_varWithInit_returnsTrue() {
    processCode("var x = 10;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertTrue(refs.firstReferenceIsAssigningDeclaration());
  }

  // Tests first reference is not assigning declaration for plain var
  @Test
  public void testFirstReferenceIsAssigningDeclaration_plainVar_returnsFalse() {
    processCode("var x; x = 10;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.firstReferenceIsAssigningDeclaration());
  }

  // Tests getInitializingReferenceForConstants
  @Test
  public void testGetInitializingReferenceForConstants_returnsValidReference() {
    processCode("var X = 42;");
    ReferenceCollection refs = referenceMap.get("X");
    assertNotNull(refs);
    Reference init = refs.getInitializingReferenceForConstants();
    assertNotNull(init);
    assertEquals(refs.references.get(0), init);
  }

  // Tests getAllSymbols and getScope methods on callback
  @Test
  public void testGetAllSymbolsAndGetScope_validTraversal_returnsCorrectSymbols() {
    Node root = compiler.parseTestCode("var a = 1; function b() { var c = 2; }");
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(new Node(com.google.javascript.rhino.Token.BLOCK), root);

    Iterable<Var> symbols = callback.getAllSymbols();
    assertNotNull(symbols);
    boolean foundA = false;
    for (Var v : symbols) {
      if ("a".equals(v.getName())) {
        foundA = true;
        assertNotNull(callback.getScope(v));
        assertNotNull(callback.getReferences(v));
      }
    }
    assertTrue(foundA);
  }

  // Tests basic block provablyExecutesBefore logic
  @Test
  public void testBasicBlock_provablyExecutesBefore_sameBlockReturnsTrue() {
    Node node = new Node(com.google.javascript.rhino.Token.BLOCK);
    BasicBlock block = new BasicBlock(null, node);
    assertTrue(block.provablyExecutesBefore(block));
    assertTrue(block.isGlobalScopeBlock());
    assertNull(block.getParent());
  }

  // Tests basic block parent descendant relationship
  @Test
  public void testBasicBlock_provablyExecutesBefore_parentExecutesBeforeChild() {
    Node parentNode = new Node(com.google.javascript.rhino.Token.BLOCK);
    BasicBlock parentBlock = new BasicBlock(null, parentNode);
    Node childNode = new Node(com.google.javascript.rhino.Token.BLOCK);
    BasicBlock childBlock = new BasicBlock(parentBlock, childNode);

    assertTrue(parentBlock.provablyExecutesBefore(childBlock));
    assertFalse(childBlock.provablyExecutesBefore(parentBlock));
    assertFalse(childBlock.isGlobalScopeBlock());
    assertEquals(parentBlock, childBlock.getParent());
  }

  // Tests hotSwapScript method
  @Test
  public void testHotSwapScript_validScript_collectsReferences() {
    Node scriptRoot = compiler.parseTestCode("var z = 100; z++;");
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(scriptRoot, null);

    boolean foundZ = false;
    for (Var v : callback.getAllSymbols()) {
      if ("z".equals(v.getName())) {
        foundZ = true;
        ReferenceCollection coll = callback.getReferences(v);
        assertEquals(2, coll.references.size());
      }
    }
    assertTrue(foundZ);
  }

  // Additional tests covering branches and member methods

  @Test
  public void testReferenceCollection_iterator() {
    processCode("var a = 1; a = 2;");
    ReferenceCollection refs = referenceMap.get("a");
    assertNotNull(refs);
    Iterator<Reference> it = refs.iterator();
    assertTrue(it.hasNext());
    assertNotNull(it.next());
    assertTrue(it.hasNext());
    assertNotNull(it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testReference_propertiesAndMethods() {
    processCode("var x = 1; x += 2;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);

    Reference r0 = refs.references.get(0);
    assertTrue(r0.isDeclaration());
    assertTrue(r0.isVarDeclaration());
    assertTrue(r0.isInitializingDeclaration());
    assertFalse(r0.isHoistedFunction());
    assertTrue(r0.isLvalue());
    assertTrue(r0.isSimpleAssignmentToName());
    assertNotNull(r0.getNode());
    assertNotNull(r0.getParent());
    assertNotNull(r0.getGrandparent());
    assertNotNull(r0.getScope());
    assertNotNull(r0.getBasicBlock());

    Reference r1 = refs.references.get(1);
    assertFalse(r1.isDeclaration());
    assertFalse(r1.isVarDeclaration());
    assertFalse(r1.isInitializingDeclaration());
    assertTrue(r1.isLvalue());
    assertFalse(r1.isSimpleAssignmentToName());
  }

  @Test
  public void testHoistedFunctionDeclaration() {
    processCode("function foo() {} foo();");
    ReferenceCollection refs = referenceMap.get("foo");
    assertNotNull(refs);

    Reference decl = refs.references.get(0);
    assertTrue(decl.isDeclaration());
    assertTrue(decl.isHoistedFunction());
    assertFalse(decl.isVarDeclaration());
    assertTrue(refs.isWellDefined());
    assertNotNull(refs.getInitializingReference());
  }

  @Test
  public void testCatchBlockReference() {
    processCode("try { var x = 1; } catch (err) { var y = err; }");
    ReferenceCollection errRefs = referenceMap.get("err");
    assertNotNull(errRefs);
    assertEquals(2, errRefs.references.size());
    Reference decl = errRefs.references.get(0);
    assertFalse(decl.isVarDeclaration());
    assertFalse(decl.isHoistedFunction());
  }

  @Test
  public void testConditionalExecution_ifElse() {
    processCode("var x; if (true) { x = 1; } else { x = 2; } var y = x;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertFalse(refs.isWellDefined());
  }

  @Test
  public void testConditionalExecution_switchCase() {
    processCode("var x = 0; switch (x) { case 1: var y = 1; break; default: var z = 2; }");
    ReferenceCollection yRefs = referenceMap.get("y");
    assertNotNull(yRefs);
    ReferenceCollection zRefs = referenceMap.get("z");
    assertNotNull(zRefs);
  }

  @Test
  public void testConditionalExecution_hookAndLogicalOps() {
    processCode("var a = true ? 1 : 2; var b = a || 3; var c = b && 4;");
    ReferenceCollection aRefs = referenceMap.get("a");
    assertNotNull(aRefs);
    ReferenceCollection bRefs = referenceMap.get("b");
    assertNotNull(bRefs);
    ReferenceCollection cRefs = referenceMap.get("c");
    assertNotNull(cRefs);
  }

  @Test
  public void testForInLoopAssignment() {
    processCode("var obj = {a: 1}; for (var k in obj) { var v = obj[k]; }");
    ReferenceCollection kRefs = referenceMap.get("k");
    assertNotNull(kRefs);
    assertFalse(kRefs.isAssignedOnceInLifetime());
  }

  @Test
  public void testReferenceCollectingCallback_filterPredicate() {
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return "a".equals(input.getName());
      }
    };
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);
    callback.process(new Node(Token.BLOCK), root);

    assertNotNull(callback.getReferences(callback.getScope(new Node(Token.BLOCK)).getVar("a")));
  }

  @Test
  public void testBasicBlock_conditionAndLoopBlocks() {
    Node ifNode = new Node(Token.IF);
    Node forNode = new Node(Token.FOR);
    Node fnNode = new Node(Token.FUNCTION);

    BasicBlock rootBlock = new BasicBlock(null, new Node(Token.BLOCK));
    BasicBlock ifBlock = new BasicBlock(rootBlock, ifNode);
    BasicBlock forBlock = new BasicBlock(rootBlock, forNode);
    BasicBlock fnBlock = new BasicBlock(rootBlock, fnNode);

    assertFalse(ifBlock.provablyExecutesBefore(forBlock));
    assertFalse(forBlock.provablyExecutesBefore(ifBlock));
    assertFalse(rootBlock.provablyExecutesBefore(fnBlock));
  }

  @Test
  public void testGetInitializingReference_noInit() {
    processCode("var x;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertNull(refs.getInitializingReference());
    assertNull(refs.getInitializingReferenceForConstants());
  }

  @Test
  public void testGetInitializingReference_twoAssignments() {
    processCode("var x = 1; x = 2;");
    ReferenceCollection refs = referenceMap.get("x");
    assertNotNull(refs);
    assertNull(refs.getInitializingReferenceForConstants());
    assertNotNull(refs.getInitializingReference());
  }
}
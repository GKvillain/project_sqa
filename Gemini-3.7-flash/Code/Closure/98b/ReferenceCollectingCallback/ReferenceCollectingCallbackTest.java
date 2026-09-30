package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReferenceCollectingCallbackTest {

  private Compiler compiler;
  private Map<Var, ReferenceCollection> referenceMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    referenceMap = Maps.newHashMap();
  }

  private Map<Var, ReferenceCollection> processJs(String js) {
    return processJs(js, null);
  }

  private Map<Var, ReferenceCollection> processJs(String js, Predicate<Var> filter) {
    Node root = compiler.parseTestCode(js);
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> map) {
        referenceMap.putAll(map);
      }
    };

    ReferenceCollectingCallback callback;
    if (filter != null) {
      callback = new ReferenceCollectingCallback(compiler, behavior, filter);
    } else {
      callback = new ReferenceCollectingCallback(compiler, behavior);
    }
    callback.process(null, root);
    return referenceMap;
  }

  private ReferenceCollection getReferencesFor(String name) {
    for (Map.Entry<Var, ReferenceCollection> entry : referenceMap.entrySet()) {
      if (entry.getKey().getName().equals(name)) {
        return entry.getValue();
      }
    }
    return null;
  }

  // Tests basic variable declaration with initialization
  @Test
  public void testProcess_varWithInitialization_collectsReferences() {
    processJs("var x = 1; x + 1;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertEquals(2, collection.references.size());
    assertTrue(collection.isWellDefined());
    assertTrue(collection.firstReferenceIsAssigningDeclaration());
    assertFalse(collection.isNeverAssigned());
    assertTrue(collection.isAssignedOnceInLifetime());
    assertFalse(collection.isEscaped());
  }

  // Tests variable declared without value then assigned immediately
  @Test
  public void testProcess_uninitializedVarFollowedByAssignment_isWellDefined() {
    processJs("var x; x = 1; x;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertEquals(3, collection.references.size());
    assertTrue(collection.isWellDefined());
    assertFalse(collection.firstReferenceIsAssigningDeclaration());
    assertNotNull(collection.getInitializingReference());
    assertEquals(collection.references.get(1), collection.getInitializingReference());
  }

  // Tests uninitialized variable without assignment is not well defined
  @Test
  public void testProcess_uninitializedVar_isNotWellDefined() {
    processJs("var x; x;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertFalse(collection.isWellDefined());
    assertTrue(collection.isNeverAssigned());
    assertNull(collection.getInitializingReference());
  }

  // Tests variable used in inner function scope is escaped
  @Test
  public void testIsEscaped_innerScopeAccess_returnsTrue() {
    processJs("var x = 1; function f() { return x; }");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertTrue(collection.isEscaped());
  }

  // Tests multiple assignments to the same variable
  @Test
  public void testIsAssignedOnceInLifetime_multipleAssignments_returnsFalse() {
    processJs("var x = 1; x = 2;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertFalse(collection.isAssignedOnceInLifetime());
    assertFalse(collection.isNeverAssigned());
  }

  // Tests variable filter predicate functionality
  @Test
  public void testProcess_withVarFilter_onlyCollectsMatchingVars() {
    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var v) {
        return "target".equals(v.getName());
      }
    };
    processJs("var target = 1; var ignored = 2;", filter);
    assertNotNull(getReferencesFor("target"));
    assertNull(getReferencesFor("ignored"));
  }

  // Tests constants initialized after first use
  @Test
  public void testGetInitializingReferenceForConstants_lateAssignment_findsInit() {
    processJs("function f() { x; } var x = 1;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertNotNull(collection.getInitializingReferenceForConstants());
  }

  // Tests function declarations and hoisted functions
  @Test
  public void testProcess_functionDeclaration_identifiedAsHoisted() {
    processJs("function foo() {} foo();");
    ReferenceCollection collection = getReferencesFor("foo");
    assertNotNull(collection);
    Reference ref = collection.references.get(0);
    assertTrue(ref.isDeclaration());
    assertTrue(ref.isHoistedFunction());
    assertTrue(ref.isInitializingDeclaration());
    assertNotNull(ref.getAssignedValue());
  }

  // Tests catch parameter declaration and references
  @Test
  public void testProcess_catchBlockVariable_isInitializingDeclaration() {
    processJs("try {} catch (e) { e; }");
    ReferenceCollection collection = getReferencesFor("e");
    assertNotNull(collection);
    Reference declRef = collection.references.get(0);
    assertTrue(declRef.isDeclaration());
    assertTrue(declRef.isInitializingDeclaration());
  }

  // Tests unary and compound assignment lvalues
  @Test
  public void testReference_incrementAndCompoundAssign_isLvalue() {
    processJs("var x = 0; x++; x += 2;");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertEquals(3, collection.references.size());
    assertTrue(collection.references.get(0).isLvalue());
    assertTrue(collection.references.get(1).isLvalue());
    assertTrue(collection.references.get(2).isLvalue());
  }

  // Tests control flow structures creating basic blocks
  @Test
  public void testBasicBlock_conditionalAndLoopBlocks_tracksHierarchy() {
    processJs("var x = 1; if (true) { x; } while (false) { x; } do { x; } while(false);");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    BasicBlock initBlock = collection.references.get(0).getBasicBlock();
    BasicBlock ifBlock = collection.references.get(1).getBasicBlock();
    BasicBlock whileBlock = collection.references.get(2).getBasicBlock();
    assertTrue(initBlock.provablyExecutesBefore(ifBlock));
    assertTrue(initBlock.provablyExecutesBefore(whileBlock));
    assertNotNull(ifBlock.getParent());
  }

  // Tests switch-case boundary basic blocks
  @Test
  public void testBasicBlock_switchCaseBoundary_createsNewBlock() {
    processJs("var x = 1; switch (x) { case 1: x; break; default: x; }");
    ReferenceCollection collection = getReferencesFor("x");
    assertNotNull(collection);
    assertTrue(collection.references.size() >= 3);
  }

  // Tests empty reference collection behavior
  @Test
  public void testReferenceCollection_emptyCollection_returnsDefaults() {
    ReferenceCollection empty = new ReferenceCollection();
    assertFalse(empty.isWellDefined());
    assertFalse(empty.isAssignedOnceInLifetime());
    assertTrue(empty.isNeverAssigned());
    assertFalse(empty.firstReferenceIsAssigningDeclaration());
    assertNull(empty.getInitializingReference());
  }

  // Tests Reference getters
  @Test
  public void testReference_nodeGetters_returnCorrectNodes() {
    processJs("var a = 1;");
    ReferenceCollection collection = getReferencesFor("a");
    assertNotNull(collection);
    Reference ref = collection.references.get(0);
    assertNotNull(ref.getNameNode());
    assertNotNull(ref.getParent());
    assertNotNull(ref.getGrandparent());
    assertNotNull(ref.getScope());
    assertNotNull(ref.getBasicBlock());
    assertTrue(ref.isVarDeclaration());
  }

  // Tests bleeding function constructor helper
  @Test
  public void testReference_newBleedingFunction_createsReference() {
    Node funcNode = compiler.parseTestCode("var f = function bleed() {};");
    NodeTraversal t = new NodeTraversal(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    BasicBlock block = new BasicBlock(null, funcNode);
    Reference ref = Reference.newBleedingFunction(t, block, funcNode.getFirstChild().getFirstChild().getFirstChild());
    assertNotNull(ref);
    assertEquals(block, ref.getBasicBlock());
  }
}
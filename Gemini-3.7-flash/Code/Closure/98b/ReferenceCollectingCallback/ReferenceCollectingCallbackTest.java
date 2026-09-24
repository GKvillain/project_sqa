package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ReferenceCollectingCallbackTest {

  private ReferenceCollection analyze(String js, final String varName) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    final ReferenceCollection[] result = new ReferenceCollection[1];
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler,
        new Behavior() {
          @Override
          public void afterExitScope(NodeTraversal t, Map<Scope.Var, ReferenceCollection> referenceMap) {
            Scope.Var v = t.getScope().getVar(varName);
            if (v != null && referenceMap.containsKey(v)) {
              result[0] = referenceMap.get(v);
            }
          }
        });
    callback.process(null, root);
    return result[0];
  }

  // Tests collecting references for a simple initialized variable
  @Test
  public void testProcess_simpleInitializedVar_collectsDeclarationAndRead() {
    ReferenceCollection refs = analyze("var x = 1; var y = x;", "x");
    assertNotNull(refs);
    assertEquals(2, refs.references.size());
    assertTrue(refs.isWellDefined());
    assertTrue(refs.firstReferenceIsAssigningDeclaration());
    assertFalse(refs.isNeverAssigned());
  }

  // Tests well-defined analysis for uninitialized variable
  @Test
  public void testIsWellDefined_uninitializedVar_returnsFalse() {
    ReferenceCollection refs = analyze("var x; x = 1;", "x");
    assertNotNull(refs);
    assertEquals(2, refs.references.size());
    assertTrue(refs.isWellDefined());
    assertFalse(refs.firstReferenceIsAssigningDeclaration());
  }

  // Tests well-defined check when variable is used without assignment
  @Test
  public void testIsWellDefined_unassignedVar_returnsFalse() {
    ReferenceCollection refs = analyze("var x; var y = x;", "x");
    assertNotNull(refs);
    assertFalse(refs.isWellDefined());
    assertTrue(refs.isNeverAssigned());
    assertNull(refs.getInitializingReference());
  }

  // Tests well-defined check when assignment is inside conditional branch
  @Test
  public void testIsWellDefined_assignedInIfBranch_returnsFalse() {
    ReferenceCollection refs = analyze("var x; if (true) { x = 1; } var y = x;", "x");
    assertNotNull(refs);
    assertFalse(refs.isWellDefined());
  }

  // Tests scope escaping detection when variable is accessed in inner function
  @Test
  public void testIsEscaped_usedInInnerScope_returnsTrue() {
    ReferenceCollection refs = analyze("var x = 1; function f() { return x; }", "x");
    assertNotNull(refs);
    assertTrue(refs.isEscaped());
  }

  // Tests scope escaping detection when variable is only in root scope
  @Test
  public void testIsEscaped_usedOnlyInSameScope_returnsFalse() {
    ReferenceCollection refs = analyze("var x = 1; var y = x + 2;", "x");
    assertNotNull(refs);
    assertFalse(refs.isEscaped());
  }

  // Tests isAssignedOnceInLifetime for single assignment
  @Test
  public void testIsAssignedOnceInLifetime_singleAssignment_returnsTrue() {
    ReferenceCollection refs = analyze("var x = 1; var y = x;", "x");
    assertNotNull(refs);
    assertTrue(refs.isAssignedOnceInLifetime());
  }

  // Tests isAssignedOnceInLifetime for multiple assignments
  @Test
  public void testIsAssignedOnceInLifetime_reassigned_returnsFalse() {
    ReferenceCollection refs = analyze("var x = 1; x = 2;", "x");
    assertNotNull(refs);
    assertFalse(refs.isAssignedOnceInLifetime());
  }

  // Tests getInitializingReference for var declaration initialized immediately
  @Test
  public void testGetInitializingReference_varWithInitialValue_returnsFirstReference() {
    ReferenceCollection refs = analyze("var x = 10;", "x");
    assertNotNull(refs);
    Reference init = refs.getInitializingReference();
    assertNotNull(init);
    assertTrue(init.isDeclaration());
    assertTrue(init.isInitializingDeclaration());
    assertEquals(refs.references.get(0), init);
  }

  // Tests getInitializingReference for declaration followed by assignment
  @Test
  public void testGetInitializingReference_varThenAssign_returnsSecondReference() {
    ReferenceCollection refs = analyze("var x; x = 10;", "x");
    assertNotNull(refs);
    Reference init = refs.getInitializingReference();
    assertNotNull(init);
    assertTrue(init.isSimpleAssignmentToName());
    assertEquals(refs.references.get(1), init);
  }

  // Tests getInitializingReferenceForConstants when assigned later
  @Test
  public void testGetInitializingReferenceForConstants_assignedAfterUse_returnsAssignment() {
    ReferenceCollection refs = analyze("var x; y = x; x = 1;", "x");
    assertNotNull(refs);
    Reference constInit = refs.getInitializingReferenceForConstants();
    assertNotNull(constInit);
    assertTrue(constInit.isSimpleAssignmentToName());
  }

  // Tests isLvalue for increment and assignment operators
  @Test
  public void testReference_lvalueDetection_identifiesAssignments() {
    ReferenceCollection refsInc = analyze("var x = 0; x++;", "x");
    assertNotNull(refsInc);
    assertTrue(refsInc.references.get(1).isLvalue());

    ReferenceCollection refsAddAssign = analyze("var x = 0; x += 1;", "x");
    assertNotNull(refsAddAssign);
    assertTrue(refsAddAssign.references.get(1).isLvalue());
  }

  // Tests Reference attributes on function declaration
  @Test
  public void testReference_functionDeclaration_isDeclarationAndInitializing() {
    ReferenceCollection refs = analyze("function f() {} f();", "f");
    assertNotNull(refs);
    Reference first = refs.references.get(0);
    assertTrue(first.isDeclaration());
    assertTrue(first.isInitializingDeclaration());
    assertNotNull(first.getAssignedValue());
    assertEquals(Token.FUNCTION, first.getAssignedValue().getType());
  }

  // Tests Reference attributes on catch block parameter
  @Test
  public void testReference_catchClause_isDeclarationAndInitializing() {
    ReferenceCollection refs = analyze("try {} catch (e) { var y = e; }", "e");
    assertNotNull(refs);
    Reference first = refs.references.get(0);
    assertTrue(first.isDeclaration());
    assertTrue(first.isInitializingDeclaration());
  }

  // Tests BasicBlock provablyExecutesBefore relationship
  @Test
  public void testBasicBlock_provablyExecutesBefore_ancestorExecutesBeforeDescendant() {
    Node rootNode = new Node(Token.BLOCK);
    BasicBlock rootBlock = new BasicBlock(null, rootNode);
    Node childNode = new Node(Token.BLOCK);
    BasicBlock childBlock = new BasicBlock(rootBlock, childNode);

    assertTrue(rootBlock.provablyExecutesBefore(childBlock));
    assertFalse(childBlock.provablyExecutesBefore(rootBlock));
    assertTrue(rootBlock.provablyExecutesBefore(rootBlock));
    assertEquals(rootBlock, childBlock.getParent());
  }

  // Tests constructor with varFilter and behavior DO_NOTHING_BEHAVIOR
  @Test
  public void testProcess_filteredVariables_onlyCollectsMatchingVars() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler,
        ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR,
        Predicates.<Scope.Var>alwaysFalse());

    callback.process(null, root);
    Scope.Var varA = compiler.getTopScope().getVar("a");
    assertNull(callback.getReferenceCollection(varA));
  }

  // Tests empty ReferenceCollection edge cases
  @Test
  public void testReferenceCollection_emptyCollection_returnsFalseAndNull() {
    ReferenceCollection empty = new ReferenceCollection();
    assertFalse(empty.isWellDefined());
    assertFalse(empty.isAssignedOnceInLifetime());
    assertTrue(empty.isNeverAssigned());
    assertFalse(empty.firstReferenceIsAssigningDeclaration());
    assertNull(empty.getInitializingReference());
    assertNull(empty.getInitializingReferenceForConstants());
  }

  // Tests control structures creating basic block boundaries
  @Test
  public void testProcess_variousControlStructures_traversesSuccessfully() {
    String code = "var x = 0;\n"
        + "do { x++; } while (x < 1);\n"
        + "while (x < 2) { x++; }\n"
        + "for (var i = 0; i < 1; i++) { x += i; }\n"
        + "if (x > 0) { x = 3; } else { x = 4; }\n"
        + "switch (x) { case 3: x = 5; break; }\n"
        + "try { x = 6; } catch (e) { x = 7; }\n"
        + "var y = (x > 0) ? x : 0;\n"
        + "var z = x && y;\n"
        + "var w = x || y;\n";

    ReferenceCollection refs = analyze(code, "x");
    assertNotNull(refs);
    assertTrue(refs.references.size() > 5);
  }
}
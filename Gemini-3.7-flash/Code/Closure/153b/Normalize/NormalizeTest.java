package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NormalizeTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node testNormalize(String js) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    return root;
  }

  // Tests splitting multiple var declarations into separate var statements
  @Test
  public void testProcess_splitVars_separatesDeclarations() {
    Node root = testNormalize("var a = 1, b = 2;");
    int varCount = 0;
    for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varCount++;
        assertTrue(c.hasOneChild());
      }
    }
    assertEquals(2, varCount);
  }

  // Tests conversion of while loop to for loop
  @Test
  public void testProcess_whileLoop_convertsToForLoop() {
    Node root = testNormalize("while (true) { foo(); }");
    Node statement = root.getFirstChild();
    assertEquals(Token.FOR, statement.getType());
    assertEquals(Token.EMPTY, statement.getFirstChild().getType());
  }

  // Tests extracting var initializer from for-in loop
  @Test
  public void testProcess_forInVar_extractsVarDeclaration() {
    Node root = testNormalize("for (var a in b) {}");
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("a", first.getFirstChild().getString());
    Node forNode = first.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  // Tests extracting initializer from standard for loop
  @Test
  public void testProcess_forInitializer_movesInitBeforeFor() {
    Node root = testNormalize("for (var i = 0; i < 10; i++) {}");
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("i", first.getFirstChild().getString());
    Node forNode = first.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  // Tests that duplicate var declarations are converted to assignments
  @Test
  public void testProcess_duplicateVarDeclaration_replacesWithAssignment() {
    Node root = testNormalize("var x = 1; var x = 2;");
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  // Tests converting unhoisted function declaration in block to var declaration
  @Test
  public void testProcess_unhoistedFunctionDeclaration_rewritesToVarFunction() {
    Node root = testNormalize("if (true) { function f() {} }");
    Node ifNode = root.getFirstChild();
    Node block = ifNode.getLastChild();
    Node varNode = block.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    assertEquals("f", varNode.getFirstChild().getString());
    assertEquals(Token.FUNCTION, varNode.getFirstChild().getFirstChild().getType());
  }

  // Tests hoisting named functions to the top of function body
  @Test
  public void testProcess_moveNamedFunctions_hoistsToBeginning() {
    Node root = testNormalize("function outer() { var x = 1; function inner() {} }");
    Node outerFn = root.getFirstChild();
    Node outerBody = outerFn.getLastChild();
    Node firstInBody = outerBody.getFirstChild();
    assertEquals(Token.FUNCTION, firstInBody.getType());
    assertEquals("inner", firstInBody.getFirstChild().getString());
  }

  // Tests wrapping non-block labeled statements into blocks
  @Test
  public void testProcess_labeledStatement_wrapsInBlock() {
    Node root = testNormalize("lbl: foo();");
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelTarget = labelNode.getLastChild();
    assertEquals(Token.BLOCK, labelTarget.getType());
    assertEquals(Token.EXPR_RESULT, labelTarget.getFirstChild().getType());
  }

  // Tests catch block redeclaration reporting error
  @Test
  public void testProcess_catchVarRedeclaration_reportsError() {
    Node root = compiler.parseTestCode("function f() { try {} catch (e) { var e = 1; } }");
    Node externs = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests duplicate var declaration when function name conflicts with var
  @Test
  public void testProcess_functionRedeclaringVar_handlesConflict() {
    Node root = testNormalize("function f() { var x = 1; function x() {} }");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests IllegalStateException thrown when assertOnChange is true and changes occur
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChange_throwsIllegalStateException() {
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests parseAndNormalizeTestCode utility method
  @Test
  public void testParseAndNormalizeTestCode_validCode_normalizesNode() {
    Node node = Normalize.parseAndNormalizeTestCode(compiler, "while(true);", "prefix");
    assertNotNull(node);
    assertEquals(Token.FOR, node.getFirstChild().getType());
  }

  // Tests parseAndNormalizeSyntheticCode utility method
  @Test
  public void testParseAndNormalizeSyntheticCode_validCode_normalizesWithPrefix() {
    Node node = Normalize.parseAndNormalizeSyntheticCode(compiler, "var a = 1, b = 2;", "syn_");
    assertNotNull(node);
    int varCount = 0;
    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varCount++;
      }
    }
    assertEquals(2, varCount);
  }

  // Tests PropagateConstantAnnotationsOverVars marking constant by convention
  @Test
  public void testPropagateConstantAnnotationsOverVars_constantConvention_setsProp() {
    Node root = compiler.parseTestCode("var CONST_VALUE = 42;");
    Node externs = new Node(Token.BLOCK);
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);
    Node nameNode = root.getFirstChild().getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests VerifyConstants pass with consistent annotations
  @Test
  public void testVerifyConstants_consistentConstants_passes() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var CONST_FOO = 1; var y = CONST_FOO;");
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize.PropagateConstantAnnotationsOverVars propagate =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    propagate.process(externs, root);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, false);
    verify.process(externs, root);
  }

  // Tests VerifyConstants pass throwing when constants are inconsistently annotated
  @Test(expected = IllegalStateException.class)
  public void testVerifyConstants_inconsistentConstants_throwsIllegalStateException() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var CONST_FOO = 1; var y = CONST_FOO;");
    new Node(Token.BLOCK, externs, root);

    Node firstVarName = root.getFirstChild().getFirstChild();
    firstVarName.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, false);
    verify.process(externs, root);
  }
}
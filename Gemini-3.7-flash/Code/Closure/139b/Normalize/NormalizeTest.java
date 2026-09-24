package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NormalizeTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node testNormalize(String js, String expectedJs) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    String actual = compiler.toSource(root);
    assertEquals(expectedJs, actual);
    return root;
  }

  // Tests splitting multiple variable declarations in a single var statement
  @Test
  public void testProcess_splitVarDeclarations_splitsCorrectly() {
    testNormalize("var a = 1, b = 2;", "var a = 1;var b = 2;");
  }

  // Tests splitting variable declarations without initializers
  @Test
  public void testProcess_splitVarDeclarationsNoInit_splitsCorrectly() {
    testNormalize("var a, b;", "var a;var b;");
  }

  // Tests converting while loops into for loops
  @Test
  public void testProcess_whileLoop_convertsToForLoop() {
    testNormalize("while (true) { foo(); }", "for (;true;) { foo(); }");
  }

  // Tests extracting var initializer from for loop
  @Test
  public void testProcess_forLoopWithVarInit_extractsVarInitializer() {
    testNormalize("for (var i = 0; i < 10; i++) {}", "var i = 0;for (;i < 10;i++) {}");
  }

  // Tests extracting expression initializer from for loop
  @Test
  public void testProcess_forLoopWithExprInit_extractsExprInitializer() {
    testNormalize("var i; for (i = 0; i < 10; i++) {}", "var i;i = 0;for (;i < 10;i++) {}");
  }

  // Tests moving hoisted function declarations to the top of the containing function
  @Test
  public void testProcess_hoistedFunction_movesToTop() {
    testNormalize("function f() { var x = 1; function g() {} }",
        "function f() { function g() {} var x = 1; }");
  }

  // Tests handling functions already at the top
  @Test
  public void testProcess_functionAlreadyAtTop_remainsAtTop() {
    testNormalize("function f() { function g() {} var x = 1; }",
        "function f() { function g() {} var x = 1; }");
  }

  // Tests removing duplicate var declaration with assignment
  @Test
  public void testProcess_duplicateVarWithInit_convertsToAssignment() {
    testNormalize("var a = 1; var a = 2;", "var a = 1;a = 2;");
  }

  // Tests removing duplicate var declaration without assignment
  @Test
  public void testProcess_duplicateVarWithoutInit_removesDeclaration() {
    testNormalize("var a = 1; var a;", "var a = 1;");
  }

  // Tests normalizing non-block label child into a block
  @Test
  public void testProcess_labelWithExpr_wrapsInBlock() {
    testNormalize("label: a();", "label: { a(); }");
  }

  // Tests label on while loop preserves structure after while to for conversion
  @Test
  public void testProcess_labelOnWhile_convertsWhileInPlace() {
    testNormalize("label: while (true) { break label; }",
        "label: for (;true;) { break label; }");
  }

  // Tests extractForInitializer inside a labeled block
  @Test
  public void testProcess_labeledForLoopWithInit_extractsBeforeLabel() {
    testNormalize("label: for (var i = 0; i < 10; i++) {}",
        "var i = 0;label: for (;i < 10;i++) {}");
  }

  // Tests assertOnChange throws exception when modification occurs
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChange_throwsOnModification() {
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests duplicate var declaration in for-in loop
  @Test
  public void testProcess_duplicateVarInForIn_removesVar() {
    testNormalize("var a; for (var a in obj) {}", "var a;for (a in obj) {}");
  }

  // Tests duplicate var declaration with label
  @Test
  public void testProcess_duplicateVarInLabel_replacesWithEmpty() {
    testNormalize("var a = 1; label: var a;", "var a = 1;label: ;");
  }

  // Tests propagating constant annotations on JSDoc constant variables
  @Test
  public void testPropogateConstantAnnotations_annotatesConstVar() {
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 1; var use = CONST_VAL;");
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    pass.process(externs, root);

    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests PropagateConstantAnnotations throws when assertOnChange is true and change occurs
  @Test(expected = IllegalStateException.class)
  public void testPropogateConstantAnnotations_assertOnChange_throwsOnAnnotation() {
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 1;");
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    pass.process(externs, root);
  }

  // Tests VerifyConstants passes for consistent constant declarations
  @Test
  public void testVerifyConstants_validConstants_passesVerification() {
    Node root = compiler.parseTestCode("var CONST_A = 1; var b = CONST_A;");
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // Tests VerifyConstants checks user declarations correctly
  @Test
  public void testVerifyConstants_checkUserDeclarations_validatesSuccessfully() {
    Node root = compiler.parseTestCode("var a = 1; var b = a;");
    Node externs = new Node(Token.BLOCK);
    Node externsAndJs = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }
}
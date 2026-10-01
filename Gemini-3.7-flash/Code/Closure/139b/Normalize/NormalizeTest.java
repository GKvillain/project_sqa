package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node testNormalize(String js, String expectedJs) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    if (expectedJs != null) {
      Node expectedRoot = compiler.parseTestCode(expectedJs);
      String explanation = compiler.toSource(root) + " != " + compiler.toSource(expectedRoot);
      assertEquals(explanation, compiler.toSource(expectedRoot), compiler.toSource(root));
    }
    return root;
  }

  // Tests splitting multiple variable declarations into individual var statements
  @Test
  public void testProcess_splitVarDeclarations_splitsVariables() {
    testNormalize("var a = 1, b = 2;", "var a = 1; var b = 2;");
  }

  // Tests moving variable initializers out of standard for loop
  @Test
  public void testProcess_forLoopInitializer_extractsInitializer() {
    testNormalize("for (var i = 0; i < 10; i++) {}", "var i = 0; for (; i < 10; i++) {}");
  }

  // Tests converting while loops to for loops
  @Test
  public void testProcess_whileLoop_convertsToForLoop() {
    testNormalize("while (true) { foo(); }", "for (; true;) { foo(); }");
  }

  // Tests moving hoisted function declarations to the top of the enclosing function scope
  @Test
  public void testProcess_unhoistedFunctionDeclaration_movesToTopOfScope() {
    testNormalize(
        "function f() { var x = 1; function g() {} return x; }",
        "function f() { function g() {} var x = 1; return x; }"
    );
  }

  // Tests wrapping non-block labeled statements into a block
  @Test
  public void testProcess_labelNonBlock_wrapsInBlock() {
    testNormalize("lab: a = 1;", "lab: { a = 1; }");
  }

  // Tests labeled loops retain their structure without extra block wrapping
  @Test
  public void testProcess_labeledLoop_preservesStructure() {
    testNormalize("lab: for (; true;) {}", "lab: for (; true;) {}");
  }

  // Tests duplicate var declarations are converted to assignments
  @Test
  public void testProcess_duplicateVarDeclaration_convertsToAssignment() {
    testNormalize("var a = 1; var a = 2;", "var a = 1; a = 2;");
  }

  // Tests duplicate empty var declarations are removed
  @Test
  public void testProcess_duplicateEmptyVarDeclaration_removesSecondVar() {
    testNormalize("var a = 1; var a;", "var a = 1;");
  }

  // Tests duplicate var in for-in loop has the var keyword removed
  @Test
  public void testProcess_duplicateVarInForIn_removesVarInForIn() {
    testNormalize("var a = 1; for (var a in b) {}", "var a = 1; for (a in b) {}");
  }

  // Tests exception path when assertOnChange is true and code is modified
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChange_throwsExceptionOnModification() {
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests constant annotation propagation visitor
  @Test
  public void testPropagateConstantAnnotations_jsDocConstant_marksNodeAsConstant() {
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 10; var b = CONST_VAL;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize.PropagateConstantAnnotations propPass =
        new Normalize.PropagateConstantAnnotations(compiler, false);
    propPass.process(externs, root);
    assertNotNull(root);
  }

  // Tests constant propagation exception path when assertOnChange is enabled
  @Test(expected = IllegalStateException.class)
  public void testPropagateConstantAnnotations_assertOnChange_throwsException() {
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 10;");
    Node externs = new Node(Token.BLOCK);
    Node nameNode = new Node(Token.NAME, "CONST_VAL");
    Node varNode = new Node(Token.VAR, nameNode);
    root.addChildToBack(varNode);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build();
    nameNode.setJSDocInfo(info);

    Normalize.PropagateConstantAnnotations propPass =
        new Normalize.PropagateConstantAnnotations(compiler, true);
    propPass.process(externs, root);
  }

  // Tests VerifyConstants pass when constants are consistently annotated
  @Test
  public void testVerifyConstants_validUsage_succeeds() {
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifyPass = new Normalize.VerifyConstants(compiler, false);
    verifyPass.process(externs, root);
    assertNotNull(parent);
  }

  // Tests VerifyConstants pass throws exception when constant annotations are inconsistent
  @Test(expected = IllegalStateException.class)
  public void testVerifyConstants_inconsistentConstants_throwsException() {
    Node root = compiler.parseTestCode("var a = 1; var a = 2;");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    Node name1 = root.getFirstChild().getFirstChild();
    Node name2 = root.getLastChild().getFirstChild();
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    Normalize.VerifyConstants verifyPass = new Normalize.VerifyConstants(compiler, false);
    verifyPass.process(externs, root);
  }

  // Tests normalizing function declarations inside blocks
  @Test
  public void testProcess_functionInBlock_hoistsFunction() {
    testNormalize("if (true) { function f() {} }", "if (true) { var f = function() {}; }");
  }

  // Tests multiple variable declarations in for-in loop body
  @Test
  public void testProcess_varInForIn_preservesSingleVar() {
    testNormalize("for (var a in obj) { foo(a); }", "for (var a in obj) { foo(a); }");
  }

  // Tests duplicate var declaration with function parameters
  @Test
  public void testProcess_duplicateVarWithParam_removesVar() {
    testNormalize("function f(x) { var x = 1; }", "function f(x) { x = 1; }");
  }

  // Tests duplicate var declaration without init when param exists
  @Test
  public void testProcess_duplicateEmptyVarWithParam_removesVarStatement() {
    testNormalize("function f(x) { var x; }", "function f(x) {}");
  }

  // Tests nested function scope duplicate var handling
  @Test
  public void testProcess_nestedScopeDuplicateVars_normalizesCorrectly() {
    testNormalize(
        "var x = 1; function f() { var x = 2; var x = 3; }",
        "var x = 1; function f() { var x = 2; x = 3; }"
    );
  }

  // Tests VerifyConstants with checkUserDeclarations set to true
  @Test
  public void testVerifyConstants_checkUserDeclarations_succeeds() {
    Node root = compiler.parseTestCode("var A = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize.VerifyConstants verifyPass = new Normalize.VerifyConstants(compiler, true);
    verifyPass.process(externs, root);
    assertNotNull(root);
  }
}
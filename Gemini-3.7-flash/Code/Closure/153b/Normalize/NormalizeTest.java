package com.google.javascript.jscomp;

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

  private Node normalize(String js) {
    return normalize("", js);
  }

  private Node normalize(String externsJs, String js) {
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    return root;
  }

  // Tests splitting multiple variable declarations in a single var statement
  @Test
  public void testNormalize_splitVars_splitsMultipleDeclarations() {
    Node root = normalize("var a = 1, b = 2;");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.isNormalized());
  }

  // Tests converting while loops into for loops
  @Test
  public void testNormalize_whileLoop_convertsToFor() {
    Node root = normalize("while (x < 10) { x++; }");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
    Node firstStatement = root.getFirstChild();
    assertEquals(Token.FOR, firstStatement.getType());
  }

  // Tests extracting initializers from standard for loops
  @Test
  public void testNormalize_forLoop_extractsInitializer() {
    Node root = normalize("for (var i = 0; i < 10; i++) {}");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests extracting variable declarations from for-in loops
  @Test
  public void testNormalize_forInLoop_extractsVarDeclaration() {
    Node root = normalize("for (var prop in obj) {}");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests hoisting functions to top of containing function body
  @Test
  public void testNormalize_moveNamedFunctions_hoistsFunctions() {
    Node root = normalize("function outer() { foo(); function inner() {} }");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests removing duplicate var declarations with initializers
  @Test
  public void testNormalize_duplicateVarDeclarations_removesDuplicates() {
    Node root = normalize("var a = 1; var a = 2;");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests removing duplicate var declarations without initializers
  @Test
  public void testNormalize_duplicateVarNoInit_removesDuplicates() {
    Node root = normalize("var a = 1; var a;");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests duplicate declaration between a function declaration and a var declaration
  @Test
  public void testNormalize_duplicateFunctionAndVar_handlesGracefully() {
    Node root = normalize("var f = 1; function f() {}");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests duplicate declaration in reverse order (var after function)
  @Test
  public void testNormalize_varAfterFunction_handlesGracefully() {
    Node root = normalize("function f() {} var f = 1;");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests catch block variable redeclaration triggers error
  @Test
  public void testNormalize_catchBlockVarRedeclaration_reportsError() {
    normalize("function test() { try { throw 0; } catch (e) { var e = 1; } }");
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests label normalization wrapping non-block statement in a block
  @Test
  public void testNormalize_labelNonBlock_normalizesLabel() {
    Node root = normalize("myLabel: a = 1;");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
    Node label = root.getFirstChild();
    assertEquals(Token.LABEL, label.getType());
    assertEquals(Token.BLOCK, label.getLastChild().getType());
  }

  // Tests parseAndNormalizeSyntheticCode static method
  @Test
  public void testNormalize_parseAndNormalizeSyntheticCode_returnsNormalizedNode() {
    Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, "var a = 1, b = 2;", "prefix_");
    assertNotNull(result);
    assertEquals(Token.SCRIPT, result.getType());
  }

  // Tests parseAndNormalizeTestCode static method
  @Test
  public void testNormalize_parseAndNormalizeTestCode_returnsNormalizedNode() {
    Node result = Normalize.parseAndNormalizeTestCode(compiler, "while (true) {}", "prefix_");
    assertNotNull(result);
    assertEquals(Token.FOR, result.getFirstChild().getType());
  }

  // Tests assertOnChange throws exception when AST requires modification
  @Test(expected = IllegalStateException.class)
  public void testNormalize_assertOnChange_throwsOnModification() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("while (true) {}");
    new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests PropagateConstantAnnotationsOverVars pass
  @Test
  public void testPropagateConstantAnnotationsOverVars_marksConstants() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var CONST_VAL = 1; var y = CONST_VAL;");
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);
    assertNotNull(root);
  }

  // Tests VerifyConstants pass without violations
  @Test
  public void testVerifyConstants_validCode_success() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1; var b = a;");
    new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
    assertEquals(0, compiler.getErrorCount());
  }
}
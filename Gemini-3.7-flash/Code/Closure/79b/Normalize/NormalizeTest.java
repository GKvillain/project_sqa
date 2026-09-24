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
    Node externs = compiler.parseTestCode("");
    Node externsAndJs = new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    return root;
  }

  private String toSource(Node n) {
    return compiler.toSource(n);
  }

  // Tests splitting multiple var declarations into separate var statements
  @Test
  public void testProcess_splitVarDeclarations_splitsIntoMultipleStatements() {
    Node root = testNormalize("var a = 1, b = 2;");
    assertEquals("var a=1;var b=2", toSource(root));
  }

  // Tests conversion of while loop to for loop
  @Test
  public void testProcess_whileLoop_convertsToForLoop() {
    Node root = testNormalize("while(x < 10) { x++; }");
    assertEquals("for(;x<10;)x++", toSource(root));
  }

  // Tests moving for loop initializer out of for statement
  @Test
  public void testProcess_forLoopInitializer_extractsInitializer() {
    Node root = testNormalize("for (var i = 0; i < 10; i++) {}");
    assertEquals("var i=0;for(;i<10;i++);", toSource(root));
  }

  // Tests extracting var declaration in for-in loop
  @Test
  public void testProcess_forInVarDeclaration_extractsVar() {
    Node root = testNormalize("for (var k in obj) {}");
    assertEquals("var k;for(k in obj);", toSource(root));
  }

  // Tests moving hoisted function declarations to the top of function scope
  @Test
  public void testProcess_hoistedFunction_movesToTopOfScope() {
    Node root = testNormalize("function f() { var x = 1; function g() {} }");
    assertEquals("function f(){function g(){}var x=1}", toSource(root));
  }

  // Tests rewriting unhoisted function declaration in block to var assignment
  @Test
  public void testProcess_unhoistedFunctionInBlock_rewritesToVar() {
    Node root = testNormalize("if (true) { function f() {} }");
    assertEquals("if(true){var f=function(){}}", toSource(root));
  }

  // Tests duplicate var declaration removal with assignment preservation
  @Test
  public void testProcess_duplicateVarDeclaration_convertsSecondToAssignment() {
    Node root = testNormalize("var a = 1; var a = 2;");
    assertEquals("var a=1;a=2", toSource(root));
  }

  // Tests duplicate var declaration without initializer removal
  @Test
  public void testProcess_duplicateEmptyVarDeclaration_removesDuplicate() {
    Node root = testNormalize("var a = 1; var a;");
    assertEquals("var a=1", toSource(root));
  }

  // Tests duplicate var declaration error inside catch block
  @Test
  public void testProcess_catchBlockDuplicateVar_reportsError() {
    Node root = compiler.parseTestCode("try { throw 1; } catch (e) { var e = 2; }");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests label normalization when wrapping non-block statement
  @Test
  public void testProcess_labelNormalization_wrapsInBlock() {
    Node root = testNormalize("label: var a = 1;");
    assertEquals("label:{var a=1}", toSource(root));
  }

  // Tests parseAndNormalizeSyntheticCode utility method
  @Test
  public void testParseAndNormalizeSyntheticCode_validCode_normalizesAst() {
    Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, "var a = 1, b = 2;", "prefix_");
    assertNotNull(result);
    assertEquals("var a=1;var b=2", toSource(result));
  }

  // Tests parseAndNormalizeTestCode utility method
  @Test
  public void testParseAndNormalizeTestCode_validCode_normalizesAst() {
    Node result = Normalize.parseAndNormalizeTestCode(compiler, "while(true) {}", "prefix_");
    assertNotNull(result);
    assertEquals("for(;true;);", toSource(result));
  }

  // Tests PropagateConstantAnnotationsOverVars marks constant variables
  @Test
  public void testPropagateConstantAnnotationsOverVars_constantJsDoc_marksIsConstant() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("/** @const */ var FOO = 1; var b = FOO;");
    Node externsAndJs = new Node(Token.BLOCK, externs, root);
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);

    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests VerifyConstants passes when constant names are consistently annotated
  @Test
  public void testVerifyConstants_consistentAnnotations_processesSuccessfully() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1;");
    Node externsAndJs = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
    assertFalse(compiler.hasErrors());
  }

  // Tests assertOnChange throws IllegalStateException on modifications
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChangeWithUnnormalizedCode_throwsException() {
    Node root = compiler.parseTestCode("while(true) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests duplicate var declaration without initializer in catch block reports error
  @Test
  public void testProcess_catchBlockDuplicateVarWithoutInit_reportsError() {
    Node root = compiler.parseTestCode("try {} catch (e) { var e; }");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests multiple variables declared in for loop header are extracted properly
  @Test
  public void testProcess_forLoopWithMultipleVars_extractsAllVars() {
    Node root = testNormalize("for (var i = 0, j = 1; i < 10; i++) {}");
    assertEquals("var i=0;var j=1;for(;i<10;i++);", toSource(root));
  }

  // Tests unhoisted function declaration in IF without block wrapper
  @Test
  public void testProcess_functionInIfWithoutBlock_normalizesToVar() {
    Node root = testNormalize("if (true) function f() {}");
    assertEquals("if(true){var f=function(){}}", toSource(root));
  }

  // Tests nested labels wrapping statement in block
  @Test
  public void testProcess_nestedLabels_wrapsInBlock() {
    Node root = testNormalize("a: b: var x = 1;");
    assertEquals("a:b:{var x=1}", toSource(root));
  }

  // Tests empty statements removal
  @Test
  public void testProcess_emptyStatements_removesEmptyNodes() {
    Node root = testNormalize(";;var a = 1;;;");
    assertEquals("var a=1", toSource(root));
  }

  // Tests duplicate var across different scopes preserves the inner declaration
  @Test
  public void testProcess_duplicateVarAcrossScopes_preservesInnerScopeVar() {
    Node root = testNormalize("var a = 1; function f() { var a = 2; }");
    assertEquals("function f(){var a=2}var a=1", toSource(root));
  }

  // Tests function declarations at the root of a script are hoisted to top of script
  @Test
  public void testProcess_hoistedFunctionAtScriptLevel_movesToTop() {
    Node root = testNormalize("var x = 1; function f() {}");
    assertEquals("function f(){}var x=1", toSource(root));
  }

  // Tests VerifyConstants detects inconsistent constant annotations
  @Test(expected = IllegalStateException.class)
  public void testVerifyConstants_inconsistentConstantAnnotation_throwsException() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1; a = 2;");
    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  // Tests PropagateConstantAnnotationsOverVars with assertOnChange flag enabled
  @Test
  public void testPropagateConstantAnnotationsOverVars_withAssertOnChange_processesSuccessfully() {
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1;");
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, true);
    pass.process(externs, root);
    assertFalse(compiler.hasErrors());
  }
}
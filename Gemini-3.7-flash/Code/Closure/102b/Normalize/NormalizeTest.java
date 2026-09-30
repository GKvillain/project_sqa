package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class NormalizeTest extends CompilerTestCase {

  private boolean assertOnChange = false;

  public NormalizeTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new Normalize(compiler, assertOnChange);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    assertOnChange = false;
  }

  // Tests converting WHILE loops to FOR loops
  @Test
  public void testVisit_whileLoop_convertedToForLoop() {
    test("while (a < 10) { a++; }", "for (; a < 10;) { a++; }");
  }

  // Tests splitting multiple VAR declarations into separate statements
  @Test
  public void testSplitVarDeclarations_multipleDeclarations_splitsIntoSeparateVars() {
    test("var a = 1, b = 2, c = 3;", "var a = 1; var b = 2; var c = 3;");
  }

  // Tests splitting uninitialized multiple VAR declarations
  @Test
  public void testSplitVarDeclarations_uninitializedVars_splitsIntoSeparateVars() {
    test("var x, y;", "var x; var y;");
  }

  // Tests extracting VAR initializer from FOR loop
  @Test
  public void testExtractForInitializer_varInit_extractedBeforeFor() {
    test("for (var i = 0; i < 10; i++) {}", "var i = 0; for (; i < 10; i++) {}");
  }

  // Tests extracting expression initializer from FOR loop
  @Test
  public void testExtractForInitializer_expressionInit_extractedBeforeFor() {
    test("for (i = 0; i < 10; i++) {}", "i = 0; for (; i < 10; i++) {}");
  }

  // Tests that FOR-IN loop initializer is not extracted
  @Test
  public void testExtractForInitializer_forInLoop_remainsUnextracted() {
    testSame("for (var prop in obj) {}");
  }

  // Tests extracting FOR initializer inside labeled statement
  @Test
  public void testExtractForInitializer_insideLabel_extractedBeforeLabel() {
    test("lab: for (var j = 0; j < 5; j++) {}", "var j = 0; lab: for (; j < 5; j++) {}");
  }

  // Tests removing simple duplicate VAR declarations without initializers
  @Test
  public void testRemoveDuplicateDeclarations_duplicateUninitializedVar_removesDuplicate() {
    test("var x = 1; var x;", "var x = 1;");
  }

  // Tests converting duplicate VAR with initializer to ASSIGN statement
  @Test
  public void testRemoveDuplicateDeclarations_duplicateVarWithInit_convertsToAssignment() {
    test("var a = 1; var a = 2;", "var a = 1; a = 2;");
  }

  // Tests duplicate VAR removal in FOR-IN loop
  @Test
  public void testRemoveDuplicateDeclarations_duplicateVarInForIn_removesVarKeyword() {
    test("var k; for (var k in obj) {}", "var k; for (k in obj) {}");
  }

  // Tests handling duplicate declaration of arguments in function scope (Closure-102 regression)
  @Test
  public void testRemoveDuplicateDeclarations_duplicateArgumentsVar_convertsToAssignment() {
    test("function f() { var arguments = 1; }", "function f() { arguments = 1; }");
  }

  // Tests moving named function declarations to the top of the function body
  @Test
  public void testMoveNamedFunctions_functionDeclarationAfterStatement_movedToTop() {
    test("function outer() { var x = 1; function inner() {} }",
         "function outer() { function inner() {} var x = 1; }");
  }

  // Tests preserving function declarations already at the top
  @Test
  public void testMoveNamedFunctions_functionsAlreadyAtTop_orderingPreserved() {
    testSame("function outer() { function f1() {} function f2() {} var x = 1; }");
  }

  // Tests normalizing labels wrapping non-block/non-loop statements
  @Test
  public void testNormalizeLabels_statementUnderLabel_wrappedInBlock() {
    test("foo: a = 1;", "foo: { a = 1; }");
  }

  // Tests constant propagation from JSDoc @const annotation
  @Test
  public void testPropagateConstantAnnotations_jsdocConstant_marksConstantProp() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var FOO = 1; var bar = FOO;");
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node nameNode = root.getFirstChild().getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests VerifyConstants pass when constants are consistently annotated
  @Test
  public void testVerifyConstants_validConstants_passesVerification() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // Tests assertOnChange flag throwing exception on unexpected AST modifications
  @Test(expected = IllegalStateException.class)
  public void testAssertOnChange_unexpectedChange_throwsException() {
    assertOnChange = true;
    test("while (true) {}", "for (; true;) {}");
  }

  // Tests duplicate var declaration shadowing function parameter without initialization
  @Test
  public void testRemoveDuplicateDeclarations_paramShadowUninitialized_removesVar() {
    test("function f(x) { var x; }", "function f(x) {}");
  }

  // Tests duplicate var declaration shadowing function parameter with initialization
  @Test
  public void testRemoveDuplicateDeclarations_paramShadowWithInit_convertsToAssignment() {
    test("function f(x) { var x = 1; }", "function f(x) { x = 1; }");
  }

  // Tests duplicate var declaration shadowing catch block parameter
  @Test
  public void testRemoveDuplicateDeclarations_catchParamShadow_removesVar() {
    test("try {} catch (e) { var e; }", "try {} catch (e) {}");
    test("try {} catch (e) { var e = 1; }", "try {} catch (e) { e = 1; }");
  }

  // Tests duplicate var declaration with existing function name
  @Test
  public void testRemoveDuplicateDeclarations_functionNameShadowWithInit_convertsToAssignment() {
    test("function f() {} var f = 1;", "function f() {} f = 1;");
    test("var f = 1; function f() {}", "function f() {} f = 1;");
  }

  // Tests hoisting multiple function declarations interspersed between statements
  @Test
  public void testMoveNamedFunctions_multipleFunctionsInterspersed_hoistedToTopInOrder() {
    test("function outer() { var a = 1; function f1() {} var b = 2; function f2() {} }",
         "function outer() { function f1() {} function f2() {} var a = 1; var b = 2; }");
  }

  // Tests nested labels wrapping a loop with var initializer extraction
  @Test
  public void testExtractForInitializer_nestedLabels_extractedBeforeOuterLabel() {
    test("l1: l2: for (var i = 0; i < 10; i++) {}",
         "var i = 0; l1: l2: for (; i < 10; i++) {}");
  }

  // Tests constant propagation to references across nested function scopes
  @Test
  public void testPropagateConstantAnnotations_referencesInInnerScope_markedConstant() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var C = 1; function f() { return C; }");
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node constVarName = root.getFirstChild().getFirstChild();
    assertTrue(constVarName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests empty statement under label wrapped in empty block
  @Test
  public void testNormalizeLabels_emptyStatementUnderLabel_wrappedInBlock() {
    test("lab: ;", "lab: {}");
  }
}
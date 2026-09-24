package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/**
 * Unit tests for {@link Normalize}.
 */
public class NormalizeTest extends CompilerTestCase {

  public NormalizeTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new Normalize(compiler, false);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests splitting multiple variable declarations in a single var statement
  @Test
  public void testSplitVarDeclarations_multipleVars_splitsIntoSeparateStatements() {
    test("var a = 1, b = 2, c = 3;", "var a = 1; var b = 2; var c = 3;");
  }

  // Tests splitting multiple uninitialized variable declarations
  @Test
  public void testSplitVarDeclarations_uninitializedVars_splitsIntoSeparateStatements() {
    test("var a, b, c;", "var a; var b; var c;");
  }

  // Tests conversion of while loop to for loop
  @Test
  public void testWhileToFor_simpleWhile_convertsToForLoop() {
    test("while (a < 10) { a++; }", "for (; a < 10;) { a++; }");
  }

  // Tests extraction of for loop var initializer
  @Test
  public void testExtractForInitializer_varInit_movesBeforeForLoop() {
    test("for (var a = 0; a < 10; a++) {}", "var a = 0; for (; a < 10; a++) {}");
  }

  // Tests extraction of for loop expression initializer
  @Test
  public void testExtractForInitializer_exprInit_movesBeforeForLoop() {
    test("for (a = 0; a < 10; a++) {}", "a = 0; for (; a < 10; a++) {}");
  }

  // Tests extraction of for loop initializer inside labeled statement
  @Test
  public void testExtractForInitializer_labeledFor_movesBeforeLabel() {
    test("label: for (var a = 0; a < 10; a++) {}", "var a = 0; label: for (; a < 10; a++) {}");
  }

  // Tests that for-in loops do not have their initializers extracted
  @Test
  public void testExtractForInitializer_forIn_preservesForIn() {
    testSame("for (var a in obj) {}");
  }

  // Tests label normalization wrapping non-block/non-loop statements in blocks
  @Test
  public void testNormalizeLabels_nonBlockBody_wrapsInBlock() {
    test("label: a = 1;", "label: { a = 1; }");
  }

  // Tests duplicate var declaration removal with assignment conversion
  @Test
  public void testRemoveDuplicateDeclarations_withInitializer_convertsToAssign() {
    test("var a = 1; var a = 2;", "var a = 1; a = 2;");
  }

  // Tests duplicate uninitialized var declaration removal
  @Test
  public void testRemoveDuplicateDeclarations_uninitialized_removesDuplicate() {
    test("var a = 1; var a;", "var a = 1;");
  }

  // Tests duplicate var declaration in for-in loop header
  @Test
  public void testRemoveDuplicateDeclarations_forInVar_removesVarKeyword() {
    test("var a; for (var a in obj) {}", "var a; for (a in obj) {}");
  }

  // Tests duplicate var declaration within label
  @Test
  public void testRemoveDuplicateDeclarations_inLabel_replacesWithEmpty() {
    test("var a = 1; label: var a;", "var a = 1; label: ;");
  }

  // Tests handling of duplicate declaration of arguments variable
  @Test
  public void testRemoveDuplicateDeclarations_varArguments_handlesRedeclaration() {
    test("function f() { var arguments = 1; }", "function f() { var arguments = 1; }");
  }

  // Tests moving inner named function declarations to top of enclosing function body
  @Test
  public void testMoveNamedFunctions_nestedFunctionDeclaration_hoistedToTop() {
    test("function f() { var a = 1; function g() {} var b = 2; }",
         "function f() { function g() {} var a = 1; var b = 2; }");
  }

  // Tests that function declarations already at the beginning are kept in order
  @Test
  public void testMoveNamedFunctions_alreadyAtTop_preservesOrder() {
    testSame("function f() { function g() {} function h() {} var a = 1; }");
  }

  // Tests assertOnChange flag throwing IllegalStateException on modification
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChangeTrue_throwsExceptionOnModification() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("while (true) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests VerifyConstants helper pass
  @Test
  public void testVerifyConstants_validConstantAnnotations_passesVerification() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var A = 1;");
    Node rootParent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
    assertNotNull(rootParent);
  }
}
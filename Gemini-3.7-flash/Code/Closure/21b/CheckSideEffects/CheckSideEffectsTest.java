package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;

public class CheckSideEffectsTest extends CompilerTestCase {

  private static final DiagnosticType WARNING = CheckSideEffects.USELESS_CODE_ERROR;

  private boolean protectSideEffects = false;
  private CheckLevel level = CheckLevel.WARNING;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    protectSideEffects = false;
    level = CheckLevel.WARNING;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckSideEffects(compiler, level, protectSideEffects);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests string literal without side effects
  @Test
  public void testVisit_stringLiteral_reportsWarning() {
    test("('str');", WARNING);
  }

  // Tests simple operator result not used
  @Test
  public void testVisit_simpleOperatorUnused_reportsWarning() {
    test("x == foo();", WARNING);
  }

  // Tests side-effect free number literal statement
  @Test
  public void testVisit_uselessCode_reportsWarning() {
    test("1;", WARNING);
  }

  // Tests side-effecting call expression has no warning
  @Test
  public void testVisit_callExpression_noWarning() {
    testSame("foo();");
  }

  // Tests assignment expression has no warning
  @Test
  public void testVisit_assignment_noWarning() {
    testSame("x = 1;");
  }

  // Tests comma operator with useless code
  @Test
  public void testVisit_commaWithUselessCode_reportsWarning() {
    test("1, 2;", WARNING);
  }

  // Tests comma expression in variable declaration
  @Test
  public void testVisit_commaInVarDeclaration_reportsWarning() {
    test("var x = (1, 2);", WARNING);
  }

  // Tests for loop initializer with useless code
  @Test
  public void testVisit_forLoopInitializerWithUselessCode_reportsWarning() {
    test("for (1; ; ) {}", WARNING);
  }

  // Tests for loop increment with useless code
  @Test
  public void testVisit_forLoopIncrementWithUselessCode_reportsWarning() {
    test("for ( ; ; 1) {}", WARNING);
  }

  // Tests for loop with side effects produces no warning
  @Test
  public void testVisit_forLoopWithSideEffects_noWarning() {
    testSame("for (foo(); ; bar()) {}");
  }

  // Tests JSDoc on qualified name produces no warning
  @Test
  public void testVisit_jsdocOnQualifiedName_noWarning() {
    testSame("/** @type {number} */ var x; /** @type {number} */ x.y;");
  }

  // Tests protectSideEffects wraps side-effect free node in JSCOMPILER_PRESERVE
  @Test
  public void testProcess_protectSideEffectsEnabled_wrapsNode() {
    protectSideEffects = true;
    test("x.offsetWidth;", "JSCOMPILER_PRESERVE(x.offsetWidth);", WARNING);
  }

  // Tests StripProtection removes protector call correctly
  @Test
  public void testStripProtection_removesProtectorCall_restoresOriginalNode() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("JSCOMPILER_PRESERVE(x.offsetWidth);");
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    assertEquals("x.offsetWidth", compiler.toSource(root).trim().replace(";", ""));
  }

  // Tests multiple comma items
  @Test
  public void testVisit_nestedComma_reportsWarning() {
    test("a, b, c;", WARNING);
  }

  // Tests hotSwapScript traverses and reports warnings
  @Test
  public void testHotSwapScript_uselessStatement_reportsWarning() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(root, null);
    assertEquals(1, compiler.getWarningCount());
  }
}
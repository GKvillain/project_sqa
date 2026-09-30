package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckSideEffectsTest extends CompilerTestCase {

  private static final DiagnosticType e = CheckSideEffects.USELESS_CODE_ERROR;
  private boolean protectSideEffectFreeCode = true;

  public CheckSideEffectsTest() {
    this.enableEcmaScript5(true);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckSideEffects(compiler, CheckLevel.WARNING, protectSideEffectFreeCode);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.protectSideEffectFreeCode = true;
  }

  // Tests warning and protector injection on unused literal number
  @Test
  public void testVisit_literalNumber_warnsAndProtects() {
    test("1;", "JSCOMPILER_PRESERVE(1);", e);
  }

  // Tests warning on unused string literal suggesting missing plus
  @Test
  public void testVisit_stringLiteral_warnsMissingPlus() {
    test("\"foobar\";", "JSCOMPILER_PRESERVE(\"foobar\");", e);
  }

  // Tests warning on unused result of comparison operator
  @Test
  public void testVisit_comparisonOperator_warnsUnusedResult() {
    test("x == 10;", "JSCOMPILER_PRESERVE(x == 10);", e);
  }

  // Tests that qualified name with JSDoc is ignored and not warned
  @Test
  public void testVisit_jsDocQualifiedName_noWarning() {
    testSame("var x = {}; /** @type {number} */ x.y;");
  }

  // Tests warning on comma expressions where expressions lack side-effects
  @Test
  public void testVisit_commaOperatorUnused_warnsAndProtects() {
    test("1, 2;", "JSCOMPILER_PRESERVE(1); JSCOMPILER_PRESERVE(2);", e);
  }

  // Tests comma expression with assignment where only the unused expression is warned
  @Test
  public void testVisit_commaOperatorAssignment_warnsOnlyUnused() {
    test("x = 10, 20;", "x = 10; JSCOMPILER_PRESERVE(20);", e);
  }

  // Tests nested comma expressions with unused expressions
  @Test
  public void testVisit_nestedComma_warnsNestedUnused() {
    test("x = 10, (20, 30);", "x = 10; JSCOMPILER_PRESERVE(20); JSCOMPILER_PRESERVE(30);", e);
  }

  // Tests for-loop with unused init and increment expressions
  @Test
  public void testVisit_forLoopUnusedInitAndInc_warnsAndProtects() {
    test("for (1; true; 2) {}", "for (JSCOMPILER_PRESERVE(1); true; JSCOMPILER_PRESERVE(2)) {}", e);
  }

  // Tests standard for-loop with side-effecting init and increment
  @Test
  public void testVisit_validForLoop_noWarning() {
    testSame("for (var i = 0; i < 10; i++) { foo(); }");
  }

  // Tests function call with side effects produces no warning
  @Test
  public void testVisit_callWithSideEffects_noWarning() {
    testSame("foo();");
  }

  // Tests stray semicolons producing empty nodes produce no warning
  @Test
  public void testVisit_emptySemicolon_noWarning() {
    testSame(";;");
  }

  // Tests standard var declaration produces no warning
  @Test
  public void testVisit_varDeclaration_noWarning() {
    testSame("var x = 1;");
  }

  // Tests warning generation when code protection is disabled
  @Test
  public void testVisit_protectSideEffectFreeCodeDisabled_warnsWithoutReplacement() {
    this.protectSideEffectFreeCode = false;
    test("1;", "1;", e);
  }

  // Tests StripProtection compiler pass removes JSCOMPILER_PRESERVE calls
  @Test
  public void testStripProtection_protectedNode_removesProtectorCall() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("JSCOMPILER_PRESERVE(1);");
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    assertEquals("1;", compiler.toSource(root).trim());
  }

  // Tests hotSwapScript traverses and reports warnings
  @Test
  public void testHotSwapScript_scriptNode_reportsWarnings() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests warning on unused object literal
  @Test
  public void testVisit_objectLiteral_warnsAndProtects() {
    test("({a: 1});", "JSCOMPILER_PRESERVE({a: 1});", e);
  }

  // Tests warning on unused array literal
  @Test
  public void testVisit_arrayLiteral_warnsAndProtects() {
    test("[1, 2];", "JSCOMPILER_PRESERVE([1, 2]);", e);
  }

  // Tests warning on unused binary operator expression
  @Test
  public void testVisit_binaryOperator_warnsAndProtects() {
    test("x + y;", "JSCOMPILER_PRESERVE(x + y);", e);
  }

  // Tests warning on unused unary operator expression
  @Test
  public void testVisit_unaryOperator_warnsAndProtects() {
    test("!x;", "JSCOMPILER_PRESERVE(!x);", e);
  }

  // Tests warning on unused ternary/hook expression
  @Test
  public void testVisit_hookOperator_warnsAndProtects() {
    test("x ? y : z;", "JSCOMPILER_PRESERVE(x ? y : z);", e);
  }

  // Tests warning on unused function expression
  @Test
  public void testVisit_functionExpression_warnsAndProtects() {
    test("(function() {});", "JSCOMPILER_PRESERVE(function() {});", e);
  }

  // Tests warning on unused property access without JSDoc
  @Test
  public void testVisit_getPropWithoutJSDoc_warnsAndProtects() {
    test("x.y;", "JSCOMPILER_PRESERVE(x.y);", e);
  }

  // Tests warning on unused element access
  @Test
  public void testVisit_getElem_warnsAndProtects() {
    test("x[0];", "JSCOMPILER_PRESERVE(x[0]);", e);
  }

  // Tests warning on unused typeof operator expression
  @Test
  public void testVisit_typeofOperator_warnsAndProtects() {
    test("typeof x;", "JSCOMPILER_PRESERVE(typeof x);", e);
  }

  // Tests warning on unused comma expression inside a for-loop initializer and condition
  @Test
  public void testVisit_commaInForLoop_warnsAndProtects() {
    test("for ((1, 2); true; (3, 4)) {}",
         "for (JSCOMPILER_PRESERVE(1), JSCOMPILER_PRESERVE(2); true; JSCOMPILER_PRESERVE(3), JSCOMPILER_PRESERVE(4)) {}",
         e);
  }

  // Tests StripProtection removes multiple protector calls in a sequence
  @Test
  public void testStripProtection_multipleProtectedNodes_removesAllProtectorCalls() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("JSCOMPILER_PRESERVE(1); JSCOMPILER_PRESERVE(2);");
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    assertEquals("1;2;", compiler.toSource(root).trim().replaceAll("\\s+", ""));
  }
}
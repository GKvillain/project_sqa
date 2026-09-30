package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckSideEffectsTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private void test(String js, int expectedWarnings) {
    Node root = compiler.parseTestCode(js);
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    assertEquals("Warnings count mismatch for: " + js, expectedWarnings, compiler.getWarningCount());
  }

  // Tests string literal with no side-effects (missing '+')
  @Test
  public void testVisit_stringLiteralNoSideEffect_reportsWarning() {
    test("\"this is a string\";", 1);
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR.key, compiler.getWarnings()[0].getType().key);
    assertTrue(compiler.getWarnings()[0].description.contains("missing '+'"));
  }

  // Tests simple operator result unused (e.g., comparison ==)
  @Test
  public void testVisit_simpleOperatorUnused_reportsWarning() {
    test("var x = 1; x == 2;", 1);
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR.key, compiler.getWarnings()[0].getType().key);
    assertTrue(compiler.getWarnings()[0].description.contains("operator is not being used"));
  }

  // Tests empty statement (stray semicolon) which should be ignored
  @Test
  public void testVisit_emptyStatement_noWarning() {
    test(";;", 0);
  }

  // Tests valid function call with potential side effects
  @Test
  public void testVisit_callWithSideEffects_noWarning() {
    test("function foo() {} foo();", 0);
  }

  // Tests valid variable declaration and assignment
  @Test
  public void testVisit_variableDeclaration_noWarning() {
    test("var x = 10; var y = x + 1;", 0);
  }

  // Tests indirect eval call pattern (1, eval)("code") which should not warn on 1
  @Test
  public void testVisit_indirectEvalPattern_noWarning() {
    test("(1, eval)(\"alert(1)\");", 0);
  }

  // Tests comma operator with useless sub-expression
  @Test
  public void testVisit_commaExpressionWithUselessCode_reportsWarning() {
    test("var x = (1, 2);", 1);
  }

  // Tests comma operator containing multiple useless expressions
  @Test
  public void testVisit_commaMultipleUseless_reportsWarnings() {
    test("(1, 2, 3);", 3);
  }

  // Tests useless expression in for loop initializer and update
  @Test
  public void testVisit_forLoopUselessInitAndUpdate_reportsWarnings() {
    test("for (1; true; 2) { break; }", 2);
  }

  // Tests normal for loop with variable declaration and increment
  @Test
  public void testVisit_forLoopNormal_noWarning() {
    test("for (var i = 0; i < 10; i++) {}", 0);
  }

  // Tests qualified name node with JSDoc attached (should not warn)
  @Test
  public void testVisit_qualifiedNameWithJSDoc_noWarning() {
    Node root = IR.script(IR.exprResult(IR.name("x")));
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordType(new com.google.javascript.rhino.JSTypeExpression(
        new com.google.javascript.rhino.Node(com.google.javascript.rhino.Token.STAR), "test"));
    JSDocInfo info = builder.build(root.getFirstChild().getFirstChild());
    root.getFirstChild().getFirstChild().setJSDocInfo(info);

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests hotSwapScript method behavior
  @Test
  public void testHotSwapScript_uselessCode_reportsWarning() {
    Node root = compiler.parseTestCode("1 + 1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests StripProtection removes JSCOMPILER_PRESERVE calls
  @Test
  public void testStripProtection_removesProtectorCall() {
    Node callTarget = IR.name(CheckSideEffects.PROTECTOR_FN);
    Node arg = IR.number(42);
    Node call = IR.call(callTarget, arg);
    Node exprResult = IR.exprResult(call);
    Node root = IR.script(exprResult);

    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);

    assertEquals(1, root.getChildCount());
    assertEquals(arg, root.getFirstChild().getFirstChild());
  }

  // Tests visit with null parent
  @Test
  public void testVisit_nullParent_noWarning() {
    Node standaloneNode = IR.number(123);
    NodeTraversal t = new NodeTraversal(compiler, new CheckSideEffects(compiler, CheckLevel.WARNING, false));
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.visit(t, standaloneNode, null);
    assertEquals(0, compiler.getWarningCount());
  }
}
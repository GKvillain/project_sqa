package com.google.javascript.jscomp;

import org.junit.Test;

public class RemoveConstantExpressionsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RemoveConstantExpressions(compiler);
  }

  // Tests removal of a simple number literal expression statement
  @Test
  public void testProcess_numberLiteral_removed() {
    test("1;", "");
  }

  // Tests removal of a string literal expression statement
  @Test
  public void testProcess_stringLiteral_removed() {
    test("'hello';", "");
  }

  // Tests removal of a boolean literal expression statement
  @Test
  public void testProcess_booleanLiteral_removed() {
    test("true;", "");
  }

  // Tests removal of a pure binary constant expression
  @Test
  public void testProcess_constantBinaryOp_removed() {
    test("1 + 2 * 3;", "");
  }

  // Tests extraction of side-effect call from binary expression
  @Test
  public void testProcess_binaryWithSingleCall_extractsCall() {
    test("1 + foo();", "foo();");
  }

  // Tests extraction of multiple side-effect calls from binary expression
  @Test
  public void testProcess_binaryWithMultipleCalls_extractsCalls() {
    test("1 + foo() + bar();", "foo(); bar();");
  }

  // Tests call node directly as expression statement is retained
  @Test
  public void testProcess_callExpression_retained() {
    testSame("foo();");
  }

  // Tests new operator call expression is retained
  @Test
  public void testProcess_newExpression_retained() {
    testSame("new Foo();");
  }

  // Tests assignment expressions which have side effects are retained
  @Test
  public void testProcess_assignmentExpression_retained() {
    testSame("x = 1;");
  }

  // Tests increment and decrement expressions are retained
  @Test
  public void testProcess_incDecExpression_retained() {
    testSame("x++;");
    testSame("++x;");
    testSame("x--;");
    testSame("--x;");
  }

  // Tests pure array and object literals without side effects are removed
  @Test
  public void testProcess_pureLiterals_removed() {
    test("[1, 2, 3];", "");
    test("({a: 1, b: 2});", "");
  }

  // Tests array literal containing side-effect call extracts the call
  @Test
  public void testProcess_arrayWithSideEffect_extractsCall() {
    test("[1, foo(), 2];", "foo();");
  }

  // Tests object literal containing side-effect call extracts the call
  @Test
  public void testProcess_objectWithSideEffect_extractsCall() {
    test("({a: foo(), b: 2});", "foo();");
  }

  // Tests comma operator with pure constants is completely removed
  @Test
  public void testProcess_commaOperatorPureConstants_removed() {
    test("1, 2, 3;", "");
  }

  // Tests comma operator containing calls extracts only the calls
  @Test
  public void testProcess_commaOperatorWithCalls_extractsCalls() {
    test("1, foo(), 2, bar();", "foo(); bar();");
  }

  // Tests hook (ternary) operator containing calls extracts side effects
  @Test
  public void testProcess_hookOperatorWithCalls_extractsCalls() {
    test("1 ? foo() : bar();", "foo(); bar();");
  }

  // Tests variable declarations are ignored and retained
  @Test
  public void testProcess_varDeclaration_retained() {
    testSame("var x = 1 + 2;");
  }

  // Tests function declarations are ignored and retained
  @Test
  public void testProcess_functionDeclaration_retained() {
    testSame("function f() { return 1; }");
  }
}
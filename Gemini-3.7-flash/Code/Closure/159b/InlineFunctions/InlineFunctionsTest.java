package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link InlineFunctions}.
 */
public class InlineFunctionsTest extends CompilerTestCase {

  private boolean inlineGlobalFunctions = true;
  private boolean inlineLocalFunctions = true;
  private boolean blockFunctionInliningEnabled = true;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.inlineGlobalFunctions = true;
    this.inlineLocalFunctions = true;
    this.blockFunctionInliningEnabled = true;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineFunctions(
        compiler,
        compiler.getUniqueNameIdSupplier(),
        inlineGlobalFunctions,
        inlineLocalFunctions,
        blockFunctionInliningEnabled);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests constructor null check for compiler
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_nullCompiler_throwsException() {
    new InlineFunctions(null, new Compiler().getUniqueNameIdSupplier(), true, true, true);
  }

  // Tests constructor null check for supplier
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_nullSupplier_throwsException() {
    new InlineFunctions(new Compiler(), null, true, true, true);
  }

  // Tests direct inlining of simple return statement
  @Test
  public void testInlineDirect_simpleFunction_inlinesReturnValue() {
    test("function foo() { return 4; } var x = foo();",
         "var x = 4;");
  }

  // Tests direct inlining with parameter passing
  @Test
  public void testInlineDirect_withParameters_inlinesParameters() {
    test("function foo(a, b) { return a + b; } var x = foo(1, 2);",
         "var x = 1 + 2;");
  }

  // Tests function expression assigned to var
  @Test
  public void testInlineFunctionVar_directReplacement_inlinesCall() {
    test("var foo = function() { return 1; }; var x = foo();",
         "var x = 1;");
  }

  // Tests immediately invoked function expression (IIFE)
  @Test
  public void testInlineFunctionExpression_iife_inlinesBody() {
    test("(function(a) { return a + 1; })(5);",
         "5 + 1;");
  }

  // Tests function call using .call() syntax
  @Test
  public void testInlineFunction_callMethod_inlinesCall() {
    test("function foo(a) { return a; } var x = foo.call(this, 10);",
         "var x = 10;");
  }

  // Tests block inlining when function has multiple statements
  @Test
  public void testInlineBlock_multipleStatements_inlinesBlock() {
    test("function foo(a) { var b = a + 1; return b; } var x = foo(2);",
         "var x; { var b$$inline_0 = 2 + 1; x = b$$inline_0; }");
  }

  // Tests block inlining disabled
  @Test
  public void testInlineBlock_disabledBlockInlining_doesNotInlined() {
    this.blockFunctionInliningEnabled = false;
    testSame("function foo(a) { var b = a + 1; return b; } var x = foo(2);");
  }

  // Tests global functions inlining disabled
  @Test
  public void testInlineGlobal_disabled_doesNotInlinedGlobalFunction() {
    this.inlineGlobalFunctions = false;
    testSame("function foo() { return 1; } var x = foo();");
  }

  // Tests local functions inlining disabled
  @Test
  public void testInlineLocal_disabled_doesNotInlinedLocalFunction() {
    this.inlineLocalFunctions = false;
    test("function outer() { function inner() { return 1; } return inner(); }",
         "function outer() { function inner() { return 1; } return inner(); }");
  }

  // Tests recursive function is not inlined
  @Test
  public void testInline_recursiveFunction_notInlined() {
    testSame("function f(x) { return x <= 0 ? 0 : f(x - 1); } f(5);");
  }

  // Tests function referenced as a value is not removed
  @Test
  public void testInline_referencedAsValue_notRemoved() {
    test("function foo() { return 1; } var alias = foo; var x = foo();",
         "function foo() { return 1; } var alias = foo; var x = 1;");
  }

  // Tests functions called via .call inside nested functions (Defects4J Closure 159 regression)
  @Test
  public void testIssueBug159_calledViaCallInsideFunction_correctlyIdentified() {
    test("function initialize() { " +
         "  var f = function () {};" +
         "  (function () { f.call(this); })();" +
         "}",
         "function initialize() { " +
         "  var f = function () {};" +
         "  { f.call(this); }" +
         "}");
  }

  // Tests isCandidateUsage for standard AST nodes
  @Test
  public void testIsCandidateUsage_nameInCall_returnsTrue() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    Node callNode = new Node(Token.CALL, nameNode);
    assertTrue(InlineFunctions.isCandidateUsage(nameNode));
  }

  // Tests isCandidateUsage for non-candidate nodes
  @Test
  public void testIsCandidateUsage_nameInReturn_returnsFalse() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    Node returnNode = new Node(Token.RETURN, nameNode);
    assertFalse(InlineFunctions.isCandidateUsage(nameNode));
  }
}
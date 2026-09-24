package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

/**
 * Unit tests for {@link FunctionRewriter}.
 */
public class FunctionRewriterTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new FunctionRewriter(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests empty function reduction when savings exceed threshold
  public void testEmptyFunctionReduction_multipleFunctions_rewritesToCall() {
    String helper = "function JSCompiler_emptyFn() { return function() {} }";
    String input = "a.p1 = function() {};"
        + "a.p2 = function() {};"
        + "a.p3 = function() {};"
        + "a.p4 = function() {};"
        + "a.p5 = function() {};";
    String expected = helper
        + "a.p1 = JSCompiler_emptyFn();"
        + "a.p2 = JSCompiler_emptyFn();"
        + "a.p3 = JSCompiler_emptyFn();"
        + "a.p4 = JSCompiler_emptyFn();"
        + "a.p5 = JSCompiler_emptyFn();";
    test(input, expected);
  }

  // Tests identity function reduction when savings exceed threshold
  public void testIdentityReduction_multipleFunctions_rewritesToCall() {
    String helper = "function JSCompiler_identityFn() {"
        + "  return function(JSCompiler_identityFn_value) {"
        + "    return JSCompiler_identityFn_value"
        + "  }"
        + "}";
    String input = "a.p1 = function(x) { return x; };"
        + "a.p2 = function(y) { return y; };"
        + "a.p3 = function(z) { return z; };"
        + "a.p4 = function(w) { return w; };"
        + "a.p5 = function(v) { return v; };";
    String expected = helper
        + "a.p1 = JSCompiler_identityFn();"
        + "a.p2 = JSCompiler_identityFn();"
        + "a.p3 = JSCompiler_identityFn();"
        + "a.p4 = JSCompiler_identityFn();"
        + "a.p5 = JSCompiler_identityFn();";
    test(input, expected);
  }

  // Tests return constant function reduction when savings exceed threshold
  public void testReturnConstantReduction_multipleFunctions_rewritesToCall() {
    String helper = "function JSCompiler_returnArg(JSCompiler_returnArg_value) {"
        + "  return function() { return JSCompiler_returnArg_value }"
        + "}";
    String input = "a.p1 = function() { return 10; };"
        + "a.p2 = function() { return 10; };"
        + "a.p3 = function() { return 10; };"
        + "a.p4 = function() { return 10; };"
        + "a.p5 = function() { return 10; };";
    String expected = helper
        + "a.p1 = JSCompiler_returnArg(10);"
        + "a.p2 = JSCompiler_returnArg(10);"
        + "a.p3 = JSCompiler_returnArg(10);"
        + "a.p4 = JSCompiler_returnArg(10);"
        + "a.p5 = JSCompiler_returnArg(10);";
    test(input, expected);
  }

  // Tests getter reduction when savings exceed threshold
  public void testGetterReduction_multipleFunctions_rewritesToCall() {
    String helper = "function JSCompiler_get(JSCompiler_get_name) {"
        + "  return function() { return this[JSCompiler_get_name] }"
        + "}";
    String input = "a.p1 = function() { return this.foo; };"
        + "a.p2 = function() { return this.foo; };"
        + "a.p3 = function() { return this.foo; };"
        + "a.p4 = function() { return this.foo; };"
        + "a.p5 = function() { return this.foo; };";
    String expected = helper
        + "a.p1 = JSCompiler_get(\"foo\");"
        + "a.p2 = JSCompiler_get(\"foo\");"
        + "a.p3 = JSCompiler_get(\"foo\");"
        + "a.p4 = JSCompiler_get(\"foo\");"
        + "a.p5 = JSCompiler_get(\"foo\");";
    test(input, expected);
  }

  // Tests setter reduction when savings exceed threshold
  public void testSetterReduction_multipleFunctions_rewritesToCall() {
    String helper = "function JSCompiler_set(JSCompiler_set_name) {"
        + "  return function(JSCompiler_set_value) {"
        + "    this[JSCompiler_set_name] = JSCompiler_set_value"
        + "  }"
        + "}";
    String input = "a.p1 = function(v) { this.foo = v; };"
        + "a.p2 = function(v) { this.foo = v; };"
        + "a.p3 = function(v) { this.foo = v; };"
        + "a.p4 = function(v) { this.foo = v; };"
        + "a.p5 = function(v) { this.foo = v; };";
    String expected = helper
        + "a.p1 = JSCompiler_set(\"foo\");"
        + "a.p2 = JSCompiler_set(\"foo\");"
        + "a.p3 = JSCompiler_set(\"foo\");"
        + "a.p4 = JSCompiler_set(\"foo\");"
        + "a.p5 = JSCompiler_set(\"foo\");";
    test(input, expected);
  }

  // Tests that reduction is not applied when savings do not exceed threshold
  public void testReduction_singleFunction_notReducedDueToCost() {
    testSame("a.p1 = function() {};");
    testSame("a.p1 = function() { return 10; };");
    testSame("a.p1 = function() { return this.foo; };");
    testSame("a.p1 = function(v) { this.foo = v; };");
    testSame("a.p1 = function(x) { return x; };");
  }

  // Tests function declarations are not reduced
  public void testFunctionDeclaration_notReduced() {
    testSame("function f1() {} function f2() {} function f3() {} function f4() {} function f5() {}");
  }

  // Tests functions with multiple statements are not reduced
  public void testMultiStatementFunction_notReduced() {
    testSame("a.p1 = function() { var x = 1; return x; };"
        + "a.p2 = function() { var x = 1; return x; };"
        + "a.p3 = function() { var x = 1; return x; };"
        + "a.p4 = function() { var x = 1; return x; };"
        + "a.p5 = function() { var x = 1; return x; };");
  }

  // Tests setter with mismatched parameter and assign value is not reduced
  public void testSetterMismatchParam_notReduced() {
    testSame("a.p1 = function(v) { this.foo = other; };"
        + "a.p2 = function(v) { this.foo = other; };"
        + "a.p3 = function(v) { this.foo = other; };"
        + "a.p4 = function(v) { this.foo = other; };"
        + "a.p5 = function(v) { this.foo = other; };");
  }

  // Tests getter without 'this' property access is not reduced
  public void testGetterNotThisProperty_notReduced() {
    testSame("a.p1 = function() { return other.foo; };"
        + "a.p2 = function() { return other.foo; };"
        + "a.p3 = function() { return other.foo; };"
        + "a.p4 = function() { return other.foo; };"
        + "a.p5 = function() { return other.foo; };");
  }

  // Tests identity function with parameter mismatch is not reduced
  public void testIdentityMismatchParam_notReduced() {
    testSame("a.p1 = function(x) { return y; };"
        + "a.p2 = function(x) { return y; };"
        + "a.p3 = function(x) { return y; };"
        + "a.p4 = function(x) { return y; };"
        + "a.p5 = function(x) { return y; };");
  }

  // Tests object literal getters and setters (Defects4J Closure 55 regression test)
  public void testObjectLiteralGetterSetter_notCorrupted() {
    testSame("var a = { get foo() { return 1; } };");
    testSame("var a = { set foo(v) { this.x = v; } };");
    testSame("a = function() { return { get foo() { return 1; } }; };");
  }

  // Tests empty program
  public void testEmptyProgram_doesNotFail() {
    testSame("");
  }
}
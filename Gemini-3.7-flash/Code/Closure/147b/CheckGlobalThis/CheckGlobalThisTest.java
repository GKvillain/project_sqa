package com.google.javascript.jscomp;

import org.junit.Test;

public class CheckGlobalThisTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CombinedCompilerPass(
        compiler, new CheckGlobalThis(compiler, CheckLevel.WARNING));
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private void testSame(String js) {
    test(js, js);
  }

  private void testFailure(String js) {
    test(js, CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this access at top level script
  @Test
  public void testShouldTraverse_topLevelThisPropertyAccess_reportsWarning() {
    testFailure("this.a = 1;");
    testFailure("this['a'] = 1;");
    testFailure("var x = this.a;");
  }

  // Tests global this inside a normal function declaration
  @Test
  public void testShouldTraverse_normalFunction_reportsWarning() {
    testFailure("function f() { this.a = 1; }");
    testFailure("function f() { return this.a; }");
  }

  // Tests global this inside a function expression assigned to a variable
  @Test
  public void testShouldTraverse_functionExpressionInVar_reportsWarning() {
    testFailure("var f = function() { this.a = 1; };");
    testFailure("var f = function() { return this.a; };");
  }

  // Tests constructor function with @constructor annotation (declaration and expression)
  @Test
  public void testShouldTraverse_constructorFunction_noWarning() {
    testSame("/** @constructor */ function C() { this.a = 1; }");
    testSame("/** @constructor */ var C = function() { this.a = 1; };");
    testSame("var C; /** @constructor */ C = function() { this.a = 1; };");
  }

  // Tests function with @this annotation
  @Test
  public void testShouldTraverse_functionWithThisAnnotation_noWarning() {
    testSame("/** @this {Object} */ function f() { this.a = 1; }");
    testSame("/** @this {Object} */ var f = function() { this.a = 1; };");
    testSame("var f; /** @this {Object} */ f = function() { this.a = 1; };");
  }

  // Tests interface function with @interface annotation
  @Test
  public void testShouldTraverse_interfaceFunction_noWarning() {
    testSame("/** @interface */ function I() { this.a; }");
    testSame("/** @interface */ var I = function() { this.a; };");
  }

  // Tests function with @override annotation
  @Test
  public void testShouldTraverse_overrideFunction_noWarning() {
    testSame("/** @override */ function f() { this.a = 1; }");
    testSame("/** @override */ var f = function() { this.a = 1; };");
  }

  // Tests assignment to prototype method
  @Test
  public void testShouldTraverse_prototypeMethodAssignment_noWarning() {
    testSame("C.prototype.m = function() { this.a = 1; };");
    testSame("C.prototype.m = function() { return this.a; };");
    testSame("C.prototype.m.sub = function() { this.a = 1; };");
  }

  // Tests assignment to prototype property directly
  @Test
  public void testShouldTraverse_prototypePropertyAssignment_noWarning() {
    testSame("C.prototype = { m: function() { this.a = 1; } };");
  }

  // Tests function in an object literal not attached to prototype
  @Test
  public void testShouldTraverse_objectLiteralFunction_reportsWarning() {
    testFailure("var o = { m: function() { this.a = 1; } };");
    testFailure("o = { m: function() { this.a = 1; } };");
  }

  // Tests assigning this to a variable without immediate property access
  @Test
  public void testVisit_assignThisToVariable_noWarning() {
    testSame("var a = this;");
    testSame("function f() { var a = this; a.x = 1; }");
  }

  // Tests nested assignment where this is on the LHS
  @Test
  public void testVisit_nestedLhsAssign_reportsWarning() {
    testFailure("(a = this).property = 1;");
  }

  // Tests nested functions where inner function uses this illegally
  @Test
  public void testShouldTraverse_nestedFunction_reportsWarning() {
    testFailure("/** @constructor */ function C() { function inner() { this.a = 1; } }");
  }

  // Tests nested functions where inner function has valid @this
  @Test
  public void testShouldTraverse_nestedFunctionWithAnnotation_noWarning() {
    testSame("function f() { /** @this {Object} */ function inner() { this.a = 1; } }");
  }
}
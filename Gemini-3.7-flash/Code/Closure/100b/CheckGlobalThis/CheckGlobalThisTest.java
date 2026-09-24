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

  // Tests global this in a simple assignment
  @Test
  public void testShouldReportThis_globalThisAssignment_reportsWarning() {
    test("this.a = 1;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this in property read access
  @Test
  public void testShouldReportThis_globalThisPropertyRead_reportsWarning() {
    test("var x = this.foo;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this in function call argument
  @Test
  public void testShouldReportThis_globalThisInFunctionCall_reportsWarning() {
    test("alert(this.foo);", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests bare global this without property access or assignment
  @Test
  public void testShouldReportThis_bareThis_noWarning() {
    testSame("var a = this;");
  }

  // Tests constructor function with @constructor JSDoc annotation
  @Test
  public void testShouldTraverse_constructorFunction_noWarning() {
    testSame("/** @constructor */ function Foo() { this.m = 1; }");
  }

  // Tests function with @this JSDoc annotation
  @Test
  public void testShouldTraverse_functionWithThisAnnotation_noWarning() {
    testSame("/** @this {Foo} */ function f() { this.m = 1; }");
  }

  // Tests function assigned to a prototype property
  @Test
  public void testShouldTraverse_prototypeMethodAssignment_noWarning() {
    testSame("Foo.prototype.bar = function() { this.m = 1; };");
  }

  // Tests function assigned to a prototype subproperty
  @Test
  public void testShouldTraverse_prototypeSubpropertyAssignment_noWarning() {
    testSame("Foo.prototype.bar.baz = function() { this.m = 1; };");
  }

  // Tests assignment to a prototype object directly
  @Test
  public void testShouldTraverse_assignToPrototype_noWarning() {
    testSame("Foo.prototype = { bar: function() { this.m = 1; } };");
  }

  // Tests unannotated function declaration using this
  @Test
  public void testShouldReportThis_unannotatedFunction_reportsWarning() {
    test("function f() { this.m = 1; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests unannotated function expression assigned to variable using this
  @Test
  public void testShouldReportThis_unannotatedFunctionExpression_reportsWarning() {
    test("var f = function() { this.m = 1; };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests nested assignment with this on left-hand side
  @Test
  public void testShouldReportThis_nestedAssignmentLhs_reportsWarning() {
    test("(a = this).property = 1;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests constructor assignment with var JSDoc
  @Test
  public void testShouldTraverse_varJSDocConstructor_noWarning() {
    testSame("/** @constructor */ var Foo = function() { this.m = 1; };");
  }

  // Tests constructor assignment to namespace property
  @Test
  public void testShouldTraverse_namespaceConstructor_noWarning() {
    testSame("var ns = {}; /** @constructor */ ns.Foo = function() { this.m = 1; };");
  }
}
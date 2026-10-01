package com.google.javascript.jscomp;

import org.junit.Test;

public class CheckGlobalThisTest extends CompilerTestCase {

  private CheckLevel level = CheckLevel.ERROR;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CombinedCompilerPass(
        compiler, new CheckGlobalThis(compiler, level));
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private void testFailure(String js) {
    test(js, CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests dangerous global this property read
  @Test
  public void testShouldReportThis_globalPropertyRead_reportsError() {
    testFailure("var x = this.foo;");
  }

  // Tests dangerous global this property assignment on left-hand side
  @Test
  public void testShouldReportThis_globalPropertyAssignment_reportsError() {
    testFailure("this.foo = 5;");
  }

  // Tests global this in element access
  @Test
  public void testShouldReportThis_globalElementAccess_reportsError() {
    testFailure("this['foo'] = 5;");
  }

  // Tests constructor function suppresses check
  @Test
  public void testShouldTraverse_constructorFunction_noError() {
    testSame("/** @constructor */ function F() { this.foo = 5; }");
  }

  // Tests function with @this annotation suppresses check
  @Test
  public void testShouldTraverse_functionWithThisAnnotation_noError() {
    testSame("/** @this {F} */ function f() { this.foo = 5; }");
  }

  // Tests prototype property function assignment suppresses check
  @Test
  public void testShouldTraverse_prototypeMethodAssign_noError() {
    testSame("F.prototype.bar = function() { this.foo = 5; };");
  }

  // Tests prototype subproperty assignment suppresses check
  @Test
  public void testShouldTraverse_prototypeSubpropertyAssign_noError() {
    testSame("F.prototype.bar.baz = function() { this.foo = 5; };");
  }

  // Tests assignment to prototype object suppresses check
  @Test
  public void testShouldTraverse_prototypeObjectAssign_noError() {
    testSame("F.prototype = { bar: function() { this.foo = 5; } };");
  }

  // Tests unannotated regular function reports dangerous this usage
  @Test
  public void testShouldTraverse_unannotatedFunction_reportsError() {
    testFailure("function f() { this.foo = 5; }");
  }

  // Tests unannotated function variable assignment reports dangerous this usage
  @Test
  public void testShouldTraverse_varFunctionWithoutJsDoc_reportsError() {
    testFailure("var f = function() { this.foo = 5; };");
  }

  // Tests constructor variable assignment suppresses check
  @Test
  public void testShouldTraverse_varConstructorFunction_noError() {
    testSame("/** @constructor */ var F = function() { this.foo = 5; };");
  }

  // Tests variable assignment with @this annotation suppresses check
  @Test
  public void testShouldTraverse_varFunctionWithThisAnnotation_noError() {
    testSame("/** @this {F} */ var f = function() { this.foo = 5; };");
  }

  // Tests non-prototype property assignment function reports dangerous this usage
  @Test
  public void testShouldTraverse_nonPrototypeMethodAssign_reportsError() {
    testFailure("a.b.c = function() { this.x = 1; };");
  }

  // Tests nested assignment with this on LHS
  @Test
  public void testShouldTraverse_nestedAssignLhs_reportsError() {
    testFailure("(a = this).property = 5;");
  }

  // Tests interface function suppresses check
  @Test
  public void testShouldTraverse_interfaceFunction_noError() {
    testSame("/** @interface */ function F() { this.foo = 5; }");
  }

  // Tests interface variable assignment suppresses check
  @Test
  public void testShouldTraverse_varInterfaceFunction_noError() {
    testSame("/** @interface */ var F = function() { this.foo = 5; };");
  }

  // Tests inner function in constructor reports dangerous this usage
  @Test
  public void testShouldTraverse_innerFunctionInConstructor_reportsError() {
    testFailure("/** @constructor */ function F() { function inner() { this.foo = 5; } }");
  }

  // Tests inner function in prototype method reports dangerous this usage
  @Test
  public void testShouldTraverse_innerFunctionInPrototypeMethod_reportsError() {
    testFailure("F.prototype.bar = function() { function inner() { this.foo = 5; } };");
  }

  // Tests prototype property assignment via bracket notation suppresses check
  @Test
  public void testShouldTraverse_prototypeMethodAssignBracketNotation_noError() {
    testSame("F.prototype['bar'] = function() { this.foo = 5; };");
  }

  // Tests prototype object string key assignment suppresses check
  @Test
  public void testShouldTraverse_prototypeObjectAssignStringKey_noError() {
    testSame("F.prototype = { 'bar': function() { this.foo = 5; } };");
  }

  // Tests getter in prototype object suppresses check
  @Test
  public void testShouldTraverse_prototypeObjectGetter_noError() {
    testSame("F.prototype = { get bar() { return this.foo; } };");
  }

  // Tests setter in prototype object suppresses check
  @Test
  public void testShouldTraverse_prototypeObjectSetter_noError() {
    testSame("F.prototype = { set bar(val) { this.foo = val; } };");
  }

  // Tests object literal with @lends annotation suppresses check
  @Test
  public void testShouldTraverse_lendsAnnotation_noError() {
    testSame("var F = define(/** @lends {F.prototype} */ { bar: function() { this.foo = 5; } });");
  }

  // Tests global this passed as function argument
  @Test
  public void testShouldReportThis_globalFunctionCallArgument_reportsError() {
    testFailure("foo(this);");
  }

  // Tests return this in regular function reports dangerous this usage
  @Test
  public void testShouldReportThis_returnThisInFunction_reportsError() {
    testFailure("function f() { return this; }");
  }

  // Tests this in conditional expression
  @Test
  public void testShouldReportThis_conditionalExpression_reportsError() {
    testFailure("if (this.foo) {}");
  }

  // Tests this in callback function reports dangerous this usage
  @Test
  public void testShouldTraverse_callbackFunction_reportsError() {
    testFailure("setTimeout(function() { this.foo = 5; }, 0);");
  }

  // Tests static method on constructor reports dangerous this usage without annotation
  @Test
  public void testShouldTraverse_staticMethodAssignWithoutAnnotation_reportsError() {
    testFailure("/** @constructor */ function F() {} F.bar = function() { this.foo = 5; };");
  }

  // Tests IIFE with dangerous this usage reports error
  @Test
  public void testShouldTraverse_iife_reportsError() {
    testFailure("(function() { this.foo = 5; })();");
  }

  // Tests unannotated method in object literal reports error
  @Test
  public void testShouldTraverse_objectLiteralMethod_reportsError() {
    testFailure("var obj = { foo: function() { this.x = 1; } };");
  }
}
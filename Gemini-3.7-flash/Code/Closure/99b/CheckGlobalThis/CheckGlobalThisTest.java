package com.google.javascript.jscomp;

public class CheckGlobalThisTest extends CompilerTestCase {

  private CheckLevel checkLevel = CheckLevel.WARNING;

  public CheckGlobalThisTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CombinedCompilerPass(
        compiler, new CheckGlobalThis(compiler, checkLevel));
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests global this in script level assignment
  public void testVisit_globalThisAssignment_reportsWarning() {
    test("this.foo = 5;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this property access in plain function
  public void testVisit_functionGlobalThis_reportsWarning() {
    test("function f() { this.foo = 5; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this in var assigned function
  public void testVisit_varFunctionGlobalThis_reportsWarning() {
    test("var f = function() { this.foo = 5; };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this in property assigned function
  public void testVisit_propertyFunctionGlobalThis_reportsWarning() {
    test("a.b = function() { this.foo = 5; };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests parentheses around this on assignment LHS
  public void testVisit_parenthesizedThis_reportsWarning() {
    test("function f() { (this).foo = 5; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests nested assignment containing this on LHS
  public void testVisit_nestedAssignLhsThis_reportsWarning() {
    test("function f() { (a = this).foo = 5; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function with @constructor annotation
  public void testShouldTraverse_constructorFunction_noWarning() {
    testSame("/** @constructor */ function F() { this.foo = 5; }");
  }

  // Tests var function with @constructor annotation on var
  public void testShouldTraverse_constructorVar_noWarning() {
    testSame("/** @constructor */ var F = function() { this.foo = 5; };");
  }

  // Tests assignment with @constructor annotation
  public void testShouldTraverse_constructorAssign_noWarning() {
    testSame("/** @constructor */ f = function() { this.foo = 5; };");
  }

  // Tests function with @this annotation
  public void testShouldTraverse_thisAnnotation_noWarning() {
    testSame("/** @this {F} */ function f() { this.foo = 5; }");
  }

  // Tests var function with @this annotation
  public void testShouldTraverse_thisAnnotationVar_noWarning() {
    testSame("/** @this {F} */ var f = function() { this.foo = 5; };");
  }

  // Tests function with @override annotation
  public void testShouldTraverse_overrideAnnotation_noWarning() {
    testSame("/** @override */ function f() { this.foo = 5; }");
  }

  // Tests assignment to prototype property
  public void testShouldTraverse_prototypeMethod_noWarning() {
    testSame("Foo.prototype.bar = function() { this.foo = 5; };");
  }

  // Tests assignment to prototype subproperty
  public void testShouldTraverse_prototypeSubproperty_noWarning() {
    testSame("Foo.prototype.bar.baz = function() { this.foo = 5; };");
  }

  // Tests assignment directly to prototype
  public void testShouldTraverse_prototypeAssignment_noWarning() {
    testSame("Foo.prototype = function() { this.foo = 5; };");
  }

  // Tests function in object literal
  public void testShouldTraverse_functionInObjectLiteral_noWarning() {
    testSame("var a = { b: function() { this.foo = 5; } };");
  }

  // Tests function in array literal
  public void testShouldTraverse_functionInArrayLiteral_noWarning() {
    test("var a = [function() { this.foo = 5; }];", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests inner function inside outer function
  public void testVisit_innerFunction_reportsWarning() {
    test("function f() { function g() { this.foo = 5; } }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function with @interface annotation
  public void testShouldTraverse_interfaceFunction_noWarning() {
    testSame("/** @interface */ function F() { this.foo = 5; }");
  }

  // Tests function with @record annotation
  public void testShouldTraverse_recordFunction_noWarning() {
    testSame("/** @record */ function F() { this.foo = 5; }");
  }

  // Tests function with @struct annotation
  public void testShouldTraverse_structFunction_noWarning() {
    testSame("/** @struct */ function F() { this.foo = 5; }");
  }

  // Tests read-only access to global this property
  public void testVisit_readGlobalThis_noWarning() {
    testSame("var x = this.foo;");
  }

  // Tests global this element access assignment
  public void testVisit_elementAssignment_reportsWarning() {
    test("this['foo'] = 5;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this element access assignment in function
  public void testVisit_functionElementAssignment_reportsWarning() {
    test("function f() { this['foo'] = 5; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests object literal prototype assignment
  public void testShouldTraverse_prototypeObjectLiteral_noWarning() {
    testSame("Foo.prototype = { bar: function() { this.foo = 5; } };");
  }

  // Tests object literal with string and number keys
  public void testShouldTraverse_objectLiteralKeyTypes_noWarning() {
    testSame("var a = { 'b': function() { this.foo = 5; }, 1: function() { this.foo = 5; } };");
  }

  // Tests IIFE containing global this assignment
  public void testVisit_iife_reportsWarning() {
    test("(function() { this.foo = 5; })();", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests assignment with @this annotation
  public void testShouldTraverse_thisAnnotationAssign_noWarning() {
    testSame("/** @this {F} */ f = function() { this.foo = 5; };");
  }

  // Tests var assignment with @override annotation
  public void testShouldTraverse_overrideAnnotationVar_noWarning() {
    testSame("/** @override */ var f = function() { this.foo = 5; };");
  }
}
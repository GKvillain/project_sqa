package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CheckGlobalThisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private void test(String js, int expectedWarnings) {
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal.traverse(compiler, root, callback);
    assertEquals(expectedWarnings, compiler.getWarningCount());
  }

  // Tests top-level assignment to property on this
  @Test
  public void testGlobalThis_topLevelAssignment_reportsWarning() {
    test("this.a = 1;", 1);
  }

  // Tests top-level property access on this
  @Test
  public void testGlobalThis_topLevelPropertyAccess_reportsWarning() {
    test("this.a;", 1);
  }

  // Tests top-level bracket property access on this
  @Test
  public void testGlobalThis_topLevelBracketAccess_reportsWarning() {
    test("this['a'];", 1);
  }

  // Tests assigning this to a variable without property access
  @Test
  public void testGlobalThis_assignToVariable_noWarning() {
    test("var a = this;", 0);
  }

  // Tests global function using this
  @Test
  public void testFunction_globalFunctionWithThis_reportsWarning() {
    test("function f() { this.a = 1; }", 1);
  }

  // Tests function expression in var assignment using this
  @Test
  public void testFunction_varFunctionWithThis_reportsWarning() {
    test("var f = function() { this.a = 1; };", 1);
  }

  // Tests function expression in property assignment using this
  @Test
  public void testFunction_assignFunctionWithThis_reportsWarning() {
    test("x.f = function() { this.a = 1; };", 1);
  }

  // Tests function annotated with @constructor
  @Test
  public void testFunction_constructorAnnotation_noWarning() {
    test("/** @constructor */ function F() { this.a = 1; }", 0);
  }

  // Tests var function annotated with @constructor on var declaration
  @Test
  public void testFunction_varConstructorAnnotation_noWarning() {
    test("/** @constructor */ var F = function() { this.a = 1; };", 0);
  }

  // Tests function annotated with @this
  @Test
  public void testFunction_thisAnnotation_noWarning() {
    test("/** @this {Object} */ function f() { this.a = 1; }", 0);
  }

  // Tests function annotated with @override
  @Test
  public void testFunction_overrideAnnotation_noWarning() {
    test("/** @override */ function f() { this.a = 1; }", 0);
  }

  // Tests method assigned to a prototype property
  @Test
  public void testFunction_prototypeMethod_noWarning() {
    test("F.prototype.bar = function() { this.a = 1; };", 0);
  }

  // Tests subproperty of prototype assignment
  @Test
  public void testFunction_subPrototypeMethod_noWarning() {
    test("a.b.prototype.c = function() { this.a = 1; };", 0);
  }

  // Tests prototype object assignment
  @Test
  public void testFunction_prototypeAssignment_noWarning() {
    test("F.prototype = { bar: function() { this.a = 1; } };", 0);
  }

  // Tests method defined in an object literal
  @Test
  public void testFunction_objectLiteralMethod_noWarning() {
    test("var obj = { f: function() { this.a = 1; } };", 0);
  }

  // Tests CheckLevel.ERROR configuration reporting errors instead of warnings
  @Test
  public void testVisit_errorLevel_reportsError() {
    Node root = compiler.parseTestCode("this.a = 1;");
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);
    NodeTraversal.traverse(compiler, root, callback);
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests function annotated with @interface
  @Test
  public void testFunction_interfaceAnnotation_noWarning() {
    test("/** @interface */ function I() { this.a = 1; }", 0);
  }

  // Tests var declaration annotated with @interface
  @Test
  public void testFunction_varInterfaceAnnotation_noWarning() {
    test("/** @interface */ var I = function() { this.a = 1; };", 0);
  }

  // Tests anonymous function expression directly annotated with @constructor
  @Test
  public void testFunction_functionExpressionConstructorAnnotation_noWarning() {
    test("var F = /** @constructor */ function() { this.a = 1; };", 0);
  }

  // Tests function expression passed as an argument using this
  @Test
  public void testFunction_callArgumentFunctionWithThis_reportsWarning() {
    test("call(function() { this.a = 1; });", 1);
  }

  // Tests inner function inside constructor using this
  @Test
  public void testFunction_innerFunctionInsideConstructor_reportsWarning() {
    test("/** @constructor */ function F() { function inner() { this.a = 1; } }", 1);
  }

  // Tests getter/setter in object literal
  @Test
  public void testFunction_getterSetterInObjectLiteral_noWarning() {
    test("var obj = { get a() { return this.x; }, set a(val) { this.x = val; } };", 0);
  }
}
package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

public class CheckGlobalThisTest extends CompilerTestCase {

  private CheckLevel level = CheckLevel.WARNING;

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        NodeTraversal.traverse(compiler, root, new CheckGlobalThis(compiler, level));
      }
    };
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests global this property assignment at script level
  @Test
  public void testGlobalThis_topLevelAssign_reportsWarning() {
    test("this.foo = 1;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this property access at script level
  @Test
  public void testGlobalThis_topLevelPropertyAccess_reportsWarning() {
    test("var x = this.foo;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests this keyword inside unannotated global function
  @Test
  public void testGlobalThis_unannotatedFunction_reportsWarning() {
    test("function foo() { this.a = 1; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests constructor annotation skips warning
  @Test
  public void testGlobalThis_constructorAnnotation_noWarning() {
    testSame("/** @constructor */ function Foo() { this.a = 1; }");
  }

  // Tests interface annotation skips warning
  @Test
  public void testGlobalThis_interfaceAnnotation_noWarning() {
    testSame("/** @interface */ function Foo() { this.a = 1; }");
  }

  // Tests @this annotation skips warning
  @Test
  public void testGlobalThis_thisAnnotation_noWarning() {
    testSame("/** @this {Foo} */ function foo() { this.a = 1; }");
  }

  // Tests @override annotation skips warning
  @Test
  public void testGlobalThis_overrideAnnotation_noWarning() {
    testSame("/** @override */ function foo() { this.a = 1; }");
  }

  // Tests assignment to prototype property skips warning
  @Test
  public void testGlobalThis_prototypeMethodAssignment_noWarning() {
    testSame("Foo.prototype.bar = function() { this.a = 1; };");
  }

  // Tests assignment to prototype subproperty skips warning
  @Test
  public void testGlobalThis_prototypeSubPropertyAssignment_noWarning() {
    testSame("Foo.prototype.bar.baz = function() { this.a = 1; };");
  }

  // Tests assignment of object literal to prototype
  @Test
  public void testGlobalThis_prototypeObjectLiteralAssignment_noWarning() {
    testSame("Foo.prototype = { bar: function() { this.a = 1; } };");
  }

  // Tests var statement with constructor JSDoc
  @Test
  public void testGlobalThis_varConstructorJsDoc_noWarning() {
    testSame("/** @constructor */ var Foo = function() { this.a = 1; };");
  }

  // Tests assignment with constructor JSDoc
  @Test
  public void testGlobalThis_assignConstructorJsDoc_noWarning() {
    testSame("/** @constructor */ Foo = function() { this.a = 1; };");
  }

  // Tests nested assignment on LHS of assign
  @Test
  public void testGlobalThis_nestedAssignLhs_reportsWarning() {
    test("(this.a = 1).b = 2;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests anonymous function in call argument where this cannot be annotated
  @Test
  public void testGlobalThis_functionInCallArgument_noWarning() {
    testSame("foo(function() { this.a = 1; });");
  }

  // Tests function property in object literal without annotation
  @Test
  public void testGlobalThis_objectLiteralMethod_reportsWarning() {
    test("var o = { k: function() { this.a = 1; } };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests number key in object literal
  @Test
  public void testGlobalThis_objectLiteralNumberKey_reportsWarning() {
    test("var o = { 1: function() { this.a = 1; } };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests safe code without this usage
  @Test
  public void testGlobalThis_noThisUsage_noWarning() {
    testSame("var a = 1; function foo() { var b = 2; }");
  }

  // Tests CheckLevel.OFF disables traversal and reporting
  @Test
  public void testGlobalThis_levelOff_noWarning() {
    level = CheckLevel.OFF;
    testSame("this.foo = 1;");
  }

  // Tests CheckLevel.ERROR reports error
  @Test
  public void testGlobalThis_levelError_reportsError() {
    level = CheckLevel.ERROR;
    test("this.foo = 1;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests GETELEM property access on global this
  @Test
  public void testGlobalThis_getElem_reportsWarning() {
    test("this['foo'] = 1;", CheckGlobalThis.GLOBAL_THIS);
    test("var x = this['foo'];", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests @record annotation skips warning
  @Test
  public void testGlobalThis_recordAnnotation_noWarning() {
    testSame("/** @record */ function Foo() { this.a = 1; }");
  }

  // Tests @lends annotation skips warning
  @Test
  public void testGlobalThis_lendsAnnotation_noWarning() {
    testSame("var o = /** @lends {Foo.prototype} */ ({ bar: function() { this.a = 1; } });");
  }

  // Tests @this annotation on object literal method skips warning
  @Test
  public void testGlobalThis_objectLiteralWithThisAnnotation_noWarning() {
    testSame("var o = { k: /** @this {Foo} */ function() { this.a = 1; } };");
  }

  // Tests GETELEM prototype assignment skips warning
  @Test
  public void testGlobalThis_prototypeGetElemAssignment_noWarning() {
    testSame("Foo.prototype['bar'] = function() { this.a = 1; };");
  }

  // Tests unannotated nested function inside constructor reports warning
  @Test
  public void testGlobalThis_nestedFunctionInConstructor_reportsWarning() {
    test("/** @constructor */ function Foo() { function inner() { this.a = 1; } }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests unannotated namespaced function reports warning
  @Test
  public void testGlobalThis_namespacedFunction_reportsWarning() {
    test("goog.bar = function() { this.a = 1; };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests standalone this without GETPROP or GETELEM does not report warning
  @Test
  public void testGlobalThis_standaloneThis_noWarning() {
    testSame("var x = this;");
    testSame("function foo() { return this; }");
  }

  // Tests ES6 class methods do not report warning
  @Test
  public void testGlobalThis_es6Class_noWarning() {
    testSame("class Foo { constructor() { this.a = 1; } bar() { this.b = 2; } }");
  }
}
package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link CheckGlobalThis}.
 */
public class CheckGlobalThisTest extends CompilerTestCase {

  private CheckLevel level = CheckLevel.WARNING;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CombinedCompilerPass(
        compiler, new CheckGlobalThis(compiler, level));
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests global this property assignment triggers warning
  @Test
  public void testShouldTraverse_globalThisPropertyAssignment_reportsWarning() {
    test("this.foo = 5;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this property read triggers warning
  @Test
  public void testShouldTraverse_globalThisPropertyRead_reportsWarning() {
    test("var a = this.foo;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this bracket property access triggers warning
  @Test
  public void testShouldTraverse_globalThisGetElem_reportsWarning() {
    test("var a = this['foo'];", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests global this assignment in nested expression triggers warning
  @Test
  public void testShouldTraverse_nestedAssignLhs_reportsWarning() {
    test("(this.foo = 1).bar = 2;", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function without JSDoc referencing this triggers warning
  @Test
  public void testShouldTraverse_functionDeclarationUsingThis_reportsWarning() {
    test("function foo() { this.bar = 5; }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function expression assigned to variable referencing this triggers warning
  @Test
  public void testShouldTraverse_functionExpressionUsingThis_reportsWarning() {
    test("var foo = function() { this.bar = 5; };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests constructor function does not trigger warning
  @Test
  public void testShouldTraverse_constructorFunction_noWarning() {
    testSame("/** @constructor */ function Foo() { this.bar = 5; }");
  }

  // Tests constructor annotation on var declaration does not trigger warning
  @Test
  public void testShouldTraverse_varConstructorFunction_noWarning() {
    testSame("/** @constructor */ var Foo = function() { this.bar = 5; };");
  }

  // Tests constructor annotation on assign does not trigger warning
  @Test
  public void testShouldTraverse_assignConstructorFunction_noWarning() {
    testSame("var ns = {}; /** @constructor */ ns.Foo = function() { this.bar = 5; };");
  }

  // Tests interface annotation does not trigger warning
  @Test
  public void testShouldTraverse_interfaceFunction_noWarning() {
    testSame("/** @interface */ function Foo() { this.bar = 5; }");
  }

  // Tests this annotation does not trigger warning
  @Test
  public void testShouldTraverse_thisAnnotation_noWarning() {
    testSame("/** @this {Foo} */ function foo() { this.bar = 5; }");
  }

  // Tests override annotation does not trigger warning
  @Test
  public void testShouldTraverse_overrideAnnotation_noWarning() {
    testSame("/** @override */ function foo() { this.bar = 5; }");
  }

  // Tests prototype method assignment does not trigger warning
  @Test
  public void testShouldTraverse_prototypeMethodAssign_noWarning() {
    testSame("Foo.prototype.bar = function() { this.baz = 5; };");
  }

  // Tests prototype subproperty method assignment does not trigger warning
  @Test
  public void testShouldTraverse_prototypeSubpropertyMethodAssign_noWarning() {
    testSame("Foo.prototype.bar.baz = function() { this.qux = 5; };");
  }

  // Tests prototype object literal assignment does not trigger warning
  @Test
  public void testShouldTraverse_prototypeObjectLiteral_noWarning() {
    testSame("Foo.prototype = { bar: function() { this.baz = 5; } };");
  }

  // Tests inner function inside constructor triggers warning
  @Test
  public void testShouldTraverse_innerFunctionInsideConstructor_reportsWarning() {
    test("/** @constructor */ function Foo() { function bar() { this.x = 1; } }", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function in object literal inside non-prototype assignment triggers warning
  @Test
  public void testShouldTraverse_nonPrototypeObjectLiteral_reportsWarning() {
    test("var a = { foo: function() { this.bar = 5; } };", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests function expression in call argument without constructor annotation triggers warning
  @Test
  public void testShouldTraverse_callArgumentFunctionUsingThis_reportsWarning() {
    test("baz(function() { this.bar = 5; });", CheckGlobalThis.GLOBAL_THIS);
  }

  // Tests standalone this without property access does not trigger warning
  @Test
  public void testShouldTraverse_standaloneThis_noWarning() {
    testSame("var a = this;");
  }

  @Test
  public void testShouldTraverse_globalThisBracketAssignment_reportsWarning() {
    test("this['foo'] = 5;", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_globalThisUnaryIncDec_reportsWarning() {
    test("this.foo++;", CheckGlobalThis.GLOBAL_THIS);
    test("--this.foo;", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_globalThisDelete_reportsWarning() {
    test("delete this.foo;", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_namespaceFunctionAssign_reportsWarning() {
    test("goog.foo = function() { this.bar = 5; };", CheckGlobalThis.GLOBAL_THIS);
    test("goog.foo.bar = function() { this.baz = 5; };", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_nestedObjectLiteralFunction_reportsWarning() {
    test("var a = { b: { c: function() { this.foo = 5; } } };", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_objectLiteralGetterSetter_reportsWarning() {
    test("var a = { get foo() { return this.bar; } };", CheckGlobalThis.GLOBAL_THIS);
    test("var a = { set foo(val) { this.bar = val; } };", CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testShouldTraverse_prototypeGetterSetter_noWarning() {
    testSame("Foo.prototype = { get bar() { return this.baz; }, set bar(val) { this.baz = val; } };");
  }

  @Test
  public void testShouldTraverse_prototypeBracketAccess_noWarning() {
    testSame("Foo.prototype['bar'] = function() { this.baz = 5; };");
    testSame("Foo.prototype['bar']['baz'] = function() { this.qux = 5; };");
  }

  @Test
  public void testShouldTraverse_suppressGlobalThis_noWarning() {
    testSame("/** @suppress {globalThis} */ function foo() { this.bar = 5; }");
    testSame("/** @suppress {globalThis} */ var a = this.foo;");
  }

  @Test
  public void testShouldTraverse_lendsAnnotation_noWarning() {
    testSame("var makeObj = function(x) { return x; }; "
        + "makeObj(/** @lends {Foo.prototype} */ ({ bar: function() { this.baz = 5; } }));");
  }

  @Test
  public void testShouldTraverse_innerFunctionInsideMethod_reportsWarning() {
    test("Foo.prototype.bar = function() { function inner() { this.x = 1; } };", CheckGlobalThis.GLOBAL_THIS);
  }
}
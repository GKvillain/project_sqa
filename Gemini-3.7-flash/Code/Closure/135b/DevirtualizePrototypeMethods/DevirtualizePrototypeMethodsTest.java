package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link DevirtualizePrototypeMethods}.
 */
public class DevirtualizePrototypeMethodsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DevirtualizePrototypeMethods(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests rewriting a simple prototype method and its call site
  @Test
  public void testProcess_simplePrototypeMethod_rewritesToStaticCall() {
    test(
        "function A() {} A.prototype.foo = function() { return this.x; }; (new A()).foo();",
        "function A() {} var JSCompiler_StaticMethods_foo = "
            + "function(JSCompiler_StaticMethods_foo$self) { return JSCompiler_StaticMethods_foo$self.x; }; "
            + "JSCompiler_StaticMethods_foo(new A());");
  }

  // Tests that prototype methods with parameters prepend self parameter
  @Test
  public void testProcess_methodWithParameters_rewritesWithSelfFirst() {
    test(
        "function A() {} A.prototype.add = function(a, b) { return this.x + a + b; }; (new A()).add(1, 2);",
        "function A() {} var JSCompiler_StaticMethods_add = "
            + "function(JSCompiler_StaticMethods_add$self, a, b) { return JSCompiler_StaticMethods_add$self.x + a + b; }; "
            + "JSCompiler_StaticMethods_add(new A(), 1, 2);");
  }

  // Tests rewriting multiple calls to the same eligible method
  @Test
  public void testProcess_multipleCallSites_rewritesAllCallSites() {
    test(
        "function A() {} A.prototype.foo = function() { return this.x; }; var a = new A(); a.foo(); a.foo();",
        "function A() {} var JSCompiler_StaticMethods_foo = "
            + "function(JSCompiler_StaticMethods_foo$self) { return JSCompiler_StaticMethods_foo$self.x; }; "
            + "var a = new A(); JSCompiler_StaticMethods_foo(a); JSCompiler_StaticMethods_foo(a);");
  }

  // Tests rewriting a method that does not reference this
  @Test
  public void testProcess_methodWithoutThis_rewritesCorrectly() {
    test(
        "function A() {} A.prototype.getZero = function() { return 0; }; (new A()).getZero();",
        "function A() {} var JSCompiler_StaticMethods_getZero = "
            + "function(JSCompiler_StaticMethods_getZero$self) { return 0; }; "
            + "JSCompiler_StaticMethods_getZero(new A());");
  }

  // Tests that references to this inside nested functions are not replaced
  @Test
  public void testProcess_nestedFunctionThis_preservesInnerThis() {
    test(
        "function A() {} A.prototype.foo = function() { var self = this; return function() { return this.x; }; }; (new A()).foo();",
        "function A() {} var JSCompiler_StaticMethods_foo = "
            + "function(JSCompiler_StaticMethods_foo$self) { var self = JSCompiler_StaticMethods_foo$self; return function() { return this.x; }; }; "
            + "JSCompiler_StaticMethods_foo(new A());");
  }

  // Tests that methods accessing arguments are not eligible
  @Test
  public void testProcess_methodAccessingArguments_doesNotRewrite() {
    testSame("function A() {} A.prototype.foo = function() { return arguments[0]; }; (new A()).foo(1);");
  }

  // Tests that unused prototype methods are not rewritten
  @Test
  public void testProcess_unusedPrototypeMethod_doesNotRewrite() {
    testSame("function A() {} A.prototype.foo = function() { return this.x; };");
  }

  // Tests that property access outside a direct call expression prevents rewrite
  @Test
  public void testProcess_propertyAccessOutsideCall_doesNotRewrite() {
    testSame("function A() {} A.prototype.foo = function() { return this.x; }; var a = new A(); var f = a.foo;");
  }

  // Tests that multiple definitions for the same method name prevent rewrite
  @Test
  public void testProcess_multipleDefinitionsSameName_doesNotRewrite() {
    testSame(
        "function A() {} A.prototype.foo = function() { return this.x; }; "
            + "function B() {} B.prototype.foo = function() { return this.y; }; "
            + "(new A()).foo(); (new B()).foo();");
  }

  // Tests that definition inside a control structure is not rewritten
  @Test
  public void testProcess_definitionInsideControlStructure_doesNotRewrite() {
    testSame("function A() {} if (true) { A.prototype.foo = function() { return this.x; }; } (new A()).foo();");
  }

  // Tests that non-prototype method assignments are not rewritten
  @Test
  public void testProcess_staticMethodOnConstructor_doesNotRewrite() {
    testSame("function A() {} A.foo = function() { return this.x; }; A.foo();");
  }

  // Tests that assignment expression not being an ExprAssign statement is not rewritten
  @Test
  public void testProcess_assignmentInExpression_doesNotRewrite() {
    testSame("function A() {} var x = (A.prototype.foo = function() { return this.x; }); (new A()).foo();");
  }

  // Tests rewriting multiple distinct prototype methods on the same constructor
  @Test
  public void testProcess_multipleMethodsOnSamePrototype_rewritesBoth() {
    test(
        "function A() {} A.prototype.foo = function() { return this.x; }; "
            + "A.prototype.bar = function() { return this.y; }; var a = new A(); a.foo(); a.bar();",
        "function A() {} var JSCompiler_StaticMethods_foo = "
            + "function(JSCompiler_StaticMethods_foo$self) { return JSCompiler_StaticMethods_foo$self.x; }; "
            + "var JSCompiler_StaticMethods_bar = "
            + "function(JSCompiler_StaticMethods_bar$self) { return JSCompiler_StaticMethods_bar$self.y; }; "
            + "var a = new A(); JSCompiler_StaticMethods_foo(a); JSCompiler_StaticMethods_bar(a);");
  }

  // Tests rewriting a method returning 'this'
  @Test
  public void testProcess_methodReturningThis_rewritesCorrectly() {
    test(
        "function A() {} A.prototype.getSelf = function() { return this; }; (new A()).getSelf();",
        "function A() {} var JSCompiler_StaticMethods_getSelf = "
            + "function(JSCompiler_StaticMethods_getSelf$self) { return JSCompiler_StaticMethods_getSelf$self; }; "
            + "JSCompiler_StaticMethods_getSelf(new A());");
  }

  // Tests rewriting a method that modifies a property on 'this'
  @Test
  public void testProcess_methodModifyingThisProperty_rewritesCorrectly() {
    test(
        "function A() {} A.prototype.setX = function(val) { this.x = val; }; var a = new A(); a.setX(10);",
        "function A() {} var JSCompiler_StaticMethods_setX = "
            + "function(JSCompiler_StaticMethods_setX$self, val) { JSCompiler_StaticMethods_setX$self.x = val; }; "
            + "var a = new A(); JSCompiler_StaticMethods_setX(a, 10);");
  }

  // Tests that bracket access syntax prevents rewriting
  @Test
  public void testProcess_bracketAccess_doesNotRewrite() {
    testSame("function A() {} A.prototype['foo'] = function() { return this.x; }; (new A())['foo']();");
  }

  // Tests that non-function prototype property assignments are not rewritten
  @Test
  public void testProcess_nonFunctionPrototypeAssignment_doesNotRewrite() {
    testSame("function A() {} A.prototype.foo = 123; var a = new A();");
  }

  // Tests that prototype assigned from a variable reference instead of a function literal is not rewritten
  @Test
  public void testProcess_prototypeAssignedFromVariable_doesNotRewrite() {
    testSame("function A() {} var f = function() { return this.x; }; A.prototype.foo = f; (new A()).foo();");
  }

  // Tests that method calls via .call() or .apply() prevent rewriting
  @Test
  public void testProcess_methodCallViaCallOrApply_doesNotRewrite() {
    testSame("function A() {} A.prototype.foo = function() { return this.x; }; var a = new A(); a.foo.call(a);");
  }
}
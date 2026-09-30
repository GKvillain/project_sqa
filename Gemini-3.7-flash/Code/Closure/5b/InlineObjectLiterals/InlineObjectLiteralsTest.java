package com.google.javascript.jscomp;

import org.junit.Test;

public class InlineObjectLiteralsTest extends CompilerTestCase {

  public InlineObjectLiteralsTest() {
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineObjectLiterals(
        compiler,
        compiler.getUniqueNameIdSupplier());
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic inlining of a local object literal with a single property
  @Test
  public void testProcess_simpleObject_inlinesProperties() {
    test(
        "function f() { var a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining of an object literal with multiple properties
  @Test
  public void testProcess_multipleProperties_inlinesProperties() {
    test(
        "function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; "
            + "return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests inlining when an object literal is reassigned
  @Test
  public void testProcess_objectReassignment_inlinesAssignments() {
    test(
        "function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; "
            + "JSCompiler_object_inline_x_0 = 2, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining with property assignment after declaration
  @Test
  public void testProcess_propertyAssignment_inlinesProperty() {
    test(
        "function f() { var a = {x: 1}; a.x = 2; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; "
            + "JSCompiler_object_inline_x_0 = 2; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining of an uninitialized variable assigned an object literal later
  @Test
  public void testProcess_uninitializedVar_inlinesCorrectly() {
    test(
        "function f() { var a; a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0; "
            + "JSCompiler_object_inline_x_0 = 1, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests that global object literals are not inlined
  @Test
  public void testProcess_globalScope_doesNotInline() {
    testSame("var a = {x: 1}; a.x;");
  }

  // Tests that an object literal passed to a function is not inlined
  @Test
  public void testProcess_escapedObject_doesNotInline() {
    testSame("function f() { var a = {x: 1}; g(a); }");
  }

  // Tests that method calls on an object literal prevent inlining due to 'this' context
  @Test
  public void testProcess_methodCall_doesNotInline() {
    testSame("function f() { var a = {x: function() {}}; a.x(); }");
  }

  // Tests that deleting a property on an object prevents inlining (Defects4J Closure-5)
  @Test
  public void testProcess_deletedProperty_doesNotInline() {
    testSame("function f() { var foo = {bar: 1}; delete foo.bar; return foo.bar; }");
  }

  // Tests that self-referential object literals are not inlined
  @Test
  public void testProcess_selfReferential_doesNotInline() {
    testSame("function f() { var x = {a: 1, b: x.a}; return x.b; }");
  }

  // Tests that ES5 getters prevent inlining
  @Test
  public void testProcess_getter_doesNotInline() {
    testSame("function f() { var x = { get a() { return 1; } }; return x.a; }");
  }

  // Tests that ES5 setters prevent inlining
  @Test
  public void testProcess_setter_doesNotInline() {
    testSame("function f() { var x = { set a(val) { } }; x.a = 1; }");
  }

  // Tests that reading a property not defined in the literal prevents inlining
  @Test
  public void testProcess_undeclaredPropertyRead_doesNotInline() {
    testSame("function f() { var a = {x: 1}; return a.y; }");
  }
}
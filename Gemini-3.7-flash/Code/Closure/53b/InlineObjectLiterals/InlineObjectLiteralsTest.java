package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link InlineObjectLiterals}.
 */
public class InlineObjectLiteralsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineObjectLiterals(
        compiler,
        compiler.getUniqueNameIdSupplier());
  }

  // Tests defect 53: empty object assignment without properties causing index out of bounds
  @Test
  public void testProcess_emptyObjectAssignment_inlinesOrReplaces() {
    test("function f() { var a = {}; a = {}; }",
         "function f() { true; }");
  }

  // Tests simple object literal with one property
  @Test
  public void testProcess_simpleObjectLiteral_inlinesObject() {
    test("function f() { var a = {x: 1}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  // Tests object literal with multiple properties
  @Test
  public void testProcess_multipleProperties_inlinesObject() {
    test("function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests reassignment of object literal properties
  @Test
  public void testProcess_reassignment_inlinesObject() {
    test("function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests object literal initialized without value then assigned
  @Test
  public void testProcess_uninitializedVarAssigned_inlinesObject() {
    test("function f() { var a; a = {x: 1}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0; JSCompiler_object_inline_x_0 = 1, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests partial property reassignment where missing properties become undefined
  @Test
  public void testProcess_partialPropertyReassignment_setsUndefined() {
    test("function f() { var a = {x: 1, y: 2}; a = {x: 3}; return a.x + a.y; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; JSCompiler_object_inline_x_0 = 3, JSCompiler_object_inline_y_1 = void 0, true; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests global variable which should be forbidden from inlining
  @Test
  public void testProcess_globalVariable_doesNotInlined() {
    testSame("var a = {x: 1}; a.x;");
  }

  // Tests direct object reference passed to function which prevents inlining
  @Test
  public void testProcess_directObjectEscape_doesNotInlined() {
    testSame("function f() { var a = {x: 1}; g(a); return a.x; }");
  }

  // Tests method call on property which uses object as 'this' context
  @Test
  public void testProcess_methodCallOnProperty_doesNotInlined() {
    testSame("function f() { var a = {x: function() {}}; a.x(); }");
  }

  // Tests self-referential assignment which forbids inlining
  @Test
  public void testProcess_selfReferentialObject_doesNotInlined() {
    testSame("function f() { var a = {x: 1, y: a.x}; return a.x; }");
  }

  // Tests ES5 getter/setter which is not supported for inlining
  @Test
  public void testProcess_es5GetterSetter_doesNotInlined() {
    testSame("function f() { var a = { get x() { return 1; } }; return a.x; }");
  }

  // Tests object with property access that was not in the initial literal
  @Test
  public void testProcess_accessUndeclaredKey_inlinesObject() {
    test("function f() { var a = {x: 1}; return a.y; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1; return JSCompiler_object_inline_y_1; }");
  }

  // Tests non-object literal assignment forbidding inlining
  @Test
  public void testProcess_nonObjectAssignment_doesNotInlined() {
    testSame("function f() { var a = {x: 1}; a = 2; return a.x; }");
  }
}
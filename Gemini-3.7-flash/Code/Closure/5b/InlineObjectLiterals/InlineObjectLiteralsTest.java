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

  // Tests simple object literal inlining with single property
  @Test
  public void testProcess_simpleObjectLiteral_inlinesProperties() {
    test(
        "function f() { var a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  // Tests object literal with multiple properties inlined into separate variables
  @Test
  public void testProcess_multipleProperties_inlinesAllVariables() {
    test(
        "function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests object assignment after declaration without initial value
  @Test
  public void testProcess_uninitializedVarThenAssign_inlinesVariables() {
    test(
        "function f() { var a; a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0; JSCompiler_object_inline_x_0 = 1, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests reassignment of object literal with different property values
  @Test
  public void testProcess_reassignedObjectLiteral_inlinesMultipleAssignments() {
    test(
        "function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests that object passed as argument directly is not inlined
  @Test
  public void testProcess_escapedObjectReference_doesNotInline() {
    testSame("function f() { var a = {x: 1}; g(a); return a.x; }");
  }

  // Tests that method invocation on object property prevents inlining due to potential 'this' binding
  @Test
  public void testProcess_methodCallOnProperty_doesNotInline() {
    testSame("function f() { var a = {x: function() { return 1; }}; a.x(); }");
  }

  // Tests ES5 getter property disallows inlining
  @Test
  public void testProcess_objectWithGetter_doesNotInline() {
    testSame("function f() { var a = { get x() { return 1; } }; return a.x; }");
  }

  // Tests ES5 setter property disallows inlining
  @Test
  public void testProcess_objectWithSetter_doesNotInline() {
    testSame("function f() { var a = { set x(val) { } }; a.x = 1; }");
  }

  // Tests self-referential property assignment prevents inlining
  @Test
  public void testProcess_selfReferentialAssignment_doesNotInline() {
    testSame("function f() { var a = {x: 1, y: a.x}; return a.y; }");
  }

  // Tests access to undeclared property on the object literal prevents inlining
  @Test
  public void testProcess_undeclaredPropertyAccess_doesNotInline() {
    testSame("function f() { var a = {x: 1}; return a.y; }");
  }

  // Tests property deletion prevents inlining
  @Test
  public void testProcess_deleteProperty_doesNotInline() {
    testSame("function f() { var a = {x: 1}; delete a.x; }");
  }

  // Tests global variable is not inlined
  @Test
  public void testProcess_globalScopeObject_doesNotInline() {
    testSame("var a = {x: 1}; a.x;");
  }

  // Tests empty object literal inlining
  @Test
  public void testProcess_emptyObjectLiteral_inlinesSuccessfully() {
    test(
        "function f() { var a = {}; }",
        "function f() { }");
  }

  // Tests simple property assignment on inlined object
  @Test
  public void testProcess_propertyAssignment_inlinesVariableAssignment() {
    test(
        "function f() { var a = {x: 1}; a.x = 2; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2; return JSCompiler_object_inline_x_0; }");
  }

  // Tests object literal with missing property in later reassignment
  @Test
  public void testProcess_reassignmentMissingProperty_setsUndefined() {
    test(
        "function f() { var a = {x: 1, y: 2}; a = {x: 3}; return a.x + a.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; JSCompiler_object_inline_x_0 = 3, JSCompiler_object_inline_y_1 = void 0, true; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }
}
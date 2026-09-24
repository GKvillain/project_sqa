package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link InlineObjectLiterals}.
 */
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

  // Tests that global object literals are not inlined
  @Test
  public void testProcess_globalVariable_notInlined() {
    testSame("var a = {x: 1}; a.x;");
  }

  // Tests that escaped object references (passed to functions) are not inlined
  @Test
  public void testProcess_escapedObjectReference_notInlined() {
    testSame("function f() { var a = {x: 1}; g(a); }");
  }

  // Tests that method calls where the object is the 'this' context are not inlined
  @Test
  public void testProcess_methodCallTarget_notInlined() {
    testSame("function f() { var a = {m: function() { return this; }}; a.m(); }");
  }

  // Tests that self-referential assignments are not inlined
  @Test
  public void testProcess_selfReferentialAssignment_notInlined() {
    testSame("function f() { var a = {x: 1}; a = {x: a.x}; return a.x; }");
  }

  // Tests that ES5 getters and setters are not inlined
  @Test
  public void testProcess_getterSetterObject_notInlined() {
    testSame("function f() { var a = { get x() { return 1; } }; return a.x; }");
    testSame("function f() { var a = { set x(val) { } }; a.x = 1; }");
  }

  // Tests inlining a simple object literal with one property
  @Test
  public void testProcess_simpleObjectLiteral_inlined() {
    test("function f() { var a = {x: 1}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining an object literal with multiple properties
  @Test
  public void testProcess_multipleProperties_inlined() {
    test("function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests inlining an uninitialized variable followed by an assignment
  @Test
  public void testProcess_uninitializedVarThenAssigned_inlined() {
    test("function f() { var a; a = {x: 1}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0; JSCompiler_object_inline_x_0 = 1, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining an object with reassignment
  @Test
  public void testProcess_reassignment_inlined() {
    test("function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining an empty object literal assignment (Defects4J Closure-53 regression test)
  @Test
  public void testProcess_emptyObjectAssignment_inlined() {
    test("function f() { var a = {x: 1}; a = {}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = void 0, true; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining an object with multiple keys reassigned to empty object
  @Test
  public void testProcess_multipleKeysReassignedToEmpty_inlined() {
    test("function f() { var a = {x: 1, y: 2}; a = {}; return a.x + a.y; }",
         "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; JSCompiler_object_inline_x_0 = void 0, JSCompiler_object_inline_y_1 = void 0, true; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  // Tests inlining when property is accessed without initial definition in object
  @Test
  public void testProcess_propertyReadWithoutInitialValue_inlined() {
    test("function f() { var a = {}; return a.x; }",
         "function f() { var JSCompiler_object_inline_x_0; return JSCompiler_object_inline_x_0; }");
  }

  // Tests inlining when non-object assignment prevents inlining
  @Test
  public void testProcess_nonObjectAssignment_notInlined() {
    testSame("function f() { var a = {x: 1}; a = 5; return a.x; }");
  }
}
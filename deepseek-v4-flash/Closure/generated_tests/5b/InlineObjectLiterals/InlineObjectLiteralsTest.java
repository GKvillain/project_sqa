package com.google.javascript.jscomp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class InlineObjectLiteralsTest {

  private Compiler compiler;
  private AtomicInteger counter;

  @Before
  public void setUp() {
    compiler = new Compiler();
    counter = new AtomicInteger(0);
  }

  private Node parse(String code) {
    return compiler.parseTestCode(code);
  }

  private void process(Node root) {
    Node externs = new Node(Token.SCRIPT);
    supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(counter.getAndIncrement());
      }
    };
    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, supplier);
    pass.process(externs, root);
  }

  private Supplier<String> supplier;

  private String toSource(Node root) {
    return compiler.toSource(root);
  }

  // Normal case: single object literal, property access inline
  @Test
  public void testInlineObjectLiteral_simplePropertyAccess_inlines() {
    Node root = parse("var x = {a: 1}; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("JSCompiler_object_inline_a_0"));
    assertTrue(src.contains("alert(JSCompiler_object_inline_a_0)"));
    assertFalse(src.contains("var x"));
  }

  // Multiple properties are all split and inlined
  @Test
  public void testInlineObjectLiteral_multipleProperties_inlines() {
    Node root = parse("var x = {a: 1, b: 2}; alert(x.a); alert(x.b);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("JSCompiler_object_inline_a_0"));
    assertTrue(src.contains("JSCompiler_object_inline_b_1"));
    assertFalse(src.contains("var x"));
  }

  // Assignment expression (not var declaration) is handled
  @Test
  public void testInlineObjectLiteral_assignmentExpression_inlines() {
    Node root = parse("var x; x = {a: 1, b: 2}; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("JSCompiler_object_inline_a_0"));
    assertTrue(src.contains("JSCompiler_object_inline_b_1"));
    assertFalse(src.contains("var x"));
  }

  // The whole object is used directly, so it is not inlined
  @Test
  public void testInlineObjectLiteral_usedAsWhole_doesNotInline() {
    Node root = parse("var x = {a: 1}; foo(x);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Referencing a property that is not defined in the object literal
  @Test
  public void testInlineObjectLiteral_propertyNotDefined_doesNotInline() {
    Node root = parse("var x = {a: 1}; alert(x.b);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Self-referential assignment must not be inlined
  @Test
  public void testInlineObjectLiteral_selfReference_doesNotInline() {
    Node root = parse("var x = {a: x.b, b: 2}; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // ES5 getter/setter is not supported by this pass
  @Test
  public void testInlineObjectLiteral_getterSetter_doesNotInline() {
    Node root = parse("var x = {get a() { return 1; }}; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Calling a method on an object property prevents inlining
  @Test
  public void testInlineObjectLiteral_callTarget_doesNotInline() {
    Node root = parse("var x = {a: 1, b: function(){}}; x.b();");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // A variable with no object literal assignment is not eligible
  @Test
  public void testInlineObjectLiteral_uninitializedVar_doesNotInline() {
    Node root = parse("var x; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // A variable assigned a non-object-literal value cannot be inlined
  @Test
  public void testInlineObjectLiteral_nonObjectAssignment_doesNotInline() {
    Node root = parse("var x = 1; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Array access (not property access) is not handled
  @Test
  public void testInlineObjectLiteral_arrayAccess_doesNotInline() {
    Node root = parse("var x = {a: 1}; x[0];");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Multiple assignments of non-object values should not be inlined
  @Test
  public void testInlineObjectLiteral_multipleAssignments_doesNotInline() {
    Node root = parse("var x; x = {a: 1}; x = 3; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Empty object literal is not useful to inline
  @Test
  public void testInlineObjectLiteral_emptyObject_doesNotInline() {
    Node root = parse("var x = {}; alert(x.a);");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }

  // Property assignment can also be inlined
  @Test
  public void testInlineObjectLiteral_propertyAssignment_inlines() {
    Node root = parse("var x = {a: 1}; x.a = 3;");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("JSCompiler_object_inline_a_0"));
    assertFalse(src.contains("var x"));
  }

  // No references to the object (only declaration) is not inlined
  @Test
  public void testInlineObjectLiteral_noUses_doesNotInline() {
    Node root = parse("var x = {a: 1};");
    process(root);

    String src = toSource(root);
    assertTrue(src.contains("var x"));
    assertFalse(src.contains("JSCompiler_object_inline"));
  }
}
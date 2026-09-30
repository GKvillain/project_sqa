package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessClosurePrimitivesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ProcessClosurePrimitives createPass(CheckLevel requiresLevel) {
    return new ProcessClosurePrimitives(compiler, null, requiresLevel);
  }

  private Node processJs(String js, CheckLevel requiresLevel) {
    Node root = compiler.parseTestCode(js);
    ProcessClosurePrimitives pass = createPass(requiresLevel);
    pass.process(null, root);
    return root;
  }

  // Tests valid goog.provide replacement for a simple namespace
  @Test
  public void testProcess_simpleProvide_replacesWithVar() {
    String js = "goog.provide('foo');";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.toSource(root).contains("var foo"));
  }

  // Tests valid goog.provide replacement for nested namespaces
  @Test
  public void testProcess_nestedProvide_replacesWithAssign() {
    String js = "goog.provide('foo.bar');";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("var foo"));
    assertTrue(source.contains("foo.bar"));
  }

  // Tests valid goog.provide and goog.require resolution
  @Test
  public void testProcess_validProvideAndRequire_removesRequire() {
    String js = "goog.provide('foo'); goog.require('foo');";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertFalse(compiler.toSource(root).contains("goog.require"));
  }

  // Tests missing goog.provide when required
  @Test
  public void testProcess_missingProvide_reportsError() {
    String js = "goog.require('foo.missing');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.MISSING_PROVIDE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests require called before provide
  @Test
  public void testProcess_lateProvide_reportsError() {
    String js = "goog.require('foo'); goog.provide('foo');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.LATE_PROVIDE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests duplicate goog.provide calls
  @Test
  public void testProcess_duplicateProvide_reportsError() {
    String js = "goog.provide('foo'); goog.provide('foo');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests invalid identifier in goog.provide
  @Test
  public void testProcess_invalidProvideIdentifier_reportsError() {
    String js = "goog.provide('foo.123bar');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_PROVIDE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests declaring a function with the same name as a provided namespace
  @Test
  public void testProcess_functionDeclaredAsProvidedNamespace_reportsError() {
    String js = "goog.provide('foo'); function foo() {}";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests missing require error is ignored when requiresLevel is OFF
  @Test
  public void testProcess_missingProvideWithCheckLevelOff_noError() {
    String js = "goog.require('foo.missing');";
    processJs(js, CheckLevel.OFF);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests goog.exportSymbol extracts variable name correctly
  @Test
  public void testProcess_exportSymbol_recordsExportedVariable() {
    String js = "goog.exportSymbol('myExportedVar.subProp', obj);";
    Node root = compiler.parseTestCode(js);
    ProcessClosurePrimitives pass = createPass(CheckLevel.ERROR);
    pass.process(null, root);

    assertEquals(0, compiler.getErrorCount());
    assertTrue(pass.getExportedVariableNames().contains("myExportedVar"));
  }

  // Tests goog.addDependency replacement
  @Test
  public void testProcess_addDependency_replacesWithNumberLiteral() {
    String js = "goog.addDependency('foo.js', ['foo'], []);";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertFalse(compiler.toSource(root).contains("goog.addDependency"));
  }

  // Tests valid goog.setCssNameMapping with BY_PART style
  @Test
  public void testProcess_setCssNameMapping_validObjectLit() {
    String js = "goog.setCssNameMapping({'active': 'act', 'button': 'btn'});";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertFalse(compiler.toSource(root).contains("goog.setCssNameMapping"));
    assertNotNull(compiler.getCssRenamingMap());
  }

  // Tests goog.setCssNameMapping with invalid style parameter
  @Test
  public void testProcess_setCssNameMapping_invalidStyle_reportsError() {
    String js = "goog.setCssNameMapping({'active': 'act'}, 'INVALID_STYLE');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_STYLE_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.setCssNameMapping with non-string values
  @Test
  public void testProcess_setCssNameMapping_nonStringValue_reportsError() {
    String js = "goog.setCssNameMapping({'active': 123});";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.base without 'this' argument
  @Test
  public void testProcess_baseClassCallWithoutThis_reportsError() {
    String js = "function Foo() { goog.base(); }";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.base used as a property reference instead of direct call
  @Test
  public void testProcess_baseClassGetPropNotDirectCall_reportsError() {
    String js = "var ref = goog.base;";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests valid goog.setCssNameMapping with BY_WHOLE style
  @Test
  public void testProcess_setCssNameMapping_byWholeStyle() {
    String js = "goog.setCssNameMapping({'active-button': 'act-btn'}, 'BY_WHOLE');";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertFalse(compiler.toSource(root).contains("goog.setCssNameMapping"));
    assertNotNull(compiler.getCssRenamingMap());
  }

  // Tests duplicate goog.setCssNameMapping calls report error
  @Test
  public void testProcess_duplicateSetCssNameMapping_reportsError() {
    String js = "goog.setCssNameMapping({'a': 'b'}); goog.setCssNameMapping({'c': 'd'});";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.DUPLICATE_SET_CSS_NAME_MAPPING_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests non-object literal passed to goog.setCssNameMapping
  @Test
  public void testProcess_setCssNameMapping_nonObjectLit_reportsError() {
    String js = "goog.setCssNameMapping('notAnObject');";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.NON_OBJECT_LITERAL_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.base constructor call rewriting
  @Test
  public void testProcess_baseConstructorCall_rewritesCorrectly() {
    String js = "function Base(x) {} function Sub(x) { goog.base(this, x); } goog.inherits(Sub, Base);";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("Base.call(this, x)"));
  }

  // Tests goog.base method call rewriting
  @Test
  public void testProcess_baseMethodCall_rewritesCorrectly() {
    String js = "function Base() {} Base.prototype.foo = function(x) {};"
        + "function Sub() {} goog.inherits(Sub, Base);"
        + "Sub.prototype.foo = function(x) { goog.base(this, 'foo', x); };";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("Base.prototype.foo.call(this, x)"));
  }

  // Tests goog.base called with mismatched method name reports error
  @Test
  public void testProcess_baseMethodNameMismatch_reportsError() {
    String js = "function Base() {} Base.prototype.foo = function() {};"
        + "function Sub() {} goog.inherits(Sub, Base);"
        + "Sub.prototype.bar = function() { goog.base(this, 'foo'); };";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.base called without a superclass reports error
  @Test
  public void testProcess_baseCallWithoutSuperclass_reportsError() {
    String js = "function Foo() { goog.base(this); }";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.base called in global scope reports error
  @Test
  public void testProcess_baseCallInGlobalScope_reportsError() {
    String js = "goog.base(this);";
    processJs(js, CheckLevel.ERROR);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR, compiler.getErrors()[0].getType());
  }

  // Tests goog.exportProperty call handling
  @Test
  public void testProcess_exportProperty_removesOrPreservesCall() {
    String js = "var obj = {}; goog.exportProperty(obj, 'publicName', obj.internalName);";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    assertNotNull(root);
  }

  // Tests multiple provided sub-namespaces sharing the same parent
  @Test
  public void testProcess_multipleSubNamespaces_createsSingleParentVar() {
    String js = "goog.provide('foo.bar'); goog.provide('foo.baz');";
    Node root = processJs(js, CheckLevel.ERROR);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("var foo"));
    assertTrue(source.contains("foo.bar"));
    assertTrue(source.contains("foo.baz"));
  }
}
package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  private static final String EXTERNS = "var window;";

  public ScopedAliasesTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias expansion
  @Test
  public void testBasicAlias_replacesAliasWithQualifiedName() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests transitive alias resolution
  @Test
  public void testTransitiveAliases_resolvesChainedAliases() {
    test(
        "goog.scope(function() {\n" +
        "  var g = goog;\n" +
        "  var d = g.dom;\n" +
        "  d.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests alias cycle reporting
  @Test
  public void testAliasCycle_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var a = b;\n" +
        "  var b = a;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests type annotation replacement in JSDoc
  @Test
  public void testTypeAnnotation_updatesAliasedTypes() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @type {Button} */\n" +
        "  var b;\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "/** @type {goog.ui.Button} */\n" +
        "$jscomp.scope.b;");
  }

  // Tests nested type annotations in JSDoc (Bug 108 regression)
  @Test
  public void testNestedTypeAnnotation_updatesAliasedNestedType() {
    test(
        "goog.scope(function() {\n" +
        "  var ui = goog.ui;\n" +
        "  /** @type {ui.Button.Item} */\n" +
        "  var item;\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "/** @type {goog.ui.Button.Item} */\n" +
        "$jscomp.scope.item;");
  }

  // Tests function declarations inside goog.scope hoisted to global namespace
  @Test
  public void testFunctionDeclaration_convertsToScopedGlobal() {
    test(
        "goog.scope(function() {\n" +
        "  function helper() {}\n" +
        "  helper();\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.helper = function() {};\n" +
        "$jscomp.scope.helper();");
  }

  // Tests non-alias var declarations rewritten to $jscomp.scope
  @Test
  public void testNonAliasLocalVar_convertsToScopedGlobal() {
    test(
        "goog.scope(function() {\n" +
        "  var count = 42;\n" +
        "  alert(count);\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.count = 42;\n" +
        "alert($jscomp.scope.count);");
  }

  // Tests multiple scopes creating unique scoped global names
  @Test
  public void testMultipleScopes_createsUniqueNames() {
    test(
        "goog.scope(function() {\n" +
        "  var a = 1;\n" +
        "});\n" +
        "goog.scope(function() {\n" +
        "  var a = 2;\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.a = 1;\n" +
        "$jscomp.scope.a$1 = 2;");
  }

  // Tests error when goog.scope is not in a single expression statement
  @Test
  public void testImproperScopeUsage_reportsError() {
    testError(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error on invalid arguments passed to goog.scope
  @Test
  public void testBadParameters_reportsError() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(123);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(function(param) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(function named() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope body contains this
  @Test
  public void testReferencesThis_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  this.foo = 1;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error when goog.scope body contains return
  @Test
  public void testUsesReturn_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  return;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error when goog.scope body contains throw
  @Test
  public void testUsesThrow_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  throw 'error';\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error when alias is reassigned / redefined
  @Test
  public void testAliasRedefined_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom = goog.events;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests renaming of local variable that shadows an aliased namespace root
  @Test
  public void testNamespaceShadowing_renamesConflictingInnerLocal() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function inner() {\n" +
        "    var goog = 1;\n" +
        "    return dom.createElement(goog);\n" +
        "  }\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.inner = function() {\n" +
        "  var goog$$module$ = 1;\n" +
        "  return goog.dom.createElement(goog$$module$);\n" +
        "};");
  }

  // Tests multiple declarators in a single var statement
  @Test
  public void testMultipleDeclarators_replacesAllAliases() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom, events = goog.events;\n" +
        "  dom.createElement(events.EventType.CLICK);\n" +
        "});",
        "goog.dom.createElement(goog.events.EventType.CLICK);");
  }

  // Tests alias usage in new expression
  @Test
  public void testNewExpression_replacesConstructorAlias() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  var b = new Button();\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.b = new goog.ui.Button();");
  }

  // Tests alias usage in object literal values
  @Test
  public void testObjectLiteral_replacesAliasedPropertyValues() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  var map = {d: dom};\n" +
        "});",
        "var $jscomp = $jscomp || {};\n" +
        "$jscomp.scope = $jscomp.scope || {};\n" +
        "$jscomp.scope.map = {d: goog.dom};");
  }

  // Tests property assignment on aliased namespace/constructor
  @Test
  public void testPropertiesOnAliasedNamespace_expandsCorrectly() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  Button.Side = { LEFT: 0, RIGHT: 1 };\n" +
        "});",
        "goog.ui.Button.Side = { LEFT: 0, RIGHT: 1 };");
  }

  // Tests @typedef JSDoc tag type alias expansion
  @Test
  public void testTypedefAnnotation_updatesAliasedType() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @typedef {Button} */\n" +
        "  goog.ui.MyButton;\n" +
        "});",
        "/** @typedef {goog.ui.Button} */\n" +
        "goog.ui.MyButton;");
  }

  // Tests @extends and @constructor JSDoc tags type alias expansion
  @Test
  public void testExtendsAnnotation_updatesAliasedType() {
    test(
        "goog.scope(function() {\n" +
        "  var Component = goog.ui.Component;\n" +
        "  /** @constructor @extends {Component} */\n" +
        "  goog.ui.CustomComponent = function() {};\n" +
        "});",
        "/** @constructor @extends {goog.ui.Component} */\n" +
        "goog.ui.CustomComponent = function() {};");
  }

  // Tests @param and @return JSDoc tags type alias expansion
  @Test
  public void testParamAndReturnAnnotation_updatesAliasedType() {
    test(
        "goog.scope(function() {\n" +
        "  var Element = goog.dom.Element;\n" +
        "  /**\n" +
        "   * @param {Element} el\n" +
        "   * @return {Element}\n" +
        "   */\n" +
        "  goog.dom.clone = function(el) {\n" +
        "    return el;\n" +
        "  };\n" +
        "});",
        "/**\n" +
        " * @param {goog.dom.Element} el\n" +
        " * @return {goog.dom.Element}\n" +
        " */\n" +
        "goog.dom.clone = function(el) {\n" +
        "  return el;\n" +
        "};");
  }

  // Tests empty goog.scope removal
  @Test
  public void testEmptyScope_removesScopeWithoutArtifacts() {
    test("goog.scope(function() {});", "");
  }
}
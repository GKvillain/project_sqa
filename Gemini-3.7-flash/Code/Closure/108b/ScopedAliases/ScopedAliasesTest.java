package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  private static final String EXTERNS = "var goog = {}; goog.scope = function(f) {};";

  public ScopedAliasesTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias expansion and scope collapsing
  @Test
  public void testProcess_basicAlias_inlinesAliasAndRemovesScope() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  var DIV = dom.TagName.DIV;\n"
            + "  dom.createElement(DIV);\n"
            + "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

  // Tests transitive alias resolution
  @Test
  public void testProcess_transitiveAlias_resolvesCorrectly() {
    test(
        "goog.scope(function() {\n"
            + "  var g = goog;\n"
            + "  var d = g.dom;\n"
            + "  d.createElement('DIV');\n"
            + "});",
        "goog.dom.createElement('DIV');");
  }

  // Tests hoisted function declaration transformation in goog.scope
  @Test
  public void testProcess_functionDeclaration_rewritesToGlobalScope() {
    test(
        "goog.scope(function() {\n"
            + "  function foo() {}\n"
            + "  foo();\n"
            + "});",
        "var $jscomp = $jscomp || {};\n"
            + "$jscomp.scope = $jscomp.scope || {};\n"
            + "$jscomp.scope.foo = function() {};\n"
            + "$jscomp.scope.foo();");
  }

  // Tests type alias replacement in JSDoc annotations
  @Test
  public void testProcess_jsdocTypeAnnotation_expandsTypeAlias() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  /** @type {dom.Element} */ var el;\n"
            + "});",
        "var $jscomp = $jscomp || {};\n"
            + "$jscomp.scope = $jscomp.scope || {};\n"
            + "/** @type {goog.dom.Element} */\n"
            + "$jscomp.scope.el;\n"
            + "var $jscomp$scope$el;");
  }

  // Tests alias cycle detection and error reporting
  @Test
  public void testProcess_aliasCycle_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var a = b;\n"
            + "  var b = a;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests redefined alias error reporting
  @Test
  public void testProcess_redefinedAlias_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var a = goog.dom;\n"
            + "  a = goog.events;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests improper usage of goog.scope in expressions
  @Test
  public void testProcess_scopeUsedInExpression_reportsError() {
    testError(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests goog.scope without parameters
  @Test
  public void testProcess_scopeWithoutParameters_reportsError() {
    testError(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests goog.scope with parameters in the inner anonymous function
  @Test
  public void testProcess_scopeFunctionWithParameters_reportsError() {
    testError(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests goog.scope with a named function instead of an anonymous function
  @Test
  public void testProcess_scopeFunctionWithNamedFunction_reportsError() {
    testError(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests non-alias local variable error reporting
  @Test
  public void testProcess_nonAliasLocalVariable_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  try {} catch (e) {}\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests 'this' reference within goog.scope block
  @Test
  public void testProcess_referenceThisInScope_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  this.foo();\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests 'return' statement within goog.scope block
  @Test
  public void testProcess_returnInScope_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  return;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests 'throw' statement within goog.scope block
  @Test
  public void testProcess_throwInScope_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  throw 'err';\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests shadowing of namespace variable in inner function scope
  @Test
  public void testProcess_shadowedNamespaceLocal_renamesShadowedLocal() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function f() {\n"
            + "    var goog = 1;\n"
            + "    dom.createElement(goog);\n"
            + "  }\n"
            + "});",
        "var $jscomp = $jscomp || {};\n"
            + "$jscomp.scope = $jscomp.scope || {};\n"
            + "$jscomp.scope.f = function() {\n"
            + "  var goog$$module$index = 1;\n"
            + "  goog.dom.createElement(goog$$module$index);\n"
            + "};");
  }
}
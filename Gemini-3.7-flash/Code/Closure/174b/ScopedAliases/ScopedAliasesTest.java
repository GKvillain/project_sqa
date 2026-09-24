package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  public ScopedAliasesTest() {
    super("var goog = {}; goog.scope = function(f) {}; var alert;");
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(
        compiler,
        null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias expansion and scope collapsing
  @Test
  public void testProcess_basicAlias_inlinesAlias() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests chained / transitive alias references
  @Test
  public void testProcess_transitiveAliases_inlinesAllAliases() {
    test(
        "goog.scope(function() {\n" +
        "  var d = goog.dom;\n" +
        "  var create = d.createElement;\n" +
        "  create('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests rewriting non-alias primitive variable inside goog.scope
  @Test
  public void testProcess_nonAliasVar_rewritesToJscompScope() {
    test(
        "goog.scope(function() {\n" +
        "  var x = 10;\n" +
        "  alert(x);\n" +
        "});",
        "$jscomp.scope.x = 10;\n" +
        "alert($jscomp.scope.x);");
  }

  // Tests multiple non-alias variables across different scopes to ensure uniquified scope names
  @Test
  public void testProcess_multipleScopesWithSameNonAliasName_disambiguatesNames() {
    test(
        "goog.scope(function() {\n" +
        "  var x = 1;\n" +
        "  alert(x);\n" +
        "});\n" +
        "goog.scope(function() {\n" +
        "  var x = 2;\n" +
        "  alert(x);\n" +
        "});",
        "$jscomp.scope.x = 1;\n" +
        "alert($jscomp.scope.x);\n" +
        "$jscomp.scope.x$1 = 2;\n" +
        "alert($jscomp.scope.x$1);");
  }

  // Tests multiple var declarations in a single statement inside goog.scope
  @Test
  public void testProcess_multipleVarsInSingleStatement_handlesCorrectly() {
    test(
        "goog.scope(function() {\n" +
        "  var a = 1, b = 2;\n" +
        "  alert(a + b);\n" +
        "});",
        "$jscomp.scope.a = 1;\n" +
        "$jscomp.scope.b = 2;\n" +
        "alert($jscomp.scope.a + $jscomp.scope.b);");
  }

  // Tests alias combined with non-alias var in single var statement
  @Test
  public void testProcess_mixedAliasAndNonAliasVar_handlesCorrectly() {
    test(
        "goog.scope(function() {\n" +
        "  var a = 1, d = goog.dom;\n" +
        "  d.createElement(a);\n" +
        "});",
        "$jscomp.scope.a = 1;\n" +
        "goog.dom.createElement($jscomp.scope.a);");
  }

  // Tests alias usage in JSDoc type annotation
  @Test
  public void testProcess_jsdocTypeAnnotation_expandsTypeAlias() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  /** @type {dom.Element} */\n" +
        "  var el;\n" +
        "});",
        "/** @type {goog.dom.Element} */\n" +
        "var el;");
  }

  // Tests namespace shadowing inside inner functions
  @Test
  public void testProcess_innerScopeShadowsNamespace_renamesShadowedVariable() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function foo() {\n" +
        "    var goog = 1;\n" +
        "    dom.createElement(goog);\n" +
        "  }\n" +
        "});",
        "function foo() {\n" +
        "  var goog$$module$default = 1;\n" +
        "  goog.dom.createElement(goog$$module$default);\n" +
        "}");
  }

  // Tests error reporting when goog.scope is used in an expression instead of a statement
  @Test
  public void testValidateScopeCall_usedInExpression_reportsError() {
    test(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error reporting when goog.scope has no arguments
  @Test
  public void testValidateScopeCall_noArguments_reportsError() {
    test(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope is passed a named function
  @Test
  public void testValidateScopeCall_namedFunction_reportsError() {
    test(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope function takes parameters
  @Test
  public void testValidateScopeCall_functionWithParameters_reportsError() {
    test(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope body references 'this'
  @Test
  public void testVisit_referencesThis_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  this.x = 1;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error reporting when goog.scope body uses 'return'
  @Test
  public void testVisit_usesReturn_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  return;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error reporting when goog.scope body uses 'throw'
  @Test
  public void testVisit_usesThrow_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  throw 'error';\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error reporting when an alias is reassigned
  @Test
  public void testVisit_aliasRedefined_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom = goog.events;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error reporting when aliases have a circular reference cycle
  @Test
  public void testHotSwapScript_aliasCycle_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  var a = b;\n" +
        "  var b = a;\n" +
        "  a();\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests error reporting when local hoisted function declaration is in goog.scope
  @Test
  public void testFindAliases_hoistedFunction_reportsError() {
    test(
        "goog.scope(function() {\n" +
        "  function bar() {}\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }
}
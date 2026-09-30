package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  private static final String EXTERNS = "var window;";

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

  private void testScopedFailure(String js, DiagnosticType expectedError) {
    test(js, (String) null, expectedError);
  }

  // Tests basic alias replacement
  @Test
  public void testProcess_basicAlias_inlinesAlias() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.createElement('div');\n"
            + "});",
        "goog.dom.createElement('div');");
  }

  // Tests chained / transitive aliases
  @Test
  public void testProcess_chainedAlias_inlinesTransitively() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  var create = dom.createElement;\n"
            + "  create('div');\n"
            + "});",
        "goog.dom.createElement('div');");
  }

  // Tests multiple aliases in a single var declaration
  @Test
  public void testProcess_multipleVarsInOneDeclaration_inlinesAliases() {
    test(
        "goog.scope(function() {\n"
            + "  var a = goog.a, b = goog.b;\n"
            + "  a();\n"
            + "  b();\n"
            + "});",
        "goog.a();\n"
            + "goog.b();");
  }

  // Tests non-alias local variable inside goog.scope
  @Test
  public void testProcess_nonAliasLocal_rewritesToScopeNamespace() {
    test(
        "goog.scope(function() {\n"
            + "  var a = 1;\n"
            + "  var b = a + 2;\n"
            + "});",
        "$jscomp.scope.a = 1;\n"
            + "$jscomp.scope.b = $jscomp.scope.a + 2;");
  }

  // Tests non-alias variable without initial value
  @Test
  public void testProcess_uninitializedLocal_rewritesToScopeNamespace() {
    test(
        "goog.scope(function() {\n"
            + "  var a;\n"
            + "  a = 1;\n"
            + "});",
        "$jscomp.scope.a = 1;");
  }

  // Tests multiple scopes with same local variable names
  @Test
  public void testProcess_duplicateNamesInSeparateScopes_createsUniqueNames() {
    test(
        "goog.scope(function() {\n"
            + "  var a = 1;\n"
            + "});\n"
            + "goog.scope(function() {\n"
            + "  var a = 2;\n"
            + "});",
        "$jscomp.scope.a = 1;\n"
            + "$jscomp.scope.a$1 = 2;");
  }

  // Tests JSDoc type node alias replacement
  @Test
  public void testProcess_jsdocTypeAlias_expandsType() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  /** @type {Button} */\n"
            + "  var b = null;\n"
            + "});",
        "/** @type {goog.ui.Button} */\n"
            + "$jscomp.scope.b = null;");
  }

  // Tests JSDoc type with property access
  @Test
  public void testProcess_jsdocSubtypeAlias_expandsType() {
    test(
        "goog.scope(function() {\n"
            + "  var ui = goog.ui;\n"
            + "  /** @type {ui.Button} */\n"
            + "  var b = null;\n"
            + "});",
        "/** @type {goog.ui.Button} */\n"
            + "$jscomp.scope.b = null;");
  }

  // Tests shadowed namespace renaming
  @Test
  public void testProcess_shadowedNamespace_renamesShadowedVariable() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function foo(goog) {\n"
            + "    return goog;\n"
            + "  }\n"
            + "  dom.foo = foo;\n"
            + "});",
        "$jscomp.scope.foo = function(goog$$1) {\n"
            + "  return goog$$1;\n"
            + "};\n"
            + "goog.dom.foo = $jscomp.scope.foo;");
  }

  // Tests error when goog.scope is used in an expression
  @Test
  public void testProcess_googScopeInExpression_reportsError() {
    testScopedFailure("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error when goog.scope has no arguments
  @Test
  public void testProcess_noArguments_reportsError() {
    testScopedFailure("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope argument is not a function
  @Test
  public void testProcess_nonFunctionArgument_reportsError() {
    testScopedFailure("goog.scope(42);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope function has parameters
  @Test
  public void testProcess_functionWithParameters_reportsError() {
    testScopedFailure("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope function has a name
  @Test
  public void testProcess_namedFunction_reportsError() {
    testScopedFailure("goog.scope(function foo() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope contains return statement
  @Test
  public void testProcess_returnInScope_reportsError() {
    testScopedFailure("goog.scope(function() { return; });", ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error when goog.scope contains this reference
  @Test
  public void testProcess_thisInScope_reportsError() {
    testScopedFailure("goog.scope(function() { this.x = 1; });", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error when goog.scope contains throw statement
  @Test
  public void testProcess_throwInScope_reportsError() {
    testScopedFailure("goog.scope(function() { throw 'err'; });", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error when alias is reassigned
  @Test
  public void testProcess_aliasRedefined_reportsError() {
    testScopedFailure(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom = goog.dom2;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error on cyclic alias reference
  @Test
  public void testProcess_aliasCycle_reportsError() {
    testScopedFailure(
        "goog.scope(function() {\n"
            + "  var a = b;\n"
            + "  var b = a;\n"
            + "  a();\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests error on catch parameter in scope
  @Test
  public void testProcess_catchParameterInScope_reportsError() {
    testScopedFailure(
        "goog.scope(function() {\n"
            + "  try {\n"
            + "  } catch (e) {\n"
            + "  }\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests code without goog.scope is unchanged
  @Test
  public void testProcess_noScope_unchanged() {
    testSame("var a = 1;");
  }

  // Tests function declarations hoisted to scope namespace
  @Test
  public void testProcess_functionDeclaration_rewritesToScopeNamespace() {
    test(
        "goog.scope(function() {\n"
            + "  function f() {}\n"
            + "  f();\n"
            + "});",
        "$jscomp.scope.f = function() {};\n"
            + "$jscomp.scope.f();");
  }

  // Tests alias usage inside inner function
  @Test
  public void testProcess_innerFunctionUsingAlias_inlinesAlias() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function f() {\n"
            + "    return dom.createElement('div');\n"
            + "  }\n"
            + "});",
        "$jscomp.scope.f = function() {\n"
            + "  return goog.dom.createElement('div');\n"
            + "};");
  }

  // Tests JSDoc typedef alias expansion
  @Test
  public void testProcess_jsdocTypedef_expandsType() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  /** @typedef {Button} */\n"
            + "  var MyButton;\n"
            + "});",
        "/** @typedef {goog.ui.Button} */\n"
            + "$jscomp.scope.MyButton;");
  }

  // Tests JSDoc union type alias expansion
  @Test
  public void testProcess_jsdocUnionType_expandsType() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  /** @type {dom|string} */\n"
            + "  var a = null;\n"
            + "});",
        "/** @type {goog.dom|string} */\n"
            + "$jscomp.scope.a = null;");
  }

  // Tests constructor assignment with alias namespace
  @Test
  public void testProcess_constructorAssignment_rewritesConstructor() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  /** @constructor */\n"
            + "  dom.Foo = function() {};\n"
            + "});",
        "/** @constructor */\n"
            + "goog.dom.Foo = function() {};");
  }
}
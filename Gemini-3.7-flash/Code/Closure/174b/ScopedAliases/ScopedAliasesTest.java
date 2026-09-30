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

  // Tests basic alias transformation for qualified names
  @Test
  public void testProcess_basicAlias_inlinesAlias() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var DIV = dom.TagName.DIV;" +
        "  dom.createElement(DIV);" +
        "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

  // Tests chained alias resolution in order of declaration
  @Test
  public void testProcess_transitiveAlias_inlinesCorrectly() {
    test(
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  var d = g.dom;" +
        "  d.createElement('DIV');" +
        "});",
        "goog.dom.createElement('DIV');");
  }

  // Tests local non-qualified variable definition converted to $jscomp.scope
  @Test
  public void testProcess_nonAliasLocalVariable_transformsToJscompScope() {
    test(
        "goog.scope(function() {" +
        "  var x = 10;" +
        "  var y = x + 5;" +
        "});",
        "$jscomp.scope.x = 10;" +
        "$jscomp.scope.y = $jscomp.scope.x + 5;");
  }

  // Tests multiple variables declared in a single var statement
  @Test
  public void testProcess_multipleVarsInSingleStatement_transformsCorrectly() {
    test(
        "goog.scope(function() {" +
        "  var a = 1, b = 2;" +
        "  var sum = a + b;" +
        "});",
        "$jscomp.scope.a = 1;" +
        "$jscomp.scope.b = 2;" +
        "$jscomp.scope.sum = $jscomp.scope.a + $jscomp.scope.b;");
  }

  // Tests mixed alias and literal in single var statement
  @Test
  public void testProcess_mixedAliasAndLiteralInSingleVar_transformsCorrectly() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom, x = 1;" +
        "  dom.createElement(x);" +
        "});",
        "$jscomp.scope.x = 1;" +
        "goog.dom.createElement($jscomp.scope.x);");
  }

  // Tests error reporting when goog.scope is assigned to a variable
  @Test
  public void testProcess_improperScopeUse_reportsError() {
    testError(
        "var s = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error reporting when goog.scope has no arguments
  @Test
  public void testProcess_noParameters_reportsError() {
    testError(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope parameter is not an anonymous function
  @Test
  public void testProcess_namedFunctionParameter_reportsError() {
    testError(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope parameter function takes arguments
  @Test
  public void testProcess_functionWithParameters_reportsError() {
    testError(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope function uses return statement
  @Test
  public void testProcess_usesReturn_reportsError() {
    testError(
        "goog.scope(function() { return; });",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error reporting when goog.scope function uses this keyword
  @Test
  public void testProcess_referencesThis_reportsError() {
    testError(
        "goog.scope(function() { this.x = 1; });",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error reporting when goog.scope function uses throw statement
  @Test
  public void testProcess_usesThrow_reportsError() {
    testError(
        "goog.scope(function() { throw 'error'; });",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error reporting when an alias is reassigned
  @Test
  public void testProcess_aliasRedefined_reportsError() {
    testError(
        "goog.scope(function() {" +
        "  var d = goog.dom;" +
        "  d = goog.events;" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error reporting on cyclical alias references
  @Test
  public void testProcess_aliasCycle_reportsError() {
    testError(
        "goog.scope(function() {" +
        "  var a = b.c;" +
        "  var b = a.c;" +
        "  a.foo();" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests error reporting when function declaration is hoisted inside goog.scope
  @Test
  public void testProcess_hoistedFunction_reportsError() {
    testError(
        "goog.scope(function() {" +
        "  function helper() {}" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests renaming of shadowed namespace variables in inner scopes
  @Test
  public void testProcess_shadowedNamespace_renamesShadowedVariable() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  function f() {" +
        "    var goog = 1;" +
        "    return goog;" +
        "  }" +
        "  dom.createElement(f());" +
        "});",
        "function f() {" +
        "  var goog$$module$default = 1;" +
        "  return goog$$module$default;" +
        "}" +
        "goog.dom.createElement(f());");
  }

  // Tests multiple separate goog.scope blocks in same file
  @Test
  public void testProcess_multipleScopes_transformsIndependently() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('A');" +
        "});" +
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('B');" +
        "});",
        "goog.dom.createElement('A');" +
        "goog.dom.createElement('B');");
  }

  // Tests JSDoc type references aliasing transformation
  @Test
  public void testProcess_jsdocTypeAlias_expandsType() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  /** @type {Button} */" +
        "  var b;" +
        "});",
        "/** @type {goog.ui.Button} */" +
        "$jscomp.scope.b;");
  }

  // Tests empty goog.scope block removal
  @Test
  public void testProcess_emptyScope_removesBlock() {
    test("goog.scope(function() {});", "");
  }

  // Tests alias used inside new expressions and constructor instantiation
  @Test
  public void testProcess_aliasInNewExpression_inlinesConstructor() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  var b = new Button();" +
        "});",
        "$jscomp.scope.b = new goog.ui.Button();");
  }

  // Tests alias used inside object literals
  @Test
  public void testProcess_aliasInObjectLiteral_inlinesValue() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  var obj = {button: Button};" +
        "});",
        "$jscomp.scope.obj = {button: goog.ui.Button};");
  }

  // Tests alias resolution inside inner function body
  @Test
  public void testProcess_aliasInsideInnerFunction_inlinesCorrectly() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var fn = function() { return dom.getElement('id'); };" +
        "});",
        "$jscomp.scope.fn = function() { return goog.dom.getElement('id'); };");
  }

  // Tests inner function parameter shadowing alias
  @Test
  public void testProcess_innerFunctionShadowsAlias_doesNotInlineInner() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var fn = function(dom) { return dom.custom(); };" +
        "  dom.init();" +
        "});",
        "$jscomp.scope.fn = function(dom) { return dom.custom(); };" +
        "goog.dom.init();");
  }

  // Tests JSDoc param and return type expansions with aliases
  @Test
  public void testProcess_jsdocParamAndReturn_expandsTypes() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  /**\n" +
        "   * @param {Button} btn\n" +
        "   * @return {Button}\n" +
        "   */\n" +
        "  var render = function(btn) { return btn; };" +
        "});",
        "/**\n" +
        " * @param {goog.ui.Button} btn\n" +
        " * @return {goog.ui.Button}\n" +
        " */\n" +
        "$jscomp.scope.render = function(btn) { return btn; };");
  }

  // Tests type cast expression expansion with aliases
  @Test
  public void testProcess_jsdocTypeCast_expandsType() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  var b = (/** @type {Button} */ (x));" +
        "});",
        "$jscomp.scope.b = (/** @type {goog.ui.Button} */ (x));");
  }

  // Tests nested goog.scope reports improper scope error
  @Test
  public void testProcess_nestedScope_reportsError() {
    testError(
        "goog.scope(function() {" +
        "  goog.scope(function() {});" +
        "});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests uninitialized local variable transformation
  @Test
  public void testProcess_uninitializedVar_transformsToJscompScope() {
    test(
        "goog.scope(function() {" +
        "  var a;" +
        "  a = 1;" +
        "});",
        "$jscomp.scope.a;" +
        "$jscomp.scope.a = 1;");
  }
}
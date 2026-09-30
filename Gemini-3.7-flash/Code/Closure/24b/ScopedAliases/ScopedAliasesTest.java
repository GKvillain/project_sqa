package com.google.javascript.jscomp;

import org.junit.Test;

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

  // Tests basic alias transformation and scope unwrapping
  @Test
  public void testBasicAlias_validScope_transformsAlias() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.createElement('div');\n"
            + "});",
        "goog.dom.createElement('div');");
  }

  // Tests transitive aliases resolution
  @Test
  public void testTransitiveAlias_chainedAliases_transformsAll() {
    test(
        "goog.scope(function() {\n"
            + "  var g = goog;\n"
            + "  var d = g.dom;\n"
            + "  d.createElement('div');\n"
            + "});",
        "goog.dom.createElement('div');");
  }

  // Tests multiple alias definitions in a single var statement
  @Test
  public void testMultipleAliasInOneVar_validAliases_transformsAll() {
    test(
        "goog.scope(function() {\n"
            + "  var a = goog.a, b = goog.b;\n"
            + "  a(); b();\n"
            + "});",
        "goog.a(); goog.b();");
  }

  // Tests alias replacement inside inner functions
  @Test
  public void testInnerFunction_usesAlias_replacesAliasInsideFunction() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.create = function() { return dom.createElement('div'); };\n"
            + "});",
        "goog.dom.create = function() { return goog.dom.createElement('div'); };");
  }

  // Tests type annotation aliased node replacement
  @Test
  public void testTypeAnnotation_aliasInDocComment_replacesTypeName() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  /** @type {Button} */ var b;\n"
            + "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests compound type annotation aliased node replacement
  @Test
  public void testCompoundTypeAnnotation_aliasWithSubfield_replacesBaseTypeName() {
    test(
        "goog.scope(function() {\n"
            + "  var ui = goog.ui;\n"
            + "  /** @type {ui.Button} */ var b;\n"
            + "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests non-alias local variable error
  @Test
  public void testNonAliasLocal_primitiveAssigned_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var x = 1;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests function declaration inside scope (regression test for defect 24)
  @Test
  public void testFunctionDeclarationInScope_nonAlias_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  function foo() {}\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests improper usage when goog.scope is in an expression rather than standalone statement
  @Test
  public void testScopeUsedImproperly_assignedToVar_reportsError() {
    testError(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests bad parameter error when goog.scope has no arguments
  @Test
  public void testBadParameters_noArguments_reportsError() {
    testError(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests bad parameter error when function takes arguments
  @Test
  public void testBadParameters_functionWithArguments_reportsError() {
    testError(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests bad parameter error when goog.scope takes a named function
  @Test
  public void testBadParameters_namedFunction_reportsError() {
    testError(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests bad parameter error when extra arguments are passed
  @Test
  public void testBadParameters_extraArguments_reportsError() {
    testError(
        "goog.scope(function() {}, 1);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests referencing 'this' in goog.scope body
  @Test
  public void testReferencesThis_topLevelThis_reportsError() {
    testError(
        "goog.scope(function() { this.foo(); });",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests using 'return' in goog.scope body
  @Test
  public void testUsesReturn_topLevelReturn_reportsError() {
    testError(
        "goog.scope(function() { return 1; });",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests using 'throw' in goog.scope body
  @Test
  public void testUsesThrow_topLevelThrow_reportsError() {
    testError(
        "goog.scope(function() { throw 'error'; });",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests redefining an alias within goog.scope body
  @Test
  public void testAliasRedefined_reassignedLater_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var x = goog.dom;\n"
            + "  x = goog.events;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests detecting alias cycles within goog.scope
  @Test
  public void testAliasCycle_circularAlias_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var a = b;\n"
            + "  var b = a;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests uninitialized variable in goog.scope reports non-alias local error
  @Test
  public void testUninitializedVar_noInitializer_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var x;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests that shadowing an alias with a function parameter prevents transformation
  @Test
  public void testShadowing_functionParameter_doesNotTransformInner() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function f(dom) {\n"
            + "    return dom.createElement('div');\n"
            + "  }\n"
            + "});",
        "function f(dom) {\n"
            + "  return dom.createElement('div');\n"
            + "}");
  }

  // Tests that shadowing an alias with a catch variable prevents transformation
  @Test
  public void testShadowing_catchVariable_doesNotTransformInner() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  try {\n"
            + "  } catch (dom) {\n"
            + "    dom();\n"
            + "  }\n"
            + "});",
        "try {\n"
            + "} catch (dom) {\n"
            + "  dom();\n"
            + "}");
  }

  // Tests alias used with 'new' expression
  @Test
  public void testNewExpression_aliasedConstructor_transformsTarget() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  var b = new Button();\n"
            + "});",
        "var b = new goog.ui.Button();");
  }

  // Tests alias used with 'instanceof' expression
  @Test
  public void testInstanceof_aliasedType_transformsTarget() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  var isButton = x instanceof Button;\n"
            + "});",
        "var isButton = x instanceof goog.ui.Button;");
  }

  // Tests multiple independent goog.scope blocks in the same file
  @Test
  public void testMultipleScopes_separateBlocks_transformsBoth() {
    test(
        "goog.scope(function() {\n"
            + "  var a = goog.a;\n"
            + "  a();\n"
            + "});\n"
            + "goog.scope(function() {\n"
            + "  var b = goog.b;\n"
            + "  b();\n"
            + "});",
        "goog.a();\n"
            + "goog.b();");
  }

  // Tests alias used in complex JSDoc annotations (param, return, union, array)
  @Test
  public void testComplexTypeAnnotations_paramAndReturn_replacesTypeNames() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  /**\n"
            + "   * @param {Button|string} a\n"
            + "   * @return {Array.<Button>}\n"
            + "   */\n"
            + "  function f(a) {}\n"
            + "});",
        "/**\n"
            + " * @param {goog.ui.Button|string} a\n"
            + " * @return {Array.<goog.ui.Button>}\n"
            + " */\n"
            + "function f(a) {}");
  }

  // Tests deep property access on an aliased namespace
  @Test
  public void testDeepPropertyAccess_onAlias_transformsCorrectly() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.classes.add(x, 'active');\n"
            + "});",
        "goog.dom.classes.add(x, 'active');");
  }

  // Tests alias used inside object and array literals
  @Test
  public void testAliasInLiterals_objectAndArray_transformsCorrectly() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  var obj = {d: dom};\n"
            + "  var arr = [dom];\n"
            + "});",
        "var obj = {d: goog.dom};\n"
            + "var arr = [goog.dom];");
  }
}
package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * Unit tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  public ScopedAliasesTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias substitution and scope unwrapping
  @Test
  public void testScopedAliases_basicAlias_replacesAlias() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  var DIV = dom.TagName.DIV;\n" +
        "  dom.createElement(DIV);\n" +
        "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

  // Tests transitive aliases substitution
  @Test
  public void testScopedAliases_transitiveAlias_replacesBoth() {
    test(
        "goog.scope(function() {\n" +
        "  var g = goog;\n" +
        "  var d = g.dom;\n" +
        "  d.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests alias replacement in type annotations
  @Test
  public void testScopedAliases_typeAnnotation_fixesType() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @type {Button} */ var b;\n" +
        "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests alias replacement in dotted type annotations
  @Test
  public void testScopedAliases_dottedTypeAnnotation_fixesType() {
    test(
        "goog.scope(function() {\n" +
        "  var ui = goog.ui;\n" +
        "  /** @type {ui.Button.State} */ var s;\n" +
        "});",
        "/** @type {goog.ui.Button.State} */ var s;");
  }

  // Tests alias usage inside nested functions
  @Test
  public void testScopedAliases_nestedFunctionScope_resolvesAlias() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function helper() {\n" +
        "    return dom.createElement('div');\n" +
        "  }\n" +
        "});",
        "function helper() {\n" +
        "  return goog.dom.createElement('div');\n" +
        "}");
  }

  // Tests error when goog.scope is used in an expression
  @Test
  public void testScopedAliases_usedInExpression_reportsError() {
    testError("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error when goog.scope is called with no arguments
  @Test
  public void testScopedAliases_noArguments_reportsError() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope is called with a named function
  @Test
  public void testScopedAliases_namedFunction_reportsError() {
    testError("goog.scope(function foo() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope function has parameters
  @Test
  public void testScopedAliases_functionWithParameters_reportsError() {
    testError("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope body contains 'this'
  @Test
  public void testScopedAliases_referencesThis_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  this.foo = null;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error when goog.scope body contains 'return'
  @Test
  public void testScopedAliases_usesReturn_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  return;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error when goog.scope body contains 'throw'
  @Test
  public void testScopedAliases_usesThrow_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  throw 'error';\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error when an alias is reassigned
  @Test
  public void testScopedAliases_aliasRedefined_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom = goog.otherDom;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error when a local variable is not an alias
  @Test
  public void testScopedAliases_nonAliasLocal_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var x = 1;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests multiple alias declarations in a single var statement
  @Test
  public void testScopedAliases_multipleAliasesInOneVar_removesVar() {
    test(
        "goog.scope(function() {\n" +
        "  var a = goog.a, b = goog.b;\n" +
        "  a();\n" +
        "  b();\n" +
        "});",
        "goog.a();\n" +
        "goog.b();");
  }

  // Tests that non-scoped JavaScript remains unchanged
  @Test
  public void testScopedAliases_noScopeCall_remainsUnchanged() {
    testSame("var a = 1; function f() { return a; }");
  }

  // Tests cyclical alias definitions report error
  @Test
  public void testScopedAliases_aliasCycle_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var a = b;\n" +
        "  var b = a;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests error when a local variable is uninitialized
  @Test
  public void testScopedAliases_uninitializedLocal_reportsError() {
    testError(
        "goog.scope(function() {\n" +
        "  var a;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests that inner function parameters shadowing an alias are not replaced
  @Test
  public void testScopedAliases_shadowedAliasInInnerScope_doesNotReplaceShadowed() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function helper(dom) {\n" +
        "    return dom;\n" +
        "  }\n" +
        "});",
        "function helper(dom) {\n" +
        "  return dom;\n" +
        "}");
  }

  // Tests multiple separate goog.scope blocks in the same file
  @Test
  public void testScopedAliases_multipleScopeBlocks_unwrapsBoth() {
    test(
        "goog.scope(function() {\n" +
        "  var a = goog.a;\n" +
        "  a();\n" +
        "});\n" +
        "goog.scope(function() {\n" +
        "  var b = goog.b;\n" +
        "  b();\n" +
        "});",
        "goog.a();\n" +
        "goog.b();");
  }

  // Tests alias replacement in @extends and @implements annotations
  @Test
  public void testScopedAliases_extendsAndImplementsAnnotations_fixesTypes() {
    test(
        "goog.scope(function() {\n" +
        "  var Base = goog.ui.Base;\n" +
        "  var Interface = goog.ui.Interface;\n" +
        "  /**\n" +
        "   * @constructor\n" +
        "   * @extends {Base}\n" +
        "   * @implements {Interface}\n" +
        "   */\n" +
        "  function Sub() {}\n" +
        "});",
        "/**\n" +
        " * @constructor\n" +
        " * @extends {goog.ui.Base}\n" +
        " * @implements {goog.ui.Interface}\n" +
        " */\n" +
        "function Sub() {}");
  }

  // Tests alias replacement in @param and @return annotations
  @Test
  public void testScopedAliases_paramAndReturnAnnotations_fixesTypes() {
    test(
        "goog.scope(function() {\n" +
        "  var Element = goog.dom.Element;\n" +
        "  /**\n" +
        "   * @param {Element} el\n" +
        "   * @return {Element}\n" +
        "   */\n" +
        "  function process(el) {\n" +
        "    return el;\n" +
        "  }\n" +
        "});",
        "/**\n" +
        " * @param {goog.dom.Element} el\n" +
        " * @return {goog.dom.Element}\n" +
        " */\n" +
        "function process(el) {\n" +
        "  return el;\n" +
        "}");
  }

  // Tests alias replacement in @typedef and union types
  @Test
  public void testScopedAliases_typedefAndUnionType_fixesTypes() {
    test(
        "goog.scope(function() {\n" +
        "  var TypeA = goog.TypeA;\n" +
        "  var TypeB = goog.TypeB;\n" +
        "  /** @typedef {TypeA|TypeB} */ var UnionType;\n" +
        "});",
        "/** @typedef {goog.TypeA|goog.TypeB} */ var UnionType;");
  }

  // Tests error when goog.scope is nested inside a block
  @Test
  public void testScopedAliases_nestedInBlock_reportsError() {
    testError(
        "if (true) {\n" +
        "  goog.scope(function() {});\n" +
        "}",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }
}
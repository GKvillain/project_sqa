package com.google.javascript.jscomp;

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

  // Tests replacing a simple alias reference in goog.scope
  public void testProcess_simpleAlias_replacesAlias() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests replacing multiple aliases within a single goog.scope
  public void testProcess_multipleAliases_replacesAllAliases() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var events = goog.events;" +
        "  dom.createElement('div');" +
        "  events.listen();" +
        "});",
        "goog.dom.createElement('div');" +
        "goog.events.listen();");
  }

  // Tests transitive aliases resolution
  public void testProcess_transitiveAlias_resolvesCorrectly() {
    test(
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  var d = g.dom;" +
        "  d.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  // Tests alias usage inside nested functions within goog.scope
  public void testProcess_nestedFunctionInScope_replacesAlias() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  function helper() {" +
        "    dom.createElement('div');" +
        "  }" +
        "  helper();" +
        "});",
        "function helper() {" +
        "  goog.dom.createElement('div');" +
        "}" +
        "helper();");
  }

  // Tests JSDoc type annotation alias replacement
  public void testProcess_jsDocType_updatesTypeName() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  /** @type {Button} */ var b;" +
        "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests JSDoc type annotation with property access on alias
  public void testProcess_jsDocTypeWithProperty_updatesQualifiedName() {
    test(
        "goog.scope(function() {" +
        "  var ui = goog.ui;" +
        "  /** @type {ui.Button} */ var b;" +
        "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests error reporting when a non-alias local variable is declared
  public void testProcess_nonAliasLocal_reportsError() {
    test(
        "goog.scope(function() {" +
        "  var x = 1 + 1;" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests error reporting when an alias is reassigned
  public void testProcess_aliasRedefined_reportsError() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom = goog.events;" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error reporting when goog.scope is called with no arguments
  public void testProcess_badParametersNoArgs_reportsError() {
    test("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope function takes parameters
  public void testProcess_badParametersFunctionWithParam_reportsError() {
    test("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope has a named function argument
  public void testProcess_badParametersNamedFunction_reportsError() {
    test("goog.scope(function named() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error reporting when goog.scope is used in an expression
  public void testProcess_usedImproperlyInVar_reportsError() {
    test("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error reporting when goog.scope references 'this'
  public void testProcess_scopeReferencesThis_reportsError() {
    test(
        "goog.scope(function() {" +
        "  this.x = 1;" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error reporting when goog.scope body contains a return statement
  public void testProcess_scopeUsesReturn_reportsError() {
    test(
        "goog.scope(function() {" +
        "  return;" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error reporting when goog.scope body contains a throw statement
  public void testProcess_scopeUsesThrow_reportsError() {
    test(
        "goog.scope(function() {" +
        "  throw 'error';" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests regular code without goog.scope remains unchanged
  public void testProcess_noScopeBlock_leavesCodeUnchanged() {
    testSame("var x = 1; function foo() { return x; }");
  }

  // Tests multiple independent goog.scope blocks in the same script
  public void testProcess_multipleScopeBlocks_processesBoth() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "});" +
        "goog.scope(function() {" +
        "  var events = goog.events;" +
        "  events.listen();" +
        "});",
        "goog.dom.createElement('div');" +
        "goog.events.listen();");
  }
}
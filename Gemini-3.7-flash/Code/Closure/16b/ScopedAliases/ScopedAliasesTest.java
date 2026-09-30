package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

public class ScopedAliasesTest extends CompilerTestCase {

  private static final String EXTERNS = "var window; var goog = {}; goog.dom = {}; goog.events = {};";
  private PreprocessorSymbolTable preprocessorSymbolTable;

  public ScopedAliasesTest() {
    super(EXTERNS);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.preprocessorSymbolTable = null;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, preprocessorSymbolTable,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias transformation
  @Test
  public void testProcess_basicAlias_inlinesAlias() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  dom.createElement('div');" +
         "});",
         "goog.dom.createElement('div');");
  }

  // Tests multiple aliases in the same scope
  @Test
  public void testProcess_multipleAliases_inlinesAllAliases() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  var events = goog.events;" +
         "  dom.createElement('div');" +
         "  events.listen();" +
         "});",
         "goog.dom.createElement('div');" +
         "goog.events.listen();");
  }

  // Tests transitive alias resolution
  @Test
  public void testProcess_transitiveAlias_inlinesCorrectly() {
    test("goog.scope(function() {" +
         "  var g = goog;" +
         "  var d = g.dom;" +
         "  d.createElement('div');" +
         "});",
         "goog.dom.createElement('div');");
  }

  // Tests JSDoc type annotation aliasing
  @Test
  public void testProcess_jsdocTypeAnnotation_inlinesType() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  /** @type {dom.Element} */ var x;" +
         "});",
         "/** @type {goog.dom.Element} */ var x;");
  }

  // Tests nested JSDoc type annotation aliasing
  @Test
  public void testProcess_nestedJsdocType_inlinesType() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  /** @type {dom.Element.Sub} */ var x;" +
         "});",
         "/** @type {goog.dom.Element.Sub} */ var x;");
  }

  // Tests namespace shadow renaming inside inner scope
  @Test
  public void testProcess_namespaceShadow_renamesShadowedVariable() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  function f() {" +
         "    var goog = 1;" +
         "    dom.createElement('div');" +
         "  }" +
         "});",
         "function f() {" +
         "  var goog$jscomp$1 = 1;" +
         "  goog.dom.createElement('div');" +
         "}");
  }

  // Tests reporting error when goog.scope is used improperly in expression
  @Test
  public void testValidateScopeCall_notExprResult_reportsError() {
    testError("var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests reporting error when goog.scope has no arguments
  @Test
  public void testValidateScopeCall_noArguments_reportsError() {
    testError("goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests reporting error when goog.scope has multiple arguments
  @Test
  public void testValidateScopeCall_multipleArguments_reportsError() {
    testError("goog.scope(function() {}, 1);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests reporting error when goog.scope parameter is not a function
  @Test
  public void testValidateScopeCall_nonFunctionParameter_reportsError() {
    testError("goog.scope(42);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests reporting error when goog.scope function has parameters
  @Test
  public void testValidateScopeCall_functionWithParams_reportsError() {
    testError("goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests reporting error when goog.scope function has a name
  @Test
  public void testValidateScopeCall_namedFunction_reportsError() {
    testError("goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests reporting error when goog.scope body references 'this'
  @Test
  public void testVisit_referencesThis_reportsError() {
    testError("goog.scope(function() { this.x = 1; });",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests reporting error when goog.scope body uses 'return'
  @Test
  public void testVisit_usesReturn_reportsError() {
    testError("goog.scope(function() { return; });",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests reporting error when goog.scope body uses 'throw'
  @Test
  public void testVisit_usesThrow_reportsError() {
    testError("goog.scope(function() { throw 'error'; });",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests reporting error when alias is redefined
  @Test
  public void testVisit_aliasRedefined_reportsError() {
    testError("goog.scope(function() {" +
              "  var dom = goog.dom;" +
              "  dom = goog.events;" +
              "});",
              ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests reporting error when local variable is not an alias
  @Test
  public void testFindAliases_nonAliasLocal_reportsError() {
    testError("goog.scope(function() { var x = 1; });",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests that functions outside goog.scope are preserved
  @Test
  public void testShouldTraverse_globalFunction_preserved() {
    testSame("function globalFn() { var x = 1; }");
  }

  // Tests with PreprocessorSymbolTable enabled
  @Test
  public void testProcess_withPreprocessorSymbolTable_inlinesAlias() {
    this.preprocessorSymbolTable = new PreprocessorSymbolTable(new Node(Token.SCRIPT));
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  dom.createElement('div');" +
         "});",
         "goog.dom.createElement('div');");
  }

  // Tests reporting error when alias definitions form a cycle
  @Test
  public void testFindAliases_aliasCycle_reportsError() {
    testError("goog.scope(function() {" +
              "  var a = b;" +
              "  var b = a;" +
              "});",
              ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  // Tests multiple alias declarations in a single var statement
  @Test
  public void testProcess_multipleVarDeclarations_inlinesAll() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom, events = goog.events;" +
         "  dom.createElement('div');" +
         "  events.listen();" +
         "});",
         "goog.dom.createElement('div');" +
         "goog.events.listen();");
  }

  // Tests JSDoc @param and @return type aliasing
  @Test
  public void testProcess_jsdocParamAndReturn_inlinesTypes() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  /**\n" +
         "   * @param {dom.Element} elem\n" +
         "   * @return {dom.Element}\n" +
         "   */\n" +
         "  goog.dom.find = function(elem) { return elem; };" +
         "});",
         "/**\n" +
         " * @param {goog.dom.Element} elem\n" +
         " * @return {goog.dom.Element}\n" +
         " */\n" +
         "goog.dom.find = function(elem) { return elem; };");
  }

  // Tests type cast expression aliasing
  @Test
  public void testProcess_typeCast_inlinesType() {
    test("goog.scope(function() {" +
         "  var dom = goog.dom;" +
         "  goog.events.listen(/** @type {dom.Element} */ (x));" +
         "});",
         "goog.events.listen(/** @type {goog.dom.Element} */ (x));");
  }
}
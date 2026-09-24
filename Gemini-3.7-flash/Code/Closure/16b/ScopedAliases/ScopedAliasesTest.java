package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ScopedAliases}.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  private static final AliasTransformationHandler NULL_ALIAS_TRANSFORMATION_HANDLER =
      new AliasTransformationHandler() {
        @Override
        public AliasTransformation logAliasTransformation(
            String sourceFile, SourcePosition<AliasTransformation> position) {
          return new AliasTransformation() {
            @Override
            public void addAlias(String alias, String qualifiedName) {}
          };
        }
      };

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, null, NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias expansion and scope unwrap
  @Test
  public void testProcess_basicAlias_expandsQualifiedName() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  var DIV = dom.TagName.DIV;\n"
            + "  dom.createElement(DIV);\n"
            + "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

  // Tests transitive aliases across multiple declarations
  @Test
  public void testProcess_transitiveAlias_expandsCorrectly() {
    test(
        "goog.scope(function() {\n"
            + "  var g = goog;\n"
            + "  var d = g.dom;\n"
            + "  d.createElement('div');\n"
            + "});",
        "goog.dom.createElement('div');");
  }

  // Tests multiple aliases in single var statement
  @Test
  public void testProcess_multipleVarsInSingleStatement_removesCorrectly() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom, events = goog.events;\n"
            + "  dom.createElement('div');\n"
            + "  events.listen();\n"
            + "});",
        "goog.dom.createElement('div');\n"
            + "goog.events.listen();");
  }

  // Tests error when goog.scope is not in an expression statement
  @Test
  public void testValidateScopeCall_notExprResult_reportsError() {
    testError(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests error when goog.scope has no parameters
  @Test
  public void testValidateScopeCall_noParams_reportsError() {
    testError(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope receives a non-function parameter
  @Test
  public void testValidateScopeCall_nonFunctionParam_reportsError() {
    testError(
        "goog.scope(42);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope function takes parameters
  @Test
  public void testValidateScopeCall_functionWithParams_reportsError() {
    testError(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when goog.scope function is named
  @Test
  public void testValidateScopeCall_namedFunction_reportsError() {
    testError(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests error when body references this
  @Test
  public void testVisit_referencesThis_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  this.foo = null;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests error when body uses return
  @Test
  public void testVisit_usesReturn_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  return;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests error when body uses throw
  @Test
  public void testVisit_usesThrow_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  throw 'error';\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests error when alias is reassigned
  @Test
  public void testVisit_redefinedAlias_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var d = goog.dom;\n"
            + "  d = goog.events;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests error when variable in scope is not an alias
  @Test
  public void testFindAliases_nonAliasLocal_reportsError() {
    testError(
        "goog.scope(function() {\n"
            + "  var x = 1;\n"
            + "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests shadowing of namespace in inner function
  @Test
  public void testRenameNamespaceShadows_shadowsNamespace_renamesShadow() {
    test(
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function foo() {\n"
            + "    var goog = 1;\n"
            + "    return goog;\n"
            + "  }\n"
            + "  dom.createElement('div');\n"
            + "});",
        "function foo() {\n"
            + "  var goog$$module$1 = 1;\n"
            + "  return goog$$module$1;\n"
            + "}\n"
            + "goog.dom.createElement('div');");
  }

  // Tests JSDoc type transformation for aliased type names
  @Test
  public void testFixTypeNode_jsdocTypeReference_updatesTypeString() {
    test(
        "goog.scope(function() {\n"
            + "  var Button = goog.ui.Button;\n"
            + "  /** @type {Button} */\n"
            + "  var b;\n"
            + "});",
        "/** @type {goog.ui.Button} */\n"
            + "var b;");
  }

  // Tests JSDoc type transformation for aliased subtype references
  @Test
  public void testFixTypeNode_jsdocSubtypeReference_updatesTypeString() {
    test(
        "goog.scope(function() {\n"
            + "  var ui = goog.ui;\n"
            + "  /** @type {ui.Button} */\n"
            + "  var b;\n"
            + "});",
        "/** @type {goog.ui.Button} */\n"
            + "var b;");
  }

  // Tests code without goog.scope is left unchanged
  @Test
  public void testProcess_noScope_noChange() {
    testSame("var x = 10; function foo() { return x; }");
  }
}
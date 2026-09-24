package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Before;
import org.junit.Test;

public class ScopedAliasesTest extends CompilerTestCase {

  private static final String EXTERNS = "var window;";

  private AliasTransformationHandler transformationHandler =
      CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER;

  public ScopedAliasesTest() {
    super(EXTERNS);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    transformationHandler = CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, null, transformationHandler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic alias replacement in goog.scope
  @Test
  public void testProcess_basicAlias_replacesAlias() {
    test(
        "goog.scope(function() { var x = a.b.c; x(); });",
        "a.b.c();");
  }

  // Tests transitive alias resolution
  @Test
  public void testProcess_transitiveAlias_replacesTransitively() {
    test(
        "goog.scope(function() { var x = a.b; var y = x.c; y(); });",
        "a.b.c();");
  }

  // Tests multiple alias declarations in a single var statement
  @Test
  public void testProcess_multipleAliasesInVar_replacesAll() {
    test(
        "goog.scope(function() { var x = a.b, y = c.d; x(); y(); });",
        "a.b(); c.d();");
  }

  // Tests non-alias local variable error (primitive value)
  @Test
  public void testProcess_nonAliasLocalValue_reportsError() {
    testError(
        "goog.scope(function() { var x = 1; });",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests non-alias uninitialized local variable error
  @Test
  public void testProcess_uninitializedLocalVar_reportsError() {
    testError(
        "goog.scope(function() { var x; });",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests non-alias function declaration in goog.scope
  @Test
  public void testProcess_functionDeclarationInScope_reportsError() {
    testError(
        "goog.scope(function() { function foo() {} });",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // Tests goog.scope used improperly inside an expression
  @Test
  public void testProcess_googScopeInExpression_reportsError() {
    testError(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  // Tests goog.scope with missing parameters
  @Test
  public void testProcess_missingParameters_reportsError() {
    testError(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests goog.scope with non-function parameter
  @Test
  public void testProcess_nonFunctionParameter_reportsError() {
    testError(
        "goog.scope(123);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests goog.scope with function parameter containing arguments
  @Test
  public void testProcess_functionWithParameters_reportsError() {
    testError(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests goog.scope with named function
  @Test
  public void testProcess_namedFunction_reportsError() {
    testError(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  // Tests 'this' reference in goog.scope
  @Test
  public void testProcess_referencesThis_reportsError() {
    testError(
        "goog.scope(function() { this.foo(); });",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  // Tests 'return' statement in goog.scope
  @Test
  public void testProcess_usesReturn_reportsError() {
    testError(
        "goog.scope(function() { return; });",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  // Tests 'throw' statement in goog.scope
  @Test
  public void testProcess_usesThrow_reportsError() {
    testError(
        "goog.scope(function() { throw 'error'; });",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  // Tests alias re-assignment in goog.scope
  @Test
  public void testProcess_aliasRedefined_reportsError() {
    testError(
        "goog.scope(function() { var x = a.b; x = c.d; });",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  // Tests type annotation fix with alias
  @Test
  public void testProcess_jsdocTypeAlias_fixesTypeNode() {
    test(
        "goog.scope(function() { var Button = goog.ui.Button; /** @type {Button} */ var b; });",
        "/** @type {goog.ui.Button} */ var b;");
  }

  // Tests sub-property type annotation fix with alias
  @Test
  public void testProcess_jsdocSubTypeAlias_fixesTypeNode() {
    test(
        "goog.scope(function() { var B = goog.ui.Button; /** @type {B.Sub} */ var b; });",
        "/** @type {goog.ui.Button.Sub} */ var b;");
  }

  // Tests inner function scope shadowing an alias
  @Test
  public void testProcess_innerFunctionScopeShadowing_preservesInnerScope() {
    test(
        "goog.scope(function() { var x = a.b; var f = function() { var x = 1; return x; }; });",
        "var f = function() { var x = 1; return x; };");
  }

  // Tests regular code without goog.scope remains unchanged
  @Test
  public void testProcess_noGoogScope_leavesCodeUnchanged() {
    testSame("var x = 1; function foo() { return x; }");
  }
}
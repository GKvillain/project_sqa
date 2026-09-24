package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link CollapseProperties}.
 */
public class CollapsePropertiesTest extends CompilerTestCase {

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, collapsePropertiesOnExternTypes, inlineAliases);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    collapsePropertiesOnExternTypes = false;
    inlineAliases = true;
  }

  // Tests collapsing a simple global object property assignment
  @Test
  public void testProcess_simpleGlobalProperty_collapsesToFlattenedVar() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  // Tests collapsing nested object literal properties
  @Test
  public void testProcess_nestedObjectLiteral_collapsesNestedProperties() {
    test("var a = {b: {c: 1}};", "var a$b$c = 1;");
  }

  // Tests collapsing properties added to a function namespace
  @Test
  public void testProcess_functionNamespaceProperty_collapsesProperty() {
    test("var a = function() {}; a.b = 1;",
         "var a = function() {}; var a$b = 1;");
  }

  // Tests inlining local alias of a collapsed property
  @Test
  public void testProcess_localAliasOfProperty_inlinesAlias() {
    test("var a = {b: 1}; function f() { var x = a.b; return x; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  // Tests inlining local alias when child properties exist (Defects4J Closure 130 regression)
  @Test
  public void testProcess_localAliasWithChildProperties_inlinesAndCollapsesChildren() {
    test(
        "var a = {b: {c: 1}};" +
        "function f() {" +
        "  var x = a.b;" +
        "  return x.c;" +
        "}",
        "var a$b$c = 1;" +
        "function f() {" +
        "  var x = null;" +
        "  return a$b$c;" +
        "}");
  }

  // Tests that aliased namespace prevents collapsing and triggers warning
  @Test
  public void testCheckNamespaces_aliasedNamespace_reportsUnsafeNamespaceWarning() {
    test("var a = {b: 1}; var c = a;",
         "var a = {b: 1}; var c = a;",
         null,
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests that redefining an initialized namespace produces a warning
  @Test
  public void testCheckNamespaces_redefinedNamespace_reportsRedefinitionWarning() {
    test("var a = {}; a = {};",
         "var a = {}; a = {};",
         null,
         CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests warning on dangerous use of 'this' in a collapsed static method
  @Test
  public void testCheckForHosedThisReferences_unsafeThisInFunction_reportsWarning() {
    test("var a = {}; a.b = function() { return this.c; };",
         "var a$b = function() { return this.c; };",
         null,
         CollapseProperties.UNSAFE_THIS);
  }

  // Tests that constructor functions with 'this' do not produce UNSAFE_THIS warning
  @Test
  public void testCheckForHosedThisReferences_constructorWithThis_noWarning() {
    test("var a = {}; /** @constructor */ a.b = function() { this.c = 1; };",
         "/** @constructor */ var a$b = function() { this.c = 1; };");
  }

  // Tests that properties containing dollar signs are encoded properly
  @Test
  public void testAppendPropForAlias_propertyWithDollarSign_encodesDollarSign() {
    test("var a = {}; a['$b'] = 1;", "var a = {}; a['$b'] = 1;");
    test("var a = {}; a.$b = 1;", "var a$$0b = 1;");
  }

  // Tests properties initialized in a local scope get stub declarations
  @Test
  public void testAddStubsForUndeclaredProperties_lateLocalAssignment_createsVarStub() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a$b; function f() { a$b = 1; }");
  }

  // Tests complex assignment chain maintains correct collapsed variable
  @Test
  public void testFlattenReferencesTo_complexAssignment_collapsesCorrectly() {
    test("var a = {}; var b = (a.c = 1);",
         "var a$c; var b = (a$c = 1);");
  }

  // Tests that bracket property access is not collapsed
  @Test
  public void testProcess_bracketAccess_doesNotCollapse() {
    testSame("var a = {}; a['b'] = 1;");
  }

  // Tests that inlineAliases disabled flag preserves local aliases
  @Test
  public void testProcess_inlineAliasesDisabled_doesNotInlineLocalAlias() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var x = a.b; return x; }");
  }
}
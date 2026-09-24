package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CollapseProperties}.
 */
public class CollapsePropertiesTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; function alert(s) {} function custom() {}";

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    super(EXTERNS);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    collapsePropertiesOnExternTypes = false;
    inlineAliases = true;
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

  // Tests basic object literal property flattening
  @Test
  public void testProcess_basicObjectLiteral_collapsesProperties() {
    test("var a = {}; a.b = 1; a.c = 2;",
         "var a$b = 1; var a$c = 2;");
  }

  // Tests nested object literal collapsing
  @Test
  public void testProcess_nestedObjectLiteral_collapsesRecursively() {
    test("var a = {b: {c: 1}};",
         "var a$b$c = 1;");
  }

  // Tests function property collapsing
  @Test
  public void testProcess_functionWithProperties_collapsesProperties() {
    test("function a() {} a.b = 1;",
         "function a() {} var a$b = 1;");
  }

  // Tests property defined in a local scope creates a stub
  @Test
  public void testProcess_propertySetInLocalScope_createsStub() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a$b; function f() { a$b = 1; }");
  }

  // Tests alias inlining and subsequent collapsing
  @Test
  public void testProcess_localAlias_inlinesAndCollapses() {
    test("var a = {b: 1}; function f() { var x = a; return x.b; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  // Tests namespace aliasing warning
  @Test
  public void testProcess_namespaceAliasing_reportsWarning() {
    testWarning("/** @constructor */ function Foo() {} Foo.bar = 1; var x = Foo;",
                CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests namespace redefinition warning
  @Test
  public void testProcess_namespaceRedefinition_reportsWarning() {
    testWarning("/** @constructor */ function Foo() {} Foo = function() {};",
                CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests unsafe this warning in static collapsed function
  @Test
  public void testProcess_unsafeThis_reportsWarning() {
    testWarning("var a = {}; a.b = function() { return this.x; };",
                CollapseProperties.UNSAFE_THIS);
  }

  // Tests property names containing dollar signs
  @Test
  public void testProcess_propertyWithDollarSign_encodesDollarSign() {
    test("var a = {}; a['$b'] = 1; a.$b = 2;",
         "var a$$0b = 2; a['$b'] = 1;");
  }

  // Tests bracket property access does not collapse
  @Test
  public void testProcess_bracketAccess_doesNotCollapse() {
    testSame("var a = {}; a['b'] = 1;");
  }

  // Tests non-identifier object literal keys
  @Test
  public void testProcess_numericOrNonIdentKeys_preservesSideEffects() {
    test("var a = {1: 'num', 'foo-bar': 'hyphen'};",
         "var a$1 = 'num'; var a$2 = 'hyphen';");
  }

  // Tests complex assignment twin references (Defects4J 156 regression)
  @Test
  public void testProcess_complexAssignment_collapsesCorrectly() {
    test("var a = {}; var b; b = a.c = 1;",
         "var a$c; var b; b = a$c = 1;");
  }

  // Tests collapsing properties on extern types when enabled
  @Test
  public void testProcess_externTypesEnabled_collapsesExternProperties() {
    collapsePropertiesOnExternTypes = true;
    test("custom.b = 1; var x = custom.b;",
         "var custom$b = 1; var x = custom$b;");
  }

  // Tests disabling inlineAliases prevents alias inlining
  @Test
  public void testProcess_inlineAliasesDisabled_doesNotInline() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var x = a; return x.b; }");
  }
}
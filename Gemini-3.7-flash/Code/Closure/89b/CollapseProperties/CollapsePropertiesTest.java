package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CollapseProperties}.
 */
public class CollapsePropertiesTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; function alert(x) {} var String;";

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
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, collapsePropertiesOnExternTypes, inlineAliases);
  }

  // Tests collapsing simple object property
  @Test
  public void testProcess_simpleProperty_collapsesToFlattenedName() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  // Tests collapsing nested object properties
  @Test
  public void testProcess_nestedProperties_collapsesAllLevels() {
    test("var a = {}; a.b = {}; a.b.c = 1;", "var a$b$c = 1;");
  }

  // Tests collapsing object literal keys
  @Test
  public void testProcess_objectLiteralDeclaration_collapsesKeys() {
    test("var a = {b: 1, c: 2};", "var a$b = 1; var a$c = 2;");
  }

  // Tests function property collapsing
  @Test
  public void testProcess_functionProperty_collapsesProperty() {
    test("function a() {} a.b = 1; a.b();", "function a() {} var a$b = 1; a$b();");
  }

  // Tests namespace aliasing warning
  @Test
  public void testProcess_aliasedNamespace_reportsUnsafeNamespaceWarning() {
    test("var a = {}; var c = a; c.b = 1;",
         "var a = {}; var c = a; c.b = 1;",
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests namespace redefinition warning
  @Test
  public void testProcess_redefinedNamespace_reportsNamespaceRedefinedWarning() {
    test("var a = {}; a = {}; a.b = 1;",
         "var a = {}; a = {}; a.b = 1;",
         CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests dangerous use of 'this' in static method warning
  @Test
  public void testProcess_unsafeThis_reportsUnsafeThisWarning() {
    test("var a = {}; a.b = function() { return this.c; };",
         "var a$b = function() { return this.c; };",
         CollapseProperties.UNSAFE_THIS);
  }

  // Tests constructor with 'this' does not report warning
  @Test
  public void testProcess_constructorWithThis_noWarning() {
    test("var a = {}; /** @constructor */ a.b = function() { this.c = 1; };",
         "var a$b = function() { this.c = 1; };");
  }

  // Tests property set in local scope creates stub variable
  @Test
  public void testProcess_localScopePropertyAssignment_createsStub() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a$b; function f() { a$b = 1; }");
  }

  // Tests complex assignment creates stub variable
  @Test
  public void testProcess_complexAssign_createsStub() {
    test("var a = {}; var c = (a.b = 1);",
         "var a$b; var c = (a$b = 1);");
  }

  // Tests property name with dollar sign escaping
  @Test
  public void testProcess_propertyNameWithDollar_escapesCorrectly() {
    test("var a = {}; a['$b'] = 1; a.$b;", "var a$$0b = 1; a$$0b;");
  }

  // Tests inlining local alias
  @Test
  public void testProcess_inlineLocalAlias_inlinesAndCollapses() {
    test("var a = {b: 1}; function f() { var c = a; return c.b; }",
         "var a$b = 1; function f() { var c = null; return a$b; }");
  }

  // Tests disabling alias inlining
  @Test
  public void testProcess_inlineAliasesDisabled_doesNotInline() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var c = a; return c.b; }");
  }

  // Tests non-collapsible property on non-initialized object
  @Test
  public void testProcess_propertyAddedToUninitializedNamespace_doesNotCrash() {
    testSame("var a; a.b = 1;");
  }

  // Tests global alias assignment of property on object
  @Test
  public void testProcess_aliasedSubProperty_preservesCorrectness() {
    test("var a = {}; a.b = {}; a.b.c = function() {}; var f = a.b.c;",
         "var a$b$c = function() {}; var f = a$b$c;");
  }

  // Tests collapsing properties on extern types when enabled
  @Test
  public void testProcess_collapsePropertiesOnExternTypes_collapsesStaticProp() {
    collapsePropertiesOnExternTypes = true;
    test("String.foo = 1; var x = String.foo;",
         "var String$foo = 1; var x = String$foo;");
  }
}
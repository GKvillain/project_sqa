package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CollapseProperties (Defects4J Closure 156b).
 */
public class CollapsePropertiesTest extends CompilerTestCase {

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    super();
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
    return new CollapseProperties(compiler, collapsePropertiesOnExternTypes, inlineAliases);
  }

  // Tests collapsing simple object property assignment
  @Test
  public void testProcess_simpleObjectProperty_collapsesToVariable() {
    test("var a = {}; a.b = 1; var c = a.b;",
         "var a$b = 1; var c = a$b;");
  }

  // Tests collapsing nested namespace properties
  @Test
  public void testProcess_nestedNamespace_collapsesHierarchy() {
    test("var a = {}; a.b = {}; a.b.c = 1; var d = a.b.c;",
         "var a$b$c = 1; var d = a$b$c;");
  }

  // Tests collapsing object literal keys
  @Test
  public void testProcess_objectLiteralWithKeys_collapsesKeys() {
    test("var a = {b: 1, c: 2}; var d = a.b + a.c;",
         "var a$b = 1; var a$c = 2; var d = a$b + a$c;");
  }

  // Tests collapsing functions attached to namespaces
  @Test
  public void testProcess_functionProperty_collapsesFunction() {
    test("var a = {}; a.b = function() { return 42; }; a.b();",
         "var a$b = function() { return 42; }; a$b();");
  }

  // Tests aliasing warning and non-collapsing behavior for unsafe namespace aliases
  @Test
  public void testProcess_aliasedNamespace_warnsAndDoesNotCollapse() {
    test("var a = {}; a.b = 1; var c = a; c.b = 2;",
         "var a = {}; a.b = 1; var c = a; c.b = 2;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests namespace redefinition warning
  @Test
  public void testProcess_namespaceRedefined_warns() {
    test("var a = {}; a.b = {}; a = {};",
         "var a = {}; a.b = {}; a = {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests property name with dollar sign escaping
  @Test
  public void testProcess_propertyNameWithDollar_escapesProperly() {
    test("var a = {}; a['$b'] = 1;",
         "var a = {}; a['$b'] = 1;");
    test("var a = {}; a.$b = 1; var c = a.$b;",
         "var a$$0b = 1; var c = a$$0b;");
  }

  // Tests unsafe 'this' warning in collapsed static methods
  @Test
  public void testProcess_staticMethodUsingThis_warnsUnsafeThis() {
    test("var a = {}; a.b = function() { return this.c; };",
         "var a$b = function() { return this.c; };",
         null, CollapseProperties.UNSAFE_THIS);
  }

  // Tests constructor function with 'this' does not trigger unsafe this warning
  @Test
  public void testProcess_constructorUsingThis_noWarning() {
    test("var a = {}; /** @constructor */ a.b = function() { this.c = 1; };",
         "var a$b = function() { this.c = 1; };");
  }

  // Tests non-collapsible object literal with getter/setter
  @Test
  public void testProcess_getterSetterInObjLit_doesNotCollapseGetter() {
    testSame("var a = { get b() { return 1; } };");
  }

  // Tests collapsing properties added in local scope stubbing
  @Test
  public void testProcess_propertyAddedInLocalScope_createsStub() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a = {}; var a$b; function f() { a$b = 1; }");
  }

  // Tests twin reference in complex assignment
  @Test
  public void testProcess_complexAssignment_collapsesWithStub() {
    test("var a = {}; var b; b = a.c = 1; var d = a.c;",
         "var a$c; var b; b = a$c = 1; var d = a$c;");
  }

  // Tests inlining local alias of global namespace
  @Test
  public void testProcess_localAliasInlining_inlinesCorrectly() {
    inlineAliases = true;
    test("var a = {}; a.b = 1; function f() { var x = a; return x.b; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  // Tests extern types property collapsing disabled by default
  @Test
  public void testProcess_externTypePropertyDisabled_doesNotCollapseExtern() {
    collapsePropertiesOnExternTypes = false;
    testSame("String.prototype.foo = function() {};");
  }

  // Tests non-identifier object literal keys
  @Test
  public void testProcess_nonIdentifierObjLitKey_handledProperly() {
    test("var a = { '123': 456 };",
         "var a$1 = 456;");
  }

  // Tests function declaration namespace property collapsing
  @Test
  public void testProcess_functionDeclarationNamespace_collapsesProperty() {
    test("function a() {} a.b = 1; var c = a.b;",
         "function a() {} var a$b = 1; var c = a$b;");
  }

  // Tests passing namespace as an argument warns and prevents collapsing
  @Test
  public void testProcess_namespacePassedAsArgument_warnsUnsafeNamespace() {
    test("var a = {}; a.b = 1; f(a);",
         "var a = {}; a.b = 1; f(a);",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests collapsing enum properties
  @Test
  public void testProcess_enumObject_collapsesEnumProperties() {
    test("/** @enum {number} */ var E = {A: 1, B: 2}; var x = E.A;",
         "var E$A = 1; var E$B = 2; var x = E$A;");
  }

  // Tests chained property assignment on namespace
  @Test
  public void testProcess_chainedPropertyAssignment_collapsesBoth() {
    test("var a = {}; a.b = a.c = 1; var d = a.b; var e = a.c;",
         "var a$b; var a$c; a$b = a$c = 1; var d = a$b; var e = a$c;");
  }

  // Tests multiple namespace variable declarations in a single var statement
  @Test
  public void testProcess_multipleVarDeclarations_collapsesBothNamespaces() {
    test("var a = {}, b = {}; a.x = 1; b.y = 2; var c = a.x + b.y;",
         "var a$x = 1; var b$y = 2; var c = a$x + b$y;");
  }

  // Tests prototype property on collapsed namespace constructor
  @Test
  public void testProcess_prototypeOnCollapsedConstructor_leavesPrototypeIntact() {
    test("var a = {}; /** @constructor */ a.b = function() {}; a.b.prototype.c = function() {};",
         "var a$b = function() {}; a$b.prototype.c = function() {};");
  }

  // Tests inlineAliases set to false disables local alias inlining
  @Test
  public void testProcess_inlineAliasesDisabled_doesNotInlineLocalAlias() {
    inlineAliases = false;
    test("var a = {}; a.b = 1; function f() { var x = a; return x.b; }",
         "var a = {}; a.b = 1; function f() { var x = a; return x.b; }",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests collapsing properties with null and undefined initial values
  @Test
  public void testProcess_nullAndUndefinedPropertyValues_collapsesSuccessfully() {
    test("var a = {}; a.b = null; a.c = undefined; var d = a.b; var e = a.c;",
         "var a$b = null; var a$c = undefined; var d = a$b; var e = a$c;");
  }

  // Tests conditional assignment to namespace property creates a stub declaration
  @Test
  public void testProcess_conditionalPropertyAssignment_collapsesWithStub() {
    test("var a = {}; if (true) { a.b = 1; } var c = a.b;",
         "var a = {}; var a$b; if (true) { a$b = 1; } var c = a$b;");
  }

  // Tests nested function assignment on namespace
  @Test
  public void testProcess_nestedFunctionOnNamespace_collapsesChain() {
    test("var a = {}; a.b = function() {}; a.b.c = function() { return 1; }; var d = a.b.c();",
         "var a$b = function() {}; var a$b$c = function() { return 1; }; var d = a$b$c();");
  }
}
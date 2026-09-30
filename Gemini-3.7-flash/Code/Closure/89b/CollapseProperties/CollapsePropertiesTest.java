package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class CollapsePropertiesTest extends CompilerTestCase {

  private static final String EXTERNS = "var window; function alert(s) {}";

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, collapsePropertiesOnExternTypes, inlineAliases);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    collapsePropertiesOnExternTypes = false;
    inlineAliases = true;
  }

  // Tests collapsing basic property assignment on an object literal
  @Test
  public void testCollapseBasicProperty_collapsesToVar() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  // Tests collapsing nested properties across multiple levels
  @Test
  public void testCollapseNestedProperty_collapsesToVars() {
    test("var a = {}; a.b = {}; a.b.c = 1;", "var a$b$c = 1;");
  }

  // Tests collapsing object literal keys into distinct variables
  @Test
  public void testCollapseObjectLit_collapsesKeys() {
    test("var a = {b: 1, c: 2};", "var a$b = 1; var a$c = 2;");
  }

  // Tests collapsing nested object literal declarations
  @Test
  public void testCollapseNestedObjectLit_collapsesNestedKeys() {
    test("var a = {b: {c: 1}};", "var a$b$c = 1;");
  }

  // Tests collapsing property attached to a function declaration
  @Test
  public void testCollapseFunctionProperty_collapsesProperty() {
    test("function a() {} a.b = 1;", "function a() {} var a$b = 1;");
  }

  // Tests collapsing property of a function assigned to a namespace
  @Test
  public void testCollapseFunctionWithProps_collapsesNestedFunctionProperty() {
    test("var a = {}; a.b = function() {}; a.b.c = 1;",
         "var a$b = function() {}; var a$b$c = 1;");
  }

  // Tests complex assignment creates stub variable before statement
  @Test
  public void testComplexAssignment_createsStubAndAssigns() {
    test("var x = {}; var y = (x.a = 1);", "var x$a; var y = (x$a = 1);");
  }

  // Tests warning on namespace redefinition
  @Test
  public void testNamespaceRedefined_reportsWarning() {
    testWarning("var a = {}; a = {};", CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests warning when an incomplete alias is created for a namespace
  @Test
  public void testNamespaceAliased_reportsWarning() {
    testWarning("var a = {}; var b = a;", CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests warning when 'this' is used in collapsed static function without @this or @constructor
  @Test
  public void testUnsafeThis_reportsWarning() {
    testWarning("var a = {}; a.b = function() { return this.x; };",
                CollapseProperties.UNSAFE_THIS);
  }

  // Tests safe use of 'this' in constructor does not trigger warning
  @Test
  public void testSafeThisInConstructor_noWarning() {
    test("var a = {}; /** @constructor */ a.b = function() { this.x = 1; };",
         "var a$b = function() { this.x = 1; };");
  }

  // Tests property name with dollar sign is escaped properly
  @Test
  public void testPropertyWithDollarSign_encodesDollar() {
    test("var a = {}; a.b$c = 1;", "var a$b$0c = 1;");
  }

  // Tests inlining of local aliases to collapsible properties
  @Test
  public void testInlineLocalAlias_inlinesAlias() {
    test("var a = {b: 1}; function f() { var x = a; return x.b; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  // Tests property set in local scope generates variable stub at global declaration
  @Test
  public void testLatePropertyInLocalScope_createsStub() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a$b; function f() { a$b = 1; }");
  }

  // Tests twin references in chained assignment
  @Test
  public void testTwinReference_collapsesCorrectly() {
    test("var a = {}; var b = {}; a.c = b.c = 1;",
         "var b$c; var a$c = b$c = 1;");
  }

  // Tests nested function hierarchy with properties
  @Test
  public void testNestedFunctionHierarchy_collapsesDescendantProperties() {
    test("var a = {}; a.b = function() {}; a.b.c = function() {}; a.b.c.d = 2;",
         "var a$b = function() {}; var a$b$c = function() {}; var a$b$c$d = 2;");
  }

  // Tests enum property collapsing
  @Test
  public void testEnumProperty_collapsesValues() {
    test("/** @enum {number} */ var E = {A: 1, B: 2}; var x = E.A;",
         "var E$A = 1; var E$B = 2; var x = E$A;");
  }

  // Tests bracket access prevents property collapsing
  @Test
  public void testBracketAccess_preventsCollapsing() {
    testSame("var a = {}; a['b'] = 1;");
  }

  // Tests that prototype properties are not collapsed into global variables
  @Test
  public void testPrototypeProperty_notCollapsed() {
    testSame("function Foo() {} Foo.prototype.bar = function() {};");
  }

  // Tests delete operator on property prevents collapsing
  @Test
  public void testDeleteProperty_preventsCollapsing() {
    testSame("var a = {}; a.b = 1; delete a.b;");
  }

  // Tests multiple properties on the same object are collapsed into distinct vars
  @Test
  public void testMultipleProperties_collapsesAll() {
    test("var a = {}; a.b = 1; a.c = 2; a.d = 3;",
         "var a$b = 1; var a$c = 2; var a$d = 3;");
  }

  // Tests property assignment inside a conditional block creates a stub at top level
  @Test
  public void testConditionalPropertyAssignment_createsStub() {
    test("var a = {}; if (true) { a.b = 1; }",
         "var a$b; if (true) { a$b = 1; }");
  }

  // Tests that inlineAliases set to false does not inline local namespace aliases
  @Test
  public void testInlineAliasesDisabled_doesNotInlineAlias() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var x = a; return x.b; }");
  }

  // Tests collapsing properties on extern types when enabled
  @Test
  public void testCollapsePropertiesOnExternTypes_enabled() {
    collapsePropertiesOnExternTypes = true;
    test("window.a = 1;", "var window$a = 1;");
  }

  // Tests object literal with method shorthand / function expressions
  @Test
  public void testObjectLitFunctionProperty_collapsesToFunction() {
    test("var a = {b: function() { return 1; }};",
         "var a$b = function() { return 1; };");
  }

  // Tests property access on an uncollapsible object leaves references intact
  @Test
  public void testUncollapsibleObjectAccess_preservesAccess() {
    test("var a = {}; var b = a; a.c = 1; alert(b.c);",
         "var a = {}; var b = a; a.c = 1; alert(b.c);",
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }
}
package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class CollapsePropertiesTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; function alert(s) {} function setTimeout(fn, ms) {}";

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    super(EXTERNS);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
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

  // Tests collapsing a simple property assignment
  @Test
  public void testProcess_simpleProperty_collapsesToVar() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  // Tests collapsing nested properties and namespaces
  @Test
  public void testProcess_nestedProperties_collapsesToFlattenedVar() {
    test("var a = {}; a.b = {}; a.b.c = 1;", "var a$b$c = 1;");
  }

  // Tests collapsing properties initialized via object literals
  @Test
  public void testProcess_objectLiteralKeys_collapsesKeysToVars() {
    test("var a = {b: 1, c: 2};", "var a$b = 1; var a$c = 2;");
  }

  // Tests nested object literal collapsing
  @Test
  public void testProcess_nestedObjectLiterals_collapsesAllLevels() {
    test("var a = {b: {c: 1}};", "var a$b$c = 1;");
  }

  // Tests property access on functions
  @Test
  public void testProcess_functionWithProperties_collapsesStaticProperty() {
    test("var a = function() {}; a.b = 1;", "var a = function() {}; var a$b = 1;");
  }

  // Tests warning when namespace is aliased unsafely
  @Test
  public void testProcess_namespaceAliased_reportsUnsafeNamespaceWarning() {
    test("var a = {}; var b = a; a.c = 1;",
        "var a = {}; var b = a; a.c = 1;",
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // Tests warning when namespace is redefined
  @Test
  public void testProcess_namespaceRedefined_reportsNamespaceRedefinedWarning() {
    test("var a = {}; a = {}; a.b = 1;",
        "var a = {}; a = {}; a.b = 1;",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // Tests warning when 'this' is dangerously referenced in static method
  @Test
  public void testProcess_unsafeThisInStaticMethod_reportsUnsafeThisWarning() {
    test("var a = {}; a.b = function() { return this.c; };",
        "var a$b = function() { return this.c; };",
        CollapseProperties.UNSAFE_THIS);
  }

  // Tests encoding of '$' in property names to prevent collision
  @Test
  public void testProcess_propertyNameWithDollarSign_escapesDollarSign() {
    test("var a = {}; a.$b = 1;", "var a$$0b = 1;");
  }

  // Tests inlining of local aliases for global names
  @Test
  public void testProcess_localAlias_inlinesAliasAndCollapses() {
    test("var a = {b: 1}; function f() { var x = a.b; return x; }",
        "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  // Tests regression where aliasing 'arguments' in local scope should not inline across inner functions
  @Test
  public void testProcess_argumentsAliasedInLocalScope_doesNotInliningIncorrectly() {
    testSame("function f() { var args = arguments; setTimeout(function() { alert(args); }, 0); }");
  }

  // Tests stub variable generation for undeclared properties assigned in local scope
  @Test
  public void testProcess_undeclaredPropertyAssignedInLocalScope_createsVarStub() {
    test("var a = {}; function f() { a.b = 1; }",
        "var a$b; var a = {}; function f() { a$b = 1; }");
  }

  // Tests property access inside complex assignment (twin references)
  @Test
  public void testProcess_complexAssignment_createsVarStubBeforeStatement() {
    test("var a = {}; var b = (a.c = 1);",
        "var a$c; var a = {}; var b = (a$c = 1);");
  }

  // Tests when inlineAliases is disabled
  @Test
  public void testProcess_inlineAliasesDisabled_doesNotInlineLocalAlias() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var x = a.b; return x; }");
  }

  // Tests when collapsePropertiesOnExternTypes is enabled
  @Test
  public void testProcess_collapseOnExternTypesEnabled_processesWithoutError() {
    collapsePropertiesOnExternTypes = true;
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  // Tests bracket access prevents property collapsing on the namespace
  @Test
  public void testProcess_bracketAccess_preventsCollapsing() {
    testSame("var a = {}; a['b'] = 1;");
  }

  // Tests property reassignment
  @Test
  public void testProcess_propertyReassigned_collapsesBothAssignments() {
    test("var a = {}; a.b = 1; a.b = 2;", "var a$b = 1; a$b = 2;");
  }

  // Tests chained assignment across two namespaces
  @Test
  public void testProcess_chainedAssignment_collapsesBothProperties() {
    test("var a = {}; var b = {}; a.x = b.y = 1;",
        "var a$x; var b$y; a$x = b$y = 1;");
  }

  // Tests constructor with prototype method
  @Test
  public void testProcess_constructorPrototype_collapsesConstructorAndPreservesPrototype() {
    test("var a = {}; a.b = function() {}; a.b.prototype.c = 1;",
        "var a$b = function() {}; a$b.prototype.c = 1;");
  }

  // Tests increment and decrement operations on collapsed properties
  @Test
  public void testProcess_incrementDecrement_collapsesCorrectly() {
    test("var a = {}; a.b = 1; a.b++; --a.b;",
        "var a$b = 1; a$b++; --a$b;");
  }

  // Tests conditional assignment to property
  @Test
  public void testProcess_conditionalAssignment_createsVarStub() {
    test("var a = {}; if (true) { a.b = 1; } else { a.b = 2; }",
        "var a$b; var a = {}; if (true) { a$b = 1; } else { a$b = 2; }");
  }

  // Tests reading a property before its assignment
  @Test
  public void testProcess_readBeforeAssignment_createsVarStub() {
    test("var a = {}; var x = a.b; a.b = 1;",
        "var a$b; var a = {}; var x = a$b; a$b = 1;");
  }

  // Tests passing collapsed property as a function argument
  @Test
  public void testProcess_propertyAsFunctionArgument_collapsesAndPassesVariable() {
    test("var a = {b: 1}; alert(a.b);",
        "var a$b = 1; alert(a$b);");
  }

  // Tests nested object literals mixed with direct property assignments
  @Test
  public void testProcess_nestedLiteralWithAssignment_collapsesAll() {
    test("var a = {b: {}}; a.b.c = {d: 1};",
        "var a$b$c$d = 1;");
  }
}
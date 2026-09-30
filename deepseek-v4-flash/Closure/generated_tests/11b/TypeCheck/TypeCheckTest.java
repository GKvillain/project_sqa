package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

import org.junit.Test;

import java.util.List;

/**
 * JUnit 4 test class for TypeCheck.
 * Tests cover normal cases, boundary cases, edge cases, and branch coverage
 * focusing on key logic likely related to Defects4J bug 11b.
 */
public class TypeCheckTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    // Enable all warnings for testing
    options.setWarningLevel(DiagnosticGroups.ACCESS_CONTROLS, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.CONST, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.NON_STANDARD_TYPE_DECL, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.STRICT_MODULE_DEP, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.TYPE_CHECK, CheckLevel.WARNING);
    compiler.initOptions(options);
    return compiler;
  }

  private List<JSError> getErrors(Compiler compiler) {
    return compiler.getErrors();
  }

  private List<JSError> getWarnings(Compiler compiler) {
    return compiler.getWarnings();
  }

  // Helper to compile JS and return warnings
  private Compiler compileAndGetWarnings(String js) {
    Compiler compiler = createCompiler();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("test.js", js);
    compiler.compile(extern, input);
    return compiler;
  }

  // Helper to assert that a warning message contains given substring
  private void assertWarningContains(Compiler compiler, String substring) {
    boolean found = false;
    for (JSError warning : compiler.getWarnings()) {
      if (warning.description.contains(substring)) {
        found = true;
        break;
      }
    }
    assertTrue("Expected warning containing: " + substring, found);
  }

  // Helper to assert no warnings of a certain type
  private void assertNoWarning(Compiler compiler, DiagnosticType type) {
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType().equals(type)) {
        fail("Unexpected warning: " + warning.description);
      }
    }
  }

  // Test: variable type mismatch on assignment
  @Test
  public void testVisitVar_typeMismatch_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @type {number} */ var x = 'string';");
    assertWarningContains(compiler, "initializing variable");
  }

  // Test: valid type assignment should not warn
  @Test
  public void testVisitVar_typeMatches_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @type {number} */ var x = 42;");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: property access on undefined type
  @Test
  public void testGetProp_undefinedProperty_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = {}; var y = x.nonexistent;");
    assertWarningContains(compiler, "Property nonexistent never defined");
  }

  // Test: property access on known type with existing property
  @Test
  public void testGetProp_existingProperty_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = {a: 1}; var y = x.a;");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: enum element existence
  @Test
  public void testGetProp_enumElementDoesNotExist_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @enum {number} */ var E = {A: 1}; var x = E.B;");
    assertWarningContains(compiler, "element B does not exist on this enum");
  }

  // Test: enum element exists
  @Test
  public void testGetProp_enumElementExists_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @enum {number} */ var E = {A: 1}; var x = E.A;");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: direct call of constructor without new
  @Test
  public void testCall_constructorWithoutNew_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @constructor */ function Foo() {}; var f = Foo();");
    assertWarningContains(compiler, "Constructor");
  }

  // Test: constructor called with new is fine
  @Test
  public void testCall_constructorWithNew_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @constructor */ function Foo() {}; var f = new Foo();");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: wrong number of arguments
  @Test
  public void testCall_wrongArgumentCount_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @param {number} a */ function f(a) {}; f(1, 2);");
    assertWarningContains(compiler, "called with 2 argument(s). Function requires at least 1 argument(s)");
  }

  // Test: correct number of arguments
  @Test
  public void testCall_correctArgumentCount_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @param {number} a */ function f(a) {}; f(1);");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: return type mismatch
  @Test
  public void testReturn_typeMismatch_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @return {number} */ function f() { return 'string'; }");
    assertWarningContains(compiler, "inconsistent return type");
  }

  // Test: correct return type
  @Test
  public void testReturn_typeMatches_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @return {number} */ function f() { return 42; }");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: bitwise operation on non-integer
  @Test
  public void testBitwise_nonInteger_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = 'a' | 1;");
    assertWarningContains(compiler, "operator | cannot be applied to string");
  }

  // Test: bitwise on integer is fine
  @Test
  public void testBitwise_integer_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = 1 | 2;");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: equality comparison of incompatible types (deterministic)
  @Test
  public void testEq_incompatibleTypes_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = 1; var y = 'a'; var z = (x === y);");
    assertWarningContains(compiler, "condition always evaluates to");
  }

  // Test: equality of same type should not warn
  @Test
  public void testEq_compatibleTypes_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "var x = 1; var y = 2; var z = (x === y);");
    assertTrue(compiler.getWarnings().isEmpty());
  }

  // Test: missing @override on property overriding superclass method
  @Test
  public void testOverride_missingOnSuperclass_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @constructor */ function Parent() {};\n" +
        "/** @return {number} */ Parent.prototype.method = function() { return 1; };\n" +
        "/** @constructor @extends {Parent} */ function Child() {};\n" +
        "/** @return {number} */ Child.prototype.method = function() { return 2; };");
    // This should warn about missing @override and hidden superclass property
    assertWarningContains(compiler, "property method already defined on superclass Parent; use @override to override it");
  }

  // Test: @override properly used
  @Test
  public void testOverride_withAnnotation_noWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @constructor */ function Parent() {};\n" +
        "/** @return {number} */ Parent.prototype.method = function() { return 1; };\n" +
        "/** @constructor @extends {Parent} */ function Child() {};\n" +
        "/** @override @return {number} */ Child.prototype.method = function() { return 2; };");
    // Should not warn about missing override (but may warn about hidden property? Actually with @override it's fine)
    assertNoWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
    // No warning expected
    assertTrue(compiler.getWarnings().isEmpty() || 
               compiler.getWarnings().size() == 0);
  }

  // Test: interface property conflicts
  @Test
  public void testInterface_conflictingProperties_reportsWarning() {
    Compiler compiler = compileAndGetWarnings(
        "/** @interface */ function I1() {};\n" +
        "/** @type {number} */ I1.prototype.prop;\n" +
        "/** @interface */ function I2() {};\n" +
        "/** @type {string} */ I2.prototype.prop;\n" +
        "/** @interface @extends {I1} @extends {I2} */ function I3() {};");
    assertWarningContains(compiler, "Interface I3 has a property prop with incompatible types in its super interfaces");
  }
}
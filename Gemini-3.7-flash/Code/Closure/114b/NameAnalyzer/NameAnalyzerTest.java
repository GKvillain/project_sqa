package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class NameAnalyzerTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; var goog = {}; goog.inherits = function(child, parent) {}; goog.nullFunction = function() {};";

  public NameAnalyzerTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new NameAnalyzer(compiler, true);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests bug fix 114: caller assignment expression dependency scope
  @Test
  public void testProcess_assignedFunctionCall_preservesInnerVariableReference() {
    testSame("var fun, var1; (fun = function(){ var1; })();");
  }

  // Tests removal of unreferenced simple variable declaration
  @Test
  public void testProcess_unreferencedVariable_isRemoved() {
    test("var a = 1;", "");
  }

  // Tests preservation of variable referenced through window
  @Test
  public void testProcess_variableReferencedByWindow_isPreserved() {
    testSame("var a = 1; window['a'] = a;");
  }

  // Tests removal of unreferenced function declaration
  @Test
  public void testProcess_unreferencedFunction_isRemoved() {
    test("function foo() { var x = 1; }", "");
  }

  // Tests preservation of function referenced through execution
  @Test
  public void testProcess_calledFunctionWithSideEffect_isPreserved() {
    testSame("function foo() { window.x = 1; } foo();");
  }

  // Tests removal of unreferenced prototype property assignments
  @Test
  public void testProcess_unreferencedPrototype_isRemoved() {
    test("function Foo() {} Foo.prototype.bar = function() { return 1; };", "");
  }

  // Tests preservation of prototype property on referenced class
  @Test
  public void testProcess_referencedPrototype_isPreserved() {
    testSame("function Foo() {} Foo.prototype.bar = function() { window.x = 1; }; (new Foo()).bar();");
  }

  // Tests removal of unreferenced class inheritance
  @Test
  public void testProcess_unreferencedInheritance_isRemoved() {
    test("function Super() {} function Sub() {} goog.inherits(Sub, Super);", "");
  }

  // Tests preservation of class inheritance when subclass is used
  @Test
  public void testProcess_referencedInheritance_isPreserved() {
    testSame("function Super() {} function Sub() {} goog.inherits(Sub, Super); window['Sub'] = Sub;");
  }

  // Tests replacement of instanceof check on unreferenced class with false
  @Test
  public void testProcess_instanceofUnreferencedClass_replacedWithFalse() {
    test("function Foo() {} if (x instanceof Foo) { window.a = 1; }",
         "if (false) { window.a = 1; }");
  }

  // Tests extraction of side-effect subexpression from unreferenced assignment
  @Test
  public void testProcess_unreferencedVarWithSideEffectRhs_extractsRhs() {
    test("var a = window.foo();", "window.foo();");
  }

  // Tests removal of unreferenced qualified names / property chain
  @Test
  public void testProcess_unreferencedNamespace_isRemoved() {
    test("var a = {}; a.b = {}; a.b.c = 1;", "");
  }

  // Tests handling of aliased global object properties
  @Test
  public void testProcess_aliasedObjectProperties_isPreserved() {
    testSame("var a = {}; var b = a; b.foo = 3; window.x = a.foo;");
  }

  // Tests removal of unreferenced aliased objects
  @Test
  public void testProcess_unreferencedAlias_isRemoved() {
    test("var a = {}; var b = a; a.foo = 1;", "");
  }

  // Tests preservation of variables inside for loops
  @Test
  public void testProcess_variableInForLoop_isPreserved() {
    testSame("for (var i = 0; i < 10; i++) { window.x = i; }");
  }

  // Tests generation of HTML report
  @Test
  public void testGetHtmlReport_afterProcess_returnsHtmlString() {
    Compiler compiler = new Compiler();
    NameAnalyzer analyzer = new NameAnalyzer(compiler, false);
    Node externs = compiler.parseTestCode(EXTERNS);
    Node root = compiler.parseTestCode("var a = 1; window.a = a;");
    analyzer.process(externs, root);

    String report = analyzer.getHtmlReport();
    assertNotNull(report);
    assertTrue(report.contains("<html><body>"));
    assertTrue(report.contains("OVERALL STATS"));
    assertTrue(report.contains("ALL NAMES"));
  }
}
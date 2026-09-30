package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

public class NameAnalyzerTest extends CompilerTestCase {

  private static final String EXTERNS = "var window; var goog = {}; goog.inherits = function(a, b) {};";

  public NameAnalyzerTest() {
    super(EXTERNS);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new NameAnalyzer(compiler, true);
  }

  // Tests removal of simple unreferenced variable
  @Test
  public void testProcess_unreferencedVar_removesVar() {
    test("var a = 1;", "");
  }

  // Tests preservation of externally referenced variable via window
  @Test
  public void testProcess_windowReferencedVar_preservesVar() {
    testSame("var a = 1; window.a = a;");
  }

  // Tests removal of unreferenced function declaration
  @Test
  public void testProcess_unreferencedFunction_removesFunction() {
    test("function foo() { return 1; }", "");
  }

  // Tests preservation of referenced function declaration
  @Test
  public void testProcess_referencedFunction_preservesFunction() {
    testSame("function foo() { return 1; } window['foo'] = foo;");
  }

  // Tests removal of unreferenced prototype assignment
  @Test
  public void testProcess_unreferencedPrototype_removesPrototype() {
    test("function Foo() {} Foo.prototype.bar = function() { return 1; };", "");
  }

  // Tests preservation of referenced prototype method
  @Test
  public void testProcess_referencedClassWithPrototype_preservesClassAndPrototype() {
    testSame("function Foo() {} Foo.prototype.bar = function() { return 1; }; window['Foo'] = Foo;");
  }

  // Tests defect case: assignment used as call target where dependency scope must be tracked correctly
  @Test
  public void testProcess_assignWithCallTarget_preservesAssignment() {
    test("var fun, x; (fun = function(){ x = 1; })();",
         "var fun, x; (fun = function(){ x = 1; })();");
  }

  // Tests nested assignment expression in call
  @Test
  public void testProcess_nestedAssignInCall_preservesCall() {
    test("var x = 1; var y; (y = function() { window.x = x; })();",
         "var x = 1; var y; (y = function() { window.x = x; })();");
  }

  // Tests unreferenced circular references are eliminated
  @Test
  public void testProcess_circularReferences_removesAll() {
    test("function a() { b(); } function b() { a(); }", "");
  }

  // Tests aliasing of global object
  @Test
  public void testProcess_aliasWithPropertyWrite_preservesReferencedAlias() {
    testSame("var a = {}; var b = a; a.foo = 3; window['out'] = b.foo;");
  }

  // Tests unreferenced instanceof check gets replaced with false
  @Test
  public void testProcess_unreferencedInstanceOf_replacesWithFalse() {
    test("var a = {}; var b = function() {}; if (a instanceof b) { window.alert('yes'); }",
         "var a = {}; if (false) { window.alert('yes'); }");
  }

  // Tests for-loop with assignments in condition and increment
  @Test
  public void testProcess_forLoopWithAssignments_preservesReferencedVars() {
    testSame("var i; for (i = 0; i < 10; i++) { window.i = i; }");
  }

  // Tests for-in loop with variable declaration
  @Test
  public void testProcess_forInLoop_preservesIteration() {
    testSame("var obj = {a: 1}; for (var k in obj) { window[k] = obj[k]; }");
  }

  // Tests HTML report generation does not throw exception
  @Test
  public void testGetHtmlReport_afterProcess_returnsNonEmptyReport() {
    Compiler compiler = new Compiler();
    NameAnalyzer analyzer = new NameAnalyzer(compiler, false);
    Node externs = compiler.parseTestCode(EXTERNS);
    Node root = compiler.parseTestCode("var a = 1; window.a = a;");
    analyzer.process(externs, root);
    String report = analyzer.getHtmlReport();
    assertNotNull(report);
    assertTrue(report.contains("OVERALL STATS"));
  }

  // Tests do-while loop condition references
  @Test
  public void testProcess_doWhileCondition_preservesConditionRef() {
    testSame("var x = 0; do { window.x = x; } while (x < 1);");
  }

  // Tests hook (ternary) expression dependencies
  @Test
  public void testProcess_hookExpression_preservesSideEffects() {
    testSame("var a = 1; var b = 2; window.c = a ? b : 0;");
  }

  // Tests side effect preservation in unreferenced variable assignment
  @Test
  public void testProcess_varWithSideEffect_preservesSideEffect() {
    test("var a = window.alert('sideEffect');", "window.alert('sideEffect');");
  }

  // Tests multiple variables declared where only some are referenced
  @Test
  public void testProcess_multiVarDecl_removesOnlyUnreferenced() {
    test("var a = 1, b = 2; window.b = b;", "var b = 2; window.b = b;");
  }

  // Tests unreferenced goog.inherits call removal
  @Test
  public void testProcess_googInherits_unreferenced_removesAll() {
    test("function A() {} function B() {} goog.inherits(B, A);", "");
  }

  // Tests goog.inherits call preservation when subclass is referenced
  @Test
  public void testProcess_googInherits_referencedSubclass_preservesAll() {
    testSame("function A() {} function B() {} goog.inherits(B, A); window['B'] = B;");
  }

  // Tests nested namespace removal when unreferenced
  @Test
  public void testProcess_nestedNamespace_unreferenced_removesAll() {
    test("var ns = {}; ns.sub = {}; ns.sub.foo = function() { return 42; };", "");
  }

  // Tests nested namespace preservation when referenced
  @Test
  public void testProcess_nestedNamespace_referenced_preservesPath() {
    testSame("var ns = {}; ns.sub = {}; ns.sub.foo = function() { return 42; }; window['foo'] = ns.sub.foo;");
  }

  // Tests switch statement condition and case body references
  @Test
  public void testProcess_switchStatement_preservesReferencedVars() {
    testSame("var a = 1; switch(a) { case 1: window.out = 1; break; }");
  }

  // Tests try-catch block references
  @Test
  public void testProcess_tryCatch_preservesReferences() {
    testSame("var a = 1; try { window.a = a; } catch (e) { window.e = e; }");
  }

  // Tests comma operator expression references
  @Test
  public void testProcess_commaOperator_preservesSideEffects() {
    testSame("var a = 1; var b = 2; (a = 3, window.b = b);");
  }

  // Tests typeof expression dependencies
  @Test
  public void testProcess_typeofExpression_preservesTarget() {
    testSame("var a = {}; if (typeof a !== 'undefined') { window.out = 1; }");
  }

  // Tests assigning handler directly on extern property
  @Test
  public void testProcess_externPropertyAssignment_preservesFunction() {
    testSame("window.onload = function() { var x = 1; window.x = x; };");
  }
}
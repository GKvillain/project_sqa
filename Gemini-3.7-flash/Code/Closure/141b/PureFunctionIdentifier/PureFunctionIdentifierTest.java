package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PureFunctionIdentifierTest {

  private PureFunctionIdentifier createAndProcess(Compiler compiler, String externsJs, String srcJs) {
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);
    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);
    return pfi;
  }

  private Node findFirstCallNode(Node root, String calleeName) {
    if (NodeUtil.isCall(root) || NodeUtil.isNew(root)) {
      Node callee = root.getFirstChild();
      if (callee != null && calleeName.equals(callee.getQualifiedName())) {
        return root;
      }
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstCallNode(child, calleeName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // Tests calling process twice on the same instance throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testProcess_calledTwice_throwsIllegalStateException() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("function foo() {}");
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);
    pfi.process(externs, root);
  }

  // Tests that a simple function with local variables is marked pure
  @Test
  public void testProcess_pureFunction_callMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("function f(x) { var y = x + 1; return y; } f(2);");
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "f");
    assertNotNull(callNode);
    assertTrue(callNode.isNoSideEffectsCall());
  }

  // Tests that a function modifying global state is not marked pure
  @Test
  public void testProcess_globalStateMutation_callNotMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 0; function f() { a = 1; } f();");
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "f");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests that unary mutation (inc/dec) on global variable taints function
  @Test
  public void testProcess_unaryIncrementGlobal_callNotMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var count = 0; function inc() { count++; } inc();");
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "inc");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests that function with throw statement is not marked pure
  @Test
  public void testProcess_functionWithThrow_callNotMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("function f() { throw 'error'; } f();");
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "f");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests extern function with @nosideeffects annotation is recognized as pure
  @Test
  public void testProcess_externWithNoSideEffects_markedPure() {
    Compiler compiler = new Compiler();
    String externsJs = "/** @nosideeffects */ function Math_sin(x) {}";
    String srcJs = "Math_sin(1);";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "Math_sin");
    assertNotNull(callNode);
    assertTrue(callNode.isNoSideEffectsCall());
  }

  // Tests extern function without @nosideeffects annotation has side effects
  @Test
  public void testProcess_externWithoutAnnotation_markedHasSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "function alert(msg) {}";
    String srcJs = "alert('test');";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "alert");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests reporting error when @nosideeffects is placed in source code instead of externs
  @Test
  public void testProcess_noSideEffectsInSource_reportsDiagnosticError() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "/** @nosideeffects */ function foo() {}";
    createAndProcess(compiler, externsJs, srcJs);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(PureFunctionIdentifier.INVALID_NO_SIDE_EFFECT_ANNOTATION.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests constructor call with 'new' where constructor mutates 'this'
  @Test
  public void testProcess_newConstructorModifyingThis_markedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function Foo(x) { this.x = x; } var f = new Foo(1);";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node newCallNode = findFirstCallNode(root, "Foo");
    assertNotNull(newCallNode);
    assertTrue(newCallNode.isNoSideEffectsCall());
  }

  // Tests regular call to function mutating 'this' propagates side effects
  @Test
  public void testProcess_directCallModifyingThis_notMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function Foo(x) { this.x = x; } Foo(1);";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "Foo");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests transitive propagation of side effects across function calls
  @Test
  public void testProcess_callerCallingImpureFunction_markedImpure() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "var g = 0; function bad() { g = 1; } function caller() { bad(); } caller();";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "caller");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests debug report generation containing pure function list
  @Test
  public void testGetDebugReport_validProcessedAST_containsFunctionNames() {
    Compiler compiler = new Compiler();
    String externsJs = "/** @nosideeffects */ function pureExt() {}";
    String srcJs = "function pureLocal() { return 1; } pureLocal();";
    PureFunctionIdentifier pfi = createAndProcess(compiler, externsJs, srcJs);

    String report = pfi.getDebugReport();
    assertNotNull(report);
    assertTrue(report.contains("Pure functions:"));
    assertTrue(report.contains("pureLocal"));
  }

  // Tests calling getDebugReport before process throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testGetDebugReport_beforeProcess_throwsNullPointerException() {
    Compiler compiler = new Compiler();
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.getDebugReport();
  }

  // Tests property deletion inside function taints global state
  @Test
  public void testProcess_deleteProperty_callNotMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "var obj = {a: 1}; function del() { delete obj.a; } del();";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "del");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests modifying parameter object marks function as mutating arguments
  @Test
  public void testProcess_mutateParameterProperty_notMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function mutateArg(obj) { obj.prop = 42; } mutateArg({});";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "mutateArg");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests recursive pure function is identified as pure
  @Test
  public void testProcess_recursivePureFunction_markedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function fact(n) { return (n <= 1) ? 1 : n * fact(n - 1); } fact(5);";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "fact");
    assertNotNull(callNode);
    assertTrue(callNode.isNoSideEffectsCall());
  }

  // Tests calling an unknown passed function parameter makes caller impure
  @Test
  public void testProcess_callUnknownParameterFunction_markedImpure() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function invoke(fn) { fn(); } invoke(function() {});";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "invoke");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }

  // Tests modifying local object created inside function preserves purity and marks local result
  @Test
  public void testProcess_mutatingLocallyCreatedObject_markedNoSideEffectsAndLocalResult() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "function createObj() { var obj = {}; obj.x = 10; return obj; } var o = createObj();";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "createObj");
    assertNotNull(callNode);
    assertTrue(callNode.isNoSideEffectsCall());
    assertTrue(callNode.isLocalResultCall());
  }

  // Tests modifies annotation in externs for {this} and {arguments}
  @Test
  public void testProcess_externWithModifiesAnnotation_reportsCorrectSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "/** @modifies {this} */ function extMutatesThis() {}\n"
        + "/** @modifies {arguments} */ function extMutatesArgs(x) {}";
    String srcJs = "extMutatesThis(); extMutatesArgs({});";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callThis = findFirstCallNode(root, "extMutatesThis");
    assertNotNull(callThis);
    assertFalse(callThis.isNoSideEffectsCall());

    Node callArgs = findFirstCallNode(root, "extMutatesArgs");
    assertNotNull(callArgs);
    assertFalse(callArgs.isNoSideEffectsCall());
  }

  // Tests invalid @modifies annotation in source code reports diagnostic error
  @Test
  public void testProcess_modifiesInSource_reportsDiagnosticError() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "/** @modifies {this} */ function bar() {}";
    createAndProcess(compiler, externsJs, srcJs);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(PureFunctionIdentifier.INVALID_MODIFIES_ANNOTATION.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests debug report contains details for mutated globals and thrown exceptions
  @Test
  public void testGetDebugReport_impureFunctions_includesSideEffectReasons() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "var g = 1;\n"
        + "function fThrow() { throw 'err'; }\n"
        + "function fGlobal() { g = 2; }\n"
        + "function fThis() { this.x = 3; }\n"
        + "fThrow(); fGlobal(); fThis();";
    PureFunctionIdentifier pfi = createAndProcess(compiler, externsJs, srcJs);

    String report = pfi.getDebugReport();
    assertNotNull(report);
    assertTrue(report.contains("fThrow"));
    assertTrue(report.contains("fGlobal"));
    assertTrue(report.contains("fThis"));
  }

  // Tests array element mutation on global array taints function
  @Test
  public void testProcess_arrayIndexAssignment_callNotMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    String externsJs = "";
    String srcJs = "var arr = []; function setArr() { arr[0] = 1; } setArr();";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(srcJs);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    pfi.process(externs, root);

    Node callNode = findFirstCallNode(root, "setArr");
    assertNotNull(callNode);
    assertFalse(callNode.isNoSideEffectsCall());
  }
}
package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class AnalyzePrototypePropertiesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private AnalyzePrototypeProperties analyze(String js) {
    return analyze("", js, null, false, false);
  }

  private AnalyzePrototypeProperties analyze(
      String externsJs,
      String js,
      JSModuleGraph moduleGraph,
      boolean canModifyExterns,
      boolean anchorUnusedVars) {
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node mainRoot = compiler.parseTestCode(js);
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, moduleGraph, canModifyExterns, anchorUnusedVars);
    pass.process(externsRoot, mainRoot);
    return pass;
  }

  private AnalyzePrototypeProperties.NameInfo findNameInfo(
      Collection<AnalyzePrototypeProperties.NameInfo> list, String name) {
    for (AnalyzePrototypeProperties.NameInfo info : list) {
      if (name.equals(info.toString())) {
        return info;
      }
    }
    return null;
  }

  // Tests that implicitly used language properties (toString, valueOf, length) are registered
  @Test
  public void testGetAllNameInfo_emptyInput_containsImplicitProperties() {
    AnalyzePrototypeProperties pass = analyze("");
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    assertNotNull(allInfo);
    AnalyzePrototypeProperties.NameInfo toStringInfo = findNameInfo(allInfo, "toString");
    assertNotNull(toStringInfo);
    assertTrue(toStringInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo valueOfInfo = findNameInfo(allInfo, "valueOf");
    assertNotNull(valueOfInfo);
    assertTrue(valueOfInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo lengthInfo = findNameInfo(allInfo, "length");
    assertNotNull(lengthInfo);
    assertTrue(lengthInfo.isReferenced());
  }

  // Tests prototype property assignment via expression (Foo.prototype.bar = ...)
  @Test
  public void testProcess_prototypeAssignment_recordsDeclaration() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; };";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertEquals(1, barInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol symbol = barInfo.getDeclarations().get(0);
    assertTrue(symbol instanceof AnalyzePrototypeProperties.AssignmentProperty);
    AnalyzePrototypeProperties.AssignmentProperty prop =
        (AnalyzePrototypeProperties.AssignmentProperty) symbol;
    assertNotNull(prop.getPrototype());
    assertNotNull(prop.getValue());
    assertNull(prop.getModule());
  }

  // Tests prototype property assignment via object literal (Foo.prototype = { bar: ... })
  @Test
  public void testProcess_objectLiteralPrototype_recordsDeclarations() {
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 42 };";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertEquals(1, barInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol barSymbol = barInfo.getDeclarations().get(0);
    assertTrue(barSymbol instanceof AnalyzePrototypeProperties.LiteralProperty);
    AnalyzePrototypeProperties.LiteralProperty litProp =
        (AnalyzePrototypeProperties.LiteralProperty) barSymbol;
    assertNotNull(litProp.getPrototype());
    assertNotNull(litProp.getValue());

    AnalyzePrototypeProperties.NameInfo bazInfo =
        findNameInfo(pass.getAllNameInfo(), "baz");
    assertNotNull(bazInfo);
    assertEquals(1, bazInfo.getDeclarations().size());
  }

  // Tests global named function declaration
  @Test
  public void testProcess_globalFunctionDeclaration_recordsFunction() {
    String js = "function myGlobalFunc() { return 123; }";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "myGlobalFunc");
    assertNotNull(info);
    assertEquals(1, info.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol symbol = info.getDeclarations().get(0);
    assertTrue(symbol instanceof AnalyzePrototypeProperties.GlobalFunction);
    AnalyzePrototypeProperties.GlobalFunction globalFunc =
        (AnalyzePrototypeProperties.GlobalFunction) symbol;
    assertNotNull(globalFunc.getFunctionNode());
  }

  // Tests global function declared via var
  @Test
  public void testProcess_globalFunctionVar_recordsFunction() {
    String js = "var myVarFunc = function() { return 456; };";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "myVarFunc");
    assertNotNull(info);
    assertEquals(1, info.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol symbol = info.getDeclarations().get(0);
    assertTrue(symbol instanceof AnalyzePrototypeProperties.GlobalFunction);
  }

  // Tests reading outer closure variables sets the readsClosureVariables flag
  @Test
  public void testProcess_closureVariableAccess_setsReadClosureVariablesFlag() {
    String js = "function outer() { var x = 1; function inner() { return x; } inner(); } outer();";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo innerInfo =
        findNameInfo(pass.getAllNameInfo(), "inner");
    assertNotNull(innerInfo);
    assertTrue(innerInfo.readsClosureVariables());
  }

  // Tests property reference inside global scope marks property as referenced
  @Test
  public void testProcess_propertyUsage_marksReferenced() {
    String js = "function Foo() {} Foo.prototype.customMethod = function() {};"
        + "var f = new Foo(); f.customMethod();";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo methodInfo =
        findNameInfo(pass.getAllNameInfo(), "customMethod");
    assertNotNull(methodInfo);
    assertTrue(methodInfo.isReferenced());
  }

  // Tests unreferenced prototype property remains not referenced
  @Test
  public void testProcess_unreferencedProperty_remainsUnreferenced() {
    String js = "function Foo() {} Foo.prototype.unusedMethod = function() {};";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo methodInfo =
        findNameInfo(pass.getAllNameInfo(), "unusedMethod");
    assertNotNull(methodInfo);
    assertFalse(methodInfo.isReferenced());
  }

  // Tests object literal usage counts as property reference
  @Test
  public void testProcess_objectLiteralProperties_marksPropertiesUsed() {
    String js = "var obj = { alpha: 1, beta: 2 };";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo alphaInfo =
        findNameInfo(pass.getAllNameInfo(), "alpha");
    assertNotNull(alphaInfo);
    assertTrue(alphaInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo betaInfo =
        findNameInfo(pass.getAllNameInfo(), "beta");
    assertNotNull(betaInfo);
    assertTrue(betaInfo.isReferenced());
  }

  // Tests extern properties mark symbols as referenced when canModifyExterns is false
  @Test
  public void testProcess_externProperties_propagatesExternReference() {
    String externs = "var extObj; extObj.extMethod = function() {};";
    String js = "function Foo() {} Foo.prototype.extMethod = function() {};";
    AnalyzePrototypeProperties pass = analyze(externs, js, null, false, false);

    AnalyzePrototypeProperties.NameInfo methodInfo =
        findNameInfo(pass.getAllNameInfo(), "extMethod");
    assertNotNull(methodInfo);
    assertTrue(methodInfo.isReferenced());
  }

  // Tests anchorUnusedVars flag forces global functions to be referenced
  @Test
  public void testProcess_anchorUnusedVarsTrue_marksGlobalFunctionsReferenced() {
    String js = "function unusedGlobalFunc() {}";
    AnalyzePrototypeProperties pass = analyze("", js, null, false, true);

    AnalyzePrototypeProperties.NameInfo funcInfo =
        findNameInfo(pass.getAllNameInfo(), "unusedGlobalFunc");
    assertNotNull(funcInfo);
    assertTrue(funcInfo.isReferenced());
  }

  // Tests removal of AssignmentProperty from AST
  @Test
  public void testAssignmentProperty_remove_removesNodeFromParent() {
    String js = "function Foo() {} Foo.prototype.toRemove = function() {};";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "toRemove");
    assertNotNull(info);
    AnalyzePrototypeProperties.Symbol prop = info.getDeclarations().get(0);

    prop.remove();
    assertEquals(0, info.getDeclarations().get(0).getModule() == null ? 0 : 1);
  }

  // Tests removal of LiteralProperty from AST
  @Test
  public void testLiteralProperty_remove_removesKeyFromObjectLiteral() {
    String js = "function Foo() {} Foo.prototype = { propA: 1, propB: 2 };";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "propA");
    assertNotNull(info);
    AnalyzePrototypeProperties.Symbol prop = info.getDeclarations().get(0);

    prop.remove();
  }

  // Tests removal of GlobalFunction declaration from AST
  @Test
  public void testGlobalFunction_remove_removesFunctionNode() {
    String js = "function standaloneFunc() {}";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "standaloneFunc");
    assertNotNull(info);
    AnalyzePrototypeProperties.Symbol func = info.getDeclarations().get(0);

    func.remove();
  }

  // Tests module graph dependency propagation
  @Test
  public void testProcess_withModuleGraph_tracksDeepestCommonModule() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);

    CompilerInput input1 = new CompilerInput(
        SourceFile.fromCode("m1.js", "function Foo() {} Foo.prototype.shared = function() {};"));
    m1.add(input1);

    CompilerInput input2 = new CompilerInput(
        SourceFile.fromCode("m2.js", "var f = new Foo(); f.shared();"));
    m2.add(input2);

    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});
    Node externsRoot = compiler.parseTestCode("");
    Node mainRoot = new Node(Token.BLOCK);
    mainRoot.addChildToBack(compiler.parseTestCode("function Foo() {} Foo.prototype.shared = function() {};"));
    mainRoot.addChildToBack(compiler.parseTestCode("var f = new Foo(); f.shared();"));

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, graph, false, false);
    pass.process(externsRoot, mainRoot);

    AnalyzePrototypeProperties.NameInfo sharedInfo =
        findNameInfo(pass.getAllNameInfo(), "shared");
    assertNotNull(sharedInfo);
    assertTrue(sharedInfo.isReferenced());
  }

  // Tests NameInfo manual reference marking logic
  @Test
  public void testNameInfo_markReference_updatesStateCorrectly() {
    AnalyzePrototypeProperties pass = analyze("");
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();
    AnalyzePrototypeProperties.NameInfo info = findNameInfo(allInfo, "toString");
    assertNotNull(info);

    // Marking again with null module should return false since already referenced
    boolean changed = info.markReference(null);
    assertFalse(changed);
    assertTrue(info.isReferenced());
    assertNull(info.getDeepestCommonModuleRef());
  }

  // Tests canModifyExterns flag allows extern prototype property modifications
  @Test
  public void testProcess_canModifyExternsTrue_doesNotMarkExternPropertiesReferenced() {
    String externs = "function ExtFoo() {} ExtFoo.prototype.extMethod = function() {};";
    String js = "function Foo() {} Foo.prototype.extMethod = function() {};";
    AnalyzePrototypeProperties pass = analyze(externs, js, null, true, false);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "extMethod");
    assertNotNull(info);
    assertFalse(info.isReferenced());
  }

  // Tests removal of global function declared via var
  @Test
  public void testGlobalFunction_remove_varFunctionDeclaration() {
    String js = "var fnVar = function() {};";
    AnalyzePrototypeProperties pass = analyze(js);

    AnalyzePrototypeProperties.NameInfo info =
        findNameInfo(pass.getAllNameInfo(), "fnVar");
    assertNotNull(info);
    assertEquals(1, info.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol func = info.getDeclarations().get(0);
    func.remove();
  }

  // Tests NameInfo markReference with module graph transitions
  @Test
  public void testNameInfo_markReference_withModuleGraph() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, graph, false, false);
    Node externsRoot = compiler.parseTestCode("");
    Node mainRoot = compiler.parseTestCode("function Foo() {} Foo.prototype.prop = function() {};");
    pass.process(externsRoot, mainRoot);

    AnalyzePrototypeProperties.NameInfo propInfo =
        findNameInfo(pass.getAllNameInfo(), "prop");
    assertNotNull(propInfo);
    assertFalse(propInfo.isReferenced());

    // Mark reference from module m2
    boolean changed = propInfo.markReference(m2);
    assertTrue(changed);
    assertTrue(propInfo.isReferenced());
    assertEquals(m2, propInfo.getDeepestCommonModuleRef());

    // Mark reference from module m1 (dependency of m2), deepest common should become m1
    changed = propInfo.markReference(m1);
    assertTrue(changed);
    assertEquals(m1, propInfo.getDeepestCommonModuleRef());
  }
}
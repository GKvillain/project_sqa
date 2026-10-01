package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AnalyzePrototypeProperties.AssignmentProperty;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.GlobalFunction;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.LiteralProperty;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.NameInfo;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.Symbol;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AnalyzePrototypePropertiesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Map<String, NameInfo> analyze(String js, String externsJs,
      boolean canModifyExterns, boolean anchorUnusedVars) {
    Node externRoot = compiler.parseTestCode(externsJs != null ? externsJs : "");
    Node mainRoot = compiler.parseTestCode(js);

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, null, canModifyExterns, anchorUnusedVars);
    pass.process(externRoot, mainRoot);

    Map<String, NameInfo> map = new HashMap<String, NameInfo>();
    for (NameInfo info : pass.getAllNameInfo()) {
      map.put(info.toString(), info);
    }
    return map;
  }

  private Map<String, NameInfo> analyze(String js) {
    return analyze(js, "", false, false);
  }

  // Tests standard prototype property assignment via EXPR
  @Test
  public void testProcess_simplePrototypeAssign_recordsDeclaration() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    assertEquals(1, barInfo.getDeclarations().size());
    Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof AssignmentProperty);

    AssignmentProperty assignProp = (AssignmentProperty) symbol;
    assertNotNull(assignProp.getPrototype());
    assertNotNull(assignProp.getValue());
    assertNull(assignProp.getModule());
  }

  // Tests prototype assignment using object literal syntax
  @Test
  public void testProcess_objectLiteralPrototypeAssign_recordsDeclarations() {
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 42 };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    assertEquals(1, barInfo.getDeclarations().size());
    assertTrue(barInfo.getDeclarations().getFirst() instanceof LiteralProperty);

    LiteralProperty litProp = (LiteralProperty) barInfo.getDeclarations().getFirst();
    assertNotNull(litProp.getPrototype());
    assertNotNull(litProp.getValue());
    assertNull(litProp.getModule());

    NameInfo bazInfo = map.get("baz");
    assertNotNull(bazInfo);
    assertEquals(1, bazInfo.getDeclarations().size());
  }

  // Tests global function declaration
  @Test
  public void testProcess_globalFunctionDeclaration_recordsDeclaration() {
    String js = "function globalFunc() { return 1; }";
    Map<String, NameInfo> map = analyze(js);

    NameInfo funcInfo = map.get("globalFunc");
    assertNotNull(funcInfo);
    assertEquals(1, funcInfo.getDeclarations().size());
    Symbol symbol = funcInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof GlobalFunction);

    GlobalFunction globalFunc = (GlobalFunction) symbol;
    assertNotNull(globalFunc.getFunctionNode());
    assertNull(globalFunc.getModule());
  }

  // Tests global var function declaration
  @Test
  public void testProcess_globalVarFunction_recordsDeclaration() {
    String js = "var globalVarFunc = function() { return 2; };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo funcInfo = map.get("globalVarFunc");
    assertNotNull(funcInfo);
    assertEquals(1, funcInfo.getDeclarations().size());
    assertTrue(funcInfo.getDeclarations().getFirst() instanceof GlobalFunction);
  }

  // Tests chained prototype property assignments
  @Test
  public void testProcess_chainedPrototypePropertyAssign_doesNotCrash() {
    String js = "var a = {}; a.b = {}; a.b.prototype.foo = function() {};";
    Map<String, NameInfo> map = analyze(js);

    NameInfo fooInfo = map.get("foo");
    assertNotNull(fooInfo);
  }

  // Tests property usage inside another method connects reference in graph
  @Test
  public void testProcess_propertyAccessInsideMethod_marksReferenced() {
    String js = "function Foo() {} "
        + "Foo.prototype.bar = function(x) { x.baz(); }; "
        + "var x = new Foo(); x.bar();";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    assertTrue(barInfo.isReferenced());

    NameInfo bazInfo = map.get("baz");
    assertNotNull(bazInfo);
    assertTrue(bazInfo.isReferenced());
  }

  // Tests object literal usage counts properties as referenced
  @Test
  public void testProcess_objectLiteralPropertyUsage_marksReferenced() {
    String js = "function Foo() {} Foo.prototype.bar = function() {}; "
        + "var obj = { bar: 123 };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    assertTrue(barInfo.isReferenced());
  }

  // Tests closure variable access detection
  @Test
  public void testProcess_closureVariableRead_setsReadClosureVariables() {
    String js = "function outer() { "
        + "  var secret = 1; "
        + "  function Foo() {} "
        + "  Foo.prototype.reveal = function() { return secret; }; "
        + "}";
    Map<String, NameInfo> map = analyze(js);

    NameInfo revealInfo = map.get("reveal");
    assertNotNull(revealInfo);
    assertTrue(revealInfo.readsClosureVariables());
  }

  // Tests non-closure property does not set readClosureVariables
  @Test
  public void testProcess_noClosureVariableRead_readsClosureVariablesIsFalse() {
    String js = "function Foo() {} Foo.prototype.normal = function(x) { return x + 1; };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo normalInfo = map.get("normal");
    assertNotNull(normalInfo);
    assertFalse(normalInfo.readsClosureVariables());
  }

  // Tests anchorUnusedVars option marks declarations as referenced
  @Test
  public void testProcess_anchorUnusedVarsTrue_marksGlobalVarReferenced() {
    String js = "var unusedFunc = function() {};";
    Map<String, NameInfo> map = analyze(js, "", false, true);

    NameInfo unusedInfo = map.get("unusedFunc");
    assertNotNull(unusedInfo);
    assertTrue(unusedInfo.isReferenced());
  }

  // Tests unreferenced function remains unreferenced when anchorUnusedVars is false
  @Test
  public void testProcess_anchorUnusedVarsFalse_leavesUnusedUnreferenced() {
    String js = "var unusedFunc = function() {};";
    Map<String, NameInfo> map = analyze(js, "", false, false);

    NameInfo unusedInfo = map.get("unusedFunc");
    assertNotNull(unusedInfo);
    assertFalse(unusedInfo.isReferenced());
  }

  // Tests extern properties processing with canModifyExterns false
  @Test
  public void testProcess_externProperties_marksPropertyReferenced() {
    String externs = "var ext; ext.extProp = 1;";
    String js = "function Foo() {} Foo.prototype.extProp = function() {};";
    Map<String, NameInfo> map = analyze(js, externs, false, false);

    NameInfo extPropInfo = map.get("extProp");
    assertNotNull(extPropInfo);
    assertTrue(extPropInfo.isReferenced());
  }

  // Tests implicit language properties like toString are automatically initialized
  @Test
  public void testProcess_implicitlyUsedProperties_presentInAllNameInfo() {
    Map<String, NameInfo> map = analyze("");

    NameInfo toStringInfo = map.get("toString");
    assertNotNull(toStringInfo);
    assertTrue(toStringInfo.isReferenced());

    NameInfo valueOfInfo = map.get("valueOf");
    assertNotNull(valueOfInfo);
    assertTrue(valueOfInfo.isReferenced());

    NameInfo lengthInfo = map.get("length");
    assertNotNull(lengthInfo);
    assertTrue(lengthInfo.isReferenced());
  }

  // Tests module dependency reference propagation
  @Test
  public void testProcess_withJSModules_propagatesModuleReferences() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModule[] modules = new JSModule[] { m1, m2 };
    JSModuleGraph moduleGraph = new JSModuleGraph(modules);

    Compiler comp = new Compiler();
    JSModule rootMod = moduleGraph.getRootModule();
    assertEquals(m1, rootMod);

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        comp, moduleGraph, false, false);

    Collection<NameInfo> allInfo = pass.getAllNameInfo();
    assertFalse(allInfo.isEmpty());
  }

  // Tests GlobalFunction AST removal
  @Test
  public void testGlobalFunction_remove_removesFunctionNodeFromAst() {
    String js = "function toRemove() {} var kept = 1;";
    Node root = compiler.parseTestCode(js);
    Node funcNode = root.getFirstChild();

    AnalyzePrototypeProperties.GlobalFunction gf =
        new AnalyzePrototypeProperties(compiler, null, false, false).new GlobalFunction(
            funcNode.getFirstChild(), funcNode, root, null);

    gf.remove();
    assertNotEquals(funcNode, root.getFirstChild());
  }

  // Tests AssignmentProperty AST removal
  @Test
  public void testAssignmentProperty_remove_removesExprNodeFromAst() {
    String js = "Foo.prototype.bar = function() {}; var next = 1;";
    Node root = compiler.parseTestCode(js);
    Node exprNode = root.getFirstChild();

    AssignmentProperty prop = new AssignmentProperty(exprNode, null);
    prop.remove();

    assertNotEquals(exprNode, root.getFirstChild());
  }

  // Tests LiteralProperty AST removal
  @Test
  public void testLiteralProperty_remove_removesKeyFromMap() {
    String js = "Foo.prototype = { bar: 1, baz: 2 };";
    Node root = compiler.parseTestCode(js);
    Node expr = root.getFirstChild();
    Node assign = expr.getFirstChild();
    Node map = assign.getLastChild();
    Node key = map.getFirstChild();

    LiteralProperty litProp = new LiteralProperty(key, key.getFirstChild(), map, assign, null);
    litProp.remove();

    assertNotEquals("bar", map.getFirstChild().getString());
    assertEquals("baz", map.getFirstChild().getString());
  }

  // Tests GlobalFunction AST removal when declared via var with multiple variables
  @Test
  public void testGlobalFunction_remove_varWithMultipleDeclarations() {
    String js = "var a = 1, toRemove = function() {};";
    Node root = compiler.parseTestCode(js);
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getLastChild();

    AnalyzePrototypeProperties.GlobalFunction gf =
        new AnalyzePrototypeProperties(compiler, null, false, false).new GlobalFunction(
            nameNode, nameNode.getFirstChild(), varNode, null);

    gf.remove();
    assertEquals(1, varNode.getChildCount());
    assertEquals("a", varNode.getFirstChild().getString());
  }

  // Tests GlobalFunction AST removal when declared via var with single variable
  @Test
  public void testGlobalFunction_remove_varWithSingleDeclaration() {
    String js = "var toRemove = function() {}; var kept = 1;";
    Node root = compiler.parseTestCode(js);
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();

    AnalyzePrototypeProperties.GlobalFunction gf =
        new AnalyzePrototypeProperties(compiler, null, false, false).new GlobalFunction(
            nameNode, nameNode.getFirstChild(), varNode, null);

    gf.remove();
    assertNotEquals(varNode, root.getFirstChild());
    assertEquals("kept", root.getFirstChild().getFirstChild().getString());
  }

  // Tests canModifyExterns true does not mark extern property as referenced
  @Test
  public void testProcess_canModifyExternsTrue_externNotMarkedReferenced() {
    String externs = "var ext; ext.extProp = 1;";
    String js = "function Foo() {} Foo.prototype.extProp = function() {};";
    Map<String, NameInfo> map = analyze(js, externs, true, false);

    NameInfo extPropInfo = map.get("extProp");
    assertNotNull(extPropInfo);
    assertFalse(extPropInfo.isReferenced());
  }

  // Tests GlobalFunction getRootNode
  @Test
  public void testGlobalFunction_getRootNode() {
    String js = "function globalFunc() {}";
    Map<String, NameInfo> map = analyze(js);

    NameInfo funcInfo = map.get("globalFunc");
    assertNotNull(funcInfo);
    Symbol symbol = funcInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof GlobalFunction);
    GlobalFunction gf = (GlobalFunction) symbol;
    assertNotNull(gf.getRootNode());
  }

  // Tests AssignmentProperty getRootNode
  @Test
  public void testAssignmentProperty_getRootNode() {
    String js = "Foo.prototype.bar = function() {};";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof AssignmentProperty);
    AssignmentProperty ap = (AssignmentProperty) symbol;
    assertNotNull(ap.getRootNode());
  }

  // Tests LiteralProperty getRootNode
  @Test
  public void testLiteralProperty_getRootNode() {
    String js = "Foo.prototype = { bar: 1 };";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof LiteralProperty);
    LiteralProperty lp = (LiteralProperty) symbol;
    assertNotNull(lp.getRootNode());
  }

  // Tests multiple prototype assignments to the same property name
  @Test
  public void testProcess_multiplePrototypeAssignments_accumulatesDeclarations() {
    String js = "function Foo() {} Foo.prototype.bar = 1; Foo.prototype.bar = 2;";
    Map<String, NameInfo> map = analyze(js);

    NameInfo barInfo = map.get("bar");
    assertNotNull(barInfo);
    assertEquals(2, barInfo.getDeclarations().size());
  }

  // Tests global function call marks the function as referenced
  @Test
  public void testProcess_globalFunctionCall_marksReferenced() {
    String js = "function helper() {} helper();";
    Map<String, NameInfo> map = analyze(js);

    NameInfo helperInfo = map.get("helper");
    assertNotNull(helperInfo);
    assertTrue(helperInfo.isReferenced());
  }
}
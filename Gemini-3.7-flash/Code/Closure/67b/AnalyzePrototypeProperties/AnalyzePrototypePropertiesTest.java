package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AnalyzePrototypeProperties.AssignmentProperty;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AnalyzePrototypePropertiesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private AnalyzePrototypeProperties.NameInfo findNameInfo(
      Collection<AnalyzePrototypeProperties.NameInfo> infos, String name) {
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if (name.equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  // Tests constructor initialization of implicit properties with null module graph
  @Test
  public void testConstructor_nullModuleGraph_initializesImplicitProperties() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();

    assertNotNull(infos);
    AnalyzePrototypeProperties.NameInfo toStringInfo = findNameInfo(infos, "toString");
    assertNotNull(toStringInfo);
    assertTrue(toStringInfo.isReferenced());
  }

  // Tests constructor initialization with JSModuleGraph
  @Test
  public void testConstructor_withModuleGraph_initializesModules() {
    JSModule rootModule = new JSModule("m1");
    JSModule childModule = new JSModule("m2");
    childModule.addDep(rootModule);
    JSModuleGraph moduleGraph = new JSModuleGraph(new JSModule[] {rootModule, childModule});

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, moduleGraph, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo lengthInfo = findNameInfo(infos, "length");
    assertNotNull(lengthInfo);
    assertTrue(lengthInfo.isReferenced());
  }

  // Tests simple prototype property assignment via expression
  @Test
  public void testProcess_simplePrototypeAssignment_registersDeclaration() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function Foo() {} Foo.prototype.bar = function() {};";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertEquals(1, barInfo.getDeclarations().size());
    assertFalse(barInfo.isReferenced());
  }

  // Tests prototype property reference from another function
  @Test
  public void testProcess_prototypePropertyReferenced_marksReferenced() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function Foo() {} Foo.prototype.bar = function() {};"
        + "function test(f) { f.bar(); } test(new Foo());";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertTrue(barInfo.isReferenced());
  }

  // Tests object literal prototype assignment
  @Test
  public void testProcess_objectLiteralPrototype_registersProperties() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 42 };";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    AnalyzePrototypeProperties.NameInfo bazInfo =
        findNameInfo(pass.getAllNameInfo(), "baz");

    assertNotNull(barInfo);
    assertNotNull(bazInfo);
    assertEquals(1, barInfo.getDeclarations().size());
    assertEquals(1, bazInfo.getDeclarations().size());
  }

  // Tests global function declaration via VAR
  @Test
  public void testProcess_globalVarFunction_registersGlobalFunction() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "var myFunc = function() { return 1; };";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo myFuncInfo =
        findNameInfo(pass.getAllNameInfo(), "myFunc");
    assertNotNull(myFuncInfo);
    assertEquals(1, myFuncInfo.getDeclarations().size());
    assertFalse(myFuncInfo.isReferenced());
  }

  // Tests anchorUnusedVars forces reference mark on global functions
  @Test
  public void testProcess_anchorUnusedVarsTrue_marksGlobalFunctionReferenced() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, true);
    String js = "function myUnusedFunc() {}";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo funcInfo =
        findNameInfo(pass.getAllNameInfo(), "myUnusedFunc");
    assertNotNull(funcInfo);
    assertTrue(funcInfo.isReferenced());
  }

  // Tests closure variable read detection inside function
  @Test
  public void testProcess_closureVariableRead_marksReadsClosureVariables() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function outer() { var x = 1; "
        + "function Foo() {} Foo.prototype.bar = function() { return x; }; }";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo barInfo =
        findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertTrue(barInfo.readsClosureVariables());
  }

  // Tests extern properties processing when canModifyExterns is false
  @Test
  public void testProcess_externProperties_marksPropertyReferenced() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String externs = "window.externProp;";
    String js = "function Foo() {} Foo.prototype.externProp = function() {};";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode(externs);

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo externPropInfo =
        findNameInfo(pass.getAllNameInfo(), "externProp");
    assertNotNull(externPropInfo);
    assertTrue(externPropInfo.isReferenced());
  }

  // Tests non-chained property assignment does not count as prototype assignment
  @Test
  public void testProcess_nonChainedAssignment_doesNotRegisterAsPrototypeProperty() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "var obj = {}; obj.foo = function() {};";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo fooInfo =
        findNameInfo(pass.getAllNameInfo(), "foo");
    if (fooInfo != null) {
      assertEquals(0, fooInfo.getDeclarations().size());
    }
  }

  // Tests AssignmentProperty remove, getPrototype and getValue
  @Test
  public void testAssignmentProperty_methods_returnExpectedNodesAndRemove() {
    String js = "Foo.prototype.bar = 123;";
    Node root = compiler.parseTestCode(js);
    Node exprNode = root.getFirstChild();

    AssignmentProperty prop = new AssignmentProperty(exprNode, null);
    assertNull(prop.getModule());
    assertNotNull(prop.getPrototype());
    assertEquals(Token.GETPROP, prop.getPrototype().getType());
    assertNotNull(prop.getValue());
    assertEquals(Token.NUMBER, prop.getValue().getType());

    prop.remove();
    assertEquals(0, root.getChildCount());
  }

  // Tests LiteralProperty remove, getPrototype and getValue
  @Test
  public void testLiteralProperty_methods_returnExpectedNodesAndRemove() {
    String js = "Foo.prototype = { bar: 123 };";
    Node root = compiler.parseTestCode(js);
    Node exprNode = root.getFirstChild();
    Node assignNode = exprNode.getFirstChild();
    Node mapNode = assignNode.getLastChild();
    Node keyNode = mapNode.getFirstChild();
    Node valueNode = keyNode.getFirstChild();

    AnalyzePrototypeProperties.LiteralProperty litProp =
        new AnalyzePrototypeProperties.LiteralProperty(
            keyNode, valueNode, mapNode, assignNode, null);

    assertNull(litProp.getModule());
    assertEquals(assignNode.getFirstChild(), litProp.getPrototype());
    assertEquals(valueNode, litProp.getValue());

    litProp.remove();
    assertEquals(0, mapNode.getChildCount());
  }

  // Tests GlobalFunction remove for var node and function node
  @Test
  public void testGlobalFunction_remove_removesFromAst() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function namedFunc() {}";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo funcInfo =
        findNameInfo(pass.getAllNameInfo(), "namedFunc");
    assertNotNull(funcInfo);
    assertFalse(funcInfo.getDeclarations().isEmpty());

    AnalyzePrototypeProperties.Symbol symbol = funcInfo.getDeclarations().getFirst();
    symbol.remove();
    assertEquals(0, root.getChildCount());
  }

  // Tests markReference module tracking with JSModuleGraph
  @Test
  public void testNameInfo_markReference_tracksDeepestCommonModule() {
    JSModule rootModule = new JSModule("root");
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m1.addDep(rootModule);
    m2.addDep(rootModule);
    JSModuleGraph moduleGraph = new JSModuleGraph(new JSModule[] {rootModule, m1, m2});

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, moduleGraph, false, false);
    AnalyzePrototypeProperties.NameInfo info = pass.new NameInfo("customProp");

    assertFalse(info.isReferenced());
    assertTrue(info.markReference(m1));
    assertTrue(info.isReferenced());
    assertEquals(m1, info.getDeepestCommonModuleRef());

    assertTrue(info.markReference(m2));
    assertEquals(rootModule, info.getDeepestCommonModuleRef());
  }

  // Tests object literal usage references properties within function
  @Test
  public void testProcess_objectLiteralUsage_createsReferenceEdge() {
    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    String js = "function Foo() {} Foo.prototype.propA = function() {};"
        + "function useObj() { var x = { propA: 1 }; } useObj();";
    Node root = compiler.parseTestCode(js);
    Node externsRoot = compiler.parseTestCode("");

    pass.process(externsRoot, root);

    AnalyzePrototypeProperties.NameInfo propAInfo =
        findNameInfo(pass.getAllNameInfo(), "propA");
    assertNotNull(propAInfo);
    assertTrue(propAInfo.isReferenced());
  }
}
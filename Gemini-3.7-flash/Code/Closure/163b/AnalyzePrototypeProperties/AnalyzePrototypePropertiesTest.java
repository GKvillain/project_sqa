package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AnalyzePrototypeProperties.AssignmentProperty;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.GlobalFunction;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.LiteralProperty;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.NameInfo;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.Property;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.Symbol;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class AnalyzePrototypePropertiesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private NameInfo findNameInfo(Collection<NameInfo> infos, String name) {
    for (NameInfo info : infos) {
      if (name.equals(info.toString())) {
        return info;
      }
    }
    return null;
  }

  // Tests basic prototype assignment and reference analysis
  @Test
  public void testProcess_simplePrototypeAssignment_createsNameInfo() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; };";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<NameInfo> nameInfos = pass.getAllNameInfo();
    NameInfo barInfo = findNameInfo(nameInfos, "bar");
    assertNotNull(barInfo);
    assertEquals("bar", barInfo.toString());
    assertEquals(1, barInfo.getDeclarations().size());
  }

  // Tests object literal prototype property definition
  @Test
  public void testProcess_objectLiteralPrototype_registersProperties() {
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 2 };";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<NameInfo> nameInfos = pass.getAllNameInfo();
    NameInfo barInfo = findNameInfo(nameInfos, "bar");
    NameInfo bazInfo = findNameInfo(nameInfos, "baz");

    assertNotNull(barInfo);
    assertNotNull(bazInfo);
    assertEquals(1, barInfo.getDeclarations().size());
    assertEquals(1, bazInfo.getDeclarations().size());
    assertTrue(barInfo.getDeclarations().getFirst() instanceof LiteralProperty);
  }

  // Tests implicitly used properties like toString, valueOf, length
  @Test
  public void testProcess_implicitProperties_areReferenced() {
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<NameInfo> nameInfos = pass.getAllNameInfo();
    NameInfo toStringInfo = findNameInfo(nameInfos, "toString");
    assertNotNull(toStringInfo);
    assertTrue(toStringInfo.isReferenced());
  }

  // Tests global function declaration and anchorUnusedVars flag
  @Test
  public void testProcess_globalFunctionDeclaration_anchoredAndUnanchored() {
    String js = "function globalFunc() { return 42; }";
    Node root1 = compiler.parseTestCode(js);
    Node externs1 = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass1 =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass1.process(externs1, root1);

    NameInfo funcInfo1 = findNameInfo(pass1.getAllNameInfo(), "globalFunc");
    assertNotNull(funcInfo1);
    assertFalse(funcInfo1.isReferenced());

    Node root2 = compiler.parseTestCode(js);
    Node externs2 = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass2 =
        new AnalyzePrototypeProperties(compiler, null, false, true);
    pass2.process(externs2, root2);

    NameInfo funcInfo2 = findNameInfo(pass2.getAllNameInfo(), "globalFunc");
    assertNotNull(funcInfo2);
    assertTrue(funcInfo2.isReferenced());
  }

  // Tests global function assigned via VAR declaration
  @Test
  public void testProcess_globalVarFunction_registersDeclaration() {
    String js = "var globalVarFunc = function() {}; globalVarFunc();";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo info = findNameInfo(pass.getAllNameInfo(), "globalVarFunc");
    assertNotNull(info);
    assertTrue(info.isReferenced());
    assertEquals(1, info.getDeclarations().size());
    assertTrue(info.getDeclarations().getFirst() instanceof GlobalFunction);
  }

  // Tests reference propagation between prototype properties
  @Test
  public void testProcess_propertyCallChain_propagatesReferences() {
    String js =
        "function Foo() {}\n"
            + "Foo.prototype.bar = function() { this.baz(); };\n"
            + "Foo.prototype.baz = function() {};\n"
            + "var f = new Foo(); f.bar();";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo barInfo = findNameInfo(pass.getAllNameInfo(), "bar");
    NameInfo bazInfo = findNameInfo(pass.getAllNameInfo(), "baz");

    assertNotNull(barInfo);
    assertNotNull(bazInfo);
    assertTrue(barInfo.isReferenced());
    assertTrue(bazInfo.isReferenced());
  }

  // Tests non-function prototype property assignment
  @Test
  public void testProcess_nonFunctionPrototypePropertyAssign_handledCorrectly() {
    String js = "function Foo() {} Foo.prototype.num = 123;";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo numInfo = findNameInfo(pass.getAllNameInfo(), "num");
    assertNotNull(numInfo);
    assertEquals(1, numInfo.getDeclarations().size());
  }

  // Tests object literal usage counting as property use
  @Test
  public void testProcess_objectLiteralUse_countsAsSymbolUse() {
    String js =
        "function Foo() {} Foo.prototype.bar = function() {};\n"
            + "var obj = { bar: 1 };";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo barInfo = findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    assertTrue(barInfo.isReferenced());
  }

  // Tests closure variable access detection inside prototype property
  @Test
  public void testProcess_closureVariableAccess_marksReadClosureVariables() {
    String js =
        "function createClass() {\n"
            + "  var localVar = 10;\n"
            + "  function Foo() {}\n"
            + "  Foo.prototype.getVal = function() { return localVar; };\n"
            + "}";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo getValInfo = findNameInfo(pass.getAllNameInfo(), "getVal");
    assertNotNull(getValInfo);
    assertTrue(getValInfo.readsClosureVariables());
  }

  // Tests extern properties traversal when canModifyExterns is false
  @Test
  public void testProcess_externProperties_areMarkedReferenced() {
    String externsJs = "var externalObj; externalObj.externProp = function() {};";
    String js = "function Foo() {} Foo.prototype.externProp = function() {};";
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo propInfo = findNameInfo(pass.getAllNameInfo(), "externProp");
    assertNotNull(propInfo);
    assertTrue(propInfo.isReferenced());
  }

  // Tests module graph integration and deepest common module computation
  @Test
  public void testProcess_withModuleGraph_tracksModuleDependencies() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModuleGraph moduleGraph = new JSModuleGraph(new JSModule[] {m1, m2});

    String js1 = "function Foo() {} Foo.prototype.bar = function() {};";
    String js2 = "var f = new Foo(); f.bar();";

    Node root = new Node(com.google.javascript.rhino.Token.BLOCK);
    Node rootM1 = compiler.parseTestCode(js1);
    Node rootM2 = compiler.parseTestCode(js2);
    root.addChildToBack(rootM1);
    root.addChildToBack(rootM2);

    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, moduleGraph, false, false);
    pass.process(externs, root);

    NameInfo barInfo = findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
  }

  // Tests Symbol.remove() for AssignmentProperty
  @Test
  public void testAssignmentProperty_remove_removesNodeFromParent() {
    String js = "function Foo() {} Foo.prototype.bar = function() {};";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo barInfo = findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof AssignmentProperty);

    Property prop = (Property) symbol;
    assertNotNull(prop.getPrototype());
    assertNotNull(prop.getValue());
    assertNull(prop.getModule());

    int childCountBefore = root.getChildCount();
    symbol.remove();
    assertEquals(childCountBefore - 1, root.getChildCount());
  }

  // Tests Symbol.remove() for LiteralProperty
  @Test
  public void testLiteralProperty_remove_removesKeyFromObjectLiteral() {
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 1 };";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo barInfo = findNameInfo(pass.getAllNameInfo(), "bar");
    assertNotNull(barInfo);
    Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof LiteralProperty);

    Property prop = (Property) symbol;
    assertNotNull(prop.getPrototype());
    assertNotNull(prop.getValue());
    assertNull(prop.getModule());

    symbol.remove();
    NameInfo bazInfo = findNameInfo(pass.getAllNameInfo(), "baz");
    assertEquals(1, bazInfo.getDeclarations().size());
  }

  // Tests Symbol.remove() and getFunctionNode() for GlobalFunction
  @Test
  public void testGlobalFunction_remove_removesFunctionDeclaration() {
    String js = "function globalFunc() {}";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties pass =
        new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    NameInfo funcInfo = findNameInfo(pass.getAllNameInfo(), "globalFunc");
    assertNotNull(funcInfo);
    GlobalFunction globalFunc = (GlobalFunction) funcInfo.getDeclarations().getFirst();
    assertNotNull(globalFunc.getFunctionNode());
    assertNull(globalFunc.getModule());

    assertEquals(1, root.getChildCount());
    globalFunc.remove();
    assertEquals(0, root.getChildCount());
  }
}
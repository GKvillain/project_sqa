package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ScopeTest {

  private Node rootNode;
  private Scope globalScope;

  @Before
  public void setUp() {
    rootNode = new Node(Token.BLOCK);
    globalScope = new Scope(rootNode, (ObjectType) null);
  }

  // Tests global scope initial properties
  @Test
  public void testGlobalScope_initialState_propertiesMatch() {
    assertTrue(globalScope.isGlobal());
    assertFalse(globalScope.isLocal());
    assertTrue(globalScope.isBottom());
    assertEquals(0, globalScope.getDepth());
    assertEquals(rootNode, globalScope.getRootNode());
    assertNull(globalScope.getParent());
    assertNull(globalScope.getParentScope());
    assertEquals(globalScope, globalScope.getGlobalScope());
    assertEquals(0, globalScope.getVarCount());
  }

  // Tests nested local scope depth, hierarchy and properties
  @Test
  public void testChildScope_nested_correctHierarchy() {
    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);

    assertFalse(childScope.isGlobal());
    assertTrue(childScope.isLocal());
    assertFalse(childScope.isBottom());
    assertEquals(1, childScope.getDepth());
    assertEquals(fnNode, childScope.getRootNode());
    assertEquals(globalScope, childScope.getParent());
    assertEquals(globalScope, childScope.getParentScope());
    assertEquals(globalScope, childScope.getGlobalScope());
  }

  // Tests declaring a variable and retrieving it from scope
  @Test
  public void testDeclare_newVariable_returnsVarAndUpdatesCount() {
    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = globalScope.declare("x", nameNode, null, null);

    assertNotNull(var);
    assertEquals("x", var.getName());
    assertEquals(nameNode, var.getNameNode());
    assertTrue(var.isTypeInferred());
    assertEquals(globalScope, var.getScope());
    assertTrue(var.isGlobal());
    assertFalse(var.isLocal());
    assertEquals("<non-file>", var.getInputName());
    assertEquals(1, globalScope.getVarCount());
    assertTrue(globalScope.isDeclared("x", false));
    assertEquals(var, globalScope.getVar("x"));
    assertEquals(var, globalScope.getSlot("x"));
    assertEquals(var, globalScope.getOwnSlot("x"));
  }

  // Tests recursive variable lookup in parent scopes
  @Test
  public void testGetVar_inChildScope_resolvesParentVar() {
    Node nameNode = Node.newString(Token.NAME, "parentVar");
    Scope.Var var = globalScope.declare("parentVar", nameNode, null, null);

    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);

    assertTrue(childScope.isDeclared("parentVar", true));
    assertFalse(childScope.isDeclared("parentVar", false));
    assertEquals(var, childScope.getVar("parentVar"));
    assertNull(childScope.getOwnSlot("parentVar"));
  }

  // Tests undeclaring a variable
  @Test
  public void testUndeclare_existingVar_removesFromScope() {
    Node nameNode = Node.newString(Token.NAME, "y");
    Scope.Var var = globalScope.declare("y", nameNode, null, null);
    assertEquals(1, globalScope.getVarCount());

    globalScope.undeclare(var);
    assertEquals(0, globalScope.getVarCount());
    assertFalse(globalScope.isDeclared("y", false));
    assertNull(globalScope.getVar("y"));
    assertNull(globalScope.getSlot("y"));
  }

  // Tests exception when declaring duplicate variable in same scope
  @Test(expected = IllegalStateException.class)
  public void testDeclare_duplicateName_throwsIllegalStateException() {
    Node name1 = Node.newString(Token.NAME, "dup");
    Node name2 = Node.newString(Token.NAME, "dup");
    globalScope.declare("dup", name1, null, null);
    globalScope.declare("dup", name2, null, null);
  }

  // Tests exception when declaring variable with empty name
  @Test(expected = IllegalStateException.class)
  public void testDeclare_emptyName_throwsIllegalStateException() {
    Node nameNode = Node.newString(Token.NAME, "");
    globalScope.declare("", nameNode, null, null);
  }

  // Tests exception when undeclaring variable not in this scope
  @Test(expected = IllegalStateException.class)
  public void testUndeclare_varFromDifferentScope_throwsIllegalStateException() {
    Node nameNode = Node.newString(Token.NAME, "z");
    Scope.Var var = globalScope.declare("z", nameNode, null, null);

    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);
    childScope.undeclare(var);
  }

  // Tests Var initial value extraction for VAR statement
  @Test
  public void testVar_getInitialValue_varDeclaration() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "a");
    Node initVal = Node.newString("val");
    nameNode.addChildToBack(initVal);
    varNode.addChildToBack(nameNode);

    Scope.Var var = globalScope.declare("a", nameNode, null, null);
    assertEquals(varNode, var.getParentNode());
    assertEquals(initVal, var.getInitialValue());
  }

  // Tests Var initial value extraction for ASSIGN expression
  @Test
  public void testVar_getInitialValue_assignExpression() {
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, "b");
    Node assignVal = Node.newString("assigned");
    assignNode.addChildToBack(nameNode);
    assignNode.addChildToBack(assignVal);

    Scope.Var var = globalScope.declare("b", nameNode, null, null);
    assertEquals(assignVal, var.getInitialValue());
  }

  // Tests Var initial value extraction for FUNCTION declaration
  @Test
  public void testVar_getInitialValue_functionDeclaration() {
    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "f");
    fnNode.addChildToBack(nameNode);

    Scope.Var var = globalScope.declare("f", nameNode, null, null);
    assertEquals(fnNode, var.getInitialValue());
  }

  // Tests Var initial value extraction when no initial value exists
  @Test
  public void testVar_getInitialValue_unsupportedParentReturnsNull() {
    Node exprNode = new Node(Token.EXPR_RESULT);
    Node nameNode = Node.newString(Token.NAME, "c");
    exprNode.addChildToBack(nameNode);

    Scope.Var var = globalScope.declare("c", nameNode, null, null);
    assertNull(var.getInitialValue());
  }

  // Tests setType on inferred vs declared variable
  @Test(expected = IllegalStateException.class)
  public void testVar_setType_declaredVarThrowsException() {
    Node nameNode = Node.newString(Token.NAME, "declaredVar");
    Scope.Var var = globalScope.declare("declaredVar", nameNode, null, null, false);
    assertFalse(var.isTypeInferred());
    var.setType(null);
  }

  // Tests setType on inferred variable
  @Test
  public void testVar_setType_inferredVarSucceeds() {
    Node nameNode = Node.newString(Token.NAME, "inferredVar");
    Scope.Var var = globalScope.declare("inferredVar", nameNode, null, null, true);
    assertTrue(var.isTypeInferred());
    var.setType(null);
    assertNull(var.getType());
  }

  // Tests Var isConst detection based on name convention
  @Test
  public void testVar_isConst_recognizesConstantName() {
    Node constName = Node.newString(Token.NAME, "MY_CONST");
    Scope.Var constVar = globalScope.declare("MY_CONST", constName, null, null);
    assertTrue(constVar.isConst());

    Node regularName = Node.newString(Token.NAME, "myVar");
    Scope.Var regularVar = globalScope.declare("myVar", regularName, null, null);
    assertFalse(regularVar.isConst());
  }

  // Tests Var isBleedingFunction
  @Test
  public void testVar_isBleedingFunction_namedFunctionExpression() {
    Node assignNode = new Node(Token.ASSIGN);
    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "bleedingFn");
    fnNode.addChildToBack(nameNode);
    assignNode.addChildToBack(fnNode);

    Scope.Var var = globalScope.declare("bleedingFn", nameNode, null, null);
    assertTrue(var.isBleedingFunction());
  }

  // Tests Var equals, hashCode and toString
  @Test
  public void testVar_equalsAndHashCodeAndToString() {
    Node nameNode1 = Node.newString(Token.NAME, "v");
    Node nameNode2 = Node.newString(Token.NAME, "v");

    Scope.Var var1 = globalScope.declare("v", nameNode1, null, null);
    assertEquals(var1, var1);
    assertFalse(var1.equals("v"));
    assertFalse(var1.equals(null));
    assertEquals(nameNode1.hashCode(), var1.hashCode());
    assertEquals("Scope.Var v", var1.toString());
  }

  // Tests getVars iterator
  @Test
  public void testGetVars_multipleVars_iteratesAll() {
    Node name1 = Node.newString(Token.NAME, "v1");
    Node name2 = Node.newString(Token.NAME, "v2");
    globalScope.declare("v1", name1, null, null);
    globalScope.declare("v2", name2, null, null);

    Iterator<Scope.Var> iter = globalScope.getVars();
    assertTrue(iter.hasNext());
    assertEquals("v1", iter.next().getName());
    assertTrue(iter.hasNext());
    assertEquals("v2", iter.next().getName());
    assertFalse(iter.hasNext());
  }

  // Tests getTypeOfThis for global and inherited child scopes
  @Test
  public void testScope_getTypeOfThis_returnsNullOrInheritedType() {
    assertNull(globalScope.getTypeOfThis());

    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);
    assertNull(childScope.getTypeOfThis());
  }

  // Tests getArgumentsVar in function scopes vs global scope
  @Test
  public void testChildScope_argumentsVar_createdForFunctionScope() {
    assertNull(globalScope.getArgumentsVar());

    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);
    Scope.Var argsVar = childScope.getArgumentsVar();

    assertNotNull(argsVar);
    assertEquals("arguments", argsVar.getName());
    assertTrue(argsVar.isArguments());
    assertTrue(argsVar.isLocal());
    assertFalse(argsVar.isGlobal());
  }

  // Tests isParam on function parameter nodes
  @Test
  public void testVar_isParam_forParameterNode() {
    Node lpNode = new Node(Token.LP);
    Node paramNode = Node.newString(Token.NAME, "param1");
    lpNode.addChildToBack(paramNode);

    Scope.Var var = globalScope.declare("param1", paramNode, null, null);
    assertTrue(var.isParam());
    assertFalse(var.isCatch());
  }

  // Tests isCatch on catch clause parameter nodes
  @Test
  public void testVar_isCatch_forCatchNode() {
    Node catchNode = new Node(Token.CATCH);
    Node errNode = Node.newString(Token.NAME, "err");
    catchNode.addChildToBack(errNode);

    Scope.Var var = globalScope.declare("err", errNode, null, null);
    assertTrue(var.isCatch());
    assertFalse(var.isParam());
  }

  // Tests isBleedingFunction returns false for regular function declarations
  @Test
  public void testVar_isBleedingFunction_falseForFunctionDeclaration() {
    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "normalFn");
    fnNode.addChildToBack(nameNode);

    Scope.Var var = globalScope.declare("normalFn", nameNode, null, null);
    assertFalse(var.isBleedingFunction());
  }

  // Tests declaring variable with CompilerInput provides input and input name
  @Test
  public void testVar_withCompilerInput_storesInputAndInputName() {
    SourceFile sf = SourceFile.fromCode("input.js", "var inputVar;");
    CompilerInput input = new CompilerInput(sf);
    Node nameNode = Node.newString(Token.NAME, "inputVar");

    Scope.Var var = globalScope.declare("inputVar", nameNode, null, input);
    assertEquals("input.js", var.getInputName());
    assertEquals(input, var.getInput());
  }

  // Tests JSDocInfo handling on Var (@const and @noshadow)
  @Test
  public void testVar_jsDocInfo_constAndNoShadowAnnotations() {
    Node nameNode = Node.newString(Token.NAME, "annotatedVar");
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordConstancy();
    builder.recordNoShadow();
    JSDocInfo info = builder.build(nameNode);
    nameNode.setJSDocInfo(info);

    Scope.Var var = globalScope.declare("annotatedVar", nameNode, null, null);
    assertTrue(var.isConst());
    assertTrue(var.isNoShadow());
    assertEquals(info, var.getJSDocInfo());
  }

  // Tests getNode returns the underlying name node
  @Test
  public void testVar_getNode_returnsNameNode() {
    Node nameNode = Node.newString(Token.NAME, "varNode");
    Scope.Var var = globalScope.declare("varNode", nameNode, null, null);
    assertEquals(nameNode, var.getNode());
  }
}
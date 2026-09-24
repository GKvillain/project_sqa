package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ScopeTest {

  private Node globalRoot;
  private Scope globalScope;

  @Before
  public void setUp() {
    globalRoot = new Node(Token.BLOCK);
    globalScope = new Scope(globalRoot, (ObjectType) null);
  }

  // Tests bottom scope creation and properties
  @Test
  public void testScopeCreation_bottomScope_propertiesMatch() {
    assertTrue(globalScope.isBottom());
    assertEquals(0, globalScope.getDepth());
    assertTrue(globalScope.isGlobal());
    assertFalse(globalScope.isLocal());
    assertEquals(globalRoot, globalScope.getRootNode());
    assertNull(globalScope.getParent());
    assertNull(globalScope.getParentScope());
    assertSame(globalScope, globalScope.getGlobalScope());
    assertNull(globalScope.getTypeOfThis());
  }

  // Tests child scope hierarchy and depth calculation
  @Test
  public void testScopeCreation_childScope_depthAndHierarchy() {
    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, childRoot);

    assertFalse(childScope.isBottom());
    assertEquals(1, childScope.getDepth());
    assertFalse(childScope.isGlobal());
    assertTrue(childScope.isLocal());
    assertEquals(childRoot, childScope.getRootNode());
    assertSame(globalScope, childScope.getParent());
    assertSame(globalScope, childScope.getParentScope());
    assertSame(globalScope, childScope.getGlobalScope());
  }

  // Tests declaring a variable and retrieving it via getVar, getSlot, getOwnSlot
  @Test
  public void testDeclare_newVariable_storesCorrectly() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    Scope.Var var = globalScope.declare("foo", nameNode, null, null);

    assertNotNull(var);
    assertEquals("foo", var.getName());
    assertEquals(nameNode, var.getNameNode());
    assertSame(var, globalScope.getVar("foo"));
    assertSame(var, globalScope.getSlot("foo"));
    assertSame(var, globalScope.getOwnSlot("foo"));
    assertEquals(1, globalScope.getVarCount());
    assertTrue(var.isTypeInferred());
    assertTrue(var.isGlobal());
    assertFalse(var.isLocal());
  }

  // Tests declaring duplicate variable in same scope throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testDeclare_duplicateVariable_throwsException() {
    Node nameNode1 = Node.newString(Token.NAME, "foo");
    Node nameNode2 = Node.newString(Token.NAME, "foo");
    globalScope.declare("foo", nameNode1, null, null);
    globalScope.declare("foo", nameNode2, null, null);
  }

  // Tests undeclaring a variable
  @Test
  public void testUndeclare_existingVariable_removesSuccessfully() {
    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = globalScope.declare("x", nameNode, null, null);
    assertEquals(1, globalScope.getVarCount());

    globalScope.undeclare(var);
    assertEquals(0, globalScope.getVarCount());
    assertNull(globalScope.getVar("x"));
    assertNull(globalScope.getOwnSlot("x"));
  }

  // Tests undeclaring variable belonging to another scope throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testUndeclare_varFromDifferentScope_throwsException() {
    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, childRoot);
    Node nameNode = Node.newString(Token.NAME, "y");
    Scope.Var var = childScope.declare("y", nameNode, null, null);

    globalScope.undeclare(var);
  }

  // Tests recursive variable lookup across parent scopes
  @Test
  public void testGetVar_parentScopeResolution_findsInParent() {
    Node nameNode = Node.newString(Token.NAME, "a");
    Scope.Var globalVar = globalScope.declare("a", nameNode, null, null);

    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, childRoot);

    assertSame(globalVar, childScope.getVar("a"));
    assertSame(globalVar, childScope.getSlot("a"));
    assertNull(childScope.getOwnSlot("a"));
  }

  // Tests isDeclared method with recursive flag true and false
  @Test
  public void testIsDeclared_recursiveAndNonRecursive_returnsExpected() {
    Node nameNode = Node.newString(Token.NAME, "b");
    globalScope.declare("b", nameNode, null, null);

    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, childRoot);

    assertTrue(globalScope.isDeclared("b", false));
    assertTrue(globalScope.isDeclared("b", true));
    assertFalse(childScope.isDeclared("b", false));
    assertTrue(childScope.isDeclared("b", true));
    assertFalse(childScope.isDeclared("unknown", true));
  }

  // Tests getVars iterator
  @Test
  public void testGetVars_multipleVars_iteratesAll() {
    Node n1 = Node.newString(Token.NAME, "v1");
    Node n2 = Node.newString(Token.NAME, "v2");
    globalScope.declare("v1", n1, null, null);
    globalScope.declare("v2", n2, null, null);

    Iterator<Scope.Var> it = globalScope.getVars();
    assertTrue(it.hasNext());
    assertEquals("v1", it.next().getName());
    assertTrue(it.hasNext());
    assertEquals("v2", it.next().getName());
    assertFalse(it.hasNext());
  }

  // Tests Var.getInitialValue with VAR parent node
  @Test
  public void testVar_getInitialValue_varNode() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "c");
    Node initVal = Node.newNumber(42.0);
    nameNode.addChildToFront(initVal);
    varNode.addChildToFront(nameNode);

    Scope.Var var = globalScope.declare("c", nameNode, null, null);
    assertSame(initVal, var.getInitialValue());
    assertEquals(varNode, var.getParentNode());
  }

  // Tests Var.getInitialValue with ASSIGN parent node
  @Test
  public void testVar_getInitialValue_assignNode() {
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, "d");
    Node initVal = Node.newString("val");
    assignNode.addChildToBack(nameNode);
    assignNode.addChildToBack(initVal);

    Scope.Var var = globalScope.declare("d", nameNode, null, null);
    assertSame(initVal, var.getInitialValue());
  }

  // Tests Var.getInitialValue with FUNCTION parent node
  @Test
  public void testVar_getInitialValue_functionNode() {
    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "fn");
    fnNode.addChildToFront(nameNode);

    Scope.Var var = globalScope.declare("fn", nameNode, null, null);
    assertSame(fnNode, var.getInitialValue());
  }

  // Tests Var equals, hashCode and toString
  @Test
  public void testVar_equalsAndHashCodeAndToString() {
    Node nameNode1 = Node.newString(Token.NAME, "item");
    Node nameNode2 = Node.newString(Token.NAME, "item");
    Scope.Var var1 = globalScope.declare("item", nameNode1, null, null);

    assertEquals(var1, var1);
    assertFalse(var1.equals(null));
    assertFalse(var1.equals("item"));
    assertEquals(nameNode1.hashCode(), var1.hashCode());
    assertEquals("Scope.Var item", var1.toString());
  }

  // Tests Var.isConst for constant and non-constant names
  @Test
  public void testVar_isConst() {
    Node constNode = Node.newString(Token.NAME, "CONST_VAL");
    Node normalNode = Node.newString(Token.NAME, "normalVal");

    Scope.Var constVar = globalScope.declare("CONST_VAL", constNode, null, null);
    Scope.Var normalVar = globalScope.declare("normalVal", normalNode, null, null);

    assertTrue(constVar.isConst());
    assertFalse(normalVar.isConst());
  }

  // Tests Var.getInputName for null input
  @Test
  public void testVar_getInputName_nullInput_returnsNonFile() {
    Node nameNode = Node.newString(Token.NAME, "temp");
    Scope.Var var = globalScope.declare("temp", nameNode, null, null);

    assertEquals("<non-file>", var.getInputName());
    assertTrue(var.isExtern());
  }

  // Tests Var.setType on non-inferred variable throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testVar_setType_notInferred_throwsException() {
    Node nameNode = Node.newString(Token.NAME, "declaredVar");
    Scope.Var var = globalScope.declare("declaredVar", nameNode, null, null, false);

    assertFalse(var.isTypeInferred());
    var.setType(null);
  }

  // Tests Scope.getArgumentsVar in function scopes
  @Test
  public void testScope_argumentsVar() {
    assertNull(globalScope.getArgumentsVar());

    Node fnRoot = new Node(Token.FUNCTION);
    Scope fnScope = new Scope(globalScope, fnRoot);
    Scope.Var argsVar = fnScope.getArgumentsVar();
    assertNotNull(argsVar);
    assertEquals("arguments", argsVar.getName());
    assertSame(argsVar, fnScope.getArgumentsVar());
    assertTrue(argsVar.isLocal());
    assertSame(fnScope, argsVar.getScope());
  }

  // Tests Var properties such as getScope, isBleedingFunction, isDefine, isNoShadow, and getJSDocInfo
  @Test
  public void testVar_additionalProperties() {
    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "bleedFn");
    fnNode.addChildToFront(nameNode);

    Scope fnScope = new Scope(globalScope, fnNode);
    Scope.Var var = fnScope.declare("bleedFn", nameNode, null, null);

    assertSame(fnScope, var.getScope());
    assertTrue(var.isBleedingFunction());
    assertFalse(var.isDefine());
    assertFalse(var.isNoShadow());
    assertNull(var.getJSDocInfo());

    JSDocInfo info = new JSDocInfo();
    nameNode.setJSDocInfo(info);
    assertSame(info, var.getJSDocInfo());
  }

  // Tests Var.setType and getType for type-inferred variables
  @Test
  public void testVar_setType_inferred() {
    Node nameNode = Node.newString(Token.NAME, "inferredVar");
    Scope.Var var = globalScope.declare("inferredVar", nameNode, null, null, true);

    assertTrue(var.isTypeInferred());
    assertNull(var.getType());

    var.setType(null);
    assertNull(var.getType());
  }

  // Tests Var.getInitialValue when parent node is null or unexpected token
  @Test
  public void testVar_getInitialValue_noParentOrOtherToken() {
    Node nameNode = Node.newString(Token.NAME, "standalone");
    Scope.Var var = globalScope.declare("standalone", nameNode, null, null);
    assertNull(var.getInitialValue());

    Node exprNode = new Node(Token.EXPR_RESULT);
    exprNode.addChildToFront(nameNode);
    assertNull(var.getInitialValue());
  }
}
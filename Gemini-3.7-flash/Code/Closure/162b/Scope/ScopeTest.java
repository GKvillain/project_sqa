package com.google.javascript.jscomp;

import com.google.common.collect.Iterables;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ScopeTest {

  // Tests global scope creation and its properties
  @Test
  public void testGlobalScope_creation_hasCorrectProperties() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    assertTrue(scope.isGlobal());
    assertFalse(scope.isLocal());
    assertTrue(scope.isBottom());
    assertEquals(0, scope.getDepth());
    assertSame(root, scope.getRootNode());
    assertNull(scope.getParent());
    assertSame(scope, scope.getGlobalScope());
    assertEquals(0, scope.getVarCount());
  }

  // Tests child scope creation and hierarchy
  @Test
  public void testChildScope_creation_linksToParent() {
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, (ObjectType) null);

    Node functionRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, functionRoot);

    assertFalse(childScope.isGlobal());
    assertTrue(childScope.isLocal());
    assertFalse(childScope.isBottom());
    assertEquals(1, childScope.getDepth());
    assertSame(globalScope, childScope.getParent());
    assertSame(globalScope, childScope.getParentScope());
    assertSame(globalScope, childScope.getGlobalScope());
    assertSame(functionRoot, childScope.getRootNode());
  }

  // Tests variable declaration and lookup in the same scope
  @Test
  public void testDeclare_newVariable_retrievesSuccessfully() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = scope.declare("x", nameNode, null, null);

    assertNotNull(var);
    assertEquals("x", var.getName());
    assertSame(nameNode, var.getNode());
    assertSame(nameNode, var.getNameNode());
    assertSame(scope, var.getScope());
    assertTrue(var.isTypeInferred());
    assertEquals(1, scope.getVarCount());
    assertSame(var, scope.getVar("x"));
    assertSame(var, scope.getOwnSlot("x"));
    assertSame(var, scope.getSlot("x"));
    assertTrue(scope.isDeclared("x", false));
  }

  // Tests variable lookup in parent scopes
  @Test
  public void testGetVar_inParentScope_resolvesRecursively() {
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, (ObjectType) null);
    Node nameNode = Node.newString(Token.NAME, "parentVar");
    Scope.Var var = globalScope.declare("parentVar", nameNode, null, null);

    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, childRoot);

    assertSame(var, childScope.getVar("parentVar"));
    assertSame(var, childScope.getSlot("parentVar"));
    assertNull(childScope.getOwnSlot("parentVar"));
    assertTrue(childScope.isDeclared("parentVar", true));
    assertFalse(childScope.isDeclared("parentVar", false));
  }

  // Tests duplicate declaration in same scope throws exception
  @Test(expected = IllegalStateException.class)
  public void testDeclare_duplicateName_throwsException() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode1 = Node.newString(Token.NAME, "x");
    Node nameNode2 = Node.newString(Token.NAME, "x");

    scope.declare("x", nameNode1, null, null);
    scope.declare("x", nameNode2, null, null);
  }

  // Tests declaration with empty name throws exception
  @Test(expected = IllegalStateException.class)
  public void testDeclare_emptyName_throwsException() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);
    Node nameNode = Node.newString(Token.NAME, "");
    scope.declare("", nameNode, null, null);
  }

  // Tests undeclaring a variable from scope
  @Test
  public void testUndeclare_existingVariable_removesFromScope() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = scope.declare("x", nameNode, null, null);

    assertEquals(1, scope.getVarCount());
    scope.undeclare(var);

    assertEquals(0, scope.getVarCount());
    assertNull(scope.getVar("x"));
    assertFalse(scope.isDeclared("x", false));
  }

  // Tests undeclaring a variable belonging to another scope throws exception
  @Test(expected = IllegalStateException.class)
  public void testUndeclare_varFromDifferentScope_throwsException() {
    Node root1 = new Node(Token.BLOCK);
    Scope scope1 = new Scope(root1, (ObjectType) null);
    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = scope1.declare("x", nameNode, null, null);

    Node root2 = new Node(Token.BLOCK);
    Scope scope2 = new Scope(root2, (ObjectType) null);
    scope2.undeclare(var);
  }

  // Tests getArgumentsVar lazy initialization and equality
  @Test
  public void testGetArgumentsVar_returnsSingletonArgumentsVar() {
    Node root = new Node(Token.FUNCTION);
    Scope scope = new Scope(root, (ObjectType) null);

    Scope.Var args1 = scope.getArgumentsVar();
    Scope.Var args2 = scope.getArgumentsVar();

    assertNotNull(args1);
    assertSame(args1, args2);
    assertEquals("arguments", args1.getName());
    assertNull(args1.getNode());
    assertNull(args1.getDeclaration());
    assertNull(args1.getParentNode());
    assertEquals(args1, args2);
    assertEquals(args1.hashCode(), args1.hashCode());
  }

  // Tests Scope.Var initial value resolution for VAR statement
  @Test
  public void testVar_getInitialValue_fromVarNode() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node initVal = Node.newString("init");
    Node nameNode = Node.newString(Token.NAME, "a");
    nameNode.addChildToFront(initVal);
    Node varNode = new Node(Token.VAR, nameNode);

    Scope.Var var = scope.declare("a", nameNode, null, null);
    assertSame(varNode, var.getParentNode());
    assertSame(initVal, var.getInitialValue());
  }

  // Tests Scope.Var initial value resolution for ASSIGN expression
  @Test
  public void testVar_getInitialValue_fromAssignNode() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "b");
    Node rightVal = Node.newString("val");
    new Node(Token.ASSIGN, nameNode, rightVal);

    Scope.Var var = scope.declare("b", nameNode, null, null);
    assertSame(rightVal, var.getInitialValue());
  }

  // Tests Scope.Var equals, hashCode and toString
  @Test
  public void testVar_equalsAndHashCodeAndToString() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode1 = Node.newString(Token.NAME, "x");
    Scope.Var var1 = scope.declare("x", nameNode1, null, null);

    assertEquals(var1, var1);
    assertFalse(var1.equals("non-var-object"));
    assertEquals(nameNode1.hashCode(), var1.hashCode());
    assertTrue(var1.toString().contains("x"));
    assertEquals("<non-file>", var1.getInputName());
    assertFalse(var1.isNoShadow());
    assertFalse(var1.isDefine());
    assertFalse(var1.isConst());
    assertSame(var1, var1.getSymbol());
    assertSame(var1, var1.getDeclaration());
  }

  // Tests getDeclarativelyUnboundVarsWithoutTypes filter
  @Test
  public void testGetDeclarativelyUnboundVarsWithoutTypes_filtersCorrectly() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode1 = Node.newString(Token.NAME, "unbound");
    new Node(Token.VAR, nameNode1);
    scope.declare("unbound", nameNode1, null, null, false);

    Node nameNode2 = Node.newString(Token.NAME, "other");
    new Node(Token.EXPR_RESULT, nameNode2);
    scope.declare("other", nameNode2, null, null, false);

    Iterator<Scope.Var> unboundVars = scope.getDeclarativelyUnboundVarsWithoutTypes();
    assertTrue(unboundVars.hasNext());
    assertEquals("unbound", unboundVars.next().getName());
    assertFalse(unboundVars.hasNext());
  }

  // Tests getVars, getAllSymbols, and getReferences iteration
  @Test
  public void testScope_collectionsAndIterables() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node node1 = Node.newString(Token.NAME, "v1");
    Node node2 = Node.newString(Token.NAME, "v2");
    Scope.Var var1 = scope.declare("v1", node1, null, null);
    Scope.Var var2 = scope.declare("v2", node2, null, null);

    assertEquals(2, Iterables.size(scope.getAllSymbols()));
    Iterator<Scope.Var> it = scope.getVars();
    assertTrue(it.hasNext());
    assertSame(var1, it.next());
    assertTrue(it.hasNext());
    assertSame(var2, it.next());
    assertFalse(it.hasNext());

    Iterable<Scope.Var> refs = scope.getReferences(var1);
    assertTrue(refs.iterator().hasNext());
    assertSame(var1, refs.iterator().next());
    assertSame(scope, scope.getScope(var1));
  }
}
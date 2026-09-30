package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class ScopeTest {

  private Compiler compiler;
  private Node globalRoot;
  private Scope globalScope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    globalRoot = new Node(Token.BLOCK);
    globalScope = new Scope(globalRoot, compiler);
  }

  // Tests global scope creation and properties
  @Test
  public void testGlobalScope_initialState_propertiesMatch() {
    assertTrue(globalScope.isGlobal());
    assertFalse(globalScope.isLocal());
    assertFalse(globalScope.isBottom());
    assertEquals(0, globalScope.getDepth());
    assertEquals(0, globalScope.getVarCount());
    assertSame(globalRoot, globalScope.getRootNode());
    assertNull(globalScope.getParent());
    assertNull(globalScope.getParentScope());
    assertSame(globalScope, globalScope.getGlobalScope());
  }

  // Tests bottom scope creation
  @Test
  public void testBottomScope_creation_isBottomTrue() {
    Node node = new Node(Token.BLOCK);
    Scope bottomScope = new Scope(node, (ObjectType) null);
    assertTrue(bottomScope.isBottom());
    assertEquals(0, bottomScope.getDepth());
    assertNull(bottomScope.getParent());
  }

  // Tests child scope hierarchy and depth
  @Test
  public void testChildScope_nesting_correctDepthAndParent() {
    Node funcNode1 = new Node(Token.FUNCTION);
    Scope childScope1 = new Scope(globalScope, funcNode1);

    Node funcNode2 = new Node(Token.FUNCTION);
    Scope childScope2 = new Scope(childScope1, funcNode2);

    assertFalse(childScope1.isGlobal());
    assertTrue(childScope1.isLocal());
    assertEquals(1, childScope1.getDepth());
    assertSame(globalScope, childScope1.getParent());
    assertSame(globalScope, childScope1.getGlobalScope());

    assertEquals(2, childScope2.getDepth());
    assertSame(childScope1, childScope2.getParent());
    assertSame(globalScope, childScope2.getGlobalScope());
  }

  // Tests declaring and retrieving variables in global scope
  @Test
  public void testDeclare_newVariable_canBeRetrieved() {
    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var var = globalScope.declare("x", nameNode, null, null);

    assertNotNull(var);
    assertEquals("x", var.getName());
    assertSame(nameNode, var.getNode());
    assertSame(nameNode, var.getNameNode());
    assertSame(var, var.getSymbol());
    assertSame(var, var.getDeclaration());
    assertSame(globalScope, var.getScope());
    assertEquals(1, globalScope.getVarCount());

    assertSame(var, globalScope.getVar("x"));
    assertSame(var, globalScope.getSlot("x"));
    assertSame(var, globalScope.getOwnSlot("x"));
    assertTrue(globalScope.isDeclared("x", false));
  }

  // Tests variable lookup traversing parent scopes
  @Test
  public void testGetVar_inParentScope_foundViaRecursion() {
    Node nameNode = Node.newString(Token.NAME, "parentVar");
    Scope.Var var = globalScope.declare("parentVar", nameNode, null, null);

    Node funcNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, funcNode);

    assertSame(var, childScope.getVar("parentVar"));
    assertSame(var, childScope.getSlot("parentVar"));
    assertNull(childScope.getOwnSlot("parentVar"));
    assertTrue(childScope.isDeclared("parentVar", true));
    assertFalse(childScope.isDeclared("parentVar", false));
  }

  // Tests declaring duplicate variable in same scope throws exception
  @Test(expected = IllegalStateException.class)
  public void testDeclare_duplicateName_throwsException() {
    Node name1 = Node.newString(Token.NAME, "a");
    Node name2 = Node.newString(Token.NAME, "a");
    globalScope.declare("a", name1, null, null);
    globalScope.declare("a", name2, null, null);
  }

  // Tests declaring variable with empty name throws exception
  @Test(expected = IllegalStateException.class)
  public void testDeclare_emptyName_throwsException() {
    Node nameNode = Node.newString(Token.NAME, "");
    globalScope.declare("", nameNode, null, null);
  }

  // Tests undeclaring a variable
  @Test
  public void testUndeclare_existingVariable_removedFromScope() {
    Node nameNode = Node.newString(Token.NAME, "y");
    Scope.Var var = globalScope.declare("y", nameNode, null, null);
    assertEquals(1, globalScope.getVarCount());

    globalScope.undeclare(var);
    assertEquals(0, globalScope.getVarCount());
    assertNull(globalScope.getVar("y"));
    assertNull(globalScope.getOwnSlot("y"));
    assertFalse(globalScope.isDeclared("y", false));
  }

  // Tests undeclaring a variable belonging to another scope throws exception
  @Test(expected = IllegalStateException.class)
  public void testUndeclare_varFromDifferentScope_throwsException() {
    Node funcNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, funcNode);
    Node nameNode = Node.newString(Token.NAME, "z");
    Scope.Var var = globalScope.declare("z", nameNode, null, null);

    childScope.undeclare(var);
  }

  // Tests getArgumentsVar creation and identity
  @Test
  public void testGetArgumentsVar_returnsSameInstance() {
    Scope.Var args1 = globalScope.getArgumentsVar();
    Scope.Var args2 = globalScope.getArgumentsVar();
    assertNotNull(args1);
    assertSame(args1, args2);
    assertEquals("arguments", args1.getName());
    assertNull(args1.getNode());
    assertNull(args1.getDeclaration());
    assertNull(args1.getParentNode());
  }

  // Tests Scope.Arguments equals and hashCode
  @Test
  public void testArguments_equalsAndHashCode() {
    Scope.Var args1 = globalScope.getArgumentsVar();
    Node funcNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, funcNode);
    Scope.Var args2 = childScope.getArgumentsVar();

    assertEquals(args1, args1);
    assertFalse(args1.equals(args2));
    assertFalse(args1.equals(null));
    assertFalse(args1.equals("arguments"));
  }

  // Tests Var properties: isGlobal, isLocal, isExtern, getInputName
  @Test
  public void testVar_localityAndInputProperties() {
    Node globalName = Node.newString(Token.NAME, "g");
    Scope.Var globalVar = globalScope.declare("g", globalName, null, null);
    assertTrue(globalVar.isGlobal());
    assertFalse(globalVar.isLocal());
    assertTrue(globalVar.isExtern());
    assertEquals("<non-file>", globalVar.getInputName());

    Node funcNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, funcNode);
    Node localName = Node.newString(Token.NAME, "l");
    Scope.Var localVar = childScope.declare("l", localName, null, null);
    assertFalse(localVar.isGlobal());
    assertTrue(localVar.isLocal());
  }

  // Tests Var.getInitialValue under different AST parent configurations
  @Test
  public void testVar_getInitialValue_variousParentNodes() {
    // Parent is VAR
    Node varParent = new Node(Token.VAR);
    Node nameNode1 = Node.newString(Token.NAME, "v");
    Node valueNode = Node.newNumber(42);
    nameNode1.addChildToFront(valueNode);
    varParent.addChildToFront(nameNode1);
    Scope.Var var1 = globalScope.declare("v", nameNode1, null, null);
    assertSame(valueNode, var1.getInitialValue());

    // Parent is ASSIGN
    Node assignParent = new Node(Token.ASSIGN);
    Node nameNode2 = Node.newString(Token.NAME, "a");
    Node assignVal = Node.newString("hello");
    assignParent.addChildToFront(nameNode2);
    assignParent.addChildToBack(assignVal);
    Scope.Var var2 = globalScope.declare("a", nameNode2, null, null);
    assertSame(assignVal, var2.getInitialValue());

    // Parent is FUNCTION
    Node funcParent = new Node(Token.FUNCTION);
    Node nameNode3 = Node.newString(Token.NAME, "f");
    funcParent.addChildToFront(nameNode3);
    Scope.Var var3 = globalScope.declare("f", nameNode3, null, null);
    assertSame(funcParent, var3.getInitialValue());

    // Parent is EXPR_RESULT (unsupported for initial value)
    Node exprParent = new Node(Token.EXPR_RESULT);
    Node nameNode4 = Node.newString(Token.NAME, "e");
    exprParent.addChildToFront(nameNode4);
    Scope.Var var4 = globalScope.declare("e", nameNode4, null, null);
    assertNull(var4.getInitialValue());
  }

  // Tests Var.setType on inferred vs non-inferred types
  @Test
  public void testVar_setType_inferredAllowedNonInferredThrows() {
    Node nameNode1 = Node.newString(Token.NAME, "inferredVar");
    Scope.Var inferredVar = globalScope.declare("inferredVar", nameNode1, null, null, true);
    assertTrue(inferredVar.isTypeInferred());
    inferredVar.setType(null);

    Node nameNode2 = Node.newString(Token.NAME, "declaredVar");
    Scope.Var declaredVar = globalScope.declare("declaredVar", nameNode2, null, null, false);
    assertFalse(declaredVar.isTypeInferred());
  }

  // Tests Var.setType throwing exception when type is not inferred
  @Test(expected = IllegalStateException.class)
  public void testVar_setType_declaredVarThrowsException() {
    Node nameNode = Node.newString(Token.NAME, "strictVar");
    Scope.Var declaredVar = globalScope.declare("strictVar", nameNode, null, null, false);
    declaredVar.setType(null);
  }

  // Tests Var equals, hashCode, and toString
  @Test
  public void testVar_equalsHashCodeToString() {
    Node nameNode1 = Node.newString(Token.NAME, "v1");
    Node nameNode2 = Node.newString(Token.NAME, "v2");
    Scope.Var var1 = globalScope.declare("v1", nameNode1, null, null);
    Scope.Var var2 = globalScope.declare("v2", nameNode2, null, null);

    assertEquals(var1, var1);
    assertFalse(var1.equals(var2));
    assertFalse(var1.equals(null));
    assertFalse(var1.equals("string"));
    assertEquals(nameNode1.hashCode(), var1.hashCode());
    assertTrue(var1.toString().contains("v1"));
  }

  // Tests getDeclarativelyUnboundVarsWithoutTypes filtering
  @Test
  public void testGetDeclarativelyUnboundVarsWithoutTypes_filtersCorrectly() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "unbound");
    varNode.addChildToFront(nameNode);
    globalScope.declare("unbound", nameNode, null, null);

    Iterator<Scope.Var> it = globalScope.getDeclarativelyUnboundVarsWithoutTypes();
    assertTrue(it.hasNext());
    Scope.Var found = it.next();
    assertEquals("unbound", found.getName());
    assertFalse(it.hasNext());
  }

  // Tests Scope helper methods: getAllSymbols, getVars, getReferences, getScope
  @Test
  public void testScope_iteratorsAndSymbolAccess() {
    Node nameNode = Node.newString(Token.NAME, "item");
    Scope.Var var = globalScope.declare("item", nameNode, null, null);

    assertEquals(1, globalScope.getAllSymbols().size());
    assertTrue(globalScope.getVars().hasNext());
    assertEquals(1, globalScope.getReferences(var).iterator().next() == var ? 1 : 0);
    assertSame(globalScope, globalScope.getScope(var));
  }

  // Tests child scope root node must differ from parent root node
  @Test(expected = IllegalArgumentException.class)
  public void testChildScope_sameRootAsParent_throwsException() {
    new Scope(globalScope, globalRoot);
  }

  // Tests createLatticeBottom factory method and type of this
  @Test
  public void testCreateLatticeBottom_createsBottomScope() {
    Node node = new Node(Token.BLOCK);
    Scope bottom = Scope.createLatticeBottom(node);
    assertTrue(bottom.isBottom());
    assertNull(bottom.getTypeOfThis());
    assertNull(globalScope.getTypeOfThis());
  }

  // Tests Var with CompilerInput and SourceFile
  @Test
  public void testVar_withCompilerInput_returnsInputAndSourceFile() {
    SourceFile sourceFile = SourceFile.fromCode("sample.js", "var test = 1;");
    CompilerInput input = new CompilerInput(sourceFile);
    Node nameNode = Node.newString(Token.NAME, "test");
    Scope.Var var = globalScope.declare("test", nameNode, null, input);

    assertSame(input, var.getInput());
    assertSame(sourceFile, var.getSourceFile());
    assertEquals("sample.js", var.getInputName());
    assertFalse(var.isExtern());
  }

  // Tests Var with JSDocInfo for const, define, and noShadow
  @Test
  public void testVar_jsDocInfo_constDefineAndNoShadow() {
    Node nameNode = Node.newString(Token.NAME, "docVar");
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordConstancy();
    builder.recordDefineType(null);
    builder.recordNoShadow();
    JSDocInfo info = builder.build(nameNode);
    nameNode.setJSDocInfo(info);

    Scope.Var var = globalScope.declare("docVar", nameNode, null, null);
    assertSame(info, var.getJSDocInfo());
    assertTrue(var.isConst());
    assertTrue(var.isDefine());
    assertTrue(var.isNoShadow());
  }

  // Tests bleeding function expression detection
  @Test
  public void testVar_isBleedingFunction_detectedCorrectly() {
    Node fnNode = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, "bleedFn");
    fnNode.addChildToFront(fnName);
    Scope.Var bleedingVar = globalScope.declare("bleedFn", fnName, null, null);
    assertTrue(bleedingVar.isBleedingFunction());

    Node varNode = new Node(Token.VAR);
    Node nonBleedName = Node.newString(Token.NAME, "normalVar");
    varNode.addChildToFront(nonBleedName);
    Scope.Var normalVar = globalScope.declare("normalVar", nonBleedName, null, null);
    assertFalse(normalVar.isBleedingFunction());
  }

  // Tests Arguments toString
  @Test
  public void testArguments_toString() {
    Scope.Var args = globalScope.getArgumentsVar();
    assertNotNull(args.toString());
    assertTrue(args.toString().contains("arguments"));
  }
}
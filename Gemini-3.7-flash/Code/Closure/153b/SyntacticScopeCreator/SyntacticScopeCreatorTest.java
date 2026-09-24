package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SyntacticScopeCreatorTest {
  private Compiler compiler;
  private SyntacticScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    scopeCreator = new SyntacticScopeCreator(compiler);
  }

  // Tests creating a global scope with a single var declaration
  @Test
  public void testCreateScope_globalVarDeclaration_scopeContainsVar() {
    Node root = compiler.parseTestCode("var a = 1;");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isGlobal());
    assertTrue(scope.isDeclared("a", false));
    assertNotNull(scope.getVar("a"));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests creating a global scope with multiple var declarations in one statement
  @Test
  public void testCreateScope_multipleVarsInOneStatement_allVarsDeclared() {
    Node root = compiler.parseTestCode("var a = 1, b = 2, c;");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests creating a global scope with a named function declaration
  @Test
  public void testCreateScope_functionDeclaration_declaresFunctionInGlobalScope() {
    Node root = compiler.parseTestCode("function foo() {}");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("foo", false));
    assertNotNull(scope.getVar("foo"));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests creating a function scope with arguments and local variables
  @Test
  public void testCreateScope_functionScopeWithArgsAndLocals_createsLocalScope() {
    Node root = compiler.parseTestCode("function foo(x, y) { var z = 10; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope fnScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(fnScope.isLocal());
    assertFalse(fnScope.isGlobal());
    assertTrue(fnScope.isDeclared("x", false));
    assertTrue(fnScope.isDeclared("y", false));
    assertTrue(fnScope.isDeclared("z", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests function expression name bleeding into local function scope
  @Test
  public void testCreateScope_functionExpressionName_bleedsIntoFunctionScope() {
    Node root = compiler.parseTestCode("var f = function bar() {};");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();

    Scope fnScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(fnScope.isDeclared("bar", false));
    assertFalse(globalScope.isDeclared("bar", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests catch block variable declaration and scoping
  @Test
  public void testCreateScope_catchBlock_declaresCatchVariable() {
    Node root = compiler.parseTestCode("try { } catch (e) { var x = 1; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("e", false));
    assertTrue(scope.isDeclared("x", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests duplicate catch variables are allowed without error
  @Test
  public void testCreateScope_duplicateCatchVariable_noError() {
    Node root = compiler.parseTestCode("try { } catch (e) { } try { } catch (e) { }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("e", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests error reporting for multiply declared variables in global scope
  @Test
  public void testCreateScope_duplicateGlobalVar_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("var a = 1; var a = 2;");
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate declaration allowed with @suppress {duplicate} JSDoc
  @Test
  public void testCreateScope_duplicateVarWithSuppression_noError() {
    Node root = compiler.parseTestCode("var a = 1; /** @suppress {duplicate} */ var a = 2;");
    scopeCreator.createScope(root, null);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests shadowing arguments variable in function parameter reports error
  @Test
  public void testCreateScope_shadowingArgumentsParameter_reportsError() {
    Node root = compiler.parseTestCode("function foo(arguments) {}");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(SyntacticScopeCreator.VAR_ARGUMENTS_SHADOWED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests declaring arguments via var in local scope does not report shadowed error
  @Test
  public void testCreateScope_argumentsAsVarInFunction_allowed() {
    Node root = compiler.parseTestCode("function foo() { var arguments = 1; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests anonymous function expression does not declare function name in outer scope
  @Test
  public void testCreateScope_anonymousFunctionExpression_noNameDeclared() {
    Node root = compiler.parseTestCode("(function() { var local = 1; })();");
    Scope scope = scopeCreator.createScope(root, null);

    assertFalse(scope.isDeclared("local", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests nested control structures containing var declarations
  @Test
  public void testCreateScope_varsInsideControlStructures_hoistedToScope() {
    Node root = compiler.parseTestCode(
        "if (true) { for (var i = 0; i < 10; i++) { while (false) { var nested = 1; } } }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("i", false));
    assertTrue(scope.isDeclared("nested", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests custom RedeclarationHandler invocation
  @Test
  public void testCustomRedeclarationHandler_invokedOnDuplicate() {
    final boolean[] called = new boolean[] { false };
    SyntacticScopeCreator.RedeclarationHandler customHandler =
        new SyntacticScopeCreator.RedeclarationHandler() {
          public void onRedeclaration(
              Scope s, String name, Node n, Node parent, Node gramps, Node nodeWithLineNumber) {
            called[0] = true;
            assertEquals("dup", name);
          }
        };

    SyntacticScopeCreator customCreator = new SyntacticScopeCreator(compiler, customHandler);
    Node root = compiler.parseTestCode("var dup = 1; var dup = 2;");
    customCreator.createScope(root, null);

    assertTrue(called[0]);
  }
}
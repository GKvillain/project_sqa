package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;

public class SyntacticScopeCreatorTest {

  private Compiler compiler;
  private SyntacticScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    scopeCreator = new SyntacticScopeCreator(compiler);
  }

  // Tests global scope creation and variable declaration
  @Test
  public void testCreateScope_globalVarDeclaration_declaresVariable() {
    Node root = compiler.parseTestCode("var x = 1;");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isGlobal());
    assertTrue(scope.isDeclared("x", false));
    assertNotNull(scope.getVar("x"));
  }

  // Tests multiple global variable declarations in a single VAR statement
  @Test
  public void testCreateScope_multipleVarsInSingleStatement_declaresAllVariables() {
    Node root = compiler.parseTestCode("var a = 1, b = 2, c;");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
    assertNull(scope.getParent());
  }

  // Tests named function declaration in global scope
  @Test
  public void testCreateScope_functionDeclaration_declaresFunctionNameInScope() {
    Node root = compiler.parseTestCode("function foo() {}");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("foo", false));
    assertNotNull(scope.getVar("foo"));
  }

  // Tests function parameters and local variables in local scope
  @Test
  public void testCreateScope_functionParametersAndLocalVars_declaredInLocalScope() {
    Node root = compiler.parseTestCode("function foo(param1, param2) { var local1 = 1; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("param1", false));
    assertTrue(localScope.isDeclared("param2", false));
    assertTrue(localScope.isDeclared("local1", false));
    assertTrue(localScope.isDeclared("arguments", false));
  }

  // Tests named function expression bleeding name into inner function scope
  @Test
  public void testCreateScope_namedFunctionExpression_bleedsIntoLocalScope() {
    Node root = compiler.parseTestCode("var f = function fnExpr() { var inner = 2; };");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node varNode = root.getFirstChild();
    Node fnNode = varNode.getFirstChild().getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("fnExpr", false));
    assertTrue(localScope.isDeclared("inner", false));
  }

  // Tests anonymous function expression does not bleed empty name
  @Test
  public void testCreateScope_anonymousFunctionExpression_doesNotThrow() {
    Node root = compiler.parseTestCode("(function() { var local = 1; })();");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node exprNode = root.getFirstChild();
    Node callNode = exprNode.getFirstChild();
    Node fnNode = callNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("local", false));
    assertFalse(localScope.isDeclared("", false));
  }

  // Tests catch statement variable declaration and block scanning
  @Test
  public void testCreateScope_catchBlock_declaresCatchVariableAndInnerVars() {
    Node root = compiler.parseTestCode("try { var a = 1; } catch (e) { var b = 2; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("e", false));
    assertTrue(scope.isDeclared("b", false));
  }

  // Tests control structures containing variable declarations
  @Test
  public void testCreateScope_controlStructures_scansNestedBlocks() {
    Node root = compiler.parseTestCode(
        "if (true) { var x = 1; } else { var y = 2; } "
        + "while (false) { var z = 3; } "
        + "for (var i = 0; i < 10; i++) { var j = 4; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("y", false));
    assertTrue(scope.isDeclared("z", false));
    assertTrue(scope.isDeclared("i", false));
    assertTrue(scope.isDeclared("j", false));
  }

  // Tests duplicate variable declaration in global scope reports error
  @Test
  public void testCreateScope_duplicateGlobalVar_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("var duplicate = 1; var duplicate = 2;");
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate global variable with @suppress {duplicate} annotation
  @Test
  public void testCreateScope_duplicateGlobalVarWithSuppression_noErrorReported() {
    Node root = compiler.parseTestCode(
        "var duplicate = 1; /** @suppress {duplicate} */ var duplicate = 2;");
    scopeCreator.createScope(root, null);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests duplicate catch variables are permitted without error
  @Test
  public void testCreateScope_duplicateCatchVariables_allowedWithoutError() {
    Node root = compiler.parseTestCode(
        "try {} catch (e) {} try {} catch (e) {}");
    scopeCreator.createScope(root, null);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests shadowing arguments as parameter in function scope reports error
  @Test
  public void testCreateScope_argumentsAsFunctionParam_reportsShadowedError() {
    Node root = compiler.parseTestCode("function test(arguments) {}");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_ARGUMENTS_SHADOWED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests custom RedeclarationHandler invocation on redeclaration
  @Test
  public void testCreateScope_customRedeclarationHandler_invokesHandlerOnDuplicate() {
    final boolean[] handlerCalled = new boolean[1];
    SyntacticScopeCreator.RedeclarationHandler customHandler =
        new SyntacticScopeCreator.RedeclarationHandler() {
          @Override
          public void onRedeclaration(
              Scope s, String name, Node n, Node parent, Node gramps, Node nodeWithLineNumber) {
            handlerCalled[0] = true;
            assertEquals("dupVar", name);
          }
        };

    SyntacticScopeCreator customScopeCreator =
        new SyntacticScopeCreator(compiler, customHandler);
    Node root = compiler.parseTestCode("var dupVar = 1; var dupVar = 2;");
    customScopeCreator.createScope(root, null);

    assertTrue(handlerCalled[0]);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests for-in loop variable declarations
  @Test
  public void testCreateScope_forInLoop_declaresLoopVariable() {
    Node root = compiler.parseTestCode("for (var key in obj) { var val = obj[key]; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("key", false));
    assertTrue(scope.isDeclared("val", false));
  }

  // Tests do-while loop variable declarations
  @Test
  public void testCreateScope_doWhileLoop_declaresLoopBodyVariable() {
    Node root = compiler.parseTestCode("do { var loopVar = 1; } while (false);");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("loopVar", false));
  }

  // Tests switch statement variable declarations in cases and default blocks
  @Test
  public void testCreateScope_switchStatement_declaresCaseAndDefaultVariables() {
    Node root = compiler.parseTestCode(
        "switch (x) { case 1: var case1 = 1; break; default: var def = 2; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("case1", false));
    assertTrue(scope.isDeclared("def", false));
  }

  // Tests labeled statement variable declarations
  @Test
  public void testCreateScope_labeledStatement_declaresLabeledVariable() {
    Node root = compiler.parseTestCode("myLabel: var labeledVar = 10;");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("labeledVar", false));
  }

  // Tests with statement variable declarations
  @Test
  public void testCreateScope_withStatement_declaresWithBodyVariable() {
    Node root = compiler.parseTestCode("with (obj) { var withVar = 10; }");
    Scope scope = scopeCreator.createScope(root, null);

    assertTrue(scope.isDeclared("withVar", false));
  }

  // Tests duplicate parameter names in function declaration report error
  @Test
  public void testCreateScope_duplicateFunctionParams_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("function test(p, p) {}");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate var named 'arguments' declared in function body reports error
  @Test
  public void testCreateScope_varArgumentsInFunctionBody_reportsShadowedError() {
    Node root = compiler.parseTestCode("function test() { var arguments = 1; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_ARGUMENTS_SHADOWED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate function declarations in global scope report error
  @Test
  public void testCreateScope_duplicateFunctionDeclarations_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("function f() {} function f() {}");
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate function declarations with @suppress {duplicate} on function
  @Test
  public void testCreateScope_duplicateFunctionWithSuppression_noErrorReported() {
    Node root = compiler.parseTestCode(
        "function f() {} /** @suppress {duplicate} */ function f() {}");
    scopeCreator.createScope(root, null);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests duplicate variable with @suppress {duplicate} on the first declaration
  @Test
  public void testCreateScope_duplicateVarWithSuppressionOnFirst_noErrorReported() {
    Node root = compiler.parseTestCode(
        "/** @suppress {duplicate} */ var duplicate = 1; var duplicate = 2;");
    scopeCreator.createScope(root, null);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests local scope duplicate variable declaration reports error
  @Test
  public void testCreateScope_duplicateLocalVar_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("function test() { var x = 1; var x = 2; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    scopeCreator.createScope(fnNode, globalScope);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // Tests var declared inside catch block having same name as catch variable
  @Test
  public void testCreateScope_varWithSameNameAsCatchVar_reportsMultiplyDeclaredError() {
    Node root = compiler.parseTestCode("try {} catch (e) { var e = 1; }");
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(
        SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }
}
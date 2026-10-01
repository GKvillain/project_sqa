package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests splitting multiple variable declarations in a single var statement
  @Test
  public void testProcess_splitVarDeclarations_splitsMultipleVars() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(2, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertEquals(Token.VAR, root.getLastChild().getType());
  }

  // Tests converting a while loop into a for loop
  @Test
  public void testProcess_whileLoop_convertsToForLoop() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("while (x) { foo(); }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node firstStmt = root.getFirstChild();
    assertEquals(Token.FOR, firstStmt.getType());
    assertEquals(Token.EMPTY, firstStmt.getFirstChild().getType());
  }

  // Tests extracting initializers out of a standard for loop
  @Test
  public void testProcess_forInitializer_movesInitializerBeforeLoop() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("for (var i = 0; i < 10; i++) {}");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertEquals(Token.FOR, root.getLastChild().getType());
    assertEquals(Token.EMPTY, root.getLastChild().getFirstChild().getType());
  }

  // Tests extracting var declaration out of a for-in loop
  @Test
  public void testProcess_forInVarDeclaration_extractsVar() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("for (var a in b) {}");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertEquals(Token.FOR, root.getLastChild().getType());
    assertEquals(Token.NAME, root.getLastChild().getFirstChild().getType());
  }

  // Tests moving unhoisted function declarations into var declarations
  @Test
  public void testProcess_unhoistedFunctionDeclaration_rewritesToVar() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("if (true) { function f() {} }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node ifBlock = root.getFirstChild().getLastChild();
    assertEquals(Token.VAR, ifBlock.getFirstChild().getType());
  }

  // Tests moving hoisted function declarations to the top of the function scope
  @Test
  public void testProcess_hoistedFunctionDeclaration_movesToTopOfScope() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("function outer() { var x = 1; function inner() {} }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node fnBody = root.getFirstChild().getLastChild();
    assertEquals(Token.FUNCTION, fnBody.getFirstChild().getType());
    assertEquals(Token.VAR, fnBody.getChildAtIndex(1).getType());
  }

  // Tests removing duplicate var declarations within the same scope
  @Test
  public void testProcess_duplicateVarDeclaration_replacesWithAssignment() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var a = 1; var a = 2;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertEquals(Token.EXPR_RESULT, root.getLastChild().getType());
    assertEquals(Token.ASSIGN, root.getLastChild().getFirstChild().getType());
  }

  // Tests removing empty duplicate var declaration
  @Test
  public void testProcess_duplicateVarDeclarationNoInit_removesDuplicate() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var a = 1; var a;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests error reporting when catch variable is redeclared in catch block
  @Test
  public void testProcess_catchBlockVarRedeclaration_reportsError() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("function f() { try {} catch (e) { var e = 1; } }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR.key, compiler.getErrors()[0].getType().key);
  }

  // Tests normalizing non-block labeled statements into blocks
  @Test
  public void testProcess_labelWithoutBlock_wrapsInBlock() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("label: x = 1;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals(Token.BLOCK, labelNode.getLastChild().getType());
  }

  // Tests constant annotation propagation based on naming conventions
  @Test
  public void testProcess_constantNamingConvention_marksConstant() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var CONST_VAL = 1; var x = CONST_VAL;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node firstVar = root.getFirstChild();
    assertTrue(firstVar.getFirstChild().getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests assertOnChange throws IllegalStateException when modifications occur
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChangeTrue_throwsExceptionOnAstChange() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests VerifyConstants passes when constant usage is consistent
  @Test
  public void testVerifyConstants_consistentConstantUsage_succeeds() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var CONST_VAL = 1; var b = CONST_VAL;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, false);
    verify.process(externs, root);
  }

  // Tests parseAndNormalizeSyntheticCode static utility method
  @Test
  public void testParseAndNormalizeSyntheticCode_validInput_returnsNormalizedTree() {
    Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, "var a = 1, b = 2;", "prefix_");
    assertNotNull(result);
    assertEquals(2, result.getChildCount());
  }

  // Tests parseAndNormalizeTestCode static utility method
  @Test
  public void testParseAndNormalizeTestCode_validInput_returnsNormalizedTree() {
    Node result = Normalize.parseAndNormalizeTestCode(compiler, "while(x) { foo(); }", "prefix_");
    assertNotNull(result);
    assertEquals(Token.FOR, result.getFirstChild().getType());
  }

  // Tests PropagateConstantAnnotationsOverVars pass directly
  @Test
  public void testPropagateConstantAnnotationsOverVars_constantName_annotated() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var CONST_NAME = 10;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);

    Node nameNode = root.getFirstChild().getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests removing empty statements from block/root
  @Test
  public void testProcess_emptyStatements_removesEmptyNodes() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("; var a = 1; ; ;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests wrapping non-block then and else branches into blocks
  @Test
  public void testProcess_ifWithoutBlocks_wrapsInBlocks() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("if (x) foo(); else bar();");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node thenBlock = ifNode.getChildAtIndex(1);
    Node elseBlock = ifNode.getChildAtIndex(2);
    assertEquals(Token.BLOCK, thenBlock.getType());
    assertEquals(Token.BLOCK, elseBlock.getType());
  }

  // Tests wrapping do-while body without block into a block
  @Test
  public void testProcess_doWhileWithoutBlock_wrapsInBlock() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("do foo(); while (x);");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node doNode = root.getFirstChild();
    assertEquals(Token.DO, doNode.getType());
    assertEquals(Token.BLOCK, doNode.getFirstChild().getType());
  }

  // Tests duplicate var declaration redeclaring a function parameter with initialization
  @Test
  public void testProcess_varRedeclaringParameterWithInit_replacesWithAssign() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("function f(x) { var x = 1; }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node fnBody = root.getFirstChild().getLastChild();
    assertEquals(1, fnBody.getChildCount());
    assertEquals(Token.EXPR_RESULT, fnBody.getFirstChild().getType());
    assertEquals(Token.ASSIGN, fnBody.getFirstChild().getFirstChild().getType());
  }

  // Tests duplicate var declaration redeclaring a function parameter without initialization
  @Test
  public void testProcess_varRedeclaringParameterNoInit_removesVar() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("function f(x) { var x; }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node fnBody = root.getFirstChild().getLastChild();
    assertEquals(0, fnBody.getChildCount());
  }

  // Tests uninitialized var followed by initialized duplicate var
  @Test
  public void testProcess_uninitializedVarFollowedByInitVar_turnsSecondIntoAssignment() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var a; var a = 1;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(2, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertEquals(Token.EXPR_RESULT, root.getLastChild().getType());
  }

  // Tests duplicate function declarations in the same scope
  @Test
  public void testProcess_duplicateFunctionDeclarations_hoistsFunctions() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("function f() { return 1; } function f() { return 2; }");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(2, root.getChildCount());
    assertEquals(Token.FUNCTION, root.getFirstChild().getType());
    assertEquals(Token.FUNCTION, root.getLastChild().getType());
  }

  // Tests JSDoc @const annotation propagation
  @Test
  public void testPropagateConstantAnnotationsOverVars_jsdocConst_marksConstant() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("/** @const */ var x = 1; var y = x;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);

    Node varNode = root.getFirstChild();
    assertTrue(varNode.getFirstChild().getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests VerifyConstants with checkModified mode enabled
  @Test
  public void testVerifyConstants_checkModified_consistentConstants_succeeds() {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var CONST_VAL = 1; var b = CONST_VAL;");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, true);
    verify.process(externs, root);
  }
}
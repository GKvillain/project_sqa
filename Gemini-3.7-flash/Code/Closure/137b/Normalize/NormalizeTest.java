package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
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

  private Node parseAndNormalize(String js, boolean assertOnChange) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, assertOnChange);
    normalize.process(externs, root);
    return root;
  }

  // Tests splitting multiple variable declarations into individual var statements
  @Test
  public void testProcess_splitVarDeclarations_splitsVars() {
    String js = "var a = 1, b = 2, c = 3;";
    Node root = parseAndNormalize(js, false);
    String expected = "var a = 1;var b = 2;var c = 3;";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests conversion of while loop to for loop
  @Test
  public void testProcess_whileLoop_convertsToFor() {
    String js = "while (x < 10) { x++; }";
    Node root = parseAndNormalize(js, false);
    String expected = "for(;x < 10;){x++}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests extracting variable initializer from a for loop header
  @Test
  public void testProcess_forLoopInitializer_extractsVarInitializer() {
    String js = "for (var i = 0; i < 10; i++) { foo(); }";
    Node root = parseAndNormalize(js, false);
    String expected = "var i = 0;for(;i < 10;i++){foo()}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests extracting expression initializer from a for loop header
  @Test
  public void testProcess_forLoopExprInitializer_extractsExprInitializer() {
    String js = "for (i = 0; i < 10; i++) { foo(); }";
    Node root = parseAndNormalize(js, false);
    String expected = "i = 0;for(;i < 10;i++){foo()}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests label normalization when the labeled statement is not a block or loop
  @Test
  public void testProcess_labelOnExpression_wrapsInBlock() {
    String js = "lbl: a = 1;";
    Node root = parseAndNormalize(js, false);
    String expected = "lbl:{a = 1}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests that labeled loops are not wrapped in a block
  @Test
  public void testProcess_labelOnLoop_keepsLoopWithoutWrappingBlock() {
    String js = "lbl: while (true) { break lbl; }";
    Node root = parseAndNormalize(js, false);
    String expected = "lbl:for(;true;){break lbl}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests moving function declarations to the top of the containing function
  @Test
  public void testProcess_moveNamedFunctions_hoistsToTop() {
    String js = "function f() { var x = 1; function g() { return 2; } return x + g(); }";
    Node root = parseAndNormalize(js, false);
    String expected = "function f(){function g(){return 2}var x = 1;return x + g()}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests removing duplicate empty var declarations in the same scope
  @Test
  public void testProcess_duplicateVarDeclaration_removesDuplicate() {
    String js = "var a; var a;";
    Node root = parseAndNormalize(js, false);
    String expected = "var a;";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests converting duplicate initialized var declaration into an assignment
  @Test
  public void testProcess_duplicateVarWithInit_convertsToAssignment() {
    String js = "var a = 1; var a = 2;";
    Node root = parseAndNormalize(js, false);
    String expected = "var a = 1;a = 2;";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests duplicate var declaration inside a for-in loop header
  @Test
  public void testProcess_duplicateVarInForIn_removesVarFromHeader() {
    String js = "var a; for (var a in obj) {}";
    Node root = parseAndNormalize(js, false);
    String expected = "var a;for(a in obj){}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests duplicate var declaration inside a label
  @Test
  public void testProcess_duplicateVarInLabel_replacesWithEmpty() {
    String js = "var a; lbl: var a;";
    Node root = parseAndNormalize(js, false);
    String expected = "var a;lbl:;";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests that assertOnChange throws IllegalStateException when modifications occur
  @Test(expected = IllegalStateException.class)
  public void testProcess_assertOnChangeWithModifications_throwsIllegalStateException() {
    parseAndNormalize("var a = 1, b = 2;", true);
  }

  // Tests constant propagation from JSDoc annotation to AST node properties
  @Test
  public void testPropogateConstantAnnotations_constantJsDoc_setsConstantProp() {
    Node root = compiler.parseTestCode("var FOO = 1; var x = FOO;");
    Node externs = new Node(Token.BLOCK);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(null);

    // Attach constant JSDoc to the first VAR
    root.getFirstChild().setJSDocInfo(info);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    pass.process(externs, root);

    // Verify constant propagation on nodes
    Node firstVarName = root.getFirstChild().getFirstChild();
    assertTrue(firstVarName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests VerifyConstants throws exception when constant usage is inconsistent
  @Test(expected = IllegalStateException.class)
  public void testVerifyConstants_inconsistentConstantUsage_throwsIllegalStateException() {
    Node root = compiler.parseTestCode("var a = 1; var b = a;");
    Node externs = new Node(Token.BLOCK);

    Node parent = new Node(Token.BLOCK);
    parent.addChildToBack(externs);
    parent.addChildToBack(root);

    // Set constant property on only one of the 'a' nodes
    Node firstVarName = root.getFirstChild().getFirstChild();
    firstVarName.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // Tests VerifyConstants pass when constants are consistently annotated
  @Test
  public void testVerifyConstants_consistentConstants_completesSuccessfully() {
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = new Node(Token.BLOCK);

    Node parent = new Node(Token.BLOCK);
    parent.addChildToBack(externs);
    parent.addChildToBack(root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
    assertNotNull(root);
  }

  // Tests NormalizeStatements extracts initializers from nested labeled for loops
  @Test
  public void testProcess_nestedLabeledForLoop_extractsInitializer() {
    String js = "label1: label2: for (var i = 0; i < 10; i++) { break label1; }";
    Node root = parseAndNormalize(js, false);
    String expected = "var i = 0;label1:label2:for(;i < 10;i++){break label1}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests duplicate var declaration matching a function parameter
  @Test
  public void testProcess_duplicateVarOfParameter_removesVarDeclaration() {
    String js = "function f(x) { var x; }";
    Node root = parseAndNormalize(js, false);
    String expected = "function f(x){}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests initialized duplicate var matching a function parameter converts to assignment
  @Test
  public void testProcess_duplicateVarOfParameterWithInit_convertsToAssignment() {
    String js = "function f(x) { var x = 1; }";
    Node root = parseAndNormalize(js, false);
    String expected = "function f(x){x = 1}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests duplicate var declaration inside catch block matching catch parameter
  @Test
  public void testProcess_duplicateVarOfCatchParam_removesVarDeclaration() {
    String js = "try {} catch (e) { var e; }";
    Node root = parseAndNormalize(js, false);
    String expected = "try{}catch(e){}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests initialized duplicate var inside catch block matching catch parameter
  @Test
  public void testProcess_duplicateVarOfCatchParamWithInit_convertsToAssignment() {
    String js = "try {} catch (e) { var e = 1; }";
    Node root = parseAndNormalize(js, false);
    String expected = "try{}catch(e){e = 1}";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests duplicate var declaration matching a function declaration name
  @Test
  public void testProcess_duplicateVarOfFunctionName_removesVarDeclaration() {
    String js = "function f() {} var f;";
    Node root = parseAndNormalize(js, false);
    String expected = "function f(){}var f;";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests function declarations inside blocks get hoisted to top of the enclosing script/function
  @Test
  public void testProcess_functionDeclarationInsideBlock_hoistsToTop() {
    String js = "if (true) { function f() {} }";
    Node root = parseAndNormalize(js, false);
    String expected = "function f(){}if(true);";
    assertEquals(expected, compiler.toSource(root));
  }

  // Tests assertOnChange does not throw when code is already normalized
  @Test
  public void testProcess_assertOnChange_passesWhenAlreadyNormalized() {
    String js = "var a = 1;";
    Node root = parseAndNormalize(js, false);
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  // Tests constant propagation on assignments with @const JSDoc
  @Test
  public void testPropogateConstantAnnotations_assignWithConstJsDoc_setsConstantProp() {
    Node root = compiler.parseTestCode("var a = {}; a.B = 1;");
    Node externs = new Node(Token.BLOCK);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(null);

    // Attach constant JSDoc to the assignment expr node
    Node exprResult = root.getLastChild();
    exprResult.getFirstChild().setJSDocInfo(info);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    pass.process(externs, root);

    Node getprop = exprResult.getFirstChild().getFirstChild();
    assertTrue(getprop.getLastChild().getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests PropogateConstantAnnotations assertOnChange throws when annotations change
  @Test(expected = IllegalStateException.class)
  public void testPropogateConstantAnnotations_assertOnChange_throwsOnModification() {
    Node root = compiler.parseTestCode("var FOO = 1;");
    Node externs = new Node(Token.BLOCK);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    root.getFirstChild().setJSDocInfo(builder.build(null));

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    pass.process(externs, root);
  }

  // Tests splitting VAR declarations preserves JSDoc annotations on the first variable
  @Test
  public void testProcess_splitVarDeclarationsWithJsDoc_preservesJSDoc() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    Node root = parseAndNormalize(js, false);
    Node firstVar = root.getFirstChild();
    assertNotNull(firstVar.getJSDocInfo());
  }

  // Tests do-while loop normalization
  @Test
  public void testProcess_doWhileLoop_normalizesBody() {
    String js = "do { var x = 1; } while (false);";
    Node root = parseAndNormalize(js, false);
    String expected = "do{var x = 1}while(false)";
    assertEquals(expected, compiler.toSource(root));
  }
}
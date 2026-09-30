package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrepareAstTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests process method with null externs and null root
  @Test
  public void testProcess_nullNodes_doesNotThrow() {
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, null);
  }

  // Tests annotateCalls for a free function call
  @Test
  public void testProcess_freeCall_marksFreeCall() {
    Node root = compiler.parseTestCode("foo();");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node call = exprResult.getFirstChild();

    assertTrue(call.isCall());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

  // Tests annotateCalls for a property call which is not a free call
  @Test
  public void testProcess_methodCall_doesNotMarkFreeCall() {
    Node root = compiler.parseTestCode("obj.foo();");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node call = exprResult.getFirstChild();

    assertTrue(call.isCall());
    assertFalse(call.getBooleanProp(Node.FREE_CALL));
  }

  // Tests annotateCalls for a new expression
  @Test
  public void testProcess_newCall_doesNotMarkFreeCall() {
    Node root = compiler.parseTestCode("new Foo();");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node newExpr = exprResult.getFirstChild();

    assertTrue(newExpr.isNew());
    assertFalse(newExpr.getBooleanProp(Node.FREE_CALL));
  }

  // Tests annotateCalls for a direct eval() call
  @Test
  public void testProcess_directEvalCall_marksDirectEval() {
    Node root = compiler.parseTestCode("eval('1');");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node call = exprResult.getFirstChild();
    Node evalName = call.getFirstChild();

    assertTrue(call.isCall());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
    assertTrue(evalName.getBooleanProp(Node.DIRECT_EVAL));
  }

  // Tests annotateCalls for an indirect eval call via comma operator
  @Test
  public void testProcess_indirectEvalCall_notDirectEval() {
    Node root = compiler.parseTestCode("(0, eval)('1');");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node call = exprResult.getFirstChild();
    Node first = call.getFirstChild();

    assertTrue(call.isCall());
    assertFalse(first.getBooleanProp(Node.DIRECT_EVAL));
  }

  // Tests normalizeBlocks wrapping unblocked statement in an IF node
  @Test
  public void testProcess_ifWithoutBlock_wrapsInBlock() {
    Node root = compiler.parseTestCode("if (true) x = 1;");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node ifNode = script.getFirstChild();
    Node thenBlock = ifNode.getLastChild();

    assertTrue(thenBlock.isBlock());
    assertTrue(thenBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks wrapping unblocked statements in an IF-ELSE node
  @Test
  public void testProcess_ifElseWithoutBlocks_wrapsBothInBlocks() {
    Node root = compiler.parseTestCode("if (true) x = 1; else y = 2;");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node ifNode = script.getFirstChild();
    Node thenBlock = ifNode.getChildAtIndex(1);
    Node elseBlock = ifNode.getChildAtIndex(2);

    assertTrue(thenBlock.isBlock());
    assertTrue(thenBlock.getFirstChild().isExprResult());
    assertTrue(elseBlock.isBlock());
    assertTrue(elseBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks wrapping unblocked WHILE statement
  @Test
  public void testProcess_whileWithoutBlock_wrapsInBlock() {
    Node root = compiler.parseTestCode("while (true) x = 1;");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node whileNode = script.getFirstChild();
    Node bodyBlock = whileNode.getLastChild();

    assertTrue(bodyBlock.isBlock());
    assertTrue(bodyBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks wrapping unblocked FOR statement
  @Test
  public void testProcess_forWithoutBlock_wrapsInBlock() {
    Node root = compiler.parseTestCode("for (;;) x = 1;");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node forNode = script.getFirstChild();
    Node bodyBlock = forNode.getLastChild();

    assertTrue(bodyBlock.isBlock());
    assertTrue(bodyBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks wrapping unblocked FOR-IN statement
  @Test
  public void testProcess_forInWithoutBlock_wrapsInBlock() {
    Node root = compiler.parseTestCode("for (var p in obj) x = 1;");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node forInNode = script.getFirstChild();
    Node bodyBlock = forInNode.getLastChild();

    assertTrue(bodyBlock.isBlock());
    assertTrue(bodyBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks wrapping unblocked DO-WHILE statement
  @Test
  public void testProcess_doWhileWithoutBlock_wrapsInBlock() {
    Node root = compiler.parseTestCode("do x = 1; while (true);");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node doNode = script.getFirstChild();
    Node bodyBlock = doNode.getFirstChild();

    assertTrue(bodyBlock.isBlock());
    assertTrue(bodyBlock.getFirstChild().isExprResult());
  }

  // Tests normalizeBlocks with empty statement in IF
  @Test
  public void testProcess_ifWithEmptyStatement_setsWasEmptyNode() {
    Node root = compiler.parseTestCode("if (true);");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node ifNode = script.getFirstChild();
    Node thenBlock = ifNode.getLastChild();

    assertTrue(thenBlock.isBlock());
    assertTrue(thenBlock.getWasEmptyNode());
  }

  // Tests normalizeBlocks with empty statement in WHILE
  @Test
  public void testProcess_whileWithEmptyStatement_setsWasEmptyNode() {
    Node root = compiler.parseTestCode("while (true);");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node whileNode = script.getFirstChild();
    Node bodyBlock = whileNode.getLastChild();

    assertTrue(bodyBlock.isBlock());
    assertTrue(bodyBlock.getWasEmptyNode());
  }

  // Tests normalizeObjectLiteralAnnotations transfers JSDoc from key to function value
  @Test
  public void testProcess_objectLitKeyWithJsDoc_transfersToFunction() {
    Node root = compiler.parseTestCode("var obj = { /** @return {number} */ foo: function() { return 1; } };");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node objLit = nameNode.getFirstChild();
    Node keyNode = objLit.getFirstChild();
    Node fnNode = keyNode.getFirstChild();

    assertNotNull(fnNode.getJSDocInfo());
  }

  // Tests annotateDispatchers for function assigned with JavaDispatch JSDoc
  @Test
  public void testProcess_dispatcherFunction_marksIsDispatcher() {
    Node root = compiler.parseTestCode("/** @javadispatch */ var dispatch = function() {};");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();

    assertTrue(fnNode.getBooleanProp(Node.IS_DISPATCHER));
  }

  // Tests checkOnly mode on normalized valid AST does not throw
  @Test
  public void testProcess_checkOnlyMode_validAst_succeeds() {
    Node root = compiler.parseTestCode("if (true) { x = 1; }");
    PrepareAst normalizer = new PrepareAst(compiler, false);
    normalizer.process(null, root);

    PrepareAst checker = new PrepareAst(compiler, true);
    checker.process(null, root);
  }

  // Tests checkOnly mode with null root does not throw
  @Test
  public void testProcess_checkOnlyMode_nullRoot_doesNotThrow() {
    PrepareAst checker = new PrepareAst(compiler, true);
    checker.process(null, null);
  }

  // Tests checkOnly mode throws exception when AST requires block normalization
  @Test(expected = IllegalStateException.class)
  public void testProcess_checkOnlyMode_unnormalizedAst_throwsException() {
    Node ifNode = IR.ifNode(IR.name("cond"), IR.exprResult(IR.name("x")));
    Node script = IR.script(ifNode);

    PrepareAst checker = new PrepareAst(compiler, true);
    checker.process(null, script);
  }

  // Tests process on externs root
  @Test
  public void testProcess_externsRoot_annotatesCalls() {
    Node externs = compiler.parseTestCode("function ext() {}; ext();");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(externs, null);

    Node script = externs.getFirstChild();
    Node exprResult = script.getLastChild();
    Node call = exprResult.getFirstChild();

    assertTrue(call.isCall());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

  // Tests process on both externs and root simultaneously
  @Test
  public void testProcess_bothExternsAndRoot_annotatesBoth() {
    Node externs = compiler.parseTestCode("function ext() {}; ext();");
    Node root = compiler.parseTestCode("function main() {}; main();");
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(externs, root);

    Node externsCall = externs.getFirstChild().getLastChild().getFirstChild();
    Node rootCall = root.getFirstChild().getLastChild().getFirstChild();

    assertTrue(externsCall.isCall());
    assertTrue(externsCall.getBooleanProp(Node.FREE_CALL));
    assertTrue(rootCall.isCall());
    assertTrue(rootCall.getBooleanProp(Node.FREE_CALL));
  }
}
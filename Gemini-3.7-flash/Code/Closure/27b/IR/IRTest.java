package com.google.javascript.rhino;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class IRTest {

  // Tests tryFinally construction with block nodes (Defects4J Closure 27b target)
  @Test
  public void testTryFinally_validBlocks_createsTryNode() {
    Node tryBody = IR.block(IR.exprResult(IR.number(1)));
    Node finallyBody = IR.block(IR.exprResult(IR.number(2)));
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertEquals(Token.BLOCK, tryNode.getChildAtIndex(1).getType());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  // Tests tryCatch construction
  @Test
  public void testTryCatch_validBlocks_createsTryCatchNode() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node tryNode = IR.tryCatch(tryBody, catchNode);

    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(2, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertEquals(Token.BLOCK, tryNode.getLastChild().getType());
    assertSame(catchNode, tryNode.getLastChild().getFirstChild());
  }

  // Tests tryCatchFinally construction
  @Test
  public void testTryCatchFinally_validNodes_createsTryCatchFinallyNode() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node finallyBody = IR.block();
    Node tryNode = IR.tryCatchFinally(tryBody, catchNode, finallyBody);

    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  // Tests function construction with valid name, paramList, and body
  @Test
  public void testFunction_validInputs_createsFunctionNode() {
    Node name = IR.name("foo");
    Node params = IR.paramList(IR.name("a"), IR.name("b"));
    Node body = IR.block(IR.returnNode(IR.name("a")));
    Node func = IR.function(name, params, body);

    assertEquals(Token.FUNCTION, func.getType());
    assertSame(name, func.getFirstChild());
    assertSame(params, func.getChildAtIndex(1));
    assertSame(body, func.getChildAtIndex(2));
  }

  // Tests function exception when name is not a NAME token
  @Test(expected = IllegalStateException.class)
  public void testFunction_invalidName_throwsException() {
    IR.function(IR.string("notAName"), IR.paramList(), IR.block());
  }

  // Tests paramList with List collection
  @Test
  public void testParamList_fromList_createsParamList() {
    Node paramList = IR.paramList(Arrays.asList(IR.name("x"), IR.name("y")));
    assertEquals(Token.PARAM_LIST, paramList.getType());
    assertEquals(2, paramList.getChildCount());
    assertEquals("x", paramList.getFirstChild().getString());
    assertEquals("y", paramList.getLastChild().getString());
  }

  // Tests block creation with multiple statements
  @Test
  public void testBlock_multipleStatements_createsBlock() {
    Node stmt1 = IR.var(IR.name("a"), IR.number(1));
    Node stmt2 = IR.returnNode();
    Node block = IR.block(stmt1, stmt2);

    assertEquals(Token.BLOCK, block.getType());
    assertEquals(2, block.getChildCount());
  }

  // Tests block creation with statement list
  @Test
  public void testBlock_fromList_createsBlock() {
    Node block = IR.block(Arrays.asList(IR.exprResult(IR.nullNode())));
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(1, block.getChildCount());
  }

  // Tests block exception when statement is an expression
  @Test(expected = IllegalStateException.class)
  public void testBlock_invalidStatement_throwsException() {
    IR.block(IR.number(42));
  }

  // Tests var node creation with and without initial value
  @Test
  public void testVar_withAndWithoutValue_createsVarNode() {
    Node varWithoutVal = IR.var(IR.name("x"));
    assertEquals(Token.VAR, varWithoutVal.getType());
    assertEquals("x", varWithoutVal.getFirstChild().getString());

    Node varWithVal = IR.var(IR.name("y"), IR.string("val"));
    assertEquals(Token.VAR, varWithVal.getType());
    Node nameNode = varWithVal.getFirstChild();
    assertEquals("y", nameNode.getString());
    assertTrue(nameNode.hasChildren());
    assertEquals(Token.STRING, nameNode.getFirstChild().getType());
  }

  // Tests ifNode with 2 and 3 arguments
  @Test
  public void testIfNode_twoAndThreeArgs_createsIfNode() {
    Node cond = IR.trueNode();
    Node thenBranch = IR.block();
    Node elseBranch = IR.block();

    Node ifTwo = IR.ifNode(cond, thenBranch);
    assertEquals(Token.IF, ifTwo.getType());
    assertEquals(2, ifTwo.getChildCount());

    Node ifThree = IR.ifNode(cond, thenBranch, elseBranch);
    assertEquals(Token.IF, ifThree.getChildCount());
  }

  // Tests loop constructs: doNode, whileNode, forIn, forNode
  @Test
  public void testLoops_validInputs_createsLoopNodes() {
    Node doNode = IR.doNode(IR.block(), IR.falseNode());
    assertEquals(Token.DO, doNode.getType());

    Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
    assertEquals(Token.WHILE, whileNode.getType());

    Node forInNode = IR.forIn(IR.name("k"), IR.name("obj"), IR.block());
    assertEquals(Token.FOR, forInNode.getType());
    assertEquals(3, forInNode.getChildCount());

    Node forNode = IR.forNode(
        IR.var(IR.name("i"), IR.number(0)),
        IR.lt(IR.name("i"), IR.number(10)),
        IR.assign(IR.name("i"), IR.add(IR.name("i"), IR.number(1))),
        IR.block());
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(4, forNode.getChildCount());
  }

  // Tests switch, case, and defaultCase construction
  @Test
  public void testSwitchAndCase_validInputs_createsSwitchNode() {
    Node caseNode = IR.caseNode(IR.number(1), IR.block());
    Node defaultNode = IR.defaultCase(IR.block());
    Node switchNode = IR.switchNode(IR.name("x"), caseNode, defaultNode);

    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(3, switchNode.getChildCount());
    assertTrue(caseNode.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
    assertTrue(defaultNode.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
  }

  // Tests label, labelName, breakNode, and continueNode
  @Test
  public void testLabelAndJumpNodes_validInputs_createsExpectedNodes() {
    Node labelName = IR.labelName("loop");
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("loop", labelName.getString());

    Node labelNode = IR.label(labelName, IR.block());
    assertEquals(Token.LABEL, labelNode.getType());

    Node breakWithLabel = IR.breakNode(IR.labelName("loop"));
    assertEquals(Token.BREAK, breakWithLabel.getType());
    assertEquals(1, breakWithLabel.getChildCount());

    Node breakNoLabel = IR.breakNode();
    assertEquals(Token.BREAK, breakNoLabel.getType());
    assertEquals(0, breakNoLabel.getChildCount());

    Node contNoLabel = IR.continueNode();
    assertEquals(Token.CONTINUE, contNoLabel.getType());
    assertEquals(0, contNoLabel.getChildCount());

    Node contWithLabel = IR.continueNode(IR.labelName("loop"));
    assertEquals(Token.CONTINUE, contWithLabel.getType());
    assertEquals(1, contWithLabel.getChildCount());
  }

  // Tests call and newNode with arguments
  @Test
  public void testCallAndNew_validArgs_createsCallNodes() {
    Node target = IR.name("foo");
    Node arg1 = IR.number(1);
    Node arg2 = IR.string("a");

    Node callNode = IR.call(target, arg1, arg2);
    assertEquals(Token.CALL, callNode.getType());
    assertEquals(3, callNode.getChildCount());

    Node newNode = IR.newNode(target, arg1);
    assertEquals(Token.NEW, newNode.getType());
    assertEquals(2, newNode.getChildCount());
  }

  // Tests property and element access: getprop, getelem, assign, hook
  @Test
  public void testAccessAndAssignAndHook_validInputs_createsNodes() {
    Node prop = IR.getprop(IR.name("obj"), IR.string("prop"));
    assertEquals(Token.GETPROP, prop.getType());

    Node elem = IR.getelem(IR.name("arr"), IR.number(0));
    assertEquals(Token.GETELEM, elem.getType());

    Node assign = IR.assign(prop, IR.number(10));
    assertEquals(Token.ASSIGN, assign.getType());

    Node hook = IR.hook(IR.trueNode(), IR.number(1), IR.number(2));
    assertEquals(Token.HOOK, hook.getType());
  }

  // Tests binary, unary, and bitwise operators
  @Test
  public void testOperators_validExpressions_createsOpNodes() {
    assertEquals(Token.ADD, IR.add(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.SUB, IR.sub(IR.number(3), IR.number(4)).getType());
    assertEquals(Token.MUL, IR.mul(IR.number(2), IR.number(3)).getType());
    assertEquals(Token.DIV, IR.div(IR.number(6), IR.number(2)).getType());
    assertEquals(Token.MOD, IR.mod(IR.number(5), IR.number(2)).getType());

    assertEquals(Token.AND, IR.and(IR.trueNode(), IR.falseNode()).getType());
    assertEquals(Token.OR, IR.or(IR.trueNode(), IR.falseNode()).getType());

    assertEquals(Token.EQ, IR.eq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.NE, IR.ne(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.SHEQ, IR.sheq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.SHNE, IR.shne(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.LT, IR.lt(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.LE, IR.le(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.GT, IR.gt(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.GE, IR.ge(IR.name("a"), IR.name("b")).getType());

    assertEquals(Token.BITOR, IR.bitOr(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.BITXOR, IR.bitXor(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.BITAND, IR.bitAnd(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.LSH, IR.lsh(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.RSH, IR.rsh(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.URSH, IR.ursh(IR.number(1), IR.number(2)).getType());

    assertEquals(Token.NOT, IR.not(IR.trueNode()).getType());
    assertEquals(Token.BITNOT, IR.bitNot(IR.number(1)).getType());
    assertEquals(Token.NEG, IR.neg(IR.number(5)).getType());
    assertEquals(Token.POS, IR.pos(IR.number(5)).getType());
    assertEquals(Token.VOID, IR.voidNode(IR.number(0)).getType());
    assertEquals(Token.COMMA, IR.comma(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.THROW, IR.throwNode(IR.string("err")).getType());
  }

  // Tests object and array literals, regexp, and primitives
  @Test
  public void testLiterals_validInputs_createsLiteralNodes() {
    Node key = IR.stringKey("key");
    key.addChildToFront(IR.number(42));
    Node objLit = IR.objectlit(key);
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(1, objLit.getChildCount());

    Node arrLit = IR.arraylit(IR.number(1), IR.empty(), IR.string("x"));
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(3, arrLit.getChildCount());

    Node regex1 = IR.regexp(IR.string("abc"));
    assertEquals(Token.REGEXP, regex1.getType());
    assertEquals(1, regex1.getChildCount());

    Node regex2 = IR.regexp(IR.string("abc"), IR.string("g"));
    assertEquals(Token.REGEXP, regex2.getType());
    assertEquals(2, regex2.getChildCount());

    assertEquals(Token.THIS, IR.thisNode().getType());
    assertEquals(Token.NULL, IR.nullNode().getType());
    assertEquals(Token.EMPTY, IR.empty().getType());
  }
}
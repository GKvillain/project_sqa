package com.google.javascript.rhino;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class IRTest {

  // Tests tryFinally helper with block nodes (Defects4J Closure-27)
  @Test
  public void testTryFinally_withBlockNodes_returnsTryNode() {
    Node tryBody = IR.block();
    Node finallyBody = IR.block();
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertNotNull(tryNode);
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertEquals(Token.BLOCK, tryNode.getFirstChild().getNext().getType());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  // Tests tryCatch and catchNode construction
  @Test
  public void testTryCatch_validNodes_returnsTryNode() {
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
  public void testTryCatchFinally_validNodes_returnsTryWithThreeChildren() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node finallyBody = IR.block();
    Node tryNode = IR.tryCatchFinally(tryBody, catchNode, finallyBody);

    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  // Tests function and paramList variations
  @Test
  public void testFunction_validComponents_returnsFunctionNode() {
    Node name = IR.name("foo");
    Node params = IR.paramList(IR.name("a"), IR.name("b"));
    Node body = IR.block();

    Node func = IR.function(name, params, body);
    assertEquals(Token.FUNCTION, func.getType());
    assertEquals(3, func.getChildCount());
    assertSame(name, func.getFirstChild());
    assertSame(params, func.getFirstChild().getNext());
    assertSame(body, func.getLastChild());

    List<Node> paramList = Arrays.asList(IR.name("x"));
    Node paramListNode = IR.paramList(paramList);
    assertEquals(Token.PARAM_LIST, paramListNode.getType());
    assertEquals(1, paramListNode.getChildCount());
  }

  // Tests function with non-block body throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testFunction_invalidBody_throwsException() {
    IR.function(IR.name("f"), IR.paramList(), IR.name("notABlock"));
  }

  // Tests block and script construction
  @Test
  public void testBlockAndScript_statements_createsNodesWithChildren() {
    Node stmt1 = IR.var(IR.name("x"), IR.number(1));
    Node stmt2 = IR.returnNode(IR.name("x"));

    Node blockNode = IR.block(stmt1, stmt2);
    assertEquals(Token.BLOCK, blockNode.getType());
    assertEquals(2, blockNode.getChildCount());

    Node scriptNode = IR.script(stmt1, stmt2);
    assertEquals(Token.SCRIPT, scriptNode.getType());
    assertEquals(2, scriptNode.getChildCount());
  }

  // Tests var without and with initial value
  @Test
  public void testVar_withAndWithoutValue_returnsVarNode() {
    Node varWithoutVal = IR.var(IR.name("a"));
    assertEquals(Token.VAR, varWithoutVal.getType());
    assertEquals(1, varWithoutVal.getChildCount());

    Node varWithVal = IR.var(IR.name("b"), IR.string("hello"));
    assertEquals(Token.VAR, varWithVal.getType());
    Node nameChild = varWithVal.getFirstChild();
    assertEquals("b", nameChild.getString());
    assertTrue(nameChild.hasChildren());
    assertEquals("hello", nameChild.getFirstChild().getString());
  }

  // Tests ifNode with and without else clause
  @Test
  public void testIfNode_withAndWithoutElse_returnsIfNode() {
    Node cond = IR.trueNode();
    Node thenBlock = IR.block();
    Node elseBlock = IR.block();

    Node ifSimple = IR.ifNode(cond, thenBlock);
    assertEquals(Token.IF, ifSimple.getType());
    assertEquals(2, ifSimple.getChildCount());

    Node ifElse = IR.ifNode(cond, thenBlock, elseBlock);
    assertEquals(Token.IF, ifElse.getType());
    assertEquals(3, ifElse.getChildCount());
  }

  // Tests do-while, while, and for loops
  @Test
  public void testLoops_validParameters_returnsLoopNodes() {
    Node doNode = IR.doNode(IR.block(), IR.falseNode());
    assertEquals(Token.DO, doNode.getType());

    Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
    assertEquals(Token.WHILE, whileNode.getType());

    Node forIn = IR.forIn(IR.name("x"), IR.name("obj"), IR.block());
    assertEquals(Token.FOR, forIn.getType());

    Node forNode = IR.forNode(IR.var(IR.name("i"), IR.number(0)),
                              IR.eq(IR.name("i"), IR.number(10)),
                              IR.assign(IR.name("i"), IR.number(1)),
                              IR.block());
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(4, forNode.getChildCount());
  }

  // Tests switch, caseNode, and defaultCase
  @Test
  public void testSwitch_withCases_returnsSwitchNode() {
    Node case1 = IR.caseNode(IR.number(1), IR.block());
    Node defCase = IR.defaultCase(IR.block());
    Node switchNode = IR.switchNode(IR.name("x"), case1, defCase);

    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(3, switchNode.getChildCount());
    assertTrue(case1.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
    assertTrue(defCase.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
  }

  // Tests label, labelName, breakNode, and continueNode
  @Test
  public void testLabelAndControlFlow_validNames_returnsCorrectNodes() {
    Node lblName = IR.labelName("myLabel");
    assertEquals(Token.LABEL_NAME, lblName.getType());
    assertEquals("myLabel", lblName.getString());

    Node lbl = IR.label(lblName, IR.block());
    assertEquals(Token.LABEL, lbl.getType());

    Node brkNamed = IR.breakNode(IR.labelName("myLabel"));
    assertEquals(Token.BREAK, brkNamed.getType());
    assertEquals(1, brkNamed.getChildCount());

    Node brk = IR.breakNode();
    assertEquals(Token.BREAK, brk.getType());
    assertEquals(0, brk.getChildCount());

    Node contNamed = IR.continueNode(IR.labelName("myLabel"));
    assertEquals(Token.CONTINUE, contNamed.getType());

    Node cont = IR.continueNode();
    assertEquals(Token.CONTINUE, cont.getType());
  }

  // Tests call, newNode, and property access expressions
  @Test
  public void testCallsAndAccessors_validExpressions_returnsCorrectNodes() {
    Node target = IR.name("fn");
    Node callNode = IR.call(target, IR.number(1), IR.string("a"));
    assertEquals(Token.CALL, callNode.getType());
    assertEquals(3, callNode.getChildCount());

    Node newCall = IR.newNode(target, IR.nullNode());
    assertEquals(Token.NEW, newCall.getType());
    assertEquals(2, newCall.getChildCount());

    Node getProp = IR.getprop(IR.name("obj"), IR.string("prop"));
    assertEquals(Token.GETPROP, getProp.getType());

    Node getElem = IR.getelem(IR.name("arr"), IR.number(0));
    assertEquals(Token.GETELEM, getElem.getType());

    Node assignNode = IR.assign(IR.name("x"), IR.number(5));
    assertEquals(Token.ASSIGN, assignNode.getType());
  }

  // Tests binary, unary, and ternary operators
  @Test
  public void testOperators_validOperands_returnsOpNodes() {
    Node hookNode = IR.hook(IR.name("c"), IR.number(1), IR.number(2));
    assertEquals(Token.HOOK, hookNode.getType());

    assertEquals(Token.COMMA, IR.comma(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.AND, IR.and(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.OR, IR.or(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.EQ, IR.eq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.SHEQ, IR.sheq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.ADD, IR.add(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.SUB, IR.sub(IR.name("a"), IR.name("b")).getType());

    assertEquals(Token.NOT, IR.not(IR.name("a")).getType());
    assertEquals(Token.VOID, IR.voidNode(IR.name("a")).getType());
    assertEquals(Token.NEG, IR.neg(IR.name("a")).getType());
    assertEquals(Token.POS, IR.pos(IR.name("a")).getType());
  }

  // Tests literal constructors
  @Test
  public void testLiterals_validInputs_returnsLiteralNodes() {
    assertEquals(Token.EMPTY, IR.empty().getType());
    assertEquals(Token.THIS, IR.thisNode().getType());
    assertEquals(Token.TRUE, IR.trueNode().getType());
    assertEquals(Token.FALSE, IR.falseNode().getType());
    assertEquals(Token.NULL, IR.nullNode().getType());

    Node str = IR.string("val");
    assertEquals(Token.STRING, str.getType());
    assertEquals("val", str.getString());

    Node num = IR.number(3.14);
    assertEquals(Token.NUMBER, num.getType());
    assertEquals(3.14, num.getDouble(), 0.0001);

    Node strKey = IR.stringKey("k");
    assertEquals(Token.STRING_KEY, strKey.getType());

    Node propDef = IR.propdef(strKey, IR.number(10));
    assertEquals(Token.STRING_KEY, propDef.getType());
    assertTrue(propDef.hasChildren());

    Node objLit = IR.objectlit(propDef);
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(1, objLit.getChildCount());

    Node arrLit = IR.arraylit(IR.number(1), IR.empty());
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(2, arrLit.getChildCount());

    Node regex1 = IR.regexp(IR.string("abc"));
    assertEquals(Token.REGEXP, regex1.getType());
    assertEquals(1, regex1.getChildCount());

    Node regex2 = IR.regexp(IR.string("abc"), IR.string("g"));
    assertEquals(Token.REGEXP, regex2.getType());
    assertEquals(2, regex2.getChildCount());
  }

  // Tests assign with invalid assignment target throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testAssign_invalidTarget_throwsException() {
    IR.assign(IR.number(123), IR.number(456));
  }

  // Tests returnNode, throwNode, and exprResult
  @Test
  public void testStatements_validExpressions_returnsStatementNodes() {
    Node retEmpty = IR.returnNode();
    assertEquals(Token.RETURN, retEmpty.getType());

    Node retExpr = IR.returnNode(IR.number(0));
    assertEquals(Token.RETURN, retExpr.getType());
    assertEquals(1, retExpr.getChildCount());

    Node thrw = IR.throwNode(IR.string("err"));
    assertEquals(Token.THROW, thrw.getType());

    Node exprRes = IR.exprResult(IR.assign(IR.name("x"), IR.number(1)));
    assertEquals(Token.EXPR_RESULT, exprRes.getType());
  }
}
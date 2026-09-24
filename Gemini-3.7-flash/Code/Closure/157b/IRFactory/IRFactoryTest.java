package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IRFactoryTest {

  private List<String> errors;
  private List<String> warnings;
  private ErrorReporter errorReporter;

  @Before
  public void setUp() {
    errors = new ArrayList<String>();
    warnings = new ArrayList<String>();
    errorReporter = new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        warnings.add(message);
      }

      @Override
      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        errors.add(message);
      }

      @Override
      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
      }
    };
  }

  private Node parse(String source, LanguageMode mode, boolean acceptConst) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setWarnTrailingComma(true);
    if (mode == LanguageMode.ECMASCRIPT3) {
      env.setLanguageVersion(com.google.javascript.jscomp.mozilla.rhino.Context.VERSION_1_5);
    } else {
      env.setLanguageVersion(com.google.javascript.jscomp.mozilla.rhino.Context.VERSION_1_8);
    }
    Parser parser = new Parser(env, errorReporter);
    AstRoot astRoot = parser.parse(source, "testcode", 1);
    Set<String> emptySet = Collections.emptySet();
    Config config = new Config(emptySet, emptySet, true, mode, acceptConst);
    return IRFactory.transformTree(astRoot, source, config, errorReporter);
  }

  private Node parse(String source) {
    return parse(source, LanguageMode.ECMASCRIPT5, false);
  }

  // Tests transformation of basic variable declaration and assignment
  @Test
  public void testTransformTree_varDeclaration_createsVarNode() {
    Node script = parse("var x = 10;");
    assertEquals(Token.SCRIPT, script.getType());
    Node varNode = script.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals("x", nameNode.getString());
    Node numNode = nameNode.getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(10.0, numNode.getDouble(), 0.0);
  }

  // Tests object literal properties with quoted and unquoted keys
  @Test
  public void testTransformTree_objectLiteralKeys_marksQuotedCorrectly() {
    Node script = parse("var obj = {a: 1, 'b': 2, 3: 4};");
    Node varNode = script.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());

    Node keyA = objLit.getFirstChild();
    assertEquals(Token.STRING, keyA.getType());
    assertEquals("a", keyA.getString());
    assertFalse(keyA.getBooleanProp(Node.QUOTED_PROP));

    Node keyB = keyA.getNext();
    assertEquals(Token.STRING, keyB.getType());
    assertEquals("b", keyB.getString());
    assertTrue(keyB.getBooleanProp(Node.QUOTED_PROP));

    Node keyC = keyB.getNext();
    assertEquals(Token.NUMBER, keyC.getType());
    assertEquals(3.0, keyC.getDouble(), 0.0);
  }

  // Tests ES5 getter and setter in object literal
  @Test
  public void testTransformTree_getterSetter_createsGetAndSetNodes() {
    Node script = parse("var o = { get x() { return 1; }, set x(v) { this._x = v; } };", LanguageMode.ECMASCRIPT5, false);
    Node objLit = script.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());

    Node getProp = objLit.getFirstChild();
    assertEquals(Token.GET, getProp.getType());
    assertEquals("x", getProp.getString());
    Node fnGet = getProp.getFirstChild();
    assertEquals(Token.FUNCTION, fnGet.getType());

    Node setProp = getProp.getNext();
    assertEquals(Token.SET, setProp.getType());
    assertEquals("x", setProp.getString());
    Node fnSet = setProp.getFirstChild();
    assertEquals(Token.FUNCTION, fnSet.getType());
    assertTrue(errors.isEmpty());
  }

  // Tests ES3 mode reports error on getter and setter
  @Test
  public void testTransformTree_getterSetterInES3_reportsError() {
    parse("var o = { get x() { return 1; } };", LanguageMode.ECMASCRIPT3, false);
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("getters are not supported in Internet Explorer"));
  }

  // Tests unary negation of number literal collapses value
  @Test
  public void testTransformTree_unaryNegationOnNumber_invertsNumber() {
    Node script = parse("var x = -5;");
    Node nameNode = script.getFirstChild().getFirstChild();
    Node numNode = nameNode.getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(-5.0, numNode.getDouble(), 0.0);
  }

  // Tests increment and decrement on invalid target reports error
  @Test
  public void testTransformTree_invalidIncTarget_reportsError() {
    parse("1++;");
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("invalid increment target"));
  }

  // Tests invalid assignment target reports error
  @Test
  public void testTransformTree_invalidAssignTarget_reportsError() {
    parse("1 = 2;");
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("invalid assignment target"));
  }

  // Tests function declarations named and unnamed
  @Test
  public void testTransformTree_functionDeclaration_setsProperties() {
    Node script = parse("function foo(a, b) { return a + b; }");
    Node fnNode = script.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node nameNode = fnNode.getFirstChild();
    assertEquals("foo", nameNode.getString());
    Node lpNode = nameNode.getNext();
    assertEquals(Token.LP, lpNode.getType());
    assertEquals(2, lpNode.getChildCount());
    Node bodyNode = lpNode.getNext();
    assertEquals(Token.BLOCK, bodyNode.getType());
  }

  // Tests function expression inside parenthesized expression
  @Test
  public void testTransformTree_parenthesizedExpression_setsParenProp() {
    Node script = parse("(1 + 2);");
    Node exprResult = script.getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprResult.getType());
    Node addNode = exprResult.getFirstChild();
    assertEquals(Token.ADD, addNode.getType());
    assertTrue(addNode.getBooleanProp(Node.PARENTHESIZED_PROP));
  }

  // Tests directives parsing in script and function bodies
  @Test
  public void testTransformTree_directives_attachesToNode() {
    Node script = parse("'use strict'; var x = 1;");
    Set<String> directives = script.getDirectives();
    assertNotNull(directives);
    assertTrue(directives.contains("use strict"));
    assertEquals(Token.VAR, script.getFirstChild().getType());
  }

  // Tests labeled statements and break with label
  @Test
  public void testTransformTree_labeledStatementAndBreak_transformsCorrectly() {
    Node script = parse("loop: while(true) { break loop; }");
    Node labelNode = script.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("loop", labelName.getString());

    Node whileNode = labelName.getNext();
    assertEquals(Token.WHILE, whileNode.getType());
    Node blockNode = whileNode.getLastChild();
    Node breakNode = blockNode.getFirstChild();
    assertEquals(Token.BREAK, breakNode.getType());
    assertEquals("loop", breakNode.getFirstChild().getString());
    assertEquals(Token.LABEL_NAME, breakNode.getFirstChild().getType());
  }

  // Tests switch statement with cases and default clause
  @Test
  public void testTransformTree_switchStatement_createsCaseAndDefaultBlocks() {
    Node script = parse("switch (x) { case 1: break; default: break; }");
    Node switchNode = script.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    Node caseNode = switchNode.getFirstChild().getNext();
    assertEquals(Token.CASE, caseNode.getType());
    Node defaultNode = caseNode.getNext();
    assertEquals(Token.DEFAULT, defaultNode.getType());
  }

  // Tests try catch finally statement
  @Test
  public void testTransformTree_tryCatchFinally_createsTryStructure() {
    Node script = parse("try { throw 'err'; } catch (e) { } finally { }");
    Node tryNode = script.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    Node tryBlock = tryNode.getFirstChild();
    assertEquals(Token.BLOCK, tryBlock.getType());
    Node catchBlock = tryBlock.getNext();
    assertEquals(Token.BLOCK, catchBlock.getType());
    Node catchNode = catchBlock.getFirstChild();
    assertEquals(Token.CATCH, catchNode.getType());
    Node finallyBlock = catchBlock.getNext();
    assertEquals(Token.BLOCK, finallyBlock.getType());
  }

  // Tests for-in loop and standard for loop
  @Test
  public void testTransformTree_loops_createsForAndDoWhileNodes() {
    Node scriptFor = parse("for (var k in obj) {}");
    assertEquals(Token.FOR, scriptFor.getFirstChild().getType());

    Node scriptDo = parse("do {} while (false);");
    assertEquals(Token.DO, scriptDo.getFirstChild().getType());

    Node scriptForLoop = parse("for (var i = 0; i < 10; i++) {}");
    assertEquals(Token.FOR, scriptForLoop.getFirstChild().getType());
  }

  // Tests regex literal with flags
  @Test
  public void testTransformTree_regExpLiteral_createsRegExpNode() {
    Node script = parse("var re = /abc/gi;");
    Node regExpNode = script.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, regExpNode.getType());
    assertEquals("abc", regExpNode.getFirstChild().getString());
    assertEquals("gi", regExpNode.getLastChild().getString());
  }

  // Tests JSDoc fileoverview and license handling
  @Test
  public void testTransformTree_jsdocFileOverviewAndComments_attachesJSDocInfo() {
    String source = "/** @fileoverview Test file\n * @license Apache 2.0 */\nvar a = 1;";
    Node script = parse(source);
    JSDocInfo info = script.getJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getLicense());
    assertTrue(info.getLicense().contains("Apache 2.0"));
  }

  // Tests ES5 reserved keywords detection
  @Test
  public void testTransformTree_es5ReservedKeyword_reportsErrorInES5() {
    parse("var implements = 1;", LanguageMode.ECMASCRIPT5_STRICT, false);
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("identifier is a reserved word"));
  }

  // Tests const keyword acceptance based on config
  @Test
  public void testTransformTree_constKeyword_handledByConfig() {
    parse("const x = 1;", LanguageMode.ECMASCRIPT5, false);
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("Unsupported syntax: const"));
  }

  // Tests if-else statement and conditional hook expression
  @Test
  public void testTransformTree_ifStatementAndHook_createsCorrectNodes() {
    Node script = parse("if (a) { b(); } else { c(); } var d = a ? b : c;");
    Node ifNode = script.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(3, ifNode.getChildCount());

    Node varNode = ifNode.getNext();
    Node hookNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
  }
}
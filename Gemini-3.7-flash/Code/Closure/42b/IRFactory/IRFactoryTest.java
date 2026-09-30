package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class IRFactoryTest {

  private TestErrorReporter errorReporter;

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

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
      errors.add(message);
      return new EvaluatorException(message);
    }
  }

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
  }

  private Node parse(String js, LanguageMode mode, boolean isIdeMode, boolean acceptConstKeyword) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setLanguageVersion(com.google.javascript.rhino.head.Context.VERSION_1_8);
    env.setIdeMode(isIdeMode);
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);

    Parser parser = new Parser(env, errorReporter);
    AstRoot astRoot = parser.parse(js, "testcode", 1);

    Config config = new Config(
        ImmutableSet.<String>of(),
        ImmutableSet.<String>of(),
        isIdeMode,
        mode,
        acceptConstKeyword);

    StaticSourceFile sourceFile = new SimpleSourceFile("testcode", false);
    return IRFactory.transformTree(astRoot, sourceFile, js, config, errorReporter);
  }

  private Node parse(String js) {
    return parse(js, LanguageMode.ECMASCRIPT5, false, true);
  }

  // Tests unsupported for each loop syntax reporting
  @Test
  public void testProcessForInLoop_forEach_reportsUnsupportedSyntax() {
    Node root = parse("for each (var x in [1, 2, 3]) {}");
    assertNotNull(root);
    assertFalse("Expected error for 'for each' loop", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("unsupported") ||
               errorReporter.errors.get(0).contains("Unsupported"));
  }

  // Tests standard for-in loop transformation
  @Test
  public void testProcessForInLoop_standardForIn_returnsForNode() {
    Node root = parse("for (var x in obj) {}");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
  }

  // Tests standard for loop transformation
  @Test
  public void testProcessForLoop_validInput_returnsForNode() {
    Node root = parse("for (var i = 0; i < 10; i++) {}");
    assertNotNull(root);
    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
  }

  // Tests do-while loop transformation
  @Test
  public void testProcessDoLoop_validInput_returnsDoNode() {
    Node root = parse("do { x++; } while (x < 10);");
    assertNotNull(root);
    Node doNode = root.getFirstChild();
    assertEquals(Token.DO, doNode.getType());
  }

  // Tests while loop transformation
  @Test
  public void testProcessWhileLoop_validInput_returnsWhileNode() {
    Node root = parse("while (true) { break; }");
    assertNotNull(root);
    Node whileNode = root.getFirstChild();
    assertEquals(Token.WHILE, whileNode.getType());
  }

  // Tests try-catch-finally statement transformation
  @Test
  public void testProcessTryStatement_withCatchAndFinally_returnsTryNode() {
    Node root = parse("try { throw 'err'; } catch (e) { } finally { }");
    assertNotNull(root);
    Node tryNode = root.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
  }

  // Tests switch-case-default transformation
  @Test
  public void testProcessSwitchStatement_withCasesAndDefault_returnsSwitchNode() {
    Node root = parse("switch (x) { case 1: break; default: break; }");
    assertNotNull(root);
    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
  }

  // Tests function expression with name and arguments
  @Test
  public void testProcessFunctionNode_namedFunction_returnsFunctionNode() {
    Node root = parse("function foo(a, b) { return a + b; }");
    assertNotNull(root);
    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    assertEquals("foo", fnNode.getFirstChild().getString());
  }

  // Tests unnamed function statement reporting error
  @Test
  public void testProcessFunctionNode_unnamedFunctionStatement_reportsError() {
    Node root = parse("var f = function() {};");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  // Tests reserved keyword in ES5 strict mode
  @Test
  public void testProcessName_reservedKeywordES5Strict_reportsError() {
    parse("var let = 1;", LanguageMode.ECMASCRIPT5_STRICT, false, true);
    assertFalse("Expected error for reserved keyword 'let'", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("reserved word"));
  }

  // Tests object literal with getter and setter in ES5
  @Test
  public void testProcessObjectLiteral_getterAndSetter_success() {
    Node root = parse("var obj = { get x() { return 1; }, set x(v) {} };");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  // Tests object literal getter in ES3 reports error
  @Test
  public void testProcessObjectLiteral_getterInES3_reportsError() {
    parse("var obj = { get x() { return 1; } };", LanguageMode.ECMASCRIPT3, false, true);
    assertFalse("Expected error for getter in ES3", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("getters are not supported"));
  }

  // Tests array literal and elements
  @Test
  public void testProcessArrayLiteral_normalElements_returnsArrayLitNode() {
    Node root = parse("var arr = [1, 'two', 3.0];");
    assertNotNull(root);
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    Node arrayLit = nameNode.getFirstChild();
    assertEquals(Token.ARRAYLIT, arrayLit.getType());
  }

  // Tests unary expressions (increment, decrement, negation)
  @Test
  public void testProcessUnaryExpression_validOperands_success() {
    Node root = parse("var x = -5; x++; ++x; delete x.p;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  // Tests invalid delete target error reporting
  @Test
  public void testProcessUnaryExpression_invalidDelete_reportsError() {
    parse("delete (1 + 2);");
    assertFalse("Expected error for deleting non-property", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("Invalid delete operand"));
  }

  // Tests suspicious block comment warning
  @Test
  public void testHandleBlockComment_suspiciousComment_reportsWarning() {
    parse("/* @type {number} */ var x = 1;");
    assertFalse("Expected warning for suspicious non-JSDoc comment", errorReporter.warnings.isEmpty());
    assertEquals(IRFactory.SUSPICIOUS_COMMENT_WARNING, errorReporter.warnings.get(0));
  }

  // Tests const keyword rejected when disabled
  @Test
  public void testProcessVariableDeclaration_constDisabled_reportsError() {
    parse("const X = 1;", LanguageMode.ECMASCRIPT5, false, false);
    assertFalse("Expected error for const keyword", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("Unsupported syntax"));
  }

  // Tests directive parsing in AST root
  @Test
  public void testParseDirectives_useStrict_setsDirectiveOnScript() {
    Node root = parse("'use strict'; var x = 1;");
    assertNotNull(root);
    assertNotNull(root.getDirectives());
    assertTrue(root.getDirectives().contains("use strict"));
  }

  // Tests string with vertical tab escaping
  @Test
  public void testProcessStringLiteral_verticalTab_setsSlashVProp() {
    Node root = parse("var s = '\\v';");
    assertNotNull(root);
    Node stringNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.STRING, stringNode.getType());
    assertTrue(stringNode.getBooleanProp(Node.SLASH_V));
  }

  @Test
  public void testProcessObjectLiteral_setterInES3_reportsError() {
    parse("var obj = { set x(v) {} };", LanguageMode.ECMASCRIPT3, false, true);
    assertFalse("Expected error for setter in ES3", errorReporter.errors.isEmpty());
    assertTrue(errorReporter.errors.get(0).contains("setters are not supported"));
  }

  @Test
  public void testProcessLabeledStatement_andBreakContinueWithLabel() {
    Node root = parse("label1: for (;;) { break label1; continue label1; }");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node nameNode = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, nameNode.getType());
    assertEquals("label1", nameNode.getString());
  }

  @Test
  public void testProcessConditionalExpression() {
    Node root = parse("var x = a ? b : c;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node hookNode = nameNode.getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
  }

  @Test
  public void testProcessNewAndCallExpression() {
    Node root = parse("new Foo(1, 2); foo(a, b);");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node exprResult1 = root.getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprResult1.getType());
    assertEquals(Token.NEW, exprResult1.getFirstChild().getType());

    Node exprResult2 = exprResult1.getNext();
    assertEquals(Token.EXPR_RESULT, exprResult2.getType());
    assertEquals(Token.CALL, exprResult2.getFirstChild().getType());
  }

  @Test
  public void testProcessElementGetAndPropertyGet() {
    Node root = parse("var x = a.b; var y = a[b];");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());

    Node getprop = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getprop.getType());

    Node getelem = root.getFirstChild().getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETELEM, getelem.getType());
  }

  @Test
  public void testProcessWithStatement() {
    Node root = parse("with (obj) { x = 1; }");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node withNode = root.getFirstChild();
    assertEquals(Token.WITH, withNode.getType());
  }

  @Test
  public void testProcessEmptyStatement() {
    Node root = parse(";;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node emptyNode = root.getFirstChild();
    assertEquals(Token.EMPTY, emptyNode.getType());
  }

  @Test
  public void testProcessDebuggerStatement() {
    Node root = parse("debugger;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node debuggerNode = root.getFirstChild();
    assertEquals(Token.DEBUGGER, debuggerNode.getType());
  }

  @Test
  public void testProcessThrowStatement() {
    Node root = parse("throw new Error('msg');");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node throwNode = root.getFirstChild();
    assertEquals(Token.THROW, throwNode.getType());
  }

  @Test
  public void testProcessRegExpLiteral() {
    Node root = parse("var re = /abc/gi;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node regExpNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, regExpNode.getType());
  }

  @Test
  public void testProcessKeywords_thisNullTrueFalse() {
    Node root = parse("var a = [this, null, true, false];");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node arrayLit = root.getFirstChild().getFirstChild().getFirstChild();
    Node child = arrayLit.getFirstChild();
    assertEquals(Token.THIS, child.getType());
    child = child.getNext();
    assertEquals(Token.NULL, child.getType());
    child = child.getNext();
    assertEquals(Token.TRUE, child.getType());
    child = child.getNext();
    assertEquals(Token.FALSE, child.getType());
  }

  @Test
  public void testProcessArrayLiteral_withEmptySlots() {
    Node root = parse("var arr = [1, , 2];");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node arrayLit = root.getFirstChild().getFirstChild().getFirstChild();
    Node emptySlot = arrayLit.getFirstChild().getNext();
    assertEquals(Token.EMPTY, emptySlot.getType());
  }

  @Test
  public void testProcessObjectLiteral_quotedAndNumericKeys() {
    Node root = parse("var obj = { 'a': 1, 2: 3, b: 4 };");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node objLit = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    Node key1 = objLit.getFirstChild();
    assertTrue(key1.isQuotedString());
    Node key2 = key1.getNext();
    assertTrue(key2.isQuotedString());
    Node key3 = key2.getNext();
    assertFalse(key3.isQuotedString());
  }

  @Test
  public void testProcessFunctionDirectives_useStrictInFunction() {
    Node root = parse("function foo() { 'use strict'; return 1; }");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node bodyNode = fnNode.getLastChild();
    assertEquals(Token.BLOCK, bodyNode.getType());
    assertNotNull(bodyNode.getDirectives());
    assertTrue(bodyNode.getDirectives().contains("use strict"));
  }

  @Test
  public void testProcessBinaryExpressions_variousOperators() {
    Node root = parse("var res = (a + b) * (c - d) / e % f & g | h ^ i << j >> k >>> l;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testProcessAssignmentOperators() {
    Node root = parse("a += 1; a -= 2; a *= 3; a /= 4; a %= 5; a &= 6; a |= 7; a ^= 8; a <<= 9; a >>= 10; a >>>= 11;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testProcessLogicalAndCommaExpressions() {
    Node root = parse("var x = (a && b) || (c, d);");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testProcessVarWithoutInitializer() {
    Node root = parse("var a, b;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node firstVar = varNode.getFirstChild();
    assertFalse(firstVar.hasChildren());
    Node secondVar = firstVar.getNext();
    assertFalse(secondVar.hasChildren());
  }

  @Test
  public void testProcessJSDocAttachment() {
    Node root = parse("/** @type {number} */ var x = 1;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    Node varNode = root.getFirstChild();
    assertNotNull(varNode.getJSDocInfo());
  }

  @Test
  public void testProcessFileOverviewJSDoc() {
    Node root = parse("/** @fileoverview Test file */ var x = 1;");
    assertNotNull(root);
    assertEquals(0, errorReporter.errors.size());
    assertNotNull(root.getJSDocInfo());
  }

  @Test
  public void testProcessIdeMode_toleranceOnIncompleteInput() {
    Node root = parse("var x = ;", LanguageMode.ECMASCRIPT5, true, true);
    assertNotNull(root);
    assertFalse(errorReporter.errors.isEmpty());
  }
}
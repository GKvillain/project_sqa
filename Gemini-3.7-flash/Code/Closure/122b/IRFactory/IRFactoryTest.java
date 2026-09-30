package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.Context;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IRFactoryTest {

  private TestErrorReporter errorReporter;
  private Config es5Config;
  private Config es3Config;

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
    es5Config = new Config(
        ImmutableSet.<String>of(),
        ImmutableSet.<String>of(),
        false,
        LanguageMode.ECMASCRIPT5,
        false);
    es3Config = new Config(
        ImmutableSet.<String>of(),
        ImmutableSet.<String>of(),
        false,
        LanguageMode.ECMASCRIPT3,
        false);
  }

  private AstRoot parseRhinoAst(String source) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(Context.VERSION_1_8);
    Parser p = new Parser(env, errorReporter);
    return p.parse(source, "test.js", 1);
  }

  private Node transform(String source, Config config) {
    AstRoot root = parseRhinoAst(source);
    return IRFactory.transformTree(root, null, source, config, errorReporter);
  }

  // Tests suspicious comment warning on single-line block comment with annotation
  @Test
  public void testSuspiciousComment_singleLineBlockComment_reportsWarning() {
    String source = "/* @type {number} */ var x = 1;";
    transform(source, es5Config);
    assertTrue(errorReporter.hasWarning(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests suspicious comment warning on multi-line block comment with annotation
  @Test
  public void testSuspiciousComment_multilineBlockComment_reportsWarning() {
    String source = "/*\n * @type {string}\n */\nvar str = 'hello';";
    transform(source, es5Config);
    assertTrue(errorReporter.hasWarning(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests that normal block comment without annotation does not report warning
  @Test
  public void testNormalBlockComment_noAnnotation_noWarning() {
    String source = "/* This is a normal block comment */ var x = 1;";
    transform(source, es5Config);
    assertFalse(errorReporter.hasWarning(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests that valid JSDoc comment does not trigger suspicious comment warning
  @Test
  public void testJsDocComment_validJSDoc_noSuspiciousCommentWarning() {
    String source = "/** @type {number} */ var x = 1;";
    Node root = transform(source, es5Config);
    assertNotNull(root);
    assertFalse(errorReporter.hasWarning(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests parsing directive 'use strict' attached to script node
  @Test
  public void testParseDirectives_useStrict_setsDirectiveOnNode() {
    String source = "'use strict'; var x = 1;";
    Node root = transform(source, es5Config);
    assertNotNull(root.getDirectives());
    assertTrue(root.getDirectives().contains("use strict"));
  }

  // Tests ES5 getter and setter in object literal
  @Test
  public void testObjectLiteral_getterAndSetter_transformedCorrectly() {
    String source = "var obj = { get foo() { return 1; }, set foo(val) { this.x = val; } };";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node objLit = nameNode.getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(Token.GETTER_DEF, objLit.getFirstChild().getType());
    assertEquals(Token.SETTER_DEF, objLit.getLastChild().getType());
  }

  // Tests error reporting for getters in ES3 mode
  @Test
  public void testObjectLiteral_getterInES3_reportsError() {
    String source = "var obj = { get foo() { return 1; } };";
    transform(source, es3Config);
    assertTrue(errorReporter.hasError(IRFactory.GETTER_ERROR_MESSAGE));
  }

  // Tests error reporting for setters in ES3 mode
  @Test
  public void testObjectLiteral_setterInES3_reportsError() {
    String source = "var obj = { set foo(val) { this.x = val; } };";
    transform(source, es3Config);
    assertTrue(errorReporter.hasError(IRFactory.SETTER_ERROR_MESSAGE));
  }

  // Tests error reporting when getter has parameters
  @Test
  public void testObjectLiteral_getterWithParam_reportsError() {
    String source = "var obj = { get foo(x) { return x; } };";
    transform(source, es5Config);
    assertTrue(errorReporter.hasError("getters may not have parameters"));
  }

  // Tests error reporting when setter does not have exactly one parameter
  @Test
  public void testObjectLiteral_setterWithoutParam_reportsError() {
    String source = "var obj = { set foo() {} };";
    transform(source, es5Config);
    assertTrue(errorReporter.hasError("setters must have exactly one parameter"));
  }

  // Tests invalid delete operand error
  @Test
  public void testUnaryExpression_invalidDelete_reportsError() {
    String source = "delete 10;";
    transform(source, es5Config);
    assertTrue(errorReporter.hasError("Invalid delete operand. Only properties can be deleted."));
  }

  // Tests invalid increment target error
  @Test
  public void testUnaryExpression_invalidIncrement_reportsError() {
    String source = "++10;";
    transform(source, es5Config);
    assertTrue(errorReporter.hasError("invalid increment target"));
  }

  // Tests unary negation folding on number literal
  @Test
  public void testUnaryExpression_negation_invertsNumber() {
    String source = "var x = -5;";
    Node root = transform(source, es5Config);
    Node numberNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, numberNode.getType());
    assertEquals(-5.0, numberNode.getDouble(), 0.0);
  }

  // Tests control structures: if, while, do-while, and for loops
  @Test
  public void testControlStructures_loopsAndIf_transformsCorrectly() {
    String source = "if (true) { while (false) { do { for (var i = 0; i < 1; i++) {} } while (false); } }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
  }

  // Tests try-catch-finally statement transformation
  @Test
  public void testTryCatchFinally_transformsCorrectly() {
    String source = "try { var x = 1; } catch (e) { var y = 2; } finally { var z = 3; }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node tryNode = root.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
  }

  // Tests switch-case-default transformation
  @Test
  public void testSwitchStatement_transformsCorrectly() {
    String source = "switch (x) { case 1: break; default: break; }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
  }

  // Tests string literal containing vertical tab character
  @Test
  public void testStringLiteral_verticalTab_setsSlashVProperty() {
    String source = "var s = '\\v';";
    Node root = transform(source, es5Config);
    Node strNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.STRING, strNode.getType());
    assertTrue(strNode.getBooleanProp(Node.SLASH_V));
  }

  // Tests misplaced type annotation warning
  @Test
  public void testMisplacedTypeAnnotation_onStatement_reportsWarning() {
    String source = "/** @type {number} */ if (true) {}";
    transform(source, es5Config);
    assertTrue(errorReporter.hasWarning(IRFactory.MISPLACED_TYPE_ANNOTATION));
  }

  // Tests invalid decrement target error
  @Test
  public void testUnaryExpression_invalidDecrement_reportsError() {
    String source = "--10;";
    transform(source, es5Config);
    assertTrue(errorReporter.hasError("invalid decrement target"));
  }

  // Tests valid delete operand on property and getelem
  @Test
  public void testUnaryExpression_validDelete_transformsCorrectly() {
    String source = "delete obj.prop; delete obj['prop'];";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    assertEquals(Token.EXPR_RESULT, root.getFirstChild().getType());
    assertEquals(Token.DELPROP, root.getFirstChild().getFirstChild().getType());
  }

  // Tests for-in loop transformation
  @Test
  public void testForInLoop_transformsCorrectly() {
    String source = "for (var key in obj) { console.log(key); }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node forInNode = root.getFirstChild();
    assertEquals(Token.FOR, forInNode.getType());
    assertEquals(Token.VAR, forInNode.getFirstChild().getType());
  }

  // Tests labelled statement and break/continue with labels
  @Test
  public void testLabelAndBreakContinue_transformsCorrectly() {
    String source = "loop1: while (true) { if (false) continue loop1; else break loop1; }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  // Tests function expression and function declaration with parameters and return
  @Test
  public void testFunctionDeclarationAndExpression_transformsCorrectly() {
    String source = "function foo(a, b) { return a + b; } var bar = function(x) { return; };";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    assertEquals(Token.FUNCTION, root.getFirstChild().getType());
    assertEquals(Token.VAR, root.getLastChild().getType());
  }

  // Tests binary operators, ternary conditional, and comma expression
  @Test
  public void testBinaryAndTernaryExpressions_transformsCorrectly() {
    String source = "var res = (a && b) || (c === d) ? (1, 2) : (e instanceof f);";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node hookNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
  }

  // Tests array literal with empty slots (elisions)
  @Test
  public void testArrayLiteral_withEmptySlots_transformsCorrectly() {
    String source = "var arr = [1, , 3];";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node arrayNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrayNode.getType());
    assertEquals(Token.EMPTY, arrayNode.getChildAtIndex(1).getType());
  }

  // Tests regular expression literal transformation
  @Test
  public void testRegExpLiteral_transformsCorrectly() {
    String source = "var regex = /abc/gi;";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node regexNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, regexNode.getType());
  }

  // Tests postfix increment and decrement
  @Test
  public void testPostfixIncrementAndDecrement_transformsCorrectly() {
    String source = "x++; y--;";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node incNode = root.getFirstChild().getFirstChild();
    Node decNode = root.getLastChild().getFirstChild();
    assertEquals(Token.INC, incNode.getType());
    assertTrue(incNode.getBooleanProp(Node.INCRDECR_PROP));
    assertEquals(Token.DEC, decNode.getType());
    assertTrue(decNode.getBooleanProp(Node.INCRDECR_PROP));
  }

  // Tests with statement and throw statement
  @Test
  public void testWithAndThrowStatements_transformsCorrectly() {
    String source = "with (obj) { throw new Error('msg'); }";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node withNode = root.getFirstChild();
    assertEquals(Token.WITH, withNode.getType());
    Node blockNode = withNode.getLastChild();
    assertEquals(Token.THROW, blockNode.getFirstChild().getType());
  }

  // Tests fileoverview JSDoc comments
  @Test
  public void testFileOverviewJSDoc_attachesToRootNode() {
    String source = "/** @fileoverview Test file summary */ var a = 1;";
    Node root = transform(source, es5Config);
    assertNotNull(root.getJSDocInfo());
  }

  // Tests type cast expression with parentheses
  @Test
  public void testTypeCastExpression_attachesJSDocToCastNode() {
    String source = "var x = /** @type {string} */ (val);";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node nameNode = root.getFirstChild().getFirstChild();
    Node castNode = nameNode.getFirstChild();
    assertNotNull(castNode.getJSDocInfo());
  }

  // Tests object literal with numeric and quoted string keys
  @Test
  public void testObjectLiteral_stringAndNumericKeys_transformsCorrectly() {
    String source = "var obj = { 'a': 1, 2: 'two', c: 3 };";
    Node root = transform(source, es5Config);
    assertEquals(0, errorReporter.errors.size());
    Node objLit = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(3, objLit.getChildCount());
  }

  private static class TestErrorReporter implements ErrorReporter {
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> errors = new ArrayList<String>();

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

    public boolean hasWarning(String message) {
      for (String w : warnings) {
        if (w.contains(message)) {
          return true;
        }
      }
      return false;
    }

    public boolean hasError(String message) {
      for (String e : errors) {
        if (e.contains(message)) {
          return true;
        }
      }
      return false;
    }
  }
}
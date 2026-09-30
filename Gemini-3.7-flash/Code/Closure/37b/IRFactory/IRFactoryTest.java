package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class IRFactoryTest {

  private SimpleErrorReporter errorReporter;

  private static class SimpleErrorReporter implements ErrorReporter {
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
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
  }

  private Node parse(String source, LanguageMode mode, boolean isIdeMode) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageMode(CompilerEnvirons.parseLanguageVersion(
        mode == LanguageMode.ECMASCRIPT3 ? "ECMASCRIPT3" : "ECMASCRIPT5"));
    env.setWarnTrailingComma(true);

    Parser p = new Parser(env, errorReporter);
    AstRoot astRoot = p.parse(source, "test.js", 1);

    Set<String> emptySet = Collections.emptySet();
    Config config = new Config(emptySet, emptySet, isIdeMode, mode, false);

    return IRFactory.transformTree(astRoot, null, source, config, errorReporter);
  }

  private Node parse(String source) {
    return parse(source, LanguageMode.ECMASCRIPT5, false);
  }

  // Tests transformation of basic variable declaration and assignments
  @Test
  public void testTransformTree_varDeclaration_createsScriptWithVar() {
    Node root = parse("var x = 10;");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(1, root.getChildCount());
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals("x", nameNode.getString());
    Node numNode = nameNode.getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(10.0, numNode.getDouble(), 0.0);
  }

  // Tests function declaration transformation with parameters and body
  @Test
  public void testTransformTree_functionDeclaration_createsFunctionNode() {
    Node root = parse("function foo(a, b) { return a + b; }");
    assertEquals(Token.SCRIPT, root.getType());
    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node fnName = fnNode.getFirstChild();
    assertEquals(Token.NAME, fnName.getType());
    assertEquals("foo", fnName.getString());

    Node paramList = fnName.getNext();
    assertEquals(Token.PARAM_LIST, paramList.getType());
    assertEquals(2, paramList.getChildCount());

    Node body = paramList.getNext();
    assertEquals(Token.BLOCK, body.getType());
  }

  // Tests anonymous function expression parsing
  @Test
  public void testTransformTree_unnamedFunctionExpression_createsFunctionNodeWithEmptyName() {
    Node root = parse("var f = function() {};");
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node fnName = fnNode.getFirstChild();
    assertEquals(Token.NAME, fnName.getType());
    assertEquals("", fnName.getString());
  }

  // Tests control structures: if, else, while, do-while, and for loop
  @Test
  public void testTransformTree_controlFlowStatements_createsExpectedNodes() {
    String source = "if (true) { x = 1; } else { x = 2; } while (x < 10) { x++; } do { x--; } while (x > 0); for (var i = 0; i < 5; i++) {}";
    Node root = parse(source);
    assertEquals(Token.SCRIPT, root.getType());

    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());

    Node whileNode = ifNode.getNext();
    assertEquals(Token.WHILE, whileNode.getType());

    Node doNode = whileNode.getNext();
    assertEquals(Token.DO, doNode.getType());

    Node forNode = doNode.getNext();
    assertEquals(Token.FOR, forNode.getType());
  }

  // Tests for-in loop construct
  @Test
  public void testTransformTree_forInLoop_createsForNode() {
    Node root = parse("for (var p in obj) { process(p); }");
    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(3, forNode.getChildCount());
  }

  // Tests try-catch-finally statement transformation
  @Test
  public void testTransformTree_tryCatchFinally_createsTryNode() {
    Node root = parse("try { throw 'err'; } catch (e) { log(e); } finally { cleanup(); }");
    Node tryNode = root.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    Node tryBlock = tryNode.getFirstChild();
    assertEquals(Token.BLOCK, tryBlock.getType());
    Node catchBlock = tryBlock.getNext();
    assertEquals(Token.BLOCK, catchBlock.getType());
    Node catchClause = catchBlock.getFirstChild();
    assertEquals(Token.CATCH, catchClause.getType());
    Node finallyBlock = catchBlock.getNext();
    assertEquals(Token.BLOCK, finallyBlock.getType());
  }

  // Tests switch statement with case, default, and synthetic blocks
  @Test
  public void testTransformTree_switchStatement_createsSwitchCaseNodes() {
    Node root = parse("switch (x) { case 1: break; default: break; }");
    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    Node expr = switchNode.getFirstChild();
    assertEquals("x", expr.getString());
    Node caseNode = expr.getNext();
    assertEquals(Token.CASE, caseNode.getType());
    Node defaultNode = caseNode.getNext();
    assertEquals(Token.DEFAULT_CASE, defaultNode.getType());
  }

  // Tests object literal with properties, getters, and setters
  @Test
  public void testTransformTree_objectLiteralWithGetterSetter_createsDefNodes() {
    Node root = parse("var obj = { a: 1, get b() { return 2; }, set c(v) { this.x = v; } };");
    Node varNode = root.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(3, objLit.getChildCount());

    Node propA = objLit.getFirstChild();
    assertEquals(Token.STRING, propA.getType());
    assertEquals("a", propA.getString());

    Node propB = propA.getNext();
    assertEquals(Token.GETTER_DEF, propB.getType());
    assertEquals("b", propB.getString());

    Node propC = propB.getNext();
    assertEquals(Token.SETTER_DEF, propC.getType());
    assertEquals("c", propC.getString());
  }

  // Tests array literal creation
  @Test
  public void testTransformTree_arrayLiteral_createsArraylitNode() {
    Node root = parse("var arr = [1, 'two', true, null];");
    Node varNode = root.getFirstChild();
    Node arrayLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrayLit.getType());
    assertEquals(4, arrayLit.getChildCount());
  }

  // Tests unary expressions including negation, delete, increment, and decrement
  @Test
  public void testTransformTree_unaryExpressions_correctlyFoldAndTransform() {
    Node root = parse("var a = -5; var b = !true; var c = ~0; var d = typeof a; delete obj.p; x++; --y;");
    assertEquals(Token.SCRIPT, root.getType());

    Node varA = root.getFirstChild();
    Node negNum = varA.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, negNum.getType());
    assertEquals(-5.0, negNum.getDouble(), 0.0);

    Node delExpr = root.getChildAtIndex(4);
    assertEquals(Token.EXPR_RESULT, delExpr.getType());
    assertEquals(Token.DELPROP, delExpr.getFirstChild().getType());

    Node incExpr = root.getChildAtIndex(5);
    assertEquals(Token.INC, incExpr.getFirstChild().getType());
    assertTrue(incExpr.getFirstChild().getBooleanProp(Node.INCRDECR_PROP));
  }

  // Tests conditional (hook) expression
  @Test
  public void testTransformTree_conditionalExpression_createsHookNode() {
    Node root = parse("var res = condition ? 1 : 2;");
    Node hookNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
    assertEquals(3, hookNode.getChildCount());
  }

  // Tests labeled statements, break, and continue with labels
  @Test
  public void testTransformTree_labeledStatements_createsLabelAndBreakNodes() {
    Node root = parse("outer: while(true) { inner: for(;;) { continue outer; break outer; } }");
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("outer", labelName.getString());
  }

  // Tests directive parsing: "use strict"
  @Test
  public void testTransformTree_useStrictDirective_setsDirectivesOnScript() {
    Node root = parse("'use strict'; var x = 1;");
    assertNotNull(root.getDirectives());
    assertTrue(root.getDirectives().contains("use strict"));
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests string literal with vertical tab handling
  @Test
  public void testTransformTree_verticalTabInString_setsSlashVProp() {
    Node root = parse("var s = '\\v';");
    Node strNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.STRING, strNode.getType());
    assertTrue(strNode.getBooleanProp(Node.SLASH_V));
  }

  // Tests warning generation for suspicious non-JSDoc comment annotations
  @Test
  public void testTransformTree_suspiciousBlockComment_generatesWarning() {
    parse("/* @type {number} */ var x = 1;");
    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests error reporting for reserved keywords used as identifiers in ES5 mode
  @Test
  public void testTransformTree_reservedKeywordInES5_reportsError() {
    parse("var class = 1;", LanguageMode.ECMASCRIPT5, false);
    assertTrue(errorReporter.errors.size() > 0);
  }

  // Tests IDE mode length setting on transformed nodes
  @Test
  public void testTransformTree_ideMode_setsNodeLength() {
    Node root = parse("var abc = 123;", LanguageMode.ECMASCRIPT5, true);
    Node varNode = root.getFirstChild();
    assertTrue(varNode.getLength() > 0);
  }

  // Tests member access expressions: getprop and getelem
  @Test
  public void testTransformTree_propertyAccess_createsGetPropAndGetElem() {
    Node root = parse("var a = obj.prop; var b = arr[0]; new Constructor(1);");
    Node varA = root.getFirstChild();
    Node getProp = varA.getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getProp.getType());

    Node varB = varA.getNext();
    Node getElem = varB.getFirstChild().getFirstChild();
    assertEquals(Token.GETELEM, getElem.getType());

    Node newExpr = varB.getNext().getFirstChild();
    assertEquals(Token.NEW, newExpr.getType());
  }

  // Tests with statement transformation
  @Test
  public void testTransformTree_withStatement_createsWithNode() {
    Node root = parse("with (obj) { x = 1; }", LanguageMode.ECMASCRIPT3, false);
    assertEquals(Token.SCRIPT, root.getType());
    Node withNode = root.getFirstChild();
    assertEquals(Token.WITH, withNode.getType());
    assertEquals(Token.NAME, withNode.getFirstChild().getType());
    assertEquals("obj", withNode.getFirstChild().getString());
    assertEquals(Token.BLOCK, withNode.getLastChild().getType());
  }

  // Tests debugger statement transformation
  @Test
  public void testTransformTree_debuggerStatement_createsDebuggerNode() {
    Node root = parse("debugger;");
    assertEquals(Token.SCRIPT, root.getType());
    Node debuggerNode = root.getFirstChild();
    assertEquals(Token.DEBUGGER, debuggerNode.getType());
  }

  // Tests empty statement transformation
  @Test
  public void testTransformTree_emptyStatement_createsEmptyNode() {
    Node root = parse(";");
    assertEquals(Token.SCRIPT, root.getType());
    Node emptyNode = root.getFirstChild();
    assertEquals(Token.EMPTY, emptyNode.getType());
  }

  // Tests regular expression literal transformation
  @Test
  public void testTransformTree_regExpLiteral_createsRegExpNode() {
    Node root = parse("var re = /abc/gi;");
    Node varNode = root.getFirstChild();
    Node reNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, reNode.getType());
    assertEquals(Token.STRING, reNode.getFirstChild().getType());
    assertEquals("abc", reNode.getFirstChild().getString());
    assertEquals(Token.STRING, reNode.getLastChild().getType());
    assertEquals("gi", reNode.getLastChild().getString());
  }

  // Tests comma operator expression transformation
  @Test
  public void testTransformTree_commaOperator_createsCommaNode() {
    Node root = parse("var x = (1, 2);");
    Node varNode = root.getFirstChild();
    Node commaNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.COMMA, commaNode.getType());
    assertEquals(2, commaNode.getChildCount());
  }

  // Tests unary plus and void operator transformation
  @Test
  public void testTransformTree_unaryPosAndVoid_createsPosAndVoidNodes() {
    Node root = parse("var a = +x; var b = void 0;");
    Node varA = root.getFirstChild();
    Node posNode = varA.getFirstChild().getFirstChild();
    assertEquals(Token.POS, posNode.getType());

    Node varB = varA.getNext();
    Node voidNode = varB.getFirstChild().getFirstChild();
    assertEquals(Token.VOID, voidNode.getType());
  }

  // Tests literal keywords: this, null, true, false
  @Test
  public void testTransformTree_literalKeywords_createsKeywordNodes() {
    Node root = parse("this; null; true; false;");
    Node expr1 = root.getFirstChild();
    assertEquals(Token.THIS, expr1.getFirstChild().getType());

    Node expr2 = expr1.getNext();
    assertEquals(Token.NULL, expr2.getFirstChild().getType());

    Node expr3 = expr2.getNext();
    assertEquals(Token.TRUE, expr3.getFirstChild().getType());

    Node expr4 = expr3.getNext();
    assertEquals(Token.FALSE, expr4.getFirstChild().getType());
  }

  // Tests binary arithmetic and logical operators
  @Test
  public void testTransformTree_binaryAndLogicalOperators_createsBinaryNodes() {
    Node root = parse("a + b; a - b; a * b; a / b; a % b; a && b; a || b;");
    Node expr = root.getFirstChild();
    assertEquals(Token.ADD, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.SUB, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.MUL, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.DIV, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.MOD, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.AND, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.OR, expr.getFirstChild().getType());
  }

  // Tests comparison and bitwise operators
  @Test
  public void testTransformTree_comparisonAndBitwiseOperators_createsExpectedNodes() {
    Node root = parse("a == b; a != b; a === b; a !== b; a < b; a <= b; a > b; a >= b; a instanceof b; a in b; a & b; a | b; a ^ b; a << b; a >> b; a >>> b;");
    Node expr = root.getFirstChild();
    assertEquals(Token.EQ, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.NE, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.SHEQ, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.SHNE, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.LT, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.LE, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.GT, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.GE, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.INSTANCEOF, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.IN, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.BITAND, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.BITOR, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.BITXOR, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.LSH, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.RSH, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.URSH, expr.getFirstChild().getType());
  }

  // Tests compound assignment operators
  @Test
  public void testTransformTree_compoundAssignments_createsAssignNodes() {
    Node root = parse("x += 1; x -= 1; x *= 1; x /= 1; x %= 1; x &= 1; x |= 1; x ^= 1; x <<= 1; x >>= 1; x >>>= 1;");
    Node expr = root.getFirstChild();
    assertEquals(Token.ASSIGN_ADD, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_SUB, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_MUL, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_DIV, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_MOD, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_BITAND, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_BITOR, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_BITXOR, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_LSH, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_RSH, expr.getFirstChild().getType());

    expr = expr.getNext();
    assertEquals(Token.ASSIGN_URSH, expr.getFirstChild().getType());
  }

  // Tests JSDoc comment attachment to AST nodes
  @Test
  public void testTransformTree_jsdocAttachment_attachesJSDocInfo() {
    Node root = parse("/** @type {number} */ var x = 42;");
    Node varNode = root.getFirstChild();
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasType());
  }

  // Tests ES5 strict mode duplicate parameter warning/error
  @Test
  public void testTransformTree_duplicateParamInStrictMode_reportsError() {
    parse("'use strict'; function f(a, a) {}", LanguageMode.ECMASCRIPT5_STRICT, false);
    assertTrue(errorReporter.errors.size() > 0);
  }

  // Tests ES3 trailing comma warning
  @Test
  public void testTransformTree_trailingCommaInES3_reportsWarning() {
    parse("var obj = { a: 1, };", LanguageMode.ECMASCRIPT3, false);
    assertTrue(errorReporter.warnings.size() > 0);
  }
}
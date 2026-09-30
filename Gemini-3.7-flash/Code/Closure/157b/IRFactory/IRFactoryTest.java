package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.Context;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
  private Set<String> extraAnnotations;
  private Set<String> extraSuppressions;

  @Before
  public void setUp() {
    errors = new ArrayList<String>();
    warnings = new ArrayList<String>();
    extraAnnotations = ImmutableSet.of();
    extraSuppressions = ImmutableSet.of();
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
        errors.add(message);
        return new EvaluatorException(message);
      }
    };
  }

  private Node parse(String source, Config.LanguageMode languageMode) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setLanguageVersion(Context.VERSION_1_8);
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);

    Parser parser = new Parser(env, errorReporter);
    AstRoot astRoot = parser.parse(source, "testcode", 1);
    Config config = new Config(extraAnnotations, extraSuppressions, false, languageMode, true);
    return IRFactory.transformTree(astRoot, source, config, errorReporter);
  }

  private Node parse(String source) {
    return parse(source, Config.LanguageMode.ECMASCRIPT5);
  }

  // Tests transformation of basic variable declaration and literal types
  @Test
  public void testTransformTree_variableDeclarationAndLiterals_returnsCorrectTree() {
    String source = "var a = 1, b = 'hello', c = true, d = null;";
    Node root = parse(source);

    assertEquals(Token.SCRIPT, root.getType());
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());

    Node varA = varNode.getFirstChild();
    assertEquals(Token.NAME, varA.getType());
    assertEquals("a", varA.getString());
    assertEquals(1.0, varA.getFirstChild().getDouble(), 0.0);

    Node varB = varA.getNext();
    assertEquals("b", varB.getString());
    assertEquals("hello", varB.getFirstChild().getString());

    Node varC = varB.getNext();
    assertEquals("c", varC.getString());
    assertEquals(Token.TRUE, varC.getFirstChild().getType());

    Node varD = varC.getNext();
    assertEquals("d", varD.getString());
    assertEquals(Token.NULL, varD.getFirstChild().getType());
  }

  // Tests object literal property keys quoting and types
  @Test
  public void testTransformTree_objectLiteralKeys_preservesQuotingAndTypes() {
    String source = "var obj = {a: 1, 'b': 2, 3: 4};";
    Node root = parse(source);

    Node objLit = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());

    Node keyA = objLit.getFirstChild();
    assertEquals(Token.STRING, keyA.getType());
    assertEquals("a", keyA.getString());
    assertFalse(keyA.isQuotedString());

    Node keyB = keyA.getNext();
    assertEquals(Token.STRING, keyB.getType());
    assertEquals("b", keyB.getString());
    assertTrue(keyB.isQuotedString());

    Node keyC = keyB.getNext();
    assertEquals(Token.NUMBER, keyC.getType());
    assertEquals(3.0, keyC.getDouble(), 0.0);
  }

  // Tests ES5 getter and setter in object literal
  @Test
  public void testTransformTree_gettersAndSetters_createsGetAndSetNodes() {
    String source = "var obj = { get x() { return 1; }, set x(v) { this.x = v; } };";
    Node root = parse(source, Config.LanguageMode.ECMASCRIPT5);

    Node objLit = root.getFirstChild().getFirstChild().getFirstChild();
    Node getter = objLit.getFirstChild();
    assertEquals(Token.GET, getter.getType());
    assertEquals("x", getter.getString());
    assertEquals(Token.FUNCTION, getter.getFirstChild().getType());

    Node setter = getter.getNext();
    assertEquals(Token.SET, setter.getType());
    assertEquals("x", setter.getString());
    assertEquals(Token.FUNCTION, setter.getFirstChild().getType());
    assertTrue(errors.isEmpty());
  }

  // Tests ES3 mode reporting errors on getters and setters
  @Test
  public void testTransformTree_gettersAndSettersInES3_reportsErrors() {
    String source = "var obj = { get x() { return 1; }, set x(v) { } };";
    parse(source, Config.LanguageMode.ECMASCRIPT3);

    assertEquals(2, errors.size());
    assertTrue(errors.get(0).contains("getters are not supported"));
    assertTrue(errors.get(1).contains("setters are not supported"));
  }

  // Tests parsing and encoding directives like 'use strict'
  @Test
  public void testTransformTree_useStrictDirective_encodesInScriptNode() {
    String source = "'use strict'; var x = 1;";
    Node root = parse(source);

    assertNotNull(root.getDirectives());
    assertTrue(root.getDirectives().contains("use strict"));
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // Tests function declarations with named and unnamed function expressions
  @Test
  public void testTransformTree_functionDeclarationAndExpression_createsFunctionNodes() {
    String source = "function foo(a, b) { return a + b; } var bar = function() {};";
    Node root = parse(source);

    Node fnDecl = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnDecl.getType());
    Node fnName = fnDecl.getFirstChild();
    assertEquals("foo", fnName.getString());
    Node fnParams = fnName.getNext();
    assertEquals(Token.LP, fnParams.getType());
    assertEquals(2, fnParams.getChildCount());

    Node varBar = fnDecl.getNext();
    Node fnExpr = varBar.getFirstChild().getFirstChild();
    assertEquals(Token.FUNCTION, fnExpr.getType());
    assertEquals("", fnExpr.getFirstChild().getString());
  }

  // Tests control structures: if, while, do-while, and for loops
  @Test
  public void testTransformTree_controlStructures_createsValidNodes() {
    String source = "if (true) { while (false) {} } else { do {} while(false); } for (var i = 0; i < 10; i++) {}";
    Node root = parse(source);

    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(Token.TRUE, ifNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, ifNode.getFirstChild().getNext().getType());
    assertEquals(Token.BLOCK, ifNode.getLastChild().getType());

    Node forNode = ifNode.getNext();
    assertEquals(Token.FOR, forNode.getType());
  }

  // Tests for-in loop transformation
  @Test
  public void testTransformTree_forInLoop_createsForInNode() {
    String source = "for (var key in obj) { }";
    Node root = parse(source);

    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.VAR, forNode.getFirstChild().getType());
    assertEquals(Token.NAME, forNode.getFirstChild().getNext().getType());
  }

  // Tests try-catch-finally block transformation
  @Test
  public void testTransformTree_tryCatchFinally_createsTryCatchFinallyNodes() {
    String source = "try { throw 1; } catch (e) { } finally { }";
    Node root = parse(source);

    Node tryNode = root.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    Node catchBlock = tryNode.getFirstChild().getNext();
    assertEquals(Token.BLOCK, catchBlock.getType());
    Node catchClause = catchBlock.getFirstChild();
    assertEquals(Token.CATCH, catchClause.getType());
  }

  // Tests switch-case and default statements
  @Test
  public void testTransformTree_switchStatement_createsSwitchCases() {
    String source = "switch (x) { case 1: break; default: break; }";
    Node root = parse(source);

    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    Node caseNode = switchNode.getFirstChild().getNext();
    assertEquals(Token.CASE, caseNode.getType());
    Node defaultNode = caseNode.getNext();
    assertEquals(Token.DEFAULT, defaultNode.getType());
  }

  // Tests unary expressions including negation folding and postfix increment
  @Test
  public void testTransformTree_unaryExpressions_foldsNumberAndHandlesIncDec() {
    String source = "var x = -5; x++; ++x; !x; ~x; typeof x; void 0; delete x.a;";
    Node root = parse(source);

    Node varNode = root.getFirstChild();
    Node negNum = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, negNum.getType());
    assertEquals(-5.0, negNum.getDouble(), 0.0);

    Node expr1 = varNode.getNext();
    assertEquals(Token.EXPR_RESULT, expr1.getType());
    Node incNode = expr1.getFirstChild();
    assertEquals(Token.INC, incNode.getType());
    assertEquals(Boolean.TRUE, incNode.getProp(Node.INCRDECR_PROP));
  }

  // Tests infix, binary, and ternary expressions
  @Test
  public void testTransformTree_binaryAndConditionalExpressions_createsExpectedTokens() {
    String source = "var r = (a ? b : c) + (x && y) || (m == n);";
    Node root = parse(source);

    Node expr = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OR, expr.getType());
    Node addNode = expr.getFirstChild();
    assertEquals(Token.ADD, addNode.getType());
    Node hookNode = addNode.getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
    assertEquals(Boolean.TRUE, hookNode.getProp(Node.PARENTHESIZED_PROP));
  }

  // Tests labeled statements, break and continue with labels
  @Test
  public void testTransformTree_labeledStatements_createsLabelAndBreakNodes() {
    String source = "outer: while(true) { break outer; continue outer; }";
    Node root = parse(source);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("outer", labelName.getString());

    Node whileNode = labelName.getNext();
    Node block = whileNode.getFirstChild().getNext();
    Node breakNode = block.getFirstChild().getFirstChild();
    assertEquals(Token.BREAK, breakNode.getType());
    assertEquals("outer", breakNode.getFirstChild().getString());
  }

  // Tests array literals and regexp literals
  @Test
  public void testTransformTree_arrayAndRegExpLiterals_createsArrayLitAndRegExp() {
    String source = "var arr = [1, 'str']; var re = /abc/gi;";
    Node root = parse(source);

    Node arrLit = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(2, arrLit.getChildCount());

    Node regExpNode = root.getFirstChild().getNext().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, regExpNode.getType());
    assertEquals("abc", regExpNode.getFirstChild().getString());
    assertEquals("gi", regExpNode.getFirstChild().getNext().getString());
  }

  // Tests ES5 reserved keyword detection
  @Test
  public void testTransformTree_es5ReservedKeyword_reportsError() {
    String source = "var class = 1;";
    parse(source, Config.LanguageMode.ECMASCRIPT5);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("reserved word"));
  }

  // Tests ES5 strict reserved keyword detection
  @Test
  public void testTransformTree_es5StrictReservedKeyword_reportsError() {
    String source = "var let = 1;";
    parse(source, Config.LanguageMode.ECMASCRIPT5_STRICT);

    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("reserved word"));
  }

  // Tests file-level JSDoc parsing and attachment
  @Test
  public void testTransformTree_fileOverviewJsDoc_attachesToFileOverviewInfo() {
    String source = "/** @fileoverview Test overview */ var x = 1;";
    Node root = parse(source);

    assertNotNull(root.getJSDocInfo());
    assertEquals("Test overview", root.getJSDocInfo().getFileOverview());
  }

  // Tests call and new expressions transformation
  @Test
  public void testTransformTree_callAndNewExpressions_createsExpectedNodes() {
    String source = "var x = new Foo(1, 2); bar('test');";
    Node root = parse(source);

    Node varNode = root.getFirstChild();
    Node newExpr = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NEW, newExpr.getType());
    assertEquals("Foo", newExpr.getFirstChild().getString());
    assertEquals(1.0, newExpr.getFirstChild().getNext().getDouble(), 0.0);

    Node callExpr = varNode.getNext().getFirstChild();
    assertEquals(Token.CALL, callExpr.getType());
    assertEquals("bar", callExpr.getFirstChild().getString());
    assertEquals("test", callExpr.getFirstChild().getNext().getString());
  }

  // Tests property and element access expressions (GETPROP, GETELEM)
  @Test
  public void testTransformTree_propertyAndElementAccess_createsGetPropAndGetElem() {
    String source = "var a = obj.prop; var b = obj['prop'];";
    Node root = parse(source);

    Node getPropNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getPropNode.getType());
    assertEquals("obj", getPropNode.getFirstChild().getString());
    assertEquals("prop", getPropNode.getLastChild().getString());

    Node getElemNode = root.getLastChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETELEM, getElemNode.getType());
    assertEquals("obj", getElemNode.getFirstChild().getString());
    assertEquals("prop", getElemNode.getLastChild().getString());
  }

  // Tests comma operator expression transformation
  @Test
  public void testTransformTree_commaOperator_createsCommaNode() {
    String source = "var x = (1, 2, 3);";
    Node root = parse(source);

    Node commaNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.COMMA, commaNode.getType());
    assertEquals(Token.COMMA, commaNode.getFirstChild().getType());
  }

  // Tests sparse array literal containing empty elements
  @Test
  public void testTransformTree_sparseArrayLiteral_containsEmptyNodes() {
    String source = "var arr = [1, , 3];";
    Node root = parse(source);

    Node arrLit = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(Token.NUMBER, arrLit.getFirstChild().getType());
    assertEquals(Token.EMPTY, arrLit.getFirstChild().getNext().getType());
    assertEquals(Token.NUMBER, arrLit.getLastChild().getType());
  }

  // Tests debugger and with statements
  @Test
  public void testTransformTree_debuggerAndWithStatements_createsExpectedNodes() {
    String source = "debugger; with (obj) { var x = 1; }";
    Node root = parse(source);

    Node dbgNode = root.getFirstChild();
    assertEquals(Token.DEBUGGER, dbgNode.getType());

    Node withNode = dbgNode.getNext();
    assertEquals(Token.WITH, withNode.getType());
    assertEquals("obj", withNode.getFirstChild().getString());
  }

  // Tests empty statement transformation
  @Test
  public void testTransformTree_emptyStatement_createsEmptyNode() {
    String source = ";";
    Node root = parse(source);

    assertEquals(Token.EMPTY, root.getFirstChild().getType());
  }

  // Tests ES5 keywords as object keys in ES5 mode vs ES3 mode
  @Test
  public void testTransformTree_keywordsAsObjectKeys_allowedInES5AndRejectedInES3() {
    String source = "var obj = { delete: 1, class: 2, default: 3 };";

    Node rootES5 = parse(source, Config.LanguageMode.ECMASCRIPT5);
    assertTrue(errors.isEmpty());
    assertEquals(Token.OBJECTLIT, rootES5.getFirstChild().getFirstChild().getFirstChild().getType());

    parse(source, Config.LanguageMode.ECMASCRIPT3);
    assertFalse(errors.isEmpty());
  }

  // Tests JSDoc info attachment to function and its parameter
  @Test
  public void testTransformTree_jsdocOnFunctionAndParameters_attachesJSDocInfo() {
    String source = "/** @param {number} x */ function f(x) {}";
    Node root = parse(source);

    Node fnNode = root.getFirstChild();
    assertNotNull(fnNode.getJSDocInfo());
    assertNotNull(fnNode.getJSDocInfo().getParameterType("x"));
  }

  // Tests extra annotations and suppressions configuration
  @Test
  public void testTransformTree_extraAnnotationsAndSuppressions_recognizedWithoutWarnings() {
    extraAnnotations = ImmutableSet.of("customAnnotation");
    extraSuppressions = ImmutableSet.of("customSuppression");
    String source = "/** @customAnnotation \n * @suppress {customSuppression} */ var x = 1;";
    Node root = parse(source);

    assertNotNull(root.getFirstChild().getJSDocInfo());
    assertTrue(warnings.isEmpty());
  }

  // Tests suspect comment warning for non-JSDoc comment with annotations
  @Test
  public void testTransformTree_suspectComment_emitsWarning() {
    String source = "/* @type {number} */ var x = 1;";
    parse(source);

    assertFalse(warnings.isEmpty());
  }

  // Tests duplicate parameter detection in ES5 strict mode
  @Test
  public void testTransformTree_duplicateParamInES5Strict_reportsError() {
    String source = "function f(a, a) { 'use strict'; }";
    parse(source, Config.LanguageMode.ECMASCRIPT5_STRICT);

    assertFalse(errors.isEmpty());
  }

  // Tests delete of unqualified identifier in ES5 strict mode
  @Test
  public void testTransformTree_deleteUnqualifiedIdentifierInES5Strict_reportsError() {
    String source = "'use strict'; delete x;";
    parse(source, Config.LanguageMode.ECMASCRIPT5_STRICT);

    assertFalse(errors.isEmpty());
  }
}
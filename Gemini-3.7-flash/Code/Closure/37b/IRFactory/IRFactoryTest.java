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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class IRFactoryTest {

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

    public boolean hasWarnings() {
      return !warnings.isEmpty();
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }

  private TestErrorReporter errorReporter;
  private Set<String> emptyAnnotationSet;

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
    emptyAnnotationSet = Collections.emptySet();
  }

  private Node parseAndTransform(String js, LanguageMode mode, boolean isIdeMode) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(
        mode == LanguageMode.ECMASCRIPT3
            ? com.google.javascript.rhino.head.Context.VERSION_1_5
            : com.google.javascript.rhino.head.Context.VERSION_1_8);

    Parser parser = new Parser(env, errorReporter);
    AstRoot root = parser.parse(js, "test.js", 1);
    StaticSourceFile sourceFile = new SimpleSourceFile("test.js", false);
    Config config = new Config(emptyAnnotationSet, emptyAnnotationSet, isIdeMode, mode, false);

    return IRFactory.transformTree(root, sourceFile, js, config, errorReporter);
  }

  // Tests basic script parsing and node type
  @Test
  public void testTransformTree_simpleExpression_returnsScriptNode() {
    Node script = parseAndTransform("var a = 1;", LanguageMode.ECMASCRIPT3, false);
    assertNotNull(script);
    assertEquals(Token.SCRIPT, script.getType());
    assertTrue(script.hasChildren());
    Node varNode = script.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
  }

  // Tests function declaration transformation
  @Test
  public void testTransformTree_functionDeclaration_createsFunctionNode() {
    Node script = parseAndTransform("function foo(x) { return x; }", LanguageMode.ECMASCRIPT3, false);
    Node fn = script.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node fnName = fn.getFirstChild();
    assertEquals(Token.NAME, fnName.getType());
    assertEquals("foo", fnName.getString());
    Node paramList = fnName.getNext();
    assertEquals(Token.PARAM_LIST, paramList.getType());
    assertTrue(paramList.hasChildren());
    assertEquals("x", paramList.getFirstChild().getString());
    Node body = paramList.getNext();
    assertEquals(Token.BLOCK, body.getType());
  }

  // Tests anonymous function expression transformation
  @Test
  public void testTransformTree_anonymousFunctionExpression_createsEmptyName() {
    Node script = parseAndTransform("var f = function() {};", LanguageMode.ECMASCRIPT3, false);
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fn = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    assertEquals("", fn.getFirstChild().getString());
  }

  // Tests directive parsing in SCRIPT node
  @Test
  public void testTransformTree_useStrictDirective_setsDirectivesOnScript() {
    Node script = parseAndTransform("'use strict'; var x = 1;", LanguageMode.ECMASCRIPT5, false);
    Set<String> directives = script.getDirectives();
    assertNotNull(directives);
    assertTrue(directives.contains("use strict"));
    assertEquals(Token.VAR, script.getFirstChild().getType());
  }

  // Tests suspicious comment warning
  @Test
  public void testTransformTree_suspiciousComment_reportsWarning() {
    parseAndTransform("/* @type {number} */ var x = 1;", LanguageMode.ECMASCRIPT3, false);
    assertTrue(errorReporter.hasWarnings());
    assertTrue(errorReporter.warnings.get(0).contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  // Tests valid JSDoc comment processing
  @Test
  public void testTransformTree_validJsDoc_attachesJSDocInfo() {
    Node script = parseAndTransform("/** @type {number} */ var x = 1;", LanguageMode.ECMASCRIPT3, false);
    Node varNode = script.getFirstChild();
    assertNotNull(varNode.getJSDocInfo());
  }

  // Tests reserved keyword check in ECMASCRIPT5 mode
  @Test
  public void testTransformTree_es5ReservedKeyword_reportsError() {
    parseAndTransform("var class = 1;", LanguageMode.ECMASCRIPT5, false);
    assertTrue(errorReporter.hasErrors());
    assertTrue(errorReporter.errors.get(0).contains("reserved word"));
  }

  // Tests ES5 getter and setter in object literal
  @Test
  public void testTransformTree_getterSetterObjectLiteral_createsGetterSetterDefs() {
    String js = "var obj = { get a() { return 1; }, set a(v) { this.x = v; } };";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT5, false);
    Node varNode = script.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    Node getDef = objLit.getFirstChild();
    assertEquals(Token.GETTER_DEF, getDef.getType());
    assertEquals("a", getDef.getString());
    Node setDef = getDef.getNext();
    assertEquals(Token.SETTER_DEF, setDef.getType());
    assertEquals("a", setDef.getString());
  }

  // Tests ES3 getter error reporting
  @Test
  public void testTransformTree_getterInEs3Mode_reportsError() {
    String js = "var obj = { get a() { return 1; } };";
    parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    assertTrue(errorReporter.hasErrors());
    assertTrue(errorReporter.errors.get(0).contains("getters are not supported"));
  }

  // Tests conditional (hook) expression
  @Test
  public void testTransformTree_conditionalExpression_createsHookNode() {
    Node script = parseAndTransform("var x = true ? 1 : 2;", LanguageMode.ECMASCRIPT3, false);
    Node hook = script.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.HOOK, hook.getType());
    assertEquals(3, hook.getChildCount());
  }

  // Tests try, catch, and finally block handling
  @Test
  public void testTransformTree_tryCatchFinally_createsTryNode() {
    String js = "try { var a = 1; } catch (e) { var b = 2; } finally { var c = 3; }";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node tryNode = script.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertEquals(Token.BLOCK, tryNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, tryNode.getFirstChild().getNext().getType());
  }

  // Tests switch statement with cases and default
  @Test
  public void testTransformTree_switchStatement_createsSwitchCaseNodes() {
    String js = "switch(x) { case 1: break; default: break; }";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node switchNode = script.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    Node caseNode = switchNode.getFirstChild().getNext();
    assertEquals(Token.CASE, caseNode.getType());
    Node defaultNode = caseNode.getNext();
    assertEquals(Token.DEFAULT_CASE, defaultNode.getType());
  }

  // Tests loops: while, do-while, for, and for-in
  @Test
  public void testTransformTree_loops_createsExpectedLoopNodes() {
    String js = "while(true) { break; } do { continue; } while(true); for(var i=0; i<1; i++) {} for(var k in obj) {}";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node whileNode = script.getFirstChild();
    assertEquals(Token.WHILE, whileNode.getType());
    Node doNode = whileNode.getNext();
    assertEquals(Token.DO, doNode.getType());
    Node forNode = doNode.getNext();
    assertEquals(Token.FOR, forNode.getType());
    Node forInNode = forNode.getNext();
    assertEquals(Token.FOR, forInNode.getType());
  }

  // Tests unary expressions including negative numbers, increment, and delete
  @Test
  public void testTransformTree_unaryExpressions_transformsCorrectly() {
    String js = "var a = -5; a++; delete obj.prop;";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node varNode = script.getFirstChild();
    Node numNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(-5.0, numNode.getDouble(), 0.0);

    Node incNode = varNode.getNext().getFirstChild();
    assertEquals(Token.INC, incNode.getType());
    assertTrue(incNode.getBooleanProp(Node.INCRDECR_PROP));

    Node delNode = varNode.getNext().getNext().getFirstChild();
    assertEquals(Token.DELPROP, delNode.getType());
  }

  // Tests invalid increment target error
  @Test
  public void testTransformTree_invalidIncrementTarget_reportsError() {
    parseAndTransform("5++;", LanguageMode.ECMASCRIPT3, false);
    assertTrue(errorReporter.hasErrors());
    assertTrue(errorReporter.errors.get(0).contains("invalid increment target"));
  }

  // Tests labeled statement and break with label
  @Test
  public void testTransformTree_labeledStatement_createsLabelNode() {
    String js = "myLabel: while(true) { break myLabel; }";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node labelNode = script.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("myLabel", labelName.getString());
  }

  // Tests RegExp literal with flags
  @Test
  public void testTransformTree_regexpLiteral_createsRegexpNode() {
    String js = "var re = /abc/gi;";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node reNode = script.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, reNode.getType());
    assertEquals("abc", reNode.getFirstChild().getString());
    assertEquals("gi", reNode.getFirstChild().getNext().getString());
  }

  // Tests string with vertical tab escaping
  @Test
  public void testTransformTree_stringWithVerticalTab_setsSlashVProp() {
    String js = "var s = '\\v';";
    Node script = parseAndTransform(js, LanguageMode.ECMASCRIPT3, false);
    Node strNode = script.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.STRING, strNode.getType());
    assertTrue(strNode.getBooleanProp(Node.SLASH_V));
  }
}
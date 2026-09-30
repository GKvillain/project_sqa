package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  private TestCodeConsumer consumer;
  private CodeGenerator generator;

  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    char getLastChar() {
      return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    String getCode() {
      return sb.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    generator = new CodeGenerator(consumer);
  }

  // Tests null character escaping in JS string
  @Test
  public void testEscapeToDoubleQuotedJsString_nullChar_escapesCorrectly() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("\0");
    assertEquals("\"\\000\"", escaped);
  }

  // Tests null character followed by digits in JS string
  @Test
  public void testEscapeToDoubleQuotedJsString_nullCharFollowedByDigit_escapesCorrectly() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("\0" + "7");
    assertEquals("\"\\0007\"", escaped);
  }

  // Tests standard control character escaping (\n, \r, \t, backslash, quotes)
  @Test
  public void testEscapeToDoubleQuotedJsString_controlCharacters_escapesCorrectly() {
    String input = "line1\nline2\rline3\ttab\\slash\"quote'single";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"line1\\nline2\\rline3\\ttab\\\\slash\\\"quote'single\"", escaped);
  }

  // Tests closing script tag prevention in string literals
  @Test
  public void testEscapeToDoubleQuotedJsString_closingScriptTag_escapesCorrectly() {
    String input = "</script><SCRIPT>";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"<\\/script><SCRIPT>\"", escaped);
  }

  // Tests HTML comment start prevention in string literals
  @Test
  public void testEscapeToDoubleQuotedJsString_htmlCommentStart_escapesCorrectly() {
    String input = "<!-- comment";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"<\\!-- comment\"", escaped);
  }

  // Tests HTML comment end and CDATA end prevention in string literals
  @Test
  public void testEscapeToDoubleQuotedJsString_htmlCommentEndAndCdataEnd_escapesCorrectly() {
    String input = "--> and ]]>";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"--\\> and ]]\\>\"", escaped);
  }

  // Tests regexp escaping without custom charset encoder
  @Test
  public void testRegexpEscape_simplePattern_escapesCorrectly() {
    String escaped = CodeGenerator.regexpEscape("hello/world");
    assertEquals("/hello/world/", escaped);
  }

  // Tests regexp escaping with custom CharsetEncoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_escapesProperly() {
    CharsetEncoder encoder = Charsets.US_ASCII.newEncoder();
    String escaped = CodeGenerator.regexpEscape("\u4e16\u754c", encoder);
    assertEquals("/\\u4e16\\u754c/", escaped);
  }

  // Tests latin identifier escaping preserves original string
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsUnchanged() {
    String input = "myVariable_123$";
    String escaped = CodeGenerator.identifierEscape(input);
    assertEquals(input, escaped);
  }

  // Tests non-latin identifier escaping converts to unicode hex representation
  @Test
  public void testIdentifierEscape_nonLatinIdentifier_escapesToHex() {
    String input = "\u00f8Var";
    String escaped = CodeGenerator.identifierEscape(input);
    assertEquals("\\u00f8Var", escaped);
  }

  // Tests isSimpleNumber boundary and validity checking
  @Test
  public void testIsSimpleNumber_variousInputs_returnsExpected() {
    assertTrue(CodeGenerator.isSimpleNumber("12345"));
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("123a"));
    assertFalse(CodeGenerator.isSimpleNumber("-5"));
  }

  // Tests getSimpleNumber conversion for numbers and NaN cases
  @Test
  public void testGetSimpleNumber_variousInputs_returnsNumberOrNaN() {
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("notANumber")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
  }

  // Tests optimal quote selection in jsString
  @Test
  public void testJsString_quoteSelection_selectsOptimalQuote() {
    // More single quotes than double quotes -> wrap in double quotes
    String s1 = "It's a 'test'";
    assertEquals("\"It's a 'test'\"", generator.jsString(s1));

    // More double quotes than single quotes -> wrap in single quotes
    String s2 = "He said \"hello\" and \"world\"";
    assertEquals("'He said \"hello\" and \"world\"'", generator.jsString(s2));
  }

  // Tests tagAsStrict output
  @Test
  public void testTagAsStrict_emitsUseStrictDirective() {
    generator.tagAsStrict();
    assertEquals("'use strict';", consumer.getCode());
  }

  // Tests code generation for a binary operator (ADD)
  @Test
  public void testAdd_binaryOperatorNode_generatesInfixExpression() {
    Node left = Node.newString(Token.NAME, "a");
    Node right = Node.newString(Token.NAME, "b");
    Node addNode = new Node(Token.ADD, left, right);

    generator.add(addNode);
    assertEquals("a+b", consumer.getCode());
  }

  // Tests code generation for variable declaration (VAR and NAME)
  @Test
  public void testAdd_varDeclarationNode_generatesVarStatement() {
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToFront(Node.newNumber(42));
    Node varNode = new Node(Token.VAR, nameNode);

    generator.add(varNode);
    assertEquals("var x=42", consumer.getCode());
  }

  // Tests code generation for array literal (ARRAYLIT)
  @Test
  public void testAdd_arrayLiteralNode_generatesArray() {
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(Node.newNumber(1));
    arrayLit.addChildToBack(Node.newNumber(2));

    generator.add(arrayLit);
    assertEquals("[1,2]", consumer.getCode());
  }

  // Tests code generation for ternary conditional (HOOK)
  @Test
  public void testAdd_hookNode_generatesTernary() {
    Node cond = Node.newString(Token.NAME, "a");
    Node thenExpr = Node.newNumber(1);
    Node elseExpr = Node.newNumber(2);
    Node hookNode = new Node(Token.HOOK, cond, thenExpr, elseExpr);

    generator.add(hookNode);
    assertEquals("a?1:2", consumer.getCode());
  }

  // Tests code generation for negative numbers
  @Test
  public void testAdd_negNumberNode_generatesNegativeNumber() {
    Node numberNode = Node.newNumber(5);
    Node negNode = new Node(Token.NEG, numberNode);

    generator.add(negNode);
    assertEquals("-5", consumer.getCode());
  }

  // Tests CodeGenerator with custom Charset constructor
  @Test
  public void testConstructor_withCustomCharset_initializesProperly() {
    CodeGenerator customGen = new CodeGenerator(consumer, Charset.forName("UTF-8"));
    customGen.add("test");
    assertEquals("test", consumer.getCode());
  }

  // Tests isSimpleNumber with leading zero numbers
  @Test
  public void testIsSimpleNumber_leadingZeros_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
  }

  // Tests escapeToDoubleQuotedJsString with unicode line separators and control characters
  @Test
  public void testEscapeToDoubleQuotedJsString_unicodeLineSeparatorsAndControls() {
    String input = "\u2028\u2029\u0008\u000c\u000b\u00ad";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertTrue(escaped.contains("\\u2028"));
    assertTrue(escaped.contains("\\u2029"));
    assertTrue(escaped.contains("\\b"));
    assertTrue(escaped.contains("\\f"));
  }

  // Tests regexp escaping with regex flags and html comments
  @Test
  public void testRegexpEscape_withSpecialCharsAndComments() {
    String escaped = CodeGenerator.regexpEscape("<!--</script>-->]]>", null);
    assertTrue(escaped.contains("<\\!--"));
    assertTrue(escaped.contains("<\\/script>"));
    assertTrue(escaped.contains("--\\>"));
    assertTrue(escaped.contains("]]\\>"));
  }

  // Tests code generation for function declarations
  @Test
  public void testAdd_functionNode_generatesFunction() {
    Node fnName = Node.newString(Token.NAME, "foo");
    Node paramList = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "p1"));
    Node body = new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "p1")));
    Node fn = new Node(Token.FUNCTION, fnName, paramList, body);

    generator.add(fn);
    assertEquals("function foo(p1){return p1}", consumer.getCode());
  }

  // Tests code generation for if-else control flow
  @Test
  public void testAdd_ifElseNode_generatesIfElseStatement() {
    Node cond = Node.newString(Token.NAME, "c");
    Node thenBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node elseBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);

    generator.add(ifNode);
    assertEquals("if(c){1}else{2}", consumer.getCode());
  }

  // Tests code generation for for loop
  @Test
  public void testAdd_forLoopNode_generatesForStatement() {
    Node init = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    init.getFirstChild().addChildToFront(Node.newNumber(0));
    Node cond = new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(10));
    Node incr = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    incr.putBooleanProp(Node.INCRDECR_PROP, true);
    Node body = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, init, cond, incr, body);

    generator.add(forNode);
    assertEquals("for(var i=0;i<10;i++){}", consumer.getCode());
  }

  // Tests code generation for while and do-while loops
  @Test
  public void testAdd_whileAndDoWhileLoops_generatesLoops() {
    Node whileCond = Node.newString(Token.NAME, "a");
    Node whileBody = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, whileCond, whileBody);

    generator.add(whileNode);

    Node doCond = Node.newString(Token.NAME, "b");
    Node doBody = new Node(Token.BLOCK);
    Node doNode = new Node(Token.DO, doBody, doCond);

    generator.add(doNode);
    assertEquals("while(a){}do{}while(b);", consumer.getCode());
  }

  // Tests code generation for try-catch-finally
  @Test
  public void testAdd_tryCatchFinally_generatesTryCatchFinallyStatement() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, catchVar, catchBlock);
    Node catchBlockWrapper = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlockWrapper, finallyBlock);

    generator.add(tryNode);
    assertEquals("try{}catch(e){}finally{}", consumer.getCode());
  }

  // Tests code generation for switch case statements
  @Test
  public void testAdd_switchCaseNode_generatesSwitchStatement() {
    Node expr = Node.newString(Token.NAME, "val");
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.BREAK)));
    Node def = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, expr, case1, def);

    generator.add(switchNode);
    assertEquals("switch(val){case 1:break;default:}", consumer.getCode());
  }

  // Tests code generation for object literals with string and numeric keys
  @Test
  public void testAdd_objectLiteralNode_generatesObjectLiteral() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key1 = Node.newString(Token.STRING_KEY, "k1");
    key1.addChildToFront(Node.newNumber(1));
    Node key2 = Node.newString(Token.STRING_KEY, "k2");
    key2.addChildToFront(Node.newString("val"));
    objLit.addChildToBack(key1);
    objLit.addChildToBack(key2);

    generator.add(objLit);
    assertEquals("{k1:1,k2:\"val\"}", consumer.getCode());
  }

  // Tests code generation for member access and function calls (GETPROP, GETELEM, CALL, NEW)
  @Test
  public void testAdd_propertyAccessAndCallNodes_generatesExpressions() {
    Node target = Node.newString(Token.NAME, "obj");
    Node getProp = new Node(Token.GETPROP, target, Node.newString(Token.STRING, "prop"));
    Node getElem = new Node(Token.GETELEM, target, Node.newString("idx"));
    Node call = new Node(Token.CALL, getProp, Node.newNumber(1));
    Node newExpr = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));

    generator.add(call);
    generator.add(getElem);
    generator.add(newExpr);
    assertEquals("obj.prop(1)obj[\"idx\"]new Foo", consumer.getCode());
  }

  // Tests code generation for unary operators (NOT, TYPEOF, VOID, DELPROP, BITNOT)
  @Test
  public void testAdd_unaryOperators_generatesUnaryExpressions() {
    Node operand = Node.newString(Token.NAME, "x");
    Node notNode = new Node(Token.NOT, operand.cloneTree());
    Node typeofNode = new Node(Token.TYPEOF, operand.cloneTree());
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Node delNode = new Node(Token.DELPROP, operand.cloneTree());
    Node bitNotNode = new Node(Token.BITNOT, operand.cloneTree());

    generator.add(notNode);
    generator.add(typeofNode);
    generator.add(voidNode);
    generator.add(delNode);
    generator.add(bitNotNode);
    assertEquals("!xtypeof xvoid 0delete x~x", consumer.getCode());
  }

  // Tests code generation for primitive literal keywords (THIS, NULL, TRUE, FALSE, DEBUGGER)
  @Test
  public void testAdd_primitiveKeywords_generatesKeywordTokens() {
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.FALSE));
    generator.add(new Node(Token.DEBUGGER));
    assertEquals("thisnulltruefalsedebugger", consumer.getCode());
  }

  // Tests code generation for labeled statements and continue
  @Test
  public void testAdd_labeledStatementAndContinue_generatesLabelAndContinue() {
    Node labelName = Node.newString(Token.LABEL_NAME, "myLabel");
    Node body = new Node(Token.BLOCK, new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "myLabel")));
    Node labelNode = new Node(Token.LABEL, labelName, body);

    generator.add(labelNode);
    assertEquals("myLabel:{continue myLabel;}", consumer.getCode());
  }

  // Tests code generation for throw statement
  @Test
  public void testAdd_throwNode_generatesThrowStatement() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    generator.add(throwNode);
    assertEquals("throw err;", consumer.getCode());
  }
}
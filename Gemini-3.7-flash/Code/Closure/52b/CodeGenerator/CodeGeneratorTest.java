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
  private CodeGenerator codeGenerator;

  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    codeGenerator = new CodeGenerator(consumer);
  }

  // Tests leading zeros in isSimpleNumber which must return false to avoid octal ambiguity (Defects4J 52b)
  @Test
  public void testIsSimpleNumber_leadingZero_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("010"));
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
  }

  // Tests valid single zero in isSimpleNumber
  @Test
  public void testIsSimpleNumber_singleZero_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
  }

  // Tests positive integer strings in isSimpleNumber
  @Test
  public void testIsSimpleNumber_validNumbers_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertTrue(CodeGenerator.isSimpleNumber("42"));
    assertTrue(CodeGenerator.isSimpleNumber("1234567890"));
  }

  // Tests invalid non-numeric or empty strings in isSimpleNumber
  @Test
  public void testIsSimpleNumber_invalidStrings_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber(" 1"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
  }

  // Tests getSimpleNumber with leading zeros returns NaN (Defects4J 52b)
  @Test
  public void testGetSimpleNumber_leadingZero_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("01")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("00")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("0123")));
  }

  // Tests getSimpleNumber with valid numbers
  @Test
  public void testGetSimpleNumber_validIntegers_returnsCorrectValue() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);
    assertEquals(12345.0, CodeGenerator.getSimpleNumber("12345"), 0.0);
  }

  // Tests getSimpleNumber with overflow values and invalid formats
  @Test
  public void testGetSimpleNumber_overflowAndInvalid_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9999999999999999999999999999999999999999")));
  }

  // Tests string escaping preference for single quotes when double quotes are present
  @Test
  public void testJsString_preferSingleQuoteWhenDoubleQuotesPresent_returnsSingleQuoted() {
    assertEquals("'\"hello\"'", codeGenerator.jsString("\"hello\""));
  }

  // Tests string escaping preference for double quotes when single quotes are present
  @Test
  public void testJsString_preferDoubleQuoteWhenSingleQuotesPresent_returnsDoubleQuoted() {
    assertEquals("\"'hello'\"", codeGenerator.jsString("'hello'"));
  }

  // Tests escaping of control and special characters in strings
  @Test
  public void testJsString_escapeSpecialCharacters_returnsEscaped() {
    assertEquals("\"\\x00\\n\\r\\t\\\\\"", codeGenerator.jsString("\0\n\r\t\\"));
  }

  // Tests escaping HTML tags that could break inline script execution
  @Test
  public void testJsString_htmlScriptAndCommentTags_escapesTagStarts() {
    assertEquals("\"<\\/script>\"", codeGenerator.jsString("</script>"));
    assertEquals("\"<\\!--\"", codeGenerator.jsString("<!--"));
  }

  // Tests escaping comment and CDATA terminators
  @Test
  public void testJsString_htmlCommentAndCdataEnd_escapesEndings() {
    assertEquals("\"--\\>\"", codeGenerator.jsString("-->"));
    assertEquals("\"]]\\>\"", codeGenerator.jsString("]]>"));
  }

  // Tests regexpEscape on normal string
  @Test
  public void testRegexpEscape_simpleString_returnsSlashedRegex() {
    assertEquals("/abc/", CodeGenerator.regexpEscape("abc"));
  }

  // Tests regexpEscape on strings containing special tokens
  @Test
  public void testRegexpEscape_specialChars_escapesProperly() {
    assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
    assertEquals("/--\\>/", CodeGenerator.regexpEscape("-->"));
  }

  // Tests escapeToDoubleQuotedJsString escaping
  @Test
  public void testEscapeToDoubleQuotedJsString_escapesDoubleQuotes() {
    assertEquals("\"hello \\\"world\\\"\"", CodeGenerator.escapeToDoubleQuotedJsString("hello \"world\""));
  }

  // Tests identifierEscape with latin identifier
  @Test
  public void testIdentifierEscape_latin_returnsUnchanged() {
    assertEquals("myVar123$", CodeGenerator.identifierEscape("myVar123$"));
  }

  // Tests identifierEscape with non-latin characters
  @Test
  public void testIdentifierEscape_nonLatin_escapesUnicode() {
    assertEquals("\\u00f8", CodeGenerator.identifierEscape("\u00f8"));
  }

  // Tests tagAsStrict output
  @Test
  public void testTagAsStrict_invoked_addsStrictDirective() {
    codeGenerator.tagAsStrict();
    assertEquals("'use strict';", consumer.getOutput());
  }

  // Tests add method with NUMBER AST node
  @Test
  public void testAdd_numberNode_outputsNumber() {
    Node numNode = Node.newNumber(42.0);
    codeGenerator.add(numNode);
    assertEquals("42", consumer.getOutput());
  }

  // Tests add method with VAR AST node
  @Test
  public void testAdd_varNode_outputsVarStatement() {
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToBack(Node.newNumber(10));
    Node varNode = new Node(Token.VAR, nameNode);
    codeGenerator.add(varNode);
    assertEquals("var x=10", consumer.getOutput());
  }

  // Tests add method with ARRAYLIT AST node
  @Test
  public void testAdd_arrayLit_outputsArray() {
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    codeGenerator.add(array);
    assertEquals("[1,2]", consumer.getOutput());
  }

  // Tests add method with OBJECTLIT AST node
  @Test
  public void testAdd_objectLit_outputsObjectLiteral() {
    Node obj = new Node(Token.OBJECTLIT);
    Node key1 = Node.newString("a");
    key1.addChildToBack(Node.newNumber(1));
    obj.addChildToBack(key1);
    codeGenerator.add(obj);
    assertEquals("{a:1}", consumer.getOutput());
  }

  // Tests CodeGenerator constructor with Charsets.US_ASCII
  @Test
  public void testConstructor_withAsciiCharset_escapesNonAscii() {
    CodeGenerator asciiGenerator = new CodeGenerator(consumer, Charsets.US_ASCII);
    assertEquals("\"\\u00a9\"", asciiGenerator.jsString("\u00a9"));
  }

  @Test
  public void testJsString_lineAndParagraphSeparators_escapesProperly() {
    assertEquals("\"\\u2028\\u2029\"", codeGenerator.jsString("\u2028\u2029"));
  }

  @Test
  public void testJsString_controlChars_escapesProperly() {
    assertEquals("\"\\b\\f\\x0b\"", codeGenerator.jsString("\b\f\u000b"));
  }

  @Test
  public void testJsString_surrogatePair_escapesWithAsciiEncoder() {
    CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
    assertEquals("\"\\ud83d\\ude00\"", CodeGenerator.strEscape("\uD83D\uDE00", '"', "\\\"", "\'", "\\\\", asciiEncoder));
  }

  @Test
  public void testRegexpEscape_withCharsetEncoder() {
    CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
    assertEquals("/\\u00f8/", CodeGenerator.regexpEscape("\u00f8", asciiEncoder));
    assertEquals("/\\n/", CodeGenerator.regexpEscape("\n", asciiEncoder));
  }

  @Test
  public void testIdentifierEscape_nonLatinInitialChar() {
    assertEquals("\\u00e9var", CodeGenerator.identifierEscape("\u00e9var"));
  }

  @Test
  public void testAdd_functionAndReturn() {
    Node params = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "a"));
    Node body = new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "a")));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), params, body);
    codeGenerator.add(fn);
    assertEquals("function foo(a){return a}", consumer.getOutput());
  }

  @Test
  public void testAdd_callAndNew() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "fn"), Node.newNumber(1));
    codeGenerator.add(call);
    assertEquals("fn(1)", consumer.getOutput());

    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator g2 = new CodeGenerator(c2);
    Node newExpr = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(2));
    g2.add(newExpr);
    assertEquals("new Foo(2)", c2.getOutput());
  }

  @Test
  public void testAdd_ifElse() {
    Node ifNode = new Node(
        Token.IF,
        Node.newString(Token.NAME, "x"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")))
    );
    codeGenerator.add(ifNode);
    assertEquals("if(x){a}else{b}", consumer.getOutput());
  }

  @Test
  public void testAdd_whileAndDoWhile() {
    Node whileNode = new Node(
        Token.WHILE,
        Node.newString(Token.NAME, "x"),
        new Node(Token.BLOCK, new Node(Token.BREAK))
    );
    codeGenerator.add(whileNode);
    assertEquals("while(x){break}", consumer.getOutput());

    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator g2 = new CodeGenerator(c2);
    Node doNode = new Node(
        Token.DO,
        new Node(Token.BLOCK, new Node(Token.CONTINUE)),
        Node.newString(Token.NAME, "x")
    );
    g2.add(doNode);
    assertEquals("do{continue}while(x)", c2.getOutput());
  }

  @Test
  public void testAdd_forAndForIn() {
    Node forNode = new Node(
        Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(10)),
        new Node(Token.INC, Node.newString(Token.NAME, "i")),
        new Node(Token.BLOCK)
    );
    codeGenerator.add(forNode);
    assertEquals("for(var i;i<10;i++){}", consumer.getOutput());

    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator g2 = new CodeGenerator(c2);
    Node forIn = new Node(
        Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK)
    );
    g2.add(forIn);
    assertEquals("for(k in obj){}", c2.getOutput());
  }

  @Test
  public void testAdd_tryCatchFinally() {
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node blockCatch = new Node(Token.BLOCK, catchNode);
    Node tryBody = new Node(Token.BLOCK);
    Node finallyBody = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBody, blockCatch, finallyBody);
    codeGenerator.add(tryNode);
    assertEquals("try{}catch(e){}finally{}", consumer.getOutput());
  }

  @Test
  public void testAdd_switchCaseDefault() {
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.BREAK)));
    Node def = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, def);
    codeGenerator.add(switchNode);
    assertEquals("switch(x){case 1:break;default:}", consumer.getOutput());
  }

  @Test
  public void testAdd_unaryAndBinaryOperators() {
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node not = new Node(Token.NOT, add);
    Node typeofNode = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    Node comma = new Node(Token.COMMA, not, typeofNode);
    codeGenerator.add(comma);
    assertEquals("!(1+2),typeof x", consumer.getOutput());
  }

  @Test
  public void testAdd_hookTernaryAndPropAccess() {
    Node hook = new Node(
        Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b")),
        new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0))
    );
    codeGenerator.add(hook);
    assertEquals("cond?a.b:a[0]", consumer.getOutput());
  }

  @Test
  public void testAdd_literalsAndSpecialTokens() {
    Node script = new Node(
        Token.SCRIPT,
        new Node(Token.EXPR_RESULT, new Node(Token.TRUE)),
        new Node(Token.EXPR_RESULT, new Node(Token.FALSE)),
        new Node(Token.EXPR_RESULT, new Node(Token.NULL)),
        new Node(Token.EXPR_RESULT, new Node(Token.THIS)),
        new Node(Token.DEBUGGER)
    );
    codeGenerator.add(script);
    assertEquals("true;false;null;this;debugger", consumer.getOutput());
  }

  @Test
  public void testAdd_getterAndSetterInObjectLit() {
    Node getter = Node.newString(Token.GETTER_DEF, "x");
    getter.addChildToBack(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));

    Node setter = Node.newString(Token.SETTER_DEF, "y");
    setter.addChildToBack(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "v")), new Node(Token.BLOCK)));

    Node obj = new Node(Token.OBJECTLIT, getter, setter);
    codeGenerator.add(obj);
    assertEquals("{get x(){},set y(v){}}", consumer.getOutput());
  }

  @Test
  public void testAdd_labelAndThrow() {
    Node labeled = new Node(
        Token.LABEL,
        Node.newString(Token.NAME, "lbl"),
        new Node(Token.THROW, Node.newString(Token.NAME, "err"))
    );
    codeGenerator.add(labeled);
    assertEquals("lbl:throw err;", consumer.getOutput());
  }

  @Test
  public void testAdd_regexpNode() {
    Node regex = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("g"));
    codeGenerator.add(regex);
    assertEquals("/abc/g", consumer.getOutput());
  }
}
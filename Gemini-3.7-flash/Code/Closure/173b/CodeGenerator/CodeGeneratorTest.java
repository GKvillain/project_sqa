package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  private TestCodeConsumer consumer;
  private CodeGenerator codeGenerator;

  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    private char lastChar = '\0';

    @Override
    char getLastChar() {
      return lastChar;
    }

    @Override
    void append(String str) {
      buffer.append(str);
      if (str.length() > 0) {
        lastChar = str.charAt(str.length() - 1);
      }
    }

    String getCode() {
      return buffer.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    codeGenerator = CodeGenerator.forCostEstimation(consumer);
  }

  // Tests valid simple number formats
  @Test
  public void testIsSimpleNumber_validNumbers_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("123"));
    assertTrue(CodeGenerator.isSimpleNumber("99999"));
  }

  // Tests invalid simple number formats including empty string, non-digits, and leading zeros
  @Test
  public void testIsSimpleNumber_invalidFormats_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber(" 5"));
  }

  // Tests getSimpleNumber for valid numeric strings
  @Test
  public void testGetSimpleNumber_validSimpleNumber_returnsDoubleValue() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);
    assertEquals(1000.0, CodeGenerator.getSimpleNumber("1000"), 0.0);
  }

  // Tests getSimpleNumber for invalid formats or numbers exceeding MAX_POSITIVE_INTEGER_NUMBER
  @Test
  public void testGetSimpleNumber_invalidOrOverflow_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("05")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
  }

  // Tests Latin identifiers without needing unicode escaping
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsUnchanged() {
    assertEquals("variableName", CodeGenerator.identifierEscape("variableName"));
    assertEquals("_valid$id_123", CodeGenerator.identifierEscape("_valid$id_123"));
  }

  // Tests non-Latin identifiers escaping to unicode format
  @Test
  public void testIdentifierEscape_nonLatinCharacters_escapesToUnicode() {
    String escaped = CodeGenerator.identifierEscape("var\u1234name");
    assertTrue(escaped.contains("\\u1234"));
  }

  // Tests string escaping to double quoted JS string
  @Test
  public void testEscapeToDoubleQuotedJsString_controlAndQuoteChars_escapesProperly() {
    assertEquals("\"hello \\\"world\\\"\"", codeGenerator.escapeToDoubleQuotedJsString("hello \"world\""));
    assertEquals("\"line1\\nline2\\tline3\"", codeGenerator.escapeToDoubleQuotedJsString("line1\nline2\tline3"));
    assertEquals("\"backslash\\\\escaped\"", codeGenerator.escapeToDoubleQuotedJsString("backslash\\escaped"));
  }

  // Tests HTML sensitive tags in string escaping
  @Test
  public void testEscapeToDoubleQuotedJsString_htmlTags_escapesAngleBrackets() {
    String scriptTag = codeGenerator.escapeToDoubleQuotedJsString("</script>");
    assertTrue(scriptTag.contains("\\x3c"));

    String commentStart = codeGenerator.escapeToDoubleQuotedJsString("<!--");
    assertTrue(commentStart.contains("\\x3c"));

    String commentEnd = codeGenerator.escapeToDoubleQuotedJsString("-->");
    assertTrue(commentEnd.contains("\\x3e"));
  }

  // Tests regexp escaping without custom CharsetEncoder
  @Test
  public void testRegexpEscape_plainPattern_wrapsWithSlashes() {
    assertEquals("/abc[0-9]+/", codeGenerator.regexpEscape("abc[0-9]+"));
    assertEquals("/a\\nb/", codeGenerator.regexpEscape("a\nb"));
  }

  // Tests regexp escaping with explicit CharsetEncoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_encodesAppropriately() {
    CharsetEncoder encoder = Charsets.UTF_8.newEncoder();
    String escaped = codeGenerator.regexpEscape("test/pattern", encoder);
    assertEquals("/test/pattern/", escaped);
  }

  // Tests tagAsStrict adding the 'use strict' directive
  @Test
  public void testTagAsStrict_invoked_addsUseStrictStatement() {
    codeGenerator.tagAsStrict();
    assertEquals("'use strict';", consumer.getCode());
  }

  // Tests printing null, true, false, and this literal nodes
  @Test
  public void testAdd_literalNodes_printsExpectedConstants() {
    codeGenerator.add(new Node(Token.NULL));
    codeGenerator.add(new Node(Token.TRUE));
    codeGenerator.add(new Node(Token.FALSE));
    codeGenerator.add(new Node(Token.THIS));
    assertEquals("nulltruefalsethis", consumer.getCode());
  }

  // Tests number node output
  @Test
  public void testAdd_numberNode_printsCorrectNumber() {
    codeGenerator.add(Node.newNumber(123.45));
    assertEquals("123.45", consumer.getCode());
  }

  // Tests variable declaration node output
  @Test
  public void testAdd_varStatement_printsVariableDeclaration() {
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "myVar"));
    codeGenerator.add(varNode);
    assertEquals("var myVar", consumer.getCode());
  }

  // Tests ternary hook operator node output
  @Test
  public void testAdd_hookExpression_printsTernaryOperation() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    codeGenerator.add(hook);
    assertEquals("cond?a:b", consumer.getCode());
  }

  // Tests binary arithmetic operator associativity and precedence unrolling
  @Test
  public void testAdd_nestedBinaryOperators_printsAssociativeExpressions() {
    Node leftAdd = new Node(Token.ADD,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node rootAdd = new Node(Token.ADD,
        leftAdd,
        Node.newString(Token.NAME, "c"));
    codeGenerator.add(rootAdd);
    assertEquals("a+b+c", consumer.getCode());
  }

  // Tests binary logical OR operators associativity
  @Test
  public void testAdd_logicalOrOperators_printsCorrectPrecedence() {
    Node leftOr = new Node(Token.OR,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"));
    Node rootOr = new Node(Token.OR,
        leftOr,
        Node.newString(Token.NAME, "z"));
    codeGenerator.add(rootOr);
    assertEquals("x||y||z", consumer.getCode());
  }

  // Tests CompilerOptions configuration for single quote preference and untrusted strings
  @Test
  public void testCodeGenerator_compilerOptionsPreferences_appliesConfig() {
    CompilerOptions options = new CompilerOptions();
    options.preferSingleQuotes = true;
    options.trustedStrings = false;
    options.setLanguageOut(LanguageMode.ECMASCRIPT5);

    TestCodeConsumer customConsumer = new TestCodeConsumer();
    CodeGenerator customGenerator = new CodeGenerator(customConsumer, options);

    Node strNode = Node.newString("quote\"test");
    customGenerator.add(strNode);
    assertEquals("'quote\"test'", customConsumer.getCode());
  }

  // Tests try-catch-finally block construct
  @Test
  public void testAdd_tryCatchBlock_printsTryCatch() {
    Node tryBody = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlockWrapper = new Node(Token.BLOCK, catchNode);
    Node tryNode = new Node(Token.TRY, tryBody, catchBlockWrapper);

    codeGenerator.add(tryNode);
    assertEquals("try{}catch(e){}", consumer.getCode());
  }
}
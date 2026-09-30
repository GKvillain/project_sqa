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

  private StringBuilder buffer;
  private CodeConsumer consumer;

  @Before
  public void setUp() {
    buffer = new StringBuilder();
    consumer = new CodeConsumer() {
      @Override
      void append(String op) {
        buffer.append(op);
      }

      @Override
      char getLastChar() {
        return buffer.length() > 0 ? buffer.charAt(buffer.length() - 1) : '\0';
      }
    };
  }

  // Tests string escaping with fewer double quotes preferring single quotes
  @Test
  public void testJsString_moreDoubleQuotes_usesSingleQuotes() {
    String input = "Hello \"world\"!";
    String result = CodeGenerator.jsString(input, null);
    assertEquals("'Hello \"world\"!'", result);
  }

  // Tests string escaping with fewer single quotes preferring double quotes
  @Test
  public void testJsString_moreSingleQuotes_usesDoubleQuotes() {
    String input = "Hello 'world'!";
    String result = CodeGenerator.jsString(input, null);
    assertEquals("\"Hello 'world'!\"", result);
  }

  // Tests escaping standard control characters like newline, carriage return, tab, and backslash
  @Test
  public void testStrEscape_standardControlChars_escapesCorrectly() {
    String input = "Line1\nLine2\rLine3\tTab\\Backslash";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"Line1\\nLine2\\rLine3\\tTab\\\\Backslash\"", result);
  }

  // Tests escaping null character (zero character)
  @Test
  public void testStrEscape_zeroChar_escapesCorrectly() {
    String input = "pre\0post";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"pre\\u0000post\"", result);
  }

  // Tests escaping closing script tags to prevent XSS / parser breakout
  @Test
  public void testStrEscape_closingScriptTag_escapesSlash() {
    String input = "</script>";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"<\\/script>\"", result);
  }

  // Tests escaping HTML comment starts and ends
  @Test
  public void testStrEscape_htmlCommentsAndCdata_escapesCorrectly() {
    String inputComment = "<!-- comment -->";
    String resultComment = CodeGenerator.escapeToDoubleQuotedJsString(inputComment);
    assertEquals("\"<\\!-- comment --\\>\"", resultComment);

    String inputCdata = "]]>";
    String resultCdata = CodeGenerator.escapeToDoubleQuotedJsString(inputCdata);
    assertEquals("\"]]\\>\"", resultCdata);
  }

  // Tests regexp escape without CharsetEncoder
  @Test
  public void testRegexpEscape_simplePattern_escapesRegexDelimiters() {
    String input = "a/b/c";
    String result = CodeGenerator.regexpEscape(input);
    assertEquals("/a/b/c/", result);
  }

  // Tests regexp escape with CharsetEncoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_preservesEncodable() {
    CharsetEncoder encoder = Charsets.UTF_8.newEncoder();
    String input = "hello/world";
    String result = CodeGenerator.regexpEscape(input, encoder);
    assertEquals("/hello/world/", result);
  }

  // Tests identifier escaping with pure Latin characters
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsSameString() {
    String input = "myVariable_123$";
    String result = CodeGenerator.identifierEscape(input);
    assertEquals("myVariable_123$", result);
  }

  // Tests identifier escaping with non-Latin Unicode characters
  @Test
  public void testIdentifierEscape_nonLatinChars_escapesToHexRepresentation() {
    String input = "var_\u03b1";
    String result = CodeGenerator.identifierEscape(input);
    assertEquals("var_\\u03b1", result);
  }

  // Tests supplementary Unicode codepoint escaping (surrogate pairs)
  @Test
  public void testStrEscape_supplementaryCodePoint_escapesSurrogatePair() {
    // Codepoint U+1F600 (GRINNING FACE)
    String input = "\uD83D\uDE00";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"\\ud83d\\ude00\"", result);
  }

  // Tests escape with custom CharsetEncoder that cannot encode non-ascii
  @Test
  public void testStrEscape_nonAsciiWithAsciiEncoder_escapesUnicode() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String input = "caf\u00e9";
    String result = CodeGenerator.strEscape(input, '"', "\\\"", "'", "\\\\", asciiEncoder);
    assertEquals("\"caf\\u00e9\"", result);
  }

  // Tests tagAsStrict output
  @Test
  public void testTagAsStrict_addsStrictDirective() {
    CodeGenerator generator = new CodeGenerator(consumer);
    generator.tagAsStrict();
    assertEquals("'use strict';", buffer.toString());
  }

  // Tests adding simple expression node to CodeGenerator
  @Test
  public void testAdd_emptyNode_generatesEmpty() {
    CodeGenerator generator = new CodeGenerator(consumer);
    Node emptyNode = new Node(Token.EMPTY);
    generator.add(emptyNode);
    assertEquals("", buffer.toString());
  }

  // Tests adding null, this, true, false token nodes
  @Test
  public void testAdd_keywordLiterals_generatesKeywords() {
    CodeGenerator generator = new CodeGenerator(consumer);
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.FALSE));
    assertEquals("nullthistruefalse", buffer.toString());
  }

  // Tests escaping JS line and paragraph separators (\u2028 and \u2029)
  @Test
  public void testStrEscape_lineAndParagraphSeparators_escapedAsUnicode() {
    String input = "line1\u2028line2\u2029line3";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"line1\\u2028line2\\u2029line3\"", result);
  }

  // Tests escaping other control characters like backspace, formfeed, and vertical tab
  @Test
  public void testStrEscape_backspaceAndFormFeedAndVerticalTab() {
    String input = "a\bb\fc\u000bd";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"a\\bb\\fc\\x0bd\"", result);
  }

  // Tests regexpEscape with newline, carriage return, and special unicode line separators
  @Test
  public void testRegexpEscape_lineSeparatorsAndTags() {
    String input = "line1\nline2\rline3\u2028line4\u2029<!--</script>";
    String result = CodeGenerator.regexpEscape(input);
    assertEquals("/line1\\nline2\\rline3\\u2028line4\\u2029<\\!--<\\/script/", result);
  }

  // Tests regexpEscape with unencodable character via CharsetEncoder
  @Test
  public void testRegexpEscape_unencodableCharWithAsciiEncoder() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String input = "test\u00e9";
    String result = CodeGenerator.regexpEscape(input, asciiEncoder);
    assertEquals("/test\\u00e9/", result);
  }

  // Tests isSimpleNumber
  @Test
  public void testIsSimpleNumber() {
    assertTrue(CodeGenerator.isSimpleNumber("123"));
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("012"));
    assertFalse(CodeGenerator.isSimpleNumber("12a"));
  }

  // Tests isRepresentableAsInt
  @Test
  public void testIsRepresentableAsInt() {
    assertTrue(CodeGenerator.isRepresentableAsInt(0.0));
    assertTrue(CodeGenerator.isRepresentableAsInt(12345.0));
    assertTrue(CodeGenerator.isRepresentableAsInt(-12345.0));
    assertFalse(CodeGenerator.isRepresentableAsInt(1.5));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.NaN));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.POSITIVE_INFINITY));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.NEGATIVE_INFINITY));
  }

  // Tests addNumber with various values (integers, negative zero, scientific formatting)
  @Test
  public void testAddNumber_variousValues() {
    CodeGenerator generator = new CodeGenerator(consumer);

    generator.addNumber(0.0);
    assertEquals("0", buffer.toString());

    buffer.setLength(0);
    generator.addNumber(-0.0);
    assertEquals("-0.0", buffer.toString());

    buffer.setLength(0);
    generator.addNumber(1000000.0);
    assertEquals("1E6", buffer.toString());

    buffer.setLength(0);
    generator.addNumber(100.0);
    assertEquals("100", buffer.toString());

    buffer.setLength(0);
    generator.addNumber(0.00001);
    assertEquals("1E-5", buffer.toString());
  }

  // Tests CodeGenerator AST generation for binary operations, unary operations, and expressions
  @Test
  public void testAdd_binaryAndUnaryOperations() {
    CodeGenerator generator = new CodeGenerator(consumer);

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    generator.add(addNode);
    assertEquals("1+2", buffer.toString());

    buffer.setLength(0);
    Node negNode = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    generator.add(negNode);
    assertEquals("-x", buffer.toString());

    buffer.setLength(0);
    Node notNode = new Node(Token.NOT, Node.newString(Token.NAME, "y"));
    generator.add(notNode);
    assertEquals("!y", buffer.toString());
  }

  // Tests CodeGenerator AST generation for arrays, calls, and member expressions
  @Test
  public void testAdd_arrayCallAndProp() {
    CodeGenerator generator = new CodeGenerator(consumer);

    Node arrayNode = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    generator.add(arrayNode);
    assertEquals("[1,2]", buffer.toString());

    buffer.setLength(0);
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newNumber(10));
    generator.add(callNode);
    assertEquals("foo(10)", buffer.toString());

    buffer.setLength(0);
    Node getPropNode = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("prop"));
    generator.add(getPropNode);
    assertEquals("obj.prop", buffer.toString());
  }

  // Tests CodeGenerator AST generation for control structures
  @Test
  public void testAdd_controlStructures() {
    CodeGenerator generator = new CodeGenerator(consumer);

    Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
    generator.add(returnNode);
    assertEquals("return 1", buffer.toString());

    buffer.setLength(0);
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    generator.add(throwNode);
    assertEquals("throw err", buffer.toString());

    buffer.setLength(0);
    Node breakNode = new Node(Token.BREAK);
    generator.add(breakNode);
    assertEquals("break", buffer.toString());

    buffer.setLength(0);
    Node continueNode = new Node(Token.CONTINUE);
    generator.add(continueNode);
    assertEquals("continue", buffer.toString());
  }

  // Tests CodeGenerator AST generation for ternary operator (HOOK)
  @Test
  public void testAdd_hookExpression() {
    CodeGenerator generator = new CodeGenerator(consumer);

    Node hookNode = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newNumber(1),
        Node.newNumber(2));
    generator.add(hookNode);
    assertEquals("a?1:2", buffer.toString());
  }

  // Tests CodeGenerator forCostEstimation factory method
  @Test
  public void testForCostEstimation_createsFunctionalGenerator() {
    CodeGenerator costGen = CodeGenerator.forCostEstimation(consumer);
    costGen.add(Node.newNumber(42));
    assertEquals("42", buffer.toString());
  }
}
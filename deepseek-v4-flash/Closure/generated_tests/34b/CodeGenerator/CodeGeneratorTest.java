package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class CodeGeneratorTest {

  private TestCodeConsumer consumer;
  private CodeGenerator generator;

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    generator = new CodeGenerator(consumer);
  }

  // Helper methods to create test nodes
  private Node createNumberNode(double value) {
    Node n = new Node(Token.NUMBER);
    n.setDouble(value);
    return n;
  }

  private Node createStringNode(String value) {
    Node n = new Node(Token.STRING);
    n.setString(value);
    return n;
  }

  private Node createNameNode(String name) {
    Node n = new Node(Token.NAME);
    n.setString(name);
    return n;
  }

  private Node createLabelNameNode(String name) {
    Node n = new Node(Token.LABEL_NAME);
    n.setString(name);
    return n;
  }

  // Simple CodeConsumer implementation that records output
  private static class TestCodeConsumer extends CodeConsumer {
    StringBuilder sb = new StringBuilder();

    @Override
    public void add(String str) {
      sb.append(str);
    }

    @Override
    public void addIdentifier(String identifier) {
      sb.append(identifier);
    }

    @Override
    public void addOp(String op, boolean b) {
      sb.append(op);
    }

    @Override
    public void startSourceMapping(Node n) {}

    @Override
    public void endSourceMapping(Node n) {}

    @Override
    public void endStatement(boolean b) {
      sb.append(';');
    }

    @Override
    public void endStatement() {
      sb.append(';');
    }

    @Override
    public void beginBlock() {
      sb.append('{');
    }

    @Override
    public void endBlock(boolean b) {
      sb.append('}');
    }

    @Override
    public boolean breakAfterBlockFor(Node n, boolean b) {
      return false;
    }

    @Override
    public void maybeLineBreak() {}

    @Override
    public void notePreferredLineBreak() {}

    @Override
    public void listSeparator() {
      sb.append(',');
    }

    @Override
    public void beginCaseBody() {}

    @Override
    public void endCaseBody() {}

    @Override
    public void endFunction(boolean b) {
      if (b) {
        sb.append(';');
      }
    }

    @Override
    public boolean shouldPreserveExtraBlocks() {
      return false;
    }

    @Override
    public boolean continueProcessing() {
      return true;
    }
  }

  // Tests for static utility methods
  @Test
  public void testIsSimpleNumber_normal_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("123"));
    assertTrue(CodeGenerator.isSimpleNumber("0"));
  }

  @Test
  public void testIsSimpleNumber_leadingZero_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("012"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
  }

  @Test
  public void testIsSimpleNumber_empty_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
  }

  @Test
  public void testIsSimpleNumber_nonDigit_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("12a"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
  }

  @Test
  public void testGetSimpleNumber_normal_returnsNumber() {
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
  }

  @Test
  public void testGetSimpleNumber_tooLarge_returnsNaN() {
    // Exceeds MAX_POSITIVE_INTEGER_NUMBER (2^53)
    double result = CodeGenerator.getSimpleNumber("99999999999999999999999999");
    assertTrue(Double.isNaN(result));
  }

  @Test
  public void testGetSimpleNumber_nonSimple_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
  }

  @Test
  public void testIdentifierEscape_latin_returnsSame() {
    assertEquals("abc", CodeGenerator.identifierEscape("abc"));
    assertEquals("_foo", CodeGenerator.identifierEscape("_foo"));
  }

  @Test
  public void testIdentifierEscape_nonLatin_escapes() {
    // é (U+00E9) should be escaped
    String escaped = CodeGenerator.identifierEscape("a\u00E9b");
    assertTrue(escaped.contains("\\u00e9"));
    // Ensure original latin parts remain
    assertTrue(escaped.startsWith("a"));
    assertTrue(escaped.endsWith("b"));
  }

  @Test
  public void testRegexpEscape_basic() {
    String result = CodeGenerator.regexpEscape("hello");
    assertEquals("/hello/", result);
  }

  @Test
  public void testRegexpEscape_specialChars() {
    // newline, tab, backslash
    String result = CodeGenerator.regexpEscape("a\nb\tc\\d");
    assertEquals("/a\\nb\\tc\\\\d/", result);
  }

  @Test
  public void testRegexpEscape_xssSequence() {
    // break --> and ]]>
    String result1 = CodeGenerator.regexpEscape("a-->b");
    assertEquals("/a--\\>b/", result1);
    String result2 = CodeGenerator.regexpEscape("a]]>b");
    assertEquals("/a]]\\>b/", result2);
    // break </script (case-insensitive)
    String result3 = CodeGenerator.regexpEscape("a</script>b");
    // <\/script pattern
    assertEquals("/a<\\/script>b/", result3);
    // break <!--
    String result4 = CodeGenerator.regexpEscape("a<!--b");
    assertEquals("/a<\\!--b/", result4);
  }

  @Test
  public void testRegexpEscape_withCharsetEncoder() {
    Charset charset = Charset.forName("ISO-8859-1");
    CharsetEncoder encoder = charset.newEncoder();
    // non-latin character that is not in Latin1 will be escaped
    String result = CodeGenerator.regexpEscape("\u00E9", encoder);
    // é is in Latin1, should pass through
    assertEquals("/\u00E9/", result);
    // Greek letter alpha (U+03B1) not in Latin1 -> escaped
    String result2 = CodeGenerator.regexpEscape("\u03B1", encoder);
    assertTrue(result2.contains("\\u03b1"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    String input = "a'b\"c";
    String expected = "\"a'b\\\"c\"";
    assertEquals(expected, CodeGenerator.escapeToDoubleQuotedJsString(input));
  }

  // Tests for add(Node) with various token types
  @Test
  public void testAddNumber_positiveAndNegative() {
    generator.add(createNumberNode(42.0));
    assertEquals("42", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(createNumberNode(-5.0));
    assertEquals("-5", consumer.sb.toString());
  }

  @Test
  public void testAddString_simpleAndEscaped() {
    generator.add(createStringNode("hello"));
    assertEquals("\"hello\"", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(createStringNode("a\nb"));
    assertEquals("\"a\\nb\"", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(createStringNode("a\\b"));
    assertEquals("\"a\\\\b\"", consumer.sb.toString());
  }

  @Test
  public void testAddString_withDangerousSequences() {
    // test escaping of -->
    generator.add(createStringNode("a-->b"));
    assertEquals("\"a--\\>b\"", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(createStringNode("a</script>b"));
    assertEquals("\"a<\\/script>b\"", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(createStringNode("a<!--b"));
    assertEquals("\"a<\\!--b\"", consumer.sb.toString());
  }

  @Test
  public void testAddString_withSlashV() {
    Node n = createStringNode("a\u000Bb");
    n.putBooleanProp(Node.SLASH_V, true);
    generator.add(n);
    assertEquals("\"a\\vb\"", consumer.sb.toString());

    // without SLASH_V, should use \x0B
    consumer.sb.setLength(0);
    Node n2 = createStringNode("a\u000Bb");
    generator.add(n2);
    assertEquals("\"a\\x0Bb\"", consumer.sb.toString());
  }

  @Test
  public void testAddName_simpleAndWithInitializer() {
    generator.add(createNameNode("x"));
    assertEquals("x", consumer.sb.toString());

    // NAME with initializer: x = 1
    consumer.sb.setLength(0);
    Node nameNode = createNameNode("x");
    nameNode.addChildToFront(createNumberNode(1.0));
    generator.add(nameNode);
    assertEquals("x=1", consumer.sb.toString());
  }

  @Test
  public void testAddThisNullTrueFalse() {
    generator.add(new Node(Token.THIS));
    assertEquals("this", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(new Node(Token.NULL));
    assertEquals("null", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(new Node(Token.TRUE));
    assertEquals("true", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(new Node(Token.FALSE));
    assertEquals("false", consumer.sb.toString());
  }

  @Test
  public void testAddObjectLit_simple() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = createStringNode("a");
    key.addChildToFront(createNumberNode(1.0));
    objLit.addChildToFront(key);
    generator.add(objLit);
    assertEquals("{a:1}", consumer.sb.toString());
  }

  @Test
  public void testAddArrayLit_simple() {
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToFront(createNumberNode(1.0));
    arrayLit.addChildToBack(createNumberNode(2.0));
    generator.add(arrayLit);
    assertEquals("[1,2]", consumer.sb.toString());
  }

  @Test
  public void testAddFunction_named() {
    Node functionNode = new Node(Token.FUNCTION);
    // name: ""
    Node nameNode = createNameNode("f");
    functionNode.addChildToFront(nameNode);
    // params: empty
    Node params = new Node(Token.PARAM_LIST);
    functionNode.addChildToBack(params);
    // body: empty block
    Node body = new Node(Token.BLOCK);
    functionNode.addChildToBack(body);
    generator.add(functionNode);
    assertEquals("function f(){}", consumer.sb.toString());
  }

  @Test
  public void testAddCall_indirectEval() {
    Node evalNode = createNameNode("eval");
    // eval without DIRECT_EVAL property -> indirect
    Node callNode = new Node(Token.CALL, evalNode);
    generator.add(callNode);
    assertEquals("(0,eval)()", consumer.sb.toString());
  }

  @Test
  public void testAddIf_withElse() {
    Node ifNode = new Node(Token.IF);
    Node condition = new Node(Token.TRUE);
    ifNode.addChildToFront(condition);
    Node thenBlock = new Node(Token.BLOCK);
    ifNode.addChildToBack(thenBlock);
    Node elseBlock = new Node(Token.BLOCK);
    ifNode.addChildToBack(elseBlock);
    generator.add(ifNode);
    assertEquals("if(true){}else{}", consumer.sb.toString());
  }

  @Test
  public void testAddFor_empty() {
    Node forNode = new Node(Token.FOR);
    Node init = new Node(Token.EMPTY);
    forNode.addChildToFront(init);
    Node cond = new Node(Token.EMPTY);
    forNode.addChildToBack(cond);
    Node incr = new Node(Token.EMPTY);
    forNode.addChildToBack(incr);
    Node body = new Node(Token.BLOCK);
    forNode.addChildToBack(body);
    generator.add(forNode);
    assertEquals("for(;;){}", consumer.sb.toString());
  }

  @Test
  public void testAddWhile() {
    Node whileNode = new Node(Token.WHILE);
    Node condition = new Node(Token.TRUE);
    whileNode.addChildToFront(condition);
    Node body = new Node(Token.BLOCK);
    whileNode.addChildToBack(body);
    generator.add(whileNode);
    assertEquals("while(true){}", consumer.sb.toString());
  }

  @Test
  public void testAddThrowAndReturn() {
    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToFront(createStringNode("err"));
    generator.add(throwNode);
    assertEquals("throw\"err\";", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node returnNode = new Node(Token.RETURN);
    returnNode.addChildToFront(createNumberNode(1.0));
    generator.add(returnNode);
    assertEquals("return1;", consumer.sb.toString());
  }

  @Test
  public void testAddVar() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = createNameNode("a");
    nameNode.addChildToFront(createNumberNode(1.0));
    varNode.addChildToFront(nameNode);
    generator.add(varNode);
    assertEquals("var a=1", consumer.sb.toString());
  }

  @Test
  public void testAddGetProp_numberNeedsParens() {
    Node getProp = new Node(Token.GETPROP, createNumberNode(1.0), createStringNode("toString"));
    generator.add(getProp);
    assertEquals("(1).toString", consumer.sb.toString());
  }

  @Test
  public void testAddIncPreAndPost() {
    Node preInc = new Node(Token.INC, createNameNode("x"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0); // default pre
    generator.add(preInc);
    assertEquals("++x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node postInc = new Node(Token.INC, createNameNode("x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1); // post
    generator.add(postInc);
    assertEquals("x++", consumer.sb.toString());
  }

  @Test
  public void testAddHook() {
    Node hook = new Node(Token.HOOK);
    Node condition = new Node(Token.TRUE);
    hook.addChildToFront(condition);
    Node thenExpr = createNumberNode(1.0);
    hook.addChildToBack(thenExpr);
    Node elseExpr = createNumberNode(2.0);
    hook.addChildToBack(elseExpr);
    generator.add(hook);
    assertEquals("true?1:2", consumer.sb.toString());
  }
}
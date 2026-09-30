package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  private String printNode(Node n) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(false);
    builder.setLineBreak(false);
    return builder.build();
  }

  // Tests string escaping when string contains double quotes
  @Test
  public void testJsString_withDoubleQuotes_prefersSingleQuotes() {
    String input = "Hello \"World\"";
    String result = CodeGenerator.jsString(input, null);
    assertEquals("'Hello \"World\"'", result);
  }

  // Tests string escaping when string contains single quotes
  @Test
  public void testJsString_withSingleQuotes_prefersDoubleQuotes() {
    String input = "Don't do that";
    String result = CodeGenerator.jsString(input, null);
    assertEquals("\"Don't do that\"", result);
  }

  // Tests escaping of control characters in js string
  @Test
  public void testJsString_controlCharacters_escapedProperly() {
    String input = "Line1\nLine2\rTab\tBackslash\\";
    String result = CodeGenerator.jsString(input, null);
    assertEquals("\"Line1\\nLine2\\rTab\\tBackslash\\\\\"", result);
  }

  // Tests escaping of script tags and comment closers
  @Test
  public void testJsString_scriptAndCommentClosers_escapedProperly() {
    String scriptTag = "</script>";
    String htmlComment = "-->";
    String cdataClose = "]]>";

    assertEquals("\"<\\/script>\"", CodeGenerator.jsString(scriptTag, null));
    assertEquals("\"--\\>\"", CodeGenerator.jsString(htmlComment, null));
    assertEquals("\"]]\\>\"", CodeGenerator.jsString(cdataClose, null));
  }

  // Tests double-quoted JS string helper method
  @Test
  public void testEscapeToDoubleQuotedJsString_escapesQuotesAndChars() {
    String input = "a\"b\nc";
    String result = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"a\\\"b\\nc\"", result);
  }

  // Tests regexp escaping
  @Test
  public void testRegexpEscape_escapesSlashesAndScriptTags() {
    String input = "foo/bar</script>";
    String result = CodeGenerator.regexpEscape(input);
    assertEquals("/foo/bar<\\/script>/", result);
  }

  // Tests Latin identifier escaping returns unchanged
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsSameString() {
    String ident = "valid_Identifier$123";
    String result = CodeGenerator.identifierEscape(ident);
    assertEquals("valid_Identifier$123", result);
  }

  // Tests non-Latin identifier escaping converts to unicode hex
  @Test
  public void testIdentifierEscape_nonLatinIdentifier_escapesToHex() {
    String ident = "var\u00f8";
    String result = CodeGenerator.identifierEscape(ident);
    assertEquals("var\\u00f8", result);
  }

  // Tests outputCharsetEncoder behavior with UTF-8
  @Test
  public void testJsString_withCharsetEncoder_encodesSupportedChars() {
    CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
    String input = "\u00e9";
    String result = CodeGenerator.jsString(input, encoder);
    assertEquals("\"\u00e9\"", result);
  }

  // Tests ternary hook operator formatting
  @Test
  public void testAdd_hookOperator_formatsCorrectly() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    Node expr = new Node(Token.EXPR_RESULT, hook);

    assertEquals("a?b:c;", printNode(expr));
  }

  // Tests indirect eval preservation as (0,eval)
  @Test
  public void testAdd_indirectEval_preservesIndirectCall() {
    Node nameNode = Node.newString(Token.NAME, "eval");
    nameNode.putBooleanProp(Node.DIRECT_EVAL, false);
    Node call = new Node(Token.CALL,
        nameNode,
        Node.newString(Token.NAME, "x"));
    Node expr = new Node(Token.EXPR_RESULT, call);

    assertEquals("(0,eval)(x);", printNode(expr));
  }

  // Tests direct eval call
  @Test
  public void testAdd_directEval_outputsRegularCall() {
    Node nameNode = Node.newString(Token.NAME, "eval");
    nameNode.putBooleanProp(Node.DIRECT_EVAL, true);
    Node call = new Node(Token.CALL,
        nameNode,
        Node.newString(Token.NAME, "x"));
    Node expr = new Node(Token.EXPR_RESULT, call);

    assertEquals("eval(x);", printNode(expr));
  }

  // Tests object literal generation
  @Test
  public void testAdd_objectLiteral_formatsKeysAndValues() {
    Node obj = new Node(Token.OBJECTLIT,
        Node.newString(Token.STRING, "a"),
        Node.newNumber(1.0),
        Node.newString(Token.STRING, "default"),
        Node.newNumber(2.0));
    Node expr = new Node(Token.EXPR_RESULT, obj);

    assertEquals("({a:1,\"default\":2});", printNode(expr));
  }

  // Tests try catch finally block generation
  @Test
  public void testAdd_tryCatchFinally_formatsProperly() {
    Node tryBlock = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")));
    Node catchBody = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")));
    Node catchNode = new Node(Token.CATCH,
        Node.newString(Token.NAME, "e"),
        new Node(Token.EMPTY),
        catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c")));

    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    assertEquals("try{a;}catch(e){b;}finally{c;}", printNode(tryNode));
  }

  // Tests while and do-while loops
  @Test
  public void testAdd_doWhileLoop_formatsCorrectly() {
    Node body = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x")));
    Node doWhile = new Node(Token.DO,
        body,
        Node.newString(Token.NAME, "cond"));

    assertEquals("do{x;}while(cond);", printNode(doWhile));
  }

  // Tests if-else dangling else disambiguation
  @Test
  public void testAdd_ifElseAmbiguousDanglingElse_preservesBlock() {
    Node innerIf = new Node(Token.IF,
        Node.newString(Token.NAME, "b"),
        new Node(Token.BLOCK,
            new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c"))));
    Node outerIf = new Node(Token.IF,
        Node.newString(Token.NAME, "a"),
        new Node(Token.BLOCK, innerIf),
        new Node(Token.BLOCK,
            new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "d"))));

    assertEquals("if(a){if(b)c;}else d;", printNode(outerIf));
  }

  // Tests that labeled FUNCTION inside an IF block preserves the block wrapper (Defects4J Closure-145)
  @Test
  public void testAdd_labeledFunctionInIfBlock_preservesBlock() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "l1"),
        fn);
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "x"),
        new Node(Token.BLOCK, label));

    String code = printNode(ifNode);
    assertTrue("Block around labeled function in IF must be preserved",
        code.equals("if(x){l1:function foo(){}}") || code.contains("{l1:function foo()}"));
  }

  // Tests that labeled DO loop inside an IF block preserves the block wrapper (Defects4J Closure-145)
  @Test
  public void testAdd_labeledDoInIfBlock_preservesBlock() {
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK),
        Node.newString(Token.NAME, "cond"));
    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "l1"),
        doNode);
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "x"),
        new Node(Token.BLOCK, label));

    String code = printNode(ifNode);
    assertTrue("Block around labeled DO in IF must be preserved",
        code.equals("if(x){l1:do;while(cond);}") || code.contains("{l1:do;while(cond);}"));
  }
}
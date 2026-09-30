package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class CodeGeneratorTest {

  private SimpleCodeConsumer consumer;
  private CompilerOptions options;
  private CodeGenerator generator;

  private static class SimpleCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    private char lastChar = '\0';

    @Override
    void append(String str) {
      buffer.append(str);
      if (str.length() > 0) {
        lastChar = str.charAt(str.length() - 1);
      }
    }

    @Override
    char getLastChar() {
      return lastChar;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new SimpleCodeConsumer();
    options = new CompilerOptions();
    generator = new CodeGenerator(consumer, options);
  }

  // Tests isSimpleNumber with valid numbers
  @Test
  public void testIsSimpleNumber_validNumbers_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
  }

  // Tests isSimpleNumber with invalid strings (empty, leading zero, non-digits)
  @Test
  public void testIsSimpleNumber_invalidStrings_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("abc"));
    assertFalse(CodeGenerator.isSimpleNumber("12a34"));
    assertFalse(CodeGenerator.isSimpleNumber("-5"));
  }

  // Tests getSimpleNumber parsing valid and invalid strings
  @Test
  public void testGetSimpleNumber_variousInputs_returnsParsedNumberOrNaN() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
  }

  // Tests identifierEscape with latin and non-latin characters
  @Test
  public void testIdentifierEscape_latinAndNonLatin_escapesCorrectly() {
    assertEquals("validIdent", CodeGenerator.identifierEscape("validIdent"));
    String escaped = CodeGenerator.identifierEscape("foo\u00A9bar");
    assertTrue(escaped.startsWith("foo\\u"));
    assertTrue(escaped.endsWith("bar"));
  }

  // Tests escapeToDoubleQuotedJsString with special characters
  @Test
  public void testEscapeToDoubleQuotedJsString_specialCharacters_escapedProperly() {
    String input = "Hello \"World\"\n\r\t\\ \u2028\u2029";
    String escaped = generator.escapeToDoubleQuotedJsString(input);
    assertTrue(escaped.startsWith("\""));
    assertTrue(escaped.endsWith("\""));
    assertTrue(escaped.contains("\\\""));
    assertTrue(escaped.contains("\\n"));
    assertTrue(escaped.contains("\\r"));
    assertTrue(escaped.contains("\\t"));
    assertTrue(escaped.contains("\\\\"));
    assertTrue(escaped.contains("\\u2028"));
    assertTrue(escaped.contains("\\u2029"));
  }

  // Tests regexpEscape with script tags and comments
  @Test
  public void testRegexpEscape_dangerousStrings_escapedProperly() {
    String escapedScript = generator.regexpEscape("</script>");
    assertTrue(escapedScript.contains("\\x3c"));

    String escapedComment = generator.regexpEscape("<!--");
    assertTrue(escapedComment.contains("\\x3c"));

    String escapedEndComment = generator.regexpEscape("-->");
    assertTrue(escapedEndComment.contains("\\x3e"));

    String escapedCData = generator.regexpEscape("]]>");
    assertTrue(escapedCData.contains("\\x3e"));
  }

  // Tests regexpEscape with custom CharsetEncoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_escapesNonEncodable() {
    String escaped = generator.regexpEscape("foo\u00ffbar", Charset.forName("US-ASCII").newEncoder());
    assertTrue(escaped.contains("\\u00ff"));
  }

  // Tests tagAsStrict output
  @Test
  public void testTagAsStrict_appendsStrictDirective() {
    generator.tagAsStrict();
    assertEquals("'use strict';", consumer.getCode());
  }

  // Tests forCostEstimation factory method
  @Test
  public void testForCostEstimation_returnsValidInstance() {
    CodeGenerator costGen = CodeGenerator.forCostEstimation(consumer);
    assertNotNull(costGen);
  }

  // Tests hook (ternary) in for-loop initializer containing 'in' operator (Defects4J Bug 123)
  @Test
  public void testAdd_hookWithInOperatorInForInitClause_parenthesizesInExpression() {
    Node lhs = Node.newString("a");
    Node rhs = Node.newString("b");
    Node inNode = new Node(Token.IN, lhs, rhs);
    Node hook = new Node(Token.HOOK, new Node(Token.TRUE), inNode, Node.newNumber(1.0));
    Node forNode = new Node(Token.FOR, hook, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));

    generator.add(forNode);
    String code = consumer.getCode();
    assertTrue("Should parenthesize 'in' inside ternary when in for-init: " + code,
        code.contains("(a in b)"));
  }

  // Tests hook (ternary) in third child with 'in' operator in for-loop init
  @Test
  public void testAdd_hookWithInInElseBranchInForInitClause_parenthesizesInExpression() {
    Node lhs = Node.newString("a");
    Node rhs = Node.newString("b");
    Node inNode = new Node(Token.IN, lhs, rhs);
    Node hook = new Node(Token.HOOK, new Node(Token.TRUE), Node.newNumber(1.0), inNode);
    Node forNode = new Node(Token.FOR, hook, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));

    generator.add(forNode);
    String code = consumer.getCode();
    assertTrue("Should parenthesize 'in' inside ternary else-branch when in for-init: " + code,
        code.contains("(a in b)"));
  }

  // Tests binary operators and precedence
  @Test
  public void testAdd_binaryOperators_preservesAssociativityAndPrecedence() {
    Node a = Node.newString("a");
    Node b = Node.newString("b");
    Node c = Node.newString("c");
    Node addInner = new Node(Token.ADD, b, c);
    Node addOuter = new Node(Token.ADD, a, addInner);

    generator.add(addOuter);
    assertEquals("a+b+c", consumer.getCode());
  }

  // Tests unary operators: NOT, VOID, NEG
  @Test
  public void testAdd_unaryOperators_generatesCorrectPrefix() {
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    generator.add(notNode);
    assertEquals("!true", consumer.getCode());

    SimpleCodeConsumer consumerNeg = new SimpleCodeConsumer();
    CodeGenerator genNeg = new CodeGenerator(consumerNeg, options);
    Node negNode = new Node(Token.NEG, Node.newNumber(5.0));
    genNeg.add(negNode);
    assertEquals("-5", consumerNeg.getCode());
  }

  // Tests try-catch-finally block generation
  @Test
  public void testAdd_tryCatchFinally_generatesCompleteStructure() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString("e"), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    generator.add(tryNode);
    String code = consumer.getCode();
    assertTrue(code.contains("try"));
    assertTrue(code.contains("catch(e)"));
    assertTrue(code.contains("finally"));
  }

  // Tests if-else statement generation
  @Test
  public void testAdd_ifElseStatement_generatesIfAndElseBlocks() {
    Node cond = new Node(Token.TRUE);
    Node thenBlock = new Node(Token.BLOCK);
    Node elseBlock = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);

    generator.add(ifNode);
    String code = consumer.getCode();
    assertTrue(code.startsWith("if(true)"));
    assertTrue(code.contains("else"));
  }

  // Tests array literal and object literal generation
  @Test
  public void testAdd_arrayAndObjectLiterals_generatesCorrectSyntax() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1.0), Node.newNumber(2.0));
    generator.add(arrayLit);
    assertEquals("[1,2]", consumer.getCode());

    SimpleCodeConsumer objConsumer = new SimpleCodeConsumer();
    CodeGenerator objGen = new CodeGenerator(objConsumer, options);
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("x");
    key.setType(Token.STRING_KEY);
    key.addChildToFront(Node.newNumber(10.0));
    objLit.addChildToFront(key);

    objGen.add(objLit);
    assertEquals("{x:10}", objConsumer.getCode());
  }

  // Tests switch-case statements
  @Test
  public void testAdd_switchCase_generatesSwitchCaseStructure() {
    Node switchVal = Node.newString("x");
    Node caseCond = Node.newNumber(1.0);
    Node caseBody = new Node(Token.BLOCK);
    Node caseNode = new Node(Token.CASE, caseCond, caseBody);
    Node switchNode = new Node(Token.SWITCH, switchVal, caseNode);

    generator.add(switchNode);
    String code = consumer.getCode();
    assertTrue(code.startsWith("switch(x)"));
    assertTrue(code.contains("case 1:"));
  }

  // Tests function expression and return statement
  @Test
  public void testAdd_functionAndReturn_generatesFunctionStructure() {
    Node fnName = Node.newString("myFunc");
    Node paramList = new Node(Token.PARAM_LIST);
    Node returnNode = new Node(Token.RETURN, Node.newNumber(42.0));
    Node body = new Node(Token.BLOCK, returnNode);
    Node fnNode = new Node(Token.FUNCTION, fnName, paramList, body);

    generator.add(fnNode);
    String code = consumer.getCode();
    assertTrue(code.contains("function myFunc()"));
    assertTrue(code.contains("return 42"));
  }

  // Tests while and do-while loops
  @Test
  public void testAdd_whileAndDoWhile_generatesLoopSyntax() {
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    generator.add(whileNode);
    assertEquals("while(true);", consumer.getCode());

    SimpleCodeConsumer doConsumer = new SimpleCodeConsumer();
    CodeGenerator doGen = new CodeGenerator(doConsumer, options);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.FALSE));
    doGen.add(doNode);
    assertEquals("do;while(false);", doConsumer.getCode());
  }

  // Tests simple constants: null, this, false, true, debugger
  @Test
  public void testAdd_constantsAndKeywords_generatesCorrectTokens() {
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.FALSE));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.DEBUGGER));
    assertEquals("nullthisfalsetruedebugger;", consumer.getCode());
  }

  // Tests variable declarations: VAR and CONST
  @Test
  public void testAdd_varAndConstDeclarations_generatesCorrectDeclarations() {
    Node varName = Node.newString("a");
    varName.addChildToFront(Node.newNumber(1.0));
    Node varNode = new Node(Token.VAR, varName);
    generator.add(varNode);
    assertEquals("var a=1;", consumer.getCode());

    SimpleCodeConsumer constConsumer = new SimpleCodeConsumer();
    CodeGenerator constGen = new CodeGenerator(constConsumer, options);
    Node constName = Node.newString("b");
    constName.addChildToFront(Node.newNumber(2.0));
    Node constNode = new Node(Token.CONST, constName);
    constGen.add(constNode);
    assertEquals("const b=2;", constConsumer.getCode());
  }

  // Tests function call and new expressions
  @Test
  public void testAdd_callAndNewExpressions_generatesCorrectInvocationSyntax() {
    Node callNode = new Node(Token.CALL, Node.newString("foo"), Node.newNumber(1.0), Node.newNumber(2.0));
    generator.add(callNode);
    assertEquals("foo(1,2)", consumer.getCode());

    SimpleCodeConsumer newConsumer = new SimpleCodeConsumer();
    CodeGenerator newGen = new CodeGenerator(newConsumer, options);
    Node newNode = new Node(Token.NEW, Node.newString("Bar"), Node.newString(Token.STRING, "arg"));
    newGen.add(newNode);
    assertEquals("new Bar(\"arg\")", newConsumer.getCode());
  }

  // Tests property and element access: GETPROP and GETELEM
  @Test
  public void testAdd_getpropAndGetelem_generatesCorrectAccessSyntax() {
    Node propNode = Node.newString(Token.STRING, "bar");
    Node getProp = new Node(Token.GETPROP, Node.newString("foo"), propNode);
    generator.add(getProp);
    assertEquals("foo.bar", consumer.getCode());

    SimpleCodeConsumer elemConsumer = new SimpleCodeConsumer();
    CodeGenerator elemGen = new CodeGenerator(elemConsumer, options);
    Node getElem = new Node(Token.GETELEM, Node.newString("arr"), Node.newNumber(0.0));
    elemGen.add(getElem);
    assertEquals("arr[0]", elemConsumer.getCode());
  }

  // Tests prefix and postfix INC/DEC operators
  @Test
  public void testAdd_incrementAndDecrement_generatesPrefixAndPostfixSyntax() {
    Node prefixInc = new Node(Token.INC, Node.newString("x"));
    generator.add(prefixInc);
    assertEquals("++x", consumer.getCode());

    SimpleCodeConsumer postConsumer = new SimpleCodeConsumer();
    CodeGenerator postGen = new CodeGenerator(postConsumer, options);
    Node postfixDec = new Node(Token.DEC, Node.newString("y"));
    postfixDec.putBooleanProp(Node.INCRDECR_PROP, true);
    postGen.add(postfixDec);
    assertEquals("y--", postConsumer.getCode());
  }

  // Tests control flow: with, label, break, continue, throw
  @Test
  public void testAdd_controlFlowStatements_generatesCorrectStatements() {
    Node withNode = new Node(Token.WITH, Node.newString("o"), new Node(Token.BLOCK));
    generator.add(withNode);

    Node labelNode = new Node(Token.LABEL, Node.newString(Token.LABEL_NAME, "loop"), new Node(Token.BLOCK));
    generator.add(labelNode);

    Node breakNode = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop"));
    generator.add(breakNode);

    Node contNode = new Node(Token.CONTINUE);
    generator.add(contNode);

    Node throwNode = new Node(Token.THROW, Node.newString(Token.STRING, "error"));
    generator.add(throwNode);

    String code = consumer.getCode();
    assertTrue(code.contains("with(o);"));
    assertTrue(code.contains("loop:;"));
    assertTrue(code.contains("break loop;"));
    assertTrue(code.contains("continue;"));
    assertTrue(code.contains("throw\"error\";"));
  }

  // Tests compound assignments and bitwise/logical operators
  @Test
  public void testAdd_compoundAssignAndLogicalOperators_generatesCorrectTokens() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString("x"), Node.newNumber(5.0));
    generator.add(assignAdd);

    Node andNode = new Node(Token.AND, Node.newString("a"), Node.newString("b"));
    generator.add(andNode);

    Node orNode = new Node(Token.OR, Node.newString("c"), Node.newString("d"));
    generator.add(orNode);

    Node bitNotNode = new Node(Token.BITNOT, Node.newString("e"));
    generator.add(bitNotNode);

    Node commaNode = new Node(Token.COMMA, Node.newString("f"), Node.newString("g"));
    generator.add(commaNode);

    String code = consumer.getCode();
    assertTrue(code.contains("x+=5"));
    assertTrue(code.contains("a&&b"));
    assertTrue(code.contains("c||d"));
    assertTrue(code.contains("~e"));
    assertTrue(code.contains("f,g"));
  }

  // Tests for-in loop generation
  @Test
  public void testAdd_forInLoop_generatesForInSyntax() {
    Node varNode = new Node(Token.VAR, Node.newString("k"));
    Node objNode = Node.newString("obj");
    Node forInNode = new Node(Token.FOR, varNode, objNode, new Node(Token.BLOCK));

    generator.add(forInNode);
    assertEquals("for(var k in obj);", consumer.getCode());
  }

  // Tests getter and setter definitions in object literals
  @Test
  public void testAdd_getterAndSetterInObjectLit_generatesGetSetSyntax() {
    Node objLit = new Node(Token.OBJECTLIT);

    Node getterKey = Node.newString(Token.GETTER_DEF, "val");
    Node getterFn = new Node(Token.FUNCTION, Node.newString(""), new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1.0))));
    getterKey.addChildToFront(getterFn);
    objLit.addChildToFront(getterKey);

    Node setterKey = Node.newString(Token.SETTER_DEF, "val");
    Node setterFn = new Node(Token.FUNCTION, Node.newString(""),
        new Node(Token.PARAM_LIST, Node.newString("v")), new Node(Token.BLOCK));
    setterKey.addChildToFront(setterFn);
    objLit.addChildToFront(setterKey);

    generator.add(objLit);
    String code = consumer.getCode();
    assertTrue(code.contains("get val()"));
    assertTrue(code.contains("set val(v)"));
  }

  // Tests switch statement default case
  @Test
  public void testAdd_switchWithDefaultCase_generatesDefaultLabel() {
    Node defaultCase = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString("x"), defaultCase);

    generator.add(switchNode);
    String code = consumer.getCode();
    assertTrue(code.contains("default:"));
  }

  // Tests special number formatting: negative zero, NaN, infinities, exponents
  @Test
  public void testAdd_specialNumbers_generatesCorrectRepresentations() {
    generator.add(Node.newNumber(-0.0));
    generator.add(Node.newNumber(Double.NaN));
    generator.add(Node.newNumber(Double.POSITIVE_INFINITY));
    generator.add(Node.newNumber(Double.NEGATIVE_INFINITY));
    generator.add(Node.newNumber(1000000.0));
    generator.add(Node.newNumber(0.00001));

    String code = consumer.getCode();
    assertTrue(code.contains("-0"));
    assertTrue(code.contains("NaN") || code.contains("0/0"));
    assertTrue(code.contains("Infinity") || code.contains("1/0"));
  }

  // Tests preferSingleQuotes option in string escaping
  @Test
  public void testEscape_preferSingleQuotes_usesSingleQuotesWhenConfigured() {
    CompilerOptions singleQuoteOptions = new CompilerOptions();
    singleQuoteOptions.setPreferSingleQuotes(true);
    SimpleCodeConsumer sqConsumer = new SimpleCodeConsumer();
    CodeGenerator sqGen = new CodeGenerator(sqConsumer, singleQuoteOptions);

    Node strNode = Node.newString(Token.STRING, "hello 'world'");
    sqGen.add(strNode);
    String code = sqConsumer.getCode();
    assertTrue(code.startsWith("'") || code.startsWith("\""));
  }

  // Tests typeof and delete operators
  @Test
  public void testAdd_typeofAndDelete_generatesCorrectPrefix() {
    Node typeofNode = new Node(Token.TYPEOF, Node.newString("x"));
    generator.add(typeofNode);
    assertEquals("typeof x", consumer.getCode());

    SimpleCodeConsumer delConsumer = new SimpleCodeConsumer();
    CodeGenerator delGen = new CodeGenerator(delConsumer, options);
    Node delNode = new Node(Token.DELPROP, Node.newString("x"));
    delGen.add(delNode);
    assertEquals("delete x", delConsumer.getCode());
  }

  // Tests cast and expr_result nodes
  @Test
  public void testAdd_castAndExprResult_generatesExpectedCode() {
    Node castNode = new Node(Token.CAST, Node.newNumber(1.0));
    generator.add(castNode);

    SimpleCodeConsumer exprConsumer = new SimpleCodeConsumer();
    CodeGenerator exprGen = new CodeGenerator(exprConsumer, options);
    Node exprResult = new Node(Token.EXPR_RESULT, Node.newNumber(2.0));
    exprGen.add(exprResult);
    assertEquals("2;", exprConsumer.getCode());
  }
}
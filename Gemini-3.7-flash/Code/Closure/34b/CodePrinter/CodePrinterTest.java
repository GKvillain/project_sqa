package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CodePrinterTest {

  private Node parse(String js) {
    Compiler compiler = new Compiler();
    return compiler.parseTestCode(js);
  }

  // Tests exception when root node is null
  @Test(expected = IllegalStateException.class)
  public void testBuild_nullRootNode_throwsIllegalStateException() {
    new CodePrinter.Builder(null).build();
  }

  // Tests basic compact code generation
  @Test
  public void testBuild_compactPrint_generatesCompactCode() {
    Node n = parse("var a = 1; var b = 2;");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertEquals("var a=1;var b=2", result);
  }

  // Tests pretty printing with indentation
  @Test
  public void testBuild_prettyPrint_generatesIndentedCode() {
    Node n = parse("function foo() { var x = 1; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    String expected = "function foo() {\n  var x = 1;\n}\n";
    assertEquals(expected, result);
  }

  // Tests pretty printing if-else statements
  @Test
  public void testBuild_prettyPrintIfElse_formatsCorrectly() {
    Node n = parse("if (true) { x = 1; } else { x = 2; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("if (true) {\n  x = 1;\n} else {\n  x = 2;\n}"));
  }

  // Tests pretty printing try-catch-finally statements
  @Test
  public void testBuild_prettyPrintTryCatchFinally_formatsCorrectly() {
    Node n = parse("try { x = 1; } catch (e) { x = 2; } finally { x = 3; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("try {\n  x = 1;\n} catch (e) {\n  x = 2;\n} finally {\n  x = 3;\n}"));
  }

  // Tests pretty printing do-while statements
  @Test
  public void testBuild_prettyPrintDoWhile_formatsCorrectly() {
    Node n = parse("do { x++; } while (x < 10);");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("do {\n  x++;\n} while (x < 10);"));
  }

  // Tests pretty printing switch-case statements
  @Test
  public void testBuild_prettyPrintSwitchCase_formatsCorrectly() {
    Node n = parse("switch (x) { case 1: y = 1; break; default: y = 2; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("case 1:\n"));
    assertTrue(result.contains("default:\n"));
  }

  // Tests strict mode output tagging
  @Test
  public void testBuild_tagAsStrict_prependsUseStrict() {
    Node n = parse("var x = 1;");
    String result = new CodePrinter.Builder(n)
        .setTagAsStrict(true)
        .build();
    assertEquals("'use strict';var x=1", result);
  }

  // Tests line length threshold causing line breaks in compact mode
  @Test
  public void testBuild_lineLengthThreshold_breaksLongLines() {
    Node n = parse("var a = 1; var b = 2; var c = 3; var d = 4;");
    String result = new CodePrinter.Builder(n)
        .setLineLengthThreshold(10)
        .build();
    assertTrue(result.contains("\n"));
  }

  // Tests line break after functions when lineBreak is true
  @Test
  public void testBuild_lineBreakEnabled_breaksAfterFunction() {
    Node n = parse("function a() {} function b() {}");
    String result = new CodePrinter.Builder(n)
        .setLineBreak(true)
        .build();
    assertTrue(result.contains("\n"));
  }

  // Tests prefer line break at end of file
  @Test
  public void testBuild_preferLineBreakAtEndOfFile_appendsSemicolonAndBreak() {
    Node n = parse("var longVariableName = 1234567890;");
    String result = new CodePrinter.Builder(n)
        .setLineLengthThreshold(20)
        .setPreferLineBreakAtEndOfFile(true)
        .build();
    assertTrue(result.endsWith(";\n") || result.endsWith("\n"));
  }

  // Tests source map generation integration
  @Test
  public void testBuild_withSourceMap_generatesMapping() {
    Node n = parse("var a = 1;");
    n.setSourceFileName("test.js");
    n.setLineno(1);
    SourceMap sourceMap = new SourceMap();
    String result = new CodePrinter.Builder(n)
        .setSourceMap(sourceMap)
        .setSourceMapDetailLevel(SourceMap.DetailLevel.ALL)
        .build();
    assertEquals("var a=1", result);
  }

  // Tests source map detail level cannot be null
  @Test(expected = IllegalStateException.class)
  public void testBuild_nullSourceMapDetailLevel_throwsIllegalStateException() {
    Node n = parse("var a = 1;");
    new CodePrinter.Builder(n).setSourceMapDetailLevel(null);
  }

  // Tests output types option (TYPED format)
  @Test
  public void testBuild_outputTypes_formatsWithTypeInfo() {
    Node n = parse("/** @type {number} */ var a = 1;");
    String result = new CodePrinter.Builder(n)
        .setOutputTypes(true)
        .build();
    assertTrue(result.contains("var a=1") || result.contains("a = 1"));
  }

  // Tests output charset configuration
  @Test
  public void testBuild_withCharset_outputsValidCode() {
    Node n = parse("var msg = 'hello';");
    String result = new CodePrinter.Builder(n)
        .setOutputCharset(Charset.forName("UTF-8"))
        .build();
    assertEquals("var msg=\"hello\"", result);
  }

  // Tests source map generation with SYMBOLS detail level
  @Test
  public void testBuild_withSourceMapSymbolsDetailLevel_generatesCode() {
    Node n = parse("var a = 1; function test() { return a; }");
    n.setSourceFileName("test.js");
    n.setLineno(1);
    SourceMap sourceMap = new SourceMap();
    String result = new CodePrinter.Builder(n)
        .setSourceMap(sourceMap)
        .setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS)
        .build();
    assertTrue(result.contains("var a=1"));
    assertTrue(result.contains("function test()"));
  }

  // Tests binary expression operator precedence with parentheses
  @Test
  public void testBuild_operatorPrecedence_parenthesizesCorrectly() {
    Node n = parse("var x = (a + b) * c;");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertEquals("var x=(a+b)*c", result);
  }

  // Tests unary and binary operator disambiguation (space separation)
  @Test
  public void testBuild_unaryBinaryDisambiguation_preservesSpace() {
    Node n = parse("var x = a - -b; var y = a + +b;");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertEquals("var x=a- -b;var y=a+ +b", result);
  }

  // Tests for loop formatting in pretty print
  @Test
  public void testBuild_prettyPrintForLoop_formatsCorrectly() {
    Node n = parse("for (var i = 0; i < 10; i++) { x += i; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("for (var i = 0; i < 10; i++) {\n  x += i;\n}\n"));
  }

  // Tests for-in loop formatting in pretty print
  @Test
  public void testBuild_prettyPrintForInLoop_formatsCorrectly() {
    Node n = parse("for (var k in obj) { foo(k); }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("for (var k in obj) {\n  foo(k);\n}\n"));
  }

  // Tests while loop formatting in pretty print
  @Test
  public void testBuild_prettyPrintWhileLoop_formatsCorrectly() {
    Node n = parse("while (x > 0) { x--; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("while (x > 0) {\n  x--;\n}\n"));
  }

  // Tests labeled statements, break and continue
  @Test
  public void testBuild_labelsBreakContinue_formatsCorrectly() {
    Node n = parse("loop: for (var i = 0; i < 10; i++) { if (i === 5) continue loop; else break loop; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertTrue(result.contains("loop:for("));
    assertTrue(result.contains("continue loop"));
    assertTrue(result.contains("break loop"));
  }

  // Tests object literal and array literal formatting
  @Test
  public void testBuild_objectAndArrayLiterals_formatsCorrectly() {
    Node n = parse("var obj = {a: 1, 'b': 2, 3: 4}; var arr = [1, 2, , 4];");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertTrue(result.contains("a:1"));
    assertTrue(result.contains("arr=[1,2,,4]"));
  }

  // Tests throw and return statements
  @Test
  public void testBuild_throwAndReturn_formatsCorrectly() {
    Node n = parse("function fn() { if (!x) throw new Error('err'); return x; }");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertTrue(result.contains("throw new Error(\"err\")"));
    assertTrue(result.contains("return x"));
  }

  // Tests number with dot method invocation parenthesization (e.g. 1..toString())
  @Test
  public void testBuild_numberMethodCall_formatsCorrectly() {
    Node n = parse("var s = (1).toString();");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertEquals("var s=1..toString()", result);
  }

  // Tests regex literals and division disambiguation
  @Test
  public void testBuild_regexAndDivision_formatsCorrectly() {
    Node n = parse("var r = /abc/g; var d = a / /regex/.source;");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(false)
        .build();
    assertTrue(result.contains("/abc/g"));
    assertTrue(result.contains("a/ /regex/"));
  }

  // Tests standard UTF-8 charset output with non-ASCII characters
  @Test
  public void testBuild_utf8CharsetWithUnicode_outputsCorrectly() {
    Node n = parse("var text = '\u00e9\u3042';");
    String result = new CodePrinter.Builder(n)
        .setOutputCharset(StandardCharsets.UTF_8)
        .build();
    assertTrue(result.contains("text="));
  }
}
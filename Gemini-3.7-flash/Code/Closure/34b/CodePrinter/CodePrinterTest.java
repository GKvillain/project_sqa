package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CodePrinterTest {

  private Node parse(String js) {
    Compiler compiler = new Compiler();
    return compiler.parseTestCode(js);
  }

  // Tests null root node throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testBuild_nullRootNode_throwsIllegalStateException() {
    new CodePrinter.Builder(null).build();
  }

  // Tests null source map detail level throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetSourceMapDetailLevel_nullLevel_throwsIllegalStateException() {
    Node n = parse("var x = 1;");
    new CodePrinter.Builder(n).setSourceMapDetailLevel(null);
  }

  // Tests default compact printing for basic statement
  @Test
  public void testBuild_compactFormat_returnsCompactCode() {
    Node n = parse("var x = 1;");
    String result = new CodePrinter.Builder(n).build();
    assertEquals("var x=1", result);
  }

  // Tests pretty printing output with proper indentation
  @Test
  public void testBuild_prettyPrint_returnsFormattedCode() {
    Node n = parse("function foo() { var x = 1; return x; }");
    String result = new CodePrinter.Builder(n).setPrettyPrint(true).build();
    assertTrue(result.contains("function foo() {\n"));
    assertTrue(result.contains("  var x = 1;\n"));
    assertTrue(result.contains("  return x;\n"));
    assertTrue(result.contains("}\n"));
  }

  // Tests pretty printing with switch-case constructs
  @Test
  public void testBuild_prettyPrintSwitchCase_indentsCaseBody() {
    Node n = parse("switch (x) { case 1: y = 2; break; default: y = 3; }");
    String result = new CodePrinter.Builder(n).setPrettyPrint(true).build();
    assertTrue(result.contains("case 1:\n"));
    assertTrue(result.contains("default:\n"));
  }

  // Tests pretty printing block breaks for if-else statements
  @Test
  public void testBuild_prettyPrintIfElse_formatsBlocksCorrectly() {
    Node n = parse("if (a) { b(); } else { c(); }");
    String result = new CodePrinter.Builder(n).setPrettyPrint(true).build();
    assertTrue(result.contains("if (a) {\n  b();\n} else {\n  c();\n}"));
  }

  // Tests pretty printing block breaks for try-catch-finally
  @Test
  public void testBuild_prettyPrintTryCatchFinally_formatsBlocksCorrectly() {
    Node n = parse("try { a(); } catch (e) { b(); } finally { c(); }");
    String result = new CodePrinter.Builder(n).setPrettyPrint(true).build();
    assertTrue(result.contains("try {\n  a();\n} catch (e) {\n  b();\n} finally {\n  c();\n}"));
  }

  // Tests pretty printing block breaks for do-while loops
  @Test
  public void testBuild_prettyPrintDoWhile_doesNotBreakBeforeWhile() {
    Node n = parse("do { a(); } while (true);");
    String result = new CodePrinter.Builder(n).setPrettyPrint(true).build();
    assertTrue(result.contains("do {\n  a();\n} while (true);"));
  }

  // Tests line breaking when line length exceeds threshold in compact mode
  @Test
  public void testBuild_compactLineLengthThreshold_breaksLine() {
    Node n = parse("var a = 1; var b = 2; var c = 3; var d = 4;");
    String result = new CodePrinter.Builder(n)
        .setLineLengthThreshold(10)
        .build();
    assertTrue(result.contains("\n"));
  }

  // Tests compact printer with preferLineBreakAtEndOfFile for long line
  @Test
  public void testBuild_preferLineBreakAtEndOfFile_longLine_appendsBreak() {
    Node n = parse("var a = 1; var b = 2; var c = 3;");
    String result = new CodePrinter.Builder(n)
        .setLineLengthThreshold(10)
        .setPreferLineBreakAtEndOfFile(true)
        .build();
    assertTrue(result.endsWith("\n"));
  }

  // Tests compact printer with lineBreak option and function definitions
  @Test
  public void testBuild_compactWithLineBreak_breaksAfterFunction() {
    Node n = parse("function a() {} function b() {}");
    String result = new CodePrinter.Builder(n)
        .setLineBreak(true)
        .build();
    assertTrue(result.contains("\n"));
  }

  // Tests strict mode output tagging
  @Test
  public void testBuild_tagAsStrict_prependsUseStrict() {
    Node n = parse("var x = 1;");
    String result = new CodePrinter.Builder(n)
        .setTagAsStrict(true)
        .build();
    assertTrue(result.startsWith("'use strict';") || result.startsWith("\"use strict\";"));
  }

  // Tests output types mode using TypedCodeGenerator
  @Test
  public void testBuild_outputTypes_generatesTypedOutput() {
    Node n = parse("var x = 1;");
    String result = new CodePrinter.Builder(n)
        .setOutputTypes(true)
        .build();
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  // Tests custom output charset
  @Test
  public void testBuild_outputCharset_usesSpecifiedCharset() {
    Node n = parse("var x = 'hello';");
    String result = new CodePrinter.Builder(n)
        .setOutputCharset(Charset.forName("UTF-8"))
        .build();
    assertTrue(result.contains("hello"));
  }

  // Tests source map generation with valid mapping positions
  @Test
  public void testBuild_withSourceMap_populatesSourceMap() {
    Node n = parse("var x = 1; var y = 2;");
    SourceMap sourceMap = Mockito.mock(SourceMap.class);
    String result = new CodePrinter.Builder(n)
        .setSourceMap(sourceMap)
        .setSourceMapDetailLevel(SourceMap.DetailLevel.ALL)
        .build();
    assertNotNull(result);
  }

  // Tests binary and unary operators formatting in pretty printing
  @Test
  public void testBuild_prettyPrintOperators_formatsWithSpaces() {
    Node n = parse("var x = 1 + 2 * 3; var y = -x;");
    String result = new CodePrinter.Builder(n)
        .setPrettyPrint(true)
        .build();
    assertTrue(result.contains("1 + 2 * 3"));
    assertTrue(result.contains("-x"));
  }
}
package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class JsAstTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests constructor initialization of source file and input id
  @Test
  public void testConstructor_validSourceFile_initializesCorrectly() {
    SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(sourceFile);

    assertEquals(new InputId("test.js"), ast.getInputId());
    assertSame(sourceFile, ast.getSourceFile());
  }

  // Tests getAstRoot with valid JavaScript code
  @Test
  public void testGetAstRoot_validCode_returnsParsedRoot() {
    SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(sourceFile);

    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("test.js"), root.getInputId());
    assertSame(sourceFile, root.getStaticSourceFile());
  }

  // Tests that getAstRoot caches the parsed AST and returns the same instance
  @Test
  public void testGetAstRoot_multipleCalls_returnsCachedInstance() {
    SourceFile sourceFile = SourceFile.fromCode("test.js", "function foo() {}");
    JsAst ast = new JsAst(sourceFile);

    Node firstRoot = ast.getAstRoot(compiler);
    Node secondRoot = ast.getAstRoot(compiler);

    assertSame(firstRoot, secondRoot);
  }

  // Tests getAstRoot with empty JavaScript code
  @Test
  public void testGetAstRoot_emptyCode_returnsScriptNode() {
    SourceFile sourceFile = SourceFile.fromCode("empty.js", "");
    JsAst ast = new JsAst(sourceFile);

    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("empty.js"), root.getInputId());
  }

  // Tests getAstRoot when syntax error occurs
  @Test
  public void testGetAstRoot_syntaxError_returnsFallbackScriptNode() {
    SourceFile sourceFile = SourceFile.fromCode("invalid.js", "var = ;");
    JsAst ast = new JsAst(sourceFile);

    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("invalid.js"), root.getInputId());
    assertSame(sourceFile, root.getStaticSourceFile());
  }

  // Tests clearAst resets the cached AST and allows re-parsing
  @Test
  public void testClearAst_afterParsing_resetsAndReparsesAst() {
    SourceFile sourceFile = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(sourceFile);

    Node firstRoot = ast.getAstRoot(compiler);
    ast.clearAst();
    Node secondRoot = ast.getAstRoot(compiler);

    assertNotNull(secondRoot);
    assertNotSame(firstRoot, secondRoot);
    assertEquals(new InputId("test.js"), secondRoot.getInputId());
  }

  // Tests clearAst when AST has not been parsed yet
  @Test
  public void testClearAst_beforeParsing_doesNotThrow() {
    SourceFile sourceFile = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(sourceFile);

    ast.clearAst();

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
  }

  // Tests setSourceFile with a matching file name
  @Test
  public void testSetSourceFile_matchingName_updatesSourceFile() {
    SourceFile sourceFile1 = SourceFile.fromCode("test.js", "var a = 1;");
    SourceFile sourceFile2 = SourceFile.fromCode("test.js", "var a = 2;");
    JsAst ast = new JsAst(sourceFile1);

    ast.setSourceFile(sourceFile2);

    assertSame(sourceFile2, ast.getSourceFile());
  }

  // Tests setSourceFile with a mismatched file name throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetSourceFile_mismatchedName_throwsException() {
    SourceFile sourceFile1 = SourceFile.fromCode("test1.js", "var a = 1;");
    SourceFile sourceFile2 = SourceFile.fromCode("test2.js", "var a = 2;");
    JsAst ast = new JsAst(sourceFile1);

    ast.setSourceFile(sourceFile2);
  }

  // Tests getAstRoot when an IOException occurs during source reading
  @Test
  public void testGetAstRoot_ioExceptionOnRead_returnsFallbackScriptNodeAndReportsError() {
    File nonExistentFile = new File("/non/existent/path/io_error.js");
    SourceFile sourceFile = SourceFile.fromFile(nonExistentFile);
    JsAst ast = new JsAst(sourceFile);

    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId(nonExistentFile.getPath()), root.getInputId());
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests setSourceFile followed by clearAst and re-parsing with new source file
  @Test
  public void testSetSourceFile_andReparse_usesUpdatedSourceFile() {
    SourceFile sourceFile1 = SourceFile.fromCode("test.js", "var a = 1;");
    SourceFile sourceFile2 = SourceFile.fromCode("test.js", "var b = 2;");
    JsAst ast = new JsAst(sourceFile1);

    Node firstRoot = ast.getAstRoot(compiler);
    ast.clearAst();
    ast.setSourceFile(sourceFile2);
    Node secondRoot = ast.getAstRoot(compiler);

    assertNotNull(secondRoot);
    assertNotSame(firstRoot, secondRoot);
    assertSame(sourceFile2, ast.getSourceFile());
    assertSame(sourceFile2, secondRoot.getStaticSourceFile());
  }

  // Tests getAstRoot with JSDoc comments to verify JSDoc attached to parsed nodes
  @Test
  public void testGetAstRoot_withJsDoc_parsesCommentsCorrectly() {
    SourceFile sourceFile = SourceFile.fromCode("jsdoc.js", "/** @type {number} */ var x = 10;");
    JsAst ast = new JsAst(sourceFile);

    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.hasChildren());
    assertNotNull(root.getFirstChild().getJSDocInfo());
  }
}
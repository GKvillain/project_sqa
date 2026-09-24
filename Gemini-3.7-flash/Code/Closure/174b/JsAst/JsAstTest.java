package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
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

  // Tests getInputId returns InputId matching source file name
  @Test
  public void testGetInputId_validSourceFile_returnsMatchingInputId() {
    SourceFile file = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(file);
    assertEquals(new InputId("test.js"), ast.getInputId());
  }

  // Tests getSourceFile returns the initial SourceFile
  @Test
  public void testGetSourceFile_validSourceFile_returnsSourceFile() {
    SourceFile file = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(file);
    assertSame(file, ast.getSourceFile());
  }

  // Tests setSourceFile with matching filename updates source file
  @Test
  public void testSetSourceFile_sameFileName_updatesSourceFile() {
    SourceFile file1 = SourceFile.fromCode("test.js", "var a = 1;");
    SourceFile file2 = SourceFile.fromCode("test.js", "var b = 2;");
    JsAst ast = new JsAst(file1);
    ast.setSourceFile(file2);
    assertSame(file2, ast.getSourceFile());
  }

  // Tests setSourceFile with different filename throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetSourceFile_differentFileName_throwsIllegalStateException() {
    SourceFile file1 = SourceFile.fromCode("test1.js", "var a = 1;");
    SourceFile file2 = SourceFile.fromCode("test2.js", "var b = 2;");
    JsAst ast = new JsAst(file1);
    ast.setSourceFile(file2);
  }

  // Tests getAstRoot parses valid JS code and sets root properties
  @Test
  public void testGetAstRoot_validCode_returnsScriptNode() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(file);
    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("test.js"), root.getInputId());
    assertSame(file, root.getStaticSourceFile());
  }

  // Tests getAstRoot caches the parsed AST root on subsequent calls
  @Test
  public void testGetAstRoot_multipleCalls_returnsCachedRoot() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(file);
    Node root1 = ast.getAstRoot(compiler);
    Node root2 = ast.getAstRoot(compiler);

    assertSame(root1, root2);
  }

  // Tests getAstRoot on empty source code
  @Test
  public void testGetAstRoot_emptyCode_returnsScriptNode() {
    SourceFile file = SourceFile.fromCode("empty.js", "");
    JsAst ast = new JsAst(file);
    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("empty.js"), root.getInputId());
  }

  // Tests getAstRoot on invalid JS code returns dummy script root
  @Test
  public void testGetAstRoot_syntaxError_returnsDummyScriptNode() {
    SourceFile file = SourceFile.fromCode("invalid.js", "var x = ;");
    JsAst ast = new JsAst(file);
    Node root = ast.getAstRoot(compiler);

    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(new InputId("invalid.js"), root.getInputId());
  }

  // Tests clearAst resets root and allows reparsing
  @Test
  public void testClearAst_afterParsing_clearsRootAndAllowsReparsing() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(file);
    Node root1 = ast.getAstRoot(compiler);
    assertNotNull(root1);

    ast.clearAst();
    Node root2 = ast.getAstRoot(compiler);
    assertNotNull(root2);
    assertNotSame(root1, root2);
  }

  // Tests setSourceFile after AST has been parsed updates the static source file on the root node
  @Test
  public void testSetSourceFile_afterAstParsed_updatesRootStaticSourceFile() {
    SourceFile file1 = SourceFile.fromCode("test.js", "var a = 1;");
    SourceFile file2 = SourceFile.fromCode("test.js", "var b = 2;");
    JsAst ast = new JsAst(file1);
    Node root = ast.getAstRoot(compiler);
    assertSame(file1, root.getStaticSourceFile());

    ast.setSourceFile(file2);
    assertSame(file2, ast.getSourceFile());
    assertSame(file2, root.getStaticSourceFile());
  }

  // Tests clearAst before parsing does not throw exceptions
  @Test
  public void testClearAst_beforeParsing_doesNotThrow() {
    SourceFile file = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(file);
    ast.clearAst();
    assertSame(file, ast.getSourceFile());
  }
}
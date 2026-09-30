package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ProcessCommonJSModulesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node createScriptNode(String filename, String code) {
    CompilerInput input = new CompilerInput(SourceFile.fromCode(filename, code));
    return input.getAstRoot(compiler);
  }

  // Tests static toModuleName with standard filename
  @Test
  public void testToModuleName_standardFilename_convertsCorrectly() {
    String actual = ProcessCommonJSModules.toModuleName("foo/bar.js");
    assertEquals("module$foo$bar", actual);
  }

  // Tests static toModuleName with leading relative path ./
  @Test
  public void testToModuleName_leadingDotSlash_removesLeadingDot() {
    String actual = ProcessCommonJSModules.toModuleName("." + File.separator + "foo" + File.separator + "bar.js");
    assertEquals("module$foo$bar", actual);
  }

  // Tests static toModuleName with dashes replaced by underscores
  @Test
  public void testToModuleName_dashesInName_replacesWithUnderscores() {
    String actual = ProcessCommonJSModules.toModuleName("foo-bar-baz.js");
    assertEquals("module$foo_bar_baz", actual);
  }

  // Tests static toModuleName without .js extension
  @Test
  public void testToModuleName_withoutJsExtension_convertsCorrectly() {
    String actual = ProcessCommonJSModules.toModuleName("foo/bar");
    assertEquals("module$foo$bar", actual);
  }

  // Tests relative toModuleName with same directory relative require
  @Test
  public void testToModuleName_relativeSameDir_resolvesCorrectly() {
    String actual = ProcessCommonJSModules.toModuleName("." + File.separator + "bar.js", "foo" + File.separator + "baz.js");
    assertEquals("module$foo$bar", actual);
  }

  // Tests relative toModuleName with parent directory relative require
  @Test
  public void testToModuleName_relativeParentDir_resolvesCorrectly() {
    String actual = ProcessCommonJSModules.toModuleName(".." + File.separator + "bar.js", "foo" + File.separator + "baz.js");
    assertEquals("module$bar", actual);
  }

  // Tests relative toModuleName with non-relative target
  @Test
  public void testToModuleName_nonRelativeRequired_convertsDirectly() {
    String actual = ProcessCommonJSModules.toModuleName("other" + File.separator + "module.js", "foo" + File.separator + "baz.js");
    assertEquals("module$other$module", actual);
  }

  // Tests static toModuleName with nested relative paths
  @Test
  public void testToModuleName_nestedRelativePath_resolvesCorrectly() {
    String actual = ProcessCommonJSModules.toModuleName("." + File.separator + "sub" + File.separator + "target.js", "base" + File.separator + "current.js");
    assertEquals("module$base$sub$target", actual);
  }

  // Tests guessCJSModuleName with filename prefix removal
  @Test
  public void testGuessCJSModuleName_withPrefix_normalizesAndConverts() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "base" + File.separator + "path");
    String actual = processor.guessCJSModuleName("base" + File.separator + "path" + File.separator + "module.js");
    assertEquals("module$module", actual);
  }

  // Tests guessCJSModuleName without prefix match
  @Test
  public void testGuessCJSModuleName_withoutMatchingPrefix_convertsFullPath() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "other" + File.separator);
    String actual = processor.guessCJSModuleName("base" + File.separator + "module.js");
    assertEquals("module$base$module", actual);
  }

  // Tests guessCJSModuleName with prefix ending with slash
  @Test
  public void testGuessCJSModuleName_prefixWithTrailingSlash_normalizesProperly() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "base" + File.separator + "path" + File.separator);
    String actual = processor.guessCJSModuleName("base" + File.separator + "path" + File.separator + "sub" + File.separator + "module.js");
    assertEquals("module$sub$module", actual);
  }

  // Tests process on a simple script without require or module.exports
  @Test
  public void testProcess_simpleScript_generatesProvideAndVar() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".");
    String code = "var x = 10;";
    Node script = createScriptNode("test.js", code);
    Node root = new Node(Token.ROOT, script);

    processor.process(null, root);

    assertNotNull(processor.getModule());
    assertEquals("module$test", processor.getModule().getName());
  }

  // Tests process with require call rewriting
  @Test
  public void testProcess_requireCall_rewritesToModuleRef() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".");
    String code = "var bar = require('./bar');";
    Node script = createScriptNode("foo.js", code);
    Node root = new Node(Token.ROOT, script);

    processor.process(null, root);

    assertNotNull(processor.getModule());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests process with module.exports rewriting
  @Test
  public void testProcess_moduleExports_rewritesExportAssignment() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".");
    String code = "var a = 1; module.exports = a;";
    Node script = createScriptNode("foo.js", code);
    Node root = new Node(Token.ROOT, script);

    processor.process(null, root);

    assertNotNull(processor.getModule());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests process with exports property assignments
  @Test
  public void testProcess_exportsProperty_rewritesPropertyAssignment() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".");
    String code = "exports.foo = 1; exports.bar = 2;";
    Node script = createScriptNode("mod.js", code);
    Node root = new Node(Token.ROOT, script);

    processor.process(null, root);

    assertNotNull(processor.getModule());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests process with reportDependencies set to false
  @Test
  public void testProcess_reportDependenciesFalse_moduleIsNull() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".", false);
    String code = "var bar = require('./bar'); module.exports = bar;";
    Node script = createScriptNode("foo.js", code);
    Node root = new Node(Token.ROOT, script);

    processor.process(null, root);

    assertNull(processor.getModule());
  }

  // Tests process throws exception if multiple script nodes are traversed
  @Test(expected = RuntimeException.class)
  public void testProcess_multipleScriptNodes_throwsException() {
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, ".");
    Node script1 = createScriptNode("foo1.js", "var a = 1;");
    Node script2 = createScriptNode("foo2.js", "var b = 2;");
    Node root = new Node(Token.ROOT, script1, script2);

    processor.process(null, root);
  }
}
package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

public class ProcessCommonJSModulesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests simple module name conversion with .js extension
  @Test
  public void testToModuleName_simpleName_addsPrefixAndRemovesExtension() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
  }

  // Tests module name conversion with leading ./
  @Test
  public void testToModuleName_leadingDotSlash_removesLeadingDotSlash() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo.js"));
  }

  // Tests module name conversion with slash hierarchy
  @Test
  public void testToModuleName_nestedPath_replacesSlashWithDollar() {
    assertEquals("module$foo$bar$baz", ProcessCommonJSModules.toModuleName("foo/bar/baz.js"));
  }

  // Tests module name conversion with hyphen characters
  @Test
  public void testToModuleName_hyphens_replacesWithUnderscore() {
    assertEquals("module$foo_bar_baz", ProcessCommonJSModules.toModuleName("foo-bar-baz.js"));
  }

  // Tests module name conversion with combined slashes, hyphens, and dot-slash
  @Test
  public void testToModuleName_complexPath_normalizesAllSpecialChars() {
    assertEquals("module$foo_bar$baz_qux", ProcessCommonJSModules.toModuleName("./foo-bar/baz-qux.js"));
  }

  // Tests relative path resolution in current directory
  @Test
  public void testToModuleName_relativeCurrentDirectory_resolvesCorrectly() {
    String resolved = ProcessCommonJSModules.toModuleName("./bar", "foo/index.js");
    assertEquals("module$foo$bar", resolved);
  }

  // Tests relative path resolution with parent directory traversal
  @Test
  public void testToModuleName_parentDirectoryTraversal_resolvesCorrectly() {
    String resolved = ProcessCommonJSModules.toModuleName("../bar", "foo/baz/index.js");
    assertEquals("module$foo$bar", resolved);
  }

  // Tests non-relative required module name resolution
  @Test
  public void testToModuleName_nonRelativeRequiredName_preservesModuleName() {
    String resolved = ProcessCommonJSModules.toModuleName("external-module", "foo/index.js");
    assertEquals("module$external_module", resolved);
  }

  // Tests guessCJSModuleName with matching filename prefix
  @Test
  public void testGuessCJSModuleName_withMatchingPrefix_stripsPrefix() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "root/base/");
    assertEquals("module$app", pass.guessCJSModuleName("root/base/app.js"));
  }

  // Tests guessCJSModuleName when prefix lacks trailing slash
  @Test
  public void testGuessCJSModuleName_prefixWithoutTrailingSlash_addsSlashAndStrips() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "root/base");
    assertEquals("module$app", pass.guessCJSModuleName("root/base/app.js"));
  }

  // Tests guessCJSModuleName when filename does not start with prefix
  @Test
  public void testGuessCJSModuleName_unmatchedPrefix_keepsFullPath() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "other/base/");
    assertEquals("module$root$base$app", pass.guessCJSModuleName("root/base/app.js"));
  }

  // Tests getModule returns null before processing
  @Test
  public void testGetModule_beforeProcess_returnsNull() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./");
    assertNull(pass.getModule());
  }

  // Tests process pass on simple script creating module definition
  @Test
  public void testProcess_simpleScript_createsModuleAndProvides() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", true);
    Node root = compiler.parseSyntheticCode("test.js", "var x = 10;");
    pass.process(null, root);

    assertNotNull(pass.getModule());
    assertEquals("module$test", pass.getModule().getName());
  }

  // Tests process pass with reportDependencies disabled
  @Test
  public void testProcess_withoutReportingDependencies_moduleIsNull() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", false);
    Node root = compiler.parseSyntheticCode("test.js", "var x = 10;");
    pass.process(null, root);

    assertNull(pass.getModule());
  }

  // Tests process pass with require call rewriting
  @Test
  public void testProcess_requireCall_rewritesRequireToModuleReference() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", true);
    Node root = compiler.parseSyntheticCode("main.js", "var mod = require('./foo');");
    pass.process(null, root);

    String js = compiler.toSource(root);
    assertTrue(js.contains("goog.provide(\"module$main\")"));
    assertTrue(js.contains("goog.require(\"module$foo\")"));
    assertTrue(js.contains("module$foo"));
  }

  // Tests process pass with module.exports rewriting
  @Test
  public void testProcess_moduleExports_emitsExportOverride() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", true);
    Node root = compiler.parseSyntheticCode("foo.js", "module.exports = { k: 1 };");
    pass.process(null, root);

    String js = compiler.toSource(root);
    assertTrue(js.contains("goog.provide(\"module$foo\")"));
    assertTrue(js.contains("module$foo.module$exports"));
  }

  // Tests process pass renaming global variables to avoid conflicts
  @Test
  public void testProcess_globalVariables_renamesWithModuleSuffix() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", true);
    Node root = compiler.parseSyntheticCode("mod.js", "var myVar = 1; exports.foo = myVar;");
    pass.process(null, root);

    String js = compiler.toSource(root);
    assertTrue(js.contains("myVar$$module$mod"));
  }
}
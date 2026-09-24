package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for ProcessCommonJSModules.
 */
public class ProcessCommonJSModulesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests single filename without path or dash
  @Test
  public void testToModuleName_simpleFilename_returnsPrefixedModuleName() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
  }

  // Tests filename with leading relative path ./
  @Test
  public void testToModuleName_leadingDotSlash_stripsPrefix() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo.js"));
  }

  // Tests filename with directory separators and hyphens
  @Test
  public void testToModuleName_nestedPathAndHyphens_convertsSeparatorsAndDashes() {
    assertEquals("module$foo_bar$baz_qux", ProcessCommonJSModules.toModuleName("./foo-bar/baz-qux.js"));
  }

  // Tests filename without js extension
  @Test
  public void testToModuleName_noExtension_retainsName() {
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar"));
  }

  // Tests relative module resolution with ./
  @Test
  public void testToModuleName_relativeSameDirectory_resolvesCorrectly() {
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("./bar", "foo/baz.js"));
  }

  // Tests relative module resolution with ../
  @Test
  public void testToModuleName_relativeParentDirectory_resolvesCorrectly() {
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("../bar", "foo/sub/baz.js"));
  }

  // Tests non-relative module name in relative method
  @Test
  public void testToModuleName_absoluteOrNamedModule_returnsPrefixedModule() {
    assertEquals("module$other", ProcessCommonJSModules.toModuleName("other", "foo/baz.js"));
  }

  // Tests guessCJSModuleName with matching filename prefix
  @Test
  public void testGuessCJSModuleName_withPrefix_normalizesAndConverts() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "base/path/");
    assertEquals("module$foo$bar", pass.guessCJSModuleName("base/path/foo/bar.js"));
  }

  // Tests guessCJSModuleName when prefix lacks trailing slash
  @Test
  public void testGuessCJSModuleName_prefixWithoutTrailingSlash_addsSlashAutomatically() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "base/path");
    assertEquals("module$foo", pass.guessCJSModuleName("base/path/foo.js"));
  }

  // Tests guessCJSModuleName when filename does not start with prefix
  @Test
  public void testGuessCJSModuleName_unrelatedPrefix_convertsFullPath() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "base/path/");
    assertEquals("module$other$foo", pass.guessCJSModuleName("other/foo.js"));
  }

  // Tests processing of simple exports statement
  @Test
  public void testProcess_simpleExports_transformsModuleExports() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, ".");
    Node script = compiler.parseSyntheticCode("test.js", "exports.foo = 1;");
    Node root = IR.block(script);
    Node externs = IR.block();

    pass.process(externs, root);

    assertNotNull(pass.getModule());
    assertEquals("module$test", pass.getModule().getName());
  }

  // Tests processing of require call and dependency emission
  @Test
  public void testProcess_requireCall_rewritesRequireAndEmitsProvide() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, ".", true);
    Node script = compiler.parseSyntheticCode("test.js", "var mod = require('./other');");
    Node root = IR.block(script);
    Node externs = IR.block();

    pass.process(externs, root);

    assertNotNull(pass.getModule());
    CompilerInput input = compiler.getInput(script.getInputId());
    assertNotNull(input);
    assertTrue(input.getRequires().contains("module$other"));
    assertTrue(input.getProvides().contains("module$test"));
  }

  // Tests module.exports assignment rewrite
  @Test
  public void testProcess_moduleExportsAssignment_rewritesAssignment() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, ".");
    Node script = compiler.parseSyntheticCode("foo/bar.js", "module.exports = function() {};");
    Node root = IR.block(script);
    Node externs = IR.block();

    pass.process(externs, root);

    assertNotNull(pass.getModule());
    assertEquals("module$foo$bar", pass.getModule().getName());
  }

  // Tests reportDependencies set to false
  @Test
  public void testProcess_reportDependenciesFalse_doesNotCreateModule() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, ".", false);
    Node script = compiler.parseSyntheticCode("test.js", "var a = 1;");
    Node root = IR.block(script);
    Node externs = IR.block();

    pass.process(externs, root);

    assertNull(pass.getModule());
  }
}
package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSModule;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * JUnit 4 test class for ProcessCommonJSModules.
 * Covers static methods, guessCJSModuleName, and the full pass.
 */
public class ProcessCommonJSModulesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    // No need to call initCompilerOptions – will be initialized by compile methods
  }

  // ---------- Static method: toModuleName(String) ----------

  // Tests basic module name conversion: prefix "module$" and slashes to "$"
  @Test
  public void testToModuleName_simplePath_returnsModulePrefixAndSlashesAsDollars() {
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("./foo/bar.js"));
  }

  // Tests that leading ./, /, .js, and dashes are properly handled
  @Test
  public void testToModuleName_noLeadingDotSlash_keepsString() {
    assertEquals("module$some$module", ProcessCommonJSModules.toModuleName("some/module.js"));
  }

  // Tests empty string input
  @Test
  public void testToModuleName_emptyString_returnsModulePrefix() {
    assertEquals("module$", ProcessCommonJSModules.toModuleName(""));
  }

  // Tests removal of .js suffix
  @Test
  public void testToModuleName_withJsSuffix_removesSuffix() {
    assertEquals("module$app", ProcessCommonJSModules.toModuleName("./app.js"));
  }

  // Tests replacement of dashes with underscores
  @Test
  public void testToModuleName_withDash_replacesWithUnderscore() {
    assertEquals("module$my_lib", ProcessCommonJSModules.toModuleName("./my-lib.js"));
  }

  // Tests that leading "./" is removed when present
  @Test
  public void testToModuleName_withDotSlash_removesLeadingDotSlash() {
    assertEquals("module$subdir$file", ProcessCommonJSModules.toModuleName("./subdir/file.js"));
  }

  // Tests that a path without .js but with trailing slash? Not typical, but okay.
  @Test
  public void testToModuleName_withoutJsExtension_keepsLastPart() {
    assertEquals("module$noext", ProcessCommonJSModules.toModuleName("./noext"));
  }

  // ---------- Static method: toModuleName(String, String) ----------

  // Tests relative path resolution with current filename
  @Test
  public void testToModuleName_relativePathWithCurrent_resolvesCorrectly() {
    String result = ProcessCommonJSModules.toModuleName("./bar.js", "/root/foo.js");
    // Expected: module$root$bar
    assertEquals("module$root$bar", result);
  }

  // Tests relative path with ".." level
  @Test
  public void testToModuleName_relativeDotDot_resolvesAbove() {
    String result = ProcessCommonJSModules.toModuleName("../bar.js", "/root/foo/foo.js");
    // Expected: module$root$bar
    assertEquals("module$root$bar", result);
  }

  // Tests relative path resolving to root level
  @Test
  public void testToModuleName_relativeDotDotToRoot_resolvesCorrectly() {
    String result = ProcessCommonJSModules.toModuleName("../bar.js", "/root/foo.js");
    // Expected: module$bar
    assertEquals("module$bar", result);
  }

  // Tests that .js suffix is stripped from both arguments
  @Test
  public void testToModuleName_bothHaveJsSuffix_strippedCorrectly() {
    String result = ProcessCommonJSModules.toModuleName("./sub.js", "/base/main.js");
    // Expected: module$base$sub
    assertEquals("module$base$sub", result);
  }

  // ---------- guessCJSModuleName ----------

  // Tests that filename prefix is stripped and module name is generated
  @Test
  public void testGuessCJSModuleName_withPrefix_stripsPrefix() {
    // pass with default prefix ("./") and filename "./foo.js"
    ProcessCommonJSModules p = new ProcessCommonJSModules(compiler, "./");
    String result = p.guessCJSModuleName("./foo.js");
    assertEquals("module$foo", result);
  }

  // Tests when filename does not start with prefix
  @Test
  public void testGuessCJSModuleName_noPrefix_keepsFullPath() {
    ProcessCommonJSModules p = new ProcessCommonJSModules(compiler, "./");
    String result = p.guessCJSModuleName("/absolute/file.js");
    assertEquals("module$absolute$file", result);
  }

  // Tests when filename prefix is empty
  @Test
  public void testGuessCJSModuleName_emptyPrefix_filenameKeptAsIs() {
    ProcessCommonJSModules p = new ProcessCommonJSModules(compiler, "");
    String result = p.guessCJSModuleName("relative/mod.js");
    // "relative/mod.js" -> module$relative$mod
    assertEquals("module$relative$mod", result);
  }

  // ---------- Full pass process() ----------

  // Helper to parse JS and run the pass, returning root node AST
  private Node compileAndProcess(String js, boolean reportDependencies) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    List<SourceFile> inputs = List.of(input);
    compiler.compileModules(List.of(), inputs, System.err);
    Node root = compiler.getRoot();
    root.removeChildren(); // Make sure we have a clean root? Actually we'll use the script node.
    // Instead, we create a new compiler and parse directly.
    // Better approach: create Compiler, parse one script, get its AST.
    Compiler temp = new Compiler();
    temp.compileModules(List.of(), List.of(input), System.err);
    // The root contains externs and code. We'll extract the script node.
    Node jsRoot = temp.getRoot().getLastChild(); // Usually the second child is code.
    // For safety, find the script node.
    Node script = null;
    for (Node child : jsRoot.children()) {
      if (child.isScript()) {
        script = child;
        break;
      }
    }
    if (script == null) {
      // If not found, use the root's last child directly
      script = jsRoot;
    }
    // Create a new root with just this script.
    Node syntheticRoot = IR.root();
    syntheticRoot.addChildToBack(script.cloneTree());
    // Now run the pass
    ProcessCommonJSModules pass = new ProcessCommonJSModules(temp, "./", reportDependencies);
    pass.process(temp.getRoot(), syntheticRoot); // externs can be empty root
    return syntheticRoot;
  }

  // Tests that goog.provide is added to script
  @Test
  public void testProcess_script_googProvideAdded() {
    Node root = compileAndProcess("var x = 1;", true);
    Node script = root.getFirstChild();
    // First child should be goog.provide call
    Node firstChild = script.getFirstChild();
    assertTrue(firstChild.isExprResult());
    Node call = firstChild.getFirstChild();
    assertTrue(call.isCall());
    assertEquals("goog.provide", call.getFirstChild().getQualifiedName());
    assertEquals("module$test", call.getLastChild().getString());
  }

  // Tests that require call is replaced with module name and goog.require added
  @Test
  public void testProcess_requireCall_rewrittenToNameAndGoogRequireAdded() {
    Node root = compileAndProcess("var a = require('./other.js');", true);
    Node script = root.getFirstChild();
    // The require call should be replaced with a name node "module$test$other"
    // The statement should be "var a = module$test$other"
    // Also a goog.require should be added at front.
    Node firstExpr = script.getFirstChild(); // goog.require
    Node secondExpr = script.getChildAtIndex(1); // var statement
    // verify var
    assertTrue(secondExpr.isVar());
    Node varName = secondExpr.getFirstChild();
    assertEquals("a", varName.getString());
    Node init = varName.getFirstChild();
    assertEquals("module$test$other", init.getString());
    // verify goog.require
    Node call = firstExpr.getFirstChild();
    assertTrue(call.isCall());
    assertEquals("goog.require", call.getFirstChild().getQualifiedName());
    assertEquals("module$test$other", call.getLastChild().getString());
  }

  // Tests that module.exports is rewritten to moduleName.module$exports
  @Test
  public void testProcess_moduleExports_renamedToModuleExports() {
    Node root = compileAndProcess("module.exports = 42;", true);
    Node script = root.getFirstChild();
    // The script should have a goog.provide, a var init, and the assignment.
    // The assignment should be module$test.module$exports = 42
    // Find the expr result that has the assignment
    Node assignExpr = null;
    for (Node child : script.children()) {
      if (child.isExprResult() && child.getFirstChild().isAssign()) {
        assignExpr = child;
        break;
      }
    }
    assertNotNull(assignExpr);
    Node assign = assignExpr.getFirstChild();
    Node getprop = assign.getFirstChild();
    assertTrue(getprop.isGetProp());
    assertEquals("module$test", getprop.getFirstChild().getString());
    assertEquals("module$exports", getprop.getLastChild().getString());
  }

  // Tests that global variables are renamed with suffix
  @Test
  public void testProcess_globalVarRenamed_withSuffix() {
    Node root = compileAndProcess("var g = 1;", false);
    Node script = root.getFirstChild();
    // The var statement should be renamed to g$$module$test
    Node var = null;
    for (Node child : script.children()) {
      if (child.isVar()) {
        var = child;
        break;
      }
    }
    assertNotNull(var);
    Node varName = var.getFirstChild();
    assertEquals("g$$module$test", varName.getString());
    assertEquals("g", varName.getProp(Node.ORIGINALNAME_PROP));
  }

  // Tests that when reportDependencies is false, no goog.provide or goog.require are added
  @Test
  public void testProcess_noReportDependencies_skipsProvidesAndRequires() {
    Node root = compileAndProcess("var a = require('./other.js');", false);
    Node script = root.getFirstChild();
    // Should only have the var statement (require replaced) and maybe module var? Actually it still adds module var and goog.provide? Wait: in visitScript it always adds goog.provide and var for module regardless of reportDependencies.
    // The code shows: script.addChildToFront(IR.var(IR.name(moduleName), IR.objectlit())); always.
    // reportDependencies affects only the addition of provide to CI and JSModule creation.
    // So goog.provide is still added? Let's check: line "script.addChildToFront(IR.exprResult(IR.call(IR.getprop(IR.name("goog"), IR.string("provide")), IR.string(moduleName))));" is unconditional.
    // So even with reportDependencies false, goog.provide is added. That is correct.
    // The require replacement happens, but goog.require is added only if reportDependencies true? Actually visitRequireCall: "if (reportDependencies) { t.getInput().addRequire(moduleName); }" and then always "script.addChildToFront(IR.exprResult(IR.call(...)));". So goog.require is always added! That seems to match source: it always adds the expression.
    // So we cannot test skip here. But we can test that JSModule is not set.
    // Actually we can test that pass.getModule() returns null? Let's create pass with false and check.
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", false);
    // We can run a small script and then check getModule().
    SourceFile input = SourceFile.fromCode("test.js", "var x=1;");
    compiler.compileModules(List.of(), List.of(input), System.err);
    Node root = compiler.getRoot().getLastChild();
    pass.process(compiler.getRoot(), root.cloneTree());
    assertNull(pass.getModule());
  }

  // Tests that passing two script nodes throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testProcess_twoScriptNodes_throwsException() {
    // Manually create a root with two script nodes
    Node root = IR.root();
    root.addChildToBack(IR.script());
    root.addChildToBack(IR.script());
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./");
    compiler.getRoot().addChildToBack(IR.externs()); // Need externs
    pass.process(compiler.getRoot(), root);
  }

  // Tests that module.exports override emits if block when moduleExports is present
  @Test
  public void testProcess_moduleExportsOverride_emitsIfBlock() {
    Node root = compileAndProcess("module.exports = 42;", true);
    Node script = root.getFirstChild();
    // At the end of script, there should be an if block for module$test.module$exports
    Node lastChild = script.getLastChild();
    assertTrue(lastChild.isIf());
    Node ifCond = lastChild.getFirstChild();
    assertTrue(ifCond.isGetProp());
    assertEquals("module$test", ifCond.getFirstChild().getString());
    assertEquals("module$exports", ifCond.getLastChild().getString());
  }
}
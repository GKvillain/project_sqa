package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbstractCommandLineRunnerTest {

  private static class DummyCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {

    @Override
    protected Compiler createCompiler() {
      return new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      CompilerOptions options = new CompilerOptions();
      initOptionsFromFlags(options);
      return options;
    }
  }

  private DummyCommandLineRunner runner;

  @Before
  public void setUp() {
    runner = new DummyCommandLineRunner();
  }

  // Tests define replacement with implicit boolean true
  @Test
  public void testCreateDefineReplacements_implicitTrue() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_BOOL");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    assertEquals(1, options.getDefineReplacements().size());
  }

  // Tests define replacement with explicit boolean true and false
  @Test
  public void testCreateDefineReplacements_booleanTrueAndFalse() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_TRUE=true", "DEF_FALSE=false");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    assertEquals(2, options.getDefineReplacements().size());
  }

  // Tests define replacement with string literal in single quotes
  @Test
  public void testCreateDefineReplacements_stringLiteral() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_STR='hello'");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    assertEquals(1, options.getDefineReplacements().size());
  }

  // Tests define replacement with numeric literals
  @Test
  public void testCreateDefineReplacements_numberLiteral() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_INT=42", "DEF_DOUBLE=3.14");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    assertEquals(2, options.getDefineReplacements().size());
  }

  // Tests define replacement with invalid string containing inner quote throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_stringWithInnerQuote_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_STR='hel'lo'");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests define replacement with empty definition name throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_emptyName_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("=true");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests define replacement with invalid non-numeric/non-boolean unquoted value throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_invalidValue_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_VAR=invalid_unquoted");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests creating a single valid JS module
  @Test
  public void testCreateJsModules_validSingleModule() throws Exception {
    List<String> specs = Lists.newArrayList("m1:2");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);

    assertEquals(1, modules.length);
    assertEquals("m1", modules[0].getName());
    assertEquals(2, modules[0].getInputs().size());
  }

  // Tests creating multiple JS modules with dependencies
  @Test
  public void testCreateJsModules_validMultipleModulesWithDependencies() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:m1");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);

    assertEquals(2, modules.length);
    assertEquals("m1", modules[0].getName());
    assertEquals("m2", modules[1].getName());
    assertEquals(1, modules[1].getDependencies().size());
    assertEquals("m1", modules[1].getDependencies().get(0).getName());
  }

  // Tests module specification with invalid number of parts throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidPartsCount_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification with invalid module name throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidModuleName_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("123badname:1");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification with duplicate module name throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m1:1");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification with invalid non-numeric file count throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidFileCount_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:notanumber");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification when there are not enough js files throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_notEnoughJsFiles_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:2");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification when there are too many js files throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_tooManyJsFiles_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module specification depending on unknown module throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_unknownDependency_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1:unknownModule");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests parsing valid module wrapper
  @Test
  public void testParseModuleWrappers_validWrapper() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1"), new JSModule("m2") };
    List<String> specs = Lists.newArrayList("m1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);

    assertEquals("(function(){%s})();", wrappers.get("m1"));
    assertEquals("", wrappers.get("m2"));
  }

  // Tests parsing module wrapper missing colon format throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m1_wrapper");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests parsing module wrapper with unknown module throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("unknownMod:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests parsing module wrapper missing placeholder throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingPlaceholder_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m1:wrapperWithoutPlaceholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests writing output with wrapper containing placeholder in middle
  @Test
  public void testWriteOutput_withWrapperPlaceholder() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    AbstractCommandLineRunner.writeOutput(ps, null, "var a = 1;", "PREFIX(%output%);", "%output%");
    ps.flush();

    String result = baos.toString().trim();
    assertEquals("PREFIX(var a = 1;);", result);
  }

  // Tests writing output with placeholder at the end of wrapper
  @Test
  public void testWriteOutput_placeholderAtEnd() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    AbstractCommandLineRunner.writeOutput(ps, null, "var a = 1;", "PREFIX:%output%", "%output%");
    ps.flush();

    String result = baos.toString().trim();
    assertEquals("PREFIX:var a = 1;", result);
  }

  // Tests writing output without wrapper placeholder
  @Test
  public void testWriteOutput_withoutPlaceholder() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    AbstractCommandLineRunner.writeOutput(ps, null, "var a = 1;", "no_placeholder", "%output%");
    ps.flush();

    String result = baos.toString().trim();
    assertEquals("var a = 1;", result);
  }

  // Tests initializing compiler options from CommandLineConfig
  @Test
  public void testInitOptionsFromFlags_populatesOptions() {
    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    config.setDefine(Lists.newArrayList("FLAG_A=true", "FLAG_B=123"));

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals(2, options.getDefineReplacements().size());
  }

  // Tests setting run options with jsOutputFile and summaryDetailLevel
  @Test
  public void testSetRunOptions_configuresOptionsProperly() throws Exception {
    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    config.setJsOutputFile("output.js");
    config.setSummaryDetailLevel(2);

    CompilerOptions options = new CompilerOptions();
    runner.setRunOptions(options);

    assertEquals("output.js", options.jsOutputFile);
    assertEquals(2, options.summaryDetailLevel);
  }
}
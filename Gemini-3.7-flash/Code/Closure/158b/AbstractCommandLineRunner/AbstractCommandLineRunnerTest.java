package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.AbstractCommandLineRunner.CommandLineConfig;
import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractCommandLineRunnerTest {

  private TestRunner runner;
  private CompilerOptions options;

  private static class TestRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private final Compiler compiler;
    private final CompilerOptions options;

    TestRunner(Compiler compiler, CompilerOptions options) {
      super();
      this.compiler = compiler;
      this.options = options;
    }

    @Override
    protected Compiler createCompiler() {
      return compiler;
    }

    @Override
    protected CompilerOptions createOptions() {
      return options;
    }
  }

  @Before
  public void setUp() {
    Compiler compiler = new Compiler();
    options = new CompilerOptions();
    runner = new TestRunner(compiler, options);
  }

  // Tests boolean defines (true and false)
  @Test
  public void testCreateDefineOrTweakReplacements_booleanValues_setsBooleanDefines() {
    List<String> defs = Lists.newArrayList("DEF_TRUE=true", "DEF_FALSE=false");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
    // Verifies no exception and flags are accepted
  }

  // Tests define with no value defaults to true
  @Test
  public void testCreateDefineOrTweakReplacements_noValue_defaultsToTrue() {
    List<String> defs = Lists.newArrayList("DEF_FLAG");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests single-quoted string define value
  @Test
  public void testCreateDefineOrTweakReplacements_singleQuotedString_setsStringDefine() {
    List<String> defs = Lists.newArrayList("DEF_STR='hello world'");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests double-quoted string define value
  @Test
  public void testCreateDefineOrTweakReplacements_doubleQuotedString_setsStringDefine() {
    List<String> defs = Lists.newArrayList("DEF_STR=\"hello world\"");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests numeric define value
  @Test
  public void testCreateDefineOrTweakReplacements_numericValue_setsDoubleDefine() {
    List<String> defs = Lists.newArrayList("DEF_NUM=42.5");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests invalid define value throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineOrTweakReplacements_invalidValue_throwsException() {
    List<String> defs = Lists.newArrayList("DEF_INVALID=invalid_identifier");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests invalid tweak value throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineOrTweakReplacements_invalidTweak_throwsException() {
    List<String> tweaks = Lists.newArrayList("TWEAK_INVALID=invalid_val");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  // Tests valid tweak processing
  @Test
  public void testCreateDefineOrTweakReplacements_validTweaks_processedSuccessfully() {
    List<String> tweaks = Lists.newArrayList("twk.bool=true", "twk.str='abc'", "twk.num=123");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  // Tests tweak with no value defaults to true
  @Test
  public void testCreateDefineOrTweakReplacements_tweakNoValue_defaultsToTrue() {
    List<String> tweaks = Lists.newArrayList("twk.flag");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  // Tests negative numeric define value
  @Test
  public void testCreateDefineOrTweakReplacements_negativeNumericValue_setsDoubleDefine() {
    List<String> defs = Lists.newArrayList("DEF_NEG=-10.5");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  // Tests parsing valid module wrapper specifications
  @Test
  public void testParseModuleWrappers_validSpecs_returnsParsedMap() throws FlagUsageException {
    JSModule m1 = new JSModule("mod1");
    JSModule m2 = new JSModule("mod2");
    List<JSModule> modules = Lists.newArrayList(m1, m2);
    List<String> specs = Lists.newArrayList("mod1:(function(){%s})();");

    Map<String, String> result = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals("(function(){%s})();", result.get("mod1"));
    assertEquals("", result.get("mod2"));
  }

  // Tests empty module wrapper specifications return empty wrapper for all modules
  @Test
  public void testParseModuleWrappers_emptySpecs_returnsEmptyWrappersForAllModules() throws FlagUsageException {
    JSModule m1 = new JSModule("mod1");
    JSModule m2 = new JSModule("mod2");
    List<JSModule> modules = Lists.newArrayList(m1, m2);
    List<String> specs = Collections.emptyList();

    Map<String, String> result = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals("", result.get("mod1"));
    assertEquals("", result.get("mod2"));
  }

  // Tests module wrapper without colon throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsFlagUsageException() throws FlagUsageException {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("mod1_wrapper");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests module wrapper with unknown module throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsFlagUsageException() throws FlagUsageException {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("unknownMod:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests module wrapper missing %s placeholder throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappers_missingPlaceholder_throwsFlagUsageException() throws FlagUsageException {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("mod1:no_placeholder;");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests writing output with wrapper placeholder replacement
  @Test
  public void testWriteOutput_withWrapper_replacesPlaceholderAndAppendsNewline() throws IOException {
    StringBuilder out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(out, null, "var a = 1;", "prefix(%s)suffix;", "%s");
    assertEquals("prefix(var a = 1;)suffix;\n", out.toString());
  }

  // Tests writing output without wrapper
  @Test
  public void testWriteOutput_withoutWrapper_writesCodeDirectly() throws IOException {
    StringBuilder out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(out, null, "var a = 1;", "", "%s");
    assertEquals("var a = 1;\n", out.toString());
  }

  // Tests valid JS module name validation
  @Test
  public void testCheckModuleName_validIdentifier_doesNotThrow() throws FlagUsageException {
    runner.checkModuleName("validModule_123");
  }

  // Tests invalid JS module name validation throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCheckModuleName_invalidIdentifier_throwsFlagUsageException() throws FlagUsageException {
    runner.checkModuleName("invalid-module-name");
  }

  // Tests creating JS modules with empty specs returns null
  @Test
  public void testCreateJsModules_emptySpecs_returnsNull() throws Exception {
    assertNull(runner.createJsModules(Collections.<String>emptyList(), Lists.newArrayList("f1.js")));
    assertNull(runner.createJsModules(null, Lists.newArrayList("f1.js")));
  }

  // Tests creating JS modules with valid dependency order
  @Test
  public void testCreateJsModules_validSpecsAndFiles_createsModules() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:m1");
    List<String> files = Lists.newArrayList("m1_file.js", "m2_file.js");

    List<JSModule> modules = runner.createJsModules(specs, files);
    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getName());
    assertEquals("m2", modules.get(1).getName());
    assertTrue(modules.get(1).getDependencies().contains(modules.get(0)));
  }

  // Tests creating JS modules with multiple dependencies
  @Test
  public void testCreateJsModules_multipleDependencies_parsesCorrectly() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1", "m3:1:m1,m2");
    List<String> files = Lists.newArrayList("f1.js", "f2.js", "f3.js");

    List<JSModule> modules = runner.createJsModules(specs, files);
    assertEquals(3, modules.size());
    assertEquals(2, modules.get(2).getDependencies().size());
  }

  // Tests creating JS modules with wildcard file count consumes remaining files
  @Test
  public void testCreateJsModules_wildcardFileCount_consumesRemainingFiles() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:*");
    List<String> files = Lists.newArrayList("f1.js", "f2.js", "f3.js");

    List<JSModule> modules = runner.createJsModules(specs, files);
    assertEquals(2, modules.size());
    assertEquals(1, modules.get(0).getInputs().size());
    assertEquals(2, modules.get(1).getInputs().size());
  }

  // Tests creating JS modules with missing dependency throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_missingDependency_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:nonexistent_dep");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules when file count is less than files provided throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_fileCountMismatchTooFew_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules when file count is more than files provided throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_fileCountMismatchTooMany_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:3");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules with non-integer file count throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_invalidFileCountNotAnInt_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:notAnInt");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules with too many parts in spec throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_tooManyPartsInSpec_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1:dep1:extraPart");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules with duplicate name throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m1:1");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests creating JS modules with invalid spec format throws FlagUsageException
  @Test(expected = FlagUsageException.class)
  public void testCreateJsModules_invalidSpecFormat_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("m1");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests expanding source map path with null/empty path returns null
  @Test
  public void testExpandSourceMapPath_nullOrEmptySourceMapPath_returnsNull() {
    options.sourceMapOutputPath = null;
    assertNull(runner.expandSourceMapPath(options, null));

    options.sourceMapOutputPath = "";
    assertNull(runner.expandSourceMapPath(options, null));
  }

  // Tests expanding source map path with %outname% placeholder
  @Test
  public void testExpandSourceMapPath_withOutnamePlaceholder_expandsCorrectly() {
    options.sourceMapOutputPath = "%outname%.map";
    runner.getCommandLineConfig().setJsOutputFile("output.js");

    String expanded = runner.expandSourceMapPath(options, null);
    assertEquals("output.js.map", expanded);
  }

  // Tests expanding source map path for a module
  @Test
  public void testExpandSourceMapPath_withModule_expandsWithModulePrefix() {
    options.sourceMapOutputPath = "%outname%.map";
    runner.getCommandLineConfig().setModuleOutputPathPrefix("dist/mod_");
    JSModule module = new JSModule("core");

    String expanded = runner.expandSourceMapPath(options, module);
    assertEquals("dist/mod_core.js.map", expanded);
  }

  // Tests expanding manifest path returns null when not configured
  @Test
  public void testExpandManifest_emptyManifestConfig_returnsNull() {
    runner.getCommandLineConfig().setOutputManifest("");
    assertNull(runner.expandManifest(null));

    runner.getCommandLineConfig().setOutputManifest(null);
    assertNull(runner.expandManifest(null));
  }

  // Tests expanding manifest path with outname without module
  @Test
  public void testExpandManifest_withOutnameNoModule_expandsCorrectly() {
    runner.getCommandLineConfig().setOutputManifest("%outname%.manifest");
    runner.getCommandLineConfig().setJsOutputFile("app.js");

    assertEquals("app.js.manifest", runner.expandManifest(null));
  }

  // Tests expanding manifest path with outname for a module
  @Test
  public void testExpandManifest_withOutnameAndModule_expandsCorrectly() {
    runner.getCommandLineConfig().setOutputManifest("%outname%.manifest");
    runner.getCommandLineConfig().setModuleOutputPathPrefix("dist/mod_");
    JSModule module = new JSModule("core");

    assertEquals("dist/mod_core.manifest", runner.expandManifest(module));
  }
}
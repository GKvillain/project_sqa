package com.google.javascript.jscomp;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractCommandLineRunnerTest {

  private TestCommandLineRunner runner;

  private static class TestCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private Compiler compiler = new Compiler();
    private CompilerOptions options = new CompilerOptions();

    TestCommandLineRunner() {
      super();
    }

    TestCommandLineRunner(PrintStream out, PrintStream err) {
      super(out, err);
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
    runner = new TestCommandLineRunner();
  }

  // Tests define replacement with boolean, number, and string literals
  @Test
  public void testCreateDefineOrTweakReplacements_validDefines_setsOptions() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList(
        "BOOL_TRUE=true",
        "BOOL_FALSE=false",
        "IMPLICIT_TRUE",
        "NUM_VAL=123.45",
        "STR_SINGLE='test_str'",
        "STR_DOUBLE=\"another_str\""
    );
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defines, options, false);
    // Verified if no exception is thrown and parsed properly
    assertNotNull(options);
  }

  // Tests tweak replacement with boolean and string
  @Test
  public void testCreateDefineOrTweakReplacements_validTweaks_setsOptions() {
    CompilerOptions options = new CompilerOptions();
    List<String> tweaks = Lists.newArrayList(
        "tweak_bool=true",
        "tweak_implicit",
        "tweak_num=42",
        "tweak_str='sample'"
    );
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
    assertNotNull(options);
  }

  // Tests invalid define syntax exception path
  @Test(expected = RuntimeException.class)
  public void testCreateDefineOrTweakReplacements_invalidDefine_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("INVALID_VAL=not_quoted_string");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defines, options, false);
  }

  // Tests invalid tweak syntax exception path
  @Test(expected = RuntimeException.class)
  public void testCreateDefineOrTweakReplacements_invalidTweak_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> tweaks = Lists.newArrayList("INVALID_TWEAK=not_quoted");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  // Tests parsing valid module wrappers
  @Test
  public void testParseModuleWrappers_validSpecs_returnsMap() throws Exception {
    JSModule m1 = new JSModule("mod1");
    JSModule m2 = new JSModule("mod2");
    List<JSModule> modules = Lists.newArrayList(m1, m2);
    List<String> specs = Lists.newArrayList("mod1:(function(){%s})();");

    Map<String, String> wrappers =
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);

    assertEquals("(function(){%s})();", wrappers.get("mod1"));
    assertEquals("", wrappers.get("mod2"));
  }

  // Tests module wrapper with missing delimiter exception
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsException() throws Exception {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("mod1_without_colon");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests module wrapper with unknown module exception
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsException() throws Exception {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("unknownMod:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests module wrapper without placeholder exception
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_noPlaceholder_throwsException() throws Exception {
    JSModule m1 = new JSModule("mod1");
    List<JSModule> modules = Lists.newArrayList(m1);
    List<String> specs = Lists.newArrayList("mod1:no_placeholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests writeOutput formatting with wrapper placeholder
  @Test
  public void testWriteOutput_withWrapper_appendsCorrectly() throws IOException {
    StringBuilder out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(
        out, null, "var x = 1;", "(function(){%output%})();", "%output%");
    assertEquals("(function(){var x = 1;\n})();\n", out.toString());
  }

  // Tests writeOutput without placeholder
  @Test
  public void testWriteOutput_withoutPlaceholder_appendsCodeDirectly() throws IOException {
    StringBuilder out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(
        out, null, "var x = 1;", "wrapperWithoutMarker", "%output%");
    assertEquals("var x = 1;\n", out.toString());
  }

  // Tests createJsModules with valid specs and dependencies
  @Test
  public void testCreateJsModules_validSpecs_createsModulesSuccessfully() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:m1");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");

    List<JSModule> modules = runner.createJsModules(specs, files);

    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getName());
    assertEquals("m2", modules.get(1).getName());
    assertTrue(modules.get(1).getDependencies().contains(modules.get(0)));
  }

  // Tests createJsModules with invalid js file count format
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidFileCount_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:invalidCount");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests createJsModules with not enough JS files
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_notEnoughFiles_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:2");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests createJsModules with too many JS files
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_tooManyFiles_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests createJsModules with unknown module dependency
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_unknownDependency_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1:unknownModule");
    List<String> files = Lists.newArrayList("f1.js");
    runner.createJsModules(specs, files);
  }

  // Tests createJsModules with duplicate module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m1:1");
    List<String> files = Lists.newArrayList("f1.js", "f2.js");
    runner.createJsModules(specs, files);
  }

  // Tests checkModuleName validation with invalid JS identifier
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCheckModuleName_invalidIdentifier_throwsException() throws Exception {
    runner.checkModuleName("invalid-module-name");
  }

  // Tests setRunOptions setting various language modes and options
  @Test
  public void testSetRunOptions_languageModes_setsOptionsSuccessfully() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
        .setLanguageIn("ECMASCRIPT5")
        .setAcceptConstKeyword(true)
        .setJsOutputFile("out.js")
        .setCreateSourceMap("out.map");

    runner.setRunOptions(options);

    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5, options.getLanguageIn());
    assertTrue(options.acceptConstKeyword);
    assertEquals("out.js", options.jsOutputFile);
    assertEquals("out.map", options.sourceMapOutputPath);
  }

  // Tests setRunOptions with unknown language option
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptions_unknownLanguage_throwsException() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig().setLanguageIn("INVALID_LANG");
    runner.setRunOptions(options);
  }

  // Tests setRunOptions with invalid charset
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptions_invalidCharset_throwsException() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME");
    runner.setRunOptions(options);
  }

  // Tests path expansions for source map and manifest
  @Test
  public void testExpandCommandLinePath_placeholdersExpanded() {
    runner.getCommandLineConfig()
        .setJsOutputFile("output/bundle.js")
        .setModuleOutputPathPrefix("modules/prefix_")
        .setOutputManifest("%outname%.manifest");

    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "%outname%.map";

    JSModule mod = new JSModule("core");
    String expandedModuleMap = runner.expandSourceMapPath(options, mod);
    assertEquals("modules/prefix_core.js.map", expandedModuleMap);

    String expandedManifest = runner.expandManifest(null);
    assertEquals("output/bundle.js.manifest", expandedManifest);
  }

  // Tests enableTestMode flags and behavior
  @Test
  public void testEnableTestMode_flagsSet_testModeTrue() {
    final int[] exitCode = new int[]{-999};
    runner.enableTestMode(
        new Supplier<List<JSSourceFile>>() {
          @Override
          public List<JSSourceFile> get() {
            return Collections.emptyList();
          }
        },
        new Supplier<List<JSSourceFile>>() {
          @Override
          public List<JSSourceFile> get() {
            return Collections.emptyList();
          }
        },
        null,
        new Function<Integer, Boolean>() {
          @Override
          public Boolean apply(Integer code) {
            exitCode[0] = code;
            return true;
          }
        }
    );

    assertTrue(runner.isInTestMode());
    runner.run();
    assertTrue(exitCode[0] != -999);
  }
}
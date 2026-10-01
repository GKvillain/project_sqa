package com.google.javascript.jscomp;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractCommandLineRunnerTest {

  private TestCommandLineRunner runner;
  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;

  private static class TestCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {

    TestCommandLineRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    @Override
    protected Compiler createCompiler() {
      return new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      return new CompilerOptions();
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    runner = new TestCommandLineRunner(
        new PrintStream(outStream), new PrintStream(errStream));
  }

  // Tests define boolean literals (both implicit and explicit)
  @Test
  public void testCreateDefineReplacements_booleanDefines() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("FLAG_A", "FLAG_B=true", "FLAG_C=false");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);
    // Verified by running without exception
  }

  // Tests define string literals with single and double quotes
  @Test
  public void testCreateDefineReplacements_stringDefines() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("STR_A='hello'", "STR_B=\"world\"");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);
  }

  // Tests define numeric literal
  @Test
  public void testCreateDefineReplacements_numberDefine() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("NUM=42.5");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);
  }

  // Tests invalid define syntax throwing RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_invalidSyntax_throwsException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("INVALID=unquotedString");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);
  }

  // Tests createJsModules with valid specs and dependency
  @Test
  public void testCreateJsModules_validSpecs() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:m1");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertEquals(2, modules.length);
    assertEquals("m1", modules[0].getName());
    assertEquals("m2", modules[1].getName());
    assertEquals(1, modules[1].getDependencies().size());
  }

  // Tests createJsModules with invalid spec format
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidSpecFormat_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules with invalid identifier as module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidModuleName_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("123bad:1");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules with duplicate module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m1:1");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules when js files count does not match total
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_notEnoughJsFiles_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:2");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules with unknown dependency
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_unknownDependency_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1:unknown");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests parseModuleWrappers with valid input
  @Test
  public void testParseModuleWrappers_validInput() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1"), new JSModule("m2")};
    List<String> specs = Lists.newArrayList("m1:(function(){%s})();");
    Map<String, String> wrappers =
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals(2, wrappers.size());
    assertEquals("(function(){%s})();", wrappers.get("m1"));
    assertEquals("", wrappers.get("m2"));
  }

  // Tests parseModuleWrappers missing colon
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsException() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1")};
    List<String> specs = Lists.newArrayList("m1Wrapper");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests parseModuleWrappers for unknown module
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsException() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1")};
    List<String> specs = Lists.newArrayList("m2:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests parseModuleWrappers missing placeholder %s
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingPlaceholder_throwsException() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1")};
    List<String> specs = Lists.newArrayList("m1:no_placeholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests writeOutput with wrapper and placeholder
  @Test
  public void testWriteOutput_withWrapper() throws Exception {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(
        sb, null, "var a = 1;", "prefix(%s)suffix;", "%s");
    assertEquals("prefix(var a = 1;)suffix;\n", sb.toString());
  }

  // Tests writeOutput without placeholder
  @Test
  public void testWriteOutput_withoutPlaceholder() throws Exception {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(
        sb, null, "var a = 1;", "no_placeholder", "%s");
    assertEquals("var a = 1;\n", sb.toString());
  }

  // Tests expandSourceMapPath and expandManifest paths
  @Test
  public void testExpandCommandLinePath_singleAndModuleMode() {
    runner.getCommandLineConfig().setJsOutputFile("out.js");
    runner.getCommandLineConfig().setCreateSourceMap("%outname%.map");
    runner.getCommandLineConfig().setOutputManifest("%outname%.manifest");

    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "%outname%.map";

    String smPath = runner.expandSourceMapPath(options, null);
    assertEquals("out.js.map", smPath);

    String manifestPath = runner.expandManifest(null);
    assertEquals("out.js.manifest", manifestPath);

    JSModule mod = new JSModule("core");
    runner.getCommandLineConfig().setModuleOutputPathPrefix("dist/");
    String modSmPath = runner.expandSourceMapPath(options, mod);
    assertEquals("dist/core.js.map", modSmPath);
  }

  // Tests setRunOptions setting options from config
  @Test
  public void testSetRunOptions_validConfig() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
        .setJsOutputFile("output.js")
        .setCreateSourceMap("output.js.map")
        .setSummaryDetailLevel(2);

    runner.setRunOptions(options);

    assertEquals("output.js", options.jsOutputFile);
    assertEquals("output.js.map", options.sourceMapOutputPath);
    assertEquals(2, options.summaryDetailLevel);
  }

  // Tests setRunOptions with invalid charset throwing FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptions_invalidCharset_throwsException() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME_12345");
    runner.setRunOptions(options);
  }

  // Tests createJsModules with multiple dependencies
  @Test
  public void testCreateJsModules_multipleDependencies() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1", "m3:1:m1,m2");
    List<String> jsFiles = Lists.newArrayList("f1.js", "f2.js", "f3.js");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertEquals(3, modules.length);
    assertEquals("m3", modules[2].getName());
    assertEquals(2, modules[2].getDependencies().size());
  }

  // Tests createJsModules with non-integer count
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_nonIntegerCount_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("m1:invalid_num");
    List<String> jsFiles = Lists.newArrayList("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests parseModuleWrappers with null or empty specs
  @Test
  public void testParseModuleWrappers_emptySpecs() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1")};
    Map<String, String> wrappers =
        AbstractCommandLineRunner.parseModuleWrappers(new ArrayList<String>(), modules);
    assertEquals(1, wrappers.size());
    assertEquals("", wrappers.get("m1"));

    Map<String, String> nullWrappers =
        AbstractCommandLineRunner.parseModuleWrappers(null, modules);
    assertEquals(1, nullWrappers.size());
    assertEquals("", nullWrappers.get("m1"));
  }

  // Tests parseModuleWrappers with duplicate module wrapper specification
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_duplicateModule_throwsException() throws Exception {
    JSModule[] modules = new JSModule[] {new JSModule("m1")};
    List<String> specs = Lists.newArrayList("m1:%s", "m1:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests setRunOptions with formatting options enabled
  @Test
  public void testSetRunOptions_formattingOptions() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
        .setFormattingPaths(Lists.newArrayList(
            AbstractCommandLineRunner.CommandLineConfig.FormattingOption.PRETTY_PRINT,
            AbstractCommandLineRunner.CommandLineConfig.FormattingOption.PRINT_INPUT_DELIMITER));

    runner.setRunOptions(options);

    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests setRunOptions with source map format options
  @Test
  public void testSetRunOptions_sourceMapFormat() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
        .setCreateSourceMap("output.map")
        .setSourceMapFormat(SourceMap.Format.V3);

    runner.setRunOptions(options);

    assertEquals(SourceMap.Format.V3, options.sourceMapFormat);
  }

  // Tests createInputs with standard input indicator '-'
  @Test
  public void testCreateInputs_stdin() throws Exception {
    List<String> files = Lists.newArrayList("-");
    List<CompilerInput> inputs = runner.createInputs(files, false);
    assertEquals(1, inputs.size());
    assertEquals(AbstractCommandLineRunner.FLAG_DEF_JS_ERROR_SOURCE, inputs.get(0).getName());
  }

  // Tests expandCommandLinePath when neither jsOutputFile nor moduleOutputPathPrefix is set
  @Test
  public void testExpandCommandLinePath_emptyOutName() {
    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "%outname%.map";
    String path = runner.expandSourceMapPath(options, null);
    assertNull(path);
  }
}
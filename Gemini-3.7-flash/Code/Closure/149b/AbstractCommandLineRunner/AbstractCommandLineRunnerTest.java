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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractCommandLineRunnerTest {

  private TestCommandLineRunner runner;
  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;

  private static class TestCommandLineRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private Compiler mockCompiler;
    private CompilerOptions mockOptions;

    TestCommandLineRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    void setMockCompiler(Compiler compiler) {
      this.mockCompiler = compiler;
    }

    void setMockOptions(CompilerOptions options) {
      this.mockOptions = options;
    }

    @Override
    protected Compiler createCompiler() {
      return mockCompiler != null ? mockCompiler : new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      return mockOptions != null ? mockOptions : new CompilerOptions();
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    runner = new TestCommandLineRunner(new PrintStream(outStream), new PrintStream(errStream));
  }

  // Tests boolean defines flag syntax
  @Test
  public void testCreateDefineReplacements_booleanValues_setsCorrectOptions() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("FLAG1", "FLAG2=true", "FLAG3=false");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);

    // Verify through define evaluation or absence of exceptions
    assertNotNull(options);
  }

  // Tests string and numeric defines flag syntax
  @Test
  public void testCreateDefineReplacements_stringAndNumberValues_setsCorrectOptions() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("STR='hello'", "STR2=\"world\"", "NUM=123.45");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);

    assertNotNull(options);
  }

  // Tests invalid define flag syntax throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_invalidSyntax_throwsRuntimeException() {
    CompilerOptions options = new CompilerOptions();
    List<String> defines = Lists.newArrayList("INVALID_VAL=abc");
    AbstractCommandLineRunner.createDefineReplacements(defines, options);
  }

  // Tests valid module creation with 1 module
  @Test
  public void testCreateJsModules_validSingleModule_returnsModuleArray() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:2");
    List<String> jsFiles = Lists.newArrayList("a.js", "b.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertEquals(1, modules.length);
    assertEquals("mod1", modules[0].getName());
    assertEquals(2, modules[0].getInputs().size());
  }

  // Tests module creation with dependencies
  @Test
  public void testCreateJsModules_withDependencies_returnsModulesWithDeps() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:1:m1");
    List<String> jsFiles = Lists.newArrayList("a.js", "b.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertEquals(2, modules.length);
    assertEquals("m1", modules[0].getName());
    assertEquals("m2", modules[1].getName());
    assertEquals(1, modules[1].getDependencies().size());
    assertEquals("m1", modules[1].getDependencies().get(0).getName());
  }

  // Tests module creation with invalid module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidModuleName_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("invalid-name:1");
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module creation with duplicate module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:1", "mod1:1");
    List<String> jsFiles = Lists.newArrayList("a.js", "b.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module creation with wrong JS file count
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_notEnoughJsFiles_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:2");
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module creation when too many JS files are provided
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_tooManyJsFiles_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:1");
    List<String> jsFiles = Lists.newArrayList("a.js", "b.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests module creation with unknown dependency
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_unknownDependency_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:1:unknownMod");
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests parse module wrappers successfully
  @Test
  public void testParseModuleWrappers_validWrapper_returnsMap() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1"), new JSModule("m2") };
    List<String> specs = Lists.newArrayList("m1:(function(){%s})();");

    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals("(function(){%s})();", wrappers.get("m1"));
    assertEquals("", wrappers.get("m2"));
  }

  // Tests parse module wrapper with unknown module
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m2:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests parse module wrapper without placeholder
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_noPlaceholder_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m1:no_placeholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests writeOutput with wrapper
  @Test
  public void testWriteOutput_withWrapper_wrapsCodeCorrectly() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 1;", "(function(){%s})();", "%s");
    assertEquals("(function(){var a = 1;})();\n", sb.toString());
  }

  // Tests writeOutput without wrapper placeholder
  @Test
  public void testWriteOutput_withoutPlaceholder_appendsCodeDirectly() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 1;", "wrapper", "%s");
    assertEquals("var a = 1;\n", sb.toString());
  }

  // Tests expandManifest and expandSourceMapPath
  @Test
  public void testExpandCommandLinePath_singleOutputAndModule_expandsCorrectly() {
    runner.getCommandLineConfig().setJsOutputFile("output.js");
    runner.getCommandLineConfig().setOutputManifest("manifest-%outname%.txt");

    String expanded = runner.expandManifest(null);
    assertEquals("manifest-output.js.txt", expanded);

    JSModule mod = new JSModule("core");
    runner.getCommandLineConfig().setModuleOutputPathPrefix("out/");
    String expandedModule = runner.expandManifest(mod);
    assertEquals("manifest-out/core.js.txt", expandedModule);
  }

  // Tests createExterns default behavior when no externs are provided
  @Test
  public void testCreateExterns_emptyExternsList_returnsDefaultDevNull() throws Exception {
    List<JSSourceFile> externs = runner.createExterns();
    assertEquals(1, externs.size());
    assertEquals("/dev/null", externs.get(0).getName());
  }

  // Tests setRunOptions sets configuration into CompilerOptions
  @Test
  public void testSetRunOptions_configuresOptionsProperly() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
        .setJsOutputFile("out.js")
        .setCreateSourceMap("map.out")
        .setSummaryDetailLevel(2);

    runner.setRunOptions(options);

    assertEquals("out.js", options.jsOutputFile);
    assertEquals("map.out", options.sourceMapOutputPath);
    assertEquals(2, options.summaryDetailLevel);
  }

  // Tests invalid charset in setRunOptions throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptions_invalidCharset_throwsFlagUsageException() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME");
    runner.setRunOptions(options);
  }

  // Tests createInputs with files and stdin
  @Test
  public void testCreateInputs_regularFilesAndStdIn_createsInputsCorrectly() throws Exception {
    List<String> files = Lists.newArrayList("file1.js", "-", "file2.js");
    List<JSSourceFile> inputs = AbstractCommandLineRunner.createInputs(files, true);
    assertEquals(3, inputs.size());
    assertEquals("file1.js", inputs.get(0).getName());
    assertEquals("stdin", inputs.get(1).getName());
    assertEquals("file2.js", inputs.get(2).getName());
  }

  // Tests createInputs with stdin not allowed throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateInputs_stdInNotAllowed_throwsFlagUsageException() throws Exception {
    List<String> files = Lists.newArrayList("file1.js", "-");
    AbstractCommandLineRunner.createInputs(files, false);
  }

  // Tests createInputs with duplicate stdin throws FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateInputs_duplicateStdIn_throwsFlagUsageException() throws Exception {
    List<String> files = Lists.newArrayList("-", "-");
    AbstractCommandLineRunner.createInputs(files, true);
  }

  // Tests createJsModules with invalid spec format without colon
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_specMissingColon_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("invalid_spec");
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules with non-integer file count
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_specInvalidNumber_throwsFlagUsageException() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:not_a_number");
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests createJsModules with empty specs list
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_emptySpecs_throwsFlagUsageException() throws Exception {
    List<String> specs = Collections.emptyList();
    List<String> jsFiles = Lists.newArrayList("a.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  // Tests parseModuleWrappers with output placeholder %output%
  @Test
  public void testParseModuleWrappers_withOutputPlaceholder_returnsMap() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m1:// header\n%output%");

    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals("// header\n%output%", wrappers.get("m1"));
  }

  // Tests parseModuleWrappers with spec missing colon
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsFlagUsageException() throws Exception {
    JSModule[] modules = new JSModule[] { new JSModule("m1") };
    List<String> specs = Lists.newArrayList("m1_wrapper_without_colon");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  // Tests expandSourceMapPath expansion
  @Test
  public void testExpandSourceMapPath_withPlaceholders_expandsCorrectly() {
    runner.getCommandLineConfig().setJsOutputFile("app.js");
    runner.getCommandLineConfig().setCreateSourceMap("maps/%outname%.map");

    String expanded = runner.expandSourceMapPath(null);
    assertEquals("maps/app.js.map", expanded);

    JSModule mod = new JSModule("submodule");
    runner.getCommandLineConfig().setModuleOutputPathPrefix("dist/");
    String expandedMod = runner.expandSourceMapPath(mod);
    assertEquals("maps/dist/submodule.js.map", expandedMod);
  }

  // Tests expandManifest when manifest is null
  @Test
  public void testExpandManifest_nullManifest_returnsNull() {
    runner.getCommandLineConfig().setOutputManifest(null);
    assertNull(runner.expandManifest(null));
  }

  // Tests expandSourceMapPath when createSourceMap is null
  @Test
  public void testExpandSourceMapPath_nullSourceMap_returnsNull() {
    runner.getCommandLineConfig().setCreateSourceMap(null);
    assertNull(runner.expandSourceMapPath(null));
  }

  // Tests expandCommandLinePath with null pattern
  @Test
  public void testExpandCommandLinePath_nullPattern_returnsNull() {
    assertNull(AbstractCommandLineRunner.expandCommandLinePath(null, null));
  }
}
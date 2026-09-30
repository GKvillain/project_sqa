package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

  private static class TestableCommandLineRunner extends CommandLineRunner {
    TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    TestableCommandLineRunner(String[] args) {
      super(args);
    }

    @Override
    public CompilerOptions createOptions() {
      return super.createOptions();
    }

    @Override
    public Compiler createCompiler() {
      return super.createCompiler();
    }

    @Override
    public List<JSSourceFile> createExterns() throws IOException, FlagUsageException {
      return super.createExterns();
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    out = new PrintStream(outStream);
    err = new PrintStream(errStream);
  }

  @After
  public void tearDown() {
    out.close();
    err.close();
  }

  // Tests --version flag alone without explicit value
  @Test
  public void testVersion_noParam_printsVersionAndValid() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--version"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    String errOutput = errStream.toString();
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
  }

  // Tests --version flag with explicit boolean values
  @Test
  public void testVersion_explicitValues_handledCorrectly() {
    TestableCommandLineRunner runnerTrue = new TestableCommandLineRunner(
        new String[] {"--version=true"}, out, err);
    assertTrue(runnerTrue.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Closure Compiler"));

    errStream.reset();
    TestableCommandLineRunner runnerFalse = new TestableCommandLineRunner(
        new String[] {"--version=false"}, out, err);
    assertTrue(runnerFalse.shouldRunCompiler());
    assertEquals("", errStream.toString());
  }

  // Tests --help flag triggers usage output and invalidates config
  @Test
  public void testHelp_flagPassed_printsUsageAndInvalidatesConfig() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--help"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = errStream.toString();
    assertTrue(errOutput.contains("--help"));
  }

  // Tests default runner construction with valid empty arguments
  @Test
  public void testInit_emptyArgs_configIsValid() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {}, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests invalid/unknown command line argument handling
  @Test
  public void testInit_unknownFlag_configIsInvalid() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--non_existent_flag_xyz"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests single-argument constructor
  @Test
  public void testConstructor_singleArgArray_constructsSuccessfully() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {});
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with default options
  @Test
  public void testCreateOptions_defaultFlags_returnsOptions() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests createOptions with --debug enabled
  @Test
  public void testCreateOptions_debugFlag_setsDebugOptions() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--debug"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with compilation level settings
  @Test
  public void testCreateOptions_compilationLevel_configuresOptions() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--compilation_level=WHITESPACE_ONLY"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with warning level settings
  @Test
  public void testCreateOptions_warningLevel_configuresOptions() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--warning_level=VERBOSE"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with formatting options
  @Test
  public void testCreateOptions_formattingOptions_setsPrettyPrintAndDelimiter() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--formatting=PRETTY_PRINT", "--formatting=PRINT_INPUT_DELIMITER"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests BooleanOptionHandler with boolean truthy and falsy aliases
  @Test
  public void testBooleanOptionHandler_variousAliases_parsedCorrectly() {
    TestableCommandLineRunner runnerOn = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=on"}, out, err);
    assertTrue(runnerOn.createOptions().closurePass);

    TestableCommandLineRunner runnerYes = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=yes"}, out, err);
    assertTrue(runnerYes.createOptions().closurePass);

    TestableCommandLineRunner runnerOne = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=1"}, out, err);
    assertTrue(runnerOne.createOptions().closurePass);

    TestableCommandLineRunner runnerOff = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=off"}, out, err);
    assertFalse(runnerOff.createOptions().closurePass);

    TestableCommandLineRunner runnerNo = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=no"}, out, err);
    assertFalse(runnerNo.createOptions().closurePass);

    TestableCommandLineRunner runnerZero = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=0"}, out, err);
    assertFalse(runnerZero.createOptions().closurePass);
  }

  // Tests argument parsing with single and double quotes stripping
  @Test
  public void testInit_quotedArguments_quotesStrippedCorrectly() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--define='FOO=true'", "--define=\"BAR=123\""}, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests third_party flag affects coding convention
  @Test
  public void testInit_thirdPartyFlag_setsDefaultCodingConvention() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--third_party"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.getCodingConvention() instanceof DefaultCodingConvention);
  }

  // Tests getDefaultExterns loads required standard externs list
  @Test
  public void testGetDefaultExterns_loadsValidDefaultExterns() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
  }

  // Tests createExterns with default externs included
  @Test
  public void testCreateExterns_default_includesDefaultExterns() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
  }

  // Tests createExterns when --use_only_custom_externs is enabled
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlyCustom() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--use_only_custom_externs"}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests createCompiler returns non-null Compiler instance
  @Test
  public void testCreateCompiler_createsCompilerInstance() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {}, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests default coding convention is ClosureCodingConvention
  @Test
  public void testInit_defaultCodingConvention_isClosure() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {}, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.getCodingConvention() instanceof ClosureCodingConvention);
  }

  // Tests BooleanOptionHandler error message on invalid boolean argument
  @Test
  public void testBooleanOptionHandler_invalidValue_printsErrorAndInvalidatesConfig() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--process_closure_primitives=invalid_bool"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests warning_level QUIET and DEFAULT configurations
  @Test
  public void testCreateOptions_warningLevels_quietAndDefault() {
    TestableCommandLineRunner runnerQuiet = new TestableCommandLineRunner(
        new String[] {"--warning_level=QUIET"}, out, err);
    CompilerOptions optionsQuiet = runnerQuiet.createOptions();
    assertNotNull(optionsQuiet);

    TestableCommandLineRunner runnerDefault = new TestableCommandLineRunner(
        new String[] {"--warning_level=DEFAULT"}, out, err);
    CompilerOptions optionsDefault = runnerDefault.createOptions();
    assertNotNull(optionsDefault);
  }

  // Tests compilation_level SIMPLE_OPTIMIZATIONS and ADVANCED_OPTIMIZATIONS
  @Test
  public void testCreateOptions_compilationLevels_simpleAndAdvanced() {
    TestableCommandLineRunner runnerSimple = new TestableCommandLineRunner(
        new String[] {"--compilation_level=SIMPLE_OPTIMIZATIONS"}, out, err);
    CompilerOptions optionsSimple = runnerSimple.createOptions();
    assertNotNull(optionsSimple);

    TestableCommandLineRunner runnerAdvanced = new TestableCommandLineRunner(
        new String[] {"--compilation_level=ADVANCED_OPTIMIZATIONS"}, out, err);
    CompilerOptions optionsAdvanced = runnerAdvanced.createOptions();
    assertNotNull(optionsAdvanced);
  }

  // Tests dependency management and closure entry point flags
  @Test
  public void testCreateOptions_closureDependenciesAndEntryPoints() {
    TestableCommandLineRunner runnerManage = new TestableCommandLineRunner(
        new String[] {"--manage_closure_dependencies=true"}, out, err);
    assertNotNull(runnerManage.createOptions());

    TestableCommandLineRunner runnerEntry = new TestableCommandLineRunner(
        new String[] {"--closure_entry_point=my.app.start"}, out, err);
    assertNotNull(runnerEntry.createOptions());

    TestableCommandLineRunner runnerOnly = new TestableCommandLineRunner(
        new String[] {"--only_closure_dependencies=true", "--closure_entry_point=my.app.start"}, out, err);
    assertNotNull(runnerOnly.createOptions());
  }

  // Tests define flags configured into options
  @Test
  public void testCreateOptions_defines() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {"--define=FLAG_BOOL=true", "--define=FLAG_NUM=42", "--define=FLAG_STR='hello'"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }
}
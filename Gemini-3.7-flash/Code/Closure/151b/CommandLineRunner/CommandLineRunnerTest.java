package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandLineRunnerTest {

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
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
    public List<JSSourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  private SubCommandLineRunner createRunner(String[] args, ByteArrayOutputStream out, ByteArrayOutputStream err) {
    return new SubCommandLineRunner(args, new PrintStream(out), new PrintStream(err));
  }

  // Tests runner initialization with default/empty arguments
  @Test
  public void testConstructor_emptyArgs_shouldRunCompiler() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{}, out, err);

    assertTrue(runner.shouldRunCompiler());
    assertEquals("", err.toString());
  }

  // Tests --help flag branch which disables compilation and prints usage
  @Test
  public void testConstructor_helpFlag_shouldNotRunCompiler() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{"--help"}, out, err);

    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString().contains("--help"));
  }

  // Tests invalid flag argument handling
  @Test
  public void testConstructor_unknownFlag_shouldNotRunCompiler() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{"--non_existent_flag=true"}, out, err);

    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString().length() > 0);
  }

  // Tests quoted argument parsing branch
  @Test
  public void testConstructor_quotedArguments_parsedCorrectly() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--js_output_file=\"out.js\"", "--variable_map_input_file='var.map'"},
        out,
        err);

    assertTrue(runner.shouldRunCompiler());
    assertEquals("", err.toString());
  }

  // Tests BooleanOptionHandler with valid true values
  @Test
  public void testBooleanOptionHandler_validTrueValues_setsTrue() {
    String[] trueValues = {"true", "on", "yes", "1"};
    for (String val : trueValues) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      SubCommandLineRunner runner = createRunner(new String[]{"--debug=" + val}, out, err);

      assertTrue("Expected valid config for " + val, runner.shouldRunCompiler());
      CompilerOptions options = runner.createOptions();
      assertNotNull(options);
      assertTrue(options.anonymousFunctionNaming == AnonymousFunctionNamingPolicy.UNMAPPED);
    }
  }

  // Tests BooleanOptionHandler with valid false values
  @Test
  public void testBooleanOptionHandler_validFalseValues_setsFalse() {
    String[] falseValues = {"false", "off", "no", "0"};
    for (String val : falseValues) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      SubCommandLineRunner runner = createRunner(new String[]{"--debug=" + val}, out, err);

      assertTrue("Expected valid config for " + val, runner.shouldRunCompiler());
      CompilerOptions options = runner.createOptions();
      assertNotNull(options);
      assertFalse(options.anonymousFunctionNaming == AnonymousFunctionNamingPolicy.UNMAPPED);
    }
  }

  // Tests BooleanOptionHandler with illegal value throws exception and marks config invalid
  @Test
  public void testBooleanOptionHandler_illegalValue_marksConfigInvalid() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{"--debug=invalid_value"}, out, err);

    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString().contains("Illegal boolean value"));
  }

  // Tests default CompilerOptions creation
  @Test
  public void testCreateOptions_defaultFlags_returnsDefaultOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{}, out, err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests createOptions with compilation level and debug options
  @Test
  public void testCreateOptions_advancedOptimizationsWithDebug_setsOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--compilation_level=ADVANCED_OPTIMIZATIONS", "--debug=true"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.checkGlobalThisLevel.isOn());
    assertTrue(options.anonymousFunctionNaming == AnonymousFunctionNamingPolicy.UNMAPPED);
  }

  // Tests formatting option PRETTY_PRINT
  @Test
  public void testCreateOptions_formattingPrettyPrint_setsPrettyPrint() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--formatting=PRETTY_PRINT"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests formatting option PRINT_INPUT_DELIMITER
  @Test
  public void testCreateOptions_formattingPrintInputDelimiter_setsDelimiter() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--formatting=PRINT_INPUT_DELIMITER"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertFalse(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests warning level configuration
  @Test
  public void testCreateOptions_warningLevelQuiet_setsQuietOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--warning_level=QUIET"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests process_closure_primitives set to false
  @Test
  public void testCreateOptions_processClosurePrimitivesFalse_setsClosurePassFalse() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--process_closure_primitives=false"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests createCompiler returns non-null compiler instance
  @Test
  public void testCreateCompiler_returnsValidCompilerInstance() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(new String[]{}, out, err);

    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests getDefaultExterns loads default extern JS files correctly
  @Test
  public void testGetDefaultExterns_returnsExpectedExternsList() throws IOException {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
    assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  // Tests createExterns with use_only_custom_externs flag
  @Test
  public void testCreateExterns_useOnlyCustomExternsTrue_excludesDefaultExterns()
      throws FlagUsageException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--use_only_custom_externs=true"},
        out,
        err);

    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests compilation levels WHITESPACE_ONLY and SIMPLE_OPTIMIZATIONS
  @Test
  public void testCreateOptions_compilationLevels_setProperly() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runnerWhitespace = createRunner(
        new String[]{"--compilation_level=WHITESPACE_ONLY"},
        out,
        err);
    CompilerOptions optionsWhitespace = runnerWhitespace.createOptions();
    assertNotNull(optionsWhitespace);

    SubCommandLineRunner runnerSimple = createRunner(
        new String[]{"--compilation_level=SIMPLE_OPTIMIZATIONS"},
        out,
        err);
    CompilerOptions optionsSimple = runnerSimple.createOptions();
    assertNotNull(optionsSimple);
  }

  // Tests warning level VERBOSE
  @Test
  public void testCreateOptions_warningLevelVerbose_setsVerboseOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--warning_level=VERBOSE"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.checkGlobalThisLevel.isOn());
  }

  // Tests third_party flag
  @Test
  public void testCreateOptions_thirdParty_setsOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--third_party=true"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests manage_closure_dependencies and only_closure_dependencies flags
  @Test
  public void testCreateOptions_closureDependenciesFlags_setOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{
          "--manage_closure_dependencies=true",
          "--only_closure_dependencies=true",
          "--closure_entry_point=goog.events"
        },
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.dependencyOptions.needsManagement());
  }

  // Tests summary_detail_level option
  @Test
  public void testCreateOptions_summaryDetailLevel_setsSummaryDetailLevel() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = createRunner(
        new String[]{"--summary_detail_level=3"},
        out,
        err);

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals(3, options.summaryDetailLevel);
  }
}
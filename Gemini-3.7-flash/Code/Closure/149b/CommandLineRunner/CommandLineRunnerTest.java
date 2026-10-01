package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

  // Subclass to expose protected methods for testing
  private static class SubCommandLineRunner extends CommandLineRunner {
    public SubCommandLineRunner(String[] args) {
      super(args);
    }

    public SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
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

  // Tests valid empty arguments leading to runnable state
  @Test
  public void testShouldRunCompiler_emptyArgs_returnsTrue() {
    String[] args = new String[] {};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests --help flag setting configuration to invalid and displaying usage
  @Test
  public void testInitConfigFromFlags_helpFlag_returnsFalseAndPrintsUsage() {
    String[] args = new String[] {"--help"};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Displays this message"));
  }

  // Tests invalid/unknown option handling
  @Test
  public void testInitConfigFromFlags_unknownFlag_returnsFalse() {
    String[] args = new String[] {"--unknown_flag_value"};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests key-value pair argument format with quotes stripping
  @Test
  public void testInitConfigFromFlags_quotedArgPattern_parsesCorrectly() {
    String[] args = new String[] {"--logging_level='INFO'", "--charset=\"UTF-8\""};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with default options configuration
  @Test
  public void testCreateOptions_defaultOptions_returnsValidCompilerOptions() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
  }

  // Tests debug and formatting flag options applied to CompilerOptions
  @Test
  public void testCreateOptions_debugAndFormattingFlags_appliesToOptions() {
    String[] args = new String[] {
        "--debug=true",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests warning level QUIET option
  @Test
  public void testCreateOptions_warningLevelQuiet_appliesToOptions() {
    String[] args = new String[] {"--warning_level=QUIET"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests compilation level WHITESPACE_ONLY option
  @Test
  public void testCreateOptions_compilationLevelWhitespace_appliesToOptions() {
    String[] args = new String[] {"--compilation_level=WHITESPACE_ONLY"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests BooleanOptionHandler with valid true/false alias inputs
  @Test
  public void testInitConfigFromFlags_booleanOptionHandler_validValues() {
    String[] argsTrue = new String[] {"--process_closure_primitives=1"};
    CommandLineRunner runnerTrue = new CommandLineRunner(argsTrue, out, err);
    assertTrue(runnerTrue.shouldRunCompiler());

    String[] argsFalse = new String[] {"--process_closure_primitives=off"};
    SubCommandLineRunner runnerFalse = new SubCommandLineRunner(argsFalse, out, err);
    assertTrue(runnerFalse.shouldRunCompiler());
    CompilerOptions options = runnerFalse.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests BooleanOptionHandler with illegal boolean value
  @Test
  public void testInitConfigFromFlags_booleanOptionHandler_invalidValue_fails() {
    String[] args = new String[] {"--process_closure_primitives=not_a_boolean"};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Illegal boolean value"));
  }

  // Tests createCompiler returns non-null Compiler instance
  @Test
  public void testCreateCompiler_createsNonNullInstance() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests getDefaultExterns loading built-in default externs list from zip resource
  @Test
  public void testGetDefaultExterns_loadsSuccessfully() throws IOException {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
    assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  // Tests createExterns including default externs
  @Test
  public void testCreateExterns_defaultExternsIncluded() throws Exception {
    String[] args = new String[] {};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
  }

  // Tests createExterns excluding default externs via --use_only_custom_externs
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlyProvided() throws Exception {
    String[] args = new String[] {"--use_only_custom_externs=true"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests third_party flag configuration
  @Test
  public void testInitConfigFromFlags_thirdPartyFlag_setsCodingConvention() {
    String[] args = new String[] {"--third_party=true"};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests single argument constructor
  @Test
  public void testSingleArgConstructor_validInstantiation() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = new SubCommandLineRunner(args);
    assertTrue(runner.shouldRunCompiler());
    assertNotNull(runner.createCompiler());
  }

  // Tests compilation level ADVANCED_OPTIMIZATIONS
  @Test
  public void testCreateOptions_compilationLevelAdvanced_appliesToOptions() {
    String[] args = new String[] {"--compilation_level=ADVANCED_OPTIMIZATIONS"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.removeDeadCode);
    assertTrue(options.inlineFunctions);
  }

  // Tests compilation level SIMPLE_OPTIMIZATIONS
  @Test
  public void testCreateOptions_compilationLevelSimple_appliesToOptions() {
    String[] args = new String[] {"--compilation_level=SIMPLE_OPTIMIZATIONS"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertFalse(options.inlineFunctions);
  }

  // Tests warning level DEFAULT and VERBOSE
  @Test
  public void testCreateOptions_warningLevelDefaultAndVerbose_appliesToOptions() {
    String[] argsDefault = new String[] {"--warning_level=DEFAULT"};
    SubCommandLineRunner runnerDefault = new SubCommandLineRunner(argsDefault, out, err);
    CompilerOptions optionsDefault = runnerDefault.createOptions();
    assertNotNull(optionsDefault);

    String[] argsVerbose = new String[] {"--warning_level=VERBOSE"};
    SubCommandLineRunner runnerVerbose = new SubCommandLineRunner(argsVerbose, out, err);
    CompilerOptions optionsVerbose = runnerVerbose.createOptions();
    assertNotNull(optionsVerbose);
    assertTrue(optionsVerbose.checkGlobalNamesLevel.isOn());
  }

  // Tests --version flag prints version and marks compiler not to run
  @Test
  public void testInitConfigFromFlags_versionFlag_printsVersionAndHalts() {
    String[] args = new String[] {"--version"};
    CommandLineRunner runner = new CommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Closure Compiler (http://code.google.com/closure/compiler)"));
  }

  // Tests diagnostic group flags: jscomp_error, jscomp_warning, and jscomp_off
  @Test
  public void testCreateOptions_diagnosticGroups_setsCheckLevels() {
    String[] args = new String[] {
        "--jscomp_error=checkVars",
        "--jscomp_warning=undefinedVars",
        "--jscomp_off=deprecated"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals(CheckLevel.ERROR, options.getDiagnosticGroupState(DiagnosticGroups.CHECK_VARIABLES));
    assertEquals(CheckLevel.WARNING, options.getDiagnosticGroupState(DiagnosticGroups.UNDEFINED_VARIABLES));
    assertEquals(CheckLevel.OFF, options.getDiagnosticGroupState(DiagnosticGroups.DEPRECATED));
  }

  // Tests invalid diagnostic group throws FlagUsageException during createOptions
  @Test
  public void testCreateOptions_invalidDiagnosticGroup_throwsFlagUsageException() {
    String[] args = new String[] {"--jscomp_error=non_existent_group"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    try {
      runner.createOptions();
      fail("Expected FlagUsageException on invalid diagnostic group name");
    } catch (AbstractCommandLineRunner.FlagUsageException e) {
      assertTrue(e.getMessage().contains("non_existent_group"));
    }
  }

  // Tests --define flag for string, number, and boolean constants
  @Test
  public void testCreateOptions_defineFlags_setsDefineReplacements() {
    String[] args = new String[] {
        "--define=STR_VAR='hello'",
        "--define=NUM_VAR=42",
        "--define=BOOL_VAR=true"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertNotNull(options.getDefineReplacements());
  }

  // Tests invalid define format without equal sign throws FlagUsageException
  @Test
  public void testCreateOptions_invalidDefineFlag_throwsFlagUsageException() {
    String[] args = new String[] {"--define=INVALID_NO_EQUALS"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    try {
      runner.createOptions();
      fail("Expected FlagUsageException on invalid define expression");
    } catch (AbstractCommandLineRunner.FlagUsageException e) {
      assertTrue(e.getMessage().contains("INVALID_NO_EQUALS"));
    }
  }

  // Tests --output_wrapper flag and marker replacement
  @Test
  public void testCreateOptions_outputWrapper_appliesToOptions() {
    String[] args = new String[] {
        "--output_wrapper=(function(){%output%})();"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.outputWrapper.contains("(function(){%output%})();"));
  }

  // Tests summary_detail_level flag values
  @Test
  public void testCreateOptions_summaryDetailLevel_appliesToOptions() {
    String[] args = new String[] {"--summary_detail_level=0"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals(0, options.summaryDetailLevel);
  }

  // Tests generate_exports flag
  @Test
  public void testCreateOptions_generateExports_appliesToOptions() {
    String[] args = new String[] {"--generate_exports=true"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.generateExports);
  }

  // Tests create_source_map flag setting source map output path
  @Test
  public void testCreateOptions_sourceMap_appliesToOptions() {
    String[] args = new String[] {"--create_source_map=map.js"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals("map.js", options.sourceMapOutputPath);
  }

  // Tests manage_closure_dependencies and closure_entry_point flags
  @Test
  public void testCreateOptions_closureDependencies_appliesToOptions() {
    String[] args = new String[] {
        "--manage_closure_dependencies=true",
        "--closure_entry_point=goog.events"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.dependencyOptions.needsManagement());
    assertTrue(options.dependencyOptions.getEntryPoints().contains("goog.events"));
  }

  // Tests only_closure_dependencies flag enabling dependency management
  @Test
  public void testCreateOptions_onlyClosureDependencies_appliesToOptions() {
    String[] args = new String[] {
        "--only_closure_dependencies=true",
        "--closure_entry_point=my.app"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.dependencyOptions.needsManagement());
  }

  // Tests BooleanOptionHandler aliases: "yes", "no", "0", "true", "false", "on"
  @Test
  public void testInitConfigFromFlags_booleanOptionHandler_allValidAliases() {
    String[][] validPairs = new String[][] {
        {"--process_closure_primitives=yes", "true"},
        {"--process_closure_primitives=no", "false"},
        {"--process_closure_primitives=0", "false"},
        {"--process_closure_primitives=true", "true"},
        {"--process_closure_primitives=false", "false"},
        {"--process_closure_primitives=on", "true"}
    };

    for (String[] pair : validPairs) {
      CommandLineRunner runner = new CommandLineRunner(new String[] {pair[0]}, out, err);
      assertTrue("Failed for: " + pair[0], runner.shouldRunCompiler());
    }
  }

  // Tests custom externs inclusion with --externs flag
  @Test
  public void testCreateExterns_withCustomExternsFile() throws Exception {
    File tempExtern = File.createTempFile("custom_extern", ".js");
    tempExtern.deleteOnExit();

    String[] args = new String[] {
        "--externs=" + tempExtern.getAbsolutePath()
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.size() > 1);
  }
}
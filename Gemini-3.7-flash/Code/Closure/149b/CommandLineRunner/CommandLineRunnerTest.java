package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args) {
      super(args);
    }

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
    public List<JSSourceFile> createExterns() throws IOException {
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

  // Tests valid default arguments initializing the runner
  @Test
  public void testShouldRunCompiler_validDefaultArgs_returnsTrue() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {}, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests runner when --help is passed
  @Test
  public void testShouldRunCompiler_helpFlag_returnsFalse() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {"--help"}, out, err);
    assertFalse(runner.shouldRunCompiler());
  }

  // Tests runner when an invalid option is passed
  @Test
  public void testShouldRunCompiler_unknownFlag_returnsFalse() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {"--unknown_invalid_option=foo"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests quoted argument values are parsed and unquoted properly
  @Test
  public void testInitConfig_quotedAndEqualFormat_parsedSuccessfully() {
    String[] args = new String[] {
        "--js='input.js'",
        "--output_wrapper=\"(function(){%output%})();\"",
        "--charset=UTF-8"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests default options configuration created by createOptions
  @Test
  public void testCreateOptions_defaultConfiguration_createsExpectedDefaults() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {}, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests --debug option configuring debug options on compilation level
  @Test
  public void testCreateOptions_debugFlag_setsDebugOptions() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {"--debug=true"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.anonymousFunctionNaming != AnonymousFunctionNamingPolicy.OFF);
  }

  // Tests --formatting option with PRETTY_PRINT and PRINT_INPUT_DELIMITER
  @Test
  public void testCreateOptions_formattingOptions_setsPrettyPrintAndInputDelimiter() {
    String[] args = new String[] {
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests compilation level and warning level flag configurations
  @Test
  public void testCreateOptions_compilationAndWarningLevels_appliedCorrectly() {
    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=VERBOSE"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.checkGlobalThisLevel.isOn());
    assertTrue(options.smartNameRemoval);
  }

  // Tests disabling closure primitives via BooleanOptionHandler
  @Test
  public void testCreateOptions_processClosurePrimitivesFalse_disablesClosurePass() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--process_closure_primitives=false"}, out, err);
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests BooleanOptionHandler with valid true/false variants
  @Test
  public void testBooleanOptionHandler_validBooleanValues_parsedCorrectly() {
    SubCommandLineRunner runner1 = new SubCommandLineRunner(
        new String[] {"--print_tree=on", "--compute_phase_ordering=1", "--third_party=yes"}, out, err);
    assertTrue(runner1.shouldRunCompiler());
    CompilerOptions options1 = runner1.createOptions();
    assertTrue(options1.computePhaseOrdering);
    assertTrue(options1.getCodingConvention() instanceof DefaultCodingConvention);

    SubCommandLineRunner runner2 = new SubCommandLineRunner(
        new String[] {"--print_tree=off", "--compute_phase_ordering=0", "--third_party=no"}, out, err);
    assertTrue(runner2.shouldRunCompiler());
    CompilerOptions options2 = runner2.createOptions();
    assertFalse(options2.computePhaseOrdering);
    assertTrue(options2.getCodingConvention() instanceof ClosureCodingConvention);
  }

  // Tests BooleanOptionHandler with invalid value failing parse
  @Test
  public void testBooleanOptionHandler_invalidValue_failsValidation() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--print_tree=not_a_boolean"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Illegal boolean value"));
  }

  // Tests --third_party flag setting DefaultCodingConvention vs ClosureCodingConvention
  @Test
  public void testInitConfig_thirdPartyFlag_setsDefaultCodingConvention() {
    SubCommandLineRunner runnerDefault = new SubCommandLineRunner(new String[] {}, out, err);
    assertTrue(runnerDefault.createOptions().getCodingConvention() instanceof ClosureCodingConvention);

    SubCommandLineRunner runnerThirdParty = new SubCommandLineRunner(
        new String[] {"--third_party=true"}, out, err);
    assertTrue(runnerThirdParty.createOptions().getCodingConvention() instanceof DefaultCodingConvention);
  }

  // Tests createCompiler creating a non-null Compiler instance
  @Test
  public void testCreateCompiler_createsNonNullCompilerInstance() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {}, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests getDefaultExterns loading all default externs from archive
  @Test
  public void testGetDefaultExterns_returnsExpectedExternFiles() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
    assertEquals("externs.zip//es3.js", defaultExterns.get(0).getName());
  }

  // Tests createExterns including default externs by default
  @Test
  public void testCreateExterns_default_includesDefaultExterns() throws IOException {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
  }

  // Tests createExterns excluding default externs when use_only_custom_externs is enabled
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlyCustomList() throws IOException {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--use_only_custom_externs=true"}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests single-arg constructor of CommandLineRunner
  @Test
  public void testConstructor_singleArg_initializesCorrectly() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {});
    assertTrue(runner.shouldRunCompiler());
  }
}
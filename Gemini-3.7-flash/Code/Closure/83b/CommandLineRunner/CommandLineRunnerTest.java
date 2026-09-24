package com.google.javascript.jscomp;

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

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    SubCommandLineRunner(String[] args) {
      super(args);
    }

    @Override
    public CompilerOptions createOptions() {
      return super.createOptions();
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    out = new PrintStream(outStream);
    err = new PrintStream(errStream);
  }

  private SubCommandLineRunner createRunner(String[] args) {
    return new SubCommandLineRunner(args, out, err);
  }

  // Tests --version flag when passed with no subsequent parameters (Regression test for Defect 83)
  @Test
  public void testVersionFlag_alone_shouldBeValid() {
    String[] args = new String[] {"--version"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
  }

  // Tests --version flag followed by another argument
  @Test
  public void testVersionFlag_withFollowingArg_shouldBeValid() {
    String[] args = new String[] {"--version", "--js", "test.js"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("Closure Compiler"));
  }

  // Tests boolean option explicitly set to true
  @Test
  public void testBooleanOption_explicitTrue_shouldBeValid() {
    String[] args = new String[] {"--debug=true"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests boolean option explicitly set to false
  @Test
  public void testBooleanOption_explicitFalse_shouldBeValid() {
    String[] args = new String[] {"--process_closure_primitives=false"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests boolean option alternative values (on/off, yes/no, 1/0)
  @Test
  public void testBooleanOption_truthyAndFalsyValues_setsProperValue() {
    String[] argsOn = new String[] {"--debug=on"};
    SubCommandLineRunner runnerOn = createRunner(argsOn);
    assertTrue(runnerOn.shouldRunCompiler());

    String[] argsYes = new String[] {"--debug=yes"};
    SubCommandLineRunner runnerYes = createRunner(argsYes);
    assertTrue(runnerYes.shouldRunCompiler());

    String[] argsOne = new String[] {"--debug=1"};
    SubCommandLineRunner runnerOne = createRunner(argsOne);
    assertTrue(runnerOne.shouldRunCompiler());

    String[] argsOff = new String[] {"--debug=off"};
    SubCommandLineRunner runnerOff = createRunner(argsOff);
    assertTrue(runnerOff.shouldRunCompiler());

    String[] argsNo = new String[] {"--debug=no"};
    SubCommandLineRunner runnerNo = createRunner(argsNo);
    assertTrue(runnerNo.shouldRunCompiler());

    String[] argsZero = new String[] {"--debug=0"};
    SubCommandLineRunner runnerZero = createRunner(argsZero);
    assertTrue(runnerZero.shouldRunCompiler());
  }

  // Tests --help flag sets config invalid and prints usage
  @Test
  public void testHelpFlag_setsConfigInvalidAndPrintsUsage() {
    String[] args = new String[] {"--help"};
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("--help"));
  }

  // Tests unknown flag makes config invalid
  @Test
  public void testUnknownFlag_makesConfigInvalid() {
    String[] args = new String[] {"--non_existent_flag"};
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
  }

  // Tests default options creation
  @Test
  public void testCreateOptions_defaults_createsValidOptions() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests compilation_level flag with WHITESPACE_ONLY
  @Test
  public void testCreateOptions_compilationLevelWhitespace_setsOptions() {
    String[] args = new String[] {"--compilation_level=WHITESPACE_ONLY"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests compilation_level flag with ADVANCED_OPTIMIZATIONS
  @Test
  public void testCreateOptions_compilationLevelAdvanced_setsOptions() {
    String[] args = new String[] {"--compilation_level=ADVANCED_OPTIMIZATIONS"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests warning_level flag with QUIET and VERBOSE
  @Test
  public void testCreateOptions_warningLevels_setsOptions() {
    String[] argsQuiet = new String[] {"--warning_level=QUIET"};
    SubCommandLineRunner runnerQuiet = createRunner(argsQuiet);
    assertTrue(runnerQuiet.shouldRunCompiler());
    assertNotNull(runnerQuiet.createOptions());

    String[] argsVerbose = new String[] {"--warning_level=VERBOSE"};
    SubCommandLineRunner runnerVerbose = createRunner(argsVerbose);
    assertTrue(runnerVerbose.shouldRunCompiler());
    assertNotNull(runnerVerbose.createOptions());
  }

  // Tests formatting options PRETTY_PRINT and PRINT_INPUT_DELIMITER
  @Test
  public void testCreateOptions_formattingOptions_setsOptions() {
    String[] args = new String[] {
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER"
    };
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests argument parsing with double-quoted values
  @Test
  public void testArgParsing_quotedValues_unquotesSuccessfully() {
    String[] args = new String[] {"--compilation_level=\"WHITESPACE_ONLY\""};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests argument parsing with single-quoted values
  @Test
  public void testArgParsing_singleQuotedValues_unquotesSuccessfully() {
    String[] args = new String[] {"--compilation_level='ADVANCED_OPTIMIZATIONS'"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests third_party flag configuration
  @Test
  public void testThirdPartyFlag_setsDefaultCodingConvention() {
    String[] args = new String[] {"--third_party=true"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.getCodingConvention() instanceof DefaultCodingConvention);
  }

  // Tests non third_party flag configuration (Closure convention)
  @Test
  public void testClosureConvention_isDefault() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.getCodingConvention() instanceof ClosureCodingConvention);
  }

  // Tests getDefaultExterns loads required standard extern files
  @Test
  public void testGetDefaultExterns_returnsNonEmptyList() throws IOException {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
    assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  // Tests createCompiler returns non-null compiler instance
  @Test
  public void testCreateCompiler_returnsCompiler() {
    String[] args = new String[] {};
    SubCommandLineRunner runner = createRunner(args);
    assertNotNull(runner.createCompiler());
  }
}
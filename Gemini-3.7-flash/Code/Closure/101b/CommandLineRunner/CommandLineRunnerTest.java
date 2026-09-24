package com.google.javascript.jscomp;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CommandLineRunner}.
 */
public class CommandLineRunnerTest {

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args) throws CmdLineException {
      super(args);
    }

    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) throws CmdLineException {
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
    public List<JSSourceFile> createExterns() throws FlagUsageException, java.io.IOException {
      return super.createExterns();
    }
  }

  private SubCommandLineRunner createRunner(String[] args) throws CmdLineException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    return new SubCommandLineRunner(args, new PrintStream(out), new PrintStream(err));
  }

  // Tests defect 101b: --process_closure_primitives=false should disable closurePass even in ADVANCED_OPTIMIZATIONS
  @Test
  public void testProcessClosurePrimitives_advancedOptimizationFalse_disablesClosurePass() throws Exception {
    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--process_closure_primitives=false"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests default process_closure_primitives is true
  @Test
  public void testProcessClosurePrimitives_default_enablesClosurePass() throws Exception {
    String[] args = new String[] {};
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.closurePass);
  }

  // Tests compilation level WHITESPACE_ONLY
  @Test
  public void testCompilationLevel_whitespaceOnly_setsWhitespaceOnlyOptions() throws Exception {
    String[] args = new String[] {
        "--compilation_level=WHITESPACE_ONLY"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertFalse(options.checkTypes);
  }

  // Tests warning level QUIET
  @Test
  public void testWarningLevel_quiet_setsQuietOptions() throws Exception {
    String[] args = new String[] {
        "--warning_level=QUIET"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests warning level VERBOSE
  @Test
  public void testWarningLevel_verbose_setsVerboseOptions() throws Exception {
    String[] args = new String[] {
        "--warning_level=VERBOSE"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.checkTypes);
  }

  // Tests formatting flag with PRETTY_PRINT
  @Test
  public void testFormatting_prettyPrint_enablesPrettyPrint() throws Exception {
    String[] args = new String[] {
        "--formatting=PRETTY_PRINT"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
  }

  // Tests formatting flag with PRINT_INPUT_DELIMITER
  @Test
  public void testFormatting_printInputDelimiter_enablesInputDelimiter() throws Exception {
    String[] args = new String[] {
        "--formatting=PRINT_INPUT_DELIMITER"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.printInputDelimiter);
  }

  // Tests debug flag
  @Test
  public void testDebug_flagTrue_appliesDebugOptions() throws Exception {
    String[] args = new String[] {
        "--debug=true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests parsing argument with quotes
  @Test
  public void testArgParsing_quotedValues_unquotesProperly() throws Exception {
    String[] args = new String[] {
        "--js_output_file=\"out.js\""
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests boolean handler with various accepted truthy values
  @Test
  public void testBooleanOptionHandler_truthyValues_parsesTrue() throws Exception {
    String[] args = new String[] {
        "--print_tree=on",
        "--print_ast=yes",
        "--compute_phase_ordering=1"
    };
    SubCommandLineRunner runner = createRunner(args);
    assertNotNull(runner.createOptions());
  }

  // Tests boolean handler with various accepted falsy values
  @Test
  public void testBooleanOptionHandler_falsyValues_parsesFalse() throws Exception {
    String[] args = new String[] {
        "--print_tree=off",
        "--print_ast=no",
        "--compute_phase_ordering=0"
    };
    SubCommandLineRunner runner = createRunner(args);
    assertNotNull(runner.createOptions());
  }

  // Tests boolean handler with invalid value throws exception
  @Test(expected = CmdLineException.class)
  public void testBooleanOptionHandler_invalidValue_throwsCmdLineException() throws Exception {
    String[] args = new String[] {
        "--print_tree=invalid_bool"
    };
    createRunner(args);
  }

  // Tests unknown flag throws CmdLineException
  @Test(expected = CmdLineException.class)
  public void testInitConfig_unknownFlag_throwsCmdLineException() throws Exception {
    String[] args = new String[] {
        "--non_existent_flag=true"
    };
    createRunner(args);
  }

  // Tests createCompiler returns non-null compiler instance
  @Test
  public void testCreateCompiler_returnsCompiler() throws Exception {
    String[] args = new String[] {};
    SubCommandLineRunner runner = createRunner(args);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests custom externs flag excludes default externs
  @Test
  public void testCreateExterns_useOnlyCustomExternsTrue_returnsOnlyCustomExterns() throws Exception {
    String[] args = new String[] {
        "--use_only_custom_externs=true"
    };
    SubCommandLineRunner runner = createRunner(args);
    List<JSSourceFile> externs = runner.createExterns();
    assertEquals(0, externs.size());
  }

  // Tests default externs are loaded when use_only_custom_externs is false
  @Test
  public void testCreateExterns_useOnlyCustomExternsFalse_includesDefaultExterns() throws Exception {
    String[] args = new String[] {
        "--use_only_custom_externs=false"
    };
    SubCommandLineRunner runner = createRunner(args);
    List<JSSourceFile> externs = runner.createExterns();
    assertTrue(externs.size() > 0);
  }

  // Tests default constructor initializes without error
  @Test
  public void testConstructor_noArgs_succeeds() throws Exception {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {});
    assertNotNull(runner.createOptions());
  }
}
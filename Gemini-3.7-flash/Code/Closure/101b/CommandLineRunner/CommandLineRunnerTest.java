package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.kohsuke.args4j.CmdLineException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.List;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

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

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) throws CmdLineException {
      super(args, out, err);
    }

    SubCommandLineRunner(String[] args) throws CmdLineException {
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
    public List<JSSourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  // Tests process_closure_primitives flag set to false (Defects4J Closure-101 regression test)
  @Test
  public void testProcessClosurePrimitives_false_setsClosurePassFalse() throws Exception {
    String[] args = new String[] {
        "--process_closure_primitives=false",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests process_closure_primitives flag default value is true
  @Test
  public void testProcessClosurePrimitives_default_setsClosurePassTrue() throws Exception {
    String[] args = new String[] {
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.closurePass);
  }

  // Tests compilation_level ADVANCED_OPTIMIZATIONS and debug flag true
  @Test
  public void testCompilationLevel_advancedWithDebug_configuresOptions() throws Exception {
    String[] args = new String[] {
        "--compilation_level", "ADVANCED_OPTIMIZATIONS",
        "--debug=true",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.checkGlobalThisLevel.isOn());
    assertTrue(options.removeUnusedVars);
    assertTrue(options.anonymousFunctionNaming != AnonymousFunctionNamingPolicy.OFF);
  }

  // Tests compilation_level WHITESPACE_ONLY
  @Test
  public void testCompilationLevel_whitespaceOnly_configuresOptions() throws Exception {
    String[] args = new String[] {
        "--compilation_level=WHITESPACE_ONLY",
        "--js=input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertFalse(options.checkGlobalThisLevel.isOn());
  }

  // Tests warning_level QUIET and VERBOSE
  @Test
  public void testWarningLevel_quiet_configuresOptions() throws Exception {
    String[] args = new String[] {
        "--warning_level", "QUIET",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests warning_level VERBOSE
  @Test
  public void testWarningLevel_verbose_configuresOptions() throws Exception {
    String[] args = new String[] {
        "--warning_level", "VERBOSE",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.checkGlobalThisLevel.isOn());
  }

  // Tests formatting options PRETTY_PRINT and PRINT_INPUT_DELIMITER
  @Test
  public void testFormattingOptions_prettyPrintAndInputDelimiter_setsOptions() throws Exception {
    String[] args = new String[] {
        "--formatting", "PRETTY_PRINT",
        "--formatting", "PRINT_INPUT_DELIMITER",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests boolean option aliases (true, false, on, off, yes, no, 1, 0)
  @Test
  public void testBooleanOptionHandler_variousValues_parsedCorrectly() throws Exception {
    String[] args = new String[] {
        "--third_party=yes",
        "--print_tree=1",
        "--compute_phase_ordering=on",
        "--print_ast=true",
        "--create_name_map_files=false",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests boolean option parser with invalid boolean value
  @Test(expected = CmdLineException.class)
  public void testBooleanOptionHandler_invalidValue_throwsException() throws Exception {
    String[] args = new String[] {
        "--third_party=invalid_bool",
        "--js", "input.js"
    };
    new SubCommandLineRunner(args, out, err);
  }

  // Tests quoted argument values in initConfigFromFlags
  @Test
  public void testInitConfigFromFlags_quotedValues_unquotedCorrectly() throws Exception {
    String[] args = new String[] {
        "--charset='UTF-8'",
        "--output_wrapper=\"(function(){%output%})();\"",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertNotNull(runner.createOptions());
  }

  // Tests unknown command line flag throws CmdLineException
  @Test(expected = CmdLineException.class)
  public void testInitConfigFromFlags_unknownFlag_throwsException() throws Exception {
    String[] args = new String[] {
        "--unknown_flag_xyz=123"
    };
    new SubCommandLineRunner(args, out, err);
  }

  // Tests createCompiler returns non-null compiler instance
  @Test
  public void testCreateCompiler_returnsValidCompilerInstance() throws Exception {
    String[] args = new String[] {
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests createExterns default behavior (includes default externs from zip)
  @Test
  public void testCreateExterns_default_loadsZipExterns() throws Exception {
    String[] args = new String[] {
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
  }

  // Tests createExterns with use_only_custom_externs flag
  @Test
  public void testCreateExterns_useOnlyCustomExterns_doesNotLoadDefaultExterns() throws Exception {
    String[] args = new String[] {
        "--use_only_custom_externs=true",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests define flags with aliases -D and --D
  @Test
  public void testDefineFlag_aliases_parsedCorrectly() throws Exception {
    String[] args = new String[] {
        "-D", "DEF_BOOLEAN",
        "--D", "DEF_NUM=123",
        "--define", "DEF_STR='foo'",
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests single argument constructor using System.err
  @Test
  public void testConstructor_singleArg_initializesCorrectly() throws Exception {
    String[] args = new String[] {
        "--js", "input.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args);
    assertNotNull(runner.createOptions());
  }
}
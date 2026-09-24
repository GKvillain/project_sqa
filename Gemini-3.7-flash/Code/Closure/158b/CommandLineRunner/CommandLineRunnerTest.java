package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

  // Tests default initialization with valid minimal arguments
  @Test
  public void testInit_validArgs_shouldRunCompiler() {
    String[] args = new String[] {"--js=test.js"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests help flag causes shouldRunCompiler to be false and prints usage
  @Test
  public void testInit_helpFlag_shouldNotRunCompiler() {
    String[] args = new String[] {"--help"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("--help"));
  }

  // Tests invalid flag handling
  @Test
  public void testInit_invalidFlag_shouldNotRunCompiler() {
    String[] args = new String[] {"--unknown_flag_xyz"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests quotes handling in arguments
  @Test
  public void testProcessArgs_quotedValue_unquotesCorrectly() {
    String[] args = new String[] {"--define='FOO=true'", "--js=\"test.js\""};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests flagfile option reading from a temporary file
  @Test
  public void testProcessFlagFile_validFile_loadsFlags() throws IOException {
    File tempFlagFile = File.createTempFile("flags", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--js test.js --compilation_level WHITESPACE_ONLY".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  // Tests flagfile pointing to nonexistent file
  @Test
  public void testProcessFlagFile_nonExistentFile_handlesError() {
    String[] args = new String[] {"--flagfile=non_existent_file_12345.txt"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("read error"));
  }

  // Tests nested flagfile in flagfile is rejected
  @Test
  public void testProcessFlagFile_nestedFlagFile_rejected() throws IOException {
    File tempFlagFile = File.createTempFile("flags_nested", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--flagfile other.txt".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("cannot contain --flagfile"));
  }

  // Tests createOptions with default flags
  @Test
  public void testCreateOptions_defaultFlags_setsExpectedOptions() {
    String[] args = new String[] {"--js=test.js"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  // Tests createOptions with debug, generate_exports, and formatting flags
  @Test
  public void testCreateOptions_customFlags_appliedToOptions() {
    String[] args = new String[] {
      "--js=test.js",
      "--debug=true",
      "--generate_exports=true",
      "--formatting=PRETTY_PRINT",
      "--formatting=PRINT_INPUT_DELIMITER",
      "--process_closure_primitives=false"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertFalse(options.closurePass);
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests createOptions with compilation_level ADVANCED_OPTIMIZATIONS and warning_level VERBOSE
  @Test
  public void testCreateOptions_advancedAndVerbose_applied() {
    String[] args = new String[] {
      "--js=test.js",
      "--compilation_level=ADVANCED_OPTIMIZATIONS",
      "--warning_level=VERBOSE"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.checkGlobalThisLevel.isOn());
  }

  // Tests createCompiler creates non-null instance
  @Test
  public void testCreateCompiler_createsNonNullInstance() {
    String[] args = new String[] {"--js=test.js"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests getDefaultExterns loads required externs without failure
  @Test
  public void testGetDefaultExterns_returnsExpectedExterns() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
    assertEquals("externs.zip//es3.js", defaultExterns.get(0).getName());
  }

  // Tests createExterns with use_only_custom_externs flag
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlyCustom() throws Exception {
    String[] args = new String[] {
      "--js=test.js",
      "--use_only_custom_externs=true"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests BooleanOptionHandler parsing various true/false/implicit representations
  @Test
  public void testBooleanOptionHandler_variousInputs() throws Exception {
    final List<Boolean> assignedValues = new ArrayList<Boolean>();
    Setter<Boolean> setter = new Setter<Boolean>() {
      public Class<Boolean> getType() {
        return Boolean.class;
      }
      public boolean isMultiValued() {
        return false;
      }
      public void addValue(Boolean value) {
        assignedValues.add(value);
      }
    };

    CommandLineRunner.Flags.BooleanOptionHandler handler =
        new CommandLineRunner.Flags.BooleanOptionHandler(null, null, setter);

    // Case 1: param is null (implicit true)
    Parameters emptyParams = new Parameters() {
      public String getParameter(int idx) throws CmdLineException {
        throw new CmdLineException((CmdLineParser) null, "No param");
      }
      public int size() {
        return 0;
      }
    };
    assignedValues.clear();
    int consumed = handler.parseArguments(emptyParams);
    assertEquals(0, consumed);
    assertEquals(1, assignedValues.size());
    assertTrue(assignedValues.get(0));

    // Case 2: param is "false"
    Parameters falseParams = new Parameters() {
      public String getParameter(int idx) {
        return "false";
      }
      public int size() {
        return 1;
      }
    };
    assignedValues.clear();
    consumed = handler.parseArguments(falseParams);
    assertEquals(1, consumed);
    assertEquals(1, assignedValues.size());
    assertFalse(assignedValues.get(0));

    // Case 3: param is "true"
    Parameters trueParams = new Parameters() {
      public String getParameter(int idx) {
        return "true";
      }
      public int size() {
        return 1;
      }
    };
    assignedValues.clear();
    consumed = handler.parseArguments(trueParams);
    assertEquals(1, consumed);
    assertEquals(1, assignedValues.size());
    assertTrue(assignedValues.get(0));

    // Case 4: non-boolean value acts as next argument, sets true
    Parameters nonBoolParams = new Parameters() {
      public String getParameter(int idx) {
        return "other_file.js";
      }
      public int size() {
        return 1;
      }
    };
    assignedValues.clear();
    consumed = handler.parseArguments(nonBoolParams);
    assertEquals(0, consumed);
    assertEquals(1, assignedValues.size());
    assertTrue(assignedValues.get(0));

    assertNull(handler.getDefaultMetaVariable());
  }

  // Tests single-arg constructor of CommandLineRunner
  @Test
  public void testConstructor_singleArg_initializes() {
    String[] args = new String[] {"--js=test.js"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests version flag prints version to err stream
  @Test
  public void testInit_versionFlag_printsVersion() {
    String[] args = new String[] {"--version"};
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(errStream.toString().contains("Closure Compiler"));
  }
}
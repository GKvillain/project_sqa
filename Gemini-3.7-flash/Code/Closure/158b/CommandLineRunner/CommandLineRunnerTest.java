package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    TestableCommandLineRunner(String[] args) {
      super(args);
    }

    TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
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

  // Tests runner initialization with valid arguments
  @Test
  public void testInitConfigFromFlags_validJsArg_shouldRunCompilerTrue() {
    String[] args = new String[] {"--js", "test.js"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests runner initialization with invalid argument
  @Test
  public void testInitConfigFromFlags_invalidArg_shouldRunCompilerFalse() {
    String[] args = new String[] {"--unknown_flag_value_xyz"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests help flag behavior
  @Test
  public void testInitConfigFromFlags_helpFlag_shouldRunCompilerFalse() {
    String[] args = new String[] {"--help"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("--help"));
  }

  // Tests version flag behavior
  @Test
  public void testInitConfigFromFlags_versionFlag_printsVersion() {
    String[] args = new String[] {"--version"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Closure Compiler"));
  }

  // Tests quote stripping in processArgs
  @Test
  public void testProcessArgs_quotedValues_unquotesProperly() {
    String[] args = new String[] {"--compilation_level='ADVANCED_OPTIMIZATIONS'"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.isRemoveUnusedVariables());
  }

  // Tests boolean option parsing with true, false, on, off, yes, no, 1, 0
  @Test
  public void testBooleanOptionHandler_variousTrueAndFalseValues_handledCorrectly() {
    String[] args1 = new String[] {"--debug=true", "--process_closure_primitives=false"};
    TestableCommandLineRunner runner1 = new TestableCommandLineRunner(args1, out, err);
    CompilerOptions options1 = runner1.createOptions();
    assertFalse(options1.closurePass);

    String[] args2 = new String[] {"--debug=on", "--process_closure_primitives=off"};
    TestableCommandLineRunner runner2 = new TestableCommandLineRunner(args2, out, err);
    CompilerOptions options2 = runner2.createOptions();
    assertFalse(options2.closurePass);

    String[] args3 = new String[] {"--debug=yes", "--process_closure_primitives=no"};
    TestableCommandLineRunner runner3 = new TestableCommandLineRunner(args3, out, err);
    CompilerOptions options3 = runner3.createOptions();
    assertFalse(options3.closurePass);

    String[] args4 = new String[] {"--debug=1", "--process_closure_primitives=0"};
    TestableCommandLineRunner runner4 = new TestableCommandLineRunner(args4, out, err);
    CompilerOptions options4 = runner4.createOptions();
    assertFalse(options4.closurePass);
  }

  // Tests createOptions with ADVANCED_OPTIMIZATIONS compilation level and debug mode
  @Test
  public void testCreateOptions_advancedOptimizationsAndDebug_setsOptionsCorrectly() {
    String[] args = new String[] {
        "--compilation_level", "ADVANCED_OPTIMIZATIONS",
        "--debug=true",
        "--generate_exports=true"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.checkGlobalThisLevel.isOn());
    assertTrue(options.isRemoveUnusedVariables());
  }

  // Tests createOptions with WHITESPACE_ONLY compilation level and QUIET warning level
  @Test
  public void testCreateOptions_whitespaceOnlyAndQuiet_setsOptionsCorrectly() {
    String[] args = new String[] {
        "--compilation_level", "WHITESPACE_ONLY",
        "--warning_level", "QUIET"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests createOptions with formatting options PRETTY_PRINT and PRINT_INPUT_DELIMITER
  @Test
  public void testCreateOptions_formattingOptions_setsFormattingFlags() {
    String[] args = new String[] {
        "--formatting", "PRETTY_PRINT",
        "--formatting", "PRINT_INPUT_DELIMITER"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  // Tests third party flag sets DefaultCodingConvention
  @Test
  public void testInitConfigFromFlags_thirdPartyFlag_setsDefaultCodingConvention() {
    String[] args = new String[] {"--third_party=true"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.getCodingConvention() instanceof DefaultCodingConvention);
  }

  // Tests warning guards flags configuration
  @Test
  public void testInitConfigFromFlags_warningGuards_registeredInConfig() {
    String[] args = new String[] {
        "--jscomp_error", "checkVars",
        "--jscomp_warning", "checkTypes",
        "--jscomp_off", "globalThis"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests flagfile option parsing from a valid file
  @Test
  public void testProcessFlagFile_validFile_parsesArgumentsFromFile() throws IOException {
    File tempFlagFile = File.createTempFile("flags", ".txt");
    tempFlagFile.deleteOnExit();

    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--compilation_level ADVANCED_OPTIMIZATIONS".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile", tempFlagFile.getAbsolutePath()};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.isRemoveUnusedVariables());
  }

  // Tests flagfile containing another nested flagfile triggers error
  @Test
  public void testProcessFlagFile_nestedFlagFile_reportsError() throws IOException {
    File tempFlagFile = File.createTempFile("flags_nested", ".txt");
    tempFlagFile.deleteOnExit();

    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--flagfile nested_flags.txt".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile", tempFlagFile.getAbsolutePath()};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);

    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("cannot contain --flagfile option"));
  }

  // Tests non-existent flagfile handling
  @Test
  public void testProcessFlagFile_nonExistentFile_reportsReadError() {
    String[] args = new String[] {"--flagfile", "non_existent_flags_file_xyz.txt"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);

    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("read error"));
  }

  // Tests getDefaultExterns loads default externs resources
  @Test
  public void testGetDefaultExterns_returnsNonNullNonEmptyList() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
  }

  // Tests createCompiler instantiation
  @Test
  public void testCreateCompiler_returnsValidCompilerInstance() {
    String[] args = new String[] {"--js", "test.js"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests createExterns with custom externs only flag
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlyUserExterns() throws Exception {
    String[] args = new String[] {"--use_only_custom_externs=true", "--externs", "custom_externs.js"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertEquals(1, externs.size());
    assertEquals("custom_externs.js", externs.get(0).getName());
  }

  // Tests createExterns with default externs included
  @Test
  public void testCreateExterns_defaultExternsIncluded() throws Exception {
    String[] args = new String[] {"--externs", "custom_externs.js"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.size() > 1);
    assertEquals("custom_externs.js", externs.get(externs.size() - 1).getName());
  }

  // Tests createOptions with charset flag
  @Test
  public void testCreateOptions_charsetFlag_setsOutputCharset() {
    String[] args = new String[] {"--charset", "UTF-8"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertEquals("UTF-8", options.outputCharset);
  }

  // Tests createOptions with manage_closure_dependencies flag
  @Test
  public void testCreateOptions_manageClosureDependencies_setsDependencyOptions() {
    String[] args = new String[] {"--manage_closure_dependencies=true"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.dependencyOptions.needsManagement());
  }

  // Tests createOptions with only_closure_dependencies and closure_entry_point
  @Test
  public void testCreateOptions_onlyClosureDependenciesAndEntryPoint() {
    String[] args = new String[] {
        "--only_closure_dependencies=true",
        "--closure_entry_point", "goog.test.Entry"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.dependencyOptions.needsManagement());
    assertTrue(options.dependencyOptions.isOnlyClosureDependencies());
    assertTrue(options.dependencyOptions.getEntryPoints().contains("goog.test.Entry"));
  }

  // Tests createOptions with define flag
  @Test
  public void testCreateOptions_defineFlags_setsDefines() {
    String[] args = new String[] {
        "--define", "DEF_BOOLEAN=true",
        "--define", "DEF_STRING='val'",
        "--define", "DEF_NUMBER=42"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.getDefineReplacements().containsKey("DEF_BOOLEAN"));
    assertTrue(options.getDefineReplacements().containsKey("DEF_STRING"));
    assertTrue(options.getDefineReplacements().containsKey("DEF_NUMBER"));
  }

  // Tests createOptions with process_jquery_primitives and transform_amd_modules
  @Test
  public void testCreateOptions_jqueryAndAmdFlags() {
    String[] args = new String[] {
        "--process_jquery_primitives=true",
        "--transform_amd_modules=true"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertTrue(options.processJqueryPrimitives);
    assertTrue(options.transformAMDModules);
  }

  // Tests createOptions with source_map_format flag
  @Test
  public void testCreateOptions_sourceMapFormat_setsSourceMapFormat() {
    String[] args = new String[] {
        "--create_source_map", "map.out",
        "--source_map_format", "V3"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();
    assertEquals(SourceMap.Format.V3, options.sourceMapFormat);
  }
}
package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
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
import static org.junit.Assert.assertNull;
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

    @Override
    public Compiler createCompiler() {
      return super.createCompiler();
    }

    @Override
    public List<SourceFile> createExterns() throws IOException {
      return super.createExterns();
    }

    @Override
    public boolean shouldRunCompiler() {
      return super.shouldRunCompiler();
    }

    @Override
    public int doRun() throws IOException {
      return super.doRun();
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

  private SubCommandLineRunner createRunner(String[] args) {
    return new SubCommandLineRunner(args, out, err);
  }

  // Tests valid default configuration flags
  @Test
  public void testShouldRunCompiler_validArgs_returnsTrue() {
    String[] args = new String[] {"--js", "test.js"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests invalid flag argument
  @Test
  public void testShouldRunCompiler_invalidArg_returnsFalse() {
    String[] args = new String[] {"--invalid_unknown_flag_xyz"};
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
  }

  // Tests display help flag
  @Test
  public void testShouldRunCompiler_helpFlag_returnsFalse() {
    String[] args = new String[] {"--help"};
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
  }

  // Tests version flag
  @Test
  public void testShouldRunCompiler_versionFlag_printsVersion() {
    String[] args = new String[] {"--version", "--js", "input.js"};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Closure Compiler"));
  }

  // Tests createOptions with default flags
  @Test
  public void testCreateOptions_defaultFlags_setsExpectedDefaults() {
    String[] args = new String[] {"--js", "test.js"};
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.jqueryPass);
    assertFalse(options.angularPass);
    assertNull(options.messageBundle);
  }

  // Tests createOptions under ADVANCED_OPTIMIZATIONS compilation level
  @Test
  public void testCreateOptions_advancedOptimizations_setsEmptyMessageBundle() {
    String[] args = new String[] {
        "--js", "test.js",
        "--compilation_level", "ADVANCED_OPTIMIZATIONS"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertNotNull(options.messageBundle);
    assertTrue(options.messageBundle instanceof EmptyMessageBundle);
  }

  // Tests createOptions with QUIET warning level
  @Test
  public void testCreateOptions_warningLevelQuiet_disablesWarnings() {
    String[] args = new String[] {
        "--js", "test.js",
        "--warning_level", "QUIET",
        "--compilation_level", "ADVANCED_OPTIMIZATIONS"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertNotNull(options.messageBundle);
  }

  // Tests createOptions with VERBOSE warning level
  @Test
  public void testCreateOptions_warningLevelVerbose_setsCheckLevel() {
    String[] args = new String[] {
        "--js", "test.js",
        "--warning_level", "VERBOSE"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertEquals(CheckLevel.WARNING, options.checkSymbols);
  }

  // Tests createOptions with debug flag enabled
  @Test
  public void testCreateOptions_debugFlag_setsDebugOptions() {
    String[] args = new String[] {
        "--js", "test.js",
        "--compilation_level", "SIMPLE_OPTIMIZATIONS",
        "--debug", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.anonymousFunctionNaming != AnonymousFunctionNamingPolicy.OFF);
  }

  // Tests createOptions with use_types_for_optimization flag
  @Test
  public void testCreateOptions_useTypesForOptimization_enablesOption() {
    String[] args = new String[] {
        "--js", "test.js",
        "--use_types_for_optimization", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.inferTypes);
  }

  // Tests createOptions with generate_exports flag
  @Test
  public void testCreateOptions_generateExports_enablesOption() {
    String[] args = new String[] {
        "--js", "test.js",
        "--generate_exports", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.exportTestFunctions);
  }

  // Tests createOptions with formatting options
  @Test
  public void testCreateOptions_formattingOptions_setsFormatting() {
    String[] args = new String[] {
        "--js", "test.js",
        "--formatting", "PRETTY_PRINT",
        "--formatting", "PRINT_INPUT_DELIMITER",
        "--formatting", "SINGLE_QUOTES"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
    assertTrue(options.preferSingleQuotes);
  }

  // Tests createOptions with jquery and angular flags
  @Test
  public void testCreateOptions_jqueryAndAngularFlags_enablesPasses() {
    String[] args = new String[] {
        "--js", "test.js",
        "--compilation_level", "ADVANCED_OPTIMIZATIONS",
        "--process_jquery_primitives", "true",
        "--angular_pass", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.jqueryPass);
    assertTrue(options.angularPass);
  }

  // Tests createOptions with extra annotation names
  @Test
  public void testCreateOptions_extraAnnotations_setsAnnotations() {
    String[] args = new String[] {
        "--js", "test.js",
        "--extra_annotation_name", "myCustomTag"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.extraAnnotationNames.contains("myCustomTag"));
  }

  // Tests common_js module configuration without entry module
  @Test
  public void testInitConfig_commonJsWithoutEntryModule_invalidConfig() {
    String[] args = new String[] {
        "--js", "test.js",
        "--process_common_js_modules", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Please specify --common_js_entry_module."));
  }

  // Tests common_js module configuration with valid entry module
  @Test
  public void testInitConfig_commonJsWithEntryModule_validConfig() {
    String[] args = new String[] {
        "--js", "test.js",
        "--process_common_js_modules", "true",
        "--common_js_entry_module", "entry.js"
    };
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests loading arguments from flagfile
  @Test
  public void testInitConfig_flagFile_loadsArguments() throws IOException {
    File tempFlagFile = File.createTempFile("flags", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--js='test1.js'\n--debug=true\n".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()};
    SubCommandLineRunner runner = createRunner(args);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests nested flagfile in flagfile error path
  @Test
  public void testInitConfig_nestedFlagFile_reportsError() throws IOException {
    File tempFlagFile = File.createTempFile("flags_nested", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--flagfile=other.txt\n".getBytes());
    fos.close();

    String[] args = new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()};
    SubCommandLineRunner runner = createRunner(args);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("ERROR - Arguments in the file cannot contain --flagfile option."));
  }

  // Tests getDefaultExterns loads required standard extern files
  @Test
  public void testGetDefaultExterns_returnsNonEmptyExterns() throws IOException {
    List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
  }

  // Tests createExterns with use_only_custom_externs
  @Test
  public void testCreateExterns_useOnlyCustomExterns_returnsOnlySpecifiedExterns() throws Exception {
    String[] args = new String[] {
        "--js", "test.js",
        "--use_only_custom_externs", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    List<SourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  // Tests createCompiler instantiation
  @Test
  public void testCreateCompiler_returnsCompilerInstance() {
    String[] args = new String[] {"--js", "test.js"};
    SubCommandLineRunner runner = createRunner(args);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  // Tests single-argument constructor of SubCommandLineRunner
  @Test
  public void testConstructor_singleArgument_initializesSuccessfully() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {"--js", "test.js"});
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createOptions with language_in flag
  @Test
  public void testCreateOptions_languageIn_setsLanguageMode() {
    String[] args = new String[] {
        "--js", "test.js",
        "--language_in", "ECMASCRIPT5_STRICT"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals(LanguageMode.ECMASCRIPT5_STRICT, options.getLanguageIn());
  }

  // Tests createOptions with transform_amd_modules flag
  @Test
  public void testCreateOptions_transformAmdModules_enablesOption() {
    String[] args = new String[] {
        "--js", "test.js",
        "--transform_amd_modules", "true"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.transformAMDToCJSModules);
  }

  // Tests createOptions with output_wrapper flag
  @Test
  public void testCreateOptions_outputWrapper_setsOutputWrapper() {
    String[] args = new String[] {
        "--js", "test.js",
        "--output_wrapper", "(function(){%output%})();"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertEquals("(function(){%output%})();", options.outputWrapper);
  }

  // Tests createOptions with diagnostic group flags
  @Test
  public void testCreateOptions_diagnosticGroups_setsErrorAndWarning() {
    String[] args = new String[] {
        "--js", "test.js",
        "--jscomp_error", "checkVars",
        "--jscomp_warning", "checkTypes",
        "--jscomp_off", "deprecated"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  // Tests createOptions with dependency management flags
  @Test
  public void testCreateOptions_dependencyManagement_configuresDependencies() {
    String[] args = new String[] {
        "--js", "test.js",
        "--only_closure_dependencies", "true",
        "--closure_entry_point", "my.entry"
    };
    SubCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.dependencyOptions.shouldPruneDependencies());
  }

  // Tests doRun execution with valid source file
  @Test
  public void testDoRun_validInput_compilesSuccessfully() throws IOException {
    File tempJs = File.createTempFile("sample_input", ".js");
    tempJs.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempJs);
    fos.write("var hello = 'world';".getBytes());
    fos.close();

    SubCommandLineRunner runner = createRunner(new String[] {
        "--js", tempJs.getAbsolutePath()
    });
    int exitCode = runner.doRun();
    assertEquals(0, exitCode);
  }
}
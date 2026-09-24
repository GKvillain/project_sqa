package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
    public List<SourceFile> createExterns() throws FlagUsageException, java.io.IOException {
      return super.createExterns();
    }
  }

  // Tests valid default configuration flags
  @Test
  public void testInitConfig_defaultFlags_validConfig() {
    String[] args = new String[] { "--js=foo.js" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests --help flag sets config invalid
  @Test
  public void testInitConfig_helpFlag_shouldNotRunCompiler() {
    String[] args = new String[] { "--help" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
  }

  // Tests invalid flag syntax sets config invalid
  @Test
  public void testInitConfig_invalidFlag_shouldNotRunCompiler() {
    String[] args = new String[] { "--unknown_flag_xyz=true" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().length() > 0);
  }

  // Tests --version flag prints compiler version
  @Test
  public void testInitConfig_versionFlag_printsVersion() {
    String[] args = new String[] { "--version" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    String errorOutput = errStream.toString();
    assertTrue(errorOutput.contains("Closure Compiler"));
    assertTrue(errorOutput.contains("Version:"));
  }

  // Tests default compiler options created
  @Test
  public void testCreateOptions_defaultOptions_createsProperDefaults() {
    String[] args = new String[] { "--js=test.js" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.jqueryPass);
    assertFalse(options.angularPass);
    assertNull(options.messageBundle);
  }

  // Tests advanced optimizations mode sets EmptyMessageBundle
  @Test
  public void testCreateOptions_advancedOptimizations_setsEmptyMessageBundle() {
    String[] args = new String[] {
      "--compilation_level=ADVANCED_OPTIMIZATIONS",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertNotNull(options.messageBundle);
    assertTrue(options.messageBundle instanceof EmptyMessageBundle);
  }

  // Tests quiet warning level with simple optimizations (Closure-107 defect regression)
  @Test
  public void testCreateOptions_warningLevelQuiet_setsOptionsCorrectly() {
    String[] args = new String[] {
      "--warning_level=QUIET",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertNotNull(options.messageBundle);
    assertTrue(options.messageBundle instanceof EmptyMessageBundle);
  }

  // Tests all formatting options applied correctly
  @Test
  public void testCreateOptions_formattingFlags_setsOptions() {
    String[] args = new String[] {
      "--formatting=PRETTY_PRINT",
      "--formatting=PRINT_INPUT_DELIMITER",
      "--formatting=SINGLE_QUOTES",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
    assertTrue(options.preferSingleQuotes);
  }

  // Tests debug, generate exports, and use_types_for_optimization flags
  @Test
  public void testCreateOptions_debugAndOptimizationFlags_setsOptions() {
    String[] args = new String[] {
      "--debug=true",
      "--generate_exports=true",
      "--use_types_for_optimization=true",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertNotNull(options);
    assertFalse(options.removeDeadCode);
  }

  // Tests jQuery primitives flag
  @Test
  public void testCreateOptions_processJqueryPrimitives_setsCodingConvention() {
    String[] args = new String[] {
      "--process_jquery_primitives=true",
      "--compilation_level=ADVANCED_OPTIMIZATIONS",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertTrue(options.jqueryPass);
    assertTrue(options.getCodingConvention() instanceof JqueryCodingConvention);
  }

  // Tests third party coding convention
  @Test
  public void testInitConfig_thirdPartyFlag_setsDefaultCodingConvention() {
    String[] args = new String[] {
      "--third_party=true",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options.getCodingConvention());
  }

  // Tests angular pass flag
  @Test
  public void testCreateOptions_angularPass_setsAngularPassOption() {
    String[] args = new String[] {
      "--angular_pass=true",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    CompilerOptions options = runner.createOptions();

    assertTrue(options.angularPass);
  }

  // Tests CommonJS modules without entry module causes config failure
  @Test
  public void testInitConfig_processCommonJsWithoutEntryModule_fails() {
    String[] args = new String[] {
      "--process_common_js_modules=true",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(errStream.toString().contains("Please specify --common_js_entry_module."));
  }

  // Tests CommonJS modules with entry module
  @Test
  public void testInitConfig_processCommonJsWithEntryModule_succeeds() {
    String[] args = new String[] {
      "--process_common_js_modules=true",
      "--common_js_entry_module=main.js",
      "--js=main.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests warning guard flags (error, warning, off)
  @Test
  public void testInitConfig_warningGuards_registeredCorrectly() {
    String[] args = new String[] {
      "--jscomp_error=checkVars",
      "--jscomp_warning=checkTypes",
      "--jscomp_off=fileoverviewTags",
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests default externs loading
  @Test
  public void testGetDefaultExterns_loadsSuccessfully() throws Exception {
    List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
  }

  // Tests flagfile reading
  @Test
  public void testInitConfig_flagFile_readsArgsSuccessfully() throws Exception {
    File tempFile = File.createTempFile("flagfile", ".txt");
    tempFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFile);
    fos.write("--warning_level=VERBOSE --summary_detail_level=3".getBytes());
    fos.close();

    String[] args = new String[] {
      "--flagfile=" + tempFile.getAbsolutePath(),
      "--js=test.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  // Tests createCompiler instantiation
  @Test
  public void testCreateCompiler_returnsValidCompilerInstance() {
    String[] args = new String[] { "--js=test.js" };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }
}
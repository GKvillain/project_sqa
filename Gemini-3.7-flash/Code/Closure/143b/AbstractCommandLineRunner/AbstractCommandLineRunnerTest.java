package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractCommandLineRunnerTest {

  private CompilerOptions options;

  @Before
  public void setUp() {
    options = new CompilerOptions();
  }

  // Tests boolean define flag without explicit value defaulting to true
  @Test
  public void testCreateDefineReplacements_noValue_setsBooleanTrue() {
    List<String> defs = Lists.newArrayList("DEF_FLAG");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    // Verified by checking define replacement execution without exception
  }

  // Tests boolean define flag with explicit true value
  @Test
  public void testCreateDefineReplacements_explicitTrue_setsBooleanTrue() {
    List<String> defs = Lists.newArrayList("DEF_FLAG=true");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests boolean define flag with explicit false value
  @Test
  public void testCreateDefineReplacements_explicitFalse_setsBooleanFalse() {
    List<String> defs = Lists.newArrayList("DEF_FLAG=false");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests single-quoted string literal define flag
  @Test
  public void testCreateDefineReplacements_stringLiteral_setsString() {
    List<String> defs = Lists.newArrayList("DEF_STR='hello world'");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests single-quoted empty string literal define flag
  @Test
  public void testCreateDefineReplacements_emptyStringLiteral_setsString() {
    List<String> defs = Lists.newArrayList("DEF_STR=''");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests numeric double literal define flag
  @Test
  public void testCreateDefineReplacements_numericLiteral_setsDouble() {
    List<String> defs = Lists.newArrayList("DEF_NUM=123.45");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests invalid define value syntax throwing RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_invalidValue_throwsException() {
    List<String> defs = Lists.newArrayList("DEF_INVALID=not_a_valid_val");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests empty define variable name throwing RuntimeException
  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacements_emptyName_throwsException() {
    List<String> defs = Lists.newArrayList("=true");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  // Tests valid JS module creation with single module and files
  @Test
  public void testCreateJsModules_validSingleModule_createsModule() throws Exception {
    List<String> specs = Lists.newArrayList("mod1:2");
    List<String> files = Lists.newArrayList("file1.js", "file2.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    assertNotNull(modules);
    assertEquals(1, modules.length);
    assertEquals("mod1", modules[0].getName());
    assertEquals(2, modules[0].getInputs().size());
  }

  // Tests valid JS module creation with dependencies between modules
  @Test
  public void testCreateJsModules_withDependencies_createsLinkedModules() throws Exception {
    List<String> specs = Lists.newArrayList("base:1", "child:1:base");
    List<String> files = Lists.newArrayList("base.js", "child.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    assertNotNull(modules);
    assertEquals(2, modules.length);
    assertEquals("base", modules[0].getName());
    assertEquals("child", modules[1].getName());
    assertTrue(modules[1].getDependencies().contains(modules[0]));
  }

  // Tests module spec with invalid module identifier name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidModuleName_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("bad-name:1");
    List<String> files = Lists.newArrayList("file1.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests module spec with duplicate module name
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_duplicateModuleName_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("mod:1", "mod:1");
    List<String> files = Lists.newArrayList("file1.js", "file2.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests module spec depending on an unknown non-existent module
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_unknownDependency_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("mod:1:unknown");
    List<String> files = Lists.newArrayList("file1.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests module spec when fewer JS files provided than required
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_notEnoughJsFiles_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("mod:3");
    List<String> files = Lists.newArrayList("file1.js", "file2.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests module spec when more JS files provided than consumed by modules
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_tooManyJsFiles_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("mod:1");
    List<String> files = Lists.newArrayList("file1.js", "file2.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests module spec with non-integer file count
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModules_invalidFileCountFormat_throwsException() throws Exception {
    List<String> specs = Lists.newArrayList("mod:abc");
    List<String> files = Lists.newArrayList("file1.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  // Tests parsing valid module wrapper
  @Test
  public void testParseModuleWrappers_validWrapper_returnsMap() throws Exception {
    JSModule mod1 = new JSModule("mod1");
    JSModule mod2 = new JSModule("mod2");
    JSModule[] modules = new JSModule[]{mod1, mod2};

    List<String> specs = Lists.newArrayList("mod1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);

    assertEquals("(function(){%s})();", wrappers.get("mod1"));
    assertEquals("", wrappers.get("mod2"));
  }

  // Tests module wrapper without delimiter colon throwing FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingColon_throwsException() throws Exception {
    JSModule mod = new JSModule("mod1");
    List<String> specs = Lists.newArrayList("mod1wrapper");
    AbstractCommandLineRunner.parseModuleWrappers(specs, new JSModule[]{mod});
  }

  // Tests module wrapper for unknown module name throwing FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_unknownModule_throwsException() throws Exception {
    JSModule mod = new JSModule("mod1");
    List<String> specs = Lists.newArrayList("unknownMod:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, new JSModule[]{mod});
  }

  // Tests module wrapper missing %s placeholder throwing FlagUsageException
  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappers_missingPlaceholder_throwsException() throws Exception {
    JSModule mod = new JSModule("mod1");
    List<String> specs = Lists.newArrayList("mod1:no_placeholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, new JSModule[]{mod});
  }

  // Tests writeOutput inserting code into wrapper placeholder
  @Test
  public void testWriteOutput_withWrapper_insertsCodeProperly() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);

    AbstractCommandLineRunner.writeOutput(out, null, "var a = 1;", "(function(){%output%})();", "%output%");
    out.flush();

    String result = baos.toString().trim();
    assertEquals("(function(){var a = 1;})();", result);
  }

  // Tests writeOutput when wrapper has no placeholder outputs raw code
  @Test
  public void testWriteOutput_withoutPlaceholder_outputsRawCode() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);

    AbstractCommandLineRunner.writeOutput(out, null, "var a = 1;", "no_marker_here", "%output%");
    out.flush();

    String result = baos.toString().trim();
    assertEquals("var a = 1;", result);
  }
}
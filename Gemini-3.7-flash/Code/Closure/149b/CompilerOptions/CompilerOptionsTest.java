package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CompilerOptions}.
 */
public class CompilerOptionsTest {

  private CompilerOptions options;

  @Before
  public void setUp() {
    options = new CompilerOptions();
  }

  // Tests default constructor field initializations
  @Test
  public void testConstructor_defaultValues_initializedCorrectly() {
    assertFalse(options.skipAllPasses);
    assertFalse(options.nameAnonymousFunctionsOnly);
    assertEquals(CompilerOptions.DevMode.OFF, options.devMode);
    assertFalse(options.checkSymbols);
    assertEquals(CheckLevel.OFF, options.checkShadowVars);
    assertEquals(CheckLevel.ERROR, options.brokenClosureRequiresLevel);
    assertEquals(CheckLevel.OFF, options.checkGlobalThisLevel);
    assertFalse(options.foldConstants);
    assertEquals(VariableRenamingPolicy.OFF, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.OFF, options.propertyRenaming);
    assertFalse(options.prettyPrint);
    assertFalse(options.lineBreak);
    assertFalse(options.isExternExportsEnabled());
    assertNull(options.getWarningsGuard());
  }

  // Tests boolean defines replacement mapping (both true and false branches)
  @Test
  public void testGetDefineReplacements_booleanValues_mapsToBooleanNodes() {
    options.setDefineToBooleanLiteral("ENABLE_FEATURE", true);
    options.setDefineToBooleanLiteral("DISABLE_FEATURE", false);

    Map<String, Node> replacements = options.getDefineReplacements();
    assertEquals(2, replacements.size());

    Node trueNode = replacements.get("ENABLE_FEATURE");
    assertNotNull(trueNode);
    assertEquals(Token.TRUE, trueNode.getType());

    Node falseNode = replacements.get("DISABLE_FEATURE");
    assertNotNull(falseNode);
    assertEquals(Token.FALSE, falseNode.getType());
  }

  // Tests integer number defines replacement mapping
  @Test
  public void testGetDefineReplacements_integerNumber_mapsToNumberNode() {
    options.setDefineToNumberLiteral("MAX_LIMIT", 100);

    Map<String, Node> replacements = options.getDefineReplacements();
    assertEquals(1, replacements.size());

    Node numberNode = replacements.get("MAX_LIMIT");
    assertNotNull(numberNode);
    assertEquals(Token.NUMBER, numberNode.getType());
    assertEquals(100.0, numberNode.getDouble(), 0.0);
  }

  // Tests double number defines replacement mapping
  @Test
  public void testGetDefineReplacements_doubleNumber_mapsToNumberNode() {
    options.setDefineToDoubleLiteral("RATIO", 2.5);

    Map<String, Node> replacements = options.getDefineReplacements();
    assertEquals(1, replacements.size());

    Node doubleNode = replacements.get("RATIO");
    assertNotNull(doubleNode);
    assertEquals(Token.NUMBER, doubleNode.getType());
    assertEquals(2.5, doubleNode.getDouble(), 0.0);
  }

  // Tests string defines replacement mapping
  @Test
  public void testGetDefineReplacements_stringValue_mapsToStringNode() {
    options.setDefineToStringLiteral("APP_NAME", "ClosureApp");

    Map<String, Node> replacements = options.getDefineReplacements();
    assertEquals(1, replacements.size());

    Node stringNode = replacements.get("APP_NAME");
    assertNotNull(stringNode);
    assertEquals(Token.STRING, stringNode.getType());
    assertEquals("ClosureApp", stringNode.getString());
  }

  // Tests skipping all compiler passes flag
  @Test
  public void testSkipAllCompilerPasses_invocation_setsSkipAllPassesTrue() {
    assertFalse(options.skipAllPasses);
    options.skipAllCompilerPasses();
    assertTrue(options.skipAllPasses);
  }

  // Tests setting warning level and guard composition
  @Test
  public void testSetWarningLevel_diagnosticGroup_addsGuard() {
    assertNull(options.getWarningsGuard());
    DiagnosticGroup group = DiagnosticGroups.NON_STANDARD_JSDOC;
    options.setWarningLevel(group, CheckLevel.WARNING);

    assertNotNull(options.getWarningsGuard());
    assertTrue(options.enables(group));
    assertFalse(options.disables(group));
  }

  // Tests composing multiple warning guards
  @Test
  public void testAddWarningsGuard_multipleGuards_composesCorrectly() {
    DiagnosticGroup group1 = DiagnosticGroups.NON_STANDARD_JSDOC;
    DiagnosticGroup group2 = DiagnosticGroups.ACCESS_CONTROLS;

    options.addWarningsGuard(new DiagnosticGroupWarningsGuard(group1, CheckLevel.ERROR));
    options.addWarningsGuard(new DiagnosticGroupWarningsGuard(group2, CheckLevel.OFF));

    assertTrue(options.enables(group1));
    assertTrue(options.disables(group2));
  }

  // Tests variable and property renaming policy configuration
  @Test
  public void testSetRenamingPolicy_customPolicies_setsBothFields() {
    options.setRenamingPolicy(
        VariableRenamingPolicy.LOCAL,
        PropertyRenamingPolicy.ALL_UNQUOTED);

    assertEquals(VariableRenamingPolicy.LOCAL, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.ALL_UNQUOTED, options.propertyRenaming);
  }

  // Tests runtime type check enabling and disabling
  @Test
  public void testRuntimeTypeCheck_enableAndDisable_togglesState() {
    assertFalse(options.runtimeTypeCheck);
    assertNull(options.runtimeTypeCheckLogFunction);

    options.enableRuntimeTypeCheck("customLogFunc");
    assertTrue(options.runtimeTypeCheck);
    assertEquals("customLogFunc", options.runtimeTypeCheckLogFunction);

    options.disableRuntimeTypeCheck();
    assertFalse(options.runtimeTypeCheck);
  }

  // Tests colorize error output getter and setter
  @Test
  public void testColorizeErrorOutput_setAndGet_returnsSetValue() {
    assertFalse(options.shouldColorizeErrorOutput());
    options.setColorizeErrorOutput(true);
    assertTrue(options.shouldColorizeErrorOutput());
    options.setColorizeErrorOutput(false);
    assertFalse(options.shouldColorizeErrorOutput());
  }

  // Tests coding convention getter and setter
  @Test
  public void testCodingConvention_setAndGet_returnsConvention() {
    CodingConvention convention = new DefaultCodingConvention();
    options.setCodingConvention(convention);
    assertEquals(convention, options.getCodingConvention());
  }

  // Tests extern exports enablement getter and setter
  @Test
  public void testExternExports_setAndGet_returnsExpected() {
    assertFalse(options.isExternExportsEnabled());
    options.enableExternExports(true);
    assertTrue(options.isExternExportsEnabled());
    options.enableExternExports(false);
    assertFalse(options.isExternExportsEnabled());
  }

  // Tests string replacement configuration helper
  @Test
  public void testSetReplaceStringsConfiguration_validInputs_setsFields() {
    List<String> descriptors = Lists.newArrayList("func(1)", "func(2)");
    options.setReplaceStringsConfiguration("TOKEN", descriptors);

    assertEquals("TOKEN", options.replaceStringsPlaceholderToken);
    assertEquals(descriptors, options.replaceStringsFunctionDescriptions);
  }

  // Tests id generators setting
  @Test
  public void testSetIdGenerators_setOfStrings_copiesSet() {
    Set<String> ids = ImmutableSet.of("gen1", "gen2");
    options.setIdGenerators(ids);
    assertEquals(ids, options.idGenerators);
  }

  // Tests miscellaneous setters
  @Test
  public void testMiscellaneousSetters_variousValues_updatesState() {
    options.setCollapsePropertiesOnExternTypes(true);
    assertTrue(options.collapsePropertiesOnExternTypes);

    options.setProcessObjectPropertyString(true);
    assertTrue(options.processObjectPropertyString);

    options.setRewriteNewDateGoogNow(false);
    assertFalse(options.rewriteNewDateGoogNow);

    options.setRemoveAbstractMethods(false);
    assertFalse(options.removeAbstractMethods);

    options.setNameAnonymousFunctionsOnly(true);
    assertTrue(options.nameAnonymousFunctionsOnly);

    options.setChainCalls(true);
    assertTrue(options.chainCalls);

    options.setManageClosureDependencies(true);
    assertTrue(options.manageClosureDependencies);

    options.setSummaryDetailLevel(3);
    assertEquals(3, options.summaryDetailLevel);

    options.setLooseTypes(true);
    assertTrue(options.looseTypes);
  }

  // Tests cloning of compiler options
  @Test
  public void testClone_clonedInstance_hasEqualFieldValues() throws Exception {
    options.prettyPrint = true;
    options.lineBreak = true;
    options.checkSymbols = true;

    CompilerOptions clone = (CompilerOptions) options.clone();
    assertNotNull(clone);
    assertTrue(clone.prettyPrint);
    assertTrue(clone.lineBreak);
    assertTrue(clone.checkSymbols);
  }

  // Tests TracerMode enum isOn behavior
  @Test
  public void testTracerMode_isOn_returnsExpectedBoolean() {
    assertTrue(CompilerOptions.TracerMode.ALL.isOn());
    assertTrue(CompilerOptions.TracerMode.FAST.isOn());
    assertFalse(CompilerOptions.TracerMode.OFF.isOn());
  }
}
package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CompilerOptionsTest {

  private CompilerOptions options;

  @Before
  public void setUp() {
    options = new CompilerOptions();
  }

  // Tests default configuration values initialized in constructor
  @Test
  public void testConstructor_defaultValues_initializedCorrectly() {
    assertFalse(options.skipAllPasses);
    assertFalse(options.nameAnonymousFunctionsOnly);
    assertEquals(CompilerOptions.DevMode.OFF, options.devMode);
    assertFalse(options.checkSymbols);
    assertEquals(CheckLevel.OFF, options.checkShadowVars);
    assertEquals(CheckLevel.OFF, options.aggressiveVarCheck);
    assertEquals(CheckLevel.ERROR, options.brokenClosureRequiresLevel);
    assertFalse(options.closurePass);
    assertTrue(options.rewriteNewDateGoogNow);
    assertTrue(options.removeAbstractMethods);
    assertEquals(VariableRenamingPolicy.OFF, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.OFF, options.propertyRenaming);
    assertEquals(CompilerOptions.TracerMode.OFF, options.tracer);
    assertEquals(ErrorFormat.SINGLELINE, options.errorFormat);
    assertEquals(1, options.summaryDetailLevel);
    assertNull(options.getWarningsGuard());
  }

  // Tests define replacements with boolean literals (both true and false branches)
  @Test
  public void testSetDefineToBooleanLiteral_trueAndFalse_createsCorrectNodes() {
    options.setDefineToBooleanLiteral("FLAG_TRUE", true);
    options.setDefineToBooleanLiteral("FLAG_FALSE", false);

    Map<String, Node> defines = options.getDefineReplacements();
    assertEquals(2, defines.size());

    Node trueNode = defines.get("FLAG_TRUE");
    assertNotNull(trueNode);
    assertEquals(Token.TRUE, trueNode.getType());

    Node falseNode = defines.get("FLAG_FALSE");
    assertNotNull(falseNode);
    assertEquals(Token.FALSE, falseNode.getType());
  }

  // Tests define replacements with integer, double, and string literals
  @Test
  public void testSetDefineToLiteral_numericAndStringTypes_createsCorrectNodes() {
    options.setDefineToNumberLiteral("INT_VAL", 42);
    options.setDefineToDoubleLiteral("DBL_VAL", 3.14);
    options.setDefineToStringLiteral("STR_VAL", "closure");

    Map<String, Node> defines = options.getDefineReplacements();
    assertEquals(3, defines.size());

    Node intNode = defines.get("INT_VAL");
    assertNotNull(intNode);
    assertEquals(Token.NUMBER, intNode.getType());
    assertEquals(42.0, intNode.getDouble(), 0.0);

    Node dblNode = defines.get("DBL_VAL");
    assertNotNull(dblNode);
    assertEquals(Token.NUMBER, dblNode.getType());
    assertEquals(3.14, dblNode.getDouble(), 0.0001);

    Node strNode = defines.get("STR_VAL");
    assertNotNull(strNode);
    assertEquals(Token.STRING, strNode.getType());
    assertEquals("closure", strNode.getString());
  }

  // Tests skipAllCompilerPasses setter
  @Test
  public void testSkipAllCompilerPasses_setsSkipAllPassesToTrue() {
    assertFalse(options.skipAllPasses);
    options.skipAllCompilerPasses();
    assertTrue(options.skipAllPasses);
  }

  // Tests setWarningLevel and guard checking methods
  @Test
  public void testSetWarningLevel_diagnosticGroup_updatesWarningsGuard() {
    DiagnosticGroup testGroup = DiagnosticGroups.NON_STANDARD_JSDOC;
    assertFalse(options.enables(testGroup));
    assertFalse(options.disables(testGroup));

    options.setWarningLevel(testGroup, CheckLevel.WARNING);
    assertNotNull(options.getWarningsGuard());
    assertTrue(options.enables(testGroup));
    assertFalse(options.disables(testGroup));

    options.setWarningLevel(testGroup, CheckLevel.OFF);
    assertTrue(options.disables(testGroup));
  }

  // Tests adding multiple warning guards
  @Test
  public void testAddWarningsGuard_multipleGuards_composedCorrectly() {
    DiagnosticGroup group1 = DiagnosticGroups.NON_STANDARD_JSDOC;
    DiagnosticGroup group2 = DiagnosticGroups.ACCESS_CONTROLS;

    options.addWarningsGuard(new DiagnosticGroupWarningsGuard(group1, CheckLevel.WARNING));
    options.addWarningsGuard(new DiagnosticGroupWarningsGuard(group2, CheckLevel.ERROR));

    assertTrue(options.enables(group1));
    assertTrue(options.enables(group2));
  }

  // Tests enables and disables when no guard is present (null branch)
  @Test
  public void testEnablesAndDisables_nullGuard_returnsFalse() {
    assertNull(options.getWarningsGuard());
    assertFalse(options.enables(DiagnosticGroups.ACCESS_CONTROLS));
    assertFalse(options.disables(DiagnosticGroups.ACCESS_CONTROLS));
  }

  // Tests renaming policy setter
  @Test
  public void testSetRenamingPolicy_validPolicies_setsCorrectly() {
    options.setRenamingPolicy(
        VariableRenamingPolicy.ALL,
        PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC);
    assertEquals(VariableRenamingPolicy.ALL, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC, options.propertyRenaming);
  }

  // Tests runtime type check enable and disable
  @Test
  public void testEnableAndDisableRuntimeTypeCheck_toggleState_updatesCorrectly() {
    assertFalse(options.runtimeTypeCheck);
    assertNull(options.runtimeTypeCheckLogFunction);

    options.enableRuntimeTypeCheck("logError");
    assertTrue(options.runtimeTypeCheck);
    assertEquals("logError", options.runtimeTypeCheckLogFunction);

    options.disableRuntimeTypeCheck();
    assertFalse(options.runtimeTypeCheck);
  }

  // Tests colorize error output getter and setter
  @Test
  public void testSetColorizeErrorOutput_booleanValues_returnsExpected() {
    assertFalse(options.shouldColorizeErrorOutput());

    options.setColorizeErrorOutput(true);
    assertTrue(options.shouldColorizeErrorOutput());

    options.setColorizeErrorOutput(false);
    assertFalse(options.shouldColorizeErrorOutput());
  }

  // Tests coding convention getter and setter
  @Test
  public void testSetCodingConvention_customConvention_storedAndRetrieved() {
    assertNull(options.getCodingConvention());
    CodingConvention convention = new ClosureCodingConvention();
    options.setCodingConvention(convention);
    assertEquals(convention, options.getCodingConvention());
  }

  // Tests extern exports enablement toggle
  @Test
  public void testEnableExternExports_booleanValues_updatesState() {
    assertFalse(options.isExternExportsEnabled());

    options.enableExternExports(true);
    assertTrue(options.isExternExportsEnabled());

    options.enableExternExports(false);
    assertFalse(options.isExternExportsEnabled());
  }

  // Tests id generators and replace strings configuration
  @Test
  public void testSetIdGeneratorsAndReplaceStrings_validInputs_setsValues() {
    Set<String> idGens = Sets.newHashSet("gen1", "gen2");
    options.setIdGenerators(idGens);
    assertEquals(idGens, options.idGenerators);

    List<String> descriptors = Lists.newArrayList("desc1", "desc2");
    options.setReplaceStringsConfiguration("TOKEN", descriptors);
    assertEquals("TOKEN", options.replaceStringsPlaceholderToken);
    assertEquals(descriptors, options.replaceStringsFunctionDescriptions);
  }

  // Tests miscellaneous boolean and integer configuration setters
  @Test
  public void testMiscellaneousSetters_validInputs_setsFieldsCorrectly() {
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

  // Tests cloning of CompilerOptions object
  @Test
  public void testClone_clonedInstance_hasSameFieldValues() throws Exception {
    options.setDefineToBooleanLiteral("MY_FLAG", true);
    options.setSummaryDetailLevel(2);
    options.checkSymbols = true;

    CompilerOptions cloned = (CompilerOptions) options.clone();
    assertNotNull(cloned);
    assertEquals(options.checkSymbols, cloned.checkSymbols);
    assertEquals(options.summaryDetailLevel, cloned.summaryDetailLevel);
  }

  // Tests TracerMode isOn method
  @Test
  public void testTracerMode_isOn_returnsCorrectBoolean() {
    assertTrue(CompilerOptions.TracerMode.ALL.isOn());
    assertTrue(CompilerOptions.TracerMode.FAST.isOn());
    assertFalse(CompilerOptions.TracerMode.OFF.isOn());
  }

  // Tests tweak replacements for boolean, number, double, and string literals
  @Test
  public void testSetTweakToLiteral_allLiteralTypes_createsCorrectNodes() {
    options.setTweakToBooleanLiteral("tweak.bool.true", true);
    options.setTweakToBooleanLiteral("tweak.bool.false", false);
    options.setTweakToNumberLiteral("tweak.int", 100);
    options.setTweakToDoubleLiteral("tweak.double", 2.718);
    options.setTweakToStringLiteral("tweak.str", "hello");

    Map<String, Node> tweaks = options.getTweakReplacements();
    assertEquals(5, tweaks.size());

    assertEquals(Token.TRUE, tweaks.get("tweak.bool.true").getType());
    assertEquals(Token.FALSE, tweaks.get("tweak.bool.false").getType());

    Node numNode = tweaks.get("tweak.int");
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(100.0, numNode.getDouble(), 0.0);

    Node dblNode = tweaks.get("tweak.double");
    assertEquals(Token.NUMBER, dblNode.getType());
    assertEquals(2.718, dblNode.getDouble(), 0.0001);

    Node strNode = tweaks.get("tweak.str");
    assertEquals(Token.STRING, strNode.getType());
    assertEquals("hello", strNode.getString());
  }

  // Tests resetWarningsGuard method
  @Test
  public void testResetWarningsGuard_clearsExistingGuards() {
    DiagnosticGroup group = DiagnosticGroups.ACCESS_CONTROLS;
    options.setWarningLevel(group, CheckLevel.ERROR);
    assertNotNull(options.getWarningsGuard());
    assertTrue(options.enables(group));

    options.resetWarningsGuard();
    assertNull(options.getWarningsGuard());
    assertFalse(options.enables(group));
  }

  // Tests language mode setters and getters
  @Test
  public void testLanguageMode_setAndGet_updatesCorrectly() {
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5, options.getLanguageIn());

    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT3);
    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT3, options.getLanguageOut());
  }

  // Tests tweak processing setter and getter
  @Test
  public void testTweakProcessing_setAndGet_updatesCorrectly() {
    options.setTweakProcessing(CompilerOptions.TweakProcessing.STRIP);
    assertEquals(CompilerOptions.TweakProcessing.STRIP, options.getTweakProcessing());

    options.setTweakProcessing(CompilerOptions.TweakProcessing.CHECK);
    assertEquals(CompilerOptions.TweakProcessing.CHECK, options.getTweakProcessing());
  }

  // Tests manageClosureDependencies with entry points list
  @Test
  public void testSetManageClosureDependencies_withEntryPointsList_setsFields() {
    List<String> entryPoints = Lists.newArrayList("goog.dom", "goog.events");
    options.setManageClosureDependencies(entryPoints);
    assertTrue(options.manageClosureDependencies);
    assertEquals(entryPoints, options.manageClosureDependenciesEntryPoints);
  }

  // Tests reach enum values for inline functions and variables
  @Test
  public void testReach_values_accessible() {
    CompilerOptions.Reach allReach = CompilerOptions.Reach.ALL;
    CompilerOptions.Reach localReach = CompilerOptions.Reach.LOCAL;
    CompilerOptions.Reach noneReach = CompilerOptions.Reach.NONE;

    assertEquals(CompilerOptions.Reach.ALL, allReach);
    assertEquals(CompilerOptions.Reach.LOCAL, localReach);
    assertEquals(CompilerOptions.Reach.NONE, noneReach);
  }

  // Tests DevMode enum values
  @Test
  public void testDevMode_values_accessible() {
    assertEquals(CompilerOptions.DevMode.OFF, CompilerOptions.DevMode.valueOf("OFF"));
    assertEquals(CompilerOptions.DevMode.START, CompilerOptions.DevMode.valueOf("START"));
    assertEquals(CompilerOptions.DevMode.START_AND_END, CompilerOptions.DevMode.valueOf("START_AND_END"));
  }
}
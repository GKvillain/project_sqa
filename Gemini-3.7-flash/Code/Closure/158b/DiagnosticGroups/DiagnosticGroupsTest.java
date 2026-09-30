package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DiagnosticGroupsTest {

  private DiagnosticGroups diagnosticGroups;
  private CompilerOptions options;

  @Before
  public void setUp() {
    diagnosticGroups = new DiagnosticGroups();
    options = new CompilerOptions();
  }

  // Tests forName returns correct group for standard registered name
  @Test
  public void testForName_validRegisteredName_returnsDiagnosticGroup() {
    DiagnosticGroup group = diagnosticGroups.forName("globalThis");
    assertNotNull(group);
    assertSame(DiagnosticGroups.GLOBAL_THIS, group);
  }

  // Tests forName returns null for unknown name
  @Test
  public void testForName_unknownName_returnsNull() {
    DiagnosticGroup group = diagnosticGroups.forName("nonExistentGroupName");
    assertNull(group);
  }

  // Tests getRegisteredGroups contains pre-registered static groups
  @Test
  public void testGetRegisteredGroups_defaultState_containsStandardGroups() {
    Map<String, DiagnosticGroup> groups = diagnosticGroups.getRegisteredGroups();
    assertNotNull(groups);
    assertFalse(groups.isEmpty());
    assertTrue(groups.containsKey("globalThis"));
    assertTrue(groups.containsKey("checkVars"));
    assertTrue(groups.containsKey("checkTypes"));
    assertTrue(groups.containsKey("visibility"));
    assertTrue(groups.containsKey("deprecated"));
    assertSame(DiagnosticGroups.CHECK_TYPES, groups.get("checkTypes"));
  }

  // Tests immutability of map returned by getRegisteredGroups
  @Test(expected = UnsupportedOperationException.class)
  public void testGetRegisteredGroups_modifyReturnedMap_throwsException() {
    Map<String, DiagnosticGroup> groups = diagnosticGroups.getRegisteredGroups();
    groups.put("customGroup", new DiagnosticGroup("customGroup"));
  }

  // Tests registerGroup with DiagnosticType array overload
  @Test
  public void testRegisterGroup_withDiagnosticTypes_registersSuccessfully() {
    DiagnosticType type = DiagnosticType.error("TEST_ERR", "Test description");
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("customTypeGroup", type);

    assertNotNull(registered);
    assertSame(registered, diagnosticGroups.forName("customTypeGroup"));
    assertTrue(registered.matches(type));
  }

  // Tests registerGroup with nested DiagnosticGroup array overload
  @Test
  public void testRegisterGroup_withSubGroups_registersSuccessfully() {
    DiagnosticGroup groupA = new DiagnosticGroup("groupA");
    DiagnosticGroup groupB = new DiagnosticGroup("groupB");
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("compositeGroup", groupA, groupB);

    assertNotNull(registered);
    assertSame(registered, diagnosticGroups.forName("compositeGroup"));
  }

  // Tests registerGroup with single DiagnosticGroup overload
  @Test
  public void testRegisterGroup_withExistingGroup_registersSuccessfully() {
    DiagnosticGroup directGroup = new DiagnosticGroup("directGroup");
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("directGroupName", directGroup);

    assertNotNull(registered);
    assertSame(directGroup, registered);
    assertSame(directGroup, diagnosticGroups.forName("directGroupName"));
  }

  // Tests setWarningLevels with valid group names and ERROR level
  @Test
  public void testSetWarningLevels_validGroups_setsWarningLevelInOptions() {
    diagnosticGroups.setWarningLevels(
        options,
        Arrays.asList("globalThis", "visibility"),
        CheckLevel.ERROR);
  }

  // Tests setWarningLevels with WARNING level
  @Test
  public void testSetWarningLevels_warningLevel_setsWarningLevelInOptions() {
    diagnosticGroups.setWarningLevels(
        options,
        Collections.singletonList("deprecated"),
        CheckLevel.WARNING);
  }

  // Tests setWarningLevels with OFF level
  @Test
  public void testSetWarningLevels_offLevel_setsWarningLevelInOptions() {
    diagnosticGroups.setWarningLevels(
        options,
        Collections.singletonList("checkVars"),
        CheckLevel.OFF);
  }

  // Tests setWarningLevels with empty list does not modify options
  @Test
  public void testSetWarningLevels_emptyList_noModification() {
    diagnosticGroups.setWarningLevels(
        options,
        Collections.<String>emptyList(),
        CheckLevel.ERROR);
  }

  // Tests setWarningLevels throws NullPointerException when group name is unknown
  @Test(expected = NullPointerException.class)
  public void testSetWarningLevels_unknownGroupName_throwsNullPointerException() {
    diagnosticGroups.setWarningLevels(
        options,
        Collections.singletonList("unknownInvalidGroup"),
        CheckLevel.ERROR);
  }

  // Tests all static diagnostic groups are initialized properly
  @Test
  public void testStaticDiagnosticGroups_initialization_allNotNull() {
    assertNotNull(DiagnosticGroups.GLOBAL_THIS);
    assertNotNull(DiagnosticGroups.DEPRECATED);
    assertNotNull(DiagnosticGroups.VISIBILITY);
    assertNotNull(DiagnosticGroups.CONSTANT_PROPERTY);
    assertNotNull(DiagnosticGroups.NON_STANDARD_JSDOC);
    assertNotNull(DiagnosticGroups.ACCESS_CONTROLS);
    assertNotNull(DiagnosticGroups.INVALID_CASTS);
    assertNotNull(DiagnosticGroups.FILEOVERVIEW_JSDOC);
    assertNotNull(DiagnosticGroups.STRICT_MODULE_DEP_CHECK);
    assertNotNull(DiagnosticGroups.EXTERNS_VALIDATION);
    assertNotNull(DiagnosticGroups.AMBIGUOUS_FUNCTION_DECL);
    assertNotNull(DiagnosticGroups.UNKNOWN_DEFINES);
    assertNotNull(DiagnosticGroups.TWEAKS);
    assertNotNull(DiagnosticGroups.MISSING_PROPERTIES);
    assertNotNull(DiagnosticGroups.INTERNET_EXPLORER_CHECKS);
    assertNotNull(DiagnosticGroups.UNDEFINED_VARIABLES);
    assertNotNull(DiagnosticGroups.CHECK_REGEXP);
    assertNotNull(DiagnosticGroups.CHECK_TYPES);
    assertNotNull(DiagnosticGroups.CHECK_VARIABLES);
    assertNotNull(DiagnosticGroups.CHECK_USELESS_CODE);
    assertNotNull(DiagnosticGroups.TYPE_INVALIDATION);
  }

  // Tests consistency of DIAGNOSTIC_GROUP_NAMES constant
  @Test
  public void testDiagnosticGroupNames_constantString_containsExpectedNames() {
    String names = DiagnosticGroups.DIAGNOSTIC_GROUP_NAMES;
    assertNotNull(names);
    assertTrue(names.contains("globalThis"));
    assertTrue(names.contains("checkTypes"));
    assertTrue(names.contains("checkVars"));
    assertTrue(names.contains("deprecated"));
    assertTrue(names.contains("visibility"));
  }

  // Tests registering an empty DiagnosticType array
  @Test
  public void testRegisterGroup_withEmptyDiagnosticTypes() {
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("emptyTypeGroup", new DiagnosticType[0]);
    assertNotNull(registered);
    assertSame(registered, diagnosticGroups.forName("emptyTypeGroup"));
    assertEquals("emptyTypeGroup", registered.getName());
  }

  // Tests registering an empty DiagnosticGroup array
  @Test
  public void testRegisterGroup_withEmptySubGroups() {
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("emptySubGroupComposite", new DiagnosticGroup[0]);
    assertNotNull(registered);
    assertSame(registered, diagnosticGroups.forName("emptySubGroupComposite"));
    assertEquals("emptySubGroupComposite", registered.getName());
  }
}
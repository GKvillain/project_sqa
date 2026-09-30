package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link RenamePrototypes}.
 */
public class RenamePrototypesTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; window.indexOf; window.lastIndexOf; window.toString; window.valueOf;";

  private VariableMap prevUsedRenameMap = null;
  private boolean aggressiveRenaming = true;
  private char[] reservedChars = null;
  private RenamePrototypes lastPass = null;

  public RenamePrototypesTest() {
    super(EXTERNS);
    enableNormalize();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    aggressiveRenaming = true;
    prevUsedRenameMap = null;
    reservedChars = null;
    lastPass = null;
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    lastPass = new RenamePrototypes(compiler, aggressiveRenaming, reservedChars, prevUsedRenameMap);
    return lastPass;
  }

  // Tests renaming prototype properties in normal assignment
  @Test
  public void testProcess_simplePrototypeMethod_renamesProperty() {
    test(
        "function Point() {} Point.prototype.getX = function() { return this.x; };",
        "function Point() {} Point.prototype.a = function() { return this.x; };");
  }

  // Tests property sorting by reference frequency
  @Test
  public void testProcess_multipleProperties_sortedByFrequency() {
    test(
        "Bar.prototype.getB = function() {}; Bar.prototype.getA = function() {}; x.getA();",
        "Bar.prototype.b = function() {}; Bar.prototype.a = function() {}; x.a();");
  }

  // Tests renaming properties defined inside prototype object literal
  @Test
  public void testProcess_prototypeObjectLiteral_renamesKeys() {
    test(
        "Point.prototype = { getX: function() {}, getY: function() {} };",
        "Point.prototype = { a: function() {}, b: function() {} };");
  }

  // Tests renaming properties inside call with object literal argument
  @Test
  public void testProcess_prototypeInCall_renamesKeys() {
    test(
        "goog.inherits(Point, { getX: function() {} });",
        "goog.inherits(Point, { a: function() {} });");
  }

  // Tests that standard object literal properties are not renamed without private convention
  @Test
  public void testProcess_standardObjectLiteral_doesNotRename() {
    testSame("var obj = { getX: 1, getY: 2 };");
  }

  // Tests private property in object literal is renamed
  @Test
  public void testProcess_privateObjectLiteralProperty_renames() {
    test(
        "var obj = { getX_: 1 };",
        "var obj = { a: 1 };");
  }

  // Tests non-aggressive renaming skips all-lowercase method names
  @Test
  public void testProcess_nonAggressiveLowerCases_doesNotRename() {
    aggressiveRenaming = false;
    testSame("Point.prototype.getx = function() {};");
  }

  // Tests non-aggressive renaming renames methods with uppercase characters
  @Test
  public void testProcess_nonAggressiveMixedCase_renames() {
    aggressiveRenaming = false;
    test(
        "Point.prototype.getX = function() {};",
        "Point.prototype.a = function() {};");
  }

  // Tests built-in externed properties are reserved and not renamed
  @Test
  public void testProcess_reservedExternProperties_notRenamed() {
    testSame("Point.prototype.toString = function() {};");
    testSame("Point.prototype.indexOf = function() {};");
    testSame("Point.prototype.lastIndexOf = function() {};");
    testSame("Point.prototype.valueOf = function() {};");
  }

  // Tests reserved characters are avoided during name generation
  @Test
  public void testProcess_withReservedChars_skipsReservedChars() {
    reservedChars = new char[]{'a', 'b'};
    test(
        "Point.prototype.getX = function() {};",
        "Point.prototype.c = function() {};");
  }

  // Tests reusing rename map from a previous compilation
  @Test
  public void testProcess_withPreviousRenameMap_reusesNames() {
    prevUsedRenameMap = new VariableMap(ImmutableMap.of("getX", "z"));
    test(
        "Point.prototype.getX = function() {};",
        "Point.prototype.z = function() {};");
  }

  // Tests previous rename map where old newName conflicts with reserved names
  @Test
  public void testProcess_previousRenameMapConflict_generatesNewName() {
    prevUsedRenameMap = new VariableMap(ImmutableMap.of("getX", "toString"));
    test(
        "Point.prototype.getX = function() {};",
        "Point.prototype.a = function() {};");
  }

  // Tests object literal with numeric keys are skipped
  @Test
  public void testProcess_numericKeysInPrototypeLiteral_skipsNumbers() {
    testSame("Point.prototype = { 1: function() {}, 2: function() {} };");
  }

  // Tests getPropertyMap returns populated mappings
  @Test
  public void testGetPropertyMap_afterProcess_returnsCorrectMap() {
    test(
        "Point.prototype.getX = function() {};",
        "Point.prototype.a = function() {};");
    assertNotNull(lastPass);
    VariableMap map = lastPass.getPropertyMap();
    assertNotNull(map);
    assertEquals("a", map.lookupNewName("getX"));
  }

  // Tests getter and setter in prototype object literal
  @Test
  public void testProcess_gettersAndSettersInPrototype_renamesKeys() {
    test(
        "Point.prototype = { get getX() { return 0; }, set setX(val) {} };",
        "Point.prototype = { get a() { return 0; }, set b(val) {} };");
  }

  // Tests property accessed both on prototype and in object literal
  @Test
  public void testProcess_sharedPrototypeAndObjLit_renamedIfPrivate() {
    test(
        "Point.prototype.foo_ = function() {}; var o = { foo_: 1 };",
        "Point.prototype.a = function() {}; var o = { a: 1 };");
  }

  // Tests unnormalized life cycle stage throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testProcess_unnormalizedStage_throwsException() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = IR.block();
    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externs, root);
  }

  // Tests calling prototype methods through instance references
  @Test
  public void testProcess_instanceMethodCall_renamesCorrectly() {
    test(
        "function Foo() {} Foo.prototype.bar = function() {}; var f = new Foo(); f.bar();",
        "function Foo() {} Foo.prototype.a = function() {}; var f = new Foo(); f.a();");
  }

  // Tests multiple prototype assignments to the same property name
  @Test
  public void testProcess_multipleClassesSameProperty_renamedConsistently() {
    test(
        "Foo.prototype.display = function() {}; Bar.prototype.display = function() {};",
        "Foo.prototype.a = function() {}; Bar.prototype.a = function() {};");
  }

  // Tests that prototype property defined on an object instance is updated when it matches prototype property
  @Test
  public void testProcess_matchingPrototypeAndObjectLiteralKey_renamesBoth() {
    test(
        "Foo.prototype.render = function() {}; var obj = { render: 123 };",
        "Foo.prototype.a = function() {}; var obj = { a: 123 };");
  }

  // Tests prototype property assignment nested under an expression
  @Test
  public void testProcess_nestedPrototypeAssignment_renamesProperty() {
    test(
        "(Foo.prototype.calculate = function() {}) && true;",
        "(Foo.prototype.a = function() {}) && true;");
  }
}
package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CrossModuleMethodMotion}.
 */
public class CrossModuleMethodMotionTest extends CompilerTestCase {

  private boolean canModifyExterns = false;
  private static final String STUB_DECLARATIONS =
      CrossModuleMethodMotion.STUB_DECLARATIONS;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.canModifyExterns = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CrossModuleMethodMotion(
        compiler, new CrossModuleMethodMotion.IdGenerator(),
        canModifyExterns);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests IdGenerator basic functionality
  @Test
  public void testIdGenerator_initialStateAndIncrement_returnsExpectedIds() {
    CrossModuleMethodMotion.IdGenerator idGen =
        new CrossModuleMethodMotion.IdGenerator();
    assertFalse(idGen.hasGeneratedAnyIds());
    assertEquals(0, idGen.newId());
    assertTrue(idGen.hasGeneratedAnyIds());
    assertEquals(1, idGen.newId());
    assertEquals(2, idGen.newId());
  }

  // Tests single module case where no motion occurs
  @Test
  public void testProcess_singleModule_noChange() {
    testSame("function Foo() {} Foo.prototype.bar = function() {};");
  }

  // Tests moving a simple prototype method to a deeper module
  @Test
  public void testMoveMethod_simpleFunction_movedToDependentModule() {
    test(
        createModuleChain(
          "function Foo() {} Foo.prototype.bar = function() { return 1; };",
          "var f = new Foo(); f.bar();"
        ),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {} Foo.prototype.bar = JSCompiler_stubMethod(0);",
          "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() { return 1; });" +
          "var f = new Foo(); f.bar();"
        });
  }

  // Tests moving multiple prototype methods across modules
  @Test
  public void testMoveMethod_multipleMethods_movedWithDistinctIds() {
    test(
        createModuleChain(
          "function Foo() {} " +
          "Foo.prototype.m1 = function() { return 1; };" +
          "Foo.prototype.m2 = function() { return 2; };",
          "var f = new Foo(); f.m1(); f.m2();"
        ),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {} " +
          "Foo.prototype.m1 = JSCompiler_stubMethod(0);" +
          "Foo.prototype.m2 = JSCompiler_stubMethod(1);",
          "Foo.prototype.m2 = JSCompiler_unstubMethod(1, function() { return 2; });" +
          "Foo.prototype.m1 = JSCompiler_unstubMethod(0, function() { return 1; });" +
          "var f = new Foo(); f.m1(); f.m2();"
        });
  }

  // Tests that methods reading closure variables are not moved
  @Test
  public void testMoveMethod_readsClosureVariable_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {} " +
          "(function() {" +
          "  var x = 1;" +
          "  Foo.prototype.bar = function() { return x; };" +
          "})();",
          "var f = new Foo(); f.bar();"
        ));
  }

  // Tests that unreferenced prototype methods are skipped
  @Test
  public void testMoveMethod_unreferencedMethod_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {} Foo.prototype.bar = function() { return 1; };",
          "var x = 1;"
        ));
  }

  // Tests getter property definitions are not moved
  @Test
  public void testMoveMethod_getterDefinition_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {} Foo.prototype = { get bar() { return 1; } };",
          "var f = new Foo(); var x = f.bar;"
        ));
  }

  // Tests setter property definitions are not moved
  @Test
  public void testMoveMethod_setterDefinition_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {} Foo.prototype = { set bar(x) { this.x = x; } };",
          "var f = new Foo(); f.bar = 2;"
        ));
  }

  // Tests method already defined in the deepest common module is not moved
  @Test
  public void testMoveMethod_alreadyInDeepestModule_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {}",
          "Foo.prototype.bar = function() { return 1; }; var f = new Foo(); f.bar();"
        ));
  }

  // Tests moving method across three module chain
  @Test
  public void testMoveMethod_threeModuleChain_movedToLeafModule() {
    test(
        createModuleChain(
          "function Foo() {} Foo.prototype.bar = function() { return 1; };",
          "var a = 1;",
          "var f = new Foo(); f.bar();"
        ),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {} Foo.prototype.bar = JSCompiler_stubMethod(0);",
          "var a = 1;",
          "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() { return 1; });" +
          "var f = new Foo(); f.bar();"
        });
  }

  // Tests non-function prototype property is not moved as a method
  @Test
  public void testMoveMethod_nonFunctionProperty_notMoved() {
    testSame(
        createModuleChain(
          "function Foo() {} Foo.prototype.bar = 123;",
          "var f = new Foo(); var x = f.bar;"
        ));
  }

  // Tests externs modification flag
  @Test
  public void testMoveMethod_canModifyExternsEnabled_processesNormally() {
    this.canModifyExterns = true;
    test(
        createModuleChain(
          "function Foo() {} Foo.prototype.bar = function() { return 1; };",
          "var f = new Foo(); f.bar();"
        ),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {} Foo.prototype.bar = JSCompiler_stubMethod(0);",
          "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() { return 1; });" +
          "var f = new Foo(); f.bar();"
        });
  }
}
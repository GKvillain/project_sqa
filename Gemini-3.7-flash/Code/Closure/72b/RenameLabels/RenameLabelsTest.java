package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link RenameLabels}.
 */
public class RenameLabelsTest extends CompilerTestCase {

  private static final String EXTERNS = "var window;";
  private boolean removeUnused = true;

  public RenameLabelsTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RenameLabels(compiler, new RenameLabels.DefaultNameSupplier(), removeUnused);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    removeUnused = true;
  }

  // Tests renaming of a single referenced label with break statement
  @Test
  public void testProcess_namedBreak_renamesLabel() {
    test("Foo: while (1) { break Foo; }", "a: while (1) { break a; }");
  }

  // Tests renaming of a single referenced label with continue statement
  @Test
  public void testProcess_namedContinue_renamesLabel() {
    test("Foo: for (;;) { continue Foo; }", "a: for (;;) { continue a; }");
  }

  // Tests that unreferenced labels are removed when removeUnused is true
  @Test
  public void testProcess_unreferencedLabelRemoveUnusedTrue_removesLabel() {
    removeUnused = true;
    test("Foo: while (1) { var x = 1; }", "while (1) { var x = 1; }");
  }

  // Tests that unreferenced labels are preserved and renamed when removeUnused is false
  @Test
  public void testProcess_unreferencedLabelRemoveUnusedFalse_renamesLabel() {
    removeUnused = false;
    test("Foo: while (1) { var x = 1; }", "a: while (1) { var x = 1; }");
  }

  // Tests nested labels where outer is referenced and inner is unreferenced
  @Test
  public void testProcess_nestedLabelsInnerUnreferenced_removesInnerRenamesOuter() {
    removeUnused = true;
    test("Outer: { Inner: { break Outer; } }", "a: { { break a; } }");
  }

  // Tests nested labels where both outer and inner labels are referenced
  @Test
  public void testProcess_nestedLabelsBothReferenced_renamesBothLabels() {
    test("Outer: while (1) { Inner: for (;;) { if (1) break Outer; else continue Inner; } }",
         "a: while (1) { b: for (;;) { if (1) break a; else continue b; } }");
  }

  // Tests that label names are reused across different function scopes
  @Test
  public void testProcess_separateFunctionScopes_reusesLabelNames() {
    test(
        "function f() { Foo: while (1) { break Foo; } } "
            + "function g() { Bar: while (1) { break Bar; } }",
        "function f() { a: while (1) { break a; } } "
            + "function g() { a: while (1) { break a; } }");
  }

  // Tests when a label is already named with the target generated name
  @Test
  public void testProcess_alreadyNamedWithTarget_keepsName() {
    test("a: while (1) { break a; }", "a: while (1) { break a; }");
  }

  // Tests multiple break references pointing to the same label
  @Test
  public void testProcess_multipleReferencesToSameLabel_renamesAll() {
    test(
        "MyLabel: while (1) { if (1) { break MyLabel; } else { break MyLabel; } }",
        "a: while (1) { if (1) { break a; } else { break a; } }");
  }

  // Tests unreferenced label containing a block merges the block
  @Test
  public void testProcess_unreferencedLabelWithBlock_mergesBlock() {
    removeUnused = true;
    test("Foo: { var a = 1; var b = 2; }", "var a = 1; var b = 2;");
  }

  // Tests unreferenced label containing a block when removeUnused is false
  @Test
  public void testProcess_unreferencedLabelWithBlockRemoveUnusedFalse_keepsBlockAndLabel() {
    removeUnused = false;
    test("Foo: { var a = 1; }", "a: { var a = 1; }");
  }

  // Tests DefaultNameSupplier generates names sequentially
  @Test
  public void testDefaultNameSupplier_generatesSequentialNames() {
    RenameLabels.DefaultNameSupplier supplier = new RenameLabels.DefaultNameSupplier();
    assertNotNull(supplier.get());
    assertEquals("b", supplier.get());
    assertEquals("c", supplier.get());
  }
}
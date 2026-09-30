package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import org.junit.Test;

/**
 * Tests for {@link RenameLabels}.
 */
public class RenameLabelsTest extends CompilerTestCase {
  private boolean removeUnused = true;
  private Supplier<String> nameSupplier = new RenameLabels.DefaultNameSupplier();
  private boolean useDefaultConstructor = false;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    if (useDefaultConstructor) {
      return new RenameLabels(compiler);
    }
    return new RenameLabels(compiler, nameSupplier, removeUnused);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    removeUnused = true;
    nameSupplier = new RenameLabels.DefaultNameSupplier();
    useDefaultConstructor = false;
  }

  // Tests renaming of a single referenced label in a loop
  @Test
  public void testProcess_singleReferencedLabel_renamed() {
    test("foo: while (true) { break foo; }",
         "a: while (true) { break a; }");
  }

  // Tests renaming of nested referenced labels
  @Test
  public void testProcess_nestedReferencedLabels_renamed() {
    test("outer: while (true) { inner: while (true) { break outer; } }",
         "a: while (true) { b: while (true) { break a; } }");
  }

  // Tests renaming of continue statement target
  @Test
  public void testProcess_continueStatement_renamed() {
    test("foo: for (;;) { continue foo; }",
         "a: for (;;) { continue a; }");
  }

  // Tests removal of unreferenced label on a loop
  @Test
  public void testProcess_unusedLabelOnLoop_removed() {
    test("foo: while (true) { var x = 1; }",
         "while (true) { var x = 1; }");
  }

  // Tests removal of unreferenced label block and merging child block
  @Test
  public void testProcess_unusedLabelBlock_merged() {
    test("foo: { var x = 1; var y = 2; }",
         "var x = 1; var y = 2;");
  }

  // Tests renaming labels across separate function scopes
  @Test
  public void testProcess_separateFunctionScopes_reusesLabelNames() {
    test("function f() { foo: while (true) { break foo; } }" +
         "function g() { bar: while (true) { break bar; } }",
         "function f() { a: while (true) { break a; } }" +
         "function g() { a: while (true) { break a; } }");
  }

  // Tests nested function scopes do not share label namespace
  @Test
  public void testProcess_nestedFunctionScope_createsNewNamespace() {
    test("foo: while (true) {" +
         "  function f() { bar: while (true) { break bar; } }" +
         "  break foo;" +
         "}",
         "a: while (true) {" +
         "  function f() { a: while (true) { break a; } }" +
         "  break a;" +
         "}");
  }

  // Tests mixed referenced and unreferenced labels
  @Test
  public void testProcess_mixedReferencedAndUnreferenced_removesOnlyUnreferenced() {
    test("outer: while (true) {" +
         "  unused: while (true) {" +
         "    break outer;" +
         "  }" +
         "}",
         "a: while (true) {" +
         "  while (true) {" +
         "    break a;" +
         "  }" +
         "}");
  }

  // Tests when removeUnused is false, unreferenced label is preserved and renamed
  @Test
  public void testProcess_removeUnusedFalse_preservesUnreferencedLabel() {
    removeUnused = false;
    test("foo: while (true) {}",
         "a: while (true) {}",
         "a: while (true);");
  }

  // Tests when removeUnused is false, multiple unreferenced labels are preserved
  @Test
  public void testProcess_removeUnusedFalse_nestedLabelsPreserved() {
    removeUnused = false;
    test("outer: { inner: { var x = 1; } }",
         "a: { b: { var x = 1; } }");
  }

  // Tests DefaultNameSupplier generates distinct sequential names
  @Test
  public void testDefaultNameSupplier_generatesSequentialNames() {
    RenameLabels.DefaultNameSupplier supplier = new RenameLabels.DefaultNameSupplier();
    String first = supplier.get();
    String second = supplier.get();
    assertNotNull(first);
    assertNotNull(second);
    assertFalse(first.equals(second));
  }

  // Tests custom name supplier with RenameLabels
  @Test
  public void testProcess_customNameSupplier_usesCustomNames() {
    nameSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return "custom" + (++count);
      }
    };
    test("foo: while (true) { break foo; }",
         "custom1: while (true) { break custom1; }");
  }

  // Tests already matching short name does not trigger unnecessary change
  @Test
  public void testProcess_alreadyShortName_kept() {
    test("a: while (true) { break a; }",
         "a: while (true) { break a; }");
  }

  // Tests unreferenced label containing a single statement
  @Test
  public void testProcess_unreferencedLabelSingleStatement_unwrapped() {
    test("foo: x = 1;",
         "x = 1;");
  }

  // Tests single-argument constructor of RenameLabels
  @Test
  public void testProcess_defaultConstructor_renamesAndRemovesUnused() {
    useDefaultConstructor = true;
    test("foo: while (true) { break foo; } bar: while (true) {}",
         "a: while (true) { break a; } while (true) {}");
  }

  // Tests sibling labeled blocks reuse label names in the same function scope
  @Test
  public void testProcess_siblingLabeledBlocks_reusesLabelNames() {
    test("foo: { break foo; } bar: { break bar; }",
         "a: { break a; } a: { break a; }");
  }

  // Tests multiple labels on the same statement with only one used
  @Test
  public void testProcess_multipleLabelsSameStatement_mixedUsage() {
    test("lbl1: lbl2: while (true) { break lbl2; }",
         "a: while (true) { break a; }");
  }

  // Tests unlabeled break does not prevent label removal when unused
  @Test
  public void testProcess_unlabeledBreak_removesUnusedLabel() {
    test("foo: while (true) { break; }",
         "while (true) { break; }");
  }

  // Tests labeled switch statement
  @Test
  public void testProcess_switchWithLabel_renamed() {
    test("foo: switch (x) { case 1: break foo; }",
         "a: switch (x) { case 1: break a; }");
  }

  // Tests empty labeled block removal
  @Test
  public void testProcess_emptyLabeledBlock_removed() {
    test("foo: {}",
         "");
  }

  // Tests labeled do-while loop
  @Test
  public void testProcess_doWhileLoop_renamed() {
    test("foo: do { continue foo; } while (true);",
         "a: do { continue a; } while (true);");
  }

  // Tests labeled try-catch-finally block
  @Test
  public void testProcess_tryCatchFinally_renamed() {
    test("foo: try { break foo; } catch (e) { break foo; } finally { break foo; }",
         "a: try { break a; } catch (e) { break a; } finally { break a; }");
  }
}
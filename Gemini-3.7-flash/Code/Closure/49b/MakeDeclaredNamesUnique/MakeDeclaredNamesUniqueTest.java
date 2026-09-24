package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueTest {

  private Supplier<String> createSequentialIdSupplier() {
    return new Supplier<String>() {
      private int counter = 0;

      @Override
      public String get() {
        return String.valueOf(counter++);
      }
    };
  }

  // Tests global scope handling in ContextualRenamer where names are reserved but not replaced
  @Test
  public void testContextualRenamer_globalScope_doesNotReplace() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    renamer.addDeclaredName("a");
    assertNull(renamer.getReplacementName("a"));
    assertFalse(renamer.stripConstIfReplaced());
  }

  // Tests child scope renaming in ContextualRenamer for duplicate variable names
  @Test
  public void testContextualRenamer_childScope_renamesDuplicateDeclarations() {
    MakeDeclaredNamesUnique.ContextualRenamer globalRenamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    globalRenamer.addDeclaredName("foo");

    MakeDeclaredNamesUnique.Renamer childRenamer1 = globalRenamer.forChildScope();
    childRenamer1.addDeclaredName("foo");
    assertEquals("foo$$1", childRenamer1.getReplacementName("foo"));

    MakeDeclaredNamesUnique.Renamer childRenamer2 = globalRenamer.forChildScope();
    childRenamer2.addDeclaredName("foo");
    assertEquals("foo$$2", childRenamer2.getReplacementName("foo"));
  }

  // Tests child scope in ContextualRenamer with newly introduced names
  @Test
  public void testContextualRenamer_childScope_firstOccurrenceNotRenamed() {
    MakeDeclaredNamesUnique.ContextualRenamer globalRenamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.Renamer childRenamer = globalRenamer.forChildScope();
    childRenamer.addDeclaredName("uniqueLocal");
    assertNull(childRenamer.getReplacementName("uniqueLocal"));
  }

  // Tests that 'arguments' is ignored by ContextualRenamer
  @Test
  public void testContextualRenamer_argumentsIgnored_notRenamed() {
    MakeDeclaredNamesUnique.ContextualRenamer globalRenamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    globalRenamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNull(globalRenamer.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS));

    MakeDeclaredNamesUnique.Renamer childRenamer = globalRenamer.forChildScope();
    childRenamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNull(childRenamer.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS));
  }

  // Tests InlineRenamer basic renaming functionality with supplier and prefix
  @Test
  public void testInlineRenamer_addDeclaredName_replacesWithPrefixAndId() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", true);

    renamer.addDeclaredName("varName");
    assertEquals("varName$$inline_0", renamer.getReplacementName("varName"));
    assertTrue(renamer.stripConstIfReplaced());
  }

  // Tests InlineRenamer stripping existing unique separator
  @Test
  public void testInlineRenamer_existingSeparator_replacesSuffix() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "in_", false);

    renamer.addDeclaredName("varName$$old_1");
    assertEquals("varName$$in_0", renamer.getReplacementName("varName$$old_1"));
    assertFalse(renamer.stripConstIfReplaced());
  }

  // Tests InlineRenamer handling empty name
  @Test
  public void testInlineRenamer_emptyName_returnsEmpty() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "in_", false);

    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
  }

  // Tests InlineRenamer throws exception on 'arguments'
  @Test(expected = IllegalStateException.class)
  public void testInlineRenamer_argumentsName_throwsIllegalStateException() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "in_", false);

    renamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
  }

  // Tests InlineRenamer forChildScope returns a valid new instance
  @Test
  public void testInlineRenamer_forChildScope_createsWorkingChildRenamer() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "child_", true);

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    assertNotNull(child);
    assertTrue(child.stripConstIfReplaced());
    child.addDeclaredName("x");
    assertEquals("x$$child_0", child.getReplacementName("x"));
  }

  // Tests BoilerplateRenamer creates child scope with InlineRenamer
  @Test
  public void testBoilerplateRenamer_forChildScope_returnsInlineRenamer() {
    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.BoilerplateRenamer renamer =
        new MakeDeclaredNamesUnique.BoilerplateRenamer(supplier, "bp_");

    renamer.addDeclaredName("globalVar");
    assertNull(renamer.getReplacementName("globalVar"));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    assertNotNull(child);
    child.addDeclaredName("localVar");
    assertEquals("localVar$$bp_0", child.getReplacementName("localVar"));
    assertFalse(child.stripConstIfReplaced());
  }

  // Tests ContextualRenameInverter getOriginalName utility method
  @Test
  public void testContextualRenameInverter_getOrginalName_handlesSeparator() {
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$1"));
    assertEquals("bar", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("bar$$inline_123"));
    assertEquals("noSeparator", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("noSeparator"));
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("$$1"));
  }

  // Tests full traversal renaming duplicate names in nested function scopes
  @Test
  public void testMakeDeclaredNamesUnique_traversal_renamesDuplicateLocalVars() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; function f(a) { var a = 2; } function g(a) { var a = 3; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    String result = compiler.toSource(root);
    assertTrue(result.contains("a$$1") || result.contains("a$$2"));
  }

  // Tests traversal renaming in catch block
  @Test
  public void testMakeDeclaredNamesUnique_traversal_renamesCatchVar() {
    Compiler compiler = new Compiler();
    String js = "var e = 1; try { throw 2; } catch (e) { e = 3; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    String result = compiler.toSource(root);
    assertTrue(result.contains("e$$1"));
  }

  // Tests traversal renaming recursive named function expressions
  @Test
  public void testMakeDeclaredNamesUnique_traversal_renamesFunctionExpressionName() {
    Compiler compiler = new Compiler();
    String js = "var f = 1; var x = function f() { f(); };";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    String result = compiler.toSource(root);
    assertTrue(result.contains("f$$1"));
  }

  // Tests ContextualRenameInverter inverts unique names back when safe
  @Test
  public void testContextualRenameInverter_process_invertsUniqueNames() {
    Compiler compiler = new Compiler();
    String js = "function f() { var x$$1 = 1; x$$1++; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    String result = compiler.toSource(root);
    assertFalse(result.contains("x$$1"));
    assertTrue(result.contains("x"));
  }

  // Tests MakeDeclaredNamesUnique with custom InlineRenamer
  @Test
  public void testMakeDeclaredNamesUnique_withInlineRenamer_renamesTarget() {
    Compiler compiler = new Compiler();
    String js = "function f(x) { var y = x; }";
    Node root = compiler.parseTestCode(js);

    Supplier<String> supplier = createSequentialIdSupplier();
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "test_", false);
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);

    NodeTraversal.traverse(compiler, root, pass);
    String result = compiler.toSource(root);
    assertTrue(result.contains("test_"));
  }
}
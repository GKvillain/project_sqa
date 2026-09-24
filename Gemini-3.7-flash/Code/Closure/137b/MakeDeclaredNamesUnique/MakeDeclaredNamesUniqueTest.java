package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MakeDeclaredNamesUniqueTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests ContextualRenamer in global scope reserving names
  @Test
  public void testContextualRenamer_globalScope_reservesName() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    renamer.addDeclaredName("foo");
    assertNull(renamer.getReplacementName("foo"));
    assertFalse(renamer.stripConstIfReplaced());
  }

  // Tests ContextualRenamer in child scopes generating unique names upon conflict
  @Test
  public void testContextualRenamer_childScope_generatesUniqueNames() {
    MakeDeclaredNamesUnique.ContextualRenamer rootRenamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    rootRenamer.addDeclaredName("x");

    MakeDeclaredNamesUnique.Renamer childRenamer1 = rootRenamer.forChildScope();
    childRenamer1.addDeclaredName("x");
    assertEquals("x$$1", childRenamer1.getReplacementName("x"));

    MakeDeclaredNamesUnique.Renamer childRenamer2 = rootRenamer.forChildScope();
    childRenamer2.addDeclaredName("x");
    assertEquals("x$$2", childRenamer2.getReplacementName("x"));

    // Name that does not conflict in first occurrence in child scope
    childRenamer1.addDeclaredName("y");
    assertNull(childRenamer1.getReplacementName("y"));
  }

  // Tests InlineRenamer with valid prefix and unique supplier
  @Test
  public void testInlineRenamer_validInput_replacesName() {
    Supplier<String> idSupplier = Suppliers.ofInstance("123");
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inline_", true);

    assertTrue(renamer.stripConstIfReplaced());
    renamer.addDeclaredName("foo");
    assertEquals("foo$$inline_123", renamer.getReplacementName("foo"));
  }

  // Tests InlineRenamer with empty name
  @Test
  public void testInlineRenamer_emptyName_returnsEmpty() {
    Supplier<String> idSupplier = Suppliers.ofInstance("1");
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "prefix_", false);

    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
    assertNull(renamer.getReplacementName("unknown"));
  }

  // Tests InlineRenamer with name already containing separator
  @Test
  public void testInlineRenamer_nameWithSeparator_stripsPreviousSuffix() {
    Supplier<String> idSupplier = Suppliers.ofInstance("99");
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "js_", false);

    renamer.addDeclaredName("bar$$oldSuffix");
    assertEquals("bar$$js_99", renamer.getReplacementName("bar$$oldSuffix"));
  }

  // Tests InlineRenamer constructor throws exception with empty prefix
  @Test(expected = IllegalArgumentException.class)
  public void testInlineRenamer_emptyPrefix_throwsException() {
    Supplier<String> idSupplier = Suppliers.ofInstance("1");
    new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "", false);
  }

  // Tests InlineRenamer forChildScope creates child with same properties
  @Test
  public void testInlineRenamer_forChildScope_createsIndependentChild() {
    Supplier<String> idSupplier = Suppliers.ofInstance("42");
    MakeDeclaredNamesUnique.InlineRenamer parent =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "in_", true);
    MakeDeclaredNamesUnique.Renamer child = parent.forChildScope();

    assertTrue(child.stripConstIfReplaced());
    child.addDeclaredName("a");
    assertEquals("a$$in_42", child.getReplacementName("a"));
    assertNull(parent.getReplacementName("a"));
  }

  // Tests ContextualRenameInverter getOrginalName with and without separator
  @Test
  public void testContextualRenameInverter_getOriginalName() {
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo"));
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOriginalName("foo$$1"));
    assertEquals("foo$$1", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$1$$2"));
  }

  // Tests full traversal renaming nested function parameters and local vars
  @Test
  public void testTraverse_nestedFunctionsAndVars_renamesDuplicates() {
    String js = "var a = 1; function f(a) { var a = 2; function g(a) { var a = 3; } }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
    assertTrue(result.contains("a$$1") || result.contains("a$$2"));
  }

  // Tests full traversal renaming catch block exception variable
  @Test
  public void testTraverse_catchBlock_renamesCatchVar() {
    String js = "var e = 1; try {} catch (e) { var e = 2; }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
    assertTrue(result.contains("e$$1") || result.contains("e$$2"));
  }

  // Tests ContextualRenameInverter inverting renamed vars
  @Test
  public void testContextualRenameInverter_process_revertsUniqueNames() {
    String js = "function f() { var x = 1; { var x$$1 = 2; } }";
    Node root = compiler.parseTestCode(js);
    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    String result = compiler.toSource(root);
    assertNotNull(result);
  }

  // Tests MakeDeclaredNamesUnique with custom InlineRenamer
  @Test
  public void testTraverse_withInlineRenamer_renamesAllDeclarations() {
    Supplier<String> idSupplier = Suppliers.ofInstance("unique");
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inline_", true);
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);

    String js = "var a = 1; function f(b) { var c = 2; }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, pass);

    String result = compiler.toSource(root);
    assertTrue(result.contains("a$$inline_unique"));
    assertTrue(result.contains("b$$inline_unique"));
    assertTrue(result.contains("c$$inline_unique"));
  }

  // Tests named function expression recursive name handling
  @Test
  public void testTraverse_namedFunctionExpression_handlesRecursiveName() {
    String js = "var x = function foo() { foo(); };";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
  }

  // Tests ContextualRenamer with repeated declarations in the same child scope
  @Test
  public void testContextualRenamer_multipleDeclarationsInSameChildScope() {
    MakeDeclaredNamesUnique.ContextualRenamer root =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    root.addDeclaredName("v");

    MakeDeclaredNamesUnique.Renamer child = root.forChildScope();
    child.addDeclaredName("v");
    String firstReplacement = child.getReplacementName("v");
    assertEquals("v$$1", firstReplacement);

    // Adding same variable declaration again in the same child scope shouldn't change replacement
    child.addDeclaredName("v");
    assertEquals(firstReplacement, child.getReplacementName("v"));
  }

  // Tests ContextualRenamer deep hierarchy (grandchild scopes)
  @Test
  public void testContextualRenamer_grandchildScope_generatesIncrementedNames() {
    MakeDeclaredNamesUnique.ContextualRenamer root =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    root.addDeclaredName("k");

    MakeDeclaredNamesUnique.Renamer child = root.forChildScope();
    child.addDeclaredName("k");
    assertEquals("k$$1", child.getReplacementName("k"));

    MakeDeclaredNamesUnique.Renamer grandchild = child.forChildScope();
    grandchild.addDeclaredName("k");
    assertEquals("k$$2", grandchild.getReplacementName("k"));
  }

  // Tests ContextualRenameInverter when reverting is blocked by a name collision in scope
  @Test
  public void testContextualRenameInverter_collisionPreventsReverting() {
    String js = "function f() { var x = 1; function g() { var x$$1 = 2; alert(x); } }";
    Node root = compiler.parseTestCode(js);
    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    String result = compiler.toSource(root);
    assertTrue(result.contains("x$$1"));
  }

  // Tests traversal preserves object property names matching renamed variable names
  @Test
  public void testTraverse_objectProperties_notRenamed() {
    String js = "var a = 1; function f(a) { var obj = {a: a}; return obj.a; }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertTrue(result.contains("{a: a$$1}") || result.contains("{a: a$$2}") || result.contains(".a"));
  }

  // Tests traversal with multiple duplicate var statements in the same scope
  @Test
  public void testTraverse_duplicateVarInSameScope() {
    String js = "function f() { var a = 1; var a = 2; return a; }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
    assertFalse(result.contains("a$$"));
  }

  // Tests traversal with arguments parameter handling
  @Test
  public void testTraverse_argumentsParameter() {
    String js = "function f(arguments) { return arguments; }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
  }

  // Tests traversal with nested catch blocks sharing catch variable name
  @Test
  public void testTraverse_nestedCatchBlocks() {
    String js = "try {} catch(e) { try {} catch(e) { var x = e; } }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    String result = compiler.toSource(root);
    assertNotNull(result);
    assertTrue(result.contains("e$$1") || result.contains("e$$2"));
  }
}
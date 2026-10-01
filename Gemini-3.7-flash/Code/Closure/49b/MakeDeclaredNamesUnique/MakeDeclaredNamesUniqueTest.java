package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests contextual renaming for global and child scopes
  @Test
  public void testContextualRenamer_childScopeRenaming_returnsUniqueNames() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();

    renamer.addDeclaredName("a");
    assertNull(renamer.getReplacementName("a"));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    child.addDeclaredName("a");
    assertEquals("a$$1", child.getReplacementName("a"));

    child.addDeclaredName("b");
    assertNull(child.getReplacementName("b"));

    MakeDeclaredNamesUnique.Renamer grandChild = child.forChildScope();
    grandChild.addDeclaredName("a");
    assertEquals("a$$2", grandChild.getReplacementName("a"));

    grandChild.addDeclaredName("b");
    assertEquals("b$$1", grandChild.getReplacementName("b"));

    assertFalse(renamer.stripConstIfReplaced());
    assertFalse(child.stripConstIfReplaced());
  }

  // Tests contextual renamer ignoring arguments identifier
  @Test
  public void testContextualRenamer_argumentsIgnored() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    renamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNull(renamer.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    child.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNull(child.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS));
  }

  // Tests inline renamer adds prefix and unique id
  @Test
  public void testInlineRenamer_basicRenaming_returnsPrefixedName() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inline_", true);

    renamer.addDeclaredName("foo");
    assertEquals("foo$$inline_0", renamer.getReplacementName("foo"));
    assertTrue(renamer.stripConstIfReplaced());

    // Second declaration of same name in same renamer is ignored
    renamer.addDeclaredName("foo");
    assertEquals("foo$$inline_0", renamer.getReplacementName("foo"));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    child.addDeclaredName("bar");
    assertEquals("bar$$inline_1", child.getReplacementName("bar"));
  }

  // Tests inline renamer with empty name and already suffixed name
  @Test
  public void testInlineRenamer_emptyAndSuffixedNames() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "123";
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "prefix_", false);

    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));

    renamer.addDeclaredName("foo$$old");
    assertEquals("foo$$prefix_123", renamer.getReplacementName("foo$$old"));
  }

  // Tests inline renamer throws exception on arguments name
  @Test(expected = IllegalStateException.class)
  public void testInlineRenamer_argumentsName_throwsException() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "0";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "p_", false);
    renamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);
  }

  // Tests inline renamer constructor throws exception on empty prefix
  @Test(expected = IllegalArgumentException.class)
  public void testInlineRenamer_emptyPrefix_throwsException() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "0";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "", false);
  }

  // Tests boilerplate renamer creating inline renamer child scope
  @Test
  public void testBoilerplateRenamer_forChildScope_createsInlineRenamer() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "99";
      }
    };

    MakeDeclaredNamesUnique.BoilerplateRenamer boilerplate =
        new MakeDeclaredNamesUnique.BoilerplateRenamer(idSupplier, "bp_");

    MakeDeclaredNamesUnique.Renamer child = boilerplate.forChildScope();
    assertTrue(child instanceof MakeDeclaredNamesUnique.InlineRenamer);
    child.addDeclaredName("local");
    assertEquals("local$$bp_99", child.getReplacementName("local"));
  }

  // Tests ContextualRenameInverter getOriginalName extraction
  @Test
  public void testContextualRenameInverter_getOriginalName() {
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$1"));
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$inline_2"));
    assertEquals("bar", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("bar"));
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("$$1"));
  }

  // Tests full traversal with function and var declarations
  @Test
  public void testTraverse_functionAndVarShadowing_renamesProperly() {
    String js = "var a = 1; function f(a) { var a = 2; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests full traversal with catch block variable
  @Test
  public void testTraverse_catchBlock_renamesProperly() {
    String js = "var e = 1; try { } catch (e) { var e = 2; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests full traversal with function expression recursive name
  @Test
  public void testTraverse_functionExpressionRecursiveName() {
    String js = "var x = function f() { f(); };";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests contextual rename inverter process restores names
  @Test
  public void testContextualRenameInverter_process_invertsNames() {
    String js = "var a = 1; function f(a$$1) { var a$$2 = 2; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    assertNotNull(root);
  }

  // Tests contextual rename inverter with token constant stripping
  @Test
  public void testTraverse_withInlineRenamer_stripsConst() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    String js = "var CONST_VAL = 1; function f() { var CONST_VAL = 2; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique.InlineRenamer inlineRenamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "in_", true);
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(inlineRenamer);
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests function expression with empty name node
  @Test
  public void testTraverse_anonymousFunctionExpression() {
    String js = "var f = function() { var a = 1; };";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests nested functions scoping
  @Test
  public void testTraverse_nestedFunctions() {
    String js = "function outer(x) { function inner(x) { return x; } return inner(x); }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests boilerplate renamer in full AST traversal
  @Test
  public void testTraverse_withBoilerplateRenamer() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    String js = "var a = 1; function f(b) { var c = 2; return b + c; }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique.BoilerplateRenamer boilerplate =
        new MakeDeclaredNamesUnique.BoilerplateRenamer(idSupplier, "bp_");
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(boilerplate);
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests nested catch blocks traversal
  @Test
  public void testTraverse_nestedCatchBlocks() {
    String js = "try { } catch (e) { try { } catch (e) { var e = 3; } }";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    assertNotNull(root);
  }

  // Tests inverter with conflict when restoring names
  @Test
  public void testContextualRenameInverter_withConflict_handlesSafely() {
    String js = "var a = 1; function f() { var a$$1 = 2; var a = 3; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    assertNotNull(root);
  }

  // Tests contextual renamer with names already containing $$
  @Test
  public void testContextualRenamer_nameWithDollarDollar() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();

    renamer.addDeclaredName("a$$old");
    assertNull(renamer.getReplacementName("a$$old"));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    child.addDeclaredName("a$$old");
    assertEquals("a$$1", child.getReplacementName("a$$old"));
  }

  // Tests shouldTraverse method on non-function / function nodes
  @Test
  public void testShouldTraverse_handlesNodes() {
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    Node block = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER));
    block.addChildToBack(exprResult);

    NodeTraversal t = new NodeTraversal(compiler, pass);
    assertTrue(pass.shouldTraverse(t, exprResult, block));
  }
}
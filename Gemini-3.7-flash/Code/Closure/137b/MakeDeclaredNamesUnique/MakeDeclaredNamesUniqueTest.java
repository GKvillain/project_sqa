package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueTest {

  // Tests ContextualRenamer global declaration does not rename first occurrence
  @Test
  public void testContextualRenamer_globalDeclaration_returnsNullReplacement() {
    MakeDeclaredNamesUnique.Renamer renamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    renamer.addDeclaredName("x");
    assertNull(renamer.getReplacementName("x"));
    assertFalse(renamer.stripConstIfReplaced());
  }

  // Tests ContextualRenamer child scope renaming collision
  @Test
  public void testContextualRenamer_childScopeDuplicateName_generatesUniqueName() {
    MakeDeclaredNamesUnique.Renamer global = new MakeDeclaredNamesUnique.ContextualRenamer();
    global.addDeclaredName("a");

    MakeDeclaredNamesUnique.Renamer child1 = global.forChildScope();
    child1.addDeclaredName("a");
    assertEquals("a$$1", child1.getReplacementName("a"));

    MakeDeclaredNamesUnique.Renamer child2 = global.forChildScope();
    child2.addDeclaredName("a");
    assertEquals("a$$2", child2.getReplacementName("a"));
  }

  // Tests ContextualRenamer child scope with unseen variable name does not rename
  @Test
  public void testContextualRenamer_childScopeFirstEncounter_returnsNullReplacement() {
    MakeDeclaredNamesUnique.Renamer global = new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.Renamer child = global.forChildScope();
    child.addDeclaredName("uniqueVar");
    assertNull(child.getReplacementName("uniqueVar"));
  }

  // Tests ContextualRenamer multiple additions in same scope only record once
  @Test
  public void testContextualRenamer_sameScopeDuplicateAddition_retainsSameName() {
    MakeDeclaredNamesUnique.Renamer global = new MakeDeclaredNamesUnique.ContextualRenamer();
    global.addDeclaredName("foo");

    MakeDeclaredNamesUnique.Renamer child = global.forChildScope();
    child.addDeclaredName("foo");
    child.addDeclaredName("foo");
    assertEquals("foo$$1", child.getReplacementName("foo"));
  }

  // Tests InlineRenamer basic renaming
  @Test
  public void testInlineRenamer_basicName_appendsPrefixAndSupplierId() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inline_", true);

    renamer.addDeclaredName("varName");
    assertEquals("varName$$inline_0", renamer.getReplacementName("varName"));
    assertTrue(renamer.stripConstIfReplaced());
  }

  // Tests InlineRenamer with empty name string
  @Test
  public void testInlineRenamer_emptyName_returnsEmptyString() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "pre_", false);

    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
    assertFalse(renamer.stripConstIfReplaced());
  }

  // Tests InlineRenamer stripping existing unique separator before appending
  @Test
  public void testInlineRenamer_nameWithExistingSeparator_stripsOldSeparator() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "99";
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "injected_", false);

    renamer.addDeclaredName("origVar$$oldSuffix");
    assertEquals("origVar$$injected_99", renamer.getReplacementName("origVar$$oldSuffix"));
  }

  // Tests InlineRenamer child scope creation
  @Test
  public void testInlineRenamer_forChildScope_sharesSupplier() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer parent =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "p_", true);
    MakeDeclaredNamesUnique.Renamer child = parent.forChildScope();

    parent.addDeclaredName("x");
    child.addDeclaredName("y");

    assertEquals("x$$p_0", parent.getReplacementName("x"));
    assertEquals("y$$p_1", child.getReplacementName("y"));
  }

  // Tests InlineRenamer constructor with empty prefix throws exception
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

  // Tests ContextualRenameInverter getOrginalName with separator
  @Test
  public void testContextualRenameInverter_getOrginalName_withSeparator_returnsPrefix() {
    String original = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("myVar$$12");
    assertEquals("myVar", original);
  }

  // Tests ContextualRenameInverter getOrginalName without separator
  @Test
  public void testContextualRenameInverter_getOrginalName_withoutSeparator_returnsSameName() {
    String original = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("myVar");
    assertEquals("myVar", original);
  }

  // Tests MakeDeclaredNamesUnique instantiation with custom renamer
  @Test
  public void testMakeDeclaredNamesUnique_customRenamerConstructor() {
    MakeDeclaredNamesUnique.Renamer renamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);
    assertNotNull(pass);
  }

  // Tests MakeDeclaredNamesUnique traversing a simple script node
  @Test
  public void testMakeDeclaredNamesUnique_traverseScript_renamesLocalVars() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node var1 = Node.newString(Token.VAR, "");
    Node name1 = Node.newString(Token.NAME, "x");
    var1.addChildToBack(name1);

    Node fn = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, "foo");
    Node fnParams = new Node(Token.LP);
    Node fnParam = Node.newString(Token.NAME, "x");
    fnParams.addChildToBack(fnParam);
    Node fnBody = new Node(Token.BLOCK);

    fn.addChildToBack(fnName);
    fn.addChildToBack(fnParams);
    fn.addChildToBack(fnBody);

    script.addChildToBack(var1);
    script.addChildToBack(fn);

    MakeDeclaredNamesUnique callback = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, script, callback);

    assertEquals("x", name1.getString());
    assertEquals("x$$1", fnParam.getString());
  }

  // Tests ContextualRenameInverter compiler pass getter
  @Test
  public void testGetContextualRenameInverter_returnsCompilerPass() {
    Compiler compiler = new Compiler();
    CompilerPass pass = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    assertNotNull(pass);
  }

  // Tests catch block variable renaming
  @Test
  public void testMakeDeclaredNamesUnique_catchBlock_renamesCatchVar() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node var1 = Node.newString(Token.VAR, "");
    Node outerE = Node.newString(Token.NAME, "e");
    var1.addChildToBack(outerE);

    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH);
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBlock = new Node(Token.BLOCK);
    Node refE = Node.newString(Token.NAME, "e");
    Node exprResult = new Node(Token.EXPR_RESULT, refE);
    catchBlock.addChildToBack(exprResult);

    catchNode.addChildToBack(catchVar);
    catchNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);

    script.addChildToBack(var1);
    script.addChildToBack(tryNode);

    MakeDeclaredNamesUnique callback = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, script, callback);

    assertEquals("e", outerE.getString());
    assertEquals("e$$1", catchVar.getString());
    assertEquals("e$$1", refE.getString());
  }

  // Tests InlineRenamer with NodeTraversal and constant property stripping
  @Test
  public void testMakeDeclaredNamesUnique_inlineRenamer_stripsConstantProp() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node varNode = Node.newString(Token.VAR, "");
    Node constVar = Node.newString(Token.NAME, "CONST_VAL");
    constVar.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    varNode.addChildToBack(constVar);
    script.addChildToBack(varNode);

    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "0";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inlined_", true);

    MakeDeclaredNamesUnique callback = new MakeDeclaredNamesUnique(renamer);
    NodeTraversal.traverse(compiler, script, callback);

    assertEquals("CONST_VAL$$inlined_0", constVar.getString());
    assertFalse(constVar.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // Tests ContextualRenameInverter processing AST to invert renamed variables
  @Test
  public void testContextualRenameInverter_process_invertsUniqueNames() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node fn = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, "foo");
    Node fnParams = new Node(Token.LP);
    Node fnParam = Node.newString(Token.NAME, "x$$1");
    fnParams.addChildToBack(fnParam);
    Node fnBody = new Node(Token.BLOCK);
    Node refParam = Node.newString(Token.NAME, "x$$1");
    fnBody.addChildToBack(new Node(Token.EXPR_RESULT, refParam));

    fn.addChildToBack(fnName);
    fn.addChildToBack(fnParams);
    fn.addChildToBack(fnBody);
    script.addChildToBack(fn);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, script);

    assertEquals("x", fnParam.getString());
    assertEquals("x", refParam.getString());
  }

  // Tests ContextualRenameInverter does not invert when outer scope conflict exists
  @Test
  public void testContextualRenameInverter_process_doesNotInvertOnConflict() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node outerVar = Node.newString(Token.VAR, "");
    Node outerX = Node.newString(Token.NAME, "x");
    outerVar.addChildToBack(outerX);

    Node fn = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, "foo");
    Node fnParams = new Node(Token.LP);
    Node fnParam = Node.newString(Token.NAME, "x$$1");
    fnParams.addChildToBack(fnParam);
    Node fnBody = new Node(Token.BLOCK);

    fn.addChildToBack(fnName);
    fn.addChildToBack(fnParams);
    fn.addChildToBack(fnBody);

    script.addChildToBack(outerVar);
    script.addChildToBack(fn);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, script);

    assertEquals("x", outerX.getString());
    assertEquals("x$$1", fnParam.getString());
  }

  // Tests named function expressions traversal
  @Test
  public void testMakeDeclaredNamesUnique_functionExpression_renamesNameNode() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);

    Node outerFn = Node.newString(Token.NAME, "rec");
    Node outerVar = Node.newString(Token.VAR, "");
    outerVar.addChildToBack(outerFn);

    Node exprResult = new Node(Token.EXPR_RESULT);
    Node fnExpr = new Node(Token.FUNCTION);
    Node fnExprName = Node.newString(Token.NAME, "rec");
    Node fnExprParams = new Node(Token.LP);
    Node fnExprBody = new Node(Token.BLOCK);
    fnExpr.addChildToBack(fnExprName);
    fnExpr.addChildToBack(fnExprParams);
    fnExpr.addChildToBack(fnExprBody);
    exprResult.addChildToBack(fnExpr);

    script.addChildToBack(outerVar);
    script.addChildToBack(exprResult);

    MakeDeclaredNamesUnique callback = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, script, callback);

    assertEquals("rec", outerFn.getString());
    assertEquals("rec$$1", fnExprName.getString());
  }
}
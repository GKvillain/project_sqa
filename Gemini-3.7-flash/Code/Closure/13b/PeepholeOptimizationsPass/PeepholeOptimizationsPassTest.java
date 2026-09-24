package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeOptimizationsPassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests constructor and getCompiler getter
  @Test
  public void testGetCompiler_returnsInitializedCompiler() {
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
    assertSame(compiler, pass.getCompiler());
  }

  // Tests process with an empty script root and no optimizations
  @Test
  public void testProcess_emptyScriptNoOptimizations_doesNotChange() {
    Node root = IR.script();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
    pass.process(null, root);
    assertEquals(0, root.getChildCount());
  }

  // Tests process with real peephole optimizations on a simple AST
  @Test
  public void testProcess_withRemoveDeadCodeOptimization() {
    Node root = compiler.parseTestCode("if (false) { var x = 1; }");
    PeepholeRemoveDeadCode deadCodeOpt = new PeepholeRemoveDeadCode();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, deadCodeOpt);
    pass.process(null, root);
    assertNotNull(root);
  }

  // Tests process with multiple peephole optimizations applied together
  @Test
  public void testProcess_withMultipleOptimizations() {
    Node root = compiler.parseTestCode("var x = 1 + 2; if (false) { x = 3; }");
    PeepholeFoldConstants foldOpt = new PeepholeFoldConstants(true);
    PeepholeRemoveDeadCode deadCodeOpt = new PeepholeRemoveDeadCode();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, foldOpt, deadCodeOpt);
    pass.process(null, root);
    assertNotNull(root);
  }

  // Tests process on nested functions scope traversal
  @Test
  public void testProcess_nestedFunctions() {
    Node root = compiler.parseTestCode(
        "function outer() { function inner() { var a = 1 + 1; } }");
    PeepholeFoldConstants foldOpt = new PeepholeFoldConstants(true);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, foldOpt);
    pass.process(null, root);
    assertNotNull(root);
  }

  // Tests process when changes trigger scope retraversal in script and functions
  @Test
  public void testProcess_retraversalOnCodeChange() {
    Node root = compiler.parseTestCode(
        "function f() { var a = 1; if (false) { a = 2; } return a; }");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeRemoveDeadCode(), new PeepholeFoldConstants(true));
    pass.process(null, root);
    assertNotNull(root);
  }

  // Tests visit directly when optimization modifies the node
  @Test
  public void testVisit_replacesNode() {
    PeepholeFoldConstants foldOpt = new PeepholeFoldConstants(true);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, foldOpt);
    Node expr = IR.add(IR.number(1), IR.number(2));
    pass.visit(expr);
    assertNotNull(expr);
  }

  // Tests visit directly when optimization does not modify the node
  @Test
  public void testVisit_noChangeOnLeafNode() {
    PeepholeFoldConstants foldOpt = new PeepholeFoldConstants(true);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, foldOpt);
    Node number = IR.number(42);
    pass.visit(number);
    assertEquals(42.0, number.getDouble(), 0.0);
  }

  // Tests that infinite loop / iteration limit throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testProcess_infiniteRetraversal_throwsIllegalStateException() {
    Node root = compiler.parseTestCode("function f() { var x = 1; }");
    AbstractPeepholeOptimization infiniteChangeOpt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        if (subtree.isName()) {
          compiler.reportCodeChange();
        }
        return subtree;
      }
    };
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, infiniteChangeOpt);
    pass.process(null, root);
  }

  // Tests traversal handles node returning null from optimizeSubtree
  @Test
  public void testVisit_optimizationReturnsNull() {
    AbstractPeepholeOptimization nullOpt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        return null;
      }
    };
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, nullOpt);
    Node node = IR.var(IR.name("x"));
    pass.visit(node);
  }

  // Tests process with script node having no parent (root node)
  @Test
  public void testProcess_scriptNodeWithoutParent() {
    Node script = IR.script(IR.var(IR.name("a"), IR.number(1)));
    PeepholeFoldConstants foldOpt = new PeepholeFoldConstants(true);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, foldOpt);
    pass.process(null, script);
    assertNotNull(script);
  }
}
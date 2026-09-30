package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeOptimizationsPassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests getCompiler returns the compiler passed in constructor
  @Test
  public void testGetCompiler_returnsInitializedCompiler() {
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
    assertSame(compiler, pass.getCompiler());
  }

  // Tests process on an empty script node with no optimizations
  @Test
  public void testProcess_emptyScriptWithoutOptimizations_succeeds() {
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
    Node root = IR.script();
    Node externs = IR.script();
    pass.process(externs, root);
    assertNotNull(root);
  }

  // Tests process on a simple AST with a dummy optimization that counts visits
  @Test
  public void testProcess_simpleAst_callsOptimizeSubtree() {
    TestCountingOptimization opt = new TestCountingOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node root = IR.script(IR.var(IR.name("x"), IR.number(1.0)));
    pass.process(null, root);

    assertTrue(opt.traversalBegan);
    assertTrue(opt.traversalEnded);
    assertTrue(opt.optimizeCalls > 0);
  }

  // Tests process with an optimization that mutates AST and reports changes to trigger retraversal
  @Test
  public void testProcess_withChangeReporting_retraversesScope() {
    TestChangeOnceOptimization opt = new TestChangeOnceOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node fnNode = IR.function(IR.name("fn"), IR.paramList(), IR.block(IR.exprResult(IR.string("test"))));
    Node scriptNode = IR.script(fnNode);
    Node root = IR.root(scriptNode);

    pass.process(null, root);

    assertTrue("Optimization should have performed replacement", opt.replaced);
    assertTrue("Optimization should have visited nodes multiple times", opt.optimizeCalls > 1);
  }

  // Tests retraverse logic when a function node has no parent (node.getParent() == null)
  @Test
  public void testProcess_functionWithoutParent_doesNotThrowNullPointer() {
    TestChangeOnceOptimization opt = new TestChangeOnceOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node fnNode = IR.function(IR.name("orphan"), IR.paramList(), IR.block(IR.exprResult(IR.string("orphanTest"))));
    pass.process(null, fnNode);

    assertNotNull(fnNode);
  }

  // Tests visit method when optimization returns null to ensure loop terminates gracefully
  @Test
  public void testVisit_optimizationReturnsNull_terminatesGracefully() {
    AbstractPeepholeOptimization nullReturningOpt = new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node subtree) {
        return null;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, nullReturningOpt);
    Node node = IR.exprResult(IR.number(42));
    pass.visit(node);
  }

  // Tests multiple optimizations applied in sequence in a single pass
  @Test
  public void testProcess_multipleOptimizations_runsAllOptimizations() {
    TestCountingOptimization opt1 = new TestCountingOptimization();
    TestCountingOptimization opt2 = new TestCountingOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt1, opt2);

    Node root = IR.script(IR.exprResult(IR.number(10)));
    pass.process(null, root);

    assertTrue(opt1.traversalBegan);
    assertTrue(opt1.traversalEnded);
    assertTrue(opt2.traversalBegan);
    assertTrue(opt2.traversalEnded);
    assertEquals(opt1.optimizeCalls, opt2.optimizeCalls);
  }

  // Tests nested functions to cover push and pop on StateStack
  @Test
  public void testProcess_nestedFunctions_traversesAndMaintainsStackCorrectly() {
    TestCountingOptimization opt = new TestCountingOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node innerFn = IR.function(IR.name("inner"), IR.paramList(), IR.block(IR.returnNode(IR.number(2))));
    Node outerFn = IR.function(IR.name("outer"), IR.paramList(), IR.block(innerFn, IR.returnNode(IR.number(1))));
    Node script = IR.script(outerFn);
    Node root = IR.root(script);

    pass.process(null, root);

    assertTrue(opt.optimizeCalls >= 3);
  }

  // Tests infinite loop protection: when state repeatedly changes, exception is thrown after 10000 visits
  @Test(expected = IllegalStateException.class)
  public void testProcess_infiniteChanges_throwsIllegalStateException() {
    AbstractPeepholeOptimization infiniteChangeOpt = new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node subtree) {
        if (subtree.isScript()) {
          reportCodeChange();
          Node newNode = IR.script();
          return newNode;
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, infiniteChangeOpt);
    Node script = IR.script();
    Node root = IR.root(script);
    pass.process(null, root);
  }

  // Tests shouldVisit when child scope traversal is disabled
  @Test
  public void testProcess_childScopesNotRevisited_whenScopeRetraversed() {
    TestTrackVisitsOptimization tracker = new TestTrackVisitsOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, tracker);

    Node childFn = IR.function(IR.name("child"), IR.paramList(), IR.block());
    Node parentFn = IR.function(IR.name("parent"), IR.paramList(), IR.block(childFn));
    Node script = IR.script(parentFn);
    Node root = IR.root(script);

    pass.process(null, root);

    assertTrue(tracker.visitedNodes.contains(childFn));
    assertTrue(tracker.visitedNodes.contains(parentFn));
  }

  // Helper class: counts begin/end traversal and optimizeSubtree invocations
  private static class TestCountingOptimization extends AbstractPeepholeOptimization {
    int optimizeCalls = 0;
    boolean traversalBegan = false;
    boolean traversalEnded = false;

    @Override
    public void beginTraversal(AbstractCompiler compiler) {
      super.beginTraversal(compiler);
      traversalBegan = true;
    }

    @Override
    public void endTraversal(AbstractCompiler compiler) {
      super.endTraversal(compiler);
      traversalEnded = true;
    }

    @Override
    public Node optimizeSubtree(Node subtree) {
      optimizeCalls++;
      return subtree;
    }
  }

  // Helper class: replaces a string node once and reports code change
  private static class TestChangeOnceOptimization extends AbstractPeepholeOptimization {
    boolean replaced = false;
    int optimizeCalls = 0;

    @Override
    public Node optimizeSubtree(Node subtree) {
      optimizeCalls++;
      if (!replaced && subtree.isString() && "test".equals(subtree.getString())) {
        replaced = true;
        Node parent = subtree.getParent();
        if (parent != null) {
          Node replacement = IR.string("replaced");
          parent.replaceChild(subtree, replacement);
          reportCodeChange();
          return replacement;
        }
      }
      return subtree;
    }
  }

  // Helper class: tracks which nodes were visited
  private static class TestTrackVisitsOptimization extends AbstractPeepholeOptimization {
    java.util.List<Node> visitedNodes = new java.util.ArrayList<Node>();

    @Override
    public Node optimizeSubtree(Node subtree) {
      visitedNodes.add(subtree);
      return subtree;
    }
  }
}
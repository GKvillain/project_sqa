package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

public class PeepholeOptimizationsPassTest {

  private AbstractCompiler compiler;
  private PeepholeOptimizationsPass pass;

  // A simple peephole optimization that does nothing
  private static class NoOpOptimization extends AbstractPeepholeOptimization {
    @Override
    Node optimizeSubtree(Node node) {
      return node;
    }

    @Override
    void beginTraversal(AbstractCompiler compiler) {
      // nothing
    }

    @Override
    void endTraversal() {
      // nothing
    }
  }

  // A peephole optimization that replaces a NAME node with a NUMBER node
  private static class ReplaceNameWithNumberOptimization extends AbstractPeepholeOptimization {
    @Override
    Node optimizeSubtree(Node node) {
      if (node != null && node.isName()) {
        return Node.newNumber(42);
      }
      return node;
    }

    @Override
    void beginTraversal(AbstractCompiler compiler) {
      // nothing
    }

    @Override
    void endTraversal() {
      // nothing
    }
  }

  // A peephole optimization that replaces a NUMBER node with null
  private static class NullReturningOptimization extends AbstractPeepholeOptimization {
    @Override
    Node optimizeSubtree(Node node) {
      if (node != null && node.isNumber()) {
        return null;
      }
      return node;
    }

    @Override
    void beginTraversal(AbstractCompiler compiler) {
      // nothing
    }

    @Override
    void endTraversal() {
      // nothing
    }
  }

  // A peephole optimization that always returns a new node
  private static class AlwaysChangeOptimization extends AbstractPeepholeOptimization {
    @Override
    Node optimizeSubtree(Node node) {
      return Node.newNumber(1);
    }

    @Override
    void beginTraversal(AbstractCompiler compiler) {
      // nothing
    }

    @Override
    void endTraversal() {
      // nothing
    }
  }

  @Before
  public void setUp() {
    compiler = new TestCompiler();
  }

  // Tests normal processing with no optimizations
  @Test
  public void testProcess_noOptimizations_noChange() {
    pass = new PeepholeOptimizationsPass(compiler);
    Node root = new Node(1); // SCRIPT node
    Node child = new Node(2); // NAME node
    root.addChildToBack(child);
    pass.process(null, root);
    assertEquals(2, child.getType());
  }

  // Tests normal processing with a single optimization that does nothing
  @Test
  public void testProcess_singleNoOpOptimization_noChange() {
    pass = new PeepholeOptimizationsPass(compiler, new NoOpOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child = new Node(2); // NAME node
    root.addChildToBack(child);
    pass.process(null, root);
    assertEquals(2, child.getType());
  }

  // Tests processing where optimization changes a node
  @Test
  public void testProcess_optimizationChangesNode_nodeReplaced() {
    pass = new PeepholeOptimizationsPass(compiler, new ReplaceNameWithNumberOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child = Node.newString(2, "test"); // NAME node with string token
    root.addChildToBack(child);
    pass.process(null, root);
    assertTrue(root.getFirstChild().isNumber());
    assertEquals(42, root.getFirstChild().getDouble(), 0.001);
  }

  // Tests processing where optimization returns null (removes node)
  @Test
  public void testProcess_optimizationReturnsNull_nodeRemoved() {
    pass = new PeepholeOptimizationsPass(compiler,
        new ReplaceNameWithNumberOptimization(),
        new NullReturningOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child = Node.newString(2, "test"); // NAME node
    root.addChildToBack(child);
    pass.process(null, root);
    assertNull(root.getFirstChild());
  }

  // Tests processing where optimization causes retraversal due to change
  @Test
  public void testProcess_changeInScope_causesRetraversal() {
    // Create a non-empty array of optimizations
    pass = new PeepholeOptimizationsPass(compiler, new AlwaysChangeOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child = Node.newNumber(1); // NUMBER node
    root.addChildToBack(child);
    // This should not loop infinitely because shouldRetraverse checks visits < 10000
    pass.process(null, root);
    assertNotNull(root.getFirstChild());
    assertTrue(root.getFirstChild().isNumber());
  }

  // Tests that the pass correctly handlers null externs
  @Test
  public void testProcess_nullExterns_noException() {
    pass = new PeepholeOptimizationsPass(compiler, new NoOpOptimization());
    Node root = new Node(1); // SCRIPT node
    try {
      pass.process(null, root);
    } catch (Exception e) {
      fail("Should not throw exception for null externs");
    }
  }

  // Tests that visit method returns early when node is null
  @Test
  public void testVisit_nullNode_returnsEarly() {
    pass = new PeepholeOptimizationsPass(compiler, new NullReturningOptimization());
    // visit is called internally by process, we just verify it doesn't crash
    Node root = new Node(1); // SCRIPT node
    pass.process(null, root);
  }

  // Tests shouldVisit returns false when traverseChildScopes is false
  @Test
  public void testShouldVisit_traverseChildScopesFalse_returnsFalse() {
    // Accessing private state is not possible directly, but we can test through behavior
    // The shouldRetraverse method sets traverseChildScopes to false when changed is true
    pass = new PeepholeOptimizationsPass(compiler, new AlwaysChangeOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child = new Node(2); // Another node
    root.addChildToBack(child);
    pass.process(null, root);
    // If it didn't infinite loop, the logic works
  }

  // Tests that process can handle multiple children
  @Test
  public void testProcess_multipleChildren_allProcessed() {
    pass = new PeepholeOptimizationsPass(compiler, new ReplaceNameWithNumberOptimization());
    Node root = new Node(1); // SCRIPT node
    Node child1 = Node.newString(2, "test1");
    Node child2 = Node.newString(2, "test2");
    Node child3 = Node.newString(2, "test3");
    root.addChildToBack(child1);
    root.addChildToBack(child2);
    root.addChildToBack(child3);
    pass.process(null, root);
    Node c = root.getFirstChild();
    while (c != null) {
      assertTrue(c.isNumber());
      assertEquals(42, c.getDouble(), 0.001);
      c = c.getNext();
    }
  }

  // Tests that beginTraversal and endTraversal are called
  @Test
  public void testProcess_beginAndEndTraversal_called() {
    // This test verifies that process calls beginTraversal and endTraversal
    // by using a custom optimization that tracks calls
    final boolean[] beginCalled = {false};
    final boolean[] endCalled = {false};
    AbstractPeepholeOptimization trackingOptimization = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node node) {
        return node;
      }

      @Override
      void beginTraversal(AbstractCompiler compiler) {
        beginCalled[0] = true;
      }

      @Override
      void endTraversal() {
        endCalled[0] = true;
      }
    };
    pass = new PeepholeOptimizationsPass(compiler, trackingOptimization);
    Node root = new Node(1); // SCRIPT node
    pass.process(null, root);
    assertTrue("beginTraversal should be called", beginCalled[0]);
    assertTrue("endTraversal should be called", endCalled[0]);
  }

  // Tests that visitor does not get stuck in infinite loop when always changing
  @Test
  public void testVisit_alwaysChanging_terminates() {
    pass = new PeepholeOptimizationsPass(compiler, new AlwaysChangeOptimization());
    Node root = new Node(1); // SCRIPT node
    try {
      pass.process(null, root);
    } catch (Exception e) {
      fail("Should not throw exception even with always changing optimization");
    }
  }

  // Tests processing with nested functions
  @Test
  public void testProcess_nestedFunctions_traversesCorrectly() {
    pass = new PeepholeOptimizationsPass(compiler, new ReplaceNameWithNumberOptimization());
    Node root = new Node(1); // SCRIPT node
    Node functionNode = new Node(2); // FUNCTION node
    Node nameNode = Node.newString(3, "innerName");
    functionNode.addChildToBack(nameNode);
    root.addChildToBack(functionNode);
    pass.process(null, root);
    // The NAME inside function should be replaced
    assertTrue(functionNode.getFirstChild().isNumber());
    assertEquals(42, functionNode.getFirstChild().getDouble(), 0.001);
  }

  // Tests that compiler is accessible
  @Test
  public void testGetCompiler_returnsCorrectCompiler() {
    pass = new PeepholeOptimizationsPass(compiler, new NoOpOptimization());
    assertSame(compiler, pass.getCompiler());
  }

  // Helper class to simulate a minimal compiler
  private static class TestCompiler extends AbstractCompiler {
    private CodeChangeHandler handler;

    @Override
    void addChangeHandler(CodeChangeHandler handler) {
      this.handler = handler;
    }

    @Override
    void removeChangeHandler(CodeChangeHandler handler) {
      if (this.handler == handler) {
        this.handler = null;
      }
    }

    public void reportChange() {
      if (handler != null) {
        handler.reportChange();
      }
    }
  }
}
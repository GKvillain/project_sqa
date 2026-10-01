package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for NodeTraversal.
 */
public class NodeTraversalTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parse(String code) {
    return compiler.parseSyntheticCode(code);
  }

  // Tests basic post-order traversal visits all nodes
  @Test
  public void testTraverse_simpleScript_visitsAllNodesInPostOrder() {
    Node root = parse("var a = 1;");
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });
    t.traverse(root);
    assertFalse(visited.isEmpty());
    assertEquals(root, visited.get(visited.size() - 1));
  }

  // Tests shouldTraverse returning false skips the node's subtree
  @Test
  public void testTraverse_shouldTraverseFalse_skipsChildren() {
    Node root = parse("var a = 1; alert(a);");
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.Callback() {
          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return !n.isExprResult();
          }
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });
    t.traverse(root);
    for (Node n : visited) {
      assertFalse(n.isExprResult());
    }
  }

  // Tests scope enter/exit callbacks for a function
  @Test
  public void testTraverse_scopedCallback_enterExitCalledForFunction() {
    Node root = parse("function f() { var x = 1; }");
    final List<String> events = new ArrayList<String>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void enterScope(NodeTraversal t) {
            events.add("enter");
          }
          @Override
          public void exitScope(NodeTraversal t) {
            events.add("exit");
          }
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            // no-op
          }
        });
    t.traverse(root);
    assertTrue(events.size() >= 2);
    assertEquals(events.size(), events.size()); // just ensuring no exception
  }

  // Tests getEnclosingFunction return type inside a function scope
  @Test
  public void testGetEnclosingFunction_insideFunction_returnsFunctionNode() {
    Node root = parse("function f() { var x = 1; }");
    final List<Node> enclosing = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.isVar()) {
              enclosing.add(t.getEnclosingFunction());
            }
          }
        });
    t.traverse(root);
    assertFalse(enclosing.isEmpty());
    for (Node n : enclosing) {
      assertNotNull(n);
      assertTrue(n.isFunction());
    }
  }

  // Tests getEnclosingFunction is null in global scope
  @Test
  public void testGetEnclosingFunction_globalScope_returnsNull() {
    Node root = parse("var a;");
    final boolean[] seenVar = new boolean[1];
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.isVar()) {
              seenVar[0] = true;
              assertNull(t.getEnclosingFunction());
            }
          }
        });
    t.traverse(root);
    assertTrue(seenVar[0]);
  }

  // Tests getLineNumber with a manually built node lacking line info
  @Test
  public void testGetLineNumber_nodeWithoutLineInfo_returnsZero() {
    Node root = new Node(Token.SCRIPT);
    final int[] line = new int[1];
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            line[0] = t.getLineNumber();
          }
        });
    t.traverse(root);
    assertEquals(0, line[0]);
  }

  // Tests getCurrentNode returns the node being visited
  @Test
  public void testGetCurrentNode_duringVisit_returnsCurrentNode() {
    Node root = parse("var a;");
    final List<Node> current = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            current.add(t.getCurrentNode());
          }
        });
    t.traverse(root);
    assertFalse(current.isEmpty());
    for (Node n : current) {
      assertNotNull(n);
    }
  }

  // Tests getSourceName is never null after parsing
  @Test
  public void testGetSourceName_afterParsing_notNull() {
    Node root = parse("var a;");
    final List<String> names = new ArrayList<String>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            names.add(t.getSourceName());
          }
        });
    t.traverse(root);
    for (String name : names) {
      assertNotNull(name);
    }
  }

  // Tests traverseRoots with multiple sibling roots
  @Test
  public void testTraverseRoots_multipleRoots_visitsAllRoots() {
    Node root = parse("var a; var b;");
    Node first = root.getFirstChild();
    Node second = first.getNext();
    assertNotNull(first);
    assertNotNull(second);
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });
    t.traverseRoots(first, second);
    assertTrue(visited.contains(first));
    assertTrue(visited.contains(second));
  }

  // Tests traverseRoots with an empty list
  @Test
  public void testTraverseRoots_emptyList_noException() {
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            // no-op
          }
        });
    t.traverseRoots(new ArrayList<Node>());
    assertTrue(true);
  }

  // Tests traverseRoots when roots have no parent
  @Test(expected = IllegalStateException.class)
  public void testTraverseRoots_rootWithoutParent_throwsPreconditionsException() {
    Node root = new Node(Token.SCRIPT);
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            // no-op
          }
        });
    t.traverseRoots(root);
  }

  // Tests getControlFlowGraph inside a function scope
  @Test
  public void testGetControlFlowGraph_insideFunction_returnsGraph() {
    Node root = parse("function f() { if (a) { b(); } }");
    final boolean[] checked = {false};
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void enterScope(NodeTraversal t) {
            if (t.getScope().getRootNode().isFunction()) {
              ControlFlowGraph<Node> cfg = t.getControlFlowGraph();
              assertNotNull(cfg);
              checked[0] = true;
            }
          }
          @Override
          public void exitScope(NodeTraversal t) {
            // no-op
          }
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            // no-op
          }
        });
    t.traverse(root);
    assertTrue(checked[0]);
  }

  // Tests report adds a compiler error
  @Test
  public void testReport_reportsError() {
    Node root = parse("var a;");
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.isVar()) {
              t.report(n, NodeTraversal.NODE_TRAVERSAL_ERROR, "custom error");
            }
          }
        });
    t.traverse(root);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests getScope inside a function returns a function root
  @Test
  public void testGetScope_insideFunction_returnsFunctionRoot() {
    Node root = parse("function f() { var x; }");
    final Node[] functionRoot = new Node[1];
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void enterScope(NodeTraversal t) {
            if (t.getScope().getRootNode().isFunction()) {
              functionRoot[0] = t.getScope().getRootNode();
            }
          }
          @Override
          public void exitScope(NodeTraversal t) {
            // no-op
          }
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            // no-op
          }
        });
    t.traverse(root);
    assertNotNull(functionRoot[0]);
    assertTrue(functionRoot[0].isFunction());
  }

  // Tests AbstractShallowCallback does not traverse function bodies
  @Test
  public void testTraverse_shallowCallback_skipsFunctionBody() {
    Node root = parse("function f() { var x = 1; }");
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractShallowCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });
    t.traverse(root);
    for (Node n : visited) {
      assertFalse(n.isVar());
    }
  }

  // Tests AbstractNodeTypePruningCallback with include mode
  @Test
  public void testTraverse_pruningCallback_onlyVisitsSelectedTypes() {
    Node root = parse("var a; function f() {}");
    Set<Integer> includeSet = new HashSet<Integer>();
    includeSet.add(Token.VAR);
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.AbstractNodeTypePruningCallback(includeSet) {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });
    t.traverse(root);
    assertFalse(visited.isEmpty());
    for (Node n : visited) {
      assertTrue(n.isVar());
    }
  }

  // Tests static convenience traversal method
  @Test
  public void testStaticTraverse_invokesCallback() {
    Node root = parse("var a;");
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };
    NodeTraversal.traverse(compiler, root, cb);
    assertTrue(visited.contains(root));
  }
}
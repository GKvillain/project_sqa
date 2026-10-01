package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeTraversalTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // Tests basic post-order traversal with AbstractPostOrderCallback
  @Test
  public void testTraverse_simpleTree_visitsAllNodesPostOrder() {
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node num = Node.newNumber(42);
    expr.addChildToBack(num);
    root.addChildToBack(expr);

    final List<Integer> visitedTypes = new ArrayList<Integer>();
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visitedTypes.add(n.getType());
      }
    });

    assertEquals(3, visitedTypes.size());
    assertEquals(Integer.valueOf(Token.NUMBER), visitedTypes.get(0));
    assertEquals(Integer.valueOf(Token.EXPR_RESULT), visitedTypes.get(1));
    assertEquals(Integer.valueOf(Token.BLOCK), visitedTypes.get(2));
  }

  // Tests traversal with ScopedCallback entering and exiting scopes
  @Test
  public void testTraverse_scopedCallback_tracksScopeDepthAndRoots() {
    Node script = new Node(Token.SCRIPT);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    script.addChildToBack(fn);

    final List<String> events = new ArrayList<String>();
    NodeTraversal.Callback cb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        events.add("enter:" + t.getScopeDepth());
      }

      @Override
      public void exitScope(NodeTraversal t) {
        events.add("exit:" + t.getScopeDepth());
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(script);

    assertEquals(4, events.size());
    assertEquals("enter:1", events.get(0));
    assertEquals("enter:2", events.get(1));
    assertEquals("exit:2", events.get(2));
    assertEquals("exit:1", events.get(3));
  }

  // Tests getEnclosingFunction within function body and globally
  @Test
  public void testGetEnclosingFunction_insideFunction_returnsFunctionNode() {
    final Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);

    final List<Node> enclosingList = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isBlock() && parent == fn) {
          enclosingList.add(t.getEnclosingFunction());
        }
      }
    };

    NodeTraversal.traverse(compiler, script, cb);
    assertEquals(1, enclosingList.size());
    assertEquals(fn, enclosingList.get(0));
  }

  // Tests AbstractShallowCallback prune behavior for functions
  @Test
  public void testAbstractShallowCallback_functionNode_skipsBody() {
    Node script = new Node(Token.SCRIPT);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "bar"), new Node(Token.LP), new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    script.addChildToBack(fn);

    final List<Integer> visited = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractShallowCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n.getType());
      }
    };

    NodeTraversal.traverse(compiler, script, cb);
    assertTrue(visited.contains(Token.NAME));
    assertTrue(visited.contains(Token.FUNCTION));
    assertFalse(visited.contains(Token.NUMBER));
  }

  // Tests AbstractShallowStatementCallback pruning expressions
  @Test
  public void testAbstractShallowStatementCallback_statements_traversesBlocksOnly() {
    Node block = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(10));
    block.addChildToBack(expr);

    final List<Integer> visited = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractShallowStatementCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n.getType());
      }
    };

    NodeTraversal.traverse(compiler, block, cb);
    assertTrue(visited.contains(Token.EXPR_RESULT));
    assertFalse(visited.contains(Token.NUMBER));
  }

  // Tests AbstractNodeTypePruningCallback inclusion mode
  @Test
  public void testAbstractNodeTypePruningCallback_includeTypes_traversesOnlyMatching() {
    Node root = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(100)));
    Set<Integer> include = ImmutableSet.of(Token.BLOCK, Token.EXPR_RESULT);

    final List<Integer> visited = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractNodeTypePruningCallback(include, true) {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n.getType());
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertTrue(visited.contains(Token.EXPR_RESULT));
    assertFalse(visited.contains(Token.NUMBER));
  }

  // Tests AbstractNodeTypePruningCallback exclusion mode
  @Test
  public void testAbstractNodeTypePruningCallback_excludeTypes_skipsMatching() {
    Node root = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(100)));
    Set<Integer> exclude = ImmutableSet.of(Token.EXPR_RESULT);

    final List<Integer> visited = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractNodeTypePruningCallback(exclude, false) {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n.getType());
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertEquals(1, visited.size());
    assertEquals(Integer.valueOf(Token.BLOCK), visited.get(0));
  }

  // Tests function expression traversal with name inside function scope
  @Test
  public void testTraverse_functionExpression_traversesCorrectly() {
    Node expr = new Node(Token.EXPR_RESULT);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "namedExpr"), new Node(Token.LP), new Node(Token.BLOCK));
    expr.addChildToBack(fn);

    final List<String> scopesEntered = new ArrayList<String>();
    NodeTraversal.Callback cb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        scopesEntered.add(t.getScopeRoot().toString());
      }

      @Override
      public void exitScope(NodeTraversal t) {}
    };

    NodeTraversal.traverse(compiler, expr, cb);
    assertEquals(2, scopesEntered.size());
  }

  // Tests traverseRoots with empty list
  @Test
  public void testTraverseRoots_emptyList_doesNothing() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal.traverseRoots(compiler, Collections.<Node>emptyList(), cb);
    assertTrue(visited.isEmpty());
  }

  // Tests traverseRoots with valid child nodes sharing parent
  @Test
  public void testTraverseRoots_multipleRoots_traversesAll() {
    Node parent = new Node(Token.BLOCK);
    Node c1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node c2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    parent.addChildToBack(c1);
    parent.addChildToBack(c2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal.traverseRoots(compiler, cb, c1, c2);
    assertTrue(visited.contains(c1));
    assertTrue(visited.contains(c2));
  }

  // Tests traverseRoots throws RuntimeException when roots have no parent
  @Test(expected = RuntimeException.class)
  public void testTraverseRoots_nullParent_throwsException() {
    Node root1 = new Node(Token.BLOCK);
    Node root2 = new Node(Token.BLOCK);

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    };

    NodeTraversal.traverseRoots(compiler, cb, root1, root2);
  }

  // Tests traverseInnerNode with null refinedScope
  @Test
  public void testTraverseInnerNode_nullScope_traversesBranch() {
    final Node root = new Node(Token.BLOCK);
    final Node child = new Node(Token.EXPR_RESULT);
    root.addChildToBack(child);

    final List<Node> innerVisited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == root) {
          t.traverseInnerNode(child, root, null);
        }
        innerVisited.add(n);
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);
    assertTrue(innerVisited.contains(child));
  }

  // Tests getLineNumber returns line when present or traverses parent
  @Test
  public void testGetLineNumber_withLineno_returnsCorrectLineno() {
    Node parent = new Node(Token.BLOCK);
    parent.setLineno(15);
    Node child = new Node(Token.EXPR_RESULT);
    parent.addChildToBack(child);

    final int[] lineNumbers = new int[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isExprResult()) {
          lineNumbers[0] = t.getLineNumber();
        }
      }
    };

    NodeTraversal.traverse(compiler, parent, cb);
    assertEquals(15, lineNumbers[0]);
  }

  // Tests inGlobalScope and getScopeRoot
  @Test
  public void testInGlobalScope_atRoot_returnsTrue() {
    final Node root = new Node(Token.BLOCK);
    final boolean[] isGlobal = new boolean[1];
    final Node[] scopeRoot = new Node[1];

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == root) {
          isGlobal[0] = t.inGlobalScope();
          scopeRoot[0] = t.getScopeRoot();
        }
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertTrue(isGlobal[0]);
    assertEquals(root, scopeRoot[0]);
  }

  // Tests report diagnostic and makeError creation
  @Test
  public void testReportAndMakeError_createsJSErrorCorrectly() {
    Node node = new Node(Token.NAME);
    node.setLineno(5);
    node.setCharno(10);

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });

    JSError error = t.makeError(node, NodeTraversal.NODE_TRAVERSAL_ERROR, "error msg");
    assertNotNull(error);
    assertEquals(5, error.lineNumber);
    assertEquals(10, error.getCharno());

    t.report(node, NodeTraversal.NODE_TRAVERSAL_ERROR, "reported error");
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests getControlFlowGraph lazily generates CFG
  @Test
  public void testGetControlFlowGraph_returnsNonNullCfg() {
    Node root = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));

    final ControlFlowGraph<Node>[] cfgHolder = new ControlFlowGraph[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isBlock()) {
          cfgHolder[0] = t.getControlFlowGraph();
        }
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertNotNull(cfgHolder[0]);
  }

  // Tests traversing malformed function without block throws RuntimeException
  @Test(expected = RuntimeException.class)
  public void testTraverseFunction_bodyNotBlock_throwsException() {
    Node badFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "bad"), new Node(Token.LP), new Node(Token.EXPR_RESULT));
    Node script = new Node(Token.SCRIPT, badFn);

    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
  }

  // Tests SCRIPT node updates inputId and sourceName
  @Test
  public void testTraverse_scriptNode_setsInputIdAndSourceName() {
    Node script = new Node(Token.SCRIPT);
    script.setInputId(new InputId("input_test.js"));
    script.setSourceFileName("input_test.js");

    final InputId[] idHolder = new InputId[1];
    final String[] nameHolder = new String[1];

    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        idHolder[0] = t.getInputId();
        nameHolder[0] = t.getSourceName();
      }
    });

    assertEquals(new InputId("input_test.js"), idHolder[0]);
    assertEquals("input_test.js", nameHolder[0]);
  }

  // Tests AbstractPreOrderCallback visits nodes in pre-order
  @Test
  public void testAbstractPreOrderCallback_visitsInPreOrder() {
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node num = Node.newNumber(1);
    expr.addChildToBack(num);
    root.addChildToBack(expr);

    final List<Integer> visited = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPreOrderCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        visited.add(n.getType());
        return true;
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertEquals(3, visited.size());
    assertEquals(Integer.valueOf(Token.BLOCK), visited.get(0));
    assertEquals(Integer.valueOf(Token.EXPR_RESULT), visited.get(1));
    assertEquals(Integer.valueOf(Token.NUMBER), visited.get(2));
  }

  // Tests shouldTraverse returning false prunes traversal of subtrees
  @Test
  public void testTraverse_callbackPrunesSubtree_childrenNotVisited() {
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(5));
    root.addChildToBack(expr);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.Callback() {
      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        return n != expr;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    assertEquals(1, visited.size());
    assertEquals(root, visited.get(0));
  }

  // Tests traverseGlobal static method
  @Test
  public void testTraverseGlobal_runsCallbackOnRoot() {
    Node root = new Node(Token.BLOCK);
    final boolean[] visited = new boolean[1];

    NodeTraversal.traverseGlobal(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == root) {
          visited[0] = true;
        }
      }
    });

    assertTrue(visited[0]);
  }

  // Tests getCompiler, getCurrentNode, and hasHaltingErrors accessors
  @Test
  public void testGetters_compilerAndCurrentNodeAndHaltingErrors() {
    final Node root = new Node(Token.BLOCK);
    final Node[] current = new Node[1];
    final Compiler[] comp = new Compiler[1];
    final boolean[] halting = new boolean[1];

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        current[0] = t.getCurrentNode();
        comp[0] = t.getCompiler();
        halting[0] = t.hasHaltingErrors();
      }
    });

    t.traverse(root);
    assertEquals(root, current[0]);
    assertEquals(compiler, comp[0]);
    assertFalse(halting[0]);
  }

  // Tests catch block scope creation and traversal
  @Test
  public void testTraverse_catchBlock_createsScope() {
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    Node tryNode = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK, catchNode));

    final List<Integer> scopeDepths = new ArrayList<Integer>();
    NodeTraversal.ScopedCallback cb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        scopeDepths.add(t.getScopeDepth());
      }

      @Override
      public void exitScope(NodeTraversal t) {}
    };

    NodeTraversal.traverse(compiler, tryNode, cb);
    assertTrue(scopeDepths.contains(1));
    assertTrue(scopeDepths.contains(2));
  }

  // Tests getLineNumber and getSourceName with missing info returns defaults
  @Test
  public void testGetLineNumberAndSourceName_missingInfo_returnsDefaults() {
    Node root = new Node(Token.BLOCK);
    final int[] lineno = new int[1];
    final String[] sourceName = new String[1];

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        lineno[0] = t.getLineNumber();
        sourceName[0] = t.getSourceName();
      }
    });

    assertEquals(0, lineno[0]);
    assertNull(sourceName[0]);
  }

  // Tests traverseRoots with List overload
  @Test
  public void testTraverseRoots_listOverload_traversesAll() {
    Node parent = new Node(Token.BLOCK);
    Node c1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node c2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    parent.addChildToBack(c1);
    parent.addChildToBack(c2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(c1, c2), cb);
    assertTrue(visited.contains(c1));
    assertTrue(visited.contains(c2));
  }
}
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

  // Tests basic traversal on a simple AST with post order visiting
  @Test
  public void testTraverse_simpleTree_visitsAllNodes() {
    Node root = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node number = Node.newNumber(42);
    exprResult.addChildToBack(number);
    root.addChildToBack(exprResult);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal.traverse(compiler, root, cb);

    assertEquals(3, visited.size());
    assertEquals(number, visited.get(0));
    assertEquals(exprResult, visited.get(1));
    assertEquals(root, visited.get(2));
  }

  // Tests function declaration traversal and scope tracking
  @Test
  public void testTraverse_functionDeclaration_tracksScopeAndVisitsBody() {
    Node script = new Node(Token.SCRIPT);
    Node nameNode = Node.newString(Token.NAME, "foo");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN);
    body.addChildToBack(returnNode);

    Node fnNode = new Node(Token.FUNCTION, nameNode, params, body);
    script.addChildToBack(fnNode);

    final List<String> events = new ArrayList<String>();
    NodeTraversal.ScopedCallback cb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        events.add("visit:" + n.getType());
      }

      @Override
      public void enterScope(NodeTraversal t) {
        events.add("enterScope:" + t.getScopeDepth());
      }

      @Override
      public void exitScope(NodeTraversal t) {
        events.add("exitScope:" + t.getScopeDepth());
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(script);

    assertTrue(events.contains("enterScope:1"));
    assertTrue(events.contains("enterScope:2"));
    assertTrue(events.contains("exitScope:2"));
    assertTrue(events.contains("exitScope:1"));
    assertFalse(t.hasScope());
  }

  // Tests function expression traversal where function name is visited within function scope
  @Test
  public void testTraverse_functionExpression_visitsNameInFunctionScope() {
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR);
    Node varName = Node.newString(Token.NAME, "f");
    Node fnName = Node.newString(Token.NAME, "fnExpr");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);

    Node fnNode = new Node(Token.FUNCTION, fnName, params, body);
    varName.addChildToBack(fnNode);
    varNode.addChildToBack(varName);
    script.addChildToBack(varNode);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    assertTrue(visited.contains(fnName));
    assertTrue(visited.contains(fnNode));
  }

  // Tests traverseFunction with malformed function body (not a block) throws internal error
  @Test(expected = RuntimeException.class)
  public void testTraverseFunction_invalidBody_throwsException() {
    Node script = new Node(Token.SCRIPT);
    Node nameNode = Node.newString(Token.NAME, "invalidFn");
    Node params = new Node(Token.PARAM_LIST);
    Node notABlock = Node.newNumber(123);

    Node fnNode = new Node(Token.FUNCTION, nameNode, params, notABlock);
    script.addChildToBack(fnNode);

    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
  }

  // Tests traverseRoots with multiple roots having common parent
  @Test
  public void testTraverseRoots_multipleRoots_visitsAllRoots() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node child2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    parent.addChildToBack(child1);
    parent.addChildToBack(child2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(child1, child2),
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node p) {
            visited.add(n);
          }
        });

    assertTrue(visited.contains(child1));
    assertTrue(visited.contains(child2));
  }

  // Tests traverseRoots with empty list does nothing
  @Test
  public void testTraverseRoots_emptyRoots_noOp() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverseRoots(compiler, Collections.<Node>emptyList(),
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node p) {
            visited.add(n);
          }
        });
    assertTrue(visited.isEmpty());
  }

  // Tests AbstractShallowCallback prunes function body traversal
  @Test
  public void testAbstractShallowCallback_functionBody_doesNotTraverse() {
    Node script = new Node(Token.SCRIPT);
    Node nameNode = Node.newString(Token.NAME, "foo");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN);
    body.addChildToBack(returnNode);

    Node fnNode = new Node(Token.FUNCTION, nameNode, params, body);
    script.addChildToBack(fnNode);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractShallowCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    assertTrue(visited.contains(nameNode));
    assertFalse(visited.contains(returnNode));
  }

  // Tests AbstractShallowStatementCallback filters expressions outside control structures
  @Test
  public void testAbstractShallowStatementCallback_filtersNonStatements() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    block.addChildToBack(expr);
    script.addChildToBack(block);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractShallowStatementCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    assertTrue(visited.contains(block));
  }

  // Tests AbstractNodeTypePruningCallback with include = true
  @Test
  public void testAbstractNodeTypePruningCallback_includeNodes() {
    Node script = new Node(Token.SCRIPT);
    Node numberNode = Node.newNumber(10);
    Node stringNode = Node.newString("test");
    script.addChildToBack(numberNode);
    script.addChildToBack(stringNode);

    Set<Integer> types = ImmutableSet.of(Token.SCRIPT, Token.NUMBER);
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractNodeTypePruningCallback(types, true) {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    assertTrue(visited.contains(numberNode));
    assertFalse(visited.contains(stringNode));
  }

  // Tests AbstractNodeTypePruningCallback with include = false
  @Test
  public void testAbstractNodeTypePruningCallback_excludeNodes() {
    Node script = new Node(Token.SCRIPT);
    Node numberNode = Node.newNumber(10);
    Node stringNode = Node.newString("test");
    script.addChildToBack(numberNode);
    script.addChildToBack(stringNode);

    Set<Integer> types = ImmutableSet.of(Token.NUMBER);
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractNodeTypePruningCallback(types, false) {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    assertFalse(visited.contains(numberNode));
    assertTrue(visited.contains(stringNode));
  }

  // Tests getEnclosingFunction, inGlobalScope, and getControlFlowGraph inside function scope
  @Test
  public void testGetEnclosingFunctionAndCfg_insideFunction_returnsCorrectValues() {
    Node script = new Node(Token.SCRIPT);
    final Node fnNode = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "myFn"),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));
    script.addChildToBack(fnNode);

    final boolean[] checked = new boolean[1];
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractScopedCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isBlock() && parent == fnNode) {
          assertEquals(fnNode, t.getEnclosingFunction());
          assertFalse(t.inGlobalScope());
          assertNotNull(t.getControlFlowGraph());
          checked[0] = true;
        }
      }
    });

    assertTrue(checked[0]);
  }

  // Tests traverseInnerNode with null and refined scope
  @Test
  public void testTraverseInnerNode_withRefinedScope_executesCorrectly() {
    final Node script = new Node(Token.SCRIPT);
    final Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(5));
    script.addChildToBack(expr);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    t.traverse(script);
    assertTrue(visited.contains(expr));
    t.traverseInnerNode(expr, script, null);
    assertEquals(script, expr.getParent());
  }

  // Tests line number, source name, and getCurrentNode retrieval
  @Test
  public void testGetLineNumberAndSourceName_returnsExpectedValues() {
    Node script = new Node(Token.SCRIPT);
    script.setLineno(15);
    script.setSourceFileName("test.js");
    script.setInputId(new InputId("test.js"));

    final int[] recordedLine = new int[1];
    final String[] recordedSource = new String[1];
    final Node[] recordedNode = new Node[1];

    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        recordedLine[0] = t.getLineNumber();
        recordedSource[0] = t.getSourceName();
        recordedNode[0] = t.getCurrentNode();
      }
    });

    assertEquals(15, recordedLine[0]);
    assertEquals("test.js", recordedSource[0]);
    assertEquals(script, recordedNode[0]);
  }

  // Tests makeError and report methods during traversal
  @Test
  public void testMakeErrorAndReport_createsValidJSError() {
    Node script = new Node(Token.SCRIPT);
    script.setSourceFileName("input.js");
    script.setLineno(1);

    final JSError[] generatedError = new JSError[1];
    NodeTraversal.traverse(compiler, script, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        generatedError[0] = t.makeError(n, NodeTraversal.NODE_TRAVERSAL_ERROR, "test error");
        t.report(n, NodeTraversal.NODE_TRAVERSAL_ERROR, "reported error");
      }
    });

    assertNotNull(generatedError[0]);
    assertEquals(NodeTraversal.NODE_TRAVERSAL_ERROR, generatedError[0].getType());
    assertEquals(1, compiler.getErrorCount());
  }
}
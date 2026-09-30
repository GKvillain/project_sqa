package com.google.javascript.rhino;

import org.junit.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.Assert.*;

public class NodeTest {

  // Tests string and number node creation and their value getters
  @Test
  public void testNewStringAndNumber_validValues_returnsCorrectValues() {
    Node strNode = Node.newString("foo");
    Node numNode = Node.newNumber(42.5);

    assertEquals(Token.STRING, strNode.getType());
    assertEquals("foo", strNode.getString());
    assertTrue(strNode.isString());

    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(42.5, numNode.getDouble(), 0.0);
    assertTrue(numNode.isNumber());
  }

  // Tests exception when creating StringNode with null
  @Test(expected = IllegalArgumentException.class)
  public void testNewString_nullValue_throwsException() {
    Node.newString(null);
  }

  // Tests exception when calling getString on non-string node
  @Test(expected = UnsupportedOperationException.class)
  public void testGetString_onNonStringNode_throwsException() {
    Node node = new Node(Token.BLOCK);
    node.getString();
  }

  // Tests exception when calling getDouble on non-number node
  @Test(expected = UnsupportedOperationException.class)
  public void testGetDouble_onNonNumberNode_throwsException() {
    Node node = new Node(Token.BLOCK);
    node.getDouble();
  }

  // Tests child manipulation: addChildToFront, addChildToBack, and child count
  @Test
  public void testAddChild_frontAndBack_maintainsCorrectOrder() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.NAME);
    Node child2 = new Node(Token.NAME);
    Node child3 = new Node(Token.NAME);

    parent.addChildToBack(child2);
    parent.addChildToFront(child1);
    parent.addChildToBack(child3);

    assertEquals(3, parent.getChildCount());
    assertTrue(parent.hasMoreThanOneChild());
    assertFalse(parent.hasOneChild());
    assertSame(child1, parent.getFirstChild());
    assertSame(child3, parent.getLastChild());
    assertSame(child2, parent.getChildAtIndex(1));
    assertEquals(1, parent.getIndexOfChild(child2));
  }

  // Tests addChildBefore and addChildAfter
  @Test
  public void testAddChildBeforeAndAfter_validChildren_insertsCorrectly() {
    Node parent = new Node(Token.BLOCK);
    Node mid = new Node(Token.NAME);
    parent.addChildToBack(mid);

    Node before = new Node(Token.NAME);
    parent.addChildBefore(before, mid);

    Node after = new Node(Token.NAME);
    parent.addChildAfter(after, mid);

    assertSame(before, parent.getFirstChild());
    assertSame(mid, before.getNext());
    assertSame(after, mid.getNext());
    assertSame(after, parent.getLastChild());
  }

  // Tests removeChild and removeFirstChild
  @Test
  public void testRemoveChild_existingChild_removesAndUnlinks() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.NAME);
    Node child2 = new Node(Token.NAME);
    parent.addChildToBack(child1);
    parent.addChildToBack(child2);

    parent.removeChild(child1);
    assertEquals(1, parent.getChildCount());
    assertSame(child2, parent.getFirstChild());
    assertNull(child1.getParent());
    assertNull(child1.getNext());

    Node removed = parent.removeFirstChild();
    assertSame(child2, removed);
    assertEquals(0, parent.getChildCount());
    assertFalse(parent.hasChildren());
  }

  // Tests replaceChild and replaceChildAfter
  @Test
  public void testReplaceChild_validNodes_replacesInTree() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.NAME);
    Node child2 = new Node(Token.NAME);
    parent.addChildToBack(child1);
    parent.addChildToBack(child2);

    Node replacement1 = new Node(Token.NUMBER);
    parent.replaceChild(child1, replacement1);
    assertSame(replacement1, parent.getFirstChild());
    assertNull(child1.getParent());

    Node replacement2 = new Node(Token.NUMBER);
    parent.replaceChildAfter(replacement1, replacement2);
    assertSame(replacement2, parent.getLastChild());
    assertNull(child2.getParent());
  }

  // Tests detachFromParent and detachChildren
  @Test
  public void testDetachOperations_validSubtree_clearsReferences() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.NAME);
    Node child2 = new Node(Token.NAME);
    parent.addChildToBack(child1);
    parent.addChildToBack(child2);

    child1.detachFromParent();
    assertNull(child1.getParent());
    assertSame(child2, parent.getFirstChild());

    parent.detachChildren();
    assertFalse(parent.hasChildren());
    assertNull(child2.getParent());
    assertNull(child2.getNext());
  }

  // Tests getQualifiedName for simple name, getprop, and 'this'
  @Test
  public void testGetQualifiedName_variousNodes_returnsExpectedNames() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    assertEquals("foo", nameNode.getQualifiedName());
    assertTrue(nameNode.isQualifiedName());
    assertTrue(nameNode.isUnscopedQualifiedName());

    Node thisNode = new Node(Token.THIS);
    assertEquals("this", thisNode.getQualifiedName());
    assertTrue(thisNode.isQualifiedName());
    assertFalse(thisNode.isUnscopedQualifiedName());

    Node getPropNode = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    assertEquals("a.b", getPropNode.getQualifiedName());
    assertTrue(getPropNode.isQualifiedName());
    assertTrue(getPropNode.isUnscopedQualifiedName());

    Node nonQualNode = new Node(Token.BLOCK);
    assertNull(nonQualNode.getQualifiedName());
    assertFalse(nonQualNode.isQualifiedName());
  }

  // Tests cloneNode and cloneTree
  @Test
  public void testCloneTree_nodeWithChildren_clonesEntireSubtree() {
    Node root = new Node(Token.BLOCK);
    Node child = Node.newString(Token.NAME, "x");
    root.addChildToBack(child);

    Node shallowClone = root.cloneNode();
    assertFalse(shallowClone.hasChildren());
    assertNull(shallowClone.getParent());

    Node deepClone = root.cloneTree();
    assertTrue(deepClone.hasChildren());
    assertNotSame(child, deepClone.getFirstChild());
    assertEquals(Token.NAME, deepClone.getFirstChild().getType());
    assertEquals("x", deepClone.getFirstChild().getString());
    assertSame(deepClone, deepClone.getFirstChild().getParent());
  }

  // Tests isEquivalentTo and checkTreeEquals
  @Test
  public void testIsEquivalentTo_matchingAndDivergentTrees_evaluatesCorrectly() {
    Node tree1 = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"));
    Node tree2 = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"));
    Node tree3 = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"));

    assertTrue(tree1.isEquivalentTo(tree2));
    assertNull(tree1.checkTreeEquals(tree2));

    assertFalse(tree1.isEquivalentTo(tree3));
    assertNotNull(tree1.checkTreeEquals(tree3));
  }

  // Tests property storage: int, boolean, object, and removal
  @Test
  public void testProperties_putGetRemove_worksAsExpected() {
    Node node = new Node(Token.BLOCK);

    node.putIntProp(Node.CHANGE_TIME, 100);
    assertEquals(100, node.getIntProp(Node.CHANGE_TIME));
    assertEquals(100, node.getExistingIntProp(Node.CHANGE_TIME));

    node.putBooleanProp(Node.SYNTHETIC_BLOCK_PROP, true);
    assertTrue(node.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    node.putProp(Node.ORIGINALNAME_PROP, "original");
    assertEquals("original", node.getProp(Node.ORIGINALNAME_PROP));

    node.removeProp(Node.CHANGE_TIME);
    assertEquals(0, node.getIntProp(Node.CHANGE_TIME));
    assertNull(node.getProp(Node.CHANGE_TIME));
  }

  // Tests line and char position encoding and decoding
  @Test
  public void testSourcePosition_encodeAndDecode_preservesLineAndChar() {
    Node node = new Node(Token.BLOCK, 12, 34);
    assertEquals(12, node.getLineno());
    assertEquals(34, node.getCharno());

    node.setLineno(56);
    node.setCharno(78);
    assertEquals(56, node.getLineno());
    assertEquals(78, node.getCharno());
  }

  // Tests side effect flags and query methods
  @Test
  public void testSideEffectFlags_variousConfigurations_returnsExpectedFlags() {
    Node callNode = new Node(Token.CALL);
    Node.SideEffectFlags flags = new Node.SideEffectFlags();

    flags.clearAllFlags();
    callNode.setSideEffectFlags(flags);
    assertTrue(callNode.isNoSideEffectsCall());
    assertTrue(callNode.isLocalResultCall());
    assertFalse(callNode.mayMutateArguments());
    assertFalse(callNode.mayMutateGlobalStateOrThrow());

    flags.setAllFlags();
    callNode.setSideEffectFlags(flags);
    assertFalse(callNode.isNoSideEffectsCall());
    assertTrue(callNode.mayMutateArguments());
    assertTrue(callNode.mayMutateGlobalStateOrThrow());
  }

  // Tests exception when setting side effect flags on non-CALL/NEW node
  @Test(expected = IllegalArgumentException.class)
  public void testSetSideEffectFlags_onInvalidNodeType_throwsException() {
    Node node = new Node(Token.BLOCK);
    node.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
  }

  // Tests children(), siblings(), and ancestors() iterators
  @Test
  public void testIterables_childrenSiblingsAncestors_iteratesCorrectly() {
    Node root = new Node(Token.BLOCK);
    Node c1 = new Node(Token.NAME);
    Node c2 = new Node(Token.NAME);
    root.addChildToBack(c1);
    root.addChildToBack(c2);

    int childCount = 0;
    for (Node child : root.children()) {
      assertNotNull(child);
      childCount++;
    }
    assertEquals(2, childCount);

    int sibCount = 0;
    for (Node sib : c1.siblings()) {
      assertNotNull(sib);
      sibCount++;
    }
    assertEquals(2, sibCount);

    Iterator<Node> ancestorIter = c1.getAncestors().iterator();
    assertTrue(ancestorIter.hasNext());
    assertSame(root, ancestorIter.next());
    assertFalse(ancestorIter.hasNext());
  }

  // Tests exception when iterator next is called past end
  @Test(expected = NoSuchElementException.class)
  public void testSiblingIterator_pastEnd_throwsException() {
    Node node = new Node(Token.BLOCK);
    Iterator<Node> it = node.siblings().iterator();
    assertTrue(it.hasNext());
    it.next();
    it.next();
  }

  // Tests AST token type check predicates
  @Test
  public void testTypePredicates_variousTokens_returnTrueOnlyForMatchingTypes() {
    assertTrue(new Node(Token.ADD).isAdd());
    assertTrue(new Node(Token.ASSIGN).isAssign());
    assertTrue(new Node(Token.CALL).isCall());
    assertTrue(new Node(Token.FUNCTION).isFunction());
    assertTrue(new Node(Token.IF).isIf());
    assertTrue(new Node(Token.VAR).isVar());
    assertTrue(new Node(Token.RETURN).isReturn());
    assertTrue(new Node(Token.OBJECTLIT).isObjectLit());
    assertTrue(new Node(Token.ARRAYLIT).isArrayLit());

    assertFalse(new Node(Token.ADD).isAssign());
  }
}
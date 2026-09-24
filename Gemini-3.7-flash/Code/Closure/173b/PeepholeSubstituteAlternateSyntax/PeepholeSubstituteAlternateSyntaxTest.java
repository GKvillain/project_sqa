package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeSubstituteAlternateSyntaxTest {

  private PeepholeSubstituteAlternateSyntax peepholeLate;
  private PeepholeSubstituteAlternateSyntax peepholeEarly;

  @Before
  public void setUp() {
    peepholeLate = new PeepholeSubstituteAlternateSyntax(true);
    peepholeEarly = new PeepholeSubstituteAlternateSyntax(false);
  }

  // Helper to wrap node under a parent for optimizations that need parent replacement
  private Node wrapInParent(Node child) {
    Node parent = IR.exprResult(child);
    return child;
  }

  // Helper to wrap statement in a block
  private Node wrapInBlock(Node exprResult) {
    Node block = IR.block(exprResult);
    return exprResult;
  }

  // Tests reduceTrueFalse with late = true
  @Test
  public void testOptimizeSubtree_trueNodeLate_reducesToNotZero() {
    Node trueNode = IR.trueNode();
    wrapInParent(trueNode);
    Node result = peepholeLate.optimizeSubtree(trueNode);
    assertEquals(Token.NOT, result.getType());
    assertEquals(0.0, result.getFirstChild().getDouble(), 0.0);
  }

  // Tests reduceTrueFalse with late = true
  @Test
  public void testOptimizeSubtree_falseNodeLate_reducesToNotOne() {
    Node falseNode = IR.falseNode();
    wrapInParent(falseNode);
    Node result = peepholeLate.optimizeSubtree(falseNode);
    assertEquals(Token.NOT, result.getType());
    assertEquals(1.0, result.getFirstChild().getDouble(), 0.0);
  }

  // Tests reduceTrueFalse with late = false (should remain unchanged)
  @Test
  public void testOptimizeSubtree_trueNodeEarly_remainsUnchanged() {
    Node trueNode = IR.trueNode();
    wrapInParent(trueNode);
    Node result = peepholeEarly.optimizeSubtree(trueNode);
    assertEquals(Token.TRUE, result.getType());
  }

  // Tests tryReduceReturn when operand is undefined name
  @Test
  public void testOptimizeSubtree_returnUndefined_removesChild() {
    Node returnNode = IR.returnNode(IR.name("undefined"));
    Node result = peepholeEarly.optimizeSubtree(returnNode);
    assertNull(result.getFirstChild());
  }

  // Tests tryReduceReturn when operand is void 0 without side effects
  @Test
  public void testOptimizeSubtree_returnVoidZero_removesChild() {
    Node voidNode = IR.voidNode(IR.number(0));
    Node returnNode = IR.returnNode(voidNode);
    Node result = peepholeEarly.optimizeSubtree(returnNode);
    assertNull(result.getFirstChild());
  }

  // Tests tryReduceReturn when return has regular value
  @Test
  public void testOptimizeSubtree_returnNumber_keepsChild() {
    Node returnNode = IR.returnNode(IR.number(42));
    Node result = peepholeEarly.optimizeSubtree(returnNode);
    assertNotNull(result.getFirstChild());
    assertEquals(42.0, result.getFirstChild().getDouble(), 0.0);
  }

  // Tests trySplitComma with late = false inside exprResult
  @Test
  public void testOptimizeSubtree_commaEarlyInsideExprResult_splitsComma() {
    Node left = IR.number(1);
    Node right = IR.number(2);
    Node comma = IR.comma(left, right);
    Node exprResult = IR.exprResult(comma);
    wrapInBlock(exprResult);

    Node result = peepholeEarly.optimizeSubtree(comma);
    assertEquals(left, result);
    assertEquals(left, exprResult.getFirstChild());
    assertNotNull(exprResult.getNext());
    assertEquals(right, exprResult.getNext().getFirstChild());
  }

  // Tests trySplitComma with late = true
  @Test
  public void testOptimizeSubtree_commaLate_doesNotSplit() {
    Node left = IR.number(1);
    Node right = IR.number(2);
    Node comma = IR.comma(left, right);
    Node exprResult = IR.exprResult(comma);
    wrapInBlock(exprResult);

    Node result = peepholeLate.optimizeSubtree(comma);
    assertEquals(comma, result);
  }

  // Tests tryFoldSimpleFunctionCall with String("hello")
  @Test
  public void testOptimizeSubtree_callStringWithStringLiteral_foldsToAddition() {
    Node call = IR.call(IR.name("String"), IR.string("hello"));
    wrapInParent(call);

    Node result = peepholeEarly.optimizeSubtree(call);
    assertEquals(Token.ADD, result.getType());
    assertEquals("", result.getFirstChild().getString());
    assertEquals("hello", result.getLastChild().getString());
  }

  // Tests tryFoldSimpleFunctionCall with non-immutable value
  @Test
  public void testOptimizeSubtree_callStringWithVariable_remainsUnchanged() {
    Node call = IR.call(IR.name("String"), IR.name("x"));
    wrapInParent(call);

    Node result = peepholeEarly.optimizeSubtree(call);
    assertEquals(Token.CALL, result.getType());
  }

  // Tests tryMinimizeStringArrayLiteral with late = true and multiple short strings
  @Test
  public void testOptimizeSubtree_arrayLiteralOfStringsLate_foldsToSplit() {
    Node arrayLit = IR.arraylit(
        IR.string("a"),
        IR.string("b"),
        IR.string("c"),
        IR.string("d"),
        IR.string("e"),
        IR.string("f"),
        IR.string("g"),
        IR.string("h")
    );
    wrapInParent(arrayLit);

    Node result = peepholeLate.optimizeSubtree(arrayLit);
    assertEquals(Token.CALL, result.getType());
    Node getprop = result.getFirstChild();
    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals("split", getprop.getLastChild().getString());
  }

  // Tests tryMinimizeArrayLiteral with non-string elements
  @Test
  public void testOptimizeSubtree_arrayLiteralWithNumbers_remainsUnchanged() {
    Node arrayLit = IR.arraylit(IR.number(1), IR.number(2));
    wrapInParent(arrayLit);

    Node result = peepholeLate.optimizeSubtree(arrayLit);
    assertEquals(Token.ARRAYLIT, result.getType());
  }

  // Tests containsUnicodeEscape utility with and without unicode escapes
  @Test
  public void testContainsUnicodeEscape_escapedUnicode_returnsTrue() {
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\u0000"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("abc"));
  }

  // Tests default case with unsupported node type
  @Test
  public void testOptimizeSubtree_unhandledNodeType_returnsSameNode() {
    Node varNode = IR.var(IR.name("x"));
    Node result = peepholeEarly.optimizeSubtree(varNode);
    assertSame(varNode, result);
  }
}
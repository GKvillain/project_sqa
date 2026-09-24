package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class InlineCostEstimatorTest {

  private static int check(String js) {
    return check(js, Integer.MAX_VALUE);
  }

  private static int check(String js, int costThreshold) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return InlineCostEstimator.getCost(root, costThreshold);
  }

  // Tests cost of simple number literal
  @Test
  public void testGetCost_numberLiteral_returnsCorrectCost() {
    assertEquals(1, check("1"));
    assertEquals(2, check("10"));
  }

  // Tests cost of single identifier which uses estimated 2-char identifier length
  @Test
  public void testGetCost_identifier_returnsEstimatedIdentifierCost() {
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, check("a"));
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, check("veryLongVariableName"));
  }

  // Tests cost of binary expression with identifiers
  @Test
  public void testGetCost_binaryExpression_returnsEstimatedCost() {
    // "ab+ab" -> 2 + 1 + 2 = 5
    assertEquals(5, check("a + b"));
  }

  // Tests cost of assignment expression
  @Test
  public void testGetCost_assignment_returnsEstimatedCost() {
    // "ab=1" -> 2 + 1 + 1 = 4
    assertEquals(4, check("x = 1"));
  }

  // Tests cost of function call
  @Test
  public void testGetCost_functionCall_returnsEstimatedCost() {
    // "ab()" -> 2 + 1 + 1 = 4
    assertEquals(4, check("foo()"));
  }

  // Tests cost of function expression
  @Test
  public void testGetCost_functionExpression_returnsEstimatedCost() {
    // "function(){}" -> 12
    assertEquals(12, check("function(){}"));
  }

  // Tests cost of string literal
  @Test
  public void testGetCost_stringLiteral_returnsQuotedStringCost() {
    // "\"hello\"" -> 7
    assertEquals(7, check("\"hello\""));
  }

  // Tests cost of array literal
  @Test
  public void testGetCost_arrayLiteral_returnsEstimatedCost() {
    // "[1,2]" -> 5
    assertEquals(5, check("[1, 2]"));
  }

  // Tests cost of object literal
  @Test
  public void testGetCost_objectLiteral_returnsEstimatedCost() {
    // "({ab:1})" -> 8
    assertEquals(8, check("({a: 1})"));
  }

  // Tests cost of var declaration
  @Test
  public void testGetCost_varDeclaration_returnsEstimatedCost() {
    // "var ab=1" -> 3 + 1 + 2 + 1 + 1 = 8
    assertEquals(8, check("var x = 1;"));
  }

  // Tests threshold bounding where processing stops early when maxCost is reached
  @Test
  public void testGetCost_thresholdStopsEarly_returnsCostAtOrAboveThreshold() {
    int normalCost = check("1 + 2 + 3 + 4 + 5");
    int thresholdCost = check("1 + 2 + 3 + 4 + 5", 3);
    assertTrue(thresholdCost >= 3);
    assertTrue(thresholdCost <= normalCost);
  }

  // Tests threshold equal to zero stops processing immediately
  @Test
  public void testGetCost_zeroThreshold_stopsImmediately() {
    int cost = check("x = 1 + 2", 0);
    assertTrue(cost >= 0);
    assertTrue(cost < check("x = 1 + 2"));
  }

  // Tests threshold larger than code cost computes full cost
  @Test
  public void testGetCost_highThreshold_returnsFullCost() {
    int defaultCost = check("a + b");
    int thresholdCost = check("a + b", 1000);
    assertEquals(defaultCost, thresholdCost);
  }

  // Tests cost of boolean literals
  @Test
  public void testGetCost_booleanLiteral_returnsEstimatedCost() {
    int costTrue = check("true");
    int costFalse = check("false");
    assertTrue(costTrue > 0);
    assertTrue(costFalse > 0);
  }

  // Tests cost of null literal
  @Test
  public void testGetCost_nullLiteral_returnsEstimatedCost() {
    int costNull = check("null");
    assertTrue(costNull > 0);
  }
}
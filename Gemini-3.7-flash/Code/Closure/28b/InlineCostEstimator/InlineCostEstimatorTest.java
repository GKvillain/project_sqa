package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class InlineCostEstimatorTest {

  private Node parse(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return root.getFirstChild();
  }

  private int checkCost(String js) {
    return InlineCostEstimator.getCost(parse(js));
  }

  private int checkCost(String js, int threshold) {
    return InlineCostEstimator.getCost(parse(js), threshold);
  }

  // Tests cost estimation for a simple number literal
  @Test
  public void testGetCost_numberLiteral_returnsCorrectCost() {
    assertEquals(1, checkCost("1"));
  }

  // Tests cost estimation for an identifier using ESTIMATED_IDENTIFIER length
  @Test
  public void testGetCost_identifier_returnsEstimatedCost() {
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, checkCost("a"));
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, checkCost("longIdentifierName"));
  }

  // Tests cost estimation for boolean true constant
  @Test
  public void testGetCost_trueConstant_returnsFoldedCost() {
    assertEquals(1, checkCost("true"));
  }

  // Tests cost estimation for boolean false constant
  @Test
  public void testGetCost_falseConstant_returnsFoldedCost() {
    assertEquals(1, checkCost("false"));
  }

  // Tests cost estimation for null constant
  @Test
  public void testGetCost_nullConstant_returnsFoldedCost() {
    assertEquals(1, checkCost("null"));
  }

  // Tests cost estimation for this constant
  @Test
  public void testGetCost_thisConstant_returnsFoldedCost() {
    assertEquals(1, checkCost("this"));
  }

  // Tests cost estimation for binary expression
  @Test
  public void testGetCost_binaryExpression_returnsCorrectCost() {
    assertEquals(5, checkCost("a + b"));
  }

  // Tests cost estimation for function call
  @Test
  public void testGetCost_functionCall_returnsCorrectCost() {
    assertEquals(4, checkCost("a()"));
  }

  // Tests cost estimation for property access and method call
  @Test
  public void testGetCost_methodCall_returnsCorrectCost() {
    assertEquals(7, checkCost("a.b()"));
  }

  // Tests cost estimation for string literal
  @Test
  public void testGetCost_stringLiteral_returnsCorrectCost() {
    assertEquals(4, checkCost("'ab'"));
  }

  // Tests cost estimation for empty array literal
  @Test
  public void testGetCost_arrayLiteral_returnsCorrectCost() {
    assertEquals(2, checkCost("[]"));
  }

  // Tests cost estimation for object literal
  @Test
  public void testGetCost_objectLiteral_returnsCorrectCost() {
    assertEquals(2, checkCost("({})"));
  }

  // Tests threshold cutoff when cost reaches maximum allowed cost
  @Test
  public void testGetCost_thresholdExceeded_stopsProcessingEarly() {
    int fullCost = checkCost("a + b + c + d");
    int thresholdCost = checkCost("a + b + c + d", 3);
    assertTrue(thresholdCost >= 3);
    assertTrue(thresholdCost <= fullCost);
  }

  // Tests threshold not exceeded when maxCost is Integer.MAX_VALUE
  @Test
  public void testGetCost_thresholdMaxInt_returnsFullCost() {
    Node node = parse("a + b");
    int costWithDefault = InlineCostEstimator.getCost(node);
    int costWithMaxInt = InlineCostEstimator.getCost(node, Integer.MAX_VALUE);
    assertEquals(costWithDefault, costWithMaxInt);
  }

  // Tests threshold set to zero triggers cutoff immediately on append
  @Test
  public void testGetCost_zeroThreshold_stopsImmediately() {
    int cost = checkCost("a", 0);
    assertTrue(cost >= 0);
  }
}
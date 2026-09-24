package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeConsumerTest {

  private TestCodeConsumer consumer;

  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();

    @Override
    char getLastChar() {
      return buffer.length() > 0 ? buffer.charAt(buffer.length() - 1) : '\0';
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    String getCode() {
      return buffer.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
  }

  // Tests negative zero following a minus sign to prevent misparsing (Defects4J 38b)
  @Test
  public void testAddNumber_negativeZeroAfterMinus_addsSpace() {
    consumer.append("-");
    consumer.addNumber(-0.0);
    assertEquals("- -0.0", consumer.getCode());
  }

  // Tests negative number following a minus sign
  @Test
  public void testAddNumber_negativeNumberAfterMinus_addsSpace() {
    consumer.append("-");
    consumer.addNumber(-4.0);
    assertEquals("- -4", consumer.getCode());
  }

  // Tests positive integer representation in scientific notation
  @Test
  public void testAddNumber_largeInteger_usesScientificNotation() {
    consumer.addNumber(1000.0);
    assertEquals("1E3", consumer.getCode());
  }

  // Tests positive integer representation without exponent
  @Test
  public void testAddNumber_smallInteger_noScientificNotation() {
    consumer.addNumber(100.0);
    assertEquals("100", consumer.getCode());
  }

  // Tests floating point number
  @Test
  public void testAddNumber_decimalValue_printsStandard() {
    consumer.addNumber(1.25);
    assertEquals("1.25", consumer.getCode());
  }

  // Tests isNegativeZero helper
  @Test
  public void testIsNegativeZero_variousValues_identifiesCorrectly() {
    assertTrue(CodeConsumer.isNegativeZero(-0.0));
    assertFalse(CodeConsumer.isNegativeZero(0.0));
    assertFalse(CodeConsumer.isNegativeZero(-1.0));
    assertFalse(CodeConsumer.isNegativeZero(1.0));
  }

  // Tests isWordChar helper
  @Test
  public void testIsWordChar_variousChars_identifiesCorrectly() {
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('1'));
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));
    assertFalse(CodeConsumer.isWordChar('+'));
    assertFalse(CodeConsumer.isWordChar(' '));
  }

  // Tests add method when space separation is required between word characters
  @Test
  public void testAdd_wordCharsConsecutive_addsSpace() {
    consumer.add("return");
    consumer.add("foo");
    assertEquals("return foo", consumer.getCode());
  }

  // Tests add method when consecutive slashes need space separation
  @Test
  public void testAdd_slashAfterSlash_addsSpace() {
    consumer.add("/");
    consumer.add("/");
    assertEquals("/ /", consumer.getCode());
  }

  // Tests add method with empty string input
  @Test
  public void testAdd_emptyString_noChange() {
    consumer.add("");
    assertEquals("", consumer.getCode());
  }

  // Tests addOp when consecutive plus signs need space separation
  @Test
  public void testAddOp_plusAfterPlus_addsSpace() {
    consumer.add("x");
    consumer.addOp("+", true);
    consumer.addOp("++", false);
    consumer.add("y");
    assertEquals("x+ ++y", consumer.getCode());
  }

  // Tests addOp when consecutive minus signs need space separation
  @Test
  public void testAddOp_minusAfterMinus_addsSpace() {
    consumer.add("x");
    consumer.addOp("-", true);
    consumer.addOp("--", false);
    consumer.add("y");
    assertEquals("x- --y", consumer.getCode());
  }

  // Tests addOp when > follows - to prevent html comment close -->
  @Test
  public void testAddOp_greaterThanAfterMinus_addsSpace() {
    consumer.addOp("-", false);
    consumer.addOp(">", false);
    assertEquals("- >", consumer.getCode());
  }

  // Tests addOp with letter operator following word character
  @Test
  public void testAddOp_letterOpAfterWordChar_addsSpace() {
    consumer.add("x");
    consumer.addOp("instanceof", true);
    assertEquals("x instanceof", consumer.getCode());
  }

  // Tests block begin and end formatting
  @Test
  public void testBeginBlockAndEndBlock_createsBlock() {
    consumer.beginBlock();
    consumer.add("foo");
    consumer.endBlock();
    assertEquals("{foo}", consumer.getCode());
  }

  // Tests statement ending with semicolon
  @Test
  public void testEndStatement_needSemicolon_appendsSemicolon() {
    consumer.add("var x");
    consumer.endStatement(true);
    consumer.add("var y");
    assertEquals("var x;var y", consumer.getCode());
  }

  // Tests statement ending delayed semicolon before next statement
  @Test
  public void testMaybeEndStatement_delayedSemicolon_appendsSemicolon() {
    consumer.add("var x");
    consumer.endStatement();
    consumer.add("var y");
    assertEquals("var x;var y", consumer.getCode());
  }

  // Tests list separator
  @Test
  public void testListSeparator_appendsComma() {
    consumer.add("a");
    consumer.listSeparator();
    consumer.add("b");
    assertEquals("a,b", consumer.getCode());
  }

  // Tests case body
  @Test
  public void testBeginCaseBody_appendsColon() {
    consumer.add("case 1");
    consumer.beginCaseBody();
    assertEquals("case 1:", consumer.getCode());
  }
}
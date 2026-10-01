package org.jfree.chart.imagemap;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StandardToolTipTagFragmentGeneratorTest {

    // Tests normal valid input
    @Test
    public void testGenerateToolTipFragment_normalInput_returnsCorrectHtml() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("Hello");
        assertEquals(" title=\"Hello\" alt=\"\"", result);
    }

    // Tests empty string input (boundary case)
    @Test
    public void testGenerateToolTipFragment_emptyString_returnsEmptyTitle() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("");
        assertEquals(" title=\"\" alt=\"\"", result);
    }

    // Tests null input (edge case)
    @Test
    public void testGenerateToolTipFragment_nullInput_returnsTitleWithNull() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment(null);
        assertEquals(" title=\"null\" alt=\"\"", result);
    }

    // Tests input with special characters
    @Test
    public void testGenerateToolTipFragment_specialCharacters_returnsEscapedOrRaw() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("a<b>c");
        // No escaping in this implementation
        assertEquals(" title=\"a<b>c\" alt=\"\"", result);
    }

    // Tests input with quotes (boundary case, potential attribute break)
    @Test
    public void testGenerateToolTipFragment_quotesInInput_returnsRawQuotes() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("he\"llo");
        // No escaping, quotes are embedded directly
        assertEquals(" title=\"he\"llo\" alt=\"\"", result);
    }

    // Tests input with single quotes
    @Test
    public void testGenerateToolTipFragment_singleQuoteInInput_returnsRawSingleQuote() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("it's");
        assertEquals(" title=\"it's\" alt=\"\"", result);
    }

    // Tests input with ampersand (potential HTML special character)
    @Test
    public void testGenerateToolTipFragment_ampersandInInput_returnsRaw() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("A&B");
        assertEquals(" title=\"A&B\" alt=\"\"", result);
    }

    // Tests input with multiple spaces
    @Test
    public void testGenerateToolTipFragment_multipleSpaces_returnsWithSpaces() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("  test  ");
        assertEquals(" title=\"  test  \" alt=\"\"", result);
    }

    // Tests input with newline character
    @Test
    public void testGenerateToolTipFragment_newlineInInput_returnsWithNewline() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String result = generator.generateToolTipFragment("line1\nline2");
        assertEquals(" title=\"line1\nline2\" alt=\"\"", result);
    }

    // Tests the constructor exists and does not throw exception
    @Test
    public void testConstructor_default_createsInstance() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        assertEquals(StandardToolTipTagFragmentGenerator.class, generator.getClass());
    }

}
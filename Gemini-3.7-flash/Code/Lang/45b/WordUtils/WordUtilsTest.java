package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class WordUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_isNotNull() {
        assertNotNull(new WordUtils());
    }

    // Tests wrap with null and empty string input
    @Test
    public void testWrap_nullOrEmptyInput_returnsInput() {
        assertNull(WordUtils.wrap(null, 20));
        assertNull(WordUtils.wrap(null, 20, "\n", true));
        assertEquals("", WordUtils.wrap("", 20));
        assertEquals("", WordUtils.wrap("", 20, "\n", true));
    }

    // Tests normal wrapping with default and custom newline
    @Test
    public void testWrap_normalCase_wrapsCorrectly() {
        String input = "Here is one line of text that is going to be wrapped";
        String expected = "Here is one\nline of text\nthat is going\nto be\nwrapped";
        assertEquals(expected, WordUtils.wrap(input, 15, "\n", false));
        assertNotNull(WordUtils.wrap(input, 15));
    }

    // Tests wrap with negative/zero wrapLength and long words wrapping
    @Test
    public void testWrap_longWordsAndMinWrapLength_wrapsProperly() {
        assertEquals("a\nb\nc", WordUtils.wrap("abc", 0, "\n", true));
        assertEquals("abcdef", WordUtils.wrap("abcdef", 3, "\n", false));
        assertEquals("abc\ndef", WordUtils.wrap("abcdef", 3, "\n", true));
        assertEquals("abc\ndef gh", WordUtils.wrap("abcdef gh", 3, "\n", false));
    }

    // Tests capitalize with default whitespace and custom delimiters
    @Test
    public void testCapitalize_normalAndDelimiters_capitalizesFirstLetter() {
        assertNull(WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("I Am FINE", WordUtils.capitalize("i am FINE"));
        assertEquals("i aM.fine", WordUtils.capitalize("i aM.fine", new char[0]));
        assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", new char[]{'.'}));
    }

    // Tests capitalizeFully with default whitespace and custom delimiters
    @Test
    public void testCapitalizeFully_normalAndDelimiters_capitalizesAndLowercasesRest() {
        assertNull(WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
        assertEquals("i aM.fine", WordUtils.capitalizeFully("i aM.fine", new char[0]));
        assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", new char[]{'.'}));
    }

    // Tests uncapitalize with default whitespace and custom delimiters
    @Test
    public void testUncapitalize_normalAndDelimiters_uncapitalizesFirstLetter() {
        assertNull(WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE"));
        assertEquals("I AM.FINE", WordUtils.uncapitalize("I AM.FINE", new char[0]));
        assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", new char[]{'.'}));
    }

    // Tests swapCase with various character cases
    @Test
    public void testSwapCase_variousCases_swapsCaseProperly() {
        assertNull(WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", WordUtils.swapCase("The dog has a BONE"));
        assertEquals("hELLO 123 wORLD", WordUtils.swapCase("Hello 123 World"));
    }

    // Tests initials with default whitespace and custom delimiters
    @Test
    public void testInitials_normalAndDelimiters_returnsInitials() {
        assertNull(WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("", WordUtils.initials("Ben John Lee", new char[0]));
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", new char[]{' ', '.'}));
    }

    // Tests abbreviate with null and empty string input
    @Test
    public void testAbbreviate_nullOrEmptyInput_returnsInput() {
        assertNull(WordUtils.abbreviate(null, 0, 10, "..."));
        assertEquals("", WordUtils.abbreviate("", 0, 10, "..."));
    }

    // Tests abbreviate when no abbreviation is needed
    @Test
    public void testAbbreviate_noAbbreviationNeeded_returnsOriginalString() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", -1, -1, "..."));
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 0, 20, "..."));
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 0, 11, "..."));
    }

    // Tests abbreviate when space is found between lower and upper
    @Test
    public void testAbbreviate_spaceWithinRange_abbreviatesAtSpace() {
        assertEquals("Hello...", WordUtils.abbreviate("Hello World", 0, 10, "..."));
        assertEquals("Hello", WordUtils.abbreviate("Hello World", 0, 10, null));
    }

    // Tests abbreviate when no space is found or space is beyond upper limit
    @Test
    public void testAbbreviate_noSpaceWithinRange_abbreviatesAtUpper() {
        assertEquals("Hello...", WordUtils.abbreviate("HelloWorldLongString", 0, 5, "..."));
        assertEquals("Hello Wor...", WordUtils.abbreviate("Hello World Again", 8, 9, "..."));
    }

    // Tests abbreviate boundary conditions where lower > length or upper < lower (Defects4J Lang-45)
    @Test
    public void testAbbreviate_lowerGreaterThanLength_handlesOutOfBounds() {
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 20, null));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, -1, null));
        assertEquals("01234...", WordUtils.abbreviate("0123456789", 5, 2, "..."));
    }
}
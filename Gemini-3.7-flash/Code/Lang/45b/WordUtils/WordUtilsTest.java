package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link WordUtils}.
 */
public class WordUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_instanceCreated() {
        assertNotNull(new WordUtils());
    }

    // Tests wrap with null input
    @Test
    public void testWrap_nullInput_returnsNull() {
        assertNull(WordUtils.wrap(null, 20));
        assertNull(WordUtils.wrap(null, 20, "\n", true));
    }

    // Tests wrap with empty string
    @Test
    public void testWrap_emptyString_returnsEmpty() {
        assertEquals("", WordUtils.wrap("", 20));
        assertEquals("", WordUtils.wrap("", 20, "\n", false));
    }

    // Tests wrap normal line length
    @Test
    public void testWrap_normalText_wrapsCorrectly() {
        String input = "Here is one line of text that is going to be wrapped but not too short.";
        String expected = "Here is one line of\ntext that is going to\nbe wrapped but not\ntoo short.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
    }

    // Tests wrap with long words and wrapLongWords false/true
    @Test
    public void testWrap_longWord_wrapsAccordingToFlag() {
        String input = "Click here http://commons.apache.org/lang to download";
        String expectedNoWrap = "Click here\nhttp://commons.apache.org/lang\nto download";
        assertEquals(expectedNoWrap, WordUtils.wrap(input, 15, "\n", false));

        String expectedWrap = "Click here\nhttp://commons.\napache.org/lang\nto download";
        assertEquals(expectedWrap, WordUtils.wrap(input, 15, "\n", true));
    }

    // Tests wrap with wrapLength less than 1
    @Test
    public void testWrap_wrapLengthLessThanOne_treatedAsOne() {
        assertEquals("a\nb", WordUtils.wrap("a b", 0, "\n", false));
    }

    // Tests wrap with null newLineStr (uses default system line separator)
    @Test
    public void testWrap_nullNewLineStr_usesSystemSeparator() {
        assertEquals("here is" + SystemUtils.LINE_SEPARATOR + "a line", WordUtils.wrap("here is a line", 7, null, false));
    }

    // Tests wrap overload with default newline and wrapLongWords
    @Test
    public void testWrap_overloadWithWrapLengthOnly_usesDefaults() {
        assertEquals("here is" + SystemUtils.LINE_SEPARATOR + "a line", WordUtils.wrap("here is a line", 7));
    }

    // Tests capitalize with default whitespace and null/empty
    @Test
    public void testCapitalize_defaultDelimiters_capitalizesWords() {
        assertNull(WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("I Am Fine", WordUtils.capitalize("i am fine"));
        assertEquals("I Am FINE", WordUtils.capitalize("i am FINE"));
    }

    // Tests capitalize with custom delimiter array
    @Test
    public void testCapitalize_customDelimiters_capitalizesWords() {
        char[] delimiters = new char[]{'.', '-'};
        assertEquals("I aM.Fine-Day", WordUtils.capitalize("i aM.fine-day", delimiters));
        assertEquals("i aM.fine-day", WordUtils.capitalize("i aM.fine-day", new char[0]));
        assertEquals("I Am Fine", WordUtils.capitalize("i am fine", (char[]) null));
    }

    // Tests capitalizeFully with default and custom delimiters
    @Test
    public void testCapitalizeFully_variousInputs_capitalizesAndLowersRest() {
        assertNull(WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
        assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", new char[]{'.'}));
        assertEquals("i am.fine", WordUtils.capitalizeFully("i aM.fine", new char[0]));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i aM.fine", (char[]) null));
    }

    // Tests uncapitalize with default and custom delimiters
    @Test
    public void testUncapitalize_variousInputs_uncapitalizesFirstLetter() {
        assertNull(WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE"));
        assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", new char[]{'.'}));
        assertEquals("I AM.FINE", WordUtils.uncapitalize("I AM.FINE", new char[0]));
        assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE", (char[]) null));
    }

    // Tests swapCase with mixed case inputs
    @Test
    public void testSwapCase_mixedInputs_swapsCaseProperly() {
        assertNull(WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", WordUtils.swapCase("The dog has a BONE"));
        assertEquals("hELLO wORLD", WordUtils.swapCase("Hello World"));
        assertEquals("1234.!?abcABC", WordUtils.swapCase("1234.!?ABCabc"));
    }

    // Tests initials with default whitespace
    @Test
    public void testInitials_defaultDelimiters_extractsInitials() {
        assertNull(WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
    }

    // Tests initials with custom delimiters and empty delimiters
    @Test
    public void testInitials_customDelimiters_extractsInitials() {
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", new char[]{' ', '.'}));
        assertEquals("", WordUtils.initials("Ben John Lee", new char[0]));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", (char[]) null));
    }

    // Tests abbreviate with null and empty string
    @Test
    public void testAbbreviate_nullAndEmpty_returnsCorrectValue() {
        assertNull(WordUtils.abbreviate(null, 1, -1, ""));
        assertEquals("", WordUtils.abbreviate("", 0, 10, ""));
    }

    // Tests abbreviate normal cases
    @Test
    public void testAbbreviate_normalCase_abbreviatesAtSpace() {
        assertEquals("01234 6", WordUtils.abbreviate("01234 6789", 0, 10, null));
        assertEquals("01234", WordUtils.abbreviate("01234 6789", 0, 5, null));
        assertEquals("01234...", WordUtils.abbreviate("01234 6789", 5, 10, "..."));
        assertEquals("012", WordUtils.abbreviate("012 456", 2, 2, null));
    }

    // Tests abbreviate upper limit greater than length and no space
    @Test
    public void testAbbreviate_noSpaceAndUpperNoLimit_returnsSubstrings() {
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 0, -1, null));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 0, 5, null));
        assertEquals("01234...", WordUtils.abbreviate("0123456789", 0, 5, "..."));
    }

    // Tests abbreviate when lower limit exceeds string length (Lang-45 defect)
    @Test
    public void testAbbreviate_lowerGreaterThanLength_doesNotThrowIndexOutOfBoundsException() {
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, -1, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 10, ""));
    }

    // Tests abbreviate when upper limit is lower than lower limit
    @Test
    public void testAbbreviate_upperLessThanLower_adjustsUpperToLower() {
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, 2, null));
    }

    // Tests abbreviate when lower is negative
    @Test
    public void testAbbreviate_negativeLower_adjustedToZero() {
        assertEquals("01234", WordUtils.abbreviate("01234 56789", -5, 5, null));
    }
}
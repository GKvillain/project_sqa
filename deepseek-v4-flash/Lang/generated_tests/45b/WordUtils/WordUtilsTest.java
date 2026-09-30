package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class WordUtilsTest {

    // ----- capitalize tests -----

    @Test
    public void testCapitalize_nullInput_returnsNull() {
        assertNull(WordUtils.capitalize(null));
    }

    @Test
    public void testCapitalize_basicString_capitalizesWords() {
        assertEquals("I Am FINE", WordUtils.capitalize("i am FINE"));
    }

    @Test
    public void testCapitalize_withDelimiter_capitalizesCorrectly() {
        char[] delimiters = {'.'};
        assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", delimiters));
    }

    // ----- capitalizeFully tests -----

    @Test
    public void testCapitalizeFully_basicString_capitalizesFully() {
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
    }

    // ----- uncapitalize tests -----

    @Test
    public void testUncapitalize_basicString_uncapitalizesWords() {
        assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE"));
    }

    // ----- swapCase tests -----

    @Test
    public void testSwapCase_basicString_swapsCase() {
        assertEquals("tHE DOG HAS A bone", WordUtils.swapCase("The dog has a BONE"));
    }

    // ----- initials tests -----

    @Test
    public void testInitials_basicString_returnsInitials() {
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
    }

    @Test
    public void testInitials_withDelimiter_returnsInitials() {
        char[] delimiters = {' ', '.'};
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", delimiters));
    }

    // ----- wrap tests -----

    @Test
    public void testWrap_nullInput_returnsNull() {
        assertNull(WordUtils.wrap(null, 10));
    }

    @Test
    public void testWrap_shortLine_wrapsAtLength() {
        String result = WordUtils.wrap("hello world", 5);
        String lineSep = System.getProperty("line.separator");
        assertEquals("hello" + lineSep + "world", result);
    }

    @Test
    public void testWrap_longWordNoWrap_returnsSameString() {
        // word longer than wrapLength with default wrapLongWords=false
        assertEquals("abcdefghijk", WordUtils.wrap("abcdefghijk", 5));
    }

    @Test
    public void testWrap_longWordWrap_wrapsWord() {
        // word longer than wrapLength with wrapLongWords=true
        String result = WordUtils.wrap("abcdefghijk", 5, "\n", true);
        assertEquals("abcde\nfghij\nk", result);
    }

    @Test
    public void testWrap_wrapLengthLessThanOne_adjustsToOne() {
        // wrapLength=0 becomes 1
        String result = WordUtils.wrap("a b", 0, "\n", false);
        assertEquals("a\nb", result);
    }

    // ----- abbreviate tests -----

    @Test
    public void testAbbreviate_nullInput_returnsNull() {
        assertNull(WordUtils.abbreviate(null, 0, 10, "..."));
    }

    @Test
    public void testAbbreviate_upperMinusOne_abbreviatesAtSpace() {
        // covers upper == -1 adjustment branch
        assertEquals("hello...", WordUtils.abbreviate("hello world", 0, -1, "..."));
    }

    @Test
    public void testAbbreviate_indexAfterUpper_abbreviatesAtUpper() {
        assertEquals("hell.", WordUtils.abbreviate("hello world", 0, 4, "."));
    }

    @Test
    public void testAbbreviate_noSpaceAndUpperEqualsLength_returnsFullString() {
        assertEquals("hello", WordUtils.abbreviate("hello", 0, 5, "..."));
    }

    @Test
    public void testAbbreviate_noSpaceAndUpperLessThanLength_appends() {
        assertEquals("hel...", WordUtils.abbreviate("hello", 0, 3, "..."));
    }

    @Test
    public void testAbbreviate_upperLessThanLower_adjustsUpperToLower() {
        // upper < lower triggers adjustment; also no space in the remainder
        assertEquals("hello ...", WordUtils.abbreviate("hello world", 6, 3, "..."));
    }

    @Test
    public void testAbbreviate_lowerExceedsLength_doesNotThrow() {
        // currently throws IndexOutOfBoundsException due to bug
        // this test verifies that no exception is thrown
        String result = WordUtils.abbreviate("hello", 10, 5, "...");
        assertNotNull(result);
    }
}
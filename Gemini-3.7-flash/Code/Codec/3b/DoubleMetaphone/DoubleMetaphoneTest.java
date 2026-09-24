package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleMetaphoneTest {

    private DoubleMetaphone doubleMetaphone;

    @Before
    public void setUp() {
        this.doubleMetaphone = new DoubleMetaphone();
    }

    // Tests null and empty string input
    @Test
    public void testDoubleMetaphone_nullAndEmptyInput_returnsNull() {
        assertNull(this.doubleMetaphone.doubleMetaphone(null));
        assertNull(this.doubleMetaphone.doubleMetaphone(""));
        assertNull(this.doubleMetaphone.doubleMetaphone("   "));
        assertNull(this.doubleMetaphone.doubleMetaphone(null, true));
        assertNull(this.doubleMetaphone.doubleMetaphone("", true));
    }

    // Tests defect fix where 'G' followed by "IER" should produce 'J' for both primary and alternate
    @Test
    public void testDoubleMetaphone_gFollowedByIER_returnsJEncoding() {
        assertEquals("ANJR", this.doubleMetaphone.doubleMetaphone("Angier", false));
        assertEquals("ANJR", this.doubleMetaphone.doubleMetaphone("Angier", true));
        assertEquals("AJR", this.doubleMetaphone.doubleMetaphone("Augier", false));
        assertEquals("AJR", this.doubleMetaphone.doubleMetaphone("Augier", true));
    }

    // Tests default and custom max code length
    @Test
    public void testGetSetMaxCodeLen_customLength_respectsLimit() {
        assertEquals(4, this.doubleMetaphone.getMaxCodeLen());
        this.doubleMetaphone.setMaxCodeLen(6);
        assertEquals(6, this.doubleMetaphone.getMaxCodeLen());
        assertEquals("ALKSNT", this.doubleMetaphone.doubleMetaphone("Alexander"));
        this.doubleMetaphone.setMaxCodeLen(2);
        assertEquals("AL", this.doubleMetaphone.doubleMetaphone("Alexander"));
    }

    // Tests StringEncoder encode(Object) with valid and invalid types
    @Test
    public void testEncode_objectInput_returnsEncodedString() throws EncoderException {
        Object result = this.doubleMetaphone.encode((Object) "Testing");
        assertEquals("TSTN", result);
    }

    // Tests encode(Object) exception path for non-string input
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        this.doubleMetaphone.encode(Integer.valueOf(123));
    }

    // Tests StringEncoder encode(String)
    @Test
    public void testEncode_stringInput_returnsEncodedString() {
        assertEquals("TSTN", this.doubleMetaphone.encode("Testing"));
    }

    // Tests isDoubleMetaphoneEqual comparisons
    @Test
    public void testIsDoubleMetaphoneEqual_matchingAndNonMatchingWords_returnsCorrectBoolean() {
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt"));
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt", false));
        assertTrue(this.doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt", true));
        assertFalse(this.doubleMetaphone.isDoubleMetaphoneEqual("John", "Peter"));
    }

    // Tests words with silent start letters
    @Test
    public void testDoubleMetaphone_silentStartLetters_encodesCorrectly() {
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Gnat"));
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Knight"));
        assertEquals("NM", this.doubleMetaphone.doubleMetaphone("Pneumonia"));
        assertEquals("RK", this.doubleMetaphone.doubleMetaphone("Wreck"));
        assertEquals("SM", this.doubleMetaphone.doubleMetaphone("Psalm"));
    }

    // Tests vowel and vowel-like characters
    @Test
    public void testDoubleMetaphone_vowelsAndSpecialCharacters_encodesCorrectly() {
        assertEquals("APL", this.doubleMetaphone.doubleMetaphone("Apple"));
        assertEquals("ERN", this.doubleMetaphone.doubleMetaphone("Orange"));
        assertEquals("S", this.doubleMetaphone.doubleMetaphone("\u00C7a"));
        assertEquals("N", this.doubleMetaphone.doubleMetaphone("\u00D1o"));
    }

    // Tests various 'C' branch conditions
    @Test
    public void testDoubleMetaphone_cBranches_encodesCorrectly() {
        assertEquals("SSR", this.doubleMetaphone.doubleMetaphone("Caesar"));
        assertEquals("KLK", this.doubleMetaphone.doubleMetaphone("Chianti"));
        assertEquals("MK", this.doubleMetaphone.doubleMetaphone("Michael"));
        assertEquals("KMR", this.doubleMetaphone.doubleMetaphone("Chemistry"));
        assertEquals("SRN", this.doubleMetaphone.doubleMetaphone("Czerny", false));
        assertEquals("XRN", this.doubleMetaphone.doubleMetaphone("Czerny", true));
        assertEquals("FKX", this.doubleMetaphone.doubleMetaphone("Focaccia"));
        assertEquals("AKST", this.doubleMetaphone.doubleMetaphone("Accident"));
        assertEquals("BK", this.doubleMetaphone.doubleMetaphone("Bacchus"));
        assertEquals("BXS", this.doubleMetaphone.doubleMetaphone("Bacci"));
        assertEquals("MK", this.doubleMetaphone.doubleMetaphone("McClelland"));
    }

    // Tests various 'G' and 'GH' branch conditions
    @Test
    public void testDoubleMetaphone_gBranches_encodesCorrectly() {
        assertEquals("LF", this.doubleMetaphone.doubleMetaphone("Laugh"));
        assertEquals("K", this.doubleMetaphone.doubleMetaphone("Hugh"));
        assertEquals("JN", this.doubleMetaphone.doubleMetaphone("Ghislane"));
        assertEquals("N", this.doubleMetaphone.doubleMetaphone("Agnes"));
        assertEquals("KL", this.doubleMetaphone.doubleMetaphone("Tagliaro"));
        assertEquals("KJ", this.doubleMetaphone.doubleMetaphone("Gell"));
        assertEquals("TNKR", this.doubleMetaphone.doubleMetaphone("Danger"));
        assertEquals("BJ", this.doubleMetaphone.doubleMetaphone("Biaggi"));
    }

    // Tests various 'J' branch conditions
    @Test
    public void testDoubleMetaphone_jBranches_encodesCorrectly() {
        assertEquals("HS", this.doubleMetaphone.doubleMetaphone("Jose", false));
        assertEquals("HS", this.doubleMetaphone.doubleMetaphone("Jose", true));
        assertEquals("SNHS", this.doubleMetaphone.doubleMetaphone("San Jose"));
        assertEquals("J", this.doubleMetaphone.doubleMetaphone("John", false));
        assertEquals("A", this.doubleMetaphone.doubleMetaphone("John", true));
        assertEquals("BJ", this.doubleMetaphone.doubleMetaphone("Baja", false));
        assertEquals("BH", this.doubleMetaphone.doubleMetaphone("Baja", true));
    }

    // Tests various 'S', 'SC', and 'SH' branch conditions
    @Test
    public void testDoubleMetaphone_sBranches_encodesCorrectly() {
        assertEquals("ALNT", this.doubleMetaphone.doubleMetaphone("Island"));
        assertEquals("XKR", this.doubleMetaphone.doubleMetaphone("Sugar", false));
        assertEquals("SKR", this.doubleMetaphone.doubleMetaphone("Sugar", true));
        assertEquals("SM", this.doubleMetaphone.doubleMetaphone("Shalom"));
        assertEquals("SMTH", this.doubleMetaphone.doubleMetaphone("Smith", false));
        assertEquals("XMT", this.doubleMetaphone.doubleMetaphone("Smith", true));
        assertEquals("SKL", this.doubleMetaphone.doubleMetaphone("School"));
        assertEquals("XMRH", this.doubleMetaphone.doubleMetaphone("Schermerhorn", false));
        assertEquals("SKMR", this.doubleMetaphone.doubleMetaphone("Schermerhorn", true));
        assertEquals("SN", this.doubleMetaphone.doubleMetaphone("Scene"));
    }

    // Tests 'T', 'W', 'X', and 'Z' branch conditions
    @Test
    public void testDoubleMetaphone_twxzBranches_encodesCorrectly() {
        assertEquals("XN", this.doubleMetaphone.doubleMetaphone("Nation"));
        assertEquals("0M", this.doubleMetaphone.doubleMetaphone("Thomas", false));
        assertEquals("TM", this.doubleMetaphone.doubleMetaphone("Thomas", true));
        assertEquals("A", this.doubleMetaphone.doubleMetaphone("Wasserman", false));
        assertEquals("FSRM", this.doubleMetaphone.doubleMetaphone("Wasserman", true));
        assertEquals("ARN", this.doubleMetaphone.doubleMetaphone("Arnow", false));
        assertEquals("ARNF", this.doubleMetaphone.doubleMetaphone("Arnow", true));
        assertEquals("FLPT", this.doubleMetaphone.doubleMetaphone("Filipowicz", false));
        assertEquals("FLPF", this.doubleMetaphone.doubleMetaphone("Filipowicz", true));
        assertEquals("S", this.doubleMetaphone.doubleMetaphone("Xavier"));
        assertEquals("BR", this.doubleMetaphone.doubleMetaphone("Breaux"));
        assertEquals("J", this.doubleMetaphone.doubleMetaphone("Zhao"));
        assertEquals("S", this.doubleMetaphone.doubleMetaphone("Zack", false));
        assertEquals("TSK", this.doubleMetaphone.doubleMetaphone("Zack", true));
    }

    // Tests 'L', 'M', 'P', and 'R' branch conditions
    @Test
    public void testDoubleMetaphone_lmprBranches_encodesCorrectly() {
        assertEquals("FL", this.doubleMetaphone.doubleMetaphone("Villa"));
        assertEquals("AL", this.doubleMetaphone.doubleMetaphone("Alle"));
        assertEquals("0M", this.doubleMetaphone.doubleMetaphone("Thumb"));
        assertEquals("0MR", this.doubleMetaphone.doubleMetaphone("Thumber"));
        assertEquals("F", this.doubleMetaphone.doubleMetaphone("Philip"));
        assertEquals("P", this.doubleMetaphone.doubleMetaphone("Campbell"));
        assertEquals("PR", this.doubleMetaphone.doubleMetaphone("Pierre", false));
        assertEquals("PR", this.doubleMetaphone.doubleMetaphone("Pierre", true));
    }

    // Tests DoubleMetaphoneResult inner class methods directly
    @Test
    public void testDoubleMetaphoneResult_appendOperations_buildsExpectedCode() {
        DoubleMetaphone.DoubleMetaphoneResult result = this.doubleMetaphone.new DoubleMetaphoneResult(4);
        result.append('A');
        result.append('B', 'C');
        result.append("DE");
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
        assertTrue(result.isComplete());

        DoubleMetaphone.DoubleMetaphoneResult result2 = this.doubleMetaphone.new DoubleMetaphoneResult(4);
        result2.append("LONGSTRING", "LONGSTRING2");
        assertEquals("LONG", result2.getPrimary());
        assertEquals("LONG", result2.getAlternate());
        assertTrue(result2.isComplete());
    }

    // Tests additional DoubleMetaphoneResult appendPrimary and appendAlternate methods
    @Test
    public void testDoubleMetaphoneResult_primaryAndAlternateOnlyAppends_buildsCorrectCodes() {
        DoubleMetaphone.DoubleMetaphoneResult result = this.doubleMetaphone.new DoubleMetaphoneResult(5);
        result.appendPrimary('P');
        result.appendAlternate('A');
        result.appendPrimary("RIM");
        result.appendAlternate("LT");
        assertEquals("PRIM", result.getPrimary());
        assertEquals("ALT", result.getAlternate());
        assertFalse(result.isComplete());
    }

    // Tests additional 'C' and 'CH' branches (Archer, Architect, Charac, Polish/Italian endings)
    @Test
    public void testDoubleMetaphone_additionalCBranches_encodesCorrectly() {
        assertEquals("ARKT", this.doubleMetaphone.doubleMetaphone("Architect"));
        assertEquals("AXR", this.doubleMetaphone.doubleMetaphone("Archer", false));
        assertEquals("AKR", this.doubleMetaphone.doubleMetaphone("Archer", true));
        assertEquals("KRKT", this.doubleMetaphone.doubleMetaphone("Character"));
        assertEquals("AKNT", this.doubleMetaphone.doubleMetaphone("Acquaint"));
        assertEquals("PLK", this.doubleMetaphone.doubleMetaphone("Black"));
        assertEquals("SPXL", this.doubleMetaphone.doubleMetaphone("Special"));
    }

    // Tests 'D' branches (DG -> J / TK, DT, DD)
    @Test
    public void testDoubleMetaphone_dBranches_encodesCorrectly() {
        assertEquals("AJ", this.doubleMetaphone.doubleMetaphone("Edge"));
        assertEquals("ATKR", this.doubleMetaphone.doubleMetaphone("Edgar"));
        assertEquals("TT", this.doubleMetaphone.doubleMetaphone("Dated"));
    }

    // Tests 'GH', 'G', and silent/middle 'H' branches
    @Test
    public void testDoubleMetaphone_additionalGAndHBranches_encodesCorrectly() {
        assertEquals("KSTL", this.doubleMetaphone.doubleMetaphone("Ghastly"));
        assertEquals("NT", this.doubleMetaphone.doubleMetaphone("Night"));
        assertEquals("AHT", this.doubleMetaphone.doubleMetaphone("Ahead"));
        assertEquals("SN", this.doubleMetaphone.doubleMetaphone("Sign"));
    }

    // Tests Spanish 'LL' and French endings 'IER', 'T'
    @Test
    public void testDoubleMetaphone_spanishLLAndFrenchEndings_encodesCorrectly() {
        assertEquals("KBR", this.doubleMetaphone.doubleMetaphone("Cabrillo", false));
        assertEquals("KBRL", this.doubleMetaphone.doubleMetaphone("Cabrillo", true));
        assertEquals("KLK", this.doubleMetaphone.doubleMetaphone("Gallego", false));
        assertEquals("KJK", this.doubleMetaphone.doubleMetaphone("Gallego", true));
        assertEquals("KB", this.doubleMetaphone.doubleMetaphone("Cabot", false));
        assertEquals("KBT", this.doubleMetaphone.doubleMetaphone("Cabot", true));
    }

    // Tests additional 'S', 'SC', 'W', 'X', and 'Z' branches
    @Test
    public void testDoubleMetaphone_additionalSWXZBranches_encodesCorrectly() {
        assertEquals("AX", this.doubleMetaphone.doubleMetaphone("Asia", false));
        assertEquals("AS", this.doubleMetaphone.doubleMetaphone("Asia", true));
        assertEquals("RXN", this.doubleMetaphone.doubleMetaphone("Russian", false));
        assertEquals("RSN", this.doubleMetaphone.doubleMetaphone("Russian", true));
        assertEquals("AKST", this.doubleMetaphone.doubleMetaphone("Extra"));
        assertEquals("MKS", this.doubleMetaphone.doubleMetaphone("Max"));
        assertEquals("FTS", this.doubleMetaphone.doubleMetaphone("Witz", false));
        assertEquals("FX", this.doubleMetaphone.doubleMetaphone("Witz", true));
    }
}
package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetaphoneTest {

    private Metaphone metaphone;

    @Before
    public void setUp() {
        this.metaphone = new Metaphone();
    }

    // Tests null and empty string input
    @Test
    public void testMetaphone_nullOrEmptyInput_returnsEmptyString() {
        assertEquals("", this.metaphone.metaphone(null));
        assertEquals("", this.metaphone.metaphone(""));
    }

    // Tests single character input
    @Test
    public void testMetaphone_singleCharacter_returnsUpperCaseChar() {
        assertEquals("A", this.metaphone.metaphone("a"));
        assertEquals("X", this.metaphone.metaphone("x"));
    }

    // Tests initial character transformations: KN, GN, PN, AE, WR, WH, X
    @Test
    public void testMetaphone_initialTransformations_encodedCorrectly() {
        assertEquals("NT", this.metaphone.metaphone("KNIGHT"));
        assertEquals("NT", this.metaphone.metaphone("GNAT"));
        assertEquals("NM", this.metaphone.metaphone("PNEUMONIA"));
        assertEquals("ES", this.metaphone.metaphone("AEGIS"));
        assertEquals("RT", this.metaphone.metaphone("WRITE"));
        assertEquals("WT", this.metaphone.metaphone("WHITE"));
        assertEquals("SFT", this.metaphone.metaphone("XAVIER"));
    }

    // Tests words with vowels at beginning vs middle
    @Test
    public void testMetaphone_vowels_leadingIncludedMiddleIgnored() {
        assertEquals("APL", this.metaphone.metaphone("APPLE"));
        assertEquals("ORNG", this.metaphone.metaphone("ORANGE"));
    }

    // Tests 'B' silent after 'M' at the end of word vs normal 'B'
    @Test
    public void testMetaphone_bTransformations_silentOrB() {
        assertEquals("TM", this.metaphone.metaphone("DUMB"));
        assertEquals("KM", this.metaphone.metaphone("COMB"));
        assertEquals("BB", this.metaphone.metaphone("BABY"));
    }

    // Tests 'C' transformations: SC(E|I|Y), CIA, CE/CI/CY, SCH, CH, and default K
    @Test
    public void testMetaphone_cTransformations_correctlyProcessed() {
        assertEquals("SN", this.metaphone.metaphone("SCENE"));
        assertEquals("SNS", this.metaphone.metaphone("SCIENCE"));
        assertEquals("X", this.metaphone.metaphone("CIA"));
        assertEquals("ST", this.metaphone.metaphone("CITY"));
        assertEquals("SKL", this.metaphone.metaphone("SCHOOL"));
        assertEquals("KRKT", this.metaphone.metaphone("CHARACTER"));
        assertEquals("XT", this.metaphone.metaphone("CHAT"));
        assertEquals("KT", this.metaphone.metaphone("CAT"));
    }

    // Tests 'D' transformations: DGE/DGI/DGY -> J, otherwise T
    @Test
    public void testMetaphone_dTransformations_dgeToJAndDefaultT() {
        assertEquals("AJ", this.metaphone.metaphone("EDGE"));
        assertEquals("TJ", this.metaphone.metaphone("DODGER"));
        assertEquals("TR", this.metaphone.metaphone("DOOR"));
    }

    // Tests 'G' transformations: GH silent, GN/GNED silent, GI/GE/GY to J, GG to K
    @Test
    public void testMetaphone_gTransformations_correctlyProcessed() {
        assertEquals("N", this.metaphone.metaphone("GNOME"));
        assertEquals("SN", this.metaphone.metaphone("SIGN"));
        assertEquals("SNT", this.metaphone.metaphone("SIGNED"));
        assertEquals("JL", this.metaphone.metaphone("GEL"));
        assertEquals("JT", this.metaphone.metaphone("GIANT"));
        assertEquals("K", this.metaphone.metaphone("EGG"));
        assertEquals("KT", this.metaphone.metaphone("GATE"));
    }

    // Tests 'H' transformations: terminal H, H after VARSON, H before vowel
    @Test
    public void testMetaphone_hTransformations_correctlyProcessed() {
        assertEquals("AH", this.metaphone.metaphone("AHA"));
        assertEquals("A", this.metaphone.metaphone("AH"));
        assertEquals("T", this.metaphone.metaphone("THAT"));
    }

    // Tests 'K' transformation: silent after 'C', preserved otherwise
    @Test
    public void testMetaphone_kTransformations_silentAfterC() {
        assertEquals("BK", this.metaphone.metaphone("BOOK"));
        assertEquals("BK", this.metaphone.metaphone("BACK"));
    }

    // Tests 'P' transformations: PH -> F, normal P
    @Test
    public void testMetaphone_pTransformations_phToF() {
        assertEquals("FNX", this.metaphone.metaphone("PHOENIX"));
        assertEquals("PRT", this.metaphone.metaphone("PART"));
    }

    // Tests 'Q', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' transformations
    @Test
    public void testMetaphone_variousConsonants_encodedCorrectly() {
        assertEquals("KK", this.metaphone.metaphone("QUICK"));
        assertEquals("XP", this.metaphone.metaphone("SHIP"));
        assertEquals("NXN", this.metaphone.metaphone("NATION"));
        assertEquals("MTX", this.metaphone.metaphone("MATCH"));
        assertEquals("0NK", this.metaphone.metaphone("THINK"));
        assertEquals("FST", this.metaphone.metaphone("VEST"));
        assertEquals("WST", this.metaphone.metaphone("WEST"));
        assertEquals("YLT", this.metaphone.metaphone("YELL"));
        assertEquals("TKS", this.metaphone.metaphone("TAXI"));
        assertEquals("SB", this.metaphone.metaphone("ZEBRA"));
    }

    // Tests duplicate letter deduplication except 'C'
    @Test
    public void testMetaphone_duplicateLetters_deduplicated() {
        assertEquals("TR", this.metaphone.metaphone("TERRA"));
        assertEquals("APL", this.metaphone.metaphone("APPLE"));
        assertEquals("AKST", this.metaphone.metaphone("ACCIDENT"));
    }

    // Tests max code length property and truncation
    @Test
    public void testMetaphone_maxCodeLength_truncatesCorrectly() {
        assertEquals(4, this.metaphone.getMaxCodeLen());
        this.metaphone.setMaxCodeLen(2);
        assertEquals(2, this.metaphone.getMaxCodeLen());
        assertEquals("AL", this.metaphone.metaphone("ALLIGATOR"));

        this.metaphone.setMaxCodeLen(6);
        assertEquals(6, this.metaphone.getMaxCodeLen());
        assertEquals("ALKRTR", this.metaphone.metaphone("ALLIGATOR"));
    }

    // Tests encode(String) method
    @Test
    public void testEncode_stringObject_returnsEncodedString() {
        assertEquals("TEST", this.metaphone.encode("TEST"));
    }

    // Tests encode(Object) with valid String object
    @Test
    public void testEncode_validObject_returnsEncodedString() throws EncoderException {
        Object result = this.metaphone.encode((Object) "TEST");
        assertEquals("TEST", result);
    }

    // Tests encode(Object) throwing EncoderException for non-String input
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        this.metaphone.encode(Integer.valueOf(123));
    }

    // Tests isMetaphoneEqual method with matching and non-matching values
    @Test
    public void testIsMetaphoneEqual_equalAndNonEqualStrings_returnsExpectedBoolean() {
        assertTrue(this.metaphone.isMetaphoneEqual("wright", "right"));
        assertTrue(this.metaphone.isMetaphoneEqual("knight", "night"));
        assertFalse(this.metaphone.isMetaphoneEqual("cat", "dog"));
    }

    // Tests additional C transformations: CH not at start, SCY
    @Test
    public void testMetaphone_cAdditionalTransformations() {
        assertEquals("ARX", this.metaphone.metaphone("ARCH"));
        assertEquals("S0", this.metaphone.metaphone("SCYTHE"));
        assertEquals("K", this.metaphone.metaphone("CH"));
    }

    // Tests additional G transformations: GH before vowel, DG followed by non-front vowel, G after G
    @Test
    public void testMetaphone_gAdditionalTransformations() {
        assertEquals("KST", this.metaphone.metaphone("GHOST"));
        assertEquals("TK", this.metaphone.metaphone("TAG"));
        assertEquals("ALN", this.metaphone.metaphone("ALIGN"));
        assertEquals("ETKR", this.metaphone.metaphone("EDGAR"));
        assertEquals("SJST", this.metaphone.metaphone("SUGGEST"));
    }

    // Tests additional H transformations: H not followed by vowel, H after non-VARSON consonant
    @Test
    public void testMetaphone_hAdditionalTransformations() {
        assertEquals("BHNT", this.metaphone.metaphone("BEHIND"));
        assertEquals("JN", this.metaphone.metaphone("JOHN"));
        assertEquals("ALM", this.metaphone.metaphone("AHLM"));
    }

    // Tests additional S and T transformations: SIA, SIO, TIA
    @Test
    public void testMetaphone_sAndTAdditionalTransformations() {
        assertEquals("RX", this.metaphone.metaphone("RUSSIA"));
        assertEquals("PXN", this.metaphone.metaphone("PASSION"));
        assertEquals("SPXL", this.metaphone.metaphone("SPATIAL"));
    }

    // Tests W and Y ignored when not followed by a vowel
    @Test
    public void testMetaphone_wAndYSilentWhenNotBeforeVowel() {
        assertEquals("BL", this.metaphone.metaphone("BLOW"));
        assertEquals("T", this.metaphone.metaphone("DAY"));
        assertEquals("KR", this.metaphone.metaphone("CRY"));
    }
}
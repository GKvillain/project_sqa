package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.codec.EncoderException;

public class DoubleMetaphoneTest {

    // Tests null input
    @Test
    public void testDoubleMetaphone_nullInput_returnsNull() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertNull(dp.doubleMetaphone(null));
        assertNull(dp.doubleMetaphone(null, false));
        assertNull(dp.doubleMetaphone(null, true));
    }

    // Tests empty string after trim
    @Test
    public void testDoubleMetaphone_emptyInput_returnsNull() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertNull(dp.doubleMetaphone(""));
        assertNull(dp.doubleMetaphone("  "));
    }

    // Tests single vowel encoding
    @Test
    public void testDoubleMetaphone_singleVowel_returnsA() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("A", dp.doubleMetaphone("A"));
        assertEquals("A", dp.doubleMetaphone("E"));
        assertEquals("A", dp.doubleMetaphone("I"));
        assertEquals("A", dp.doubleMetaphone("O"));
        assertEquals("A", dp.doubleMetaphone("U"));
        assertEquals("A", dp.doubleMetaphone("Y"));
    }

    // Tests single consonant 'B' mapping to 'P'
    @Test
    public void testDoubleMetaphone_singleConsonantB_returnsP() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("P", dp.doubleMetaphone("B"));
    }

    // Tests silent start skipping first letter
    @Test
    public void testDoubleMetaphone_silentStart_skipsFirstLetter() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("N", dp.doubleMetaphone("GN"));
        assertEquals("N", dp.doubleMetaphone("KN"));
        assertEquals("N", dp.doubleMetaphone("PN"));
        assertEquals("R", dp.doubleMetaphone("WR"));
        assertEquals("S", dp.doubleMetaphone("PS"));
    }

    // Tests 'C' with CH at start (e.g., "CH" -> 'X')
    @Test
    public void testDoubleMetaphone_chStart_returnsX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("X", dp.doubleMetaphone("CH"));
    }

    // Tests 'C' with CHAE (Michael) -> KX
    @Test
    public void testDoubleMetaphone_chae_returnsKX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("MKL", dp.doubleMetaphone("MICHAEL"));
    }

    // Tests 'D' with DG and following vowel (e.g., "EDGE" -> "AJ")
    @Test
    public void testDoubleMetaphone_edgeWord_returnsAJ() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("AJ", dp.doubleMetaphone("EDGE"));
    }

    // Tests 'G' with GH and preceding non-vowel (e.g., "BGH" -> "BK")
    @Test
    public void testDoubleMetaphone_ghPrecedingNonVowel_returnsK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("BK", dp.doubleMetaphone("BGH"));
    }

    // Tests 'H' between vowels (e.g., "AHI" -> "AH")
    @Test
    public void testDoubleMetaphone_hBetweenVowels_returnsH() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("AH", dp.doubleMetaphone("AHI"));
    }

    // Tests 'J' with Spanish "JOSE" -> "HS"
    @Test
    public void testDoubleMetaphone_jose_returnsHS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("HS", dp.doubleMetaphone("JOSE"));
    }

    // Tests 'L' with double L and conditionL0 true (e.g., "BILLO" -> primary has 'L' only)
    @Test
    public void testDoubleMetaphone_conditionL0True_primaryLOnly() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("PAL", dp.doubleMetaphone("BILLA", false));
        assertEquals("PA", dp.doubleMetaphone("BILLA", true));
    }

    // Tests 'P' with PH -> 'F'
    @Test
    public void testDoubleMetaphone_ph_returnsF() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("F", dp.doubleMetaphone("PH"));
    }

    // Tests 'S' with special "ISL" pattern (e.g., "ISLAND" -> "ALNT"? Actually ISL at start: "ISLE" -> "AL")
    @Test
    public void testDoubleMetaphone_islPattern_skipsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("AL", dp.doubleMetaphone("ISLE"));
    }

    // Tests 'SC' with Dutch "SCHOOL" -> "SK"
    @Test
    public void testDoubleMetaphone_scDutch_returnsSK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("SKL", dp.doubleMetaphone("SCHOOL"));
    }

    // Tests 'T' with TH and "OM" -> 'T' (Thomas)
    @Test
    public void testDoubleMetaphone_thomas_returnsT() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("TMS", dp.doubleMetaphone("THOMAS"));
    }

    // Tests 'W' with WR silent -> 'R'
    @Test
    public void testDoubleMetaphone_wrSilent_returnsR() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("RT", dp.doubleMetaphone("WRITE"));
    }

    // Tests 'X' at beginning -> 'S'
    @Test
    public void testDoubleMetaphone_xStart_returnsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("S", dp.doubleMetaphone("X"));
    }

    // Tests 'Z' with slavoGermanic (e.g., "WICZ") -> primary "TS", alternate "FX"
    @Test
    public void testDoubleMetaphone_wicz_returnsTS_FX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("TS", dp.doubleMetaphone("WICZ", false));
        assertEquals("FX", dp.doubleMetaphone("WICZ", true));
    }

    // Tests slavoGermanic detection via 'K' (e.g., "KRAKOW")
    @Test
    public void testDoubleMetaphone_slavoGermanic_K_affectsG() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertTrue(dp.isDoubleMetaphoneEqual("WICZ", "WITZ", false));
        assertTrue(dp.isDoubleMetaphoneEqual("WICZ", "WITZ", true));
    }

    // Tests encode(Object) with non-String throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_nonString_throwsEncoderException() throws EncoderException {
        DoubleMetaphone dp = new DoubleMetaphone();
        dp.encode(new Integer(123));
    }

    // Tests setMaxCodeLen affects output length
    @Test
    public void testSetMaxCodeLen_shortensEncoding() {
        DoubleMetaphone dp = new DoubleMetaphone();
        dp.setMaxCodeLen(2);
        assertEquals("SM", dp.doubleMetaphone("SMITH"));
        dp.setMaxCodeLen(4);
        assertEquals("SM0", dp.doubleMetaphone("SMITH")); // default length 4, "SMITH" -> "SM0"
    }

    // Tests isDoubleMetaphoneEqual
    @Test
    public void testIsDoubleMetaphoneEqual_equalReturnsTrue() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertTrue(dp.isDoubleMetaphoneEqual("SMITH", "SMYTH"));
        assertFalse(dp.isDoubleMetaphoneEqual("SMITH", "JONES"));
    }

    // Tests alternate encoding for a case with difference (if any)
    @Test
    public void testDoubleMetaphone_alternateDifferent_returnsDifferent() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("SRN", dp.doubleMetaphone("CZERNY", false));
        assertEquals("XRN", dp.doubleMetaphone("CZERNY", true));
    }

    // ========== New test cases for uncovered branches ==========

    // Vowel at start of longer word
    @Test
    public void testDoubleMetaphone_vowelStartLongWord() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("AN", dp.doubleMetaphone("AN"));
        assertEquals("AP", dp.doubleMetaphone("AB"));
    }

    // 'C' before 'A', 'O', 'U', 'K', 'Q' -> 'K'
    @Test
    public void testDoubleMetaphone_cBeforeA_returnsK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("KT", dp.doubleMetaphone("CAT"));
    }

    // 'C' before 'E', 'I', 'Y' -> 'S'
    @Test
    public void testDoubleMetaphone_cBeforeE_returnsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("ST", dp.doubleMetaphone("CITY"));
    }

    // 'C' with 'TCH' pattern -> 'X'
    @Test
    public void testDoubleMetaphone_tchPattern_returnsX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("KX", dp.doubleMetaphone("CATCH"));
    }

    // 'C' with 'CIA' pattern -> 'S'
    @Test
    public void testDoubleMetaphone_ciaPattern_returnsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("S", dp.doubleMetaphone("CIAO"));
    }

    // 'D' followed by 'G' without vowel after -> 'T' + 'K'
    @Test
    public void testDoubleMetaphone_dgNoVowel_returnsTK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("ATK", dp.doubleMetaphone("EDG"));
    }

    // Simple 'F' mapping
    @Test
    public void testDoubleMetaphone_singleF_returnsF() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("F", dp.doubleMetaphone("F"));
        assertEquals("AF", dp.doubleMetaphone("AF"));
    }

    // 'G' before 'E' (non-Slavic) -> 'J'/'K'
    @Test
    public void testDoubleMetaphone_gBeforeE_returnsJK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("JL", dp.doubleMetaphone("GEL", false));
        assertEquals("KL", dp.doubleMetaphone("GEL", true));
    }

    // 'H' after consonant (not between vowels) -> not appended
    @Test
    public void testDoubleMetaphone_hAfterConsonant_skipped() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("P", dp.doubleMetaphone("BH"));
    }

    // Simple 'J' mapping (non-Spanish)
    @Test
    public void testDoubleMetaphone_simpleJ_returnsJ() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("J", dp.doubleMetaphone("J"));
        assertEquals("JF", dp.doubleMetaphone("JAVA"));
    }

    // Single 'K'
    @Test
    public void testDoubleMetaphone_singleK_returnsK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("K", dp.doubleMetaphone("K"));
    }

    // Single 'L'
    @Test
    public void testDoubleMetaphone_singleL_returnsL() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("L", dp.doubleMetaphone("L"));
        assertEquals("AL", dp.doubleMetaphone("AL"));
    }

    // Single 'M'
    @Test
    public void testDoubleMetaphone_singleM_returnsM() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("M", dp.doubleMetaphone("M"));
    }

    // Single 'N'
    @Test
    public void testDoubleMetaphone_singleN_returnsN() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("N", dp.doubleMetaphone("N"));
    }

    // 'P' and double 'P'
    @Test
    public void testDoubleMetaphone_singleAndDoubleP() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("P", dp.doubleMetaphone("P"));
        assertEquals("P", dp.doubleMetaphone("PP"));
    }

    // Single 'Q' -> 'K'
    @Test
    public void testDoubleMetaphone_singleQ_returnsK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("K", dp.doubleMetaphone("Q"));
    }

    // Single 'R'
    @Test
    public void testDoubleMetaphone_singleR_returnsR() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("R", dp.doubleMetaphone("R"));
    }

    // 'S' with 'SH' -> 'X'
    @Test
    public void testDoubleMetaphone_shStart_returnsX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("XP", dp.doubleMetaphone("SHIP"));
    }

    // 'S' with 'SC' (non-Dutch) -> 'S'
    @Test
    public void testDoubleMetaphone_scNonDutch_returnsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("SN", dp.doubleMetaphone("SCENE"));
    }

    // 'T' with 'TION' -> 'X'
    @Test
    public void testDoubleMetaphone_tionPattern_returnsX() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("NXN", dp.doubleMetaphone("NATION"));
    }

    // Simple 'V' -> 'F'
    @Test
    public void testDoubleMetaphone_singleV_returnsF() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("F", dp.doubleMetaphone("V"));
    }

    // 'W' with 'WH' at start -> primary 'A', alternate 'F'
    @Test
    public void testDoubleMetaphone_whStart_returnsAF() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("A", dp.doubleMetaphone("WHY", false));
        assertEquals("F", dp.doubleMetaphone("WHY", true));
    }

    // 'X' in middle -> 'K'
    @Test
    public void testDoubleMetaphone_xMiddle_returnsK() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("AK", dp.doubleMetaphone("AX"));
    }

    // 'Y' after vowel -> not appended
    @Test
    public void testDoubleMetaphone_yAfterVowel_skipped() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("A", dp.doubleMetaphone("AY"));
    }

    // Simple 'Z' -> 'S'
    @Test
    public void testDoubleMetaphone_singleZ_returnsS() {
        DoubleMetaphone dp = new DoubleMetaphone();
        assertEquals("S", dp.doubleMetaphone("Z"));
        assertEquals("AS", dp.doubleMetaphone("AZ"));
    }
}
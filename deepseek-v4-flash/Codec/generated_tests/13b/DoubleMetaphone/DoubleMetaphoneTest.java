package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

public class DoubleMetaphoneTest {

    private DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

    // Tests null input returns null
    @Test
    public void testDoubleMetaphone_nullInput_returnsNull() {
        assertNull(doubleMetaphone.doubleMetaphone(null));
    }

    // Tests empty string returns null
    @Test
    public void testDoubleMetaphone_emptyString_returnsNull() {
        assertNull(doubleMetaphone.doubleMetaphone(""));
    }

    // Tests whitespace-only input returns null
    @Test
    public void testDoubleMetaphone_whitespaceInput_returnsNull() {
        assertNull(doubleMetaphone.doubleMetaphone("   "));
    }

    // Tests single vowel at start appends 'A'
    @Test
    public void testDoubleMetaphone_singleVowel_returnsA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("A"));
    }

    // Tests silent start "GN" skips first character
    @Test
    public void testDoubleMetaphone_silentStartGN_skipsG() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Gnome"));
    }

    // Tests silent start "KN"
    @Test
    public void testDoubleMetaphone_silentStartKN_returnsN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Knight"));
    }

    // Tests silent start "PN"
    @Test
    public void testDoubleMetaphone_silentStartPN_returnsN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Pneumonia"));
    }

    // Tests silent start "WR"
    @Test
    public void testDoubleMetaphone_silentStartWR_returnsR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("Wright"));
    }

    // Tests silent start "PS"
    @Test
    public void testDoubleMetaphone_silentStartPS_returnsS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Psychology"));
    }

    // Tests 'B' with double B
    @Test
    public void testDoubleMetaphone_doubleB_returnsSingleP() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("Abbot"));
    }

    // Tests 'C' with "CH" and conditionCH0 (Greek root "chorus")
    @Test
    public void testDoubleMetaphone_CHGreek_returnsK() {
        assertEquals("KRS", doubleMetaphone.doubleMetaphone("Chorus"));
    }

    // Tests 'C' with "CH" and conditionCH0 (beginning "ch" not Greek)
    @Test
    public void testDoubleMetaphone_CHBeginningNonGreek_returnsX() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("Chase"));
    }

    // Tests 'C' with "CZ" and not "WICZ" -> "SX"
    @Test
    public void testDoubleMetaphone_Czerny_returnsSXRN() {
        assertEquals("SXRN", doubleMetaphone.doubleMetaphone("Czerny"));
    }

    // Tests 'C' with "CIA" -> 'X'
    @Test
    public void testDoubleMetaphone_focaccia_returnsFX() {
        assertEquals("FX", doubleMetaphone.doubleMetaphone("Focaccia"));
    }

    // Tests double "CC" with "I" and not "HU" (Italian "bacci")
    @Test
    public void testDoubleMetaphone_bacci_returnsBX() {
        assertEquals("BX", doubleMetaphone.doubleMetaphone("Bacci"));
    }

    // Tests 'C' with "CI" before vowel -> "SX"
    @Test
    public void testDoubleMetaphone_CIO_returnsSX() {
        assertEquals("SX", doubleMetaphone.doubleMetaphone("Cio"));
    }

    // Tests 'C' with "CK" -> "K"
    @Test
    public void testDoubleMetaphone_ck_returnsK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Back"));
    }

    // Tests 'D' with "DG" and "I" -> 'J'
    @Test
    public void testDoubleMetaphone_edge_returnsJ() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("Edge"));
    }

    // Tests 'D' with "DG" not followed by I/E/Y -> "TK"
    @Test
    public void testDoubleMetaphone_edgar_returnsTKR() {
        assertEquals("TKR", doubleMetaphone.doubleMetaphone("Edgar"));
    }

    // Tests 'G' with "GH" after vowel and not special -> 'K'
    @Test
    public void testDoubleMetaphone_GH_afterVowel_returnsK() {
        assertEquals("TKKF", doubleMetaphone.doubleMetaphone("Tough"));
    }

    // Tests 'G' with "GN" at start and vowel before -> "N" alternate "KN"
    @Test
    public void testDoubleMetaphone_GN_initialVowelBefore_returnsN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Agnes"));
    }

    // Tests 'H' between vowels -> 'H'
    @Test
    public void testDoubleMetaphone_H_betweenVowels_returnsH() {
        assertEquals("AH", doubleMetaphone.doubleMetaphone("Aha"));
    }

    // Tests 'J' in "JOSE" at start -> 'H'
    @Test
    public void testDoubleMetaphone_jose_returnsHS() {
        assertEquals("HS", doubleMetaphone.doubleMetaphone("Jose"));
    }

    // Tests 'L' with double L and conditionL0 (ILLO at end)
    @Test
    public void testDoubleMetaphone_illo_returnsL() {
        assertEquals("L", doubleMetaphone.doubleMetaphone("Tillo"));
    }

    // Tests 'M' with conditionM0 (UMB before ER)
    @Test
    public void testDoubleMetaphone_umber_returnsM() {
        assertEquals("M", doubleMetaphone.doubleMetaphone("Umber"));
    }

    // Tests 'P' with "PH" -> 'F'
    @Test
    public void testDoubleMetaphone_ph_returnsF() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("Phonetic"));
    }

    // Tests 'R' at end with "IE" before and not "ME"/"MA" -> alternate 'R'
    @Test
    public void testDoubleMetaphone_ieEnd_alternateR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("Carrie"));
    }

    // Tests 'S' with "SC" and "H" not Dutch -> 'X'
    @Test
    public void testDoubleMetaphone_sch_returnsX() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("School"));
    }

    // Tests 'T' with "TION" -> 'X'
    @Test
    public void testDoubleMetaphone_tion_returnsX() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("Action"));
    }

    // Tests 'T' with "TH" not special -> "0T"
    @Test
    public void testDoubleMetaphone_th_returns0T() {
        assertEquals("0T", doubleMetaphone.doubleMetaphone("Theater"));
    }

    // Tests 'W' at start before vowel -> 'A' (or 'F' alternate)
    @Test
    public void testDoubleMetaphone_Wasserman_returnsASRMN() {
        assertEquals("ASRMN", doubleMetaphone.doubleMetaphone("Wasserman"));
    }

    // Tests 'X' at start -> 'S'
    @Test
    public void testDoubleMetaphone_xavier_returnsSF() {
        assertEquals("SF", doubleMetaphone.doubleMetaphone("Xavier"));
    }

    // Tests 'Z' with "ZH" -> 'J'
    @Test
    public void testDoubleMetaphone_zhang_returnsJN() {
        assertEquals("JN", doubleMetaphone.doubleMetaphone("Zhang"));
    }

    // Tests alternate encoding via doubleMetaphone(String, boolean)
    @Test
    public void testDoubleMetaphone_alternateEncoding_returnsAlternate() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Carrie", false));
        assertEquals("KR", doubleMetaphone.doubleMetaphone("Carrie", true));
    }

    // Tests isDoubleMetaphoneEqual
    @Test
    public void testIsDoubleMetaphoneEqual_sameEnconding_returnsTrue() {
        assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt"));
    }

    // Tests isDoubleMetaphoneEqual with alternate
    @Test
    public void testIsDoubleMetaphoneEqual_alternate_returnsTrue() {
        assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("Carrie", "Carrie", false));
        assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("Carrie", "Carrie", true));
    }

    // Tests encode(String)
    @Test
    public void testEncode_string_returnsDoubleMetaphone() {
        assertEquals("KNS", doubleMetaphone.encode("Knight"));
    }

    // Tests encode(Object) with non-String throws EncoderException
    @Test(expected = org.apache.commons.codec.EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() {
        doubleMetaphone.encode(new Integer(123));
    }

    // Tests encode(Object) with String works
    @Test
    public void testEncode_stringObject_returnsDoubleMetaphone() throws Exception {
        assertEquals("KNS", doubleMetaphone.encode((Object) "Knight"));
    }

    // Tests setMaxCodeLen affects output length
    @Test
    public void testSetMaxCodeLen_shortensEncoding() {
        doubleMetaphone.setMaxCodeLen(3);
        assertEquals("KNS", doubleMetaphone.doubleMetaphone("Knight"));
        // reset for other tests
        doubleMetaphone.setMaxCodeLen(4);
    }

    // ========== New test cases to cover uncovered parts ==========

    // Single vowel "E", "I", "O", "U"
    @Test
    public void testDoubleMetaphone_singleVowelE_returnsA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("E"));
    }

    @Test
    public void testDoubleMetaphone_singleVowelI_returnsA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("I"));
    }

    @Test
    public void testDoubleMetaphone_singleVowelO_returnsA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("O"));
    }

    @Test
    public void testDoubleMetaphone_singleVowelU_returnsA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("U"));
    }

    // Single consonants
    @Test
    public void testDoubleMetaphone_singleB_returnsP() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("B"));
    }

    @Test
    public void testDoubleMetaphone_singleC_returnsK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("C"));
    }

    @Test
    public void testDoubleMetaphone_singleD_returnsT() {
        assertEquals("T", doubleMetaphone.doubleMetaphone("D"));
    }

    @Test
    public void testDoubleMetaphone_singleF_returnsF() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("F"));
    }

    @Test
    public void testDoubleMetaphone_singleG_returnsK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("G"));
    }

    @Test
    public void testDoubleMetaphone_singleH_returnsH() {
        assertEquals("H", doubleMetaphone.doubleMetaphone("H"));
    }

    @Test
    public void testDoubleMetaphone_singleJ_returnsJ() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("J"));
    }

    @Test
    public void testDoubleMetaphone_singleK_returnsK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("K"));
    }

    @Test
    public void testDoubleMetaphone_singleL_returnsL() {
        assertEquals("L", doubleMetaphone.doubleMetaphone("L"));
    }

    @Test
    public void testDoubleMetaphone_singleM_returnsM() {
        assertEquals("M", doubleMetaphone.doubleMetaphone("M"));
    }

    @Test
    public void testDoubleMetaphone_singleN_returnsN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("N"));
    }

    @Test
    public void testDoubleMetaphone_singleP_returnsP() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("P"));
    }

    @Test
    public void testDoubleMetaphone_singleQ_returnsK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Q"));
    }

    @Test
    public void testDoubleMetaphone_singleR_returnsR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("R"));
    }

    @Test
    public void testDoubleMetaphone_singleS_returnsS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("S"));
    }

    @Test
    public void testDoubleMetaphone_singleT_returnsT() {
        assertEquals("T", doubleMetaphone.doubleMetaphone("T"));
    }

    @Test
    public void testDoubleMetaphone_singleV_returnsF() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("V"));
    }

    @Test
    public void testDoubleMetaphone_singleW_returnsW() {
        // "W" alone (no following vowel) -> "W"
        assertEquals("W", doubleMetaphone.doubleMetaphone("W"));
    }

    @Test
    public void testDoubleMetaphone_singleX_returnsS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("X"));
    }

    @Test
    public void testDoubleMetaphone_singleZ_returnsS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Z"));
    }

    // Double letters (not already covered)
    @Test
    public void testDoubleMetaphone_doubleD_returnsT() {
        assertEquals("AT", doubleMetaphone.doubleMetaphone("Add"));
    }

    @Test
    public void testDoubleMetaphone_doubleF_returnsF() {
        assertEquals("AF", doubleMetaphone.doubleMetaphone("Aff"));
    }

    @Test
    public void testDoubleMetaphone_doubleG_returnsK() {
        assertEquals("AK", doubleMetaphone.doubleMetaphone("Egg"));
    }

    @Test
    public void testDoubleMetaphone_doubleL_returnsL() {
        assertEquals("AL", doubleMetaphone.doubleMetaphone("All"));
    }

    @Test
    public void testDoubleMetaphone_doubleM_returnsM() {
        assertEquals("AM", doubleMetaphone.doubleMetaphone("Imm"));
    }

    @Test
    public void testDoubleMetaphone_doubleN_returnsN() {
        assertEquals("AN", doubleMetaphone.doubleMetaphone("Ann"));
    }

    @Test
    public void testDoubleMetaphone_doubleP_returnsP() {
        assertEquals("AP", doubleMetaphone.doubleMetaphone("App"));
    }

    @Test
    public void testDoubleMetaphone_doubleR_returnsR() {
        assertEquals("AR", doubleMetaphone.doubleMetaphone("Arr"));
    }

    @Test
    public void testDoubleMetaphone_doubleS_returnsS() {
        assertEquals("AS", doubleMetaphone.doubleMetaphone("Ass"));
    }

    @Test
    public void testDoubleMetaphone_doubleT_returnsT() {
        assertEquals("AT", doubleMetaphone.doubleMetaphone("Att"));
    }

    @Test
    public void testDoubleMetaphone_doubleC_returnsK() {
        assertEquals("AK", doubleMetaphone.doubleMetaphone("Acc"));
    }

    // Common words covering various patterns
    @Test
    public void testDoubleMetaphone_cat_returnsKT() {
        assertEquals("KT", doubleMetaphone.doubleMetaphone("Cat"));
    }

    @Test
    public void testDoubleMetaphone_dog_returnsTK() {
        assertEquals("TK", doubleMetaphone.doubleMetaphone("Dog"));
    }

    @Test
    public void testDoubleMetaphone_fish_returnsFX() {
        assertEquals("FX", doubleMetaphone.doubleMetaphone("Fish"));
    }

    @Test
    public void testDoubleMetaphone_ball_returnsPL() {
        assertEquals("PL", doubleMetaphone.doubleMetaphone("Ball"));
    }

    @Test
    public void testDoubleMetaphone_gel_returnsJL() {
        assertEquals("JL", doubleMetaphone.doubleMetaphone("Gel"));
    }

    @Test
    public void testDoubleMetaphone_gone_returnsKN() {
        assertEquals("KN", doubleMetaphone.doubleMetaphone("Gone"));
    }

    @Test
    public void testDoubleMetaphone_jazz_returnsJS() {
        assertEquals("JS", doubleMetaphone.doubleMetaphone("Jazz"));
    }

    @Test
    public void testDoubleMetaphone_kick_returnsKK() {
        assertEquals("KK", doubleMetaphone.doubleMetaphone("Kick"));
    }

    @Test
    public void testDoubleMetaphone_luck_returnsLK() {
        assertEquals("LK", doubleMetaphone.doubleMetaphone("Luck"));
    }

    @Test
    public void testDoubleMetaphone_much_returnsMX() {
        assertEquals("MX", doubleMetaphone.doubleMetaphone("Much"));
    }

    @Test
    public void testDoubleMetaphone_rich_returnsRX() {
        assertEquals("RX", doubleMetaphone.doubleMetaphone("Rich"));
    }

    @Test
    public void testDoubleMetaphone_zoo_returnsS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Zoo"));
    }

    @Test
    public void testDoubleMetaphone_box_returnsPKS() {
        assertEquals("PKS", doubleMetaphone.doubleMetaphone("Box"));
    }

    @Test
    public void testDoubleMetaphone_texas_returnsTKSS() {
        assertEquals("TKSS", doubleMetaphone.doubleMetaphone("Texas"));
    }

    @Test
    public void testDoubleMetaphone_hat_returnsHT() {
        assertEquals("HT", doubleMetaphone.doubleMetaphone("Hat"));
    }

    @Test
    public void testDoubleMetaphone_jack_returnsJK() {
        assertEquals("JK", doubleMetaphone.doubleMetaphone("Jack"));
    }

    @Test
    public void testDoubleMetaphone_vance_returnsFNS() {
        assertEquals("FNS", doubleMetaphone.doubleMetaphone("Vance"));
    }

    @Test
    public void testDoubleMetaphone_wack_returnsAK() {
        assertEquals("AK", doubleMetaphone.doubleMetaphone("Wack"));
    }

    @Test
    public void testDoubleMetaphone_wish_returnsAX() {
        assertEquals("AX", doubleMetaphone.doubleMetaphone("Wish"));
    }

    @Test
    public void testDoubleMetaphone_caesar_returnsSSR() {
        assertEquals("SSR", doubleMetaphone.doubleMetaphone("Caesar"));
    }
}
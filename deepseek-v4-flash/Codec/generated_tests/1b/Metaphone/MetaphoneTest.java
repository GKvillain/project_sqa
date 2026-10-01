package org.apache.commons.codec.language;

import static org.junit.Assert.*;

import org.junit.Test;

public class MetaphoneTest {

    // Tests null input returns empty string
    @Test
    public void testMetaphone_nullInput_returnsEmptyString() {
        Metaphone mp = new Metaphone();
        assertEquals("", mp.metaphone(null));
    }

    // Tests empty string input returns empty string
    @Test
    public void testMetaphone_emptyString_returnsEmptyString() {
        Metaphone mp = new Metaphone();
        assertEquals("", mp.metaphone(""));
    }

    // Tests single character input returns uppercase character
    @Test
    public void testMetaphone_singleCharacter_returnsUppercaseChar() {
        Metaphone mp = new Metaphone();
        assertEquals("A", mp.metaphone("a"));
        assertEquals("Z", mp.metaphone("z"));
    }

    // Tests initial KN becomes N
    @Test
    public void testMetaphone_initialKN_returnsN() {
        Metaphone mp = new Metaphone();
        assertEquals("N", mp.metaphone("kn"));
    }

    // Tests initial GN becomes N
    @Test
    public void testMetaphone_initialGN_returnsN() {
        Metaphone mp = new Metaphone();
        assertEquals("N", mp.metaphone("gn"));
    }

    // Tests initial AE becomes E
    @Test
    public void testMetaphone_initialAE_returnsE() {
        Metaphone mp = new Metaphone();
        assertEquals("E", mp.metaphone("ae"));
    }

    // Tests initial WR becomes R
    @Test
    public void testMetaphone_initialWR_returnsR() {
        Metaphone mp = new Metaphone();
        assertEquals("R", mp.metaphone("wr"));
    }

    // Tests initial WH becomes W
    @Test
    public void testMetaphone_initialWH_returnsW() {
        Metaphone mp = new Metaphone();
        assertEquals("W", mp.metaphone("wh"));
    }

    // Tests initial X becomes S
    @Test
    public void testMetaphone_initialX_returnsS() {
        Metaphone mp = new Metaphone();
        assertEquals("S", mp.metaphone("xenon"));
    }

    // Tests B after M at end of word is silent
    @Test
    public void testMetaphone_wordEndingMB_returnsM() {
        Metaphone mp = new Metaphone();
        assertEquals("M", mp.metaphone("mb"));
    }

    // Tests CIA maps to X
    @Test
    public void testMetaphone_CIA_returnsX() {
        Metaphone mp = new Metaphone();
        assertEquals("X", mp.metaphone("cia"));
    }

    // Tests CH at start before consonant maps to K
    @Test
    public void testMetaphone_CH_initialBeforeConsonant_returnsK() {
        Metaphone mp = new Metaphone();
        assertEquals("K", mp.metaphone("chris"));
    }

    // Tests CH at start before vowel maps to X
    @Test
    public void testMetaphone_CH_initialBeforeVowel_returnsX() {
        Metaphone mp = new Metaphone();
        assertEquals("X", mp.metaphone("charlie"));
    }

    // Tests DGE maps to J
    @Test
    public void testMetaphone_DGE_returnsJ() {
        Metaphone mp = new Metaphone();
        assertEquals("J", mp.metaphone("dge"));
    }

    // Tests PH maps to F
    @Test
    public void testMetaphone_PH_returnsF() {
        Metaphone mp = new Metaphone();
        assertEquals("F", mp.metaphone("ph"));
    }

    // Tests TH maps to 0
    @Test
    public void testMetaphone_TH_returnsZero() {
        Metaphone mp = new Metaphone();
        assertEquals("0", mp.metaphone("th"));
    }

    // Tests TCH is silent
    @Test
    public void testMetaphone_TCH_returnsT() {
        Metaphone mp = new Metaphone();
        assertEquals("T", mp.metaphone("tch"));
    }

    // Tests default max code length is 4
    @Test
    public void testGetMaxCodeLen_default_returns4() {
        Metaphone mp = new Metaphone();
        assertEquals(4, mp.getMaxCodeLen());
    }

    // Tests setMaxCodeLen changes max code length
    @Test
    public void testSetMaxCodeLen_positiveValue_affectsOutput() {
        Metaphone mp = new Metaphone();
        mp.setMaxCodeLen(10);
        assertEquals(10, mp.getMaxCodeLen());
        String result = mp.metaphone("kn");
        assertEquals("N", result);
    }

    // Tests encode with non-String object throws EncoderException
    @Test(expected = org.apache.commons.codec.EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws Exception {
        Metaphone mp = new Metaphone();
        mp.encode(Integer.valueOf(1));
    }

    // Tests isMetaphoneEqual returns true for same sounding words
    @Test
    public void testIsMetaphoneEqual_sameSound_returnsTrue() {
        Metaphone mp = new Metaphone();
        assertTrue(mp.isMetaphoneEqual("knight", "night"));
    }

    // Tests encode method (String version) works correctly
    @Test
    public void testEncode_stringInput_returnsMetaphone() {
        Metaphone mp = new Metaphone();
        assertEquals("K", mp.encode("chris"));
    }

    // Tests vowel after first consonant is skipped
    @Test
    public void testMetaphone_vowelAfterConsonant_skipped() {
        Metaphone mp = new Metaphone();
        assertEquals("B", mp.metaphone("ba"));
        assertEquals("T", mp.metaphone("to"));
        assertEquals("K", mp.metaphone("ka"));
    }

    // Tests duplicate consonants are suppressed
    @Test
    public void testMetaphone_duplicateConsonants_suppressed() {
        Metaphone mp = new Metaphone();
        assertEquals("B", mp.metaphone("bb"));
        assertEquals("T", mp.metaphone("tt"));
        assertEquals("F", mp.metaphone("ff"));
    }

    // Tests C followed by E produces S
    @Test
    public void testMetaphone_C_beforeE_returnsS() {
        Metaphone mp = new Metaphone();
        assertEquals("SNT", mp.metaphone("cent"));
    }

    // Tests G followed by E produces J
    @Test
    public void testMetaphone_G_beforeE_returnsJ() {
        Metaphone mp = new Metaphone();
        assertEquals("JM", mp.metaphone("gem"));
    }

    // Tests H after a vowel is silent
    @Test
    public void testMetaphone_silentHAfterVowel_dropped() {
        Metaphone mp = new Metaphone();
        assertEquals("A", mp.metaphone("ah"));
        assertEquals("E", mp.metaphone("eh"));
    }

    // Tests H at the beginning followed by a vowel is kept
    @Test
    public void testMetaphone_initialH_beforeVowel_kept() {
        Metaphone mp = new Metaphone();
        assertEquals("H", mp.metaphone("ha"));
    }

    // Tests G before H is silent
    @Test
    public void testMetaphone_silentG_beforeH_dropped() {
        Metaphone mp = new Metaphone();
        assertEquals("H", mp.metaphone("high"));
    }

    // Tests W after a vowel is silent
    @Test
    public void testMetaphone_silentWAfterVowel_dropped() {
        Metaphone mp = new Metaphone();
        assertEquals("A", mp.metaphone("aw"));
    }

    // Tests max code length truncates the produced code
    @Test
    public void testMetaphone_maxCodeLen_truncatesResult() {
        Metaphone mp = new Metaphone();
        mp.setMaxCodeLen(2);
        assertEquals("KR", mp.metaphone("chris"));
    }

    // Tests isMetaphoneEqual returns false for different sounding words
    @Test
    public void testIsMetaphoneEqual_differentSound_returnsFalse() {
        Metaphone mp = new Metaphone();
        assertFalse(mp.isMetaphoneEqual("cat", "dog"));
    }

    // Tests full words with known combinations
    @Test
    public void testMetaphone_knownFullWords() {
        Metaphone mp = new Metaphone();
        assertEquals("NT", mp.metaphone("knight"));
        assertEquals("NM", mp.metaphone("gnome"));
        assertEquals("RT", mp.metaphone("write"));
        assertEquals("FN", mp.metaphone("phone"));
    }
}
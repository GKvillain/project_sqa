package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    // Tests null input returns default 10 ones
    @Test
    public void testCaverphone_nullInput_returnsDefaultCode() {
        assertEquals("1111111111", caverphone.caverphone(null));
    }

    // Tests empty string returns default 10 ones
    @Test
    public void testCaverphone_emptyInput_returnsDefaultCode() {
        assertEquals("1111111111", caverphone.caverphone(""));
    }

    // Tests encode(String) method returns expected caverphone code
    @Test
    public void testEncode_string_returnsEncodedString() {
        assertEquals("PTA1111111", caverphone.encode("Peter"));
    }

    // Tests encode(Object) with valid String object
    @Test
    public void testEncode_objectString_returnsEncodedObject() throws EncoderException {
        Object result = caverphone.encode((Object) "Stevenson");
        assertEquals("STFNSN1111", result);
    }

    // Tests encode(Object) with non-string object throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        caverphone.encode(Integer.valueOf(12345));
    }

    // Tests isCaverphoneEqual with phonetically identical strings
    @Test
    public void testIsCaverphoneEqual_sameSoundingWords_returnsTrue() {
        assertTrue(caverphone.isCaverphoneEqual("Stevenson", "Stephenson"));
        assertTrue(caverphone.isCaverphoneEqual("Peter", "Peiter"));
    }

    // Tests isCaverphoneEqual with phonetically different strings
    @Test
    public void testIsCaverphoneEqual_differentWords_returnsFalse() {
        assertFalse(caverphone.isCaverphoneEqual("Peter", "Stevenson"));
    }

    // Tests words with special start prefixes (cough, rough, tough, enough, trough, gn, mb)
    @Test
    public void testCaverphone_specialPrefixes_encodedCorrectly() {
        assertEquals("KFA1111111", caverphone.caverphone("cough"));
        assertEquals("RFA1111111", caverphone.caverphone("rough"));
        assertEquals("TFA1111111", caverphone.caverphone("tough"));
        assertEquals("ANFA111111", caverphone.caverphone("enough"));
        assertEquals("TRFA111111", caverphone.caverphone("trough"));
        assertEquals("NA11111111", caverphone.caverphone("gnome"));
        assertEquals("MA11111111", caverphone.caverphone("mbamba"));
    }

    // Tests various replacement rules (cq, ci, ce, cy, tch, x, v, dg, tio, tia, ph, sh, z)
    @Test
    public void testCaverphone_replacements_encodedCorrectly() {
        assertEquals("SA11111111", caverphone.caverphone("ciao"));
        assertEquals("SA11111111", caverphone.caverphone("cease"));
        assertEquals("SA11111111", caverphone.caverphone("cyan"));
        assertEquals("TKA1111111", caverphone.caverphone("match"));
        assertEquals("KA11111111", caverphone.caverphone("xray"));
        assertEquals("FA11111111", caverphone.caverphone("vase"));
        assertEquals("KA11111111", caverphone.caverphone("bridge"));
        assertEquals("SA11111111", caverphone.caverphone("motion"));
        assertEquals("STA1111111", caverphone.caverphone("spatial"));
        assertEquals("FA11111111", caverphone.caverphone("phase"));
        assertEquals("SA11111111", caverphone.caverphone("shine"));
        assertEquals("SA11111111", caverphone.caverphone("zero"));
    }

    // Tests words ending with final e, w, r, l, and vowels
    @Test
    public void testCaverphone_endingsAndVowels_encodedCorrectly() {
        assertEquals("LA11111111", caverphone.caverphone("Lee"));
        assertEquals("PA11111111", caverphone.caverphone("bow"));
        assertEquals("KA11111111", caverphone.caverphone("car"));
        assertEquals("PA11111111", caverphone.caverphone("ball"));
    }

    // Tests handling of non-alpha characters
    @Test
    public void testCaverphone_nonAlphaCharacters_strippedProperly() {
        assertEquals(caverphone.caverphone("Peter"), caverphone.caverphone("Peter123#@!"));
    }

    // Tests long words truncation to 10 characters
    @Test
    public void testCaverphone_longWord_truncatedToTenCharacters() {
        String code = caverphone.caverphone("Supercalifragilisticexpialidocious");
        assertEquals(10, code.length());
    }

    // Tests words ending with mb
    @Test
    public void testCaverphone_endWithMb_encodedCorrectly() {
        assertEquals("KLM1111111", caverphone.caverphone("climb"));
        assertEquals("LM11111111", caverphone.caverphone("lamb"));
        assertEquals("PM11111111", caverphone.caverphone("bomb"));
    }

    // Tests cq and q replacement rules
    @Test
    public void testCaverphone_cqAndQ_encodedCorrectly() {
        assertEquals("AKR1111111", caverphone.caverphone("acquire"));
        assertEquals("KN11111111", caverphone.caverphone("queen"));
        assertEquals("KK11111111", caverphone.caverphone("quick"));
    }

    // Tests h and wh prefix rules
    @Test
    public void testCaverphone_hAndWhPrefixes_encodedCorrectly() {
        assertEquals("AL11111111", caverphone.caverphone("hello"));
        assertEquals("WT11111111", caverphone.caverphone("white"));
        assertEquals("WT11111111", caverphone.caverphone("what"));
    }

    // Tests repeated consonants collapsing (ss, tt, pp, kk, ff, mm, nn)
    @Test
    public void testCaverphone_repeatedConsonants_collapsedCorrectly() {
        assertEquals("KS11111111", caverphone.caverphone("kiss"));
        assertEquals("PTA1111111", caverphone.caverphone("butter"));
        assertEquals("AP11111111", caverphone.caverphone("apple"));
        assertEquals("KFA1111111", caverphone.caverphone("coffee"));
        assertEquals("SMA1111111", caverphone.caverphone("summer"));
        assertEquals("TNA1111111", caverphone.caverphone("dinner"));
    }

    // Tests gh combinations
    @Test
    public void testCaverphone_ghCombinations_encodedCorrectly() {
        assertEquals("NT11111111", caverphone.caverphone("night"));
        assertEquals("A111111111", caverphone.caverphone("high"));
        assertEquals("AKST111111", caverphone.caverphone("aghast"));
    }

    // Tests d and b replacement rules
    @Test
    public void testCaverphone_dAndBReplacements_encodedCorrectly() {
        assertEquals("TFT1111111", caverphone.caverphone("david"));
        assertEquals("PP11111111", caverphone.caverphone("bob"));
        assertEquals("TK11111111", caverphone.caverphone("dog"));
    }

    // Tests r, l, and w transitions before vowels vs consonants
    @Test
    public void testCaverphone_rlwTransitions_encodedCorrectly() {
        assertEquals("RN11111111", caverphone.caverphone("run"));
        assertEquals("LK11111111", caverphone.caverphone("like"));
        assertEquals("WTA1111111", caverphone.caverphone("water"));
        assertEquals("PK11111111", caverphone.caverphone("park"));
        assertEquals("MLK1111111", caverphone.caverphone("milk"));
    }
}
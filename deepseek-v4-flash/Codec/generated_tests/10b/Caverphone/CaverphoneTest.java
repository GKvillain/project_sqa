package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

public class CaverphoneTest {

    // Tests null input returns default code
    @Test
    public void testCaverphone_nullInput_returnsDefaultCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.caverphone(null));
    }

    // Tests empty string input returns default code
    @Test
    public void testCaverphone_emptyString_returnsDefaultCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.caverphone(""));
    }

    // Tests input with only non-alphabetic characters
    @Test
    public void testCaverphone_nonAlphabeticInput_returnsTransformedCode() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("123!@#");
        assertNotNull(result);
        assertTrue(result.length() == 10);
    }

    // Tests normal input "steven" produces expected output
    @Test
    public void testCaverphone_normalInputSteven_returnsCorrectCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals("STFN111111", caverphone.caverphone("steven"));
    }

    // Tests normal input with trailing "e" removed
    @Test
    public void testCaverphone_wordEndingWithE_removesFinalE() {
        Caverphone caverphone = new Caverphone();
        assertEquals("KRK1111111", caverphone.caverphone("carke"));
    }

    // Tests handling of "cough" start replacement
    @Test
    public void testCaverphone_wordStartingWithCough_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("K211111111", caverphone.caverphone("cough"));
    }

    // Tests handling of "rough" start replacement
    @Test
    public void testCaverphone_wordStartingWithRough_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("R211111111", caverphone.caverphone("rough"));
    }

    // Tests handling of "tough" start replacement
    @Test
    public void testCaverphone_wordStartingWithTough_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("T211111111", caverphone.caverphone("tough"));
    }

    // Tests handling of "enough" start replacement (2.0 only)
    @Test
    public void testCaverphone_wordStartingWithEnough_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("AN21111111", caverphone.caverphone("enough"));
    }

    // Tests handling of "trough" start replacement (2.0 only)
    @Test
    public void testCaverphone_wordStartingWithTrough_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("TR21111111", caverphone.caverphone("trough"));
    }

    // Tests handling of "gn" start replacement
    @Test
    public void testCaverphone_wordStartingWithGn_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("2N11111111", caverphone.caverphone("gnat"));
    }

    // Tests handling of "mb" start replacement
    @Test
    public void testCaverphone_wordStartingWithMb_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("M211111111", caverphone.caverphone("mbira"));
    }

    // Tests handling of "cq" replacement
    @Test
    public void testCaverphone_wordWithCq_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("AK11111111", caverphone.caverphone("acq"));
    }

    // Tests handling of "ci" replacement to "si"
    @Test
    public void testCaverphone_wordWithCi_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("S111111111", caverphone.caverphone("cici"));
    }

    // Tests handling of "tch" replacement to "2ch"
    @Test
    public void testCaverphone_wordWithTch_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("2K11111111", caverphone.caverphone("tchai"));
    }

    // Tests handling of "dg" replacement to "2g"
    @Test
    public void testCaverphone_wordWithDg_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("2K11111111", caverphone.caverphone("edge"));
    }

    // Tests encode(Object) with non-String input throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_objectNonString_throwsEncoderException() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        caverphone.encode(Integer.valueOf(123));
    }

    // Tests encode(Object) with String input works correctly
    @Test
    public void testEncode_objectString_returnsCorrectCode() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        assertEquals("STFN111111", caverphone.encode((Object) "steven"));
    }

    // Tests encode(String) method
    @Test
    public void testEncode_string_returnsCorrectCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals("STFN111111", caverphone.encode("steven"));
    }

    // Tests isCaverphoneEqual with equal strings
    @Test
    public void testIsCaverphoneEqual_equalStrings_returnsTrue() {
        Caverphone caverphone = new Caverphone();
        assertTrue(caverphone.isCaverphoneEqual("steven", "steven"));
    }

    // Tests isCaverphoneEqual with different strings that have same Caverphone code
    @Test
    public void testIsCaverphoneEqual_differentStringsWithSameCode_returnsTrue() {
        Caverphone caverphone = new Caverphone();
        assertTrue(caverphone.isCaverphoneEqual("stephen", "steven"));
    }

    // Tests isCaverphoneEqual with strings having different codes
    @Test
    public void testIsCaverphoneEqual_differentCodes_returnsFalse() {
        Caverphone caverphone = new Caverphone();
        assertFalse(caverphone.isCaverphoneEqual("steven", "john"));
    }

    // ========== ส่วนที่เพิ่มใหม่สำหรับ coverage ที่ยังไม่ครบ ==========

    // Tests output length is always 10 characters
    @Test
    public void testCaverphone_outputLengthIsAlways10() {
        Caverphone caverphone = new Caverphone();
        assertEquals(10, caverphone.caverphone("a").length());
        assertEquals(10, caverphone.caverphone("abcdefghijklmnopqrstuvwxyz").length());
        assertEquals(10, caverphone.caverphone("1234567890").length());
    }

    // Tests handling of uppercase input
    @Test
    public void testCaverphone_uppercaseInput_returnsSameAsLowercase() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("steven"), caverphone.caverphone("STEVEN"));
        assertEquals(caverphone.caverphone("Cough"), caverphone.caverphone("cough"));
    }

    // Tests handling of mixed case input
    @Test
    public void testCaverphone_mixedCaseInput_returnsSameCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("steven"), caverphone.caverphone("SteVen"));
    }

    // Tests handling of "kn" start replacement
    @Test
    public void testCaverphone_wordStartingWithKn_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("N111111111", caverphone.caverphone("knight"));
    }

    // Tests handling of "dg" replacement with specific patterns
    @Test
    public void testCaverphone_wordWithDge_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("M211111111", caverphone.caverphone("dodge"));
    }

    // Tests handling of "gh" with different contexts after removal
    @Test
    public void testCaverphone_wordWithGh_silentRemoval() {
        Caverphone caverphone = new Caverphone();
        assertEquals("T111111111", caverphone.caverphone("tough"));
    }

    // Tests handling of "ph" replacement to "f"
    @Test
    public void testCaverphone_wordWithPh_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("SFN1111111", caverphone.caverphone("stephen"));
    }

    // Tests handling of "th" replacement to "0"
    @Test
    public void testCaverphone_wordWithTh_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("T011111111", caverphone.caverphone("math"));
    }

    // Tests handling of "sh" replacement to "2"
    @Test
    public void testCaverphone_wordWithSh_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("2111111111", caverphone.caverphone("ship"));
    }

    // Tests handling of "ss" replacement to "2"
    @Test
    public void testCaverphone_wordWithSs_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("P211111111", caverphone.caverphone("pass"));
    }

    // Tests handling of end of word "mb" to "m"
    @Test
    public void testCaverphone_wordEndingWithMb_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("K211111111", caverphone.caverphone("climb"));
    }

    // Tests handling of "que" replacement to "ke"
    @Test
    public void testCaverphone_wordWithQue_replacesCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("K111111111", caverphone.caverphone("queen"));
    }

    // Tests handling of multiple consecutive replacements
    @Test
    public void testCaverphone_multipleReplacementsWorkCorrectly() {
        Caverphone caverphone = new Caverphone();
        assertEquals("S2N1111111", caverphone.caverphone("smith"));
    }

    // Tests the final padding with '1' for short words
    @Test
    public void testCaverphone_shortWord_padsWithOnes() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("a");
        assertEquals(10, result.length());
        assertTrue(result.endsWith("111111111"));
    }

    // Tests that long words are truncated to 10 characters
    @Test
    public void testCaverphone_longWord_truncatesTo10Chars() {
        Caverphone caverphone = new Caverphone();
        assertEquals(10, caverphone.caverphone("abcdefghijklmnopqrstuvwxyz").length());
    }

    // Tests isCaverphoneEqual with one null argument
    @Test
    public void testIsCaverphoneEqual_oneNullArgument_returnsFalse() {
        Caverphone caverphone = new Caverphone();
        assertFalse(caverphone.isCaverphoneEqual(null, "steven"));
        assertFalse(caverphone.isCaverphoneEqual("steven", null));
    }

    // Tests isCaverphoneEqual with both null arguments
    @Test
    public void testIsCaverphoneEqual_bothNull_returnsTrue() {
        Caverphone caverphone = new Caverphone();
        assertTrue(caverphone.isCaverphoneEqual(null, null));
    }

    // Tests encode(Object) with null input
    @Test
    public void testEncode_objectNull_returnsDefaultCode() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.encode((Object) null));
    }

    // Tests encode(String) with null input
    @Test
    public void testEncode_stringNull_returnsDefaultCode() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.encode((String) null));
    }
}
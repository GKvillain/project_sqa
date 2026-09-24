package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link Caverphone}.
 */
public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    // Tests null input returns default ten ones
    @Test
    public void testCaverphone_nullInput_returnsDefaultCode() {
        assertEquals("1111111111", this.caverphone.caverphone(null));
    }

    // Tests empty string returns default ten ones
    @Test
    public void testCaverphone_emptyInput_returnsDefaultCode() {
        assertEquals("1111111111", this.caverphone.caverphone(""));
    }

    // Tests simple normal encoding
    @Test
    public void testCaverphone_simpleName_returnsEncodedValue() {
        assertEquals("PTA1111111", this.caverphone.caverphone("Peter"));
    }

    // Tests words with various start options (cough, rough, tough, gn)
    @Test
    public void testCaverphone_startOptions_returnsEncodedValue() {
        assertEquals("KFA1111111", this.caverphone.caverphone("cough"));
        assertEquals("RFA1111111", this.caverphone.caverphone("rough"));
        assertEquals("TFA1111111", this.caverphone.caverphone("tough"));
        assertEquals("N111111111", this.caverphone.caverphone("gnome"));
    }

    // Tests words ending with mb
    @Test
    public void testCaverphone_mbEnding_returnsEncodedValue() {
        assertEquals("TMA1111111", this.caverphone.caverphone("tomb"));
    }

    // Tests string containing non-alpha characters and uppercase letters
    @Test
    public void testCaverphone_nonAlphaCharacters_ignoresNonAlpha() {
        assertEquals("PTA1111111", this.caverphone.caverphone("Peter 123 !@#"));
    }

    // Tests words with trailing e removed in Caverphone 2.0
    @Test
    public void testCaverphone_trailingE_removesFinalE() {
        assertEquals(this.caverphone.caverphone("love"), this.caverphone.caverphone("lov"));
    }

    // Tests encode(Object) with valid String object
    @Test
    public void testEncode_validStringObject_returnsEncodedString() throws EncoderException {
        Object result = this.caverphone.encode((Object) "Stevenson");
        assertEquals("STFNSN1111", result);
    }

    // Tests encode(Object) with invalid non-String object throwing EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_nonStringObject_throwsEncoderException() throws EncoderException {
        this.caverphone.encode(Integer.valueOf(12345));
    }

    // Tests encode(String) method
    @Test
    public void testEncode_string_returnsEncodedString() {
        assertEquals("STFNSN1111", this.caverphone.encode("Stevenson"));
    }

    // Tests isCaverphoneEqual for equivalent sounding words
    @Test
    public void testIsCaverphoneEqual_equivalentWords_returnsTrue() {
        assertTrue(this.caverphone.isCaverphoneEqual("Lee", "Lie"));
        assertTrue(this.caverphone.isCaverphoneEqual("Peter", "Peiter"));
    }

    // Tests isCaverphoneEqual for different sounding words
    @Test
    public void testIsCaverphoneEqual_differentWords_returnsFalse() {
        assertFalse(this.caverphone.isCaverphoneEqual("Peter", "Paul"));
    }
}
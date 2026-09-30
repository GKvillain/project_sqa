package org.apache.commons.codec.net;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;

public class QuotedPrintableCodecTest {

    // Tests default charset is UTF-8
    @Test
    public void testDefaultConstructor_usesUtf8() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("UTF-8", codec.getDefaultCharset());
    }

    // Tests constructor with a given charset
    @Test
    public void testConstructorWithCharset_usesGivenCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
        assertEquals("ISO-8859-1", codec.getDefaultCharset());
    }

    // Tests encodeQuotedPrintable with null input returns null
    @Test
    public void testEncodeQuotedPrintable_nullBytes_returnsNull() {
        assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, null));
    }

    // Tests decodeQuotedPrintable with null input returns null
    @Test
    public void testDecodeQuotedPrintable_nullBytes_returnsNull() throws DecoderException {
        assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));
    }

    // Tests printable bytes are left unchanged
    @Test
    public void testEncodeQuotedPrintable_printableBytes_unchanged() {
        byte[] input = StringUtils.getBytesUsAscii("Hello, World!");
        assertArrayEquals(input, QuotedPrintableCodec.encodeQuotedPrintable(null, input));
    }

    // Tests non-printable byte is encoded as =XX
    @Test
    public void testEncodeQuotedPrintable_nonPrintableByte_encodesHex() {
        byte[] input = new byte[] {1};
        assertArrayEquals(StringUtils.getBytesUsAscii("=01"),
                          QuotedPrintableCodec.encodeQuotedPrintable(null, input));
    }

    // Tests high (negative) byte is treated as unsigned and encoded as =FF
    @Test
    public void testEncodeQuotedPrintable_highByte_encodesHex() {
        byte[] input = new byte[] {(byte) 0xFF};
        assertArrayEquals(StringUtils.getBytesUsAscii("=FF"),
                          QuotedPrintableCodec.encodeQuotedPrintable(null, input));
    }

    // Tests trailing whitespace must be encoded (RFC 1521 rule #3)
    @Test
    public void testEncodeQuotedPrintable_trailingWhitespace_encodes() {
        byte[] input = StringUtils.getBytesUsAscii("hello \t");
        byte[] expected = StringUtils.getBytesUsAscii("hello=20=09");
        assertArrayEquals(expected, QuotedPrintableCodec.encodeQuotedPrintable(null, input));
    }

    // Tests soft line break is inserted for long lines (RFC 1521 rule #5)
    @Test
    public void testEncodeQuotedPrintable_longLine_insertsSoftBreak() {
        StringBuilder sb = new StringBuilder(80);
        for (int i = 0; i < 80; i++) {
            sb.append('A');
        }
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null,
                               StringUtils.getBytesUsAscii(sb.toString()));
        assertTrue(StringUtils.newStringUsAscii(encoded).contains("=\r\n"));
    }

    // Tests decoding a simple escaped sequence
    @Test
    public void testDecodeQuotedPrintable_simpleEscape_decodes() throws DecoderException {
        byte[] input = StringUtils.getBytesUsAscii("=41");
        assertArrayEquals(StringUtils.getBytesUsAscii("A"),
                          QuotedPrintableCodec.decodeQuotedPrintable(input));
    }

    // Tests decoding multiple escaped sequences
    @Test
    public void testDecodeQuotedPrintable_multipleEscapes_decodes() throws DecoderException {
        byte[] input = StringUtils.getBytesUsAscii("=41=42");
        assertArrayEquals(StringUtils.getBytesUsAscii("AB"),
                          QuotedPrintableCodec.decodeQuotedPrintable(input));
    }

    // Tests invalid escape sequence throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecodeQuotedPrintable_invalidEscape_throwsDecoderException() throws DecoderException {
        QuotedPrintableCodec.decodeQuotedPrintable(StringUtils.getBytesUsAscii("="));
    }

    // Tests soft line break is ignored when decoding (RFC 1521)
    @Test
    public void testDecodeQuotedPrintable_softLineBreak_ignored() throws DecoderException {
        byte[] input = StringUtils.getBytesUsAscii("=\r\n");
        assertArrayEquals(new byte[0], QuotedPrintableCodec.decodeQuotedPrintable(input));
    }

    // Tests encode(String) with null input returns null
    @Test
    public void testEncodeString_null_returnsNull() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((String) null));
    }

    // Tests decode(String) with null input returns null
    @Test
    public void testDecodeString_null_returnsNull() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((String) null));
    }

    // Tests encode(String) with ASCII printable characters returns same string
    @Test
    public void testEncodeString_ascii_returnsSameString() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("ABCDE", codec.encode("ABCDE"));
    }

    // Tests encode(String) with non-ASCII character returns quoted-printable representation
    @Test
    public void testEncodeString_nonAscii_returnsEncodedString() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("=C3=A9", codec.encode("\u00E9"));
    }

    // Tests decode(String) with quoted-printable non-ASCII string returns original
    @Test
    public void testDecodeString_encodedNonAscii_returnsOriginalString() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("\u00E9", codec.decode("=C3=A9"));
    }

    // Tests encode(Object) with byte[] returns byte[]
    @Test
    public void testEncodeObject_byteArray_returnsByteArray() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = StringUtils.getBytesUsAscii("abc");
        Object result = codec.encode((Object) input);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(input, (byte[]) result);
    }

    // Tests decode(Object) with byte[] returns byte[]
    @Test
    public void testDecodeObject_byteArray_returnsByteArray() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = StringUtils.getBytesUsAscii("=61");
        Object result = codec.decode((Object) input);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(StringUtils.getBytesUsAscii("a"), (byte[]) result);
    }

    // Tests encode(Object) with unsupported type throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncodeObject_invalidType_throwsEncoderException() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode((Object) Integer.valueOf(1));
    }

    // Tests decode(Object) with unsupported type throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecodeObject_invalidType_throwsDecoderException() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode((Object) Integer.valueOf(1));
    }
}
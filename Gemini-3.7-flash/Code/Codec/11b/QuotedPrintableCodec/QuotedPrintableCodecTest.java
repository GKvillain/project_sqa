package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuotedPrintableCodecTest {

    // Tests default constructor and getDefaultCharset
    @Test
    public void testGetDefaultCharset_defaultConstructor_returnsUtf8() {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(CharEncoding.UTF_8, qpcodec.getDefaultCharset());
    }

    // Tests custom charset constructor
    @Test
    public void testGetDefaultCharset_customConstructor_returnsCustomCharset() {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec(CharEncoding.ISO_8859_1);
        assertEquals(CharEncoding.ISO_8859_1, qpcodec.getDefaultCharset());
    }

    // Tests encoding basic ASCII string
    @Test
    public void testEncode_plainAsciiString_returnsSameString() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "Hello World!";
        String encoded = qpcodec.encode(plain);
        assertEquals("Hello World!", encoded);
    }

    // Tests encoding unsafe characters
    @Test
    public void testEncode_unsafeCharacters_returnsQuotedPrintableString() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "Hello=World?~";
        String encoded = qpcodec.encode(plain);
        assertEquals("Hello=3DWorld=3F~", encoded);
    }

    // Tests decoding quoted-printable string
    @Test
    public void testDecode_quotedPrintableString_returnsOriginalString() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String encoded = "Hello=3DWorld=3F~";
        String decoded = qpcodec.decode(encoded);
        assertEquals("Hello=World?~", decoded);
    }

    // Tests encoding and decoding with specific charset
    @Test
    public void testEncodeDecode_withSpecificCharset_returnsOriginal() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "abc \t xyz";
        String encoded = qpcodec.encode(plain, CharEncoding.UTF_8);
        assertEquals("abc \t xyz", encoded);
        String decoded = qpcodec.decode(encoded, CharEncoding.UTF_8);
        assertEquals(plain, decoded);
    }

    // Tests static encodeQuotedPrintable and decodeQuotedPrintable with byte arrays
    @Test
    public void testEncodeDecodeQuotedPrintable_byteArrays_returnsOriginalBytes() throws Exception {
        byte[] input = new byte[] { (byte) 0, (byte) 1, (byte) 255, (byte) 'A', (byte) '=' };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        assertNotNull(encoded);
        byte[] decoded = QuotedPrintableCodec.decodeQuotedPrintable(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests static encodeQuotedPrintable with custom BitSet
    @Test
    public void testEncodeQuotedPrintable_customBitSet_escapesNonPrintable() {
        BitSet customPrintable = new BitSet();
        customPrintable.set('A');
        byte[] input = new byte[] { 'A', 'B' };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(customPrintable, input);
        assertEquals("A=42", new String(encoded));
    }

    // Tests null inputs for byte array methods
    @Test
    public void testEncodeDecode_nullByteArray_returnsNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertNull(qpcodec.encode((byte[]) null));
        assertNull(qpcodec.decode((byte[]) null));
        assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, null));
        assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));
    }

    // Tests null inputs for string methods
    @Test
    public void testEncodeDecode_nullString_returnsNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertNull(qpcodec.encode((String) null));
        assertNull(qpcodec.decode((String) null));
        assertNull(qpcodec.encode((String) null, CharEncoding.UTF_8));
        assertNull(qpcodec.decode((String) null, CharEncoding.UTF_8));
    }

    // Tests Object encode and decode with supported types
    @Test
    public void testEncodeDecode_objectTypes_returnsExpectedObject() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertNull(qpcodec.encode((Object) null));
        assertNull(qpcodec.decode((Object) null));

        String text = "Test String=";
        Object encodedString = qpcodec.encode((Object) text);
        assertTrue(encodedString instanceof String);
        Object decodedString = qpcodec.decode(encodedString);
        assertEquals(text, decodedString);

        byte[] bytes = text.getBytes(CharEncoding.UTF_8);
        Object encodedBytes = qpcodec.encode((Object) bytes);
        assertTrue(encodedBytes instanceof byte[]);
        Object decodedBytes = qpcodec.decode(encodedBytes);
        assertArrayEquals(bytes, (byte[]) decodedBytes);
    }

    // Tests Object encode with invalid type throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_invalidObjectType_throwsEncoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.encode(Integer.valueOf(123));
    }

    // Tests Object decode with invalid type throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_invalidObjectType_throwsDecoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.decode(Double.valueOf(12.34));
    }

    // Tests decode invalid truncated escape sequence throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_truncatedEscapeSequence_throwsDecoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.decode("ABC=1");
    }

    // Tests decode incomplete escape character at end throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_trailingEscapeChar_throwsDecoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.decode("ABC=");
    }

    // Tests decode invalid hex characters throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_invalidHexDigits_throwsDecoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.decode("ABC=ZZ");
    }

    // Tests decode with unsupported charset throws UnsupportedEncodingException
    @Test(expected = UnsupportedEncodingException.class)
    public void testDecode_unsupportedCharset_throwsUnsupportedEncodingException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.decode("ABC", "INVALID_CHARSET_NAME");
    }

    // Tests encode with unsupported charset throws UnsupportedEncodingException
    @Test(expected = UnsupportedEncodingException.class)
    public void testEncode_unsupportedCharset_throwsUnsupportedEncodingException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        qpcodec.encode("ABC", "INVALID_CHARSET_NAME");
    }

    // Tests encode with invalid default charset throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_invalidDefaultCharset_throwsEncoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec("INVALID_CHARSET_NAME");
        qpcodec.encode("ABC");
    }

    // Tests decode with invalid default charset throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_invalidDefaultCharset_throwsDecoderException() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec("INVALID_CHARSET_NAME");
        qpcodec.decode("ABC");
    }
}
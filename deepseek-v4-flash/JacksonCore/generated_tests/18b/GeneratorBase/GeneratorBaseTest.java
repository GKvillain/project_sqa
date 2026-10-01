package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import static org.junit.Assert.*;
import org.junit.Test;

public class GeneratorBaseTest {

    // -------------------------------------------------------
    // Inner test stub that implements all abstract methods of GeneratorBase
    // -------------------------------------------------------
    static class TestGenerator extends GeneratorBase {
        boolean writeNullCalled = false;
        String writeFieldNameValue = null;
        String writeStringValue = null;
        String writeRawValue = null;
        char[] writeRawChars = null;
        int rawOffset, rawLen;
        boolean verifyValueWriteInvoked = false;
        String verifyTypeMsg = null;
        int highestNonEscapedCharValue = -1;

        // Flags for newly covered methods
        boolean writeBooleanCalled = false;
        boolean writeBooleanValue = false;
        boolean writeNumberIntCalled = false;
        int writeNumberIntValue = 0;
        boolean writeNumberLongCalled = false;
        long writeNumberLongValue = 0L;
        boolean writeNumberDoubleCalled = false;
        double writeNumberDoubleValue = 0.0;
        boolean writeNumberFloatCalled = false;
        float writeNumberFloatValue = 0.0f;
        boolean writeNumberBigDecimalCalled = false;
        BigDecimal writeNumberBigDecimalValue = null;
        boolean writeStartArrayCalled = false;
        boolean writeEndArrayCalled = false;
        boolean writeStartObjectCalled = false;
        boolean writeEndObjectCalled = false;
        boolean flushCalled = false;
        boolean writeStringCharArrayCalled = false;
        char[] writeStringCharArrayText = null;
        int writeStringCharArrayOffset, writeStringCharArrayLen;
        boolean writeRawStringCalled = false;  // for writeRaw(String)
        boolean writeRawStringOffsetCalled = false; // for writeRaw(String,int,int)
        boolean writeRawCharArrayCalled = false; // for writeRaw(char[],int,int)

        public TestGenerator(int features, ObjectCodec codec) {
            super(features, codec);
        }

        @Override public void writeStartArray() throws IOException { writeStartArrayCalled = true; }
        @Override public void writeEndArray() throws IOException { writeEndArrayCalled = true; }
        @Override public void writeStartObject() throws IOException { writeStartObjectCalled = true; }
        @Override public void writeEndObject() throws IOException { writeEndObjectCalled = true; }
        @Override public void writeFieldName(String name) throws IOException { writeFieldNameValue = name; }
        @Override public void writeString(String text) throws IOException { writeStringValue = text; }
        @Override public void writeString(char[] text, int offset, int len) throws IOException {
            writeStringCharArrayCalled = true;
            writeStringCharArrayText = text;
            writeStringCharArrayOffset = offset;
            writeStringCharArrayLen = len;
        }
        @Override public void writeRaw(String text) throws IOException {
            writeRawValue = text;
            writeRawStringCalled = true;
        }
        @Override public void writeRaw(String text, int offset, int len) throws IOException {
            writeRawValue = text;
            rawOffset = offset;
            rawLen = len;
            writeRawStringOffsetCalled = true;
        }
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {
            writeRawChars = text;
            rawOffset = offset;
            rawLen = len;
            writeRawCharArrayCalled = true;
        }
        @Override public void writeNumber(int i) throws IOException {
            writeNumberIntCalled = true;
            writeNumberIntValue = i;
        }
        @Override public void writeNumber(long l) throws IOException {
            writeNumberLongCalled = true;
            writeNumberLongValue = l;
        }
        @Override public void writeNumber(double d) throws IOException {
            writeNumberDoubleCalled = true;
            writeNumberDoubleValue = d;
        }
        @Override public void writeNumber(float f) throws IOException {
            writeNumberFloatCalled = true;
            writeNumberFloatValue = f;
        }
        @Override public void writeNumber(BigDecimal dec) throws IOException {
            writeNumberBigDecimalCalled = true;
            writeNumberBigDecimalValue = dec;
        }
        @Override public void writeBoolean(boolean state) throws IOException {
            writeBooleanCalled = true;
            writeBooleanValue = state;
        }
        @Override public void writeNull() throws IOException { writeNullCalled = true; }
        @Override public void flush() throws IOException { flushCalled = true; }
        @Override protected void _releaseBuffers() { }
        @Override protected void _verifyValueWrite(String typeMsg) throws IOException {
            verifyValueWriteInvoked = true;
            verifyTypeMsg = typeMsg;
        }
        @Override
        public JsonGenerator setHighestNonEscapedChar(int c) {
            highestNonEscapedCharValue = c;
            return this;
        }
    }

    // -------------------------------------------------------
    // Tests
    // -------------------------------------------------------

    // Tests isEnabled both cases
    @Test
    public void testIsEnabled_returnsCorrectValue() {
        TestGenerator gen = new TestGenerator(0, null);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        gen._features = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    // Tests getFeatureMask
    @Test
    public void testGetFeatureMask_returnsFeatures() {
        int mask = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        assertEquals(mask, gen.getFeatureMask());
    }

    // Tests enable for WRITE_NUMBERS_AS_STRINGS (derived feature)
    @Test
    public void testEnable_derivedFeatureNumbersAsStrings_setsCfgNumbersAsStrings() {
        TestGenerator gen = new TestGenerator(0, null);
        gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(gen._cfgNumbersAsStrings);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    // Tests enable for ESCAPE_NON_ASCII (derived feature)
    @Test
    public void testEnable_derivedFeatureEscapeNonAscii_setsHighestNonEscapedChar() {
        TestGenerator gen = new TestGenerator(0, null);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertEquals(127, gen.highestNonEscapedCharValue);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    // Tests enable for STRICT_DUPLICATE_DETECTION (derived feature)
    @Test
    public void testEnable_derivedFeatureStrictDuplicateDetection_addsDupDetector() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNull(gen._writeContext.getDupDetector());
        gen.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertNotNull(gen._writeContext.getDupDetector());
        assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
    }

    // Tests disable for WRITE_NUMBERS_AS_STRINGS (derived feature)
    @Test
    public void testDisable_derivedFeatureNumbersAsStrings_clearsCfgNumbersAsStrings() {
        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        assertTrue(gen._cfgNumbersAsStrings);
        gen.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(gen._cfgNumbersAsStrings);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    // Tests disable for ESCAPE_NON_ASCII (derived feature)
    @Test
    public void testDisable_derivedFeatureEscapeNonAscii_setsHighestNonEscapedCharZero() {
        int mask = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        gen.highestNonEscapedCharValue = -1; // reset
        gen.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertEquals(0, gen.highestNonEscapedCharValue);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    // Tests disable for STRICT_DUPLICATE_DETECTION (derived feature)
    @Test
    public void testDisable_derivedFeatureStrictDuplicateDetection_removesDupDetector() {
        int mask = JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        assertNotNull(gen._writeContext.getDupDetector());
        gen.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertNull(gen._writeContext.getDupDetector());
        assertFalse(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
    }

    // Tests setFeatureMask with derived feature changes
    @Test
    public void testSetFeatureMask_derivedChanges_updatesSideEffects() {
        // Initial: no feature
        TestGenerator gen = new TestGenerator(0, null);
        // Change to WRITE_NUMBERS_AS_STRINGS + ESCAPE_NON_ASCII
        int newMask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        gen.setFeatureMask(newMask);
        assertTrue(gen._cfgNumbersAsStrings);
        assertEquals(127, gen.highestNonEscapedCharValue);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    // Tests overrideStdFeatures with derived feature changes
    @Test
    public void testOverrideStdFeatures_derivedChanges_updatesSideEffects() {
        TestGenerator gen = new TestGenerator(0, null);
        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        int values = mask; // enable both
        gen.overrideStdFeatures(values, mask);
        assertTrue(gen._cfgNumbersAsStrings);
        assertNotNull(gen._writeContext.getDupDetector());
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
    }

    // Tests useDefaultPrettyPrinter when no printer is set
    @Test
    public void testUseDefaultPrettyPrinter_withoutExisting_setsDefault() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNull(gen.getPrettyPrinter());
        gen.useDefaultPrettyPrinter();
        assertNotNull(gen.getPrettyPrinter());
        assertTrue(gen.getPrettyPrinter() instanceof DefaultPrettyPrinter);
    }

    // Tests useDefaultPrettyPrinter when a printer already exists (should not change)
    @Test
    public void testUseDefaultPrettyPrinter_withExisting_returnsThis() {
        TestGenerator gen = new TestGenerator(0, null);
        PrettyPrinter custom = new PrettyPrinter() {
            @Override public void writeStartObject(JsonGenerator gen) throws IOException { }
            @Override public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException { }
            @Override public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException { }
            @Override public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException { }
            @Override public void writeStartArray(JsonGenerator gen) throws IOException { }
            @Override public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException { }
            @Override public void writeArrayValueSeparator(JsonGenerator gen) throws IOException { }
            @Override public void beforeArrayValues(JsonGenerator gen) throws IOException { }
            @Override public void beforeObjectEntries(JsonGenerator gen) throws IOException { }
        };
        gen.setPrettyPrinter(custom);
        gen.useDefaultPrettyPrinter();
        assertSame(custom, gen.getPrettyPrinter());
    }

    // Tests writeBinary throws IOException
    @Test(expected = IOException.class)
    public void testWriteBinary_throwsException() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeBinary(null, (InputStream) null, 0);
    }

    // Tests _decodeSurrogate with valid pair
    @Test
    public void testDecodeSurrogate_validPair_returnsCorrectCodePoint() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        assertEquals(0x10000, result);
    }

    // Tests _decodeSurrogate with second surrogate below range -> IOException
    @Test(expected = IOException.class)
    public void testDecodeSurrogate_invalidSecondSurrogateLow_throwsException() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen._decodeSurrogate(0xD800, 0xDBFF);
    }

    // Tests _decodeSurrogate with second surrogate above range -> IOException
    @Test(expected = IOException.class)
    public void testDecodeSurrogate_invalidSecondSurrogateHigh_throwsException() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen._decodeSurrogate(0xD800, 0xE000);
    }

    // Tests close() and isClosed()
    @Test
    public void testCloseAndIsClosed() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        assertFalse(gen.isClosed());
        gen.close();
        assertTrue(gen.isClosed());
    }

    // Tests writeFieldName(SerializableString) delegation
    @Test
    public void testWriteFieldName_serializableString_delegates() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override public String getValue() { return "myField"; }
            @Override public int charLength() { return 7; }
            @Override public char[] asQuotedChars() { return null; }
            @Override public byte[] asUnquotedUTF8() { return null; }
            @Override public byte[] asQuotedUTF8() { return null; }
            @Override public byte[] asUTF8() { return null; }
            @Override public void appendQuotedUTF8(byte[] dest, int offset) { }
            @Override public void appendUnquotedUTF8(byte[] dest, int offset) { }
            @Override public int appendQuoted(char[] dest, int offset) { return 0; }
            @Override public int appendUnquoted(char[] dest, int offset) { return 0; }
            @Override public int writeQuotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int writeUnquotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int putQuotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
            @Override public int putUnquotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
        };
        gen.writeFieldName(ss);
        assertEquals("myField", gen.writeFieldNameValue);
    }

    // Tests writeString(SerializableString) delegation
    @Test
    public void testWriteString_serializableString_delegates() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override public String getValue() { return "myString"; }
            @Override public int charLength() { return 8; }
            @Override public char[] asQuotedChars() { return null; }
            @Override public byte[] asUnquotedUTF8() { return null; }
            @Override public byte[] asQuotedUTF8() { return null; }
            @Override public byte[] asUTF8() { return null; }
            @Override public void appendQuotedUTF8(byte[] dest, int offset) { }
            @Override public void appendUnquotedUTF8(byte[] dest, int offset) { }
            @Override public int appendQuoted(char[] dest, int offset) { return 0; }
            @Override public int appendUnquoted(char[] dest, int offset) { return 0; }
            @Override public int writeQuotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int writeUnquotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int putQuotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
            @Override public int putUnquotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
        };
        gen.writeString(ss);
        assertEquals("myString", gen.writeStringValue);
    }

    // Tests writeRawValue(String) calls _verifyValueWrite and delegates to writeRaw
    @Test
    public void testWriteRawValue_string_callsVerifyAndWriteRaw() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRawValue("testRaw");
        assertTrue(gen.verifyValueWriteInvoked);
        assertEquals("write raw value", gen.verifyTypeMsg);
        assertEquals("testRaw", gen.writeRawValue);
    }

    // Tests writeRawValue(SerializableString) calls _verifyValueWrite and delegates to writeRaw
    @Test
    public void testWriteRawValue_serializableString_callsVerifyAndWriteRaw() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override public String getValue() { return "serRaw"; }
            @Override public int charLength() { return 6; }
            @Override public char[] asQuotedChars() { return null; }
            @Override public byte[] asUnquotedUTF8() { return null; }
            @Override public byte[] asQuotedUTF8() { return null; }
            @Override public byte[] asUTF8() { return null; }
            @Override public void appendQuotedUTF8(byte[] dest, int offset) { }
            @Override public void appendUnquotedUTF8(byte[] dest, int offset) { }
            @Override public int appendQuoted(char[] dest, int offset) { return 0; }
            @Override public int appendUnquoted(char[] dest, int offset) { return 0; }
            @Override public int writeQuotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int writeUnquotedUTF8(OutputStream out) throws IOException { return 0; }
            @Override public int putQuotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
            @Override public int putUnquotedUTF8(ByteArrayBuilder out) throws IOException { return 0; }
        };
        gen.writeRawValue(ss);
        assertTrue(gen.verifyValueWriteInvoked);
        assertEquals("write raw value", gen.verifyTypeMsg);
        assertEquals("serRaw", gen.writeRawValue);
    }

    // Tests writeObject(null) calls writeNull
    @Test
    public void testWriteObject_null_callsWriteNull() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeObject(null);
        assertTrue(gen.writeNullCalled);
    }

    // Tests writeTree(null) calls writeNull
    @Test
    public void testWriteTree_null_callsWriteNull() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeTree(null);
        assertTrue(gen.writeNullCalled);
    }

    // Tests _asString with null BigDecimal throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testAsString_nullInput_throwsNullPointerException() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen._asString(null);
    }

    // ---------- New tests for uncovered methods ----------

    // writeBoolean
    @Test
    public void testWriteBoolean_true() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeBoolean(true);
        assertTrue(gen.writeBooleanCalled);
        assertTrue(gen.writeBooleanValue);
    }

    @Test
    public void testWriteBoolean_false() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeBoolean(false);
        assertTrue(gen.writeBooleanCalled);
        assertFalse(gen.writeBooleanValue);
    }

    // writeNumber overloads
    @Test
    public void testWriteNumber_int() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeNumber(42);
        assertTrue(gen.writeNumberIntCalled);
        assertEquals(42, gen.writeNumberIntValue);
    }

    @Test
    public void testWriteNumber_long() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeNumber(123L);
        assertTrue(gen.writeNumberLongCalled);
        assertEquals(123L, gen.writeNumberLongValue);
    }

    @Test
    public void testWriteNumber_double() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeNumber(3.14);
        assertTrue(gen.writeNumberDoubleCalled);
        assertEquals(3.14, gen.writeNumberDoubleValue, 0.0);
    }

    @Test
    public void testWriteNumber_float() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeNumber(2.5f);
        assertTrue(gen.writeNumberFloatCalled);
        assertEquals(2.5f, gen.writeNumberFloatValue, 0.0f);
    }

    @Test
    public void testWriteNumber_BigDecimal() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        BigDecimal bd = new BigDecimal("10.5");
        gen.writeNumber(bd);
        assertTrue(gen.writeNumberBigDecimalCalled);
        assertEquals(bd, gen.writeNumberBigDecimalValue);
    }

    // writeString(char[], int, int)
    @Test
    public void testWriteString_charArray() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        char[] chars = "hello".toCharArray();
        gen.writeString(chars, 1, 3);
        assertTrue(gen.writeStringCharArrayCalled);
        assertSame(chars, gen.writeStringCharArrayText);
        assertEquals(1, gen.writeStringCharArrayOffset);
        assertEquals(3, gen.writeStringCharArrayLen);
    }

    // writeRaw(String)
    @Test
    public void testWriteRaw_string() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRaw("rawString");
        assertTrue(gen.writeRawStringCalled);
        assertEquals("rawString", gen.writeRawValue);
    }

    // writeRaw(String, int, int)
    @Test
    public void testWriteRaw_stringWithOffset() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRaw("raw", 1, 2);
        assertTrue(gen.writeRawStringOffsetCalled);
        assertEquals("raw", gen.writeRawValue);
        assertEquals(1, gen.rawOffset);
        assertEquals(2, gen.rawLen);
    }

    // writeRaw(char[], int, int)
    @Test
    public void testWriteRaw_charArray() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        char[] chars = "test".toCharArray();
        gen.writeRaw(chars, 0, 4);
        assertTrue(gen.writeRawCharArrayCalled);
        assertSame(chars, gen.writeRawChars);
        assertEquals(0, gen.rawOffset);
        assertEquals(4, gen.rawLen);
    }

    // writeStartArray
    @Test
    public void testWriteStartArray() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeStartArray();
        assertTrue(gen.writeStartArrayCalled);
    }

    // writeEndArray
    @Test
    public void testWriteEndArray() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeEndArray();
        assertTrue(gen.writeEndArrayCalled);
    }

    // writeStartObject
    @Test
    public void testWriteStartObject() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeStartObject();
        assertTrue(gen.writeStartObjectCalled);
    }

    // writeEndObject
    @Test
    public void testWriteEndObject() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeEndObject();
        assertTrue(gen.writeEndObjectCalled);
    }

    // flush
    @Test
    public void testFlush() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.flush();
        assertTrue(gen.flushCalled);
    }
}
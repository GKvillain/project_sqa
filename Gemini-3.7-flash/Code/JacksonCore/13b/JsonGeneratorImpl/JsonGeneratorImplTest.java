package com.fasterxml.jackson.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.BufferRecycler;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonGeneratorImplTest {

    private IOContext _ioContext;

    @Before
    public void setUp() {
        _ioContext = new IOContext(new BufferRecycler(), "test", false);
    }

    // Concrete test implementation of abstract JsonGeneratorImpl
    private static class TestJsonGeneratorImpl extends JsonGeneratorImpl {
        String lastFieldName;
        String lastStringValue;

        public TestJsonGeneratorImpl(IOContext ctxt, int features, ObjectCodec codec) {
            super(ctxt, features, codec);
        }

        public boolean isUnquotedNames() {
            return _cfgUnqNames;
        }

        public int[] getOutputEscapes() {
            return _outputEscapes;
        }

        public SerializableString getRootValueSeparator() {
            return _rootValueSeparator;
        }

        public void triggerCheckStdFeatureChanges(int newFeatureFlags, int changedFeatures) {
            _checkStdFeatureChanges(newFeatureFlags, changedFeatures);
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            this.lastFieldName = name;
        }

        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            this.lastFieldName = name.getValue();
        }

        @Override
        public void writeString(String text) throws IOException {
            this.lastStringValue = text;
        }

        @Override
        public void writeString(char[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeString(SerializableString text) throws IOException {
            this.lastStringValue = text.getValue();
        }

        @Override
        public void writeRawUTF8String(byte[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeUTF8String(byte[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(String text) throws IOException {}

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char c) throws IOException {}

        @Override
        public void writeRaw(SerializableString text) throws IOException {}

        @Override
        public void writeRawValue(String text) throws IOException {}

        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}

        @Override
        public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
            return 0;
        }

        @Override
        public void writeNumber(int v) throws IOException {}

        @Override
        public void writeNumber(long v) throws IOException {}

        @Override
        public void writeNumber(BigInteger v) throws IOException {}

        @Override
        public void writeNumber(double v) throws IOException {}

        @Override
        public void writeNumber(float v) throws IOException {}

        @Override
        public void writeNumber(BigDecimal v) throws IOException {}

        @Override
        public void writeNumber(String encodedValue) throws IOException {}

        @Override
        public void writeBoolean(boolean state) throws IOException {}

        @Override
        public void writeNull() throws IOException {}

        @Override
        public void writeStartArray() throws IOException {}

        @Override
        public void writeEndArray() throws IOException {}

        @Override
        public void writeStartObject() throws IOException {}

        @Override
        public void writeEndObject() throws IOException {}

        @Override
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {}

        @Override
        protected void _releaseBuffers() {}

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {}
    }

    private static class CustomCharacterEscapes extends CharacterEscapes {
        private final int[] _ascii = CharacterEscapes.sStandardAsciiEscapesForJSON();

        @Override
        public int[] getEscapeCodesForAscii() {
            return _ascii;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    }

    // Tests constructor with ESCAPE_NON_ASCII feature enabled
    @Test
    public void testConstructor_escapeNonAsciiEnabled_setsHighestNonEscapedChar() {
        int features = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, features, null);
        assertEquals(127, gen.getHighestEscapedChar());
    }

    // Tests constructor with ESCAPE_NON_ASCII feature disabled
    @Test
    public void testConstructor_escapeNonAsciiDisabled_highestNonEscapedCharIsZero() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertEquals(0, gen.getHighestEscapedChar());
    }

    // Tests constructor with QUOTE_FIELD_NAMES enabled
    @Test
    public void testConstructor_quoteFieldNamesEnabled_cfgUnqNamesIsFalse() {
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, features, null);
        assertFalse(gen.isUnquotedNames());
    }

    // Tests constructor with QUOTE_FIELD_NAMES disabled
    @Test
    public void testConstructor_quoteFieldNamesDisabled_cfgUnqNamesIsTrue() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertTrue(gen.isUnquotedNames());
    }

    // Tests enable QUOTE_FIELD_NAMES updates _cfgUnqNames flag
    @Test
    public void testEnable_quoteFieldNames_setsUnquotedNamesFalse() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertTrue(gen.isUnquotedNames());

        gen.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(gen.isUnquotedNames());
    }

    // Tests enable feature other than QUOTE_FIELD_NAMES
    @Test
    public void testEnable_otherFeature_doesNotModifyUnquotedNames() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertTrue(gen.isUnquotedNames());

        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(gen.isUnquotedNames());
        assertTrue(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    // Tests disable QUOTE_FIELD_NAMES feature directly
    @Test
    public void testDisable_quoteFieldNames_setsUnquotedNamesTrue() {
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, features, null);
        assertFalse(gen.isUnquotedNames());

        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(gen.isUnquotedNames());
    }

    // Tests enable ESCAPE_NON_ASCII feature directly
    @Test
    public void testEnable_escapeNonAscii_setsHighestEscapedCharTo127() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertEquals(0, gen.getHighestEscapedChar());

        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertEquals(127, gen.getHighestEscapedChar());
    }

    // Tests disable ESCAPE_NON_ASCII feature directly
    @Test
    public void testDisable_escapeNonAscii_setsHighestEscapedCharToZero() {
        int features = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, features, null);
        assertEquals(127, gen.getHighestEscapedChar());

        gen.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertEquals(0, gen.getHighestEscapedChar());
    }

    // Tests _checkStdFeatureChanges with QUOTE_FIELD_NAMES enabled
    @Test
    public void testCheckStdFeatureChanges_quoteFieldNamesEnabled_setsUnquotedNamesFalse() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        int newFeatures = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        gen.triggerCheckStdFeatureChanges(newFeatures, newFeatures);
        assertFalse(gen.isUnquotedNames());
    }

    // Tests _checkStdFeatureChanges with QUOTE_FIELD_NAMES disabled
    @Test
    public void testCheckStdFeatureChanges_quoteFieldNamesDisabled_setsUnquotedNamesTrue() {
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, features, null);
        gen.triggerCheckStdFeatureChanges(0, features);
        assertTrue(gen.isUnquotedNames());
    }

    // Tests _checkStdFeatureChanges with ESCAPE_NON_ASCII enabled
    @Test
    public void testCheckStdFeatureChanges_escapeNonAsciiEnabled_setsHighestEscapedCharTo127() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        int feature = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        gen.triggerCheckStdFeatureChanges(feature, feature);
        assertEquals(127, gen.getHighestEscapedChar());
    }

    // Tests _checkStdFeatureChanges with ESCAPE_NON_ASCII disabled
    @Test
    public void testCheckStdFeatureChanges_escapeNonAsciiDisabled_setsHighestEscapedCharToZero() {
        int feature = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, feature, null);
        assertEquals(127, gen.getHighestEscapedChar());

        gen.triggerCheckStdFeatureChanges(0, feature);
        assertEquals(0, gen.getHighestEscapedChar());
    }

    // Tests setHighestNonEscapedChar with positive value
    @Test
    public void testSetHighestNonEscapedChar_positiveValue_setsMaximumNonEscapedChar() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        gen.setHighestNonEscapedChar(255);
        assertEquals(255, gen.getHighestEscapedChar());
    }

    // Tests setHighestNonEscapedChar with zero value
    @Test
    public void testSetHighestNonEscapedChar_zeroValue_setsZero() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        gen.setHighestNonEscapedChar(0);
        assertEquals(0, gen.getHighestEscapedChar());
    }

    // Tests setHighestNonEscapedChar with negative value resetting to zero
    @Test
    public void testSetHighestNonEscapedChar_negativeValue_setsZero() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        gen.setHighestNonEscapedChar(-100);
        assertEquals(0, gen.getHighestEscapedChar());
    }

    // Tests setCharacterEscapes with custom escapes
    @Test
    public void testSetCharacterEscapes_customEscapes_setsCharacterEscapesAndOutputEscapes() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        CharacterEscapes customEscapes = new CustomCharacterEscapes();

        gen.setCharacterEscapes(customEscapes);
        assertSame(customEscapes, gen.getCharacterEscapes());
        assertSame(customEscapes.getEscapeCodesForAscii(), gen.getOutputEscapes());
    }

    // Tests setCharacterEscapes with null resetting to default escapes
    @Test
    public void testSetCharacterEscapes_nullEscapes_revertsToDefaultEscapes() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        CharacterEscapes customEscapes = new CustomCharacterEscapes();
        gen.setCharacterEscapes(customEscapes);

        gen.setCharacterEscapes(null);
        assertNull(gen.getCharacterEscapes());
        assertSame(JsonGeneratorImpl.sOutputEscapes, gen.getOutputEscapes());
    }

    // Tests default root value separator
    @Test
    public void testRootValueSeparator_default_returnsDefaultSeparator() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        assertEquals(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, gen.getRootValueSeparator());
    }

    // Tests setRootValueSeparator with custom and null values
    @Test
    public void testSetRootValueSeparator_customAndNull_setsSeparatorCorrectly() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        SerializableString customSep = new SerializedString("/");

        gen.setRootValueSeparator(customSep);
        assertSame(customSep, gen.getRootValueSeparator());

        gen.setRootValueSeparator(null);
        assertNull(gen.getRootValueSeparator());
    }

    // Tests version method returns valid Version object
    @Test
    public void testVersion_returnsNonNullVersion() {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        Version v = gen.version();
        assertNotNull(v);
    }

    // Tests writeStringField delegates to writeFieldName and writeString
    @Test
    public void testWriteStringField_validStrings_writesFieldNameAndStringValue() throws IOException {
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(_ioContext, 0, null);
        gen.writeStringField("testField", "testValue");
        assertEquals("testField", gen.lastFieldName);
        assertEquals("testValue", gen.lastStringValue);
    }
}
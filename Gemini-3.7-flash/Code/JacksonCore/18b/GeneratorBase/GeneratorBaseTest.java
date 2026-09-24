package com.fasterxml.jackson.core.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonWriteContext;

public class GeneratorBaseTest {

    private static class TestGenerator extends GeneratorBase {
        public String lastRawWritten;
        public String lastStringWritten;
        public String lastFieldNameWritten;
        public String lastVerifiedType;
        public int highestNonEscapedChar = -1;
        public PrettyPrinter assignedPrettyPrinter;

        public TestGenerator(int features, ObjectCodec codec) {
            super(features, codec);
        }

        public TestGenerator(int features, ObjectCodec codec, JsonWriteContext ctxt) {
            super(features, codec, ctxt);
        }

        @Override
        public JsonGenerator setHighestNonEscapedChar(int charCode) {
            this.highestNonEscapedChar = charCode;
            return this;
        }

        @Override
        public int getHighestNonEscapedChar() {
            return this.highestNonEscapedChar;
        }

        @Override
        public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
            this.assignedPrettyPrinter = pp;
            return this;
        }

        @Override
        public PrettyPrinter getPrettyPrinter() {
            return this.assignedPrettyPrinter;
        }

        @Override
        public void writeStartArray() throws IOException {}

        @Override
        public void writeEndArray() throws IOException {}

        @Override
        public void writeStartObject() throws IOException {}

        @Override
        public void writeEndObject() throws IOException {}

        @Override
        public void writeFieldName(String name) throws IOException {
            this.lastFieldNameWritten = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            this.lastStringWritten = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            this.lastStringWritten = new String(text, offset, len);
        }

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeRaw(String text) throws IOException {
            this.lastRawWritten = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            this.lastRawWritten = text.substring(offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            this.lastRawWritten = new String(text, offset, len);
        }

        @Override
        public void writeRaw(char c) throws IOException {
            this.lastRawWritten = String.valueOf(c);
        }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}

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
        public void writeNull() throws IOException {
            this.lastStringWritten = "null";
        }

        @Override
        public void flush() throws IOException {}

        @Override
        protected void _releaseBuffers() {}

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            this.lastVerifiedType = typeMsg;
        }
    }

    private TestGenerator generator;

    @Before
    public void setUp() {
        generator = new TestGenerator(0, null);
    }

    // Tests initial feature state and feature query
    @Test
    public void testIsEnabled_initialFeatures_returnsExpectedState() {
        assertFalse(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(generator.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        assertFalse(generator.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertEquals(0, generator.getFeatureMask());
    }

    // Tests enable feature for derived features
    @Test
    public void testEnable_derivedFeatures_updatesDerivedState() {
        generator.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(generator._cfgNumbersAsStrings);

        generator.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        assertEquals(127, generator.getHighestNonEscapedChar());

        generator.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(generator.getOutputContext().getDupDetector());
    }

    // Tests disable feature for derived features
    @Test
    public void testDisable_derivedFeatures_clearsDerivedState() {
        int initialMask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()
                | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestGenerator gen = new TestGenerator(initialMask, null);

        assertTrue(gen._cfgNumbersAsStrings);
        assertNotNull(gen.getOutputContext().getDupDetector());

        gen.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(gen._cfgNumbersAsStrings);

        gen.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertEquals(0, gen.getHighestNonEscapedChar());

        gen.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertNull(gen.getOutputContext().getDupDetector());
    }

    // Tests setFeatureMask updating configuration
    @Test
    public void testSetFeatureMask_validMask_updatesFeaturesAndDerivedFlags() {
        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        generator.setFeatureMask(mask);

        assertEquals(mask, generator.getFeatureMask());
        assertTrue(generator._cfgNumbersAsStrings);
        assertEquals(127, generator.getHighestNonEscapedChar());

        generator.setFeatureMask(0);
        assertEquals(0, generator.getFeatureMask());
        assertFalse(generator._cfgNumbersAsStrings);
        assertEquals(0, generator.getHighestNonEscapedChar());
    }

    // Tests overrideStdFeatures with bitmask
    @Test
    public void testOverrideStdFeatures_changingFlags_appliesCorrectChanges() {
        int mask = JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        generator.overrideStdFeatures(mask, mask);

        assertTrue(generator.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(generator.getOutputContext().getDupDetector());

        generator.overrideStdFeatures(0, mask);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(generator.getOutputContext().getDupDetector());
    }

    // Tests current value accessors via JsonWriteContext
    @Test
    public void testCurrentValue_getterAndSetter_retainsValue() {
        Object testObj = new Object();
        generator.setCurrentValue(testObj);
        assertSame(testObj, generator.getCurrentValue());
    }

    // Tests default pretty printer configuration
    @Test
    public void testUseDefaultPrettyPrinter_notSet_setsDefaultPrinter() {
        assertNull(generator.getPrettyPrinter());
        generator.useDefaultPrettyPrinter();
        assertNotNull(generator.getPrettyPrinter());

        PrettyPrinter currentPrinter = generator.getPrettyPrinter();
        generator.useDefaultPrettyPrinter();
        assertSame(currentPrinter, generator.getPrettyPrinter());
    }

    // Tests codec getter and setter
    @Test
    public void testSetCodec_customCodec_returnsAssignedCodec() {
        assertNull(generator.getCodec());
        ObjectCodec dummyCodec = new ObjectCodec() {
            @Override
            public Version version() { return null; }
            @Override
            public <T extends TreeNode> T readTree(com.fasterxml.jackson.core.JsonParser p) throws IOException { return null; }
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) throws IOException {}
            @Override
            public <T> T readValue(com.fasterxml.jackson.core.JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override
            public <T> T readValue(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) throws IOException { return null; }
            @Override
            public <T> T readValue(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) throws IOException { return null; }
            @Override
            public <T> java.util.Iterator<T> readValues(com.fasterxml.jackson.core.JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override
            public <T> java.util.Iterator<T> readValues(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) throws IOException { return null; }
            @Override
            public <T> java.util.Iterator<T> readValues(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) throws IOException { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) throws IOException {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public com.fasterxml.jackson.core.JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonGenerationException { return null; }
        };
        generator.setCodec(dummyCodec);
        assertSame(dummyCodec, generator.getCodec());
    }

    // Tests writeFieldName and writeString with SerializableString
    @Test
    public void testWriteSerializableString_validInput_delegatesToStringMethods() throws IOException {
        SerializableString fieldName = new SerializedString("testField");
        generator.writeFieldName(fieldName);
        assertEquals("testField", generator.lastFieldNameWritten);

        SerializableString stringVal = new SerializedString("testValue");
        generator.writeString(stringVal);
        assertEquals("testValue", generator.lastStringWritten);
    }

    // Tests writeRawValue variants
    @Test
    public void testWriteRawValue_variousInputs_verifiesAndWritesRaw() throws IOException {
        generator.writeRawValue("raw1");
        assertEquals("write raw value", generator.lastVerifiedType);
        assertEquals("raw1", generator.lastRawWritten);

        generator.writeRawValue("prefix_raw2_suffix", 7, 4);
        assertEquals("raw2", generator.lastRawWritten);

        char[] charArr = "hello_world".toCharArray();
        generator.writeRawValue(charArr, 6, 5);
        assertEquals("world", generator.lastRawWritten);

        SerializableString rawSer = new SerializedString("rawSerializable");
        generator.writeRawValue(rawSer);
        assertEquals("rawSerializable", generator.lastRawWritten);
    }

    // Tests writeObject null value
    @Test
    public void testWriteObject_nullValue_writesNull() throws IOException {
        generator.writeObject(null);
        assertEquals("null", generator.lastStringWritten);
    }

    // Tests writeTree null node
    @Test
    public void testWriteTree_nullNode_writesNull() throws IOException {
        generator.writeTree(null);
        assertEquals("null", generator.lastStringWritten);
    }

    // Tests writeTree with missing ObjectCodec throws exception
    @Test(expected = IllegalStateException.class)
    public void testWriteTree_nonNullNodeWithoutCodec_throwsIllegalStateException() throws IOException {
        TreeNode mockNode = new TreeNode() {
            @Override public com.fasterxml.jackson.core.JsonToken asToken() { return null; }
            @Override public com.fasterxml.jackson.core.JsonParser.NumberType numberType() { return null; }
            @Override public int size() { return 0; }
            @Override public boolean isValueNode() { return false; }
            @Override public boolean isContainerNode() { return false; }
            @Override public boolean isMissingNode() { return false; }
            @Override public boolean isArray() { return false; }
            @Override public boolean isObject() { return false; }
            @Override public TreeNode get(String fieldName) { return null; }
            @Override public TreeNode get(int index) { return null; }
            @Override public TreeNode path(String fieldName) { return null; }
            @Override public TreeNode path(int index) { return null; }
            @Override public TreeNode path(int index, TreeNode missing) { return null; }
            @Override public TreeNode path(String fieldName, TreeNode missing) { return null; }
            @Override public com.fasterxml.jackson.core.JsonParser traverse() { return null; }
            @Override public com.fasterxml.jackson.core.JsonParser traverse(ObjectCodec codec) { return null; }
        };
        generator.writeTree(mockNode);
    }

    // Tests close and isClosed state
    @Test
    public void testClose_whenCalled_marksClosed() throws IOException {
        assertFalse(generator.isClosed());
        generator.close();
        assertTrue(generator.isClosed());
    }

    // Tests _asString for BigDecimal serialization
    @Test
    public void testAsString_bigDecimal_returnsStringRepresentation() throws IOException {
        BigDecimal dec = new BigDecimal("123.456");
        assertEquals("123.456", generator._asString(dec));
    }

    // Tests _decodeSurrogate with valid surrogate pair
    @Test
    public void testDecodeSurrogate_validSurrogates_returnsDecodedCodepoint() throws IOException {
        int surr1 = 0xD83D;
        int surr2 = 0xDE00;
        int expectedCodePoint = 0x1F600;
        int actual = generator._decodeSurrogate(surr1, surr2);
        assertEquals(expectedCodePoint, actual);
    }

    // Tests _decodeSurrogate with invalid second surrogate throws exception
    @Test(expected = JsonGenerationException.class)
    public void testDecodeSurrogate_invalidSecondSurrogate_throwsException() throws IOException {
        generator._decodeSurrogate(0xD800, 0xDB00);
    }

    // Tests writeBinary stream unsupported operation
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteBinary_inputStream_throwsUnsupportedOperationException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        generator.writeBinary(null, in, 3);
    }

    // Tests secondary constructor passing custom context
    @Test
    public void testConstructor_customContext_setsCorrectContext() {
        JsonWriteContext customCtxt = JsonWriteContext.createRootContext(null);
        TestGenerator gen = new TestGenerator(0, null, customCtxt);
        assertSame(customCtxt, gen.getOutputContext());
    }
}
package com.fasterxml.jackson.core;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonGeneratorTest {

    // Test Feature enum
    @Test
    public void testFeatureCollectDefaults_returnsCorrectMask() {
        int defaults = JsonGenerator.Feature.collectDefaults();
        assertTrue((defaults & JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask()) != 0);
        assertTrue((defaults & JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask()) != 0);
        assertTrue((defaults & JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.getMask()) != 0);
        assertTrue((defaults & JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask()) != 0);
        assertTrue((defaults & JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask()) != 0);
        assertFalse((defaults & JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()) != 0);
        assertFalse((defaults & JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()) != 0);
        assertFalse((defaults & JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask()) != 0);
        assertFalse((defaults & JsonGenerator.Feature.IGNORE_UNKNOWN.getMask()) != 0);
    }

    @Test
    public void testFeatureEnabledByDefault_returnsTrueForQuoteFieldNames() {
        assertTrue(JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledByDefault());
    }

    @Test
    public void testFeatureEnabledIn_EnabledFlag_ReturnsTrue() {
        int flags = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        assertTrue(JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledIn(flags));
        assertFalse(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(0));
    }

    @Test
    public void testConfigure_EnableFeature_ReturnsSameGenerator() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        assertSame(gen, gen.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true));
        assertSame(gen, gen.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false));
        gen.close();
    }

    @Test
    public void testOverrideStdFeatures_UpdatesFeatureMask() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        int mask = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        gen.overrideStdFeatures(0, mask); // disable QUOTE_NON_NUMERIC_NUMBERS
        assertFalse(gen.isEnabled(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS));
        gen.close();
    }

    // Tests for writeArray methods - use real generator
    @Test
    public void testWriteArray_intArrayNormal_ProducesJsonArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        int[] array = {1, 2, 3};
        gen.writeArray(array, 0, array.length);
        gen.close();
        String json = out.toString("UTF-8");
        assertEquals("[1,2,3]", json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArray_intArrayNull_throwsIllegalArgumentException() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        int[] array = null;
        gen.writeArray(array, 0, 0);
        gen.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArray_intArrayInvalidOffset_throwsIllegalArgumentException() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        int[] array = {1, 2};
        gen.writeArray(array, -1, 1);
        gen.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArray_intArrayLengthExceeds_throwsIllegalArgumentException() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        int[] array = {1, 2};
        gen.writeArray(array, 1, 2);
        gen.close();
    }

    @Test
    public void testWriteArray_longArrayNormal_ProducesJsonArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        long[] array = {10L, 20L};
        gen.writeArray(array, 0, array.length);
        gen.close();
        String json = out.toString("UTF-8");
        assertEquals("[10,20]", json);
    }

    @Test
    public void testWriteArray_doubleArrayNormal_ProducesJsonArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        double[] array = {1.5, 2.5};
        gen.writeArray(array, 0, array.length);
        gen.close();
        String json = out.toString("UTF-8");
        assertEquals("[1.5,2.5]", json);
    }

    // Test delegation: writeNumber(short) should call writeNumber(int)
    @Test
    public void testWriteNumber_shortValue_callsWriteNumberInt() throws Exception {
        final int[] captured = new int[1];
        JsonGenerator stub = new JsonGenerator() {
            @Override public JsonGenerator setCodec(ObjectCodec oc) { return this; }
            @Override public ObjectCodec getCodec() { return null; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public JsonGenerator enable(Feature f) { return this; }
            @Override public JsonGenerator disable(Feature f) { return this; }
            @Override public boolean isEnabled(Feature f) { return true; }
            @Override public int getFeatureMask() { return Feature.collectDefaults(); }
            @Override @Deprecated public JsonGenerator setFeatureMask(int values) { return this; }
            @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
            @Override public void writeStartArray() throws IOException {}
            @Override public void writeEndArray() throws IOException {}
            @Override public void writeStartObject() throws IOException {}
            @Override public void writeEndObject() throws IOException {}
            @Override public void writeFieldName(String name) throws IOException {}
            @Override public void writeFieldName(SerializableString name) throws IOException {}
            @Override public void writeString(String text) throws IOException {}
            @Override public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override public void writeString(SerializableString text) throws IOException {}
            @Override public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeRaw(String text) throws IOException {}
            @Override public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char c) throws IOException {}
            @Override public void writeRawValue(String text) throws IOException {}
            @Override public void writeRawValue(String text, int offset, int len) throws IOException {}
            @Override public void writeRawValue(char[] text, int offset, int len) throws IOException {}
            @Override public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException { return 0; }
            @Override public void writeNumber(int v) throws IOException { captured[0] = v; }
            @Override public void writeNumber(long v) throws IOException {}
            @Override public void writeNumber(BigInteger v) throws IOException {}
            @Override public void writeNumber(double v) throws IOException {}
            @Override public void writeNumber(float v) throws IOException {}
            @Override public void writeNumber(BigDecimal v) throws IOException {}
            @Override public void writeNumber(String encodedValue) throws IOException {}
            @Override public void writeBoolean(boolean state) throws IOException {}
            @Override public void writeNull() throws IOException {}
            @Override public void writeObject(Object pojo) throws IOException {}
            @Override public void writeTree(TreeNode rootNode) throws IOException {}
            @Override public JsonStreamContext getOutputContext() { return null; }
            @Override public void flush() throws IOException {}
            @Override public boolean isClosed() { return false; }
            @Override public void close() throws IOException {}
        };
        stub.writeNumber((short) 42);
        assertEquals(42, captured[0]);
    }

    // Test delegation: writeRaw(SerializableString) should call writeRaw(String)
    @Test
    public void testWriteRaw_SerializableString_callsWriteRawString() throws Exception {
        final String[] captured = new String[1];
        JsonGenerator stub = new JsonGenerator() {
            @Override public JsonGenerator setCodec(ObjectCodec oc) { return this; }
            @Override public ObjectCodec getCodec() { return null; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public JsonGenerator enable(Feature f) { return this; }
            @Override public JsonGenerator disable(Feature f) { return this; }
            @Override public boolean isEnabled(Feature f) { return true; }
            @Override public int getFeatureMask() { return Feature.collectDefaults(); }
            @Override @Deprecated public JsonGenerator setFeatureMask(int values) { return this; }
            @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
            @Override public void writeStartArray() throws IOException {}
            @Override public void writeEndArray() throws IOException {}
            @Override public void writeStartObject() throws IOException {}
            @Override public void writeEndObject() throws IOException {}
            @Override public void writeFieldName(String name) throws IOException {}
            @Override public void writeFieldName(SerializableString name) throws IOException {}
            @Override public void writeString(String text) throws IOException {}
            @Override public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override public void writeString(SerializableString text) throws IOException {}
            @Override public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeRaw(String text) throws IOException { captured[0] = text; }
            @Override public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char c) throws IOException {}
            @Override public void writeRawValue(String text) throws IOException {}
            @Override public void writeRawValue(String text, int offset, int len) throws IOException {}
            @Override public void writeRawValue(char[] text, int offset, int len) throws IOException {}
            @Override public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException { return 0; }
            @Override public void writeNumber(int v) throws IOException {}
            @Override public void writeNumber(long v) throws IOException {}
            @Override public void writeNumber(BigInteger v) throws IOException {}
            @Override public void writeNumber(double v) throws IOException {}
            @Override public void writeNumber(float v) throws IOException {}
            @Override public void writeNumber(BigDecimal v) throws IOException {}
            @Override public void writeNumber(String encodedValue) throws IOException {}
            @Override public void writeBoolean(boolean state) throws IOException {}
            @Override public void writeNull() throws IOException {}
            @Override public void writeObject(Object pojo) throws IOException {}
            @Override public void writeTree(TreeNode rootNode) throws IOException {}
            @Override public JsonStreamContext getOutputContext() { return null; }
            @Override public void flush() throws IOException {}
            @Override public boolean isClosed() { return false; }
            @Override public void close() throws IOException {}
        };
        stub.writeRaw(new SerializableString() {
            @Override public String getValue() { return "raw"; }
            @Override public int charLength() { return 3; }
            @Override public char[] asQuotedChars() { return "raw".toCharArray(); }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'r','a','w'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'r','a','w'}; }
        });
        assertEquals("raw", captured[0]);
    }

    // Tests for copyCurrentEvent using real parser and generator
    @Test
    public void testCopyCurrentEvent_StringValue_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "\"hello\"";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentEvent(parser);
        gen.close();
        assertEquals("\"hello\"", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentEvent_IntValue_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "42";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentEvent(parser);
        gen.close();
        assertEquals("42", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentEvent_FloatValue_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "3.14";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentEvent(parser);
        gen.close();
        assertEquals("3.14", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentEvent_BooleanTrue_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "true";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentEvent(parser);
        gen.close();
        assertEquals("true", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentEvent_Null_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "null";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentEvent(parser);
        gen.close();
        assertEquals("null", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentStructure_Object_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "{\"a\":1,\"b\":2}";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentStructure(parser);
        gen.close();
        assertEquals("{\"a\":1,\"b\":2}", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentStructure_Array_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        String input = "[1,2,3]";
        JsonParser parser = factory.createParser(input);
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.copyCurrentStructure(parser);
        gen.close();
        assertEquals("[1,2,3]", out.toString("UTF-8"));
    }

    // Tests for _writeSimpleObject via custom subclass
    @Test
    public void testWriteSimpleObject_null_callsWriteNull() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(null);
        assertTrue(gen.writeNullCalled);
    }

    @Test
    public void testWriteSimpleObject_String_callsWriteString() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject("test");
        assertTrue(gen.writeStringCalled);
        assertEquals("test", gen.lastString);
    }

    @Test
    public void testWriteSimpleObject_Integer_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(123);
        assertTrue(gen.writeNumberCalled);
        assertEquals(123, gen.lastInt);
    }

    @Test
    public void testWriteSimpleObject_Long_callsWriteNumberLong() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(999L);
        assertTrue(gen.writeNumberCalled);
        assertEquals(999L, gen.lastLong);
    }

    @Test
    public void testWriteSimpleObject_Double_callsWriteNumberDouble() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(1.5);
        assertTrue(gen.writeNumberCalled);
        assertEquals(1.5, gen.lastDouble, 0.0);
    }

    @Test
    public void testWriteSimpleObject_Boolean_callsWriteBoolean() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(true);
        assertTrue(gen.writeBooleanCalled);
        assertTrue(gen.lastBoolean);
    }

    @Test
    public void testWriteSimpleObject_AtomicBoolean_callsWriteBoolean() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new AtomicBoolean(true));
        assertTrue(gen.writeBooleanCalled);
        assertTrue(gen.lastBoolean);
    }

    @Test
    public void testWriteSimpleObject_byteArray_callsWriteBinary() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        byte[] data = {1,2,3};
        gen.callWriteSimpleObject(data);
        assertTrue(gen.writeBinaryCalled);
        assertArrayEquals(data, gen.lastBinaryData);
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteSimpleObject_UnknownType_throwsIllegalStateException() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new Object());
    }

    // ===== Additional tests for previously skipped/untested areas =====

    @Test
    public void testWriteStartArrayEndArray_ProducesEmptyArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        assertEquals("[]", out.toString("UTF-8"));
    }

    @Test
    public void testWriteStartObjectEndObject_ProducesEmptyObject() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", out.toString("UTF-8"));
    }

    @Test
    public void testWriteFieldNameAndString_ProducesObjectField() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"name\":\"value\"}", out.toString("UTF-8"));
    }

    @Test
    public void testWriteFieldNameSerializableString_ProducesObjectField() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeFieldName(serializableString("field"));
        gen.writeNumber(7);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"field\":7}", out.toString("UTF-8"));
    }

    @Test
    public void testWriteStringCharArray_ProducesQuotedString() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeString(new char[]{'h','e','l','l','o'}, 0, 5);
        gen.close();
        assertEquals("\"hello\"", out.toString("UTF-8"));
    }

    @Test
    public void testWriteStringSerializableString_ProducesQuotedString() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeString(serializableString("hello"));
        gen.close();
        assertEquals("\"hello\"", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberLong_ProducesNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber(42L);
        gen.close();
        assertEquals("42", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberBigInteger_ProducesNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber(new BigInteger("123456789"));
        gen.close();
        assertEquals("123456789", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberDouble_ProducesNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber(1.5d);
        gen.close();
        assertEquals("1.5", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberFloat_ProducesNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber(1.5f);
        gen.close();
        assertEquals("1.5", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberBigDecimal_ProducesNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber(new BigDecimal("1.5"));
        gen.close();
        assertEquals("1.5", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberString_ProducesRawNumber() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNumber("1.25");
        gen.close();
        assertEquals("1.25", out.toString("UTF-8"));
    }

    @Test
    public void testWriteBooleanTrue_ProducesTrue() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeBoolean(true);
        gen.close();
        assertEquals("true", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNull_ProducesNull() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeNull();
        gen.close();
        assertEquals("null", out.toString("UTF-8"));
    }

    @Test
    public void testWriteRawString_ProducesRawText() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeRaw("raw");
        gen.close();
        assertEquals("raw", out.toString("UTF-8"));
    }

    @Test
    public void testWriteRawValue_String_ProducesRawValue() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeRawValue("1");
        gen.close();
        assertEquals("1", out.toString("UTF-8"));
    }

    @Test
    public void testWriteBinaryByteArray_ProducesBase64() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        byte[] data = {1, 2, 3};
        gen.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
        gen.close();
        assertEquals("\"AQID\"", out.toString("UTF-8"));
    }

    @Test
    public void testCopyCurrentEvent_FieldName_CopiesCorrectly() throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser("{\"a\":1}");
        parser.nextToken();
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.copyCurrentEvent(parser);
        gen.writeNumber(1);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"a\":1}", out.toString("UTF-8"));
    }

    @Test
    public void testFlush_DoesNotThrow() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeString("x");
        gen.flush();
        assertEquals("\"x\"", out.toString("UTF-8"));
        gen.close();
    }

    @Test
    public void testIsClosedLifecycle() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        assertFalse(gen.isClosed());
        gen.close();
        assertTrue(gen.isClosed());
    }

    @Test
    public void testGetOutputContext_AfterStartArray_IsNotNull() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.writeStartArray();
        assertNotNull(gen.getOutputContext());
        gen.close();
    }

    @Test
    public void testUseDefaultPrettyPrinter_DoesNotThrow() throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        gen.useDefaultPrettyPrinter();
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(1);
        gen.writeEndObject();
        gen.close();
        String json = out.toString("UTF-8");
        assertTrue(json.contains("\"a\""));
    }

    @Test
    public void testWriteSimpleObject_Float_callsWriteNumberFloat() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(1.5f);
        assertTrue(gen.writeNumberCalled);
        assertEquals(1.5f, gen.lastFloat, 0.0f);
    }

    @Test
    public void testWriteSimpleObject_Short_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject((short) 7);
        assertTrue(gen.writeNumberCalled);
        assertEquals(7, gen.lastInt);
    }

    @Test
    public void testWriteSimpleObject_Byte_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject((byte) 3);
        assertTrue(gen.writeNumberCalled);
        assertEquals(3, gen.lastInt);
    }

    @Test
    public void testWriteSimpleObject_BigInteger_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new BigInteger("123456789"));
        assertTrue(gen.writeNumberCalled);
        assertEquals(new BigInteger("123456789"), gen.lastBigInteger);
    }

    @Test
    public void testWriteSimpleObject_BigDecimal_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new BigDecimal("1.5"));
        assertTrue(gen.writeNumberCalled);
        assertEquals(new BigDecimal("1.5"), gen.lastBigDecimal);
    }

    @Test
    public void testWriteSimpleObject_AtomicInteger_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new AtomicInteger(42));
        assertTrue(gen.writeNumberCalled);
        assertEquals(42, gen.lastInt);
    }

    @Test
    public void testWriteSimpleObject_AtomicLong_callsWriteNumber() throws Exception {
        SimpleTestGenerator gen = new SimpleTestGenerator();
        gen.callWriteSimpleObject(new AtomicLong(99L));
        assertTrue(gen.writeNumberCalled);
        assertEquals(99L, gen.lastLong);
    }

    private static SerializableString serializableString(final String value) {
        final String quoted = "\"" + value + "\"";
        return new SerializableString() {
            @Override public String getValue() { return value; }
            @Override public int charLength() { return quoted.length(); }
            @Override public char[] asQuotedChars() { return quoted.toCharArray(); }
            @Override public byte[] asUnquotedUTF8() { return value.getBytes(StandardCharsets.UTF_8); }
            @Override public byte[] asQuotedUTF8() { return quoted.getBytes(StandardCharsets.UTF_8); }
        };
    }

    // Inner class that allows calling protected _writeSimpleObject
    private static class SimpleTestGenerator extends JsonGenerator {
        boolean writeNullCalled;
        boolean writeStringCalled;
        boolean writeNumberCalled;
        boolean writeBooleanCalled;
        boolean writeBinaryCalled;
        String lastString;
        int lastInt;
        long lastLong;
        double lastDouble;
        float lastFloat;
        boolean lastBoolean;
        byte[] lastBinaryData;
        BigInteger lastBigInteger;
        BigDecimal lastBigDecimal;

        @Override public JsonGenerator setCodec(ObjectCodec oc) { return this; }
        @Override public ObjectCodec getCodec() { return null; }
        @Override public Version version() { return Version.unknownVersion(); }
        @Override public JsonGenerator enable(Feature f) { return this; }
        @Override public JsonGenerator disable(Feature f) { return this; }
        @Override public boolean isEnabled(Feature f) { return true; }
        @Override public int getFeatureMask() { return Feature.collectDefaults(); }
        @Override @Deprecated public JsonGenerator setFeatureMask(int values) { return this; }
        @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
        @Override public void writeStartArray() throws IOException {}
        @Override public void writeEndArray() throws IOException {}
        @Override public void writeStartObject() throws IOException {}
        @Override public void writeEndObject() throws IOException {}
        @Override public void writeFieldName(String name) throws IOException {}
        @Override public void writeFieldName(SerializableString name) throws IOException {}
        @Override public void writeString(String text) throws IOException { writeStringCalled = true; lastString = text; }
        @Override public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override public void writeString(SerializableString text) throws IOException {}
        @Override public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override public void writeRaw(String text) throws IOException {}
        @Override public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char c) throws IOException {}
        @Override public void writeRawValue(String text) throws IOException {}
        @Override public void writeRawValue(String text, int offset, int len) throws IOException {}
        @Override public void writeRawValue(char[] text, int offset, int len) throws IOException {}
        @Override public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
            writeBinaryCalled = true;
            lastBinaryData = new byte[len];
            System.arraycopy(data, offset, lastBinaryData, 0, len);
        }
        @Override public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException { return 0; }
        @Override public void writeNumber(int v) throws IOException { writeNumberCalled = true; lastInt = v; }
        @Override public void writeNumber(long v) throws IOException { writeNumberCalled = true; lastLong = v; }
        @Override public void writeNumber(BigInteger v) throws IOException { writeNumberCalled = true; lastBigInteger = v; }
        @Override public void writeNumber(double v) throws IOException { writeNumberCalled = true; lastDouble = v; }
        @Override public void writeNumber(float v) throws IOException { writeNumberCalled = true; lastFloat = v; }
        @Override public void writeNumber(BigDecimal v) throws IOException { writeNumberCalled = true; lastBigDecimal = v; }
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException { writeBooleanCalled = true; lastBoolean = state; }
        @Override public void writeNull() throws IOException { writeNullCalled = true; }
        @Override public void writeObject(Object pojo) throws IOException {}
        @Override public void writeTree(TreeNode rootNode) throws IOException {}
        @Override public JsonStreamContext getOutputContext() { return null; }
        @Override public void flush() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() throws IOException {}

        public void callWriteSimpleObject(Object value) throws IOException {
            _writeSimpleObject(value);
        }
    }
}
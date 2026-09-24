package com.fasterxml.jackson.core;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class JsonGeneratorTest {

    private TestJsonGenerator generator;

    @Before
    public void setUp() {
        generator = new TestJsonGenerator();
    }

    // Tests Feature defaults and bitmask utilities
    @Test
    public void testFeatureDefaults_collectDefaults_returnsCorrectMask() {
        int defaults = JsonGenerator.Feature.collectDefaults();
        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_TARGET.enabledByDefault());
        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.enabledByDefault());
        assertTrue(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.enabledByDefault());
        assertTrue(JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledByDefault());
        assertTrue(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledByDefault());
        assertFalse(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.enabledByDefault());
        assertFalse(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledByDefault());
        assertFalse(JsonGenerator.Feature.ESCAPE_NON_ASCII.enabledByDefault());
        assertFalse(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.enabledByDefault());
        assertFalse(JsonGenerator.Feature.IGNORE_UNKNOWN.enabledByDefault());

        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_TARGET.enabledIn(defaults));
        assertFalse(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(defaults));
    }

    // Tests configure method toggling features
    @Test
    public void testConfigure_enableAndDisableFeature_updatesMask() {
        generator.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));

        generator.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    // Tests overrideStdFeatures calculation
    @Test
    public void testOverrideStdFeatures_validMasks_updatesFeatureMask() {
        int mask = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        int values = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();

        generator.setFeatureMask(JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask());
        generator.overrideStdFeatures(values, mask);

        assertFalse(generator.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        assertTrue(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    // Tests overrideFormatFeatures throwing exception by default
    @Test(expected = IllegalArgumentException.class)
    public void testOverrideFormatFeatures_defaultImplementation_throwsIllegalArgumentException() {
        generator.overrideFormatFeatures(0, 0);
    }

    // Tests setSchema throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetSchema_defaultImplementation_throwsUnsupportedOperationException() {
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "TEST_SCHEMA";
            }
        };
        generator.setSchema(schema);
    }

    // Tests setRootValueSeparator throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetRootValueSeparator_defaultImplementation_throwsUnsupportedOperationException() {
        generator.setRootValueSeparator(null);
    }

    // Tests pretty printer getter and setter
    @Test
    public void testSetPrettyPrinter_customPrinter_storesAndReturnsPrinter() {
        assertNull(generator.getPrettyPrinter());
        PrettyPrinter pp = new PrettyPrinter() {
            @Override
            public void writeRootValueSeparator(JsonGenerator gen) { }
            @Override
            public void writeStartObject(JsonGenerator gen) { }
            @Override
            public void writeEndObject(JsonGenerator gen, int nrOfValues) { }
            @Override
            public void writeObjectEntrySeparator(JsonGenerator gen) { }
            @Override
            public void writeObjectFieldValueSeparator(JsonGenerator gen) { }
            @Override
            public void writeStartArray(JsonGenerator gen) { }
            @Override
            public void writeEndArray(JsonGenerator gen, int nrOfValues) { }
            @Override
            public void writeArrayValueSeparator(JsonGenerator gen) { }
            @Override
            public void beforeArrayValues(JsonGenerator gen) { }
            @Override
            public void beforeObjectEntries(JsonGenerator gen) { }
        };
        generator.setPrettyPrinter(pp);
        assertSame(pp, generator.getPrettyPrinter());
    }

    // Tests default capability introspection methods
    @Test
    public void testCapabilityDefaults_defaultState_returnsExpectedBooleans() {
        assertFalse(generator.canUseSchema(null));
        assertFalse(generator.canWriteObjectId());
        assertFalse(generator.canWriteTypeId());
        assertFalse(generator.canWriteBinaryNatively());
        assertTrue(generator.canOmitFields());
        assertFalse(generator.canWriteFormattedNumbers());
        assertEquals(0, generator.getFormatFeatures());
        assertNull(generator.getSchema());
        assertNull(generator.getOutputTarget());
        assertEquals(-1, generator.getOutputBuffered());
        assertEquals(0, generator.getHighestEscapedChar());
        assertNull(generator.getCharacterEscapes());
    }

    // Tests current value handling via context
    @Test
    public void testCurrentValue_withContext_storesAndRetrievesValue() {
        assertNull(generator.getCurrentValue());
        Object value = new Object();
        generator.setCurrentValue(value);
        assertSame(value, generator.getCurrentValue());
    }

    // Tests writeArray for int[]
    @Test
    public void testWriteArray_intArray_writesStartNumbersAndEnd() throws IOException {
        int[] data = new int[] { 1, 2, 3 };
        generator.writeArray(data, 0, data.length);
        assertEquals("[startArray, 1, 2, 3, endArray]", generator.events.toString());
    }

    // Tests writeArray for int[] with invalid bounds throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWriteArray_invalidOffset_throwsIllegalArgumentException() throws IOException {
        int[] data = new int[] { 1, 2 };
        generator.writeArray(data, 1, 2);
    }

    // Tests writeArray with null array input
    @Test(expected = IllegalArgumentException.class)
    public void testWriteArray_nullIntArray_throwsIllegalArgumentException() throws IOException {
        generator.writeArray((int[]) null, 0, 0);
    }

    // Tests writeArray for long[]
    @Test
    public void testWriteArray_longArray_writesStartNumbersAndEnd() throws IOException {
        long[] data = new long[] { 100L, 200L };
        generator.writeArray(data, 0, data.length);
        assertEquals("[startArray, 100, 200, endArray]", generator.events.toString());
    }

    // Tests writeArray for double[]
    @Test
    public void testWriteArray_doubleArray_writesStartNumbersAndEnd() throws IOException {
        double[] data = new double[] { 1.5, 2.5 };
        generator.writeArray(data, 0, data.length);
        assertEquals("[startArray, 1.5, 2.5, endArray]", generator.events.toString());
    }

    // Tests writeEmbeddedObject throwing JsonGenerationException
    @Test(expected = JsonGenerationException.class)
    public void testWriteEmbeddedObject_defaultImplementation_throwsJsonGenerationException() throws IOException {
        generator.writeEmbeddedObject(new Object());
    }

    // Tests writeObjectId throwing JsonGenerationException
    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectId_defaultImplementation_throwsJsonGenerationException() throws IOException {
        generator.writeObjectId("id123");
    }

    // Tests writeObjectRef throwing JsonGenerationException
    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectRef_defaultImplementation_throwsJsonGenerationException() throws IOException {
        generator.writeObjectRef("id123");
    }

    // Tests writeTypeId throwing JsonGenerationException
    @Test(expected = JsonGenerationException.class)
    public void testWriteTypeId_defaultImplementation_throwsJsonGenerationException() throws IOException {
        generator.writeTypeId("type123");
    }

    // Tests convenience field write methods
    @Test
    public void testConvenienceFields_writeFieldMethods_delegateProperly() throws IOException {
        generator.writeStringField("f_str", "val");
        generator.writeBooleanField("f_bool", true);
        generator.writeNullField("f_null");
        generator.writeNumberField("f_int", 10);
        generator.writeNumberField("f_long", 20L);
        generator.writeNumberField("f_double", 30.5);
        generator.writeNumberField("f_float", 40.5f);
        generator.writeNumberField("f_bd", new BigDecimal("50.5"));
        generator.writeBinaryField("f_bin", new byte[] { 1, 2 });
        generator.writeArrayFieldStart("f_arr");
        generator.writeEndArray();
        generator.writeObjectFieldStart("f_obj");
        generator.writeEndObject();
        generator.writeObjectField("f_pojo", "objValue");
        generator.writeFieldId(999L);
        generator.writeNumber((short) 5);

        assertTrue(generator.events.contains("field:f_str"));
        assertTrue(generator.events.contains("string:val"));
        assertTrue(generator.events.contains("field:f_bool"));
        assertTrue(generator.events.contains("bool:true"));
        assertTrue(generator.events.contains("field:f_null"));
        assertTrue(generator.events.contains("null"));
        assertTrue(generator.events.contains("field:f_int"));
        assertTrue(generator.events.contains("10"));
        assertTrue(generator.events.contains("field:f_long"));
        assertTrue(generator.events.contains("20"));
        assertTrue(generator.events.contains("field:f_double"));
        assertTrue(generator.events.contains("30.5"));
        assertTrue(generator.events.contains("field:f_float"));
        assertTrue(generator.events.contains("40.5"));
        assertTrue(generator.events.contains("field:f_bd"));
        assertTrue(generator.events.contains("50.5"));
        assertTrue(generator.events.contains("field:f_bin"));
        assertTrue(generator.events.contains("binary:2"));
        assertTrue(generator.events.contains("field:f_arr"));
        assertTrue(generator.events.contains("field:f_obj"));
        assertTrue(generator.events.contains("field:f_pojo"));
        assertTrue(generator.events.contains("pojo:objValue"));
        assertTrue(generator.events.contains("field:999"));
        assertTrue(generator.events.contains("5"));
    }

    // Tests _writeSimpleObject for supported types
    @Test
    public void testWriteSimpleObject_supportedTypes_delegatesToCorrectWriteMethod() throws IOException {
        generator.testWriteSimpleObject(null);
        generator.testWriteSimpleObject("text");
        generator.testWriteSimpleObject(Integer.valueOf(1));
        generator.testWriteSimpleObject(Long.valueOf(2L));
        generator.testWriteSimpleObject(Double.valueOf(3.0));
        generator.testWriteSimpleObject(Float.valueOf(4.0f));
        generator.testWriteSimpleObject(Short.valueOf((short) 5));
        generator.testWriteSimpleObject(Byte.valueOf((byte) 6));
        generator.testWriteSimpleObject(BigInteger.valueOf(7));
        generator.testWriteSimpleObject(new BigDecimal("8.0"));
        generator.testWriteSimpleObject(new AtomicInteger(9));
        generator.testWriteSimpleObject(new AtomicLong(10L));
        generator.testWriteSimpleObject(new byte[] { 1, 2, 3 });
        generator.testWriteSimpleObject(Boolean.TRUE);
        generator.testWriteSimpleObject(new AtomicBoolean(false));

        assertEquals("[null, string:text, 1, 2, 3.0, 4.0, 5, 6, 7, 8.0, 9, 10, binary:3, bool:true, bool:false]",
                generator.events.toString());
    }

    // Tests _writeSimpleObject with unsupported object throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWriteSimpleObject_unsupportedType_throwsIllegalStateException() throws IOException {
        generator.testWriteSimpleObject(new Object());
    }

    // Tests writeBinary overload delegates
    @Test
    public void testWriteBinary_overloads_delegatesProperly() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4 };
        generator.writeBinary(data);
        generator.writeBinary(data, 1, 2);
        InputStream in = new ByteArrayInputStream(data);
        generator.writeBinary(in, data.length);

        assertTrue(generator.events.contains("binary:4"));
        assertTrue(generator.events.contains("binary:2"));
        assertTrue(generator.events.contains("binaryStream:4"));
    }

    // Tests startObject and startArray with arguments
    @Test
    public void testStructuralStarts_withArguments_updatesContextAndStarts() throws IOException {
        Object testObj = new Object();
        generator.writeStartObject(testObj);
        assertSame(testObj, generator.getCurrentValue());
        generator.writeEndObject();

        generator.writeStartArray(5);
        generator.writeEndArray();

        assertTrue(generator.events.contains("startObject"));
        assertTrue(generator.events.contains("endObject"));
        assertTrue(generator.events.contains("startArray"));
        assertTrue(generator.events.contains("endArray"));
    }

    /**
     * Concrete helper implementation of JsonGenerator for testing base class behaviors.
     */
    private static class TestJsonGenerator extends JsonGenerator {
        final List<String> events = new ArrayList<String>();
        private int _features = Feature.collectDefaults();
        private TestStreamContext _context = new TestStreamContext();

        public void testWriteSimpleObject(Object value) throws IOException {
            _writeSimpleObject(value);
        }

        @Override
        public JsonGenerator setCodec(ObjectCodec oc) {
            return this;
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonGenerator enable(Feature f) {
            _features |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            _features &= ~f.getMask();
            return this;
        }

        @Override
        public boolean isEnabled(Feature f) {
            return (_features & f.getMask()) != 0;
        }

        @Override
        public int getFeatureMask() {
            return _features;
        }

        @Override
        public JsonGenerator setFeatureMask(int values) {
            _features = values;
            return this;
        }

        @Override
        public JsonGenerator useDefaultPrettyPrinter() {
            return this;
        }

        @Override
        public void writeStartArray() throws IOException {
            events.add("startArray");
        }

        @Override
        public void writeEndArray() throws IOException {
            events.add("endArray");
        }

        @Override
        public void writeStartObject() throws IOException {
            events.add("startObject");
        }

        @Override
        public void writeEndObject() throws IOException {
            events.add("endObject");
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            events.add("field:" + name);
        }

        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            events.add("field:" + name.getValue());
        }

        @Override
        public void writeString(String text) throws IOException {
            events.add("string:" + text);
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            events.add("string:" + new String(text, offset, len));
        }

        @Override
        public void writeString(SerializableString text) throws IOException {
            events.add("string:" + text.getValue());
        }

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException { }

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException { }

        @Override
        public void writeRaw(String text) throws IOException { }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException { }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException { }

        @Override
        public void writeRaw(char c) throws IOException { }

        @Override
        public void writeRawValue(String text) throws IOException { }

        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException { }

        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException { }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
            events.add("binary:" + len);
        }

        @Override
        public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
            events.add("binaryStream:" + dataLength);
            return dataLength;
        }

        @Override
        public void writeNumber(int v) throws IOException {
            events.add(String.valueOf(v));
        }

        @Override
        public void writeNumber(long v) throws IOException {
            events.add(String.valueOf(v));
        }

        @Override
        public void writeNumber(BigInteger v) throws IOException {
            events.add(v == null ? "null" : v.toString());
        }

        @Override
        public void writeNumber(double v) throws IOException {
            events.add(String.valueOf(v));
        }

        @Override
        public void writeNumber(float v) throws IOException {
            events.add(String.valueOf(v));
        }

        @Override
        public void writeNumber(BigDecimal v) throws IOException {
            events.add(v == null ? "null" : v.toString());
        }

        @Override
        public void writeNumber(String encodedValue) throws IOException {
            events.add(encodedValue);
        }

        @Override
        public void writeBoolean(boolean state) throws IOException {
            events.add("bool:" + state);
        }

        @Override
        public void writeNull() throws IOException {
            events.add("null");
        }

        @Override
        public void writeObject(Object pojo) throws IOException {
            events.add("pojo:" + pojo);
        }

        @Override
        public void writeTree(TreeNode rootNode) throws IOException {
            events.add("tree:" + rootNode);
        }

        @Override
        public JsonStreamContext getOutputContext() {
            return _context;
        }

        @Override
        public void flush() throws IOException { }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void close() throws IOException { }
    }

    private static class TestStreamContext extends JsonStreamContext {
        private Object _currentValue;

        TestStreamContext() {
            super();
            _type = TYPE_ROOT;
            _index = 0;
        }

        @Override
        public String getCurrentName() {
            return null;
        }

        @Override
        public JsonStreamContext getParent() {
            return null;
        }

        @Override
        public Object getCurrentValue() {
            return _currentValue;
        }

        @Override
        public void setCurrentValue(Object v) {
            _currentValue = v;
        }
    }
}
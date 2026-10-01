package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializersTest {

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final JsonFactory JSON_F = new JsonFactory();

    static class FormattedIntWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int value;

        public FormattedIntWrapper(int v) {
            this.value = v;
        }
    }

    static class FormattedLongWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public long value;

        public FormattedLongWrapper(long v) {
            this.value = v;
        }
    }

    static class FormattedDoubleWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double value;

        public FormattedDoubleWrapper(double v) {
            this.value = v;
        }
    }

    static class FormattedNumberDefaultShapeWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int value;

        public FormattedNumberDefaultShapeWrapper(int v) {
            this.value = v;
        }
    }

    static class FormattedByteWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public byte value;

        public FormattedByteWrapper(byte v) {
            this.value = v;
        }
    }

    static class FormattedShortWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public short value;

        public FormattedShortWrapper(short v) {
            this.value = v;
        }
    }

    static class FormattedFloatWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public float value;

        public FormattedFloatWrapper(float v) {
            this.value = v;
        }
    }

    static class PolymorphicWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object value;

        public PolymorphicWrapper(Object v) {
            this.value = v;
        }
    }

    // Tests addAll mapping registration for all supported primitive and wrapper types
    @Test
    public void testAddAll_containsAllStandardNumberTypes() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        assertTrue(map.containsKey(Integer.class.getName()));
        assertTrue(map.containsKey(Integer.TYPE.getName()));
        assertTrue(map.containsKey(Long.class.getName()));
        assertTrue(map.containsKey(Long.TYPE.getName()));
        assertTrue(map.containsKey(Byte.class.getName()));
        assertTrue(map.containsKey(Byte.TYPE.getName()));
        assertTrue(map.containsKey(Short.class.getName()));
        assertTrue(map.containsKey(Short.TYPE.getName()));
        assertTrue(map.containsKey(Double.class.getName()));
        assertTrue(map.containsKey(Double.TYPE.getName()));
        assertTrue(map.containsKey(Float.class.getName()));
        assertTrue(map.containsKey(Float.TYPE.getName()));
        assertEquals(12, map.size());
    }

    // Tests default constructor instantiation of NumberSerializers container
    @Test
    public void testConstructor_instantiationSucceeds() {
        NumberSerializers serializers = new NumberSerializers();
        assertNotNull(serializers);
    }

    // Tests IntegerSerializer direct serialization of positive, negative and zero values
    @Test
    public void testIntegerSerializer_serialize_writesCorrectNumber() throws IOException {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Integer.valueOf(123), gen, null);
        ser.serialize(Integer.valueOf(-456), gen, null);
        ser.serialize(Integer.valueOf(0), gen, null);
        gen.flush();

        assertEquals("123-4560", sw.toString());
    }

    // Tests IntLikeSerializer with Byte, Short and Number values
    @Test
    public void testIntLikeSerializer_serialize_writesIntValue() throws IOException {
        NumberSerializers.IntLikeSerializer ser = NumberSerializers.IntLikeSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Byte.valueOf((byte) 42), gen, null);
        ser.serialize(Short.valueOf((short) 100), gen, null);
        gen.flush();

        assertEquals("42100", sw.toString());
    }

    // Tests LongSerializer serialization with min and max boundaries
    @Test
    public void testLongSerializer_serialize_writesLongBoundaries() throws IOException {
        NumberSerializers.LongSerializer ser = new NumberSerializers.LongSerializer(Long.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Long.valueOf(Long.MAX_VALUE), gen, null);
        ser.serialize(Long.valueOf(Long.MIN_VALUE), gen, null);
        gen.flush();

        assertEquals(Long.MAX_VALUE + "" + Long.MIN_VALUE, sw.toString());
    }

    // Tests ShortSerializer serialization
    @Test
    public void testShortSerializer_serialize_writesShortValue() throws IOException {
        NumberSerializers.ShortSerializer ser = NumberSerializers.ShortSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Short.valueOf((short) 15), gen, null);
        gen.flush();

        assertEquals("15", sw.toString());
    }

    // Tests FloatSerializer serialization
    @Test
    public void testFloatSerializer_serialize_writesFloatValue() throws IOException {
        NumberSerializers.FloatSerializer ser = NumberSerializers.FloatSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Float.valueOf(1.25f), gen, null);
        gen.flush();

        assertEquals("1.25", sw.toString());
    }

    // Tests DoubleSerializer serialization
    @Test
    public void testDoubleSerializer_serialize_writesDoubleValue() throws IOException {
        NumberSerializers.DoubleSerializer ser = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);

        ser.serialize(Double.valueOf(3.14159), gen, null);
        gen.flush();

        assertEquals("3.14159", sw.toString());
    }

    // Tests IntegerSerializer and DoubleSerializer serializeWithType delegating to serialize without type info
    @Test
    public void testSerializeWithType_nativeScalarTypes_serializesWithoutTypeInfo() throws Exception {
        String jsonInt = MAPPER.writeValueAsString(new PolymorphicWrapper(Integer.valueOf(100)));
        assertEquals("{\"value\":100}", jsonInt);

        String jsonDouble = MAPPER.writeValueAsString(new PolymorphicWrapper(Double.valueOf(2.5)));
        assertEquals("{\"value\":2.5}", jsonDouble);
    }

    // Tests getSchema for integer and floating point serializers
    @Test
    public void testGetSchema_returnsCorrectSchemaNodes() {
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        JsonNode intSchema = intSer.getSchema(null, null);
        assertEquals("integer", intSchema.get("type").asText());

        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        JsonNode doubleSchema = doubleSer.getSchema(null, null);
        assertEquals("number", doubleSchema.get("type").asText());

        NumberSerializers.FloatSerializer floatSer = NumberSerializers.FloatSerializer.instance;
        JsonNode floatSchema = floatSer.getSchema(null, null);
        assertEquals("number", floatSchema.get("type").asText());
    }

    // Tests createContextual when JsonFormat shape is STRING returns ToStringSerializer
    @Test
    public void testCreateContextual_shapeString_serializesAsString() throws Exception {
        String jsonInt = MAPPER.writeValueAsString(new FormattedIntWrapper(123));
        assertEquals("{\"value\":\"123\"}", jsonInt);

        String jsonLong = MAPPER.writeValueAsString(new FormattedLongWrapper(999999999999L));
        assertEquals("{\"value\":\"999999999999\"}", jsonLong);

        String jsonDouble = MAPPER.writeValueAsString(new FormattedDoubleWrapper(1.5));
        assertEquals("{\"value\":\"1.5\"}", jsonDouble);
    }

    // Tests createContextual when JsonFormat shape is default or not STRING
    @Test
    public void testCreateContextual_shapeNumber_serializesAsNumber() throws Exception {
        String json = MAPPER.writeValueAsString(new FormattedNumberDefaultShapeWrapper(456));
        assertEquals("{\"value\":456}", json);
    }

    // Tests acceptJsonFormatVisitor for integer types
    @Test
    public void testAcceptJsonFormatVisitor_integerVisitorCalled() throws Exception {
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        intSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Integer.class));
        assertTrue(visited[0]);
    }

    // Tests acceptJsonFormatVisitor for floating point types
    @Test
    public void testAcceptJsonFormatVisitor_floatVisitorCalled() throws Exception {
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };

        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        doubleSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Double.class));
        assertTrue(visited[0]);
    }

    // Tests createContextual when property is null returns same serializer
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        SerializerProvider prov = MAPPER.getSerializerProviderInstance();
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        JsonSerializer<?> result = intSer.createContextual(prov, null);
        assertSame(intSer, result);
    }

    // Additional tests for complete coverage

    @Test
    public void testGetSchema_remainingSerializers() {
        NumberSerializers.ShortSerializer shortSer = NumberSerializers.ShortSerializer.instance;
        JsonNode shortSchema = shortSer.getSchema(null, null);
        assertEquals("number", shortSchema.get("type").asText());

        NumberSerializers.IntLikeSerializer intLikeSer = NumberSerializers.IntLikeSerializer.instance;
        JsonNode intLikeSchema = intLikeSer.getSchema(null, null);
        assertEquals("integer", intLikeSchema.get("type").asText());

        NumberSerializers.LongSerializer longSer = new NumberSerializers.LongSerializer(Long.class);
        JsonNode longSchema = longSer.getSchema(null, null);
        assertEquals("number", longSchema.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitor_numberTypeSetOnVisitor() throws Exception {
        final JsonParser.NumberType[] integerNumberType = new JsonParser.NumberType[1];
        JsonIntegerFormatVisitor intVisitor = new JsonIntegerFormatVisitor.Base() {
            @Override
            public void numberType(JsonParser.NumberType type) {
                integerNumberType[0] = type;
            }
        };

        final JsonParser.NumberType[] numberNumberType = new JsonParser.NumberType[1];
        JsonNumberFormatVisitor numVisitor = new JsonNumberFormatVisitor.Base() {
            @Override
            public void numberType(JsonParser.NumberType type) {
                numberNumberType[0] = type;
            }
        };

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return intVisitor;
            }

            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return numVisitor;
            }
        };

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        intSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Integer.class));
        assertEquals(JsonParser.NumberType.INT, integerNumberType[0]);

        NumberSerializers.LongSerializer longSer = new NumberSerializers.LongSerializer(Long.class);
        longSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Long.class));
        assertEquals(JsonParser.NumberType.LONG, integerNumberType[0]);

        NumberSerializers.IntLikeSerializer intLikeSer = NumberSerializers.IntLikeSerializer.instance;
        intLikeSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Byte.class));
        assertEquals(JsonParser.NumberType.INT, integerNumberType[0]);

        NumberSerializers.ShortSerializer shortSer = NumberSerializers.ShortSerializer.instance;
        shortSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Short.class));
        assertEquals(JsonParser.NumberType.INT, integerNumberType[0]);

        NumberSerializers.FloatSerializer floatSer = NumberSerializers.FloatSerializer.instance;
        floatSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Float.class));
        assertEquals(JsonParser.NumberType.FLOAT, numberNumberType[0]);

        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        doubleSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Double.class));
        assertEquals(JsonParser.NumberType.DOUBLE, numberNumberType[0]);
    }

    @Test
    public void testCreateContextual_additionalFormattedWrappers() throws Exception {
        String jsonByte = MAPPER.writeValueAsString(new FormattedByteWrapper((byte) 7));
        assertEquals("{\"value\":\"7\"}", jsonByte);

        String jsonShort = MAPPER.writeValueAsString(new FormattedShortWrapper((short) 42));
        assertEquals("{\"value\":\"42\"}", jsonShort);

        String jsonFloat = MAPPER.writeValueAsString(new FormattedFloatWrapper(3.5f));
        assertEquals("{\"value\":\"3.5\"}", jsonFloat);
    }

    @Test
    public void testSerializeWithType_directCalls() throws Exception {
        SerializerProvider prov = MAPPER.getSerializerProviderInstance();
        TypeSerializer typeSer = MAPPER.getSerializationConfig()
                .getDefaultTyper(TypeFactory.defaultInstance().constructType(Object.class))
                .buildTypeSerializer(MAPPER.getSerializationConfig(),
                        TypeFactory.defaultInstance().constructType(Object.class), null);

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        intSer.serializeWithType(Integer.valueOf(789), gen, prov, typeSer);
        gen.flush();
        assertEquals("789", sw.toString());

        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
        doubleSer.serializeWithType(Double.valueOf(4.56), gen, prov, typeSer);
        gen.flush();
        assertEquals("4.56", sw.toString());
    }

    @Test
    public void testSerializeWithType_otherPolymorphicTypes() throws Exception {
        String jsonLong = MAPPER.writeValueAsString(new PolymorphicWrapper(Long.valueOf(1234567890123L)));
        assertEquals("{\"value\":1234567890123}", jsonLong);

        String jsonFloat = MAPPER.writeValueAsString(new PolymorphicWrapper(Float.valueOf(1.5f)));
        assertEquals("{\"value\":1.5}", jsonFloat);

        String jsonShort = MAPPER.writeValueAsString(new PolymorphicWrapper(Short.valueOf((short) 10)));
        assertEquals("{\"value\":10}", jsonShort);

        String jsonByte = MAPPER.writeValueAsString(new PolymorphicWrapper(Byte.valueOf((byte) 5)));
        assertEquals("{\"value\":5}", jsonByte);
    }
}
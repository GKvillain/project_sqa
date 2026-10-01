package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeSerializerBaseTest {

    private static class ConcreteDateSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public ConcreteDateSerializer() {
            this(null, null);
        }

        public ConcreteDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public ConcreteDateSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value == null ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else if (_customFormat != null) {
                synchronized (_customFormat) {
                    gen.writeString(_customFormat.format(value));
                }
            } else {
                serializers.defaultSerializeDateValue(value, gen);
            }
        }
    }

    static class WrapperWithPatternAnyShape {
        @JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy/MM/dd")
        public Date value;

        public WrapperWithPatternAnyShape(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithPatternStringShape {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        public Date value;

        public WrapperWithPatternStringShape(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithNumericShape {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date value;

        public WrapperWithNumericShape(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithShapeStringNoPattern {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date value;

        public WrapperWithShapeStringNoPattern(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithLocaleOnly {
        @JsonFormat(locale = "fr", pattern = "MMMM")
        public Date value;

        public WrapperWithLocaleOnly(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithTimeZoneOnly {
        @JsonFormat(timezone = "GMT+5")
        public Date value;

        public WrapperWithTimeZoneOnly(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithShapeAnyNoPattern {
        @JsonFormat(shape = JsonFormat.Shape.ANY)
        public Date value;

        public WrapperWithShapeAnyNoPattern(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithShapeObject {
        @JsonFormat(shape = JsonFormat.Shape.OBJECT)
        public Date value;

        public WrapperWithShapeObject(Date value) {
            this.value = value;
        }
    }

    static class WrapperWithoutFormat {
        public Date value;

        public WrapperWithoutFormat(Date value) {
            this.value = value;
        }
    }

    // Tests isEmpty method with null value
    @Test
    public void testIsEmpty_nullValue_returnsTrue() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertTrue(ser.isEmpty(null));
    }

    // Tests isEmpty method with date representing timestamp 0
    @Test
    public void testIsEmpty_zeroTimestamp_returnsTrue() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertTrue(ser.isEmpty(new Date(0L)));
    }

    // Tests isEmpty method with non-zero timestamp date
    @Test
    public void testIsEmpty_nonZeroTimestamp_returnsFalse() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertFalse(ser.isEmpty(new Date(123456789L)));
    }

    // Tests contextual isEmpty method with null value
    @Test
    public void testIsEmptyWithSerializerProvider_nullValue_returnsTrue() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertTrue(ser.isEmpty(null, null));
    }

    // Tests contextual isEmpty method with zero timestamp date
    @Test
    public void testIsEmptyWithSerializerProvider_zeroTimestamp_returnsTrue() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertTrue(ser.isEmpty(null, new Date(0L)));
    }

    // Tests contextual isEmpty method with non-zero timestamp date
    @Test
    public void testIsEmptyWithSerializerProvider_nonZeroTimestamp_returnsFalse() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        assertFalse(ser.isEmpty(null, new Date(123456789L)));
    }

    // Tests _asTimestamp when explicit useTimestamp is Boolean.TRUE
    @Test
    public void testAsTimestamp_useTimestampTrue_returnsTrue() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.TRUE, null);
        assertTrue(ser._asTimestamp(null));
    }

    // Tests _asTimestamp when explicit useTimestamp is Boolean.FALSE
    @Test
    public void testAsTimestamp_useTimestampFalse_returnsFalse() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.FALSE, null);
        assertFalse(ser._asTimestamp(null));
    }

    // Tests _asTimestamp when customFormat is provided
    @Test
    public void testAsTimestamp_customFormatNotNull_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        ConcreteDateSerializer ser = new ConcreteDateSerializer(null, df);
        assertFalse(ser._asTimestamp(null));
    }

    // Tests _asTimestamp exception path when provider is null and useTimestamp/customFormat are null
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProviderAndNoExplicitConfig_throwsIllegalArgumentException() {
        ConcreteDateSerializer ser = new ConcreteDateSerializer(null, null);
        ser._asTimestamp(null);
    }

    // Tests _asTimestamp delegating to SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
    @Test
    public void testAsTimestamp_providerEnabledFeature_returnsCorrectBoolean() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        ConcreteDateSerializer ser = new ConcreteDateSerializer(null, null);

        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        provider = mapper.getSerializerProviderInstance();
        assertTrue(ser._asTimestamp(provider));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        provider = mapper.getSerializerProviderInstance();
        assertFalse(ser._asTimestamp(provider));
    }

    // Tests getSchema when serialized as timestamp number
    @Test
    public void testGetSchema_asTimestamp_returnsNumberSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.TRUE, null);

        JsonNode schemaNode = ser.getSchema(provider, null);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests getSchema when serialized as date string
    @Test
    public void testGetSchema_asString_returnsStringSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.FALSE, null);

        JsonNode schemaNode = ser.getSchema(provider, null);
        assertNotNull(schemaNode);
        assertEquals("string", schemaNode.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor when serialized as integer/number
    @Test
    public void testAcceptJsonFormatVisitor_asTimestamp_visitsIntFormat() throws Exception {
        final boolean[] visitedInt = new boolean[1];
        final boolean[] visitedNumberType = new boolean[1];
        final boolean[] visitedFormat = new boolean[1];

        JsonIntegerFormatVisitor intVisitor = new JsonIntegerFormatVisitor.Base() {
            @Override
            public void numberType(JsonParser.NumberType type) {
                visitedNumberType[0] = (type == JsonParser.NumberType.LONG);
            }

            @Override
            public void format(JsonValueFormat format) {
                visitedFormat[0] = (format == JsonValueFormat.UTC_MILLISEC);
            }
        };

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public SerializerProvider getProvider() {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.getSerializerProviderInstance();
            }

            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visitedInt[0] = true;
                return intVisitor;
            }
        };

        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.TRUE, null);
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        assertTrue(visitedInt[0]);
        assertTrue(visitedNumberType[0]);
        assertTrue(visitedFormat[0]);
    }

    // Tests acceptJsonFormatVisitor when serialized as string
    @Test
    public void testAcceptJsonFormatVisitor_asString_visitsStringFormat() throws Exception {
        final boolean[] visitedString = new boolean[1];
        final boolean[] visitedFormat = new boolean[1];

        JsonStringFormatVisitor stringVisitor = new JsonStringFormatVisitor.Base() {
            @Override
            public void format(JsonValueFormat format) {
                visitedFormat[0] = (format == JsonValueFormat.DATE_TIME);
            }
        };

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public SerializerProvider getProvider() {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.getSerializerProviderInstance();
            }

            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                visitedString[0] = true;
                return stringVisitor;
            }
        };

        ConcreteDateSerializer ser = new ConcreteDateSerializer(Boolean.FALSE, null);
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        assertTrue(visitedString[0]);
        assertTrue(visitedFormat[0]);
    }

    // Tests createContextual when property is null
    @Test
    public void testCreateContextual_nullProperty_returnsThis() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        ConcreteDateSerializer ser = new ConcreteDateSerializer();

        JsonSerializer<?> contextual = ser.createContextual(provider, null);
        assertSame(ser, contextual);
    }

    // Tests createContextual when property has no JsonFormat annotation
    @Test
    public void testCreateContextual_propertyWithoutFormat_returnsThis() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanProperty prop = new BeanProperty.Bogus();

        ConcreteDateSerializer ser = new ConcreteDateSerializer();
        JsonSerializer<?> contextual = ser.createContextual(mapper.getSerializerProviderInstance(), prop);
        assertSame(ser, contextual);
    }

    // Tests createContextual and serialization with Shape.NUMBER
    @Test
    public void testCreateContextual_shapeNumber_serializesAsNumericTimestamp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Date date = new Date(1400000000000L);
        WrapperWithNumericShape input = new WrapperWithNumericShape(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":1400000000000}", json);
    }

    // Tests createContextual and serialization with Shape.STRING and pattern
    @Test
    public void testCreateContextual_shapeStringWithPattern_serializesFormattedString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(1400000000000L);
        WrapperWithPatternStringShape input = new WrapperWithPatternStringShape(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":\"2014-05-13\"}", json);
    }

    // Tests createContextual when JsonFormat has pattern with Shape.ANY (Defects4J bug JacksonDatabind-45)
    @Test
    public void testCreateContextual_shapeAnyWithPattern_serializesFormattedString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(1400000000000L);
        WrapperWithPatternAnyShape input = new WrapperWithPatternAnyShape(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":\"2014/05/13\"}", json);
    }

    // Tests createContextual with Shape.STRING without pattern (uses ISO-8601)
    @Test
    public void testCreateContextual_shapeStringWithoutPattern_serializesIso8601() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(1400000000000L);
        WrapperWithShapeStringNoPattern input = new WrapperWithShapeStringNoPattern(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":\"2014-05-13T16:53:20.000+0000\"}", json);
    }

    // Tests createContextual with custom Locale in JsonFormat
    @Test
    public void testCreateContextual_withLocale() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(1400000000000L);
        WrapperWithLocaleOnly input = new WrapperWithLocaleOnly(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":\"mai\"}", json);
    }

    // Tests createContextual with custom TimeZone in JsonFormat
    @Test
    public void testCreateContextual_withTimeZoneOnly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(1400000000000L);
        WrapperWithTimeZoneOnly input = new WrapperWithTimeZoneOnly(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":\"2014-05-13T21:53:20.000+0500\"}", json);
    }

    // Tests createContextual with Shape.ANY without pattern/tz/locale
    @Test
    public void testCreateContextual_shapeAnyNoPattern_returnsThis() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Date date = new Date(1400000000000L);
        WrapperWithShapeAnyNoPattern input = new WrapperWithShapeAnyNoPattern(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":1400000000000}", json);
    }

    // Tests createContextual with Shape.OBJECT without pattern/tz/locale
    @Test
    public void testCreateContextual_shapeObject_returnsThis() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Date date = new Date(1400000000000L);
        WrapperWithShapeObject input = new WrapperWithShapeObject(date);
        String json = mapper.writeValueAsString(input);

        assertEquals("{\"value\":1400000000000}", json);
    }
}
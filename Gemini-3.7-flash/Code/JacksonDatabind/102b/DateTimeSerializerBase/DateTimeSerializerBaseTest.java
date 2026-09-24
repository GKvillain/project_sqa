package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class DateTimeSerializerBaseTest {

    private static class TestDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public TestDateTimeSerializer() {
            super(Date.class, null, null);
        }

        public TestDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new TestDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value == null ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else {
                _serializeAsString(value, gen, serializers);
            }
        }
    }

    private ObjectMapper mapper;
    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
    }

    // Tests isEmpty method returns false
    @Test
    public void testIsEmpty_returnsFalse() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer();
        assertFalse(serializer.isEmpty(null, new Date(0L)));
        assertFalse(serializer.isEmpty(null, new Date(1000L)));
        assertFalse(serializer.isEmpty(null, null));
    }

    // Tests _asTimestamp with explicit timestamp flag true
    @Test
    public void testAsTimestamp_useTimestampTrue_returnsTrue() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.TRUE, null);
        assertTrue(serializer._asTimestamp(null));
    }

    // Tests _asTimestamp with explicit timestamp flag false
    @Test
    public void testAsTimestamp_useTimestampFalse_returnsFalse() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.FALSE, null);
        assertFalse(serializer._asTimestamp(null));
    }

    // Tests _asTimestamp with custom format specified returns false
    @Test
    public void testAsTimestamp_customFormatProvided_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(null, df);
        assertFalse(serializer._asTimestamp(null));
    }

    // Tests _asTimestamp with null provider and no custom format throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProvider_throwsException() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(null, null);
        serializer._asTimestamp(null);
    }

    // Tests _asTimestamp checks SerializerProvider feature when no flags set
    @Test
    public void testAsTimestamp_providerFeatureCheck() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(null, null);

        SerializerProvider provTimestamp = mapper.getSerializerProviderInstance();
        assertTrue(serializer._asTimestamp(provTimestamp));

        ObjectMapper noTimestampMapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider provNoTimestamp = noTimestampMapper.getSerializerProviderInstance();
        assertFalse(serializer._asTimestamp(provNoTimestamp));
    }

    // Tests getSchema when serialized as timestamp number
    @Test
    public void testGetSchema_asTimestamp_returnsNumberSchema() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.TRUE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schema = serializer.getSchema(provider, (Type) Date.class);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // Tests getSchema when serialized as string format
    @Test
    public void testGetSchema_asString_returnsStringSchema() {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.FALSE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schema = serializer.getSchema(provider, (Type) Date.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    // Tests _serializeAsString without custom format delegates to provider default
    @Test
    public void testSerializeAsString_nullCustomFormat_usesDefaultSerialize() throws Exception {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.FALSE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        Date date = new Date(0L);
        serializer._serializeAsString(date, gen, provider);
        gen.flush();

        assertNotNull(sw.toString());
        assertTrue(sw.toString().length() > 0);
    }

    // Tests _serializeAsString with custom format uses format and reuses cached instance
    @Test
    public void testSerializeAsString_customFormat_formatsCorrectlyAndReuses() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.FALSE, df);

        SerializerProvider provider = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        Date date = new Date(0L);
        serializer._serializeAsString(date, gen, provider);
        // Serialize second time to test format reuse path
        serializer._serializeAsString(date, gen, provider);
        gen.flush();

        assertEquals("\"1970/01/01\"\"1970/01/01\"", sw.toString());
    }

    // Tests acceptJsonFormatVisitor when serializing as number
    @Test
    public void testAcceptJsonFormatVisitor_asNumber() throws Exception {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.TRUE, null);
        final boolean[] intVisitorVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                intVisitorVisited[0] = true;
                return null;
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Date.class));
        assertTrue(intVisitorVisited[0]);
    }

    // Tests acceptJsonFormatVisitor when serializing as string
    @Test
    public void testAcceptJsonFormatVisitor_asString() throws Exception {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer(Boolean.FALSE, null);
        final boolean[] stringVisitorVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                stringVisitorVisited[0] = true;
                return null;
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Date.class));
        assertTrue(stringVisitorVisited[0]);
    }

    // Tests createContextual with null property returns this
    @Test
    public void testCreateContextual_nullProperty_returnsThis() throws Exception {
        TestDateTimeSerializer serializer = new TestDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> contextual = serializer.createContextual(provider, null);
        assertSame(serializer, contextual);
    }

    // Helper class for format annotation testing
    static class DateWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date numericDate;

        @JsonFormat(pattern = "yyyy_MM_dd", timezone = "UTC")
        public Date patternDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "GMT+2")
        public Date stringDate;

        @JsonFormat(locale = "fr_FR")
        public Date localeDate;

        public Date normalDate;
    }

    // Tests createContextual with numeric shape format override
    @Test
    public void testCreateContextual_numericShape_returnsNumericFormat() throws Exception {
        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("numericDate"),
                mapper.constructType(Date.class),
                null,
                mapper.getSerializationConfig().introspect(mapper.constructType(DateWrapper.class)).findProperties().get(0).getPrimaryMember(),
                PropertyMetadata.STD_OPTIONAL
        );

        TestDateTimeSerializer serializer = new TestDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> result = serializer.createContextual(provider, prop);

        assertTrue(result instanceof TestDateTimeSerializer);
        TestDateTimeSerializer dateSer = (TestDateTimeSerializer) result;
        assertTrue(dateSer._asTimestamp(provider));
    }

    // Tests createContextual with custom pattern and timezone
    @Test
    public void testCreateContextual_customPatternAndTimeZone() throws Exception {
        DateWrapper wrapper = new DateWrapper();
        wrapper.patternDate = new Date(0L);

        String json = mapper.writeValueAsString(wrapper);
        assertTrue(json.contains("\"patternDate\":\"1970_01_01\""));
    }

    // Tests createContextual with STRING shape and custom timezone on StdDateFormat
    @Test
    public void testCreateContextual_stringShapeWithTimeZone() throws Exception {
        DateWrapper wrapper = new DateWrapper();
        wrapper.stringDate = new Date(0L);

        String json = mapper.writeValueAsString(wrapper);
        assertTrue(json.contains("\"stringDate\":\"1970-01-01T02:00:00.000+02:00\"")
                || json.contains("\"stringDate\":\"1970-01-01T02:00:00.000+0200\""));
    }

    // Tests createContextual with SimpleDateFormat base and locale override
    @Test
    public void testCreateContextual_simpleDateFormatWithLocale() throws Exception {
        SimpleDateFormat customBase = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        customBase.setTimeZone(TimeZone.getTimeZone("UTC"));
        ObjectMapper customMapper = new ObjectMapper().setDateFormat(customBase);

        DateWrapper wrapper = new DateWrapper();
        wrapper.localeDate = new Date(0L);

        String json = customMapper.writeValueAsString(wrapper);
        assertNotNull(json);
        assertTrue(json.contains("localeDate"));
    }

    // Tests createContextual with config override on type for root value
    @Test
    public void testCreateContextual_configOverrideForType() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.configOverride(Date.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy/MM/dd").withTimeZone(TimeZone.getTimeZone("UTC")));

        String json = customMapper.writeValueAsString(new Date(0L));
        assertEquals("\"1970/01/01\"", json);
    }
}
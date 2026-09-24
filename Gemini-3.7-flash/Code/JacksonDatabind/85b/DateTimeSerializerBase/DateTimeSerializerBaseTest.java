package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;

public class DateTimeSerializerBaseTest {

    private static class DummyDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public DummyDateTimeSerializer() {
            this(null, null);
        }

        public DummyDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DummyDateTimeSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
            return new DummyDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return (value == null) ? 0L : value.getTime();
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

    private static class DummyWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date numericDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        public Date formattedDate;

        @JsonFormat(locale = "fr")
        public Date localeDate;

        public Date plainDate;
    }

    private ObjectMapper mapper;
    private DummyDateTimeSerializer defaultSerializer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        defaultSerializer = new DummyDateTimeSerializer();
    }

    private BeanProperty getBeanProperty(String fieldName) {
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(DummyWrapper.class));
        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            if (propDef.getName().equals(fieldName)) {
                return new BeanProperty.Std(
                        propDef.getFullName(),
                        propDef.getPrimaryType(),
                        propDef.getWrapperName(),
                        beanDesc.getClassAnnotations(),
                        propDef.getPrimaryMember(),
                        propDef.getMetadata()
                );
            }
        }
        return null;
    }

    // Tests isEmpty method with null and boundary timestamp values
    @Test
    public void testIsEmpty_variousDates_returnsExpected() {
        assertTrue(defaultSerializer.isEmpty(null));
        assertTrue(defaultSerializer.isEmpty(new Date(0L)));
        assertFalse(defaultSerializer.isEmpty(new Date(123456L)));

        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertTrue(defaultSerializer.isEmpty(provider, null));
        assertTrue(defaultSerializer.isEmpty(provider, new Date(0L)));
        assertFalse(defaultSerializer.isEmpty(provider, new Date(123456L)));
    }

    // Tests _asTimestamp when explicit useTimestamp flag is configured
    @Test
    public void testAsTimestamp_useTimestampConfigured_returnsConfiguredFlag() {
        DummyDateTimeSerializer timestampSerializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        assertTrue(timestampSerializer._asTimestamp(null));

        DummyDateTimeSerializer stringSerializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        assertFalse(stringSerializer._asTimestamp(null));
    }

    // Tests _asTimestamp when customFormat is provided
    @Test
    public void testAsTimestamp_withCustomFormat_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        DummyDateTimeSerializer customSerializer = new DummyDateTimeSerializer(null, df);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertFalse(customSerializer._asTimestamp(provider));
    }

    // Tests _asTimestamp fallback to SerializerProvider's SerializationFeature
    @Test
    public void testAsTimestamp_providerFeatures_returnsProviderSetting() {
        SerializerProvider providerWithTimestamps = mapper.getSerializerProviderInstance();
        assertTrue(defaultSerializer._asTimestamp(providerWithTimestamps));

        ObjectMapper noTimestampMapper = new ObjectMapper();
        noTimestampMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider providerWithoutTimestamps = noTimestampMapper.getSerializerProviderInstance();
        assertFalse(defaultSerializer._asTimestamp(providerWithoutTimestamps));
    }

    // Tests _asTimestamp throws IllegalArgumentException when provider is null and no timestamp flag
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProvider_throwsIllegalArgumentException() {
        defaultSerializer._asTimestamp(null);
    }

    // Tests getSchema returns number schema when timestamps are enabled
    @Test
    public void testGetSchema_asTimestamp_returnsNumberSchema() throws Exception {
        DummyDateTimeSerializer timestampSerializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schema = timestampSerializer.getSchema(provider, Date.class);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // Tests getSchema returns string schema when timestamps are disabled
    @Test
    public void testGetSchema_asString_returnsStringSchema() throws Exception {
        DummyDateTimeSerializer stringSerializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schema = stringSerializer.getSchema(provider, Date.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor visits integer format when serialized as timestamp
    @Test
    public void testAcceptJsonFormatVisitor_asTimestamp_callsVisitIntFormat() throws Exception {
        DummyDateTimeSerializer timestampSerializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        final boolean[] intVisited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                intVisited[0] = true;
                return null;
            }
        };

        timestampSerializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Date.class));
        assertTrue(intVisited[0]);
    }

    // Tests acceptJsonFormatVisitor visits string format when serialized as string
    @Test
    public void testAcceptJsonFormatVisitor_asString_callsVisitStringFormat() throws Exception {
        DummyDateTimeSerializer stringSerializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        final boolean[] stringVisited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                stringVisited[0] = true;
                return null;
            }
        };

        stringSerializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Date.class));
        assertTrue(stringVisited[0]);
    }

    // Tests createContextual returns self when property is null
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> result = defaultSerializer.createContextual(provider, null);
        assertSame(defaultSerializer, result);
    }

    // Tests createContextual handles numeric shape annotation
    @Test
    public void testCreateContextual_numericShape_returnsTimestampSerializer() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty property = getBeanProperty("numericDate");

        JsonSerializer<?> result = defaultSerializer.createContextual(provider, property);
        assertNotNull(result);
        assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer custom = (DummyDateTimeSerializer) result;
        assertTrue(custom._useTimestamp);
        assertNull(custom._customFormat);
    }

    // Tests createContextual handles custom string pattern and timezone annotations
    @Test
    public void testCreateContextual_stringFormatWithPatternAndTimeZone_returnsFormattedSerializer() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty property = getBeanProperty("formattedDate");

        JsonSerializer<?> result = defaultSerializer.createContextual(provider, property);
        assertNotNull(result);
        assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer custom = (DummyDateTimeSerializer) result;
        assertFalse(custom._useTimestamp);
        assertNotNull(custom._customFormat);
        assertTrue(custom._customFormat instanceof SimpleDateFormat);
        assertEquals("UTC", custom._customFormat.getTimeZone().getID());
    }

    // Tests createContextual handles locale annotation override
    @Test
    public void testCreateContextual_localeOverride_returnsConfiguredSerializer() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty property = getBeanProperty("localeDate");

        JsonSerializer<?> result = defaultSerializer.createContextual(provider, property);
        assertNotNull(result);
        assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer custom = (DummyDateTimeSerializer) result;
        assertFalse(custom._useTimestamp);
        assertNotNull(custom._customFormat);
    }

    // Tests createContextual returns self when property has no format annotations
    @Test
    public void testCreateContextual_noFormatAnnotation_returnsSelf() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty property = getBeanProperty("plainDate");

        JsonSerializer<?> result = defaultSerializer.createContextual(provider, property);
        assertSame(defaultSerializer, result);
    }
}
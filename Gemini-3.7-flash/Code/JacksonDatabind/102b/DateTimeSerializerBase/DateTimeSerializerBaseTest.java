package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
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
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class DateTimeSerializerBaseTest {

    private static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public ConcreteDateTimeSerializer() {
            super(Date.class, null, null);
        }

        public ConcreteDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public ConcreteDateTimeSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(timestamp, customFormat);
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

    // Tests isEmpty always returns false regardless of value
    @Test
    public void testIsEmpty_returnsFalse() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        assertFalse(ser.isEmpty(null, new Date(0L)));
        assertFalse(ser.isEmpty(null, new Date(1000L)));
        assertFalse(ser.isEmpty(null, null));
    }

    // Tests _asTimestamp when _useTimestamp is explicitly Boolean.TRUE
    @Test
    public void testAsTimestamp_useTimestampTrue_returnsTrue() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        assertTrue(ser._asTimestamp(null));
    }

    // Tests _asTimestamp when _useTimestamp is explicitly Boolean.FALSE
    @Test
    public void testAsTimestamp_useTimestampFalse_returnsFalse() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        assertFalse(ser._asTimestamp(null));
    }

    // Tests _asTimestamp with custom format returns false
    @Test
    public void testAsTimestamp_withCustomFormat_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, df);
        assertFalse(ser._asTimestamp(null));
    }

    // Tests _asTimestamp with null provider and null custom format throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProvider_throwsException() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, null);
        ser._asTimestamp(null);
    }

    // Tests _asTimestamp delegates to SerializerProvider feature
    @Test
    public void testAsTimestamp_withProviderFeature_returnsFeatureSetting() {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, null);

        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov1 = mapper.getSerializerProviderInstance();
        assertTrue(ser._asTimestamp(prov1));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov2 = mapper.getSerializerProviderInstance();
        assertFalse(ser._asTimestamp(prov2));
    }

    // Tests getSchema returns number schema when asTimestamp is true
    @Test
    public void testGetSchema_asTimestampTrue_returnsNumberNode() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        JsonNode node = ser.getSchema(null, (Type) null);
        assertNotNull(node);
        assertEquals("number", node.get("type").asText());
    }

    // Tests getSchema returns string schema when asTimestamp is false
    @Test
    public void testGetSchema_asTimestampFalse_returnsStringNode() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        JsonNode node = ser.getSchema(null, (Type) null);
        assertNotNull(node);
        assertEquals("string", node.get("type").asText());
    }

    // Tests _serializeAsString with null custom format delegates to provider default
    @Test
    public void testSerializeAsString_nullCustomFormat_usesProviderDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, null);

        Date date = new Date(1500000000000L);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(date, gen, prov);
        gen.flush();

        assertNotNull(sw.toString());
        assertTrue(sw.toString().length() > 0);
    }

    // Tests _serializeAsString with custom format uses and reuses the format
    @Test
    public void testSerializeAsString_customFormat_formatsCorrectly() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, df);

        Date date = new Date(1500000000000L);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(date, gen, prov);
        gen.flush();

        assertEquals("\"2017-07-14\"", sw.toString());

        // Re-run to verify reused custom format branch
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = new JsonFactory().createGenerator(sw2);
        ser.serialize(date, gen2, prov);
        gen2.flush();

        assertEquals("\"2017-07-14\"", sw2.toString());
    }

    // Tests acceptJsonFormatVisitor for number visitor
    @Test
    public void testAcceptJsonFormatVisitor_asNumber() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        assertTrue(visited[0]);
    }

    // Tests acceptJsonFormatVisitor for string visitor
    @Test
    public void testAcceptJsonFormatVisitor_asString() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        assertTrue(visited[0]);
    }

    // Tests createContextual with null property returns serializer
    @Test
    public void testCreateContextual_nullProperty_returnsSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        JsonSerializer<?> contextual = ser.createContextual(prov, null);
        assertNotNull(contextual);
    }

    // Tests createContextual with property and custom pattern
    @Test
    public void testCreateContextual_propertyWithPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        BeanProperty prop = new BeanProperty.Bogus() {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> baseType) {
                return JsonFormat.Value.forPattern("yyyy/MM/dd");
            }
        };

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotNull(contextual);
        assertTrue(contextual instanceof ConcreteDateTimeSerializer);

        ConcreteDateTimeSerializer resultSer = (ConcreteDateTimeSerializer) contextual;
        assertEquals(Boolean.FALSE, resultSer._useTimestamp);
        assertNotNull(resultSer._customFormat);
    }

    // Tests createContextual with numeric shape
    @Test
    public void testCreateContextual_shapeNumeric_returnsTimestampSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        BeanProperty prop = new BeanProperty.Bogus() {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> baseType) {
                return JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
            }
        };

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotNull(contextual);
        assertTrue(contextual instanceof ConcreteDateTimeSerializer);

        ConcreteDateTimeSerializer resultSer = (ConcreteDateTimeSerializer) contextual;
        assertEquals(Boolean.TRUE, resultSer._useTimestamp);
        assertNull(resultSer._customFormat);
    }

    // Tests createContextual with StdDateFormat locale and timezone adjustments
    @Test
    public void testCreateContextual_stdDateFormatWithLocaleAndTZ() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new StdDateFormat());
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        BeanProperty prop = new BeanProperty.Bogus() {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> baseType) {
                return new JsonFormat.Value(null, JsonFormat.Shape.STRING, Locale.GERMAN, "CET", null, null);
            }
        };

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotNull(contextual);
        assertTrue(contextual instanceof ConcreteDateTimeSerializer);

        ConcreteDateTimeSerializer resultSer = (ConcreteDateTimeSerializer) contextual;
        assertEquals(Boolean.FALSE, resultSer._useTimestamp);
        assertTrue(resultSer._customFormat instanceof StdDateFormat);
    }

    // Tests createContextual with SimpleDateFormat and timezone adjustment
    @Test
    public void testCreateContextual_simpleDateFormatWithTZ() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        BeanProperty prop = new BeanProperty.Bogus() {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> baseType) {
                return new JsonFormat.Value(null, null, Locale.US, "GMT+2", null, null);
            }
        };

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotNull(contextual);
        assertTrue(contextual instanceof ConcreteDateTimeSerializer);

        ConcreteDateTimeSerializer resultSer = (ConcreteDateTimeSerializer) contextual;
        assertEquals(Boolean.FALSE, resultSer._useTimestamp);
        assertTrue(resultSer._customFormat instanceof SimpleDateFormat);
    }

    // Tests createContextual with unsupported DateFormat reports bad definition
    @Test(expected = JsonMappingException.class)
    public void testCreateContextual_customNonSimpleDateFormat_reportsBadDefinition() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new DateFormat() {
            private static final long serialVersionUID = 1L;
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition fieldPosition) {
                return toAppendTo;
            }
            @Override
            public Date parse(String source, java.text.ParsePosition pos) {
                return null;
            }
        });
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        BeanProperty prop = new BeanProperty.Bogus() {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> baseType) {
                return new JsonFormat.Value(null, JsonFormat.Shape.STRING, null, null, null, null);
            }
        };

        ser.createContextual(prov, prop);
    }
}
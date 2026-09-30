package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class JsonValueSerializerTest {

    static class SimpleBean {
        private final String value;

        public SimpleBean(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    static class FieldBean {
        @JsonValue
        public final String name;

        public FieldBean(String name) {
            this.name = name;
        }
    }

    static class NullBean {
        @JsonValue
        public String getValue() {
            return null;
        }
    }

    static class IntBean {
        private final int value;

        public IntBean(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
    }

    static class BooleanBean {
        private final boolean value;

        public BooleanBean(boolean value) {
            this.value = value;
        }

        @JsonValue
        public boolean getValue() {
            return value;
        }
    }

    static class DoubleBean {
        private final double value;

        public DoubleBean(double value) {
            this.value = value;
        }

        @JsonValue
        public double getValue() {
            return doubleValue();
        }

        public double doubleValue() {
            return value;
        }
    }

    static class NonFinalBean {
        private final Object value;

        public NonFinalBean(Object value) {
            this.value = value;
        }

        @JsonValue
        public Object getValue() {
            return value;
        }
    }

    enum ValueEnum {
        ALPHA("A"),
        BETA("B");

        private final String code;

        ValueEnum(String code) {
            this.code = code;
        }

        @JsonValue
        public String getCode() {
            return code;
        }
    }

    enum FailingEnum {
        ITEM;

        @JsonValue
        public String getCode() {
            throw new RuntimeException("Enum access failed");
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    static class PolymorphicWrapperBean {
        private final int value;

        public PolymorphicWrapperBean(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    static class PolymorphicMapBean {
        private final Map<String, String> map;

        public PolymorphicMapBean(Map<String, String> map) {
            this.map = map;
        }

        @JsonValue
        public Map<String, String> getMap() {
            return map;
        }
    }

    static class ExceptionBean {
        @JsonValue
        public String getValue() {
            throw new IllegalStateException("Method invocation failed");
        }
    }

    static class ErrorBean {
        @JsonValue
        public String getValue() {
            throw new OutOfMemoryError("Simulated error");
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private DefaultSerializerProvider.Impl createSerializerProvider() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        return (DefaultSerializerProvider.Impl) prov.createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());
    }

    // Tests normal serialization of standard string returned by @JsonValue
    @Test
    public void testSerialize_simpleString_returnsSerializedString() throws IOException {
        SimpleBean bean = new SimpleBean("testValue");
        String json = mapper.writeValueAsString(bean);
        assertEquals("\"testValue\"", json);
    }

    // Tests serialization when @JsonValue is placed on a field
    @Test
    public void testSerialize_fieldAnnotated_returnsSerializedFieldValue() throws IOException {
        FieldBean bean = new FieldBean("fromField");
        String json = mapper.writeValueAsString(bean);
        assertEquals("\"fromField\"", json);
    }

    // Tests serialization when @JsonValue method returns null
    @Test
    public void testSerialize_nullValue_returnsNullLiteral() throws IOException {
        NullBean bean = new NullBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("null", json);
    }

    // Tests serialization of natural primitive types (int, boolean, double)
    @Test
    public void testSerialize_naturalTypes_returnsCorrectScalars() throws IOException {
        assertEquals("42", mapper.writeValueAsString(new IntBean(42)));
        assertEquals("true", mapper.writeValueAsString(new BooleanBean(true)));
        assertEquals("3.14", mapper.writeValueAsString(new DoubleBean(3.14)));
    }

    // Tests serialization of non-final Object type resolved dynamically at runtime
    @Test
    public void testSerialize_nonFinalType_serializesRuntimeType() throws IOException {
        NonFinalBean bean = new NonFinalBean(Collections.singletonMap("key", "value"));
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"key\":\"value\"}", json);
    }

    // Tests serialization of enum annotated with @JsonValue
    @Test
    public void testSerialize_enumWithJsonValue_returnsCustomValue() throws IOException {
        String json = mapper.writeValueAsString(ValueEnum.ALPHA);
        assertEquals("\"A\"", json);
    }

    // Tests serialization with USE_STATIC_TYPING enabled
    @Test
    public void testSerialize_staticTypingEnabled_serializesSuccessfully() throws IOException {
        ObjectMapper staticMapper = new ObjectMapper();
        staticMapper.enable(MapperFeature.USE_STATIC_TYPING);
        String json = staticMapper.writeValueAsString(new SimpleBean("static"));
        assertEquals("\"static\"", json);
    }

    // Tests polymorphic serialization with scalar natural type forcing type information
    @Test
    public void testSerializeWithType_forceTypeInfoOnNaturalType_includesWrapper() throws IOException {
        PolymorphicWrapperBean bean = new PolymorphicWrapperBean(123);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("PolymorphicWrapperBean"));
        assertTrue(json.contains("123"));
    }

    // Tests polymorphic serialization with map container value
    @Test
    public void testSerializeWithType_containerValue_serializesWithTypeInfo() throws IOException {
        PolymorphicMapBean bean = new PolymorphicMapBean(Collections.singletonMap("k", "v"));
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"k\":\"v\""));
    }

    // Tests exception handling when @JsonValue method throws an Exception
    @Test(expected = JsonMappingException.class)
    public void testSerialize_methodThrowsException_wrapsInJsonMappingException() throws IOException {
        mapper.writeValueAsString(new ExceptionBean());
    }

    // Tests error handling when @JsonValue method throws an Error
    @Test(expected = OutOfMemoryError.class)
    public void testSerialize_methodThrowsError_rethrowsErrorDirectly() throws IOException {
        mapper.writeValueAsString(new ErrorBean());
    }

    // Tests schema generation for standard class annotated with @JsonValue
    @Test
    @SuppressWarnings("deprecation")
    public void testGetSchema_standardClass_returnsValidSchema() throws Exception {
        JsonSchema schema = mapper.generateJsonSchema(SimpleBean.class);
        assertNotNull(schema);
        assertNotNull(schema.getSchemaNode());
    }

    // Tests schema generation via direct serializer call
    @Test
    public void testGetSchema_directSerializerCall_returnsJsonNode() throws Exception {
        DefaultSerializerProvider.Impl provider = createSerializerProvider();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(ser instanceof JsonValueSerializer);
        JsonValueSerializer jvSer = (JsonValueSerializer) ser;
        JsonNode node = jvSer.getSchema(provider, null);
        assertNotNull(node);
    }

    // Tests JSON format visitor introspection for standard class
    @Test
    public void testAcceptJsonFormatVisitor_standardClass_acceptsWithoutError() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
    }

    // Tests JSON format visitor introspection for enum with @JsonValue
    @Test
    public void testAcceptJsonFormatVisitor_enumClass_visitsEnumValues() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(ValueEnum.class, visitor);
    }

    // Tests JSON format visitor introspection for non-final class
    @Test
    public void testAcceptJsonFormatVisitor_nonFinalClass_acceptsWithoutError() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(NonFinalBean.class, visitor);
    }

    // Tests visitor failure on enum when accessor method throws exception
    @Test(expected = JsonMappingException.class)
    public void testAcceptJsonFormatVisitor_failingEnum_throwsJsonMappingException() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(FailingEnum.class, visitor);
    }

    // Tests toString method description
    @Test
    public void testToString_serializer_containsMethodInfo() throws Exception {
        DefaultSerializerProvider.Impl provider = createSerializerProvider();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(ser instanceof JsonValueSerializer);
        String desc = ser.toString();
        assertTrue(desc.contains("SimpleBean#getValue"));
    }

    // Tests withResolved method when same properties are passed returns this instance
    @Test
    public void testWithResolved_sameAttributes_returnsSameInstance() throws Exception {
        DefaultSerializerProvider.Impl provider = createSerializerProvider();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(ser instanceof JsonValueSerializer);
        JsonValueSerializer jvSer = (JsonValueSerializer) ser;
        JsonValueSerializer same = jvSer.withResolved(jvSer._property, jvSer._valueSerializer, jvSer._forceTypeInformation);
        assertSame(jvSer, same);
        JsonValueSerializer diff = jvSer.withResolved(jvSer._property, null, !jvSer._forceTypeInformation);
        assertNotSame(jvSer, diff);
    }
}
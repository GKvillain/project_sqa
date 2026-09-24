package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdSerializerTest {

    private static class ConcreteSerializer extends StdSerializer<String> {
        public ConcreteSerializer() {
            super(String.class);
        }

        public ConcreteSerializer(JavaType type) {
            super(type);
        }

        public ConcreteSerializer(Class<?> cls, boolean dummy) {
            super(cls, dummy);
        }

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) {
        }
    }

    @JacksonStdImpl
    private static class AnnotatedSerializer extends StdSerializer<Integer> {
        public AnnotatedSerializer() {
            super(Integer.class);
        }

        @Override
        public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider) {
        }
    }

    // Tests constructor with Class and handledType accessor
    @Test
    public void testHandledType_classConstructor_returnsCorrectClass() {
        ConcreteSerializer serializer = new ConcreteSerializer();
        assertEquals(String.class, serializer.handledType());
    }

    // Tests constructor with JavaType
    @Test
    public void testHandledType_javaTypeConstructor_returnsCorrectClass() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSerializer serializer = new ConcreteSerializer(type);
        assertEquals(String.class, serializer.handledType());
    }

    // Tests constructor with boolean dummy
    @Test
    public void testHandledType_dummyConstructor_returnsCorrectClass() {
        ConcreteSerializer serializer = new ConcreteSerializer(String.class, true);
        assertEquals(String.class, serializer.handledType());
    }

    // Tests getSchema without optional flag
    @Test
    public void testGetSchema_defaultSchema_returnsStringType() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        JsonNode schema = serializer.getSchema(null, null);
        assertNotNull(schema);
        assertTrue(schema instanceof ObjectNode);
        assertEquals("string", schema.get("type").asText());
        assertNull(schema.get("required"));
    }

    // Tests getSchema with optional true
    @Test
    public void testGetSchema_isOptionalTrue_doesNotSetRequired() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        JsonNode schema = serializer.getSchema(null, null, true);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
        assertNull(schema.get("required"));
    }

    // Tests getSchema with optional false
    @Test
    public void testGetSchema_isOptionalFalse_setsRequiredTrue() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        JsonNode schema = serializer.getSchema(null, null, false);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
        assertNotNull(schema.get("required"));
        assertTrue(schema.get("required").asBoolean());
    }

    // Tests createSchemaNode helper
    @Test
    public void testCreateSchemaNode_typeAndOptional_setsAttributesCorrectly() {
        ConcreteSerializer serializer = new ConcreteSerializer();
        ObjectNode schema = serializer.createSchemaNode("number", false);
        assertEquals("number", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());

        ObjectNode optSchema = serializer.createSchemaNode("number", true);
        assertEquals("number", optSchema.get("type").asText());
        assertNull(optSchema.get("required"));
    }

    // Tests acceptJsonFormatVisitor
    @Test
    public void testAcceptJsonFormatVisitor_invokesExpectAnyFormat() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor expectAnyFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };
        serializer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(String.class));
        assertTrue(visited[0]);
    }

    // Tests wrapAndThrow with InvocationTargetException wrapping Error
    @Test(expected = OutOfMemoryError.class)
    public void testWrapAndThrow_invocationTargetExceptionWithError_rethrowsError() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        InvocationTargetException ite = new InvocationTargetException(new OutOfMemoryError("test OOM"));
        serializer.wrapAndThrow(null, ite, this, "field");
    }

    // Tests wrapAndThrow with plain IOException (not JsonMappingException)
    @Test(expected = IOException.class)
    public void testWrapAndThrow_plainIOException_throwsAsIs() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        IOException ioe = new IOException("plain io exception");
        serializer.wrapAndThrow(null, ioe, this, "field");
    }

    // Tests wrapAndThrow with Exception when wrapping is enabled (field name)
    @Test
    public void testWrapAndThrow_regularException_wrapsInJsonMappingExceptionWithField() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        Exception ex = new Exception("inner exception");
        try {
            serializer.wrapAndThrow(null, ex, "beanObject", "fieldName");
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("fieldName"));
            assertEquals(ex, e.getCause());
        }
    }

    // Tests wrapAndThrow with Exception when wrapping is enabled (index)
    @Test
    public void testWrapAndThrow_regularException_wrapsInJsonMappingExceptionWithIndex() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        Exception ex = new Exception("inner exception");
        try {
            serializer.wrapAndThrow(null, ex, "beanObject", 5);
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("[5]"));
            assertEquals(ex, e.getCause());
        }
    }

    // Tests wrapAndThrow with Error directly and index
    @Test(expected = AssertionError.class)
    public void testWrapAndThrow_directErrorWithIndex_throwsAsIs() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        serializer.wrapAndThrow(null, new AssertionError("direct error"), this, 0);
    }

    // Tests isDefaultSerializer with and without JacksonStdImpl annotation
    @Test
    public void testIsDefaultSerializer_annotatedVsCustom() {
        ConcreteSerializer nonDefault = new ConcreteSerializer();
        AnnotatedSerializer defaultSer = new AnnotatedSerializer();

        assertFalse(nonDefault.isDefaultSerializer(nonDefault));
        assertTrue(nonDefault.isDefaultSerializer(defaultSer));
    }

    // Tests findPropertyFilter throws exception when FilterProvider is missing
    @Test(expected = JsonMappingException.class)
    public void testFindPropertyFilter_missingFilterProvider_throwsJsonMappingException() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        SerializerProvider provider = new DefaultSerializerProvider.Impl() {
            private static final long serialVersionUID = 1L;

            @Override
            public FilterProvider getFilterProvider() {
                return null;
            }
        };

        serializer.findPropertyFilter(provider, "filterId", "value");
    }

    // Tests findPropertyFilter resolves correctly with configured FilterProvider
    @Test
    public void testFindPropertyFilter_withConfiguredProvider_returnsFilterOrNull() throws Exception {
        ConcreteSerializer serializer = new ConcreteSerializer();
        final SimpleFilterProvider filters = new SimpleFilterProvider();
        SerializerProvider provider = new DefaultSerializerProvider.Impl() {
            private static final long serialVersionUID = 1L;

            @Override
            public FilterProvider getFilterProvider() {
                return filters;
            }
        };

        PropertyFilter filter = serializer.findPropertyFilter(provider, "unknownFilter", "value");
        assertNull(filter);
    }
}
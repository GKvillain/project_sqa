package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExternalTypeHandlerTest {

    private ObjectMapper mapper;

    // Helper classes for standard external type deserialization
    static class SimpleExternalBean {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object value;

        public SimpleExternalBean() {}
        public SimpleExternalBean(String type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    // Helper classes for polymorphic types
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
    interface Shape {}

    static class Circle implements Shape {
        public int radius;
        public Circle() {}
        public Circle(int radius) { this.radius = radius; }
    }

    static class Square implements Shape {
        public int size;
        public Square() {}
        public Square(int size) { this.size = size; }
    }

    static class ShapeContainer {
        public String extType;
        public Shape shape;
    }

    // Helper classes for defaultImpl test
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = DefaultPayload.class)
    interface Payload {}

    static class DefaultPayload implements Payload {
        public int val;
    }

    static class CustomPayload implements Payload {
        public int val;
    }

    static class PayloadContainer {
        public String type;
        public Payload payload;
    }

    // Helper class for Creator-based deserialization
    static class CreatorExternalBean {
        final String type;
        final Object value;

        public CreatorExternalBean(
                @com.fasterxml.jackson.annotation.JsonProperty("type") String type,
                @com.fasterxml.jackson.annotation.JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                Object value) {
            this.type = type;
            this.value = value;
        }
    }

    // Helper classes for Defects4J Bug 52 (external type id as creator parameter)
    static class Item52 {
        final String type;
        final Object data;

        @com.fasterxml.jackson.annotation.JsonCreator
        public Item52(
                @com.fasterxml.jackson.annotation.JsonProperty("type") String type,
                @com.fasterxml.jackson.annotation.JsonProperty("data")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                Object data) {
            this.type = type;
            this.data = data;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerSubtypes(Circle.class, Square.class, DefaultPayload.class, CustomPayload.class);
    }

    // Tests normal case where type comes before value property
    @Test
    public void testDeserialize_typeFirst_success() throws Exception {
        String json = "{\"extType\":\"Circle\",\"shape\":{\"radius\":5}}";
        ShapeContainer container = mapper.readValue(json, ShapeContainer.class);
        assertNotNull(container);
        assertEquals("Circle", container.extType);
        assertTrue(container.shape instanceof Circle);
        assertEquals(5, ((Circle) container.shape).radius);
    }

    // Tests normal case where value property comes before type property
    @Test
    public void testDeserialize_valueFirst_success() throws Exception {
        String json = "{\"shape\":{\"radius\":10},\"extType\":\"Circle\"}";
        ShapeContainer container = mapper.readValue(json, ShapeContainer.class);
        assertNotNull(container);
        assertEquals("Circle", container.extType);
        assertTrue(container.shape instanceof Circle);
        assertEquals(10, ((Circle) container.shape).radius);
    }

    // Tests natural scalar type deserialization without explicit type id
    @Test
    public void testDeserialize_naturalScalarType_success() throws Exception {
        String json = "{\"value\":\"some string\"}";
        SimpleExternalBean bean = mapper.readValue(json, SimpleExternalBean.class);
        assertNotNull(bean);
        assertEquals("some string", bean.value);
    }

    // Tests natural integer type deserialization without explicit type id
    @Test
    public void testDeserialize_naturalIntegerType_success() throws Exception {
        String json = "{\"value\":123}";
        SimpleExternalBean bean = mapper.readValue(json, SimpleExternalBean.class);
        assertNotNull(bean);
        assertEquals(123, bean.value);
    }

    // Tests null value handling when type is provided
    @Test
    public void testDeserialize_nullValue_success() throws Exception {
        String json = "{\"extType\":\"Circle\",\"shape\":null}";
        ShapeContainer container = mapper.readValue(json, ShapeContainer.class);
        assertNotNull(container);
        assertEquals("Circle", container.extType);
        assertNull(container.shape);
    }

    // Tests null value handling when value comes before type
    @Test
    public void testDeserialize_nullValueFirst_success() throws Exception {
        String json = "{\"shape\":null,\"extType\":\"Circle\"}";
        ShapeContainer container = mapper.readValue(json, ShapeContainer.class);
        assertNotNull(container);
        assertEquals("Circle", container.extType);
        assertNull(container.shape);
    }

    // Tests missing property exception when type id is given but property is absent
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingPropertyForTypeId_throwsException() throws Exception {
        String json = "{\"extType\":\"Circle\"}";
        mapper.readValue(json, ShapeContainer.class);
    }

    // Tests missing type id exception when property is present and not a natural type
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingTypeId_throwsException() throws Exception {
        String json = "{\"shape\":{\"radius\":5}}";
        mapper.readValue(json, ShapeContainer.class);
    }

    // Tests defaultImpl fallback when type id is missing
    @Test
    public void testDeserialize_defaultImplFallback_success() throws Exception {
        String json = "{\"payload\":{\"val\":42}}";
        PayloadContainer container = mapper.readValue(json, PayloadContainer.class);
        assertNotNull(container);
        assertTrue(container.payload instanceof DefaultPayload);
        assertEquals(42, ((DefaultPayload) container.payload).val);
    }

    // Tests explicit type override with defaultImpl present
    @Test
    public void testDeserialize_explicitTypeWithDefaultImpl_success() throws Exception {
        String json = "{\"type\":\"CustomPayload\",\"payload\":{\"val\":99}}";
        PayloadContainer container = mapper.readValue(json, PayloadContainer.class);
        assertNotNull(container);
        assertEquals("CustomPayload", container.type);
        assertTrue(container.payload instanceof CustomPayload);
        assertEquals(99, ((CustomPayload) container.payload).val);
    }

    // Tests creator with external type handler where type comes first
    @Test
    public void testCreator_typeFirst_success() throws Exception {
        String json = "{\"type\":\"Circle\",\"data\":{\"radius\":7}}";
        Item52 item = mapper.readValue(json, Item52.class);
        assertNotNull(item);
        assertEquals("Circle", item.type);
        assertTrue(item.data instanceof Circle);
        assertEquals(7, ((Circle) item.data).radius);
    }

    // Tests Defects4J 52b: creator with external type handler where value comes before type
    @Test
    public void testCreator_valueFirst_success() throws Exception {
        String json = "{\"data\":{\"radius\":8},\"type\":\"Circle\"}";
        Item52 item = mapper.readValue(json, Item52.class);
        assertNotNull(item);
        assertEquals("Circle", item.type);
        assertTrue(item.data instanceof Circle);
        assertEquals(8, ((Circle) item.data).radius);
    }

    // Tests builder and start method behavior directly
    @Test
    public void testStart_createsFreshInstance() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests handlePropertyValue returns false for unregistered property name
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws Exception {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();

        JsonParser parser = mapper.getFactory().createParser("{\"unknown\":123}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "unknown"
        parser.nextToken(); // VALUE_NUMBER_INT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknown", new Object());
        assertFalse(handled);
        parser.close();
    }

    // Tests handleTypePropertyValue returns false for unregistered property name
    @Test
    public void testHandleTypePropertyValue_unknownProperty_returnsFalse() throws Exception {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();

        JsonParser parser = mapper.getFactory().createParser("{\"unknown\":\"val\"}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknown", new Object());
        assertFalse(handled);
        parser.close();
    }

    // Tests complete method when both type and property are missing (empty JSON)
    @Test
    public void testDeserialize_emptyObject_success() throws Exception {
        String json = "{}";
        ShapeContainer container = mapper.readValue(json, ShapeContainer.class);
        assertNotNull(container);
        assertNull(container.extType);
        assertNull(container.shape);
    }
}
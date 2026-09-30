package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ExternalTypeHandlerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Helper classes for testing external type deserialization

    interface BasePoly {
    }

    static class ImplOne implements BasePoly {
        public int x;

        public ImplOne() {
        }

        public ImplOne(int x) {
            this.x = x;
        }
    }

    static class ImplTwo implements BasePoly {
        public String y;

        public ImplTwo() {
        }

        public ImplTwo(String y) {
            this.y = y;
        }
    }

    static class StandardContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ImplOne.class, name = "one"),
                @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
        })
        public BasePoly value;
    }

    static class DefaultImplContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = ImplOne.class)
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
        })
        public BasePoly value;
    }

    static class CreatorContainer {
        public final String type;
        public final BasePoly value;

        @JsonCreator
        public CreatorContainer(
                @JsonProperty("type") String type,
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                        @JsonSubTypes.Type(value = ImplOne.class, name = "one"),
                        @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
                })
                BasePoly value) {
            this.type = type;
            this.value = value;
        }
    }

    static class CreatorWithDefaultContainer {
        public final String type;
        public final BasePoly value;

        @JsonCreator
        public CreatorWithDefaultContainer(
                @JsonProperty("type") String type,
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = ImplOne.class)
                @JsonSubTypes({
                        @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
                })
                BasePoly value) {
            this.type = type;
            this.value = value;
        }
    }

    static class MultiPropertyContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ImplOne.class, name = "one"),
                @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
        })
        public BasePoly val1;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ImplOne.class, name = "one"),
                @JsonSubTypes.Type(value = ImplTwo.class, name = "two")
        })
        public BasePoly val2;
    }

    // Tests builder initialization and start method
    @Test
    public void testBuilder_createsHandlerAndStartReturnsInstance() {
        JavaType javaType = TypeFactory.defaultInstance().constructType(StandardContainer.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);
        assertNotNull(builder);

        BeanPropertyMap emptyMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, List<PropertyName>>emptyMap());
        ExternalTypeHandler handler = builder.build(emptyMap);
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests unknown property handling in handleTypePropertyValue
    @Test
    public void testHandleTypePropertyValue_unknownProperty_returnsFalse() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(StandardContainer.class);
        BeanPropertyMap emptyMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, List<PropertyName>>emptyMap());
        ExternalTypeHandler handler = ExternalTypeHandler.builder(javaType).build(emptyMap).start();

        JsonParser parser = mapper.getFactory().createParser("{\"unknown\": \"val\"}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "nonExistentProp", new Object());
        assertFalse(handled);
        parser.close();
    }

    // Tests unknown property handling in handlePropertyValue
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(StandardContainer.class);
        BeanPropertyMap emptyMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false, Collections.<String, List<PropertyName>>emptyMap());
        ExternalTypeHandler handler = ExternalTypeHandler.builder(javaType).build(emptyMap).start();

        JsonParser parser = mapper.getFactory().createParser("{\"unknown\": 123}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handlePropertyValue(parser, ctxt, "nonExistentProp", new Object());
        assertFalse(handled);
        parser.close();
    }

    // Tests standard deserialization when type property comes before value property
    @Test
    public void testComplete_typeFirst_deserializesCorrectly() throws Exception {
        String json = "{\"type\":\"one\",\"value\":{\"x\":42}}";
        StandardContainer container = mapper.readValue(json, StandardContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertTrue(container.value instanceof ImplOne);
        assertEquals(42, ((ImplOne) container.value).x);
    }

    // Tests standard deserialization when value property comes before type property
    @Test
    public void testComplete_valueFirst_deserializesCorrectly() throws Exception {
        String json = "{\"value\":{\"y\":\"hello\"},\"type\":\"two\"}";
        StandardContainer container = mapper.readValue(json, StandardContainer.class);

        assertNotNull(container);
        assertEquals("two", container.type);
        assertTrue(container.value instanceof ImplTwo);
        assertEquals("hello", ((ImplTwo) container.value).y);
    }

    // Tests deserialization when value is null
    @Test
    public void testComplete_nullValue_handledGracefully() throws Exception {
        String json = "{\"type\":\"one\",\"value\":null}";
        StandardContainer container = mapper.readValue(json, StandardContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertNull(container.value);
    }

    // Tests deserialization with creator-based class
    @Test
    public void testComplete_withCreatorProperties_deserializesCorrectly() throws Exception {
        String json = "{\"type\":\"one\",\"value\":{\"x\":99}}";
        CreatorContainer container = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertTrue(container.value instanceof ImplOne);
        assertEquals(99, ((ImplOne) container.value).x);
    }

    // Tests deserialization with creator-based class and value before type
    @Test
    public void testComplete_withCreatorPropertiesValueFirst_deserializesCorrectly() throws Exception {
        String json = "{\"value\":{\"y\":\"creator\"},\"type\":\"two\"}";
        CreatorContainer container = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(container);
        assertEquals("two", container.type);
        assertTrue(container.value instanceof ImplTwo);
        assertEquals("creator", ((ImplTwo) container.value).y);
    }

    // Tests defaultImpl support when type is missing for standard bean
    @Test
    public void testComplete_missingTypeWithDefaultImpl_usesDefault() throws Exception {
        String json = "{\"value\":{\"x\":7}}";
        DefaultImplContainer container = mapper.readValue(json, DefaultImplContainer.class);

        assertNotNull(container);
        assertTrue(container.value instanceof ImplOne);
        assertEquals(7, ((ImplOne) container.value).x);
    }

    // Tests defaultImpl support when type is missing for creator-based bean
    @Test
    public void testComplete_missingTypeWithDefaultImplOnCreator_usesDefault() throws Exception {
        String json = "{\"value\":{\"x\":15}}";
        CreatorWithDefaultContainer container = mapper.readValue(json, CreatorWithDefaultContainer.class);

        assertNotNull(container);
        assertTrue(container.value instanceof ImplOne);
        assertEquals(15, ((ImplOne) container.value).x);
    }

    // Tests exception when type is missing and no defaultImpl is configured
    @Test(expected = JsonMappingException.class)
    public void testComplete_missingTypeNoDefault_throwsException() throws Exception {
        String json = "{\"value\":{\"x\":10}}";
        mapper.readValue(json, StandardContainer.class);
    }

    // Tests multiple external properties sharing same type property
    @Test
    public void testComplete_multiplePropertiesSharedTypeId_deserializesBoth() throws Exception {
        String json = "{\"type\":\"one\",\"val1\":{\"x\":1},\"val2\":{\"x\":2}}";
        MultiPropertyContainer container = mapper.readValue(json, MultiPropertyContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertTrue(container.val1 instanceof ImplOne);
        assertEquals(1, ((ImplOne) container.val1).x);
        assertTrue(container.val2 instanceof ImplOne);
        assertEquals(2, ((ImplOne) container.val2).x);
    }

    // Tests multiple external properties when values appear before type id
    @Test
    public void testComplete_multiplePropertiesValuesBeforeType_deserializesBoth() throws Exception {
        String json = "{\"val1\":{\"x\":10},\"val2\":{\"x\":20},\"type\":\"one\"}";
        MultiPropertyContainer container = mapper.readValue(json, MultiPropertyContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertTrue(container.val1 instanceof ImplOne);
        assertEquals(10, ((ImplOne) container.val1).x);
        assertTrue(container.val2 instanceof ImplOne);
        assertEquals(2, ((ImplOne) container.val2).x);
    }

    // Tests missing property with FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY enabled throws exception
    @Test(expected = JsonMappingException.class)
    public void testComplete_missingValueWhenTypePresentAndFeatureEnabled_throwsException() throws Exception {
        mapper.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        String json = "{\"type\":\"one\"}";
        mapper.readValue(json, StandardContainer.class);
    }

    // Tests missing property with FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY disabled succeeds
    @Test
    public void testComplete_missingValueWhenTypePresentAndFeatureDisabled_returnsBean() throws Exception {
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        String json = "{\"type\":\"one\"}";
        StandardContainer container = mapper.readValue(json, StandardContainer.class);

        assertNotNull(container);
        assertEquals("one", container.type);
        assertNull(container.value);
    }
}
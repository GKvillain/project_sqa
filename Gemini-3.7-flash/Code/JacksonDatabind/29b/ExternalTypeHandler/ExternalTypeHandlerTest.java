package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalTypeHandlerTest {

    static class Container {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubA.class, name = "a"),
            @JsonSubTypes.Type(value = SubB.class, name = "b")
        })
        public Base value;
    }

    static class ContainerWithDefault {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = SubA.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubA.class, name = "a"),
            @JsonSubTypes.Type(value = SubB.class, name = "b")
        })
        public Base value;
    }

    static class CreatorContainer {
        public final String type;
        public final Base value;

        @JsonCreator
        public CreatorContainer(@JsonProperty("type") String type,
                                @JsonProperty("value")
                                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                                @JsonSubTypes({
                                    @JsonSubTypes.Type(value = SubA.class, name = "a"),
                                    @JsonSubTypes.Type(value = SubB.class, name = "b")
                                })
                                Base value) {
            this.type = type;
            this.value = value;
        }
    }

    static class NaturalContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object value;
    }

    interface Base {}

    @JsonTypeName("a")
    static class SubA implements Base {
        public int x;
    }

    @JsonTypeName("b")
    static class SubB implements Base {
        public String y;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests Builder and start method creation
    @Test
    public void testBuilderAndStart_emptyBuilder_createsHandler() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests handlePropertyValue with unknown property name returns false
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build().start();

        JsonParser jp = mapper.getFactory().createParser("{\"unknown\":\"value\"}");
        jp.nextToken();
        jp.nextToken();
        jp.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handlePropertyValue(jp, ctxt, "unknown", null);
        assertFalse(handled);
        jp.close();
    }

    // Tests handleTypePropertyValue with unknown property name returns false
    @Test
    public void testHandleTypePropertyValue_unknownProperty_returnsFalse() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build().start();

        JsonParser jp = mapper.getFactory().createParser("\"typeName\"");
        jp.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        boolean handled = handler.handleTypePropertyValue(jp, ctxt, "unknownType", null);
        assertFalse(handled);
        jp.close();
    }

    // Tests normal deserialization when type id property appears before value property
    @Test
    public void testDeserialize_typeBeforeValue_deserializesCorrectly() throws Exception {
        String json = "{\"type\":\"a\",\"value\":{\"x\":42}}";
        Container result = mapper.readValue(json, Container.class);

        assertNotNull(result);
        assertEquals("a", result.type);
        assertTrue(result.value instanceof SubA);
        assertEquals(42, ((SubA) result.value).x);
    }

    // Tests normal deserialization when value property appears before type id property
    @Test
    public void testDeserialize_valueBeforeType_deserializesCorrectly() throws Exception {
        String json = "{\"value\":{\"y\":\"hello\"},\"type\":\"b\"}";
        Container result = mapper.readValue(json, Container.class);

        assertNotNull(result);
        assertEquals("b", result.type);
        assertTrue(result.value instanceof SubB);
        assertEquals("hello", ((SubB) result.value).y);
    }

    // Tests deserialization when both external type id and value are missing
    @Test
    public void testDeserialize_bothPropertiesMissing_leavesNull() throws Exception {
        String json = "{}";
        Container result = mapper.readValue(json, Container.class);

        assertNotNull(result);
        assertNull(result.type);
        assertNull(result.value);
    }

    // Tests exception path when value is present but external type id is missing without defaultImpl
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingTypeId_throwsException() throws Exception {
        String json = "{\"value\":{\"x\":1}}";
        mapper.readValue(json, Container.class);
    }

    // Tests exception path when type id is present but value property is missing
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingValueProperty_throwsException() throws Exception {
        String json = "{\"type\":\"a\"}";
        mapper.readValue(json, Container.class);
    }

    // Tests creator-based deserialization when type id appears before value
    @Test
    public void testDeserialize_creatorContainer_typeBeforeValue_deserializesCorrectly() throws Exception {
        String json = "{\"type\":\"a\",\"value\":{\"x\":10}}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertEquals("a", result.type);
        assertTrue(result.value instanceof SubA);
        assertEquals(10, ((SubA) result.value).x);
    }

    // Tests creator-based deserialization when value appears before type id
    @Test
    public void testDeserialize_creatorContainer_valueBeforeType_deserializesCorrectly() throws Exception {
        String json = "{\"value\":{\"y\":\"creator\"},\"type\":\"b\"}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertEquals("b", result.type);
        assertTrue(result.value instanceof SubB);
        assertEquals("creator", ((SubB) result.value).y);
    }

    // Tests exception path for creator-based deserialization when value is present but type id is missing
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorContainer_missingTypeId_throwsException() throws Exception {
        String json = "{\"value\":{\"x\":5}}";
        mapper.readValue(json, CreatorContainer.class);
    }

    // Tests exception path for creator-based deserialization when type id is present but value is missing
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorContainer_missingValue_throwsException() throws Exception {
        String json = "{\"type\":\"a\"}";
        mapper.readValue(json, CreatorContainer.class);
    }

    // Tests defaultImpl fallback when external type id is missing
    @Test
    public void testDeserialize_defaultImpl_missingTypeId_usesDefaultImpl() throws Exception {
        String json = "{\"value\":{\"x\":99}}";
        ContainerWithDefault result = mapper.readValue(json, ContainerWithDefault.class);

        assertNotNull(result);
        assertTrue(result.value instanceof SubA);
        assertEquals(99, ((SubA) result.value).x);
    }

    // Tests natural scalar type deserialization when no type id is provided
    @Test
    public void testDeserialize_naturalScalarType_deserializesWithoutTypeId() throws Exception {
        String json = "{\"value\":\"naturalString\"}";
        NaturalContainer result = mapper.readValue(json, NaturalContainer.class);

        assertNotNull(result);
        assertEquals("naturalString", result.value);
    }
}
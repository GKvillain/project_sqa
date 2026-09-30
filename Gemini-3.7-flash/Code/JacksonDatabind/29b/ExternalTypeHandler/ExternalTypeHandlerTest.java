package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalTypeHandlerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    interface BaseValue {}

    static class SubValueA implements BaseValue {
        public int x;
        public SubValueA() {}
        public SubValueA(int x) { this.x = x; }
    }

    static class SubValueB implements BaseValue {
        public String y;
        public SubValueB() {}
        public SubValueB(String y) { this.y = y; }
    }

    static class ExternalBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubValueA.class, name = "typeA"),
            @JsonSubTypes.Type(value = SubValueB.class, name = "typeB")
        })
        public BaseValue value;

        public String name;
    }

    static class DefaultImplBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType", defaultImpl = SubValueA.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubValueB.class, name = "typeB")
        })
        public BaseValue value;
    }

    static class CreatorExternalBean {
        public final BaseValue value;
        public final String name;

        @JsonCreator
        public CreatorExternalBean(
                @JsonProperty("name") String name,
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = SubValueA.class, name = "typeA"),
                    @JsonSubTypes.Type(value = SubValueB.class, name = "typeB")
                })
                BaseValue value) {
            this.name = name;
            this.value = value;
        }
    }

    static class CreatorDefaultImplBean {
        public final BaseValue value;
        public final String name;

        @JsonCreator
        public CreatorDefaultImplBean(
                @JsonProperty("name") String name,
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = SubValueA.class)
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = SubValueB.class, name = "typeB")
                })
                BaseValue value) {
            this.name = name;
            this.value = value;
        }
    }

    // Tests Builder and start() method instantiating a fresh ExternalTypeHandler
    @Test
    public void testStart_createsDistinctInstance() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests handleTypePropertyValue with unknown property returns false
    @Test
    public void testHandleTypePropertyValue_unknownProperty_returnsFalse() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING

        DeserializationContext ctxt = mapper.getDeserializationContext();
        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknownProp", new Object());
        assertFalse(handled);
    }

    // Tests handlePropertyValue with unknown property returns false
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build().start();

        JsonParser parser = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING

        DeserializationContext ctxt = mapper.getDeserializationContext();
        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknownProp", new Object());
        assertFalse(handled);
    }

    // Tests normal deserialization where type id property appears before value property
    @Test
    public void testDeserialize_typeIdBeforeValue_success() throws Exception {
        String json = "{\"name\":\"test\",\"extType\":\"typeA\",\"value\":{\"x\":42}}";
        ExternalBean bean = mapper.readValue(json, ExternalBean.class);

        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertTrue(bean.value instanceof SubValueA);
        assertEquals(42, ((SubValueA) bean.value).x);
    }

    // Tests normal deserialization where value property appears before type id property
    @Test
    public void testDeserialize_valueBeforeTypeId_success() throws Exception {
        String json = "{\"value\":{\"y\":\"hello\"},\"extType\":\"typeB\",\"name\":\"test\"}";
        ExternalBean bean = mapper.readValue(json, ExternalBean.class);

        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertTrue(bean.value instanceof SubValueB);
        assertEquals("hello", ((SubValueB) bean.value).y);
    }

    // Tests missing both type id and property leaves target property as null
    @Test
    public void testDeserialize_missingBothTypeAndProperty_leavesNull() throws Exception {
        String json = "{\"name\":\"onlyName\"}";
        ExternalBean bean = mapper.readValue(json, ExternalBean.class);

        assertNotNull(bean);
        assertEquals("onlyName", bean.name);
        assertNull(bean.value);
    }

    // Tests missing value property when external type id is present throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingPropertyWhenTypeIdPresent_throwsException() throws Exception {
        String json = "{\"name\":\"test\",\"extType\":\"typeA\"}";
        mapper.readValue(json, ExternalBean.class);
    }

    // Tests missing external type id when value property is present throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingTypeIdWhenPropertyPresent_throwsException() throws Exception {
        String json = "{\"name\":\"test\",\"value\":{\"x\":123}}";
        mapper.readValue(json, ExternalBean.class);
    }

    // Tests missing external type id falls back to defaultImpl when configured
    @Test
    public void testDeserialize_missingTypeIdWithDefaultImpl_usesDefault() throws Exception {
        String json = "{\"value\":{\"x\":99}}";
        DefaultImplBean bean = mapper.readValue(json, DefaultImplBean.class);

        assertNotNull(bean);
        assertTrue(bean.value instanceof SubValueA);
        assertEquals(99, ((SubValueA) bean.value).x);
    }

    // Tests creator-based deserialization with type id before value
    @Test
    public void testDeserialize_creatorPropertyTypeBeforeValue_success() throws Exception {
        String json = "{\"name\":\"creatorTest\",\"type\":\"typeA\",\"value\":{\"x\":7}}";
        CreatorExternalBean bean = mapper.readValue(json, CreatorExternalBean.class);

        assertNotNull(bean);
        assertEquals("creatorTest", bean.name);
        assertTrue(bean.value instanceof SubValueA);
        assertEquals(7, ((SubValueA) bean.value).x);
    }

    // Tests creator-based deserialization with value before type id
    @Test
    public void testDeserialize_creatorPropertyValueBeforeType_success() throws Exception {
        String json = "{\"value\":{\"y\":\"creatorValue\"},\"type\":\"typeB\",\"name\":\"creatorTest\"}";
        CreatorExternalBean bean = mapper.readValue(json, CreatorExternalBean.class);

        assertNotNull(bean);
        assertEquals("creatorTest", bean.name);
        assertTrue(bean.value instanceof SubValueB);
        assertEquals("creatorValue", ((SubValueB) bean.value).y);
    }

    // Tests creator-based deserialization when both property and type id are omitted
    @Test
    public void testDeserialize_creatorMissingBothTypeAndProperty_success() throws Exception {
        String json = "{\"name\":\"onlyName\"}";
        CreatorExternalBean bean = mapper.readValue(json, CreatorExternalBean.class);

        assertNotNull(bean);
        assertEquals("onlyName", bean.name);
        assertNull(bean.value);
    }

    // Tests creator-based deserialization when value is missing but type id is present throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorMissingPropertyWhenTypeIdPresent_throwsException() throws Exception {
        String json = "{\"name\":\"test\",\"type\":\"typeA\"}";
        mapper.readValue(json, CreatorExternalBean.class);
    }

    // Tests creator-based deserialization when type id is missing but value is present throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorMissingTypeIdWhenPropertyPresent_throwsException() throws Exception {
        String json = "{\"name\":\"test\",\"value\":{\"x\":123}}";
        mapper.readValue(json, CreatorExternalBean.class);
    }

    // Tests creator-based deserialization with defaultImpl when type id is missing
    @Test
    public void testDeserialize_creatorMissingTypeIdWithDefaultImpl_usesDefault() throws Exception {
        String json = "{\"name\":\"test\",\"value\":{\"x\":55}}";
        CreatorDefaultImplBean bean = mapper.readValue(json, CreatorDefaultImplBean.class);

        assertNotNull(bean);
        assertEquals("test", bean.name);
        assertTrue(bean.value instanceof SubValueA);
        assertEquals(55, ((SubValueA) bean.value).x);
    }

    // Tests handling null value with external type id
    @Test
    public void testDeserialize_nullValueWithTypeId_setsNull() throws Exception {
        String json = "{\"extType\":\"typeA\",\"value\":null,\"name\":\"nullTest\"}";
        ExternalBean bean = mapper.readValue(json, ExternalBean.class);

        assertNotNull(bean);
        assertEquals("nullTest", bean.name);
        assertNull(bean.value);
    }

    // Tests handling null value when value appears before type id
    @Test
    public void testDeserialize_nullValueBeforeTypeId_setsNull() throws Exception {
        String json = "{\"value\":null,\"extType\":\"typeB\",\"name\":\"nullTest\"}";
        ExternalBean bean = mapper.readValue(json, ExternalBean.class);

        assertNotNull(bean);
        assertEquals("nullTest", bean.name);
        assertNull(bean.value);
    }
}
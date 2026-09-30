package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

public class ObjectIdValuePropertyTest {

    private ObjectMapper mapper;
    private JavaType stringType;
    private JsonDeserializer<Object> stringDeserializer;
    private ObjectIdReader defaultObjectIdReader;

    @Retention(RetentionPolicy.RUNTIME)
    private @interface DummyAnnotation {
    }

    static class SimpleModel {
        public String id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedModel {
        public String id;
        public String name;

        public IdentifiedModel() {}

        public IdentifiedModel(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        stringType = mapper.constructType(String.class);
        stringDeserializer = (JsonDeserializer<Object>) mapper.getDeserializationConfig()
                .findRootValueDeserializer(stringType);
        defaultObjectIdReader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(),
                stringDeserializer,
                null,
                new SimpleObjectIdResolver()
        );
    }

    // Tests constructor initialization and metadata retrieval
    @Test
    public void testConstructor_validReaderAndMetadata_initializedProperly() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);

        assertEquals("id", prop.getName());
        assertEquals(PropertyMetadata.STD_REQUIRED, prop.getMetadata());
        assertEquals(stringType, prop.getType());
        assertSame(stringDeserializer, prop.getValueDeserializer());
        assertSame(defaultObjectIdReader, prop._objectIdReader);
    }

    // Tests getAnnotation always returns null
    @Test
    public void testGetAnnotation_anyAnnotationClass_returnsNull() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);

        assertNull(prop.getAnnotation(DummyAnnotation.class));
        assertNull(prop.getAnnotation(Override.class));
    }

    // Tests getMember always returns null
    @Test
    public void testGetMember_noMemberAssociated_returnsNull() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_OPTIONAL);

        assertNull(prop.getMember());
    }

    // Tests withName creates a new copy with the updated PropertyName
    @Test
    public void testWithName_newPropertyName_returnsNewInstanceWithNewName() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        PropertyName newName = new PropertyName("customId");

        ObjectIdValueProperty renamedProp = prop.withName(newName);

        assertNotNull(renamedProp);
        assertNotSame(prop, renamedProp);
        assertEquals("customId", renamedProp.getName());
        assertSame(defaultObjectIdReader, renamedProp._objectIdReader);
    }

    // Tests withValueDeserializer creates a new copy with updated deserializer
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstanceWithDeserializer() {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        JsonDeserializer<?> dummyDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "dummy";
            }
        };

        ObjectIdValueProperty updatedProp = prop.withValueDeserializer(dummyDeser);

        assertNotNull(updatedProp);
        assertNotSame(prop, updatedProp);
        assertSame(dummyDeser, updatedProp.getValueDeserializer());
        assertSame(defaultObjectIdReader, updatedProp._objectIdReader);
    }

    // Tests set throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        prop.set(new Object(), "test-id");
    }

    // Tests setAndReturn throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        prop.setAndReturn(new Object(), "test-id");
    }

    // Tests deserializeAndSet and deserializeSetAndReturn without underlying idProperty
    @Test
    public void testDeserializeSetAndReturn_noIdProperty_bindsIdAndReturnsInstance() throws Exception {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        JsonParser parser = mapper.getFactory().createParser("\"12345\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SimpleModel instance = new SimpleModel();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
        assertNull(instance.id);
        parser.close();
    }

    // Tests deserializeAndSet execution path
    @Test
    public void testDeserializeAndSet_validParser_executesWithoutException() throws Exception {
        ObjectIdValueProperty prop = new ObjectIdValueProperty(defaultObjectIdReader, PropertyMetadata.STD_REQUIRED);
        JsonParser parser = mapper.getFactory().createParser("\"id-999\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SimpleModel instance = new SimpleModel();
        prop.deserializeAndSet(parser, ctxt, instance);

        assertNull(instance.id);
        parser.close();
    }

    // Tests deserialization of object with @JsonIdentityInfo end-to-end
    @Test
    public void testDeserialize_identifiedObject_resolvesObjectId() throws Exception {
        String json = "{\"id\":\"abc\",\"name\":\"testObject\"}";
        IdentifiedModel result = mapper.readValue(json, IdentifiedModel.class);

        assertNotNull(result);
        assertEquals("abc", result.id);
        assertEquals("testObject", result.name);
    }

    // Tests deserialization when ObjectId is null in JSON payload
    @Test
    public void testDeserialize_nullObjectId_handlesGracefully() throws Exception {
        String json = "{\"id\":null,\"name\":\"nullIdObject\"}";
        IdentifiedModel result = mapper.readValue(json, IdentifiedModel.class);

        assertNotNull(result);
        assertNull(result.id);
        assertEquals("nullIdObject", result.name);
    }
}
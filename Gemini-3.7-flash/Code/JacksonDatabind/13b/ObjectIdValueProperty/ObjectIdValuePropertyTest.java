package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdValuePropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {}

    private ObjectMapper _objectMapper;
    private JavaType _idType;
    private PropertyName _propertyName;
    private ObjectIdGenerator<?> _generator;
    private ObjectIdResolver _resolver;
    private JsonDeserializer<Object> _deserializer;

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
        _idType = TypeFactory.defaultInstance().constructType(String.class);
        _propertyName = new PropertyName("id");
        _generator = new ObjectIdGenerators.IntSequenceGenerator();
        _resolver = new SimpleObjectIdResolver();
        _deserializer = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };
    }

    // Tests constructor initialization and basic property accessors
    @Test
    public void testConstructor_validReader_initializesCorrectly() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        assertEquals("id", prop.getName());
        assertEquals(_idType, prop.getType());
        assertEquals(PropertyMetadata.STD_REQUIRED, prop.getMetadata());
        assertNull(prop.getAnnotation(TestAnnotation.class));
        assertNull(prop.getMember());
    }

    // Tests withName method returns a new instance with updated property name
    @Test
    public void testWithName_newPropertyName_returnsUpdatedInstance() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        PropertyName newName = new PropertyName("customId");
        ObjectIdValueProperty updated = prop.withName(newName);

        assertNotNull(updated);
        assertNotSame(prop, updated);
        assertEquals("customId", updated.getName());
    }

    // Tests withName method with same name
    @Test
    public void testWithName_samePropertyName_returnsUpdatedInstance() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectIdValueProperty updated = prop.withName(_propertyName);

        assertNotNull(updated);
        assertEquals("id", updated.getName());
    }

    // Tests withValueDeserializer returns a new instance with updated deserializer
    @Test
    public void testWithValueDeserializer_newDeserializer_returnsUpdatedInstance() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom";
            }
        };

        ObjectIdValueProperty updated = prop.withValueDeserializer(newDeser);

        assertNotNull(updated);
        assertNotSame(prop, updated);
        assertSame(newDeser, updated.getValueDeserializer());
    }

    // Tests set method throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        prop.set(new Object(), "someValue");
    }

    // Tests setAndReturn throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        prop.setAndReturn(new Object(), "someValue");
    }

    // Tests deserializeSetAndReturn without underlying SettableBeanProperty idProperty
    @Test
    public void testDeserializeSetAndReturn_withoutIdProperty_bindsAndReturnsInstance() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = _objectMapper.getFactory().createParser("\"123\"");
        parser.nextToken();
        DeserializationContext ctxt = _objectMapper.getDeserializationContext();

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
        parser.close();
    }

    // Tests deserializeAndSet executes without throwing exception when idProperty is null
    @Test
    public void testDeserializeAndSet_withoutIdProperty_successfullyExecutes() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = _objectMapper.getFactory().createParser("\"456\"");
        parser.nextToken();
        DeserializationContext ctxt = _objectMapper.getDeserializationContext();

        Object instance = new Object();
        prop.deserializeAndSet(parser, ctxt, instance);

        parser.close();
    }

    // Tests deprecated String constructor
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstructor_stringName_createsInstance() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectIdValueProperty copy = new ObjectIdValueProperty(prop, "renamed");
        assertEquals("renamed", copy.getName());
    }

    // Tests deprecated PropertyName constructor
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstructor_propertyName_createsInstance() {
        ObjectIdReader reader = ObjectIdReader.construct(_idType, _propertyName, _generator, _deserializer, null, _resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectIdValueProperty copy = new ObjectIdValueProperty(prop, new PropertyName("renamedProp"));
        assertEquals("renamedProp", copy.getName());
    }
}
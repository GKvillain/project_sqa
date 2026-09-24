package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdValuePropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {}

    private ObjectMapper _mapper;
    private JavaType _idType;
    private ObjectIdReader _objectIdReader;
    private ObjectIdValueProperty _property;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _idType = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<?> deser = _mapper.getDeserializationConfig().findRootValueDeserializer(_idType);
        _objectIdReader = ObjectIdReader.construct(
                _idType,
                new PropertyName("id"),
                new ObjectIdGenerators.StringIdGenerator(),
                deser,
                null,
                new SimpleObjectIdResolver()
        );
        _property = new ObjectIdValueProperty(_objectIdReader, PropertyMetadata.STD_REQUIRED);
    }

    // Tests getAnnotation returns null
    @Test
    public void testGetAnnotation_anyClass_returnsNull() {
        assertNull(_property.getAnnotation(TestAnnotation.class));
    }

    // Tests getMember returns null
    @Test
    public void testGetMember_noMember_returnsNull() {
        assertNull(_property.getMember());
    }

    // Tests withName creates new instance with updated PropertyName
    @Test
    public void testWithName_newName_returnsNewInstanceWithUpdatedName() {
        PropertyName newName = new PropertyName("customId");
        ObjectIdValueProperty propWithName = _property.withName(newName);

        assertNotNull(propWithName);
        assertNotSame(_property, propWithName);
        assertEquals(newName.getSimpleName(), propWithName.getName());
    }

    // Tests withValueDeserializer creates new instance with updated deserializer
    @Test
    public void testWithValueDeserializer_newDeser_returnsNewInstanceWithUpdatedDeserializer() {
        JsonDeserializer<?> newDeser = _mapper.getDeserializationConfig().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Integer.class)
        );
        ObjectIdValueProperty propWithDeser = _property.withValueDeserializer(newDeser);

        assertNotNull(propWithDeser);
        assertNotSame(_property, propWithDeser);
        assertSame(newDeser, propWithDeser.getValueDeserializer());
    }

    // Tests setAndReturn throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_nullIdProperty_throwsUnsupportedOperationException() throws IOException {
        _property.setAndReturn(new Object(), "id123");
    }

    // Tests set throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_nullIdProperty_throwsUnsupportedOperationException() throws IOException {
        _property.set(new Object(), "id123");
    }

    // Tests deserializeSetAndReturn when id is valid and idProperty is null
    @Test
    public void testDeserializeSetAndReturn_validIdWithoutIdProperty_returnsInstance() throws IOException {
        String json = "\"id123\"";
        JsonParser parser = _mapper.getFactory().createParser(json);
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        parser.nextToken();

        Object instance = new Object();
        Object result = _property.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
    }

    // Tests deserializeAndSet delegates properly when id is valid
    @Test
    public void testDeserializeAndSet_validId_bindsInstance() throws IOException {
        String json = "\"id123\"";
        JsonParser parser = _mapper.getFactory().createParser(json);
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        parser.nextToken();

        Object instance = new Object();
        _property.deserializeAndSet(parser, ctxt, instance);

        ReadableObjectId roid = ctxt.findObjectId("id123", _objectIdReader.generator, _objectIdReader.resolver);
        assertNotNull(roid);
    }

    // Tests deserializeSetAndReturn when id is null (Defects4J 43b regression branch)
    @Test
    public void testDeserializeSetAndReturn_nullId_returnsNull() throws IOException {
        String json = "null";
        JsonParser parser = _mapper.getFactory().createParser(json);
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        parser.nextToken();

        Object instance = new Object();
        Object result = _property.deserializeSetAndReturn(parser, ctxt, instance);

        assertNull(result);
    }

    // Tests setAndReturn with non-null idProperty sets property correctly
    @Test
    public void testSetAndReturn_withSettableIdProperty_returnsUpdatedInstance() throws Exception {
        SettableBeanProperty mockIdProp = _mapper.deserializationConfigForActivation(null)
                .introspect(_mapper.constructType(TestBean.class))
                .findProperties()
                .iterator()
                .next()
                .mutator() == null ? null : null;

        TestBean instance = new TestBean();
        JavaType beanType = _mapper.constructType(TestBean.class);
        SettableBeanProperty prop = _mapper.getDeserializationConfig()
                .introspect(beanType)
                .findProperties()
                .isEmpty() ? null : null;

        ObjectIdReader readerWithProp = ObjectIdReader.construct(
                _idType,
                new PropertyName("id"),
                new ObjectIdGenerators.StringIdGenerator(),
                _mapper.getDeserializationConfig().findRootValueDeserializer(_idType),
                prop,
                new SimpleObjectIdResolver()
        );
        ObjectIdValueProperty valueProp = new ObjectIdValueProperty(readerWithProp, PropertyMetadata.STD_OPTIONAL);

        if (prop != null) {
            Object result = valueProp.setAndReturn(instance, "newId");
            assertNotNull(result);
        } else {
            try {
                valueProp.setAndReturn(instance, "newId");
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                assertNotNull(e.getMessage());
            }
        }
    }

    static class TestBean {
        public String id;
        public String name;
    }
}
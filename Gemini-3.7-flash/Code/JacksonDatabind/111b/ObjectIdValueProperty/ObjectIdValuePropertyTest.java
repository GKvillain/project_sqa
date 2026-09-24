package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;

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
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdValuePropertyTest {

    private ObjectMapper _mapper;
    private JavaType _idType;
    private ObjectIdReader _objectIdReaderNoProp;
    private ObjectIdValueProperty _propNoProp;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _idType = TypeFactory.defaultInstance().constructType(Integer.class);
        JsonDeserializer<?> deser = new NumberDeserializers.IntegerDeserializer(Integer.class, 0);

        _objectIdReaderNoProp = ObjectIdReader.construct(
                _idType,
                PropertyName.construct("id"),
                new ObjectIdGenerators.IntSequenceGenerator(),
                deser,
                null,
                new SimpleObjectIdResolver()
        );

        _propNoProp = new ObjectIdValueProperty(_objectIdReaderNoProp, PropertyMetadata.STD_REQUIRED);
    }

    // Tests getAnnotation returns null
    @Test
    public void testGetAnnotation_anyClass_returnsNull() {
        assertNull(_propNoProp.getAnnotation(Deprecated.class));
    }

    // Tests getMember returns null
    @Test
    public void testGetMember_noMember_returnsNull() {
        assertNull(_propNoProp.getMember());
    }

    // Tests withName returns a new instance with updated PropertyName
    @Test
    public void testWithName_newName_returnsNewInstanceWithUpdatedName() {
        PropertyName newName = PropertyName.construct("customId");
        SettableBeanProperty updated = _propNoProp.withName(newName);

        assertNotNull(updated);
        assertNotSame(_propNoProp, updated);
        assertEquals(newName, updated.getFullName());
        assertTrue(updated instanceof ObjectIdValueProperty);
    }

    // Tests withValueDeserializer returns same instance when deserializer is identical
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        SettableBeanProperty updated = _propNoProp.withValueDeserializer(_propNoProp.getValueDeserializer());
        assertSame(_propNoProp, updated);
    }

    // Tests withValueDeserializer returns new instance when deserializer differs
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() {
        JsonDeserializer<?> newDeser = NullifyingDeserializer.instance;
        SettableBeanProperty updated = _propNoProp.withValueDeserializer(newDeser);

        assertNotNull(updated);
        assertNotSame(_propNoProp, updated);
        assertSame(newDeser, updated.getValueDeserializer());
    }

    // Tests withNullProvider returns new instance with updated null provider
    @Test
    public void testWithNullProvider_customProvider_returnsNewInstanceWithProvider() {
        NullValueProvider nva = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return -1;
            }
        };

        SettableBeanProperty updated = _propNoProp.withNullProvider(nva);
        assertNotNull(updated);
        assertNotSame(_propNoProp, updated);
        assertSame(_propNoProp.getValueDeserializer(), updated.getValueDeserializer());
    }

    // Tests set throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        _propNoProp.set(new Object(), 123);
    }

    // Tests setAndReturn throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_noIdProperty_throwsUnsupportedOperationException() throws IOException {
        _propNoProp.setAndReturn(new Object(), 123);
    }

    // Tests deserializeSetAndReturn when input token is VALUE_NULL
    @Test
    public void testDeserializeSetAndReturn_nullToken_returnsNull() throws IOException {
        JsonParser parser = _mapper.getFactory().createParser("null");
        parser.nextToken(); // advance to VALUE_NULL
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        Object result = _propNoProp.deserializeSetAndReturn(parser, ctxt, new Object());
        assertNull(result);
        parser.close();
    }

    // Tests deserializeAndSet when input token is VALUE_NULL
    @Test
    public void testDeserializeAndSet_nullToken_completesWithoutError() throws IOException {
        JsonParser parser = _mapper.getFactory().createParser("null");
        parser.nextToken(); // advance to VALUE_NULL
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        Object target = new Object();
        _propNoProp.deserializeAndSet(parser, ctxt, target);
        parser.close();
    }

    // Tests deserializeSetAndReturn without underlying idProperty returns target instance
    @Test
    public void testDeserializeSetAndReturn_validIdWithoutIdProperty_returnsInstance() throws IOException {
        JsonParser parser = _mapper.getFactory().createParser("42");
        parser.nextToken(); // advance to VALUE_NUMBER_INT

        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                _mapper.getDeserializationContext()).createInstance(
                _mapper.getDeserializationConfig(), parser, _mapper.getInjectableValues());

        Object instance = new Object();
        Object result = _propNoProp.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
        parser.close();
    }
}
package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
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
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdValuePropertyTest {

    private ObjectMapper mapper;
    private JavaType stringType;
    private ObjectIdGenerator<?> generator;
    private SimpleObjectIdResolver resolver;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        stringType = TypeFactory.defaultInstance().constructType(String.class);
        generator = new ObjectIdGenerators.IntSequenceGenerator();
        resolver = new SimpleObjectIdResolver();
    }

    private static class DummyProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        public Object setTarget;
        public Object setValue;

        public DummyProperty(PropertyName name, JavaType type) {
            super(name, type, PropertyMetadata.STD_REQUIRED, null);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nval) {
            return this;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) {
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) {
            return instance;
        }

        @Override
        public void set(Object instance, Object value) {
            this.setTarget = instance;
            this.setValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) {
            this.setTarget = instance;
            this.setValue = value;
            return "customReturn";
        }
    }

    // Tests getAnnotation returns null
    @Test
    public void testGetAnnotation_anyClass_returnsNull() {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        assertNull(prop.getAnnotation(Annotation.class));
    }

    // Tests getMember returns null
    @Test
    public void testGetMember_returnsNull() {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        assertNull(prop.getMember());
    }

    // Tests withName creates a new copy with updated PropertyName
    @Test
    public void testWithName_returnsNewInstanceWithUpdatedName() {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("oldId"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        PropertyName newName = new PropertyName("newId");

        ObjectIdValueProperty updatedProp = prop.withName(newName);

        assertNotNull(updatedProp);
        assertNotSame(prop, updatedProp);
        assertEquals("newId", updatedProp.getName());
    }

    // Tests withValueDeserializer creates a new copy with updated JsonDeserializer
    @Test
    public void testWithValueDeserializer_returnsNewInstanceWithUpdatedDeserializer() {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom";
            }
        };

        ObjectIdValueProperty updatedProp = prop.withValueDeserializer(newDeser);

        assertNotNull(updatedProp);
        assertNotSame(prop, updatedProp);
        assertSame(newDeser, updatedProp.getValueDeserializer());
    }

    // Tests set throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_whenIdPropertyIsNull_throwsUnsupportedOperationException() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        prop.set(new Object(), "val");
    }

    // Tests setAndReturn throws UnsupportedOperationException when idProperty is null
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_whenIdPropertyIsNull_throwsUnsupportedOperationException() throws IOException {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        prop.setAndReturn(new Object(), "val");
    }

    // Tests deserializeSetAndReturn when deserializer produces null id
    @Test
    public void testDeserializeSetAndReturn_nullId_returnsNullOrHandlesGracefully() throws IOException {
        JsonDeserializer<Object> nullDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                nullDeser,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        assertNull(result);
    }

    // Tests deserializeSetAndReturn and deserializeAndSet with valid non-null id
    @Test
    public void testDeserializeSetAndReturn_validId_bindsAndReturnsInstance() throws IOException {
        JsonDeserializer<Object> idDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "id123";
            }
        };

        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                idDeser,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        JsonParser parser = mapper.getFactory().createParser("\"id123\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
    }

    // Tests deserializeAndSet delegates to deserializeSetAndReturn
    @Test
    public void testDeserializeAndSet_validId_successfullyExecutes() throws IOException {
        JsonDeserializer<Object> idDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "id456";
            }
        };

        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                idDeser,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        JsonParser parser = mapper.getFactory().createParser("\"id456\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object instance = new Object();
        prop.deserializeAndSet(parser, ctxt, instance);
    }

    // Tests withValueDeserializer returning same instance when deserializer is unchanged
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "val";
            }
        };
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                deser,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        ObjectIdValueProperty updatedProp = prop.withValueDeserializer(deser);

        assertSame(prop, updatedProp);
    }

    // Tests withNullProvider creates a new copy with updated NullValueProvider
    @Test
    public void testWithNullProvider_returnsNewInstanceWithUpdatedNullProvider() {
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                null,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        NullValueProvider nullProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "customNull";
            }
        };

        ObjectIdValueProperty updatedProp = prop.withNullProvider(nullProvider);

        assertNotNull(updatedProp);
        assertNotSame(prop, updatedProp);
        assertSame(nullProvider, updatedProp.getNullValueProvider());
    }

    // Tests set delegates to idProperty when idProperty is present
    @Test
    public void testSet_whenIdPropertyIsPresent_delegatesToIdProperty() throws IOException {
        DummyProperty dummy = new DummyProperty(new PropertyName("id"), stringType);
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                dummy,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        Object instance = new Object();
        prop.set(instance, "dummyValue");

        assertSame(instance, dummy.setTarget);
        assertEquals("dummyValue", dummy.setValue);
    }

    // Tests setAndReturn delegates to idProperty when idProperty is present
    @Test
    public void testSetAndReturn_whenIdPropertyIsPresent_delegatesToIdProperty() throws IOException {
        DummyProperty dummy = new DummyProperty(new PropertyName("id"), stringType);
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                null,
                dummy,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        Object instance = new Object();
        Object result = prop.setAndReturn(instance, "dummyValue");

        assertSame(instance, dummy.setTarget);
        assertEquals("dummyValue", dummy.setValue);
        assertEquals("customReturn", result);
    }

    // Tests deserializeSetAndReturn delegates to idProperty when idProperty is present
    @Test
    public void testDeserializeSetAndReturn_whenIdPropertyIsPresent_delegatesToIdProperty() throws IOException {
        DummyProperty dummy = new DummyProperty(new PropertyName("id"), stringType);
        JsonDeserializer<Object> idDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "id789";
            }
        };
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                generator,
                idDeser,
                dummy,
                resolver
        );
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        JsonParser parser = mapper.getFactory().createParser("\"id789\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);

        assertEquals("customReturn", result);
        assertSame(instance, dummy.setTarget);
        assertEquals("id789", dummy.setValue);
    }
}
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

public class ObjectIdValuePropertyTest {

    private ObjectMapper mapper;
    private JavaType stringType;
    private ObjectIdGenerator<?> generator;
    private SimpleObjectIdResolver resolver;
    private JsonDeserializer<Object> dummyDeser;
    private ObjectIdReader objectIdReaderNoProp;
    private ObjectIdValueProperty propNoIdProp;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        stringType = mapper.constructType(String.class);
        generator = new ObjectIdGenerators.StringIdGenerator();
        resolver = new SimpleObjectIdResolver();
        dummyDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };
        objectIdReaderNoProp = ObjectIdReader.construct(
                stringType,
                PropertyName.construct("id"),
                generator,
                dummyDeser,
                null,
                resolver
        );
        propNoIdProp = new ObjectIdValueProperty(objectIdReaderNoProp, PropertyMetadata.STD_REQUIRED);
    }

    private static class DummySettableProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private Object lastAssignedValue;

        public DummySettableProperty(PropertyName name, JavaType type, JsonDeserializer<?> deser) {
            super(name, type, PropertyMetadata.STD_REQUIRED, deser);
        }

        protected DummySettableProperty(DummySettableProperty src) {
            super(src);
            this.lastAssignedValue = src.lastAssignedValue;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
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
            this.lastAssignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) {
            this.lastAssignedValue = value;
            return instance;
        }

        public Object getLastAssignedValue() {
            return lastAssignedValue;
        }
    }

    // Tests constructor initialization and metadata
    @Test
    public void testConstructor_validReader_initializesCorrectly() {
        assertEquals("id", propNoIdProp.getName());
        assertEquals(stringType, propNoIdProp.getType());
        assertEquals(dummyDeser, propNoIdProp.getValueDeserializer());
        assertTrue(propNoIdProp.isRequired());
    }

    // Tests getAnnotation always returns null
    @Test
    public void testGetAnnotation_anyClass_returnsNull() {
        assertNull(propNoIdProp.getAnnotation(Deprecated.class));
    }

    // Tests getMember always returns null
    @Test
    public void testGetMember_voidInput_returnsNull() {
        assertNull(propNoIdProp.getMember());
    }

    // Tests withName creates a new instance with updated property name
    @Test
    public void testWithName_newName_returnsNewInstanceWithNewName() {
        PropertyName newName = new PropertyName("customId");
        SettableBeanProperty copy = propNoIdProp.withName(newName);

        assertNotNull(copy);
        assertNotSame(propNoIdProp, copy);
        assertEquals("customId", copy.getName());
        assertTrue(copy instanceof ObjectIdValueProperty);
    }

    // Tests withValueDeserializer with the same deserializer returns same instance
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        SettableBeanProperty copy = propNoIdProp.withValueDeserializer(dummyDeser);
        assertSame(propNoIdProp, copy);
    }

    // Tests withValueDeserializer with a new deserializer returns new instance with updated deserializer
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() {
        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        SettableBeanProperty copy = propNoIdProp.withValueDeserializer(newDeser);
        assertNotNull(copy);
        assertNotSame(propNoIdProp, copy);
        assertSame(newDeser, copy.getValueDeserializer());
    }

    // Tests withValueDeserializer when null provider differs from value deserializer preserves null provider
    @Test
    public void testWithValueDeserializer_withCustomNullProvider_preservesNullProvider() {
        NullValueProvider customProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "CUSTOM_NULL";
            }
        };

        SettableBeanProperty propWithNullProvider = propNoIdProp.withNullProvider(customProvider);

        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        SettableBeanProperty copy = propWithNullProvider.withValueDeserializer(newDeser);
        assertNotNull(copy);
        assertNotSame(propWithNullProvider, copy);
        assertSame(newDeser, copy.getValueDeserializer());
        assertSame(customProvider, copy.getNullValueProvider());
    }

    // Tests withNullProvider returns new instance with updated null provider
    @Test
    public void testWithNullProvider_customProvider_returnsNewInstanceWithProvider() {
        NullValueProvider customProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "DEFAULT";
            }
        };

        SettableBeanProperty copy = propNoIdProp.withNullProvider(customProvider);
        assertNotNull(copy);
        assertNotSame(propNoIdProp, copy);
        assertSame(customProvider, copy.getNullValueProvider());
    }

    // Tests deserializeSetAndReturn when parser points to VALUE_NULL returns null
    @Test
    public void testDeserializeSetAndReturn_nullToken_returnsNull() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // Move to VALUE_NULL
        DeserializationContext ctxt = mapper.createDeserializationContext(
                parser, mapper.getDeserializationConfig());

        Object instance = new Object();
        Object result = propNoIdProp.deserializeSetAndReturn(parser, ctxt, instance);
        assertNull(result);
        parser.close();
    }

    // Tests deserializeAndSet when parser points to VALUE_NULL does not throw exception
    @Test
    public void testDeserializeAndSet_nullToken_doesNothing() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // Move to VALUE_NULL
        DeserializationContext ctxt = mapper.createDeserializationContext(
                parser, mapper.getDeserializationConfig());

        Object instance = new Object();
        propNoIdProp.deserializeAndSet(parser, ctxt, instance);
        parser.close();
    }

    // Tests deserializeSetAndReturn without underlying idProperty returns instance bound to ObjectId
    @Test
    public void testDeserializeSetAndReturn_withoutIdProperty_returnsInstance() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("\"12345\"");
        parser.nextToken(); // Move to VALUE_STRING
        DeserializationContext ctxt = mapper.createDeserializationContext(
                parser, mapper.getDeserializationConfig());

        Object instance = new Object();
        Object result = propNoIdProp.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
        parser.close();
    }

    // Tests deserializeSetAndReturn with underlying idProperty sets id property and returns instance
    @Test
    public void testDeserializeSetAndReturn_withIdProperty_setsPropertyAndReturnsResult() throws Exception {
        DummySettableProperty dummyIdProp = new DummySettableProperty(
                PropertyName.construct("id"), stringType, dummyDeser);

        ObjectIdReader readerWithProp = ObjectIdReader.construct(
                stringType,
                PropertyName.construct("id"),
                generator,
                dummyDeser,
                dummyIdProp,
                resolver
        );
        ObjectIdValueProperty propWithIdProp = new ObjectIdValueProperty(readerWithProp, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = mapper.getFactory().createParser("\"id-value-abc\"");
        parser.nextToken(); // Move to VALUE_STRING
        DeserializationContext ctxt = mapper.createDeserializationContext(
                parser, mapper.getDeserializationConfig());

        Object instance = new Object();
        Object result = propWithIdProp.deserializeSetAndReturn(parser, ctxt, instance);

        assertSame(instance, result);
        assertEquals("id-value-abc", dummyIdProp.getLastAssignedValue());
        parser.close();
    }

    // Tests deserializeAndSet delegates correctly
    @Test
    public void testDeserializeAndSet_validParser_completesWithoutException() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("\"test-id\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.createDeserializationContext(
                parser, mapper.getDeserializationConfig());

        Object instance = new Object();
        propNoIdProp.deserializeAndSet(parser, ctxt, instance);
        parser.close();
    }

    // Tests setAndReturn when idProperty is null throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_noIdProperty_throwsUnsupportedOperationException() throws Exception {
        Object instance = new Object();
        propNoIdProp.setAndReturn(instance, "some-id");
    }

    // Tests set when idProperty is null throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_noIdProperty_throwsUnsupportedOperationException() throws Exception {
        Object instance = new Object();
        propNoIdProp.set(instance, "some-id");
    }

    // Tests setAndReturn with underlying idProperty delegates correctly
    @Test
    public void testSetAndReturn_withIdProperty_delegatesToIdProperty() throws Exception {
        DummySettableProperty dummyIdProp = new DummySettableProperty(
                PropertyName.construct("id"), stringType, dummyDeser);

        ObjectIdReader readerWithProp = ObjectIdReader.construct(
                stringType,
                PropertyName.construct("id"),
                generator,
                dummyDeser,
                dummyIdProp,
                resolver
        );
        ObjectIdValueProperty propWithIdProp = new ObjectIdValueProperty(readerWithProp, PropertyMetadata.STD_OPTIONAL);

        Object instance = new Object();
        Object returned = propWithIdProp.setAndReturn(instance, "direct-id");

        assertSame(instance, returned);
        assertEquals("direct-id", dummyIdProp.getLastAssignedValue());
    }

    // Tests set with underlying idProperty delegates correctly
    @Test
    public void testSet_withIdProperty_delegatesToIdProperty() throws Exception {
        DummySettableProperty dummyIdProp = new DummySettableProperty(
                PropertyName.construct("id"), stringType, dummyDeser);

        ObjectIdReader readerWithProp = ObjectIdReader.construct(
                stringType,
                PropertyName.construct("id"),
                generator,
                dummyDeser,
                dummyIdProp,
                resolver
        );
        ObjectIdValueProperty propWithIdProp = new ObjectIdValueProperty(readerWithProp, PropertyMetadata.STD_OPTIONAL);

        Object instance = new Object();
        propWithIdProp.set(instance, "direct-id-2");

        assertEquals("direct-id-2", dummyIdProp.getLastAssignedValue());
    }
}
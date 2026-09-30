package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdReferencePropertyTest {

    private static class DummyProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        boolean fixAccessCalled = false;
        Object setValue = null;
        Object setInstance = null;

        public DummyProperty(PropertyName name, JavaType type) {
            super(name, type, PropertyMetadata.STD_REQUIRED, null);
        }

        protected DummyProperty(DummyProperty src) {
            super(src);
        }

        protected DummyProperty(DummyProperty src, JsonDeserializer<?> deser, NullValueProvider nva) {
            super(src, deser, nva);
        }

        protected DummyProperty(DummyProperty src, PropertyName newName) {
            super(src, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummyProperty(this, deser, _nullProvider);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummyProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return new DummyProperty(this, _valueDeserializer, nva);
        }

        @Override
        public void fixAccess(DeserializationConfig config) {
            this.fixAccessCalled = true;
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
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return setAndReturn(instance, deserialize(p, ctxt));
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.setInstance = instance;
            this.setValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return instance;
        }

        @Override
        public int getCreatorIndex() {
            return 3;
        }
    }

    private static class DummyDeserializer extends JsonDeserializer<Object> {
        private final Object _value;
        private final ObjectIdReader _objectIdReader;

        public DummyDeserializer(Object value) {
            this(value, null);
        }

        public DummyDeserializer(Object value, ObjectIdReader oir) {
            this._value = value;
            this._objectIdReader = oir;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return _value;
        }

        @Override
        public ObjectIdReader getObjectIdReader() {
            return _objectIdReader;
        }
    }

    private static class FailingDeserializer extends JsonDeserializer<Object> {
        private final UnresolvedForwardReference _ref;
        private final ObjectIdReader _objectIdReader;

        public FailingDeserializer(UnresolvedForwardReference ref) {
            this(ref, null);
        }

        public FailingDeserializer(UnresolvedForwardReference ref, ObjectIdReader oir) {
            this._ref = ref;
            this._objectIdReader = oir;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws UnresolvedForwardReference {
            throw _ref;
        }

        @Override
        public ObjectIdReader getObjectIdReader() {
            return _objectIdReader;
        }
    }

    // Tests constructor and forward delegation methods
    @Test
    public void testForwardDelegationMethods() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);

        assertNull(prop.getAnnotation(Override.class));
        assertNull(prop.getMember());
        assertEquals(3, prop.getCreatorIndex());

        prop.fixAccess(null);
        assertTrue(forward.fixAccessCalled);
    }

    // Tests withName method creates new instance with updated name
    @Test
    public void testWithName_returnsNewInstanceWithNewName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("oldName"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);

        PropertyName newName = new PropertyName("newName");
        SettableBeanProperty renamed = prop.withName(newName);

        assertNotNull(renamed);
        assertNotSame(prop, renamed);
        assertEquals("newName", renamed.getName());
    }

    // Tests withValueDeserializer returns same instance when deserializer is identical
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        SettableBeanProperty result = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, result);
    }

    // Tests withValueDeserializer returns new instance with updated deserializer
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        JsonDeserializer<Object> deser = new DummyDeserializer("test");
        SettableBeanProperty result = prop.withValueDeserializer(deser);

        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(deser, result.getValueDeserializer());
    }

    // Tests withNullProvider returns new instance with updated null provider
    @Test
    public void testWithNullProvider_returnsNewInstanceWithNullProvider() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        NullValueProvider nva = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "null-val";
            }
        };

        SettableBeanProperty result = prop.withNullProvider(nva);
        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(nva, result.getNullValueProvider());
    }

    // Tests set and setAndReturn delegation to forward property
    @Test
    public void testSetAndSetAndReturn_delegatesToForward() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        Object instance = new Object();
        prop.set(instance, "value1");
        assertSame(instance, forward.setInstance);
        assertEquals("value1", forward.setValue);

        Object result = prop.setAndReturn(instance, "value2");
        assertSame(instance, result);
        assertEquals("value2", forward.setValue);
    }

    // Tests deserializeAndSet delegates through deserializeSetAndReturn
    @Test
    public void testDeserializeAndSet_setsValueCorrectly() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        prop = (ObjectIdReferenceProperty) prop.withValueDeserializer(new DummyDeserializer("deserialized"));

        Object instance = new Object();
        prop.deserializeAndSet(null, null, instance);

        assertSame(instance, forward.setInstance);
        assertEquals("deserialized", forward.setValue);
    }

    // Tests deserializeSetAndReturn when UnresolvedForwardReference occurs with ObjectIdInfo
    @Test
    public void testDeserializeSetAndReturn_unresolvedForwardReference_appendsReferring() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);

        ReadableObjectId roid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id-1"));
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved", JsonLocation.NA, roid);
        prop = (ObjectIdReferenceProperty) prop.withValueDeserializer(new FailingDeserializer(ref));

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(null, null, instance);

        assertNull(result);
        assertTrue(roid.hasReferringProperties());
    }

    // Tests deserializeSetAndReturn when UnresolvedForwardReference occurs with ObjectIdReader on deserializer
    @Test
    public void testDeserializeSetAndReturn_unresolvedForwardReference_withObjectIdReader_appendsReferring() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"), null, null, null, new SimpleObjectIdResolver());
        ReadableObjectId roid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id-reader-1"));
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved", JsonLocation.NA, roid);
        prop = (ObjectIdReferenceProperty) prop.withValueDeserializer(new FailingDeserializer(ref, reader));

        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(null, null, instance);

        assertNull(result);
        assertTrue(roid.hasReferringProperties());
    }

    // Tests deserializeSetAndReturn throws JsonMappingException when no identity info exists
    @Test(expected = JsonMappingException.class)
    public void testDeserializeSetAndReturn_unresolvedForwardReference_noIdentityInfo_throwsException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        ReadableObjectId roid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id-2"));
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved", JsonLocation.NA, roid);
        prop = (ObjectIdReferenceProperty) prop.withValueDeserializer(new FailingDeserializer(ref));

        prop.deserializeSetAndReturn(null, null, new Object());
    }

    // Tests PropertyReferring handles resolved forward reference successfully
    @Test
    public void testPropertyReferring_handleResolvedForwardReference_success() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        ReadableObjectId roid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id-123"));
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved", JsonLocation.NA, roid);

        Object pojo = new Object();
        ObjectIdReferenceProperty.PropertyReferring referring =
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, String.class, pojo);

        referring.handleResolvedForwardReference("id-123", "resolvedValue");

        assertSame(pojo, forward.setInstance);
        assertEquals("resolvedValue", forward.setValue);
    }

    // Tests PropertyReferring throws IllegalArgumentException when id does not match
    @Test(expected = IllegalArgumentException.class)
    public void testPropertyReferring_handleResolvedForwardReference_mismatchedId_throwsException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty forward = new DummyProperty(new PropertyName("prop"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        ReadableObjectId roid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id-123"));
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved", JsonLocation.NA, roid);

        Object pojo = new Object();
        ObjectIdReferenceProperty.PropertyReferring referring =
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, String.class, pojo);

        referring.handleResolvedForwardReference("wrong-id", "resolvedValue");
    }
}
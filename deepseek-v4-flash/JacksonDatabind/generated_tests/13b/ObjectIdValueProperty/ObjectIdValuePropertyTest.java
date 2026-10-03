package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

public class ObjectIdValuePropertyTest {

    // Helper stubs
    private static class SimpleJsonDeserializer extends JsonDeserializer<Object> {
        private final Object value;
        SimpleJsonDeserializer(Object value) { this.value = value; }
        @Override public Object deserialize(JsonParser p, DeserializationContext ctxt) { return value; }
        @Override public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) { return value; }
    }

    private static class SimpleObjectIdGenerator implements ObjectIdGenerator<String> {
        @Override public ObjectIdGenerator<String> forScope(Class<?> scope) { return this; }
        @Override public Class<?> getScope() { return Object.class; }
        @Override public String generateId(Object forPojo) { return "genId"; }
        @Override public boolean canUseFor(ObjectIdGenerator<?> gen) { return false; }
        @Override public String key(Object key) { return key.toString(); }
    }

    private static class SimpleObjectIdResolver implements ObjectIdResolver {
        @Override public void bindItem(ObjectIdGenerator.IdKey key, Object pojo) {}
        @Override public Object resolveId(ObjectIdGenerator.IdKey key) { return null; }
        @Override public ObjectIdResolver newForDeserialization(Object context) { return this; }
        @Override public boolean canUseFor(ObjectIdResolver resolver) { return false; }
    }

    private static class MockReadableObjectId extends ReadableObjectId {
        private Object item;
        MockReadableObjectId(Object id) { super(id); }
        @Override public void bindItem(Object ob) { this.item = ob; }
        @Override public Object getItem() { return item; }
    }

    private static class MockDeserializationContext extends DeserializationContext {
        protected MockDeserializationContext() {
            super(new DeserializationConfig(null, null, null, null, null) {
                // minimal config
            }, null, null);
        }
        @Override public Object findObjectId(Object id, ObjectIdGenerator<?> gen, ObjectIdResolver resolver) {
            return new MockReadableObjectId(id);
        }
        // override all other abstract methods with no-ops and dummy
        @Override public boolean hasDeserializationFeatures(int featureMask) { return false; }
        @Override public boolean hasSomeOfFeatures(int featureMask) { return false; }
        @Override public boolean isEnabled(DeserializationFeature feature) { return false; }
        @Override public int getDeserializationFeatures() { return 0; }
        @Override public JsonParser getParser() { return null; }
        @Override public int getAttribute(Object key) { return 0; }
        @Override public Object getAttribute(Object key, int def) { return def; }
        @Override public void setAttribute(Object key, Object value) {}
        @Override public DeserializationConfig getConfig() { return null; }
        @Override public Class<?> getActiveView() { return null; }
        @Override public AnnotationIntrospector getAnnotationIntrospector() { return null; }
        @Override public JsonDeserializer<Object> findRootValueDeserializer(JavaType type) { return null; }
        @Override public JsonDeserializer<Object> findValueDeserializer(JavaType type, BeanProperty prop) { return null; }
        @Override public JsonDeserializer<Object> findDeserializerFromAnnotation(Annotation a) { return null; }
        @Override public KeyDeserializer findKeyDeserializer(JavaType type, BeanProperty prop) { return null; }
        @Override public Object handleUnexpectedToken(Class<?> targetType, JsonParser p) { return null; }
        @Override public Object handleInstantiationProblem(Class<?> instClass, Object argument, Throwable t) { return null; }
        @Override public Object handleWeirdStringValue(Class<?> targetType, String value, String msg, Object... args) { return null; }
        @Override public Object handleWeirdNumberValue(Class<?> targetType, Number value, String msg, Object... args) { return null; }
        @Override public Object handleMissingInstantiator(Class<?> instClass, valueProvider) { return null; }
        @Override public Object handleMissingValue(BeanProperty prop) { return null; }
        @Override public Object handleMissingValue(String propName) { return null; }
        @Override public void reportUnresolvedObjectId(Object id, String msg) {}
        @Override public JsonMappingException mappingException(String msg, Object... args) { return null; }
        @Override public JsonMappingException instantiationException(Class<?> instClass, Throwable t) { return null; }
        @Override public JsonMappingException weirdStringException(Class<?> targetType, String value, String msg, Object... args) { return null; }
        @Override public JsonMappingException weirdNumberException(Class<?> targetType, Number value, String msg, Object... args) { return null; }
        @Override public JsonMappingException wrongTokenException(JsonParser p, JsonToken exp, String msg) { return null; }
        @Override public JsonMappingException unknownTypeException(JavaType type, String id) { return null; }
        @Override public JsonMappingException endOfInputException(Class<?> instClass) { return null; }
        @Override public void reportTrailingTokens(JsonParser p) {}
        @Override public Object handleUnrecognizedProperty(BeanProperty prop, JsonParser p) { return null; }
        @Override public Object handleUnrecognizedProperty(String propName, Class<?> beanClass) { return null; }
        @Override public boolean isAbsent(Object value) { return false; }
        @Override public Object getLastHandledPropertyName() { return null; }
        @Override public void setLastHandledPropertyName(String name) {}
        @Override public TypeFactory getTypeFactory() { return TypeFactory.defaultInstance(); }
        @Override public Object extractScalarFromToken(JsonParser p, JavaType targetType) { return null; }
    }

    private static class MockSettableBeanProperty extends SettableBeanProperty {
        private Object lastSetValue;
        private final boolean idPropNull; // controls setAndReturn behavior
        MockSettableBeanProperty(PropertyName name, JavaType type, PropertyMetadata metadata, JsonDeserializer<?> deser, boolean idPropNull) {
            super(name, type, metadata, deser);
            this.idPropNull = idPropNull;
        }
        // For test we only care about setAndReturn
        @Override public Object setAndReturn(Object instance, Object value) throws IOException {
            if (idPropNull) {
                throw new UnsupportedOperationException("Should not call set() on ObjectIdProperty that has no SettableBeanProperty");
            }
            lastSetValue = value;
            return instance;
        }
        @Override public void set(Object instance, Object value) throws IOException {
            setAndReturn(instance, value);
        }
        @Override public Object deserializeSetAndReturn(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException {
            return null;
        }
        @Override public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException {}
        @Override public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
        @Override public AnnotatedMember getMember() { return null; }
        @Override public SettableBeanProperty withName(PropertyName newName) { return this; }
        @Override public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }
        // getter for verification
        Object getLastSetValue() { return lastSetValue; }
    }

    // Helper to create ObjectIdReader with or without idProperty
    private ObjectIdReader createReader(SettableBeanProperty idProp) {
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = new SimpleJsonDeserializer("testId");
        ObjectIdGenerator<?> gen = new SimpleObjectIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();
        return ObjectIdReader.construct(idType, "id", deser, gen, resolver, idProp);
    }

    // Tests for constructors and copy methods
    @Test
    public void testConstructor_withObjectIdReaderAndMetadata_createsProperty() {
        PropertyMetadata meta = PropertyMetadata.STD_OPTIONAL;
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, meta);
        assertNotNull(prop);
        assertEquals("id", prop.getName());
        assertSame(reader, prop._objectIdReader);
    }

    @Test
    public void testWithValueDeserializer_returnsNewPropertyWithSameReader() {
        PropertyMetadata meta = PropertyMetadata.STD_OPTIONAL;
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, meta);
        JsonDeserializer<?> newDeser = new SimpleJsonDeserializer("newId");
        ObjectIdValueProperty copy = prop.withValueDeserializer(newDeser);
        assertNotSame(prop, copy);
        assertSame(reader, copy._objectIdReader);
        assertSame(newDeser, copy._valueDeserializer);
    }

    @Test
    public void testWithName_returnsNewPropertyWithNewName() {
        PropertyMetadata meta = PropertyMetadata.STD_OPTIONAL;
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, meta);
        PropertyName newName = new PropertyName("newId");
        ObjectIdValueProperty copy = prop.withName(newName);
        assertNotSame(prop, copy);
        assertEquals(newName, copy.getFullName());
    }

    @Test
    public void testGetAnnotation_returnsNull() {
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        assertNull(prop.getAnnotation(Override.class));
        assertNull(prop.getAnnotation(Deprecated.class));
    }

    @Test
    public void testGetMember_returnsNull() {
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        assertNull(prop.getMember());
    }

    // Tests for setAndReturn
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_idPropNull_throwsUnsupportedOperationException() throws Exception {
        ObjectIdReader reader = createReader(null); // idProp null
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        prop.setAndReturn(new Object(), "value");
    }

    @Test
    public void testSetAndReturn_idPropNotNull_setsPropertyAndReturnsInstance() throws Exception {
        MockSettableBeanProperty idProp = new MockSettableBeanProperty(
                new PropertyName("prop"), 
                TypeFactory.defaultInstance().constructType(Object.class),
                PropertyMetadata.STD_OPTIONAL,
                new SimpleJsonDeserializer("x"),
                false); // not null behavior
        ObjectIdReader reader = createReader(idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        Object instance = new Object();
        Object result = prop.setAndReturn(instance, "setValue");
        assertSame(instance, result);
        assertEquals("setValue", idProp.getLastSetValue());
    }

    @Test
    public void testSet_callsSetAndReturn() throws Exception {
        MockSettableBeanProperty idProp = new MockSettableBeanProperty(
                new PropertyName("prop"),
                TypeFactory.defaultInstance().constructType(Object.class),
                PropertyMetadata.STD_OPTIONAL,
                new SimpleJsonDeserializer("x"),
                false);
        ObjectIdReader reader = createReader(idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        Object instance = new Object();
        prop.set(instance, "setValue");
        assertEquals("setValue", idProp.getLastSetValue());
    }

    // Tests for deserializeSetAndReturn
    @Test
    public void testDeserializeSetAndReturn_idPropNull_bindsItemAndReturnsInstance() throws Exception {
        ObjectIdReader reader = createReader(null); // idProp null
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser dummyParser = null; // not used by our simple deserializer
        DeserializationContext ctxt = new MockDeserializationContext();
        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(dummyParser, ctxt, instance);
        assertSame(instance, result);
        // The mock context's findObjectId returns a MockReadableObjectId; we cannot easily verify binding, but no exception
    }

    @Test
    public void testDeserializeSetAndReturn_idPropNotNull_bindsItemAndSetsProperty() throws Exception {
        MockSettableBeanProperty idProp = new MockSettableBeanProperty(
                new PropertyName("prop"),
                TypeFactory.defaultInstance().constructType(Object.class),
                PropertyMetadata.STD_OPTIONAL,
                new SimpleJsonDeserializer("x"),
                false);
        ObjectIdReader reader = createReader(idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser dummyParser = null;
        DeserializationContext ctxt = new MockDeserializationContext();
        Object instance = new Object();
        Object result = prop.deserializeSetAndReturn(dummyParser, ctxt, instance);
        assertSame(instance, result);
        // The idProp's setAndReturn is called, so lastSetValue should be the deserialized id ("testId")
        assertEquals("testId", idProp.getLastSetValue());
    }

    @Test
    public void testDeserializeSetAndReturn_idNull_doesNotThrow() throws Exception {
        // Create a deserializer that returns null
        JsonDeserializer<Object> nullDeser = new SimpleJsonDeserializer(null);
        ObjectIdReader reader = ObjectIdReader.construct(
                TypeFactory.defaultInstance().constructType(String.class),
                "id", nullDeser, new SimpleObjectIdGenerator(), new SimpleObjectIdResolver(), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser dummyParser = null;
        DeserializationContext ctxt = new MockDeserializationContext();
        Object instance = new Object();
        // Should not throw, per comment in source about [databind#742]
        Object result = prop.deserializeSetAndReturn(dummyParser, ctxt, instance);
        assertSame(instance, result);
    }

    @Test
    public void testDeserializeAndSet_delegatesToDeserializeSetAndReturn() throws Exception {
        ObjectIdReader reader = createReader(null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        JsonParser dummyParser = null;
        DeserializationContext ctxt = new MockDeserializationContext();
        Object instance = new Object();
        // Should not throw
        prop.deserializeAndSet(dummyParser, ctxt, instance);
        // No return value; success if no exception
    }
}
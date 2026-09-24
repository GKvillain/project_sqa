package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SettableBeanPropertyTest {

    private static class ConcreteSettableBeanProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private Object valueSet;

        public ConcreteSettableBeanProperty(PropertyName propName, JavaType type, PropertyMetadata metadata,
                JsonDeserializer<Object> valueDeser) {
            super(propName, type, metadata, valueDeser);
        }

        public ConcreteSettableBeanProperty(PropertyName propName, JavaType type, PropertyName wrapper,
                com.fasterxml.jackson.databind.jsontype.TypeDeserializer typeDeser,
                com.fasterxml.jackson.databind.util.Annotations contextAnnotations,
                PropertyMetadata metadata) {
            super(propName, type, wrapper, typeDeser, contextAnnotations, metadata);
        }

        protected ConcreteSettableBeanProperty(ConcreteSettableBeanProperty src) {
            super(src);
        }

        protected ConcreteSettableBeanProperty(ConcreteSettableBeanProperty src, JsonDeserializer<?> deser,
                NullValueProvider nuller) {
            super(src, deser, nuller);
        }

        protected ConcreteSettableBeanProperty(ConcreteSettableBeanProperty src, PropertyName newName) {
            super(src, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new ConcreteSettableBeanProperty(this, deser, this._nullProvider);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new ConcreteSettableBeanProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return new ConcreteSettableBeanProperty(this, this._valueDeserializer, nva);
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance)
                throws IOException {
            set(instance, deserialize(p, ctxt));
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.valueSet = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            this.valueSet = value;
            return instance;
        }

        public void testThrowAsIOE(JsonParser p, Exception e, Object value) throws IOException {
            _throwAsIOE(p, e, value);
        }

        public void testThrowAsIOE(JsonParser p, Exception e) throws IOException {
            _throwAsIOE(p, e);
        }

        public void testThrowAsIOE(Exception e, Object value) throws IOException {
            _throwAsIOE(e, value);
        }

        public void testThrowAsIOE(Exception e) throws IOException {
            _throwAsIOE(e);
        }
    }

    private static class ConcreteDelegatingProperty extends SettableBeanProperty.Delegating {
        private static final long serialVersionUID = 1L;

        public ConcreteDelegatingProperty(SettableBeanProperty d) {
            super(d);
        }

        @Override
        protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
            return new ConcreteDelegatingProperty(d);
        }
    }

    // Tests construction with default/missing deserializer
    @Test
    public void testConstructor_defaultValues_initializedCorrectly() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        PropertyName propName = new PropertyName("propName");
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                propName, type, null, null, null, PropertyMetadata.STD_REQUIRED);

        assertEquals("propName", prop.getName());
        assertEquals(propName, prop.getFullName());
        assertEquals(type, prop.getType());
        assertNull(prop.getWrapperName());
        assertFalse(prop.hasValueDeserializer());
        assertNull(prop.getValueDeserializer());
        assertNull(prop.getValueTypeDeserializer());
        assertEquals(-1, prop.getPropertyIndex());
        assertNull(prop.getInjectableValueId());
        assertFalse(prop.isIgnorable());
    }

    // Tests construction with null PropertyName defaulting to NO_NAME
    @Test
    public void testConstructor_nullPropertyName_defaultsToNoName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                null, type, PropertyMetadata.STD_OPTIONAL, null);

        assertEquals("", prop.getName());
        assertEquals(PropertyName.NO_NAME, prop.getFullName());
    }

    // Tests withSimpleName creates new property or returns same if name matches
    @Test
    public void testWithSimpleName_sameAndDifferentNames() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        PropertyName propName = new PropertyName("testProp");
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                propName, type, PropertyMetadata.STD_OPTIONAL, null);

        SettableBeanProperty sameProp = prop.withSimpleName("testProp");
        assertEquals(prop.getName(), sameProp.getName());

        SettableBeanProperty diffProp = prop.withSimpleName("renamedProp");
        assertEquals("renamedProp", diffProp.getName());
    }

    // Tests assignIndex sets property index correctly
    @Test
    public void testAssignIndex_validIndex_setsIndex() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.assignIndex(5);
        assertEquals(5, prop.getPropertyIndex());
    }

    // Tests assignIndex throws exception when index is already assigned
    @Test(expected = IllegalStateException.class)
    public void testAssignIndex_alreadyAssigned_throwsIllegalStateException() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.assignIndex(1);
        prop.assignIndex(2);
    }

    // Tests getCreatorIndex throws IllegalStateException by default
    @Test(expected = IllegalStateException.class)
    public void testGetCreatorIndex_default_throwsIllegalStateException() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.getCreatorIndex();
    }

    // Tests setViews and visibleInView behavior
    @Test
    public void testSetViews_andVisibleInView() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(String.class));

        prop.setViews(new Class<?>[] { CharSequence.class });
        assertTrue(prop.hasViews());
        assertTrue(prop.visibleInView(String.class));
        assertFalse(prop.visibleInView(Integer.class));

        prop.setViews(null);
        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(Integer.class));
    }

    // Tests managedReferenceName and objectIdInfo setters and getters
    @Test
    public void testManagedReferenceAndObjectIdInfo() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        assertNull(prop.getManagedReferenceName());
        prop.setManagedReferenceName("refName");
        assertEquals("refName", prop.getManagedReferenceName());

        assertNull(prop.getObjectIdInfo());
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        prop.setObjectIdInfo(info);
        assertSame(info, prop.getObjectIdInfo());
    }

    // Tests _throwAsIOE wraps IllegalArgumentException in JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testThrowAsIOE_illegalArgumentException_wrapsInJsonMappingException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE((JsonParser) null, new IllegalArgumentException("Invalid argument"), 123);
    }

    // Tests _throwAsIOE handles IllegalArgumentException with null message
    @Test(expected = JsonMappingException.class)
    public void testThrowAsIOE_illegalArgumentExceptionNullMessage_wrapsInJsonMappingException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE((JsonParser) null, new IllegalArgumentException((String) null), 123);
    }

    // Tests _throwAsIOE rethrows IOException directly
    @Test(expected = JsonParseException.class)
    public void testThrowAsIOE_jsonParseException_rethrowsDirectly() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE((JsonParser) null, new JsonParseException(null, "Parse error"));
    }

    // Tests _throwAsIOE wraps general checked exception into JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testThrowAsIOE_checkedException_wrapsInJsonMappingException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE((JsonParser) null, new Exception("General checked exception"));
    }

    // Tests toString format
    @Test
    public void testToString_returnsExpectedFormat() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("testField"), type, PropertyMetadata.STD_OPTIONAL, null);

        assertEquals("[property 'testField']", prop.toString());
    }

    // Tests Delegating wrapper delegates calls correctly
    @Test
    public void testDelegating_delegatesAllMethods() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("delegateProp"), type, PropertyMetadata.STD_OPTIONAL, null);

        ConcreteDelegatingProperty delegating = new ConcreteDelegatingProperty(prop);

        assertSame(prop, delegating.getDelegate());
        assertEquals("delegateProp", delegating.getName());
        assertEquals(prop.getFullName(), delegating.getFullName());
        assertEquals(prop.getType(), delegating.getType());
        assertFalse(delegating.hasValueDeserializer());
        assertNull(delegating.getValueDeserializer());
        assertNull(delegating.getValueTypeDeserializer());
        assertNull(delegating.getManagedReferenceName());
        assertNull(delegating.getObjectIdInfo());
        assertFalse(delegating.hasViews());
        assertEquals(-1, delegating.getPropertyIndex());
        assertNull(delegating.getInjectableValueId());
        assertNull(delegating.getMember());
        assertNull(delegating.getAnnotation(Override.class));

        delegating.set("target", "value");
        assertEquals("value", prop.valueSet);

        Object result = delegating.setAndReturn("target2", "value2");
        assertEquals("target2", result);
        assertEquals("value2", prop.valueSet);

        delegating.assignIndex(3);
        assertEquals(3, prop.getPropertyIndex());
        assertEquals(3, delegating.getPropertyIndex());
    }

    // Tests legacy _throwAsIOE methods
    @Test(expected = JsonMappingException.class)
    public void testThrowAsIOE_legacyWithoutParser_wrapsException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE(new Exception("Checked exception without parser"));
    }

    @Test(expected = JsonMappingException.class)
    public void testThrowAsIOE_legacyWithValueWithoutParser_wrapsException() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        prop.testThrowAsIOE(new IllegalArgumentException("Illegal argument without parser"), "sampleValue");
    }

    // Tests delegating withName, withValueDeserializer, withNullProvider
    @Test
    public void testDelegating_withMethods() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteSettableBeanProperty prop = new ConcreteSettableBeanProperty(
                new PropertyName("prop"), type, PropertyMetadata.STD_OPTIONAL, null);

        ConcreteDelegatingProperty delegating = new ConcreteDelegatingProperty(prop);

        SettableBeanProperty renamed = delegating.withName(new PropertyName("newPropName"));
        assertEquals("newPropName", renamed.getName());

        SettableBeanProperty withDeser = delegating.withValueDeserializer(null);
        assertNotNull(withDeser);

        SettableBeanProperty withNuller = delegating.withNullProvider(null);
        assertNotNull(withNuller);
    }
}
package com.fasterxml.jackson.databind.deser.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class InnerClassPropertyTest {

    // Helper classes for testing inner class construction
    static class Outer {
        public class Inner {
            public String value;
            public Inner() { }
        }
    }

    static class FailingOuter {
        public class FailingInner {
            public FailingInner() {
                throw new RuntimeException("Instantiation failed intentionally");
            }
        }
    }

    // Dummy SettableBeanProperty for delegation testing
    static class DummyProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private int _index = -1;
        private Object _assignedValue;

        public DummyProperty(String name, JavaType type) {
            super(new PropertyName(name), type, null, null);
        }

        public DummyProperty(DummyProperty src, PropertyName newName) {
            super(src, newName);
            this._index = src._index;
        }

        public DummyProperty(DummyProperty src, JsonDeserializer<?> deser) {
            super(src, deser);
            this._index = src._index;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummyProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummyProperty(this, deser);
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
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) { }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) {
            return instance;
        }

        @Override
        public void set(Object instance, Object value) {
            this._assignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) {
            this._assignedValue = value;
            return instance;
        }

        @Override
        public void assignIndex(int index) {
            this._index = index;
        }

        @Override
        public int getPropertyIndex() {
            return this._index;
        }

        public Object getAssignedValue() {
            return _assignedValue;
        }
    }

    // Dummy Deserializer to verify deserialize calls
    static class DummyDeserializer extends JsonDeserializer<Object> {
        private Object _nullValue;
        private boolean _deserializeCalled = false;
        private boolean _typeDeserializeCalled = false;

        public DummyDeserializer(Object nullValue) {
            this._nullValue = nullValue;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            _deserializeCalled = true;
            return "deserialized";
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) {
            _deserializeCalled = true;
            if (intoValue instanceof Outer.Inner) {
                ((Outer.Inner) intoValue).value = "deserializedValue";
            }
            return intoValue;
        }

        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) {
            _typeDeserializeCalled = true;
            return "typedValue";
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt) {
            return _nullValue;
        }
    }

    // Tests construction and delegation of metadata methods
    @Test
    public void testConstructor_validDelegate_initializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        assertEquals("inner", prop.getName());
        assertNull(prop.getAnnotation(Override.class));
        assertNull(prop.getMember());
    }

    // Tests index assignment delegation
    @Test
    public void testAssignIndex_validIndex_delegatesToProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        prop.assignIndex(5);

        assertEquals(5, prop.getPropertyIndex());
    }

    // Tests withName copy method
    @Test
    public void testWithName_newName_createsCopyWithName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        InnerClassProperty renamedProp = prop.withName(new PropertyName("newInner"));

        assertEquals("newInner", renamedProp.getName());
        assertEquals("inner", prop.getName());
    }

    // Tests withValueDeserializer copy method
    @Test
    public void testWithValueDeserializer_newDeserializer_createsCopy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        DummyDeserializer deser = new DummyDeserializer("nullVal");
        InnerClassProperty deserProp = prop.withValueDeserializer(deser);

        assertNotNull(deserProp.getValueDeserializer());
        assertEquals(deser, deserProp.getValueDeserializer());
    }

    // Tests set and setAndReturn methods
    @Test
    public void testSetAndReturn_validInstance_delegatesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        Outer outer = new Outer();
        Object value = outer.new Inner();

        prop.set(outer, value);
        assertEquals(value, delegate.getAssignedValue());

        Object returned = prop.setAndReturn(outer, value);
        assertEquals(outer, returned);
        assertEquals(value, delegate.getAssignedValue());
    }

    // Tests deserializeAndSet when token is VALUE_NULL
    @Test
    public void testDeserializeAndSet_tokenNull_setsNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        DummyDeserializer deser = new DummyDeserializer("customNull");
        prop = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // position to VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Outer outer = new Outer();
        prop.deserializeAndSet(parser, ctxt, outer);

        assertEquals("customNull", delegate.getAssignedValue());
    }

    // Tests normal deserializeAndSet instantiating inner class
    @Test
    public void testDeserializeAndSet_normalCase_instantiatesInnerClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        DummyDeserializer deser = new DummyDeserializer(null);
        prop = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Outer outer = new Outer();
        prop.deserializeAndSet(parser, ctxt, outer);

        assertTrue(deser._deserializeCalled);
        assertNotNull(delegate.getAssignedValue());
        assertTrue(delegate.getAssignedValue() instanceof Outer.Inner);
        assertEquals("deserializedValue", ((Outer.Inner) delegate.getAssignedValue()).value);
    }

    // Tests deserializeAndSet exception handling during inner class creation
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeAndSet_instantiationThrows_unwrapsAndThrowsIAE() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(FailingOuter.FailingInner.class);
        DummyProperty delegate = new DummyProperty("failingInner", type);
        Constructor<?> ctor = FailingOuter.FailingInner.class.getConstructor(FailingOuter.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        DummyDeserializer deser = new DummyDeserializer(null);
        prop = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        FailingOuter outer = new FailingOuter();
        prop.deserializeAndSet(parser, ctxt, outer);
    }

    // Tests deserializeSetAndReturn
    @Test
    public void testDeserializeSetAndReturn_validInstance_deserializesAndReturns() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        DummyDeserializer deser = new DummyDeserializer(null);
        prop = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Outer outer = new Outer();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, outer);

        assertEquals(outer, result);
        assertEquals("deserialized", delegate.getAssignedValue());
    }

    // Tests JDK serialization readResolve and writeReplace
    @Test
    public void testWriteReplaceAndReadResolve_serializationLifecycle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        Object replaced = prop.writeReplace();
        assertTrue(replaced instanceof InnerClassProperty);

        // writeReplace again when _annotated is already set
        InnerClassProperty replacedProp = (InnerClassProperty) replaced;
        assertSame(replacedProp, replacedProp.writeReplace());

        Object resolved = replacedProp.readResolve();
        assertTrue(resolved instanceof InnerClassProperty);
    }

    // Tests serialization constructor with missing constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSerializationConstructor_missingConstructor_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        new InnerClassProperty(prop, (AnnotatedConstructor) null);
    }

    // Tests JDK serialization full round-trip
    @Test
    public void testJdkSerialization_fullRoundTrip_preservesProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Outer.Inner.class);
        DummyProperty delegate = new DummyProperty("inner", type);
        Constructor<?> ctor = Outer.Inner.class.getConstructor(Outer.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertTrue(deserialized instanceof InnerClassProperty);
        InnerClassProperty deserializedProp = (InnerClassProperty) deserialized;
        assertEquals("inner", deserializedProp.getName());
    }
}
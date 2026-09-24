package com.fasterxml.jackson.databind.deser.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class InnerClassPropertyTest {

    static class Outer {
        public Inner inner;

        public class Inner {
            public int val;
            public Inner() { }
        }

        public class FailingInner {
            public FailingInner() {
                throw new RuntimeException("Instantiate failed");
            }
        }
        public FailingInner failingInner;
    }

    // Tests constructor with null AnnotatedConstructor leading to IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullAnnotatedConstructor_throwsIllegalArgumentException() {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        InnerClassProperty prop = new InnerClassProperty(createDummyProperty("inner", Outer.Inner.class), ctor);
        new InnerClassProperty(prop, (AnnotatedConstructor) null);
    }

    // Tests withName method creating new instance with updated PropertyName
    @Test
    public void testWithName_validPropertyName_returnsNewInstanceWithName() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        PropertyName newName = new PropertyName("renamedInner");
        InnerClassProperty result = prop.withName(newName);

        assertNotNull(result);
        assertNotSame(prop, result);
        assertEquals("renamedInner", result.getName());
    }

    // Tests withValueDeserializer method creating new instance with updated deserializer
    @Test
    public void testWithValueDeserializer_validDeserializer_returnsNewInstanceWithDeserializer() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        InnerClassProperty result = prop.withValueDeserializer(newDeser);
        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(newDeser, result.getValueDeserializer());
    }

    // Tests index assignment and retrieval delegation
    @Test
    public void testAssignIndex_validIndex_delegatesCorrectly() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        prop.assignIndex(5);
        assertEquals(5, prop.getPropertyIndex());
    }

    // Tests getAnnotation delegation to delegate property
    @Test
    public void testGetAnnotation_nullAnnotation_returnsNull() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        assertNull(prop.getAnnotation(Deprecated.class));
    }

    // Tests getMember delegation to delegate property
    @Test
    public void testGetMember_existingDelegate_returnsMember() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        AnnotatedMember member = prop.getMember();
        assertNotNull(member);
        assertEquals("inner", member.getName());
    }

    // Tests set and setAndReturn delegation
    @Test
    public void testSetAndReturn_validInstanceAndValue_setsAndReturnsInstance() throws IOException {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        Outer outer = new Outer();
        Outer.Inner innerVal = outer.new Inner();
        innerVal.val = 42;

        Object returned = prop.setAndReturn(outer, innerVal);
        assertSame(outer, returned);
        assertSame(innerVal, outer.inner);
    }

    // Tests writeReplace when _annotated is null creates new instance with AnnotatedConstructor
    @Test
    public void testWriteReplace_annotatedIsNull_createsAnnotatedConstructorInstance() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        Object replaced = prop.writeReplace();
        assertNotNull(replaced);
        assertTrue(replaced instanceof InnerClassProperty);
        assertNotSame(prop, replaced);
    }

    // Tests writeReplace when _annotated is already set returns self
    @Test
    public void testWriteReplace_annotatedIsNotNull_returnsSelf() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        InnerClassProperty replaced = (InnerClassProperty) prop.writeReplace();
        assertSame(replaced, replaced.writeReplace());
    }

    // Tests readResolve returns a new InnerClassProperty instance
    @Test
    public void testReadResolve_validAnnotated_returnsNewInstance() {
        Constructor<?> ctor = Outer.Inner.class.getDeclaredConstructors()[0];
        SettableBeanProperty delegate = createDummyProperty("inner", Outer.Inner.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        InnerClassProperty prepProp = (InnerClassProperty) prop.writeReplace();
        Object resolved = prepProp.readResolve();

        assertNotNull(resolved);
        assertTrue(resolved instanceof InnerClassProperty);
    }

    // Tests deserializeAndSet handles VALUE_NULL token branch
    @Test
    public void testDeserializeAndSet_nullToken_setsNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Outer result = mapper.readValue("{\"inner\": null}", Outer.class);
        assertNotNull(result);
        assertNull(result.inner);
    }

    // Tests deserializeAndSet regular inner class instantiation and population
    @Test
    public void testDeserializeAndSet_validJson_instantiatesAndPopulatesInnerClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Outer result = mapper.readValue("{\"inner\": {\"val\": 99}}", Outer.class);
        assertNotNull(result);
        assertNotNull(result.inner);
        assertEquals(99, result.inner.val);
    }

    // Tests deserializeAndSet throws IllegalArgumentException on instantiation failure
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeAndSet_instantiationFails_throwsIllegalArgumentException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"failingInner\": {}}", Outer.class);
    }

    // Helper method to create a delegate SettableBeanProperty using ObjectMapper
    private SettableBeanProperty createDummyProperty(String propName, Class<?> propClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType outerType = TypeFactory.defaultInstance().constructType(Outer.class);
            List<BeanPropertyDefinition> props = mapper.deserializationConfig()
                    .introspect(outerType)
                    .findProperties();
            for (BeanPropertyDefinition propDef : props) {
                if (propDef.getName().equals(propName)) {
                    JavaType propType = TypeFactory.defaultInstance().constructType(propClass);
                    return MethodProperty.construct(
                            mapper.getDeserializationContext(),
                            propDef,
                            propType,
                            null,
                            0
                    );
                }
            }
            JavaType propType = TypeFactory.defaultInstance().constructType(propClass);
            return new MethodProperty(
                    new PropertyName(propName),
                    propType,
                    null,
                    null,
                    new AnnotationMap(),
                    null
            );
        } catch (Exception e) {
            JavaType propType = TypeFactory.defaultInstance().constructType(propClass);
            return new MethodProperty(
                    new PropertyName(propName),
                    propType,
                    null,
                    null,
                    new AnnotationMap(),
                    null
            );
        }
    }
}
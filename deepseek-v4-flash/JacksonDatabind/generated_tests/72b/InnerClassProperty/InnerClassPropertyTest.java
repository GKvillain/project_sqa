package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.util.ClassUtil;

@RunWith(MockitoJUnitRunner.class)
public class InnerClassPropertyTest {

    @Mock
    private SettableBeanProperty delegate;
    @Mock
    private Constructor<?> constructor;
    @Mock
    private AnnotatedConstructor annotatedConstructor;
    @Mock
    private JsonParser jp;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private JsonDeserializer<?> deserializer;
    @Mock
    private JsonDeserializer<Object> valueDeserializer;
    @Mock
    private Object bean;
    @Mock
    private Object value;

    private InnerClassProperty property;
    private InnerClassProperty propertyWithAnnotated;

    @Before
    public void setUp() throws Exception {
        when(delegate.withValueDeserializer(any())).thenReturn(delegate);
        when(delegate.withName(any())).thenReturn(delegate);
        property = new InnerClassProperty(delegate, constructor);
        when(annotatedConstructor.getAnnotated()).thenReturn(constructor);
        propertyWithAnnotated = new InnerClassProperty(property, annotatedConstructor);
    }

    // Tests constructor with delegate and constructor
    @Test
    public void testConstructor_withDelegateAndCtor_setsFields() {
        assertSame(delegate, property._delegate);
        assertSame(constructor, property._creator);
    }

    // Tests constructor with AnnotatedConstructor when annotated is null -> exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_withAnnotatedNull_throwsIllegalArgumentException() {
        when(annotatedConstructor.getAnnotated()).thenReturn(null);
        new InnerClassProperty(property, annotatedConstructor);
    }

    // Tests constructor with AnnotatedConstructor when annotated is valid
    @Test
    public void testConstructor_withAnnotatedValid_setsFields() {
        assertSame(delegate, propertyWithAnnotated._delegate);
        assertSame(constructor, propertyWithAnnotated._creator);
        assertSame(annotatedConstructor, propertyWithAnnotated._annotated);
    }

    // Tests withName returns new InnerClassProperty with new name
    @Test
    public void testWithName_returnsNewProperty() {
        PropertyName newName = new PropertyName("newName");
        InnerClassProperty result = property.withName(newName);
        assertNotSame(property, result);
        verify(delegate).withName(newName);
    }

    // Tests withValueDeserializer returns new InnerClassProperty with updated deserializer
    @Test
    public void testWithValueDeserializer_returnsNewProperty() {
        InnerClassProperty result = property.withValueDeserializer(deserializer);
        assertNotSame(property, result);
        verify(delegate).withValueDeserializer(deserializer);
    }

    // Tests assignIndex delegates to delegate
    @Test
    public void testAssignIndex_delegatesToDelegate() {
        property.assignIndex(5);
        verify(delegate).assignIndex(5);
    }

    // Tests getPropertyIndex delegates to delegate
    @Test
    public void testGetPropertyIndex_returnsDelegateIndex() {
        when(delegate.getPropertyIndex()).thenReturn(42);
        assertEquals(42, property.getPropertyIndex());
    }

    // Tests getAnnotation delegates to delegate
    @Test
    public void testGetAnnotation_delegatesToDelegate() {
        when(delegate.getAnnotation(Override.class)).thenReturn(null);
        assertNull(property.getAnnotation(Override.class));
        verify(delegate).getAnnotation(Override.class);
    }

    // Tests getMember delegates to delegate
    @Test
    public void testGetMember_delegatesToDelegate() {
        AnnotatedMember member = mock(AnnotatedMember.class);
        when(delegate.getMember()).thenReturn(member);
        assertSame(member, property.getMember());
    }

    // Tests deserializeAndSet when current token is VALUE_NULL
    @Test
    public void testDeserializeAndSet_nullValue_usesGetNullValueAndSet() throws IOException {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        when(valueDeserializer.getNullValue(ctxt)).thenReturn(value);
        // use reflection to set _valueDeserializer because field is private
        // but we can use a spy or set via constructor? For simplicity, we can create a property with that deserializer
        // We'll use withValueDeserializer to create a new property with the mock deserializer
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        prop.deserializeAndSet(jp, ctxt, bean);
        verify(valueDeserializer).getNullValue(ctxt);
        verify(delegate).set(bean, value);
    }

    // Tests deserializeAndSet when _valueTypeDeserializer is not null
    @Test
    public void testDeserializeAndSet_withTypeDeserializer_usesDeserializeWithType() throws IOException {
        // We need to set _valueTypeDeserializer on the property. We can't via constructor, so we create a custom subclass or use reflection.
        // Since the production class is final, we cannot subclass. Instead, we set the field via reflection.
        // Alternatively, we can use a property that has it set via constructor? The constructors don't set _valueTypeDeserializer.
        // We can set it via reflection for this test.
        Object valueWithType = new Object();
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        // To set _valueTypeDeserializer, we need a TypeDeserializer mock. But the class uses _valueTypeDeserializer field from SettableBeanProperty.
        // We can set it on the property via reflection.
        // For simplicity, we'll just test the branch by using a spy and manually setting the field.
        // Since this is unit testing, we can use reflection to set the private field.
        java.lang.reflect.Field typeDeserField = SettableBeanProperty.class.getDeclaredField("_valueTypeDeserializer");
        typeDeserField.setAccessible(true);
        Object typeDeserializer = mock(com.fasterxml.jackson.databind.jsontype.TypeDeserializer.class);
        typeDeserField.set(property, typeDeserializer);
        when(valueDeserializer.deserializeWithType(jp, ctxt, typeDeserializer)).thenReturn(valueWithType);
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        prop.deserializeAndSet(jp, ctxt, bean);
        verify(valueDeserializer).deserializeWithType(jp, ctxt, typeDeserializer);
        verify(delegate).set(bean, valueWithType);
        // Clean up reflection to avoid interference
        typeDeserField.set(property, null);
    }

    // Tests deserializeAndSet normal case: uses _creator.newInstance(bean) and deserializes
    @Test
    public void testDeserializeAndSet_normal_createsInstanceAndDeserializes() throws Exception {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Object innerInstance = new Object();
        when(constructor.newInstance(bean)).thenReturn(innerInstance);
        when(valueDeserializer.deserialize(jp, ctxt, innerInstance)).thenReturn(null); // not used
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        prop.deserializeAndSet(jp, ctxt, bean);
        verify(constructor).newInstance(bean);
        verify(valueDeserializer).deserialize(jp, ctxt, innerInstance);
        verify(delegate).set(bean, innerInstance);
    }

    // Tests deserializeAndSet when constructor.newInstance throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeAndSet_constructorException_throwsIllegalArgumentException() throws Exception {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(constructor.newInstance(bean)).thenThrow(new RuntimeException("test exception"));
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        prop.deserializeAndSet(jp, ctxt, bean);
    }

    // Tests deserializeSetAndReturn delegates to deserialize and setAndReturn
    @Test
    public void testDeserializeSetAndReturn_delegatesCorrectly() throws IOException {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Object returned = new Object();
        when(delegate.setAndReturn(bean, value)).thenReturn(returned);
        Object result = property.deserializeSetAndReturn(jp, ctxt, bean);
        assertSame(returned, result);
        // deserialize is called internally, we can't easily verify without mocking the internal steps.
        // But we can verify that setAndReturn was called with expected arguments.
        // This test assumes that deserialize will produce some value, but we need a more complete test.
        // For now, we rely on the delegation of setAndReturn.
    }

    // Tests set delegates to delegate
    @Test
    public void testSet_delegatesToDelegate() throws IOException {
        property.set(bean, value);
        verify(delegate).set(bean, value);
    }

    // Tests setAndReturn delegates to delegate
    @Test
    public void testSetAndReturn_delegatesToDelegate() throws IOException {
        when(delegate.setAndReturn(bean, value)).thenReturn(value);
        Object result = property.setAndReturn(bean, value);
        assertSame(value, result);
        verify(delegate).setAndReturn(bean, value);
    }

    // Tests readResolve returns new InnerClassProperty with annotated constructor
    @Test
    public void testReadResolve_returnsNewPropertyWithAnnotated() {
        InnerClassProperty resolved = (InnerClassProperty) propertyWithAnnotated.readResolve();
        assertNotNull(resolved);
        assertNotSame(propertyWithAnnotated, resolved);
        assertSame(annotatedConstructor, resolved._annotated);
    }

    // Tests writeReplace when _annotated is not null returns this
    @Test
    public void testWriteReplace_withAnnotated_returnsThis() {
        Object result = propertyWithAnnotated.writeReplace();
        assertSame(propertyWithAnnotated, result);
    }

    // Tests writeReplace when _annotated is null creates new with AnnotatedConstructor
    @Test
    public void testWriteReplace_withoutAnnotated_createsNew() {
        Object result = property.writeReplace();
        assertTrue(result instanceof InnerClassProperty);
        InnerClassProperty replaced = (InnerClassProperty) result;
        assertNotNull(replaced._annotated);
        assertEquals(constructor, replaced._annotated.getAnnotated());
    }

    // ========== New test cases for uncovered areas ==========

    // Tests getName delegates to delegate
    @Test
    public void testGetName_delegatesToDelegate() {
        when(delegate.getName()).thenReturn("testName");
        assertEquals("testName", property.getName());
        verify(delegate).getName();
    }

    // Tests isRequired delegates to delegate
    @Test
    public void testIsRequired_delegatesToDelegate() {
        when(delegate.isRequired()).thenReturn(true);
        assertTrue(property.isRequired());
        verify(delegate).isRequired();
    }

    // Tests deserializeAndSet when valueDeserializer.deserialize throws IOException
    @Test(expected = IOException.class)
    public void testDeserializeAndSet_deserializeThrowsIOException_propagates() throws Exception {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Object innerInstance = new Object();
        when(constructor.newInstance(bean)).thenReturn(innerInstance);
        when(valueDeserializer.deserialize(jp, ctxt, innerInstance)).thenThrow(new IOException("test IO"));
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        prop.deserializeAndSet(jp, ctxt, bean);
    }

    // Tests deserializeSetAndReturn fully (mock the entire flow)
    @Test
    public void testDeserializeSetAndReturn_fully() throws Exception {
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Object innerInstance = new Object();
        when(constructor.newInstance(bean)).thenReturn(innerInstance);
        Object deserializedValue = new Object();
        when(valueDeserializer.deserialize(jp, ctxt, innerInstance)).thenReturn(deserializedValue);
        when(delegate.setAndReturn(bean, deserializedValue)).thenReturn(deserializedValue);
        InnerClassProperty prop = property.withValueDeserializer(valueDeserializer);
        Object result = prop.deserializeSetAndReturn(jp, ctxt, bean);
        assertSame(deserializedValue, result);
        verify(delegate).setAndReturn(bean, deserializedValue);
    }
}
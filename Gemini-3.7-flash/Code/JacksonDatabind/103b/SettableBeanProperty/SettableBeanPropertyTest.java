package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

public class SettableBeanPropertyTest {

    private static class TestProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;

        private Object lastSetValue;
        private AnnotatedMember member;

        public TestProperty(PropertyName name, JavaType type, PropertyMetadata metadata,
                            JsonDeserializer<Object> deser) {
            super(name, type, metadata, deser);
        }

        public TestProperty(PropertyName name, JavaType type, PropertyName wrapper,
                            TypeDeserializer typeDeser, Annotations contextAnnotations,
                            PropertyMetadata metadata) {
            super(name, type, wrapper, typeDeser, contextAnnotations, metadata);
        }

        public TestProperty(TestProperty src) {
            super(src);
            this.member = src.member;
            this.lastSetValue = src.lastSetValue;
        }

        public TestProperty(TestProperty src, JsonDeserializer<?> deser, NullValueProvider nuller) {
            super(src, deser, nuller);
            this.member = src.member;
            this.lastSetValue = src.lastSetValue;
        }

        public TestProperty(TestProperty src, PropertyName newName) {
            super(src, newName);
            this.member = src.member;
            this.lastSetValue = src.lastSetValue;
        }

        public void setMember(AnnotatedMember m) {
            this.member = m;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new TestProperty(this, deser, _nullProvider);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new TestProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return new TestProperty(this, _valueDeserializer, nva);
        }

        @Override
        public AnnotatedMember getMember() {
            return member;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance)
                throws IOException {
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
            this.lastSetValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            this.lastSetValue = value;
            return instance;
        }
    }

    private static class TestDelegatingProperty extends SettableBeanProperty.Delegating {
        private static final long serialVersionUID = 1L;

        public TestDelegatingProperty(SettableBeanProperty delegate) {
            super(delegate);
        }

        @Override
        protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
            return new TestDelegatingProperty(d);
        }
    }

    private static final JavaType STRING_TYPE = TypeFactory.defaultInstance().constructType(String.class);

    // Tests null PropertyName initialization defaults to NO_NAME
    @Test
    public void testConstructor_nullPropertyName_defaultsToNoName() {
        TestProperty prop = new TestProperty(null, STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertEquals("", prop.getName());
        assertEquals(PropertyName.NO_NAME, prop.getFullName());
    }

    // Tests non-null PropertyName initialization
    @Test
    public void testConstructor_validPropertyName_setsCorrectName() {
        PropertyName propName = new PropertyName("myProp");
        TestProperty prop = new TestProperty(propName, STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertEquals("myProp", prop.getName());
        assertEquals(propName, prop.getFullName());
        assertEquals(STRING_TYPE, prop.getType());
        assertFalse(prop.hasValueDeserializer());
        assertFalse(prop.hasValueTypeDeserializer());
        assertNull(prop.getValueDeserializer());
        assertNull(prop.getValueTypeDeserializer());
    }

    // Tests assignIndex sets property index correctly on first call
    @Test
    public void testAssignIndex_firstAssignment_succeeds() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertEquals(-1, prop.getPropertyIndex());
        prop.assignIndex(5);
        assertEquals(5, prop.getPropertyIndex());
    }

    // Tests assignIndex throws IllegalStateException when assigned more than once
    @Test(expected = IllegalStateException.class)
    public void testAssignIndex_duplicateAssignment_throwsException() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        prop.assignIndex(1);
        prop.assignIndex(2);
    }

    // Tests getCreatorIndex throws IllegalStateException by default
    @Test(expected = IllegalStateException.class)
    public void testGetCreatorIndex_defaultImplementation_throwsIllegalStateException() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        prop.getCreatorIndex();
    }

    // Tests withSimpleName when new name is identical
    @Test
    public void testWithSimpleName_sameName_returnsSameInstance() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        SettableBeanProperty updated = prop.withSimpleName("prop");
        assertSame(prop, updated);
    }

    // Tests withSimpleName when new name is different
    @Test
    public void testWithSimpleName_differentName_returnsNewInstance() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        SettableBeanProperty updated = prop.withSimpleName("newProp");
        assertNotSame(prop, updated);
        assertEquals("newProp", updated.getName());
    }

    // Tests setManagedReferenceName and getManagedReferenceName
    @Test
    public void testManagedReferenceName_setAndGet() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertNull(prop.getManagedReferenceName());
        prop.setManagedReferenceName("refName");
        assertEquals("refName", prop.getManagedReferenceName());
    }

    // Tests setObjectIdInfo and getObjectIdInfo
    @Test
    public void testObjectIdInfo_setAndGet() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertNull(prop.getObjectIdInfo());
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        prop.setObjectIdInfo(info);
        assertSame(info, prop.getObjectIdInfo());
    }

    // Tests setViews and visibleInView behavior
    @Test
    public void testViews_configurationAndViewVisibility() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(Object.class));

        prop.setViews(new Class<?>[] { String.class });
        assertTrue(prop.hasViews());
        assertTrue(prop.visibleInView(String.class));
        assertFalse(prop.visibleInView(Integer.class));

        prop.setViews(null);
        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(Integer.class));
    }

    // Tests markAsIgnorable and isIgnorable
    @Test
    public void testIgnorable_defaultsToFalse() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertFalse(prop.isIgnorable());
        prop.markAsIgnorable();
        assertFalse(prop.isIgnorable());
    }

    // Tests toString formatting
    @Test
    public void testToString_formatsCorrectly() {
        TestProperty prop = new TestProperty(new PropertyName("myField"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertEquals("[property 'myField']", prop.toString());
    }

    // Tests _throwAsIOE with IllegalArgumentException wraps into JsonMappingException
    @Test
    public void testThrowAsIOE_withIllegalArgumentException_wrapsJsonMappingException() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        try {
            prop._throwAsIOE(new IllegalArgumentException("invalid argument"), 123);
            fail("Expected JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertTrue(e.getMessage().contains("Problem deserializing property 'prop'"));
            assertTrue(e.getMessage().contains("invalid argument"));
        }
    }

    // Tests _throwAsIOE with IOException rethrows directly
    @Test
    public void testThrowAsIOE_withIOException_rethrowsDirectly() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        IOException original = new IOException("io failure");
        try {
            prop._throwAsIOE(original, "val");
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(original, e);
        }
    }

    // Tests _throwAsIOE with RuntimeException rethrows directly
    @Test
    public void testThrowAsIOE_withRuntimeException_rethrowsDirectly() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        RuntimeException original = new NullPointerException("npe");
        try {
            prop._throwAsIOE(original, "val");
            fail("Expected RuntimeException");
        } catch (Exception e) {
            assertSame(original, e);
        }
    }

    // Tests _throwAsIOE with checked Exception wraps into JsonMappingException
    @Test
    public void testThrowAsIOE_withCheckedException_wrapsInJsonMappingException() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        Exception original = new Exception("checked failure");
        try {
            prop._throwAsIOE(original, "val");
            fail("Expected JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertSame(original, e.getCause());
        }
    }

    // Tests Delegating property delegation of base methods
    @Test
    public void testDelegatingProperty_delegatesCorrectly() throws IOException {
        TestProperty prop = new TestProperty(new PropertyName("delProp"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        prop.assignIndex(3);
        TestDelegatingProperty delegating = new TestDelegatingProperty(prop);

        assertSame(prop, delegating.getDelegate());
        assertEquals("delProp", delegating.getName());
        assertEquals(3, delegating.getPropertyIndex());
        assertFalse(delegating.hasValueDeserializer());
        assertFalse(delegating.hasValueTypeDeserializer());

        delegating.set("target", "value");
        assertEquals("value", prop.lastSetValue);

        Object ret = delegating.setAndReturn("target", "value2");
        assertEquals("target", ret);
        assertEquals("value2", prop.lastSetValue);

        SettableBeanProperty renamed = delegating.withName(new PropertyName("renamed"));
        assertTrue(renamed instanceof SettableBeanProperty.Delegating);
        assertEquals("renamed", renamed.getName());
    }

    // Tests Delegating _with returning same instance when delegate does not change
    @Test
    public void testDelegatingProperty_withSameName_returnsThis() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        TestDelegatingProperty delegating = new TestDelegatingProperty(prop);
        SettableBeanProperty result = delegating._with(prop);
        assertSame(delegating, result);
    }

    // Tests constructor with wrapper name, type deserializer, annotations, metadata
    @Test
    public void testFullConstructor_andPropertyAccessors() {
        PropertyName name = new PropertyName("fullProp");
        PropertyName wrapper = new PropertyName("wrapper");
        TestProperty prop = new TestProperty(name, STRING_TYPE, wrapper, null, null, PropertyMetadata.STD_REQUIRED);

        assertEquals("fullProp", prop.getName());
        assertEquals(wrapper, prop.getWrapperName());
        assertTrue(prop.isRequired());
        assertNull(prop.getValueTypeDeserializer());
        assertNull(prop.getContextAnnotation(JsonFormat.class));
    }

    // Tests withNullProvider and getNullValueProvider
    @Test
    public void testWithNullProvider() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        assertNull(prop.getNullValueProvider());

        NullValueProvider nuller = NullsConstantProvider.nuller();
        SettableBeanProperty updated = prop.withNullProvider(nuller);
        assertSame(nuller, updated.getNullValueProvider());
    }

    // Tests withValueDeserializer and hasValueDeserializer
    @Test
    public void testWithValueDeserializer() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) (JsonDeserializer<?>) com.fasterxml.jackson.databind.deser.std.StringDeserializer.instance;
        SettableBeanProperty updated = prop.withValueDeserializer(deser);

        assertTrue(updated.hasValueDeserializer());
        assertSame(deser, updated.getValueDeserializer());
    }

    // Tests _throwAsIOE with null parser overload
    @Test
    public void testThrowAsIOE_withParserOverload() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        try {
            prop._throwAsIOE((JsonParser) null, new IllegalArgumentException("test message"), "val");
            fail("Expected JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertTrue(e.getMessage().contains("test message"));
        }
    }

    // Tests Delegating extra forwarded methods
    @Test
    public void testDelegatingProperty_additionalForwarding() {
        TestProperty prop = new TestProperty(new PropertyName("delProp"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        prop.setViews(new Class<?>[] { String.class });
        prop.setManagedReferenceName("managedRef");
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("oid"), Object.class, null, null);
        prop.setObjectIdInfo(info);

        TestDelegatingProperty delegating = new TestDelegatingProperty(prop);

        assertEquals(STRING_TYPE, delegating.getType());
        assertEquals(PropertyName.NO_NAME, delegating.getWrapperName());
        assertTrue(delegating.hasViews());
        assertTrue(delegating.visibleInView(String.class));
        assertFalse(delegating.visibleInView(Integer.class));
        assertEquals("managedRef", delegating.getManagedReferenceName());
        assertSame(info, delegating.getObjectIdInfo());
        assertFalse(delegating.isIgnorable());
        assertNull(delegating.getMember());
        assertNull(delegating.getAnnotation(JsonFormat.class));
        assertNull(delegating.getContextAnnotation(JsonFormat.class));
        assertNull(delegating.getNullValueProvider());

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) (JsonDeserializer<?>) com.fasterxml.jackson.databind.deser.std.StringDeserializer.instance;
        SettableBeanProperty withDeser = delegating.withValueDeserializer(deser);
        assertTrue(withDeser.hasValueDeserializer());

        NullValueProvider nuller = NullsConstantProvider.nuller();
        SettableBeanProperty withNuller = delegating.withNullProvider(nuller);
        assertSame(nuller, withNuller.getNullValueProvider());
    }

    // Tests Delegating getCreatorIndex throws default exception
    @Test(expected = IllegalStateException.class)
    public void testDelegatingProperty_getCreatorIndex_throwsException() {
        TestProperty prop = new TestProperty(new PropertyName("prop"), STRING_TYPE, PropertyMetadata.STD_OPTIONAL, null);
        TestDelegatingProperty delegating = new TestDelegatingProperty(prop);
        delegating.getCreatorIndex();
    }
}
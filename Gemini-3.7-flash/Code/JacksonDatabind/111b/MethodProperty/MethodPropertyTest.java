package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class MethodPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnnotation {
        String value() default "test";
    }

    static class SimpleBean {
        private String value;
        private String returnValue;

        @CustomAnnotation("custom")
        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public SimpleBean setAndReturnSelf(String value) {
            this.returnValue = value;
            return this;
        }

        public void throwException(String value) {
            throw new IllegalArgumentException("Forced error: " + value);
        }
    }

    private ObjectMapper mapper;
    private MethodProperty methodProperty;
    private BeanPropertyDefinition propDef;
    private AnnotatedMethod annotatedMethod;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("value".equals(prop.getName())) {
                propDef = prop;
                break;
            }
        }
        annotatedMethod = propDef.getSetter();
        methodProperty = new MethodProperty(propDef, propDef.getPrimaryType(), null,
                desc.getClassAnnotations(), annotatedMethod);
    }

    // Tests getter methods for member and annotations
    @Test
    public void testGetMemberAndAnnotation_existingAnnotation_returnsMemberAndAnnotation() {
        assertEquals(annotatedMethod, methodProperty.getMember());
        CustomAnnotation ann = methodProperty.getAnnotation(CustomAnnotation.class);
        assertNotNull(ann);
        assertEquals("custom", ann.value());
        assertNull(methodProperty.getAnnotation(Override.class));
    }

    // Tests withName creates a new copy with the given PropertyName
    @Test
    public void testWithName_newPropertyName_returnsNewInstanceWithNewName() {
        PropertyName newName = new PropertyName("renamedValue");
        SettableBeanProperty renamed = methodProperty.withName(newName);

        assertNotSame(methodProperty, renamed);
        assertEquals(newName.getSimpleName(), renamed.getName());
    }

    // Tests withValueDeserializer returns same instance if deserializer is identical
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        SettableBeanProperty result = methodProperty.withValueDeserializer(methodProperty.getValueDeserializer());
        assertSame(methodProperty, result);
    }

    // Tests withValueDeserializer returns new instance when deserializer changes
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty result = methodProperty.withValueDeserializer(deser);

        assertNotSame(methodProperty, result);
        assertSame(deser, result.getValueDeserializer());
    }

    // Tests withNullProvider creates a new copy with the given NullValueProvider
    @Test
    public void testWithNullProvider_customNullProvider_returnsNewInstance() {
        NullValueProvider nva = NullsConstantProvider.nuller();
        SettableBeanProperty result = methodProperty.withNullProvider(nva);

        assertNotSame(methodProperty, result);
        assertSame(nva, result.getNullValueProvider());
    }

    // Tests fixAccess executes without error
    @Test
    public void testFixAccess_normalConfig_fixesAccessSuccessfully() {
        methodProperty.fixAccess(mapper.getDeserializationConfig());
        assertNotNull(methodProperty.getMember());
    }

    // Tests set directly invokes the underlying setter method
    @Test
    public void testSet_validValue_updatesTargetInstance() throws Exception {
        SimpleBean target = new SimpleBean();
        methodProperty.set(target, "hello");
        assertEquals("hello", target.getValue());
    }

    // Tests set handles invocation exception properly
    @Test(expected = JsonMappingException.class)
    public void testSet_exceptionInSetter_throwsJsonMappingException() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanPropertyDefinition errProp = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("throwException".equals(prop.getName())) {
                errProp = prop;
                break;
            }
        }
        MethodProperty errMp = new MethodProperty(errProp, errProp.getPrimaryType(), null,
                desc.getClassAnnotations(), errProp.getSetter());
        SimpleBean target = new SimpleBean();
        errMp.set(target, "fail");
    }

    // Tests setAndReturn invokes setter and returns target instance
    @Test
    public void testSetAndReturn_voidMethod_returnsInstance() throws Exception {
        SimpleBean target = new SimpleBean();
        Object result = methodProperty.setAndReturn(target, "world");
        assertSame(target, result);
        assertEquals("world", target.getValue());
    }

    // Tests setAndReturn invokes setter that returns non-null result
    @Test
    public void testSetAndReturn_methodReturningSelf_returnsResult() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanPropertyDefinition retProp = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("setAndReturnSelf".equals(prop.getName())) {
                retProp = prop;
                break;
            }
        }
        MethodProperty retMp = new MethodProperty(retProp, retProp.getPrimaryType(), null,
                desc.getClassAnnotations(), retProp.getSetter());
        SimpleBean target = new SimpleBean();
        Object result = retMp.setAndReturn(target, "chained");
        assertSame(target, result);
        assertEquals("chained", target.returnValue);
    }

    // Tests setAndReturn handles invocation exception properly
    @Test(expected = JsonMappingException.class)
    public void testSetAndReturn_exceptionInSetter_throwsJsonMappingException() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanPropertyDefinition errProp = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("throwException".equals(prop.getName())) {
                errProp = prop;
                break;
            }
        }
        MethodProperty errMp = new MethodProperty(errProp, errProp.getPrimaryType(), null,
                desc.getClassAnnotations(), errProp.getSetter());
        SimpleBean target = new SimpleBean();
        errMp.setAndReturn(target, "fail");
    }

    // Tests deserializeAndSet with a regular valid JSON string value
    @Test
    public void testDeserializeAndSet_validJsonToken_setsDeserializedValue() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty prop = methodProperty.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("\"deserializedValue\"");
        parser.nextToken();

        SimpleBean target = new SimpleBean();
        prop.deserializeAndSet(parser, mapper.getDeserializationContext(), target);
        parser.close();

        assertEquals("deserializedValue", target.getValue());
    }

    // Tests deserializeAndSet with null token when skipNulls is false
    @Test
    public void testDeserializeAndSet_nullTokenNotSkipping_setsNullValue() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty prop = methodProperty.withValueDeserializer(deser)
                .withNullProvider(NullsConstantProvider.nuller());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        SimpleBean target = new SimpleBean();
        target.setValue("before");
        prop.deserializeAndSet(parser, mapper.getDeserializationContext(), target);
        parser.close();

        assertNull(target.getValue());
    }

    // Tests deserializeAndSet with null token when skipNulls is true
    @Test
    public void testDeserializeAndSet_nullTokenSkipping_doesNotModifyTarget() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty prop = methodProperty.withValueDeserializer(deser)
                .withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        SimpleBean target = new SimpleBean();
        target.setValue("preserved");
        prop.deserializeAndSet(parser, mapper.getDeserializationContext(), target);
        parser.close();

        assertEquals("preserved", target.getValue());
    }

    // Tests deserializeSetAndReturn with valid JSON token
    @Test
    public void testDeserializeSetAndReturn_validJsonToken_returnsInstance() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty prop = methodProperty.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("\"returnedValue\"");
        parser.nextToken();

        SimpleBean target = new SimpleBean();
        Object result = prop.deserializeSetAndReturn(parser, mapper.getDeserializationContext(), target);
        parser.close();

        assertSame(target, result);
        assertEquals("returnedValue", target.getValue());
    }

    // Tests deserializeSetAndReturn with null token when skipNulls is true
    @Test
    public void testDeserializeSetAndReturn_nullTokenSkipping_returnsInstanceDirectly() throws Exception {
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(
                mapper.constructType(String.class));
        SettableBeanProperty prop = methodProperty.withValueDeserializer(deser)
                .withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        SimpleBean target = new SimpleBean();
        target.setValue("skipTest");
        Object result = prop.deserializeSetAndReturn(parser, mapper.getDeserializationContext(), target);
        parser.close();

        assertSame(target, result);
        assertEquals("skipTest", target.getValue());
    }

    // Tests readResolve JDK serialization support
    @Test
    public void testReadResolve_returnsReconstructedMethodProperty() {
        Object resolved = methodProperty.readResolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof MethodProperty);
        assertEquals(methodProperty.getName(), ((MethodProperty) resolved).getName());
    }
}
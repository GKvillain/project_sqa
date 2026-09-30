package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

public class FieldPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnnotation {
        String value() default "";
    }

    static class TargetBean {
        @CustomAnnotation("testAnnot")
        public String value;
        public String otherValue;
        public final String finalField = "fixed";
    }

    static class DummyPropertyDef extends BeanPropertyDefinition {
        private final PropertyName _name;

        public DummyPropertyDef(String name) {
            _name = PropertyName.construct(name);
        }

        @Override public PropertyName getFullName() { return _name; }
        @Override public String getName() { return _name.getSimpleName(); }
        @Override public PropertyName getWrapperName() { return null; }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return true; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getGetter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedField getField() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedParameter getConstructorParameter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getPrimaryMember() { return null; }
    }

    private FieldProperty createFieldProperty(String fieldName) throws Exception {
        Field rawField = TargetBean.class.getDeclaredField(fieldName);
        TypeResolutionContext.Basic trc = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(TargetBean.class).getBindings());
        AnnotationMap annMap = new AnnotationMap();
        for (java.lang.annotation.Annotation ann : rawField.getDeclaredAnnotations()) {
            annMap.add(ann);
        }
        AnnotatedField annField = new AnnotatedField(trc, rawField, annMap);
        JavaType type = TypeFactory.defaultInstance().constructType(rawField.getGenericType());
        BeanPropertyDefinition propDef = new DummyPropertyDef(fieldName);
        Annotations contextAnnotations = annMap;
        return new FieldProperty(propDef, type, null, contextAnnotations, annField);
    }

    // Tests getter methods (getMember, getAnnotation)
    @Test
    public void testGetMemberAndAnnotation_validField_returnsExpected() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        assertNotNull(prop.getMember());
        assertEquals("value", prop.getMember().getName());

        CustomAnnotation ann = prop.getAnnotation(CustomAnnotation.class);
        assertNotNull(ann);
        assertEquals("testAnnot", ann.value());

        assertNull(prop.getAnnotation(Override.class));
    }

    // Tests withName method
    @Test
    public void testWithName_newName_createsNewInstanceWithUpdatedName() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        PropertyName newName = new PropertyName("renamedValue");
        SettableBeanProperty renamedProp = prop.withName(newName);

        assertNotNull(renamedProp);
        assertNotSame(prop, renamedProp);
        assertEquals("renamedValue", renamedProp.getName());
    }

    // Tests withValueDeserializer identity check
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        SettableBeanProperty result = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, result);
    }

    // Tests withValueDeserializer and withNullProvider syncing (defect 111)
    @Test
    public void testWithValueDeserializer_syncNullProvider_updatesCorrectly() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        final AtomicReference<String> nullValueRef = new AtomicReference<String>("customNull");
        JsonDeserializer<Object> deserWithNull = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return nullValueRef.get();
            }
        };

        SettableBeanProperty withDeser = prop.withValueDeserializer(deserWithNull);
        assertNotNull(withDeser.getValueDeserializer());
        assertEquals(deserWithNull, withDeser.getValueDeserializer());

        SettableBeanProperty withNull = withDeser.withNullProvider(NullsConstantProvider.skipper());
        assertNotNull(withNull);
        assertNotSame(withDeser, withNull);
    }

    // Tests fixAccess method
    @Test
    public void testFixAccess_normalConfig_fixesFieldAccess() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        prop.fixAccess(config);
        assertTrue(prop._field.isAccessible());
    }

    // Tests set and setAndReturn normal cases
    @Test
    public void testSetAndReturn_validValue_setsFieldAndReturnsInstance() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        TargetBean bean = new TargetBean();
        
        prop.set(bean, "test1");
        assertEquals("test1", bean.value);

        Object result = prop.setAndReturn(bean, "test2");
        assertSame(bean, result);
        assertEquals("test2", bean.value);
    }

    // Tests set method throwing IOException on illegal access
    @Test(expected = IOException.class)
    public void testSet_finalFieldWithoutAccess_throwsException() throws Exception {
        FieldProperty prop = createFieldProperty("finalField");
        prop._field.setAccessible(false);
        TargetBean bean = new TargetBean();
        prop.set(bean, "newValue");
    }

    // Tests setAndReturn throwing IOException on illegal access
    @Test(expected = IOException.class)
    public void testSetAndReturn_finalFieldWithoutAccess_throwsException() throws Exception {
        FieldProperty prop = createFieldProperty("finalField");
        prop._field.setAccessible(false);
        TargetBean bean = new TargetBean();
        prop.setAndReturn(bean, "newValue");
    }

    // Tests deserializeAndSet and deserializeSetAndReturn with non-null token
    @Test
    public void testDeserializeAndSet_normalValue_setsSuccessfully() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"value\":\"abc\"}";
        TargetBean bean = mapper.readValue(json, TargetBean.class);
        assertEquals("abc", bean.value);
    }

    // Tests deserializeAndSet and deserializeSetAndReturn with null value and skipNulls
    static class SkipNullBean {
        @JsonSetter(nulls = Nulls.SKIP)
        public String str = "defaultStr";
    }

    @Test
    public void testDeserializeAndSet_nullValueWithSkipNulls_preservesDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"str\":null}";
        SkipNullBean bean = mapper.readValue(json, SkipNullBean.class);
        assertEquals("defaultStr", bean.str);
    }

    // Tests deserializeAndSet with null token and default null provider
    @Test
    public void testDeserializeAndSet_nullValue_setsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"value\":null}";
        TargetBean bean = mapper.readValue(json, TargetBean.class);
        assertNull(bean.value);
    }

    // Tests readResolve serialization reconstruction
    @Test
    public void testReadResolve_validProperty_reconstructsInstance() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        Object resolved = prop.readResolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof FieldProperty);
        FieldProperty resolvedProp = (FieldProperty) resolved;
        assertEquals(prop.getName(), resolvedProp.getName());
    }

    // Tests copy constructor exception when field is missing
    @Test(expected = IllegalArgumentException.class)
    public void testCopyConstructor_missingField_throwsIllegalArgumentException() throws Exception {
        TypeResolutionContext.Basic trc = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(TargetBean.class).getBindings());
        AnnotatedField missingAnnField = new AnnotatedField(trc, null, new AnnotationMap());
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyDefinition propDef = new DummyPropertyDef("missing");
        
        FieldProperty baseProp = new FieldProperty(propDef, type, null, new AnnotationMap(), missingAnnField);
        new FieldProperty(baseProp);
    }

    // Tests withNullProvider returning same instance when provider is identical
    @Test
    public void testWithNullProvider_sameProvider_returnsSameInstance() throws Exception {
        FieldProperty prop = createFieldProperty("value");
        SettableBeanProperty result = prop.withNullProvider(prop.getNullValueProvider());
        assertSame(prop, result);
    }

    // Tests direct deserializeAndSet and deserializeSetAndReturn methods
    @Test
    public void testDirectDeserializeAndSet() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldProperty prop = createFieldProperty("value");
        JsonDeserializer<Object> deser = mapper.findRootValueDeserializer(mapper.constructType(String.class));
        prop = (FieldProperty) prop.withValueDeserializer(deser);

        TargetBean bean = new TargetBean();
        JsonParser p = mapper.createParser("\"directValue\"");
        p.nextToken();
        prop.deserializeAndSet(p, mapper.getDeserializationContext(), bean);
        assertEquals("directValue", bean.value);
        p.close();

        p = mapper.createParser("\"returnValue\"");
        p.nextToken();
        Object result = prop.deserializeSetAndReturn(p, mapper.getDeserializationContext(), bean);
        assertSame(bean, result);
        assertEquals("returnValue", bean.value);
        p.close();
    }

    // Tests direct deserializeAndSet and deserializeSetAndReturn with skipNulls
    @Test
    public void testDirectDeserializeAndSet_withSkipNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldProperty prop = createFieldProperty("value");
        JsonDeserializer<Object> deser = mapper.findRootValueDeserializer(mapper.constructType(String.class));
        prop = (FieldProperty) prop.withValueDeserializer(deser).withNullProvider(NullsConstantProvider.skipper());

        TargetBean bean = new TargetBean();
        bean.value = "initial";

        JsonParser p = mapper.createParser("null");
        p.nextToken();
        prop.deserializeAndSet(p, mapper.getDeserializationContext(), bean);
        assertEquals("initial", bean.value);
        p.close();

        p = mapper.createParser("null");
        p.nextToken();
        Object result = prop.deserializeSetAndReturn(p, mapper.getDeserializationContext(), bean);
        assertSame(bean, result);
        assertEquals("initial", bean.value);
        p.close();
    }
}
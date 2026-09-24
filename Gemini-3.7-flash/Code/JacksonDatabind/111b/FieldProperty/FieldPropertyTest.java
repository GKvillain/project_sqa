package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class FieldPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface CustomAnnotation {
        String value() default "";
    }

    static class SampleBean {
        @CustomAnnotation("testValue")
        @JsonProperty("name")
        public String name;

        public Integer count;

        public final String readOnly = "fixed";
    }

    private ObjectMapper mapper;
    private DeserializationConfig config;
    private JavaType beanType;
    private BeanDescription beanDesc;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        beanType = mapper.constructType(SampleBean.class);
        beanDesc = config.introspect(beanType);
    }

    private FieldProperty createFieldProperty(String propName) {
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        for (BeanPropertyDefinition def : props) {
            if (def.getName().equals(propName)) {
                AnnotatedField field = def.getField();
                return new FieldProperty(def, def.getPrimaryType(), null,
                        beanDesc.getClassAnnotations(), field);
            }
        }
        throw new IllegalArgumentException("Property not found: " + propName);
    }

    // Tests getMember returns the underlying AnnotatedField
    @Test
    public void testGetMember_returnsAnnotatedField() {
        FieldProperty prop = createFieldProperty("name");
        assertNotNull(prop.getMember());
        assertEquals("name", prop.getMember().getName());
    }

    // Tests getAnnotation returns custom annotation and null when not present
    @Test
    public void testGetAnnotation_returnsAnnotationOrNull() {
        FieldProperty prop = createFieldProperty("name");
        CustomAnnotation ann = prop.getAnnotation(CustomAnnotation.class);
        assertNotNull(ann);
        assertEquals("testValue", ann.value());

        assertNull(prop.getAnnotation(Deprecated.class));
    }

    // Tests withName creates a new copy with updated PropertyName
    @Test
    public void testWithName_returnsNewInstanceWithUpdatedName() {
        FieldProperty prop = createFieldProperty("name");
        PropertyName newName = new PropertyName("newName");
        SettableBeanProperty renamedProp = prop.withName(newName);

        assertNotNull(renamedProp);
        assertNotSame(prop, renamedProp);
        assertEquals("newName", renamedProp.getName());
    }

    // Tests withValueDeserializer returns same instance if deserializer is unchanged
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() {
        FieldProperty prop = createFieldProperty("name");
        SettableBeanProperty sameProp = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, sameProp);
    }

    // Tests withValueDeserializer returns a new instance when deserializer differs
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() {
        FieldProperty prop = createFieldProperty("name");
        JsonDeserializer<?> deser = StringDeserializer.instance;
        SettableBeanProperty newProp = prop.withValueDeserializer(deser);

        assertNotNull(newProp);
        assertNotSame(prop, newProp);
        assertSame(deser, newProp.getValueDeserializer());
    }

    // Tests withNullProvider creates a new FieldProperty with specified provider
    @Test
    public void testWithNullProvider_returnsNewInstanceWithNullProvider() {
        FieldProperty prop = createFieldProperty("name");
        NullValueProvider nva = NullsConstantProvider.nuller();
        SettableBeanProperty newProp = prop.withNullProvider(nva);

        assertNotNull(newProp);
        assertNotSame(prop, newProp);
        assertSame(nva, newProp.getNullValueProvider());
    }

    // Tests fixAccess on field property
    @Test
    public void testFixAccess_succeedsWithoutException() {
        FieldProperty prop = createFieldProperty("name");
        prop.fixAccess(config);
    }

    // Tests set directly assigns value to the field on the instance
    @Test
    public void testSet_validValue_setsFieldOnInstance() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        SampleBean bean = new SampleBean();
        prop.set(bean, "hello");
        assertEquals("hello", bean.name);
    }

    // Tests setAndReturn directly assigns value and returns the instance
    @Test
    public void testSetAndReturn_validValue_setsFieldAndReturnsInstance() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        SampleBean bean = new SampleBean();
        Object result = prop.setAndReturn(bean, "world");
        assertSame(bean, result);
        assertEquals("world", bean.name);
    }

    // Tests set throws exception when setting on incompatible target instance
    @Test(expected = IOException.class)
    public void testSet_incompatibleInstance_throwsIOException() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop.set("NotASampleBeanInstance", "value");
    }

    // Tests setAndReturn throws exception when setting on incompatible target instance
    @Test(expected = IOException.class)
    public void testSetAndReturn_incompatibleInstance_throwsIOException() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop.setAndReturn("NotASampleBeanInstance", "value");
    }

    // Tests deserializeAndSet assigns deserialized value to instance
    @Test
    public void testDeserializeAndSet_normalValue_setsDeserializedField() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(StringDeserializer.instance);

        JsonParser parser = mapper.getFactory().createParser("\"deserializedName\"");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();

        prop.deserializeAndSet(parser, ctxt, bean);
        assertEquals("deserializedName", bean.name);
        parser.close();
    }

    // Tests deserializeSetAndReturn assigns deserialized value and returns instance
    @Test
    public void testDeserializeSetAndReturn_normalValue_returnsInstance() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(StringDeserializer.instance);

        JsonParser parser = mapper.getFactory().createParser("\"returnedName\"");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();

        Object result = prop.deserializeSetAndReturn(parser, ctxt, bean);
        assertSame(bean, result);
        assertEquals("returnedName", bean.name);
        parser.close();
    }

    // Tests deserializeAndSet when null token is encountered and skipNulls is false
    @Test
    public void testDeserializeAndSet_nullToken_setsNullValue() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(StringDeserializer.instance);
        prop = (FieldProperty) prop.withNullProvider(NullsConstantProvider.nuller());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();
        bean.name = "initial";

        prop.deserializeAndSet(parser, ctxt, bean);
        assertNull(bean.name);
        parser.close();
    }

    // Tests deserializeAndSet when skipNulls is true skips setting value
    @Test
    public void testDeserializeAndSet_nullTokenWithSkipNulls_doesNotModifyField() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(StringDeserializer.instance);
        prop = (FieldProperty) prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();
        bean.name = "keepMe";

        prop.deserializeAndSet(parser, ctxt, bean);
        assertEquals("keepMe", bean.name);
        parser.close();
    }

    // Tests deserializeSetAndReturn when skipNulls is true returns instance unchanged
    @Test
    public void testDeserializeSetAndReturn_nullTokenWithSkipNulls_returnsInstanceUnmodified() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(StringDeserializer.instance);
        prop = (FieldProperty) prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();
        bean.name = "keepMeReturn";

        Object result = prop.deserializeSetAndReturn(parser, ctxt, bean);
        assertSame(bean, result);
        assertEquals("keepMeReturn", bean.name);
        parser.close();
    }

    // Tests deserializeAndSet when deserializer returns null on string coercion and skipNulls is true
    @Test
    public void testDeserializeAndSet_deserializerReturnsNullWithSkipNulls_doesNotModifyField() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(NullifyingDeserializer.instance);
        prop = (FieldProperty) prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("\"dummy\"");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();
        bean.name = "preserve";

        prop.deserializeAndSet(parser, ctxt, bean);
        assertEquals("preserve", bean.name);
        parser.close();
    }

    // Tests deserializeSetAndReturn when deserializer returns null and skipNulls is true
    @Test
    public void testDeserializeSetAndReturn_deserializerReturnsNullWithSkipNulls_returnsInstanceUnmodified() throws Exception {
        FieldProperty prop = createFieldProperty("name");
        prop = (FieldProperty) prop.withValueDeserializer(NullifyingDeserializer.instance);
        prop = (FieldProperty) prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("\"dummy\"");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        SampleBean bean = new SampleBean();
        bean.name = "preserveReturn";

        Object result = prop.deserializeSetAndReturn(parser, ctxt, bean);
        assertSame(bean, result);
        assertEquals("preserveReturn", bean.name);
        parser.close();
    }

    // Tests readResolve returns a valid reconstituted FieldProperty
    @Test
    public void testReadResolve_validSource_returnsNewFieldProperty() {
        FieldProperty prop = createFieldProperty("name");
        Object resolved = prop.readResolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof FieldProperty);
        assertEquals("name", ((FieldProperty) resolved).getName());
    }
}
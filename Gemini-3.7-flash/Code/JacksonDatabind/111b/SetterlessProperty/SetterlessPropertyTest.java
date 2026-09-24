package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;

public class SetterlessPropertyTest {

    static class SetterlessListBean {
        private final List<String> values = new ArrayList<String>();

        public List<String> getValues() {
            return values;
        }
    }

    static class SetterlessMapBean {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public Map<String, Object> getMap() {
            return map;
        }
    }

    static class NullReturningBean {
        public List<String> getItems() {
            return null;
        }
    }

    static class ExceptionThrowingBean {
        public List<String> getItems() {
            throw new IllegalStateException("Simulated getter failure");
        }
    }

    static class SetterlessNullsFailBean {
        private final List<String> values = new ArrayList<String>();

        @JsonSetter(nulls = Nulls.FAIL)
        public List<String> getValues() {
            return values;
        }
    }

    private SetterlessProperty createSetterlessProperty(Class<?> beanClass, String getterName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(beanClass.getMethod(getterName).getGenericReturnType());
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(null, beanClass.getMethod(getterName), null, null);
        PropertyName propName = new PropertyName(getterName.substring(3).toLowerCase());
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                mapper.getDeserializationConfig(), annotatedMethod, propName);

        return new SetterlessProperty(propDef, type, null, null, annotatedMethod);
    }

    // Tests that set() throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_calledDirectly_throwsUnsupportedOperationException() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        prop.set(new SetterlessListBean(), new ArrayList<String>());
    }

    // Tests that setAndReturn() throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_calledDirectly_throwsUnsupportedOperationException() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        prop.setAndReturn(new SetterlessListBean(), new ArrayList<String>());
    }

    // Tests withName returns a new instance with updated PropertyName
    @Test
    public void testWithName_differentName_returnsNewInstanceWithUpdatedName() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        PropertyName newName = new PropertyName("renamedValues");
        SettableBeanProperty modified = prop.withName(newName);

        assertNotNull(modified);
        assertNotSame(prop, modified);
        assertEquals("renamedValues", modified.getName());
    }

    // Tests withValueDeserializer returns same instance when deserializer is identical
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        SettableBeanProperty same = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, same);
    }

    // Tests withValueDeserializer returns new instance when deserializer is different
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        JsonDeserializer<?> deser = NullifyingDeserializer.instance;
        SettableBeanProperty modified = prop.withValueDeserializer(deser);

        assertNotNull(modified);
        assertNotSame(prop, modified);
        assertSame(deser, modified.getValueDeserializer());
    }

    // Tests withNullProvider returns new instance with updated NullValueProvider
    @Test
    public void testWithNullProvider_customProvider_returnsNewInstance() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        NullValueProvider nva = NullifyingDeserializer.instance;
        SettableBeanProperty modified = prop.withNullProvider(nva);

        assertNotNull(modified);
        assertNotSame(prop, modified);
        assertSame(nva, modified.getNullValueProvider());
    }

    // Tests withValueDeserializer updates null provider if they were in sync
    @Test
    public void testWithValueDeserializer_whenDeserAndNullProviderSynced_updatesNullProvider() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        JsonDeserializer<?> deser1 = NullifyingDeserializer.instance;
        SettableBeanProperty withDeser1 = prop.withValueDeserializer(deser1).withNullProvider(deser1);

        JsonDeserializer<?> deser2 = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        SettableBeanProperty withDeser2 = withDeser1.withValueDeserializer(deser2);
        assertSame(deser2, withDeser2.getValueDeserializer());
        assertSame(deser2, withDeser2.getNullValueProvider());
    }

    // Tests fixAccess configures member access
    @Test
    public void testFixAccess_enabledOverride_succeedsWithoutException() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        prop.fixAccess(mapper.getDeserializationConfig());
        assertNotNull(prop.getMember());
    }

    // Tests getAnnotation and getMember delegations
    @Test
    public void testGetMemberAndAnnotation_validProperty_delegatesToAnnotatedMethod() throws Exception {
        SetterlessProperty prop = createSetterlessProperty(SetterlessListBean.class, "getValues");
        assertNotNull(prop.getMember());
        assertEquals("getValues", prop.getMember().getName());
        assertNull(prop.getAnnotation(Deprecated.class));
    }

    // Tests normal deserialization of setterless Collection property
    @Test
    public void testDeserialize_setterlessCollection_populatesExistingCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"values\":[\"item1\",\"item2\"]}";
        SetterlessListBean result = mapper.readValue(json, SetterlessListBean.class);

        assertNotNull(result);
        assertEquals(2, result.getValues().size());
        assertEquals("item1", result.getValues().get(0));
        assertEquals("item2", result.getValues().get(1));
    }

    // Tests normal deserialization of setterless Map property
    @Test
    public void testDeserialize_setterlessMap_populatesExistingMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"key1\":\"value1\",\"key2\":123}}";
        SetterlessMapBean result = mapper.readValue(json, SetterlessMapBean.class);

        assertNotNull(result);
        assertEquals(2, result.getMap().size());
        assertEquals("value1", result.getMap().get("key1"));
        assertEquals(123, result.getMap().get("key2"));
    }

    // Tests deserialization when json value is null (branch JsonToken.VALUE_NULL)
    @Test
    public void testDeserialize_nullToken_leavesCollectionUntouched() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"values\":null}";
        SetterlessListBean result = mapper.readValue(json, SetterlessListBean.class);

        assertNotNull(result);
        assertTrue(result.getValues().isEmpty());
    }

    // Tests deserialization when getter returns null, reports bad definition
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_getterReturnsNull_throwsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"items\":[\"a\",\"b\"]}", NullReturningBean.class);
    }

    // Tests deserialization when getter throws exception, wraps as IOException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_getterThrowsException_wrapsAndThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"items\":[\"a\",\"b\"]}", ExceptionThrowingBean.class);
    }

    // Tests deserializeSetAndReturn via ObjectMapper deserialization
    @Test
    public void testDeserializeSetAndReturn_validPayload_returnsPopulatedBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SetterlessListBean bean = new SetterlessListBean();
        bean.getValues().add("initial");
        SetterlessListBean updated = mapper.readerForUpdating(bean).readValue("{\"values\":[\"new\"]}");

        assertSame(bean, updated);
        assertTrue(updated.getValues().contains("initial"));
        assertTrue(updated.getValues().contains("new"));
    }
}
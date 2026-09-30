package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SettableAnyPropertyTest {

    static class TargetBean {
        public Map<Object, Object> mapField = new HashMap<Object, Object>();
        public Map<Object, Object> nullField = null;

        public void anySetterMethod(String name, Object value) {
            mapField.put(name, value);
        }

        public void failingSetterWithMsg(String name, Object value) {
            throw new IllegalArgumentException("Custom error");
        }

        public void failingSetterNoMsg(String name, Object value) {
            throw new IllegalArgumentException((String) null);
        }

        public void ioExceptionSetter(String name, Object value) throws IOException {
            throw new IOException("IO error");
        }

        public void runtimeExceptionSetter(String name, Object value) {
            throw new RuntimeException("Runtime error");
        }

        public void checkedExceptionSetter(String name, Object value) throws Exception {
            throw new Exception("Checked root cause");
        }
    }

    private ObjectMapper _mapper;
    private JavaType _javaType;
    private BeanProperty _property;
    private AnnotatedMethod _setterMethod;
    private AnnotatedMethod _failingSetterWithMsgMethod;
    private AnnotatedMethod _failingSetterNoMsgMethod;
    private AnnotatedMethod _ioExceptionSetterMethod;
    private AnnotatedMethod _runtimeExceptionSetterMethod;
    private AnnotatedMethod _checkedExceptionSetterMethod;
    private AnnotatedField _mapField;
    private AnnotatedField _nullField;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _javaType = TypeFactory.defaultInstance().constructType(String.class);
        _property = new BeanProperty.Bogus();

        BeanDescription desc = _mapper.getDeserializationConfig().introspect(
                TypeFactory.defaultInstance().constructType(TargetBean.class));

        for (AnnotatedMethod m : desc.getClassInfo().memberMethods()) {
            String name = m.getName();
            if ("anySetterMethod".equals(name)) {
                _setterMethod = m;
            } else if ("failingSetterWithMsg".equals(name)) {
                _failingSetterWithMsgMethod = m;
            } else if ("failingSetterNoMsg".equals(name)) {
                _failingSetterNoMsgMethod = m;
            } else if ("ioExceptionSetter".equals(name)) {
                _ioExceptionSetterMethod = m;
            } else if ("runtimeExceptionSetter".equals(name)) {
                _runtimeExceptionSetterMethod = m;
            } else if ("checkedExceptionSetter".equals(name)) {
                _checkedExceptionSetterMethod = m;
            }
        }

        for (AnnotatedField f : desc.getClassInfo().fields()) {
            if ("mapField".equals(f.getName())) {
                _mapField = f;
            } else if ("nullField".equals(f.getName())) {
                _nullField = f;
            }
        }
    }

    // Tests basic accessors and properties initialized via constructor
    @Test
    public void testGettersAndProperties_standardInitialization_returnsCorrectValues() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null, null);

        assertSame(_property, prop.getProperty());
        assertSame(_javaType, prop.getType());
        assertFalse(prop.hasValueDeserializer());
        assertTrue(prop.toString().contains("TargetBean"));
    }

    // Tests deprecated constructor initialization
    @Test
    public void testDeprecatedConstructor_initialization_succeeds() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null);

        assertSame(_property, prop.getProperty());
        assertSame(_javaType, prop.getType());
        assertFalse(prop.hasValueDeserializer());
    }

    // Tests withValueDeserializer creates a new instance with updated value deserializer
    @Test
    public void testWithValueDeserializer_customDeserializer_createsCopy() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null, null);
        assertFalse(prop.hasValueDeserializer());

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "dummy";
            }
        };

        SettableAnyProperty copy = prop.withValueDeserializer(deser);
        assertNotSame(prop, copy);
        assertTrue(copy.hasValueDeserializer());
    }

    // Tests fixAccess method
    @Test
    public void testFixAccess_withConfig_invokesFixAccess() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null, null);
        DeserializationConfig config = _mapper.getDeserializationConfig();
        prop.fixAccess(config);
    }

    // Tests readResolve when setter is null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadResolve_nullSetter_throwsException() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, null, _javaType, null, null, null);
        prop.readResolve();
    }

    // Tests readResolve with valid setter returns self
    @Test
    public void testReadResolve_validSetter_returnsSelf() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null, null);
        assertSame(prop, prop.readResolve());
    }

    // Tests set using AnnotatedMethod
    @Test
    public void testSet_annotatedMethod_successfullySetsProperty() throws IOException {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        prop.set(bean, "key1", "val1");
        assertEquals("val1", bean.mapField.get("key1"));
    }

    // Tests set using AnnotatedField with non-null map
    @Test
    public void testSet_annotatedField_successfullySetsProperty() throws IOException {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _mapField, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        prop.set(bean, "key2", "val2");
        assertEquals("val2", bean.mapField.get("key2"));
    }

    // Tests set using AnnotatedField when map field is null (noop branch)
    @Test
    public void testSet_annotatedFieldNull_doesNotThrow() throws IOException {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _nullField, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        prop.set(bean, "key3", "val3");
        assertNull(bean.nullField);
    }

    // Tests deserialize with VALUE_NULL token
    @Test
    public void testDeserialize_nullToken_returnsNullValue() throws IOException {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "unexpected";
            }

            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "customNull";
            }
        };

        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, deser, null);
        JsonParser p = new JsonFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        Object result = prop.deserialize(p, ctxt);
        assertEquals("customNull", result);
        p.close();
    }

    // Tests deserialize with regular token and without type deserializer
    @Test
    public void testDeserialize_regularValue_returnsDeserializedValue() throws IOException {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserializedStr";
            }
        };

        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, deser, null);
        JsonParser p = new JsonFactory().createParser("\"someString\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        Object result = prop.deserialize(p, ctxt);
        assertEquals("deserializedStr", result);
        p.close();
    }

    // Tests deserializeAndSet with key deserializer and normal flow
    @Test
    public void testDeserializeAndSet_withKeyDeserializer_setsTransformedKey() throws IOException {
        KeyDeserializer keyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return "prefix_" + key;
            }
        };
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserVal";
            }
        };

        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, keyDeser, deser, null);
        TargetBean bean = new TargetBean();
        JsonParser p = new JsonFactory().createParser("\"test\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        prop.deserializeAndSet(p, ctxt, bean, "myKey");
        assertEquals("deserVal", bean.mapField.get("prefix_myKey"));
        p.close();
    }

    // Tests deserializeAndSet throwing UnresolvedForwardReference without ObjectIdReader
    @Test(expected = JsonMappingException.class)
    public void testDeserializeAndSet_unresolvedForwardReferenceWithoutObjectIdReader_throwsJsonMappingException() throws IOException {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new UnresolvedForwardReference(p, "Unresolved ref");
            }
        };

        SettableAnyProperty prop = new SettableAnyProperty(_property, _setterMethod, _javaType, null, deser, null);
        TargetBean bean = new TargetBean();
        JsonParser p = new JsonFactory().createParser("\"test\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        try {
            prop.deserializeAndSet(p, ctxt, bean, "myKey");
        } finally {
            p.close();
        }
    }

    // Tests _throwAsIOE with IllegalArgumentException containing error message
    @Test
    public void testSet_illegalArgumentExceptionWithMessage_throwsJsonMappingExceptionWithDetails() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _failingSetterWithMsgMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            prop.set(bean, "prop", "val");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Problem deserializing \"any\" property 'prop'"));
            assertTrue(e.getMessage().contains("Custom error"));
        } catch (IOException e) {
            fail("Expected JsonMappingException but got: " + e);
        }
    }

    // Tests _throwAsIOE with IllegalArgumentException with null message
    @Test
    public void testSet_illegalArgumentExceptionWithoutMessage_throwsJsonMappingExceptionWithFallback() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _failingSetterNoMsgMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            prop.set(bean, "propNullMsg", "val");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Problem deserializing \"any\" property 'propNullMsg'"));
            assertTrue(e.getMessage().contains("(no error message provided)"));
        } catch (IOException e) {
            fail("Expected JsonMappingException but got: " + e);
        }
    }

    // Tests _throwAsIOE rethrowing IOException
    @Test(expected = IOException.class)
    public void testSet_ioException_rethrowsIOException() throws IOException {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _ioExceptionSetterMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();
        prop.set(bean, "propIO", "val");
    }

    // Tests _throwAsIOE rethrowing RuntimeException
    @Test(expected = RuntimeException.class)
    public void testSet_runtimeException_rethrowsRuntimeException() throws IOException {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _runtimeExceptionSetterMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();
        prop.set(bean, "propRTE", "val");
    }

    // Tests _throwAsIOE wrapping general checked Exception
    @Test
    public void testSet_checkedException_wrapsInJsonMappingException() {
        SettableAnyProperty prop = new SettableAnyProperty(_property, _checkedExceptionSetterMethod, _javaType, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            prop.set(bean, "propChecked", "val");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Checked root cause"));
        } catch (IOException e) {
            fail("Expected JsonMappingException but got: " + e);
        }
    }
}
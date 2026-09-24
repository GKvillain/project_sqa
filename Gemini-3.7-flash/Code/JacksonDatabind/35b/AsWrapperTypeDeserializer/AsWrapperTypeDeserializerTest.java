package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AsWrapperTypeDeserializerTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonTypeName("base")
    static class BaseObject {
        public int id;
    }

    @JsonTypeName("subObject")
    static class SubObject extends BaseObject {
        public String name;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, visible = true, property = "type")
    @JsonTypeName("subVisible")
    static class SubObjectWithVisibleTypeId extends BaseObject {
        public String type;
        public String name;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    static abstract class BaseWrapper<T> {
        public T value;
    }

    @JsonTypeName("arrayWrapper")
    static class ArrayWrapper extends BaseWrapper<List<String>> {
    }

    @JsonTypeName("scalarWrapper")
    static class ScalarWrapper extends BaseWrapper<String> {
    }

    // Tests getTypeInclusion returns WRAPPER_OBJECT
    @Test
    public void testGetTypeInclusion_default_returnsWrapperObject() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseObject.class);
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, null, "type", false, null);
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, deser.getTypeInclusion());
    }

    // Tests forProperty with same property returns same instance
    @Test
    public void testForProperty_sameProperty_returnsThis() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseObject.class);
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, null, "type", false, null);
        assertSame(deser, deser.forProperty(null));
    }

    // Tests forProperty with different property returns new instance
    @Test
    public void testForProperty_newProperty_returnsNewInstance() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseObject.class);
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, null, "type", false, null);
        BeanProperty prop = new BeanProperty.Std(PropertyName.construct("testProp"), baseType, null, null, null, PropertyMetadata.STD_OPTIONAL);
        com.fasterxml.jackson.databind.jsontype.TypeDeserializer result = deser.forProperty(prop);
        assertNotNull(result);
        assertNotSame(deser, result);
        assertTrue(result instanceof AsWrapperTypeDeserializer);
    }

    // Tests normal deserialization of an object wrapped in WRAPPER_OBJECT format
    @Test
    public void testDeserializeTypedFromObject_validObjectJson_deserializesSuccessfully() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObject.class);

        String json = "{\"subObject\":{\"id\":10,\"name\":\"testName\"}}";
        BaseObject result = mapper.readValue(json, BaseObject.class);

        assertNotNull(result);
        assertTrue(result instanceof SubObject);
        SubObject sub = (SubObject) result;
        assertEquals(10, sub.id);
        assertEquals("testName", sub.name);
    }

    // Tests deserialization when type id is configured to be visible in the target object
    @Test
    public void testDeserializeTypedFromObject_typeIdVisible_populatesTypeIdProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObjectWithVisibleTypeId.class);

        String json = "{\"subVisible\":{\"id\":20,\"name\":\"visibleTest\"}}";
        BaseObject result = mapper.readValue(json, BaseObject.class);

        assertNotNull(result);
        assertTrue(result instanceof SubObjectWithVisibleTypeId);
        SubObjectWithVisibleTypeId sub = (SubObjectWithVisibleTypeId) result;
        assertEquals(20, sub.id);
        assertEquals("visibleTest", sub.name);
        assertEquals("subVisible", sub.type);
    }

    // Tests deserialization of an array payload wrapped with type id
    @Test
    public void testDeserializeTypedFromArray_validArrayJson_deserializesSuccessfully() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ArrayWrapper.class);

        String json = "{\"arrayWrapper\":{\"value\":[\"a\",\"b\",\"c\"]}}";
        BaseWrapper<?> result = mapper.readValue(json, BaseWrapper.class);

        assertNotNull(result);
        assertTrue(result instanceof ArrayWrapper);
        ArrayWrapper wrapper = (ArrayWrapper) result;
        assertNotNull(wrapper.value);
        assertEquals(3, wrapper.value.size());
        assertEquals("a", wrapper.value.get(0));
    }

    // Tests deserialization of a scalar payload wrapped with type id
    @Test
    public void testDeserializeTypedFromScalar_validScalarJson_deserializesSuccessfully() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ScalarWrapper.class);

        String json = "{\"scalarWrapper\":{\"value\":\"helloScalar\"}}";
        BaseWrapper<?> result = mapper.readValue(json, BaseWrapper.class);

        assertNotNull(result);
        assertTrue(result instanceof ScalarWrapper);
        ScalarWrapper wrapper = (ScalarWrapper) result;
        assertEquals("helloScalar", wrapper.value);
    }

    // Tests exception path when root token is not START_OBJECT
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_notStartObject_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObject.class);

        String json = "[\"subObject\", {\"id\":1}]";
        mapper.readValue(json, BaseObject.class);
    }

    // Tests exception path when root object is empty and missing type id field
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyObject_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObject.class);

        String json = "{}";
        mapper.readValue(json, BaseObject.class);
    }

    // Tests exception path when trailing content prevents expected END_OBJECT
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_extraFieldAfterValue_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObject.class);

        String json = "{\"subObject\":{\"id\":1}, \"extra\":\"field\"}";
        mapper.readValue(json, BaseObject.class);
    }

    // Tests direct invocation of deserializeTypedFromAny
    @Test
    public void testDeserializeTypedFromAny_validJson_deserializesSuccessfully() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubObject.class);

        String json = "{\"subObject\":{\"id\":99,\"name\":\"anyTest\"}}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken();

        JavaType baseType = mapper.constructType(BaseObject.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
        AsWrapperTypeDeserializer deser = (AsWrapperTypeDeserializer) mapper.getDeserializationConfig()
                .findTypeDeserializer(baseType);

        Object result = deser.deserializeTypedFromAny(parser, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof SubObject);
        assertEquals(99, ((SubObject) result).id);
        assertEquals("anyTest", ((SubObject) result).name);
    }
}
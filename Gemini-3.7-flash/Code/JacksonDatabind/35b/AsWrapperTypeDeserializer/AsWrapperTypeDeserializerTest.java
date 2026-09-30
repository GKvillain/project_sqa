package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AsWrapperTypeDeserializerTest {

    private ObjectMapper mapper;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    static abstract class Animal {
        public String name;
    }

    @JsonTypeName("dog")
    static class Dog extends Animal {
        public boolean barks;
    }

    @JsonTypeName("cat")
    static class Cat extends Animal {
        public boolean purrs;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = VisibleTypeBean.class, name = "vbean")
    })
    static class VisibleTypeBean {
        public String type;
        public int value;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests getTypeInclusion returns WRAPPER_OBJECT
    @Test
    public void testGetTypeInclusion_returnsWrapperObject() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, deser.getTypeInclusion());
    }

    // Tests forProperty with same property returns this instance
    @Test
    public void testForProperty_sameProperty_returnsThis() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        assertSame(deser, deser.forProperty(null));
    }

    // Tests normal deserialization of WRAPPER_OBJECT format
    @Test
    public void testDeserialize_validWrapperObject_success() throws Exception {
        String json = "{\"dog\":{\"name\":\"Rex\",\"barks\":true}}";
        Animal animal = mapper.readValue(json, Animal.class);

        assertNotNull(animal);
        assertTrue(animal instanceof Dog);
        Dog dog = (Dog) animal;
        assertEquals("Rex", dog.name);
        assertTrue(dog.barks);
    }

    // Tests deserialization with another subtype
    @Test
    public void testDeserialize_anotherSubtype_success() throws Exception {
        String json = "{\"cat\":{\"name\":\"Whiskers\",\"purrs\":true}}";
        Animal animal = mapper.readValue(json, Animal.class);

        assertNotNull(animal);
        assertTrue(animal instanceof Cat);
        Cat cat = (Cat) animal;
        assertEquals("Whiskers", cat.name);
        assertTrue(cat.purrs);
    }

    // Tests deserialization when typeId is visible in bean property
    @Test
    public void testDeserialize_typeIdVisible_propertyPopulated() throws Exception {
        String json = "{\"vbean\":{\"value\":42}}";
        VisibleTypeBean bean = mapper.readValue(json, VisibleTypeBean.class);

        assertNotNull(bean);
        assertEquals(42, bean.value);
        assertEquals("vbean", bean.type);
    }

    // Tests exception thrown when JSON starts with array token instead of START_OBJECT
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_arrayToken_throwsException() throws Exception {
        String json = "[\"dog\",{\"name\":\"Rex\"}]";
        mapper.readValue(json, Animal.class);
    }

    // Tests exception thrown when JSON starts with scalar token instead of START_OBJECT
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_scalarToken_throwsException() throws Exception {
        String json = "\"dog\"";
        mapper.readValue(json, Animal.class);
    }

    // Tests exception thrown when wrapper object is empty
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyObject_throwsException() throws Exception {
        String json = "{}";
        mapper.readValue(json, Animal.class);
    }

    // Tests exception thrown when unknown type id is encountered
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unknownTypeId_throwsException() throws Exception {
        String json = "{\"unknown\":{\"name\":\"Rex\"}}";
        mapper.readValue(json, Animal.class);
    }

    // Tests exception thrown when wrapper object has extra tokens after deserialized value
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_extraFieldInWrapper_throwsException() throws Exception {
        String json = "{\"dog\":{\"name\":\"Rex\"},\"extra\":1}";
        mapper.readValue(json, Animal.class);
    }

    // Tests deserializeTypedFromArray calls internal deserializer correctly
    @Test
    public void testDeserializeTypedFromArray_validWrapper_success() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        idRes.registerSubtype(Dog.class, "dog");
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        String json = "{\"dog\":{\"name\":\"Rex\",\"barks\":true}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromArray(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
    }

    // Tests deserializeTypedFromScalar calls internal deserializer correctly
    @Test
    public void testDeserializeTypedFromScalar_validWrapper_success() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        idRes.registerSubtype(Dog.class, "dog");
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        String json = "{\"dog\":{\"name\":\"Rex\",\"barks\":true}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromScalar(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
    }

    // Tests deserializeTypedFromAny calls internal deserializer correctly
    @Test
    public void testDeserializeTypedFromAny_validWrapper_success() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        idRes.registerSubtype(Dog.class, "dog");
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        String json = "{\"dog\":{\"name\":\"Rex\",\"barks\":true}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromAny(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
    }

    // Tests deserialize when parser has not yet advanced to START_OBJECT (Defects4J bug 35)
    @Test
    public void testDeserialize_parserAtStart_handlesUnadvancedParser() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        idRes.registerSubtype(Dog.class, "dog");
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(baseType, idRes, "type", false, null);

        String json = "{\"dog\":{\"name\":\"Rex\",\"barks\":true}}";
        JsonParser p = mapper.getFactory().createParser(json);
        // parser is not advanced (p.getCurrentToken() is null)
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromObject(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Rex", ((Dog) result).name);
    }
}
package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AsPropertyTypeDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Helper classes for testing polymorphic deserialization
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public boolean barks;
    }

    static class Cat extends Animal {
        public int lives;
    }

    // Tests constructor and getTypeInclusion
    @Test
    public void testGetTypeInclusion_defaultConstructor_returnsPropertyInclusion() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        assertEquals(As.PROPERTY, deser.getTypeInclusion());
        assertEquals("type", deser.getPropertyName());
    }

    // Tests constructor with explicit As inclusion
    @Test
    public void testGetTypeInclusion_customInclusion_returnsConfiguredInclusion() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "@class", true, null, As.EXISTING_PROPERTY);

        assertEquals(As.EXISTING_PROPERTY, deser.getTypeInclusion());
        assertEquals("@class", deser.getPropertyName());
    }

    // Tests forProperty with same property reference returning this
    @Test
    public void testForProperty_sameProperty_returnsSelf() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        assertSame(deser, deser.forProperty(null));
    }

    // Tests forProperty with new property returning new instance
    @Test
    public void testForProperty_differentProperty_returnsNewInstance() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);
        BeanProperty.Bogus prop = new BeanProperty.Bogus();

        AsPropertyTypeDeserializer scopedDeser = (AsPropertyTypeDeserializer) deser.forProperty(prop);
        assertNotSame(deser, scopedDeser);
        assertEquals(deser.getTypeInclusion(), scopedDeser.getTypeInclusion());
        assertEquals(deser.getPropertyName(), scopedDeser.getPropertyName());
    }

    // Tests deserializing typed object when type property is first
    @Test
    public void testDeserializeTypedFromObject_typePropertyFirst_deserializesCorrectly() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        String json = "{\"type\":\"" + Dog.class.getName() + "\",\"name\":\"Rex\",\"barks\":true}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Dog);
        Dog dog = (Dog) result;
        assertEquals("Rex", dog.name);
        assertTrue(dog.barks);
        parser.close();
    }

    // Tests deserializing typed object when type property is not first (buffering required)
    @Test
    public void testDeserializeTypedFromObject_typePropertySecond_buffersAndDeserializesCorrectly() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        String json = "{\"name\":\"Whiskers\",\"type\":\"" + Cat.class.getName() + "\",\"lives\":9}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Cat);
        Cat cat = (Cat) result;
        assertEquals("Whiskers", cat.name);
        assertEquals(9, cat.lives);
        parser.close();
    }

    // Tests deserializeTypedFromObject when typeIdVisible is true
    @Test
    public void testDeserializeTypedFromObject_typeIdVisibleTrue_preservesTypeId() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", true, null);

        String json = "{\"type\":\"" + Dog.class.getName() + "\",\"name\":\"Buddy\",\"barks\":false}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Dog);
        Dog dog = (Dog) result;
        assertEquals("Buddy", dog.name);
        assertFalse(dog.barks);
        parser.close();
    }

    // Tests deserializeTypedFromObject falling back to defaultImpl when type id is missing
    @Test
    public void testDeserializeTypedFromObject_missingTypeIdWithDefaultImpl_usesDefaultImpl() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(Dog.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, defaultImpl);

        String json = "{\"name\":\"Rover\",\"barks\":true}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Dog);
        Dog dog = (Dog) result;
        assertEquals("Rover", dog.name);
        assertTrue(dog.barks);
        parser.close();
    }

    // Tests deserializeTypedFromObject with missing type id and no defaultImpl throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeTypedFromObject_missingTypeIdWithoutDefaultImpl_throwsException() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        String json = "{\"name\":\"Rover\"}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.deserializeTypedFromObject(parser, ctxt);
        parser.close();
    }

    // Tests deserializeTypedFromAny with START_ARRAY token delegates to array deserialization
    @Test
    public void testDeserializeTypedFromAny_startArray_delegatesToArrayDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        String json = "[\"" + Dog.class.getName() + "\",{\"name\":\"Spike\",\"barks\":true}]";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_ARRAY

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromAny(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Spike", ((Dog) result).name);
        parser.close();
    }

    // Tests deserializeTypedFromAny with START_OBJECT delegates to object deserialization
    @Test
    public void testDeserializeTypedFromAny_startObject_delegatesToObjectDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        String json = "{\"type\":\"" + Cat.class.getName() + "\",\"name\":\"Kitty\",\"lives\":7}";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // point to START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromAny(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Cat);
        assertEquals("Kitty", ((Cat) result).name);
        parser.close();
    }

    // Tests deserializeTypedFromObject when empty string is passed with ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
    @Test
    public void testDeserializeTypedFromObject_emptyStringWithFeatureEnabled_returnsNull() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idRes, "type", false, null);

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        JsonParser parser = customMapper.getFactory().createParser("\"\"");
        parser.nextToken(); // point to VALUE_STRING

        DeserializationContext ctxt = customMapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNull(result);
        parser.close();
    }
}
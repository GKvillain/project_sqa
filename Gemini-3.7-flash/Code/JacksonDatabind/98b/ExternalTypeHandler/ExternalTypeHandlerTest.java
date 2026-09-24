package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExternalTypeHandlerTest
{
    private final ObjectMapper MAPPER = new ObjectMapper();

    // Helper polymorphic hierarchy
    interface Animal { }

    static class Dog implements Animal {
        public String name;
        public Dog() { }
        public Dog(String name) { this.name = name; }
    }

    static class Cat implements Animal {
        public int lives;
        public Cat() { }
        public Cat(int lives) { this.lives = lives; }
    }

    // Helper container with external type property (field/setter based)
    static class AnimalContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal animal;

        public AnimalContainer() { }
        public AnimalContainer(String type, Animal animal) {
            this.type = type;
            this.animal = animal;
        }
    }

    // Helper container with external type property and @JsonCreator
    static class AnimalCreatorContainer {
        public final String type;
        public final Animal animal;

        @JsonCreator
        public AnimalCreatorContainer(
                @JsonProperty("type") String type,
                @JsonProperty("animal")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
                    @JsonSubTypes.Type(value = Cat.class, name = "cat")
                }) Animal animal) {
            this.type = type;
            this.animal = animal;
        }
    }

    // Helper container with defaultImpl
    static class AnimalDefaultContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Dog.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal animal;
    }

    // Helper container with defaultImpl and @JsonCreator
    static class AnimalDefaultCreatorContainer {
        public final String type;
        public final Animal animal;

        @JsonCreator
        public AnimalDefaultCreatorContainer(
                @JsonProperty("type") String type,
                @JsonProperty("animal")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Dog.class)
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
                    @JsonSubTypes.Type(value = Cat.class, name = "cat")
                }) Animal animal) {
            this.type = type;
            this.animal = animal;
        }
    }

    // Helper container with multiple external typed properties
    static class MultiContainer {
        public String type1;
        public String type2;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type1")
        @JsonSubTypes({ @JsonSubTypes.Type(value = Dog.class, name = "dog") })
        public Animal animal1;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type2")
        @JsonSubTypes({ @JsonSubTypes.Type(value = Cat.class, name = "cat") })
        public Animal animal2;
    }

    // Tests normal deserialization where type id comes before value
    @Test
    public void testDeserialization_typeIdBeforeValue_success() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":{\"name\":\"Rex\"}}";
        AnimalContainer container = MAPPER.readValue(json, AnimalContainer.class);
        assertNotNull(container);
        assertEquals("dog", container.type);
        assertTrue(container.animal instanceof Dog);
        assertEquals("Rex", ((Dog) container.animal).name);
    }

    // Tests normal deserialization where value comes before type id
    @Test
    public void testDeserialization_valueBeforeTypeId_success() throws Exception {
        String json = "{\"animal\":{\"lives\":9},\"type\":\"cat\"}";
        AnimalContainer container = MAPPER.readValue(json, AnimalContainer.class);
        assertNotNull(container);
        assertEquals("cat", container.type);
        assertTrue(container.animal instanceof Cat);
        assertEquals(9, ((Cat) container.animal).lives);
    }

    // Tests @JsonCreator deserialization with type id before value
    @Test
    public void testCreatorDeserialization_typeIdBeforeValue_success() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":{\"name\":\"Spike\"}}";
        AnimalCreatorContainer container = MAPPER.readValue(json, AnimalCreatorContainer.class);
        assertNotNull(container);
        assertEquals("dog", container.type);
        assertTrue(container.animal instanceof Dog);
        assertEquals("Spike", ((Dog) container.animal).name);
    }

    // Tests @JsonCreator deserialization with value before type id
    @Test
    public void testCreatorDeserialization_valueBeforeTypeId_success() throws Exception {
        String json = "{\"animal\":{\"lives\":7},\"type\":\"cat\"}";
        AnimalCreatorContainer container = MAPPER.readValue(json, AnimalCreatorContainer.class);
        assertNotNull(container);
        assertEquals("cat", container.type);
        assertTrue(container.animal instanceof Cat);
        assertEquals(7, ((Cat) container.animal).lives);
    }

    // Tests defaultImpl support when type id property is omitted
    @Test
    public void testDefaultImpl_missingTypeId_usesDefaultImpl() throws Exception {
        String json = "{\"animal\":{\"name\":\"DefaultDog\"}}";
        AnimalDefaultContainer container = MAPPER.readValue(json, AnimalDefaultContainer.class);
        assertNotNull(container);
        assertTrue(container.animal instanceof Dog);
        assertEquals("DefaultDog", ((Dog) container.animal).name);
    }

    // Tests defaultImpl with @JsonCreator when type id property is omitted
    @Test
    public void testDefaultImplCreator_missingTypeId_usesDefaultImpl() throws Exception {
        String json = "{\"animal\":{\"name\":\"DefaultDogCreator\"}}";
        AnimalDefaultCreatorContainer container = MAPPER.readValue(json, AnimalDefaultCreatorContainer.class);
        assertNotNull(container);
        assertTrue(container.animal instanceof Dog);
        assertEquals("DefaultDogCreator", ((Dog) container.animal).name);
    }

    // Tests multiple external properties deserialized correctly
    @Test
    public void testMultipleExternalProperties_success() throws Exception {
        String json = "{\"type1\":\"dog\",\"animal1\":{\"name\":\"Buddy\"},\"type2\":\"cat\",\"animal2\":{\"lives\":5}}";
        MultiContainer container = MAPPER.readValue(json, MultiContainer.class);
        assertNotNull(container);
        assertTrue(container.animal1 instanceof Dog);
        assertEquals("Buddy", ((Dog) container.animal1).name);
        assertTrue(container.animal2 instanceof Cat);
        assertEquals(5, ((Cat) container.animal2).lives);
    }

    // Tests missing external type id when no defaultImpl is configured throws exception
    @Test(expected = JsonProcessingException.class)
    public void testMissingTypeId_withoutDefaultImpl_throwsException() throws Exception {
        String json = "{\"animal\":{\"name\":\"Fido\"}}";
        MAPPER.readValue(json, AnimalContainer.class);
    }

    // Tests missing property value when type id is provided throws exception
    @Test(expected = JsonProcessingException.class)
    public void testMissingPropertyValue_withTypeId_throwsException() throws Exception {
        String json = "{\"type\":\"dog\"}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        mapper.readValue(json, AnimalContainer.class);
    }

    // Tests null value handling for external property
    @Test
    public void testNullPropertyValue_success() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":null}";
        AnimalContainer container = MAPPER.readValue(json, AnimalContainer.class);
        assertNotNull(container);
        assertEquals("dog", container.type);
        assertNull(container.animal);
    }

    // Tests ExternalTypeHandler.builder() instantiation and basic start() functionality
    @Test
    public void testBuilderAndStart_notNull() {
        JavaType javaType = MAPPER.constructType(AnimalContainer.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);
        assertNotNull(builder);

        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ExternalTypeHandler handler = builder.build(propMap);
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests handlePropertyValue with unknown property name returns false
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws IOException {
        JavaType javaType = MAPPER.constructType(AnimalContainer.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ExternalTypeHandler handler = builder.build(propMap).start();

        JsonParser parser = MAPPER.getFactory().createParser("{\"unknown\":\"val\"}");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        boolean handled = handler.handlePropertyValue(parser, ctxt, "unknown", new AnimalContainer());
        assertFalse(handled);
        parser.close();
    }

    // Tests handleTypePropertyValue with unknown property name returns false
    @Test
    public void testHandleTypePropertyValue_unknownProperty_returnsFalse() throws IOException {
        JavaType javaType = MAPPER.constructType(AnimalContainer.class);
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(javaType);
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ExternalTypeHandler handler = builder.build(propMap).start();

        JsonParser parser = MAPPER.getFactory().createParser("{\"unknown\":\"val\"}");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        boolean handled = handler.handleTypePropertyValue(parser, ctxt, "unknown", new AnimalContainer());
        assertFalse(handled);
        parser.close();
    }
}
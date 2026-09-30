package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ExternalTypeHandlerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Helper classes for testing polymorphic external type handling
    interface Vehicle { }

    static class Car implements Vehicle {
        public int wheels = 4;
    }

    static class Truck implements Vehicle {
        public int wheels = 18;
    }

    static class VehicleContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Vehicle vehicle;
    }

    static class VehicleContainerDefault {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Car.class)
        public Vehicle vehicle;
    }

    static class NaturalTypeContainer {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object value;
    }

    static class CreatorContainer {
        public String type;
        public Vehicle vehicle;

        public CreatorContainer(
                @com.fasterxml.jackson.annotation.JsonProperty("type") String type,
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @com.fasterxml.jackson.annotation.JsonProperty("vehicle") Vehicle vehicle) {
            this.type = type;
            this.vehicle = vehicle;
        }
    }

    static class CreatorContainerDefault {
        public String type;
        public Vehicle vehicle;

        public CreatorContainerDefault(
                @com.fasterxml.jackson.annotation.JsonProperty("type") String type,
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Car.class)
                @com.fasterxml.jackson.annotation.JsonProperty("vehicle") Vehicle vehicle) {
            this.type = type;
            this.vehicle = vehicle;
        }
    }

    static class CustomResolverContainer {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonTypeIdResolver(CustomResolver.class)
        public Vehicle vehicle;
        public String type;
    }

    static class CustomResolver extends TypeIdResolverBase {
        @Override
        public String idFromValue(Object value) {
            return idFromValueAndType(value, value.getClass());
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            if (suggestedType == Car.class) return "car_type";
            if (suggestedType == Truck.class) return "truck_type";
            return "unknown";
        }

        @Override
        public com.fasterxml.jackson.databind.JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) {
            if ("car_type".equals(id)) return context.constructType(Car.class);
            if ("truck_type".equals(id)) return context.constructType(Truck.class);
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    // Tests normal deserialization when type property comes first
    @Test
    public void testDeserialize_typeFirst_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\",\"vehicle\":{\"wheels\":4}}";
        VehicleContainer result = mapper.readValue(json, VehicleContainer.class);

        assertNotNull(result);
        assertEquals("Car", result.type);
        assertTrue(result.vehicle instanceof Car);
        assertEquals(4, ((Car) result.vehicle).wheels);
    }

    // Tests normal deserialization when value property comes first
    @Test
    public void testDeserialize_valueFirst_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":18},\"type\":\"Truck\"}";
        VehicleContainer result = mapper.readValue(json, VehicleContainer.class);

        assertNotNull(result);
        assertEquals("Truck", result.type);
        assertTrue(result.vehicle instanceof Truck);
        assertEquals(18, ((Truck) result.vehicle).wheels);
    }

    // Tests handling of null value when external type is provided
    @Test
    public void testDeserialize_nullValue_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\",\"vehicle\":null}";
        VehicleContainer result = mapper.readValue(json, VehicleContainer.class);

        assertNotNull(result);
        assertEquals("Car", result.type);
        assertNull(result.vehicle);
    }

    // Tests missing both external type and value property (allowed to remain null)
    @Test
    public void testDeserialize_missingBothTypeAndValue_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{}";
        VehicleContainer result = mapper.readValue(json, VehicleContainer.class);

        assertNotNull(result);
        assertNull(result.type);
        assertNull(result.vehicle);
    }

    // Tests exception path when value is provided but external type id is missing
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingTypeId_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":4}}";
        mapper.readValue(json, VehicleContainer.class);
    }

    // Tests exception path when type id is provided but value property is missing
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_missingValueProperty_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\"}";
        mapper.readValue(json, VehicleContainer.class);
    }

    // Tests fallback to defaultImpl when type id is missing
    @Test
    public void testDeserialize_defaultImpl_missingTypeId_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":4}}";
        VehicleContainerDefault result = mapper.readValue(json, VehicleContainerDefault.class);

        assertNotNull(result);
        assertTrue(result.vehicle instanceof Car);
    }

    // Tests deserialization of natural types (Integer scalar) without explicit type id
    @Test
    public void testDeserialize_naturalTypeInteger_success() throws IOException {
        String json = "{\"value\":123}";
        NaturalTypeContainer result = MAPPER.readValue(json, NaturalTypeContainer.class);

        assertNotNull(result);
        assertEquals(Integer.valueOf(123), result.value);
    }

    // Tests deserialization of natural types (String scalar) without explicit type id
    @Test
    public void testDeserialize_naturalTypeString_success() throws IOException {
        String json = "{\"value\":\"test\"}";
        NaturalTypeContainer result = MAPPER.readValue(json, NaturalTypeContainer.class);

        assertNotNull(result);
        assertEquals("test", result.value);
    }

    // Tests deserialization of natural types (Boolean scalar) without explicit type id
    @Test
    public void testDeserialize_naturalTypeBoolean_success() throws IOException {
        String json = "{\"value\":true}";
        NaturalTypeContainer result = MAPPER.readValue(json, NaturalTypeContainer.class);

        assertNotNull(result);
        assertEquals(Boolean.TRUE, result.value);
    }

    // Tests creator-based deserialization with external type id
    @Test
    public void testDeserialize_creatorBased_typeFirst_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\",\"vehicle\":{\"wheels\":4}}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertEquals("Car", result.type);
        assertTrue(result.vehicle instanceof Car);
        assertEquals(4, ((Car) result.vehicle).wheels);
    }

    // Tests creator-based deserialization when value comes first
    @Test
    public void testDeserialize_creatorBased_valueFirst_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":18},\"type\":\"Truck\"}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertEquals("Truck", result.type);
        assertTrue(result.vehicle instanceof Truck);
        assertEquals(18, ((Truck) result.vehicle).wheels);
    }

    // Tests creator-based deserialization with null value
    @Test
    public void testDeserialize_creatorBased_nullValue_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\",\"vehicle\":null}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertEquals("Car", result.type);
        assertNull(result.vehicle);
    }

    // Tests creator-based deserialization with missing both properties
    @Test
    public void testDeserialize_creatorBased_missingBoth_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{}";
        CreatorContainer result = mapper.readValue(json, CreatorContainer.class);

        assertNotNull(result);
        assertNull(result.type);
        assertNull(result.vehicle);
    }

    // Tests creator-based deserialization exception on missing type id
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorBased_missingTypeId_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":4}}";
        mapper.readValue(json, CreatorContainer.class);
    }

    // Tests creator-based deserialization exception on missing value
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_creatorBased_missingValue_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"type\":\"Car\"}";
        mapper.readValue(json, CreatorContainer.class);
    }

    // Tests creator-based fallback to defaultImpl when type id is missing
    @Test
    public void testDeserialize_creatorBased_defaultImpl_success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(Car.class, Truck.class);

        String json = "{\"vehicle\":{\"wheels\":4}}";
        CreatorContainerDefault result = mapper.readValue(json, CreatorContainerDefault.class);

        assertNotNull(result);
        assertTrue(result.vehicle instanceof Car);
    }

    // Tests external type deserialization using custom TypeIdResolver
    @Test
    public void testDeserialize_customTypeIdResolver_success() throws IOException {
        String json = "{\"type\":\"car_type\",\"vehicle\":{\"wheels\":4}}";
        CustomResolverContainer result = MAPPER.readValue(json, CustomResolverContainer.class);

        assertNotNull(result);
        assertEquals("car_type", result.type);
        assertTrue(result.vehicle instanceof Car);
    }

    // Tests start method creating a non-blueprint instance copy
    @Test
    public void testStart_createsFreshHandlerInstance() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        assertNotNull(started);
        assertNotSame(handler, started);
    }

    // Tests handlePropertyValue returns false for unrelated properties
    @Test
    public void testHandlePropertyValue_unknownProperty_returnsFalse() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build().start();

        com.fasterxml.jackson.core.JsonParser p = MAPPER.getFactory().createParser("{\"dummy\":1}");
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        assertFalse(handler.handlePropertyValue(p, ctxt, "unknownProp", new Object()));
        assertFalse(handler.handleTypePropertyValue(p, ctxt, "unknownProp", new Object()));
        p.close();
    }
}
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AsPropertyTypeDeserializerTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type")
    static abstract class Shape {
        public int id;
    }

    @JsonTypeName("circle")
    static class Circle extends Shape {
        public int radius;
    }

    @JsonTypeName("square")
    static class Square extends Shape {
        public int length;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type", defaultImpl = Circle.class)
    static abstract class ShapeWithDefault {
        public int id;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type", visible = true)
    static class ShapeWithVisibleId {
        public String type;
        public int value;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
    static abstract class ShapeWithExistingProperty {
        public String type;
        public int id;
    }

    @JsonTypeName("circle")
    static class ExistingCircle extends ShapeWithExistingProperty {
        public int radius;
    }

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerSubtypes(Circle.class, Square.class, ExistingCircle.class);
    }

    // Tests constructor and getTypeInclusion method
    @Test
    public void testGetTypeInclusion_defaultConstructor_returnsPropertyInclusion() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);

        assertEquals(As.PROPERTY, deser.getTypeInclusion());
    }

    // Tests constructor with explicit inclusion type
    @Test
    public void testGetTypeInclusion_explicitInclusion_returnsConfiguredInclusion() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null, As.EXISTING_PROPERTY);

        assertEquals(As.EXISTING_PROPERTY, deser.getTypeInclusion());
    }

    // Tests forProperty returning same instance when property matches
    @Test
    public void testForProperty_sameProperty_returnsSameInstance() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);

        assertSame(deser, deser.forProperty(null));
    }

    // Tests forProperty returning new instance when property is different
    @Test
    public void testForProperty_differentProperty_returnsNewInstance() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);
        BeanProperty.Bogus prop = new BeanProperty.Bogus();

        com.fasterxml.jackson.databind.jsontype.TypeDeserializer newDeser = deser.forProperty(prop);
        assertNotSame(deser, newDeser);
        assertEquals(As.PROPERTY, newDeser.getTypeInclusion());
    }

    // Tests deserializing object where type id is the first field
    @Test
    public void testDeserializeTypedFromObject_typeIdFirst_deserializesCorrectSubtype() throws IOException {
        String json = "{\"@type\":\"circle\",\"radius\":5,\"id\":1}";
        Shape shape = mapper.readValue(json, Shape.class);

        assertNotNull(shape);
        assertTrue(shape instanceof Circle);
        Circle circle = (Circle) shape;
        assertEquals(5, circle.radius);
        assertEquals(1, circle.id);
    }

    // Tests deserializing object where type id appears after other fields (buffering required)
    @Test
    public void testDeserializeTypedFromObject_typeIdLast_deserializesCorrectSubtype() throws IOException {
        String json = "{\"length\":10,\"id\":2,\"@type\":\"square\"}";
        Shape shape = mapper.readValue(json, Shape.class);

        assertNotNull(shape);
        assertTrue(shape instanceof Square);
        Square square = (Square) shape;
        assertEquals(10, square.length);
        assertEquals(2, square.id);
    }

    // Tests deserializing with typeIdVisible set to true
    @Test
    public void testDeserializeTypedFromObject_typeIdVisible_keepsTypeIdInObject() throws IOException {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.registerSubtypes(ShapeWithVisibleId.class);
        String json = "{\"value\":42,\"@type\":\"" + ShapeWithVisibleId.class.getName() + "\"}";
        
        ShapeWithVisibleId result = localMapper.readValue(json, ShapeWithVisibleId.class);
        assertNotNull(result);
        assertEquals(42, result.value);
    }

    // Tests deserialization using defaultImpl when type id is missing
    @Test
    public void testDeserializeTypedFromObject_missingTypeIdWithDefaultImpl_usesDefaultImpl() throws IOException {
        String json = "{\"id\":3}";
        ShapeWithDefault shape = mapper.readValue(json, ShapeWithDefault.class);

        assertNotNull(shape);
        assertTrue(shape instanceof Circle);
        assertEquals(3, shape.id);
    }

    // Tests exception when type id is missing and no defaultImpl is defined
    @Test(expected = JsonMappingException.class)
    public void testDeserializeTypedFromObject_missingTypeIdWithoutDefaultImpl_throwsException() throws IOException {
        String json = "{\"id\":3,\"radius\":5}";
        mapper.readValue(json, Shape.class);
    }

    // Tests deserializeTypedFromAny with JSON array token delegating to array deserialization
    @Test
    public void testDeserializeTypedFromAny_arrayToken_delegatesToArrayDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);

        JsonParser parser = mapper.getFactory().createParser("[\"circle\",{\"radius\":5}]");
        parser.nextToken(); // Move to START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromAny(parser, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Circle);
        parser.close();
    }

    // Tests deserializeTypedFromAny with JSON object token delegating to object deserializer
    @Test
    public void testDeserializeTypedFromAny_objectToken_delegatesToObjectDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);

        JsonParser parser = mapper.getFactory().createParser("{\"@type\":\"circle\",\"radius\":7}");
        parser.nextToken(); // Move to START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = deser.deserializeTypedFromAny(parser, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof Circle);
        parser.close();
    }

    // Tests deserializing empty string when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled (Defects4J 74 bug check)
    @Test
    public void testDeserializeTypedFromObject_emptyStringWithFeatureEnabled_returnsNull() throws IOException {
        ObjectMapper configuredMapper = new ObjectMapper();
        configuredMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        configuredMapper.registerSubtypes(Circle.class, Square.class);

        Shape shape = configuredMapper.readValue("\"\"", Shape.class);
        assertNull(shape);
    }

    // Tests deserialization using As.EXISTING_PROPERTY inclusion
    @Test
    public void testDeserializeTypedFromObject_existingProperty_deserializesCorrectly() throws IOException {
        String json = "{\"type\":\"circle\",\"radius\":12,\"id\":4}";
        ShapeWithExistingProperty shape = mapper.readValue(json, ShapeWithExistingProperty.class);

        assertNotNull(shape);
        assertTrue(shape instanceof ExistingCircle);
        ExistingCircle circle = (ExistingCircle) shape;
        assertEquals(12, circle.radius);
        assertEquals(4, circle.id);
        assertEquals("circle", circle.type);
    }

    // Tests _deserializeTypedUsingDefaultImpl when parser starts with FIELD_NAME
    @Test
    public void testDeserializeTypedFromObject_startingAtFieldName_deserializesCorrectly() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        JavaType defaultType = TypeFactory.defaultInstance().constructType(Circle.class);
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, defaultType);

        JsonParser parser = mapper.getFactory().createParser("{\"radius\":15,\"id\":5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME ("radius")

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserializeTypedFromObject(parser, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof Circle);
        assertEquals(15, ((Circle) result).radius);
        assertEquals(5, ((Circle) result).id);
        parser.close();
    }

    // Tests _deserializeTypedUsingDefaultImpl directly with a TokenBuffer
    @Test
    public void testDeserializeTypedUsingDefaultImpl_withBufferedTokens_success() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        JavaType defaultType = TypeFactory.defaultInstance().constructType(Circle.class);
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, defaultType);

        JsonParser parser = mapper.getFactory().createParser("{\"radius\":20}");
        parser.nextToken(); // START_OBJECT

        DeserializationContext ctxt = mapper.getDeserializationContext();
        TokenBuffer tb = new TokenBuffer(parser, ctxt);
        tb.writeStartObject();
        tb.writeFieldName("id");
        tb.writeNumber(99);

        Object result = deser._deserializeTypedUsingDefaultImpl(parser, ctxt, tb);

        assertNotNull(result);
        assertTrue(result instanceof Circle);
        Circle circle = (Circle) result;
        assertEquals(99, circle.id);
        assertEquals(20, circle.radius);
        parser.close();
        tb.close();
    }

    // Tests deserializeTypedFromAny with non-object/non-array token throwing exception when no defaultImpl
    @Test(expected = JsonMappingException.class)
    public void testDeserializeTypedFromAny_scalarTokenWithoutDefaultImpl_throwsException() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Shape.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, null);

        JsonParser parser = mapper.getFactory().createParser("12345");
        parser.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = mapper.getDeserializationContext();

        deser.deserializeTypedFromAny(parser, ctxt);
        parser.close();
    }
}
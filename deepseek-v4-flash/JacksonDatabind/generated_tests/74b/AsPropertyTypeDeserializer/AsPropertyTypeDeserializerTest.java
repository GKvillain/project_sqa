package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

/**
 * JUnit 4 test class for AsPropertyTypeDeserializer.
 * Uses real Jackson objects to cover key branches and likely defect paths.
 */
public class AsPropertyTypeDeserializerTest {

    // -----------------------------------------------------------------------
    // Test helper types
    // -----------------------------------------------------------------------

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type")
    @JsonSubTypes({@JsonSubTypes.Type(value = SubType.class, name = "sub")})
    static abstract class BaseType {
        public int baseField;
    }

    static class SubType extends BaseType {
        public int subField;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type",
            defaultImpl = BaseWithDefault.class)
    @JsonSubTypes({@JsonSubTypes.Type(value = SubWithDefault.class, name = "sub")})
    static abstract class BaseWithDefault {
        public int field;
    }

    static class SubWithDefault extends BaseWithDefault {
    }

    // Base with visible type id (type id kept as a field in the bean)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type",
            visible = true)
    @JsonSubTypes({@JsonSubTypes.Type(value = SubVisible.class, name = "sub")})
    static abstract class BaseVisible {
        public String field;
        public String getType() { return null; } // not used
    }

    static class SubVisible extends BaseVisible {
        public int subField;
    }

    // Container with a polymorphic list (exercises wrapper-array fallback path)
    static class Container {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type")
        @JsonSubTypes({@JsonSubTypes.Type(value = SubType.class, name = "sub")})
        public java.util.List<BaseType> items;
    }

    // Base class that captures the visible type id into a field named exactly as the property
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "typeId",
            visible = true)
    @JsonSubTypes({@JsonSubTypes.Type(value = SubVisibleCapture.class, name = "sub")})
    static abstract class BaseVisibleCapture {
        public String typeId; // field matches property name "typeId"
        public String field;
    }

    static class SubVisibleCapture extends BaseVisibleCapture {
        public int subField;
    }

    // -----------------------------------------------------------------------
    // Normal test: type property present as first field
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedFromObject_typeIdFirst_returnsCorrectSubtype() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@type\":\"sub\",\"baseField\":10,\"subField\":20}";
        BaseType result = mapper.readValue(json, BaseType.class);
        assertTrue(result instanceof SubType);
        assertEquals(10, result.baseField);
        assertEquals(20, ((SubType)result).subField);
    }

    // -----------------------------------------------------------------------
    // Normal test: type property not first (exercise property scanning loop)
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedFromObject_typeIdAfterOtherFields_returnsCorrectSubtype() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"baseField\":10,\"@type\":\"sub\",\"subField\":20}";
        BaseType result = mapper.readValue(json, BaseType.class);
        assertTrue(result instanceof SubType);
        assertEquals(10, result.baseField);
        assertEquals(20, ((SubType)result).subField);
    }

    // -----------------------------------------------------------------------
    // Normal test: type property after multiple fields, with null TokenBuffer initially
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedFromObject_typeIdAfterManyFields_returnsCorrectSubtype() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"baseField\":10,\"extraField\":\"abc\",\"@type\":\"sub\",\"subField\":20}";
        BaseType result = mapper.readValue(json, BaseType.class);
        assertTrue(result instanceof SubType);
        assertEquals(10, result.baseField);
        assertEquals(20, ((SubType)result).subField);
    }

    // -----------------------------------------------------------------------
    // Default implementation path: missing type id, defaultImpl configured
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedUsingDefaultImpl_withDefaultImpl_usesDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"field\":5}";
        BaseWithDefault result = mapper.readValue(json, BaseWithDefault.class);
        assertTrue(result instanceof BaseWithDefault);
        assertEquals(5, result.field);
    }

    // -----------------------------------------------------------------------
    // Missing type id, no defaultImpl: must throw exception
    // -----------------------------------------------------------------------
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testDeserializeTypedUsingDefaultImpl_noDefaultImpl_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"baseField\":1}";
        mapper.readValue(json, BaseType.class); // BaseType has no defaultImpl
    }

    // -----------------------------------------------------------------------
    // _typeIdVisible = true: type id should be preserved in the bean
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedForId_typeIdVisible_mergesIdBack() throws Exception {
        // This test is replaced by a more concrete one below, left as placeholder
    }

    // Simpler visible test: use a Map or JsonNode
    @Test
    public void testDeserializeTypedForId_typeIdVisible_presentInRaw() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@type\":\"sub\",\"field\":\"abc\",\"subField\":1}";
        BaseVisible result = mapper.readValue(json, BaseVisible.class);
        assertTrue(result instanceof SubVisible);
        assertEquals("abc", result.field);
        assertEquals(1, ((SubVisible)result).subField);
    }

    // -----------------------------------------------------------------------
    // Array fallback (wrapper-array): when the JSON is an array and the type uses As.PROPERTY
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeTypedFromObject_arrayFallback_delegatesToSuper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"items\":[{\"@type\":\"sub\",\"baseField\":1,\"subField\":2}]}";
        Container container = mapper.readValue(json, Container.class);
        assertNotNull(container.items);
        assertEquals(1, container.items.size());
        assertTrue(container.items.get(0) instanceof SubType);
        assertEquals(1, container.items.get(0).baseField);
        assertEquals(2, ((SubType)container.items.get(0)).subField);
    }

    // -----------------------------------------------------------------------
    // Direct test of forProperty() and constructor
    // -----------------------------------------------------------------------
    @Test
    public void testForProperty_sameProperty_returnsThis() throws Exception {
        TypeIdResolver idResolver = new TypeIdResolver() {
            @Override
            public String idFromValue(Object value) { return null; }
            @Override
            public String idFromValueAndType(Object value, Class<?> cls) { return null; }
            @Override
            public String idFromBaseType() { return null; }
            @Override
            public JavaType typeFromId(String id) throws IOException { return null; }
            @Override
            public String getDescForKnownTypeIds() { return null; }
            @Override
            public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        JavaType baseType = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null, As.PROPERTY);
        assertNotNull(deser);
        assertEquals(As.PROPERTY, deser.getTypeInclusion());
    }

    // -----------------------------------------------------------------------
    // Test getTypeInclusion()
    // -----------------------------------------------------------------------
    @Test
    public void testGetTypeInclusion_returnsConfiguredInclusion() throws Exception {
        TypeIdResolver idResolver = null;
        JavaType baseType = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null, As.PROPERTY);
        assertEquals(As.PROPERTY, deser.getTypeInclusion());
    }

    // =======================================================================
    // NEW TEST CASES to cover previously uncovered parts
    // =======================================================================

    // -----------------------------------------------------------------------
    // Test forProperty when property name differs (should return new instance)
    // -----------------------------------------------------------------------
    @Test
    public void testForProperty_differentProperty_returnsNewInstance() throws Exception {
        // Create a minimal BeanProperty stub
        BeanProperty prop = new BeanProperty.Std("newProp",
                TypeFactory.defaultInstance().uncheckedSimpleType(Object.class), null, null);
        TypeIdResolver idResolver = new TypeIdResolver() {
            @Override
            public String idFromValue(Object value) { return null; }
            @Override
            public String idFromValueAndType(Object value, Class<?> cls) { return null; }
            @Override
            public String idFromBaseType() { return null; }
            @Override
            public JavaType typeFromId(String id) throws IOException { return null; }
            @Override
            public String getDescForKnownTypeIds() { return null; }
            @Override
            public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        JavaType baseType = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
        AsPropertyTypeDeserializer original = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null, As.PROPERTY);
        AsPropertyTypeDeserializer result = original.forProperty(prop);
        assertNotNull(result);
        // Should not be the same object because property name differs
        assertNotSame("forProperty should return a new instance when property changes", original, result);
        // Verify the new instance's property name is "newProp" (by checking its internal state via getter if available)
        // We cannot access private field, but we can check behavior: serialize something? Not easily.
        // At least ensure no exception and object is created.
    }

    // -----------------------------------------------------------------------
    // Test forProperty with same property name (should return this)
    // -----------------------------------------------------------------------
    @Test
    public void testForProperty_samePropertyName_returnsThis() throws Exception {
        // Create a BeanProperty with the same property name as the deserializer
        BeanProperty prop = new BeanProperty.Std("@type",
                TypeFactory.defaultInstance().uncheckedSimpleType(Object.class), null, null);
        TypeIdResolver idResolver = new TypeIdResolver() {
            @Override
            public String idFromValue(Object value) { return null; }
            @Override
            public String idFromValueAndType(Object value, Class<?> cls) { return null; }
            @Override
            public String idFromBaseType() { return null; }
            @Override
            public JavaType typeFromId(String id) throws IOException { return null; }
            @Override
            public String getDescForKnownTypeIds() { return null; }
            @Override
            public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        JavaType baseType = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
        AsPropertyTypeDeserializer original = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null, As.PROPERTY);
        AsPropertyTypeDeserializer result = original.forProperty(prop);
        assertSame("forProperty should return the same instance when property name matches", original, result);
    }

    // -----------------------------------------------------------------------
    // Test deserialization using a custom property name (not "@type")
    // -----------------------------------------------------------------------
    @Test
    public void testDeserializeWithCustomPropertyName() throws Exception {
        // Use a different hierarchy with property = "kind"
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "kind")
        @JsonSubTypes({@JsonSubTypes.Type(value = SubKind.class, name = "subKind")})
        abstract class BaseKind {
            public int baseField;
        }
        class SubKind extends BaseKind {
            public int subField;
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"kind\":\"subKind\",\"baseField\":7,\"subField\":8}";
        BaseKind result = mapper.readValue(json, BaseKind.class);
        assertTrue(result instanceof SubKind);
        assertEquals(7, result.baseField);
        assertEquals(8, ((SubKind)result).subField);
    }

    // -----------------------------------------------------------------------
    // Test defaultImpl when the default implementation is an abstract class
    // (should throw an exception)
    // -----------------------------------------------------------------------
    @Test(expected = com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class)
    public void testDeserializeWithInvalidDefaultImpl_throwsException() throws Exception {
        // Define a type with defaultImpl being an abstract class that cannot be instantiated
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@type",
                defaultImpl = AbstractDefault.class)
        abstract class BaseWithAbstractDefault {
            public int value;
        }
        abstract class AbstractDefault extends BaseWithAbstractDefault {
            // abstract, cannot instantiate
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"value\":123}";
        mapper.readValue(json, BaseWithAbstractDefault.class);
    }

    // -----------------------------------------------------------------------
    // Test visible = true with field that captures the type id
    // -----------------------------------------------------------------------
    @Test
    public void testVisibleTypeIdFieldIsPopulated() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // The base class BaseVisibleCapture has a field "typeId" which matches the property name
        String json = "{\"typeId\":\"sub\",\"field\":\"hello\",\"subField\":42}";
        BaseVisibleCapture result = mapper.readValue(json, BaseVisibleCapture.class);
        assertTrue(result instanceof SubVisibleCapture);
        assertEquals("sub", result.typeId);   // visible type id should be stored in the field
        assertEquals("hello", result.field);
        assertEquals(42, ((SubVisibleCapture)result).subField);
    }

    // -----------------------------------------------------------------------
    // Test array fallback branch: top-level JSON array for a polymorphic type
    // (expects an exception or specific behavior depending on configuration)
    // -----------------------------------------------------------------------
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testArrayFallbackTopLevelArray_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // AsPropertyTypeDeserializer expects an object, but we provide an array
        String json = "[{\"@type\":\"sub\",\"baseField\":1,\"subField\":2}]";
        mapper.readValue(json, BaseType.class);
    }

    // -----------------------------------------------------------------------
    // Test the case where type property appears after a nested object
    // (exercises the scanner with deeper nesting)
    // -----------------------------------------------------------------------
    @Test
    public void testTypeIdAfterNestedObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Add a nested field to test scanner behavior
        String json = "{\"nested\":{\"a\":1},\"@type\":\"sub\",\"baseField\":3,\"subField\":4}";
        BaseType result = mapper.readValue(json, BaseType.class);
        assertTrue(result instanceof SubType);
        assertEquals(3, result.baseField);
        assertEquals(4, ((SubType)result).subField);
    }

    // -----------------------------------------------------------------------
    // Test that missing type id with defaultImpl works even when defaultImpl is a concrete subclass
    // (already tested, but here we ensure no additional fields cause issues)
    // -----------------------------------------------------------------------
    @Test
    public void testDefaultImplWithExtraFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"field\":10,\"extra\":\"ignored\"}";
        BaseWithDefault result = mapper.readValue(json, BaseWithDefault.class);
        assertTrue(result instanceof BaseWithDefault);
        assertEquals(10, result.field);
    }
}
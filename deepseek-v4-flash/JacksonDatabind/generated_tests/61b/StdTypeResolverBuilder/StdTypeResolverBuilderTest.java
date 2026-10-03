package com.fasterxml.jackson.databind.jsontype.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.TypeFactory;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class StdTypeResolverBuilderTest {

    private StdTypeResolverBuilder builder;
    private SerializationConfig serConfig;
    private DeserializationConfig deserConfig;
    private JavaType stringType;
    private JavaType intType;
    private Collection<NamedType> emptySubtypes;

    @Before
    public void setUp() {
        builder = new StdTypeResolverBuilder();
        ObjectMapper mapper = new ObjectMapper();
        serConfig = mapper.getSerializationConfig();
        deserConfig = mapper.getDeserializationConfig();
        TypeFactory tf = TypeFactory.defaultInstance();
        stringType = tf.constructType(String.class);
        intType = tf.constructType(int.class);
        emptySubtypes = new ArrayList<>();
        // Initialize builder with a valid idType by default for most tests
        builder.init(JsonTypeInfo.Id.CLASS, null);
    }

    // Test init with null idType throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInit_nullIdType_throwsIllegalArgumentException() {
        builder.init(null, null);
    }

    // Test init sets fields correctly
    @Test
    public void testInit_validIdType_setsFields() {
        StdTypeResolverBuilder b = new StdTypeResolverBuilder();
        b.init(JsonTypeInfo.Id.NAME, null);
        assertEquals(JsonTypeInfo.Id.NAME, b._idType);
        assertEquals(JsonTypeInfo.Id.NAME.getDefaultPropertyName(), b._typeProperty);
    }

    // Test inclusion with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInclusion_nullIncludeAs_throwsIllegalArgumentException() {
        builder.inclusion(null);
    }

    // Test inclusion sets field
    @Test
    public void testInclusion_validIncludeAs_setsField() {
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        assertEquals(JsonTypeInfo.As.PROPERTY, builder._includeAs);
    }

    // Test typeProperty with null or empty restores default
    @Test
    public void testTypeProperty_nullAndEmpty_restoresDefault() {
        String defaultProp = builder._idType.getDefaultPropertyName();
        // null
        builder.typeProperty(null);
        assertEquals(defaultProp, builder._typeProperty);
        // empty
        builder.typeProperty("");
        assertEquals(defaultProp, builder._typeProperty);
    }

    // Test typeProperty with non-empty string
    @Test
    public void testTypeProperty_nonEmpty_setsProperty() {
        builder.typeProperty("customProp");
        assertEquals("customProp", builder._typeProperty);
    }

    // Test defaultImpl, typeIdVisibility, and getters
    @Test
    public void testDefaultImplAndVisibilityAndGetters() {
        builder.defaultImpl(Integer.class);
        assertEquals(Integer.class, builder.getDefaultImpl());
        builder.typeIdVisibility(true);
        assertTrue(builder.isTypeIdVisible());
        builder.typeIdVisibility(false);
        assertFalse(builder.isTypeIdVisible());
        // getTypeProperty after setting
        builder.typeProperty("myProp");
        assertEquals("myProp", builder.getTypeProperty());
    }

    // BuildTypeSerializer: idType NONE -> null
    @Test
    public void testBuildTypeSerializer_idTypeNone_returnsNull() {
        builder.init(JsonTypeInfo.Id.NONE, null);
        TypeSerializer ts = builder.buildTypeSerializer(serConfig, stringType, emptySubtypes);
        assertNull(ts);
    }

    // Bug detection: primitive baseType should return null (see [databind#1395])
    @Test
    public void testBuildTypeSerializer_primitiveBaseType_returnsNull() {
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeSerializer ts = builder.buildTypeSerializer(serConfig, intType, emptySubtypes);
        assertNull("Expected null for primitive type, but got non-null (bug).", ts);
    }

    // BuildTypeSerializer: all inclusion types return correct class
    @Test
    public void testBuildTypeSerializer_eachIncludeCase_returnsCorrectType() {
        // WRAPPER_ARRAY
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        assertTrue(builder.buildTypeSerializer(serConfig, stringType, emptySubtypes) instanceof AsArrayTypeSerializer);

        // PROPERTY
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        assertTrue(builder.buildTypeSerializer(serConfig, stringType, emptySubtypes) instanceof AsPropertyTypeSerializer);

        // WRAPPER_OBJECT
        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        assertTrue(builder.buildTypeSerializer(serConfig, stringType, emptySubtypes) instanceof AsWrapperTypeSerializer);

        // EXTERNAL_PROPERTY
        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        assertTrue(builder.buildTypeSerializer(serConfig, stringType, emptySubtypes) instanceof AsExternalTypeSerializer);

        // EXISTING_PROPERTY
        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        assertTrue(builder.buildTypeSerializer(serConfig, stringType, emptySubtypes) instanceof AsExistingPropertyTypeSerializer);
    }

    // BuildTypeDeserializer: idType NONE -> null
    @Test
    public void testBuildTypeDeserializer_idTypeNone_returnsNull() {
        builder.init(JsonTypeInfo.Id.NONE, null);
        TypeDeserializer td = builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes);
        assertNull(td);
    }

    // Bug detection: primitive baseType should return null for deserializer as well
    @Test
    public void testBuildTypeDeserializer_primitiveBaseType_returnsNull() {
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeDeserializer td = builder.buildTypeDeserializer(deserConfig, intType, emptySubtypes);
        assertNull("Expected null for primitive type, but got non-null (bug).", td);
    }

    // BuildTypeDeserializer: defaultImpl Void.class and NoClass.class use constructType
    @Test
    public void testBuildTypeDeserializer_defaultImplVoidAndNoClass_usesConstructType() {
        builder.inclusion(JsonTypeInfo.As.PROPERTY); // any inclusion works

        // Void default
        builder.defaultImpl(Void.class);
        TypeDeserializer td1 = builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes);
        assertNotNull(td1);

        // NoClass default
        builder.defaultImpl(NoClass.class);
        TypeDeserializer td2 = builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes);
        assertNotNull(td2);
    }

    // BuildTypeDeserializer: normal defaultImpl uses constructSpecializedType
    @Test
    public void testBuildTypeDeserializer_defaultImplNormal_usesConstructSpecializedType() {
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(StringBuilder.class);
        TypeDeserializer td = builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes);
        assertNotNull(td);
    }

    // BuildTypeDeserializer: all inclusion types return correct class
    @Test
    public void testBuildTypeDeserializer_eachIncludeCase_returnsCorrectType() {
        // WRAPPER_ARRAY
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        assertTrue(builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes) instanceof AsArrayTypeDeserializer);

        // PROPERTY
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        assertTrue(builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes) instanceof AsPropertyTypeDeserializer);

        // WRAPPER_OBJECT
        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        assertTrue(builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes) instanceof AsWrapperTypeDeserializer);

        // EXTERNAL_PROPERTY
        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        assertTrue(builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes) instanceof AsExternalTypeDeserializer);

        // EXISTING_PROPERTY also returns AsPropertyTypeDeserializer
        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        assertTrue(builder.buildTypeDeserializer(deserConfig, stringType, emptySubtypes) instanceof AsPropertyTypeDeserializer);
    }

    // Build method without init throws IllegalStateException through idResolver
    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutInit_throwsIllegalStateException() {
        StdTypeResolverBuilder b = new StdTypeResolverBuilder();
        b.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        b.buildTypeSerializer(serConfig, stringType, emptySubtypes);
    }

    // idResolver with CUSTOM and custom resolver uses that resolver
    @Test
    public void testBuildWithCustomResolver_UsesCustom() {
        TypeIdResolver custom = new TypeIdResolver() {
            @Override public void init(JavaType bt) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(String id) { return null; }
            @Override public String getMechanism() { return null; }
        };
        builder.init(JsonTypeInfo.Id.CUSTOM, custom);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeSerializer ts = builder.buildTypeSerializer(serConfig, stringType, emptySubtypes);
        assertNotNull(ts);
    }

    // idResolver with CUSTOM but no custom resolver throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testBuildWithCustomNoResolver_throwsIllegalStateException() {
        StdTypeResolverBuilder b = new StdTypeResolverBuilder();
        b.init(JsonTypeInfo.Id.CUSTOM, null);
        b.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        b.buildTypeSerializer(serConfig, stringType, emptySubtypes);
    }
}
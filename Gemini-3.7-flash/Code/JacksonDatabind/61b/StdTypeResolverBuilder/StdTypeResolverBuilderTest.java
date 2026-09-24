package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdTypeResolverBuilderTest {

    private ObjectMapper mapper;
    private SerializationConfig serConfig;
    private DeserializationConfig deserConfig;
    private JavaType objectType;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        serConfig = mapper.getSerializationConfig();
        deserConfig = mapper.getDeserializationConfig();
        objectType = mapper.constructType(Object.class);
    }

    // Tests creation of no-op type info builder
    @Test
    public void testNoTypeInfoBuilder_returnsNullSerializers() {
        StdTypeResolverBuilder builder = StdTypeResolverBuilder.noTypeInfoBuilder();
        TypeSerializer ser = builder.buildTypeSerializer(serConfig, objectType, null);
        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, null);

        assertNull(ser);
        assertNull(deser);
    }

    // Tests exception path when init() receives null idType
    @Test(expected = IllegalArgumentException.class)
    public void testInit_nullIdType_throwsIllegalArgumentException() {
        new StdTypeResolverBuilder().init(null, null);
    }

    // Tests exception path when inclusion() receives null includeAs
    @Test(expected = IllegalArgumentException.class)
    public void testInclusion_nullIncludeAs_throwsIllegalArgumentException() {
        new StdTypeResolverBuilder().inclusion(null);
    }

    // Tests setting and getting type property with default fallbacks
    @Test
    public void testTypeProperty_nullOrEmpty_resetsToDefault() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());

        builder.typeProperty("customProp");
        assertEquals("customProp", builder.getTypeProperty());

        builder.typeProperty(null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());

        builder.typeProperty("");
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());
    }

    // Tests typeIdVisibility flag setter and getter
    @Test
    public void testTypeIdVisibility_setsCorrectFlag() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        assertFalse(builder.isTypeIdVisible());

        builder.typeIdVisibility(true);
        assertTrue(builder.isTypeIdVisible());

        builder.typeIdVisibility(false);
        assertFalse(builder.isTypeIdVisible());
    }

    // Tests buildTypeSerializer across all JsonTypeInfo.As inclusion mechanisms
    @Test
    public void testBuildTypeSerializer_allInclusionTypes() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeSerializer ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertTrue(ser instanceof AsArrayTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertTrue(ser instanceof AsPropertyTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertTrue(ser instanceof AsWrapperTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertTrue(ser instanceof AsExternalTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertTrue(ser instanceof AsExistingPropertyTypeSerializer);
    }

    // Tests buildTypeDeserializer across all JsonTypeInfo.As inclusion mechanisms
    @Test
    public void testBuildTypeDeserializer_allInclusionTypes() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertTrue(deser instanceof AsArrayTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertTrue(deser instanceof AsWrapperTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertTrue(deser instanceof AsExternalTypeDeserializer);
    }

    // Tests defaultImpl resolution with Void.class and NoClass.class
    @Test
    public void testBuildTypeDeserializer_defaultImpl_markerClasses() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY);

        builder.defaultImpl(Void.class);
        assertEquals(Void.class, builder.getDefaultImpl());
        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertEquals(Void.class, deser.getDefaultImpl());

        builder.defaultImpl(NoClass.class);
        assertEquals(NoClass.class, builder.getDefaultImpl());
        deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertEquals(NoClass.class, deser.getDefaultImpl());
    }

    // Tests defaultImpl resolution with specialized class type
    @Test
    public void testBuildTypeDeserializer_defaultImpl_specializedClass() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(ArrayList.class);

        JavaType listType = mapper.constructType(List.class);
        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, listType, null);

        assertEquals(ArrayList.class, deser.getDefaultImpl());
    }

    // Tests build with custom TypeIdResolver
    @Test
    public void testBuildTypeSerializerAndDeserializer_customIdResolver() {
        TypeIdResolver customResolver = new ClassNameIdResolver(objectType, mapper.getTypeFactory());
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, customResolver).inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);

        TypeSerializer ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertNotNull(ser);

        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertNotNull(deser);
    }

    // Tests TypeNameIdResolver construction with Id.NAME
    @Test
    public void testBuildTypeSerializer_idTypeName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null).inclusion(JsonTypeInfo.As.PROPERTY);

        List<NamedType> subtypes = Collections.singletonList(new NamedType(String.class, "string"));
        TypeSerializer ser = builder.buildTypeSerializer(serConfig, objectType, subtypes);
        assertNotNull(ser);

        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, subtypes);
        assertNotNull(deser);
    }

    // Tests MinimalClassNameIdResolver construction with Id.MINIMAL_CLASS
    @Test
    public void testBuildTypeSerializer_idTypeMinimalClass() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.MINIMAL_CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY);

        TypeSerializer ser = builder.buildTypeSerializer(serConfig, objectType, null);
        assertNotNull(ser);

        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, objectType, null);
        assertNotNull(deser);
    }

    // Tests exception path when idResolver is called without calling init()
    @Test(expected = IllegalStateException.class)
    public void testBuildTypeSerializer_uninitialized_throwsIllegalStateException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.buildTypeSerializer(serConfig, objectType, null);
    }

    // Tests exception path when inclusion is not configured
    @Test(expected = NullPointerException.class)
    public void testBuildTypeSerializer_nullInclusion_throwsException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeSerializer(serConfig, objectType, null);
    }

    // Tests behavior when Id.NONE is explicitly initialized
    @Test
    public void testBuildTypeSerializerAndDeserializer_idTypeNone() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null).inclusion(JsonTypeInfo.As.PROPERTY);

        assertNull(builder.buildTypeSerializer(serConfig, objectType, null));
        assertNull(builder.buildTypeDeserializer(deserConfig, objectType, null));
    }

    // Tests exception when Id.CUSTOM is specified without a custom TypeIdResolver
    @Test(expected = IllegalStateException.class)
    public void testBuildTypeSerializer_customIdWithoutResolver_throwsIllegalStateException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, null).inclusion(JsonTypeInfo.As.PROPERTY);
        builder.buildTypeSerializer(serConfig, objectType, null);
    }

    // Tests defaultImpl when the default implementation class is not a subtype of baseType
    @Test
    public void testBuildTypeDeserializer_defaultImpl_nonSubtype() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(Integer.class);

        JavaType stringType = mapper.constructType(String.class);
        TypeDeserializer deser = builder.buildTypeDeserializer(deserConfig, stringType, null);
        assertEquals(Integer.class, deser.getDefaultImpl());
    }

    // Tests defaultImpl resolution when USE_BASE_TYPE_AS_DEFAULT_IMPL is enabled for non-abstract base type
    @Test
    public void testBuildTypeDeserializer_useBaseTypeAsDefaultImpl() {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);
        DeserializationConfig config = customMapper.getDeserializationConfig();

        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(null);

        JavaType concreteType = customMapper.constructType(String.class);
        TypeDeserializer deser = builder.buildTypeDeserializer(config, concreteType, null);
        assertEquals(String.class, deser.getDefaultImpl());

        JavaType abstractType = customMapper.constructType(Number.class);
        TypeDeserializer deserAbstract = builder.buildTypeDeserializer(config, abstractType, null);
        assertNull(deserAbstract.getDefaultImpl());
    }

    // Tests getDefaultImpl getter when not set
    @Test
    public void testGetDefaultImpl_initiallyNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        assertNull(builder.getDefaultImpl());
    }
}
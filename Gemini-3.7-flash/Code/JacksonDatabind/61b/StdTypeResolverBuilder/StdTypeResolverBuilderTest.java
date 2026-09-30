package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdTypeResolverBuilderTest {

    private ObjectMapper _mapper;
    private SerializationConfig _serConfig;
    private DeserializationConfig _deserConfig;
    private JavaType _baseType;
    private Collection<NamedType> _subtypes;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _serConfig = _mapper.getSerializationConfig();
        _deserConfig = _mapper.getDeserializationConfig();
        _baseType = TypeFactory.defaultInstance().constructType(Number.class);
        _subtypes = new ArrayList<NamedType>();
        _subtypes.add(new NamedType(Integer.class, "int"));
        _subtypes.add(new NamedType(Long.class, "long"));
    }

    // Tests initialization with null idType throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInit_nullIdType_throwsIllegalArgumentException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(null, null);
    }

    // Tests noTypeInfoBuilder factory method
    @Test
    public void testNoTypeInfoBuilder_defaultState_returnsNullSerializers() {
        StdTypeResolverBuilder builder = StdTypeResolverBuilder.noTypeInfoBuilder();
        assertNull(builder.buildTypeSerializer(_serConfig, _baseType, _subtypes));
        assertNull(builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes));
    }

    // Tests inclusion setter with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInclusion_nullInclusion_throwsIllegalArgumentException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(null);
    }

    // Tests typeProperty with custom value, null, and empty string
    @Test
    public void testTypeProperty_customAndDefaultValues_setsCorrectProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());

        builder.typeProperty("myType");
        assertEquals("myType", builder.getTypeProperty());

        builder.typeProperty(null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());

        builder.typeProperty("");
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());
    }

    // Tests setters and accessors for defaultImpl and typeIdVisible
    @Test
    public void testSettersAndGetters_validInputs_setsValuesProperly() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        assertNull(builder.getDefaultImpl());
        assertFalse(builder.isTypeIdVisible());

        builder.defaultImpl(Integer.class);
        builder.typeIdVisibility(true);

        assertEquals(Integer.class, builder.getDefaultImpl());
        assertTrue(builder.isTypeIdVisible());
    }

    // Tests buildTypeSerializer with Id.NONE returns null
    @Test
    public void testBuildTypeSerializer_idTypeNone_returnsNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeSerializer serializer = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertNull(serializer);
    }

    // Tests buildTypeDeserializer with Id.NONE returns null
    @Test
    public void testBuildTypeDeserializer_idTypeNone_returnsNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeDeserializer deserializer = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNull(deserializer);
    }

    // Tests buildTypeSerializer across all As inclusions
    @Test
    public void testBuildTypeSerializer_allInclusions_constructsProperSerializers() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeSerializer ser = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertTrue(ser instanceof AsArrayTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        ser = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertTrue(ser instanceof AsPropertyTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        ser = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertTrue(ser instanceof AsWrapperTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        ser = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertTrue(ser instanceof AsExternalTypeSerializer);

        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        ser = builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
        assertTrue(ser instanceof AsExistingPropertyTypeSerializer);
    }

    // Tests buildTypeDeserializer across all As inclusions
    @Test
    public void testBuildTypeDeserializer_allInclusions_constructsProperDeserializers() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        TypeDeserializer deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertTrue(deser instanceof AsArrayTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertTrue(deser instanceof AsWrapperTypeDeserializer);

        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertTrue(deser instanceof AsExternalTypeDeserializer);
    }

    // Tests buildTypeDeserializer with special defaultImpl classes (Void, NoClass, Subclass)
    @Test
    public void testBuildTypeDeserializer_specialDefaultImpl_constructsExpectedType() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        builder.defaultImpl(Void.class);
        TypeDeserializer deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(Void.class, deser.getDefaultImpl());

        builder.defaultImpl(NoClass.class);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(NoClass.class, deser.getDefaultImpl());

        builder.defaultImpl(Integer.class);
        deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(Integer.class, deser.getDefaultImpl());
    }

    // Tests idResolver creation with different Id types: MINIMAL_CLASS, NAME, and custom resolver
    @Test
    public void testIdResolver_differentIdTypes_createsCorrectResolvers() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        builder.init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
        TypeIdResolver res = builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
        assertTrue(res instanceof MinimalClassNameIdResolver);

        builder.init(JsonTypeInfo.Id.NAME, null);
        res = builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
        assertTrue(res instanceof TypeNameIdResolver);

        TypeIdResolver custom = new ClassNameIdResolver(_baseType, _serConfig.getTypeFactory());
        builder.init(JsonTypeInfo.Id.CUSTOM, custom);
        res = builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
        assertSame(custom, res);
    }

    // Tests idResolver throws exception when init was not called
    @Test(expected = IllegalStateException.class)
    public void testIdResolver_uninitialized_throwsIllegalStateException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
    }

    // Tests idResolver throws exception for Id.CUSTOM when custom resolver is null
    @Test(expected = IllegalStateException.class)
    public void testIdResolver_customWithoutResolver_throwsIllegalStateException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, null);
        builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
    }

    // Tests exception path when inclusion is null on buildTypeSerializer
    @Test(expected = NullPointerException.class)
    public void testBuildTypeSerializer_nullInclusion_throwsException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeSerializer(_serConfig, _baseType, _subtypes);
    }

    // Tests exception path when inclusion is null on buildTypeDeserializer
    @Test(expected = NullPointerException.class)
    public void testBuildTypeDeserializer_nullInclusion_throwsException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
    }

    // Tests defineDefaultImpl using DeserializationFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL
    @Test
    public void testBuildTypeDeserializer_useBaseTypeAsDefaultImpl_usesBaseType() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        DeserializationConfig configWithBaseType = _deserConfig.with(DeserializationFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);
        TypeDeserializer deser = builder.buildTypeDeserializer(configWithBaseType, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(Number.class, deser.getDefaultImpl());

        DeserializationConfig configWithoutBaseType = _deserConfig.without(DeserializationFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);
        deser = builder.buildTypeDeserializer(configWithoutBaseType, _baseType, _subtypes);
        assertNotNull(deser);
        assertNull(deser.getDefaultImpl());
    }

    // Tests defineDefaultImpl when defaultImpl is the exact same class as baseType
    @Test
    public void testBuildTypeDeserializer_defaultImplSameAsBaseType() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(Number.class);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(Number.class, deser.getDefaultImpl());
    }

    // Tests defineDefaultImpl when defaultImpl is not a subtype of baseType
    @Test
    public void testBuildTypeDeserializer_defaultImplUnrelatedType() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(String.class);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserConfig, _baseType, _subtypes);
        assertNotNull(deser);
        assertEquals(String.class, deser.getDefaultImpl());
    }

    // Tests idResolver for Id.NONE returns null
    @Test
    public void testIdResolver_idNone_returnsNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        TypeIdResolver res = builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
        assertNull(res);
    }

    // Tests idResolver for Id.CLASS creates ClassNameIdResolver
    @Test
    public void testIdResolver_idClass_createsClassNameIdResolver() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        TypeIdResolver res = builder.idResolver(_serConfig, _baseType, _subtypes, true, false);
        assertTrue(res instanceof ClassNameIdResolver);
    }

    // Tests idResolver for deserialization mode
    @Test
    public void testIdResolver_forDeserializationMode() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        TypeIdResolver res = builder.idResolver(_deserConfig, _baseType, _subtypes, false, true);
        assertTrue(res instanceof TypeNameIdResolver);
    }
}
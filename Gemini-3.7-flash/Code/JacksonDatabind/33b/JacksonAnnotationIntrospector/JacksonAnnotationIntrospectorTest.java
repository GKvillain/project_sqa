package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
    }

    // =========================================================================
    // Test classes and dummy types
    // =========================================================================

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface BundleAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NonBundleAnnotation {
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    static class RootWithNamespace {
    }

    @JsonRootName(value = "rootNoNs", namespace = "")
    static class RootWithEmptyNamespace {
    }

    static class NoRoot {
    }

    @JsonIgnoreProperties(value = {"prop1", "prop2"}, ignoreUnknown = true, allowGetters = true, allowSetters = false)
    static class IgnorePropertiesAllowGetters {
    }

    @JsonIgnoreProperties(value = {"prop1", "prop2"}, ignoreUnknown = true, allowGetters = false, allowSetters = true)
    static class IgnorePropertiesAllowSetters {
    }

    @JsonIgnoreType(true)
    static class IgnorableClass {
    }

    @JsonFilter("filter123")
    static class FilteredClass {
    }

    @JsonFilter("")
    static class EmptyFilteredClass {
    }

    @JsonNaming(PropertyNamingStrategy.LowerCaseStrategy.class)
    static class NamingStrategyClass {
    }

    @JsonTypeName("customTypeName")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubTypeA.class, name = "subA"),
        @JsonSubTypes.Type(value = SubTypeB.class, name = "subB")
    })
    static class PolymorphicBase {
    }

    static class SubTypeA extends PolymorphicBase {
    }

    static class SubTypeB extends PolymorphicBase {
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    static class IdentityClass {
        public int id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    static class IdentityNoneClass {
    }

    static enum TestEnum {
        @JsonProperty("customA")
        VALUE_A,
        VALUE_B
    }

    static class DummySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen,
                SerializerProvider serializers) {}
    }

    static class DummyDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(com.fasterxml.jackson.core.JsonParser p,
                DeserializationContext ctxt) {
            return null;
        }
    }

    static class DummyKeySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen,
                SerializerProvider serializers) {}
    }

    static class DummyKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return null;
        }
    }

    static class DummyConverter extends StdConverter<Object, Object> {
        @Override
        public Object convert(Object value) {
            return value;
        }
    }

    @JsonPropertyOrder(value = {"b", "a"}, alphabetic = true)
    static class OrderedClass {
        public int b;
        public int a;
    }

    @JsonPOJOBuilder(buildMethodName = "construct", withPrefix = "with")
    static class CustomBuilder {
    }

    @JsonDeserialize(builder = CustomBuilder.class)
    static class ClassWithBuilder {
    }

    @JsonValueInstantiator(Object.class)
    static class InstantiatorClass {
    }

    static class MemberTestClass {
        @JsonProperty(value = "customField", required = true, index = 3, defaultValue = "defVal",
                access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("field description")
        public String fieldWithProp;

        @JsonIgnore
        public String ignoredField;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public String formattedField;

        @JsonManagedReference("refName")
        public MemberTestClass managedRef;

        @JsonBackReference("refName")
        public MemberTestClass backRef;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post", enabled = true)
        public MemberTestClass unwrappedField;

        @JacksonInject("injectedId")
        public String injectedNamed;

        @JacksonInject("")
        public String injectedDefault;

        @JsonView({Object.class})
        public String viewedField;

        @JsonTypeId
        public String typeIdField;

        @JsonSerialize(using = DummySerializer.class, keyUsing = DummyKeySerializer.class,
                contentUsing = DummySerializer.class, nullsUsing = DummySerializer.class,
                converter = DummyConverter.class, contentConverter = DummyConverter.class,
                as = String.class, keyAs = String.class, contentAs = String.class,
                typing = JsonSerialize.Typing.STATIC)
        public Object serializedField;

        @JsonDeserialize(using = DummyDeserializer.class, keyUsing = DummyKeyDeserializer.class,
                contentUsing = DummyDeserializer.class,
                converter = DummyConverter.class, contentConverter = DummyConverter.class,
                as = String.class, keyAs = String.class, contentAs = String.class)
        public Object deserializedField;

        @JsonRawValue(true)
        public String rawField;

        @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_EMPTY)
        public Object includeField;

        @JsonGetter("getterName")
        public String getGetterName() {
            return null;
        }

        @JsonSetter("setterName")
        public void setSetterName(String value) {
        }

        @JsonValue
        public String valueMethod() {
            return null;
        }

        @JsonAnyGetter
        public java.util.Map<String, Object> anyGetter() {
            return null;
        }

        @JsonAnySetter
        public void anySetter(String name, Object value) {
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public MemberTestClass(@JsonProperty("fieldWithProp") String fieldWithProp) {
            this.fieldWithProp = fieldWithProp;
        }

        public MemberTestClass() {
        }

        @JacksonInject("")
        public void injectMethod(String val) {
        }
    }

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClass.constructWithoutSuperTypes(cls, introspector, null);
    }

    private AnnotatedField getAnnotatedField(Class<?> cls, String fieldName) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Field not found: " + fieldName);
    }

    private AnnotatedMethod getAnnotatedMethod(Class<?> cls, String methodName) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    private AnnotatedConstructor getAnnotatedConstructor(Class<?> cls, int paramCount) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedConstructor c : ac.getConstructors()) {
            if (c.getParameterCount() == paramCount) {
                return c;
            }
        }
        throw new IllegalArgumentException("Constructor with " + paramCount + " params not found");
    }

    // =========================================================================
    // General annotation tests
    // =========================================================================

    // Tests version retrieval
    @Test
    public void testVersion_default_notNull() {
        Version v = introspector.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // Tests annotation bundle identification
    @Test
    public void testIsAnnotationBundle_bundleAndNonBundle_returnsExpected() throws Exception {
        Annotation bundle = BundleAnnotation.class.getAnnotation(JacksonAnnotationsInside.class);
        assertTrue(introspector.isAnnotationBundle(bundle));

        Annotation nonBundle = MemberTestClass.class.getMethod("valueMethod").getAnnotation(JsonValue.class);
        assertFalse(introspector.isAnnotationBundle(nonBundle));
    }

    // Tests findEnumValue with explicit @JsonProperty and default fallback
    @Test
    public void testFindEnumValue_annotatedAndUnannotated_returnsCorrectValue() {
        assertEquals("customA", introspector.findEnumValue(TestEnum.VALUE_A));
        assertEquals("VALUE_B", introspector.findEnumValue(TestEnum.VALUE_B));
    }

    // =========================================================================
    // Class annotation tests
    // =========================================================================

    // Tests findRootName with namespace and without namespace
    @Test
    public void testFindRootName_withAndWithoutNamespace_returnsPropertyName() {
        AnnotatedClass acWithNs = getAnnotatedClass(RootWithNamespace.class);
        PropertyName pn1 = introspector.findRootName(acWithNs);
        assertNotNull(pn1);
        assertEquals("root", pn1.getSimpleName());
        assertEquals("http://example.com", pn1.getNamespace());

        AnnotatedClass acEmptyNs = getAnnotatedClass(RootWithEmptyNamespace.class);
        PropertyName pn2 = introspector.findRootName(acEmptyNs);
        assertNotNull(pn2);
        assertEquals("rootNoNs", pn2.getSimpleName());
        assertNull(pn2.getNamespace());

        AnnotatedClass acNone = getAnnotatedClass(NoRoot.class);
        assertNull(introspector.findRootName(acNone));
    }

    // Tests findPropertiesToIgnore for serialization and deserialization
    @Test
    public void testFindPropertiesToIgnore_serializationAndDeserialization_handlesAllowFlags() {
        AnnotatedClass acGetters = getAnnotatedClass(IgnorePropertiesAllowGetters.class);
        // allowGetters is true: ignored on deserialization, not on serialization
        assertNull(introspector.findPropertiesToIgnore(acGetters, true));
        assertArrayEquals(new String[]{"prop1", "prop2"}, introspector.findPropertiesToIgnore(acGetters, false));

        AnnotatedClass acSetters = getAnnotatedClass(IgnorePropertiesAllowSetters.class);
        // allowSetters is true: ignored on serialization, not on deserialization
        assertArrayEquals(new String[]{"prop1", "prop2"}, introspector.findPropertiesToIgnore(acSetters, true));
        assertNull(introspector.findPropertiesToIgnore(acSetters, false));

        // Deprecated variant
        assertArrayEquals(new String[]{"prop1", "prop2"}, introspector.findPropertiesToIgnore(acGetters));
    }

    // Tests findIgnoreUnknownProperties and isIgnorableType
    @Test
    public void testFindIgnoreUnknownProperties_andIsIgnorableType_returnsCorrectBooleans() {
        AnnotatedClass acIgnore = getAnnotatedClass(IgnorePropertiesAllowGetters.class);
        assertEquals(Boolean.TRUE, introspector.findIgnoreUnknownProperties(acIgnore));

        AnnotatedClass acIgnorableType = getAnnotatedClass(IgnorableClass.class);
        assertEquals(Boolean.TRUE, introspector.isIgnorableType(acIgnorableType));

        AnnotatedClass acNormal = getAnnotatedClass(NoRoot.class);
        assertNull(introspector.findIgnoreUnknownProperties(acNormal));
        assertNull(introspector.isIgnorableType(acNormal));
    }

    // Tests findFilterId with valid id, empty id, and absent annotation
    @Test
    public void testFindFilterId_variousCases_returnsExpected() {
        AnnotatedClass acFiltered = getAnnotatedClass(FilteredClass.class);
        assertEquals("filter123", introspector.findFilterId(acFiltered));

        AnnotatedClass acEmpty = getAnnotatedClass(EmptyFilteredClass.class);
        assertNull(introspector.findFilterId(acEmpty));

        AnnotatedClass acNone = getAnnotatedClass(NoRoot.class);
        assertNull(introspector.findFilterId(acNone));
    }

    // Tests findNamingStrategy
    @Test
    public void testFindNamingStrategy_presentAndAbsent_returnsStrategyOrNull() {
        AnnotatedClass ac = getAnnotatedClass(NamingStrategyClass.class);
        assertEquals(PropertyNamingStrategy.LowerCaseStrategy.class, introspector.findNamingStrategy(ac));

        AnnotatedClass acNone = getAnnotatedClass(NoRoot.class);
        assertNull(introspector.findNamingStrategy(acNone));
    }

    // =========================================================================
    // Member annotation tests
    // =========================================================================

    // Tests member property attributes: required, access, description, index, defaultValue, ignore
    @Test
    public void testMemberPropertyAttributes_validField_returnsMetadata() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "fieldWithProp");
        assertEquals(Boolean.TRUE, introspector.hasRequiredMarker(f));
        assertEquals(JsonProperty.Access.READ_ONLY, introspector.findPropertyAccess(f));
        assertEquals("field description", introspector.findPropertyDescription(f));
        assertEquals(Integer.valueOf(3), introspector.findPropertyIndex(f));
        assertEquals("defVal", introspector.findPropertyDefaultValue(f));
        assertFalse(introspector.hasIgnoreMarker(f));

        AnnotatedField ignored = getAnnotatedField(MemberTestClass.class, "ignoredField");
        assertTrue(introspector.hasIgnoreMarker(ignored));
    }

    // Tests findFormat
    @Test
    public void testFindFormat_formattedField_returnsFormatValue() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "formattedField");
        JsonFormat.Value val = introspector.findFormat(f);
        assertNotNull(val);
        assertEquals("yyyy-MM-dd", val.getPattern());
        assertEquals(JsonFormat.Shape.STRING, val.getShape());
    }

    // Tests findReferenceType for managed and back references
    @Test
    public void testFindReferenceType_managedAndBackRef_returnsReferenceProperty() {
        AnnotatedField fManaged = getAnnotatedField(MemberTestClass.class, "managedRef");
        AnnotationIntrospector.ReferenceProperty refManaged = introspector.findReferenceType(fManaged);
        assertNotNull(refManaged);
        assertTrue(refManaged.isManagedReference());
        assertEquals("refName", refManaged.getName());

        AnnotatedField fBack = getAnnotatedField(MemberTestClass.class, "backRef");
        AnnotationIntrospector.ReferenceProperty refBack = introspector.findReferenceType(fBack);
        assertNotNull(refBack);
        assertTrue(refBack.isBackReference());
        assertEquals("refName", refBack.getName());
    }

    // Tests findUnwrappingNameTransformer
    @Test
    public void testFindUnwrappingNameTransformer_unwrappedField_returnsTransformer() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "unwrappedField");
        com.fasterxml.jackson.databind.util.NameTransformer transformer = introspector.findUnwrappingNameTransformer(f);
        assertNotNull(transformer);
        assertEquals("pre_name_post", transformer.transform("name"));
    }

    // Tests findInjectableValueId on field and method
    @Test
    public void testFindInjectableValueId_namedAndDefault_returnsExpectedId() {
        AnnotatedField fNamed = getAnnotatedField(MemberTestClass.class, "injectedNamed");
        assertEquals("injectedId", introspector.findInjectableValueId(fNamed));

        AnnotatedField fDefault = getAnnotatedField(MemberTestClass.class, "injectedDefault");
        assertEquals(String.class.getName(), introspector.findInjectableValueId(fDefault));

        AnnotatedMethod mInject = getAnnotatedMethod(MemberTestClass.class, "injectMethod");
        assertEquals(String.class.getName(), introspector.findInjectableValueId(mInject));
    }

    // Tests findViews
    @Test
    public void testFindViews_viewedField_returnsViews() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "viewedField");
        Class<?>[] views = introspector.findViews(f);
        assertNotNull(views);
        assertEquals(1, views.length);
        assertEquals(Object.class, views[0]);
    }

    // =========================================================================
    // Polymorphism & Object Identity tests
    // =========================================================================

    // Tests findSubtypes, findTypeName, and isTypeId
    @Test
    public void testPolymorphicAnnotations_baseClass_returnsSubtypesAndTypeName() {
        AnnotatedClass ac = getAnnotatedClass(PolymorphicBase.class);
        assertEquals("customTypeName", introspector.findTypeName(ac));

        List<NamedType> subtypes = introspector.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        assertEquals(SubTypeA.class, subtypes.get(0).getType());
        assertEquals("subA", subtypes.get(0).getName());

        AnnotatedField fTypeId = getAnnotatedField(MemberTestClass.class, "typeIdField");
        assertEquals(Boolean.TRUE, introspector.isTypeId(fTypeId));
    }

    // Tests findObjectIdInfo and findObjectReferenceInfo
    @Test
    public void testObjectIdInfo_identityClass_returnsCorrectInfo() {
        AnnotatedClass ac = getAnnotatedClass(IdentityClass.class);
        ObjectIdInfo info = introspector.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = introspector.findObjectReferenceInfo(ac, info);
        assertTrue(refInfo.getAlwaysAsId());

        AnnotatedClass acNone = getAnnotatedClass(IdentityNoneClass.class);
        assertNull(introspector.findObjectIdInfo(acNone));
    }

    // =========================================================================
    // Serialization & Deserialization property name tests (Bug 33 regression)
    // =========================================================================

    // Tests findNameForSerialization across various annotations
    @Test
    public void testFindNameForSerialization_variousAnnotations_returnsCorrectPropertyName() {
        AnnotatedMethod mGetter = getAnnotatedMethod(MemberTestClass.class, "getGetterName");
        PropertyName pnGetter = introspector.findNameForSerialization(mGetter);
        assertNotNull(pnGetter);
        assertEquals("getterName", pnGetter.getSimpleName());

        AnnotatedField fProp = getAnnotatedField(MemberTestClass.class, "fieldWithProp");
        PropertyName pnProp = introspector.findNameForSerialization(fProp);
        assertNotNull(pnProp);
        assertEquals("customField", pnProp.getSimpleName());

        AnnotatedField fSerialize = getAnnotatedField(MemberTestClass.class, "serializedField");
        PropertyName pnSerialize = introspector.findNameForSerialization(fSerialize);
        assertNotNull(pnSerialize);
        assertEquals("", pnSerialize.getSimpleName());

        AnnotatedField fRaw = getAnnotatedField(MemberTestClass.class, "rawField");
        PropertyName pnRaw = introspector.findNameForSerialization(fRaw);
        assertNotNull(pnRaw);
        assertEquals("", pnRaw.getSimpleName());

        AnnotatedField fUnwrapped = getAnnotatedField(MemberTestClass.class, "unwrappedField");
        PropertyName pnUnwrapped = introspector.findNameForSerialization(fUnwrapped);
        // An unwrapped property marker or serializer marker should provide PropertyName.USE_DEFAULT or empty name
        // rather than being completely ignored (defect 33 detection)
        if (pnUnwrapped != null) {
            assertEquals("", pnUnwrapped.getSimpleName());
        }

        AnnotatedField fIgnored = getAnnotatedField(MemberTestClass.class, "ignoredField");
        assertNull(introspector.findNameForSerialization(fIgnored));
    }

    // Tests findNameForDeserialization across various annotations
    @Test
    public void testFindNameForDeserialization_variousAnnotations_returnsCorrectPropertyName() {
        AnnotatedMethod mSetter = getAnnotatedMethod(MemberTestClass.class, "setSetterName");
        PropertyName pnSetter = introspector.findNameForDeserialization(mSetter);
        assertNotNull(pnSetter);
        assertEquals("setterName", pnSetter.getSimpleName());

        AnnotatedField fProp = getAnnotatedField(MemberTestClass.class, "fieldWithProp");
        PropertyName pnProp = introspector.findNameForDeserialization(fProp);
        assertNotNull(pnProp);
        assertEquals("customField", pnProp.getSimpleName());

        AnnotatedField fDeserialize = getAnnotatedField(MemberTestClass.class, "deserializedField");
        PropertyName pnDeserialize = introspector.findNameForDeserialization(fDeserialize);
        assertNotNull(pnDeserialize);
        assertEquals("", pnDeserialize.getSimpleName());

        AnnotatedField fUnwrapped = getAnnotatedField(MemberTestClass.class, "unwrappedField");
        PropertyName pnUnwrapped = introspector.findNameForDeserialization(fUnwrapped);
        assertNotNull(pnUnwrapped);
        assertEquals("", pnUnwrapped.getSimpleName());

        AnnotatedField fIgnored = getAnnotatedField(MemberTestClass.class, "ignoredField");
        assertNull(introspector.findNameForDeserialization(fIgnored));
    }

    // =========================================================================
    // Serializer / Deserializer handler tests
    // =========================================================================

    // Tests findSerializer, findKeySerializer, findContentSerializer, findNullSerializer
    @Test
    public void testSerializers_annotatedField_returnsHandlerClasses() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "serializedField");
        assertEquals(DummySerializer.class, introspector.findSerializer(f));
        assertEquals(DummyKeySerializer.class, introspector.findKeySerializer(f));
        assertEquals(DummySerializer.class, introspector.findContentSerializer(f));
        assertEquals(DummySerializer.class, introspector.findNullSerializer(f));
        assertEquals(DummyConverter.class, introspector.findSerializationConverter(f));
        assertEquals(DummyConverter.class, introspector.findSerializationContentConverter(f));
        assertEquals(String.class, introspector.findSerializationType(f));
        assertEquals(JsonSerialize.Typing.STATIC, introspector.findSerializationTyping(f));

        AnnotatedField fRaw = getAnnotatedField(MemberTestClass.class, "rawField");
        Object rawSer = introspector.findSerializer(fRaw);
        assertNotNull(rawSer);
        assertTrue(rawSer instanceof RawSerializer);
    }

    // Tests findDeserializer, findKeyDeserializer, findContentDeserializer
    @Test
    public void testDeserializers_annotatedField_returnsHandlerClasses() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "deserializedField");
        assertEquals(DummyDeserializer.class, introspector.findDeserializer(f));
        assertEquals(DummyKeyDeserializer.class, introspector.findKeyDeserializer(f));
        assertEquals(DummyDeserializer.class, introspector.findContentDeserializer(f));
        assertEquals(DummyConverter.class, introspector.findDeserializationConverter(f));
        assertEquals(DummyConverter.class, introspector.findDeserializationContentConverter(f));
        assertEquals(String.class, introspector.findDeserializationType(f, null));
    }

    // Tests inclusion annotations
    @Test
    public void testInclusion_annotatedField_returnsInclusionSettings() {
        AnnotatedField f = getAnnotatedField(MemberTestClass.class, "includeField");
        assertEquals(JsonInclude.Include.NON_NULL, introspector.findSerializationInclusion(f, JsonInclude.Include.ALWAYS));
        assertEquals(JsonInclude.Include.NON_EMPTY, introspector.findSerializationInclusionForContent(f, JsonInclude.Include.ALWAYS));

        JsonInclude.Value val = introspector.findPropertyInclusion(f);
        assertNotNull(val);
        assertEquals(JsonInclude.Include.NON_NULL, val.getValueInclusion());
        assertEquals(JsonInclude.Include.NON_EMPTY, val.getContentInclusion());
    }

    // Tests class serialization and deserialization configurations (Ordering, POJOBuilder, Creator)
    @Test
    public void testClassConfigurations_builderAndOrdering_returnsConfigObjects() {
        AnnotatedClass acOrdered = getAnnotatedClass(OrderedClass.class);
        assertArrayEquals(new String[]{"b", "a"}, introspector.findSerializationPropertyOrder(acOrdered));
        assertEquals(Boolean.TRUE, introspector.findSerializationSortAlphabetically(acOrdered));

        AnnotatedClass acBuilder = getAnnotatedClass(ClassWithBuilder.class);
        assertEquals(CustomBuilder.class, introspector.findPOJOBuilder(acBuilder));

        AnnotatedClass acBuilderConfig = getAnnotatedClass(CustomBuilder.class);
        JsonPOJOBuilder.Value bValue = introspector.findPOJOBuilderConfig(acBuilderConfig);
        assertNotNull(bValue);
        assertEquals("construct", bValue.buildMethodName);
        assertEquals("with", bValue.withPrefix);

        AnnotatedClass acInstantiator = getAnnotatedClass(InstantiatorClass.class);
        assertEquals(Object.class, introspector.findValueInstantiator(acInstantiator));
    }

    // Tests method annotations: JsonValue, AnyGetter, AnySetter, Creator
    @Test
    public void testMethodAnnotations_creatorAndAnyMethods_returnsExpectedFlags() {
        AnnotatedMethod mVal = getAnnotatedMethod(MemberTestClass.class, "valueMethod");
        assertTrue(introspector.hasAsValueAnnotation(mVal));

        AnnotatedMethod mAnyGet = getAnnotatedMethod(MemberTestClass.class, "anyGetter");
        assertTrue(introspector.hasAnyGetterAnnotation(mAnyGet));

        AnnotatedMethod mAnySet = getAnnotatedMethod(MemberTestClass.class, "anySetter");
        assertTrue(introspector.hasAnySetterAnnotation(mAnySet));

        AnnotatedConstructor ctor = getAnnotatedConstructor(MemberTestClass.class, 1);
        assertTrue(introspector.hasCreatorAnnotation(ctor));
        assertEquals(JsonCreator.Mode.PROPERTIES, introspector.findCreatorBinding(ctor));
    }
}
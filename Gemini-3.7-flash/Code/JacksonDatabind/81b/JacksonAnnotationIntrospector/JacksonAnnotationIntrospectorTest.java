package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector _ai;
    private ObjectMapper _mapper;

    @Before
    public void setUp() {
        _ai = new JacksonAnnotationIntrospector();
        _mapper = new ObjectMapper();
    }

    private AnnotatedClass _annotatedClass(Class<?> cls) {
        JavaType type = _mapper.constructType(cls);
        BeanDescription desc = _mapper.getSerializationConfig().introspect(type);
        return desc.getClassInfo();
    }

    private AnnotatedMember _findField(Class<?> cls, String fieldName) {
        AnnotatedClass ac = _annotatedClass(cls);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    private AnnotatedMethod _findMethod(Class<?> cls, String methodName) {
        AnnotatedClass ac = _annotatedClass(cls);
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    // =========================================================================
    // Test Classes & Enums
    // =========================================================================

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface BundleAnn {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface NonBundleAnn {}

    enum SampleEnum {
        @JsonProperty("first_val")
        FIRST,
        @JsonEnumDefaultValue
        DEFAULT_VAL,
        THIRD
    }

    @JsonRootName(value = "customRoot", namespace = "http://example.com")
    @JsonIgnoreProperties({"prop1", "prop2"})
    @JsonIgnoreType(true)
    @JsonFilter("filter123")
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("A test class description")
    @JsonPropertyOrder(value = {"fieldB", "fieldA"}, alphabetic = true)
    @JsonTypeName("CustomTypeName")
    @JsonSubTypes({@JsonSubTypes.Type(value = SubClass.class, name = "sub")})
    static class FullAnnotatedClass {
        @JsonProperty(value = "field_a", index = 1, defaultValue = "defaultA", required = true, access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("Desc for fieldA")
        @JsonAlias({"alias1", "alias2"})
        public String fieldA;

        @JsonIgnore
        public int fieldB;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public Date dateField;

        @JsonManagedReference("ref-name")
        public ChildClass managedRef;

        @JsonBackReference("ref-name")
        public FullAnnotatedClass backRef;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post")
        public ChildClass unwrappedChild;

        @JacksonInject("injectId")
        public String injected;

        @JsonView(String.class)
        public String viewed;

        @JsonRawValue
        public String rawVal;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String incField;

        @JsonTypeId
        public String typeIdField;
    }

    static class SubClass extends FullAnnotatedClass {}

    static class ChildClass {}

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    static class IdentClass {
        public int id;
    }

    static class SerializationAnnClass {
        @JsonSerialize(using = JsonSerializer.None.class, as = Number.class, keyAs = String.class, contentAs = Long.class, typing = JsonSerialize.Typing.STATIC)
        public Map<Object, Object> mapField;

        @JsonGetter("customGetter")
        public String getSomething() { return "test"; }

        @JsonValue
        public String asValue() { return "val"; }

        @JsonAnyGetter
        public Map<String, Object> anyGetter() { return Collections.emptyMap(); }
    }

    static class DeserializationAnnClass {
        @JsonDeserialize(using = JsonDeserializer.None.class, as = HashMap.class, keyAs = String.class, contentAs = Integer.class)
        public Map<?, ?> mapField;

        @JsonSetter("customSetter")
        public void setSomething(String val) {}

        @JsonAnySetter
        public void anySetter(String key, Object val) {}

        @JsonMerge(OptBoolean.TRUE)
        public List<String> mergeList;
    }

    static class ConflictSetters {
        public void setVal(int x) {}
        public void setVal(Integer x) {}
        public void setStr(String s) {}
        public void setStr(Object s) {}
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    static class TypeInfoClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    static class NoTypeInfoClass {}

    // =========================================================================
    // Tests
    // =========================================================================

    // Tests version reporting
    @Test
    public void testVersion_returnsNonNullVersion() {
        Version v = _ai.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // Tests bundle annotation detection
    @Test
    public void testIsAnnotationBundle_bundleAndNonBundle() {
        Annotation bundle = FullAnnotatedClass.class.getAnnotation(JsonRootName.class);
        assertFalse(_ai.isAnnotationBundle(bundle));

        Annotation bundleAnn = BundleClass.class.getAnnotation(BundleAnn.class);
        assertTrue(_ai.isAnnotationBundle(bundleAnn));
    }

    @BundleAnn
    static class BundleClass {}

    // Tests enum value and default value resolution
    @Test
    public void testFindEnumValue_annotatedAndUnannotated() {
        assertEquals("first_val", _ai.findEnumValue(SampleEnum.FIRST));
        assertEquals("THIRD", _ai.findEnumValue(SampleEnum.THIRD));

        String[] names = new String[SampleEnum.values().length];
        String[] result = _ai.findEnumValues(SampleEnum.class, SampleEnum.values(), names);
        assertEquals("first_val", result[0]);
        assertNull(result[1]);

        Enum<?> def = _ai.findDefaultEnumValue(SampleEnum.class);
        assertEquals(SampleEnum.DEFAULT_VAL, def);
    }

    // Tests root name annotation handling
    @Test
    public void testFindRootName_presentAndAbsent() {
        AnnotatedClass ac = _annotatedClass(FullAnnotatedClass.class);
        PropertyName pn = _ai.findRootName(ac);
        assertNotNull(pn);
        assertEquals("customRoot", pn.getSimpleName());
        assertEquals("http://example.com", pn.getNamespace());

        AnnotatedClass acPlain = _annotatedClass(ChildClass.class);
        assertNull(_ai.findRootName(acPlain));
    }

    // Tests class level annotations: ignore properties, ignore type, filter, naming, description
    @Test
    public void testClassAnnotations_various() {
        AnnotatedClass ac = _annotatedClass(FullAnnotatedClass.class);

        JsonIgnoreProperties.Value ignorals = _ai.findPropertyIgnorals(ac);
        assertTrue(ignorals.findIgnoredForSerialization().contains("prop1"));
        assertTrue(ignorals.findIgnoredForSerialization().contains("prop2"));

        assertEquals(Boolean.TRUE, _ai.isIgnorableType(ac));
        assertEquals("filter123", _ai.findFilterId(ac));
        assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, _ai.findNamingStrategy(ac));
        assertEquals("A test class description", _ai.findClassDescription(ac));

        AnnotatedClass plainAc = _annotatedClass(ChildClass.class);
        assertNull(_ai.isIgnorableType(plainAc));
        assertNull(_ai.findFilterId(plainAc));
        assertNull(_ai.findNamingStrategy(plainAc));
        assertNull(_ai.findClassDescription(plainAc));
    }

    // Tests field level metadata: aliases, required, access, description, index, defaultValue
    @Test
    public void testMemberPropertyAnnotations_attributes() {
        AnnotatedMember fieldA = _findField(FullAnnotatedClass.class, "fieldA");
        assertNotNull(fieldA);

        List<PropertyName> aliases = _ai.findPropertyAliases(fieldA);
        assertNotNull(aliases);
        assertEquals(2, aliases.size());
        assertEquals("alias1", aliases.get(0).getSimpleName());

        assertEquals(Boolean.TRUE, _ai.hasRequiredMarker(fieldA));
        assertEquals(JsonProperty.Access.READ_ONLY, _ai.findPropertyAccess(fieldA));
        assertEquals("Desc for fieldA", _ai.findPropertyDescription(fieldA));
        assertEquals(Integer.valueOf(1), _ai.findPropertyIndex(fieldA));
        assertEquals("defaultA", _ai.findPropertyDefaultValue(fieldA));

        AnnotatedMember fieldB = _findField(FullAnnotatedClass.class, "fieldB");
        assertTrue(_ai.hasIgnoreMarker(fieldB));
        assertNull(_ai.hasRequiredMarker(fieldB));
    }

    // Tests formatting, reference types, unwrapping, injection, and views
    @Test
    public void testMemberPropertyAnnotations_referencesAndViews() {
        AnnotatedMember dateField = _findField(FullAnnotatedClass.class, "dateField");
        JsonFormat.Value format = _ai.findFormat(dateField);
        assertNotNull(format);
        assertEquals("yyyy-MM-dd", format.getPattern());

        AnnotatedMember managedRef = _findField(FullAnnotatedClass.class, "managedRef");
        AnnotationIntrospector.ReferenceProperty refProp = _ai.findReferenceType(managedRef);
        assertNotNull(refProp);
        assertTrue(refProp.isManagedReference());
        assertEquals("ref-name", refProp.getName());

        AnnotatedMember backRef = _findField(FullAnnotatedClass.class, "backRef");
        AnnotationIntrospector.ReferenceProperty backRefProp = _ai.findReferenceType(backRef);
        assertNotNull(backRefProp);
        assertTrue(backRefProp.isBackReference());

        AnnotatedMember unwrapped = _findField(FullAnnotatedClass.class, "unwrappedChild");
        NameTransformer transformer = _ai.findUnwrappingNameTransformer(unwrapped);
        assertNotNull(transformer);
        assertEquals("pre_name_post", transformer.transform("name"));

        AnnotatedMember injected = _findField(FullAnnotatedClass.class, "injected");
        JacksonInject.Value injVal = _ai.findInjectableValue(injected);
        assertNotNull(injVal);
        assertEquals("injectId", injVal.getId());

        AnnotatedMember viewed = _findField(FullAnnotatedClass.class, "viewed");
        Class<?>[] views = _ai.findViews(viewed);
        assertNotNull(views);
        assertEquals(1, views.length);
        assertEquals(String.class, views[0]);

        AnnotatedMember typeId = _findField(FullAnnotatedClass.class, "typeIdField");
        assertEquals(Boolean.TRUE, _ai.isTypeId(typeId));
    }

    // Tests setter conflict resolution preferring primitives and String
    @Test
    public void testResolveSetterConflict_primitiveAndStringPrecedence() {
        AnnotatedClass ac = _annotatedClass(ConflictSetters.class);
        AnnotatedMethod intSetter = null;
        AnnotatedMethod integerSetter = null;
        AnnotatedMethod strSetter = null;
        AnnotatedMethod objSetter = null;

        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("setVal".equals(m.getName())) {
                if (m.getRawParameterType(0).isPrimitive()) {
                    intSetter = m;
                } else {
                    integerSetter = m;
                }
            } else if ("setStr".equals(m.getName())) {
                if (m.getRawParameterType(0) == String.class) {
                    strSetter = m;
                } else {
                    objSetter = m;
                }
            }
        }

        MapperConfig<?> config = _mapper.getSerializationConfig();
        assertSame(intSetter, _ai.resolveSetterConflict(config, intSetter, integerSetter));
        assertSame(intSetter, _ai.resolveSetterConflict(config, integerSetter, intSetter));
        assertSame(strSetter, _ai.resolveSetterConflict(config, strSetter, objSetter));
        assertSame(strSetter, _ai.resolveSetterConflict(config, objSetter, strSetter));
    }

    // Tests type resolvers and subtypes
    @Test
    public void testTypeResolversAndSubtypes() {
        AnnotatedClass ac = _annotatedClass(FullAnnotatedClass.class);
        assertEquals("CustomTypeName", _ai.findTypeName(ac));

        List<NamedType> subtypes = _ai.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(1, subtypes.size());
        assertEquals(SubClass.class, subtypes.get(0).getType());
        assertEquals("sub", subtypes.get(0).getName());

        AnnotatedClass typeInfoAc = _annotatedClass(TypeInfoClass.class);
        TypeResolverBuilder<?> b = _ai.findTypeResolver(_mapper.getSerializationConfig(), typeInfoAc, _mapper.constructType(TypeInfoClass.class));
        assertNotNull(b);

        AnnotatedClass noTypeInfoAc = _annotatedClass(NoTypeInfoClass.class);
        TypeResolverBuilder<?> noB = _ai.findTypeResolver(_mapper.getSerializationConfig(), noTypeInfoAc, _mapper.constructType(NoTypeInfoClass.class));
        assertNotNull(noB);
    }

    // Tests object identity handling
    @Test
    public void testFindObjectIdInfo_andReferenceInfo() {
        AnnotatedClass ac = _annotatedClass(IdentClass.class);
        ObjectIdInfo info = _ai.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = _ai.findObjectReferenceInfo(ac, info);
        assertTrue(refInfo.getAlwaysAsId());
    }

    // Tests serialization annotations
    @Test
    public void testSerializationAnnotations_namingAndInclusion() {
        AnnotatedMember rawVal = _findField(FullAnnotatedClass.class, "rawVal");
        Object rawSer = _ai.findSerializer(rawVal);
        assertNotNull(rawSer);

        AnnotatedMember incField = _findField(FullAnnotatedClass.class, "incField");
        JsonInclude.Value incVal = _ai.findPropertyInclusion(incField);
        assertEquals(JsonInclude.Include.NON_EMPTY, incVal.getValueInclusion());

        AnnotatedMethod getMethod = _findMethod(SerializationAnnClass.class, "getSomething");
        PropertyName getterName = _ai.findNameForSerialization(getMethod);
        assertEquals("customGetter", getterName.getSimpleName());

        AnnotatedMethod asValMethod = _findMethod(SerializationAnnClass.class, "asValue");
        assertEquals(Boolean.TRUE, _ai.hasAsValue(asValMethod));

        AnnotatedMethod anyGetMethod = _findMethod(SerializationAnnClass.class, "anyGetter");
        assertEquals(Boolean.TRUE, _ai.hasAnyGetter(anyGetMethod));
    }

    // Tests deserialization annotations
    @Test
    public void testDeserializationAnnotations_methodsAndMerge() {
        AnnotatedMethod setMethod = _findMethod(DeserializationAnnClass.class, "setSomething");
        PropertyName setterName = _ai.findNameForDeserialization(setMethod);
        assertEquals("customSetter", setterName.getSimpleName());

        AnnotatedMethod anySetMethod = _findMethod(DeserializationAnnClass.class, "anySetter");
        assertEquals(Boolean.TRUE, _ai.hasAnySetter(anySetMethod));

        AnnotatedMember mergeField = _findField(DeserializationAnnClass.class, "mergeList");
        assertEquals(Boolean.TRUE, _ai.findMergeInfo(mergeField));
    }

    // Tests type refinement for serialization and deserialization
    @Test
    public void testRefineSerializationAndDeserializationType() throws Exception {
        MapperConfig<?> config = _mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();

        AnnotatedMember serMapField = _findField(SerializationAnnClass.class, "mapField");
        JavaType baseMapType = tf.constructMapType(Map.class, Object.class, Object.class);

        JavaType refinedSerType = _ai.refineSerializationType(config, serMapField, baseMapType);
        assertNotNull(refinedSerType);
        assertEquals(String.class, refinedSerType.getKeyType().getRawClass());
        assertEquals(Long.class, refinedSerType.getContentType().getRawClass());

        AnnotatedMember deserMapField = _findField(DeserializationAnnClass.class, "mapField");
        JavaType refinedDeserType = _ai.refineDeserializationType(config, deserMapField, baseMapType);
        assertNotNull(refinedDeserType);
        assertEquals(HashMap.class, refinedDeserType.getRawClass());
        assertEquals(String.class, refinedDeserType.getKeyType().getRawClass());
        assertEquals(Integer.class, refinedDeserType.getContentType().getRawClass());
    }

    // Tests configuration toggle for ConstructorProperties
    @Test
    public void testSetConstructorPropertiesImpliesCreator() {
        assertSame(_ai, _ai.setConstructorPropertiesImpliesCreator(false));
        assertSame(_ai, _ai.setConstructorPropertiesImpliesCreator(true));
    }
}
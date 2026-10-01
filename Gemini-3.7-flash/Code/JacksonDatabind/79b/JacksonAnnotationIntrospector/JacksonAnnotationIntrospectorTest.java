package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector _introspector;
    private ObjectMapper _mapper;

    @Before
    public void setUp() {
        _introspector = new JacksonAnnotationIntrospector();
        _mapper = new ObjectMapper();
    }

    private AnnotatedClass _getAnnotatedClass(Class<?> cls) {
        return _mapper.getSerializationConfig().introspect(
                _mapper.constructType(cls)).getClassInfo();
    }

    private AnnotatedMethod _findMethod(AnnotatedClass ac, String name) {
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private AnnotatedField _findField(AnnotatedClass ac, String name) {
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    // Tests version retrieval
    @Test
    public void testVersion_returnsNonNullVersion() {
        Version v = _introspector.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // Tests findEnumValue with explicit @JsonProperty and default fallback
    private enum TestEnum {
        @JsonProperty("first_value")
        FIRST,
        SECOND,
        @JsonEnumDefaultValue
        DEFAULT_VAL
    }

    @Test
    public void testFindEnumValue_explicitAnnotation_returnsCustomName() {
        assertEquals("first_value", _introspector.findEnumValue(TestEnum.FIRST));
        assertEquals("SECOND", _introspector.findEnumValue(TestEnum.SECOND));
    }

    // Tests findEnumValues batch resolution
    @Test
    public void testFindEnumValues_array_replacesAnnotatedNames() {
        TestEnum[] values = TestEnum.values();
        String[] names = new String[] { "FIRST", "SECOND", "DEFAULT_VAL" };
        String[] result = _introspector.findEnumValues(TestEnum.class, values, names);

        assertNotNull(result);
        assertEquals("first_value", result[0]);
        assertEquals("SECOND", result[1]);
        assertEquals("DEFAULT_VAL", result[2]);
    }

    // Tests class level annotations: @JsonRootName, @JsonIgnoreProperties, @JsonIgnoreType
    @JsonRootName(value = "root", namespace = "http://example.com")
    @JsonIgnoreProperties(value = { "ignoredProp" }, ignoreUnknown = true, allowGetters = true)
    private static class RootAndIgnoreBean {
    }

    @JsonIgnoreType
    private static class IgnoredTypeBean {
    }

    @Test
    public void testClassAnnotations_rootNameAndIgnoredProperties_returnsConfiguredValues() {
        AnnotatedClass ac = _getAnnotatedClass(RootAndIgnoreBean.class);

        PropertyName rootName = _introspector.findRootName(ac);
        assertNotNull(rootName);
        assertEquals("root", rootName.getSimpleName());
        assertEquals("http://example.com", rootName.getNamespace());

        assertTrue(_introspector.findIgnoreUnknownProperties(ac));
        assertNull(_introspector.findPropertiesToIgnore(ac, true));
        assertArrayEquals(new String[] { "ignoredProp" }, _introspector.findPropertiesToIgnore(ac, false));

        AnnotatedClass acIgnoreType = _getAnnotatedClass(IgnoredTypeBean.class);
        assertTrue(_introspector.isIgnorableType(acIgnoreType));
    }

    // Tests @JsonFilter, @JsonNaming, @JsonClassDescription
    @JsonFilter("filter123")
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("A bean description")
    private static class MetaClassBean {
    }

    @Test
    public void testClassAnnotations_filterNamingAndDescription_returnsExpectedValues() {
        AnnotatedClass ac = _getAnnotatedClass(MetaClassBean.class);

        assertEquals("filter123", _introspector.findFilterId(ac));
        assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, _introspector.findNamingStrategy(ac));
        assertEquals("A bean description", _introspector.findClassDescription(ac));
    }

    // Tests property level annotations: @JsonProperty markers
    private static class PropertyMarkerBean {
        @JsonProperty(value = "customProp", index = 3, defaultValue = "defVal", access = JsonProperty.Access.READ_ONLY)
        public String prop;

        @JsonIgnore
        public int ignoredField;
    }

    @Test
    public void testPropertyAnnotations_markersAndAccess_returnsCorrectMetadata() {
        AnnotatedClass ac = _getAnnotatedClass(PropertyMarkerBean.class);
        AnnotatedField field = _findField(ac, "prop");
        AnnotatedField ignoredField = _findField(ac, "ignoredField");

        assertNotNull(field);
        assertEquals(Integer.valueOf(3), _introspector.findPropertyIndex(field));
        assertEquals("defVal", _introspector.findPropertyDefaultValue(field));
        assertEquals(JsonProperty.Access.READ_ONLY, _introspector.findPropertyAccess(field));
        assertFalse(_introspector.hasIgnoreMarker(field));

        assertNotNull(ignoredField);
        assertTrue(_introspector.hasIgnoreMarker(ignoredField));
    }

    // Tests @JsonFormat
    private static class FormatBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public String date;
    }

    @Test
    public void testFindFormat_configuredPattern_returnsFormatValue() {
        AnnotatedClass ac = _getAnnotatedClass(FormatBean.class);
        AnnotatedField field = _findField(ac, "date");

        JsonFormat.Value format = _introspector.findFormat(field);
        assertNotNull(format);
        assertEquals("yyyy-MM-dd", format.getPattern());
        assertEquals(JsonFormat.Shape.STRING, format.getShape());
    }

    // Tests @JsonManagedReference and @JsonBackReference
    private static class ReferenceBean {
        @JsonManagedReference("parent-child")
        public ReferenceBean child;

        @JsonBackReference("parent-child")
        public ReferenceBean parent;
    }

    @Test
    public void testFindReferenceType_managedAndBack_returnsReferenceProperty() {
        AnnotatedClass ac = _getAnnotatedClass(ReferenceBean.class);
        AnnotatedField childField = _findField(ac, "child");
        AnnotatedField parentField = _findField(ac, "parent");

        AnnotationIntrospector.ReferenceProperty managed = _introspector.findReferenceType(childField);
        assertNotNull(managed);
        assertTrue(managed.isManagedReference());
        assertEquals("parent-child", managed.getName());

        AnnotationIntrospector.ReferenceProperty back = _introspector.findReferenceType(parentField);
        assertNotNull(back);
        assertTrue(back.isBackReference());
        assertEquals("parent-child", back.getName());
    }

    // Tests @JsonUnwrapped
    private static class UnwrappedBean {
        @JsonUnwrapped(prefix = "pre_", suffix = "_post")
        public Object unwrappedProp;

        @JsonUnwrapped(enabled = false)
        public Object disabledUnwrapped;
    }

    @Test
    public void testFindUnwrappingNameTransformer_enabledAndDisabled_returnsTransformerOrNull() {
        AnnotatedClass ac = _getAnnotatedClass(UnwrappedBean.class);
        AnnotatedField enabledField = _findField(ac, "unwrappedProp");
        AnnotatedField disabledField = _findField(ac, "disabledUnwrapped");

        NameTransformer transformer = _introspector.findUnwrappingNameTransformer(enabledField);
        assertNotNull(transformer);
        assertEquals("pre_name_post", transformer.transform("name"));

        assertNull(_introspector.findUnwrappingNameTransformer(disabledField));
    }

    // Tests @JacksonInject and @JsonView
    private static class View1 {}
    private static class View2 {}

    private static class InjectAndViewBean {
        @JacksonInject("customInjectId")
        @JsonView({ View1.class, View2.class })
        public String field;
    }

    @Test
    public void testFindInjectableAndViews_configuredAnnotations_returnsIdAndViews() {
        AnnotatedClass ac = _getAnnotatedClass(InjectAndViewBean.class);
        AnnotatedField field = _findField(ac, "field");

        assertEquals("customInjectId", _introspector.findInjectableValueId(field));
        Class<?>[] views = _introspector.findViews(field);
        assertNotNull(views);
        assertEquals(2, views.length);
        assertEquals(View1.class, views[0]);
        assertEquals(View2.class, views[1]);
    }

    // Tests resolveSetterConflict
    private static class SetterConflictBean {
        public void setVal(int x) {}
        public void setVal(Integer x) {}
        public void setName(String s) {}
        public void setName(Object o) {}
    }

    @Test
    public void testResolveSetterConflict_prefersPrimitivesAndString() {
        AnnotatedClass ac = _getAnnotatedClass(SetterConflictBean.class);
        AnnotatedMethod setterInt = null;
        AnnotatedMethod setterInteger = null;
        AnnotatedMethod setterString = null;
        AnnotatedMethod setterObject = null;

        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("setVal".equals(m.getName())) {
                if (m.getRawParameterType(0).isPrimitive()) {
                    setterInt = m;
                } else {
                    setterInteger = m;
                }
            } else if ("setName".equals(m.getName())) {
                if (m.getRawParameterType(0) == String.class) {
                    setterString = m;
                } else {
                    setterObject = m;
                }
            }
        }

        assertNotNull(setterInt);
        assertNotNull(setterInteger);
        assertEquals(setterInt, _introspector.resolveSetterConflict(null, setterInt, setterInteger));
        assertEquals(setterInt, _introspector.resolveSetterConflict(null, setterInteger, setterInt));

        assertNotNull(setterString);
        assertNotNull(setterObject);
        assertEquals(setterString, _introspector.resolveSetterConflict(null, setterString, setterObject));
        assertEquals(setterString, _introspector.resolveSetterConflict(null, setterObject, setterString));
    }

    // Tests @JsonSubTypes and @JsonTypeName
    @JsonTypeName("baseType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubTypeA.class, name = "typeA"),
        @JsonSubTypes.Type(value = SubTypeB.class, name = "typeB")
    })
    private static class PolyBaseBean {}
    private static class SubTypeA extends PolyBaseBean {}
    private static class SubTypeB extends PolyBaseBean {}

    @Test
    public void testFindSubtypesAndTypeName_polymorphicConfig_returnsCorrectSubtypes() {
        AnnotatedClass ac = _getAnnotatedClass(PolyBaseBean.class);

        assertEquals("baseType", _introspector.findTypeName(ac));
        List<NamedType> subtypes = _introspector.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        assertEquals("typeA", subtypes.get(0).getName());
        assertEquals(SubTypeA.class, subtypes.get(0).getType());
        assertEquals("typeB", subtypes.get(1).getName());
        assertEquals(SubTypeB.class, subtypes.get(1).getType());
    }

    // Tests @JsonIdentityInfo and @JsonIdentityReference
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private static class IdentityBean {
        public int id;
    }

    @Test
    public void testFindObjectIdInfo_identityConfigured_returnsObjectIdInfo() {
        AnnotatedClass ac = _getAnnotatedClass(IdentityBean.class);

        ObjectIdInfo info = _introspector.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = _introspector.findObjectReferenceInfo(ac, info);
        assertNotNull(refInfo);
        assertTrue(refInfo.getAlwaysAsId());
    }

    // Tests @JsonSerialize and @JsonDeserialize
    @SuppressWarnings("serial")
    private static class CustomSer extends StdSerializer<String> {
        public CustomSer() { super(String.class); }
        @Override
        public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) {}
    }

    @SuppressWarnings("serial")
    private static class CustomDeser extends StdDeserializer<String> {
        public CustomDeser() { super(String.class); }
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) { return null; }
    }

    private static class CustomSerDeserBean {
        @JsonSerialize(using = CustomSer.class)
        @JsonDeserialize(using = CustomDeser.class)
        public String prop;
    }

    @Test
    public void testFindSerializerAndDeserializer_explicitClasses_returnsConfiguredClasses() {
        AnnotatedClass ac = _getAnnotatedClass(CustomSerDeserBean.class);
        AnnotatedField field = _findField(ac, "prop");

        assertEquals(CustomSer.class, _introspector.findSerializer(field));
        assertEquals(CustomDeser.class, _introspector.findDeserializer(field));
    }

    // Tests @JsonInclude
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    private static class InclusionBean {
    }

    @Test
    public void testFindPropertyInclusion_configuredIncludes_returnsCorrectValues() {
        AnnotatedClass ac = _getAnnotatedClass(InclusionBean.class);

        JsonInclude.Value incl = _introspector.findPropertyInclusion(ac);
        assertNotNull(incl);
        assertEquals(JsonInclude.Include.NON_EMPTY, incl.getValueInclusion());
        assertEquals(JsonInclude.Include.NON_NULL, incl.getContentInclusion());
    }

    // Tests @JsonPropertyOrder
    @JsonPropertyOrder(value = { "a", "b", "c" }, alphabetic = true)
    private static class OrderBean {
        public int b;
        public int a;
        public int c;
    }

    @Test
    public void testFindSerializationPropertyOrder_andSortAlpha_returnsConfiguredOrder() {
        AnnotatedClass ac = _getAnnotatedClass(OrderBean.class);

        String[] order = _introspector.findSerializationPropertyOrder(ac);
        assertNotNull(order);
        assertArrayEquals(new String[] { "a", "b", "c" }, order);
        assertEquals(Boolean.TRUE, _introspector.findSerializationSortAlphabetically(ac));
    }

    // Tests @JsonGetter and @JsonSetter name resolution
    private static class GetterSetterBean {
        @JsonGetter("customGet")
        public String getFoo() { return "foo"; }

        @JsonSetter("customSet")
        public void setFoo(String v) {}
    }

    @Test
    public void testFindNameForSerializationAndDeserialization_getterSetter_returnsExplicitNames() {
        AnnotatedClass ac = _getAnnotatedClass(GetterSetterBean.class);
        AnnotatedMethod getter = _findMethod(ac, "getFoo");
        AnnotatedMethod setter = _findMethod(ac, "setFoo");

        PropertyName serName = _introspector.findNameForSerialization(getter);
        assertNotNull(serName);
        assertEquals("customGet", serName.getSimpleName());

        PropertyName deserName = _introspector.findNameForDeserialization(setter);
        assertNotNull(deserName);
        assertEquals("customSet", deserName.getSimpleName());
    }

    // Tests @JsonCreator and mode
    private static class CreatorBean {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public CreatorBean(@JsonProperty("name") String name) {}

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public CreatorBean(int id) {}
    }

    @Test
    public void testHasCreatorAnnotation_enabledAndDisabled_returnsExpectedBoolean() {
        AnnotatedClass ac = _getAnnotatedClass(CreatorBean.class);
        List<AnnotatedConstructor> ctors = ac.getConstructors();

        AnnotatedConstructor activeCtor = null;
        AnnotatedConstructor disabledCtor = null;

        for (AnnotatedConstructor c : ctors) {
            if (c.getParameterCount() == 1) {
                if (c.getRawParameterType(0) == String.class) {
                    activeCtor = c;
                } else if (c.getRawParameterType(0) == int.class) {
                    disabledCtor = c;
                }
            }
        }

        assertNotNull(activeCtor);
        assertNotNull(disabledCtor);

        assertTrue(_introspector.hasCreatorAnnotation(activeCtor));
        assertEquals(JsonCreator.Mode.PROPERTIES, _introspector.findCreatorBinding(activeCtor));

        assertFalse(_introspector.hasCreatorAnnotation(disabledCtor));
        assertEquals(JsonCreator.Mode.DISABLED, _introspector.findCreatorBinding(disabledCtor));
    }

    // Tests @JacksonAnnotationsInside bundle detection
    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomBundleAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface NormalAnnotation {}

    @CustomBundleAnnotation
    private static class BundleTargetBean {}

    @NormalAnnotation
    private static class NormalTargetBean {}

    @Test
    public void testIsAnnotationBundle_bundleAndNormal_returnsCorrectBooleanAndCaches() {
        CustomBundleAnnotation bundleAnn = BundleTargetBean.class.getAnnotation(CustomBundleAnnotation.class);
        NormalAnnotation normalAnn = NormalTargetBean.class.getAnnotation(NormalAnnotation.class);

        assertTrue(_introspector.isAnnotationBundle(bundleAnn));
        // Test caching path
        assertTrue(_introspector.isAnnotationBundle(bundleAnn));

        assertFalse(_introspector.isAnnotationBundle(normalAnn));
        // Test caching path
        assertFalse(_introspector.isAnnotationBundle(normalAnn));
    }

    // Tests @JsonValue
    private static class JsonValueBean {
        @JsonValue
        public String asValue() { return "value"; }
    }

    @Test
    public void testHasAsValueAnnotation_annotatedMethod_returnsTrue() {
        AnnotatedClass ac = _getAnnotatedClass(JsonValueBean.class);
        AnnotatedMethod method = _findMethod(ac, "asValue");

        assertNotNull(method);
        assertTrue(_introspector.hasAsValueAnnotation(method));
    }

    // Additional tests for coverage

    @Test
    public void testFindDefaultEnumValue() {
        AnnotatedClass ac = _getAnnotatedClass(TestEnum.class);
        assertEquals(TestEnum.DEFAULT_VAL, _introspector.findDefaultEnumValue(ac, TestEnum.class));
    }

    @JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
    )
    private static class AutoDetectBean {}

    @Test
    public void testFindAutoDetectVisibility() {
        AnnotatedClass ac = _getAnnotatedClass(AutoDetectBean.class);
        VisibilityChecker<?> checker = _introspector.findAutoDetectVisibility(
                ac, _mapper.getVisibilityChecker());
        assertNotNull(checker);
    }

    private static class AliasAndDescriptionBean {
        @JsonAlias({"alias1", "alias2"})
        @JsonPropertyDescription("A property description")
        public String aliasedProp;
    }

    @Test
    public void testFindPropertyAliasesAndDescription() {
        AnnotatedClass ac = _getAnnotatedClass(AliasAndDescriptionBean.class);
        AnnotatedField field = _findField(ac, "aliasedProp");

        List<PropertyName> aliases = _introspector.findPropertyAliases(field);
        assertNotNull(aliases);
        assertEquals(2, aliases.size());
        assertEquals("alias1", aliases.get(0).getSimpleName());
        assertEquals("alias2", aliases.get(1).getSimpleName());

        assertEquals("A property description", _introspector.findPropertyDescription(field));
    }

    private static class AnyGetterSetterBean {
        @JsonAnyGetter
        public Map<String, Object> anyGet() { return null; }

        @JsonAnySetter
        public void anySet(String name, Object value) {}
    }

    @Test
    public void testFindAnyGetterAndSetter() {
        AnnotatedClass ac = _getAnnotatedClass(AnyGetterSetterBean.class);
        AnnotatedMethod getter = _findMethod(ac, "anyGet");
        AnnotatedMethod setter = _findMethod(ac, "anySet");

        assertTrue(_introspector.hasAnyGetterAnnotation(getter));
        assertTrue(_introspector.hasAnySetterAnnotation(setter));
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private static class TypeInfoBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public Object polyProp;
    }

    @Test
    public void testFindTypeResolver() {
        AnnotatedClass ac = _getAnnotatedClass(TypeInfoBean.class);
        JavaType baseType = _mapper.constructType(TypeInfoBean.class);
        TypeResolverBuilder<?> b = _introspector.findTypeResolver(_mapper.getSerializationConfig(), ac, baseType);
        assertNotNull(b);

        AnnotatedField field = _findField(ac, "polyProp");
        JavaType propType = _mapper.constructType(Object.class);
        TypeResolverBuilder<?> pb = _introspector.findPropertyTypeResolver(_mapper.getSerializationConfig(), field, propType);
        assertNotNull(pb);
    }

    @JsonPOJOBuilder(buildMethodName = "construct", withPrefix = "with")
    private static class CustomBuilder {}

    @JsonDeserialize(builder = CustomBuilder.class)
    private static class BuilderValueClass {}

    @Test
    public void testFindPOJOBuilderAndConfig() {
        AnnotatedClass ac = _getAnnotatedClass(BuilderValueClass.class);
        Class<?> builderCls = _introspector.findPOJOBuilder(ac);
        assertEquals(CustomBuilder.class, builderCls);

        AnnotatedClass builderAc = _getAnnotatedClass(CustomBuilder.class);
        JsonPOJOBuilder.Value bConfig = _introspector.findPOJOBuilderConfig(builderAc);
        assertNotNull(bConfig);
        assertEquals("construct", bConfig.buildMethodName);
        assertEquals("with", bConfig.withPrefix);
    }

    private static abstract class DummyConverter implements Converter<String, Integer> {}

    private static class ConverterBean {
        @JsonSerialize(converter = DummyConverter.class)
        @JsonDeserialize(converter = DummyConverter.class)
        public String convertedProp;
    }

    @Test
    public void testFindConverters() {
        AnnotatedClass ac = _getAnnotatedClass(ConverterBean.class);
        AnnotatedField field = _findField(ac, "convertedProp");

        assertEquals(DummyConverter.class, _introspector.findSerializationConverter(field));
        assertEquals(DummyConverter.class, _introspector.findDeserializationConverter(field));
    }

    private static class MergeBean {
        @JsonMerge(OptBoolean.TRUE)
        public String merged;
    }

    @Test
    public void testFindMergeInfo() {
        AnnotatedClass ac = _getAnnotatedClass(MergeBean.class);
        AnnotatedField field = _findField(ac, "merged");
        assertEquals(Boolean.TRUE, _introspector.findMergeInfo(field));
    }

    private static class SetterInfoBean {
        @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.SKIP)
        public String setterField;
    }

    @Test
    public void testFindSetterInfo() {
        AnnotatedClass ac = _getAnnotatedClass(SetterInfoBean.class);
        AnnotatedField field = _findField(ac, "setterField");
        JsonSetter.Value value = _introspector.findSetterInfo(field);
        assertNotNull(value);
        assertEquals(Nulls.AS_EMPTY, value.getValueNulls());
        assertEquals(Nulls.SKIP, value.getContentNulls());
    }
}
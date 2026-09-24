package com.fasterxml.jackson.databind;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AnnotationIntrospectorTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface DummyAnnotation {
        String value() default "";
    }

    private static class DummyIntrospector extends AnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        private Class<?> _serType;
        private Class<?> _serKeyType;
        private Class<?> _serContentType;
        private Class<?> _deserType;
        private Class<?> _deserKeyType;
        private Class<?> _deserContentType;

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public Class<?> findSerializationType(Annotated a) {
            return _serType;
        }

        @Override
        public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
            return _serKeyType;
        }

        @Override
        public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
            return _serContentType;
        }

        @Override
        public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
            return _deserType;
        }

        @Override
        public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType) {
            return _deserKeyType;
        }

        @SuppressWarnings("deprecation")
        @Override
        public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
            return _deserContentType;
        }

        public DummyIntrospector withSerType(Class<?> cls) {
            _serType = cls;
            return this;
        }

        public DummyIntrospector withSerKeyType(Class<?> cls) {
            _serKeyType = cls;
            return this;
        }

        public DummyIntrospector withSerContentType(Class<?> cls) {
            _serContentType = cls;
            return this;
        }

        public DummyIntrospector withDeserType(Class<?> cls) {
            _deserType = cls;
            return this;
        }

        public DummyIntrospector withDeserKeyType(Class<?> cls) {
            _deserKeyType = cls;
            return this;
        }

        public DummyIntrospector withDeserContentType(Class<?> cls) {
            _deserContentType = cls;
            return this;
        }
    }

    private enum TestEnum {
        A, B
    }

    // Tests ReferenceProperty helper creation and queries
    @Test
    public void testReferenceProperty_creationAndQueries() {
        AnnotationIntrospector.ReferenceProperty managed = AnnotationIntrospector.ReferenceProperty.managed("parent");
        assertNotNull(managed);
        assertEquals("parent", managed.getName());
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, managed.getType());
        assertTrue(managed.isManagedReference());
        assertFalse(managed.isBackReference());

        AnnotationIntrospector.ReferenceProperty back = AnnotationIntrospector.ReferenceProperty.back("child");
        assertNotNull(back);
        assertEquals("child", back.getName());
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE, back.getType());
        assertFalse(back.isManagedReference());
        assertTrue(back.isBackReference());
    }

    // Tests nopInstance and pair factory methods
    @Test
    public void testFactoryMethods_nopAndPair() {
        AnnotationIntrospector nop = AnnotationIntrospector.nopInstance();
        assertNotNull(nop);
        assertSame(NopAnnotationIntrospector.instance, nop);

        DummyIntrospector d1 = new DummyIntrospector();
        DummyIntrospector d2 = new DummyIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospector.pair(d1, d2);
        assertNotNull(pair);
        assertTrue(pair instanceof AnnotationIntrospectorPair);
    }

    // Tests allIntrospectors collection retrieval
    @Test
    public void testAllIntrospectors_collectionRetrieval() {
        DummyIntrospector intro = new DummyIntrospector();
        Collection<AnnotationIntrospector> list = intro.allIntrospectors();
        assertEquals(1, list.size());
        assertTrue(list.contains(intro));

        List<AnnotationIntrospector> target = new ArrayList<AnnotationIntrospector>();
        Collection<AnnotationIntrospector> result = intro.allIntrospectors(target);
        assertSame(target, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(intro));
    }

    // Tests default implementations of various introspector methods
    @SuppressWarnings("deprecation")
    @Test
    public void testDefaultMethodImplementations_returnNullOrDefaults() {
        DummyIntrospector intro = new DummyIntrospector();
        assertFalse(intro.isAnnotationBundle((Annotation) null));
        assertNull(intro.findObjectIdInfo(null));

        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class, null, null);
        assertSame(info, intro.findObjectReferenceInfo(null, info));

        assertNull(intro.findRootName((AnnotatedClass) null));
        assertNull(intro.findPropertiesToIgnore((Annotated) null, true));
        assertNull(intro.findPropertiesToIgnore((Annotated) null));
        assertNull(intro.findIgnoreUnknownProperties(null));
        assertNull(intro.isIgnorableType(null));
        assertNull(intro.findFilterId(null));
        assertNull(intro.findNamingStrategy(null));
        assertNull(intro.findClassDescription(null));
        assertNull(intro.findTypeResolver(null, null, null));
        assertNull(intro.findPropertyTypeResolver(null, null, null));
        assertNull(intro.findPropertyContentTypeResolver(null, null, null));
        assertNull(intro.findSubtypes(null));
        assertNull(intro.findTypeName(null));
        assertNull(intro.isTypeId(null));
        assertNull(intro.findReferenceType(null));
        assertNull(intro.findUnwrappingNameTransformer(null));
        assertFalse(intro.hasIgnoreMarker(null));
        assertNull(intro.findInjectableValueId(null));
        assertNull(intro.hasRequiredMarker(null));
        assertNull(intro.findViews(null));
        assertNull(intro.findFormat((Annotated) null));
        assertNull(intro.findWrapperName(null));
        assertNull(intro.findPropertyDefaultValue(null));
        assertNull(intro.findPropertyDescription(null));
        assertNull(intro.findPropertyIndex(null));
        assertNull(intro.findImplicitPropertyName(null));
        assertNull(intro.findPropertyAccess(null));
        assertNull(intro.resolveSetterConflict(null, null, null));
        assertNull(intro.findSerializer(null));
        assertNull(intro.findKeySerializer(null));
        assertNull(intro.findContentSerializer(null));
        assertNull(intro.findNullSerializer(null));
        assertNull(intro.findSerializationTyping(null));
        assertNull(intro.findSerializationConverter(null));
        assertNull(intro.findSerializationContentConverter(null));
        assertNull(intro.findSerializationPropertyOrder(null));
        assertNull(intro.findSerializationSortAlphabetically(null));
        assertNull(intro.findNameForSerialization(null));
        assertFalse(intro.hasAsValueAnnotation((AnnotatedMethod) null));
        assertNull(intro.findDeserializer(null));
        assertNull(intro.findKeyDeserializer(null));
        assertNull(intro.findContentDeserializer(null));
        assertNull(intro.findDeserializationConverter(null));
        assertNull(intro.findDeserializationContentConverter(null));
        assertNull(intro.findValueInstantiator(null));
        assertNull(intro.findPOJOBuilder(null));
        assertNull(intro.findPOJOBuilderConfig(null));
        assertNull(intro.findNameForDeserialization(null));
        assertFalse(intro.hasAnySetterAnnotation((AnnotatedMethod) null));
        assertFalse(intro.hasAnyGetterAnnotation((AnnotatedMethod) null));
        assertFalse(intro.hasCreatorAnnotation((Annotated) null));
        assertNull(intro.findCreatorBinding(null));
    }

    // Tests findAutoDetectVisibility returning unchanged checker
    @Test
    public void testFindAutoDetectVisibility_returnsGivenChecker() {
        DummyIntrospector intro = new DummyIntrospector();
        VisibilityChecker<?> checker = VisibilityChecker.Std.defaultInstance();
        assertSame(checker, intro.findAutoDetectVisibility(null, checker));
    }

    // Tests findEnumValue and findEnumValues default logic
    @Test
    public void testFindEnumValues_populatesNullSlotsOnly() {
        DummyIntrospector intro = new DummyIntrospector();
        assertEquals("A", intro.findEnumValue(TestEnum.A));

        Enum<?>[] values = new Enum<?>[] { TestEnum.A, TestEnum.B };
        String[] names = new String[] { "CUSTOM_A", null };
        String[] result = intro.findEnumValues(TestEnum.class, values, names);

        assertSame(names, result);
        assertEquals("CUSTOM_A", result[0]);
        assertEquals("B", result[1]);
    }

    // Tests refineSerializationType when no refinement annotation is present
    @Test
    public void testRefineSerializationType_noRefinement() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(Number.class);

        DummyIntrospector intro = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Number.class, config, null);

        JavaType result = intro.refineSerializationType(config, ac, baseType);
        assertSame(baseType, result);
    }

    // Tests refineSerializationType when target class has same raw class
    @Test
    public void testRefineSerializationType_sameRawClass_appliesStaticTyping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(Number.class);

        DummyIntrospector intro = new DummyIntrospector().withSerType(Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Number.class, config, null);

        JavaType result = intro.refineSerializationType(config, ac, baseType);
        assertNotNull(result);
        assertEquals(Number.class, result.getRawClass());
        assertTrue(result.useStaticType());
    }

    // Tests refineSerializationType when generalizing base type
    @Test
    public void testRefineSerializationType_generalizedType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(Integer.class);

        DummyIntrospector intro = new DummyIntrospector().withSerType(Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Integer.class, config, null);

        JavaType result = intro.refineSerializationType(config, ac, baseType);
        assertEquals(Number.class, result.getRawClass());
    }

    // Tests refineSerializationType with Map key and content types
    @Test
    public void testRefineSerializationType_mapGeneralizationAndSpecialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructMapType(HashMap.class, Number.class, Object.class);

        DummyIntrospector intro = new DummyIntrospector()
                .withSerKeyType(Object.class)
                .withSerContentType(String.class);

        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(HashMap.class, config, null);
        JavaType result = intro.refineSerializationType(config, ac, baseType);

        assertEquals(Object.class, result.getKeyType().getRawClass());
        assertEquals(String.class, result.getContentType().getRawClass());
    }

    // Tests refineSerializationType throwing exception on unrelated key type
    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationType_unrelatedKeyType_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructMapType(HashMap.class, String.class, Object.class);

        DummyIntrospector intro = new DummyIntrospector().withSerKeyType(Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(HashMap.class, config, null);

        intro.refineSerializationType(config, ac, baseType);
    }

    // Tests refineSerializationType throwing exception on unrelated content type
    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationType_unrelatedContentType_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructCollectionType(ArrayList.class, String.class);

        DummyIntrospector intro = new DummyIntrospector().withSerContentType(Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(ArrayList.class, config, null);

        intro.refineSerializationType(config, ac, baseType);
    }

    // Tests refineDeserializationType narrowing main, key, and content types
    @Test
    public void testRefineDeserializationType_specializedTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructMapType(Map.class, Object.class, Object.class);

        DummyIntrospector intro = new DummyIntrospector()
                .withDeserType(HashMap.class)
                .withDeserKeyType(String.class)
                .withDeserContentType(Integer.class);

        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, config, null);
        JavaType result = intro.refineDeserializationType(config, ac, baseType);

        assertEquals(HashMap.class, result.getRawClass());
        assertEquals(String.class, result.getKeyType().getRawClass());
        assertEquals(Integer.class, result.getContentType().getRawClass());
    }

    // Tests refineDeserializationType throwing exception on invalid subtype
    @Test(expected = JsonMappingException.class)
    public void testRefineDeserializationType_invalidSubtype_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(String.class);

        DummyIntrospector intro = new DummyIntrospector().withDeserType(Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config, null);

        intro.refineDeserializationType(config, ac, baseType);
    }

    // Tests protected annotation lookup helper methods
    @Test
    public void testProtectedAnnotationHelpers() {
        DummyIntrospector intro = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(DummyAnnotatedClass.class, null, null);

        DummyAnnotation ann = intro._findAnnotation(ac, DummyAnnotation.class);
        assertNotNull(ann);
        assertEquals("test", ann.value());

        assertTrue(intro._hasAnnotation(ac, DummyAnnotation.class));
        assertFalse(intro._hasAnnotation(ac, Retention.class));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] checkList = new Class[] { Retention.class, DummyAnnotation.class };
        assertTrue(intro._hasOneOf(ac, checkList));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] noneList = new Class[] { Retention.class };
        assertFalse(intro._hasOneOf(ac, noneList));
    }

    @DummyAnnotation("test")
    private static class DummyAnnotatedClass {
    }
}
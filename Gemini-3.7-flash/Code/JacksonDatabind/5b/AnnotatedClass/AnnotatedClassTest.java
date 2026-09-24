package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;

import static org.junit.Assert.*;

public class AnnotatedClassTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnn {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnn2 {
        String value() default "";
    }

    static class SimpleMixInResolver implements ClassIntrospector.MixInResolver {
        private final Map<Class<?>, Class<?>> _mixIns = new HashMap<Class<?>, Class<?>>();

        public void addMixIn(Class<?> target, Class<?> mixin) {
            _mixIns.put(target, mixin);
        }

        @Override
        public Class<?> findMixInClassFor(Class<?> cls) {
            return _mixIns.get(cls);
        }

        public ClassIntrospector.MixInResolver copy() {
            return this;
        }
    }

    interface BaseInterface {
        @CustomAnn("interfaceMethod")
        void doSomething();
    }

    @CustomAnn("superClass")
    static class SuperClass implements BaseInterface {
        public int superField;

        @Override
        public void doSomething() { }

        public void superMethod() { }
    }

    @CustomAnn2("subClass")
    static class SubClass extends SuperClass {
        public int subField;
        private int _ignoredField;

        public SubClass() { }

        public SubClass(int value) {
            this.subField = value;
        }

        @Override
        public void doSomething() { }

        public static SubClass create(int v) {
            return new SubClass(v);
        }

        @JsonIgnore
        public static SubClass ignoredFactory(int v) {
            return new SubClass(v);
        }

        @JsonIgnore
        public void ignoredMethod() { }
    }

    @CustomAnn("classMixIn")
    static class MixInForSubClass {
        @JsonProperty("prop")
        public int subField;

        @CustomAnn("mixInCtor")
        public MixInForSubClass(@JsonProperty("val") int value) { }

        @CustomAnn("mixInMethod")
        public void doSomething() { }

        @CustomAnn("mixInFactory")
        public static SubClass create(@JsonProperty("val") int v) {
            return null;
        }
    }

    static class InnerClassHolder {
        class InnerClass {
            public InnerClass(@CustomAnn("param") String name) { }
        }
    }

    enum SampleEnum {
        A("first"),
        B("second");

        private final String desc;
        SampleEnum(@CustomAnn("enumParam") String desc) {
            this.desc = desc;
        }
    }

    static class ObjectMixIn {
        @CustomAnn("hashCodeMixIn")
        @Override
        public int hashCode() {
            return 0;
        }
    }

    private AnnotationIntrospector _introspector;
    private SimpleMixInResolver _mixInResolver;

    @Before
    public void setUp() {
        _introspector = new JacksonAnnotationIntrospector();
        _mixInResolver = new SimpleMixInResolver();
    }

    // Tests construction and generic type/modifier accessors
    @Test
    public void testBasicProperties_validClass_returnsCorrectMetadata() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);
        assertEquals(SubClass.class, ac.getAnnotated());
        assertEquals(SubClass.class, ac.getRawType());
        assertEquals(SubClass.class, ac.getGenericType());
        assertEquals(SubClass.class.getName(), ac.getName());
        assertEquals(SubClass.class.getModifiers(), ac.getModifiers());
        assertEquals("[AnnotedClass " + SubClass.class.getName() + "]", ac.toString());
    }

    // Tests class-level annotation resolution including hierarchy and mix-in overrides
    @Test
    public void testClassAnnotations_withMixInAndSuperType_resolvesCombinedAnnotations() {
        _mixInResolver.addMixIn(SubClass.class, MixInForSubClass.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertTrue(ac.hasAnnotations());
        assertEquals(2, ac.getAnnotations().size());
        assertNotNull(ac.getAllAnnotations());

        CustomAnn ann1 = ac.getAnnotation(CustomAnn.class);
        assertNotNull(ann1);
        assertEquals("classMixIn", ann1.value());

        CustomAnn2 ann2 = ac.getAnnotation(CustomAnn2.class);
        assertNotNull(ann2);
        assertEquals("subClass", ann2.value());

        int count = 0;
        for (Annotation a : ac.annotations()) {
            assertNotNull(a);
            count++;
        }
        assertEquals(2, count);
    }

    // Tests constructWithoutSuperTypes factory method
    @Test
    public void testConstructWithoutSuperTypes_superAnnotationsIgnored() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SubClass.class, _introspector, _mixInResolver);
        CustomAnn superAnn = ac.getAnnotation(CustomAnn.class);
        assertNull(superAnn);
        assertNotNull(ac.getAnnotation(CustomAnn2.class));
    }

    // Tests withAnnotations creating a shallow copy with replaced AnnotationMap
    @Test
    public void testWithAnnotations_replacesAnnotationMap() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);
        AnnotationMap newMap = new AnnotationMap();
        AnnotatedClass ac2 = ac.withAnnotations(newMap);
        assertNotSame(ac, ac2);
        assertFalse(ac2.hasAnnotations());
    }

    // Tests constructor resolution including default constructor and single argument constructor
    @Test
    public void testCreators_constructorsResolvedCorrectly() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        AnnotatedConstructor defaultCtor = ac.getDefaultConstructor();
        assertNotNull(defaultCtor);
        assertEquals(0, defaultCtor.getParameterCount());

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertNotNull(ctors);
        assertEquals(1, ctors.size());
        assertEquals(1, ctors.get(0).getParameterCount());
    }

    // Tests constructor mix-in annotations and parameter annotations
    @Test
    public void testConstructors_withMixIn_mixesOverAnnotations() {
        _mixInResolver.addMixIn(SubClass.class, MixInForSubClass.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        assertTrue(ctor.hasAnnotation(CustomAnn.class));
    }

    // Tests static creator methods and ignored creator methods filtering
    @Test
    public void testStaticMethods_creatorMethodsAndIgnoreFiltering() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);
        List<AnnotatedMethod> staticMethods = ac.getStaticMethods();
        assertNotNull(staticMethods);
        assertEquals(1, staticMethods.size());
        assertEquals("create", staticMethods.get(0).getName());
    }

    // Tests static factory mix-ins
    @Test
    public void testStaticMethods_withMixIn_mixesOverAnnotations() {
        _mixInResolver.addMixIn(SubClass.class, MixInForSubClass.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);
        List<AnnotatedMethod> staticMethods = ac.getStaticMethods();
        assertEquals(1, staticMethods.size());
        AnnotatedMethod factory = staticMethods.get(0);
        assertTrue(factory.hasAnnotation(CustomAnn.class));
    }

    // Tests member methods collection, hierarchy traversal, and findMethod
    @Test
    public void testMemberMethods_methodsResolvedWithHierarchy() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertTrue(ac.getMemberMethodCount() >= 2);
        AnnotatedMethod found = ac.findMethod("doSomething", new Class<?>[0]);
        assertNotNull(found);
        assertEquals(SubClass.class, found.getDeclaringClass());
        assertTrue(found.hasAnnotation(CustomAnn.class));

        AnnotatedMethod superM = ac.findMethod("superMethod", new Class<?>[0]);
        assertNotNull(superM);

        AnnotatedMethod notFound = ac.findMethod("nonExistent", new Class<?>[0]);
        assertNull(notFound);

        int count = 0;
        for (AnnotatedMethod m : ac.memberMethods()) {
            assertNotNull(m);
            count++;
        }
        assertEquals(ac.getMemberMethodCount(), count);
    }

    // Tests member method mix-ins
    @Test
    public void testMemberMethods_withMixIn_appliesMixInAnnotations() {
        _mixInResolver.addMixIn(SubClass.class, MixInForSubClass.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        AnnotatedMethod found = ac.findMethod("doSomething", new Class<?>[0]);
        assertNotNull(found);
        CustomAnn ann = found.getAnnotation(CustomAnn.class);
        assertNotNull(ann);
        assertEquals("mixInMethod", ann.value());
    }

    // Tests Object.class mix-in resolution
    @Test
    public void testMemberMethods_withObjectMixIn_augmentsObjectMethods() {
        _mixInResolver.addMixIn(Object.class, ObjectMixIn.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        AnnotatedMethod hashM = ac.findMethod("hashCode", new Class<?>[0]);
        assertNotNull(hashM);
        assertTrue(hashM.hasAnnotation(CustomAnn.class));
    }

    // Tests field resolution including super class fields and mix-in overrides
    @Test
    public void testFields_withHierarchyAndMixIns_resolvesFields() {
        _mixInResolver.addMixIn(SubClass.class, MixInForSubClass.class);
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertTrue(ac.getFieldCount() >= 2);
        boolean foundSubField = false;
        boolean foundSuperField = false;

        for (AnnotatedField f : ac.fields()) {
            if ("subField".equals(f.getName())) {
                foundSubField = true;
                assertTrue(f.hasAnnotation(JsonProperty.class));
            } else if ("superField".equals(f.getName())) {
                foundSuperField = true;
            }
        }
        assertTrue(foundSubField);
        assertTrue(foundSuperField);
    }

    // Tests handling with null AnnotationIntrospector
    @Test
    public void testNullAnnotationIntrospector_disablesAnnotationsWithoutErrors() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, null, null);
        assertFalse(ac.hasAnnotations());
        assertNull(ac.getAnnotation(CustomAnn.class));
        assertNotNull(ac.getDefaultConstructor());
        assertEquals(1, ac.getConstructors().size());
        assertEquals(2, ac.getStaticMethods().size());
        assertTrue(ac.getFieldCount() >= 2);
        assertTrue(ac.getMemberMethodCount() >= 2);
    }

    // Tests constructor of inner member class with implicit outer 'this' param
    @Test
    public void testInnerClassConstructor_resolvesParamAnnotationsCorrectly() {
        AnnotatedClass ac = AnnotatedClass.construct(InnerClassHolder.InnerClass.class, _introspector, _mixInResolver);
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        assertEquals(1, ctor.getParameterCount());
        assertNotNull(ctor.getParameter(0).getAnnotation(CustomAnn.class));
    }

    // Tests enum constructor parameter annotations handling extra synthetic arguments
    @Test
    public void testEnumConstructor_resolvesParamAnnotationsCorrectly() {
        AnnotatedClass ac = AnnotatedClass.construct(SampleEnum.class, _introspector, _mixInResolver);
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        assertEquals(1, ctor.getParameterCount());
        assertNotNull(ctor.getParameter(0).getAnnotation(CustomAnn.class));
    }
}
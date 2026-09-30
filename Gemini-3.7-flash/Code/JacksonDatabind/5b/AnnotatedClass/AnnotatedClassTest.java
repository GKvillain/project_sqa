package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnnotatedClassTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    public @interface TestAnn {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    public @interface OtherAnn {
        int priority() default 0;
    }

    @TestAnn("base")
    static class BaseClass {
        @TestAnn("baseField")
        public int baseField;

        @TestAnn("baseMethod")
        public void baseMethod() { }

        public void overriddenMethod() { }
    }

    interface InterfaceA {
        @TestAnn("interfaceMethod")
        void interfaceMethod();
    }

    @OtherAnn(priority = 1)
    static class SubClass extends BaseClass implements InterfaceA {
        @TestAnn("subField")
        public String subField;

        private int ignoredField;

        public SubClass() { }

        public SubClass(@TestAnn("param") String arg) { }

        @TestAnn("factory")
        public static SubClass create(String val) {
            return new SubClass(val);
        }

        public static void staticNonFactory() { }

        @Override
        public void interfaceMethod() { }

        @Override
        public void overriddenMethod() { }

        public void methodWithTwoParams(int a, String b) { }
    }

    enum TestEnum {
        A("first"),
        B("second");

        private final String desc;
        TestEnum(String desc) {
            this.desc = desc;
        }
    }

    class InnerClass {
        public InnerClass(@TestAnn("inner") String a) { }
    }

    abstract static class MixInForBase {
        @OtherAnn(priority = 99)
        public int baseField;

        @OtherAnn(priority = 10)
        public abstract void baseMethod();
    }

    abstract static class MixInForSub {
        public MixInForSub(@OtherAnn(priority = 5) String arg) { }

        @OtherAnn(priority = 20)
        public static SubClass create(String val) { return null; }
    }

    static class SimpleMixInResolver implements ClassIntrospector.MixInResolver {
        private final Map<Class<?>, Class<?>> _mixIns = new HashMap<Class<?>, Class<?>>();

        public void addMixIn(Class<?> target, Class<?> mixIn) {
            _mixIns.put(target, mixIn);
        }

        @Override
        public Class<?> findMixInClassFor(Class<?> cls) {
            return _mixIns.get(cls);
        }

        @Override
        public ClassIntrospector.MixInResolver copy() {
            return this;
        }
    }

    private JacksonAnnotationIntrospector _introspector;
    private SimpleMixInResolver _mixInResolver;

    @Before
    public void setUp() {
        _introspector = new JacksonAnnotationIntrospector();
        _mixInResolver = new SimpleMixInResolver();
    }

    // Tests construct with supertypes resolving annotations and member methods
    @Test
    public void testConstruct_withSuperTypes_resolvesAnnotationsAndMembers() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertEquals(SubClass.class, ac.getAnnotated());
        assertEquals(SubClass.class, ac.getRawType());
        assertEquals(SubClass.class, ac.getGenericType());
        assertEquals(SubClass.class.getName(), ac.getName());
        assertTrue(ac.hasAnnotations());
        assertTrue(ac.hasAnnotation(OtherAnn.class));
        assertNotNull(ac.getAnnotation(OtherAnn.class));
        assertNotNull(ac.getAnnotation(TestAnn.class));
        assertEquals("[AnnotedClass " + SubClass.class.getName() + "]", ac.toString());
    }

    // Tests constructWithoutSuperTypes excludes superclass annotations
    @Test
    public void testConstructWithoutSuperTypes_excludesSuperAnnotations() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SubClass.class, _introspector, _mixInResolver);

        assertNotNull(ac.getAnnotation(OtherAnn.class));
        assertNull(ac.getAnnotation(TestAnn.class));
        assertTrue(ac.hasAnnotations());
    }

    // Tests null AnnotationIntrospector leaves annotations empty
    @Test
    public void testConstruct_nullIntrospector_doesNotProcessAnnotations() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, null, _mixInResolver);

        assertFalse(ac.hasAnnotations());
        assertNull(ac.getAnnotation(OtherAnn.class));
        assertFalse(ac.annotations().iterator().hasNext());
        assertNotNull(ac.getDefaultConstructor());
        assertNotNull(ac.getConstructors());
    }

    // Tests withAnnotations creates a new instance with specified AnnotationMap
    @Test
    public void testWithAnnotations_returnsNewInstanceWithProvidedAnnotations() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);
        AnnotationMap map = new AnnotationMap();
        AnnotatedClass modified = ac.withAnnotations(map);

        assertNotNull(modified);
        assertEquals(ac.getAnnotated(), modified.getAnnotated());
        assertFalse(modified.hasAnnotations());
    }

    // Tests constructors and static creator methods resolution
    @Test
    public void testResolveCreators_findsDefaultAndSingleArgConstructorsAndFactory() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        AnnotatedConstructor defaultCtor = ac.getDefaultConstructor();
        assertNotNull(defaultCtor);
        assertEquals(0, defaultCtor.getParameterCount());

        assertEquals(1, ac.getConstructors().size());
        AnnotatedConstructor singleArgCtor = ac.getConstructors().get(0);
        assertEquals(1, singleArgCtor.getParameterCount());
        assertNotNull(singleArgCtor.getAnnotation(TestAnn.class));

        assertEquals(1, ac.getStaticMethods().size());
        AnnotatedMethod factory = ac.getStaticMethods().get(0);
        assertEquals("create", factory.getName());
        assertTrue(Modifier.isStatic(factory.getModifiers()));
    }

    // Tests non-static inner class constructor parameter count handling
    @Test
    public void testConstructConstructor_innerClass_handlesSyntheticThisParameter() {
        AnnotatedClass ac = AnnotatedClass.construct(InnerClass.class, _introspector, _mixInResolver);

        assertEquals(1, ac.getConstructors().size());
        AnnotatedConstructor ctor = ac.getConstructors().get(0);
        assertEquals(2, ctor.getParameterCount());
    }

    // Tests enum constructor parameter count handling
    @Test
    public void testConstructConstructor_enumType_handlesImplicitParameters() {
        AnnotatedClass ac = AnnotatedClass.construct(TestEnum.class, _introspector, _mixInResolver);

        assertFalse(ac.getConstructors().isEmpty());
        AnnotatedConstructor ctor = ac.getConstructors().get(0);
        assertEquals(3, ctor.getParameterCount());
    }

    // Tests member method resolution including inherited and interface methods
    @Test
    public void testResolveMemberMethods_includesInheritedAndInterfaceMethods() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertTrue(ac.getMemberMethodCount() > 0);
        AnnotatedMethod baseMethod = ac.findMethod("baseMethod", new Class<?>[0]);
        assertNotNull(baseMethod);
        assertNotNull(baseMethod.getAnnotation(TestAnn.class));

        AnnotatedMethod ifaceMethod = ac.findMethod("interfaceMethod", new Class<?>[0]);
        assertNotNull(ifaceMethod);

        AnnotatedMethod twoParamMethod = ac.findMethod("methodWithTwoParams", new Class<?>[]{int.class, String.class});
        assertNotNull(twoParamMethod);
        assertNotNull(ac.memberMethods());
    }

    // Tests field resolution including super class fields
    @Test
    public void testResolveFields_collectsDeclaredAndInheritedFields() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        assertTrue(ac.getFieldCount() >= 2);
        boolean foundBaseField = false;
        boolean foundSubField = false;
        for (AnnotatedField f : ac.fields()) {
            if ("baseField".equals(f.getName())) {
                foundBaseField = true;
                assertNotNull(f.getAnnotation(TestAnn.class));
            } else if ("subField".equals(f.getName())) {
                foundSubField = true;
                assertNotNull(f.getAnnotation(TestAnn.class));
            }
        }
        assertTrue(foundBaseField);
        assertTrue(foundSubField);
    }

    // Tests class, method, constructor and field mix-in overrides
    @Test
    public void testMixIns_overridesAnnotationsCorrectly() {
        _mixInResolver.addMixIn(BaseClass.class, MixInForBase.class);
        _mixInResolver.addMixIn(SubClass.class, MixInForSub.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, _introspector, _mixInResolver);

        AnnotatedMethod baseMethod = ac.findMethod("baseMethod", new Class<?>[0]);
        assertNotNull(baseMethod);
        assertNotNull(baseMethod.getAnnotation(OtherAnn.class));

        AnnotatedConstructor ctor = ac.getConstructors().get(0);
        assertNotNull(ctor.getParameterAnnotations(0));
        assertNotNull(ctor.getParameterAnnotations(0).get(OtherAnn.class));

        AnnotatedMethod factory = ac.getStaticMethods().get(0);
        assertNotNull(factory.getAnnotation(OtherAnn.class));

        for (AnnotatedField f : ac.fields()) {
            if ("baseField".equals(f.getName())) {
                assertNotNull(f.getAnnotation(OtherAnn.class));
            }
        }
    }

    // Tests Object.class mix-in resolution for hashCode/toString
    @Test
    public void testObjectMixIn_appliesToClassMethods() {
        abstract class ObjectMixIn {
            @TestAnn("hashCodeMixIn")
            @Override
            public abstract int hashCode();
        }

        _mixInResolver.addMixIn(Object.class, ObjectMixIn.class);
        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, _introspector, _mixInResolver);

        AnnotatedMethod hashMethod = ac.findMethod("hashCode", new Class<?>[0]);
        assertNotNull(hashMethod);
        assertNotNull(hashMethod.getAnnotation(TestAnn.class));
    }
}
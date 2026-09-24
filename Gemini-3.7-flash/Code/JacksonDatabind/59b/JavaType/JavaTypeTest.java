package com.fasterxml.jackson.databind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static org.junit.Assert.*;

public class JavaTypeTest {

    private static enum TestEnum {
        A, B
    }

    private static abstract class AbstractTestClass {
    }

    // Concrete dummy subclass to test JavaType base methods directly
    private static class DummyJavaType extends JavaType {
        private static final long serialVersionUID = 1L;

        public DummyJavaType(Class<?> raw) {
            super(raw, 0, null, null, false);
        }

        public DummyJavaType(Class<?> raw, int hash, Object valH, Object typeH, boolean asStatic) {
            super(raw, hash, valH, typeH, asStatic);
        }

        public DummyJavaType(DummyJavaType base) {
            super(base);
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new DummyJavaType(_class, _hash, _valueHandler, h, _asStatic);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new DummyJavaType(_class, _hash, h, _typeHandler, _asStatic);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return new DummyJavaType(_class, _hash, _valueHandler, _typeHandler, true);
        }

        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
            return this;
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public int containedTypeCount() {
            return 0;
        }

        @Override
        public JavaType containedType(int index) {
            return null;
        }

        @Deprecated
        @Override
        public String containedTypeName(int index) {
            return null;
        }

        @Override
        public TypeBindings getBindings() {
            return TypeBindings.emptyBindings();
        }

        @Override
        public JavaType findSuperType(Class<?> erasedTarget) {
            return null;
        }

        @Override
        public JavaType getSuperClass() {
            return null;
        }

        @Override
        public List<JavaType> getInterfaces() {
            return java.util.Collections.emptyList();
        }

        @Override
        public JavaType[] findTypeParameters(Class<?> expType) {
            return new JavaType[0];
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append(_class.getName());
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append(_class.getName());
            return sb;
        }

        @Override
        public String toString() {
            return "[DummyJavaType " + _class.getName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            DummyJavaType other = (DummyJavaType) o;
            return other._class == _class;
        }
    }

    // Tests getRawClass and hasRawClass methods
    @Test
    public void testGetRawClassAndHasRawClass_returnsExpected() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Integer.class));
    }

    // Tests isTypeOrSubTypeOf method
    @Test
    public void testIsTypeOrSubTypeOf_variousClasses_returnsCorrectHierarchy() {
        JavaType type = TypeFactory.defaultInstance().constructType(ArrayList.class);
        assertTrue(type.isTypeOrSubTypeOf(ArrayList.class));
        assertTrue(type.isTypeOrSubTypeOf(List.class));
        assertTrue(type.isTypeOrSubTypeOf(Object.class));
        assertFalse(type.isTypeOrSubTypeOf(Map.class));
    }

    // Tests isAbstract and isConcrete properties
    @Test
    public void testIsAbstractAndIsConcrete_concreteClass_returnsTrueForConcrete() {
        JavaType concreteType = TypeFactory.defaultInstance().constructType(String.class);
        assertFalse(concreteType.isAbstract());
        assertTrue(concreteType.isConcrete());

        JavaType abstractType = TypeFactory.defaultInstance().constructType(AbstractTestClass.class);
        assertTrue(abstractType.isAbstract());
        assertFalse(abstractType.isConcrete());

        JavaType interfaceType = TypeFactory.defaultInstance().constructType(List.class);
        assertTrue(interfaceType.isAbstract());
        assertFalse(interfaceType.isConcrete());

        JavaType primitiveType = TypeFactory.defaultInstance().constructType(int.class);
        assertTrue(primitiveType.isConcrete());
    }

    // Tests isPrimitive, isFinal, isInterface, isEnumType, and isThrowable
    @Test
    public void testTypeModifiers_differentTypes_returnsExpectedFlags() {
        JavaType primType = TypeFactory.defaultInstance().constructType(int.class);
        assertTrue(primType.isPrimitive());
        assertFalse(primType.isFinal());

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        assertFalse(stringType.isPrimitive());
        assertTrue(stringType.isFinal());
        assertFalse(stringType.isInterface());
        assertFalse(stringType.isEnumType());
        assertFalse(stringType.isThrowable());

        JavaType interfaceType = TypeFactory.defaultInstance().constructType(Serializable.class);
        assertTrue(interfaceType.isInterface());

        JavaType enumType = TypeFactory.defaultInstance().constructType(TestEnum.class);
        assertTrue(enumType.isEnumType());

        JavaType throwableType = TypeFactory.defaultInstance().constructType(IllegalArgumentException.class);
        assertTrue(throwableType.isThrowable());
    }

    // Tests isJavaLangObject
    @Test
    public void testIsJavaLangObject_objectAndNonObject_returnsExpected() {
        JavaType objType = TypeFactory.defaultInstance().constructType(Object.class);
        assertTrue(objType.isJavaLangObject());

        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        assertFalse(strType.isJavaLangObject());
    }

    // Tests default implementations of isArrayType, isCollectionLikeType, isMapLikeType
    @Test
    public void testBaseTypeDefaults_returnsFalseAndNulls() {
        DummyJavaType dummy = new DummyDummyTypeSubclass(Object.class);
        assertFalse(dummy.isArrayType());
        assertFalse(dummy.isCollectionLikeType());
        assertFalse(dummy.isMapLikeType());
        assertNull(dummy.getKeyType());
        assertNull(dummy.getContentType());
        assertNull(dummy.getReferencedType());
        assertNull(dummy.getParameterSource());
        assertNull(dummy.getContentValueHandler());
        assertNull(dummy.getContentTypeHandler());
        assertTrue(dummy.hasContentType());
    }

    private static class DummyDummyTypeSubclass extends DummyJavaType {
        private static final long serialVersionUID = 1L;

        public DummyDummyTypeSubclass(Class<?> raw) {
            super(raw);
        }
    }

    // Tests value and type handler assignments and checks
    @Test
    public void testHandlers_assignmentAndCheck_returnsCorrectHandlers() {
        DummyJavaType dummy = new DummyJavaType(String.class);
        assertFalse(dummy.hasValueHandler());
        assertFalse(dummy.hasHandlers());
        assertNull(dummy.getValueHandler());
        assertNull(dummy.getTypeHandler());

        JavaType withVal = dummy.withValueHandler("valHandler");
        assertTrue(withVal.hasValueHandler());
        assertTrue(withVal.hasHandlers());
        assertEquals("valHandler", withVal.getValueHandler());
        assertNull(withVal.getTypeHandler());

        JavaType withType = dummy.withTypeHandler("typeHandler");
        assertFalse(withType.hasValueHandler());
        assertTrue(withType.hasHandlers());
        assertEquals("typeHandler", withType.getTypeHandler());
    }

    // Tests static typing flag
    @Test
    public void testUseStaticType_defaultAndModified_returnsCorrectFlag() {
        DummyJavaType dummy = new DummyJavaType(String.class);
        assertFalse(dummy.useStaticType());

        JavaType staticType = dummy.withStaticTyping();
        assertTrue(staticType.useStaticType());
    }

    // Tests generic signatures generation
    @Test
    public void testSignatures_returnsExpectedString() {
        DummyJavaType dummy = new DummyJavaType(String.class);
        assertEquals("java.lang.String", dummy.getGenericSignature());
        assertEquals("java.lang.String", dummy.getErasedSignature());
    }

    // Tests containedTypeOrUnknown behavior
    @Test
    public void testContainedTypeOrUnknown_indexOutOfBounds_returnsUnknownType() {
        DummyJavaType dummy = new DummyJavaType(String.class);
        JavaType unknown = dummy.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        assertEquals(String.class, listType.containedTypeOrUnknown(0).getRawClass());
    }

    // Tests hasGenericTypes method
    @Test
    public void testHasGenericTypes_withAndWithoutGenericParams_returnsExpected() {
        JavaType simpleType = TypeFactory.defaultInstance().constructType(String.class);
        assertFalse(simpleType.hasGenericTypes());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        assertTrue(listType.hasGenericTypes());
    }

    // Tests forcedNarrowBy when subclass is identical
    @Test
    public void testForcedNarrowBy_sameClass_returnsThis() {
        DummyJavaType dummy = new DummyJavaType(Number.class);
        JavaType narrowed = dummy.forcedNarrowBy(Number.class);
        assertSame(dummy, narrowed);
    }

    // Tests forcedNarrowBy when narrowing to a subclass preserves handlers
    @Test
    public void testForcedNarrowBy_differentSubclass_preservesHandlers() {
        DummyJavaType dummy = new DummyJavaType(Number.class, 0, "valH", "typeH", false);
        JavaType narrowed = dummy.forcedNarrowBy(Integer.class);
        assertNotSame(dummy, narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals("valH", narrowed.getValueHandler());
        assertEquals("typeH", narrowed.getTypeHandler());
    }

    // Tests hashCode and copy constructor
    @Test
    public void testHashCodeAndCopyConstructor_createsEqualHashCode() {
        DummyJavaType dummy1 = new DummyJavaType(String.class, 123, "v", "t", true);
        DummyJavaType dummy2 = new DummyJavaType(dummy1);
        assertEquals(dummy1.hashCode(), dummy2.hashCode());
        assertEquals(dummy1.getRawClass(), dummy2.getRawClass());
        assertEquals(dummy1.getValueHandler(), dummy2.getValueHandler());
        assertEquals(dummy1.getTypeHandler(), dummy2.getTypeHandler());
        assertEquals(dummy1.useStaticType(), dummy2.useStaticType());
    }
}
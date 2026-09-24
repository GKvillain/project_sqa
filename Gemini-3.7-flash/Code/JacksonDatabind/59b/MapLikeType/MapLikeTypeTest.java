package com.fasterxml.jackson.databind.type;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class MapLikeTypeTest {

    private TypeFactory _typeFactory;
    private JavaType _keyType;
    private JavaType _valueType;
    private MapLikeType _mapLikeType;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _keyType = _typeFactory.constructType(String.class);
        _valueType = _typeFactory.constructType(Integer.class);
        _mapLikeType = MapLikeType.construct(Map.class, _keyType, _valueType);
    }

    // Tests basic construction and property getters
    @Test
    public void testConstruct_validTypes_returnsConfiguredInstance() {
        assertNotNull(_mapLikeType);
        assertEquals(Map.class, _mapLikeType.getRawClass());
        assertEquals(_keyType, _mapLikeType.getKeyType());
        assertEquals(_valueType, _mapLikeType.getContentType());
        assertTrue(_mapLikeType.isContainerType());
        assertTrue(_mapLikeType.isMapLikeType());
        assertTrue(_mapLikeType.isTrueMapType());
    }

    // Tests isTrueMapType on non-Map class
    @Test
    public void testIsTrueMapType_nonMapClass_returnsFalse() {
        MapLikeType nonMap = MapLikeType.construct(String.class, _keyType, _valueType);
        assertFalse(nonMap.isTrueMapType());
    }

    // Tests upgradeFrom with TypeBase instance
    @Test
    public void testUpgradeFrom_typeBase_returnsMapLikeType() {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        MapLikeType upgraded = MapLikeType.upgradeFrom(baseType, _keyType, _valueType);

        assertNotNull(upgraded);
        assertEquals(Object.class, upgraded.getRawClass());
        assertEquals(_keyType, upgraded.getKeyType());
        assertEquals(_valueType, upgraded.getContentType());
    }

    // Tests upgradeFrom with non-TypeBase instance expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFrom_nonTypeBase_throwsIllegalArgumentException() {
        JavaType customType = new JavaType(Object.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;

            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withStaticTyping() { return this; }
            @Override
            public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override
            protected String buildCanonicalName() { return ""; }
            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override
            public JavaType _narrow(Class<?> subclass) { return this; }
            @Override
            public JavaType getKeyType() { return null; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public boolean isContainerType() { return false; }
            @Override
            public String toString() { return ""; }
            @Override
            public boolean equals(Object o) { return o == this; }
        };

        MapLikeType.upgradeFrom(customType, _keyType, _valueType);
    }

    // Tests withKeyType with same and new key types
    @Test
    public void testWithKeyType_sameAndDifferent_returnsExpectedInstances() {
        assertSame(_mapLikeType, _mapLikeType.withKeyType(_keyType));

        JavaType newKeyType = _typeFactory.constructType(Long.class);
        MapLikeType modified = _mapLikeType.withKeyType(newKeyType);

        assertNotSame(_mapLikeType, modified);
        assertEquals(newKeyType, modified.getKeyType());
        assertEquals(_valueType, modified.getContentType());
    }

    // Tests withContentType with same and new content types
    @Test
    public void testWithContentType_sameAndDifferent_returnsExpectedInstances() {
        assertSame(_mapLikeType, _mapLikeType.withContentType(_valueType));

        JavaType newContentType = _typeFactory.constructType(Double.class);
        JavaType modified = _mapLikeType.withContentType(newContentType);

        assertNotSame(_mapLikeType, modified);
        assertEquals(_keyType, ((MapLikeType) modified).getKeyType());
        assertEquals(newContentType, modified.getContentType());
    }

    // Tests handler assignments and hasHandlers condition
    @Test
    public void testHandlers_setAndRetrieve_handlersPresent() {
        assertFalse(_mapLikeType.hasHandlers());
        assertNull(_mapLikeType.getContentValueHandler());
        assertNull(_mapLikeType.getContentTypeHandler());

        Object typeHandler = "typeHandler";
        Object valHandler = "valHandler";
        Object keyTypeHandler = "keyTypeHandler";
        Object keyValHandler = "keyValHandler";

        MapLikeType withHandlers = _mapLikeType
                .withTypeHandler(typeHandler)
                .withValueHandler(valHandler)
                .withContentTypeHandler(typeHandler)
                .withContentValueHandler(valHandler)
                .withKeyTypeHandler(keyTypeHandler)
                .withKeyValueHandler(keyValHandler);

        assertNotNull(withHandlers.getTypeHandler());
        assertNotNull(withHandlers.getValueHandler());
        assertEquals(valHandler, withHandlers.getContentValueHandler());
        assertEquals(typeHandler, withHandlers.getContentTypeHandler());
        assertTrue(withHandlers.hasHandlers());
    }

    // Tests withStaticTyping when already static and when not static
    @Test
    public void testWithStaticTyping_stateTransitions_returnsExpectedInstance() {
        assertFalse(_mapLikeType.useStaticType());

        MapLikeType staticType = _mapLikeType.withStaticTyping();
        assertTrue(staticType.useStaticType());
        assertSame(staticType, staticType.withStaticTyping());
    }

    // Tests refine method
    @Test
    public void testRefine_validParameters_returnsRefinedInstance() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, _keyType, _valueType);
        JavaType superClass = _typeFactory.constructType(Object.class);
        JavaType refined = _mapLikeType.refine(HashMap.class, bindings, superClass, new JavaType[0]);

        assertNotNull(refined);
        assertEquals(HashMap.class, refined.getRawClass());
        assertEquals(_keyType, refined.getKeyType());
        assertEquals(_valueType, refined.getContentType());
    }

    // Tests narrow method
    @Test
    public void testNarrow_subclass_returnsNarrowedMapLikeType() {
        JavaType narrowed = _mapLikeType._narrow(HashMap.class);

        assertNotNull(narrowed);
        assertEquals(HashMap.class, narrowed.getRawClass());
        assertEquals(_keyType, narrowed.getKeyType());
        assertEquals(_valueType, narrowed.getContentType());
    }

    // Tests canonical name and signatures
    @Test
    public void testCanonicalNameAndSignatures_validMap_matchesExpectedFormats() {
        String canonical = _mapLikeType.toCanonical();
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", canonical);

        StringBuilder erasedSb = new StringBuilder();
        _mapLikeType.getErasedSignature(erasedSb);
        assertEquals("Ljava/util/Map;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        _mapLikeType.getGenericSignature(genericSb);
        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;", genericSb.toString());
    }

    // Tests equals, hashCode, and toString methods
    @Test
    public void testEqualsAndToString_variousCases_returnsExpectedResults() {
        assertTrue(_mapLikeType.equals(_mapLikeType));
        assertFalse(_mapLikeType.equals(null));
        assertFalse(_mapLikeType.equals("string"));

        MapLikeType equalType = MapLikeType.construct(Map.class, _keyType, _valueType);
        assertTrue(_mapLikeType.equals(equalType));
        assertEquals(_mapLikeType.hashCode(), equalType.hashCode());

        MapLikeType diffKey = MapLikeType.construct(Map.class, _typeFactory.constructType(Long.class), _valueType);
        assertFalse(_mapLikeType.equals(diffKey));

        MapLikeType diffVal = MapLikeType.construct(Map.class, _keyType, _typeFactory.constructType(Long.class));
        assertFalse(_mapLikeType.equals(diffVal));

        MapLikeType diffClass = MapLikeType.construct(HashMap.class, _keyType, _valueType);
        assertFalse(_mapLikeType.equals(diffClass));

        String str = _mapLikeType.toString();
        assertTrue(str.contains("java.util.Map"));
        assertTrue(str.contains("java.lang.String"));
        assertTrue(str.contains("java.lang.Integer"));
    }
}
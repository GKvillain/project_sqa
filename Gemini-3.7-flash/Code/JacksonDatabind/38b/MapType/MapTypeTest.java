package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MapTypeTest {

    private JavaType stringType;
    private JavaType integerType;
    private JavaType objectType;
    private MapType mapType;

    @Before
    public void setUp() {
        stringType = SimpleType.constructUnsafe(String.class);
        integerType = SimpleType.constructUnsafe(Integer.class);
        objectType = SimpleType.constructUnsafe(Object.class);
        mapType = MapType.construct(Map.class, stringType, integerType);
    }

    // Tests 3-argument construct method
    @Test
    public void testConstruct_threeArgs_createsValidMapType() {
        MapType type = MapType.construct(Map.class, stringType, integerType);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(stringType, type.getKeyType());
        assertEquals(integerType, type.getContentType());
        assertFalse(type.useStaticType());
    }

    // Tests 6-argument construct method
    @Test
    public void testConstruct_sixArgs_createsValidMapType() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{stringType, integerType});
        MapType type = MapType.construct(Map.class, bindings, objectType, null, stringType, integerType);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(stringType, type.getKeyType());
        assertEquals(integerType, type.getContentType());
        assertEquals(objectType, type.getSuperClass());
        assertEquals(bindings, type.getBindings());
    }

    // Tests withTypeHandler method
    @Test
    public void testWithTypeHandler_validHandler_setsTypeHandler() {
        Object handler = "customTypeHandler";
        MapType result = mapType.withTypeHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getTypeHandler());
        assertEquals(mapType.getKeyType(), result.getKeyType());
        assertEquals(mapType.getContentType(), result.getContentType());
    }

    // Tests withContentTypeHandler method
    @Test
    public void testWithContentTypeHandler_validHandler_setsContentTypeHandler() {
        Object handler = "customContentTypeHandler";
        MapType result = mapType.withContentTypeHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getContentType().getTypeHandler());
    }

    // Tests withValueHandler method
    @Test
    public void testWithValueHandler_validHandler_setsValueHandler() {
        Object handler = "customValueHandler";
        MapType result = mapType.withValueHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getValueHandler());
    }

    // Tests withContentValueHandler method
    @Test
    public void testWithContentValueHandler_validHandler_setsContentValueHandler() {
        Object handler = "customContentValueHandler";
        MapType result = mapType.withContentValueHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getContentType().getValueHandler());
    }

    // Tests withKeyTypeHandler method
    @Test
    public void testWithKeyTypeHandler_validHandler_setsKeyTypeHandler() {
        Object handler = "customKeyTypeHandler";
        MapType result = mapType.withKeyTypeHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getKeyType().getTypeHandler());
    }

    // Tests withKeyValueHandler method
    @Test
    public void testWithKeyValueHandler_validHandler_setsKeyValueHandler() {
        Object handler = "customKeyValueHandler";
        MapType result = mapType.withKeyValueHandler(handler);

        assertNotSame(mapType, result);
        assertEquals(handler, result.getKeyType().getValueHandler());
    }

    // Tests withStaticTyping when false branch of _asStatic is executed
    @Test
    public void testWithStaticTyping_whenNotStatic_returnsStaticInstance() {
        assertFalse(mapType.useStaticType());
        MapType result = mapType.withStaticTyping();

        assertNotSame(mapType, result);
        assertTrue(result.useStaticType());
        assertTrue(result.getKeyType().useStaticType());
        assertTrue(result.getContentType().useStaticType());
    }

    // Tests withStaticTyping when true branch of _asStatic is executed
    @Test
    public void testWithStaticTyping_whenAlreadyStatic_returnsSameInstance() {
        MapType staticMapType = mapType.withStaticTyping();
        MapType result = staticMapType.withStaticTyping();

        assertSame(staticMapType, result);
    }

    // Tests withContentType when new content type is supplied
    @Test
    public void testWithContentType_differentType_returnsNewInstance() {
        JavaType newContentType = SimpleType.constructUnsafe(Double.class);
        MapType result = (MapType) mapType.withContentType(newContentType);

        assertNotSame(mapType, result);
        assertEquals(newContentType, result.getContentType());
        assertEquals(mapType.getKeyType(), result.getKeyType());
    }

    // Tests withContentType when same content type is supplied
    @Test
    public void testWithContentType_sameType_returnsSameInstance() {
        JavaType result = mapType.withContentType(integerType);

        assertSame(mapType, result);
    }

    // Tests withKeyType when new key type is supplied
    @Test
    public void testWithKeyType_differentType_returnsNewInstance() {
        JavaType newKeyType = SimpleType.constructUnsafe(Long.class);
        MapType result = mapType.withKeyType(newKeyType);

        assertNotSame(mapType, result);
        assertEquals(newKeyType, result.getKeyType());
        assertEquals(mapType.getContentType(), result.getContentType());
    }

    // Tests withKeyType when same key type is supplied
    @Test
    public void testWithKeyType_sameType_returnsSameInstance() {
        MapType result = mapType.withKeyType(stringType);

        assertSame(mapType, result);
    }

    // Tests refine method
    @Test
    public void testRefine_validArguments_returnsRefinedMapType() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{stringType, integerType});
        JavaType[] superInterfaces = new JavaType[]{mapType};
        JavaType refined = mapType.refine(HashMap.class, bindings, objectType, superInterfaces);

        assertNotNull(refined);
        assertTrue(refined instanceof MapType);
        assertEquals(HashMap.class, refined.getRawClass());
        assertEquals(bindings, refined.getBindings());
        assertEquals(objectType, refined.getSuperClass());
        assertEquals(stringType, ((MapType) refined).getKeyType());
        assertEquals(integerType, refined.getContentType());
    }

    // Tests _narrow method
    @Test
    public void testNarrow_subclass_returnsNarrowedMapType() {
        JavaType narrowed = mapType._narrow(HashMap.class);

        assertNotNull(narrowed);
        assertTrue(narrowed instanceof MapType);
        assertEquals(HashMap.class, narrowed.getRawClass());
        assertEquals(mapType.getKeyType(), ((MapType) narrowed).getKeyType());
        assertEquals(mapType.getContentType(), narrowed.getContentType());
    }

    // Tests toString method
    @Test
    public void testToString_validMapType_returnsExpectedFormat() {
        String result = mapType.toString();
        assertNotNull(result);
        assertTrue(result.contains("[map type; class java.util.Map"));
        assertTrue(result.contains(stringType.toString()));
        assertTrue(result.contains(integerType.toString()));
    }

    // Additional tests for complete branch coverage

    @Test
    public void testConstruct_withSuperInterfaces_createsValidMapType() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{stringType, integerType});
        JavaType[] superInterfaces = new JavaType[]{mapType};
        MapType type = MapType.construct(HashMap.class, bindings, objectType, superInterfaces, stringType, integerType);

        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertNotNull(type.getInterfaces());
        assertEquals(1, type.getInterfaces().length);
        assertEquals(mapType, type.getInterfaces()[0]);
    }

    @Test
    public void testWithTypeHandler_sameHandler_returnsSameInstance() {
        Object handler = "customTypeHandler";
        MapType withHandler = mapType.withTypeHandler(handler);
        MapType result = withHandler.withTypeHandler(handler);

        assertSame(withHandler, result);
    }

    @Test
    public void testWithValueHandler_sameHandler_returnsSameInstance() {
        Object handler = "customValueHandler";
        MapType withHandler = mapType.withValueHandler(handler);
        MapType result = withHandler.withValueHandler(handler);

        assertSame(withHandler, result);
    }
}
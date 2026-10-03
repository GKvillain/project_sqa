package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeBindings;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public class MapTypeTest {

    private TypeFactory tf;
    private JavaType stringType;
    private JavaType intType;
    private JavaType longType;

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
        stringType = tf.constructType(String.class);
        intType = tf.constructType(Integer.class);
        longType = tf.constructType(Long.class);
    }

    // Helper to build a default MapType using the public (non‑deprecated) constructor
    private MapType createDefaultMapType() {
        return MapType.construct(
                HashMap.class,
                TypeBindings.emptyBindings(),
                tf.constructType(Object.class),
                new JavaType[0],
                stringType,
                intType
        );
    }

    // Helper to build a MapType using the deprecated constructor (simpler, no bindings)
    private MapType createDeprecatedMapType() {
        return MapType.construct(HashMap.class, stringType, intType);
    }

    // ---------- Normal construction ----------

    @Test
    // Tests that the public static construct method returns a valid MapType
    public void testConstruct_nonDeprecated_returnsMapType() {
        MapType mapType = createDefaultMapType();
        assertNotNull(mapType);
        assertTrue(mapType instanceof MapType);
        assertEquals(stringType, mapType.getKeyType());
        assertEquals(intType, mapType.getContentType());
    }

    @Test
    // Tests that the deprecated static construct method returns a valid MapType
    public void testConstruct_deprecated_returnsMapType() {
        MapType mapType = createDeprecatedMapType();
        assertNotNull(mapType);
        assertTrue(mapType instanceof MapType);
        assertEquals(stringType, mapType.getKeyType());
        assertEquals(intType, mapType.getContentType());
    }

    // ---------- withTypeHandler ----------

    @Test
    // Tests withTypeHandler: returns a new instance with a different type handler
    public void testWithTypeHandler_newHandler_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withTypeHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
        // toString should contain original key/value, handler not reflected in toString (not required)
    }

    @Test
    // Tests withTypeHandler with null handler
    public void testWithTypeHandler_nullHandler_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        MapType modified = original.withTypeHandler(null);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withContentTypeHandler ----------

    @Test
    // Tests withContentTypeHandler: returns a new instance with handler on content type
    public void testWithContentTypeHandler_newHandler_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withContentTypeHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withValueHandler ----------

    @Test
    // Tests withValueHandler: returns a new instance with different value handler
    public void testWithValueHandler_newHandler_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withValueHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withContentValueHandler ----------

    @Test
    // Tests withContentValueHandler: returns a new instance with handler on content value
    public void testWithContentValueHandler_newHandler_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withContentValueHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withStaticTyping branch ----------

    @Test
    // Tests the true branch: calling withStaticTyping on an already static instance returns the same object
    public void testWithStaticTyping_alreadyStatic_returnsSame() {
        MapType nonStatic = createDeprecatedMapType();
        MapType staticMap = nonStatic.withStaticTyping(); // becomes static
        MapType same = staticMap.withStaticTyping();      // already static -> return this
        assertSame(staticMap, same);
    }

    @Test
    // Tests the false branch: calling withStaticTyping on a non‑static instance returns a new instance
    public void testWithStaticTyping_notStatic_returnsNewInstance() {
        MapType original = createDeprecatedMapType();
        MapType modified = original.withStaticTyping();
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withContentType branch ----------

    @Test
    // Tests the true branch: same content type returns this
    public void testWithContentType_sameType_returnsSame() {
        MapType original = createDefaultMapType(); // content type is intType
        MapType same = original.withContentType(intType);
        assertSame(original, same);
    }

    @Test
    // Tests the false branch: different content type returns new
    public void testWithContentType_differentType_returnsNew() {
        MapType original = createDefaultMapType(); // content type is intType
        MapType modified = original.withContentType(longType);
        assertNotNull(modified);
        assertNotSame(original, modified);
        assertEquals(longType, modified.getContentType());
    }

    // ---------- withKeyType branch ----------

    @Test
    // Tests the true branch: same key type returns this
    public void testWithKeyType_sameKey_returnsSame() {
        MapType original = createDefaultMapType(); // key type is stringType
        MapType same = original.withKeyType(stringType);
        assertSame(original, same);
    }

    @Test
    // Tests the false branch: different key type returns new
    public void testWithKeyType_differentKey_returnsNew() {
        MapType original = createDefaultMapType(); // key type is stringType
        JavaType differentKey = tf.constructType(Integer.class);
        MapType modified = original.withKeyType(differentKey);
        assertNotNull(modified);
        assertNotSame(original, modified);
        assertEquals(differentKey, modified.getKeyType());
    }

    // ---------- refine ----------

    @Test
    // Tests refine: returns a new MapType with the given rawType and bindings
    public void testRefine_returnsNew() {
        MapType original = createDefaultMapType();
        JavaType refined = original.refine(
                HashMap.class,
                TypeBindings.emptyBindings(),
                tf.constructType(Object.class),
                new JavaType[0]
        );
        assertNotNull(refined);
        assertTrue(refined instanceof MapType);
        // Should have same key and value types
        assertEquals(stringType, refined.getKeyType());
        assertEquals(intType, refined.getContentType());
    }

    // ---------- withKeyTypeHandler ----------

    @Test
    // Tests withKeyTypeHandler: returns a new instance with a type handler on key type
    public void testWithKeyTypeHandler_newHandler_returnsNew() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withKeyTypeHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- withKeyValueHandler ----------

    @Test
    // Tests withKeyValueHandler: returns a new instance with a value handler on key type
    public void testWithKeyValueHandler_newHandler_returnsNew() {
        MapType original = createDeprecatedMapType();
        Object handler = new Object();
        MapType modified = original.withKeyValueHandler(handler);
        assertNotNull(modified);
        assertNotSame(original, modified);
    }

    // ---------- toString ----------

    @Test
    // Tests toString returns the expected format
    public void testToString_returnsCorrectFormat() {
        MapType mapType = createDeprecatedMapType(); // HashMap, String -> Integer
        String expected = "[map type; class java.util.HashMap, " + stringType + " -> " + intType + "]";
        assertEquals(expected, mapType.toString());
    }

    // ========== New test cases for uncovered coverage ==========

    @Test
    // Tests isContainerType returns true for MapType
    public void testIsContainerType() {
        MapType mapType = createDeprecatedMapType();
        assertTrue(mapType.isContainerType());
    }

    @Test
    // Tests containedTypeCount returns 2 (key and value)
    public void testContainedTypeCount() {
        MapType mapType = createDeprecatedMapType();
        assertEquals(2, mapType.containedTypeCount());
    }

    @Test
    // Tests containedType returns correct types for indices 0 and 1, and null for out-of-range
    public void testContainedType() {
        MapType mapType = createDeprecatedMapType();
        assertEquals(stringType, mapType.containedType(0));
        assertEquals(intType, mapType.containedType(1));
        assertNull(mapType.containedType(2));
    }

    @Test
    // Tests getGenericSignature returns a non-null signature containing relevant class names
    public void testGetGenericSignature() {
        MapType mapType = createDeprecatedMapType();
        String sig = mapType.getGenericSignature();
        assertNotNull(sig);
        // Should contain type parameter names (String, Integer) and the raw class (HashMap)
        assertTrue(sig.contains("String"));
        assertTrue(sig.contains("Integer"));
        assertTrue(sig.contains("HashMap"));
    }

    @Test
    // Tests getErasedSignature returns a non-null signature containing the raw class name
    public void testGetErasedSignature() {
        MapType mapType = createDeprecatedMapType();
        String sig = mapType.getErasedSignature();
        assertNotNull(sig);
        assertTrue(sig.contains("HashMap"));
    }

    @Test
    // Tests equals and hashCode for MapType instances
    public void testEqualsAndHashCode() {
        MapType mapType1 = createDeprecatedMapType();
        // Same configuration should be equal
        MapType mapType2 = MapType.construct(HashMap.class, stringType, intType);
        assertEquals(mapType1, mapType2);
        assertEquals(mapType1.hashCode(), mapType2.hashCode());

        // Different content type (long instead of Integer) should not be equal
        MapType mapType3 = MapType.construct(HashMap.class, stringType, longType);
        assertNotEquals(mapType1, mapType3);

        // Reflexivity
        assertEquals(mapType1, mapType1);

        // Not equals to null
        assertFalse(mapType1.equals(null));

        // Not equals to a different object type
        assertFalse(mapType1.equals("string"));
    }

    @Test
    // Tests isConcrete returns true for a concrete Map implementation (HashMap)
    public void testIsConcrete() {
        MapType mapType = createDeprecatedMapType();
        assertTrue(mapType.isConcrete());
    }

    @Test
    // Tests isAbstract returns false for a concrete Map implementation (HashMap)
    public void testIsAbstract() {
        MapType mapType = createDeprecatedMapType();
        assertFalse(mapType.isAbstract());
    }

    @Test
    // Tests canHaveSubtypes returns true (Map types generally can have subclasses)
    public void testCanHaveSubtypes() {
        MapType mapType = createDeprecatedMapType();
        assertTrue(mapType.canHaveSubtypes());
    }
}
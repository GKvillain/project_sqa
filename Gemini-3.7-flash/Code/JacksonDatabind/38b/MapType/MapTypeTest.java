package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class MapTypeTest {

    private JavaType stringType;
    private JavaType integerType;
    private JavaType booleanType;
    private MapType baseMapType;

    @Before
    public void setUp() {
        TypeFactory tf = TypeFactory.defaultInstance();
        stringType = tf.constructType(String.class);
        integerType = tf.constructType(Integer.class);
        booleanType = tf.constructType(Boolean.class);
        baseMapType = MapType.construct(HashMap.class, TypeBindings.emptyBindings(), null, null, stringType, integerType);
    }

    // Tests MapType construction with full arguments
    @Test
    public void testConstruct_withFullParameters_createsValidInstance() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{stringType, integerType});
        MapType mapType = MapType.construct(Map.class, bindings, null, null, stringType, integerType);

        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(stringType, mapType.getKeyType());
        assertEquals(integerType, mapType.getContentType());
        assertFalse(mapType.useStaticType());
    }

    // Tests deprecated MapType construct method
    @SuppressWarnings("deprecation")
    @Test
    public void testConstruct_deprecatedMethod_createsValidInstance() {
        MapType mapType = MapType.construct(HashMap.class, stringType, integerType);

        assertNotNull(mapType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(stringType, mapType.getKeyType());
        assertEquals(integerType, mapType.getContentType());
    }

    // Tests setting type handler on the MapType
    @Test
    public void testWithTypeHandler_customHandler_returnsNewInstanceWithHandler() {
        String handler = "customTypeHandler";
        MapType result = baseMapType.withTypeHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getTypeHandler());
        assertNull(baseMapType.getTypeHandler());
    }

    // Tests setting content type handler on the value type
    @Test
    public void testWithContentTypeHandler_customHandler_setsHandlerOnValueType() {
        String handler = "customContentTypeHandler";
        MapType result = baseMapType.withContentTypeHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getContentType().getTypeHandler());
        assertNull(baseMapType.getContentType().getTypeHandler());
    }

    // Tests setting value handler on the MapType
    @Test
    public void testWithValueHandler_customHandler_returnsNewInstanceWithValueHandler() {
        String handler = "customValueHandler";
        MapType result = baseMapType.withValueHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getValueHandler());
        assertNull(baseMapType.getValueHandler());
    }

    // Tests setting content value handler on the value type
    @Test
    public void testWithContentValueHandler_customHandler_setsHandlerOnValueType() {
        String handler = "customContentValueHandler";
        MapType result = baseMapType.withContentValueHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getContentType().getValueHandler());
        assertNull(baseMapType.getContentType().getValueHandler());
    }

    // Tests setting key type handler
    @Test
    public void testWithKeyTypeHandler_customHandler_setsHandlerOnKeyType() {
        String handler = "customKeyTypeHandler";
        MapType result = baseMapType.withKeyTypeHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getKeyType().getTypeHandler());
        assertNull(baseMapType.getKeyType().getTypeHandler());
    }

    // Tests setting key value handler
    @Test
    public void testWithKeyValueHandler_customHandler_setsHandlerOnKeyType() {
        String handler = "customKeyValueHandler";
        MapType result = baseMapType.withKeyValueHandler(handler);

        assertNotSame(baseMapType, result);
        assertEquals(handler, result.getKeyType().getValueHandler());
        assertNull(baseMapType.getKeyType().getValueHandler());
    }

    // Tests withStaticTyping when instance is not yet static
    @Test
    public void testWithStaticTyping_nonStatic_returnsStaticInstance() {
        assertFalse(baseMapType.useStaticType());
        MapType staticMapType = baseMapType.withStaticTyping();

        assertNotSame(baseMapType, staticMapType);
        assertTrue(staticMapType.useStaticType());
    }

    // Tests withStaticTyping when instance is already static (branch coverage)
    @Test
    public void testWithStaticTyping_alreadyStatic_returnsSameInstance() {
        MapType staticMapType = baseMapType.withStaticTyping();
        MapType sameType = staticMapType.withStaticTyping();

        assertSame(staticMapType, sameType);
    }

    // Tests withContentType with identical content type (branch coverage)
    @Test
    public void testWithContentType_sameType_returnsSameInstance() {
        JavaType result = baseMapType.withContentType(integerType);

        assertSame(baseMapType, result);
    }

    // Tests withContentType with different content type (branch coverage)
    @Test
    public void testWithContentType_differentType_returnsNewInstance() {
        JavaType result = baseMapType.withContentType(booleanType);

        assertNotSame(baseMapType, result);
        assertEquals(booleanType, result.getContentType());
        assertEquals(stringType, result.getKeyType());
    }

    // Tests withKeyType with identical key type (branch coverage)
    @Test
    public void testWithKeyType_sameType_returnsSameInstance() {
        MapType result = baseMapType.withKeyType(stringType);

        assertSame(baseMapType, result);
    }

    // Tests withKeyType with different key type (branch coverage)
    @Test
    public void testWithKeyType_differentType_returnsNewInstance() {
        MapType result = baseMapType.withKeyType(booleanType);

        assertNotSame(baseMapType, result);
        assertEquals(booleanType, result.getKeyType());
        assertEquals(integerType, result.getContentType());
    }

    // Tests refine method updating class and bindings
    @Test
    public void testRefine_subclassAndNewBindings_returnsRefinedInstance() {
        TypeBindings newBindings = TypeBindings.create(TreeMap.class, new JavaType[]{stringType, integerType});
        JavaType refined = baseMapType.refine(TreeMap.class, newBindings, null, null);

        assertNotNull(refined);
        assertEquals(TreeMap.class, refined.getRawClass());
        assertEquals(stringType, refined.getKeyType());
        assertEquals(integerType, refined.getContentType());
    }

    // Tests _narrow method changing raw subclass
    @SuppressWarnings("deprecation")
    @Test
    public void testNarrow_subclass_returnsNarrowedType() {
        JavaType narrowed = baseMapType._narrow(TreeMap.class);

        assertNotNull(narrowed);
        assertEquals(TreeMap.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getKeyType());
        assertEquals(integerType, narrowed.getContentType());
    }

    // Tests toString formatting
    @Test
    public void testToString_validMapType_returnsExpectedFormat() {
        String representation = baseMapType.toString();

        assertTrue(representation.startsWith("[map type; class java.util.HashMap, "));
        assertTrue(representation.contains(" -> "));
        assertTrue(representation.endsWith("]"));
    }

    // Tests construct with non-null superClass and superInterfaces
    @Test
    public void testConstruct_withSuperClassAndSuperInterfaces_createsValidInstance() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType superClass = tf.constructType(Object.class);
        JavaType[] superInterfaces = new JavaType[]{tf.constructType(Cloneable.class)};
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{stringType, integerType});

        MapType mapType = MapType.construct(HashMap.class, bindings, superClass, superInterfaces, stringType, integerType);

        assertNotNull(mapType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(superClass, mapType.getSuperClass());
        assertEquals(1, mapType.getInterfaces().size());
        assertEquals(superInterfaces[0], mapType.getInterfaces().get(0));
    }

    // Tests protected MapType(TypeBase, JavaType, JavaType) constructor
    @Test
    public void testProtectedConstructor_withTypeBase_createsValidInstance() {
        MapType copy = new MapType(baseMapType, booleanType, stringType);

        assertNotNull(copy);
        assertEquals(HashMap.class, copy.getRawClass());
        assertEquals(booleanType, copy.getKeyType());
        assertEquals(stringType, copy.getContentType());
    }

    // Tests refine with non-null superClass and superInterfaces
    @Test
    public void testRefine_withSuperClassAndSuperInterfaces_preservesHierarchy() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType superClass = tf.constructType(Object.class);
        JavaType[] superInterfaces = new JavaType[]{tf.constructType(Cloneable.class)};
        TypeBindings bindings = TypeBindings.create(TreeMap.class, new JavaType[]{stringType, integerType});

        JavaType refined = baseMapType.refine(TreeMap.class, bindings, superClass, superInterfaces);

        assertNotNull(refined);
        assertEquals(TreeMap.class, refined.getRawClass());
        assertEquals(superClass, refined.getSuperClass());
        assertEquals(1, refined.getInterfaces().size());
        assertEquals(superInterfaces[0], refined.getInterfaces().get(0));
    }

    // Tests that withStaticTyping propagates static typing to key and value types
    @Test
    public void testWithStaticTyping_propagatesToKeyAndValueTypes() {
        MapType staticMapType = baseMapType.withStaticTyping();

        assertTrue(staticMapType.useStaticType());
        assertTrue(staticMapType.getKeyType().useStaticType());
        assertTrue(staticMapType.getContentType().useStaticType());
    }
}
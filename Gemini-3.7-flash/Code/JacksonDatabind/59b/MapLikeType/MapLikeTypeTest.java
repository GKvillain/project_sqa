package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JavaType;

public class MapLikeTypeTest {

    private final JavaType stringType = SimpleType.constructUnsafe(String.class);
    private final JavaType integerType = SimpleType.constructUnsafe(Integer.class);
    private final JavaType longType = SimpleType.constructUnsafe(Long.class);

    // Tests construct factory method and basic property getters
    @Test
    public void testConstruct_validTypes_returnsConfiguredInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(stringType, type.getKeyType());
        assertEquals(integerType, type.getContentType());
        assertTrue(type.isContainerType());
        assertTrue(type.isMapLikeType());
        assertTrue(type.isTrueMapType());
    }

    // Tests isTrueMapType with non-Map class
    @Test
    public void testIsTrueMapType_nonMapClass_returnsFalse() {
        MapLikeType type = MapLikeType.construct(String.class, stringType, integerType);
        assertFalse(type.isTrueMapType());
    }

    // Tests upgradeFrom with TypeBase instance
    @Test
    public void testUpgradeFrom_validBaseType_returnsUpgradedMapLikeType() {
        JavaType baseType = SimpleType.constructUnsafe(Map.class);
        MapLikeType upgraded = MapLikeType.upgradeFrom(baseType, stringType, integerType);
        assertNotNull(upgraded);
        assertEquals(Map.class, upgraded.getRawClass());
        assertEquals(stringType, upgraded.getKeyType());
        assertEquals(integerType, upgraded.getContentType());
    }

    // Tests upgradeFrom throws IllegalArgumentException when baseType is not TypeBase
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
            protected String buildCanonicalName() { return null; }
            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public JavaType getKeyType() { return null; }
            @Override
            public boolean isContainerType() { return false; }
            @Override
            public String toString() { return ""; }
            @Override
            public boolean equals(Object o) { return false; }
        };
        MapLikeType.upgradeFrom(customType, stringType, integerType);
    }

    // Tests withKeyType with same key returns this
    @Test
    public void testWithKeyType_sameKey_returnsSameInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        MapLikeType result = type.withKeyType(stringType);
        assertSame(type, result);
    }

    // Tests withKeyType with different key returns new instance
    @Test
    public void testWithKeyType_differentKey_returnsNewInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        MapLikeType result = type.withKeyType(longType);
        assertNotSame(type, result);
        assertEquals(longType, result.getKeyType());
    }

    // Tests withContentType with same and different value types
    @Test
    public void testWithContentType_sameAndDifferent_returnsExpectedInstances() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        JavaType sameResult = type.withContentType(integerType);
        assertSame(type, sameResult);

        JavaType diffResult = type.withContentType(longType);
        assertNotSame(type, diffResult);
        assertEquals(longType, diffResult.getContentType());
    }

    // Tests type handlers and value handlers on MapLikeType
    @Test
    public void testWithHandlers_setProperHandlers() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        assertFalse(type.hasHandlers());

        String typeHandler = "typeHandler";
        String valueHandler = "valueHandler";
        String contentHandler = "contentHandler";
        String contentValueHandler = "contentValHandler";

        MapLikeType withTH = type.withTypeHandler(typeHandler);
        assertEquals(typeHandler, withTH.getTypeHandler());
        assertTrue(withTH.hasHandlers());

        MapLikeType withVH = type.withValueHandler(valueHandler);
        assertEquals(valueHandler, withVH.getValueHandler());
        assertTrue(withVH.hasHandlers());

        MapLikeType withCTH = type.withContentTypeHandler(contentHandler);
        assertEquals(contentHandler, withCTH.getContentTypeHandler());
        assertTrue(withCTH.hasHandlers());

        MapLikeType withCVH = type.withContentValueHandler(contentValueHandler);
        assertEquals(contentValueHandler, withCVH.getContentValueHandler());
        assertTrue(withCVH.hasHandlers());

        MapLikeType withKTH = type.withKeyTypeHandler(contentHandler);
        assertEquals(contentHandler, withKTH.getKeyType().getTypeHandler());
        assertTrue(withKTH.hasHandlers());

        MapLikeType withKVH = type.withKeyValueHandler(contentValueHandler);
        assertEquals(contentValueHandler, withKVH.getKeyType().getValueHandler());
        assertTrue(withKVH.hasHandlers());
    }

    // Tests withStaticTyping on dynamic and static instances
    @Test
    public void testWithStaticTyping_togglesStaticCorrectly() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        assertFalse(type.useStaticType());

        MapLikeType staticType = type.withStaticTyping();
        assertTrue(staticType.useStaticType());
        assertSame(staticType, staticType.withStaticTyping());
    }

    // Tests refine method returns refined MapLikeType
    @Test
    public void testRefine_validParameters_returnsRefinedInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        JavaType refined = type.refine(HashMap.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertEquals(HashMap.class, refined.getRawClass());
        assertEquals(stringType, ((MapLikeType) refined).getKeyType());
        assertEquals(integerType, refined.getContentType());
    }

    // Tests canonical name construction
    @Test
    public void testBuildCanonicalName_returnsCanonicalRepresentation() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", type.toCanonical());
    }

    // Tests generic and erased signatures
    @Test
    public void testSignatures_returnsExpectedFormat() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        StringBuilder erased = new StringBuilder();
        type.getErasedSignature(erased);
        assertEquals("Ljava/util/Map;", erased.toString());

        StringBuilder generic = new StringBuilder();
        type.getGenericSignature(generic);
        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;", generic.toString());
    }

    // Tests toString format
    @Test
    public void testToString_returnsFormattedString() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        String str = type.toString();
        assertTrue(str.startsWith("[map-like type; class java.util.Map,"));
        assertTrue(str.contains(stringType.toString()));
        assertTrue(str.contains(integerType.toString()));
    }

    // Tests equals and identity equality
    @Test
    public void testEquals_variousScenarios_returnsExpectedResults() {
        MapLikeType type1 = MapLikeType.construct(Map.class, stringType, integerType);
        MapLikeType type2 = MapLikeType.construct(Map.class, stringType, integerType);
        MapLikeType typeDiffKey = MapLikeType.construct(Map.class, longType, integerType);
        MapLikeType typeDiffValue = MapLikeType.construct(Map.class, stringType, longType);
        MapLikeType typeDiffClass = MapLikeType.construct(HashMap.class, stringType, integerType);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("not-a-type"));
        assertFalse(type1.equals(typeDiffKey));
        assertFalse(type1.equals(typeDiffValue));
        assertFalse(type1.equals(typeDiffClass));
    }

    // Tests deprecated _narrow method
    @Test
    public void testNarrow_returnsNarrowedType() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        JavaType narrowed = type._narrow(HashMap.class);
        assertEquals(HashMap.class, narrowed.getRawClass());
        assertEquals(stringType, ((MapLikeType) narrowed).getKeyType());
        assertEquals(integerType, narrowed.getContentType());
    }

    // Tests upgradeFrom when baseType is already a MapLikeType instance
    @Test
    public void testUpgradeFrom_alreadyMapLikeType_returnsSameInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        MapLikeType upgraded = MapLikeType.upgradeFrom(type, longType, longType);
        assertSame(type, upgraded);
    }

    // Tests withTypeHandler and withValueHandler when setting the same handler returns this
    @Test
    public void testWithHandlers_sameHandler_returnsSameInstance() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);

        String th = "th";
        MapLikeType withTH = type.withTypeHandler(th);
        assertSame(withTH, withTH.withTypeHandler(th));

        String vh = "vh";
        MapLikeType withVH = type.withValueHandler(vh);
        assertSame(withVH, withVH.withValueHandler(vh));

        String cth = "cth";
        MapLikeType withCTH = type.withContentTypeHandler(cth);
        assertSame(withCTH, withCTH.withContentTypeHandler(cth));

        String cvh = "cvh";
        MapLikeType withCVH = type.withContentValueHandler(cvh);
        assertSame(withCVH, withCVH.withContentValueHandler(cvh));

        String kth = "kth";
        MapLikeType withKTH = type.withKeyTypeHandler(kth);
        assertSame(withKTH, withKTH.withKeyTypeHandler(kth));

        String kvh = "kvh";
        MapLikeType withKVH = type.withKeyValueHandler(kvh);
        assertSame(withKVH, withKVH.withKeyValueHandler(kvh));
    }

    // Tests initial handler getters return null
    @Test
    public void testGetContentHandlers_initialNull() {
        MapLikeType type = MapLikeType.construct(Map.class, stringType, integerType);
        assertNull(type.getContentValueHandler());
        assertNull(type.getContentTypeHandler());
        assertNull(type.getKeyType().getValueHandler());
        assertNull(type.getKeyType().getTypeHandler());
    }
}
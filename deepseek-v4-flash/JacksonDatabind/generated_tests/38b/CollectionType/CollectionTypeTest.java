package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.CollectionType;

public class CollectionTypeTest {

    private TypeFactory typeFactory;
    private JavaType stringType;
    private JavaType integerType;
    private JavaType listType;
    private TypeBindings emptyBindings;
    private CollectionType sampleType;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
        stringType = typeFactory.constructType(String.class);
        integerType = typeFactory.constructType(Integer.class);
        listType = typeFactory.constructType(java.util.List.class);
        emptyBindings = TypeBindings.emptyBindings();
        sampleType = CollectionType.construct(
                java.util.List.class, emptyBindings, listType, new JavaType[0], stringType);
    }

    @Test
    public void testConstruct_normal_returnsNonNull() {
        CollectionType ct = CollectionType.construct(
                java.util.List.class, emptyBindings, listType, new JavaType[0], stringType);
        assertNotNull(ct);
        assertEquals(java.util.List.class, ct.getRawClass());
        assertEquals(stringType, ct.getContentType());
    }

    @Test
    public void testConstruct_deprecatedTwoArgs_returnsNonNull() {
        CollectionType ct = CollectionType.construct(java.util.List.class, stringType);
        assertNotNull(ct);
        assertEquals(java.util.List.class, ct.getRawClass());
        assertEquals(stringType, ct.getContentType());
    }

    @Test
    public void testWithContentType_sameType_returnsSame() {
        assertSame(sampleType, sampleType.withContentType(stringType));
    }

    @Test
    public void testWithContentType_differentType_returnsNewWithDifferentContentType() {
        CollectionType newCt = sampleType.withContentType(integerType);
        assertNotNull(newCt);
        assertNotSame(sampleType, newCt);
        assertEquals(integerType, newCt.getContentType());
        assertEquals(stringType, sampleType.getContentType());
    }

    @Test
    public void testWithTypeHandler_returnsNewWithHandler() {
        Object handler = "typeHandler";
        CollectionType newCt = sampleType.withTypeHandler(handler);
        assertNotNull(newCt);
        assertNotSame(sampleType, newCt);
        // type handler not exposed, but verify different instance
    }

    @Test
    public void testWithContentTypeHandler_returnsNewWithContentTypeHandler() {
        Object handler = "contentHandler";
        CollectionType newCt = sampleType.withContentTypeHandler(handler);
        assertNotNull(newCt);
        assertNotSame(sampleType, newCt);
        assertNotSame(sampleType.getContentType(), newCt.getContentType());
    }

    @Test
    public void testWithValueHandler_returnsNewWithValueHandler() {
        Object handler = "valueHandler";
        CollectionType newCt = sampleType.withValueHandler(handler);
        assertNotNull(newCt);
        assertNotSame(sampleType, newCt);
    }

    @Test
    public void testWithContentValueHandler_returnsNewWithContentValueHandler() {
        Object handler = "contentValueHandler";
        CollectionType newCt = sampleType.withContentValueHandler(handler);
        assertNotNull(newCt);
        assertNotSame(sampleType, newCt);
        assertNotSame(sampleType.getContentType(), newCt.getContentType());
    }

    @Test
    public void testWithStaticTyping_alreadyStatic_returnsSame() {
        CollectionType staticType = sampleType.withStaticTyping();
        assertSame(staticType, staticType.withStaticTyping());
    }

    @Test
    public void testWithStaticTyping_notStatic_returnsNewWithStatic() {
        CollectionType staticType = sampleType.withStaticTyping();
        assertNotNull(staticType);
        assertNotSame(sampleType, staticType);
        assertTrue(staticType.getContentType().isStatic());
        assertFalse(sampleType.getContentType().isStatic());
    }

    @Test
    public void testRefine_returnsNewCollectionType() {
        JavaType refined = sampleType.refine(
                java.util.ArrayList.class, emptyBindings, listType, new JavaType[0]);
        assertNotNull(refined);
        assertTrue(refined instanceof CollectionType);
        assertEquals(java.util.ArrayList.class, refined.getRawClass());
        assertEquals(stringType, refined.getContentType());
    }

    @Test
    public void testToString_containsExpectedFormat() {
        String str = sampleType.toString();
        assertTrue(str.startsWith("[collection type; class "));
        assertTrue(str.contains("java.util.List"));
        assertTrue(str.contains(stringType.toString()));
    }

    @Test
    public void testNarrow_deprecated_returnsNewCollectionType() {
        CollectionType narrowed = sampleType._narrow(java.util.ArrayList.class);
        assertNotNull(narrowed);
        assertEquals(java.util.ArrayList.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getContentType());
    }

    @Test
    public void testWithContentTypeHandler_originalElementTypeUnchanged() {
        JavaType originalContent = sampleType.getContentType();
        sampleType.withContentTypeHandler("handler");
        assertSame(originalContent, sampleType.getContentType());
    }

    @Test
    public void testConstruct_deprecatedTwoArgs_worksWithStringType() {
        CollectionType ct = CollectionType.construct(java.util.Set.class, stringType);
        assertNotNull(ct);
        assertEquals(java.util.Set.class, ct.getRawClass());
        assertEquals(stringType, ct.getContentType());
    }

    // ===== New test cases for uncovered parts =====

    @Test
    public void testConstruct_withNullContentType_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CollectionType.construct(java.util.List.class, emptyBindings, listType, 
                    new JavaType[0], null);
        });
    }

    @Test
    public void testConstruct_deprecatedTwoArgs_withNullContentType_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CollectionType.construct(java.util.List.class, (JavaType) null);
        });
    }

    @Test
    public void testWithContentType_null_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            sampleType.withContentType(null);
        });
    }

    @Test
    public void testRefine_withNullRawClass_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            sampleType.refine(null, emptyBindings, listType, new JavaType[0]);
        });
    }

    @Test
    public void testRefine_withDifferentSuperClass_returnsCollectionType() {
        // Using a non-collection subclass should still work as long as it's valid
        JavaType refined = sampleType.refine(
                java.util.AbstractList.class, emptyBindings, listType, new JavaType[0]);
        assertNotNull(refined);
        assertTrue(refined instanceof CollectionType);
        assertEquals(java.util.AbstractList.class, refined.getRawClass());
    }

    @Test
    public void testGetContentType_returnsCorrectType() {
        assertEquals(stringType, sampleType.getContentType());
    }

    @Test
    public void testGetContentType_notNull() {
        assertNotNull(sampleType.getContentType());
    }

    @Test
    public void testEquals_sameInstance_returnsTrue() {
        assertTrue(sampleType.equals(sampleType));
    }

    @Test
    public void testEquals_differentInstanceSameContent_returnsTrue() {
        CollectionType other = CollectionType.construct(
                java.util.List.class, emptyBindings, listType, new JavaType[0], stringType);
        assertEquals(sampleType, other);
    }

    @Test
    public void testEquals_differentContentType_returnsFalse() {
        CollectionType other = CollectionType.construct(
                java.util.List.class, emptyBindings, listType, new JavaType[0], integerType);
        assertFalse(sampleType.equals(other));
    }

    @Test
    public void testEquals_differentRawClass_returnsFalse() {
        CollectionType other = CollectionType.construct(
                java.util.ArrayList.class, emptyBindings, listType, new JavaType[0], stringType);
        assertFalse(sampleType.equals(other));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        assertFalse(sampleType.equals(null));
    }

    @Test
    public void testHashCode_isConsistent() {
        int hash1 = sampleType.hashCode();
        int hash2 = sampleType.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCode_equalObjects_hasEqualHashCodes() {
        CollectionType other = CollectionType.construct(
                java.util.List.class, emptyBindings, listType, new JavaType[0], stringType);
        assertEquals(sampleType.hashCode(), other.hashCode());
    }

    @Test
    public void testIsCollectionLikeType_returnsTrue() {
        assertTrue(sampleType.isCollectionLikeType());
    }

    @Test
    public void testIsAbstract_returnsFalseForConcreteCollection() {
        CollectionType concreteType = CollectionType.construct(
                java.util.ArrayList.class, emptyBindings, listType, new JavaType[0], stringType);
        assertFalse(concreteType.isAbstract());
    }
}
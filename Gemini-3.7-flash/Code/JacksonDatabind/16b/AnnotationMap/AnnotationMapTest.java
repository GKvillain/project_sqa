package com.fasterxml.jackson.databind.introspect;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;

import static org.junit.Assert.*;

public class AnnotationMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotationA {
        String value() default "A1";
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotationB {
        int value() default 0;
    }

    @TestAnnotationA("A1")
    @TestAnnotationB(1)
    private static class AnnotatedClass1 {}

    @TestAnnotationA("A2")
    @TestAnnotationB(2)
    private static class AnnotatedClass2 {}

    private Annotation annA1;
    private Annotation annA2;
    private Annotation annB1;

    @Before
    public void setUp() {
        annA1 = AnnotatedClass1.class.getAnnotation(TestAnnotationA.class);
        annA2 = AnnotatedClass2.class.getAnnotation(TestAnnotationA.class);
        annB1 = AnnotatedClass1.class.getAnnotation(TestAnnotationB.class);
    }

    // Tests adding a new annotation to empty map returns true
    @Test
    public void testAdd_newAnnotationToEmptyMap_returnsTrue() {
        AnnotationMap map = new AnnotationMap();
        boolean changed = map.add(annA1);
        assertTrue(changed);
        assertEquals(1, map.size());
        assertEquals(annA1, map.get(TestAnnotationA.class));
    }

    // Tests adding the identical annotation already present returns false
    @Test
    public void testAdd_sameAnnotationAlreadyPresent_returnsFalse() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        boolean changed = map.add(annA1);
        assertFalse(changed);
        assertEquals(1, map.size());
    }

    // Tests replacing existing annotation with different instance of same type returns true
    @Test
    public void testAdd_differentAnnotationSameType_returnsTrue() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        boolean changed = map.add(annA2);
        assertTrue(changed);
        assertEquals(annA2, map.get(TestAnnotationA.class));
    }

    // Tests addIfNotPresent when annotation is not yet in map
    @Test
    public void testAddIfNotPresent_annotationNotPresent_returnsTrueAndAdds() {
        AnnotationMap map = new AnnotationMap();
        boolean added = map.addIfNotPresent(annA1);
        assertTrue(added);
        assertEquals(1, map.size());
        assertEquals(annA1, map.get(TestAnnotationA.class));
    }

    // Tests addIfNotPresent when annotation type already exists
    @Test
    public void testAddIfNotPresent_annotationAlreadyPresent_returnsFalseAndDoesNotReplace() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        boolean added = map.addIfNotPresent(annA2);
        assertFalse(added);
        assertEquals(1, map.size());
        assertEquals(annA1, map.get(TestAnnotationA.class));
    }

    // Tests get when map is null/empty
    @Test
    public void testGet_emptyMap_returnsNull() {
        AnnotationMap map = new AnnotationMap();
        assertNull(map.get(TestAnnotationA.class));
    }

    // Tests get when annotation type exists and does not exist
    @Test
    public void testGet_existingAndNonExistingType_returnsCorrectResult() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        assertNotNull(map.get(TestAnnotationA.class));
        assertNull(map.get(TestAnnotationB.class));
    }

    // Tests size of empty and populated map
    @Test
    public void testSize_emptyAndPopulatedMap_returnsCorrectSize() {
        AnnotationMap map = new AnnotationMap();
        assertEquals(0, map.size());
        map.add(annA1);
        assertEquals(1, map.size());
        map.add(annB1);
        assertEquals(2, map.size());
    }

    // Tests annotations iterator when map is uninitialized or empty
    @Test
    public void testAnnotations_emptyMap_returnsEmptyIterable() {
        AnnotationMap map = new AnnotationMap();
        Iterable<Annotation> it = map.annotations();
        assertNotNull(it);
        assertFalse(it.iterator().hasNext());
    }

    // Tests annotations iterator when map contains elements
    @Test
    public void testAnnotations_populatedMap_returnsAllAnnotations() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        map.add(annB1);
        int count = 0;
        for (Annotation a : map.annotations()) {
            assertNotNull(a);
            count++;
        }
        assertEquals(2, count);
    }

    // Tests merge when primary is null or empty
    @Test
    public void testMerge_primaryNullOrEmpty_returnsSecondary() {
        AnnotationMap secondary = new AnnotationMap();
        secondary.add(annA1);

        assertSame(secondary, AnnotationMap.merge(null, secondary));

        AnnotationMap emptyPrimary = new AnnotationMap();
        assertSame(secondary, AnnotationMap.merge(emptyPrimary, secondary));
    }

    // Tests merge when secondary is null or empty
    @Test
    public void testMerge_secondaryNullOrEmpty_returnsPrimary() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(annA1);

        assertSame(primary, AnnotationMap.merge(primary, null));

        AnnotationMap emptySecondary = new AnnotationMap();
        assertSame(primary, AnnotationMap.merge(primary, emptySecondary));
    }

    // Tests merge when both maps have elements with primary overriding secondary
    @Test
    public void testMerge_bothPopulated_primaryOverridesSecondary() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(annA1);

        AnnotationMap secondary = new AnnotationMap();
        secondary.add(annA2);
        secondary.add(annB1);

        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        assertNotNull(merged);
        assertEquals(2, merged.size());
        assertEquals(annA1, merged.get(TestAnnotationA.class));
        assertEquals(annB1, merged.get(TestAnnotationB.class));
    }

    // Tests toString for null/empty map
    @Test
    public void testToString_emptyMap_returnsNullRepresentation() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("[null]", map.toString());
    }

    // Tests toString for populated map
    @Test
    public void testToString_populatedMap_returnsMapString() {
        AnnotationMap map = new AnnotationMap();
        map.add(annA1);
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains(TestAnnotationA.class.getName()));
    }
}
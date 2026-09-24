package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnnotationMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnn1 {
        String value() default "1";
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnn2 {
        String value() default "2";
    }

    @TestAnn1("first")
    @TestAnn2("second")
    private static class AnnotatedClass1 { }

    @TestAnn1("override")
    private static class AnnotatedClass2 { }

    private Annotation getAnn1(Class<?> clazz) {
        return clazz.getAnnotation(TestAnn1.class);
    }

    private Annotation getAnn2(Class<?> clazz) {
        return clazz.getAnnotation(TestAnn2.class);
    }

    // Tests add method with a new annotation returns true and increases size
    @Test
    public void testAdd_newAnnotation_returnsTrue() {
        AnnotationMap map = new AnnotationMap();
        Annotation ann1 = getAnn1(AnnotatedClass1.class);
        boolean changed = map.add(ann1);

        assertTrue(changed);
        assertEquals(1, map.size());
        assertEquals(ann1, map.get(TestAnn1.class));
    }

    // Tests add method when same annotation instance is added again returns false
    @Test
    public void testAdd_sameAnnotationTwice_returnsFalseOnSecondAdd() {
        AnnotationMap map = new AnnotationMap();
        Annotation ann1 = getAnn1(AnnotatedClass1.class);
        map.add(ann1);

        boolean changed = map.add(ann1);
        assertFalse(changed);
        assertEquals(1, map.size());
    }

    // Tests add method when different annotation with same type is added returns true
    @Test
    public void testAdd_differentValueOfSameType_returnsTrue() {
        AnnotationMap map = new AnnotationMap();
        Annotation ann1 = getAnn1(AnnotatedClass1.class);
        Annotation ann2 = getAnn1(AnnotatedClass2.class);

        map.add(ann1);
        boolean changed = map.add(ann2);

        assertTrue(changed);
        assertEquals(1, map.size());
        assertEquals(ann2, map.get(TestAnn1.class));
    }

    // Tests addIfNotPresent when annotation is not yet in map
    @Test
    public void testAddIfNotPresent_notPresent_addsAndReturnsTrue() {
        AnnotationMap map = new AnnotationMap();
        Annotation ann1 = getAnn1(AnnotatedClass1.class);

        boolean added = map.addIfNotPresent(ann1);
        assertTrue(added);
        assertEquals(1, map.size());
        assertEquals(ann1, map.get(TestAnn1.class));
    }

    // Tests addIfNotPresent when annotation type already exists in map
    @Test
    public void testAddIfNotPresent_alreadyPresent_returnsFalseAndDoesNotReplace() {
        AnnotationMap map = new AnnotationMap();
        Annotation ann1 = getAnn1(AnnotatedClass1.class);
        Annotation ann2 = getAnn1(AnnotatedClass2.class);

        map.addIfNotPresent(ann1);
        boolean added = map.addIfNotPresent(ann2);

        assertFalse(added);
        assertEquals(1, map.size());
        assertEquals(ann1, map.get(TestAnn1.class));
    }

    // Tests get when map is null/empty
    @Test
    public void testGet_emptyMap_returnsNull() {
        AnnotationMap map = new AnnotationMap();
        assertNull(map.get(TestAnn1.class));
    }

    // Tests get when requested annotation type is missing from non-empty map
    @Test
    public void testGet_missingAnnotation_returnsNull() {
        AnnotationMap map = new AnnotationMap();
        map.add(getAnn1(AnnotatedClass1.class));

        assertNull(map.get(TestAnn2.class));
    }

    // Tests size on empty and non-empty map
    @Test
    public void testSize_variousStates_returnsCorrectSize() {
        AnnotationMap map = new AnnotationMap();
        assertEquals(0, map.size());

        map.add(getAnn1(AnnotatedClass1.class));
        assertEquals(1, map.size());

        map.add(getAnn2(AnnotatedClass1.class));
        assertEquals(2, map.size());
    }

    // Tests annotations() on empty or uninitialized map
    @Test
    public void testAnnotations_emptyMap_returnsEmptyIterable() {
        AnnotationMap map = new AnnotationMap();
        Iterable<Annotation> iterable = map.annotations();
        assertNotNull(iterable);
        assertFalse(iterable.iterator().hasNext());
    }

    // Tests annotations() on map with elements
    @Test
    public void testAnnotations_populatedMap_returnsAllAnnotations() {
        AnnotationMap map = new AnnotationMap();
        map.add(getAnn1(AnnotatedClass1.class));
        map.add(getAnn2(AnnotatedClass1.class));

        Iterable<Annotation> iterable = map.annotations();
        int count = 0;
        for (Iterator<Annotation> it = iterable.iterator(); it.hasNext();) {
            it.next();
            count++;
        }
        assertEquals(2, count);
    }

    // Tests toString on empty map
    @Test
    public void testToString_emptyMap_returnsNullRepresentation() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("[null]", map.toString());
    }

    // Tests toString on populated map
    @Test
    public void testToString_populatedMap_returnsMapString() {
        AnnotationMap map = new AnnotationMap();
        map.add(getAnn1(AnnotatedClass1.class));
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains(TestAnn1.class.getName()));
    }

    // Tests merge when primary is null or empty
    @Test
    public void testMerge_primaryNullOrEmpty_returnsSecondary() {
        AnnotationMap secondary = new AnnotationMap();
        secondary.add(getAnn1(AnnotatedClass1.class));

        AnnotationMap result1 = AnnotationMap.merge(null, secondary);
        assertSame(secondary, result1);

        AnnotationMap primaryEmpty = new AnnotationMap();
        AnnotationMap result2 = AnnotationMap.merge(primaryEmpty, secondary);
        assertSame(secondary, result2);
    }

    // Tests merge when secondary is null or empty
    @Test
    public void testMerge_secondaryNullOrEmpty_returnsPrimary() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(getAnn1(AnnotatedClass1.class));

        AnnotationMap result1 = AnnotationMap.merge(primary, null);
        assertSame(primary, result1);

        AnnotationMap secondaryEmpty = new AnnotationMap();
        AnnotationMap result2 = AnnotationMap.merge(primary, secondaryEmpty);
        assertSame(primary, result2);
    }

    // Tests merge when both maps have annotations with overriding
    @Test
    public void testMerge_bothPopulated_primaryOverridesSecondary() {
        AnnotationMap primary = new AnnotationMap();
        Annotation ann1Primary = getAnn1(AnnotatedClass2.class);
        primary.add(ann1Primary);

        AnnotationMap secondary = new AnnotationMap();
        Annotation ann1Secondary = getAnn1(AnnotatedClass1.class);
        Annotation ann2Secondary = getAnn2(AnnotatedClass1.class);
        secondary.add(ann1Secondary);
        secondary.add(ann2Secondary);

        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        assertNotNull(merged);
        assertEquals(2, merged.size());
        assertEquals(ann1Primary, merged.get(TestAnn1.class));
        assertEquals(ann2Secondary, merged.get(TestAnn2.class));
    }
}
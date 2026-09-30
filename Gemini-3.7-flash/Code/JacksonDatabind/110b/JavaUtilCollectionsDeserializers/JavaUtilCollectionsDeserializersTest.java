package com.fasterxml.jackson.databind.deser.impl;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class JavaUtilCollectionsDeserializersTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();

    // Tests findForCollection matching Arrays.asList type
    @Test
    public void testFindForCollection_arraysAsList_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Arrays.asList("a", "b").getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForCollection matching Collections.singletonList type
    @Test
    public void testFindForCollection_singletonList_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForCollection matching Collections.singleton type (Set)
    @Test
    public void testFindForCollection_singletonSet_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singleton("a").getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForCollection matching Collections.unmodifiableList type
    @Test
    public void testFindForCollection_unmodifiableList_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableList(Collections.singletonList("a")).getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForCollection matching Collections.unmodifiableSet type
    @Test
    public void testFindForCollection_unmodifiableSet_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableSet(Collections.singleton("a")).getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForCollection with unsupported collection type returning null
    @Test
    public void testFindForCollection_unsupportedType_returnsNull() throws Exception {
        JavaType type = _typeFactory.constructType(ArrayList.class);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNull(deser);
    }

    // Tests findForMap matching Collections.singletonMap type
    @Test
    public void testFindForMap_singletonMap_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonMap("key", "value").getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForMap matching Collections.unmodifiableMap type
    @Test
    public void testFindForMap_unmodifiableMap_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableMap(Collections.singletonMap("key", "value")).getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    // Tests findForMap with unsupported map type returning null
    @Test
    public void testFindForMap_unsupportedType_returnsNull() throws Exception {
        JavaType type = _typeFactory.constructType(HashMap.class);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        assertNull(deser);
    }

    // Tests converter for singleton set with valid single element input
    @Test
    public void testConvert_singletonSetValidInput_returnsSingletonSet() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singleton("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Converter<Object, ?> conv = deser.getConverter();

        Set<String> input = new HashSet<String>(Collections.singletonList("item"));
        Object result = conv.convert(input);

        assertNotNull(result);
        assertTrue(result instanceof Set);
        assertEquals(Collections.singleton("item"), result);
    }

    // Tests converter for singleton set throwing exception when empty
    @Test(expected = IllegalArgumentException.class)
    public void testConvert_singletonSetEmptyInput_throwsIllegalArgumentException() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singleton("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        deser.getConverter().convert(Collections.emptySet());
    }

    // Tests converter for singleton set throwing exception when multiple elements present
    @Test(expected = IllegalArgumentException.class)
    public void testConvert_singletonSetMultipleElements_throwsIllegalArgumentException() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singleton("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        deser.getConverter().convert(new HashSet<String>(Arrays.asList("a", "b")));
    }

    // Tests converter for singleton list with valid single element input
    @Test
    public void testConvert_singletonListValidInput_returnsSingletonList() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Converter<Object, ?> conv = deser.getConverter();

        List<String> input = Arrays.asList("item");
        Object result = conv.convert(input);

        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(Collections.singletonList("item"), result);
    }

    // Tests converter for singleton list throwing exception when size != 1
    @Test(expected = IllegalArgumentException.class)
    public void testConvert_singletonListMultipleElements_throwsIllegalArgumentException() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        deser.getConverter().convert(Arrays.asList("a", "b"));
    }

    // Tests converter for singleton map with valid single entry input
    @Test
    public void testConvert_singletonMapValidInput_returnsSingletonMap() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonMap("k", "v").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForMap(null, type);
        Converter<Object, ?> conv = deser.getConverter();

        Map<String, String> input = new HashMap<String, String>();
        input.put("key", "val");
        Object result = conv.convert(input);

        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(Collections.singletonMap("key", "val"), result);
    }

    // Tests converter for singleton map throwing exception when empty
    @Test(expected = IllegalArgumentException.class)
    public void testConvert_singletonMapEmptyInput_throwsIllegalArgumentException() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonMap("k", "v").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForMap(null, type);
        deser.getConverter().convert(Collections.emptyMap());
    }

    // Tests converter for singleton map throwing exception when multiple entries present
    @Test(expected = IllegalArgumentException.class)
    public void testConvert_singletonMapMultipleEntries_throwsIllegalArgumentException() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonMap("k", "v").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForMap(null, type);
        Map<String, String> input = new HashMap<String, String>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        deser.getConverter().convert(input);
    }

    // Tests converter for unmodifiable set returns unmodifiable wrapper
    @Test(expected = UnsupportedOperationException.class)
    public void testConvert_unmodifiableSet_returnsUnmodifiableSet() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableSet(Collections.singleton("a")).getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);

        Set<String> input = new HashSet<String>(Arrays.asList("x", "y"));
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) deser.getConverter().convert(input);

        assertEquals(input, result);
        result.add("z");
    }

    // Tests converter for unmodifiable list returns unmodifiable wrapper
    @Test(expected = UnsupportedOperationException.class)
    public void testConvert_unmodifiableList_returnsUnmodifiableList() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableList(Collections.singletonList("a")).getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);

        List<String> input = new ArrayList<String>(Arrays.asList("x", "y"));
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) deser.getConverter().convert(input);

        assertEquals(input, result);
        result.add("z");
    }

    // Tests converter for unmodifiable map returns unmodifiable wrapper
    @Test(expected = UnsupportedOperationException.class)
    public void testConvert_unmodifiableMap_returnsUnmodifiableMap() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.unmodifiableMap(Collections.singletonMap("k", "v")).getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForMap(null, type);

        Map<String, String> input = new HashMap<String, String>();
        input.put("k1", "v1");
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) deser.getConverter().convert(input);

        assertEquals(input, result);
        result.put("k2", "v2");
    }

    // Tests converter for Arrays.asList returning list as-is
    @Test
    public void testConvert_asList_returnsListAsIs() throws Exception {
        JavaType type = _typeFactory.constructType(Arrays.asList("a", "b").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);

        List<String> input = Arrays.asList("a", "b", "c");
        Object result = deser.getConverter().convert(input);

        assertSame(input, result);
    }

    // Tests converter null input handling
    @Test
    public void testConvert_nullInput_returnsNull() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNull(deser.getConverter().convert(null));
    }

    // Tests converter input and output JavaType methods
    @Test
    public void testConverter_getInputTypeAndOutputType_returnsInputType() throws Exception {
        JavaType concreteType = _typeFactory.constructType(Arrays.asList("a", "b").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>) JavaUtilCollectionsDeserializers.findForCollection(null, concreteType);
        Converter<Object, ?> conv = deser.getConverter();

        JavaType inType = conv.getInputType(_typeFactory);
        JavaType outType = conv.getOutputType(_typeFactory);

        assertNotNull(inType);
        assertEquals(inType, outType);
    }

    // Tests findForCollection matching Collections.unmodifiableSortedSet
    @Test
    public void testFindForCollection_unmodifiableSortedSet() throws Exception {
        SortedSet<String> set = Collections.unmodifiableSortedSet(new TreeSet<String>());
        JavaType type = _typeFactory.constructType(set.getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        if (deser != null) {
            assertTrue(deser instanceof StdDelegatingDeserializer);
            StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
            TreeSet<String> input = new TreeSet<String>(Arrays.asList("a", "b"));
            Object result = sdd.getConverter().convert(input);
            assertTrue(result instanceof SortedSet);
            assertEquals(input, result);
        }
    }

    // Tests findForCollection matching Collections.unmodifiableNavigableSet
    @Test
    public void testFindForCollection_unmodifiableNavigableSet() throws Exception {
        NavigableSet<String> set = Collections.unmodifiableNavigableSet(new TreeSet<String>());
        JavaType type = _typeFactory.constructType(set.getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        if (deser != null) {
            assertTrue(deser instanceof StdDelegatingDeserializer);
            StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
            TreeSet<String> input = new TreeSet<String>(Arrays.asList("a", "b"));
            Object result = sdd.getConverter().convert(input);
            assertTrue(result instanceof NavigableSet);
            assertEquals(input, result);
        }
    }

    // Tests findForMap matching Collections.unmodifiableSortedMap
    @Test
    public void testFindForMap_unmodifiableSortedMap() throws Exception {
        SortedMap<String, String> map = Collections.unmodifiableSortedMap(new TreeMap<String, String>());
        JavaType type = _typeFactory.constructType(map.getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        if (deser != null) {
            assertTrue(deser instanceof StdDelegatingDeserializer);
            StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
            TreeMap<String, String> input = new TreeMap<String, String>();
            input.put("k", "v");
            Object result = sdd.getConverter().convert(input);
            assertTrue(result instanceof SortedMap);
            assertEquals(input, result);
        }
    }

    // Tests findForMap matching Collections.unmodifiableNavigableMap
    @Test
    public void testFindForMap_unmodifiableNavigableMap() throws Exception {
        NavigableMap<String, String> map = Collections.unmodifiableNavigableMap(new TreeMap<String, String>());
        JavaType type = _typeFactory.constructType(map.getClass());
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        if (deser != null) {
            assertTrue(deser instanceof StdDelegatingDeserializer);
            StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
            TreeMap<String, String> input = new TreeMap<String, String>();
            input.put("k", "v");
            Object result = sdd.getConverter().convert(input);
            assertTrue(result instanceof NavigableMap);
            assertEquals(input, result);
        }
    }
}
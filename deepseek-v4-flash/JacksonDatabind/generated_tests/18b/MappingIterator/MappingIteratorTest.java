package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

public class MappingIteratorTest {

    private MappingIterator<String> createIterator(String jsonArray) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(jsonArray);
        return mapper.readValues(parser, String.class);
    }

    @Test
    public void testHasNextValue_normalIteration_returnsCorrectValues() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\",\"b\",\"c\"]");
        assertTrue(it.hasNextValue());
        assertEquals("a", it.nextValue());
        assertTrue(it.hasNextValue());
        assertEquals("b", it.nextValue());
        assertTrue(it.hasNextValue());
        assertEquals("c", it.nextValue());
        assertFalse(it.hasNextValue());
    }

    @Test
    public void testHasNextValue_emptyArray_returnsFalse() throws IOException {
        MappingIterator<String> it = createIterator("[]");
        assertFalse(it.hasNextValue());
    }

    @Test
    public void testHasNextValue_singleElement_returnsOneValue() throws IOException {
        MappingIterator<String> it = createIterator("[\"x\"]");
        assertTrue(it.hasNextValue());
        assertEquals("x", it.nextValue());
        assertFalse(it.hasNextValue());
    }

    @Test
    public void testHasNext_normalIteration_returnsTrueThenFalse() throws IOException {
        MappingIterator<String> it = createIterator("[\"hello\"]");
        assertTrue(it.hasNext());
        assertEquals("hello", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNext_noElement_throwsNoSuchElementException() throws IOException {
        MappingIterator<String> it = createIterator("[]");
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextValue_noElement_throwsNoSuchElementException() throws IOException {
        MappingIterator<String> it = createIterator("[]");
        it.nextValue();
    }

    @Test
    public void testClose_afterClose_hasNextValueReturnsFalse() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.close();
        assertFalse(it.hasNextValue());
    }

    @Test
    public void testReadAll_list_returnsAllValues() throws IOException {
        MappingIterator<String> it = createIterator("[\"x\",\"y\",\"z\"]");
        List<String> list = it.readAll();
        assertEquals(Arrays.asList("x","y","z"), list);
    }

    @Test
    public void testReadAll_collection_returnsAllValues() throws IOException {
        MappingIterator<String> it = createIterator("[\"1\",\"2\"]");
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> returned = it.readAll(result);
        assertSame(result, returned);
        assertEquals(Arrays.asList("1","2"), result);
    }

    @Test
    public void testReadAll_existingList_appendsValues() throws IOException {
        MappingIterator<String> it = createIterator("[\"c\",\"d\"]");
        List<String> list = new ArrayList<>(Arrays.asList("a","b"));
        List<String> returned = it.readAll(list);
        assertSame(list, returned);
        assertEquals(Arrays.asList("a","b","c","d"), list);
    }

    @Test
    public void testGetParser_beforeClose_returnsParser() throws IOException {
        MappingIterator<String> it = createIterator("[\"val\"]");
        assertNotNull(it.getParser());
    }

    @Test
    public void testGetParser_afterClose_returnsParserNotNull() throws IOException {
        MappingIterator<String> it = createIterator("[\"x\"]");
        JsonParser p = it.getParser();
        it.close();
        assertNotNull(it.getParser());
        assertTrue(p.isClosed());
    }

    @Test
    public void testGetCurrentLocation_returnsLocation() throws IOException {
        MappingIterator<String> it = createIterator("[\"test\"]");
        assertNotNull(it.getCurrentLocation());
    }

    @Test
    public void testGetParserSchema_returnsNullForJson() throws IOException {
        MappingIterator<String> it = createIterator("[\"x\"]");
        assertNull(it.getParserSchema());
    }

    @Test
    public void testHasNextValue_calledTwice_returnsTrue() throws IOException {
        MappingIterator<String> it = createIterator("[\"x\"]");
        assertTrue(it.hasNextValue());
        assertTrue(it.hasNextValue());
        assertEquals("x", it.nextValue());
        assertFalse(it.hasNextValue());
    }

    @Test
    public void testNextValue_afterNext_hasNextReturnsFalse() throws IOException {
        MappingIterator<String> it = createIterator("[\"only\"]");
        assertEquals("only", it.nextValue());
        assertFalse(it.hasNextValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_always_throwsUnsupportedOperationException() {
        MappingIterator<String> it = createIterator("[]");
        it.remove();
    }

    @Test
    public void testNextValue_withUpdatedValue_updatesGivenValue() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class).withValueToUpdate("initial");
        JsonParser p = mapper.getFactory().createParser("[\"newValue\"]");
        MappingIterator<String> it = reader.readValues(p);
        assertTrue(it.hasNextValue());
        String value = it.nextValue();
        assertEquals("newValue", value);
    }

    @Test
    public void testEmptyIterator_hasNextValue_returnsFalse() throws IOException {
        MappingIterator<String> it = MappingIterator.emptyIterator();
        assertFalse(it.hasNextValue());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyIterator_next_throwsNoSuchElement() throws IOException {
        MappingIterator<String> it = MappingIterator.emptyIterator();
        it.next();
    }

    @Test
    public void testGetParser_afterExhaustion_returnsNull() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.readAll();
        assertNull(it.getParser());
    }

    // ========== New tests to cover previously skipped areas ==========

    @Test
    public void testHasNextAfterClose_returnsFalse() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.close();
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextValueAfterClose_throwsException() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.close();
        it.nextValue();
    }

    @Test
    public void testReadAllAfterClose_returnsEmptyList() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.close();
        List<String> list = it.readAll();
        assertTrue(list.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testReadAllWithNullArgument_throwsException() throws IOException {
        MappingIterator<String> it = createIterator("[\"a\"]");
        it.readAll(null);
    }

    @Test
    public void testEmptyIterator_readAll_returnsEmptyList() throws IOException {
        MappingIterator<String> it = MappingIterator.emptyIterator();
        List<String> list = it.readAll();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testEmptyIterator_getParser_returnsNull() throws IOException {
        MappingIterator<String> it = MappingIterator.emptyIterator();
        assertNull(it.getParser());
    }
}
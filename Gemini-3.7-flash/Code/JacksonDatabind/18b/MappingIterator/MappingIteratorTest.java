package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class MappingIteratorTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Tests emptyIterator behavior
    @Test
    public void testEmptyIterator_emptyInstance_hasNoElements() throws IOException {
        MappingIterator<Object> it = MappingIterator.emptyIterator();
        assertFalse(it.hasNext());
        assertFalse(it.hasNextValue());
        assertNull(it.getParser());
    }

    // Tests nextValue on emptyIterator throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testEmptyIterator_nextValue_throwsNoSuchElementException() throws IOException {
        MappingIterator<Object> it = MappingIterator.emptyIterator();
        it.nextValue();
    }

    // Tests next on emptyIterator throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testEmptyIterator_next_throwsNoSuchElementException() {
        MappingIterator<Object> it = MappingIterator.emptyIterator();
        it.next();
    }

    // Tests standard iteration with hasNext and next
    @Test
    public void testHasNextAndNext_validSequence_iteratesSuccessfully() {
        MappingIterator<Integer> it;
        try {
            it = MAPPER.readerFor(Integer.class).readValues("1 2 3");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    // Tests iteration with hasNextValue and nextValue
    @Test
    public void testHasNextValueAndNextValue_validSequence_iteratesSuccessfully() throws IOException {
        MappingIterator<String> it = MAPPER.readerFor(String.class).readValues("\"foo\" \"bar\"");
        assertTrue(it.hasNextValue());
        assertEquals("foo", it.nextValue());
        assertTrue(it.hasNextValue());
        assertEquals("bar", it.nextValue());
        assertFalse(it.hasNextValue());
    }

    // Tests calling nextValue directly without hasNextValue
    @Test
    public void testNextValue_withoutCallingHasNextValue_returnsElement() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("10 20");
        assertEquals(Integer.valueOf(10), it.nextValue());
        assertEquals(Integer.valueOf(20), it.nextValue());
        assertFalse(it.hasNextValue());
    }

    // Tests calling next when exhausted throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testNext_whenExhausted_throwsNoSuchElementException() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("42");
        assertEquals(Integer.valueOf(42), it.next());
        it.next();
    }

    // Tests readAll method returning new ArrayList
    @Test
    public void testReadAll_validSequence_returnsList() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("1 2 3 4");
        List<Integer> list = it.readAll();
        assertEquals(Arrays.asList(1, 2, 3, 4), list);
        assertFalse(it.hasNextValue());
    }

    // Tests readAll method with custom List target
    @Test
    public void testReadAll_withTargetList_populatesList() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("5 6");
        List<Integer> target = new LinkedList<Integer>();
        target.add(4);
        List<Integer> result = it.readAll(target);
        assertSame(target, result);
        assertEquals(Arrays.asList(4, 5, 6), result);
    }

    // Tests readAll method with custom Collection target
    @Test
    public void testReadAll_withTargetCollection_populatesCollection() throws IOException {
        MappingIterator<String> it = MAPPER.readerFor(String.class).readValues("\"a\" \"b\"");
        Set<String> set = new HashSet<String>();
        Set<String> result = it.readAll(set);
        assertSame(set, result);
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // Tests array sequence iteration
    @Test
    public void testReadValues_arrayInput_iteratesElements() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("[1, 2, 3]");
        List<Integer> values = it.readAll();
        assertEquals(Arrays.asList(1, 2, 3), values);
    }

    // Tests empty array sequence
    @Test
    public void testReadValues_emptyArray_iteratesEmpty() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("[]");
        assertFalse(it.hasNextValue());
        List<Integer> values = it.readAll();
        assertTrue(values.isEmpty());
    }

    // Tests remove method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_always_throwsUnsupportedOperationException() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("1 2");
        it.remove();
    }

    // Tests close method closes underlying parser
    @Test
    public void testClose_openIterator_closesParser() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("1 2");
        assertNotNull(it.getParser());
        it.close();
        it.close(); // Calling close again should be safe
    }

    // Tests getCurrentLocation and getParser accessors
    @Test
    public void testAccessors_openIterator_returnsNonNull() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("1");
        assertNotNull(it.getParser());
        assertNotNull(it.getCurrentLocation());
        assertNull(it.getParserSchema());
        it.close();
    }

    // Tests updating existing value during iteration
    @Test
    public void testUpdatingValue_existingBean_updatesInstance() throws IOException {
        MyBean bean = new MyBean();
        bean.x = 10;
        MappingIterator<MyBean> it = MAPPER.readerForUpdating(bean).readValues("{\"x\":20}");
        assertTrue(it.hasNextValue());
        MyBean result = it.nextValue();
        assertSame(bean, result);
        assertEquals(20, result.x);
    }

    // Tests mapping exception during next()
    @Test(expected = RuntimeJsonMappingException.class)
    public void testNext_invalidJsonStructure_throwsRuntimeJsonMappingException() throws IOException {
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues("{\"key\": 1}");
        it.next();
    }

    // Tests parser without managed close
    @Test
    public void testUnmanagedParser_explicitParser_doesNotCloseOnIterationEnd() throws IOException {
        JsonParser p = MAPPER.getFactory().createParser("1 2");
        MappingIterator<Integer> it = MAPPER.readerFor(Integer.class).readValues(p);
        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(1), it.nextValue());
        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(2), it.nextValue());
        assertFalse(it.hasNextValue());
        // Since parser was passed in, it should not be closed automatically
        assertFalse(p.isClosed());
        p.close();
    }

    // Helper class for update test
    static class MyBean {
        public int x;
    }
}
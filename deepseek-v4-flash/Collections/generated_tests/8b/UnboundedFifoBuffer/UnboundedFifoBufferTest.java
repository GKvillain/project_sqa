package org.apache.commons.collections.buffer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;

import org.apache.commons.collections.BufferUnderflowException;
import org.junit.Test;

public class UnboundedFifoBufferTest {

    // Test constructor with invalid size (<=0)
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeSize_throwsIllegalArgumentException() {
        new UnboundedFifoBuffer(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroSize_throwsIllegalArgumentException() {
        new UnboundedFifoBuffer(0);
    }

    // Test add null object
    @Test(expected = NullPointerException.class)
    public void testAdd_nullObject_throwsNullPointerException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add(null);
    }

    // Test get on empty buffer
    @Test(expected = BufferUnderflowException.class)
    public void testGet_emptyBuffer_throwsBufferUnderflowException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.get();
    }

    // Test remove on empty buffer
    @Test(expected = BufferUnderflowException.class)
    public void testRemove_emptyBuffer_throwsBufferUnderflowException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.remove();
    }

    // Normal FIFO behavior
    @Test
    public void testAddAndRemove_normalFifoBehavior() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        assertEquals(3, buffer.size());
        assertEquals("A", buffer.get());
        assertEquals("A", buffer.remove());
        assertEquals("B", buffer.remove());
        assertEquals("C", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    // Test expansion of internal array
    @Test
    public void testAdd_triggerExpansion_correctState() {
        // Start with tiny buffer to force expansion
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(1);
        assertTrue(buffer.add("X"));
        // Adding second element triggers expansion (buffer.length=2, size()=1, 1+1 >= 2)
        assertTrue(buffer.add("Y"));
        assertEquals(2, buffer.size());
        assertEquals("X", buffer.remove());
        assertEquals("Y", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    // Test adding and removing to cause head/tail wrap-around
    @Test
    public void testAddAndRemove_wrapAround_correctOrder() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3); // buffer.length=4
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        buffer.remove(); // removes A, head=1
        buffer.remove(); // removes B, head=2
        buffer.add("D"); // tail=3
        buffer.add("E"); // tail=0 (wrap)
        // Order should be: C, D, E
        assertEquals(3, buffer.size());
        assertEquals("C", buffer.remove());
        assertEquals("D", buffer.remove());
        assertEquals("E", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    // Iterator: basic iteration
    @Test
    public void testIterator_iterationOverElements() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("1");
        buffer.add("2");
        buffer.add("3");
        java.util.Iterator it = buffer.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertEquals("2", it.next());
        assertEquals("3", it.next());
        assertFalse(it.hasNext());
    }

    // Iterator: remove first element (head)
    @Test
    public void testIterator_removeFirstElement_success() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("A");
        buffer.add("B");
        java.util.Iterator it = buffer.iterator();
        assertEquals("A", it.next());
        it.remove(); // removes head
        assertEquals(1, buffer.size());
        assertEquals("B", buffer.get());
    }

    // Iterator: remove middle element (not head)
    @Test
    public void testIterator_removeMiddleElement_success() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        java.util.Iterator it = buffer.iterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // remove B
        assertEquals(2, buffer.size());
        assertEquals("A", buffer.remove());
        assertEquals("C", buffer.remove());
    }

    // Iterator: remove last element in iteration
    @Test
    public void testIterator_removeLastElement_success() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("X");
        buffer.add("Y");
        java.util.Iterator it = buffer.iterator();
        it.next(); // X
        it.next(); // Y
        it.remove(); // remove Y
        assertEquals(1, buffer.size());
        assertEquals("X", buffer.remove());
    }

    // Iterator: remove after last remove throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testIterator_removeAfterRemove_throwsIllegalStateException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("only");
        java.util.Iterator it = buffer.iterator();
        it.next();
        it.remove();
        it.remove(); // second remove should throw
    }

    // Iterator: next when exhausted throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextWhenExhausted_throwsNoSuchElementException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        java.util.Iterator it = buffer.iterator();
        it.next(); // empty buffer
    }

    // Serialization: empty buffer
    @Test
    public void testSerialization_emptyBuffer() throws Exception {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();
        ois.close();

        assertTrue(deserialized.isEmpty());
        assertEquals(0, deserialized.size());
    }

    // Serialization: non-empty buffer
    @Test
    public void testSerialization_nonEmptyBuffer() throws Exception {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("first");
        buffer.add("second");
        buffer.add("third");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();
        ois.close();

        assertEquals(3, deserialized.size());
        assertEquals("first", deserialized.remove());
        assertEquals("second", deserialized.remove());
        assertEquals("third", deserialized.remove());
        assertTrue(deserialized.isEmpty());
    }

    // Serialization: buffer that had head/tail wrap-around before serialization
    @Test
    public void testSerialization_afterWrapAround() throws Exception {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3); // length=4
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        buffer.remove(); // removes A, head=1
        buffer.remove(); // removes B, head=2
        buffer.add("D"); // tail=3
        buffer.add("E"); // tail=0 (wrap)
        // Now head=2, tail=0; order: C, D, E
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();
        ois.close();

        assertEquals(3, deserialized.size());
        assertEquals("C", deserialized.remove());
        assertEquals("D", deserialized.remove());
        assertEquals("E", deserialized.remove());
        assertTrue(deserialized.isEmpty());
    }

    // Iterator: remove inside wrapped buffer (non-head)
    @Test
    public void testIterator_removeMiddleElementInWrappedBuffer() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3); // length=4
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        buffer.remove(); // head=1
        buffer.remove(); // head=2
        buffer.add("D"); // tail=3
        buffer.add("E"); // tail=0
        // State: head=2, tail=0; elements: [C@2, D@3, E@0]
        java.util.Iterator it = buffer.iterator();
        assertEquals("C", it.next());
        assertEquals("D", it.next());
        it.remove(); // remove D (middle element)
        assertEquals(2, buffer.size());
        // Remaining: C, E
        assertEquals("C", buffer.remove());
        assertEquals("E", buffer.remove());
        assertTrue(buffer.isEmpty());
    }
}
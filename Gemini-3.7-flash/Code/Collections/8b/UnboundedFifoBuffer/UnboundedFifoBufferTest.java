package org.apache.commons.collections.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.BufferUnderflowException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnboundedFifoBufferTest {

    private UnboundedFifoBuffer buffer;

    @Before
    public void setUp() {
        buffer = new UnboundedFifoBuffer();
    }

    // Tests default constructor initialization
    @Test
    public void testConstructor_default_createsEmptyBuffer() {
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());
    }

    // Tests constructor with valid initial capacity
    @Test
    public void testConstructor_validSize_createsEmptyBuffer() {
        UnboundedFifoBuffer customBuffer = new UnboundedFifoBuffer(5);
        assertTrue(customBuffer.isEmpty());
        assertEquals(0, customBuffer.size());
    }

    // Tests constructor with zero initial capacity throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroSize_throwsIllegalArgumentException() {
        new UnboundedFifoBuffer(0);
    }

    // Tests constructor with negative initial capacity throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeSize_throwsIllegalArgumentException() {
        new UnboundedFifoBuffer(-1);
    }

    // Tests add method with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testAdd_nullElement_throwsNullPointerException() {
        buffer.add(null);
    }

    // Tests adding elements and buffer FIFO retrieval order
    @Test
    public void testAddAndGetAndRemove_validElements_maintainsFifoOrder() {
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        assertFalse(buffer.isEmpty());
        assertEquals(3, buffer.size());
        assertEquals("A", buffer.get());

        assertEquals("A", buffer.remove());
        assertEquals(2, buffer.size());
        assertEquals("B", buffer.get());

        assertEquals("B", buffer.remove());
        assertEquals(1, buffer.size());
        assertEquals("C", buffer.get());

        assertEquals("C", buffer.remove());
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());
    }

    // Tests buffer automatic growth when capacity is exceeded
    @Test
    public void testAdd_exceedInitialCapacity_expandsBufferCorrectly() {
        UnboundedFifoBuffer smallBuffer = new UnboundedFifoBuffer(1);
        smallBuffer.add("1");
        smallBuffer.add("2");
        smallBuffer.add("3");
        smallBuffer.add("4");

        assertEquals(4, smallBuffer.size());
        assertEquals("1", smallBuffer.remove());
        assertEquals("2", smallBuffer.remove());
        assertEquals("3", smallBuffer.remove());
        assertEquals("4", smallBuffer.remove());
        assertTrue(smallBuffer.isEmpty());
    }

    // Tests buffer circular wrap-around when elements are removed and added
    @Test
    public void testAddAndRemove_wrapAround_sizeAndOrderCorrect() {
        UnboundedFifoBuffer smallBuffer = new UnboundedFifoBuffer(2);
        smallBuffer.add("1");
        smallBuffer.add("2");
        assertEquals("1", smallBuffer.remove());

        smallBuffer.add("3");
        assertEquals(2, smallBuffer.size());
        assertEquals("2", smallBuffer.get());
        assertEquals("2", smallBuffer.remove());
        assertEquals("3", smallBuffer.remove());
        assertTrue(smallBuffer.isEmpty());
    }

    // Tests get method on empty buffer throws BufferUnderflowException
    @Test(expected = BufferUnderflowException.class)
    public void testGet_emptyBuffer_throwsBufferUnderflowException() {
        buffer.get();
    }

    // Tests remove method on empty buffer throws BufferUnderflowException
    @Test(expected = BufferUnderflowException.class)
    public void testRemove_emptyBuffer_throwsBufferUnderflowException() {
        buffer.remove();
    }

    // Tests iterator traversal through buffer elements
    @Test
    public void testIterator_traversal_returnsElementsInOrder() {
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    // Tests iterator next method when no elements remain throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextOnEmpty_throwsNoSuchElementException() {
        Iterator it = buffer.iterator();
        it.next();
    }

    // Tests iterator remove before calling next throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testIteratorRemove_withoutNext_throwsIllegalStateException() {
        buffer.add("A");
        Iterator it = buffer.iterator();
        it.remove();
    }

    // Tests iterator remove called twice in a row throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testIteratorRemove_twiceConsecutively_throwsIllegalStateException() {
        buffer.add("A");
        buffer.add("B");
        Iterator it = buffer.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    // Tests iterator removing the first (head) element
    @Test
    public void testIteratorRemove_headElement_removesCorrectly() {
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        assertEquals("A", it.next());
        it.remove();

        assertEquals(2, buffer.size());
        assertEquals("B", buffer.get());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
    }

    // Tests iterator removing a middle element requiring element shifting
    @Test
    public void testIteratorRemove_middleElement_removesAndShiftsCorrectly() {
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator it = buffer.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();

        assertEquals(2, buffer.size());
        assertEquals("A", buffer.get());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        assertEquals("A", buffer.remove());
        assertEquals("C", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    // Tests iterator removing the last element
    @Test
    public void testIteratorRemove_lastElement_removesCorrectly() {
        buffer.add("A");
        buffer.add("B");

        Iterator it = buffer.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();

        assertEquals(1, buffer.size());
        assertEquals("A", buffer.get());
        assertFalse(it.hasNext());
    }

    // Tests iterator remove when buffer is in wrap-around state
    @Test
    public void testIteratorRemove_wrappedBuffer_removesAndMaintainsOrder() {
        UnboundedFifoBuffer customBuffer = new UnboundedFifoBuffer(3);
        customBuffer.add("1");
        customBuffer.add("2");
        customBuffer.remove(); // head advances
        customBuffer.add("3");
        customBuffer.add("4"); // wrap around occurred

        Iterator it = customBuffer.iterator();
        assertEquals("2", it.next());
        assertEquals("3", it.next());
        it.remove(); // remove middle element in wrapped array

        assertEquals(2, customBuffer.size());
        assertEquals("2", customBuffer.remove());
        assertEquals("4", customBuffer.remove());
        assertTrue(customBuffer.isEmpty());
    }

    // Tests serialization and deserialization of the buffer
    @Test
    public void testSerialization_roundTrip_preservesContentAndOrder() throws Exception {
        buffer.add("X");
        buffer.add("Y");
        buffer.add("Z");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(buffer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnboundedFifoBuffer deserialized = (UnboundedFifoBuffer) ois.readObject();
        ois.close();

        assertEquals(3, deserialized.size());
        assertEquals("X", deserialized.remove());
        assertEquals("Y", deserialized.remove());
        assertEquals("Z", deserialized.remove());
        assertTrue(deserialized.isEmpty());
    }
}
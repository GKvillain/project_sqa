package org.apache.commons.collections4.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections4.BoundedCollection;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link UnmodifiableBoundedCollection}.
 */
public class UnmodifiableBoundedCollectionTest {

    private TestBoundedCollection<String> testCollection;

    @Before
    public void setUp() {
        testCollection = new TestBoundedCollection<String>(2);
        testCollection.getUnderlyingCollection().add("A");
        testCollection.getUnderlyingCollection().add("B");
    }

    // Tests factory method with BoundedCollection input
    @Test
    public void testUnmodifiableBoundedCollection_boundedCollectionInput_returnsValidCollection() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        assertNotNull(col);
        assertEquals(2, col.size());
        assertTrue(col.contains("A"));
        assertTrue(col.contains("B"));
    }

    // Tests factory method with null Collection input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_nullCollection_throwsIllegalArgumentException() {
        Collection<String> nullColl = null;
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(nullColl);
    }

    // Tests factory method with non-bounded collection input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_notBoundedCollection_throwsIllegalArgumentException() {
        Collection<String> list = new ArrayList<String>();
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(list);
    }

    // Tests factory method unwrapping AbstractCollectionDecorator to find BoundedCollection
    @Test
    public void testUnmodifiableBoundedCollection_wrappedInAbstractCollectionDecorator_unwrapsSuccessfully() {
        AbstractCollectionDecorator<String> decorated = new AbstractCollectionDecorator<String>(testCollection) {
            private static final long serialVersionUID = 1L;
        };
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) decorated);
        assertNotNull(col);
        assertEquals(2, col.maxSize());
        assertTrue(col.isFull());
    }

    // Tests factory method unwrapping SynchronizedCollection to find BoundedCollection
    @Test
    public void testUnmodifiableBoundedCollection_wrappedInSynchronizedCollection_unwrapsSuccessfully() {
        Collection<String> synchronizedCol = SynchronizedCollection.synchronizedCollection(testCollection);
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(synchronizedCol);
        assertNotNull(col);
        assertEquals(2, col.maxSize());
        assertTrue(col.isFull());
    }

    // Tests iterator returns elements and its remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_removeCalled_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        Iterator<String> it = col.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
    }

    // Tests add method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_elementAdded_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.add("C");
    }

    // Tests addAll method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAll_collectionAdded_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.addAll(Arrays.asList("C", "D"));
    }

    // Tests clear method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testClear_called_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.clear();
    }

    // Tests remove method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_objectRemoved_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.remove("A");
    }

    // Tests removeAll method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll_collectionRemoved_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.removeAll(Arrays.asList("A"));
    }

    // Tests retainAll method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll_collectionRetained_throwsUnsupportedOperationException() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        col.retainAll(Arrays.asList("A"));
    }

    // Tests isFull method delegates correctly
    @Test
    public void testIsFull_delegatesToUnderlyingCollection_returnsCorrectStatus() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        assertTrue(col.isFull());

        TestBoundedCollection<String> nonFullCollection = new TestBoundedCollection<String>(5);
        nonFullCollection.getUnderlyingCollection().add("A");
        BoundedCollection<String> nonFullCol = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(nonFullCollection);
        assertFalse(nonFullCol.isFull());
    }

    // Tests maxSize method delegates correctly
    @Test
    public void testMaxSize_delegatesToUnderlyingCollection_returnsCorrectMaxSize() {
        BoundedCollection<String> col = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(testCollection);
        assertEquals(2, col.maxSize());
    }

    /**
     * Helper dummy BoundedCollection implementation for testing purposes.
     */
    private static class TestBoundedCollection<E> implements BoundedCollection<E> {
        private final Collection<E> list = new ArrayList<E>();
        private final int max;

        public TestBoundedCollection(final int max) {
            this.max = max;
        }

        public Collection<E> getUnderlyingCollection() {
            return list;
        }

        public boolean isFull() {
            return list.size() >= max;
        }

        public int maxSize() {
            return max;
        }

        public int size() {
            return list.size();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public boolean contains(final Object o) {
            return list.contains(o);
        }

        public Iterator<E> iterator() {
            return list.iterator();
        }

        public Object[] toArray() {
            return list.toArray();
        }

        public <T> T[] toArray(final T[] a) {
            return list.toArray(a);
        }

        public boolean add(final E e) {
            return list.add(e);
        }

        public boolean remove(final Object o) {
            return list.remove(o);
        }

        public boolean containsAll(final Collection<?> c) {
            return list.containsAll(c);
        }

        public boolean addAll(final Collection<? extends E> c) {
            return list.addAll(c);
        }

        public boolean removeAll(final Collection<?> c) {
            return list.removeAll(c);
        }

        public boolean retainAll(final Collection<?> c) {
            return list.retainAll(c);
        }

        public void clear() {
            list.clear();
        }
    }
}
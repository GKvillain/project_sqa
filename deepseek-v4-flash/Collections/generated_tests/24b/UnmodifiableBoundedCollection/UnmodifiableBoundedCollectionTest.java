package org.apache.commons.collections4.collection;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.collections4.BoundedCollection;
import org.junit.Test;

public class UnmodifiableBoundedCollectionTest {

    // ########################################################################
    // Helper stubs
    // ########################################################################

    private static class SimpleBoundedCollection<E> extends AbstractCollection<E> implements BoundedCollection<E> {
        private final Collection<E> delegate;

        SimpleBoundedCollection(Collection<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() { return delegate.size(); }
        @Override
        public Iterator<E> iterator() { return delegate.iterator(); }
        @Override
        public boolean add(E e) { return delegate.add(e); }
        @Override
        public boolean addAll(Collection<? extends E> c) { return delegate.addAll(c); }
        @Override
        public boolean remove(Object o) { return delegate.remove(o); }
        @Override
        public boolean removeAll(Collection<?> c) { return delegate.removeAll(c); }
        @Override
        public boolean retainAll(Collection<?> c) { return delegate.retainAll(c); }
        @Override
        public void clear() { delegate.clear(); }
        @Override
        public boolean isFull() { return false; }
        @Override
        public int maxSize() { return Integer.MAX_VALUE; }
    }

    private static class TestAbstractCollectionDecorator extends AbstractCollectionDecorator<String> {
        TestAbstractCollectionDecorator(Collection<String> coll) {
            super(coll);
        }
    }

    private static class TestSynchronizedCollection extends SynchronizedCollection<String> {
        TestSynchronizedCollection(Collection<String> coll) {
            super(coll);
        }
    }

    // ########################################################################
    // Tests for factory method unmodifiableBoundedCollection(BoundedCollection)
    // ########################################################################

    @Test
    public void testUnmodifiableBoundedCollection_BoundedCollectionInput_returnsUnmodifiable() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.add("a");
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        assertNotNull(result);
        assertTrue(result instanceof UnmodifiableBoundedCollection);
        // delegation
        assertFalse(result.isFull());
        assertEquals(Integer.MAX_VALUE, result.maxSize());
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // Null input for BoundedCollection factory
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_NullBoundedCollection_throwsException() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<?>) null);
    }

    // ########################################################################
    // Tests for factory method unmodifiableBoundedCollection(Collection)
    // ########################################################################

    // Null Collection input
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_NullCollection_throwsException() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<?>) null);
    }

    // Already a BoundedCollection
    @Test
    public void testUnmodifiableBoundedCollection_CollectionAlreadyBounded_returnsUnmodifiable() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        BoundedCollection<String> result =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<? extends String>) original);
        assertNotNull(result);
        assertTrue(result instanceof UnmodifiableBoundedCollection);
    }

    // Wrapped in AbstractCollectionDecorator (unwraps to BoundedCollection)
    @Test
    public void testUnmodifiableBoundedCollection_CollectionWrappedInAbstractCollectionDecorator_works() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        TestAbstractCollectionDecorator decorator = new TestAbstractCollectionDecorator(original);
        BoundedCollection<String> result =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<? extends String>) decorator);
        assertNotNull(result);
        assertTrue(result instanceof UnmodifiableBoundedCollection);
    }

    // Wrapped in SynchronizedCollection
    @Test
    public void testUnmodifiableBoundedCollection_CollectionWrappedInSynchronizedCollection_works() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        TestSynchronizedCollection decorator = new TestSynchronizedCollection(original);
        BoundedCollection<String> result =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<? extends String>) decorator);
        assertNotNull(result);
        assertTrue(result instanceof UnmodifiableBoundedCollection);
    }

    // Plain Collection (non‑Bounded) -> exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_CollectionNotBounded_throwsException() {
        ArrayList<String> list = new ArrayList<>();
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<? extends String>) list);
    }

    // Decorator that wraps a non‑Bounded collection -> exception after unwrapping
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableBoundedCollection_CollectionNotBoundedAfterUnwrapping_throwsException() {
        ArrayList<String> list = new ArrayList<>();
        TestAbstractCollectionDecorator decorator = new TestAbstractCollectionDecorator(list);
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<? extends String>) decorator);
    }

    // ########################################################################
    // Tests for unmodifiable behavior (all mutating methods throw UOE)
    // ########################################################################

    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.add("x");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAll_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.addAll(Arrays.asList("x", "y"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.add("a");
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.add("a");
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.remove("a");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.addAll(Arrays.asList("a", "b"));
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.removeAll(Arrays.asList("a"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll_throwsUnsupportedOperationException() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.addAll(Arrays.asList("a", "b"));
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        result.retainAll(Arrays.asList("a"));
    }

    // ########################################################################
    // Tests for iterator – returns an unmodifiable iterator
    // ########################################################################

    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_returnsUnmodifiableIterator() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        original.add("x");
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        Iterator<String> it = result.iterator();
        it.next(); // consume element
        it.remove(); // should throw UnsupportedOperationException
    }

    // ########################################################################
    // Tests for isFull() and maxSize() delegation
    // ########################################################################

    @Test
    public void testIsFull_delegatesToDecorated() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>()) {
            @Override
            public boolean isFull() { return true; }
            @Override
            public int maxSize() { return 10; }
        };
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        assertTrue(result.isFull());
    }

    @Test
    public void testMaxSize_delegatesToDecorated() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>()) {
            @Override
            public boolean isFull() { return true; }
            @Override
            public int maxSize() { return 10; }
        };
        BoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        assertEquals(10, result.maxSize());
    }

    // ########################################################################
    // Test that decorated() returns a BoundedCollection
    // ########################################################################

    @Test
    public void testDecorated_returnsBoundedCollection() {
        SimpleBoundedCollection<String> original = new SimpleBoundedCollection<>(new ArrayList<String>());
        UnmodifiableBoundedCollection<String> result =
                (UnmodifiableBoundedCollection<String>)
                        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);
        BoundedCollection<String> inner = result.decorated();
        assertNotNull(inner);
        assertTrue(inner instanceof BoundedCollection);
        // verify it's the same object
        assertSame(original, inner);
    }
}
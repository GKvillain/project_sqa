package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class NameBasedCandidateFilterTest {

    private MockCandidateFilter next;
    private NameBasedCandidateFilter filter;
    private List<Object> capturedCandidates;
    private Field capturedField;
    private Object capturedFieldInstance;
    private int capturedCallCount;

    private static class Target {
        @SuppressWarnings("unused")
        private Object foo;
        @SuppressWarnings("unused")
        private Object bar;
    }

    @Before
    public void setUp() {
        capturedCandidates = new ArrayList<Object>();
        capturedField = null;
        capturedFieldInstance = null;
        capturedCallCount = 0;

        next = (MockCandidateFilter) Proxy.newProxyInstance(
                NameBasedCandidateFilterTest.class.getClassLoader(),
                new Class<?>[]{MockCandidateFilter.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("filterCandidate".equals(method.getName()) && args != null) {
                            capturedCallCount++;
                            capturedCandidates.clear();
                            if (args[0] instanceof Collection<?>) {
                                capturedCandidates.addAll((Collection<?>) args[0]);
                            }
                            capturedField = (Field) args[1];
                            capturedFieldInstance = args[2];
                        }
                        return null;
                    }
                });

        filter = new NameBasedCandidateFilter(next);
    }

    private Object namedMock(String name) {
        return org.mockito.Mockito.mock(List.class, name);
    }

    private List<Object> mockList(String... names) {
        List<Object> mocks = new ArrayList<Object>();
        for (String name : names) {
            mocks.add(namedMock(name));
        }
        return mocks;
    }

    private Field field(String name) throws Exception {
        return Target.class.getDeclaredField(name);
    }

    // Tests true branch of mocks.size() > 1 and true branch of name matching.
    @Test
    public void testFilterCandidate_moreThanOneCandidate_withMatchingName_passesOnlyMatchingMock() throws Exception {
        Object matchingMock = namedMock("foo");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(namedMock("bar"));
        mocks.add(matchingMock);
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertEquals(1, capturedCandidates.size());
        assertSame(matchingMock, capturedCandidates.get(0));
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }

    // Tests true branch of mocks.size() > 1 and false branch of name matching.
    @Test
    public void testFilterCandidate_moreThanOneCandidate_withoutMatchingName_passesEmptyList() throws Exception {
        List<Object> mocks = mockList("bar", "baz");
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertTrue(capturedCandidates.isEmpty());
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }

    // Regression: a single candidate must not be dropped by name filtering.
    @Test
    public void testFilterCandidate_singleCandidate_withoutMatchingName_passesOriginalCandidate() throws Exception {
        Object onlyMock = namedMock("bar");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(onlyMock);
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertEquals(1, capturedCandidates.size());
        assertSame(onlyMock, capturedCandidates.get(0));
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }

    // Tests single candidate with a matching name.
    @Test
    public void testFilterCandidate_singleCandidate_withMatchingName_passesOriginalCandidate() throws Exception {
        Object onlyMock = namedMock("foo");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(onlyMock);

        filter.filterCandidate(mocks, field("foo"), new Object());

        assertEquals(1, capturedCallCount);
        assertEquals(1, capturedCandidates.size());
        assertSame(onlyMock, capturedCandidates.get(0));
    }

    // Tests empty candidate list boundary.
    @Test
    public void testFilterCandidate_emptyCandidateList_passesEmptyList() throws Exception {
        List<Object> mocks = new ArrayList<Object>();

        filter.filterCandidate(mocks, field("foo"), new Object());

        assertEquals(1, capturedCallCount);
        assertTrue(capturedCandidates.isEmpty());
    }

    // New test: multiple candidates with multiple matches (two mocks with same name as field)
    @Test
    public void testFilterCandidate_multipleCandidates_multipleMatchingNames_passesAllMatching() throws Exception {
        Object matchingMock1 = namedMock("foo");
        Object matchingMock2 = namedMock("foo");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(namedMock("bar"));
        mocks.add(matchingMock1);
        mocks.add(matchingMock2);
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertEquals(2, capturedCandidates.size());
        assertTrue(capturedCandidates.contains(matchingMock1));
        assertTrue(capturedCandidates.contains(matchingMock2));
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }

    // New test: case-sensitivity – names with different case should not match
    @Test
    public void testFilterCandidate_multipleCandidates_caseSensitiveMismatch_passesEmptyList() throws Exception {
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(namedMock("Foo"));
        mocks.add(namedMock("FOO"));
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertTrue(capturedCandidates.isEmpty());
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }

    // New test: multiple candidates with one matching and three non-matching (ensures multiple false conditions)
    @Test
    public void testFilterCandidate_multipleCandidates_oneMatchingMultipleNonMatching_passesOnlyMatching() throws Exception {
        Object matchingMock = namedMock("foo");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(namedMock("bar"));
        mocks.add(namedMock("baz"));
        mocks.add(matchingMock);
        mocks.add(namedMock("qux"));
        Field field = field("foo");
        Object instance = new Object();

        filter.filterCandidate(mocks, field, instance);

        assertEquals(1, capturedCallCount);
        assertEquals(1, capturedCandidates.size());
        assertSame(matchingMock, capturedCandidates.get(0));
        assertSame(field, capturedField);
        assertSame(instance, capturedFieldInstance);
    }
}
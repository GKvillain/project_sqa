package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MockCandidateFilterTest {

    private static class SampleTarget {
        private String stringField;
        private Integer intField;
        private List<String> listField;
    }

    // Tests normal case with valid inputs returning an injecter
    @Test
    public void testFilterCandidate_validInputs_returnsOngoingInjecter() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                return new OngoingInjecter() {
                    @Override
                    public Object thenInject() {
                        return "injectedMock";
                    }
                };
            }
        };

        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Arrays.<Object>asList("injectedMock");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);
        assertEquals("injectedMock", injecter.thenInject());
    }

    // Tests boundary case with empty mocks collection
    @Test
    public void testFilterCandidate_emptyMocks_returnsOngoingInjecter() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks.isEmpty()) {
                    return new OngoingInjecter() {
                        @Override
                        public Object thenInject() {
                            return null;
                        }
                    };
                }
                return null;
            }
        };

        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("intField");
        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, target);

        assertNotNull(injecter);
        assertNull(injecter.thenInject());
    }

    // Tests argument passing to ensure received parameters match given inputs
    @Test
    public void testFilterCandidate_argumentPassing_receivesExactArguments() throws Exception {
        final SampleTarget target = new SampleTarget();
        final Field field = SampleTarget.class.getDeclaredField("listField");
        final List<Object> mocks = new ArrayList<Object>();
        mocks.add(new ArrayList<String>());

        final boolean[] called = new boolean[1];

        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> receivedMocks, Field receivedField, Object receivedInstance) {
                assertSame(mocks, receivedMocks);
                assertSame(field, receivedField);
                assertSame(target, receivedInstance);
                called[0] = true;
                return new OngoingInjecter() {
                    @Override
                    public Object thenInject() {
                        return null;
                    }
                };
            }
        };

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertTrue(called[0]);
        assertNotNull(injecter);
    }

    // Tests edge case with null mocks collection
    @Test
    public void testFilterCandidate_nullMocks_returnsInjecter() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks == null) {
                    return new OngoingInjecter() {
                        @Override
                        public Object thenInject() {
                            return null;
                        }
                    };
                }
                return null;
            }
        };

        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        OngoingInjecter injecter = filter.filterCandidate(null, field, target);

        assertNotNull(injecter);
        assertNull(injecter.thenInject());
    }

    // Tests edge case with null field instance
    @Test
    public void testFilterCandidate_nullFieldInstance_returnsInjecter() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (fieldInstance == null) {
                    return new OngoingInjecter() {
                        @Override
                        public Object thenInject() {
                            return null;
                        }
                    };
                }
                return null;
            }
        };

        Field field = SampleTarget.class.getDeclaredField("stringField");
        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, null);

        assertNotNull(injecter);
        assertNull(injecter.thenInject());
    }

    // Tests edge case with null fieldToBeInjected
    @Test
    public void testFilterCandidate_nullFieldToBeInjected_returnsInjecter() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (fieldToBeInjected == null) {
                    return new OngoingInjecter() {
                        @Override
                        public Object thenInject() {
                            return null;
                        }
                    };
                }
                return null;
            }
        };

        SampleTarget target = new SampleTarget();
        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), null, target);

        assertNotNull(injecter);
        assertNull(injecter.thenInject());
    }

    // Tests execution of OngoingInjecter thenInject
    @Test
    public void testFilterCandidate_thenInjectExecution_performsInjectionAction() throws Exception {
        final Integer mockValue = Integer.valueOf(42);
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field fieldToBeInjected, final Object fieldInstance) {
                return new OngoingInjecter() {
                    @Override
                    public Object thenInject() {
                        try {
                            fieldToBeInjected.setAccessible(true);
                            fieldToBeInjected.set(fieldInstance, mocks.iterator().next());
                            return fieldToBeInjected.get(fieldInstance);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };

        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("intField");
        OngoingInjecter injecter = filter.filterCandidate(Collections.<Object>singletonList(mockValue), field, target);

        assertNotNull(injecter);
        Object injectedResult = injecter.thenInject();
        assertEquals(mockValue, injectedResult);
        assertEquals(mockValue, target.intField);
    }
}
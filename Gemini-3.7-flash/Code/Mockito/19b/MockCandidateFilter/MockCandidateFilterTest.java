package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MockCandidateFilterTest {

    private static class BaseTarget {
        protected Number baseNumberField;
    }

    private static class SampleTarget extends BaseTarget {
        private String stringField;
        private Integer intField;
        private List<String> listField;
        private boolean primitiveBooleanField;
        private String[] arrayField;
    }

    private SampleTarget targetInstance;
    private Field stringField;
    private Field intField;
    private Field listField;
    private Field baseNumberField;
    private Field primitiveBooleanField;
    private Field arrayField;

    @Before
    public void setUp() throws Exception {
        targetInstance = new SampleTarget();
        stringField = SampleTarget.class.getDeclaredField("stringField");
        intField = SampleTarget.class.getDeclaredField("intField");
        listField = SampleTarget.class.getDeclaredField("listField");
        baseNumberField = BaseTarget.class.getDeclaredField("baseNumberField");
        primitiveBooleanField = SampleTarget.class.getDeclaredField("primitiveBooleanField");
        arrayField = SampleTarget.class.getDeclaredField("arrayField");
    }

    // Tests normal case where filter returns an OngoingInjecter instance
    @Test
    public void testFilterCandidate_validInputs_returnsOngoingInjecter() {
        final OngoingInjecter expectedInjecter = new OngoingInjecter() {
            public boolean thenInject() {
                return true;
            }
        };

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks != null && fieldToBeInjected != null && fieldInstance != null) {
                    return expectedInjecter;
                }
                return null;
            }
        };

        Collection<Object> mocks = Arrays.<Object>asList("mockString");
        OngoingInjecter result = filter.filterCandidate(mocks, stringField, targetInstance);

        assertNotNull(result);
        assertEquals(expectedInjecter, result);
        assertTrue(result.thenInject());
    }

    // Tests case where mocks collection is empty
    @Test
    public void testFilterCandidate_emptyMocks_returnsInjecterOrNull() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks.isEmpty()) {
                    return new OngoingInjecter() {
                        public boolean thenInject() {
                            return false;
                        }
                    };
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.emptyList(), stringField, targetInstance);

        assertNotNull(result);
        assertFalse(result.thenInject());
    }

    // Tests boundary case where mocks collection contains multiple candidate objects
    @Test
    public void testFilterCandidate_multipleCandidates_filtersCorrectMock() {
        final String expectedMock = "targetMock";
        final List<Object> capturedCandidates = new ArrayList<Object>();

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (fieldToBeInjected.getType().isInstance(mock)) {
                        capturedCandidates.add(mock);
                    }
                }
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        return !capturedCandidates.isEmpty();
                    }
                };
            }
        };

        List<Object> mocks = Arrays.asList(Integer.valueOf(123), expectedMock, Double.valueOf(45.6));
        OngoingInjecter result = filter.filterCandidate(mocks, stringField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
        assertEquals(1, capturedCandidates.size());
        assertEquals(expectedMock, capturedCandidates.get(0));
    }

    // Tests edge case where mocks collection is null
    @Test
    public void testFilterCandidate_nullMocks_handledSafely() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks == null) {
                    return new OngoingInjecter() {
                        public boolean thenInject() {
                            return false;
                        }
                    };
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(null, stringField, targetInstance);

        assertNotNull(result);
        assertFalse(result.thenInject());
    }

    // Tests edge case where fieldToBeInjected is null
    @Test
    public void testFilterCandidate_nullField_returnsNull() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (fieldToBeInjected == null) {
                    return null;
                }
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        return true;
                    }
                };
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList("mock"), null, targetInstance);

        assertNull(result);
    }

    // Tests edge case where fieldInstance is null
    @Test
    public void testFilterCandidate_nullFieldInstance_returnsNull() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (fieldInstance == null) {
                    return null;
                }
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        return true;
                    }
                };
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList("mock"), stringField, null);

        assertNull(result);
    }

    // Tests filtering for primitive wrapper field type
    @Test
    public void testFilterCandidate_intFieldTypeMatching_returnsInjecter() {
        final Integer mockInt = Integer.valueOf(42);

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList(mockInt), intField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
    }

    // Tests filtering for generic/interface field type
    @Test
    public void testFilterCandidate_listFieldTypeMatching_returnsInjecter() {
        final ArrayList<String> mockList = new ArrayList<String>();

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList(mockList), listField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
    }

    // Tests behavior when no candidate matches the target field type
    @Test
    public void testFilterCandidate_noMatchingCandidate_returnsNull() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList("nonMatchingString"), intField, targetInstance);

        assertNull(result);
    }

    // Tests chained filter delegation pattern
    @Test
    public void testFilterCandidate_chainedFilterDelegation_passesParametersCorrectly() {
        final boolean[] filterInvoked = new boolean[]{false, false};

        final MockCandidateFilter nextFilter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                filterInvoked[1] = true;
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        return true;
                    }
                };
            }
        };

        MockCandidateFilter firstFilter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                filterInvoked[0] = true;
                return nextFilter.filterCandidate(mocks, fieldToBeInjected, fieldInstance);
            }
        };

        OngoingInjecter result = firstFilter.filterCandidate(Collections.<Object>singletonList("mock"), stringField, targetInstance);

        assertNotNull(result);
        assertTrue(filterInvoked[0]);
        assertTrue(filterInvoked[1]);
        assertTrue(result.thenInject());
    }

    // Tests handling of null elements within the mocks collection
    @Test
    public void testFilterCandidate_collectionWithNullElements_skipsNullsSafely() {
        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (mock != null && fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        Collection<Object> mocks = Arrays.asList(null, "validMockString", null);
        OngoingInjecter result = filter.filterCandidate(mocks, stringField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
    }

    // Tests superclass/polymorphic type matching
    @Test
    public void testFilterCandidate_inheritedFieldPolymorphicMatching_returnsInjecter() {
        final Double mockDouble = Double.valueOf(3.14);

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (mock != null && fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList(mockDouble), baseNumberField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
    }

    // Tests array type field matching
    @Test
    public void testFilterCandidate_arrayFieldMatching_returnsInjecter() {
        final String[] mockArray = new String[]{"elem1", "elem2"};

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                for (Object mock : mocks) {
                    if (mock != null && fieldToBeInjected.getType().isAssignableFrom(mock.getClass())) {
                        return new OngoingInjecter() {
                            public boolean thenInject() {
                                return true;
                            }
                        };
                    }
                }
                return null;
            }
        };

        OngoingInjecter result = filter.filterCandidate(Collections.<Object>singletonList(mockArray), arrayField, targetInstance);

        assertNotNull(result);
        assertTrue(result.thenInject());
    }

    // Tests actual field injection execution via OngoingInjecter
    @Test
    public void testFilterCandidate_ongoingInjecterActualInjection_setsFieldValue() {
        final String injectedValue = "injectedStringValue";

        MockCandidateFilter filter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field fieldToBeInjected, final Object fieldInstance) {
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        try {
                            fieldToBeInjected.setAccessible(true);
                            fieldToBeInjected.set(fieldInstance, mocks.iterator().next());
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                };
            }
        };

        OngoingInjecter injecter = filter.filterCandidate(Collections.<Object>singletonList(injectedValue), stringField, targetInstance);
        assertNotNull(injecter);
        assertTrue(injecter.thenInject());
        assertEquals(injectedValue, targetInstance.stringField);
    }
}
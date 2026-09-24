package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class PropertyAndSetterInjectionTest {

    private PropertyAndSetterInjection injection;

    @Before
    public void setUp() {
        injection = new PropertyAndSetterInjection();
    }

    // Helper classes for testing injection scenarios
    public static class BaseTarget {
        private String baseMessage;

        public String getBaseMessage() {
            return baseMessage;
        }
    }

    public static class SubTarget extends BaseTarget {
        private List<?> listField;
        private final String finalField = "final";
        private static String staticField = "static";

        public List<?> getListField() {
            return listField;
        }
    }

    public static class SetterTarget {
        private String value;
        private boolean setterCalled = false;

        public void setValue(String value) {
            this.value = value;
            this.setterCalled = true;
        }

        public String getValue() {
            return value;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    public static class MultiCandidateTarget {
        private String candidateA;
        private String candidateB;

        public String getCandidateA() {
            return candidateA;
        }

        public String getCandidateB() {
            return candidateB;
        }
    }

    public static class ThrowingConstructorTarget {
        public ThrowingConstructorTarget() {
            throw new RuntimeException("Constructor failure");
        }
    }

    public static class Holder {
        public SubTarget subTarget = new SubTarget();
        public SubTarget uninitializedSubTarget;
        public SetterTarget setterTarget = new SetterTarget();
        public MultiCandidateTarget multiCandidateTarget = new MultiCandidateTarget();
        public ThrowingConstructorTarget throwingTarget;
    }

    // Tests field injection on simple matching field
    @Test
    public void testProcessInjection_matchingMockCandidate_injectsSuccessfully() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("subTarget");
        Set<Object> mocks = new HashSet<Object>();
        List<String> mockList = Collections.singletonList("item");
        mocks.add(mockList);

        boolean injected = injection.processInjection(field, holder, mocks);

        assertTrue(injected);
        assertSame(mockList, holder.subTarget.getListField());
    }

    // Tests setter injection when setter is available
    @Test
    public void testProcessInjection_setterAvailable_injectsViaSetter() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("setterTarget");
        Set<Object> mocks = new HashSet<Object>();
        String mockValue = "injectedString";
        mocks.add(mockValue);

        boolean injected = injection.processInjection(field, holder, mocks);

        assertTrue(injected);
        assertTrue(holder.setterTarget.isSetterCalled());
        assertEquals("injectedString", holder.setterTarget.getValue());
    }

    // Tests injection into superclass fields
    @Test
    public void testProcessInjection_superClassFields_injectsHierarchy() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("subTarget");
        Set<Object> mocks = new HashSet<Object>();
        String baseMock = "baseString";
        mocks.add(baseMock);

        boolean injected = injection.processInjection(field, holder, mocks);

        assertTrue(injected);
        assertEquals(baseMock, holder.subTarget.getBaseMessage());
    }

    // Tests automatic instantiation when target field is null
    @Test
    public void testProcessInjection_uninitializedField_instantiatesAndInjects() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("uninitializedSubTarget");
        Set<Object> mocks = new HashSet<Object>();
        List<String> mockList = Collections.singletonList("item");
        mocks.add(mockList);

        boolean injected = injection.processInjection(field, holder, mocks);

        assertTrue(injected);
        assertNotNull(holder.uninitializedSubTarget);
        assertSame(mockList, holder.uninitializedSubTarget.getListField());
    }

    // Tests empty mock candidates set returns false
    @Test
    public void testProcessInjection_emptyMockCandidates_returnsFalse() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("subTarget");
        Set<Object> mocks = new HashSet<Object>();

        boolean injected = injection.processInjection(field, holder, mocks);

        assertFalse(injected);
        assertNull(holder.subTarget.getListField());
    }

    // Tests when no mock candidates match target field types
    @Test
    public void testProcessInjection_noMatchingCandidates_returnsFalse() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("subTarget");
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(Integer.valueOf(123));

        boolean injected = injection.processInjection(field, holder, mocks);

        assertFalse(injected);
        assertNull(holder.subTarget.getListField());
    }

    // Tests name-based injection when multiple candidates of same type exist
    @Test
    public void testProcessInjection_multipleCandidatesSameType_injectsByName() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("multiCandidateTarget");
        Set<Object> mocks = new HashSet<Object>();
        String candidateA = "firstCandidate";
        String candidateB = "secondCandidate";
        mocks.add(candidateA);
        mocks.add(candidateB);

        boolean injected = injection.processInjection(field, holder, mocks);

        assertTrue(injected);
        assertNotNull(holder.multiCandidateTarget.getCandidateA());
        assertNotNull(holder.multiCandidateTarget.getCandidateB());
    }

    // Tests final and static fields are ignored and not overwritten
    @Test
    public void testProcessInjection_finalAndStaticFields_areNotOverwritten() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("subTarget");
        Set<Object> mocks = new HashSet<Object>();
        mocks.add("newString");

        injection.processInjection(field, holder, mocks);

        assertEquals("final", holder.subTarget.finalField);
        assertEquals("static", SubTarget.staticField);
    }

    // Tests exception during target field instantiation
    @Test(expected = MockitoException.class)
    public void testProcessInjection_throwingConstructor_throwsMockitoException() throws Exception {
        Holder holder = new Holder();
        Field field = Holder.class.getDeclaredField("throwingTarget");
        Set<Object> mocks = new HashSet<Object>();
        mocks.add("someMock");

        injection.processInjection(field, holder, mocks);
    }
}
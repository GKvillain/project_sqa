package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PropertyAndSetterInjectionTest {

    private PropertyAndSetterInjection injection;

    @Before
    public void setUp() {
        injection = new PropertyAndSetterInjection();
    }

    // Tests successful field injection with a single matching mock candidate
    @Test
    public void testProcessInjection_singleMatchingCandidate_injectsFieldAndReturnsTrue() throws Exception {
        UnderTestHolder holder = new UnderTestHolder();
        holder.target = new Target();
        Field injectMocksField = UnderTestHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.dependency);
    }

    // Tests setter injection when a matching setter method exists
    @Test
    public void testProcessInjection_matchingSetter_injectsViaSetterAndReturnsTrue() throws Exception {
        UnderTestHolderWithSetter holder = new UnderTestHolderWithSetter();
        holder.target = new TargetWithSetter();
        Field injectMocksField = UnderTestHolderWithSetter.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.getDependency());
        assertTrue(holder.target.setterCalled);
    }

    // Tests behavior when candidate set is empty
    @Test
    public void testProcessInjection_emptyCandidates_returnsFalse() throws Exception {
        UnderTestHolder holder = new UnderTestHolder();
        holder.target = new Target();
        Field injectMocksField = UnderTestHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertFalse(result);
        assertNull(holder.target.dependency);
    }

    // Tests behavior when no candidate matches the target field type
    @Test
    public void testProcessInjection_noMatchingType_returnsFalse() throws Exception {
        UnderTestHolder holder = new UnderTestHolder();
        holder.target = new Target();
        Field injectMocksField = UnderTestHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        candidates.add("nonMatchingString");

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertFalse(result);
        assertNull(holder.target.dependency);
    }

    // Tests initialization of target instance when target field is null
    @Test
    public void testProcessInjection_nullTargetInstance_initializesInstanceAndInjects() throws Exception {
        UnderTestHolder holder = new UnderTestHolder();
        holder.target = null;
        Field injectMocksField = UnderTestHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertNotNull(holder.target);
        assertSame(dep, holder.target.dependency);
    }

    // Tests injection across class hierarchy (super class and sub class)
    @Test
    public void testProcessInjection_classHierarchy_injectsSubAndSuperClassFields() throws Exception {
        UnderTestSubHolder holder = new UnderTestSubHolder();
        holder.target = new SubTarget();
        Field injectMocksField = UnderTestSubHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        OtherDependency otherDep = new OtherDependency();
        candidates.add(dep);
        candidates.add(otherDep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.dependency);
        assertSame(otherDep, holder.target.otherDependency);
    }

    // Tests filtering of static and final fields to ensure they are not injected
    @Test
    public void testProcessInjection_staticAndFinalFields_ignoresStaticAndFinal() throws Exception {
        UnderTestStaticFinalHolder holder = new UnderTestStaticFinalHolder();
        holder.target = new TargetWithStaticAndFinal();
        Field injectMocksField = UnderTestStaticFinalHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertFalse(result);
        assertNull(holder.target.getFinalDependency());
    }

    // Tests name-based resolution when multiple candidates have the same type
    @Test
    public void testProcessInjection_multipleCandidatesSameType_injectsByName() throws Exception {
        UnderTestNamedHolder holder = new UnderTestNamedHolder();
        holder.target = new TargetWithNamedFields();
        Field injectMocksField = UnderTestNamedHolder.class.getDeclaredField("target");

        Dependency firstDep = new Dependency();
        Dependency secondDep = new Dependency();

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(firstDep);
        candidates.add(secondDep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
    }

    // Tests exception handling when target instantiation fails
    @Test(expected = MockitoException.class)
    public void testProcessInjection_instantiationFailsWithException_throwsMockitoException() throws Exception {
        UnderTestThrowingHolder holder = new UnderTestThrowingHolder();
        Field injectMocksField = UnderTestThrowingHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new Dependency());

        injection.processInjection(injectMocksField, holder, candidates);
    }

    // Tests that an injected mock is not reused for another field in the same class
    @Test
    public void testProcessInjection_singleCandidateTwoFields_injectsOnlyOnce() throws Exception {
        UnderTestTwoFieldsHolder holder = new UnderTestTwoFieldsHolder();
        holder.target = new TargetWithTwoFieldsOfSameType();
        Field injectMocksField = UnderTestTwoFieldsHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertTrue((holder.target.firstDep == dep && holder.target.secondDep == null)
                || (holder.target.firstDep == null && holder.target.secondDep == dep));
    }

    // Tests failure when target is an interface and cannot be instantiated
    @Test(expected = MockitoException.class)
    public void testProcessInjection_nullTargetInterface_throwsMockitoException() throws Exception {
        UnderTestInterfaceHolder holder = new UnderTestInterfaceHolder();
        Field injectMocksField = UnderTestInterfaceHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new Dependency());

        injection.processInjection(injectMocksField, holder, candidates);
    }

    // Tests failure when target is an abstract class and cannot be instantiated
    @Test(expected = MockitoException.class)
    public void testProcessInjection_nullTargetAbstractClass_throwsMockitoException() throws Exception {
        UnderTestAbstractHolder holder = new UnderTestAbstractHolder();
        Field injectMocksField = UnderTestAbstractHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new Dependency());

        injection.processInjection(injectMocksField, holder, candidates);
    }

    // Tests exception propagation when setter throws an exception
    @Test(expected = MockitoException.class)
    public void testProcessInjection_setterThrowsException_throwsMockitoException() throws Exception {
        UnderTestHolderWithThrowingSetter holder = new UnderTestHolderWithThrowingSetter();
        holder.target = new TargetWithThrowingSetter();
        Field injectMocksField = UnderTestHolderWithThrowingSetter.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new Dependency());

        injection.processInjection(injectMocksField, holder, candidates);
    }

    // Tests fallback to field injection when setter signature does not match
    @Test
    public void testProcessInjection_setterWithInvalidSignature_fallsBackToFieldInjection() throws Exception {
        UnderTestHolderWithInvalidSetter holder = new UnderTestHolderWithInvalidSetter();
        holder.target = new TargetWithInvalidSetter();
        Field injectMocksField = UnderTestHolderWithInvalidSetter.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.dependency);
    }

    // Tests successful injection on an already-instantiated target without a default constructor
    @Test
    public void testProcessInjection_targetAlreadyInstantiatedWithoutDefaultConstructor_injectsSuccessfully() throws Exception {
        UnderTestNoDefaultConstructorHolder holder = new UnderTestNoDefaultConstructorHolder();
        holder.target = new TargetWithoutDefaultConstructor("preset");
        Field injectMocksField = UnderTestNoDefaultConstructorHolder.class.getDeclaredField("target");

        Set<Object> candidates = new HashSet<Object>();
        Dependency dep = new Dependency();
        candidates.add(dep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.dependency);
    }

    // Tests injection through a 3-level class hierarchy (Grandparent -> Parent -> Child)
    @Test
    public void testProcessInjection_threeLevelHierarchy_injectsAllLevels() throws Exception {
        UnderThreeLevelHolder holder = new UnderThreeLevelHolder();
        holder.target = new ChildTarget();
        Field injectMocksField = UnderThreeLevelHolder.class.getDeclaredField("target");

        Dependency dep = new Dependency();
        OtherDependency otherDep = new OtherDependency();
        ThirdDependency thirdDep = new ThirdDependency();

        Set<Object> candidates = new HashSet<Object>();
        candidates.add(dep);
        candidates.add(otherDep);
        candidates.add(thirdDep);

        boolean result = injection.processInjection(injectMocksField, holder, candidates);

        assertTrue(result);
        assertSame(dep, holder.target.dependency);
        assertSame(otherDep, holder.target.otherDependency);
        assertSame(thirdDep, holder.target.thirdDependency);
    }

    // --- Helper classes for testing ---

    public static class Dependency {}

    public static class OtherDependency {}

    public static class ThirdDependency {}

    public static class Target {
        Dependency dependency;
    }

    public static class UnderTestHolder {
        Target target;
    }

    public static class TargetWithSetter {
        private Dependency dependency;
        boolean setterCalled = false;

        public void setDependency(Dependency dependency) {
            this.dependency = dependency;
            this.setterCalled = true;
        }

        public Dependency getDependency() {
            return dependency;
        }
    }

    public static class UnderTestHolderWithSetter {
        TargetWithSetter target;
    }

    public static class SuperTarget {
        Dependency dependency;
    }

    public static class SubTarget extends SuperTarget {
        OtherDependency otherDependency;
    }

    public static class UnderTestSubHolder {
        SubTarget target;
    }

    public static class TargetWithStaticAndFinal {
        static Dependency staticDep;
        final Dependency finalDep = null;

        public Dependency getFinalDependency() {
            return finalDep;
        }
    }

    public static class UnderTestStaticFinalHolder {
        TargetWithStaticAndFinal target;
    }

    public static class TargetWithNamedFields {
        Dependency first;
        Dependency second;
    }

    public static class UnderTestNamedHolder {
        TargetWithNamedFields target;
    }

    public static class TargetThrowingException {
        public TargetThrowingException() {
            throw new RuntimeException("Constructor failure");
        }
    }

    public static class UnderTestThrowingHolder {
        TargetThrowingException target;
    }

    public static class TargetWithTwoFieldsOfSameType {
        Dependency firstDep;
        Dependency secondDep;
    }

    public static class UnderTestTwoFieldsHolder {
        TargetWithTwoFieldsOfSameType target;
    }

    public interface InterfaceTarget {}

    public static class UnderTestInterfaceHolder {
        InterfaceTarget target;
    }

    public static abstract class AbstractTarget {
        Dependency dependency;
    }

    public static class UnderTestAbstractHolder {
        AbstractTarget target;
    }

    public static class TargetWithThrowingSetter {
        Dependency dependency;

        public void setDependency(Dependency dependency) {
            throw new RuntimeException("Setter invocation failed");
        }
    }

    public static class UnderTestHolderWithThrowingSetter {
        TargetWithThrowingSetter target;
    }

    public static class TargetWithInvalidSetter {
        Dependency dependency;

        public void setDependency() {
        }

        public void setDependency(Dependency dep, String extra) {
        }
    }

    public static class UnderTestHolderWithInvalidSetter {
        TargetWithInvalidSetter target;
    }

    public static class TargetWithoutDefaultConstructor {
        Dependency dependency;
        final String name;

        public TargetWithoutDefaultConstructor(String name) {
            this.name = name;
        }
    }

    public static class UnderTestNoDefaultConstructorHolder {
        TargetWithoutDefaultConstructor target;
    }

    public static class GrandparentTarget {
        Dependency dependency;
    }

    public static class ParentTarget extends GrandparentTarget {
        OtherDependency otherDependency;
    }

    public static class ChildTarget extends ParentTarget {
        ThirdDependency thirdDependency;
    }

    public static class UnderThreeLevelHolder {
        ChildTarget target;
    }
}
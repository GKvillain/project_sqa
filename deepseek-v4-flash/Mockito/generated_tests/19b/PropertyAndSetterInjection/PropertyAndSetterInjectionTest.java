package org.mockito.internal.configuration.injection;

import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.*;
import org.mockito.exceptions.base.MockitoException;

public class PropertyAndSetterInjectionTest {

    // -------- helper types for testing ----------
    private static interface FooService {}
    private static class FooServiceImpl implements FooService {}
    private static interface BarService {}
    private static class BarServiceImpl implements BarService {}
    private static class AnotherService {}
    private static class AnotherServiceImpl extends AnotherService {}

    // target bean with private fields: one setter, one field-only, one setter
    private static class TargetBean {
        private FooService foo;
        private BarService bar;   // no setter -> field injection
        private AnotherService another;
        public void setFoo(FooService foo) { this.foo = foo; }
        public FooService getFoo() { return foo; }
        public BarService getBar() { return bar; }
        public void setAnother(AnotherService a) { this.another = a; }
        public AnotherService getAnother() { return another; }
    }

    // subclass to test hierarchy
    private static class SubTargetBean extends TargetBean {
        private String extra;
        public void setExtra(String extra) { this.extra = extra; }
        public String getExtra() { return extra; }
    }

    // owners for each scenario
    private static class Owner {
        public TargetBean target = null;
    }
    private static class OwnerWithSubTarget {
        public SubTargetBean subTarget = null;
    }
    private static class OwnerWithFinalField {
        public BeanWithFinalField finalTarget = null;
    }
    private static class BeanWithFinalField {
        private final String finalField = "initial";
        public String getFinalField() { return finalField; }
    }
    private static class OwnerWithStaticField {
        public BeanWithStaticField staticTarget = null;
    }
    private static class BeanWithStaticField {
        private static String staticField = "static";
        public String getStaticField() { return staticField; }
    }
    private static class OwnerWithAbstractField {
        public AbstractClass abstractTarget = null;
    }
    private static abstract class AbstractClass {
        public abstract void doSomething();
    }
    private static class OwnerWithObjectField {
        public Object obj = null;
    }

    private PropertyAndSetterInjection injector = new PropertyAndSetterInjection();

    // ---- tests ----

    // Normal: no candidates -> false, object created, nothing injected
    @Test
    public void testProcessInjection_noCandidates_returnsFalse() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertFalse(result);
        assertNotNull(owner.target);
        assertNull(owner.target.getFoo());
        assertNull(owner.target.getBar());
    }

    // Normal: one candidate matching setter field type
    @Test
    public void testProcessInjection_normalInjectionViaSetter_returnsTrueAndInjects() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new FooServiceImpl());
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertTrue(result);
        assertNotNull(owner.target);
        assertNotNull(owner.target.getFoo()); // injected via setter
        assertNull(owner.target.getBar());    // no candidate for bar
    }

    // Normal: one candidate matching field-only (no setter) -> field injection
    @Test
    public void testProcessInjection_normalInjectionViaField_returnsTrueAndInjects() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new BarServiceImpl());
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertTrue(result);
        assertNotNull(owner.target);
        assertNull(owner.target.getFoo());   // no candidate for foo
        assertNotNull(owner.target.getBar()); // injected directly into field
    }

    // Normal: multiple candidates matching different fields
    @Test
    public void testProcessInjection_multipleCandidatesTypeMatch_returnsTrueAndInjectsCorrect() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new FooServiceImpl());
        candidates.add(new BarServiceImpl());
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertTrue(result);
        assertNotNull(owner.target.getFoo());
        assertNotNull(owner.target.getBar());
    }

    // Normal: subclass hierarchy – inject both superclass and subclass fields
    @Test
    public void testProcessInjection_inheritedFields_injectsBothLevels() throws Exception {
        OwnerWithSubTarget owner = new OwnerWithSubTarget();
        Field targetField = OwnerWithSubTarget.class.getDeclaredField("subTarget");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new FooServiceImpl());
        candidates.add(new BarServiceImpl());
        candidates.add(new AnotherServiceImpl());
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertTrue(result);
        assertNotNull(owner.subTarget);
        // superclass fields
        assertNotNull(owner.subTarget.getFoo());
        assertNotNull(owner.subTarget.getBar());
        // subclass settable field
        assertNotNull(owner.subTarget.getAnother());
        // no candidate of type String -> extra not injected
        assertNull(owner.subTarget.getExtra());
    }

    // Edge: final field should be filtered out
    @Test
    public void testProcessInjection_finalField_skippedAndNotInjected() throws Exception {
        OwnerWithFinalField owner = new OwnerWithFinalField();
        Field targetField = OwnerWithFinalField.class.getDeclaredField("finalTarget");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add("someString");
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertFalse(result);
        assertNotNull(owner.finalTarget);
        assertEquals("initial", owner.finalTarget.getFinalField()); // unchanged
    }

    // Edge: static field should be filtered out
    @Test
    public void testProcessInjection_staticField_skippedAndNotInjected() throws Exception {
        OwnerWithStaticField owner = new OwnerWithStaticField();
        Field targetField = OwnerWithStaticField.class.getDeclaredField("staticTarget");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add("newStaticValue");
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertFalse(result);
        assertNotNull(owner.staticTarget);
        assertEquals("static", owner.staticTarget.getStaticField());
    }

    // Invalid: field type cannot be instantiated (abstract) -> fieldInitializer throws
    // The catch block returns null report, causing NPE downstream
    @Test(expected = NullPointerException.class)
    public void testProcessInjection_fieldInitializerThrowsException_throwsNullPointerException() throws Exception {
        OwnerWithAbstractField owner = new OwnerWithAbstractField();
        Field targetField = OwnerWithAbstractField.class.getDeclaredField("abstractTarget");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add("anything");
        // This should produce NPE because report is null
        injector.processInjection(targetField, owner, candidates);
    }

    // Edge: candidate type does not match any field -> no injection
    @Test
    public void testProcessInjection_noMatchingCandidates_returnsFalse() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(Integer.valueOf(42)); // not assignable to any field
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertFalse(result);
        assertNotNull(owner.target);
        assertNull(owner.target.getFoo());
        assertNull(owner.target.getBar());
    }

    // Edge: field type Object has no fields -> injection not possible
    @Test
    public void testProcessInjection_fieldTypeObject_noInjection() throws Exception {
        OwnerWithObjectField owner = new OwnerWithObjectField();
        Field targetField = OwnerWithObjectField.class.getDeclaredField("obj");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add("something");
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertFalse(result);
        assertNotNull(owner.obj);
    }

    // Normal: multiple candidates of same type – one is injected via type then name filter
    @Test
    public void testProcessInjection_multipleCandidatesSameType_returnsTrueAndInjectsOne() throws Exception {
        Owner owner = new Owner();
        Field targetField = Owner.class.getDeclaredField("target");
        targetField.setAccessible(true);
        Set<Object> candidates = new HashSet<Object>();
        candidates.add(new FooServiceImpl());
        candidates.add(new FooServiceImpl()); // same type, different instances
        boolean result = injector.processInjection(targetField, owner, candidates);
        assertTrue(result);
        assertNotNull(owner.target.getFoo()); // only one should be injected
        // bar remains null because no BarService candidate
        assertNull(owner.target.getBar());
    }
}
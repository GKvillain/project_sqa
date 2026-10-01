package org.mockito.internal.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class DefaultInjectionEngineTest {

    private DefaultInjectionEngine engine;

    @Before
    public void setUp() {
        engine = new DefaultInjectionEngine();
    }

    public static class Foo {}
    public static class Bar {}
    public static class Baz {}

    public static class FooBarTarget {
        Foo foo;
        Bar bar;
    }

    public static class Holder {
        FooBarTarget target;
    }

    public static class ParentTarget {
        Foo foo;
    }

    public static class ChildTarget extends ParentTarget {
        Bar bar;
    }

    public static class ChildHolder {
        ChildTarget target;
    }

    public static class MultipleHolder {
        FooBarTarget first;
        FooBarTarget second;
    }

    // Comparator fixtures
    public static class Base {}
    public static class Sub extends Base {}

    public static class TypeFields {
        Base base;
        Sub sub;
    }

    public static class SameTypeFields {
        String first;
        String second;
    }

    public static class UnrelatedFields {
        String text;
        Integer number;
    }

    private Field field(Class<?> clazz, String name) throws Exception {
        return clazz.getDeclaredField(name);
    }

    private Set<Object> mocks(Object... objects) {
        return new HashSet<Object>(Arrays.asList(objects));
    }

    private Set<Field> fieldSet(Field... fields) {
        return new HashSet<Field>(Arrays.asList(fields));
    }

    @SuppressWarnings("unchecked")
    private Comparator<Field> supertypesLastComparator() throws Exception {
        Field comparatorField = DefaultInjectionEngine.class.getDeclaredField("supertypesLast");
        comparatorField.setAccessible(true);
        return (Comparator<Field>) comparatorField.get(engine);
    }

    // Verifies normal type-based injection.
    @Test
    public void testInjectMocksOnFields_singleMatchingMock_injectsMockIntoField() throws Exception {
        Foo foo = new Foo();
        Bar bar = new Bar();
        Holder holder = new Holder();
        holder.target = new FooBarTarget();

        engine.injectMocksOnFields(
                Collections.singleton(field(Holder.class, "target")),
                mocks(foo, bar),
                holder);

        assertSame(foo, holder.target.foo);
        assertSame(bar, holder.target.bar);
    }

    // Verifies that an empty mock set leaves fields untouched.
    @Test
    public void testInjectMocksOnFields_emptyMocks_leavesFieldsNull() throws Exception {
        Holder holder = new Holder();
        holder.target = new FooBarTarget();

        engine.injectMocksOnFields(
                Collections.singleton(field(Holder.class, "target")),
                Collections.<Object>emptySet(),
                holder);

        assertNull(holder.target.foo);
        assertNull(holder.target.bar);
    }

    // Verifies that non-matching mock types are not injected.
    @Test
    public void testInjectMocksOnFields_noMatchingMock_leavesFieldNull() throws Exception {
        Holder holder = new Holder();
        holder.target = new FooBarTarget();

        engine.injectMocksOnFields(
                Collections.singleton(field(Holder.class, "target")),
                mocks(new Baz()),
                holder);

        assertNull(holder.target.foo);
        assertNull(holder.target.bar);
    }

    // Verifies that an already initialized @InjectMocks field is retained.
    @Test
    public void testInjectMocksOnFields_existingInjectMocksInstance_injectsIntoSameInstance() throws Exception {
        Foo foo = new Foo();
        Bar bar = new Bar();
        Holder holder = new Holder();
        FooBarTarget target = new FooBarTarget();
        holder.target = target;

        engine.injectMocksOnFields(
                Collections.singleton(field(Holder.class, "target")),
                mocks(foo, bar),
                holder);

        assertSame(target, holder.target);
        assertSame(foo, holder.target.foo);
        assertSame(bar, holder.target.bar);
    }

    // Verifies that a null @InjectMocks field is initialized before injection.
    @Test
    public void testInjectMocksOnFields_nullInjectMocksInstance_initializesAndInjects() throws Exception {
        Foo foo = new Foo();
        Bar bar = new Bar();
        Holder holder = new Holder();

        engine.injectMocksOnFields(
                Collections.singleton(field(Holder.class, "target")),
                mocks(foo, bar),
                holder);

        assertNotNull(holder.target);
        assertSame(foo, holder.target.foo);
        assertSame(bar, holder.target.bar);
    }

    // Verifies injection into fields declared in the superclass.
    @Test
    public void testInjectMocksOnFields_fieldsInSuperclass_injectsIntoSuperclassFields() throws Exception {
        Foo foo = new Foo();
        Bar bar = new Bar();
        ChildHolder holder = new ChildHolder();
        holder.target = new ChildTarget();

        engine.injectMocksOnFields(
                Collections.singleton(field(ChildHolder.class, "target")),
                mocks(foo, bar),
                holder);

        assertSame(foo, holder.target.foo);
        assertSame(bar, holder.target.bar);
    }

    // Verifies that multiple @InjectMocks fields are all handled.
    @Test
    public void testInjectMocksOnFields_multipleInjectMocksFields_injectsBoth() throws Exception {
        Foo foo = new Foo();
        Bar bar = new Bar();
        MultipleHolder holder = new MultipleHolder();
        holder.first = new FooBarTarget();
        holder.second = new FooBarTarget();

        engine.injectMocksOnFields(
                fieldSet(
                        field(MultipleHolder.class, "first"),
                        field(MultipleHolder.class, "second")),
                mocks(foo, bar),
                holder);

        assertSame(foo, holder.first.foo);
        assertSame(bar, holder.first.bar);
        assertSame(foo, holder.second.foo);
        assertSame(bar, holder.second.bar);
    }

    // Tests the false branch of the same-type comparator condition.
    @Test
    public void testCompare_sameType_returnsZero() throws Exception {
        Comparator<Field> comparator = supertypesLastComparator();
        Field first = field(SameTypeFields.class, "first");
        Field second = field(SameTypeFields.class, "second");

        assertEquals(0, comparator.compare(first, second));
        assertEquals(0, comparator.compare(second, first));
    }

    // Tests the true branch where the first field type is assignable from the second.
    @Test
    public void testCompare_superTypeBeforeSubType_returnsPositive() throws Exception {
        Comparator<Field> comparator = supertypesLastComparator();
        Field base = field(TypeFields.class, "base");
        Field sub = field(TypeFields.class, "sub");

        assertTrue(comparator.compare(base, sub) > 0);
    }

    // Tests the true branch where the second field type is assignable from the first.
    @Test
    public void testCompare_subTypeBeforeSuperType_returnsNegative() throws Exception {
        Comparator<Field> comparator = supertypesLastComparator();
        Field base = field(TypeFields.class, "base");
        Field sub = field(TypeFields.class, "sub");

        assertTrue(comparator.compare(sub, base) < 0);
    }

    // Tests unrelated field types: neither type is assignable from the other.
    @Test
    public void testCompare_unrelatedTypes_returnsZero() throws Exception {
        Comparator<Field> comparator = supertypesLastComparator();
        Field text = field(UnrelatedFields.class, "text");
        Field number = field(UnrelatedFields.class, "number");

        assertEquals(0, comparator.compare(text, number));
    }
}
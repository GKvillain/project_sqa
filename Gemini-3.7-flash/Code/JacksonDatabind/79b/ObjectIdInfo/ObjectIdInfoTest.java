package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.PropertyName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectIdInfoTest {

    // Tests constructor with 4 parameters (PropertyName, Scope, Generator, Resolver)
    @Test
    public void testConstructor_withExplicitResolver_initializesCorrectly() {
        PropertyName propName = new PropertyName("id");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.IntSequenceGenerator.class;
        Class<? extends ObjectIdResolver> resolver = SimpleObjectIdResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, resolver);

        assertEquals(propName, info.getPropertyName());
        assertEquals(scope, info.getScope());
        assertEquals(gen, info.getGeneratorType());
        assertEquals(resolver, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests constructor with null resolver defaulting to SimpleObjectIdResolver
    @Test
    public void testConstructor_withNullResolver_defaultsToSimpleObjectIdResolver() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, null);

        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests deprecated 3-arg constructor with PropertyName
    @Test
    @SuppressWarnings("deprecation")
    public void testConstructor_withPropertyNameScopeAndGenerator_defaultsCorrectly() {
        PropertyName propName = new PropertyName("customId");
        ObjectIdInfo info = new ObjectIdInfo(propName, Integer.class, ObjectIdGenerators.PropertyGenerator.class);

        assertEquals(propName, info.getPropertyName());
        assertEquals(Integer.class, info.getScope());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests deprecated 3-arg constructor with String name
    @Test
    @SuppressWarnings("deprecation")
    public void testConstructor_withStringScopeAndGenerator_createsPropertyName() {
        ObjectIdInfo info = new ObjectIdInfo("strId", Long.class, ObjectIdGenerators.UUIDGenerator.class);

        assertNotNull(info.getPropertyName());
        assertEquals("strId", info.getPropertyName().getSimpleName());
        assertEquals(Long.class, info.getScope());
        assertEquals(ObjectIdGenerators.UUIDGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests protected 4-arg constructor with alwaysAsId flag
    @Test
    public void testConstructor_withAlwaysAsIdTrue_setsAlwaysAsId() {
        PropertyName propName = new PropertyName("itemId");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, true);

        assertEquals(propName, info.getPropertyName());
        assertEquals(Object.class, info.getScope());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertTrue(info.getAlwaysAsId());
    }

    // Tests withAlwaysAsId when state changes from false to true
    @Test
    public void testWithAlwaysAsId_stateChanged_returnsNewInstance() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo original = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, false);

        ObjectIdInfo updated = original.withAlwaysAsId(true);

        assertNotSame(original, updated);
        assertTrue(updated.getAlwaysAsId());
        assertEquals(propName, updated.getPropertyName());
        assertEquals(String.class, updated.getScope());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, updated.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, updated.getResolverType());
    }

    // Tests withAlwaysAsId when state does not change (same instance returned)
    @Test
    public void testWithAlwaysAsId_sameState_returnsSameInstance() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), String.class, ObjectIdGenerators.IntSequenceGenerator.class, false);

        ObjectIdInfo same = info.withAlwaysAsId(false);

        assertSame(info, same);
    }

    // Tests toString representation with non-null values
    @Test
    public void testToString_validFields_formatsCorrectString() {
        PropertyName propName = new PropertyName("userId");
        ObjectIdInfo info = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, true);

        String str = info.toString();

        assertTrue(str.contains("propName=userId"));
        assertTrue(str.contains("scope=java.lang.String"));
        assertTrue(str.contains("generatorType=" + ObjectIdGenerators.IntSequenceGenerator.class.getName()));
        assertTrue(str.contains("alwaysAsId=true"));
    }

    // Tests toString representation with null scope and generator
    @Test
    public void testToString_nullScopeAndGenerator_formatsNullValues() {
        PropertyName propName = new PropertyName("userId");
        ObjectIdInfo info = new ObjectIdInfo(propName, null, null, false);

        String str = info.toString();

        assertTrue(str.contains("propName=userId"));
        assertTrue(str.contains("scope=null"));
        assertTrue(str.contains("generatorType=null"));
        assertTrue(str.contains("alwaysAsId=false"));
    }

    // Tests 5-arg constructor with explicit alwaysAsId and resolver
    @Test
    public void testConstructor_5Args_initializesAllFields() {
        PropertyName propName = new PropertyName("fullId");
        Class<?> scope = Double.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.StringIdGenerator.class;
        Class<? extends ObjectIdResolver> resolver = SimpleObjectIdResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true, resolver);

        assertEquals(propName, info.getPropertyName());
        assertEquals(scope, info.getScope());
        assertEquals(gen, info.getGeneratorType());
        assertTrue(info.getAlwaysAsId());
        assertEquals(resolver, info.getResolverType());
    }

    // Tests 5-arg constructor with null resolver defaulting to SimpleObjectIdResolver
    @Test
    public void testConstructor_5ArgsWithNullResolver_defaultsToSimpleObjectIdResolver() {
        PropertyName propName = new PropertyName("fullId");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, false, null);

        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests static empty() factory method
    @Test
    public void testEmpty_returnsSingletonEmptyInstance() {
        ObjectIdInfo empty = ObjectIdInfo.empty();

        assertNotNull(empty);
        assertSame(empty, ObjectIdInfo.empty());
        assertNull(empty.getPropertyName());
        assertEquals(Object.class, empty.getScope());
        assertNull(empty.getGeneratorType());
        assertFalse(empty.getAlwaysAsId());
        assertEquals(SimpleObjectIdResolver.class, empty.getResolverType());
    }

    // Tests toString representation with null property name
    @Test
    public void testToString_nullPropertyName_formatsNullPropName() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, null, null, false, null);

        String str = info.toString();

        assertTrue(str.contains("propName=null"));
        assertTrue(str.contains("scope=null"));
        assertTrue(str.contains("generatorType=null"));
        assertTrue(str.contains("alwaysAsId=false"));
    }
}
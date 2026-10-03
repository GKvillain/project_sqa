package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.PropertyName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectIdInfoTest {

    // Tests public 5-arg constructor with non-null resolver
    @Test
    public void testConstructor_FullParams_DefaultResolver() {
        PropertyName name = new PropertyName("id");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerator.class;
        Class<? extends ObjectIdResolver> resolver = SimpleObjectIdResolver.class;
        ObjectIdInfo info = new ObjectIdInfo(name, scope, gen, resolver);
        assertEquals(name, info.getPropertyName());
        assertEquals(scope, info.getScope());
        assertEquals(gen, info.getGeneratorType());
        assertEquals(resolver, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests null resolver -> uses SimpleObjectIdResolver
    @Test
    public void testConstructor_NullResolver_UsesSimple() {
        PropertyName name = new PropertyName("id");
        Class<?> scope = Object.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerator.class;
        ObjectIdInfo info = new ObjectIdInfo(name, scope, gen, null);
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests protected 5-arg constructor with alwaysAsId=true
    @Test
    public void testConstructor_WithAlwaysAsIdTrue_ReturnsTrue() {
        PropertyName name = new PropertyName("id");
        Class<?> scope = Object.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerator.class;
        ObjectIdInfo info = new ObjectIdInfo(name, scope, gen, true, SimpleObjectIdResolver.class);
        assertTrue(info.getAlwaysAsId());
    }

    // Tests protected 5-arg constructor with alwaysAsId=false
    @Test
    public void testConstructor_WithAlwaysAsIdFalse_ReturnsFalse() {
        PropertyName name = new PropertyName("id");
        Class<?> scope = Object.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerator.class;
        ObjectIdInfo info = new ObjectIdInfo(name, scope, gen, false, SimpleObjectIdResolver.class);
        assertFalse(info.getAlwaysAsId());
    }

    // Tests deprecated 3-arg constructor with String name
    @Test
    public void testConstructor_DeprecatedStringName_PropertyNameCreated() {
        ObjectIdInfo info = new ObjectIdInfo("myId", Integer.class, ObjectIdGenerator.class);
        assertEquals(new PropertyName("myId"), info.getPropertyName());
        assertEquals(Integer.class, info.getScope());
        assertEquals(ObjectIdGenerator.class, info.getGeneratorType());
    }

    // Tests deprecated 4-arg constructor with PropertyName (no resolver)
    @Test
    public void testConstructor_DeprecatedPropertyName_NoResolver() {
        PropertyName name = new PropertyName("deprecated");
        ObjectIdInfo info = new ObjectIdInfo(name, Long.class, ObjectIdGenerator.class);
        assertEquals(name, info.getPropertyName());
        assertEquals(Long.class, info.getScope());
        assertEquals(ObjectIdGenerator.class, info.getGeneratorType());
        // resolver defaults to SimpleObjectIdResolver
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests withAlwaysAsId when state equals current -> returns same instance
    @Test
    public void testWithAlwaysAsId_SameState_ReturnsThis() {
        PropertyName name = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(name, null, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        ObjectIdInfo result = info.withAlwaysAsId(false);
        assertSame(info, result);
    }

    // Tests withAlwaysAsId when state differs -> returns new instance with new state
    @Test
    public void testWithAlwaysAsId_DifferentState_ReturnsNew() {
        PropertyName name = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(name, null, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        ObjectIdInfo result = info.withAlwaysAsId(true);
        assertNotSame(info, result);
        assertTrue(result.getAlwaysAsId());
        // other fields should remain same
        assertEquals(name, result.getPropertyName());
        assertNull(result.getScope());
        assertEquals(ObjectIdGenerator.class, result.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, result.getResolverType());
    }

    // Tests getters: getPropertyName, getScope, getGeneratorType, getResolverType, getAlwaysAsId
    @Test
    public void testGetters_CorrectValues() {
        PropertyName name = new PropertyName("testProp");
        Class<?> scope = Double.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerator.class;
        Class<? extends ObjectIdResolver> resolver = SimpleObjectIdResolver.class;
        ObjectIdInfo info = new ObjectIdInfo(name, scope, gen, true, resolver);

        assertEquals(name, info.getPropertyName());
        assertEquals(scope, info.getScope());
        assertEquals(gen, info.getGeneratorType());
        assertEquals(resolver, info.getResolverType());
        assertTrue(info.getAlwaysAsId());
    }

    // Tests toString with all non-null fields
    @Test
    public void testToString_AllFieldsNonNull_ContainsExpected() {
        PropertyName name = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(name, String.class, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        String str = info.toString();
        assertTrue(str.contains("propName=id"));
        assertTrue(str.contains("scope=java.lang.String"));
        assertTrue(str.contains("generatorType=com.fasterxml.jackson.annotation.ObjectIdGenerator"));
        assertTrue(str.contains("alwaysAsId=false"));
    }

    // Tests toString when scope is null
    @Test
    public void testToString_ScopeNull_ShowsNull() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("x"), null, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        String str = info.toString();
        assertTrue(str.contains("scope=null"));
    }

    // Tests toString when generator is null
    @Test
    public void testToString_GeneratorNull_ShowsNull() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("x"), String.class, null, false, SimpleObjectIdResolver.class);
        String str = info.toString();
        assertTrue(str.contains("generatorType=null"));
    }

    // Tests constructor with null propertyName
    @Test
    public void testConstructor_NullPropertyName_GetterReturnsNull() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, String.class, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        assertNull(info.getPropertyName());
    }

    // Tests constructor with null scope
    @Test
    public void testConstructor_NullScope_GetterReturnsNull() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), null, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        assertNull(info.getScope());
    }

    // Tests constructor with null generator
    @Test
    public void testConstructor_NullGenerator_GetterReturnsNull() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), String.class, null, false, SimpleObjectIdResolver.class);
        assertNull(info.getGeneratorType());
    }

    // Tests deprecated 4-arg constructor with true alwaysAsId (via protected)
    // This directly exercises the protected constructor that bridges to the 5-arg
    @Test
    public void testConstructor_ProtectedBoolean_True() {
        PropertyName name = new PropertyName("b");
        ObjectIdInfo info = new ObjectIdInfo(name, null, ObjectIdGenerator.class, true);
        assertTrue(info.getAlwaysAsId());
    }

    // Tests deprecated 4-arg constructor with false alwaysAsId
    @Test
    public void testConstructor_ProtectedBoolean_False() {
        PropertyName name = new PropertyName("b");
        ObjectIdInfo info = new ObjectIdInfo(name, null, ObjectIdGenerator.class, false);
        assertFalse(info.getAlwaysAsId());
        // resolver defaults to SimpleObjectIdResolver
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // ========== New tests covering previously uncovered parts ==========

    // Tests default resolver in deprecated 3-arg String constructor
    @Test
    public void testConstructor_DeprecatedStringName_ResolverDefault() {
        ObjectIdInfo info = new ObjectIdInfo("testId", Integer.class, ObjectIdGenerator.class);
        // resolver should default to SimpleObjectIdResolver
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests that deprecated 4-arg boolean constructor with true also uses SimpleObjectIdResolver
    @Test
    public void testConstructor_ProtectedBoolean_True_ResolverDefault() {
        PropertyName name = new PropertyName("b");
        ObjectIdInfo info = new ObjectIdInfo(name, null, ObjectIdGenerator.class, true);
        // alwaysAsId is true, resolver should be SimpleObjectIdResolver
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests toString when propertyName is null
    @Test
    public void testToString_NullPropertyName() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, String.class, ObjectIdGenerator.class, false, SimpleObjectIdResolver.class);
        String str = info.toString();
        assertTrue(str.contains("propName=null"));
    }

    // Tests toString when alwaysAsId is true
    @Test
    public void testToString_AlwaysAsIdTrue() {
        PropertyName name = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(name, String.class, ObjectIdGenerator.class, true, SimpleObjectIdResolver.class);
        String str = info.toString();
        assertTrue(str.contains("alwaysAsId=true"));
    }
}
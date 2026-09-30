package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.PropertyName;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectIdInfoTest {

    // Tests 4-arg constructor with custom resolver and default alwaysAsId (false)
    @Test
    public void testConstructor_withResolver_initializesCorrectly() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);

        assertEquals(propName, info.getPropertyName());
        assertEquals(String.class, info.getScope());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests constructor when resolver is null, verifying fallback to SimpleObjectIdResolver
    @Test
    public void testConstructor_nullResolver_defaultsToSimpleObjectIdResolver() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests deprecated 3-arg constructor with PropertyName
    @Test
    @SuppressWarnings("deprecation")
    public void testConstructor_deprecatedPropertyName_initializesCorrectly() {
        PropertyName propName = new PropertyName("customId");
        ObjectIdInfo info = new ObjectIdInfo(propName, Integer.class, ObjectIdGenerators.UUIDGenerator.class);

        assertEquals(propName, info.getPropertyName());
        assertEquals(Integer.class, info.getScope());
        assertEquals(ObjectIdGenerators.UUIDGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests deprecated 3-arg constructor with String name
    @Test
    @SuppressWarnings("deprecation")
    public void testConstructor_deprecatedStringName_initializesCorrectly() {
        ObjectIdInfo info = new ObjectIdInfo("strId", Long.class, ObjectIdGenerators.StringIdGenerator.class);

        assertEquals(new PropertyName("strId"), info.getPropertyName());
        assertEquals(Long.class, info.getScope());
        assertEquals(ObjectIdGenerators.StringIdGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    // Tests protected 4-arg constructor with alwaysAsId set to true
    @Test
    public void testConstructor_withAlwaysAsIdTrue_setsAlwaysAsIdTrue() {
        PropertyName propName = new PropertyName("alwaysId");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, true);

        assertTrue(info.getAlwaysAsId());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    // Tests withAlwaysAsId when the state is changed, verifying new instance creation
    @Test
    public void testWithAlwaysAsId_differentState_returnsNewInstance() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo original = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, false);
        ObjectIdInfo modified = original.withAlwaysAsId(true);

        assertNotSame(original, modified);
        assertTrue(modified.getAlwaysAsId());
        assertEquals(original.getPropertyName(), modified.getPropertyName());
        assertEquals(original.getScope(), modified.getScope());
        assertEquals(original.getGeneratorType(), modified.getGeneratorType());
        assertEquals(original.getResolverType(), modified.getResolverType());
    }

    // Tests withAlwaysAsId when the state is identical, verifying same instance is returned
    @Test
    public void testWithAlwaysAsId_sameState_returnsSameInstance() {
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), String.class, ObjectIdGenerators.IntSequenceGenerator.class, false);
        ObjectIdInfo result = info.withAlwaysAsId(false);

        assertSame(info, result);
    }

    // Tests toString output with non-null values
    @Test
    public void testToString_nonNullValues_returnsFormattedString() {
        PropertyName propName = new PropertyName("testProp");
        ObjectIdInfo info = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, true);
        String str = info.toString();

        assertTrue(str.contains("propName=testProp"));
        assertTrue(str.contains("scope=java.lang.String"));
        assertTrue(str.contains("generatorType=" + ObjectIdGenerators.IntSequenceGenerator.class.getName()));
        assertTrue(str.contains("alwaysAsId=true"));
    }

    // Tests toString output when scope and generator are null
    @Test
    public void testToString_nullScopeAndGenerator_handlesNullStrings() {
        ObjectIdInfo info = new ObjectIdInfo(null, null, null, false);
        String str = info.toString();

        assertTrue(str.contains("propName=null"));
        assertTrue(str.contains("scope=null"));
        assertTrue(str.contains("generatorType=null"));
        assertTrue(str.contains("alwaysAsId=false"));
    }

    // Tests static empty() factory method
    @Test
    public void testEmpty_returnsEmptyInstance() {
        ObjectIdInfo empty = ObjectIdInfo.empty();

        assertNotNull(empty);
        assertEquals(PropertyName.NO_NAME, empty.getPropertyName());
        assertEquals(Object.class, empty.getScope());
        assertNull(empty.getGeneratorType());
        assertFalse(empty.getAlwaysAsId());
        assertEquals(SimpleObjectIdResolver.class, empty.getResolverType());
    }

    // Tests 5-arg constructor explicitly
    @Test
    public void test5ArgConstructor_withAllParameters() {
        PropertyName propName = new PropertyName("fullProp");
        ObjectIdInfo info = new ObjectIdInfo(propName, Double.class, ObjectIdGenerators.UUIDGenerator.class, true, SimpleObjectIdResolver.class);

        assertEquals(propName, info.getPropertyName());
        assertEquals(Double.class, info.getScope());
        assertEquals(ObjectIdGenerators.UUIDGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertTrue(info.getAlwaysAsId());
    }

    // Tests withAlwaysAsId transitioning from true to false
    @Test
    public void testWithAlwaysAsId_fromTrueToFalse_returnsNewInstance() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo original = new ObjectIdInfo(propName, String.class, ObjectIdGenerators.IntSequenceGenerator.class, true);
        ObjectIdInfo modified = original.withAlwaysAsId(false);

        assertNotSame(original, modified);
        assertFalse(modified.getAlwaysAsId());
        assertEquals(original.getPropertyName(), modified.getPropertyName());
        assertEquals(original.getScope(), modified.getScope());
        assertEquals(original.getGeneratorType(), modified.getGeneratorType());
        assertEquals(original.getResolverType(), modified.getResolverType());
    }
}
package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorCollectorTest {

    static class SimpleBean {
        public SimpleBean() { }
        public SimpleBean(String s) { }
        public SimpleBean(int i) { }
        public SimpleBean(long l) { }
        public SimpleBean(double d) { }
        public SimpleBean(boolean b) { }
    }

    private BeanDescription _getBeanDescription(Class<?> cls) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getDeserializationConfig().introspect(mapper.constructType(cls));
    }

    // Tests default creator status when none is set
    @Test
    public void testHasDefaultCreator_noDefaultSet_returnsFalse() {
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        assertFalse(collector.hasDefaultCreator());
    }

    // Tests setting default creator
    @Test
    public void testSetDefaultCreator_validCreator_hasDefaultCreatorReturnsTrue() {
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.setDefaultCreator(ctor);

        assertTrue(collector.hasDefaultCreator());
    }

    // Tests constructValueInstantiator for vanilla ArrayList
    @Test
    public void testConstructValueInstantiator_vanillaCollection_returnsVanillaInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(ArrayList.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canInstantiate());
        assertTrue(vi.canCreateUsingDefault());
        assertEquals(ArrayList.class.getName(), vi.getValueTypeDesc());
    }

    // Tests constructValueInstantiator for vanilla LinkedHashMap
    @Test
    public void testConstructValueInstantiator_vanillaMap_returnsVanillaInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(LinkedHashMap.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canInstantiate());
        assertTrue(vi.canCreateUsingDefault());
        assertEquals(LinkedHashMap.class.getName(), vi.getValueTypeDesc());
    }

    // Tests constructValueInstantiator for vanilla HashMap
    @Test
    public void testConstructValueInstantiator_vanillaHashMap_returnsVanillaInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(HashMap.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canInstantiate());
        assertTrue(vi.canCreateUsingDefault());
        assertEquals(HashMap.class.getName(), vi.getValueTypeDesc());
    }

    // Tests adding string creator and building StdValueInstantiator
    @Test
    public void testAddStringCreator_explicitCreator_configuresInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addStringCreator(ctor, true);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canCreateFromString());
    }

    // Tests adding int, long, double, and boolean creators
    @Test
    public void testAddScalarCreators_explicitCreators_configuresInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addIntCreator(ctor, true);
        collector.addLongCreator(ctor, true);
        collector.addDoubleCreator(ctor, true);
        collector.addBooleanCreator(ctor, true);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canCreateFromInt());
        assertTrue(vi.canCreateFromLong());
        assertTrue(vi.canCreateFromDouble());
        assertTrue(vi.canCreateFromBoolean());
    }

    // Tests adding delegating creator with injectables
    @Test
    public void testAddDelegatingCreator_withInjectables_configuresInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = new CreatorProperty(PropertyName.construct("inj"), strType, null, null, null, null, 0, "injId", PropertyMetadata.STD_OPTIONAL);

        collector.addDelegatingCreator(ctor, true, new CreatorProperty[] { null, prop });

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canCreateUsingDelegate());
    }

    // Tests property creator with duplicate property names throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreator_duplicatePropertyNames_throwsIllegalArgumentException() {
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop1 = new CreatorProperty(PropertyName.construct("dup"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty prop2 = new CreatorProperty(PropertyName.construct("dup"), strType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addPropertyCreator(ctor, true, new CreatorProperty[] { prop1, prop2 });
    }

    // Tests property creator with unnamed injectable value skips duplicate check
    @Test
    public void testAddPropertyCreator_injectableWithEmptyName_doesNotThrow() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop1 = new CreatorProperty(PropertyName.construct(""), strType, null, null, null, null, 0, "injectId", PropertyMetadata.STD_REQUIRED);
        CreatorProperty prop2 = new CreatorProperty(PropertyName.construct("name"), strType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addPropertyCreator(ctor, true, new CreatorProperty[] { prop1, prop2 });

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canCreateFromObjectWith());
    }

    // Tests conflicting duplicate explicit creators throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNonDup_duplicateExplicitCreators_throwsIllegalArgumentException() {
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addStringCreator(ctor, true);
        collector.addStringCreator(ctor, true);
    }

    // Tests non-explicit creator is ignored when an explicit one is already registered
    @Test
    public void testVerifyNonDup_nonExplicitIgnoredWhenExplicitExists_doesNotThrow() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        collector.addStringCreator(ctor, true);
        collector.addStringCreator(ctor, false);

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertTrue(vi.canCreateFromString());
    }

    // Tests adding incomplete parameter to creator collector
    @Test
    public void testAddIncompleteParameter_setsParameter_configuresInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = _getBeanDescription(SimpleBean.class);
        CreatorCollector collector = new CreatorCollector(desc, true);

        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        AnnotatedParameter param = new AnnotatedParameter(ctor, String.class, null, 0);
        collector.addIncompeteParameter(param);
        // Second call should not overwrite the first one
        collector.addIncompeteParameter(new AnnotatedParameter(ctor, Integer.class, null, 1));

        ValueInstantiator vi = collector.constructValueInstantiator(mapper.getDeserializationConfig());
        assertNotNull(vi);
        assertEquals(param, vi.getIncompleteParameter());
    }

    // Tests Vanilla instantiator for Collection type
    @Test
    public void testVanilla_collectionType_instantiatesCorrectly() throws IOException {
        CreatorCollector.Vanilla vanilla = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        assertEquals(ArrayList.class.getName(), vanilla.getValueTypeDesc());
        assertTrue(vanilla.canInstantiate());
        assertTrue(vanilla.canCreateUsingDefault());

        Object result = vanilla.createUsingDefault(null);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
    }

    // Tests Vanilla instantiator for Map and HashMap types
    @Test
    public void testVanilla_mapTypes_instantiatesCorrectly() throws IOException {
        CreatorCollector.Vanilla vanillaMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        assertEquals(LinkedHashMap.class.getName(), vanillaMap.getValueTypeDesc());
        Object mapResult = vanillaMap.createUsingDefault(null);
        assertTrue(mapResult instanceof LinkedHashMap);

        CreatorCollector.Vanilla vanillaHashMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        assertEquals(HashMap.class.getName(), vanillaHashMap.getValueTypeDesc());
        Object hashMapResult = vanillaHashMap.createUsingDefault(null);
        assertTrue(hashMapResult instanceof HashMap);
    }

    // Tests Vanilla instantiator for unknown type
    @Test(expected = IllegalStateException.class)
    public void testVanilla_unknownType_throwsIllegalStateException() throws IOException {
        CreatorCollector.Vanilla vanilla = new CreatorCollector.Vanilla(999);
        assertEquals(Object.class.getName(), vanilla.getValueTypeDesc());
        vanilla.createUsingDefault(null);
    }
}
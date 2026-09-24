package com.fasterxml.jackson.databind.deser.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorCollectorTest {

    private ObjectMapper _mapper;
    private DeserializationConfig _config;

    static class SampleBean {
        public SampleBean() { }
        public SampleBean(String s) { }
        public SampleBean(int i) { }
        public SampleBean(long l) { }
        public SampleBean(double d) { }
        public SampleBean(boolean b) { }
        public SampleBean(String s, int i) { }
        public static SampleBean factory(String s) { return new SampleBean(s); }
        public static SampleBean factory2(String s) { return new SampleBean(s); }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
    }

    private BeanDescription _getBeanDescription(Class<?> cls) {
        JavaType type = TypeFactory.defaultInstance().constructType(cls);
        return _config.introspect(type);
    }

    private AnnotatedConstructor _getAnnotatedConstructor(Class<?> cls, Class<?>... paramTypes) throws Exception {
        Constructor<?> ctor = cls.getDeclaredConstructor(paramTypes);
        AnnotationMap[] paramAnns = new AnnotationMap[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramAnns[i] = new AnnotationMap();
        }
        return new AnnotatedConstructor(ctor, new AnnotationMap(), paramAnns);
    }

    private AnnotatedMethod _getAnnotatedMethod(Class<?> cls, String name, Class<?>... paramTypes) throws Exception {
        Method method = cls.getDeclaredMethod(name, paramTypes);
        AnnotationMap[] paramAnns = new AnnotationMap[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramAnns[i] = new AnnotationMap();
        }
        return new AnnotatedMethod(method, new AnnotationMap(), paramAnns);
    }

    // Tests default creator state when none is added
    @Test
    public void testHasDefaultCreator_emptyCollector_returnsFalse() {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        assertFalse(collector.hasDefaultCreator());
    }

    // Tests setting default creator and verifying state
    @Test
    public void testSetDefaultCreator_validConstructor_hasDefaultCreatorIsTrue() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class);

        collector.setDefaultCreator(ctor);

        assertTrue(collector.hasDefaultCreator());
        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDefault());
    }

    // Tests adding string creator and building instantiator
    @Test
    public void testAddStringCreator_explicitCreator_configuresInstantiator() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class, String.class);

        collector.addStringCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }

    // Tests adding int, long, double, and boolean creators
    @Test
    public void testAddScalarCreators_validConstructors_configuresInstantiator() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        collector.addIntCreator(_getAnnotatedConstructor(SampleBean.class, int.class), true);
        collector.addLongCreator(_getAnnotatedConstructor(SampleBean.class, long.class), true);
        collector.addDoubleCreator(_getAnnotatedConstructor(SampleBean.class, double.class), true);
        collector.addBooleanCreator(_getAnnotatedConstructor(SampleBean.class, boolean.class), true);

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromInt());
        assertTrue(inst.canCreateFromLong());
        assertTrue(inst.canCreateFromDouble());
        assertTrue(inst.canCreateFromBoolean());
    }

    // Tests adding delegating creator with injectables
    @Test
    public void testAddDelegatingCreator_withDelegateArg_configuresInstantiator() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class, String.class);

        collector.addDelegatingCreator(ctor, true, new CreatorProperty[0]);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDelegate());
    }

    // Tests adding property-based creator with properties
    @Test
    public void testAddPropertyCreator_validProperties_configuresInstantiator() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class, String.class, int.class);

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);

        CreatorProperty prop1 = new CreatorProperty(new PropertyName("name"), stringType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty prop2 = new CreatorProperty(new PropertyName("age"), intType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        collector.addPropertyCreator(ctor, true, new CreatorProperty[] { prop1, prop2 });
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromObjectWith());
    }

    // Tests exception path when duplicate property names are provided
    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreator_duplicatePropertyNames_throwsException() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class, String.class, int.class);

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);

        CreatorProperty prop1 = new CreatorProperty(new PropertyName("name"), stringType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty prop2 = new CreatorProperty(new PropertyName("name"), stringType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        collector.addPropertyCreator(ctor, true, new CreatorProperty[] { prop1, prop2 });
    }

    // Tests exception path when conflicting explicit creators of the same type are added
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNonDup_conflictingExplicitCreators_throwsException() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        AnnotatedMethod factory1 = _getAnnotatedMethod(SampleBean.class, "factory", String.class);
        AnnotatedMethod factory2 = _getAnnotatedMethod(SampleBean.class, "factory2", String.class);

        collector.addStringCreator(factory1, true);
        collector.addStringCreator(factory2, true);
    }

    // Tests implicit creator ignored when explicit creator is already registered
    @Test
    public void testVerifyNonDup_implicitAfterExplicit_keepsExplicitCreator() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        AnnotatedMethod factory1 = _getAnnotatedMethod(SampleBean.class, "factory", String.class);
        AnnotatedMethod factory2 = _getAnnotatedMethod(SampleBean.class, "factory2", String.class);

        collector.addStringCreator(factory1, true);
        collector.addStringCreator(factory2, false);

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }

    // Tests Vanilla instantiator created for standard collection types when no special creator
    @Test
    public void testConstructValueInstantiator_vanillaCollection_returnsVanillaInstantiator() {
        BeanDescription beanDesc = _getBeanDescription(ArrayList.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canInstantiate());
        assertTrue(inst.canCreateUsingDefault());
        assertEquals(ArrayList.class.getName(), inst.getValueTypeDesc());
    }

    // Tests Vanilla instantiator created for standard map types when no special creator
    @Test
    public void testConstructValueInstantiator_vanillaMap_returnsVanillaInstantiator() {
        BeanDescription beanDesc = _getBeanDescription(HashMap.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canInstantiate());
        assertTrue(inst.canCreateUsingDefault());
        assertEquals(HashMap.class.getName(), inst.getValueTypeDesc());
    }

    // Tests Vanilla instantiator createUsingDefault creates empty instances
    @Test
    public void testVanilla_createUsingDefault_createsInstances() throws Exception {
        CreatorCollector.Vanilla vanillaCol = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        Object col = vanillaCol.createUsingDefault(null);
        assertTrue(col instanceof ArrayList);

        CreatorCollector.Vanilla vanillaMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        Object map = vanillaMap.createUsingDefault(null);
        assertTrue(map instanceof LinkedHashMap);

        CreatorCollector.Vanilla vanillaHashMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        Object hashMap = vanillaHashMap.createUsingDefault(null);
        assertTrue(hashMap instanceof HashMap);
    }

    // Tests incomplete parameter handling
    @Test
    public void testAddIncompeteParameter_validParameter_configuresCorrectly() {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, true);

        AnnotatedParameter param = new AnnotatedParameter(null, String.class, new AnnotationMap(), 0);
        collector.addIncompeteParameter(param);
        collector.addIncompeteParameter(new AnnotatedParameter(null, int.class, new AnnotationMap(), 1));

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst instanceof StdValueInstantiator);
        assertEquals(param, ((StdValueInstantiator) inst).getIncompleteParameter());
    }

    // Tests deprecated helper methods
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedMethods_variousCreators_delegateCorrectly() throws Exception {
        BeanDescription beanDesc = _getBeanDescription(SampleBean.class);
        CreatorCollector collector = new CreatorCollector(beanDesc, false);
        AnnotatedConstructor ctor = _getAnnotatedConstructor(SampleBean.class, String.class);

        collector.addStringCreator(ctor);
        collector.verifyNonDup(ctor, 1);

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }
}
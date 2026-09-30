package com.fasterxml.jackson.databind.deser.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorCollectorTest {

    private ObjectMapper _mapper;
    private DeserializationConfig _config;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
    }

    // Static helper classes for introspection
    static class DummyBean {
        public DummyBean() {}
        public DummyBean(String s) {}
        public DummyBean(int i) {}
        public DummyBean(long l) {}
        public DummyBean(double d) {}
        public DummyBean(boolean b) {}
        public DummyBean(List<?> list) {}
        public DummyBean(Object o) {}
        public DummyBean(String s1, int i2) {}
    }

    static class SubDummyBean extends DummyBean {
        public SubDummyBean(String s) { super(s); }
        public SubDummyBean(Object o) { super(o); }
    }

    private BeanDescription _describe(Class<?> cls) {
        JavaType type = TypeFactory.defaultInstance().constructType(cls);
        return _config.introspect(type);
    }

    private AnnotatedConstructor _findConstructor(Class<?> cls, Class<?>... paramTypes) {
        try {
            Constructor<?> ctor = cls.getDeclaredConstructor(paramTypes);
            AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(cls, _config);
            AnnotationMap annMap = new AnnotationMap();
            AnnotationMap[] paramAnns = new AnnotationMap[paramTypes.length];
            for (int i = 0; i < paramTypes.length; ++i) {
                paramAnns[i] = new AnnotationMap();
            }
            return new AnnotatedConstructor(ac, ctor, annMap, paramAnns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Tests default constructor detection and hasDefaultCreator
    @Test
    public void testDefaultCreator_singleDefaultConstructor_hasDefaultCreatorIsTrue() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        assertFalse(coll.hasDefaultCreator());
        AnnotatedConstructor ctor = _findConstructor(DummyBean.class);
        coll.setDefaultCreator(ctor);
        assertTrue(coll.hasDefaultCreator());
    }

    // Tests adding string creator with verifyNonDup
    @Test
    public void testAddStringCreator_validStringConstructor_instantiatorConfigured() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, String.class);
        coll.addStringCreator(ctor, true);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        assertTrue(vi.canCreateFromString());
    }

    // Tests adding int creator and verifies int instantiator configuration
    @Test
    public void testAddIntCreator_validIntConstructor_canCreateFromInt() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, int.class);
        coll.addIntCreator(ctor, true);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        assertTrue(vi.canCreateFromInt());
    }

    // Tests deprecated addIntCreator method correctly routes to int creator
    @Test
    @SuppressWarnings("deprecation")
    public void testAddIntCreatorDeprecated_routesToIntCreator_canCreateFromInt() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, int.class);
        coll.addIntCreator(ctor);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateFromInt());
        assertFalse(vi.canCreateFromBoolean());
    }

    // Tests deprecated addLongCreator method correctly routes to long creator
    @Test
    @SuppressWarnings("deprecation")
    public void testAddLongCreatorDeprecated_routesToLongCreator_canCreateFromLong() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, long.class);
        coll.addLongCreator(ctor);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateFromLong());
        assertFalse(vi.canCreateFromBoolean());
    }

    // Tests deprecated addDoubleCreator method correctly routes to double creator
    @Test
    @SuppressWarnings("deprecation")
    public void testAddDoubleCreatorDeprecated_routesToDoubleCreator_canCreateFromDouble() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, double.class);
        coll.addDoubleCreator(ctor);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateFromDouble());
        assertFalse(vi.canCreateFromBoolean());
    }

    // Tests deprecated addBooleanCreator method routes to boolean creator
    @Test
    @SuppressWarnings("deprecation")
    public void testAddBooleanCreatorDeprecated_routesToBooleanCreator_canCreateFromBoolean() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, boolean.class);
        coll.addBooleanCreator(ctor);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateFromBoolean());
    }

    // Tests delegating creator with non-collection type
    @Test
    public void testAddDelegatingCreator_objectType_hasDelegatingCreatorIsTrue() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, Object.class);
        coll.addDelegatingCreator(ctor, true, null);

        assertTrue(coll.hasDelegatingCreator());
        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateUsingDelegate());
    }

    // Tests delegating creator with collection-like type (array delegate)
    @Test
    public void testAddDelegatingCreator_collectionLikeType_configuresArrayDelegate() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, List.class);
        coll.addDelegatingCreator(ctor, true, null);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateUsingArrayDelegate());
    }

    // Tests property-based creator configuration
    @Test
    public void testAddPropertyCreator_validProperties_hasPropertyBasedCreatorIsTrue() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, String.class, int.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);

        SettableBeanProperty prop1 = new CreatorProperty(PropertyName.construct("s1"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        SettableBeanProperty prop2 = new CreatorProperty(PropertyName.construct("i2"), intType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        coll.addPropertyCreator(ctor, true, new SettableBeanProperty[] { prop1, prop2 });

        assertTrue(coll.hasPropertyBasedCreator());
        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertTrue(vi.canCreateFromObjectWith());
    }

    // Tests duplicate property name exception in addPropertyCreator
    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreator_duplicatePropertyNames_throwsIllegalArgumentException() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, String.class, int.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        SettableBeanProperty prop1 = new CreatorProperty(PropertyName.construct("dup"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        SettableBeanProperty prop2 = new CreatorProperty(PropertyName.construct("dup"), strType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        coll.addPropertyCreator(ctor, true, new SettableBeanProperty[] { prop1, prop2 });
    }

    // Tests duplicate creator conflict throwing exception when both explicit
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNonDup_duplicateExplicitCreatorsSameType_throwsIllegalArgumentException() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor1 = _findConstructor(DummyBean.class, String.class);
        AnnotatedConstructor ctor2 = _findConstructor(DummyBean.class, String.class);

        coll.addStringCreator(ctor1, true);
        coll.addStringCreator(ctor2, true);
    }

    // Tests ignoring non-explicit creator when explicit one is already present
    @Test
    public void testVerifyNonDup_explicitThenNonExplicit_retainsExplicit() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor1 = _findConstructor(DummyBean.class, String.class);
        AnnotatedConstructor ctor2 = _findConstructor(DummyBean.class, String.class);

        coll.addStringCreator(ctor1, true);
        coll.addStringCreator(ctor2, false);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        assertTrue(vi.canCreateFromString());
    }

    // Tests Vanilla instantiator for ArrayList
    @Test
    public void testConstructValueInstantiator_arrayListTypeNoCustomCreators_returnsVanilla() throws Exception {
        BeanDescription beanDesc = _describe(ArrayList.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        assertTrue(vi.canInstantiate());
        assertTrue(vi.canCreateUsingDefault());
        Object instance = vi.createUsingDefault(null);
        assertTrue(instance instanceof ArrayList);
        assertEquals(ArrayList.class.getName(), vi.getValueTypeDesc());
    }

    // Tests Vanilla instantiator for HashMap
    @Test
    public void testConstructValueInstantiator_hashMapTypeNoCustomCreators_returnsVanilla() throws Exception {
        BeanDescription beanDesc = _describe(HashMap.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        assertTrue(vi.canInstantiate());
        assertTrue(vi.canCreateUsingDefault());
        Object instance = vi.createUsingDefault(null);
        assertTrue(instance instanceof HashMap);
        assertEquals(HashMap.class.getName(), vi.getValueTypeDesc());
    }

    // Tests Vanilla instantiator for LinkedHashMap / Map
    @Test
    public void testConstructValueInstantiator_mapTypeNoCustomCreators_returnsVanilla() throws Exception {
        BeanDescription beanDesc = _describe(Map.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
        Object instance = vi.createUsingDefault(null);
        assertTrue(instance instanceof LinkedHashMap);
        assertEquals(LinkedHashMap.class.getName(), vi.getValueTypeDesc());
    }

    // Tests addIncompleteParameter branch
    @Test
    public void testAddIncompleteParameter_setsParameterOnce() {
        BeanDescription beanDesc = _describe(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(beanDesc, _config);

        AnnotatedConstructor ctor = _findConstructor(DummyBean.class, String.class);
        AnnotatedParameter param = new AnnotatedParameter(ctor, TypeFactory.defaultInstance().constructType(String.class), new AnnotationMap(), 0);

        coll.addIncompeteParameter(param);
        coll.addIncompeteParameter(null);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        assertNotNull(vi);
    }
}
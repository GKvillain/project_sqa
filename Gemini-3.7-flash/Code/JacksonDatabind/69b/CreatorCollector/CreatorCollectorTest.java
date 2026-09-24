package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorCollectorTest {

    private ObjectMapper _mapper;
    private DeserializationConfig _config;

    static class SimpleBean {
        public SimpleBean() {}
    }

    static class MultiCreatorBean {
        protected String _s;
        protected int _i;
        protected long _l;
        protected double _d;
        protected boolean _b;

        public MultiCreatorBean() {}
        public MultiCreatorBean(String s) { _s = s; }
        public MultiCreatorBean(int i) { _i = i; }
        public MultiCreatorBean(long l) { _l = l; }
        public MultiCreatorBean(double d) { _d = d; }
        public MultiCreatorBean(boolean b) { _b = b; }
    }

    static class ConflictBean {
        public ConflictBean(String a) {}
        public static ConflictBean create(String b) { return new ConflictBean(b); }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
    }

    private CreatorCollector createCollector(Class<?> cls) {
        JavaType type = TypeFactory.defaultInstance().constructType(cls);
        BeanDescription beanDesc = _config.introspect(type);
        return new CreatorCollector(beanDesc, _config);
    }

    private AnnotatedConstructor findConstructor(Class<?> cls, Class<?>... paramTypes) {
        JavaType type = TypeFactory.defaultInstance().constructType(cls);
        BeanDescription beanDesc = _config.introspect(type);
        AnnotatedClass ac = beanDesc.getClassInfo();
        for (AnnotatedConstructor ctor : ac.getConstructors()) {
            if (ctor.getParameterCount() == paramTypes.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; ++i) {
                    if (!ctor.getRawParameterType(i).equals(paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return ctor;
                }
            }
        }
        return null;
    }

    private AnnotatedMethod findStaticMethod(Class<?> cls, String name) {
        JavaType type = TypeFactory.defaultInstance().constructType(cls);
        BeanDescription beanDesc = _config.introspect(type);
        AnnotatedClass ac = beanDesc.getClassInfo();
        for (AnnotatedMethod m : ac.getStaticMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    // Tests default creator configuration and hasDefaultCreator
    @Test
    public void testSetDefaultCreator_validConstructor_hasDefaultCreatorTrue() {
        CreatorCollector collector = createCollector(SimpleBean.class);
        assertFalse(collector.hasDefaultCreator());

        AnnotatedConstructor ctor = findConstructor(SimpleBean.class);
        assertNotNull(ctor);
        collector.setDefaultCreator(ctor);

        assertTrue(collector.hasDefaultCreator());
        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDefault());
    }

    // Tests vanilla Collection instantiator when no custom creator is set
    @Test
    public void testConstructValueInstantiator_vanillaCollection_returnsVanillaInstantiator() throws IOException {
        CreatorCollector collector = createCollector(ArrayList.class);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst instanceof CreatorCollector.Vanilla);
        assertTrue(inst.canInstantiate());
        assertTrue(inst.canCreateUsingDefault());
        assertEquals(ArrayList.class.getName(), inst.getValueTypeDesc());

        Object created = inst.createUsingDefault(null);
        assertNotNull(created);
        assertTrue(created instanceof ArrayList);
    }

    // Tests vanilla Map instantiator when no custom creator is set
    @Test
    public void testConstructValueInstantiator_vanillaMap_returnsVanillaInstantiator() throws IOException {
        CreatorCollector collector = createCollector(LinkedHashMap.class);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst instanceof CreatorCollector.Vanilla);
        assertEquals(LinkedHashMap.class.getName(), inst.getValueTypeDesc());

        Object created = inst.createUsingDefault(null);
        assertNotNull(created);
        assertTrue(created instanceof LinkedHashMap);
    }

    // Tests vanilla HashMap instantiator when no custom creator is set
    @Test
    public void testConstructValueInstantiator_vanillaHashMap_returnsVanillaInstantiator() throws IOException {
        CreatorCollector collector = createCollector(HashMap.class);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst instanceof CreatorCollector.Vanilla);
        assertEquals(HashMap.class.getName(), inst.getValueTypeDesc());

        Object created = inst.createUsingDefault(null);
        assertNotNull(created);
        assertTrue(created instanceof HashMap);
    }

    // Tests adding string creator
    @Test
    public void testAddStringCreator_validConstructor_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, String.class);
        assertNotNull(ctor);

        collector.addStringCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }

    // Tests adding int creator
    @Test
    public void testAddIntCreator_validConstructor_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, int.class);
        assertNotNull(ctor);

        collector.addIntCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromInt());
    }

    // Tests adding long creator
    @Test
    public void testAddLongCreator_validConstructor_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, long.class);
        assertNotNull(ctor);

        collector.addLongCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromLong());
    }

    // Tests adding double creator
    @Test
    public void testAddDoubleCreator_validConstructor_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, double.class);
        assertNotNull(ctor);

        collector.addDoubleCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromDouble());
    }

    // Tests adding boolean creator
    @Test
    public void testAddBooleanCreator_validConstructor_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, boolean.class);
        assertNotNull(ctor);

        collector.addBooleanCreator(ctor, true);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);

        assertNotNull(inst);
        assertTrue(inst.canCreateFromBoolean());
    }

    // Tests adding delegating creator
    @Test
    public void testAddDelegatingCreator_scalarType_hasDelegatingCreatorTrue() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, String.class);
        assertNotNull(ctor);

        assertFalse(collector.hasDelegatingCreator());
        collector.addDelegatingCreator(ctor, true, null);
        assertTrue(collector.hasDelegatingCreator());

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDelegate());
    }

    // Tests adding property-based creator
    @Test
    public void testAddPropertyCreator_validProperties_hasPropertyBasedCreatorTrue() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, String.class);
        assertNotNull(ctor);

        assertFalse(collector.hasPropertyBasedCreator());
        SettableBeanProperty[] props = new SettableBeanProperty[0];
        collector.addPropertyCreator(ctor, true, props);
        assertTrue(collector.hasPropertyBasedCreator());

        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromObjectWith());
    }

    // Tests duplicate property names in addPropertyCreator throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreator_duplicatePropertyNames_throwsException() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, String.class);

        SettableBeanProperty prop1 = org.mockito.Mockito.mock(SettableBeanProperty.class);
        org.mockito.Mockito.when(prop1.getName()).thenReturn("sameName");
        SettableBeanProperty prop2 = org.mockito.Mockito.mock(SettableBeanProperty.class);
        org.mockito.Mockito.when(prop2.getName()).thenReturn("sameName");

        collector.addPropertyCreator(ctor, true, new SettableBeanProperty[] { prop1, prop2 });
    }

    // Tests conflicting duplicate creators throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNonDup_conflictingExplicitCreators_throwsException() {
        CreatorCollector collector = createCollector(ConflictBean.class);
        AnnotatedConstructor ctor1 = findConstructor(ConflictBean.class, String.class);
        AnnotatedConstructor ctor2 = findConstructor(ConflictBean.class, String.class);
        assertNotNull(ctor1);
        assertNotNull(ctor2);

        collector.addStringCreator(ctor1, true);
        collector.addStringCreator(ctor2, true);
    }

    // Tests non-conflicting override when new creator is not explicit
    @Test
    public void testVerifyNonDup_explicitThenImplicit_keepsExplicit() {
        CreatorCollector collector = createCollector(ConflictBean.class);
        AnnotatedConstructor ctor = findConstructor(ConflictBean.class, String.class);
        assertNotNull(ctor);

        collector.addStringCreator(ctor, true);
        collector.addStringCreator(ctor, false);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }

    // Tests incomplete parameter handling
    @Test
    public void testAddIncompleteParameter_parameterProvided_configuresInstantiator() {
        CreatorCollector collector = createCollector(MultiCreatorBean.class);
        AnnotatedConstructor ctor = findConstructor(MultiCreatorBean.class, String.class);
        AnnotatedParameter param = ctor.getParameter(0);

        collector.addIncompeteParameter(param);
        ValueInstantiator inst = collector.constructValueInstantiator(_config);
        assertNotNull(inst);
        assertEquals(param, inst.getIncompleteParameter());
    }

    // Tests Vanilla ValueInstantiator unknown type exception
    @Test(expected = IllegalStateException.class)
    public void testVanillaInstantiator_unknownType_throwsException() throws IOException {
        CreatorCollector.Vanilla vanilla = new CreatorCollector.Vanilla(999);
        assertEquals(Object.class.getName(), vanilla.getValueTypeDesc());
        vanilla.createUsingDefault(null);
    }
}
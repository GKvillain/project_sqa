package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;

public class StdSubtypeResolverTest {

    private StdSubtypeResolver _resolver;
    private ObjectMapper _mapper;
    private DeserializationConfig _deserConfig;
    private SerializationConfig _serConfig;

    static abstract class AbstractBase { }

    static class ConcreteBase { }

    static class SubA extends AbstractBase { }

    static class SubB extends AbstractBase { }

    @JsonTypeName("namedSub")
    static class NamedSub extends AbstractBase { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = AnnotatedSub1.class, name = "sub1"),
        @JsonSubTypes.Type(value = AnnotatedSub2.class, name = "sub2")
    })
    static abstract class AnnotatedBase { }

    static class AnnotatedSub1 extends AnnotatedBase { }
    static class AnnotatedSub2 extends AnnotatedBase { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = Level2Sub.class, name = "level2")
    })
    static class Level1Sub extends AnnotatedBase { }

    static class Level2Sub extends Level1Sub { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubA.class, name = "polyA")
    })
    static class PropHolder {
        public AbstractBase prop;
    }

    interface InterfaceBase { }

    static class ImplA implements InterfaceBase { }

    @Before
    public void setUp() {
        _resolver = new StdSubtypeResolver();
        _mapper = new ObjectMapper();
        _deserConfig = _mapper.getDeserializationConfig();
        _serConfig = _mapper.getSerializationConfig();
    }

    // Tests registration of subtypes with Class array
    @Test
    public void testRegisterSubtypes_classes_registersSuccessfully() {
        _resolver.registerSubtypes(SubA.class, SubB.class);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(2, result.size());

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (NamedType nt : result) {
            types.add(nt.getType());
        }
        assertTrue(types.contains(SubA.class));
        assertTrue(types.contains(SubB.class));
    }

    // Tests registration of subtypes with NamedType array
    @Test
    public void testRegisterSubtypes_namedTypes_registersSuccessfully() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "customA"), new NamedType(SubB.class, "customB"));

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(2, result.size());

        Map<Class<?>, String> nameMap = new HashMap<Class<?>, String>();
        for (NamedType nt : result) {
            nameMap.put(nt.getType(), nt.getName());
        }
        assertEquals("customA", nameMap.get(SubA.class));
        assertEquals("customB", nameMap.get(SubB.class));
    }

    // Tests collectAndResolveSubtypesByClass for AnnotatedClass with annotations
    @Test
    public void testCollectAndResolveSubtypesByClass_annotatedClass_returnsSubtypes() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_serConfig, AnnotatedBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_serConfig, ac);

        assertNotNull(result);
        assertEquals(3, result.size());

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (NamedType nt : result) {
            types.add(nt.getType());
        }
        assertTrue(types.contains(AnnotatedBase.class));
        assertTrue(types.contains(AnnotatedSub1.class));
        assertTrue(types.contains(AnnotatedSub2.class));
    }

    // Tests collectAndResolveSubtypesByClass for AnnotatedMember property
    @Test
    public void testCollectAndResolveSubtypesByClass_memberProperty_returnsSubtypes() {
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : _mapper.getSerializationConfig().introspect(_mapper.constructType(PropHolder.class)).findProperties()) {
            if ("prop".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }

        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_serConfig, member, baseType);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        boolean foundSubA = false;
        for (NamedType nt : result) {
            if (nt.getType() == SubA.class) {
                foundSubA = true;
                assertEquals("polyA", nt.getName());
            }
        }
        assertTrue(foundSubA);
    }

    // Tests collectAndResolveSubtypesByClass when baseType is null
    @Test
    public void testCollectAndResolveSubtypesByClass_nullBaseType_usesMemberRawType() {
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : _mapper.getSerializationConfig().introspect(_mapper.constructType(PropHolder.class)).findProperties()) {
            if ("prop".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }

        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_serConfig, member, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests collectAndResolveSubtypesByTypeId for AnnotatedClass without registered subtypes
    @Test
    public void testCollectAndResolveSubtypesByTypeId_annotatedClass_resolvesTypeNames() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AnnotatedBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(2, result.size());

        Map<String, Class<?>> nameToClass = new HashMap<String, Class<?>>();
        for (NamedType nt : result) {
            nameToClass.put(nt.getName(), nt.getType());
        }
        assertEquals(AnnotatedSub1.class, nameToClass.get("sub1"));
        assertEquals(AnnotatedSub2.class, nameToClass.get("sub2"));
    }

    // Tests collectAndResolveSubtypesByTypeId for AnnotatedClass with registered subtypes
    @Test
    public void testCollectAndResolveSubtypesByTypeId_withRegisteredSubtypes_resolvesCombined() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "typeA"));

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(1, result.size());
        NamedType nt = result.iterator().next();
        assertEquals(SubA.class, nt.getType());
        assertEquals("typeA", nt.getName());
    }

    // Tests collectAndResolveSubtypesByTypeId for AnnotatedMember property
    @Test
    public void testCollectAndResolveSubtypesByTypeId_memberProperty_resolvesPropertySubtypes() {
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : _mapper.getDeserializationConfig().introspect(_mapper.constructType(PropHolder.class)).findProperties()) {
            if ("prop".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }

        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, member, baseType);
        assertNotNull(result);
        assertEquals(1, result.size());
        NamedType nt = result.iterator().next();
        assertEquals(SubA.class, nt.getType());
        assertEquals("polyA", nt.getName());
    }

    // Tests combineNamedAndUnnamed omitting abstract base type without name
    @Test
    public void testCombineNamedAndUnnamed_abstractBaseWithoutName_omitsAbstractBase() {
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        typesHandled.add(AbstractBase.class);
        typesHandled.add(SubA.class);

        Map<String, NamedType> byName = new LinkedHashMap<String, NamedType>();
        byName.put("subA", new NamedType(SubA.class, "subA"));

        Collection<NamedType> combined = _resolver._combineNamedAndUnnamed(AbstractBase.class, typesHandled, byName);
        assertNotNull(combined);
        assertEquals(1, combined.size());
        assertEquals(SubA.class, combined.iterator().next().getType());
    }

    // Tests combineNamedAndUnnamed retaining concrete base type without explicit name
    @Test
    public void testCombineNamedAndUnnamed_concreteBaseWithoutName_retainsConcreteBase() {
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        typesHandled.add(ConcreteBase.class);
        typesHandled.add(SubA.class);

        Map<String, NamedType> byName = new LinkedHashMap<String, NamedType>();
        byName.put("subA", new NamedType(SubA.class, "subA"));

        Collection<NamedType> combined = _resolver._combineNamedAndUnnamed(ConcreteBase.class, typesHandled, byName);
        assertNotNull(combined);
        assertEquals(2, combined.size());

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (NamedType nt : combined) {
            types.add(nt.getType());
        }
        assertTrue(types.contains(ConcreteBase.class));
        assertTrue(types.contains(SubA.class));
    }

    // Tests JsonTypeName resolution from class annotation
    @Test
    public void testCollectAndResolve_namedSubclass_extractsJsonTypeName() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_serConfig, NamedSub.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_serConfig, ac);

        assertNotNull(result);
        assertEquals(1, result.size());
        NamedType nt = result.iterator().next();
        assertEquals(NamedSub.class, nt.getType());
        assertEquals("namedSub", nt.getName());
    }

    // Tests registered subtypes precedence over class annotations
    @Test
    public void testRegisteredSubtypes_overrideAnnotationName() {
        _resolver.registerSubtypes(new NamedType(NamedSub.class, "overriddenName"));

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(1, result.size());
        NamedType nt = result.iterator().next();
        assertEquals(NamedSub.class, nt.getType());
        assertEquals("overriddenName", nt.getName());
    }

    // Tests registration with Collection of classes
    @Test
    public void testRegisterSubtypes_collection_registersSuccessfully() {
        List<Class<?>> classes = Arrays.<Class<?>>asList(SubA.class, SubB.class);
        _resolver.registerSubtypes(classes);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(2, result.size());

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (NamedType nt : result) {
            types.add(nt.getType());
        }
        assertTrue(types.contains(SubA.class));
        assertTrue(types.contains(SubB.class));
    }

    // Tests copy method preserves registered subtypes
    @Test
    public void testCopy_preservesRegisteredSubtypes() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "copiedSubA"));

        SubtypeResolver copy = _resolver.copy();
        assertNotNull(copy);
        assertNotSame(_resolver, copy);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AbstractBase.class);
        Collection<NamedType> result = copy.collectAndResolveSubtypesByTypeId(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(1, result.size());
        NamedType nt = result.iterator().next();
        assertEquals(SubA.class, nt.getType());
        assertEquals("copiedSubA", nt.getName());
    }

    // Tests recursive subtype resolution with multi-level hierarchies
    @Test
    public void testCollectAndResolveSubtypesByTypeId_multiLevelHierarchy() {
        _resolver.registerSubtypes(Level1Sub.class);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, AnnotatedBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, ac);

        assertNotNull(result);
        Map<String, Class<?>> nameMap = new HashMap<String, Class<?>>();
        for (NamedType nt : result) {
            nameMap.put(nt.getName(), nt.getType());
        }
        assertTrue(nameMap.containsKey("sub1"));
        assertTrue(nameMap.containsKey("sub2"));
        assertTrue(nameMap.containsKey("level2"));
        assertEquals(Level2Sub.class, nameMap.get("level2"));
    }

    // Tests collectAndResolveSubtypesByTypeId with null baseType on member property
    @Test
    public void testCollectAndResolveSubtypesByTypeId_nullBaseType_usesMemberRawType() {
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : _mapper.getDeserializationConfig().introspect(_mapper.constructType(PropHolder.class)).findProperties()) {
            if ("prop".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }

        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByTypeId(_deserConfig, member, null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(SubA.class, result.iterator().next().getType());
    }

    // Tests resolution for interface base types
    @Test
    public void testCollectAndResolveSubtypesByClass_interfaceBaseType() {
        _resolver.registerSubtypes(ImplA.class);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_deserConfig, InterfaceBase.class);
        Collection<NamedType> result = _resolver.collectAndResolveSubtypesByClass(_deserConfig, ac);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ImplA.class, result.iterator().next().getType());
    }
}
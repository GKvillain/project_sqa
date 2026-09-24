package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public class StdSubtypeResolverTest {

    private ObjectMapper _mapper;
    private MapperConfig<?> _config;
    private StdSubtypeResolver _resolver;

    // Test types hierarchy
    static abstract class AbstractBase { }

    @JsonTypeName("named_abstract_base")
    static abstract class NamedAbstractBase { }

    static class ConcreteBase { }

    static class SubA extends AbstractBase { }

    static class SubB extends AbstractBase { }

    @JsonTypeName("subC_name")
    static class SubC extends AbstractBase { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubA.class, name = "subA_name"),
        @JsonSubTypes.Type(value = SubB.class)
    })
    static class BaseWithSubTypes { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = IntermediateNode.class, name = "intermediate")
    })
    static abstract class TreeRoot { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = LeafNode.class, name = "leaf")
    })
    static class IntermediateNode extends TreeRoot { }

    static class LeafNode extends IntermediateNode { }

    static class Container {
        @JsonSubTypes({
            @JsonSubTypes.Type(value = SubA.class, name = "propSubA")
        })
        public AbstractBase memberWithSubTypes;

        public AbstractBase plainMember;
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
        _resolver = new StdSubtypeResolver();
    }

    // Tests registration of subtypes with Class array
    @Test
    public void testRegisterSubtypes_withClasses_registersSuccessfully() {
        _resolver.registerSubtypes(SubA.class, SubB.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
    }

    // Tests registration of subtypes with NamedType array
    @Test
    public void testRegisterSubtypes_withNamedTypes_registersSuccessfully() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "customA"), new NamedType(SubB.class, "customB"));
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        boolean foundA = false;
        for (NamedType nt : resolved) {
            if ("customA".equals(nt.getName()) && nt.getType() == SubA.class) {
                foundA = true;
            }
        }
        assertTrue(foundA);
    }

    // Tests collectAndResolveSubtypesByClass on AnnotatedClass without registered subtypes
    @Test
    public void testCollectAndResolveSubtypesByClass_annotatedClass_returnsRootType() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, ConcreteBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals(ConcreteBase.class, resolved.iterator().next().getType());
    }

    // Tests collectAndResolveSubtypesByClass on AnnotatedClass with @JsonSubTypes annotations
    @Test
    public void testCollectAndResolveSubtypesByClass_annotatedClassWithSubtypesAnnotation_returnsAllSubtypes() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, BaseWithSubTypes.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(3, resolved.size()); // BaseWithSubTypes, SubA, SubB
    }

    // Tests collectAndResolveSubtypesByClass with AnnotatedMember and baseType
    @Test
    public void testCollectAndResolveSubtypesByClass_annotatedMemberWithBaseType_resolvesCorrectly() throws Exception {
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("memberWithSubTypes".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, member, baseType);

        assertNotNull(resolved);
        assertEquals(2, resolved.size()); // AbstractBase and SubA
    }

    // Tests collectAndResolveSubtypesByClass when baseType is null (falls back to member raw type)
    @Test
    public void testCollectAndResolveSubtypesByClass_nullBaseType_fallsBackToMemberRawType() throws Exception {
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("memberWithSubTypes".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, member, null);

        assertNotNull(resolved);
        assertEquals(2, resolved.size()); // AbstractBase and SubA
    }

    // Tests collectAndResolveSubtypesByTypeId with AnnotatedClass and registered subtypes
    @Test
    public void testCollectAndResolveSubtypesByTypeId_annotatedClass_withRegisteredSubtypes() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "customA"), new NamedType(SubB.class, "customB"));
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
    }

    // Tests collectAndResolveSubtypesByTypeId on abstract class excludes abstract base type if unnamed
    @Test
    public void testCollectAndResolveSubtypesByTypeId_abstractBaseWithoutExplicitName_excludedFromResults() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertTrue(resolved.isEmpty());
    }

    // Tests collectAndResolveSubtypesByTypeId on concrete class includes concrete base type even if unnamed
    @Test
    public void testCollectAndResolveSubtypesByTypeId_concreteBaseWithoutExplicitName_includedInResults() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, ConcreteBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals(ConcreteBase.class, resolved.iterator().next().getType());
    }

    // Tests collectAndResolveSubtypesByTypeId with AnnotatedMember and baseType
    @Test
    public void testCollectAndResolveSubtypesByTypeId_annotatedMemberWithBaseType_resolvesCorrectly() {
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("memberWithSubTypes".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, member, baseType);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        NamedType nt = resolved.iterator().next();
        assertEquals(SubA.class, nt.getType());
        assertEquals("propSubA", nt.getName());
    }

    // Tests resolution with @JsonTypeName on subtype class
    @Test
    public void testCollectAndResolve_withJsonTypeName_resolvesAssignedName() {
        _resolver.registerSubtypes(SubC.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        NamedType nt = resolved.iterator().next();
        assertEquals(SubC.class, nt.getType());
        assertEquals("subC_name", nt.getName());
    }

    // Tests subtype resolution precedence where registered subtypes override annotation names
    @Test
    public void testCollectAndResolveSubtypesByTypeId_registeredPrecedenceOverAnnotations() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "registeredOverrideA"));
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("memberWithSubTypes".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, member, baseType);

        assertNotNull(resolved);
        boolean foundRegistered = false;
        for (NamedType nt : resolved) {
            if ("registeredOverrideA".equals(nt.getName())) {
                foundRegistered = true;
            }
        }
        assertTrue(foundRegistered);
    }

    // Tests copy method creates an independent copy with registered subtypes
    @Test
    public void testCopy_preservesRegisteredSubtypes() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "copiedA"));
        StdSubtypeResolver copy = _resolver.copy();
        assertNotNull(copy);
        assertNotSame(_resolver, copy);

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = copy.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals("copiedA", resolved.iterator().next().getName());
    }

    // Tests copy method when registered subtypes set is null
    @Test
    public void testCopy_whenNoRegisteredSubtypes() {
        StdSubtypeResolver copy = _resolver.copy();
        assertNotNull(copy);
        assertNotSame(_resolver, copy);
    }

    // Tests collectAndResolveSubtypesByTypeId when baseType is null
    @Test
    public void testCollectAndResolveSubtypesByTypeId_nullBaseType_fallsBackToMemberRawType() {
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("memberWithSubTypes".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, member, null);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals(SubA.class, resolved.iterator().next().getType());
        assertEquals("propSubA", resolved.iterator().next().getName());
    }

    // Tests collectAndResolveSubtypesByTypeId with named abstract base class includes it
    @Test
    public void testCollectAndResolveSubtypesByTypeId_abstractBaseWithExplicitName_includedInResults() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, NamedAbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        NamedType nt = resolved.iterator().next();
        assertEquals(NamedAbstractBase.class, nt.getType());
        assertEquals("named_abstract_base", nt.getName());
    }

    // Tests recursive subtype resolution with nested @JsonSubTypes annotations
    @Test
    public void testCollectAndResolveSubtypesByTypeId_nestedHierarchy_resolvesAllSubtypes() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, TreeRoot.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (NamedType nt : resolved) {
            types.add(nt.getType());
        }
        assertTrue(types.contains(IntermediateNode.class));
        assertTrue(types.contains(LeafNode.class));
    }

    // Tests recursive subtype resolution by class with nested @JsonSubTypes annotations
    @Test
    public void testCollectAndResolveSubtypesByClass_nestedHierarchy_resolvesAllSubtypes() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, TreeRoot.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(3, resolved.size()); // TreeRoot, IntermediateNode, LeafNode
    }

    // Tests registerSubtypes with Collection of Class
    @Test
    public void testRegisterSubtypes_withClassCollection() {
        _resolver.registerSubtypes(Arrays.<Class<?>>asList(SubA.class, SubB.class));
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByClass(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
    }

    // Tests registerSubtypes with Collection of NamedType
    @Test
    public void testRegisterSubtypes_withNamedTypeCollection() {
        _resolver.registerSubtypes(Arrays.asList(new NamedType(SubA.class, "collA"), new NamedType(SubB.class, "collB")));
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, ac);

        assertNotNull(resolved);
        assertEquals(2, resolved.size());
    }

    // Tests collectAndResolveSubtypesByTypeId when property has no @JsonSubTypes
    @Test
    public void testCollectAndResolveSubtypesByTypeId_memberWithoutAnnotations_resolvesRegisteredSubtypes() {
        _resolver.registerSubtypes(new NamedType(SubA.class, "registeredA"));
        AnnotatedClass containerAc = AnnotatedClassResolver.resolveWithoutSuperTypes(_config, Container.class);
        AnnotatedMember member = null;
        for (AnnotatedField f : containerAc.fields()) {
            if ("plainMember".equals(f.getName())) {
                member = f;
                break;
            }
        }
        assertNotNull(member);
        JavaType baseType = _mapper.constructType(AbstractBase.class);
        Collection<NamedType> resolved = _resolver.collectAndResolveSubtypesByTypeId(_config, member, baseType);

        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals("registeredA", resolved.iterator().next().getName());
    }
}
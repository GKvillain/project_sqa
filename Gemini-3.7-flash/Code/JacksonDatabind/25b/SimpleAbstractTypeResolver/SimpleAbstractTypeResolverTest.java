package com.fasterxml.jackson.databind.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleAbstractTypeResolverTest {

    private SimpleAbstractTypeResolver resolver;
    private TypeFactory typeFactory;

    // Custom abstract and concrete classes for testing custom types
    public static abstract class CustomAbstract {
        public int id;
    }

    public static class CustomConcrete extends CustomAbstract {
        public String name;
    }

    public interface CustomInterface {
    }

    public static class CustomInterfaceImpl implements CustomInterface {
    }

    @Before
    public void setUp() {
        resolver = new SimpleAbstractTypeResolver();
        typeFactory = TypeFactory.defaultInstance();
    }

    // Tests normal mapping registration and method chaining
    @Test
    public void testAddMapping_validSuperAndSubType_returnsThis() {
        SimpleAbstractTypeResolver result = resolver.addMapping(List.class, ArrayList.class);
        assertSame(resolver, result);
    }

    // Tests exception when mapping a class to itself
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_sameClass_throwsIllegalArgumentException() {
        resolver.addMapping(List.class, List.class);
    }

    // Tests exception when subType is not assignable to superType
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_notSubtype_throwsIllegalArgumentException() {
        resolver.addMapping(List.class, (Class) String.class);
    }

    // Tests exception when superType is a concrete class
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_concreteSuperType_throwsIllegalArgumentException() {
        resolver.addMapping(ArrayList.class, ArrayList.class);
    }

    // Tests exception when superType is concrete even if subType is a valid subclass
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_concreteSuperTypeWithSubclass_throwsIllegalArgumentException() {
        resolver.addMapping(String.class, String.class);
    }

    // Tests finding type mapping when mapping exists for an interface
    @Test
    public void testFindTypeMapping_existingInterfaceMapping_returnsNarrowedType() {
        resolver.addMapping(List.class, LinkedList.class);
        JavaType inputType = typeFactory.constructType(List.class);

        JavaType resultType = resolver.findTypeMapping(null, inputType);

        assertNotNull(resultType);
        assertEquals(LinkedList.class, resultType.getRawClass());
    }

    // Tests finding type mapping when mapping exists for an abstract class
    @Test
    public void testFindTypeMapping_existingAbstractClassMapping_returnsNarrowedType() {
        resolver.addMapping(AbstractList.class, ArrayList.class);
        JavaType inputType = typeFactory.constructType(AbstractList.class);

        JavaType resultType = resolver.findTypeMapping(null, inputType);

        assertNotNull(resultType);
        assertEquals(ArrayList.class, resultType.getRawClass());
    }

    // Tests finding type mapping when mapping exists with generic parameters
    @Test
    public void testFindTypeMapping_genericCollectionMapping_preservesGenericType() {
        resolver.addMapping(Collection.class, ArrayList.class);
        JavaType inputType = typeFactory.constructCollectionType(Collection.class, String.class);

        JavaType resultType = resolver.findTypeMapping(null, inputType);

        assertNotNull(resultType);
        assertEquals(ArrayList.class, resultType.getRawClass());
        assertEquals(1, resultType.containedTypeCount());
        assertEquals(String.class, resultType.containedType(0).getRawClass());
    }

    // Tests finding type mapping when mapping exists for map with generic parameters
    @Test
    public void testFindTypeMapping_genericMapMapping_preservesKeyAndValueTypes() {
        resolver.addMapping(Map.class, HashMap.class);
        JavaType inputType = typeFactory.constructMapType(Map.class, String.class, Integer.class);

        JavaType resultType = resolver.findTypeMapping(null, inputType);

        assertNotNull(resultType);
        assertEquals(HashMap.class, resultType.getRawClass());
        assertEquals(String.class, resultType.getKeyType().getRawClass());
        assertEquals(Integer.class, resultType.getContentType().getRawClass());
    }

    // Tests finding type mapping when no mapping exists
    @Test
    public void testFindTypeMapping_noMappingFound_returnsNull() {
        JavaType inputType = typeFactory.constructType(List.class);

        JavaType resultType = resolver.findTypeMapping(null, inputType);

        assertNull(resultType);
    }

    // Tests resolveAbstractType default implementation returns null
    @Test
    public void testResolveAbstractType_always_returnsNull() {
        JavaType inputType = typeFactory.constructType(List.class);

        JavaType resultType = resolver.resolveAbstractType(null, inputType);

        assertNull(resultType);
    }

    // Tests finding type mapping when DeserializationConfig is provided
    @Test
    public void testFindTypeMapping_withNonNullDeserializationConfig_returnsMappedType() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        resolver.addMapping(CustomInterface.class, CustomInterfaceImpl.class);
        JavaType inputType = config.constructType(CustomInterface.class);

        JavaType resultType = resolver.findTypeMapping(config, inputType);

        assertNotNull(resultType);
        assertEquals(CustomInterfaceImpl.class, resultType.getRawClass());
    }

    // Tests multiple distinct mappings on the same resolver
    @Test
    public void testAddMapping_multipleMappings_resolvesIndependently() {
        resolver.addMapping(List.class, LinkedList.class);
        resolver.addMapping(CustomAbstract.class, CustomConcrete.class);

        JavaType listType = resolver.findTypeMapping(null, typeFactory.constructType(List.class));
        JavaType customType = resolver.findTypeMapping(null, typeFactory.constructType(CustomAbstract.class));

        assertNotNull(listType);
        assertEquals(LinkedList.class, listType.getRawClass());

        assertNotNull(customType);
        assertEquals(CustomConcrete.class, customType.getRawClass());
    }

    // Tests full deserialization integration via ObjectMapper and SimpleModule
    @Test
    public void testObjectMapperIntegration_resolvesAbstractTypeDuringDeserialization() throws Exception {
        resolver.addMapping(CustomAbstract.class, CustomConcrete.class);

        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        CustomAbstract result = mapper.readValue("{\"id\": 123, \"name\": \"test\"}", CustomAbstract.class);

        assertNotNull(result);
        assertTrue(result instanceof CustomConcrete);
        assertEquals(123, result.id);
        assertEquals("test", ((CustomConcrete) result).name);
    }

    // Tests serialization and deserialization of SimpleAbstractTypeResolver instance
    @Test
    public void testSerialization_resolverInstance_retainsMappings() throws Exception {
        resolver.addMapping(CustomInterface.class, CustomInterfaceImpl.class);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(resolver);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SimpleAbstractTypeResolver deserialized = (SimpleAbstractTypeResolver) in.readObject();
        in.close();

        JavaType resultType = deserialized.findTypeMapping(null, typeFactory.constructType(CustomInterface.class));
        assertNotNull(resultType);
        assertEquals(CustomInterfaceImpl.class, resultType.getRawClass());
    }
}
package com.google.gson.internal;

import static org.junit.Assert.*;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

public class ConstructorConstructorTest {

    // Tests basic construction of a simple class with a public no-arg constructor
    @Test
    public void testGet_simpleClassWithNoArgConstructor_returnsInstance() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<StringBuilder> constructor = constructorConstructor.get(TypeToken.get(StringBuilder.class));
        assertNotNull(constructor);
        StringBuilder instance = constructor.construct();
        assertNotNull(instance);
        assertEquals("", instance.toString());
    }

    // Tests that a null instance creator map leads to default construction
    @Test
    public void testGet_nullInstanceCreators_usesDefaultConstructor() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(null);
        ObjectConstructor<String> constructor = constructorConstructor.get(TypeToken.get(String.class));
        assertNotNull(constructor);
        String instance = constructor.construct();
        assertNotNull(instance);
        assertEquals("", instance);
    }

    // Tests TypeToken with raw type match for instance creator
    @Test
    public void testGet_rawTypeInstanceCreator_usesRawTypeCreator() {
        Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<Type, InstanceCreator<?>>();
        final StringBuilder sb = new StringBuilder("raw");
        instanceCreators.put(StringBuilder.class, new InstanceCreator<StringBuilder>() {
            @Override
            public StringBuilder createInstance(Type type) {
                return sb;
            }
        });
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);
        // Use a subtype token, e.g., no subtype; raw type matches
        ObjectConstructor<StringBuilder> constructor = constructorConstructor.get(TypeToken.get(StringBuilder.class));
        assertNotNull(constructor);
        assertSame(sb, constructor.construct());
    }

    // Tests that a type-specific instance creator takes precedence over raw type
    @Test
    public void testGet_typeInstanceCreator_precedesRawType() {
        final StringBuilder typeCreatorResult = new StringBuilder("type");
        final StringBuilder rawCreatorResult = new StringBuilder("raw");
        Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<Type, InstanceCreator<?>>();
        // Use a ParameterizedTypeToken for type-specific entry
        TypeToken<StringBuilder> typeToken = TypeToken.get(StringBuilder.class);
        instanceCreators.put(typeToken.getType(), new InstanceCreator<StringBuilder>() {
            @Override
            public StringBuilder createInstance(Type type) {
                return typeCreatorResult;
            }
        });
        instanceCreators.put(StringBuilder.class, new InstanceCreator<StringBuilder>() {
            @Override
            public StringBuilder createInstance(Type type) {
                return rawCreatorResult;
            }
        });
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);
        ObjectConstructor<StringBuilder> constructor = constructorConstructor.get(typeToken);
        assertNotNull(constructor);
        assertSame(typeCreatorResult, constructor.construct());
    }

    // Tests that type-specific instance creator is used when it matches exactly
    @Test
    public void testGet_typeInstanceCreatorExactMatch_usesCreator() {
        final String expected = "fromCreator";
        Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<Type, InstanceCreator<?>>();
        TypeToken<Simple> typeToken = TypeToken.get(Simple.class);
        instanceCreators.put(typeToken.getType(), new InstanceCreator<Simple>() {
            @Override
            public Simple createInstance(Type type) {
                return new Simple(expected);
            }
        });
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);
        ObjectConstructor<Simple> constructor = constructorConstructor.get(typeToken);
        assertNotNull(constructor);
        Simple instance = constructor.construct();
        assertEquals(expected, instance.value);
    }

    // Tests default implementation for SortedSet (returns TreeSet)
    @Test
    public void testGet_sortedSetInterface_returnsTreeSet() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedSet> constructor = constructorConstructor.get(TypeToken.get(SortedSet.class));
        assertNotNull(constructor);
        SortedSet instance = constructor.construct();
        assertTrue(instance instanceof TreeSet);
    }

    // Tests default implementation for Set interface (returns LinkedHashSet)
    @Test
    public void testGet_setInterface_returnsLinkedHashSet() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Set> constructor = constructorConstructor.get(TypeToken.get(Set.class));
        assertNotNull(constructor);
        Set instance = constructor.construct();
        assertTrue(instance instanceof LinkedHashSet);
    }

    // Tests default implementation for Queue interface (returns LinkedList)
    @Test
    public void testGet_queueInterface_returnsLinkedList() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Queue> constructor = constructorConstructor.get(TypeToken.get(Queue.class));
        assertNotNull(constructor);
        Queue instance = constructor.construct();
        assertTrue(instance instanceof LinkedList);
    }

    // Tests default implementation for Collection interface (returns ArrayList)
    @Test
    public void testGet_collectionInterface_returnsArrayList() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Collection> constructor = constructorConstructor.get(TypeToken.get(Collection.class));
        assertNotNull(constructor);
        Collection instance = constructor.construct();
        assertTrue(instance instanceof ArrayList);
    }

    // Tests default implementation for SortedMap interface (returns TreeMap)
    @Test
    public void testGet_sortedMapInterface_returnsTreeMap() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedMap> constructor = constructorConstructor.get(TypeToken.get(SortedMap.class));
        assertNotNull(constructor);
        SortedMap instance = constructor.construct();
        assertTrue(instance instanceof TreeMap);
    }

    // Tests default implementation for Map interface with non-String key (returns LinkedHashMap)
    @Test
    public void testGet_mapInterfaceNonStringKey_returnsLinkedHashMap() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<Integer, String>> typeToken = new TypeToken<Map<Integer, String>>() {};
        ObjectConstructor<Map> constructor = constructorConstructor.get(typeToken);
        assertNotNull(constructor);
        Map instance = constructor.construct();
        assertTrue(instance instanceof LinkedHashMap);
    }

    // Tests default implementation for Map interface with String key (returns LinkedTreeMap)
    @Test
    public void testGet_mapInterfaceStringKey_returnsLinkedTreeMap() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<String, String>> typeToken = new TypeToken<Map<String, String>>() {};
        ObjectConstructor<Map> constructor = constructorConstructor.get(typeToken);
        assertNotNull(constructor);
        Map instance = constructor.construct();
        assertTrue(instance instanceof LinkedTreeMap);
    }

    // Tests construction of an abstract class where no default constructor exists (falls back to UnsafeAllocator)
    @Test
    public void testGet_abstractClass_fallsBackToUnsafeAllocator() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<AbstractList> constructor = constructorConstructor.get(TypeToken.get(AbstractList.class));
        assertNotNull(constructor);
        AbstractList instance = constructor.construct();
        assertNotNull(instance);
    }

    // Tests EnumSet creation with a valid enum type
    @Test
    public void testGet_enumSetWithValidEnumType_returnsEmptyEnumSet() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet<SampleEnum>> typeToken = new TypeToken<EnumSet<SampleEnum>>() {};
        ObjectConstructor<EnumSet> constructor = constructorConstructor.get(typeToken);
        assertNotNull(constructor);
        EnumSet instance = constructor.construct();
        assertTrue(instance.isEmpty());
    }

    // Tests EnumSet with a generic type argument that is not a Class (should throw JsonIOException)
    @Test(expected = JsonIOException.class)
    public void testGet_enumSetWithNonClassType_throwsJsonIOException() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        // Create a type that is ParameterizedType but element type is not a Class
        TypeToken<EnumSet<Comparable>> typeToken = new TypeToken<EnumSet<Comparable>>() {};
        ObjectConstructor<EnumSet> constructor = constructorConstructor.get(typeToken);
        constructor.construct();
    }

    // ========== New test cases for uncovered coverage ==========
    
    // Tests that an interface without default implementation falls back to UnsafeAllocator
    @Test
    public void testGet_interfaceWithoutDefaultMapping_usesUnsafeAllocator() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        // RandomAccess is an interface not in default mappings
        ObjectConstructor<RandomAccess> constructor = constructorConstructor.get(TypeToken.get(RandomAccess.class));
        assertNotNull(constructor);
        RandomAccess instance = constructor.construct();
        assertNotNull(instance);
    }

    // Tests that a class with private constructor falls back to UnsafeAllocator
    @Test
    public void testGet_classWithPrivateConstructor_usesUnsafeAllocator() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<PrivateConstructorClass> constructor = constructorConstructor.get(TypeToken.get(PrivateConstructorClass.class));
        assertNotNull(constructor);
        PrivateConstructorClass instance = constructor.construct();
        assertNotNull(instance);
    }

    // Tests that a class with only parameterized constructor falls back to UnsafeAllocator
    @Test
    public void testGet_classWithOnlyParameterizedConstructor_usesUnsafeAllocator() {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<NoDefaultConstructorClass> constructor = constructorConstructor.get(TypeToken.get(NoDefaultConstructorClass.class));
        assertNotNull(constructor);
        NoDefaultConstructorClass instance = constructor.construct();
        assertNotNull(instance);
    }

    // Simple helper class for testing instance creators
    private static class Simple {
        String value;
        Simple() { this.value = "default"; }
        Simple(String value) { this.value = value; }
    }

    // Sample enum for EnumSet testing
    private enum SampleEnum { A, B, C }

    // Helper class with private constructor
    private static class PrivateConstructorClass {
        private PrivateConstructorClass() {
            // private constructor
        }
    }

    // Helper class with no default constructor (only parameterized constructor)
    private static class NoDefaultConstructorClass {
        private final int x;
        
        public NoDefaultConstructorClass(int x) {
            this.x = x;
        }
    }
}
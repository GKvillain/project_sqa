package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConstructorConstructorTest {

  private enum TestEnum {
    FOO, BAR
  }

  private static class PrivateConstructorClass {
    private PrivateConstructorClass() {}
  }

  private static class ThrowingConstructorClass {
    public ThrowingConstructorClass() {
      throw new IllegalStateException("Test exception");
    }
  }

  private static class NoDefaultConstructorClass {
    public final String name;
    public NoDefaultConstructorClass(String name) {
      this.name = name;
    }
  }

  // Tests instance creator registered for exact Type
  @Test
  public void testGet_registeredTypeCreator_returnsCreatedInstance() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    final String expected = "custom-instance";
    creators.put(String.class, new InstanceCreator<String>() {
      @Override public String createInstance(Type type) {
        return expected;
      }
    });

    ConstructorConstructor constructorConstructor = new ConstructorConstructor(creators);
    ObjectConstructor<String> constructor = constructorConstructor.get(TypeToken.get(String.class));
    assertEquals(expected, constructor.construct());
  }

  // Tests instance creator registered for raw Class type
  @Test
  public void testGet_registeredRawTypeCreator_returnsCreatedInstance() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    final List<String> expected = new ArrayList<String>();
    creators.put(List.class, new InstanceCreator<List<String>>() {
      @Override public List<String> createInstance(Type type) {
        return expected;
      }
    });

    ConstructorConstructor constructorConstructor = new ConstructorConstructor(creators);
    TypeToken<List<String>> typeToken = new TypeToken<List<String>>() {};
    ObjectConstructor<List<String>> constructor = constructorConstructor.get(typeToken);
    assertEquals(expected, constructor.construct());
  }

  // Tests public no-arg default constructor
  @Test
  public void testGet_defaultPublicConstructor_createsInstance() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<ArrayList> constructor =
        constructorConstructor.get(TypeToken.get(ArrayList.class));
    ArrayList<?> instance = constructor.construct();
    assertNotNull(instance);
    assertTrue(instance.isEmpty());
  }

  // Tests accessible reflection for private default constructor
  @Test
  public void testGet_defaultPrivateConstructor_createsInstance() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<PrivateConstructorClass> constructor =
        constructorConstructor.get(TypeToken.get(PrivateConstructorClass.class));
    PrivateConstructorClass instance = constructor.construct();
    assertNotNull(instance);
  }

  // Tests default constructor throwing InvocationTargetException
  @Test(expected = RuntimeException.class)
  public void testGet_constructorThrowsException_throwsRuntimeException() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<ThrowingConstructorClass> constructor =
        constructorConstructor.get(TypeToken.get(ThrowingConstructorClass.class));
    constructor.construct();
  }

  // Tests SortedSet default implementation creation
  @Test
  public void testGet_sortedSetInterface_returnsTreeSet() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<SortedSet<String>> typeToken = new TypeToken<SortedSet<String>>() {};
    ObjectConstructor<SortedSet<String>> constructor = constructorConstructor.get(typeToken);
    SortedSet<String> instance = constructor.construct();
    assertTrue(instance instanceof TreeSet);
  }

  // Tests EnumSet default implementation creation with valid ParameterizedType
  @Test
  public void testGet_enumSetParameterizedType_returnsEnumSet() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<EnumSet<TestEnum>> typeToken = new TypeToken<EnumSet<TestEnum>>() {};
    ObjectConstructor<EnumSet<TestEnum>> constructor = constructorConstructor.get(typeToken);
    EnumSet<TestEnum> instance = constructor.construct();
    assertNotNull(instance);
    assertTrue(instance.isEmpty());
  }

  // Tests EnumSet raw type without type argument throws JsonIOException
  @Test(expected = JsonIOException.class)
  public void testGet_enumSetRawType_throwsJsonIOException() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<EnumSet> constructor =
        constructorConstructor.get(TypeToken.get(EnumSet.class));
    constructor.construct();
  }

  // Tests Set default implementation creation
  @Test
  public void testGet_setInterface_returnsLinkedHashSet() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Set<String>> typeToken = new TypeToken<Set<String>>() {};
    ObjectConstructor<Set<String>> constructor = constructorConstructor.get(typeToken);
    Set<String> instance = constructor.construct();
    assertTrue(instance instanceof LinkedHashSet);
  }

  // Tests Queue default implementation creation
  @Test
  public void testGet_queueInterface_returnsLinkedList() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Queue<String>> typeToken = new TypeToken<Queue<String>>() {};
    ObjectConstructor<Queue<String>> constructor = constructorConstructor.get(typeToken);
    Queue<String> instance = constructor.construct();
    assertTrue(instance instanceof LinkedList);
  }

  // Tests Collection/List default implementation creation
  @Test
  public void testGet_collectionInterface_returnsArrayList() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Collection<String>> typeToken = new TypeToken<Collection<String>>() {};
    ObjectConstructor<Collection<String>> constructor = constructorConstructor.get(typeToken);
    Collection<String> instance = constructor.construct();
    assertTrue(instance instanceof ArrayList);
  }

  // Tests SortedMap default implementation creation
  @Test
  public void testGet_sortedMapInterface_returnsTreeMap() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<SortedMap<String, String>> typeToken =
        new TypeToken<SortedMap<String, String>>() {};
    ObjectConstructor<SortedMap<String, String>> constructor =
        constructorConstructor.get(typeToken);
    SortedMap<String, String> instance = constructor.construct();
    assertTrue(instance instanceof TreeMap);
  }

  // Tests Map default implementation with non-String key type
  @Test
  public void testGet_mapWithNonStringKey_returnsLinkedHashMap() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Map<Integer, String>> typeToken = new TypeToken<Map<Integer, String>>() {};
    ObjectConstructor<Map<Integer, String>> constructor =
        constructorConstructor.get(typeToken);
    Map<Integer, String> instance = constructor.construct();
    assertTrue(instance instanceof LinkedHashMap);
  }

  // Tests Map default implementation with String key type
  @Test
  public void testGet_mapWithStringKey_returnsLinkedTreeMap() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
    ObjectConstructor<Map<String, Object>> constructor =
        constructorConstructor.get(typeToken);
    Map<String, Object> instance = constructor.construct();
    assertTrue(instance instanceof LinkedTreeMap);
  }

  // Tests raw Map default implementation
  @Test
  public void testGet_rawMap_returnsLinkedTreeMap() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<Map> constructor = constructorConstructor.get(TypeToken.get(Map.class));
    Map<?, ?> instance = constructor.construct();
    assertTrue(instance instanceof LinkedTreeMap);
  }

  // Tests fallback to UnsafeAllocator for classes without default constructor
  @Test
  public void testGet_classWithoutDefaultConstructor_allocatesViaUnsafe() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<NoDefaultConstructorClass> constructor =
        constructorConstructor.get(TypeToken.get(NoDefaultConstructorClass.class));
    NoDefaultConstructorClass instance = constructor.construct();
    assertNotNull(instance);
  }

  // Tests toString method
  @Test
  public void testToString_returnsInstanceCreatorsString() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    ConstructorConstructor constructorConstructor = new ConstructorConstructor(creators);
    assertEquals(creators.toString(), constructorConstructor.toString());
  }
}
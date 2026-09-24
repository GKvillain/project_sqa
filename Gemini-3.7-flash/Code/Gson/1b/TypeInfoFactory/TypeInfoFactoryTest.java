package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TypeInfoFactoryTest {

  private static class SimpleClass {
    public int intField;
    public String stringField;
    public int[] intArrayField;
    public String[] stringArrayField;
  }

  private static class GenericClass<T> {
    public T value;
    public T[] arrayValue;
    public List<T> listValue;
  }

  private static class MultiGenericClass<K, V> {
    public K key;
    public V val;
    public Map<K, V> mapValue;
  }

  private static class WildcardClass {
    public List<? extends Number> wildcardList;
  }

  private static class UnboundedWildcardClass {
    public List<?> wildcardList;
  }

  private static class NestedGenericClass<K, V> {
    public Map<K, List<V>> nestedMap;
  }

  // Tests private constructor for code coverage
  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<TypeInfoFactory> constructor = TypeInfoFactory.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    TypeInfoFactory instance = constructor.newInstance();
    assertNotNull(instance);
  }

  // Tests getting TypeInfoArray for primitive array class
  @Test
  public void testGetTypeInfoForArray_primitiveArrayType_returnsTypeInfoArray() {
    TypeInfoArray typeInfo = TypeInfoFactory.getTypeInfoForArray(int[].class);
    assertNotNull(typeInfo);
    assertEquals(int.class, typeInfo.getComponentRawType());
    assertEquals(int.class, typeInfo.getGenericComponentType());
  }

  // Tests getting TypeInfoArray for Object array class
  @Test
  public void testGetTypeInfoForArray_objectArrayType_returnsTypeInfoArray() {
    TypeInfoArray typeInfo = TypeInfoFactory.getTypeInfoForArray(String[].class);
    assertNotNull(typeInfo);
    assertEquals(String.class, typeInfo.getComponentRawType());
    assertEquals(String.class, typeInfo.getGenericComponentType());
  }

  // Tests getting TypeInfoArray for GenericArrayType
  @Test
  public void testGetTypeInfoForArray_genericArrayType_returnsTypeInfoArray() throws Exception {
    Field field = GenericClass.class.getField("arrayValue");
    Type genericArrayType = field.getGenericType();
    assertTrue(genericArrayType instanceof GenericArrayType);
    TypeInfoArray typeInfo = TypeInfoFactory.getTypeInfoForArray(genericArrayType);
    assertNotNull(typeInfo);
    assertEquals(Object.class, typeInfo.getComponentRawType());
  }

  // Tests getTypeInfoForArray with non-array type throwing IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testGetTypeInfoForArray_nonArrayType_throwsException() {
    TypeInfoFactory.getTypeInfoForArray(String.class);
  }

  // Tests primitive field extraction
  @Test
  public void testGetTypeInfoForField_primitiveField_returnsExactType() throws Exception {
    Field field = SimpleClass.class.getField("intField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
    assertNotNull(typeInfo);
    assertEquals(int.class, typeInfo.getActualType());
  }

  // Tests simple Object field extraction
  @Test
  public void testGetTypeInfoForField_objectField_returnsExactType() throws Exception {
    Field field = SimpleClass.class.getField("stringField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
    assertNotNull(typeInfo);
    assertEquals(String.class, typeInfo.getActualType());
  }

  // Tests array field extraction on non-generic class
  @Test
  public void testGetTypeInfoForField_arrayField_returnsArrayType() throws Exception {
    Field field = SimpleClass.class.getField("stringArrayField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
    assertNotNull(typeInfo);
    assertEquals(String[].class, typeInfo.getActualType());
  }

  // Tests generic TypeVariable resolution with ParameterizedType parent
  @Test
  public void testGetTypeInfoForField_typeVariableField_resolvesActualType() throws Exception {
    Field field = GenericClass.class.getField("value");
    Type parentType = new GenericClass<Integer>() {}.getClass().getGenericSuperclass();
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parentType);
    assertNotNull(typeInfo);
    assertEquals(Integer.class, typeInfo.getActualType());
  }

  // Tests generic array TypeVariable resolution with ParameterizedType parent
  @Test
  public void testGetTypeInfoForField_genericArrayField_resolvesActualArrayType() throws Exception {
    Field field = GenericClass.class.getField("arrayValue");
    Type parentType = new GenericClass<Integer>() {}.getClass().getGenericSuperclass();
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parentType);
    assertNotNull(typeInfo);
    assertEquals(Integer[].class, typeInfo.getActualType());
  }

  // Tests parameterized type field containing TypeVariable resolution
  @Test
  public void testGetTypeInfoForField_parameterizedFieldWithTypeVariable_resolvesNestedType() throws Exception {
    Field field = GenericClass.class.getField("listValue");
    Type parentType = new GenericClass<String>() {}.getClass().getGenericSuperclass();
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parentType);
    assertNotNull(typeInfo);
    assertTrue(typeInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) typeInfo.getActualType();
    assertEquals(List.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  // Tests multiple generic TypeVariables resolution
  @Test
  public void testGetTypeInfoForField_multipleTypeVariables_resolvesBoth() throws Exception {
    Type parentType = new MultiGenericClass<String, Long>() {}.getClass().getGenericSuperclass();

    Field keyField = MultiGenericClass.class.getField("key");
    TypeInfo keyInfo = TypeInfoFactory.getTypeInfoForField(keyField, parentType);
    assertEquals(String.class, keyInfo.getActualType());

    Field valField = MultiGenericClass.class.getField("val");
    TypeInfo valInfo = TypeInfoFactory.getTypeInfoForField(valField, parentType);
    assertEquals(Long.class, valInfo.getActualType());
  }

  // Tests parameterized map field with multiple TypeVariables
  @Test
  public void testGetTypeInfoForField_multiGenericParameterizedField_resolvesMapArguments() throws Exception {
    Field mapField = MultiGenericClass.class.getField("mapValue");
    Type parentType = new MultiGenericClass<String, Long>() {}.getClass().getGenericSuperclass();
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(mapField, parentType);
    assertNotNull(typeInfo);
    assertTrue(typeInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) typeInfo.getActualType();
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Long.class, paramType.getActualTypeArguments()[1]);
  }

  // Tests nested parameterized type with type variable resolution
  @Test
  public void testGetTypeInfoForField_nestedParameterizedType_resolvesCorrectly() throws Exception {
    Field field = NestedGenericClass.class.getField("nestedMap");
    Type parentType = new NestedGenericClass<String, Integer>() {}.getClass().getGenericSuperclass();
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parentType);
    assertNotNull(typeInfo);
    assertTrue(typeInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType mapType = (ParameterizedType) typeInfo.getActualType();
    assertEquals(Map.class, mapType.getRawType());
    assertEquals(String.class, mapType.getActualTypeArguments()[0]);
    assertTrue(mapType.getActualTypeArguments()[1] instanceof ParameterizedType);
    ParameterizedType listType = (ParameterizedType) mapType.getActualTypeArguments()[1];
    assertEquals(List.class, listType.getRawType());
    assertEquals(Integer.class, listType.getActualTypeArguments()[0]);
  }

  // Tests TypeVariable without ParameterizedType parent throwing UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetTypeInfoForField_rawParentClassWithTypeVariable_throwsUnsupportedOperationException() throws Exception {
    Field field = GenericClass.class.getField("value");
    TypeInfoFactory.getTypeInfoForField(field, GenericClass.class);
  }

  // Tests wildcard type resolution inside parameterized field
  @Test
  public void testGetTypeInfoForField_wildcardTypeField_resolvesUpperBound() throws Exception {
    Field field = WildcardClass.class.getField("wildcardList");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, WildcardClass.class);
    assertNotNull(typeInfo);
    assertTrue(typeInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) typeInfo.getActualType();
    assertEquals(Number.class, paramType.getActualTypeArguments()[0]);
  }

  // Tests unbounded wildcard type resolution inside parameterized field
  @Test
  public void testGetTypeInfoForField_unboundedWildcardTypeField_resolvesToObject() throws Exception {
    Field field = UnboundedWildcardClass.class.getField("wildcardList");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, UnboundedWildcardClass.class);
    assertNotNull(typeInfo);
    assertTrue(typeInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) typeInfo.getActualType();
    assertEquals(Object.class, paramType.getActualTypeArguments()[0]);
  }
}
package com.fasterxml.jackson.databind.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class TypeBindingsTest {

    private TypeFactory _typeFactory;
    private JavaType _stringType;
    private JavaType _integerType;
    private JavaType _booleanType;

    static class Triplet<A, B, C> { }
    static class NonGeneric { }

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _stringType = _typeFactory.constructType(String.class);
        _integerType = _typeFactory.constructType(Integer.class);
        _booleanType = _typeFactory.constructType(Boolean.class);
    }

    // Tests emptyBindings factory and its basic properties
    @Test
    public void testEmptyBindings_returnsEmptyInstance() {
        TypeBindings empty = TypeBindings.emptyBindings();
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertTrue(empty.getTypeParameters().isEmpty());
        assertEquals("<>", empty.toString());
        assertNull(empty.getBoundName(0));
        assertNull(empty.getBoundName(-1));
        assertNull(empty.getBoundType(0));
        assertNull(empty.getBoundType(-1));
        assertNull(empty.findBoundType("T"));
        assertFalse(empty.hasUnbound("T"));
        assertEquals(0, empty.typeParameterArray().length);
    }

    // Tests creating TypeBindings for 1 type parameter via TypeParamStash and custom class
    @Test
    public void testCreate_oneTypeArg_success() {
        TypeBindings bindingsList = TypeBindings.create(List.class, _stringType);
        assertEquals(1, bindingsList.size());
        assertFalse(bindingsList.isEmpty());
        assertEquals("E", bindingsList.getBoundName(0));
        assertSame(_stringType, bindingsList.getBoundType(0));
        assertSame(_stringType, bindingsList.findBoundType("E"));
        assertNull(bindingsList.findBoundType("X"));

        TypeBindings bindingsArrayList = TypeBindings.create(ArrayList.class, _stringType);
        assertEquals(1, bindingsArrayList.size());
        assertEquals("E", bindingsArrayList.getBoundName(0));

        TypeBindings bindingsCollection = TypeBindings.create(Collection.class, _stringType);
        assertEquals(1, bindingsCollection.size());

        TypeBindings bindingsIterable = TypeBindings.create(Iterable.class, _stringType);
        assertEquals(1, bindingsIterable.size());

        TypeBindings bindingsAbstractList = TypeBindings.create(AbstractList.class, _stringType);
        assertEquals(1, bindingsAbstractList.size());
    }

    // Tests create with 1 type parameter expecting mismatch exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_oneTypeArg_mismatchThrowsException() {
        TypeBindings.create(Map.class, _stringType);
    }

    // Tests create with 2 type parameters via TypeParamStash
    @Test
    public void testCreate_twoTypeArgs_success() {
        TypeBindings bindingsMap = TypeBindings.create(Map.class, _stringType, _integerType);
        assertEquals(2, bindingsMap.size());
        assertEquals("K", bindingsMap.getBoundName(0));
        assertEquals("V", bindingsMap.getBoundName(1));
        assertSame(_stringType, bindingsMap.getBoundType(0));
        assertSame(_integerType, bindingsMap.getBoundType(1));

        TypeBindings bindingsHashMap = TypeBindings.create(HashMap.class, _stringType, _integerType);
        assertEquals(2, bindingsHashMap.size());

        TypeBindings bindingsLinkedHashMap = TypeBindings.create(LinkedHashMap.class, _stringType, _integerType);
        assertEquals(2, bindingsLinkedHashMap.size());
    }

    // Tests create with 2 type parameters expecting mismatch exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_twoTypeArgs_mismatchThrowsException() {
        TypeBindings.create(List.class, _stringType, _integerType);
    }

    // Tests create with array of 3 type parameters for custom generic class
    @Test
    public void testCreate_typeArray_success() {
        JavaType[] types = new JavaType[] { _stringType, _integerType, _booleanType };
        TypeBindings bindings = TypeBindings.create(Triplet.class, types);

        assertEquals(3, bindings.size());
        assertEquals("A", bindings.getBoundName(0));
        assertEquals("B", bindings.getBoundName(1));
        assertEquals("C", bindings.getBoundName(2));
        assertSame(_stringType, bindings.findBoundType("A"));
        assertSame(_integerType, bindings.findBoundType("B"));
        assertSame(_booleanType, bindings.findBoundType("C"));
    }

    // Tests create with array count mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_typeArray_mismatchThrowsException() {
        JavaType[] types = new JavaType[] { _stringType };
        TypeBindings.create(Triplet.class, types);
    }

    // Tests create with List of JavaType
    @Test
    public void testCreate_typeList_success() {
        List<JavaType> typeList = Arrays.asList(_stringType, _integerType);
        TypeBindings bindings = TypeBindings.create(Map.class, typeList);
        assertEquals(2, bindings.size());

        TypeBindings emptyFromNull = TypeBindings.create(NonGeneric.class, (List<JavaType>) null);
        assertTrue(emptyFromNull.isEmpty());

        TypeBindings emptyFromEmptyList = TypeBindings.create(NonGeneric.class, Collections.<JavaType>emptyList());
        assertTrue(emptyFromEmptyList.isEmpty());
    }

    // Tests createIfNeeded with 1 type argument
    @Test
    public void testCreateIfNeeded_oneTypeArg_successAndEmptyFallback() {
        TypeBindings bindingsNonGeneric = TypeBindings.createIfNeeded(NonGeneric.class, _stringType);
        assertTrue(bindingsNonGeneric.isEmpty());

        TypeBindings bindingsList = TypeBindings.createIfNeeded(List.class, _stringType);
        assertEquals(1, bindingsList.size());
        assertSame(_stringType, bindingsList.getBoundType(0));
    }

    // Tests createIfNeeded with 1 type argument throwing exception on mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded_oneTypeArg_mismatchThrowsException() {
        TypeBindings.createIfNeeded(Map.class, _stringType);
    }

    // Tests createIfNeeded with array of JavaTypes
    @Test
    public void testCreateIfNeeded_typeArray_successAndEmptyFallback() {
        TypeBindings emptyForNonGeneric = TypeBindings.createIfNeeded(NonGeneric.class, new JavaType[] { _stringType });
        assertTrue(emptyForNonGeneric.isEmpty());

        TypeBindings emptyForNullArray = TypeBindings.createIfNeeded(NonGeneric.class, (JavaType[]) null);
        assertTrue(emptyForNullArray.isEmpty());

        TypeBindings bindings = TypeBindings.createIfNeeded(Map.class, new JavaType[] { _stringType, _integerType });
        assertEquals(2, bindings.size());
    }

    // Tests createIfNeeded with array count mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded_typeArray_mismatchThrowsException() {
        TypeBindings.createIfNeeded(Map.class, new JavaType[] { _stringType });
    }

    // Tests withUnboundVariable and hasUnbound behavior
    @Test
    public void testWithUnboundVariable_andHasUnbound() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        assertFalse(bindings.hasUnbound("T"));

        TypeBindings withT = bindings.withUnboundVariable("T");
        assertTrue(withT.hasUnbound("T"));
        assertFalse(withT.hasUnbound("U"));

        TypeBindings withTU = withT.withUnboundVariable("U");
        assertTrue(withTU.hasUnbound("T"));
        assertTrue(withTU.hasUnbound("U"));
        assertFalse(withTU.hasUnbound("V"));
    }

    // Tests findBoundType with ResolvedRecursiveType unwrapping
    @Test
    public void testFindBoundType_resolvedRecursiveType() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        TypeBindings bindings = TypeBindings.create(List.class, recursiveType);

        // Before self-referenced type is set
        assertSame(recursiveType, bindings.findBoundType("E"));

        // After self-referenced type is set
        recursiveType.setReference(_stringType);
        assertSame(_stringType, bindings.findBoundType("E"));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_contracts() {
        TypeBindings b1 = TypeBindings.create(Map.class, _stringType, _integerType);
        TypeBindings b2 = TypeBindings.create(Map.class, _stringType, _integerType);
        TypeBindings b3 = TypeBindings.create(Map.class, _stringType, _booleanType);
        TypeBindings b4 = TypeBindings.create(List.class, _stringType);

        assertTrue(b1.equals(b1));
        assertTrue(b1.equals(b2));
        assertEquals(b1.hashCode(), b2.hashCode());

        assertFalse(b1.equals(null));
        assertFalse(b1.equals("different-type"));
        assertFalse(b1.equals(b3));
        assertFalse(b1.equals(b4));
    }

    // Tests toString formatting
    @Test
    public void testToString_formatting() {
        assertEquals("<>", TypeBindings.emptyBindings().toString());

        TypeBindings b1 = TypeBindings.create(List.class, _stringType);
        assertEquals("<" + _stringType.getGenericSignature() + ">", b1.toString());

        TypeBindings b2 = TypeBindings.create(Map.class, _stringType, _integerType);
        assertEquals("<" + _stringType.getGenericSignature() + "," + _integerType.getGenericSignature() + ">", b2.toString());
    }

    // Tests serialization and readResolve canonicalization
    @Test
    public void testSerialization_canonicalizesEmpty() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(TypeBindings.emptyBindings());
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object result = in.readObject();
        in.close();

        assertSame(TypeBindings.emptyBindings(), result);
    }
}
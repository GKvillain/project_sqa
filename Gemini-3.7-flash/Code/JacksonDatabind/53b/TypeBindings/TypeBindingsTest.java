package com.fasterxml.jackson.databind.type;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.JavaType;

public class TypeBindingsTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();
    private final JavaType _strType = _typeFactory.constructType(String.class);
    private final JavaType _intType = _typeFactory.constructType(Integer.class);
    private final JavaType _longType = _typeFactory.constructType(Long.class);
    private final JavaType _boolType = _typeFactory.constructType(Boolean.class);

    static class CustomSingle<T> {}
    static class CustomPair<A, B> {}
    static class CustomTriple<X, Y, Z> {}
    static class CustomNonGeneric {}

    // Tests emptyBindings factory method
    @Test
    public void testEmptyBindings_returnsEmptyInstance() {
        TypeBindings empty = TypeBindings.emptyBindings();
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertEquals("<>", empty.toString());
        assertTrue(empty.getTypeParameters().isEmpty());
        assertNull(empty.getBoundName(0));
        assertNull(empty.getBoundType(0));
        assertNull(empty.findBoundType("T"));
        assertFalse(empty.hasUnbound("T"));
    }

    // Tests create with single type parameter using stashed types (e.g. List)
    @Test
    public void testCreate_singleTypeParam_stashedClass_success() {
        TypeBindings bindings = TypeBindings.create(List.class, _strType);
        assertEquals(1, bindings.size());
        assertFalse(bindings.isEmpty());
        assertEquals("E", bindings.getBoundName(0));
        assertEquals(_strType, bindings.getBoundType(0));
        assertEquals(_strType, bindings.findBoundType("E"));
        assertNull(bindings.findBoundType("K"));
        assertNull(bindings.getBoundName(-1));
        assertNull(bindings.getBoundName(1));
        assertNull(bindings.getBoundType(-1));
        assertNull(bindings.getBoundType(1));
    }

    // Tests create with two type parameters using stashed types (e.g. Map)
    @Test
    public void testCreate_twoTypeParams_stashedClass_success() {
        TypeBindings bindings = TypeBindings.create(Map.class, _strType, _intType);
        assertEquals(2, bindings.size());
        assertEquals("K", bindings.getBoundName(0));
        assertEquals(_strType, bindings.getBoundType(0));
        assertEquals("V", bindings.getBoundName(1));
        assertEquals(_intType, bindings.getBoundType(1));
        assertEquals(_strType, bindings.findBoundType("K"));
        assertEquals(_intType, bindings.findBoundType("V"));
    }

    // Tests create with JavaType list
    @Test
    public void testCreate_withJavaTypeList_success() {
        List<JavaType> typeList = Arrays.asList(_strType, _intType);
        TypeBindings bindings = TypeBindings.create(Map.class, typeList);
        assertEquals(2, bindings.size());
        assertEquals(_strType, bindings.getBoundType(0));
        assertEquals(_intType, bindings.getBoundType(1));

        TypeBindings emptyFromList = TypeBindings.create(String.class, (List<JavaType>) null);
        assertTrue(emptyFromList.isEmpty());

        TypeBindings emptyFromEmptyList = TypeBindings.create(String.class, Collections.<JavaType>emptyList());
        assertTrue(emptyFromEmptyList.isEmpty());
    }

    // Tests create with JavaType array containing multiple parameters
    @Test
    public void testCreate_withArrayNullAndZeroArgs_success() {
        TypeBindings bindings = TypeBindings.create(String.class, (JavaType[]) null);
        assertTrue(bindings.isEmpty());

        TypeBindings bindingsZero = TypeBindings.create(String.class, new JavaType[0]);
        assertTrue(bindingsZero.isEmpty());
    }

    // Tests create throwing exception on parameter length mismatch (1 param expected)
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_singleTypeArgMismatch_throwsException() {
        TypeBindings.create(String.class, _strType);
    }

    // Tests create throwing exception on parameter length mismatch (2 params expected)
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_twoTypeArgsMismatch_throwsException() {
        TypeBindings.create(List.class, _strType, _intType);
    }

    // Tests create throwing exception on array parameter length mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_arrayMismatch_throwsException() {
        TypeBindings.create(Map.class, new JavaType[] { _strType });
    }

    // Tests createIfNeeded with single type argument on non-generic class returning empty
    @Test
    public void testCreateIfNeeded_singleArgNonGeneric_returnsEmpty() {
        TypeBindings bindings = TypeBindings.createIfNeeded(String.class, _strType);
        assertTrue(bindings.isEmpty());
    }

    // Tests createIfNeeded with single type argument on generic class
    @Test
    public void testCreateIfNeeded_singleArgGeneric_createsBindings() {
        TypeBindings bindings = TypeBindings.createIfNeeded(List.class, _strType);
        assertEquals(1, bindings.size());
        assertEquals(_strType, bindings.getBoundType(0));
    }

    // Tests createIfNeeded with single type argument throwing exception when mismatching
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded_singleArgMismatch_throwsException() {
        TypeBindings.createIfNeeded(Map.class, _strType);
    }

    // Tests createIfNeeded with array on non-generic and generic class
    @Test
    public void testCreateIfNeeded_arrayArgs_handlesAppropriately() {
        TypeBindings nonGeneric = TypeBindings.createIfNeeded(String.class, new JavaType[] { _strType });
        assertTrue(nonGeneric.isEmpty());

        TypeBindings genericNullTypes = TypeBindings.createIfNeeded(List.class, (JavaType[]) null);
        assertEquals(0, genericNullTypes.size());

        TypeBindings generic = TypeBindings.createIfNeeded(Map.class, new JavaType[] { _strType, _intType });
        assertEquals(2, generic.size());
    }

    // Tests createIfNeeded with array throwing exception when parameter count mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded_arrayMismatch_throwsException() {
        TypeBindings.createIfNeeded(Map.class, new JavaType[] { _strType });
    }

    // Tests unbound variable management
    @Test
    public void testWithUnboundVariable_tracksUnboundVariablesCorrectly() {
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

    // Tests findBoundType with ResolvedRecursiveType resolution
    @Test
    public void testFindBoundType_withRecursiveType_returnsResolved() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(List.class, TypeBindings.emptyBindings());
        TypeBindings bindings = TypeBindings.create(List.class, recursiveType);
        
        assertEquals(recursiveType, bindings.findBoundType("E"));

        recursiveType.setReference(_strType);
        assertEquals(_strType, bindings.findBoundType("E"));
    }

    // Tests toString formatting
    @Test
    public void testToString_formatsGenericSignatures() {
        assertEquals("<>", TypeBindings.emptyBindings().toString());

        TypeBindings single = TypeBindings.create(List.class, _strType);
        assertEquals("<" + _strType.getGenericSignature() + ">", single.toString());

        TypeBindings pair = TypeBindings.create(Map.class, _strType, _intType);
        assertEquals("<" + _strType.getGenericSignature() + "," + _intType.getGenericSignature() + ">", pair.toString());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_verifiesContract() {
        TypeBindings b1 = TypeBindings.create(Map.class, _strType, _intType);
        TypeBindings b2 = TypeBindings.create(Map.class, _strType, _intType);
        TypeBindings b3 = TypeBindings.create(Map.class, _strType, _longType);
        TypeBindings b4 = TypeBindings.create(List.class, _strType);

        assertTrue(b1.equals(b1));
        assertTrue(b1.equals(b2));
        assertEquals(b1.hashCode(), b2.hashCode());

        assertFalse(b1.equals(null));
        assertFalse(b1.equals("differentType"));
        assertFalse(b1.equals(b3));
        assertFalse(b1.equals(b4));
    }

    // Tests typeParameterArray and getTypeParameters list accessors
    @Test
    public void testTypeParameterArrayAndList_returnsElements() {
        TypeBindings bindings = TypeBindings.create(Map.class, _strType, _intType);
        JavaType[] array = bindings.typeParameterArray();
        assertEquals(2, array.length);
        assertEquals(_strType, array[0]);
        assertEquals(_intType, array[1]);

        List<JavaType> list = bindings.getTypeParameters();
        assertEquals(2, list.size());
        assertEquals(_strType, list.get(0));
        assertEquals(_intType, list.get(1));
    }

    // Tests Stash lookup paths for different pre-cached collections
    @Test
    public void testTypeParamStash_coversVariousStashedClasses() {
        TypeBindings bArrayList = TypeBindings.create(ArrayList.class, _strType);
        assertEquals(1, bArrayList.size());

        TypeBindings bAbstractList = TypeBindings.create(AbstractList.class, _strType);
        assertEquals(1, bAbstractList.size());

        TypeBindings bCollection = TypeBindings.create(Collection.class, _strType);
        assertEquals(1, bCollection.size());

        TypeBindings bIterable = TypeBindings.create(Iterable.class, _strType);
        assertEquals(1, bIterable.size());

        TypeBindings bHashMap = TypeBindings.create(HashMap.class, _strType, _intType);
        assertEquals(2, bHashMap.size());

        TypeBindings bLinkedHashMap = TypeBindings.create(LinkedHashMap.class, _strType, _intType);
        assertEquals(2, bLinkedHashMap.size());
    }

    // Tests readResolve logic
    @Test
    public void testReadResolve_emptyReturnsStaticEmpty() {
        TypeBindings empty = TypeBindings.emptyBindings();
        assertSame(empty, empty.readResolve());

        TypeBindings nonEmpty = TypeBindings.create(List.class, _strType);
        assertSame(nonEmpty, nonEmpty.readResolve());
    }

    // --- New Tests ---

    // Tests custom non-stashed generic classes using reflection fallback
    @Test
    public void testCreate_customGenericClasses_reflectionFallback() {
        TypeBindings bSingle = TypeBindings.create(CustomSingle.class, _strType);
        assertEquals(1, bSingle.size());
        assertEquals("T", bSingle.getBoundName(0));
        assertEquals(_strType, bSingle.getBoundType(0));

        TypeBindings bPair = TypeBindings.create(CustomPair.class, _strType, _intType);
        assertEquals(2, bPair.size());
        assertEquals("A", bPair.getBoundName(0));
        assertEquals("B", bPair.getBoundName(1));
        assertEquals(_strType, bPair.getBoundType(0));
        assertEquals(_intType, bPair.getBoundType(1));

        TypeBindings bTriple = TypeBindings.create(CustomTriple.class, new JavaType[] { _strType, _intType, _boolType });
        assertEquals(3, bTriple.size());
        assertEquals("X", bTriple.getBoundName(0));
        assertEquals("Y", bTriple.getBoundName(1));
        assertEquals("Z", bTriple.getBoundName(2));
        assertEquals(_strType, bTriple.getBoundType(0));
        assertEquals(_intType, bTriple.getBoundType(1));
        assertEquals(_boolType, bTriple.getBoundType(2));
    }

    // Tests createIfNeeded with List<JavaType> parameter
    @Test
    public void testCreateIfNeeded_withJavaTypeList() {
        TypeBindings nonGeneric = TypeBindings.createIfNeeded(CustomNonGeneric.class, Arrays.asList(_strType));
        assertTrue(nonGeneric.isEmpty());

        TypeBindings genericNull = TypeBindings.createIfNeeded(CustomSingle.class, (List<JavaType>) null);
        assertTrue(genericNull.isEmpty());

        TypeBindings genericEmpty = TypeBindings.createIfNeeded(CustomSingle.class, Collections.<JavaType>emptyList());
        assertTrue(genericEmpty.isEmpty());

        TypeBindings single = TypeBindings.createIfNeeded(CustomSingle.class, Arrays.asList(_strType));
        assertEquals(1, single.size());
        assertEquals(_strType, single.getBoundType(0));

        TypeBindings pair = TypeBindings.createIfNeeded(CustomPair.class, Arrays.asList(_strType, _intType));
        assertEquals(2, pair.size());
        assertEquals(_strType, pair.getBoundType(0));
        assertEquals(_intType, pair.getBoundType(1));
    }

    // Tests createIfNeeded with List<JavaType> parameter mismatch throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded_withJavaTypeListMismatch_throwsException() {
        TypeBindings.createIfNeeded(CustomPair.class, Arrays.asList(_strType));
    }

    // Tests createIfNeeded with single type argument on custom classes
    @Test
    public void testCreateIfNeeded_customSingleArg() {
        TypeBindings nonGeneric = TypeBindings.createIfNeeded(CustomNonGeneric.class, _strType);
        assertTrue(nonGeneric.isEmpty());

        TypeBindings single = TypeBindings.createIfNeeded(CustomSingle.class, _strType);
        assertEquals(1, single.size());
        assertEquals(_strType, single.getBoundType(0));
    }

    // Tests typeParameterArray on empty bindings
    @Test
    public void testTypeParameterArray_emptyBindings() {
        JavaType[] array = TypeBindings.emptyBindings().typeParameterArray();
        assertNotNull(array);
        assertEquals(0, array.length);
    }

    // Tests withUnboundVariable when variable name already exists
    @Test
    public void testWithUnboundVariable_duplicateHandling() {
        TypeBindings b1 = TypeBindings.emptyBindings().withUnboundVariable("T");
        assertTrue(b1.hasUnbound("T"));

        TypeBindings b2 = b1.withUnboundVariable("T");
        assertTrue(b2.hasUnbound("T"));
    }

    // Tests create exception messages for non-stashed classes
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_customClassSingleMismatch_throwsException() {
        TypeBindings.create(CustomPair.class, _strType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_customClassTwoMismatch_throwsException() {
        TypeBindings.create(CustomSingle.class, _strType, _intType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_customClassArrayMismatch_throwsException() {
        TypeBindings.create(CustomTriple.class, new JavaType[] { _strType });
    }
}
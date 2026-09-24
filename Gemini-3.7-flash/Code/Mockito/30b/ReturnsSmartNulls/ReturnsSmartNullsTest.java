package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.SmartNullPointerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;

    interface Bar {
        void doSomething();
        String getName();
    }

    static class NonFinalClass {
        public String getMessage() {
            return "message";
        }
    }

    static final class FinalClass {
    }

    interface Foo {
        Bar getBar();
        Bar getBarWithArgs(String a, int b);
        Bar getBarWithNullArg(String a);
        FinalClass getFinalClass();
        NonFinalClass getNonFinalClass();
        int getInt();
        boolean getBoolean();
        Boolean getBooleanWrapper();
        Integer getIntegerWrapper();
        String getString();
        int[] getIntArray();
        String[] getStringArray();
        List<String> getList();
        Set<String> getSet();
        Map<String, String> getMap();
    }

    @Before
    public void setUp() {
        returnsSmartNulls = new ReturnsSmartNulls();
    }

    // Tests delegate returning default primitive value
    @Test
    public void testAnswer_primitiveType_returnsDefaultZero() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertEquals(0, foo.getInt());
    }

    // Tests delegate returning default empty String
    @Test
    public void testAnswer_stringType_returnsEmptyString() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertEquals("", foo.getString());
    }

    // Tests non-mockable final return type returns null
    @Test
    public void testAnswer_finalClass_returnsNull() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertNull(foo.getFinalClass());
    }

    // Tests mockable interface returns non-null SmartNull proxy
    @Test
    public void testAnswer_mockableType_returnsSmartNullProxy() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBar();
        assertNotNull(bar);
    }

    // Tests SmartNull toString without method arguments
    @Test
    public void testAnswer_smartNullToString_containsMethodName() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBar();
        String result = bar.toString();
        assertEquals("SmartNull returned by unstubbed getBar() method on mock", result);
    }

    // Tests SmartNull toString with arguments
    @Test
    public void testAnswer_smartNullToString_containsArguments() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBarWithArgs("test", 42);
        String result = bar.toString();
        assertEquals("SmartNull returned by unstubbed getBarWithArgs(test, 42) method on mock", result);
    }

    // Tests SmartNull toString with null argument
    @Test
    public void testAnswer_smartNullToString_containsNullArgument() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBarWithNullArg(null);
        String result = bar.toString();
        assertEquals("SmartNull returned by unstubbed getBarWithNullArg(null) method on mock", result);
    }

    // Tests invocation on SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testAnswer_invokingMethodOnSmartNull_throwsSmartNullPointerException() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBar();
        bar.doSomething();
    }

    // Tests exception message contains unstubbed method call details
    @Test
    public void testAnswer_invokingMethodOnSmartNull_exceptionContainsDetails() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        Bar bar = foo.getBarWithArgs("abc", 123);
        try {
            bar.getName();
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Tests delegate returning primitive boolean
    @Test
    public void testAnswer_primitiveBoolean_returnsFalse() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertFalse(foo.getBoolean());
    }

    // Tests delegate returning wrapper types
    @Test
    public void testAnswer_wrapperTypes_returnDefaultValues() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertEquals(Boolean.FALSE, foo.getBooleanWrapper());
        assertEquals(Integer.valueOf(0), foo.getIntegerWrapper());
    }

    // Tests delegate returning empty collections and maps
    @Test
    public void testAnswer_collectionsAndMaps_returnEmptyInstances() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertNotNull(foo.getList());
        assertTrue(foo.getList().isEmpty());
        assertNotNull(foo.getSet());
        assertTrue(foo.getSet().isEmpty());
        assertNotNull(foo.getMap());
        assertTrue(foo.getMap().isEmpty());
    }

    // Tests delegate returning empty arrays
    @Test
    public void testAnswer_arrays_returnEmptyArrays() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        assertNotNull(foo.getIntArray());
        assertEquals(0, foo.getIntArray().length);
        assertNotNull(foo.getStringArray());
        assertEquals(0, foo.getStringArray().length);
    }

    // Tests mockable non-final class returns SmartNull proxy
    @Test
    public void testAnswer_nonFinalClass_returnsSmartNullProxy() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        NonFinalClass nonFinal = foo.getNonFinalClass();
        assertNotNull(nonFinal);
        assertEquals("SmartNull returned by unstubbed getNonFinalClass() method on mock", nonFinal.toString());
    }

    // Tests invoking method on non-final class SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testAnswer_invokingMethodOnNonFinalClassSmartNull_throwsSmartNullPointerException() {
        Foo foo = Mockito.mock(Foo.class, returnsSmartNulls);
        NonFinalClass nonFinal = foo.getNonFinalClass();
        nonFinal.getMessage();
    }

    // Tests serialization of ReturnsSmartNulls
    @Test
    public void testSerialization_returnsSmartNullsIsSerializable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsSmartNulls);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsSmartNulls);
    }
}
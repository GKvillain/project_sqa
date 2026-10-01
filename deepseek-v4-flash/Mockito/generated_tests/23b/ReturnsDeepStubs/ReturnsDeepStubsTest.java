package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;

import org.junit.Test;
import org.mockito.MockSettings;

/*
 * Test class for ReturnsDeepStubs (Defects4J bug 23b).
 * Focuses on primitive/final return types, deep stub reuse, stubbing match, and extra interfaces.
 */
public class ReturnsDeepStubsTest {

    // Interfaces for testing
    interface PrimitiveReturn {
        int getInt();
        boolean getBoolean();
        double getDouble();
    }

    interface FinalReturn {
        String getString();
        Integer getInteger();
    }

    interface DeepFoo {
        DeepBar getBar();
    }

    interface DeepBar {
        DeepBaz getBaz();
    }

    interface DeepBaz {
        String getName();
    }

    interface Stubbable {
        StubbableItem getItem();
    }

    interface StubbableItem {
        String getValue();
    }

    interface GenericBound<T extends Comparable<T> & Serializable> {
        T get();
    }

    // Test primitive int return -> !isTypeMockable branch, delegate.returnValueFor returns 0
    @Test
    public void testAnswer_primitiveIntReturnType_returnsDefaultZero() {
        PrimitiveReturn mock = mock(PrimitiveReturn.class, RETURNS_DEEP_STUBS);
        assertEquals(0, mock.getInt());
    }

    // Test primitive boolean return -> returns false
    @Test
    public void testAnswer_primitiveBooleanReturnType_returnsDefaultFalse() {
        PrimitiveReturn mock = mock(PrimitiveReturn.class, RETURNS_DEEP_STUBS);
        assertFalse(mock.getBoolean());
    }

    // Test primitive double return -> returns 0.0
    @Test
    public void testAnswer_primitiveDoubleReturnType_returnsDefaultZero() {
        PrimitiveReturn mock = mock(PrimitiveReturn.class, RETURNS_DEEP_STUBS);
        assertEquals(0.0, mock.getDouble(), 0.0);
    }

    // Test final class String -> !isTypeMockable, delegate.returnValueFor returns null
    @Test
    public void testAnswer_finalStringReturnType_returnsNull() {
        FinalReturn mock = mock(FinalReturn.class, RETURNS_DEEP_STUBS);
        assertNull(mock.getString());
    }

    // Test final class Integer -> returns null
    @Test
    public void testAnswer_finalIntegerReturnType_returnsNull() {
        FinalReturn mock = mock(FinalReturn.class, RETURNS_DEEP_STUBS);
        assertNull(mock.getInteger());
    }

    // Test deep stub reuses mock for same invocation (key defect detection for bug 23b)
    @Test
    public void testGetMock_sameInvocation_returnsSameMock() {
        DeepFoo mock = mock(DeepFoo.class, RETURNS_DEEP_STUBS);
        DeepBar bar1 = mock.getBar();
        DeepBar bar2 = mock.getBar();
        assertSame("Deep stub should return same mock for identical invocation", bar1, bar2);
    }

    // Test stubbed nested method returns stubbed value (match branch in getMock)
    @Test
    public void testGetMock_stubbedInvocation_returnsStubbedAnswer() {
        Stubbable mock = mock(Stubbable.class, RETURNS_DEEP_STUBS);
        when(mock.getItem().getValue()).thenReturn("stubbed");
        assertEquals("stubbed", mock.getItem().getValue());
    }

    // Test deep stub returns non-null mock for chain calls
    @Test
    public void testAnswer_deepGenericNested_returnsNonNullMock() {
        DeepFoo mock = mock(DeepFoo.class, RETURNS_DEEP_STUBS);
        assertNotNull(mock.getBar());
        assertNotNull(mock.getBar().getBaz());
    }

    // Test extra interfaces branch (rawExtraInterfaces.length > 0)
    // Use generic bound type that adds extra interfaces beyond the raw type.
    @Test
    public void testWithSettings_extraInterfaces_returnsMockRespectingExtras() {
        GenericBound<?> mock = mock(GenericBound.class, RETURNS_DEEP_STUBS);
        Comparable<?> result = mock.get();
        assertNotNull("Should return a mock that implements the bound type", result);
        // The mock should also be serializable (extra interface)
        assertTrue("Deep stub mock should implement Serializable due to generic bound",
                   result instanceof java.io.Serializable);
    }
}
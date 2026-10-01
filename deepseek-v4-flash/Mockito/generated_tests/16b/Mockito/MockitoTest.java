package org.mockito;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit 4 test class for org.mockito.Mockito (Defects4J bug 16b).
 * Focuses on null-handling, boundary conditions, and core API methods.
 */
public class MockitoTest {

    // --- Normal cases and core behavior ---

    @Test
    public void testMock_defaultSettings_createsMock() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        assertNotNull(mock);
    }

    @Test
    public void testMock_withName_createsMock() {
        java.util.List mock = Mockito.mock(java.util.List.class, "testMock");
        assertNotNull(mock);
    }

    @Test
    public void testMock_withAnswer_createsMock() {
        java.util.List mock = Mockito.mock(java.util.List.class, Mockito.RETURNS_DEFAULTS);
        assertNotNull(mock);
    }

    @Test
    public void testSpy_realObject_createsSpy() {
        java.util.List real = new java.util.ArrayList();
        java.util.List spy = Mockito.spy(real);
        assertNotNull(spy);
    }

    @Test
    public void testWhen_stubbing_returnsStubbedValue() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.when(mock.get(0)).thenReturn("value");
        assertEquals("value", mock.get(0));
    }

    @Test
    public void testVerify_singleInvocation_passes() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        mock.add("one");
        Mockito.verify(mock).add("one");
    }

    @Test
    public void testDoReturn_nullValue_returnsNull() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.doReturn(null).when(mock).get(0);
        assertNull(mock.get(0));
    }

    @Test
    public void testDoNothing_voidMethod_stubs() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.doNothing().when(mock).clear();
        mock.clear();
        Mockito.verify(mock).clear();
    }

    @Test
    public void testReset_clearsStubbing() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.when(mock.size()).thenReturn(10);
        Mockito.reset(mock);
        // default return for int is 0 (primitive wrapper)
        assertEquals(0, mock.size());
    }

    @Test
    public void testVerifyZeroInteractions_noInteractions_passes() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.verifyZeroInteractions(mock);
    }

    @Test
    public void testInOrder_verifiesOrder() {
        java.util.List mock1 = Mockito.mock(java.util.List.class);
        java.util.List mock2 = Mockito.mock(java.util.List.class);
        mock1.add("first");
        mock2.add("second");
        org.mockito.InOrder inOrder = Mockito.inOrder(mock1, mock2);
        inOrder.verify(mock1).add("first");
        inOrder.verify(mock2).add("second");
    }

    @Test
    public void testTimes_zero_verificationMode() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        Mockito.verify(mock, Mockito.never()).add("notCalled");
    }

    @Test
    public void testAtLeastOnce_verificationMode() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        mock.add("x");
        Mockito.verify(mock, Mockito.atLeastOnce()).add("x");
    }

    @Test
    public void testAtMost_verificationMode() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        mock.add("x");
        Mockito.verify(mock, Mockito.atMost(1)).add("x");
    }

    @Test
    public void testOnly_verificationMode() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        mock.add("only");
        Mockito.verify(mock, Mockito.only()).add("only");
    }

    @Test
    public void testValidateMockitoUsage_noMisuse_passes() {
        Mockito.mock(java.util.List.class);
        Mockito.validateMockitoUsage();
    }

    @Test
    public void testWithSettings_returnsNonNull() {
        assertNotNull(Mockito.withSettings());
    }

    // --- Null/Invalid argument tests (core defect detection) ---

    @Test
    public void testMock_nullClass_throwsExceptionNotNPE() {
        try {
            Mockito.mock(null);
            fail("Expected exception for null class");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }

    @Test
    public void testSpy_nullObject_throwsExceptionNotNPE() {
        try {
            Mockito.spy(null);
            fail("Expected exception for null object");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }

    @Test
    public void testWhen_nullMethodCall_throwsExceptionNotNPE() {
        try {
            Mockito.when(null);
            fail("Expected exception for null method call");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }

    @Test
    public void testVerify_nullMock_throwsExceptionNotNPE() {
        try {
            Mockito.verify(null);
            fail("Expected exception for null mock");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }

    @Test
    public void testInOrder_nullVarargs_throwsExceptionNotNPE() {
        try {
            Mockito.inOrder((Object[]) null);
            fail("Expected exception for null varargs");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }

    @Test
    public void testDoThrow_nullException_throwsExceptionNotNPE() {
        java.util.List mock = Mockito.mock(java.util.List.class);
        try {
            Mockito.doThrow(null).when(mock).clear();
            fail("Expected exception for null throwable");
        } catch (Exception e) {
            assertFalse("Should not throw NullPointerException", e instanceof NullPointerException);
        }
    }
}
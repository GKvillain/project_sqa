package org.mockito.internal;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.Invocation;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.OngoingStubbing;

public class MockitoCoreTest {
    
    private final MockitoCore core = new MockitoCore();

    // Verify with null mock should throw RuntimeException (reporter.nullPassedToVerify)
    @Test(expected = RuntimeException.class)
    public void testVerify_nullMock_throwsException() {
        core.verify(null, VerificationModeFactory.times(1));
    }

    // Verify with non-mock object should throw NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerify_nonMockObject_throwsNotAMockException() {
        core.verify(new Object(), VerificationModeFactory.times(1));
    }

    // inOrder with null array throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testInOrder_nullArray_throwsRuntimeException() {
        core.inOrder((Object[]) null);
    }

    // inOrder with empty varargs throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testInOrder_emptyArray_throwsRuntimeException() {
        core.inOrder();
    }

    // inOrder with null element throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testInOrder_containsNull_throwsRuntimeException() {
        core.inOrder((Object) null);
    }

    // inOrder with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testInOrder_containsNonMock_throwsNotAMockException() {
        core.inOrder(new Object());
    }

    // verifyNoMoreInteractions with null vararg throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testVerifyNoMoreInteractions_nullArg_throwsRuntimeException() {
        core.verifyNoMoreInteractions((Object) null);
    }

    // verifyNoMoreInteractions with no arguments throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testVerifyNoMoreInteractions_emptyArgs_throwsRuntimeException() {
        core.verifyNoMoreInteractions();
    }

    // verifyNoMoreInteractions with non-mock throws RuntimeException (reporter.notAMockPassedToVerifyNoMoreInteractions)
    @Test(expected = RuntimeException.class)
    public void testVerifyNoMoreInteractions_nonMock_throwsRuntimeException() {
        core.verifyNoMoreInteractions(new Object());
    }

    // reset with null mock should throw NullPointerException (mockUtil.resetMock(null))
    @Test(expected = NullPointerException.class)
    public void testReset_nullMock_throwsNullPointerException() {
        core.reset((Object) null);
    }

    // when with null method call throws RuntimeException (reporter.missingMethodInvocation)
    @Test(expected = RuntimeException.class)
    public void testWhen_nullMethodCall_throwsRuntimeException() {
        core.when(null);
    }

    // stubVoid with null mock throws RuntimeException (getMockHandler(null))
    @Test(expected = RuntimeException.class)
    public void testStubVoid_nullMock_throwsRuntimeException() {
        core.stubVoid(null);
    }

    // stub() without ongoing stubbing throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testStub_noOngoingStubbing_throwsRuntimeException() {
        core.stub();
    }

    // getLastInvocation without ongoing stubbing throws exception (NPE or IndexOutOfBounds)
    @Test(expected = RuntimeException.class)
    public void testGetLastInvocation_noInvocation_throwsRuntimeException() {
        core.getLastInvocation();
    }

    // validateMockitoUsage when state is clean should not throw
    @Test
    public void testValidateMockitoUsage_noProblem() {
        core.validateMockitoUsage();
        assertTrue(true);
    }

    // ====== New tests to increase coverage ======

    // Verify with a valid mock and mode should pass
    @Test
    public void testVerify_withValidMock() {
        Object mock = mock(List.class);
        core.verify(mock, VerificationModeFactory.times(0));
        // No exception expected
    }

    // inOrder with valid mocks should return an InOrder object
    @Test
    public void testInOrder_withValidMocks() {
        Object mock1 = mock(List.class);
        Object mock2 = mock(List.class);
        assertNotNull(core.inOrder(mock1, mock2));
    }

    // verifyNoMoreInteractions with mock that has no interactions should pass
    @Test
    public void testVerifyNoMoreInteractions_withCleanMock() {
        Object mock = mock(List.class);
        core.verifyNoMoreInteractions(mock);
        // No exception
    }

    // reset with a valid mock should succeed
    @Test
    public void testReset_withValidMock() {
        Object mock = mock(List.class);
        core.reset(mock);
        // No exception
    }

    // when with a valid method call should return an OngoingStubbing
    @Test
    public void testWhen_withValidMethodCall() {
        List<String> mock = mock(List.class);
        OngoingStubbing<String> stub = core.when(mock.size());
        assertNotNull(stub);
    }

    // stubVoid with a valid mock should return a Stubber
    @Test
    public void testStubVoid_withValidMock() {
        List<String> mock = mock(List.class);
        Stubber stubber = core.stubVoid(mock);
        assertNotNull(stubber);
    }

    // stub() after an ongoing stubbing should return the ongoing stubbing
    @Test
    public void testStub_withOngoingStubbing() {
        List<String> mock = mock(List.class);
        core.when(mock.size());
        assertNotNull(core.stub());
    }

    // getLastInvocation after an invocation on mock should return the invocation
    @Test
    public void testGetLastInvocation_withInvocation() {
        List<String> mock = mock(List.class);
        mock.size(); // triggers invocation
        Invocation invocation = core.getLastInvocation();
        assertNotNull(invocation);
        assertTrue(invocation.getMethod().getName().equals("size"));
    }

    // validateMockitoUsage when state is dirty (e.g., after when() without stubbing) should throw RuntimeException
    @Test(expected = RuntimeException.class)
    public void testValidateMockitoUsage_whenStateDirty() {
        List<String> mock = mock(List.class);
        core.when(mock.size()); // leaves stubbing incomplete
        core.validateMockitoUsage(); // should detect problem and throw
    }
}
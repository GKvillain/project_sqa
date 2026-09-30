package org.mockito.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullGivenException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;

import java.util.List;

import static org.junit.Assert.*;

public class MockitoCoreTest {

    private MockitoCore mockitoCore;

    @Before
    public void setUp() {
        mockitoCore = new MockitoCore();
    }

    @After
    public void tearDown() {
        mockitoCore.validateMockitoUsage();
    }

    // Tests mock creation with default MockSettings
    @Test
    public void testMock_validClassAndSettings_returnsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        assertNotNull(mockList);
    }

    // Tests mock creation with boolean flag
    @Test
    public void testMock_withResetOngoingStubbingFlag_returnsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl(), true);
        assertNotNull(mockList);
    }

    // Tests when() without ongoing stubbing throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testWhen_withoutOngoingMethodCall_throwsException() {
        mockitoCore.when("dummy");
    }

    // Tests when() with ongoing method call returns ongoing stubbing
    @Test
    public void testWhen_withOngoingMethodCall_returnsOngoingStubbing() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.get(0);
        OngoingStubbing<?> ongoingStubbing = mockitoCore.when(null);
        assertNotNull(ongoingStubbing);
    }

    // Tests stub() without ongoing stubbing throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStub_withoutOngoingMethodCall_throwsException() {
        mockitoCore.stub("dummy");
    }

    // Tests stub(methodCall) with ongoing method call returns ongoing stubbing
    @Test
    public void testStub_withOngoingMethodCall_returnsOngoingStubbing() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.get(0);
        OngoingStubbing<?> ongoingStubbing = mockitoCore.stub(null);
        assertNotNull(ongoingStubbing);
    }

    // Tests stub() pull with no invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStub_noOngoingStubbing_throwsException() {
        mockitoCore.stub();
    }

    // Tests stub() pull with invocation returns ongoing stubbing
    @Test
    public void testStub_pullWithOngoingStubbing_returnsOngoingStubbing() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.get(0);
        OngoingStubbing<?> ongoingStubbing = mockitoCore.stub();
        assertNotNull(ongoingStubbing);
    }

    // Tests verify with null mock throws NullGivenException
    @Test(expected = NullGivenException.class)
    public void testVerify_nullMock_throwsException() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    // Tests verify with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerify_notAMock_throwsException() {
        mockitoCore.verify("notAMock", VerificationModeFactory.times(1));
    }

    // Tests verify on valid mock returns mock
    @Test
    public void testVerify_validMock_returnsMock() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> returnedMock = mockitoCore.verify(mockList, VerificationModeFactory.times(1));
        assertSame(mockList, returnedMock);
    }

    // Tests reset on valid mock
    @Test
    public void testReset_validMock_resetsSuccessfully() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.reset(mockList);
    }

    // Tests reset with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testReset_nullArray_throwsException() {
        mockitoCore.reset((Object[]) null);
    }

    // Tests reset with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testReset_emptyArray_throwsException() {
        mockitoCore.reset(new Object[0]);
    }

    // Tests reset with null element throws NullGivenException
    @Test(expected = NullGivenException.class)
    public void testReset_nullElement_throwsException() {
        mockitoCore.reset(new Object[] { null });
    }

    // Tests reset with non-mock throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testReset_notAMock_throwsException() {
        mockitoCore.reset("notAMock");
    }

    // Tests verifyNoMoreInteractions with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_nullArray_throwsException() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    // Tests verifyNoMoreInteractions with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_emptyArray_throwsException() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    // Tests verifyNoMoreInteractions with null element throws NullGivenException
    @Test(expected = NullGivenException.class)
    public void testVerifyNoMoreInteractions_nullElement_throwsException() {
        mockitoCore.verifyNoMoreInteractions(new Object[] { null });
    }

    // Tests verifyNoMoreInteractions with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerifyNoMoreInteractions_notAMock_throwsException() {
        mockitoCore.verifyNoMoreInteractions("notAMock");
    }

    // Tests verifyNoMoreInteractions with valid mock passes
    @Test
    public void testVerifyNoMoreInteractions_validMock_passes() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests inOrder with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInOrder_nullArray_throwsException() {
        mockitoCore.inOrder((Object[]) null);
    }

    // Tests inOrder with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInOrder_emptyArray_throwsException() {
        mockitoCore.inOrder(new Object[0]);
    }

    // Tests inOrder with null element throws NullGivenException
    @Test(expected = NullGivenException.class)
    public void testInOrder_nullElement_throwsException() {
        mockitoCore.inOrder(new Object[] { null });
    }

    // Tests inOrder with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testInOrder_notAMock_throwsException() {
        mockitoCore.inOrder("notAMock");
    }

    // Tests inOrder with valid mock returns InOrder instance
    @Test
    public void testInOrder_validMock_returnsInOrderInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mockList);
        assertNotNull(inOrder);
    }

    // Tests doAnswer returns Stubber instance
    @Test
    public void testDoAnswer_validAnswer_returnsStubber() {
        Answer<Object> dummyAnswer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        };
        Stubber stubber = mockitoCore.doAnswer(dummyAnswer);
        assertNotNull(stubber);
    }

    // Tests stubVoid on valid mock returns VoidMethodStubbable
    @Test
    public void testStubVoid_validMock_returnsVoidMethodStubbable() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        VoidMethodStubbable<?> stubbable = mockitoCore.stubVoid(mockList);
        assertNotNull(stubbable);
    }

    // Tests stubVoid on non-mock throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testStubVoid_notAMock_throwsException() {
        mockitoCore.stubVoid("notAMock");
    }

    // Tests stubVoid on null throws NullGivenException
    @Test(expected = NullGivenException.class)
    public void testStubVoid_nullMock_throwsException() {
        mockitoCore.stubVoid(null);
    }

    // Tests validateMockitoUsage when state is valid
    @Test
    public void testValidateMockitoUsage_cleanState_doesNotThrow() {
        mockitoCore.validateMockitoUsage();
    }
}
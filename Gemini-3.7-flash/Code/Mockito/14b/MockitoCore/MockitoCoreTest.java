package org.mockito.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;

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

    // Tests normal mock creation
    @Test
    public void testMock_validClass_createsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        assertNotNull(mockList);
    }

    // Tests stub with missing method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStub_missingMethodInvocation_throwsException() {
        mockitoCore.stub();
    }

    // Tests when with missing method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testWhen_withoutMethodCall_throwsException() {
        mockitoCore.when("dummy");
    }

    // Tests stub(methodCall) with missing method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStubMethodCall_withoutMethodCall_throwsException() {
        mockitoCore.stub("dummy");
    }

    // Tests normal when and stubbing flow
    @Test
    public void testWhen_validMockInvocation_stubsMethod() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        OngoingStubbing<Object> stubbing = mockitoCore.when(mockList.get(0));
        assertNotNull(stubbing);
        stubbing.thenReturn("element");
        assertEquals("element", mockList.get(0));
    }

    // Tests verify with null mock throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testVerify_nullMock_throwsException() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    // Tests verify with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerify_nonMockObject_throwsException() {
        mockitoCore.verify("not a mock", VerificationModeFactory.times(1));
    }

    // Tests normal verify returns the mock
    @Test
    public void testVerify_validMock_returnsMock() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        List returnedMock = mockitoCore.verify(mockList, VerificationModeFactory.times(1));
        assertSame(mockList, returnedMock);
        mockList.clear();
    }

    // Tests reset on mock object
    @Test
    public void testReset_validMock_resetsMockState() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.when(mockList.get(0)).thenReturn("element");
        assertEquals("element", mockList.get(0));

        mockitoCore.reset(mockList);
        assertNull(mockList.get(0));
    }

    // Tests reset with null mock throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testReset_nullMock_throwsException() {
        mockitoCore.reset((Object) null);
    }

    // Tests verifyNoMoreInteractions with null array throws exception
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_nullArray_throwsException() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    // Tests verifyNoMoreInteractions with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_emptyArray_throwsException() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    // Tests verifyNoMoreInteractions with null element throws exception
    @Test(expected = NullInsteadOfMockException.class)
    public void testVerifyNoMoreInteractions_nullElement_throwsException() {
        mockitoCore.verifyNoMoreInteractions(new Object[] { null });
    }

    // Tests verifyNoMoreInteractions with non-mock element throws exception
    @Test(expected = NotAMockException.class)
    public void testVerifyNoMoreInteractions_nonMockElement_throwsException() {
        mockitoCore.verifyNoMoreInteractions("not a mock");
    }

    // Tests verifyNoMoreInteractions on unused mock passes
    @Test
    public void testVerifyNoMoreInteractions_unusedMock_passes() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests verifyNoMoreInteractions with unverified interactions throws NoInteractionsWanted
    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyNoMoreInteractions_unverifiedInteractions_throwsException() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.add("item");
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests inOrder with null array throws exception
    @Test(expected = MockitoException.class)
    public void testInOrder_nullArray_throwsException() {
        mockitoCore.inOrder((Object[]) null);
    }

    // Tests inOrder with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testInOrder_emptyArray_throwsException() {
        mockitoCore.inOrder(new Object[0]);
    }

    // Tests inOrder with null mock throws exception
    @Test(expected = NullInsteadOfMockException.class)
    public void testInOrder_nullMock_throwsException() {
        mockitoCore.inOrder(new Object[] { null });
    }

    // Tests inOrder with non-mock throws exception
    @Test(expected = NotAMockException.class)
    public void testInOrder_nonMock_throwsException() {
        mockitoCore.inOrder("not a mock");
    }

    // Tests inOrder with valid mock creates InOrder instance
    @Test
    public void testInOrder_validMock_returnsInOrderInstance() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mockList);
        assertNotNull(inOrder);
    }

    // Tests doAnswer returns Stubber and executes correctly
    @Test
    public void testDoAnswer_validAnswer_stubsMethod() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        Stubber stubber = mockitoCore.doAnswer(new Answer<Object>() {
            public Object answer(org.mockito.invocation.InvocationOnMock invocation) {
                return "custom";
            }
        });
        assertNotNull(stubber);
        stubber.when(mockList).get(0);
        assertEquals("custom", mockList.get(0));
    }

    // Tests stubVoid on mock
    @Test
    public void testStubVoid_validMock_returnsVoidMethodStubbable() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        assertNotNull(mockitoCore.stubVoid(mockList));
    }

    // Tests validateMockitoUsage succeeds in valid state
    @Test
    public void testValidateMockitoUsage_validState_noException() {
        mockitoCore.validateMockitoUsage();
    }

    // Tests verifyNoMoreInteractionsInOrder with empty list
    @Test
    public void testVerifyNoMoreInteractionsInOrder_emptyList_passes() {
        InOrder inOrder = mockitoCore.inOrder(mockitoCore.mock(List.class, new MockSettingsImpl()));
        mockitoCore.verifyNoMoreInteractionsInOrder(new ArrayList<Object>(), (InOrderImpl) inOrder);
    }

    // Tests verifyNoMoreInteractionsInOrder with verified interactions passes
    @Test
    public void testVerifyNoMoreInteractionsInOrder_verifiedInteractions_passes() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mockList);
        mockList.add("item");
        inOrder.verify(mockList).add("item");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(mockList);
        mockitoCore.verifyNoMoreInteractionsInOrder(mocks, (InOrderImpl) inOrder);
    }

    // Tests verifyNoMoreInteractionsInOrder with unverified interactions throws VerificationInOrderFailure
    @Test(expected = VerificationInOrderFailure.class)
    public void testVerifyNoMoreInteractionsInOrder_unverifiedInteractions_throwsException() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mockList);
        mockList.add("item");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(mockList);
        mockitoCore.verifyNoMoreInteractionsInOrder(mocks, (InOrderImpl) inOrder);
    }

    // Tests isTypeMockable for mockable interface
    @Test
    public void testIsTypeMockable_interface_returnsTrue() {
        assertTrue(mockitoCore.isTypeMockable(List.class));
    }

    // Tests isTypeMockable for final class
    @Test
    public void testIsTypeMockable_finalClass_returnsFalse() {
        assertFalse(mockitoCore.isTypeMockable(String.class));
    }
}
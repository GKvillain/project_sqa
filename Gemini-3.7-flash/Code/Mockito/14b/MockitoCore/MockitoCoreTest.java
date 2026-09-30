package org.mockito.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    // Tests creating a mock with valid class and settings
    @Test
    public void testMock_validClassAndSettings_returnsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        assertNotNull(mockList);
    }

    // Tests stubbing method call with when
    @Test
    public void testWhen_validMockInvocation_returnsOngoingStubbing() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        OngoingStubbing<Object> stubbing = mockitoCore.when(mockList.get(0));
        assertNotNull(stubbing);
        stubbing.thenReturn("expectedValue");
        assertEquals("expectedValue", mockList.get(0));
    }

    // Tests stubbing without preceding method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStub_noMethodInvocation_throwsMissingMethodInvocationException() {
        mockitoCore.stub();
    }

    // Tests verify on a mock object
    @Test
    public void testVerify_validMock_verifiesInteraction() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.clear();
        List verifiedMock = mockitoCore.verify(mockList, VerificationModeFactory.times(1));
        assertNotNull(verifiedMock);
        verifiedMock.clear();
    }

    // Tests verify with null mock throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testVerify_nullMock_throwsNullInsteadOfMockException() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    // Tests verify with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerify_nonMockObject_throwsNotAMockException() {
        mockitoCore.verify("notAMock", VerificationModeFactory.times(1));
    }

    // Tests reset on a valid mock
    @Test
    public void testReset_validMock_resetsState() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.when(mockList.get(0)).thenReturn("value");
        assertEquals("value", mockList.get(0));
        mockitoCore.reset(mockList);
        assertEquals(null, mockList.get(0));
    }

    // Tests verifyNoMoreInteractions with valid mock
    @Test
    public void testVerifyNoMoreInteractions_noInteractions_succeeds() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests verifyNoMoreInteractions with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_nullArray_throwsMockitoException() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    // Tests verifyNoMoreInteractions with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_emptyArray_throwsMockitoException() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    // Tests verifyNoMoreInteractions with null element throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testVerifyNoMoreInteractions_nullElement_throwsNullInsteadOfMockException() {
        mockitoCore.verifyNoMoreInteractions(new Object[]{null});
    }

    // Tests verifyNoMoreInteractions with non-mock element throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerifyNoMoreInteractions_nonMockElement_throwsNotAMockException() {
        mockitoCore.verifyNoMoreInteractions("notAMock");
    }

    // Tests inOrder creation with valid mock
    @Test
    public void testInOrder_validMock_returnsInOrderInstance() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        InOrder inOrder = mockitoCore.inOrder(mockList);
        assertNotNull(inOrder);
    }

    // Tests inOrder with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInOrder_nullArray_throwsMockitoException() {
        mockitoCore.inOrder((Object[]) null);
    }

    // Tests inOrder with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInOrder_emptyArray_throwsMockitoException() {
        mockitoCore.inOrder(new Object[0]);
    }

    // Tests inOrder with null element throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testInOrder_nullElement_throwsNullInsteadOfMockException() {
        mockitoCore.inOrder(new Object[]{null});
    }

    // Tests inOrder with non-mock element throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testInOrder_nonMockElement_throwsNotAMockException() {
        mockitoCore.inOrder("notAMock");
    }

    // Tests doAnswer stubbing behavior
    @Test
    public void testDoAnswer_validAnswer_stubsMockCorrectly() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        Stubber stubber = mockitoCore.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "stubbedAnswer";
            }
        });
        assertNotNull(stubber);
        stubber.when(mockList).get(0);
        assertEquals("stubbedAnswer", mockList.get(0));
    }

    // Tests validateMockitoUsage when state is valid
    @Test
    public void testValidateMockitoUsage_validState_noExceptionThrown() {
        mockitoCore.validateMockitoUsage();
    }

    // Tests getLastInvocation when no interaction has occurred
    @Test
    public void testGetLastInvocation_noInvocations_returnsNull() {
        Invocation lastInvocation = mockitoCore.getLastInvocation();
        assertNull(lastInvocation);
    }

    // Tests getLastInvocation after a mock invocation
    @Test
    public void testGetLastInvocation_afterInvocation_returnsInvocation() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.get(0);
        Invocation lastInvocation = mockitoCore.getLastInvocation();
        assertNotNull(lastInvocation);
        assertEquals("get", lastInvocation.getMethod().getName());
    }

    // Tests reset with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testReset_nullArray_throwsMockitoException() {
        mockitoCore.reset((Object[]) null);
    }

    // Tests reset with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testReset_emptyArray_throwsMockitoException() {
        mockitoCore.reset(new Object[0]);
    }

    // Tests reset with null element throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testReset_nullElement_throwsNullInsteadOfMockException() {
        mockitoCore.reset(new Object[]{null});
    }

    // Tests reset with non-mock element throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testReset_nonMockElement_throwsNotAMockException() {
        mockitoCore.reset("notAMock");
    }

    // Tests stubVoid with valid mock
    @Test
    public void testStubVoid_validMock_returnsStubber() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.stubVoid(mockList).toThrow(new RuntimeException()).on().clear();
        try {
            mockList.clear();
        } catch (RuntimeException expected) {
            assertNotNull(expected);
        }
    }

    // Tests stubVoid with null mock throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testStubVoid_nullMock_throwsNullInsteadOfMockException() {
        mockitoCore.stubVoid(null);
    }

    // Tests stubVoid with non-mock throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testStubVoid_nonMock_throwsNotAMockException() {
        mockitoCore.stubVoid("notAMock");
    }

    // Tests verifyNoMoreInteractions throws NoInteractionsWanted when unverified interactions exist
    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyNoMoreInteractions_unverifiedInteraction_throwsNoInteractionsWanted() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.add("unverified");
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests ignoreStubs ignores stubbed calls during verification
    @Test
    public void testIgnoreStubs_stubbedCallsIgnored() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.when(mockList.get(0)).thenReturn("val");
        mockList.get(0);

        Object[] ignored = mockitoCore.ignoreStubs(mockList);
        assertEquals(1, ignored.length);
        mockitoCore.verifyNoMoreInteractions(ignored);
    }

    // Tests ignoreStubs with null array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testIgnoreStubs_nullArray_throwsMockitoException() {
        mockitoCore.ignoreStubs((Object[]) null);
    }

    // Tests ignoreStubs with empty array throws MockitoException
    @Test(expected = MockitoException.class)
    public void testIgnoreStubs_emptyArray_throwsMockitoException() {
        mockitoCore.ignoreStubs(new Object[0]);
    }

    // Tests ignoreStubs with null element throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testIgnoreStubs_nullElement_throwsNullInsteadOfMockException() {
        mockitoCore.ignoreStubs(new Object[]{null});
    }

    // Tests ignoreStubs with non-mock element throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testIgnoreStubs_nonMockElement_throwsNotAMockException() {
        mockitoCore.ignoreStubs("notAMock");
    }

    // Tests stub() after method invocation returns OngoingStubbing
    @Test
    public void testStub_afterMethodInvocation_returnsOngoingStubbing() {
        List mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.get(0);
        OngoingStubbing<Object> stubbing = mockitoCore.stub();
        assertNotNull(stubbing);
        stubbing.thenReturn("stubbedValue");
        assertEquals("stubbedValue", mockList.get(0));
    }
}
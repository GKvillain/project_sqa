package org.mockito.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.util.List;

import static org.junit.Assert.*;

public class MockitoCoreTest {

    private MockitoCore mockitoCore;

    @Before
    public void setUp() {
        mockitoCore = new MockitoCore();
        new ThreadSafeMockingProgress().reset();
    }

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().reset();
    }

    // Tests creating mock with valid class and settings
    @Test
    public void testMock_validClassAndSettings_returnsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        assertNotNull(mockList);
    }

    // Tests creating mock with shouldResetOngoingStubbing boolean parameter
    @Test
    public void testMock_withShouldResetOngoingStubbing_returnsMockInstance() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl(), true);
        assertNotNull(mockList);
    }

    // Tests stubbing when without method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testWhen_withoutMethodInvocation_throwsMissingMethodInvocationException() {
        mockitoCore.when("invalidCall");
    }

    // Tests stub when without method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStub_withoutMethodInvocation_throwsMissingMethodInvocationException() {
        mockitoCore.stub();
    }

    // Tests deprecated stub method without method invocation throws exception
    @Test(expected = MissingMethodInvocationException.class)
    public void testStubWithMethodCall_withoutMethodInvocation_throwsMissingMethodInvocationException() {
        mockitoCore.stub("invalidCall");
    }

    // Tests verify with null mock throws exception
    @Test(expected = MockitoException.class)
    public void testVerify_nullMock_throwsException() {
        mockitoCore.verify(null, VerificationModeFactory.times(1));
    }

    // Tests verify with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerify_notAMock_throwsNotAMockException() {
        mockitoCore.verify("notAMock", VerificationModeFactory.times(1));
    }

    // Tests verify with valid mock returns the mock instance
    @Test
    public void testVerify_validMock_returnsSameMock() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        List<?> verified = mockitoCore.verify(mockList, VerificationModeFactory.times(1));
        assertSame(mockList, verified);
    }

    // Tests reset with valid mock
    @Test
    public void testReset_validMock_clearsMockState() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.clear();
        mockitoCore.reset(mockList);
        assertNotNull(mockList);
    }

    // Tests verifyNoMoreInteractions with null array throws exception
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_nullArray_throwsMockitoException() {
        mockitoCore.verifyNoMoreInteractions((Object[]) null);
    }

    // Tests verifyNoMoreInteractions with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_emptyArray_throwsMockitoException() {
        mockitoCore.verifyNoMoreInteractions(new Object[0]);
    }

    // Tests verifyNoMoreInteractions with null element throws exception
    @Test(expected = MockitoException.class)
    public void testVerifyNoMoreInteractions_nullElement_throwsMockitoException() {
        mockitoCore.verifyNoMoreInteractions(new Object[]{null});
    }

    // Tests verifyNoMoreInteractions with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testVerifyNoMoreInteractions_notAMock_throwsNotAMockException() {
        mockitoCore.verifyNoMoreInteractions("notAMock");
    }

    // Tests verifyNoMoreInteractions with valid mock passes
    @Test
    public void testVerifyNoMoreInteractions_validMockWithoutInteractions_succeeds() {
        List<?> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.verifyNoMoreInteractions(mockList);
    }

    // Tests inOrder with null array throws exception
    @Test(expected = MockitoException.class)
    public void testInOrder_nullArray_throwsMockitoException() {
        mockitoCore.inOrder((Object[]) null);
    }

    // Tests inOrder with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testInOrder_emptyArray_throwsMockitoException() {
        mockitoCore.inOrder(new Object[0]);
    }

    // Tests inOrder with null element throws exception
    @Test(expected = MockitoException.class)
    public void testInOrder_nullElement_throwsMockitoException() {
        mockitoCore.inOrder(new Object[]{null});
    }

    // Tests inOrder with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testInOrder_notAMock_throwsNotAMockException() {
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
        Answer<?> answer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        };
        Stubber stubber = mockitoCore.doAnswer(answer);
        assertNotNull(stubber);
    }

    // Tests validateMockitoUsage when state is valid
    @Test
    public void testValidateMockitoUsage_validState_succeeds() {
        mockitoCore.validateMockitoUsage();
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

    // Tests reset with null element throws MockitoException
    @Test(expected = MockitoException.class)
    public void testReset_nullElement_throwsMockitoException() {
        mockitoCore.reset(new Object[]{null});
    }

    // Tests reset with non-mock throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testReset_notAMock_throwsNotAMockException() {
        mockitoCore.reset("notAMock");
    }

    // Tests when with successful stubbing and execution
    @SuppressWarnings("unchecked")
    @Test
    public void testWhen_validMockInvocation_stubsMethodSuccessfully() {
        List<String> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.when(mockList.get(0)).thenReturn("firstElement");
        assertEquals("firstElement", mockList.get(0));
    }

    // Tests stub with successful stubbing and execution
    @SuppressWarnings("unchecked")
    @Test
    public void testStub_validMockInvocation_stubsMethodSuccessfully() {
        List<String> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockitoCore.stub(mockList.get(0)).toReturn("firstElement");
        assertEquals("firstElement", mockList.get(0));
    }

    // Tests doAnswer chaining to mock invocation
    @SuppressWarnings("unchecked")
    @Test
    public void testDoAnswer_appliedToMock_executesAnswer() {
        List<String> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        Answer<String> answer = new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return "customAnswer";
            }
        };
        mockitoCore.doAnswer(answer).when(mockList).get(0);
        assertEquals("customAnswer", mockList.get(0));
    }

    // Tests inOrder verification succeeds for calls in order
    @SuppressWarnings("unchecked")
    @Test
    public void testInOrder_verifiesInvocationOrder() {
        List<String> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.add("one");
        mockList.add("two");

        InOrder inOrder = mockitoCore.inOrder(mockList);
        inOrder.verify(mockList).add("one");
        inOrder.verify(mockList).add("two");
    }

    // Tests verifyNoMoreInteractions throws exception when there are unverified interactions
    @SuppressWarnings("unchecked")
    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyNoMoreInteractions_unverifiedInteractions_throwsNoInteractionsWanted() {
        List<String> mockList = mockitoCore.mock(List.class, new MockSettingsImpl());
        mockList.add("unverified");
        mockitoCore.verifyNoMoreInteractions(mockList);
    }
}
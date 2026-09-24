package org.mockito;

import org.junit.After;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.verification.api.VerificationMode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MockitoTest {

    @After
    public void tearDown() {
        Mockito.validateMockitoUsage();
    }

    // Tests mock creation with default answer
    @Test
    public void testMock_classOnly_createsMockObject() {
        List<?> list = Mockito.mock(List.class);
        assertNotNull(list);
        assertNull(list.get(0));
    }

    // Tests mock creation with name
    @Test
    public void testMock_withName_createsMockObject() {
        List<?> list = Mockito.mock(List.class, "myMock");
        assertNotNull(list);
        assertEquals("myMock", list.toString());
    }

    // Tests mock creation with custom Answer
    @Test
    public void testMock_withAnswer_usesGivenAnswer() {
        Answer<String> dummyAnswer = new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        List<?> list = Mockito.mock(List.class, dummyAnswer);
        assertEquals("custom", list.get(0));
    }

    // Tests mock creation with custom MockSettings
    @Test
    public void testMock_withSettings_createsConfiguredMock() {
        MockSettings settings = Mockito.withSettings().name("customNamed");
        List<?> list = Mockito.mock(List.class, settings);
        assertNotNull(list);
        assertEquals("customNamed", list.toString());
    }

    // Tests spy creation and calling real methods
    @Test
    public void testSpy_realObject_callsRealMethods() {
        List<String> realList = new ArrayList<String>();
        List<String> spyList = Mockito.spy(realList);

        spyList.add("test");
        assertEquals(1, spyList.size());
        assertEquals("test", spyList.get(0));
        Mockito.verify(spyList).add("test");
    }

    // Tests stubbing and verification of mock interactions
    @Test
    public void testWhen_thenReturnsStubbedValue() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.get(0)).thenReturn("first");

        assertEquals("first", list.get(0));
        assertNull(list.get(1));
        Mockito.verify(list).get(0);
    }

    // Tests deprecated stub() method
    @Test
    public void testStub_deprecatedStubbing_returnsStubbedValue() {
        List<String> list = Mockito.mock(List.class);
        Mockito.stub(list.get(0)).toReturn("stubbed");

        assertEquals("stubbed", list.get(0));
    }

    // Tests verification modes: times, never, atLeast, atLeastOnce, atMost, only
    @Test
    public void testVerify_variousVerificationModes_verifiesCorrectly() {
        List<String> list = Mockito.mock(List.class);

        list.add("one");
        list.add("two");
        list.add("two");

        Mockito.verify(list, Mockito.times(1)).add("one");
        Mockito.verify(list, Mockito.times(2)).add("two");
        Mockito.verify(list, Mockito.never()).add("three");
        Mockito.verify(list, Mockito.atLeastOnce()).add("one");
        Mockito.verify(list, Mockito.atLeast(1)).add("two");
        Mockito.verify(list, Mockito.atMost(2)).add("two");
    }

    // Tests verification mode only()
    @Test
    public void testVerify_onlyMode_verifiesSingleInvocation() {
        List<String> list = Mockito.mock(List.class);
        list.add("unique");

        Mockito.verify(list, Mockito.only()).add("unique");
    }

    // Tests InOrder verification succeeds when calls are in order
    @Test
    public void testInOrder_correctOrder_verificationPasses() {
        List<String> first = Mockito.mock(List.class);
        List<String> second = Mockito.mock(List.class);

        first.add("1");
        second.add("2");

        InOrder inOrder = Mockito.inOrder(first, second);
        inOrder.verify(first).add("1");
        inOrder.verify(second).add("2");
    }

    // Tests InOrder verification fails when calls are out of order
    @Test(expected = VerificationInOrderFailure.class)
    public void testInOrder_wrongOrder_throwsException() {
        List<String> first = Mockito.mock(List.class);
        List<String> second = Mockito.mock(List.class);

        first.add("1");
        second.add("2");

        InOrder inOrder = Mockito.inOrder(first, second);
        inOrder.verify(second).add("2");
        inOrder.verify(first).add("1");
    }

    // Tests verifyZeroInteractions when no interactions happened
    @Test
    public void testVerifyZeroInteractions_noInteractions_succeeds() {
        List<?> list1 = Mockito.mock(List.class);
        List<?> list2 = Mockito.mock(List.class);

        Mockito.verifyZeroInteractions(list1, list2);
    }

    // Tests verifyNoMoreInteractions throws exception when unverified interactions exist
    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyNoMoreInteractions_unverifiedInteraction_throwsException() {
        List<String> list = Mockito.mock(List.class);
        list.add("unexpected");

        Mockito.verifyNoMoreInteractions(list);
    }

    // Tests reset clears interactions and stubbings
    @Test
    public void testReset_clearsInteractionsAndStubbings() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.get(0)).thenReturn("value");
        list.add("call");

        assertEquals("value", list.get(0));

        Mockito.reset(list);

        assertNull(list.get(0));
        Mockito.verifyZeroInteractions(list);
    }

    // Tests doReturn stubbing
    @Test
    public void testDoReturn_stubbing_returnsValue() {
        List<String> list = Mockito.mock(List.class);
        Mockito.doReturn("returned").when(list).get(0);

        assertEquals("returned", list.get(0));
    }

    // Tests doThrow stubbing with exception
    @Test(expected = IllegalArgumentException.class)
    public void testDoThrow_stubbing_throwsExpectedException() {
        List<String> list = Mockito.mock(List.class);
        Mockito.doThrow(new IllegalArgumentException()).when(list).clear();

        list.clear();
    }

    // Tests doAnswer stubbing
    @Test
    public void testDoAnswer_stubbing_executesAnswer() {
        List<String> list = Mockito.mock(List.class);
        Mockito.doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return "answered";
            }
        }).when(list).get(1);

        assertEquals("answered", list.get(1));
    }

    // Tests doNothing stubbing on spy
    @Test
    public void testDoNothing_onSpy_suppressesRealMethod() {
        List<String> spy = Mockito.spy(new ArrayList<String>());
        Mockito.doNothing().when(spy).clear();

        spy.add("item");
        spy.clear();

        assertEquals(1, spy.size());
    }

    // Tests doCallRealMethod on mock
    @Test
    public void testDoCallRealMethod_onMock_callsRealImplementation() {
        ArrayList<String> mockList = Mockito.mock(ArrayList.class);
        Mockito.doCallRealMethod().when(mockList).size();

        assertEquals(0, mockList.size());
    }

    // Tests debug() returns non-null debugger instance
    @Test
    public void testDebug_returnsMockitoDebugger() {
        MockitoDebugger debugger = Mockito.debug();
        assertNotNull(debugger);
    }
}
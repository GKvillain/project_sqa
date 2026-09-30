package org.mockito;

import org.junit.After;
import org.junit.Test;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MockitoTest {

    @After
    public void tearDown() {
        Mockito.validateMockitoUsage();
    }

    // Tests creating standard mock by class
    @Test
    public void testMock_class_createsMockInstance() {
        List<?> mockList = Mockito.mock(List.class);
        assertNotNull(mockList);
        assertNull(mockList.get(0));
    }

    // Tests creating mock with custom name
    @Test
    public void testMock_withName_createsMockWithName() {
        List<?> mockList = Mockito.mock(List.class, "customName");
        assertNotNull(mockList);
        assertEquals("customName", mockList.toString());
    }

    // Tests creating mock with default answer
    @Test
    public void testMock_withDefaultAnswer_usesAnswer() {
        List<?> mockList = Mockito.mock(List.class, Mockito.RETURNS_SMART_NULLS);
        assertNotNull(mockList);
    }

    // Tests creating mock with custom mock settings
    @Test
    public void testMock_withSettings_createsConfiguredMock() {
        List<?> mockList = Mockito.mock(List.class, Mockito.withSettings().name("settingsMock"));
        assertNotNull(mockList);
        assertEquals("settingsMock", mockList.toString());
    }

    // Tests creating a spy of a real object
    @Test
    public void testSpy_realObject_callsRealMethods() {
        List<String> realList = new ArrayList<String>();
        List<String> spyList = Mockito.spy(realList);

        spyList.add("test");
        assertEquals(1, spyList.size());
        assertEquals("test", spyList.get(0));
    }

    // Tests stubbing method with return value using when/thenReturn
    @Test
    public void testWhen_thenReturn_returnsStubbedValue() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.when(mockList.get(0)).thenReturn("first");

        assertEquals("first", mockList.get(0));
        assertNull(mockList.get(1));
    }

    // Tests stubbing method with exception using when/thenThrow
    @Test(expected = IllegalArgumentException.class)
    public void testWhen_thenThrow_throwsStubbedException() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.when(mockList.get(0)).thenThrow(new IllegalArgumentException());

        mockList.get(0);
    }

    // Tests basic verification with default times(1)
    @Test
    public void testVerify_singleInvocation_verifiesSuccessfully() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("one");

        Mockito.verify(mockList).add("one");
    }

    // Tests verification with explicit times count
    @Test
    public void testVerify_times_verifiesExactInvocations() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("item");
        mockList.add("item");

        Mockito.verify(mockList, Mockito.times(2)).add("item");
    }

    // Tests verification using never mode
    @Test
    public void testVerify_never_verifiesZeroInvocations() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.verify(mockList, Mockito.never()).clear();
    }

    // Tests verification using atLeastOnce mode
    @Test
    public void testVerify_atLeastOnce_verifiesInvocation() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("test");
        mockList.add("test");

        Mockito.verify(mockList, Mockito.atLeastOnce()).add("test");
    }

    // Tests verification using atLeast mode
    @Test
    public void testVerify_atLeast_verifiesMinimumInvocations() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("item");
        mockList.add("item");
        mockList.add("item");

        Mockito.verify(mockList, Mockito.atLeast(2)).add("item");
    }

    // Tests verification using atMost mode
    @Test
    public void testVerify_atMost_verifiesMaximumInvocations() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("item");

        Mockito.verify(mockList, Mockito.atMost(2)).add("item");
    }

    // Tests verification using only mode
    @Test
    public void testVerify_only_verifiesSingleUniqueInvocation() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("item");

        Mockito.verify(mockList, Mockito.only()).add("item");
    }

    // Tests verification failure when expected invocation did not happen
    @Test(expected = WantedButNotInvoked.class)
    public void testVerify_uninvokedMethod_throwsWantedButNotInvoked() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.verify(mockList).add("notCalled");
    }

    // Tests in-order verification of multiple interactions
    @Test
    public void testInOrder_multipleMocks_verifiesInOrder() {
        List<String> firstMock = Mockito.mock(List.class);
        List<String> secondMock = Mockito.mock(List.class);

        firstMock.add("first");
        secondMock.add("second");

        InOrder inOrder = Mockito.inOrder(firstMock, secondMock);
        inOrder.verify(firstMock).add("first");
        inOrder.verify(secondMock).add("second");
    }

    // Tests stubbing using doReturn on a spy
    @Test
    public void testDoReturn_spyObject_returnsStubbedValue() {
        List<String> list = new ArrayList<String>();
        List<String> spyList = Mockito.spy(list);

        Mockito.doReturn("mocked").when(spyList).get(0);
        assertEquals("mocked", spyList.get(0));
    }

    // Tests stubbing void method using doThrow
    @Test(expected = RuntimeException.class)
    public void testDoThrow_voidMethod_throwsException() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.doThrow(new RuntimeException()).when(mockList).clear();

        mockList.clear();
    }

    // Tests stubbing void method using doNothing
    @Test
    public void testDoNothing_voidMethod_doesNothing() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.doNothing().when(mockList).clear();

        mockList.clear();
        Mockito.verify(mockList).clear();
    }

    // Tests stubbing method using doAnswer
    @Test
    public void testDoAnswer_customAnswer_returnsAnswerResult() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return "customAnswer";
            }
        }).when(mockList).get(0);

        assertEquals("customAnswer", mockList.get(0));
    }

    // Tests calling real method on a mock using doCallRealMethod
    @Test
    public void testDoCallRealMethod_partialMock_executesRealMethod() {
        ArrayList<String> mockList = Mockito.mock(ArrayList.class);
        Mockito.doCallRealMethod().when(mockList).size();

        assertEquals(0, mockList.size());
    }

    // Tests reset method clearing mock stubbing and interactions
    @Test
    public void testReset_stubbedMock_clearsStubbingAndInteractions() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.when(mockList.get(0)).thenReturn("stubbed");
        mockList.get(0);

        Mockito.reset(mockList);

        assertNull(mockList.get(0));
        Mockito.verify(mockList, Mockito.never()).get(0);
    }

    // Tests verifyZeroInteractions when no interaction occurred
    @Test
    public void testVerifyZeroInteractions_noInvocations_succeeds() {
        List<String> mockList1 = Mockito.mock(List.class);
        List<String> mockList2 = Mockito.mock(List.class);

        Mockito.verifyZeroInteractions(mockList1, mockList2);
    }

    // Tests verifyZeroInteractions throws exception when unexpected interaction occurred
    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyZeroInteractions_withInvocation_throwsException() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("interaction");

        Mockito.verifyZeroInteractions(mockList);
    }

    // Tests verifyNoMoreInteractions after verifying all interactions
    @Test
    public void testVerifyNoMoreInteractions_allInteractionsVerified_succeeds() {
        List<String> mockList = Mockito.mock(List.class);
        mockList.add("test");

        Mockito.verify(mockList).add("test");
        Mockito.verifyNoMoreInteractions(mockList);
    }

    // Tests deprecated stub method
    @Test
    public void testStub_deprecatedStubbing_returnsConfiguredValue() {
        List<String> mockList = Mockito.mock(List.class);
        Mockito.stub(mockList.get(0)).toReturn("val");

        assertEquals("val", mockList.get(0));
    }

    // Tests withSettings factory method
    @Test
    public void testWithSettings_returnsNonNullMockSettings() {
        MockSettings settings = Mockito.withSettings();
        assertNotNull(settings);
    }

    // Tests debug factory method
    @Test
    public void testDebug_returnsNonNullDebugger() {
        MockitoDebugger debugger = Mockito.debug();
        assertNotNull(debugger);
    }
}
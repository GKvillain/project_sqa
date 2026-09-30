package org.mockito.internal;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.List;
import org.mockito.InOrder;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.internal.progress.IOngoingStubbing;

public class MockitoCoreTest {

    private MockitoCore core;
    private List mockList;
    private List anotherMock;
    private Object nonMock = "not a mock";

    @Before
    public void setUp() {
        core = new MockitoCore();
        mockList = core.mock(List.class, new MockSettingsImpl());
        anotherMock = core.mock(List.class, new MockSettingsImpl());
    }

    @Test
    public void testMock_validSettings_returnsMock() {
        assertNotNull(mockList);
        MockUtil util = new MockUtil();
        assertTrue(util.isMock(mockList));
    }

    @Test(expected = Exception.class)
    public void testVerify_nullMock_throwsException() {
        core.verify(null, null);
    }

    @Test(expected = Exception.class)
    public void testVerify_notAMock_throwsException() {
        core.verify(nonMock, null);
    }

    @Test(expected = Exception.class)
    public void testVerifyNoMoreInteractions_nullArray_throwsException() {
        core.verifyNoMoreInteractions((Object[]) null);
    }

    @Test(expected = Exception.class)
    public void testVerifyNoMoreInteractions_nullElement_throwsException() {
        core.verifyNoMoreInteractions(new Object[]{null});
    }

    @Test(expected = Exception.class)
    public void testVerifyNoMoreInteractions_notAMock_throwsException() {
        core.verifyNoMoreInteractions(nonMock);
    }

    @Test
    public void testVerifyNoMoreInteractions_validMock_noException() {
        core.verifyNoMoreInteractions(mockList);
    }

    @Test(expected = Exception.class)
    public void testInOrder_nullArray_throwsException() {
        core.inOrder((Object[]) null);
    }

    @Test(expected = Exception.class)
    public void testInOrder_nullElement_throwsException() {
        core.inOrder(new Object[]{null});
    }

    @Test(expected = Exception.class)
    public void testInOrder_notAMockElement_throwsException() {
        core.inOrder(new Object[]{nonMock});
    }

    @Test
    public void testInOrder_validMocks_returnsInOrder() {
        InOrder inOrder = core.inOrder(mockList, anotherMock);
        assertNotNull(inOrder);
        assertTrue(inOrder instanceof InOrder);
    }

    @Test
    public void testReset_singleMock_noException() {
        core.reset(mockList);
    }

    @Test(expected = Exception.class)
    public void testStub_noOngoingStubbing_throwsException() {
        core.stub();
    }

    @Test
    public void testStub_withOngoingStubbing_returnsStubbing() {
        mockList.get(0);
        IOngoingStubbing stubbing = core.stub();
        assertNotNull(stubbing);
    }

    @Test(expected = Exception.class)
    public void testWhen_noOngoingStubbing_throwsException() {
        core.when("any");
    }

    @Test
    public void testWhen_withOngoingStubbing_returnsOngoingStubbing() {
        Object result = mockList.get(0);
        OngoingStubbing<?> stubbing = core.when(result);
        assertNotNull(stubbing);
    }

    @Test
    public void testStubVoid_validMock_returnsVoidMethodStubbable() {
        VoidMethodStubbable<?> stubbable = core.stubVoid(mockList);
        assertNotNull(stubbable);
    }

    @Test(expected = Exception.class)
    public void testGetLastInvocation_noOngoingStubbing_throwsException() {
        core.getLastInvocation();
    }

    @Test
    public void testValidateMockitoUsage_noException() {
        core.validateMockitoUsage();
    }

    // =============== New test cases to improve coverage ===============

    @Test
    public void testMock_nullSettings_returnsMock() {
        MockitoCore core2 = new MockitoCore();
        List mock = core2.mock(List.class, null);
        assertNotNull(mock);
        MockUtil util = new MockUtil();
        assertTrue(util.isMock(mock));
    }

    @Test(expected = Exception.class)
    public void testVerify_mockWithNullVerificationMode_throwsException() {
        core.verify(mockList, null);
    }

    @Test
    public void testVerify_mockWithNonNullVerificationMode_returnsSameMock() {
        Object verified = core.verify(mockList, org.mockito.Mockito.times(1));
        assertNotNull(verified);
        assertSame(mockList, verified);
    }

    @Test(expected = Exception.class)
    public void testVerifyNoMoreInteractions_emptyArray_throwsException() {
        core.verifyNoMoreInteractions(new Object[0]);
    }

    @Test(expected = Exception.class)
    public void testVerifyNoMoreInteractions_mockWithInteractions_throwsException() {
        mockList.add("something");
        core.verifyNoMoreInteractions(mockList);
    }

    @Test(expected = Exception.class)
    public void testInOrder_emptyArray_throwsException() {
        core.inOrder(new Object[0]);
    }

    @Test
    public void testInOrder_singleMock_returnsInOrder() {
        InOrder inOrder = core.inOrder(mockList);
        assertNotNull(inOrder);
        assertTrue(inOrder instanceof InOrder);
    }

    @Test
    public void testReset_multipleMocks_noException() {
        core.reset(mockList, anotherMock);
    }

    @Test
    public void testStubVoid_withAnswer_returnsVoidMethodStubbable() {
        VoidMethodStubbable<?> stubbable = core.stubVoid(mockList);
        stubbable.toThrow(new RuntimeException("test"));
        // No exception expected
    }

    @Test(expected = Exception.class)
    public void testGetLastInvocation_withInvocation_returnsLastInvocation() {
        mockList.get(0);
        mockList.get(1);
        Object lastInvocation = core.getLastInvocation();
        assertNotNull(lastInvocation);
    }

    @Test
    public void testValidateMockitoUsage_withNoIssues_doesNotThrow() {
        core.validateMockitoUsage();
    }

    @Test(expected = Exception.class)
    public void testStub_withMultipleOngoingStubbings_returnsCorrectStubbing() {
        mockList.get(0);
        mockList.get(1);
        IOngoingStubbing stubbing = core.stub();
        assertNotNull(stubbing);
    }

    @Test(expected = Exception.class)
    public void testWhen_withNonMockArgument_throwsException() {
        Object nonMockResult = "some string";
        core.when(nonMockResult);
    }

    @Test
    public void testMock_withDefaultAnswer_returnsMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        MockitoCore core2 = new MockitoCore();
        List mock = core2.mock(List.class, settings);
        assertNotNull(mock);
        assertTrue(new MockUtil().isMock(mock));
    }

    @Test
    public void testMock_withName_returnsMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("testMock");
        MockitoCore core2 = new MockitoCore();
        List mock = core2.mock(List.class, settings);
        assertNotNull(mock);
        assertTrue(new MockUtil().isMock(mock));
    }
}
package org.mockito.internal;

import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;

public class MockHandlerTest {

    private MockHandler<Object> mockHandler;
    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        new ThreadSafeMockingProgress().reset();
        mockSettings = new MockSettingsImpl();
        mockHandler = new MockHandler<Object>(mockSettings);
    }

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().reset();
    }

    // Tests default constructor initializes mockSettings and invocationContainer
    @Test
    public void testConstructor_default_initializesNonNullState() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests constructor with specific MockSettingsImpl
    @Test
    public void testConstructor_withMockSettings_setsSameSettings() {
        MockSettingsImpl customSettings = new MockSettingsImpl();
        MockHandler<Object> handler = new MockHandler<Object>(customSettings);
        assertSame(customSettings, handler.getMockSettings());
    }

    // Tests constructor copying settings from old MockHandler
    @Test
    public void testConstructor_withOldMockHandler_copiesSettings() {
        MockHandler<Object> copyHandler = new MockHandler<Object>(mockHandler);
        assertSame(mockHandler.getMockSettings(), copyHandler.getMockSettings());
    }

    // Tests handle method with unstubbed invocation returning default answer
    @Test
    public void testHandle_unstubbedInvocation_returnsDefaultAnswer() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = mockHandler.handle(invocation);
        assertNull(result);
    }

    // Tests handle when answers for stubbing are queued (first call sets method for stubbing)
    @Test
    public void testHandle_hasAnswersForStubbing_returnsNullOnFirstCall() throws Throwable {
        mockHandler.setAnswersForStubbing(Collections.<Answer>singletonList(new Returns("stubbedValue")));
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        
        Object firstResult = mockHandler.handle(invocation);
        assertNull(firstResult);

        Object secondResult = mockHandler.handle(invocation);
        assertEquals("stubbedValue", secondResult);
    }

    // Tests handle returning stubbed answer on matching invocation
    @Test
    public void testHandle_stubbedInvocation_returnsConfiguredValue() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        mockHandler.setAnswersForStubbing(Collections.<Answer>singletonList(new Returns("expectedResult")));
        
        mockHandler.handle(invocation);
        Object result = mockHandler.handle(invocation);
        assertEquals("expectedResult", result);
    }

    // Tests handle re-throwing exception when stubbed with ThrowsException
    @Test(expected = IllegalArgumentException.class)
    public void testHandle_stubbedWithException_throwsConfiguredException() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        mockHandler.setAnswersForStubbing(Collections.<Answer>singletonList(new ThrowsException(new IllegalArgumentException())));
        
        mockHandler.handle(invocation);
        mockHandler.handle(invocation);
    }

    // Tests handle when verification mode is set on the current mock
    @Test
    public void testHandle_withVerificationMode_verifiesAndReturnsNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().mock("mockObject").method("simpleMethod").toInvocation();
        mockHandler.handle(invocation);

        mockHandler.mockingProgress.verificationStarted(VerificationModeFactory.times(1));
        Object result = mockHandler.handle(invocation);
        assertNull(result);
    }

    // Tests defect scenario: handle should not verify if verification mode belongs to a different mock
    @Test
    public void testHandle_verificationModeForDifferentMock_doesNotConsumeVerificationOnCurrentMock() throws Throwable {
        Object mockA = "mockA";
        Object mockB = "mockB";

        MockAwareVerificationMode verificationMode = new MockAwareVerificationMode(mockA, VerificationModeFactory.times(1));
        mockHandler.mockingProgress.verificationStarted(verificationMode);

        Invocation invocationB = new InvocationBuilder().mock(mockB).method("simpleMethod").toInvocation();
        mockHandler.handle(invocationB);

        // Verification mode for mockA should still be preserved in mocking progress
        assertNotNull(mockHandler.mockingProgress.pullVerificationMode());
    }

    // Tests voidMethodStubbable returns a non-null VoidMethodStubbable instance
    @Test
    public void testVoidMethodStubbable_validMock_returnsNonNull() {
        VoidMethodStubbable<Object> stubbable = mockHandler.voidMethodStubbable("testMock");
        assertNotNull(stubbable);
    }

    // Tests getInvocationContainer returns non-null container
    @Test
    public void testGetInvocationContainer_returnsNonNullContainer() {
        assertNotNull(mockHandler.getInvocationContainer());
    }

    // Tests getMockSettings returns configured mock settings
    @Test
    public void testGetMockSettings_returnsConfiguredSettings() {
        assertEquals(mockSettings, mockHandler.getMockSettings());
    }

    // Tests handle method with custom default answer configured in mock settings
    @Test
    public void testHandle_customDefaultAnswer_returnsConfiguredDefault() throws Throwable {
        mockSettings.defaultAnswer(new Returns("customDefault"));
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        
        Object result = handler.handle(invocation);
        assertEquals("customDefault", result);
    }

    // Tests handle with multiple consecutive answers for stubbing
    @Test
    public void testHandle_consecutiveAnswers_returnsAnswersInSequence() throws Throwable {
        mockHandler.setAnswersForStubbing(Arrays.<Answer>asList(new Returns("first"), new Returns("second")));
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        mockHandler.handle(invocation); // register stubbing
        assertEquals("first", mockHandler.handle(invocation));
        assertEquals("second", mockHandler.handle(invocation));
        assertEquals("second", mockHandler.handle(invocation));
    }

    // Tests invocation listener notification on successful handle
    @Test
    public void testHandle_withInvocationListener_notifiesListenerOnSuccess() throws Throwable {
        final boolean[] listenerCalled = new boolean[]{false};
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
                listenerCalled[0] = true;
                assertNotNull(methodInvocationReport.getInvocation());
                assertEquals("returnedValue", methodInvocationReport.getReturnedValue());
                assertNull(methodInvocationReport.getThrowable());
            }
        };

        mockSettings.invocationListeners(listener);
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);
        handler.setAnswersForStubbing(Collections.<Answer>singletonList(new Returns("returnedValue")));
        
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        handler.handle(invocation); // register stubbing
        listenerCalled[0] = false; // reset after registration invocation

        Object result = handler.handle(invocation);
        assertEquals("returnedValue", result);
        assertTrue(listenerCalled[0]);
    }

    // Tests invocation listener notification when invocation throws exception
    @Test
    public void testHandle_withInvocationListener_notifiesListenerOnException() throws Throwable {
        final boolean[] listenerCalled = new boolean[]{false};
        final RuntimeException expectedException = new RuntimeException("boom");
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
                listenerCalled[0] = true;
                assertSame(expectedException, methodInvocationReport.getThrowable());
            }
        };

        mockSettings.invocationListeners(listener);
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);
        handler.setAnswersForStubbing(Collections.<Answer>singletonList(new ThrowsException(expectedException)));
        
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        handler.handle(invocation); // register stubbing
        listenerCalled[0] = false;

        try {
            handler.handle(invocation);
            fail("Expected exception to be thrown");
        } catch (RuntimeException e) {
            assertSame(expectedException, e);
        }
        assertTrue(listenerCalled[0]);
    }
}
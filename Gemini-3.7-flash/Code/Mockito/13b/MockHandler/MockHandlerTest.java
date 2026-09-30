package org.mockito.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.stubbing.answers.Returns;
import org.mockito.verification.VerificationMode;

public class MockHandlerTest {

    private MockHandler<String> mockHandler;
    private MockingProgress mockingProgress;

    @Before
    public void setUp() {
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.reset();
        mockHandler = new MockHandler<String>();
    }

    @After
    public void tearDown() {
        mockingProgress.reset();
    }

    // Tests default constructor initializes settings and container
    @Test
    public void testConstructor_default_initializesCorrectly() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests constructor with custom MockSettingsImpl
    @Test
    public void testConstructor_customMockSettings_setsSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("customDefault"));
        MockHandler<Object> handler = new MockHandler<Object>(settings);

        assertSame(settings, handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests copy constructor from existing MockHandler
    @Test
    public void testConstructor_fromOldMockHandler_copiesSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<String> oldHandler = new MockHandler<String>(settings);
        MockHandler<String> newHandler = new MockHandler<String>(oldHandler);

        assertSame(settings, newHandler.getMockSettings());
    }

    // Tests getMockSettings returns the configured MockSettingsImpl
    @Test
    public void testGetMockSettings_returnsConfiguredInstance() {
        MockSettingsImpl settings = mockHandler.getMockSettings();
        assertNotNull(settings);
        assertSame(settings, mockHandler.getMockSettings());
    }

    // Tests getInvocationContainer returns non-null container
    @Test
    public void testGetInvocationContainer_returnsNonNullContainer() {
        InvocationContainer container = mockHandler.getInvocationContainer();
        assertNotNull(container);
    }

    // Tests voidMethodStubbable creates non-null stubbable
    @Test
    public void testVoidMethodStubbable_validMock_returnsVoidMethodStubbable() {
        String mockInstance = "mockInstance";
        VoidMethodStubbable<String> stubbable = mockHandler.voidMethodStubbable(mockInstance);
        assertNotNull(stubbable);
    }

    // Tests setAnswersForStubbing sets consecutive answers
    @Test
    public void testSetAnswersForStubbing_validAnswerList_registersAnswers() throws Throwable {
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("first"));
        answers.add(new Returns("second"));

        mockHandler.setAnswersForStubbing(answers);

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = mockHandler.handle(invocation);

        assertNull(result);
    }

    // Tests handle method when no stub is registered, returns default answer
    @Test
    public void testHandle_unstubbedInvocation_returnsDefaultAnswer() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = mockHandler.handle(invocation);

        assertNull(result);
    }

    // Tests handle method with configured default answer
    @Test
    public void testHandle_customDefaultAnswer_returnsExpectedValue() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Returns("defaultResult"));
        MockHandler<Object> handler = new MockHandler<Object>(settings);

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = handler.handle(invocation);

        assertEquals("defaultResult", result);
    }

    // Tests handle method when matching stubbed answer exists
    @Test
    public void testHandle_stubbedAnswer_returnsStubbedValue() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        StubbedInvocationMatcher stub = new StubbedInvocationMatcher(matcher, new Returns("stubbedValue"));

        mockHandler.invocationContainerImpl.addAnswer(stub);

        Object result = mockHandler.handle(invocation);
        assertEquals("stubbedValue", result);
    }

    // Tests handle method when answers are queued for void stubbing
    @Test
    public void testHandle_hasAnswersForStubbing_setsMethodForStubbingAndReturnsNull() throws Throwable {
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("answer"));
        mockHandler.setAnswersForStubbing(answers);

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = mockHandler.handle(invocation);

        assertNull(result);
        assertTrue(mockHandler.invocationContainerImpl.hasAnswersForStubbing());
    }

    // Tests handle method during verification on matching mock
    @Test
    public void testHandle_verificationModeOnMatchingMock_verifiesAndReturnsNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object mockObject = invocation.getMock();

        VerificationMode mode = VerificationModeFactory.times(1);
        MockAwareVerificationMode mockAwareMode = new MockAwareVerificationMode(mockObject, mode);
        mockHandler.mockingProgress.verificationStarted(mockAwareMode);

        Object result = mockHandler.handle(invocation);
        assertNull(result);
    }

    // Tests handle method when verification mode is for a different mock (Defect 13 scenario)
    @Test
    public void testHandle_verificationModeOnDifferentMock_retainsVerificationMode() throws Throwable {
        Object mock1 = "mock1";
        Object mock2 = "mock2";

        VerificationMode mode = VerificationModeFactory.times(1);
        MockAwareVerificationMode mockAwareMode = new MockAwareVerificationMode(mock1, mode);
        mockHandler.mockingProgress.verificationStarted(mockAwareMode);

        Invocation invocationOnMock2 = new InvocationBuilder().mock(mock2).method("simpleMethod").toInvocation();
        mockHandler.handle(invocationOnMock2);

        VerificationMode pulledMode = mockHandler.mockingProgress.pullVerificationMode();
        assertNotNull(pulledMode);
        assertSame(mockAwareMode, pulledMode);
    }

    // Tests reset invocation for potential stubbing in handle
    @Test
    public void testHandle_unstubbedInvocation_resetsInvocationForPotentialStubbing() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        mockHandler.handle(invocation);

        InvocationContainerImpl container = (InvocationContainerImpl) mockHandler.getInvocationContainer();
        assertNotNull(container.getInvocationForPotentialStubbing());
    }

    // Tests handle method throwing exception when stubbed answer throws exception
    @Test
    public void testHandle_stubbedAnswerThrowsException_propagatesException() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        RuntimeException expectedException = new RuntimeException("Stubbed error");
        StubbedInvocationMatcher stub = new StubbedInvocationMatcher(matcher, new ThrowsException(expectedException));

        mockHandler.invocationContainerImpl.addAnswer(stub);

        try {
            mockHandler.handle(invocation);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Stubbed error", e.getMessage());
        }
    }
}
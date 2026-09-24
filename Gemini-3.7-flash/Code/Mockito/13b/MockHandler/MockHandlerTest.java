package org.mockito.internal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MockHandlerTest {

    private MockHandler<Object> mockHandler;
    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        mockSettings = new MockSettingsImpl();
        mockHandler = new MockHandler<Object>(mockSettings);
    }

    // Tests default constructor initializes settings and container
    @Test
    public void testConstructor_default_initializesNonNullSettings() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests copy constructor initializes with old handler's settings
    @Test
    public void testConstructor_copyFromOldHandler_preservesSettings() {
        MockHandler<Object> copyHandler = new MockHandler<Object>(mockHandler);
        assertSame(mockSettings, copyHandler.getMockSettings());
        assertNotNull(copyHandler.getInvocationContainer());
    }

    // Tests getMockSettings getter
    @Test
    public void testGetMockSettings_returnsConfiguredSettings() {
        assertSame(mockSettings, mockHandler.getMockSettings());
    }

    // Tests getInvocationContainer getter
    @Test
    public void testGetInvocationContainer_returnsNonNullContainer() {
        assertNotNull(mockHandler.getInvocationContainer());
    }

    // Tests voidMethodStubbable creation
    @Test
    public void testVoidMethodStubbable_returnsNonNullStubber() {
        Object mock = new Object();
        VoidMethodStubbable<Object> stubbable = mockHandler.voidMethodStubbable(mock);
        assertNotNull(stubbable);
    }

    // Tests setAnswersForStubbing sets answers in invocation container
    @Test
    public void testSetAnswersForStubbing_storesAnswersInContainer() {
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("test"));
        mockHandler.setAnswersForStubbing(answers);
        assertTrue(mockHandler.invocationContainerImpl.hasAnswersForStubbing());
    }

    // Tests handle when hasAnswersForStubbing is true
    @Test
    public void testHandle_withAnswersForStubbing_setsMethodForStubbingAndReturnsNull() throws Throwable {
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("val"));
        mockHandler.setAnswersForStubbing(answers);

        Invocation invocation = new InvocationBuilder().toInvocation();
        Object result = mockHandler.handle(invocation);

        assertNull(result);
        assertTrue(mockHandler.invocationContainerImpl.hasInvocationForPotentialStubbing());
    }

    // Tests handle standard invocation returning default answer
    @Test
    public void testHandle_unstubbedInvocation_returnsDefaultAnswer() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("toString").toInvocation();
        Object result = mockHandler.handle(invocation);
        assertNotNull(result);
        assertEquals("", result);
    }

    // Tests handle stubbed invocation returning stubbed value
    @Test
    public void testHandle_stubbedInvocation_returnsStubbedValue() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        mockHandler.invocationContainerImpl.setInvocationForPotentialStubbing(new InvocationMatcher(invocation));
        mockHandler.invocationContainerImpl.addAnswer(new Returns("stubbed_result"));

        Object result = mockHandler.handle(invocation);
        assertEquals("stubbed_result", result);
    }

    // Tests handle verification on the correct mock
    @Test
    public void testHandle_verificationOnCorrectMock_verifiesAndReturnsNull() throws Throwable {
        Invocation invocation = new InvocationBuilder().toInvocation();
        Object mock = invocation.getMock();

        VerificationMode mode = new MockAwareVerificationMode(mock, VerificationModeFactory.atLeastOnce());
        MockingProgress progress = new ThreadSafeMockingProgress();
        progress.pushVerificationMode(mode);
        mockHandler.mockingProgress = progress;

        // Record invocation first so verification succeeds
        mockHandler.invocationContainerImpl.setInvocationForPotentialStubbing(new InvocationMatcher(invocation));

        Object result = mockHandler.handle(invocation);
        assertNull(result);
        assertNull(progress.pullVerificationMode());
    }

    // Tests handle verification when invocation is on a different mock (bug 13b regression)
    @Test
    public void testHandle_verificationOnDifferentMock_reAddsVerificationMode() throws Throwable {
        Invocation invocation = new InvocationBuilder().toInvocation();
        Object differentMock = new Object();

        VerificationMode mode = new MockAwareVerificationMode(differentMock, VerificationModeFactory.atLeastOnce());
        MockingProgress progress = new ThreadSafeMockingProgress();
        progress.pushVerificationMode(mode);
        mockHandler.mockingProgress = progress;

        mockHandler.handle(invocation);

        // Verification mode should still be present in progress for the intended mock
        VerificationMode pulledMode = progress.pullVerificationMode();
        assertNotNull(pulledMode);
    }

    // Tests handle with non-MockAware verification mode
    @Test
    public void testHandle_nonMockAwareVerificationMode_proceedsToDefaultAnswer() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        VerificationMode mode = VerificationModeFactory.atLeastOnce();
        MockingProgress progress = new ThreadSafeMockingProgress();
        progress.pushVerificationMode(mode);
        mockHandler.mockingProgress = progress;

        Object result = mockHandler.handle(invocation);
        assertNull(result);
    }
}
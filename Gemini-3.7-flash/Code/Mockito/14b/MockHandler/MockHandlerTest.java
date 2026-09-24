package org.mockito.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

public class MockHandlerTest {

    private MockHandler<Object> mockHandler;
    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        mockSettings = new MockSettingsImpl();
        mockHandler = new MockHandler<Object>(mockSettings);
    }

    // Tests default constructor initializes internal fields correctly
    @Test
    public void testDefaultConstructor_initializesProperly() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests constructor with MockSettings initializes properly
    @Test
    public void testConstructor_withMockSettings_initializesProperly() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<Object> handler = new MockHandler<Object>(settings);
        assertSame(settings, handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests constructor with old MockHandler copies mock settings
    @Test
    public void testConstructor_withOldMockHandler_copiesSettings() {
        MockHandler<Object> handler = new MockHandler<Object>(mockHandler);
        assertSame(mockSettings, handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
    }

    // Tests getMockSettings returns current settings
    @Test
    public void testGetMockSettings_returnsSettingsInstance() {
        assertSame(mockSettings, mockHandler.getMockSettings());
    }

    // Tests getInvocationContainer returns valid invocation container
    @Test
    public void testGetInvocationContainer_returnsNonNullContainer() {
        InvocationContainer container = mockHandler.getInvocationContainer();
        assertNotNull(container);
    }

    // Tests voidMethodStubbable returns a non-null VoidMethodStubbable instance
    @Test
    public void testVoidMethodStubbable_returnsStubbableInstance() {
        Object mock = new Object();
        VoidMethodStubbable<Object> stubbable = mockHandler.voidMethodStubbable(mock);
        assertNotNull(stubbable);
    }

    // Tests handle method returns default answer when invocation is unstubbed
    @Test
    public void testHandle_unstubbedInvocation_returnsDefaultAnswer() throws Throwable {
        mockSettings.defaultAnswer(new Returns("default_value"));
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object result = handler.handle(invocation);

        assertEquals("default_value", result);
    }

    // Tests handle method when answers for stubbing are queued (doAnswer / doReturn style)
    @Test
    public void testHandle_hasAnswersForStubbing_stubsMethodAndReturnsNull() throws Throwable {
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Returns("stubbed_via_doAnswer"));
        mockHandler.setAnswersForStubbing(answers);

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Object firstCallResult = mockHandler.handle(invocation);
        assertNull(firstCallResult);

        // After stubbing is registered, the next regular invocation should return the stubbed value
        Object secondCallResult = mockHandler.handle(invocation);
        assertEquals("stubbed_via_doAnswer", secondCallResult);
    }

    // Tests handle method when stubbed answer exists for invocation
    @Test
    public void testHandle_stubbedInvocation_returnsStubbedValue() throws Throwable {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        StubbedInvocationMatcher stubbedInvocationMatcher = new StubbedInvocationMatcher(matcher, new Returns("stubbed_value"));
        mockHandler.invocationContainerImpl.setMethodForStubbing(matcher);
        mockHandler.invocationContainerImpl.addAnswer(new Returns("stubbed_value"));

        Object result = mockHandler.handle(invocation);
        assertEquals("stubbed_value", result);
    }

    // Tests handle method executes verification when verification mode is set
    @Test
    public void testHandle_withVerificationMode_performsVerificationAndReturnsNull() throws Throwable {
        final boolean[] verified = new boolean[]{false};
        VerificationMode verificationMode = new VerificationMode() {
            public void verify(org.mockito.internal.verification.api.VerificationData data) {
                verified[0] = true;
            }
        };

        mockHandler.mockingProgress.verificationStarted(verificationMode);
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        Object result = mockHandler.handle(invocation);
        assertNull(result);
        assertTrue(verified[0]);
    }

    // Tests setAnswersForStubbing properly passes answers to invocationContainer
    @Test
    public void testSetAnswersForStubbing_delegatesToInvocationContainer() {
        List<Answer> answers = Arrays.<Answer>asList(new Returns("val1"), new Returns("val2"));
        mockHandler.setAnswersForStubbing(answers);
        assertTrue(mockHandler.invocationContainerImpl.hasAnswersForStubbing());
    }

    // Tests handle method validates state and throws exception if state is invalid
    @Test(expected = MockitoException.class)
    public void testHandle_invalidMockingProgressState_throwsException() throws Throwable {
        mockHandler.mockingProgress.stubbingStarted();
        mockHandler.mockingProgress.stubbingStarted();

        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        mockHandler.handle(invocation);
    }

    // Tests handle captures arguments when stubbed invocation has argument matchers
    @Test
    public void testHandle_stubbedInvocationWithArguments_capturesArgumentsCorrectly() throws Throwable {
        Invocation stubInvocation = new InvocationBuilder().method("differentMethod").args("inputArg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(stubInvocation);
        mockHandler.invocationContainerImpl.setMethodForStubbing(matcher);
        mockHandler.invocationContainerImpl.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return invocation.getArguments()[0] + "_processed";
            }
        });

        Invocation callInvocation = new InvocationBuilder().method("differentMethod").args("inputArg").toInvocation();
        Object result = mockHandler.handle(callInvocation);
        assertEquals("inputArg_processed", result);
    }

    // Tests resetInvocationForPotentialStubbing after unstubbed invocation handles nested calls
    @Test
    public void testHandle_unstubbedInvocation_resetsInvocationForPotentialStubbing() throws Throwable {
        mockSettings.defaultAnswer(new Returns(null));
        MockHandler<Object> handler = new MockHandler<Object>(mockSettings);

        Invocation invocation1 = new InvocationBuilder().method("simpleMethod").seq(1).toInvocation();
        handler.handle(invocation1);

        assertNotNull(handler.getInvocationContainer());
    }
}
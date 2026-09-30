package org.mockito.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.VoidMethodStubbableImpl;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

public class MockHandlerTest {

    // Tests that default constructor creates handler with mockSettings
    @Test
    public void testDefaultConstructor_createsHandlerWithSettings() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getMockSettings());
    }

    // Tests that constructor with MockSettingsImpl sets the settings
    @Test
    public void testConstructorWithSettings_setsMockSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<Object> handler = new MockHandler<Object>(settings);
        assertSame(settings, handler.getMockSettings());
    }

    // Tests that constructor with MockHandlerInterface copies settings from old handler
    @Test
    public void testConstructorWithOldHandler_copiesSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<Object> oldHandler = new MockHandler<Object>(settings);
        MockHandler<Object> newHandler = new MockHandler<Object>((MockHandlerInterface<Object>) oldHandler);
        assertNotNull(newHandler.getMockSettings());
    }

    // Tests that handle returns null when hasAnswersForStubbing is true
    @Test
    public void testHandle_hasAnswersForStubbing_returnsNull() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        // Simulate stubbing in progress
        handler.setAnswersForStubbing(new ArrayList<Answer>());
        // We need an invocation
        Invocation dummyInvocation = new DummyInvocation();
        Object result = handler.handle(dummyInvocation);
        assertNull(result);
    }

    // Tests that handle returns null when verificationMode is not null
    @Test
    public void testHandle_verificationModeSet_returnsNull() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        Invocation dummyInvocation = new DummyInvocation();

        // Create a simple verification mode
        VerificationMode mode = new VerificationMode() {
            private static final long serialVersionUID = 1L;

            public void verify(VerificationDataImpl data) {
                // stub
            }

            public VerificationMode description(String description) {
                return this;
            }
        };

        // Set verification mode directly
        handler.mockingProgress.verificationStarted(mode);

        Object result = handler.handle(dummyInvocation);
        assertNull(result);
    }

    // Tests that handle with no stubbing and no verification returns default answer
    @Test
    public void testHandle_noStubbingNoVerification_returnsDefaultAnswer() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        Invocation dummyInvocation = new DummyInvocation();
        Object result = handler.handle(dummyInvocation);
        // Default answer should be null (default MockSettingsImpl)
        assertNull(result);
    }

    // Tests that handle with stubbing returns stubbed answer
    @Test
    public void testHandle_withStubbing_returnsStubbedAnswer() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        Invocation dummyInvocation = new DummyInvocation();
        final Object expected = "stubbed value";

        // First call to establish stubbing
        handler.handle(dummyInvocation);

        // Create a simple Answer
        Answer<Object> stubAnswer = new Answer<Object>() {
            public Object answer(Invocation invocation) throws Throwable {
                return expected;
            }
        };

        // Register stub
        handler.invocationContainerImpl.addAnswer(stubAnswer, false);

        // Second call should return stubbed value
        Object result = handler.handle(dummyInvocation);
        assertEquals(expected, result);
    }

    // Tests that voidMethodStubbable returns non-null VoidMethodStubbableImpl
    @Test
    public void testVoidMethodStubbable_returnsImpl() {
        MockHandler<Object> handler = new MockHandler<Object>();
        VoidMethodStubbable<Object> stubbable = handler.voidMethodStubbable(new Object());
        assertNotNull(stubbable);
        assertTrue(stubbable instanceof VoidMethodStubbableImpl);
    }

    // Tests that getMockSettings returns the settings
    @Test
    public void testGetMockSettings_returnsSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<Object> handler = new MockHandler<Object>(settings);
        assertSame(settings, handler.getMockSettings());
    }

    // Tests that setAnswersForStubbing sets the answers
    @Test
    public void testSetAnswersForStubbing_setsAnswers() {
        MockHandler<Object> handler = new MockHandler<Object>();
        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Answer<Object>() {
            public Object answer(Invocation invocation) throws Throwable {
                return null;
            }
        });
        handler.setAnswersForStubbing(answers);
        assertTrue(handler.invocationContainerImpl.hasAnswersForStubbing());
    }

    // Tests that getInvocationContainer returns the invocation container
    @Test
    public void testGetInvocationContainer_returnsContainer() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertNotNull(handler.getInvocationContainer());
        assertTrue(handler.getInvocationContainer() instanceof InvocationContainerImpl);
    }

    // Tests that handle with null verification mode proceeds to stubbing
    @Test
    public void testHandle_verificationModeNull_proceedsToStubbing() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        Invocation dummyInvocation = new DummyInvocation();
        
        // Ensure verification mode is not set
        handler.mockingProgress.pullVerificationMode();
        
        Object result = handler.handle(dummyInvocation);
        // Should return default answer
        assertNull(result);
    }

    // Tests that handle with stubbed invocation returns answer
    @Test
    public void testHandle_stubbedInvocationFound_returnsAnswer() throws Throwable {
        MockHandler<Object> handler = new MockHandler<Object>();
        Invocation dummyInvocation = new DummyInvocation();
        final Object expected = "found value";

        // First call to create invocation record
        handler.handle(dummyInvocation);

        // Manually add stub
        StubbedInvocationMatcher stubMatcher = new StubbedInvocationMatcher(
            new InvocationMatcher(dummyInvocation),
            new Answer<Object>() {
                public Object answer(Invocation invocation) throws Throwable {
                    return expected;
                }
            }
        );
        handler.invocationContainerImpl.addAnswer(stubMatcher.getAnswer(), false);

        // Second call should find the stub
        Object result = handler.handle(dummyInvocation);
        assertEquals(expected, result);
    }

    // Dummy Invocation for testing purposes (simulates a basic Invocation)
    private static class DummyInvocation extends Invocation {
        private static final long serialVersionUID = 1L;

        public DummyInvocation() {
            super(new Object(), new Class<?>[0], new Object[] {});
        }

        @Override
        public Object getMock() {
            return new Object();
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public boolean isVerified() {
            return false;
        }

        @Override
        public Iterable<Object> getArgumentsAsList() {
            return new ArrayList<Object>();
        }

        @Override
        public Class<?> getParameters() {
            return new Class<?>[0].getClass();
        }
    }
}
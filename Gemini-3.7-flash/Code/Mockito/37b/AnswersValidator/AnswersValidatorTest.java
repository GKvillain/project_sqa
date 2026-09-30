package org.mockito.internal.stubbing.answers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

public class AnswersValidatorTest {

    private AnswersValidator validator;

    @Before
    public void setUp() {
        validator = new AnswersValidator();
    }

    // Tests exception when stubbing with null throwable
    @Test(expected = MockitoException.class)
    public void testValidate_nullThrowable_throwsException() {
        validator.validate(new ThrowsException(null), new InvocationBuilder().toInvocation());
    }

    // Tests valid stubbing with RuntimeException
    @Test
    public void testValidate_runtimeException_isValid() {
        validator.validate(new ThrowsException(new RuntimeException()), new InvocationBuilder().toInvocation());
    }

    // Tests valid stubbing with Error
    @Test
    public void testValidate_error_isValid() {
        validator.validate(new ThrowsException(new Error()), new InvocationBuilder().toInvocation());
    }

    // Tests invalid checked exception for method that does not declare it
    @Test(expected = MockitoException.class)
    public void testValidate_invalidCheckedException_throwsException() {
        validator.validate(new ThrowsException(new Exception()), new InvocationBuilder().toInvocation());
    }

    // Tests valid checked exception for method that declares it
    @Test
    public void testValidate_validCheckedException_isValid() {
        validator.validate(new ThrowsException(new Exception()), new InvocationBuilder().method("canThrowException").toInvocation());
    }

    // Tests stubbing void method with a return value
    @Test(expected = MockitoException.class)
    public void testValidate_returnValueOnVoidMethod_throwsException() {
        validator.validate(new Returns("value"), new InvocationBuilder().method("voidMethod").toInvocation());
    }

    // Tests stubbing primitive method with null return value
    @Test(expected = MockitoException.class)
    public void testValidate_nullReturnValueOnPrimitiveMethod_throwsException() {
        validator.validate(new Returns(null), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
    }

    // Tests stubbing with incompatible return value type
    @Test(expected = MockitoException.class)
    public void testValidate_incompatibleReturnType_throwsException() {
        validator.validate(new Returns(123), new InvocationBuilder().method("simpleMethod").toInvocation());
    }

    // Tests valid stubbing with correct return type
    @Test
    public void testValidate_correctReturnType_isValid() {
        validator.validate(new Returns("valid string"), new InvocationBuilder().method("simpleMethod").toInvocation());
    }

    // Tests valid stubbing with null return value on non-primitive method
    @Test
    public void testValidate_nullReturnValueOnNonPrimitiveMethod_isValid() {
        validator.validate(new Returns(null), new InvocationBuilder().method("simpleMethod").toInvocation());
    }

    // Tests doNothing on non-void method
    @Test(expected = MockitoException.class)
    public void testValidate_doNothingOnNonVoidMethod_throwsException() {
        validator.validate(new DoesNothing(), new InvocationBuilder().method("simpleMethod").toInvocation());
    }

    // Tests doNothing on void method
    @Test
    public void testValidate_doNothingOnVoidMethod_isValid() {
        validator.validate(new DoesNothing(), new InvocationBuilder().method("voidMethod").toInvocation());
    }

    // Tests custom Answer implementation that does not match standard answer types
    @Test
    public void testValidate_customAnswer_isValid() {
        validator.validate(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }, new InvocationBuilder().toInvocation());
    }
}
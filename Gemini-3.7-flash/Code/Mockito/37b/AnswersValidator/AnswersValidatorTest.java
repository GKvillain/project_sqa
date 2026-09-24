package org.mockito.internal.stubbing.answers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.Invocation;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnswersValidatorTest {

    private AnswersValidator validator;
    private Invocation invocation;

    @Before
    public void setUp() {
        validator = new AnswersValidator();
        invocation = mock(Invocation.class);
    }

    // Tests null throwable in ThrowsException
    @Test(expected = MockitoException.class)
    public void testValidate_nullThrowable_throwsException() {
        validator.validate(new ThrowsException(null), invocation);
    }

    // Tests RuntimeException in ThrowsException which is always valid
    @Test
    public void testValidate_runtimeException_validationPasses() {
        validator.validate(new ThrowsException(new RuntimeException()), invocation);
    }

    // Tests Error in ThrowsException which is always valid
    @Test
    public void testValidate_error_validationPasses() {
        validator.validate(new ThrowsException(new Error()), invocation);
    }

    // Tests valid checked exception in ThrowsException
    @Test
    public void testValidate_validCheckedException_validationPasses() {
        when(invocation.isValidException(any(Throwable.class))).thenReturn(true);
        validator.validate(new ThrowsException(new Exception()), invocation);
    }

    // Tests invalid checked exception in ThrowsException
    @Test(expected = MockitoException.class)
    public void testValidate_invalidCheckedException_throwsException() {
        when(invocation.isValidException(any(Throwable.class))).thenReturn(false);
        validator.validate(new ThrowsException(new Exception()), invocation);
    }

    // Tests stubbing void method with a return value
    @Test(expected = MockitoException.class)
    public void testValidate_returnsOnVoidMethod_throwsException() {
        when(invocation.isVoid()).thenReturn(true);
        validator.validate(new Returns("value"), invocation);
    }

    // Tests returning null for primitive return type
    @Test(expected = MockitoException.class)
    public void testValidate_returnsNullOnPrimitive_throwsException() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(true);
        validator.validate(new Returns(null), invocation);
    }

    // Tests returning null for non-primitive return type
    @Test
    public void testValidate_returnsNullOnNonPrimitive_validationPasses() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(false);
        validator.validate(new Returns(null), invocation);
    }

    // Tests returning incompatible return type
    @Test(expected = MockitoException.class)
    public void testValidate_returnsInvalidType_throwsException() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(false);
        when(invocation.isValidReturnType(any(Class.class))).thenReturn(false);
        validator.validate(new Returns("string"), invocation);
    }

    // Tests returning compatible return type
    @Test
    public void testValidate_returnsValidType_validationPasses() {
        when(invocation.isVoid()).thenReturn(false);
        when(invocation.returnsPrimitive()).thenReturn(false);
        when(invocation.isValidReturnType(any(Class.class))).thenReturn(true);
        validator.validate(new Returns("string"), invocation);
    }

    // Tests DoesNothing on a non-void method
    @Test(expected = MockitoException.class)
    public void testValidate_doesNothingOnNonVoidMethod_throwsException() {
        when(invocation.isVoid()).thenReturn(false);
        validator.validate(new DoesNothing(), invocation);
    }

    // Tests DoesNothing on a void method
    @Test
    public void testValidate_doesNothingOnVoidMethod_validationPasses() {
        when(invocation.isVoid()).thenReturn(true);
        validator.validate(new DoesNothing(), invocation);
    }

    // Tests custom answer instance that does not match specific types
    @Test
    public void testValidate_customAnswer_validationPasses() {
        Answer<Object> customAnswer = mock(Answer.class);
        validator.validate(customAnswer, invocation);
    }
}
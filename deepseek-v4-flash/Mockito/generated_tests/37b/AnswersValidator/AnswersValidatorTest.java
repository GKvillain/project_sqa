package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import org.mockito.internal.invocation.Invocation;

public class AnswersValidatorTest {

    private AnswersValidator validator = new AnswersValidator();

    private Invocation createMockInvocation(boolean isVoid, boolean returnsPrimitive,
                                            boolean isValidReturnType, Class<?> returnType,
                                            String methodName, boolean isValidException) {
        Invocation mock = Mockito.mock(Invocation.class);
        Mockito.when(mock.isVoid()).thenReturn(isVoid);
        Mockito.when(mock.returnsPrimitive()).thenReturn(returnsPrimitive);
        Mockito.when(mock.isValidReturnType(Mockito.any())).thenReturn(isValidReturnType);
        Mockito.when(mock.printMethodReturnType()).thenReturn(returnType != null ? returnType.getSimpleName() : "");
        Mockito.when(mock.getMethodName()).thenReturn(methodName != null ? methodName : "");
        Mockito.when(mock.isValidException(Mockito.any())).thenReturn(isValidException);
        return mock;
    }

    // Tests ThrowsException with null throwable -> reporter.cannotStubWithNullThrowable() called
    @Test(expected = RuntimeException.class)
    public void testValidate_throwsExceptionNullThrowable_cannotStubWithNullThrowable() {
        ThrowsException answer = Mockito.mock(ThrowsException.class);
        Mockito.when(answer.getThrowable()).thenReturn(null);
        Invocation invocation = createMockInvocation(false, false, true, null, null, false);
        validator.validate(answer, invocation);
    }

    // Tests ThrowsException with RuntimeException -> no exception expected
    @Test
    public void testValidate_throwsExceptionRuntimeException_noException() {
        ThrowsException answer = Mockito.mock(ThrowsException.class);
        Mockito.when(answer.getThrowable()).thenReturn(new RuntimeException("test"));
        Invocation invocation = createMockInvocation(false, false, true, null, null, false);
        validator.validate(answer, invocation);
    }

    // Tests ThrowsException with Error -> no exception expected
    @Test
    public void testValidate_throwsExceptionError_noException() {
        ThrowsException answer = Mockito.mock(ThrowsException.class);
        Mockito.when(answer.getThrowable()).thenReturn(new Error("test"));
        Invocation invocation = createMockInvocation(false, false, true, null, null, false);
        validator.validate(answer, invocation);
    }

    // Tests ThrowsException with invalid checked exception -> reporter.checkedExceptionInvalid() called
    @Test(expected = RuntimeException.class)
    public void testValidate_throwsExceptionCheckedExceptionInvalid_checkedExceptionInvalid() {
        ThrowsException answer = Mockito.mock(ThrowsException.class);
        Mockito.when(answer.getThrowable()).thenReturn(new Exception("checked"));
        Invocation invocation = createMockInvocation(false, false, true, null, null, false);
        Mockito.when(invocation.isValidException(Mockito.any())).thenReturn(false);
        validator.validate(answer, invocation);
    }

    // Tests ThrowsException with valid checked exception -> no exception expected
    @Test
    public void testValidate_throwsExceptionCheckedExceptionValid_noException() {
        ThrowsException answer = Mockito.mock(ThrowsException.class);
        Mockito.when(answer.getThrowable()).thenReturn(new Exception("checked"));
        Invocation invocation = createMockInvocation(false, false, true, null, null, true);
        validator.validate(answer, invocation);
    }

    // Tests Returns with void method -> reporter.cannotStubVoidMethodWithAReturnValue() called
    @Test(expected = RuntimeException.class)
    public void testValidate_returnsVoidMethod_cannotStubVoidMethodWithAReturnValue() {
        Returns answer = Mockito.mock(Returns.class);
        Mockito.when(answer.returnsNull()).thenReturn(false);
        Mockito.doReturn(String.class).when(answer).getReturnType();
        Mockito.when(answer.printReturnType()).thenReturn("String");
        Invocation invocation = createMockInvocation(true, false, true, null, null, false);
        validator.validate(answer, invocation);
    }

    // Tests Returns with null and primitive -> reporter.wrongTypeOfReturnValue() called (null case)
    @Test(expected = RuntimeException.class)
    public void testValidate_returnsNullAndPrimitive_wrongTypeOfReturnValue() {
        Returns answer = Mockito.mock(Returns.class);
        Mockito.when(answer.returnsNull()).thenReturn(true);
        Invocation invocation = createMockInvocation(false, true, false, Integer.class, "testMethod", false);
        validator.validate(answer, invocation);
    }

    // Tests Returns with non-null and invalid return type -> reporter.wrongTypeOfReturnValue() called
    @Test(expected = RuntimeException.class)
    public void testValidate_returnsNotNullInvalidReturnType_wrongTypeOfReturnValue() {
        Returns answer = Mockito.mock(Returns.class);
        Mockito.when(answer.returnsNull()).thenReturn(false);
        Mockito.doReturn(Integer.class).when(answer).getReturnType();
        Mockito.when(answer.printReturnType()).thenReturn("Integer");
        Invocation invocation = createMockInvocation(false, false, false, Integer.class, "testMethod", false);
        validator.validate(answer, invocation);
    }

    // Tests Returns with non-null and valid return type -> no exception expected
    @Test
    public void testValidate_returnsNotNullValidReturnType_noException() {
        Returns answer = Mockito.mock(Returns.class);
        Mockito.when(answer.returnsNull()).thenReturn(false);
        Mockito.doReturn(String.class).when(answer).getReturnType();
        Mockito.when(answer.printReturnType()).thenReturn("String");
        Invocation invocation = createMockInvocation(false, false, true, String.class, "testMethod", false);
        validator.validate(answer, invocation);
    }

    // Tests DoesNothing with non-void method -> reporter.onlyVoidMethodsCanBeSetToDoNothing() called
    @Test(expected = RuntimeException.class)
    public void testValidate_doNothingNonVoidMethod_onlyVoidMethodsCanBeSetToDoNothing() {
        DoesNothing answer = Mockito.mock(DoesNothing.class);
        Invocation invocation = createMockInvocation(false, false, true, null, null, false);
        validator.validate(answer, invocation);
    }

    // Tests DoesNothing with void method -> no exception expected
    @Test
    public void testValidate_doNothingVoidMethod_noException() {
        DoesNothing answer = Mockito.mock(DoesNothing.class);
        Invocation invocation = createMockInvocation(true, false, true, null, null, false);
        validator.validate(answer, invocation);
    }
}
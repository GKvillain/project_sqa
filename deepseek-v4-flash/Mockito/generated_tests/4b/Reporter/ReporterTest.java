package org.mockito.exceptions;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReporterTest {

    private Reporter reporter = new Reporter();

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testCheckedExceptionInvalid_throwable_throwsMockitoException() {
        reporter.checkedExceptionInvalid(new RuntimeException("test"));
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testCannotStubWithNullThrowable_throwsMockitoException() {
        reporter.cannotStubWithNullThrowable();
    }

    @Test(expected = org.mockito.exceptions.misusing.UnfinishedStubbingException.class)
    public void testUnfinishedStubbing_location_throwsUnfinishedStubbingException() {
        reporter.unfinishedStubbing(new org.mockito.internal.debugging.LocationImpl());
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testIncorrectUseOfApi_throwsMockitoException() {
        reporter.incorrectUseOfApi();
    }

    @Test(expected = org.mockito.exceptions.misusing.MissingMethodInvocationException.class)
    public void testMissingMethodInvocation_throwsMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    @Test(expected = org.mockito.exceptions.misusing.UnfinishedVerificationException.class)
    public void testUnfinishedVerificationException_location_throwsUnfinishedVerificationException() {
        reporter.unfinishedVerificationException(new org.mockito.internal.debugging.LocationImpl());
    }

    @Test(expected = org.mockito.exceptions.misusing.NotAMockException.class)
    public void testNotAMockPassedToVerify_class_throwsNotAMockException() {
        reporter.notAMockPassedToVerify(String.class);
    }

    @Test(expected = org.mockito.exceptions.misusing.NullInsteadOfMockException.class)
    public void testNullPassedToVerify_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerify();
    }

    @Test(expected = org.mockito.exceptions.misusing.NotAMockException.class)
    public void testNotAMockPassedToWhenMethod_throwsNotAMockException() {
        reporter.notAMockPassedToWhenMethod();
    }

    @Test(expected = org.mockito.exceptions.misusing.NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod_throwsNullInsteadOfMockException() {
        reporter.nullPassedToWhenMethod();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions_throwsMockitoException() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    @Test(expected = org.mockito.exceptions.misusing.NotAMockException.class)
    public void testNotAMockPassedToVerifyNoMoreInteractions_throwsNotAMockException() {
        reporter.notAMockPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = org.mockito.exceptions.misusing.NullInsteadOfMockException.class)
    public void testNullPassedToVerifyNoMoreInteractions_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerifyNoMoreInteractions();
    }

    @Test(expected = org.mockito.exceptions.misusing.NotAMockException.class)
    public void testNotAMockPassedWhenCreatingInOrder_throwsNotAMockException() {
        reporter.notAMockPassedWhenCreatingInOrder();
    }

    @Test(expected = org.mockito.exceptions.misusing.NullInsteadOfMockException.class)
    public void testNullPassedWhenCreatingInOrder_throwsNullInsteadOfMockException() {
        reporter.nullPassedWhenCreatingInOrder();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testMocksHaveToBePassedWhenCreatingInOrder_throwsMockitoException() {
        reporter.mocksHaveToBePassedWhenCreatingInOrder();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testInOrderRequiresFamiliarMock_throwsMockitoException() {
        reporter.inOrderRequiresFamiliarMock();
    }

    @Test(expected = org.mockito.exceptions.misusing.CannotVerifyStubOnlyMock.class)
    public void testStubPassedToVerify_throwsCannotVerifyStubOnlyMock() {
        reporter.stubPassedToVerify();
    }

    @Test(expected = org.mockito.exceptions.misusing.InvalidUseOfMatchersException.class)
    public void testReportNoSubMatchersFound_string_throwsInvalidUseOfMatchersException() {
        reporter.reportNoSubMatchersFound("anyMatcher");
    }

    @Test(expected = org.mockito.exceptions.misusing.CannotStubVoidMethodWithReturnValue.class)
    public void testCannotStubVoidMethodWithAReturnValue_string_throwsCannotStubVoidMethodWithReturnValue() {
        reporter.cannotStubVoidMethodWithAReturnValue("someMethod");
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testOnlyVoidMethodsCanBeSetToDoNothing_throwsMockitoException() {
        reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testCannotMockFinalClass_class_throwsMockitoException() {
        reporter.cannotMockFinalClass(String.class);
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testCannotVerifyToString_throwsMockitoException() {
        reporter.cannotVerifyToString();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testExtraInterfacesDoesNotAcceptNullParameters_throwsMockitoException() {
        reporter.extraInterfacesDoesNotAcceptNullParameters();
    }

    @Test(expected = org.mockito.exceptions.base.MockitoException.class)
    public void testExtraInterfacesAcceptsOnlyInterfaces_nonInterface_throwsMockitoException() {
        reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
    }

    @Test(expected = org.mockito.exceptions.base.MockitoAssertionError.class)
    public void testWantedAtMostX_values_throwsMockitoAssertionError() {
        reporter.wantedAtMostX(3, 5);
    }
}
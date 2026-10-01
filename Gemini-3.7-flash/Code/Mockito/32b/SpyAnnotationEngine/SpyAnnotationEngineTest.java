package org.mockito.internal.configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;

public class SpyAnnotationEngineTest {

    private SpyAnnotationEngine engine;

    @Before
    public void setUp() {
        engine = new SpyAnnotationEngine();
    }

    private static class ValidSpyTestClass {
        @Spy
        List<String> spiedList = new ArrayList<String>();

        List<String> regularList = new ArrayList<String>();
    }

    private static class NullSpyTestClass {
        @Spy
        List<String> nullList;
    }

    private static class MultipleAnnotationsTestClass {
        @Spy
        @Mock
        List<String> spyAndMock = new ArrayList<String>();
    }

    private static class CaptorAndSpyTestClass {
        @Spy
        @Captor
        List<String> spyAndCaptor = new ArrayList<String>();
    }

    private static class PrivateFieldTestClass {
        @Spy
        private List<String> privateList = new ArrayList<String>();

        public List<String> getPrivateList() {
            return privateList;
        }
    }

    // Tests createMockFor always returns null as per implementation
    @Test
    public void testCreateMockFor_anyInput_returnsNull() {
        assertNull(engine.createMockFor(null, null));
    }

    // Tests processing normal @Spy annotated field creates a mock/spy
    @Test
    public void testProcess_validSpyField_createsSpy() {
        ValidSpyTestClass testInstance = new ValidSpyTestClass();
        engine.process(ValidSpyTestClass.class, testInstance);
        assertTrue(new MockUtil().isMock(testInstance.spiedList));
    }

    // Tests field without annotation remains un-spied
    @Test
    public void testProcess_fieldWithoutAnnotation_notSpied() {
        ValidSpyTestClass testInstance = new ValidSpyTestClass();
        engine.process(ValidSpyTestClass.class, testInstance);
        assertFalse(new MockUtil().isMock(testInstance.regularList));
    }

    // Tests @Spy field with null instance throws MockitoException
    @Test(expected = MockitoException.class)
    public void testProcess_nullSpyInstance_throwsMockitoException() {
        NullSpyTestClass testInstance = new NullSpyTestClass();
        engine.process(NullSpyTestClass.class, testInstance);
    }

    // Tests @Spy on an already mocked/spied instance resets the mock
    @Test
    public void testProcess_alreadyMockedInstance_resetsMock() {
        ValidSpyTestClass testInstance = new ValidSpyTestClass();
        testInstance.spiedList = Mockito.spy(new ArrayList<String>());
        testInstance.spiedList.add("element");
        engine.process(ValidSpyTestClass.class, testInstance);
        assertTrue(new MockUtil().isMock(testInstance.spiedList));
        assertEquals(0, testInstance.spiedList.size());
    }

    // Tests unsupported combination of @Spy and @Mock annotations throws exception
    @Test(expected = MockitoException.class)
    public void testProcess_spyAndMockAnnotation_throwsMockitoException() {
        MultipleAnnotationsTestClass testInstance = new MultipleAnnotationsTestClass();
        engine.process(MultipleAnnotationsTestClass.class, testInstance);
    }

    // Tests unsupported combination of @Spy and @Captor annotations throws exception
    @Test(expected = MockitoException.class)
    public void testProcess_spyAndCaptorAnnotation_throwsMockitoException() {
        CaptorAndSpyTestClass testInstance = new CaptorAndSpyTestClass();
        engine.process(CaptorAndSpyTestClass.class, testInstance);
    }

    // Tests private field is made accessible and spied properly
    @Test
    public void testProcess_privateSpyField_successfullySpies() {
        PrivateFieldTestClass testInstance = new PrivateFieldTestClass();
        engine.process(PrivateFieldTestClass.class, testInstance);
        assertTrue(new MockUtil().isMock(testInstance.getPrivateList()));
    }

    // Tests assertNoAnnotations when no undesired annotation is present
    @Test
    public void testAssertNoAnnotations_noUndesiredAnnotation_doesNotThrow() throws NoSuchFieldException {
        Field field = ValidSpyTestClass.class.getDeclaredField("spiedList");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
    }

    // Tests assertNoAnnotations when undesired annotation is present
    @Test(expected = MockitoException.class)
    public void testAssertNoAnnotations_withUndesiredAnnotation_throwsException() throws NoSuchFieldException {
        Field field = MultipleAnnotationsTestClass.class.getDeclaredField("spyAndMock");
        engine.assertNoAnnotations(Spy.class, field, Mock.class);
    }
}
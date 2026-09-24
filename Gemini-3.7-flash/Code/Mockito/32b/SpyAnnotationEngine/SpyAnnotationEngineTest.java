package org.mockito.internal.configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SpyAnnotationEngineTest {

    private SpyAnnotationEngine engine;

    @Before
    public void setUp() {
        engine = new SpyAnnotationEngine();
    }

    // Helper classes for testing annotations
    private static class ClassWithValidSpy {
        @Spy
        List<String> spiedList = new ArrayList<String>();
    }

    private static class ClassWithNullSpy {
        @Spy
        List<String> nullList;
    }

    private static class ClassWithPreMockedSpy {
        @Spy
        List<String> alreadyMocked = Mockito.spy(new ArrayList<String>());
    }

    private static class ClassWithNoAnnotations {
        List<String> regularList = new ArrayList<String>();
        String regularString = "test";
    }

    private static class ClassWithEmptyFields {
    }

    private static class ClassWithSpyAndMock {
        @Spy
        @Mock
        List<String> invalidCombination = new ArrayList<String>();
    }

    private static class ClassWithSpyAndCaptor {
        @Spy
        @Captor
        ArgumentCaptor<String> invalidCombination;
    }

    @SuppressWarnings("deprecation")
    private static class ClassWithSpyAndDeprecatedMock {
        @Spy
        @org.mockito.MockitoAnnotations.Mock
        List<String> invalidCombination = new ArrayList<String>();
    }

    private static class ClassWithPrivateSpy {
        @Spy
        private List<String> privateList = new ArrayList<String>();

        public List<String> getPrivateList() {
            return privateList;
        }
    }

    // Tests createMockFor always returns null
    @Test
    public void testCreateMockFor_anyParameters_returnsNull() {
        Object result = engine.createMockFor(null, null);
        assertNull(result);
    }

    // Tests normal case where a valid instance is turned into a spy
    @Test
    public void testProcess_validSpyField_createsSpyInstance() {
        ClassWithValidSpy testInstance = new ClassWithValidSpy();
        List<String> originalList = testInstance.spiedList;

        engine.process(ClassWithValidSpy.class, testInstance);

        assertNotNull(testInstance.spiedList);
        assertTrue(new MockUtil().isMock(testInstance.spiedList));
        assertEquals(originalList, testInstance.spiedList);
    }

    // Tests exception path when field annotated with @Spy is null
    @Test(expected = MockitoException.class)
    public void testProcess_nullSpyField_throwsMockitoException() {
        ClassWithNullSpy testInstance = new ClassWithNullSpy();
        engine.process(ClassWithNullSpy.class, testInstance);
    }

    // Tests branch where the field instance is already a mock/spy
    @Test
    public void testProcess_alreadyMockedInstance_resetsMock() {
        ClassWithPreMockedSpy testInstance = new ClassWithPreMockedSpy();
        testInstance.alreadyMocked.add("element");
        assertEquals(1, testInstance.alreadyMocked.size());

        engine.process(ClassWithPreMockedSpy.class, testInstance);

        assertTrue(new MockUtil().isMock(testInstance.alreadyMocked));
    }

    // Tests that fields without @Spy are ignored
    @Test
    public void testProcess_fieldsWithoutSpyAnnotation_doNotModifyFields() {
        ClassWithNoAnnotations testInstance = new ClassWithNoAnnotations();
        List<String> originalList = testInstance.regularList;

        engine.process(ClassWithNoAnnotations.class, testInstance);

        assertFalse(new MockUtil().isMock(testInstance.regularList));
        assertEquals(originalList, testInstance.regularList);
        assertEquals("test", testInstance.regularString);
    }

    // Tests boundary condition with a class containing no declared fields
    @Test
    public void testProcess_classWithoutFields_doesNothing() {
        ClassWithEmptyFields testInstance = new ClassWithEmptyFields();
        engine.process(ClassWithEmptyFields.class, testInstance);
        assertNotNull(testInstance);
    }

    // Tests private field accessibility handling
    @Test
    public void testProcess_privateSpyField_successfullyInitializesSpy() {
        ClassWithPrivateSpy testInstance = new ClassWithPrivateSpy();
        engine.process(ClassWithPrivateSpy.class, testInstance);

        assertNotNull(testInstance.getPrivateList());
        assertTrue(new MockUtil().isMock(testInstance.getPrivateList()));
    }

    // Tests exception path when field has both @Spy and @Mock annotations
    @Test(expected = MockitoException.class)
    public void testProcess_spyAndMockAnnotations_throwsMockitoException() {
        ClassWithSpyAndMock testInstance = new ClassWithSpyAndMock();
        engine.process(ClassWithSpyAndMock.class, testInstance);
    }

    // Tests exception path when field has both @Spy and @Captor annotations
    @Test(expected = MockitoException.class)
    public void testProcess_spyAndCaptorAnnotations_throwsMockitoException() {
        ClassWithSpyAndCaptor testInstance = new ClassWithSpyAndCaptor();
        engine.process(ClassWithSpyAndCaptor.class, testInstance);
    }

    // Tests exception path when field has both @Spy and deprecated @Mock annotations
    @Test(expected = MockitoException.class)
    public void testProcess_spyAndDeprecatedMockAnnotations_throwsMockitoException() {
        ClassWithSpyAndDeprecatedMock testInstance = new ClassWithSpyAndDeprecatedMock();
        engine.process(ClassWithSpyAndDeprecatedMock.class, testInstance);
    }

    // Tests assertNoAnnotations when no conflicting annotations are present
    @Test
    public void testAssertNoAnnotations_noConflictingAnnotations_doesNotThrow() throws NoSuchFieldException {
        Field field = ClassWithValidSpy.class.getDeclaredField("spiedList");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
    }

    // Tests assertNoAnnotations when conflicting annotations are present
    @Test(expected = MockitoException.class)
    public void testAssertNoAnnotations_conflictingAnnotationPresent_throwsMockitoException() throws NoSuchFieldException {
        Field field = ClassWithSpyAndMock.class.getDeclaredField("invalidCombination");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
    }
}
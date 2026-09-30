package org.mockito.internal.configuration;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

public class SpyAnnotationEngineTest {

    private SpyAnnotationEngine engine = new SpyAnnotationEngine();

    // Fixtures for different scenarios
    static class NormalSpy {
        @Spy private List<String> spyField = new ArrayList<>();
    }

    static class NullSpy {
        @Spy private List<String> spyField = null;
    }

    static class AlreadyMockSpy {
        @Spy private List<String> spyField = Mockito.mock(List.class);
    }

    static class SpyWithMock {
        @Spy @Mock private List<String> spyField = new ArrayList<>();
    }

    static class SpyWithCaptor {
        @Spy @Captor private List<String> spyField = new ArrayList<>();
    }

    static class NoSpyField {
        private String notSpy;
    }

    // Tests positive normal case: non-null instance, non-mock
    @Test
    public void testProcess_normalSpy_createsSpyAndRestoresAccessibility() throws Exception {
        NormalSpy obj = new NormalSpy();
        Field field = NormalSpy.class.getDeclaredField("spyField");
        boolean originalAccessible = field.isAccessible();
        Object originalValue = field.get(obj);

        engine.process(NormalSpy.class, obj);

        Object newValue = field.get(obj);
        assertNotNull(newValue);
        assertTrue(new MockUtil().isMock(newValue));
        assertNotSame(originalValue, newValue);
        assertEquals(originalAccessible, field.isAccessible());
    }

    // Tests exception path: null instance
    @Test(expected = MockitoException.class)
    public void testProcess_nullSpy_throwsMockitoException() {
        NullSpy obj = new NullSpy();
        engine.process(NullSpy.class, obj);
    }

    // Tests branch where instance is already a mock – should reset
    @Test
    public void testProcess_alreadyMockSpy_resetsMock() throws Exception {
        AlreadyMockSpy obj = new AlreadyMockSpy();
        Field field = AlreadyMockSpy.class.getDeclaredField("spyField");
        Object mockInstance = field.get(obj);
        assertTrue(new MockUtil().isMock(mockInstance));

        engine.process(AlreadyMockSpy.class, obj);

        Object after = field.get(obj);
        assertSame(mockInstance, after);
        assertTrue(new MockUtil().isMock(after));
    }

    // Tests combination with @Mock
    @Test(expected = MockitoException.class)
    public void testProcess_spyWithMockAnnotation_throwsException() {
        SpyWithMock obj = new SpyWithMock();
        engine.process(SpyWithMock.class, obj);
    }

    // Tests combination with @Captor
    @Test(expected = MockitoException.class)
    public void testProcess_spyWithCaptorAnnotation_throwsException() {
        SpyWithCaptor obj = new SpyWithCaptor();
        engine.process(SpyWithCaptor.class, obj);
    }

    // Tests no @Spy field
    @Test
    public void testProcess_noSpyField_doesNothing() {
        NoSpyField obj = new NoSpyField();
        engine.process(NoSpyField.class, obj);
        // No exception expected
    }

    // Tests that accessibility is restored even when an exception occurs
    @Test
    public void testProcess_nullSpy_restoresAccessibility() throws Exception {
        NullSpy obj = new NullSpy();
        Field field = NullSpy.class.getDeclaredField("spyField");
        boolean originalAccessible = field.isAccessible();
        try {
            engine.process(NullSpy.class, obj);
        } catch (MockitoException e) {
            // expected
        }
        assertEquals(originalAccessible, field.isAccessible());
    }
}
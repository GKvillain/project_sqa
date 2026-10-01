package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonWriteContextTest {

    // helper to create root context without DupDetector
    private JsonWriteContext createRoot() {
        return JsonWriteContext.createRootContext(null);
    }

    // simple DupDetector stub that never reports duplicates,
    // and returns a new stub for children
    private static class NoOpDupDetector extends DupDetector {
        @Override
        public boolean isDup(String name) {
            return false;
        }
        @Override
        public DupDetector child() {
            return new NoOpDupDetector();
        }
        @Override
        public void reset() {
            // no-op
        }
    }

    // --- Root context creation ---
    @Test
    public void testCreateRootContext_defaultState() {
        JsonWriteContext root = createRoot();
        assertNotNull(root);
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getType());
        assertNull(root.getParent());
        assertEquals(-1, root.getCurrentIndex());
        assertNull(root.getCurrentName());
        assertNull(root.getCurrentValue());
    }

    // --- Child array context creation and reuse ---
    @Test
    public void testCreateChildArrayContext_newAndReuse() {
        JsonWriteContext root = createRoot();
        // first creation
        JsonWriteContext child1 = root.createChildArrayContext();
        assertNotNull(child1);
        assertEquals(JsonStreamContext.TYPE_ARRAY, child1.getType());
        assertSame(root, child1.getParent());
        assertEquals(-1, child1.getCurrentIndex());

        // second creation – reuse same object, reset
        JsonWriteContext child2 = root.createChildArrayContext();
        assertSame(child1, child2);
        assertEquals(JsonStreamContext.TYPE_ARRAY, child2.getType());
        assertEquals(-1, child2.getCurrentIndex());
        assertNull(child2.getCurrentName());
    }

    // --- Child object context creation and reuse ---
    @Test
    public void testCreateChildObjectContext_newAndReuse() {
        JsonWriteContext root = createRoot();
        JsonWriteContext child1 = root.createChildObjectContext();
        assertNotNull(child1);
        assertEquals(JsonStreamContext.TYPE_OBJECT, child1.getType());
        assertSame(root, child1.getParent());
        assertEquals(-1, child1.getCurrentIndex());

        // reuse
        JsonWriteContext child2 = root.createChildObjectContext();
        assertSame(child1, child2);
        assertEquals(JsonStreamContext.TYPE_OBJECT, child2.getType());
        assertEquals(-1, child2.getCurrentIndex());
        assertNull(child2.getCurrentName());
    }

    // --- writeValue in root context ---
    @Test
    public void testWriteValue_inRoot() {
        JsonWriteContext root = createRoot();
        // first value
        int status = root.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status);
        assertEquals(0, root.getCurrentIndex());
        // second value
        status = root.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, status);
        assertEquals(1, root.getCurrentIndex());
    }

    // --- writeValue in array context ---
    @Test
    public void testWriteValue_inArray() {
        JsonWriteContext root = createRoot();
        JsonWriteContext array = root.createChildArrayContext();
        // first element
        int status = array.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status);
        assertEquals(0, array.getCurrentIndex());
        // second element
        status = array.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status);
        assertEquals(1, array.getCurrentIndex());
    }

    // --- writeValue in object context (after a field name) ---
    @Test
    public void testWriteValue_inObject() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("a");
        int status = obj.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, status);
        assertEquals(0, obj.getCurrentIndex());
    }

    // --- writeFieldName on fresh object (first field) ---
    @Test
    public void testWriteFieldName_inObject_first() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        int status = obj.writeFieldName("field1");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status);
        assertEquals("field1", obj.getCurrentName());
        assertEquals(-1, obj.getCurrentIndex());
    }

    // --- writeFieldName after a value (expect comma) ---
    @Test
    public void testWriteFieldName_inObject_afterValue() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("a");
        obj.writeValue();
        int status = obj.writeFieldName("b");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status);
        assertEquals("b", obj.getCurrentName());
        assertEquals(0, obj.getCurrentIndex());
    }

    // --- writeFieldName twice without value → expect value ---
    @Test
    public void testWriteFieldName_inObject_withoutValue_returnsExpectValue() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("a");
        int status = obj.writeFieldName("b");
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, status);
    }

    // --- writeFieldName with non-null DupDetector that does not detect duplicate ---
    @Test
    public void testWriteFieldName_withDupsNotNull_shouldNotThrow() throws JsonProcessingException {
        DupDetector dups = new NoOpDupDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("a");
        obj.writeValue();
        int status = obj.writeFieldName("b");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status);
    }

    // --- writeFieldName with duplicate field → exception ---
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_duplicate_throwsException() throws JsonProcessingException {
        DupDetector dups = new DupDetector() {
            boolean seen = false;
            @Override
            public boolean isDup(String name) {
                if (seen) return true;
                seen = true;
                return false;
            }
            @Override
            public DupDetector child() {
                return new DupDetector() {
                    boolean childSeen = false;
                    @Override
                    public boolean isDup(String name) {
                        if (childSeen) return true;
                        childSeen = true;
                        return false;
                    }
                    @Override
                    public DupDetector child() {
                        return null;
                    }
                    @Override
                    public void reset() {
                    }
                };
            }
            @Override
            public void reset() {
            }
        };
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("dupField");
        obj.writeValue();
        // second same field should raise duplicate
        obj.writeFieldName("dupField");
    }

    // --- Reuse of child context when parent has DupDetector (reset path) ---
    @Test
    public void testReuse_childContext_resetDups() {
        DupDetector dups = new NoOpDupDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        JsonWriteContext child = root.createChildObjectContext();
        // reuse
        JsonWriteContext child2 = root.createChildObjectContext();
        assertSame(child, child2);
        assertEquals(-1, child2.getCurrentIndex());
        // no exception should occur
    }

    // --- get/setCurrentValue ---
    @Test
    public void testGetSetCurrentValue() {
        JsonWriteContext root = createRoot();
        assertNull(root.getCurrentValue());
        Object val = "testValue";
        root.setCurrentValue(val);
        assertSame(val, root.getCurrentValue());
    }

    // --- toString for root context ---
    @Test
    public void testToString_root() {
        JsonWriteContext root = createRoot();
        assertEquals("/", root.toString());
    }

    // --- toString for object context with a field name ---
    @Test
    public void testToString_objectWithName() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        obj.writeFieldName("myField");
        assertEquals("{\"myField\"}", obj.toString());
    }

    // --- toString for object context without a field name ---
    @Test
    public void testToString_objectWithoutName() {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        assertEquals("{?}", obj.toString());
    }

    // --- toString for array context before and after writeValue ---
    @Test
    public void testToString_array() {
        JsonWriteContext root = createRoot();
        JsonWriteContext array = root.createChildArrayContext();
        // before any value (index = -1)
        assertEquals("[-1]", array.toString());
        // after first value (index = 0)
        array.writeValue();
        assertEquals("[0]", array.toString());
    }

    // ========================
    // New tests for uncovered parts (AI test suite compile failed)
    // ========================

    // --- writeFieldName on root context should throw an exception ---
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_inRootContext_throwsException() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        root.writeFieldName("test");
    }

    // --- writeFieldName on array context should throw an exception ---
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_inArrayContext_throwsException() throws JsonProcessingException {
        JsonWriteContext root = createRoot();
        JsonWriteContext array = root.createChildArrayContext();
        array.writeFieldName("test");
    }

    // --- writeValue in object context without a field name should return STATUS_EXPECT_NAME ---
    @Test
    public void testWriteValue_inObjectWithoutFieldName_returnsExpectName() {
        JsonWriteContext root = createRoot();
        JsonWriteContext obj = root.createChildObjectContext();
        int status = obj.writeValue();
        assertEquals(JsonWriteContext.STATUS_EXPECT_NAME, status);
    }

    // --- create child object context when parent's DupDetector.child() returns null ---
    @Test
    public void testCreateChildObjectContext_withNullDupDetectorChild() {
        DupDetector dups = new DupDetector() {
            @Override
            public boolean isDup(String name) {
                return false;
            }
            @Override
            public DupDetector child() {
                return null;
            }
            @Override
            public void reset() {
            }
        };
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        // Should not throw NullPointerException
        JsonWriteContext obj = root.createChildObjectContext();
        assertNotNull(obj);
        // subsequent operations should still work
        obj.writeFieldName("a");
        obj.writeValue();
        int status = obj.writeFieldName("b");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status);
    }
}
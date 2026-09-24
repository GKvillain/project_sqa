package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonStreamContext;

public class JsonWriteContextTest {

    // Tests initialization and properties of root context
    @Test
    public void testCreateRootContext_default_initializesCorrectly() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getTypeDesc().charAt(0) == '/' ? JsonStreamContext.TYPE_ROOT : root.getEntryCount());
        assertTrue(root.inRoot());
        assertNull(root.getParent());
        assertNull(root.getCurrentName());
        assertNull(root.getCurrentValue());
        assertNull(root.getDupDetector());
        assertEquals(-1, root.getCurrentIndex());
        assertEquals(0, root.getEntryCount());
        assertEquals("/", root.toString());
    }

    // Tests root context writeValue status transitions
    @Test
    public void testWriteValue_inRootContext_returnsOkAsIsThenSpace() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);

        int status1 = root.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status1);
        assertEquals(0, root.getCurrentIndex());
        assertEquals(1, root.getEntryCount());

        int status2 = root.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, status2);
        assertEquals(1, root.getCurrentIndex());
        assertEquals(2, root.getEntryCount());
    }

    // Tests array child context creation and value transitions
    @Test
    public void testWriteValue_inArrayContext_returnsOkAsIsThenComma() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext array = root.createChildArrayContext();

        assertTrue(array.inArray());
        assertSame(root, array.getParent());
        assertEquals("[-1]", array.toString());

        int status1 = array.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status1);
        assertEquals(0, array.getCurrentIndex());
        assertEquals("[0]", array.toString());

        int status2 = array.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status2);
        assertEquals(1, array.getCurrentIndex());
        assertEquals("[1]", array.toString());
    }

    // Tests object child context field write and value transitions
    @Test
    public void testWriteFieldNameAndValue_inObjectContext_returnsCorrectStatuses() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext object = root.createChildObjectContext();

        assertTrue(object.inObject());
        assertSame(root, object.getParent());
        assertEquals("{?}", object.toString());

        int nameStatus1 = object.writeFieldName("fieldA");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, nameStatus1);
        assertEquals("fieldA", object.getCurrentName());
        assertEquals("{\"fieldA\"}", object.toString());

        int valueStatus1 = object.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, valueStatus1);
        assertEquals(0, object.getCurrentIndex());

        int nameStatus2 = object.writeFieldName("fieldB");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, nameStatus2);
        assertEquals("fieldB", object.getCurrentName());

        int valueStatus2 = object.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, valueStatus2);
        assertEquals(1, object.getCurrentIndex());
    }

    // Tests writing field name consecutively without value returns STATUS_EXPECT_VALUE
    @Test
    public void testWriteFieldName_consecutiveCallsWithoutValue_returnsExpectValue() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext object = root.createChildObjectContext();

        int first = object.writeFieldName("first");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, first);

        int second = object.writeFieldName("second");
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, second);
    }

    // Tests context reuse and reset for child array and object contexts
    @Test
    public void testCreateChildContext_reuseInstance_resetsStateProperly() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        
        JsonWriteContext child1 = root.createChildArrayContext();
        child1.writeValue();
        child1.setCurrentValue("testValue");
        assertEquals(0, child1.getCurrentIndex());
        assertEquals("testValue", child1.getCurrentValue());

        // Recreating child array context reuses child instance
        JsonWriteContext reusedArray = root.createChildArrayContext();
        assertSame(child1, reusedArray);
        assertTrue(reusedArray.inArray());
        assertEquals(-1, reusedArray.getCurrentIndex());
        assertNull(reusedArray.getCurrentValue());
        assertNull(reusedArray.getCurrentName());

        // Recreating child object context reuses child instance
        JsonWriteContext reusedObject = root.createChildObjectContext();
        assertSame(child1, reusedObject);
        assertTrue(reusedObject.inObject());
        assertEquals(-1, reusedObject.getCurrentIndex());
        assertNull(reusedObject.getCurrentName());
    }

    // Tests getter and setter for currentValue
    @Test
    public void testCurrentValue_getAndSet_storesCorrectObject() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Object obj = new Object();
        root.setCurrentValue(obj);
        assertSame(obj, root.getCurrentValue());
    }

    // Tests duplicate field detection in object context throwing exception
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_duplicateFieldWithDupDetector_throwsException() throws Exception {
        DupDetector dd = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext object = root.createChildObjectContext();

        object.writeFieldName("dupField");
        object.writeValue();
        object.writeFieldName("dupField");
    }

    // Tests duplicate detector reset across reuse
    @Test
    public void testWithDupDetector_setter_updatesDetector() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        assertNull(root.getDupDetector());

        DupDetector dd = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        root.withDupDetector(dd);
        assertSame(dd, root.getDupDetector());
    }

    // Tests reset method resets all internal fields
    @Test
    public void testReset_customType_resetsAllFields() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext object = root.createChildObjectContext();
        object.writeFieldName("prop");
        object.setCurrentValue("value");

        object.reset(JsonStreamContext.TYPE_ROOT);
        assertTrue(object.inRoot());
        assertEquals(-1, object.getCurrentIndex());
        assertNull(object.getCurrentName());
        assertNull(object.getCurrentValue());
    }

    // Tests createChildArrayContext with initial currentValue
    @Test
    public void testCreateChildArrayContext_withCurrentValue_setsInitialValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Object customVal = new Object();
        JsonWriteContext array = root.createChildArrayContext(customVal);

        assertTrue(array.inArray());
        assertSame(customVal, array.getCurrentValue());
        assertSame(root, array.getParent());
    }

    // Tests createChildObjectContext with initial currentValue
    @Test
    public void testCreateChildObjectContext_withCurrentValue_setsInitialValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Object customVal = new Object();
        JsonWriteContext object = root.createChildObjectContext(customVal);

        assertTrue(object.inObject());
        assertSame(customVal, object.getCurrentValue());
        assertSame(root, object.getParent());
    }

    // Tests hasCurrentName behavior across different states
    @Test
    public void testHasCurrentName_inDifferentContexts() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        assertFalse(root.hasCurrentName());

        JsonWriteContext object = root.createChildObjectContext();
        assertFalse(object.hasCurrentName());

        object.writeFieldName("prop");
        assertTrue(object.hasCurrentName());

        object.writeValue();
        assertFalse(object.hasCurrentName());
    }

    // Tests writeFieldName in Root and Array contexts returns STATUS_EXPECT_VALUE
    @Test
    public void testWriteFieldName_inRootAndArrayContexts_returnsExpectValue() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, root.writeFieldName("field"));

        JsonWriteContext array = root.createChildArrayContext();
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, array.writeFieldName("field"));
    }

    // Tests clearAndGetParent clears current value and returns parent
    @Test
    public void testClearAndGetParent_clearsCurrentValueAndReturnsParent() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext child = root.createChildObjectContext("childVal");
        assertEquals("childVal", child.getCurrentValue());

        JsonWriteContext parent = child.clearAndGetParent();
        assertSame(root, parent);
        assertNull(child.getCurrentValue());

        JsonWriteContext rootParent = root.clearAndGetParent();
        assertNull(rootParent);
    }

    // Tests reset with custom type and currentValue overload
    @Test
    public void testReset_withTypeAndCurrentValue_initializesProperly() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Object customVal = "newVal";

        root.reset(JsonStreamContext.TYPE_ARRAY, customVal);
        assertTrue(root.inArray());
        assertSame(customVal, root.getCurrentValue());
        assertEquals(-1, root.getCurrentIndex());
        assertNull(root.getCurrentName());
    }

    // Tests DupDetector propagation to child array and object contexts
    @Test
    public void testDupDetector_propagatesToChildContexts() {
        DupDetector dd = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        assertNotNull(root.getDupDetector());

        JsonWriteContext array = root.createChildArrayContext();
        assertNotNull(array.getDupDetector());
        assertNotSame(root.getDupDetector(), array.getDupDetector());

        JsonWriteContext object = array.createChildObjectContext();
        assertNotNull(object.getDupDetector());
        assertNotSame(array.getDupDetector(), object.getDupDetector());
    }
}
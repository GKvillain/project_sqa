package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class JsonTreeWriterTest {

    // Tests normal array construction
    @Test
    public void testBeginArray_writeValues_createsJsonArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.value(3);
        writer.endArray();
        JsonElement result = writer.get();
        assertTrue(result.isJsonArray());
        JsonArray array = result.getAsJsonArray();
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals(2, array.get(1).getAsInt());
        assertEquals(3, array.get(2).getAsInt());
    }

    // Tests nested arrays
    @Test
    public void testBeginArray_nestedArrays_createsNestedStructure() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.beginArray();
        writer.value("inner");
        writer.endArray();
        writer.endArray();
        JsonElement result = writer.get();
        assertTrue(result.isJsonArray());
        JsonArray outer = result.getAsJsonArray();
        assertEquals(1, outer.size());
        assertTrue(outer.get(0).isJsonArray());
        assertEquals("inner", outer.get(0).getAsJsonArray().get(0).getAsString());
    }

    // Tests normal object construction
    @Test
    public void testBeginObject_writeValues_createsJsonObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("name").value("John");
        writer.name("age").value(30);
        writer.endObject();
        JsonElement result = writer.get();
        assertTrue(result.isJsonObject());
        JsonObject object = result.getAsJsonObject();
        assertEquals("John", object.get("name").getAsString());
        assertEquals(30, object.get("age").getAsInt());
    }

    // Tests object with null value when serializeNulls is false (default)
    @Test
    public void testName_nullValue_skipsNullInObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key").nullValue();
        writer.endObject();
        JsonElement result = writer.get();
        JsonObject object = result.getAsJsonObject();
        assertFalse(object.has("key"));
        assertEquals(0, object.entrySet().size());
    }

    // Tests object with null value when serializeNulls is true
    @Test
    public void testName_nullValueWithSerializeNulls_includesNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("key").nullValue();
        writer.endObject();
        JsonElement result = writer.get();
        JsonObject object = result.getAsJsonObject();
        assertTrue(object.has("key"));
        assertTrue(object.get("key").isJsonNull());
    }

    // Tests writing null string value
    @Test
    public void testValue_nullString_createsJsonNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((String) null);
        JsonElement result = writer.get();
        assertEquals(JsonNull.INSTANCE, result);
    }

    // Tests writing null number value
    @Test
    public void testValue_nullNumber_createsJsonNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) null);
        JsonElement result = writer.get();
        assertEquals(JsonNull.INSTANCE, result);
    }

    // Tests writing boolean values
    @Test
    public void testValue_booleanValues_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(true);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertTrue(result.getAsBoolean());
    }

    // Tests writing double value
    @Test
    public void testValue_doubleValue_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(3.14);
        JsonElement result = writer.get();
        assertEquals(3.14, result.getAsDouble(), 0.0001);
    }

    // Tests writing NaN double in strict mode
    @Test(expected = IllegalArgumentException.class)
    public void testValue_nanDouble_strictMode_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NaN);
    }

    // Tests writing infinity double in strict mode
    @Test(expected = IllegalArgumentException.class)
    public void testValue_infinityDouble_strictMode_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.POSITIVE_INFINITY);
    }

    // Tests writing NaN double in lenient mode
    @Test
    public void testValue_nanDouble_lenientMode_acceptsValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NaN);
        JsonElement result = writer.get();
        assertTrue(Double.isNaN(result.getAsDouble()));
    }

    // Tests writing long value
    @Test
    public void testValue_longValue_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(123456789L);
        JsonElement result = writer.get();
        assertEquals(123456789L, result.getAsLong());
    }

    // Tests writing number with NaN in strict mode
    @Test(expected = IllegalArgumentException.class)
    public void testValue_nanNumber_strictMode_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NaN);
    }

    // Tests writing number with infinity in strict mode
    @Test(expected = IllegalArgumentException.class)
    public void testValue_infinityNumber_strictMode_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.POSITIVE_INFINITY);
    }

    // Tests writing number with NaN in lenient mode
    @Test
    public void testValue_nanNumber_lenientMode_acceptsValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NaN);
        JsonElement result = writer.get();
        assertTrue(Double.isNaN(result.getAsDouble()));
    }

    // Tests endArray without matching beginArray
    @Test(expected = IllegalStateException.class)
    public void testEndArray_noMatchingBegin_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endArray();
    }

    // Tests endObject without matching beginObject
    @Test(expected = IllegalStateException.class)
    public void testEndObject_noMatchingBegin_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endObject();
    }

    // Tests name without object context
    @Test(expected = IllegalStateException.class)
    public void testName_notInObject_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.name("key");
    }

    // Tests name when pending name exists
    @Test(expected = IllegalStateException.class)
    public void testName_pendingNameAlreadyExists_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("first");
        writer.name("second");
    }

    // Tests value with pending name (normal object flow)
    @Test
    public void testPut_valueWithPendingName_addsToObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key");
        writer.value("value");
        writer.endObject();
        JsonObject result = writer.get().getAsJsonObject();
        assertEquals("value", result.get("key").getAsString());
    }

    // Tests put with non-array parent
    @Test(expected = IllegalStateException.class)
    public void testPut_nonArrayParent_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.value("value");
    }

    // Tests close on incomplete document
    @Test(expected = IOException.class)
    public void testClose_incompleteDocument_throwsIOException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.close();
    }

    // Tests close on complete document
    @Test
    public void testClose_completeDocument_succeeds() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("test");
        writer.close();
        // Verify that operations after close fail
        try {
            writer.value("another");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected - sentinel closed prevents further operations
        }
    }

    // Tests get with non-empty stack
    @Test(expected = IllegalStateException.class)
    public void testGet_nonEmptyStack_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.get();
    }

    // Tests get with empty stack returns product
    @Test
    public void testGet_emptyStack_returnsProduct() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(42);
        JsonElement result = writer.get();
        assertEquals(42, result.getAsInt());
    }

    // Tests flush operation (no-op)
    @Test
    public void testFlush_noException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("test");
        writer.flush();
        JsonElement result = writer.get();
        assertEquals("test", result.getAsString());
    }

    // Tests primitive string value
    @Test
    public void testValue_string_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("hello");
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals("hello", result.getAsString());
    }

    // Tests empty string in array
    @Test
    public void testValue_emptyStringInArray_addsElement() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value("");
        writer.endArray();
        JsonArray result = writer.get().getAsJsonArray();
        assertEquals(1, result.size());
        assertEquals("", result.get(0).getAsString());
    }

    // Tests zero value
    @Test
    public void testValue_zero_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(0);
        JsonElement result = writer.get();
        assertEquals(0, result.getAsInt());
    }

    // Tests negative value
    @Test
    public void testValue_negativeValue_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(-42);
        JsonElement result = writer.get();
        assertEquals(-42, result.getAsInt());
    }

    // Tests min integer value
    @Test
    public void testValue_minInteger_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Integer.MIN_VALUE);
        JsonElement result = writer.get();
        assertEquals(Integer.MIN_VALUE, result.getAsInt());
    }

    // Tests max integer value
    @Test
    public void testValue_maxInteger_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Integer.MAX_VALUE);
        JsonElement result = writer.get();
        assertEquals(Integer.MAX_VALUE, result.getAsInt());
    }

    // Tests min long value
    @Test
    public void testValue_minLong_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Long.MIN_VALUE);
        JsonElement result = writer.get();
        assertEquals(Long.MIN_VALUE, result.getAsLong());
    }

    // Tests max long value
    @Test
    public void testValue_maxLong_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Long.MAX_VALUE);
        JsonElement result = writer.get();
        assertEquals(Long.MAX_VALUE, result.getAsLong());
    }

    // Tests min double value
    @Test
    public void testValue_minDouble_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.MIN_VALUE);
        JsonElement result = writer.get();
        assertEquals(Double.MIN_VALUE, result.getAsDouble(), 0.0);
    }

    // Tests max double value
    @Test
    public void testValue_maxDouble_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.MAX_VALUE);
        JsonElement result = writer.get();
        assertEquals(Double.MAX_VALUE, result.getAsDouble(), 0.0);
    }

    // ==================== Additional test cases for coverage ====================

    // Tests boolean false
    @Test
    public void testValue_booleanFalse_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(false);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertFalse(result.getAsBoolean());
    }

    // Tests float value
    @Test
    public void testValue_floatNumber_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(1.5f);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(1.5f, result.getAsFloat(), 0.0f);
    }

    // Tests short value
    @Test
    public void testValue_shortNumber_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((short) 1);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(1, result.getAsShort());
    }

    // Tests byte value
    @Test
    public void testValue_byteNumber_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((byte) 1);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(1, result.getAsByte());
    }

    // Tests BigDecimal value
    @Test
    public void testValue_bigDecimal_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new BigDecimal("123.456"));
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(new BigDecimal("123.456"), result.getAsBigDecimal());
    }

    // Tests BigInteger value
    @Test
    public void testValue_bigInteger_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new BigInteger("9999999999999999999"));
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(new BigInteger("9999999999999999999"), result.getAsBigInteger());
    }

    // Tests null value inside array
    @Test
    public void testValue_nullInArray_addsJsonNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(1, array.size());
        assertTrue(array.get(0).isJsonNull());
    }

    // Tests nullValue inside array
    @Test
    public void testNullValue_inArray_addsJsonNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.nullValue();
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(1, array.size());
        assertTrue(array.get(0).isJsonNull());
    }

    // Tests lenient mode with positive infinity
    @Test
    public void testValue_lenientPositiveInfinity_acceptsValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.POSITIVE_INFINITY);
        JsonElement result = writer.get();
        assertEquals(Double.POSITIVE_INFINITY, result.getAsDouble(), 0.0);
    }

    // Tests lenient mode with negative infinity
    @Test
    public void testValue_lenientNegativeInfinity_acceptsValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NEGATIVE_INFINITY);
        JsonElement result = writer.get();
        assertEquals(Double.NEGATIVE_INFINITY, result.getAsDouble(), 0.0);
    }

    // Tests null name throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testName_nullName_throwsNullPointerException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name(null);
    }

    // Tests negative zero double
    @Test
    public void testValue_negativeZeroDouble_createsJsonPrimitive() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(-0.0);
        JsonElement result = writer.get();
        assertTrue(result.isJsonPrimitive());
        assertEquals(-0.0, result.getAsDouble(), 0.0);
        // Ensure it's actually negative zero
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(result.getAsDouble()));
    }

    // Tests get after close on complete document
    @Test
    public void testGet_afterClose_returnsProduct() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(42);
        writer.close();
        JsonElement result = writer.get();
        assertEquals(42, result.getAsInt());
    }

    // Tests that beginArray after close throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testBeginArray_afterClose_throwsException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("done");
        writer.close();
        writer.beginArray();
    }

    // Tests toggle serializeNulls
    @Test
    public void testSetSerializeNulls_toggle_affectsObjectNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("nullKey").nullValue();
        writer.endObject();
        JsonObject obj1 = writer.get().getAsJsonObject();
        assertFalse(obj1.has("nullKey"));

        // Reset writer and toggle to true
        writer = new JsonTreeWriter();
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("nullKey").nullValue();
        writer.endObject();
        JsonObject obj2 = writer.get().getAsJsonObject();
        assertTrue(obj2.has("nullKey"));
        assertTrue(obj2.get("nullKey").isJsonNull());
    }
}
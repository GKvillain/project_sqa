package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class JsonTreeReaderTest {

    // ==================== Existing tests ====================

    // Tests skipValue after endArray to verify pathIndices increment and no crash
    @Test
    public void testSkipValue_afterEndArray_pathUpdated() throws IOException {
        JsonArray array = new JsonArray();
        array.add(1);
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        reader.skipValue(); // skip 1
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    // Tests skipValue for a primitive to verify pathIndices increment
    @Test
    public void testSkipValue_primitive_incrementsPathIndex() throws IOException {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        String pathBefore = reader.getPath();
        reader.skipValue();
        String pathAfter = reader.getPath();
        // After skipping first element, index should be 1 at path $[1]
        assertEquals("$[1]", pathAfter);
        reader.skipValue();
        reader.endArray();
    }

    // Tests skipValue for a nested object name to verify pathNames update
    @Test
    public void testSkipValue_nestedObjectName_pathNamesUpdated() throws IOException {
        JsonObject obj = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("key", "value");
        obj.add("outer", inner);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.nextName(); // outer
        reader.beginObject();
        reader.skipValue(); // skip "key":"value"
        // After skipping name, pathNames at outer scope should have "null"
        String path = reader.getPath();
        assertEquals("$['outer']['null']", path); // Defects4J bug expected path
        reader.endObject();
        reader.endObject();
    }

    // Tests skipValue for a name at top level to verify pathNames update
    @Test
    public void testSkipValue_topLevelName_pathNamesUpdated() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.skipValue(); // skip name "a"
        // After skipping name, pathNames[0] should have "null"
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
    }

    // ==================== New tests for uncovered scenarios ====================

    // Tests skipValue for a JsonNull element inside an array
    @Test
    public void testSkipValue_nullInArray_incrementsPathIndex() throws IOException {
        JsonArray array = new JsonArray();
        array.add(JsonNull.INSTANCE);
        array.add(42);
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        assertEquals(JsonToken.NULL, reader.peek());
        reader.skipValue();
        // After skipping the null, path should point to index 1
        assertEquals("$[1]", reader.getPath());
        assertEquals(JsonToken.NUMBER, reader.peek()); // next element is 42
        reader.skipValue();
        reader.endArray();
    }

    // Tests skipValue for a nested array (skips the entire inner array)
    @Test
    public void testSkipValue_nestedArray_skipsInnerArray() throws IOException {
        JsonArray outer = new JsonArray();
        JsonArray inner = new JsonArray();
        inner.add(1);
        inner.add(2);
        outer.add(inner);
        outer.add("tail");
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginArray();
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.skipValue(); // skip the inner array entirely
        // After skipping, path index should be 1
        assertEquals("$[1]", reader.getPath());
        assertEquals(JsonToken.STRING, reader.peek()); // next element is "tail"
        reader.skipValue();
        reader.endArray();
    }

    // Tests skipValue for a nested object (skips the entire inner object)
    @Test
    public void testSkipValue_nestedObject_skipsInnerObject() throws IOException {
        JsonObject outer = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("x", true);
        outer.add("obj", inner);
        outer.addProperty("after", "done");
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginObject();
        reader.nextName(); // "obj"
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.skipValue(); // skip the inner object
        // After skipping, pathNames should have "obj" (name) then "null" (value slot)
        assertEquals("$['obj']['null']", reader.getPath());
        assertEquals(JsonToken.NAME, reader.peek()); // next name is "after"
        reader.nextName(); // "after"
        reader.skipValue(); // skip "done"
        reader.endObject();
    }

    // Tests skipValue after a nextName() call (skipping the value of a name-value pair)
    @Test
    public void testSkipValue_afterNextName_skipsValue() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("alpha", 100);
        obj.addProperty("beta", "test");
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.nextName(); // "alpha"
        reader.skipValue(); // skip value 100
        // After skipping, path should have "null" for the value slot
        assertEquals("$['alpha']['null']", reader.getPath());
        assertEquals(JsonToken.NAME, reader.peek()); // next name is "beta"
        reader.nextName(); // "beta"
        reader.skipValue(); // skip "test"
        reader.endObject();
    }

    // Tests that skipValue throws IOException when called at illegal positions
    @Test(expected = IOException.class)
    public void testSkipValue_atEndArray_throwsIOException() throws IOException {
        JsonArray array = new JsonArray();
        array.add(1);
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        reader.skipValue(); // skip 1
        reader.endArray();
        // Now at END_ARRAY, skipValue should throw
        reader.skipValue();
    }

    @Test(expected = IOException.class)
    public void testSkipValue_atEndObject_throwsIOException() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.nextName();
        reader.skipValue(); // skip value 1
        reader.endObject();
        // Now at END_OBJECT, skipValue should throw
        reader.skipValue();
    }

    @Test(expected = IOException.class)
    public void testSkipValue_atEndDocument_throwsIOException() throws IOException {
        JsonArray array = new JsonArray();
        array.add(true);
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        reader.skipValue();
        reader.endArray();
        // Now at END_DOCUMENT, skipValue should throw
        reader.skipValue();
    }

    // Tests that path indices are correctly updated after multiple skips in an array
    @Test
    public void testPathIndices_afterMultipleSkipsInArray() throws IOException {
        JsonArray array = new JsonArray();
        array.add("a");
        array.add("b");
        array.add("c");
        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        reader.skipValue(); // skip "a", index -> 1
        assertEquals("$[1]", reader.getPath());
        reader.skipValue(); // skip "b", index -> 2
        assertEquals("$[2]", reader.getPath());
        reader.skipValue(); // skip "c", index -> 3
        assertEquals("$[3]", reader.getPath());
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
    }
}
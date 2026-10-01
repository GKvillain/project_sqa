package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.NopIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.FixedSpaceIndenter;

public class DefaultPrettyPrinterTest {

    // Helper functional interface to write JSON content
    @FunctionalInterface
    private interface JsonWriter {
        void write(JsonGenerator g) throws IOException;
    }

    // Helper method to generate JSON string using given printer and writer
    private String jsonString(DefaultPrettyPrinter pp, JsonWriter writer) throws IOException {
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonGenerator g = factory.createGenerator(os);
        g.setPrettyPrinter(pp);
        writer.write(g);
        g.close();
        return os.toString("UTF-8");
    }

    // ============================================================
    // Normal/Boundary tests
    // ============================================================

    // Tests default object with one field: spaces around colon, newlines
    @Test
    public void testObjectWithField_DefaultSettings_UsesSpacesAroundColonAndNewlines() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartObject();
            g.writeFieldName("key");
            g.writeString("value");
            g.writeEndObject();
        });
        String ls = System.lineSeparator();
        String expected = "{" + ls +
                          "  \"key\" : \"value\"" + ls +
                          "}";
        assertEquals(expected, json);
    }

    // Tests object without spaces in object entries: colon without spaces
    @Test
    public void testObjectWithoutSpacesInEntries_ColonWithoutSpaces() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();
        String json = jsonString(pp, g -> {
            g.writeStartObject();
            g.writeFieldName("key");
            g.writeString("value");
            g.writeEndObject();
        });
        String ls = System.lineSeparator();
        String expected = "{" + ls +
                          "  \"key\":\"value\"" + ls +
                          "}";
        assertEquals(expected, json);
    }

    // Tests empty object output (expected to be "{}")
    // This test detects the defect in empty object handling (space before closing brace)
    @Test
    public void testEmptyObject_OutputIsEmptyBraces() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartObject();
            g.writeEndObject();
        });
        assertEquals("{}", json);
    }

    // Tests empty array output (expected to be "[]")
    @Test
    public void testEmptyArray_OutputIsEmptyBrackets() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartArray();
            g.writeEndArray();
        });
        assertEquals("[]", json);
    }

    // Tests array with values (default fixed-space indenter)
    @Test
    public void testArrayWithValues_FixedSpaceIndenter() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartArray();
            g.writeNumber(1);
            g.writeNumber(2);
            g.writeEndArray();
        });
        assertEquals("[ 1, 2 ]", json);
    }

    // Tests nested objects correct indentation levels
    @Test
    public void testNestedObjectsNestingIndentationCorrect() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartObject();
            g.writeFieldName("outer");
            g.writeStartObject();
            g.writeFieldName("inner");
            g.writeString("value");
            g.writeEndObject();
            g.writeEndObject();
        });
        String ls = System.lineSeparator();
        String expected = "{" + ls +
                          "  \"outer\" : {" + ls +
                          "    \"inner\" : \"value\"" + ls +
                          "  }" + ls +
                          "}";
        assertEquals(expected, json);
    }

    // Tests root separator default (space)
    @Test
    public void testRootSeparatorDefault_SpacePrintedBetweenRoots() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        String json = jsonString(pp, g -> {
            g.writeStartObject(); g.writeEndObject();
            g.writeStartObject(); g.writeEndObject();
        });
        assertEquals("{} {}", json);
    }

    // Tests root separator null (no separator)
    @Test
    public void testRootSeparatorNull_NoSeparatorBetweenRoots() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter((String) null);
        String json = jsonString(pp, g -> {
            g.writeStartObject(); g.writeEndObject();
            g.writeStartObject(); g.writeEndObject();
        });
        assertEquals("{}{}", json);
    }

    // Tests custom root separator string
    @Test
    public void testRootSeparatorCustomString_UsedBetweenRoots() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("---");
        String json = jsonString(pp, g -> {
            g.writeStartObject(); g.writeEndObject();
            g.writeStartObject(); g.writeEndObject();
        });
        assertEquals("{}---{}", json);
    }

    // Tests indentArraysWith NopIndenter produces no spaces inside array
    @Test
    public void testIndentArraysWithNopIndenter_NoSpacesInsideArray() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(NopIndenter.instance);
        String json = jsonString(pp, g -> {
            g.writeStartArray();
            g.writeNumber(1);
            g.writeNumber(2);
            g.writeEndArray();
        });
        assertEquals("[1,2]", json);
    }

    // Tests indentObjectsWith FixedSpaceIndenter produces spaces inside object without newlines
    @Test
    public void testIndentObjectsWithFixedSpaceIndenter_SpacesInsideObjectNoNewlines() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(FixedSpaceIndenter.instance);
        String json = jsonString(pp, g -> {
            g.writeStartObject();
            g.writeFieldName("key");
            g.writeString("value");
            g.writeEndObject();
        });
        assertEquals("{ \"key\" : \"value\" }", json);
    }

    // ============================================================
    // Factory / immutability tests
    // ============================================================

    // Tests withArrayIndenter(null) sets indenter to NopIndenter and returns new instance
    @Test
    public void testWithArrayIndenterNull_SetsNopIndenter() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter pp2 = pp.withArrayIndenter(null);
        // original unchanged, pp2 uses NopIndenter
        String defaultOut = jsonString(pp, g -> {
            g.writeStartArray(); g.writeNumber(1); g.writeNumber(2); g.writeEndArray();
        });
        assertEquals("[ 1, 2 ]", defaultOut);
        String nopOut = jsonString(pp2, g -> {
            g.writeStartArray(); g.writeNumber(1); g.writeNumber(2); g.writeEndArray();
        });
        assertEquals("[1,2]", nopOut);
    }

    // Tests withArrayIndenter when same instance returns this
    @Test
    public void testWithArrayIndenterSameInstance_ReturnsThis() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        // default _arrayIndenter is FixedSpaceIndenter.instance
        DefaultPrettyPrinter result = pp.withArrayIndenter(FixedSpaceIndenter.instance);
        assertSame("Should return same instance when array indenter unchanged", pp, result);
    }

    // Tests withObjectIndenter when same instance returns this
    @Test
    public void testWithObjectIndenterSameInstance_ReturnsThis() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        // Change object indenter to NopIndenter first
        DefaultPrettyPrinter pp2 = pp.withObjectIndenter(NopIndenter.instance);
        // Now call with same indenter
        DefaultPrettyPrinter result = pp2.withObjectIndenter(NopIndenter.instance);
        assertSame("Should return same instance when object indenter unchanged", pp2, result);
    }

    // Tests withSpacesInObjectEntries when already true returns this
    @Test
    public void testWithSpacesInObjectEntriesAlreadyTrue_ReturnsThis() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(); // default true
        DefaultPrettyPrinter result = pp.withSpacesInObjectEntries();
        assertSame("Should return same instance when spaces already enabled", pp, result);
    }

    // Tests withoutSpacesInObjectEntries when already false returns this
    @Test
    public void testWithoutSpacesInObjectEntriesAlreadyFalse_ReturnsThis() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withoutSpacesInObjectEntries(); // false
        DefaultPrettyPrinter result = pp.withoutSpacesInObjectEntries();
        assertSame("Should return same instance when spaces already disabled", pp, result);
    }

    // Tests withSpacesInObjectEntries when state changes returns new instance
    @Test
    public void testWithSpacesInObjectEntries_ReturnsNewInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(); // true
        DefaultPrettyPrinter pp2 = pp.withoutSpacesInObjectEntries(); // false
        DefaultPrettyPrinter result = pp2.withSpacesInObjectEntries(); // true
        assertNotSame("Should return new instance when changing to true", pp2, result);
    }

    // Tests withRootSeparator same root separator returns this
    @Test
    public void testWithRootSeparatorSame_ReturnsThis() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(); // default " "
        DefaultPrettyPrinter result = pp.withRootSeparator(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        assertSame("Should return same instance when root separator unchanged", pp, result);
    }

    // Tests withRootSeparator different root separator returns new instance
    @Test
    public void testWithRootSeparatorDifferent_ReturnsNewInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(); // default " "
        DefaultPrettyPrinter changed = pp.withRootSeparator("---");
        assertNotSame("Should return new instance when root separator changed", pp, changed);
    }

    // Tests createInstance produces independent copy
    @Test
    public void testCreateInstanceCopiesSettingsAndIsIndependent() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter copy = pp.createInstance();
        // Identical output initially
        String out1 = jsonString(pp, g -> { g.writeStartObject(); g.writeEndObject(); });
        String out2 = jsonString(copy, g -> { g.writeStartObject(); g.writeEndObject(); });
        assertEquals(out1, out2);
        // Modify original
        pp.indentObjectsWith(NopIndenter.instance);
        String outOriginal = jsonString(pp, g -> {
            g.writeStartObject(); g.writeFieldName("a"); g.writeString("b"); g.writeEndObject();
        });
        String outCopy = jsonString(copy, g -> {
            g.writeStartObject(); g.writeFieldName("a"); g.writeString("b"); g.writeEndObject();
        });
        assertNotEquals("Changes to original should not affect copy", outOriginal, outCopy);
    }
}
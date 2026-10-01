package org.apache.commons.lang3.builder;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ToStringStyle}.
 */
public class ToStringStyleTest {

    private static class TestToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;
    }

    private TestToStringStyle style;
    private StringBuffer buffer;

    @Before
    public void setUp() {
        style = new TestToStringStyle();
        buffer = new StringBuffer();
    }

    @After
    public void tearDown() {
        ToStringStyle.unregister(this);
    }

    // Tests registry operations including register, isRegistered, unregister, and null handling
    @Test
    public void testRegistry_registerAndUnregister_managesRegistryCorrectly() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        assertFalse(ToStringStyle.isRegistered(obj1));
        assertFalse(ToStringStyle.isRegistered(null));

        ToStringStyle.register(null);
        assertFalse(ToStringStyle.isRegistered(null));

        ToStringStyle.register(obj1);
        assertTrue(ToStringStyle.isRegistered(obj1));
        assertFalse(ToStringStyle.isRegistered(obj2));

        ToStringStyle.register(obj2);
        assertTrue(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));

        ToStringStyle.unregister(obj1);
        assertFalse(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));

        ToStringStyle.unregister(null);
        ToStringStyle.unregister(obj2);
        assertFalse(ToStringStyle.isRegistered(obj2));
        assertTrue(ToStringStyle.getRegistry().isEmpty());
    }

    // Tests appending null values
    @Test
    public void testAppend_nullValue_appendsNullText() {
        style.append(buffer, "field", (Object) null, true);
        assertEquals("field=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "arr", (Object[]) null, true);
        assertEquals("arr=<null>,", buffer.toString());
    }

    // Tests appending simple objects with detail and summary modes
    @Test
    public void testAppend_objectDetailAndSummary_appendsExpectedFormat() {
        style.append(buffer, "name", "John", true);
        assertEquals("name=John,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "name", "John", false);
        assertEquals("name=<String>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "name", "John", null);
        assertEquals("name=John,", buffer.toString());
    }

    // Tests appending collections and maps in detail and summary modes
    @Test
    public void testAppend_collectionAndMap_appendsDetailAndSummary() {
        List<String> list = new ArrayList<String>();
        list.add("item1");
        list.add("item2");

        style.append(buffer, "list", list, true);
        assertEquals("list=[item1, item2],", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "list", list, false);
        assertEquals("list=<size=2>,", buffer.toString());

        buffer.setLength(0);
        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        style.append(buffer, "map", map, true);
        assertEquals("map={k=v},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "map", map, false);
        assertEquals("map=<size=1>,", buffer.toString());
    }

    // Tests appending primitive types
    @Test
    public void testAppend_primitives_appendsCorrectly() {
        style.append(buffer, "longVal", 100L);
        style.append(buffer, "intVal", 10);
        style.append(buffer, "shortVal", (short) 5);
        style.append(buffer, "byteVal", (byte) 1);
        style.append(buffer, "charVal", 'a');
        style.append(buffer, "doubleVal", 1.5d);
        style.append(buffer, "floatVal", 2.5f);
        style.append(buffer, "boolVal", true);

        String expected = "longVal=100,intVal=10,shortVal=5,byteVal=1,charVal=a,doubleVal=1.5,floatVal=2.5,boolVal=true,";
        assertEquals(expected, buffer.toString());
    }

    // Tests appending primitive arrays in detail and summary modes
    @Test
    public void testAppend_primitiveArrays_appendsDetailAndSummary() {
        style.append(buffer, "longs", new long[]{1L, 2L}, true);
        style.append(buffer, "ints", new int[]{3, 4}, true);
        style.append(buffer, "shorts", new short[]{5, 6}, true);
        style.append(buffer, "bytes", new byte[]{7, 8}, true);
        style.append(buffer, "chars", new char[]{'x', 'y'}, true);
        style.append(buffer, "doubles", new double[]{1.0, 2.0}, true);
        style.append(buffer, "floats", new float[]{3.0f, 4.0f}, true);
        style.append(buffer, "booleans", new boolean[]{true, false}, true);

        String expected = "longs={1,2},ints={3,4},shorts={5,6},bytes={7,8},chars={x,y},"
                + "doubles={1.0,2.0},floats={3.0,4.0},booleans={true,false},";
        assertEquals(expected, buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "intsSummary", new int[]{1, 2, 3}, false);
        style.append(buffer, "longsSummary", new long[]{1L}, false);
        style.append(buffer, "shortsSummary", new short[]{1}, false);
        style.append(buffer, "bytesSummary", new byte[]{1}, false);
        style.append(buffer, "charsSummary", new char[]{'a'}, false);
        style.append(buffer, "doublesSummary", new double[]{1.0}, false);
        style.append(buffer, "floatsSummary", new float[]{1.0f}, false);
        style.append(buffer, "booleansSummary", new boolean[]{true}, false);

        String summaryExpected = "intsSummary=<size=3>,longsSummary=<size=1>,shortsSummary=<size=1>,"
                + "bytesSummary=<size=1>,charsSummary=<size=1>,doublesSummary=<size=1>,"
                + "floatsSummary=<size=1>,booleansSummary=<size=1>,";
        assertEquals(summaryExpected, buffer.toString());
    }

    // Tests appending Object array with detail, summary, and null elements
    @Test
    public void testAppend_objectArray_appendsDetailAndSummary() {
        Object[] array = new Object[]{"A", null, "B"};
        style.append(buffer, "arr", array, true);
        assertEquals("arr={A,<null>,B},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "arr", array, false);
        assertEquals("arr=<size=3>,", buffer.toString());
    }

    // Tests cyclic object detection and formatting
    @Test
    public void testAppend_cyclicObject_handlesRecursionWithoutLoop() {
        Object cyclicObject = new Object();
        ToStringStyle.register(cyclicObject);
        try {
            style.append(buffer, "cycle", cyclicObject, true);
            String result = buffer.toString();
            assertTrue(result.startsWith("cycle="));
            assertTrue(result.contains(ObjectUtils.identityToString(cyclicObject)));
        } finally {
            ToStringStyle.unregister(cyclicObject);
        }
    }

    // Tests appendSuper and appendToString with normal and edge cases
    @Test
    public void testAppendSuperAndToString_validAndNull_extractsContent() {
        style.appendSuper(buffer, null);
        assertEquals("", buffer.toString());

        style.appendSuper(buffer, "Person[name=John]");
        assertEquals("name=John,", buffer.toString());

        style.appendToString(buffer, null);
        assertEquals("name=John,", buffer.toString());

        style.appendToString(buffer, "Person[age=30]");
        assertEquals("name=John,age=30,", buffer.toString());

        // Invalid format without delimiters
        style.appendToString(buffer, "InvalidString");
        assertEquals("name=John,age=30,", buffer.toString());
    }

    // Tests appendStart and appendEnd formatting
    @Test
    public void testAppendStartAndEnd_normalObject_generatesCorrectStructure() {
        String testObj = "TestObject";
        style.setUseIdentityHashCode(false);
        style.setUseShortClassName(true);

        style.appendStart(buffer, testObj);
        style.append(buffer, "f1", "val1", true);
        style.appendEnd(buffer, testObj);

        assertEquals("String[f1=val1]", buffer.toString());
        assertFalse(ToStringStyle.isRegistered(testObj));
    }

    // Tests removing the last field separator
    @Test
    public void testRemoveLastFieldSeparator_variousBufferStates_removesExpectedChars() {
        style.setFieldSeparator(",");

        // Buffer matching separator
        buffer.append("name=John,");
        style.removeLastFieldSeparator(buffer);
        assertEquals("name=John", buffer.toString());

        // Buffer not ending with separator
        style.removeLastFieldSeparator(buffer);
        assertEquals("name=John", buffer.toString());

        // Buffer shorter than separator
        buffer.setLength(0);
        style.setFieldSeparator("---");
        buffer.append("-");
        style.removeLastFieldSeparator(buffer);
        assertEquals("-", buffer.toString());
    }

    // Tests all setter methods with null inputs to ensure conversion to empty string
    @Test
    public void testSetters_nullInputs_convertedToEmptyString() {
        style.setArrayStart(null);
        assertEquals("", style.getArrayStart());

        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd());

        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator());

        style.setContentStart(null);
        assertEquals("", style.getContentStart());

        style.setContentEnd(null);
        assertEquals("", style.getContentEnd());

        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator());

        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator());

        style.setNullText(null);
        assertEquals("", style.getNullText());

        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText());

        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText());

        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());

        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText());
    }

    // Tests predefined ToStringStyle singletons
    @Test
    public void testPredefinedStyles_configurations_verifyBehavior() {
        ToStringStyle defaultStyle = ToStringStyle.DEFAULT_STYLE;
        assertTrue(defaultStyle.isUseClassName());
        assertTrue(defaultStyle.isUseFieldNames());
        assertTrue(defaultStyle.isUseIdentityHashCode());

        ToStringStyle noFieldNames = ToStringStyle.NO_FIELD_NAMES_STYLE;
        assertFalse(noFieldNames.isUseFieldNames());

        ToStringStyle shortPrefix = ToStringStyle.SHORT_PREFIX_STYLE;
        assertTrue(shortPrefix.isUseShortClassName());
        assertFalse(shortPrefix.isUseIdentityHashCode());

        ToStringStyle simple = ToStringStyle.SIMPLE_STYLE;
        assertFalse(simple.isUseClassName());
        assertFalse(simple.isUseFieldNames());
        assertFalse(simple.isUseIdentityHashCode());
        assertEquals("", simple.getContentStart());
        assertEquals("", simple.getContentEnd());

        ToStringStyle multiLine = ToStringStyle.MULTI_LINE_STYLE;
        assertTrue(multiLine.isFieldSeparatorAtStart());
    }

    // Tests reflectionAppendArrayDetail method
    @Test
    public void testReflectionAppendArrayDetail_arrayObject_appendsDetail() {
        int[] intArray = new int[]{10, 20};
        style.reflectionAppendArrayDetail(buffer, "intArray", intArray);
        assertEquals("{10,20}", buffer.toString());

        buffer.setLength(0);
        Object[] objArray = new Object[]{"a", null};
        style.reflectionAppendArrayDetail(buffer, "objArray", objArray);
        assertEquals("{a,<null>}", buffer.toString());
    }

    // Tests primitive array null handling with append methods
    @Test
    public void testAppend_primitiveArraysNull_appendsNullText() {
        style.append(buffer, "bNull", (boolean[]) null, true);
        style.append(buffer, "byNull", (byte[]) null, true);
        style.append(buffer, "cNull", (char[]) null, true);
        style.append(buffer, "sNull", (short[]) null, true);
        style.append(buffer, "iNull", (int[]) null, true);
        style.append(buffer, "lNull", (long[]) null, true);
        style.append(buffer, "fNull", (float[]) null, true);
        style.append(buffer, "dNull", (double[]) null, true);

        String expected = "bNull=<null>,byNull=<null>,cNull=<null>,sNull=<null>,"
                + "iNull=<null>,lNull=<null>,fNull=<null>,dNull=<null>,";
        assertEquals(expected, buffer.toString());
    }

    // Tests default full detail configuration and boolean getters/setters
    @Test
    public void testStyleConfiguration_flagsAndDefaults() {
        style.setDefaultFullDetail(false);
        assertFalse(style.isDefaultFullDetail());

        style.setArrayContentDetail(false);
        assertFalse(style.isArrayContentDetail());

        style.setFieldSeparatorAtStart(true);
        assertTrue(style.isFieldSeparatorAtStart());

        style.setFieldSeparatorAtEnd(true);
        assertTrue(style.isFieldSeparatorAtEnd());

        style.setUseClassName(false);
        assertFalse(style.isUseClassName());

        style.setUseFieldNames(false);
        assertFalse(style.isUseFieldNames());
    }

    // Tests appendStart and appendEnd with null object and custom separator placements
    @Test
    public void testAppendStartAndEnd_nullObjectAndFieldSeparatorPlacements() {
        style.setFieldSeparatorAtStart(true);
        style.setFieldSeparatorAtEnd(true);
        style.appendStart(buffer, null);
        style.append(buffer, "k", "v", true);
        style.appendEnd(buffer, null);

        assertEquals(",[k=v,]", buffer.toString());
    }

    // Tests appendSuper and appendToString with missing end delimiter
    @Test
    public void testAppendSuperAndToString_missingEndDelimiter() {
        style.appendSuper(buffer, "Prefix[field=value");
        assertEquals("", buffer.toString());

        style.appendToString(buffer, "Prefix[field=value");
        assertEquals("", buffer.toString());
    }

    // Tests removeLastFieldSeparator when buffer is null, empty, or fieldSeparator is empty
    @Test
    public void testRemoveLastFieldSeparator_edgeCases() {
        style.removeLastFieldSeparator(null);

        buffer.setLength(0);
        style.removeLastFieldSeparator(buffer);
        assertEquals("", buffer.toString());

        style.setFieldSeparator("");
        buffer.append("test");
        style.removeLastFieldSeparator(buffer);
        assertEquals("test", buffer.toString());
    }
}
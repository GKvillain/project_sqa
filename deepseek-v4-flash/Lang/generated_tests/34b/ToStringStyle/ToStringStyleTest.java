package org.apache.commons.lang3.builder;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

public class ToStringStyleTest {

    private static String identity(Object obj) {
        return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }

    private static class TestStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        TestStyle() {
            super();
        }

        TestStyle useFieldNames(boolean value) { super.setUseFieldNames(value); return this; }
        TestStyle useClassName(boolean value) { super.setUseClassName(value); return this; }
        TestStyle useShortClassName(boolean value) { super.setUseShortClassName(value); return this; }
        TestStyle useIdentityHashCode(boolean value) { super.setUseIdentityHashCode(value); return this; }
        TestStyle defaultFullDetail(boolean value) { super.setDefaultFullDetail(value); return this; }
        TestStyle arrayContentDetail(boolean value) { super.setArrayContentDetail(value); return this; }
        TestStyle arrayStart(String value) { super.setArrayStart(value); return this; }
        TestStyle arrayEnd(String value) { super.setArrayEnd(value); return this; }
        TestStyle arraySeparator(String value) { super.setArraySeparator(value); return this; }
        TestStyle contentStart(String value) { super.setContentStart(value); return this; }
        TestStyle contentEnd(String value) { super.setContentEnd(value); return this; }
        TestStyle fieldNameValueSeparator(String value) { super.setFieldNameValueSeparator(value); return this; }
        TestStyle fieldSeparator(String value) { super.setFieldSeparator(value); return this; }
        TestStyle fieldSeparatorAtStart(boolean value) { super.setFieldSeparatorAtStart(value); return this; }
        TestStyle fieldSeparatorAtEnd(boolean value) { super.setFieldSeparatorAtEnd(value); return this; }
        TestStyle nullText(String value) { super.setNullText(value); return this; }
        TestStyle sizeStartText(String value) { super.setSizeStartText(value); return this; }
        TestStyle sizeEndText(String value) { super.setSizeEndText(value); return this; }
        TestStyle summaryObjectStartText(String value) { super.setSummaryObjectStartText(value); return this; }
        TestStyle summaryObjectEndText(String value) { super.setSummaryObjectEndText(value); return this; }

        void callAppendInternal(StringBuffer buffer, String fieldName, Object value, boolean detail) {
            super.appendInternal(buffer, fieldName, value, detail);
        }

        void callReflectionAppendArrayDetail(StringBuffer buffer, String fieldName, Object array) {
            super.reflectionAppendArrayDetail(buffer, fieldName, array);
        }

        void callRemoveLastFieldSeparator(StringBuffer buffer) {
            super.removeLastFieldSeparator(buffer);
        }

        boolean callIsFullDetail(Boolean fullDetailRequest) {
            return super.isFullDetail(fullDetailRequest);
        }
    }

    // Tests appendStart with default style containing class name, identity hash, and content start
    @Test
    public void testAppendStart_defaultStyle_containsClassNameIdentityAndContentStart() {
        Object obj = new Object();
        StringBuffer buffer = new StringBuffer();
        try {
            ToStringStyle.DEFAULT_STYLE.appendStart(buffer, obj);
            assertEquals(identity(obj) + "[", buffer.toString());
        } finally {
            ToStringStyle.unregister(obj);
        }
    }

    // Tests appendStart with SIMPLE_STYLE which suppresses prefix output
    @Test
    public void testAppendStart_simpleStyle_doesNotOutputPrefix() {
        StringBuffer buffer = new StringBuffer();
        ToStringStyle.SIMPLE_STYLE.appendStart(buffer, new Object());
        assertEquals("", buffer.toString());
    }

    // Tests appendStart when fieldSeparatorAtStart is true
    @Test
    public void testAppendStart_fieldSeparatorAtStart_appendsFieldSeparator() {
        Object obj = new Object();
        TestStyle style = new TestStyle()
            .contentStart("[")
            .contentEnd("]")
            .fieldSeparator(",")
            .fieldSeparatorAtStart(true);
        StringBuffer buffer = new StringBuffer();
        try {
            style.appendStart(buffer, obj);
            assertEquals(identity(obj) + "[,", buffer.toString());
        } finally {
            ToStringStyle.unregister(obj);
        }
    }

    // Tests appendEnd removing the last field separator and unregistering the object
    @Test
    public void testAppendEnd_removesLastFieldSeparatorAndUnregisters() {
        TestStyle style = new TestStyle().fieldSeparator(",").contentEnd("]");
        Object obj = new Object();
        ToStringStyle.register(obj);
        StringBuffer buffer = new StringBuffer("abc,");
        style.appendEnd(buffer, obj);
        assertEquals("abc]", buffer.toString());
        assertFalse(ToStringStyle.isRegistered(obj));
    }

    // Tests appendEnd when fieldSeparatorAtEnd is true
    @Test
    public void testAppendEnd_fieldSeparatorAtEnd_keepsSeparator() {
        TestStyle style = new TestStyle()
            .fieldSeparator(",")
            .contentEnd("]")
            .fieldSeparatorAtEnd(true);
        StringBuffer buffer = new StringBuffer("abc,");
        style.appendEnd(buffer, null);
        assertEquals("abc,]", buffer.toString());
    }

    // Tests null value output
    @Test
    public void testAppendObject_nullValue_appendNullText() {
        StringBuffer buffer = new StringBuffer();
        ToStringStyle.DEFAULT_STYLE.append(buffer, "field", null, true);
        assertEquals("field=<null>,", buffer.toString());
    }

    // Tests Object detail output
    @Test
    public void testAppendObject_value_detailAppendsValue() {
        StringBuffer buffer = new StringBuffer();
        ToStringStyle.DEFAULT_STYLE.append(buffer, "field", "hello", true);
        assertEquals("field=hello,", buffer.toString());
    }

    // Tests Object summary output and null fullDetail decision
    @Test
    public void testAppendObject_summaryAndDefaultFullDetail() {
        TestStyle style = new TestStyle();
        StringBuffer buffer = new StringBuffer();

        style.append(buffer, "field", "hello", false);
        assertEquals("field=<String>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "field", "hello", null);
        assertEquals("field=hello,", buffer.toString());

        style.defaultFullDetail(false);
        buffer.setLength(0);
        style.append(buffer, "field", "hello", null);
        assertEquals("field=<String>,", buffer.toString());
    }

    // Tests all primitive append methods including boundary values
    @Test
    public void testAppendPrimitiveValues_outputsAllPrimitiveTypes() {
        StringBuffer buffer = new StringBuffer();
        ToStringStyle.DEFAULT_STYLE.append(buffer, "long", Long.MIN_VALUE);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "longMax", Long.MAX_VALUE);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "int", Integer.MIN_VALUE);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "intMax", Integer.MAX_VALUE);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "short", (short) -1);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "byte", (byte) 0);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "char", 'A');
        ToStringStyle.DEFAULT_STYLE.append(buffer, "double", Double.NaN);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "float", Float.POSITIVE_INFINITY);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "boolean", true);

        assertEquals(
            "long=-9223372036854775808,longMax=9223372036854775807,"
            + "int=-2147483648,intMax=2147483647,"
            + "short=-1,byte=0,char=A,double=NaN,float=Infinity,boolean=true,",
            buffer.toString());
    }

    // Tests Object array detail including null element and summary
    @Test
    public void testAppendObjectArray_detailAndSummary() {
        StringBuffer buffer = new StringBuffer();
        Object[] array = new Object[] { null, "x" };

        ToStringStyle.DEFAULT_STYLE.append(buffer, "arr", array, true);
        assertEquals("arr={<null>,x},", buffer.toString());

        buffer.setLength(0);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "arr", new Object[] { "a", "b" }, false);
        assertEquals("arr=<size=2>,", buffer.toString());
    }

    // Tests primitive array detail output
    @Test
    public void testAppendPrimitiveArrays_detailOutputsAllTypes() {
        StringBuffer buffer = new StringBuffer();

        ToStringStyle.DEFAULT_STYLE.append(buffer, "longs", new long[] {1L, 2L}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "ints", new int[] {1, 2}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "shorts", new short[] {1, 2}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "bytes", new byte[] {1, 2}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "chars", new char[] {'a', 'b'}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "doubles", new double[] {1.5, 2.5}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "floats", new float[] {2.5f, 3.5f}, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "booleans", new boolean[] {true, false}, true);

        assertEquals(
            "longs={1,2},ints={1,2},shorts={1,2},bytes={1,2},chars={a,b},"
            + "doubles={1.5,2.5},floats={2.5,3.5},booleans={true,false},",
            buffer.toString());
    }

    // Tests primitive array summary output
    @Test
    public void testAppendPrimitiveArrays_summaryOutputsAllTypes() {
        StringBuffer buffer = new StringBuffer();

        ToStringStyle.DEFAULT_STYLE.append(buffer, "longs", new long[] {1L}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "ints", new int[] {1}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "shorts", new short[] {1}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "bytes", new byte[] {1}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "chars", new char[] {'a'}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "doubles", new double[] {1.0}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "floats", new float[] {1.0f}, false);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "booleans", new boolean[] {true}, false);

        assertEquals(
            "longs=<size=1>,ints=<size=1>,shorts=<size=1>,bytes=<size=1>,chars=<size=1>,"
            + "doubles=<size=1>,floats=<size=1>,booleans=<size=1>,",
            buffer.toString());
    }

    // Tests Collection and Map detail and summary paths
    @Test
    public void testAppendCollectionAndMap_detailAndSummary() {
        StringBuffer buffer = new StringBuffer();

        ToStringStyle.DEFAULT_STYLE.append(buffer, "list", Arrays.asList("a", "b"), true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "listSummary", Arrays.asList("a", "b"), false);

        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        ToStringStyle.DEFAULT_STYLE.append(buffer, "map", map, true);
        ToStringStyle.DEFAULT_STYLE.append(buffer, "mapSummary", map, false);

        assertEquals(
            "list=[a, b],listSummary=<size=2>,map={k=v},mapSummary=<size=1>,",
            buffer.toString());
    }

    // Tests that a registered String is still output as a normal String, not as a cyclic object
    @Test
    public void testAppendInternal_registeredString_outputsValue() {
        String value = "leak";
        ToStringStyle.register(value);
        try {
            StringBuffer buffer = new StringBuffer();
            new TestStyle().append(buffer, "field", (Object) value, true);
            assertEquals("field=leak,", buffer.toString());
        } finally {
            ToStringStyle.unregister(value);
        }
    }

    // Tests cycle detection for arrays
    @Test
    public void testAppendInternal_cycleDetectionUsesIdentityToString() {
        TestStyle style = new TestStyle();
        Object[] cycle = new Object[1];
        cycle[0] = cycle;

        StringBuffer buffer = new StringBuffer();
        style.callAppendInternal(buffer, "field", cycle, true);

        StringBuffer identity = new StringBuffer();
        ObjectUtils.identityToString(identity, cycle);
        assertEquals("{" + identity.toString() + "}", buffer.toString());
    }

    // Tests appendToString and appendSuper valid/null behavior
    @Test
    public void testAppendToStringAndSuper_appendsExtractedData() {
        TestStyle style = new TestStyle().fieldSeparator(",");

        StringBuffer buffer = new StringBuffer("Prefix[");
        style.appendToString(buffer, "Other[data]");
        assertEquals("Prefix[data,", buffer.toString());

        buffer.setLength(0);
        buffer.append("Prefix[");
        style.appendSuper(buffer, "Other[data]");
        assertEquals("Prefix[data,", buffer.toString());

        buffer.setLength(0);
        buffer.append("abc");
        style.appendToString(buffer, null);
        assertEquals("abc", buffer.toString());

        buffer.setLength(0);
        buffer.append("Prefix[,");
        style.fieldSeparatorAtStart(true);
        style.appendToString(buffer, "Other[data]");
        assertEquals("Prefix[data,", buffer.toString());
    }

    // Tests removeLastFieldSeparator including no-op cases
    @Test
    public void testRemoveLastFieldSeparator_removesOnlyWhenPresent() {
        TestStyle style = new TestStyle().fieldSeparator(",");

        StringBuffer buffer = new StringBuffer("a,b,");
        style.callRemoveLastFieldSeparator(buffer);
        assertEquals("a,b", buffer.toString());

        style.callRemoveLastFieldSeparator(buffer);
        assertEquals("a,b", buffer.toString());

        buffer.setLength(0);
        style.callRemoveLastFieldSeparator(buffer);
        assertEquals("", buffer.toString());
    }

    // Tests reflectionAppendArrayDetail for Object arrays with null elements
    @Test
    public void testReflectionAppendArrayDetail_outputsArrayContents() {
        TestStyle style = new TestStyle();
        Object[] array = new Object[] { "a", null, "b" };
        StringBuffer buffer = new StringBuffer();

        style.callReflectionAppendArrayDetail(buffer, "field", array);

        assertEquals("{a,<null>,b}", buffer.toString());
    }
}
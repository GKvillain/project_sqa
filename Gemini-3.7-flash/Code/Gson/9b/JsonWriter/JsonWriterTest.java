package com.google.gson.stream;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonWriterTest {

  private StringWriter stringWriter;
  private JsonWriter jsonWriter;

  @Before
  public void setUp() {
    stringWriter = new StringWriter();
    jsonWriter = new JsonWriter(stringWriter);
  }

  // Tests constructor with null writer throwing NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullWriter_throwsNullPointerException() {
    new JsonWriter(null);
  }

  // Tests standard object creation with primitives and string values
  @Test
  public void testObject_standardKeyValues_writesCorrectJson() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("name").value("Gson");
    jsonWriter.name("age").value(10);
    jsonWriter.name("active").value(true);
    jsonWriter.name("score").value(99.5);
    jsonWriter.endObject();
    jsonWriter.close();

    assertEquals("{\"name\":\"Gson\",\"age\":10,\"active\":true,\"score\":99.5}", stringWriter.toString());
  }

  // Tests nested arrays and objects
  @Test
  public void testArrayAndObject_nested_writesCorrectJson() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.beginObject();
    jsonWriter.name("a").value(1L);
    jsonWriter.endObject();
    jsonWriter.beginObject();
    jsonWriter.name("b").value(2L);
    jsonWriter.endObject();
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[{\"a\":1},{\"b\":2}]", stringWriter.toString());
  }

  // Tests pretty printing with indent
  @Test
  public void testIndent_prettyPrinting_formatsWithNewlinesAndSpaces() throws IOException {
    jsonWriter.setIndent("  ");
    jsonWriter.beginArray();
    jsonWriter.value("item1");
    jsonWriter.value("item2");
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[\n  \"item1\",\n  \"item2\"\n]", stringWriter.toString());
  }

  // Tests empty indent reset
  @Test
  public void testSetIndent_emptyString_resetsToCompact() throws IOException {
    jsonWriter.setIndent("  ");
    jsonWriter.setIndent("");
    jsonWriter.beginArray();
    jsonWriter.value(1);
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[1]", stringWriter.toString());
  }

  // Tests serializeNulls true vs false
  @Test
  public void testSerializeNulls_trueAndFalse_handlesNullPropertiesCorrectly() throws IOException {
    assertTrue(jsonWriter.getSerializeNulls());

    jsonWriter.beginObject();
    jsonWriter.name("a").nullValue();
    jsonWriter.setSerializeNulls(false);
    assertFalse(jsonWriter.getSerializeNulls());
    jsonWriter.name("b").nullValue();
    jsonWriter.name("c").value((String) null);
    jsonWriter.endObject();
    jsonWriter.close();

    assertEquals("{\"a\":null}", stringWriter.toString());
  }

  // Tests special characters escaping in string values
  @Test
  public void testString_escapedCharacters_escapesCorrectly() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value("\"\\\t\b\n\r\f\u2028\u2029");
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[\"\\\"\\\\\\t\\b\\n\\r\\f\\u2028\\u2029\"]", stringWriter.toString());
  }

  // Tests HTML safe escaping enabled
  @Test
  public void testHtmlSafe_specialHtmlChars_escapesHtmlCharacters() throws IOException {
    jsonWriter.setHtmlSafe(true);
    assertTrue(jsonWriter.isHtmlSafe());

    jsonWriter.beginArray();
    jsonWriter.value("<tag> & 'test' = true");
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[\"\\u003ctag\\u003e \\u0026 \\u0027test\\u0027 \\u003d true\"]", stringWriter.toString());
  }

  // Tests jsonValue direct raw JSON injection
  @Test
  public void testJsonValue_rawJson_appendsDirectly() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("raw");
    jsonWriter.jsonValue("{\"nested\":true}");
    jsonWriter.name("rawNull");
    jsonWriter.jsonValue(null);
    jsonWriter.endObject();
    jsonWriter.close();

    assertEquals("{\"raw\":{\"nested\":true},\"rawNull\":null}", stringWriter.toString());
  }

  // Tests various Number implementations
  @Test
  public void testValue_numberSubtypes_writesCorrectValues() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(new BigDecimal("1234567890.1234567890"));
    jsonWriter.value(new BigInteger("98765432109876543210"));
    jsonWriter.value((Number) null);
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[\n1234567890.1234567890,\n98765432109876543210,\nnull]".replace("\n", ""), stringWriter.toString());
  }

  // Tests NaN double throwing IllegalArgumentException in strict mode
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleNaNStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.value(Double.NaN);
  }

  // Tests infinite double throwing IllegalArgumentException in strict mode
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleInfiniteStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.value(Double.POSITIVE_INFINITY);
  }

  // Tests Number NaN throwing IllegalArgumentException in strict mode
  @Test(expected = IllegalArgumentException.class)
  public void testValue_numberNaNStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.value(Double.valueOf(Double.NaN));
  }

  // Tests lenient mode allowing NaN and top-level values
  @Test
  public void testLenient_topLevelAndNonFiniteNumbers_allowed() throws IOException {
    jsonWriter.setLenient(true);
    assertTrue(jsonWriter.isLenient());

    jsonWriter.value(Double.valueOf(Double.NaN));
    jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    jsonWriter.value("anotherTopLevel");
    jsonWriter.close();

    assertEquals("NaN-Infinity\"anotherTopLevel\"", stringWriter.toString());
  }

  // Tests null name throwing NullPointerException
  @Test(expected = NullPointerException.class)
  public void testName_nullName_throwsNullPointerException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name(null);
  }

  // Tests consecutive names without value throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_consecutiveNames_throwsIllegalStateException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.name("b");
  }

  // Tests name outside of object throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_outsideObject_throwsIllegalStateException() throws IOException {
    jsonWriter.name("a");
  }

  // Tests invalid endArray inside object throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndArray_insideObject_throwsIllegalStateException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.endArray();
  }

  // Tests dangling name when closing object throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_danglingName_throwsIllegalStateException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.endObject();
  }

  // Tests unclosed document throwing IOException on close
  @Test(expected = IOException.class)
  public void testClose_unclosedDocument_throwsIOException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.close();
  }

  // Tests Boolean object values including null, true, and false
  @Test
  public void testValue_booleanObject_writesCorrectValues() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(Boolean.TRUE);
    jsonWriter.value(Boolean.FALSE);
    jsonWriter.value((Boolean) null);
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[true,false,null]", stringWriter.toString());
  }

  // Tests null string value with default serializeNulls=true
  @Test
  public void testValue_nullString_writesNull() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value((String) null);
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[null]", stringWriter.toString());
  }

  // Tests flush method writes buffered data to underlying writer
  @Test
  public void testFlush_writesBufferedContent() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value("flushed");
    jsonWriter.flush();

    assertEquals("[\"flushed\"", stringWriter.toString());

    jsonWriter.endArray();
    jsonWriter.close();
  }

  // Tests empty array and empty object with indentation
  @Test
  public void testIndent_emptyArrayAndObject_formatsCompactly() throws IOException {
    jsonWriter.setIndent("  ");
    jsonWriter.beginArray();
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.beginObject();
    jsonWriter.endObject();
    jsonWriter.endArray();
    jsonWriter.close();

    assertEquals("[\n  [],\n  {}\n]", stringWriter.toString());
  }

  // Tests stack resizing when nesting exceeds initial stack capacity (32)
  @Test
  public void testDeepNesting_stackResizing_writesCorrectJson() throws IOException {
    int depth = 40;
    for (int i = 0; i < depth; i++) {
      jsonWriter.beginArray();
    }
    jsonWriter.value("deep");
    for (int i = 0; i < depth; i++) {
      jsonWriter.endArray();
    }
    jsonWriter.close();

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      expected.append('[');
    }
    expected.append("\"deep\"");
    for (int i = 0; i < depth; i++) {
      expected.append(']');
    }
    assertEquals(expected.toString(), stringWriter.toString());
  }

  // Tests multiple top-level values in strict mode throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testMultipleTopLevelValues_strictMode_throwsIllegalStateException() throws IOException {
    jsonWriter.value("first");
    jsonWriter.value("second");
  }

  // Tests endObject inside array throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_insideArray_throwsIllegalStateException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.endObject();
  }

  // Tests endArray on empty stack throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndArray_emptyStack_throwsIllegalStateException() throws IOException {
    jsonWriter.endArray();
  }

  // Tests endObject on empty stack throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_emptyStack_throwsIllegalStateException() throws IOException {
    jsonWriter.endObject();
  }

  // Tests write operations on closed JsonWriter throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testOperationsOnClosedWriter_throwsIllegalStateException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();

    jsonWriter.value("after-close");
  }

  // Tests closed state close is idempotent
  @Test
  public void testClose_calledTwice_doesNotThrow() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();
    jsonWriter.close();
  }

  // Tests Number string representations of NaN / Infinity in strict mode throw IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testValue_customNumberNaNStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.value(Float.valueOf(Float.NaN));
  }

  // Tests Number string representation of -Infinity in strict mode throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testValue_customNumberNegativeInfinityStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.value(Float.valueOf(Float.NEGATIVE_INFINITY));
  }
}
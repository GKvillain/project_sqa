package com.google.gson.stream;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JsonWriterTest {

  private StringWriter stringWriter;
  private JsonWriter jsonWriter;

  @Before
  public void setUp() {
    stringWriter = new StringWriter();
    jsonWriter = new JsonWriter(stringWriter);
  }

  // Tests null writer in constructor throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullWriter_throwsNullPointerException() {
    new JsonWriter(null);
  }

  // Tests writing empty object
  @Test
  public void testBeginObject_emptyObject_writesEmptyObject() throws IOException {
    jsonWriter.beginObject().endObject();
    assertEquals("{}", stringWriter.toString());
  }

  // Tests writing empty array
  @Test
  public void testBeginArray_emptyArray_writesEmptyArray() throws IOException {
    jsonWriter.beginArray().endArray();
    assertEquals("[]", stringWriter.toString());
  }

  // Tests writing string values with special character escaping
  @Test
  public void testValue_stringWithEscapedChars_escapesCorrectly() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value("a\"b\\c\td\be\nf\rg\fh\u2028i\u2029j");
    jsonWriter.endArray();
    assertEquals("[\"a\\\"b\\\\c\\td\\be\\nf\\rg\\fh\\u2028i\\u2029j\"]", stringWriter.toString());
  }

  // Tests html safe mode escaping
  @Test
  public void testValue_htmlSafeEnabled_escapesHtmlCharacters() throws IOException {
    jsonWriter.setHtmlSafe(true);
    assertTrue(jsonWriter.isHtmlSafe());
    jsonWriter.beginArray();
    jsonWriter.value("<>&='");
    jsonWriter.endArray();
    assertEquals("[\"\\u003c\\u003e\\u0026\\u003d\\u0027\"]", stringWriter.toString());
  }

  // Tests writing boolean values
  @Test
  public void testValue_booleanValues_writesCorrectLiterals() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(true);
    jsonWriter.value(false);
    jsonWriter.value((Boolean) true);
    jsonWriter.value((Boolean) null);
    jsonWriter.endArray();
    assertEquals("[true,false,true,null]", stringWriter.toString());
  }

  // Tests writing long values
  @Test
  public void testValue_longValue_writesCorrectNumber() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(12345678901L);
    jsonWriter.endArray();
    assertEquals("[12345678901]", stringWriter.toString());
  }

  // Tests strict mode throws exception on NaN double
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleNanStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(Double.NaN);
  }

  // Tests strict mode throws exception on Infinite double
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleInfiniteStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(Double.POSITIVE_INFINITY);
  }

  // Tests lenient mode permits NaN double value
  @Test
  public void testValue_doubleNanLenient_writesNan() throws IOException {
    jsonWriter.setLenient(true);
    assertTrue(jsonWriter.isLenient());
    jsonWriter.beginArray();
    jsonWriter.value(Double.NaN);
    jsonWriter.endArray();
    assertEquals("[NaN]", stringWriter.toString());
  }

  // Tests lenient mode permits Infinite double value
  @Test
  public void testValue_doubleInfiniteLenient_writesInfinity() throws IOException {
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Double.NEGATIVE_INFINITY);
    jsonWriter.value(Double.POSITIVE_INFINITY);
    jsonWriter.endArray();
    assertEquals("[-Infinity,Infinity]", stringWriter.toString());
  }

  // Tests strict mode throws exception on NaN Number object
  @Test(expected = IllegalArgumentException.class)
  public void testValue_numberNanStrict_throwsIllegalArgumentException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NaN));
  }

  // Tests lenient mode permits NaN and Infinity Number object
  @Test
  public void testValue_numberNanLenient_writesNan() throws IOException {
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NaN));
    jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
    jsonWriter.value((Number) null);
    jsonWriter.endArray();
    assertEquals("[NaN,Infinity,null]", stringWriter.toString());
  }

  // Tests serializeNulls flag true vs false
  @Test
  public void testSetSerializeNulls_false_skipsNullObjectMembers() throws IOException {
    jsonWriter.setSerializeNulls(false);
    assertFalse(jsonWriter.getSerializeNulls());
    jsonWriter.beginObject();
    jsonWriter.name("a").value((String) null);
    jsonWriter.name("b").nullValue();
    jsonWriter.name("c").value("value");
    jsonWriter.endObject();
    assertEquals("{\"c\":\"value\"}", stringWriter.toString());
  }

  // Tests writing raw json value
  @Test
  public void testJsonValue_validString_appendsDirectly() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.jsonValue("{\"raw\":1}");
    jsonWriter.jsonValue(null);
    jsonWriter.endArray();
    assertEquals("[{\"raw\":1},null]", stringWriter.toString());
  }

  // Tests indentation and formatting
  @Test
  public void testSetIndent_customIndent_formatsOutput() throws IOException {
    jsonWriter.setIndent("  ");
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.beginArray();
    jsonWriter.value(1);
    jsonWriter.endArray();
    jsonWriter.endObject();
    assertEquals("{\n  \"a\": [\n    1\n  ]\n}", stringWriter.toString());
  }

  // Tests null name throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testName_nullName_throwsNullPointerException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name(null);
  }

  // Tests duplicate name before value throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_consecutiveNames_throwsIllegalStateException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.name("b");
  }

  // Tests closing writer when document is incomplete throws IOException
  @Test(expected = IOException.class)
  public void testClose_incompleteDocument_throwsIOException() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.close();
  }

  // Tests nesting problem throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_unbalancedNesting_throwsIllegalStateException() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.endObject();
  }
}
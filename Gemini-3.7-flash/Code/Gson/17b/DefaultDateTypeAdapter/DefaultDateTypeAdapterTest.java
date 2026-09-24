package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class DefaultDateTypeAdapterTest {

  // Tests constructor with invalid date type class
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_unsupportedDateType_throwsIllegalArgumentException() {
    new DefaultDateTypeAdapter(DateSubclass.class);
  }

  // Tests constructor with dateStyle and timeStyle for default Date.class
  @Test
  public void testConstructor_styleInts_createsAdapter() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT);
    assertNotNull(adapter);
    assertTrue(adapter.toString().startsWith("DefaultDateTypeAdapter"));
  }

  // Tests constructor with Date class and style
  @Test
  public void testConstructor_dateTypeAndStyle_createsAdapter() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT);
    assertNotNull(adapter);
  }

  // Tests constructor with Date class and pattern string
  @Test
  public void testConstructor_dateTypeAndPattern_createsAdapter() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    assertNotNull(adapter);
  }

  // Tests write method with null date value
  @Test
  public void testWrite_nullDate_writesNull() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, null);
    assertEquals("null", writer.toString());
  }

  // Tests write and read roundtrip with Date.class
  @Test
  public void testWriteAndRead_utilDate_roundTripSuccess() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd HH:mm:ss");
    Date expectedDate = new Date(1000000000000L);

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, expectedDate);

    String json = writer.toString();
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    Date actualDate = adapter.read(jsonReader);

    assertEquals(expectedDate.getTime() / 1000, actualDate.getTime() / 1000);
    assertEquals(Date.class, actualDate.getClass());
  }

  // Tests write and read roundtrip with java.sql.Date
  @Test
  public void testWriteAndRead_sqlDate_returnsSqlDateInstance() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
    java.sql.Date expectedDate = new java.sql.Date(1000000000000L);

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, expectedDate);

    JsonReader jsonReader = new JsonReader(new StringReader(writer.toString()));
    Date actualDate = adapter.read(jsonReader);

    assertTrue(actualDate instanceof java.sql.Date);
  }

  // Tests write and read roundtrip with Timestamp
  @Test
  public void testWriteAndRead_timestamp_returnsTimestampInstance() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd HH:mm:ss");
    Timestamp expectedTimestamp = new Timestamp(1000000000000L);

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, expectedTimestamp);

    JsonReader jsonReader = new JsonReader(new StringReader(writer.toString()));
    Date actualDate = adapter.read(jsonReader);

    assertTrue(actualDate instanceof Timestamp);
  }

  // Tests read with ISO8601 formatted date string
  @Test
  public void testRead_iso8601String_parsesSuccessfully() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader jsonReader = new JsonReader(new StringReader("\"2020-01-01T00:00:00Z\""));
    Date date = adapter.read(jsonReader);
    assertNotNull(date);
  }

  // Tests read when JsonReader does not contain a string
  @Test(expected = JsonParseException.class)
  public void testRead_nonStringToken_throwsJsonParseException() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader jsonReader = new JsonReader(new StringReader("12345"));
    adapter.read(jsonReader);
  }

  // Tests read when JsonReader contains null value token (Defects4J Bug 17 check)
  @Test
  public void testRead_nullToken_returnsNull() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader jsonReader = new JsonReader(new StringReader("null"));
    Date date = adapter.read(jsonReader);
    assertNull(date);
  }

  // Tests read with unparseable date string
  @Test(expected = JsonSyntaxException.class)
  public void testRead_invalidDateString_throwsJsonSyntaxException() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader jsonReader = new JsonReader(new StringReader("\"not-a-valid-date\""));
    adapter.read(jsonReader);
  }

  // Tests toString output format
  @Test
  public void testToString_validAdapter_returnsFormattedString() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    assertEquals("DefaultDateTypeAdapter(SimpleDateFormat)", adapter.toString());
  }

  private static class DateSubclass extends Date {
  }
}
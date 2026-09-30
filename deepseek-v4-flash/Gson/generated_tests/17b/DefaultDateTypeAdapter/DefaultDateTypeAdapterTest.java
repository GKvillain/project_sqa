package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class DefaultDateTypeAdapterTest {

  private static class UnsupportedDate extends Date {
    private static final long serialVersionUID = 1L;
  }

  // Tests valid write operation with a Date value
  @Test
  public void testWrite_validDate_writesFormattedString() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    Date date = new Date(0L);
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    adapter.write(writer, date);
    writer.close();
    assertNotNull(stringWriter.toString());
  }

  // Tests write with null value
  @Test
  public void testWrite_nullValue_writesNull() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    adapter.write(writer, null);
    writer.close();
    assertEquals("null", stringWriter.toString());
  }

  // Tests read with valid date string
  @Test
  public void testRead_validDateString_returnsDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    String json = "\"Jan 1, 1970 12:00:00 AM\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertNotNull(date);
  }

  // Tests read with Timestamp type
  @Test
  public void testRead_validDateString_returnsTimestamp() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
    String json = "\"Jan 1, 1970 12:00:00 AM\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertTrue(date instanceof Timestamp);
  }

  // Tests read with java.sql.Date type
  @Test
  public void testRead_validDateString_returnsSqlDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
    String json = "\"Jan 1, 1970 12:00:00 AM\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertTrue(date instanceof java.sql.Date);
  }

  // Tests read with non-string token
  @Test(expected = JsonParseException.class)
  public void testRead_nonStringToken_throwsException() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = new JsonReader(new StringReader("123"));
    adapter.read(reader);
  }

  // Tests read with ISO8601 format
  @Test
  public void testRead_ISO8601Format_returnsDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    String json = "\"1970-01-01T00:00:00.000Z\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertNotNull(date);
  }

  // Tests read with invalid date string
  @Test(expected = JsonSyntaxException.class)
  public void testRead_invalidDateString_throwsException() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    String json = "\"invalid-date\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    adapter.read(reader);
  }

  // Tests constructor with invalid date type
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_invalidDateType_throwsException() {
    new DefaultDateTypeAdapter(UnsupportedDate.class);
  }

  // Tests constructor with date pattern
  @Test
  public void testConstructor_datePattern_createsAdapter() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    assertNotNull(adapter);
  }

  // Tests constructor with style
  @Test
  public void testConstructor_style_createsAdapter() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT);
    assertNotNull(adapter);
  }

  // Tests constructor with date style and time style
  @SuppressWarnings("deprecation")
  @Test
  public void testConstructor_dateTimeStyle_createsAdapter() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.DEFAULT, DateFormat.DEFAULT);
    assertNotNull(adapter);
  }

  // Tests round-trip write and read
  @Test
  public void testWriteRead_roundTrip_returnsOriginalDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    Date originalDate = new Date(123456789L);
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    adapter.write(writer, originalDate);
    writer.close();
    String json = stringWriter.toString();
    JsonReader reader = new JsonReader(new StringReader(json));
    Date deserializedDate = adapter.read(reader);
    assertNotNull(deserializedDate);
  }

  // Tests toString method
  @Test
  public void testToString_containsAdapterName() {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    String toString = adapter.toString();
    assertTrue(toString.contains("DefaultDateTypeAdapter"));
  }

  // Tests read with Timestamp and ISO8601 format
  @Test
  public void testRead_timestampISO8601_returnsTimestamp() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
    String json = "\"1970-01-01T00:00:00.000Z\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertTrue(date instanceof Timestamp);
  }

  // Tests read with java.sql.Date and ISO8601 format
  @Test
  public void testRead_sqlDateISO8601_returnsSqlDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
    String json = "\"1970-01-01T00:00:00.000Z\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertTrue(date instanceof java.sql.Date);
  }

  // Tests read with local format date string
  @Test
  public void testRead_localFormatDate_returnsDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
    String dateString = format.format(new Date(0L));
    String json = "\"" + dateString + "\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertNotNull(date);
  }

  // Tests read with enUs format date string
  @Test
  public void testRead_enUsFormatDate_returnsDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    String dateString = format.format(new Date(0L));
    String json = "\"" + dateString + "\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    Date date = adapter.read(reader);
    assertNotNull(date);
  }

  // Tests read with null token
  @Test
  public void testRead_nullValue_returnsNull() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    JsonReader reader = new JsonReader(new StringReader("null"));
    assertNull(adapter.read(reader));
  }

  // Tests write with a pattern constructor
  @Test
  public void testWrite_patternDate_writesExpectedDateString() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
      StringWriter stringWriter = new StringWriter();
      JsonWriter writer = new JsonWriter(stringWriter);
      adapter.write(writer, new Date(0L));
      writer.close();
      assertEquals("\"1970-01-01\"", stringWriter.toString());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests read with a pattern constructor
  @Test
  public void testRead_patternDate_returnsDate() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
      JsonReader reader = new JsonReader(new StringReader("\"1970-01-01\""));
      Date date = adapter.read(reader);
      assertNotNull(date);
      assertEquals(0L, date.getTime());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests write with Timestamp and pattern constructor
  @Test
  public void testWrite_timestamp_writesFormattedString() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd HH:mm:ss");
      StringWriter stringWriter = new StringWriter();
      JsonWriter writer = new JsonWriter(stringWriter);
      adapter.write(writer, new Timestamp(0L));
      writer.close();
      assertEquals("\"1970-01-01 00:00:00\"", stringWriter.toString());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests read with Timestamp and pattern constructor
  @Test
  public void testRead_timestampPattern_returnsTimestamp() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd HH:mm:ss");
      JsonReader reader = new JsonReader(new StringReader("\"1970-01-01 00:00:00\""));
      Date date = adapter.read(reader);
      assertNotNull(date);
      assertTrue(date instanceof Timestamp);
      assertEquals(0L, date.getTime());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests write with java.sql.Date and pattern constructor
  @Test
  public void testWrite_sqlDate_writesFormattedString() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
      StringWriter stringWriter = new StringWriter();
      JsonWriter writer = new JsonWriter(stringWriter);
      adapter.write(writer, new java.sql.Date(0L));
      writer.close();
      assertEquals("\"1970-01-01\"", stringWriter.toString());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests read with java.sql.Date and pattern constructor
  @Test
  public void testRead_sqlDatePattern_returnsSqlDate() throws Exception {
    Locale originalLocale = Locale.getDefault();
    TimeZone originalTimeZone = TimeZone.getDefault();
    try {
      Locale.setDefault(Locale.US);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
      JsonReader reader = new JsonReader(new StringReader("\"1970-01-01\""));
      Date date = adapter.read(reader);
      assertNotNull(date);
      assertTrue(date instanceof java.sql.Date);
      assertEquals(0L, date.getTime());
    } finally {
      Locale.setDefault(originalLocale);
      TimeZone.setDefault(originalTimeZone);
    }
  }

  // Tests enUs fallback when local format cannot parse the date
  @Test
  public void testRead_enUsFallbackWhenLocalFormatFails() throws Exception {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.GERMANY);
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
      JsonReader reader = new JsonReader(new StringReader("\"Jan 1, 1970 12:00:00 AM\""));
      Date date = adapter.read(reader);
      assertNotNull(date);
    } finally {
      Locale.setDefault(originalLocale);
    }
  }
}
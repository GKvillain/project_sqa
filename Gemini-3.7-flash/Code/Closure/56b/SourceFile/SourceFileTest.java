package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SourceFileTest {

  private File tempFile;

  @Before
  public void setUp() throws Exception {
    tempFile = File.createTempFile("SourceFileTest", ".js");
  }

  @After
  public void tearDown() throws Exception {
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  // Tests null fileName in constructor throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_nullFileName_throwsException() {
    new SourceFile(null);
  }

  // Tests empty fileName in constructor throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_emptyFileName_throwsException() {
    new SourceFile("");
  }

  // Tests basic properties of SourceFile created from code
  @Test
  public void testFromCode_validInput_setsPropertiesCorrectly() throws IOException {
    SourceFile sf = SourceFile.fromCode("test.js", "orig/test.js", "var a = 1;");
    assertEquals("test.js", sf.getName());
    assertEquals("orig/test.js", sf.getOriginalPath());
    assertEquals("test.js", sf.toString());
    assertEquals("var a = 1;", sf.getCode());
    assertTrue(sf.hasSourceInMemory());
  }

  // Tests default original path equals file name if not explicitly set
  @Test
  public void testGetOriginalPath_defaultPath_returnsFileName() {
    SourceFile sf = SourceFile.fromCode("test.js", "var a = 1;");
    assertEquals("test.js", sf.getOriginalPath());

    sf.setOriginalPath("custom/path.js");
    assertEquals("custom/path.js", sf.getOriginalPath());
  }

  // Tests getLine when file has single line without trailing newline (detects defect in getLine)
  @Test
  public void testGetLine_singleLineWithoutNewline_returnsCorrectLine() {
    SourceFile sf = SourceFile.fromCode("test.js", "var a = 1;");
    assertEquals("var a = 1;", sf.getLine(1));
    assertNull(sf.getLine(2));
  }

  // Tests getLine with multiple lines and sequential queries
  @Test
  public void testGetLine_multipleLines_returnsCorrectLines() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3");
    assertEquals("line1", sf.getLine(1));
    assertEquals("line2", sf.getLine(2));
    assertEquals("line3", sf.getLine(3));
    assertNull(sf.getLine(4));
  }

  // Tests getLine query for invalid line number before line 1 or beyond line count
  @Test
  public void testGetLine_outOfRange_returnsNull() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    assertNull(sf.getLine(0));
    assertNull(sf.getLine(3));
  }

  // Tests getLineOffset returns correct byte/char offset for valid lines
  @Test
  public void testGetLineOffset_validLines_returnsOffsets() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3");
    assertEquals(0, sf.getLineOffset(1));
    assertEquals(6, sf.getLineOffset(2));
    assertEquals(12, sf.getLineOffset(3));
  }

  // Tests getLineOffset with line number 0 throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffset_zeroLineNumber_throwsException() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    sf.getLineOffset(0);
  }

  // Tests getLineOffset with line number exceeding line count throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffset_lineNumberExceedsLines_throwsException() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    sf.getLineOffset(3);
  }

  // Tests getNumLines returns correct line count
  @Test
  public void testGetNumLines_multipleLines_returnsCorrectCount() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3\n");
    assertEquals(4, sf.getNumLines());
  }

  // Tests getCodeReader returns a Reader providing the source code
  @Test
  public void testGetCodeReader_preloaded_readsCorrectContent() throws IOException {
    SourceFile sf = SourceFile.fromCode("test.js", "var a = 1;");
    Reader reader = sf.getCodeReader();
    assertNotNull(reader);
    char[] buf = new char[10];
    int read = reader.read(buf);
    assertEquals("var a = 1;", new String(buf, 0, read));
  }

  // Tests getRegion returns appropriate excerpt around specified line
  @Test
  public void testGetRegion_validLine_returnsRegion() {
    SourceFile sf = SourceFile.fromCode("test.js", "1\n2\n3\n4\n5\n6\n7\n");
    Region region = sf.getRegion(3);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(6, region.getEndingLineNumber());
  }

  // Tests getRegion returns null when line number exceeds file length
  @Test
  public void testGetRegion_lineExceedsFileLength_returnsNull() {
    SourceFile sf = SourceFile.fromCode("test.js", "1\n2\n");
    Region region = sf.getRegion(10);
    assertNull(region);
  }

  // Tests SourceFile created from Generator caches code and can clear cache
  @Test
  public void testFromGenerator_generatesAndCachesCode() throws IOException {
    final int[] callCount = new int[]{0};
    SourceFile.Generator generator = new SourceFile.Generator() {
      @Override
      public String getCode() {
        callCount[0]++;
        return "generatedCode();";
      }
    };
    SourceFile sf = SourceFile.fromGenerator("generated.js", generator);
    assertEquals("generatedCode();", sf.getCode());
    assertEquals(1, callCount[0]);

    // Second call should hit cache
    assertEquals("generatedCode();", sf.getCode());
    assertEquals(1, callCount[0]);

    // Clear cache and call again
    sf.clearCachedSource();
    assertEquals("generatedCode();", sf.getCode());
    assertEquals(2, callCount[0]);
  }

  // Tests SourceFile created from InputStream
  @Test
  public void testFromInputStream_validStream_readsContent() throws IOException {
    InputStream is = new ByteArrayInputStream("streamCode();".getBytes(Charsets.UTF_8));
    SourceFile sf = SourceFile.fromInputStream("stream.js", "orig/stream.js", is);
    assertEquals("streamCode();", sf.getCode());
    assertEquals("stream.js", sf.getName());
    assertEquals("orig/stream.js", sf.getOriginalPath());
  }

  // Tests SourceFile created from Reader
  @Test
  public void testFromReader_validReader_readsContent() throws IOException {
    Reader reader = new StringReader("readerCode();");
    SourceFile sf = SourceFile.fromReader("reader.js", reader);
    assertEquals("readerCode();", sf.getCode());
    assertEquals("reader.js", sf.getName());
  }

  // Tests SourceFile created from File on disk
  @Test
  public void testFromFile_onDisk_readsContentAndClearsCache() throws IOException {
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write("fileContent();".getBytes(Charsets.UTF_8));
    }
    SourceFile sf = SourceFile.fromFile(tempFile, Charsets.UTF_8);
    assertFalse(sf.hasSourceInMemory());

    Reader reader = sf.getCodeReader();
    assertNotNull(reader);
    reader.close();

    assertEquals("fileContent();", sf.getCode());
    assertTrue(sf.hasSourceInMemory());

    sf.clearCachedSource();
    assertFalse(sf.hasSourceInMemory());
  }

  // Tests setting and getting extern status
  @Test
  public void testIsExtern_setAndGet_returnsCorrectStatus() {
    SourceFile sf = SourceFile.fromCode("test.js", "var a = 1;");
    assertFalse(sf.isExtern());
    sf.setIsExtern(true);
    assertTrue(sf.isExtern());
    sf.setIsExtern(false);
    assertFalse(sf.isExtern());
  }
}
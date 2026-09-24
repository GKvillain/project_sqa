package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

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

  // Tests constructor with null filename throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_nullFileName_throwsException() {
    new SourceFile(null);
  }

  // Tests constructor with empty filename throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_emptyFileName_throwsException() {
    new SourceFile("");
  }

  // Tests getting the last line when there is no trailing newline (Defects4J 56 defect test)
  @Test
  public void testGetLine_lastLineWithoutTrailingNewline_returnsLineContent() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3");
    assertEquals("line1", sf.getLine(1));
    assertEquals("line2", sf.getLine(2));
    assertEquals("line3", sf.getLine(3));
  }

  // Tests getting the single line without newline
  @Test
  public void testGetLine_singleLineWithoutNewline_returnsLineContent() {
    SourceFile sf = SourceFile.fromCode("test.js", "single_line_code");
    assertEquals("single_line_code", sf.getLine(1));
  }

  // Tests getting line beyond total lines returns null
  @Test
  public void testGetLine_lineNumberOutOfBounds_returnsNull() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\n");
    assertNull(sf.getLine(3));
    assertNull(sf.getLine(10));
  }

  // Tests sequential and reverse line queries using internal offset caching
  @Test
  public void testGetLine_cachedOffsetScanning_returnsCorrectLines() {
    SourceFile sf = SourceFile.fromCode("test.js", "first\nsecond\nthird\nfourth\n");
    assertEquals("first", sf.getLine(1));
    assertEquals("third", sf.getLine(3));
    assertEquals("second", sf.getLine(2));
    assertEquals("fourth", sf.getLine(4));
  }

  // Tests line offset calculations for valid line numbers
  @Test
  public void testGetLineOffset_validLines_returnsCorrectOffsets() {
    SourceFile sf = SourceFile.fromCode("test.js", "123\n5678\n0");
    assertEquals(0, sf.getLineOffset(1));
    assertEquals(4, sf.getLineOffset(2));
    assertEquals(9, sf.getLineOffset(3));
  }

  // Tests line offset throws exception for line 0
  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffset_zeroLineNumber_throwsException() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    sf.getLineOffset(0);
  }

  // Tests line offset throws exception for line beyond maximum
  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffset_exceedingLineNumber_throwsException() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    sf.getLineOffset(3);
  }

  // Tests getNumLines count
  @Test
  public void testGetNumLines_multiLineContent_returnsCorrectCount() {
    SourceFile sf = SourceFile.fromCode("test.js", "a\nb\nc\n");
    assertEquals(3, sf.getNumLines());
  }

  // Tests getRegion within bounds
  @Test
  public void testGetRegion_validMiddle_returnsRegion() {
    SourceFile sf = SourceFile.fromCode("test.js", "1\n2\n3\n4\n5\n6\n7\n8\n9\n");
    Region region = sf.getRegion(5);
    assertNotNull(region);
    assertEquals(3, region.getBeginningLineNumber());
    assertEquals(8, region.getEndingLineNumber());
    assertEquals("3\n4\n5\n6\n7", region.getSourceExcerpt());
  }

  // Tests getRegion with line number out of range returns null
  @Test
  public void testGetRegion_outOfRangeLineNumber_returnsNull() {
    SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
    assertNull(sf.getRegion(10));
  }

  // Tests getRegion when content does not end with a newline
  @Test
  public void testGetRegion_noTrailingNewline_returnsValidRegion() {
    SourceFile sf = SourceFile.fromCode("test.js", "1\n2\n3");
    Region region = sf.getRegion(2);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(4, region.getEndingLineNumber());
    assertEquals("1\n2\n3", region.getSourceExcerpt());
  }

  // Tests original path and extern flags getter and setter
  @Test
  public void testProperties_nameOriginalPathAndExtern_returnsExpectedValues() {
    SourceFile sf = SourceFile.fromCode("file.js", "orig/file.js", "var x;");
    assertEquals("file.js", sf.getName());
    assertEquals("file.js", sf.toString());
    assertEquals("orig/file.js", sf.getOriginalPath());

    sf.setOriginalPath("new/orig/file.js");
    assertEquals("new/orig/file.js", sf.getOriginalPath());

    assertFalse(sf.isExtern());
    sf.setIsExtern(true);
    assertTrue(sf.isExtern());
  }

  // Tests creation from InputStream
  @Test
  public void testFromInputStream_validStream_loadsCodeCorrectly() throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream("var a = 1;".getBytes(Charsets.UTF_8));
    SourceFile sf = SourceFile.fromInputStream("stream.js", in);
    assertEquals("var a = 1;", sf.getCode());
    assertTrue(sf.hasSourceInMemory());
  }

  // Tests creation from Reader
  @Test
  public void testFromReader_validReader_loadsCodeCorrectly() throws IOException {
    SourceFile sf = SourceFile.fromReader("reader.js", new StringReader("var b = 2;"));
    assertEquals("var b = 2;", sf.getCode());
  }

  // Tests generated SourceFile and clearCachedSource behavior
  @Test
  public void testFromGenerator_andClearCachedSource_regeneratesCode() throws IOException {
    SourceFile.Generator generator = new SourceFile.Generator() {
      @Override
      public String getCode() {
        return "generated()";
      }
    };
    SourceFile sf = SourceFile.fromGenerator("generated.js", generator);
    assertNull(sf.getCodeNoCache());
    assertEquals("generated()", sf.getCode());
    assertEquals("generated()", sf.getCodeNoCache());

    sf.clearCachedSource();
    assertNull(sf.getCodeNoCache());
    assertEquals("generated()", sf.getCode());
  }

  // Tests OnDisk SourceFile reading from file, getCodeReader, and clearCachedSource
  @Test
  public void testFromFile_onDiskFile_readsContentAndClearsCache() throws IOException {
    FileWriter writer = new FileWriter(tempFile);
    writer.write("var disk = true;\n");
    writer.close();

    SourceFile sf = SourceFile.fromFile(tempFile, Charsets.UTF_8);
    assertFalse(sf.hasSourceInMemory());

    Reader readerBeforeLoad = sf.getCodeReader();
    assertNotNull(readerBeforeLoad);
    readerBeforeLoad.close();

    assertEquals("var disk = true;\n", sf.getCode());
    assertTrue(sf.hasSourceInMemory());

    Reader readerAfterLoad = sf.getCodeReader();
    assertNotNull(readerAfterLoad);
    readerAfterLoad.close();

    sf.clearCachedSource();
    assertFalse(sf.hasSourceInMemory());
  }
}
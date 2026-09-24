package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;

public class LightweightMessageFormatterTest {

  private static final DiagnosticType FOO_ERROR =
      DiagnosticType.error("FOO_ERROR", "foo error message");
  private static final DiagnosticType BAR_WARNING =
      DiagnosticType.warning("BAR_WARNING", "bar warning message");

  // Tests formatting error without source provider
  @Test
  public void testFormatError_withoutSource_formatsHeaderOnly() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 10, 5, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:10: ERROR - foo error message\n", formatted);
  }

  // Tests formatting warning without source provider
  @Test
  public void testFormatWarning_withoutSource_formatsHeaderOnly() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError warning = JSError.make("test.js", 5, 2, BAR_WARNING);

    String formatted = formatter.formatWarning(warning);
    assertEquals("test.js:5: WARNING - bar warning message\n", formatted);
  }

  // Tests formatting error with source line and caret position
  @Test
  public void testFormatError_withSourceLine_appendsLineAndCaret() {
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider("var a = 1;");
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 4, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\nvar a = 1;\n    ^\n", formatted);
  }

  // Tests caret at end of line (regression test for charno == length)
  @Test
  public void testFormatError_charnoAtEndOfLine_appendsCaret() {
    String line = "var x = 1;";
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider(line);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, line.length(), FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\nvar x = 1;\n          ^\n", formatted);
  }

  // Tests caret formatting with whitespace characters (tabs)
  @Test
  public void testFormatError_withTabsInSource_preservesWhitespaceInCaretPadding() {
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider("\tvar x = 1;");
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 5, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\n\tvar x = 1;\n\t   ^\n", formatted);
  }

  // Tests formatting with null source name
  @Test
  public void testFormatError_nullSourceName_omitsSourceNameAndLine() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make(null, -1, -1, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("ERROR - foo error message\n", formatted);
  }

  // Tests formatting with negative line number
  @Test
  public void testFormatError_negativeLineNumber_omitsLineNumber() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", -1, -1, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js: ERROR - foo error message\n", formatted);
  }

  // Tests formatting when source excerpt is null
  @Test
  public void testFormatError_nullSourceExcerpt_omitsLineAndCaret() {
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider(null);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 0, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\n", formatted);
  }

  // Tests charno out of bounds (greater than line length)
  @Test
  public void testFormatError_charnoOutOfRange_omitsCaret() {
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider("var a;");
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 100, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\nvar a;\n", formatted);
  }

  // Tests negative charno
  @Test
  public void testFormatError_negativeCharno_omitsCaret() {
    SourceExcerptProvider provider = new SimpleSourceExcerptProvider("var a;");
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, -1, FOO_ERROR);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - foo error message\nvar a;\n", formatted);
  }

  // Tests constructor with null source provider throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullSourceProvider_throwsException() {
    new LightweightMessageFormatter(null, SourceExcerpt.LINE);
  }

  // Tests LineNumberingFormatter formatLine method
  @Test
  public void testLineNumberingFormatter_formatLine_returnsSameLine() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    String result = formatter.formatLine("var x = 1;", 1);
    assertEquals("var x = 1;", result);
  }

  // Tests LineNumberingFormatter formatRegion with null region
  @Test
  public void testLineNumberingFormatter_formatRegion_nullRegionReturnsNull() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    assertNull(formatter.formatRegion(null));
  }

  // Tests LineNumberingFormatter formatRegion with empty code
  @Test
  public void testLineNumberingFormatter_formatRegion_emptyCodeReturnsNull() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new SimpleRegion(1, 1, "");
    assertNull(formatter.formatRegion(region));
  }

  // Tests LineNumberingFormatter formatRegion with multiple lines
  @Test
  public void testLineNumberingFormatter_formatRegion_multiLineFormatsWithLineNumbers() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new SimpleRegion(9, 11, "if (foo) {\n  alert('bar');\n}");

    String result = formatter.formatRegion(region);
    assertEquals("   9| if (foo) {\n  10|   alert('bar');\n  11| }", result);
  }

  // Tests LineNumberingFormatter formatRegion with trailing newline
  @Test
  public void testLineNumberingFormatter_formatRegion_trailingNewline() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new SimpleRegion(1, 2, "line1\n");

    String result = formatter.formatRegion(region);
    assertEquals("  1| line1", result);
  }

  private static class SimpleSourceExcerptProvider implements SourceExcerptProvider {
    private final String line;
    private final Region region;

    SimpleSourceExcerptProvider(String line) {
      this(line, null);
    }

    SimpleSourceExcerptProvider(String line, Region region) {
      this.line = line;
      this.region = region;
    }

    @Override
    public String getSourceLine(String sourceName, int lineNumber) {
      return line;
    }

    @Override
    public Region getSourceRegion(String sourceName, int lineNumber) {
      return region;
    }
  }

  private static class SimpleRegion implements Region {
    private final int beginningLineNumber;
    private final int endingLineNumber;
    private final String sourceExcerpt;

    SimpleRegion(int beginningLineNumber, int endingLineNumber, String sourceExcerpt) {
      this.beginningLineNumber = beginningLineNumber;
      this.endingLineNumber = endingLineNumber;
      this.sourceExcerpt = sourceExcerpt;
    }

    @Override
    public int getBeginningLineNumber() {
      return beginningLineNumber;
    }

    @Override
    public int getEndingLineNumber() {
      return endingLineNumber;
    }

    @Override
    public String getSourceExcerpt() {
      return sourceExcerpt;
    }
  }
}
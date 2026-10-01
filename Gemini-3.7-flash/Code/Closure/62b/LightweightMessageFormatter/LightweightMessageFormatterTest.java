package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt.LINE;
import static com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt.REGION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LightweightMessageFormatterTest {

  private static final DiagnosticType FOO_ERROR =
      DiagnosticType.error("JSC_FOO_ERROR", "error description");
  private static final DiagnosticType FOO_WARNING =
      DiagnosticType.warning("JSC_FOO_WARNING", "warning description");

  // Tests formatting error without source provider
  @Test
  public void testFormatError_withoutSource_formatsMessageOnly() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 5, 2, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:5: ERROR - error description\n", formatted);
  }

  // Tests formatting warning without source provider
  @Test
  public void testFormatWarning_withoutSource_formatsWarningMessageOnly() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError warning = JSError.make("test.js", 5, 2, FOO_WARNING);
    String formatted = formatter.formatWarning(warning);
    assertEquals("test.js:5: WARNING - warning description\n", formatted);
  }

  // Tests formatting error with source line and caret positioning
  @Test
  public void testFormatError_withSourceAndLineExcerpt_includesLineAndCaret() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return "var x = 1;";
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 4, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\nvar x = 1;\n    ^\n", formatted);
  }

  // Tests defect Closure-62 where charno == line.length() (missing token at end of line)
  @Test
  public void testFormatError_caretAtEndOfLine_includesCaret() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return "foo";
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 3, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\nfoo\n   ^\n", formatted);
  }

  // Tests caret padding with whitespace and tabs
  @Test
  public void testFormatError_withTabsAndSpaces_preservesWhitespaceInCaretPadding() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return "\tvar\tx = 1;";
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 5, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\n\tvar\tx = 1;\n\t   ^\n", formatted);
  }

  // Tests negative charno branch where caret is omitted
  @Test
  public void testFormatError_negativeCharno_noCaret() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return "var x = 1;";
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, -1, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\nvar x = 1;\n", formatted);
  }

  // Tests charno out of bounds greater than line length
  @Test
  public void testFormatError_charnoGreaterThanLineLength_noCaret() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return "foo";
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 10, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\nfoo\n", formatted);
  }

  // Tests formatting error with null sourceName
  @Test
  public void testFormatError_nullSourceName_formatsWithoutSourcePrefix() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make(null, -1, -1, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("ERROR - error description\n", formatted);
  }

  // Tests formatting error with sourceName but negative lineNumber
  @Test
  public void testFormatError_sourceNameWithoutLineNumber_formatsSourceWithoutLine() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", -1, -1, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js: ERROR - error description\n", formatted);
  }

  // Tests null SourceExcerptProvider constructor throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullSource_throwsException() {
    new LightweightMessageFormatter(null);
  }

  // Tests LineNumberingFormatter formatLine returns original line
  @Test
  public void testLineNumberingFormatter_formatLine_returnsSameLine() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    assertEquals("var x = 1;", formatter.formatLine("var x = 1;", 1));
  }

  // Tests LineNumberingFormatter formatRegion with null and empty region
  @Test
  public void testLineNumberingFormatter_formatRegion_nullOrEmptyRegion() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    assertNull(formatter.formatRegion(null));

    Region emptyRegion = new Region() {
      public String getSourceExcerpt() {
        return "";
      }
      public int getBeginningLineNumber() {
        return 1;
      }
      public int getEndingLineNumber() {
        return 1;
      }
    };
    assertNull(formatter.formatRegion(emptyRegion));
  }

  // Tests LineNumberingFormatter formatRegion with multi-line region
  @Test
  public void testLineNumberingFormatter_formatRegion_multiLine() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new Region() {
      public String getSourceExcerpt() {
        return "if (foo) {\n  alert('bar');\n}";
      }
      public int getBeginningLineNumber() {
        return 9;
      }
      public int getEndingLineNumber() {
        return 11;
      }
    };
    String expected = "   9| if (foo) {\n  10|   alert('bar');\n  11| }";
    assertEquals(expected, formatter.formatRegion(region));
  }

  // Tests formatting with REGION excerpt type
  @Test
  public void testFormatError_withRegionExcerpt_formatsRegionWithoutCaret() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      public String getSourceLine(String sourceName, int lineNumber) {
        return null;
      }
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return new Region() {
          public String getSourceExcerpt() {
            return "var a;\nvar b;";
          }
          public int getBeginningLineNumber() {
            return 1;
          }
          public int getEndingLineNumber() {
            return 2;
          }
        };
      }
    };
    LightweightMessageFormatter formatter =
        new LightweightMessageFormatter(provider, REGION);
    JSError error = JSError.make("test.js", 1, 0, FOO_ERROR);
    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - error description\n  1| var a;\n  2| var b;\n", formatted);
  }
}
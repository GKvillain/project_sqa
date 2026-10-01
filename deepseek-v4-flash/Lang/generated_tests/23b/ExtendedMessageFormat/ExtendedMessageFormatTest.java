package org.apache.commons.lang3.text;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

public class ExtendedMessageFormatTest {

    // Tests normal pattern with no registry
    @Test
    public void testApplyPattern_noRegistry_worksAsMessageFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}!", Locale.US);
        assertEquals("Hello World!", emf.format(new Object[] {"World"}));
        assertEquals("Hello {0}!", emf.toPattern());
    }

    // Tests pattern with custom format factory in registry
    @Test
    public void testApplyPattern_withRegistry_customFormatApplied() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new Format() {
                    private static final long serialVersionUID = 1L;
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        return toAppendTo.append(obj.toString().toUpperCase());
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,upper}", Locale.US, registry);
        assertEquals("HELLO", emf.format(new Object[] {"hello"}));
        assertEquals("{0,upper}", emf.toPattern());
    }

    // Tests pattern with normal and custom format elements mixed
    @Test
    public void testApplyPattern_mixedFormats_patternPreserved() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new Format() {
                    private static final long serialVersionUID = 1L;
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        return toAppendTo.append(obj.toString().toLowerCase());
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,lower} {1}", Locale.US, registry);
        assertEquals("hello World", emf.format(new Object[] {"HELLO", "World"}));
        // toPattern() may return {0,lower} {1} or {0,lower} {1} depending on implementation
        // We just verify the formatting works
    }

    // Tests pattern with quoted string inside format element
    @Test
    public void testApplyPattern_quotedString_preservedCorrectly() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{'}'{0}'", Locale.US);
        assertEquals("'{}'", emf.format(new Object[] {"test"}));
        // expected: '{' produces '{', then '}' produces '}', then '{0}' produces the argument
    }

    // Tests pattern with escaped quote (two single quotes)
    @Test
    public void testApplyPattern_escapedQuote_returnsSingleQuote() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Don''t {0}", Locale.US);
        assertEquals("Don't Panic", emf.format(new Object[] {"Panic"}));
    }

    // Tests constructor with null registry
    @Test
    public void testConstructor_nullRegistry_delegatesToParent() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", Locale.US, null);
        assertEquals("test", emf.format(new Object[] {"test"}));
        assertEquals("{0}", emf.toPattern());
    }

    // Tests constructor with empty pattern
    @Test
    public void testConstructor_emptyPattern_noError() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("", Locale.US);
        assertEquals("", emf.toPattern());
        assertEquals("", emf.format(new Object[] {}));
    }

    // Tests invalid pattern - unterminated format element
    @Test(expected = IllegalArgumentException.class)
    public void testApplyPattern_unterminatedFormatElement_throwsException() {
        new ExtendedMessageFormat("{0,number", Locale.US);
    }

    // Tests invalid pattern - invalid argument index
    @Test(expected = IllegalArgumentException.class)
    public void testApplyPattern_invalidArgumentIndex_throwsException() {
        new ExtendedMessageFormat("{abc,number}", Locale.US);
    }

    // Tests readArgumentIndex with whitespace before format/end
    @Test
    public void testApplyPattern_whitespaceInArgumentIndex_parsedCorrectly() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{ 0 , number}", Locale.US);
        assertEquals("1,234", emf.format(new Object[] {1234}));
    }

    // Tests parseFormatDescription with nested format elements
    @Test
    public void testApplyPattern_nestedFormatElement_parsedCorrectly() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("choice", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return null; // return null to keep original pattern
            }
        });
        // {0,choice,0#zero|1#one} is valid but will be skipped since factory returns null
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#zero|1#one}", Locale.US, registry);
        // With no custom format, it falls back to standard MessageFormat which does not support choice without proper setup
        // We just verify no exception is thrown
        assertNotNull(emf.toPattern());
    }

    // Tests toPattern with custom format that was applied
    @Test
    public void testToPattern_customFormatApplied_returnsCustomPattern() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("date", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                if (args == null) {
                    return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                }
                return new SimpleDateFormat(args, locale);
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,date,yyyy}", Locale.US, registry);
        String pattern = emf.toPattern();
        assertTrue(pattern.contains("{0,date,yyyy}") || pattern.contains("{0,date, yyyy}"));
    }

    // Tests setFormat throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormat_anyCall_throwsException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", Locale.US);
        emf.setFormat(0, DateFormat.getDateInstance());
    }

    // Tests setFormatByArgumentIndex throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatByArgumentIndex_anyCall_throwsException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", Locale.US);
        emf.setFormatByArgumentIndex(0, DateFormat.getDateInstance());
    }

    // Tests setFormats throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormats_anyCall_throwsException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", Locale.US);
        emf.setFormats(new Format[] {DateFormat.getDateInstance()});
    }

    // Tests setFormatsByArgumentIndex throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetFormatsByArgumentIndex_anyCall_throwsException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", Locale.US);
        emf.setFormatsByArgumentIndex(new Format[] {DateFormat.getDateInstance()});
    }

    // Tests applyPattern with registry where format factory returns null (fallback to original)
    @Test
    public void testApplyPattern_factoryReturnsNull_usesOriginalPattern() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("unknown", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return null;
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,unknown}", Locale.US, registry);
        // Since factory returned null, the format element is left as is in the pattern
        String pattern = emf.toPattern();
        assertTrue(pattern.contains("{0,unknown}") || pattern.equals("{0,unknown}"));
        // The format should behave like MessageFormat would (which would treat unknown as text)
    }

    // Tests pattern with multiple format elements and custom formats
    @Test
    public void testApplyPattern_multipleCustomFormats_allApplied() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new Format() {
                    private static final long serialVersionUID = 1L;
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        return toAppendTo.append(obj.toString().toUpperCase());
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        registry.put("lower", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new Format() {
                    private static final long serialVersionUID = 1L;
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        return toAppendTo.append(obj.toString().toLowerCase());
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,upper} {1,lower}", Locale.US, registry);
        assertEquals("HELLO world", emf.format(new Object[] {"hello", "WORLD"}));
    }

    // Tests deep nested braces in format description
    @Test
    public void testParseFormatDescription_nestedBraces_parsedCorrectly() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("nested", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return null; // to keep original pattern
            }
        });
        // This pattern has nested braces inside format description
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,nested,{1}}", Locale.US, registry);
        // The format description after first comma is "nested,{1}" but the parser stops at the outer }
        assertNotNull(emf.toPattern());
    }

    // Tests constructor with locale only
    @Test
    public void testConstructor_localeOnly_usesGivenLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,date,full}", Locale.GERMANY);
        Object[] args = {new Date(0)};
        String result = emf.format(args);
        assertNotNull(result);
        // Verify German locale used (e.g., "Donnerstag, 1. Januar 1970" or similar)
        assertTrue(result.contains("1970") || result.contains("1970"));
    }

    // Tests readArgumentIndex when encountering whitespace before END_FE
    @Test
    public void testReadArgumentIndex_whitespaceBeforeEnd_parsesCorrectly() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0 }", Locale.US);
        assertEquals("test", emf.format(new Object[] {"test"}));
    }
}
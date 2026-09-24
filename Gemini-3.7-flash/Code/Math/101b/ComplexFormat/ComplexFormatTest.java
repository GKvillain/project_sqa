package org.apache.commons.math.complex;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComplexFormatTest {

    private ComplexFormat complexFormat;
    private ComplexFormat complexFormatJ;

    @Before
    public void setUp() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(2);
        complexFormat = new ComplexFormat(nf);
        complexFormatJ = new ComplexFormat("j", nf);
    }

    // Tests formatting standard complex number with positive imaginary part
    @Test
    public void testFormat_positiveImaginary_returnsFormattedString() {
        Complex c = new Complex(1.23, 4.56);
        String actual = complexFormat.format(c);
        assertEquals("1.23 + 4.56i", actual);
    }

    // Tests formatting standard complex number with negative imaginary part
    @Test
    public void testFormat_negativeImaginary_returnsFormattedString() {
        Complex c = new Complex(1.23, -4.56);
        String actual = complexFormat.format(c);
        assertEquals("1.23 - 4.56i", actual);
    }

    // Tests formatting zero imaginary part
    @Test
    public void testFormat_zeroImaginary_returnsRealOnly() {
        Complex c = new Complex(1.23, 0.0);
        String actual = complexFormat.format(c);
        assertEquals("1.23", actual);
    }

    // Tests formatting custom imaginary character
    @Test
    public void testFormat_customImaginaryCharacter_returnsFormattedString() {
        Complex c = new Complex(1.0, 2.0);
        String actual = complexFormatJ.format(c);
        assertEquals("1 + 2j", actual);
    }

    // Tests formatting special values: NaN and Infinity
    @Test
    public void testFormat_specialDoubleValues_returnsParenthesizedString() {
        Complex nan = new Complex(Double.NaN, Double.NaN);
        assertEquals("(NaN) + (NaN)i", complexFormat.format(nan));

        Complex inf = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals("(Infinity) - (Infinity)i", complexFormat.format(inf));
    }

    // Tests static formatComplex method
    @Test
    public void testFormatComplex_validComplex_returnsFormattedString() {
        Complex c = new Complex(2.0, 3.0);
        String formatted = ComplexFormat.formatComplex(c);
        assertNotNull(formatted);
        assertTrue(formatted.contains("2") && formatted.contains("3"));
    }

    // Tests formatting java.lang.Number object as Complex
    @Test
    public void testFormat_numberObject_formatsAsRealComplex() {
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        complexFormat.format(Double.valueOf(5.5), sb, pos);
        assertEquals("5.5", sb.toString());
    }

    // Tests formatting unsupported Object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_unsupportedObject_throwsIllegalArgumentException() {
        complexFormat.format("not a number or complex", new StringBuffer(), new FieldPosition(0));
    }

    // Tests parsing standard positive imaginary complex string
    @Test
    public void testParse_positiveImaginary_returnsComplex() throws ParseException {
        Complex c = complexFormat.parse("1.23 + 4.56i");
        assertEquals(1.23, c.getReal(), 1e-6);
        assertEquals(4.56, c.getImaginary(), 1e-6);
    }

    // Tests parsing standard negative imaginary complex string
    @Test
    public void testParse_negativeImaginary_returnsComplex() throws ParseException {
        Complex c = complexFormat.parse("1.23 - 4.56i");
        assertEquals(1.23, c.getReal(), 1e-6);
        assertEquals(-4.56, c.getImaginary(), 1e-6);
    }

    // Tests parsing real-only string
    @Test
    public void testParse_realOnly_returnsComplex() throws ParseException {
        Complex c = complexFormat.parse("1.23");
        assertEquals(1.23, c.getReal(), 1e-6);
        assertEquals(0.0, c.getImaginary(), 1e-6);
    }

    // Tests parsing string with special Double values
    @Test
    public void testParse_specialValues_returnsComplex() throws ParseException {
        Complex c = complexFormat.parse("(NaN) + (Infinity)i");
        assertTrue(Double.isNaN(c.getReal()));
        assertTrue(Double.isInfinite(c.getImaginary()));
    }

    // Tests parseObject method
    @Test
    public void testParseObject_validString_returnsObject() {
        ParsePosition pos = new ParsePosition(0);
        Object obj = complexFormat.parseObject("2 + 3i", pos);
        assertTrue(obj instanceof Complex);
        Complex c = (Complex) obj;
        assertEquals(2.0, c.getReal(), 1e-6);
        assertEquals(3.0, c.getImaginary(), 1e-6);
    }

    // Tests parsing invalid format throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidString_throwsParseException() throws ParseException {
        complexFormat.parse("invalid");
    }

    // Tests parsing string missing imaginary character without StringIndexOutOfBoundsException (Defect 101)
    @Test
    public void testParse_missingImaginaryCharacter_returnsNullOrSetsErrorIndex() {
        ParsePosition pos = new ParsePosition(0);
        Complex result = complexFormat.parse("1 + 1", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parse with invalid sign character
    @Test
    public void testParse_invalidSign_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Complex result = complexFormat.parse("1 * 2i", pos);
        assertNull(result);
    }

    // Tests setImaginaryCharacter with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacter_null_throwsIllegalArgumentException() {
        complexFormat.setImaginaryCharacter(null);
    }

    // Tests setImaginaryCharacter with empty string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacter_empty_throwsIllegalArgumentException() {
        complexFormat.setImaginaryCharacter("");
    }

    // Tests setImaginaryFormat with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetImaginaryFormat_null_throwsIllegalArgumentException() {
        complexFormat.setImaginaryFormat(null);
    }

    // Tests setRealFormat with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetRealFormat_null_throwsIllegalArgumentException() {
        complexFormat.setRealFormat(null);
    }

    // Tests constructors and getters/available locales
    @Test
    public void testGettersAndAvailableLocales() {
        ComplexFormat defaultFormat = new ComplexFormat();
        assertEquals("i", defaultFormat.getImaginaryCharacter());
        assertNotNull(defaultFormat.getRealFormat());
        assertNotNull(defaultFormat.getImaginaryFormat());
        assertNotNull(ComplexFormat.getAvailableLocales());
        assertTrue(ComplexFormat.getAvailableLocales().length > 0);

        ComplexFormat custom = new ComplexFormat("j", NumberFormat.getInstance(), NumberFormat.getInstance());
        assertEquals("j", custom.getImaginaryCharacter());
    }
}
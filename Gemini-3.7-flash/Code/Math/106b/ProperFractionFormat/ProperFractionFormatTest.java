package org.apache.commons.math.fraction;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProperFractionFormatTest {

    private ProperFractionFormat format;

    @Before
    public void setUp() {
        format = new ProperFractionFormat(NumberFormat.getIntegerInstance(Locale.US));
    }

    // Tests default constructor initialization
    @Test
    public void testDefaultConstructor() {
        ProperFractionFormat properFormat = new ProperFractionFormat();
        assertNotNull(properFormat.getWholeFormat());
        assertNotNull(properFormat.getNumeratorFormat());
        assertNotNull(properFormat.getDenominatorFormat());
    }

    // Tests three-argument constructor
    @Test
    public void testThreeArgConstructor() {
        NumberFormat nf1 = NumberFormat.getInstance();
        NumberFormat nf2 = NumberFormat.getInstance();
        NumberFormat nf3 = NumberFormat.getInstance();
        ProperFractionFormat properFormat = new ProperFractionFormat(nf1, nf2, nf3);
        assertEquals(nf1, properFormat.getWholeFormat());
        assertEquals(nf2, properFormat.getNumeratorFormat());
        assertEquals(nf3, properFormat.getDenominatorFormat());
    }

    // Tests formatting a proper fraction where whole part is zero
    @Test
    public void testFormat_wholePartZero_formatsFractionOnly() {
        Fraction fraction = new Fraction(1, 2);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        format.format(fraction, sb, pos);
        assertEquals("1 / 2", sb.toString());
    }

    // Tests formatting a positive mixed fraction
    @Test
    public void testFormat_positiveMixedFraction_formatsWholeAndFraction() {
        Fraction fraction = new Fraction(7, 2);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        format.format(fraction, sb, pos);
        assertEquals("3 1 / 2", sb.toString());
    }

    // Tests formatting a negative mixed fraction
    @Test
    public void testFormat_negativeMixedFraction_formatsNegativeWholeAndPositiveFraction() {
        Fraction fraction = new Fraction(-7, 2);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        format.format(fraction, sb, pos);
        assertEquals("-3 1 / 2", sb.toString());
    }

    // Tests parsing a valid positive proper fraction
    @Test
    public void testParse_validPositiveProperFraction_returnsCorrectFraction() throws ParseException {
        Fraction fraction = format.parse("1 1/2");
        assertNotNull(fraction);
        assertEquals(3, fraction.getNumerator());
        assertEquals(2, fraction.getDenominator());
    }

    // Tests parsing a valid negative proper fraction
    @Test
    public void testParse_validNegativeProperFraction_returnsCorrectFraction() throws ParseException {
        Fraction fraction = format.parse("-1 1/2");
        assertNotNull(fraction);
        assertEquals(-3, fraction.getNumerator());
        assertEquals(2, fraction.getDenominator());
    }

    // Tests parsing improper fraction fallback
    @Test
    public void testParse_improperFractionString_returnsCorrectFraction() throws ParseException {
        Fraction fraction = format.parse("2/3");
        assertNotNull(fraction);
        assertEquals(2, fraction.getNumerator());
        assertEquals(3, fraction.getDenominator());
    }

    // Tests parsing when numerator has an invalid negative sign
    @Test
    public void testParse_negativeNumeratorInProperFraction_failsParsing() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 -2/3", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing when denominator has an invalid negative sign
    @Test
    public void testParse_negativeDenominatorInProperFraction_failsParsing() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 2/-3", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing when both whole and numerator have negative signs
    @Test
    public void testParse_negativeWholeAndNegativeNumerator_failsParsing() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("-1 -2/3", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing when both whole and denominator have negative signs
    @Test
    public void testParse_negativeWholeAndNegativeDenominator_failsParsing() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("-1 2/-3", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing with no slash after whole and numerator
    @Test
    public void testParse_noSlashAfterNumerator_returnsNumeratorAsDenominatorOne() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 2", pos);
        assertNotNull(result);
        assertEquals(2, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    // Tests parsing with invalid character instead of slash
    @Test
    public void testParse_invalidCharacterInsteadOfSlash_failsParsing() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 2 a", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertTrue(pos.getErrorIndex() > 0);
    }

    // Tests parsing invalid whole number string
    @Test
    public void testParse_invalidWholeNumber_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("abc 1/2", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing invalid numerator string
    @Test
    public void testParse_invalidNumerator_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 abc/2", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests parsing invalid denominator string
    @Test
    public void testParse_invalidDenominator_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Fraction result = format.parse("1 2/abc", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests setting null whole format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetWholeFormat_nullFormat_throwsIllegalArgumentException() {
        format.setWholeFormat(null);
    }

    // Tests setting and getting valid whole format
    @Test
    public void testSetWholeFormat_validFormat_updatesWholeFormat() {
        NumberFormat newFormat = NumberFormat.getIntegerInstance();
        format.setWholeFormat(newFormat);
        assertEquals(newFormat, format.getWholeFormat());
    }
}
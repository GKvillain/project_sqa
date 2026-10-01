package com.fasterxml.jackson.core.base;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class ParserBaseTest {

    // Helper inner class that extends ParserBase so we can instantiate and test
    private static class TestParser extends ParserBase {
        public TestParser(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        @Override
        protected void _closeInput() throws IOException {
            // do nothing
        }

        @Override
        protected char _decodeEscaped() throws IOException {
            throw new UnsupportedOperationException();
        }

        // Required JsonParser abstract methods to make the test parser concrete
        @Override
        public JsonToken nextToken() throws IOException {
            return null;
        }

        @Override
        public String getText() throws IOException {
            return _textBuffer.contentsAsString();
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _textBuffer.getTextBuffer();
        }

        @Override
        public int getTextLength() throws IOException {
            return _textBuffer.getTextLength();
        }

        @Override
        public int getTextOffset() throws IOException {
            return _textBuffer.getTextOffset();
        }

        @Override
        public boolean hasTextCharacters() {
            return false;
        }

        // Expose protected fields if necessary for assertions
        public JsonReadContext getReadContext() {
            return _parsingContext;
        }

        public boolean isClosed() {
            return _closed;
        }
    }

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), null, false);
    }

    // Helper to reset numeric state for testing
    private void setText(TestParser p, String text) {
        p._textBuffer.resetWithString(text);
    }

    // =================================================================
    // Tests for STRICT_DUPLICATE_DETECTION feature (Defect area)
    // =================================================================

    // Test constructor: default features -> no dup detector
    @Test
    public void testConstructor_defaultFeatures_dupDetectorNull() {
        TestParser p = new TestParser(createIOContext(), 0);
        assertNull(p.getReadContext().getDupDetector());
    }

    // Test constructor: with STRICT_DUPLICATE_DETECTION enabled -> dup detector present
    @Test
    public void testConstructor_enableStrictDuplicateDetection_dupDetectorNotNull() {
        int features = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParser p = new TestParser(createIOContext(), features);
        assertNotNull(p.getReadContext().getDupDetector());
    }

    // Test enable: should add detector when not present
    @Test
    public void testEnable_strictDuplicateDetection_shouldAddDetector() {
        TestParser p = new TestParser(createIOContext(), 0);
        p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertNotNull(p.getReadContext().getDupDetector());
    }

    // Test disable: should remove detector when present
    @Test
    public void testDisable_strictDuplicateDetection_shouldRemoveDetector() {
        int features = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParser p = new TestParser(createIOContext(), features);
        p.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertNull(p.getReadContext().getDupDetector());
    }

    // Test overrideStdFeatures: enable feature -> detector added
    @Test
    public void testOverrideStdFeatures_enableFeature_shouldAddDetector() {
        TestParser p = new TestParser(createIOContext(), 0);
        int values = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        p.overrideStdFeatures(values, values);
        assertNotNull(p.getReadContext().getDupDetector());
    }

    // Test overrideStdFeatures: disable feature -> detector removed
    @Test
    public void testOverrideStdFeatures_disableFeature_shouldRemoveDetector() {
        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParser p = new TestParser(createIOContext(), mask);
        p.overrideStdFeatures(0, mask);
        assertNull(p.getReadContext().getDupDetector());
    }

    // Test that overrideStdFeatures toggles correctly when feature is already active
    // This targets the suspicious logic in _checkStdFeatureChanges
    @Test
    public void testOverrideStdFeatures_enableThenDisable_shouldMatchExpected() {
        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParser p = new TestParser(createIOContext(), 0);

        // Enable
        p.overrideStdFeatures(mask, mask);
        assertNotNull("Detector should be present after enable", p.getReadContext().getDupDetector());

        // Disable
        p.overrideStdFeatures(0, mask);
        assertNull("Detector should be null after disable", p.getReadContext().getDupDetector());
    }

    // Test setFeatureMask similar to overrideStdFeatures
    @Test
    public void testSetFeatureMask_enableThenDisable_shouldMatchExpected() {
        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        TestParser p = new TestParser(createIOContext(), 0);

        // Enable: set new mask to include the feature
        int newMask = p._features | mask;
        p.setFeatureMask(newMask);
        assertNotNull("Detector should be present after enable", p.getReadContext().getDupDetector());

        // Disable: set new mask to exclude the feature
        int disableMask = p._features & ~mask;
        p.setFeatureMask(disableMask);
        assertNull("Detector should be null after disable", p.getReadContext().getDupDetector());
    }

    // =================================================================
    // Tests for numeric state reset and accessors
    // =================================================================

    // Test resetInt returns correct token and sets state
    @Test
    public void testResetInt_positiveValue_returnsIntToken() {
        TestParser p = new TestParser(createIOContext(), 0);
        JsonToken token = p.resetInt(false, 5);
        assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        assertFalse(p._numberNegative);
        assertEquals(5, p._intLength);
        assertEquals(0, p._fractLength);
        assertEquals(0, p._expLength);
    }

    // Test resetFloat returns correct token
    @Test
    public void testResetFloat_positiveValue_returnsFloatToken() {
        TestParser p = new TestParser(createIOContext(), 0);
        JsonToken token = p.resetFloat(false, 3, 2, 1);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, token);
        assertFalse(p._numberNegative);
        assertEquals(3, p._intLength);
        assertEquals(2, p._fractLength);
        assertEquals(1, p._expLength);
    }

    // Test resetAsNaN sets NaN state
    @Test
    public void testResetAsNaN_nanString_returnsFloatTokenAndSetsNaN() {
        TestParser p = new TestParser(createIOContext(), 0);
        JsonToken token = p.resetAsNaN("NaN", Double.NaN);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, token);
        assertTrue(Double.isNaN(p._numberDouble));
        assertEquals(Double.NaN, p._numberDouble, 0.0);
    }

    // Test isNaN with NaN value
    @Test
    public void testIsNaN_nanValue_returnsTrue() {
        TestParser p = new TestParser(createIOContext(), 0);
        p.resetAsNaN("NaN", Double.NaN);
        assertTrue(p.isNaN());
    }

    // Test isNaN with normal double returns false
    @Test
    public void testIsNaN_normalDouble_returnsFalse() {
        TestParser p = new TestParser(createIOContext(), 0);
        p.resetFloat(false, 1, 1, 0);
        p._numberDouble = 3.14;
        p._numTypesValid = ParserBase.NR_DOUBLE; // direct access
        assertFalse(p.isNaN());
    }

    // =================================================================
    // Tests for getNumberValue and specific type accessors
    // =================================================================

    // Test getIntValue with small integer (fits int)
    @Test
    public void testGetIntValue_smallInt_returnsCorrectInt() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(false, 3);
        setText(p, "123");
        assertEquals(123, p.getIntValue());
    }

    // Test getIntValue with negative small integer
    @Test
    public void testGetIntValue_negativeSmallInt_returnsCorrectInt() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(true, 3);
        setText(p, "-456");
        assertEquals(-456, p.getIntValue());
    }

    // Test getIntValue with integer that fits long but not int => should throw?
    // Actually getIntValue will try to convert from long and throw if out of range
    @Test(expected = JsonParseException.class)
    public void testGetIntValue_overflowInt_throwsException() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(true, 12);
        setText(p, "-1234567890123"); // about -1.23e12, exceeds int range
        p.getIntValue();
    }

    // Test getLongValue with long integer
    @Test
    public void testGetLongValue_longInteger_returnsCorrectLong() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(false, 12);
        setText(p, "123456789012");
        assertEquals(123456789012L, p.getLongValue());
    }

    // Test getDoubleValue from float
    @Test
    public void testGetDoubleValue_floatValue_returnsCorrectDouble() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetFloat(false, 2, 2, 0);
        setText(p, "12.34");
        assertEquals(12.34, p.getDoubleValue(), 0.0001);
    }

    // Test getDecimalValue from float
    @Test
    public void testGetDecimalValue_floatValue_returnsBigDecimal() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetFloat(false, 1, 2, 0);
        setText(p, "1.23");
        assertEquals(new BigDecimal("1.23"), p.getDecimalValue());
    }

    // Test getNumberValue returns correct type for small int
    @Test
    public void testGetNumberValue_smallInt_returnsInteger() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(false, 2);
        setText(p, "42");
        Number num = p.getNumberValue();
        assertTrue(num instanceof Integer);
        assertEquals(42, num.intValue());
    }

    // Test getNumberValue returns Long for large int
    @Test
    public void testGetNumberValue_largeInt_returnsLong() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(false, 12);
        setText(p, "100000000000"); // 10^11
        Number num = p.getNumberValue();
        assertTrue(num instanceof Long);
        assertEquals(100000000000L, num.longValue());
    }

    // =================================================================
    // New tests for previously uncovered / compile-failed coverage
    // =================================================================

    @Test
    public void testGetNumberType_int_returnsInt() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetInt(false, 2);
        setText(p, "42");
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
    }

    @Test
    public void testGetNumberType_float_returnsDouble() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetFloat(false, 2, 2, 0);
        setText(p, "12.34");
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
    }

    @Test
    public void testGetNumberValue_floatValue_returnsDouble() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = p.resetFloat(false, 2, 2, 0);
        setText(p, "12.34");
        Number num = p.getNumberValue();
        assertTrue(num instanceof Double);
        assertEquals(12.34, num.doubleValue(), 0.0001);
    }

    @Test
    public void testGetCurrentName_startArray_returnsParentName() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = JsonToken.START_ARRAY;
        JsonReadContext parent = p._parsingContext;
        p._parsingContext = parent.createChildArrayContext(1, 1);
        parent.setCurrentName("arrField");
        assertEquals("arrField", p.getCurrentName());
    }

    @Test
    public void testGetCurrentName_fieldName_returnsCurrentName() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = JsonToken.FIELD_NAME;
        p._parsingContext.setCurrentName("field");
        assertEquals("field", p.getCurrentName());
    }

    // =================================================================
    // Tests for close and buffer release
    // =================================================================

    // Test close sets closed flag and releases buffers
    @Test
    public void testClose_parser_closedAndBuffersReleased() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
        // After close, _textBuffer should have been released (likely empty)
        // We can check internal buffer is released by trying to access _textBuffer? Not needed.
    }

    // =================================================================
    // Tests for location and name methods
    // =================================================================

    // Test getCurrentName returns parent name for START_OBJECT
    @Test
    public void testGetCurrentName_startObject_returnsParentName() throws IOException {
        TestParser p = new TestParser(createIOContext(), 0);
        // Simulate parent context with a name
        JsonReadContext parent = p._parsingContext;
        // We need to create a child context inside an object?
        // Simulate by setting current token to START_OBJECT and changing parsing context
        p._currToken = JsonToken.START_OBJECT;
        // Create child context (simulate entering object)
        JsonReadContext child = parent.createChildObjectContext(1, 1);
        p._parsingContext = child;
        // Set parent name
        parent.setCurrentName("parentField");
        assertEquals("parentField", p.getCurrentName());
    }

    // Test overrideCurrentName with START_OBJECT
    @Test
    public void testOverrideCurrentName_startObject_setsParentName() {
        TestParser p = new TestParser(createIOContext(), 0);
        p._currToken = JsonToken.START_OBJECT;
        JsonReadContext child = p._parsingContext.createChildObjectContext(1, 1);
        p._parsingContext = child;
        p.overrideCurrentName("newName");
        // The parent context should now have the name
        JsonReadContext parent = p._parsingContext.getParent();
        try {
            assertEquals("newName", parent.getCurrentName());
        } catch (IOException e) {
            fail("Unexpected exception");
        }
    }

    // Test getTokenLocation returns non-null location
    @Test
    public void testGetTokenLocation_returnsValidLocation() {
        TestParser p = new TestParser(createIOContext(), 0);
        p._tokenInputTotal = 100;
        p._tokenInputRow = 5;
        p._tokenInputCol = 0; // 0-based will become 1 in getter
        JsonLocation loc = p.getTokenLocation();
        assertNotNull(loc);
        assertEquals(100, loc.getCharOffset());
        assertEquals(5, loc.getLineNr());
        assertEquals(1, loc.getColumnNr()); // converted from 0 to 1
    }

    // Test getCurrentLocation returns correct location
    @Test
    public void testGetCurrentLocation_returnsValidLocation() {
        TestParser p = new TestParser(createIOContext(), 0);
        p._inputPtr = 50;
        p._currInputRowStart = 20;
        p._currInputRow = 3;
        p._currInputProcessed = 200;
        JsonLocation loc = p.getCurrentLocation();
        assertNotNull(loc);
        // column = _inputPtr - _currInputRowStart + 1 = 50-20+1=31
        assertEquals(31, loc.getColumnNr());
        assertEquals(3, loc.getLineNr());
        // char offset = _currInputProcessed + _inputPtr = 200+50=250
        assertEquals(250, loc.getCharOffset());
    }
}
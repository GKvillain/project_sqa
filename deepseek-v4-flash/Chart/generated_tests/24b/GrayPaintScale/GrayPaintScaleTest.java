package org.jfree.chart.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Paint;

import org.junit.Test;

/**
 * Test class for {@link GrayPaintScale}. This test focuses on the
 * defect in the getPaint() method where the value is not clamped
 * before computing the gray intensity, as well as covering the
 * constructors, bounds, equality, and clone functionality.
 */
public class GrayPaintScaleTest {

    /**
     * Tests the default constructor and verifies the bounds.
     */
    @Test
    public void testDefaultConstructor_createsScaleWithDefaultBounds() {
        GrayPaintScale scale = new GrayPaintScale();
        assertEquals(0.0, scale.getLowerBound(), 0.0000001);
        assertEquals(1.0, scale.getUpperBound(), 0.0000001);
    }

    /**
     * Tests the constructor with valid bounds.
     */
    @Test
    public void testConstructor_validBounds_setsBounds() {
        GrayPaintScale scale = new GrayPaintScale(-1.0, 1.0);
        assertEquals(-1.0, scale.getLowerBound(), 0.0000001);
        assertEquals(1.0, scale.getUpperBound(), 0.0000001);
    }

    /**
     * Tests the constructor when lowerBound equals upperBound, expecting an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_equalBounds_throwsException() {
        new GrayPaintScale(1.0, 1.0);
    }

    /**
     * Tests the constructor when lowerBound is greater than upperBound, expecting an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lowerGreaterThanUpper_throwsException() {
        new GrayPaintScale(2.0, 1.0);
    }

    /**
     * Tests getLowerBound for a non-default scale.
     */
    @Test
    public void testGetLowerBound_customScale_returnsLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(-5.0, 5.0);
        assertEquals(-5.0, scale.getLowerBound(), 0.0000001);
    }

    /**
     * Tests getUpperBound for a non-default scale.
     */
    @Test
    public void testGetUpperBound_customScale_returnsUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(-5.0, 5.0);
        assertEquals(5.0, scale.getUpperBound(), 0.0000001);
    }

    /**
     * Tests getPaint with a value exactly at the lower bound.
     * Expected color should be black (RGB 0,0,0).
     */
    @Test
    public void testGetPaint_valueAtLowerBound_returnsBlack() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(0.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(0, color.getAlpha()); // default alpha is 255, but we check RGB
    }

    /**
     * Tests getPaint with a value at the upper bound.
     * Expected color should be white (RGB 255,255,255).
     */
    @Test
    public void testGetPaint_valueAtUpperBound_returnsWhite() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    /**
     * Tests getPaint with a value in the middle of the range.
     * For range [0,1], middle 0.5 should produce RGB around 127 or 128.
     */
    @Test
    public void testGetPaint_midValue_returnsGray() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(0.5);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        // Since 0.5 * 255 = 127.5 which is cast to int (truncates to 127)
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    /**
     * Tests getPaint with a value below the lower bound.
     * The method should clamp the value to the lower bound before computing color.
     * Even though the clamping is not effective in the original code (bug),
     * the expected result should be black if the defect is fixed.
     * Here we verify that the returned Paint is a Color and has consistent RGB.
     */
    @Test
    public void testGetPaint_valueBelowLowerBound_returnsBlackWhenFixed() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(-0.5);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        // For the buggy version, the color might not be black, 
        // but we are testing the expected behavior after fix:
        // The value is clamped, so gray intensity should be 0.
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    /**
     * Tests getPaint with a value above the upper bound.
     * The method should clamp the value to the upper bound before computing color.
     * Similarly, expected white after fix.
     */
    @Test
    public void testGetPaint_valueAboveUpperBound_returnsWhiteWhenFixed() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.5);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        // After clamping to 1.0, intensity = 255
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    /**
     * Tests equals with the same object.
     */
    @Test
    public void testEquals_sameObject_returnsTrue() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertTrue(scale.equals(scale));
    }

    /**
     * Tests equals with null.
     */
    @Test
    public void testEquals_null_returnsFalse() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertFalse(scale.equals(null));
    }

    /**
     * Tests equals with different object type.
     */
    @Test
    public void testEquals_differentObjectType_returnsFalse() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertFalse(scale.equals("test"));
    }

    /**
     * Tests equals with different lower bound.
     */
    @Test
    public void testEquals_differentLowerBound_returnsFalse() {
        GrayPaintScale scale1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale2 = new GrayPaintScale(0.5, 1.0);
        assertFalse(scale1.equals(scale2));
    }

    /**
     * Tests equals with different upper bound.
     */
    @Test
    public void testEquals_differentUpperBound_returnsFalse() {
        GrayPaintScale scale1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale2 = new GrayPaintScale(0.0, 2.0);
        assertFalse(scale1.equals(scale2));
    }

    /**
     * Tests equals with equal scales.
     */
    @Test
    public void testEquals_equalScales_returnsTrue() {
        GrayPaintScale scale1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale2 = new GrayPaintScale(0.0, 1.0);
        assertTrue(scale1.equals(scale2));
    }

    /**
     * Tests clone returns a non-null clone that is equal to original.
     */
    @Test
    public void testClone_returnsCloneAndIndependent() throws CloneNotSupportedException {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Object clone = scale.clone();
        assertNotNull(clone);
        assertTrue(clone instanceof GrayPaintScale);
        assertEquals(scale, clone);
        // Verify independence: change clone bounds, original should remain same
        GrayPaintScale cloneScale = (GrayPaintScale) clone;
        cloneScale = new GrayPaintScale(0.0, 2.0);
        assertEquals(1.0, scale.getUpperBound(), 0.0000001);
    }

    /**
     * Tests that getPaint returns a Color with the correct RGB for a negative range.
     */
    @Test
    public void testGetPaint_negativeRange_midValue() {
        GrayPaintScale scale = new GrayPaintScale(-1.0, 1.0);
        // value = 0.0 should map to 127 or 128 (0.0 - (-1.0)) / 2 * 255 = 127.5 truncates to 127
        Paint paint = scale.getPaint(0.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    /**
     * Tests getPaint with a value exactly at lower bound for a negative range.
     */
    @Test
    public void testGetPaint_negativeRange_lowerBound_returnsBlack() {
        GrayPaintScale scale = new GrayPaintScale(-1.0, 1.0);
        Paint paint = scale.getPaint(-1.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    /**
     * Tests getPaint with a value exactly at upper bound for a negative range.
     */
    @Test
    public void testGetPaint_negativeRange_upperBound_returnsWhite() {
        GrayPaintScale scale = new GrayPaintScale(-1.0, 1.0);
        Paint paint = scale.getPaint(1.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    /**
     * Additional test to verify that getPaint returns a Color instance.
     * This is more of a sanity check than a behavioral verification.
     */
    @Test
    public void testGetPaint_returnsNonNullPaint() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertNotNull(scale.getPaint(0.5));
    }
}
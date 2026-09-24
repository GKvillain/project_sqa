package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class GrayPaintScaleTest {

    // Tests default constructor initializes bounds to 0.0 and 1.0
    @Test
    public void testConstructor_default_setsDefaultBounds() {
        GrayPaintScale gps = new GrayPaintScale();
        assertEquals(0.0, gps.getLowerBound(), 0.00001);
        assertEquals(1.0, gps.getUpperBound(), 0.00001);
    }

    // Tests custom constructor sets bounds correctly
    @Test
    public void testConstructor_validBounds_setsBoundsCorrectly() {
        GrayPaintScale gps = new GrayPaintScale(10.0, 50.0);
        assertEquals(10.0, gps.getLowerBound(), 0.00001);
        assertEquals(50.0, gps.getUpperBound(), 0.00001);
    }

    // Tests constructor throws exception when lowerBound equals upperBound
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lowerBoundEqualsUpperBound_throwsException() {
        new GrayPaintScale(5.0, 5.0);
    }

    // Tests constructor throws exception when lowerBound is greater than upperBound
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lowerBoundGreaterThanUpperBound_throwsException() {
        new GrayPaintScale(10.0, 5.0);
    }

    // Tests getPaint returns black at lowerBound
    @Test
    public void testGetPaint_valueAtLowerBound_returnsBlack() {
        GrayPaintScale gps = new GrayPaintScale(0.0, 1.0);
        Color paint = (Color) gps.getPaint(0.0);
        assertEquals(new Color(0, 0, 0), paint);
    }

    // Tests getPaint returns white at upperBound
    @Test
    public void testGetPaint_valueAtUpperBound_returnsWhite() {
        GrayPaintScale gps = new GrayPaintScale(0.0, 1.0);
        Color paint = (Color) gps.getPaint(1.0);
        assertEquals(new Color(255, 255, 255), paint);
    }

    // Tests getPaint returns intermediate gray at midpoint
    @Test
    public void testGetPaint_midpointValue_returnsCorrectGray() {
        GrayPaintScale gps = new GrayPaintScale(0.0, 1.0);
        Color paint = (Color) gps.getPaint(0.5);
        assertEquals(new Color(127, 127, 127), paint);
    }

    // Tests getPaint clamps values below lowerBound to lowerBound color
    @Test
    public void testGetPaint_valueBelowLowerBound_returnsClampedColor() {
        GrayPaintScale gps = new GrayPaintScale(0.0, 1.0);
        Color paint = (Color) gps.getPaint(-0.5);
        assertEquals(new Color(0, 0, 0), paint);
    }

    // Tests getPaint clamps values above upperBound to upperBound color
    @Test
    public void testGetPaint_valueAboveUpperBound_returnsClampedColor() {
        GrayPaintScale gps = new GrayPaintScale(0.0, 1.0);
        Color paint = (Color) gps.getPaint(1.5);
        assertEquals(new Color(255, 255, 255), paint);
    }

    // Tests getPaint works properly with non-zero and negative bounds
    @Test
    public void testGetPaint_negativeRange_returnsClampedAndCorrectColors() {
        GrayPaintScale gps = new GrayPaintScale(-10.0, 10.0);
        assertEquals(new Color(0, 0, 0), (Color) gps.getPaint(-15.0));
        assertEquals(new Color(0, 0, 0), (Color) gps.getPaint(-10.0));
        assertEquals(new Color(127, 127, 127), (Color) gps.getPaint(0.0));
        assertEquals(new Color(255, 255, 255), (Color) gps.getPaint(10.0));
        assertEquals(new Color(255, 255, 255), (Color) gps.getPaint(20.0));
    }

    // Tests equals for same instance reflexivity
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        GrayPaintScale gps = new GrayPaintScale();
        assertTrue(gps.equals(gps));
    }

    // Tests equals for two distinct instances with same bounds
    @Test
    public void testEquals_equalInstances_returnsTrue() {
        GrayPaintScale gps1 = new GrayPaintScale(10.0, 20.0);
        GrayPaintScale gps2 = new GrayPaintScale(10.0, 20.0);
        assertTrue(gps1.equals(gps2));
        assertTrue(gps2.equals(gps1));
    }

    // Tests equals returns false for null
    @Test
    public void testEquals_nullInput_returnsFalse() {
        GrayPaintScale gps = new GrayPaintScale();
        assertFalse(gps.equals(null));
    }

    // Tests equals returns false for different object type
    @Test
    public void testEquals_differentObjectType_returnsFalse() {
        GrayPaintScale gps = new GrayPaintScale();
        assertFalse(gps.equals("NotAGrayPaintScale"));
    }

    // Tests equals returns false when lowerBounds differ
    @Test
    public void testEquals_differentLowerBound_returnsFalse() {
        GrayPaintScale gps1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale gps2 = new GrayPaintScale(0.1, 1.0);
        assertFalse(gps1.equals(gps2));
    }

    // Tests equals returns false when upperBounds differ
    @Test
    public void testEquals_differentUpperBound_returnsFalse() {
        GrayPaintScale gps1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale gps2 = new GrayPaintScale(0.0, 1.5);
        assertFalse(gps1.equals(gps2));
    }

    // Tests clone creates an independent equal instance
    @Test
    public void testClone_validInstance_returnsEqualCopy() throws CloneNotSupportedException {
        GrayPaintScale gps1 = new GrayPaintScale(10.0, 20.0);
        GrayPaintScale gps2 = (GrayPaintScale) gps1.clone();
        assertNotSame(gps1, gps2);
        assertEquals(gps1.getClass(), gps2.getClass());
        assertEquals(gps1, gps2);
    }

    // Tests serialization and deserialization preserves equality
    @Test
    public void testSerialization_validInstance_preservesEquality() throws Exception {
        GrayPaintScale gps1 = new GrayPaintScale(5.0, 25.0);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(gps1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        GrayPaintScale gps2 = (GrayPaintScale) in.readObject();
        in.close();

        assertEquals(gps1, gps2);
    }
}
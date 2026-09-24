package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ValueMarker} class.
 */
public class ValueMarkerTest {

    private static final double EPSILON = 0.000000001;

    // Tests 1-argument constructor sets the value properly
    @Test
    public void testConstructor_singleValue_setsValue() {
        ValueMarker marker = new ValueMarker(45.5);
        assertEquals(45.5, marker.getValue(), EPSILON);
    }

    // Tests 3-argument constructor sets value, paint, stroke, and default alpha
    @Test
    public void testConstructor_threeArguments_setsExpectedProperties() {
        Paint paint = Color.red;
        Stroke stroke = new BasicStroke(1.0f);
        ValueMarker marker = new ValueMarker(10.0, paint, stroke);

        assertEquals(10.0, marker.getValue(), EPSILON);
        assertEquals(paint, marker.getPaint());
        assertEquals(stroke, marker.getStroke());
        assertEquals(paint, marker.getOutlinePaint());
        assertEquals(stroke, marker.getOutlineStroke());
        assertEquals(1.0f, marker.getAlpha(), 0.0001f);
    }

    // Tests 6-argument constructor with distinct outline paint and outline stroke
    @Test
    public void testConstructor_sixArguments_setsDistinctOutlinePaintAndStroke() {
        Paint paint = Color.red;
        Stroke stroke = new BasicStroke(1.0f);
        Paint outlinePaint = Color.blue;
        Stroke outlineStroke = new BasicStroke(2.0f);
        float alpha = 0.75f;

        ValueMarker marker = new ValueMarker(25.0, paint, stroke, outlinePaint, outlineStroke, alpha);

        assertEquals(25.0, marker.getValue(), EPSILON);
        assertEquals(paint, marker.getPaint());
        assertEquals(stroke, marker.getStroke());
        assertEquals(outlinePaint, marker.getOutlinePaint());
        assertEquals(outlineStroke, marker.getOutlineStroke());
        assertEquals(alpha, marker.getAlpha(), 0.0001f);
    }

    // Tests 6-argument constructor with null outline paint and null outline stroke
    @Test
    public void testConstructor_nullOutlinePaintAndStroke_allowed() {
        Paint paint = Color.green;
        Stroke stroke = new BasicStroke(1.5f);
        ValueMarker marker = new ValueMarker(100.0, paint, stroke, null, null, 0.5f);

        assertEquals(100.0, marker.getValue(), EPSILON);
        assertEquals(paint, marker.getPaint());
        assertEquals(stroke, marker.getStroke());
        assertNull(marker.getOutlinePaint());
        assertNull(marker.getOutlineStroke());
        assertEquals(0.5f, marker.getAlpha(), 0.0001f);
    }

    // Tests getValue method returns current value
    @Test
    public void testGetValue_negativeValue_returnsCorrectValue() {
        ValueMarker marker = new ValueMarker(-99.9);
        assertEquals(-99.9, marker.getValue(), EPSILON);
    }

    // Tests setValue updates the value
    @Test
    public void testSetValue_newValue_updatesValue() {
        ValueMarker marker = new ValueMarker(10.0);
        marker.setValue(20.0);
        assertEquals(20.0, marker.getValue(), EPSILON);
    }

    // Tests setValue notifies registered MarkerChangeListener
    @Test
    public void testSetValue_listenerRegistered_triggersNotification() {
        ValueMarker marker = new ValueMarker(10.0);
        final boolean[] notified = new boolean[]{false};

        marker.addChangeListener(new MarkerChangeListener() {
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
            }
        });

        marker.setValue(30.0);
        assertTrue(notified[0]);
    }

    // Tests equals method with same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ValueMarker marker = new ValueMarker(50.0);
        assertTrue(marker.equals(marker));
    }

    // Tests equals method with null object
    @Test
    public void testEquals_nullObject_returnsFalse() {
        ValueMarker marker = new ValueMarker(50.0);
        assertFalse(marker.equals(null));
    }

    // Tests equals method with different object type
    @Test
    public void testEquals_differentType_returnsFalse() {
        ValueMarker marker = new ValueMarker(50.0);
        assertFalse(marker.equals("Not a ValueMarker"));
    }

    // Tests equals method with identical properties
    @Test
    public void testEquals_identicalObjects_returnsTrue() {
        ValueMarker m1 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f));
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));
    }

    // Tests equals method with different value
    @Test
    public void testEquals_differentValue_returnsFalse() {
        ValueMarker m1 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(50.1, Color.black, new BasicStroke(1.0f));
        assertFalse(m1.equals(m2));
    }

    // Tests equals method with different paint
    @Test
    public void testEquals_differentPaint_returnsFalse() {
        ValueMarker m1 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(50.0, Color.white, new BasicStroke(1.0f));
        assertFalse(m1.equals(m2));
    }

    // Tests equals method with different outline paint
    @Test
    public void testEquals_differentOutlinePaint_returnsFalse() {
        ValueMarker m1 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f), Color.red, new BasicStroke(1.0f), 1.0f);
        ValueMarker m2 = new ValueMarker(50.0, Color.black, new BasicStroke(1.0f), Color.blue, new BasicStroke(1.0f), 1.0f);
        assertFalse(m1.equals(m2));
    }
}
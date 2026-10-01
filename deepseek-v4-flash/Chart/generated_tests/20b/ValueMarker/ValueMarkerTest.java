package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import static org.junit.Assert.*;
import org.junit.Test;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;

public class ValueMarkerTest {

    // Tests single-value constructor
    @Test
    public void testConstructor_oneArg_setsValue() {
        ValueMarker m = new ValueMarker(5.0);
        assertEquals(5.0, m.getValue(), 0.0);
    }

    // Tests two-argument constructor with paint and stroke
    @Test
    public void testConstructor_twoArg_setsValue() {
        ValueMarker m = new ValueMarker(3.5, Color.RED, new BasicStroke(1.0f));
        assertEquals(3.5, m.getValue(), 0.0);
    }

    // Tests full constructor sets value
    @Test
    public void testConstructor_fullArg_setsValue() {
        ValueMarker m = new ValueMarker(7.0, Color.RED, new BasicStroke(1.0f),
                Color.BLUE, new BasicStroke(2.0f), 0.5f);
        assertEquals(7.0, m.getValue(), 0.0);
    }

    // Tests full constructor sets paint and stroke correctly
    @Test
    public void testConstructor_fullArg_setsPaintAndStroke() {
        ValueMarker m = new ValueMarker(7.0, Color.RED, new BasicStroke(1.0f),
                Color.BLUE, new BasicStroke(2.0f), 0.5f);
        assertSame(Color.RED, m.getPaint());
        assertEquals(1.0f, ((BasicStroke) m.getStroke()).getLineWidth(), 0.0f);
    }

    // Tests full constructor sets outline paint and outline stroke correctly
    @Test
    public void testConstructor_fullArg_setsOutlinePaintAndStroke() {
        ValueMarker m = new ValueMarker(7.0, Color.RED, new BasicStroke(1.0f),
                Color.BLUE, new BasicStroke(2.0f), 0.5f);
        assertSame(Color.BLUE, m.getOutlinePaint());
        assertEquals(2.0f, ((BasicStroke) m.getOutlineStroke()).getLineWidth(), 0.0f);
    }

    // Tests zero value bound
    @Test
    public void testConstructor_zeroValue_setsValue() {
        ValueMarker m = new ValueMarker(0.0);
        assertEquals(0.0, m.getValue(), 0.0);
    }

    // Tests negative value bound
    @Test
    public void testConstructor_negativeValue_setsValue() {
        ValueMarker m = new ValueMarker(-2.5);
        assertEquals(-2.5, m.getValue(), 0.0);
    }

    // Tests max double value
    @Test
    public void testConstructor_doubleMaxValue_setsValue() {
        ValueMarker m = new ValueMarker(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, m.getValue(), 0.0);
    }

    // Tests min double value
    @Test
    public void testConstructor_doubleMinValue_setsValue() {
        ValueMarker m = new ValueMarker(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, m.getValue(), 0.0);
    }

    // Tests getValue returns initial value
    @Test
    public void testGetValue_initialValue_returnsIt() {
        ValueMarker m = new ValueMarker(42.0);
        assertEquals(42.0, m.getValue(), 0.0);
    }

    // Tests setValue changes value
    @Test
    public void testSetValue_changesValue() {
        ValueMarker m = new ValueMarker(1.0);
        m.setValue(99.0);
        assertEquals(99.0, m.getValue(), 0.0);
    }

    // Tests setValue to zero
    @Test
    public void testSetValue_zeroValue_updatesValue() {
        ValueMarker m = new ValueMarker(5.0);
        m.setValue(0.0);
        assertEquals(0.0, m.getValue(), 0.0);
    }

    // Tests setValue triggers listener notification
    @Test
    public void testSetValue_notifiesListeners() {
        ValueMarker m = new ValueMarker(1.0);
        final int[] count = {0};
        m.addChangeListener(new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                count[0]++;
            }
        });
        m.setValue(2.0);
        assertEquals(1, count[0]);
    }

    // Tests equals with same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        ValueMarker m = new ValueMarker(1.0);
        assertTrue(m.equals(m));
    }

    // Tests equals between equal default markers
    @Test
    public void testEquals_equalValueMarkers_returnsTrue() {
        ValueMarker m1 = new ValueMarker(1.0);
        ValueMarker m2 = new ValueMarker(1.0);
        assertTrue(m1.equals(m2));
    }

    // Tests equals between equally constructed markers
    @Test
    public void testEquals_equalConstructedMarkers_returnsTrue() {
        ValueMarker m1 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        assertTrue(m1.equals(m2));
    }

    // Tests equals with different value
    @Test
    public void testEquals_differentValue_returnsFalse() {
        ValueMarker m1 = new ValueMarker(1.0);
        ValueMarker m2 = new ValueMarker(2.0);
        assertFalse(m1.equals(m2));
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        ValueMarker m = new ValueMarker(1.0);
        assertFalse(m.equals(null));
    }

    // Tests equals with non-ValueMarker object
    @Test
    public void testEquals_nonValueMarker_returnsFalse() {
        ValueMarker m = new ValueMarker(1.0);
        assertFalse(m.equals("some string"));
    }

    // Tests equals when value same but paint different
    @Test
    public void testEquals_sameValueButDifferentPaint_returnsFalse() {
        ValueMarker m1 = new ValueMarker(1.0);
        ValueMarker m2 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        assertFalse(m1.equals(m2));
    }

    // Tests equals when value same but stroke different
    @Test
    public void testEquals_sameValueButDifferentStroke_returnsFalse() {
        ValueMarker m1 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(1.0, Color.RED, new BasicStroke(2.0f));
        assertFalse(m1.equals(m2));
    }
}
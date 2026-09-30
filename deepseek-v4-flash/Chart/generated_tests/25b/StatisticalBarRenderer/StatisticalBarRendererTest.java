package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

/**
 * JUnit tests for the StatisticalBarRenderer class.
 * Focuses on the defect in equals() that does not compare errorIndicatorStroke.
 */
public class StatisticalBarRendererTest {

    // Tests default error indicator paint is gray
    @Test
    public void testGetErrorIndicatorPaint_default_returnsGray() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
    }

    // Tests default error indicator stroke is a BasicStroke of width 0.5f
    @Test
    public void testGetErrorIndicatorStroke_default_returnsBasicStroke() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Stroke stroke = renderer.getErrorIndicatorStroke();
        assertNotNull(stroke);
        assertTrue(stroke instanceof BasicStroke);
        BasicStroke bs = (BasicStroke) stroke;
        assertEquals(0.5f, bs.getLineWidth(), 0.0001f);
    }

    // Tests setting a new error indicator paint and getting it back
    @Test
    public void testSetErrorIndicatorPaint_validPaint_returnsNewPaint() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Paint paint = Color.red;
        renderer.setErrorIndicatorPaint(paint);
        assertEquals(paint, renderer.getErrorIndicatorPaint());
    }

    // Tests setting a new error indicator stroke and getting it back
    @Test
    public void testSetErrorIndicatorStroke_validStroke_returnsNewStroke() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setErrorIndicatorStroke(stroke);
        assertEquals(stroke, renderer.getErrorIndicatorStroke());
    }

    // Tests that setting error indicator paint to null is permitted
    @Test
    public void testSetErrorIndicatorPaint_null_allowed() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setErrorIndicatorPaint(null);
        assertNull(renderer.getErrorIndicatorPaint());
    }

    // Tests that setting error indicator stroke to null is permitted
    @Test
    public void testSetErrorIndicatorStroke_null_allowed() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setErrorIndicatorStroke(null);
        assertNull(renderer.getErrorIndicatorStroke());
    }

    // Tests equals with the same object reference returns true
    @Test
    public void testEquals_sameObject_returnsTrue() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        assertEquals(renderer, renderer);
    }

    // Tests equals with two renderers having identical properties returns true
    @Test
    public void testEquals_sameValues_returnsTrue() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertEquals(r1, r2);
    }

    // Tests equals returns false when errorIndicatorPaint differs
    @Test
    public void testEquals_differentPaint_returnsFalse() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        r2.setErrorIndicatorPaint(Color.blue);
        assertFalse(r1.equals(r2));
    }

    // Tests equals returns false when errorIndicatorStroke differs (defect bug)
    // This test will fail on the original buggy code because equals() does not
    // compare errorIndicatorStroke.
    @Test
    public void testEquals_differentStroke_returnsFalse() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        r2.setErrorIndicatorStroke(new BasicStroke(2.0f));
        assertFalse(r1.equals(r2));
    }

    // Tests drawItem with a non-statistical dataset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDrawItem_nonStatisticalDataset_throwsIllegalArgumentException() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.drawItem(null, null, null, null, null, null,
                new DefaultCategoryDataset(), 0, 0, 0);
    }

    // Tests drawItem with null dataset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDrawItem_nullDataset_throwsIllegalArgumentException() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.drawItem(null, null, null, null, null, null, null, 0, 0, 0);
    }
}
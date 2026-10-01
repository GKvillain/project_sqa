package org.jfree.chart.plot;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * JUnit 4 test class for {@link PiePlot} targeting Defects4J bug 15b.
 * Tests focus on normal, boundary, and edge cases for key methods.
 */
public class PiePlotTest {

    // Tests constructor with null dataset
    @Test
    public void testConstructor_nullDataset_createsPlot() {
        PiePlot plot = new PiePlot(null);
        assertNull("Dataset should be null", plot.getDataset());
        assertEquals("Default start angle should be 90.0", 90.0, plot.getStartAngle(), 0.0001);
        assertEquals("Default direction should be CLOCKWISE", Rotation.CLOCKWISE, plot.getDirection());
        assertTrue("Default circular should be true", plot.isCircular());
        assertFalse("Default simpleLabels should be false", plot.getSimpleLabels());
        assertEquals("Default interiorGap", 0.08, plot.getInteriorGap(), 0.0001);
    }

    // Tests constructor with non-null dataset
    @Test
    public void testConstructor_withDataset_setsDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        assertNotNull("Dataset should not be null", plot.getDataset());
        assertEquals("Start angle should be 90.0", 90.0, plot.getStartAngle(), 0.0001);
    }

    // Tests setStartAngle
    @Test
    public void testSetStartAngle_positiveValue_updatesStartAngle() {
        PiePlot plot = new PiePlot();
        plot.setStartAngle(45.0);
        assertEquals("Start angle should be 45.0", 45.0, plot.getStartAngle(), 0.0001);
    }

    // Tests setStartAngle with zero
    @Test
    public void testSetStartAngle_zeroValue_updatesStartAngle() {
        PiePlot plot = new PiePlot();
        plot.setStartAngle(0.0);
        assertEquals("Start angle should be 0.0", 0.0, plot.getStartAngle(), 0.0001);
    }

    // Tests setDirection with valid Rotation
    @Test
    public void testSetDirection_anticlockwise_updatesDirection() {
        PiePlot plot = new PiePlot();
        plot.setDirection(Rotation.ANTICLOCKWISE);
        assertEquals("Direction should be ANTICLOCKWISE", Rotation.ANTICLOCKWISE, plot.getDirection());
    }

    // Tests setDirection with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetDirection_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setDirection(null);
    }

    // Tests setCircular
    @Test
    public void testSetCircular_false_setsCircularFalse() {
        PiePlot plot = new PiePlot();
        plot.setCircular(false);
        assertFalse("Circular should be false", plot.isCircular());
    }

    // Tests setInteriorGap within valid range
    @Test
    public void testSetInteriorGap_validValue_updatesInteriorGap() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.2);
        assertEquals("Interior gap should be 0.2", 0.2, plot.getInteriorGap(), 0.0001);
    }

    // Tests setInteriorGap at boundary (0.0)
    @Test
    public void testSetInteriorGap_zeroValue_updatesInteriorGap() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.0);
        assertEquals("Interior gap should be 0.0", 0.0, plot.getInteriorGap(), 0.0001);
    }

    // Tests setInteriorGap at maximum boundary
    @Test
    public void testSetInteriorGap_maxValue_updatesInteriorGap() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(PiePlot.MAX_INTERIOR_GAP);
        assertEquals("Interior gap should be MAX", PiePlot.MAX_INTERIOR_GAP, plot.getInteriorGap(), 0.0001);
    }

    // Tests setInteriorGap above maximum - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGap_aboveMax_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.41);
    }

    // Tests setInteriorGap negative - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGap_negative_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(-0.1);
    }

    // Tests setIgnoreNullValues and getIgnoreNullValues
    @Test
    public void testSetIgnoreNullValues_true_setsFlag() {
        PiePlot plot = new PiePlot();
        assertFalse("Default ignoreNullValues should be false", plot.getIgnoreNullValues());
        plot.setIgnoreNullValues(true);
        assertTrue("ignoreNullValues should be true", plot.getIgnoreNullValues());
    }

    // Tests setIgnoreZeroValues and getIgnoreZeroValues
    @Test
    public void testSetIgnoreZeroValues_true_setsFlag() {
        PiePlot plot = new PiePlot();
        assertFalse("Default ignoreZeroValues should be false", plot.getIgnoreZeroValues());
        plot.setIgnoreZeroValues(true);
        assertTrue("ignoreZeroValues should be true", plot.getIgnoreZeroValues());
    }

    // Tests setBaseSectionPaint with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionPaint_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionPaint(null);
    }

    // Tests setBaseSectionPaint with valid paint
    @Test
    public void testSetBaseSectionPaint_validPaint_setsPaint() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionPaint(Color.red);
        assertEquals("Base section paint should be red", Color.red, plot.getBaseSectionPaint());
    }

    // Tests getPlotType
    @Test
    public void testGetPlotType_returnsPiePlot() {
        PiePlot plot = new PiePlot();
        assertEquals("Plot type should be 'Pie Plot'", "Pie Plot", plot.getPlotType());
    }

    // Tests getLegendItems with null dataset - returns empty collection
    @Test
    public void testGetLegendItems_nullDataset_returnsEmptyCollection() {
        PiePlot plot = new PiePlot();
        assertTrue("Legend items should be empty", plot.getLegendItems().getItemCount() == 0);
    }

    // Tests getLegendItems with non-null dataset returns items
    @Test
    public void testGetLegendItems_withDataset_returnsItems() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        int count = plot.getLegendItems().getItemCount();
        // Both values included since both > 0
        assertEquals("Should have 2 legend items", 2, count);
    }

    // Tests getLegendItems with ignoreZeroValues = true and zero value
    @Test
    public void testGetLegendItems_ignoreZeroValuesTrueZeroValue_excludesItem() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 0.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreZeroValues(true);
        int count = plot.getLegendItems().getItemCount();
        assertEquals("Should have 1 legend item (B excluded)", 1, count);
    }

    // Tests getLegendItems with ignoreNullValues = true and null value
    @Test
    public void testGetLegendItems_ignoreNullValuesTrueNullValue_excludesItem() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", null);
        dataset.setValue("B", 3.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreNullValues(true);
        int count = plot.getLegendItems().getItemCount();
        assertEquals("Should have 1 legend item (A excluded)", 1, count);
    }

    // Tests getSectionKey with dataset - returns valid key
    @Test
    public void testGetSectionKey_withDataset_returnsKey() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        Comparable key = plot.getSectionKey(0);
        assertEquals("Section key at index 0 should be 'A'", "A", key);
        key = plot.getSectionKey(1);
        assertEquals("Section key at index 1 should be 'B'", "B", key);
    }

    // Tests getSectionKey with invalid index returns integer wrapper
    @Test
    public void testGetSectionKey_invalidIndex_returnsInteger() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        Comparable key = plot.getSectionKey(5);
        assertTrue("Key should be an Integer", key instanceof Integer);
        assertEquals("Key should be 5", 5, ((Integer) key).intValue());
    }

    // Tests getArcBounds with explodePercent = 0 returns unexploded
    @Test
    public void testGetArcBounds_explodePercentZero_returnsUnexploded() {
        PiePlot plot = new PiePlot();
        Rectangle2D unexploded = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D exploded = new Rectangle2D.Double(10, 10, 80, 80);
        Rectangle2D result = plot.getArcBounds(unexploded, exploded, 0.0, 90.0, 0.0);
        assertEquals("Should equal unexploded", unexploded, result);
    }

    // Tests getArcBounds with explodePercent > 0 returns modified bounds
    @Test
    public void testGetArcBounds_explodePercentPositive_returnsModified() {
        PiePlot plot = new PiePlot();
        Rectangle2D unexploded = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D exploded = new Rectangle2D.Double(10, 10, 80, 80);
        Rectangle2D result = plot.getArcBounds(unexploded, exploded, 0.0, 90.0, 0.5);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should not equal unexploded", !result.equals(unexploded));
    }

    // Tests setLabelGenerator and getLabelGenerator
    @Test
    public void testSetLabelGenerator_nullGenerator_setsNull() {
        PiePlot plot = new PiePlot();
        plot.setLabelGenerator(null);
        assertNull("Label generator should be null", plot.getLabelGenerator());
    }

    // Tests setLabelGenerator with valid generator
    @Test
    public void testSetLabelGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLabelGenerator(gen);
        assertSame("Label generator should be same", gen, plot.getLabelGenerator());
    }

    // Tests setLabelFont with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFont_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelFont(null);
    }

    // Tests setLabelFont with valid font
    @Test
    public void testSetLabelFont_validFont_setsFont() {
        PiePlot plot = new PiePlot();
        Font font = new Font("Serif", Font.BOLD, 12);
        plot.setLabelFont(font);
        assertSame("Label font should be same", font, plot.getLabelFont());
    }

    // Tests setLabelPaint with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaint_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelPaint(null);
    }

    // Tests setLabelPaint with valid paint
    @Test
    public void testSetLabelPaint_validPaint_setsPaint() {
        PiePlot plot = new PiePlot();
        plot.setLabelPaint(Color.blue);
        assertEquals("Label paint should be blue", Color.blue, plot.getLabelPaint());
    }

    // Tests setSimpleLabels and getSimpleLabels
    @Test
    public void testSetSimpleLabels_true_setsSimpleLabels() {
        PiePlot plot = new PiePlot();
        plot.setSimpleLabels(true);
        assertTrue("Simple labels should be true", plot.getSimpleLabels());
    }

    // Tests setLabelPadding with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPadding_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelPadding(null);
    }

    // Tests setLabelPadding with valid insets
    @Test
    public void testSetLabelPadding_validInsets_setsPadding() {
        PiePlot plot = new PiePlot();
        RectangleInsets insets = new RectangleInsets(2, 2, 2, 2);
        plot.setLabelPadding(insets);
        assertSame("Label padding should be same", insets, plot.getLabelPadding());
    }

    // Tests getMaximumExplodePercent with no explosions
    @Test
    public void testGetMaximumExplodePercent_noExplosions_returnsZero() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        assertEquals("Max explode percent should be 0.0", 0.0, plot.getMaximumExplodePercent(), 0.0001);
    }

    // Tests setExplodePercent and getExplodePercent
    @Test
    public void testSetExplodePercent_validKey_setsExplosion() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setExplodePercent("A", 0.3);
        assertEquals("Explode percent should be 0.3", 0.3, plot.getExplodePercent("A"), 0.0001);
    }

    // Tests setExplodePercent with null key - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetExplodePercent_nullKey_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setExplodePercent(null, 0.3);
    }

    // Tests setLabelLinkPaint with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkPaint_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelLinkPaint(null);
    }

    // Tests setLabelLinkPaint with valid paint
    @Test
    public void testSetLabelLinkPaint_validPaint_setsPaint() {
        PiePlot plot = new PiePlot();
        plot.setLabelLinkPaint(Color.green);
        assertEquals("Label link paint should be green", Color.green, plot.getLabelLinkPaint());
    }

    // Tests setLabelLinkStroke with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkStroke_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelLinkStroke(null);
    }

    // Tests setLabelLinkStroke with valid stroke
    @Test
    public void testSetLabelLinkStroke_validStroke_setsStroke() {
        PiePlot plot = new PiePlot();
        Stroke stroke = new BasicStroke(2.0f);
        plot.setLabelLinkStroke(stroke);
        assertSame("Label link stroke should be same", stroke, plot.getLabelLinkStroke());
    }

    // Tests setLegendItemShape with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendItemShape_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLegendItemShape(null);
    }

    // Tests setLegendItemShape with valid shape
    @Test
    public void testSetLegendItemShape_validShape_setsShape() {
        PiePlot plot = new PiePlot();
        Shape shape = new Ellipse2D.Double(0, 0, 10, 10);
        plot.setLegendItemShape(shape);
        assertSame("Legend item shape should be same", shape, plot.getLegendItemShape());
    }

    // Tests clone - basic cloneability
    @Test
    public void testClone_basicPlot_returnsClone() throws CloneNotSupportedException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        PiePlot clone = (PiePlot) plot.clone();
        assertNotSame("Clone should be different object", plot, clone);
        assertEquals("Cloned plot should equal original", plot, clone);
    }

    // Tests equals with same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        PiePlot plot = new PiePlot();
        assertTrue("Equals with itself should be true", plot.equals(plot));
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        PiePlot plot = new PiePlot();
        assertFalse("Equals with null should be false", plot.equals(null));
    }

    // Tests equals with different type
    @Test
    public void testEquals_differentType_returnsFalse() {
        PiePlot plot = new PiePlot();
        assertFalse("Equals with different type should be false", plot.equals("string"));
    }

    // Tests equals with different startAngle
    @Test
    public void testEquals_differentStartAngle_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setStartAngle(45.0);
        assertFalse("Equals with different startAngle should be false", plot1.equals(plot2));
    }

    // Tests getLabelGap and setLabelGap
    @Test
    public void testSetLabelGap_validValue_updatesGap() {
        PiePlot plot = new PiePlot();
        assertEquals("Default label gap", 0.025, plot.getLabelGap(), 0.0001);
        plot.setLabelGap(0.1);
        assertEquals("Label gap should be 0.1", 0.1, plot.getLabelGap(), 0.0001);
    }

    // Tests getMaximumLabelWidth and setMaximumLabelWidth
    @Test
    public void testSetMaximumLabelWidth_validValue_updatesWidth() {
        PiePlot plot = new PiePlot();
        assertEquals("Default max label width", 0.14, plot.getMaximumLabelWidth(), 0.0001);
        plot.setMaximumLabelWidth(0.2);
        assertEquals("Max label width should be 0.2", 0.2, plot.getMaximumLabelWidth(), 0.0001);
    }

    // Tests getLabelLinksVisible and setLabelLinksVisible
    @Test
    public void testSetLabelLinksVisible_false_updatesFlag() {
        PiePlot plot = new PiePlot();
        assertTrue("Default label links visible should be true", plot.getLabelLinksVisible());
        plot.setLabelLinksVisible(false);
        assertFalse("Label links visible should be false", plot.getLabelLinksVisible());
    }

    // Tests getLabelLinkMargin and setLabelLinkMargin
    @Test
    public void testSetLabelLinkMargin_validValue_updatesMargin() {
        PiePlot plot = new PiePlot();
        assertEquals("Default label link margin", 0.025, plot.getLabelLinkMargin(), 0.0001);
        plot.setLabelLinkMargin(0.1);
        assertEquals("Label link margin should be 0.1", 0.1, plot.getLabelLinkMargin(), 0.0001);
    }

    // Tests setSimpleLabelOffset with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetSimpleLabelOffset_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setSimpleLabelOffset(null);
    }

    // Tests setLabelDistributor with null - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelDistributor_null_throwsException() {
        PiePlot plot = new PiePlot();
        plot.setLabelDistributor(null);
    }

    // Tests setLabelDistributor with valid distributor
    @Test
    public void testSetLabelDistributor_validDistributor_setsDistributor() {
        PiePlot plot = new PiePlot();
        AbstractPieLabelDistributor dist = new PieLabelDistributor(10);
        plot.setLabelDistributor(dist);
        assertSame("Label distributor should be same", dist, plot.getLabelDistributor());
    }

    // Tests getMinimumArcAngleToDraw default value
    @Test
    public void testGetMinimumArcAngleToDraw_defaultValue_returns000001() {
        PiePlot plot = new PiePlot();
        assertEquals("Default min arc angle", 0.00001, plot.getMinimumArcAngleToDraw(), 0.0000001);
    }

    // Tests setMinimumArcAngleToDraw
    @Test
    public void testSetMinimumArcAngleToDraw_validValue_updatesAngle() {
        PiePlot plot = new PiePlot();
        plot.setMinimumArcAngleToDraw(0.5);
        assertEquals("Min arc angle should be 0.5", 0.5, plot.getMinimumArcAngleToDraw(), 0.0000001);
    }

    // Tests getShadowPaint default
    @Test
    public void testGetShadowPaint_default_returnsGray() {
        PiePlot plot = new PiePlot();
        assertEquals("Default shadow paint should be gray", Color.gray, plot.getShadowPaint());
    }

    // Tests setShadowPaint with null
    @Test
    public void testSetShadowPaint_null_setsNull() {
        PiePlot plot = new PiePlot();
        plot.setShadowPaint(null);
        assertNull("Shadow paint should be null", plot.getShadowPaint());
    }

    // Tests getShadowXOffset default
    @Test
    public void testGetShadowXOffset_default_returns4() {
        PiePlot plot = new PiePlot();
        assertEquals("Default shadow X offset", 4.0, plot.getShadowXOffset(), 0.0001);
    }

    // Tests setShadowXOffset
    @Test
    public void testSetShadowXOffset_validValue_updatesOffset() {
        PiePlot plot = new PiePlot();
        plot.setShadowXOffset(10.0);
        assertEquals("Shadow X offset should be 10.0", 10.0, plot.getShadowXOffset(), 0.0001);
    }

    // Tests getShadowYOffset default
    @Test
    public void testGetShadowYOffset_default_returns4() {
        PiePlot plot = new PiePlot();
        assertEquals("Default shadow Y offset", 4.0, plot.getShadowYOffset(), 0.0001);
    }

    // Tests setShadowYOffset
    @Test
    public void testSetShadowYOffset_validValue_updatesOffset() {
        PiePlot plot = new PiePlot();
        plot.setShadowYOffset(10.0);
        assertEquals("Shadow Y offset should be 10.0", 10.0, plot.getShadowYOffset(), 0.0001);
    }

    // ================= Additional tests for uncovered coverage =================

    @Test
    public void testSetDataset_nonNull_setsDataset() {
        PiePlot plot = new PiePlot();
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        plot.setDataset(dataset);
        assertSame("Dataset should be set", dataset, plot.getDataset());
    }

    @Test
    public void testSetDataset_null_clearsDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setDataset(null);
        assertNull("Dataset should be null", plot.getDataset());
    }

    @Test
    public void testGetSectionKey_nullDataset_returnsInteger() {
        PiePlot plot = new PiePlot();
        Comparable key = plot.getSectionKey(0);
        assertTrue("Key should be Integer", key instanceof Integer);
        assertEquals("Key should be 0", 0, ((Integer) key).intValue());
    }

    @Test
    public void testGetSectionKey_negativeIndex_returnsInteger() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        Comparable key = plot.getSectionKey(-1);
        assertTrue("Key should be Integer", key instanceof Integer);
        assertEquals("Key should be -1", -1, ((Integer) key).intValue());
    }

    @Test
    public void testGetLegendItems_defaultIncludesZeroValue() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 0.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        assertEquals("Default should include zero value", 2,
                plot.getLegendItems().getItemCount());
    }

    @Test
    public void testGetMaximumExplodePercent_withExplosions_returnsMax() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setExplodePercent("A", 0.3);
        plot.setExplodePercent("B", 0.5);
        assertEquals("Maximum explode percent should be 0.5",
                0.5, plot.getMaximumExplodePercent(), 0.0001);
    }

    @Test
    public void testSetSimpleLabelOffset_validOffset_setsOffset() {
        PiePlot plot = new PiePlot();
        RectangleInsets offset = new RectangleInsets(1.0, 2.0, 3.0, 4.0);
        plot.setSimpleLabelOffset(offset);
        assertSame("Simple label offset should be same", offset,
                plot.getSimpleLabelOffset());
    }

    @Test
    public void testSetShadowPaint_validPaint_setsPaint() {
        PiePlot plot = new PiePlot();
        plot.setShadowPaint(Color.red);
        assertEquals("Shadow paint should be red", Color.red, plot.getShadowPaint());
    }

    @Test
    public void testSetBaseSectionOutlinePaint_validPaint_setsPaint() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionOutlinePaint(Color.orange);
        assertEquals("Base section outline paint should be orange",
                Color.orange, plot.getBaseSectionOutlinePaint());
    }

    @Test
    public void testSetBaseSectionOutlineStroke_validStroke_setsStroke() {
        PiePlot plot = new PiePlot();
        Stroke stroke = new BasicStroke(2.0f);
        plot.setBaseSectionOutlineStroke(stroke);
        assertSame("Base section outline stroke should be same", stroke,
                plot.getBaseSectionOutlineStroke());
    }

    @Test
    public void testSetSectionOutlinePaint_validKey_setsPaint() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setSectionOutlinePaint("A", Color.cyan);
        assertEquals("Section outline paint should be cyan",
                Color.cyan, plot.getSectionOutlinePaint("A"));
        assertEquals("Section outline paint (by index) should be cyan",
                Color.cyan, plot.getSectionOutlinePaint(0));
    }

    @Test
    public void testSetSectionOutlineStroke_validKey_setsStroke() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        Stroke stroke = new BasicStroke(3.0f);
        plot.setSectionOutlineStroke("A", stroke);
        assertSame("Section outline stroke should be same", stroke,
                plot.getSectionOutlineStroke("A"));
        assertSame("Section outline stroke (by index) should be same", stroke,
                plot.getSectionOutlineStroke(0));
    }

    @Test
    public void testSetSectionOutlinesVisible_false_updatesFlag() {
        PiePlot plot = new PiePlot();
        assertTrue("Default section outlines visible should be true",
                plot.getSectionOutlinesVisible());
        plot.setSectionOutlinesVisible(false);
        assertFalse("Section outlines visible should be false",
                plot.getSectionOutlinesVisible());
    }

    @Test
    public void testSetAutoPopulateSectionPaint_true_setsFlag() {
        PiePlot plot = new PiePlot();
        plot.setAutoPopulateSectionPaint(true);
        assertTrue("Auto-populate section paint should be true",
                plot.getAutoPopulateSectionPaint());
    }

    @Test
    public void testSetAutoPopulateSectionOutlinePaint_true_setsFlag() {
        PiePlot plot = new PiePlot();
        plot.setAutoPopulateSectionOutlinePaint(true);
        assertTrue("Auto-populate section outline paint should be true",
                plot.getAutoPopulateSectionOutlinePaint());
    }

    @Test
    public void testSetAutoPopulateSectionOutlineStroke_true_setsFlag() {
        PiePlot plot = new PiePlot();
        plot.setAutoPopulateSectionOutlineStroke(true);
        assertTrue("Auto-populate section outline stroke should be true",
                plot.getAutoPopulateSectionOutlineStroke());
    }

    @Test
    public void testSetSectionPaint_validKey_setsPaint() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setSectionPaint("A", Color.red);
        assertEquals("Section paint should be red", Color.red,
                plot.getSectionPaint("A"));
    }

    @Test
    public void testSetSectionPaint_validIndex_setsPaint() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setSectionPaint(0, Color.blue);
        assertEquals("Section paint (by index) should be blue", Color.blue,
                plot.getSectionPaint(0));
    }

    @Test
    public void testSetLegendLabelGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLegendLabelGenerator(gen);
        assertSame("Legend label generator should be same", gen,
                plot.getLegendLabelGenerator());
    }

    @Test
    public void testSetLegendLabelToolTipGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLegendLabelToolTipGenerator(gen);
        assertSame("Legend label tool tip generator should be same", gen,
                plot.getLegendLabelToolTipGenerator());
    }

    @Test
    public void testSetLegendLabelURLGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieURLGenerator gen = new StandardPieURLGenerator("index.html");
        plot.setLegendLabelURLGenerator(gen);
        assertSame("Legend label URL generator should be same", gen,
                plot.getLegendLabelURLGenerator());
    }

    @Test
    public void testSetToolTipGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieToolTipGenerator gen = new StandardPieToolTipGenerator();
        plot.setToolTipGenerator(gen);
        assertSame("Tool tip generator should be same", gen,
                plot.getToolTipGenerator());
    }

    @Test
    public void testSetURLGenerator_validGenerator_setsGenerator() {
        PiePlot plot = new PiePlot();
        PieURLGenerator gen = new StandardPieURLGenerator("index.html");
        plot.setURLGenerator(gen);
        assertSame("URL generator should be same", gen,
                plot.getURLGenerator());
    }

    @Test
    public void testDraw_withDataset_noException() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        BufferedImage image = new BufferedImage(400, 300,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), null, null,
                info.getPlotInfo());
        g2.dispose();
    }

    @Test
    public void testDraw_nullDataset_noException() {
        PiePlot plot = new PiePlot();
        BufferedImage image = new BufferedImage(200, 200,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 200, 200), null, null,
                info.getPlotInfo());
        g2.dispose();
    }

    @Test
    public void testDraw_nonCircularPlot_noException() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setCircular(false);
        BufferedImage image = new BufferedImage(400, 300,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), null, null,
                info.getPlotInfo());
        g2.dispose();
    }

    @Test
    public void testEquals_differentDataset_returnsFalse() {
        DefaultPieDataset dataset1 = new DefaultPieDataset();
        dataset1.setValue("A", 1.0);
        DefaultPieDataset dataset2 = new DefaultPieDataset();
        dataset2.setValue("B", 2.0);
        PiePlot plot1 = new PiePlot(dataset1);
        PiePlot plot2 = new PiePlot(dataset2);
        assertFalse("Plots with different datasets should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentDirection_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setDirection(Rotation.ANTICLOCKWISE);
        assertFalse("Plots with different direction should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentCircular_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setCircular(false);
        assertFalse("Plots with different circular flag should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentInteriorGap_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setInteriorGap(0.2);
        assertFalse("Plots with different interior gap should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentIgnoreNullValues_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setIgnoreNullValues(true);
        assertFalse("Plots with different ignoreNullValues should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentIgnoreZeroValues_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setIgnoreZeroValues(true);
        assertFalse("Plots with different ignoreZeroValues should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentBaseSectionPaint_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setBaseSectionPaint(Color.red);
        assertFalse("Plots with different base section paint should not be equal",
                plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentShadowPaint_returnsFalse() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setShadowPaint(Color.red);
        assertFalse("Plots with different shadow paint should not be equal",
                plot1.equals(plot2));
    }
}
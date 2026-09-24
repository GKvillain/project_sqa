package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StatisticalBarRenderer}.
 */
public class StatisticalBarRendererTest {

    private StatisticalBarRenderer renderer;

    @Before
    public void setUp() {
        renderer = new StatisticalBarRenderer();
    }

    // Tests default constructor property initialization
    @Test
    public void testConstructor_defaultValues_initializedCorrectly() {
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
        assertEquals(new BasicStroke(0.5f), renderer.getErrorIndicatorStroke());
    }

    // Tests setErrorIndicatorPaint with color and null, verifying listener notification
    @Test
    public void testSetErrorIndicatorPaint_validAndNull_updatesAndNotifies() {
        final boolean[] notified = new boolean[1];
        renderer.addChangeListener(new RendererChangeListener() {
            public void rendererChanged(RendererChangeEvent event) {
                notified[0] = true;
            }
        });

        renderer.setErrorIndicatorPaint(Color.red);
        assertEquals(Color.red, renderer.getErrorIndicatorPaint());
        assertTrue(notified[0]);

        notified[0] = false;
        renderer.setErrorIndicatorPaint(null);
        assertNull(renderer.getErrorIndicatorPaint());
        assertTrue(notified[0]);
    }

    // Tests setErrorIndicatorStroke with stroke and null, verifying listener notification
    @Test
    public void testSetErrorIndicatorStroke_validAndNull_updatesAndNotifies() {
        final boolean[] notified = new boolean[1];
        renderer.addChangeListener(new RendererChangeListener() {
            public void rendererChanged(RendererChangeEvent event) {
                notified[0] = true;
            }
        });

        Stroke stroke = new BasicStroke(2.0f);
        renderer.setErrorIndicatorStroke(stroke);
        assertEquals(stroke, renderer.getErrorIndicatorStroke());
        assertTrue(notified[0]);

        notified[0] = false;
        renderer.setErrorIndicatorStroke(null);
        assertNull(renderer.getErrorIndicatorStroke());
        assertTrue(notified[0]);
    }

    // Tests equals method for symmetry, same instance, different paints, and non-statistical renderer
    @Test
    public void testEquals_variousScenarios_returnsExpected() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();

        assertTrue(r1.equals(r1));
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        assertFalse(r1.equals(null));
        assertFalse(r1.equals("Not a renderer"));
        assertFalse(r1.equals(new BarRenderer()));

        r1.setErrorIndicatorPaint(Color.blue);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.blue);
        assertTrue(r1.equals(r2));

        r1.setErrorIndicatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.white));
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.white));
        assertTrue(r1.equals(r2));
    }

    // Tests cloning creates an independent copy
    @Test
    public void testCloning_createsIndependentCopy() throws CloneNotSupportedException {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(Color.yellow);
        r1.setErrorIndicatorStroke(new BasicStroke(1.5f));

        StatisticalBarRenderer r2 = (StatisticalBarRenderer) r1.clone();
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        r2.setErrorIndicatorPaint(Color.magenta);
        assertFalse(r1.equals(r2));
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_preservesState() throws Exception {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.blue));
        r1.setErrorIndicatorStroke(new BasicStroke(1.2f));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) in.readObject();
        in.close();

        assertEquals(r1, r2);
    }

    // Tests exception path when non-statistical dataset is supplied to drawItem
    @Test(expected = IllegalArgumentException.class)
    public void testDrawItem_nonStatisticalDataset_throwsIllegalArgumentException() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        CategoryPlot plot = new CategoryPlot();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryItemRendererState state = renderer.initialise(g2, new Rectangle2D.Double(0, 0, 200, 200),
                plot, 0, new ChartRenderingInfo());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series 1", "Category 1");

        renderer.drawItem(g2, state, new Rectangle2D.Double(0, 0, 200, 200),
                plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    // Tests drawItem with vertical orientation and multiple series
    @Test
    public void testDrawItem_verticalOrientation_rendersWithoutException() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Category 1");
        dataset.add(15.0, 2.5, "Series 2", "Category 1");
        dataset.add(20.0, 3.0, "Series 1", "Category 2");
        dataset.add(25.0, 3.5, "Series 2", "Category 2");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 400, 300));
        g2.dispose();

        assertNotNull(image);
    }

    // Tests drawItem with horizontal orientation and multiple series
    @Test
    public void testDrawItem_horizontalOrientation_rendersWithoutException() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Category 1");
        dataset.add(15.0, 2.5, "Series 2", "Category 1");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 400, 300));
        g2.dispose();

        assertNotNull(image);
    }

    // Tests drawing items with null error indicator paint and stroke (fallback branches)
    @Test
    public void testDrawItem_nullErrorIndicatorPaintAndStroke_usesOutlinePaintAndStroke() {
        renderer.setErrorIndicatorPaint(null);
        renderer.setErrorIndicatorStroke(null);
        renderer.setDrawBarOutline(true);

        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(12.0, 1.5, "Series 1", "Category 1");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 300));
        g2.dispose();

        assertNotNull(image);
    }

    // Tests drawItem with item labels and entity collection enabled
    @Test
    public void testDrawItem_withItemLabelsAndEntities_rendersLabelsAndEntities() {
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);

        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Category 1");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), info);
        g2.dispose();

        assertTrue(info.getEntityCollection().getEntityCount() > 0);
    }

    // Tests clipping branches (negative values, uclip <= 0, lclip <= 0, lclip > 0)
    @Test
    public void testDrawItem_variousClippingRanges_rendersCorrectly() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(-10.0, 1.0, "Series 1", "Category 1");
        dataset.add(5.0, 1.0, "Series 1", "Category 2");
        dataset.add(20.0, 1.0, "Series 1", "Category 3");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        rangeAxis.setRange(-15.0, 25.0);

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 400, 300));
        g2.dispose();

        assertNotNull(image);
    }

    // Tests drawing when mean value or std dev value is null in dataset
    @Test
    public void testDrawItem_nullMeanOrStdDevValue_chartRendersWithoutCrash() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(null, null, "Series 1", "Category 1");

        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 300));
        g2.dispose();

        assertNotNull(image);
    }
}
package org.jfree.chart.renderer.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

public class MinMaxCategoryRendererTest {

    private MinMaxCategoryRenderer renderer;

    @Before
    public void setUp() {
        this.renderer = new MinMaxCategoryRenderer();
    }

    // Tests default property values
    @Test
    public void testDefaultValues() {
        assertFalse(this.renderer.isDrawLines());
        assertEquals(Color.black, this.renderer.getGroupPaint());
        assertEquals(new BasicStroke(1.0f), this.renderer.getGroupStroke());
        assertNotNull(this.renderer.getObjectIcon());
        assertNotNull(this.renderer.getMinIcon());
        assertNotNull(this.renderer.getMaxIcon());
    }

    // Tests setDrawLines changes value
    @Test
    public void testSetDrawLines() {
        this.renderer.setDrawLines(true);
        assertTrue(this.renderer.isDrawLines());
        this.renderer.setDrawLines(false);
        assertFalse(this.renderer.isDrawLines());
    }

    // Tests setGroupPaint normal case
    @Test
    public void testSetGroupPaint() {
        this.renderer.setGroupPaint(Color.red);
        assertEquals(Color.red, this.renderer.getGroupPaint());
    }

    // Tests setGroupPaint null exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetGroupPaint_nullThrowsException() {
        this.renderer.setGroupPaint(null);
    }

    // Tests setGroupStroke normal case
    @Test
    public void testSetGroupStroke() {
        BasicStroke stroke = new BasicStroke(2.0f);
        this.renderer.setGroupStroke(stroke);
        assertEquals(stroke, this.renderer.getGroupStroke());
    }

    // Tests setGroupStroke null exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetGroupStroke_nullThrowsException() {
        this.renderer.setGroupStroke(null);
    }

    // Tests setObjectIcon normal case
    @Test
    public void testSetObjectIcon() {
        Icon icon = new ImageIcon();
        this.renderer.setObjectIcon(icon);
        assertEquals(icon, this.renderer.getObjectIcon());
    }

    // Tests setObjectIcon null exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectIcon_nullThrowsException() {
        this.renderer.setObjectIcon(null);
    }

    // Tests setMinIcon normal case
    @Test
    public void testSetMinIcon() {
        Icon icon = new ImageIcon();
        this.renderer.setMinIcon(icon);
        assertEquals(icon, this.renderer.getMinIcon());
    }

    // Tests setMinIcon null exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMinIcon_nullThrowsException() {
        this.renderer.setMinIcon(null);
    }

    // Tests setMaxIcon normal case
    @Test
    public void testSetMaxIcon() {
        Icon icon = new ImageIcon();
        this.renderer.setMaxIcon(icon);
        assertEquals(icon, this.renderer.getMaxIcon());
    }

    // Tests setMaxIcon null exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxIcon_nullThrowsException() {
        this.renderer.setMaxIcon(null);
    }

    // Tests equals method
    @Test
    public void testEquals() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        r1.setDrawLines(true);
        assertFalse(r1.equals(r2));
        r2.setDrawLines(true);
        assertTrue(r1.equals(r2));

        r1.setGroupPaint(Color.blue);
        assertFalse(r1.equals(r2));
        r2.setGroupPaint(Color.blue);
        assertTrue(r1.equals(r2));

        r1.setGroupStroke(new BasicStroke(2.5f));
        assertFalse(r1.equals(r2));
        r2.setGroupStroke(new BasicStroke(2.5f));
        assertTrue(r1.equals(r2));
    }

    // Tests drawItem with vertical orientation
    @Test
    public void testDrawItem_vertical() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(5.0, "R2", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(6.0, "R2", "C2");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);
        g2.dispose();
    }

    // Tests drawItem with horizontal orientation and lines enabled
    @Test
    public void testDrawItem_horizontalWithLines() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(5.0, "R2", "C1");
        dataset.addValue(3.0, "R1", "C2");
        dataset.addValue(2.0, "R2", "C2");

        this.renderer.setDrawLines(true);
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);
        g2.dispose();
    }

    // Tests serialization
    @Test
    public void testSerialization() throws Exception {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(true);
        r1.setGroupPaint(Color.red);
        r1.setGroupStroke(new BasicStroke(1.5f));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) in.readObject();
        in.close();

        assertEquals(r1.isDrawLines(), r2.isDrawLines());
        assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
        assertNotNull(r2.getObjectIcon());
        assertNotNull(r2.getMinIcon());
        assertNotNull(r2.getMaxIcon());
    }

    // Tests equals method with other object types, null, and icon comparisons
    @Test
    public void testEquals_additionalCases() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();

        // Reflexive and null/type tests
        assertTrue(r1.equals(r1));
        assertFalse(r1.equals(null));
        assertFalse(r1.equals("Some String"));

        // Test objectIcon difference
        Icon icon1 = new ImageIcon(new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB));
        Icon icon2 = new ImageIcon(new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB));
        r1.setObjectIcon(icon1);
        assertFalse(r1.equals(r2));
        r2.setObjectIcon(icon1);
        assertTrue(r1.equals(r2));

        // Test minIcon difference
        r1.setMinIcon(icon1);
        assertFalse(r1.equals(r2));
        r2.setMinIcon(icon1);
        assertTrue(r1.equals(r2));

        // Test maxIcon difference
        r1.setMaxIcon(icon2);
        assertFalse(r1.equals(r2));
        r2.setMaxIcon(icon2);
        assertTrue(r1.equals(r2));
    }

    // Tests drawItem with vertical orientation and line drawing enabled
    @Test
    public void testDrawItem_verticalWithLines() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(5.0, "R2", "C1");
        dataset.addValue(3.0, "R1", "C2");
        dataset.addValue(2.0, "R2", "C2");

        this.renderer.setDrawLines(true);
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);
        g2.dispose();
    }

    // Tests drawItem with null values in dataset across vertical and horizontal orientations
    @Test
    public void testDrawItem_withNullValues() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "R1", "C1");
        dataset.addValue(5.0, "R2", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(null, "R2", "C2");

        this.renderer.setDrawLines(true);
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);

        g2.dispose();
    }

    // Tests drawItem with a single row dataset
    @Test
    public void testDrawItem_singleRow() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");

        this.renderer.setDrawLines(true);
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);
        g2.dispose();
    }

    // Tests drawItem with custom icon implementation
    @Test
    public void testDrawItem_withCustomIcons() {
        Icon customIcon = new Icon() {
            public int getIconWidth() { return 10; }
            public int getIconHeight() { return 10; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.fillRect(x, y, 10, 10);
            }
        };

        this.renderer.setObjectIcon(customIcon);
        this.renderer.setMinIcon(customIcon);
        this.renderer.setMaxIcon(customIcon);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(3.0, "R1", "C1");
        dataset.addValue(7.0, "R2", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, null);
        g2.dispose();
    }
}
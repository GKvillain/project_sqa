package org.jfree.chart.util;

import org.junit.Test;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link ShapeList} class.
 */
public class ShapeListTest {

    // Tests equals method with the same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ShapeList list = new ShapeList();
        assertTrue(list.equals(list));
    }

    // Tests equals method with null
    @Test
    public void testEquals_null_returnsFalse() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals(null));
    }

    // Tests equals method with incompatible type
    @Test
    public void testEquals_differentType_returnsFalse() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals("Not a ShapeList"));
    }

    // Tests equals method with two empty ShapeLists
    @Test
    public void testEquals_twoEmptyLists_returnsTrue() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        assertTrue(list1.equals(list2));
        assertTrue(list2.equals(list1));
    }

    // Tests equals method with identical shape instances and distinct shape instances
    @Test
    public void testEquals_sameShapes_returnsTrue() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        list1.setShape(1, new Line2D.Double(0.0, 0.0, 5.0, 5.0));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        list2.setShape(1, new Line2D.Double(0.0, 0.0, 5.0, 5.0));

        assertTrue(list1.equals(list2));
        assertTrue(list2.equals(list1));
    }

    // Tests equals method with different shapes at the same index
    @Test
    public void testEquals_differentShapes_returnsFalse() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle2D.Double(2.0, 3.0, 4.0, 5.0));

        assertFalse(list1.equals(list2));
    }

    // Tests equals method with different sizes / indices
    @Test
    public void testEquals_differentIndices_returnsFalse() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(0, 0, 10, 10));

        ShapeList list2 = new ShapeList();
        list2.setShape(1, new Rectangle(0, 0, 10, 10));

        assertFalse(list1.equals(list2));
    }

    // Tests getShape and setShape with valid indices
    @Test
    public void testGetAndSetShape_validIndex_returnsCorrectShape() {
        ShapeList list = new ShapeList();
        Shape shape0 = new Rectangle(0, 0, 10, 10);
        Shape shape5 = new Line2D.Double(1.0, 1.0, 2.0, 2.0);

        list.setShape(0, shape0);
        list.setShape(5, shape5);

        assertEquals(shape0, list.getShape(0));
        assertNull(list.getShape(1));
        assertNull(list.getShape(4));
        assertEquals(shape5, list.getShape(5));
    }

    // Tests getShape with an unassigned index
    @Test
    public void testGetShape_unassignedIndex_returnsNull() {
        ShapeList list = new ShapeList();
        assertNull(list.getShape(0));
        assertNull(list.getShape(10));
    }

    // Tests setShape with null shape value
    @Test
    public void testSetShape_nullShape_storesNull() {
        ShapeList list = new ShapeList();
        list.setShape(0, new Rectangle(1, 1, 10, 10));
        list.setShape(0, null);

        assertNull(list.getShape(0));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalObjects_sameHashCode() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(0, 0, 5, 5));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle(0, 0, 5, 5));

        assertEquals(list1.hashCode(), list2.hashCode());
    }

    // Tests cloning creates an independent yet equal copy
    @Test
    public void testClone_clonedInstance_equalsOriginalAndIndependent() throws CloneNotSupportedException {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle(0, 0, 10, 10));
        list1.setShape(2, new Line2D.Double(0.0, 0.0, 1.0, 1.0));

        ShapeList list2 = (ShapeList) list1.clone();

        assertNotSame(list1, list2);
        assertEquals(list1.getClass(), list2.getClass());
        assertTrue(list1.equals(list2));

        list2.setShape(0, new Rectangle(5, 5, 20, 20));
        assertFalse(list1.equals(list2));
    }

    // Tests serialization and deserialization of ShapeList with shapes and null entries
    @Test
    public void testSerialization_shapesAndNulls_restoresEquivalentObject() throws Exception {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        list1.setShape(2, new Line2D.Double(1.0, 1.0, 2.0, 2.0));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(list1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ShapeList list2 = (ShapeList) in.readObject();
        in.close();

        assertEquals(list1, list2);
        assertEquals(list1.getShape(0), list2.getShape(0));
        assertNull(list2.getShape(1));
        assertEquals(list1.getShape(2), list2.getShape(2));
    }

    // Tests serialization and deserialization of an empty ShapeList
    @Test
    public void testSerialization_emptyList_restoresEquivalentObject() throws Exception {
        ShapeList list1 = new ShapeList();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(list1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ShapeList list2 = (ShapeList) in.readObject();
        in.close();

        assertEquals(list1, list2);
    }
}
package org.jfree.chart.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Test class for ShapeList, targeting Defects4J bug 6b.
 */
public class ShapeListTest {

    // Tests default constructor creates empty list
    @Test
    public void testConstructor_default_createsEmptyList() {
        ShapeList list = new ShapeList();
        assertEquals(0, list.size());
    }

    // Tests getShape with valid index
    @Test
    public void testGetShape_validIndex_returnsShape() {
        ShapeList list = new ShapeList();
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        list.setShape(0, shape);
        assertSame(shape, list.getShape(0));
    }

    // Tests getShape with null value at index
    @Test
    public void testGetShape_indexWithNull_returnsNull() {
        ShapeList list = new ShapeList();
        list.setShape(0, null);
        assertNull(list.getShape(0));
    }

    // Tests setShape expands list when index beyond current size
    @Test
    public void testSetShape_indexBeyondSize_expandsList() {
        ShapeList list = new ShapeList();
        Shape shape = new Rectangle2D.Double(5, 6, 7, 8);
        list.setShape(5, shape);
        assertSame(shape, list.getShape(5));
        // intermediate positions should be null
        for (int i = 0; i < 5; i++) {
            assertNull(list.getShape(i));
        }
        assertEquals(6, list.size());
    }

    // Tests setShape at index 0
    @Test
    public void testSetShape_firstIndex_storesShape() {
        ShapeList list = new ShapeList();
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        list.setShape(0, shape);
        assertSame(shape, list.getShape(0));
    }

    // Tests setShape with null value
    @Test
    public void testSetShape_nullValue_storesNull() {
        ShapeList list = new ShapeList();
        list.setShape(0, null);
        assertNull(list.getShape(0));
    }

    // Tests equals with same object returns true
    @Test
    public void testEquals_sameObject_returnsTrue() {
        ShapeList list = new ShapeList();
        assertTrue(list.equals(list));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_nullObject_returnsFalse() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals(null));
    }

    // Tests equals with different type returns false
    @Test
    public void testEquals_differentType_returnsFalse() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals("not a ShapeList"));
    }

    // Tests equals with two empty lists returns true
    @Test
    public void testEquals_bothEmpty_returnsTrue() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        assertTrue(list1.equals(list2));
    }

    // Tests equals with identical content returns true
    @Test
    public void testEquals_sameContent_returnsTrue() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        list1.setShape(0, shape);
        list2.setShape(0, shape);
        assertTrue(list1.equals(list2));
    }

    // Tests equals with different content returns false
    @Test
    public void testEquals_differentContent_returnsFalse() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double(1, 2, 3, 4));
        list2.setShape(0, new Rectangle2D.Double(5, 6, 7, 8));
        assertFalse(list1.equals(list2));
    }

    // Tests clone returns independent copy
    @Test
    public void testClone_returnsIndependentCopy() throws CloneNotSupportedException {
        ShapeList list = new ShapeList();
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        list.setShape(0, shape);
        ShapeList cloned = (ShapeList) list.clone();
        assertNotSame(list, cloned);
        assertSame(shape, cloned.getShape(0));
    }

    // Tests serialization roundtrip
    @Test
    public void testSerialization_roundtrip_preservesShape() throws Exception {
        ShapeList list = new ShapeList();
        Shape originalShape = new Rectangle2D.Double(1, 2, 3, 4);
        list.setShape(0, originalShape);
        list.setShape(2, null);
        list.setShape(5, new Rectangle2D.Double(10, 20, 30, 40));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(list);
        oos.flush();
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();

        assertEquals(list.size(), deserialized.size());
        assertEquals(originalShape, deserialized.getShape(0));
        assertNull(deserialized.getShape(2));
        assertEquals(new Rectangle2D.Double(10, 20, 30, 40), deserialized.getShape(5));
    }

    // Tests serialization of empty list
    @Test
    public void testSerialization_emptyList_roundtrip() throws Exception {
        ShapeList list = new ShapeList();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(list);
        oos.flush();
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();

        assertEquals(0, deserialized.size());
    }

    // Tests hashCode consistent with equals
    @Test
    public void testHashCode_equalLists_haveSameHashCode() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    // Tests getShape returns null for index not yet set
    @Test
    public void testGetShape_unsetIndex_returnsNull() {
        ShapeList list = new ShapeList();
        assertNull(list.getShape(0));
    }
}
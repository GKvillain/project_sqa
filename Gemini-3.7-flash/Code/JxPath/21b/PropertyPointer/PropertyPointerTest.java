package org.apache.commons.jxpath.ri.model.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyPointerTest {

    private static class TestPropertyPointer extends PropertyPointer {
        private String propertyName = "testProp";
        private Object baseValue;
        private boolean actualProperty = true;

        public TestPropertyPointer(NodePointer parent) {
            super(parent);
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public int getPropertyCount() {
            return 1;
        }

        public String[] getPropertyNames() {
            return new String[] { propertyName };
        }

        protected boolean isActualProperty() {
            return actualProperty;
        }

        public void setActualProperty(boolean actualProperty) {
            this.actualProperty = actualProperty;
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        public void setValue(Object value) {
            this.baseValue = value;
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        public String asPath() {
            return "/" + propertyName;
        }
    }

    private NodePointer parentPointer;
    private TestPropertyPointer propertyPointer;

    @Before
    public void setUp() {
        parentPointer = NodePointer.newNodePointer(new QName("root"), "parentBean", Locale.getDefault());
        propertyPointer = new TestPropertyPointer(parentPointer);
    }

    // Tests default property index value
    @Test
    public void testGetPropertyIndex_default_returnsUnspecified() {
        assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, propertyPointer.getPropertyIndex());
    }

    // Tests setting property index resets collection index when changed
    @Test
    public void testSetPropertyIndex_differentIndex_updatesIndexAndResetsCollectionIndex() {
        propertyPointer.setIndex(2);
        propertyPointer.setPropertyIndex(3);
        assertEquals(3, propertyPointer.getPropertyIndex());
        assertEquals(NodePointer.WHOLE_COLLECTION, propertyPointer.getIndex());
    }

    // Tests setting property index to same value does not reset collection index
    @Test
    public void testSetPropertyIndex_sameIndex_retainsCollectionIndex() {
        propertyPointer.setPropertyIndex(3);
        propertyPointer.setIndex(2);
        propertyPointer.setPropertyIndex(3);
        assertEquals(3, propertyPointer.getPropertyIndex());
        assertEquals(2, propertyPointer.getIndex());
    }

    // Tests getBean returns parent node value
    @Test
    public void testGetBean_uninitialized_returnsParentNode() {
        assertEquals("parentBean", propertyPointer.getBean());
    }

    // Tests getName returns QName with property name
    @Test
    public void testGetName_validPropertyName_returnsQName() {
        QName name = propertyPointer.getName();
        assertNotNull(name);
        assertEquals("testProp", name.getName());
        assertNull(name.getPrefix());
    }

    // Tests isActual returns false when isActualProperty is false
    @Test
    public void testIsActual_notActualProperty_returnsFalse() {
        propertyPointer.setActualProperty(false);
        assertFalse(propertyPointer.isActual());
    }

    // Tests isActual returns true when isActualProperty is true
    @Test
    public void testIsActual_actualProperty_returnsTrue() {
        propertyPointer.setActualProperty(true);
        assertTrue(propertyPointer.isActual());
    }

    // Tests getImmediateNode for whole collection
    @Test
    public void testGetImmediateNode_wholeCollection_returnsBaseValue() {
        propertyPointer.setBaseValue("testValue");
        assertEquals("testValue", propertyPointer.getImmediateNode());
    }

    // Tests getImmediateNode with indexed collection
    @Test
    public void testGetImmediateNode_indexedCollection_returnsElement() {
        List<String> list = Arrays.asList("item0", "item1", "item2");
        propertyPointer.setBaseValue(list);
        propertyPointer.setIndex(1);
        assertEquals("item1", propertyPointer.getImmediateNode());
    }

    // Tests isCollection returns true for collection base value
    @Test
    public void testIsCollection_collectionValue_returnsTrue() {
        propertyPointer.setBaseValue(new ArrayList<String>());
        assertTrue(propertyPointer.isCollection());
    }

    // Tests isCollection returns false for null base value
    @Test
    public void testIsCollection_nullValue_returnsFalse() {
        propertyPointer.setBaseValue(null);
        assertFalse(propertyPointer.isCollection());
    }

    // Tests isCollection returns false for non-collection base value
    @Test
    public void testIsCollection_scalarValue_returnsFalse() {
        propertyPointer.setBaseValue("singleString");
        assertFalse(propertyPointer.isCollection());
    }

    // Tests isLeaf returns true for null immediate node
    @Test
    public void testIsLeaf_nullNode_returnsTrue() {
        propertyPointer.setBaseValue(null);
        assertTrue(propertyPointer.isLeaf());
    }

    // Tests isLeaf returns true for atomic/primitive types
    @Test
    public void testIsLeaf_atomicNode_returnsTrue() {
        propertyPointer.setBaseValue("stringValue");
        assertTrue(propertyPointer.isLeaf());
    }

    // Tests getLength returns collection length
    @Test
    public void testGetLength_collectionValue_returnsLength() {
        propertyPointer.setBaseValue(Arrays.asList("a", "b", "c"));
        assertEquals(3, propertyPointer.getLength());
    }

    // Tests getLength returns 0 for null base value
    @Test
    public void testGetLength_nullValue_returnsZero() {
        propertyPointer.setBaseValue(null);
        assertEquals(0, propertyPointer.getLength());
    }

    // Tests getLength returns 1 for single scalar value
    @Test
    public void testGetLength_scalarValue_returnsOne() {
        propertyPointer.setBaseValue("singleValue");
        assertEquals(1, propertyPointer.getLength());
    }

    // Tests getImmediateValuePointer returns valid NodePointer
    @Test
    public void testGetImmediateValuePointer_validValue_returnsChildPointer() {
        propertyPointer.setBaseValue("value");
        NodePointer valuePointer = propertyPointer.getImmediateValuePointer();
        assertNotNull(valuePointer);
        assertEquals("value", valuePointer.getImmediateNode());
    }

    // Tests createPath with factory throws exception on creation failure
    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreatePath_factoryFails_throwsException() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, org.apache.commons.jxpath.Pointer pointer,
                    Object parent, String name, int index) {
                return false;
            }
        });
        propertyPointer.setBaseValue(null);
        propertyPointer.createPath(context);
    }

    // Tests createPath with factory succeeds
    @Test
    public void testCreatePath_factorySucceeds_returnsPointer() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, org.apache.commons.jxpath.Pointer pointer,
                    Object parent, String name, int index) {
                ((TestPropertyPointer) pointer).setBaseValue("created");
                return true;
            }
        });
        propertyPointer.setBaseValue(null);
        NodePointer result = propertyPointer.createPath(context);
        assertSame(propertyPointer, result);
    }

    // Tests createPath with value sets the value
    @Test
    public void testCreatePath_withValue_setsValue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = propertyPointer.createPath(context, "newValue");
        assertSame(propertyPointer, result);
        assertEquals("newValue", propertyPointer.getBaseValue());
    }

    // Tests equals for same object and different types
    @Test
    public void testEquals_variousScenarios_returnsExpected() {
        assertTrue(propertyPointer.equals(propertyPointer));
        assertFalse(propertyPointer.equals(null));
        assertFalse(propertyPointer.equals("stringObject"));

        TestPropertyPointer other = new TestPropertyPointer(parentPointer);
        assertTrue(propertyPointer.equals(other));

        other.setPropertyName("differentProp");
        assertFalse(propertyPointer.equals(other));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testHashCode_equalObjects_haveSameHashCode() {
        TestPropertyPointer other = new TestPropertyPointer(parentPointer);
        assertEquals(propertyPointer.hashCode(), other.hashCode());
    }
}
package org.apache.commons.jxpath.ri.model.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyPointerTest {

    private static class ConcretePropertyPointer extends PropertyPointer {
        private static final long serialVersionUID = 1L;
        private String propertyName = "testProperty";
        private Object baseValue;
        private boolean actualProperty = true;
        private int propertyCount = 1;
        private String[] propertyNames = new String[]{"testProperty"};

        public ConcretePropertyPointer(NodePointer parent) {
            super(parent);
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public int getPropertyCount() {
            return propertyCount;
        }

        public String[] getPropertyNames() {
            return propertyNames;
        }

        protected boolean isActualProperty() {
            return actualProperty;
        }

        public void setActualProperty(boolean actualProperty) {
            this.actualProperty = actualProperty;
        }

        public void setValue(Object value) {
            this.baseValue = value;
        }
    }

    private NodePointer parentPointer;
    private ConcretePropertyPointer pointer;
    private String parentBean;

    @Before
    public void setUp() {
        parentBean = "parentBeanObject";
        parentPointer = NodePointer.newNodePointer(new QName("root"), parentBean, Locale.getDefault());
        pointer = new ConcretePropertyPointer(parentPointer);
    }

    // Tests getLength returns 1 when baseValue is null (Defects4J bug 21b regression test)
    @Test
    public void testGetLength_nullBaseValue_returnsOne() {
        pointer.setBaseValue(null);
        assertEquals(1, pointer.getLength());
    }

    // Tests getLength returns size of collection for List baseValue
    @Test
    public void testGetLength_listBaseValue_returnsCollectionLength() {
        List<String> list = Arrays.asList("item1", "item2", "item3");
        pointer.setBaseValue(list);
        assertEquals(3, pointer.getLength());
    }

    // Tests getLength returns 1 for non-collection scalar baseValue
    @Test
    public void testGetLength_scalarBaseValue_returnsOne() {
        pointer.setBaseValue("singleValue");
        assertEquals(1, pointer.getLength());
    }

    // Tests setPropertyIndex updates propertyIndex and resets element index to WHOLE_COLLECTION
    @Test
    public void testSetPropertyIndex_newIndex_resetsElementIndex() {
        pointer.setIndex(2);
        pointer.setPropertyIndex(5);
        assertEquals(5, pointer.getPropertyIndex());
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    // Tests setPropertyIndex does not reset element index when setting same propertyIndex
    @Test
    public void testSetPropertyIndex_sameIndex_preservesElementIndex() {
        pointer.setPropertyIndex(3);
        pointer.setIndex(2);
        pointer.setPropertyIndex(3);
        assertEquals(3, pointer.getPropertyIndex());
        assertEquals(2, pointer.getIndex());
    }

    // Tests getBean retrieves bean from parent pointer when bean field is null
    @Test
    public void testGetBean_uninitialized_returnsParentNode() {
        assertEquals(parentBean, pointer.getBean());
    }

    // Tests getName returns QName matching propertyName
    @Test
    public void testGetName_returnsQNameWithPropertyName() {
        pointer.setPropertyName("customName");
        QName name = pointer.getName();
        assertEquals("customName", name.getName());
        assertNull(name.getPrefix());
    }

    // Tests isActual returns false when isActualProperty returns false
    @Test
    public void testIsActual_whenNotActualProperty_returnsFalse() {
        pointer.setActualProperty(false);
        assertFalse(pointer.isActual());
    }

    // Tests isActual returns true when isActualProperty is true and parent is actual
    @Test
    public void testIsActual_whenActualProperty_returnsTrue() {
        pointer.setActualProperty(true);
        assertTrue(pointer.isActual());
    }

    // Tests isCollection returns true for Collection and Array baseValue
    @Test
    public void testIsCollection_collectionOrArray_returnsTrue() {
        pointer.setBaseValue(new ArrayList<String>());
        assertTrue(pointer.isCollection());

        pointer.setBaseValue(new String[]{"a", "b"});
        assertTrue(pointer.isCollection());
    }

    // Tests isCollection returns false for null and scalar baseValue
    @Test
    public void testIsCollection_nullOrScalar_returnsFalse() {
        pointer.setBaseValue(null);
        assertFalse(pointer.isCollection());

        pointer.setBaseValue("notACollection");
        assertFalse(pointer.isCollection());
    }

    // Tests isLeaf returns true for null or atomic objects
    @Test
    public void testIsLeaf_nullOrAtomic_returnsTrue() {
        pointer.setBaseValue(null);
        assertTrue(pointer.isLeaf());

        pointer.setBaseValue("stringIsAtomic");
        assertTrue(pointer.isLeaf());
    }

    // Tests getImmediateNode returns baseValue when index is WHOLE_COLLECTION
    @Test
    public void testGetImmediateNode_wholeCollection_returnsBaseValue() {
        String[] array = new String[]{"val1", "val2"};
        pointer.setBaseValue(array);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(array, pointer.getImmediateNode());
    }

    // Tests getImmediateNode returns indexed element when index is specified
    @Test
    public void testGetImmediateNode_specificIndex_returnsIndexedElement() {
        String[] array = new String[]{"val1", "val2"};
        pointer.setBaseValue(array);
        pointer.setIndex(1);
        assertEquals("val2", pointer.getImmediateNode());
    }

    // Tests getImmediateValuePointer creates a valid child NodePointer
    @Test
    public void testGetImmediateValuePointer_returnsNonNullNodePointer() {
        pointer.setBaseValue("sample");
        NodePointer valuePointer = pointer.getImmediateValuePointer();
        assertNotNull(valuePointer);
        assertEquals("testProperty", valuePointer.getName().getName());
        assertEquals("sample", valuePointer.getImmediateNode());
    }

    // Tests equals returns true for same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        assertTrue(pointer.equals(pointer));
    }

    // Tests equals returns false for null or incompatible type
    @Test
    public void testEquals_differentTypeOrNull_returnsFalse() {
        assertFalse(pointer.equals(null));
        assertFalse(pointer.equals("notAPointer"));
    }

    // Tests equals and hashCode consistency for equal pointers
    @Test
    public void testEqualsAndHashCode_equalPointers_returnsTrueAndSameHashCode() {
        ConcretePropertyPointer other = new ConcretePropertyPointer(parentPointer);
        other.setPropertyName(pointer.getPropertyName());
        other.setPropertyIndex(pointer.getPropertyIndex());
        other.setIndex(pointer.getIndex());

        assertTrue(pointer.equals(other));
        assertTrue(other.equals(pointer));
        assertEquals(pointer.hashCode(), other.hashCode());
    }

    // Tests equals returns false when propertyIndex or propertyName differs
    @Test
    public void testEquals_differentPropertyIndexOrName_returnsFalse() {
        ConcretePropertyPointer other = new ConcretePropertyPointer(parentPointer);
        other.setPropertyIndex(99);
        assertFalse(pointer.equals(other));

        ConcretePropertyPointer otherName = new ConcretePropertyPointer(parentPointer);
        otherName.setPropertyName("differentProperty");
        assertFalse(pointer.equals(otherName));
    }

    // Tests equals returns false when parent pointer differs
    @Test
    public void testEquals_differentParent_returnsFalse() {
        NodePointer differentParent = NodePointer.newNodePointer(new QName("other"), "otherBean", Locale.getDefault());
        ConcretePropertyPointer other = new ConcretePropertyPointer(differentParent);
        assertFalse(pointer.equals(other));
    }
}
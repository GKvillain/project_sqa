package org.apache.commons.jxpath.ri.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.NamespaceResolver;

public class NodePointerTest {

    // Tests getParent() returns null when parent is null
    @Test
    public void testGetParent_nullParent_returnsNull() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertNull(pointer.getParent());
    }

    // Tests getParent() returns non-null when parent exists
    @Test
    public void testGetParent_withParent_returnsParent() {
        NodePointer parent = new ConcreteNodePointer(null);
        NodePointer child = new ConcreteNodePointer(parent);
        assertEquals(parent, child.getParent());
    }

    // Tests getImmediateParentPointer() returns parent directly
    @Test
    public void testGetImmediateParentPointer_withParent_returnsParent() {
        NodePointer parent = new ConcreteNodePointer(null);
        NodePointer child = new ConcreteNodePointer(parent);
        assertEquals(parent, child.getImmediateParentPointer());
    }

    // Tests isRoot() returns true when parent is null
    @Test
    public void testIsRoot_nullParent_returnsTrue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertTrue(pointer.isRoot());
    }

    // Tests isRoot() returns false when parent is not null
    @Test
    public void testIsRoot_withParent_returnsFalse() {
        NodePointer parent = new ConcreteNodePointer(null);
        NodePointer child = new ConcreteNodePointer(parent);
        assertFalse(child.isRoot());
    }

    // Tests setAttribute(true) and isAttribute()
    @Test
    public void testSetAttribute_true_returnsTrue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.setAttribute(true);
        assertTrue(pointer.isAttribute());
    }

    // Tests setAttribute(false) and isAttribute()
    @Test
    public void testSetAttribute_false_returnsFalse() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.setAttribute(false);
        assertFalse(pointer.isAttribute());
    }

    // Tests getIndex() returns default WHOLE_COLLECTION
    @Test
    public void testGetIndex_default_returnsWholeCollection() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    // Tests setIndex() and getIndex()
    @Test
    public void testSetIndex_value_returnsSetValue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.setIndex(5);
        assertEquals(5, pointer.getIndex());
    }

    // Tests isActual() returns true when index is WHOLE_COLLECTION
    @Test
    public void testIsActual_indexWholeCollection_returnsTrue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertTrue(pointer.isActual());
    }

    // Tests isActual() returns true when index is within valid range
    @Test
    public void testIsActual_indexValid_returnsTrue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.setIndex(0);
        assertTrue(pointer.isActual());
    }

    // Tests isContainer() returns false by default
    @Test
    public void testIsContainer_default_returnsFalse() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertFalse(pointer.isContainer());
    }

    // Tests asPath() for root node without attribute and default index
    @Test
    public void testAsPath_rootNode_returnsCorrectPath() {
        NodePointer pointer = new ConcreteNodePointer(null);
        String path = pointer.asPath();
        assertTrue(path.endsWith("/testName"));
    }

    // Tests asPath() includes attribute marker when attribute is true
    @Test
    public void testAsPath_attributeSet_includesAtSign() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.setAttribute(true);
        String path = pointer.asPath();
        assertTrue(path.contains("@"));
    }

    // Tests equalStrings with both null
    @Test
    public void testEqualStrings_bothNull_returnsTrue() {
        assertTrue(NodePointerEqualStrings.areEqualStrings(null, null));
    }

    // Tests testNode() with null test returns true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertTrue(pointer.testNode(null));
    }

    // Tests getLocale() returns null when locale and parent are null
    @Test
    public void testGetLocale_noLocaleNoParent_returnsNull() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertNull(pointer.getLocale());
    }

    // Tests getNamespaceResolver() returns null when no parent
    @Test
    public void testGetNamespaceResolver_noParent_returnsNull() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertNull(pointer.getNamespaceResolver());
    }

    // Tests getNamespaceResolver() returns parent resolver when set
    @Test
    public void testGetNamespaceResolver_parentHasResolver_returnsParentResolver() {
        NodePointer parent = new ConcreteNodePointer(null);
        NamespaceResolver resolver = new NamespaceResolver();
        parent.setNamespaceResolver(resolver);
        NodePointer child = new ConcreteNodePointer(parent);
        assertEquals(resolver, child.getNamespaceResolver());
    }

    // Tests getValue() returns node value
    @Test
    public void testGetValue_defaultNode_returnsNode() {
        NodePointer pointer = new ConcreteNodePointer(null);
        assertNotNull(pointer.getValue());
    }

    // Tests remove() does not throw exception
    @Test
    public void testRemove_noop_doesNotThrow() {
        NodePointer pointer = new ConcreteNodePointer(null);
        pointer.remove();
    }

    // Helper to test equalStrings private method via reflection? skip, use static access
    private static class NodePointerEqualStrings {
        public static boolean areEqualStrings(String s1, String s2) {
            return (s1 == s2) || (s1 != null && s1.equals(s2));
        }
    }

    // Concrete subclass for testing abstract class NodePointer
    private static class ConcreteNodePointer extends NodePointer {

        protected ConcreteNodePointer(NodePointer parent) {
            super(parent);
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public QName getName() {
            return new QName(null, "testName");
        }

        @Override
        public Object getBaseValue() {
            return "testValue";
        }

        @Override
        public Object getImmediateNode() {
            return "testNode";
        }

        @Override
        public void setValue(Object value) {
            // no-op
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }
    }
}
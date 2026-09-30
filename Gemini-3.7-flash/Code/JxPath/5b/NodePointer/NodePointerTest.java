package org.apache.commons.jxpath.ri.model;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodePointerTest {

    private static class TestNodePointer extends NodePointer {
        private QName name;
        private Object value;
        private int length = 1;
        private boolean collection = false;
        private boolean container = false;

        TestNodePointer(NodePointer parent, QName name, Object value) {
            super(parent);
            this.name = name;
            this.value = value;
        }

        TestNodePointer(NodePointer parent, Locale locale, QName name, Object value) {
            super(parent, locale);
            this.name = name;
            this.value = value;
        }

        public boolean isLeaf() {
            return true;
        }

        public boolean isCollection() {
            return collection;
        }

        public void setCollection(boolean collection) {
            this.collection = collection;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean isContainer() {
            return container;
        }

        public void setContainer(boolean container) {
            this.container = container;
        }

        public QName getName() {
            return name;
        }

        public Object getBaseValue() {
            return value;
        }

        public Object getImmediateNode() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            String n1 = pointer1.getName() == null ? "" : pointer1.getName().getName();
            String n2 = pointer2.getName() == null ? "" : pointer2.getName().getName();
            return n1.compareTo(n2);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof TestNodePointer)) {
                return false;
            }
            TestNodePointer other = (TestNodePointer) obj;
            return (name == null ? other.name == null : name.equals(other.name))
                    && (value == null ? other.value == null : value.equals(other.value));
        }

        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    // Tests factory creation when bean is null
    @Test
    public void testNewNodePointer_nullBean_returnsNullPointer() {
        QName name = new QName("test");
        NodePointer pointer = NodePointer.newNodePointer(name, null, Locale.ENGLISH);
        assertNotNull(pointer);
        assertTrue(pointer instanceof NullPointer);
        assertEquals(name, pointer.getName());
    }

    // Tests factory creation for regular object bean
    @Test
    public void testNewNodePointer_objectBean_returnsNonNullPointer() {
        QName name = new QName("test");
        NodePointer pointer = NodePointer.newNodePointer(name, "stringValue", Locale.US);
        assertNotNull(pointer);
        assertEquals(Locale.US, pointer.getLocale());
    }

    // Tests factory creation of child node pointer
    @Test
    public void testNewChildNodePointer_withParent_returnsChildPointer() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), "rootValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(parent, new QName("child"), "childValue");
        assertNotNull(child);
        assertEquals(parent, child.getImmediateParentPointer());
    }

    // Tests inheritance and setting of NamespaceResolver
    @Test
    public void testGetNamespaceResolver_parentInheritance_returnsParentResolver() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        NamespaceResolver resolver = new NamespaceResolver();
        root.setNamespaceResolver(resolver);

        TestNodePointer child = new TestNodePointer(root, new QName("child"), "child");
        assertSame(resolver, child.getNamespaceResolver());
    }

    // Tests getParent when an intermediate container pointer exists
    @Test
    public void testGetParent_withIntermediateContainer_skipsContainer() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        TestNodePointer container = new TestNodePointer(root, new QName("container"), "container");
        container.setContainer(true);
        TestNodePointer child = new TestNodePointer(container, new QName("child"), "child");

        assertSame(container, child.getImmediateParentPointer());
        assertSame(root, child.getParent());
    }

    // Tests attribute flag manipulation
    @Test
    public void testAttribute_setAndGet_returnsCorrectValue() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("attr"), "val");
        assertFalse(pointer.isAttribute());
        pointer.setAttribute(true);
        assertTrue(pointer.isAttribute());
    }

    // Tests root pointer identification
    @Test
    public void testIsRoot_rootAndChild_returnsTrueAndFalse() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "child");
        assertTrue(root.isRoot());
        assertFalse(child.isRoot());
    }

    // Tests isActual for whole collection and bounded indices
    @Test
    public void testIsActual_variousIndices_returnsExpected() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("node"), "val");
        pointer.setLength(2);

        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertTrue(pointer.isActual());

        pointer.setIndex(0);
        assertTrue(pointer.isActual());

        pointer.setIndex(1);
        assertTrue(pointer.isActual());

        pointer.setIndex(2);
        assertFalse(pointer.isActual());

        pointer.setIndex(-1);
        assertFalse(pointer.isActual());
    }

    // Tests getValue, getNode, and getNodeValue
    @Test
    public void testGetValueAndGetNode_returnsImmediateNode() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("node"), "hello");
        assertEquals("hello", pointer.getValue());
        assertEquals("hello", pointer.getNode());
        assertEquals("hello", pointer.getNodeValue());
    }

    // Tests getRootNode traversal to the topmost parent
    @Test
    public void testGetRootNode_childNode_returnsRootValue() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "rootVal");
        TestNodePointer child1 = new TestNodePointer(root, new QName("c1"), "c1Val");
        TestNodePointer child2 = new TestNodePointer(child1, new QName("c2"), "c2Val");

        assertEquals("rootVal", child2.getRootNode());
    }

    // Tests node matching with NodeNameTest and NodeTypeTest
    @Test
    public void testTestNode_variousNodeTests_returnsCorrectResult() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("prefix", "localName"), "val");

        assertTrue(pointer.testNode(null));
        assertTrue(pointer.testNode(new NodeNameTest(new QName("prefix", "localName"))));
        assertTrue(pointer.testNode(new NodeNameTest(new QName("prefix", "*"))));
        assertFalse(pointer.testNode(new NodeNameTest(new QName("prefix", "otherName"))));

        NodeTypeTest nodeTypeNode = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(pointer.testNode(nodeTypeNode));

        NodeTypeTest nodeTypeText = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertFalse(pointer.testNode(nodeTypeText));
    }

    // Tests createPath, setValue, and remove
    @Test
    public void testCreatePathAndSetValue_updatesValueAndReturnsPointer() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("node"), "initial");
        NodePointer result = pointer.createPath(null, "updated");
        assertSame(pointer, result);
        assertEquals("updated", pointer.getValue());
        pointer.remove();
        assertEquals("updated", pointer.getValue());
    }

    // Tests exception on unsupported createChild operation
    @Test(expected = JXPathException.class)
    public void testCreateChild_withValue_throwsJXPathException() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("node"), "val");
        pointer.createChild(null, new QName("child"), 0, "childVal");
    }

    // Tests exception on unsupported createAttribute operation
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_throwsJXPathException() {
        TestNodePointer pointer = new TestNodePointer(null, new QName("node"), "val");
        pointer.createAttribute(null, new QName("attr"));
    }

    // Tests getLocale and isLanguage
    @Test
    public void testGetLocaleAndIsLanguage_returnsExpected() {
        TestNodePointer root = new TestNodePointer(null, Locale.CANADA_FRENCH, new QName("root"), "val");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "val");

        assertEquals(Locale.CANADA_FRENCH, child.getLocale());
        assertTrue(child.isLanguage("fr"));
        assertTrue(child.isLanguage("FR-CA"));
        assertFalse(child.isLanguage("en"));
    }

    // Tests asPath generation with attributes and collection indices
    @Test
    public void testAsPath_variousFormats_returnsExpectedPath() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        assertEquals("/root", root.asPath());

        TestNodePointer child = new TestNodePointer(root, new QName("child"), "child");
        assertEquals("/root/child", child.asPath());

        TestNodePointer attr = new TestNodePointer(child, new QName("id"), "100");
        attr.setAttribute(true);
        assertEquals("/root/child/@id", attr.asPath());

        TestNodePointer item = new TestNodePointer(root, new QName("item"), "item1");
        item.setCollection(true);
        item.setIndex(0);
        assertEquals("/root/item[1]", item.asPath());
    }

    // Tests clone operation creates independent copy with cloned parent
    @Test
    public void testClone_clonesPointerAndParent() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "child");

        TestNodePointer clonedChild = (TestNodePointer) child.clone();
        assertNotNull(clonedChild);
        assertNotSame(child, clonedChild);
        assertNotNull(clonedChild.getImmediateParentPointer());
        assertNotSame(root, clonedChild.getImmediateParentPointer());
    }

    // Tests compareTo between pointers sharing the same parent and different depths
    @Test
    public void testCompareTo_sameParentAndDifferentDepths_comparesCorrectly() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "root");
        TestNodePointer childA = new TestNodePointer(root, new QName("a"), "a");
        TestNodePointer childB = new TestNodePointer(root, new QName("b"), "b");

        assertTrue(childA.compareTo(childB) < 0);
        assertTrue(childB.compareTo(childA) > 0);
        assertEquals(0, childA.compareTo(childA));

        TestNodePointer grandChild = new TestNodePointer(childA, new QName("gc"), "gc");
        assertTrue(grandChild.compareTo(childB) < 0);
        assertTrue(childB.compareTo(grandChild) > 0);
    }

    // Tests compareTo between pointers from completely different trees throws JXPathException
    @Test(expected = JXPathException.class)
    public void testCompareTo_differentTrees_throwsJXPathException() {
        TestNodePointer root1 = new TestNodePointer(null, new QName("root1"), "r1");
        TestNodePointer root2 = new TestNodePointer(null, new QName("root2"), "r2");
        root1.compareTo(root2);
    }
}
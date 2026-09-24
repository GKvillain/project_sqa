package org.apache.commons.jxpath.ri.model;

import java.util.Locale;
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
        private boolean leaf = false;
        private boolean container = false;
        private String namespaceURI = null;

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
            return leaf;
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

        public boolean isContainer() {
            return container;
        }

        public void setContainer(boolean container) {
            this.container = container;
        }

        public void setNamespaceURI(String uri) {
            this.namespaceURI = uri;
        }

        public String getNamespaceURI(String prefix) {
            return namespaceURI;
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            if (pointer1 == pointer2) {
                return 0;
            }
            if (pointer1 == null) {
                return -1;
            }
            if (pointer2 == null) {
                return 1;
            }
            String n1 = pointer1.getName() != null ? pointer1.getName().getName() : "";
            String n2 = pointer2.getName() != null ? pointer2.getName().getName() : "";
            return n1.compareTo(n2);
        }
    }

    // Tests newNodePointer with null bean returns NullPointer
    @Test
    public void testNewNodePointer_nullBean_returnsNullPointer() {
        QName name = new QName("test");
        NodePointer pointer = NodePointer.newNodePointer(name, null, Locale.ENGLISH);
        assertNotNull(pointer);
        assertTrue(pointer instanceof NullPointer);
        assertEquals(name, pointer.getName());
    }

    // Tests newNodePointer with standard bean object allocates valid NodePointer
    @Test
    public void testNewNodePointer_validBean_returnsAllocatedPointer() {
        QName name = new QName("root");
        NodePointer pointer = NodePointer.newNodePointer(name, "hello", Locale.ENGLISH);
        assertNotNull(pointer);
        assertEquals("hello", pointer.getNode());
    }

    // Tests getParent unwrapping container parent pointers
    @Test
    public void testGetParent_containerParent_returnsFirstNonContainerParent() {
        TestNodePointer root = new TestNodePointer(null, Locale.ENGLISH, new QName("root"), "rootVal");
        TestNodePointer container = new TestNodePointer(root, new QName("container"), "containerVal");
        container.setContainer(true);
        TestNodePointer child = new TestNodePointer(container, new QName("child"), "childVal");

        assertEquals(root, child.getParent());
        assertEquals(container, child.getImmediateParentPointer());
        assertFalse(child.isRoot());
        assertTrue(root.isRoot());
    }

    // Tests attribute flag setter and getter
    @Test
    public void testSetAttribute_booleanFlag_updatesAttributeState() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("attr"), "val");
        assertFalse(ptr.isAttribute());
        ptr.setAttribute(true);
        assertTrue(ptr.isAttribute());
    }

    // Tests isActual for whole collection and indexed boundaries
    @Test
    public void testIsActual_indexBoundaries_returnsExpectedResult() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("item"), "val");
        ptr.setLength(3);
        ptr.setIndex(NodePointer.WHOLE_COLLECTION);
        assertTrue(ptr.isActual());

        ptr.setIndex(0);
        assertTrue(ptr.isActual());

        ptr.setIndex(2);
        assertTrue(ptr.isActual());

        ptr.setIndex(3);
        assertFalse(ptr.isActual());

        ptr.setIndex(-1);
        assertFalse(ptr.isActual());
    }

    // Tests getRootNode resolution traversing parents
    @Test
    public void testGetRootNode_nestedPointers_returnsRootNode() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "rootVal");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "childVal");
        TestNodePointer grandChild = new TestNodePointer(child, new QName("grandChild"), "grandChildVal");

        assertEquals("rootVal", grandChild.getRootNode());
    }

    // Tests testNode with null NodeTest returns true
    @Test
    public void testTestNode_nullTest_returnsTrue() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("test"), "val");
        assertTrue(ptr.testNode(null));
    }

    // Tests testNode matching NodeNameTest with wildcard and exact match
    @Test
    public void testTestNode_nodeNameTest_returnsCorrectMatch() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("item"), "val");
        NodeNameTest matchTest = new NodeNameTest(new QName("item"));
        NodeNameTest mismatchTest = new NodeNameTest(new QName("other"));
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));

        assertTrue(ptr.testNode(matchTest));
        assertFalse(ptr.testNode(mismatchTest));
        assertTrue(ptr.testNode(wildcardTest));
    }

    // Tests testNode with NodeNameTest on container returns false
    @Test
    public void testTestNode_containerNodeNameTest_returnsFalse() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("container"), "val");
        ptr.setContainer(true);
        NodeNameTest test = new NodeNameTest(new QName("container"));
        assertFalse(ptr.testNode(test));
    }

    // Tests testNode matching NodeTypeTest
    @Test
    public void testTestNode_nodeTypeTest_matchesNodeTypeNode() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("node"), "val");
        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);

        assertTrue(ptr.testNode(nodeTest));
        assertFalse(ptr.testNode(textTest));
    }

    // Tests isLanguage with matching and non-matching language tag
    @Test
    public void testIsLanguage_localeMatching_returnsExpected() {
        TestNodePointer ptr = new TestNodePointer(null, Locale.US, new QName("root"), "val");
        assertTrue(ptr.isLanguage("en"));
        assertTrue(ptr.isLanguage("en-US"));
        assertFalse(ptr.isLanguage("fr"));
    }

    // Tests asPath for root, child, attribute, and indexed elements
    @Test
    public void testAsPath_variousNodeTypes_buildsCorrectXPath() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        assertEquals("/root", root.asPath());

        TestNodePointer child = new TestNodePointer(root, new QName("child"), "childVal");
        assertEquals("/root/child", child.asPath());

        TestNodePointer attr = new TestNodePointer(root, new QName("id"), "123");
        attr.setAttribute(true);
        assertEquals("/root/@id", attr.asPath());

        TestNodePointer elem = new TestNodePointer(root, new QName("items"), "itemVal");
        elem.setCollection(true);
        elem.setIndex(2);
        assertEquals("/root/items[3]", elem.asPath());
    }

    // Tests asPath when parent is a container
    @Test
    public void testAsPath_parentIsContainer_delegatesToParentPath() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        TestNodePointer container = new TestNodePointer(root, new QName("container"), "val");
        container.setContainer(true);
        TestNodePointer child = new TestNodePointer(container, new QName("child"), "val");

        assertEquals(root.asPath(), child.asPath());
    }

    // Tests compareTo for pointers under the same parent
    @Test
    public void testCompareTo_sameParent_returnsComparisonResult() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        TestNodePointer childA = new TestNodePointer(root, new QName("a"), "valA");
        TestNodePointer childB = new TestNodePointer(root, new QName("b"), "valB");

        assertTrue(childA.compareTo(childB) < 0);
        assertTrue(childB.compareTo(childA) > 0);
        assertEquals(0, childA.compareTo(childA));
    }

    // Tests compareTo for pointers of different depths in the same tree
    @Test
    public void testCompareTo_differentDepths_comparesCorrectly() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        TestNodePointer childA = new TestNodePointer(root, new QName("a"), "valA");
        TestNodePointer childB = new TestNodePointer(root, new QName("b"), "valB");
        TestNodePointer subChild = new TestNodePointer(childA, new QName("sub"), "subVal");

        assertTrue(subChild.compareTo(childB) < 0);
        assertTrue(childB.compareTo(subChild) > 0);
    }

    // Tests compareTo between unrelated trees throws JXPathException
    @Test(expected = JXPathException.class)
    public void testCompareTo_differentTrees_throwsJXPathException() {
        TestNodePointer root1 = new TestNodePointer(null, new QName("root1"), "val1");
        TestNodePointer root2 = new TestNodePointer(null, new QName("root2"), "val2");
        TestNodePointer child1 = new TestNodePointer(root1, new QName("child1"), "val1");
        TestNodePointer child2 = new TestNodePointer(root2, new QName("child2"), "val2");

        child1.compareTo(child2);
    }

    // Tests createChild throwing exception for unsupported operation
    @Test(expected = JXPathException.class)
    public void testCreateChild_unsupported_throwsJXPathException() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("test"), "val");
        ptr.createChild(null, new QName("child"), 0, "newVal");
    }

    // Tests createAttribute throwing exception for unsupported operation
    @Test(expected = JXPathException.class)
    public void testCreateAttribute_unsupported_throwsJXPathException() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("test"), "val");
        ptr.createAttribute(null, new QName("attr"));
    }

    // Tests getNamespaceResolver inheritance from parent
    @Test
    public void testGetNamespaceResolver_inheritedFromParent_returnsParentResolver() {
        NamespaceResolver resolver = new NamespaceResolver();
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        root.setNamespaceResolver(resolver);

        TestNodePointer child = new TestNodePointer(root, new QName("child"), "val");
        assertEquals(resolver, child.getNamespaceResolver());
    }

    // Tests clone creates a clone with cloned parent hierarchy
    @Test
    public void testClone_withParent_clonesPointerAndParent() {
        TestNodePointer root = new TestNodePointer(null, Locale.ENGLISH, new QName("root"), "val");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "childVal");

        TestNodePointer clonedChild = (TestNodePointer) child.clone();
        assertNotNull(clonedChild);
        assertNotSame(child, clonedChild);
        assertNotNull(clonedChild.getImmediateParentPointer());
        assertNotSame(child.getImmediateParentPointer(), clonedChild.getImmediateParentPointer());
        assertEquals(child.getName(), clonedChild.getName());
    }
}
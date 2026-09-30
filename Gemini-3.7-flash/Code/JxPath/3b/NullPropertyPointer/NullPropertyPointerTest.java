package org.apache.commons.jxpath.ri.model.beans;

import java.util.HashMap;
import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class NullPropertyPointerTest {

    // Tests default property name and getName
    @Test
    public void testGetName_default_returnsWildcardQName() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertEquals("*", npp.getPropertyName());
        assertEquals(new QName("*"), npp.getName());
    }

    // Tests custom property name setting and getting
    @Test
    public void testSetPropertyName_customName_returnsUpdatedQName() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setPropertyName("testProperty");
        assertEquals("testProperty", npp.getPropertyName());
        assertEquals(new QName("testProperty"), npp.getName());
    }

    // Tests getLength returns 0
    @Test
    public void testGetLength_returnsZero() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertEquals(0, npp.getLength());
    }

    // Tests getBaseValue and getImmediateNode return null
    @Test
    public void testGetBaseValueAndImmediateNode_returnNull() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertNull(npp.getBaseValue());
        assertNull(npp.getImmediateNode());
    }

    // Tests isLeaf returns true
    @Test
    public void testIsLeaf_returnsTrue() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertTrue(npp.isLeaf());
    }

    // Tests isActual, isActualProperty, and isContainer
    @Test
    public void testFlags_isActualAndContainer() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertFalse(npp.isActual());
        assertFalse(npp.isActualProperty());
        assertTrue(npp.isContainer());
    }

    // Tests getPropertyCount and getPropertyNames
    @Test
    public void testGetPropertyCountAndNames_empty() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        assertEquals(0, npp.getPropertyCount());
        assertNotNull(npp.getPropertyNames());
        assertEquals(0, npp.getPropertyNames().length);
    }

    // Tests getValuePointer returns a NullPointer with correct name
    @Test
    public void testGetValuePointer_returnsNullPointer() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setPropertyName("childProp");
        NodePointer vp = npp.getValuePointer();
        assertNotNull(vp);
        assertTrue(vp instanceof NullPointer);
        assertEquals(new QName("childProp"), vp.getName());
    }

    // Tests isCollection with WHOLE_COLLECTION and specific index
    @Test
    public void testIsCollection_indexHandling() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setIndex(PropertyPointer.WHOLE_COLLECTION);
        assertFalse(npp.isCollection());

        npp.setIndex(0);
        assertTrue(npp.isCollection());
    }

    // Tests setPropertyIndex does not fail
    @Test
    public void testSetPropertyIndex_noOp() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setPropertyIndex(5);
        assertEquals(0, npp.getPropertyCount());
    }

    // Tests asPath when byNameAttribute is false
    @Test
    public void testAsPath_standardPropertyPath() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("foo");
        assertEquals("/foo", npp.asPath());
    }

    // Tests asPath when byNameAttribute is true without index
    @Test
    public void testAsPath_nameAttributeWithoutIndex() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("bar");
        assertEquals("/.[@name='bar']", npp.asPath());
    }

    // Tests asPath when byNameAttribute is true with index
    @Test
    public void testAsPath_nameAttributeWithIndex() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("bar");
        npp.setIndex(2);
        assertEquals("/.[@name='bar'][3]", npp.asPath());
    }

    // Tests asPath escaping single and double quotes in attribute value
    @Test
    public void testAsPath_escapingQuotesInNameAttribute() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("a'b\"c");
        assertEquals("/.[@name='a&apos;b&quot;c']", npp.asPath());
    }

    // Tests setValue throws exception when parent is null
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_nullParent_throwsException() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setValue("value");
    }

    // Tests setValue throws exception when parent is container
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_parentIsContainer_throwsException() {
        NullPointer nullParent = new NullPointer(Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(nullParent);
        npp.setValue("value");
    }

    // Tests setValue throws exception when parent does not support dynamic properties
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_nonDynamicParent_throwsException() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setValue("value");
    }

    // Tests setValue delegates to dynamic property owner pointer
    @Test
    public void testSetValue_dynamicPropertyOwnerParent_setsValue() {
        HashMap map = new HashMap();
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), map, Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("dynamicKey");
        npp.setValue("dynamicValue");
        assertEquals("dynamicValue", map.get("dynamicKey"));
    }

    // Tests createPath creates child on context
    @Test
    public void testCreatePath_mapContext_createsProperty() {
        HashMap map = new HashMap();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), map, Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("newKey");

        NodePointer result = npp.createPath(context);
        assertNotNull(result);
        assertEquals(new QName("newKey"), result.getName());
    }

    // Tests createPath with value on dynamic property owner
    @Test
    public void testCreatePathWithValue_mapContext_setsValue() {
        HashMap map = new HashMap();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), map, Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("newKey");

        NodePointer result = npp.createPath(context, "testVal");
        assertNotNull(result);
        assertEquals("testVal", map.get("newKey"));
    }

    // Tests asPath when parent is null and propertyName is set
    @Test
    public void testAsPath_nullParent_standardProperty() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setPropertyName("testProp");
        assertEquals("/testProp", npp.asPath());
    }

    // Tests asPath when parent is null and nameAttributeValue is set
    @Test
    public void testAsPath_nullParent_nameAttribute() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setNameAttributeValue("testAttr");
        assertEquals(".[@name='testAttr']", npp.asPath());
    }

    // Tests asPath when parent is not null and standard property has an index
    @Test
    public void testAsPath_standardPropertyWithIndex() {
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("items");
        npp.setIndex(1);
        assertEquals("/items[2]", npp.asPath());
    }

    // Tests createPath throws exception when parent is null
    @Test(expected = JXPathInvalidAccessException.class)
    public void testCreatePath_nullParent_throwsException() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.createPath(JXPathContext.newContext(new Object()));
    }

    // Tests createPath with value throws exception when parent is null
    @Test(expected = JXPathInvalidAccessException.class)
    public void testCreatePathWithValue_nullParent_throwsException() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.createPath(JXPathContext.newContext(new Object()), "val");
    }

    // Tests createChild delegates properly via createPath
    @Test
    public void testCreateChild_delegatesToCreatePath() {
        HashMap map = new HashMap();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), map, Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("subMap");

        NodePointer childPointer = npp.createChild(context, new QName("childKey"), 0);
        assertNotNull(childPointer);
    }

    // Tests createChild with value delegates properly via createPath
    @Test
    public void testCreateChildWithValue_delegatesToCreatePath() {
        HashMap map = new HashMap();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer parent = NodePointer.newNodePointer(new QName("root"), map, Locale.getDefault());
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("subMap");

        NodePointer childPointer = npp.createChild(context, new QName("childKey"), 0, "childValue");
        assertNotNull(childPointer);
    }
}
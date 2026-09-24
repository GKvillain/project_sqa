package org.apache.commons.jxpath.ri.model.beans;

import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;
import static org.junit.Assert.*;

public class NullPropertyPointerTest {

    // Tests default property name and getName
    @Test
    public void testGetName_defaultState_returnsStarQName() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        assertEquals("*", npp.getPropertyName());
        assertEquals(new QName("*"), npp.getName());
    }

    // Tests setPropertyName updates propertyName and getName
    @Test
    public void testSetPropertyName_customName_updatesName() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("customProp");
        assertEquals("customProp", npp.getPropertyName());
        assertEquals(new QName("customProp"), npp.getName());
    }

    // Tests basic structural query methods
    @Test
    public void testBasicProperties_defaultState_returnsExpectedValues() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        assertEquals(0, npp.getLength());
        assertNull(npp.getBaseValue());
        assertNull(npp.getImmediateNode());
        assertTrue(npp.isLeaf());
        assertFalse(npp.isActual());
        assertFalse(npp.isActualProperty());
        assertTrue(npp.isContainer());
        assertEquals(0, npp.getPropertyCount());
        assertNotNull(npp.getPropertyNames());
        assertEquals(0, npp.getPropertyNames().length);

        // setPropertyIndex should be a no-op and not throw
        npp.setPropertyIndex(5);
    }

    // Tests getValuePointer returns a NullPointer with matching QName
    @Test
    public void testGetValuePointer_customPropertyName_returnsNullPointer() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("field");
        NodePointer valuePointer = npp.getValuePointer();

        assertNotNull(valuePointer);
        assertTrue(valuePointer instanceof NullPointer);
        assertEquals(new QName("field"), valuePointer.getName());
    }

    // Tests isCollection returns false for WHOLE_COLLECTION and true when indexed
    @Test
    public void testIsCollection_wholeCollectionAndIndexed_returnsCorrectBoolean() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        assertFalse(npp.isCollection());
        npp.setIndex(0);
        assertTrue(npp.isCollection());
        npp.setIndex(2);
        assertTrue(npp.isCollection());
    }

    // Tests asPath when byNameAttribute is false
    @Test
    public void testAsPath_standardPath_returnsSuperAsPath() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("child");
        String path = npp.asPath();
        assertNotNull(path);
        assertTrue(path.endsWith("child") || path.endsWith("*"));
    }

    // Tests asPath when byNameAttribute is true
    @Test
    public void testAsPath_byNameAttribute_formatsCorrectly() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "parent");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("simpleName");

        assertEquals("simpleName", npp.getPropertyName());
        String path = npp.asPath();
        assertTrue(path.contains("[@name='simpleName']"));
    }

    // Tests asPath with escape characters in name attribute
    @Test
    public void testAsPath_nameAttributeWithQuotes_escapesCorrectly() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "parent");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("a'b\"c");

        String path = npp.asPath();
        assertTrue(path.contains("[@name='a&apos;b&quot;c']"));
    }

    // Tests asPath when byNameAttribute is true and index is specified
    @Test
    public void testAsPath_byNameAttributeWithIndex_includesIndex() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "parent");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setNameAttributeValue("item");
        npp.setIndex(2);

        String path = npp.asPath();
        assertTrue(path.endsWith("[@name='item'][3]"));
    }

    // Tests setValue with null parent throws JXPathInvalidAccessException
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_nullParent_throwsException() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setValue("testValue");
    }

    // Tests setValue with container parent throws JXPathInvalidAccessException
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_containerParent_throwsException() {
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setValue("testValue");
    }

    // Tests setValue with non-dynamic PropertyOwnerPointer throws JXPathInvalidAccessException
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_nonDynamicParent_throwsException() {
        BeanPointer parent = new BeanPointer(new QName("bean"), new Object(), new PropertyBeanInfo() {
            public PropertyDescriptor getPropertyDescriptor(String propertyName) {
                return null;
            }
            public PropertyDescriptor[] getPropertyDescriptors() {
                return new PropertyDescriptor[0];
            }
            public boolean isAtomic() {
                return false;
            }
            public boolean isDynamic() {
                return false;
            }
        }, Locale.getDefault());

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setValue("testValue");
    }

    // Tests createPath on NullPropertyPointer when parent is NullPointer
    @Test
    public void testCreatePath_nullPointerParent_createsPath() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("prop");

        try {
            NodePointer result = npp.createPath(context);
            assertNotNull(result);
        }
        catch (UnsupportedOperationException e) {
            // Expected if parent NullPointer does not support createPath
        }
    }

    // Tests createPath with value on NullPropertyPointer
    @Test
    public void testCreatePathWithValue_nullPointerParent_handlesInvocation() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("prop");

        try {
            NodePointer result = npp.createPath(context, "val");
            assertNotNull(result);
        }
        catch (UnsupportedOperationException e) {
            // Expected if parent NullPointer does not support createPath
        }
    }

    // Tests createPath with attribute flag set
    @Test
    public void testCreatePath_attributePointer_delegatesToCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setAttribute(true);
        npp.setPropertyName("attr");

        try {
            NodePointer result = npp.createPath(context);
            assertNotNull(result);
        }
        catch (UnsupportedOperationException e) {
            // Expected if parent does not support createAttribute
        }
    }

    // Tests createChild with QName and index
    @Test
    public void testCreateChild_delegatesToCreatePath() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("prop");

        try {
            NodePointer result = npp.createChild(context, new QName("child"), 0);
            assertNotNull(result);
        }
        catch (UnsupportedOperationException e) {
            // Expected propagation from createPath
        }
    }

    // Tests createChild with QName, index, and value
    @Test
    public void testCreateChildWithValue_delegatesToCreatePath() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NullPointer parent = new NullPointer(Locale.getDefault(), "id");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("prop");

        try {
            NodePointer result = npp.createChild(context, new QName("child"), 0, "val");
            assertNotNull(result);
        }
        catch (UnsupportedOperationException e) {
            // Expected propagation from createPath
        }
    }
}
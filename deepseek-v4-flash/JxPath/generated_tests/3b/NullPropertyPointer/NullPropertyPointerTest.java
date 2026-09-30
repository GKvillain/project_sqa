package org.apache.commons.jxpath.ri.model.beans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

public class NullPropertyPointerTest {

    private NullPropertyPointer createDefaultPointer() {
        NodePointer parent = new NullPointer(Locale.getDefault(), "root");
        return new NullPropertyPointer(parent);
    }

    private NullPropertyPointer createPointerWithPropertyName(String propertyName) {
        NullPropertyPointer pointer = createDefaultPointer();
        if (propertyName != null) {
            pointer.setPropertyName(propertyName);
        }
        return pointer;
    }

    // Tests constructor and default property name
    @Test
    public void testConstructor_defaultPropertyNameIsAsterisk() {
        NullPropertyPointer pointer = createDefaultPointer();
        assertEquals("*", pointer.getPropertyName());
        assertEquals("*", pointer.getName().getName());
    }

    // Tests setPropertyName updates property name
    @Test
    public void testSetPropertyName() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setPropertyName("myProp");
        assertEquals("myProp", pointer.getPropertyName());
        assertEquals("myProp", pointer.getName().getName());
    }

    // Tests getLength always returns 0
    @Test
    public void testGetLength_returnsZero() {
        assertEquals(0, createDefaultPointer().getLength());
    }

    // Tests getBaseValue returns null
    @Test
    public void testGetBaseValue_returnsNull() {
        assertNull(createDefaultPointer().getBaseValue());
    }

    // Tests getImmediateNode returns null
    @Test
    public void testGetImmediateNode_returnsNull() {
        assertNull(createDefaultPointer().getImmediateNode());
    }

    // Tests isLeaf returns true
    @Test
    public void testIsLeaf_returnsTrue() {
        assertTrue(createDefaultPointer().isLeaf());
    }

    // Tests isActual returns false
    @Test
    public void testIsActual_returnsFalse() {
        assertFalse(createDefaultPointer().isActual());
    }

    // Tests isContainer returns true
    @Test
    public void testIsContainer_returnsTrue() {
        assertTrue(createDefaultPointer().isContainer());
    }

    // Tests getPropertyCount returns 0
    @Test
    public void testGetPropertyCount_returnsZero() {
        assertEquals(0, createDefaultPointer().getPropertyCount());
    }

    // Tests getPropertyNames returns empty array
    @Test
    public void testGetPropertyNames_returnsEmptyArray() {
        String[] names = createDefaultPointer().getPropertyNames();
        assertNotNull(names);
        assertEquals(0, names.length);
    }

    // Tests isCollection returns false when index is default (WHOLE_COLLECTION)
    @Test
    public void testIsCollection_whenIndexDefault_returnsFalse() {
        assertFalse(createDefaultPointer().isCollection());
    }

    // Tests setValue throws exception when parent is container
    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValue_whenParentIsContainer_throwsException() {
        createDefaultPointer().setValue("someValue");
    }

    // Tests setNameAttributeValue updates property name and affects asPath
    @Test
    public void testSetNameAttributeValue_updatesPropertyNameAndAsPath() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setNameAttributeValue("attributeName");
        assertEquals("attributeName", pointer.getPropertyName());
        assertTrue(pointer.asPath().contains("[@name='attributeName']"));
    }

    // Tests asPath escaping of single quote
    @Test
    public void testAsPath_byNameAttribute_withSingleQuote() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setNameAttributeValue("it's");
        assertTrue(pointer.asPath().contains("[@name='it&apos;s']"));
    }

    // Tests asPath escaping of double quote
    @Test
    public void testAsPath_byNameAttribute_withDoubleQuote() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setNameAttributeValue("say \"hello\"");
        assertTrue(pointer.asPath().contains("[@name='say &quot;hello&quot;']"));
    }

    // Tests asPath escaping of both quotes
    @Test
    public void testAsPath_byNameAttribute_withBothQuotes() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setNameAttributeValue("a'b\"c");
        assertTrue(pointer.asPath().contains("[@name='a&apos;b&quot;c']"));
    }

    // Tests asPath without byNameAttribute returns non-null
    @Test
    public void testAsPath_withoutByAttribute() {
        assertNotNull(createPointerWithPropertyName("prop").asPath());
    }

    // Tests getValuePointer returns NullPointer with correct name
    @Test
    public void testGetValuePointer_returnsNullPointerWithCorrectName() {
        NullPropertyPointer pointer = createPointerWithPropertyName("propName");
        NodePointer valuePointer = pointer.getValuePointer();
        assertTrue(valuePointer instanceof NullPointer);
        assertEquals("propName", valuePointer.getName().getName());
    }

    // Tests setPropertyIndex does not affect isCollection
    @Test
    public void testSetPropertyIndex_doesNotAffectIsCollection() {
        NullPropertyPointer pointer = createDefaultPointer();
        pointer.setPropertyIndex(5);
        assertFalse(pointer.isCollection());
    }

    // Tests isActualProperty returns false
    @Test
    public void testIsActualProperty_returnsFalse() {
        assertFalse(createDefaultPointer().isActualProperty());
    }
}
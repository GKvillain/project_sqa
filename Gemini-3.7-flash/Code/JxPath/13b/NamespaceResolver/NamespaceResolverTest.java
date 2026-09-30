package org.apache.commons.jxpath.ri;

import java.util.Locale;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NamespaceResolverTest {

    private NamespaceResolver resolver;

    @Before
    public void setUp() {
        resolver = new NamespaceResolver();
    }

    // Tests default constructor and unsealed state
    @Test
    public void testIsSealed_defaultConstructor_returnsFalse() {
        assertFalse(resolver.isSealed());
    }

    // Tests sealing of resolver without parent
    @Test
    public void testSeal_withoutParent_setsSealedToTrue() {
        resolver.seal();
        assertTrue(resolver.isSealed());
    }

    // Tests sealing of resolver cascades to parent
    @Test
    public void testSeal_withParent_sealsBothChildAndParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);

        child.seal();

        assertTrue(child.isSealed());
        assertTrue(parent.isSealed());
    }

    // Tests registering namespace under normal condition
    @Test
    public void testRegisterNamespace_unsealed_registersSuccessfully() {
        resolver.registerNamespace("prefix1", "http://example.com/ns1");
        assertEquals("http://example.com/ns1", resolver.getNamespaceURI("prefix1"));
    }

    // Tests exception path when registering namespace on sealed resolver
    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespace_whenSealed_throwsIllegalStateException() {
        resolver.seal();
        resolver.registerNamespace("prefix1", "http://example.com/ns1");
    }

    // Tests retrieval of registered namespace URI
    @Test
    public void testGetNamespaceURI_registeredPrefix_returnsCorrectURI() {
        resolver.registerNamespace("ns", "http://example.com/ns");
        assertEquals("http://example.com/ns", resolver.getNamespaceURI("ns"));
    }

    // Tests retrieval of namespace URI from parent resolver
    @Test
    public void testGetNamespaceURI_prefixInParent_returnsParentURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentNs", "http://example.com/parent");
        NamespaceResolver child = new NamespaceResolver(parent);

        assertEquals("http://example.com/parent", child.getNamespaceURI("parentNs"));
    }

    // Tests retrieval of unregistered namespace URI returns null
    @Test
    public void testGetNamespaceURI_unregisteredPrefix_returnsNull() {
        assertNull(resolver.getNamespaceURI("unknown"));
    }

    // Tests defect where pointer is null when resolving prefix
    @Test
    public void testGetPrefix_nullPointerRegisteredNamespace_returnsPrefix() {
        resolver.registerNamespace("ns", "http://example.com/ns");
        assertEquals("ns", resolver.getPrefix("http://example.com/ns"));
    }

    // Tests getPrefix delegation to parent resolver when pointer is null
    @Test
    public void testGetPrefix_fromParentResolver_returnsPrefix() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentNs", "http://example.com/parent");
        NamespaceResolver child = new NamespaceResolver(parent);

        assertEquals("parentNs", child.getPrefix("http://example.com/parent"));
    }

    // Tests getPrefix with unregistered URI returns null
    @Test
    public void testGetPrefix_unregisteredURI_returnsNull() {
        assertNull(resolver.getPrefix("http://example.com/unknown"));
    }

    // Tests getNamespaceContextPointer when pointer is null and no parent
    @Test
    public void testGetNamespaceContextPointer_nullPointerNoParent_returnsNull() {
        assertNull(resolver.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer delegates to parent when child pointer is null
    @Test
    public void testGetNamespaceContextPointer_nullPointerWithParent_returnsParentPointer() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);

        assertNull(child.getNamespaceContextPointer());
    }

    // Tests clone returns a new instance with sealed set to false
    @Test
    public void testClone_sealedResolver_returnsUnsealedClone() {
        resolver.registerNamespace("ns", "http://example.com/ns");
        resolver.seal();
        assertTrue(resolver.isSealed());

        NamespaceResolver cloned = (NamespaceResolver) resolver.clone();

        assertNotNull(cloned);
        assertNotSame(resolver, cloned);
        assertFalse(cloned.isSealed());
        assertEquals("http://example.com/ns", cloned.getNamespaceURI("ns"));
    }

    // Tests setting and getting namespace context pointer directly
    @Test
    public void testSetNamespaceContextPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        resolver.setNamespaceContextPointer(pointer);
        assertEquals(pointer, resolver.getNamespaceContextPointer());
    }

    // Tests getting namespace context pointer delegated from parent
    @Test
    public void testGetNamespaceContextPointer_fromParent() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        NamespaceResolver parent = new NamespaceResolver();
        parent.setNamespaceContextPointer(pointer);
        NamespaceResolver child = new NamespaceResolver(parent);

        assertEquals(pointer, child.getNamespaceContextPointer());
    }

    // Tests static getPrefix with null pointer returns null
    @Test
    public void testStaticGetPrefix_nullPointer_returnsNull() {
        assertNull(NamespaceResolver.getPrefix(null, "http://example.com"));
    }

    // Tests static getPrefix with non-null pointer for nonexistent URI returns null
    @Test
    public void testStaticGetPrefix_withPointer_returnsNull() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        assertNull(NamespaceResolver.getPrefix(pointer, "http://example.com/nonexistent"));
    }

    // Tests getNamespaceURI with pointer set when prefix is not in map
    @Test
    public void testGetNamespaceURI_withPointer_returnsNullForUnknownPrefix() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        resolver.setNamespaceContextPointer(pointer);
        assertNull(resolver.getNamespaceURI("unknownPrefix"));
    }

    // Tests getPrefix delegation to parent when pointer does not resolve the prefix
    @Test
    public void testGetPrefix_withPointerResolvingNull_delegatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("pNs", "http://example.com/parent");
        NamespaceResolver child = new NamespaceResolver(parent);
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        child.setNamespaceContextPointer(pointer);

        assertEquals("pNs", child.getPrefix("http://example.com/parent"));
    }

    // Tests clone preserves pointer and namespace mappings
    @Test
    public void testClone_withParentAndPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.ENGLISH);
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        child.setNamespaceContextPointer(pointer);
        child.registerNamespace("childNs", "http://example.com/child");

        NamespaceResolver cloned = (NamespaceResolver) child.clone();

        assertNotNull(cloned);
        assertNotSame(child, cloned);
        assertEquals(pointer, cloned.getNamespaceContextPointer());
        assertEquals("http://example.com/child", cloned.getNamespaceURI("childNs"));
    }
}
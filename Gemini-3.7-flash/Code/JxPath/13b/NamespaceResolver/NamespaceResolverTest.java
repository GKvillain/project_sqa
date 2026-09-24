package org.apache.commons.jxpath.ri;

import java.util.Locale;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NamespaceResolverTest {

    private NamespaceResolver resolver;

    @Before
    public void setUp() {
        resolver = new NamespaceResolver();
    }

    // Tests default state: resolver is not sealed
    @Test
    public void testIsSealed_defaultState_returnsFalse() {
        assertFalse(resolver.isSealed());
    }

    // Tests sealing the resolver updates sealed state
    @Test
    public void testSeal_onResolver_setsSealedToTrue() {
        resolver.seal();
        assertTrue(resolver.isSealed());
    }

    // Tests sealing also seals parent resolver
    @Test
    public void testSeal_withParent_sealsParentAsWell() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        child.seal();
        assertTrue(child.isSealed());
        assertTrue(parent.isSealed());
    }

    // Tests registering a namespace on an unsealed resolver
    @Test
    public void testRegisterNamespace_unsealedResolver_registersSuccessfully() {
        resolver.registerNamespace("ns", "http://example.com/ns");
        assertEquals("http://example.com/ns", resolver.getNamespaceURI("ns"));
    }

    // Tests exception thrown when registering namespace on a sealed resolver
    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespace_sealedResolver_throwsException() {
        resolver.seal();
        resolver.registerNamespace("ns", "http://example.com/ns");
    }

    // Tests getNamespaceURI when prefix is not registered and returns null
    @Test
    public void testGetNamespaceURI_unknownPrefix_returnsNull() {
        assertNull(resolver.getNamespaceURI("unknown"));
    }

    // Tests getNamespaceURI falls back to parent resolver
    @Test
    public void testGetNamespaceURI_registeredInParent_returnsParentURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("ns", "http://example.com/parent");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("http://example.com/parent", child.getNamespaceURI("ns"));
    }

    // Tests getNamespaceURI prioritizes child over parent
    @Test
    public void testGetNamespaceURI_registeredInBoth_returnsChildURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("ns", "http://example.com/parent");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("ns", "http://example.com/child");
        assertEquals("http://example.com/child", child.getNamespaceURI("ns"));
    }

    // Tests getPrefix retrieves prefix when pointer is null (Defects4J 13b defect detection)
    @Test
    public void testGetPrefix_nullPointerWithRegisteredNamespace_returnsPrefix() {
        resolver.registerNamespace("prefix1", "http://example.com/ns1");
        assertEquals("prefix1", resolver.getPrefix("http://example.com/ns1"));
    }

    // Tests getPrefix retrieves prefix from parent resolver when pointer is null
    @Test
    public void testGetPrefix_nullPointerWithParent_returnsPrefixFromParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentPrefix", "http://example.com/parentNs");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("parentPrefix", child.getPrefix("http://example.com/parentNs"));
    }

    // Tests getPrefix returns null for unknown namespace URI
    @Test
    public void testGetPrefix_unknownURI_returnsNull() {
        resolver.registerNamespace("prefix1", "http://example.com/ns1");
        assertNull(resolver.getPrefix("http://example.com/unknown"));
    }

    // Tests getNamespaceContextPointer returns null by default
    @Test
    public void testGetNamespaceContextPointer_default_returnsNull() {
        assertNull(resolver.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer falls back to parent when pointer is null
    @Test
    public void testGetNamespaceContextPointer_delegatesToParent_whenNull() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        assertNull(child.getNamespaceContextPointer());
    }

    // Tests clone creates unsealed copy with independent state
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

    // Tests re-registering namespace clears reverse map cache
    @Test
    public void testRegisterNamespace_afterGetPrefix_clearsReverseMap() {
        resolver.registerNamespace("p1", "http://example.com/1");
        assertEquals("p1", resolver.getPrefix("http://example.com/1"));

        resolver.registerNamespace("p2", "http://example.com/2");
        assertEquals("p2", resolver.getPrefix("http://example.com/2"));
    }

    // Tests setting and getting namespace context pointer
    @Test
    public void testSetAndGetNamespaceContextPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.US);
        resolver.setNamespaceContextPointer(pointer);
        assertSame(pointer, resolver.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer retrieves pointer from parent when child pointer is null
    @Test
    public void testGetNamespaceContextPointer_fromParent() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.US);
        NamespaceResolver parent = new NamespaceResolver();
        parent.setNamespaceContextPointer(pointer);

        NamespaceResolver child = new NamespaceResolver(parent);
        assertSame(pointer, child.getNamespaceContextPointer());
    }

    // Tests clone on an empty unsealed resolver without registered namespaces
    @Test
    public void testClone_emptyResolver() {
        NamespaceResolver cloned = (NamespaceResolver) resolver.clone();
        assertNotNull(cloned);
        assertNotSame(resolver, cloned);
        assertFalse(cloned.isSealed());
        assertNull(cloned.getNamespaceURI("any"));
    }

    // Tests getNamespaceURI delegates to context pointer when prefix is not registered locally
    @Test
    public void testGetNamespaceURI_fromContextPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.US);
        resolver.setNamespaceContextPointer(pointer);
        assertNull(resolver.getNamespaceURI("unregistered"));
    }

    // Tests getPrefix delegates to context pointer when URI is not registered locally
    @Test
    public void testGetPrefix_fromContextPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("root"), "test", Locale.US);
        resolver.setNamespaceContextPointer(pointer);
        assertNull(resolver.getPrefix("http://example.com/unregistered"));
    }
}
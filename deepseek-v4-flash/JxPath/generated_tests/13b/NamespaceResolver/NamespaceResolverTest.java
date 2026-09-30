package org.apache.commons.jxpath.ri;

import org.junit.Test;
import static org.junit.Assert.*;

public class NamespaceResolverTest {

    // Tests registerNamespace and getNamespaceURI for a valid prefix
    @Test
    public void testRegisterNamespace_validInput_returnsRegisteredURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("foo", "http://example.com/foo");
        String uri = resolver.getNamespaceURI("foo");
        assertEquals("http://example.com/foo", uri);
    }

    // Tests getNamespaceURI for an unknown prefix returns null
    @Test
    public void testGetNamespaceURI_unknownPrefix_returnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceURI("unknown"));
    }

    // Tests getNamespaceURI delegates to parent resolver
    @Test
    public void testGetNamespaceURI_parentHasMapping_returnsParentURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parent", "http://parent");
        NamespaceResolver child = new NamespaceResolver(parent);
        String uri = child.getNamespaceURI("parent");
        assertEquals("http://parent", uri);
    }

    // Tests getPrefix with registered mapping and no pointer set (triggers defect)
    @Test
    public void testGetPrefix_registeredMappingWithoutPointer_returnsPrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "http://pre");
        String prefix = resolver.getPrefix("http://pre");
        assertEquals("pre", prefix);
    }

    // Tests getPrefix for an unknown namespace returns null
    @Test
    public void testGetPrefix_unknownNamespace_returnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getPrefix("http://unknown"));
    }

    // Tests getPrefix delegates to parent resolver
    @Test
    public void testGetPrefix_parentHasMapping_returnsParentPrefix() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("par", "http://par");
        NamespaceResolver child = new NamespaceResolver(parent);
        String prefix = child.getPrefix("http://par");
        assertEquals("par", prefix);
    }

    // Tests getPrefix cache behavior (reverseMap reused)
    @Test
    public void testGetPrefix_cacheUsed_returnsSamePrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("x", "http://x");
        String prefix1 = resolver.getPrefix("http://x");
        assertEquals("x", prefix1);
        String prefix2 = resolver.getPrefix("http://x");
        assertEquals("x", prefix2);
    }

    // Tests isSealed returns false initially
    @Test
    public void testIsSealed_initialState_returnsFalse() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertFalse(resolver.isSealed());
    }

    // Tests isSealed returns true after seal
    @Test
    public void testSeal_afterSeal_isSealedReturnsTrue() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        assertTrue(resolver.isSealed());
    }

    // Tests registerNamespace after seal throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespace_afterSeal_throwsIllegalStateException() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.registerNamespace("try", "http://try");
    }

    // Tests clone produces an unsealed copy
    @Test
    public void testClone_clonedResolver_isNotSealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertFalse(clone.isSealed());
    }

    // Tests getNamespaceContextPointer returns null when no context set
    @Test
    public void testGetNamespaceContextPointer_noContext_returnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceContextPointer());
    }

    // Tests setNamespaceContextPointer(null) sets pointer to null
    @Test
    public void testSetNamespaceContextPointer_null_setsPointerToNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(null);
        assertNull(resolver.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer returns null when parent also has no context
    @Test
    public void testGetNamespaceContextPointer_parentNoContext_returnsNull() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        assertNull(child.getNamespaceContextPointer());
    }

    // Tests seal propagates to parent
    @Test
    public void testSeal_childSealed_parentAlsoSealed() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        child.seal();
        assertTrue(child.isSealed());
        assertTrue(parent.isSealed());
    }

    // ========== Additional tests for uncovered coverage ==========

    // Tests setNamespaceContextPointer with a non-null pointer
    @Test
    public void testSetNamespaceContextPointer_nonNullPointer_returnsSamePointer() {
        NamespaceResolver resolver = new NamespaceResolver();
        Object pointer = new Object();
        resolver.setNamespaceContextPointer(pointer);
        assertSame(pointer, resolver.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer when child sets own pointer (parent not set)
    @Test
    public void testGetNamespaceContextPointer_childSetParentNotSet_returnsChildPointer() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        Object pointer = new Object();
        child.setNamespaceContextPointer(pointer);
        assertSame(pointer, child.getNamespaceContextPointer());
    }

    // Tests getNamespaceContextPointer when both parent and child have pointers, child's is returned
    @Test
    public void testGetNamespaceContextPointer_childSetParentSet_returnsChildPointer() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        Object parentPointer = new Object();
        Object childPointer = new Object();
        parent.setNamespaceContextPointer(parentPointer);
        child.setNamespaceContextPointer(childPointer);
        assertSame(childPointer, child.getNamespaceContextPointer());
    }

    // Tests that clone preserves registered namespace mappings
    @Test
    public void testClone_clonedResolver_preservesMapping() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "http://pre");
        resolver.registerNamespace("pre2", "http://pre2");
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertEquals("http://pre", clone.getNamespaceURI("pre"));
        assertEquals("http://pre2", clone.getNamespaceURI("pre2"));
        assertEquals("pre", clone.getPrefix("http://pre"));
        assertEquals("pre2", clone.getPrefix("http://pre2"));
    }

    // Tests that duplicate prefix registration updates the URI
    @Test
    public void testRegisterNamespace_duplicatePrefix_updatesURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("dup", "http://old");
        resolver.registerNamespace("dup", "http://new");
        assertEquals("http://new", resolver.getNamespaceURI("dup"));
    }

    // Tests getNamespaceURI with a pointer set (should not affect normal resolution)
    @Test
    public void testGetNamespaceURI_pointerSet_registeredPrefix_returnsURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer("context");
        resolver.registerNamespace("p", "http://p");
        assertEquals("http://p", resolver.getNamespaceURI("p"));
    }

    // Tests getPrefix with a pointer set and unknown namespace (should return null)
    @Test
    public void testGetPrefix_pointerSet_unknownNamespace_returnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer("context");
        assertNull(resolver.getPrefix("http://unknown"));
    }
}
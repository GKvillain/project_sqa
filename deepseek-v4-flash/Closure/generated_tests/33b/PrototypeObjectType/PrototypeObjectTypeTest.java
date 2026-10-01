package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test cases for PrototypeObjectType.
 * Focuses on property management, prototype chain, and key behavioral methods.
 * Designed to detect bugs related to Defects4J closure-33b.
 */
public class PrototypeObjectTypeTest {

    private JSTypeRegistry registry;
    private ObjectType nativeObjectProto;
    private PrototypeObjectType testObject;
    private PrototypeObjectType nullProtoObject;

    @Before
    public void setUp() {
        // Create a dummy ErrorReporter to satisfy the JSTypeRegistry constructor
        ErrorReporter reporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, int lineOffset) {
                // ignore
            }

            @Override
            public void error(String message, String sourceName, int line, int lineOffset) {
                // ignore
            }
        };
        registry = new JSTypeRegistry(reporter);
        nativeObjectProto = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);

        // Create a normal object with an implicit prototype (Object.prototype)
        testObject = new PrototypeObjectType(registry, "TestClass", nativeObjectProto, false);

        // Create an object with nativeType=true and null implicit prototype.
        // This forces the implicit prototype to be null (constructor sets it directly).
        nullProtoObject = new PrototypeObjectType(registry, "NullProto", null, true);
    }

    // Helper to create a simple Node mock (we don't need actual AST).
    private static Node dummyNode() {
        return null; // many methods accept null Node
    }

    // ========== Constructor and basic structure ==========

    @Test
    public void testConstructor_defaultPrototypeSet() {
        assertNotNull(testObject.getImplicitPrototype());
        assertEquals(nativeObjectProto, testObject.getImplicitPrototype());
    }

    @Test
    public void testConstructor_nullImplicitPrototypeNativeType() {
        assertNull(nullProtoObject.getImplicitPrototype());
    }

    // ========== defineProperty and property existence ==========

    @Test
    public void testDefineProperty_addsProperty() {
        JSType numberType = registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE);
        assertTrue(testObject.defineProperty("x", numberType, false, dummyNode()));
        assertTrue(testObject.hasOwnProperty("x"));
        assertEquals(numberType, testObject.getPropertyType("x"));
    }

    @Test
    public void testDefineProperty_declaredPropertyCannotBeRedefined() {
        testObject.defineProperty("x", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        assertFalse(testObject.defineProperty("x", registry.getNativeObjectType(JSTypeNative.STRING_TYPE), false, dummyNode()));
    }

    // ========== getSlot ==========

    @Test
    public void testGetSlot_existingProperty_returnsProperty() {
        testObject.defineProperty("a", registry.getNativeObjectType(JSTypeNative.BOOLEAN_TYPE), false, dummyNode());
        Property slot = testObject.getSlot("a");
        assertNotNull(slot);
        assertEquals("a", slot.getName());
        assertEquals(registry.getNativeObjectType(JSTypeNative.BOOLEAN_TYPE), slot.getType());
    }

    @Test
    public void testGetSlot_missingProperty_returnsNull() {
        assertNull(testObject.getSlot("nonexistent"));
    }

    @Test
    public void testGetSlot_propertyFromPrototype_returnsProperty() {
        // Object.prototype usually has "toString" – we check that it is accessible
        Property slot = testObject.getSlot("toString");
        assertNotNull(slot);
    }

    @Test
    public void testGetSlot_nullImplicitPrototype_noException() {
        // Should not throw NullPointerException
        assertNull(nullProtoObject.getSlot("any"));
    }

    // ========== hasProperty ==========

    @Test
    public void testHasProperty_ownProperty_returnsTrue() {
        testObject.defineProperty("p", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        assertTrue(testObject.hasProperty("p"));
    }

    @Test
    public void testHasProperty_inheritedProperty_returnsTrue() {
        // "toString" exists on Object.prototype
        assertTrue(testObject.hasProperty("toString"));
    }

    @Test
    public void testHasProperty_unknown_returnsFalse() {
        assertFalse(testObject.hasProperty("xyz"));
    }

    @Test
    public void testHasProperty_nullPrototype_noError() {
        assertFalse(nullProtoObject.hasProperty("anything"));
    }

    // ========== getPropertiesCount ==========

    @Test
    public void testGetPropertiesCount_withPrototype() {
        // At least satisfies: prototype properties + local properties
        testObject.defineProperty("local", registry.getNativeObjectType(JSTypeNative.VOID_TYPE), false, dummyNode());
        int count = testObject.getPropertiesCount();
        assertTrue("Count should be at least 1 (local property)", count >= 1);
    }

    @Test
    public void testGetPropertiesCount_nullPrototype() {
        nullProtoObject.defineProperty("a", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        assertEquals(1, nullProtoObject.getPropertiesCount());
    }

    // ========== hasOwnProperty ==========

    @Test
    public void testHasOwnProperty_own_returnsTrue() {
        testObject.defineProperty("x", registry.getNativeObjectType(JSTypeNative.STRING_TYPE), false, dummyNode());
        assertTrue(testObject.hasOwnProperty("x"));
    }

    @Test
    public void testHasOwnProperty_inherited_returnsFalse() {
        assertFalse(testObject.hasOwnProperty("toString"));
    }

    // ========== isPropertyTypeDeclared / Inferred ==========

    @Test
    public void testIsPropertyTypeDeclared_declaredProperty_returnsTrue() {
        testObject.defineProperty("d", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        assertTrue(testObject.isPropertyTypeDeclared("d"));
        assertFalse(testObject.isPropertyTypeInferred("d"));
    }

    @Test
    public void testIsPropertyTypeDeclared_inferredProperty_returnsFalse() {
        testObject.defineProperty("i", registry.getNativeObjectType(JSTypeNative.STRING_TYPE), true, dummyNode());
        assertFalse(testObject.isPropertyTypeDeclared("i"));
        assertTrue(testObject.isPropertyTypeInferred("i"));
    }

    @Test
    public void testIsPropertyTypeDeclared_nonexistentProperty_returnsFalse() {
        assertFalse(testObject.isPropertyTypeDeclared("nope"));
        assertFalse(testObject.isPropertyTypeInferred("nope"));
    }

    // ========== matchesNumberContext / matchesStringContext ==========

    @Test
    public void testMatchesNumberContext_overriddenValueOf_returnsTrue() {
        // Override "valueOf" with a different type (e.g., Number)
        JSType numberType = registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE);
        testObject.defineProperty("valueOf", numberType, false, dummyNode());
        assertTrue(testObject.matchesNumberContext());
    }

    @Test
    public void testMatchesStringContext_overriddenToString_returnsTrue() {
        JSType stringType = registry.getNativeObjectType(JSTypeNative.STRING_TYPE);
        testObject.defineProperty("toString", stringType, false, dummyNode());
        assertTrue(testObject.matchesStringContext());
    }

    @Test
    public void testMatchesNumberContext_nativeObject_returnsFalse() {
        // For a native object (nativeType=true) overridden native properties are ignored.
        PrototypeObjectType nativeObj = new PrototypeObjectType(registry, "Native", nativeObjectProto, true);
        nativeObj.defineProperty("valueOf", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        assertFalse(nativeObj.matchesNumberContext());
    }

    // ========== toStringHelper ==========

    @Test
    public void testToStringHelper_hasReferenceName_returnsName() {
        assertEquals("TestClass", testObject.toStringHelper(false));
    }

    @Test
    public void testToStringHelper_prettyPrint_noReferenceName() {
        // Create an object without a reference name
        PrototypeObjectType anonymous = new PrototypeObjectType(registry, null, nativeObjectProto, false);
        anonymous.setPrettyPrint(true);
        anonymous.defineProperty("a", registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE), false, dummyNode());
        String printed = anonymous.toStringHelper(false);
        assertTrue(printed.startsWith("{"));
        assertTrue(printed.contains("a"));
        assertTrue(printed.endsWith("}"));
    }

    @Test
    public void testToStringHelper_prettyPrint_notEnabled_returnsPlaceholder() {
        PrototypeObjectType anonymous = new PrototypeObjectType(registry, null, nativeObjectProto, false);
        anonymous.setPrettyPrint(false);
        assertEquals("{...}", anonymous.toStringHelper(false));
    }

    // ========== getReferenceName ==========

    @Test
    public void testGetReferenceName_withClassName_returnsClassName() {
        assertEquals("TestClass", testObject.getReferenceName());
    }

    @Test
    public void testGetReferenceName_nullClassNameAndNoOwner_returnsNull() {
        PrototypeObjectType noName = new PrototypeObjectType(registry, null, nativeObjectProto, false);
        assertNull(noName.getReferenceName());
    }

    // ========== setImplicitPrototype ==========

    @Test
    public void testSetImplicitPrototype_beforeCachedValues_succeeds() {
        // should not throw if hasCachedValues() is false (initial state)
        ObjectType newProto = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
        testObject.setImplicitPrototype(newProto);
        assertEquals(newProto, testObject.getImplicitPrototype());
    }

    // Note: Testing setImplicitPrototype when hasCachedValues() is true is tricky
    // because it would throw an exception (checkState). We rely on the existing constraint.

    // ========== setOwnerFunction / getOwnerFunction ==========

    @Test
    public void testSetOwnerFunction_setsOwner() {
        // null is a valid argument to reset the owner
        testObject.setOwnerFunction(null);
        assertNull(testObject.getOwnerFunction());
    }

    // ========== Misc ==========

    @Test
    public void testRemoveProperty_removesOwnProperty() {
        testObject.defineProperty("r", registry.getNativeObjectType(JSTypeNative.BOOLEAN_TYPE), false, dummyNode());
        assertTrue(testObject.removeProperty("r"));
        assertFalse(testObject.hasOwnProperty("r"));
    }

    @Test
    public void testRemoveProperty_nonExistentProperty_returnsFalse() {
        assertFalse(testObject.removeProperty("hello"));
    }

    @Test
    public void testMatchesObjectContext_alwaysTrue() {
        assertTrue(testObject.matchesObjectContext());
    }

    @Test
    public void testCanBeCalled_nonRegexp_returnsFalse() {
        assertFalse(testObject.canBeCalled());
    }

    @Test
    public void testIsNativeObjectType_nativeTypeTrue_returnsTrue() {
        assertTrue(nullProtoObject.isNativeObjectType());
    }

    @Test
    public void testIsNativeObjectType_nativeTypeFalse_returnsFalse() {
        assertFalse(testObject.isNativeObjectType());
    }
}
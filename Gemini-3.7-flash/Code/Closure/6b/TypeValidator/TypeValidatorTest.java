package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class TypeValidatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;
  private NodeTraversal traversal;
  private Node dummyNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, null);
    dummyNode = new Node(Token.NAME);
  }

  // Tests expectObject with valid Object type and primitive type
  @Test
  public void testExpectObject_matchingAndNonMatching() {
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    assertTrue(validator.expectObject(traversal, dummyNode, objectType, "expected object"));
    assertEquals(0, compiler.getWarningCount());

    assertFalse(validator.expectObject(traversal, dummyNode, numberType, "expected object"));
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectActualObject with primitive and object types
  @Test
  public void testExpectActualObject_primitiveType_emitsWarning() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    validator.expectActualObject(traversal, dummyNode, numberType, "expected actual object");
    assertEquals(1, compiler.getWarningCount());

    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    validator.expectActualObject(traversal, dummyNode, objectType, "expected actual object");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectAnyObject with non-object type
  @Test
  public void testExpectAnyObject_numberType_emitsWarning() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    validator.expectAnyObject(traversal, dummyNode, numberType, "expected any object");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectString with string and boolean types
  @Test
  public void testExpectString_stringAndBooleanTypes() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    validator.expectString(traversal, dummyNode, stringType, "expected string");
    assertEquals(0, compiler.getWarningCount());

    validator.expectString(traversal, dummyNode, booleanType, "expected string");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNumber with number and string types
  @Test
  public void testExpectNumber_numberAndStringTypes() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectNumber(traversal, dummyNode, numberType, "expected number");
    assertEquals(0, compiler.getWarningCount());

    validator.expectNumber(traversal, dummyNode, stringType, "expected number");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectBitwiseable with valid primitive and object types
  @Test
  public void testExpectBitwiseable_primitiveAndObjectType() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectBitwiseable(traversal, dummyNode, numberType, "expected bitwiseable");
    assertEquals(0, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, dummyNode, objectType, "expected bitwiseable");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectStringOrNumber with boolean type
  @Test
  public void testExpectStringOrNumber_booleanType_emitsWarning() {
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    validator.expectStringOrNumber(traversal, dummyNode, booleanType, "expected string or number");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with null type and object type
  @Test
  public void testExpectNotNullOrUndefined_nullAndObjectType() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    assertFalse(validator.expectNotNullOrUndefined(traversal, dummyNode, nullType, "null check", objectType));
    assertEquals(1, compiler.getWarningCount());

    assertTrue(validator.expectNotNullOrUndefined(traversal, dummyNode, objectType, "object check", objectType));
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectSwitchMatchesCase with mismatched switch and case types
  @Test
  public void testExpectSwitchMatchesCase_mismatchedTypes_emitsWarning() {
    Node switchNode = new Node(Token.SWITCH, new Node(Token.NAME));
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectSwitchMatchesCase(traversal, switchNode, stringType, numberType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectIndexMatch on struct object type
  @Test
  public void testExpectIndexMatch_structObject_emitsIllegalAccessWarning() {
    Node getElem = new Node(Token.GETELEM, new Node(Token.NAME), new Node(Token.STRING));
    ObjectType structType = registry.createObjectType("StructType", (ObjectType) null);
    structType.setStruct();
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectIndexMatch(traversal, getElem, structType, stringType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with constructor types to verify mismatch registration
  @Test
  public void testExpectCanAssignTo_constructorTypes_registersMismatch() {
    FunctionType ctor1 = registry.createConstructorType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType ctor2 = registry.createConstructorType(registry.getNativeType(JSTypeNative.STRING_TYPE));

    boolean result = validator.expectCanAssignTo(traversal, dummyNode, ctor1, ctor2, "type mismatch");
    assertFalse(result);

    Iterator<TypeValidator.TypeMismatch> mismatches = validator.getMismatches().iterator();
    assertTrue(mismatches.hasNext());
    TypeValidator.TypeMismatch mismatch = mismatches.next();
    assertEquals(ctor1, mismatch.typeA);
    assertEquals(ctor2, mismatch.typeB);
  }

  // Tests expectCanAssignToPropertyOf with incompatible types
  @Test
  public void testExpectCanAssignToPropertyOf_incompatibleTypes_returnsFalseAndWarns() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Node owner = new Node(Token.NAME);
    owner.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));

    boolean result = validator.expectCanAssignToPropertyOf(traversal, dummyNode, stringType, numberType, owner, "prop");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectArgumentMatchesParameter with incompatible argument type
  @Test
  public void testExpectArgumentMatchesParameter_incompatibleType_emitsWarning() {
    Node callNode = new Node(Token.CALL, new Node(Token.NAME));
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectArgumentMatchesParameter(traversal, dummyNode, stringType, numberType, callNode, 1);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanCast with incompatible types
  @Test
  public void testExpectCanCast_incompatibleTypes_emitsInvalidCastWarning() {
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectCanCast(traversal, dummyNode, booleanType, numberType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectValidTypeofName with unknown typeof string
  @Test
  public void testExpectValidTypeofName_emitsUnknownTypeofWarning() {
    validator.expectValidTypeofName(traversal, dummyNode, "invalid_typeof");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests TypeMismatch equality, hashCode, and toString
  @Test
  public void testTypeMismatch_equalsAndHashCode() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    TypeValidator.TypeMismatch m1 = new TypeValidator.TypeMismatch(stringType, numberType, null);
    TypeValidator.TypeMismatch m2 = new TypeValidator.TypeMismatch(numberType, stringType, null);

    assertEquals(m1, m2);
    assertEquals(m1.hashCode(), m2.hashCode());
    assertNotNull(m1.toString());
    assertFalse(m1.equals("non-mismatch-object"));
  }
}
package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.Iterables;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TypeValidatorTest {

  private Compiler compiler;
  private TypeValidator validator;
  private JSTypeRegistry registry;
  private NodeTraversal traversal;
  private Node dummyNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    validator = new TypeValidator(compiler);
    registry = compiler.getTypeRegistry();
    traversal = new NodeTraversal(compiler, null);
    dummyNode = new Node(Token.NAME);
  }

  private JSType getNativeType(JSTypeNative type) {
    return registry.getNativeType(type);
  }

  // Tests reporting unknown typeof name
  @Test
  public void testExpectValidTypeofName_invalidName_reportsWarning() {
    validator.expectValidTypeofName(traversal, dummyNode, "int");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.UNKNOWN_TYPEOF_VALUE,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectObject with valid object context
  @Test
  public void testExpectObject_validObject_returnsTrueAndNoWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    boolean result = validator.expectObject(traversal, dummyNode, objType, "msg");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectObject with primitive type
  @Test
  public void testExpectObject_primitiveType_returnsFalseAndReportsWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectObject(traversal, dummyNode, numType, "msg");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectActualObject with primitive type
  @Test
  public void testExpectActualObject_primitiveType_reportsWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectActualObject(traversal, dummyNode, strType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectAnyObject with invalid type
  @Test
  public void testExpectAnyObject_numberType_reportsWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectAnyObject(traversal, dummyNode, numType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectString with number type
  @Test
  public void testExpectString_numberType_reportsWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectString(traversal, dummyNode, numType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNumber with valid and invalid types
  @Test
  public void testExpectNumber_stringType_reportsWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectNumber(traversal, dummyNode, strType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectBitwiseable with valid primitive type
  @Test
  public void testExpectBitwiseable_booleanType_noWarning() {
    JSType boolType = getNativeType(BOOLEAN_TYPE);
    validator.expectBitwiseable(traversal, dummyNode, boolType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectStringOrNumber with object type
  @Test
  public void testExpectStringOrNumber_objectType_reportsWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    validator.expectStringOrNumber(traversal, dummyNode, objType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with null type
  @Test
  public void testExpectNotNullOrUndefined_nullType_returnsFalseAndReportsWarning() {
    JSType nullType = getNativeType(NULL_TYPE);
    JSType expectedType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(
        traversal, dummyNode, nullType, "null found", expectedType);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectSwitchMatchesCase with incompatible types
  @Test
  public void testExpectSwitchMatchesCase_incompatibleTypes_reportsWarning() {
    Node switchNode = new Node(Token.SWITCH, dummyNode);
    JSType boolType = getNativeType(BOOLEAN_TYPE);
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectSwitchMatchesCase(traversal, switchNode, boolType, strType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectIndexMatch on array node
  @Test
  public void testExpectIndexMatch_arrayWithStringType_reportsWarning() {
    Node left = new Node(Token.NAME);
    Node right = new Node(Token.STRING);
    Node getElemNode = new Node(Token.GETELEM, left, right);

    JSType arrayType = getNativeType(ARRAY_TYPE);
    JSType strType = getNativeType(STRING_TYPE);

    validator.expectIndexMatch(traversal, getElemNode, arrayType, strType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with compatible types
  @Test
  public void testExpectCanAssignTo_compatibleTypes_returnsTrueAndNoWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignTo(
        traversal, dummyNode, numType, numType, "msg");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with incompatible constructor types registers mismatch without warning
  @Test
  public void testExpectCanAssignTo_constructorMismatch_registersMismatchWithoutWarning() {
    FunctionType ctorA = registry.createConstructorType("CtorA", null, null, null);
    FunctionType ctorB = registry.createConstructorType("CtorB", null, null, null);

    boolean result = validator.expectCanAssignTo(
        traversal, dummyNode, ctorA, ctorB, "msg");
    assertFalse(result);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectCanAssignToPropertyOf with incompatible types
  @Test
  public void testExpectCanAssignToPropertyOf_incompatibleTypes_reportsWarning() {
    Node ownerNode = new Node(Token.NAME);
    ownerNode.setJSType(getNativeType(OBJECT_TYPE));

    JSType numType = getNativeType(NUMBER_TYPE);
    JSType strType = getNativeType(STRING_TYPE);

    boolean result = validator.expectCanAssignToPropertyOf(
        traversal, dummyNode, numType, strType, ownerNode, "prop");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectArgumentMatchesParameter with incompatible argument type
  @Test
  public void testExpectArgumentMatchesParameter_mismatch_reportsWarning() {
    Node callTarget = new Node(Token.NAME);
    callTarget.setJSType(getNativeType(UNKNOWN_TYPE));
    Node callNode = new Node(Token.CALL, callTarget);

    JSType numType = getNativeType(NUMBER_TYPE);
    JSType strType = getNativeType(STRING_TYPE);

    validator.expectArgumentMatchesParameter(
        traversal, dummyNode, numType, strType, callNode, 1);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanCast with incompatible types
  @Test
  public void testExpectCanCast_incompatibleTypes_reportsWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    JSType boolType = getNativeType(BOOLEAN_TYPE);

    validator.expectCanCast(traversal, dummyNode, numType, boolType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectCanOverride with incompatible overriding type
  @Test
  public void testExpectCanOverride_incompatibleOverride_reportsWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    JSType strType = getNativeType(STRING_TYPE);
    JSType objType = getNativeType(OBJECT_TYPE);

    validator.expectCanOverride(
        traversal, dummyNode, numType, strType, "prop", objType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_PROPERTY_MISMATCH,
        compiler.getWarnings()[0].getType());
  }

  // Tests getReadableJSTypeName on qualified name
  @Test
  public void testGetReadableJSTypeName_namedNode_returnsQualifiedName() {
    Node nameNode = Node.newString(Token.NAME, "myVariable");
    nameNode.setJSType(getNativeType(NUMBER_TYPE));

    String name = validator.getReadableJSTypeName(nameNode, false);
    assertEquals("myVariable", name);
  }

  // Tests TypeMismatch equals and hashCode behavior
  @Test
  public void testTypeMismatch_equalsAndHashCode_symmetric() {
    JSType numType = getNativeType(NUMBER_TYPE);
    JSType strType = getNativeType(STRING_TYPE);

    TypeValidator.TypeMismatch mismatch1 =
        new TypeValidator.TypeMismatch(numType, strType, null);
    TypeValidator.TypeMismatch mismatch2 =
        new TypeValidator.TypeMismatch(strType, numType, null);

    assertEquals(mismatch1, mismatch2);
    assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    assertNotNull(mismatch1.toString());
  }

  @Test
  public void testExpectValidTypeofName_validName_noWarning() {
    validator.expectValidTypeofName(traversal, dummyNode, "number");
    validator.expectValidTypeofName(traversal, dummyNode, "string");
    validator.expectValidTypeofName(traversal, dummyNode, "boolean");
    validator.expectValidTypeofName(traversal, dummyNode, "undefined");
    validator.expectValidTypeofName(traversal, dummyNode, "function");
    validator.expectValidTypeofName(traversal, dummyNode, "object");
    validator.expectValidTypeofName(traversal, dummyNode, "unknown");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectBitwiseable_objectType_reportsWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    validator.expectBitwiseable(traversal, dummyNode, objType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined_voidType_reportsWarning() {
    JSType voidType = getNativeType(VOID_TYPE);
    JSType expectedType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(
        traversal, dummyNode, voidType, "void found", expectedType);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined_validType_returnsTrueAndNoWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(
        traversal, dummyNode, strType, "msg", strType);
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanCast_compatibleTypes_noWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    JSType arrayType = getNativeType(ARRAY_TYPE);
    validator.expectCanCast(traversal, dummyNode, arrayType, objType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignTo_incompatibleNonConstructor_reportsWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    JSType numType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignTo(
        traversal, dummyNode, strType, numType, "msg");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING,
        compiler.getWarnings()[0].getType());
  }

  @Test
  public void testExpectCanOverride_compatibleOverride_noWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    JSType objType = getNativeType(OBJECT_TYPE);
    validator.expectCanOverride(
        traversal, dummyNode, numType, numType, "prop", objType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignToPropertyOf_compatibleTypes_noWarning() {
    Node ownerNode = new Node(Token.NAME);
    ownerNode.setJSType(getNativeType(OBJECT_TYPE));

    JSType numType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignToPropertyOf(
        traversal, dummyNode, numType, numType, ownerNode, "prop");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testGetReadableJSTypeName_nonQualifiedNameNode_returnsTypeName() {
    Node addNode = new Node(Token.ADD);
    addNode.setJSType(getNativeType(NUMBER_TYPE));

    String name = validator.getReadableJSTypeName(addNode, false);
    assertEquals("number", name);
  }

  @Test
  public void testExpectActualObject_validObject_noWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    validator.expectActualObject(traversal, dummyNode, objType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject_validObject_noWarning() {
    JSType objType = getNativeType(OBJECT_TYPE);
    validator.expectAnyObject(traversal, dummyNode, objType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectString_validString_noWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectString(traversal, dummyNode, strType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectNumber_validNumber_noWarning() {
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectNumber(traversal, dummyNode, numType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectStringOrNumber_validTypes_noWarning() {
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectStringOrNumber(traversal, dummyNode, strType, "msg");
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectStringOrNumber(traversal, dummyNode, numType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectSwitchMatchesCase_matchingTypes_noWarning() {
    Node switchNode = new Node(Token.SWITCH, dummyNode);
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectSwitchMatchesCase(traversal, switchNode, strType, strType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch_arrayWithNumberType_noWarning() {
    Node left = new Node(Token.NAME);
    Node right = new Node(Token.NUMBER);
    Node getElemNode = new Node(Token.GETELEM, left, right);

    JSType arrayType = getNativeType(ARRAY_TYPE);
    JSType numType = getNativeType(NUMBER_TYPE);

    validator.expectIndexMatch(traversal, getElemNode, arrayType, numType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectArgumentMatchesParameter_matching_noWarning() {
    Node callTarget = new Node(Token.NAME);
    callTarget.setJSType(getNativeType(UNKNOWN_TYPE));
    Node callNode = new Node(Token.CALL, callTarget);

    JSType numType = getNativeType(NUMBER_TYPE);

    validator.expectArgumentMatchesParameter(
        traversal, dummyNode, numType, numType, callNode, 1);
    assertEquals(0, compiler.getWarningCount());
  }
}
package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class TypeValidatorTest {

  private Compiler compiler;
  private TypeValidator validator;
  private JSTypeRegistry registry;
  private NodeTraversal traversal;
  private Node node;

  @Before
  public void setUp() {
    compiler = new Compiler();
    validator = new TypeValidator(compiler);
    registry = compiler.getTypeRegistry();
    traversal = new NodeTraversal(compiler, null);
    node = Node.newString(Token.NAME, "testNode");
  }

  // Tests expectObject with valid object type returning true
  @Test
  public void testExpectObject_withObjectType_returnsTrue() {
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    boolean result = validator.expectObject(traversal, node, objectType, "object expected");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectObject with non-object type returning false and recording warning
  @Test
  public void testExpectObject_withNumberType_returnsFalseAndReportsWarning() {
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    boolean result = validator.expectObject(traversal, node, numberType, "object expected");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
    Iterator<TypeValidator.TypeMismatch> it = validator.getMismatches().iterator();
    assertTrue(it.hasNext());
  }

  // Tests expectActualObject with primitive type triggering mismatch
  @Test
  public void testExpectActualObject_withPrimitiveType_recordsMismatch() {
    JSType stringType = registry.getNativeType(STRING_TYPE);
    validator.expectActualObject(traversal, node, stringType, "actual object expected");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectString with string context matching
  @Test
  public void testExpectString_withStringType_noWarning() {
    JSType stringType = registry.getNativeType(STRING_TYPE);
    validator.expectString(traversal, node, stringType, "string expected");
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectNumber with number and non-number types
  @Test
  public void testExpectNumber_withInvalidType_recordsWarning() {
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);
    validator.expectNumber(traversal, node, boolType, "number expected");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectBitwiseable with valid primitive and value types
  @Test
  public void testExpectBitwiseable_withValidNumber_noWarning() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectBitwiseable(traversal, node, numType, "bitwiseable expected");
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectStringOrNumber with valid and invalid types
  @Test
  public void testExpectStringOrNumber_withBoolean_recordsWarning() {
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);
    validator.expectStringOrNumber(traversal, node, boolType, "str or num expected");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with null type returning false
  @Test
  public void testExpectNotNullOrUndefined_withNullType_returnsFalse() {
    JSType nullType = registry.getNativeType(NULL_TYPE);
    JSType expectedType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(traversal, node, nullType, "not null", expectedType);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with valid object type returning true
  @Test
  public void testExpectNotNullOrUndefined_withValidType_returnsTrue() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(traversal, node, strType, "not null", strType);
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with compatible types
  @Test
  public void testExpectCanAssignTo_compatibleTypes_returnsTrue() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignTo(traversal, node, numType, numType, "assign");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with incompatible types
  @Test
  public void testExpectCanAssignTo_incompatibleTypes_returnsFalseAndRecordsMismatch() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType strType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectCanAssignTo(traversal, node, numType, strType, "incompatible assign");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanCast with incompatible types
  @Test
  public void testExpectCanCast_incompatibleCast_recordsWarning() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);
    validator.expectCanCast(traversal, node, numType, boolType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests setShouldReport suppression of compiler warnings
  @Test
  public void testSetShouldReport_whenFalse_doesNotReportToCompiler() {
    validator.setShouldReport(false);
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType strType = registry.getNativeType(STRING_TYPE);
    validator.expectCanAssignTo(traversal, node, numType, strType, "msg");
    assertEquals(0, compiler.getWarningCount());
    assertTrue(validator.getMismatches().iterator().hasNext());
  }

  // Tests getReadableJSTypeName with typed node
  @Test
  public void testGetReadableJSTypeName_withAssignedType_returnsTypeName() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    node.setJSType(numType);
    String typeName = validator.getReadableJSTypeName(node, false);
    assertEquals("number", typeName);
  }

  // Tests TypeMismatch equals and hashCode symmetry
  @Test
  public void testTypeMismatch_equalsAndHashCode_areSymmetric() {
    JSType typeA = registry.getNativeType(NUMBER_TYPE);
    JSType typeB = registry.getNativeType(STRING_TYPE);
    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(typeA, typeB);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(typeB, typeA);

    assertEquals(mismatch1, mismatch2);
    assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    assertNotNull(mismatch1.toString());
    assertFalse(mismatch1.equals("nonMismatchObject"));
  }

  @Test
  public void testExpectActualObject_withValidObjectType_noWarning() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    validator.expectActualObject(traversal, node, objType, "actual object expected");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectString_withNonStringType_recordsWarning() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectString(traversal, node, numType, "string expected");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNumber_withValidNumber_noWarning() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectNumber(traversal, node, numType, "number expected");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectBitwiseable_withInvalidType_recordsWarning() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    validator.expectBitwiseable(traversal, node, objType, "bitwiseable expected");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectStringOrNumber_withValidTypes_noWarning() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    validator.expectStringOrNumber(traversal, node, strType, "str or num expected");
    assertEquals(0, compiler.getWarningCount());

    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectStringOrNumber(traversal, node, numType, "str or num expected");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined_withVoidType_returnsFalse() {
    JSType voidType = registry.getNativeType(VOID_TYPE);
    JSType expectedType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(traversal, node, voidType, "not undefined", expectedType);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanCast_compatibleCast_noWarning() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    validator.expectCanCast(traversal, node, objType, objType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectArgumentMatchesParameter_matching_noWarning() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    JSType strType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectArgumentMatchesParameter(traversal, node, strType, strType, callNode, 1);
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectArgumentMatchesParameter_mismatch_recordsWarning() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    JSType strType = registry.getNativeType(STRING_TYPE);
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    boolean result = validator.expectArgumentMatchesParameter(traversal, node, numType, strType, callNode, 1);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignToProperty_matching_noWarning() {
    Node owner = Node.newString(Token.NAME, "ownerObj");
    JSType strType = registry.getNativeType(STRING_TYPE);
    boolean result = validator.expectCanAssignToProperty(traversal, node, strType, strType, owner, "myProp");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignToProperty_mismatch_recordsWarning() {
    Node owner = Node.newString(Token.NAME, "ownerObj");
    JSType strType = registry.getNativeType(STRING_TYPE);
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignToProperty(traversal, node, numType, strType, owner, "myProp");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch_matching_noWarning() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    JSType strType = registry.getNativeType(STRING_TYPE);
    validator.expectIndexMatch(traversal, node, objType, strType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch_mismatch_recordsWarning() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);
    validator.expectIndexMatch(traversal, node, objType, boolType);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectSwitchMatchesCase_matching_noWarning() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    validator.expectSwitchMatchesCase(traversal, node, strType, strType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectSwitchMatchesCase_mismatch_recordsWarning() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectSwitchMatchesCase(traversal, node, strType, numType);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectValidTypeofName_validAndInvalidNames() {
    validator.expectValidTypeofName(traversal, node, "number");
    assertEquals(0, compiler.getWarningCount());

    validator.expectValidTypeofName(traversal, node, "invalid_type_name");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject_withObjectType_returnsTrue() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    boolean result = validator.expectAnyObject(traversal, node, objType, "any object expected");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject_withPrimitiveType_returnsFalse() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    boolean result = validator.expectAnyObject(traversal, node, numType, "any object expected");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectSuperType_matching_noWarning() {
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    validator.expectSuperType(traversal, node, objType, objType);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectSuperType_mismatch_recordsWarning() {
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    validator.expectSuperType(traversal, node, objType, numType);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testGetReadableJSTypeName_withGetpropNode() {
    Node target = Node.newString(Token.NAME, "a");
    Node getprop = new Node(Token.GETPROP, target, Node.newString("b"));
    getprop.setJSType(registry.getNativeType(STRING_TYPE));
    String typeName = validator.getReadableJSTypeName(getprop, true);
    assertEquals("string", typeName);
  }

  @Test
  public void testGetReadableJSTypeName_withoutNodeJSType_returnsUnknown() {
    Node unTypedNode = Node.newString(Token.NAME, "x");
    String typeName = validator.getReadableJSTypeName(unTypedNode, false);
    assertEquals("unknown", typeName);
  }
}
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

  private JSType getNativeType(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }

  // Tests expectObject with valid object context and non-object context
  @Test
  public void testExpectObject_validAndInvalidContext_returnsExpectedResult() {
    JSType objectType = getNativeType(OBJECT_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);

    assertTrue(validator.expectObject(traversal, dummyNode, objectType, "expected object"));
    assertEquals(0, Iterables.size(validator.getMismatches()));

    assertFalse(validator.expectObject(traversal, dummyNode, numberType, "expected object"));
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectActualObject with Object and primitive types
  @Test
  public void testExpectActualObject_objectAndPrimitive_recordsMismatchWhenNotObject() {
    JSType objectType = getNativeType(OBJECT_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);

    validator.expectActualObject(traversal, dummyNode, objectType, "expected actual object");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectActualObject(traversal, dummyNode, stringType, "expected actual object");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectAnyObject matching condition
  @Test
  public void testExpectAnyObject_matchingAndNonMatching_recordsMismatch() {
    JSType objectType = getNativeType(OBJECT_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);

    validator.expectAnyObject(traversal, dummyNode, objectType, "expected any object");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectAnyObject(traversal, dummyNode, numberType, "expected any object");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectString with string and non-string types
  @Test
  public void testExpectString_stringAndNumber_recordsMismatchForNumber() {
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);

    validator.expectString(traversal, dummyNode, stringType, "expected string");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectString(traversal, dummyNode, numberType, "expected string");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectNumber with number and non-number types
  @Test
  public void testExpectNumber_numberAndBoolean_recordsMismatchForBoolean() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType booleanType = getNativeType(BOOLEAN_TYPE);

    validator.expectNumber(traversal, dummyNode, numberType, "expected number");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectNumber(traversal, dummyNode, booleanType, "expected number");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectBitwiseable with valid primitive and invalid object type
  @Test
  public void testExpectBitwiseable_primitiveAndObject_recordsMismatchForObject() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType objectType = getNativeType(OBJECT_TYPE);

    validator.expectBitwiseable(traversal, dummyNode, numberType, "expected bitwiseable");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectBitwiseable(traversal, dummyNode, objectType, "expected bitwiseable");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectStringOrNumber with valid and invalid types
  @Test
  public void testExpectStringOrNumber_validPrimitivesAndObject_handlesBranches() {
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType objectType = getNativeType(OBJECT_TYPE);

    validator.expectStringOrNumber(traversal, dummyNode, stringType, "msg");
    validator.expectStringOrNumber(traversal, dummyNode, numberType, "msg");
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectStringOrNumber(traversal, dummyNode, objectType, "msg");
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectNotNullOrUndefined with null, void, and string
  @Test
  public void testExpectNotNullOrUndefined_nullAndNonNull_returnsExpectedResult() {
    JSType nullType = getNativeType(NULL_TYPE);
    JSType voidType = getNativeType(VOID_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);

    assertTrue(validator.expectNotNullOrUndefined(
        traversal, dummyNode, stringType, "not null", stringType));
    assertEquals(0, Iterables.size(validator.getMismatches()));

    assertFalse(validator.expectNotNullOrUndefined(
        traversal, dummyNode, nullType, "not null", stringType));
    assertEquals(1, Iterables.size(validator.getMismatches()));

    assertFalse(validator.expectNotNullOrUndefined(
        traversal, dummyNode, voidType, "not undefined", stringType));
    assertEquals(2, Iterables.size(validator.getMismatches()));
  }

  // Tests expectSwitchMatchesCase with compatible and incompatible types
  @Test
  public void testExpectSwitchMatchesCase_matchingAndMismatchingTypes_recordsMismatch() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    Node switchNode = new Node(Token.SWITCH, new Node(Token.NAME));

    validator.expectSwitchMatchesCase(traversal, switchNode, numberType, numberType);
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectSwitchMatchesCase(traversal, switchNode, numberType, stringType);
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectIndexMatch across various object and index combinations
  @Test
  public void testExpectIndexMatch_arrayAndObject_validatesIndexTypes() {
    JSType arrayType = getNativeType(ARRAY_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    JSType unknownType = getNativeType(UNKNOWN_TYPE);
    JSType boolType = getNativeType(BOOLEAN_TYPE);

    validator.expectIndexMatch(traversal, dummyNode, unknownType, stringType);
    validator.expectIndexMatch(traversal, dummyNode, arrayType, numberType);
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectIndexMatch(traversal, dummyNode, boolType, numberType);
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectCanAssignTo for compatible and incompatible types
  @Test
  public void testExpectCanAssignTo_assignmentCompatibility_reportsProperly() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);

    assertTrue(validator.expectCanAssignTo(traversal, dummyNode, numberType, numberType, "msg"));
    assertEquals(0, Iterables.size(validator.getMismatches()));

    assertFalse(validator.expectCanAssignTo(traversal, dummyNode, stringType, numberType, "msg"));
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectCanAssignToPropertyOf
  @Test
  public void testExpectCanAssignToPropertyOf_propertyAssignment_verifiesTypes() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    Node ownerNode = Node.newString(Token.NAME, "myObj");

    assertTrue(validator.expectCanAssignToPropertyOf(
        traversal, dummyNode, numberType, numberType, ownerNode, "prop"));
    assertEquals(0, Iterables.size(validator.getMismatches()));

    assertFalse(validator.expectCanAssignToPropertyOf(
        traversal, dummyNode, stringType, numberType, ownerNode, "prop"));
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectArgumentMatchesParameter with valid and invalid argument types
  @Test
  public void testExpectArgumentMatchesParameter_argumentCompatibility_recordsMismatch() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));

    validator.expectArgumentMatchesParameter(
        traversal, dummyNode, numberType, numberType, callNode, 1);
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectArgumentMatchesParameter(
        traversal, dummyNode, stringType, numberType, callNode, 1);
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests expectCanOverride property mismatch
  @Test
  public void testExpectCanOverride_incompatibleOverride_recordsMismatchAndWarning() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    JSType objectType = getNativeType(OBJECT_TYPE);

    validator.expectCanOverride(traversal, dummyNode, numberType, numberType, "foo", objectType);
    assertEquals(0, Iterables.size(validator.getMismatches()));

    validator.expectCanOverride(traversal, dummyNode, stringType, numberType, "foo", objectType);
    assertEquals(1, Iterables.size(validator.getMismatches()));
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanCast with valid and invalid casts
  @Test
  public void testExpectCanCast_validAndInvalidCasts_reportsWarning() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);

    validator.expectCanCast(traversal, dummyNode, numberType, numberType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectCanCast(traversal, dummyNode, numberType, stringType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests getReadableJSTypeName with various Node types
  @Test
  public void testGetReadableJSTypeName_variousNodes_returnsReadableName() {
    Node nameNode = Node.newString(Token.NAME, "myVar");
    nameNode.setJSType(getNativeType(NUMBER_TYPE));
    assertEquals("number", validator.getReadableJSTypeName(nameNode, false));

    Node getPropNode = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.STRING, "b"));
    assertEquals("a.b", validator.getReadableJSTypeName(getPropNode, false));
  }

  // Tests setShouldReport toggling
  @Test
  public void testSetShouldReport_suppressesCompilerWarningsWhenFalse() {
    JSType numberType = getNativeType(NUMBER_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);

    validator.setShouldReport(false);
    validator.expectCanCast(traversal, dummyNode, numberType, stringType);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, Iterables.size(validator.getMismatches()));
  }

  // Tests TypeMismatch equals, hashCode, and toString
  @Test
  public void testTypeMismatch_symmetryEqualityAndToString() {
    JSType typeA = getNativeType(NUMBER_TYPE);
    JSType typeB = getNativeType(STRING_TYPE);
    JSType typeC = getNativeType(BOOLEAN_TYPE);

    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(typeA, typeB);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(typeB, typeA);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(typeA, typeC);

    assertEquals(mismatch1, mismatch2);
    assertEquals(mismatch2, mismatch1);
    assertNotEquals(mismatch1, mismatch3);
    assertNotEquals(mismatch1, null);
    assertNotEquals(mismatch1, "someString");

    assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    assertNotNull(mismatch1.toString());
    assertTrue(mismatch1.toString().contains("number"));
    assertTrue(mismatch1.toString().contains("string"));
  }
}
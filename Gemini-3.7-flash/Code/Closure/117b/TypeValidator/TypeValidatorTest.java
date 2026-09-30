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

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
  }

  private JSType getNativeType(JSTypeNative type) {
    return registry.getNativeType(type);
  }

  private NodeTraversal createTraversal() {
    return new NodeTraversal(compiler, null);
  }

  // Tests expectValidTypeofName reports unknown typeof value warning
  @Test
  public void testExpectValidTypeofName_invalidName_reportsWarning() {
    Node n = Node.newString("invalid_type");
    NodeTraversal t = createTraversal();
    validator.expectValidTypeofName(t, n, "invalid_type");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.UNKNOWN_TYPEOF_VALUE,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectObject with valid object type returns true without warnings
  @Test
  public void testExpectObject_validObject_returnsTrue() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType objType = getNativeType(OBJECT_TYPE);
    boolean result = validator.expectObject(t, n, objType, "msg");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectObject with non-object type returns false and reports warning
  @Test
  public void testExpectObject_numberType_returnsFalseAndReportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType numType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectObject(t, n, numType, "msg");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectActualObject with primitive type reports mismatch
  @Test
  public void testExpectActualObject_stringType_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectActualObject(t, n, strType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectAnyObject with non-matching primitive reports warning
  @Test
  public void testExpectAnyObject_numberType_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectAnyObject(t, n, numType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectString with number type reports warning
  @Test
  public void testExpectString_numberType_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType numType = getNativeType(NUMBER_TYPE);
    validator.expectString(t, n, numType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNumber with string type reports warning
  @Test
  public void testExpectNumber_stringType_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType strType = getNativeType(STRING_TYPE);
    validator.expectNumber(t, n, strType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectBitwiseable with valid primitive types
  @Test
  public void testExpectBitwiseable_primitiveType_noWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType boolType = getNativeType(BOOLEAN_TYPE);
    validator.expectBitwiseable(t, n, boolType, "msg");
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with null type reports warning
  @Test
  public void testExpectNotNullOrUndefined_nullType_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType nullType = getNativeType(NULL_TYPE);
    boolean result = validator.expectNotNullOrUndefined(
        t, n, nullType, "null found", getNativeType(OBJECT_TYPE));
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with non-null type returns true
  @Test
  public void testExpectNotNullOrUndefined_stringType_returnsTrue() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    JSType strType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(
        t, n, strType, "msg", getNativeType(STRING_TYPE));
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectSwitchMatchesCase when types do not match
  @Test
  public void testExpectSwitchMatchesCase_mismatchedTypes_reportsWarning() {
    Node switchNode = new Node(Token.SWITCH, Node.newString("s"));
    Node caseNode = new Node(Token.CASE, Node.newNumber(1));
    switchNode.addChildToBack(caseNode);
    NodeTraversal t = createTraversal();
    validator.expectSwitchMatchesCase(
        t, caseNode, getNativeType(STRING_TYPE), getNativeType(NUMBER_TYPE));
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectIndexMatch on struct object reports illegal property access
  @Test
  public void testExpectIndexMatch_structObject_reportsWarning() {
    Node target = Node.newString("x");
    Node index = Node.newString("prop");
    Node getElem = new Node(Token.GETELEM, target, index);

    ObjectType structType = registry.createObjectType("StructObj", null);
    structType.setStruct();
    target.setJSType(structType);

    NodeTraversal t = createTraversal();
    validator.expectIndexMatch(t, getElem, structType, getNativeType(STRING_TYPE));
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.ILLEGAL_PROPERTY_ACCESS,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectCanAssignTo with compatible types returns true
  @Test
  public void testExpectCanAssignTo_compatibleTypes_returnsTrue() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    boolean result = validator.expectCanAssignTo(
        t, n, getNativeType(NUMBER_TYPE), getNativeType(NUMBER_TYPE), "msg");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with incompatible types returns false and registers mismatch
  @Test
  public void testExpectCanAssignTo_incompatibleTypes_returnsFalseAndRegistersMismatch() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    boolean result = validator.expectCanAssignTo(
        t, n, getNativeType(STRING_TYPE), getNativeType(NUMBER_TYPE), "msg");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());

    Iterator<TypeValidator.TypeMismatch> it = validator.getMismatches().iterator();
    assertTrue(it.hasNext());
    TypeValidator.TypeMismatch mismatch = it.next();
    assertEquals(getNativeType(STRING_TYPE), mismatch.typeA);
    assertEquals(getNativeType(NUMBER_TYPE), mismatch.typeB);
  }

  // Tests expectArgumentMatchesParameter with incompatible argument type
  @Test
  public void testExpectArgumentMatchesParameter_incompatibleArg_reportsWarning() {
    Node callTarget = Node.newString("foo");
    callTarget.setJSType(getNativeType(UNKNOWN_TYPE));
    Node callNode = new Node(Token.CALL, callTarget);
    Node argNode = Node.newString("bar");
    NodeTraversal t = createTraversal();

    validator.expectArgumentMatchesParameter(
        t, argNode, getNativeType(STRING_TYPE), getNativeType(NUMBER_TYPE), callNode, 1);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectCanCast with incompatible cast types reports warning
  @Test
  public void testExpectCanCast_invalidCast_reportsWarning() {
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    validator.expectCanCast(
        t, n, getNativeType(NUMBER_TYPE), getNativeType(BOOLEAN_TYPE));
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST,
        compiler.getWarnings()[0].getType());
  }

  // Tests expectSuperType when missing extends tag
  @Test
  public void testExpectSuperType_missingExtendsTag_reportsWarning() {
    FunctionType superCtor = registry.createConstructorType(
        "SuperClass", null, null, null);
    FunctionType subCtor = registry.createConstructorType(
        "SubClass", null, null, null);

    Node n = Node.newString("SubClass");
    NodeTraversal t = createTraversal();

    validator.expectSuperType(
        t, n, superCtor.getInstanceType(), subCtor.getInstanceType());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.MISSING_EXTENDS_TAG_WARNING,
        compiler.getWarnings()[0].getType());
  }

  // Tests getReadableJSTypeName on GETPROP with object and prototype chain
  @Test
  public void testGetReadableJSTypeName_getPropOnObjectType_returnsFormattedName() {
    ObjectType protoType = registry.createObjectType("MyClass.prototype", null);
    protoType.defineDeclaredProperty("foo", getNativeType(NUMBER_TYPE), null);

    FunctionType ctor = registry.createConstructorType("MyClass", null, null, null);
    ctor.getPrototype().defineDeclaredProperty("foo", getNativeType(NUMBER_TYPE), null);
    ObjectType instanceType = ctor.getInstanceType();

    Node target = Node.newString("instance");
    target.setJSType(instanceType);
    Node prop = Node.newString("foo");
    Node getProp = new Node(Token.GETPROP, target, prop);

    String readableName = validator.getReadableJSTypeName(getProp, true);
    assertEquals("MyClass.prototype.foo", readableName);
  }

  // Tests TypeMismatch equals and hashCode methods
  @Test
  public void testTypeMismatch_equalsAndHashCode() {
    JSType typeA = getNativeType(STRING_TYPE);
    JSType typeB = getNativeType(NUMBER_TYPE);
    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(typeA, typeB, null);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(typeB, typeA, null);

    assertTrue(mismatch1.equals(mismatch2));
    assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    assertNotNull(mismatch1.toString());
    assertFalse(mismatch1.equals("other"));
  }

  // Tests setShouldReport disables warning reporting to compiler
  @Test
  public void testSetShouldReport_disabled_doesNotReportToCompiler() {
    validator.setShouldReport(false);
    Node n = Node.newString("x");
    NodeTraversal t = createTraversal();
    validator.expectString(t, n, getNativeType(NUMBER_TYPE), "msg");
    assertEquals(0, compiler.getWarningCount());
  }
}
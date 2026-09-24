package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
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

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, null);
  }

  private JSType getNativeType(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }

  // Tests expectObject with valid object type
  @Test
  public void testExpectObject_withObjectType_returnsTrue() {
    Node node = IR.empty();
    JSType objectType = getNativeType(OBJECT_TYPE);
    boolean result = validator.expectObject(traversal, node, objectType, "expected object");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectObject with non-object type
  @Test
  public void testExpectObject_withNumberType_returnsFalseAndReportsWarning() {
    Node node = IR.empty();
    JSType numberType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectObject(traversal, node, numberType, "expected object");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectActualObject with primitive type
  @Test
  public void testExpectActualObject_withStringType_reportsWarning() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    validator.expectActualObject(traversal, node, stringType, "expected actual object");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectString with number type
  @Test
  public void testExpectString_withNumberType_reportsWarning() {
    Node node = IR.empty();
    JSType numberType = getNativeType(NUMBER_TYPE);
    validator.expectString(traversal, node, numberType, "expected string");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNumber with string type
  @Test
  public void testExpectNumber_withStringType_reportsWarning() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    validator.expectNumber(traversal, node, stringType, "expected number");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectBitwiseable with object type
  @Test
  public void testExpectBitwiseable_withObjectType_reportsWarning() {
    Node node = IR.empty();
    JSType objectType = getNativeType(OBJECT_TYPE);
    validator.expectBitwiseable(traversal, node, objectType, "expected bitwiseable");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with null type in global scope
  @Test
  public void testExpectNotNullOrUndefined_withNullType_returnsFalseAndReportsWarning() {
    Node node = IR.empty();
    JSType nullType = getNativeType(NULL_TYPE);
    JSType stringType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(traversal, node, nullType, "null found", stringType);
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectNotNullOrUndefined with valid type
  @Test
  public void testExpectNotNullOrUndefined_withStringType_returnsTrue() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    boolean result = validator.expectNotNullOrUndefined(traversal, node, stringType, "valid", stringType);
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with compatible types
  @Test
  public void testExpectCanAssignTo_compatibleTypes_returnsTrue() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    boolean result = validator.expectCanAssignTo(traversal, node, stringType, stringType, "assignment");
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests expectCanAssignTo with incompatible types
  @Test
  public void testExpectCanAssignTo_incompatibleTypes_returnsFalseAndRegistersMismatch() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    boolean result = validator.expectCanAssignTo(traversal, node, stringType, numberType, "assignment");
    assertFalse(result);
    assertEquals(1, compiler.getWarningCount());

    Iterator<TypeValidator.TypeMismatch> mismatches = validator.getMismatches().iterator();
    assertTrue(mismatches.hasNext());
    TypeValidator.TypeMismatch mismatch = mismatches.next();
    assertEquals(stringType, mismatch.typeA);
    assertEquals(numberType, mismatch.typeB);
  }

  // Tests expectCanCast with invalid cast
  @Test
  public void testExpectCanCast_incompatibleTypes_reportsWarning() {
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    validator.expectCanCast(traversal, node, numberType, stringType);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests expectValidTypeofName
  @Test
  public void testExpectValidTypeofName_reportsWarning() {
    Node node = IR.empty();
    validator.expectValidTypeofName(traversal, node, "invalid_type");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests getReadableJSTypeName on simple identifier node
  @Test
  public void testGetReadableJSTypeName_nameNode_returnsQualifiedName() {
    Node node = IR.name("myVar");
    node.setJSType(getNativeType(NUMBER_TYPE));
    String name = validator.getReadableJSTypeName(node, false);
    assertEquals("myVar", name);
  }

  // Tests getReadableJSTypeName on GETPROP node with prototype access
  @Test
  public void testGetReadableJSTypeName_getPropNode_returnsQualifiedPropertyName() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null, null);
    ObjectType instanceType = ctor.getInstanceType();
    Node target = IR.name("fooInstance");
    target.setJSType(instanceType);
    Node propNode = IR.getprop(target, IR.string("bar"));
    propNode.setJSType(getNativeType(STRING_TYPE));

    String readableName = validator.getReadableJSTypeName(propNode, true);
    assertNotNull(readableName);
  }

  // Tests setShouldReport false suppresses compiler warnings
  @Test
  public void testSetShouldReport_false_suppressesReporting() {
    validator.setShouldReport(false);
    Node node = IR.empty();
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    validator.expectCanAssignTo(traversal, node, stringType, numberType, "assignment");
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests TypeMismatch equals and hashCode
  @Test
  public void testTypeMismatch_equalsAndHashCode() {
    JSType stringType = getNativeType(STRING_TYPE);
    JSType numberType = getNativeType(NUMBER_TYPE);
    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(stringType, numberType, null);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(numberType, stringType, null);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(stringType, stringType, null);

    assertEquals(mismatch1, mismatch2);
    assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    assertFalse(mismatch1.equals(mismatch3));
    assertFalse(mismatch1.equals("non-mismatch-object"));
    assertNotNull(mismatch1.toString());
  }
}
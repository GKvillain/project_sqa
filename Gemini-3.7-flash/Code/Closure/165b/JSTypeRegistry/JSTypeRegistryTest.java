package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry.ResolveMode;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class JSTypeRegistryTest {

  private JSTypeRegistry registry;
  private ErrorReporter dummyReporter;

  @Before
  public void setUp() {
    dummyReporter = new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, int lineOffset) {}

      @Override
      public void error(String message, String sourceName, int line, int lineOffset) {}
    };
    registry = new JSTypeRegistry(dummyReporter);
  }

  // Tests initialization of native types and basic retrieval
  @Test
  public void testGetNativeType_validNativeTypes_returnsNonNullTypes() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    FunctionType functionType = registry.getNativeFunctionType(JSTypeNative.FUNCTION_FUNCTION_TYPE);

    assertNotNull(numberType);
    assertNotNull(stringType);
    assertNotNull(booleanType);
    assertNotNull(objectType);
    assertNotNull(functionType);
    assertTrue(numberType.isNumberType());
    assertTrue(stringType.isStringType());
    assertTrue(booleanType.isBooleanValueType());
  }

  // Tests declaring a new type and verifying namespace creation
  @Test
  public void testDeclareType_newType_returnsTrueAndRecordsNamespace() {
    ObjectType customType = registry.createAnonymousObjectType();
    boolean declared = registry.declareType("my.custom.Namespace.Type", customType);

    assertTrue(declared);
    assertEquals(customType, registry.getType("my.custom.Namespace.Type"));
    assertTrue(registry.hasNamespace("my"));
    assertTrue(registry.hasNamespace("my.custom"));
    assertTrue(registry.hasNamespace("my.custom.Namespace"));
    assertFalse(registry.hasNamespace("my.unknown.Namespace"));
  }

  // Tests declaring a duplicate type name returns false
  @Test
  public void testDeclareType_duplicateTypeName_returnsFalse() {
    ObjectType type1 = registry.createAnonymousObjectType();
    ObjectType type2 = registry.createAnonymousObjectType();

    assertTrue(registry.declareType("UniqueName", type1));
    assertFalse(registry.declareType("UniqueName", type2));
    assertEquals(type1, registry.getType("UniqueName"));
  }

  // Tests overwriting an already declared type
  @Test
  public void testOverwriteDeclaredType_existingType_overwritesSuccessfully() {
    ObjectType type1 = registry.createAnonymousObjectType();
    ObjectType type2 = registry.createAnonymousObjectType();

    registry.declareType("TypeToOverwrite", type1);
    registry.overwriteDeclaredType("TypeToOverwrite", type2);

    assertEquals(type2, registry.getType("TypeToOverwrite"));
  }

  // Tests overwriting a non-existent type throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testOverwriteDeclaredType_nonExistingType_throwsException() {
    ObjectType type = registry.createAnonymousObjectType();
    registry.overwriteDeclaredType("NonExistentType", type);
  }

  // Tests forward declaring types
  @Test
  public void testForwardDeclareType_checksStatusCorrectly() {
    assertFalse(registry.isForwardDeclaredType("ForwardType"));
    registry.forwardDeclareType("ForwardType");
    assertTrue(registry.isForwardDeclaredType("ForwardType"));
  }

  // Tests setting and clearing template type
  @Test
  public void testTemplateType_setAndClear_returnsExpectedType() {
    registry.setTemplateTypeName("T");
    JSType tType = registry.getType("T");
    assertNotNull(tType);
    assertTrue(tType.isTemplateType());

    registry.clearTemplateTypeName();
    assertNull(registry.getType("T"));
  }

  // Tests union type creation with JSType instances and JSTypeNative enums
  @Test
  public void testCreateUnionType_variants_createsUnionCorrectly() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());

    JSType nativeUnion = registry.createUnionType(JSTypeNative.NUMBER_TYPE, JSTypeNative.STRING_TYPE);
    assertTrue(nativeUnion.isUnionType());
    assertEquals(union, nativeUnion);
  }

  // Tests optional and nullable type creation
  @Test
  public void testCreateOptionalAndNullableTypes_wrapsCorrectly() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    JSType optionalNumber = registry.createOptionalType(numberType);
    assertTrue(optionalNumber.isUnionType());
    assertTrue(optionalNumber.isSubtype(registry.getNativeType(JSTypeNative.VOID_TYPE)));

    JSType nullableNumber = registry.createNullableType(numberType);
    assertTrue(nullableNumber.isUnionType());
    assertTrue(nullableNumber.isSubtype(registry.getNativeType(JSTypeNative.NULL_TYPE)));

    JSType optionalNullableNumber = registry.createOptionalNullableType(numberType);
    assertTrue(optionalNullableNumber.isUnionType());
    assertTrue(optionalNullableNumber.isSubtype(registry.getNativeType(JSTypeNative.VOID_TYPE)));
    assertTrue(optionalNullableNumber.isSubtype(registry.getNativeType(JSTypeNative.NULL_TYPE)));

    JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    assertEquals(unknownType, registry.createOptionalType(unknownType));
  }

  // Tests registering and unregistering properties on types
  @Test
  public void testRegisterAndUnregisterPropertyOnType_tracksPropertiesProperly() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);

    assertFalse(registry.canPropertyBeDefined(objType, "customProp"));

    registry.registerPropertyOnType("customProp", objType);
    assertTrue(registry.canPropertyBeDefined(objType, "customProp"));

    JSType greatestSubtype = registry.getGreatestSubtypeWithProperty(objType, "customProp");
    assertFalse(greatestSubtype.isEmptyType());

    registry.unregisterPropertyOnType("customProp", objType);
    assertNotNull(registry.getTypesWithProperty("customProp"));
  }

  // Tests finding common super object between two object types
  @Test
  public void testFindCommonSuperObject_differentTypes_findsObjectRoot() {
    ObjectType dateType = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    ObjectType common = registry.findCommonSuperObject(dateType, arrayType);

    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), common);
  }

  // Tests creating function and constructor types
  @Test
  public void testCreateFunctionType_validParameters_createsFunction() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    FunctionType fnType = registry.createFunctionType(strType, numType);
    assertNotNull(fnType);
    assertEquals(strType, fnType.getReturnType());

    FunctionType ctorType = registry.createConstructorType(strType, numType);
    assertNotNull(ctorType);
    assertTrue(ctorType.isConstructor());

    FunctionType interfaceType = registry.createInterfaceType("MyInterface", null);
    assertNotNull(interfaceType);
    assertTrue(interfaceType.isInterface());
  }

  // Tests resetting implicit prototype on a prototype object type
  @Test
  public void testResetImplicitPrototype_validObjectType_returnsTrue() {
    ObjectType obj1 = registry.createAnonymousObjectType();
    ObjectType obj2 = registry.createAnonymousObjectType();

    boolean reset = registry.resetImplicitPrototype(obj1, obj2);
    assertTrue(reset);
    assertEquals(obj2, obj1.getImplicitPrototype());

    boolean resetNonProto = registry.resetImplicitPrototype(registry.getNativeType(JSTypeNative.NUMBER_TYPE), obj2);
    assertFalse(resetNonProto);
  }

  // Tests registering interface implementors
  @Test
  public void testRegisterTypeImplementingInterface_directImplementors_retrievedCorrectly() {
    FunctionType iface = registry.createInterfaceType("IInterface", null);
    ObjectType ifaceInstance = iface.getInstanceType();

    FunctionType implType = registry.createConstructorType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    registry.registerTypeImplementingInterface(implType, ifaceInstance);

    Collection<FunctionType> implementors = registry.getDirectImplementors(ifaceInstance);
    assertNotNull(implementors);
    assertTrue(implementors.contains(implType));
  }

  // Tests resolving mode and generation manipulation
  @Test
  public void testResolveModeAndGenerations_stateChangesCorrectly() {
    registry.setResolveMode(ResolveMode.IMMEDIATE);
    assertEquals(ResolveMode.IMMEDIATE, registry.getResolveMode());

    registry.setResolveMode(ResolveMode.LAZY_NAMES);
    assertEquals(ResolveMode.LAZY_NAMES, registry.getResolveMode());

    assertTrue(registry.isLastGeneration());
    registry.setLastGeneration(false);
    assertFalse(registry.isLastGeneration());

    registry.incrementGeneration();
    registry.clearNamedTypes();
  }

  // Tests createFromTypeNodes with AST token types
  @Test
  public void testCreateFromTypeNodes_variousASTNodes_createsCorrectTypes() {
    Node starNode = new Node(Token.STAR);
    JSType allType = registry.createFromTypeNodes(starNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.ALL_TYPE), allType);

    Node voidNode = new Node(Token.VOID);
    JSType voidType = registry.createFromTypeNodes(voidNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), voidType);

    Node emptyNode = new Node(Token.EMPTY);
    JSType unknownType = registry.createFromTypeNodes(emptyNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), unknownType);

    Node lbNode = new Node(Token.LB);
    JSType arrayType = registry.createFromTypeNodes(lbNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), arrayType);

    Node qmarkNode = new Node(Token.QMARK);
    JSType qmarkType = registry.createFromTypeNodes(qmarkNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), qmarkType);
  }

  // Tests creating parameterized type
  @Test
  public void testCreateParameterizedType_validObjectAndParameter_returnsParameterizedType() {
    ObjectType arrayObjType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    ParameterizedType paramType = registry.createParameterizedType(arrayObjType, strType);
    assertNotNull(paramType);
    assertEquals(strType, paramType.getParameterType());
  }

  // Tests creating record type with empty properties
  @Test
  public void testCreateRecordType_emptyMap_returnsRecordType() {
    RecordType recordType = registry.createRecordType(Collections.<String, RecordTypeBuilder.RecordProperty>emptyMap());
    assertNotNull(recordType);
    assertTrue(recordType.isRecordType());
  }

  // Tests tolerateUndefinedValues flag setting
  @Test
  public void testTolerateUndefinedValues_constructorFlag_honored() {
    JSTypeRegistry tolerantRegistry = new JSTypeRegistry(dummyReporter, true);
    assertTrue(tolerantRegistry.shouldTolerateUndefinedValues());
    assertFalse(registry.shouldTolerateUndefinedValues());

    JSType numType = tolerantRegistry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType defaultUnion = tolerantRegistry.createDefaultObjectUnion(numType);
    assertTrue(defaultUnion.isSubtype(tolerantRegistry.getNativeType(JSTypeNative.VOID_TYPE)));
    assertTrue(defaultUnion.isSubtype(tolerantRegistry.getNativeType(JSTypeNative.NULL_TYPE)));
  }
}
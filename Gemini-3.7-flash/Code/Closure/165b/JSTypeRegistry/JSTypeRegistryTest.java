package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JSTypeRegistryTest {

  private JSTypeRegistry registry;
  private ErrorReporter testReporter;

  @Before
  public void setUp() {
    testReporter = new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, int lineOffset) {}
      @Override
      public void error(String message, String sourceName, int line, int lineOffset) {}
    };
    registry = new JSTypeRegistry(testReporter);
  }

  // Tests initialization and lookup of native types
  @Test
  public void testGetNativeType_builtInTypes_returnsCorrectInstances() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNotNull(numberType);
    assertTrue(numberType.isNumber());

    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertNotNull(stringType);
    assertTrue(stringType.isString());

    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    assertNotNull(objectType);
    assertTrue(objectType.isObject());
  }

  // Tests resolving named types by string lookup
  @Test
  public void testGetType_registeredNames_returnsExpectedType() {
    JSType numberType = registry.getType("number");
    assertNotNull(numberType);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), numberType);

    JSType nonExistent = registry.getType("NonExistentType");
    assertNull(nonExistent);
  }

  // Tests declaring a new type and overwriting it
  @Test
  public void testDeclareAndOverwriteType_validTypes_managesCorrectly() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    boolean declared = registry.declareType("CustomType", numberType);
    assertTrue(declared);
    assertEquals(numberType, registry.getType("CustomType"));

    boolean declaredAgain = registry.declareType("CustomType", stringType);
    assertFalse(declaredAgain);

    registry.overwriteDeclaredType("CustomType", stringType);
    assertEquals(stringType, registry.getType("CustomType"));
  }

  // Tests exception when overwriting undeclared type
  @Test(expected = IllegalStateException.class)
  public void testOverwriteDeclaredType_undeclaredName_throwsException() {
    registry.overwriteDeclaredType("UnregisteredType", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  // Tests forward declaring types and checking namespace
  @Test
  public void testForwardDeclareType_andNamespace_trackedProperly() {
    assertFalse(registry.isForwardDeclaredType("com.example.MyType"));
    registry.forwardDeclareType("com.example.MyType");
    assertTrue(registry.isForwardDeclaredType("com.example.MyType"));

    registry.declareType("a.b.c.MyClass", registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    assertTrue(registry.hasNamespace("a"));
    assertTrue(registry.hasNamespace("a.b"));
    assertTrue(registry.hasNamespace("a.b.c"));
    assertFalse(registry.hasNamespace("a.b.c.d"));
  }

  // Tests creating nullable and optional types
  @Test
  public void testCreateNullableAndOptionalType_validTypes_returnsUnions() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullableNumber = registry.createNullableType(numberType);
    assertTrue(nullableNumber.isNullable());

    JSType optionalNumber = registry.createOptionalType(numberType);
    assertTrue(optionalNumber.isUnionType());

    JSType optionalUnknown = registry.createOptionalType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    assertTrue(optionalUnknown.isUnknownType());
  }

  // Tests default object union with tolerateUndefinedValues flag
  @Test
  public void testCreateDefaultObjectUnion_tolerateUndefined_includesVoidType() {
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType standardUnion = registry.createDefaultObjectUnion(objectType);
    assertTrue(standardUnion.isNullable());

    JSTypeRegistry tolerantRegistry = new JSTypeRegistry(testReporter, true);
    assertTrue(tolerantRegistry.shouldTolerateUndefinedValues());
    JSType tolerantUnion = tolerantRegistry.createDefaultObjectUnion(objectType);
    assertTrue(tolerantUnion.isNullable());
    assertTrue(tolerantUnion.isUnionType());
  }

  // Tests union type creation from multiple variants
  @Test
  public void testCreateUnionType_multipleTypes_buildsCorrectUnion() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());

    JSType nativeUnion = registry.createUnionType(JSTypeNative.NUMBER_TYPE, JSTypeNative.STRING_TYPE);
    assertTrue(nativeUnion.isUnionType());
  }

  // Tests property registration, querying, and unregistering on types
  @Test
  public void testRegisterAndUnregisterPropertyOnType_tracksCorrectly() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    registry.registerPropertyOnType("customProp", objType);

    assertTrue(registry.canPropertyBeDefined(objType, "customProp"));
    assertFalse(registry.canPropertyBeDefined(objType, "nonExistentProp"));

    Iterable<JSType> typesWithProp = registry.getTypesWithProperty("customProp");
    assertNotNull(typesWithProp);
    assertTrue(typesWithProp.iterator().hasNext());

    JSType greatestSubtype = registry.getGreatestSubtypeWithProperty(objType, "customProp");
    assertNotNull(greatestSubtype);

    registry.unregisterPropertyOnType("customProp", objType);
    Iterable<ObjectType> refTypes = registry.getEachReferenceTypeWithProperty("customProp");
    assertFalse(refTypes.iterator().hasNext());
  }

  // Tests template type registration and clearing
  @Test
  public void testTemplateType_setAndClear_updatesLookup() {
    registry.setTemplateTypeName("T");
    JSType templateType = registry.getType("T");
    assertNotNull(templateType);
    assertTrue(templateType.isTemplateType());

    registry.clearTemplateTypeName();
    assertNull(registry.getType("T"));
  }

  // Tests interface implementors tracking
  @Test
  public void testInterfaceImplementors_registered_returnsCorrectCollection() {
    FunctionType interfaceType = registry.createInterfaceType("MyInterface", null);
    ObjectType interfaceInstance = interfaceType.getInstanceType();

    FunctionType implementorType = registry.createConstructorType("MyImplementor", null, null, null);
    registry.registerTypeImplementingInterface(implementorType, interfaceInstance);

    Collection<FunctionType> implementors = registry.getDirectImplementors(interfaceInstance);
    assertEquals(1, implementors.size());
    assertTrue(implementors.contains(implementorType));
  }

  // Tests function type creation and modification
  @Test
  public void testCreateFunctionType_variousSignatures_buildsCorrectFunction() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    FunctionType fn = registry.createFunctionType(numberType, stringType);
    assertNotNull(fn);
    assertEquals(numberType, fn.getReturnType());

    FunctionType modifiedReturn = registry.createFunctionTypeWithNewReturnType(fn, stringType);
    assertEquals(stringType, modifiedReturn.getReturnType());

    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    FunctionType modifiedThis = registry.createFunctionTypeWithNewThisType(fn, objType);
    assertEquals(objType, modifiedThis.getTypeOfThis());
  }

  // Tests record type creation via RecordTypeBuilder
  @Test
  public void testCreateRecordType_fromMap_createsRecord() {
    Map<String, RecordProperty> fields = new HashMap<String, RecordProperty>();
    fields.put("x", new RecordProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE), null));
    RecordType record = registry.createRecordType(fields);
    assertNotNull(record);
    assertTrue(record.isRecordType());
    assertTrue(record.hasProperty("x"));
  }

  // Tests creating types from basic AST nodes
  @Test
  public void testCreateFromTypeNodes_primitivesAndModifiers_evaluatesProperly() {
    Node starNode = new Node(Token.STAR);
    JSType allType = registry.createFromTypeNodes(starNode, "test.js", null);
    assertTrue(allType.isAllType());

    Node emptyNode = new Node(Token.EMPTY);
    JSType unknownType = registry.createFromTypeNodes(emptyNode, "test.js", null);
    assertTrue(unknownType.isUnknownType());

    Node bangNode = new Node(Token.BANG, Node.newString("number"));
    JSType notNullNumber = registry.createFromTypeNodes(bangNode, "test.js", null);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), notNullNumber);

    Node qmarkNode = new Node(Token.QMARK);
    JSType qmarkUnknown = registry.createFromTypeNodes(qmarkNode, "test.js", null);
    assertTrue(qmarkUnknown.isUnknownType());
  }

  // Tests creating record type from AST nodes (Token.LC)
  @Test
  public void testCreateFromTypeNodes_recordType_buildsExpectedRecord() {
    Node colonNode = new Node(Token.COLON, Node.newString("prop"), Node.newString("string"));
    Node lcNode = new Node(Token.LC, colonNode);

    JSType result = registry.createFromTypeNodes(lcNode, "test.js", null);
    assertNotNull(result);
    assertTrue(result.isRecordType());
    assertTrue(result.toObjectType().hasProperty("prop"));
  }

  // Tests creating function type from AST nodes (Token.FUNCTION)
  @Test
  public void testCreateFromTypeNodes_functionExpression_buildsFunction() {
    Node paramList = new Node(Token.PARAM_LIST, Node.newString("number"));
    Node returnType = Node.newString("string");
    Node fnNode = new Node(Token.FUNCTION, paramList, returnType);

    JSType result = registry.createFromTypeNodes(fnNode, "test.js", null);
    assertNotNull(result);
    assertTrue(result.isFunctionType());
    FunctionType fnType = result.toMaybeFunctionType();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
  }

  // Tests findCommonSuperObject
  @Test
  public void testFindCommonSuperObject_sharedInheritance_findsSupertype() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    ObjectType common = registry.findCommonSuperObject(arrayType, objType);
    assertEquals(objType, common);
  }

  // Tests resetImplicitPrototype on prototype objects
  @Test
  public void testResetImplicitPrototype_updatesPrototype() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType customObj = registry.createObjectType(null);
    boolean success = registry.resetImplicitPrototype(customObj, objType);
    assertTrue(success);
    assertEquals(objType, customObj.getImplicitPrototype());

    boolean failOnNonProto = registry.resetImplicitPrototype(registry.getNativeType(JSTypeNative.NUMBER_TYPE), objType);
    assertFalse(failOnNonProto);
  }

  // Tests resolving mode configuration
  @Test
  public void testSetResolveMode_updatesMode() {
    registry.setResolveMode(JSTypeRegistry.ResolveMode.IMMEDIATE);
    assertEquals(JSTypeRegistry.ResolveMode.IMMEDIATE, registry.getResolveMode());
  }
}
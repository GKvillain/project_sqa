package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

public class FunctionTypeBuilderTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope scope;
  private Node rootNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    rootNode = new Node(Token.BLOCK);
    scope = Scope.createGlobalScope(rootNode);
  }

  // Tests building function type without setting parameters throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testBuildAndRegister_withoutParams_throwsException() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "foo", compiler, rootNode, "test.js", scope);
    builder.buildAndRegister();
  }

  // Tests normal function type building with parameter and return type inference
  @Test
  public void testBuildAndRegister_normalFunction_success() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "add", compiler, rootNode, "test.js", scope);

    Node paramsNode = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    builder.inferParameterTypes(paramsNode, null);
    builder.inferReturnType(null);

    FunctionType fnType = builder.buildAndRegister();
    assertNotNull(fnType);
    assertEquals("add", fnType.getDisplayName());
    assertTrue(fnType.getReturnType().isUnknownType());
  }

  // Tests inferring return type from null JSDocInfo defaults to unknown type
  @Test
  public void testInferReturnType_nullInfo_setsUnknownType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);

    builder.inferReturnType(null);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  // Tests inferring inheritance with null JSDocInfo does not set constructor or interface
  @Test
  public void testInferInheritance_nullInfo_doesNothing() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);

    builder.inferInheritance(null);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertFalse(fnType.isConstructor());
    assertFalse(fnType.isInterface());
  }

  // Tests inferring inheritance when @constructor is specified in JSDocInfo
  @Test
  public void testInferInheritance_constructorInfo_createsConstructor() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyClass", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isConstructor());
  }

  // Tests inferring inheritance when @interface is specified in JSDocInfo
  @Test
  public void testInferInheritance_interfaceInfo_createsInterface() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyInterface", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isInterface());
  }

  // Tests inferring this type using direct JSType
  @Test
  public void testInferThisType_withJSType_setsThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);

    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, objectType);
    builder.inferParameterTypes(new Node(Token.LP), null);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(objectType, fnType.getTypeOfThis());
  }

  // Tests inferring this type from owner node when info is null
  @Test
  public void testInferThisType_withOwnerNode_setsThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);

    Node ownerNode = Node.newString(Token.NAME, "Object");
    builder.inferThisType(null, ownerNode);
    builder.inferParameterTypes(new Node(Token.LP), null);

    FunctionType fnType = builder.buildAndRegister();
    assertNotNull(fnType.getTypeOfThis());
  }

  // Tests inferring from overridden function without params parent
  @Test
  public void testInferFromOverriddenFunction_nullParamsParent_copiesParameters() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "overrideFn", compiler, rootNode, "test.js", scope);

    builder.inferFromOverriddenFunction(oldType, null);
    FunctionType newType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), newType.getReturnType());
  }

  // Tests inferring from overridden function with params parent node
  @Test
  public void testInferFromOverriddenFunction_withParamsParent_appliesParamTypes() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    Node paramsParent = new Node(Token.LP, Node.newString(Token.NAME, "param1"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "overrideFn", compiler, rootNode, "test.js", scope);

    builder.inferFromOverriddenFunction(oldType, paramsParent);
    FunctionType newType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), newType.getReturnType());
  }

  // Tests template type name inference from JSDocInfo
  @Test
  public void testInferTemplateTypeName_withTemplateInfo_setsTemplate() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "templatedFn", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordTemplateTypeName("T");
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferTemplateTypeName(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
  }

  // Tests isFunctionTypeDeclaration with non-function doc info
  @Test
  public void testIsFunctionTypeDeclaration_emptyDoc_returnsFalse() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    JSDocInfo info = docBuilder.build(rootNode);

    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with constructor doc info
  @Test
  public void testIsFunctionTypeDeclaration_constructorDoc_returnsTrue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(rootNode);

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests setSourceNode chain call
  @Test
  public void testSetSourceNode_returnsSameBuilder() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);
    Node sourceNode = new Node(Token.FUNCTION);

    FunctionTypeBuilder result = builder.setSourceNode(sourceNode);
    assertSame(builder, result);
  }

  // Tests isFunctionTypeDeclaration with null info
  @Test
  public void testIsFunctionTypeDeclaration_null_returnsFalse() {
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(null));
  }

  // Tests isFunctionTypeDeclaration with return type in doc info
  @Test
  public void testIsFunctionTypeDeclaration_returnTypeDoc_returnsTrue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with parameters in doc info
  @Test
  public void testIsFunctionTypeDeclaration_paramDoc_returnsTrue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("x");
    docBuilder.recordParameterType("x", new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with this type in doc info
  @Test
  public void testIsFunctionTypeDeclaration_thisTypeDoc_returnsTrue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with interface doc info
  @Test
  public void testIsFunctionTypeDeclaration_interfaceDoc_returnsTrue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(rootNode);

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests inferReturnType from JSDocInfo with declared return type
  @Test
  public void testInferReturnType_withDeclaredReturnType_setsDeclaredType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "getStr", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferReturnType(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
  }

  // Tests inferReturnType on constructor sets instance type as return type
  @Test
  public void testInferReturnType_constructor_setsInstanceType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyClass", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferReturnType(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isConstructor());
    assertEquals(fnType.getInstanceType(), fnType.getReturnType());
  }

  // Tests inferParameterTypes using an existing FunctionType directly
  @Test
  public void testInferParameterTypes_withFunctionType_copiesParameters() {
    FunctionType existingFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "copiedFn", compiler, rootNode, "test.js", scope);

    builder.inferParameterTypes(existingFn);
    builder.inferReturnType(null);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(2, fnType.getParametersCount());
  }

  // Tests inferParameterTypes with typed parameters in JSDocInfo
  @Test
  public void testInferParameterTypes_withTypedParamsInDoc() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "add", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("a");
    docBuilder.recordParameterType("a", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    docBuilder.recordParameter("b");
    docBuilder.recordParameterType("b", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    Node paramsNode = new Node(Token.LP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));

    builder.inferParameterTypes(paramsNode, info);
    builder.inferReturnType(null);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(2, fnType.getParametersCount());
  }

  // Tests inferThisType with @this JSDocInfo
  @Test
  public void testInferThisType_withThisDocInfo_setsThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "method", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(new JSTypeExpression(Node.newString(Token.NAME, "Array"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferThisType(info, (Node) null);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType.getTypeOfThis());
    assertFalse(fnType.getTypeOfThis().isUnknownType());
  }

  // Tests inferThisType with null JSType
  @Test
  public void testInferThisType_withNullJSType_doesNotThrow() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, rootNode, "test.js", scope);

    builder.inferThisType(null, (JSType) null);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
  }

  // Tests inferInheritance with @implements in constructor
  @Test
  public void testInferInheritance_constructorWithInterfaceImplementation() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyImpl", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordImplementedInterface(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isConstructor());
    assertNotNull(fnType.getImplementedInterfaces());
  }

  // Tests inferInheritance with @extends base type
  @Test
  public void testInferInheritance_constructorWithBaseType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "SubClass", compiler, rootNode, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isConstructor());
  }
}
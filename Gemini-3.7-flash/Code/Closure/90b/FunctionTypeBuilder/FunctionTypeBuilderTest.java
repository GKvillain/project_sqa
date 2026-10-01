package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;

public class FunctionTypeBuilderTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Node rootNode;
  private Scope scope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    rootNode = new Node(Token.SCRIPT);
    scope = Scope.createGlobalScope(rootNode);
  }

  // Tests isFunctionTypeDeclaration with empty doc info
  @Test
  public void testIsFunctionTypeDeclaration_emptyDoc_returnsFalse() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    JSDocInfo info = builder.build(rootNode);
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with constructor tag
  @Test
  public void testIsFunctionTypeDeclaration_constructor_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordConstructor();
    JSDocInfo info = builder.build(rootNode);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with interface tag
  @Test
  public void testIsFunctionTypeDeclaration_interface_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordInterface();
    JSDocInfo info = builder.build(rootNode);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with return type tag
  @Test
  public void testIsFunctionTypeDeclaration_returnType_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordReturnType(new JSTypeExpression(new Node(Token.STRING), "test"));
    JSDocInfo info = builder.build(rootNode);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with this type tag
  @Test
  public void testIsFunctionTypeDeclaration_thisType_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordThisType(new JSTypeExpression(new Node(Token.STRING), "test"));
    JSDocInfo info = builder.build(rootNode);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration with parameter tags
  @Test
  public void testIsFunctionTypeDeclaration_parameterCount_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordParameter("x", new JSTypeExpression(new Node(Token.STRING), "test"));
    JSDocInfo info = builder.build(rootNode);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests exception when building without parameter node
  @Test(expected = IllegalStateException.class)
  public void testBuildAndRegister_missingParameters_throwsException() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.buildAndRegister();
  }

  // Tests building standard function type with inferred void return from empty block
  @Test
  public void testInferReturnStatementsAsLastResort_emptyBlock_infersVoid() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node block = new Node(Token.BLOCK);
    builder.inferReturnStatementsAsLastResort(block);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();
    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), fnType.getReturnType());
    assertTrue(fnType.isReturnTypeInferred());
  }

  // Tests inferring return statements when return statement exists
  @Test
  public void testInferReturnStatementsAsLastResort_withReturn_doesNotInferVoid() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node block = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN, Node.newString("val"));
    block.addChildToBack(returnNode);
    builder.inferReturnStatementsAsLastResort(block);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();
    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  // Tests inferring this type from JSType ObjectType
  @Test
  public void testInferThisType_fromObjectType_setsThisType() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, objType);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();
    assertEquals(objType, fnType.getTypeOfThis());
  }

  // Tests inferring this type from owner node
  @Test
  public void testInferThisType_fromOwnerNode_setsThisType() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node owner = Node.newString(Token.NAME, "Object");
    builder.inferThisType(null, owner);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_TYPE), fnType.getTypeOfThis());
  }

  // Tests extends warning when used without constructor or interface
  @Test
  public void testInferInheritance_extendsWithoutConstructor_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordBaseType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests implements warning when used without constructor
  @Test
  public void testInferInheritance_implementsWithoutConstructor_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests constructor with extends inherits base type
  @Test
  public void testInferInheritance_constructorWithExtends_setsBaseType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isConstructor());
    assertNotNull(fnType.getPrototype());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests parameter parsing and inexistent parameter warning
  @Test
  public void testInferParameterTypes_inexistentParam_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("y", new JSTypeExpression(new Node(Token.STRING), "test"));
    JSDocInfo info = docBuilder.build(rootNode);

    Node paramsNode = new Node(Token.LP, Node.newString(Token.NAME, "x"));
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(paramsNode, info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests inferring parameter and return types from overridden function
  @Test
  public void testInferFromOverriddenFunction_validOldType_copiesTypes() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        new Node(Token.LP, Node.newString(Token.NAME, "a")));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node paramsNode = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    builder.inferFromOverriddenFunction(oldType, paramsNode);
    FunctionType newType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), newType.getReturnType());
    assertNotNull(newType.getParametersNode());
  }

  // Tests inferring template type name
  @Test
  public void testInferTemplateTypeName_validTemplate_registersTemplate() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordTemplateTypeName("T");
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferTemplateTypeName(info);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
  }

  // Tests building interface type
  @Test
  public void testBuildAndRegister_interfaceType_createsInterface() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyInterface", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isInterface());
  }

  // Tests inferReturnType from JSDocInfo return type
  @Test
  public void testInferReturnType_fromDocInfo_setsReturnType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(
        new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferReturnType(info);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
    assertFalse(fnType.isReturnTypeInferred());
  }

  // Tests constructor with @return tag emits warning
  @Test
  public void testInferReturnType_constructorWithReturnType_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordReturnType(
        new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferReturnType(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests interface with @return tag emits warning
  @Test
  public void testInferReturnType_interfaceWithReturnType_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    docBuilder.recordReturnType(
        new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyInterface", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferReturnType(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests inferThisType with @this tag from JSDocInfo
  @Test
  public void testInferThisType_fromDocInfo_setsThisType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(
        new JSTypeExpression(Node.newString(Token.NAME, "Array"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferThisType(info, (Node) null);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), fnType.getTypeOfThis());
  }

  // Tests constructor with @this tag emits warning
  @Test
  public void testInferThisType_constructorWithThisType_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordThisType(
        new JSTypeExpression(Node.newString(Token.NAME, "Array"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferThisType(info, (Node) null);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests interface extending multiple valid interfaces
  @Test
  public void testInferInheritance_interfaceExtendsMultiple_success() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    docBuilder.recordExtendedInterface(
        new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyInterface", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertTrue(fnType.isInterface());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests interface extending a non-interface type emits warning
  @Test
  public void testInferInheritance_interfaceExtendsNonInterface_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    docBuilder.recordExtendedInterface(
        new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyInterface", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests constructor extending non-object/constructor emits warning
  @Test
  public void testInferInheritance_constructorExtendsPrimitive_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(
        new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests constructor implementing duplicate interfaces emits warning
  @Test
  public void testInferInheritance_constructorImplementsDuplicate_emitsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests building anonymous function without function name
  @Test
  public void testBuildAndRegister_anonymousFunction_success() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder(null, compiler, rootNode, "test.js", scope);
    Node paramsNode = new Node(Token.LP);
    builder.inferParameterTypes(paramsNode, null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertFalse(fnType.isConstructor());
    assertFalse(fnType.isInterface());
  }

  // Tests inferFromOverriddenFunction with null oldType does nothing
  @Test
  public void testInferFromOverriddenFunction_nullOldType_noOp() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node paramsNode = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    builder.inferFromOverriddenFunction(null, paramsNode);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  // Tests inferParameterTypes with typed parameters in JSDoc
  @Test
  public void testInferParameterTypes_withTypes_setsParamTypes() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter(
        "x", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    docBuilder.recordParameter(
        "y", new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    Node paramsNode = new Node(Token.LP,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"));
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(paramsNode, info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(0, compiler.getWarningCount());
  }
}
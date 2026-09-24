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
  private Node errorRoot;
  private Scope scope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    errorRoot = new Node(Token.SCRIPT);
    scope = Scope.createGlobalScope(errorRoot);
  }

  // Tests function type declaration check with constructor tag
  @Test
  public void testIsFunctionTypeDeclaration_withConstructor_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordConstructor();
    JSDocInfo info = builder.build("test.js");

    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests function type declaration check with empty JSDoc
  @Test
  public void testIsFunctionTypeDeclaration_emptyDoc_returnsFalse() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    JSDocInfo info = builder.build("test.js");

    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests exception when building without parameter info
  @Test(expected = IllegalStateException.class)
  public void testBuildAndRegister_missingParameters_throwsException() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);
    builder.buildAndRegister();
  }

  // Tests standard function building with simple parameter and return type
  @Test
  public void testBuildAndRegister_basicFunction_createsFunctionType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "a"));

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter(
        "a", new JSTypeExpression(Node.newString("number"), "test.js"));
    docBuilder.recordReturnType(
        new JSTypeExpression(Node.newString("string"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferParameterTypes(lp, info);
    builder.inferReturnType(info);

    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests inferring void return type from empty block
  @Test
  public void testInferReturnStatementsAsLastResort_emptyBlock_infersVoid() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    Node block = new Node(Token.BLOCK);
    builder.inferReturnStatementsAsLastResort(block);

    Node lp = new Node(Token.LP);
    builder.inferParameterTypes(lp, null);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), fnType.getReturnType());
  }

  // Tests inferring return statements when function has a return expression
  @Test
  public void testInferReturnStatementsAsLastResort_withReturn_doesNotInferVoid() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    Node block = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN, Node.newNumber(42));
    block.addChildToBack(returnNode);

    builder.inferReturnStatementsAsLastResort(block);
    builder.inferParameterTypes(new Node(Token.LP), null);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  // Tests warning emission when @extends is used without @constructor or @interface
  @Test
  public void testInferInheritance_extendsWithoutConstructor_emitsWarning() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordBaseType(
        new JSTypeExpression(Node.newString("Object"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferInheritance(info);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests warning emission when @implements is used without @constructor or @interface
  @Test
  public void testInferInheritance_implementsWithoutConstructor_emitsWarning() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(Node.newString("Object"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferInheritance(info);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests valid constructor creation with inheritance
  @Test
  public void testInferInheritance_constructorWithExtends_setsBaseType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyClass", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(
        new JSTypeExpression(Node.newString("Object"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);

    FunctionType fnType = builder.buildAndRegister();
    assertTrue(fnType.isConstructor());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferring @this type from JSDocInfo
  @Test
  public void testInferThisType_fromJSDoc_setsThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "method", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(
        new JSTypeExpression(Node.newString("Object"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferThisType(info, (Node) null);
    builder.inferParameterTypes(new Node(Token.LP), info);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        fnType.getTypeOfThis());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferring @this type directly from JSType
  @Test
  public void testInferThisType_fromJSType_setsThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "method", compiler, errorRoot, "test.js", scope);

    ObjectType objType = (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, objType);
    builder.inferParameterTypes(new Node(Token.LP), null);

    FunctionType fnType = builder.buildAndRegister();
    assertEquals(objType, fnType.getTypeOfThis());
  }

  // Tests warning when @param does not exist in function argument list
  @Test
  public void testInferParameterTypes_inexistentParam_reportsWarning() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "a"));

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter(
        "nonExistent", new JSTypeExpression(Node.newString("number"), "test.js"));
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferParameterTypes(lp, info);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests warning when optional argument precedes a required argument
  @Test
  public void testInferParameterTypes_optionalBeforeRequired_reportsWarning() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testFn", compiler, errorRoot, "test.js", scope);

    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "opt_a"));
    lp.addChildToBack(Node.newString(Token.NAME, "b"));

    builder.inferParameterTypes(lp, null);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests inferring types from overridden function
  @Test
  public void testInferFromOverriddenFunction_copiesParametersAndReturnType() {
    FunctionType origFnType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "subFn", compiler, errorRoot, "test.js", scope);

    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "arg0"));

    builder.inferFromOverriddenFunction(origFnType, lp);
    FunctionType newFnType = builder.buildAndRegister();

    assertEquals(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        newFnType.getReturnType());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferring interface function declaration
  @Test
  public void testBuildAndRegister_interfaceType_createsInterface() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "AnInterface", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build("test.js");

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);

    FunctionType fnType = builder.buildAndRegister();
    assertTrue(fnType.isInterface());
    assertEquals(0, compiler.getWarningCount());
  }
}
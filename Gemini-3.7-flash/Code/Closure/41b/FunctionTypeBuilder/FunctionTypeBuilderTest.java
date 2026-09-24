package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.IR;
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
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    errorRoot = IR.script();
    scope = Scope.createGlobalScope(errorRoot);
  }

  // Tests isFunctionTypeDeclaration with empty doc info
  @Test
  public void testIsFunctionTypeDeclaration_emptyInfo_returnsFalse() {
    JSDocInfo info = new JSDocInfo();
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration when constructor tag is present
  @Test
  public void testIsFunctionTypeDeclaration_constructor_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo info = builder.build(errorRoot);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration when return type is present
  @Test
  public void testIsFunctionTypeDeclaration_returnType_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordReturnType(new JSTypeExpression(IR.string("number"), "test.js"));
    JSDocInfo info = builder.build(errorRoot);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration when parameter is present
  @Test
  public void testIsFunctionTypeDeclaration_parameters_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordParameter("x", new JSTypeExpression(IR.string("string"), "test.js"));
    JSDocInfo info = builder.build(errorRoot);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests isFunctionTypeDeclaration when interface tag is present
  @Test
  public void testIsFunctionTypeDeclaration_interface_returnsTrue() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordInterface();
    JSDocInfo info = builder.build(errorRoot);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  // Tests buildAndRegister throws IllegalStateException when parameter node is missing
  @Test(expected = IllegalStateException.class)
  public void testBuildAndRegister_missingParameters_throwsException() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope);
    builder.buildAndRegister();
  }

  // Tests building a basic function with param types and return type
  @Test
  public void testBuildAndRegister_normalFunction_buildsCorrectFunctionType() {
    Node paramList = IR.paramList(IR.name("a"), IR.name("b"));
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordParameter("a", new JSTypeExpression(IR.string("number"), "test.js"));
    docBuilder.recordParameter("b", new JSTypeExpression(IR.string("string"), "test.js"));
    docBuilder.recordReturnType(new JSTypeExpression(IR.string("boolean"), "test.js"));
    JSDocInfo info = docBuilder.build(errorRoot);

    FunctionType fnType =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope)
            .inferParameterTypes(paramList, info)
            .inferReturnType(info)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), fnType.getReturnType());
    assertEquals(2, fnType.getParametersNode().getChildCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests inferParameterTypes with inexistent doc parameter reports warning
  @Test
  public void testInferParameterTypes_inexistentParam_reportsWarning() {
    Node paramList = IR.paramList(IR.name("a"));
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordParameter("a", new JSTypeExpression(IR.string("number"), "test.js"));
    docBuilder.recordParameter("extraParam", new JSTypeExpression(IR.string("string"), "test.js"));
    JSDocInfo info = docBuilder.build(errorRoot);

    FunctionType fnType =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope)
            .inferParameterTypes(paramList, info)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        FunctionTypeBuilder.INEXISTANT_PARAM.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests inferParameterTypes when argsParent is null uses doc parameter list
  @Test
  public void testInferParameterTypes_nullArgsParent_infersFromDoc() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordParameter("x", new JSTypeExpression(IR.string("number"), "test.js"));
    JSDocInfo info = docBuilder.build(errorRoot);

    FunctionType fnType =
        new FunctionTypeBuilder("bar", compiler, errorRoot, "test.js", scope)
            .inferParameterTypes(null, info)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(1, fnType.getParametersNode().getChildCount());
  }

  // Tests constructor creation and registry declaration
  @Test
  public void testInferInheritance_constructor_createsConstructorType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(errorRoot);

    Node paramList = IR.paramList();
    FunctionType fnType =
        new FunctionTypeBuilder("MyClass", compiler, errorRoot, "test.js", scope)
            .inferInheritance(info)
            .inferParameterTypes(paramList, info)
            .buildAndRegister();

    assertTrue(fnType.isConstructor());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests @extends without @constructor or @interface reports warning
  @Test
  public void testInferInheritance_extendsWithoutConstructor_reportsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordBaseType(new JSTypeExpression(IR.string("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(errorRoot);

    Node paramList = IR.paramList();
    new FunctionTypeBuilder("notClass", compiler, errorRoot, "test.js", scope)
        .inferInheritance(info)
        .inferParameterTypes(paramList, info)
        .buildAndRegister();

    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        FunctionTypeBuilder.EXTENDS_WITHOUT_TYPEDEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests @implements without @constructor reports warning
  @Test
  public void testInferInheritance_implementsWithoutConstructor_reportsWarning() {
    JSDocInfoBuilder docBuilder = compulsoryInterfaceDocBuilder();
    JSDocInfo info = docBuilder.build(errorRoot);

    Node paramList = IR.paramList();
    new FunctionTypeBuilder("notClass", compiler, errorRoot, "test.js", scope)
        .inferInheritance(info)
        .inferParameterTypes(paramList, info)
        .buildAndRegister();

    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        FunctionTypeBuilder.IMPLEMENTS_WITHOUT_CONSTRUCTOR.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests @this with non-object type reports warning
  @Test
  public void testInferThisType_nonObjectType_reportsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordThisType(new JSTypeExpression(IR.string("number"), "test.js"));
    JSDocInfo info = docBuilder.build(errorRoot);

    Node paramList = IR.paramList();
    FunctionType fnType =
        new FunctionTypeBuilder("withThis", compiler, errorRoot, "test.js", scope)
            .inferThisType(info)
            .inferParameterTypes(paramList, info)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        FunctionTypeBuilder.THIS_TYPE_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests inferFromOverriddenFunction copying return type and parameters
  @Test
  public void testInferFromOverriddenFunction_validOldType_copiesTypes() {
    FunctionType oldType =
        registry.createFunctionType(
            registry.getNativeType(JSTypeNative.NUMBER_TYPE),
            registry.getNativeType(JSTypeNative.STRING_TYPE));

    Node paramList = IR.paramList(IR.name("s"));
    FunctionType fnType =
        new FunctionTypeBuilder("overrideFn", compiler, errorRoot, "test.js", scope)
            .inferFromOverriddenFunction(oldType, paramList)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fnType.getReturnType());
    assertEquals(1, fnType.getParametersNode().getChildCount());
  }

  // Tests inferFromOverriddenFunction with null paramsParent
  @Test
  public void testInferFromOverriddenFunction_nullParamsParent_copiesParametersNode() {
    FunctionType oldType =
        registry.createFunctionType(
            registry.getNativeType(JSTypeNative.STRING_TYPE),
            registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    FunctionType fnType =
        new FunctionTypeBuilder("overrideFnNoLiteral", compiler, errorRoot, "test.js", scope)
            .inferFromOverriddenFunction(oldType, null)
            .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
  }

  // Tests AstFunctionContents recording and reading non-empty return
  @Test
  public void testAstFunctionContents_recordsNonEmptyReturn() {
    Node fnNode = IR.function(IR.name("fn"), IR.paramList(), IR.block());
    FunctionTypeBuilder.AstFunctionContents contents =
        new FunctionTypeBuilder.AstFunctionContents(fnNode);

    assertFalse(contents.mayHaveNonEmptyReturns());
    contents.recordNonEmptyReturn();
    assertTrue(contents.mayHaveNonEmptyReturns());
    assertEquals(fnNode, contents.getSourceNode());
  }

  // Tests UnknownFunctionContents defaults
  @Test
  public void testUnknownFunctionContents_returnsDefaultValues() {
    FunctionTypeBuilder.FunctionContents contents =
        FunctionTypeBuilder.UnknownFunctionContents.get();

    assertNull(contents.getSourceNode());
    assertTrue(contents.mayBeFromExterns());
    assertTrue(contents.mayHaveNonEmptyReturns());
    assertFalse(contents.getEscapedVarNames().iterator().hasNext());
  }

  private JSDocInfoBuilder compulsoryInterfaceDocBuilder() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(IR.string("IDisposable"), "test.js"));
    return docBuilder;
  }
}
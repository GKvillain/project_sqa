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
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    errorRoot = IR.name("testNode");
    scope = Scope.createGlobalScope(errorRoot);
  }

  // Tests constructor null check on errorRoot
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullErrorRoot_throwsNullPointerException() {
    new FunctionTypeBuilder("foo", compiler, null, "test.js", scope);
  }

  // Tests building function type without initializing parametersNode throws exception
  @Test(expected = IllegalStateException.class)
  public void testBuildAndRegister_missingParametersNode_throwsIllegalStateException() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope);
    builder.buildAndRegister();
  }

  // Tests normal function building with empty parameters and default return type
  @Test
  public void testBuildAndRegister_emptyParams_createsFunctionType() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertFalse(fnType.isConstructor());
    assertFalse(fnType.isInterface());
    assertEquals(0, fnType.getParametersCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests constructor function creation from JSDoc
  @Test
  public void testInferInheritance_constructorAnnotation_createsConstructorType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("FooCtor", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertTrue(fnType.isConstructor());
    assertFalse(fnType.isInterface());
  }

  // Tests interface function creation from JSDoc
  @Test
  public void testInferInheritance_interfaceAnnotation_createsInterfaceType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("FooInterface", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertTrue(fnType.isInterface());
    assertFalse(fnType.isConstructor());
  }

  // Tests extends tag used without @constructor or @interface emits warning
  @Test
  public void testInferInheritance_extendsWithoutConstructorOrInterface_reportsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordBaseType(new JSTypeExpression(IR.string("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_WITHOUT_TYPEDEF,
        compiler.getWarnings()[0].getType());
  }

  // Tests implements tag used without @constructor reports warning
  @Test
  public void testInferInheritance_implementsWithoutConstructor_reportsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(IR.string("SomeInterface"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.IMPLEMENTS_WITHOUT_CONSTRUCTOR,
        compiler.getWarnings()[0].getType());
  }

  // Tests inferring parameter and return types from overridden function
  @Test
  public void testInferFromOverriddenFunction_validFunction_copiesSignature() {
    FunctionType origFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("overrideFn", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(origFn, null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fnType.getReturnType());
  }

  // Tests inferFromOverriddenFunction with null oldType does nothing
  @Test
  public void testInferFromOverriddenFunction_nullOldType_noOp() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fn", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(null, null);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
  }

  // Tests parameter parsing with normal parameters
  @Test
  public void testInferParameterTypes_withNamedArgs_setsParameterCount() {
    Node paramList = IR.paramList(IR.name("a"), IR.name("b"));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnWithArgs", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(paramList, null);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(2, fnType.getParametersCount());
  }

  // Tests optional parameter before required parameter emits warning
  @Test
  public void testInferParameterTypes_optionalParamBeforeRequired_reportsWarning() {
    Node paramList = IR.paramList(IR.name("opt_a"), IR.name("b"));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("invalidOrderFn", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(paramList, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.OPTIONAL_ARG_AT_END,
        compiler.getWarnings()[0].getType());
  }

  // Tests var_args parameter not at end emits warning
  @Test
  public void testInferParameterTypes_varArgsNotLast_reportsWarning() {
    Node paramList = IR.paramList(IR.name("var_args"), IR.name("b"));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("varArgsFn", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(paramList, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.VAR_ARGS_MUST_BE_LAST,
        compiler.getWarnings()[0].getType());
  }

  // Tests doc info with parameter not in AST parameter list emits inexistant param warning
  @Test
  public void testInferParameterTypes_paramInDocNotInAst_reportsWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter(
        "missingParam", new JSTypeExpression(IR.string("string"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    Node paramList = IR.paramList(IR.name("existingParam"));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fn", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(paramList, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.INEXISTANT_PARAM,
        compiler.getWarnings()[0].getType());
  }

  // Tests inferThisType with valid object type
  @Test
  public void testInferThisType_validObjectType_setsTypeOfThis() {
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fn", compiler, errorRoot, "test.js", scope);
    builder.inferThisType(null, registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType.getTypeOfThis());
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_TYPE), fnType.getTypeOfThis());
  }

  // Tests isFunctionTypeDeclaration with various JSDoc inputs
  @Test
  public void testIsFunctionTypeDeclaration_variousJSDocTags() {
    JSDocInfoBuilder emptyBuilder = new JSDocInfoBuilder(true);
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(emptyBuilder.build(null)));

    JSDocInfoBuilder ctorBuilder = new JSDocInfoBuilder(true);
    ctorBuilder.recordConstructor();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(ctorBuilder.build(null)));

    JSDocInfoBuilder ifaceBuilder = new JSDocInfoBuilder(true);
    ifaceBuilder.recordInterface();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(ifaceBuilder.build(null)));

    JSDocInfoBuilder returnBuilder = new JSDocInfoBuilder(true);
    returnBuilder.recordReturnType(new JSTypeExpression(IR.string("number"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(returnBuilder.build(null)));

    JSDocInfoBuilder thisBuilder = new JSDocInfoBuilder(true);
    thisBuilder.recordThisType(new JSTypeExpression(IR.string("Object"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(thisBuilder.build(null)));
  }

  // Tests AstFunctionContents methods
  @Test
  public void testAstFunctionContents_recordingProperties_reflectsState() {
    Node fnNode = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    FunctionTypeBuilder.AstFunctionContents contents =
        new FunctionTypeBuilder.AstFunctionContents(fnNode);

    assertEquals(fnNode, contents.getSourceNode());
    assertFalse(contents.mayHaveNonEmptyReturns());

    contents.recordNonEmptyReturn();
    assertTrue(contents.mayHaveNonEmptyReturns());

    assertFalse(contents.getEscapedVarNames().iterator().hasNext());
    contents.recordEscapedVarName("x");
    assertTrue(contents.getEscapedVarNames().iterator().hasNext());
    assertEquals("x", contents.getEscapedVarNames().iterator().next());
  }

  // Tests UnknownFunctionContents singleton methods
  @Test
  public void testUnknownFunctionContents_defaultBehavior() {
    FunctionTypeBuilder.FunctionContents contents =
        FunctionTypeBuilder.UnknownFunctionContents.get();

    assertNull(contents.getSourceNode());
    assertTrue(contents.mayBeFromExterns());
    assertTrue(contents.mayHaveNonEmptyReturns());
    assertFalse(contents.getEscapedVarNames().iterator().hasNext());
  }

  // Tests inferReturnType from JSDoc info
  @Test
  public void testInferReturnType_withDocReturnType_setsReturnType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(new JSTypeExpression(IR.string("string"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("returnFn", compiler, errorRoot, "test.js", scope);
    builder.inferReturnType(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
  }

  // Tests inferThisType with JSDoc @this tag
  @Test
  public void testInferThisType_withJSDocThisType_setsTypeOfThis() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(new JSTypeExpression(IR.string("Array"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("thisFn", compiler, errorRoot, "test.js", scope);
    builder.inferThisType(info, (JSType) null);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType.getTypeOfThis());
    assertTrue(fnType.getTypeOfThis().isSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE)));
  }

  // Tests inferInheritance with @constructor and @extends Object
  @Test
  public void testInferInheritance_constructorExtends_setsSuperType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(new JSTypeExpression(IR.string("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("SubCtor", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertTrue(fnType.isConstructor());
    ObjectType prototype = fnType.getPrototype();
    assertNotNull(prototype);
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferInheritance with @interface and @extends
  @Test
  public void testInferInheritance_interfaceExtends_setsExtendedInterfaces() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    docBuilder.recordExtendedInterface(new JSTypeExpression(IR.string("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("SubInterface", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertTrue(fnType.isInterface());
    assertEquals(1, fnType.getExtendedInterfacesCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferInheritance with @constructor and @implements
  @Test
  public void testInferInheritance_constructorImplements_setsImplementedInterfaces() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordImplementedInterface(new JSTypeExpression(IR.string("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("ImplCtor", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertTrue(fnType.isConstructor());
    assertEquals(1, fnType.getOwnImplementedInterfaces().size());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests inferParameterTypes with JSDoc @param matching AST params
  @Test
  public void testInferParameterTypes_withDocParamTypes_assignsCorrectTypes() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("x", new JSTypeExpression(IR.string("number"), "test.js"));
    docBuilder.recordParameter("y", new JSTypeExpression(IR.string("string"), "test.js"));
    JSDocInfo info = docBuilder.build(null);

    Node paramList = IR.paramList(IR.name("x"), IR.name("y"));

    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("typedParamsFn", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(paramList, info);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(2, fnType.getParametersCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests AstFunctionContents escaped qualified names recording
  @Test
  public void testAstFunctionContents_escapedQualifiedNames() {
    Node fnNode = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    FunctionTypeBuilder.AstFunctionContents contents =
        new FunctionTypeBuilder.AstFunctionContents(fnNode);

    assertFalse(contents.mayBeFromExterns());
    assertFalse(contents.getEscapedQualifiedNames().iterator().hasNext());

    contents.recordEscapedQualifiedName("a.b.c");
    assertTrue(contents.getEscapedQualifiedNames().iterator().hasNext());
    assertEquals("a.b.c", contents.getEscapedQualifiedNames().iterator().next());
  }

  // Tests UnknownFunctionContents escaped qualified names
  @Test
  public void testUnknownFunctionContents_escapedQualifiedNames_isEmpty() {
    FunctionTypeBuilder.FunctionContents contents =
        FunctionTypeBuilder.UnknownFunctionContents.get();

    assertFalse(contents.getEscapedQualifiedNames().iterator().hasNext());
  }
}
package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FunctionBuilderTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests building function type with default builder state
  @Test
  public void testBuild_defaultValues_createsFunctionTypeWithDefaults() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.build();

    assertNotNull(fn);
    assertNull(fn.getReferenceName());
    assertNull(fn.getSource());
    assertNull(fn.getParametersNode());
    assertNull(fn.getTemplateTypeName());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isNativeObjectType());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests setting function name
  @Test
  public void testWithName_validName_setsFunctionName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withName("myFunction");
    FunctionType fn = builder.build();

    assertEquals("myFunction", fn.getReferenceName());
  }

  // Tests setting function name with null
  @Test
  public void testWithName_nullName_setsNullFunctionName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withName("temp").withName(null);
    FunctionType fn = builder.build();

    assertNull(fn.getReferenceName());
  }

  // Tests setting source node
  @Test
  public void testWithSourceNode_validNode_setsSourceNode() {
    Node sourceNode = new Node(0);
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withSourceNode(sourceNode);
    FunctionType fn = builder.build();

    assertSame(sourceNode, fn.getSource());
  }

  // Tests setting parameters using a Node directly
  @Test
  public void testWithParamsNode_validNode_setsParametersNode() {
    Node paramsNode = new Node(0);
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withParamsNode(paramsNode);
    FunctionType fn = builder.build();

    assertSame(paramsNode, fn.getParametersNode());
  }

  // Tests setting parameters using FunctionParamBuilder
  @Test
  public void testWithParams_paramBuilder_setsParametersNode() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(stringType);

    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withParams(paramBuilder);
    FunctionType fn = builder.build();

    assertNotNull(fn.getParametersNode());
  }

  // Tests setting standard return type (not inferred)
  @Test
  public void testWithReturnType_validType_setsReturnTypeNotInferred() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withReturnType(numberType);
    FunctionType fn = builder.build();

    assertEquals(numberType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests setting inferred return type
  @Test
  public void testWithInferredReturnType_validType_setsReturnTypeAndInferredFlag() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withInferredReturnType(stringType);
    FunctionType fn = builder.build();

    assertEquals(stringType, fn.getReturnType());
    assertTrue(fn.isReturnTypeInferred());
  }

  // Tests setting type of 'this'
  @Test
  public void testWithTypeOfThis_validObjectType_setsTypeOfThis() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withTypeOfThis(objectType);
    FunctionType fn = builder.build();

    assertEquals(objectType, fn.getTypeOfThis());
  }

  // Tests setting template type name
  @Test
  public void testWithTemplateName_validTemplateName_setsTemplateTypeName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withTemplateName("T");
    FunctionType fn = builder.build();

    assertEquals("T", fn.getTemplateTypeName());
  }

  // Tests marking the function as constructor
  @Test
  public void testForConstructor_flagSet_isConstructorReturnsTrue() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.forConstructor();
    FunctionType fn = builder.build();

    assertTrue(fn.isConstructor());
  }

  // Tests marking the function as native type
  @Test
  public void testForNativeType_flagSet_isNativeObjectTypeReturnsTrue() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.forNativeType();
    FunctionType fn = builder.build();

    assertTrue(fn.isNativeObjectType());
  }

  // Tests copying properties from another function type
  @Test
  public void testCopyFromOtherFunction_allProperties_copiesCorrectly() {
    Node sourceNode = new Node(0);
    FunctionType original = new FunctionBuilder(registry)
        .withName("originalFn")
        .withSourceNode(sourceNode)
        .withReturnType(numberType)
        .withTypeOfThis(objectType)
        .withTemplateName("T")
        .forConstructor()
        .build();

    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.copyFromOtherFunction(original);
    FunctionType copy = builder.build();

    assertEquals("originalFn", copy.getReferenceName());
    assertSame(sourceNode, copy.getSource());
    assertEquals(numberType, copy.getReturnType());
    assertEquals(objectType, copy.getTypeOfThis());
    assertEquals("T", copy.getTemplateTypeName());
    assertTrue(copy.isConstructor());
  }

  // Tests building function with chained fluent setters
  @Test
  public void testBuild_chainedConfiguration_allAttributesSetCorrectly() {
    Node sourceNode = new Node(0);
    Node paramsNode = new Node(0);

    FunctionType fn = new FunctionBuilder(registry)
        .withName("completeFunction")
        .withSourceNode(sourceNode)
        .withParamsNode(paramsNode)
        .withReturnType(stringType)
        .withTypeOfThis(objectType)
        .withTemplateName("U")
        .forConstructor()
        .build();

    assertEquals("completeFunction", fn.getReferenceName());
    assertSame(sourceNode, fn.getSource());
    assertSame(paramsNode, fn.getParametersNode());
    assertEquals(stringType, fn.getReturnType());
    assertEquals(objectType, fn.getTypeOfThis());
    assertEquals("U", fn.getTemplateTypeName());
    assertTrue(fn.isConstructor());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests setting return type with explicit inferred flag
  @Test
  public void testWithReturnType_explicitInferredFlag_setsReturnTypeAndInferredFlagCorrectly() {
    FunctionBuilder builderInferred = new FunctionBuilder(registry);
    builderInferred.withReturnType(numberType, true);
    FunctionType fnInferred = builderInferred.build();

    assertEquals(numberType, fnInferred.getReturnType());
    assertTrue(fnInferred.isReturnTypeInferred());

    FunctionBuilder builderNotInferred = new FunctionBuilder(registry);
    builderNotInferred.withReturnType(stringType, false);
    FunctionType fnNotInferred = builderNotInferred.build();

    assertEquals(stringType, fnNotInferred.getReturnType());
    assertFalse(fnNotInferred.isReturnTypeInferred());
  }

  // Tests marking the function as an interface
  @Test
  public void testForInterface_flagSet_isInterfaceReturnsTrue() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.forInterface();
    FunctionType fn = builder.build();

    assertTrue(fn.isInterface());
    assertFalse(fn.isConstructor());
  }

  // Tests copying properties from another function type including interface, native type, and inferred return
  @Test
  public void testCopyFromOtherFunction_interfaceAndInferredReturn_copiesCorrectly() {
    Node paramsNode = new Node(0);
    FunctionType original = new FunctionBuilder(registry)
        .withParamsNode(paramsNode)
        .withInferredReturnType(stringType)
        .forInterface()
        .forNativeType()
        .build();

    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.copyFromOtherFunction(original);
    FunctionType copy = builder.build();

    assertSame(paramsNode, copy.getParametersNode());
    assertEquals(stringType, copy.getReturnType());
    assertTrue(copy.isReturnTypeInferred());
    assertTrue(copy.isInterface());
    assertTrue(copy.isNativeObjectType());
  }
}
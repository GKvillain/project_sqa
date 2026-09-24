package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

  // Tests building function with default builder state
  @Test
  public void testBuild_defaultState_returnsFunctionWithDefaults() {
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
  public void testWithName_validName_setsReferenceName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withName("testFunction").build();

    assertEquals("testFunction", fn.getReferenceName());
  }

  // Tests setting function name with null
  @Test
  public void testWithName_nullName_setsNullReferenceName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withName("initial").withName(null).build();

    assertNull(fn.getReferenceName());
  }

  // Tests setting source node
  @Test
  public void testWithSourceNode_validNode_setsSource() {
    Node node = new Node(0);
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withSourceNode(node).build();

    assertSame(node, fn.getSource());
  }

  // Tests setting parameters using FunctionParamBuilder
  @Test
  public void testWithParams_paramBuilder_setsParametersNode() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(numberType);

    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withParams(paramBuilder).build();

    assertNotNull(fn.getParametersNode());
    assertEquals(1, fn.getParametersNode().getChildCount());
  }

  // Tests setting parameters directly via Node
  @Test
  public void testWithParamsNode_directNode_setsParametersNode() {
    Node paramNode = new Node(0);
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withParamsNode(paramNode).build();

    assertSame(paramNode, fn.getParametersNode());
  }

  // Tests setting return type without inference
  @Test
  public void testWithReturnType_validType_setsReturnTypeNotInferred() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withReturnType(numberType).build();

    assertEquals(numberType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests setting return type with explicit inferred flag (true)
  @Test
  public void testWithReturnType_withInferredTrue_setsReturnTypeAndInferredFlag() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withReturnType(numberType, true).build();

    assertEquals(numberType, fn.getReturnType());
    assertTrue(fn.isReturnTypeInferred());
  }

  // Tests setting return type with explicit inferred flag (false)
  @Test
  public void testWithReturnType_withInferredFalse_setsReturnTypeAndNotInferredFlag() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withReturnType(numberType, false).build();

    assertEquals(numberType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests setting inferred return type
  @Test
  public void testWithInferredReturnType_validType_setsReturnTypeAndInferredFlag() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withInferredReturnType(stringType).build();

    assertEquals(stringType, fn.getReturnType());
    assertTrue(fn.isReturnTypeInferred());
  }

  // Tests setting type of this
  @Test
  public void testWithTypeOfThis_objectType_setsTypeOfThis() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withTypeOfThis(objectType).build();

    assertEquals(objectType, fn.getTypeOfThis());
  }

  // Tests setting template type name
  @Test
  public void testWithTemplateName_validName_setsTemplateTypeName() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.withTemplateName("T").build();

    assertEquals("T", fn.getTemplateTypeName());
  }

  // Tests constructor flag
  @Test
  public void testForConstructor_called_setsIsConstructorTrue() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.forConstructor().build();

    assertTrue(fn.isConstructor());
  }

  // Tests native type flag
  @Test
  public void testForNativeType_called_setsIsNativeObjectTypeTrue() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.forNativeType().build();

    assertTrue(fn.isNativeObjectType());
  }

  // Tests copying state from another FunctionType
  @Test
  public void testCopyFromOtherFunction_copiesAllAttributes() {
    Node sourceNode = new Node(0);
    Node paramsNode = new Node(0);

    FunctionType original = new FunctionBuilder(registry)
        .withName("originalFn")
        .withSourceNode(sourceNode)
        .withParamsNode(paramsNode)
        .withReturnType(numberType)
        .withTypeOfThis(objectType)
        .withTemplateName("T")
        .forConstructor()
        .forNativeType()
        .build();

    FunctionType copied = new FunctionBuilder(registry)
        .copyFromOtherFunction(original)
        .build();

    assertEquals("originalFn", copied.getReferenceName());
    assertSame(sourceNode, copied.getSource());
    assertSame(paramsNode, copied.getParametersNode());
    assertEquals(numberType, copied.getReturnType());
    assertEquals(objectType, copied.getTypeOfThis());
    assertEquals("T", copied.getTemplateTypeName());
    assertTrue(copied.isConstructor());
    assertTrue(copied.isNativeObjectType());
    assertFalse(copied.isReturnTypeInferred());
  }

  // Tests copying state from another FunctionType with inferred return type
  @Test
  public void testCopyFromOtherFunction_withInferredReturnType_copiesInferredFlag() {
    FunctionType original = new FunctionBuilder(registry)
        .withInferredReturnType(stringType)
        .build();

    FunctionType copied = new FunctionBuilder(registry)
        .copyFromOtherFunction(original)
        .build();

    assertEquals(stringType, copied.getReturnType());
    assertTrue(copied.isReturnTypeInferred());
  }

  // Tests fluent chaining with multiple configurations
  @Test
  public void testChainedConfiguration_allMethods_buildsFullyConfiguredFunction() {
    Node sourceNode = new Node(0);
    Node paramsNode = new Node(0);

    FunctionType fn = new FunctionBuilder(registry)
        .withName("chainTest")
        .withSourceNode(sourceNode)
        .withParamsNode(paramsNode)
        .withInferredReturnType(stringType)
        .withTypeOfThis(objectType)
        .withTemplateName("U")
        .forConstructor()
        .forNativeType()
        .build();

    assertEquals("chainTest", fn.getReferenceName());
    assertSame(sourceNode, fn.getSource());
    assertSame(paramsNode, fn.getParametersNode());
    assertEquals(stringType, fn.getReturnType());
    assertTrue(fn.isReturnTypeInferred());
    assertEquals(objectType, fn.getTypeOfThis());
    assertEquals("U", fn.getTemplateTypeName());
    assertTrue(fn.isConstructor());
    assertTrue(fn.isNativeObjectType());
  }
}
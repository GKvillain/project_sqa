package com.google.javascript.jscomp;

import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MethodCompilerPassTest {

  private Compiler compiler;
  private ConcreteMethodCompilerPass pass;

  private static class DummySignatureStore implements MethodCompilerPass.SignatureStore {
    final Map<String, List<Node>> signatures = new HashMap<String, List<Node>>();
    int resetCount = 0;

    @Override
    public void reset() {
      resetCount++;
      signatures.clear();
    }

    @Override
    public void addSignature(String functionName, Node functionNode, String sourceFile) {
      if (!signatures.containsKey(functionName)) {
        signatures.put(functionName, new ArrayList<Node>());
      }
      signatures.get(functionName).add(functionNode);
    }

    @Override
    public void removeSignature(String functionName) {
      signatures.remove(functionName);
    }
  }

  private static class DummyCallback implements Callback {
    int visitCount = 0;

    @Override
    public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
      return true;
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      visitCount++;
    }
  }

  private static class ConcreteMethodCompilerPass extends MethodCompilerPass {
    final DummySignatureStore store = new DummySignatureStore();
    final DummyCallback callback = new DummyCallback();

    ConcreteMethodCompilerPass(AbstractCompiler compiler) {
      super(compiler);
    }

    @Override
    Callback getActingCallback() {
      return callback;
    }

    @Override
    SignatureStore getSignatureStore() {
      return store;
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
    pass = new ConcreteMethodCompilerPass(compiler);
  }

  // Tests null externs input handling and signature store reset
  @Test
  public void testProcess_nullExterns_resetsStoreAndProcessesRoot() {
    Node root = compiler.parseTestCode("var x = 1;");
    pass.process(null, root);

    assertEquals(1, pass.store.resetCount);
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
    assertTrue(pass.methodDefinitions.isEmpty());
  }

  // Tests extern method defined as an assignment to a function
  @Test
  public void testProcess_externFunctionAssign_addsExternMethodWithSignature() {
    Node externs = compiler.parseTestCode("var a = {}; a.foo = function() {};");
    Node root = compiler.parseTestCode("a.foo();");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("foo"));
    assertFalse(pass.externMethodsWithoutSignatures.contains("foo"));
    assertTrue(pass.store.signatures.containsKey("foo"));
    assertTrue(pass.methodDefinitions.containsKey("foo"));
  }

  // Tests extern method without a function signature
  @Test
  public void testProcess_externNonFunctionAssign_marksExternMethodWithoutSignature() {
    Node externs = compiler.parseTestCode("var a = {}; a.foo = 1;");
    Node root = compiler.parseTestCode("a.foo();");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("foo"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("foo"));
    assertFalse(pass.store.signatures.containsKey("foo"));
    assertFalse(pass.methodDefinitions.containsKey("foo"));
  }

  // Tests extern object literal with function values
  @Test
  public void testProcess_externObjectLiteralFunction_addsSignature() {
    Node externs = compiler.parseTestCode("var a = { foo: function() {} };");
    Node root = compiler.parseTestCode("a.foo();");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("foo"));
    assertFalse(pass.externMethodsWithoutSignatures.contains("foo"));
    assertTrue(pass.store.signatures.containsKey("foo"));
  }

  // Tests extern object literal with non-function values
  @Test
  public void testProcess_externObjectLiteralNonFunction_marksWithoutSignature() {
    Node externs = compiler.parseTestCode("var a = { bar: 10 };");
    Node root = compiler.parseTestCode("a.bar = 20;");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("bar"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("bar"));
    assertFalse(pass.store.signatures.containsKey("bar"));
  }

  // Tests gathering static method assignment in JS code
  @Test
  public void testProcess_jsStaticMethodAssignment_gathersMethodDefinition() {
    Node root = compiler.parseTestCode("var Foo = {}; Foo.bar = function() {};");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.store.signatures.containsKey("bar"));
    assertFalse(pass.nonMethodProperties.contains("bar"));
  }

  // Tests gathering prototype method assignment in JS code
  @Test
  public void testProcess_jsPrototypeMethodAssignment_gathersMethodDefinition() {
    Node root = compiler.parseTestCode("function Foo() {} Foo.prototype.baz = function() {};");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("baz"));
    assertTrue(pass.store.signatures.containsKey("baz"));
    assertFalse(pass.nonMethodProperties.contains("baz"));
  }

  // Tests JS object literal with function and non-function properties
  @Test
  public void testProcess_jsObjectLiteral_categorizesMethodsAndNonMethods() {
    Node root = compiler.parseTestCode("var obj = { methodA: function() {}, propB: 123 };");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("methodA"));
    assertTrue(pass.nonMethodProperties.contains("propB"));
    assertFalse(pass.methodDefinitions.containsKey("propB"));
  }

  // Tests JS static method assigned to a declared function by name
  @Test
  public void testProcess_jsMethodAssignedFromVar_gathersMethodDefinition() {
    Node root = compiler.parseTestCode("function myFunc() {} var Foo = {}; Foo.bar = myFunc;");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.store.signatures.containsKey("bar"));
  }

  // Tests non-function property assignment recorded in nonMethodProperties
  @Test
  public void testProcess_jsNonFunctionPropertyAssignment_addedToNonMethodProperties() {
    Node root = compiler.parseTestCode("var Foo = {}; Foo.bar = 42;");

    pass.process(null, root);

    assertFalse(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.nonMethodProperties.contains("bar"));
  }

  // Tests undefined function name assignment throws IllegalStateException when not in IDE mode
  @Test(expected = IllegalStateException.class)
  public void testProcess_undefinedFunctionAssignment_throwsException() {
    Node root = compiler.parseTestCode("var Foo = {}; Foo.bar = undeclaredFunction;");

    pass.process(null, root);
  }

  // Tests that processing resets previous state across multiple runs
  @Test
  public void testProcess_consecutiveRuns_clearsPreviousData() {
    Node root1 = compiler.parseTestCode("var Foo = {}; Foo.bar = function() {};");
    pass.process(null, root1);
    assertEquals(1, pass.methodDefinitions.get("bar").size());

    Node root2 = compiler.parseTestCode("var Baz = {}; Baz.qux = function() {};");
    pass.process(null, root2);

    assertFalse(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.methodDefinitions.containsKey("qux"));
    assertEquals(2, pass.store.resetCount);
  }

  // Tests acting callback traversal occurs during process
  @Test
  public void testProcess_actingCallback_invokedDuringExecution() {
    Node root = compiler.parseTestCode("var x = 1; var y = 2;");

    pass.process(null, root);

    assertTrue(pass.callback.visitCount > 0);
  }

  // Tests extern prototype property declared without assignment expression
  @Test
  public void testProcess_externPrototypePropertyWithoutAssign_markedWithoutSignature() {
    Node externs = compiler.parseTestCode("Foo.prototype.bar;");
    Node root = compiler.parseTestCode("var x = 1;");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("bar"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("bar"));
  }

  // Tests that JS code signatures are ignored when extern already marks method without signature
  @Test
  public void testProcess_externMethodWithoutSignature_ignoresSubsequentJsSignatures() {
    Node externs = compiler.parseTestCode("Foo.prototype.bar;");
    Node root = compiler.parseTestCode("function Foo() {} Foo.prototype.bar = function() {};");

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("bar"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("bar"));
    assertFalse(pass.store.signatures.containsKey("bar"));
    assertFalse(pass.methodDefinitions.containsKey("bar"));
  }

  // Tests JS prototype method assigned from a variable holding a non-function value
  @Test
  public void testProcess_jsPrototypeMethodAssignedFromNonFunctionVar_addedToNonMethods() {
    Node root = compiler.parseTestCode("var notAFunc = 123; function Foo() {} Foo.prototype.bar = notAFunc;");

    pass.process(null, root);

    assertTrue(pass.nonMethodProperties.contains("bar"));
    assertFalse(pass.methodDefinitions.containsKey("bar"));
  }

  // Tests JS object literal property assigned to a declared function by name
  @Test
  public void testProcess_jsObjectLiteralFunctionAssignedFromVar_gathersMethodDefinition() {
    Node root = compiler.parseTestCode("function myFunc() {} var obj = { methodA: myFunc };");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("methodA"));
    assertTrue(pass.store.signatures.containsKey("methodA"));
  }

  // Tests JS object literal property assigned to a declared non-function variable
  @Test
  public void testProcess_jsObjectLiteralAssignedFromNonFunctionVar_addedToNonMethods() {
    Node root = compiler.parseTestCode("var num = 42; var obj = { propA: num };");

    pass.process(null, root);

    assertTrue(pass.nonMethodProperties.contains("propA"));
    assertFalse(pass.methodDefinitions.containsKey("propA"));
  }

  // Tests multiple definitions for the same method name accumulate correctly
  @Test
  public void testProcess_multipleMethodDefinitions_accumulatesAllSignatures() {
    Node root = compiler.parseTestCode(
        "function A() {} A.prototype.render = function() {};" +
        "function B() {} B.prototype.render = function() {};");

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("render"));
    assertEquals(2, pass.methodDefinitions.get("render").size());
    assertEquals(2, pass.store.signatures.get("render").size());
  }

  // Tests nested GETPROP assignment where target is not prototype or direct name
  @Test
  public void testProcess_nestedGetPropAssignment_categorizedAsNonMethod() {
    Node root = compiler.parseTestCode("var a = { b: {} }; a.b.c = function() {};");

    pass.process(null, root);

    assertTrue(pass.nonMethodProperties.contains("c"));
  }

  // Tests undefined function name assignment does not throw when running in IDE mode
  @Test
  public void testProcess_undefinedFunctionAssignmentInIdeMode_doesNotThrow() {
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    compiler.initOptions(options);

    Node root = compiler.parseTestCode("var Foo = {}; Foo.bar = undeclaredFunction;");

    pass.process(null, root);

    assertTrue(pass.nonMethodProperties.contains("bar"));
  }
}
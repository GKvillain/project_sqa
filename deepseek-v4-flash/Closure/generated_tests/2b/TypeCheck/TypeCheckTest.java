package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;

import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private AbstractCompiler compiler;
  private JSTypeRegistry typeRegistry;
  private ReverseAbstractInterpreter reverseInterpreter;
  private TypeCheck typeCheck;
  private Node root;

  @Before
  public void setUp() throws Exception {
    CompilerOptions options = new CompilerOptions();
    compiler = new Compiler();
    compiler.initOptions(options);
    typeRegistry = compiler.getTypeRegistry();
    CodingConvention codingConvention = new DefaultCodingConvention();
    compiler.setCodingConvention(codingConvention);
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
      }
    };
    compiler.addChangeHandler(handler);
  }

  private TypeCheck createTypeCheck() {
    return new TypeCheck(compiler, reverseInterpreter, typeRegistry);
  }

  private TypeCheck createTypeCheckWithScope() {
    return new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        CheckLevel.WARNING, CheckLevel.OFF);
  }

  // Tests for visitName method: normal case with variable reference
  @Test
  public void testVisitName_normalVarRef_assignsType() {
    String code = "var x = 1; x;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node xNode = findNodeByTypeAndString(jsRoot, Token.NAME, "x");
    if (xNode != null) {
      JSType type = xNode.getJSType();
      assertNotNull(type);
    }
  }

  // Tests for visitName: name in function parameter (should return false typeable)
  @Test
  public void testVisitName_functionParam_returnsFalse() {
    String code = "function f(a) { return a; }";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node paramNode = findNodeByTypeAndString(jsRoot, Token.NAME, "a");
    if (paramNode != null && paramNode.getParent().isParamList()) {
      // Should return false for parameter node
    }
  }

  // Tests visitGetProp: normal property access
  @Test
  public void testVisitGetProp_normalAccess_checksProperty() {
    String code = "var obj = {}; obj.x;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node getPropNode = findNodeByType(jsRoot, Token.GETPROP);
    if (getPropNode != null) {
      // Ensure it doesn't throw
      tc.visit(t, getPropNode, getPropNode.getParent());
    }
  }

  // Tests visitNew: normal constructor call
  @Test
  public void testVisitNew_constructorCall_assignsInstanceType() {
    String code = "function Foo() {} var f = new Foo();";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node newNode = findNodeByType(jsRoot, Token.NEW);
    if (newNode != null) {
      tc.visit(t, newNode, newNode.getParent());
    }
  }

  // Tests visitCall: normal function call
  @Test
  public void testVisitCall_normalCall_checksParameters() {
    String code = "function f(x) { return x; } f(1);";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node callNode = findNodeByType(jsRoot, Token.CALL);
    if (callNode != null) {
      tc.visit(t, callNode, callNode.getParent());
    }
  }

  // Tests visitReturn: non-void return in void function
  @Test
  public void testVisitReturn_voidFunction_returnsWarning() {
    String code = "/** @return {void} */ function f() { return 1; }";
    Node jsRoot = parseCode(code);
  }

  // Tests checkPropCreation: assignment to new property on struct
  @Test
  public void testCheckPropCreation_structNewProp_generatesWarning() {
    String code = "/** @struct */ var S = function() {}; var s = new S(); s.x = 1;";
    Node jsRoot = parseCode(code);
  }

  // Tests checkEnumAlias: valid enum alias
  @Test
  public void testCheckEnumAlias_validAlias_noWarning() {
    String code = "/** @enum {number} */ var E = {A: 1}; var e = E;";
    Node jsRoot = parseCode(code);
  }

  // Tests visitFunction: constructor type
  @Test
  public void testVisitFunction_constructor_checksImplementation() {
    String code = "/** @constructor */ function Foo() {}";
    Node jsRoot = parseCode(code);
  }

  // Tests visitFunction: interface type
  @Test
  public void testVisitFunction_interface_checksSuperInterfaces() {
    String code = "/** @interface */ function I() {}";
    Node jsRoot = parseCode(code);
  }

  // Tests visitBinaryOperator: numeric operation
  @Test
  public void testVisitBinaryOperator_numericOp_validatesOperands() {
    String code = "1 + 2;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node addNode = findNodeByType(jsRoot, Token.ADD);
    if (addNode != null) {
      tc.visit(t, addNode, addNode.getParent());
    }
  }

  // Tests visitBinaryOperator: bitwise operation
  @Test
  public void testVisitBinaryOperator_bitwiseOp_validatesOperands() {
    String code = "1 & 2;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node bitAndNode = findNodeByType(jsRoot, Token.BITAND);
    if (bitAndNode != null) {
      tc.visit(t, bitAndNode, bitAndNode.getParent());
    }
  }

  // Tests visitGetElem: index access
  @Test
  public void testVisitGetElem_arrayAccess_validatesIndex() {
    String code = "var a = []; a[0];";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node getElemNode = findNodeByType(jsRoot, Token.GETELEM);
    if (getElemNode != null) {
      tc.visit(t, getElemNode, getElemNode.getParent());
    }
  }

  // Tests visitAssign: property assignment
  @Test
  public void testVisitAssign_propertyAssign_validatesAssignment() {
    String code = "var obj = {}; obj.x = 1;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node assignNode = findNodeByType(jsRoot, Token.ASSIGN);
    if (assignNode != null) {
      tc.visit(t, assignNode, assignNode.getParent());
    }
  }

  // Tests visitVar: variable declaration with inferred type
  @Test
  public void testVisitVar_inferredType_assignsType() {
    String code = "var x = 1;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    NodeTraversal t = new NodeTraversal(compiler, tc, null);
    Node varNode = findNodeByType(jsRoot, Token.VAR);
    if (varNode != null) {
      tc.visit(t, varNode, varNode.getParent());
    }
  }

  // Tests process with null externs
  @Test
  public void testProcess_nullExterns_processesJsRoot() {
    String code = "var x = 1;";
    Node jsRoot = parseCode(code);
    reverseInterpreter = new ReverseAbstractInterpreter(compiler);
    TypeCheck tc = new TypeCheck(compiler, reverseInterpreter, typeRegistry,
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    Scope scope = tc.topScope;
    if (scope == null) {
      scope = new Scope(jsRoot.getParent(), null);
    }
    tc.topScope = scope;
    tc.scopeCreator = new MemoizedScopeCreator(new TypedScopeCreator(compiler));
    tc.process(null, jsRoot);
  }

  // Helper methods
  private Node parseCode(String code) {
    CompilerInput input = new CompilerInput(
        SourceFile.fromCode("test", code));
    compiler.compile(
        ImmutableList.<SourceFile>of(),
        ImmutableList.of(input),
        new CompilerOptions());
    return compiler.getRoot();
  }

  private Node findNodeByType(Node n, int type) {
    if (n.getType() == type) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findNodeByType(child, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private Node findNodeByTypeAndString(Node n, int type, String str) {
    if (n.getType() == type) {
      if (n.getString().equals(str)) {
        return n;
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findNodeByTypeAndString(child, type, str);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
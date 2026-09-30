package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.CodingConvention.Bind;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ClosureCodingConventionTest {

  private ClosureCodingConvention convention;

  @Before
  public void setUp() {
    convention = new ClosureCodingConvention();
  }

  // Tests extractClassNameIfRequire with valid string target
  @Test
  public void testExtractClassNameIfRequire_validTarget_returnsClassName() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "require"));
    Node target = Node.newString(Token.STRING, "foo.bar.Baz");
    Node call = new Node(Token.CALL, callee, target);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String result = convention.extractClassNameIfRequire(call, expr);
    assertEquals("foo.bar.Baz", result);
  }

  // Tests extractClassNameIfProvide with valid string target
  @Test
  public void testExtractClassNameIfProvide_validTarget_returnsClassName() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "provide"));
    Node target = Node.newString(Token.STRING, "foo.bar.Baz");
    Node call = new Node(Token.CALL, callee, target);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String result = convention.extractClassNameIfProvide(call, expr);
    assertEquals("foo.bar.Baz", result);
  }

  // Tests extractClassNameIfRequire when callee is not goog.require
  @Test
  public void testExtractClassNameIfRequire_unrelatedCall_returnsNull() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "other"),
        Node.newString(Token.STRING, "require"));
    Node target = Node.newString(Token.STRING, "foo.bar.Baz");
    Node call = new Node(Token.CALL, callee, target);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String result = convention.extractClassNameIfRequire(call, expr);
    assertNull(result);
  }

  // Tests extractClassNameIfRequire when parent is not an expression call
  @Test
  public void testExtractClassNameIfRequire_nonExprParent_returnsNull() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "require"));
    Node target = Node.newString(Token.STRING, "foo.bar.Baz");
    Node call = new Node(Token.CALL, callee, target);
    Node varNode = new Node(Token.VAR, call);

    String result = convention.extractClassNameIfRequire(call, varNode);
    assertNull(result);
  }

  // Tests extractClassNameIfProvide without arguments
  @Test
  public void testExtractClassNameIfProvide_noArgs_returnsNull() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "provide"));
    Node call = new Node(Token.CALL, callee);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String result = convention.extractClassNameIfProvide(call, expr);
    assertNull(result);
  }

  // Tests getClassesDefinedByCall with goog.inherits
  @Test
  public void testGetClassesDefinedByCall_googInherits_returnsRelationship() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "inherits"));
    Node subClass = Node.newString(Token.NAME, "Child");
    Node superClass = Node.newString(Token.NAME, "Parent");
    Node call = new Node(Token.CALL, callee, subClass, superClass);

    SubclassRelationship result = convention.getClassesDefinedByCall(call);
    assertNotNull(result);
    assertEquals(SubclassType.INHERITS, result.subclassType);
    assertEquals("Child", result.subclassName);
    assertEquals("Parent", result.superclassName);
  }

  // Tests getClassesDefinedByCall with goog$inherits collapsed name
  @Test
  public void testGetClassesDefinedByCall_googDollarInherits_returnsRelationship() {
    Node callee = Node.newString(Token.NAME, "goog$inherits");
    Node subClass = Node.newString(Token.NAME, "Child");
    Node superClass = Node.newString(Token.NAME, "Parent");
    Node call = new Node(Token.CALL, callee, subClass, superClass);

    SubclassRelationship result = convention.getClassesDefinedByCall(call);
    assertNotNull(result);
    assertEquals(SubclassType.INHERITS, result.subclassType);
    assertEquals("Child", result.subclassName);
    assertEquals("Parent", result.superclassName);
  }

  // Tests getClassesDefinedByCall with deprecated SubClass.inherits(SuperClass)
  @Test
  public void testGetClassesDefinedByCall_deprecatedInherits_returnsRelationship() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "Child"),
        Node.newString(Token.STRING, "inherits"));
    Node superClass = Node.newString(Token.NAME, "Parent");
    Node call = new Node(Token.CALL, callee, superClass);

    SubclassRelationship result = convention.getClassesDefinedByCall(call);
    assertNotNull(result);
    assertEquals(SubclassType.INHERITS, result.subclassType);
    assertEquals("Child", result.subclassName);
    assertEquals("Parent", result.superclassName);
  }

  // Tests getClassesDefinedByCall with goog.mixin on prototypes
  @Test
  public void testGetClassesDefinedByCall_googMixin_returnsRelationship() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "mixin"));
    Node subProto = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "Child"),
        Node.newString(Token.STRING, "prototype"));
    Node superProto = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "Parent"),
        Node.newString(Token.STRING, "prototype"));
    Node call = new Node(Token.CALL, callee, subProto, superProto);

    SubclassRelationship result = convention.getClassesDefinedByCall(call);
    assertNotNull(result);
    assertEquals(SubclassType.MIXIN, result.subclassType);
    assertEquals("Child", result.subclassName);
    assertEquals("Parent", result.superclassName);
  }

  // Tests getClassesDefinedByCall with invalid arguments count
  @Test
  public void testGetClassesDefinedByCall_invalidArgCount_returnsNull() {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "inherits"));
    Node call = new Node(Token.CALL, callee);

    SubclassRelationship result = convention.getClassesDefinedByCall(call);
    assertNull(result);
  }

  // Tests isSuperClassReference
  @Test
  public void testIsSuperClassReference_propertyName_returnsExpected() {
    assertTrue(convention.isSuperClassReference("superClass_"));
    assertFalse(convention.isSuperClassReference("superClass"));
    assertFalse(convention.isSuperClassReference("constructor"));
  }

  // Tests export function names and global object name
  @Test
  public void testGetExportAndGlobalNames_returnsClosureConstants() {
    assertEquals("goog.exportProperty", convention.getExportPropertyFunction());
    assertEquals("goog.exportSymbol", convention.getExportSymbolFunction());
    assertEquals("goog.abstractMethod", convention.getAbstractMethodName());
    assertEquals("goog.global", convention.getGlobalObject());
    assertFalse(convention.isOptionalParameter(new Node(Token.NAME, "param")));
    assertFalse(convention.isVarArgsParameter(new Node(Token.NAME, "param")));
    assertFalse(convention.isPrivate("privateMethod"));
  }

  // Tests identifyTypeDeclarationCall with goog.addDependency
  @Test
  public void testIdentifyTypeDeclarationCall_validAddDependency_returnsTypeList() {
    Node callee = Node.newString(Token.NAME, "goog.addDependency");
    Node file = Node.newString(Token.STRING, "foo.js");
    Node typeList = new Node(Token.ARRAYLIT,
        Node.newString(Token.STRING, "TypeA"),
        Node.newString(Token.STRING, "TypeB"));
    Node call = new Node(Token.CALL, callee, file, typeList);

    List<String> types = convention.identifyTypeDeclarationCall(call);
    assertNotNull(types);
    assertEquals(2, types.size());
    assertEquals("TypeA", types.get(0));
    assertEquals("TypeB", types.get(1));
  }

  // Tests identifyTypeDeclarationCall with non-dependency call
  @Test
  public void testIdentifyTypeDeclarationCall_otherCall_returnsNull() {
    Node callee = Node.newString(Token.NAME, "goog.other");
    Node call = new Node(Token.CALL, callee);

    List<String> types = convention.identifyTypeDeclarationCall(call);
    assertNull(types);
  }

  // Tests getSingletonGetterClassName with goog.addSingletonGetter
  @Test
  public void testGetSingletonGetterClassName_validCall_returnsClassName() {
    Node callee = Node.newString(Token.NAME, "goog.addSingletonGetter");
    Node arg = Node.newString(Token.NAME, "MySingleton");
    Node call = new Node(Token.CALL, callee, arg);

    String result = convention.getSingletonGetterClassName(call);
    assertEquals("MySingleton", result);
  }

  // Tests getSingletonGetterClassName with goog$addSingletonGetter collapsed name
  @Test
  public void testGetSingletonGetterClassName_collapsedCall_returnsClassName() {
    Node callee = Node.newString(Token.NAME, "goog$addSingletonGetter");
    Node arg = Node.newString(Token.NAME, "MySingleton");
    Node call = new Node(Token.CALL, callee, arg);

    String result = convention.getSingletonGetterClassName(call);
    assertEquals("MySingleton", result);
  }

  // Tests getSingletonGetterClassName with wrong argument count
  @Test
  public void testGetSingletonGetterClassName_wrongArgCount_returnsNull() {
    Node callee = Node.newString(Token.NAME, "goog.addSingletonGetter");
    Node call = new Node(Token.CALL, callee);

    String result = convention.getSingletonGetterClassName(call);
    assertNull(result);
  }

  // Tests isPropertyTestFunction
  @Test
  public void testIsPropertyTestFunction_knownAndUnknownFunctions_returnsExpected() {
    Node isDefCall = new Node(Token.CALL, Node.newString(Token.NAME, "goog.isDef"));
    assertTrue(convention.isPropertyTestFunction(isDefCall));

    Node isStringCall = new Node(Token.CALL, Node.newString(Token.NAME, "goog.isString"));
    assertTrue(convention.isPropertyTestFunction(isStringCall));

    Node otherCall = new Node(Token.CALL, Node.newString(Token.NAME, "goog.other"));
    assertFalse(convention.isPropertyTestFunction(otherCall));
  }

  // Tests describeFunctionBind for goog.bind and goog.partial
  @Test
  public void testDescribeFunctionBind_googBindAndPartial_returnsBindObject() {
    Node fn = Node.newString(Token.NAME, "myFn");
    Node thisValue = Node.newString(Token.NAME, "thisObj");
    Node arg = Node.newString(Token.NAME, "arg1");

    Node bindCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "goog.bind"),
        fn, thisValue, arg);
    Bind bind = convention.describeFunctionBind(bindCall);
    assertNotNull(bind);
    assertEquals(fn, bind.target);
    assertEquals(thisValue, bind.thisValue);
    assertEquals(arg, bind.parameters);

    Node partialCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "goog.partial"),
        fn.cloneNode(), arg.cloneNode());
    Bind partialBind = convention.describeFunctionBind(partialCall);
    assertNotNull(partialBind);
    assertNull(partialBind.thisValue);
  }

  // Tests describeFunctionBind for non-bind calls
  @Test
  public void testDescribeFunctionBind_nonBindCall_returnsNull() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo.bar"));
    assertNull(convention.describeFunctionBind(call));

    Node nonCall = new Node(Token.NAME, "nameNode");
    assertNull(convention.describeFunctionBind(nonCall));
  }

  // Tests getAssertionFunctions
  @Test
  public void testGetAssertionFunctions_returnsNonEmptyCollection() {
    Collection<AssertionFunctionSpec> assertions = convention.getAssertionFunctions();
    assertNotNull(assertions);
    assertFalse(assertions.isEmpty());
  }
}
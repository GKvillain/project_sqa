package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GlobalNamespaceTest {

  private GlobalNamespace createNamespace(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace createNamespaceWithExterns(String externsJs, String js) {
    Compiler compiler = new Compiler();
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests building global namespace for a simple global var declaration
  @Test
  public void testGetNameIndex_simpleVar_createsName() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    GlobalNamespace.Name aName = nameIndex.get("a");
    assertEquals("a", aName.fullName());
    assertTrue(aName.isSimpleName());
    assertEquals(1, aName.globalSets);
    assertEquals(0, aName.totalGets);
  }

  // Tests building hierarchical name forest for qualified property names
  @Test
  public void testGetNameForest_nestedProperties_createsTree() {
    GlobalNamespace gn = createNamespace("var a = {}; a.b = {}; a.b.c = 1;");
    List<GlobalNamespace.Name> forest = gn.getNameForest();
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertEquals(1, forest.size());
    assertEquals("a", forest.get(0).name);

    assertTrue(nameIndex.containsKey("a"));
    assertTrue(nameIndex.containsKey("a.b"));
    assertTrue(nameIndex.containsKey("a.b.c"));

    GlobalNamespace.Name abcName = nameIndex.get("a.b.c");
    assertEquals("a.b.c", abcName.fullName());
    assertFalse(abcName.isSimpleName());
    assertEquals("c", abcName.name);
    assertEquals("a.b", abcName.parent.fullName());
  }

  // Tests object literal key extraction in global namespace
  @Test
  public void testGetNameIndex_objectLiteral_createsProperties() {
    GlobalNamespace gn = createNamespace("var obj = { x: 10, y: { z: 20 } };");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("obj"));
    assertTrue(nameIndex.containsKey("obj.x"));
    assertTrue(nameIndex.containsKey("obj.y"));
    assertTrue(nameIndex.containsKey("obj.y.z"));
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameIndex.get("obj").type);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameIndex.get("obj.y").type);
  }

  // Tests global function declaration sets function type
  @Test
  public void testGetNameIndex_functionDeclaration_setsFunctionType() {
    GlobalNamespace gn = createNamespace("function foo() {} foo();");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("foo"));
    GlobalNamespace.Name fooName = nameIndex.get("foo");
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, fooName.type);
    assertEquals(1, fooName.globalSets);
    assertEquals(1, fooName.callGets);
    assertEquals(1, fooName.totalGets);
  }

  // Tests aliasing get reference detection
  @Test
  public void testGetNameIndex_aliasingGet_trackedCorrectly() {
    GlobalNamespace gn = createNamespace("var a = {}; var b = a;");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    GlobalNamespace.Name aName = nameIndex.get("a");
    assertEquals(1, aName.globalSets);
    assertEquals(1, aName.aliasingGets);
    assertEquals(1, aName.totalGets);
  }

  // Tests prototype prefix handling in property references
  @Test
  public void testGetNameIndex_prototypePrefix_handled() {
    GlobalNamespace gn = createNamespace("function Foo() {} Foo.prototype.bar = function() {};");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("Foo"));
    GlobalNamespace.Name fooName = nameIndex.get("Foo");
    assertEquals(1, fooName.totalGets);
  }

  // Tests names declared in externs
  @Test
  public void testGetNameIndex_withExterns_tracksExternNames() {
    GlobalNamespace gn = createNamespaceWithExterns("var extObj = {};", "extObj.prop = 1;");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("extObj"));
    assertTrue(nameIndex.containsKey("extObj.prop"));
    assertTrue(nameIndex.get("extObj").inExterns);
  }

  // Tests local set and local scope variable references
  @Test
  public void testGetNameIndex_localSet_trackedCorrectly() {
    GlobalNamespace gn = createNamespace("var a; function f() { a = 1; }");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    GlobalNamespace.Name aName = nameIndex.get("a");
    assertEquals(1, aName.globalSets);
    assertEquals(1, aName.localSets);
  }

  // Tests Name.canCollapse logic
  @Test
  public void testName_canCollapse_basicLogic() {
    GlobalNamespace.Name globalObj = new GlobalNamespace.Name("globalObj", null, false);
    globalObj.type = GlobalNamespace.Name.Type.OBJECTLIT;
    globalObj.globalSets = 1;

    assertTrue(globalObj.canCollapse());
    assertTrue(globalObj.canCollapseUnannotatedChildNames());

    GlobalNamespace.Name child = globalObj.addProperty("prop", false);
    child.globalSets = 1;
    assertTrue(child.canCollapse());

    GlobalNamespace.Name externObj = new GlobalNamespace.Name("ext", null, true);
    externObj.globalSets = 1;
    assertFalse(externObj.canCollapse());
  }

  // Tests Name.canEliminate condition
  @Test
  public void testName_canEliminate_noGets() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("foo", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    name.globalSets = 1;
    name.totalGets = 0;

    assertTrue(name.canEliminate());

    name.totalGets = 1;
    assertFalse(name.canEliminate());
  }

  // Tests Name.needsToBeStubbed when only local sets exist
  @Test
  public void testName_needsToBeStubbed_localSetsOnly() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("bar", null, false);
    name.globalSets = 0;
    name.localSets = 1;
    assertTrue(name.needsToBeStubbed());

    name.globalSets = 1;
    assertFalse(name.needsToBeStubbed());
  }

  // Tests Name.isNamespace when descendant is class or enum
  @Test
  public void testName_isNamespace_classOrEnumDescendant() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("ns", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;

    GlobalNamespace.Name child = parent.addProperty("ChildClass", false);
    child.setIsClassOrEnum();

    assertTrue(parent.isNamespace());
    assertFalse(child.isNamespace());
  }

  // Tests Name.removeRef updating counts and declaration reference
  @Test
  public void testName_removeRef_updatesCountsAndDecl() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("foo", null, false);
    GlobalNamespace.Ref declRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref getRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);

    name.addRef(declRef);
    name.addRef(getRef);

    assertEquals(1, name.globalSets);
    assertEquals(1, name.totalGets);
    assertEquals(declRef, name.declaration);

    name.removeRef(declRef);
    assertEquals(0, name.globalSets);
    assertNull(name.declaration);

    name.removeRef(getRef);
    assertEquals(0, name.totalGets);
  }

  // Tests Ref.markTwins linking two references
  @Test
  public void testRef_markTwins_setsTwinsBothWays() {
    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref aliasRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);

    GlobalNamespace.Ref.markTwins(setRef, aliasRef);

    assertEquals(aliasRef, setRef.getTwin());
    assertEquals(setRef, aliasRef.getTwin());
    assertTrue(setRef.isSet());
    assertFalse(aliasRef.isSet());
  }

  // Tests Ref.markTwins throwing exception for invalid combination
  @Test(expected = IllegalArgumentException.class)
  public void testRef_markTwins_invalidTypes_throwsException() {
    GlobalNamespace.Ref get1 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref get2 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);

    GlobalNamespace.Ref.markTwins(get1, get2);
  }

  // Tests Ref.cloneAndReclassify creating a new Ref with new type
  @Test
  public void testRef_cloneAndReclassify_createsNewRefWithType() {
    GlobalNamespace.Ref original = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref cloned = original.cloneAndReclassify(GlobalNamespace.Ref.Type.ALIASING_GET);

    assertEquals(GlobalNamespace.Ref.Type.ALIASING_GET, cloned.type);
  }

  // Tests Name.toString representation
  @Test
  public void testName_toString_containsExpectedInfo() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("a", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    name.globalSets = 1;
    name.totalGets = 2;

    String str = name.toString();
    assertTrue(str.contains("a (OBJECTLIT)"));
    assertTrue(str.contains("globalSets=1"));
    assertTrue(str.contains("totalGets=2"));
  }

  // Tests scanNewNodes on existing namespace
  @Test
  public void testScanNewNodes_updatesNamespace() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = {};");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertTrue(index.containsKey("a"));

    Node newCode = compiler.parseTestCode("a.b = 1;");
    Scope globalScope = new SyntacticScopeCreator(compiler).createScope(root, null);
    Set<Node> newNodes = Collections.singleton(newCode.getFirstChild().getFirstChild());

    gn.scanNewNodes(globalScope, newNodes);
    assertNotNull(gn.getNameIndex());
  }

  // Tests Name.canCollapse returning false when aliasing gets are present
  @Test
  public void testName_canCollapse_aliasingGets_returnsFalse() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("a", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    name.globalSets = 1;
    name.aliasingGets = 1;

    assertFalse(name.canCollapse());
    assertFalse(name.canCollapseUnannotatedChildNames());
  }

  // Tests Name.canCollapse returning false when globalSets != 1 or localSets > 0
  @Test
  public void testName_canCollapse_multipleSetsOrLocalSets_returnsFalse() {
    GlobalNamespace.Name multiGlobal = new GlobalNamespace.Name("a", null, false);
    multiGlobal.type = GlobalNamespace.Name.Type.OBJECTLIT;
    multiGlobal.globalSets = 2;
    assertFalse(multiGlobal.canCollapse());

    GlobalNamespace.Name localSet = new GlobalNamespace.Name("b", null, false);
    localSet.type = GlobalNamespace.Name.Type.OBJECTLIT;
    localSet.globalSets = 1;
    localSet.localSets = 1;
    assertFalse(localSet.canCollapse());
  }

  // Tests Name.getBaseName, getDeclaration, getRefs, and getFirstRef
  @Test
  public void testName_getters_returnExpectedValues() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("a", null, false);
    GlobalNamespace.Name child = parent.addProperty("b", false);

    assertEquals("a", parent.getBaseName());
    assertEquals("b", child.getBaseName());
    assertTrue(child.hasParent());
    assertFalse(parent.hasParent());

    GlobalNamespace.Ref declRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref getRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    parent.addRef(declRef);
    parent.addRef(getRef);

    assertEquals(declRef, parent.getDeclaration());
    assertEquals(declRef, parent.getFirstRef());
    assertEquals(2, parent.getRefs().size());
  }

  // Tests Name.hasSubnamespaces property tracking
  @Test
  public void testName_hasSubnamespaces_subnamespaceAdded() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("ns", null, false);
    assertFalse(parent.hasSubnamespaces());

    GlobalNamespace.Name sub = parent.addProperty("sub", false);
    sub.type = GlobalNamespace.Name.Type.OBJECTLIT;
    sub.globalSets = 1;

    assertTrue(parent.hasSubnamespaces());
    assertNotNull(parent.props);
    assertEquals(1, parent.props.size());
  }

  // Tests JSDoc @nocollapse prevents collapsing
  @Test
  public void testGetNameIndex_nocollapseAnnotation_preventsCollapsing() {
    GlobalNamespace gn = createNamespace("/** @nocollapse */ var a = {};");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    GlobalNamespace.Name aName = nameIndex.get("a");
    assertTrue(aName.isCollapsingExplicitlyPrevented());
    assertFalse(aName.canCollapse());
  }

  // Tests constructor function marked as class/enum
  @Test
  public void testGetNameIndex_constructorFunction_detectedAsClass() {
    GlobalNamespace gn = createNamespace("/** @constructor */ function Foo() {}");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("Foo"));
    GlobalNamespace.Name fooName = nameIndex.get("Foo");
    assertTrue(fooName.isClassOrEnum);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, fooName.type);
  }

  // Tests enum declaration marked as class/enum
  @Test
  public void testGetNameIndex_enumDeclaration_detectedAsEnum() {
    GlobalNamespace gn = createNamespace("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("MyEnum"));
    GlobalNamespace.Name enumName = nameIndex.get("MyEnum");
    assertTrue(enumName.isClassOrEnum);
    assertEquals(GlobalNamespace.Name.Type.ENUM, enumName.type);
  }

  // Tests Ref helper methods and properties
  @Test
  public void testRef_typeCheckMethods() {
    GlobalNamespace.Ref setGlobal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref setLocal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref protoGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.PROTOTYPE_GET);
    GlobalNamespace.Ref callGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);

    assertTrue(setGlobal.isSet());
    assertTrue(setLocal.isSet());
    assertFalse(protoGet.isSet());
    assertFalse(callGet.isSet());
    assertFalse(directGet.isSet());

    assertNull(setGlobal.getTwin());
  }

  // Tests nested property assignments under function scopes
  @Test
  public void testGetNameIndex_propertyAssignedInFunctionScope() {
    GlobalNamespace gn = createNamespace("var a = {}; function init() { a.b = 10; }");
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    assertTrue(nameIndex.containsKey("a.b"));
    GlobalNamespace.Name abName = nameIndex.get("a.b");
    assertEquals(0, abName.globalSets);
    assertEquals(1, abName.localSets);
  }
}
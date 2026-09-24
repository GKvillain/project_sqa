package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GlobalNamespaceTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private GlobalNamespace createGlobalNamespace(String js) {
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace createGlobalNamespace(String externsJs, String js) {
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests global variable declaration and global set reference counting
  @Test
  public void testGetNameIndex_simpleVar_createsNameWithGlobalSet() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1;");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("a"));
    GlobalNamespace.Name a = names.get("a");
    assertEquals("a", a.fullName());
    assertTrue(a.isSimpleName());
    assertEquals(1, a.globalSets);
    assertEquals(0, a.localSets);
    assertEquals(0, a.totalGets);
    assertEquals(GlobalNamespace.Name.Type.OTHER, a.type);
  }

  // Tests function declaration type resolution
  @Test
  public void testGetNameIndex_functionDeclaration_createsFunctionType() {
    GlobalNamespace gn = createGlobalNamespace("function foo() {}");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("foo"));
    GlobalNamespace.Name foo = names.get("foo");
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, foo.type);
    assertEquals(1, foo.globalSets);
    assertTrue(foo.canCollapseUnannotatedChildNames());
  }

  // Tests object literal properties creation and hierarchy
  @Test
  public void testGetNameIndex_objectLiteral_createsHierarchyAndProperties() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: 1, c: function() {}};");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("a"));
    assertTrue(names.containsKey("a.b"));
    assertTrue(names.containsKey("a.c"));

    GlobalNamespace.Name a = names.get("a");
    GlobalNamespace.Name ab = names.get("a.b");
    GlobalNamespace.Name ac = names.get("a.c");

    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, a.type);
    assertEquals(GlobalNamespace.Name.Type.OTHER, ab.type);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, ac.type);

    assertEquals(a, ab.parent);
    assertFalse(ab.isSimpleName());
    assertEquals("a.b", ab.fullName());
  }

  // Tests nested assignment twin references
  @Test
  public void testGetNameIndex_nestedAssign_createsTwinRefs() {
    GlobalNamespace gn = createGlobalNamespace("var a = b = 1;");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("b"));
    GlobalNamespace.Name b = names.get("b");
    assertEquals(1, b.globalSets);
    assertEquals(1, b.aliasingGets);
    assertNotNull(b.declaration);
    assertNotNull(b.declaration.getTwin());
  }

  // Tests property assignments and hierarchy tree in name forest
  @Test
  public void testGetNameForest_nestedProperties_rootsOnlyInForest() {
    GlobalNamespace gn = createGlobalNamespace("var a = {}; a.b = {}; a.b.c = 1;");
    List<GlobalNamespace.Name> forest = gn.getNameForest();

    assertEquals(1, forest.size());
    GlobalNamespace.Name a = forest.get(0);
    assertEquals("a", a.name);
    assertNotNull(a.props);
    assertEquals(1, a.props.size());

    GlobalNamespace.Name b = a.props.get(0);
    assertEquals("b", b.name);
    assertNotNull(b.props);
    assertEquals(1, b.props.size());

    GlobalNamespace.Name c = b.props.get(0);
    assertEquals("c", c.name);
  }

  // Tests local set and read references inside functions
  @Test
  public void testGetNameIndex_localSetAndGet_recordsLocalSetsAndGets() {
    GlobalNamespace gn = createGlobalNamespace(
        "var a = 1; function f() { a = 2; var b = a; }");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    GlobalNamespace.Name a = names.get("a");
    assertEquals(1, a.globalSets);
    assertEquals(1, a.localSets);
    assertEquals(1, a.totalGets);
    assertFalse(a.canCollapseUnannotatedChildNames());
  }

  // Tests extern declarations handling and filtering
  @Test
  public void testGetNameIndex_withExterns_marksInExterns() {
    GlobalNamespace gn = createGlobalNamespace("var extObj;", "extObj.prop = 1;");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("extObj"));
    GlobalNamespace.Name extObj = names.get("extObj");
    assertTrue(extObj.inExterns);
    assertFalse(extObj.canCollapse());
  }

  // Tests prototype reference handling
  @Test
  public void testGetNameIndex_prototypeProperty_handlesPrototypePrefix() {
    GlobalNamespace gn = createGlobalNamespace(
        "function MyClass() {} MyClass.prototype.foo = function() {};");
    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();

    assertTrue(names.containsKey("MyClass"));
    GlobalNamespace.Name myClass = names.get("MyClass");
    assertEquals(1, myClass.totalGets);
  }

  // Tests canEliminate logic when no gets exist
  @Test
  public void testName_canEliminate_noGets_returnsTrue() {
    GlobalNamespace gn = createGlobalNamespace("var a = { b: 1 };");
    GlobalNamespace.Name a = gn.getNameIndex().get("a");

    assertTrue(a.canEliminate());
  }

  // Tests canEliminate logic when gets exist
  @Test
  public void testName_canEliminate_withGets_returnsFalse() {
    GlobalNamespace gn = createGlobalNamespace("var a = { b: 1 }; var x = a;");
    GlobalNamespace.Name a = gn.getNameIndex().get("a");

    assertFalse(a.canEliminate());
  }

  // Tests needsToBeStubbed condition
  @Test
  public void testName_needsToBeStubbed_localSetsOnly_returnsTrue() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("foo", null, false);
    name.localSets = 1;
    name.globalSets = 0;

    assertTrue(name.needsToBeStubbed());
  }

  // Tests removeRef updates reference counters correctly
  @Test
  public void testName_removeRef_updatesCounters() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("a", null, false);
    GlobalNamespace.Ref declRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref getRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);

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

  // Tests isNamespace and setIsClassOrEnum cascading to ancestors
  @Test
  public void testName_setIsClassOrEnum_setsAncestorsNamespace() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("a", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    GlobalNamespace.Name child = parent.addProperty("b", false);

    assertFalse(parent.isNamespace());
    child.setIsClassOrEnum();
    assertTrue(parent.isNamespace());
  }

  // Tests Ref helper methods isSet and cloneAndReclassify
  @Test
  public void testRef_isSetAndCloneAndReclassify() {
    GlobalNamespace.Ref globalSet = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    assertTrue(globalSet.isSet());

    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    assertFalse(directGet.isSet());

    GlobalNamespace.Ref cloned = directGet.cloneAndReclassify(
        GlobalNamespace.Ref.Type.ALIASING_GET);
    assertEquals(GlobalNamespace.Ref.Type.ALIASING_GET, cloned.type);
  }

  // Tests markTwins validation check
  @Test(expected = IllegalArgumentException.class)
  public void testRef_markTwins_invalidPair_throwsException() {
    GlobalNamespace.Ref directGet1 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref directGet2 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref.markTwins(directGet1, directGet2);
  }

  // Tests scanNewNodes updates references for newly added AST nodes
  @Test
  public void testScanNewNodes_scansProvidedNodes() {
    Node root = compiler.parseTestCode("var a = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameForest();

    Node newVarNode = compiler.parseTestCode("a;").getFirstChild();
    Scope globalScope = new SyntacticScopeCreator(compiler).createScope(root, null);
    gn.scanNewNodes(globalScope, Collections.singleton(newVarNode.getFirstChild()));

    GlobalNamespace.Name a = gn.getNameIndex().get("a");
    assertEquals(1, a.totalGets);
  }
}
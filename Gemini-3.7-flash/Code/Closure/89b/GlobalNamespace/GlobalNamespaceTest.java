package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

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

  private GlobalNamespace createGlobalNamespace(String externs, String js) {
    Node externsRoot = compiler.parseTestCode(externs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests global variable declaration and Name property inspection
  @Test
  public void testGetNameIndex_globalVarDeclaration_createsNameWithCorrectCounts() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1;");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("a"));
    GlobalNamespace.Name nameA = index.get("a");
    assertEquals("a", nameA.name);
    assertEquals("a", nameA.fullName());
    assertTrue(nameA.isSimpleName());
    assertEquals(1, nameA.globalSets);
    assertEquals(0, nameA.localSets);
    assertEquals(0, nameA.totalGets);
    assertEquals(GlobalNamespace.Name.Type.OTHER, nameA.type);
    assertNotNull(nameA.declaration);
    assertTrue(nameA.declaration.isSet());
  }

  // Tests object literal hierarchy parsing and properties linkage
  @Test
  public void testGetNameIndex_nestedObjectLiteral_createsHierarchy() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: {c: 10}};");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("a"));
    assertTrue(index.containsKey("a.b"));
    assertTrue(index.containsKey("a.b.c"));

    GlobalNamespace.Name nameA = index.get("a");
    GlobalNamespace.Name nameB = index.get("a.b");
    GlobalNamespace.Name nameC = index.get("a.b.c");

    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameA.type);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameB.type);
    assertEquals(GlobalNamespace.Name.Type.OTHER, nameC.type);
    assertEquals(nameA, nameB.parent);
    assertEquals(nameB, nameC.parent);
    assertFalse(nameB.isSimpleName());
  }

  // Tests global function declaration
  @Test
  public void testGetNameIndex_functionDeclaration_createsFunctionName() {
    GlobalNamespace gn = createGlobalNamespace("function foo() {}");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("foo"));
    GlobalNamespace.Name foo = index.get("foo");
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, foo.type);
    assertEquals(1, foo.globalSets);
  }

  // Tests assignment from local scope to global property
  @Test
  public void testGetNameIndex_localSet_incrementsLocalSets() {
    GlobalNamespace gn = createGlobalNamespace("var a = {}; function f() { a.b = 1; }");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("a.b"));
    GlobalNamespace.Name nameAB = index.get("a.b");
    assertEquals(0, nameAB.globalSets);
    assertEquals(1, nameAB.localSets);
    assertTrue(nameAB.needsToBeStubbed());
  }

  // Tests various get references: direct get, aliasing get, call get
  @Test
  public void testGetNameIndex_variousGets_recordedCorrectly() {
    String js = "var a = function() {}; var alias = a; a(); if (a) {}";
    GlobalNamespace gn = createGlobalNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name nameA = index.get("a");
    assertNotNull(nameA);
    assertEquals(1, nameA.globalSets);
    assertEquals(1, nameA.aliasingGets);
    assertEquals(1, nameA.callGets);
    assertEquals(3, nameA.totalGets);
  }

  // Tests prototype reference prefix handling
  @Test
  public void testGetNameIndex_prototypeProperty_handledAsPrototypeGet() {
    String js = "function Foo() {} Foo.prototype.bar = function() {}; Foo.prototype.bar.baz = 1;";
    GlobalNamespace gn = createGlobalNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("Foo"));
    GlobalNamespace.Name foo = index.get("Foo");
    assertTrue(foo.totalGets > 0);
  }

  // Tests canCollapse and canEliminate predicates on unaliased object literal
  @Test
  public void testCanCollapseAndCanEliminate_unaliasedObject_returnsTrue() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: 1};");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name nameA = index.get("a");
    GlobalNamespace.Name nameB = index.get("a.b");

    assertTrue(nameA.canCollapseUnannotatedChildNames());
    assertTrue(nameB.canCollapse());
    assertTrue(nameA.canEliminate());
  }

  // Tests that aliasing prevents collapsing of child properties
  @Test
  public void testCanCollapse_aliasedObject_returnsFalse() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: 1}; var alias = a;");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name nameA = index.get("a");
    assertTrue(nameA.shouldKeepKeys());
    assertFalse(nameA.canCollapseUnannotatedChildNames());
  }

  // Tests getNameForest method returns top-level roots
  @Test
  public void testGetNameForest_returnsOnlyTopLevelRoots() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1; var b = 2; a.c = 3;");
    List<GlobalNamespace.Name> forest = gn.getNameForest();

    assertEquals(2, forest.size());
    assertTrue(forest.get(0).isSimpleName());
    assertTrue(forest.get(1).isSimpleName());
  }

  // Tests JSDoc @constructor annotation marking class/enum
  @Test
  public void testIsClassOrEnum_withConstructorAnnotation_setsFlag() {
    String js = "var ns = {}; /** @constructor */ ns.MyClass = function() {};";
    GlobalNamespace gn = createGlobalNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name ns = index.get("ns");
    GlobalNamespace.Name myClass = index.get("ns.MyClass");

    assertNotNull(myClass);
    assertTrue(myClass.canCollapse());
    assertTrue(ns.isNamespace());
  }

  // Tests conditional HOOK and OR expressions for get reference determination
  @Test
  public void testHookAndOrExpressions_selfAssignment_isDirectGet() {
    String js = "var a = a || {}; var b = b ? b : {};";
    GlobalNamespace gn = createGlobalNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name nameA = index.get("a");
    GlobalNamespace.Name nameB = index.get("b");

    assertNotNull(nameA);
    assertNotNull(nameB);
    assertEquals(0, nameA.aliasingGets);
    assertEquals(0, nameB.aliasingGets);
  }

  // Tests externs root handling and externs flag on Name
  @Test
  public void testExterns_declaredInExterns_markedInExterns() {
    String externs = "var extObj = {};";
    String js = "extObj.prop = 1;";
    GlobalNamespace gn = createGlobalNamespace(externs, js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name extObj = index.get("extObj");
    assertNotNull(extObj);
    assertTrue(extObj.inExterns);
    assertFalse(extObj.canCollapse());
  }

  // Tests Ref twin marking and twin retrieval
  @Test
  public void testRef_markTwins_andGetTwin() {
    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref aliasRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.ALIASING_GET);

    GlobalNamespace.Ref.markTwins(setRef, aliasRef);

    assertEquals(aliasRef, setRef.getTwin());
    assertEquals(setRef, aliasRef.getTwin());
  }

  // Tests Ref twin marking with invalid types throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testRef_markTwins_invalidTypes_throwsException() {
    GlobalNamespace.Ref ref1 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref ref2 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.CALL_GET);

    GlobalNamespace.Ref.markTwins(ref1, ref2);
  }

  // Tests Name addRef and removeRef updating reference counters
  @Test
  public void testName_addRefAndRemoveRef_updatesCounts() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("x", null, false);
    GlobalNamespace.Ref decl = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref localSet = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);

    name.addRef(decl);
    name.addRef(localSet);
    name.addRef(directGet);

    assertEquals(1, name.globalSets);
    assertEquals(1, name.localSets);
    assertEquals(1, name.totalGets);
    assertEquals(decl, name.declaration);

    name.removeRef(localSet);
    assertEquals(0, name.localSets);

    name.removeRef(directGet);
    assertEquals(0, name.totalGets);

    name.removeRef(decl);
    assertEquals(0, name.globalSets);
    assertNull(name.declaration);
  }

  // Tests Ref cloneAndReclassify creates ref of specified type
  @Test
  public void testRef_cloneAndReclassify_changesType() {
    GlobalNamespace.Ref original = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref cloned = original.cloneAndReclassify(
        GlobalNamespace.Ref.Type.ALIASING_GET);

    assertEquals(GlobalNamespace.Ref.Type.ALIASING_GET, cloned.type);
  }

  // Tests scanNewNodes updates references for newly added AST nodes
  @Test
  public void testScanNewNodes_scansReferences() {
    String js = "var a = 1;";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameIndex();

    Scope scope = Scope.createGlobalScope(root);
    Node newNode = compiler.parseTestCode("a = 2;").getFirstChild();

    gn.scanNewNodes(scope, Sets.newHashSet(newNode));
    GlobalNamespace.Name nameA = gn.getNameIndex().get("a");
    assertEquals(2, nameA.globalSets);
  }

  // Tests Name toString format
  @Test
  public void testName_toString_containsRelevantInfo() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("foo", null, false);
    String str = name.toString();
    assertTrue(str.contains("foo"));
    assertTrue(str.contains("globalSets="));
  }
}
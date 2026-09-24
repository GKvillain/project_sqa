package com.google.javascript.jscomp;

import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.GlobalNamespace.Ref;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GlobalNamespace}.
 */
public class GlobalNamespaceTest {

  private GlobalNamespace createGlobalNamespace(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace createGlobalNamespace(String externs, String js) {
    Compiler compiler = new Compiler();
    Node externsRoot = compiler.parseTestCode(externs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests simple variable declaration and name indexing
  @Test
  public void testGetNameIndex_simpleVarDeclaration_recordsGlobalSet() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1;");
    Map<String, Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    Name a = nameIndex.get("a");
    assertNotNull(a);
    assertEquals("a", a.name);
    assertEquals("a", a.fullName());
    assertTrue(a.isSimpleName());
    assertEquals(1, a.globalSets);
    assertEquals(0, a.localSets);
    assertEquals(0, a.totalGets);
    assertEquals(0, a.aliasingGets);
  }

  // Tests object literal keys and hierarchical name creation
  @Test
  public void testGetNameIndex_objectLiteralHierarchy_createsNestedNames() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: {c: 10}};");
    Map<String, Name> nameIndex = gn.getNameIndex();

    assertTrue(nameIndex.containsKey("a"));
    assertTrue(nameIndex.containsKey("a.b"));
    assertTrue(nameIndex.containsKey("a.b.c"));

    Name a = nameIndex.get("a");
    Name ab = nameIndex.get("a.b");
    Name abc = nameIndex.get("a.b.c");

    assertEquals(Name.Type.OBJECTLIT, a.type);
    assertEquals(Name.Type.OBJECTLIT, ab.type);
    assertEquals(Name.Type.OTHER, abc.type);

    assertNull(a.parent);
    assertEquals(a, ab.parent);
    assertEquals(ab, abc.parent);
    assertFalse(ab.isSimpleName());
  }

  // Tests function declarations and call get references
  @Test
  public void testGetNameIndex_functionDeclarationAndCall_recordsCallGet() {
    GlobalNamespace gn = createGlobalNamespace("function foo() {} foo();");
    Map<String, Name> nameIndex = gn.getNameIndex();

    Name foo = nameIndex.get("foo");
    assertNotNull(foo);
    assertEquals(Name.Type.FUNCTION, foo.type);
    assertEquals(1, foo.globalSets);
    assertEquals(1, foo.callGets);
    assertEquals(1, foo.totalGets);
  }

  // Tests assignment in local scope increments localSets
  @Test
  public void testGetNameIndex_localScopeAssignment_incrementsLocalSets() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1; function f() { a = 2; }");
    Map<String, Name> nameIndex = gn.getNameIndex();

    Name a = nameIndex.get("a");
    assertNotNull(a);
    assertEquals(1, a.globalSets);
    assertEquals(1, a.localSets);
    assertTrue(a.canCollapse());
  }

  // Tests nested assignment creating twin aliasing get and set
  @Test
  public void testGetNameIndex_nestedAssignment_createsTwinRef() {
    GlobalNamespace gn = createGlobalNamespace("var a = 1; var b = (a = 2);");
    Map<String, Name> nameIndex = gn.getNameIndex();

    Name a = nameIndex.get("a");
    assertNotNull(a);
    assertEquals(2, a.globalSets);
    assertEquals(1, a.aliasingGets);
    assertNotNull(a.declaration);
  }

  // Tests prototype property assignment
  @Test
  public void testGetNameIndex_prototypeAssignment_recordsPrototypeGet() {
    GlobalNamespace gn = createGlobalNamespace("function Foo() {} Foo.prototype.bar = 1;");
    Map<String, Name> nameIndex = gn.getNameIndex();

    Name foo = nameIndex.get("Foo");
    assertNotNull(foo);
    assertTrue(foo.totalGets > 0);
  }

  // Tests externs root handling and inExterns flag
  @Test
  public void testGetNameIndex_externs_marksInExterns() {
    GlobalNamespace gn = createGlobalNamespace("var ext = {};", "ext.prop = 1;");
    Map<String, Name> nameIndex = gn.getNameIndex();

    Name ext = nameIndex.get("ext");
    assertNotNull(ext);
    assertTrue(ext.inExterns);
    assertFalse(ext.canCollapse());
  }

  // Tests getNameForest returns only top-level roots
  @Test
  public void testGetNameForest_multipleRoots_returnsOnlyTopLevel() {
    GlobalNamespace gn = createGlobalNamespace("var a = {b: 1}; var c = 2;");
    List<Name> forest = gn.getNameForest();

    assertEquals(2, forest.size());
    Set<String> rootNames = new HashSet<String>();
    for (Name n : forest) {
      rootNames.add(n.name);
    }
    assertTrue(rootNames.contains("a"));
    assertTrue(rootNames.contains("c"));
    assertFalse(rootNames.contains("b"));
  }

  // Tests Ref twins marking and verification
  @Test
  public void testRef_markTwins_linksBothRefs() {
    Ref setRef = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    Ref getRef = Ref.createRefForTesting(Ref.Type.ALIASING_GET);

    assertTrue(setRef.isSet());
    assertFalse(getRef.isSet());

    Ref.markTwins(setRef, getRef);
    assertEquals(getRef, setRef.getTwin());
    assertEquals(setRef, getRef.getTwin());
  }

  // Tests Ref cloneAndReclassify
  @Test
  public void testRef_cloneAndReclassify_changesType() {
    Ref orig = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
    Ref cloned = orig.cloneAndReclassify(Ref.Type.CALL_GET);

    assertEquals(Ref.Type.CALL_GET, cloned.type);
    assertEquals(orig.sourceName, cloned.sourceName);
  }

  // Tests Name removeRef and count updates
  @Test
  public void testName_removeRef_decrementsCounts() {
    Name name = new Name("test", null, false);
    Ref setRef = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    Ref getRef = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
    Ref aliasRef = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
    Ref callRef = Ref.createRefForTesting(Ref.Type.CALL_GET);
    Ref localSetRef = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);

    name.addRef(setRef);
    name.addRef(getRef);
    name.addRef(aliasRef);
    name.addRef(callRef);
    name.addRef(localSetRef);

    assertEquals(1, name.globalSets);
    assertEquals(1, name.localSets);
    assertEquals(3, name.totalGets);
    assertEquals(1, name.aliasingGets);
    assertEquals(1, name.callGets);

    name.removeRef(getRef);
    assertEquals(2, name.totalGets);

    name.removeRef(aliasRef);
    assertEquals(1, name.totalGets);
    assertEquals(0, name.aliasingGets);

    name.removeRef(callRef);
    assertEquals(0, name.totalGets);
    assertEquals(0, name.callGets);

    name.removeRef(localSetRef);
    assertEquals(0, name.localSets);

    name.removeRef(setRef);
    assertEquals(0, name.globalSets);
    assertNull(name.declaration);
  }

  // Tests Name class/enum flag propagation to ancestors
  @Test
  public void testName_setIsClassOrEnum_updatesAncestors() {
    Name parent = new Name("ns", null, false);
    parent.type = Name.Type.OBJECTLIT;
    Name child = parent.addProperty("SubClass", false);

    assertFalse(parent.isNamespace());
    child.setIsClassOrEnum();
    assertTrue(parent.isNamespace());
  }

  // Tests needsToBeStubbed logic
  @Test
  public void testName_needsToBeStubbed_trueWhenOnlyLocalSets() {
    Name name = new Name("x", null, false);
    assertFalse(name.needsToBeStubbed());

    Ref localSet = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);
    name.addRef(localSet);
    assertTrue(name.needsToBeStubbed());

    Ref globalSet = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    name.addRef(globalSet);
    assertFalse(name.needsToBeStubbed());
  }

  // Tests canEliminate logic on names with and without gets
  @Test
  public void testName_canEliminate_falseWhenHasGets() {
    Name name = new Name("obj", null, false);
    name.type = Name.Type.OBJECTLIT;
    name.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertTrue(name.canEliminate());

    name.addRef(Ref.createRefForTesting(Ref.Type.DIRECT_GET));
    assertFalse(name.canEliminate());
  }

  // Tests toString output of Name
  @Test
  public void testName_toString_containsRelevantInfo() {
    Name name = new Name("a", null, false);
    name.type = Name.Type.FUNCTION;
    String str = name.toString();
    assertTrue(str.contains("a"));
    assertTrue(str.contains("FUNCTION"));
  }

  // Tests scanNewNodes on dynamically added AST nodes
  @Test
  public void testScanNewNodes_addsNewReferences() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameIndex();

    Node newNode = compiler.parseTestCode("a = 2;").getFirstChild();
    Set<Node> newNodes = Collections.singleton(newNode);

    Scope globalScope = new SyntacticScopeCreator(compiler).createScope(root, null);
    gn.scanNewNodes(globalScope, newNodes);

    Name a = gn.getNameIndex().get("a");
    assertNotNull(a);
    assertTrue(a.globalSets >= 1);
  }
}
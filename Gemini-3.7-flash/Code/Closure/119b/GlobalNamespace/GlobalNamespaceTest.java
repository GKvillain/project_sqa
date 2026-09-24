package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
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

  private GlobalNamespace createNamespace(String js) {
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace createNamespace(String externs, String js) {
    Node externsRoot = compiler.parseTestCode(externs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests global variable declaration and basic getters
  @Test
  public void testGlobalVarDeclaration_createsNameAndRef() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertNotNull(index.get("a"));
    GlobalNamespace.Name nameA = index.get("a");
    assertEquals("a", nameA.getBaseName());
    assertEquals("a", nameA.getFullName());
    assertEquals("a", nameA.getName());
    assertTrue(nameA.isSimpleName());
    assertEquals(1, nameA.globalSets);
    assertEquals(0, nameA.localSets);
    assertEquals(0, nameA.totalGets);
    assertNotNull(nameA.getDeclaration());
    assertTrue(nameA.getDeclaration().isSet());
    assertNull(gn.getParentScope());
    assertSame(gn, gn.getScope(nameA));
    assertNotNull(gn.getSlot("a"));
    assertNotNull(gn.getOwnSlot("a"));
  }

  // Tests nested property assignment via object literal and property access
  @Test
  public void testObjectLiteralAndPropertyAssignment_createsHierarchy() {
    GlobalNamespace gn = createNamespace("var a = {b: {c: 10}}; a.b.d = 20;");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertNotNull(index.get("a"));
    assertNotNull(index.get("a.b"));
    assertNotNull(index.get("a.b.c"));
    assertNotNull(index.get("a.b.d"));

    GlobalNamespace.Name nameAB = index.get("a.b");
    assertFalse(nameAB.isSimpleName());
    assertEquals("b", nameAB.getBaseName());
    assertEquals("a.b", nameAB.getFullName());
    assertEquals(index.get("a"), nameAB.parent);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("a").type);
  }

  // Tests function declarations and invocation references
  @Test
  public void testFunctionDeclarationAndCall_setsCallGet() {
    GlobalNamespace gn = createNamespace("function foo() {} foo();");
    GlobalNamespace.Name foo = gn.getOwnSlot("foo");

    assertNotNull(foo);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, foo.type);
    assertEquals(1, foo.globalSets);
    assertEquals(1, foo.callGets);
    assertEquals(1, foo.totalGets);
  }

  // Tests local assignments versus global assignments
  @Test
  public void testLocalAssignment_recordsLocalSet() {
    GlobalNamespace gn = createNamespace("var a = 1; function f() { a = 2; }");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");

    assertNotNull(nameA);
    assertEquals(1, nameA.globalSets);
    assertEquals(1, nameA.localSets);
  }

  // Tests aliasing gets through variable assignments
  @Test
  public void testAliasingGet_incrementsAliasingCount() {
    GlobalNamespace gn = createNamespace("var a = {}; var b = a;");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");

    assertNotNull(nameA);
    assertEquals(1, nameA.aliasingGets);
    assertEquals(1, nameA.totalGets);
    assertTrue(nameA.shouldKeepKeys());
  }

  // Tests boolean expressions and short-circuit assignment
  @Test
  public void testBooleanOrExpression_selfAssignmentDetected() {
    GlobalNamespace gn = createNamespace("var a = a || {};");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");

    assertNotNull(nameA);
    assertEquals(1, nameA.globalSets);
    assertEquals(1, nameA.totalGets);
    assertEquals(0, nameA.aliasingGets);
  }

  // Tests hook (ternary) expression
  @Test
  public void testHookExpression_handlesConditionAndBranches() {
    GlobalNamespace gn = createNamespace("var a = true ? b : c;");
    assertNotNull(gn.getOwnSlot("a"));
    assertNotNull(gn.getOwnSlot("b"));
    assertNotNull(gn.getOwnSlot("c"));
  }

  // Tests delete operator on property
  @Test
  public void testDeleteProperty_recordsDeletePropRef() {
    GlobalNamespace gn = createNamespace("var a = {b: 1}; delete a.b;");
    GlobalNamespace.Name nameAB = gn.getOwnSlot("a.b");

    assertNotNull(nameAB);
    assertEquals(1, nameAB.deleteProps);
    assertFalse(nameAB.canCollapse());
  }

  // Tests prototype property access handling
  @Test
  public void testPrototypePrefix_recordsPrototypeGet() {
    GlobalNamespace gn = createNamespace("function Foo() {} Foo.prototype.bar = function() {};");
    GlobalNamespace.Name foo = gn.getOwnSlot("Foo");

    assertNotNull(foo);
    assertTrue(foo.totalGets > 0);
  }

  // Tests increment and decrement operations
  @Test
  public void testIncrementAndDecrement_recordedAsSets() {
    GlobalNamespace gn = createNamespace("var a = 0; a++; --a;");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");

    assertNotNull(nameA);
    assertEquals(3, nameA.globalSets);
  }

  // Tests externs root handling
  @Test
  public void testExternsHandling_marksInExterns() {
    GlobalNamespace gn = createNamespace("var ext = {};", "var local = ext;");
    assertTrue(gn.hasExternsRoot());

    GlobalNamespace.Name ext = gn.getOwnSlot("ext");
    assertNotNull(ext);
    assertTrue(ext.inExterns);
    assertFalse(ext.canCollapse());

    GlobalNamespace.Name local = gn.getOwnSlot("local");
    assertNotNull(local);
    assertFalse(local.inExterns);
  }

  // Tests getAllSymbols and getNameForest
  @Test
  public void testGetAllSymbolsAndForest_returnsCollection() {
    GlobalNamespace gn = createNamespace("var a = 1; var b = 2;");
    List<GlobalNamespace.Name> forest = gn.getNameForest();
    assertEquals(2, forest.size());

    Iterable<GlobalNamespace.Name> symbols = gn.getAllSymbols();
    int count = 0;
    for (GlobalNamespace.Name ignored : symbols) {
      count++;
    }
    assertEquals(2, count);
  }

  // Tests getReferences for a given slot
  @Test
  public void testGetReferences_returnsListForSlot() {
    GlobalNamespace gn = createNamespace("var a = 1; a = 2;");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");
    Iterable<GlobalNamespace.Ref> refs = gn.getReferences(nameA);

    int count = 0;
    for (GlobalNamespace.Ref ignored : refs) {
      count++;
    }
    assertEquals(2, count);
  }

  // Tests scanNewNodes with AST change
  @Test
  public void testScanNewNodes_updatesNamespace() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    assertEquals(1, gn.getNameIndex().size());

    Node newCode = compiler.parseTestCode("var b = 2;");
    Node nameNode = newCode.getFirstChild().getFirstChild(); // NAME 'b'
    GlobalNamespace.AstChange change = new GlobalNamespace.AstChange(null, null, nameNode);
    gn.scanNewNodes(Collections.singletonList(change));

    assertNotNull(gn.getOwnSlot("b"));
  }

  // Tests Ref twins and helper methods
  @Test
  public void testRefTwinsAndHelpers() {
    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref aliasRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.ALIASING_GET);

    GlobalNamespace.Ref.markTwins(setRef, aliasRef);
    assertSame(aliasRef, setRef.getTwin());
    assertSame(setRef, aliasRef.getTwin());

    GlobalNamespace.Ref cloned = setRef.cloneAndReclassify(GlobalNamespace.Ref.Type.DIRECT_GET);
    assertEquals(GlobalNamespace.Ref.Type.DIRECT_GET, cloned.type);
    assertEquals("", setRef.getSourceName());
    assertNull(setRef.getNode());
    assertNull(setRef.getSourceFile());
    assertNull(setRef.getSymbol());
    assertNull(setRef.getModule());
  }

  // Tests Name removeRef and canEliminate logic
  @Test
  public void testName_removeRefAndCanEliminate() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("test", null, false);
    GlobalNamespace.Ref ref = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);

    name.addRef(ref);
    assertEquals(1, name.globalSets);
    assertEquals(ref, name.getDeclaration());

    name.removeRef(ref);
    assertEquals(0, name.globalSets);
    assertNull(name.getDeclaration());
    assertFalse(name.canEliminate());
    assertFalse(name.isTypeInferred());
    assertNull(name.getType());
  }

  // Tests Name declaration type flag propagation
  @Test
  public void testName_setDeclaredType_propagatesToParent() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("ns", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    GlobalNamespace.Name child = parent.addProperty("Sub", false);

    assertFalse(parent.isNamespace());
    child.setDeclaredType();
    assertTrue(child.isDeclaredType());
    assertTrue(parent.isNamespace());
  }

  // Tests Name toString and stub checks
  @Test
  public void testName_stubAndToString() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("x", null, false);
    GlobalNamespace.Ref localRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    name.addRef(localRef);

    assertTrue(name.needsToBeStubbed());
    assertFalse(name.isSimpleStubDeclaration());
    String str = name.toString();
    assertTrue(str.contains("x"));
    assertTrue(str.contains("localSets=1"));
  }

  // Tests Tracker pass
  @Test
  public void testTracker_processTracksSymbols() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    GlobalNamespace.Tracker tracker = new GlobalNamespace.Tracker(
        compiler, ps, Predicates.<String>alwaysTrue());

    Node root = compiler.parseTestCode("var trackedVar = 10;");
    tracker.process(null, root);

    String output = baos.toString();
    assertTrue(output.contains("trackedVar: Added by"));
  }

  // Tests nested assignment twin creation
  @Test
  public void testNestedAssignment_createsTwins() {
    GlobalNamespace gn = createNamespace("var a; var b = (a = 1);");
    GlobalNamespace.Name nameA = gn.getOwnSlot("a");

    assertNotNull(nameA);
    boolean hasTwin = false;
    for (GlobalNamespace.Ref ref : nameA.getRefs()) {
      if (ref.getTwin() != null) {
        hasTwin = true;
        break;
      }
    }
    assertTrue(hasTwin);
  }
}
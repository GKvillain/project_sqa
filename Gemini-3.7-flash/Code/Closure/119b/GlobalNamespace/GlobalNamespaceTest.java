package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.StaticScope;
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

  private GlobalNamespace createNamespace(String externsJs, String js) {
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // Tests basic global variable declaration and slot lookup
  @Test
  public void testGetSlot_globalVarDeclaration_returnsSlot() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    GlobalNamespace.Name slot = gn.getSlot("a");
    assertNotNull(slot);
    assertEquals("a", slot.getBaseName());
    assertEquals("a", slot.getFullName());
    assertEquals("a", slot.getName());
    assertTrue(slot.isSimpleName());
    assertEquals(1, slot.globalSets);
    assertEquals(0, slot.totalGets);
    assertFalse(slot.inExterns);
  }

  // Tests missing slot lookup returns null
  @Test
  public void testGetSlot_nonExistentName_returnsNull() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    assertNull(gn.getSlot("b"));
    assertNull(gn.getOwnSlot("b"));
  }

  // Tests object literal property detection and hierarchy
  @Test
  public void testGetNameIndex_nestedObjectLiterals_buildsPropertyTree() {
    GlobalNamespace gn = createNamespace("var a = {b: {c: 10}};");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    assertTrue(index.containsKey("a"));
    assertTrue(index.containsKey("a.b"));
    assertTrue(index.containsKey("a.b.c"));

    GlobalNamespace.Name aName = index.get("a");
    GlobalNamespace.Name bName = index.get("a.b");
    GlobalNamespace.Name cName = index.get("a.b.c");

    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, aName.type);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, bName.type);
    assertEquals(GlobalNamespace.Name.Type.OTHER, cName.type);
    assertEquals(aName, bName.parent);
    assertEquals(bName, cName.parent);
    assertFalse(bName.isSimpleName());
  }

  // Tests getter and setter definitions in object literals
  @Test
  public void testGetNameIndex_getterAndSetterInObjectLit_identifiesTypes() {
    GlobalNamespace gn = createNamespace("var obj = { get foo() { return 1; }, set foo(v) {} };");
    GlobalNamespace.Name fooName = gn.getSlot("obj.foo");
    assertNotNull(fooName);
    assertTrue(fooName.isGetOrSetDefinition());
    assertFalse(fooName.canCollapse());
  }

  // Tests function declarations in global scope
  @Test
  public void testGetSlot_globalFunctionDeclaration_identifiesFunctionType() {
    GlobalNamespace gn = createNamespace("function foo() {}");
    GlobalNamespace.Name slot = gn.getSlot("foo");
    assertNotNull(slot);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, slot.type);
    assertEquals(1, slot.globalSets);
    assertNotNull(slot.getDeclaration());
  }

  // Tests property assignment via GETPROP
  @Test
  public void testGetSlot_propertyAssignment_createsChildName() {
    GlobalNamespace gn = createNamespace("var a = {}; a.b = function() {};");
    GlobalNamespace.Name bSlot = gn.getSlot("a.b");
    assertNotNull(bSlot);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, bSlot.type);
    assertEquals(1, bSlot.globalSets);
  }

  // Tests local scope assignments vs global assignments
  @Test
  public void testGetSlot_localSet_incrementsLocalSets() {
    GlobalNamespace gn = createNamespace("var a = 1; function f() { a = 2; }");
    GlobalNamespace.Name slot = gn.getSlot("a");
    assertNotNull(slot);
    assertEquals(1, slot.globalSets);
    assertEquals(1, slot.localSets);
    assertFalse(slot.needsToBeStubbed());
  }

  // Tests needsToBeStubbed when name only has local sets
  @Test
  public void testNeedsToBeStubbed_onlyLocalSets_returnsTrue() {
    GlobalNamespace gn = createNamespace("function f() { a = 2; }");
    // Since 'a' is not declared with var, it might be resolved if externs/global
    GlobalNamespace.Name name = new GlobalNamespace.Name("x", null, false);
    name.localSets = 1;
    name.globalSets = 0;
    assertTrue(name.needsToBeStubbed());
  }

  // Tests prototype reference handling
  @Test
  public void testHandleGet_prototypeProperty_handlesPrototypePrefix() {
    GlobalNamespace gn = createNamespace(
        "function MyClass() {} MyClass.prototype.foo = function() {}; MyClass.prototype.foo.bar = 1;");
    GlobalNamespace.Name slot = gn.getSlot("MyClass");
    assertNotNull(slot);
    assertTrue(slot.totalGets > 0);
  }

  // Tests aliasing get and call get reference classification
  @Test
  public void testGetReferences_callAndAliasingGets_countsCorrectly() {
    GlobalNamespace gn = createNamespace("var a = function() {}; var b = a; a();");
    GlobalNamespace.Name slot = gn.getSlot("a");
    assertNotNull(slot);
    assertEquals(1, slot.aliasingGets);
    assertEquals(1, slot.callGets);
    assertEquals(2, slot.totalGets);
  }

  // Tests delete operator on properties
  @Test
  public void testHandleGet_deleteProp_incrementsDeleteProps() {
    GlobalNamespace gn = createNamespace("var a = {b: 1}; delete a.b;");
    GlobalNamespace.Name slot = gn.getSlot("a.b");
    assertNotNull(slot);
    assertEquals(1, slot.deleteProps);
    assertFalse(slot.canCollapse());
  }

  // Tests nested assignment twin references
  @Test
  public void testHandleSetFromGlobal_nestedAssign_createsTwinRef() {
    GlobalNamespace gn = createNamespace("var a; var b = a = 1;");
    GlobalNamespace.Name aSlot = gn.getSlot("a");
    assertNotNull(aSlot);
    List<GlobalNamespace.Ref> refs = aSlot.getRefs();
    boolean foundTwin = false;
    for (GlobalNamespace.Ref r : refs) {
      if (r.getTwin() != null) {
        foundTwin = true;
        assertEquals(r, r.getTwin().getTwin());
      }
    }
    assertTrue(foundTwin);
  }

  // Tests boolean expression and hook get classification
  @Test
  public void testDetermineGetTypeForHookOrBooleanExpr_selfAssign_directGet() {
    GlobalNamespace gn = createNamespace("var a = a || {}; var b = b ? b : {};");
    GlobalNamespace.Name aSlot = gn.getSlot("a");
    GlobalNamespace.Name bSlot = gn.getSlot("b");
    assertNotNull(aSlot);
    assertNotNull(bSlot);
    assertEquals(0, aSlot.aliasingGets);
  }

  // Tests externs root handling and hasExternsRoot
  @Test
  public void testHasExternsRoot_withExterns_returnsTrue() {
    GlobalNamespace gnWithExterns = createNamespace("var ext = {};", "var a = ext;");
    assertTrue(gnWithExterns.hasExternsRoot());
    GlobalNamespace.Name extSlot = gnWithExterns.getSlot("ext");
    assertNotNull(extSlot);
    assertTrue(extSlot.inExterns);

    GlobalNamespace gnWithout = createNamespace("var a = 1;");
    assertFalse(gnWithout.hasExternsRoot());
  }

  // Tests StaticScope and StaticSymbolTable interface implementations
  @Test
  public void testStaticScopeInterfaceMethods() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    assertNull(gn.getParentScope());
    assertNotNull(gn.getRootNode());
    assertNotNull(gn.getTypeOfThis());
    GlobalNamespace.Name aSlot = gn.getSlot("a");
    assertEquals(gn, gn.getScope(aSlot));
    Iterable<GlobalNamespace.Ref> refs = gn.getReferences(aSlot);
    assertNotNull(refs);
    assertTrue(refs.iterator().hasNext());
    Iterable<GlobalNamespace.Name> allSymbols = gn.getAllSymbols();
    assertNotNull(allSymbols);
  }

  // Tests Name methods: canCollapse, canEliminate, declaredType, isNamespace
  @Test
  public void testName_methodsAndProperties() {
    GlobalNamespace.Name rootName = new GlobalNamespace.Name("ns", null, false);
    rootName.type = GlobalNamespace.Name.Type.OBJECTLIT;
    rootName.globalSets = 1;

    GlobalNamespace.Name childName = rootName.addProperty("Sub", false);
    childName.type = GlobalNamespace.Name.Type.FUNCTION;
    childName.globalSets = 1;
    childName.setDeclaredType();

    assertTrue(childName.isDeclaredType());
    assertTrue(rootName.isNamespace());
    assertTrue(childName.canCollapse());
    assertFalse(childName.isTypeInferred());
    assertNull(childName.getType());
    assertNull(childName.getJSDocInfo());
    assertTrue(childName.toString().contains("ns.Sub"));
  }

  // Tests Name.removeRef updates counts and declaration properly
  @Test
  public void testName_removeRef_updatesCounts() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("x", null, false);
    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref getRef = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.ALIASING_GET);

    name.addRef(setRef);
    name.addRef(getRef);

    assertEquals(1, name.globalSets);
    assertEquals(1, name.aliasingGets);
    assertEquals(1, name.totalGets);
    assertEquals(setRef, name.getDeclaration());

    name.removeRef(setRef);
    assertEquals(0, name.globalSets);
    assertNull(name.getDeclaration());

    name.removeRef(getRef);
    assertEquals(0, name.aliasingGets);
    assertEquals(0, name.totalGets);
  }

  // Tests Ref methods and cloneAndReclassify
  @Test
  public void testRef_methods() {
    GlobalNamespace.Ref ref = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    assertTrue(ref.isSet());
    assertNull(ref.getNode());
    assertNull(ref.getSourceFile());
    assertNull(ref.getSymbol());
    assertNull(ref.getModule());
    assertEquals("", ref.getSourceName());

    GlobalNamespace.Ref cloned = ref.cloneAndReclassify(GlobalNamespace.Ref.Type.DIRECT_GET);
    assertEquals(GlobalNamespace.Ref.Type.DIRECT_GET, cloned.type);
    assertFalse(cloned.isSet());
  }

  // Tests markTwins validation check
  @Test(expected = IllegalArgumentException.class)
  public void testRef_markTwins_invalidPair_throwsException() {
    GlobalNamespace.Ref r1 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref r2 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref.markTwins(r1, r2);
  }

  // Tests getNameForest returns list of top level roots
  @Test
  public void testGetNameForest_returnsTopLevelNames() {
    GlobalNamespace gn = createNamespace("var a = 1; var b = 2;");
    List<GlobalNamespace.Name> forest = gn.getNameForest();
    assertEquals(2, forest.size());
  }

  // Tests Tracker pass for tracking symbols added / removed
  @Test
  public void testTracker_process_logsSymbolChanges() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    GlobalNamespace.Tracker tracker = new GlobalNamespace.Tracker(
        compiler, ps, com.google.common.base.Predicates.<String>alwaysTrue());

    Node root1 = compiler.parseTestCode("var a = 1;");
    tracker.process(null, root1);
    String output1 = out.toString();
    assertTrue(output1.contains("a: Added by"));

    out.reset();
    Node root2 = compiler.parseTestCode("var b = 2;");
    tracker.process(null, root2);
    String output2 = out.toString();
    assertTrue(output2.contains("b: Added by"));
    assertTrue(output2.contains("a: Removed by"));
  }

  // Tests scanNewNodes with AstChange data class
  @Test
  public void testScanNewNodes_addsNewAstReferences() {
    GlobalNamespace gn = createNamespace("var a = {};");
    assertEquals(1, gn.getNameIndex().size());

    Node newCode = compiler.parseTestCode("a.b = 1;");
    Node expr = newCode.getFirstChild(); // EXPR_RESULT
    Node assign = expr.getFirstChild(); // ASSIGN
    Node getprop = assign.getFirstChild(); // GETPROP a.b

    List<GlobalNamespace.AstChange> changes = new ArrayList<GlobalNamespace.AstChange>();
    changes.add(new GlobalNamespace.AstChange(null, null, getprop));
    gn.scanNewNodes(changes);

    assertNotNull(gn.getSlot("a.b"));
  }

  // Tests global references inside try-catch block
  @Test
  public void testCatchClause_nameHandling() {
    GlobalNamespace gn = createNamespace("var a = 1; try { } catch (e) { a = 2; }");
    GlobalNamespace.Name aSlot = gn.getSlot("a");
    assertNotNull(aSlot);
    assertEquals(1, aSlot.globalSets);
    assertEquals(1, aSlot.localSets);
  }
}
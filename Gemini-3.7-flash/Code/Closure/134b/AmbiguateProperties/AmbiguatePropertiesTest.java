package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.CheckLevel;
import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for {@link AmbiguateProperties}.
 */
public class AmbiguatePropertiesTest extends CompilerTestCase {
  private static final String EXTERNS =
      "var window; function alert(x) {} var ext; ext.extProp = 1; var goog = {}; goog.inherits = function(x, y) {};";

  public AmbiguatePropertiesTest() {
    super(EXTERNS);
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new AmbiguateProperties(compiler, new char[]{'$'});
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests renaming of properties on a single type
  @Test
  public void testProcess_singleType_renamesPropertiesDeterministically() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.prop1 = 0;\n"
            + "Foo.prototype.prop2 = 0;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 0;\n"
            + "Foo.prototype.b = 0;\n");
  }

  // Tests renaming of properties across two disjoint types reuses names
  @Test
  public void testProcess_twoDisjointTypes_reusesPropertyNames() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.fooProp = 0;\n"
            + "/** @constructor */ function Bar() {}\n"
            + "Bar.prototype.barProp = 0;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 0;\n"
            + "function Bar() {}\n"
            + "Bar.prototype.a = 0;\n");
  }

  // Tests multiple properties on disjoint types sorted by frequency and name
  @Test
  public void testProcess_frequencyAndTieBreaking_colorsGraphCorrectly() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.f1 = 0;\n"
            + "Foo.prototype.f1 = 0;\n"
            + "Foo.prototype.f2 = 0;\n"
            + "/** @constructor */ function Bar() {}\n"
            + "Bar.prototype.b1 = 0;\n"
            + "Bar.prototype.b2 = 0;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 0;\n"
            + "Foo.prototype.a = 0;\n"
            + "Foo.prototype.b = 0;\n"
            + "function Bar() {}\n"
            + "Bar.prototype.a = 0;\n"
            + "Bar.prototype.b = 0;\n");
  }

  // Tests that subtype properties cannot collide with supertype properties
  @Test
  public void testProcess_subclassInheritance_preventsNameCollision() {
    test(
        "/** @constructor */ function Parent() {}\n"
            + "Parent.prototype.parentProp = 1;\n"
            + "/** @constructor \n * @extends {Parent} */ function Child() {}\n"
            + "goog.inherits(Child, Parent);\n"
            + "Child.prototype.childProp = 2;\n",
        "function Parent() {}\n"
            + "Parent.prototype.a = 1;\n"
            + "function Child() {}\n"
            + "goog.inherits(Child, Parent);\n"
            + "Child.prototype.b = 2;\n");
  }

  // Tests that externed properties are not renamed and not assigned to others
  @Test
  public void testProcess_externedProperties_notRenamed() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.extProp = 0;\n"
            + "Foo.prototype.otherProp = 0;\n",
        "function Foo() {}\n"
            + "Foo.prototype.extProp = 0;\n"
            + "Foo.prototype.a = 0;\n");
  }

  // Tests that quoted object literal keys reserve names to prevent collisions
  @Test
  public void testProcess_quotedObjectLiteralKey_reservesName() {
    test(
        "var obj = {'a': 1};\n"
            + "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.myProp = 0;\n",
        "var obj = {'a': 1};\n"
            + "function Foo() {}\n"
            + "Foo.prototype.b = 0;\n");
  }

  // Tests that unquoted object literal keys participate in property renaming
  @Test
  public void testProcess_unquotedObjectLiteralKey_renamed() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.fooProp = 1;\n"
            + "var obj = {unquotedProp: 2};\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n"
            + "var obj = {a: 2};\n");
  }

  // Tests that quoted GETELEM access reserves property names
  @Test
  public void testProcess_quotedElementAccess_reservesName() {
    test(
        "var obj = {};\n"
            + "var val = obj['a'];\n"
            + "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.myProp = 0;\n",
        "var obj = {};\n"
            + "var val = obj['a'];\n"
            + "function Foo() {}\n"
            + "Foo.prototype.b = 0;\n");
  }

  // Tests that properties starting with SKIP_PREFIX are skipped from ambiguating
  @Test
  public void testProcess_skipPrefix_skipsRenamingProperty() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.JSAbstractCompiler_prop = 0;\n"
            + "Foo.prototype.normalProp = 0;\n",
        "function Foo() {}\n"
            + "Foo.prototype.JSAbstractCompiler_prop = 0;\n"
            + "Foo.prototype.a = 0;\n");
  }

  // Tests interface implementations record related types properly
  @Test
  public void testProcess_interfaceImplementation_recordsRelatedTypes() {
    test(
        "/** @interface */ function AnInterface() {}\n"
            + "AnInterface.prototype.shared = function() {};\n"
            + "/** @constructor \n * @implements {AnInterface} */ function Imp() {}\n"
            + "Imp.prototype.shared = function() {};\n"
            + "Imp.prototype.ownProp = function() {};\n",
        "function AnInterface() {}\n"
            + "AnInterface.prototype.a = function() {};\n"
            + "function Imp() {}\n"
            + "Imp.prototype.a = function() {};\n"
            + "Imp.prototype.b = function() {};\n");
  }

  // Tests union types record relationship with alternate types
  @Test
  public void testProcess_unionType_handlesAlternates() {
    test(
        "/** @constructor */ function TypeA() {}\n"
            + "TypeA.prototype.propA = 1;\n"
            + "/** @constructor */ function TypeB() {}\n"
            + "TypeB.prototype.propB = 1;\n"
            + "/** @param {TypeA|TypeB} obj */ function f(obj) {\n"
            + "  obj.commonProp = 2;\n"
            + "}\n",
        "function TypeA() {}\n"
            + "TypeA.prototype.a = 1;\n"
            + "function TypeB() {}\n"
            + "TypeB.prototype.a = 1;\n"
            + "function f(obj) {\n"
            + "  obj.b = 2;\n"
            + "}\n");
  }

  // Tests getRenamingMap after processing
  @Test
  public void testGetRenamingMap_returnsPopulatedMap() {
    Compiler compiler = new Compiler();
    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[]{'$'});
    Map<String, String> map = pass.getRenamingMap();
    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

  // Tests property access on unknown types overlaps with all types
  @Test
  public void testProcess_unknownTypeAccess_conflictsWithAllTypes() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.propFoo = 1;\n"
            + "/** @constructor */ function Bar() {}\n"
            + "Bar.prototype.propBar = 1;\n"
            + "function testUnknown(x) {\n"
            + "  x.unknownProp = 2;\n"
            + "}\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n"
            + "function Bar() {}\n"
            + "Bar.prototype.a = 1;\n"
            + "function testUnknown(x) {\n"
            + "  x.b = 2;\n"
            + "}\n");
  }

  // Tests static properties on constructor functions vs instance properties
  @Test
  public void testProcess_staticAndInstanceProperties_renamedCorrectly() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.staticProp = 1;\n"
            + "Foo.prototype.instanceProp = 2;\n",
        "function Foo() {}\n"
            + "Foo.a = 1;\n"
            + "Foo.prototype.a = 2;\n");
  }

  // Tests prototype object literal assignment
  @Test
  public void testProcess_prototypeObjectLiteral_renamesKeys() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype = {\n"
            + "  itemOne: 1,\n"
            + "  itemTwo: 2\n"
            + "};\n",
        "function Foo() {}\n"
            + "Foo.prototype = {\n"
            + "  a: 1,\n"
            + "  b: 2\n"
            + "};\n");
  }

  // Tests function type properties
  @Test
  public void testProcess_functionTypeProperties_renamesCorrectly() {
    test(
        "/** @type {Function} */ var fn1 = function() {};\n"
            + "fn1.funcProp1 = 10;\n"
            + "/** @type {Function} */ var fn2 = function() {};\n"
            + "fn2.funcProp2 = 20;\n",
        "var fn1 = function() {};\n"
            + "fn1.a = 10;\n"
            + "var fn2 = function() {};\n"
            + "fn2.b = 20;\n");
  }

  // Tests multi-level inheritance chain
  @Test
  public void testProcess_deepInheritanceChain_preventsCollisionsAcrossHierarchy() {
    test(
        "/** @constructor */ function GrandParent() {}\n"
            + "GrandParent.prototype.gpProp = 1;\n"
            + "/** @constructor \n * @extends {GrandParent} */ function Parent() {}\n"
            + "goog.inherits(Parent, GrandParent);\n"
            + "Parent.prototype.pProp = 2;\n"
            + "/** @constructor \n * @extends {Parent} */ function Child() {}\n"
            + "goog.inherits(Child, Parent);\n"
            + "Child.prototype.cProp = 3;\n",
        "function GrandParent() {}\n"
            + "GrandParent.prototype.a = 1;\n"
            + "function Parent() {}\n"
            + "goog.inherits(Parent, GrandParent);\n"
            + "Parent.prototype.b = 2;\n"
            + "function Child() {}\n"
            + "goog.inherits(Child, Parent);\n"
            + "Child.prototype.c = 3;\n");
  }

  // Tests renaming map contents after execution on compiler
  @Test
  public void testGetRenamingMap_afterCompilation_containsMapping() {
    AmbiguateProperties pass = new AmbiguateProperties(getLastCompiler(), new char[]{'$'});
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.uniquePropName = 1;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n");
  }
}
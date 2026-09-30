package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Tests for {@link DisambiguateProperties}.
 */
public class DisambiguatePropertiesTest extends CompilerTestCase {

  private DisambiguateProperties<?> lastPass;
  private boolean runTightenTypes = false;
  private TightenTypes tightenTypes;
  private Map<String, CheckLevel> propertiesToErrorFor;

  public DisambiguatePropertiesTest() {
    enableTypeCheck(CheckLevel.WARNING);
    enableNormalize();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    runTightenTypes = false;
    tightenTypes = null;
    propertiesToErrorFor = Maps.newHashMap();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    if (runTightenTypes) {
      return new CompilerPass() {
        @Override
        public void process(Node externs, Node root) {
          tightenTypes = new TightenTypes(compiler);
          tightenTypes.process(externs, root);
          lastPass = DisambiguateProperties.forConcreteTypeSystem(
              compiler, tightenTypes, propertiesToErrorFor);
          lastPass.process(externs, root);
        }
      };
    } else {
      return new CompilerPass() {
        @Override
        public void process(Node externs, Node root) {
          lastPass = DisambiguateProperties.forJSTypeSystem(
              compiler, propertiesToErrorFor);
          lastPass.process(externs, root);
        }
      };
    }
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests normal property disambiguation between two unrelated constructors
  @Test
  public void testDisambiguateProperties_twoTypes_renamesProperties() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "new Foo().a;\n"
        + "new Bar().a;\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a = 0;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a = 0;\n"
        + "new Foo().Foo_prototype$a;\n"
        + "new Bar().Bar_prototype$a;\n";
    test(js, expected);
  }

  // Tests single type reference which does not trigger renaming
  @Test
  public void testDisambiguateProperties_singleType_doesNotRename() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "new Foo().a;\n";
    testSame(js);
  }

  // Tests object literal property access and definition
  @Test
  public void testDisambiguateProperties_objectLit_handlesPropertyDefinitions() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "var obj = { a: 1 };\n"
        + "new Foo().a;\n"
        + "new Bar().a;\n";
    test(js, js);
  }

  // Tests object literal with typed structure
  @Test
  public void testDisambiguateProperties_typedObjectLit_renamesOrSkips() {
    String js = ""
        + "/** @constructor */ function Foo() { this.a = 1; }\n"
        + "/** @type {{a: number}} */ var x = {a: 2};\n"
        + "new Foo().a;\n";
    testSame(js);
  }

  // Tests invalidation when property is accessed on unknown type
  @Test
  public void testDisambiguateProperties_unknownType_invalidatesProperty() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @type {*} */ var x;\n"
        + "x.a;\n"
        + "new Foo().a;\n"
        + "new Bar().a;\n";
    testSame(js);
  }

  // Tests interface hierarchy and implementors sharing property names
  @Test
  public void testDisambiguateProperties_interfaces_unionsTypesTogether() {
    String js = ""
        + "/** @interface */ function I() {}\n"
        + "I.prototype.a;\n"
        + "/** @constructor @implements {I} */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @type {I} */ var i = new Foo();\n"
        + "i.a;\n"
        + "new Bar().a;\n";
    String expected = ""
        + "function I() {}\n"
        + "I.prototype.I_prototype$a;\n"
        + "function Foo() {}\n"
        + "Foo.prototype.I_prototype$a = 0;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a = 0;\n"
        + "var i = new Foo();\n"
        + "i.I_prototype$a;\n"
        + "new Bar().Bar_prototype$a;\n";
    test(js, expected);
  }

  // Tests union types joining equivalence classes
  @Test
  public void testDisambiguateProperties_unionType_unionsEquivalenceClasses() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @constructor */ function Baz() {}\n"
        + "Baz.prototype.a = 0;\n"
        + "/** @type {Foo|Bar} */ var x = new Foo();\n"
        + "x.a;\n"
        + "new Baz().a;\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.Bar_prototype$a = 0;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a = 0;\n"
        + "function Baz() {}\n"
        + "Baz.prototype.Baz_prototype$a = 0;\n"
        + "var x = new Foo();\n"
        + "x.Bar_prototype$a;\n"
        + "new Baz().Baz_prototype$a;\n";
    test(js, expected);
  }

  // Tests inheritance prototype chain lookup
  @Test
  public void testDisambiguateProperties_subclass_sharesSuperclassRenaming() {
    String js = ""
        + "/** @constructor */ function Super() {}\n"
        + "Super.prototype.a = 0;\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "goog.inherits(Sub, Super);\n"
        + "/** @constructor */ function Other() {}\n"
        + "Other.prototype.a = 0;\n"
        + "new Sub().a;\n"
        + "new Other().a;\n";
    String expected = ""
        + "function Super() {}\n"
        + "Super.prototype.Super_prototype$a = 0;\n"
        + "function Sub() {}\n"
        + "goog.inherits(Sub, Super);\n"
        + "function Other() {}\n"
        + "Other.prototype.Other_prototype$a = 0;\n"
        + "new Sub().Super_prototype$a;\n"
        + "new Other().Other_prototype$a;\n";
    test(js, expected);
  }

  // Tests externs property skipping
  @Test
  public void testDisambiguateProperties_externProperty_skipsRenamingOnType() {
    String externs = "var window; window.a;";
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "new Foo().a;\n"
        + "window.a;\n";
    test(externs, js, js);
  }

  // Tests propertiesToErrorFor diagnostic reporting
  @Test
  public void testDisambiguateProperties_propertyErrorConfigured_reportsWarning() {
    propertiesToErrorFor.put("badProp", CheckLevel.WARNING);
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.badProp = 0;\n"
        + "/** @type {*} */ var x;\n"
        + "x.badProp;\n";
    test(js, js, DisambiguateProperties.Warnings.INVALIDATION);
  }

  // Tests enum property skipping
  @Test
  public void testDisambiguateProperties_enumType_skipsRenaming() {
    String js = ""
        + "/** @enum {string} */ var MyEnum = { A: 'a' };\n"
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.A = 0;\n"
        + "new Foo().A;\n"
        + "var e = MyEnum.A;\n";
    testSame(js);
  }

  // Tests ConcreteTypeSystem pass execution
  @Test
  public void testDisambiguateProperties_concreteTypeSystem_processesSuccessfully() {
    runTightenTypes = true;
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "new Foo().a;\n"
        + "new Bar().a;\n";
    test(js, js);
  }

  // Tests static property disambiguation on constructor functions
  @Test
  public void testDisambiguateProperties_staticProperties_renamesCorrectly() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.a = 0;\n"
        + "Foo.a;\n"
        + "Bar.a;\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.Function$Foo_prototype$a = 0;\n"
        + "function Bar() {}\n"
        + "Bar.Function$Bar_prototype$a = 0;\n"
        + "Foo.Function$Foo_prototype$a;\n"
        + "Bar.Function$Bar_prototype$a;\n";
    test(js, expected);
  }

  // Tests multiple distinct properties on the same types
  @Test
  public void testDisambiguateProperties_multipleProperties_renamesEachIndependently() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "Foo.prototype.b = 2;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 3;\n"
        + "Bar.prototype.b = 4;\n"
        + "var f = new Foo();\n"
        + "f.a; f.b;\n"
        + "var b = new Bar();\n"
        + "b.a; b.b;\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a = 1;\n"
        + "Foo.prototype.Foo_prototype$b = 2;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a = 3;\n"
        + "Bar.prototype.Bar_prototype$b = 4;\n"
        + "var f = new Foo();\n"
        + "f.Foo_prototype$a; f.Foo_prototype$b;\n"
        + "var b = new Bar();\n"
        + "b.Bar_prototype$a; b.Bar_prototype$b;\n";
    test(js, expected);
  }

  // Tests multiple interfaces implemented by a class
  @Test
  public void testDisambiguateProperties_multipleInterfaces_sharesRenaming() {
    String js = ""
        + "/** @interface */ function I1() {}\n"
        + "I1.prototype.a;\n"
        + "/** @interface */ function I2() {}\n"
        + "I2.prototype.a;\n"
        + "/** @constructor @implements {I1} @implements {I2} */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Other() {}\n"
        + "Other.prototype.a = 0;\n"
        + "/** @type {I1} */ var x = new Foo();\n"
        + "x.a;\n"
        + "/** @type {I2} */ var y = new Foo();\n"
        + "y.a;\n"
        + "new Other().a;\n";
    String expected = ""
        + "function I1() {}\n"
        + "I1.prototype.I1_prototype$a;\n"
        + "function I2() {}\n"
        + "I2.prototype.I1_prototype$a;\n"
        + "function Foo() {}\n"
        + "Foo.prototype.I1_prototype$a = 0;\n"
        + "function Other() {}\n"
        + "Other.prototype.Other_prototype$a = 0;\n"
        + "var x = new Foo();\n"
        + "x.I1_prototype$a;\n"
        + "var y = new Foo();\n"
        + "y.I1_prototype$a;\n"
        + "new Other().Other_prototype$a;\n";
    test(js, expected);
  }
}
package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link AmbiguateProperties}.
 */
public class AmbiguatePropertiesTest extends CompilerTestCase {
  private char[] reservedCharacters = new char[]{};

  public AmbiguatePropertiesTest() {
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new AmbiguateProperties(compiler, reservedCharacters);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic property renaming between two unrelated types
  @Test
  public void testProcess_unrelatedTypes_renamesPropertiesToSameName() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.prop1 = 1;\n"
            + "/** @constructor */ function Bar() {}\n"
            + "Bar.prototype.prop2 = 2;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n"
            + "function Bar() {}\n"
            + "Bar.prototype.a = 2;\n");
  }

  // Tests multiple properties on the same type get distinct names
  @Test
  public void testProcess_sameTypeMultipleProps_renamesToDistinctNames() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.alpha = 1;\n"
            + "Foo.prototype.beta = 2;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n"
            + "Foo.prototype.b = 2;\n");
  }

  // Tests inheritance prevents sub and super types from sharing property names
  @Test
  public void testProcess_subclassAndSuperclass_propertiesDoNotAmbiguate() {
    test(
        "/** @constructor */ function Super() {}\n"
            + "Super.prototype.parentProp = 1;\n"
            + "/** @constructor\n * @extends {Super} */ function Sub() {}\n"
            + "Sub.prototype.childProp = 2;\n",
        "function Super() {}\n"
            + "Super.prototype.a = 1;\n"
            + "function Sub() {}\n"
            + "Sub.prototype.b = 2;\n");
  }

  // Tests property with skip prefix is not renamed
  @Test
  public void testProcess_skipPrefixProperty_skipsRenaming() {
    testSame(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.JSAbstractCompiler_skipMe = 1;\n");
  }

  // Tests quoted property names in Object literals are preserved and avoid name collisions
  @Test
  public void testProcess_quotedProperties_preventCollision() {
    testSame("var obj = {'a': 1};\n");
  }

  // Tests quoted property accesses in GETELEM are preserved and avoid collisions
  @Test
  public void testProcess_getElemQuotedProperty_preventsCollision() {
    testSame(
        "var x = {};\n"
            + "x['a'] = 1;\n");
  }

  // Tests extern properties are not renamed
  @Test
  public void testProcess_externProperties_skipsRenaming() {
    test(
        "var externProp;\n",
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.externProp = 1;\n",
        "function Foo() {}\n"
            + "Foo.prototype.externProp = 1;\n",
        null,
        null);
  }

  // Tests properties referenced from interfaces relate implementations
  @Test
  public void testProcess_interfaceImplementation_relatesTypes() {
    test(
        "/** @interface */ function Intf() {}\n"
            + "Intf.prototype.method1 = function() {};\n"
            + "/** @constructor\n * @implements {Intf} */ function Impl() {}\n"
            + "Impl.prototype.method1 = function() {};\n"
            + "Impl.prototype.method2 = function() {};\n",
        "function Intf() {}\n"
            + "Intf.prototype.a = function() {};\n"
            + "function Impl() {}\n"
            + "Impl.prototype.a = function() {};\n"
            + "Impl.prototype.b = function() {};\n");
  }

  // Tests object literal unquoted properties are ambiguated across unrelated types
  @Test
  public void testProcess_objectLiteralUnquotedKeys_ambiguatesProperly() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "/** @constructor */ function Bar() {}\n"
            + "/** @type {Foo} */ var f = {fooProp: 1};\n"
            + "/** @type {Bar} */ var b = {barProp: 2};\n",
        "function Foo() {}\n"
            + "function Bar() {}\n"
            + "var f = {a: 1};\n"
            + "var b = {a: 2};\n");
  }

  // Tests properties accessed on untyped objects are not ambiguated
  @Test
  public void testProcess_untypedObject_skipsAmbiguation() {
    testSame(
        "var obj = {};\n"
            + "obj.unknownProp = 1;\n");
  }

  // Tests union types relate properties across constituent types
  @Test
  public void testProcess_unionTypePropertyAccess_relatesConstituents() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.propFoo = 1;\n"
            + "/** @constructor */ function Bar() {}\n"
            + "Bar.prototype.propBar = 2;\n"
            + "/** @type {Foo|Bar} */ var u;\n"
            + "u.propFoo = 3;\n",
        "function Foo() {}\n"
            + "Foo.prototype.a = 1;\n"
            + "function Bar() {}\n"
            + "Bar.prototype.b = 2;\n"
            + "var u;\n"
            + "u.a = 3;\n");
  }

  // Tests frequency of property references determines name assignment order
  @Test
  public void testProcess_frequencyOrdering_assignsShorterNamesToMoreFrequent() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "Foo.prototype.rare = 1;\n"
            + "Foo.prototype.frequent = 1;\n"
            + "Foo.prototype.frequent = 2;\n",
        "function Foo() {}\n"
            + "Foo.prototype.b = 1;\n"
            + "Foo.prototype.a = 1;\n"
            + "Foo.prototype.a = 2;\n");
  }
}
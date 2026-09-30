package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DisambiguatePropertiesTest extends CompilerTestCase {
  private DisambiguateProperties<JSType> lastPass;

  public DisambiguatePropertiesTest() {
    super();
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        DisambiguateProperties<JSType> pass = DisambiguateProperties.forJSTypeSystem(compiler);
        pass.process(externs, root);
        lastPass = pass;
      }
    };
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests single type accessing a property does not rename
  @Test
  public void testProcess_singleType_doesNotRename() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "var f = new Foo();"
        + "f.a = 1;";
    testSame(js);
  }

  // Tests two unrelated types with same property name get disambiguated
  @Test
  public void testProcess_twoUnrelatedTypes_renamesProperties() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var f = new Foo(); f.a = 1;"
        + "var b = new Bar(); b.a = 2;";
    String expected = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.Foo_prototype$a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = 0;"
        + "var f = new Foo(); f.Foo_prototype$a = 1;"
        + "var b = new Bar(); b.Bar_prototype$a = 2;";
    test(js, expected);
  }

  // Tests subclass inheritance shares the same disambiguated property name
  @Test
  public void testProcess_subclassInheritance_sharesPropertyName() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor \n * @extends {Foo} */ function SubFoo() {}"
        + "SubFoo.prototype.a = 1;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 2;"
        + "var s = new SubFoo(); s.a = 3;"
        + "var b = new Bar(); b.a = 4;";
    String expected = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.Foo_prototype$a = 0;"
        + "/** @constructor \n * @extends {Foo} */ function SubFoo() {}"
        + "SubFoo.prototype.Foo_prototype$a = 1;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = 2;"
        + "var s = new SubFoo(); s.Foo_prototype$a = 3;"
        + "var b = new Bar(); b.Bar_prototype$a = 4;";
    test(js, expected);
  }

  // Tests interface implementations share property name
  @Test
  public void testProcess_interfaceImplementation_sharesPropertyName() {
    String js = "/** @interface */ function I() {}"
        + "I.prototype.a = function() {};"
        + "/** @constructor \n * @implements {I} */ function Foo() {}"
        + "Foo.prototype.a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = function() {};"
        + "var f = new Foo(); f.a();"
        + "var b = new Bar(); b.a();";
    String expected = "/** @interface */ function I() {}"
        + "I.prototype.I_prototype$a = function() {};"
        + "/** @constructor \n * @implements {I} */ function Foo() {}"
        + "Foo.prototype.I_prototype$a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = function() {};"
        + "var f = new Foo(); f.I_prototype$a();"
        + "var b = new Bar(); b.Bar_prototype$a();";
    test(js, expected);
  }

  // Tests externs property definition prevents renaming
  @Test
  public void testProcess_externProperty_preventsRenaming() {
    String externs = "var window; window.a = 0;";
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var f = new Foo(); f.a = 1;"
        + "var b = new Bar(); b.a = 2;";
    test(externs, js, js);
  }

  // Tests union types group property references together
  @Test
  public void testProcess_unionType_groupsProperties() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "/** @constructor */ function Baz() {}"
        + "Baz.prototype.a = 0;"
        + "/** @type {Foo|Bar} */ var u;"
        + "u.a = 1;"
        + "var b = new Baz(); b.a = 2;";
    String expected = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.Bar_prototype$a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = 0;"
        + "/** @constructor */ function Baz() {}"
        + "Baz.prototype.Baz_prototype$a = 0;"
        + "/** @type {Foo|Bar} */ var u;"
        + "u.Bar_prototype$a = 1;"
        + "var b = new Baz(); b.Baz_prototype$a = 2;";
    test(js, expected);
  }

  // Tests unknown type access invalidates property renaming
  @Test
  public void testProcess_unknownType_invalidatesProperty() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var f = new Foo(); f.a = 1;"
        + "var b = new Bar(); b.a = 2;"
        + "/** @type {Object} */ var obj;"
        + "obj.a = 3;";
    testSame(js);
  }

  // Tests object literal properties handling
  @Test
  public void testProcess_objectLiteral_handlesProperties() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var f = new Foo(); f.a = 1;"
        + "var b = new Bar(); b.a = 2;"
        + "var lit = {a: 3};";
    testSame(js);
  }

  // Tests factory method for concrete type system creation
  @Test
  public void testForConcreteTypeSystem_createsInstanceNotNull() {
    Compiler compiler = new Compiler();
    TightenTypes tt = new TightenTypes(compiler);
    DisambiguateProperties<ConcreteType> pass =
        DisambiguateProperties.forConcreteTypeSystem(compiler, tt);
    assertNotNull(pass);
  }

  // Tests factory method for JSType system creation
  @Test
  public void testForJSTypeSystem_createsInstanceNotNull() {
    Compiler compiler = new Compiler();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler);
    assertNotNull(pass);
  }

  // Tests getTypeWithProperty on direct prototype property
  @Test
  public void testGetTypeWithProperty_directProperty_returnsOwnerType() {
    String js = "/** @constructor */ function Foo() {} Foo.prototype.x = 1;"
        + "/** @constructor */ function Bar() {} Bar.prototype.x = 2;"
        + "var f = new Foo(); f.x = 1; var b = new Bar(); b.x = 2;";
    test(js, "/** @constructor */ function Foo() {} Foo.prototype.Foo_prototype$x = 1;"
        + "/** @constructor */ function Bar() {} Bar.prototype.Bar_prototype$x = 2;"
        + "var f = new Foo(); f.Foo_prototype$x = 1; var b = new Bar(); b.Bar_prototype$x = 2;");
    assertNotNull(lastPass);
  }

  // Tests getTypeWithProperty returns null for non-existent property
  @Test
  public void testGetTypeWithProperty_nonExistentProperty_returnsNull() {
    Compiler compiler = new Compiler();
    DisambiguateProperties<JSType> pass = DisambiguateProperties.forJSTypeSystem(compiler);
    JSType stringType = compiler.getTypeRegistry().getNativeType(
        JSTypeNative.STRING_TYPE);
    JSType result = pass.getTypeWithProperty("nonExistentProp", stringType);
    assertNull(result);
  }

  // Tests getTypeWithProperty returns null for prototype property name
  @Test
  public void testGetTypeWithProperty_prototypeProperty_returnsNull() {
    Compiler compiler = new Compiler();
    DisambiguateProperties<JSType> pass = DisambiguateProperties.forJSTypeSystem(compiler);
    ObjectType objType = compiler.getTypeRegistry().getNativeObjectType(
        JSTypeNative.OBJECT_TYPE);
    JSType result = pass.getTypeWithProperty("prototype", objType);
    assertNull(result);
  }

  // Tests multiple distinct properties disambiguated independently
  @Test
  public void testProcess_multipleDistinctProperties_disambiguatedIndependently() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.a = 0; Foo.prototype.b = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = 0; Bar.prototype.b = 0;"
        + "var f = new Foo(); f.a = 1; f.b = 2;"
        + "var b = new Bar(); b.a = 3; b.b = 4;";
    String expected = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.Foo_prototype$a = 0; Foo.prototype.Foo_prototype$b = 0;"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = 0; Bar.prototype.Bar_prototype$b = 0;"
        + "var f = new Foo(); f.Foo_prototype$a = 1; f.Foo_prototype$b = 2;"
        + "var b = new Bar(); b.Bar_prototype$a = 3; b.Bar_prototype$b = 4;";
    test(js, expected);
  }

  // Tests enum type property references are skipped
  @Test
  public void testProcess_enumType_skippedFromRenaming() {
    String js = "/** @enum {string} */ var MyEnum = { VAL_A: 'a', VAL_B: 'b' };"
        + "/** @constructor */ function Foo() {}"
        + "Foo.prototype.VAL_A = 'foo';"
        + "var f = new Foo(); f.VAL_A = 'test';"
        + "var e = MyEnum.VAL_A;";
    testSame(js);
  }

  // Tests static constructor properties disambiguation
  @Test
  public void testProcess_staticProperties_renamed() {
    String js = "/** @constructor */ function Foo() {} Foo.a = 1;"
        + "/** @constructor */ function Bar() {} Bar.a = 2;"
        + "var x = Foo.a; var y = Bar.a;";
    String expected = "/** @constructor */ function Foo() {} Foo.Function$Foo$a = 1;"
        + "/** @constructor */ function Bar() {} Bar.Function$Bar$a = 2;"
        + "var x = Foo.Function$Foo$a; var y = Bar.Function$Bar$a;";
    test(js, expected);
  }

  // Tests interface hierarchy extension
  @Test
  public void testProcess_interfaceHierarchy_sharesName() {
    String js = "/** @interface */ function SuperI() {}"
        + "SuperI.prototype.a = function() {};"
        + "/** @interface \n * @extends {SuperI} */ function SubI() {}"
        + "/** @constructor \n * @implements {SubI} */ function Foo() {}"
        + "Foo.prototype.a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = function() {};"
        + "var f = new Foo(); f.a(); var b = new Bar(); b.a();";
    String expected = "/** @interface */ function SuperI() {}"
        + "SuperI.prototype.SuperI_prototype$a = function() {};"
        + "/** @interface \n * @extends {SuperI} */ function SubI() {}"
        + "/** @constructor \n * @implements {SubI} */ function Foo() {}"
        + "Foo.prototype.SuperI_prototype$a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = function() {};"
        + "var f = new Foo(); f.SuperI_prototype$a(); var b = new Bar(); b.Bar_prototype$a();";
    test(js, expected);
  }

  // Tests multiple interfaces implemented by a single class
  @Test
  public void testProcess_multipleInterfaces_sharesName() {
    String js = "/** @interface */ function I1() {}"
        + "I1.prototype.a = function() {};"
        + "/** @interface */ function I2() {}"
        + "I2.prototype.a = function() {};"
        + "/** @constructor \n * @implements {I1} \n * @implements {I2} */ function Foo() {}"
        + "Foo.prototype.a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.a = function() {};"
        + "var f = new Foo(); f.a(); var b = new Bar(); b.a();";
    String expected = "/** @interface */ function I1() {}"
        + "I1.prototype.I2_prototype$a = function() {};"
        + "/** @interface */ function I2() {}"
        + "I2.prototype.I2_prototype$a = function() {};"
        + "/** @constructor \n * @implements {I1} \n * @implements {I2} */ function Foo() {}"
        + "Foo.prototype.I2_prototype$a = function() {};"
        + "/** @constructor */ function Bar() {}"
        + "Bar.prototype.Bar_prototype$a = function() {};"
        + "var f = new Foo(); f.I2_prototype$a(); var b = new Bar(); b.Bar_prototype$a();";
    test(js, expected);
  }

  // Tests string prototype property access does not invalidate prototype disambiguation
  @Test
  public void testProcess_stringType_handledProperly() {
    String js = "/** @constructor */ function Foo() {}"
        + "Foo.prototype.length = 1;"
        + "var f = new Foo(); f.length = 2;"
        + "var s = 'hello'.length;";
    testSame(js);
  }
}
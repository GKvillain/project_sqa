package com.google.javascript.jscomp;

/**
 * Tests for {@link ProcessClosurePrimitives}.
 */
public class ProcessClosurePrimitivesTest extends CompilerTestCase {

  private boolean rewriteNewDateGoogNow = true;

  public ProcessClosurePrimitivesTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ProcessClosurePrimitives(
        compiler, CheckLevel.ERROR, rewriteNewDateGoogNow);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    rewriteNewDateGoogNow = true;
  }

  // Tests simple single-level goog.provide
  public void testProvide_simpleNamespace_declaresVar() {
    test("goog.provide('foo');", "var foo = {};");
  }

  // Tests multi-level dotted goog.provide
  public void testProvide_nestedNamespace_declaresVarAndProperty() {
    test("goog.provide('foo.bar');", "var foo = {}; foo.bar = {};");
  }

  // Tests duplicate goog.provide reports error
  public void testProvide_duplicateNamespace_reportsError() {
    test("goog.provide('foo'); goog.provide('foo');", (String) null,
        ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR);
  }

  // Tests invalid JS identifier in goog.provide argument
  public void testProvide_invalidIdentifier_reportsError() {
    test("goog.provide('foo.123');", (String) null,
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
  }

  // Tests goog.provide called without argument
  public void testProvide_nullArgument_reportsError() {
    test("goog.provide();", (String) null,
        ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  // Tests goog.provide called with too many arguments
  public void testProvide_tooManyArguments_reportsError() {
    test("goog.provide('foo', 'bar');", (String) null,
        ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  // Tests goog.provide called with non-string argument
  public void testProvide_nonStringArgument_reportsError() {
    test("goog.provide(123);", (String) null,
        ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  // Tests goog.require without corresponding provide
  public void testRequire_missingProvide_reportsError() {
    test("goog.require('missing.namespace');", (String) null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

  // Tests goog.require with valid provide removes the require call
  public void testRequire_validProvide_removesRequire() {
    test("goog.provide('foo.bar'); goog.require('foo.bar');",
        "var foo = {}; foo.bar = {};");
  }

  // Tests function declaration collision with provided namespace
  public void testProvide_functionNamespaceCollision_reportsError() {
    test("goog.provide('Foo'); function Foo() {}", (String) null,
        ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR);
  }

  // Tests valid goog.base in constructor rewrites to super constructor call
  public void testBase_validConstructorCall_rewritesBaseCall() {
    test(
        "function Foo() { goog.base(this); }\n" +
        "goog.inherits(Foo, BaseFoo);",
        "function Foo() { BaseFoo.call(this); }\n" +
        "goog.inherits(Foo, BaseFoo);");
  }

  // Tests valid goog.base in prototype method rewrites to superClass call
  public void testBase_validMethodCall_rewritesSuperMethodCall() {
    test(
        "function Foo() {}\n" +
        "goog.inherits(Foo, BaseFoo);\n" +
        "Foo.prototype.bar = function() { goog.base(this, 'bar', 1); };",
        "function Foo() {}\n" +
        "goog.inherits(Foo, BaseFoo);\n" +
        "Foo.prototype.bar = function() { Foo.superClass_.bar.call(this, 1); };");
  }

  // Tests goog.base without 'this' as first argument reports error
  public void testBase_nonThisArg_reportsError() {
    test("function Foo() { goog.base(null); } goog.inherits(Foo, BaseFoo);", (String) null,
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests goog.base without enclosing goog.inherits reports error
  public void testBase_missingInherits_reportsError() {
    test("function Foo() { goog.base(this); }", (String) null,
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests goog.setCssNameMapping with valid object literal
  public void testSetCssNameMapping_validObjectLiteral_removesCall() {
    test("goog.setCssNameMapping({'active': 'act'});", "");
  }

  // Tests goog.setCssNameMapping with non-string value reports error
  public void testSetCssNameMapping_nonStringValue_reportsError() {
    test("goog.setCssNameMapping({'active': 123});", (String) null,
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  // Tests rewriting new Date(goog.now()) to new Date()
  public void testSimplifyNewDateGoogNow_enabled_rewritesToNewDate() {
    test("var d = new Date(goog.now());", "var d = new Date();");
  }

  // Tests goog.addDependency replacement
  public void testAddDependency_removesCall() {
    test("goog.addDependency('x.js', ['a'], []);", "0;");
  }

  // Tests deep nested provide across independent modules (Defects4J Closure-92 regression test)
  public void testProvide_independentModulesDeepNamespace_ordersCorrectly() {
    JSModule[] modules = createModuleStar(
        "goog.provide('apps');",
        "goog.provide('apps.foo.bar.B');",
        "goog.provide('apps.foo.bar.C');");

    test(modules, new String[] {
        "var apps = {};",
        "apps.foo = {}; apps.foo.bar = {}; apps.foo.bar.B = {};",
        "apps.foo.bar.C = {};"
    });
  }

  // Tests goog.setCssNameMapping with BY_PART style
  public void testSetCssNameMapping_byPartStyle_valid() {
    test("goog.setCssNameMapping({'active': 'act'}, 'BY_PART');", "");
  }

  // Tests goog.setCssNameMapping with BY_WHOLE style
  public void testSetCssNameMapping_byWholeStyle_valid() {
    test("goog.setCssNameMapping({'active': 'act'}, 'BY_WHOLE');", "");
  }

  // Tests goog.setCssNameMapping with invalid style argument reports error
  public void testSetCssNameMapping_invalidStyle_reportsError() {
    test("goog.setCssNameMapping({'active': 'act'}, 'INVALID_STYLE');", (String) null,
        ProcessClosurePrimitives.INVALID_STYLE_ERROR);
  }

  // Tests goog.setCssNameMapping with non-object argument reports error
  public void testSetCssNameMapping_nonObjectLiteral_reportsError() {
    test("goog.setCssNameMapping('notAnObject');", (String) null,
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  // Tests new Date(goog.now()) when rewrite is disabled
  public void testSimplifyNewDateGoogNow_disabled_keepsCall() {
    rewriteNewDateGoogNow = false;
    test("var d = new Date(goog.now());", "var d = new Date(goog.now());");
  }

  // Tests goog.base with method name mismatch reports error
  public void testBase_methodNameMismatch_reportsError() {
    test(
        "function Foo() {}\n" +
        "goog.inherits(Foo, BaseFoo);\n" +
        "Foo.prototype.bar = function() { goog.base(this, 'baz'); };",
        (String) null,
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests goog.base called outside of class definition reports error
  public void testBase_outsideClass_reportsError() {
    test("goog.base(this);", (String) null,
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests provide on existing object variable does not redeclare root
  public void testProvide_existingObjectVar_preservesDeclaration() {
    test("var foo = {}; goog.provide('foo.bar');", "var foo = {}; foo.bar = {};");
  }
}
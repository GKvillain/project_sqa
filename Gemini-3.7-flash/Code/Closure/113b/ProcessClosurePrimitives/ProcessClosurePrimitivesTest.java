package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ProcessClosurePrimitives}.
 */
public class ProcessClosurePrimitivesTest extends CompilerTestCase {

  private CheckLevel requiresLevel = CheckLevel.ERROR;
  private PreprocessorSymbolTable preprocessorSymbolTable;

  public ProcessClosurePrimitivesTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ProcessClosurePrimitives(compiler, preprocessorSymbolTable, requiresLevel);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    requiresLevel = CheckLevel.ERROR;
    preprocessorSymbolTable = null;
  }

  // Tests simple goog.provide replacement
  @Test
  public void testSimpleProvide_singleNamespace_createsVar() {
    test("goog.provide('foo');", "var foo = {};");
  }

  // Tests dotted goog.provide replacement
  @Test
  public void testProvide_dottedNamespace_createsVarAndAssign() {
    test("goog.provide('foo.bar');", "var foo = {}; foo.bar = {};");
  }

  // Tests goog.provide combined with candidate assignment
  @Test
  public void testProvide_withCandidateAssignment_convertsToVar() {
    test("goog.provide('foo'); foo = 1;", "var foo = 1;");
  }

  // Tests goog.provide combined with var declaration
  @Test
  public void testProvide_withVarDeclaration_replacesProvide() {
    test("goog.provide('foo'); var foo = 10;", "var foo = 10;");
  }

  // Tests duplicate goog.provide reporting
  @Test
  public void testProvide_duplicateProvide_reportsError() {
    test("goog.provide('foo'); goog.provide('foo');",
        ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR);
  }

  // Tests function with same name as provided namespace
  @Test
  public void testProvide_functionNamespace_reportsError() {
    test("goog.provide('foo'); function foo() {}",
        ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR);
  }

  // Tests invalid JS identifier name in goog.provide
  @Test
  public void testProvide_invalidIdentifier_reportsError() {
    test("goog.provide('foo.123');",
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
  }

  // Tests valid goog.require removal when symbol is provided
  @Test
  public void testRequire_validProvide_removesRequire() {
    test("goog.provide('foo'); goog.require('foo');", "var foo = {};");
  }

  // Tests goog.require on never provided namespace
  @Test
  public void testRequire_missingProvide_reportsError() {
    test("goog.require('missing.foo');",
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

  // Tests goog.require before goog.provide
  @Test
  public void testRequire_lateProvide_reportsError() {
    test("goog.require('foo'); goog.provide('foo');",
        ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

  // Tests goog.require when requiresLevel is set to OFF
  @Test
  public void testRequire_requiresLevelOff_noErrorReported() {
    requiresLevel = CheckLevel.OFF;
    testSame("goog.require('missing.foo');");
  }

  // Tests goog.base in constructor
  @Test
  public void testBase_constructorCall_rewritesToBaseClassCall() {
    test(
        "function BaseFoo() {}" +
        "function Foo() { goog.base(this); }" +
        "goog.inherits(Foo, BaseFoo);",
        "function BaseFoo() {}" +
        "function Foo() { BaseFoo.call(this); }" +
        "goog.inherits(Foo, BaseFoo);");
  }

  // Tests goog.base with arguments in constructor
  @Test
  public void testBase_constructorCallWithArgs_rewritesToBaseClassCall() {
    test(
        "function BaseFoo(x) {}" +
        "function Foo(x) { goog.base(this, x); }" +
        "goog.inherits(Foo, BaseFoo);",
        "function BaseFoo(x) {}" +
        "function Foo(x) { BaseFoo.call(this, x); }" +
        "goog.inherits(Foo, BaseFoo);");
  }

  // Tests goog.base in prototype method
  @Test
  public void testBase_prototypeMethodCall_rewritesToSuperClassCall() {
    test(
        "function Foo() {}" +
        "goog.inherits(Foo, BaseFoo);" +
        "Foo.prototype.bar = function() { goog.base(this, 'bar'); };",
        "function Foo() {}" +
        "goog.inherits(Foo, BaseFoo);" +
        "Foo.prototype.bar = function() { Foo.superClass_.bar.call(this); };");
  }

  // Tests invalid goog.base usage without this argument
  @Test
  public void testBase_missingThis_reportsError() {
    test("function Foo() { goog.base(); }",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests invalid goog.base usage outside methods/constructors
  @Test
  public void testBase_outsideMethod_reportsError() {
    test("goog.base(this);",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // Tests goog.exportSymbol tracking
  @Test
  public void testExportSymbol_recordsExportedVariables() {
    test("goog.exportSymbol('a.b.c', 1);", "goog.exportSymbol('a.b.c', 1);");
  }

  // Tests goog.addDependency replacement
  @Test
  public void testAddDependency_replacesWithZero() {
    test("goog.addDependency('foo.js', ['foo'], []);", "0;");
  }

  // Tests valid goog.setCssNameMapping with BY_PART style
  @Test
  public void testSetCssNameMapping_byPart_setsMapping() {
    test("goog.setCssNameMapping({'header': 'h', 'footer': 'f'}, 'BY_PART');", "");
  }

  // Tests valid goog.setCssNameMapping with BY_WHOLE style
  @Test
  public void testSetCssNameMapping_byWhole_setsMapping() {
    test("goog.setCssNameMapping({'header': 'h', 'footer': 'f'}, 'BY_WHOLE');", "");
  }

  // Tests goog.setCssNameMapping with non-string values
  @Test
  public void testSetCssNameMapping_nonStringValue_reportsError() {
    test("goog.setCssNameMapping({'header': 123});",
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  // Tests goog.setCssNameMapping with invalid style name
  @Test
  public void testSetCssNameMapping_invalidStyle_reportsError() {
    test("goog.setCssNameMapping({}, 'INVALID_STYLE');",
        ProcessClosurePrimitives.INVALID_STYLE_ERROR);
  }

  // Tests goog.setCssNameMapping with non-object literal argument
  @Test
  public void testSetCssNameMapping_expectedObjectLit_reportsError() {
    test("goog.setCssNameMapping('invalid');",
        ProcessClosurePrimitives.EXPECTED_OBJECTLIT_ERROR);
  }

  // Tests null argument error on provide
  @Test
  public void testProvide_nullArgument_reportsError() {
    test("goog.provide();",
        ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  // Tests too many arguments error on provide
  @Test
  public void testProvide_tooManyArguments_reportsError() {
    test("goog.provide('a', 'b');",
        ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  // Tests non-string argument error on provide
  @Test
  public void testProvide_invalidArgumentType_reportsError() {
    test("goog.provide(123);",
        ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }
}
package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * Tests for {@link RuntimeTypeCheck}.
 */
public class RuntimeTypeCheckTest extends CompilerTestCase {

  private String logFunction = null;

  public RuntimeTypeCheckTest() {
    super(DEFAULT_EXTERNS);
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RuntimeTypeCheck(compiler, logFunction);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    logFunction = null;
  }

  private void testChecks(String js, String expected) {
    test(js,
        "function jscomp_typecheck_checkType(expr, checkers) {" +
        "  for (var i = 0; i < checkers.length; ++i) {" +
        "    var res = checkers[i](expr);" +
        "    if (res != null) {" +
        "      return res;" +
        "    }" +
        "  }" +
        "  jscomp_typecheck_logWarning('Type violation: ' + expr);" +
        "  return expr;" +
        "}" +
        "function jscomp_typecheck_valueChecker(type) {" +
        "  return function(expr) {" +
        "    return typeof expr == type ? expr : null;" +
        "  };" +
        "}" +
        "function jscomp_typecheck_nullChecker(expr) {" +
        "  return expr == null ? expr : null;" +
        "}" +
        "function jscomp_typecheck_interfaceChecker(name) {" +
        "  return function(expr) {" +
        "    return expr['implements__' + name] ? expr : null;" +
        "  };" +
        "}" +
        "function jscomp_typecheck_classChecker(name) {" +
        "  return function(expr) {" +
        "    return expr['instance_of__' + name] ? expr : null;" +
        "  };" +
        "}" +
        "function jscomp_typecheck_externClassChecker(name) {" +
        "  return function(expr) {" +
        "    return expr instanceof window[name] ? expr : null;" +
        "  };" +
        "}" +
        "function jscomp_typecheck_logWarning(warning, expr) {" +
        "}" +
        expected);
  }

  // Tests boolean value type parameter check insertion
  @Test
  public void testValueType_boolean_insertsValueChecker() {
    testChecks(
        "/** @param {boolean} b */ function f(b) {}",
        "function f(b) {" +
        "  jscomp.typecheck.checkType(b, [jscomp.typecheck.valueChecker('boolean')]);" +
        "}");
  }

  // Tests number value type parameter check insertion
  @Test
  public void testValueType_number_insertsValueChecker() {
    testChecks(
        "/** @param {number} n */ function f(n) {}",
        "function f(n) {" +
        "  jscomp.typecheck.checkType(n, [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

  // Tests string value type parameter check insertion
  @Test
  public void testValueType_string_insertsValueChecker() {
    testChecks(
        "/** @param {string} s */ function f(s) {}",
        "function f(s) {" +
        "  jscomp.typecheck.checkType(s, [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

  // Tests null type parameter check insertion
  @Test
  public void testNullType_insertsNullChecker() {
    testChecks(
        "/** @param {null} n */ function f(n) {}",
        "function f(n) {" +
        "  jscomp.typecheck.checkType(n, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

  // Tests union type parameter check with multiple sorted checkers
  @Test
  public void testUnionType_insertsSortedCheckers() {
    testChecks(
        "/** @param {number|string} x */ function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number')," +
        "      jscomp.typecheck.valueChecker('string')" +
        "  ]);" +
        "}");
  }

  // Tests return value type check insertion
  @Test
  public void testReturn_typedFunction_insertsCheckTypeOnReturn() {
    testChecks(
        "/** @return {string} */ function f() { return 'hello'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('hello', [" +
        "      jscomp.typecheck.valueChecker('string')" +
        "  ]);" +
        "}");
  }

  // Tests empty return does not insert check
  @Test
  public void testReturn_emptyReturn_noCheckInserted() {
    testChecks(
        "/** @return {void} */ function f() { return; }",
        "function f() { return; }");
  }

  // Tests class constructor marker insertion and classChecker
  @Test
  public void testClass_userDefined_insertsInstanceMarkerAndClassChecker() {
    testChecks(
        "/** @constructor */ function C() {}" +
        "/** @param {C} c */ function f(c) {}",
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "function f(c) {" +
        "  jscomp.typecheck.checkType(c, [jscomp.typecheck.classChecker('C')]);" +
        "}");
  }

  // Tests interface implementation marker and interfaceChecker
  @Test
  public void testInterface_implemented_insertsImplementsMarkerAndInterfaceChecker() {
    testChecks(
        "/** @interface */ function I() {}" +
        "/** @constructor\n * @implements {I} */ function C() {}" +
        "/** @param {I} i */ function f(i) {}",
        "function I() {}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.interfaceChecker('I')]);" +
        "}");
  }

  // Tests extern class type check uses externClassChecker
  @Test
  public void testClass_externType_insertsExternClassChecker() {
    testChecks(
        "/** @param {Element} e */ function f(e) {}",
        "function f(e) {" +
        "  jscomp.typecheck.checkType(e, [jscomp.typecheck.externClassChecker('Element')]);" +
        "}");
  }

  // Tests multiple parameters get checked sequentially at beginning of function
  @Test
  public void testMultipleParameters_checkedInOrder() {
    testChecks(
        "/** @param {boolean} b\n * @param {string} s */ function f(b, s) {}",
        "function f(b, s) {" +
        "  jscomp.typecheck.checkType(b, [jscomp.typecheck.valueChecker('boolean')]);" +
        "  jscomp.typecheck.checkType(s, [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

  // Tests untyped function has no check inserted
  @Test
  public void testUntypedFunction_noChecksInserted() {
    testChecks(
        "function f(x) { return x; }",
        "function f(x) { return x; }");
  }

  // Tests getBoilerplateCode with null log function generates default
  @Test
  public void testGetBoilerplateCode_nullLogFunction_generatesValidNode() {
    Compiler compiler = new Compiler();
    Node node = RuntimeTypeCheck.getBoilerplateCode(compiler, null);
    assertNotNull(node);
    assertTrue(node.hasChildren());
  }

  // Tests getBoilerplateCode with custom log function replaces placeholder
  @Test
  public void testGetBoilerplateCode_customLogFunction_replacesLogPlaceholder() {
    Compiler compiler = new Compiler();
    String customLog = "function(w, e) { alert(w); }";
    Node node = RuntimeTypeCheck.getBoilerplateCode(compiler, customLog);
    assertNotNull(node);
    assertTrue(compiler.toSource(node).contains("alert(w)"));
  }

  // Tests function type parameter check insertion
  @Test
  public void testValueType_function_insertsValueChecker() {
    testChecks(
        "/** @param {!Function} f */ function g(f) {}",
        "function g(f) {" +
        "  jscomp.typecheck.checkType(f, [jscomp.typecheck.valueChecker('function')]);" +
        "}");
  }

  // Tests union type with null includes nullChecker and valueChecker
  @Test
  public void testUnionWithNull_insertsNullAndValueChecker() {
    testChecks(
        "/** @param {number|null} x */ function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.nullChecker," +
        "      jscomp.typecheck.valueChecker('number')" +
        "  ]);" +
        "}");
  }

  // Tests combined parameter and return value checks in a single function
  @Test
  public void testParamAndReturnCheck_combined() {
    testChecks(
        "/** @param {string} s\n * @return {string} */ function f(s) { return s; }",
        "function f(s) {" +
        "  jscomp.typecheck.checkType(s, [jscomp.typecheck.valueChecker('string')]);" +
        "  return jscomp.typecheck.checkType(s, [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

  // Tests multiple return statements all receive return type checks
  @Test
  public void testMultipleReturns_checksEachReturn() {
    testChecks(
        "/** @param {boolean} cond\n * @return {number} */ function f(cond) {" +
        "  if (cond) { return 1; } else { return 2; }" +
        "}",
        "function f(cond) {" +
        "  jscomp.typecheck.checkType(cond, [jscomp.typecheck.valueChecker('boolean')]);" +
        "  if (cond) {" +
        "    return jscomp.typecheck.checkType(1, [jscomp.typecheck.valueChecker('number')]);" +
        "  } else {" +
        "    return jscomp.typecheck.checkType(2, [jscomp.typecheck.valueChecker('number')]);" +
        "  }" +
        "}");
  }

  // Tests anonymous function expression parameters are checked
  @Test
  public void testFunctionExpression_checksInserted() {
    testChecks(
        "var f = /** @param {number} n */ function(n) {};",
        "var f = function(n) {" +
        "  jscomp.typecheck.checkType(n, [jscomp.typecheck.valueChecker('number')]);" +
        "};");
  }

  // Tests class inheritance adds instance markers for both parent and child
  @Test
  public void testClass_inheritance_markersAdded() {
    testChecks(
        "/** @constructor */ function Parent() {}" +
        "/** @constructor\n * @extends {Parent} */ function Child() {}",
        "function Parent() {}" +
        "Parent.prototype['instance_of__Parent'] = true;" +
        "function Child() {}" +
        "Child.prototype['instance_of__Child'] = true;");
  }

  // Tests class implementing multiple interfaces adds implements markers for all
  @Test
  public void testInterface_multipleImplements() {
    testChecks(
        "/** @interface */ function I1() {}" +
        "/** @interface */ function I2() {}" +
        "/** @constructor\n * @implements {I1}\n * @implements {I2} */ function C() {}",
        "function I1() {}" +
        "function I2() {}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I1'] = true;" +
        "C.prototype['implements__I2'] = true;");
  }
}
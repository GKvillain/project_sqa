package com.google.javascript.jscomp;

import org.junit.Test;

public class CheckAccessControlsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckAccessControls(compiler);
  }

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = super.getOptions();
    options.checkAccessControls = true;
    return options;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  // Tests deprecated name access warning without reason
  @Test
  public void testCheckNameDeprecation_deprecatedFunctionCall_reportsWarning() {
    test(
        "/** @deprecated */ function f() {} f();",
        CheckAccessControls.DEPRECATED_NAME);
  }

  // Tests deprecated name access warning with reason
  @Test
  public void testCheckNameDeprecation_deprecatedFunctionCallWithReason_reportsWarning() {
    test(
        "/** @deprecated Use g instead. */ function f() {} f();",
        CheckAccessControls.DEPRECATED_NAME_REASON);
  }

  // Tests access to deprecated name within a deprecated function is allowed
  @Test
  public void testCheckNameDeprecation_accessInDeprecatedFunction_noWarning() {
    testSame("/** @deprecated */ function f() {} /** @deprecated */ function g() { f(); }");
  }

  // Tests deprecated constructor instantiation warning without reason
  @Test
  public void testCheckConstructorDeprecation_deprecatedClassInstantiated_reportsWarning() {
    test(
        "/** @constructor\n * @deprecated */ function Foo() {} new Foo();",
        CheckAccessControls.DEPRECATED_CLASS);
  }

  // Tests deprecated constructor instantiation warning with reason
  @Test
  public void testCheckConstructorDeprecation_deprecatedClassWithReason_reportsWarning() {
    test(
        "/** @constructor\n * @deprecated Use Bar instead. */ function Foo() {} new Foo();",
        CheckAccessControls.DEPRECATED_CLASS_REASON);
  }

  // Tests deprecated property access warning without reason
  @Test
  public void testCheckPropertyDeprecation_deprecatedPropertyAccess_reportsWarning() {
    test(
        "/** @constructor */ function Foo() {} "
            + "/** @deprecated */ Foo.prototype.bar = function() {}; "
            + "function test() { (new Foo()).bar(); }",
        CheckAccessControls.DEPRECATED_PROP);
  }

  // Tests deprecated property access warning with reason
  @Test
  public void testCheckPropertyDeprecation_deprecatedPropertyWithReason_reportsWarning() {
    test(
        "/** @constructor */ function Foo() {} "
            + "/** @deprecated Use baz instead. */ Foo.prototype.bar = function() {}; "
            + "function test() { (new Foo()).bar(); }",
        CheckAccessControls.DEPRECATED_PROP_REASON);
  }

  // Tests assignment to deprecated property does not trigger warning
  @Test
  public void testCheckPropertyDeprecation_assignmentToDeprecatedProperty_noWarning() {
    testSame(
        "/** @constructor */ function Foo() {} "
            + "/** @deprecated */ Foo.prototype.bar = 1; "
            + "function test() { var f = new Foo(); f.bar = 2; }");
  }

  // Tests private global variable access across different files
  @Test
  public void testCheckNameVisibility_privateVariableAccessedOutsideFile_reportsWarning() {
    test(
        new String[] {
          "/** @private */ var x = 1;",
          "var y = x;"
        },
        CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

  // Tests private global variable access within the same file is allowed
  @Test
  public void testCheckNameVisibility_privateVariableAccessedInSameFile_noWarning() {
    testSame("/** @private */ var x = 1; var y = x;");
  }

  // Tests private property access outside defining file and class
  @Test
  public void testCheckPropertyVisibility_privatePropertyAccessedOutsideClass_reportsWarning() {
    test(
        new String[] {
          "/** @constructor */ function Foo() { /** @private */ this.x_ = 1; }",
          "function f() { var foo = new Foo(); var y = foo.x_; }"
        },
        CheckAccessControls.BAD_PRIVATE_PROPERTY_ACCESS);
  }

  // Tests private property access within the same class is allowed
  @Test
  public void testCheckPropertyVisibility_privatePropertyAccessedInsideClass_noWarning() {
    testSame(
        "/** @constructor */ function Foo() { /** @private */ this.x_ = 1; } "
            + "Foo.prototype.getX = function() { return this.x_; };");
  }

  // Tests protected property access from an unrelated class
  @Test
  public void testCheckPropertyVisibility_protectedPropertyAccessedByNonSubclass_reportsWarning() {
    test(
        new String[] {
          "/** @constructor */ function Foo() {} /** @protected */ Foo.prototype.x = 1;",
          "/** @constructor */ function Bar() {} Bar.prototype.f = function() { return (new Foo()).x; };"
        },
        CheckAccessControls.BAD_PROTECTED_PROPERTY_ACCESS);
  }

  // Tests protected property access from a subclass is allowed
  @Test
  public void testCheckPropertyVisibility_protectedPropertyAccessedBySubclass_noWarning() {
    test(
        new String[] {
          "/** @constructor */ function Foo() {} /** @protected */ Foo.prototype.x = 1;",
          "/** @constructor\n * @extends {Foo} */ function SubFoo() {} "
              + "SubFoo.prototype.f = function() { return this.x; };"
        });
  }

  // Tests overriding private property of a parent class in another file
  @Test
  public void testCheckPropertyVisibility_overridePrivateProperty_reportsWarning() {
    test(
        new String[] {
          "/** @constructor */ function Foo() {} /** @private */ Foo.prototype.x_ = 1;",
          "/** @constructor\n * @extends {Foo} */ function SubFoo() {} SubFoo.prototype.x_ = 2;"
        },
        CheckAccessControls.PRIVATE_OVERRIDE);
  }

  // Tests overriding property with mismatched visibility modifier
  @Test
  public void testCheckPropertyVisibility_visibilityMismatch_reportsWarning() {
    test(
        new String[] {
          "/** @constructor */ function Foo() {} /** @protected */ Foo.prototype.x = 1;",
          "/** @constructor\n * @extends {Foo} */ function SubFoo() {} "
              + "/** @public */ SubFoo.prototype.x = 2;"
        },
        CheckAccessControls.VISIBILITY_MISMATCH);
  }

  // Tests legal access to private constructor (instanceof check)
  @Test
  public void testCheckPropertyVisibility_privateConstructorInstanceof_noWarning() {
    test(
        new String[] {
          "/** @constructor\n * @private */ function Foo() {}",
          "function check(x) { return x instanceof Foo; }"
        });
  }

  // Tests reassignment of constant property reports warning
  @Test
  public void testCheckConstantProperty_constantReassigned_reportsWarning() {
    test(
        "/** @constructor */ function Foo() {}\n"
            + "/** @const */ Foo.prototype.BAR = 1;\n"
            + "Foo.prototype.BAR = 2;",
        CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  // Tests single assignment to constant property is allowed
  @Test
  public void testCheckConstantProperty_constantAssignedOnce_noWarning() {
    testSame(
        "/** @constructor */ function Foo() {}\n"
            + "/** @const */ Foo.prototype.BAR = 1;");
  }
}
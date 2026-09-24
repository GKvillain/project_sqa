package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
  }

  private void check(String js) {
    check("", js);
  }

  private void check(String externs, String js) {
    List<SourceFile> externsFiles = Collections.singletonList(
        SourceFile.fromCode("externs.js", externs)
    );
    List<SourceFile> inputFiles = Collections.singletonList(
        SourceFile.fromCode("testcode.js", js)
    );
    compiler.compile(externsFiles, inputFiles, options);
  }

  // Tests interface inheritance conflict check when extending non-existent interface (Closure-2 bug)
  @Test
  public void testCheckInterfaceConflictProperties_nonExistentInterface_handlesNullImplicitProto() {
    String js = ""
        + "/** @interface */\n"
        + "function I1() {}\n"
        + "I1.prototype.bar = function() {};\n"
        + "/** @interface\n"
        + " * @extends {nonExistentInterface}\n"
        + " */\n"
        + "function I2() {}\n"
        + "/** @interface\n"
        + " * @extends {I1}\n"
        + " * @extends {I2}\n"
        + " */\n"
        + "function I3() {}\n";
    check(js);
  }

  // Tests constructor called without new operator emits warning
  @Test
  public void testVisitCall_constructorWithoutNew_emitsWarning() {
    String js = ""
        + "/** @constructor */\n"
        + "function Foo() {}\n"
        + "Foo();\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests non-callable type invocation emits warning
  @Test
  public void testVisitCall_nonCallableType_emitsWarning() {
    String js = "var x = 5; x();";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests function call with wrong argument count
  @Test
  public void testVisitCall_wrongArgumentCount_emitsWarning() {
    String js = ""
        + "/**\n"
        + " * @param {number} a\n"
        + " * @param {number} b\n"
        + " */\n"
        + "function f(a, b) {}\n"
        + "f(1);\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests struct property addition after construction emits warning
  @Test
  public void testCheckPropCreation_structPropertyAddedAfterConstruction_emitsWarning() {
    String js = ""
        + "/** @struct\n"
        + " * @constructor */\n"
        + "function Foo() { this.x = 1; }\n"
        + "var f = new Foo();\n"
        + "f.y = 2;\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests struct with IN operator emits warning
  @Test
  public void testVisit_inOperatorOnStruct_emitsWarning() {
    String js = ""
        + "/** @struct\n"
        + " * @constructor */\n"
        + "function Foo() {}\n"
        + "var f = new Foo();\n"
        + "var res = 'x' in f;\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests non-empty interface function body emits warning
  @Test
  public void testVisitInterfaceGetprop_interfaceFunctionNotEmpty_emitsWarning() {
    String js = ""
        + "/** @interface */\n"
        + "function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests overriding prototype with non-object emits warning
  @Test
  public void testVisitAssign_overridingPrototypeWithNonObject_emitsWarning() {
    String js = ""
        + "/** @constructor */\n"
        + "function Foo() {}\n"
        + "Foo.prototype = 123;\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests conflicting extended type when constructor extends interface
  @Test
  public void testVisitFunction_constructorExtendsInterface_emitsWarning() {
    String js = ""
        + "/** @interface */\n"
        + "function AnInterface() {}\n"
        + "/** @constructor\n"
        + " * @extends {AnInterface} */\n"
        + "function AClass() {}\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests bad bitwise operation on non-integer type
  @Test
  public void testVisitBinaryOperator_bitOperationOnString_emitsWarning() {
    String js = "var x = 'hello' & 1;";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests deterministic equality comparison
  @Test
  public void testVisit_deterministicTestComparison_emitsWarning() {
    String js = ""
        + "var x = 1;\n"
        + "var y = 'test';\n"
        + "var z = (x === y);\n";
    check(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests valid type annotations produce no errors
  @Test
  public void testProcess_validCode_producesNoError() {
    String js = ""
        + "/**\n"
        + " * @param {number} x\n"
        + " * @return {number}\n"
        + " */\n"
        + "function addOne(x) {\n"
        + "  return x + 1;\n"
        + "}\n"
        + "var a = addOne(5);\n";
    check(js);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests typed percent calculation on normal input returns expected percentage
  @Test
  public void testGetTypedPercent_normalInput_returnsExpectedPercentage() {
    String js = "var x = 10; var y = 20; var z = x + y;";
    check(js);
    double percent = compiler.getTypedPercent();
    assertTrue(percent >= 0.0 && percent <= 100.0);
  }
}
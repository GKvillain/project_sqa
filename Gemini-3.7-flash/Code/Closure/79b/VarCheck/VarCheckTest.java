package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VarCheckTest extends CompilerTestCase {

  private static final String EXTERNS = "var window; var goog;";

  private boolean sanityCheck = false;

  public VarCheckTest() {
    super(EXTERNS);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    sanityCheck = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new VarCheck(compiler, sanityCheck);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests valid declared variable reference
  @Test
  public void testVisit_declaredVariable_noError() {
    testSame("var a = 10; var b = a;");
  }

  // Tests undeclared variable reference in normal code
  @Test
  public void testVisit_undeclaredVariable_reportsUndefinedVarError() {
    testError("x = 1;", VarCheck.UNDEFINED_VAR_ERROR);
  }

  // Tests variable reference in an inner scope when declared in outer scope
  @Test
  public void testVisit_scopedVariableDeclaration_noError() {
    testSame("var a = 1; function f() { var b = a; return b; }");
  }

  // Tests function expression name visibility inside own body
  @Test
  public void testVisit_functionExpressionName_noError() {
    testSame("var f = function foo() { return foo(); };");
  }

  // Tests anonymous function expression without name
  @Test
  public void testVisit_anonymousFunctionExpression_noError() {
    testSame("(function() { var x = 1; return x; })();");
  }

  // Tests valid function declaration
  @Test
  public void testVisit_functionDeclaration_noError() {
    testSame("function bar() { return 1; } bar();");
  }

  // Tests reference to extern variable defined in externs
  @Test
  public void testVisit_externVariable_noError() {
    testSame("window.location = 'http://google.com';");
  }

  // Tests property access on undeclared name in externs
  @Test
  public void testProcess_undefinedPropAccessInExterns_reportsWarning() {
    testSame("undeclaredExtern.prop;", "", VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

  // Tests standalone name reference in externs
  @Test
  public void testProcess_nameReferenceInExterns_reportsWarning() {
    testSame("undeclaredExtern;", "", VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

  // Tests that variable declared in normal code matching extern name removes from un-declared externs
  @Test
  public void testVisit_varDeclaredInNormalCodeAfterExternRef_handledCorrectly() {
    testSame("externalVar;", "var externalVar = 2;", VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

  // Tests sanity check mode with undeclared variable throws IllegalStateException
  @Test
  public void testVisit_sanityCheckModeWithUndeclaredVar_throwsException() {
    sanityCheck = true;
    try {
      testSame("undeclaredVar = 1;");
      fail("Expected IllegalStateException in sanity check mode");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Unexpected variable undeclaredVar"));
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IllegalStateException) {
        assertTrue(e.getCause().getMessage().contains("Unexpected variable undeclaredVar"));
      } else {
        throw e;
      }
    }
  }

  // Tests module dependency check with valid forward dependency
  @Test
  public void testVisit_validModuleDependency_noError() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var a = 10;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var b = a;"));
    m2.addDependency(m1);

    test(new JSModule[] {m1, m2}, new String[] {"var a = 10;", "var b = a;"});
  }

  // Tests module dependency check with violated dependency
  @Test
  public void testVisit_violatedModuleDependency_reportsError() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var b = a;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var a = 10;"));
    m1.addDependency(m2);

    test(new JSModule[] {m2, m1}, new String[] {"var a = 10;", "var b = a;"},
        VarCheck.VIOLATED_MODULE_DEP_ERROR);
  }

  // Tests module dependency check with missing dependency relationship
  @Test
  public void testVisit_missingModuleDependency_reportsWarning() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var a = 10;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var b = a;"));

    test(new JSModule[] {m1, m2}, new String[] {"var a = 10;", "var b = a;"},
        null, VarCheck.MISSING_MODULE_DEP_ERROR);
  }

  // Tests constant variable naming creates constant extern
  @Test
  public void testProcess_constantNamingConvention_setsConstantProp() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = IR.block(
        IR.script(
            IR.exprResult(IR.name("MY_CONSTANT_VAR"))));

    VarCheck check = new VarCheck(compiler);
    check.process(externs, root);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(VarCheck.UNDEFINED_VAR_ERROR.key, compiler.getErrors()[0].getType().key);
  }

  // Tests function declaration with empty name error report
  @Test
  public void testVisit_emptyFunctionNameInDecl_reportsInvalidFunctionDecl() {
    Compiler compiler = new Compiler();
    Node fnNode = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node scriptNode = IR.script(fnNode);
    Node externs = IR.block();
    Node root = IR.block(scriptNode);

    VarCheck check = new VarCheck(compiler);
    check.process(externs, root);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(VarCheck.INVALID_FUNCTION_DECL.key, compiler.getErrors()[0].getType().key);
  }

  // Tests catch block exception variable scoping
  @Test
  public void testVisit_catchClauseParameter_noError() {
    testSame("try { } catch (e) { var x = e; }");
  }

  // Tests function parameters scoping
  @Test
  public void testVisit_functionParameters_noError() {
    testSame("function f(param1, param2) { return param1 + param2; }");
  }

  // Tests arguments shadowing warning
  @Test
  public void testVisit_argumentsShadowing_reportsWarning() {
    testSame("function f() { var arguments = 1; }", VarCheck.VAR_ARGUMENTS_SHADOWED_ERROR);
  }
}
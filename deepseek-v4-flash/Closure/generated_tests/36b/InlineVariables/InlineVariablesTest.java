package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode;
  private boolean inlineAllStrings;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = false;
  }

  @Test
  public void testAllModeSimpleInline() {
    mode = InlineVariables.Mode.ALL;
    test("var x = 1; var y = x;", "var y = 1;");
  }

  @Test
  public void testAllModeInlineMultipleUses() {
    mode = InlineVariables.Mode.ALL;
    test("var x = 1; var y = x; var z = x;", "var y = 1; var z = 1;");
  }

  @Test
  public void testAllModeNotInlineExported() {
    mode = InlineVariables.Mode.ALL;
    testSame("/** @export */ var x = 1; var y = x;");
  }

  @Test
  public void testAllModeNotInlineForLoopInit() {
    mode = InlineVariables.Mode.ALL;
    testSame("for(var x = 0; x < 10; x++) { var y = x; }");
  }

  @Test
  public void testAllModeAliasInlining() {
    mode = InlineVariables.Mode.ALL;
    test("var a = 1; var b = a; var c = b;", "var c = 1;");
  }

  @Test
  public void testAllModeChainInlining() {
    mode = InlineVariables.Mode.ALL;
    test("var b = 1; var a = b; var c = a;", "var c = 1;");
  }

  @Test
  public void testAllModeControlFlowPreventsInlining() {
    mode = InlineVariables.Mode.ALL;
    testSame("var x = 1; if (true) { var y = x; }");
  }

  @Test
  public void testAllModeGetPropFunctionNotInlined() {
    mode = InlineVariables.Mode.ALL;
    testSame("var a = obj.method; a();");
  }

  @Test
  public void testAllModeNeverAssignedVariable() {
    mode = InlineVariables.Mode.ALL;
    test("var x; var y = x;", "var y = void 0;");
  }

  @Test
  public void testLocalsOnlyModeLocalInlinedGlobalNot() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("function f() { var x = 1; var y = x; }",
         "function f() { var y = 1; }");
    mode = InlineVariables.Mode.LOCALS_ONLY;
    testSame("var x = 1; var y = x;");
  }

  @Test
  public void testConstantsOnlyModeConstantInlinedNonConstantNot() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("/** @const */ var x = 1; var y = x;", "var y = 1;");
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    testSame("var x = 1; var y = x;");
  }

  @Test
  public void testAllModeWithInlineAllStrings() {
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = true;
    test("var x = 'longstring'; var y = x;", "var y = 'longstring';");
  }
}